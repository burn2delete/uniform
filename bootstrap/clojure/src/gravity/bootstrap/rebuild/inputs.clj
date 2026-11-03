(ns gravity.bootstrap.rebuild.inputs
  "Validate the declared inputs for one bootstrap rebuild.

  This namespace is bootstrap tooling: it does not define Gravity semantics or
  certify a self-hosted compiler.  It authenticates the exact source closure,
  seed and build declaration before handing the verified record to a caller's
  rebuild executor."

  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]
           [java.math BigInteger]
           [java.nio.file Files]
           [java.security MessageDigest]))

(def schema :gravity/bootstrap-inputs-v1)

(def ^:private required-keys
  #{:schema :input-id :stage :compiler :source-roots :direct-inputs
    :source-graph :scope :execution :comparison})

(def ^:private allowed-compiler-keys
  #{:source-language :seed-language :seed-artifact-id})

(def ^:private allowed-root-keys #{:path :role :extensions})
(def ^:private allowed-direct-input-keys #{:path :role :sha256})

(def ^:private diagnostic-codes
  {:malformed "BOOT-INPUTS-MALFORMED"
   :path "BOOT-INPUTS-PATH"
   :missing-root "BOOT-INPUTS-MISSING-ROOT"
   :missing-input "BOOT-INPUTS-MISSING-INPUT"
   :digest "BOOT-INPUTS-DIRECT-DIGEST-MISMATCH"
   :graph "BOOT-INPUTS-SOURCE-GRAPH-MISMATCH"
   :incompatible "BOOT-INPUTS-INCOMPATIBLE"})

(defn- fail!
  [kind message data]
  (throw (ex-info message
                  (merge {:diagnostic (get diagnostic-codes kind)
                          :input-schema schema}
                         data))))

(defn- nonempty-string?
  [value]
  (and (string? value) (not (str/blank? value))))

(defn- sha256-hex
  [bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (format "%064x"
            (BigInteger. 1 (.digest digest bytes)))))

(defn- sha256-id
  [bytes]
  (str "sha256:" (sha256-hex bytes)))

(defn- canonicalize
  "Make the EDN used for graph identity deterministic across map insertion order." 
  [value]
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[key nested]] [key (canonicalize nested)]) value))

    (vector? value) (mapv canonicalize value)
    (set? value) (->> value (map canonicalize) (sort-by pr-str) vec)
    (seq? value) (mapv canonicalize value)
    :else value))

(defn- canonical-bytes
  [value]
  (.getBytes (pr-str (canonicalize value)) "UTF-8"))

(defn- regular-file
  [root relative kind]
  (when-not (and (string? relative)
                 (not (str/blank? relative))
                 (not (.isAbsolute (io/file relative))))
    (fail! :path (str "bootstrap input path must be relative: " relative)
           {:path relative :kind kind}))
  (let [root-file (.getCanonicalFile (io/file root))
        file (.getCanonicalFile (io/file root-file relative))
        root-path (.getPath root-file)
        file-path (.getPath file)
        prefix (str root-path File/separator)]
    (when-not (or (= root-path file-path)
                  (str/starts-with? file-path prefix))
      (fail! :path (str "bootstrap input escapes project root: " relative)
             {:path relative :kind kind}))
    file))

(defn- extension
  [^File file]
  (let [name (.getName file)
        index (.lastIndexOf name ".")]
    (if (neg? index) "" (subs name index))))

(defn- sha256-file
  [^File file]
  (sha256-id (Files/readAllBytes (.toPath file))))

(defn- validate-id!
  [value field]
  (when-not (and (nonempty-string? value)
                 (re-matches #"[A-Za-z0-9._:/-]+" value))
    (fail! :malformed (str "invalid bootstrap input " (name field))
           {:field field :value value})))

(defn- validate-source-root!
  [root]
  (when-not (and (map? root)
                 (= allowed-root-keys (set (keys root)))
                 (nonempty-string? (:path root))
                 (keyword? (:role root))
                 (vector? (:extensions root))
                 (seq (:extensions root))
                 (every? #(and (string? %) (str/starts-with? % "."))
                         (:extensions root)))
    (fail! :malformed "invalid bootstrap source root declaration"
           {:root root}))
  root)

(defn- validate-direct-input!
  [input]
  (when-not (and (map? input)
                 (= allowed-direct-input-keys (set (keys input)))
                 (nonempty-string? (:path input))
                 (keyword? (:role input))
                 (and (string? (:sha256 input))
                      (re-matches #"sha256:[0-9a-f]{64}" (:sha256 input))))
    (fail! :malformed "invalid direct bootstrap input declaration"
           {:input input}))
  input)

(defn validate-manifest!
  "Validate the declaration without reading its source paths." 
  [manifest]
  (when-not (and (map? manifest)
                 (= required-keys (set (keys manifest)))
                 (= schema (:schema manifest))
                 (nonempty-string? (:input-id manifest))
                 (keyword? (:stage manifest))
                 (map? (:compiler manifest))
                 (= allowed-compiler-keys (set (keys (:compiler manifest))))
                 (= :gravity (get-in manifest [:compiler :source-language]))
                 (= :clojure (get-in manifest [:compiler :seed-language]))
                 (nonempty-string? (get-in manifest [:compiler :seed-artifact-id]))
                 (vector? (:source-roots manifest))
                 (seq (:source-roots manifest))
                 (every? map? (:source-roots manifest))
                 (vector? (:direct-inputs manifest))
                 (seq (:direct-inputs manifest))
                 (every? map? (:direct-inputs manifest))
                 (map? (:source-graph manifest))
                 (= #{:sha256} (set (keys (:source-graph manifest))))
                 (and (string? (get-in manifest [:source-graph :sha256]))
                      (re-matches #"sha256:[0-9a-f]{64}"
                                  (get-in manifest [:source-graph :sha256])))
                 (map? (:scope manifest))
                 (map? (:execution manifest))
                 (map? (:comparison manifest)))
    (fail! :malformed "invalid bootstrap input manifest shape"
           {:manifest manifest}))
  (doseq [root (:source-roots manifest)]
    (validate-source-root! root))
  (doseq [input (:direct-inputs manifest)]
    (validate-direct-input! input))
  (when-not (and (vector? (get-in manifest [:scope :profiles]))
                 (seq (get-in manifest [:scope :profiles]))
                 (vector? (get-in manifest [:scope :targets]))
                 (seq (get-in manifest [:scope :targets]))
                 (vector? (get-in manifest [:scope :supported-modules]))
                 (vector? (get-in manifest [:scope :accepted-fixtures]))
                 (vector? (get-in manifest [:scope :rejected-fixtures])))
    (fail! :malformed "invalid bootstrap support scope"
           {:scope (:scope manifest)}))
  (when-not (and (= :jvm (get-in manifest [:execution :target]))
                 (= :jvm (get-in manifest [:execution :backend]))
                 (= :clojure-stage0 (get-in manifest [:execution :runtime]))
                 (vector? (get-in manifest [:execution :host-boundaries]))
                 (seq (get-in manifest [:execution :host-boundaries])))
    (fail! :incompatible "bootstrap input execution target is unsupported"
           {:execution (:execution manifest)}))
  (when-not (and (vector? (get-in manifest [:comparison :modes]))
                 (seq (get-in manifest [:comparison :modes]))
                 (vector? (get-in manifest [:comparison :artifact-kinds]))
                 (seq (get-in manifest [:comparison :artifact-kinds])))
    (fail! :malformed "invalid bootstrap comparison declaration"
           {:comparison (:comparison manifest)}))
  manifest)

(defn read-manifest
  "Read and structurally validate an EDN bootstrap input declaration." 
  [manifest-path]
  (let [file (.getCanonicalFile (io/file manifest-path))]
    (when-not (.isFile file)
      (fail! :missing-input (str "bootstrap input manifest is missing: " manifest-path)
             {:path manifest-path}))
    (try
      (validate-manifest! (edn/read-string (slurp file)))
      (catch clojure.lang.ExceptionInfo error
        (throw error))
      (catch Exception error
        (fail! :malformed "bootstrap input manifest is not valid EDN"
               {:path manifest-path :cause (.getMessage error)})))))

(defn- root-files
  [project-root {:keys [path role extensions]}]
  (let [root (regular-file project-root path :source-root)]
    (when-not (.isDirectory root)
      (fail! :missing-root (str "bootstrap source root is missing: " path)
             {:path path :role role}))
    (let [root-path (.toPath root)]
      (with-open [stream (Files/walk root-path
                                  (make-array java.nio.file.FileVisitOption 0))]
        (->> (.iterator stream)
             iterator-seq
             (filter #(.isFile (.toFile ^java.nio.file.Path %)))
             (map #(.toFile ^java.nio.file.Path %))
             (filter #(some #{(extension %)} extensions))
             (map (fn [^File file]
                    (let [relative (.relativize (.toPath (.getCanonicalFile (io/file project-root)))
                                                (.toPath (.getCanonicalFile file)))]
                      {:path (str/replace (.toString relative) File/separator "/")
                       :role role
                       :bytes (.length file)
                       :sha256 (sha256-file file)})))
             vec)))))

(defn- direct-files
  [project-root direct-inputs]
  (mapv (fn [{:keys [path role sha256]}]
          (let [file (regular-file project-root path :direct-input)]
            (when-not (.isFile file)
              (fail! :missing-input (str "bootstrap direct input is missing: " path)
                     {:path path :role role}))
            (let [actual (sha256-file file)]
              (when-not (= sha256 actual)
                (fail! :digest (str "bootstrap direct input digest mismatch: " path)
                       {:path path :expected sha256 :actual actual :role role}))
              {:path path :role role :bytes (.length file) :sha256 actual})))
        direct-inputs))

(defn source-graph
  "Return the canonical, freshly observed source graph for a manifest." 
  [project-root manifest]
  (let [root-entries (mapcat #(root-files project-root %) (:source-roots manifest))
        direct-entries (direct-files project-root (:direct-inputs manifest))]
    (->> (concat root-entries direct-entries)
         (sort-by (juxt :path :role))
         vec)))

(defn source-graph-sha256
  "Hash a source graph after canonical ordering and serialization." 
  [entries]
  (sha256-id (canonical-bytes entries)))

(defn load-inputs!
  "Authenticate the manifest and all declared files before a rebuild." 
  [project-root manifest-path]
  (let [manifest (read-manifest manifest-path)
        entries (source-graph project-root manifest)
        actual (source-graph-sha256 entries)
        expected (get-in manifest [:source-graph :sha256])]
    (when-not (and (string? expected)
                   (re-matches #"sha256:[0-9a-f]{64}" expected))
      (fail! :malformed "bootstrap manifest is missing source-graph identity"
             {:field :source-graph-sha256 :value expected}))
    (when-not (= expected actual)
      (fail! :graph "bootstrap source graph identity mismatch"
             {:expected expected :actual actual
              :input-id (:input-id manifest)
              :entry-count (count entries)}))
    {:manifest manifest
     :project-root (.getCanonicalPath (io/file project-root))
     :source-graph {:sha256 actual :entries entries}
     :compiler (:compiler manifest)
     :scope (:scope manifest)
     :execution (:execution manifest)
     :comparison (:comparison manifest)}))

(defn run-rebuild!
  "Validate inputs, then invoke an explicit rebuild executor.

  The executor is deliberately supplied by the caller.  This keeps host
  process policy outside this pure input boundary while ensuring it cannot run
  before the declared source closure and seed are authenticated." 
  [project-root manifest-path executor]
  (when-not (ifn? executor)
    (fail! :malformed "bootstrap rebuild executor must be callable"
           {:executor executor}))
  (let [inputs (load-inputs! project-root manifest-path)
        result (executor inputs)]
    (when-not (and (map? result)
                   (nonempty-string? (:artifact-id result))
                   (= (get-in inputs [:source-graph :sha256])
                      (:source-graph-sha256 result)))
      (fail! :malformed "bootstrap rebuild result is not bound to verified inputs"
             {:result result
              :source-graph-sha256 (get-in inputs [:source-graph :sha256])}))
    {:inputs inputs :result result}))
