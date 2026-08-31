(ns gravity.self-hosting.sh07-semantic-carrier
  "Dev-only SH-07 semantic run-product carrier.

  This namespace deliberately owns no cache, filesystem routing, or ordinary
  SH-07 execution.  It only turns an already successful product into a closed,
  path-neutral carrier and binds that carrier to a later supplied request."
  (:require [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(def path-slot-sentinel :gravity/sh07-semantic-carrier-path-slot-v2)
(def ^:private maximum-canonical-bytes (* 256 1024 1024))

(def path-slot-addresses
  [[:raw-template-result :core-template :provenance]
   [:raw-template-result :core-template :provenance-binding-preimage
    :actual-source-path]
   [:raw-template-result :core-template :lineage
    :authenticated-sh06-artifact-id]
   [:raw-template-result :digest-requests :artifact-id-ordinal :preimage
    :lineage :authenticated-sh06-artifact-id]
   [:raw-template-result :digest-requests :provenance-ordinal :preimage
    :actual-source-path]
   [:digest-requests :artifact-id-ordinal :preimage :lineage
    :authenticated-sh06-artifact-id]
   [:digest-requests :provenance-ordinal :preimage :actual-source-path]
   [:resolved-digests :provenance-ordinal]
   [:canonical-core-artifact :provenance]
   [:canonical-core-artifact :provenance-binding-preimage
    :actual-source-path]
   [:canonical-core-artifact :provenance-binding-id]])

(def ^:private run-product-keys
  #{:raw-template-result :canonical-core-artifact :digest-requests
    :resolved-digests :template-verification :resolved-verification})

(def ^:private snapshot-required-keys
  #{:source-path :source-bytes :source-byte-count :source-bytes-sha256
    :source-extension :source-kind :file-key :last-modified-millis
    :sh06-semantic-projection-id})

(def ^:private semantic-key-keys
  #{:source-byte-count :source-bytes-sha256 :source-extension :source-kind
    :sh06-semantic-projection-id :pins})

(def ^:private static-envelope-keys
  #{:scope :adapter :task :module-policy :source-revision-id
    :sh06-semantic-projection-id :authenticated-request-path-neutral})

(def ^:private module-policy-keys
  #{:namespace :profile :target :safety :effects :capabilities :exports})

(def ^:private expected-nonclaims
  {:authoritative? false :fresh-authoritative-run-required? true
   :cross-process? false :cross-stage? false :final-artifact-cached? false
   :verification-report-cached? false})

(def ^:private raw-template-result-keys
  #{:artifact :schema-version :status :core-template :digest-requests
    :diagnostics :bounds :containment :execution-boundary})

(def ^:private template-verification-keys
  #{:artifact :schema-version :status :rule :reason :details})

(def ^:private resolved-verification-keys
  #{:artifact :schema-version :status :rule})

(def ^:private sh07-boundary-keys
  #{:slice :owner :adapter-contract :plan-binding
    :authenticated-sh06-resolution-artifact :authenticated-core-request
    :raw-template-result :canonical-core-artifact :digest-requests
    :resolved-digests :template-verification :resolved-verification
    :authenticated-envelope-descriptor :authenticated-envelope
    :target-source-reread? :clojure-adapter-residual? :self-hosted?})

(def ^:private carrier-keys
  #{:artifact :schema-version :status :authority :semantic-key
    :path-slot-addresses :shape-manifest :static-envelope :run-product
    :integrity-hash :canonical-byte-count :nonclaims})

(def ^:private required-purposes
  {:artifact-id-ordinal :sh07-core-artifact-id
   :provenance-ordinal :sh07-core-provenance-binding-id})

(def ^:private pin-spec
  {:sh03-c2-reader
   '[sh03-reader-expected-source-byte-count
     sh03-reader-expected-source-content-hash
     sh03-reader-expected-plan-semantic-hash
     sh03-reader-expected-functions-semantic-hash
     sh03-reader-expected-function-count
     sh03-reader-expected-function-names-hash
     sh03-reader-expected-function-shapes-hash
     sh03-reader-expected-entrypoint-semantic-hash
     sh03-reader-expected-verifier-semantic-hash
     sh03-reader-expected-builtin-functions-hash]
   :sh04-c3-syntax
   '[sh04-syntax-expected-source-byte-count
     sh04-syntax-expected-source-content-hash
     sh04-syntax-expected-plan-semantic-hash
     sh04-syntax-expected-functions-semantic-hash
     sh04-syntax-expected-function-count
     sh04-syntax-expected-function-names-hash
     sh04-syntax-expected-function-shapes-hash
     sh04-syntax-public-function-hashes
     sh04-syntax-public-function-shapes
     sh04-syntax-adapter-contract]
   :sh05-macro
   '[sh05-macro-expected-source-byte-count
     sh05-macro-expected-source-content-hash
     sh05-macro-expected-plan-semantic-hash
     sh05-macro-expected-functions-semantic-hash
     sh05-macro-expected-function-count
     sh05-macro-expected-function-names-hash
     sh05-macro-expected-function-shapes-hash
     sh05-macro-public-function-hashes
     sh05-macro-public-function-shapes
     sh05-macro-adapter-contract]
   :sh06-resolution
   '[sh06-resolution-expected-source-byte-count
     sh06-resolution-expected-source-content-hash
     sh06-resolution-expected-plan-semantic-hash
     sh06-resolution-expected-functions-semantic-hash
     sh06-resolution-expected-function-count
     sh06-resolution-expected-function-names-hash
     sh06-resolution-expected-function-shapes-hash
     sh06-resolution-public-function-hashes
     sh06-resolution-public-function-shapes
     sh06-resolution-adapter-contract
     sh06-resolution-pass-contract
     sh06-resolution-execution-boundary-contract]
   :sh07-core
   '[sh07-core-expected-source-byte-count
     sh07-core-expected-source-content-hash
     sh07-core-expected-plan-semantic-hash
     sh07-core-expected-functions-semantic-hash
     sh07-core-expected-function-count
     sh07-core-expected-function-names-hash
     sh07-core-expected-function-shapes-hash
     sh07-core-public-function-hashes
     sh07-core-public-function-shapes
     sh07-core-adapter-contract]})

(defn- diagnostic! [reason source-path]
  (throw (ex-info (str "SH07 semantic carrier rejected: " (name reason))
                  {:artifact :gravity/sh07-semantic-cache-diagnostic
                   :rule "SH07-SEMANTIC-CACHE"
                   :severity :error
                   :stage :development-cache
                   :reason reason
                   :source-path source-path
                   :fail-closed true
                   :fresh-authoritative-run-required? true})))

(defn- sha256 [^bytes bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest bytes)
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff)) (.digest digest))))))

(defn- canonical-bytes [value]
  (.getBytes (binding [*print-length* nil *print-level* nil *print-meta* false]
               (pr-str (bootstrap/reader-canonical-value value)))
             StandardCharsets/UTF_8))

(defn carrier-byte-count [carrier]
  (alength (canonical-bytes (dissoc carrier :integrity-hash))))

(defn- canonical-token [value]
  (String. (canonical-bytes value) StandardCharsets/UTF_8))

(defn- live-var-value [symbol]
  (let [resolved (ns-resolve 'gravity.bootstrap symbol)]
    (when-not resolved (diagnostic! :stale-pin nil))
    (let [value @resolved]
      (when (nil? value) (diagnostic! :stale-pin nil))
      value)))

(declare closed-value?)

(defn live-pins []
  (let [groups (into {}
                     (for [[group symbols] (dissoc pin-spec :request-schema-version)]
                       [group (mapv live-var-value symbols)]))]
    (let [pins (assoc groups
                      :request-schema-version 15
                      :runtime
                      {:java-runtime-version (System/getProperty "java.runtime.version")
                       :clojure-version (clojure-version)})]
      (when-not (closed-value? pins) (diagnostic! :stale-pin nil))
      pins)))

(defn semantic-key
  "Returns the path-free identity projection for a bounded source snapshot.
  The snapshot remains caller-owned; only byte-derived identity is retained."
  [snapshot]
  (let [path (:source-path snapshot)
        bytes (:source-bytes snapshot)
        byte-count (:source-byte-count snapshot)
        source-hash (:source-bytes-sha256 snapshot)
        extension (:source-extension snapshot)
        source-kind (:source-kind snapshot)
        projection-id (:sh06-semantic-projection-id snapshot)]
    (when-not (and (map? snapshot)
                   (= snapshot-required-keys (set (keys snapshot)))
                   (string? path) (not (empty? path))
                   (instance? (Class/forName "[B") bytes)
                   (integer? byte-count) (not (neg? byte-count))
                   (= byte-count (alength ^bytes bytes))
                   (= source-hash (sha256 bytes))
                   (#{".gravity" ".qst"} extension)
                   (= :gravity/source source-kind)
                   (string? (:file-key snapshot))
                   (integer? (:last-modified-millis snapshot))
                   (not (neg? (:last-modified-millis snapshot)))
                   (string? projection-id))
      (diagnostic! :invalid-selection path))
    {:source-byte-count byte-count
     :source-bytes-sha256 source-hash
     :source-extension extension
     :source-kind source-kind
     :sh06-semantic-projection-id projection-id
     :pins (live-pins)}))

(defn- closed-value? [value]
  (cond
    (nil? value) true
    (or (boolean? value) (integer? value) (string? value)
        (keyword? value) (symbol? value)) (nil? (meta value))
    (vector? value) (and (nil? (meta value)) (every? closed-value? value))
    (set? value) (and (nil? (meta value)) (every? closed-value? value))
    (map? value) (and (not (record? value)) (nil? (meta value))
                      (every? (fn [[key item]]
                                (and (closed-value? key) (closed-value? item))) value))
    :else false))

(defn- descriptor [value]
  (cond
    (nil? value) [:nil]
    (boolean? value) [:boolean]
    (integer? value) [:integer]
    (string? value) [:string (alength (.getBytes ^String value StandardCharsets/UTF_8))]
    (keyword? value) [:keyword]
    (symbol? value) [:symbol]
    (map? value) [:map (count value) (->> (keys value) (map canonical-token) sort vec)]
    (vector? value) [:vector (count value)]
    (set? value) [:set (count value) (->> value (map canonical-token) sort vec)]))

(defn- shape-manifest [value]
  (when-not (closed-value? value) (diagnostic! :carrier-schema nil))
  (loop [work (list [[] value]) manifest []]
    (if-let [[coordinate current] (first work)]
      (let [children
            (cond
              (map? current) (->> current
                                  (sort-by (comp canonical-token key))
                                  (map (fn [[key item]]
                                         [(conj coordinate [:map-key (canonical-token key)]) item])))
              (vector? current) (map-indexed (fn [index item]
                                                [(conj coordinate [:vector-index index]) item]) current)
              (set? current) (->> current (sort-by canonical-token)
                                  (map (fn [item]
                                         [(conj coordinate [:set-member (canonical-token item)]) item])))
              :else [])]
        (recur (concat (rest work) children)
               (conj manifest [coordinate (descriptor current)])))
      (->> manifest (sort-by (comp canonical-token first)) vec))))

(defn- purpose-indexes [requests]
  (when-not (vector? requests) (diagnostic! :digest-purpose-mismatch nil))
  (let [indexes (reduce-kv (fn [result index request]
                             (update result (:purpose request) (fnil conj []) index)) {} requests)]
    (when-not (and (every? #(= 1 (count (get indexes %))) (vals required-purposes))
                   (= (vec (range (count requests))) (mapv :ordinal requests)))
      (diagnostic! :digest-purpose-mismatch nil))
    (into {} (map (fn [[token purpose]] [token (first (get indexes purpose))]) required-purposes))))

(defn- resolved-address [product address]
  (let [indexes (purpose-indexes (:digest-requests product))]
    (mapv #(get indexes % %) address)))

(defn- get-address [product address]
  (let [address (resolved-address product address)
        marker (Object.) value (get-in product address marker)]
    (when (identical? marker value) (diagnostic! :path-slot-mismatch nil))
    value))

(defn- assoc-address [product address value]
  (assoc-in product (resolved-address product address) value))

(declare request-path request-sh06-id)

(defn- expected-slot-value [product request address]
  (let [path (request-path request)
        sh06-id (request-sh06-id request)
        provenance-index (:provenance-ordinal
                          (purpose-indexes (:digest-requests product)))
        provenance-digest (get-in product [:resolved-digests provenance-index])]
    (case address
      [:raw-template-result :core-template :provenance] (:provenance request)
      [:raw-template-result :core-template :provenance-binding-preimage :actual-source-path] path
      [:raw-template-result :core-template :lineage :authenticated-sh06-artifact-id] sh06-id
      [:raw-template-result :digest-requests :artifact-id-ordinal :preimage :lineage :authenticated-sh06-artifact-id] sh06-id
      [:raw-template-result :digest-requests :provenance-ordinal :preimage :actual-source-path] path
      [:digest-requests :artifact-id-ordinal :preimage :lineage :authenticated-sh06-artifact-id] sh06-id
      [:digest-requests :provenance-ordinal :preimage :actual-source-path] path
      [:resolved-digests :provenance-ordinal] provenance-digest
      [:canonical-core-artifact :provenance] (:provenance request)
      [:canonical-core-artifact :provenance-binding-preimage :actual-source-path] path
      [:canonical-core-artifact :provenance-binding-id] provenance-digest)))

(defn- neutralize-product [product request]
  (doseq [address path-slot-addresses]
    (when-not (= (expected-slot-value product request address)
                 (get-address product address))
      (diagnostic! :path-slot-mismatch (request-path request))))
  (reduce (fn [current address] (assoc-address current address path-slot-sentinel))
          product path-slot-addresses))

(defn- contains-value? [value forbidden]
  (cond
    (contains? forbidden value) true
    (map? value) (boolean (some (fn [[key item]]
                                  (or (contains-value? key forbidden)
                                      (contains-value? item forbidden)))
                                value))
    (or (vector? value) (set? value))
    (boolean (some #(contains-value? % forbidden) value))
    :else false))

(defn- path-neutral? [product request]
  (let [path (request-path request)
        path-hash (when (string? path)
                    (sha256 (.getBytes ^String path StandardCharsets/UTF_8)))]
    (not (contains-value? product
                          (cond-> #{}
                            (and (string? path) (not (empty? path))) (conj path)
                            path-hash (conj path-hash (subs path-hash 7)))))))

(defn- request-path [request]
  (get-in request [:provenance :actual-source-path]))

(defn- request-sh06-id [request]
  (get-in request [:lineage :authenticated-sh06-artifact-id]))

(defn- snapshot-matches-request? [snapshot request]
  (and (= (:source-path snapshot) (request-path request))
       (= (:sh06-semantic-projection-id snapshot)
          (get-in request [:lineage :sh06-semantic-projection-id]))
       (= (:source-bytes-sha256 snapshot)
          (get-in request [:lineage :source-revision-id])
          (get-in request [:module :source-revision-id]))))

(defn- request-envelope-valid? [request]
  (let [module (:module request)]
    (and (closed-value? request)
         (string? (request-path request))
         (not (empty? (request-path request)))
         (string? (request-sh06-id request))
         (map? module)
         (vector? (:effects module))
         (every? keyword? (:effects module))
         (vector? (:capabilities module))
         (every? keyword? (:capabilities module)))))

(defn- current-authenticated-request [sh06 source-path]
  (try
    (bootstrap/sh07-core-authenticated-request sh06)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (diagnostic! :producer-interrupted source-path))
    (catch Exception _
      (diagnostic! :request-membership-mismatch source-path))))

(defn- static-envelope [request]
  (let [neutral (-> request
                    (assoc-in [:provenance :actual-source-path] path-slot-sentinel)
                    (assoc-in [:lineage :authenticated-sh06-artifact-id] path-slot-sentinel))
        module (:module request)]
    {:scope :sh07-b15-keyword-map-lookup
     :adapter :gravity/sh07-to-c6-core-products-v16
     :task "SH-07-B47"
     :module-policy {:namespace (:namespace module)
                     :profile (:profile module)
                     :target (:target module)
                     :safety (:safety module)
                     :effects (vec (sort (:effects module)))
                     :capabilities (vec (sort (:capabilities module)))
                     :exports (:exports module)}
     :source-revision-id (get-in request [:lineage :source-revision-id])
     :sh06-semantic-projection-id (get-in request [:lineage :sh06-semantic-projection-id])
     :authenticated-request-path-neutral neutral}))

(defn cache-key
  "Returns the complete path-free cache key, including the exact request
  envelope and every live implementation pin."
  [snapshot request]
  (when-not (request-envelope-valid? request)
    (diagnostic! :invalid-selection (:source-path snapshot)))
  (let [key (semantic-key snapshot)
        envelope (static-envelope request)]
    (when-not (and (snapshot-matches-request? snapshot request)
                   (closed-value? key)
                   (closed-value? envelope))
      (diagnostic! :invalid-selection (:source-path snapshot)))
    [key envelope]))

(defn- passed-sh06-report? [verification sh06]
  (and (map? verification)
       (= :gravity/sh06-resolution-artifact-verification
          (:artifact verification))
       (= :passed (:status verification))
       (map? (:checks verification))
       (seq (:checks verification))
       (every? true? (vals (:checks verification)))
       (= [] (:failed-checks verification))
       (= (get-in sh06 [:provenance :source-path])
          (:source-path verification))))

(defn- sh06-passed? [verification sh06]
  (passed-sh06-report? verification sh06))

(defn- fresh-sh06-verification [sh06 source-path]
  (try
    (bootstrap/sh06-resolution-artifact-verification sh06)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (diagnostic! :producer-interrupted source-path))
    (catch Exception _
      (diagnostic! :sh06-verification-failed source-path))))

(defn- product-from [value]
  (cond
    (and (map? value) (= run-product-keys (set (keys value)))) value
    (and (map? value) (contains? value :gravity-core-boundary))
    (let [boundary (:gravity-core-boundary value)]
      (when (and (map? boundary)
                 (= sh07-boundary-keys (set (keys boundary))))
        (select-keys boundary run-product-keys)))
    :else nil))

(defn- exact-run-product-status? [product]
  (let [raw (:raw-template-result product)
        template (:template-verification product)
        resolved (:resolved-verification product)
        requests (:digest-requests product)
        digests (:resolved-digests product)]
    (and (map? raw)
         (= raw-template-result-keys (set (keys raw)))
         (= :gravity/sh07-core-template-result (:artifact raw))
         (= 16 (:schema-version raw))
         (= :accepted (:status raw))
         (= [] (:diagnostics raw))
         (= requests (:digest-requests raw))
         (map? (:core-template raw))
         (map? template)
         (= template-verification-keys (set (keys template)))
         (= :gravity/sh07-core-template-verification (:artifact template))
         (= 16 (:schema-version template))
         (= :passed (:status template))
         (nil? (:rule template))
         (nil? (:reason template))
         (= {} (:details template))
         (map? resolved)
         (= resolved-verification-keys (set (keys resolved)))
         (= :gravity/sh07-core-resolved-verification (:artifact resolved))
         (= 16 (:schema-version resolved))
         (= :passed (:status resolved))
         (nil? (:rule resolved))
         (vector? requests)
         (vector? digests)
         (= (count requests) (count digests))
         (every? #(or (= path-slot-sentinel %)
                      (and (string? %)
                           (boolean (re-matches #"sha256:[0-9a-f]{64}" %))))
                 digests))))

(defn- semantic-key-valid? [key]
  (and (map? key)
       (= semantic-key-keys (set (keys key)))
       (integer? (:source-byte-count key))
       (not (neg? (:source-byte-count key)))
       (string? (:source-bytes-sha256 key))
       (boolean (re-matches #"sha256:[0-9a-f]{64}"
                            (:source-bytes-sha256 key)))
       (#{".gravity" ".qst"} (:source-extension key))
       (= :gravity/source (:source-kind key))
       (string? (:sh06-semantic-projection-id key))
       (map? (:pins key))))

(defn- static-envelope-valid? [envelope]
  (let [policy (:module-policy envelope)]
    (and (map? envelope)
         (= static-envelope-keys (set (keys envelope)))
         (= :sh07-b15-keyword-map-lookup (:scope envelope))
         (= :gravity/sh07-to-c6-core-products-v16 (:adapter envelope))
         (= "SH-07-B47" (:task envelope))
         (map? policy)
         (= module-policy-keys (set (keys policy)))
         (symbol? (:namespace policy))
         (keyword? (:profile policy))
         (keyword? (:target policy))
         (keyword? (:safety policy))
         (vector? (:effects policy))
         (= (:effects policy) (vec (sort (:effects policy))))
         (every? keyword? (:effects policy))
         (vector? (:capabilities policy))
         (= (:capabilities policy) (vec (sort (:capabilities policy))))
         (every? keyword? (:capabilities policy))
         (string? (:source-revision-id envelope))
         (string? (:sh06-semantic-projection-id envelope))
         (map? (:authenticated-request-path-neutral envelope)))))

(defn- carrier-preimage [carrier]
  (dissoc carrier :integrity-hash))

(defn- final-integrity [carrier]
  (bootstrap/reader-canonical-hash
   {:domain :gravity/sh07-semantic-run-product-carrier-v2
    :carrier (carrier-preimage carrier)}))

(defn- fixpoint-carrier [base]
  (loop [carrier (assoc base :canonical-byte-count 0) previous nil]
    (let [count (carrier-byte-count carrier)
          next (assoc carrier :canonical-byte-count count)]
      (if (= previous count) next (recur next count)))))

(defn construct-carrier
  "Constructs a carrier from a bounded snapshot, verified SH-06 value, exact
  authenticated request, and already successful ordinary SH-07 run product."
  [snapshot sh06 request run-product]
  (let [key (semantic-key snapshot)
        path (request-path request)
        verified (fresh-sh06-verification sh06 path)
        expected (current-authenticated-request sh06 path)
        product (product-from run-product)]
    (when-not (sh06-passed? verified sh06) (diagnostic! :sh06-verification-failed path))
    (when-not (and (= expected request) (request-envelope-valid? request))
      (diagnostic! :request-membership-mismatch path))
    (when-not (snapshot-matches-request? snapshot request)
      (diagnostic! :source-mutated path))
    (when-not (and product
                   (= run-product-keys (set (keys run-product)))
                   (closed-value? run-product)
                   (exact-run-product-status? product))
      (diagnostic! :supplied-verification-failed path))
    (when (contains-value? product #{path-slot-sentinel})
      (diagnostic! :path-slot-mismatch path))
    (when-not (= (:digest-requests product)
                 (get-in product [:raw-template-result :digest-requests]))
      (diagnostic! :digest-purpose-mismatch path))
    (purpose-indexes (:digest-requests product))
    (let [neutral (neutralize-product product request)
          _ (when-not (path-neutral? neutral request)
              (diagnostic! :path-slot-mismatch path))
          base {:artifact :gravity/sh07-semantic-run-product-carrier-v2
                :schema-version 2 :status :accepted :authority :non-authoritative
                :semantic-key key :path-slot-addresses path-slot-addresses
                :shape-manifest (shape-manifest neutral)
                :static-envelope (static-envelope request)
                :run-product neutral :integrity-hash nil
                :canonical-byte-count 0
                :nonclaims {:authoritative? false
                            :fresh-authoritative-run-required? true
                            :cross-process? false :cross-stage? false
                            :final-artifact-cached? false
                            :verification-report-cached? false}}
          _ (when-not (path-neutral? base request)
              (diagnostic! :path-slot-mismatch path))
          _ (when-not (closed-value? base)
              (diagnostic! :carrier-schema path))
          counted (fixpoint-carrier base)]
      (when (> (:canonical-byte-count counted) maximum-canonical-bytes)
        (diagnostic! :carrier-too-large path))
      (assoc counted :integrity-hash (final-integrity counted)))))

(defn- valid-carrier? [carrier]
  (try
    (and (map? carrier)
         (closed-value? carrier)
         (= carrier-keys (set (keys carrier)))
         (= :gravity/sh07-semantic-run-product-carrier-v2 (:artifact carrier))
         (= 2 (:schema-version carrier))
         (= :accepted (:status carrier))
         (= :non-authoritative (:authority carrier))
         (semantic-key-valid? (:semantic-key carrier))
         (static-envelope-valid? (:static-envelope carrier))
         (= expected-nonclaims (:nonclaims carrier))
         (= path-slot-addresses (:path-slot-addresses carrier))
         (= run-product-keys (set (keys (:run-product carrier))))
         (exact-run-product-status? (:run-product carrier))
         (every? #(= path-slot-sentinel
                      (get-address (:run-product carrier) %))
                 path-slot-addresses)
         (= (:shape-manifest carrier) (shape-manifest (:run-product carrier)))
         (integer? (:canonical-byte-count carrier))
         (pos? (:canonical-byte-count carrier))
         (= (:canonical-byte-count carrier) (carrier-byte-count carrier))
         (= (:integrity-hash carrier) (final-integrity carrier))
         (<= (:canonical-byte-count carrier) maximum-canonical-bytes))
    (catch Exception _ false)
    (catch StackOverflowError _ false)))

(defn lookup-valid?
  "Validates an immutable process-local entry before it is counted as a hit."
  [carrier snapshot request]
  (try
    (and (valid-carrier? carrier)
         (snapshot-matches-request? snapshot request)
         (= (:semantic-key carrier) (semantic-key snapshot))
         (= (get-in carrier [:semantic-key :pins]) (live-pins))
         (= (:static-envelope carrier) (static-envelope request)))
    (catch Exception _ false)
    (catch StackOverflowError _ false)))

(defn- bound-slot-value [product request address]
  (let [path (request-path request)
        sh06-id (request-sh06-id request)
        indexes (purpose-indexes (:digest-requests product))
        artifact-index (:artifact-id-ordinal indexes)
        provenance-index (:provenance-ordinal indexes)
        provenance-digest (get-in product [:resolved-digests provenance-index])]
    (case address
      [:raw-template-result :core-template :provenance] (:provenance request)
      [:raw-template-result :core-template :provenance-binding-preimage :actual-source-path] path
      [:raw-template-result :core-template :lineage :authenticated-sh06-artifact-id] sh06-id
      [:raw-template-result :digest-requests :artifact-id-ordinal :preimage :lineage :authenticated-sh06-artifact-id]
      sh06-id
      [:raw-template-result :digest-requests :provenance-ordinal :preimage :actual-source-path]
      path
      [:digest-requests :artifact-id-ordinal :preimage :lineage :authenticated-sh06-artifact-id]
      sh06-id
      [:digest-requests :provenance-ordinal :preimage :actual-source-path]
      path
      [:resolved-digests :provenance-ordinal] provenance-digest
      [:canonical-core-artifact :provenance] (:provenance request)
      [:canonical-core-artifact :provenance-binding-preimage :actual-source-path] path
      [:canonical-core-artifact :provenance-binding-id] provenance-digest)))

(defn- copy-value [value]
  (cond
    (map? value) (if (empty? value)
                   (clojure.lang.PersistentArrayMap. (object-array 0))
                   (into {} (map (fn [[key item]]
                                   [(copy-value key) (copy-value item)]) value)))
    (vector? value) (if (empty? value)
                      (clojure.lang.PersistentVector/create (object-array 0))
                      (mapv copy-value value))
    (set? value) (if (empty? value)
                   (clojure.lang.PersistentHashSet/create (object-array 0))
                   (into #{} (map copy-value) value))
    :else value))

(defn- bind-product [carrier request]
  (let [neutral (copy-value (:run-product carrier))
        initial (reduce (fn [product address]
                          (if (or (= address [:resolved-digests :provenance-ordinal])
                                  (= address [:canonical-core-artifact
                                              :provenance-binding-id]))
                            product
                            (assoc-address product address
                                           (bound-slot-value product request address))))
                        neutral path-slot-addresses)]
    (assoc-in initial [:raw-template-result :digest-requests]
              (:digest-requests initial))))

(defn- apply-resolved-digests [product resolved]
  (let [provenance-index (:provenance-ordinal
                          (purpose-indexes (:digest-requests product)))
        provenance-id (get resolved provenance-index)]
    (-> product
        (assoc :resolved-digests resolved)
        (assoc-in [:canonical-core-artifact :provenance-binding-id]
                  provenance-id))))

(def ^:private check-catalog
  [:carrier-integrity :closed-schema :live-pins :request-membership
   :upstream-verification :source-envelope :static-envelope
   :path-slot-completeness :digest-purpose-uniqueness :digest-rebinding
   :non-path-equality :template-status :resolved-status :artifact-identity
   :provenance-identity :lineage :no-alias])

(defn- safe-check [predicate]
  (try
    (boolean (predicate))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      false)
    (catch Exception _ false)
    (catch StackOverflowError _ false)))

(defn verify-supplied-product
  "Independent closed predicates for a supplied bound product.  The optional
  `seams` map supplies integration-only pure checks (for exact digest and
  final-envelope rules) without allowing a full SH-07 build on a cache hit."
  ([carrier snapshot sh06 verification request candidate]
   (verify-supplied-product carrier snapshot sh06 verification request candidate {}))
  ([carrier snapshot sh06 verification request candidate seams]
   (let [path (request-path request)
         product (product-from candidate)
         sentinel-free? (and product
                             (not (contains-value? candidate
                                                   #{path-slot-sentinel})))
         neutral (if sentinel-free?
                   (try (neutralize-product product request)
                        (catch Exception _ ::invalid))
                   ::invalid)
         checks
         {:carrier-integrity (valid-carrier? carrier)
          :closed-schema
          (safe-check
           #(and product
                 (closed-value? candidate)
                 (= run-product-keys (set (keys product)))
                 (exact-run-product-status? product)
                 (if-let [predicate (:final-envelope? seams)]
                   (predicate sh06 request product candidate)
                   true)
                 (= (:shape-manifest carrier)
                    (shape-manifest (:run-product carrier)))))
          :live-pins
          (safe-check #(and (closed-value? (live-pins))
                            (= (get-in carrier [:semantic-key :pins])
                               (live-pins))))
          :request-membership
          (safe-check #(= request (current-authenticated-request sh06 path)))
          :upstream-verification
          (safe-check
           #(and (sh06-passed? verification sh06)
                 (if-let [predicate (:upstream-verification? seams)]
                   (predicate sh06 verification)
                   true)))
          :source-envelope
          (safe-check #(and (snapshot-matches-request? snapshot request)
                            (= (semantic-key snapshot) (:semantic-key carrier))))
          :static-envelope
          (safe-check #(= (static-envelope request) (:static-envelope carrier)))
          :path-slot-completeness
          (safe-check #(and sentinel-free?
                            (not= ::invalid neutral)
                            (path-neutral? neutral request)
                            (= neutral (:run-product carrier))))
          :digest-purpose-uniqueness
          (safe-check #(do (purpose-indexes (:digest-requests product)) true))
          :digest-rebinding
          (if-let [predicate (:digest-rebinding? seams)]
            (safe-check #(predicate request product)) false)
          :non-path-equality
          (safe-check
           #(and (not= ::invalid neutral)
                 (java.util.Arrays/equals
                  ^bytes (canonical-bytes neutral)
                  ^bytes (canonical-bytes (:run-product carrier)))))
          :template-status
          (safe-check #(and (exact-run-product-status? product)
                            (= (:digest-requests product)
                               (get-in product [:raw-template-result
                                                :digest-requests]))))
          :resolved-status
          (safe-check #(and (exact-run-product-status? product)
                            (= (count (:digest-requests product))
                               (count (:resolved-digests product)))))
          :artifact-identity
          (if-let [predicate (:artifact-identity? seams)]
            (safe-check #(predicate request product candidate)) false)
          :provenance-identity
          (if-let [predicate (:provenance-identity? seams)]
            (safe-check #(predicate request product candidate)) false)
          :lineage (if-let [predicate (:lineage? seams)]
                     (safe-check #(predicate request product candidate)) false)
          :no-alias (if-let [predicate (:no-alias? seams)]
                      (safe-check #(predicate carrier candidate)) false)}
         checks (into (sorted-map) checks)
         failed (->> check-catalog (remove checks) vec)]
     {:artifact :gravity/sh07-supplied-run-product-verification-v2
      :schema-version 2 :status (if (empty? failed) :passed :failed)
      :authority :non-authoritative :checks checks :failed-checks failed
      :source-path path :fresh-authoritative-run-required? true})))

(defn bind-carrier
  "Binds a carrier to the supplied current request.  `verification` must be
  the fresh passed SH-06 report; no ordinary SH-07 function is invoked here."
  ([carrier snapshot sh06 verification request]
   (bind-carrier carrier snapshot sh06 verification request {}))
  ([carrier snapshot sh06 verification request seams]
   (let [path (request-path request)]
     (when-not (valid-carrier? carrier) (diagnostic! :carrier-integrity path))
     (when-not (and (snapshot-matches-request? snapshot request)
                    (request-envelope-valid? request))
       (diagnostic! :source-mutated path))
     (when-not (= (:semantic-key carrier) (semantic-key snapshot))
       (diagnostic! :source-mutated path))
     (when-not
      (and (sh06-passed? verification sh06)
           (if-let [predicate (:upstream-verification? seams)]
             (safe-check #(predicate sh06 verification))
             true))
       (diagnostic! :sh06-verification-failed path))
     (when-not (= request (current-authenticated-request sh06 path))
       (diagnostic! :request-membership-mismatch path))
     (when-not (= (:static-envelope carrier) (static-envelope request))
       (diagnostic! :request-membership-mismatch path))
     (let [resolve-digests (:resolve-digests seams)
           assemble-artifact (:assemble-artifact seams)
           _ (when-not (and (fn? resolve-digests) (fn? assemble-artifact))
               (diagnostic! :digest-purpose-mismatch path))
           product (bind-product carrier request)
           resolved (try
                      (resolve-digests request (:digest-requests product))
                      (catch InterruptedException interrupted
                        (.interrupt (Thread/currentThread))
                        (diagnostic! :producer-interrupted path))
                      (catch Exception _
                        (diagnostic! :digest-purpose-mismatch path)))
           _ (when-not (and (vector? resolved)
                            (= (count (:digest-requests product))
                               (count resolved))
                            (every? #(and (string? %)
                                          (boolean
                                           (re-matches #"sha256:[0-9a-f]{64}" %)))
                                    resolved))
               (diagnostic! :digest-purpose-mismatch path))
           product (apply-resolved-digests product resolved)
           artifact (try
                      (copy-value (assemble-artifact sh06 request product))
                      (catch InterruptedException interrupted
                        (.interrupt (Thread/currentThread))
                        (diagnostic! :producer-interrupted path))
                      (catch Exception _
                        (diagnostic! :supplied-verification-failed path)))
           supplied (verify-supplied-product
                     carrier snapshot sh06 verification request artifact seams)]
       (when-not (= :passed (:status supplied))
         (diagnostic! :supplied-verification-failed path))
       {:artifact :gravity/sh07-semantic-carrier-bind-result-v2
        :schema-version 2 :status :accepted :authority :non-authoritative
        :source-path path :sh07-artifact artifact :supplied-verification supplied
        :fresh-authoritative-run-required? true}))))
