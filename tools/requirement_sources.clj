(ns requirement-sources
  "Exact pinned source views across documented authority-only layout transfers."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
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
    :l6-hermetic-input-policy-semantic-projection
    :std13-native-model-policy-semantic-projection
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

(def canonical-policy-bridge-kinds
  #{:std13-native-model-policy-semantic-projection :l6-hermetic-input-policy-semantic-projection})

(def exact-source-bridge-contracts
  {:l6-hermetic-input-policy-semantic-projection
   {:path
 "docs/phase-01-core-language/016-l6-effect-system-specification.md",
 :source-review
 {:path "/tmp/uniform-pr767-L6-d2a-independent-review.edn",
  :sha256
  "b1320a44bfcd0504ba3ff474f9bcbd6a70920ecc0f68d0248ee095cdefb1a7c0"},
 :live-sha256
 "529b2cb63e0fe304f628ed63b0e6ef64d86200034f854836399e02e103b41f72",
 :applying-blob "b9ee86d36e07745e1c31c02848ec84ac2d9430db",
 :review-state :proposed,
 :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224",
 :human-decision :L12-declared-replayable-inputs,
 :authority
 "Matt's explicit L12-declared-replayable-inputs decision, authenticated by the preserved scoped canonical Source review.",
 :baseline-tree "5b62819e511bfc7e6b5eba0527d8c9b5883c5572",
 :semantic-deltas
 ["Declared environment key/value inputs require actual scoped build grants and all applicable phase/principal/provider/profile premises."
  "Network/shell/model/tool inputs require configured replayable providers or pinned results as well as authority; no ambient input authority follows from a declaration."
  "Clock/random, target manifests, compile-time input hashes and input/output/replay records remain explicit; pure constant evaluation still forbids external effects."],
 :projection-hunks
 [{:hunk-id "L6-policy-1",
   :facet-ids
   ["facet/language/7900fe5de772b242147a53fe"
    "facet/language/c8208102085757edca466771"
    "facet/language/d2f6f8aac945203a4b58ea25"],
   :baseline-start-line 246,
   :baseline-end-line 256,
   :live-start-line 246,
   :live-end-line 263,
   :baseline-fragment-sha256
   "9a1c085d0e85384ff12003e21462cb023a3bbe08e4bf0596e1b0c9bcb0ef6eb1",
   :live-fragment-sha256
   "0544b8f9123063dd16083974c8b47285ab274d4543499855d93db1eb8497c0ea"}
  {:hunk-id "L6-policy-2",
   :facet-ids ["facet/language/dfae37a673544578c9a71dcd"],
   :baseline-start-line 342,
   :baseline-end-line 342,
   :live-start-line 349,
   :live-end-line 353,
   :baseline-fragment-sha256
   "a87c9dd9bf9ffe08059c12f6a656109cd0dc6f15e3d6125c2894ab26fda1f38b",
   :live-fragment-sha256
   "af5c773959fba6b57a4fe35246c09ea81f04d274d8c4763be4635929a950fb7b"}],
 :applying-tree "ff13ce6160d46b982125face622e5028b4e1cb38",
 :kind :l6-hermetic-input-policy-semantic-projection,
 :document-id "L6",
 :applying-ref "d2a0378bdd530857d97d530aab15268fc333ce80",
 :qualification
 "Inverse source reconstruction authenticates historical identities only. The human-selected policy delta and its current required criteria remain explicit current semantics; baseline identity recovery cannot silently restore the superseded prohibition or confer current implementation/whole-scope acceptance.",
 :baseline-sha256
 "a9d41d970d56d20f0fb74f10c280689407e3ab8fe075c5bc3b71925e9ccec1d5",
 :current-criteria
 [{:path "specs/build-workflow/tasks.md",
   :span-sha256
   "6a01f79fc41bf87fdcd93ff65c0f21e59460c74fe5b384899e2ca6e6934bd4ec",
   :whole-file-sha256
   "a256b429fdf5abd511899384769575964487c975f727373defeaa1dde5e30234",
   :marker "compile-time-source-hermetic",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "395f8261e74db5dc9b1480f1a27991691e09ed5f",
   :end-byte 46981,
   :owning-task
   "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix",
   :start-byte 45246}],
 :baseline-blob "b0f96111735331ff130fee06509d94ceea5ec0d0"}
   :std13-native-model-policy-semantic-projection
   {:path
 "docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md",
 :source-review
 {:path
  "/tmp/uniform-new240-std13-independent-source-review-authority-v2.edn",
  :sha256
  "a00c3c9054da5362ef7388cef60a6075fc4c2e6be4a3d9eee1059c3ca8786bdc"},
 :live-sha256
 "25dab04498281cc51537bb0aec2ba046670ccf3fd3dbc3cafc419d8ab79ee71c",
 :applying-blob "a2e8355b6e3f2423e1d70bcd405717eaa7ca82a2",
 :review-state :proposed,
 :baseline-ref "a1b991be499d50579a8b4366976c519ae8949224",
 :human-decision :allow-scoped-native-local-model-APIs,
 :authority
 "Matt's explicit allow-scoped-native-local-model-APIs decision, authenticated by the preserved scoped canonical Source review.",
 :baseline-tree "5b62819e511bfc7e6b5eba0527d8c9b5883c5572",
 :semantic-deltas
 ["Permit only explicitly declared native local-model providers and scoped actual call authority as the bounded profile exception."
  "Retain applicable provider/model identity, input/output schemas, AI effects, policy, taint/secrets, failure, replay/audit and ordinary native safety/runtime obligations."
  "Reject ambient access, missing grants, mismatched/nonlocal provider execution, unchecked outputs and unrestricted native agent/tool APIs; the actual human decision and Source review remain separately authenticated."],
 :projection-hunks
 [{:hunk-id "STD13-policy-1",
   :facet-ids ["stdlib/facet/std13/1049-1178"],
   :baseline-start-line 20,
   :baseline-end-line 20,
   :live-start-line 20,
   :live-end-line 20,
   :baseline-fragment-sha256
   "0f5c69b125ee56d1490ce4d3724ca6c7c61d4dda718b7821b13bc52f40f93f2c",
   :live-fragment-sha256
   "a04b0654dc1b4b034dae3e76b0a4ed92cf48b5957443dacbd6d32ca0203262f2"}
  {:hunk-id "STD13-policy-2",
   :facet-ids ["stdlib/facet/std13/4423-4509"],
   :baseline-start-line 76,
   :baseline-end-line 76,
   :live-start-line 76,
   :live-end-line 81,
   :baseline-fragment-sha256
   "911d2ff421f524ceead14b06271e387de37cc3e33c3f167ee2bf3418c0408191",
   :live-fragment-sha256
   "d81424c71021198695a70bb95f18cebdfa72872873667a264a55c2f5cf650c5e"}
  {:hunk-id "STD13-policy-3",
   :facet-ids ["stdlib/facet/std13/6202-6265"],
   :baseline-start-line 104,
   :baseline-end-line 104,
   :live-start-line 109,
   :live-end-line 113,
   :baseline-fragment-sha256
   "f609af837290ec744eccea6155bf40b8c65880dbb08d90858b3c75711986303e",
   :live-fragment-sha256
   "2549c8272ca89efbdfd696d9cdf3b333a39090497880346522974bd83d7131c5"}],
 :applying-tree "95ab69a2482a3fbcdc0400fe9c04c34950e95778",
 :kind :std13-native-model-policy-semantic-projection,
 :document-id "STD13",
 :applying-ref "4c9539d5b9adf293d7203713e7c38b491e52fa9f",
 :qualification
 "Inverse source reconstruction authenticates historical identities only. The human-selected policy delta and its current required criteria remain explicit current semantics; baseline identity recovery cannot silently restore the superseded prohibition or confer current implementation/whole-scope acceptance.",
 :baseline-sha256
 "b578d3d39ed72930f3b6c838349fa3dbcdebe77698b98b88f3d93531f87a853f",
 :current-criteria
 [{:path "specs/legality-workflow/tasks.md",
   :span-sha256
   "2d5a2df55cac68df03d27b8da8c7420b812b8953bcb2c46a73de4f1d26f21666",
   :whole-file-sha256
   "f984efa03f925267bb2b683fe0613801b6322ffd76ecd462c1f89236e49474b2",
   :marker "native-local-model-policy",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "b2e842eeb6731949fc2184470519021ee33db575",
   :end-byte 21066,
   :owning-task
   "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks",
   :start-byte 19724}
  {:path "specs/stdlib-workflow/tasks.md",
   :span-sha256
   "23f14792ece7005fffb2eb156af811a3528b8f40a63a913acdd2a06b361b591f",
   :whole-file-sha256
   "c7f392ddd67ae923d7848c63ff957a84548975461010052ff421b46516a4d617",
   :marker "library-workflow-ai-source-ai-provider",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "e44f6f266530fee734af97f94130a2c5f4e9f911",
   :end-byte 155669,
   :owning-task
   "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters",
   :start-byte 154226}
  {:path "specs/stdlib-workflow/tasks.md",
   :span-sha256
   "851f517113737b0cf7b4bf985c3c643eb9e817e707e913c435699b99cf9f02a2",
   :whole-file-sha256
   "c7f392ddd67ae923d7848c63ff957a84548975461010052ff421b46516a4d617",
   :marker "library-workflow-ai-source-ai-policy",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "e44f6f266530fee734af97f94130a2c5f4e9f911",
   :end-byte 162618,
   :owning-task
   "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters",
   :start-byte 161218}
  {:path "specs/stdlib-workflow/tasks.md",
   :span-sha256
   "dc4e5b451f38cc40d91feabb991382686481f16157862b814e65f4f3659fac74",
   :whole-file-sha256
   "c7f392ddd67ae923d7848c63ff957a84548975461010052ff421b46516a4d617",
   :marker "library-workflow-ai-source-ai-profile",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "e44f6f266530fee734af97f94130a2c5f4e9f911",
   :end-byte 167063,
   :owning-task
   "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters",
   :start-byte 165321}
  {:path "specs/effects-workflow/tasks.md",
   :span-sha256
   "35733f38579bc077d2eb66f90bda9ab34739a4d09daecbabf2c4ff0eba8e4790",
   :whole-file-sha256
   "ffd8e09758643b5207053331929f08cbd2501405083055e0239be2e934451bc2",
   :marker "library-workflow-ai-source-effect-admission",
   :recorded-status :open,
   :source-commit "67d05c761cad92d5e09f0f8cd2c6e692853887cb",
   :blob "39bf0000f2fa10458efd16739602e1f01fbfdb29",
   :end-byte 31625,
   :owning-task
   "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering",
   :start-byte 30293}],
 :baseline-blob "e1417db32e7ab319c595dee9fcb917b0950b44f7"}
   math9-rewrite-termination-kind
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
(def historical-current-source-transition-contracts
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

;; The current-main STD7-STD9 source author changed only these exact files.
;; Keep the complete-file successor contracts compactly data-driven while
;; retaining every Git/blob/span identity as an authenticated constant.
(def std7-std9-source-transition-data
  [{:path "specs/auth-workflow/plan.md"
    :before [38081 433 "a6a7bea46f4047d878890c34fed6d019bf35633ade613a3ea468e80da3489f3c" "e8c486581086c204f9c6502766607ea88fc06606"]
    :after [38920 439 "e4512113f4aca4a9e46451d4098dc8e53c33cbf0caea7990ccdbd3197319c9c4" "0c6ac8e91e043d8fd37549a0283b0cb2484d7982"]}
   {:path "specs/auth-workflow/tasks.md"
    :before [46095 314 "18f664d1dd9a19451d4d78cce3563af5f0765aeb0a67957a75b4fac1bdb19d13" "dbb688ec44230c84fa95c35588330d89578cd3c3"]
    :after [47520 323 "463a28e0e08739a4a2df2dab39293eea2a6d4ca3001a715d58c4f0b5f6e66f44" "84d18fb7d43f9bfaa16bbc07609c10dcf2addfbf"]}
   {:path "specs/backends-workflow/plan.md"
    :before [12714 165 "6060dd1fcded73b1f57483d22ced90851f42aa1f3be18855a141a958f23be874" "59e3894d0d1556029007e3dcb93ec9692a03d3a1"]
    :after [13535 171 "293af99a5232c4d78114bd7d2cd3ed01601bdcf9c87322e52bdf91ae4fe96bde" "31d05140764bfc085faba507888f8724c7a9a1ce"]}
   {:path "specs/build-workflow/plan.md"
    :before [32078 331 "bdaee4de655e07164652b2f9592a3038f65b48a57f848abe648adc8ac9d4af4f" "58913132545af334181d7a642dd968eec0febd65"]
    :after [32904 337 "0730f26d501836790078a190b04d868781c1b0044542ad13b1e110587524b293" "8cd7c8095b1df6358e53dad42b2a871255191d89"]}
   {:path "specs/domains-workflow/plan.md"
    :before [70968 720 "30a039613db68aa9a8514b591c87a2ea9ed72a520e781d5458d87582b4595b88" "76eba7e94483dbb4a91309af51092203b5a6e2fd"]
    :after [72029 726 "a326cd5a2bb7420ae519cd58228e6de574e1d604250f4c091060ac0681421db2" "2b91cc8bcc75a8b745ff4f047e28ca2450125b8c"]}
   {:path "specs/effects-workflow/plan.md"
    :before [45410 510 "3f086260dfdda82e4fd7a8dc06b11328fa713318a4e954cd87a5290019951ecf" "0f1c2d796f322f451c999c6b429a66374ce566e7"]
    :after [46179 516 "806e80fc1849c54493d2db8b44e399ca05816415f5c27205dc7f6b820acd5d6a" "8d9cb8a65e81ec6370d14677fa5d93799870c55b"]}
   {:path "specs/effects-workflow/tasks.md"
    :before [34088 244 "59aeac1148be9e492462c0834008e62a0d980954a96d1e3b7889936af7bd4913" "7b855d0e04658fe5392239523ac0486a1a1e2b9c"]
    :after [35538 253 "f5f6ca1571e0cbe0614ed304231c7f4a6fb39fc1ca0a158b89d364700be2ffcc" "af17e2b63564685d400a4de4663e7f195dc9d7fb"]}
   {:path "specs/evolution-workflow/plan.md"
    :before [18234 196 "f7ed753974edbba8b8b5b51244a24acb265ac03c7d5f6d8a8c473aa37a389182" "b07fc872f2516ea3b7cf247660cdf5d2a54073d3"]
    :after [19015 202 "512317e26137900be02c78dcce2192cf1e2acda1bedc5cf1cb30c1eb717b2bcb" "c663c174e057fec2be2b1d414959e4e2378cd38a"]}
   {:path "specs/legality-workflow/plan.md"
    :before [92851 1039 "0a2fef2e6cd2cb4c8d008726be5c9c2c9e2b6dd7f7fc0ca0cf712d73bbde3392" "1cd71fc26d5fb575e6218f2428fe47ae95371b2f"]
    :after [93672 1045 "be3665dfc93e9dd690113afbddab5d28cb8133fe39902a6db30ec92cbff27184" "f58cbb60d67f1d435e9b6cd51c387eac82252cb9"]}
   {:path "specs/opt-workflow/plan.md"
    :before [21715 240 "6c9ef876e8290819dd034ea60a94be54c723c74391aabc4b13e6c53bb9935910" "9d04daad2cee4387cef1efb6836bee11d48aeebd"]
    :after [22536 246 "6c212448498ec8206939e359d312b8a104577783577a3ea33676e9d50fb4f7a9" "43c4f62b3065350a9f648628fe6221c70d1293ab"]}
   {:path "specs/release-workflow/plan.md"
    :before [36577 397 "587d5a98bbfdf42c90b655e133f1310d1d7591c940fe6c3dd00d03ac87a5cc2a" "92ed8a4228d0a280167597c9575b3e483892ad17"]
    :after [37441 403 "858a49b3088bda01ddbf28a548616ac93b408b98ba8d5d784b095cb6d6322a20" "6600ed16b640f06e735cf4740a62ce21201b142b"]}
   {:path "specs/runtime-workflow/plan.md"
    :before [23630 234 "2ff027027462153cda6763476a5cbe2474fada3bf630ca6d27accbf5cd5eff2a" "a0cf40557be745d5dfe43214b0ebab28eafa8e80"]
    :after [24556 240 "fa76e1380f17466e8969c905fed55fcf11c3aaaff704ad1e3afec46069f5370c" "0b680273894589337d1adb8c93d5c0f447006309"]}
   {:path "specs/runtime-workflow/tasks.md"
    :before [63686 339 "a4dfbe8d903e9051a3a47b2aa22dc9400551314c08423d446f8a1a9f359de3c1" "2a5966e98f4c7e02ec48138932fb51585bf1071b"]
    :after [69465 375 "3f58d9fce0eae45936200e09da8cbc2e4300c097b25b5e85061b7e02684da18c" "af30d0dff27cd207177e5865cd3e8f1d2ed714a1"]}
   {:path "specs/stdlib-workflow/plan.md"
    :before [37921 288 "734c723398c8e42946633d32ed32ecf99daf4e22b029b14732cf08c7986399f8" "c960d1a487d87eaebf774512bfb39e0ed5302d69"]
    :after [78724 360 "64e60b71d94fc5b12c3f9b342a21f573fc078c2d11b3b7e99fe7c6ae45117f9a" "4d7521645cd41846736b7c522979e8838d6aa641"]}
   {:path "specs/stdlib-workflow/spec.md"
    :before [14689 140 "aadd79b14a6e1312bd225421d1b59f24f3da27371fcfd744c5fc31b43da4f232" "8107efb4c0d1d448867accc9104b06e2d824c2b8"]
    :after [15425 142 "8f58c18d6f9613a6d2fbf3d4547a3aa73c9c91ac7dda4ecc574406ab43a795eb" "9edc8a8e9918c31f10e706c857aabd059af27d4e"]}
   {:path "specs/stdlib-workflow/tasks.md"
    :before [38854 276 "ecbf84050a1c0bec21ab3a83fbbe3ed7e55d67fd8a08fff42fdbaccfaed01bcb" "e2933a343359c2032086e34c996d02393e86b355"]
    :after [62664 429 "1411bbbea82f3aa7cbd70bb730e6118990f915613504880db14d538a13f5ffed" "f11c09c5b94e3cd74680ac49ef210e07c6558171"]}
   {:path "specs/types-workflow/plan.md"
    :before [53049 364 "b586691e2a0e437de9abba206f21aa799286994741d60cd6b3b1f3f4b65e0827" "d3f15f60e2886d993275db9a9871c6d8466c5158"]
    :after [53774 370 "6ce077533df0984f74f12dc3e2bfab0560a7ee9955ee8e7a58b3c987b293bc48" "364c6948e76a89a76998960a93fd032ec66dd209"]}])
(def std7-std9-source-transition-shared
  {:role :current-source-interface-proposal
   :review-state :pending-independent-source-version-review
   :baseline-ref "ff783a0ab1d324bbf9316e7de6b1db55f189e338"
   :baseline-tree "46627ba4a754f934c656c4f67ce65bd5302d1be1"
   :applying-ref "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
   :applying-tree "3fc8705088dddb2984bd27181ecec3f0b6066883"
   :authority :current-source-interface-only
   :kind :authenticated-current-source-file-transition-v1})
(def std7-std9-source-transition-contracts
  (mapv (fn [{:keys [path before after]}]
          (let [[before-bytes before-lines before-sha before-blob] before
                [after-bytes after-lines after-sha after-blob] after]
            (assoc std7-std9-source-transition-shared
                   :path path
                   :changed-spans [{:id (str "STD7-STD9/" path)
                                    :kind :complete-file-transition
                                    :before {:start-byte 0 :end-byte before-bytes
                                             :start-line 1 :end-line before-lines
                                             :sha256 before-sha}
                                    :after {:start-byte 0 :end-byte after-bytes
                                            :start-line 1 :end-line after-lines
                                            :sha256 after-sha}}]
                   :live-sha256 after-sha
                   :applying-blob after-blob
                   :document-id (str "std7-std9-"
                                     (str/replace path #"[^A-Za-z0-9]+" "-")
                                     "source-interface")
                   :qualification
                   (str "Only the complete authenticated STD7-STD9 current-main source file "
                        path " is transferred from the frozen ff783 current-source frame to reviewed 4202 source bytes. "
                        "The 23 library IO criterion/owner joins are bound separately; historical/applying pins, task status "
                        "and implementation acceptance remain separate and unchanged; this successor confers no migration, "
                        "implementation or whole-source authority.")
                   :baseline-sha256 before-sha
                   :baseline-blob before-blob)))
        std7-std9-source-transition-data))

;; The 792 Auth transition remains immutable history. The composed 908 frame
;; contains that L13 source plus the Main #1160 Auth additions, so it needs a
;; successor record rather than a duplicate path or a mutation of the old
;; applying pin. Evidence is a distinct current-source input and is bound in
;; its own complete-file transition.
(def current-source-transition-successor-contracts-v1
  (vec (concat
  [{:role :current-source-interface-proposal,
    :path "specs/auth-workflow/tasks.md",
    :changed-spans
    [{:id "908/specs/auth-workflow/tasks.md",
      :kind :complete-file-transition,
      :before {:start-byte 0,
               :end-byte 37340,
               :start-line 1,
               :end-line 205,
               :sha256 "d0cada75bab244e76d0985a7d22b0cc64c25f8f26625edfe1e61df8ca1a7f9e2"},
      :after {:start-byte 0,
              :end-byte 46095,
              :start-line 1,
              :end-line 314,
              :sha256 "18f664d1dd9a19451d4d78cce3563af5f0765aeb0a67957a75b4fac1bdb19d13"}}],
    :live-sha256 "18f664d1dd9a19451d4d78cce3563af5f0765aeb0a67957a75b4fac1bdb19d13",
    :applying-blob "dbb688ec44230c84fa95c35588330d89578cd3c3",
    :review-state :pending-independent-source-version-review,
    :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
    :authority :current-source-interface-only,
    :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
    :applying-tree "8fcfe6ebbb0700a8706fd9451f5219ab611ce874",
    :kind :authenticated-current-source-file-transition-v1,
    :document-id "908-specs-auth-workflow-tasks-md-source-interface",
    :applying-ref "21cecf7e7c47187a3e0b7b5bd00dfd4eab77e7d9",
    :qualification "Only the complete authenticated composed Auth task file is transferred from the b886 current-source frame to the post-merge 908 checkpoint. It conserves the prior L13 Auth transition and the Main #1160 L9/provider-source additions exactly once; historical/applying pins, task status and implementation acceptance remain separate and unchanged; this successor confers no migration, implementation or whole-source authority.",
    :baseline-sha256 "d0cada75bab244e76d0985a7d22b0cc64c25f8f26625edfe1e61df8ca1a7f9e2",
    :baseline-blob "c6bee617c100cebc8da49c1899c341f540e35393"}
   {:role :current-source-interface-proposal,
    :path "specs/auth-workflow/evidence.md",
    :changed-spans
    [{:id "908/specs/auth-workflow/evidence.md",
      :kind :complete-file-transition,
      :before {:start-byte 0,
               :end-byte 8156,
               :start-line 1,
               :end-line 128,
               :sha256 "ba3447b29a9bda52705d9798bf4c45d83554233190f8d526d0f7490e092c714d"},
      :after {:start-byte 0,
              :end-byte 17643,
              :start-line 1,
              :end-line 307,
              :sha256 "1c9f04c5f7c7d74977986ba882ea393edf6461c1bc042d75d0da615e3b35442e"}}],
    :live-sha256 "1c9f04c5f7c7d74977986ba882ea393edf6461c1bc042d75d0da615e3b35442e",
    :applying-blob "22618dc8d441c9ed5cfd33e32b2a48b6a9a7b434",
    :review-state :pending-independent-source-version-review,
    :baseline-ref "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
    :authority :current-source-interface-only,
    :baseline-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
    :applying-tree "8fcfe6ebbb0700a8706fd9451f5219ab611ce874",
    :kind :authenticated-current-source-file-transition-v1,
    :document-id "908-specs-auth-workflow-evidence-md-source-interface",
    :applying-ref "21cecf7e7c47187a3e0b7b5bd00dfd4eab77e7d9",
    :qualification "Only the complete authenticated composed Auth evidence file is transferred from the b886 current-source frame to the post-merge 908 checkpoint. Evidence bytes remain source/interface provenance; implementation, provider execution and acceptance authority remain separate and unassessed.",
    :baseline-sha256 "ba3447b29a9bda52705d9798bf4c45d83554233190f8d526d0f7490e092c714d",
    :baseline-blob "30378108773dffce6486d5b38f2dbceff52f8785"}]
  std7-std9-source-transition-contracts)))

(def current-source-transition-history
  historical-current-source-transition-contracts)

(def current-source-transition-contracts-v1
  (let [successors (into {} (map (juxt :path identity)
                                 current-source-transition-successor-contracts-v1))
        historical-paths (set (map :path historical-current-source-transition-contracts))
        current (vec (concat
                      (map #(get successors (:path %) %)
                           historical-current-source-transition-contracts)
                      (remove #(contains? historical-paths (:path %))
                              current-source-transition-successor-contracts-v1)))
        paths (map :path current)]
    (when-not (= (count paths) (count (set paths)))
      (throw (ex-info "Duplicate current-source transition path" {:paths paths})))
    current))

(def current-source-transition-paths-v1
  (set (map :path current-source-transition-contracts-v1)))

;; The reviewed source successors are composed with the current main checkpoint
;; before this frame is bound.  Keep the old 4202/908/STD7-STD9 frame above as
;; an immutable predecessor; these exact whole-file identities authenticate the
;; next source view without renewing any applying pin or implementation claim.
(def current-source-successor-composition
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4"
   :tree "72d15504280f60dd3056bf59cb58293ed734928f"
   :parents ["bc7cadae5450978ac9a3ec6a0ee9b1ebb1a471cb"
             "895dea1ddbce13b8deae461144e57b8245caefc8"]
   :authority :current-source-interface-only
   :review-state :pending-independent-source-version-review})
(def current-source-successor-after-data
  [{:path ".specify/memory/constitution.md" :blob "4bd99817a4f09ce7418886b40293f2b5d8cae821" :bytes 7336 :lines 122 :sha256 "055229478b7e9ff487227077199f9af57b4987699ff5bab72ac838ef502061a8"}
   {:path "specs/auth-workflow/plan.md" :blob "aaef141d94381b8bc5f38c17aaa330c617e50e6a" :bytes 42692 :lines 473 :sha256 "166a13d939e53507fda977c0af034fe6e875c09ad9d94d795ca0aa5db401cc0b"}
   {:path "specs/auth-workflow/tasks.md" :blob "ad1f82fa6a0a03362df053c76dd8ec31125751ab" :bytes 55649 :lines 377 :sha256 "e507459f4ce7152f6aa48aa8154a67bab82faf2f0bdc551d8d04f82329793946"}
   {:path "specs/build-workflow/plan.md" :blob "0416193da25e2ca5b511ce2a73f5d81cd7a50658" :bytes 38352 :lines 389 :sha256 "538102419b21e39048912f6ed8eccfc5dbe3a7823faab356d1eaf8f9ff2a892c"}
   {:path "specs/build-workflow/tasks.md" :blob "395f8261e74db5dc9b1480f1a27991691e09ed5f" :bytes 52214 :lines 343 :sha256 "a256b429fdf5abd511899384769575964487c975f727373defeaa1dde5e30234"}
   {:path "specs/compiler-api-workflow/plan.md" :blob "43ca1e7a0c435b991b593a0efaa2453025df2bbb" :bytes 39932 :lines 379 :sha256 "1fb11b8832ea2d88478553cc363bf0ae4f6fb11572c3713d697c6d040d0f0736"}
   {:path "specs/diag-workflow/plan.md" :blob "847bdf592ea2d38181e0198b351c99573bd9f1dd" :bytes 28669 :lines 319 :sha256 "02519403a80757d900109e54ab505fa52099334dd928cef8570bd090bd07891b"}
   {:path "specs/diag-workflow/tasks.md" :blob "fa2a5cc3699ed041cef6ed01726e83091de41034" :bytes 21880 :lines 164 :sha256 "05050c2e3f03b410934a857e3a9a3dbae2528bb81d3d44024edd3ba09b5b73be"}
   {:path "specs/effects-workflow/plan.md" :blob "3affa278147c837c7a742681c3f0041177f34699" :bytes 49732 :lines 543 :sha256 "d076f683c306449427c1afbc60e5a6f4fe5f517954b5ebd1d85e6b3c06390685"}
   {:path "specs/effects-workflow/tasks.md" :blob "b67c5af90d668c3bc6dcdde302d8d2bcdc08e552" :bytes 40520 :lines 289 :sha256 "d2681a1c12eea5e437603e804de8d37f07df667e5909b5d06fc5a5232fbf566d"}
   {:path "specs/evolution-workflow/plan.md" :blob "72a85d3e122986eb6a3461bea8bb354e230ecbfc" :bytes 21196 :lines 220 :sha256 "9265abbc90bc30cb124487e1887d8fd79beae79bdb1036e863da6535ea3d054c"}
   {:path "specs/expand-workflow/plan.md" :blob "7a9283c5c58e0d6459483adefd608b4e45fa7f9c" :bytes 97472 :lines 814 :sha256 "5523b19e3a987e47651be031aac6cbf6f94ccefd48c1b96258de9ed824140fbc"}
   {:path "specs/expand-workflow/tasks.md" :blob "62678faeaac2bcc9080428ff963c681c2c2c620a" :bytes 70147 :lines 436 :sha256 "20a0dc66f08f66c66590b42ed29bfdfad975a0e3799eee30059920e0d48e39ab"}
   {:path "specs/legality-workflow/plan.md" :blob "76da4f7e507f2b92dd3035f88859ec801830655e" :bytes 98005 :lines 1085 :sha256 "125243566745de1607ddcec6f16936e208027dfc91e6693235ece59f2c68fd32"}
   {:path "specs/legality-workflow/tasks.md" :blob "64454da9025341130c863ee2d6b1976c1b0f0287" :bytes 138236 :lines 722 :sha256 "f8201c4ba70b1ae3e4279ba5ea055bced5d094af0c94e060739979eab67cece7"}
   {:path "specs/opt-workflow/plan.md" :blob "295124295b3328efc5a5d3d1ed5a57dfb0f95a6c" :bytes 26291 :lines 279 :sha256 "8a89196fbc9b984507524a434ba92ee2b04d7b2fbb24956a28197782a196acfc"}
   {:path "specs/opt-workflow/tasks.md" :blob "52a0f27e8dfa6e047f30459ceba43822a494d13b" :bytes 19726 :lines 158 :sha256 "507fcaac02c2c415e66af2de5d1a56d46e3ffad71a735864582d0ca8a7a84e1b"}
   {:path "specs/release-workflow/plan.md" :blob "21c5401e2b02ddc4344216b1162adbaa0d5fa71f" :bytes 42143 :lines 443 :sha256 "983e2f3a3598c8da62b71ef61ab39be7ac1bcf4ffda52409602826440bf15c7e"}
   {:path "specs/resolve-workflow/plan.md" :blob "1f3de3c03b35f0e01caa30c44eb3426a1531e543" :bytes 81931 :lines 481 :sha256 "984771ccac4beb72d619b0dfc33258c94daa5da883999c67711846da6dfefac5"}
   {:path "specs/resolve-workflow/tasks.md" :blob "436e5bb579d038b37b23f6148ae659a335e29b37" :bytes 21321 :lines 181 :sha256 "dddecd1a6e4e221cbd4bb91dcfd5c9af0e1d4e125ee25afc60e308baa669f99a"}
   {:path "specs/stdlib-workflow/plan.md" :blob "2c11af264fb81cb0b7e7aa74fc7345ab20592cc2" :bytes 153923 :lines 517 :sha256 "90b45f98672fac1400a9ee13e0c844d9c98aac473eb3f7063a89163b0dd9eb35"}
   {:path "specs/stdlib-workflow/spec.md" :blob "865f1aebf7b20c00a6c19b1e856891dac1d22b1c" :bytes 16597 :lines 146 :sha256 "7e684efa393242d159945251c40e4b1e361042f8d7afad5e93c224aafb9f8cd8"}
   {:path "specs/stdlib-workflow/tasks.md" :blob "19015ee759302bb92dc961c5e00e68ec5b7fd5fd" :bytes 118497 :lines 897 :sha256 "af2e0623460653e39ae637820e56c8420a344e3148ace26dff87fc7cea59911e"}
   {:path "specs/types-workflow/plan.md" :blob "a3e62f42a206551cae876fd6f4e8dabe8a4a849b" :bytes 73998 :lines 602 :sha256 "67d2ed7cbc48b9947c384773fccdbab1f7c9bd64a960bd3f3f43c154961c536b"}
   {:path "specs/types-workflow/tasks.md" :blob "69f864017f6f95a26c479b4aaba3cb7b114d201d" :bytes 64782 :lines 415 :sha256 "6c681ea228458bbcc02fbdb45c765ae22859d86a41092c30eb6c264c518cab43"}
   {:path "docs/acceptance-dependencies.md" :blob "bb078aba5c7663af33080584953ef3c238c4bb0b" :bytes 539166 :lines 706 :sha256 "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e"}
   {:path "specs/auth-workflow/evidence.md" :blob "22618dc8d441c9ed5cfd33e32b2a48b6a9a7b434" :bytes 17643 :lines 307 :sha256 "1c9f04c5f7c7d74977986ba882ea393edf6461c1bc042d75d0da615e3b35442e"}
   {:path "specs/backends-workflow/plan.md" :blob "c247762a0312aac95baacd59cd6e3d5551c67c33" :bytes 47504 :lines 529 :sha256 "633c3118d3521b8521efa96a0d4b59df3a202091ac5f3e0c363f2e4a3d834d49"}
   {:path "specs/domains-workflow/plan.md" :blob "7701724947c68749ca40243703719760aade9d11" :bytes 75932 :lines 760 :sha256 "58fcfa28a601df460cdfaff1a6e2322ba86b18388641752e9d923d02d614fd77"}
   {:path "specs/runtime-workflow/plan.md" :blob "4841be0e06c702ab1cc401b3a9f6620ab07140a2" :bytes 47160 :lines 498 :sha256 "be52f3406a144149486a78636925b91643f679eb7cd65c153e65744cc4a782a8"}
   {:path "specs/runtime-workflow/tasks.md" :blob "09da9f8f231be0797c20f5f2d7bed7573df85711" :bytes 76867 :lines 429 :sha256 "a23e226cbafa81d144eef2ac12c5ff6d3928d76f57bf8d76deefc681700696df"}])
(defn- current-source-successor-contract [prior]
  (let [path (:path prior)
        after (first (filter #(= path (:path %)) current-source-successor-after-data))
        old-span (get-in prior [:changed-spans 0 :after])]
    (when-not after
      (throw (ex-info "Missing source successor identity" {:path path})))
    (assoc prior
           :document-id (str "source-successor-"
                             (str/replace path #"[^A-Za-z0-9]+" "-"))
           :changed-spans [{:id (str "SOURCE-SUCCESSOR/" path)
                            :kind :complete-file-transition
                            :before (assoc old-span
                                           :start-byte 0
                                           :end-byte (:end-byte old-span)
                                           :start-line 1
                                           :end-line (:end-line old-span))
                            :after {:start-byte 0
                                    :end-byte (:bytes after)
                                    :start-line 1
                                    :end-line (:lines after)
                                    :sha256 (:sha256 after)}}]
           :baseline-ref (:applying-ref prior)
           :baseline-tree (:applying-tree prior)
           :baseline-sha256 (:sha256 old-span)
           :baseline-blob (:applying-blob prior)
           :applying-ref (:commit current-source-successor-composition)
           :applying-tree (:tree current-source-successor-composition)
           :applying-blob (:blob after)
           :live-sha256 (:sha256 after)
           :role :current-source-interface-proposal
           :authority :current-source-interface-only
           :review-state :pending-independent-source-version-review
           :successor-of {:kind :authenticated-current-source-transition-frame-v1
                          :applying-ref (:applying-ref prior)
                          :applying-tree (:applying-tree prior)
                          :applying-blob (:applying-blob prior)
                          :live-sha256 (:live-sha256 prior)}
           :qualification
           (str "Only the complete authenticated current-source file " path
                " advances from its prior current-source bytes to the composed "
                (:commit current-source-successor-composition)
                " checkpoint. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this successor confers no migration, implementation or whole-source authority."))))
(def current-source-transition-successor-contracts
  (mapv current-source-successor-contract current-source-transition-contracts-v1))
;; Preserve the complete reviewed predecessor inside this bounded later
;; document transition. The earlier source census remains a historical
;; checkpoint; the current transition is the authority for these fourteen rows.
(def original-dependency-row-successor
  {:role :current-source-interface-proposal,
 :path "docs/acceptance-dependencies.md",
 :successor-of
 {:role :current-source-interface-proposal,
  :path "docs/acceptance-dependencies.md",
  :successor-of
  {:kind :authenticated-current-source-transition-frame-v1,
   :applying-ref "f79c62bcfc33f7aa645a455e0a98f32f0e008b6b",
   :applying-tree "98c688013460d4aeddf090fadd7ec0c01d6d45d1",
   :applying-blob "bb078aba5c7663af33080584953ef3c238c4bb0b",
   :live-sha256
   "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e"},
  :changed-spans
  [{:id "SOURCE-SUCCESSOR/docs/acceptance-dependencies.md",
    :kind :complete-file-transition,
    :before
    {:start-byte 0,
     :end-byte 539166,
     :start-line 1,
     :end-line 706,
     :sha256
     "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e"},
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
  :baseline-ref "f79c62bcfc33f7aa645a455e0a98f32f0e008b6b",
  :authority :current-source-interface-only,
  :baseline-tree "98c688013460d4aeddf090fadd7ec0c01d6d45d1",
  :applying-tree "72d15504280f60dd3056bf59cb58293ed734928f",
  :kind :authenticated-current-source-file-transition-v1,
  :document-id "source-successor-docs-acceptance-dependencies-md",
  :applying-ref "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
  :qualification
  "Only the complete authenticated current-source file docs/acceptance-dependencies.md advances from its prior current-source bytes to the composed cc8a79074fc8eeeb65d72333e5d64542b201f0d4 checkpoint. Historical/applying pins, task status and implementation acceptance remain separate and unchanged; this successor confers no migration, implementation or whole-source authority.",
  :baseline-sha256
  "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e",
  :baseline-blob "bb078aba5c7663af33080584953ef3c238c4bb0b"},
 :changed-spans
 [{:id "ORIGINAL-14-DEPENDENCY-ROWS",
   :kind :complete-file-transition,
   :before
   {:start-byte 0,
    :end-byte 539166,
    :start-line 1,
    :end-line 706,
    :sha256
    "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e"},
   :after
   {:start-byte 0,
    :end-byte 583986,
    :start-line 1,
    :end-line 720,
    :sha256
    "64ba3cc0125237e9fd9de774bc2fe66eb4f420b011555510082bfc967ece52bb"}}],
 :live-sha256
 "64ba3cc0125237e9fd9de774bc2fe66eb4f420b011555510082bfc967ece52bb",
 :applying-blob "7a036ee82a481d9f7c84a5935e4137f4b4931804",
 :review-state :pending-independent-source-version-review,
 :baseline-ref "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
 :authority :current-source-interface-only,
 :baseline-tree "72d15504280f60dd3056bf59cb58293ed734928f",
 :applying-tree "86ee17912db869c670ecda2c8bcf7ef8ad4f7009",
 :kind :authenticated-current-source-file-transition-v1,
 :document-id "original-14-dependency-row-successor",
 :applying-ref "9ef3622060652f339fa75b73fefa4963db272a10",
 :qualification
 "This source/interface successor adds only the fourteen independently reviewed original 0f4 dependency rows to the f989 composition. Its complete prior cc8 transition, historical census, source pins, task owners, statuses and implementation/evidence dispositions remain unchanged. All fourteen start-result lists remain empty. Coarse selected-result feedback grants no executable cycle, provider execution, implementation, release or full-240 acceptance.",
 :baseline-sha256
 "124296646996f1f4a4ff4e908f33667f12ee8e37888ab7ea9bb5b9e9fd7b3c1e",
 :baseline-blob "bb078aba5c7663af33080584953ef3c238c4bb0b"}
)
(def predecessor-current-source-transition-contracts
  (mapv #(if (= "docs/acceptance-dependencies.md" (:path %))
           original-dependency-row-successor %)
        current-source-transition-successor-contracts))
(def source-composition-file-data
  [{:path ".specify/memory/constitution.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "4bd99817a4f09ce7418886b40293f2b5d8cae821",
   :sha256
   "055229478b7e9ff487227077199f9af57b4987699ff5bab72ac838ef502061a8",
   :bytes 7336,
   :lines 122,
   :span-end-line 121},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "65783a5ac089590662c61152482d485f0d11cdc6",
   :sha256
   "e400c13add80ae7ab66ca435956410b658bc7b7000ce5c6232e0dba87dee2e0c",
   :bytes 8681,
   :lines 145,
   :span-end-line 144}}
 {:path "docs/acceptance-dependencies.md",
  :before
  {:commit "9ef3622060652f339fa75b73fefa4963db272a10",
   :tree "86ee17912db869c670ecda2c8bcf7ef8ad4f7009",
   :blob "7a036ee82a481d9f7c84a5935e4137f4b4931804",
   :sha256
   "64ba3cc0125237e9fd9de774bc2fe66eb4f420b011555510082bfc967ece52bb",
   :bytes 583986,
   :lines 721,
   :span-end-line 720},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "3d6d1f9043943e3737206113c7348547b2ecf1f2",
   :sha256
   "6043f3d57dabb552bd3adc1112fbde2ccc64e816af5943c40d90d2635ca42ad7",
   :bytes 624580,
   :lines 760,
   :span-end-line 759}}
 {:path
  "docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md",
  :before
  {:commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
   :tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
   :blob "e1417db32e7ab319c595dee9fcb917b0950b44f7",
   :sha256
   "b578d3d39ed72930f3b6c838349fa3dbcdebe77698b98b88f3d93531f87a853f",
   :bytes 6827,
   :lines 112,
   :span-end-line 111},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "a2e8355b6e3f2423e1d70bcd405717eaa7ca82a2",
   :sha256
   "25dab04498281cc51537bb0aec2ba046670ccf3fd3dbc3cafc419d8ab79ee71c",
   :bytes 7589,
   :lines 121,
   :span-end-line 120}}
 {:path "specs/auth-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "aaef141d94381b8bc5f38c17aaa330c617e50e6a",
   :sha256
   "166a13d939e53507fda977c0af034fe6e875c09ad9d94d795ca0aa5db401cc0b",
   :bytes 42692,
   :lines 474,
   :span-end-line 473},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "27746875730b17a1f3851548c64557a2febb5685",
   :sha256
   "d4e74c0e9ea9bb6c860e6cb3e146333ed0ad262c939073e0b207cb26537eab23",
   :bytes 46652,
   :lines 506,
   :span-end-line 505}}
 {:path "specs/auth-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "ad1f82fa6a0a03362df053c76dd8ec31125751ab",
   :sha256
   "e507459f4ce7152f6aa48aa8154a67bab82faf2f0bdc551d8d04f82329793946",
   :bytes 55649,
   :lines 378,
   :span-end-line 377},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "eacc8b08da24682cce054701f6f664ec4d91d247",
   :sha256
   "9a48036280d3ec34758f5c8b8e075db60f4512821ecb454f08bf3b5402d21e86",
   :bytes 60217,
   :lines 413,
   :span-end-line 412}}
 {:path "specs/backends-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "c247762a0312aac95baacd59cd6e3d5551c67c33",
   :sha256
   "633c3118d3521b8521efa96a0d4b59df3a202091ac5f3e0c363f2e4a3d834d49",
   :bytes 47504,
   :lines 530,
   :span-end-line 529},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "089fbbca496d6583a633ca32b3349035c80ee99a",
   :sha256
   "d73112dcc7caa1c768a592fad09ac4e7743fb6f61dd37e71f2df99fbe669cdf9",
   :bytes 50502,
   :lines 554,
   :span-end-line 553}}
 {:path "specs/build-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "0416193da25e2ca5b511ce2a73f5d81cd7a50658",
   :sha256
   "538102419b21e39048912f6ed8eccfc5dbe3a7823faab356d1eaf8f9ff2a892c",
   :bytes 38352,
   :lines 390,
   :span-end-line 389},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "fb2ed8343fefbb01cb37b140c319b33fd9ec28a9",
   :sha256
   "4b2bd5bc9ab4074251098f2e83a00008391f7b1b0d1d63cf39fbe1d8a6723056",
   :bytes 42419,
   :lines 422,
   :span-end-line 421}}
 {:path "specs/cli-workflow/plan.md",
  :before
  {:commit "e9ef2a6dc4b73518b8a9ff182ae7ac96eab56baa",
   :tree "db2239aa57777b22b20aabf052dd11f93783e13c",
   :blob "fa41e362af38b94538e156aa339cc63cd7b81723",
   :sha256
   "b25db661156837e5a61d15d745dd612814a5574bc8c74e230536cecd20210735",
   :bytes 5811,
   :lines 87,
   :span-end-line 86},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "7b654869f83dfd20e96033505e07780ad49f76aa",
   :sha256
   "7c5bba798813507b05e22525344c011bf91c02db597f1815bfffab4b5f5f6807",
   :bytes 6827,
   :lines 95,
   :span-end-line 94}}
 {:path "specs/cli-workflow/spec.md",
  :before
  {:commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
   :tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
   :blob "d5b0f9f48c051235f61f14751e110904438cda11",
   :sha256
   "fa8c8d454cb98c6ab791e26258aedd65fa5d7ceca04fca36f8190dfedf9385da",
   :bytes 8717,
   :lines 103,
   :span-end-line 102},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "20077e588910710d7ffac84c5b2ca992949f2c3b",
   :sha256
   "5f7a220dc1f802ad6eaf4b5950bfc4f014c1e604bcee5d1a4df69f06f6902ad6",
   :bytes 9221,
   :lines 105,
   :span-end-line 104}}
 {:path "specs/cli-workflow/tasks.md",
  :before
  {:commit "05f11aba381d49124b8c9ddeb509f4d05791dff2",
   :tree "d257282dd6860fc79b8d827f606bd389ad5dacc7",
   :blob "fcd44645e75b3b0c17abb964c2fb18ca0831cb34",
   :sha256
   "4e98c91c8beeb40ef78c8305db99f11b05b3095bbf9076204b55f319d854b73b",
   :bytes 9642,
   :lines 150,
   :span-end-line 149},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "2da6f571cb6d7d78a5f607e0e38a35dde2eaef9a",
   :sha256
   "6e376d8b3ffefcb956604bf5a8582c17d48fa03320f04cb3022e4e4cf5d339a2",
   :bytes 18424,
   :lines 227,
   :span-end-line 226}}
 {:path "specs/compiler-api-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "43ca1e7a0c435b991b593a0efaa2453025df2bbb",
   :sha256
   "1fb11b8832ea2d88478553cc363bf0ae4f6fb11572c3713d697c6d040d0f0736",
   :bytes 39932,
   :lines 380,
   :span-end-line 379},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "4764b65083570dc625b9052036c29663e1822d0e",
   :sha256
   "c859a327caa86c26f299f9fd001aabe67d6fb6d174424cb720b8a430da1bf417",
   :bytes 41842,
   :lines 396,
   :span-end-line 395}}
 {:path "specs/compiler-api-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "b5640ee34ca334b89e50f106740d814907814de5",
   :sha256
   "a0d0928ea767f5f6901d8ae4045f28d7bea45f89fc3607737812f2e53a2558e5",
   :bytes 69276,
   :lines 281,
   :span-end-line 280},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "8ec915f3131bb67b5f3c086b89c3d2c3c21826a4",
   :sha256
   "5ba49e3cc67928a172e6e2e1a734f40d8a15920730b040976bb5bf456321745b",
   :bytes 70634,
   :lines 292,
   :span-end-line 291}}
 {:path "specs/core-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "1f7193e32e5d1342e33127dabf5250588c43ee60",
   :sha256
   "9e3197fa1ca305cbb2ce50e65698b663779cb4db2876ab97aa89a68487770498",
   :bytes 63419,
   :lines 430,
   :span-end-line 429},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "410a685f08714e2bf5590caa7527a5831872649e",
   :sha256
   "69944812333a02bd56806c27f187311472ed522596a7d76eacb22ac320b359ba",
   :bytes 63849,
   :lines 434,
   :span-end-line 433}}
 {:path "specs/diag-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "847bdf592ea2d38181e0198b351c99573bd9f1dd",
   :sha256
   "02519403a80757d900109e54ab505fa52099334dd928cef8570bd090bd07891b",
   :bytes 28669,
   :lines 320,
   :span-end-line 319},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "5c91b889aadc54dc18b9bd255e7815d75670b4fb",
   :sha256
   "6143db8d1bee0d5a6b2973c2739172f0a6ae86e27993cefbc2c0c519badf39dc",
   :bytes 32685,
   :lines 352,
   :span-end-line 351}}
 {:path "specs/domains-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "7701724947c68749ca40243703719760aade9d11",
   :sha256
   "58fcfa28a601df460cdfaff1a6e2322ba86b18388641752e9d923d02d614fd77",
   :bytes 75932,
   :lines 761,
   :span-end-line 760},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "ff66bfd032902189965d62f466a63fd50e4bdb39",
   :sha256
   "e6173ce1138b17d10fcde874dafd69e2fdc29cc4d9caed2d2577c16e6aa2f41c",
   :bytes 78511,
   :lines 781,
   :span-end-line 780}}
 {:path "specs/domains-workflow/tasks.md",
  :before
  {:commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
   :tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
   :blob "0831a57f201cd974d81d1d8c6583eef4b7f45ecd",
   :sha256
   "3294ee20a756529102efebcc745022cbb9f795be7ece97d2bdd691b9edb2f62c",
   :bytes 440047,
   :lines 3399,
   :span-end-line 3398},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "052598b8a1e20984e3347b1fb55e0ccae1564a3a",
   :sha256
   "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
   :bytes 440113,
   :lines 3399,
   :span-end-line 3398}}
 {:path "specs/effects-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "3affa278147c837c7a742681c3f0041177f34699",
   :sha256
   "d076f683c306449427c1afbc60e5a6f4fe5f517954b5ebd1d85e6b3c06390685",
   :bytes 49732,
   :lines 544,
   :span-end-line 543},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "051e7903a7584061cb35d8938040c14c632e3e96",
   :sha256
   "9a39e24ce44fb94f24177f9b199633704b11b4a1cb3d71a39706ee508d7e1d9f",
   :bytes 52693,
   :lines 568,
   :span-end-line 567}}
 {:path "specs/effects-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "b67c5af90d668c3bc6dcdde302d8d2bcdc08e552",
   :sha256
   "d2681a1c12eea5e437603e804de8d37f07df667e5909b5d06fc5a5232fbf566d",
   :bytes 40520,
   :lines 290,
   :span-end-line 289},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "39bf0000f2fa10458efd16739602e1f01fbfdb29",
   :sha256
   "ffd8e09758643b5207053331929f08cbd2501405083055e0239be2e934451bc2",
   :bytes 47205,
   :lines 343,
   :span-end-line 342}}
 {:path "specs/evolution-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "72a85d3e122986eb6a3461bea8bb354e230ecbfc",
   :sha256
   "9265abbc90bc30cb124487e1887d8fd79beae79bdb1036e863da6535ea3d054c",
   :bytes 21196,
   :lines 221,
   :span-end-line 220},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "f0311945b4181f3e8ea257d5309234d2d9959661",
   :sha256
   "d3ea6c08c5c5b7de475b892f35d96a540522639ed24f7fe0ef4b110e74b7dc02",
   :bytes 23633,
   :lines 241,
   :span-end-line 240}}
 {:path "specs/expand-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "7a9283c5c58e0d6459483adefd608b4e45fa7f9c",
   :sha256
   "5523b19e3a987e47651be031aac6cbf6f94ccefd48c1b96258de9ed824140fbc",
   :bytes 97472,
   :lines 815,
   :span-end-line 814},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "07c3892b48752cdc49a756f0576025a4480901bf",
   :sha256
   "b957ae9de3a3611817c99661e8ec24c200087e6fb819abf4d21cb6635594d547",
   :bytes 99003,
   :lines 827,
   :span-end-line 826}}
 {:path "specs/legality-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "76da4f7e507f2b92dd3035f88859ec801830655e",
   :sha256
   "125243566745de1607ddcec6f16936e208027dfc91e6693235ece59f2c68fd32",
   :bytes 98005,
   :lines 1086,
   :span-end-line 1085},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "d57596ade61465e2ed59bd9f0d8675c76b5574ff",
   :sha256
   "cf7b67dc7e205c3ddb1498d556a772d42a47b251d3b2b63731a48f068869494c",
   :bytes 101112,
   :lines 1110,
   :span-end-line 1109}}
 {:path "specs/legality-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "64454da9025341130c863ee2d6b1976c1b0f0287",
   :sha256
   "f8201c4ba70b1ae3e4279ba5ea055bced5d094af0c94e060739979eab67cece7",
   :bytes 138236,
   :lines 723,
   :span-end-line 722},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "64e79b97de95454ca9276450c043349333a22cad",
   :sha256
   "fdd2770993d1e9f848def12b7741b21f01462192616b3b843acbe49508d03e1f",
   :bytes 141167,
   :lines 741,
   :span-end-line 740}}
 {:path "specs/lower-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "c2957cb6562f91ccb8e9be3b44f3f0e89642e1a3",
   :sha256
   "e10b7b3514d7811000a1ca688366f65fbc0eb057c9c9b0c88a9ce2230ebf069b",
   :bytes 25517,
   :lines 283,
   :span-end-line 282},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "a27056e1fd7860f3e21c9f97c041f484240cd738",
   :sha256
   "25385e2a7ba1929f248213ad610b29efc33007b0b5a99f2f809cadf362d42f9e",
   :bytes 26521,
   :lines 291,
   :span-end-line 290}}
 {:path "specs/lower-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "0e155bb9c937fd5ca514a4c9107798604037cd12",
   :sha256
   "8f18064c581a2550248e67650615f117e30b995221a24a9c90cb7a08154a95a7",
   :bytes 22526,
   :lines 171,
   :span-end-line 170},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "3668d5a8e13367fdf86d7f3c28804adf14e0f4b9",
   :sha256
   "ca83727a3fdb84b4f325273da1f047bed0e74c3d3ce975318e6cd2ae7b315320",
   :bytes 25256,
   :lines 193,
   :span-end-line 192}}
 {:path "specs/mir-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "a61f5a10038db932159fb17c8ed97177eeed030a",
   :sha256
   "02531797b65766c126508628b49cc84caa5b6119495e47e1a325cadca4646289",
   :bytes 20057,
   :lines 223,
   :span-end-line 222},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "25ae0d3bbc243aeede26bb8845eb75bf3fe0339e",
   :sha256
   "acf32c648b7aa71792c1c5dd4f25cba3263b2ced7623d2412d7735d94c89ac0f",
   :bytes 20479,
   :lines 227,
   :span-end-line 226}}
 {:path "specs/opt-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "295124295b3328efc5a5d3d1ed5a57dfb0f95a6c",
   :sha256
   "8a89196fbc9b984507524a434ba92ee2b04d7b2fbb24956a28197782a196acfc",
   :bytes 26291,
   :lines 280,
   :span-end-line 279},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "89e0d104f65c382a51034c186be5464399e6c50e",
   :sha256
   "0151b0070306aa2b25c9e0636371237866764afef0dcfb7ca734763b2e073449",
   :bytes 54551,
   :lines 457,
   :span-end-line 456}}
 {:path "specs/opt-workflow/spec.md",
  :before
  {:commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
   :tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
   :blob "135d3d60ef605b535e58da90f96e64189d26d83d",
   :sha256
   "38f4706a44057d0dde7bb8025b0d47be222c419078c3e158d823502b8fd203a0",
   :bytes 9548,
   :lines 139,
   :span-end-line 138},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "b126cac09b8b1580ff9f7178b21a82f316b3c237",
   :sha256
   "c6ea664a2e976c2302f46e2336863c4ca2da26dc8a3b73fc025e26db9f3a2988",
   :bytes 10062,
   :lines 141,
   :span-end-line 140}}
 {:path "specs/opt-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "52a0f27e8dfa6e047f30459ceba43822a494d13b",
   :sha256
   "507fcaac02c2c415e66af2de5d1a56d46e3ffad71a735864582d0ca8a7a84e1b",
   :bytes 19726,
   :lines 159,
   :span-end-line 158},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "f723ee5d62e89052b291233dfbc68c4e60828ad4",
   :sha256
   "58e2feda7ac60a99594e3ef737e35cd94ce19bda5c044c8c8549cdf65c97b303",
   :bytes 39628,
   :lines 313,
   :span-end-line 312}}
 {:path "specs/platform-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "39c0cbca4f0bc6aeba37322fd0589c208478427f",
   :sha256
   "44f8c924be1a0d7b523b96f4666ba60b883159c8e71c2b2749613065dc4b574b",
   :bytes 23582,
   :lines 231,
   :span-end-line 230},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "6e6a548b5a638f731ab33bdaab46b11f3626a29c",
   :sha256
   "2a9b6383561d05ddc15a577e22ee46c3ea11d85c4b0f00902bfb270e57d7ea07",
   :bytes 24615,
   :lines 239,
   :span-end-line 238}}
 {:path "specs/platform-workflow/tasks.md",
  :before
  {:commit "05f11aba381d49124b8c9ddeb509f4d05791dff2",
   :tree "d257282dd6860fc79b8d827f606bd389ad5dacc7",
   :blob "536da4ae764a3c985d758fa3f2e8435d06430862",
   :sha256
   "34035c35f900c7d45a04de2802086608c00605f39cb7c2d17b47b8411966660c",
   :bytes 107482,
   :lines 339,
   :span-end-line 338},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "d67a0908c27714071566e1a2da51e5617718fdaf",
   :sha256
   "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
   :bytes 107550,
   :lines 339,
   :span-end-line 338}}
 {:path "specs/reader-workflow/plan.md",
  :before
  {:commit "e9ef2a6dc4b73518b8a9ff182ae7ac96eab56baa",
   :tree "db2239aa57777b22b20aabf052dd11f93783e13c",
   :blob "a139e7b84c0e1ce7972a504dcd88f65e52d6ecd7",
   :sha256
   "b3116d43e345cdb8fd211b151be62d5644c9c4aa3de3e86d16cd03876c65c774",
   :bytes 14807,
   :lines 195,
   :span-end-line 194},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "818ef2638714d9fff2b508daa3e1d105820d51d2",
   :sha256
   "23bc8032561ac4329a55702d2d07dc78b0296ecc9d97352065db354d6084b36e",
   :bytes 15332,
   :lines 199,
   :span-end-line 198}}
 {:path "specs/release-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "21c5401e2b02ddc4344216b1162adbaa0d5fa71f",
   :sha256
   "983e2f3a3598c8da62b71ef61ab39be7ac1bcf4ffda52409602826440bf15c7e",
   :bytes 42143,
   :lines 444,
   :span-end-line 443},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "11afac6090f28ac842205c85c3def15f3908ed7e",
   :sha256
   "4850d741b9d25d2e0c2103ca19d6deafe4ce808950efae64a18f562c94e6f59e",
   :bytes 45946,
   :lines 472,
   :span-end-line 471}}
 {:path "specs/release-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "a554b8acf59d56f34528e2ac86cddbba5abded70",
   :sha256
   "8931f162a0f7b9847a501ed0dc95fc027da0baa7ae91c4d7df6bcefebdb0356c",
   :bytes 102132,
   :lines 333,
   :span-end-line 332},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "394f46938cf1df31452b1ebe4a6853608aef4345",
   :sha256
   "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
   :bytes 106505,
   :lines 366,
   :span-end-line 365}}
 {:path "specs/runtime-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "4841be0e06c702ab1cc401b3a9f6620ab07140a2",
   :sha256
   "be52f3406a144149486a78636925b91643f679eb7cd65c153e65744cc4a782a8",
   :bytes 47160,
   :lines 499,
   :span-end-line 498},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "1ed8740341e1771a78d4dfda5bf10de0dd69c084",
   :sha256
   "c8f90375e5b24eee8cf5b8cf5a262db5a53d59c4c2273261f265c9b1f1cce278",
   :bytes 51249,
   :lines 531,
   :span-end-line 530}}
 {:path "specs/runtime-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "09da9f8f231be0797c20f5f2d7bed7573df85711",
   :sha256
   "a23e226cbafa81d144eef2ac12c5ff6d3928d76f57bf8d76deefc681700696df",
   :bytes 76867,
   :lines 430,
   :span-end-line 429},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "919d68eb4a2c5c6752bc9247bf80e5bdc5715226",
   :sha256
   "c6605ecd2a5edd903ef1e64957a17423446bd4f0ad2adb64e194c5883819ef10",
   :bytes 90777,
   :lines 540,
   :span-end-line 539}}
 {:path "specs/stdlib-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "2c11af264fb81cb0b7e7aa74fc7345ab20592cc2",
   :sha256
   "90b45f98672fac1400a9ee13e0c844d9c98aac473eb3f7063a89163b0dd9eb35",
   :bytes 153923,
   :lines 518,
   :span-end-line 517},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "5db3208ec4e97730ce62aa23cf233bb62a8a98a8",
   :sha256
   "31ff0a020e87a59f3b44592f3b73ac526c03e4aa2876da7475bfc7744b32f874",
   :bytes 242330,
   :lines 1001,
   :span-end-line 1000}}
 {:path "specs/stdlib-workflow/spec.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "865f1aebf7b20c00a6c19b1e856891dac1d22b1c",
   :sha256
   "7e684efa393242d159945251c40e4b1e361042f8d7afad5e93c224aafb9f8cd8",
   :bytes 16597,
   :lines 147,
   :span-end-line 146},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "840c64600eecf6e525f29ba18a37cc8155925c07",
   :sha256
   "5d93a7bb945e85d2636a042ebf7928be4db944e0554ea793cf263b00aa768b90",
   :bytes 19127,
   :lines 157,
   :span-end-line 156}}
 {:path "specs/stdlib-workflow/tasks.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "19015ee759302bb92dc961c5e00e68ec5b7fd5fd",
   :sha256
   "af2e0623460653e39ae637820e56c8420a344e3148ace26dff87fc7cea59911e",
   :bytes 118497,
   :lines 898,
   :span-end-line 897},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "e44f6f266530fee734af97f94130a2c5f4e9f911",
   :sha256
   "c7f392ddd67ae923d7848c63ff957a84548975461010052ff421b46516a4d617",
   :bytes 239031,
   :lines 1868,
   :span-end-line 1867}}
 {:path "specs/syntax-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "8191b896c0191f438c400847279ed92f04c5d05c",
   :sha256
   "27e8eb62c7c0a96a1d985eb3bf1b9efac04ca72a04bb6fcc91b122de4d103985",
   :bytes 48260,
   :lines 337,
   :span-end-line 336},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "25b97c17d3fb2cc0eee55d2b12738892fb4612ba",
   :sha256
   "330cc9fe6d7f70596023d29af9c5642b51c1302e694a6368c1049a98aa2f37a7",
   :bytes 49200,
   :lines 345,
   :span-end-line 344}}
 {:path "specs/tools-workflow/plan.md",
  :before
  {:commit "cc8a79074fc8eeeb65d72333e5d64542b201f0d4",
   :tree "72d15504280f60dd3056bf59cb58293ed734928f",
   :blob "632e48c59327cc48fb1b90d2578f5e82e9cb02ff",
   :sha256
   "71e5df6ef0787d6d6c7662959e2a0becd1c4674795e44e0e1356dfde04ba3749",
   :bytes 21512,
   :lines 220,
   :span-end-line 219},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "a5eeb294bc58febfcf55453fa133593cf16422df",
   :sha256
   "366b4efc4f29370b279e54f532f9340ce292711e75cb6a8877f729efdbe43ec4",
   :bytes 69644,
   :lines 497,
   :span-end-line 496}}
 {:path "specs/tools-workflow/spec.md",
  :before
  {:commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d",
   :tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7",
   :blob "eea3d6ba91a5c6b9dd2f1aa234e18b6bc0c70c8a",
   :sha256
   "aedf62910f4ac59305483fe5f123bfba529d3ee9cfa57434ddde6436fa8b2ae3",
   :bytes 13509,
   :lines 129,
   :span-end-line 128},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "3927f398b2b7a826d821155076b32c6c8d72348e",
   :sha256
   "d5cb1aeb1a16e6a1c923fc25d4e4987d013fe1c54e22869c5b0f1ae69c4ae181",
   :bytes 14549,
   :lines 133,
   :span-end-line 132}}
 {:path "specs/tools-workflow/tasks.md",
  :before
  {:commit "05f11aba381d49124b8c9ddeb509f4d05791dff2",
   :tree "d257282dd6860fc79b8d827f606bd389ad5dacc7",
   :blob "b414b0a0008450882853a5400b5b559ddefd0758",
   :sha256
   "dcc9b5eb9885b1c8c3608034cfad1e8ddcc0ea916c5be8a260ec5d1af93f8bf2",
   :bytes 14022,
   :lines 157,
   :span-end-line 156},
  :after
  {:commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
   :tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
   :blob "f8365604c70b1d2dde54c51c470a866c6a3e153d",
   :sha256
   "97c7c0f45d767ec962db7af3f81fa58434bfd2b6ad75fe4ee770b5dde92dec6f",
   :bytes 57724,
   :lines 564,
   :span-end-line 563}}])

(def source-composition-library-io-span-data
  [{:id "service-contracts",
  :current-span
  {:start-byte 26350,
   :end-byte 27450,
   :start-line 207,
   :end-line 207,
   :sha256
   "c31b79557f07fd82b59b8ab8ade6f4ff51bdabf1b06ede6a8e26ca2f854fabeb"}}
 {:id "concurrency-api",
  :current-span
  {:start-byte 27451,
   :end-byte 28817,
   :start-line 208,
   :end-line 208,
   :sha256
   "b4650307a9261af76c8e0746d8e41ed73df655ae7f51186d2e792f3fd446f928"}}
 {:id "channels-and-actors",
  :current-span
  {:start-byte 28818,
   :end-byte 29854,
   :start-line 209,
   :end-line 209,
   :sha256
   "6acee921026c085995e66210eb6fd4d40fca3959f8596b3adf536758d2bf4b9e"}}
 {:id "scheduler-and-atomic-apis",
  :current-span
  {:start-byte 29855,
   :end-byte 31151,
   :start-line 210,
   :end-line 210,
   :sha256
   "b3ab686bea6e919df6683314a02fabf7c75e6c645589f827f0104b86584ac4af"}}
 {:id "stream-and-handle-apis",
  :current-span
  {:start-byte 31152,
   :end-byte 32238,
   :start-line 211,
   :end-line 211,
   :sha256
   "13129429b60b526b9bed75042a3d97561d5149bd958ca0605c9f4e53fd5f5483"}}
 {:id "typed-path-policy",
  :current-span
  {:start-byte 32239,
   :end-byte 33133,
   :start-line 212,
   :end-line 212,
   :sha256
   "ab42f96fd11c402a66b8966e1ac53e2426e618ac00de66cf88c70db54bdda843"}}
 {:id "text-io",
  :current-span
  {:start-byte 33134,
   :end-byte 34004,
   :start-line 213,
   :end-line 213,
   :sha256
   "f07d3c06c899c2f489613771de7454868b726a6862bd5baeea77f7111869cb79"}}
 {:id "directory-traversal",
  :current-span
  {:start-byte 34005,
   :end-byte 34800,
   :start-line 214,
   :end-line 214,
   :sha256
   "64fd85e3df4e2c6e3d1f3c62fdc1995901438fbc77c7ce5ff72304e23cca3f1a"}}
 {:id "atomic-file-replacement",
  :current-span
  {:start-byte 34801,
   :end-byte 35703,
   :start-line 215,
   :end-line 215,
   :sha256
   "fc6bec8e38bc9c7765d8663603cdb258f4008811fa721a11eadbe3b76af47f62"}}
 {:id "network-surface",
  :current-span
  {:start-byte 35704,
   :end-byte 36872,
   :start-line 216,
   :end-line 216,
   :sha256
   "bdfa2332f3979c0ee3d54f878d1ee963f26fcb6caed5f8f78626b69da3e38da6"}}
 {:id "http-client",
  :current-span
  {:start-byte 36873,
   :end-byte 37999,
   :start-line 217,
   :end-line 217,
   :sha256
   "ca928d41a86300210073da1f6f04ffdf8556d6ff9d49660a71cb668cc0c3ff4a"}}
 {:id "http-server",
  :current-span
  {:start-byte 38000,
   :end-byte 39026,
   :start-line 218,
   :end-line 218,
   :sha256
   "b60ede05f2f1f1a1a3806557ac8f105c103df7902e71c406bacad485cecf5b51"}}
 {:id "tls-policy",
  :current-span
  {:start-byte 39027,
   :end-byte 39953,
   :start-line 219,
   :end-line 219,
   :sha256
   "2817598718a5b9bbb50559290da0b016c9247501b71cc80e435cf1c4773aa4eb"}}
 {:id "network-schema-boundary",
  :current-span
  {:start-byte 39954,
   :end-byte 40949,
   :start-line 220,
   :end-line 220,
   :sha256
   "e795a01ad166a52270b45f23cdd6cb7be5d5b0593b309309e9782ee0fa2bd063"}}
 {:id "service-profile-matrix",
  :current-span
  {:start-byte 40950,
   :end-byte 42329,
   :start-line 221,
   :end-line 221,
   :sha256
   "3ee352972dc29677aa8b026fb9c3f2a20390a0f8cb8f862497246bc7b2aef366"}}
 {:id "resource-cleanup",
  :current-span
  {:start-byte 42330,
   :end-byte 43340,
   :start-line 222,
   :end-line 222,
   :sha256
   "92a7ba41928ae3583c182ccdde662d8365a5cd6724449c6853fc4132e3a1ba80"}}
 {:id "recorded-service-replay",
  :current-span
  {:start-byte 43341,
   :end-byte 44622,
   :start-line 223,
   :end-line 223,
   :sha256
   "7e6c5aa3bdc0487ad414bfa6cf8a52cfad839ccb74cabfd7fcb2569529c16708"}}
 {:id "actual-service-enforcement",
  :current-span
  {:start-byte 44623,
   :end-byte 45773,
   :start-line 224,
   :end-line 224,
   :sha256
   "34904257fb26ca811ad16449ec91d3f8177f378c7f46fe383af39788c617f6e4"}}
 {:id "service-observation",
  :current-span
  {:start-byte 45774,
   :end-byte 46864,
   :start-line 225,
   :end-line 225,
   :sha256
   "5a23e9fee1b80ef184fb53d66cf74dc51af59249d46b96c35977df559f2665e6"}}
 {:id "service-effect-admission",
  :current-span
  {:start-byte 46865,
   :end-byte 47992,
   :start-line 226,
   :end-line 226,
   :sha256
   "8863a2c90e779286172f8f7b6cd38eada3fa320d5d28777edee187e02c938768"}}
 {:id "operation-grants",
  :current-span
  {:start-byte 47993,
   :end-byte 49125,
   :start-line 227,
   :end-line 227,
   :sha256
   "a94a42e78d8440e6dcb172d4f90fb135692933ef85ef2ebdf54a703b6cf1ca6c"}}
 {:id "unsafe-service-wrappers",
  :current-span
  {:start-byte 49126,
   :end-byte 50134,
   :start-line 228,
   :end-line 228,
   :sha256
   "bd8e4c6aabae7749d61b665023b53ef0bb7a39264a5641cdc90967baaf3e0184"}}
 {:id "library-service-conformance",
  :current-span
  {:start-byte 50135,
   :end-byte 51582,
   :start-line 229,
   :end-line 229,
   :sha256
   "176f554a076d04f5eea155ee7c68b0d194ed02e5f5b72b70caaf5c0b39dcea5e"}}
 {:id "atomic-existing",
  :current-span
  {:start-byte 51583,
   :end-byte 52192,
   :start-line 230,
   :end-line 230,
   :sha256
   "fe1c527506170e667ac19c7faee7e8255fd35c5333af7452fbea83b3de791569"}}
 {:id "channel-existing",
  :current-span
  {:start-byte 52192,
   :end-byte 52814,
   :start-line 231,
   :end-line 231,
   :sha256
   "e43b6fc42c2632c1447862d46ce7c41f33e57e8a5f9ff4c4cb756e44ab19b868"}}
 {:id "preservation-existing",
  :current-span
  {:start-byte 52814,
   :end-byte 53858,
   :start-line 232,
   :end-line 232,
   :sha256
   "18a148170b6bc76c0c691d690b3eb9ca758c5a1b136c513fe8ed11e2e12f5de4"}}
 {:id "profile-existing",
  :current-span
  {:start-byte 53858,
   :end-byte 54717,
   :start-line 233,
   :end-line 233,
   :sha256
   "43c194cfee2f0f0a026de07d611cc9436fa42cae003d431a63368a0fddacba92"}}
 {:id "race-analysis-existing",
  :current-span
  {:start-byte 54717,
   :end-byte 55764,
   :start-line 234,
   :end-line 234,
   :sha256
   "1e8af63b32c741351cf1fda8ad7302b9d620ea2f1da23935115187164b52f07b"}}
 {:id "runtime-concurrency-existing",
  :current-span
  {:start-byte 55764,
   :end-byte 57078,
   :start-line 235,
   :end-line 235,
   :sha256
   "4b6714e6b0e6685d60d9ed5e4b1339b5f3040459f826772d2b416dc1eae6ccc4"}}
 {:id "safety-existing",
  :current-span
  {:start-byte 57078,
   :end-byte 58588,
   :start-line 236,
   :end-line 236,
   :sha256
   "cf41a2cff0de1ddca2f07e3318c1f9048a1f537b29636f3474c6bfee73368563"}}
 {:id "structured-existing",
  :current-span
  {:start-byte 58588,
   :end-byte 59232,
   :start-line 237,
   :end-line 237,
   :sha256
   "c0b89b2a160d0312125851f6644d3f41ef558cd486c0d543091630aa255e3ad7"}}])

(def source-composition-history-contract
  {:predecessor-crosswalk
 {:commit "5a8791e36dd7816b686bd9a1b07bc238ef2d0df4",
  :tree "7327dff1b1c1d6416ef8b22b6b937d93e3af4764",
  :path "specs/sh-family-crosswalk/crosswalk.edn",
  :blob "6b17993d44428726df5e2c68c2a9ef695156466e",
  :sha256
  "4dbd84f1bedc95ef064e06a0d44d6b4d8d0a48978319b2b6522e2f9f5c9f5498"},
 :current-source-tree "2fe775403b016510a7828389bcbb8c1bd710c1b9",
 :review-state :pending-independent-source-version-review,
 :authority :current-source-interface-only,
 :predecessor
 {:commit "5a8791e36dd7816b686bd9a1b07bc238ef2d0df4",
  :tree "7327dff1b1c1d6416ef8b22b6b937d93e3af4764",
  :path
  "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn",
  :blob "f39ebafc150ee31b44701bb28271d2d6f3172ee6",
  :sha256
  "4b9e15bd885bc6c648a30f7b034dffe149873f9f9e94fb095cf69042a65f86e6"},
 :kind :authenticated-source-composition-transition-history-v1,
 :qualification
 "The complete 5a879 predecessor frame remains authenticated history. Explicit current file and unchanged-predicate span transitions bind only reviewed source bytes. Prior source snapshots, evidence subjects, receipts, task identities, dispositions and seed-retirement limits remain unchanged; current-source binding grants no implementation, migration, release or full-240 acceptance.",
 :current-source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
 :changed-source-paths
 [".specify/memory/constitution.md"
  "docs/acceptance-dependencies.md"
  "docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md"
  "specs/auth-workflow/plan.md"
  "specs/auth-workflow/tasks.md"
  "specs/backends-workflow/plan.md"
  "specs/build-workflow/plan.md"
  "specs/cli-workflow/plan.md"
  "specs/cli-workflow/spec.md"
  "specs/cli-workflow/tasks.md"
  "specs/compiler-api-workflow/plan.md"
  "specs/compiler-api-workflow/tasks.md"
  "specs/core-workflow/plan.md"
  "specs/diag-workflow/plan.md"
  "specs/domains-workflow/plan.md"
  "specs/domains-workflow/tasks.md"
  "specs/effects-workflow/plan.md"
  "specs/effects-workflow/tasks.md"
  "specs/evolution-workflow/plan.md"
  "specs/expand-workflow/plan.md"
  "specs/legality-workflow/plan.md"
  "specs/legality-workflow/tasks.md"
  "specs/lower-workflow/plan.md"
  "specs/lower-workflow/tasks.md"
  "specs/mir-workflow/plan.md"
  "specs/opt-workflow/plan.md"
  "specs/opt-workflow/spec.md"
  "specs/opt-workflow/tasks.md"
  "specs/platform-workflow/plan.md"
  "specs/platform-workflow/tasks.md"
  "specs/reader-workflow/plan.md"
  "specs/release-workflow/plan.md"
  "specs/release-workflow/tasks.md"
  "specs/runtime-workflow/plan.md"
  "specs/runtime-workflow/tasks.md"
  "specs/stdlib-workflow/plan.md"
  "specs/stdlib-workflow/spec.md"
  "specs/stdlib-workflow/tasks.md"
  "specs/syntax-workflow/plan.md"
  "specs/tools-workflow/plan.md"
  "specs/tools-workflow/spec.md"
  "specs/tools-workflow/tasks.md"],
 :implementation-acceptance :unassessed})

(defn- composition-source-transition [{:keys [path before after]}]
  (let [prior (first (filter #(= path (:path %)) predecessor-current-source-transition-contracts))
        whole (fn [facts] {:start-byte 0 :end-byte (:bytes facts) :start-line 1
                          :end-line (:span-end-line facts) :sha256 (:sha256 facts)})]
    (cond-> {:kind current-source-transition-kind :role :current-source-interface-proposal
             :path path :document-id (str "source-composition-" (str/replace path #"[^A-Za-z0-9]+" "-"))
             :baseline-ref (:commit before) :baseline-tree (:tree before) :baseline-blob (:blob before) :baseline-sha256 (:sha256 before)
             :applying-ref (:commit after) :applying-tree (:tree after) :applying-blob (:blob after) :live-sha256 (:sha256 after)
             :changed-spans [{:id (str "SOURCE-COMPOSITION/" path) :kind :complete-file-transition :before (whole before) :after (whole after)}]
             :authority :current-source-interface-only :review-state :pending-independent-source-version-review
             :qualification "This explicit successor authenticates current source bytes separately from the complete predecessor frame. Historical snapshots, receipts, evidence subjects, task states and implementation dispositions remain unchanged; this source binding grants no execution, implementation, migration, release or full-240 acceptance."}
      prior (assoc :successor-of prior))))

(def source-composition-transition-contracts
  (mapv composition-source-transition source-composition-file-data))

(def source-composition-current-source-transition-contracts
  (let [successors (into {} (map (juxt :path identity) source-composition-transition-contracts))
        prior-paths (set (map :path predecessor-current-source-transition-contracts))]
    (vec (concat (map #(get successors (:path %) %) predecessor-current-source-transition-contracts)
                 (remove #(contains? prior-paths (:path %)) source-composition-transition-contracts)))))

(def source-consumer-file-data
 {:path "specs/build-workflow/tasks.md",
 :before
 {:commit "0293c86cba25ecaf0e96cfcf827c029042c4ef2c",
  :tree "09f618374673feb5a2ac01d0d847e478e3bc01b9",
  :blob "395f8261e74db5dc9b1480f1a27991691e09ed5f",
  :sha256
  "a256b429fdf5abd511899384769575964487c975f727373defeaa1dde5e30234",
  :bytes 52214,
  :span-end-line 342},
 :after
 {:commit "9f6f43f93725e65b93464f62b922bcd16f2f17d9",
  :tree "cc0fdb6f7ed6014b6a449e092a1461cf738c5bc6",
  :blob "72d8816b2bf342528ed25672c3ffbafb286e2beb",
  :sha256
  "9675337c127fe1357b345a45973a5fe1093b1d63aae00ca46cd00feb17562124",
  :bytes 54908,
  :span-end-line 356}})

(def source-consumer-file-history-contract
 {:predecessor-crosswalk
 {:commit "0293c86cba25ecaf0e96cfcf827c029042c4ef2c",
  :tree "09f618374673feb5a2ac01d0d847e478e3bc01b9",
  :path "specs/sh-family-crosswalk/crosswalk.edn",
  :blob "b115846f12e743af4b5cbd2ea7d239bcba99f9d9",
  :sha256
  "064b4a0795fb36c0181a1c4b73685af56ef529e77cf6c6f379c933eded37b03e"},
 :predecessor-frame
 {:commit "0293c86cba25ecaf0e96cfcf827c029042c4ef2c",
  :tree "09f618374673feb5a2ac01d0d847e478e3bc01b9",
  :path
  "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn",
  :blob "4064953c6b65cdb074ffeb12d1808325d46c41e2",
  :sha256
  "e6cf072c34530d82e150d90e3176169e8cba75b2c9554264aae20fb735a8c182"},
 :review-state :pending-independent-source-version-review,
 :authority :current-source-interface-only,
 :source-content-commit "9f6f43f93725e65b93464f62b922bcd16f2f17d9",
 :previous-file-transition-present false,
 :kind :authenticated-build-consumer-source-successor-v1,
 :qualification
 "Only Build's file advances after two explicit B1-B3 consumer clauses. Its previous file bytes remain bound to exact0293; it had no separate current-file transition among the preserved48, so one explicit Build file transition is added without inventing an older transition. The complete previous frame, all48 prior transitions, all prior raw payload subjects, source pins, ordinary/future task identities and actual states remain preserved. The two edited task bodies retain every complete old body as a unique prefix. All other file transitions and31 library-IO predicates remain exact. This is a source identity successor, not renewed historical review, execution, task admission, Main integration, release or full240 acceptance.",
 :current-criteria
 [{:task-id
   "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution",
   :path "specs/build-workflow/tasks.md",
   :source-commit "9f6f43f93725e65b93464f62b922bcd16f2f17d9",
   :source-tree "cc0fdb6f7ed6014b6a449e092a1461cf738c5bc6",
   :source-span
   {:start-byte 7818,
    :end-byte 8758,
    :sha256
    "146514c214f9077201f9ffe759ff6eaeb28b1f957725711613592080b20722e5"},
   :implementation-acceptance :unassessed}
  {:task-id
   "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix",
   :path "specs/build-workflow/tasks.md",
   :source-commit "9f6f43f93725e65b93464f62b922bcd16f2f17d9",
   :source-tree "cc0fdb6f7ed6014b6a449e092a1461cf738c5bc6",
   :source-span
   {:start-byte 51835,
    :end-byte 52965,
    :sha256
    "23306850e905b2cb490062f7d7e544a215cc0c3e63eef350fd6a74b3000c5912"},
   :implementation-acceptance :unassessed}],
 :source-content-tree "cc0fdb6f7ed6014b6a449e092a1461cf738c5bc6",
 :changed-source-paths ["specs/build-workflow/tasks.md"],
 :implementation-acceptance :unassessed})

(def source-consumer-file-transition
  (let [path (:path source-consumer-file-data)
        prior (first (filter #(= path (:path %)) source-composition-current-source-transition-contracts))]
    (cond-> (assoc (composition-source-transition source-consumer-file-data)
           :document-id "source-consumer-build-b1-b3"
           :qualification (:qualification source-consumer-file-history-contract))
      prior (assoc :successor-of prior))))

(def build-consumer-current-source-transition-contracts
  (let [prior source-composition-current-source-transition-contracts
        path (:path source-consumer-file-data)]
    (cond-> (mapv #(if (= path (:path %)) source-consumer-file-transition %) prior)
      (not-any? #(= path (:path %)) prior) (conj source-consumer-file-transition))))

(def auth-crypto-source-document-data
 {:path "docs/acceptance-dependencies.md",
 :before
 {:commit "070afe9e051c65efe456b01c7c223b6cfcf4c78f",
  :tree "28187e995d9369f4dcddaf797dcc497b334552ef",
  :blob "3d6d1f9043943e3737206113c7348547b2ecf1f2",
  :sha256
  "6043f3d57dabb552bd3adc1112fbde2ccc64e816af5943c40d90d2635ca42ad7",
  :bytes 624580,
  :span-end-line 759},
 :after
 {:commit "336b8a7697fe90c49a8eab050c3bc0fcdfc3cf62",
  :tree "878c813060a7643b08138b8e1c9646fd922398a3",
  :blob "dd2d313ba8bc24e90f6a8e9597368a19a6d01fe8",
  :sha256
  "31c32fbea4d4994ea6f066d6b68f76db947630df0953ee0c8e0ff847fece86bd",
  :bytes 625857,
  :span-end-line 759}})

(def auth-crypto-source-document-history-contract
 {:predecessor-crosswalk
 {:commit "070afe9e051c65efe456b01c7c223b6cfcf4c78f",
  :tree "28187e995d9369f4dcddaf797dcc497b334552ef",
  :path "specs/sh-family-crosswalk/crosswalk.edn",
  :blob "928a52caf58a4660e8a073f5a5541d3630c73ab5",
  :sha256
  "c2439005f9511024e729f9e028879e357204ddf373949b21e0ce64b93a8dec70"},
 :current-criterion
 {:path "docs/acceptance-dependencies.md",
  :source-commit "336b8a7697fe90c49a8eab050c3bc0fcdfc3cf62",
  :source-tree "878c813060a7643b08138b8e1c9646fd922398a3",
  :source-span
  {:start-byte 234973,
   :end-byte 242781,
   :sha256
   "d230cb329b2ebd7ef45f47f8fe4eb8d40c601199de22112d30b5414092714e21"}},
 :predecessor-frame
 {:commit "070afe9e051c65efe456b01c7c223b6cfcf4c78f",
  :tree "28187e995d9369f4dcddaf797dcc497b334552ef",
  :path
  "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn",
  :blob "f57bebd3094df2208fe57594e887086e84d73ee1",
  :sha256
  "a703df5e57cfe97c533f8acd8615efd441ac07300fada15ff61633cc73c67c7d"},
 :review-state :pending-independent-source-version-review,
 :row-source-review
 {:path
  "/tmp/uniform-pr767-auth-crypto-qualified-row-independent-verdict.edn",
  :sha256
  "9bdb59a144fb15d373b0d8ea61f0e98f264d8621fbccdfa773354fe1ba94b389"},
 :authority :current-source-interface-only,
 :source-content-commit "336b8a7697fe90c49a8eab050c3bc0fcdfc3cf62",
 :qualified-row
 {:path "/tmp/uniform-pr767-auth-crypto-qualified-row-preparation.edn",
  :sha256
  "c3536334d4a147e9f49c3f2e871e42b5cc9e851107f3ffb8bc6845e36d23e8a1"},
 :kind :authenticated-auth-crypto-source-document-successor-v1,
 :qualification
 "Only the selected Auth/Crypto declarative pregrant request/policy interface row changes. Auth independently derives a genuine scoped grant before checked Crypto admission and protected action; admitted descriptors, provider/state/neutrality results and execution/refusal/audit traces cannot authorize their own first action. The exact complete070a predecessor frame, all49 old transitions,31 library-IO joins,40 future observations, all578 raw subjects, IDs, task gaps, statuses and every prior review remain preserved at their original subjects. This document identity successor supplies no realized grant/provider/key/action, implementation, semantic scope, migration, Main, release or full240 acceptance.",
 :source-content-tree "878c813060a7643b08138b8e1c9646fd922398a3",
 :changed-source-paths ["docs/acceptance-dependencies.md"],
 :implementation-acceptance :unassessed})

(def auth-crypto-source-document-transition
  (let [path (:path auth-crypto-source-document-data)
        prior (first (filter #(= path (:path %)) build-consumer-current-source-transition-contracts))]
    (assoc (composition-source-transition auth-crypto-source-document-data)
           :successor-of prior :document-id "auth-crypto-declarative-pregrant-source"
           :qualification (:qualification auth-crypto-source-document-history-contract))))

(def current-source-transition-contracts
  (mapv #(if (= (:path auth-crypto-source-document-data) (:path %)) auth-crypto-source-document-transition %)
        build-consumer-current-source-transition-contracts))

(def current-source-transition-paths
  (set (map :path current-source-transition-contracts)))

(declare git-commit-tree git-blob-id git-diff-paths git-source)

(def current-source-transition-successor-history-contract
  {:kind :authenticated-current-source-transition-successor-history-v1
   :predecessor {:commit "b0f1d04188c7d3eada1e4e988b207534e738577c"
                 :tree "d6bd9ceb7de89a1ca582509df68c9e7f37c4e4d6"
                 :path "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn"
                 :blob "7291d9d62b87791a82b43ebff681e1bae3e3f73a"
                 :sha256 "2fb8583291bf36a64a269d18d5bbe209f4389903c409d0d9ffd55b53926da57c"}
   :transition-count (count current-source-transition-contracts-v1)
   :transition-paths (vec (sort (map :path current-source-transition-contracts-v1)))
   :authority :current-source-interface-only
   :review-state :superseded-current-source-transition-frame
   :qualification "The b0f1 current-source transition frame is retained as immutable history. Its 28 exact transition records remain recoverable from the authenticated predecessor EDN while the composed cc8 source checkpoint is bound as a separate successor; no historical pin, source authority or implementation acceptance is renewed."})
(def current-source-successor-predecessor-frame-contract
  {:kind :authenticated-current-source-frame-history-v1
   :commit "b0f1d04188c7d3eada1e4e988b207534e738577c"
   :tree "d6bd9ceb7de89a1ca582509df68c9e7f37c4e4d6"
   :path "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn"
   :blob "7291d9d62b87791a82b43ebff681e1bae3e3f73a"
   :sha256 "2fb8583291bf36a64a269d18d5bbe209f4389903c409d0d9ffd55b53926da57c"
   :transition-count 28
   :source-input-commit "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
   :source-input-tree "3fc8705088dddb2984bd27181ecec3f0b6066883"
   :authority :current-source-interface-only
   :review-state :superseded-current-source-frame
   :qualification "The b0f1 current-source frame is retained as immutable history while the composed cc8 source checkpoint advances; all prior source pins, evidence digests and implementation statuses remain outside this successor."})
(defn validate-current-source-transition-successor-history!
  [history read-ref sha256-fn require!]
  (require! (= history current-source-transition-successor-history-contract)
            "Current source transition successor history changed")
  (let [{:keys [commit tree path blob sha256]} (:predecessor history)
        predecessor-source (read-ref commit path)]
    (require! (= tree (git-commit-tree "." commit require!))
              "Current source transition successor predecessor tree changed")
    (require! (= blob (git-blob-id predecessor-source))
              "Current source transition successor predecessor Git blob changed")
    (require! (= sha256 (sha256-fn predecessor-source))
              "Current source transition successor predecessor bytes changed")
    (let [snapshot (edn/read-string predecessor-source)]
      (require! (= current-source-transition-contracts-v1
                   (:current-source-transitions snapshot))
                "Current source transition successor predecessor records changed")))
  history)

(def source-successor-segment-specs
  [{:id :foundation-30526
    :parent "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
    :parent-tree "3fc8705088dddb2984bd27181ecec3f0b6066883"
    :candidate "30526bfab07e8bb7c9321955f202d948bf12a713"
    :candidate-tree "f1bce736fd420d896774837769e628563fb5a845"
    :review-receipt "/tmp/uniform-pr767-foundation-30526-independent-review.edn"
    :review-state :accepted-scoped-source-only
    :clause-count 62 :reused-predicate-count 8 :path-count 23
    :implementation-execution :none}
   {:id :policy-ef88
    :parent "30526bfab07e8bb7c9321955f202d948bf12a713"
    :parent-tree "f1bce736fd420d896774837769e628563fb5a845"
    :candidate "ef88f29542da85d935a8b6e52e49ccbec05340ef"
    :candidate-tree "0b4fe64b1fbcba36c8e4fb48c5ca7fd0af1af69b"
    :review-receipt "/tmp/uniform-pr767-policy-ef88-independent-review.edn"
    :review-state :accepted-scoped-policy-source-only
    :human-decision :L12-declared-replayable-inputs
    :path-count 9 :implementation-execution :none}
   {:id :l6-d2a
    :parent "ef88f29542da85d935a8b6e52e49ccbec05340ef"
    :parent-tree "0b4fe64b1fbcba36c8e4fb48c5ca7fd0af1af69b"
    :candidate "d2a0378bdd530857d97d530aab15268fc333ce80"
    :candidate-tree "ff13ce6160d46b982125face622e5028b4e1cb38"
    :review-receipt "/tmp/uniform-pr767-L6-d2a-independent-review.edn"
    :review-state :accepted-scoped-canonical-source-only
    :reviewed-span-count 2 :path-count 1 :implementation-execution :none}
   {:id :facet-f6d
    :parent "d2a0378bdd530857d97d530aab15268fc333ce80"
    :parent-tree "ff13ce6160d46b982125face622e5028b4e1cb38"
    :candidate "f6d26ebfde3f2880ae888b3831e00915e524ea5d"
    :candidate-tree "0c79dac66184c98a59c0709e6b78824f56fb505b"
    :review-receipt "/tmp/uniform-pr767-facet-f6d-independent-review.edn"
    :review-state :pending-independent-source-version-review
    :clause-count 18 :finite-predicate-count 3 :reused-predicate-count 4
    :path-count 27 :implementation-execution :none}
   {:id :alternative-ec850
    :parent "f6d26ebfde3f2880ae888b3831e00915e524ea5d"
    :parent-tree "0c79dac66184c98a59c0709e6b78824f56fb505b"
    :candidate "ec850dbdb943d821126ef9682ade7f3a8142764a"
    :candidate-tree "55ba81473d03b830dc4e9ad0703facea10f0b38d"
    :review-receipt "/tmp/uniform-alternative-types-actual-audit.edn"
    :author-receipt "/tmp/uniform-alternative-types-author-tuple.edn"
    :review-state :pending-independent-source-version-review
    :clause-count 21 :reused-predicate-count 4 :path-count 28
    :implementation-execution :none}
   {:id :interop-1a366
    :parent "ec850dbdb943d821126ef9682ade7f3a8142764a"
    :parent-tree "55ba81473d03b830dc4e9ad0703facea10f0b38d"
    :candidate "1a3661896240b5781b89ea28b73f6b841e01bbd6"
    :candidate-tree "54e8f3acf7f81f5461b27de9c5404866bd124cdd"
    :review-receipt "/tmp/uniform-interop-actual-audit.edn"
    :author-receipt "/tmp/uniform-interop-author-tuple.edn"
    :review-state :pending-independent-source-version-review
    :clause-count 26 :finite-predicate-count 2 :reused-predicate-count 3
    :path-count 34 :implementation-execution :none}
   {:id :backend-core-8add
    :parent "1a3661896240b5781b89ea28b73f6b841e01bbd6"
    :parent-tree "54e8f3acf7f81f5461b27de9c5404866bd124cdd"
    :candidate "8addf4db52ba9eba8dd292a766b4bfada592e518"
    :candidate-tree "e6b651c84d61a36560f86d33e0135723d6db88e1"
    :review-receipt "/tmp/uniform-backend-core-actual-audit.edn"
    :author-receipt "/tmp/uniform-backend-core-author-tuple.edn"
    :review-state :pending-independent-source-version-review
    :clause-count 20 :restored-historical-count 7 :path-count 3
    :implementation-execution :none}
   {:id :backend-families-d542
    :parent "8addf4db52ba9eba8dd292a766b4bfada592e518"
    :parent-tree "e6b651c84d61a36560f86d33e0135723d6db88e1"
    :candidate "d542b06ba083b9ea1972682df3b82ecec0818956"
    :candidate-tree "f98bc6e5c467cf528ab37d3709e80c6eba84e68f"
    :review-receipt "/tmp/uniform-backend-families-actual-audit.edn"
    :author-receipt "/tmp/uniform-backend-families-author-tuple.edn"
    :review-state :pending-independent-source-version-review
    :clause-count 80 :restored-historical-count 13 :reused-predicate-count 4
    :path-count 16 :implementation-execution :none}
   {:id :policy-followup-895
    :parent "d542b06ba083b9ea1972682df3b82ecec0818956"
    :parent-tree "f98bc6e5c467cf528ab37d3709e80c6eba84e68f"
    :candidate "895dea1ddbce13b8deae461144e57b8245caefc8"
    :candidate-tree "65c32e6f3677fdbd6492923cb10a002eb00d907a"
    :review-receipt "/tmp/uniform-pr767-policy-followup-895-independent-review.edn"
    :review-state :accepted-scoped-policy-source-only
    :human-decision :L12-declared-replayable-inputs
    :path-count 9 :implementation-execution :none}])
(def source-successor-join-summary-contracts
  (mapv #(select-keys % [:id :clause-count :finite-predicate-count
                         :reused-predicate-count :restored-historical-count
                         :path-count :review-state :implementation-execution])
        source-successor-segment-specs))

(declare git-commit-tree git-blob-id git-diff-paths git-source)

(def source-successor-l6-contract
  {:path "docs/phase-01-core-language/016-l6-effect-system-specification.md"
   :historical-ref "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
   :historical-sha256 "a9d41d970d56d20f0fb74f10c280689407e3ab8fe075c5bc3b71925e9ccec1d5"
   :reviewed-ref "d2a0378bdd530857d97d530aab15268fc333ce80"
   :reviewed-tree "ff13ce6160d46b982125face622e5028b4e1cb38"
   :current-sha256 "529b2cb63e0fe304f628ed63b0e6ef64d86200034f854836399e02e103b41f72"
   :review-receipt "/tmp/uniform-pr767-L6-d2a-independent-review.edn"
   :review-state :accepted-scoped-canonical-source-only
   :binding-status :qualified-current-source-successor
   :reviewed-spans
   [{:id "L6/build-effects"
     :before {:start-byte 12282 :end-byte 12513 :start-line 246 :end-line 256
              :sha256 "e73cf70297c5d8d41bdd51e437fbeb81deadb7e992363c26f6ff11ebfed3453b"}
     :after {:start-byte 12282 :end-byte 13260 :start-line 246 :end-line 263
             :sha256 "c1460f0058e42282eb90b3a5c6455ca01987331e26466d5c043c3d7445b1da8b"}}
    {:id "L6/conformance"
     :before {:start-byte 17137 :end-byte 17217 :start-line 342 :end-line 342
              :sha256 "8927f7381c6443b234b235e2b91795f92a1f953254322d1f22c36025491701d6"}
     :after {:start-byte 17884 :end-byte 18288 :start-line 349 :end-line 353
             :sha256 "9d8f77a73d6121047d53d9cbeca2e5a00ea072fb8adc0d142b90ff5ee1bbc12"}}]})

(defn- source-successor-file-facts [ref path read-ref sha256-fn _require!]
  (let [text (read-ref ref path)
        bytes (.getBytes ^String text "UTF-8")
        lines (count (.split text (str (char 10)) -1))]
    {:ref ref
     :blob (git-blob-id text)
     :bytes (alength bytes)
     :lines lines
     :sha256 (sha256-fn text)
     :span {:start-byte 0 :end-byte (alength bytes)
            :start-line 1 :end-line lines
            :sha256 (sha256-fn text)}}))

(defn validate-source-successor-binding!
  "Authenticate the composed source successor and every reviewed source
  increment.  This is deliberately source/interface provenance only: it does
  not accept implementation, migration, release or whole-source semantics."
  [binding _transitions read-ref read-text sha256-fn require!]
  (let [composition (assoc current-source-successor-composition
                           :current-transition-count (count predecessor-current-source-transition-contracts)
                           :source-census-count (count (:source-census binding)))
        predecessor current-source-successor-predecessor-frame-contract
        expected-segments (into {} (map (juxt :id identity) source-successor-segment-specs))
        segments (:reviewed-source-successors binding)
        segment-map (into {} (map (juxt :id identity) segments))
        source-paths (vec (sort (set (map :path (:source-census binding)))))
        expected-l6 (assoc source-successor-l6-contract
                           :current-ref (:commit current-source-successor-composition)
                           :current-tree (:tree current-source-successor-composition))]
    (require! (= :authenticated-source-successor-binding-v1 (:kind binding))
              "Source successor binding kind changed")
    (require! (= composition (:composition binding))
              "Source successor composition identity changed")
    (require! (= :current-source-interface-only (:authority binding))
              "Source successor authority changed")
    (require! (= :pending-independent-source-version-review (:review-state binding))
              "Source successor review state changed")
    (require! (= :unassessed (:implementation-acceptance binding))
              "Source successor implementation status changed")
    (require! (= :unassessed (:migration-acceptance binding))
              "Source successor migration status changed")
    (require! (= predecessor (:predecessor-current-source-frame binding))
              "Source successor predecessor frame changed")
    (require! (= current-source-transition-successor-history-contract
                 (:predecessor-transition-history binding))
              "Source successor transition history reference changed")
    (require! (= (vec (sort (map :path predecessor-current-source-transition-contracts)))
                 (:current-transition-paths binding))
              "Source successor current-transition population changed")
    (require! (= (count (:current-transition-paths binding))
                 (count (set (:current-transition-paths binding))))
              "Source successor current-transition paths are duplicated")
    (require! (= (set (map :id segments)) (set (keys expected-segments)))
              "Source successor segment population changed")
    (require! (= (count segments) (count (set (map :id segments))))
              "Source successor segment identities are duplicated")
    (doseq [spec source-successor-segment-specs]
      (let [segment (get segment-map (:id spec))
            paths (:paths segment)
            actual-diff (git-diff-paths "." (:parent spec) (:candidate spec) require!)]
        (require! (= (select-keys spec (keys (dissoc spec :path-count)))
                     (select-keys segment (keys (dissoc spec :path-count))))
                  (str "Source successor segment metadata changed: " (:id spec)))
        (require! (= (:path-count spec) (count paths))
                  (str "Source successor segment path count changed: " (:id spec)))
        (require! (= paths (vec (sort paths)))
                  (str "Source successor segment path order changed: " (:id spec)))
        (require! (= (count paths) (count (set paths)))
                  (str "Source successor segment has duplicate paths: " (:id spec)))
        (require! (= actual-diff paths)
                  (str "Source successor segment path census changed: " (:id spec)))
        (require! (= (:parent-tree spec)
                     (git-commit-tree "." (:parent spec) require!))
                  (str "Source successor segment parent tree changed: " (:id spec)))
        (require! (= (:candidate-tree spec)
                     (git-commit-tree "." (:candidate spec) require!))
                  (str "Source successor segment candidate tree changed: " (:id spec)))
        (doseq [path paths]
          (read-ref (:parent spec) path)
          (read-ref (:candidate spec) path))))
    (require! (= source-successor-join-summary-contracts
                 (:source-join-census binding))
              "Source successor join census changed")
    (require! (= expected-l6 (:canonical-l6-successor binding))
              "Canonical L6 successor binding changed")
    (require! (= source-paths (vec (sort source-paths)))
              "Source successor census order changed")
    (require! (= (count source-paths) (count (set source-paths)))
              "Source successor census paths are duplicated")
    (let [expected-paths (vec (sort (set (mapcat :paths segments))))]
      (require! (= expected-paths source-paths)
                "Source successor census does not cover every reviewed source path")
      (doseq [record (:source-census binding)]
        (let [path (:path record)
              segment-ids (vec (sort (for [[id segment] segment-map
                                           :when (some #{path} (:paths segment))]
                                       id)))
              expected {:path path
                        :reviewed-segment-ids segment-ids
                        :before (source-successor-file-facts
                                 "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
                                 path read-ref sha256-fn require!)
                        :after (source-successor-file-facts
                                (:commit current-source-successor-composition)
                                path read-ref sha256-fn require!)}]
          (require! (= expected record)
                    (str "Source successor file identity changed: " path))
          (require! (= (read-ref (:commit current-source-successor-composition) path)
                       (read-text path))
                    (str "Source successor is not bound to checked-out source: " path)))))
    (require! (= [:implementation-acceptance
                  :migration-acceptance
                  :whole-source-acceptance
                  :unreviewed-L17-L19-backend-successors]
                 (:nonclaims binding))
              "Source successor nonclaims changed")
    binding))

(def current-source-transition-history-kind
  :authenticated-current-source-transition-history-v1)
(def current-source-transition-history-predecessor
  {:commit "792a2b4e6397adecbba46f75d785a2c5482977d5"
   :tree "4ba5e96aea175c9f734f28ffbd3c40aed7d3abef"
   :path "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn"
   :blob "55ebe27032dfdb76e7d0c118544af843ea3f72a2"
   :sha256 "986cb8f8f69ba72b89e1850da1f833a8bef12a013b3929ae7938dbfdbf8760b7"})
(def current-source-transition-history-predecessor-input-fingerprints
  ["8edbe4bc736ea749ed688d532c9379a4e1afd878819bf642cb672466ae3a9993"
   "a21dc2f6ae3a62746ca1687b7795b38758558e2c08c0c40298b7a9613690e5cc"])
(def current-source-transition-history-predecessor-source-frame-fingerprint
  "57d4e3415247ff5e27aae9d61b143518d6cca0f0dc3c5d73762048eeb592a3ad")

(declare git-commit-tree git-blob-id)
(defn validate-current-source-transition-history!
  "Keep the predecessor transition frame authenticated when one path advances.
  The current vector intentionally contains only one record per path; the
  superseded record and its predecessor source identity remain explicit history."
  [history read-ref sha256-fn require!]
  (let [superseded-path "specs/auth-workflow/tasks.md"
        expected (first (filter #(= superseded-path (:path %))
                               historical-current-source-transition-contracts))]
    (require! (map? history)
              "Current source transition history is missing")
    (require! (= current-source-transition-history-kind (:kind history))
              "Current source transition history kind changed")
    (require! (= 23 (:record-count history))
              "Current source transition history record count changed")
    (require! (= current-source-transition-history-predecessor
                 (:predecessor history))
              "Current source transition history predecessor changed")
    (require! (= current-source-transition-history-predecessor-input-fingerprints
                 (:predecessor-input-fingerprints history))
              "Current source transition history input fingerprints changed")
    (require! (= current-source-transition-history-predecessor-source-frame-fingerprint
                 (:predecessor-source-frame-fingerprint history))
              "Current source transition history source frame changed")
    (require! (= [superseded-path] (:superseded-paths history))
              "Current source transition history paths changed")
    (require! (= [expected] (:superseded-transitions history))
              "Superseded current source transition was not preserved exactly")
    (require! (= :current-source-interface-only (:authority history))
              "Current source transition history authority changed")
    (require! (= :superseded-source-interface-history (:review-state history))
              "Current source transition history review state changed")
    (let [predecessor current-source-transition-history-predecessor
          {:keys [commit tree path blob] expected-sha256 :sha256} predecessor
          predecessor-source (read-ref commit path)]
      (require! (= tree (git-commit-tree "." commit require!))
                "Current source transition history predecessor tree changed")
      (require! (= blob (git-blob-id predecessor-source))
                "Current source transition history predecessor Git blob changed")
      (require! (= expected-sha256 (sha256-fn predecessor-source))
                "Current source transition history predecessor bytes changed"))
    history))

;; A successor frame must retain the exact prior current-source projection.  A
;; complete-file transition is not permission to discard the predecessor's
;; source counts, pins, digests or review status.
(def current-source-frame-history-kind
  :authenticated-current-source-frame-history-v1)
(def current-source-frame-history-predecessor
  {:commit "ff783a0ab1d324bbf9316e7de6b1db55f189e338"
   :tree "46627ba4a754f934c656c4f67ce65bd5302d1be1"
   :path "specs/sh-family-crosswalk/implementation-readiness-current-source-version.edn"
   :blob "35b19c122d029877dd4b62f2864c85ba01d0da87"
   :sha256 "db750ca20785c29f21babf2b61fc9b90965e0e1a69077a6054533176e4fd1402"
   :source-input-commit "b886fe2599916b1a302e17e02c45eae9ebd8a34d"
   :source-input-tree "1558fa1e9612c72e19ea030e367a6288c0e2d4b7"
   :input-fingerprint "6f062d1a19435638e157aa62b95bbfe950275b84f82d27079186ad12b24dbfce"
   :source-frame-fingerprint "57d4e3415247ff5e27aae9d61b143518d6cca0f0dc3c5d73762048eeb592a3ad"
   :current-source-transition-count 24
   :current-source-history-count 23
   :current-source-path-count 2185
   :applying-pin-count 72
   :source-digest-count 35})
(defn validate-current-source-frame-history!
  [history read-ref sha256-fn require!]
  (require! (= {:kind current-source-frame-history-kind
                :predecessor current-source-frame-history-predecessor
                :authority :current-source-interface-only
                :review-state :superseded-current-source-frame
                :qualification
                "The ff783 current-source frame is retained as immutable history. Its 24 transitions, 23 historical records, 2,185 current-source paths, 72 applying pins, 35 source digests and exact fingerprints remain conserved while STD7-STD9 advances to 4202."}
               history)
            "Current source frame history changed")
  (let [{:keys [commit tree path blob] expected-sha256 :sha256}
        current-source-frame-history-predecessor
        predecessor-source (read-ref commit path)]
    (require! (= tree (git-commit-tree "." commit require!))
              "Current source frame history predecessor tree changed")
    (require! (= blob (git-blob-id predecessor-source))
              "Current source frame history predecessor Git blob changed")
    (require! (= expected-sha256 (sha256-fn predecessor-source))
              "Current source frame history predecessor bytes changed"))
  history)

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

;; STD7-STD9 adds 23 exact criterion-to-owner/consumer joins to the current
;; source frame.  The eight `*-existing` rows are carried predicates, not new
;; implementation tasks; the distinction is authenticated below.
(def std7-std9-reused-criterion-ids
  #{"atomic-existing" "channel-existing" "preservation-existing"
    "profile-existing" "race-analysis-existing" "runtime-concurrency-existing"
    "safety-existing" "structured-existing"})
(def std7-std9-library-io-criterion-data
  [{:id "service-contracts" :owner "stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations" :start 26350 :end 27450 :line 207 :sha "c31b79557f07fd82b59b8ab8ade6f4ff51bdabf1b06ede6a8e26ca2f854fabeb"}
   {:id "concurrency-api" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 27451 :end 28817 :line 208 :sha "b4650307a9261af76c8e0746d8e41ed73df655ae7f51186d2e792f3fd446f928"}
   {:id "channels-and-actors" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 28818 :end 29854 :line 209 :sha "6acee921026c085995e66210eb6fd4d40fca3959f8596b3adf536758d2bf4b9e"}
   {:id "scheduler-and-atomic-apis" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 29855 :end 31151 :line 210 :sha "b3ab686bea6e919df6683314a02fabf7c75e6c645589f827f0104b86584ac4af"}
   {:id "stream-and-handle-apis" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 31152 :end 32238 :line 211 :sha "13129429b60b526b9bed75042a3d97561d5149bd958ca0605c9f4e53fd5f5483"}
   {:id "typed-path-policy" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 32239 :end 33133 :line 212 :sha "ab42f96fd11c402a66b8966e1ac53e2426e618ac00de66cf88c70db54bdda843"}
   {:id "text-io" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 33134 :end 34004 :line 213 :sha "f07d3c06c899c2f489613771de7454868b726a6862bd5baeea77f7111869cb79"}
   {:id "directory-traversal" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 34005 :end 34800 :line 214 :sha "64fd85e3df4e2c6e3d1f3c62fdc1995901438fbc77c7ce5ff72304e23cca3f1a"}
   {:id "atomic-file-replacement" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 34801 :end 35703 :line 215 :sha "fc6bec8e38bc9c7765d8663603cdb258f4008811fa721a11eadbe3b76af47f62"}
   {:id "network-surface" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 35704 :end 36872 :line 216 :sha "bdfa2332f3979c0ee3d54f878d1ee963f26fcb6caed5f8f78626b69da3e38da6"}
   {:id "http-client" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 36873 :end 37999 :line 217 :sha "ca928d41a86300210073da1f6f04ffdf8556d6ff9d49660a71cb668cc0c3ff4a"}
   {:id "http-server" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 38000 :end 39026 :line 218 :sha "b60ede05f2f1f1a1a3806557ac8f105c103df7902e71c406bacad485cecf5b51"}
   {:id "tls-policy" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 39027 :end 39953 :line 219 :sha "2817598718a5b9bbb50559290da0b016c9247501b71cc80e435cf1c4773aa4eb"}
   {:id "network-schema-boundary" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 39954 :end 40949 :line 220 :sha "e795a01ad166a52270b45f23cdd6cb7be5d5b0593b309309e9782ee0fa2bd063"}
   {:id "service-profile-matrix" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 40950 :end 42329 :line 221 :sha "3ee352972dc29677aa8b026fb9c3f2a20390a0f8cb8f862497246bc7b2aef366"}
   {:id "resource-cleanup" :owner "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup" :start 42330 :end 43340 :line 222 :sha "92a7ba41928ae3583c182ccdde662d8365a5cd6724449c6853fc4132e3a1ba80"}
   {:id "recorded-service-replay" :owner "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution" :start 43341 :end 44622 :line 223 :sha "7e6c5aa3bdc0487ad414bfa6cf8a52cfad839ccb74cabfd7fcb2569529c16708"}
   {:id "actual-service-enforcement" :owner "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration" :start 44623 :end 45773 :line 224 :sha "34904257fb26ca811ad16449ec91d3f8177f378c7f46fe383af39788c617f6e4"}
   {:id "service-observation" :owner "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration" :start 45774 :end 46864 :line 225 :sha "5a23e9fee1b80ef184fb53d66cf74dc51af59249d46b96c35977df559f2665e6"}
   {:id "service-effect-admission" :owner "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering" :start 46865 :end 47992 :line 226 :sha "8863a2c90e779286172f8f7b6cd38eada3fa320d5d28777edee187e02c938768"}
   {:id "operation-grants" :owner "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime" :start 47993 :end 49125 :line 227 :sha "a94a42e78d8440e6dcb172d4f90fb135692933ef85ef2ebdf54a703b6cf1ca6c"}
   {:id "unsafe-service-wrappers" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 49126 :end 50134 :line 228 :sha "bd8e4c6aabae7749d61b665023b53ef0bb7a39264a5641cdc90967baaf3e0184"}
   {:id "library-service-conformance" :owner "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests" :start 50135 :end 51582 :line 229 :sha "176f554a076d04f5eea155ee7c68b0d194ed02e5f5b72b70caaf5c0b39dcea5e"}
   {:id "atomic-existing" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 51583 :end 52192 :line 230 :sha "fe1c527506170e667ac19c7faee7e8255fd35c5333af7452fbea83b3de791569"}
   {:id "channel-existing" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 52192 :end 52814 :line 231 :sha "e43b6fc42c2632c1447862d46ce7c41f33e57e8a5f9ff4c4cb756e44ab19b868"}
   {:id "preservation-existing" :owner "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution" :start 52814 :end 53858 :line 232 :sha "18a148170b6bc76c0c691d690b3eb9ca758c5a1b136c513fe8ed11e2e12f5de4"}
   {:id "profile-existing" :owner "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks" :start 53858 :end 54717 :line 233 :sha "43c194cfee2f0f0a026de07d611cc9436fa42cae003d431a63368a0fddacba92"}
   {:id "race-analysis-existing" :owner "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis" :start 54717 :end 55764 :line 234 :sha "1e8af63b32c741351cf1fda8ad7302b9d620ea2f1da23935115187164b52f07b"}
   {:id "runtime-concurrency-existing" :owner "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries" :start 55764 :end 57078 :line 235 :sha "4b6714e6b0e6685d60d9ed5e4b1339b5f3040459f826772d2b416dc1eae6ccc4"}
   {:id "safety-existing" :owner "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries" :start 57078 :end 58588 :line 236 :sha "cf41a2cff0de1ddca2f07e3318c1f9048a1f537b29636f3474c6bfee73368563"}
   {:id "structured-existing" :owner "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis" :start 58588 :end 59232 :line 237 :sha "c0b89b2a160d0312125851f6644d3f41ef558cd486c0d543091630aa255e3ad7"}])
(def std7-std9-library-io-criterion-contracts
  (mapv (fn [{:keys [id owner start end line sha]}]
          {:id id
           :owner owner
           :consumer-task-id owner
           :path "specs/stdlib-workflow/plan.md"
           :source-commit "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
           :source-tree "3fc8705088dddb2984bd27181ecec3f0b6066883"
           :source-blob "4d7521645cd41846736b7c522979e8838d6aa641"
           :source-sha256 "64e60b71d94fc5b12c3f9b342a21f573fc078c2d11b3b7e99fe7c6ae45117f9a"
           :source-span {:start-byte start :end-byte end :start-line line :end-line line :sha256 sha}
           :relation (if (contains? std7-std9-reused-criterion-ids id)
                       :existing-exact-predicate
                       :new-current-source-criterion)
           :reused-predicate? (contains? std7-std9-reused-criterion-ids id)
           :authority :current-source-interface-only
           :review-state :pending-independent-source-version-review
           :implementation-acceptance :unassessed})
        std7-std9-library-io-criterion-data))

;; The original STD7-STD9 joins remain an immutable 4202 record.  The current
;; source successor keeps those exact criterion spans and joins, but binds them
;; to the composed source bytes so a later source transition cannot be mistaken
;; for a change to the accepted historical record.
(def current-source-library-io-criterion-contracts
  (mapv (fn [join]
          (assoc join
                 :source-commit (:commit current-source-successor-composition)
                 :source-tree (:tree current-source-successor-composition)
                 :source-blob "2c11af264fb81cb0b7e7aa74fc7345ab20592cc2"
                 :source-sha256 "90b45f98672fac1400a9ee13e0c844d9c98aac473eb3f7063a89163b0dd9eb35"
                 :successor-of {:source-commit (:source-commit join)
                                :source-tree (:source-tree join)
                                :source-blob (:source-blob join)
                                :source-sha256 (:source-sha256 join)}
                 :qualification
                 (str "The current-source successor preserves the exact STD7-STD9 criterion/owner join "
                      (:id join) " while rebinding its unchanged span to the composed current source. "
                      "The 4202 join remains immutable historical evidence; this source/interface successor "
                      "confers no implementation, migration or whole-source acceptance.")))
        std7-std9-library-io-criterion-contracts))

(defn validate-std7-std9-library-io-criterion-joins!
  [joins read-ref read-text sha256 require!]
  (require! (= joins std7-std9-library-io-criterion-contracts)
            "STD7-STD9 library IO criterion/owner join contract changed")
  (require! (= 31 (count joins)) "STD7-STD9 library IO criterion count changed")
  (require! (= 31 (count (set (map :id joins))))
            "STD7-STD9 library IO criterion identities are not unique")
  (require! (= 23 (count (filter #(= :new-current-source-criterion (:relation %)) joins)))
            "STD7-STD9 new criterion count changed")
  (require! (= std7-std9-reused-criterion-ids
               (set (for [{:keys [id reused-predicate?]} joins :when reused-predicate?] id)))
            "STD7-STD9 reused predicate population changed")
  (doseq [{:keys [id owner consumer-task-id path source-commit source-tree source-blob
                  source-sha256 source-span relation reused-predicate?]} joins]
    (require! (= owner consumer-task-id)
              (str "STD7-STD9 criterion owner/consumer mismatch: " id))
    (require! (= "specs/stdlib-workflow/plan.md" path)
              (str "STD7-STD9 criterion source path changed: " id))
    (require! (= "4202c7760b586ec25b9ea69040b6bb8a75ec4919" source-commit)
              (str "STD7-STD9 criterion source commit changed: " id))
    (require! (= "3fc8705088dddb2984bd27181ecec3f0b6066883" source-tree)
              (str "STD7-STD9 criterion source tree changed: " id))
    (require! (= "4d7521645cd41846736b7c522979e8838d6aa641" source-blob)
              (str "STD7-STD9 criterion source blob changed: " id))
    (require! (= "64e60b71d94fc5b12c3f9b342a21f573fc078c2d11b3b7e99fe7c6ae45117f9a" source-sha256)
              (str "STD7-STD9 criterion source digest changed: " id))
    (require! (= (if reused-predicate? :existing-exact-predicate :new-current-source-criterion)
                 relation)
              (str "STD7-STD9 criterion relation changed: " id))
    (require! (= source-tree (git-commit-tree "." source-commit require!))
              (str "STD7-STD9 criterion source tree is not authenticated: " id))
    (let [source (read-ref source-commit path)
          _current (read-text path)
          {:keys [start-byte end-byte start-line end-line]} source-span
          span-sha256 (:sha256 source-span)
          raw (utf8-span-text source start-byte end-byte require!)]
      (require! (= source-blob (git-blob-id source))
                (str "STD7-STD9 criterion source Git blob changed: " id))
      (require! (= source-sha256 (sha256 source))
                (str "STD7-STD9 criterion source bytes changed: " id))
      (require! (= span-sha256 (sha256 raw))
                (str "STD7-STD9 criterion span bytes changed: " id))
      (require! (= start-line (utf8-line-number source start-byte))
                (str "STD7-STD9 criterion start line changed: " id))
      (require! (= end-line (utf8-line-number source (dec end-byte)))
                (str "STD7-STD9 criterion end line changed: " id))
      (require! (str/includes? raw (str "`" id "`"))
                (str "STD7-STD9 criterion identity is absent from source span: " id))
      (require! (str/includes? raw (str "`" owner "`"))
                (str "STD7-STD9 criterion owner is absent from source span: " id)))
  joins))

(defn validate-prior-current-source-library-io-criterion-joins!
  [joins read-ref read-text sha256 require!]
  (require! (= joins current-source-library-io-criterion-contracts)
            "Current-source STD7-STD9 library IO successor changed")
  (require! (= 31 (count joins))
            "Current-source STD7-STD9 library IO criterion count changed")
  (require! (= 31 (count (set (map :id joins))))
            "Current-source STD7-STD9 library IO criterion identities are not unique")
  (doseq [{:keys [id owner path source-commit source-tree source-blob source-sha256
                  source-span successor-of]} joins]
    (require! (= "specs/stdlib-workflow/plan.md" path)
              (str "Current-source STD7-STD9 criterion path changed: " id))
    (require! (= (:commit current-source-successor-composition) source-commit)
              (str "Current-source STD7-STD9 criterion commit changed: " id))
    (require! (= (:tree current-source-successor-composition) source-tree)
              (str "Current-source STD7-STD9 criterion tree changed: " id))
    (require! (= "2c11af264fb81cb0b7e7aa74fc7345ab20592cc2" source-blob)
              (str "Current-source STD7-STD9 criterion blob changed: " id))
    (require! (= "90b45f98672fac1400a9ee13e0c844d9c98aac473eb3f7063a89163b0dd9eb35"
                 source-sha256)
              (str "Current-source STD7-STD9 criterion digest changed: " id))
    (require! (= {:source-commit "4202c7760b586ec25b9ea69040b6bb8a75ec4919"
                  :source-tree "3fc8705088dddb2984bd27181ecec3f0b6066883"
                  :source-blob "4d7521645cd41846736b7c522979e8838d6aa641"
                  :source-sha256 "64e60b71d94fc5b12c3f9b342a21f573fc078c2d11b3b7e99fe7c6ae45117f9a"}
                 successor-of)
              (str "Current-source STD7-STD9 predecessor join changed: " id))
    (require! (= source-tree (git-commit-tree "." source-commit require!))
              (str "Current-source STD7-STD9 criterion tree is not authenticated: " id))
    (let [source (read-ref source-commit path)
          current (read-text path)
          {:keys [start-byte end-byte start-line end-line]} source-span
          span-sha256 (:sha256 source-span)
          raw (utf8-span-text source start-byte end-byte require!)]
      (require! (= source current)
                (str "Current-source STD7-STD9 criterion source is not current: " id))
      (require! (= source-blob (git-blob-id source))
                (str "Current-source STD7-STD9 criterion Git blob changed: " id))
      (require! (= source-sha256 (sha256 source))
                (str "Current-source STD7-STD9 criterion bytes changed: " id))
      (require! (= span-sha256 (sha256 raw))
                (str "Current-source STD7-STD9 criterion span changed: " id))
      (require! (= start-line (utf8-line-number source start-byte))
                (str "Current-source STD7-STD9 criterion start line changed: " id))
      (require! (= end-line (utf8-line-number source (dec end-byte)))
                (str "Current-source STD7-STD9 criterion end line changed: " id))
      (require! (str/includes? raw (str "`" id "`"))
                (str "Current-source STD7-STD9 criterion identity is absent: " id))
      (require! (str/includes? raw (str "`" owner "`"))
                (str "Current-source STD7-STD9 criterion owner is absent: " id)))
  joins))

(def source-composition-future-task-records
[{:id "automation",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "c0d940760c13936487d8065806937d98852b711242806d86fab66d684ce90f39"}
 {:id "blockchain",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "da21d377d58d70dba73dbd3cccc91c718661c596dc9bc2ba262ab89e3dd767df"}
 {:id "crypto",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "14dd4ffeb48ca157548f4b3bc0fe40b8b58d982af6b6d5d175a10d3306fc061c"}
 {:id "formal",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "468632c36cc7b57fbcfe0fee8f3961ce83d34bdcefbc440585a4d835829ac4d6"}
 {:id "native",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "af96d8e0dc3c008b2aef077da854396109c2519f9bd407284a4e542077bef1b8"}
 {:id "services",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "a85cdfee26c4448ad5efd9d008431da16e3a42369eb403de2a86e7f569d1d749"}
 {:id "simulation",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "f9d45597ff23e8679b8c82bd5c5c9440ce7e74c3b69127e0c2a0cf18e9a1e7ac"}
 {:id "storage",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "18954329abf5f416d8576a86c8b2a806ba5f6e45d27fe1dd8ad64296b208405a"}
 {:id "visual",
  :region "domains-wider-integration-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "a48950198a5ec0be8f0632378886ed42fb12179b2f20da6d2ea9731212c7e469"}
 {:id "math/eml-normalization-and-search",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "e8fc3b4ec5bba68d8dbd9985ba5d73f6954d8700d1327d66ca943e09e37726f7"}
 {:id "math/certified-implementation-synthesis",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "92f7f81f218555f86406d36f45410577f2bfaa66e1c4bbf1c072d0b3afaa32de"}
 {:id "math/interval-and-real-proof-production",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "4dd9a3200cda862add2e4e5a6bb634f38795787fc97d07f74235dc3c067be7de"}
 {:id "math/symbolic-rule-and-trace-production",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "9f80100cd7fc4c576370d7890536c8e6c26ac7a39360d6e1946b5efc2bd0de59"}
 {:id "math/independent-proof-and-certificate-checking",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "ba8bc4e7ab2bbfd42b182655e684a5629d2e0d1f5153d81e469f1f8c8ca36bed"}
 {:id "schema/compatibility-and-migration",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "03c13f0584589ae0399bef9726510206fcbaaedc51f4b754fd3e6c5927456859"}
 {:id "schema/checked-consumer-projection",
  :region "domains-math-schema-proposals",
  :component "domains",
  :path "specs/domains-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "95db9c16125a5b653c7b950a1a1786b5cfb46c753bc9120f5ae6402908ef430e",
  :body-sha256
  "8749844f61158b1cf15fee34f14ea990fdd7f7e88238245911d362a6ad5130ef"}
 {:id
  "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "8d6215523bd324b10e17e58482aa6955b277041afd3ce0fdeca6b4c6f07bebe4"}
 {:id
  "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "551177a2b0e469e75657bc5d119b1613c61568e329343eee5b3251edcb54b743"}
 {:id
  "release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "a04fe57ac3daaf5cf002598c8ccf6cd6682080dfd5cda40833f2695f5fecb37c"}
 {:id
  "release/remaining-tasks-and-evidence/execute-formal-evidence-checks",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "ea0c2bf29a692e6e5e81c8393854a9e7cdfadd225cf079bbf3b811469b0f9777"}
 {:id
  "release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "9d7d7069d0b7a571902cf6b934b5f6f5a6cd30e0b31139db109c718725ab16df"}
 {:id
  "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "18bb750d2ab7cad44d643b2fa67c60bb5c61c16bd2f74e98720a5e2efde39c69"}
 {:id
  "release/remaining-tasks-and-evidence/execute-library-conformance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "290f29b392f82b8797ca86efac46acefa11a81c0131787935de679cd24c3b813"}
 {:id
  "release/remaining-tasks-and-evidence/execute-performance-regression-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "beb2425e264fe00eed110f9e675f0e98a340c8cb6a875265f96cc47b7b855ba8"}
 {:id
  "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "015036322a7cc39d779dcc4d2985f0eabc6d9cba6ca8b2de09a318b7226b3acb"}
 {:id
  "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "85511d988b1316b76b4a26e9da9ef7f6c2e55d38ce2602143db4b4943ba4c0c9"}
 {:id
  "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "50f51c2e4b1cd734a46d18d0a16e1edfa96aa9784f456c2e0c795a4214432308"}
 {:id
  "release/remaining-tasks-and-evidence/execute-staged-compiler-validation",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "de657d84993e1332296f00a25bfc896ba19edc09716bb9cc0222d4cbbae8b947"}
 {:id
  "release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations",
  :region "release-testing-proposals",
  :component "release",
  :path "specs/release-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9e27b1f19437fbacb1945981d4c3cb418145d71b99da0e7d39bdb3130bb09d98",
  :body-sha256
  "ee83573983288ce10a6d898596408e92ad61d835fa9f42d983c641a403cd0f71"}
 {:id "platform-product/adopt-hardware-contract",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "fec0124ae53255813304b7182f65923eafcf442c13c00602a05f63fbc1b007cf"}
 {:id "platform-product/adopt-kernel-contract",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "7e963af8bdd06f1a78e328feb3577cb86291d4094d9a9be9d31a703267ef0d88"}
 {:id "platform-product/adopt-application-contract",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "d6ae874658fed492c8c015c908419b3b9c3e2c277d64e2148d77ae40a9453c15"}
 {:id "platform-product/adopt-physical-boundary",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "b6d5ae5225691588cd5016c59246efdee92696a2d565e731e7479b983324639d"}
 {:id "platform-product/implement-hardware",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "9afd6d0715b4614216813137210b85d49763af277b2985e7afc2cd1bc9d52685"}
 {:id "platform-product/implement-firmware",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "ea937a0345b50e2a446c8cdd03b82538addd59da12315e1d02ba47c7796717e9"}
 {:id "platform-product/implement-os",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "dc1f4223d6182750a5c63737bc3985178d9c3f78629cee426380d46251db4a10"}
 {:id "platform-product/implement-drivers",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "4abb5a6001e4cb2f62c1dbc1d72d750a685dacafa414f46accbb2c0714263f54"}
 {:id "platform-product/implement-applications",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "981134ff3dd2d3f9b04cd48bf901251ea0966943dae9d068f96b13699d38e6b5"}
 {:id "platform-product/integrate-execution",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "a9e22d269cfc79831a3777d6f228d32703b1fe6a141c24d8231436e0376f1d1f"}
 {:id "platform-product/distribute-product",
  :region "platform-product-implementation-tasks",
  :component "platform",
  :path "specs/platform-workflow/tasks.md",
  :checked false,
  :source-commit "1615d7a37b7c35d8ad4dabf7d719e906e75277c6",
  :file-sha256
  "9d70f86f192379e5b7e70144766781128a5823d3cd8ffe637bf932976e98a5fb",
  :body-sha256
  "1be8a612524fecf067df3ad086880bfef4d62ca4296b3ad568f5d2794e1797de"}]
)

(def source-composition-library-io-criterion-contracts
  (let [observations (into {} (map (juxt :id identity) source-composition-library-io-span-data))
        current (->> source-composition-file-data (filter #(= "specs/stdlib-workflow/plan.md" (:path %))) first :after)]
    (mapv (fn [prior]
            (let [observation (observations (:id prior))]
              (when-not observation (throw (ex-info "Missing current library IO source span" {:id (:id prior)})))
              (assoc prior :source-commit (:commit current) :source-tree (:tree current) :source-blob (:blob current)
                     :source-sha256 (:sha256 current) :source-span (:current-span observation) :successor-of prior
                     :qualification "The prior join remains complete history. This current source binding preserves the unique exact predicate bytes and owner while separately authenticating the current file and UTF8 span. No receipt, task acceptance, implementation, migration or release claim is renewed.")))
          current-source-library-io-criterion-contracts)))

(def ^:private read-composition-source-edn (memoize edn/read-string))

(defn- authenticated-source-payloads
  [descriptor read-ref sha256 require!]
  (let [{:keys [commit tree path blob] expected-sha :sha256} descriptor
        text (read-ref commit path)]
    (require! (= tree (git-commit-tree "." commit require!)) "Prior crosswalk source commit/tree drift")
    (require! (= blob (git-blob-id text)) "Prior crosswalk source blob drift")
    (require! (= expected-sha (sha256 text)) "Prior crosswalk source bytes drift")
    (let [data (read-composition-source-edn text) snapshots (:source-snapshots data)]
      (require! (and (= :sh-family-derived-crosswalk (:kind data)) (= 2 (:schema-version data))
                     (map? snapshots) (seq snapshots)) "Prior crosswalk lacks its authenticated payload table")
      (doseq [[digest raw] snapshots]
        (require! (and (string? raw) (= digest (sha256 raw))) "Prior crosswalk source payload identity drift"))
      snapshots)))

(defn source-composition-predecessor-snapshots
  "Retain both earlier authenticated payload tables before adding current subjects."
  [read-ref sha256 require!]
  (let [original (authenticated-source-payloads (:predecessor-crosswalk source-composition-history-contract) read-ref sha256 require!)
        previous (authenticated-source-payloads (:predecessor-crosswalk source-consumer-file-history-contract) read-ref sha256 require!)
        latest (authenticated-source-payloads (:predecessor-crosswalk auth-crypto-source-document-history-contract) read-ref sha256 require!)]
    (doseq [[digest raw] original]
      (require! (= raw (get previous digest)) "Consumer source successor loses an earlier payload subject"))
    (doseq [[digest raw] previous]
      (require! (= raw (get latest digest)) "Auth Crypto source successor loses a prior raw payload subject"))
    (merge original previous latest)))

(defn validate-auth-crypto-source-document-history!
  "Authenticate the complete previous49-transition frame before one qualified document successor."
  [frame read-ref sha256 require!]
  (let [history (get-in frame [:source-composition-transition-history :auth-crypto-source-document-successor])
        {:keys [commit tree path blob] expected-sha :sha256} (:predecessor-frame auth-crypto-source-document-history-contract)
        text (read-ref commit path) previous (read-composition-source-edn text)
        inverse (-> frame (assoc :current-source-transitions (:current-source-transitions previous))
                    (update :source-composition-transition-history dissoc :auth-crypto-source-document-successor))]
    (require! (= auth-crypto-source-document-history-contract history) "Auth Crypto source history identity or qualification changed")
    (require! (= tree (git-commit-tree "." commit require!)) "Auth Crypto predecessor is not the actual commit/tree")
    (require! (= blob (git-blob-id text)) "Auth Crypto predecessor frame blob drift")
    (require! (= expected-sha (sha256 text)) "Auth Crypto predecessor frame bytes drift")
    (require! (= build-consumer-current-source-transition-contracts (:current-source-transitions previous)) "Auth Crypto source history loses a complete prior transition")
    (require! (= current-source-transition-contracts (:current-source-transitions frame)) "Auth Crypto current transitions differ from exact source contracts")
    (require! (= previous inverse) "Auth Crypto successor changes unlisted prior frame values")
    previous))

(defn validate-build-consumer-file-history!
  "Authenticate the exact previous frame while advancing only the Build file."
  [frame read-ref sha256 require!]
  (let [history (get-in frame [:source-composition-transition-history :source-consumer-file-successor])
        {:keys [commit tree path blob] expected-sha :sha256} (:predecessor-frame source-consumer-file-history-contract)
        text (read-ref commit path) previous (read-composition-source-edn text)
        inverse (-> frame
                    (assoc :current-source-transitions (:current-source-transitions previous))
                    (update :source-composition-transition-history dissoc :source-consumer-file-successor))]
    (require! (= source-consumer-file-history-contract history)
              "Build consumer source successor history identity or qualification changed")
    (require! (= tree (git-commit-tree "." commit require!)) "Build consumer predecessor is not the actual commit/tree")
    (require! (= blob (git-blob-id text)) "Build consumer predecessor frame blob drift")
    (require! (= expected-sha (sha256 text)) "Build consumer predecessor frame bytes drift")
    (require! (= source-composition-current-source-transition-contracts (:current-source-transitions previous))
              "Build consumer source successor loses its complete previous transition table")
    (require! (= build-consumer-current-source-transition-contracts (:current-source-transitions frame))
              "Build consumer source successor current transitions differ from exact contracts")
    (require! (= previous inverse) "Build consumer source successor changes unlisted previous frame values")
    previous))

(defn validate-source-consumer-file-history!
  "Validate the current Auth-document and historical Build source successors in their own frames."
  [frame read-ref sha256 require!]
  (validate-build-consumer-file-history!
    (validate-auth-crypto-source-document-history! frame read-ref sha256 require!) read-ref sha256 require!))

(defn validate-source-composition-history!
  [frame read-ref sha256 require!]
  (let [frame (validate-source-consumer-file-history! frame read-ref sha256 require!)
        {:keys [commit tree path blob] expected-sha :sha256} (:predecessor source-composition-history-contract)
        text (read-ref commit path)
        previous (read-composition-source-edn text)
        inverse (-> frame
                    (assoc :current-source-transitions (:current-source-transitions previous)
                           :current-source-library-io-criterion-joins (:current-source-library-io-criterion-joins previous))
                    (dissoc :source-composition-transition-history :source-composition-future-task-records))]
    (require! (= source-composition-history-contract (:source-composition-transition-history frame))
              "Current source composition history identity, scope or authority changed")
    (require! (= tree (git-commit-tree "." commit require!)) "Source composition predecessor is not the actual commit/tree")
    (require! (= blob (git-blob-id text)) "Source composition predecessor frame blob changed")
    (require! (= expected-sha (sha256 text)) "Source composition predecessor frame bytes changed")
    (require! (= previous inverse) "Source composition changes unlisted predecessor frame values")
    (require! (= predecessor-current-source-transition-contracts (:current-source-transitions previous))
              "Source composition loses its complete prior transition table")
    (require! (= current-source-library-io-criterion-contracts (:current-source-library-io-criterion-joins previous))
              "Source composition loses its complete prior library IO joins")
    (require! (= source-composition-current-source-transition-contracts (:current-source-transitions frame))
              "Source composition current transition table differs from exact contracts")
    (require! (= source-composition-library-io-criterion-contracts (:current-source-library-io-criterion-joins frame))
              "Source composition current library IO joins differ from exact contracts")
    (require! (= source-composition-future-task-records (:source-composition-future-task-records frame))
              "Source composition current future-task observations differ from exact contracts")
    (let [old-error (get-in previous [:error-refinement :future-task-source-successor :after])
          old-protocol (get-in previous [:protocol-refinement :future-task-source-successor :after])
          stable-fields [:id :region :component :path :checked]
          stable (fn [rows] (into {} (map (fn [row] [(:id row) (select-keys row stable-fields)]) rows)))]
      (require! (= old-error old-protocol) "Source composition predecessor future-task subjects disagree")
      (require! (= (stable old-error) (stable source-composition-future-task-records))
                "Source composition changes future-task identities, routing or checked state"))
    (:source-composition-transition-history frame)))

(defn validate-current-source-library-io-criterion-joins!
  [joins read-ref read-text sha256 require!]
  (require! (= source-composition-library-io-criterion-contracts joins)
            "Current library IO source composition contracts changed")
  (validate-prior-current-source-library-io-criterion-joins!
   current-source-library-io-criterion-contracts read-ref
   #(read-ref (:commit current-source-successor-composition) %) sha256 require!)
  (doseq [{:keys [id owner consumer-task-id path source-commit source-tree source-blob source-sha256 source-span successor-of]} joins]
    (let [text (read-ref source-commit path)
          prior (first (filter #(= id (:id %)) current-source-library-io-criterion-contracts))
          raw (utf8-span-text text (:start-byte source-span) (:end-byte source-span) require!)
          old-text (read-ref (:source-commit prior) path)
          old-span (:source-span prior)
          old-raw (utf8-span-text old-text (:start-byte old-span) (:end-byte old-span) require!)]
      (require! (= prior successor-of) "Current library IO join loses its complete predecessor")
      (require! (= source-tree (git-commit-tree "." source-commit require!)) "Current library IO source commit/tree drift")
      (require! (= source-blob (git-blob-id text)) "Current library IO source blob drift")
      (require! (= source-sha256 (sha256 text)) "Current library IO source file digest drift")
      (require! (= text (read-text path)) "Current library IO source is not the live file")
      (require! (= (:sha256 source-span) (sha256 raw)) "Current library IO source span digest drift")
      (require! (= (:start-line source-span) (utf8-line-number text (:start-byte source-span))) "Current library IO span start line drift")
      (require! (= (:end-line source-span) (utf8-line-number text (dec (:end-byte source-span)))) "Current library IO span end line drift")
      (require! (= raw old-raw) "Current library IO source changes the prior predicate")
      (require! (= owner consumer-task-id (:owner prior)) "Current library IO source changes the predicate owner")))
  joins)

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
                   (or (#{math4-search-policy-kind math9-rewrite-termination-kind} (:kind bridge))
                       (canonical-policy-bridge-kinds (:kind bridge)))
                   (conj :baseline-tree :applying-tree :current-criteria)
                   (canonical-policy-bridge-kinds (:kind bridge))
                   (conj :human-decision :source-review)))
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

(defn git-diff-paths [root baseline applying require!]
  (let [process (.start (.redirectErrorStream
                         (ProcessBuilder. ^java.util.List
                                          ["git" "-C" (str root) "diff" "--name-only"
                                           baseline applying]) true))
        out (with-open [input (.getInputStream process)] (slurp input :encoding "UTF-8"))
        exit (.waitFor process)]
    (require! (zero? exit) "Source successor path census could not be read")
    (vec (sort (remove str/blank? (str/split-lines out))))))

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

(defn- validate-policy-criteria! [bridge read-ref sha256 require!]
  (when (canonical-policy-bridge-kinds (:kind bridge))
    (require! (exact-source-bridge? bridge)
              "Canonical policy source bridge identity or authority changed")
    (doseq [{:keys [path marker source-commit blob whole-file-sha256 start-byte end-byte span-sha256 owning-task recorded-status]} (:current-criteria bridge)]
      (require! (and (string? path) (not (.isAbsolute (io/file path)))
                     (re-matches #"[0-9a-f]{40}" (or source-commit "")))
                "Canonical policy criterion source identity is invalid")
      (let [text (read-ref source-commit path) bytes (.getBytes ^String text "UTF-8")]
        (require! (and (= whole-file-sha256 (sha256 text)) (= blob (git-blob-id text)))
                  "Canonical policy criterion file bytes differ")
        (require! (and (integer? start-byte) (integer? end-byte) (<= 0 start-byte) (< start-byte end-byte) (<= end-byte (alength bytes)))
                  "Canonical policy criterion span is invalid")
        (let [span (String. bytes (int start-byte) (int (- end-byte start-byte)) "UTF-8")
              prefix (String. bytes 0 (int start-byte) "UTF-8")
              task (last (re-seq #"(?m)^- \[([ xX])\] `([^`]+)`" prefix))]
          (require! (and (= span-sha256 (sha256 span))
                         (str/starts-with? span (str "<!-- " marker ":start -->"))
                         (str/ends-with? span (str "<!-- " marker ":end -->"))
                         (= owning-task (nth task 2 nil))
                         (= recorded-status (when task (if (= " " (nth task 1)) :open :checked))))
                    "Canonical policy criterion body, marker, task authority or status differs")))))
  bridge)

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
  (validate-policy-criteria! bridge read-ref sha256 require!)
  (assoc bridge :snapshot-provenance :git-bytes-verified))
