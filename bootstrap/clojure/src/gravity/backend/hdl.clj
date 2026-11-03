(ns gravity.backend.hdl
  "Native B9 diagnostic lookup data; no HDL or input admission authority.")

(def ^:private namespace-contract
  {:namespace 'gravity.backend.hdl
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B9 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b9-diagnostic-policy-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-fact {:arglists '([id]) :admission :none}
                'signal-id {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b9-diagnostic-id]
   :artifact-outputs [:borrowed-source-overrides :native-missing-fact :native-signal-label]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults]
                          :existing-owner [:b9-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b9-semantics :gravity-b9-source-compilation
                 :verified-hardware-or-domain-ingress :hdl-emission :hdl-verification
                 :width-and-numeric-validation :clock-reset-and-cdc-validation
                 :finite-control-validation :interface-and-timing-validation :simulation :synthesis :downstream-handoff
                 :source-authentication :public-diagnostic-redaction :migration :release
                 :self-hosting :seed-retirement]})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b9-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn missing-fact
  [id]
  (case id
    "B9-TARGET" :hdl-target-provider-constraint-format
    "B9-WIDTH" :fixed-width-signedness-truncation-proof
    "B9-CLOCK" :clock-domain-record
    "B9-RESET" :reset-domain-record
    "B9-CDC" :cdc-synchronizer-or-waiver-proof
    "B9-RUNTIME" :hardware-runtime-construct-rejection-proof
    "B9-UNBOUNDED" :finite-state-static-control-proof
    "B9-INTERFACE" :port-bus-protocol-capability-schema
    "B9-TIMING" :timing-constraint-and-provider-report
    :hdl-artifact-manifest))

(defn signal-id
  [id]
  (case id
    "B9-CLOCK" :clk
    "B9-RESET" :rst
    "B9-CDC" :cdc_handshake
    "B9-INTERFACE" :ready_valid
    "B9-WIDTH" :accumulator
    "B9-TIMING" :clk_to_done
    "B9-RUNTIME" :runtime_call
    "B9-UNBOUNDED" :control_loop
    :gravity_stage0_hdl))

(doseq [operation [#'source-overrides #'missing-fact #'signal-id]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
