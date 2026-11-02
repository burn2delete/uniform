(ns schema-phase10-current
  "Read-only current-source bridge for the PR565 phase-10 schema proposal.

   The proposal is historical input: this checker authenticates its exact
   source facets against the current requirement manifest, but never applies
   its old task-field operations to a changed task predecessor."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]))

(def historical-head
  "cb7671e95976add7b47ee4dcd84caeee18423eff")
(def historical-task-source
  "86747916c4c876d707a1548113409fb939d854c9")
(def current-source
  "2d0844f8f7f21321d920b9245341a2b98adc642e")
(def proposal-path
  "specs/requirement-reconciliation/schema-family/decision-allocation.edn")
(def manifest-path "contracts/requirement-reconciliation.edn")
(def task-path "specs/domains-workflow/tasks.md")
(def plan-path "specs/domains-workflow/plan.md")
(def historical-task-sha
  "5abf39bc1af6040f84b4e4db77ee93ee000dc14112e8f636970833d0f945e382")
(def historical-task-tree
  "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b")
(def current-manifest-sha
  "8a41f5aa638ec717b1321a02974e63c6afc8103f498ee5d7bfc5738ebb3ad21e")
(def candidate-source
  "5a93ad9804cc7344cc57f3ecb9293ed3dae58595")
(def candidate-source-git-tree
  "3c28db82c4b751252c2fc8f37e754d5af01cc001")
(def candidate-manifest-sha
  "f104336a99da819d7a24f89ddcd7e48311b4def36fc45bc28d56444558828316")
(def current-task-sha
  "ffbab8d449a74517497b0365b53ae41f767a55ac64134fb65b4f0256dcdf3341")
(def current-plan-sha
  "7ca9c4aa25a3961180728c1c75a672a68bf0bd656a3ed258d53a1d56e842e368")
(def current-source-tree "5b62819e511bfc7e6b5eba0527d8c9b5883c5572")
(def current-source-git-tree
  "c5f5941ca3db4ce38695f870276e1b345a323339")
(def current-source-baseline "a1b991be499d50579a8b4366976c519ae8949224")
(def previous-current-source
  "585298b5faa75a307c5b06be19a0aebb8b09168c")
(def previous-current-manifest-sha
  "d24ed46e2de0c1672a1a37c0eeacb3340452dcf56ce1b511ecc678761909155b")
(def expected-current-task-bridge-count 18)
(def expected-new-task-bridge-paths
  #{"specs/runtime-workflow/tasks.md"
    "specs/build-workflow/tasks.md"
    "specs/domains-workflow/tasks.md"})
(def expected-candidate-bridge-paths
  #{"specs/types-workflow/tasks.md"
    "specs/runtime-workflow/tasks.md"
    "specs/domains-workflow/tasks.md"})
(def expected-candidate-bridges
  {"specs/types-workflow/tasks.md"
   {:current-sha256 "be4ab3523fcc9532a8f3dc7e36309843b5727cdb9bd5fea2ff8ba08faf07f946"}
   "specs/runtime-workflow/tasks.md"
   {:current-sha256 "644170d21c9015a96da5d3569739f186698933d81abd0e565f372b369f25fde5"}
   "specs/domains-workflow/tasks.md"
   {:current-sha256 "ffbab8d449a74517497b0365b53ae41f767a55ac64134fb65b4f0256dcdf3341"}})
(def expected-facet-count 463)
(def expected-facet-digest
  "9daedb26fb3bbee1ce9994d0cbec96cb8803f9428632cdb1a2f61d208850dcf1")
(def expected-document-count 19)
(def expected-document-digest
  "0f3f9f70454ac6765204b23f3daacbe5fe388c694e715c29f2c784a611ec06ad")
(def expected-decision-ids
  ["interface/language/types/project-schema-static-type"
   "interface/safety/safe10/runtime-authority"
   "interface/schema/authorize-runtime-migration"
   "interface/schema/check-binary-reference-layout"
   "interface/schema/check-canonical-identity-input"
   "interface/schema/check-client-operation"
   "interface/schema/compatibility-diff"
   "interface/schema/decode-canonical-value"
   "interface/schema/decode-declared-format"
   "interface/schema/emit-binary-codec-binding-input"
   "interface/schema/emit-database-structure"
   "interface/schema/emit-explicit-binary-layout"
   "interface/schema/emit-graphql-resolver-adapter"
   "interface/schema/emit-graphql-sdl"
   "interface/schema/emit-http-boundary-validators"
   "interface/schema/emit-openapi-document"
   "interface/schema/emit-typed-api-client"
   "interface/schema/emit-typed-migration-plan"
   "interface/schema/emit-typed-row-adapter"
   "interface/schema/encode-canonical-value"
   "interface/schema/encode-declared-format"
   "interface/schema/expand-authoritative-schema"
   "interface/schema/generate-source-validator"
   "interface/schema/run-http-contract-tests"
   "interface/schema/validate-migration-change"])
(def expected-criterion-ids
  ["schema/graphql-generation/full-source-interface"
   "schema/database-mapping/full-source-interface"
   "schema/configuration-reload/full-source-interface"
   "schema/artifact-validation/full-source-interface"
   "schema/release-evidence-gating/full-source-interface"])

(def source-join-specs
  [{:join :source-schema-projection
    :path "docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md"
    :sha256 "2f5afbb0805d20b4fd3542107a09019b25099bb3b8d855bbfb27b4abe750a2bc"
    :markers ["same source, lockfile, compiler identity, and target matrix produce same artifact IDs"
              "source schema can generate at least validator, JSON Schema, and typed artifact schema"]}
   {:join :source-schema-projection
    :path "docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md"
    :sha256 "43efc2c36c338899d42d7a6a4e4c07fc62faf7ffe5335a4be9c91161e1b37138"
    :markers [":artifact :gravity/artifact-schema"
              ":compiler-version :source-graph :dependency-lock :passes"]}
   {:join :initial-configuration-loading
    :path "docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md"
    :sha256 "b9067190de850a10200277d2ce8d9bd8b3ceabb45acbb244ca8125af175cd4ac"
    :markers ["Config declarations must define schema, sources, precedence, defaults,"
              "Config values decoded from external sources are tainted until validated."
              "Runtime config reload must define compatibility, lifecycle, and observability"]}
   {:join :hosted-runtime-source-map
    :path "docs/phase-03-profile-system/049-p4-hosted-profile-specification.md"
    :sha256 "398fa6ef060559ad16006444279b6e138b1f164be855c3d8094d6ec7910217f7"
    :markers ["`P4-SOURCEMAP` for missing diagnostic mapping where required."
              "Diagnostics must include host runtime, source span, generated-origin chain,"
              "host symbol"]}
   {:join :hosted-runtime-source-map
    :path "docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md"
    :sha256 "ea9d710d1015178f4a29f04255290162f5ddd79aa3a8f5e88cd78992c9e31127"
    :markers ["Source maps must connect browser diagnostics to Gravity source and generated"
              "`DOM6-SOURCEMAP` for browser diagnostics without source mapping."]}
   {:join :types-schema-static-projection
    :path "specs/types-workflow/tasks.md"
    :sha256 "13149ad814bebd312dd5c371ff88ff06255848e86d23b02e2d94922c1240f10f"
    :markers ["`types/current-evidence-and-remaining-work/types2-check-declared-abstractions`"
              "Validate generics, protocols/dispatch, records and schema-derived types"]}
   {:join :api-client-openapi
    :path "docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md"
    :sha256 "02e231ed2f359c46ed179cb0524bf35c12dfe5176adf055a4c0bd439d1d5c188"
    :markers ["OpenAPI generation manifest."
              "`S5-CLIENT` for generated clients without operation/schema hash validation."
              "`S5-SOURCEMAP` for OpenAPI operations without source provenance."]}
   {:join :api-client-openapi
    :path "specs/compiler-api-workflow/tasks.md"
    :sha256 "c76120c98f2e4bffd12aacd8aa8839545672408f2e95cf9eab709d0a8b87ed26"
    :markers ["retain the exact source/compiler/profile/target and applicable provider/checker identities"
              "actual positive products and negative diagnostics"]}
   {:join :hosted-runtime-source-map
    :path "specs/runtime-workflow/tasks.md"
    :sha256 "edbafd3d58f645d74e208eb428b7feb89f6dfd957899ce8c9136287026cbb9c4"
    :markers ["Closing evidence: Denial at actual service hook, redacted diagnostics and source-map preservation"]}])

(def current-join-task-specs
  [{:join :source-schema-projection
    :markers ["Current source-join obligations remain inside the five Open criteria below"
              "`source-schema-projection`"
              "Generate a JSON Schema"
              "typed `:gravity/artifact-schema`"
              "exact source/compiler/profile/target/provider/checker identities"]}
   {:join :initial-configuration-loading
    :markers ["`initial-configuration-loading`"
              "distinct acceptance case before reload"
              "validated provider input"
              "S8-INITIAL"
              "S8-RELOAD"]}
   {:join :hosted-runtime-source-map
    :markers ["`hosted-runtime-source-map`"
              "actual hosted failure fixture"
              "runtime version"
              "host symbol"
              "lowered artifact"
              "P4-SOURCEMAP"
              "conditional-unavailable"]}
   {:join :types-schema-static-projection
    :markers ["`types-schema-static-projection`"
              "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
              "generic Types ledger"
              "unrepresentable families"]}
   {:join :api-client-openapi
    :markers ["`api-client-openapi`"
              "OpenAPI route/schema/error/auth document"
              "typed API client"
              "S5-CLIENT/S5-SOURCEMAP"
              "operation/schema hashes"]}])

(def current-join-plan-specs
  [{:join :source-schema-projection
    :markers ["| source-schema-projection |" "JSON Schema" ":gravity/artifact-schema"]}
   {:join :initial-configuration-loading
    :markers ["| initial-configuration-loading |" "S8-INITIAL"]}
   {:join :hosted-runtime-source-map
    :markers ["| hosted-runtime-source-map |" "lowered artifact" "conditional-unavailable"]}
   {:join :types-schema-static-projection
    :markers ["| types-schema-static-projection |" "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"]}
   {:join :api-client-openapi
    :markers ["| api-client-openapi |" "OpenAPI document" "diagnostic identities"]}])

(def current-owner-task-specs
  [{:owner :types2-schema-static-projection
    :path "specs/types-workflow/tasks.md"
    :sha256 "be4ab3523fcc9532a8f3dc7e36309843b5727cdb9bd5fea2ff8ba08faf07f946"
    :markers ["Schema-static-projection boundary"
              "exact S1 expanded typed/versioned schema manifest"
              "accepted\nand rejected source-to-type parity fixtures"
              "Retain exact source/compiler/profile/target and"]}
   {:owner :runtime-hosted-source-map
    :path "specs/runtime-workflow/tasks.md"
    :sha256 "644170d21c9015a96da5d3569739f186698933d81abd0e565f372b369f25fde5"
    :markers ["Hosted source-map boundary"
              "actual hosted runtime failure plus emitted source/generated-origin"
              "runtime version, host symbol and lowered artifact"
              "conditional-unavailable"
              "P4-SOURCEMAP"]}])

(defn require! [condition message]
  (when-not condition
    (throw (ex-info message {:diagnostic "PR565-CURRENT-SOURCE"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1 (.digest
                           (java.security.MessageDigest/getInstance "SHA-256")
                           (.getBytes ^String text "UTF-8")))))

(defn git-text [ref path]
  (let [{:keys [exit out err]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable Git input " ref ":" path " " err))
    out))

(defn git-tree [ref]
  (let [{:keys [exit out err]} (shell/sh "git" "rev-parse" "--verify"
                                         (str ref "^{tree}"))]
    (require! (and (zero? exit) (re-matches #"[0-9a-f]{40}" (str/trim out)))
              (str "Missing immutable Git tree " ref " " err))
    (str/trim out)))

(defn historical-task! [ref expected-sha expected-tree]
  (let [text (git-text ref task-path)
        tree (git-tree ref)]
    (require! (= expected-tree tree)
              "Historical task Git tree changed")
    (require! (= expected-sha (sha256 text))
              "Historical task source bytes changed")
    {:text text :tree tree :sha256 (sha256 text)}))

(def historical-proposal
  (edn/read-string (git-text historical-head proposal-path)))
(def current-manifest-text (git-text current-source manifest-path))
(def current-manifest (edn/read-string current-manifest-text))
(def current-task-text (slurp task-path))
(def current-plan-text (slurp plan-path))

(def facet-keys
  [:id :document-id :source-occurrence-id :kind :start-byte :end-byte :raw-sha256])

(defn facet-row [facet]
  (select-keys facet facet-keys))

(defn text-markers! [text markers context]
  (doseq [marker markers]
    (require! (str/includes? text marker)
              (str context " is missing marker " marker))))

(defn source-join-entry! [spec text]
  (let [{:keys [join path markers] expected-sha :sha256} spec]
    (require! (= expected-sha (sha256 text))
              (str "Immutable source join bytes changed " join " " path))
    (text-markers! text markers (str "Immutable source join " join " " path))
    {:join join :path path :sha256 expected-sha :markers (count markers)}))

(defn source-joins! []
  (require! (= current-source-git-tree (git-tree current-source))
            "Current source Git tree identity changed")
  (mapv (fn [spec]
          (source-join-entry! spec (git-text current-source (:path spec))))
        source-join-specs))

(defn manifest-transition! []
  (let [previous-text (git-text previous-current-source manifest-path)
        previous (edn/read-string previous-text)
        current-bridges (:task-source-version-bridges current-manifest)
        previous-bridges (:task-source-version-bridges previous)
        current-paths (set (map :path current-bridges))]
    (require! (= previous-current-manifest-sha (sha256 previous-text))
              "Previous current manifest transition input changed")
    (require! (= (:source-tree previous) (:source-tree current-manifest))
              "Manifest transition changed the authenticated source tree")
    (require! (= (:source-baseline previous) (:source-baseline current-manifest))
              "Manifest transition changed the authenticated source baseline")
    (require! (= expected-current-task-bridge-count (count current-bridges))
              "Current task-source bridge population changed")
    (require! (set/subset? expected-new-task-bridge-paths current-paths)
              "Known current task-source bridge transition is incomplete")
    {:from previous-current-source
     :to current-source
     :manifest-sha256 [previous-current-manifest-sha current-manifest-sha]
     :task-source-bridges [(count previous-bridges) (count current-bridges)]
     :new-task-source-paths (vec (sort expected-new-task-bridge-paths))
     :source-tree-unchanged true
     :source-baseline-unchanged true}))

(defn candidate-manifest-transition!
  ([] (let [text (slurp manifest-path)]
        (candidate-manifest-transition! (edn/read-string text) text)))
  ([candidate] (candidate-manifest-transition! candidate nil))
  ([candidate candidate-text]
   (let [main current-manifest
         main-bridges (into {} (map (juxt :path identity)
                                    (:task-source-version-bridges main)))
         candidate-bridges (into {} (map (juxt :path identity)
                                         (:task-source-version-bridges candidate)))
         changed (get-in candidate [:task-source-version-bridges])
         changed-paths (set (keys expected-candidate-bridges))]
     (require! (or (nil? candidate-text)
                   (= candidate-manifest-sha (sha256 candidate-text)))
               "Candidate manifest bytes are not the authenticated transition input")
     (require! (= (:source-tree main) (:source-tree candidate))
               "Candidate manifest changed the authenticated source tree")
     (require! (= (:source-baseline main) (:source-baseline candidate))
               "Candidate manifest changed the authenticated source baseline")
     (require! (= (count (:task-source-version-bridges main))
                  (count changed))
               "Candidate manifest changed task-source bridge population")
     (require! (= (set (keys candidate-bridges)) (set (keys main-bridges)))
               "Candidate manifest changed task-source bridge paths")
     (doseq [path (sort changed-paths)
             :let [before (get main-bridges path)
                   after (get candidate-bridges path)
                   expected (get expected-candidate-bridges path)]]
       (require! (= (select-keys before [:path :marker :historical-ref
                                         :historical-sha256 :changed-records])
                     (select-keys after [:path :marker :historical-ref
                                         :historical-sha256 :changed-records]))
                    (str "Candidate bridge changed historical identity " path))
       (require! (and (= candidate-source (:current-ref after))
                      (= candidate-source-git-tree (:current-tree after))
                      (= (:current-sha256 expected) (:current-sha256 after))
                      (= :current-candidate-composition (:change-class after))
                      (= current-source (:current-ref (get after :known-current-main)))
                      (= "c5f5941ca3db4ce38695f870276e1b345a323339"
                         (:current-tree (get after :known-current-main))))
                 (str "Candidate bridge identity is not authenticated " path)))
     (doseq [path (sort (set/difference (set (keys main-bridges)) changed-paths))]
       (require! (= (get main-bridges path) (get candidate-bridges path))
                 (str "Unscoped candidate bridge changed " path)))
     {:from current-source
      :to candidate-source
      :main-manifest-sha256 current-manifest-sha
      :candidate-manifest-sha256 candidate-manifest-sha
      :candidate-source-git-tree candidate-source-git-tree
      :changed-task-source-paths (vec (sort changed-paths))
      :task-source-bridges (count changed)
      :source-tree-unchanged true
      :source-baseline-unchanged true})))

(defn task-joins! [text]
  (doseq [{:keys [join markers]} current-join-task-specs]
    (text-markers! text markers (str "Current task source join " join)))
  {:joins (count current-join-task-specs)
   :markers (reduce + (map #(count (:markers %)) current-join-task-specs))})

(defn plan-joins! [text]
  (require! (= current-plan-sha (sha256 text))
            "Current plan bytes changed without a new reconciled predecessor")
  (doseq [{:keys [join markers]} current-join-plan-specs]
    (text-markers! text markers (str "Current plan source join " join)))
  {:joins (count current-join-plan-specs)
   :markers (reduce + (map #(count (:markers %)) current-join-plan-specs))})

(defn owner-task-entry! [spec text]
  (let [{:keys [owner path markers] expected-sha :sha256} spec]
    (require! (= expected-sha (sha256 text))
              (str "Current owner task bytes changed " owner " " path))
    (text-markers! text markers (str "Current owner task " owner))
    {:owner owner :path path :sha256 expected-sha :markers (count markers)}))

(defn owner-tasks! []
  (mapv (fn [spec]
          (owner-task-entry! spec (slurp (:path spec))))
        current-owner-task-specs))

(defn source-pair!
  ([] (source-pair! historical-proposal))
  ([proposal]
  (require! (= current-manifest-sha (sha256 current-manifest-text))
            "Current manifest bytes are not the authenticated applying source")
  (require! (= current-source-tree (:source-tree current-manifest))
            "Current source tree identity changed")
  (require! (= current-source-baseline (:source-baseline current-manifest))
            "Current source baseline identity changed")
  (let [{historical-task-text :text} (historical-task! historical-task-source
                                                        historical-task-sha
                                                        historical-task-tree)
        witnesses (:source-facet-witnesses proposal)
        historical-facets (mapv #(facet-row (:metadata %)) witnesses)
        current-by-id (into {} (map (juxt :id identity) (:source-facets current-manifest)))
        historical-by-id (into {} (map (juxt :id identity) historical-facets))
        historical-ids (set (keys historical-by-id))
        current-docs (into {} (map (juxt :id identity) (:original-documents current-manifest)))
        document-pairs
        (->> witnesses
             (map (fn [w] [(get-in w [:metadata :document-id])
                           (:document-sha256 w)]))
             set
             sort
             vec)]
    (require! (= expected-facet-count (count historical-facets)
                       (count historical-ids))
              "Source facet population is lost or duplicated")
    (require! (set/subset? historical-ids (set (keys current-by-id)))
              "A historical source facet is absent from the current manifest")
    (doseq [[id historical] historical-by-id
            :let [current (get current-by-id id)]]
      (require! (= historical (facet-row current))
                (str "Historical/current source facet mismatch " id)))
    (doseq [witness witnesses
            :let [document-id (get-in witness [:metadata :document-id])
                  current (get current-docs document-id)]]
      (require! current (str "Missing current source document " document-id))
      (require! (= (:path witness) (:path current))
                (str "Historical witness path mismatch " document-id))
      (require! (= (:document-sha256 witness) (:sha256 current))
                (str "Historical witness document hash mismatch " document-id)))
    (doseq [[document-id document-sha] document-pairs
            :let [current (get current-docs document-id)]]
      (require! current (str "Missing current source document " document-id))
      (require! (= document-sha (:sha256 current))
                (str "Historical/current document hash mismatch " document-id)))
    (let [facet-digest (sha256 (pr-str (vec (sort-by :id historical-facets))))
          document-digest (sha256 (pr-str document-pairs))]
      (require! (= expected-facet-digest facet-digest)
                "Attached facet digest changed")
      (require! (= expected-document-count (count document-pairs))
                "Attached document population changed")
      (require! (= expected-document-digest document-digest)
                "Attached document digest changed")
      {:facets (count historical-facets)
       :documents (count document-pairs)
       :facet-digest facet-digest
       :document-digest document-digest
       :historical-task-tree historical-task-tree
       :historical-task-sha256 (sha256 historical-task-text)}))))

(defn proposal! [proposal]
  (let [operations (:decision-field-proposals proposal)
        ids (mapv :id operations)]
    (require! (= 25 (count operations) (count (set ids)))
              "Decision allocation count or uniqueness changed")
    (require! (= expected-decision-ids ids)
              "Decision allocation IDs/order changed")
    (require! (= 25 (get-in proposal [:counts :assigned-decisions]))
              "Decision allocation count is not 25")
    (require! (= 833 (get-in proposal [:counts :implementation-decisions-open]))
              "Historical Open decision population changed")
    (doseq [operation operations
            :let [before (:before-record operation)
                  after (:after operation)]]
      (require! (= :open (:status before))
                (str "Historical decision is not Open " (:id operation)))
      (require! (not (contains? before :criterion-task-allocation))
                (str "Historical decision already allocated " (:id operation)))
      (require! (and (= :unassessed (:implementation-acceptance after))
                     (= :pending (:independent-review after))
                     (= :pending (:canonical-adoption after)))
                (str "Decision allocation falsely claims acceptance " (:id operation)))
      (require! (seq (:source-facet-ids after))
                (str "Decision allocation lost source facets " (:id operation)))
      (require! (seq (:criterion-witnesses after))
                (str "Decision allocation lost criterion witness " (:id operation))))
    {:decisions (count operations)
     :ids (count (set ids))
     :open-before 833
     :implementation-acceptance :unassessed
     :canonical-adoption :pending}))

(defn task! [text]
  (require! (= current-task-sha (sha256 text))
            "Current task bytes changed without a new reconciled predecessor")
  (require! (= 1 (count (re-seq #"(?m)^- \[ \] `schema` Schema$" text)))
            "Schema task is missing, renamed or duplicated")
  (require! (= 7 (count (re-seq #"(?m)^- \[ \] `[^`]+` " text)))
            "Domain task population changed")
  (require! (= 1 (count (re-seq #"(?m)^- \[ \] `schema` Schema\n\nStatus: Open\n" text)))
            "Schema task status is not Open")
  (require! (str/includes? text "Source-specific acceptance for S1-S9 remains open.")
            "S1-S9 source acceptance is missing")
  (require! (str/includes? text "arbitrary input/work quotas confer no acceptance authority")
            "Accounting-only quota language was not retired")
  (let [ids (mapv second (re-seq #"(?m)^Criterion `([^`]+)` \(Open\)\." text))]
    (require! (= expected-criterion-ids ids)
              "Schema criterion IDs/order/status changed")
    {:task-sha256 (sha256 text)
     :schema-criteria (count ids)
     :task-count 7
     :source-joins (task-joins! text)}))

(defn plan! [text]
  {:plan-sha256 (sha256 text)
   :source-joins (plan-joins! text)})

(defn catches? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(defn negative-controls! [proposal source-summary source-join-summary]
  (let [bad [(update proposal :decision-field-proposals pop)
             (update proposal :decision-field-proposals conj
                     (first (:decision-field-proposals proposal)))
             (assoc-in proposal [:decision-field-proposals 0 :id] "forged-id")
             (assoc-in proposal [:decision-field-proposals 0 :before-record :status] :accepted)
             (assoc-in proposal [:decision-field-proposals 0 :after :implementation-acceptance]
                       :accepted)
             (update proposal :decision-field-proposals
                     (fn [operations]
                       (assoc (vec operations)
                              0 (nth operations 1)
                              1 (nth operations 0))))]]
    (require! (every? (fn [x] (catches? #(proposal! x))) bad)
              "Decision/facet tamper control accepted")
    (doseq [mutated [(assoc-in proposal [:source-facet-witnesses 0 :metadata :raw-sha256]
                                  "forged")
                     (assoc-in proposal [:source-facet-witnesses 0 :metadata :id]
                                  "forged-facet")]]
      (require! (catches?
                 (fn []
                   (let [rows (mapv (fn [w] (facet-row (:metadata w)))
                                    (:source-facet-witnesses mutated))]
                     (require! (= expected-facet-digest
                                  (sha256 (pr-str (vec (sort-by :id rows)))))
                               "Facet digest changed"))))
                "Facet tamper control accepted"))
    (require! (catches? #(task! (str/replace current-task-text
                                              "schema/graphql-generation/full-source-interface"
                                              "schema/forged-interface")))
              "Task criterion-ID tamper control accepted")
    (require! (catches?
               #(source-pair!
                 (assoc-in proposal [:source-facet-witnesses 0 :path]
                           "docs/forged/unrelated-source.md")))
              "Witness document-path tamper control accepted")
    (require! (every?
               (fn [spec]
                 (let [text (git-text current-source (:path spec))
                       marker (first (:markers spec))]
                   (catches?
                    #(source-join-entry!
                      spec
                      (str/replace-first text marker "forged-source-marker")))))
               source-join-specs)
              "Immutable source-join tamper control accepted")
    (require! (every?
               (fn [{:keys [markers]}]
                 (catches?
                  #(task-joins!
                    (str/replace-first current-task-text
                                       (first markers)
                                       "forged-current-task-marker"))))
               current-join-task-specs)
              "Current task source-join tamper control accepted")
    (require! (every?
               (fn [{:keys [markers]}]
                 (catches?
                  #(plan-joins!
                    (str/replace-first current-plan-text
                                       (first markers)
                                       "forged-current-plan-marker"))))
               current-join-plan-specs)
              "Current plan source-join tamper control accepted")
    (require! (every?
               (fn [spec]
                 (let [text (slurp (:path spec))
                       marker (first (:markers spec))]
                   (catches?
                    #(owner-task-entry!
                      spec
                      (str/replace-first text marker "forged-owner-task-marker")))))
               current-owner-task-specs)
              "Current owner-task source-join tamper control accepted")
    (require! (catches?
               #(candidate-manifest-transition!
                 (update current-manifest :task-source-version-bridges
                         (fn [bridges]
                           (mapv (fn [bridge]
                                   (if (= "specs/types-workflow/tasks.md" (:path bridge))
                                     (assoc bridge :current-tree
                                            (apply str (repeat 40 "0")))
                                     bridge))
                                 bridges)))))
              "Candidate manifest bridge tamper control accepted")
    (doseq [[ref expected-sha expected-tree message]
            [[(apply str (repeat 40 "0")) historical-task-sha historical-task-tree
              "Historical task ref tamper control accepted"]
             [historical-task-source (apply str (repeat 64 "0")) historical-task-tree
              "Historical task hash tamper control accepted"]
             [historical-task-source historical-task-sha (apply str (repeat 40 "0"))
              "Historical task tree tamper control accepted"]]]
      (require! (catches? #(historical-task! ref expected-sha expected-tree))
                message))
    (require! (= 463 (:facets source-summary))
              "Source summary changed during negative controls")
    (require! (= (count source-join-specs) (count source-join-summary))
              "Source-join summary changed during negative controls")
    (+ (count bad) 3 1 3
       (count source-join-specs)
       (count current-join-task-specs)
       (count current-join-plan-specs)
       (count current-owner-task-specs)
       1)))

(when (= ["--check"] (vec *command-line-args*))
  (let [source-summary (source-pair!)
        source-join-summary (source-joins!)
        manifest-transition-summary (manifest-transition!)
        candidate-manifest-transition-summary (candidate-manifest-transition!)
        proposal-summary (proposal! historical-proposal)
        task-summary (task! current-task-text)
        plan-summary (plan! current-plan-text)
        owner-task-summary (owner-tasks!)
        negative-controls (negative-controls! historical-proposal
                                               source-summary
                                               source-join-summary)]
    (prn {:kind :pr565-current-source-pair
          :historical-head historical-head
          :historical-task-source historical-task-source
          :current-source current-source
          :current-source-git-tree current-source-git-tree
          :current-manifest-sha256 current-manifest-sha
          :historical-task-sha256 historical-task-sha
          :current-task-sha256 current-task-sha
          :source-pair source-summary
          :source-joins source-join-summary
          :manifest-transition manifest-transition-summary
          :candidate-manifest-transition candidate-manifest-transition-summary
          :proposal proposal-summary
          :task task-summary
          :plan plan-summary
          :owner-tasks owner-task-summary
          :task-transition :historical-field-operations-held
          :negative-controls negative-controls
          :implementation-acceptance :unassessed
          :canonical-adoption :pending
          :no-denominator-renewal true
          :no-accounting-admission true})
    (shutdown-agents)))
