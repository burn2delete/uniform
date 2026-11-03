(ns gravity.backend.hdl
  "Native B9 diagnostic context, catalog and fixture data; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {source-overrides {:arglists ([module]), :admission :none},
  missing-fact {:arglists ([id]), :admission :none},
  signal-id {:arglists ([id]), :admission :none},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :signal-id :missing-fact]},
  hdl-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  testbench-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  timing-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text}},
 :supported
 [:existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition],
 :dependency-direction
 {:requires [clojure.core clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B9 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b9-semantics
  :gravity-b9-source-compilation
  :verified-hardware-or-domain-ingress
  :hdl-emission
  :hdl-verification
  :width-and-numeric-validation
  :clock-reset-and-cdc-validation
  :finite-control-validation
  :interface-and-timing-validation
  :simulation
  :synthesis
  :downstream-handoff
  :source-authentication
  :public-diagnostic-redaction
  :migration
  :release
  :self-hosting
  :seed-retirement
  :canonical-c15-conformance
  :catalog-as-observed-semantic-findings
  :native-operations-as-proof-authority
  :canonical-target-syntax-validation
  :fixture-text-as-verified-ir
  :fixture-match-as-capability-or-proof-authority],
 :artifact-inputs
 [:native-module-metadata
  :native-b9-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text],
 :namespace gravity.backend.hdl,
 :diagnostic-ownership
 {:stage
  [:native-override-precedence
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition],
  :existing-owner
  [:b9-refusal-decisions
   :failure-construction
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-fact
  :native-signal-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00250-b8-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00251-b9-document-hdl-module.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b9-document-context-catalog-and-fixture-data})

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

(defn diagnostic-stream
  [source-path input-id operations]
  {:artifact :gravity/b9-hdl-backend-diagnostic-stream
   :stage :b9-hdl-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b9-hdl-backend-document-coverage
            :backend :gravity.backend/hdl
            :message-key (keyword "backend-hdl" (str/lower-case id))
            :primary {:span ((:source-span operations) source-path index)
                      :syntax-id (str "b9-document-syntax-" index)
                      :artifact input-id}
            :profile :hardware
            :target :systemverilog
            :hardware-module :gravity_stage0_hdl
            :signal-or-state-id ((:signal-id operations) id)
            :clock-domain :clk
            :reset-domain :rst
            :missing-proof-or-constraint ((:missing-fact operations) id)
            :source-generated-origin-chain
            [:mir :c12-hardware-domain-ir :c14-target-lowering
             :b1-interface :b9-hdl-backend]
            :fallback-status :rejected
            :facts {:fixed-widths-required true
                    :implicit-runtime-rejected true
                    :unmediated-cdc-rejected true
                    :timing-constraints-required true}
            :remediation [{:kind :declare-hdl-target-provider-and-constraints}
                          {:kind :attach-fixed-width-clock-reset-cdc-records}
                          {:kind :emit-testbench-and-simulation-trace}]
            :redactions []
            :ordering-key [id :b9-hdl-backend-document-coverage
                           :systemverilog]})
         ((:diagnostic-ids operations))
         (range))
   :status :complete})

(defn hdl-structurally-valid?
  [text]
  (and (str/includes? text "module gravity_stage0_hdl")
       (str/includes? text "typedef enum logic [1:0]")
       (str/includes? text "always_ff @(posedge clk)")
       (str/includes? text "if (rst)")
       (str/includes? text "assign output_ready")))

(defn testbench-structurally-valid?
  [text]
  (and (str/includes? text "module gravity_stage0_hdl_tb")
       (str/includes? text "gravity_stage0_hdl dut")
       (str/includes? text "repeat (")
       (str/includes? text "$finish")))

(defn timing-structurally-valid?
  [text]
  (and (str/includes? text "create_clock")
       (str/includes? text "set_input_delay")
       (str/includes? text "set_output_delay")))

(doseq [operation [#'source-overrides #'missing-fact #'signal-id #'diagnostic-stream #'hdl-structurally-valid? #'testbench-structurally-valid? #'timing-structurally-valid?]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
