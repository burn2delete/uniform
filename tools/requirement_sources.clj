(ns requirement-sources
  "Exact pinned source views across documented authority-only layout transfers."
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]))

(def platform-generated-view-instructions
  "Edit the ledger and run `clojure -M tools/platform_task_projection.clj --write`;\nthe project table and full-product scope below are generated.\n")
(def platform-authority
  (str "Implementation task authority: [Spec Kit platform ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).\n"
       platform-generated-view-instructions
       "\n"))
(def platform-markers
  (for [region ["platform-task-projection" "platform-product-projection"] side ["start" "end"]]
    (str "<!-- " region ":" side " -->\n")))
(def platform-generated-view-kind :platform-task-authority-current-layout-v2)
(def platform-generated-view-contract
  {:instructions platform-generated-view-instructions
   :regions [{:marker "platform-task-projection"
              :replacement "Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).\n"}
             {:marker "platform-product-projection"
              :replacement "Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/platform-workflow/tasks.md#platform-product-scope).\n"}]})

;; Current-main source changes are projected only through these exact, named
;; bridges.  A source hash alone is insufficient: the hunk inventory and
;; semantic qualification are part of the authenticated review record.
(def exact-source-projection-kinds
  #{:l4-macro-expansion-semantic-projection
    :c4-macro-expansion-semantic-projection
    :math4-eml-search-policy-semantic-projection
    :math9-rewrite-termination-semantic-projection
    :platform-task-authority-current-layout
    :platform-task-authority-current-layout-v2
    :governance-lifecycle-policy-semantic-projection})

(defn exact-source-projection-kind? [kind]
  (contains? exact-source-projection-kinds kind))

(def l4-macro-projection-hunks
  [{:hunk-id "L4-84"
    :facet-ids ["facet/language/40374ddb1a080946390d3a73"
                "facet/language/840ec7232c44e15db7e6b4b8"
                "facet/language/5bba9c6b2116b721dced073b"]
    :baseline-start-line 84 :baseline-end-line 84
    :live-start-line 84 :live-end-line 91
    :baseline-fragment-sha256 "0c11c7df1dfea35340a4a362ebe7708f65f7b6b88b68a928a37e062a3e10dbfe"
    :live-fragment-sha256 "3c742f7d3576bce421e72dbd404f9cddd5ade666501499136daa25d3830a49c5"}
   {:hunk-id "L4-178"
    :facet-ids ["facet/language/f4c46ab57706eeb771eeec35"]
    :baseline-start-line 178 :baseline-end-line 178
    :live-start-line 185 :live-end-line 186
    :baseline-fragment-sha256 "c9f84ad3c00c0c2be9aa54f7c1734f216d914015152cdbee282694d0f19a50dc"
    :live-fragment-sha256 "eb3439233c0bcf323954b0a8c5a015703766981e6b6128995a155e0c0658bc5b"}
   {:hunk-id "L4-218"
    :facet-ids ["facet/language/1bc2e3aefe4345c6715e4b09"]
    :baseline-start-line 218 :baseline-end-line 218
    :live-start-line 226 :live-end-line 228
    :baseline-fragment-sha256 "573c98342f76eed1a8990cc558f015485d86780ef3f8e54db9a4a3cceb17f300"
    :live-fragment-sha256 "3c7e70e4e7bbca8d80d3b1ef7f26a0536fe1e4338160d498cdb8d6d4173e557e"}
   {:hunk-id "L4-225"
    :facet-ids ["facet/language/258add08b50dde3b493a8203"]
    :baseline-start-line 225 :baseline-end-line 225
    :live-start-line 235 :live-end-line 237
    :baseline-fragment-sha256 "16615da47a72f9afd45e77a9d6948adf5ba4fe11ad2f31c0ad18eea97a8580d0"
    :live-fragment-sha256 "c58dfe29be33f5c30e647c7c938a392091134f612167dbc29024228b28ac9956"}])

(def c4-macro-projection-hunks
  [{:hunk-id "C4-30"
    :facet-ids ["C4/normalized-facet/1374-1430/0c0690db0b3c3b64"
                "C4/normalized-facet/1430-1445/df3e02cd7a3c1169"
                "C4/normalized-facet/1445-1487/8cfe6ac873c72363"]
    :baseline-start-line 30 :baseline-end-line 30
    :live-start-line 30 :live-end-line 30
    :baseline-fragment-sha256 "beb8771be3497f3d4b14801b2a709d5a3e675a0ae910c18105ffd5a23ac0cd7f"
    :live-fragment-sha256 "539e12a4caf7a2e8cca3d1c5eff6ce14407803de4fcd86fc6c1c01db2045e2ca"}
   {:hunk-id "C4-66"
    :facet-ids ["C4/normalized-facet/2112-2348/08a0f0551f9983cf"
                "C4/normalized-facet/2348-2396/adbbc0fcc8bc2252"
                "C4/normalized-facet/2396-2402/9026dbfa763867af"]
    :baseline-start-line 66 :baseline-end-line 66
    :live-start-line 66 :live-end-line 66
    :baseline-fragment-sha256 "a246d40da2f3c41a8d0e16bb57c9b30a49db57fbbd986af6684e96d09418af7a"
    :live-fragment-sha256 "83eb1de6b08b43f7c03b43763d3cd7faf9cc5c1a21b4d4b29435700cf9460ee5"}
   {:hunk-id "C4-84"
    :facet-ids ["C4/normalized-facet/2990-3028/169f9707695c8842"
                "C4/normalized-facet/3028-3049/22d9e18e6a0c4070"
                "C4/normalized-facet/3049-3051/eb4bd64f7014f7d4"]
    :baseline-start-line 84 :baseline-end-line 84
    :live-start-line 84 :live-end-line 86
    :baseline-fragment-sha256 "7073dbf329d5bc9ee3deb54655aefe4a31d067edc9c2366843d6d42f2d89eb23"
    :live-fragment-sha256 "9d0b2309c45c3e8d2c1c681dc6c46ac5df1b355be30d08668222730fe23fc49a"}
   {:hunk-id "C4-175"
    :facet-ids ["C4/normalized-facet/5866-5869/934fa8d0c0971796"
                "C4/normalized-facet/5869-5910/4902048ad50c5fe6"
                "C4/normalized-facet/5910-5911/01ba4719c80b6fe9"
                "C4/normalized-facet/5911-5914/934fa8d0c0971796"
                "C4/normalized-facet/5914-5959/fa42b0f1bf9b4487"
                "C4/normalized-facet/5959-5960/01ba4719c80b6fe9"]
    :baseline-start-line 175 :baseline-end-line 176
    :live-start-line 177 :live-end-line 180
    :baseline-fragment-sha256 "28895308f53fc8a72a901e759d04852bd0af832eb6bb84930f515bbc9726005a"
    :live-fragment-sha256 "ef0b1b024f7d8b840fff82465a1b8cfde556cfdf2af6373e7e941a6bef4801eb"}
   {:hunk-id "C4-207"
    :facet-ids ["C4/normalized-facet/7051-7053/e86dd77a7ee765ea"
                "C4/normalized-facet/7053-7089/1a12aaf2dc193dbe"
                "C4/normalized-facet/7089-7091/beadba836315747f"]
    :baseline-start-line 207 :baseline-end-line 207
    :live-start-line 211 :live-end-line 212
    :baseline-fragment-sha256 "1936945e5cfb861dfb725aae04178a038dc2348031fb2a0235b80563487bc7e0"
    :live-fragment-sha256 "ff3c90d3b5895adacee3544c2fd24370d8ce286620fbbbc6eb9d5878f6c6c0f5"}])

(def platform-current-projection-hunks
  [{:hunk-id "platform-authority-stanza"
    :facet-ids ["facet/extension/a82b36fd375fb3fb6a9a5834"]
    :baseline-start-line 8 :baseline-end-line 7
    :live-start-line 8 :live-end-line 9
    :baseline-fragment-sha256 "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    :live-fragment-sha256 "cfafdceb3a2c7341ee317d1d7aa572fe7c63bb1d6168f0e224edac6368ec2b74"}
   {:hunk-id "platform-project-ledger-view"
    :facet-ids ["facet/extension/1d2fe279947d9c26d30a6647"
                "facet/extension/ed7c0ecd3830ac7df77a452a"
                "facet/extension/70402e8cb232dd79d50ad393"
                "facet/extension/bd6b008cf43a9d14cf95ea9e"
                "facet/extension/ee630855078eaef6b70e812c"
                "facet/extension/db3504f343e95aa12b940c47"
                "facet/extension/76522348020c9b41fc2ad242"]
    :baseline-start-line 36 :baseline-end-line 44
    :live-start-line 38 :live-end-line 38
    :baseline-fragment-sha256 "dc0998182542e03f1ff170e1f204936a6d0a34fab69c0601c91d750d5dbda733"
    :live-fragment-sha256 "0cfde92802968b43bc62618dd1a0f0dd96a2582e1f89f696ab87008d54116ded"}
   {:hunk-id "platform-product-ledger-view"
    :facet-ids ["facet/extension/3cc23801983338b37e2b1691"
                "facet/extension/7a4ac84981fb10372a12b34a"
                "facet/extension/4f69644b193631ea640a86a0"
                "facet/extension/d4830a671189825882f1eca1"
                "facet/extension/f097e7a38164c004ececb971"
                "facet/extension/51e1a50900a57dbb281be48b"
                "facet/extension/1d6788ee2b3ed049619e2c1e"
                "facet/extension/4f88c0f9e65b5748fc3086e9"
                "facet/extension/207f40d7c91d7a397718d38b"
                "facet/extension/cdadc309efd7cd9b4ef80339"
                "facet/extension/0c1dbf6de184685dd9d63854"
                "facet/extension/1b90f6fb7422faa58f1b4440"]
    :baseline-start-line 74 :baseline-end-line 127
    :live-start-line 68 :live-end-line 68
    :baseline-fragment-sha256 "41d23ca657df36e65bddab892a632ac04d88e77df4efce274e3b8eefd8545bab"
    :live-fragment-sha256 "f263104de6336a0b875805af93eba4d49ff5e95509c718a20d3c4d63354d6b77"}])

(def agents-policy-projection-hunks
  [{:hunk-id "AGENTS-integration-preflight"
    :facet-ids ["facet/extension/d3c54bd0a627a0423d27c564"]
    :baseline-start-line 213 :baseline-end-line 214
    :live-start-line 213 :live-end-line 217
    :baseline-fragment-sha256 "15a4f6954aef7548235631890f67d798cbb1c7b9b6f65a683fc0ab6d41a77bf9"
    :live-fragment-sha256 "36929cc193df07f3ecd0970ee7c06c55bad78f87e1609fed67ecbd399b929ef0"}])

(def math4-search-policy-kind :math4-eml-search-policy-semantic-projection)
(def math4-search-policy-path
  "docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md")
(def math4-search-policy-baseline-ref
  "a1b991be499d50579a8b4366976c519ae8949224")
(def math4-search-policy-applying-ref
  "9d5039362df42863111097217f30d8e13693c9ac")
(def math4-search-policy-baseline-tree
  "5b62819e511bfc7e6b5eba0527d8c9b5883c5572")
(def math4-search-policy-applying-tree
  "14e32fb6c46c74d5522771c0f805cd8830f99aeb")
(def math4-search-policy-baseline-sha256
  "df3613be7932354a0efa846b27365c7e7f5c9a404b2a788113857912bf5d14eb")
(def math4-search-policy-live-sha256
  "55c3eb0d96b729eba9e1600e439091db3ad9890f2e6da0ce11fe711c7567360a")
(def math4-search-policy-baseline-blob
  "893307855d138b48c68fece63122dc5a4cfe07f4")
(def math4-search-policy-applying-blob
  "907ec2399cbe35d0c4d4f97fe3cf6faafc128f2b")
(def math4-search-policy-current-criteria
  {:retained [:verified-efir-domain-branch-numeric-precision-source-anchors
              :grammar-operators-and-constants
              :rule-premises-and-invalidation
              :candidate-ranking-and-selection
              :deterministic-ordering-and-tie-policy
              :replayable-traces
              :proof-and-approximation-acceptance
              :candidate-lifecycle-and-diagnostics]
   :removed [:project-work-budget-and-admission-fuel]
   :qualification :current-math4-criteria-derived-from-9d5039362})
(def math4-search-policy-projection-hunks
  [{:hunk-id "MATH4-search-manifest"
    :facet-ids ["MATH4/partition-facet/b0242c1e615d110b8dbf22e266c89080036586922838c2974dfc04c3312c6445"
                "MATH4/partition-facet/5531de997b8799d293891bb399be16017c7b692725a8b1e7ee9d0b0d4a3396a2"
                "MATH4/partition-facet/c7a3b5fb25af8d9c6c4ed00509c91868bf90413a966106d680aed330d21df2d7"
                "MATH4/partition-facet/1d9acdbfdad21d99f36007292ef5ee91fbd87f960e96bf9bfc758ff8c979e63e"
                "MATH4/partition-facet/f46376ad54e0b21f10f9d38c8c3228d7be3bcdefb3b4d8bb11c57681be7efc7a"]
    :baseline-start-line 130 :baseline-end-line 149
    :live-start-line 130 :live-end-line 152
    :baseline-fragment-sha256 "b833d11e86d7da70026fb1ce90bb363c527d20beb2be63b75e862ef45600673a"
    :live-fragment-sha256 "833502a90365f96164650a852e0d7bfcbb655f19066628400cb0e7370e338621"}
   {:hunk-id "MATH4-search-diagnostic"
    :facet-ids ["MATH4/partition-facet/608e0b440b42427ebb5e0a6088a115707d61831b739994a227ba455b35f18ce3"]
    :baseline-start-line 206 :baseline-end-line 206
    :live-start-line 209 :live-end-line 209
    :baseline-fragment-sha256 "410ebde6d2e88ced922f21e7788a7f6dc6923fad29e37d2c344ad7a374851b35"
    :live-fragment-sha256 "b5f05427b20dfe9ebacab7c6a349622b1a7e091f35c47d21166cdc1180a1ef02"}])

(def math9-rewrite-termination-kind :math9-rewrite-termination-semantic-projection)
(def math9-rewrite-termination-path
  "docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md")
(def math9-rewrite-termination-baseline-ref
  "a1b991be499d50579a8b4366976c519ae8949224")
(def math9-rewrite-termination-applying-ref
  "4d8a085a9b68bb8c349869c574b94ee4cea81406")
(def math9-rewrite-termination-baseline-tree
  "5b62819e511bfc7e6b5eba0527d8c9b5883c5572")
(def math9-rewrite-termination-applying-tree
  "2366861f1514b0cb03a7a869b2af365d3816df3c")
(def math9-rewrite-termination-baseline-sha256
  "ca9ccdcfe25a76de1127d5629d8265f83885481e7334012ee4da8aa1fdbd0410")
(def math9-rewrite-termination-live-sha256
  "543cfc52291e89d60c0a80c0c056221c098bc9aed11d8fb7f3e45e999322bff3")
(def math9-rewrite-termination-baseline-blob
  "ac364084776e3cd0a2046c224aacd7d65178cc68")
(def math9-rewrite-termination-applying-blob
  "cda65cb93e21f483fc1dafd8dc429f38198940d6")
(def math9-rewrite-termination-current-criteria
  {:retained [:rule-domain-branch-numeric-floating-side-condition-proof-source-and-invalidation
              :deterministic-rule-ordering-cost-model-tie-breaks-equality-explanation-and-proof-replay
              :well-founded-termination-finite-trace-and-stopped-run-history]
   :changed [:unsupported-strict-tan-occurrence-example-claim-removed
             :selected-strategy-finite-termination-evidence-required]
   :qualification :current-math9-criteria-derived-from-4d8a085a})
(def math9-rewrite-termination-projection-hunks
  [{:hunk-id "MATH9-rewrite-termination"
    :facet-ids ["MATH9/partition-facet/f7ea6e89842884748fe59dbe8f51ab1430d943e274be2db887506a777a0977c9"
                "MATH9/partition-facet/e231af9a1d26e177746d45311c38254441b372ff323f06178c5a5777ae4243ad"]
    :baseline-start-line 76 :baseline-end-line 81
    :live-start-line 76 :live-end-line 82
    :baseline-fragment-sha256 "a411bb29a0fb1a407e03b9efafede91b9b3c22f59d58686da75760adfa245983"
    :live-fragment-sha256 "5f8471f4805c2b97145498313219231f2575826fe580aec51296360fe70aade5"}])

(def std3-accounting-kind :stdlib-accounting-policy-semantic-projection)
(defn baseline-projection-kind? [kind]
  (or (= std3-accounting-kind kind)
      (exact-source-projection-kind? kind)))
(def std3-accounting-path
  "docs/phase-16-standard-library/213-std3-collections-library-specification.md")
(def std3-accounting-baseline-ref
  "a1b991be499d50579a8b4366976c519ae8949224")
(def std3-accounting-applying-ref
  "5cf644298b95940b6499c52af1260dfdb609e7e0")
(def std3-accounting-baseline-sha256
  "58d096cc16d922ad72d560c5b3e4d60d093c9527a9472c2414d63deb98630e3d")
(def std3-accounting-live-sha256
  "af847392b2ff50155d61a9a9c93bb6e2f1cc573220ceb11920de4dd483bd4160")
(def std3-accounting-lineage-record-digest
  "a6780e4a2514b65d84d84f003b4eb582f5dd66866dc461915ebe72e40885cf7e")
(def std3-accounting-authority
  "AGENTS.md#project-accounting-removal, with STD3 source-bound selective supersession review.")
(def std3-accounting-qualification
  "Only the six explicitly reviewed STD3 accounting-policy source regions are projected back to the authenticated baseline view. Physical member validation, allocation, ownership, lifetime, effects, capabilities, profile legality and host restrictions remain unchanged. Traversal completeness and validation timing are recorded as mixed semantic deltas, not treated as quota equivalence. This proposal is source reconciliation evidence only and does not confer canonical adoption or implementation acceptance.")
(def std3-accounting-semantic-deltas
  [{:hunk-id "STD3-112" :classification :accounting-removal-with-complete-traversal-validation}
   {:hunk-id "STD3-313" :classification :accounting-removal-with-preconstruction-validation}
   {:hunk-id "STD3-332" :classification :accounting-only-pair-width-quota-removal}
   {:hunk-id "STD3-353" :classification :accounting-removal-with-validation-timing-change}
   {:hunk-id "STD3-359" :classification :accounting-removal-with-complete-physical-enumeration}
   {:hunk-id "STD3-497" :classification :accounting-removal-with-complete-census-validation}])

(def exact-source-bridge-contracts
  {math9-rewrite-termination-kind
   {:document-id "MATH9"
    :path math9-rewrite-termination-path
    :baseline-ref math9-rewrite-termination-baseline-ref
    :applying-ref math9-rewrite-termination-applying-ref
    :baseline-tree math9-rewrite-termination-baseline-tree
    :applying-tree math9-rewrite-termination-applying-tree
    :baseline-sha256 math9-rewrite-termination-baseline-sha256
    :live-sha256 math9-rewrite-termination-live-sha256
    :baseline-blob math9-rewrite-termination-baseline-blob
    :applying-blob math9-rewrite-termination-applying-blob
    :review-state :proposed
    :authority "MATH9 rewrite-system source audit of current termination evidence after mandatory-fuel retirement and unsupported example-claim removal."
    :qualification "Only the exact Rewrite Rule Form termination region is projected to the authenticated baseline view. Rule/domain/branch/numeric/floating/side-condition/proof/source and invalidation facts, deterministic rule ordering, cost-model tie breaks, equality explanations, proof replay, and well-founded finite termination evidence remain current semantic obligations. The current source removes an unsupported strict tan-occurrence example claim and requires finite termination evidence from the selected strategy; no proof is inferred from the example's omission, and this bridge is source reconciliation evidence only."
    :current-criteria math9-rewrite-termination-current-criteria
    :historical-applying
    {:ref "dcf3378130ef92c37105aeda17b3badfec6619dc"
     :tree "b0609b39cf93b8c27dbd204b69ca197ef781d8f8"
     :blob "5bae82b6d377e64b803dd1c6a7282f4ec179406b"
     :live-sha256 "8ee9b9bd7a70508ebac2d74eac64417462c1a18f75e5b2ae2c43b12b96266430"
     :qualification "Historical dcf337 MATH9 snapshot retained for provenance only; it is not the current source and is not re-admitted as proof."}
    :semantic-deltas [{:hunk-id "MATH9-rewrite-termination" :classification :semantic-termination-evidence-and-unsupported-example-removal}]
    :projection-hunks math9-rewrite-termination-projection-hunks}
   math4-search-policy-kind
   {:document-id "MATH4"
    :path math4-search-policy-path
    :baseline-ref math4-search-policy-baseline-ref
    :applying-ref math4-search-policy-applying-ref
    :baseline-tree math4-search-policy-baseline-tree
    :applying-tree math4-search-policy-applying-tree
    :baseline-sha256 math4-search-policy-baseline-sha256
    :live-sha256 math4-search-policy-live-sha256
    :baseline-blob math4-search-policy-baseline-blob
    :applying-blob math4-search-policy-applying-blob
    :review-state :proposed
    :authority "AGENTS.md#project-accounting-removal, with MATH4 source-bound search-policy supersession review."
    :qualification "Only the exact MATH4 search-manifest and diagnostic hunks are projected to the authenticated baseline view. Verified EFIR/domain/branch/numeric/precision/source anchors, grammar and operators, rule premises and invalidation, candidate ranking and selection, deterministic ordering and tie policy, replayable traces, proof or approximation acceptance, candidate lifecycle and diagnostic metadata remain current criteria. The project work budget and admission fuel are removed; this bridge is source reconciliation evidence only and confers no canonical adoption or implementation acceptance."
    :current-criteria math4-search-policy-current-criteria
    :semantic-deltas [{:hunk-id "MATH4-search-manifest" :classification :accounting-removal-with-deterministic-search-and-proof-criteria}
                      {:hunk-id "MATH4-search-diagnostic" :classification :diagnostic-wording-clarification}]
    :projection-hunks math4-search-policy-projection-hunks}
   :l4-macro-expansion-semantic-projection
   {:document-id "L4"
    :path "docs/phase-01-core-language/014-l4-macro-system-specification.md"
    :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224"
    :applying-ref "e766928c0ef6bb5c5b70b7e8feeae11e7b9814be"
    :baseline-sha256 "31c0e04396a99f06680258d7d52023087833762099d81aa69fb6b7e0731875e9"
    :live-sha256 "61107cce6eb93192a49de0a383d7bc95cca44169377d6385da6bcdef568ade76"
    :baseline-blob "e7455d0a5b528747d58e0056ba9304281313bf1b"
    :applying-blob "2b09823d88073f7c761c0b9e79f8ef1d1bddf543"
    :review-state :proposed
    :authority "L4 macro-system source audit of current-main quota retirement and retained cycle/host semantics."
    :qualification "Only the four exact L4 source hunks are projected to the authenticated baseline view. Recursive-cycle detection, host-containment diagnostics, finite expansion behavior, hygiene, metadata, effects and profile legality remain current semantic obligations; no project depth/body/size quota equivalence or canonical adoption is claimed."
    :semantic-deltas [{:hunk-id "L4-84" :classification :mixed-recursive-cycle-host-containment-and-accounting}
                      {:hunk-id "L4-178" :classification :mixed-diagnostic-contract-and-accounting}
                      {:hunk-id "L4-218" :classification :mixed-diagnostic-meaning-and-accounting}
                      {:hunk-id "L4-225" :classification :semantic-finite-nesting-and-cycle-fixtures}]
    :projection-hunks l4-macro-projection-hunks}
   :c4-macro-expansion-semantic-projection
   {:document-id "C4"
    :path "docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md"
    :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224"
    :applying-ref "e766928c0ef6bb5c5b70b7e8feeae11e7b9814be"
    :baseline-sha256 "0a2428520a9c5fb23336e9bcd9bd73c4f074426f4cec82b97bf0df140e994cee"
    :live-sha256 "de7259906b87253bb2c34b967755f2bce8f35f060b08de6f2325a8aea7f1733b"
    :baseline-blob "2c5c8e6539bcdfc99dc193b0f187eb167f35c51e"
    :applying-blob "722230f06b68f266efa5368b3db8f726063220c5"
    :review-state :proposed
    :authority "C4 macro-expansion-engine source audit of current-main quota retirement and retained termination/diagnostic semantics."
    :qualification "Only the five exact C4 source hunks are projected to the authenticated baseline view. Recursive-cycle rejection, phase/hygiene/effect/profile obligations, host safeguards and compatibility diagnostic identifiers remain current semantic obligations; no project depth/size quota equivalence or canonical adoption is claimed."
    :semantic-deltas [{:hunk-id "C4-30" :classification :mixed-termination-diagnostic-and-accounting}
                      {:hunk-id "C4-66" :classification :semantic-host-safeguard-schema}
                      {:hunk-id "C4-84" :classification :mixed-recursion-rejection-and-accounting}
                      {:hunk-id "C4-175" :classification :mixed-diagnostic-compatibility-and-accounting}
                      {:hunk-id "C4-207" :classification :semantic-cycle-host-containment-conformance}]
    :projection-hunks c4-macro-projection-hunks}
   :platform-task-authority-current-layout
   {:document-id "platform-product"
    :path "docs/component-roadmaps/platform.md"
    :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224"
    :applying-ref "84a929dfa6661e7e4fa8ff16f58767b3652f31f7"
    :baseline-sha256 "03a971d2bf910876352e9555d45679a9fa3d00876710c8233de5f50882349a3f"
    :live-sha256 "179397f1c4751e231981d5e6ffe44bf5d091deb2ae1767dd16aed76f5ea2ab2d"
    :baseline-blob "91c4d4fcc0094c41c4d73e39fae3006f7c9e44cf"
    :applying-blob "69520b7864b0e1d47f276eb15bbe5e3ac9712642"
    :review-state :proposed
    :authority "Platform task-ledger authority/layout reconciliation for current main, preserving the historical PR472 source-view receipt."
    :qualification "Only the exact current task-authority stanza and two generated-view replacements are projected to the authenticated historical platform source view. The editable ledger, task status/history, platform architecture decisions, target/product semantics and acceptance remain current and open; this bridge confers no implementation or product acceptance and is not accounting equivalence."
    :semantic-deltas [{:hunk-id "platform-authority-stanza" :classification :platform-task-ledger-authority-layout}
                      {:hunk-id "platform-project-ledger-view" :classification :platform-generated-view-authority-layout}
                      {:hunk-id "platform-product-ledger-view" :classification :platform-product-scope-authority-layout}]
    :projection-hunks platform-current-projection-hunks}
   platform-generated-view-kind
   {:document-id "platform-product"
    :path "docs/component-roadmaps/platform.md"
    :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224"
    :applying-ref "2a86a9f1fc56b1eceacf4ca29aac7c95a1a4792b"
    :baseline-sha256 "03a971d2bf910876352e9555d45679a9fa3d00876710c8233de5f50882349a3f"
    :live-sha256 "9d4efe1bb5348949dde850880154cf31d06dbc81cb4b659144e771efce2a54d5"
    :baseline-blob "91c4d4fcc0094c41c4d73e39fae3006f7c9e44cf"
    :applying-blob "2fdfdc30648d1bc7d08adde3d925a2d9aa7c73cb"
    :review-state :proposed
    :authority "Platform task-ledger authority and generated-view reconciliation for the bounded PR554 product-task port."
    :qualification "Only the exact generated task/product regions and the task-ledger instruction stanza are removed to recover the current navigation view; the existing named source hunks then project to the authenticated historical platform source. The sole editable task ledger remains authoritative for the eleven product task interfaces; task status, product architecture, target semantics and implementation acceptance remain open. This bridge confers no implementation or product acceptance and is not accounting equivalence."
    :semantic-deltas [{:hunk-id "platform-authority-stanza" :classification :platform-task-ledger-authority-layout}
                      {:hunk-id "platform-generated-task-view" :classification :platform-generated-task-interface-view}
                      {:hunk-id "platform-generated-product-view" :classification :platform-generated-product-interface-view}
                      {:hunk-id "platform-project-ledger-view" :classification :platform-generated-view-authority-layout}
                      {:hunk-id "platform-product-ledger-view" :classification :platform-product-scope-authority-layout}]
    :generated-view platform-generated-view-contract
    :projection-hunks platform-current-projection-hunks}
   :governance-lifecycle-policy-semantic-projection
   {:document-id "accounting-policy"
    :path "AGENTS.md"
    :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224"
    :applying-ref "60f59d225b03338eb30cdc22850838ecebd07303"
    :baseline-sha256 "52e41ad759618a3837d1f818a9f91427823d2b6461a9cd797544cfcccfed90ac"
    :live-sha256 "05758d7322985d88f8fc31cc25d02c7e5514442df11d67c0517bc96bc5004c93"
    :baseline-blob "35a8636a2e6c656ba34acc4d96da5cdb46550bab"
    :applying-blob "ad24f9fa46161ceb2ee633b438cb8d930a2ee632"
    :review-state :proposed
    :authority "AGENTS lifecycle/preflight governance source reconciliation, distinct from project-accounting removal."
    :qualification "Only the exact integration-preflight lifecycle paragraph is projected to the authenticated historical policy view. Exact base/commit/tree binding, governed-composition metadata and independent review remain current governance semantics; no quota/accounting equivalence or canonical adoption is claimed."
    :semantic-deltas [{:hunk-id "AGENTS-integration-preflight" :classification :governance-lifecycle-and-preflight-binding}]
    :projection-hunks agents-policy-projection-hunks}})

;; The PR554 task ledger is a second source view of the same bounded platform
;; transfer.  Its historical SH crosswalk points at the four short rows in the
;; frozen comparator, while the applying commit adds the current criteria to
;; those rows.  Keep that transition explicit and content-addressed: callers
;; may tolerate navigation/layout movement around a row, but they may not
;; silently accept a changed criterion, status or source basis.
(def platform-task-source-transition
  {:kind :platform-task-authority-ledger-current-layout-v1
   :document-id "platform-task-authority"
   :path "specs/platform-workflow/tasks.md"
   :baseline-ref "5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e"
   :applying-ref "2a86a9f1fc56b1eceacf4ca29aac7c95a1a4792b"
   :historical-source-commit "254a5b50ea5e2825c424248c9216802cc866c0fd"
   :historical-source-tree "abb83592fc9e5e81c0f041e59c67749dbb67b55c"
   :baseline-tree "643afe5f3b00918fc3de76f2855294481a09b505"
   :applying-tree "8a242945a690dcf4ae6a7b15ecc546c09179a80c"
   :baseline-sha256 "789f8482bf8212e68c978daaaa2487f39f66a46467385ba0b0350df82a78e39f"
   :live-sha256 "19356a500121d7c0ad6e5284c4f415dc81861ec330f7e1120c9d28932420e92f"
   :baseline-blob "0c5e17169840e1a5deee71e5733955ff0a41805a"
   :applying-blob "054ee72c93246d88785ed29d82b549428a0d4e64"
   ;; The current-main composition may add explicitly pending task criteria
   ;; after the historical PR554 applying blob.  Keep that old bridge intact
   ;; and authenticate the composed checkout as a separate source view.
   :current-source-ref "f9f56c6dffb92d6e8ca5a32ecc5c791d69d1e5db"
   :current-source-tree "d93d45d8b6c81d229d205569016799b8733b185e"
   :current-source-blob "536da4ae764a3c985d758fa3f2e8435d06430862"
   :current-source-sha256 "34035c35f900c7d45a04de2802086608c00605f39cb7c2d17b47b8411966660c"
   :parent-bridge-kind platform-generated-view-kind
   :parent-bridge-applying-ref "2a86a9f1fc56b1eceacf4ca29aac7c95a1a4792b"
   :task-ids ["hardware" "firmware" "kernel" "devices"]
   :review-state :proposed
   :authority "PR554 task-ledger source transition, authenticated against the frozen SH task rows and the applying task-authority Git blob."
   :qualification "Only the four named Platform task rows transfer from the frozen historical SH source view to the applying PR554 task ledger. The applying row bytes, IDs, status, criteria and source-basis identities are exact; surrounding navigation layout may move. This bridge preserves historical SH routing and structural-only authority, confers no implementation or product acceptance, and does not authorize arbitrary task-criterion changes or source-hash renewal."})

;; PR564 refines the five existing Stdlib task rows after the SH crosswalk's
;; historical source snapshot.  Keep the old locator and the applying bytes as
;; separate immutable Git identities; a changed task criterion is not a
;; navigation-only edit and cannot silently rebind SH routing.
(def stdlib-task-source-transition
  {:kind :stdlib-task-authority-ledger-current-layout-v1
   :document-id "stdlib-task-authority"
   :path "specs/stdlib-workflow/tasks.md"
   :baseline-ref "dbeda7c7dc9a7bb880baa90af1a89046828db327"
   :applying-ref "80d8a655cea00218799446986a976ca64043a1f8"
   :historical-source-commit "254a5b50ea5e2825c424248c9216802cc866c0fd"
   :historical-source-tree "abb83592fc9e5e81c0f041e59c67749dbb67b55c"
   :baseline-tree "576075197b5b4e815b78bae7d949bfb5fe2d0437"
   :applying-tree "8c7e533bed9536a760254754e26dbb75db0b0f55"
   :baseline-sha256 "69ac9a7163838c4cb297c94174d0b3061b0e7686f2378c45aa30e6320b6f6d94"
   :live-sha256 "9ac5d948a4df974051fe15adce874f6bf39074982991d8c6c80201a971142d57"
   :baseline-blob "36551070cb5e0f44318df28f7673773fa0df6984"
   :applying-blob "7a3643c34a3798cffcf4e7f4f539118d9ceec35f"
   ;; Preserve PR564's historical/applying bridge while binding the current
   ;; composed task ledger separately; these bytes include only pending source
   ;; criteria and do not promote implementation or library acceptance.
   :current-source-ref "f79c62bcfc33f7aa645a455e0a98f32f0e008b6b"
   :current-source-tree "98c688013460d4aeddf090fadd7ec0c01d6d45d1"
   :current-source-blob "e2933a343359c2032086e34c996d02393e86b355"
   :current-source-sha256 "ecbf84050a1c0bec21ab3a83fbbe3ed7e55d67fd8a08fff42fdbaccfaed01bcb"
   :task-ids ["stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
              "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
              "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
              "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
              "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"]
   :review-state :proposed
   :authority "PR564 Stdlib task-ledger source transition, authenticated against the frozen SH task rows and the applying task-authority Git blob."
   :qualification "Only the five named Stdlib task rows transfer from the frozen historical SH source view to the applying task ledger. The applying row bytes, IDs, status, criteria and source-basis identities are exact; surrounding navigation layout may move. This bridge preserves historical SH routing and structural-only authority, confers no implementation or library-release acceptance, and does not authorize arbitrary task-criterion changes or source-hash renewal."})

;; PR767's current-source proposal adds source-only interface regions to files
;; that were pinned to the b886 composition. Keep this separate from
;; the historical Platform/Stdlib authority bridges above: it authenticates a
;; current development view and does not rebind an applying migration pin.
(def current-source-transition-kind
  :authenticated-current-source-file-transition-v1)
(def current-source-transition-contracts
  [{:role :current-source-interface-proposal,
  :path "specs/auth-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/auth-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 37498,
     :start-line 1,
     :end-line 426,
     :sha256
     "40b8fa53f98cee5e7939d738f000cbe388fd109d7babc840e16cc6819aa46999"},
    :after
    {:start-byte 0,
     :end-byte 38081,
     :start-line 1,
     :end-line 433,
     :sha256
     "a6a7bea46f4047d878890c34fed6d019bf35633ade613a3ea468e80da3489f3c"}}],
  :live-sha256
  "a6a7bea46f4047d878890c34fed6d019bf35633ade613a3ea468e80da3489f3c",
  :applying-blob "e8c486581086c204f9c6502766607ea88fc06606",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-auth-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/auth-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "40b8fa53f98cee5e7939d738f000cbe388fd109d7babc840e16cc6819aa46999",
  :baseline-blob "6102c9015800502f241a8d4826632abcad674c3b"}
 {:role :current-source-interface-proposal,
  :path "specs/auth-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/auth-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 37340,
     :start-line 1,
     :end-line 205,
     :sha256
     "d0cada75bab244e76d0985a7d22b0cc64c25f8f26625edfe1e61df8ca1a7f9e2"},
    :after
    {:start-byte 0,
     :end-byte 38604,
     :start-line 1,
     :end-line 213,
     :sha256
     "8862ea7945d528329882e84c693b522ae971dfeaf524706feb21fa4169639514"}}],
  :live-sha256
  "8862ea7945d528329882e84c693b522ae971dfeaf524706feb21fa4169639514",
  :applying-blob "d582303b169feaaa8c67586ac3114fab3379fb4c",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-auth-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/auth-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "d0cada75bab244e76d0985a7d22b0cc64c25f8f26625edfe1e61df8ca1a7f9e2",
  :baseline-blob "c6bee617c100cebc8da49c1899c341f540e35393"}
 {:role :current-source-interface-proposal,
  :path "specs/build-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/build-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 31429,
     :start-line 1,
     :end-line 324,
     :sha256
     "53c04cef6f0f9835d7b1a7c07e60039033ad684d8da783073c27791d6d4748f5"},
    :after
    {:start-byte 0,
     :end-byte 32078,
     :start-line 1,
     :end-line 331,
     :sha256
     "bdaee4de655e07164652b2f9592a3038f65b48a57f848abe648adc8ac9d4af4f"}}],
  :live-sha256
  "bdaee4de655e07164652b2f9592a3038f65b48a57f848abe648adc8ac9d4af4f",
  :applying-blob "58913132545af334181d7a642dd968eec0febd65",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-build-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/build-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "53c04cef6f0f9835d7b1a7c07e60039033ad684d8da783073c27791d6d4748f5",
  :baseline-blob "1aabad4df6496b4d1cf948d80d22ef899b6a2be8"}
 {:role :current-source-interface-proposal,
  :path "specs/compiler-api-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/compiler-api-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 36341,
     :start-line 1,
     :end-line 344,
     :sha256
     "0df35668340a97676b7356c365e38196fe969359991c8b25ada55e107ec00028"},
    :after
    {:start-byte 0,
     :end-byte 36945,
     :start-line 1,
     :end-line 351,
     :sha256
     "c0f615433fe9b26b5a65e0cfe24896681cea097827930bf827f37f3c1235c06f"}}],
  :live-sha256
  "c0f615433fe9b26b5a65e0cfe24896681cea097827930bf827f37f3c1235c06f",
  :applying-blob "c2b12582a7a0dda6b02d115acab71a3e0838ea2c",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id
  "l13-specs-compiler-api-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/compiler-api-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "0df35668340a97676b7356c365e38196fe969359991c8b25ada55e107ec00028",
  :baseline-blob "ccc8158489430c9c52f7669e192c6a69e045fe73"}
 {:role :current-source-interface-proposal,
  :path "specs/diag-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/diag-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 25308,
     :start-line 1,
     :end-line 284,
     :sha256
     "677c20b6bf8799d53a524668ee0f460be2e521c16a3aedfc4bbe3cf29351fa2a"},
    :after
    {:start-byte 0,
     :end-byte 25909,
     :start-line 1,
     :end-line 291,
     :sha256
     "3209d86ff5cf391b38d50cde134eb900c87d5f118de44141b2be369f2acdf9fc"}}],
  :live-sha256
  "3209d86ff5cf391b38d50cde134eb900c87d5f118de44141b2be369f2acdf9fc",
  :applying-blob "6cd1ef0b0501a532147166accef5044e5254ffd6",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-diag-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/diag-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "677c20b6bf8799d53a524668ee0f460be2e521c16a3aedfc4bbe3cf29351fa2a",
  :baseline-blob "9105a521c25b6bf293e4942fc38ce816bb05cc96"}
 {:role :current-source-interface-proposal,
  :path "specs/diag-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/diag-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 14651,
     :start-line 1,
     :end-line 111,
     :sha256
     "0ab4aa75a0f694cfad605b7f959acc89d471ee7eb1cfc231b190a27f8c1a5dbd"},
    :after
    {:start-byte 0,
     :end-byte 15844,
     :start-line 1,
     :end-line 119,
     :sha256
     "5695e3359c2df12471d72670b7b322986263cedb7eba3e85e612081ea633348d"}}],
  :live-sha256
  "5695e3359c2df12471d72670b7b322986263cedb7eba3e85e612081ea633348d",
  :applying-blob "21d5db8ee470733955fc5e8281cbbac74b09d846",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-diag-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/diag-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "0ab4aa75a0f694cfad605b7f959acc89d471ee7eb1cfc231b190a27f8c1a5dbd",
  :baseline-blob "f3c4685dfa534e7ed8ff88040bc66917ebaf5c53"}
 {:role :current-source-interface-proposal,
  :path "specs/effects-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/effects-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 44810,
     :start-line 1,
     :end-line 503,
     :sha256
     "0364c080cc8f141e7d4b33e8a7042763663539a84a06817c62a8adf3532e6005"},
    :after
    {:start-byte 0,
     :end-byte 45410,
     :start-line 1,
     :end-line 510,
     :sha256
     "3f086260dfdda82e4fd7a8dc06b11328fa713318a4e954cd87a5290019951ecf"}}],
  :live-sha256
  "3f086260dfdda82e4fd7a8dc06b11328fa713318a4e954cd87a5290019951ecf",
  :applying-blob "0f1c2d796f322f451c999c6b429a66374ce566e7",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-effects-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/effects-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "0364c080cc8f141e7d4b33e8a7042763663539a84a06817c62a8adf3532e6005",
  :baseline-blob "82b8db0ca49c7f78edb0a2738a45e448de4cbbc6"}
 {:role :current-source-interface-proposal,
  :path "specs/effects-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/effects-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 32799,
     :start-line 1,
     :end-line 236,
     :sha256
     "753a8a59e29deccfee5b4b141a2114f280fc52c3d618527027e0bcf89392fd52"},
    :after
    {:start-byte 0,
     :end-byte 34088,
     :start-line 1,
     :end-line 244,
     :sha256
     "59aeac1148be9e492462c0834008e62a0d980954a96d1e3b7889936af7bd4913"}}],
  :live-sha256
  "59aeac1148be9e492462c0834008e62a0d980954a96d1e3b7889936af7bd4913",
  :applying-blob "7b855d0e04658fe5392239523ac0486a1a1e2b9c",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-effects-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/effects-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "753a8a59e29deccfee5b4b141a2114f280fc52c3d618527027e0bcf89392fd52",
  :baseline-blob "a0b9c482bca84847ed46cdfa015610889a6ad290"}
 {:role :current-source-interface-proposal,
  :path "specs/evolution-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/evolution-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 17632,
     :start-line 1,
     :end-line 190,
     :sha256
     "2b10c83aee87ffcc1a8a858c8c3cb57cf8b100f8773b5629abce85265753db56"},
    :after
    {:start-byte 0,
     :end-byte 18234,
     :start-line 1,
     :end-line 196,
     :sha256
     "f7ed753974edbba8b8b5b51244a24acb265ac03c7d5f6d8a8c473aa37a389182"}}],
  :live-sha256
  "f7ed753974edbba8b8b5b51244a24acb265ac03c7d5f6d8a8c473aa37a389182",
  :applying-blob "b07fc872f2516ea3b7cf247660cdf5d2a54073d3",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-evolution-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/evolution-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "2b10c83aee87ffcc1a8a858c8c3cb57cf8b100f8773b5629abce85265753db56",
  :baseline-blob "68552848d86e8b6f93d31a5049197daa5cbb390b"}
 {:role :current-source-interface-proposal,
  :path "specs/expand-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/expand-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 77744,
     :start-line 1,
     :end-line 587,
     :sha256
     "2429ede413743211cf939ae969ee7a9d96db058f52f3800d5dcd1c0820af0abe"},
    :after
    {:start-byte 0,
     :end-byte 78334,
     :start-line 1,
     :end-line 594,
     :sha256
     "4d262903ea074ac5def9ffa2c63635776df291dba0f8dd1d0184fe49386420fd"}}],
  :live-sha256
  "4d262903ea074ac5def9ffa2c63635776df291dba0f8dd1d0184fe49386420fd",
  :applying-blob "f665f72241e06a4d90e6a56e8ac564a6bd415759",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-expand-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/expand-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "2429ede413743211cf939ae969ee7a9d96db058f52f3800d5dcd1c0820af0abe",
  :baseline-blob "843809051226666787fe47e3cacfa2aacaefe0af"}
 {:role :current-source-interface-proposal,
  :path "specs/legality-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/legality-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 92271,
     :start-line 1,
     :end-line 1032,
     :sha256
     "347a9d2d863700faaf97a2a6c913fc841854700dab4a513a031cb5aeb9bcbb6d"},
    :after
    {:start-byte 0,
     :end-byte 92851,
     :start-line 1,
     :end-line 1039,
     :sha256
     "0a2fef2e6cd2cb4c8d008726be5c9c2c9e2b6dd7f7fc0ca0cf712d73bbde3392"}}],
  :live-sha256
  "0a2fef2e6cd2cb4c8d008726be5c9c2c9e2b6dd7f7fc0ca0cf712d73bbde3392",
  :applying-blob "1cd71fc26d5fb575e6218f2428fe47ae95371b2f",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-legality-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/legality-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "347a9d2d863700faaf97a2a6c913fc841854700dab4a513a031cb5aeb9bcbb6d",
  :baseline-blob "153b922cce858ca37492aa72572e834b6065ccc3"}
 {:role :current-source-interface-proposal,
  :path "specs/legality-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/legality-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 123915,
     :start-line 1,
     :end-line 624,
     :sha256
     "2f0435aac8e3c74f58d0aba4e7c440e16bf0065323aed7e67e89be43fffe84eb"},
    :after
    {:start-byte 0,
     :end-byte 125266,
     :start-line 1,
     :end-line 632,
     :sha256
     "40062124ec129164cb77d24a7bfc1407227355c22ea7a8e77d284ed78db2980a"}}],
  :live-sha256
  "40062124ec129164cb77d24a7bfc1407227355c22ea7a8e77d284ed78db2980a",
  :applying-blob "a17283368ade8349f962c03c40a85d8bc78b0b9d",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-legality-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/legality-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "2f0435aac8e3c74f58d0aba4e7c440e16bf0065323aed7e67e89be43fffe84eb",
  :baseline-blob "714d16623a9f8cc6657e45a6d3eefd3405c748ce"}
 {:role :current-source-interface-proposal,
  :path "specs/opt-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/opt-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 21102,
     :start-line 1,
     :end-line 233,
     :sha256
     "c3a25ee646489793dffa2c750ea28b0619d3ead4bc63cd71e85b040539aa7c42"},
    :after
    {:start-byte 0,
     :end-byte 21715,
     :start-line 1,
     :end-line 240,
     :sha256
     "6c9ef876e8290819dd034ea60a94be54c723c74391aabc4b13e6c53bb9935910"}}],
  :live-sha256
  "6c9ef876e8290819dd034ea60a94be54c723c74391aabc4b13e6c53bb9935910",
  :applying-blob "9d04daad2cee4387cef1efb6836bee11d48aeebd",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-opt-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/opt-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "c3a25ee646489793dffa2c750ea28b0619d3ead4bc63cd71e85b040539aa7c42",
  :baseline-blob "40fa4b9294b5e33e86d5a3dcce36f2186136979f"}
 {:role :current-source-interface-proposal,
  :path "specs/opt-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/opt-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 14572,
     :start-line 1,
     :end-line 123,
     :sha256
     "4d9ddc9361a6f5338bd2867f37459b604bc64ec92caa9d696052c7928f12903d"},
    :after
    {:start-byte 0,
     :end-byte 15884,
     :start-line 1,
     :end-line 131,
     :sha256
     "d66dca422fe4688e323521761a33869ccd99a2bfecfc388d7be9f23b2e54714c"}}],
  :live-sha256
  "d66dca422fe4688e323521761a33869ccd99a2bfecfc388d7be9f23b2e54714c",
  :applying-blob "14113f1844ba8cb2533321e9359dc985fb587f05",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-opt-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/opt-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "4d9ddc9361a6f5338bd2867f37459b604bc64ec92caa9d696052c7928f12903d",
  :baseline-blob "f10d98ef4c2700541e73cbe4a5a07821abc8fd27"}
 {:role :current-source-interface-proposal,
  :path "specs/release-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/release-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 35941,
     :start-line 1,
     :end-line 390,
     :sha256
     "1fe02987c27cf42ebf7190b929551664de91b652ea8208c10aa4a8b60ec55f42"},
    :after
    {:start-byte 0,
     :end-byte 36577,
     :start-line 1,
     :end-line 397,
     :sha256
     "587d5a98bbfdf42c90b655e133f1310d1d7591c940fe6c3dd00d03ac87a5cc2a"}}],
  :live-sha256
  "587d5a98bbfdf42c90b655e133f1310d1d7591c940fe6c3dd00d03ac87a5cc2a",
  :applying-blob "92ed8a4228d0a280167597c9575b3e483892ad17",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-release-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/release-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "1fe02987c27cf42ebf7190b929551664de91b652ea8208c10aa4a8b60ec55f42",
  :baseline-blob "8083d042621a2ed298a33a688ff287478e33da76"}
 {:role :current-source-interface-proposal,
  :path "specs/resolve-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/resolve-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 79913,
     :start-line 1,
     :end-line 460,
     :sha256
     "5786a4ad3dd83a6ec93e40539d70d154b3250493cc05d23a79c6e978c7a26bae"},
    :after
    {:start-byte 0,
     :end-byte 80566,
     :start-line 1,
     :end-line 467,
     :sha256
     "f3345068597572cabc8b2a2cbc4649bca8123a58900a697c541e0a5beef5cc5a"}}],
  :live-sha256
  "f3345068597572cabc8b2a2cbc4649bca8123a58900a697c541e0a5beef5cc5a",
  :applying-blob "5382df80049f05210c4ed3702c5fbbf6f73fd42f",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-resolve-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/resolve-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "5786a4ad3dd83a6ec93e40539d70d154b3250493cc05d23a79c6e978c7a26bae",
  :baseline-blob "5e7546d272564e07e6b53bb83604c6da93ad2677"}
 {:role :current-source-interface-proposal,
  :path "specs/resolve-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/resolve-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 17589,
     :start-line 1,
     :end-line 155,
     :sha256
     "226a95f25d40367b897db1aa2935bc45d545ab673ae4084a71181b6f5fd9e838"},
    :after
    {:start-byte 0,
     :end-byte 18891,
     :start-line 1,
     :end-line 163,
     :sha256
     "9b8dc5df7eb26303debc6a54f54df13e97edab8482e78804569bcb5519e65fc8"}}],
  :live-sha256
  "9b8dc5df7eb26303debc6a54f54df13e97edab8482e78804569bcb5519e65fc8",
  :applying-blob "cea1bd281528f5e84831cd676c4b48a48036c6df",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-resolve-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/resolve-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "226a95f25d40367b897db1aa2935bc45d545ab673ae4084a71181b6f5fd9e838",
  :baseline-blob "177f74794a67b2cc2f16ab1c45ec8da83dbb7407"}
 {:role :current-source-interface-proposal,
  :path "specs/stdlib-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/stdlib-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 27646,
     :start-line 1,
     :end-line 238,
     :sha256
     "e6f1cb158fc406ebb6cb4b506a8a81a68192ff62f7e89abf08c65f0c90c62ef5"},
    :after
    {:start-byte 0,
     :end-byte 37921,
     :start-line 1,
     :end-line 288,
     :sha256
     "734c723398c8e42946633d32ed32ecf99daf4e22b029b14732cf08c7986399f8"}}],
  :live-sha256
  "734c723398c8e42946633d32ed32ecf99daf4e22b029b14732cf08c7986399f8",
  :applying-blob "c960d1a487d87eaebf774512bfb39e0ed5302d69",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-stdlib-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/stdlib-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "e6f1cb158fc406ebb6cb4b506a8a81a68192ff62f7e89abf08c65f0c90c62ef5",
  :baseline-blob "f6036c3d503c7efe73132c63451e9ab607526762"}
 {:role :current-source-interface-proposal,
  :path "specs/stdlib-workflow/spec.md",
  :changed-spans
  [{:id "L13/specs/stdlib-workflow/spec.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 14135,
     :start-line 1,
     :end-line 138,
     :sha256
     "6e408abbd3dc98601395b50858addd16cdb6da71d009a2e1be154ee5e43e1057"},
    :after
    {:start-byte 0,
     :end-byte 14689,
     :start-line 1,
     :end-line 140,
     :sha256
     "aadd79b14a6e1312bd225421d1b59f24f3da27371fcfd744c5fc31b43da4f232"}}],
  :live-sha256
  "aadd79b14a6e1312bd225421d1b59f24f3da27371fcfd744c5fc31b43da4f232",
  :applying-blob "8107efb4c0d1d448867accc9104b06e2d824c2b8",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-stdlib-workflow-spec-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/stdlib-workflow/spec.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "6e408abbd3dc98601395b50858addd16cdb6da71d009a2e1be154ee5e43e1057",
  :baseline-blob "4bf6511ce08722c07fa25a0fb18e54ab4f1f17a2"}
 {:role :current-source-interface-proposal,
  :path "specs/stdlib-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/stdlib-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 16965,
     :start-line 1,
     :end-line 133,
     :sha256
     "3c863b16b7798f72aa50b9297ed126c044d7357b7002bc0ef789d65fba261055"},
    :after
    {:start-byte 0,
     :end-byte 38854,
     :start-line 1,
     :end-line 276,
     :sha256
     "ecbf84050a1c0bec21ab3a83fbbe3ed7e55d67fd8a08fff42fdbaccfaed01bcb"}}],
  :live-sha256
  "ecbf84050a1c0bec21ab3a83fbbe3ed7e55d67fd8a08fff42fdbaccfaed01bcb",
  :applying-blob "e2933a343359c2032086e34c996d02393e86b355",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-stdlib-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/stdlib-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "3c863b16b7798f72aa50b9297ed126c044d7357b7002bc0ef789d65fba261055",
  :baseline-blob "256b28deeca97929fe7240c1b8672ce42dbf3d2a"}
 {:role :current-source-interface-proposal,
  :path "specs/types-workflow/plan.md",
  :changed-spans
  [{:id "L13/specs/types-workflow/plan.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 52460,
     :start-line 1,
     :end-line 357,
     :sha256
     "cc0b0038c6f0b7849ca37bf8f682bdd63f24c272f7cd2ffa60e05bc5c9c8baf8"},
    :after
    {:start-byte 0,
     :end-byte 53049,
     :start-line 1,
     :end-line 364,
     :sha256
     "b586691e2a0e437de9abba206f21aa799286994741d60cd6b3b1f3f4b65e0827"}}],
  :live-sha256
  "b586691e2a0e437de9abba206f21aa799286994741d60cd6b3b1f3f4b65e0827",
  :applying-blob "d3f15f60e2886d993275db9a9871c6d8466c5158",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-types-workflow-plan-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/types-workflow/plan.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "cc0b0038c6f0b7849ca37bf8f682bdd63f24c272f7cd2ffa60e05bc5c9c8baf8",
  :baseline-blob "8f275c3786fcc52977ef8296fdee10a680fb2f39"}
 {:role :current-source-interface-proposal,
  :path "specs/types-workflow/tasks.md",
  :changed-spans
  [{:id "L13/specs/types-workflow/tasks.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 46760,
     :start-line 1,
     :end-line 299,
     :sha256
     "95b9209126992f006296a090569d10b22b7dac68f6c6883c63ab7f3d78827022"},
    :after
    {:start-byte 0,
     :end-byte 48032,
     :start-line 1,
     :end-line 307,
     :sha256
     "0ba3ef45bdaf8de19b9f1027463f7d8b1d98a08e059cee34f92fcdc27ff82acc"}}],
  :live-sha256
  "0ba3ef45bdaf8de19b9f1027463f7d8b1d98a08e059cee34f92fcdc27ff82acc",
  :applying-blob "ba31872977698abe7e6ec91403f2a2092f2d7656",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "3f2115bfbf04abda8aec36b6a8edee5f5b72e9f5",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-specs-types-workflow-tasks-md-source-interface",
  :applying-ref "485cfd2c54c6d1a2695cbd7e8e34495fbeb34799",
  :qualification
  "Only the complete authenticated L13 source file specs/types-workflow/tasks.md is transferred from the b886 current-source frame to the reviewed 485 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "95b9209126992f006296a090569d10b22b7dac68f6c6883c63ab7f3d78827022",
  :baseline-blob "816b12cfff602f21d22730ad0da486c12450a945"}
 {:role :current-source-interface-proposal,
  :path "docs/acceptance-dependencies.md",
  :changed-spans
  [{:id "L13/docs/acceptance-dependencies.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 535278,
     :start-line 1,
     :end-line 703,
     :sha256
     "765e1ea7aaec6c263d6bec7e2bbfd0cf71f6238cca5e4b80f65bbc64c0379a94"},
    :after
    {:start-byte 0,
     :end-byte 539166,
     :start-line 1,
     :end-line 706,
     :sha256
     "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e"}}],
  :live-sha256
  "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e",
  :applying-blob "bb078aba5c7663af33080584953ef3c238c4bb0b",
  :review-state :pending-independent-source-version-review,
  :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
  :authority :current-source-interface-only,
  :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
  :applying-tree "98c688013460d4aeddf090fadd7ec0c01d6d45d1",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "l13-docs-acceptance-dependencies-md-source-interface",
  :applying-ref "f79c62bcfc33f7aa645a455e0a98f32f0e008b6b",
  :qualification
  "Only the complete authenticated L13 source file docs/acceptance-dependencies.md is transferred from the b886 current-source frame to the reviewed f79 source bytes. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this record confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "765e1ea7aaec6c263d6bec7e2bbfd0cf71f6238cca5e4b80f65bbc64c0379a94",
  :baseline-blob "9a31beeaff7e83413085f43826fd55e47e393d7f"}])
(def current-source-transition-paths
  (set (map :path current-source-transition-contracts)))

(declare validate-snapshots!)

(defn- utf8-span-text [text start-byte end-byte require!]
  (let [bytes (.getBytes ^String text "UTF-8")]
    (require! (and (integer? start-byte) (integer? end-byte)
                   (<= 0 start-byte end-byte (alength bytes)))
              "Current source transition has an invalid UTF-8 byte span")
    (String. (java.util.Arrays/copyOfRange bytes start-byte end-byte) "UTF-8")))

(defn- utf8-line-number [text offset]
  (let [bytes (.getBytes ^String text "UTF-8")]
    (loop [index 0
           line 1]
      (if (< index offset)
        (recur (inc index)
               (if (= 10 (aget ^bytes bytes index))
                 (inc line)
                 line))
        line))))

(defn- replace-utf8-span [text span replacement require!]
  (let [bytes (.getBytes ^String text "UTF-8")
        start (:start-byte span)
        end (:end-byte span)]
    (require! (and (integer? start) (integer? end)
                   (<= 0 start end (alength bytes)))
              "Current source transition replacement span is invalid")
    (str (String. (java.util.Arrays/copyOfRange bytes 0 start) "UTF-8")
         replacement
         (String. (java.util.Arrays/copyOfRange bytes end (alength bytes)) "UTF-8"))))

(defn validate-current-source-transition!
  "Authenticate one exact current-source file transition.

  The expected contract is deliberately supplied by code, while the EDN
  record supplies the review payload. Equality against that contract prevents
  a caller from relabeling a foreign source, role, span or authority. The
  whole-file identities and a byte-level replacement check then ensure the
  named spans account for the complete before/after delta."
  [transition expected read-ref read-text sha256 require!]
  (require! (= transition expected)
            (str "Current source transition identity or qualification changed: "
                 (:path transition)))
  (require! (= current-source-transition-kind (:kind transition))
            "Current source transition kind changed")
  (require! (= :current-source-interface-proposal (:role transition))
            "Current source transition role is not a current-source proposal")
  (require! (and (string? (:path transition))
                 (not (.isAbsolute (io/file (:path transition))))
                 (not (str/blank? (:document-id transition))))
            "Current source transition path/document identity is invalid")
  (validate-snapshots! transition read-ref sha256 require!)
  (let [path (:path transition)
        before-source (read-ref (:baseline-ref transition) path)
        after-source (read-ref (:applying-ref transition) path)
        current (read-text path)
        spans (:changed-spans transition)]
    (require! (= (:baseline-sha256 transition) (sha256 before-source))
              "Current source transition baseline bytes changed")
    (require! (= (:live-sha256 transition) (sha256 after-source))
              "Current source transition applying bytes changed")
    (require! (= after-source current)
              "Current source transition is not bound to the checked-out current source")
    (require! (and (vector? spans) (seq spans)
                   (= (count spans) (count (set (map :id spans)))))
              "Current source transition changed-span identities are incomplete")
    (doseq [{:keys [id kind before after]} spans]
      (require! (and (string? id) (keyword? kind) (map? before) (map? after))
                "Current source transition changed span is malformed")
      (let [before-text (utf8-span-text
                         before-source
                         (:start-byte before) (:end-byte before) require!)
            after-text (utf8-span-text
                        after-source
                        (:start-byte after) (:end-byte after) require!)]
        (require! (= (:sha256 before) (sha256 before-text))
                  (str "Current source transition before-span digest changed: " id))
        (require! (= (:sha256 after) (sha256 after-text))
                  (str "Current source transition after-span digest changed: " id))
        (doseq [[label expected-line source-text offset] [["before-start" (:start-line before) before-source (:start-byte before)]
                                                          ["before-end" (:end-line before) before-source (dec (:end-byte before))]
                                                          ["after-start" (:start-line after) after-source (:start-byte after)]
                                                          ["after-end" (:end-line after) after-source (dec (:end-byte after))]]]
          (when (and (some? expected-line) (pos? (count (utf8-span-text source-text
                                                                        (if (= source-text before-source) (:start-byte before) (:start-byte after))
                                                                        (if (= source-text before-source) (:end-byte before) (:end-byte after))
                                                                        require!))))
            (require! (= expected-line (utf8-line-number source-text offset))
                      (str "Current source transition " label " line changed: " id))))
        (when (= :task-row kind)
          (require! (str/includes? before-text (str "`" id "`"))
                    (str "Current source transition before row identity changed: " id))
          (require! (str/includes? after-text (str "`" id "`"))
                    (str "Current source transition after row identity changed: " id))
          (require! (re-find #"(?m)^- \[[ xX]\] `[^`]+`" after-text)
                    (str "Current source transition after row marker changed: " id)))
        (when (= :plan-section kind)
          (require! (str/starts-with? after-text "## Complete library-principles source interface")
                    (str "Current source transition plan section changed: " id)))
        (require! (= after-source
                     (replace-utf8-span before-source before after-text require!))
                  (str "Current source transition has unlisted file drift: " id))))
    {:kind (:kind transition)
     :role (:role transition)
     :path path
     :document-id (:document-id transition)
     :baseline-ref (:baseline-ref transition)
     :baseline-tree (:baseline-tree transition)
     :applying-ref (:applying-ref transition)
     :applying-tree (:applying-tree transition)
     :changed-span-ids (mapv :id spans)
     :snapshot-provenance :git-bytes-verified
     :span-provenance :git-byte-and-line-spans-verified
     :authority :current-source-interface-only
     :implementation-acceptance :unassessed}))

;; These are the six reviewed source regions, not a text diff computed from the
;; checkout.  The exact baseline/live fragment hashes make an omitted hunk,
;; retained-semantic edit, or additional source mutation fail closed.
(def std3-accounting-projection-hunks
  [{:lineage-id
    "STD3/source/c0d68f7417c5026e583fc882d1ef84f94a93602a1917ee86bafd7bfa24700add"
    :facet-ids ["library/STD3/facet-lines-112-124"]
    :baseline-start-line 112 :baseline-end-line 124
    :live-start-line 112 :live-end-line 121
    :baseline-fragment-sha256
    "04048bcc61900fe2b948e68cbb284907e9d08d84ea13fceae5a48941de81bac0"
    :live-fragment-sha256
    "6f50c16e7bf77c39b55fc9380cda6838d8a8e4dd9024567f1f92182bbf8a5cab"}
   {:lineage-id
    "STD3/source/aecb86c0a5694c16e471c5ac4121a538eb02797764f2286b72425433778d820a"
    :facet-ids ["library/STD3/facet-lines-313-316"]
    :baseline-start-line 313 :baseline-end-line 316
    :live-start-line 310 :live-end-line 313
    :baseline-fragment-sha256
    "6c96de00b8181ff5c8f317e6a32966a6bc0f903e0a5a85d0084817a645236627"
    :live-fragment-sha256
    "f29ac12289fd27f2fff92db7e70e36390408a026b0c0d14d1ff7113b7ae5cedc"}
   {:lineage-id
    "STD3/source/fb024105ccc2bca425c08ad66b8b4b712b61b252baa8ad1946812879e1f0976e"
    :facet-ids ["library/STD3/facet-lines-332-341"]
    :baseline-start-line 332 :baseline-end-line 341
    :live-start-line 329 :live-end-line 338
    :baseline-fragment-sha256
    "cb7faced2f9c61f9c9ee5c2d0faaad4aac8648b47a8816c854d314ff5525018a"
    :live-fragment-sha256
    "b650cc70ac19658e03bd75691c062bf8a75f25563b28562e0873a95c152d487f"}
   {:lineage-id
    "STD3/source/d06b51d0b75765c6d2636f3768c83ac62d6915344d229bdc028174dcb09c0224"
    :facet-ids ["library/STD3/facet-lines-353-357"]
    :baseline-start-line 353 :baseline-end-line 357
    :live-start-line 350 :live-end-line 354
    :baseline-fragment-sha256
    "6db8045cb192b447aaab6e5429cd85c8a0ddd7b560fc937ad5e1434b5ad8f27c"
    :live-fragment-sha256
    "8fba07359e9bf3b800e4685897a3dd418ad7d2cafd8b0fa9b71f05a5acddad2b"}
   {:lineage-id
    "STD3/source/138372855ff20dec2623a643a41bc8891a601b7251a67711d7a9342035961ddb"
    :facet-ids ["library/STD3/facet-lines-359-366"]
    :baseline-start-line 359 :baseline-end-line 366
    :live-start-line 356 :live-end-line 363
    :baseline-fragment-sha256
    "ad4982e5c6a9cfb0067db0c580923f7240394e3782dac683f340b483a99015a9"
    :live-fragment-sha256
    "1d951bbba50073fc1351c0a3637e93ec01aa129acd7c5ae68d20410cf3aabecb"}
   {:lineage-id
    "STD3/source/21f80d05376fa7244a3b47ce45415409177a7da74f97d0e412d92244394dd4e8"
    :facet-ids (mapv #(format "library/STD3/facet-lines-%d-%d" % %)
                     (range 497 528))
    :baseline-start-line 497 :baseline-end-line 527
    :live-start-line 494 :live-end-line 524
    :baseline-fragment-sha256
    "ac7dc6454f761f0572ad2407a78baf4bd395a85680f2ad5ce44082b7cc25c7f4"
    :live-fragment-sha256
    "ffca258d344464aa30dd1107d91c940106f89a1d4858c18beb3ccc8f55ac15f6"}])

(defn- canonical-value [value]
  (cond
    (map? value) (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
                       (map (fn [[k v]] [k (canonical-value v)]) value))
    (vector? value) (mapv canonical-value value)
    (set? value) (vec (sort-by pr-str (map canonical-value value)))
    (sequential? value) (mapv canonical-value value)
    :else value))

(defn- canonical-digest [sha256 value]
  (sha256 (pr-str (canonical-value value))))

(defn- expected-std3-bridge? [bridge]
  (and (= std3-accounting-kind (:kind bridge))
       (= "STD3" (:document-id bridge))
       (= std3-accounting-path (:path bridge))
       (= std3-accounting-baseline-ref (:baseline-ref bridge))
       (= std3-accounting-applying-ref (:applying-ref bridge))
       (= std3-accounting-baseline-sha256 (:baseline-sha256 bridge))
       (= std3-accounting-live-sha256 (:live-sha256 bridge))
       (= "ca89df08c128c25d23a4a0781da79dd5e37abd74" (:baseline-blob bridge))
       (= "19581f5720fea820ff97a1afc392cf3446f44bc3" (:applying-blob bridge))
       (= :proposed (:review-state bridge))
       (= std3-accounting-authority (:authority bridge))
       (= std3-accounting-qualification (:qualification bridge))
       (= std3-accounting-semantic-deltas (:semantic-deltas bridge))
       (= std3-accounting-lineage-record-digest (:lineage-record-digest bridge))
       (= std3-accounting-projection-hunks (:projection-hunks bridge))))

(defn- line-segments [text]
  (let [parts (str/split text #"\n" -1)
        terminal-newline? (= "" (last parts))
        content (if terminal-newline? (butlast parts) parts)
        n (count content)]
    (mapv (fn [[idx line]]
            (str line (when (or terminal-newline? (< idx (dec n))) "\n")))
          (map-indexed vector content))))

(defn- line-fragment [text start-line end-line require!]
  (let [segments (line-segments text)]
    (if (= end-line (dec start-line))
      (do
        (require! (and (integer? start-line) (integer? end-line)
                       (pos? start-line) (<= start-line (inc (count segments))))
                  "Source projection empty line span is outside the authenticated snapshot")
        "")
      (do
        (require! (and (integer? start-line) (integer? end-line)
                       (pos? start-line) (<= start-line end-line) (<= end-line (count segments)))
                  "Source projection line span is outside the authenticated snapshot")
        (apply str (subvec segments (dec start-line) end-line))))))

(defn- replace-line-fragment [text start-line end-line replacement require!]
  (let [segments (line-segments text)]
    (require! (and (pos? start-line) (<= start-line end-line) (<= end-line (count segments)))
              "Source projection replacement span is outside the live source")
    (apply str (concat (subvec segments 0 (dec start-line))
                       replacement
                       (subvec segments end-line)))))

(defn- std3-lineage-records [manifest sha256 require!]
  (let [records (get-in manifest [:stdlib-reconciliation-context :accounting-lineage :mixed-std3-paragraphs])
        by-id (into {} (map (juxt :id identity) records))]
    (require! (= (count records) (count by-id)) "Duplicate STD3 accounting lineage ID")
    (require! (= (set (keys by-id)) (set (map :lineage-id std3-accounting-projection-hunks)))
              "STD3 accounting lineage contains an unprojected or missing region")
    (doseq [{:keys [lineage-id facet-ids baseline-start-line baseline-end-line
                    baseline-fragment-sha256]} std3-accounting-projection-hunks]
      (let [record (get by-id lineage-id)]
        (require! (and record
                       (= "STD3" (:document-id record))
                       (= std3-accounting-baseline-sha256 (:source-sha256 record))
                       (= :source-block (:structural-kind record))
                       (= :mixed-policy-supersession (:disposition record))
                       (= baseline-start-line (:start-line record))
                       (= baseline-end-line (:end-line record))
                       (= baseline-fragment-sha256 (:raw-sha256 record))
                       (= facet-ids (:facet-ids record))
                       (#{:proposed :reviewed} (:review-state record))
                       (vector? (:authority-source-occurrence-ids record))
                       (seq (:authority-source-occurrence-ids record))
                       (string? (:rationale record))
                       (map? (:specific-fragment-proposal record))
                       (vector? (:unit-ids record))
                       (vector? (:shared-facet-set-ids record)))
                  (str "STD3 accounting lineage is incomplete or changed: " lineage-id))))
    (let [bridge (first (filter #(= std3-accounting-kind (:kind %))
                                (:source-version-bridges manifest)))
          selected (mapv #(get by-id (:lineage-id %)) std3-accounting-projection-hunks)]
      (require! (= std3-accounting-lineage-record-digest (:lineage-record-digest bridge))
                "STD3 accounting lineage metadata is not the trusted canonical digest")
      (require! (= std3-accounting-lineage-record-digest
                   (canonical-digest sha256 selected))
                "STD3 accounting lineage metadata digest differs")
      selected)))

(defn- std3-accounting-view [manifest bridge text baseline sha256 require! verified?]
  (require! (expected-std3-bridge? bridge) "STD3 accounting bridge identity or projection contract changed")
  (require! (= (:source-baseline manifest) (:baseline-ref bridge))
            "STD3 accounting bridge is detached from the manifest source baseline")
  (std3-lineage-records manifest sha256 require!)
  (require! (string? baseline) "STD3 accounting bridge requires a verified baseline snapshot")
  (require! (= std3-accounting-baseline-sha256 (sha256 baseline))
            "STD3 accounting baseline snapshot bytes differ from the declared source")
  (require! (= std3-accounting-live-sha256 (sha256 text))
            "STD3 accounting live source bytes differ from the declared applying snapshot")
  (let [projected
        (reduce (fn [current hunk]
                  (let [live-fragment (line-fragment current (:live-start-line hunk)
                                                     (:live-end-line hunk) require!)
                        baseline-fragment (line-fragment baseline (:baseline-start-line hunk)
                                                         (:baseline-end-line hunk) require!)]
                    (require! (= (:live-fragment-sha256 hunk) (sha256 live-fragment))
                              "STD3 accounting bridge encountered unlisted or retained-semantic live changes")
                    (require! (= (:baseline-fragment-sha256 hunk) (sha256 baseline-fragment))
                              "STD3 accounting bridge baseline facet bytes differ")
                    (replace-line-fragment current (:live-start-line hunk) (:live-end-line hunk)
                                           (line-segments baseline-fragment) require!)))
                text
                (sort-by :live-start-line > std3-accounting-projection-hunks))]
    (require! (= std3-accounting-baseline-sha256 (sha256 projected))
              "STD3 accounting projection leaves unlisted source drift")
    {:text projected
     :bridge (assoc bridge :span-basis :authenticated-stdlib-baseline-view
                    :snapshot-provenance (if verified? :git-bytes-verified :unverified))}))

(defn- replace-generated-region [text marker replacement require!]
  (let [start-marker (str "<!-- " marker ":start -->")
        end-marker (str "<!-- " marker ":end -->")
        starts (count (re-seq (re-pattern (java.util.regex.Pattern/quote start-marker)) text))
        ends (count (re-seq (re-pattern (java.util.regex.Pattern/quote end-marker)) text))]
    (require! (and (= 1 starts) (= 1 ends))
              (str "Generated platform view region is missing or duplicated: " marker))
    (let [start (.indexOf text start-marker)
          end (.indexOf text end-marker (+ start (count start-marker)))
          after-end (+ end (count end-marker))
          after-end (if (and (< after-end (count text))
                            (= \newline (.charAt text after-end)))
                      (inc after-end)
                      after-end)]
      (require! (and (<= 0 start) (< start end))
                (str "Generated platform view region is reversed: " marker))
      (str (subs text 0 start) replacement (subs text after-end)))))

(defn- normalize-platform-generated-view [text require!]
  (let [instruction (:instructions platform-generated-view-contract)
        parts (str/split text (re-pattern (java.util.regex.Pattern/quote instruction)) -1)]
    (require! (= 2 (count parts))
              "Generated platform view instruction is missing or duplicated")
    (reduce (fn [current {:keys [marker replacement]}]
              (replace-generated-region current marker replacement require!))
            (apply str parts)
            (:regions platform-generated-view-contract))))

(defn- exact-source-bridge? [bridge]
  (let [expected (get exact-source-bridge-contracts (:kind bridge))]
    (and expected
         (every? (fn [k] (= (get expected k) (get bridge k)))
                 (cond-> [:document-id :path :baseline-ref :applying-ref
                          :baseline-sha256 :live-sha256 :baseline-blob :applying-blob
                          :review-state :authority :qualification :semantic-deltas
                          :projection-hunks]
                   (#{math4-search-policy-kind math9-rewrite-termination-kind} (:kind bridge))
                   (conj :baseline-tree :applying-tree :current-criteria)))
         (or (not= platform-generated-view-kind (:kind bridge))
             (= (:generated-view (get exact-source-bridge-contracts (:kind bridge)))
                (:generated-view bridge))))))

(defn- exact-source-view [manifest bridge text baseline sha256 require! verified?]
  (require! (exact-source-bridge? bridge)
            (str "Exact source bridge identity or semantic qualification changed: " (:document-id bridge)))
  (require! (= (:source-baseline manifest) (:baseline-ref bridge))
            "Exact source bridge is detached from the manifest source baseline")
  (require! (and (string? baseline) (= (:baseline-sha256 bridge) (sha256 baseline)))
            "Exact source bridge baseline snapshot bytes differ")
  (require! (= (:live-sha256 bridge) (sha256 text))
            "Exact source bridge live source bytes differ")
  (let [declared-facets (set (map :id (:source-facets manifest)))]
    (doseq [{:keys [facet-ids]} (:projection-hunks bridge)]
      (require! (and (vector? facet-ids) (seq facet-ids)
                     (set/subset? (set facet-ids) declared-facets))
                "Exact source bridge references an undeclared historical facet")))
  (let [source-text (if (= platform-generated-view-kind (:kind bridge))
                      (normalize-platform-generated-view text require!)
                      text)
        projected
        (reduce (fn [current hunk]
                  (let [live-fragment (line-fragment current (:live-start-line hunk)
                                                     (:live-end-line hunk) require!)
                        baseline-fragment (line-fragment baseline (:baseline-start-line hunk)
                                                         (:baseline-end-line hunk) require!)]
                    (require! (= (:live-fragment-sha256 hunk) (sha256 live-fragment))
                              "Exact source bridge encountered unlisted or retained-semantic live changes")
                    (require! (= (:baseline-fragment-sha256 hunk) (sha256 baseline-fragment))
                              "Exact source bridge baseline facet bytes differ")
                    (replace-line-fragment current (:live-start-line hunk) (:live-end-line hunk)
                                           (line-segments baseline-fragment) require!)))
                source-text
                (sort-by :live-start-line > (:projection-hunks bridge)))]
    (require! (= (:baseline-sha256 bridge) (sha256 projected))
              "Exact source bridge projection leaves unlisted source drift")
    {:text projected
     :bridge (assoc bridge :span-basis :authenticated-source-baseline-view
                    :snapshot-provenance (if verified? :git-bytes-verified :unverified))}))

(defn remove-once [text addition require!]
  (let [parts (str/split text (re-pattern (java.util.regex.Pattern/quote addition)) -1)]
    (require! (= 2 (count parts)) "Source transfer addition is missing or duplicated")
    (apply str parts)))

(defn view
  ([manifest document text sha256 require!]
   (view manifest document text sha256 require! {}))
  ([manifest document text sha256 require! {:keys [baseline-texts verified-baselines?]}]
   (let [live-sha (sha256 text)]
     (if (or (nil? document) (= (:sha256 document) live-sha))
       {:text text}
       (let [bridges (filter #(= (:id document) (:document-id %)) (:source-version-bridges manifest))
             b (first bridges)]
         (cond
           (and (= 1 (count bridges)) (= std3-accounting-kind (:kind b)))
           (std3-accounting-view manifest b text (get baseline-texts (:id document)) sha256 require!
                                  (contains? verified-baselines? (:id document)))

           (and (= 1 (count bridges)) (exact-source-projection-kind? (:kind b)))
           (exact-source-view manifest b text (get baseline-texts (:id document)) sha256 require!
                              (contains? verified-baselines? (:id document)))

           :else
           (do
             (require! (and (= 1 (count bridges)) (= :platform-task-authority-layout-only (:kind b))
                            (= "platform-product" (:id document))
                            (= "docs/component-roadmaps/platform.md" (:path document))
                            (= (:source-baseline manifest) (:baseline-ref b))
                            (= (:sha256 document) (:baseline-sha256 b)) (= live-sha (:live-sha256 b))
                            (re-matches #"[0-9a-f]{40}" (or (:applying-ref b) ""))
                            (#{:proposed :reviewed} (:review-state b)))
                       (str "Source drift lacks an exact supported version bridge: " (:id document)))
             (let [pinned (reduce #(remove-once %1 %2 require!)
                                  (remove-once text platform-authority require!) platform-markers)]
               (require! (= (:sha256 document) (sha256 pinned)) "Source bridge changes pinned normative bytes")
               {:text pinned :bridge (assoc b :path (:path document) :span-basis :pinned-baseline-view
                                            :snapshot-provenance :unverified)}))))))))

(defn views
  ([manifest sources sha256 require!]
   (views manifest sources sha256 require! {}))
  ([manifest sources sha256 require! opts]
   (let [documents (into {} (map (juxt :id identity)
                                 (concat (:original-documents manifest) (:extension-sources manifest))))]
     (into (sorted-map)
           (map (fn [[id text]] [id (view manifest (get documents id) text sha256 require! opts)]) sources)))))

(defn texts [views]
  (into (sorted-map) (map (fn [[id v]] [id (:text v)]) views)))

(defn git-source [root ref path require!]
  (let [process (.start (.redirectErrorStream (ProcessBuilder. ^java.util.List ["git" "-C" (str root) "show" (str ref ":" path)]) true))
        out (with-open [input (.getInputStream process)] (slurp input :encoding "UTF-8"))
        exit (.waitFor process)]
    (require! (zero? exit) "Source version bridge commit/path cannot be read")
    out))

(defn git-blob-id [text]
  (let [bytes (.getBytes ^String text "UTF-8")
        header (.getBytes (str "blob " (alength bytes) "\u0000") "UTF-8")
        payload (byte-array (+ (alength header) (alength bytes)))]
    (System/arraycopy header 0 payload 0 (alength header))
    (System/arraycopy bytes 0 payload (alength header) (alength bytes))
    (format "%040x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-1") payload)))))

(defn git-commit-tree [root ref require!]
  (let [type-process (.start (.redirectErrorStream
                              (ProcessBuilder. ^java.util.List
                                               ["git" "-C" (str root) "cat-file" "-t" ref]) true))
        type (with-open [input (.getInputStream type-process)] (slurp input :encoding "UTF-8"))
        type-exit (.waitFor type-process)]
    (require! (and (zero? type-exit) (= "commit" (str/trim type)))
              (str "Pinned Git source is not a commit: " ref))
    (let [tree-process (.start (.redirectErrorStream
                                (ProcessBuilder. ^java.util.List
                                                 ["git" "-C" (str root) "rev-parse" "--verify"
                                                  (str ref "^{tree}")]) true))
          tree (with-open [input (.getInputStream tree-process)] (slurp input :encoding "UTF-8"))
          tree-exit (.waitFor tree-process)]
      (require! (and (zero? tree-exit)
                     (re-matches #"[0-9a-f]{40}" (str/trim tree)))
                (str "Pinned Git source has no tree: " ref))
      (str/trim tree))))

(defn validate-snapshots! [bridge read-ref sha256 require!]
  (require! (and (string? (:path bridge)) (not (.isAbsolute (io/file (:path bridge)))))
            "Source version bridge path must be relative")
  (let [trees [(:baseline-tree bridge) (:applying-tree bridge)]]
    (when (some some? trees)
      (require! (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) trees)
                "Bridge tree identities must be complete and exact")
      (doseq [[ref expected-tree] [[(:baseline-ref bridge) (:baseline-tree bridge)]
                                   [(:applying-ref bridge) (:applying-tree bridge)]]]
        (require! (= expected-tree (git-commit-tree "." ref require!))
                  (str "Bridge commit/tree identity differs: " ref)))))
  (doseq [[ref expected expected-blob] [[(:baseline-ref bridge) (:baseline-sha256 bridge) (:baseline-blob bridge)]
                                        [(:applying-ref bridge) (:live-sha256 bridge) (:applying-blob bridge)]]]
    (require! (boolean (re-matches #"[0-9a-f]{40}" (or ref ""))) "Invalid bridge source identity")
    (let [text (read-ref ref (:path bridge))]
      (require! (and (string? text) (= expected (sha256 text))) "Bridge snapshot bytes differ from declared source")
      (when expected-blob
        (require! (and (string? expected-blob)
                       (re-matches #"[0-9a-f]{40}" expected-blob)
                       (= expected-blob (git-blob-id text)))
                  "Bridge snapshot Git blob differs from declared source"))))
  (assoc bridge :snapshot-provenance :git-bytes-verified))
