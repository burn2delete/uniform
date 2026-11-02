(ns gravity.standard-library
  "Native call metadata and private development orchestration for the source library kernel.")

(def ^:private namespace-contract
  {:namespace 'gravity.standard-library
   :contract-boundary :native-library-call-metadata-and-experimental-source-kernel
   :governing-contracts '[D1 D2 D3 D6 D8 D9 L13 STD1 STD2 STD3 STD5 STD6 STD8 STD20 BOOT8 TEST2]
   :public-api {'record-call! {:arglists '([record-checker-var dispatch-arg-value-var checker ctx node operator args effects capabilities return-type spec]) :admission :none}
                'module-manifest {:arglists '([invoke]) :admission :none}
                'execute-request {:arglists '([invoke request]) :admission :none}
                'verify-result {:arglists '([invoke original-request candidate]) :admission :none}}
   :artifact-inputs [:native-checker-context :native-call-node :operator :arguments :effects :capabilities
                     :return-type :call-spec :record-and-argument-vars :source-invocation
                     :original-library-request :candidate-result]
   :artifact-outputs [:legacy-library-call-records :experimental-source-module-manifest
                      :source-library-result :source-library-rejection :source-replay-verification]
   :diagnostic-ownership {:new-rules [] :source-owner 'gravity.stdlib.self-hosting-core
                          :native-operation-failures :propagated :source-diagnostics :unchanged}
   :dependency-direction {:requires ['clojure.core] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :preserves [:source-spans :node-identity :profile :target :effects :capabilities :return-type
               :request-identity :origin-chain :actual-source-path :source-verifier-diagnostics]
   :invalidates [] :regenerates [:native-call-records :source-results :source-replay-verification]
   :stability :internal :source-kernel {:stability :experimental :profile :meta :target :jvm
                                       :opt-in :request-required :tested-operations
                                       [:vector-assoc :vector-nth :checked-add-i64]}
   :legacy-compile-proof-safety-labels-authoritative? false
   :invocation-provider :trusted-seed-operation :upstream-authentication false
   :unsupported [:canonical-module-resolution :complete-export-surface :other-profiles
                 :provider-io :runtime-services :unsafe-wrapper-audits :proof-certificates
                 :authenticated-sh13-sh14-sh19 :full-std-conformance :full-sh22-conformance
                 :migration :release :self-hosting :seed-retirement]
   :test-owner 'gravity.standard-library.pipeline-test :bootstrap-hosted? true
   :clojure-seed-boundary? true :self-hosted? false})

(defn record-call!
  [record-checker-var dispatch-arg-value-var checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:standard-library-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :standard-library-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)}]
      (case kind
        :namespace-contract
        ((deref record-checker-var) checker :standard-library-namespace-contracts
                         (merge record
                                {:namespace ((deref dispatch-arg-value-var) args 0)
                                 :supported-profiles (or ((deref dispatch-arg-value-var) args 1) #{})
                                 :stability (or ((deref dispatch-arg-value-var) args 2) :experimental)
                                 :allocation (or ((deref dispatch-arg-value-var) args 3) :declared)
                                 :since (or ((deref dispatch-arg-value-var) args 4) "stage0")
                                 :contract-source :namespace-metadata}))

        :api-contract
        ((deref record-checker-var) checker :standard-library-api-contracts
                         (merge record
                                {:symbol ((deref dispatch-arg-value-var) args 0)
                                 :classification (or ((deref dispatch-arg-value-var) args 1) :pure)
                                 :supported-profiles (or ((deref dispatch-arg-value-var) args 2) #{})
                                 :declared-effects (or ((deref dispatch-arg-value-var) args 3) #{})
                                 :declared-capabilities (or ((deref dispatch-arg-value-var) args 4) #{})
                                 :allocation (or ((deref dispatch-arg-value-var) args 5) :none)
                                 :panic (or ((deref dispatch-arg-value-var) args 6) :none)
                                 :blocking (or ((deref dispatch-arg-value-var) args 7) :nonblocking)
                                 :nondeterminism (or ((deref dispatch-arg-value-var) args 8) :deterministic)
                                 :stability :stable}))

        :profile-availability
        ((deref record-checker-var) checker :standard-library-profile-availability-reports
                         (merge record
                                {:namespace ((deref dispatch-arg-value-var) args 0)
                                 :available-profiles (or ((deref dispatch-arg-value-var) args 1) #{})
                                 :unavailable-profiles (or ((deref dispatch-arg-value-var) args 2) #{})
                                 :generated-from :actual-stage0-contracts}))

        :example
        ((deref record-checker-var) checker :standard-library-documentation-examples
                         (merge record
                                {:namespace ((deref dispatch-arg-value-var) args 0)
                                 :profile (or ((deref dispatch-arg-value-var) args 1) (:profile @ctx))
                                 :example-id (or ((deref dispatch-arg-value-var) args 2) :stage0-example)
                                 :compile-status :passed
                                 :negative? (true? ((deref dispatch-arg-value-var) args 3))}))

        :unsafe-wrapper
        ((deref record-checker-var) checker :standard-library-unsafe-wrapper-audits
                         (merge record
                                {:wrapper ((deref dispatch-arg-value-var) args 0)
                                 :unsafe-namespace (or ((deref dispatch-arg-value-var) args 1)
                                                       'gravity.memory.unsafe)
                                 :invariant (or ((deref dispatch-arg-value-var) args 2)
                                                :bounds-and-alignment-checked)
                                 :proof [:stage0-test :runtime-check]
                                 :profile-gates (or ((deref dispatch-arg-value-var) args 3)
                                                    #{:native})
                                 :audit-status :proved
                                 :safe-surface true}))

        :compatibility
        ((deref record-checker-var) checker :standard-library-compatibility-records
                         (merge record
                                {:namespace ((deref dispatch-arg-value-var) args 0)
                                 :event (or ((deref dispatch-arg-value-var) args 1) :minor-addition)
                                 :from-version (or ((deref dispatch-arg-value-var) args 2) "0.1.0")
                                 :to-version (or ((deref dispatch-arg-value-var) args 3) "0.1.1")
                                 :migration-recorded? true
                                 :profile-support-change (or ((deref dispatch-arg-value-var) args 4) :none)}))

        :numeric-mode
        ((deref record-checker-var) checker :standard-library-numeric-mode-records
                         (merge record
                                {:symbol ((deref dispatch-arg-value-var) args 0)
                                 :width (or ((deref dispatch-arg-value-var) args 1) :i64)
                                 :overflow (or ((deref dispatch-arg-value-var) args 2) :checked)
                                 :rounding (or ((deref dispatch-arg-value-var) args 3) :exact)
                                 :floating-mode (or ((deref dispatch-arg-value-var) args 4) :deterministic)
                                 :rewrite-proof :required-for-optimization}))

        :resource-api
        ((deref record-checker-var) checker :standard-library-resource-records
                         (merge record
                                {:symbol ((deref dispatch-arg-value-var) args 0)
                                 :resource (or ((deref dispatch-arg-value-var) args 1) :file)
                                 :lifetime :linear
                                 :release :required
                                 :failure-mode :result
                                 :blocking (or ((deref dispatch-arg-value-var) args 2) :may-block)}))

        nil)
      record)))

(defn module-manifest [invoke]
  (invoke 'sh22-module-manifest []))

(defn verify-result [invoke original-request candidate]
  (invoke 'sh22-verify-result [original-request candidate]))

(defn execute-request [invoke request]
  (let [result (invoke 'sh22-handle-request [request])
        verification (verify-result invoke request result)]
    (if (= :passed (:status verification))
      {:result result :verification verification}
      verification)))

(doseq [operation [#'record-call! #'module-manifest #'execute-request #'verify-result]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
