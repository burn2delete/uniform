(ns gravity.pass-execution
  "Pure, non-authoritative pass execution receipts and evidence composition.

  This hosted Stage0 leaf records what an injected compiler pass actually
  consumed, produced, preserved, invalidated, regenerated, and verified.  It
  owns neither pass implementations nor cache storage and cannot mint proof,
  release, self-hosting, or aggregate authority."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]))

(def ^:private maximum-depth 64)
(def ^:private maximum-nodes 16384)
(def ^:private maximum-canonical-bytes (* 4 1024 1024))
(def ^:private maximum-integer-bits (* 3 maximum-canonical-bytes))
(def ^:private maximum-evidence-records 4096)
(def ^:private maximum-dag-receipts 1024)
(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")

(def ^:private pass-contract-fields
  #{:pass :version :order :input :output :requires :preserves :invalidates
    :regenerates :replacement-evidence :emits :effects :capabilities :profiles
    :required-evidence :verifier-required? :authority-ceiling})

(def ^:private semantic-binding-fields
  #{:compiler-id :capability-policy-id :facet-set-id :provider-manifest-id
    :package-lock-id :diagnostic-schema-id})

(def ^:private provenance-fields
  #{:provenance-id :source-path :metadata})

(def ^:private request-authority-fields
  #{:input-authorities :claimed-level :scope})

(def ^:private external-root-fields
  #{:kind :facts})

(def ^:private execution-request-fields
  #{:stage :contract :producer-binding-id :input-artifact-ids :input-facts
    :external-root-inputs
    :semantic-bindings :dependency-graph-id :build-effect-replay-id
    :profile-id :target-id :policy-ids :provenance :diagnostic-stream-id
    :execution-mode :authority})

(def ^:private execute-operation-fields
  #{:produce! :validate-output! :artifact-id-of :verifier-reports
    :evidence-records})

(def ^:private receipt-validation-operation-fields
  #{:validate-diagnostic-stream! :validate-verifier-report!
    :validate-evidence-record!})

(def ^:private verifier-report-fields
  #{:verifier-id :stage :artifact-id :status})

(def ^:private evidence-record-fields
  #{:evidence-id :kind :status :artifact-id :authority-level})

(def ^:private receipt-authority-fields
  #{:input-authorities :claimed-level :effective-level :ceiling :scope
    :authority-contribution? :aggregate-authoritative?})

(def ^:private receipt-fields
  #{:artifact :schema-version :receipt-id :stage :pass-contract-id
    :producer-binding-id :input-artifact-ids :output-artifact-id
    :external-root-inputs
    :input-facts :output-facts :requires :preserves :invalidates :regenerates
    :replacement-evidence :effects :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-id :verifier-reports :evidence-records :execution-mode
    :authority})

(def ^:private authority-rank
  {:none 0 :non-authoritative 1 :reviewed 2 :authoritative 3})

(def ^:private evidence-dag-fields
  #{:artifact :schema-version :root-receipt-id :receipts :contracts :edges
    :authority :evidence-root-id})

(def ^:private evidence-dag-authority-fields
  #{:effective-level :authority-contribution? :aggregate-authoritative?})

(def ^:private public-api
  {'pass-execution-contract {:arglists '([])}
   'canonical-pass-contract {:arglists '([contract])}
   'pass-contract-id {:arglists '([contract])}
   'validate-pass-contract! {:arglists '([contract])}
   'execute-pass! {:arglists '([request operations])}
   'validate-execution-receipt! {:arglists '([receipt contract operations])}
   'compose-evidence-dag {:arglists '([receipts contracts])}
   'evidence-root {:arglists '([dag])}})

(def ^:private namespace-contract
  {:namespace 'gravity.pass-execution
   :contract-boundary :hosted-stage0-pass-execution-receipts-v1
   :public-api public-api
   :owns [:bounded-canonical-pass-contract-identity
          :pass-execution-receipts
          :fact-flow-validation
          :evidence-dag-composition
          :authority-monotonicity]
   :dependency-direction
   {:requires ['clojure.core 'clojure.set 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.c2-pass-cache
              'gravity.c16-incremental]}
   :compatibility-only? true
   :authoritative? false
   :cache-storage? false
   :pass-implementation? false
   :proof-authority? false
   :release-authority? false
   :self-hosting-authority? false
   :aggregate-authority? false
   :digest-is-signature? false
   :semantic-ordering
   {:integers :type-sensitive-integral-tags
    :input-artifact-ids :lexical-sha256
    :policy-ids :lexical-sha256
    :pass-order [:declared-order :pass-contract-id]}})

(def ^:dynamic ^:private *diagnostic-context* {})

(declare preflight-canonical!)

(defn- fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :stage :pass-execution
                          :pass nil
                          :artifact-id nil
                          :profile-id nil
                          :target-id nil
                          :remediation
                          "validate inputs against D1/C1/C16/C18 contracts"}
                         *diagnostic-context*
                         data))))

(defn- exact-map!
  [value fields id label]
  ;; This gate also covers operation maps and operation-returned records.
  ;; Bound before inspecting keys so no derived key collection is needed.
  (preflight-canonical! value)
  (when-not (map? value)
    (fail! id (str label " must be a map") {:field label}))
  (reduce-kv (fn [_ key _]
               (when-not (contains? fields key)
                 (fail! id (str label " has an unknown field")
                        {:field label :unknown-field key}))
               nil)
             nil value)
  (let [missing (reduce (fn [result field]
                          (if (contains? value field)
                            result
                            (conj result field)))
                        [] fields)]
    (when (seq missing)
      (fail! id (str label " is missing required fields")
             {:field label :missing-fields missing})))
  value)

(defn- sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn- require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "semantic identity must be lowercase SHA-256"
           {:field field :observed value}))
  value)

(defn- distinct-vector!
  [field value predicate]
  (when-not (vector? value)
    (fail! "C16-ENTRY" "receipt identity field must be a vector"
           {:field field :observed value}))
  ;; Bound before every?/distinct can traverse or materialize derived state.
  (when (> (count value) maximum-nodes)
    (fail! "C16-KEY" "receipt vector exceeds its cardinality bound"
           {:field field :maximum-cardinality maximum-nodes}))
  (when-not (and (every? predicate value)
                 (= (count value) (count (distinct value))))
    (fail! "C16-ENTRY" "receipt vector is malformed or contains duplicates"
           {:field field :observed value}))
  value)

(defn- sorted-sha-vector!
  [field value]
  (distinct-vector! field value sha256-id?)
  (when-not (= value (vec (sort value)))
    (fail! "C16-KEY" "semantic identity vectors must use lexical SHA-256 order"
           {:field field :observed value
            :remediation "sort semantic identity vectors lexically"}))
  value)

(declare canonical-node)

(defn- integral-tag
  [value]
  (cond
    (instance? Byte value) :byte
    (instance? Short value) :short
    (instance? Integer value) :int
    (instance? Long value) :long
    (instance? clojure.lang.BigInt value) :bigint
    (instance? java.math.BigInteger value) :biginteger
    :else nil))

(defn- canonical-sort
  [values]
  (->> values (sort-by pr-str) vec))

(defn- bounded-utf8-size
  [text]
  (loop [index 0 size 0]
    (if (= index (.length ^String text))
      size
      (let [code-point (.codePointAt ^String text index)
            width (Character/charCount code-point)
            bytes (cond
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)
            next-size (+ size bytes)]
        (when (> next-size maximum-canonical-bytes)
          (fail! "C16-KEY" "canonical scalar exceeds its byte bound"
                 {:maximum-bytes maximum-canonical-bytes}))
        (recur (+ index width) next-size)))))

(defn- escaped-string-byte-bound
  [text]
  ;; Every UTF-8 byte can expand to at most one six-byte \\uXXXX escape in the
  ;; canonical printed representation. Charge that expansion before a value
  ;; can reach pr-str through canonical sorting or final encoding.
  (* 6 (bounded-utf8-size text)))

(defn- arbitrary-integer-bit-length
  [value]
  (let [integer (cond
                  (instance? clojure.lang.BigInt value)
                  (.toBigInteger ^clojure.lang.BigInt value)
                  (instance? java.math.BigInteger value) value
                  :else nil)]
    (cond
      integer
      (let [bits (.bitLength ^java.math.BigInteger integer)]
        ;; Negative bitLength excludes the sign bit. Conservatively add one
        ;; without allocating an absolute-value magnitude.
        (if (neg? (.signum ^java.math.BigInteger integer)) (inc bits) bits))

      ;; Fixed-width integral types cannot approach the configured bound.
      (integral-tag value) 64
      :else nil)))

(defn- integer-decimal-byte-bound!
  [value]
  (let [bits (arbitrary-integer-bit-length value)]
    (when (> bits maximum-integer-bits)
      (fail! "C16-KEY" "canonical integer exceeds its magnitude bound"
             {:maximum-bits maximum-integer-bits :observed-bits bits}))
    ;; ceil(bits * log10(2)), using a conservative rational upper bound, plus
    ;; one byte for zero or a possible sign. No decimal string is constructed.
    (+ 1 (quot (+ (* bits 30103) 99999) 100000))))

(defn- preflight-account!
  [state byte-bound]
  (let [{:keys [nodes bytes]}
        (swap! state (fn [{:keys [nodes bytes]}]
                       {:nodes (inc nodes) :bytes (+ bytes byte-bound)}))]
    (when (> nodes maximum-nodes)
      (fail! "C16-KEY" "canonical value exceeds its node bound"
             {:maximum-nodes maximum-nodes}))
    (when (> bytes maximum-canonical-bytes)
      (fail! "C16-KEY" "canonical value exceeds its byte bound"
             {:maximum-bytes maximum-canonical-bytes}))))

(declare preflight-value!)

(defn- preflight-container-cardinality!
  [value]
  (when (> (count value) maximum-nodes)
    (fail! "C16-KEY" "canonical container exceeds its cardinality bound"
           {:maximum-cardinality maximum-nodes})))

(defn- preflight-value!
  [value state depth]
  (when (> depth maximum-depth)
    (fail! "C16-KEY" "canonical value exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  (let [scalar-byte-bound
        (cond
          (string? value) (escaped-string-byte-bound value)
          (or (keyword? value) (symbol? value))
          (+ (if-let [space (namespace value)]
               (escaped-string-byte-bound space)
               0)
             (escaped-string-byte-bound (name value)))
          (integral-tag value) (integer-decimal-byte-bound! value)
          (ratio? value) (+ (integer-decimal-byte-bound! (numerator value))
                            (integer-decimal-byte-bound! (denominator value)))
          :else 0)]
    (preflight-account! state (+ 32 scalar-byte-bound)))
  (cond
    (map? value)
    (do
      (preflight-container-cardinality! value)
      (reduce-kv (fn [_ key item]
                   (preflight-value! key state (inc depth))
                   (preflight-value! item state (inc depth))
                   nil)
                 nil value))

    (or (set? value) (vector? value))
    (do
      (preflight-container-cardinality! value)
      (reduce (fn [_ item]
                (preflight-value! item state (inc depth))
                nil)
              nil value))

    (seq? value)
    (loop [remaining (seq value) cardinality 0]
      (when remaining
        (when (>= cardinality maximum-nodes)
          (fail! "C16-KEY" "canonical sequence exceeds its cardinality bound"
                 {:maximum-cardinality maximum-nodes}))
        (preflight-value! (first remaining) state (inc depth))
        (recur (next remaining) (inc cardinality))))

    :else nil)
  value)

(defn- preflight-canonical!
  [value]
  (preflight-value! value (atom {:nodes 0 :bytes 0}) 0)
  value)

(defn- account-canonical!
  [state byte-estimate]
  (let [{:keys [nodes bytes]}
        (swap! state (fn [{:keys [nodes bytes]}]
                       {:nodes (inc nodes)
                        :bytes (+ bytes byte-estimate)}))]
    (when (> nodes maximum-nodes)
      (fail! "C16-KEY" "canonical value exceeds its node bound"
             {:maximum-nodes maximum-nodes}))
    (when (> bytes maximum-canonical-bytes)
      (fail! "C16-KEY" "canonical value exceeds its byte bound"
             {:maximum-bytes maximum-canonical-bytes}))))

(defn- canonical-node
  [value state depth]
  (when (> depth maximum-depth)
    (fail! "C16-KEY" "canonical value exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  ;; Account before constructing or sorting the complete canonical tree.  The
  ;; scalar estimate is deliberately conservative because printed escaping can
  ;; expand a UTF-8 string; final encoded bytes are checked again below.
  (account-canonical!
   state
   (+ 32
      (cond
        (string? value) (escaped-string-byte-bound value)
        (or (keyword? value) (symbol? value))
        (+ (if-let [space (namespace value)]
             (escaped-string-byte-bound space)
             0)
           (escaped-string-byte-bound (name value)))
        (integral-tag value) (bounded-utf8-size (str value))
        (ratio? value) (+ (bounded-utf8-size (str (numerator value)))
                          (bounded-utf8-size (str (denominator value))))
        :else 0)))
  (when (and (instance? clojure.lang.IMeta value) (seq (meta value)))
    (fail! "C16-KEY" "semantic values may not carry host metadata"
           {:value-class (.getName (class value))}))
  (cond
    (nil? value) [:nil]
    (true? value) [:boolean true]
    (false? value) [:boolean false]
    (string? value) [:string value]
    (char? value) [:character (int value)]
    (keyword? value) [:keyword (namespace value) (name value)]
    (symbol? value) [:symbol (namespace value) (name value)]
    (integral-tag value) [:integer (integral-tag value) (str value)]
    (ratio? value) [:ratio (str (numerator value)) (str (denominator value))]
    (map? value)
    [:map
     (canonical-sort
      (mapv (fn [[key item]]
              [(canonical-node key state (inc depth))
               (canonical-node item state (inc depth))])
            value))]
    (set? value)
    [:set (canonical-sort
           (mapv #(canonical-node % state (inc depth)) value))]
    (vector? value)
    [:vector (mapv #(canonical-node % state (inc depth)) value)]
    (seq? value)
    [:list (mapv #(canonical-node % state (inc depth)) value)]
    :else
    (fail! "C16-KEY" "unsupported value in semantic identity"
           {:value-class (.getName (class value))})))

(defn- canonical-bytes
  [value]
  (preflight-canonical! value)
  (let [text (pr-str (canonical-node value (atom {:nodes 0 :bytes 0}) 0))
        bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)]
    (when (> (alength bytes) maximum-canonical-bytes)
      (fail! "C16-KEY" "canonical value exceeds its byte bound"
             {:maximum-bytes maximum-canonical-bytes
              :observed-bytes (alength bytes)}))
    bytes))

(defn- content-id
  [domain value]
  (str "sha256:"
       (digest/sha256-bytes-hex
        (canonical-bytes {:domain domain :schema-version 1 :value value}))))

(defn- keyword-set!
  [field value]
  (when-not (and (set? value) (every? keyword? value))
    (fail! "C1-PASS-CONTRACT" "pass fact fields must be keyword sets"
           {:field field :observed value}))
  value)

(defn- authority-level!
  [field value]
  (when-not (contains? authority-rank value)
    (fail! "C16-POLICY" "unknown authority level"
           {:field field :observed value}))
  value)

(defn- weakest-authority
  [levels]
  (first (sort-by authority-rank levels)))

(defn validate-pass-contract!
  "Validate one exact, bounded C1/C16/C18 pass contract."
  [contract]
  (binding [*diagnostic-context*
            (merge *diagnostic-context* {:pass (:pass contract)})]
  (preflight-canonical! contract)
  (exact-map! contract pass-contract-fields "C1-PASS-CONTRACT" :contract)
  (when-not (keyword? (:pass contract))
    (fail! "C1-PASS-CONTRACT" "pass id must be a keyword" {}))
  (when-not (and (string? (:version contract)) (not (empty? (:version contract))))
    (fail! "C1-PASS-CONTRACT" "pass version must be a nonempty string" {}))
  (when-not (and (integer? (:order contract)) (pos? (:order contract)))
    (fail! "D1-PIPELINE-ORDER" "pass order must be a positive integer"
           {:observed (:order contract)}))
  (doseq [field [:input :output]]
    (when-not (keyword? (get contract field))
      (fail! "C1-PASS-CONTRACT" "pass IR kinds must be keywords"
             {:field field :observed (get contract field)})))
  (doseq [field [:requires :preserves :invalidates :regenerates
                 :emits :effects :capabilities :profiles
                 :required-evidence]]
    (keyword-set! field (get contract field)))
  (when-not (and (map? (:replacement-evidence contract))
                 (every? keyword? (keys (:replacement-evidence contract)))
                 (every? keyword? (vals (:replacement-evidence contract)))
                 (= (count (:replacement-evidence contract))
                    (count (distinct (vals (:replacement-evidence contract))))))
    (fail! "C1-PASS-CONTRACT"
           "replacement evidence must map facts to unique evidence kinds"
           {:field :replacement-evidence
            :observed (:replacement-evidence contract)}))
  (when-not (boolean? (:verifier-required? contract))
    (fail! "C1-PASS-CONTRACT" "verifier requirement must be boolean" {}))
  (authority-level! :authority-ceiling (:authority-ceiling contract))
  (when (seq (set/intersection (:preserves contract) (:invalidates contract)))
    (fail! "C1-EVIDENCE-DROP" "a pass cannot preserve and invalidate one fact"
           {:facts (vec (sort (set/intersection (:preserves contract)
                                                (:invalidates contract))))}))
  (let [unregenerated (set/difference (:invalidates contract)
                                      (:regenerates contract))
        replacement-facts (set (keys (:replacement-evidence contract)))]
    (when-not (= unregenerated replacement-facts)
      (fail! "C1-EVIDENCE-DROP"
             "replacement evidence must cover exactly unregenerated invalidations"
             {:facts (vec (sort unregenerated))
              :replacement-facts (vec (sort replacement-facts))})))
  (canonical-bytes contract)
  contract))

(defn canonical-pass-contract
  "Return the stable semantic projection used to identify a pass contract."
  [contract]
  (validate-pass-contract! contract)
  (into (sorted-map)
        (map (fn [[key value]]
               [key (cond
                      (set? value) (canonical-sort value)
                      (map? value) (into (sorted-map) value)
                      :else value)]))
        contract))

(defn pass-contract-id
  "Return the content identity of one validated pass contract."
  [contract]
  (content-id :gravity/pass-contract-v1 (canonical-pass-contract contract)))

(defn- validate-operations!
  [operations expected id]
  (exact-map! operations expected id :operations)
  (doseq [[key operation] operations]
    (when-not (fn? operation)
      (fail! id "pass execution operations must be functions"
             {:operation key :observed-class (some-> operation class .getName)})))
  operations)

(defn- validate-semantic-bindings!
  [bindings]
  (exact-map! bindings semantic-binding-fields "C16-KEY" :semantic-bindings)
  (doseq [[field value] bindings]
    (require-sha256! field value))
  bindings)

(defn- validate-provenance!
  [provenance]
  (exact-map! provenance provenance-fields "C16-ENTRY" :provenance)
  (require-sha256! :provenance-id (:provenance-id provenance))
  (when-not (or (nil? (:source-path provenance))
                (string? (:source-path provenance)))
    (fail! "C16-ENTRY" "source path metadata must be a string or nil" {}))
  (when-not (map? (:metadata provenance))
    (fail! "C16-ENTRY" "provenance metadata must be a map" {}))
  (canonical-bytes (:metadata provenance))
  provenance)

(defn- validate-request-authority!
  [authority ceiling input-artifact-ids]
  (exact-map! authority request-authority-fields "C16-POLICY" :authority)
  (let [input-authorities (:input-authorities authority)
        _ (when-not (and (map? input-authorities)
                         (= (set input-artifact-ids)
                            (set (keys input-authorities))))
            (fail! "C16-POLICY"
                   "input authority must bind every and only input artifact id"
                   {:input-artifact-ids input-artifact-ids
                    :bound-artifact-ids (when (map? input-authorities)
                                          (vec (sort (keys input-authorities))))}))
        levels (mapv (fn [[artifact-id level]]
                       (require-sha256! :input-authority-artifact-id artifact-id)
                       (authority-level! :input-authority-level level))
                     input-authorities)
        claimed (authority-level! :claimed-level (:claimed-level authority))]
    (let [scope (:scope authority)]
      (when-not (or (and (keyword? scope) (not (empty? (name scope))))
                    (and (string? scope)
                         (some #(not (Character/isWhitespace ^char %)) scope)))
        (fail! "C16-POLICY" "authority scope must be explicit and nonblank"
               {:scope scope})))
    (let [maximum (weakest-authority (conj levels ceiling))]
      (when (> (authority-rank claimed) (authority-rank maximum))
        (fail! "C16-POLICY" "pass receipt would widen authority"
               {:claimed claimed :maximum maximum}))))
  authority)

(defn- validate-input-artifact-ids!
  [input-artifact-ids]
  (sorted-sha-vector! :input-artifact-ids input-artifact-ids)
  (when (empty? input-artifact-ids)
    (fail! "D1-ARTIFACT-GAP"
           "this execution wave requires at least one input artifact"
           {:input-artifact-ids input-artifact-ids
            :remediation
            "provide an input artifact; source-authority roots are not yet supported"}))
  input-artifact-ids)

(defn- validate-external-root-inputs!
  [external-root-inputs input-artifact-ids input-facts input-kind]
  (when-not (map? external-root-inputs)
    (fail! "D1-ARTIFACT-GAP"
           "external roots must map artifact ids to exact descriptors"
           {:observed external-root-inputs}))
  (doseq [[artifact-id descriptor] external-root-inputs]
    (require-sha256! :external-root-artifact-id artifact-id)
    (exact-map! descriptor external-root-fields "D1-ARTIFACT-GAP"
                :external-root)
    (when-not (keyword? (:kind descriptor))
      (fail! "D1-ARTIFACT-GAP" "external-root kind must be a keyword"
             {:artifact-id artifact-id :kind (:kind descriptor)}))
    (when-not (= input-kind (:kind descriptor))
      (fail! "C1-PASS-CONTRACT"
             "external-root kind does not match the consumer input contract"
             {:artifact-id artifact-id
              :external-root-kind (:kind descriptor)
              :consumer-input input-kind}))
    (keyword-set! :external-root-facts (:facts descriptor)))
  (when-not (set/subset? (set (keys external-root-inputs))
                         (set input-artifact-ids))
    (fail! "D1-ARTIFACT-GAP"
           "external roots must be declared input artifacts"
           {:external-root-input-ids (vec (sort (keys external-root-inputs)))}))
  (let [external-facts (reduce set/union #{}
                               (map :facts (vals external-root-inputs)))]
    (when-not (set/subset? external-facts input-facts)
      (fail! "C1-EVIDENCE-DROP"
             "external-root facts must be present in pass input facts"
             {:external-facts external-facts :input-facts input-facts})))
  external-root-inputs)

(defn- validate-request!
  [request]
  (preflight-canonical! request)
  (exact-map! request execution-request-fields "C16-KEY" :request)
  (let [contract (validate-pass-contract! (:contract request))]
    (when-not (= (:stage request) (:pass contract))
      (fail! "D1-PIPELINE-ORDER" "request stage differs from its pass contract"
             {:stage (:stage request) :contract-pass (:pass contract)}))
    (require-sha256! :producer-binding-id (:producer-binding-id request))
    (keyword-set! :input-facts (:input-facts request))
    (validate-input-artifact-ids! (:input-artifact-ids request))
    (validate-external-root-inputs! (:external-root-inputs request)
                                    (:input-artifact-ids request)
                                    (:input-facts request)
                                    (:input contract))
    (when-not (set/subset? (:requires contract) (:input-facts request))
      (fail! "C1-PASS-CONTRACT" "pass input lacks required facts"
             {:missing-facts (vec (sort (set/difference (:requires contract)
                                                       (:input-facts request))))}))
    (when-not (set/subset? (:preserves contract) (:input-facts request))
      (fail! "C1-EVIDENCE-DROP" "a pass cannot preserve absent input facts"
             {:missing-facts (vec (sort (set/difference (:preserves contract)
                                                       (:input-facts request))))}))
    (validate-semantic-bindings! (:semantic-bindings request))
    (doseq [field [:dependency-graph-id :build-effect-replay-id :profile-id
                   :target-id :diagnostic-stream-id]]
      (require-sha256! field (get request field)))
    (sorted-sha-vector! :policy-ids (:policy-ids request))
    (validate-provenance! (:provenance request))
    (when-not (= :executed (:execution-mode request))
      (fail! "C16-ENTRY"
             "execute-pass! can only attest a producer execution"
             {:observed (:execution-mode request)}))
    (validate-request-authority! (:authority request)
                                 (:authority-ceiling contract)
                                 (:input-artifact-ids request))
    (canonical-bytes request))
  request)

(defn- validate-verifier-report-shape!
  [report output-id stage]
  (exact-map! report verifier-report-fields "C18-EVIDENCE" :verifier-report)
  (require-sha256! :verifier-id (:verifier-id report))
  (when-not (and (= stage (:stage report))
                 (= output-id (:artifact-id report))
                 (= :passed (:status report)))
    (fail! "C18-VALIDATION" "pass verifier report did not accept this output"
           {:report report :stage stage :artifact-id output-id}))
  report)

(defn- validate-evidence-record-shape!
  [record output-id]
  (exact-map! record evidence-record-fields "C18-EVIDENCE" :evidence-record)
  (require-sha256! :evidence-id (:evidence-id record))
  (when-not (keyword? (:kind record))
    (fail! "C18-EVIDENCE" "evidence kind must be a keyword" {:record record}))
  (when-not (and (= output-id (:artifact-id record))
                 (= :accepted (:status record)))
    (fail! "C18-EVIDENCE" "pass evidence did not accept this output"
           {:record record :artifact-id output-id}))
  (authority-level! :authority-level (:authority-level record))
  record)

(defn- output-facts
  [contract input-facts]
  (set/union (set/intersection input-facts (:preserves contract))
             (:regenerates contract)))

(defn- receipt-id-projection
  [receipt]
  (-> receipt
      (dissoc :receipt-id)
      (assoc :provenance-id (get-in receipt [:provenance :provenance-id]))
      (dissoc :provenance)))

(defn- calculated-receipt-id
  [receipt]
  (content-id :gravity/pass-execution-receipt-v1
              (receipt-id-projection receipt)))

(defn execute-pass!
  "Execute and validate one injected pass exactly once, then emit its receipt."
  [request operations]
  (binding [*diagnostic-context*
            (merge *diagnostic-context*
                   {:pass (:stage request)
                    :artifact-id (first (:input-artifact-ids request))
                    :profile-id (:profile-id request)
                    :target-id (:target-id request)})]
    (let [request (validate-request! request)
        operations (validate-operations! operations execute-operation-fields
                                         "C16-ENTRY")
        contract (:contract request)
        produced ((:produce! operations) request)
        artifact ((:validate-output! operations) produced request contract)
        output-id ((:artifact-id-of operations) artifact)
        _ (require-sha256! :output-artifact-id output-id)
        verifier-reports ((:verifier-reports operations)
                          artifact request contract)
        evidence-records ((:evidence-records operations)
                          artifact request contract)
        _ (when-not (vector? verifier-reports)
            (fail! "C18-EVIDENCE" "verifier operation must return a vector"
                   {:pass (:pass contract)}))
        _ (when-not (vector? evidence-records)
            (fail! "C18-EVIDENCE" "evidence operation must return a vector"
                   {:pass (:pass contract)}))
        _ (when (or (> (count verifier-reports) maximum-evidence-records)
                    (> (count evidence-records) maximum-evidence-records))
            (fail! "C18-EVIDENCE" "pass evidence exceeds its record bound"
                   {:pass (:pass contract)
                    :maximum-records maximum-evidence-records}))
        _ (doseq [report verifier-reports]
            (validate-verifier-report-shape! report output-id (:stage request)))
        _ (doseq [record evidence-records]
            (validate-evidence-record-shape! record output-id))
        _ (when-not (= (count verifier-reports)
                       (count (distinct (map :verifier-id verifier-reports))))
            (fail! "C18-EVIDENCE" "verifier reports contain duplicate identities"
                   {:pass (:pass contract)}))
        _ (when-not (= (count evidence-records)
                       (count (distinct (map :evidence-id evidence-records))))
            (fail! "C18-EVIDENCE" "evidence records contain duplicate identities"
                   {:pass (:pass contract)}))
        _ (when-not (= (count evidence-records)
                       (count (distinct (map :kind evidence-records))))
            (fail! "C18-EVIDENCE" "evidence records contain duplicate kinds"
                   {:pass (:pass contract)}))
        _ (when (and (:verifier-required? contract)
                     (empty? verifier-reports))
            (fail! "C18-EVIDENCE" "required pass verifier evidence is missing"
                   {:pass (:pass contract)}))
        observed-evidence (set (map :kind evidence-records))
        missing-evidence (set/difference (set/union
                                          (:required-evidence contract)
                                          (set (vals
                                                (:replacement-evidence contract))))
                                         observed-evidence)
        _ (when (seq missing-evidence)
            (fail! "C18-EVIDENCE" "required pass evidence is missing"
                   {:pass (:pass contract)
                    :missing-evidence (vec (sort missing-evidence))}))
        authority-request (:authority request)
        ceiling (:authority-ceiling contract)
        claimed (:claimed-level authority-request)
        effective (weakest-authority
                   (into (conj (vec (vals (:input-authorities authority-request)))
                               ceiling claimed)
                         (map :authority-level evidence-records)))
        receipt-base
        {:artifact :gravity/pass-execution-receipt
         :schema-version 1
         :stage (:stage request)
         :pass-contract-id (pass-contract-id contract)
         :producer-binding-id (:producer-binding-id request)
         :input-artifact-ids (:input-artifact-ids request)
         :external-root-inputs (:external-root-inputs request)
         :output-artifact-id output-id
         :input-facts (:input-facts request)
         :output-facts (output-facts contract (:input-facts request))
         :requires (:requires contract)
         :preserves (:preserves contract)
         :invalidates (:invalidates contract)
         :regenerates (:regenerates contract)
         :replacement-evidence (:replacement-evidence contract)
         :effects (:effects contract)
         :semantic-bindings (:semantic-bindings request)
         :dependency-graph-id (:dependency-graph-id request)
         :build-effect-replay-id (:build-effect-replay-id request)
         :profile-id (:profile-id request)
         :target-id (:target-id request)
         :policy-ids (:policy-ids request)
         :provenance (:provenance request)
         :diagnostic-stream-id (:diagnostic-stream-id request)
         :verifier-reports verifier-reports
         :evidence-records evidence-records
         :execution-mode :executed
         :authority
         {:input-authorities (:input-authorities authority-request)
          :claimed-level claimed
          :effective-level effective
          :ceiling ceiling
          :scope (:scope authority-request)
          :authority-contribution? false
          :aggregate-authoritative? false}}
        receipt (assoc receipt-base :receipt-id
                       (calculated-receipt-id receipt-base))]
      {:artifact artifact :receipt receipt})))

(defn- validate-receipt-structure!
  [receipt contract]
  (preflight-canonical! receipt)
  (exact-map! receipt receipt-fields "C16-ENTRY" :receipt)
  (validate-pass-contract! contract)
  (when-not (and (= :gravity/pass-execution-receipt (:artifact receipt))
                 (= 1 (:schema-version receipt))
                 (= (:pass contract) (:stage receipt))
                 (= (pass-contract-id contract) (:pass-contract-id receipt)))
    (fail! "C16-ENTRY" "receipt does not match its pass contract"
           {:stage (:stage receipt) :pass (:pass contract)}))
  (doseq [field [:receipt-id :producer-binding-id :output-artifact-id
                 :dependency-graph-id :build-effect-replay-id :profile-id
                 :target-id :diagnostic-stream-id]]
    (require-sha256! field (get receipt field)))
  (validate-input-artifact-ids! (:input-artifact-ids receipt))
  (sorted-sha-vector! :policy-ids (:policy-ids receipt))
  (doseq [field [:input-facts :output-facts :requires :preserves :invalidates
                 :regenerates :effects]]
    (keyword-set! field (get receipt field)))
  (validate-external-root-inputs! (:external-root-inputs receipt)
                                  (:input-artifact-ids receipt)
                                  (:input-facts receipt)
                                  (:input contract))
  (validate-semantic-bindings! (:semantic-bindings receipt))
  (validate-provenance! (:provenance receipt))
  (when-not (= :executed (:execution-mode receipt))
    (fail! "C16-ENTRY" "only executed receipts are supported in this wave"
           {:observed (:execution-mode receipt)}))
  (when-not (= (:receipt-id receipt) (calculated-receipt-id receipt))
    (fail! "C16-STALE" "receipt content identity does not recompute"
           {:observed (:receipt-id receipt)}))
  (when-not (and (= (:requires contract) (:requires receipt))
                 (= (:preserves contract) (:preserves receipt))
                 (= (:invalidates contract) (:invalidates receipt))
                 (= (:regenerates contract) (:regenerates receipt))
                 (= (:replacement-evidence contract)
                    (:replacement-evidence receipt))
                 (= (:effects contract) (:effects receipt))
                 (set/subset? (:requires contract) (:input-facts receipt))
                 (set/subset? (:preserves contract) (:input-facts receipt))
                 (= (:output-facts receipt)
                    (output-facts contract (:input-facts receipt))))
    (fail! "C1-EVIDENCE-DROP" "receipt fact flow differs from its contract"
           {:pass (:pass contract)}))
  (when-not (vector? (:verifier-reports receipt))
    (fail! "C18-EVIDENCE" "verifier reports must be a vector" {}))
  (when-not (vector? (:evidence-records receipt))
    (fail! "C18-EVIDENCE" "evidence records must be a vector" {}))
  (when (or (> (count (:verifier-reports receipt)) maximum-evidence-records)
            (> (count (:evidence-records receipt)) maximum-evidence-records))
    (fail! "C18-EVIDENCE" "receipt evidence exceeds its record bound"
           {:maximum-records maximum-evidence-records}))
  (doseq [report (:verifier-reports receipt)]
    (validate-verifier-report-shape! report (:output-artifact-id receipt)
                                     (:stage receipt)))
  (doseq [record (:evidence-records receipt)]
    (validate-evidence-record-shape! record (:output-artifact-id receipt)))
  (when-not (= (count (:verifier-reports receipt))
               (count (distinct (map :verifier-id
                                     (:verifier-reports receipt)))))
    (fail! "C18-EVIDENCE" "verifier reports contain duplicate identities" {}))
  (when-not (= (count (:evidence-records receipt))
               (count (distinct (map :evidence-id
                                     (:evidence-records receipt)))))
    (fail! "C18-EVIDENCE" "evidence records contain duplicate identities" {}))
  (when-not (= (count (:evidence-records receipt))
               (count (distinct (map :kind (:evidence-records receipt)))))
    (fail! "C18-EVIDENCE" "evidence records contain duplicate kinds" {}))
  (when (and (:verifier-required? contract)
             (empty? (:verifier-reports receipt)))
    (fail! "C18-EVIDENCE" "required pass verifier evidence is missing" {}))
  (let [observed (set (map :kind (:evidence-records receipt)))
        required (set/union (:required-evidence contract)
                            (set (vals (:replacement-evidence contract))))
        missing (set/difference required observed)]
    (when (seq missing)
      (fail! "C18-EVIDENCE" "required pass evidence is missing"
             {:missing-evidence (vec (sort missing))})))
  (let [authority (:authority receipt)]
    (exact-map! authority receipt-authority-fields "C16-POLICY"
                :receipt-authority)
    (let [input-authorities (:input-authorities authority)
          _ (when-not (and (map? input-authorities)
                           (= (set (:input-artifact-ids receipt))
                              (set (keys input-authorities))))
              (fail! "C16-POLICY"
                     "receipt authority does not bind its exact inputs" {}))
          levels (mapv (fn [[artifact-id level]]
                         (require-sha256! :input-authority-artifact-id artifact-id)
                         (authority-level! :input-authority-level level))
                       input-authorities)
          claimed (authority-level! :claimed-level (:claimed-level authority))
          effective (authority-level! :effective-level
                                      (:effective-level authority))
          ceiling (authority-level! :ceiling (:ceiling authority))
          maximum (weakest-authority (conj levels ceiling))
          expected-effective
          (weakest-authority
           (into (conj levels ceiling claimed)
                 (map :authority-level (:evidence-records receipt))))]
      (when-not (and (= ceiling (:authority-ceiling contract))
                     (= expected-effective effective)
                     (<= (authority-rank effective) (authority-rank maximum))
                     (let [scope (:scope authority)]
                       (or (and (keyword? scope)
                                (not (empty? (name scope))))
                           (and (string? scope)
                                (some #(not (Character/isWhitespace ^char %))
                                      scope))))
                     (false? (:authority-contribution? authority))
                     (false? (:aggregate-authoritative? authority)))
        (fail! "C16-POLICY" "receipt authority is incomplete or widened"
               {:authority authority :maximum maximum}))))
  receipt)

(defn validate-execution-receipt!
  "Revalidate one receipt and invoke each supplied evidence validator once."
  [receipt contract operations]
  (binding [*diagnostic-context*
            (merge *diagnostic-context*
                   {:pass (:stage receipt)
                    :artifact-id (:output-artifact-id receipt)
                    :profile-id (:profile-id receipt)
                    :target-id (:target-id receipt)})]
    (let [operations (validate-operations!
                    operations receipt-validation-operation-fields
                    "C16-ENTRY")
        receipt (validate-receipt-structure! receipt contract)]
      ((:validate-diagnostic-stream! operations)
       (:diagnostic-stream-id receipt) receipt)
      (doseq [report (:verifier-reports receipt)]
        ((:validate-verifier-report! operations) report receipt))
      (doseq [record (:evidence-records receipt)]
        ((:validate-evidence-record! operations) record receipt))
      receipt)))

(defn- detect-cycle
  [edges]
  (let [state (atom {})
        stack (atom [])
        found (atom nil)]
    (letfn [(visit [node]
              (when-not @found
                (swap! state assoc node :active)
                (swap! stack conj node)
                (doseq [next-node (get edges node [])]
                  (case (get @state next-node)
                    :active (reset! found (conj (vec (drop-while
                                                     #(not= % next-node)
                                                     @stack))
                                                next-node))
                    :done nil
                    (visit next-node)))
                (swap! stack pop)
                (swap! state assoc node :done)))]
      (doseq [node (keys edges)]
        (when-not (get @state node) (visit node)))
      @found)))

(defn- dag-id-projection
  [dag]
  (-> dag
      (dissoc :evidence-root-id)
      (update :receipts
              (fn [receipts]
                (mapv (fn [receipt]
                        (-> receipt
                            (assoc :provenance-id
                                   (get-in receipt [:provenance :provenance-id]))
                            (dissoc :provenance)))
                      receipts)))))

(defn compose-evidence-dag
  "Validate and compose an order-invariant evidence DAG of pass receipts."
  [receipts contracts]
  (when-not (and (vector? receipts) (seq receipts)
                 (vector? contracts) (seq contracts))
    (fail! "D1-ARTIFACT-GAP" "receipts and contracts must be nonempty vectors"
           {}))
  (when (or (> (count receipts) maximum-dag-receipts)
            (> (count contracts) maximum-dag-receipts))
    (fail! "D1-ARTIFACT-GAP" "pass evidence DAG exceeds its receipt bound"
           {:maximum-receipts maximum-dag-receipts}))
  (doseq [contract contracts] (validate-pass-contract! contract))
  (let [contracts-by-id (into {} (map (juxt pass-contract-id identity) contracts))]
    (when-not (= (count contracts) (count contracts-by-id))
      (fail! "C1-PASS-CONTRACT" "pass contracts contain duplicate identities" {}))
    (when-not (= (count contracts) (count (distinct (map :pass contracts))))
      (fail! "C1-PASS-CONTRACT" "pass contracts contain duplicate pass ids" {}))
    (when-not (= (count contracts) (count (distinct (map :order contracts))))
      (fail! "D1-PIPELINE-ORDER" "pass contracts contain duplicate orders" {}))
    (let [receipts (mapv (fn [receipt]
                           (binding [*diagnostic-context*
                                     (merge *diagnostic-context*
                                            {:pass (:stage receipt)
                                             :artifact-id
                                             (:output-artifact-id receipt)
                                             :profile-id (:profile-id receipt)
                                             :target-id (:target-id receipt)})]
                             (if-let [contract
                                      (get contracts-by-id
                                           (:pass-contract-id receipt))]
                               (validate-receipt-structure! receipt contract)
                               (fail! "C1-PASS-CONTRACT"
                                      "receipt names an unknown pass contract"
                                      {:pass-contract-id
                                       (:pass-contract-id receipt)}))))
                         receipts)
          receipts-by-id (into {} (map (juxt :receipt-id identity) receipts))
          producers-by-output (into {} (map (juxt :output-artifact-id identity)
                                            receipts))]
      (when-not (= (count receipts) (count receipts-by-id))
        (fail! "C16-ENTRY" "evidence DAG contains duplicate receipts" {}))
      (when-not (= (count receipts) (count producers-by-output))
        (fail! "D1-ARTIFACT-GAP" "multiple receipts produce one artifact id" {}))
      (let [used-contracts (set (map :pass-contract-id receipts))]
        (when-not (= used-contracts (set (keys contracts-by-id)))
          (fail! "C1-PASS-CONTRACT"
                 "evidence DAG contains unused pass contracts"
                 {:unused-contract-ids
                  (vec (sort (set/difference (set (keys contracts-by-id))
                                             used-contracts)))})))
      (doseq [receipt receipts
              input-id (:input-artifact-ids receipt)]
        (let [producer (get producers-by-output input-id)
              external? (contains? (:external-root-inputs receipt) input-id)
              observed-authority (get-in receipt
                                         [:authority :input-authorities input-id])]
          (if producer
            (let [producer-contract
                  (get contracts-by-id (:pass-contract-id producer))
                  consumer-contract
                  (get contracts-by-id (:pass-contract-id receipt))]
              (when external?
                (fail! "D1-ARTIFACT-GAP"
                       "an internally produced input cannot be an external root"
                       {:artifact-id input-id :pass (:stage receipt)}))
              (when-not (= (:output producer-contract)
                           (:input consumer-contract))
                (fail! "C1-PASS-CONTRACT"
                       "producer output IR does not match consumer input IR"
                       {:artifact-id input-id
                        :producer-output (:output producer-contract)
                        :consumer-input (:input consumer-contract)}))
              (when-not (= observed-authority
                           (get-in producer [:authority :effective-level]))
                (fail! "C16-POLICY"
                       "internal edge authority differs from producer authority"
                       {:artifact-id input-id
                        :observed observed-authority
                        :expected (get-in producer
                                          [:authority :effective-level])})))
            (when-not external?
              (fail! "D1-ARTIFACT-GAP"
                     "input has no producer and is not an explicit external root"
                     {:artifact-id input-id :pass (:stage receipt)})))))
      (let [predecessors
            (into {}
                  (map (fn [receipt]
                         [(:receipt-id receipt)
                          (->> (:input-artifact-ids receipt)
                               (keep #(some-> (get producers-by-output %)
                                              :receipt-id))
                               set)]))
                  receipts)
            successors
            (reduce-kv
             (fn [result child parents]
               (reduce #(update %1 %2 (fnil conj #{}) child) result parents))
             (zipmap (keys predecessors) (repeat #{}))
             predecessors)
            cycle (detect-cycle successors)]
        (when cycle
          (fail! "D1-PIPELINE-ORDER" "pass evidence graph contains a cycle"
                 {:cycle cycle}))
        (doseq [receipt receipts
                predecessor-id (get predecessors (:receipt-id receipt))
                :let [predecessor (get receipts-by-id predecessor-id)
                      predecessor-contract
                      (get contracts-by-id (:pass-contract-id predecessor))
                      contract (get contracts-by-id
                                    (:pass-contract-id receipt))]]
          (when-not (< (:order predecessor-contract) (:order contract))
            (fail! "D1-PIPELINE-ORDER"
                   "pass receipt consumes an artifact out of canonical order"
                   {:producer (:stage predecessor)
                    :consumer (:stage receipt)})))
        (let [roots (filterv #(empty? (get predecessors (:receipt-id %)))
                             receipts)
              sinks (filterv #(empty? (get successors (:receipt-id %)))
                             receipts)]
          (when-not (and (= 1 (count roots)) (= 1 (count sinks)))
            (fail! "D1-ARTIFACT-GAP"
                   "pass evidence must form one connected rooted DAG"
                   {:root-count (count roots) :sink-count (count sinks)}))
          (let [reachable
                (loop [pending [(:receipt-id (first roots))] seen #{}]
                  (if-let [node (peek pending)]
                    (if (contains? seen node)
                      (recur (pop pending) seen)
                      (recur (into (pop pending) (get successors node))
                             (conj seen node)))
                    seen))]
            (when-not (= reachable (set (keys receipts-by-id)))
              (fail! "D1-ARTIFACT-GAP" "pass evidence DAG is disconnected"
                     {:unreachable
                      (vec (sort (set/difference
                                  (set (keys receipts-by-id)) reachable)))})))
          (doseq [receipt receipts
                  :let [parents (get predecessors (:receipt-id receipt))]]
            (let [parent-facts (map #(get-in receipts-by-id [% :output-facts])
                                    parents)
                  external-facts (map :facts
                                      (vals (:external-root-inputs receipt)))
                  expected-input-facts
                  (reduce set/union #{} (concat parent-facts external-facts))]
              (when-not (= expected-input-facts (:input-facts receipt))
                (fail! "C1-EVIDENCE-DROP"
                       "consumer facts do not equal predecessor output facts"
                       {:pass (:stage receipt)
                        :expected expected-input-facts
                        :observed (:input-facts receipt)}))))
          (let [ordered-receipts (->> receipts
                                      (sort-by (fn [receipt]
                                                 [(get-in contracts-by-id
                                                          [(:pass-contract-id receipt)
                                                           :order])
                                                  (:receipt-id receipt)]))
                                      vec)
                ordered-contracts (->> contracts
                                       (sort-by (juxt :order pass-contract-id))
                                       vec)
                effective-level
                (weakest-authority
                 (mapv #(get-in % [:authority :effective-level]) receipts))
                dag-base
                {:artifact :gravity/pass-evidence-dag
                 :schema-version 1
                 :root-receipt-id (:receipt-id (first sinks))
                 :receipts ordered-receipts
                 :contracts ordered-contracts
                 :edges (->> predecessors
                             (mapcat (fn [[child parents]]
                                       (map (fn [parent]
                                              {:from parent :to child})
                                            parents)))
                             (sort-by (juxt :from :to))
                             vec)
                 :authority
                 {:effective-level effective-level
                  :authority-contribution? false
                  :aggregate-authoritative? false}}]
            (assoc dag-base :evidence-root-id
                   (content-id :gravity/pass-evidence-dag-v1
                               (dag-id-projection dag-base)))))))))

(defn evidence-root
  "Exact-validate, recompose, and return a pass evidence DAG semantic root."
  [dag]
  (exact-map! dag evidence-dag-fields "C16-ENTRY" :evidence-dag)
  ;; exact-map! performs the non-materializing operational bound first;
  ;; canonical validation then rejects host metadata or unsupported values at
  ;; any nested DAG location before trusted receipt recomposition begins.
  (canonical-bytes dag)
  (when-not (and (= :gravity/pass-evidence-dag (:artifact dag))
                 (= 1 (:schema-version dag))
                 (sha256-id? (:root-receipt-id dag))
                 (sha256-id? (:evidence-root-id dag))
                 (vector? (:receipts dag))
                 (vector? (:contracts dag))
                 (vector? (:edges dag)))
    (fail! "C16-ENTRY" "pass evidence DAG envelope is malformed" {}))
  (exact-map! (:authority dag) evidence-dag-authority-fields
              "C16-POLICY" :evidence-dag-authority)
  (doseq [edge (:edges dag)]
    (exact-map! edge #{:from :to} "D1-ARTIFACT-GAP" :evidence-edge)
    (require-sha256! :edge-from (:from edge))
    (require-sha256! :edge-to (:to edge)))
  (let [recomposed (compose-evidence-dag (:receipts dag) (:contracts dag))]
    (when-not (= recomposed dag)
      (fail! "C16-STALE"
             "pass evidence DAG differs from exact canonical recomposition"
             {:observed-root (:evidence-root-id dag)
              :expected-root (:evidence-root-id recomposed)}))
    (:evidence-root-id recomposed)))

(defn pass-execution-contract
  "Return the private machine contract for this non-authoritative leaf."
  []
  namespace-contract)
