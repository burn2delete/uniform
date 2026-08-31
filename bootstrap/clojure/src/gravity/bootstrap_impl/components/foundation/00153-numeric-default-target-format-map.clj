

(def numeric-default-target-format-map
  [{:target :llvm-x86-64-linux :format :binary32
    :rounding #{:nearest-ties-to-even :toward-zero}
    :denormals :preserve :status-flags true}
   {:target :llvm-x86-64-linux :format :binary64
    :rounding #{:nearest-ties-to-even :toward-zero}
    :denormals :preserve :status-flags true}
   {:target :cuda-sm90 :format :binary32
    :rounding #{:nearest-ties-to-even :toward-zero}
    :denormals :target-native-explicit :status-flags false}])

(def numeric-default-conversion-rules
  [{:rule-id :convert/i32-to-i64
    :profile :native
    :target :llvm-x86-64-linux
    :source-family :fixed-integer
    :target-family :fixed-integer
    :source-type :I32
    :target-type :I64
    :conversion-mode :widening-exact
    :source-span "math/numeric.gravity:8:11"}
   {:rule-id :convert/i64-to-i32-checked
    :profile :native
    :target :llvm-x86-64-linux
    :source-family :fixed-integer
    :target-family :fixed-integer
    :source-type :I64
    :target-type :I32
    :conversion-mode :checked-narrowing
    :proof :range/i64-fits-i32
    :source-span "math/numeric.gravity:9:11"}
   {:rule-id :convert/f64-to-f32-rounded
    :profile :native
    :target :llvm-x86-64-linux
    :source-family :float
    :target-family :float
    :source-type :F64
    :target-type :F32
    :conversion-mode :explicitly-rounded
    :rounding :nearest-ties-to-even
    :precision-loss? true
    :error-record :round/f64-to-f32
    :source-span "math/numeric.gravity:10:11"}
   {:rule-id :convert/complex-principal-real
    :profile :native
    :target :llvm-x86-64-linux
    :source-family :complex
    :target-family :float
    :source-type :ComplexF64
    :target-type :F64
    :conversion-mode :proof-backed
    :rounding :nearest-ties-to-even
    :branch-policy :principal-real
    :proof :proof/imaginary-zero
    :source-span "math/numeric.gravity:11:11"}])

(def numeric-default-mode-records
  [{:record-id :mode/checked-int
    :math/mode :checked
    :scope {:kind :namespace :name 'math.numeric}
    :profile :native
    :target :llvm-x86-64-linux
    :family :fixed-integer
    :domain {:n [:range -2147483648 2147483647]}
    :precision {:type :I32 :symbolic-exactness true}
    :integer-overflow :checked
    :optimization {:reassociate false
                   :contract-fma :not-applicable}
    :certificate {:required false}
    :source-span "math/numeric.gravity:4:1"}
   {:record-id :mode/certified-sin
    :math/mode :certified-approx
    :scope {:kind :function :name 'math.numeric/sin-fast}
    :profile :native
    :target :llvm-x86-64-linux
    :family :float
    :domain {'x {:real [-3.1415927 3.1415927]}}
    :precision {:type :F32
                :absolute-error-max 1.0e-5
                :relative-error-max nil
                :ulp-max nil}
    :rounding :nearest-even
    :integer-overflow :checked
    :float-exceptions {:nan :propagate
                       :inf :domain-error
                       :signed-zero :preserve
                       :denormals :preserve}
    :optimization {:reassociate false
                   :contract-fma :only-if-proven}
    :certificate {:required true
                  :accepted-checkers #{:gravity-interval-checker}}
    :source-span "math/numeric.gravity:13:1"}
   {:record-id :mode/hardware-native-f32
    :math/mode :hardware-native
    :scope {:kind :provider :name :x86/f32-sqrt}
    :profile :native
    :target :llvm-x86-64-linux
    :family :float
    :domain {'x {:real [0.0 65504.0]}}
    :precision {:type :F32 :ulp-max 1}
    :rounding :nearest-even
    :integer-overflow :checked
    :float-exceptions {:nan :propagate
                       :inf :propagate
                       :signed-zero :preserve
                       :denormals :preserve}
    :optimization {:reassociate false
                   :contract-fma :only-if-proven}
    :certificate {:required true
                  :target-contract :x86-sse-sqrt}
    :target-contract :x86-sse-sqrt
    :source-span "math/numeric.gravity:20:1"}])

(def numeric-default-provider-eligibility
  [{:provider :provider/poly-sin-f32
    :function :sin
    :selected? true
    :eligible? true
    :profile :native
    :target :llvm-x86-64-linux
    :numeric-mode :certified-approx
    :precision {:absolute-error-max 1.0e-5}
    :branch-policy :real-only
    :certificate :cert/sin-poly-f32
    :effects #{}
    :capabilities #{}
    :allocation :none}
   {:provider :provider/libm-sin
    :function :sin
    :selected? false
    :eligible? false
    :rejected-reason :not-correctly-rounded-for-contract
    :profile :native
    :target :llvm-x86-64-linux
    :numeric-mode :certified-approx}])

(def numeric-default-floating-manifests
  [{:manifest-id :float/f64-dot-strict
    :operation :f64-dot-strict
    :profile :native
    :target :llvm-x86-64-linux
    :format :binary64
    :rounding :nearest-ties-to-even
    :numeric-mode :faithful
    :precision {:ulp-max 1}
    :exceptions {:invalid :quiet-nan
                 :divide-by-zero :infinity
                 :overflow :infinity
                 :underflow :subnormal-or-zero-by-policy
                 :inexact :status-flag}
    :nan {:quiet :propagate
          :signaling :quiet-and-flag
          :payload :not-portable}
    :infinity :ieee754
    :signed-zero :preserve
    :denormals :preserve
    :status-flags :observable-through-declared-api
    :fma :forbidden-unless-certified
    :reassociation :forbidden
    :reciprocal-substitution :forbidden
    :determinism :profile-target-stable
    :backend-lowering {:backend :llvm
                       :instruction :fadd-fmul
                       :preserves-manifest? true
                       :eligible? true
                       :status-preserved? true}}
   {:manifest-id :float/f32-relaxed-fma
    :operation :f32-relaxed-fma
    :profile :native
    :target :llvm-x86-64-linux
    :format :binary32
    :rounding :nearest-ties-to-even
    :numeric-mode :relaxed
    :precision {:ulp-max 2}
    :exceptions {:invalid :quiet-nan
                 :divide-by-zero :infinity
                 :overflow :infinity
                 :underflow :subnormal-or-zero-by-policy
                 :inexact :ignored-by-mode}
    :nan {:quiet :propagate
          :signaling :quiet-and-flag
          :payload :not-portable}
    :infinity :ieee754
    :signed-zero :preserve
    :denormals :preserve
    :status-flags :unobservable-by-mode
    :fma :allowed
    :reassociation :allowed
    :reciprocal-substitution :forbidden
    :determinism :target-stable
    :backend-lowering {:backend :llvm
                       :instruction :fmadd
                       :preserves-manifest? true
                       :eligible? true
                       :status-preserved? true}}])