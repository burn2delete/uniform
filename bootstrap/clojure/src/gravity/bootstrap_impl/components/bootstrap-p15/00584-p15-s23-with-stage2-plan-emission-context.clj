

(defn- p15-s23-with-stage2-plan-emission-context
  [build-fn]
  (if *p15-s23-stage2-plan-emission-context*
    (build-fn)
    (binding [*p15-s23-stage2-plan-emission-context* (atom {})]
      (build-fn))))

(defn p15-s23-stage2-plan-emitter-compile-source
  [emitter source-path source-text]
  (p15-s23-with-stage2-plan-emission-context
   (fn []
     (let [_ (validate-stage0-source-profile! source-path source-text)
           _ (validate-stage0-source-safety! source-path source-text)
           macro-artifact (macro-source-artifact source-path source-text)
           authoritative-products
           (p15-s23-stage2-c2-c3-front-end-products source-path source-text)
           authoritative-module
           (parse-module source-path (:forms authoritative-products))
           ;; Export visibility and provider selection are semantic.  The
           ;; historical macro artifact retains neither ns clause, while the
           ;; C2/C3 stage2 front end does.  Restore those authoritative fields at
           ;; this narrow direct emitter boundary so driver/direct plans agree
           ;; without normalizing either contract away.
           module (cond-> (assoc (:module macro-artifact)
                                 :forms (:expanded-forms macro-artifact))
                    (seq (:exports authoritative-module))
                    (assoc :exports (:exports authoritative-module))
                    (seq (:providers authoritative-module))
                    (assoc :providers (:providers authoritative-module)))]
       (p15-s23-stage2-emitted-core-plan
        emitter source-path source-text module)))))

(defn p15-s23-stage2-plan-emitter-accepted-record
  [emitter]
  (let [source-path p15-s23-accepted-app-source-path
        source-text (slurp source-path)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        stage2-plan (p15-s23-stage2-emitted-core-plan
                     emitter source-path source-text module)
        stage2-output (execute-stage0-compiled-plan stage2-plan)
        stage0-plan (stage0-compiled-core-plan source-path source-text module)
        stage0-output (execute-stage0-compiled-plan stage0-plan)
        expected-stdout (get-in emitter
                                [:accepted-scope :expected-stdout])
        normalized-stage2-bindings
        (mapv #(assoc % :visibility :local)
              (:binding-table stage2-plan))
        normalized-stage0-bindings
        (mapv #(assoc % :visibility :local)
              (:binding-table stage0-plan))
        binding-table-equivalent?
        (= normalized-stage2-bindings normalized-stage0-bindings)
        function-instructions-equivalent?
        (= (update-vals (:functions stage2-plan)
                        #(select-keys % [:params :body :arity
                                         :body-form-count :instructions]))
           (update-vals (:functions stage0-plan)
                        #(select-keys % [:params :body :arity
                                         :body-form-count :instructions])))
        instruction-summary-equivalent?
        (= (:instruction-summary stage2-plan)
           (:instruction-summary stage0-plan))
        effect-summary-equivalent?
        (= (:effect-summary stage2-plan)
           (:effect-summary stage0-plan))
        accepted-output-equivalent?
        (and (= stage2-output stage0-output)
             (= stage2-output expected-stdout))
        equivalent?
        (and binding-table-equivalent?
             function-instructions-equivalent?
             instruction-summary-equivalent?
             effect-summary-equivalent?
             accepted-output-equivalent?)]
    {:artifact :gravity/p15-s23-stage2-plan-emitter-accepted-record
     :fixture source-path
     :stage2-plan-id (:plan-id stage2-plan)
     :stage0-plan-id (:plan-id stage0-plan)
     :stage2-plan-kind (:kind stage2-plan)
     :stage0-plan-kind (:kind stage0-plan)
     :entrypoint (:entrypoint stage2-plan)
     :binding-table-equivalent? binding-table-equivalent?
     :binding-table-visibility-normalized? true
     :function-instructions-equivalent?
     function-instructions-equivalent?
     :instruction-summary-equivalent? instruction-summary-equivalent?
     :effect-summary-equivalent? effect-summary-equivalent?
     :accepted-output-equivalent? accepted-output-equivalent?
     :stage2-output stage2-output
     :stage0-output stage0-output
     :expected-stdout expected-stdout
     :stage2-plan (select-keys stage2-plan
                               [:kind :compatibility-kind :plan-id
                                :entrypoint :compiler :module
                                :binding-table :instruction-summary
                                :effect-summary :diagnostics])
     :status (if equivalent? :complete :failed)}))

(defn p15-s23-stage2-plan-emitter-rejected-records
  [emitter]
  (mapv
   (fn [{:keys [fixture expected-diagnostic rejected-design]}]
     (try
       (let [source-text (slurp fixture)
             plan (p15-s23-stage2-plan-emitter-compile-source
                   emitter fixture source-text)
             stdout (execute-stage0-compiled-plan plan)]
         {:fixture fixture
          :rejected-design rejected-design
          :expected-diagnostic expected-diagnostic
          :status :accepted-unexpectedly
          :stdout stdout
          :stage2-plan-id (:plan-id plan)
          :matches-expected? false})
       (catch clojure.lang.ExceptionInfo ex
         (let [data (ex-data ex)
               diagnostic (:id data)]
           {:fixture fixture
            :rejected-design rejected-design
            :expected-diagnostic expected-diagnostic
            :status :rejected
            :diagnostic diagnostic
            :message (:message data)
            :diagnostic-data data
            :matches-expected? (= expected-diagnostic diagnostic)}))))
   p15-s23-rejected-app-fixtures))

(defn p15-s23-stage2-plan-emitter-rejected-record
  [records]
  (let [expected (set (map :expected-diagnostic records))
        observed (set (map :diagnostic records))
        mismatches (remove :matches-expected? records)
        matches? (and (= expected observed) (empty? mismatches))]
    {:artifact :gravity/p15-s23-stage2-plan-emitter-rejected-record
     :expected-diagnostics (p15-s23-stage2-sort-values expected)
     :observed-diagnostics (p15-s23-stage2-sort-values observed)
     :fixture-count (count records)
     :mismatch-count (count mismatches)
     :records records
     :status (if matches? :complete :failed)}))

(defn p15-s23-stage2-plan-emitter-evidence-link-record
  [nucleus-artifact pipeline-artifact accepted-artifact rejected-artifact]
  (let [links {:stage2-compiler-nucleus
               {:artifact (:kind nucleus-artifact)
                :artifact-id (:artifact-id nucleus-artifact)
                :present?
                (= :gravity/p15-s23-stage2-compiler-nucleus-artifact
                   (:kind nucleus-artifact))}
               :compiler-pipeline-manifest
               {:artifact (:kind pipeline-artifact)
                :artifact-id (:artifact-id pipeline-artifact)
                :present?
                (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                   (:kind pipeline-artifact))}
               :accepted-app-execution-proof
               {:artifact (:kind accepted-artifact)
                :artifact-id (:artifact-id accepted-artifact)
                :present?
                (= :gravity/p15-s23-accepted-app-execution-artifact
                   (:kind accepted-artifact))}
               :rejected-app-diagnostic-proof
               {:artifact (:kind rejected-artifact)
                :artifact-id (:artifact-id rejected-artifact)
                :present?
                (= :gravity/p15-s23-rejected-app-diagnostic-artifact
                   (:kind rejected-artifact))}}
        missing (set (for [[k v] links :when (not (:present? v))] k))]
    {:artifact :gravity/p15-s23-stage2-plan-emitter-evidence-link-record
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing)
     :all-required-links-present? (empty? missing)
     :status (if (empty? missing) :complete :failed)}))

(defn p15-s23-stage2-plan-emitter-boundary-record
  [emitter]
  (let [claims (:self-hosting-claims emitter)]
    {:artifact :gravity/p15-s23-stage2-plan-emitter-boundary-record
     :implemented-by (:implemented-by emitter)
     :executed-by (:executed-by emitter)
     :compiled-by (get-in emitter [:lineage :compiled-by])
     :stage0-plan-emitter-replaced? true
     :stage2-plan-emitted-by-gravity-rules? true
     :clojure-stage0-rule-runner?
     (= :clojure-stage0-rule-runner (:executed-by emitter))
     :clojure-stage0-compiler?
     (= :clojure-stage0 (get-in emitter [:lineage :compiled-by]))
     :clojure-instruction-runner?
     (= :clojure-instruction-runner
        (get-in emitter [:seed-boundary :stage0-executor-boundary]))
     :self-hosted-compiler? false
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? claims)
     :clojure-seed-retired? (:clojure-seed-retired? claims)
     :seed-boundary (:seed-boundary emitter)
     :next-required-capability (:next-required-capability emitter)
     :status :complete}))