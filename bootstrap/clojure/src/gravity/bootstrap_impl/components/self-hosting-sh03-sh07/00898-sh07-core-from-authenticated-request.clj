

(defn sh07-core-from-authenticated-request
  ([resolution-artifact authenticated-request]
   (sh07-core-from-authenticated-request resolution-artifact authenticated-request nil))
  ([resolution-artifact authenticated-request invocation]
  (sh07-core-with-fresh-construction resolution-artifact
    #(sh07-core-build-verified-artifact resolution-artifact authenticated-request %) invocation)))

(defn-
 sh07-core-artifact-base
 [resolution-artifact authenticated-request run captured source-path]
 (gravity.checked-core/artifact-base
  resolution-artifact
  authenticated-request
  run
  captured
  source-path
  {:adapter-contract sh07-core-adapter-contract,
   :governing-document sh07-core-governing-document}))

(defn-
 sh07-core-build-verified-artifact
 [resolution-artifact authenticated-request construction]
 (let
  [input-key
   (sh07-core-current-source-key)
   captured
   @sh07-core-cached-binding
   construction
   (assoc construction :execution-binding captured)
   _
   (when-not
    (= (second (:source input-key)) (:source-content-hash captured))
    (throw
     (ex-info
      "SH-07 artifact started with a stale revision"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :reason :checked-core-operation-revision-drift})))
   artifact
   (binding
    [*sh07-core-execution-binding* captured]
    (let
     [run
      (sh07-core-run-with-construction
       resolution-artifact
       authenticated-request
       construction)
      source-path
      (sh07-core-source-path-from-resolution resolution-artifact)
      artifact-base
      (sh07-core-artifact-base
       resolution-artifact
       authenticated-request
       run
       captured
       source-path)
      upstream-verification
      (do
       (sh07-core-construction-check! construction)
       (:report construction))
      proof
      (sh07-core-proof-from-checks
       (sh07-core-verification-checks
        artifact-base
        artifact-base
        upstream-verification))]
     (assoc artifact-base :capability-based-proof proof)))]
  (when-not
   (= input-key (sh07-core-current-source-key))
   (throw
    (ex-info
     "SH-07 inputs changed during artifact construction"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :reason :checked-core-operation-revision-drift})))
  artifact))

(defn sh07-core-from-resolution-artifact
  ([resolution-artifact] (sh07-core-from-resolution-artifact resolution-artifact nil))
  ([resolution-artifact invocation]
  (sh07-core-with-fresh-construction resolution-artifact
    #(sh07-core-build-verified-artifact resolution-artifact
       (or (:request %) (sh07-core-request-from-verified-upstream resolution-artifact (:report %))) %) invocation)))

(def ^:private sh07-core-verification-session-schema-version 2)
(def ^:private sh07-core-verification-session-kind
  :gravity/sh07-core-verification-session)

(def ^:private sh07-core-verification-session-upstream-identity-keys
  [:source-path :source-byte-count :source-content-hash
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])

(defn- sh07-core-verification-session-expected-integrity
  [expected]
  (reader-canonical-hash
   {:domain :gravity/sh07-verification-session-expected-v1
    :expected (sh07-core-exact-comparison-value expected)}))

(defn- sh07-core-verification-session-upstream-snapshot
  [resolution-artifact]
  ;; The artifact digest covers every retained SH-06 product; the explicit
  ;; identity projection keeps the source and plan/function pins visible in
  ;; the session binding as well. Replay compares this immutable snapshot and
  ;; never trusts an artifact-id by itself.
  (let [plan-binding
        (get-in resolution-artifact
                [:gravity-resolution-boundary :plan-binding])]
    {:artifact-id (:artifact-id resolution-artifact)
     :source-path (sh07-core-source-path-from-resolution resolution-artifact)
     :source-and-plan-identities
     (select-keys plan-binding
                  sh07-core-verification-session-upstream-identity-keys)
     :plan-binding-integrity
     (reader-canonical-hash
      {:domain :gravity/sh07-verification-session-plan-binding-v1
       :plan-binding (sh07-core-exact-comparison-value plan-binding)})
     :resolution-artifact-integrity
     (reader-canonical-hash
      {:domain :gravity/sh07-verification-session-upstream-v1
       :resolution-artifact
       (sh07-core-exact-comparison-value resolution-artifact)})}))

(defn- sh07-core-verification-session-binding
  ([upstream-snapshot expected upstream-verification]
   (sh07-core-verification-session-binding
    upstream-snapshot expected upstream-verification
    (sh07-core-verification-session-expected-integrity expected)))
  ([upstream-snapshot expected upstream-verification expected-integrity]
   (reader-canonical-hash
    {:domain :gravity/sh07-verification-session-v1
     :resolution-artifact-id (:artifact-id upstream-snapshot)
     :expected-artifact-id (:artifact-id expected)
     :expected-integrity expected-integrity
     :source-path (:source-path upstream-snapshot)
     :source-and-plan-identities
     (:source-and-plan-identities upstream-snapshot)
     :plan-binding-integrity (:plan-binding-integrity upstream-snapshot)
     :resolution-artifact-integrity
     (:resolution-artifact-integrity upstream-snapshot)
     :upstream-status (:status upstream-verification)})))
