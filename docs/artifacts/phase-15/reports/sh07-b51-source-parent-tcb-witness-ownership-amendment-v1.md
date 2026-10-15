# SH-07 SOURCE parent TCB witness and ownership amendment V1

## Purpose and decision

Four parent checks must independently validate the complete canonical classpath
and seven dependency materializations. The existing raw preload authenticates
source residence before helper loading; it does not supply those canonical
witnesses. The ordinary checker retains its ordinary reader law, while the child
SOURCE checker authenticates an owner confined to the actual child process.
Neither route supplies the required parent witness.

This amendment defines exactly two fixed parent operations and a separate
parent-owned resource lifetime. It supersedes only the resource amendment's
sentence that the already allocated parent has no new public operation, and
conflicting wording that would restrict the shared SOURCE kernel to the child
owner. The five existing child SOURCE boundaries and their fixed rejection-only
validator retain their existing signatures and seals.

This is architecture. Implementation, exact resource and line fit, ordinary
behavior, runtime retirement and SOURCE/native/consumer evidence require their
own completed gates. The same SOURCE owner remains
01a13b8d-509d-7771-b618-06a24933391b. Coordinator alone integrates eligible
candidates and authorizes exact finite executions.

```json gravity-architecture-authority-v1
{"authority":{"integration_only":true,"release":false,"seed_retirement":false,"self_hosting":false},"base_commit":"65e1794aa64b7fb567e46a5cb08090cc1cddd667","dependencies":[{"id":"sh07-b51-nonadmitting-source-evidence-entrypoint-architecture-v1","required_state":"integrated"},{"id":"sh07-b51-source-resource-lifetime-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-identity-lexical-closure-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-to-fresh-sh06-consumer-compatibility-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-session-handoff-closure-v1","required_state":"integrated"},{"id":"sh07-b51-retained-origin-pure-reader-architecture-v1","required_state":"integrated"},{"id":"sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2","required_state":"integrated"},{"id":"c6-source-core-mapping-prerequisite-v1","required_state":"integrated"}],"historical_references":[],"invariant_family":"architecture/self-hosting-sh07-b51-nonadmitting-source-evidence-entrypoint-v1","report_path":"docs/artifacts/phase-15/reports/sh07-b51-source-parent-tcb-witness-ownership-amendment-v1.md","schema":"gravity/architecture-authority-v1","status":"draft","workstream_id":"sh07-b51-source-parent-tcb-witness-ownership-amendment-v1"}
```

## Dependencies and allocation

Accepted registration base is `65e1794aa64b7fb567e46a5cb08090cc1cddd667`, tree `bb0777dd8cfe994ba4f644fc400a96d23be4ca0f`. The eight integrated dependencies in the authority block are required. This report is the immutable pre-freeze draft tuple; ledger history records later lifecycle changes.

The entrypoint architecture, SOURCE resource lifetime and identity lexical
closure amendments, SOURCE-to-fresh-SH06 compatibility amendment, session handoff,
retained-origin pure reader, V9 authority-context erratum V2 and C6 mapping
prerequisite must all be integrated before this implementation slice is adopted.
D0-D3, D6, D8, D9 and the governing L1/L2, C2/C5/C6, BOOT, TEST, R6 and GOV
contracts remain binding. Existing ordinary parser, diagnostic and output laws
remain binding.

Only these three existing production resources gain the parent seam:

- `bootstrap/clojure/src/gravity/tools/sh07_b51_source_product_evidence.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child/03-git-operation-core.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child/04-validate-complete-classpath.clj`

They remain inside the existing twenty-three-path implementation allocation.
There is no new production file, fragment, manifest row or data resource. The
118-resource census, original fifteen child early TCB rows, seven logical
algorithms, load-order manifests and all existing wire grammars remain binding.
The already allocated dedicated test and fixture paths may cover this seam.
Every production file must fit within 250 physical lines in the complete final
implementation. The final code must prove that fit; this design does not.

## Exact callable boundary and nominal owner

The parent namespace is `gravity.tools.sh07-b51-source-product-evidence`.
Its new fixed operation is
`authenticate-source-parent-resource-lifetime! [candidate]`, returning nil or
throwing the stable SOURCE lifetime diagnostic. One lexical state atom and one
opaque owner are created inertly in that namespace. The validator first compares
`identical?` with the captured owner, before candidate data, method, hashCode,
equals, reflection or class access. It then checks the actual parent process,
main Thread, fixed process coordinates, active phase, deadline, failure and
interruption. Nil, maps, copied data, child owners, interface imposters, foreign
process/thread owners, stale and retired owners reject before candidate access.
No public operation returns an owner or its state.

The nominal binary interface is
`gravity.tools.sh07_b51_source_product_evidence.ParentTcbResourceLifetime`:

```clojure
(definterface ParentTcbResourceLifetime (^void checkpoint []) (^Object reserve [^clojure.lang.Keyword unit ^long maximum]) (^Object git [^clojure.lang.Keyword operation ^clojure.lang.IPersistentVector arguments ^long stdoutMaximum]) (^Object readSource [^java.nio.file.Path path ^long maximum]) (^Object register [^clojure.lang.Keyword kind ^Object resource]) (^void retire [^Object ticket]) (^void close []))
```

Its exact JVM descriptors are:

- `checkpoint()V`
- `reserve(Lclojure/lang/Keyword;J)Ljava/lang/Object;`
- `git(Lclojure/lang/Keyword;Lclojure/lang/IPersistentVector;J)Ljava/lang/Object;`
- `readSource(Ljava/nio/file/Path;J)Ljava/lang/Object;`
- `register(Lclojure/lang/Keyword;Ljava/lang/Object;)Ljava/lang/Object;`
- `retire(Ljava/lang/Object;)V`
- `close()V`

Parent and child interfaces are distinct and not assignable to one another.
Parent raw code may type-hint only the parent interface; child raw code may
hint only the child interface. Every shared checker03/04 helper receives the
owner as Object, without either nominal interface cast, hint, instance test or
require. Fixed literal calls `.checkpoint`, `.reserve`, `.git`, `.readSource`,
`.register`, `.retire` and `.close`, with numeric arguments explicitly cast to
long, may use the pinned Clojure compiler's ordinary Reflector invocation.
That invocation occurs only after the corresponding fixed public facade has
authenticated its captured owner. Class, method-name DATA, duck typing,
getMethods/getDeclaredMethods, Method.invoke, callbacks, private Var lookup and
runtime role selection confer no authority.

The parent interface, reify and validator are inert definitions, generated only
by the authenticated parent source through the pinned ordinary Clojure
DynamicClassLoader. No AOT, custom loader, physical classfile or extra resource
is permitted. Parent compilation/loading does not require or evaluate the child
namespace. Generated reify names are not semantic identities or authority.
Final implementation evidence must bind actual class, loader, public Var and
source residence before helper use.

## Fixed canonical witness

The checker namespace `gravity.tools.sh07-b51-attempt32-checker-child` adds
`validate-complete-classpath-for-source-parent! [parent-resource-lifetime]`.
It accepts only the opaque owner. It takes no caller options, codec, method,
path, mode, callback or authority flag. Its first action is the literal
already-loaded parent's fixed public validator, including the captured identity
seal. Its private zero-argument process-options function then derives actual
options anew; its existing `load-codec!` supplies the original bound public
H/W/R Vars and immutable transport API only after raw codec authentication.

One private `validate-complete-classpath-core! [options codec source? owner]`
serves the original ordinary false/nil branch, existing sealed child SOURCE
branch and new sealed parent SOURCE branch. It preserves the original semantic
and lexical kernels and ordinary diagnostics. No parser body is copied and no
public mode or generic factory is added.

The parent facade returns exactly:

```clojure
{:classpath {:entries E :manifest-rows M :manifest-root R}
 :dependency-materializations D}
```

Each check privately retains the full existing five-key SOURCE result while
validating complete C source authority and every JAR source. JAR rows retain all
six keys `:jar-ordinal :resource :byte-count :source-sha256 :jar-sha256 :bytes`.
Raw bytes, count, hash, JAR pin, ordinal and unique resource residence must join
before deriving external records. Reduced records or child assertions cannot
replace those observations. Raw authority stays private through that check;
each later check reacquires and validates it independently.

## Actual process predicate and method ordering

The parent role is the existing literal `:source-product-launcher`, never
`:source-product-staging`, ordinary checker or M. The child retains its separate
existing source role. The parent records actual main Thread, PID and monotonic
start before the first host probe, then observes process coordinates before
entering a public witness phase. It cannot claim observations before making them.

The following closed actual-process fields and joins are normative:

```json
{
  "source_of_fields": "Direct actual ProcessHandle.current/info/parent + originalRuntimeMXBean law + System.getProperties/getenv, pinnedliteralJava/Git path checks. Capturedbeforefirsthostprobe: actualmainThread,selfPID,monotonicstart/Dp/Wp; then boundedprobesfillimmutableprocesscoordinates beforepublicwitnessphase. Do not claimhostdata existed beforeobservingit.",
  "process_keys": [
    ":role",
    ":pid",
    ":parent-pid",
    ":command",
    ":arguments",
    ":start-instant"
  ],
  "runtime_keys": [
    ":name",
    ":pid",
    ":host",
    ":start-time-millis",
    ":vm-name",
    ":vm-vendor",
    ":vm-version",
    ":java-version",
    ":java-vendor",
    ":java-class-path"
  ],
  "argv": "Actualinfo.arguments exactly24strings; fullJavaargv exactly25strings = [actualpinnedJava] + args. Fixed10CLIstrings/flags --snapshot-commit C --snapshot-tree T --authorization-commit Qs --authorization-tree TQs --output-root OUT, eachC/T/Qs/TQs40hex; exact4CP/-Xms64m/-Xmx4g/UTF8/locale/timezone/headless/reportstderr/home/nonexistent/main sourceparent. Comparepassed-mainargswith*command-line-args* beforework.",
  "joins": "RuntimePID=start/process/CP actualsameparent; actualparentPID/start/command/args identifyretainedpinnedsupervisor independently ofchildresponse. ExactJava=/actualjava.home/bin/java,Git=/usr/bin/git; no symlink/relativealiases. Closed7env exactfullmap/no normalization/extras; CWDsameactualcanonicalfrozenCROOT;4distinctactualCP paths+exact3logicalJARversions; fullCPrawpins externallyauthenticatedbeforelaunch and privatelyreobservedbeforehelperload, canonicalequalities later. Recheckallimmutableprocess/env/CWD/CP/argv fields atpublicvalidatorentry, actualdeadline/thread/failurebeforeeveryowneroperation."
}
```

The original architecture's closed source-only Qs/M/review/grant schemas,
self-id omissions, sorted pin union, one-attempt rule and H/W/R joins remain
exact. No method or grant is supplied to the new facade. The selected Qs object
must be the direct child of C, with unchanged complete C map and exactly one
100644 addition. The five-operation selection is stricter construction for this
method, not a claim that all Qs commits universally have this shape.

1. No M or methoddescriptor is passedto parent facade orvalidator. Parentvalidator first provesactualsealedparent withcaptured C/T/Qs/TQs flags and source-only phase, nothumanpermission ormethodacceptance.

2. After16rawrows/3JARrawbookends and permittedhelper/codec load, parent fixedwitness privately derives actualoptions/codec and runs existingprivate selectedQs fiveoperationcheck (sharedwithchildSOURCEwrapper AFTERdifferentfixedseal), validatescanonicalQscontent/proposal/method/reviewdescriptor grammar/H/oneattempt/names/sourceonlyauthority. This is methodELIGIBILITY, notGO. Parentfacade returnsONLYCP/deps; no methodgetter/extraresultkey.

3. Parent private-main method gate independently derives selectedQs/Mdescriptor fromsameactualimmutableQs using existingparentrawGit/UTF8 machinery factoredforcompleteC/Qsingletonlookup plus ORIGINALcodecstrictcanonicalreader/H/W/R. It authenticatesexternalMactualbytes/R/completeclosedM/corepins, exactQs augmentedpinunion and separatelyfrozenreviewcoordinates asrequiredbyexistingarchitecture; originalprelaunchcoordinator authenticatesR1/R2decisionbytes/directhumanG andactualsupervisor. No semantic/classpath parser is copied; exactprotocolbytejoins followexistingclosedQsgrammar. All extramethodGit/pinreads explicitlycountedbyfinalmethod; 4N+374isTCBwitnesssubtotal only.

4. Storemethodselection/Mdescriptor/Qscoordinate privatelyonlyAFTERactualobject/bytejoins. Parentverifies actualsupervisorProcessHandlecommand/args/start/sourcepin/java and Msupervisordescriptor. NohumanGOinferredfromQs/marker/callerstring; trustedcoordinatorsupervisorgate unchanged. ChildlaunchblockeduntilcompleteM/pin/supervisorvalidationandinitial-beforeCP+7depscheck bothpassed.

5. Subsequentthreeparentwitnesses deriveandrechecksameactualC/T/Qs/TQs/methodselection, no differentoptions/codec permitted. No M/Q selfcycle: M excludesR/Q/G/output; R excludesown/peer/Q/G; Q unionincludesalreadyfrozenM/Rdescriptors; G/actualargv/output remainlater. No newamendmentgrantspermission orcallschildconstruction.

## Authentication and load order

The coordinator authenticates the complete actual parent classpath and
project-free literal load closure against immutable final C and accepted method
pins before parent launch. This trusted seed boundary remains explicit. The
parent independently authenticates raw source/JAR residence before helper or
codec load, then canonical metadata, then the original pure Git validators.
No later parser retroactively authenticates its already loaded source.

The parent raw route is exactly one show of C, one aggregate ls-tree for all
sixteen early paths, and sixteen blob reads: eighteen Git operations. It does
not call the child-only pre-manifest facade or silently use its per-row topology.
It also observes all three pinned JARs with full pre/post file state and raw hash
before helper/codec loading. Raw child entry bytes are authenticated but its
namespace is not required or evaluated in the parent.

Early resource order is exact. Resource paths below are relative to the existing
`bootstrap/clojure/src/` directory:

| Ordinal | Resource | Namespace owner |
| --- | --- | --- |
| 0 | `gravity/tools/sh07_b51_source_product_evidence.clj` | `gravity.tools.sh07-b51-source-product-evidence` |
| 1 | `gravity/tools/sh07_b51_source_product_evidence_child.clj` | `gravity.tools.sh07-b51-source-product-evidence-child` |
| 2 | `gravity/tools/sh07_b51_attempt32_checker_child.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 3 | `gravity/tools/sh07_b51_attempt32_checker_child/01-transport-limits.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 4 | `gravity/tools/sh07_b51_attempt32_checker_child/02-source-event-plan.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 5 | `gravity/tools/sh07_b51_attempt32_checker_child/03-git-operation-core.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 6 | `gravity/tools/sh07_b51_attempt32_checker_child/04-validate-complete-classpath.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 7 | `gravity/tools/sh07_b51_attempt32_checker_child/05-validate-source-read-receipt.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 8 | `gravity/tools/sh07_b51_attempt32_checker_child/06-v9-checker-output-shell.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 9 | `gravity/tools/sh07_b51_attempt32_checker_child/07-build-response.clj` | `gravity.tools.sh07-b51-attempt32-checker-child` |
| 10 | `gravity/c2_artifact_identity/canonical.clj` | `gravity.c2-artifact-identity.canonical` |
| 11 | `gravity/digest.clj` | `gravity.digest` |
| 12 | `gravity/tools/sh07_b51_v9_codec.clj` | `gravity.tools.sh07-b51-v9-codec` |
| 13 | `gravity/tools/sh07_b51_v9_codec/01-transport-limits.clj` | `gravity.tools.sh07-b51-v9-codec` |
| 14 | `gravity/tools/sh07_b51_v9_codec/02-read-bounded-bytes.clj` | `gravity.tools.sh07-b51-v9-codec` |
| 15 | `gravity/tools/sh07_b51_v9_codec/03-unsigned-wire-sort.clj` | `gravity.tools.sh07-b51-v9-codec` |

After the first full canonical classpath check, authenticate every existing
Git-evidence resource below against complete fresh C source authority,
materialized bytes, unique residence, exact header aliases and literal facade
load order before requiring that namespace. It uses only its existing codec
project dependency, already authenticated and loaded. All transitive external
namespace sources come from the exact pinned JAR inventories. No host fallback,
namespace autocreation, extra source/AOT provider or shadow resource is allowed.

| Ordinal | Resource | Namespace owner |
| --- | --- | --- |
| 0 | `gravity/tools/sh07_b51_git_evidence.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 1 | `gravity/tools/sh07_b51_git_evidence/00-owned-source-fragments.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 2 | `gravity/tools/sh07_b51_git_evidence/01-git-executable.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 3 | `gravity/tools/sh07_b51_git_evidence/02-row-plan-prefix.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 4 | `gravity/tools/sh07_b51_git_evidence/03-row-plan-suffix.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 5 | `gravity/tools/sh07_b51_git_evidence/04-predicate-spec-vector.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 6 | `gravity/tools/sh07_b51_git_evidence/05-evaluate-predicate-node.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 7 | `gravity/tools/sh07_b51_git_evidence/06-exact-keys.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 8 | `gravity/tools/sh07_b51_git_evidence/07-run-bounded-process.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 9 | `gravity/tools/sh07_b51_git_evidence/08-cat-file-operation.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 10 | `gravity/tools/sh07_b51_git_evidence/09-validate-source-object-proof.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 11 | `gravity/tools/sh07_b51_git_evidence/10-build-source-descriptors.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 12 | `gravity/tools/sh07_b51_git_evidence/11-build-a9-stage-result-e1.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 13 | `gravity/tools/sh07_b51_git_evidence/12-mask-span-for-rhs.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 14 | `gravity/tools/sh07_b51_git_evidence/13-build-normalized-mask-files.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 15 | `gravity/tools/sh07_b51_git_evidence/14-fenced-edn-after.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 16 | `gravity/tools/sh07_b51_git_evidence/15-reconstruct-pre-object-snapshot.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 17 | `gravity/tools/sh07_b51_git_evidence/16-validate-pre-discharge-envelope.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 18 | `gravity/tools/sh07_b51_git_evidence/17-validate-git-observation.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 19 | `gravity/tools/sh07_b51_git_evidence/18-build-evidence-eligibility.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 20 | `gravity/tools/sh07_b51_git_evidence/19-validate-runtime-lifecycle-ref.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 21 | `gravity/tools/sh07_b51_git_evidence/20-validate-eligibility-object.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 22 | `gravity/tools/sh07_b51_git_evidence/21-list-entry-names.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 23 | `gravity/tools/sh07_b51_git_evidence/22-validate-runtime-identity-receipt.clj` | `gravity.tools.sh07-b51-git-evidence` |
| 24 | `gravity/tools/sh07_b51_git_evidence/23-cleanup-unpublished.clj` | `gravity.tools.sh07-b51-git-evidence` |

The namespace dependency and alias closure is exact:

```json
{
  "namespace_dependencies": {
    "parent": [
      "clojure.string"
    ],
    "checker-child": [
      "clojure.edn",
      "clojure.string"
    ],
    "codec": [
      "gravity.c2-artifact-identity.canonical",
      "gravity.digest"
    ],
    "git-evidence": [
      "clojure.edn",
      "clojure.string",
      "gravity.tools.sh07-b51-v9-codec"
    ]
  },
  "source_aliases": {
    "checker-child": {
      "edn": "clojure.edn",
      "str": "clojure.string"
    },
    "git-evidence": {
      "edn": "clojure.edn",
      "str": "clojure.string",
      "codec": "gravity.tools.sh07-b51-v9-codec"
    }
  }
}
```

All listed fragments remain in their facade namespace. Canonical, digest and
codec resources must be raw-authenticated before the original codec loads;
all seven checker fragments must be raw-authenticated before its facade loads.
The final method must enumerate the complete final external namespace closure,
source providers, literal load vector and generated class provenance.

## Seven complete dependency proofs

All seven rows are freshly acquired for each parent check, in this exact order:

1. `bootstrap/gravity/src/gravity/bootstrap/reader.gravity`
2. `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity`
3. `bootstrap/gravity/src/gravity/macro.gravity`
4. `bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity`
5. `bootstrap/gravity/p15_s23/compiler.gravity`
6. `bootstrap/gravity/p15_s23/emitter.gravity`
7. `bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity`

The private checker04 helper receives only internally derived options, codec,
sealed owner and authenticated classpath. It acquires raw commit C once per
witness and uses only the original public pure validators
`gravity.tools.sh07-b51-git-evidence/validate-content-object-proof! [content proof]`
and `validate-source-object-proof! [content descriptor proof]`. Resolve exactly
those already authenticated bound public Vars by literal names. Requiring the
namespace does not authorize its acquisition, eligibility or orchestration APIs.
The builders, cat-file-object-bytes!, run-bounded-process!, eligibility validator
and coordinator/checker orchestration are excluded.

A content proof has exactly `:oid :raw-bytes`. It retains the complete unsigned
commit bytes; the original validator checks the commit OID and tree T. For each
fixed dependency path, obtain the selected prefix row using the original
checker03 ls-tree parser and the entire current tree payload through owner.git.
Each chain row has exactly `:tree-oid :tree-bytes :component :selected-mode
:selected-oid`. Normalize only ls-tree directory mode 040000 to proof mode 40000;
leaf mode is 100644. The next tree OID is the current selected OID. Retain every
unsigned tree byte and final blob byte, in original path-component order.

A source proof has exactly `:schema :path :root-tree :tree-chain :blob-bytes`,
with schema `:gravity/sh07-b51-git-tree-inclusion-proof-v1`. The original pure
validator independently checks full binary tree bytes/order/OIDs/modes,
components and complete leaf blob OID/count/raw hash against C/T/descriptor.
There is no new binary tree parser or selected-row summary proof. Reserve all
raw-to-unsigned conversion, tree parser, proof node and hash work before it runs.
Per-tree and dependency blob bounds are 1 MiB, path depth is at most 512, and
aggregate proof bytes are at most 32 MiB within the original stricter limits.

Each materialization retains the original complete six-field descriptor and
exact row keys `:ordinal :path :descriptor :source-object-proof
:materialized-byte-count :materialized-raw-sha256`. Dense ordinal, full proof,
C/T/path/mode/OID/count/raw hash and physical bytes must all join. Reserve path
and attribute work before probing. Require NOFOLLOW, full component containment,
regular nonlink file, file key, size and times before and after the bounded read,
complete physical byte equality with the proved blob and original strict decode
where the governing dependency requires it. Source-root equality alone is not
a proof and child rows are not substituted.

## One parent resource lifetime

One lexical parent state and owner remain inert in NEW until exact -main
activation. They cover raw, load, canonical, method, child transport, all four
checks, encoding and retirement without refill. No owner crosses IPC or appears
in returned DATA, metadata, product hashes or protocol receipts.

The parent deadline is monotonic start before the first host probe plus
7,260,000 ms. Work deadline is that same deadline minus 10,000 ms; all normal
work, output and flush must finish before it. Each Git deadline is the earlier
of now plus 30,000 ms and work deadline. The final parent cleanup has a fixed
10,000 ms bound. The existing external supervisor retains its separate
7,380,000 ms deadline and OS containment. There is no child halt or Thread.stop.

Each child has a watcher deadline born before its actual Process.start, at most
3,600,000 ms later. The retained watcher starts before blocking start/wait.
ABORTING wins over a late zero exit. Complete means observed exit zero, bounded
drains, descendant/task retirement and actual watcher join before that deadline.
No second child or parent witness starts until the previous child boundary is
fully closed. Destroy/force waits retain their original 250/1000 ms bounds.

The following closed units, operations, phases, host types, tickets and failure
laws are normative. Representation counts never replace repeated work/byte
charges. Slot reservation occurs before acquisition; the actual host handle is
retained immediately before dependent work, including late-acquisition failure.

```json
{
  "units": {
    "metadata": 67108864,
    "git-operations": 65536,
    "owning-verifier-calls": 65536,
    "classpath-objects": 4096,
    "classpath-total-bytes": 268435456,
    "expanded-jar-total-bytes": 134217728,
    "jar-1-entries": 4096,
    "jar-2-entries": 4096,
    "jar-3-entries": 4096
  },
  "representation_rule": "Classpathobjects distinctauthenticatedCsrcpath/OID/R cardinality,perJARentryboundeachcompleteinventory. Repeatedsameinput enumeration/bytes/AST/hash/path/proofwork is chargedagain toglobalmetadata/bytes/Git/deadline; no representationcount asworkcredit. No semantic fivephysicalread/decode units are accepted byparent.",
  "resource_kinds": {
    ":input-stream": "java.io.InputStream",
    ":output-stream": "java.io.OutputStream",
    ":channel": "java.nio.channels.FileChannel",
    ":jar": "java.util.jar.JarFile",
    ":process": "java.lang.Process",
    ":task": "java.util.concurrent.FutureTask",
    ":worker": "java.lang.Thread"
  },
  "register_law": "Each resourcekind reserve exactly1 typedslotBEFOREfixedacquisition. Ownerretains expectedphase/mainThread/process/C/T/Qs/TQs/deadline/fixedoperation/path/parentresource/privatependingticket. Registerexacthostclass(actualinstance) immediatelyafteracquisitionBEFOREdependentread/wait/execute, no null/duplicate/foreignhandle/reusedticket. Actualhandle retainedforcleanup evenifpostacquisitiontime/identitycheckfails. FutureTaskretainedBEFOREexecute; actualRunnablelatch, notcancelstate, provesjoined. Drainworker checkassignedactualThread/stream/task/phase/deadline only. Retiredrows retainedaudit untilend.",
  "tickets": "Opaque freshObject identities; identityhashbucket oridentitymap equality only AFTERidentical? ownergate. No userhashCode/equals. Transitions RESERVED->LIVE->RETIRING->RETIRED orFAILED, neverreuse/refund/reset; unmatchedpendingafterfailure cannotbesuccess. BeforeandafterCASclock/failurechecks, firstThrowableirreversible.",
  "git_verb_maximum": {
    "show": 4096,
    "ls-tree": 1073741824,
    "cat-file-commit": 16777216,
    "cat-file-tree": 1048576,
    "cat-file-Csrc-blob": 16777216,
    "cat-file-seven-dependency-blob": 1048576,
    "cat-file-selected-Qs-blob": 4194304
  },
  "git_forms": [
    "show -s --format=%H%n%T%n%P C orQs ONLY",
    "ls-tree -r -z C -- bootstrap/clojure/src; whole ls-tree -r -z C -- orQs -- only forselectedQssingletonproof",
    "ls-tree -z C -- EXACToriginal16earlypaths aggregate; ls-tree -z C -- one locallyderivedprefix ofonefixedseven dependency paths; ls-tree -z Qs -- only soleaddedpathderivedfromcompleteC/Qmaps",
    "cat-file commit C ONLY; cat-file tree treeOIDselectedandretainedfromcapturedCroot/prefixchain; cat-file blob OID fromauthenticatedCsrcrow/fixeddependency leaf/selectedQssoleaddition ONLY"
  ],
  "git_owner_law": "Literal/usr/bin/git/fixedactualCWD/closed7env only. Operationkeywordmustbijectivelymatchactualargvfamily. No arbitraryexecutable/options/callback/env/workingdir/ref/branch/path; enforcevector/count/string/OID/pathboundsBEFOREProcessBuilder. Suppliedmaximum mayonlytighten owningfixedlimit, neverincrease; fixedprivatecalls andfinalmethodenumerateexactbounds. PerGitmin30s/Wp, stderr1MiB/concurrent2drains. C/Qwholemap recordscount/bytescharged beforeparse, not4096whole-treecap. Anynewfixedmethodoperation requiresseparateexactreview, notimplicitwildcard.",
  "readSource_forms": [
    "Duringrawphase: exact16earlyCsrcpaths and exact3pinnedJARpaths fromactual4CP; dirsourcefiles<=16MiB/JAR<=16MiB",
    "Duringcanonicalphase: normalizedabsolute Csrcfiles underactualCP0 ONLYwhenpath/OIDknownfromcompleteCsrcinventory; exact3JARpaths; exactsevenCWD-relativedependencyfiles ONLY"
  ],
  "readSource_bounds": "Class/JAR16MiB,semanticdependency1MiB,callermaxonlytightens. NOFOLLOW/allcomponentcontainment/filekey/size/fullprepost/hash/originalowningchecks. No externalmethodfile/arbitraryCWD path throughthisinterface. Existingprivateparentmethod/supervisor pinreads remainfixedoriginalarchitecture separateinternalIO slots/counters notnewpubliccapability.",
  "phases": [
    "NEW inert/nohostresources",
    "ARMED onceactual-main/process/clockbeforefirstprobe; fixedrawprocess/pathonly",
    "RAW fixed16rowGit/materialized/JARrawbookends underSAMEbudget",
    "LOAD_AUTHENTICATED rawclosureproven; fixedhelper/codec load",
    "INITIAL_BEFORE canonicalfixedparentfacade thenprivatecompleteM/Q/supervisor checks BEFOREchildlaunch",
    "INITIAL_CHILD oneactualchild+watcher+2drains; NO parentGit/CPworkoverlap",
    "INITIAL_AFTER onlyafteractualexit/drains/desc/task/watcherjoined",
    "REPLAY_BEFORE distinctfreshcanonicalwitness BEFOREsecondchildlaunch",
    "REPLAY_CHILD serialsamecontainment",
    "REPLAY_AFTER onlyafteractualexit/drains/desc/task/watcherjoined",
    "ENCODING afterallactualchecks/comparison; write/flushbeforeWp",
    "CLEANING fixed<=10s actualretirement",
    "QUIESCENT completedbeforeDp_total thenRETIRED; failedroutecannotQUIESCENT"
  ],
  "global_failure": "AtWp/time/interruption/resourcefailure CASFAILED/CANCEL_REQUESTED irreversible; cleanupusesDp_total. Parentwatcher Dchild bornBEFOREchildProcess.start and retainedwatcherstartedBEFOREblockingstart/dependentwait, timedoutABORTINGwinslateexit. Signalretainedchild/descendants BEFOREclose/join, destroy250/force1000/fixedcleanup. Exactwatcherstartfailure/latejoin failswholemethod. Parent/supervisor complete16domain/oneheavybrokerlease unchanged."
}
```

The parent has one lazy fixed two-worker drain executor. FutureTask handles are
retained before execute and actual Runnable latches prove completion; cancel or
Future.done is insufficient. Typed streams/JARs/channels/processes/tasks/workers
are closed, destroyed and joined under the same deadline and counters. Worker
operations require the actual assigned worker, stream, task, parent resource,
phase and deadline. First Throwable is irreversible; preserve suppressed errors
and interruption. Consumed reservations are never refunded, including failure
and EOF probes. Missing, late, failed or unobserved retirement is not success.
All parent, child, Git and external supervisor processes remain inside the
existing sixteen-process domain and single heavy/broker lease.

## Four witnesses and unchanged outputs

The private parent record-check function accepts only an internal fixed phase.
It records exactly initial-before before first child start, initial-after after
first actual exit/drains/descendants/tasks/watcher join, replay-before before
second start, and replay-after after second fully joined boundary. Each is a
new complete classpath plus seven dependency witness. Reject duplicate,
misordered, child-supplied or shared candidate rows. Normalize only the existing
declared path/process receipt coordinates when comparing full values.

Parent -main independently owns actual process/runtime/environment/argv,
C/T/Qs/TQs/method/supervisor and actual child transport/comparison. The four
witness slots retain the original two-key map; there is no new wire key or
semantic product authority. External supervisor resource sealing, release,
actual parent exit and result-last ordering remain separately required. The
parent cannot attest a future exit, future release or unobserved control.

## Fit, evidence artifacts and acceptance

Let N_C count complete C-derived classpath source objects. Fixed dependency
depths are [6,6,5,6,4,4,6], sum 37. A witness's declared Git subtotal is
N_C + 89; four witnesses plus the eighteen raw parent operations yield
4*N_C + 374. This is a declared topology subtotal, not a complete or measured
bound. Final method-specific Q/M/pin work and any other actual operations must
be added explicitly. All repeated classpath/JAR reads, enumeration, lexical
forms, AST nodes, hashes, path probes, complete proofs and failed/unhooked work
consume their existing cumulative limits before work.

The implementation evidence bundle must bind a clean final C/T and all
twenty-three allocated physical code/test/fixture paths, exact public Var and
interface descriptors, source/JAR/load provenance, complete cold accounting and
ordinary behavior. Each file in the production subset must fit within 250
physical lines; tests and fixture remain inside the same twenty-three paths. Static code review must authenticate
the aggregate raw route, shared kernel, distinct owner seals, fixed pure proof
call graph and all four actual phase boundaries. Positive and negative cases
must cover foreign owners before access, paired data forgery, wrong process or
phase, stale tickets, resource overflow, late zero exit, interruption, failed
watcher start, blocked I/O, cleanup errors and ordinary inert namespace loading.

The selected route is a distinct parent interface with no child namespace load.
The alternative of inertly loading the child interface was compared; it would
add parent load-closure, late compilation and continuous controller obligations.
It is not selected and cannot silently replace this route.

A fresh finite method requires both independent pre-run accepts, coordinator
authentication and concrete one-shot GO. It must prove actual complete cumulative
fit, observed lease admission and retirement, process/stream/task/descendant
closure, timeout precedence, ordinary preservation and final load provenance.
Fresh independent actual-result reviews and coordinator audit follow execution.
Failed fit requires HOLD and a separately reviewed exact correction; it never
authorizes extra files, load rows, resource caps, fallback or replay.

The canonical amendment itself must complete its reservation, reviews, finite
validation, integration eligibility, normal coordinator merge, terminal closure
and accepted-main postflight before parent code is extended. SOURCE adoption
must preserve existing implementation identity/history/reservation and truthful
ordinal migration. Historical dirty observations are design context, never
immutable launch pins or code acceptance. Architecture acceptance grants no
SOURCE, native, consumer, G1, performance, release or self-hosting claim.
