(ns gravity.c9-ownership-checker
  "Hosted Stage0 C9 ownership facade with compatibility operation interposition."
  (:require [gravity.digest :as digest]
            [gravity.c9-ownership-checker.artifact :as artifact]
            [gravity.c9-ownership-checker.catalog :as catalog]
            [gravity.c9-ownership-checker.contract :as contract]
            [gravity.c9-ownership-checker.graphs :as graphs]
            [gravity.c9-ownership-checker.lifecycles :as lifecycles]
            [gravity.c9-ownership-checker.policy :as policy]
            [gravity.c9-ownership-checker.resources :as resources]
            [gravity.c9-ownership-checker.verification :as verification]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index] {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(def ^:private unsupported-host-operation policy/unsupported-host-operation)
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data] ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index] ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact] ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records (unsupported-host-operation :read-source-form-records)) path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax! (unsupported-host-operation :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module)) path forms))
(defn- compiler-c8-effect-source-artifact [path text]
  ((op-fn :compiler-c8-effect-source-artifact
          (unsupported-host-operation :compiler-c8-effect-source-artifact)) path text))

(def ^:dynamic c9-ownership-diagnostic-ids catalog/diagnostic-ids)
(def ^:dynamic c9-ownership-governing-document catalog/governing-document)
(def ^:dynamic c9-ownership-rejected-designs catalog/rejected-designs)
(def ^:dynamic c9-ownership-override-diagnostics catalog/override-diagnostics)

(definterposable c9-ownership-source-overrides [module]
  (get-in module [:metadata :compiler :c9-ownership-check] {}))
(definterposable c9-ownership-message [id] (catalog/ownership-message id))
(definterposable c9-ownership-fail! [id source-path subject extra]
  (fail! id (c9-ownership-message id)
         (merge {:source-span (or (:source-span subject) (get-in subject [:source :span])
                                  (:span subject) (source-span source-path 0))
                 :diagnostic-family :c9-ownership-checker :stage :ownership-lifetime-region-check
                 :document-id "C9" :expected-document c9-ownership-governing-document
                 :value-id (or (:value-id subject) :fixture/value)
                 :owner-id (or (:owner-id subject) :fixture/owner)
                 :borrow-id (or (:borrow-id subject) :fixture/borrow)
                 :region-id (or (:region-id subject) :fixture/region)
                 :arena-generation (or (:arena-generation subject) :fixture/generation)
                 :resource-id (or (:resource-id subject) :fixture/resource)
                 :control-path (or (:control-path subject) :fixture/path)
                 :generated-origin-chain (or (:generated-origin subject) (get-in subject [:source :origin-chain]))
                 :profile (:profile subject) :target (:target subject) :transfer (:transfer subject)
                 :runtime-check (:runtime-check subject) :unsafe-audit (:unsafe-audit subject)
                 :remediation "Emit ownership, borrow, lifetime, region, arena, linear-flow, transfer, runtime-check, unsafe-audit, and diagnostic records before safety analysis and MIR construction."}
                extra)))
(definterposable c9-ownership-validate-overrides! [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c9-ownership-override-diagnostics fail-kind)]
      (c9-ownership-fail! id source-path
                          {:source-span (source-span source-path 0)
                           :value-id (keyword "fixture" (name fail-kind))
                           :owner-id :fixture/owner :borrow-id :fixture/borrow :region-id :fixture/region
                           :arena-generation :fixture/generation :resource-id :fixture/resource
                           :control-path :fixture/path :profile (:profile module) :target (:target module)
                           :generated-origin []}
                          {:missing-fields [fail-kind]}))))
(definterposable c9-node-ids [effect-graph] (vec (keys (:nodes effect-graph))))
(definterposable c9-node [node-ids index fallback] (or (get node-ids index) fallback))
(definterposable c9-ownership-graph [module effect-graph]
  (graphs/ownership-graph source-span c9-node-ids c9-node module effect-graph))
(definterposable c9-borrow-graph [module effect-graph]
  (graphs/borrow-graph c9-node-ids c9-node module effect-graph))
(definterposable c9-lifetime-interval-map [module] (lifecycles/lifetime-interval-map module))
(definterposable c9-escape-analysis-report [module] (lifecycles/escape-analysis-report module))
(definterposable c9-region-lifetime-graph [module] (lifecycles/region-lifetime-graph module))
(definterposable c9-arena-generation-graph [module] (lifecycles/arena-generation-graph module))
(definterposable c9-linear-resource-flow-graph [module] (resources/linear-resource-flow-graph module))
(definterposable c9-transfer-records [module] (resources/transfer-records module))
(definterposable c9-runtime-check-records [module] (resources/runtime-check-records module))
(definterposable c9-unsafe-audit-references [module] (resources/unsafe-audit-references module))
(definterposable c9-ownership-diagnostics [source-path ownership]
  (verification/ownership-diagnostics source-span c9-ownership-diagnostic-ids
                                     c9-ownership-rejected-designs source-path ownership))
(definterposable c9-linear-paths-exact? [linear] (verification/linear-paths-exact? linear))
(definterposable c9-ownership-verifier-report
  [c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (verification/verifier-report c9-linear-paths-exact? c9-ownership-diagnostic-ids
                                c8-artifact ownership borrow lifetimes moves escape region arena
                                linear transfer runtime unsafe diagnostics))
(definterposable c9-ownership-capability-proof [artifact]
  (verification/ownership-capability-proof artifact))
(definterposable c9-ownership-validate! [source-path artifact]
  (verification/validate! c9-ownership-capability-proof c9-ownership-fail! source-path artifact))

(defn- artifact-operations []
  {:read-source-form-records read-source-form-records :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module :c9-ownership-source-overrides c9-ownership-source-overrides
   :c9-ownership-validate-overrides! c9-ownership-validate-overrides!
   :compiler-c8-effect-source-artifact compiler-c8-effect-source-artifact
   :c9-ownership-graph c9-ownership-graph :c9-borrow-graph c9-borrow-graph
   :c9-lifetime-interval-map c9-lifetime-interval-map :c9-escape-analysis-report c9-escape-analysis-report
   :c9-region-lifetime-graph c9-region-lifetime-graph :c9-arena-generation-graph c9-arena-generation-graph
   :c9-linear-resource-flow-graph c9-linear-resource-flow-graph :c9-transfer-records c9-transfer-records
   :c9-runtime-check-records c9-runtime-check-records :c9-unsafe-audit-references c9-unsafe-audit-references
   :c9-ownership-diagnostics c9-ownership-diagnostics :c9-ownership-verifier-report c9-ownership-verifier-report
   :c9-ownership-validate! c9-ownership-validate! :c9-ownership-capability-proof c9-ownership-capability-proof
   :c4-artifact-id c4-artifact-id :c9-ownership-governing-document c9-ownership-governing-document
   :c9-ownership-diagnostic-ids c9-ownership-diagnostic-ids})
(definterposable compiler-c9-ownership-source-artifact [source-path source-text]
  (artifact/source-artifact (artifact-operations) source-path source-text))
(definterposable compiler-c9-ownership-file-artifact [path]
  (artifact/file-artifact compiler-c9-ownership-source-artifact path))

(defn- validate-operations! [operations] (policy/validate-operations! operations))
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c9-ownership-diagnostic-ids (get merged :c9-ownership-diagnostic-ids c9-ownership-diagnostic-ids)
              c9-ownership-governing-document (get merged :c9-ownership-governing-document c9-ownership-governing-document)
              c9-ownership-rejected-designs (get merged :c9-ownership-rejected-designs c9-ownership-rejected-designs)
              c9-ownership-override-diagnostics (get merged :c9-ownership-override-diagnostics c9-ownership-override-diagnostics)]
      (thunk))))

(def public-api
  (into {'public-api {:kind :contract} 'with-operations {:arglists '([operations thunk])}
         'c9-engine-contract {:arglists '([])} 'c9-ownership-diagnostic-ids {:kind :constant}
         'c9-ownership-governing-document {:kind :constant} 'c9-ownership-rejected-designs {:kind :constant}
         'c9-ownership-override-diagnostics {:kind :constant}}
        (map (fn [[name arglists]] [name {:arglists arglists}])
             [['c9-ownership-source-overrides '([module])] ['c9-ownership-message '([id])]
              ['c9-ownership-fail! '([id source-path subject extra])]
              ['c9-ownership-validate-overrides! '([source-path module overrides])]
              ['c9-node-ids '([effect-graph])] ['c9-node '([node-ids index fallback])]
              ['c9-ownership-graph '([module effect-graph])] ['c9-borrow-graph '([module effect-graph])]
              ['c9-lifetime-interval-map '([module])] ['c9-escape-analysis-report '([module])]
              ['c9-region-lifetime-graph '([module])] ['c9-arena-generation-graph '([module])]
              ['c9-linear-resource-flow-graph '([module])] ['c9-transfer-records '([module])]
              ['c9-runtime-check-records '([module])] ['c9-unsafe-audit-references '([module])]
              ['c9-ownership-diagnostics '([source-path ownership])] ['c9-linear-paths-exact? '([linear])]
              ['c9-ownership-verifier-report '([c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics])]
              ['c9-ownership-capability-proof '([artifact])] ['c9-ownership-validate! '([source-path artifact])]
              ['compiler-c9-ownership-source-artifact '([source-path source-text])]
              ['compiler-c9-ownership-file-artifact '([path])]])))
(defn c9-engine-contract [] (contract/engine-contract operation-keys public-api))
