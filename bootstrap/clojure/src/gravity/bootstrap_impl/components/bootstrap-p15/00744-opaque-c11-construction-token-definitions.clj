(require '[gravity.c11-mir.source-interface :as c11-source-interface])

(let [opaque-c11-construction-token (nth __gravity_bootstrap_lexical_values_119416 0)
      opaque-c11-upstream-diagnostic-owner (nth __gravity_bootstrap_lexical_values_119416 1)
      classify-checked-core-ingress-pair __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
      invoke-checked-core-verifier! __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
      authenticate-checked-core-ingress! __gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
      invoke-pinned-c11-builder! __gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
      construct-authenticated-c11-mir! __gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir
      construct-current-scalar-mir! __gravity_bootstrap_lexical_119416_construct-current-scalar-mir
      live-application-handoffs (java.util.IdentityHashMap.)]
  (letfn [(artifact* [checked-core context version]
            (try
              (let [ingress (authenticate-checked-core-ingress!
                             checked-core context :contained-checked-core-ingress version)]
                (p15-s23-c11-mir-v2-source-family! (:source-path context) checked-core version)
                (binding [*p15-s23-c11-mir-diagnostic-context*
                          (p15-s23-c11-mir-diagnostic-context checked-core context {})]
                  (construct-authenticated-c11-mir!
                   checked-core context ingress opaque-c11-construction-token version)))
              (catch InterruptedException error (.interrupt (Thread/currentThread)) (throw error))
              (catch StackOverflowError error
                (p15-s23-c11-mir-contain-exception! (p15-s23-c11-ingress-source-path context)
                  :contained-public-c11-constructor-host-stack error))
              (catch AssertionError error
                (p15-s23-c11-mir-contain-exception! (p15-s23-c11-ingress-source-path context)
                  :contained-public-c11-constructor-assertion error))
              (catch LinkageError error
                (p15-s23-c11-mir-contain-exception! (p15-s23-c11-ingress-source-path context)
                  :contained-public-c11-constructor-linkage error))
              (catch clojure.lang.ExceptionInfo error
                (p15-s23-c11-mir-contain-exception! (p15-s23-c11-ingress-source-path context)
                  :contained-public-c11-constructor-diagnostic error))
              (catch Exception error
                (p15-s23-c11-mir-contain-exception! (p15-s23-c11-ingress-source-path context)
                  :contained-public-c11-constructor-host-failure error))))
          (report* [artifact checked-core context version]
            (let [source-path (p15-s23-c11-ingress-source-path context)
                  sorted-policy (p15-s23-c11-carrier-sorted-policy checked-core)]
              (try
                (p15-s23-c11-mir-require-trusted-carrier!
                 source-path :public-c11-mir-verifier-ingress artifact sorted-policy)
                (p15-s23-c11-mir-bounded-value!
                 source-path :public-c11-mir-verifier-ingress artifact
                 p15-s23-c11-mir-max-final-artifact-carrier-nodes p15-s23-c11-mir-max-carrier-depth)
                (let [ingress (authenticate-checked-core-ingress!
                               checked-core context :contained-public-checked-core-verifier version)]
                  (p15-s23-c11-mir-v2-source-family! source-path checked-core version)
                  (binding [*p15-s23-c11-mir-diagnostic-context*
                            (p15-s23-c11-mir-diagnostic-context checked-core context artifact)]
                    (p15-s23-c11-mir-validate-final-artifact! artifact checked-core context version)
                    (let [binding (p15-s23-c11-mir-source-binding!
                                         (:source-path context) (:requested-target context) version
                                         (when (contains? #{2 3} version) :development-current))
                          _ (p15-s23-c11-mir-require!
                             (and (= (p15-s23-c11-mir-source-rule-record binding) (:source-rule artifact))
                                  (= (:source-path binding) (get-in artifact [:provenance :actual-paths :c11-source])))
                             "C11-VERIFY" (:source-path context) artifact :fresh-pinned-c11-source-rule-and-path)
                          replay (invoke-pinned-c11-builder!
                                  checked-core context binding :verification-replay opaque-c11-construction-token)
                          replay-verifier (p15-s23-c11-mir-validate-constructed! (:source-path context) checked-core replay)
                          verifier-record (p15-s23-c11-independent-verifier-record replay-verifier version (:source-policy ingress))
                          replay-module (p15-s23-c11-mir-finalize
                                         replay checked-core verifier-record (p15-s23-c11-mir-scope-record version) version (:source-policy ingress))
                          expected (p15-s23-c11-mir-final-artifact-base
                                    checked-core context ingress binding replay replay-verifier version)]
                      (p15-s23-c11-mir-require!
                       (= (p15-s23-c11-mir-path-neutral-value replay-module)
                          (p15-s23-c11-mir-path-neutral-value (:mir-module artifact)))
                       "C11-VERIFY" (:source-path context) artifact :independent-gravity-replay-semantic-parity)
                      (p15-s23-c11-mir-require-strict-structure!
                       (:source-path context) (p15-s23-c11-mir-path-neutral-value replay-module)
                       (p15-s23-c11-mir-path-neutral-value (:mir-module artifact))
                       :type-sensitive-independent-gravity-replay-parity)
                      (p15-s23-c11-mir-require-strict-structure!
                       (:source-path context) expected artifact :type-sensitive-contextual-c11-artifact-parity)
                      (p15-s23-c11-mir-versioned-record
                       {:artifact :gravity/c11-mir-verification-report :status :passed
                        :mir-id (:mir-id artifact) :checked-core-artifact-id (:artifact-id checked-core)
                        :checked-core-ingress ingress
                        :checked-core-request-binding-provenance
                        (p15-s23-c11-mir-expected-request-binding-provenance checked-core context)
                        :source-rule (p15-s23-c11-mir-source-rule-record binding)
                        :semantic-replay-parity :passed :invocation-audit :not-available
                        :execution-count :not-claimed :live-invocation-claim? false
                        :execution-tcb :clojure-stage0-rule-runner :independent-verifier replay-verifier
                        :b1-preflight (:b1-preflight artifact)
                        :actual-path-context {:source (:source-path context) :c11-source (:source-path binding)}} version))))
                (catch InterruptedException error (.interrupt (Thread/currentThread)) (throw error))
                (catch StackOverflowError error
                  (p15-s23-c11-mir-contain-exception! source-path :contained-public-c11-verifier-host-stack error))
                (catch AssertionError error
                  (p15-s23-c11-mir-contain-exception! source-path :contained-public-c11-verifier-assertion error))
                (catch LinkageError error
                  (p15-s23-c11-mir-contain-exception! source-path :contained-public-c11-verifier-linkage error))
                (catch clojure.lang.ExceptionInfo error
                  (p15-s23-c11-mir-contain-exception! source-path :contained-public-c11-verifier-diagnostic error))
                (catch Exception error
                  (p15-s23-c11-mir-contain-exception! source-path :contained-public-c11-verifier-host-failure error)))))]
    (defn p15-s23-current-source-to-verified-mir
      "Production source-only composition under the original C11 authority.
       No accepted artifact, report, binding, plan or token is a caller input."
      [source-path policy]
      (binding [*print-length* nil *print-level* nil *print-meta* false
                *print-dup* false *print-readably* true *print-namespace-maps* false]
      (p15-s23-application-request! source-path policy)
      (let [source (p15-s23-application-source-snapshot! source-path)
            path (:path source)
            fresh (p15-s23-c6c10-current-scalar-construction path (:text source) policy)
            checked-core (:artifact fresh)
            context (:context fresh)
            mir (binding [*p15-s23-c11-mir-diagnostic-context*
                          (p15-s23-c11-mir-diagnostic-context checked-core context {})]
                  (construct-current-scalar-mir!
                   checked-core context opaque-c11-construction-token))
            result
            {:artifact :gravity/compiler-application :schema-version 1 :status :accepted
             :source (dissoc source :text)
             :source-target (:target policy)
             :checked-core checked-core :mir-module (:mir-module mir)
             :core-verification (:gravity-verification fresh)
             :mir-verification (:verification mir)
             :source-policy-receipt (:source-policy-receipt fresh)
             :compiler-bindings {:c6-c10 (:source-binding fresh) :c11 (:source-binding mir)}
             :compiler-host {:implementation :clojure-stage0-seed :target :jvm :self-hosted? false}
             :nonclaims [:target-emission
                         :independent-acceptance :self-hosting :seed-retirement]}]
        (p15-s23-c11-mir-require-strict-structure!
         path source (p15-s23-application-source-snapshot! path) :application-stable-source)
        (p15-s23-c6c10-recheck-application-owners!
         path (get-in fresh [:source-policy-receipt :owner-sources]))
        (p15-s23-application-recheck-c11! path (:source-binding mir))
        (assoc result :artifact-id
               (p15-s23-c6c10-canonical-digest
                path {:domain :gravity/compiler-application-v1 :artifact result})))))

    (defn p15-s23-authenticate-current-source-mir-handoff!
      "Authenticate an invocation-local source construction handoff.
       Only the original live lexical authenticator is invoked; arbitrary
       callback values never substitute for construction or verification."
      [authenticate! candidate]
      (p15-s23-c11-mir-require-trusted-carrier!
       "<compiler-application>" :application-backend-candidate candidate :reject)
      (let [path (if (string? (get-in candidate [:source :path]))
                   (get-in candidate [:source :path]) "<compiler-application>")
            authenticator (locking live-application-handoffs
                            (.get live-application-handoffs authenticate!))]
        (p15-s23-c11-mir-require!
         (some? authenticator) "C11-VERIFY" path {}
         :live-current-source-construction-handoff)
        (authenticator candidate)))

    ;; Capture the original constructor value in the same lexical authority.
    ;; Replacing the public source API Var must not register fabricated products.
    (let [construct-source p15-s23-current-source-to-verified-mir]
    (defn p15-s23-call-with-current-source-verified-mir
      "Compose a backend with genuine freshly constructed source products.
       The consumer receives the product and an invocation-local handoff."
      [source-path policy consumer]
      (let [product (construct-source source-path policy)
            path (get-in product [:source :path])
            authenticate!
            (fn [candidate]
              (p15-s23-c11-mir-require-trusted-carrier!
               path :application-backend-candidate candidate :reject)
              (p15-s23-c11-mir-require-strict-structure!
               path product candidate :application-original-construction-product)
              (p15-s23-c11-mir-require-strict-structure!
               path (:source product)
               (dissoc (p15-s23-application-source-snapshot! path) :text)
               :application-stable-source)
              (p15-s23-c6c10-recheck-application-owners!
               path (get-in product [:source-policy-receipt :owner-sources]))
              (p15-s23-application-recheck-c11!
               path (get-in product [:compiler-bindings :c11]))
              candidate)]
        (locking live-application-handoffs
          (.put live-application-handoffs authenticate! authenticate!))
        (try
          (consumer product authenticate!)
          (finally
            (locking live-application-handoffs
              (.remove live-application-handoffs authenticate!)))))))

    (defn p15-s23-stage2-c11-mir-artifact
      "Existing legacy arity remains v1. The explicit header selects only a built-in contract."
      ([checked-core context] (artifact* checked-core context 1))
      ([checked-core context interface-header]
       (artifact* checked-core context (c11-source-interface/public-interface-version! interface-header))))

    (defn p15-s23-stage2-c11-mir-verification-report
      ([artifact checked-core context]
       (c11-source-interface/reject-recognized-v2-on-legacy! artifact)
       (report* artifact checked-core context 1))
      ([artifact checked-core context interface-header]
       (let [version (c11-source-interface/public-interface-version! interface-header)]
         (c11-source-interface/require-matching-artifact-version! artifact version)
         (report* artifact checked-core context version))))

    (defn p15-s23-stage2-c11-mir-verify!
      ([artifact checked-core context]
       (let [report (p15-s23-stage2-c11-mir-verification-report artifact checked-core context)]
         (p15-s23-c11-mir-require! (= :passed (:status report)) "C11-VERIFY"
           (p15-s23-c11-ingress-source-path context) artifact :c11-verification-report-status)
         :passed))
      ([artifact checked-core context interface-header]
       (let [report (p15-s23-stage2-c11-mir-verification-report artifact checked-core context interface-header)]
         (p15-s23-c11-mir-require! (= :passed (:status report)) "C11-VERIFY"
           (p15-s23-c11-ingress-source-path context) artifact :c11-verification-report-status)
         :passed)))

    (defn p15-s23-stage2-c11-mir-authentic?
      ([_artifact] false)
      ([artifact checked-core context]
       (try (= :passed (p15-s23-stage2-c11-mir-verify! artifact checked-core context))
            (catch InterruptedException error (.interrupt (Thread/currentThread)) (throw error))
            (catch StackOverflowError _ false) (catch AssertionError _ false)
            (catch LinkageError _ false) (catch Exception _ false)))
      ([artifact checked-core context interface-header]
       (try (= :passed (p15-s23-stage2-c11-mir-verify! artifact checked-core context interface-header))
            (catch InterruptedException error (.interrupt (Thread/currentThread)) (throw error))
            (catch StackOverflowError _ false) (catch AssertionError _ false)
            (catch LinkageError _ false) (catch Exception _ false))))))
