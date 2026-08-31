

(def stage1-reader-compiled-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1COMP001"
      :rejected-behavior :missing-gravity-reader-compiled-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1COMP002"
      :rejected-behavior :unsupported-gravity-reader-compiled-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1COMP003"
      :rejected-behavior :compiled-reader-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1COMP004"
      :rejected-behavior :invalid-reader-compiled-program}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1COMP005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-binary-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1BIN001"
      :rejected-behavior :missing-gravity-reader-binary-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BIN002"
      :rejected-behavior :unsupported-gravity-reader-binary-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BIN003"
      :rejected-behavior :binary-reader-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BIN004"
      :rejected-behavior :invalid-reader-emitted-binary}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1BIN005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-self-hosted-runtime-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1SELF001"
      :rejected-behavior :missing-gravity-reader-self-hosted-runtime-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1SELF002"
      :rejected-behavior :unsupported-gravity-reader-self-hosted-runtime-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1SELF003"
      :rejected-behavior :self-hosted-reader-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1SELF004"
      :rejected-behavior :invalid-reader-self-hosted-runtime}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1SELF005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-core-bootstrap-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1CORE001"
      :rejected-behavior :missing-gravity-reader-core-bootstrap-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CORE002"
      :rejected-behavior :unsupported-gravity-reader-core-bootstrap-builtin}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CORE003"
      :rejected-behavior :missing-reader-core-bootstrap-builtin-record}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1CORE004"
      :rejected-behavior :core-bootstrap-stage0-reader-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CORE005"
      :rejected-behavior :core-bootstrap-host-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CORE006"
      :rejected-behavior :invalid-reader-core-bootstrap-runtime}])))

(def stage1-reader-compiler-driver-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV001"
      :rejected-behavior :missing-gravity-reader-compiler-driver-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV002"
      :rejected-behavior :unsupported-gravity-reader-compiler-driver-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV003"
      :rejected-behavior :missing-reader-compiler-driver-record}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1DRV004"
      :rejected-behavior :compiler-driver-artifact-routing-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV005"
      :rejected-behavior :compiler-driver-diagnostic-stream-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV006"
      :rejected-behavior :compiler-driver-seed-orchestration-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DRV007"
      :rejected-behavior :invalid-reader-compiler-driver}])))

(def stage1-reader-runtime-entrypoint-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE001"
      :rejected-behavior :missing-gravity-reader-runtime-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE002"
      :rejected-behavior :unsupported-gravity-reader-runtime-entrypoint-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE003"
      :rejected-behavior :missing-reader-runtime-entrypoint-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE004"
      :rejected-behavior :runtime-entrypoint-source-routing-divergence}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1RTE005"
      :rejected-behavior :runtime-entrypoint-artifact-output-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE006"
      :rejected-behavior :runtime-entrypoint-process-exit-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE007"
      :rejected-behavior :runtime-entrypoint-runner-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RTE008"
      :rejected-behavior :invalid-reader-runtime-entrypoint}])))

(def stage1-reader-runtime-image-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG001"
      :rejected-behavior :missing-gravity-reader-runtime-image}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG002"
      :rejected-behavior :unsupported-gravity-reader-runtime-image-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG003"
      :rejected-behavior :missing-reader-runtime-image-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG004"
      :rejected-behavior :runtime-image-filesystem-authority-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG005"
      :rejected-behavior :runtime-image-stdout-routing-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG006"
      :rejected-behavior :runtime-image-provenance-gap}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG007"
      :rejected-behavior :runtime-image-os-boundary-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1IMG008"
      :rejected-behavior :invalid-reader-runtime-image}])))

(def stage1-reader-verified-boot-chain-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT001"
      :rejected-behavior :missing-gravity-reader-verified-boot-chain}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT002"
      :rejected-behavior :unsupported-gravity-reader-boot-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT003"
      :rejected-behavior :missing-reader-verified-boot-chain-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT004"
      :rejected-behavior :verified-boot-chain-artifact-loader-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT005"
      :rejected-behavior :verified-boot-chain-scheduler-authority-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT006"
      :rejected-behavior :verified-boot-chain-unreproducible-provenance}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT007"
      :rejected-behavior :verified-boot-chain-machine-kernel-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1BOOT008"
      :rejected-behavior :invalid-reader-verified-boot-chain}])))

(def stage1-reader-diverse-bootstrap-verification-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV001"
      :rejected-behavior :missing-gravity-reader-diverse-bootstrap-verification}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV002"
      :rejected-behavior :unsupported-diverse-bootstrap-verification-operation}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV003"
      :rejected-behavior :missing-reader-diverse-bootstrap-verification-record}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV004"
      :rejected-behavior :diverse-bootstrap-single-implementation-self-certification}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1DIV005"
      :rejected-behavior :diverse-bootstrap-trace-divergence}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV006"
      :rejected-behavior :diverse-bootstrap-unreproducible-provenance}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV007"
      :rejected-behavior :diverse-bootstrap-independent-audit-metadata-gap}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV008"
      :rejected-behavior :diverse-bootstrap-trust-anchor-fallback}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1DIV009"
      :rejected-behavior :invalid-reader-diverse-bootstrap-verification}])))