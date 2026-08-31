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

(defn live-pins []
  (let [groups (into {}
                     (for [[group symbols] (dissoc pin-spec :request-schema-version)]
                       [group (mapv live-var-value symbols)]))]
    (assoc groups
           :request-schema-version 15
           :runtime {:java-runtime-version (System/getProperty "java.runtime.version")
                     :clojure-version (clojure-version)})))

(defn- snapshot-value [snapshot keys]
  (some #(when (contains? snapshot %) (get snapshot %)) keys))

(defn semantic-key
  "Returns the path-free identity projection for a bounded source snapshot.
  The snapshot remains caller-owned; only byte-derived identity is retained."
  [snapshot]
  (let [path (snapshot-value snapshot [:source-path :path])
        bytes (snapshot-value snapshot [:source-bytes :bytes])
        byte-count (snapshot-value snapshot [:source-byte-count :byte-count])
        source-hash (snapshot-value snapshot [:source-bytes-sha256 :source-content-hash :sha256])
        extension (snapshot-value snapshot [:source-extension :extension])
        source-kind (or (:source-kind snapshot) :gravity/source)
        projection-id (snapshot-value snapshot [:sh06-semantic-projection-id :semantic-projection-id])]
    (when-not (and (string? path) (instance? (Class/forName "[B") bytes)
                   (integer? byte-count) (not (neg? byte-count))
                   (= byte-count (alength ^bytes bytes))
                   (= source-hash (sha256 bytes))
                   (#{".gravity" ".qst"} extension)
                   (= :gravity/source source-kind)
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
        (keyword? value) (symbol? value)) (not (seq (meta value)))
    (vector? value) (and (not (seq (meta value))) (every? closed-value? value))
    (set? value) (and (not (seq (meta value))) (every? closed-value? value))
    (map? value) (and (not (record? value)) (not (seq (meta value)))
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

(defn- neutralize-product [product]
  (reduce (fn [current address] (assoc-address current address path-slot-sentinel))
          product path-slot-addresses))

(defn- request-path [request]
  (get-in request [:provenance :actual-source-path]))

(defn- request-sh06-id [request]
  (get-in request [:lineage :authenticated-sh06-artifact-id]))

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

(defn- sh06-passed? [verification sh06]
  (and (= :passed (:status verification))
       (empty? (:failed-checks verification))
       (= (:artifact verification) :gravity/sh06-resolution-artifact-verification)))

(defn- product-from [run-product]
  (select-keys run-product run-product-keys))

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
        verified (bootstrap/sh06-resolution-artifact-verification sh06)
        expected (bootstrap/sh07-core-authenticated-request sh06)
        product (product-from run-product)]
    (when-not (sh06-passed? verified sh06) (diagnostic! :sh06-verification-failed path))
    (when-not (= expected request) (diagnostic! :request-membership-mismatch path))
    (when-not (and (= run-product-keys (set (keys run-product)))
                   (= :passed (get-in product [:template-verification :status]))
                   (= :passed (get-in product [:resolved-verification :status])))
      (diagnostic! :supplied-verification-failed path))
    (when-not (= (:digest-requests product)
                 (get-in product [:raw-template-result :digest-requests]))
      (diagnostic! :digest-purpose-mismatch path))
    (purpose-indexes (:digest-requests product))
    (let [neutral (neutralize-product product)
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
          counted (fixpoint-carrier base)]
      (when (> (:canonical-byte-count counted) maximum-canonical-bytes)
        (diagnostic! :carrier-too-large path))
      (assoc counted :integrity-hash (final-integrity counted)))))

(defn- valid-carrier? [carrier]
  (and (map? carrier)
       (= carrier-keys (set (keys carrier)))
       (= path-slot-addresses (:path-slot-addresses carrier))
       (= (:shape-manifest carrier) (shape-manifest (:run-product carrier)))
       (= (:canonical-byte-count carrier) (carrier-byte-count carrier))
       (= (:integrity-hash carrier) (final-integrity carrier))
       (<= (:canonical-byte-count carrier) maximum-canonical-bytes)))

(defn- bound-slot-value [product request address]
  (let [path (request-path request)
        sh06-id (request-sh06-id request)
        indexes (purpose-indexes (:digest-requests product))
        artifact-index (:artifact-id-ordinal indexes)
        provenance-index (:provenance-ordinal indexes)
        provenance-digest (get-in product [:resolved-digests provenance-index])]
    (case address
      [:raw-template-result :core-template :provenance] {:source-path path}
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
      [:canonical-core-artifact :provenance] {:source-path path}
      [:canonical-core-artifact :provenance-binding-preimage :actual-source-path] path
      [:canonical-core-artifact :provenance-binding-id] provenance-digest)))

(defn- copy-value [value]
  (cond
    (map? value) (into {} (map (fn [[key item]] [(copy-value key) (copy-value item)]) value))
    (vector? value) (mapv copy-value value)
    (set? value) (into #{} (map copy-value) value)
    :else value))

(defn- bind-product [carrier request resolve-digest]
  (let [neutral (copy-value (:run-product carrier))
        initial (reduce (fn [product address]
                          (assoc-address product address
                                         (bound-slot-value product request address)))
                        neutral path-slot-addresses)
        resolved (reduce (fn [values digest-request]
                           (conj values
                                 (resolve-digest (:purpose digest-request)
                                                 (:preimage digest-request)
                                                 values)))
                         [] (:digest-requests initial))
        provenance-index (:provenance-ordinal
                          (purpose-indexes (:digest-requests initial)))
        provenance-id (get resolved provenance-index)
        rebound (-> initial
                    (assoc :resolved-digests resolved)
                    (assoc-in [:canonical-core-artifact
                               :provenance-binding-id]
                              provenance-id))]
    ;; The raw template has the same vector contract and receives the exact
    ;; same rebound digest requests and values, never a separately derived copy.
    (assoc-in rebound [:raw-template-result :digest-requests]
              (:digest-requests rebound))))

(def ^:private check-catalog
  [:carrier-integrity :closed-schema :live-pins :request-membership
   :upstream-verification :source-envelope :static-envelope
   :path-slot-completeness :digest-purpose-uniqueness :digest-rebinding
   :non-path-equality :template-status :resolved-status :artifact-identity
   :provenance-identity :lineage :no-alias])

(defn verify-supplied-product
  "Independent closed predicates for a supplied bound product.  The optional
  `seams` map supplies integration-only pure checks (for exact digest and
  final-envelope rules) without allowing a full SH-07 build on a cache hit."
  ([carrier snapshot sh06 verification request candidate]
   (verify-supplied-product carrier snapshot sh06 verification request candidate {}))
  ([carrier snapshot sh06 verification request candidate seams]
   (let [path (request-path request)
         product (product-from candidate)
         neutral (try (neutralize-product product) (catch Exception _ ::invalid))
         checks {:carrier-integrity (valid-carrier? carrier)
                 :closed-schema (and (= run-product-keys (set (keys product)))
                                     (= (:shape-manifest carrier)
                                        (shape-manifest (:run-product carrier))))
                 :live-pins (= (get-in carrier [:semantic-key :pins]) (live-pins))
                 :request-membership (= request (bootstrap/sh07-core-authenticated-request sh06))
                 :upstream-verification (sh06-passed? verification sh06)
                 :source-envelope (= (semantic-key snapshot) (:semantic-key carrier))
                 :static-envelope (= (static-envelope request) (:static-envelope carrier))
                 :path-slot-completeness (= neutral (:run-product carrier))
                 :digest-purpose-uniqueness (try (purpose-indexes (:digest-requests product)) true
                                                  (catch Exception _ false))
                 :digest-rebinding (if-let [predicate (:digest-rebinding? seams)]
                                      (boolean (predicate request product)) false)
                 :non-path-equality (java.util.Arrays/equals
                                     ^bytes (canonical-bytes neutral)
                                     ^bytes (canonical-bytes (:run-product carrier)))
                 :template-status (= :passed (get-in product [:template-verification :status]))
                 :resolved-status (= :passed (get-in product [:resolved-verification :status]))
                 :artifact-identity (if-let [predicate (:artifact-identity? seams)]
                                      (boolean (predicate request product)) false)
                 :provenance-identity (if-let [predicate (:provenance-identity? seams)]
                                        (boolean (predicate request product)) false)
                 :lineage (if-let [predicate (:lineage? seams)]
                            (boolean (predicate request product)) false)
                 :no-alias (if-let [predicate (:no-alias? seams)]
                             (boolean (predicate carrier candidate)) false)}
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
     (when-not (= (:semantic-key carrier) (semantic-key snapshot))
       (diagnostic! :source-mutated path))
     (when-not (sh06-passed? verification sh06) (diagnostic! :sh06-verification-failed path))
     (when-not (= request (bootstrap/sh07-core-authenticated-request sh06))
       (diagnostic! :request-membership-mismatch path))
     (when-not (= (:static-envelope carrier) (static-envelope request))
       (diagnostic! :request-membership-mismatch path))
     (let [resolve-digest (:resolve-digest seams)
           _ (when-not (fn? resolve-digest) (diagnostic! :digest-purpose-mismatch path))
           product (bind-product carrier request resolve-digest)
           supplied (verify-supplied-product carrier snapshot sh06 verification request product seams)]
       (when-not (= :passed (:status supplied))
         (diagnostic! :supplied-verification-failed path))
       {:artifact :gravity/sh07-semantic-carrier-bind-result-v2
        :schema-version 2 :status :accepted :authority :non-authoritative
        :source-path path :bound-run-product product :supplied-verification supplied
        :fresh-authoritative-run-required? true}))))
