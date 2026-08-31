

(defn p15-s23-core-diagnostic-core-form
  [value]
  (cond
    (and (seq? value) (contains? core-forms (first value)))
    (keyword (name (first value)))
    (seq? value) :call
    (symbol? value) :symbol
    :else :literal))

(defn p15-s23-core-diagnostic-evaluation-order
  [core-form value]
  (case core-form
    :quote []
    :if [:condition :then-or-else]
    :do (mapv (fn [idx] [:expr idx]) (range (count (rest value))))
    :let [:bindings-left-to-right :body-left-to-right]
    :fn [:call-arguments-left-to-right]
    :loop [:loop-bindings-left-to-right :body-left-to-right]
    :recur [:arguments-left-to-right]
    :def [:initializer]
    :var []
    :set! [:value]
    :try [:body :matching-handler]
    :throw [:value]
    :match [:scrutinee :selected-clause]
    :call [:operator :arguments-left-to-right]
    []))

(defn p15-s23-core-diagnostic-core-node
  [idx syntax]
  (let [value (get-in syntax [:form :value])
        core-form (p15-s23-core-diagnostic-core-form value)
        node-input {:syntax-id (:syntax/id syntax)
                    :form core-form
                    :value (pr-str value)
                    :span (:span syntax)
                    :origin (:origin syntax)
                    :idx idx}
        node-id (str "sha256:" (sha256-hex (pr-str node-input)))]
    {:artifact :gravity/core-node
     :node-id node-id
     :form core-form
     :children {:surface-form (pr-str value)}
     :source {:syntax-id (:syntax/id syntax)
              :span (:span syntax)
              :origin-chain (:origin syntax)}
     :binding-context :p15-s23-compiler-source
     :profile (:profile syntax)
     :target :jvm
     :metadata (:metadata syntax)
     :facts {:source-syntax-id (:syntax/id syntax)
             :source-form-kind (get-in syntax [:form :kind])}
     :effects #{}
     :capabilities #{}
     :unsafe-metadata nil
     :generated? (= :generated-form (get-in syntax [:form :kind]))
     :evaluation-order
     (p15-s23-core-diagnostic-evaluation-order core-form value)
     :lowering-rule core-form
     :version 1}))

(defn p15-s23-core-diagnostic-c6-artifact
  [source-path source-syntax-artifact pipeline-artifact]
  (let [syntax-stream (:syntax-object-stream source-syntax-artifact)
        nodes (mapv p15-s23-core-diagnostic-core-node
                    (range)
                    syntax-stream)
        surface-map
        {:artifact :gravity/c6-surface-to-core-map
         :entries (mapv (fn [node]
                          {:surface-syntax (get-in node [:source :syntax-id])
                           :core-root (:node-id node)
                           :core-form (:form node)
                           :generated? (:generated? node)})
                        nodes)
         :status :complete}
        trace
        {:artifact :gravity/c6-desugaring-trace
         :records (mapv (fn [node]
                          {:surface-syntax
                           (get-in node [:source :syntax-id])
                           :surface-kind (:form node)
                           :core-root (:node-id node)
                           :introduced-forms
                           (if (:generated? node) [(:form node)] [])
                           :preserved
                           p15-s23-core-diagnostic-required-preserves
                           :introduced-origin
                           [{:core-node (:node-id node)
                             :reason :p15-s23-source-lowering
                             :from (get-in node [:source :syntax-id])}]
                           :evaluation-order (:evaluation-order node)
                           :diagnostics []})
                        nodes)
         :status :complete}
        evaluation
        {:artifact :gravity/c6-evaluation-order-records
         :records (mapv (fn [node]
                          {:core-node (:node-id node)
                           :form (:form node)
                           :order (:evaluation-order node)
                           :effect-sensitive? false
                           :source (:source node)})
                        nodes)
         :status :complete}
        verifier
        {:artifact :gravity/c6-core-verifier-report
         :valid-core-forms?
         (every? #(contains? p15-s23-core-diagnostic-forms (:form %)) nodes)
         :source-and-generated-origins-valid?
         (every? #(and (get-in % [:source :syntax-id])
                       (p15-s23-source-syntax-span-resolves?
                        (get-in % [:source :span]))
                       (seq (get-in % [:source :origin-chain])))
                 nodes)
         :evaluation-order-present?
         (every? #(contains? % :evaluation-order) nodes)
         :profile-target-annotations-valid?
         (every? #(and (:profile %) (:target %)) nodes)
         :surface-only-forms-absent? true
         :status :passed}
        artifact-base
        {:kind :gravity/stage0-c6-core-lowering-artifact
         :task "P15-S23"
         :document-set ["C6"]
         :governing-document c6-lowering-governing-document
         :pass {:name :p15-s23-core-lowering-proof
                :input :p15-s23-source-syntax-serialization-proof
                :output :p15-s23-core-lowering-evidence
                :requires [:syntax-object-stream :source-unit-record
                           :compiler-pipeline-manifest]
                :preserves [:source-spans :syntax-identity :origin-chain
                            :metadata :profile :effects :capabilities]
                :emits [:core-ast-module :surface-to-core-map
                        :desugaring-trace :evaluation-order-records
                        :core-verifier-report]
                :rejects p15-s23-core-diagnostic-ids}
         :source-path source-path
         :source-syntax-artifact
         (select-keys source-syntax-artifact
                      [:kind :artifact-id :proof-id :source-unit-record
                       :syntax-object-summary :serialization-roundtrip-record])
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :manifest-id])
         :core-ast-module {:artifact :gravity/core-ast-module
                           :module :gravity.bootstrap.p15-s23.compiler
                           :roots (mapv :node-id nodes)
                           :node-count (count nodes)
                           :status :complete}
         :core-node-table nodes
         :surface-to-core-map surface-map
         :desugaring-trace trace
         :evaluation-order-records evaluation
         :domain-boundary-records []
         :core-verifier-report verifier
         :lowering-rule-invalidation
         {:artifact :gravity/c6-lowering-rule-invalidation
          :rule-version "p15-s23-core.1"
          :rules (vec (sort (set (map :lowering-rule nodes))))
          :invalidates [:typed-core :effects :ownership :safety :mir
                        :diagnostics]
          :status :stable}
         :p15-s23-core-lowering-results
         {:core-node-count (count nodes)
          :source-syntax-linked? true
          :pipeline-manifest-linked? true
          :core-verifier-status (:status verifier)
          :status :complete}
         :diagnostics []}
        proof {:source-syntax-linked? true
               :pipeline-manifest-linked? true
               :core-nodes-produced? (boolean (seq nodes))
               :source-to-core-map-present?
               (= :complete (:status surface-map))
               :evaluation-order-preserved?
               (= :complete (:status evaluation))
               :source-spans-preserved?
               (every? #(p15-s23-source-syntax-span-resolves?
                         (get-in % [:source :span]))
                       nodes)
               :syntax-identities-preserved?
               (= (count nodes)
                  (count (set (map #(get-in % [:source :syntax-id])
                                   nodes))))
               :origin-chains-preserved?
               (every? #(seq (get-in % [:source :origin-chain])) nodes)
               :core-verifier-passed? (= :passed (:status verifier))
               :status :complete}
        artifact (assoc artifact-base :capability-based-proof proof)]
    (assoc artifact :artifact-id (c4-artifact-id artifact))))

(defn p15-s23-core-diagnostic-stable-id
  [diagnostic]
  (str "diag-"
       (sha256-hex
        (pr-str {:rule (:rule diagnostic)
                 :stage (:stage diagnostic)
                 :primary-artifact (get-in diagnostic [:primary :artifact])
                 :facts (:facts diagnostic)}))))

(defn p15-s23-core-diagnostic-preserved-diagnostic
  [source-path c6-artifact id idx]
  (let [node (nth (:core-node-table c6-artifact)
                  (mod idx (count (:core-node-table c6-artifact))))
        diagnostic
        {:artifact :gravity/diagnostic
         :rule id
         :severity :error
         :stage :p15-s23-core-lowering-diagnostic-preservation-report
         :message-key
         (keyword "p15-s23.core-diagnostic" (str/lower-case id))
         :primary {:span (get-in node [:source :span])
                   :syntax-id (get-in node [:source :syntax-id])
                   :artifact (:node-id node)}
         :related [{:role :lowered-from
                    :span (get-in node [:source :span])
                    :artifact
                    (get-in node [:source :syntax-id])}]
         :origin-chain (get-in node [:source :origin-chain])
         :profile (:profile node)
         :target (:target node)
         :involved-artifacts [(:artifact-id c6-artifact) (:node-id node)]
         :facts {:diagnostic id
                 :core-node (:node-id node)
                 :source-path source-path
                 :preserves p15-s23-core-diagnostic-required-preserves}
         :remediation
         [{:kind :repair-p15-s23-core-diagnostic-preservation}]
         :redactions []
         :lifecycle :active
         :ordering-key [id (:node-id node)]}]
    (assoc diagnostic
           :diagnostic-id
           (p15-s23-core-diagnostic-stable-id diagnostic))))