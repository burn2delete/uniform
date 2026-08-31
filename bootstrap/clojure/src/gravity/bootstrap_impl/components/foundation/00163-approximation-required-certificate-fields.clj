

(def approximation-required-certificate-fields
  [:certificate-id :target-efir :target-nodes :function-id :domain :codomain
   :numeric-mode :precision :branch-policy :implementation :error-proof
   :target-assumptions :checker])

(def approximation-default-certificates
  [{:artifact :gravity/math-approximation-certificate
    :certificate-id :cert/sigmoid-poly-f32
    :target-efir :efir/sigmoid
    :target-nodes [:exp-neg-x :denominator :sigmoid]
    :function-id :math/sigmoid
    :domain {'x {:real [-8.0 8.0]}}
    :codomain {:real [0.0 1.0]}
    :numeric-mode :certified-approx
    :precision {:type :F32
                :absolute-error-max 1.0e-5
                :relative-error-max 1.0e-5
                :absolute-error-fallback-near-zero true}
    :branch-policy {:complex-intermediates :forbidden
                    :exceptional-values {:nan :propagate
                                         :inf :saturate-by-mode}}
    :implementation {:kind :polynomial
                     :basis :minimax
                     :degree 5
                     :coefficient-hash "sha256:stage0-sigmoid-coefficients"
                     :coefficient-rounding :nearest-ties-to-even
                     :evaluation :estrin
                     :range-reduction :none
                     :allocation :none}
    :error-proof {:approximation 4.0e-6
                  :coefficient 5.0e-7
                  :roundoff 2.0e-6
                  :reconstruction 5.0e-7
                  :target-lowering 0.0
                  :combined 7.0e-6
                  :method :interval}
    :target-assumptions {:arch :x86-64
                         :features #{:sse2}
                         :format :binary32
                         :rounding :nearest-ties-to-even
                         :denormals :preserve
                         :fma :forbidden
                         :reassociation :forbidden
                         :fast-math-flags #{}}
    :checker {:name :gravity-stage0-interval-checker
              :version "1"
              :input-hash "sha256:stage0-sigmoid-checker-input"
              :transcript-hash "sha256:stage0-sigmoid-checker-transcript"
              :independent? true
              :replayable? true
              :trust-root :gravity-stage0-checker}
    :domain-coverage :complete
    :branch-compatible? true
    :roundoff-valid? true
    :target-satisfied? true
    :status :accepted}])

(def approximation-default-candidates
  [{:candidate-id :candidate/sigmoid-poly-f32
    :source-eml :candidate/sigmoid-eml
    :certificate-id :cert/sigmoid-poly-f32
    :family :polynomial
    :status :certified}
   {:candidate-id :candidate/sigmoid-table-fast
    :source-eml :candidate/sigmoid-fast-unproved
    :family :table
    :status :rejected
    :rejection-reasons [:proof-missing :target-assumptions-missing]}])

(def approximation-default-selection
  [{:provider :provider/poly-sigmoid-f32
    :certificate-id :cert/sigmoid-poly-f32
    :efir-graph :efir/sigmoid
    :candidate-id :candidate/sigmoid-poly-f32
    :selected? true
    :evidence-accepted? true
    :profile :native
    :target :llvm-x86-64-linux
    :effects #{}
    :capabilities #{}
    :allocation :none
    :status :selected}])

(defn approximation-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :approximation] {})
        certificates (cond
                       (contains? source-suite :certificates)
                       (vec (:certificates source-suite))

                       (contains? source-suite :certificate-overrides)
                       (mapv #(merge (first approximation-default-certificates) %)
                             (:certificate-overrides source-suite))

                       :else
                       approximation-default-certificates)
        selections (cond
                     (contains? source-suite :selected-implementations)
                     (vec (:selected-implementations source-suite))

                     (contains? source-suite :selected-implementation-overrides)
                     (mapv #(merge (first approximation-default-selection) %)
                           (:selected-implementation-overrides source-suite))

                     :else
                     approximation-default-selection)]
    (assoc source-suite
           :certificates certificates
           :candidates
           (if (contains? source-suite :candidates)
             (vec (:candidates source-suite))
             approximation-default-candidates)
           :selected-implementations selections)))

(defn approximation-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH5-CERT-SHAPE" "approximation certificate is malformed"
           "MATH5-EFIR" "approximation certificate has missing or mismatched EFIR anchors"
           "MATH5-DOMAIN" "approximation certificate has incomplete domain coverage"
           "MATH5-BRANCH" "approximation certificate changes branch or exceptional-value behavior"
           "MATH5-APPROX-ERROR" "approximation error exceeds the precision contract"
           "MATH5-ROUNDOFF" "roundoff evidence is missing or excessive"
           "MATH5-TARGET" "target assumptions are unsatisfied"
           "MATH5-CHECKER" "checker evidence is unsupported or unreplayable"
           "MATH5-SELECTION" "runtime implementation selected without accepted certificate evidence"
           "approximation record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :certificate-id (:certificate-id record)
                 :graph-id (or (:target-efir record) (:efir-graph record))
                 :function-id (:function-id record)
                 :domain (:domain record)
                 :numeric-mode (:numeric-mode record)
                 :precision (:precision record)
                 :checker (get-in record [:checker :name])
                 :failing-bound (:failing-bound extra)
                 :diagnostic-family :certified-approximation}
                extra)))

(defn approximation-missing-fields
  [certificate]
  (vec (remove #(perf-present? (get certificate %))
               approximation-required-certificate-fields)))