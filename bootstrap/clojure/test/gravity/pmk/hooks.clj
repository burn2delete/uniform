(ns gravity.pmk.hooks
  "Source, machine-result and publication hooks for the first PMK request."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.pmk.acceptance :as pmk]
            [gravity.pmk.controller :as controller]
            [gravity.pmk.first-request :as first]))

(def io-keys #{:git! :snapshot! :runtime! :hash! :read! :write-new!})

(defn- need! [p reason]
  (when-not p (throw (ex-info "PMK hook refused" {:reason reason}))))

(defn- utf8 [value] (.getBytes ^String value "UTF-8"))

(defn decode-result [data]
  (binding [*read-eval* false]
    (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                    (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                    (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
          text (str (.decode decoder (java.nio.ByteBuffer/wrap data)))]
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
      (let [eof (Object.) options {:eof eof :readers {} :default (fn [& _] (need! false :result-tag))}
            result (edn/read options r)]
        (need! (map? result) :result-shape)
        (need! (identical? eof (edn/read options r)) :result-trailing-form)
        result)))))

(defn make-hooks
  "Bind immutable reviewed inputs to actual observations. Ports are injected
  only for core controls; normal-io below supplies the real read/create-new
  operations. Constructing hooks observes and executes nothing."
  [{:keys [request manifest observation binding files artifact-limit-bytes]} ports]
  (need! (and (= io-keys (set (keys ports))) (every? fn? (vals ports))) :io-ports)
  (need! (and (map? files) (seq files)
               (every? (fn [[path hash]] (and (string? path) (.isAbsolute (io/file path))
                                             (string? hash) (re-matches #"[0-9a-f]{64}" hash))) files)) :file-pins)
  (need! (and (integer? artifact-limit-bytes) (pos? artifact-limit-bytes)) :artifact-limit)
  (let [check-bound! (fn [bound]
                       (need! (= request (:request bound)) :bound-request)
                       (need! (= binding (:binding bound)) :bound-configuration))
        publish-file! (fn [path data limit]
                        (need! (<= (alength ^bytes data) limit) :capture-size)
                        ((:write-new! ports) path data))
        plan (assoc (:parent-plan request) :selection (:selection request) :argv (:argv request)
                    :closure [(first (:closure (:parent-plan request)))])]
    {:bookend!
     (fn [bound]
       (check-bound! bound)
       (doseq [[path hash] files] (need! (= hash ((:hash! ports) path)) :consumed-file-drift))
       (let [before ((:git! ports))
             _ (need! (= "" (:tracked-status before)) :dirty-source)
             identity (select-keys before [:head :tree])
             _ (need! (= (:checkout request) (:source-revision request) identity) :git-identity)
             snapshot ((:snapshot! ports))
             runtime ((:runtime! ports))
             actual (first/prepare manifest
                      (assoc observation :before identity :after identity :source-revision identity :root (:root snapshot))
                      snapshot (:operation-id request) (:outputs request))
             after ((:git! ports))]
         (need! (= before after) :git-bookends)
         (need! (= request actual) :source-request-drift)
         (need! (= (:runtime binding) runtime) :runtime-drift)
         (controller/expected-bookend actual (assoc binding :runtime runtime))))
     :capture!
     (fn [bound _process]
       (check-bound! bound)
       (let [limit (get-in binding [:limits :capture-bytes])
             data ((:read! ports) (get-in request [:outputs :result]) limit)
             _ (need! (<= (alength ^bytes data) limit) :capture-size)
             result (decode-result data)]
         (pmk/attribute plan plan result)
         result))
     :publish!
     (fn [receipt]
       (need! (and (= :pmk-first-controller-result-v1 (:schema receipt))
                    (= (:operation-id request) (:operation-id receipt))
                    (= (first/digest request) (:request-sha256 receipt))
                    (#{:passed :failed} (:status receipt))) :terminal-identity)
       (let [available? (and (string? (get-in receipt [:process :stdout]))
                             (string? (get-in receipt [:process :stderr])))
             _ (need! (or (= :failed (:status receipt)) available?) :capture-unavailable)
             stdout (utf8 (or (get-in receipt [:process :stdout]) ""))
             stderr (utf8 (or (get-in receipt [:process :stderr]) ""))
             terminal (assoc receipt :capture-state (if available? :reported :unavailable)
                                     :capture-sha256 {:stdout (pmk/sha stdout) :stderr (pmk/sha stderr)})
             data (utf8 (str (pr-str terminal) "\n"))
             limit (get-in binding [:limits :capture-bytes])]
         ;; Check every size before any write. A partial native write failure
         ;; remains terminal/uncertain; these hooks never overwrite or retry.
         (need! (and (<= (alength stdout) limit) (<= (alength stderr) limit)
                      (<= (alength data) artifact-limit-bytes)) :capture-size)
         (publish-file! (get-in request [:outputs :stdout]) stdout limit)
         (publish-file! (get-in request [:outputs :stderr]) stderr limit)
         (publish-file! (get-in request [:outputs :terminal]) data artifact-limit-bytes)))}))

(defn- hash-file [path]
  (pmk/sha (java.nio.file.Files/readAllBytes (.toPath (io/file path)))))

(defn- classpath-entry [path]
  (let [f (.getCanonicalFile (io/file path))]
    {:path (str f)
     :sha256 (if (.isFile f) (hash-file f)
               (first/digest
                (into (sorted-map)
                      (for [child (file-seq f) :when (.isFile child)]
                        [(str (.relativize (.toPath f) (.toPath child))) (hash-file child)]))))}))

(defn observe-runtime [clojure-path]
  (let [environment (first/digest (into (sorted-map) (System/getenv)))
        classpath (System/getProperty "java.class.path")
        cwd (.getCanonicalPath (io/file "."))]
    {:ordered-classpath (mapv classpath-entry (str/split classpath (re-pattern java.io.File/pathSeparator)))
     :java (classpath-entry (str (System/getProperty "java.home") "/bin/java"))
     :clojure (classpath-entry clojure-path)
     :environment-sha256 environment
     :config-sha256 (first/digest {:launcher :direct-java :jvm-options ["-Xmx2g" "-XX:-OmitStackTraceInFastThrow"]
                                  :classpath classpath :cwd cwd :jvm-version (System/getProperty "java.runtime.version")
                                  :java-home (System/getProperty "java.home") :environment-sha256 environment})}))

(defn normal-io
  "The Git binary and Clojure identity path must be pinned in files. No
  lease, compiler, target or child process interface is supplied here."
  [{:keys [request binding files]}]
  (let [cwd (:cwd request) clojure-path (get-in binding [:runtime :clojure :path])]
    (need! (and (contains? files "/usr/bin/git") (contains? files clojure-path)) :executable-pins)
    {:git! (fn []
             (let [git (fn [& args]
                         (let [r (apply shell/sh "/usr/bin/git" "--no-optional-locks" "-c" "core.fsmonitor=false"
                                        (concat args [:dir cwd]))]
                           (need! (zero? (:exit r)) :git-read)
                           (str/trim-newline (:out r))))]
               {:head (git "rev-parse" "HEAD") :tree (git "rev-parse" "HEAD^{tree}")
                :tracked-status (git "status" "--porcelain" "--untracked-files=no" "--no-ahead-behind")}))
     :snapshot! #(pmk/capture cwd)
     :runtime! #(observe-runtime clojure-path)
     :hash! hash-file
     :read! (fn [path limit]
              (need! (and (integer? limit) (pos? limit) (< limit Integer/MAX_VALUE)) :capture-limit-representability)
              (with-open [stream (java.nio.file.Files/newInputStream (.toPath (io/file path))
                                   (into-array java.nio.file.OpenOption [java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
                (let [data (.readNBytes stream (int (inc limit)))]
                  (need! (<= (alength data) limit) :capture-size) data)))
     :write-new! (fn [path data]
                   (java.nio.file.Files/write (.toPath (io/file path)) ^bytes data
                     (into-array java.nio.file.OpenOption [java.nio.file.StandardOpenOption/CREATE_NEW
                                                          java.nio.file.StandardOpenOption/WRITE])))}))
