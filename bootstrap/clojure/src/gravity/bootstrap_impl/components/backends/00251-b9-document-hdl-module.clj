

(def b9-document-hdl-module
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

(def b9-document-testbench
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

(def b9-document-timing-constraints
  (str
   "create_clock -name clk -period 10 [get_ports clk]\n"
   "set_input_delay 1 -clock clk [get_ports input_data]\n"
   "set_output_delay 1 -clock clk [get_ports done]\n"))

(defn b9-document-hdl-structurally-valid?
  [text]
  (and (str/includes? text "module gravity_stage0_hdl")
       (str/includes? text "typedef enum logic [1:0]")
       (str/includes? text "always_ff @(posedge clk)")
       (str/includes? text "if (rst)")
       (str/includes? text "assign output_ready")))

(defn b9-document-testbench-structurally-valid?
  [text]
  (and (str/includes? text "module gravity_stage0_hdl_tb")
       (str/includes? text "gravity_stage0_hdl dut")
       (str/includes? text "repeat (")
       (str/includes? text "$finish")))

(defn b9-document-timing-structurally-valid?
  [text]
  (and (str/includes? text "create_clock")
       (str/includes? text "set_input_delay")
       (str/includes? text "set_output_delay")))

(defn b9-document-diagnostic-stream
  [source-path input-id]
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
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b9-document-syntax-" index)
                      :artifact input-id}
            :profile :hardware
            :target :systemverilog
            :hardware-module :gravity_stage0_hdl
            :signal-or-state-id (b9-document-signal-id id)
            :clock-domain :clk
            :reset-domain :rst
            :missing-proof-or-constraint (b9-document-missing-fact id)
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
         b9-document-diagnostic-ids
         (range))
   :status :complete})