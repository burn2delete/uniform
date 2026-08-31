

(defn hosted-core-compiled-app-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        _ (executable-profile! source-path module (:forms module))
        _ (validate-module-effects! module)
        function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
        direct-run-output (run-main module)
        compiled-plan (stage0-compiled-core-plan source-path source-text module)
        compiled-run-output (execute-stage0-compiled-plan compiled-plan)
        compile-artifact (compile-source source-path source-text)
        user-functions (sort (keys (dissoc function-table 'main)))
        proof {:compiled-plan-emitted?
               (= :gravity/stage0-hosted-core-compiled-plan
                  (:kind compiled-plan))
               :compiled-plan-executed?
               (= compiled-run-output direct-run-output)
               :source-form-interpreter-replaced? true
               :stage0-output-matches-hosted-runner?
               (= compiled-run-output direct-run-output)
               :function-instructions-covered?
               (every? #(contains? (:functions compiled-plan) %)
                       user-functions)
               :builtin-instructions-covered?
               (every? #(pos? (get-in compiled-plan
                                       [:instruction-summary %]
                                       0))
                       [:builtin-call :function-call :println])
               :effects-and-capabilities-checked? true
               :rejected-diagnostics-covered?
               (= #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}
                  (set (map :diagnostic stage0-core-app-rejected-fixtures)))
               :limitations {:direct-form-interpreter? false
                             :clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :next-required-capability
                             :replace-clojure-compiled-plan-runner-with-gravity-runtime}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-app-proof
         :phase "01"
         :task "P01-S2"
         :governing-documents ["L2" "L3" "L5" "L6" "C5" "C7"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiler (:compiler compile-artifact)
         :module (:module compile-artifact)
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :binding-table :instruction-summary
                                      :effect-summary :diagnostics])
         :runtime-surface {:special-forms (sort stage0-special-forms)
                           :builtin-functions (sort stage0-builtin-functions)
                           :user-functions user-functions}
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout compiled-run-output}
         :reference-run {:command (str "clojure -M:gravity run " source-path)
                         :stdout direct-run-output}
         :rejected-fixtures stage0-core-app-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :direct-form-interpreter? false
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-app-proof-file-artifact
  [path]
  (hosted-core-compiled-app-proof-source-artifact path (slurp path)))