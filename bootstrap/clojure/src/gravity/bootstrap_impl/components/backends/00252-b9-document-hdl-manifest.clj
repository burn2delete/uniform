

(defn b9-document-hdl-manifest
  [source-path input-id]
  (let [hdl-hash (c4-artifact-id b9-document-hdl-module)
        testbench-hash (c4-artifact-id b9-document-testbench)
        timing-hash (c4-artifact-id b9-document-timing-constraints)]
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
       :content b9-document-hdl-module
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
      :content b9-document-timing-constraints
      :hash timing-hash
      :target-frequency-mhz 100
      :status :complete}
     :testbench
     {:path "gravity_stage0_hdl_tb.sv"
      :content b9-document-testbench
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