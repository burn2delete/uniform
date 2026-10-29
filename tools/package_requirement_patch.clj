(ns package-requirement-patch
  "Prepare and check only the original PKG1-PKG12 reconciliation proposal."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.pprint :as pp] [clojure.string :as str]))

(def manifest-ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(def input-ref "53ae6a403b20d15155bb5b04911fe7ac0092b921")
(def artifact-path "specs/requirement-reconciliation/package-family.edn")
(def manifest-hash "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690")
(defn require! [ok message] (when-not ok (throw (ex-info message {}))))
(defn sha [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String s "UTF-8")))))
(defn git-text [ref path]
  (let [r (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? (:exit r)) (str "Missing immutable input: " ref ":" path)) (:out r)))
(defn immutable-inputs []
  (let [raw (git-text manifest-ref "contracts/requirement-reconciliation.edn")
        input (git-text input-ref "specs/requirement-reconciliation/source-task-inputs.edn")]
    (require! (= manifest-hash (sha raw)) "Wrong PR445 manifest")
    (require! (= "423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94" (sha input))
              "Wrong source/task snapshot")
    [(edn/read-string raw) (edn/read-string input)]))

(def sha256 sha)
(defn occurrence-api []
  ;; Evaluate only the exact documented API defn, not the tool's CLI or loaders.
  (let [text (git-text "c0dc5790197a1bbd3a8aa285e5458c272d877220" "tools/requirement_reconciliation.clj")
        forms (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
                (loop [out []]
                  (let [form (read {:eof ::eof} r)]
                    (if (= ::eof form) out (recur (conj out form))))))
        api (first (filter #(and (seq? %) (= 'defn (first %)) (= 'source-occurrences (second %))) forms))]
    (require! api "Pinned occurrence API absent")
    (binding [*ns* (the-ns 'package-requirement-patch)] (eval api))
    (ns-resolve 'package-requirement-patch 'source-occurrences)))

(def unresolved
  {"behavior/pkg1/project-entrypoint-resolution"
   "Build normalization does not entail namespace/var resolution by artifact kind; an exact Resolver-to-Build diagnostic/entrypoint interface is missing."
   "behavior/pkg6/derived-capability-manifest"
   "The Build production task does not specify the effect-analysis-to-capability derivation input or disagreement rejection; retain the Effects/Auth producer interface gap."
   "behavior/pkg6/request-grant-separation"
   "Build declarations do not implement deployment grant issuance or scoped runtime handles; the package-to-deployment-to-runtime consumer interface remains missing."
   "behavior/pkg6/runtime-permission-audit"
   "Runtime enforcement/observability has a stable task but lacks the package requested/granted/denied/used audit schema and participating-runtime acceptance interface."
   "behavior/pkg6/transitive-deny-precedence"
   "Resolver capability rejection does not by itself close every missing edge through deployment and runtime; the denial/provenance consumer interface remains missing."})

;; Explicit source-specific routing decisions, not phase-to-component defaults.
(def routes
  {"implement-projectlock-normalization-and-constraint-resolution"
   ["pkg1/project-authority-request-check" "pkg1/project-dependency-source-check"
    "pkg1/project-normalization" "pkg1/project-profile-target-check" "pkg1/project-unsafe-policy"
    "pkg1/release-lock-admission" "pkg4/lock-trust-identity" "pkg4/package-resolution"
    "pkg5/dependency-authority-admission" "pkg5/deterministic-dependency-solver"
    "pkg5/minimal-conflict-explanation" "pkg5/offline-locked-resolution"
    "pkg5/private-latent-resolution" "pkg5/target-variant-selection"]
   "execute-typed-graph-under-declared-build-effects"
   ["pkg2/declared-build-effects" "pkg2/generated-build-inputs" "pkg2/graph-failure-location"
    "pkg2/typed-build-graph"]
   "implement-dependency-aware-caching-and-invalidation" ["pkg2/dependency-aware-cache"]
   "close-reproducibility-and-target-matrix"
   ["pkg7/canonical-rebuild-comparison" "pkg7/deterministic-build-environment"
    "pkg7/nonreproducible-claim-boundary" "pkg7/recorded-build-recipe"
    "pkg11/explicit-target-fallback" "pkg11/host-independent-cross-compile"
    "pkg11/per-target-evidence" "pkg11/target-compatibility-admission" "pkg11/target-matrix-artifact"]
   "produce-and-verify-packageprovenancesigning-records"
   ["pkg2/build-release-evidence" "pkg3/artifact-manifest" "pkg3/consumer-artifact-admission"
    "pkg4/download-verification" "pkg4/offline-install" "pkg4/package-operation-artifacts"
    "pkg4/package-update-diff" "pkg4/publication-verification" "pkg4/registry-token-redaction"
    "pkg4/yank-history" "pkg6/capability-update-diff" "pkg8/package-safety-metadata"
    "pkg8/revoked-safety-invalidation" "pkg8/safety-update-diff" "pkg8/unreviewed-unsafe-admission"
    "pkg9/generated-candidate-provenance" "pkg9/latent-package-state" "pkg9/mirror-authenticity"
    "pkg9/registry-access-privacy" "pkg9/registry-history-retention" "pkg9/registry-index-verification"
    "pkg9/registry-publication-gate" "pkg10/keyless-provenance-verification"
    "pkg10/model-generated-source-taint" "pkg10/release-provenance-artifact"
    "pkg10/revoked-provenance-admission" "pkg10/trusted-builder-admission"
    "pkg10/unknown-provenance-schema-denial" "pkg12/canonical-signing"
    "pkg12/evidence-backed-attestation-level" "pkg12/keyless-trust-verification"
    "pkg12/provenance-subject-binding" "pkg12/revoked-signing-admission"
    "pkg12/transitive-sbom" "pkg12/verification-report" "pkg12/verify-before-consumption"]})
(def yank-id "behavior/pkg5/yank-revocation-admission")
(def task-path "specs/build-workflow/tasks.md")
(def marker "build-implementation-tasks")
(defn task-reference [suffix]
  {:path task-path :sha256 (sha (slurp task-path)) :marker marker
   :id (str "build/remaining-tasks-and-evidence/" suffix)})
(defn edit [r field after]
  {:collection :requirements :id (:id r) :field-path [field]
   :before-present? (contains? r field) :before (get r field) :after after})
(defn apply-edits [manifest edits]
  (reduce
   (fn [m {:keys [collection id field-path before-present? before after]}]
     (let [rows (get m collection) hits (keep-indexed #(when (= id (:id %2)) %1) rows)]
       (require! (= 1 (count hits)) "Patch ID absent or duplicated")
       (let [path (into [collection (first hits)] field-path)
             parent (get-in m (butlast path))]
         (require! (= before-present? (contains? parent (last path))) "Stale field presence")
         (require! (= before (get-in m path)) "Stale field value or duplicate application")
         (require! (not= before after) "No-op patch")
         (assoc-in m path after)))) manifest edits))

(defn prepare [manifest inputs]
  (let [docs (filterv #(= "phase-12" (:source-family %)) (:original-documents inputs))
        doc-ids (set (map :id docs))
        facets (filterv #(doc-ids (:document-id %)) (:source-facets manifest))
        ids (set (mapcat :unit-ids facets))
        records (filterv #(ids (:id %)) (:requirements manifest))
        routing (into {} (for [[suffix rs] routes r rs] [(str "behavior/" r) (task-reference suffix)]))
        prior (edn/read-string (slurp "specs/build-workflow/yank-interface.edn"))
        references (assoc routing yank-id (get-in prior [:after :task-reference]))
        edits (vec (mapcat
                    (fn [r] (when-let [reference (routing (:id r))]
                              [(edit r :task-reference reference)
                               (edit r :task-gap
                                     {:state :interface-proposed-implementation-unassessed
                                      :prior-gap (:task-gap r)
                                      :qualification "Proposed exact task binding plus Package source acceptance interfaces in specs/build-workflow/plan.md; independent semantic review and implementation evidence remain pending."})])) records))]
    (require! (= ids (into (set (keys references)) (keys unresolved))) "Unrouted or invented semantic ID")
    {:kind :gravity/noncanonical-requirement-family-proposal :version 1
     :scope-key "requirement-reconciliation/phase-12-build-package-and-artifact-system"
     :authority :proposal-only :source-baseline (:source-commit inputs) :source-tree (:source-tree inputs)
     :source-manifest {:ref manifest-ref :path "contracts/requirement-reconciliation.edn" :sha256 manifest-hash}
     :proposal-format {:ref manifest-ref :path ".specify/requirement-proposal-format.edn"
                       :sha256 "69ec49fb954ba66ce86621d5dfac3a4cb0b514b84915277b86944ae2ec857adc"}
     :proposed-plan-addendum {:path "specs/requirement-reconciliation/package-interfaces.md"
                              :sha256 (sha (slurp "specs/requirement-reconciliation/package-interfaces.md"))
                              :destination "specs/build-workflow/plan.md"
                              :destination-before-sha256 (sha (slurp "specs/build-workflow/plan.md"))
                              :state :central-adoption-pending}
     :source-documents docs :requirements-before records :source-facets facets
     :source-dispositions (filterv #(some (fn [id] (str/starts-with? (:occurrence-id %) (str id "/"))) doc-ids)
                                   (:source-dispositions manifest))
     :units (mapv (fn [r]
                    {:proposed-id (:id r) :obligation (:obligation r)
                     :acceptance-facets (filterv #(some #{(:id r)} (:unit-ids %)) facets)
                     :task-references (if-let [ref (references (:id r))] [ref] [])
                     :task-gaps (if-let [reason (unresolved (:id r))] [reason] [])
                     :historical-support (:historical-support r)
                     :atomicity (:atomicity r) :nonoverlap (:nonoverlap r) :current-acceptance :unassessed}) records)
     :consumed-interface {:id yank-id :receipt-path "specs/build-workflow/yank-interface.edn"
                          :sha256 (sha (slurp "specs/build-workflow/yank-interface.edn"))
                          :application :already-integrated-task-interface-not-replayed
                          :qualification "Receipt applies to distinct preserved uncommitted manifest. Central owner composes its two fields against that exact before value; this patch does not duplicate it."}
     :equivalence-proposals
     [{:id "behavior/pkg4/package-resolution" :target "behavior/pkg5/deterministic-dependency-solver"
       :kind :refinement :rationale "Operation entry/structured result versus solver choice; central owner decides overlap, preserving operation context."}
      {:id "behavior/pkg6/capability-update-diff" :target "behavior/pkg4/package-update-diff"
       :kind :alias :rationale "Package capability expansion diff repeats the update diff; retain all PKG6 source and transitive context pending central adjudication."}
      {:id "behavior/pkg9/registry-history-retention" :target "behavior/pkg4/yank-history"
       :kind :refinement :rationale "Registry retention includes revocation and private/latent state; do not erase that context as a generic yank alias."}
      {:id "behavior/pkg4/download-verification" :target "behavior/pkg12/verify-before-consumption"
       :kind :refinement :rationale "Acquisition/install checkpoint and consumer-use checkpoint remain separately observable; no unconditional alias."}]
     :edits edits :unresolved-ids (vec (sort (keys unresolved)))
     :nonclaims [:implementation-acceptance :global-nonoverlap :aggregate-adoption :denominator :release :seed-retirement]}))

(defn validate! [proposal manifest inputs]
  (require! (= proposal (prepare manifest inputs)) "Family drift, lost facet/condition, or altered semantic decision")
  (let [api (occurrence-api)
        occurrences (mapcat (fn [{:keys [id path]}] (api id (slurp path))) (:source-documents proposal))
        by-id (into {} (map (juxt :id identity) occurrences))
        nonlayout (set (map :id (filter #(= :source-block (:structural-kind %)) occurrences)))]
    (require! (= nonlayout (set (map :occurrence-id (:source-dispositions proposal))))
              "Lost or invented source occurrence")
    (doseq [facet (:source-facets proposal)]
      (let [o (get by-id (:source-occurrence-id facet))]
        (require! (and o (<= (:start-byte o) (:start-byte facet) (:end-byte facet) (:end-byte o)))
                  "Facet targets wrong source occurrence"))))
  (doseq [{:keys [id path original-sha256 current-sha256]} (:source-documents proposal)]
    (require! (= current-sha256 (sha (slurp path))) (str "Current source drift: " id))
    (require! (= original-sha256 (sha (git-text (:original-source-commit inputs) path)))
              (str "Original source drift: " id)))
  (doseq [facet (:source-facets proposal)]
    (let [doc (first (filter #(= (:id %) (:document-id facet)) (:source-documents proposal)))
          bytes (.getBytes ^String (slurp (:path doc)) "UTF-8")
          raw (String. (java.util.Arrays/copyOfRange bytes (int (:start-byte facet)) (int (:end-byte facet))) "UTF-8")]
      (require! (= (:raw-sha256 facet) (sha raw)) "Wrong source facet hash/span")))
  (doseq [unit (:units proposal) reference (:task-references unit)]
    (let [{:keys [path sha256 marker id]} reference text (slurp path)
          start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
          a (.indexOf text start) b (.indexOf text end)]
      (require! (= sha256 (sha text)) "Wrong task hash")
      (require! (and (<= 0 a) (< a b) (str/includes? (subs text a b) (str "`" id "`")))
                "Wrong task marker or ID")))
  (let [applied (apply-edits manifest (:edits proposal))]
    (require! (= (mapv #(dissoc % :task-reference :task-gap) (:requirements manifest))
                 (mapv #(dissoc % :task-reference :task-gap) (:requirements applied))) "Semantic/status mutation")
    (require! (= (dissoc manifest :requirements) (dissoc applied :requirements)) "Outside-family mutation"))
  true)

(defn run [args]
  (let [[manifest inputs] (immutable-inputs)]
    (case (vec args)
      ["--prepare"] (let [p (prepare manifest inputs)] (validate! p manifest inputs)
                       (spit artifact-path (with-out-str (pp/pprint p)))
                       (println "Prepared proposal; no canonical adoption or implementation acceptance."))
      ["--check"] (let [p (edn/read-string (slurp artifact-path))] (validate! p manifest inputs)
                     (prn {:documents (count (:source-documents p)) :units (count (:units p))
                           :facets (count (:source-facets p)) :edits (count (:edits p))
                           :unresolved-ids (:unresolved-ids p) :sha256 (sha (slurp artifact-path))}))
      (throw (ex-info "Use --prepare or --check" {})))))
(when (seq *command-line-args*) (run *command-line-args*) (shutdown-agents))
