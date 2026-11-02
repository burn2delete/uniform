(ns runtime-decision-allocation
  "Bounded phase-08 current-decision allocations; no canonical writes."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.pprint :as pp]))
(binding [*command-line-args* nil] (load-file "tools/runtime_requirement_family.clj"))
(require '[runtime-requirement-family :as f] '[runtime-task-projection :as tasks])
(def base "86747916c4c876d707a1548113409fb939d854c9")
(def partition-sha "179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74")
(def decision-path "specs/requirement-reconciliation/source-dispositions.edn")
(def output "specs/requirement-reconciliation/runtime-decision-allocation.edn")
(def criterion-outputs
  {"ai-memory-policy" ["Provider/tenant/schema-bound memory access, retrieval, retention and deletion decisions" "Cross-namespace, ungranted write, secret-retention and tainted-sink denial fixtures"]
   "concurrency-memory-order" ["Atomic operation/order/scope/alignment and provider/target support table with synchronization traces" "Unsupported-order/scope/alignment, race and hidden-thread-fallback diagnostics"]
   "durable-effect-replay" ["Versioned workflow/step/event completion, restart and replay-equivalence traces" "Recorded-output mismatch/live-call/duplicate-effect rejection plus permitted R6 repetition fixture"]
   "metadata-preservation" ["Runtime/FFI/host/device/workflow provenance and taint round-trip bundles" "Required source/artifact/check/proof/replay edge loss and forgery diagnostics"]
   "model-provider-execution" ["Provider/model/prompt-role/digest/parameter/decoding/schema-bound actual model-call and validation ledger" "Prohibited substitution, mismatched identity, invalid output, missing grant and live replay-call rejection"]
   "nondeterminism-recording" ["Typed record-mode action/error/retry/scheduler trace and replay-consumption result" "Missing or mismatched identity/order/cardinality and prohibited live replay reexecution diagnostics"]
   "policy-safe-observation" ["Policy-redacted typed event/log/trace/metric/replay/diagnostic bundles and mandatory-event sampling comparison" "Ungranted sink, disallowed disclosure, lost audit event and instrumentation semantic-drift diagnostics"]
   "replay-compatible-upgrade" ["Code/topology/schema/workflow-version compatibility, migration and old-history replay report" "Incompatible/unversioned upgrade, implicit history rewrite and missing-policy diagnostics"]
   "tool-provider-execution" ["Tool/provider/schema/effect/grant/side-effect/review/taint-bound invocation and decision records" "Caller/callee authority mismatch, absent required review, secret-policy, invalid-sink and live recorded-segment call rejection"]
   "typed-message-delivery" ["Versioned message/ownership transfer, delivery/ordering/backpressure/cancellation and receive traces" "Schemaless/mismatched message, illegal sharing/transfer/lifetime and unsupported blocking/ordering diagnostics"]
   "boundary-validation" ["Source-schema/version/sink-bound validation and trust/taint admission records with typed failures" "Invalid field/refinement/null/foreign/schema identity, unvalidated taint, absent sink grants and silent-coercion rejection"]})
(def witness-dependencies
  {"deterministic-resource-cleanup" ["Ownership linear-resource flow and provider failure/partial-initialization contract" "Lower panic/cleanup ABI and provider identity" "Stdlib resource adapter terminal-operation contract"]
   "foreign-call-adaptation" ["Schema S7 FFI layout and safe-wrapper boundary manifest" "Lower ABI/layout validation" "Auth foreign-effect grants and callback thread/taint inputs" "Ownership lifetime/transfer evidence"]
   "service-manifest" ["Lower emitted artifact service/provider graph" "Legality profile/target service eligibility" "Build package runtime policy" "Auth enforcement hook or applicable static proof"]
   "service-policy-enforcement" ["Auth compiled grants, principal/provider identity, caller/callee intersection and revocation" "Legality accepted effects/profile facts" "Schema/Query/Stdlib actual service-operation identity"]
   "startup-cleanup-order" ["Lower entry ABI, reset/section/memory and initialization records" "Legality selected services" "Auth grants and L15 provider/fallback identity" "Runtime resource teardown/failure contracts"]
   "typed-host-delegation" ["Schema typed-boundary and foreign metadata" "Auth host-effect grants" "Lower host/runtime/version/symbol/source-map identity" "Core value semantics and Stdlib host-adapter contracts"]})
(def definitions
  {"ai-memory-policy" {:row 3 :criterion "At the selected AI memory/retrieval provider enforce source/trust/tenant namespace, schema and sink-taint policy, privacy, retention, deletion and embedding/provider identity. Emit memory read/write/retrieval/deletion decisions and replay identity; accept validated authorized access, reject cross-namespace/ungranted writes, expired retention, forbidden secret storage and unvalidated retrieved content at trusted sinks. Provider billing/account constraints remain external; project accounting cannot become an admission condition." :dependencies ["AI memory/retrieval declarations" "Schema validator identity" "Auth namespace and sink policy"]}
   "concurrency-memory-order" {:row 2 :criterion "For every selected atomic/lock/thread/scheduler provider execute the declared operation, memory order, synchronization scope, alignment and target support under accepted race/ownership evidence. Emit operation/provider/target records and synchronization/transfer traces; accept representable supported synchronization and reject unsupported orders/scopes, misalignment, unprotected shared mutation and undeclared host-thread fallback. Compile-time race evidence remains required independently of debug checks." :dependencies ["Legality SAFE8 and ownership facts" "Lower target atomic ABI" "Stdlib concurrency provider contract"]}
   "deterministic-resource-cleanup" {:row 1 :needles ["deterministic linear cleanup independent of process exit or GC" "execute deterministic cleanup for linear host resources despite GC" "declared allocation and cleanup failures, partial initialization cleanup"] :extra-row 2 :reason "The startup/managed task requires executed deterministic terminal operations independent of process/GC, while the memory/FFI task supplies partial-initialization, failure and correct-provider cleanup. Managed object lifetime and compile-time exactly-once flow remain distinct."}
   "durable-effect-replay" {:row 3 :criterion "Execute durable steps against versioned recorded inputs/outputs with workflow/step/event/effect/provider identity, idempotency and declared completion/transaction boundaries. Recorded-output segments consume matching records without reissuing external actions, rejecting missing/mismatched/order-inconsistent records; preserve retries, cancellation and compensation policy. R6 graph-declared replay-safe repetitions may execute only where no R7/R8 recorded-output obligation applies and authority still holds. Emit restart/replay equality and actual no-duplicate-effect traces; a replay-safe label cannot override required recorded behavior." :dependencies ["Workflow graph and mode policy" "Schema event/state versions" "Auth current grants" "Stdlib workflow/database/IO providers"]}
   "durable-failure-recovery" {:row 3 :adjudication "Four named finite18 facets have no document/occurrence/byte/hash metadata in the frozen dispatch index. Preserve every ID and existing R7 retry/timeout/cancellation/failure/compensation criterion. The canonical source owner must recover the exact immutable facet records and check source-qualified task semantics before claiming full-facet coverage; no retry allowance or guessed replacement span is introduced."}
   "forbidden-service-rejection" {:row 0 :adjudication "Six named finite18 facets lack immutable source metadata. Existing first Runtime task already rejects hidden services and explicitly permits evidence-backed static elimination OR rejection. Retain that conditional alternative and target representability/physical requirements; recover each missing facet's true source before total entailment/adoption, without inventing an elimination mandate or a compiler quota."}
   "foreign-call-adaptation" {:row 2 :needles ["FFI symbol/loading policy, ABI/layout conformance, null/handle/lifetime/ownership/error adapters" "callbacks re-entering capability/taint/thread-affinity/scheduler checks" "raw native imports are not directly safe-callable"] :reason "The executed FFI task binds actual ABI/layout/provider and wrapper/null/ownership/failure conditions, including callback reentry, denied foreign calls and opaque-only fallback when host metadata is insufficient. S7 schema/layout artifacts are producer dependencies, not execution acceptance."}
   "metadata-preservation" {:row 4 :criterion "At every selected runtime/FFI/host/device/workflow boundary preserve original source and generated-origin edges, artifact/profile/target/provider identity, declared effects/grants, taint and required proof/replay/audit links into the emitted service event and diagnostic bundle. Emit successful cross-boundary round-trip evidence and reject lost/forged required origins, effects, taint, proof or event links. Where mapping support is conditional, record unavailable optional mappings honestly and reject missing required mappings; diagnostic publication alone is not the runtime consumer." :dependencies ["Emit artifact/provenance identity" "Lower generated source maps" "Diag stable event shape" "Schema boundary adapters"]}
   "model-provider-execution" {:row 3 :criterion "Invoke the declared model/provider through its typed prompt/message provenance, roles/digests, parameters/decoding settings, output schema and validation policy, preserving input taint, output validation, errors, source identity and replay/audit mode. Record actual provider/model substitution only when policy permits it. Accept an authorized validated response and reject wrong provider/model/decoding identity, prohibited substitution, missing validation/grant or live invocation in a required recorded-output segment; retain external billing policy without compiler work quotas." :dependencies ["AI model/prompt/output declarations" "Schema output validator" "Auth provider grants" "Workflow replay/audit mode"]}
   "nondeterminism-recording" {:row 3 :criterion "For each admitted runtime action whose replay or audit policy requires recording, persist typed input/output digests, provider/action identity, error/retry decisions, timestamp source and source/generated origin; record scheduler decisions where replay depends on them. Emit actual record-mode traces and matching replay consumption, rejecting absent/mismatched identity/order/cardinality and silent live reexecution in recorded-output mode. Record-mode action and replay-mode consumption are conditional alternatives, not simultaneous mandatory effects; retain R6's authorized graph-declared repetition exception outside R7/R8 recorded-output obligations." :dependencies ["Workflow replay/record schema and mode" "Auth action grants" "Stdlib meta/host event providers"]}
   "policy-safe-observation" {:row 4 :criterion "Emit typed runtime log/trace/metric/audit and diagnostic bundles with stable event schema/ID, timestamp source, family/profile/target/artifact/provider identity, source/generated origins and relevant effect/capability/check/proof/replay links. Enforce sink/deployment grants and the full secret/credential/prompt/model/tainted/raw-memory/environment/package/database redaction policy, reporting redactions without values. Exercise allowed local logging and denied network telemetry, required audit-event retention under sampling and semantic/order/replay/safety equivalence with instrumentation; reject ungranted sinks, policy-forbidden disclosure and missing mandatory events. Mark development-only observability explicitly." :dependencies ["Auth sink and redaction policy" "Diag event/remediation schema" "Emit source/artifact links" "Stdlib logging/metrics sink adapters"]}
   "replay-compatible-upgrade" {:row 3 :criterion "For a code/topology/schema or workflow-version upgrade, compare existing event histories, persisted state, actor snapshots and message versions against the new declared compatibility/migration/replay policy. Emit migration and replay evidence that existing histories remain usable; reject incompatible upgrades without such policy/evidence, schemaless state/messages and implicit rewriting of recorded external outcomes. Preserve idempotency, retry/cancellation/compensation and authority semantics across the admitted upgrade." :dependencies ["Schema compatibility/migration output" "Workflow versioned graph" "Build code/topology identity" "Runtime event-log provider"]}
   "service-manifest" {:row 0 :needles ["linked/generated/delegated/external/forbidden service records" "runtime-none entry/reset/interrupt/ABI, sections, memory map, alignment and required target stack evidence" "selected host/version/module/collection/dynamic-state manifests"] :extra-row 1 :reason "Exact current first-task clauses classify all service categories and freestanding fields; the managed/native producer names host/version/module/collection/dynamic-state manifests. Existing manifest-to-service consumer closure is retained, not inferred from a runtime label."}
   "service-policy-enforcement" {:row 4 :needles ["every authority-bearing action carries principal/package/deployment/artifact/source/effect/capability identity" "deny by default and check both caller grant and tool/plugin contract" "use-after-revocation rejection" "enforce deployment narrowing"] :reason "Exact current actual-service-hook criterion requires source-bound identity, default denial, dual caller/callee grants, provider-bound scoped handles, revocation, redacted audit and deployment narrowing. This is the runtime consumer of accepted authority, not a grant producer or replacement for static legality."}
   "startup-cleanup-order" {:row 1 :needles ["Executed startup/failure/resource fixtures and manifest-to-service agreement" "before user code, validate selected provider existence" "startup, initialization, handoff and teardown order" "explicit panic/trap/abort/unwind/result policy"] :reason "The current startup task names actual ordered startup/handoff/teardown and failure fixtures, retaining L15 provider/fallback validation and selected target/ABI inputs; no platform-default ordering is inferred."}
   "task-lifecycle" {:row 2 :adjudication "The same four named finite18 facets as durable failure recovery are missing from the immutable facet index. Keep parent-or-lifecycle-owner, captures/transfers, scheduler/cancellation/failure/cleanup and detached-task obligations Open. Recover exact source records before full attached-facet entailment; this is not authority to drop a task or replace semantic cancellation with a project attempt limit."}
   "tool-provider-execution" {:row 3 :criterion "Invoke the declared tool/provider with input/output schemas and validation, effect/side-effect class, caller grant intersected with the tool's own contract, timeout/retry/error mapping and required review/secret/taint policy. Emit actual invocation and policy/review decision records with replay identity; reject ungranted or out-of-contract effects, missing required review, invalid/tainted sink values, secret-policy violations and live calls in recorded-output segments. Prompt text, retrieved data and review records cannot create capability authority; preserve explicitly permitted alternatives for audit-only/interactive contexts." :dependencies ["AI typed tool/policy declarations" "Auth caller/tool grant intersection" "Schema validation" "Workflow record/replay and review artifacts"]}
   "typed-host-delegation" {:row 1 :needles ["typed host adapters with effects, capabilities, taint, error and source-map contracts" "host null/undefined conversion, exception and rejected-promise translation" "host-object/native FFI records bind runtime/version/symbol"] :extra-row 2 :reason "Current managed/native typed-adapter clause plus exact host-object/native FFI metadata binds host representation, errors/nulls, taint, grants and source mapping; opaque provider-only fallback and lower-profile isolation remain explicit. Stdlib adapter APIs supply declared contracts, not host-defined portable semantics."}
   "typed-message-delivery" {:row 2 :criterion "Deliver channel/actor messages under declared schemas/versions, ownership transfers, ordering/delivery guarantees, mailbox/backpressure, cancellation and failure policies for the selected scheduler/transport. Emit actual transfer and receive traces with source/context/authority and schema validation; accept isolated/owned messages and reject schemaless/mismatched messages, illegal shared mutation, invalid transfer/lifetime and unsupported blocking/ordering behavior. Durable transports retain event-log/idempotency/compensation and migration/replay records; no hidden host scheduler or ambient network grant is implied." :dependencies ["Schema message/state validators" "Ownership transfer proof" "Concurrency and durable transport providers" "Auth effects/grants"]}
   "boundary-validation" {:row 3 :criterion "At each external host/config/message/state/query/model/tool/memory/public-API boundary invoke the validator tied to the intended source schema/version and concrete trusted sink, preserving trust/taint, defaults/normalization, errors and origin. Emit validation and sink-admission outputs for valid values and reject wrong schema/version, invalid/missing fields or refinements, unsafe null/foreign representations, unvalidated taint and missing sink grants. Structural validation alone cannot authorize execution, deployment, mutation or secret disclosure; schema/compatibility/config-reload producers and runtime sink consumers remain separate. Preserve typed failure paths and secret-redacted diagnostics rather than host exceptions or silent coercion." :dependencies ["Schema source validator/default/refinement and version contract" "Schema config/evolution/mapping producers" "Auth sink policy" "Domains/Stdlib actual boundary adapters"]}})
(defn row-block [text id]
  (let [start (.indexOf ^String text (str "- [ ] `" id "` "))
        _ (f/require! (<= 0 start) "Missing literal task ID")
        tail (subs text start) end (or (str/index-of tail "\n- [ ] `") (str/index-of tail "\n<!--"))]
    {:start start :raw (subs tail 0 end)}))
(defn witness [text row needle]
  (let [block (row-block text (:id row)) i (str/index-of (:raw block) needle)]
    (f/require! i (str "Missing exact criterion: " needle))
    {:path tasks/task-path :id (:id row) :marker "runtime-implementation-tasks" :source base
     :file-sha256 (f/sha256 text) :record-sha256 (f/sha256 (:raw block))
     :start-byte (count (.getBytes (subs text 0 (+ (:start block) i)) "UTF-8"))
     :end-byte (count (.getBytes (subs text 0 (+ (:start block) i (count needle))) "UTF-8"))
     :raw needle :raw-sha256 (f/sha256 needle) :status :open}))
(defn apply-fields [data ops]
  (reduce (fn [data op]
            (f/require! (and (= :remaining-decisions (:collection op)) (= [:criterion-task-allocation] (:field-path op))) "Wrong receiver field")
            (let [indices (keep-indexed #(when (= (:id op) (:id %2)) %1) (:remaining-decisions data))]
              (f/require! (= 1 (count indices)) "Missing/duplicate decision ID")
              (let [path [:remaining-decisions (first indices)] row (get-in data path) current (:criterion-task-allocation row)]
                (f/require! (= (:record-sha256 op) (f/sha256 (pr-str row))) "Stale record/context")
                (f/require! (and (= (:before-present? op) (contains? row :criterion-task-allocation)) (= (:before op) current)) "Stale/duplicate field")
                (assoc-in data (conj path :criterion-task-allocation) (:after op))))) data ops))
(defn apply-task-edits [text edits]
  (reduce (fn [text e]
            (f/require! (and (= :canonical-tasks (:collection e)) (= tasks/task-path (:path e))
                            (= "runtime-implementation-tasks" (:marker e)) (= [:acceptance] (:field-path e)) (:before-present? e)) "Wrong task receiver")
            (let [row (first (filter #(= (:id e) (:id %)) (tasks/records text))) block (row-block text (:id e))]
              (f/require! (and row (= (:record-sha256 e) (f/sha256 (:raw block))) (= (:before e) (:acceptance row))
                              (str/starts-with? (:after e) (str (:before e) " "))) "Stale/duplicate or weakened criterion")
              (str/replace text (str "Closing evidence: " (:before e)) (str "Closing evidence: " (:after e))))) text edits))
(defn build [partition-path]
  (f/require! (= partition-sha (f/file-sha256 partition-path)) "Wrong dispatch partition")
  (let [partition (f/read-edn partition-path) assigned (filterv #(= "phase-08" (:primary-dispatch-family %)) (:records partition))
        ds-text (f/git-source base decision-path) ds (edn/read-string ds-text) by-id (into {} (map (juxt :id identity) (:remaining-decisions ds)))
        task-text (f/git-source base tasks/task-path) rows (tasks/records task-text)
        frozen (edn/read-string (f/git-source "53ae6a403b20d15155bb5b04911fe7ac0092b921" "specs/requirement-reconciliation/source-task-inputs.edn"))
        docs (into {} (map (juxt :id identity) (:original-documents frozen)))
        _ (f/require! (= 20 (count assigned) (count definitions)) "Assigned population mismatch")
        _ (f/require! (= (set (map :semantic-source-id assigned)) (set (map #(str (if (= % "boundary-validation") "schema/" "runtime/") %) (keys definitions)))) "Foreign or missing definition")
        sources (into {} (for [id (set (keep :document-id (mapcat :source-facet-metadata assigned)))
                              :let [d (docs id) text (f/git-source base (:path d))]]
                          (do (f/require! (= (:current-sha256 d) (f/sha256 text)) "Source document drift") [id text])))
        mappings
        (mapv (fn [r]
                (let [d (definitions (last (str/split (:semantic-source-id r) #"/"))) row (by-id (:id r)) task (nth rows (:row d))
                      _ (f/require! (= (get-in r [:guard :predecessor-record-sha256]) (f/sha256 (pr-str row))) "Later decision predecessor changed")
                      _ (f/require! (not (contains? row :criterion-task-allocation)) "Completed duplicate allocation")
                      evidence (mapv (fn [m] (if (:missing-source-metadata m) m
                                               (let [s (sources (:document-id m)) b (.getBytes ^String s "UTF-8") raw (String. b (int (:start-byte m)) (int (- (:end-byte m) (:start-byte m))) "UTF-8")]
                                                 (f/require! (= (:raw-sha256 m) (f/sha256 raw)) "Raw facet bytes differ")
                                                 (assoc m :path (:path (docs (:document-id m))) :raw raw)))) (:source-facet-metadata r))
                      needles (:needles d)
                      witnesses (mapv (fn [needle]
                                        (if (str/includes? (:raw (row-block task-text (:id task))) needle)
                                          (witness task-text task needle)
                                          (witness task-text (nth rows (:extra-row d)) needle))) needles)]
                  {:id (:id r) :semantic-source-id (:semantic-source-id r) :required-output (:required-output r)
                   :dispatch-record r :before row :source-evidence evidence :unresolved-facet-metadata-ids (:unresolved-facet-metadata-ids r)
                   :task {:path tasks/task-path :id (:id task) :marker "runtime-implementation-tasks" :status :open
                          :role :runtime-provider-or-actual-boundary-consumer :owner-role :runtime-maintainer}
                   :disposition (cond (:adjudication d) :source-metadata-adjudication-required (:criterion d) :proposed-missing-criterion :else :proposed-exact-current-criterion-witness)
                   :witnesses witnesses :entailment (:reason d) :criterion-proposal (:criterion d) :source-adjudication (:adjudication d)
                   :outputs (get criterion-outputs (last (str/split (:semantic-source-id r) #"/")))
                   :dependencies (or (:dependencies d) (get witness-dependencies (last (str/split (:semantic-source-id r) #"/")))
                                     ["Canonical source/facet owner supplies exact immutable document/occurrence/byte/hash records" "Existing Runtime criterion is retained pending source-qualified review"])
                   :dependency-plan {:path "specs/runtime-workflow/plan.md" :source base :sha256 (f/sha256 (f/git-source base "specs/runtime-workflow/plan.md"))}
                   :implementation-acceptance :unassessed :canonical-adoption :pending-independent-review
                   :scope-limit "Allocates only the exact named output with its attached source qualifications. Shared facets/contexts do not transfer another primary owner's decision or establish full source-family implementation acceptance. Missing metadata cannot be counted proved."})) assigned)
        task-edits (mapv (fn [[id ms]]
                          (let [row (first (filter #(= id (:id %)) rows)) suffix (apply str (for [m ms] (str " Decision criterion " (:id m) ": " (:criterion-proposal m))))]
                            {:collection :canonical-tasks :id id :path tasks/task-path :marker "runtime-implementation-tasks"
                             :field-path [:acceptance] :before-present? true :before (:acceptance row) :after (str (:acceptance row) suffix)
                             :record-sha256 (f/sha256 (:raw (row-block task-text id)))}))
                        (sort-by key (group-by #(get-in % [:task :id]) (filter :criterion-proposal mappings))))
        ops (mapv (fn [m] {:collection :remaining-decisions :id (:id m) :field-path [:criterion-task-allocation]
                           :record-sha256 (f/sha256 (pr-str (:before m))) :before-present? false :before nil
                           :after (dissoc m :before :dispatch-record)}) mappings)]
    (let [after (apply-fields ds ops)]
      (f/require! (= 833 (count (:remaining-decisions ds))) "Decision population drift")
      (f/require! (and (every? #(= :open (:status %)) (:remaining-decisions ds))
                       (= (mapv (juxt :id :status) (:remaining-decisions ds)) (mapv (juxt :id :status) (:remaining-decisions after)))) "Decision states changed"))
    (apply-task-edits task-text task-edits)
    {:kind :runtime-current-decision-allocation-proposal :family "phase-08" :base base
     :partition {:sha256 partition-sha :source (:source partition)} :source-decision-file-sha256 (f/sha256 ds-text)
     :task-source {:path tasks/task-path :sha256 (f/sha256 task-text)} :mappings mappings :decision-field-edits ops :task-criterion-edits task-edits
     :source-documents (mapv docs (sort (keys sources)))
     :accepted430-input {:path "specs/requirement-reconciliation/accepted-historical-mappings-430.edn" :source base
                         :sha256 (f/sha256 (f/git-source base "specs/requirement-reconciliation/accepted-historical-mappings-430.edn")) :operation :preserve-unchanged}
     :preservation {:decisions 833 :all-statuses :open :accepted-historical-mappings 430 :accepted-change 0 :new-tasks 0
                    :previous-runtime-family-proposal "a6b2ee0fd88a46d20da8d70afa182d54c4ad6b69b86dbb7959adc1edb4214a3f"
                    :previous-runtime-residual-proposal "db594d1dc7c3b2d0f23c09a30f99b16aba71a69834c947b6ff6309110dd473ac"}
     :counts {:assigned 20 :proposed-existing-criterion-witnesses 6 :proposed-missing-criteria 11 :source-metadata-adjudications 3 :new-tasks 0 :accepted-allocations 0}
     :unresolved-ids (mapv :id (filter :source-adjudication mappings))
     :application "Central owner applies exact current-record and criterion guards, preserves later text, regenerates derived views and independently reviews/adopts. Never overwrite whole files or count these decisions in addition to historical joins. The three missing-metadata rows remain unproved; no facet ID/span is invented."}))
(defn validate [p expected] (f/require! (= p expected) "Forged/stale allocation, role, dependency or coverage") true)
(defn run [args]
  (f/require! (and (= 2 (count args)) (#{"--write" "--check"} (first args))) "Usage: --write|--check PARTITION")
  (let [p (build (second args))]
    (if (= "--write" (first args)) (spit output (with-out-str (pp/pprint p))) (validate (f/read-edn output) p))
    (prn (:counts p))))
(when (seq *command-line-args*) (run *command-line-args*))
