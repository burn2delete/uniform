(ns gravity.self-hosting.sh01-exact-tree-warm-worker
  "Exact-tree-bound, non-authoritative warm JVM development test worker."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test]
            [gravity.development-test-runner :as runner]
            [gravity.digest :as digest]
            [gravity.bootstrap :as bootstrap])
  (:import (java.io BufferedReader ByteArrayOutputStream InputStreamReader
                    OutputStream OutputStreamWriter PushbackReader StringReader)
           (java.nio ByteBuffer)
           (java.nio.charset CodingErrorAction)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path Paths)
           (java.nio ByteBuffer)
           (java.nio.file.attribute BasicFileAttributes)
           (java.util.concurrent TimeUnit)))

(def request-schema :gravity/exact-tree-warm-worker-request-v1)
(def cached-request-schema :gravity/exact-tree-warm-worker-cached-request-v1)
(def response-schema :gravity/exact-tree-warm-worker-response-v2)
(def cached-response-schema :gravity/exact-tree-warm-worker-cached-response-v1)
(def artifact-request-schema :gravity/exact-tree-warm-worker-artifact-request-v1)
(def artifact-response-schema :gravity/exact-tree-warm-worker-artifact-response-v1)
(def artifact-projection-schema :gravity/authenticated-artifact-fast-projection-v1)
(def ready-schema :gravity/exact-tree-warm-worker-ready-v1)
(def ^:private response-prefix "WARM_DEV ")
(def ^:private maximum-line-length 65536)
(def ^:private maximum-output-length (* 256 1024))
(def ^:private maximum-requests 256)
(def ^:private maximum-cached-count 1000000)
(def ^:private maximum-cached-elapsed-ms (* 24 60 60 1000))
(def ^:private oid-pattern #"[0-9a-f]{40,64}")
(def ^:private identity-pattern #"sha256:[0-9a-f]{64}")
(def ^:private namespace-catalog-snapshot runner/namespace-catalog)
(def ^:private namespace-authority
  (set (map (comp str :namespace) namespace-catalog-snapshot)))
(def ^:private exact-request-keys
  #{:schema :request-id :operation :candidate-commit :candidate-tree :args})
(def ^:private shutdown-request-keys
  #{:schema :request-id :operation :candidate-commit :candidate-tree})
(def ^:private cached-request-keys
  #{:schema :request-id :operation :candidate-commit :candidate-tree
    :lane :route-id :plan-id :catalog-id :closure-identity
    :result-contract-id :shard})
(def ^:private artifact-request-keys
  #{:schema :request-id :operation :candidate-commit :candidate-tree
    :lane :route-id :plan-id :catalog-id :closure-identity :binding-id
    :row-id :stage})
(def ^:private artifact-response-keys
  #{:schema :authority :authoritative? :request-id :candidate-commit
    :candidate-tree :lane :route-id :plan-id :catalog-id :closure-identity
    :binding-id :row-id :stage :outcome :exit-code :elapsed-ns
    :request-count :projection :stdout :stderr :output-truncated?})
(def ^:private artifact-projection-keys
  #{:schema :binding-id :projection-id :pipeline :result-id :requested-stage
    :selected-artifact-id :stage-artifact-ids :source-content-hash
    :source-provenance-id :source-relative-path :cache-decision
    :execution-boundary :authority :mutation-detected?
    :provenance-checked? :source-authenticated? :closure-verified?
    :fresh-replayable? :diagnostics})
(def ^:private artifact-projection-semantic-keys
  [:schema :binding-id :pipeline :result-id :requested-stage
   :selected-artifact-id :stage-artifact-ids :source-content-hash
   :source-provenance-id :execution-boundary :authority :mutation-detected?
   :provenance-checked? :source-authenticated? :closure-verified?
   :fresh-replayable? :diagnostics])
(def ^:private artifact-max-stage-rank
  (zipmap [:source :reader :syntax :macroexpansion :resolution :checked-core]
          (range)))
(def ^:private artifact-max-bytes (* 256 1024))
(def ^:private cached-response-keys
  #{:schema :authority :authoritative? :request-id :candidate-commit
    :candidate-tree :lane :route-id :plan-id :catalog-id :closure-identity
    :result-contract-id :shard :outcome :exit-code :elapsed-ns :request-count
    :runner-result :stdout :stderr :output-truncated?})
(def cached-result-contract-id :gravity/sh07-cached-shard-result-v1)
(def ^:private cached-result-keys
  #{:test :pass :fail :error :artifact :shard :test-vars
    :cache-authoritative? :fresh-authoritative-run-required?
    :cache-context :elapsed-ms :cache})
(def ^:private cached-counter-keys #{:test :pass :fail :error})
(def ^:private cached-cache-counter-keys
  #{:sh06-hits :sh06-misses :core-hits :core-misses
    :verification-hits :verification-misses})
(def ^:private cached-context-keys
  #{:request-schema-version :adapter :checked-core-source-content-hash
    :checked-core-plan-semantic-hash :java-runtime-version :clojure-version})
(def ^:private cached-context-adapter
  :gravity/sh07-to-c6-core-products-v16)
(def ^:private cached-context-source-content-hash
  "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092")
(def ^:private cached-context-plan-semantic-hash
  "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd")

(def ^:dynamic *artifact-pipeline-runner*
  "Injected pipeline seam for bounded worker tests."
  nil)

(defn- fail! [id message data]
  (throw (ex-info message (assoc data :id id))))

(defn- ensure!
  [value id message data]
  (when-not value (fail! id message data))
  value)

(defn- repository-root
  []
  (loop [candidate (.toPath (java.io.File. (System/getProperty "user.dir")))]
    (if (Files/isRegularFile (.resolve candidate "contracts/verification-catalog.edn")
                             (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
      (.toRealPath candidate (make-array LinkOption 0))
      (if-let [parent (.getParent candidate)]
        (recur parent)
        (fail! "WARM-ARTIFACT-ROOT" "Could not locate repository root" {})))))

(defn- artifact-path-parts
  [^Path root relative]
  (let [relative-path (Paths/get ^String relative (make-array String 0))
        candidate (.normalize (.resolve root relative-path))]
    (ensure! (and (not (.isAbsolute relative-path))
                  (.startsWith candidate root)
                  (= relative (str (.normalize relative-path)))
                  (not (Files/isSymbolicLink root)))
             "WARM-ARTIFACT-PATH" "Artifact binding path is not canonical"
             {:path relative})
    (loop [current root
           names (seq (iterator-seq (.iterator relative-path)))
           paths [root]]
      (if-let [name (first names)]
        (let [next-path (.resolve ^Path current ^Path name)]
          (ensure! (Files/exists next-path
                                  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   "WARM-ARTIFACT-MISSING" "Artifact binding path is missing"
                   {:path relative :component name})
          (ensure! (not (Files/isSymbolicLink next-path))
                   "WARM-ARTIFACT-SYMLINK" "Artifact binding path contains a symlink"
                   {:path relative :component name})
          (when (next names)
            (ensure! (Files/isDirectory next-path
                                        (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                     "WARM-ARTIFACT-PATH" "Artifact binding parent is not a directory"
                     {:path relative :component name}))
          (recur next-path (next names) (conj paths next-path)))
        {:paths paths :file candidate}))))

(defn- file-attributes
  [^Path path]
  (let [attrs (Files/readAttributes path BasicFileAttributes
                                    (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
    {:directory? (.isDirectory attrs)
     :regular? (.isRegularFile attrs)
     :symbolic-link? (Files/isSymbolicLink path)
     :file-key (str (.fileKey attrs))
     :size (.size attrs)
     :modified-ms (.toMillis (.lastModifiedTime attrs))}))

(defn- bounded-file-bytes
  [^Path path]
  (with-open [channel (Files/newByteChannel
                       path
                       (into-array java.nio.file.OpenOption
                                   [java.nio.file.StandardOpenOption/READ
                                    LinkOption/NOFOLLOW_LINKS]))]
    (let [out (ByteArrayOutputStream.)
          buffer (ByteBuffer/allocate 16384)]
      (loop []
        (.clear buffer)
        (let [read (.read channel buffer)]
          (when (pos? read)
            (ensure! (<= (+ (.size out) read) artifact-max-bytes)
                     "WARM-ARTIFACT-BYTES" "Artifact fixture exceeds its bound"
                     {:path (str path) :bytes (+ (.size out) read)})
            (.write out (.array buffer) 0 read)
            (recur))))
      (.toByteArray out))))

(defn- artifact-path-snapshot
  [^Path root relative]
  (let [{:keys [paths file]} (artifact-path-parts root relative)
        attrs (mapv (fn [^Path path] [(str (.relativize root path))
                                      (file-attributes path)]) paths)
        file-attrs (last attrs)]
    (ensure! (and (true? (get-in file-attrs [1 :regular?]))
                  (false? (get-in file-attrs [1 :symbolic-link?])))
             "WARM-ARTIFACT-FILE" "Artifact binding target is not a regular file"
             {:path relative})
    (let [bytes (bounded-file-bytes file)]
    {:relative relative
     :paths attrs
     :bytes-count (alength bytes)
     :bytes-sha256 (str "sha256:" (digest/sha256-bytes-hex bytes))
     :bytes bytes})))

(defn- artifact-pair-snapshot
  [^Path root binding]
  (let [gravity (artifact-path-snapshot root (:gravity-path binding))
        qst (artifact-path-snapshot root (:qst-path binding))]
    (ensure! (and (= (:bytes-count gravity) (:bytes-count qst))
                  (= (:bytes-sha256 gravity) (:bytes-sha256 qst))
                  (java.util.Arrays/equals ^bytes (:bytes gravity)
                                           ^bytes (:bytes qst)))
             "WARM-ARTIFACT-PAIR" "Fixture peers changed or differ" {})
    {:gravity gravity
     :qst qst
     :pair-sha256 (:bytes-sha256 gravity)
     :pair-bytes (:bytes-count gravity)}))

(defn- strict-edn [text]
  (let [eof (Object.)]
    (with-open [reader (PushbackReader. (StringReader. text))]
      (let [value (edn/read {:eof eof :readers {}} reader)
            trailing (edn/read {:eof eof :readers {}} reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (fail! "WARM-REQUEST-EDN" "request must contain exactly one EDN value" {}))
        value))))

(defn- bounded-text [value]
  (let [value (str value)]
    {:text (subs value 0 (min maximum-output-length (count value)))
     :truncated? (> (count value) maximum-output-length)}))

(defn- bounded-writer []
  (let [retained (ByteArrayOutputStream.)
        observed (atom 0)
        stream (proxy [OutputStream] []
      (write
        ([value]
         (let [current @observed]
           (swap! observed inc)
           (when (< current maximum-output-length)
             (.write retained (int value)))))
        ([bytes offset length]
         (let [remaining (max 0 (- maximum-output-length @observed))
               accepted (int (min remaining length))]
           (swap! observed + length)
           (when (pos? accepted)
             (.write retained ^bytes bytes (int offset) accepted)))))
      (flush [])
      (close []))]
    {:writer (OutputStreamWriter. stream StandardCharsets/UTF_8)
     :retained retained :observed observed}))

(defn- strict-utf8-prefix [^bytes bytes]
  (loop [length (alength bytes)]
    (let [decoded
          (try
            (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                            (.onMalformedInput CodingErrorAction/REPORT)
                            (.onUnmappableCharacter CodingErrorAction/REPORT))]
              (str (.decode decoder (ByteBuffer/wrap bytes 0 length))))
            (catch java.nio.charset.CharacterCodingException _ nil))]
      (if (some? decoded)
        decoded
        (if (and (pos? length) (<= (- (alength bytes) length) 3))
          (recur (dec length))
          "")))))

(defn- bounded-writer-result [{:keys [^OutputStreamWriter writer
                                      ^ByteArrayOutputStream retained observed]}]
  (.flush writer)
  {:text (strict-utf8-prefix (.toByteArray retained))
   :truncated? (> @observed maximum-output-length)})

(defn- fatal-cause [throwable]
  (loop [cause throwable]
    (cond
      (nil? cause) nil
      (or (instance? ThreadDeath cause)
          (instance? VirtualMachineError cause)
          (instance? LinkageError cause)) cause
      :else (recur (.getCause ^Throwable cause)))))

(defn- interrupted-cause
  [throwable]
  (loop [cause throwable]
    (cond
      (nil? cause) nil
      (instance? InterruptedException cause) cause
      :else (recur (.getCause ^Throwable cause)))))

(defn- rethrow-control!
  [throwable]
  (when-let [interrupted (interrupted-cause throwable)]
    (.interrupt (Thread/currentThread))
    (throw interrupted))
  (when-let [fatal (fatal-cause throwable)]
    (throw fatal)))

(defn- exact-runner-args! [args]
  (when-not (and (vector? args) (<= 4 (count args) 64)
                 (every? #(and (string? %) (<= 1 (count %) 1024)) args))
    (fail! "WARM-REQUEST-ARGS" "args must be a bounded string vector" {}))
  (loop [remaining (seq args) namespaces 0 selectors 0]
    (if-let [option (first remaining)]
      (case option
        "--namespace" (if-let [value (second remaining)]
                        (do
                          (when-not (contains? namespace-authority value)
                            (fail! "WARM-REQUEST-NAMESPACE"
                                   "namespace is outside the frozen development catalog"
                                   {:namespace value}))
                          (recur (nnext remaining) (inc namespaces) selectors))
                        (fail! "WARM-REQUEST-ARGS" "--namespace requires a value" {}))
        "--exact" (if-let [value (second remaining)]
                    (recur (nnext remaining) namespaces (inc selectors))
                    (fail! "WARM-REQUEST-ARGS" "--exact requires a value" {}))
        "--fail-fast" (recur (next remaining) namespaces selectors)
        (fail! "WARM-REQUEST-ARGS" "worker accepts only exact development selections"
               {:option option}))
      (when-not (and (pos? namespaces) (pos? selectors))
        (fail! "WARM-REQUEST-ARGS"
               "worker requires at least one namespace and exact selector" {}))))
  args)

(defn- bounded-cached-integer?
  [value]
  (and (integer? value)
       (<= 0 value maximum-cached-count)))

(defn- bounded-cached-elapsed?
  [value]
  (and (integer? value)
       (<= 0 value maximum-cached-elapsed-ms)))

(defn- validate-cached-context!
  [context]
  (when-not (map? context)
    (fail! "WARM-CACHED-CONTEXT" "cached result context must be a map"
           {:context context}))
  (when-not (= cached-context-keys (set (keys context)))
    (fail! "WARM-CACHED-CONTEXT-KEYS"
           "cached result context keys are not closed"
           {:keys (vec (sort (map str (keys context))))}))
  (when-not (= 15 (:request-schema-version context))
    (fail! "WARM-CACHED-CONTEXT" "cached result context schema is unsupported"
           {:request-schema-version (:request-schema-version context)}))
  (let [bootstrap-ns (find-ns 'gravity.bootstrap)
        bootstrap-value
        (fn [name fallback]
          (if-let [value (and bootstrap-ns (ns-resolve bootstrap-ns name))]
            (if (bound? value) @value fallback)
            fallback))
        expected-adapter
        (bootstrap-value 'sh07-core-adapter-contract cached-context-adapter)
        expected-source-hash
        (bootstrap-value 'sh07-core-expected-source-content-hash
                         cached-context-source-content-hash)
        expected-plan-hash
        (bootstrap-value 'sh07-core-expected-plan-semantic-hash
                         cached-context-plan-semantic-hash)]
    (when-not (= expected-adapter (:adapter context))
    (fail! "WARM-CACHED-CONTEXT" "cached result adapter is unsupported"
           {:expected expected-adapter :actual (:adapter context)}))
    (doseq [[field expected]
            [[:checked-core-source-content-hash expected-source-hash]
             [:checked-core-plan-semantic-hash expected-plan-hash]]]
      (when-not (= expected (field context))
        (fail! "WARM-CACHED-CONTEXT" "cached result core identity is unsupported"
               {:field field :expected expected :actual (field context)})))
    (doseq [[field expected]
            [[:java-runtime-version (System/getProperty "java.runtime.version")]
             [:clojure-version (clojure-version)]]]
      (when-not (= expected (field context))
        (fail! "WARM-CACHED-CONTEXT" "cached result runtime identity is unsupported"
               {:field field :expected expected :actual (field context)}))))
  context)

(defn- validate-cached-result!
  [shard result]
  (when-not (map? result)
    (fail! "WARM-CACHED-RESULT" "cached runner result must be a map"
           {:shard shard :result-type (str (type result))}))
  (when-not (= cached-result-keys (set (keys result)))
    (fail! "WARM-CACHED-RESULT-KEYS"
           "cached runner result keys are not closed"
           {:shard shard :keys (vec (sort (map str (keys result))))}))
  (doseq [field (sort cached-counter-keys)]
    (when-not (bounded-cached-integer? (field result))
      (fail! "WARM-CACHED-COUNTER"
             "cached runner counter must be a bounded non-negative integer"
             {:shard shard :field field :value (field result)})))
  (when-not (and (integer? (:test result)) (pos? (:test result)))
    (fail! "WARM-CACHED-COUNTER" "cached runner test count must be positive"
           {:shard shard :field :test :value (:test result)}))
  (when-not (= :gravity/sh07-cached-shard-result (:artifact result))
    (fail! "WARM-CACHED-RESULT" "cached runner artifact is unsupported"
           {:shard shard :artifact (:artifact result)}))
  (when-not (and (string? (:shard result))
                 (<= 1 (count (:shard result)) 128)
                 (= shard (:shard result)))
    (fail! "WARM-CACHED-RESULT-SHARD"
           "cached runner result shard does not match the request"
           {:shard shard :result-shard (:shard result)}))
  (when-not (and (integer? (:test-vars result))
                 (pos? (:test-vars result))
                 (<= (:test-vars result) maximum-cached-count))
    (fail! "WARM-CACHED-COUNTER"
           "cached runner test-var count must be a bounded positive integer"
           {:shard shard :field :test-vars :value (:test-vars result)}))
  (when-not (false? (:cache-authoritative? result))
    (fail! "WARM-CACHED-AUTHORITY"
           "cached runner cannot provide authoritative evidence"
           {:shard shard :cache-authoritative? (:cache-authoritative? result)}))
  (when-not (true? (:fresh-authoritative-run-required? result))
    (fail! "WARM-CACHED-AUTHORITY"
           "cached runner must require a fresh authoritative run"
           {:shard shard
            :fresh-authoritative-run-required?
            (:fresh-authoritative-run-required? result)}))
  (validate-cached-context! (:cache-context result))
  (let [cache (:cache result)]
    (when-not (map? cache)
      (fail! "WARM-CACHED-CACHE" "cached result cache counters must be a map"
             {:shard shard :cache cache}))
    (when-not (= cached-cache-counter-keys (set (keys cache)))
      (fail! "WARM-CACHED-CACHE-KEYS"
             "cached result cache counter keys are not closed"
             {:shard shard :keys (vec (sort (map str (keys cache))))}))
    (doseq [field (sort cached-cache-counter-keys)]
      (when-not (bounded-cached-integer? (field cache))
        (fail! "WARM-CACHED-CACHE"
               "cached result cache counter must be a bounded non-negative integer"
               {:shard shard :field field :value (field cache)}))))
  (when-not (bounded-cached-elapsed? (:elapsed-ms result))
    (fail! "WARM-CACHED-RESULT"
           "cached runner elapsed time must be a bounded non-negative integer"
           {:shard shard :field :elapsed-ms :value (:elapsed-ms result)}))
  result)

(defn- identity-text?
  [value]
  (and (string? value) (re-matches identity-pattern value)))

(defn- cached-plan-for-route
  "Build and validate the fast plan that declares a cached request.

  The worker deliberately derives the shard and all plan identities from this
  reviewed route.  It has no independent shard inventory or route-prefix
  classifier, so a route can be admitted only when the UEP itself declares a
  closed cached executor request."
  [route-id]
  (when-not (and (string? route-id)
                 (<= 1 (count route-id) 256))
    (fail! "WARM-CACHED-REQUEST-IDENTITY"
           "cached request route id must be bounded text"
           {:route-id route-id}))
  (try
    (let [plan-fn (or (requiring-resolve
                       'gravity.self-hosting.unified-execution-plan/plan)
                      (fail! "WARM-CACHED-CATALOG"
                             "cached plan builder is unavailable" {}))
          validate-fn (or (requiring-resolve
                           'gravity.self-hosting.unified-execution-plan/validate-plan)
                          (fail! "WARM-CACHED-CATALOG"
                                 "cached plan validator is unavailable" {}))
          plan (plan-fn {:route-id route-id :lane :fast})]
      (validate-fn plan)
      plan)
    (catch clojure.lang.ExceptionInfo error
      (rethrow-control! error)
      (fail! "WARM-CACHED-CATALOG"
             "cached route does not resolve to a validated fast execution plan"
             {:route-id route-id
              :cause-id (:id (ex-data error))}))
    (catch Throwable error
      (rethrow-control! error)
      (fail! "WARM-CACHED-CATALOG"
             "cached route plan construction failed"
             {:route-id route-id
              :class (.getName (class error))}))))

(defn- cached-plan-for-request
  "Validate every cached request identity against its declarative UEP plan."
  [request]
  (let [plan (cached-plan-for-route (:route-id request))
        executor-request (get-in plan [:executor :request])]
    (when-not (and (map? executor-request)
                   (= #{:operation :shard :result-contract-id}
                      (set (keys executor-request))))
      (fail! "WARM-CACHED-CATALOG"
             "cached route has no closed executor request"
             {:route-id (:route-id request)}))
    (when-not (= :fast (:lane request))
      (fail! "WARM-CACHED-REQUEST-IDENTITY"
             "cached requests are admitted only in the fast lane"
             {:lane (:lane request)}))
    (doseq [[field value]
            [[:plan-id (:plan-id plan)]
             [:catalog-id (:catalog-id plan)]
             [:closure-identity (:closure-identity plan)]]]
      (when-not (identity-text? (get request field))
        (fail! "WARM-CACHED-REQUEST-IDENTITY"
               "cached request identity must be a sha256 content identity"
               {:field field :value (get request field)}))
      (when-not (= value (get request field))
        (fail! "WARM-CACHED-REQUEST-IDENTITY"
               "cached request identity differs from the validated plan"
               {:field field :expected value :actual (get request field)})))
    (when-not (and (string? (:shard request))
                   (<= 1 (count (:shard request)) 128))
      (fail! "WARM-CACHED-REQUEST-IDENTITY"
             "cached request shard must be bounded text"
             {:shard (:shard request)}))
    (doseq [[field expected]
            [[:route-id (:route-id plan)]
             [:lane :fast]
             [:result-contract-id (:result-contract-id executor-request)]
             [:shard (:shard executor-request)]]]
      (when-not (= expected (get request field))
        (fail! "WARM-CACHED-REQUEST-IDENTITY"
               "cached request does not equal its declarative executor request"
               {:field field :expected expected :actual (get request field)
                :route-id (:route-id request)})))
    (when-not (= :cached-shard (:operation executor-request))
      (fail! "WARM-CACHED-CATALOG"
             "cached route executor operation is not closed"
             {:route-id (:route-id request)
              :operation (:operation executor-request)}))
    (when-not (= cached-result-contract-id (:result-contract-id request))
      (fail! "WARM-CACHED-REQUEST-IDENTITY"
             "cached request result contract is unsupported"
             {:expected cached-result-contract-id
              :actual (:result-contract-id request)}))
    plan))

(defn- artifact-plan-for-request
  "Rebuild the fast UEP plan and fixture binding from request identities.

  The request carries no source path or compiler arguments.  Paths are derived
  only after this route, closure, and row binding have been authenticated."
  [request]
  (let [plan-fn (or (requiring-resolve
                     'gravity.self-hosting.unified-execution-plan/plan)
                    (fail! "WARM-ARTIFACT-PLAN" "UEP planner is unavailable" {}))
        validate-fn (or (requiring-resolve
                         'gravity.self-hosting.unified-execution-plan/validate-plan)
                        (fail! "WARM-ARTIFACT-PLAN" "UEP validator is unavailable" {}))
        binding-fn (or (requiring-resolve
                        'gravity.self-hosting.unified-execution-plan/fixture-source-binding)
                       (fail! "WARM-ARTIFACT-BINDING" "UEP fixture binding is unavailable" {}))
        plan (plan-fn {:route-id (:route-id request) :lane :fast})]
    (ensure! (= :fast (:lane request)) "WARM-ARTIFACT-LANE"
             "Artifact request lane must be exactly :fast" {})
    (validate-fn plan)
    (ensure! (= :fast (:lane plan)) "WARM-ARTIFACT-LANE"
             "Artifact worker accepts only fast plans" {})
    (ensure! (= :warm-bounded-process (:execution-mode plan))
             "WARM-ARTIFACT-LANE" "Artifact worker accepts only warm plans" {})
    (doseq [[field value]
            [[:plan-id (:plan-id plan)]
             [:catalog-id (:catalog-id plan)]
             [:closure-identity (:closure-identity plan)]]]
      (ensure! (= value (get request field)) "WARM-ARTIFACT-IDENTITY"
               "Artifact request identity differs from the canonical plan"
               {:field field :expected value :actual (get request field)}))
    (let [binding (binding-fn plan (:row-id request) (:stage request))]
      (ensure! (= (:binding-id binding) (:binding-id request))
               "WARM-ARTIFACT-BINDING" "Fixture binding identity changed" {})
      {:plan plan :binding binding})))

(defn- validate-artifact-projection!
  [projection request binding]
  (let [entries (:stage-artifact-ids projection)
        stages (mapv first entries)
        ids (mapv second entries)
        expected-stages (vec (take (count entries)
                                   [:source :reader :syntax :macroexpansion
                                    :resolution :checked-core]))]
    (ensure! (and (map? projection)
                (= artifact-projection-keys (set (keys projection)))
                (= artifact-projection-schema (:schema projection))
                (= (:binding-id binding) (:binding-id projection))
                (= (:fixture-sha256 binding) (:source-content-hash projection))
                (= (:gravity-path binding) (:source-relative-path projection))
                (re-matches identity-pattern (:projection-id projection))
                (re-matches identity-pattern (:result-id projection))
                (re-matches identity-pattern (:selected-artifact-id projection))
                (= (:stage request) (:requested-stage projection))
                (= :fast (get-in projection [:execution-boundary :mode]))
                (= :same-jvm (get-in projection [:execution-boundary :isolation]))
                (false? (get-in projection [:execution-boundary :isolated?]))
                (false? (get-in projection [:execution-boundary :acceptance-authority?]))
                (= :non-authoritative (get-in projection [:authority :ceiling]))
                (false? (get-in projection [:authority :authoritative?]))
                (false? (:mutation-detected? projection))
                (true? (:provenance-checked? projection))
                (true? (:source-authenticated? projection))
                (true? (:closure-verified? projection))
                (false? (:fresh-replayable? projection))
                (vector? (:stage-artifact-ids projection))
                (<= 1 (count (:stage-artifact-ids projection)) 6)
                (= expected-stages stages)
                (every? (fn [entry]
                          (and (vector? entry) (= 3 (count entry))
                               (keyword? (first entry))
                               (string? (second entry))
                               (vector? (nth entry 2))))
                        (:stage-artifact-ids projection))
                (every? (fn [index]
                          (= (nth (nth entries index) 2)
                             (vec (take index ids))))
                        (range (count entries)))
                (vector? (:diagnostics projection))
                (<= (count (:diagnostics projection)) 64)
                (every? map? (:diagnostics projection))
                (= (:projection-id projection)
                   (bootstrap/reader-canonical-hash
                    (select-keys projection artifact-projection-semantic-keys))))
           "WARM-ARTIFACT-PROJECTION" "Artifact projection is not closed"
           {:projection projection :request request})
    projection))

(defn- artifact-projection
  [request binding result]
  (let [stage-map (:stage-artifacts result)
        stages (vec (keys stage-map))
        selected (:artifact result)
        provenance (:provenance selected)
        projection {:schema artifact-projection-schema
                    :binding-id (:binding-id binding)
                    :pipeline (:pipeline result)
                    :result-id (:result-id result)
                    :requested-stage (:requested-stage result)
                    :selected-artifact-id (:artifact-id selected)
                    :stage-artifact-ids
                    (mapv (fn [stage]
                            [stage (:artifact-id (get stage-map stage))
                             (vec (:input-artifact-ids (get stage-map stage)))])
                          stages)
                    :source-content-hash (:source-content-hash result)
                    :source-provenance-id
                    (str "sha256:" (digest/sha256-hex (pr-str provenance)))
                    :source-relative-path (:gravity-path binding)
                    :cache-decision (if (true? (get-in selected [:cache-policy :observed-hit?]))
                                      :hit :miss)
                    :execution-boundary {:mode :fast :isolation :same-jvm
                                         :isolated? false
                                         :acceptance-authority? false}
                    :authority {:authoritative? false :ceiling :non-authoritative}
                    :mutation-detected? false
                    :provenance-checked? true
                    :source-authenticated? true
                    :closure-verified? true
                    :fresh-replayable? false
                    :diagnostics (vec (or (:diagnostics selected) []))}
        projection (assoc projection :projection-id
                           (bootstrap/reader-canonical-hash
                            (select-keys projection artifact-projection-semantic-keys)))]
    (validate-artifact-projection! projection request binding)))

(defn- invoke-artifact-runner
  [request binding]
  (let [root (repository-root)
        before (artifact-pair-snapshot root binding)
        _ (ensure! (and (= (:pair-sha256 before) (:fixture-sha256 binding))
                        (= (:pair-bytes before) (:fixture-bytes binding)))
                   "WARM-ARTIFACT-BINDING"
                   "Fixture binding bytes do not match the request" {})
        result
        (let [runner (or *artifact-pipeline-runner*
                         (requiring-resolve
                          'gravity.authenticated-artifact-pipeline/run-pipeline))]
          (ensure! (if (var? runner) (ifn? (var-get runner)) (ifn? runner))
                   "WARM-ARTIFACT-PIPELINE" "Artifact pipeline runner is unavailable" {})
          (let [started (System/nanoTime)
                result ((if (var? runner) (var-get runner) runner)
                        {:source-path (:gravity-path binding)
                         :target-stage (:stage request)
                         :mode :fast})]
            (ensure! (and (map? result)
                          (= :gravity/authenticated-artifact-pipeline-result (:kind result))
                          (= :fast (:mode result))
                          (= (:stage request) (:requested-stage result))
                          (false? (get-in result [:authority :authoritative?]))
                          (= :same-jvm (get-in result [:execution-boundary :isolation])))
                     "WARM-ARTIFACT-PIPELINE" "Pipeline result failed its fast contract"
                     {})
            {:exit 0 :elapsed-ns (- (System/nanoTime) started)
             :projection (artifact-projection request binding result)
             :out "" :err "" :out-truncated? false :err-truncated? false}))
        after-binding (artifact-plan-for-request request)
        after (artifact-pair-snapshot root (:binding after-binding))]
        (ensure! (and (= (:pair-sha256 before) (:pair-sha256 after))
                  (= (:pair-bytes before) (:pair-bytes after))
                  (= (:paths (:gravity before)) (:paths (:gravity after)))
                  (= (:paths (:qst before)) (:paths (:qst after)))
                  (java.util.Arrays/equals ^bytes (get-in before [:gravity :bytes])
                                           ^bytes (get-in after [:gravity :bytes]))
                  (java.util.Arrays/equals ^bytes (get-in before [:qst :bytes])
                                           ^bytes (get-in after [:qst :bytes])))
             "WARM-ARTIFACT-MUTATED" "Fixture closure changed during artifact execution"
             {:before (select-keys before [:pair-sha256 :pair-bytes])
              :after (select-keys after [:pair-sha256 :pair-bytes])})
    (ensure! (and (= (:fixture-sha256 binding) (:pair-sha256 after))
                  (= (:fixture-bytes binding) (:pair-bytes after)))
             "WARM-ARTIFACT-BINDING" "Fixture binding bytes changed"
             {})
    result))

(defn validate-request!
  ([request expected]
   (validate-request! request expected nil))
  ([request expected cached-plan-holder]
   (when-not (map? request)
     (fail! "WARM-REQUEST-SHAPE" "request must be a map" {}))
   (let [operation (:operation request)
         allowed (case operation
                   :shutdown shutdown-request-keys
                   :run-cached-shard cached-request-keys
                   :run-fast-artifact artifact-request-keys
                   exact-request-keys)]
     (when-not (= allowed (set (keys request)))
       (fail! "WARM-REQUEST-SHAPE" "request keys are not closed"
              {:keys (vec (sort (map str (keys request))))}))
     (when-not (= (if (= :run-cached-shard operation)
                    cached-request-schema
                  (if (= :run-fast-artifact operation)
                    artifact-request-schema
                    request-schema))
                  (:schema request))
       (fail! "WARM-REQUEST-SCHEMA" "request schema is unsupported" {}))
     (when-not (and (string? (:request-id request))
                    (<= 1 (count (:request-id request)) 160))
       (fail! "WARM-REQUEST-ID" "request id must be bounded text" {}))
     (when-not (contains? #{:run-exact :run-cached-shard :run-fast-artifact :shutdown} operation)
       (fail! "WARM-REQUEST-OPERATION" "operation is unsupported" {}))
     (doseq [field [:candidate-commit :candidate-tree]]
       (when-not (= (field expected) (field request))
         (fail! "WARM-TREE-MISMATCH" "request identity differs from worker identity"
                {:field field :expected (field expected) :actual (field request)})))
     (cond
       (= :run-exact operation) (exact-runner-args! (:args request))
       (= :run-cached-shard operation)
       (let [plan (cached-plan-for-request request)]
         (when cached-plan-holder (reset! cached-plan-holder plan)))
       (= :run-fast-artifact operation)
       (let [artifact (artifact-plan-for-request request)]
         (when cached-plan-holder (reset! cached-plan-holder artifact))))
     request)))

(defn- bounded-process-output [process]
  (let [stream (.getInputStream process)
        output (ByteArrayOutputStream.)
        buffer (byte-array 4096)]
    (loop [total 0]
      (let [read (.read stream buffer)]
        (if (neg? read)
          (.toString output (.name StandardCharsets/UTF_8))
          (let [next-total (+ total read)]
            (when (> next-total maximum-output-length)
              (.destroyForcibly process)
              (fail! "WARM-GIT-BOUNDS" "git output exceeds bound" {}))
            (.write output buffer 0 read)
            (recur next-total)))))))

(defn- git-text [^Path root args]
  (let [builder (doto (ProcessBuilder. ^java.util.List (into ["git"] args))
                  (.directory (.toFile root))
                  (.redirectErrorStream true))
        env (.environment builder)]
    (.put env "GIT_OPTIONAL_LOCKS" "0")
    (let [process (.start builder)
          output-future (future (bounded-process-output process))
          exited? (.waitFor process 10 TimeUnit/SECONDS)]
      (when-not (and exited? (zero? (.exitValue process)))
        (.destroyForcibly process)
        (future-cancel output-future)
        (fail! "WARM-GIT" "git identity observation failed" {:args args}))
      (let [timeout (Object.)
            output (deref output-future 1000 timeout)]
        (when (identical? timeout output)
          (.destroyForcibly process)
          (future-cancel output-future)
          (fail! "WARM-GIT" "git output capture timed out" {:args args}))
        (str/trim output)))))

(defn observe-identity [^Path root]
  (let [commit (git-text root ["rev-parse" "--verify" "HEAD"])
        tree (git-text root ["rev-parse" "--verify" "HEAD^{tree}"])
        status (git-text root ["status" "--porcelain=v1" "--untracked-files=all" "--no-renames"])]
    (when-not (and (re-matches oid-pattern commit) (re-matches oid-pattern tree))
      (fail! "WARM-GIT" "git returned malformed identities" {}))
    {:candidate-commit commit :candidate-tree tree :clean? (str/blank? status)}))

(defn- response [request outcome exit-code elapsed-ns result request-count]
  (merge {:schema response-schema :authority :non-authoritative
          :authoritative? false :request-id (:request-id request)
          :candidate-commit (:candidate-commit request)
          :candidate-tree (:candidate-tree request)
          :outcome outcome :exit-code exit-code :elapsed-ns elapsed-ns
          :request-count request-count
          :runner-result (:runner-result result)}
         (let [{out-text :text out-cut :truncated?} (bounded-text (:out result))
               {err-text :text err-cut :truncated?} (bounded-text (:err result))]
           {:stdout out-text :stderr err-text
            :output-truncated? (boolean (or out-cut err-cut
                                            (:out-truncated? result)
                                            (:err-truncated? result)))})))

(defn- cached-response
  [request outcome exit-code elapsed-ns result request-count]
  (merge {:schema cached-response-schema
          :authority :non-authoritative
          :authoritative? false
          :request-id (:request-id request)
          :candidate-commit (:candidate-commit request)
          :candidate-tree (:candidate-tree request)
          :lane (:lane request)
          :route-id (:route-id request)
          :plan-id (:plan-id request)
          :catalog-id (:catalog-id request)
          :closure-identity (:closure-identity request)
          :result-contract-id (:result-contract-id request)
          :shard (:shard request)
          :outcome outcome
          :exit-code exit-code
          :elapsed-ns elapsed-ns
          :request-count request-count
          :runner-result (:runner-result result)}
         (let [{out-text :text out-cut :truncated?} (bounded-text (:out result))
               {err-text :text err-cut :truncated?} (bounded-text (:err result))]
           {:stdout out-text :stderr err-text
            :output-truncated? (boolean (or out-cut err-cut
                                            (:out-truncated? result)
                                            (:err-truncated? result)))})))

(defn- artifact-response
  [request outcome exit-code elapsed-ns result request-count]
  (merge {:schema artifact-response-schema
          :authority :non-authoritative
          :authoritative? false
          :request-id (:request-id request)
          :candidate-commit (:candidate-commit request)
          :candidate-tree (:candidate-tree request)
          :lane :fast
          :route-id (:route-id request)
          :plan-id (:plan-id request)
          :catalog-id (:catalog-id request)
          :closure-identity (:closure-identity request)
          :binding-id (:binding-id request)
          :row-id (:row-id request)
          :stage (:stage request)
          :outcome outcome
          :exit-code exit-code
          :elapsed-ns elapsed-ns
          :request-count request-count
          :projection (:projection result)}
         (let [{out-text :text out-cut :truncated?} (bounded-text (:out result))
               {err-text :text err-cut :truncated?} (bounded-text (:err result))]
           {:stdout out-text :stderr err-text
           :output-truncated? (boolean (or out-cut err-cut
                                            (:out-truncated? result)
                                            (:err-truncated? result)))})))

(defn- artifact-failure
  [error]
  {:exit 2 :elapsed-ns 0 :projection nil :out ""
   :err (str (or (:id (ex-data error)) "WARM-ARTIFACT-FAILED")
             ": " (or (.getMessage ^Throwable error) "artifact execution failed"))
   :out-truncated? false :err-truncated? false})

(defn- invoke-runner [args]
  (let [out-capture (bounded-writer) err-capture (bounded-writer)
        out (:writer out-capture) err (:writer err-capture)
        result-called? (atom false)
        machine-result (atom nil)
        started (System/nanoTime)]
    (try
      (let [exit (binding [*out* out *err* err test/*test-out* out
                           runner/*machine-result-sink*
                           (fn [result]
                             (when-not (compare-and-set! result-called? false true)
                               (fail! "WARM-RUNNER-RESULT"
                                     "runner emitted more than one machine result"
                                     {}))
                             (reset! machine-result result))]
                   (runner/run-cli! args))]
        (when (and (contains? #{0 1} exit) (not @result-called?))
          (fail! "WARM-RUNNER-RESULT"
                 "runner completed without one machine result" {}))
        (let [out-result (bounded-writer-result out-capture)
              err-result (bounded-writer-result err-capture)]
          {:exit exit :elapsed-ns (- (System/nanoTime) started)
           :out (:text out-result) :err (:text err-result)
           :runner-result @machine-result
           :out-truncated? (:truncated? out-result)
           :err-truncated? (:truncated? err-result)}))
      (catch Throwable error
        (cond
          (instance? InterruptedException error)
          (do (.interrupt (Thread/currentThread)) (throw error))

          (fatal-cause error)
          (throw (fatal-cause error))

          @result-called?
          ;; A result followed by an exception is not an honest completion.
          (throw error)

          :else
          (let [out-result (bounded-writer-result out-capture)
                err-result (bounded-writer-result err-capture)]
            {:exit 2 :elapsed-ns (- (System/nanoTime) started)
             :out (:text out-result)
             :err (str (:text err-result) (.getName (class error)) ": " (.getMessage error))
             :runner-result nil
             :out-truncated? (:truncated? out-result)
             :err-truncated? (:truncated? err-result)}))))))

(defn- invoke-cached-runner
  [shard]
  (let [out-capture (bounded-writer)
        err-capture (bounded-writer)
        out (:writer out-capture)
        err (:writer err-capture)
        started (System/nanoTime)]
    (try
      (let [run-shard (requiring-resolve
                       'gravity.self-hosting.sh07-cached-shard-runner/run-shard)
            raw-result (binding [*out* out *err* err test/*test-out* out]
                         (run-shard shard))]
        (validate-cached-result! shard raw-result)
        (let [out-result (bounded-writer-result out-capture)
              err-result (bounded-writer-result err-capture)]
          (when (or (:truncated? out-result) (:truncated? err-result))
            (fail! "WARM-CACHED-OUTPUT"
                   "cached runner output exceeds the bounded diagnostic limit"
                   {:shard shard
                    :stdout-truncated? (:truncated? out-result)
                    :stderr-truncated? (:truncated? err-result)}))
          (let [exit (if (and (zero? (:fail raw-result))
                              (zero? (:error raw-result)))
                       0
                       1)]
            {:exit exit
             :elapsed-ns (- (System/nanoTime) started)
             :out (:text out-result)
             :err (:text err-result)
             :runner-result raw-result
             :out-truncated? (:truncated? out-result)
             :err-truncated? (:truncated? err-result)})))
      (catch Throwable error
        (rethrow-control! error)
        (cond
          :else
          (let [out-result (bounded-writer-result out-capture)
                err-result (bounded-writer-result err-capture)
                diagnostic-id (:id (ex-data error))]
            {:exit 2
             :elapsed-ns (- (System/nanoTime) started)
             :out (:text out-result)
             :err (str (:text err-result)
                       (when diagnostic-id (str diagnostic-id ": "))
                       (.getName (class error)) ": " (.getMessage error))
             :runner-result nil
             :out-truncated? (:truncated? out-result)
             :err-truncated? (:truncated? err-result)}))))))

(defn- cached-plan-identity
  [plan]
  (select-keys plan [:route-id :lane :plan-id :catalog-id :closure-identity
                     :executor]))

(defn- cached-plan-after-execution
  [request before]
  (try
    (let [after (cached-plan-for-request request)]
      (if (= (cached-plan-identity before)
             (cached-plan-identity after))
        {:plan after}
        {:drift? true}))
    (catch clojure.lang.ExceptionInfo error
      (rethrow-control! error)
      {:drift? true :error error})
    (catch Throwable error
      (rethrow-control! error)
      {:drift? true :error error})))

(defn- cached-catalog-drift-result
  [result]
  {:exit 2
   :elapsed-ns (:elapsed-ns result)
   :out ""
   :err "WARM-CACHED-CATALOG: cached route plan or catalog identity changed during execution"
   :runner-result nil
   :out-truncated? false
   :err-truncated? false})

(defn run-session!
  ([lines emit! expected observe! execute! max-count]
   (run-session! lines emit! expected observe! execute!
                 invoke-cached-runner max-count))
  ([lines emit! expected observe! execute! cached-execute! max-count]
   (loop [remaining (seq lines) request-count 0]
     (if-let [line (first remaining)]
       (do
         (when (> (count line) maximum-line-length)
           (fail! "WARM-REQUEST-BOUNDS" "request line exceeds bound" {}))
         (let [cached-plan-holder (atom nil)
               request (validate-request! (strict-edn line) expected
                                          cached-plan-holder)
               cached? (= :run-cached-shard (:operation request))
               artifact? (= :run-fast-artifact (:operation request))
               cached-plan-before (when cached? @cached-plan-holder)
               artifact-plan-before (when artifact? @cached-plan-holder)
               live-before (observe!)]
           (when-not (= namespace-catalog-snapshot runner/namespace-catalog)
             (fail! "WARM-CATALOG-DRIFT" "development namespace catalog changed" {}))
           (when-not (= (assoc expected :clean? true) live-before)
             (fail! "WARM-TREE-DRIFT" "worker checkout changed after startup"
                    {:expected expected :actual live-before}))
           (if (= :shutdown (:operation request))
             (do (emit! (response request :shutdown 0 0 {:out "" :err ""
                                                         :runner-result nil}
                                  request-count)) 0)
             (let [result (cond
                            cached? (cached-execute! (:shard request))
                            artifact? (try
                                        (invoke-artifact-runner
                                         request (:binding artifact-plan-before))
                                        (catch Throwable error
                                          (rethrow-control! error)
                                          (artifact-failure error)))
                            :else (execute! (:args request)))
                   cached-plan-check (when cached?
                                       (cached-plan-after-execution
                                        request cached-plan-before))
                   cached-drift? (and cached? (:drift? cached-plan-check))
                   result (if cached-drift?
                            (cached-catalog-drift-result result)
                            result)
                   next-count (inc request-count)
                   live-after (observe!)
                   catalog-drift?
                   (not= namespace-catalog-snapshot runner/namespace-catalog)
                   drift? (or catalog-drift?
                              (not= (assoc expected :clean? true) live-after))
                   artifact-drift? (when artifact?
                                     (try
                                       (not= artifact-plan-before
                                             (artifact-plan-for-request request))
                                       (catch Throwable error
                                         (rethrow-control! error)
                                         true)))
                   result (if artifact-drift?
                            (assoc result :projection nil :exit 2)
                            result)
                   exit (if (or cached-drift? artifact-drift? drift?) 2 (:exit result))
                   outcome (cond cached-drift? :catalog-drift
                                 artifact-drift? :catalog-drift
                                 catalog-drift? :catalog-drift
                                 drift? :tree-drift
                                 (zero? exit) :passed
                                 :else :failed)]
               (emit! (cond
                        cached? (cached-response request outcome exit (:elapsed-ns result)
                                                 result next-count)
                        artifact? (artifact-response request outcome exit (:elapsed-ns result)
                                                     result next-count)
                        :else (response request outcome exit (:elapsed-ns result)
                                        result next-count)))
               (cond
                 (not (zero? exit)) exit
                 (< next-count max-count) (recur (next remaining) next-count)
                 :else 0)))))
       0))))

(defn- read-bounded-line [^BufferedReader reader]
  (let [line (StringBuilder.)]
    (loop []
      (let [character (.read reader)]
        (cond
          (neg? character) (when (pos? (.length line)) (.toString line))
          (= character (int \newline)) (.toString line)
          :else (do
                  (when (>= (.length line) maximum-line-length)
                    (fail! "WARM-REQUEST-BOUNDS" "request line exceeds bound" {}))
                  (when-not (= character (int \return))
                    (.append line (char character)))
                  (recur)))))))

(defn- bounded-lines [^BufferedReader reader]
  (lazy-seq
   (when-let [line (read-bounded-line reader)]
     (cons line (bounded-lines reader)))))

(defn- parse-cli [args]
  (loop [remaining (seq args) result {:max-requests 64}]
    (if-let [option (first remaining)]
      (let [value (second remaining)]
        (when-not value (fail! "WARM-CLI" "option requires a value" {:option option}))
        (case option
          "--expected-commit" (recur (nnext remaining) (assoc result :candidate-commit value))
          "--expected-tree" (recur (nnext remaining) (assoc result :candidate-tree value))
          "--max-requests" (recur (nnext remaining) (assoc result :max-requests (parse-long value)))
          (fail! "WARM-CLI" "unknown option" {:option option})))
      result)))

(defn -main [& args]
  (try
    (let [{:keys [candidate-commit candidate-tree max-requests] :as expected} (parse-cli args)
          expected (select-keys expected [:candidate-commit :candidate-tree])
          root (.toRealPath (.toAbsolutePath (Paths/get (System/getProperty "user.dir")
                                                       (make-array String 0)))
                            (make-array LinkOption 0))
          observed (observe-identity root)]
      (when-not (and (re-matches oid-pattern (or candidate-commit ""))
                     (re-matches oid-pattern (or candidate-tree ""))
                     (integer? max-requests) (<= 1 max-requests maximum-requests)
                     (= (assoc expected :clean? true) observed))
        (fail! "WARM-STARTUP-IDENTITY" "startup identities are not exact and clean"
               {:expected expected :observed observed}))
      (println (str response-prefix (pr-str {:schema ready-schema :authority :non-authoritative
                                             :candidate-commit candidate-commit
                                             :candidate-tree candidate-tree})))
      (flush)
      (let [exit (with-open [reader (BufferedReader. (InputStreamReader. System/in StandardCharsets/UTF_8))]
                   (run-session! (bounded-lines reader)
                                 #(do (println (str response-prefix (pr-str %))) (flush))
                                 expected #(observe-identity root) invoke-runner
                                 invoke-cached-runner max-requests))]
        (shutdown-agents)
        (when-not (zero? exit) (System/exit exit))))
    (catch Throwable error
      (cond
        (instance? InterruptedException error)
        (do (.interrupt (Thread/currentThread)) (throw error))

        (fatal-cause error)
        (throw (fatal-cause error))

        :else
        (do
          (binding [*out* *err*]
            (println (pr-str {:schema response-schema :outcome :rejected
                              :id (or (:id (ex-data error)) "WARM-UNEXPECTED")
                              :message (.getMessage error)})))
          (System/exit 2))))))
