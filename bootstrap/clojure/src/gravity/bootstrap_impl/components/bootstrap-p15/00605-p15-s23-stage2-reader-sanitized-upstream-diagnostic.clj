

(defn p15-s23-stage2-reader-sanitized-upstream-diagnostic
  [data]
  (let [span (p15-s23-stage2-reader-safe-span (:source-span data))
        source-id (:source-id data)
        reader-state (:reader-state data)]
    {:artifact :gravity/diagnostic
     :diagnostic-id (:diagnostic-id data)
     :rule (:rule data)
     :severity :error
     :source-id source-id
     :source-span span
     :primary {:span span :artifact source-id}
     :related []
     :origin-chain
     (mapv #(select-keys % [:kind :source-id :path])
           (:origin-chain data))
     :facts (:facts data)
     :diagnostic-family :c2-reader
     :stage :read-source
     :document-id "C2"
     :expected-document c2-reader-governing-document
     :involved-artifacts [source-id]
     :token-id (:token-id data)
     :form-id (:form-id data)
     :reader-engine-diagnostic (:reader-engine-diagnostic data)
     :remapped-from (:remapped-from data)
     :reader-state
     (select-keys reader-state
                  [:artifact :stage :byte-offset :line :column
                   :token-id :form-id])
     :redactions
     [:id :message :cause-message :raw :raw-spelling :value
      :reader-options :extension-tag :host-stack :sentinel]}))

(defn p15-s23-stage2-front-end-reader-products
  [source-path source-text]
  (try
    (p15-s23-stage2-c2-c3-front-end-products source-path source-text)
    (catch clojure.lang.ExceptionInfo ex
      (let [data (ex-data ex)]
        (if (and
             (contains? p15-s23-stage2-reader-compatibility-diagnostic-map
                        (:reader-engine-diagnostic data))
             (p15-s23-stage2-canonical-c2-diagnostic-authentic?
              source-path source-text data
              (p15-s23-stage2-reader-replayed-diagnostic
               source-path source-text)))
          (p15-s23-stage2-source-front-end-fail!
           "P15S23F009" source-path nil
           {:reason :authoritative-reader-rejection
            :reader-compatibility-boundary :c2-to-p15-s23
            :upstream-diagnostic-id (:diagnostic-id data)
            :upstream-rule (:rule data)
            :upstream-reader-engine-diagnostic
            (:reader-engine-diagnostic data)
            :upstream-diagnostic
            (p15-s23-stage2-reader-sanitized-upstream-diagnostic data)
            :clojure-seed-boundary? true
            :self-hosted? false})
          (throw ex))))))

(defn p15-s23-stage2-front-end-source-module-record
  [front-end source-path source-text]
  (when-not (= :gravity-stage2-reader-rules-v1
               (get-in front-end [:reader-rules :engine]))
    (p15-s23-stage2-source-front-end-fail!
     "P15S23F002" source-path front-end
     {:missing-fields [:reader-rules :engine]}))
  (let [reader-products
        (p15-s23-stage2-front-end-reader-products source-path source-text)
        records (:records reader-products)
        forms (:forms reader-products)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        syntax (mapv #(assoc %
                             :namespace (:module module)
                             :profile (:profile module)
                             :phase :read
                             :hygiene [])
                     records)
        expanded-syntax
        (mapv #(p15-s23-stage2-front-end-expand-form front-end %) syntax)
        body-syntax (subvec expanded-syntax 1)
        expanded-forms (mapv :form body-syntax)
        trace (->> body-syntax
                   (filter #(seq (:generated-origin %)))
                   (mapv (fn [syn]
                           {:macro 'gravity.core/defn
                            :macro-version "stage2-front-end"
                            :caller-namespace (:module module)
                            :caller-profile (:profile module)
                            :call-span (:span syn)
                            ;; The stage2 ingress record embeds the genuine C3
                            ;; syntax object.  C3 identity is namespaced; the
                            ;; old unqualified lookup silently recorded nil and
                            ;; broke generated-origin closure for downstream
                            ;; checked-core/MIR consumers.
                            :input-syntax-id
                            (get-in syn [:c3-syntax-object :syntax/id])
                            :generated-origin (:generated-origin syn)
                            :hygiene-policy :hygienic
                            :diagnostics []})))]
    {:artifact :gravity/p15-s23-stage2-front-end-module-record
     :source-path source-path
     :source-id (str "sha256:" (sha256-hex source-text))
     :records records
     :forms forms
     :module (assoc module :forms expanded-forms)
     :reader-products reader-products
     :source-unit-record (:source-unit-record reader-products)
     :token-stream (:token-stream reader-products)
     :form-tree (:form-tree reader-products)
     :top-level-form-ids (:top-level-form-ids reader-products)
     :syntax-seed-stream (:syntax-seed-stream reader-products)
     :reader-source-map (:reader-source-map reader-products)
     :literal-decoding-records (:literal-decoding-records reader-products)
     :semantic-error-deferment-record
     (:semantic-error-deferment-record reader-products)
     :reader-diagnostics (:reader-diagnostics reader-products)
     :incremental-reader-hashes (:incremental-reader-hashes reader-products)
     :reader-product-integrity (:reader-product-integrity reader-products)
     :c3-artifact-id (:c3-artifact-id reader-products)
     :c3-capability-proof (:c3-capability-proof reader-products)
     :syntax-object-stream syntax
     :c3-syntax-object-stream (:c3-syntax-object-stream reader-products)
     :expanded-syntax-object-stream expanded-syntax
     :expanded-forms expanded-forms
     :macro-expansion-trace trace
     :status :complete}))

(def p15-s23-stage2-front-end-executor-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity
    :generated-origin :diagnostic-codes :effects :capabilities
    :profile :compiler-lineage :artifact-provenance})

(def p15-s23-stage2-front-end-executor-required-emits
  #{:stage2-front-end-execution-record :stage2-reader-record
    :stage2-macro-expansion-record :module-context
    :expanded-core-forms :accepted-front-end-executor-comparison
    :rejected-diagnostic-comparison
    :stage2-front-end-executor-boundary-record})

(def p15-s23-stage2-front-end-executor-required-steps
  #{:load-front-end-contract :execute-reader-rules
    :execute-syntax-object-builder :execute-built-in-macro-rules
    :validate-module-contract :compare-reference-front-end
    :emit-executor-boundary-record})

(def p15-s23-stage2-front-end-executor-diagnostic-messages
  {"P15S23J001" "P15-S23 stage2 front-end executor contract is missing"
   "P15S23J002" "P15-S23 stage2 front-end executor rule set is incomplete"
   "P15S23J003" "P15-S23 stage2 front-end executor accepted output is not equivalent"
   "P15S23J004" "P15-S23 stage2 front-end executor rejected diagnostics are not preserved"
   "P15S23J005" "P15-S23 stage2 front-end executor evidence links are incomplete"
   "P15S23J006" "P15-S23 stage2 front-end executor preservation or emission contract is incomplete"
   "P15S23J007" "P15-S23 stage2 front-end executor boundary record is incomplete"
   "P15S23J008" "P15-S23 stage2 front-end executor makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-front-end-executor-diagnostic-ids
  ["P15S23J001" "P15S23J002" "P15S23J003" "P15S23J004"
   "P15S23J005" "P15S23J006" "P15S23J007" "P15S23J008"])

(defn p15-s23-stage2-front-end-executor-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-front-end-executor-diagnostic-messages
              id
              "P15-S23 stage2 front-end executor proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-front-end-executor
                 :diagnostic-family
                 :p15-s23-stage2-front-end-executor
                 :value value
                 :remediation "Keep the stage2 front-end executor authored in Gravity source, prove accepted and rejected front-end behavior through that executor boundary, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-front-end-executor-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-front-end-executor
   :source-span {:source source-path}
   :message
   (get p15-s23-stage2-front-end-executor-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_front_end_executor})

(defn p15-s23-stage2-front-end-executor-rule-record
  [executor]
  (let [steps (set (:executor-steps executor))
        missing-steps
        (set/difference
         p15-s23-stage2-front-end-executor-required-steps
         steps)
        complete?
        (and (= :gravity-stage2-front-end-executor-v1
                (:engine executor))
             (= :gravity-source (:implemented-by executor))
             (= :gravity-stage2-runtime-kernel (:executed-by executor))
             (empty? missing-steps))]
    {:artifact :gravity/p15-s23-stage2-front-end-executor-rule-record
     :engine (:engine executor)
     :implemented-by (:implemented-by executor)
     :executed-by (:executed-by executor)
     :executor-steps (p15-s23-stage2-sort-values steps)
     :missing-steps (p15-s23-stage2-sort-values missing-steps)
     :rule-set-complete? complete?
     :status (if complete? :complete :failed)}))