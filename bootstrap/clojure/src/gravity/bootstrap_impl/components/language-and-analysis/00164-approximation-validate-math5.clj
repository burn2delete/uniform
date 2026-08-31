

(defn approximation-validate-math5!
  [source-path manifest eml-artifact suite]
  (let [source-efirs (set (map :source-efir (:eml-expression-tree eml-artifact)))]
    (doseq [certificate (:certificates suite)]
      (let [missing-fields (approximation-missing-fields certificate)
            absolute-max (get-in certificate [:precision :absolute-error-max])
            approximation-error (get-in certificate [:error-proof :approximation])
            roundoff (get-in certificate [:error-proof :roundoff])
            combined (get-in certificate [:error-proof :combined])]
        (when (seq missing-fields)
          (approximation-fail! "MATH5-CERT-SHAPE" source-path manifest certificate
                               {:missing-fields missing-fields
                                :remediation "State EFIR target, function, domain, mode, precision, branch, implementation, error, target, and checker fields."}))
        (when-not (contains? source-efirs (:target-efir certificate))
          (approximation-fail! "MATH5-EFIR" source-path manifest certificate
                               {:remediation "Tie the certificate to an EFIR graph that survived EML lowering."}))
        (when (or (not= :complete (:domain-coverage certificate))
                  (:domain-widens-efir? certificate))
          (approximation-fail! "MATH5-DOMAIN" source-path manifest certificate
                               {:remediation "Prove full EFIR domain coverage and reject silent widening."}))
        (when (or (not (perf-present? (:branch-policy certificate)))
                  (false? (:branch-compatible? certificate)))
          (approximation-fail! "MATH5-BRANCH" source-path manifest certificate
                               {:remediation "Match EFIR branch and exceptional-value policy."}))
        (when (or (not (number? approximation-error))
                  (not (number? combined))
                  (not (number? absolute-max))
                  (> combined absolute-max))
          (approximation-fail! "MATH5-APPROX-ERROR" source-path manifest certificate
                               {:failing-bound combined
                                :remediation "Keep combined approximation error inside the precision contract."}))
        (when (or (not (true? (:roundoff-valid? certificate)))
                  (not (number? roundoff))
                  (and (number? absolute-max) (number? roundoff)
                       (> roundoff absolute-max)))
          (approximation-fail! "MATH5-ROUNDOFF" source-path manifest certificate
                               {:failing-bound roundoff
                                :remediation "Record valid roundoff evidence separately from approximation error."}))
        (when (or (not (true? (:target-satisfied? certificate)))
                  (not (perf-present? (:target-assumptions certificate))))
          (approximation-fail! "MATH5-TARGET" source-path manifest certificate
                               {:remediation "Satisfy or guard target assumptions before accepting a certificate."}))
        (when (or (not (true? (get-in certificate [:checker :independent?])))
                  (not (true? (get-in certificate [:checker :replayable?])))
                  (not (perf-present? (get-in certificate [:checker :trust-root]))))
          (approximation-fail! "MATH5-CHECKER" source-path manifest certificate
                               {:remediation "Use an independent replayable checker with an accepted trust root."}))))
    (doseq [selection (:selected-implementations suite)]
      (when (and (:selected? selection)
                 (or (not (:evidence-accepted? selection))
                     (not (perf-present? (:certificate-id selection)))))
        (approximation-fail! "MATH5-SELECTION" source-path manifest selection
                             {:remediation "Runtime selection needs accepted certificate evidence tied to the EFIR graph."})))
    :complete))