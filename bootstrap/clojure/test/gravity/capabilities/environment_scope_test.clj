(ns gravity.capabilities.environment-scope-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.capabilities.environment-scope :as scope]
            [gravity.providers.declarations :as declarations]))

;; Decoded data only. No environment value, service, callback or live grant.
(def declaration
  {:provider-id 'example.env/fixture :version "1.0" :kind :environment
   :implements #{:env/read} :profiles #{:hosted :meta} :targets #{:jvm}
   :runtime-effects #{:environment/read} :build-effects #{:build/env}
   :requires #{} :contracts ['example.contracts/Environment]
   :failures #{:example.env/denied} :scope-policy :example.env/key-set
   :blocking :example.env/nonblocking :concurrency :example.env/immutable
   :resource-behavior :example.env/no-handles :memory-behavior :example.env/hosted
   :determinism :example.env/recorded :replay :example.env/inputs
   :artifact-schema :example.env/record :conformance-suite :example.env/suite
   :trust-level :local-dev})
(def declaration-context
  {:source-id "fixture:environment-provider" :source-span {:line 1 :column 1}
   :signature-policy-required? false :profile :hosted :target :jvm})
(def request
  {:principal :package/app :capability :env/read
   :provider-id 'example.env/fixture :provider-version "1.0"
   :phase :runtime :profile :hosted :target :jvm
   :source-id "fixture:environment-use" :source-span {:line 12 :column 3}
   :requested-keys #{"COMMON"}})
(def bindings (select-keys request [:principal :capability :provider-id :provider-version
                                    :phase :profile :target]))
(def policies
  (into {} (map (fn [layer]
                  [layer (merge bindings
                                {:policy-source (keyword "example.policy" (name layer))
                                 :allowed-keys #{"COMMON" "EXTRA"} :denied-keys #{}
                                 :redaction-policy :example.redaction/required})])
                scope/policy-layers)))
(defn assess
  ([] (assess request policies))
  ([r p] (scope/assess-scope r p declaration declaration-context)))
(defn fault [result]
  (mapv #(select-keys % [:id :path :reason :policy-layer]) (:diagnostics result)))
(defn expected [id path reason layer]
  [{:id id :path path :reason reason :policy-layer layer}])

(deftest environment-key-intersection
  (let [p (reduce-kv (fn [out index layer]
                       (assoc-in out [layer :allowed-keys]
                                 #{"COMMON" (str "LAYER_" index)}))
                     policies scope/policy-layers)
        calls (atom [])
        producer declarations/record-declaration
        result (with-redefs [declarations/record-declaration
                             (fn [d c] (swap! calls conj [d c]) (producer d c))]
                 (assess request p))]
    (is (= [[declaration declaration-context]] @calls))
    (is (= :within-scope (:status result)))
    (is (= [] (:diagnostics result)))
    (is (= ["COMMON"] (get-in result [:assessment :effective-keys])))
    (is (= (assoc request :requested-keys ["COMMON"])
           (get-in result [:assessment :request])))
    (is (= scope/policy-layers (mapv :layer (get-in result [:assessment :layers]))))
    (is (= 8 (count (get-in result [:assessment :redaction-obligations]))))
    (is (false? (get-in result [:assessment :redaction-enforced?])))
    (is (= #{:status :diagnostics :assessment} (set (keys result))))
    (is (= #{:schema :request :effective-keys :layers :redaction-obligations
             :redaction-enforced?} (set (keys (:assessment result)))))))

(defn reverse-maps [x]
  (cond (map? x) (into (array-map) (map (fn [[k v]] [k (reverse-maps v)]) (reverse (seq x))))
        :else x))

(deftest reordered-maps-produce-equal-results
  (is (= (assess)
         (scope/assess-scope (reverse-maps request) (reverse-maps policies)
                             (reverse-maps declaration) (reverse-maps declaration-context))))
  (let [p (-> policies (assoc-in [:package :denied-keys] #{"COMMON"})
              (assoc-in [:source :allowed-keys] #{}))]
    (is (= (assess request p) (assess (reverse-maps request) (reverse-maps p))))))

(deftest downstream-broadening-cannot-restore-key
  (doseq [layer scope/policy-layers]
    (let [p (assoc-in policies [layer :allowed-keys] #{"COMMON"})
          result (assess request p)
          rows (get-in result [:assessment :layers])]
      (is (= ["COMMON"] (get-in result [:assessment :effective-keys])))
      (is (= ["EXTRA"] (:removed-keys (first (filter #(= layer (:layer %)) rows)))))
      (is (= ["EXTRA"] (:narrowed-keys (first (filter #(= layer (:layer %)) rows)))))
      (is (every? #(not (some #{"EXTRA"} (:effective-keys %)))
                  (drop-while #(not= layer (:layer %)) rows)))))
  ;; Unrequested explicit denials still narrow the assessed scope.
  (is (= ["COMMON"] (get-in (assess request (assoc-in policies [:source :denied-keys] #{"EXTRA"}))
                             [:assessment :effective-keys]))))

(deftest missing-policy-layer
  (doseq [layer scope/policy-layers]
    (let [result (assess request (dissoc policies layer))]
      (is (= :rejected (:status result)))
      (is (not (contains? result :assessment)))
      (is (= (expected "SAFE10-DENIED" [:policies layer] :missing-policy-layer layer)
             (fault result))))))

(deftest explicit-deny-at-each-layer
  (doseq [layer scope/policy-layers]
    (let [result (assess request (assoc-in policies [layer :denied-keys] #{"COMMON"}))
          d (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= (expected "SAFE10-DENIED" [:policies layer] :explicit-deny layer) (fault result)))
      (is (= {:requested-keys ["COMMON"] :effective-keys ["EXTRA"]} (:scope d)))
      (is (= ["COMMON"] (:keys d)))
      (is (= (get-in policies [layer :policy-source]) (:policy-source d)))
      (is (= ["COMMON"] (:denied-keys (first (filter #(= layer (:layer %))
                                                     (get-in result [:assessment :layers])))))))))

(deftest requested-key-outside-intersection
  (doseq [layer scope/policy-layers]
    (let [result (assess request (assoc-in policies [layer :allowed-keys] #{"EXTRA"}))]
      (is (= :rejected (:status result)))
      (is (= (expected "SAFE10-SCOPE" [:policies layer]
                      :requested-key-outside-intersection layer) (fault result)))
      (is (= (assoc request :requested-keys ["COMMON"])
             (:context (first (:diagnostics result)))))))
  ;; Empty sets are explicit policy, never missing authority or a wildcard.
  (is (= [] (get-in (assess (assoc request :requested-keys #{})
                            (assoc-in policies [:source :allowed-keys] #{}))
                   [:assessment :effective-keys]))))

(deftest wrong-phase-and-mismatched-identities
  (doseq [layer scope/policy-layers
          [field value id] [[:phase :build "SAFE10-PHASE"]
                            [:provider-id 'other.env/provider "SAFE10-PROVIDER"]
                            [:provider-version "2.0" "SAFE10-PROVIDER"]
                            [:principal :package/other "SAFE10-DENIED"]
                            [:capability :secret/read "SAFE10-DENIED"]
                            [:profile :native "SAFE10-DENIED"]
                            [:target :wasm "SAFE10-DENIED"]]]
    (let [result (assess request (assoc-in policies [layer field] value))]
      (is (= :rejected (:status result)))
      (is (not (contains? result :assessment)))
      (is (= (expected id [:policies layer field] :identity-mismatch layer) (fault result)))))
  (doseq [phase [:build :runtime]]
    (is (= :within-scope (:status (assess (assoc request :phase phase)
                                         (into {} (map (fn [[k v]] [k (assoc v :phase phase)]) policies))))))))

(deftest mismatched-provider-and-unsupported-applicability
  (doseq [[d c path reason]
          [[(assoc declaration :provider-id 'other.env/provider) declaration-context
            [:declaration] :provider-identity-mismatch]
           [(assoc declaration :version "2.0") declaration-context
            [:declaration] :provider-identity-mismatch]
           [(dissoc declaration :contracts) declaration-context [:declaration] :invalid-declaration]
           [(assoc declaration :implements #{:secret/read}) declaration-context
            [:declaration :implements] :unsupported-capability]
           [(assoc declaration :profiles #{:native}) declaration-context
            [:declaration :profiles] :unsupported-profile]
           [(assoc declaration :targets #{:wasm}) declaration-context
            [:declaration :targets] :unsupported-target]
           [declaration (assoc declaration-context :profile :native)
            [:declaration-context] :applicability-mismatch]
           [declaration (dissoc declaration-context :target)
            [:declaration-context] :applicability-mismatch]]]
    (let [result (scope/assess-scope request policies d c)]
      (is (= :rejected (:status result)))
      (is (= (expected "SAFE10-PROVIDER" path reason nil) (fault result)))
      (is (not (contains? result :assessment)))))
  (let [r (assoc request :capability :secret/read)
        p (into {} (map (fn [[k v]] [k (assoc v :capability :secret/read)]) policies))]
    (is (= (expected "SAFE10-PROVIDER" [:declaration :implements] :unsupported-capability nil)
           (fault (assess r p)))))
  ;; A supplied producer record cannot replace the explicit declaration.
  (is (= :rejected (:status (scope/assess-scope request policies
                                               (:record (declarations/record-declaration declaration declaration-context))
                                               declaration-context)))))

(deftest forged-scope-satisfied
  (let [outside (assoc-in policies [:source :allowed-keys] #{})]
    (is (= "SAFE10-SCOPE" (:id (first (:diagnostics (assess request outside))))))
    (is (= (expected "SAFE10-SCOPE" [:request] :unexpected-fields nil)
           (fault (assess (assoc request :scope-satisfied? true) outside))))
    (doseq [layer scope/policy-layers]
      (is (= (expected "SAFE10-SCOPE" [:policies layer] :unexpected-fields layer)
             (fault (assess request (assoc-in outside [layer :scope-satisfied?] true))))))))

(deftest malformed-inputs-and-secret-marker-controls
  (let [secret "SECRET-MARKER-DO-NOT-EMIT"
        bad {:payload secret}
        cases (concat
               (for [value [nil [] secret]] [value policies declaration declaration-context])
               (for [value [nil [] secret]] [request value declaration declaration-context])
               (for [value [nil [] secret]] [request policies value declaration-context])
               (for [value [nil [] secret]] [request policies declaration value])
               (for [layer scope/policy-layers value [nil [] secret]]
                 [request (assoc policies layer value) declaration declaration-context])
               (for [field (keys request)] [(assoc request field bad) policies declaration declaration-context])
               (for [field (keys request)] [(dissoc request field) policies declaration declaration-context])
               (for [layer scope/policy-layers field (keys (get policies layer))]
                 [request (assoc-in policies [layer field] bad) declaration declaration-context])
               (for [layer scope/policy-layers field (keys (get policies layer))]
                 [request (update policies layer dissoc field) declaration declaration-context])
               [[request (assoc policies bad bad) declaration declaration-context]
                [request policies (assoc declaration :build-effects #{secret}) declaration-context]
                [request policies declaration (assoc declaration-context :source-span bad)]
                [(assoc request :requested-keys #{bad}) policies declaration declaration-context]
                [(assoc request :requested-keys [secret]) policies declaration declaration-context]
                [request (assoc-in policies [:source :allowed-keys] [secret]) declaration declaration-context]
                [request (assoc-in policies [:package :denied-keys] #{bad}) declaration declaration-context]
                [request policies (assoc declaration :secret secret) (assoc declaration-context :secret secret)]
                [(assoc request :source-span (assoc (:source-span request) :payload secret))
                 policies declaration declaration-context]])]
    (doseq [[index [r p d c]] (map-indexed vector cases)]
      (let [result (scope/assess-scope r p d c)]
        (is (= (if (< index (- (count cases) 2)) :rejected :within-scope) (:status result)))
        (when (= :rejected (:status result)) (is (not (contains? result :assessment))))
        (is (not (str/includes? (binding [*print-meta* true] (pr-str result)) secret)))
        (is (= result (scope/assess-scope r p d c)))))
    (doseq [bad-key ["" "A=B" (str "A" (char 0)) (str (char 0xd800))]]
      (is (= :rejected (:status (assess (assoc request :requested-keys #{bad-key}) policies)))))
    (let [symbol (with-meta 'example.public/identity {:payload secret})
          r (assoc request :principal symbol :provider-id (with-meta (:provider-id request) {:payload secret}))
          p (into {} (map (fn [[k v]] [k (assoc v :principal symbol
                                                :policy-source symbol :redaction-policy symbol)]) policies))]
      (doseq [result [(assess r p)
                      (assess r (assoc-in p [:source :denied-keys] #{"COMMON"}))
                      (assess r (assoc-in p [:source :phase] :build))]]
        (is (not (str/includes? (binding [*print-meta* true] (pr-str result)) secret)))))
    ;; Valid Unicode names are not refused by an input quota or ASCII grammar.
    (let [key (str "KEY_" (char 0x3bb))
          p (into {} (map (fn [[k v]] [k (assoc v :allowed-keys #{key})]) policies))]
      (is (= [key] (get-in (assess (assoc request :requested-keys #{key}) p)
                            [:assessment :effective-keys]))))))
