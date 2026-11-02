(ns gravity.c7-type-checker.evidence
  "Boundary, dispatch, schema, and layout evidence emitted by hosted Stage0 C7.")

(defn dynamic-boundary-records
  [node-operator nodes module]
  {:artifact :gravity/c7-dynamic-boundary-records
   :records
   (mapv (fn [node]
           {:boundary-id (str "c7-dynamic-" (:node-id node))
            :kind :dynamic-call
            :source (:node-id node)
            :input-type "Dynamic"
            :result-type "Dynamic"
            :profile (:profile node)
            :target (:target node)
            :runtime-checks [:runtime-type-known]
            :effects #{:runtime/dynamic-dispatch}
            :capabilities #{}
            :accepted? (= :hosted (:profile module))
            :diagnostics []})
         (filter #(= 'dynamic/value (node-operator %)) nodes))
   :status :complete})

(defn cast-records
  [node-operator nodes]
  {:artifact :gravity/c7-cast-records
   :records
   (mapv (fn [node]
           {:cast-id (str "c7-cast-" (:node-id node))
            :kind :checked-dynamic-cast
            :source-node (:node-id node)
            :from "Dynamic"
            :to "String"
            :classification :runtime-checked
            :runtime-check :type-tag-check
            :unsafe-metadata (:unsafe-metadata node)
            :source (:source node)
            :status :checked})
         (filter #(= 'dynamic/cast (node-operator %)) nodes))
   :status :complete})

(defn generic-instantiations
  [node-operator nodes]
  {:artifact :gravity/c7-generic-instantiation-table
   :records
   (mapv (fn [node]
           {:instantiation-id (str "c7-generic-" (:node-id node))
            :generic 'generic/id
            :type-arguments ["T"]
            :bounds ["Any"]
            :source-node (:node-id node)
            :profile (:profile node)
            :target (:target node)
            :status :solved})
         (filter #(= 'generic/id (node-operator %)) nodes))
   :status :complete})

(defn protocol-dispatch-table
  [node-operator nodes]
  {:artifact :gravity/c7-protocol-dispatch-type-table
   :records
   (mapv (fn [node]
           {:dispatch-id (str "c7-dispatch-" (:node-id node))
            :protocol :Displayable
            :method 'protocol/value
            :receiver-type "String"
            :dispatch :hosted-dynamic
            :effects (:effects node)
            :capabilities (:capabilities node)
            :profile (:profile node)
            :target (:target node)
            :source-node (:node-id node)
            :status :typed})
         (filter #(= 'protocol/value (node-operator %)) nodes))
   :status :complete})

(defn schema-links
  [domain-boundaries]
  {:artifact :gravity/c7-schema-type-links
   :records
   (mapv (fn [boundary]
           {:schema-type-id (str "c7-schema-"
                                 (get-in boundary [:source :syntax-id]))
            :schema :Packet
            :source-schema (get-in boundary [:semantic-anchor :source-syntax])
            :domain (:domain boundary)
            :validation-boundary :schema-ir-verifier
            :profile (:profile boundary)
            :target (:target boundary)
            :status :preserved})
         (filter #(= :schema-ir (:domain %)) domain-boundaries))
   :status :complete})

(defn layout-facts
  [node-type nodes]
  {:artifact :gravity/c7-layout-facts
   :records
   (mapv (fn [node]
           {:layout-id (str "c7-layout-" (:node-id node))
            :core-node (:node-id node)
            :type (node-type node)
            :profile (:profile node)
            :target (:target node)
            :layout (case (:profile node)
                      :hosted :managed
                      :native :explicit-native-layout
                      :firmware :fixed-layout
                      :kernel :explicit-kernel-layout
                      :hardware :synthesizable-layout
                      :abstract)
            :status :recorded})
         nodes)
   :status :complete})
