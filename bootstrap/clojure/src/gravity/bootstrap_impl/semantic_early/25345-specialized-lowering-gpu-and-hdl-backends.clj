; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-gpu-and-hdl-backends
 [source-path state]
 (let
  [{:keys [gpu-module gpu-manifest hdl-module hdl-manifest]} state]
  (assoc
   {}
   :gpu-backend
   {:transfer-graph
    {:edges
     [{:from :host, :to :device, :buffer :input}
      {:from :device, :to :host, :buffer :output}],
     :implicit-transfers [],
     :status :complete},
    :math-certificate-bundle
    {:numeric-mode :strict,
     :certificates ["MATH8-stage0"],
     :status :complete},
    :synchronization-graph
    {:edges
     [{:before :copy-in, :after :kernel-launch}
      {:before :kernel-launch, :after :copy-out}],
     :status :complete},
    :launch-descriptor
    {:grid [1 1 1],
     :workgroup [32 1 1],
     :shared-memory-bytes 0,
     :status :complete},
    :status :complete,
    :kernel-modules
    [{:path "gravity_stage0.spv.ir",
      :content gpu-module,
      :hash (:content-hash gpu-manifest)}],
    :device-memory-lifetime-report
    {:buffers
     [{:id :input,
       :ownership :linear,
       :transfer-state :host-to-device}],
     :status :complete},
    :artifact :gravity/gpu-backend-manifest,
    :target
    {:api :spir-v,
     :device-class :gpu,
     :features #{:shared-memory :subgroups :fp16}},
    :backend :gravity.backend/gpu,
    :host-device-boundary
    {:kernel :gravity_stage0_kernel,
     :arguments
     [{:name :input,
       :address-space :global,
       :layout :i64,
       :alignment 8,
       :lifetime :kernel}],
     :capability-grants #{:gpu/launch :memory/device},
     :status :declared}}
   :hdl-backend
   {:hdl-artifacts
    [{:path "gravity_stage0.sv",
      :content hdl-module,
      :hash (:content-hash hdl-manifest)}],
    :hardware-ir-handoff-record
    {:domain-anchor :hardware-circuit, :status :complete},
    :timing-constraint-file
    {:format :sdc,
     :constraints ["create_clock -period 10 clk"],
     :status :complete},
    :status :complete,
    :interface-port-schema
    {:ports
     [{:name :clk, :direction :input, :width 1, :clock-domain :clk}
      {:name :done, :direction :output, :width 1, :clock-domain :clk}],
     :status :complete},
    :artifact :gravity/hdl-backend-manifest,
    :testbench
    {:path "gravity_stage0_tb.sv",
     :simulation-trace-schema :cycle-trace,
     :status :complete},
    :reset-domain-report
    {:domains [{:id :rst, :active :high, :style :synchronous}],
     :status :complete},
    :target {:hdl :systemverilog, :synthesis-tool :stage0-provider},
    :backend :gravity.backend/hdl,
    :clock-domain-report
    {:domains [{:id :clk, :frequency-mhz 100, :edge :rising}],
     :status :complete},
    :state-machine-graph
    {:states [:idle :done],
     :transitions [[:idle :done]],
     :status :complete}})))
