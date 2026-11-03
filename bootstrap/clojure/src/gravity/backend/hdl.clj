(ns gravity.backend.hdl
  "Native B9 diagnostic context, catalog and fixture data; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {testbench
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  timing-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  hdl-module
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  validate-source-overrides!
  {:arglists ([source-path overrides operations]),
   :admission :none,
   :required-operations [:override-diagnostics :failure-operation]},
  source-overrides {:arglists ([module]), :admission :none},
  fail!
  {:arglists ([id source-path subject extra operations]),
   :admission :none,
   :required-operations [:fail! :source-span :signal-id :missing-fact],
   :callback-return :propagated},
  testbench-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  hdl-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  signal-id {:arglists ([id]), :admission :none},
  timing-constraints
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  missing-fact {:arglists ([id]), :admission :none},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :signal-id :missing-fact]}},
 :supported
 [:existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition
  :fixed-native-reference-text
  :native-failure-payload-assembly
  :native-explicit-override-refusal],
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
  :fixture-match-as-capability-or-proof-authority
  :fixture-text-as-lowering-or-execution
  :refusal-callback-as-admission-or-proof
  :canonical-diagnostic-publication],
 :artifact-inputs
 [:native-module-metadata
  :native-b9-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text
  :fixed-native-fixture-definitions
  :native-diagnostic-subject
  :native-diagnostic-extra
  :native-refusal-operations],
 :namespace gravity.backend.hdl,
 :diagnostic-ownership
 {:stage
  [:native-override-precedence
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:native-artifact-validation-decisions
   :diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-fact
  :native-signal-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
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

(def hdl-module
  (str
   "module gravity_stage0_hdl(\n"
   "  input logic clk,\n"
   "  input logic rst,\n"
   "  input logic input_valid,\n"
   "  input logic [31:0] input_data,\n"
   "  output logic output_ready,\n"
   "  output logic done\n"
   ");\n"
   "  typedef enum logic [1:0] {IDLE, ACCUMULATE, DONE} state_t;\n"
   "  state_t state;\n"
   "  logic [31:0] accumulator;\n"
   "  always_ff @(posedge clk) begin\n"
   "    if (rst) begin\n"
   "      state <= IDLE;\n"
   "      accumulator <= 32'd0;\n"
   "      done <= 1'b0;\n"
   "    end else begin\n"
   "      case (state)\n"
   "        IDLE: if (input_valid) begin accumulator <= input_data; state <= ACCUMULATE; end\n"
   "        ACCUMULATE: begin accumulator <= accumulator + 32'd1; state <= DONE; end\n"
   "        DONE: begin done <= 1'b1; state <= DONE; end\n"
   "      endcase\n"
   "    end\n"
   "  end\n"
   "  assign output_ready = (state == DONE);\n"
   "endmodule\n"))

(def testbench
  (str
   "module gravity_stage0_hdl_tb;\n"
   "  logic clk = 1'b0;\n"
   "  logic rst = 1'b1;\n"
   "  logic input_valid = 1'b0;\n"
   "  logic [31:0] input_data = 32'd7;\n"
   "  logic output_ready;\n"
   "  logic done;\n"
   "  gravity_stage0_hdl dut(clk, rst, input_valid, input_data, output_ready, done);\n"
   "  always #5 clk = ~clk;\n"
   "  initial begin\n"
   "    repeat (2) @(posedge clk);\n"
   "    rst = 1'b0;\n"
   "    input_valid = 1'b1;\n"
   "    repeat (4) @(posedge clk);\n"
   "    $finish;\n"
   "  end\n"
   "endmodule\n"))

(def timing-constraints
  (str
   "create_clock -name clk -period 10 [get_ports clk]\n"
   "set_input_delay 1 -clock clk [get_ports input_data]\n"
   "set_output_delay 1 -clock clk [get_ports done]\n"))

(defn fail!
  [id source-path subject extra operations]
  ((:fail! operations) id
         "B9 HDL backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  ((:source-span operations) source-path 0))
                 :diagnostic-family :b9-hdl-backend-document
                 :stage :b9-hdl-backend-document-coverage
                 :backend :gravity.backend/hdl
                 :profile :hardware
                 :target :systemverilog
                 :hardware-module :gravity_stage0_hdl
                 :signal-or-state-id ((:signal-id operations) id)
                 :clock-domain :clk
                 :reset-domain :rst
                 :missing-proof-or-constraint
                 ((:missing-fact operations) id)
                 :fallback-status :rejected
                 :remediation "B9 requires verified hardware IR, fixed-width layout, explicit clock/reset and CDC records, finite-state control, hardware interface schemas, timing constraints, testbench/simulation trace evidence, source maps, and HDL artifact provenance."}
                extra)))

(defn validate-source-overrides!
  [source-path overrides operations]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get ((:override-diagnostics operations)) fail-kind)]
      (((:failure-operation operations))
       id source-path
       {:artifact-id (str "b9-document-" (name fail-kind))
        :signal-or-state-id (keyword (str "b9-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))

(doseq [operation [#'source-overrides #'missing-fact #'signal-id #'diagnostic-stream #'hdl-structurally-valid? #'testbench-structurally-valid? #'timing-structurally-valid? #'hdl-module #'testbench #'timing-constraints #'fail! #'validate-source-overrides!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
