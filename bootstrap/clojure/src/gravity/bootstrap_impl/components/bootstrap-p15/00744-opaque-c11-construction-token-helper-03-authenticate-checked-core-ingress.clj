(def ^:private __gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
  (let [classify-checked-core-ingress-pair __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
        invoke-checked-core-verifier! __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier]
    (fn authenticate-checked-core-ingress!
      [checked-core context boundary-prefix]
      ;; Comparator-safe bounded carrier walks intentionally precede all map
      ;; lookups.  Family classification remains top-level only; canonical
      ;; replay and C11 source lookup wait for the exact pair and the stricter
      ;; new-family no-sorted-carrier rescan below.
      (p15-s23-c11-mir-require-trusted-carrier!
       "<c11-mir>" :preclassified-checked-core
       checked-core :default-only)
      (p15-s23-c11-mir-require-trusted-carrier!
       "<c11-mir>" :preclassified-checked-core-context
       context :default-only)
      (let [ingress-mode
            (classify-checked-core-ingress-pair checked-core context)
            artifact-kind
            (when (p15-s23-c11-exact-bounded-map? checked-core 128)
              (:kind checked-core))
            context-kind
            (when (p15-s23-c11-exact-bounded-map? context 5)
              (:kind context))]
        (when (= :invalid ingress-mode)
          (p15-s23-c11-mir-fail!
           "C11-MODULE"
           (p15-s23-c11-ingress-source-path context)
           {}
           {:missing-fact :authenticated-checked-core-ingress-pair
            :checked-core-artifact-kind
            (if (keyword? artifact-kind) artifact-kind :invalid)
            :checked-core-context-kind
            (if (keyword? context-kind) context-kind :legacy-or-invalid)
            :checked-core-ingress-mode :invalid}))
        (let [source-path (p15-s23-c11-ingress-source-path context)
              sorted-policy
              (if (= :effectful-reference ingress-mode)
                :default-only :reject)]
          (p15-s23-c11-mir-require-trusted-carrier!
           source-path :checked-core checked-core sorted-policy)
          (p15-s23-c11-mir-require-trusted-carrier!
           source-path :checked-core-context context sorted-policy))
        (let [report
              (binding [*p15-s23-c11-mir-diagnostic-context*
                        (p15-s23-c11-mir-diagnostic-context
                         checked-core context {})]
                (invoke-checked-core-verifier!
                 checked-core context ingress-mode boundary-prefix))
              expected
              (p15-s23-c11-mir-expected-ingress-semantic
               checked-core context)]
          (case ingress-mode
            :gravity-source-pure
            (p15-s23-c11-mir-require!
             (and (= :passed (:status report))
                  (= :gravity-source
                     (get-in report
                             [:gravity-source-verification
                              :semantic-authority]))
                  (= :gravity-source
                     (get-in report
                             [:gravity-candidate-verification
                              :semantic-authority]))
                  (= :passed
                     (get-in report
                             [:gravity-source-verification :status]))
                  (= :passed
                     (get-in report
                             [:gravity-candidate-verification :status]))
                  (= :gravity-source-pure
                     (:checked-core-ingress-mode expected)))
             "C11-MODULE" (:source-path context) {}
             :gravity-source-checked-core-semantic-authority)

            :effectful-reference
            (p15-s23-c11-mir-require!
             (and (= :passed (:status report))
                  (= :effectful-reference (:mode report))
                  (= :effectful-reference
                     (get-in checked-core [:source-core-input :mode]))
                  (= :effectful-reference
                     (:checked-core-ingress-mode expected)))
             "C11-MODULE" (:source-path context) {}
             :explicit-effectful-reference-checked-core-authority))
          expected)))))
