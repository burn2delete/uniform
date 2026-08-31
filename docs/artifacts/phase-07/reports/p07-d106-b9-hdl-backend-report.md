# P07-D106 B9 HDL Backend Proof Report

Date: 2026-06-29
Task: `P07-D106`
Status: complete (stage0 B9 HDL backend document coverage)

## Governing Document Read

- `docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b9-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-d106-b9-hdl-backend-proof.edn`

The `backend-b9-hdl-document` command emits
`:gravity/stage0-b9-hdl-backend-document-artifact` from the current P07-T04
specialized lowering artifact. It records B9 HDL target and provider facts,
hardware IR handoff, SystemVerilog output, interface and port schema,
clock-domain and reset-domain reports, fixed-width numeric records, state
machine graph, memory block manifest, CDC proof records, runtime construct
rejection, timing constraints, testbench, simulation trace schema, source/debug
map, hardware audit records, B9 diagnostics, document-specific results, and
capability-based proof.

## Validation

```text
clojure -M:gravity backend-b9-hdl-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-b9-hdl-backend-document-artifact,
 :task "P07-D106",
 :artifact-id "sha256:4afd7038d44f6c020c9ddeddedc724fd59a5b6bdcfa902a6a6275170f0ef0314",
 :document-set ["B9"],
 :diagnostics 10,
 :rejected-designs 5,
 :conformance-criteria 9,
 :hdl-structural true,
 :testbench-structural true,
 :timing-structural true,
 :external-synthesis :not-available-in-current-environment,
 :proof :complete}
```

HDL module hash:

```text
sha256:1f7c59778761a604be1ad287c60103254fa0b45b319861533a4f77ff22e0e201
```

Testbench hash:

```text
sha256:4cde3076705fdca5669735b48cd9cf0ad512a80865db28f33c179f86b3b72db9
```

Timing constraint hash:

```text
sha256:b728232c1b5321624afab7674b1354a430965af9de40ee155d88ea037de8edf2
```

```text
clojure -M -e <extract B9 HDL, testbench, and timing constraints>
{:dir "/tmp/gravity-p07-b9-hdl",
 :files ("gravity_stage0.sdc" "gravity_stage0_hdl.sv" "gravity_stage0_hdl_tb.sv"),
 :hdl-structural true,
 :testbench-structural true,
 :timing-structural true,
 :external-synthesis :not-available-in-current-environment}
```

```text
sed -n '1,35p' /tmp/gravity-p07-b9-hdl/gravity_stage0_hdl.sv
module gravity_stage0_hdl(
  input logic clk,
  input logic rst,
  input logic input_valid,
  input logic [31:0] input_data,
  output logic output_ready,
  output logic done
);
  typedef enum logic [1:0] {IDLE, ACCUMULATE, DONE} state_t;
  state_t state;
  logic [31:0] accumulator;
  always_ff @(posedge clk) begin
    if (rst) begin
      state <= IDLE;
      accumulator <= 32'd0;
      done <= 1'b0;
    end else begin
      case (state)
        IDLE: if (input_valid) begin accumulator <= input_data; state <= ACCUMULATE; end
        ACCUMULATE: begin accumulator <= accumulator + 32'd1; state <= DONE; end
        DONE: begin done <= 1'b1; state <= DONE; end
      endcase
    end
  end
  assign output_ready = (state == DONE);
endmodule
```

```text
sed -n '1,30p' /tmp/gravity-p07-b9-hdl/gravity_stage0_hdl_tb.sv
module gravity_stage0_hdl_tb;
  logic clk = 1'b0;
  logic rst = 1'b1;
  logic input_valid = 1'b0;
  logic [31:0] input_data = 32'd7;
  logic output_ready;
  logic done;
  gravity_stage0_hdl dut(clk, rst, input_valid, input_data, output_ready, done);
  always #5 clk = ~clk;
  initial begin
    repeat (2) @(posedge clk);
    rst = 1'b0;
    input_valid = 1'b1;
    repeat (4) @(posedge clk);
    $finish;
  end
endmodule
```

```text
sed -n '1,10p' /tmp/gravity-p07-b9-hdl/gravity_stage0.sdc
create_clock -name clk -period 10 [get_ports clk]
set_input_delay 1 -clock clk [get_ports input_data]
set_output_delay 1 -clock clk [get_ports done]
```

```text
verilator --version
zsh:1: command not found: verilator
```

The HDL artifacts are structurally validated by the Clojure proof and recorded
for external HDL lint/synthesis validation when `verilator` or equivalent
tooling is available.

```text
clojure -M:test
Ran 85 tests containing 4946 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend proof EDN parse>
{:parsed 15,
 :tasks [:P07-D098 :P07-D099 :P07-D100 :P07-D101 :P07-D102 :P07-D103 :P07-D104 :P07-D105 :P07-D106 :P07-T01 :P07-T02 :P07-T03 :P07-T04 :P07-T05 :P07-T06],
 :statuses [:complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete]}
```

```text
git diff --check
passed
```

## Rejected Diagnostics

The rejected fixture suite covers all B9 HDL backend diagnostic IDs:

- `B9-TARGET`
- `B9-WIDTH`
- `B9-CLOCK`
- `B9-RESET`
- `B9-CDC`
- `B9-RUNTIME`
- `B9-UNBOUNDED`
- `B9-INTERFACE`
- `B9-TIMING`
- `B9-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-d106-b9-hdl-backend-proof.edn`

## Remaining Limits

This completes `P07-D106` for deterministic Clojure stage0 coverage of the B9
HDL backend design contract. The emitted HDL manifest includes structural
SystemVerilog, interface and port schema, clock/reset reports, fixed-width
numeric records, state-machine graph, memory block manifest, CDC proof record,
runtime construct rejection, timing constraints, testbench, simulation trace
schema, source/debug map, hardware audit records, and stable B9 diagnostics.
The current environment does not provide `verilator`, so this does not claim
external HDL lint, synthesis, simulation, timing closure, hardware device
validation, or full Phase 07 completion.
