(ns gravity.providers.declarations
  "Pure bootstrap declaration records for L15 Provider Declaration.
  Validates explicit fields, not provider behavior, selection, trust or grants.
  Input is decoded Clojure data from a trusted host caller, not a guest sandbox."
  (:require [clojure.string :as str]))

(defn- text? [x] (and (string? x) (not (str/blank? x))))
(defn- reference? [x] (or (qualified-keyword? x) (qualified-symbol? x)))
(defn- keyword-set? [x] (and (set? x) (every? keyword? x)))
(defn- nonempty-keyword-set? [x] (and (keyword-set? x) (seq x)))
(defn- contracts? [x]
  (and (vector? x) (seq x) (every? qualified-symbol? x)))
(defn- failures? [x]
  (and (set? x) (every? reference? x)))

;; Behavior references name provider-specific contracts. Resolving and checking
;; those contracts belongs to later compatibility/conformance consumers.
(def ^:private declaration-fields
  [[:provider-id qualified-symbol? :qualified-symbol]
   [:version text? :nonblank-string]
   [:kind keyword? :keyword]
   [:implements nonempty-keyword-set? :nonempty-keyword-set]
   [:profiles nonempty-keyword-set? :nonempty-keyword-set]
   [:targets nonempty-keyword-set? :nonempty-keyword-set]
   [:runtime-effects keyword-set? :keyword-set]
   [:build-effects keyword-set? :keyword-set]
   [:requires keyword-set? :keyword-set]
   [:contracts contracts? :nonempty-contract-vector]
   [:failures failures? :failure-type-set]
   [:scope-policy reference? :contract-reference]
   [:blocking reference? :contract-reference]
   [:concurrency reference? :contract-reference]
   [:resource-behavior reference? :contract-reference]
   [:memory-behavior reference? :contract-reference]
   [:determinism reference? :contract-reference]
   [:replay reference? :contract-reference]
   [:artifact-schema reference? :schema-reference]
   [:conformance-suite reference? :suite-reference]
   [:trust-level #{:trusted-core :trusted-package :local-dev :sandboxed :untrusted}
    :declared-trust-level]])

(defn- coordinate? [x] (and (integer? x) (pos? x)))
(defn- span? [x]
  (and (map? x) (coordinate? (:line x)) (coordinate? (:column x))))
(defn- manifest-entry? [x]
  (and (vector? x) (seq x)
       (every? #(or (keyword? %) (symbol? %) (text? %)
                    (and (integer? %) (not (neg? %)))) x)))

(defn- public-context [context]
  ;; Copy only valid public identity/location fields. Never echo arbitrary
  ;; context maps, source text, scopes, failed values or declaration payloads.
  (if-not (map? context)
    {}
    (cond-> {}
      (text? (:source-id context)) (assoc :source-id (:source-id context))
      (span? (:source-span context))
      (assoc :source-span (select-keys (:source-span context) [:line :column]))
      (manifest-entry? (:manifest-entry context))
      (assoc :manifest-entry (:manifest-entry context))
      (boolean? (:signature-policy-required? context))
      (assoc :signature-policy-required? (:signature-policy-required? context))
      (keyword? (:profile context)) (assoc :profile (:profile context))
      (keyword? (:target context)) (assoc :target (:target context)))))

(defn- diagnostic [context path reason expected]
  {:id "L15-CONTRACT" :message "Invalid explicit provider declaration"
   :path path :reason reason :expected expected :context context})

(defn- field-errors [data prefix fields context]
  (into []
        (keep (fn [[field valid? expected]]
                (cond
                  (not (contains? data field))
                  (diagnostic context [prefix field] :missing-field expected)
                  (not (valid? (get data field)))
                  (diagnostic context [prefix field] :invalid-field expected))))
        fields))

(defn validate-declaration
  "Return :valid or :invalid with deterministic, payload-free diagnostics.
  Context explicitly supplies signature-policy applicability and public source
  identity plus a span or manifest entry. No policy or conformance is verified."
  [declaration context]
  (let [public (public-context context)
        context-errors
        (if-not (map? context)
          [(diagnostic public [:context] :invalid-field :map)]
          (into
           (field-errors context :context
                         [[:source-id text? :nonblank-string]
                          [:signature-policy-required? boolean? :boolean]] public)
           (concat
            (when-not (or (span? (:source-span context))
                          (manifest-entry? (:manifest-entry context)))
              [(diagnostic public [:context] :missing-source-location
                           :source-span-or-manifest-entry)])
            (field-errors context :context
                          (filterv #(contains? context (first %))
                                   [[:source-span span? :source-span]
                                    [:manifest-entry manifest-entry? :manifest-entry]
                                    [:profile keyword? :keyword]
                                    [:target keyword? :keyword]]) public))))
        declaration-errors
        (if-not (map? declaration)
          [(diagnostic public [:declaration] :invalid-field :map)]
          (field-errors
           declaration :declaration
           (cond-> declaration-fields
             (or (true? (:signature-policy-required? context))
                 (contains? declaration :signature-policy))
             (conj [:signature-policy reference? :signature-policy-reference])) public))
        diagnostics (into context-errors declaration-errors)]
    {:status (if (empty? diagnostics) :valid :invalid)
     :diagnostics diagnostics}))

(defn record-declaration
  "Validate afresh and return an internal declaration record only on success.
  Preserve the supplied declaration, including extensions, without defaults or
  promotion of any supplied complete/trusted/granted/conformant claims.
  This is not a public secret-export or canonical artifact encoding boundary."
  [declaration context]
  (let [result (validate-declaration declaration context)]
    (cond-> result
      (= :valid (:status result))
      (assoc :record {:schema :gravity.provider/declaration-v1
                      :declaration declaration
                      :provenance (public-context context)}))))
