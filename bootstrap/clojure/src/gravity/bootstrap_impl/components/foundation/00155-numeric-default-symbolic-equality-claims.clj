

(def numeric-default-symbolic-equality-claims
  [{:claim-id :symbolic/sin-square-plus-cos-square
    :claimed-equal? true
    :lhs '(+ (* (sin x) (sin x)) (* (cos x) (cos x)))
    :rhs 1
    :domain {'x {:real [-3.1415927 3.1415927]}}
    :proof :proof/trig-identity-interval
    :source-span "math/numeric.gravity:30:1"}])

(defn numeric-mode-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :numeric] {})]
    (assoc source-suite
           :numeric-kind-lattice
           (or (:numeric-kind-lattice source-suite)
               numeric-default-kind-lattice)
           :profile-support-matrix
           (vec (concat numeric-default-profile-support-matrix
                        (:profile-support-matrix source-suite)))
           :conversion-rules
           (if (contains? source-suite :conversion-rules)
             (vec (:conversion-rules source-suite))
             numeric-default-conversion-rules)
           :mode-records
           (if (contains? source-suite :mode-records)
             (vec (:mode-records source-suite))
             numeric-default-mode-records)
           :floating-manifests
           (if (contains? source-suite :floating-manifests)
             (vec (:floating-manifests source-suite))
             numeric-default-floating-manifests)
           :provider-eligibility
           (if (contains? source-suite :provider-eligibility)
             (vec (:provider-eligibility source-suite))
             numeric-default-provider-eligibility)
           :efir-numeric-annotations
           (if (contains? source-suite :efir-numeric-annotations)
             (vec (:efir-numeric-annotations source-suite))
             numeric-default-efir-annotations)
           :symbolic-equality-claims
           (if (contains? source-suite :symbolic-equality-claims)
             (vec (:symbolic-equality-claims source-suite))
             numeric-default-symbolic-equality-claims))))

(defn numeric-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH1-FAMILY" "numeric family is unavailable or not explicit"
           "MATH1-CONVERSION" "numeric conversion rule is missing or illegal"
           "MATH1-NARROW" "implicit narrowing conversion is rejected"
           "MATH1-PRECISION" "numeric conversion loses precision without evidence"
           "MATH1-ROUNDING" "numeric conversion is missing rounding policy"
           "MATH1-BRANCH" "complex or elementary conversion lacks branch policy"
           "MATH1-ALLOCATION" "allocation-heavy numeric family lacks bounded provider support"
           "MATH1-EQUALITY" "symbolic equality was claimed without proof"
           "MATH1-PROFILE" "numeric family or mode is not supported by the active profile"
           "MATH7-MISSING" "numeric mode or precision contract is missing"
           "MATH7-SCOPE" "numeric mode inheritance is ambiguous or conflicting"
           "MATH7-DOWNGRADE" "numeric mode weakening lacks authorization"
           "MATH7-TARGET-DEFAULT" "target-default numeric behavior is not a portable contract"
           "MATH7-PRECISION" "precision contract is incomplete or impossible"
           "MATH7-PROVIDER" "selected numeric provider is not mode eligible"
           "MATH7-ROUNDING" "numeric mode record lacks rounding policy"
           "MATH7-EXCEPTIONAL" "numeric mode record lacks exceptional-value policy"
           "MATH7-RESIDUAL" "residual runtime checks are not allowed by the numeric mode"
           "MATH8-MANIFEST" "floating manifest is missing or incomplete"
           "MATH8-FORMAT" "floating format is unavailable or mismatched"
           "MATH8-ROUNDING" "floating rounding policy is missing or unsupported"
           "MATH8-NAN" "NaN behavior is unspecified"
           "MATH8-INF" "infinity behavior is unspecified"
           "MATH8-ZERO" "signed-zero behavior is unspecified or mismatched"
           "MATH8-DENORMAL" "denormal behavior is unspecified"
           "MATH8-FMA" "FMA contraction is illegal under the active manifest"
           "MATH8-REASSOC" "floating reassociation is illegal under the active manifest"
           "MATH8-STATUS" "observable floating status behavior is not preserved"
           "MATH8-BACKEND" "backend lowering cannot satisfy the floating manifest"
           "numeric mode record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :numeric-family (or (:family record)
                                     (:source-family record)
                                     (:target-family record))
                 :numeric-mode (or (:math/mode record)
                                   (:numeric-mode record)
                                   (:mode record))
                 :conversion-mode (:conversion-mode record)
                 :rounding-policy (or (:rounding record)
                                      (:rounding-policy record))
                 :provider (:provider record)
                 :operation (or (:operation record) (:record-id record))
                 :manifest-id (or (:manifest-id record)
                                  (:operation record))
                 :diagnostic-family :numeric-mode-validation}
                extra)))

(defn numeric-family-set
  [suite]
  (set (map :family (:numeric-kind-lattice suite))))

(defn numeric-support-row?
  [row profile target family]
  (and (= profile (:profile row))
       (= family (:family row))
       (true? (:available? row))
       (or (= target (:target row))
           (= :portable (:target row)))))

(defn numeric-family-supported?
  [suite profile target family]
  (boolean
   (some #(numeric-support-row? % profile target family)
         (:profile-support-matrix suite))))

(defn numeric-floating-conversion?
  [rule]
  (or (= :float (:source-family rule))
      (= :float (:target-family rule))
      (= :floating (:kind rule))
      (contains? numeric-floating-or-approx-modes (:numeric-mode rule))))

(defn numeric-complex-or-elementary-conversion?
  [rule]
  (or (= :complex (:source-family rule))
      (= :complex (:target-family rule))
      (= :elementary (:kind rule))
      (:elementary-operation rule)))

(defn numeric-precision-contract-meaningful?
  [precision]
  (boolean
   (some #(perf-present? (get precision %))
         numeric-result-quality-fields)))

(defn numeric-mode-value
  [record]
  (or (:math/mode record) (:mode record) (:numeric-mode record)))

(defn numeric-floating-mode-record?
  [record]
  (or (contains? numeric-floating-or-approx-modes (numeric-mode-value record))
      (= :float (:family record))
      (perf-present? (:floating-manifest record))))

(defn numeric-mode-record-complete?
  [record]
  (and (perf-present? (numeric-mode-value record))
       (perf-present? (:scope record))
       (perf-present? (:precision record))))

(defn numeric-required-floating-manifest-fields
  []
  [:operation :format :rounding :numeric-mode :precision :exceptions
   :nan :infinity :signed-zero :denormals :status-flags :fma
   :reassociation :reciprocal-substitution :determinism])

(defn numeric-missing-floating-fields
  [manifest]
  (vec (remove #(contains? manifest %)
               (numeric-required-floating-manifest-fields))))

(defn numeric-strict-floating-mode?
  [mode]
  (contains? #{:strict :correctly-rounded :faithful
               :certified-approx :exact} mode))