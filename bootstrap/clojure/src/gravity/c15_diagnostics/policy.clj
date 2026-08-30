(ns gravity.c15-diagnostics.policy)
(def function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module :compiler-c14-lowering-source-artifact
    :c15-diagnostics-source-overrides :c15-stable-diagnostic-id :c15-diagnostics-fail!
    :c15-diagnostics-validate-source-overrides! :c15-diagnostic-record
    :c15-diagnostic-catalog :c15-diagnostics-validate! :c15-diagnostics-capability-proof
    :compiler-c15-diagnostics-source-artifact :compiler-c15-diagnostics-file-artifact})
(def scalar-operation-keys
  #{:compiler-verification-diagnostic-messages :compiler-verification-override-diagnostics
    :c15-diagnostics-governing-document :c15-diagnostics-diagnostic-ids :c15-diagnostic-required-fields})
(def operation-keys (into function-operation-keys scalar-operation-keys))
(defn unsupported [key] (fn [& _] (throw (ex-info (str "C15 leaf requires injected operation " key) {:operation key}))))
(defn- string-vector? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- keyword-vector? [v] (and (vector? v) (seq v) (every? keyword? v)))
(defn- string-map? [v] (and (map? v) (every? (fn [[k x]] (and (string? k) (string? x))) v)))
(defn- override-map? [v]
  (and (map? v) (every? (fn [[k x]] (and (keyword? k) (vector? x) (= 2 (count x))
                                          (string? (first x)) (keyword? (second x)))) v)))
(defn validate-operations! [operations]
  (when-not (map? operations) (throw (ex-info "C15 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value] (select-keys operations function-operation-keys)
                           :when (not (fn? value))] key))]
    (when unknown (throw (ex-info "C15 operation map contains unknown keys" {:unknown-keys (vec unknown)})))
    (when invalid (throw (ex-info "C15 function operations must be functions" {:non-function-keys (vec invalid)}))))
  (doseq [[key pred] [[:compiler-verification-diagnostic-messages string-map?]
                       [:compiler-verification-override-diagnostics override-map?]
                       [:c15-diagnostics-governing-document #(and (string? %) (seq %))]
                       [:c15-diagnostics-diagnostic-ids string-vector?]
                       [:c15-diagnostic-required-fields keyword-vector?]]
          :when (and (contains? operations key) (not (pred (get operations key))))]
    (throw (ex-info "C15 scalar operation has invalid shape" {:key key}))) operations)
(defn engine-contract [public-api]
  {:contract-boundary :hosted-stage0-c15-compiler-diagnostics
   :dependency-direction {:requires ['clojure.string 'gravity.compiler-verification-shared 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c15-diagnostic-schema :hosted-stage0-c15-diagnostic-evidence]
   :does-not-own [:canonical-c15-authority :source-authentication :redaction-policy-authority
                  :privacy-authority :localization-authority :renderer-authority
                  :golden-fixture-authority :proof-authority :equivalence :self-hosting
                  :release :seed-retirement]
   :compatibility-only? true :diagnostic-system-complete? false :canonical-c15-authority? false
   :operation-interposition {:accepted-keys operation-keys :unknown-keys-rejected? true
                             :partial-overrides? true :single-binding-per-top-level-call? true}
   :public-api public-api})
