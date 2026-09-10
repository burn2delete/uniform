(ns gravity.tools.sh07-b51-attempt32-checker-child
  "Fresh-process entrypoint for one V9 SH-07 B5.1 census checker.

  Keep this namespace project-free.  The entry preflight runs before the
  shared codec, bootstrap constructor, checker-identity helper, or selected
  checker is loaded from the C-derived classpath."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream ByteArrayOutputStream Closeable File
            InputStream InputStreamReader OutputStream]
           [java.lang ProcessHandle]
           [java.lang.management ManagementFactory]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.nio.file.attribute BasicFileAttributes]
           [java.security MessageDigest]
           [java.time Instant]
           [java.util Arrays Optional]
           [java.util.concurrent Callable ExecutionException
            ExecutorCompletionService Executors Future TimeUnit
            TimeoutException]
           [java.util.jar JarFile]))

(def transport-limits
  {:request-max-bytes 4194304
   :response-max-bytes 1073741824
   :stderr-max-bytes 1048576
   :deadline-ms 3600000})

(def closed-environment
  {"LC_ALL" "C"
   "LANG" "C"
   "TZ" "UTC"
   "GIT_CONFIG_NOSYSTEM" "1"
   "GIT_CONFIG_GLOBAL" "/dev/null"
   "GIT_OPTIONAL_LOCKS" "0"
   "GIT_NO_REPLACE_OBJECTS" "1"})

(def profile-bounds
  {:certificate-maximum-nodes 2048
   :certificate-maximum-depth 12
   :certificate-maximum-width 35
   :certificate-maximum-collection-count 64
   :certificate-maximum-scalar-utf8 4096
   :certificate-maximum-canonical-reader-bytes 262144
   :outer-request-maximum-collection-count 65536
   :carrier-maximum-nodes 8388608
   :carrier-maximum-depth 256
   :carrier-maximum-width 65536
   :carrier-maximum-collection-count 524288
   :carrier-partition-maximum-collection-count 65536
   :carrier-maximum-scalar-utf8 65536
   :offline-transcript-maximum-nodes 262144
   :offline-transcript-maximum-records 262144
   :offline-transcript-maximum-call-rows 65536
   :offline-transcript-maximum-bytes 268435456})

(def role-specs
  {:producer-census
   {:cli "producer-census"
    :sequence :a
    :checker-namespace
    "gravity.tools.sh07-b51-attempt30-producer-census"
    :checker-entrypoint "produce-certificate"
    :forbidden-namespace
    "gravity.tools.sh07-b51-attempt30-independent-census"
    :output-name "checker-a-output.edn"
    :algorithm-role :source-first-exhaustive}
   :independent-verifier-census
   {:cli "independent-verifier-census"
    :sequence :b
    :checker-namespace
    "gravity.tools.sh07-b51-attempt30-independent-census"
    :checker-entrypoint "produce-certificate"
    :forbidden-namespace
    "gravity.tools.sh07-b51-attempt30-producer-census"
    :output-name "checker-b-output.edn"
    :algorithm-role :m2-first-independent-exhaustive}})

(def ^:private request-keys
  #{:schema :role :content :eligibility :evidence :limits})
(def ^:private content-keys #{:commit :tree})
(def ^:private evidence-keys #{:architecture-report-sha256 :profile-bounds})
(def ^:private eligibility-keys
  #{:schema :commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256 :receipt-id})
(def ^:private product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :sh06-resolution-plan-authority
    :source-read-receipt})
(def ^:private source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})
(def ^:private source-counter-keys
  #{:acquire-count :decode-count :event-count :finalize-count})
(def ^:private source-descriptor-keys
  #{:ordinal :kind :module-ordinal :path :mode :blob-oid :byte-count
    :raw-sha256})
(def ^:private source-event-keys
  #{:ordinal :domain :source-ordinal :module-ordinal :consumer
    :source-raw-sha256 :product-id})
(def ^:private checker-response-keys
  #{:schema :role :transport :result :counters :tcb :diagnostics})

(def ^:private eligibility-path
  "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn")
(def ^:private evidence-relative-root
  "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9")
(def ^:private completed-at "1970-01-01T00:00:00Z")
(def ^:private predecessor-executable-descriptor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")
(def ^:private codec-namespace "gravity.tools.sh07-b51-v9-codec")
(def ^:private bootstrap-namespace "gravity.bootstrap")
(def ^:private git-evidence-namespace
  "gravity.tools.sh07-b51-git-evidence")
(def ^:private identity-namespace
  "gravity.tools.sh07-b51-attempt32-checker-identity")
(def ^:private child-main-token
  "gravity.tools.sh07-b51-attempt32-checker-child")
(def ^:private classpath-source-prefix "bootstrap/clojure/src/")
(def ^:private empty-raw-sha256
  "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
(def ^:private pre-manifest-row-keys
  #{:ordinal :path :namespace :mode :blob-oid :byte-count :raw-sha256
    :materialized-raw-sha256 :requires-project-namespaces})
(def ^:private codec-tcb-load-order
  '[gravity.c2-artifact-identity.canonical
    gravity.digest
    gravity.tools.sh07-b51-v9-codec])
(def ^:private codec-tcb-specs
  [{:ordinal 0
    :path "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
    :namespace 'gravity.c2-artifact-identity.canonical
    :requires-project-namespaces []}
   {:ordinal 1
    :path "bootstrap/clojure/src/gravity/digest.clj"
    :namespace 'gravity.digest
    :requires-project-namespaces []}
   {:ordinal 2
    :path "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
    :namespace 'gravity.tools.sh07-b51-v9-codec
    :requires-project-namespaces
    '[gravity.c2-artifact-identity.canonical gravity.digest]}])
(def ^:private external-classpath-specs
  [{:ordinal 1 :logical-coordinate 'org.clojure/clojure
    :version "1.12.5" :required-namespace 'clojure.core}
   {:ordinal 2 :logical-coordinate 'org.clojure/core.specs.alpha
    :version "0.4.74" :required-namespace 'clojure.core.specs.alpha}
   {:ordinal 3 :logical-coordinate 'org.clojure/spec.alpha
   :version "0.5.238" :required-namespace 'clojure.spec.alpha}])
(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :pre-masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})
(def ^:private eligibility-result-keys
  #{:coordinate :receipt :observations :operation-count
    :pre-object-snapshot :pre-discharge-observations
    :pre-discharge-git-operation-count :scope :eligibility-check
    :eligibility-check-root :remaining-gaps :authority})
(def ^:private eligibility-check-keys
  #{:coordinate :observations :pre-discharge-observations :decode-count
    :receipt-id-check-count :pre-discharge-reconstruction-count
    :pre-discharge-git-operation-count
    :pre-discharge-document-descriptor-count
    :pre-discharge-source-descriptor-count
    :pre-discharge-pre-masked-file-count
    :pre-discharge-owned-c-artifact-count
    :pre-discharge-a9-artifact-count
    :pre-discharge-baseline-boot5-count
    :pre-discharge-static-decode-count :content-match
    :source-descriptors-match :source-descriptors-root-match
    :pre-masked-root-match :owned-c-root-match :pre-discharge-match})
(def ^:private eligibility-check-counts
  {:decode-count 1
   :receipt-id-check-count 1
   :pre-discharge-reconstruction-count 1
   :pre-discharge-git-operation-count 84
   :pre-discharge-document-descriptor-count 33
   :pre-discharge-source-descriptor-count 13
   :pre-discharge-pre-masked-file-count 2
   :pre-discharge-owned-c-artifact-count 33
   :pre-discharge-a9-artifact-count 3
   :pre-discharge-baseline-boot5-count 1
   :pre-discharge-static-decode-count 69})
(def ^:private eligibility-check-matches
  [:content-match :source-descriptors-match
   :source-descriptors-root-match :pre-masked-root-match
   :owned-c-root-match :pre-discharge-match])
(def ^:private nonclaims
  [:formal-proof :sh07-complete :self-hosting :release :seed-retirement
   :hostile-host])

(def ^:private source-specs
  [{:ordinal 0 :kind :l1-contract :module-ordinal nil
    :path "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"}
   {:ordinal 1 :kind :l2-contract :module-ordinal nil
    :path "docs/phase-01-core-language/012-l2-core-language-semantics.md"}
   {:ordinal 2 :kind :checked-core :module-ordinal nil
    :path "bootstrap/gravity/src/gravity/checked_core.gravity"}
   {:ordinal 3 :kind :module :module-ordinal 0
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"}
   {:ordinal 4 :kind :module :module-ordinal 1
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"}
   {:ordinal 5 :kind :module :module-ordinal 2
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"}
   {:ordinal 6 :kind :module :module-ordinal 3
    :path "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"}
   {:ordinal 7 :kind :module :module-ordinal 4
    :path "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"}
   {:ordinal 8 :kind :module :module-ordinal 5
    :path "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"}
   {:ordinal 9 :kind :module :module-ordinal 6
    :path "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"}
   {:ordinal 10 :kind :compiler :module-ordinal nil
    :path "bootstrap/gravity/p15_s23/compiler.gravity"}
   {:ordinal 11 :kind :emitter :module-ordinal nil
    :path "bootstrap/gravity/p15_s23/emitter.gravity"}
   {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
    :path "bootstrap/gravity/src/gravity/resolution.gravity"}])

(def ^:private source-event-plan
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal) :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal) :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal) :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal) :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal) :domain :compiler-artifact-binding
       :source-ordinal 11 :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding})
    [{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
      :module-ordinal nil
      :consumer
      'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}])))

(defn- reject!
  ([reason] (reject! reason {}))
  ([reason details]
   (throw (ex-info "SH-07 B51 checker child rejected"
                   {:id "SH07-B51-CHECKER-CHILD"
                    :reason reason
                    :details details}))))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn role-from-cli!
  "Apply the only admitted CLI-string/EDN-keyword role bijection."
  [role-name]
  (or (some (fn [[role spec]] (when (= role-name (:cli spec)) role))
            role-specs)
      (reject! :role {:actual role-name})))

(defn parse-role-arguments!
  "Accept exactly --role followed by one canonical role spelling."
  [arguments]
  (when-not (and (vector? arguments)
                 (= 2 (count arguments))
                 (= "--role" (first arguments)))
    (reject! :argv {:actual arguments}))
  (role-from-cli! (second arguments)))

(defn validate-closed-environment!
  "Reject inherited, injected, or omitted process environment entries."
  [environment]
  (when-not (= closed-environment environment)
    (reject! :environment
             {:expected-keys (set (keys closed-environment))
              :actual-keys (set (keys environment))}))
  environment)

(defn- classpath-entries [classpath]
  (if (string? classpath)
    (str/split classpath
               (re-pattern (java.util.regex.Pattern/quote File/pathSeparator))
               -1)
    []))

(defn validate-literal-classpath!
  "Perform the project-free literal four-entry classpath preflight."
  [classpath]
  (let [entries (classpath-entries classpath)]
    (when-not (and (= 4 (count entries))
                   (= 4 (count (distinct entries)))
                   (every? #(and (string? %) (not (str/blank? %))) entries)
                   (every? #(.isAbsolute
                             (Paths/get % (make-array String 0)))
                           entries))
      (reject! :classpath {:entry-count (count entries)}))
    entries))

(defn- canonical-java-path []
  (str (.normalize
        (.toAbsolutePath
         (Paths/get (System/getProperty "java.home")
                    (into-array String ["bin" "java"]))))))

(defn- expected-child-argv [classpath role]
  [(canonical-java-path) "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
   "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
   "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
   "-Duser.home=/nonexistent" "-cp" classpath "clojure.main" "-m"
   child-main-token "--role" (get-in role-specs [role :cli])])

(defn validate-entry-preflight!
  "Validate argv, the closed environment, working directory, and literal CP.

  This function intentionally uses no project namespace.  The full C/T
  manifest and codec TCB validation belongs to the launcher-provided preflight
  seam; no semantic source is decoded here."
  [{:keys [arguments environment working-directory classpath process-argv]}]
  (when-not (string? working-directory)
    (reject! :working-directory {:actual working-directory}))
  (let [role (parse-role-arguments! arguments)
        entries (validate-literal-classpath! classpath)
        cwd (Paths/get working-directory (make-array String 0))]
    (validate-closed-environment! environment)
    (when-not (and (string? working-directory)
                   (.isAbsolute cwd)
                   (Files/isDirectory cwd (make-array LinkOption 0)))
      (reject! :working-directory {:actual working-directory}))
    (let [expected (expected-child-argv classpath role)]
      (when-not (= 17 (count expected))
        (reject! :child-argv-count {:actual (count expected)}))
      (when (and process-argv (not= expected process-argv))
        (reject! :child-argv-self-report))
      {:role role
       :arguments arguments
       :environment {:working-directory working-directory
                     :variables environment}
       :classpath entries
       :classpath-string classpath
       :child-argv (or process-argv expected)})))

(defn- sha256-bytes-core [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") ^bytes bytes)
        out (StringBuilder. 71)]
    (.append out "sha256:")
    (doseq [value digest]
      (let [unsigned (bit-and 0xff value)]
        (.append out (Character/forDigit (bit-shift-right unsigned 4) 16))
        (.append out (Character/forDigit (bit-and unsigned 15) 16))))
    (str out)))

(defn- strict-utf8-core! [bytes reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _ (reject! reason)))))

(defn- read-bounded-core! [^InputStream input maximum reason]
  (let [out (ByteArrayOutputStream.) buffer (byte-array 8192)]
    (loop [total 0]
      (let [read (.read input buffer)]
        (if (neg? read)
          (.toByteArray out)
          (let [next-total (+ total read)]
            (when (> next-total maximum)
              (reject! reason {:maximum maximum :actual next-total}))
            (.write out buffer 0 read)
            (recur next-total)))))))

(defn- remaining-ms [deadline]
  (max 0 (long (Math/ceil
                (/ (double (- deadline (System/nanoTime))) 1000000.0)))))

(defn- close-quietly-core! [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- run-project-free-process!
  [working-directory argv stdout-maximum operation]
  (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get working-directory
                                          (make-array String 0)))))
        _ (doto (.environment builder)
            (.clear) (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process) stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        out-task (future (read-bounded-core! stdout stdout-maximum
                                             :git-stdout-overflow))
        err-task (future (read-bounded-core!
                          stderr (:stderr-max-bytes transport-limits)
                          :git-stderr-overflow))
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (.close ^OutputStream stdin)
      (when-not (.waitFor process (remaining-ms deadline)
                          TimeUnit/MILLISECONDS)
        (throw (TimeoutException. "project-free process")))
      (let [timeout (Object.)
            out (deref out-task (remaining-ms deadline) timeout)
            err (deref err-task (remaining-ms deadline) timeout)]
        (when (or (identical? timeout out) (identical? timeout err))
          (throw (TimeoutException. "project-free drains")))
        (when-not (and (zero? (.exitValue process))
                       (zero? (alength ^bytes err)))
          (reject! :git-object-operation
                   {:operation operation :exit-code (.exitValue process)
                    :stderr-byte-count (alength ^bytes err)}))
        {:operation operation :argv argv :stdin-byte-count 0
         :stdin-raw-sha256 empty-raw-sha256 :exit-code 0
         :stdout-bytes out :stdout-byte-count (alength ^bytes out)
         :stdout-raw-sha256 (sha256-bytes-core out)
         :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256})
      (catch Throwable error
        (future-cancel out-task) (future-cancel err-task)
        (when (.isAlive process)
          (.destroy process)
          (when-not (.waitFor process 250 TimeUnit/MILLISECONDS)
            (.destroyForcibly process)
            (.waitFor process 1000 TimeUnit/MILLISECONDS)))
        (if (= "SH07-B51-CHECKER-CHILD" (:id (ex-data error)))
          (throw error)
          (reject! :git-object-operation
                   {:operation operation
                    :cause (.getName (class error))})))
      (finally
        (close-quietly-core! stdin)
        (close-quietly-core! stdout)
        (close-quietly-core! stderr)))))

(defn- git-operation-core! [options operation arguments maximum]
  (run-project-free-process!
   (get-in options [:environment :working-directory])
   (into ["/usr/bin/git" "-C"
          (get-in options [:environment :working-directory])]
         arguments)
   maximum operation))

(defn- nul-records-core! [bytes reason]
  (let [text (strict-utf8-core! bytes reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000")) (reject! reason)
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-show-core! [bytes]
  (let [match
        (re-matches #"([0-9a-f]{40})\n([0-9a-f]{40})\n([0-9a-f ]*)\n"
                    (strict-utf8-core! bytes :git-show-grammar))]
    (when-not match (reject! :git-show-grammar))
    {:commit (nth match 1) :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- parse-tree-rows-core! [bytes]
  (mapv
   (fn [record]
     (let [match
           (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                       record)]
       (when-not match (reject! :git-ls-tree-grammar))
       {:mode (nth match 1) :type (nth match 2)
        :oid (nth match 3) :path (nth match 4)}))
   (nul-records-core! bytes :git-ls-tree-grammar)))

(defn- git-path-object-core! [options path]
  (let [commit (get-in options [:content :commit])
        tree (git-operation-core! options :ls-tree-z-requested
                                  ["ls-tree" "-z" commit "--" path]
                                  1048576)
        rows (parse-tree-rows-core! (:stdout-bytes tree))
        row (first rows)]
    (when-not (and (= 1 (count rows)) (= path (:path row))
                   (= "100644" (:mode row)) (= "blob" (:type row)))
      (reject! :c-source-object {:path path}))
    (let [blob (git-operation-core! options :cat-file-blob
                                    ["cat-file" "blob" (:oid row)]
                                    (:response-max-bytes transport-limits))
          bytes (:stdout-bytes blob)]
      {:path path :mode "100644" :blob-oid (:oid row)
       :byte-count (alength ^bytes bytes)
       :raw-sha256 (sha256-bytes-core bytes) :bytes bytes})))

(defn- source-forms-core! [bytes]
  (strict-utf8-core! bytes :trusted-tcb-utf8)
  (with-open [reader
              (LineNumberingPushbackReader.
               (InputStreamReader. (ByteArrayInputStream. bytes)
                                   StandardCharsets/UTF_8))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof :read-cond :allow :features #{:clj}}
                           reader)]
            (if (identical? eof form) forms
                (recur (conj forms form)))))))))

(defn- require-spec-namespace-core [spec]
  (cond (symbol? spec) spec
        (and (vector? spec) (symbol? (first spec))) (first spec)
        :else nil))

(defn- source-namespace-core! [bytes path]
  (let [forms (source-forms-core! bytes)
        ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
    (when (and (seq ns-forms)
               (or (not= 1 (count ns-forms))
                   (not= (first forms) (first ns-forms))))
      (reject! :trusted-tcb-namespace {:path path}))
    (when-let [ns-form (first ns-forms)]
      {:namespace (second ns-form)
       :requires-project-namespaces
       (->> (drop 2 ns-form)
            (filter #(and (seq? %) (= :require (first %))))
            (mapcat rest) (keep require-spec-namespace-core)
            (filter #(str/starts-with? (str %) "gravity.")) vec)})))

(defn- symlink-free-core? [^Path path]
  (loop [current (.normalize (.toAbsolutePath path))]
    (cond (nil? current) true
          (Files/isSymbolicLink current) false
          :else (recur (.getParent current)))))

(defn- materialized-source-core! [options path]
  (when-not (str/starts-with? path classpath-source-prefix)
    (reject! :materialized-source-path {:path path}))
  (let [root (Paths/get (first (:classpath options)) (make-array String 0))
        file (.normalize (.resolve root
                                   (subs path (count classpath-source-prefix))))]
    (when-not (and (.startsWith file root)
                   (Files/isRegularFile
                    file (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? file))
      (reject! :materialized-source-path {:path path}))
    (with-open [input (Files/newInputStream
                       file (make-array java.nio.file.OpenOption 0))]
      (read-bounded-core! input (:response-max-bytes transport-limits)
                          :materialized-source-overflow))))

(defn pre-manifest-tcb!
  "Authenticate the child entrypoint and codec leaves from C before codec load."
  [options]
  (let [show (git-operation-core!
              options :show-tree
              ["show" "-s" "--format=%H%n%T%n%P"
               (get-in options [:content :commit])] 4096)
        shown (parse-show-core! (:stdout-bytes show))
        _ (when-not (= (:content options) (select-keys shown [:commit :tree]))
            (reject! :content-coordinate))
        entry-spec
        {:ordinal 0
         :path "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
         :namespace 'gravity.tools.sh07-b51-attempt32-checker-child
         :requires-project-namespaces []}
        row-for
        (fn [spec]
          (let [object (git-path-object-core! options (:path spec))
                materialized (materialized-source-core! options (:path spec))
                parsed (source-namespace-core! (:bytes object) (:path spec))]
            (when-not (and (Arrays/equals ^bytes (:bytes object)
                                          ^bytes materialized)
                           (= (:namespace spec) (:namespace parsed))
                           (= (:requires-project-namespaces spec)
                              (:requires-project-namespaces parsed)))
              (reject! :trusted-tcb-source {:path (:path spec)}))
            {:ordinal (:ordinal spec) :path (:path spec)
             :namespace (:namespace spec) :mode "100644"
             :blob-oid (:blob-oid object) :byte-count (:byte-count object)
             :raw-sha256 (:raw-sha256 object)
             :materialized-raw-sha256 (sha256-bytes-core materialized)
             :requires-project-namespaces
             (:requires-project-namespaces spec)}))
        entrypoint (row-for entry-spec)
        members (mapv row-for codec-tcb-specs)
        result
        {:entrypoint entrypoint
         :codec-tcb {:schema :gravity/sh07-b51-codec-tcb-v1
                     :load-order codec-tcb-load-order :members members}}]
    (when-not (and (= pre-manifest-row-keys (set (keys entrypoint)))
                   (every? #(= pre-manifest-row-keys (set (keys %))) members)
                   (= codec-tcb-load-order (mapv :namespace members))
                   (every? #(= (:raw-sha256 %)
                               (:materialized-raw-sha256 %))
                           (into [entrypoint] members)))
      (reject! :trusted-tcb))
    result))

(defn- resolve-public! [namespace-name var-name]
  (or (ns-resolve (symbol namespace-name) (symbol var-name))
      (reject! :missing-public-var
               {:namespace namespace-name :var var-name})))

(defn load-codec!
  "Load the one shared V9 codec and its immutable transport API value."
  []
  (require (symbol codec-namespace))
  (let [transport @(resolve-public! codec-namespace "transport-api")
        api (merge {:H (resolve-public! codec-namespace "H")
                    :W (resolve-public! codec-namespace "W")
                    :R (resolve-public! codec-namespace "R")
                    :limits @(resolve-public! codec-namespace
                                              "transport-limits")}
                   transport)]
    (when-not (= (dissoc transport-limits :deadline-ms) (:limits api))
      (reject! :codec-limits {:actual (:limits api)}))
    api))

(defn- file-key-core! [^Path path]
  (let [attributes
        (Files/readAttributes
         path BasicFileAttributes
         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        key (.fileKey ^BasicFileAttributes attributes)]
    (when-not key (reject! :classpath-file-key {:path (str path)}))
    (str key)))

(defn- canonical-file-core! [path]
  (let [value (.normalize (.toAbsolutePath
                           (Paths/get path (make-array String 0))))]
    (when-not (and (Files/isRegularFile
                    value (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? value))
      (reject! :classpath-file {:path path}))
    (.toRealPath value (make-array LinkOption 0))))

(defn- jar-source-records-core! [^Path path codec]
  (with-open [jar (JarFile. (.toFile path))]
    (let [records
          (->> (enumeration-seq (.entries jar))
               (remove #(.isDirectory %))
               (filter #(or (str/ends-with? (.getName %) ".clj")
                            (str/ends-with? (.getName %) ".cljc")))
               (keep
                (fn [entry]
                  (with-open [input (.getInputStream jar entry)]
                    (let [bytes (read-bounded-core!
                                 input 67108864 :classpath-jar-entry-overflow)
                          parsed (source-namespace-core! bytes (.getName entry))]
                      (when parsed
                        [(:namespace parsed)
                         {:namespace (:namespace parsed)
                          :resource (.getName entry)
                          :byte-count (alength ^bytes bytes)
                          :source-sha256 ((:R codec) bytes)}])))))
               vec)
          result (into {} records)]
      (when-not (= (count records) (count result))
        (reject! :classpath-jar-namespace-duplicate {:path (str path)}))
      result)))

(defn- jar-classpath-entry-core! [path spec codec]
  (let [canonical (canonical-file-core! path)
        filename (str (.getFileName canonical))]
    (when-not (str/ends-with? filename (str (:version spec) ".jar"))
      (reject! :classpath-coordinate
               {:ordinal (:ordinal spec) :file filename}))
    (let [bytes (with-open [input (Files/newInputStream
                                   canonical
                                   (make-array java.nio.file.OpenOption 0))]
                  (read-bounded-core! input 1073741824
                                      :classpath-jar-overflow))
          records (jar-source-records-core! canonical codec)
          namespaces (->> (keys records) (sort-by str) vec)]
      (when-not (some #{(:required-namespace spec)} namespaces)
        (reject! :classpath-coordinate
                 {:ordinal (:ordinal spec)
                  :required-namespace (:required-namespace spec)}))
      {:entry
       {:ordinal (:ordinal spec) :kind :jar
        :logical-coordinate
        {:artifact (:logical-coordinate spec) :version (:version spec)}
        :canonical-path (str canonical) :file-key (file-key-core! canonical)
        :byte-count (alength ^bytes bytes) :sha256 ((:R codec) bytes)
        :provided-namespaces namespaces}
       :external-source-records records})))

(defn validate-complete-classpath!
  "Validate all C source objects/materialized bytes and the three exact JARs."
  [options codec]
  (let [tree
        (git-operation-core!
         options :ls-tree-z-recursive
         ["ls-tree" "-r" "-z" (get-in options [:content :commit]) "--"
          "bootstrap/clojure/src"]
         (:response-max-bytes transport-limits))
        tree-rows (parse-tree-rows-core! (:stdout-bytes tree))
        _ (when-not
           (and (seq tree-rows)
                (= (count tree-rows) (count (set (map :path tree-rows))))
                (every? #(and (= "100644" (:mode %))
                              (= "blob" (:type %))
                              (str/starts-with? (:path %)
                                                classpath-source-prefix))
                        tree-rows))
            (reject! :classpath-tree))
        authenticated
        (mapv
         (fn [{:keys [path oid mode]}]
           (let [blob (git-operation-core!
                       options :cat-file-blob ["cat-file" "blob" oid]
                       (:response-max-bytes transport-limits))
                 object-bytes (:stdout-bytes blob)
                 materialized (materialized-source-core! options path)]
             (when-not (Arrays/equals ^bytes object-bytes ^bytes materialized)
               (reject! :classpath-materialized-byte-drift {:path path}))
             {:path path :mode mode :blob-oid oid
              :byte-count (alength ^bytes object-bytes)
              :raw-sha256 ((:R codec) object-bytes) :bytes object-bytes}))
         tree-rows)
        manifest-rows
        (mapv (fn [{:keys [path blob-oid byte-count raw-sha256]}]
                [(subs path (count classpath-source-prefix))
                 blob-oid byte-count raw-sha256])
              authenticated)
        manifest-root
        ((:H codec)
         [:gravity/sh07-b51-classpath-manifest-root-v1 manifest-rows])
        namespaces
        (->> authenticated
             (filter #(or (str/ends-with? (:path %) ".clj")
                          (str/ends-with? (:path %) ".cljc")))
             (keep #(some-> (source-namespace-core! (:bytes %) (:path %))
                            :namespace)) vec)
        _ (when-not (= (count namespaces) (count (distinct namespaces)))
            (reject! :classpath-project-namespace-duplicate))
        root (Paths/get (first (:classpath options)) (make-array String 0))
        project-entry
        {:ordinal 0 :kind :directory
         :logical-coordinate
         {:artifact 'project/bootstrap-clojure-src
          :commit (get-in options [:content :commit])
          :tree (get-in options [:content :tree])}
         :canonical-path (str (.toRealPath root (make-array LinkOption 0)))
         :file-key (file-key-core! root)
         :byte-count (reduce + 0 (map :byte-count authenticated))
         :sha256 manifest-root :provided-namespaces (vec (sort-by str namespaces))}
        external
        (mapv (fn [path spec]
                (jar-classpath-entry-core! path spec codec))
              (subvec (vec (:classpath options)) 1) external-classpath-specs)
        classpath (into [project-entry] (map :entry external))
        all-namespaces (vec (mapcat :provided-namespaces classpath))
        external-records
        (reduce
         (fn [result item]
           (reduce-kv
            (fn [result namespace record]
              (when (contains? result namespace)
                (reject! :classpath-namespace-shadowing
                         {:namespace namespace}))
              (assoc result namespace record))
            result (:external-source-records item)))
         {} external)]
    (when-not (and (= 4 (count classpath))
                   (= (vec (range 4)) (mapv :ordinal classpath))
                   (= (count all-namespaces) (count (distinct all-namespaces))))
      (reject! :classpath-namespace-shadowing))
    {:classpath-receipt classpath
     :classpath-manifest-rows manifest-rows
     :classpath-manifest-root manifest-root
     :source-authority
     {:content (:content options)
      :source-objects (into {} (map (juxt :path identity) authenticated))
      :external-source-records external-records}}))

(defn validate-checker-request!
  "Validate the closed V9 request and require its role to equal the CLI role."
  [cli-role request]
  (let [content (:content request)
        evidence (:evidence request)
        eligibility (:eligibility request)]
    (when-not
     (and (exact-keys? request request-keys)
          (= :gravity/sh07-b51-checker-child-request-v4 (:schema request))
          (= cli-role (:role request))
          (exact-keys? content content-keys)
          (git-id? (:commit content))
          (git-id? (:tree content))
          (exact-keys? evidence evidence-keys)
          (sha256-id? (:architecture-report-sha256 evidence))
          (= profile-bounds (:profile-bounds evidence))
          (= transport-limits (:limits request))
          (exact-keys? eligibility eligibility-keys)
          (= :gravity/sh07-b51-external-review-object-v1
             (:schema eligibility))
          (git-id? (:commit eligibility))
          (git-id? (:tree eligibility))
          (= [(:commit content)] (:parents eligibility))
          (= eligibility-path (:path eligibility))
          (= "100644" (:mode eligibility))
          (= "blob" (:type eligibility))
          (git-id? (:blob-oid eligibility))
          (and (integer? (:byte-count eligibility))
               (<= 0 (:byte-count eligibility)))
          (sha256-id? (:raw-sha256 eligibility))
          (sha256-id? (:receipt-id eligibility)))
      (reject! :request {:role cli-role}))
    request))

(defn- load-git-evidence! []
  (require (symbol git-evidence-namespace))
  {:validate-eligibility-object!
   (resolve-public! git-evidence-namespace "validate-eligibility-object!")})

(defn- dense-observations? [expected-count observations]
  (and (vector? observations)
       (= expected-count (count observations))
       (= (vec (range expected-count)) (mapv :ordinal observations))))

(defn validate-eligibility-five-operations!
  "Validate Q with five operations and independently reconstruct all 84 PRE
  operations through the authenticated shared Git-evidence implementation.
  The shared Q sequence retains its show, diff-tree, ls-tree, blob, ls-tree
  topology; this entrypoint does not duplicate those Git operations."
  [preflight codec request]
  (let [coordinate (:eligibility request)
        validate! (:validate-eligibility-object! (load-git-evidence!))
        result (validate! {:repository-root (:working-directory preflight)
                           :content (:content request)
                           :eligibility-coordinate coordinate})
        receipt (:receipt result)
        check (:eligibility-check result)
        observations (:observations result)
        pre-observations (:pre-discharge-observations result)
        transition (get-in receipt [:freeze :active-record-ref :transition])]
    (when (seq (:remaining-gaps result))
      (reject! :pre-discharge-reconstruction-unavailable))
    (when-not
     (and (exact-keys? result eligibility-result-keys)
          (exact-keys? receipt eligibility-receipt-keys)
          (not (contains? receipt :masked-root))
          (exact-keys? check eligibility-check-keys)
          (= coordinate (:coordinate result) (:coordinate check))
          (= 5 (:operation-count result))
          (dense-observations? 5 observations)
          (= observations (:observations check))
          (= 84 (:pre-discharge-git-operation-count result))
          (dense-observations? 84 pre-observations)
          (= pre-observations (:pre-discharge-observations check))
          (= eligibility-check-counts
             (select-keys check (keys eligibility-check-counts)))
          (every? true? (map check eligibility-check-matches))
          (= :immutable-q-coordinate-and-pre-discharge (:scope result))
          (= [] (:remaining-gaps result))
          (= :none (:authority result))
          (= (:eligibility-check-root result)
             ((:H codec)
              [:gravity/sh07-b51-eligibility-check-root-v1 check]))
          (sha256-id? (:architecture-report-sha256 transition))
          (= (get-in request [:evidence :architecture-report-sha256])
             (:architecture-report-sha256 transition)))
      (reject! :eligibility-object-invalid))
    result))

(defn- read-request-before-codec! [^InputStream input]
  (let [bytes (read-bounded-core!
               input (:request-max-bytes transport-limits)
               :request-overflow)
        text (strict-utf8-core! bytes :request-utf8)]
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
      (let [eof (Object.)
            value (edn/read {:eof eof :readers {} :default nil} reader)
            trailing (edn/read {:eof eof :readers {} :default nil} reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (reject! :request-frame))
        {:bytes bytes :value value}))))

(defn- recomputable-event-product
  [event]
  (when (<= 3 (:ordinal event) 23) true))

(defn validate-receipted-products!
  "Reject any nonliteral descriptor/event schedule or broken raw/product/root
  join.  Products for events 0, 1, 2, and 24-39 remain opaque in this returned
  product shape; their IDs must still be typed and are bound by the exact event
  root."
  ([role request products]
   (validate-receipted-products! (:H (load-codec!)) role request products))
  ([H role request products]
   (let [receipt (:source-read-receipt products)
         counters (:counters receipt)
         content (:content request)
         descriptors (:source-descriptors receipt)
         events (:events receipt)
         descriptor-shas (mapv :raw-sha256 descriptors)]
    (when-not
     (and (exact-keys? products product-keys)
          (vector? (:m0s products)) (= 7 (count (:m0s products)))
          (map? (:m1 products))
          (vector? (:m2s products)) (= 7 (count (:m2s products)))
          (map? (:bundle-core products))
          (sha256-id? (:execution-bundle-core-id products))
          (vector? (:source-forms products))
          (= 7 (count (:source-forms products)))
          (vector? (:module-transactions products))
          (= 7 (count (:module-transactions products)))
          (exact-keys? receipt source-receipt-keys)
          (= :gravity/sh07-b51-source-read-receipt-v3 (:schema receipt))
          (= role (:role receipt))
          (= (:commit content) (:commit receipt))
          (= (:tree content) (:tree receipt))
          (vector? descriptors)
          (= 13 (count descriptors))
          (every? true?
                  (map (fn [spec descriptor]
                         (and (exact-keys? descriptor source-descriptor-keys)
                              (= spec (select-keys descriptor
                                                   [:ordinal :kind
                                                    :module-ordinal :path]))
                              (= "100644" (:mode descriptor))
                              (git-id? (:blob-oid descriptor))
                              (integer? (:byte-count descriptor))
                              (not (neg? (:byte-count descriptor)))
                              (sha256-id? (:raw-sha256 descriptor))))
                       source-specs descriptors))
          (= (H [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
             (:source-descriptors-root receipt))
          (vector? events)
          (= 40 (count events))
          (every? true?
                  (map (fn [plan event]
                         (let [source-ordinal (:source-ordinal event)
                               product (recomputable-event-product event)
                               product-id
                               (when (some? product)
                                 (H [:gravity/sh07-b51-source-event-product-id-v1
                                     {:ordinal (:ordinal event)
                                      :domain (:domain event)
                                      :consumer (:consumer event)
                                      :source-raw-sha256
                                      (:source-raw-sha256 event)
                                      :product product}]))]
                           (and (exact-keys? event source-event-keys)
                                (= plan (select-keys event
                                                     [:ordinal :domain
                                                      :source-ordinal
                                                      :module-ordinal
                                                      :consumer]))
                                (= (nth descriptor-shas source-ordinal nil)
                                   (:source-raw-sha256 event))
                                (sha256-id? (:product-id event))
                                (or (nil? product-id)
                                    (= product-id (:product-id event))))))
                       source-event-plan events))
          (= (H [:gravity/sh07-b51-source-event-root-v1 events])
             (:event-root receipt))
          (exact-keys? counters source-counter-keys)
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             counters)
          (true? (:finalized receipt)))
      (reject! :source-session {:role role}))
    products)))

(defn- source-session-id [H receipt]
  (H [:gravity/sh07-b51-governed-source-session-id-v1
      {:role (:role receipt)
       :commit (:commit receipt)
       :tree (:tree receipt)
       :source-descriptors-root (:source-descriptors-root receipt)
       :event-root (:event-root receipt)
       :counters (:counters receipt)}]))

(defn- checker-provenance
  [H role request checker-manifest]
  (let [spec (get role-specs role)
        snapshot (:content request)
        checker (:checker checker-manifest)
        dependency-ids
        {:local-source-record-ids
         (mapv #(H [:gravity/sh07-b51-materializer-local-source-v1
                    {:schema-version 1 :record %}])
               (get-in checker-manifest
                       [:dependencies :local-source-records]))
         :external-runtime-record-ids
         (mapv #(H [:gravity/sh07-b51-materializer-external-runtime-v1
                    {:schema-version 1 :record %}])
               (get-in checker-manifest
                       [:dependencies :external-runtime-records]))}
        tool-domain
        (case role
          :producer-census
          :gravity/sh07-b51-producer-census-materializer-tool-closure-id-v2
          :independent-verifier-census
          :gravity/sh07-b51-independent-census-materializer-tool-closure-id-v2)
        role-name (case role
                    :producer-census :producer
                    :independent-verifier-census :independent)]
    {:invocation-id
     (H [:gravity/sh07-b51-attempt32-checker-invocation-id-v1
         {:schema-version 1 :role role-name :snapshot snapshot
          :checker-executable-id (:executable-id checker)
          :completed-at completed-at
          :evidence-relative-root evidence-relative-root}])
     :tool-closure-id
     (H [tool-domain
         {:schema-version 2 :role role
          :checker-identity-manifest-id
          (:identity-manifest-id checker-manifest)
          :materializer-dependency-commitments dependency-ids}])
     :reproduction-command-id
     (H [:gravity/sh07-b51-attempt32-tier0-reproduction-command-id-v1
         {:schema-version 1 :snapshot snapshot :completed-at completed-at
          :evidence-relative-root evidence-relative-root
          :main 'gravity.tools.sh07-b51-attempt32-checker-child}])
     :evidence-relative-path
     (str evidence-relative-root "/" (:output-name spec))
     :completed-at completed-at}))

(defn build-checker-envelope
  "Build the semantic checker input only inside the selected child process."
  [H role request products checker-manifest]
  (let [m0s (:m0s products)]
    {:architecture-report-sha256
     (get-in request [:evidence :architecture-report-sha256])
     :snapshot-commit (get-in request [:content :commit])
     :snapshot-tree (get-in request [:content :tree])
     :predecessor-executable-descriptor-id
     predecessor-executable-descriptor-id
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id (:emitter-source-id products)
     :emitter-semantic-id (:emitter-semantic-id products)
     :m0s m0s
     :m1 (:m1 products)
     :m2s (:m2s products)
     :bundle-core (:bundle-core products)
     :source-forms (:source-forms products)
     :checker (:checker checker-manifest)
     :provenance
     (checker-provenance H role request checker-manifest)}))

(defn- result-record [H role status authority]
  (let [base {:schema (if authority
                        :gravity/sh07-b51-checker-production-v1
                        :gravity/sh07-b51-checker-sentinel-v1)
              :role role :status status :authority authority}
        artifact-id
        (H [:gravity/sh07-b51-checker-result-artifact-id-v1 base])
        with-artifact (assoc base :artifact-id artifact-id)
        output-id
        (H [:gravity/sh07-b51-checker-result-output-id-v1
            with-artifact])]
    (assoc with-artifact :output-id output-id)))

(defn- optional-value [^Optional value]
  (when (.isPresent value) (.get value)))

(defn self-process
  "Capture the exact current process self-report (without exit state)."
  [role]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        parent (optional-value (.parent handle))
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        started (optional-value (.startInstant info))]
    (when-not (and parent command arguments started)
      (reject! :self-process))
    {:role role
     :pid (.pid handle)
     :parent-pid (.pid ^ProcessHandle parent)
     :command command
     :arguments arguments
     :start-instant (str ^Instant started)}))

(defn runtime-mxbean []
  (let [bean (ManagementFactory/getRuntimeMXBean)
        name (.getName bean)
        [_ pid host] (re-matches #"([0-9]+)@(.+)" name)]
    (when-not (and pid host) (reject! :runtime-mxbean {:name name}))
    {:name name
     :pid (Long/parseLong pid)
     :host host
     :start-time-millis (.getStartTime bean)
     :vm-name (.getVmName bean)
     :vm-vendor (.getVmVendor bean)
     :vm-version (.getVmVersion bean)
     :java-version (System/getProperty "java.version")
     :java-vendor (System/getProperty "java.vendor")
     :java-class-path (System/getProperty "java.class.path")}))

(defn- project-library? [namespace]
  (str/starts-with? (str namespace) "gravity."))

(defn- source-record-index [source-authority]
  (reduce
   (fn [result record]
     (if (or (str/ends-with? (:path record) ".clj")
             (str/ends-with? (:path record) ".cljc"))
       (if-let [namespace
                (some-> (source-namespace-core! (:bytes record) (:path record))
                        :namespace)]
         (do
           (when (contains? result namespace)
             (reject! :loaded-namespace-source-duplicate
                      {:namespace namespace}))
           (assoc result namespace record))
         result)
       result))
   {} (vals (:source-objects source-authority))))

(defn- loaded-project-source-records
  [source-authority project-namespaces]
  (let [index (source-record-index source-authority)
        coordinate
        {:artifact 'project/bootstrap-clojure-src
         :commit (get-in source-authority [:content :commit])
         :tree (get-in source-authority [:content :tree])}]
    (mapv
     (fn [namespace]
       (let [record (get index namespace)]
         (when-not record
           (reject! :loaded-namespace-source-missing
                    {:namespace namespace}))
         {:namespace namespace :logical-coordinate coordinate
          :relative-path (:path record) :blob-oid (:blob-oid record)
          :byte-count (:byte-count record) :sha256 (:raw-sha256 record)}))
     project-namespaces)))

(defn- loaded-namespace-receipt
  [H role selected forbidden source-authority]
  (let [libraries (loaded-libs)
        projects (->> libraries (filter project-library?) (sort-by str) vec)
        external (->> libraries (remove project-library?) (sort-by str) vec)
        base {:role role
              :project-namespaces projects
              :external-namespaces external
              :source-records
              (loaded-project-source-records source-authority projects)
              :forbidden-overlap
              (vec (filter #{(symbol forbidden)} projects))}]
    (when-not (and (some #{(symbol selected)} projects)
                   (empty? (:forbidden-overlap base)))
      (reject! :checker-isolation
               {:selected selected :forbidden forbidden
                :loaded projects}))
    (assoc base :root
           (H [:gravity/sh07-b51-loaded-namespace-root-v1 base]))))

(defn execute-request!
  "Run one selected checker after strict request and role validation.

  The selected checker is loaded before the arity-two source constructor so
  that the constructor can privately derive the process-local role.  Checker
  semantics run only after the constructor returns its finalized receipt."
  [preflight codec request]
  (let [role (:role preflight)
        spec (get role-specs role)
        H (:H codec)]
    (validate-checker-request! role request)
    (require (symbol bootstrap-namespace))
    (require (symbol identity-namespace))
    ;; This is the sole role-dependent dynamic project load in the child.
    (require (symbol (:checker-namespace spec)))
    (when (contains? (loaded-libs) (symbol (:forbidden-namespace spec)))
      (reject! :checker-isolation {:role role}))
    (let [builder
          (resolve-public!
           bootstrap-namespace
           "p15-s23-sh07-b51-build-receipted-bundle-core-products")
          checker-fn
          (resolve-public! (:checker-namespace spec)
                           (:checker-entrypoint spec))
          identity-fn
          (resolve-public! identity-namespace "checker-identity-manifest")
          content (:content request)
          products
          (builder (:commit content) (:tree content))
          _ (validate-receipted-products! H role request products)
          manifest
          (identity-fn (:source-authority preflight) role)
          envelope (build-checker-envelope H role request products manifest)
          output (checker-fn envelope)
          _ (when-not (= :accepted (:status output))
              (reject! :checker-output {:role role :status (:status output)}))
          production (result-record H role :accepted true)
          sentinel (result-record H role :accepted false)
          loaded
          (loaded-namespace-receipt H role (:checker-namespace spec)
                                    (:forbidden-namespace spec)
                                    (:source-authority preflight))]
      {:role role
       :checker-identity manifest
       :checker-output output
       :source-read-receipt (:source-read-receipt products)
       :source-session-id
       (source-session-id H (:source-read-receipt products))
       :production production
       :sentinel sentinel
       :loaded-namespace-receipt loaded})))

(defn build-response
  "Construct the closed child wire envelope from process-local results."
  [preflight codec request execution]
  (let [H (:H codec)
        role (:role execution)
        process (self-process role)
        argv (into [(:command process)] (:arguments process))
        argv-id (H [:gravity/sh07-b51-process-argv-id-v1 argv])
        request-id
        (H [:gravity/sh07-b51-checker-request-id-v1 request])
        loaded (:loaded-namespace-receipt execution)
        source-closure-base
        {:role role
         :entrypoint
         'gravity.tools.sh07-b51-attempt32-checker-child
         :static-project-namespaces []
         :dynamic-project-namespaces
         (vec (remove
               #{'gravity.tools.sh07-b51-attempt32-checker-child}
               (:project-namespaces loaded)))
         :source-records (:source-records loaded)
         :forbidden-namespaces
         [(symbol (:forbidden-namespace (get role-specs role)))]}
        source-closure
        (assoc source-closure-base :root
               (H [:gravity/sh07-b51-source-closure-root-v1
                   source-closure-base]))
        mxbean (runtime-mxbean)
        host-surface-base
        {:policy :gravity/sh07-b51-trusted-clojure-host-surface-v1
         :macro-vars [] :interop-members [] :state-primitives []
         :dynamic-load-primitives ['require] :class-definitions []}
        host-surface
        (assoc host-surface-base :root
               (H [:gravity/sh07-b51-host-surface-root-v1
                   host-surface-base]))
        tcb-base
        {:role role :process process :runtime-mxbean mxbean
         :java {:logical-role :java :canonical-path (:command process)}
         :git {:logical-role :git :canonical-path "/usr/bin/git"}
         :classpath (:classpath-receipt preflight)
         :pre-manifest-tcb (:pre-manifest-tcb preflight)
         :eligibility-check (:eligibility-preflight preflight)
         :environment (:environment preflight)
         :source-closure source-closure
         :loaded-namespace-receipt loaded
         :host-surface host-surface}
        tcb (assoc tcb-base :manifest-id
                   (H [:gravity/sh07-b51-checker-child-tcb-manifest-v1
                       tcb-base]))
        base
        {:schema :gravity/sh07-b51-checker-child-response-v1
         :role role
         :transport
         {:argv-id argv-id
          :request-id request-id
          :response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}
         :result
         (select-keys execution
                      [:checker-identity :checker-output :source-read-receipt
                       :production :sentinel])
         :counters
         {:work {:vector [3 3 3 2 2] :total 13}
          :source-session
          (get-in execution [:source-read-receipt :counters])
          :process {:child-count 1 :start-count 1 :exit-count 1
                    :overlap-count 0}
          :ipc {:request-count 1 :response-count 1
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576
                :deadline-ms 3600000}}
         :tcb tcb
         :diagnostics []}
        response-id
        (H [:gravity/sh07-b51-response-preimage-id-v1
            (update base :transport dissoc :response-preimage-id)])]
    (assoc-in base [:transport :response-preimage-id] response-id)))

(defn validate-response-shape!
  "Small parent-safe top-level response check used by process tests."
  [expected-role response]
  (when-not (and (exact-keys? response checker-response-keys)
                 (= :gravity/sh07-b51-checker-child-response-v1
                    (:schema response))
                 (= expected-role (:role response))
                 (empty? (:diagnostics response)))
    (reject! :response-shape {:role expected-role}))
  response)

(defn- actual-preflight-input [arguments]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        process-arguments (some-> (optional-value (.arguments info)) vec)]
    (when-not (and command process-arguments)
      (reject! :child-argv-self-report))
    {:arguments (vec arguments)
     :environment (into {} (System/getenv))
     :working-directory
     (str (.normalize (.toAbsolutePath
                       (Paths/get (System/getProperty "user.dir")
                                  (make-array String 0)))))
     :classpath (System/getProperty "java.class.path")
     :process-argv (into [command] process-arguments)}))

(defn -main [& arguments]
  (try
    (let [preflight (validate-entry-preflight!
                     (actual-preflight-input arguments))
          request-frame (read-request-before-codec! System/in)
          request (:value request-frame)
          _ (validate-checker-request! (:role preflight) request)
          preflight (assoc preflight :content (:content request))
          pre-manifest (pre-manifest-tcb! preflight)
          codec (load-codec!)
          decoded ((:strict-read-wire codec) :request (:bytes request-frame))
          _ (when-not (= request decoded) (reject! :request-codec-parity))
          classpath (validate-complete-classpath! preflight codec)
          preflight (merge preflight classpath
                           {:pre-manifest-tcb pre-manifest})
          _ (validate-checker-request! (:role preflight) request)
          eligibility
          (validate-eligibility-five-operations! preflight codec request)
          preflight
          (assoc preflight
                 :eligibility-preflight (:eligibility-check eligibility)
                 :eligibility-check-root (:eligibility-check-root eligibility))
          execution (execute-request! preflight codec request)
          response (build-response preflight codec request execution)
          bytes ((:W codec) response)]
      ((:bounded-byte-count! codec) :response (alength bytes))
      (.write System/out bytes)
      (.flush System/out))
    (catch Throwable _
      ;; Child rejection is deliberately payload-free: M owns diagnostics.
      (System/exit 2))))
