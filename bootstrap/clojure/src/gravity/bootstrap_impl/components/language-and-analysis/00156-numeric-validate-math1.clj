

(defn numeric-validate-math1!
  [source-path manifest suite]
  (let [families (numeric-family-set suite)
        conversions (:conversion-rules suite)
        symbolic-claims (:symbolic-equality-claims suite)]
    (when-not (set/subset? numeric-required-families families)
      (numeric-fail! "MATH1-FAMILY" source-path manifest {}
                     {:missing-families
                      (set/difference numeric-required-families families)
                      :remediation "Emit every numeric family in the numeric kind lattice."}))
    (doseq [row (:profile-support-matrix suite)]
      (when (and (numeric-constrained-profiles (:profile row))
                 (numeric-allocation-sensitive-families (:family row))
                 (true? (:available? row))
                 (= :unbounded (:allocation row))
                 (not (perf-present? (:provider-bounds row))))
        (numeric-fail! "MATH1-ALLOCATION" source-path manifest row
                       {:remediation "Constrained profiles need bounded provider evidence or must reject BigInt and Ratio."})))
    (when (empty? conversions)
      (numeric-fail! "MATH1-CONVERSION" source-path manifest {}
                     {:missing-fields [:conversion-rules]
                      :remediation "Provide explicit conversion rules for the numeric tower."}))
    (doseq [rule conversions]
      (when (or (:implicit? rule)
                (= :implicit-narrowing (:class rule))
                (= :implicit (:conversion-mode rule)))
        (numeric-fail! "MATH1-NARROW" source-path manifest rule
                       {:remediation "Use checked, rounded, saturating, wrapping, proof-backed, or unsafe conversion syntax."}))
      (when (or (not (perf-present? (:source-family rule)))
                (not (perf-present? (:target-family rule)))
                (not (contains? numeric-conversion-modes
                                (:conversion-mode rule))))
        (numeric-fail! "MATH1-CONVERSION" source-path manifest rule
                       {:missing-fields
                        (cond-> []
                          (not (perf-present? (:source-family rule)))
                          (conj :source-family)
                          (not (perf-present? (:target-family rule)))
                          (conj :target-family)
                          (not (contains? numeric-conversion-modes
                                          (:conversion-mode rule)))
                          (conj :conversion-mode))
                        :remediation "Classify conversions with one of the MATH1 conversion classes."}))
      (when (and (:precision-loss? rule)
                 (not (or (perf-present? (:error-record rule))
                          (perf-present? (:proof rule)))))
        (numeric-fail! "MATH1-PRECISION" source-path manifest rule
                       {:remediation "Precision loss needs an error record or proof-backed conversion."}))
      (when (and (numeric-floating-conversion? rule)
                 (not (perf-present? (:rounding rule))))
        (numeric-fail! "MATH1-ROUNDING" source-path manifest rule
                       {:remediation "Float and approximate conversions must declare rounding policy."}))
      (when (and (numeric-complex-or-elementary-conversion? rule)
                 (not (perf-present? (:branch-policy rule))))
        (numeric-fail! "MATH1-BRANCH" source-path manifest rule
                       {:remediation "Complex and elementary conversions must declare branch and domain policy."}))
      (when (or (not (numeric-family-supported? suite (:profile rule)
                                                (:target rule)
                                                (:source-family rule)))
                (not (numeric-family-supported? suite (:profile rule)
                                                (:target rule)
                                                (:target-family rule))))
        (numeric-fail! "MATH1-PROFILE" source-path manifest rule
                       {:remediation "The active profile and target must support both numeric families used by the conversion."})))
    (doseq [claim symbolic-claims]
      (when (and (:claimed-equal? claim)
                 (not (perf-present? (:proof claim))))
        (numeric-fail! "MATH1-EQUALITY" source-path manifest claim
                       {:remediation "Symbolic or elementary equality requires a proof, interval argument, rewrite certificate, or approximation certificate."})))
    :complete))

(defn numeric-validate-math7!
  [source-path manifest suite]
  (let [records (:mode-records suite)
        grouped (group-by :scope records)]
    (when (empty? records)
      (numeric-fail! "MATH7-MISSING" source-path manifest {}
                     {:missing-fields [:mode-records]
                      :remediation "Emit at least one resolved numeric mode record with a precision contract."}))
    (doseq [[scope scoped] grouped]
      (when (and (perf-present? scope)
                 (> (count (set (map numeric-mode-value scoped))) 1)
                 (not (every? :mode-change scoped)))
        (numeric-fail! "MATH7-SCOPE" source-path manifest (first scoped)
                       {:scope scope
                        :remediation "Conflicting inherited numeric modes require an explicit mode-change trace."})))
    (doseq [record records]
      (let [mode (numeric-mode-value record)
            precision (:precision record)]
        (when (or (not (numeric-mode-record-complete? record))
                  (and mode
                       (not= :target-default mode)
                       (not (contains? numeric-standard-modes mode))))
          (numeric-fail! "MATH7-MISSING" source-path manifest record
                         {:missing-fields
                          (cond-> []
                            (not (perf-present? mode)) (conj :math/mode)
                            (not (perf-present? (:scope record)))
                            (conj :scope)
                            (not (perf-present? precision))
                            (conj :precision))
                          :remediation "Numeric mode records need mode, scope, domain, precision, rounding, exceptional policy, and certificate facts."}))
        (when (and (= :weakening (get-in record [:mode-change :kind]))
                   (not (true? (get-in record [:mode-change :approved?]))))
          (numeric-fail! "MATH7-DOWNGRADE" source-path manifest record
                         {:inherited-mode (get-in record [:mode-change
                                                          :from])
                          :local-override (get-in record [:mode-change :to])
                          :remediation "Mode weakening needs explicit source or package policy authorization and an artifacted downgrade record."}))
        (when (or (= :target-default mode)
                  (= :target-native (:rounding record))
                  (= :target-native (:rounding-policy record)))
          (numeric-fail! "MATH7-TARGET-DEFAULT" source-path manifest record
                         {:remediation "Replace target-default behavior with an explicit hardware-native, libm, or target-contract mode record."}))
        (when (and (numeric-floating-mode-record? record)
                   (not (numeric-precision-contract-meaningful? precision)))
          (numeric-fail! "MATH7-PRECISION" source-path manifest record
                         {:precision precision
                          :remediation "Approximate or floating modes need an absolute, relative, ulp, interval, faithful, or correct-rounding quality field."}))
        (when (and (numeric-floating-mode-record? record)
                   (not (perf-present? (:rounding record))))
          (numeric-fail! "MATH7-ROUNDING" source-path manifest record
                         {:remediation "Floating and approximate mode records must declare rounding policy."}))
        (when (and (numeric-floating-mode-record? record)
                   (not (perf-present? (:float-exceptions record))))
          (numeric-fail! "MATH7-EXCEPTIONAL" source-path manifest record
                         {:remediation "Floating and approximate mode records must declare NaN, infinity, signed-zero, and denormal behavior."}))
        (when (and (seq (:residual-checks record))
                   (false? (:residual-allowed? record)))
          (numeric-fail! "MATH7-RESIDUAL" source-path manifest record
                         {:remediation "Residual checks must be allowed by the active mode or rejected."}))))
    (doseq [provider (:provider-eligibility suite)]
      (when (and (:selected? provider)
                 (not (true? (:eligible? provider))))
        (numeric-fail! "MATH7-PROVIDER" source-path manifest provider
                       {:remediation "Provider selection must satisfy profile, target, numeric mode, precision, branch, certificate, allocation, effects, and capabilities."})))
    :complete))

(defn numeric-validate-math8!
  [source-path manifest suite]
  (let [floating-manifests (:floating-manifests suite)]
    (when (empty? floating-manifests)
      (numeric-fail! "MATH8-MANIFEST" source-path manifest {}
                     {:missing-fields [:floating-manifests]
                      :remediation "Emit at least one floating manifest for floating operations."}))
    (doseq [floating floating-manifests]
      (let [missing-fields (numeric-missing-floating-fields floating)
            mode (:numeric-mode floating)
            backend (:backend-lowering floating)]
        (when (seq missing-fields)
          (numeric-fail! "MATH8-MANIFEST" source-path manifest floating
                         {:missing-fields missing-fields
                          :remediation "Floating manifests must expose format, rounding, exceptional values, status, determinism, and transform policy."}))
        (when-not (contains? numeric-supported-floating-formats
                             (:format floating))
          (numeric-fail! "MATH8-FORMAT" source-path manifest floating
                         {:format (:format floating)
                          :remediation "Declare a supported binary, decimal, bfloat, or accelerator format with target availability."}))
        (when-not (contains? numeric-supported-rounding-policies
                             (:rounding floating))
          (numeric-fail! "MATH8-ROUNDING" source-path manifest floating
                         {:remediation "Use a supported floating rounding policy and map it to the target."}))
        (when-not (and (perf-present? (get-in floating [:nan :quiet]))
                       (perf-present? (get-in floating [:nan
                                                        :signaling])))
          (numeric-fail! "MATH8-NAN" source-path manifest floating
                         {:remediation "NaN behavior must distinguish quiet, signaling, and payload policy where observable."}))
        (when-not (perf-present? (:infinity floating))
          (numeric-fail! "MATH8-INF" source-path manifest floating
                         {:remediation "Infinity behavior must be explicit."}))
        (when-not (contains? #{:preserve :collapse-explicit
                               :not-observable}
                             (:signed-zero floating))
          (numeric-fail! "MATH8-ZERO" source-path manifest floating
                         {:remediation "Signed-zero behavior must be preserved or explicitly made unobservable by mode."}))
        (when (or (not (contains? numeric-supported-denormal-policies
                                  (:denormals floating)))
                  (and (= :target-native-explicit (:denormals floating))
                       (not (perf-present? (:target-contract floating)))))
          (numeric-fail! "MATH8-DENORMAL" source-path manifest floating
                         {:remediation "Denormal and flush behavior must be explicit and tied to a target contract when target-native."}))
        (when (and (numeric-strict-floating-mode? mode)
                   (contains? #{:allowed :contract :contract-fma}
                              (:fma floating))
                   (not (or (perf-present? (:proof floating))
                            (perf-present? (:certificate floating)))))
          (numeric-fail! "MATH8-FMA" source-path manifest floating
                         {:remediation "Strict modes forbid FMA contraction unless proof or certificate preserves the manifest."}))
        (when (and (numeric-strict-floating-mode? mode)
                   (= :allowed (:reassociation floating))
                   (not (or (perf-present? (:proof floating))
                            (perf-present? (:certificate floating)))))
          (numeric-fail! "MATH8-REASSOC" source-path manifest floating
                         {:remediation "Strict modes forbid reassociation unless proof or certificate preserves the manifest."}))
        (when (and (= :observable-through-declared-api
                      (:status-flags floating))
                   (false? (get-in backend [:status-preserved?])))
          (numeric-fail! "MATH8-STATUS" source-path manifest floating
                         {:remediation "Lowering and optimization must preserve observable floating status behavior."}))
        (when (or (false? (:preserves-manifest? backend))
                  (and (false? (:eligible? backend))
                       (not (perf-present? (:fallback backend)))))
          (numeric-fail! "MATH8-BACKEND" source-path manifest floating
                         {:remediation "Backend lowering must preserve the manifest, select a legal fallback, or reject compilation."}))))
    :complete))