

(defn sh04-syntax-rich-object
  [syntax serialization-id]
  (-> syntax
      (assoc :syntax/id (:syntax-id syntax)
             :identity {:algorithm :sha256
                        :authority :gravity.bootstrap.syntax
                        :domain :gravity/sh04-syntax-object-v1
                        :input-hash (:syntax-id syntax)
                        :serialization-id serialization-id}
             :authority {:slice :SH-04
                         :owner :gravity-source
                         :module 'gravity.bootstrap.syntax
                         :clojure-adapter-residual? true})
      (dissoc :syntax-id :schema-version)))

(defn sh04-syntax-generated-products!
  [source-path binding base semantic-source-id reader-binding
   reader-source-revision]
  (let [producer-id
        (reader-canonical-hash
         {:domain :gravity/sh04-generated-producer-v1
          :semantic-source-id semantic-source-id
          :input-syntax-id (:syntax-id base)})
        producer {:kind :macro
                  :name 'gravity.bootstrap.syntax/generated-conformance
                  :identity producer-id
                  :version "SH-04"
                  :source-id semantic-source-id
                  :generated-form-id :generated-form-0}
        generated-span {:kind :generated :producer-id producer-id :ordinal 0}
        namespace-context (:namespace base)
        hygiene
        {:marks [:sh04/generated-mark]
         :lexical-scopes [:caller-scope :introduced-scope]
         :renames {'tmp__auto__ 'tmp__sh04__1}
         :introduced-identifiers ['tmp__sh04__1]
         :captures [{:identifier 'captured-binding
                     :macro-api 'gravity.syntax/capture
                     :call-site-namespace (:current namespace-context)
                     :intentional? true
                     :authority-bearing? false
                     :policy-result :not-required}]
         :macro-definition-namespace 'gravity.bootstrap.syntax
         :macro-call-site-namespace (:current namespace-context)}
        raw
        (sh04-syntax-execute!
         source-path binding 'c3-generated-syntax-template
         [(:syntax-id base)
          {:kind :generated-form :value '(do tmp__sh04__1)
           :raw "(do tmp__sh04__1)"}
          generated-span producer :generated-syntax-conformance
          namespace-context (:profile base)
          {:generated true :source-metadata (:metadata base)}
          hygiene
          [{:producer-stage :macro-expansion
            :fact-kind :generated-origin-checked
            :value true :version 1
            :invalidated-by [:name-resolution]}]
          reader-binding reader-source-revision])]
    (sh04-syntax-resolve-template!
     source-path binding raw reader-binding reader-source-revision)))

(defn sh04-syntax-sh02-descriptor
  [source-path binding summary]
  (let [projection-name :syntax-product-identities
        fact-name :syntax-product-binding
        identity-name :syntax-result
        identity-domain :gravity/sh04-syntax-result-identity-v1
        evidence-id
        (p15-s23-c6c10-canonical-digest
         source-path {:domain :gravity/sh04-syntax-envelope-evidence-v1
                      :summary summary
                      :plan-semantic-hash (:plan-semantic-hash binding)})
        identity-preimage {:summary summary}
        observed-id
        (p15-s23-c6c10-canonical-digest
         source-path {:domain identity-domain
                      :semantic-input identity-preimage})
        fact-entries
        [{:syntax-result-id (:syntax-result-id summary)
          :syntax-stream-id (:syntax-stream-id summary)
          :serialization-id (:serialization-set-id summary)
          :graph-id (:graph-id summary)}]
        fact-value {:family fact-name :entries fact-entries}
        artifact-id
        (p15-s23-c6c10-canonical-digest
         source-path {:syntax-result (:syntax-result-id summary)})]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :c3-syntax
     :artifact-kind sh04-syntax-sealed-artifact-kind
     :source-revision
     {:owner :sh04-syntax
      :source-language :gravity
      :logical-source-path sh04-syntax-source-relative-path
      :source-content-hash (:source-content-hash binding)
      :source-byte-count (:source-byte-count binding)
      :plan-semantic-hash (:plan-semantic-hash binding)
      :functions-semantic-hash (:functions-semantic-hash binding)
      :builder-function 'c3-syntax-stream-build-template
      :builder-semantic-hash
      (get sh04-syntax-public-function-hashes
           'c3-syntax-stream-build-template)
      :function-shapes sh04-syntax-public-function-shapes}
     :projection-contract
     {:contract-kind :gravity/sh04-syntax-product-envelope-contract
      :contract-version 1 :profile :meta :target :jvm
      :required-semantic-projections [projection-name]
      :required-fact-families [fact-name]
      :required-identity-subjects [identity-name]}
     :semantic-projections
     [{:name projection-name :role :complete-syntax-product-identities
       :entry-count (count summary) :value summary}]
     :fact-transitions
     [{:name fact-name :disposition :preserved
       :input fact-value :output fact-value
       :input-count (count fact-value)
       :output-count (count fact-value)
       :evidence-ids [evidence-id]}]
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order [] :provider-selections [] :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :checked}]
      :proof-certificate-table {evidence-id {:status :checked}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage [{:proof-id evidence-id :used-by :syntax-products}]}
     :preservation
     {:requires [fact-name] :preserves [fact-name]
      :invalidates [] :regenerates []
      :residual-checks [:identity-subject-equality
                        :digest-graph-reachability]}
     :identity-subjects
     [{:name identity-name :domain identity-domain
       :preimage identity-preimage :observed-id observed-id}]
     :lineage
     [{:stage :sh04-syntax
       :artifact-kind :gravity/sh04-syntax-object-artifact
       :semantic-id (:syntax-result-id summary)
       :artifact-id artifact-id :verification-id evidence-id
       :relation :produced-from-gravity-syntax}]
     :reference-closure
     {:root-id "sh04-syntax-result" :node-ids ["sh04-syntax-result"]
      :edges [] :fact-reference-ids [evidence-id]
      :origin-reference-ids [] :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids [] :observed-node-count 1
      :observed-edge-count 0 :observed-maximum-depth 0}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (System/getProperty "user.dir")
      :invocation-root (System/getProperty "user.dir")}
     :bounds p15-s23-sh02-authenticated-envelope-bounds}))