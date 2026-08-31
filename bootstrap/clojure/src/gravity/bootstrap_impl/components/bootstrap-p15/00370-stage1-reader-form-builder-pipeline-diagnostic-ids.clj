

(def stage1-reader-form-builder-pipeline-diagnostic-ids
  ["STAGE1FORM001" "STAGE1FORM002" "STAGE1FORM003"
   "STAGE1FORM004" "STAGE1FORM005"])

(def stage1-reader-executor-pipeline-diagnostic-messages
  {"STAGE1EXEC001" "stage1 reader executor pipeline entrypoint is missing"
   "STAGE1EXEC002" "stage1 reader executor pipeline used unsupported executable Gravity"
   "STAGE1EXEC003" "stage1 reader executor pipeline requested an unsupported host primitive"
   "STAGE1EXEC004" "stage1 reader executor pipeline has an invalid executor or stream"
   "STAGE1EXEC005" "stage1 reader executor pipeline output diverged from stage0 reader forms"})

(def stage1-reader-executor-pipeline-diagnostic-ids
  ["STAGE1EXEC001" "STAGE1EXEC002" "STAGE1EXEC003"
   "STAGE1EXEC004" "STAGE1EXEC005"])

(def stage1-reader-runtime-pipeline-diagnostic-messages
  {"STAGE1RUN001" "stage1 reader runtime pipeline entrypoint is missing"
   "STAGE1RUN002" "stage1 reader runtime pipeline used unsupported executable Gravity"
   "STAGE1RUN003" "stage1 reader runtime pipeline requested an unsupported host primitive"
   "STAGE1RUN004" "stage1 reader runtime record or stream is invalid"
   "STAGE1RUN005" "stage1 reader runtime pipeline output diverged from stage0 reader forms"})

(def stage1-reader-runtime-pipeline-diagnostic-ids
  ["STAGE1RUN001" "STAGE1RUN002" "STAGE1RUN003"
   "STAGE1RUN004" "STAGE1RUN005"])

(def stage1-reader-compiled-pipeline-diagnostic-messages
  {"STAGE1COMP001" "stage1 reader compiled pipeline entrypoint is missing"
   "STAGE1COMP002" "stage1 reader compiled pipeline used unsupported executable Gravity"
   "STAGE1COMP003" "stage1 reader compiled pipeline requested an unsupported host primitive"
   "STAGE1COMP004" "stage1 reader compiled program is invalid"
   "STAGE1COMP005" "stage1 reader compiled pipeline output diverged from stage0 reader forms"})

(def stage1-reader-compiled-pipeline-diagnostic-ids
  ["STAGE1COMP001" "STAGE1COMP002" "STAGE1COMP003"
   "STAGE1COMP004" "STAGE1COMP005"])

(def stage1-reader-binary-pipeline-diagnostic-messages
  {"STAGE1BIN001" "stage1 reader binary pipeline entrypoint is missing"
   "STAGE1BIN002" "stage1 reader binary pipeline used unsupported executable Gravity"
   "STAGE1BIN003" "stage1 reader binary pipeline requested an unsupported host primitive"
   "STAGE1BIN004" "stage1 reader emitted binary is invalid"
   "STAGE1BIN005" "stage1 reader binary pipeline output diverged from stage0 reader forms"})

(def stage1-reader-binary-pipeline-diagnostic-ids
  ["STAGE1BIN001" "STAGE1BIN002" "STAGE1BIN003"
   "STAGE1BIN004" "STAGE1BIN005"])

(def stage1-reader-self-hosted-runtime-diagnostic-messages
  {"STAGE1SELF001" "stage1 reader self-hosted runtime entrypoint is missing"
   "STAGE1SELF002" "stage1 reader self-hosted runtime used unsupported executable Gravity"
   "STAGE1SELF003" "stage1 reader self-hosted runtime requested an unsupported host primitive"
   "STAGE1SELF004" "stage1 reader self-hosted runtime record is invalid"
   "STAGE1SELF005" "stage1 reader self-hosted runtime output diverged from stage0 reader forms"})

(def stage1-reader-self-hosted-runtime-diagnostic-ids
  ["STAGE1SELF001" "STAGE1SELF002" "STAGE1SELF003"
   "STAGE1SELF004" "STAGE1SELF005"])

(def stage1-reader-core-bootstrap-diagnostic-messages
  {"STAGE1CORE001" "stage1 reader core-bootstrap entrypoint is missing"
   "STAGE1CORE002" "stage1 reader core-bootstrap runtime requested an unsupported builtin operation"
   "STAGE1CORE003" "stage1 reader core-bootstrap builtin record is missing"
   "STAGE1CORE004" "stage1 reader core-bootstrap runtime output diverged from stage0 reader forms"
   "STAGE1CORE005" "stage1 reader core-bootstrap runtime attempted an illegal host fallback"
   "STAGE1CORE006" "stage1 reader core-bootstrap runtime record is invalid"})

(def stage1-reader-core-bootstrap-diagnostic-ids
  ["STAGE1CORE001" "STAGE1CORE002" "STAGE1CORE003"
   "STAGE1CORE004" "STAGE1CORE005" "STAGE1CORE006"])

(def stage1-reader-compiler-driver-diagnostic-messages
  {"STAGE1DRV001" "stage1 reader compiler-driver entrypoint is missing"
   "STAGE1DRV002" "stage1 reader compiler-driver requested an unsupported driver operation"
   "STAGE1DRV003" "stage1 reader compiler-driver record is missing"
   "STAGE1DRV004" "stage1 reader compiler-driver artifact routing diverged"
   "STAGE1DRV005" "stage1 reader compiler-driver diagnostic stream diverged"
   "STAGE1DRV006" "stage1 reader compiler-driver attempted a seed orchestration fallback"
   "STAGE1DRV007" "stage1 reader compiler-driver record is invalid"})

(def stage1-reader-compiler-driver-diagnostic-ids
  ["STAGE1DRV001" "STAGE1DRV002" "STAGE1DRV003"
   "STAGE1DRV004" "STAGE1DRV005" "STAGE1DRV006"
   "STAGE1DRV007"])

(def stage1-reader-runtime-entrypoint-diagnostic-messages
  {"STAGE1RTE001" "stage1 reader runtime entrypoint is missing"
   "STAGE1RTE002" "stage1 reader runtime entrypoint requested an unsupported runtime operation"
   "STAGE1RTE003" "stage1 reader runtime entrypoint record is missing"
   "STAGE1RTE004" "stage1 reader runtime entrypoint source routing diverged"
   "STAGE1RTE005" "stage1 reader runtime entrypoint artifact output diverged"
   "STAGE1RTE006" "stage1 reader runtime entrypoint process exit mapping diverged"
   "STAGE1RTE007" "stage1 reader runtime entrypoint attempted a runner fallback"
   "STAGE1RTE008" "stage1 reader runtime entrypoint record is invalid"})

(def stage1-reader-runtime-entrypoint-diagnostic-ids
  ["STAGE1RTE001" "STAGE1RTE002" "STAGE1RTE003"
   "STAGE1RTE004" "STAGE1RTE005" "STAGE1RTE006"
   "STAGE1RTE007" "STAGE1RTE008"])

(def stage1-reader-runtime-image-diagnostic-messages
  {"STAGE1IMG001" "stage1 reader runtime image entrypoint is missing"
   "STAGE1IMG002" "stage1 reader runtime image requested an unsupported image operation"
   "STAGE1IMG003" "stage1 reader runtime image record is missing"
   "STAGE1IMG004" "stage1 reader runtime image filesystem authority diverged"
   "STAGE1IMG005" "stage1 reader runtime image stdout routing diverged"
   "STAGE1IMG006" "stage1 reader runtime image provenance is incomplete"
   "STAGE1IMG007" "stage1 reader runtime image attempted an OS boundary fallback"
   "STAGE1IMG008" "stage1 reader runtime image record is invalid"})

(def stage1-reader-runtime-image-diagnostic-ids
  ["STAGE1IMG001" "STAGE1IMG002" "STAGE1IMG003"
   "STAGE1IMG004" "STAGE1IMG005" "STAGE1IMG006"
   "STAGE1IMG007" "STAGE1IMG008"])

(def stage1-reader-verified-boot-chain-diagnostic-messages
  {"STAGE1BOOT001" "stage1 reader verified boot-chain entrypoint is missing"
   "STAGE1BOOT002" "stage1 reader verified boot-chain requested an unsupported boot operation"
   "STAGE1BOOT003" "stage1 reader verified boot-chain record is missing"
   "STAGE1BOOT004" "stage1 reader verified boot-chain artifact loader diverged"
   "STAGE1BOOT005" "stage1 reader verified boot-chain scheduler authority diverged"
   "STAGE1BOOT006" "stage1 reader verified boot-chain provenance is not reproducible"
   "STAGE1BOOT007" "stage1 reader verified boot-chain attempted a machine or kernel fallback"
   "STAGE1BOOT008" "stage1 reader verified boot-chain record is invalid"})

(def stage1-reader-verified-boot-chain-diagnostic-ids
  ["STAGE1BOOT001" "STAGE1BOOT002" "STAGE1BOOT003"
   "STAGE1BOOT004" "STAGE1BOOT005" "STAGE1BOOT006"
   "STAGE1BOOT007" "STAGE1BOOT008"])

(def stage1-reader-diverse-bootstrap-verification-diagnostic-messages
  {"STAGE1DIV001" "stage1 reader diverse bootstrap verification entrypoint is missing"
   "STAGE1DIV002" "stage1 reader diverse bootstrap verification requested an unsupported operation"
   "STAGE1DIV003" "stage1 reader diverse bootstrap verification record is missing"
   "STAGE1DIV004" "stage1 reader diverse bootstrap verification attempted single-implementation self-certification"
   "STAGE1DIV005" "stage1 reader diverse bootstrap verification traces diverged"
   "STAGE1DIV006" "stage1 reader diverse bootstrap verification provenance is not reproducible"
   "STAGE1DIV007" "stage1 reader diverse bootstrap verification independent audit metadata is missing"
   "STAGE1DIV008" "stage1 reader diverse bootstrap verification attempted a hardware, firmware, or external-auditor fallback"
   "STAGE1DIV009" "stage1 reader diverse bootstrap verification record is invalid"})

(def stage1-reader-diverse-bootstrap-verification-diagnostic-ids
  ["STAGE1DIV001" "STAGE1DIV002" "STAGE1DIV003"
   "STAGE1DIV004" "STAGE1DIV005" "STAGE1DIV006"
   "STAGE1DIV007" "STAGE1DIV008" "STAGE1DIV009"])

(def stage1-reader-release-attestation-seed-retirement-diagnostic-messages
  {"STAGE1REL001" "stage1 reader release attestation seed-retirement entrypoint is missing"
   "STAGE1REL002" "stage1 reader release attestation seed-retirement requested an unsupported operation"
   "STAGE1REL003" "stage1 reader release attestation record is missing"
   "STAGE1REL004" "stage1 reader seed-retirement evidence is missing"
   "STAGE1REL005" "stage1 reader release custody is not reproducible"
   "STAGE1REL006" "stage1 reader supply-chain manifest is unverifiable"
   "STAGE1REL007" "stage1 reader governance approval is missing"
   "STAGE1REL008" "stage1 reader release attestation attempted a physical, supply-chain, or independent-review fallback"
   "STAGE1REL009" "stage1 reader release input is revoked"
   "STAGE1REL010" "stage1 reader release attestation seed-retirement record is invalid"})

(def stage1-reader-release-attestation-seed-retirement-diagnostic-ids
  ["STAGE1REL001" "STAGE1REL002" "STAGE1REL003"
   "STAGE1REL004" "STAGE1REL005" "STAGE1REL006"
   "STAGE1REL007" "STAGE1REL008" "STAGE1REL009"
   "STAGE1REL010"])

(def stage1-reader-formal-release-governance-seed-retirement-diagnostic-messages
  {"STAGE1GOV001" "stage1 reader formal release governance seed-retirement entrypoint is missing"
   "STAGE1GOV002" "stage1 reader formal release governance requested an unsupported operation"
   "STAGE1GOV003" "stage1 reader formal release governance record is missing"
   "STAGE1GOV004" "stage1 reader deployment custody record is unverifiable"
   "STAGE1GOV005" "stage1 reader self-hosting evidence is missing"
   "STAGE1GOV006" "stage1 reader full compiler rebuild evidence is not reproducible"
   "STAGE1GOV007" "stage1 reader stage compiler equivalence evidence is missing"
   "STAGE1GOV008" "stage1 reader TCB delta record is missing"
   "STAGE1GOV009" "stage1 reader formal release governance attempted a human governance or deployment custody fallback"
   "STAGE1GOV010" "stage1 reader formal release governance seed-retirement record is invalid"})

(def stage1-reader-formal-release-governance-seed-retirement-diagnostic-ids
  ["STAGE1GOV001" "STAGE1GOV002" "STAGE1GOV003"
   "STAGE1GOV004" "STAGE1GOV005" "STAGE1GOV006"
   "STAGE1GOV007" "STAGE1GOV008" "STAGE1GOV009"
   "STAGE1GOV010"])

(def p15-s23-whole-language-self-hosting-gate-diagnostic-messages
  {"P15S23001" "whole-language self-hosting compiler artifact is missing"
   "P15S23002" "whole-language compiler pipeline manifest is missing"
   "P15S23003" "source unit and syntax-object serialization evidence is missing"
   "P15S23004" "core lowering and diagnostic preservation evidence is missing"
   "P15S23005" "runtime manifest and capability enforcement evidence is missing"
   "P15S23006" "accepted Gravity application execution proof is missing"
   "P15S23007" "rejected Gravity application diagnostic proof is missing"
   "P15S23008" "reproducible rebuild log is missing"
   "P15S23009" "stage comparison and equivalence report is missing"
   "P15S23010" "self-hosting conformance report is missing"
   "P15S23011" "bootstrap provenance attestation is missing"
   "P15S23012" "trusted-computing-base delta record is missing"
   "P15S23013" "unsafe audit report is missing"
   "P15S23014" "Clojure seed boundary is not retired"
   "P15S23015" "governance and ecosystem package release evidence is missing"
   "P15S23016" "full self-hosting or seed-retirement claim is unsupported"})

(def p15-s23-whole-language-self-hosting-gate-diagnostic-ids
  ["P15S23001" "P15S23002" "P15S23003" "P15S23004"
   "P15S23005" "P15S23006" "P15S23007" "P15S23008"
   "P15S23009" "P15S23010" "P15S23011" "P15S23012"
   "P15S23013" "P15S23014" "P15S23015" "P15S23016"])