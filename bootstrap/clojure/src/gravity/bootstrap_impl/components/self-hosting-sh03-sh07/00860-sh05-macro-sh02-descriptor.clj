

(defn sh05-macro-sh02-descriptor
  [source-path binding summary]
  (let [projection-name :macro-product-identities
        fact-name :macro-product-binding
        identity-name :macro-result
        identity-domain :gravity/sh05-macro-result-identity-v2
        evidence-id
        (p15-s23-c6c10-canonical-digest
         source-path {:domain :gravity/sh05-macro-envelope-evidence-v2
                      :summary summary
                      :plan-semantic-hash (:plan-semantic-hash binding)})
        identity-preimage {:summary summary}
        observed-id
        (p15-s23-c6c10-canonical-digest
         source-path {:domain identity-domain
                      :semantic-input identity-preimage})
        fact-value {:family fact-name :entries [summary]}
        artifact-id
        (p15-s23-c6c10-canonical-digest
         source-path {:macro-result (:artifact-id summary)})]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage sh05-macro-envelope-stage
     :artifact-kind sh05-macro-sealed-artifact-kind
     :source-revision
     {:owner :sh05-macro
      :source-language :gravity
      :logical-source-path sh05-macro-source-relative-path
      :source-content-hash (:source-content-hash binding)
      :source-byte-count (:source-byte-count binding)
      :plan-semantic-hash (:plan-semantic-hash binding)
      :functions-semantic-hash (:functions-semantic-hash binding)
      :builder-function 'sh05-expand-macro-template
      :builder-semantic-hash
      (get sh05-macro-public-function-hashes
           'sh05-expand-macro-template)
      :function-shapes sh05-macro-public-function-shapes}
     :projection-contract
     {:contract-kind :gravity/sh05-macro-product-envelope-contract
      :contract-version 1 :profile :meta :target :jvm
      :required-semantic-projections [projection-name]
      :required-fact-families [fact-name]
      :required-identity-subjects [identity-name]}
     :semantic-projections
     [{:name projection-name :role :complete-macro-product-identities
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
      :proof-usage [{:proof-id evidence-id :used-by :macro-products}]}
     :preservation
     {:requires [fact-name] :preserves [fact-name]
      :invalidates [] :regenerates []
      :residual-checks [:identity-subject-equality
                        :digest-graph-reachability]}
     :identity-subjects
     [{:name identity-name :domain identity-domain
       :preimage identity-preimage :observed-id observed-id}]
     :lineage
     [{:stage :sh05-macro
       :artifact-kind :gravity/sh05-macro-expansion-artifact
       :semantic-id (:artifact-id summary)
       :artifact-id artifact-id :verification-id evidence-id
       :relation :produced-from-gravity-macro}]
     :reference-closure
     {:root-id "sh05-macro-result" :node-ids ["sh05-macro-result"]
      :edges [] :fact-reference-ids [evidence-id]
      :origin-reference-ids [] :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids [] :observed-node-count 1
      :observed-edge-count 0 :observed-maximum-depth 0}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (System/getProperty "user.dir")
      :invocation-root (System/getProperty "user.dir")}
     :bounds p15-s23-sh02-authenticated-envelope-bounds}))

(def sh05-macro-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :expanded-syntax-stream-id :macro-expansion-trace-id
    :expanded-defn-count :expanded-syntax-stream :expanded-forms
    :macro-expansion-trace :macro-environment
    :generated-origin-source-map :expanded-syntax-graph
    :sh04-syntax-artifact :gravity-macro-boundary :provenance
    :pass :capability-based-proof :execution-boundary :diagnostics})

(defn sh05-path-neutral-semantic-value
  [value]
  (cond
    (map? value)
    (let [clean
          (into {}
                (keep (fn [[key item]]
                        (when-not (contains? #{:actual-source-path
                                               :workspace-root
                                               :invocation-root}
                                             key)
                          [key (sh05-path-neutral-semantic-value item)])))
                value)]
      (if (and (contains? clean :byte-start)
               (or (contains? clean :source) (contains? clean :file)))
        (let [semantic-source (or (:file clean) (:source clean))]
          (cond-> clean
            (contains? clean :source) (assoc :source semantic-source)
            (contains? clean :file) (assoc :file semantic-source)))
        clean))

    (vector? value) (mapv sh05-path-neutral-semantic-value value)
    (set? value) (into #{} (map sh05-path-neutral-semantic-value) value)
    (seq? value) (apply list (map sh05-path-neutral-semantic-value value))
    :else value))

(def sh05-identity-chunk-width 16)

(defn sh05-ordered-identity-chunks
  [values projector]
  (->> values
       (map-indexed vector)
       (partition-all sh05-identity-chunk-width)
       (map-indexed
        (fn [chunk-index entries]
          {:chunk-index chunk-index
           :start-ordinal (first (first entries))
           :item-count (count entries)
           :items
           (mapv (fn [[ordinal value]]
                   (projector ordinal value))
                 entries)}))
       vec))

(defn sh05-macro-run-semantic-summary
  [run]
  (let [raw (:raw-template-result run)
        resolved (:resolved-expansion run)
        trace (first (:macro-expansion-trace resolved))]
    {:raw-result {:artifact (:artifact raw)
                  :schema-version (:schema-version raw)
                  :status (:status raw)}
     :digest-contract
     (mapv #(select-keys % [:ordinal :purpose]) (:digest-requests run))
     :semantic-resolved-digest (first (:resolved-digests run))
     :resolved-expansion
     (select-keys
      resolved
      [:artifact :schema-version :artifact-id :status :macro
       :macro-version :input-syntax-id :output-syntax-id
       :semantic-binding :profile :target :phase :bounds])
     :trace-replay-id (:trace-replay-id trace)
     :template-verification
     (:template-verification run)
     :resolved-verification
     (:resolved-verification run)}))

(defn sh05-macro-artifact-semantic-payload
  [artifact]
  (let [boundary (:gravity-macro-boundary artifact)]
    (sh05-path-neutral-semantic-value
     {:domain :gravity/sh05-macro-source-artifact-v2
      :kind (:kind artifact)
      :status (:status artifact)
      :slice (:slice artifact)
      :task (:task artifact)
      :document-set (:document-set artifact)
      :governing-document (:governing-document artifact)
      :sh04-syntax-artifact (:sh04-syntax-artifact artifact)
      :expanded-syntax-stream-id (:expanded-syntax-stream-id artifact)
      :macro-expansion-trace-id (:macro-expansion-trace-id artifact)
      :expanded-defn-count (:expanded-defn-count artifact)
      :macro-environment (:macro-environment artifact)
      :pass (:pass artifact)
      :execution-boundary (:execution-boundary artifact)
      :diagnostics (:diagnostics artifact)
      :boundary-contract
      (select-keys boundary
                   [:slice :owner :adapter-contract
                    :target-source-reread? :clojure-adapter-residual?
                    :self-hosted?])
      :plan-binding
      (select-keys
       (:plan-binding boundary)
       [:artifact :status :semantic-authority :compiled-by :executed-by
        :generic-bridge-residual? :self-hosted? :source-language
        :source-byte-count :source-content-hash :plan-semantic-hash
        :functions-semantic-hash :function-count :function-names-hash
        :function-shapes-hash :public-function-hashes
        :public-function-shapes])
      :expansion-run-count (count (:expansion-runs boundary))
      :expansion-run-chunks
      (sh05-ordered-identity-chunks
       (:expansion-runs boundary)
       (fn [ordinal run]
         {:ordinal ordinal
          :summary (sh05-macro-run-semantic-summary run)}))})))

(defn sh05-macro-artifact-identity-input
  [artifact]
  (sh05-macro-artifact-semantic-payload artifact))

(defn sh05-macro-artifact-id
  [artifact]
  (p15-s23-c6c10-canonical-digest
   "<sh05-macro-artifact>"
   (sh05-macro-artifact-identity-input artifact)))

(defn sh05-expanded-syntax-stream-identity-input
  [expanded-stream]
  {:domain :gravity/sh05-expanded-syntax-stream-v2
   :item-count (count expanded-stream)
   :item-chunks
   (sh05-ordered-identity-chunks
    expanded-stream
    (fn [ordinal syntax]
      {:ordinal ordinal
       :expanded-syntax-id (:expanded-syntax-id syntax)}))})

(defn sh05-expanded-syntax-stream-id
  [source-path expanded-stream]
  (p15-s23-c6c10-canonical-digest
   source-path
   (sh05-expanded-syntax-stream-identity-input expanded-stream)))