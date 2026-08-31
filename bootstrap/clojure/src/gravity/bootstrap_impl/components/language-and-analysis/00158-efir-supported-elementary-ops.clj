

(def efir-supported-elementary-ops
  #{:sin :cos :exp :log :sqrt :pow :tanh :sigmoid})

(def efir-node-ops
  #{:const :var :arith :call :let :piecewise :constraint :eml})

(def efir-default-declarations
  [{:function-id :math/sigmoid
    :source-span "math/efir.gravity:8:1"
    :domain {'x {:real [-8.0 8.0]}}
    :codomain {:real [0.0 1.0]}
    :branch-policy :real-only
    :exceptional-values {:nan :propagate
                         :inf :saturate-by-mode}
    :semantic-form '(/ 1 (+ 1 (exp (- x))))
    :numeric-modes #{:exact :faithful :certified-approx}
    :provider-requirements #{:domain :codomain :branch-policy
                             :numeric-mode :precision :certificate}
    :efir-anchor :efir/sigmoid
    :status :complete}])

(def efir-default-provider-manifest
  [{:provider :provider/poly-sigmoid-f32
    :function-id :math/sigmoid
    :selected? true
    :eligible? true
    :efir-anchor :efir/sigmoid
    :domain {'x {:real [-8.0 8.0]}}
    :codomain {:real [0.0 1.0]}
    :branch-policy :real-only
    :numeric-mode :certified-approx
    :precision {:type :F32 :absolute-error-max 1.0e-5}
    :profile :native
    :target :llvm-x86-64-linux
    :target-features #{:sse2}
    :effects #{}
    :capabilities #{}
    :allocation :none
    :certificate :cert/sigmoid-poly-f32
    :trust-root :gravity-stage0-checker}
   {:provider :provider/libm-exp
    :function-id :math/sigmoid
    :selected? false
    :eligible? false
    :efir-anchor :efir/sigmoid
    :rejected-reason :certificate-missing
    :numeric-mode :certified-approx
    :profile :native
    :target :llvm-x86-64-linux}])

(def efir-default-graphs
  [{:ir :gravity/efir
    :graph-id :efir/sigmoid
    :semantic-anchor {:typed-core [:typed/math-sigmoid-call]
                      :mir-ops []}
    :source-anchors [{:span "math/efir.gravity:8:1"
                      :origin :source}]
    :domain {'x {:real [-8.0 8.0]}}
    :codomain {:real [0.0 1.0]}
    :nodes [{:id :x
             :op :var
             :domain {:real [-8.0 8.0]}
             :codomain {:real [-8.0 8.0]}
             :source "math/efir.gravity:8:20"}
            {:id :neg-x
             :op :arith
             :arith-op :-
             :args [:x]
             :domain {:real [-8.0 8.0]}
             :codomain {:real [-8.0 8.0]}
             :source "math/efir.gravity:8:31"}
            {:id :exp-neg-x
             :op :call
             :elementary-op :exp
             :args [:neg-x]
             :domain {:real [-8.0 8.0]}
             :codomain {:real [0.0003 2981.0]}
             :branch-policy :real-only
             :source "math/efir.gravity:8:26"}
            {:id :denominator
             :op :arith
             :arith-op :+
             :args [1 :exp-neg-x]
             :domain {:real [1.0003 2982.0]}
             :codomain {:real [1.0003 2982.0]}
             :source "math/efir.gravity:8:22"}
            {:id :sigmoid
             :op :arith
             :arith-op :/
             :args [1 :denominator]
             :domain {:real [1.0003 2982.0]}
             :codomain {:real [0.0 1.0]}
             :source "math/efir.gravity:8:18"}]
    :numeric-mode :certified-approx
    :branch-policy :real-only
    :precision {:type :F32 :absolute-error-max 1.0e-5}
    :proof-obligations #{:domain-coverage :roundoff :branch-consistency}
    :runtime-anchor :provider/poly-sigmoid-f32
    :verifier {:name :gravity.math/verify-efir
               :result :accepted}
    :status :complete}])

(def efir-default-selection-decisions
  [{:decision-id :select/sigmoid-poly-f32
    :function-id :math/sigmoid
    :efir-anchor :efir/sigmoid
    :selected-provider :provider/poly-sigmoid-f32
    :rejected-providers [:provider/libm-exp]
    :numeric-mode :certified-approx
    :branch-policy :real-only
    :profile :native
    :target :llvm-x86-64-linux
    :status :selected}])

(def efir-default-equivalence-claims
  [{:claim-id :equiv/sigmoid-source-to-efir
    :function-id :math/sigmoid
    :source-expression '(/ 1 (+ 1 (exp (- x))))
    :efir-graph :efir/sigmoid
    :domain {'x {:real [-8.0 8.0]}}
    :proof :proof/sigmoid-efir-anchor
    :status :accepted}])

(def efir-default-rewrite-records
  [{:rewrite-id :rewrite/sigmoid-none
    :graph-id :efir/sigmoid
    :input-graph :efir/sigmoid
    :output-graph :efir/sigmoid
    :semantic-proof :proof/sigmoid-identity
    :status :not-applied}])

(def efir-default-eml-records
  [{:eml-id :eml/sigmoid-not-lowered
    :graph-id :efir/sigmoid
    :lowered? false
    :preserves-branch-policy? true
    :preserves-source? true
    :status :not-required}])

(defn efir-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :efir] {})]
    (assoc source-suite
           :elementary-declarations
           (if (contains? source-suite :elementary-declarations)
             (vec (:elementary-declarations source-suite))
             efir-default-declarations)
           :provider-manifest
           (if (contains? source-suite :provider-manifest)
             (vec (:provider-manifest source-suite))
             efir-default-provider-manifest)
           :efir-graphs
           (if (contains? source-suite :efir-graphs)
             (vec (:efir-graphs source-suite))
             efir-default-graphs)
           :selection-decisions
           (if (contains? source-suite :selection-decisions)
             (vec (:selection-decisions source-suite))
             efir-default-selection-decisions)
           :equivalence-claims
           (if (contains? source-suite :equivalence-claims)
             (vec (:equivalence-claims source-suite))
             efir-default-equivalence-claims)
           :rewrite-records
           (if (contains? source-suite :rewrite-records)
             (vec (:rewrite-records source-suite))
             efir-default-rewrite-records)
           :eml-records
           (if (contains? source-suite :eml-records)
             (vec (:eml-records source-suite))
             efir-default-eml-records))))

(defn efir-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH2-DECLARATION" "elementary declaration is incomplete"
           "MATH2-DOMAIN" "elementary input domain is missing or invalid"
           "MATH2-BRANCH" "elementary branch policy is missing or incompatible"
           "MATH2-PROVIDER" "no eligible elementary provider exists"
           "MATH2-NUMERIC-MODE" "elementary provider does not support the numeric mode"
           "MATH2-CERTIFICATE" "elementary provider requires a missing certificate"
           "MATH2-EQUIVALENCE" "elementary equality claim lacks proof"
           "MATH2-EFFECT" "elementary provider performs effects outside profile policy"
           "MATH2-TARGET" "elementary provider lacks required target feature"
           "MATH3-NODE" "EFIR node shape is invalid"
           "MATH3-DOMAIN" "EFIR graph or node lacks domain information"
           "MATH3-CODOMAIN" "EFIR graph or node lacks codomain information"
           "MATH3-BRANCH" "EFIR graph or node lacks branch policy"
           "MATH3-PRECISION" "EFIR graph lacks numeric mode or precision contract"
           "MATH3-SOURCE" "EFIR graph lacks source anchor or semantic anchor"
           "MATH3-REWRITE" "EFIR rewrite claims equality without proof"
           "MATH3-EML" "EML lowering loses branch or source facts"
           "MATH3-RUNTIME" "runtime implementation selection lacks EFIR anchor"
           "EFIR record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :function-id (:function-id record)
                 :efir-node (or (:node-id record) (:id record))
                 :graph-id (:graph-id record)
                 :numeric-mode (:numeric-mode record)
                 :branch-policy (:branch-policy record)
                 :provider (:provider record)
                 :diagnostic-family :efir-validation}
                extra)))

(defn efir-declaration-missing-fields
  [decl]
  (vec (remove #(perf-present? (get decl %))
               [:function-id :domain :codomain :branch-policy
                :exceptional-values :semantic-form :numeric-modes
                :provider-requirements :efir-anchor])))

(defn efir-provider-matches-declaration?
  [decl provider]
  (and (= (:function-id decl) (:function-id provider))
       (= (:efir-anchor decl) (:efir-anchor provider))
       (contains? (set (:numeric-modes decl)) (:numeric-mode provider))
       (= (:branch-policy decl) (:branch-policy provider))))