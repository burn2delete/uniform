

(def numeric-conversion-modes
  #{:widening-exact :checked-narrowing :saturating :wrapping
    :explicitly-rounded :approximate-with-error-record :proof-backed
    :unsafe-reinterpretation})

(def numeric-standard-modes
  #{:exact :checked :wrapping :saturating :symbolic :exact-real
    :interval :correctly-rounded :faithful :certified-approx
    :relaxed :fast-approx :hardware-native :libm :eml-normalized})

(def numeric-floating-or-approx-modes
  #{:correctly-rounded :faithful :certified-approx :relaxed
    :fast-approx :hardware-native :libm})

(def numeric-result-quality-fields
  [:absolute-error-max :relative-error-max :ulp-max
   :correct-rounding-target :faithful-rounding-target
   :interval-width :symbolic-exactness])

(def numeric-supported-rounding-policies
  #{:nearest-even :nearest-ties-to-even :nearest-ties-away
    :toward-zero :toward-positive-infinity :toward-negative-infinity
    :stochastic})

(def numeric-supported-floating-formats
  #{:binary16 :bfloat16 :binary32 :binary64 :binary128
    :decimal32 :decimal64 :decimal128})

(def numeric-supported-denormal-policies
  #{:preserve :flush-inputs-to-zero :flush-results-to-zero
    :flush-all :target-native-explicit})

(def numeric-default-kind-lattice
  (mapv (fn [family] {:family family :explicit? true})
        [:fixed-integer :bigint :ratio :real :float :complex
         :interval :symbolic :quantity]))