(ns gravity.self-hosting.sh01-development-test-cache
  "Bounded persistent reuse for non-authoritative development test results.

  This leaf does not discover dependencies or grant test, proof, benchmark,
  release, or self-hosting authority. Callers must supply a complete identity
  closure; incomplete or ineligible requests always execute fresh."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths
            StandardCopyOption StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.util UUID]))

(def ^:private cache-schema :gravity/development-test-cache-v1)
(def ^:private receipt-schema :gravity/development-test-cache-receipt-v1)
(def ^:private cache-file-name "results-v1.edn")
(def ^:private maximum-input-count 4096)
(def ^:private maximum-entry-count 1024)
(def ^:private maximum-result-bytes (* 1024 1024))
(def ^:private maximum-cache-bytes (* 8 1024 1024))
(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")
(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private no-file-attributes (make-array FileAttribute 0))

(def ^:private dependency-fields
  #{:complete? :production-inputs :transitive-production-inputs
    :fixture-contract-inputs :runner-identity :runtime-tool-inputs})

(def ^:private policy-fields
  #{:authority :deterministic? :performance? :proof?
    :freshness-required? :timeout-ms})

(defn- utf8-bytes [value]
  (.getBytes ^String value StandardCharsets/UTF_8))

(defn- sha256 [value]
  (str "sha256:" (digest/sha256-hex value)))

(defn- canonical-form
  [value]
  (cond
    (map? value)
    [:map (->> value
               (map (fn [[key item]]
                      [(canonical-form key) (canonical-form item)]))
               (sort-by pr-str)
               vec)]

    (set? value)
    [:set (->> value (map canonical-form) (sort-by pr-str) vec)]

    (vector? value) [:vector (mapv canonical-form value)]
    (list? value) [:list (mapv canonical-form value)]
    (seq? value) [:seq (mapv canonical-form value)]
    (nil? value) [:nil]
    (boolean? value) [:boolean value]
    (string? value) [:string value]
    (keyword? value) [:keyword (str value)]
    (symbol? value) [:symbol (str value)]
    (integer? value) [:integer (str value)]
    :else
    (throw
     (ex-info "Development test cache identity is not canonical EDN"
              {:id "DEV-TEST-CACHE-IDENTITY"
               :value-class (some-> value class .getName)}))))

(defn- canonical-text [value]
  (pr-str (canonical-form value)))

(defn- identity-value? [value]
  (or (and (string? value) (not (str/blank? value)))
      (keyword? value)
      (symbol? value)))

(defn- input-identity? [value]
  (and (map? value)
       (= #{:id :sha256} (set (keys value)))
       (identity-value? (:id value))
       (string? (:sha256 value))
       (boolean (re-matches sha256-pattern (:sha256 value)))))

(defn- input-vector? [value]
  (and (vector? value)
       (<= (count value) maximum-input-count)
       (every? input-identity? value)
       (= (count value) (count (distinct (map :id value))))))

(defn- complete-dependencies? [dependencies]
  (and (map? dependencies)
       (= dependency-fields (set (keys dependencies)))
       (true? (:complete? dependencies))
       (input-vector? (:production-inputs dependencies))
       (input-vector? (:transitive-production-inputs dependencies))
       (input-vector? (:fixture-contract-inputs dependencies))
       (input-identity? (:runner-identity dependencies))
       (input-vector? (:runtime-tool-inputs dependencies))))

(defn- complete-policy? [policy]
  (and (map? policy)
       (= policy-fields (set (keys policy)))
       (contains? #{:non-authoritative :authoritative} (:authority policy))
       (every? boolean?
               ((juxt :deterministic? :performance? :proof?
                      :freshness-required?) policy))
       (integer? (:timeout-ms policy))
       (pos? (:timeout-ms policy))))

(defn- request-eligibility [request]
  (cond
    (not (input-identity? (:test-identity request)))
    {:cacheable? false :reason :incomplete-test-identity}

    (not (complete-policy? (:test-policy request)))
    {:cacheable? false :reason :incomplete-test-policy}

    (not= :non-authoritative (get-in request [:test-policy :authority]))
    {:cacheable? false :reason :authoritative-test}

    (not (true? (get-in request [:test-policy :deterministic?])))
    {:cacheable? false :reason :nondeterministic-test}

    (true? (get-in request [:test-policy :performance?]))
    {:cacheable? false :reason :performance-test}

    (true? (get-in request [:test-policy :freshness-required?]))
    {:cacheable? false
     :reason (if (true? (get-in request [:test-policy :proof?]))
               :freshness-required-proof-test
               :freshness-required-test)}

    (not (complete-dependencies? (:dependencies request)))
    {:cacheable? false :reason :incomplete-dependencies}

    :else {:cacheable? true}))

(defn- key-material [request]
  {:test-identity (:test-identity request)
   :test-policy (:test-policy request)
   :production-inputs
   (vec (sort-by (comp canonical-text :id)
                 (get-in request [:dependencies :production-inputs])))
   :transitive-production-inputs
   (vec (sort-by
         (comp canonical-text :id)
         (get-in request [:dependencies :transitive-production-inputs])))
   :fixture-contract-inputs
   (vec (sort-by
         (comp canonical-text :id)
         (get-in request [:dependencies :fixture-contract-inputs])))
   :runner-identity (get-in request [:dependencies :runner-identity])
   :runtime-tool-inputs
   (vec (sort-by (comp canonical-text :id)
                 (get-in request [:dependencies :runtime-tool-inputs])))})

(defn cache-key
  "Returns the content key for a complete, reusable development-test request."
  [request]
  (let [{:keys [cacheable? reason]} (request-eligibility request)]
    (when-not cacheable?
      (throw
       (ex-info "Development test request is not cacheable"
                {:id "DEV-TEST-CACHE-INELIGIBLE" :reason reason})))
    (sha256 (canonical-text (key-material request)))))

(defn- reusable-result? [result]
  (and (map? result)
       (= :passed (:status result))
       (integer? (:exit-code result))
       (zero? (:exit-code result))
       (= :non-authoritative (:authority result))
       (false? (:authoritative? result))
       (false? (:timed-out? result))
       (not (true? (:nondeterministic? result)))
       (not (true? (:performance? result)))
       (not (true? (:freshness-required? result)))))

(defn- as-path [value]
  (cond
    (instance? Path value) value
    (string? value) (Paths/get value (make-array String 0))
    :else nil))

(defn- empty-cache []
  {:schema cache-schema :next-sequence 0 :entries []})

(defn- valid-cache? [cache]
  (and (map? cache)
       (= cache-schema (:schema cache))
       (integer? (:next-sequence cache))
       (not (neg? (:next-sequence cache)))
       (vector? (:entries cache))
       (<= (count (:entries cache)) maximum-entry-count)
       (= (count (:entries cache))
          (count (distinct (map :cache-key (:entries cache)))))
       (= (count (:entries cache))
          (count (distinct (map :sequence (:entries cache)))))
       (every? #(< % (:next-sequence cache))
               (map :sequence (:entries cache)))
       (every?
        (fn [entry]
          (and (map? entry)
               (= #{:cache-key :test-identity :key-material :result :sequence}
                  (set (keys entry)))
               (string? (:cache-key entry))
               (boolean (re-matches sha256-pattern (:cache-key entry)))
               (input-identity? (:test-identity entry))
               (= (:test-identity entry)
                  (get-in entry [:key-material :test-identity]))
               (integer? (:sequence entry))
               (not (neg? (:sequence entry)))
               (= (:cache-key entry)
                  (sha256 (canonical-text (:key-material entry))))
               (reusable-result? (:result entry))))
        (:entries cache))))

(defn- read-cache [^Path cache-file]
  (cond
    (not (Files/exists cache-file no-links)) {:cache (empty-cache)}
    (or (Files/isSymbolicLink cache-file)
        (not (Files/isRegularFile cache-file no-links))
        (> (Files/size cache-file) maximum-cache-bytes))
    {:corrupt? true :reason :invalid-cache-file}

    :else
    (try
      (let [raw (String. (Files/readAllBytes cache-file)
                         StandardCharsets/UTF_8)
            value (edn/read-string {:readers {} :default (fn [& _] ::tagged)} raw)]
        (if (and (not= ::tagged value) (valid-cache? value))
          {:cache value}
          {:corrupt? true :reason :invalid-cache-content}))
      (catch Exception _
        {:corrupt? true :reason :invalid-cache-content}))))

(defn- serialized-cache [cache]
  (str (pr-str cache) "\n"))

(defn- cache-fits? [cache]
  (<= (alength (utf8-bytes (serialized-cache cache))) maximum-cache-bytes))

(defn- fit-cache [cache maximum-entries]
  (loop [candidate cache
         evicted []]
    (if (and (<= (count (:entries candidate)) maximum-entries)
             (cache-fits? candidate))
      {:cache candidate :evicted-cache-keys evicted}
      (if-let [oldest (first (sort-by :sequence (:entries candidate)))]
        (recur (update candidate :entries
                       #(vec (remove (fn [entry]
                                       (= (:cache-key oldest)
                                          (:cache-key entry))) %)))
               (conj evicted (:cache-key oldest)))
        {:cache candidate :evicted-cache-keys evicted}))))

(defn- write-cache! [^Path directory ^Path cache-file cache]
  (Files/createDirectories directory no-file-attributes)
  (when (Files/isSymbolicLink directory)
    (throw (ex-info "Development test cache directory is a symbolic link"
                    {:id "DEV-TEST-CACHE-PATH"})))
  (let [temporary (.resolve directory
                            (str ".results-v1-" (UUID/randomUUID) ".tmp"))]
    (try
      (Files/write temporary (utf8-bytes (serialized-cache cache))
                   (into-array OpenOption
                               [StandardOpenOption/CREATE_NEW
                                StandardOpenOption/WRITE]))
      (try
        (Files/move temporary cache-file
                    (into-array java.nio.file.CopyOption
                                [StandardCopyOption/ATOMIC_MOVE
                                 StandardCopyOption/REPLACE_EXISTING]))
        (catch java.nio.file.AtomicMoveNotSupportedException _
          (Files/move temporary cache-file
                      (into-array java.nio.file.CopyOption
                                  [StandardCopyOption/REPLACE_EXISTING]))))
      (finally
        (Files/deleteIfExists temporary)))))

(defn- receipt [request decision reason cache-key details]
  (merge
   {:artifact receipt-schema
    :decision decision
    :reason reason
    :test-identity (:test-identity request)
    :cache-key cache-key
    :authority :non-authoritative
    :authoritative? false
    :cache-authoritative? false
    :fresh-authoritative-run-required? true
    :release-authority? false
    :proof-authority? false
    :self-hosting-authority? false}
   details))

(defn- fresh-result [request decision reason key operation details]
  (let [result (operation)]
    {:result result
     :receipt (receipt request decision reason key
                       (assoc details :stored? false))}))

(defn lookup-or-run!
  "Returns {:result ... :receipt ...}, reusing only eligible successful results.

  Required request fields are :cache-directory, :maximum-entries,
  :test-identity, :test-policy, and :dependencies. The dependency map must
  explicitly bind declared and transitive production inputs,
  fixture/contract inputs, runner identity, and runtime/tool inputs."
  [request operation]
  (when-not (ifn? operation)
    (throw (ex-info "Development test operation must be callable"
                    {:id "DEV-TEST-CACHE-OPERATION"})))
  (let [maximum-entries (:maximum-entries request)
        directory (as-path (:cache-directory request))]
    (when-not (and directory
                   (integer? maximum-entries)
                   (pos? maximum-entries)
                   (<= maximum-entries maximum-entry-count))
      (throw
       (ex-info "Development test cache bounds or directory are invalid"
                {:id "DEV-TEST-CACHE-CONFIGURATION"
                 :maximum-entries maximum-entries})))
    (let [{:keys [cacheable? reason]} (request-eligibility request)]
      (if-not cacheable?
        (fresh-result request :miss reason nil operation
                      {:cacheable? false})
        (let [key (cache-key request)
              cache-file (.resolve ^Path directory cache-file-name)
              loaded (if (Files/isSymbolicLink directory)
                       {:corrupt? true :reason :invalid-cache-directory}
                       (read-cache cache-file))]
          (if (:corrupt? loaded)
            (fresh-result request :invalidation (:reason loaded) key operation
                          {:cacheable? false})
            (let [cache (:cache loaded)
                  matching (some #(when (= key (:cache-key %)) %)
                                 (:entries cache))]
              (if matching
                {:result (:result matching)
                 :receipt (receipt request :hit :matching-input-closure key
                                   {:stored? false :cacheable? true})}
                (let [stale (filterv #(= (:test-identity request)
                                         (:test-identity %))
                                     (:entries cache))
                      result (operation)
                      reusable? (reusable-result? result)
                      canonical-result
                      (try (canonical-text result)
                           (catch clojure.lang.ExceptionInfo _ nil))
                      result-bytes
                      (when canonical-result
                        (alength (utf8-bytes canonical-result)))
                      storable? (and reusable?
                                     result-bytes
                                     (<= result-bytes maximum-result-bytes))
                      decision (if (seq stale) :invalidation :miss)
                      reason (cond
                               (seq stale) :input-closure-changed
                               :else :not-found)]
                  (if-not storable?
                    {:result result
                     :receipt
                     (receipt request decision
                              (cond
                                (not reusable?) :result-not-reusable
                                (nil? result-bytes) :result-not-persistable
                                :else :result-size-bound)
                              key
                              {:stored? false
                               :cacheable? true
                               :invalidated-cache-keys
                               (mapv :cache-key stale)})}
                    (let [without-stale
                          (update cache :entries
                                  #(vec (remove (fn [entry]
                                                  (= (:test-identity request)
                                                     (:test-identity entry))) %)))
                          sequence (:next-sequence without-stale)
                          entry {:cache-key key
                                 :test-identity (:test-identity request)
                                 :key-material (key-material request)
                                 :result result
                                 :sequence sequence}
                          candidate (-> without-stale
                                        (update :next-sequence inc)
                                        (update :entries conj entry))
                          fitted (fit-cache candidate maximum-entries)
                          retained? (some #(= key (:cache-key %))
                                          (get-in fitted [:cache :entries]))]
                      (when retained?
                        (write-cache! directory cache-file (:cache fitted)))
                      {:result result
                       :receipt
                       (receipt request decision reason key
                                {:stored? (boolean retained?)
                                 :cacheable? true
                                 :invalidated-cache-keys
                                 (mapv :cache-key stale)
                                 :evicted-cache-keys
                                 (:evicted-cache-keys fitted)})})))))))))))
