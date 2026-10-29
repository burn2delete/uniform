(ns tooling-requirements
  "Checks the bounded tooling proposal against immutable source inputs; no adoption."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def package-ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(def input-ref "53ae6a403b20d15155bb5b04911fe7ac0092b921")
(def api-ref "c0dc5790197a1bbd3a8aa285e5458c272d877220")
(def source-qualified-ref "2ab753db9a43a5544379cf72c75c91eeab3d23b7")
(def source-qualified-sha256 "8779c1a73781d241272aabe2868335068d27f3eafc346d861b46a8aae774af1f")
(def proposal-path "specs/tools-workflow/requirement-proposal.edn")
(def family-ids (set (map #(str "T" %) (range 1 14))))
(defn require! [ok message]
  (when-not ok (throw (ex-info message {:diagnostic "TOOLING-REQUIREMENTS"}))))
(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable input: " ref ":" path)) out))
(defn sha256 [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))

;; Evaluate only these three unchanged functions from the specified occurrence API.
;; This neither loads nor executes its audit/CLI entry point or compiler tooling.
(defn occurrence-api []
  (let [text (git-text api-ref "tools/requirement_reconciliation.clj")
        forms (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
                (loop [out []]
                  (let [form (read {:eof ::end} r)]
                    (if (= ::end form) out (recur (conj out form))))))
        target (create-ns 'tooling-requirements-source-api)]
    (binding [*ns* target]
      (refer 'clojure.core)
      (alias 'str 'clojure.string)
      (doseq [form forms :when (and (seq? form) (= 'defn (first form))
                                    (#{'require! 'sha256 'source-occurrences} (second form)))]
        (eval form)))
    (ns-resolve target 'source-occurrences)))

(defn inputs []
  (let [raw (git-text package-ref "contracts/requirement-reconciliation.edn")
        snapshot-raw (git-text input-ref "specs/requirement-reconciliation/source-task-inputs.edn")
        format-raw (git-text input-ref ".specify/requirement-proposal-format.edn")
        qualified-raw (git-text source-qualified-ref proposal-path)
        snapshot (edn/read-string snapshot-raw)
        manifest (edn/read-string raw)
        facets (filterv #(family-ids (:document-id %)) (:source-facets manifest))
        fids (set (map :id facets))
        requirements (filterv #(some fids (:source-facet-ids %)) (:requirements manifest))
        docs (filterv #(family-ids (:id %)) (:original-documents snapshot))]
    (require! (= "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690" (sha256 raw))
              "Committed PR445 manifest identity differs")
    (require! (and (= "423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94" (sha256 snapshot-raw))
                   (= "69ec49fb954ba66ce86621d5dfac3a4cb0b514b84915277b86944ae2ec857adc" (sha256 format-raw))
                   (= source-qualified-sha256 (sha256 qualified-raw)))
              "Immutable snapshot, format or source-qualified proposal differs")
    {:manifest manifest :requirements requirements :facets facets :snapshot snapshot :docs docs
     :source-qualified-proposal (edn/read-string qualified-raw)
     :dispositions (filterv #(family-ids (first (str/split (:occurrence-id %) #"/")))
                           (:source-dispositions manifest))}))

(defn apply-edits [manifest edits]
  (reduce
    (fn [m {:keys [collection id field-path before-present? before after]}]
      (require! (and (= :requirements collection) (= 1 (count field-path))
                     (#{:task-reference :additional-task-interfaces :task-gap :task-interface-qualification :obligation} (first field-path)))
                "Edit outside bounded requirement fields")
      (let [rows (get m collection) matches (keep-indexed #(when (= id (:id %2)) %1) rows)]
        (require! (= 1 (count matches)) "Missing or duplicate edited identity")
        (let [path (into [collection (first matches)] field-path)
              row (get rows (first matches))]
          (require! (and (= before-present? (contains? row (first field-path)))
                         (= before (get-in m path))) "Stale or duplicate field edit")
          (assoc-in m path after)))) manifest edits))

(defn task! [{:keys [path sha256 marker id acceptance-boundary-sha256]}]
  (require! (and (#{"specs/tools-workflow/tasks.md" "specs/cli-workflow/tasks.md"} path)
                 (string? marker) (string? id)) "Unexpected task source")
  (let [text (slurp path)
        start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        parts (str/split text (re-pattern (java.util.regex.Pattern/quote start)) -1)]
    (require! (= sha256 (tooling-requirements/sha256 text)) "Task hash mismatch")
    (require! (= 2 (count parts)) "Task marker missing or duplicated")
    (let [tails (str/split (second parts) (re-pattern (java.util.regex.Pattern/quote end)) -1)
          _ (require! (= 2 (count tails)) "Task end marker missing or duplicated")
          blocks (str/split (first tails) #"(?m)(?=^- \[[ x]\] `)")
          matches (filter #(str/starts-with? % (str "- [ ] `" id "` ")) blocks)]
      (require! (= 1 (count matches)) "Missing, renamed or accepted task")
      (let [block (first matches)
            boundary (second (re-find #"(?m)^Closing evidence(?: and governing contract)?: (.+)$" block))]
        (require! (and (str/includes? block "\nStatus: Open\n") boundary
                       (= acceptance-boundary-sha256 (tooling-requirements/sha256 boundary)))
                  "Task status or acceptance boundary mismatch")
        {:acceptance boundary
         :title (second (re-find #"^- \[ \] `[^`]+` ([^\n]+)" block))}))))

(defn check! [proposal input]
  (let [{:keys [docs facets requirements dispositions manifest source-qualified-proposal]} input
        units (:units proposal) ids (set (map :id requirements))
        qualified-units (into {} (map (juxt :proposed-id identity) (:units source-qualified-proposal)))
        patched (apply-edits manifest (:edits proposal))
        by-id (into {} (map (juxt :id identity) (:requirements patched)))
        occurrences (occurrence-api)
        actual-occurrences (mapcat #(occurrences (:id %) (slurp (:path %))) docs)
        by-occurrence (into {} (map (juxt :id identity) actual-occurrences))]
    (require! (= "requirement-reconciliation/phase-13-tooling-and-developer-experience" (:scope-key proposal))
              "Wrong family scope")
    ;; Reuse the exact source/task clauses from the independently inspected
    ;; candidate, never its deficient checker or an implementation acceptance.
    (doseq [k [:kind :version :authority :input :dependency-history :inherited-proposal-batch
               :scope-decisions :validation]]
      (require! (= (get source-qualified-proposal k) (get proposal k))
                (str "Missing or changed input/lineage metadata: " k)))
    (require! (= (mapv :id requirements) (:unresolved-requirement-ids proposal))
              "Unresolved requirement population changed")
    (require! (= (:edits source-qualified-proposal) (:edits proposal))
              "Consumable edits differ from exact source-qualified payloads")
    (require! (= docs (:source-documents proposal)) "Current/original document pins changed")
    (doseq [{:keys [path current-sha256 original-sha256]} docs]
      (require! (= current-sha256 original-sha256 (sha256 (slurp path))) "Source hash drift"))
    (require! (= facets (:preserved-source-facets proposal)) "Lost or changed original facet/negative condition")
    (require! (every? #(or (= :nonnormative (:kind %)) (seq (:unit-ids %))) facets)
              "Normative facet lacks a retained unit")
    (require! (= requirements (:preserved-requirements proposal)) "Original requirement lineage changed")
    (require! (= dispositions (:source-dispositions proposal)) "Lost occurrence disposition")
    (require! (= (vec actual-occurrences) (:source-occurrences proposal)) "Invented or lost occurrence")
    (require! (and (= ids (set (map :proposed-id units))) (= (count ids) (count units)))
              "Requirement population changed")
    (require! (and (every? #(ids (:id %)) (:edits proposal))
                   (= (count (:edits proposal))
                      (count (set (map (juxt :collection :id :field-path) (:edits proposal))))))
              "Duplicate or cross-family edit")
    (doseq [f facets]
      (let [o (by-occurrence (:source-occurrence-id f))
            doc (first (filter #(= (:document-id f) (:id %)) docs))
            bytes (.getBytes ^String (slurp (:path doc)) "UTF-8")
            raw (String. bytes (int (:start-byte f)) (int (- (:end-byte f) (:start-byte f))) "UTF-8")]
        (require! (and o (<= (:start-byte o) (:start-byte f)) (<= (:end-byte f) (:end-byte o))
                       (= (:raw-sha256 f) (sha256 raw))) "Wrong facet span/hash/occurrence")))
    (doseq [u units]
      (let [r (by-id (:proposed-id u))
            qualified (qualified-units (:proposed-id u))
            original (first (filter #(= (:id r) (:id %)) requirements))
            expected (filterv #(contains? (set (:source-facet-ids r)) (:id %)) facets)
            qualification (:task-interface-qualification r)
            task (task! (:task-reference r))]
        (require! (= (select-keys r [:id :source-facet-ids :atomicity :nonoverlap :historical-support :scope-reviews])
                     (select-keys original [:id :source-facet-ids :atomicity :nonoverlap :historical-support :scope-reviews]))
                  "Prior semantic/review state changed")
        (require! (and (= (:atomicity qualified) (:atomicity u))
                       (= (:historical-support original) (:historical-support u))
                       (= (:equivalence-proposals qualified) (:equivalence-proposals u)))
                  "Unit atomicity, history or contextual relationship changed")
        (require! (and (= :proposed (:state qualification))
                       (= :unassessed (:implementation-acceptance qualification))
                       (= (:title task) (:owner-role qualification))
                       (= (:acceptance task) (:acceptance qualification))
                       (= (:source-facet-ids r) (:source-facet-ids qualification))
                       (= (:dependency-boundary qualified) (:dependency-boundary qualification)
                          (:dependency-boundary u)))
                  "Qualification loses source-bound acceptance, owner or dependencies")
        (require! (and (= (:obligation u) (:obligation r)) (= expected (:acceptance-facets u))
                       (= (into [(:task-reference r)] (:additional-task-interfaces r)) (:task-references u))
                       (= :unassessed (:current-acceptance u))
                       (= :unreviewed (:nonoverlap u))
                       (= :acceptance-qualification (get-in r [:task-gap :state]))
                       (= [{:state :acceptance-qualification :qualification (get-in r [:task-gap :reason])}]
                          (:task-gaps u))) "Unit differs from guarded outcome or loses residual")
        (doseq [ref (:task-references u)] (task! ref))
        (doseq [eq (:equivalence-proposals u)]
          (require! (and (contains? ids (:target eq)) (= :refinement (:kind eq))
                         (seq (:rationale eq)) (= :central-review-pending (:state eq)))
                    "Alias loses context or invents target"))))
    {:documents (count docs) :occurrences (count actual-occurrences) :facets (count facets)
     :requirements (count units) :edits (count (:edits proposal))
     :task-references (count (set (mapcat :task-references units)))
     :implementation-acceptance :unassessed :independent-review :pending}))

(when (= ["--check"] (vec *command-line-args*))
  (prn (check! (edn/read-string (slurp proposal-path)) (inputs)))
  (shutdown-agents))
