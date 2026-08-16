(ns gravity.diagnostics
  "Stage 0 base diagnostic carrier.

  This namespace owns only construction and throwing of the bootstrap
  ExceptionInfo carrier. Rule catalogs, structured C15 records, redaction,
  rendering, and stage-specific diagnostic policy remain with their owning
  compiler stages.")

(def ^:private namespace-contract
  {:namespace 'gravity.diagnostics
   :contract-boundary :stage0-base-exception-info-carrier
   :public-api
   {'diagnostic {:arglists '([id message data])
                 :returns :clojure.lang/exception-info}
    'fail! {:arglists '([id message data])
            :throws :clojure.lang/exception-info}}
   :artifact-inputs [:rule-id :human-message :structured-data]
   :artifact-outputs [:stage0-exception-info-carrier]
   :diagnostic-ownership
   {:owns [:stage0-carrier-construction :stage0-carrier-throw]
    :does-not-own
    [:rule-catalogs :c15-schema :diagnostic-identity :ordering
     :redaction :rendering :stage-specific-policy]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap]}
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
