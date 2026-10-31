(ns gravity.diagnostics
  "Stage 0 base diagnostic carrier.

  This namespace owns construction and throwing of the bootstrap
  ExceptionInfo carrier, native seed C15 record/identity construction and
  private source-record binding through explicit source and digest operations.
  Rule catalogs, C15 validation, redaction,
  rendering, and stage-specific diagnostic policy remain with their owning
  compiler stages.")

(def ^:private namespace-contract
  {:namespace 'gravity.diagnostics
   :contract-boundary :stage0-diagnostic-carriers-and-record-data
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 BOOT8 TEST2]
   :public-api
   {'diagnostic {:arglists '([id message data])
                 :returns :clojure.lang/exception-info}
    'fail! {:arglists '([id message data])
            :throws :clojure.lang/exception-info}
    'stable-id {:arglists '([hash-text diagnostic]) :admission :none}
    'resolve-request-id
    {:arglists '([canonical-digest missing-request! source-path diagnostic]) :admission :none}
    'verify-private-record
    {:arglists '([invoke canonical-digest source-path original-fields candidate request]) :admission :none}
    'bind-private-record
    {:arglists '([invoke canonical-digest source-path original-fields]) :admission :none}
    'diagnostic-record
    {:arglists '([source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation & {:keys [related origin-chain redactions lifecycle generated? profile target syntax-id]}])
     :admission :none}}
   :artifact-inputs [:rule-id :human-message :structured-data :explicit-span-and-identity-operations
                     :native-seed-diagnostic-fields :source-invocation :canonical-request-digest
                     :original-eleven-c15-fields :private-candidate :complete-digest-request]
   :artifact-outputs [:stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id
                      :resolved-request-data :private-c15-binding :source-owned-rejection]
   :diagnostic-ownership
   {:owns [:stage0-carrier-construction :stage0-carrier-throw :native-seed-record-construction
           :legacy-stable-id-preimage :request-binding-orchestration :fresh-source-verification-orchestration]
    :does-not-own
    [:rule-catalogs :c15-schema :identity-authentication :stream-ordering
     :redaction :rendering :stage-specific-policy]}
   :dependency-direction
   {:requires ['clojure.core]
    :consumed-by ['gravity.c15-diagnostics.records 'gravity.bootstrap]
    :forbids ['gravity.bootstrap 'gravity.c15-diagnostics 'gravity.cli 'gravity.release]}
   :unsupported [:canonical-c15-conformance :untrusted-native-ingress :public-projection
                 :redaction-policy :canonical-map-encoding :migration :release :seed-retirement]
   :record-context {:profile :caller-supplied-or-nil :target :caller-supplied-or-nil
                    :syntax-id :caller-supplied-or-nil}
   :supplied-redactions-applied? false
   :private-binding {:schema-owner 'gravity.compiler.c15-compiler-diagnostics
                     :digest-input :complete-request :digest-provider :trusted-native-operation
                     :source-provider :trusted-invocation-operation :public-safe? false
                     :upstream-authentication false :source-verifier-nonclaims :preserved}
   :test-owner
   'gravity.diagnostics-test/base-exception-info-carrier-is-extracted-with-bootstrap-parity
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn diagnostic
  [id message data]
  (ex-info message
           (merge {:id id
                   :message message
                   :bootstrap-stage :stage0}
                  data)))

(defn fail!
  [id message data]
  (throw (diagnostic id message data)))

(defn stable-id [hash-text diagnostic]
  (str "diag-" (hash-text
                (pr-str {:rule (:rule diagnostic) :stage (:stage diagnostic)
                         :primary-artifact (get-in diagnostic [:primary :artifact])
                         :facts (:facts diagnostic)}))))

(defn resolve-request-id
  "Resolves prevalidated request data; the caller retains ingress admission."
  [canonical-digest missing-request! source-path diagnostic]
  (let [request (:diagnostic-id-request diagnostic)]
    (when-not (map? request)
      (missing-request! source-path diagnostic))
    (-> diagnostic
        (dissoc :diagnostic-id-request)
        (assoc :diagnostic-id (canonical-digest source-path request)))))

(defn verify-private-record
  "Rebuilds from original fields in C15 with a fresh native full-request digest.
  Operations are trusted seed inputs; preserve the source verifier's nonclaims."
  [invoke canonical-digest source-path original-fields candidate request]
  (invoke 'verify-c15-resolved-diagnostic
          [original-fields candidate request (canonical-digest source-path request)]))

(defn bind-private-record
  "Constructs, binds and freshly verifies private C15 data. No public projection."
  [invoke canonical-digest source-path original-fields]
  (let [unresolved (invoke 'build-c15-diagnostic original-fields)]
    (if (= :rejected (:status unresolved))
      unresolved
      (let [request (:diagnostic-id-request unresolved)
            candidate (assoc unresolved :diagnostic-id (canonical-digest source-path request))
            verification (verify-private-record invoke canonical-digest source-path
                                                original-fields candidate request)]
        (if (= :passed (:status verification))
          {:status :passed :diagnostic candidate :verification verification}
          verification)))))

(defn diagnostic-record
  [source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated? profile target syntax-id]}]
  (let [diagnostic {:artifact :gravity/diagnostic :rule rule :severity severity :stage stage
                    :message-key message-key
                    :primary {:span (source-span source-path form-index)
                              :syntax-id syntax-id :artifact primary-artifact}
                    :related (vec related) :origin-chain (vec origin-chain) :profile profile :target target
                    :involved-artifacts [primary-artifact] :facts facts :remediation (vec remediation)
                    :redactions (vec redactions) :lifecycle (or lifecycle :active) :generated? (true? generated?)}]
    (assoc diagnostic :diagnostic-id (stable-id diagnostic)
           :ordering-key [rule stage primary-artifact form-index])))

(doseq [operation [#'diagnostic #'fail! #'stable-id #'diagnostic-record
                  #'resolve-request-id #'verify-private-record #'bind-private-record]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
