(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c14-c-expected-record
  [optimized policy]
  (gravity.lowering/c-record optimized policy))

(defn p15-s23-c14-c-policy
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [base
        (p15-s23-c14-c-policy-base
         c11-artifact checked-core c11-report c13-record c13-rule)
        request-base
        (:request
         (p15-s23-c14-c-expected-record
          c13-record (assoc base :request-id nil)))
        request-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/c14-bounded-c-lowering-request
          :request (dissoc request-base :request-id)})]
    (assoc base :request-id request-id)))

(defn- p15-s23-c14-c-build!
  [candidate source-path c11-artifact checked-core c11-report
   c13-record binding]
  (let [c13-rule (:source-rule c13-record)
        policy
        (p15-s23-c14-c-policy
         c11-artifact checked-core c11-report c13-record c13-rule)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-c14-c-builder-function
         [c13-record policy] "C14-INPUT")
        expected (p15-s23-c14-c-expected-record c13-record policy)]
    (gravity.lowering/assert-c-reconstruction!
     #'p15-s23-c11-mir-require-strict-structure! #'p15-s23-b3-llvm-fail! source-path c11-artifact c13-record policy expected raw)
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/c14-bounded-c-lowering-record raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.compiler/c14-target-lowering binding
      p15-s23-c14-c-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (:source-path binding)})))

(defn p15-s23-b1-backend-manifest
  [c14-record]
  {:artifact :gravity/backend-manifest
   :backend :gravity.backend/llvm
   :version :bounded-authenticated-v1
   :accepts [:gravity/mir]
   :emits [:llvm-ir :elf-x86_64-object :elf-x86_64-executable]
   :requires [:profile-manifest :target-manifest :abi-policy
              :runtime-provider-selection :effect-summary
              :capability-proof-summary :safety-bundle :proof-table
              :source-map :dependency-graph]
   :supports-profiles [:hosted]
   :rejects [:unverified-ir :unsupported-op :missing-proof
             :implicit-ub :ambient-capability :profile-violation]
   :c14-request-id (get-in c14-record [:request :request-id])
   :c14-artifact-id (:artifact-id c14-record)}))
