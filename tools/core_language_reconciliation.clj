(ns core-language-reconciliation
  "Bounded phase-01 proposal preparation; never writes the canonical manifest."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str] [clojure.pprint :as pp]))

(def source-ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(def original-ref "a1b991be499d50579a8b4366976c519ae8949224")
(def input-ref "53ae6a403b20d15155bb5b04911fe7ac0092b921")
(def base-ref "6276c94b0b753c888ef8fecc794bcc81245ea3b3")
(def output-path "specs/requirement-reconciliation/phase-01-core-language.edn")
(def runtime-id "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup")
(def provider-id "language/provider/initialize-runtime-contract")
(defn require! [p message] (when-not p (throw (ex-info message {}))))
(defn sha256 [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String s "UTF-8")))))
(defn git-source [ref path]
  (let [r (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? (:exit r)) (str "Missing immutable input " ref ":" path)) (:out r)))

;; Load only the published occurrence function, without executing its CLI or
;; loading any compiler, runtime, evidence tool or whole-manifest projector.
(declare source-occurrences)
(with-open [r (java.io.PushbackReader.
                (java.io.StringReader. (git-source "c0dc5790197a1bbd3a8aa285e5458c272d877220"
                                                         "tools/requirement_reconciliation.clj")))]
  (loop []
    (let [form (read {:eof ::eof} r)]
      (require! (not= ::eof form) "Occurrence API missing")
      (if (and (seq? form) (= 'defn (first form)) (= 'source-occurrences (second form)))
        (eval form) (recur)))))

(defn slice [s lo hi]
  (String. (java.util.Arrays/copyOfRange (.getBytes ^String s "UTF-8") (int lo) (int hi)) "UTF-8"))
(defn validate-task! [r text]
  (require! (= (:sha256 r) (sha256 text)) "Task source hash mismatch")
  (let [start (str "<!-- " (:marker r) ":start -->") end (str "<!-- " (:marker r) ":end -->")
        lo (str/index-of text start) hi (str/index-of text end)]
    (require! (and lo hi (< lo hi)
                   (str/includes? (subs text lo hi) (str "`" (:id r) "`"))) "Task marker/ID mismatch")))
(defn validate-facet! [f texts occurrences]
  (let [o (get occurrences (:source-occurrence-id f))]
    (require! (and o (= (:document-id f) (:document-id o))
                   (<= (:start-byte o) (:start-byte f) (:end-byte f) (:end-byte o))) "Invented occurrence or escaped facet")
    (require! (= (:raw-sha256 f) (sha256 (slice (texts (:document-id f)) (:start-byte f) (:end-byte f)))) "Wrong raw facet bytes")))
(defn field-edit [q k after]
  {:collection :requirements :id (:id q) :field-path [k]
   :before-present? (contains? q k) :before (get q k) :after after})
(defn apply-edits [m edits]
  (reduce (fn [out {:keys [collection id field-path before-present? before after]}]
            (require! (and (= :requirements collection)
                           (#{[:task-reference] [:additional-task-interfaces] [:task-gap]} field-path))
                      "Semantic/alias edits require separate central adjudication")
            (let [rows (get out collection) matches (keep-indexed #(when (= id (:id %2)) %1) rows)]
              (require! (= 1 (count matches)) "Missing/duplicate stable ID")
              (let [i (first matches) row (nth rows i) k (first field-path)]
                (require! (and (= before-present? (contains? row k)) (= before (get row k)))
                          "Stale/duplicate field application")
                (assoc-in out [collection i k] after)))) m edits))

(defn prepare [manifest-file]
  (let [raw (slurp manifest-file)
        _ (require! (= "aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980" (sha256 raw)) "Preserved PR445 manifest mismatch")
        m (edn/read-string raw)
        input-text (git-source input-ref "specs/requirement-reconciliation/source-task-inputs.edn")
        _ (require! (= "423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94" (sha256 input-text)) "Frozen input mismatch")
        inputs (edn/read-string input-text)
        ids (set (map #(str "L" %) (range 1 20)))
        docs (filterv #(ids (:id %)) (:original-documents inputs))
        texts (into {} (for [d docs] [(:id d) (git-source original-ref (:path d))]))
        occurrences (vec (mapcat #(source-occurrences (:id %) (texts (:id %))) docs))
        occurrence-index (into {} (map (juxt :id identity) occurrences))
        facets (filterv #(ids (:document-id %)) (:source-facets m))
        wanted (set (mapcat :unit-ids facets))
        qs (filterv #(wanted (:id %)) (:requirements m))
        facet-index (into {} (map (juxt :id identity) facets))
        task-inputs (into {} (map (fn [t] [[(:path t) (:id t)] t]) (:canonical-task-inputs inputs)))
        runtime-path "specs/runtime-workflow/tasks.md"
        runtime-before (git-source base-ref runtime-path)
        runtime-text (str/replace runtime-before
                      "Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement"
                      "Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement; before user code, validate selected provider existence, artifact versions, grant scopes, compiled contracts, profile/target compatibility and enabled safety/audit requirements; reject a missing required provider unless a declared fallback applies, with positive fallback and negative mismatch fixtures under [L15 runtime providers](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md)")
        current-ref (fn [r]
                      (require! (task-inputs [(:path r) (:id r)]) "Missing/renamed canonical task")
                      (let [t (if (= runtime-path (:path r)) runtime-text (slurp (:path r)))]
                        (validate-task! (assoc r :sha256 (sha256 t)) t)
                        (require! (= (get-in task-inputs [[(:path r) (:id r)] :file-sha256])
                                     (sha256 (slurp (:path r)))) "Frozen task source drift")
                        (if (= (:sha256 r) (sha256 t)) r
                          (cond-> (assoc (dissoc r :acceptance-boundary-sha256)
                                        :sha256 (sha256 t) :source-ref base-ref)
                            (= runtime-path (:path r)) (assoc :applying-state :draft-task-amendment)))))
        provider-ref {:path runtime-path :id runtime-id :marker "runtime-implementation-tasks"
                      :sha256 (sha256 runtime-text) :source-ref base-ref
                      :applying-state :draft-task-amendment}
        edits (vec (mapcat
                     (fn [q]
                       (let [r (:task-reference q)]
                         (if (= provider-id (:id q))
                           [(field-edit q :task-reference provider-ref)
                            (field-edit q :additional-task-interfaces [(current-ref r)])
                            (field-edit q :task-gap
                              {:state :partial-acceptance-interface
                               :reason "Managed/native startup now names all L15 checks and fallback rejection. Other runtime/OS/host/deployment/workflow/kernel/firmware/hardware variants and implementation receipts remain unassessed."})]
                           (when r
                             (let [next (current-ref r)]
                               (when (not= next r) [(field-edit q :task-reference next)])))))) qs))
        units (mapv (fn [q]
                      {:proposed-id (:id q) :obligation (:obligation q)
                       :acceptance-facets (mapv facet-index (filter facet-index (:source-facet-ids q)))
                       :outside-family-facet-ids (filterv #(not (facet-index %)) (:source-facet-ids q))
                       :task-references (if (= provider-id (:id q)) [provider-ref (current-ref (:task-reference q))]
                                            (if-let [r (:task-reference q)] [(current-ref r)] []))
                       :task-gaps (cond-> [] (:task-gap q) (conj (:task-gap q))
                                    (nil? (:task-reference q)) (conj "Missing canonical task interface; retained as unresolved."))
                       :task-entailment (if (= provider-id (:id q)) :reviewed-draft-managed-native-interface
                                           :inherited-binding-entailment-unassessed)
                       :equivalence-proposals [] :historical-support (:historical-support q)
                       :preserved-record q :atomicity (:atomicity q)
                       :nonoverlap (:nonoverlap q) :current-acceptance :unassessed}) qs)
        dispositions (filterv #(some (fn [id] (str/starts-with? (:occurrence-id %) (str id "/source/"))) ids)
                              (:source-dispositions m))]
    (require! (= 19 (count docs)) "Source family not complete")
    (doseq [d docs]
      (require! (= (:original-sha256 d) (sha256 (texts (:id d)))) "Original source hash mismatch")
      (require! (= (:current-sha256 d) (sha256 (slurp (:path d)))) "Current source drift"))
    (doseq [f facets]
      (validate-facet! f texts occurrence-index)
      (require! (every? wanted (:unit-ids f)) "Lost facet owner"))
    (require! (= (set (map :id (filter #(= :source-block (:structural-kind %)) occurrences)))
                 (set (map :occurrence-id dispositions))) "Lost or invented source disposition")
    {:kind :gravity/noncanonical-requirement-proposal :version 1
     :scope-key "requirement-reconciliation/phase-01-core-language"
     :authority :proposal-only :canonical-adoption :pending :independent-review :pending
     :source-manifest {:kind :preserved-uncommitted-pr445 :sha256 (sha256 raw)
                       :committed-predecessor {:ref source-ref :sha256 "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690"}}
     :source-task-inputs {:ref input-ref :sha256 (sha256 input-text)}
     :consumed-language-lineage
     [{:artifact "language-runtime-domain-schema-normalized-application.edn" :sha256 "df2cc7b2ea0bfc60c3eb0bf7e4189d5728659c6fbfe42538f8a0e35c4747a18a"}
      {:artifact "language295-foundation-context-applicability.edn" :sha256 "4987ada5626a182422edb68639e2d7aba282d3d1cf0d232ecf10d79635a953d4"}
      {:artifact "dom19-operation-admission-correction.edn" :sha256 "b2b4cc65d6a55ffa942bec5cb1f4e91be4cef388f0aaac6b37dad5baf42b38aa"}
      {:artifact "language-foundation-composition-review.edn" :sha256 "0c710b8f6ff506ebee1d3b32d14bf86f9070cd564718aa9b719b68c0bc4017b8"}
      {:artifact "lrds295-application-review.edn" :sha256 "63758cc362418bf0aa0534273822e754d306f97803a91748b6c0d64fec7b23c5"}
      {:artifact "language-foundation-applicability-review.edn" :sha256 "a773243f1dc41659a4e72927af7302b00b0169f969e89454f9940d0049730d07"}]
     :source-documents docs :source-occurrences (mapv #(dissoc % :context) occurrences) :source-facets facets
     :source-dispositions dispositions :units units :edits edits
     :task-source-amendments [{:path runtime-path :before-sha256 (sha256 runtime-before)
                               :before runtime-before :after-sha256 (sha256 runtime-text) :after runtime-text
                               :owner-role :runtime-maintainer
                               :depends-on ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
                                            "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"]
                               :projection-action "Regenerate docs/component-roadmaps/runtime.md with tools/runtime_task_projection.clj --write after applying. Preserve all five IDs and Open statuses."}]
     :unresolved-stable-ids (mapv :id qs)
     :unresolved-facet-ids (mapv :id (filter #(or (:unresolved %) (and (= :semantic (:kind %)) (empty? (:unit-ids %)))) facets))
     :scope-decisions ["Existing PR445 language/Reader decomposition and review lineage consumed unchanged; no new extraction or alias deletion."
                       "All cross-family owners and outside-family facets are retained for central composition."
                       "Current source drift is pinned separately; original facets never receive shifted current offsets."
                       "Task binding freshness is not full entailment or implementation acceptance. All requirements remain unassessed."
                       "Accounting-only clauses remain historical source; no enforcement or bulk mixed-paragraph supersession is introduced."]
     :validation {:documents (count docs) :occurrences (count occurrences) :facets (count facets)
                  :requirements (count qs) :field-edits (count edits)
                  :missing-task-ids (mapv :id (filter #(nil? (:task-reference %)) qs))
                  :semantic-review "Inherited source decomposition conserved. Only L15 managed/native startup amendment reviewed here; residual entailment/nonoverlap stays explicit."
                  :authority "No denominator, implementation acceptance, canonical manifest write or historical receipt rewrite."}}))

(defn run [args]
  (let [p (prepare (second args))]
    (case (first args)
      "--write" (do (.mkdirs (java.io.File. "specs/requirement-reconciliation"))
                    (spit output-path (with-out-str (pp/pprint p))))
      "--check" (require! (= p (edn/read-string (slurp output-path))) "Proposal differs from exact inputs")
      (throw (ex-info "Use --write or --check" {})))
    (prn (:validation p)) (shutdown-agents)))
(when (seq *command-line-args*) (run *command-line-args*))
