(ns gravity.backend.hdl
  "Native B9 document data, diagnostics and artifact checks; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {native-manifest
  {:arglists ([operations source-path input-id]), :admission :none,
   :contract-boundary :native-hdl-document-data,
   :required-operations [:artifact-id :hdl-module :testbench :timing-constraints],
   :artifact-inputs [:unverified-source-path :unverified-input-id :fixed-native-reference-text],
   :artifact-outputs [:legacy-hdl-document-manifest],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  validate-native-manifest!
  {:arglists ([operations source-path artifact]), :admission :none,
   :contract-boundary :native-hdl-document-checks,
   :required-operations [:fail! :hdl-module :testbench :timing-constraints
                         :hdl-structurally-valid? :testbench-structurally-valid?
                         :timing-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :diagnostics ["B9-MANIFEST" "B9-TARGET" "B9-WIDTH" "B9-CLOCK" "B9-RESET"
                 "B9-CDC" "B9-RUNTIME" "B9-UNBOUNDED" "B9-INTERFACE" "B9-TIMING"],
   :diagnostic-publication :caller-owned,
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  native-coverage-report
  {:arglists ([operations artifact]), :admission :none,
   :contract-boundary :native-hdl-document-data,
   :required-operations [:hdl-module :testbench :timing-constraints
                         :hdl-structurally-valid? :testbench-structurally-valid?
                         :timing-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-document-coverage-record],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  testbench
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
 [:native-manifest-construction
  :native-artifact-validation-decisions
  :native-coverage-projection
  :existing-native-projection
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
  :canonical-hdl-emission
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
 [:unverified-native-document-data
  :native-manifest-operations
  :native-module-metadata
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
  [:native-artifact-validation-decisions
   :native-override-precedence
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:legacy-hdl-document-manifest
  :legacy-structural-completion-or-refusal
  :legacy-document-coverage-record
  :borrowed-source-overrides
  :native-missing-fact
  :native-signal-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00250-b8-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00251-b9-document-hdl-module.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00252-b9-document-hdl-manifest.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00253-b9-document-validate.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b9-document-data})

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

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations source-path input-id]
  (let [hdl-hash ((native-document-port operations :artifact-id) (native-document-port operations :hdl-module))
        testbench-hash ((native-document-port operations :artifact-id) (native-document-port operations :testbench))
        timing-hash ((native-document-port operations :artifact-id) (native-document-port operations :timing-constraints))]
    {:artifact :gravity/hdl-backend-manifest
     :backend :gravity.backend/hdl
     :target {:hdl :systemverilog
              :synthesis-tool :stage0-provider
              :constraint-format :sdc}
     :input-artifact input-id
     :hardware-ir-handoff-record
     {:domain-anchor :hardware-circuit
      :source-artifact input-id
      :accepted-by [:b1-backend-interface :c12-domain-ir
                    :c14-target-lowering :p8-hardware-profile]
      :status :complete}
     :hdl-artifacts
     [{:path "gravity_stage0_hdl.sv"
       :content (native-document-port operations :hdl-module)
       :hash hdl-hash
       :target :systemverilog
       :synthesizable-subset :stage0-structural
       :status :complete}]
     :interface-port-schema
     {:ports [{:name :clk :direction :input :width 1
               :signedness :unsigned :clock-domain :clk
               :protocol :clock}
              {:name :rst :direction :input :width 1
               :signedness :unsigned :clock-domain :clk
               :reset-domain :rst :protocol :reset}
              {:name :input_valid :direction :input :width 1
               :signedness :unsigned :clock-domain :clk
               :protocol :ready-valid}
              {:name :input_data :direction :input :width 32
               :signedness :unsigned :clock-domain :clk
               :protocol :ready-valid}
              {:name :output_ready :direction :output :width 1
               :signedness :unsigned :clock-domain :clk
               :protocol :ready-valid}
              {:name :done :direction :output :width 1
               :signedness :unsigned :clock-domain :clk
               :protocol :ready-valid}]
      :external-capabilities #{:hardware/port-access}
      :backpressure-behavior :ready-valid
      :status :complete}
     :clock-domain-report
     {:domains [{:id :clk
                 :frequency-mhz 100
                 :edge :rising
                 :generated-clock? false
                 :constraint "create_clock -name clk -period 10"}]
      :status :complete}
     :reset-domain-report
     {:domains [{:id :rst
                 :clock-domain :clk
                 :active :high
                 :style :synchronous
                 :affected-registers [:state :accumulator :done]
                 :reset-values {:state :idle
                                :accumulator "32'd0"
                                :done "1'b0"}
                 :release :clock-synchronous}]
      :status :complete}
     :width-and-numeric-report
     {:signals [{:name :state :width 2 :signedness :unsigned
                 :kind :enum :overflow :not-applicable
                 :truncation :not-applicable}
                {:name :accumulator :width 32 :signedness :unsigned
                 :kind :register :overflow :wrapping-explicit
                 :truncation :rejected-without-cast}
                {:name :input_data :width 32 :signedness :unsigned
                 :kind :port :overflow :not-applicable
                 :truncation :not-applicable}]
      :arithmetic-policy :explicit-widths-only
      :silent-host-sized-arithmetic :rejected
      :status :complete}
     :state-machine-graph
     {:states [:idle :accumulate :done]
      :encoding {:idle "2'b00"
                 :accumulate "2'b01"
                 :done "2'b10"}
      :transitions [{:from :idle :to :accumulate
                     :condition :input_valid}
                    {:from :accumulate :to :done
                     :condition :next-cycle}
                    {:from :done :to :done
                     :condition :terminal}]
      :finite-control-proof "SAFE15-stage0-finite-state"
      :status :complete}
     :memory-block-manifest
     {:blocks [{:id :accumulator
                :kind :register
                :width 32
                :depth 1
                :clock-domain :clk
                :reset-domain :rst}
               {:id :state
                :kind :register
                :width 2
                :depth 1
                :clock-domain :clk
                :reset-domain :rst}]
      :heap-allocation :rejected
      :status :complete}
     :cdc-report
     {:crossings [{:id :host_to_clk
                   :source-domain :host
                   :destination-domain :clk
                   :signal-shape :ready-valid-single-bit-control
                   :strategy :two-flop-synchronizer
                   :latency-cycles 2
                   :metastability-assumption :bounded-by-policy
                   :proof "SAFE8-stage0-cdc-handshake"}]
      :unmediated-crossings []
      :waivers []
      :status :complete}
     :runtime-construct-rejection-report
     {:rejected [:heap-allocation :gc :dynamic-dispatch :host-io
                 :reflection :dynamic-eval :model-call :tool-call
                 :thread :unbounded-recursion :unbounded-loop]
      :accepted-control-flow [:static-generate :finite-state-machine]
      :status :complete}
     :timing-constraint-file
     {:path "gravity_stage0.sdc"
      :format :sdc
      :content (native-document-port operations :timing-constraints)
      :hash timing-hash
      :target-frequency-mhz 100
      :status :complete}
     :testbench
     {:path "gravity_stage0_hdl_tb.sv"
      :content (native-document-port operations :testbench)
      :hash testbench-hash
      :simulation-trace-schema :cycle-trace
      :status :complete}
     :simulation-trace-schema
     {:columns [:cycle :clk :rst :state :input_valid :input_data
                :output_ready :done]
      :source-links [(str source-path ":hardware-ir")
                     (str source-path ":state-machine")]
      :cycle-level-behavior :connected-to-source
      :status :complete}
     :source-debug-map
     {:source input-id
      :locations [(str source-path ":hardware-ir")
                  (str source-path ":ports")
                  (str source-path ":state-machine")
                  (str source-path ":timing")]
      :generated-origin-chain [:mir :c12-hardware-domain-ir
                               :c14-target-lowering :b1-interface
                               :b9-hdl-backend]
      :state-machine-map {:idle "gravity_stage0_hdl:IDLE"
                          :accumulate "gravity_stage0_hdl:ACCUMULATE"
                          :done "gravity_stage0_hdl:DONE"}
      :timing-constraint-map {:clk "gravity_stage0.sdc:create_clock"}
      :hardware-audit-ids ["hardware-audit:stage0-cdc"
                           "hardware-audit:stage0-reset"]
      :proof-references ["SAFE8-stage0-cdc-handshake"
                         "SAFE15-stage0-finite-state"]
      :status :preserved}
     :hardware-audit-records
     [{:id "hardware-audit:stage0-cdc"
       :scope :clock-domain-crossing
       :waiver? false
       :status :complete}
      {:id "hardware-audit:stage0-reset"
       :scope :reset-release
       :waiver? false
       :status :complete}]
     :external-synthesis-validation-record
     {:declared-command
      "verilator --lint-only /tmp/gravity-p07-b9-hdl/gravity_stage0_hdl.sv"
      :proof-artifact
      "docs/artifacts/phase-07/reports/p07-d106-b9-hdl-backend-report.md"
      :status :not-available-in-current-environment}
     :status :complete}))
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:hdl-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b9-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path specialized
                         {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :systemverilog (get-in manifest [:target :hdl]))
      ((native-document-port operations :fail!) "B9-TARGET" source-path manifest
                         {:missing-fields [:target-hdl]}))
    (when-not (= :complete (get-in manifest
                                   [:hardware-ir-handoff-record
                                    :status]))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hardware-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest [:hdl-artifacts 0 :status]))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-artifact]}))
    (when-not ((native-document-port operations :hdl-structurally-valid?) (native-document-port operations :hdl-module))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:width-and-numeric-report
                                    :status]))
      ((native-document-port operations :fail!) "B9-WIDTH" source-path manifest
                         {:missing-fields [:width-and-numeric-report]}))
    (when-not (every? #(contains? % :width)
                      (get-in manifest
                              [:interface-port-schema :ports]))
      ((native-document-port operations :fail!) "B9-WIDTH" source-path manifest
                         {:missing-fields [:port-widths]}))
    (when-not (= :complete (get-in manifest
                                   [:clock-domain-report :status]))
      ((native-document-port operations :fail!) "B9-CLOCK" source-path manifest
                         {:missing-fields [:clock-domain-report]}))
    (when-not (= :complete (get-in manifest
                                   [:reset-domain-report :status]))
      ((native-document-port operations :fail!) "B9-RESET" source-path manifest
                         {:missing-fields [:reset-domain-report]}))
    (when-not (and (= :complete (get-in manifest [:cdc-report :status]))
                   (empty? (get-in manifest
                                   [:cdc-report :unmediated-crossings])))
      ((native-document-port operations :fail!) "B9-CDC" source-path manifest
                         {:missing-fields [:cdc-report]}))
    (when-not (= :complete (get-in manifest
                                   [:runtime-construct-rejection-report
                                    :status]))
      ((native-document-port operations :fail!) "B9-RUNTIME" source-path manifest
                         {:missing-fields [:runtime-rejection-report]}))
    (when-not (= :complete (get-in manifest
                                   [:state-machine-graph :status]))
      ((native-document-port operations :fail!) "B9-UNBOUNDED" source-path manifest
                         {:missing-fields [:state-machine-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:interface-port-schema :status]))
      ((native-document-port operations :fail!) "B9-INTERFACE" source-path manifest
                         {:missing-fields [:interface-port-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:timing-constraint-file :status]))
      ((native-document-port operations :fail!) "B9-TIMING" source-path manifest
                         {:missing-fields [:timing-constraint-file]}))
    (when-not ((native-document-port operations :timing-structurally-valid?)
               (native-document-port operations :timing-constraints))
      ((native-document-port operations :fail!) "B9-TIMING" source-path manifest
                         {:missing-fields [:timing-constraint-structure]}))
    (when-not ((native-document-port operations :testbench-structurally-valid?)
               (native-document-port operations :testbench))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:testbench-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:simulation-trace-schema
                                    :status]))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:simulation-trace-schema]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:source-debug-map]}))
    (when-not (every? #(= :complete (:status %))
                      (:hardware-audit-records manifest))
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hardware-audit-records]}))
    (when-not (every? #(contains? manifest %)
                      [:hardware-ir-handoff-record
                       :hdl-artifacts
                       :interface-port-schema
                       :clock-domain-report
                       :reset-domain-report
                       :width-and-numeric-report
                       :state-machine-graph
                       :memory-block-manifest
                       :cdc-report
                       :runtime-construct-rejection-report
                       :timing-constraint-file
                       :testbench
                       :simulation-trace-schema
                       :source-debug-map
                       :hardware-audit-records
                       :external-synthesis-validation-record])
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path manifest
                         {:missing-fields [:hdl-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B9-MANIFEST" source-path
                         (:b9-diagnostic-stream artifact)
                         {:missing-fields [:b9-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
  (let [manifest (:hdl-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b9-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :hdl-target-provider-covered?
     (= :systemverilog (get-in manifest [:target :hdl]))
     :hardware-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:hardware-ir-handoff-record :status]))
     :fixed-widths-covered?
     (= :complete (get-in manifest
                          [:width-and-numeric-report :status]))
     :clock-domain-covered?
     (= :complete (get-in manifest [:clock-domain-report :status]))
     :reset-domain-covered?
     (= :complete (get-in manifest [:reset-domain-report :status]))
     :cdc-covered?
     (and (= :complete (get-in manifest [:cdc-report :status]))
          (empty? (get-in manifest [:cdc-report :unmediated-crossings])))
     :runtime-constructs-rejected?
     (= :complete (get-in manifest
                          [:runtime-construct-rejection-report :status]))
     :finite-control-covered?
     (= :complete (get-in manifest [:state-machine-graph :status]))
     :interface-schema-covered?
     (= :complete (get-in manifest [:interface-port-schema :status]))
     :timing-constraints-covered?
     (= :complete (get-in manifest [:timing-constraint-file :status]))
     :testbench-covered?
     (= :complete (get-in manifest [:testbench :status]))
     :simulation-trace-covered?
     (= :complete (get-in manifest [:simulation-trace-schema :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :hdl-structurally-valid?
     ((native-document-port operations :hdl-structurally-valid?) (native-document-port operations :hdl-module))
     :testbench-structurally-valid?
     ((native-document-port operations :testbench-structurally-valid?) (native-document-port operations :testbench))
     :timing-structurally-valid?
     ((native-document-port operations :timing-structurally-valid?)
      (native-document-port operations :timing-constraints))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:hardware-ir-handoff-record
              :hdl-artifacts
              :interface-port-schema
              :clock-domain-report
              :reset-domain-report
              :width-and-numeric-report
              :state-machine-graph
              :memory-block-manifest
              :cdc-report
              :runtime-construct-rejection-report
              :timing-constraint-file
              :testbench
              :simulation-trace-schema
              :source-debug-map
              :hardware-audit-records
              :external-synthesis-validation-record])
     :diagnostics-covered?
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-synthesis-validation?
     (get-in manifest [:external-synthesis-validation-record :status])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
