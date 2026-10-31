(ns gravity.diagnostics
  "Stage 0 base diagnostic carrier.

  This namespace owns construction and throwing of the bootstrap
  ExceptionInfo carrier and native seed C15 record/identity construction.
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
    'diagnostic-record
    {:arglists '([source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation & {:keys [related origin-chain redactions lifecycle generated?]}])
     :admission :none}}
   :artifact-inputs [:rule-id :human-message :structured-data :explicit-span-and-identity-operations
                     :native-seed-diagnostic-fields]
   :artifact-outputs [:stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id]
   :diagnostic-ownership
   {:owns [:stage0-carrier-construction :stage0-carrier-throw :native-seed-record-construction
           :legacy-stable-id-preimage]
    :does-not-own
    [:rule-catalogs :c15-schema :identity-authentication :stream-ordering
     :redaction :rendering :stage-specific-policy]}
   :dependency-direction
   {:requires ['clojure.core]
    :consumed-by ['gravity.c15-diagnostics.records]
    :forbids ['gravity.bootstrap 'gravity.c15-diagnostics 'gravity.cli 'gravity.release]}
   :unsupported [:canonical-c15-conformance :untrusted-native-ingress :public-projection
                 :redaction-policy :canonical-map-encoding :migration :release :seed-retirement]
   :legacy-record-context {:profile :hosted :target :jvm :syntax-id :form-index-derived}
   :supplied-redactions-applied? false
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

(defn diagnostic-record
  [source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated?]}]
  (let [diagnostic {:artifact :gravity/diagnostic :rule rule :severity severity :stage stage
                    :message-key message-key
                    :primary {:span (source-span source-path form-index)
                              :syntax-id (str "c15-syntax-" form-index) :artifact primary-artifact}
                    :related (vec related) :origin-chain (vec origin-chain) :profile :hosted :target :jvm
                    :involved-artifacts [primary-artifact] :facts facts :remediation (vec remediation)
                    :redactions (vec redactions) :lifecycle (or lifecycle :active) :generated? (true? generated?)}]
    (assoc diagnostic :diagnostic-id (stable-id diagnostic)
           :ordering-key [rule stage primary-artifact form-index])))

(doseq [operation [#'diagnostic #'fail! #'stable-id #'diagnostic-record]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
