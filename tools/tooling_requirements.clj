(ns tooling-requirements
  "Check the historical Phase 13 proposal against current source views only."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def source-view-path "specs/tools-workflow/current-source-view.edn")
(def manifest-path "contracts/requirement-reconciliation.edn")
(def proposal-path "specs/tools-workflow/requirement-proposal.edn")
(def allowed-task-paths
  #{"specs/tools-workflow/tasks.md" "specs/cli-workflow/tasks.md"})
(def allowed-edit-fields
  #{:task-reference :additional-task-interfaces :task-gap
    :task-interface-qualification :obligation})

(defn require! [ok message]
  (when-not ok
    (throw (ex-info message {:diagnostic "TOOLING-REQUIREMENTS"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1
                       (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                (.getBytes ^String text "UTF-8")))))

(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable input: " ref ":" path))
    out))

(defn verify-commit-tree! [{:keys [commit tree]}]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit)
                 (string? tree) (re-matches #"[0-9a-f]{40}" tree))
            "Invalid pinned Git identity")
  (let [{kind-exit :exit kind-out :out} (shell/sh "git" "cat-file" "-t" commit)
        {tree-exit :exit tree-out :out} (shell/sh "git" "rev-parse" "--verify"
                                                    (str commit "^{tree}"))]
    (require! (and (zero? kind-exit) (= "commit" (str/trim kind-out)))
              (str "Missing pinned commit: " commit))
    (require! (and (zero? tree-exit) (= tree (str/trim tree-out)))
              (str "Pinned commit/tree mismatch: " commit))))

(defn verify-commit! [{:keys [commit]}]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit))
            "Invalid pinned Git commit")
  (let [{kind-exit :exit kind-out :out} (shell/sh "git" "cat-file" "-t" commit)]
    (require! (and (zero? kind-exit) (= "commit" (str/trim kind-out)))
              (str "Missing pinned commit: " commit))))

(declare view)

(defn historical-source-occurrences []
  (let [api (get-in (view) [:historical-source :occurrence-api])
        text (git-text (:commit api) (:path api))
        forms (with-open [reader (java.io.PushbackReader.
                                  (java.io.StringReader. text))]
                (loop [out []]
                  (let [form (read {:eof ::end} reader)]
                    (if (= ::end form) out (recur (conj out form))))))
        target (create-ns 'tooling-requirements-source-api)]
    (binding [*ns* target]
      (refer 'clojure.core)
      (alias 'str 'clojure.string)
      (doseq [form forms
              :when (and (seq? form)
                         (= 'defn (first form))
                         (#{'require! 'sha256 'source-occurrences}
                          (second form)))]
        (eval form)))
    (or (ns-resolve target 'source-occurrences)
        (throw (ex-info "Pinned occurrence API has no source-occurrences function"
                        {:diagnostic "TOOLING-REQUIREMENTS"})))))

(defn view []
  (edn/read-string (slurp source-view-path)))

(defn validate-view! [v]
  (require! (= :gravity/noncanonical-tooling-current-source-view (:kind v))
            "Wrong tooling source-view kind")
  (require! (= 1 (:version v)) "Unsupported tooling source-view version")
  (require! (= :proposal-only (:authority v))
            "Tooling source-view is not proposal-only")
  (require! (= "requirement-reconciliation/phase-13-tooling-and-developer-experience"
              (:scope-key v))
            "Wrong tooling source-view scope")
  (verify-commit-tree! (:base v))
  (verify-commit-tree! (get-in v [:historical-source :proposal]))
  (verify-commit! (get-in v [:historical-source :manifest]))
  (verify-commit! (get-in v [:historical-source :snapshot]))
  (verify-commit! (get-in v [:historical-source :proposal-format]))
  (verify-commit! (get-in v [:historical-source :occurrence-api]))
  (let [historical (:historical-source v)
        current (:current-source v)
        proposal (get-in historical [:proposal])]
    (require! (= (get-in historical [:manifest :sha256])
                 (sha256 (git-text (get-in historical [:manifest :commit])
                                   (get-in historical [:manifest :path]))))
              "Historical manifest bytes drifted")
    (require! (= (get-in historical [:snapshot :sha256])
                 (sha256 (git-text (get-in historical [:snapshot :commit])
                                   (get-in historical [:snapshot :path]))))
              "Historical source snapshot bytes drifted")
    (require! (= (get-in historical [:proposal-format :sha256])
                 (sha256 (git-text (get-in historical [:proposal-format :commit])
                                   (get-in historical [:proposal-format :path]))))
              "Historical proposal format bytes drifted")
    (require! (= (get-in historical [:occurrence-api :sha256])
                 (sha256 (git-text (get-in historical [:occurrence-api :commit])
                                   (get-in historical [:occurrence-api :path]))))
              "Historical occurrence API bytes drifted")
    (require! (= (get-in historical [:proposal :sha256])
                 (sha256 (git-text (:commit proposal) (:path proposal))))
              "Historical tooling proposal bytes drifted")
    (require! (= (get-in current [:manifest :sha256])
                 (sha256 (slurp (get-in current [:manifest :path]))))
              "Current manifest bytes drifted")
    (require! (= (get-in current [:proposal-format :sha256])
                 (sha256 (slurp (get-in current [:proposal-format :path]))))
              "Current proposal format bytes drifted")
    (require! (= (get-in current [:occurrence-api :sha256])
                 (sha256 (slurp (get-in current [:occurrence-api :path]))))
              "Current occurrence API bytes drifted")
    (doseq [{:keys [path] expected-sha :sha256} (get-in current [:documents])]
      (require! (= expected-sha (sha256 (slurp path))) (str "Current document drift: " path)))
    (doseq [{:keys [path] expected-sha :sha256} (get-in current [:task-sources])]
      (require! (= expected-sha (sha256 (slurp path))) (str "Current task source drift: " path)))
    (doseq [[path {:keys [commit marker] expected-sha :sha256}]
            (get-in historical [:task-sources])]
      (require! (= expected-sha (sha256 (git-text commit path)))
                (str "Historical task source drift: " path))
      (require! (and (string? marker) (not (str/blank? marker)))
                (str "Missing historical task marker: " path))))
  (require! (every? #(and (keyword? %) (not= :accepted %))
                    (:nonclaims v))
            "Source-view nonclaims are missing or assert acceptance")
  v)

(defn immutable-inputs []
  (let [v (validate-view! (view))
        historical-proposal (get-in v [:historical-source :proposal])
        proposal-raw (git-text (:commit historical-proposal)
                               (:path historical-proposal))
        proposal (edn/read-string proposal-raw)
        manifest (edn/read-string
                  (slurp (get-in v [:current-source :manifest :path])))
        docs (vec (:source-documents proposal))
        family-ids (set (map :id (get-in v [:current-source :documents])))
        facets (filterv #(family-ids (:document-id %)) (:source-facets manifest))
        facet-ids (set (map :id facets))
        requirements (filterv #(some facet-ids (:source-facet-ids %))
                              (:requirements manifest))
        dispositions (filterv #(family-ids
                                 (first (str/split (:occurrence-id %) #"/")))
                               (:source-dispositions manifest))]
    {:source-view v
     :proposal proposal
     :proposal-raw proposal-raw
     :manifest manifest
     :docs docs
     :facets facets
     :requirements requirements
     :dispositions dispositions}))

(defn apply-edits [manifest edits]
  (reduce
   (fn [m {:keys [collection id field-path before-present? before after]}]
     (require! (and (= :requirements collection) (= 1 (count field-path))
                    (contains? allowed-edit-fields (first field-path)))
               "Edit outside bounded requirement fields")
     (let [rows (:requirements m)
           hits (keep-indexed #(when (= id (:id %2)) %1) rows)]
       (require! (= 1 (count hits)) "Missing or duplicate edited identity")
       (let [index (first hits)
             path [:requirements index (first field-path)]
             row (get rows index)]
         (require! (and (= before-present? (contains? row (first field-path)))
                        (= before (get-in m path))
                        (not= before after))
                   "Stale, duplicate or no-op field edit")
         (assoc-in m path after))))
   manifest edits))

(defn task-details [text {:keys [marker id acceptance-boundary-sha256]
                          expected-sha :sha256}]
  (require! (= expected-sha (sha256 text)) "Task source hash mismatch")
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")
        parts (str/split text (re-pattern (java.util.regex.Pattern/quote start)) -1)]
    (require! (= 2 (count parts)) "Task marker missing or duplicated")
    (let [tails (str/split (second parts)
                           (re-pattern (java.util.regex.Pattern/quote end)) -1)
          _ (require! (= 2 (count tails)) "Task end marker missing or duplicated")
          blocks (str/split (first tails) #"(?m)(?=^- \[[ x]\] `)")
          matches (filter #(str/starts-with? % (str "- [ ] `" id "` ")) blocks)
          block (first matches)
          acceptance (second (re-find
                             #"(?m)^Closing evidence(?: and governing contract)?: (.+)$"
                             (or block "")))
          title (second (re-find #"^- \[ \] `[^`]+` ([^\n]+)" (or block "")))]
      (require! (= 1 (count matches)) "Missing, renamed or accepted task")
      (let [boundary-hash (when acceptance (sha256 acceptance))]
        (require! (or (nil? acceptance-boundary-sha256)
                      (= acceptance-boundary-sha256 boundary-hash))
                  "Task acceptance boundary hash mismatch"))
      (require! (and title acceptance) "Task owner or acceptance boundary missing")
      {:title title :acceptance acceptance})))

(defn current-task! [reference]
  (require! (allowed-task-paths (:path reference)) "Unexpected current task source")
  (task-details (slurp (:path reference)) reference))

(defn historical-task! [v reference]
  (let [path (:path reference)
        historical (get-in v [:historical-source :task-sources path])]
    (require! historical (str "No historical task source for " path))
    (task-details (git-text (:commit historical) path)
                  (merge historical (select-keys reference
                                                  [:path :id :acceptance-boundary-sha256])))))

(defn expected-source-ids [proposal]
  (set (map :id (:preserved-requirements proposal))))

(defn check! [proposal input]
  (let [{:keys [source-view manifest docs facets requirements dispositions]} input
        frozen (:proposal input)
        units (:units proposal)
        expected-ids (expected-source-ids frozen)
        patched (apply-edits manifest (:edits proposal))
        by-id (into {} (map (juxt :id identity) (:requirements patched)))
        occurrence-fn (historical-source-occurrences)
        actual-occurrences (vec (mapcat #(occurrence-fn (:id %) (slurp (:path %))) docs))
        actual-by-occurrence (into {} (map (juxt :id identity) actual-occurrences))
        task-reference-count (count (set (mapcat :task-references units)))
        historical-ref (get-in source-view [:historical-source :proposal :commit])]
    (require! (= (select-keys proposal [:kind :version :scope-key :authority
                                        :input :dependency-history :inherited-proposal-batch
                                        :scope-decisions :validation :nonclaims])
                 (select-keys frozen [:kind :version :scope-key :authority
                                      :input :dependency-history :inherited-proposal-batch
                                      :scope-decisions :validation :nonclaims]))
              "Proposal lineage or authority metadata changed")
    (require! (= (:edits proposal) (:edits frozen))
              "Qualified field edits changed")
    (require! (= (:units proposal) (:units frozen))
              "Qualified unit population changed")
    (require! (= (:preserved-source-facets proposal) facets)
              "Source facet population changed")
    (require! (every? #(or (= :nonnormative (:kind %))
                           (seq (:unit-ids %)))
                      facets)
              "Normative facet lacks a retained unit")
    (require! (= (:preserved-requirements proposal) requirements)
              "Requirement lineage changed")
    (require! (= (:source-dispositions proposal) dispositions)
              "Source disposition population changed")
    (require! (= (vec actual-occurrences) (:source-occurrences proposal))
              "Source occurrence population changed")
    (require! (= docs (:source-documents proposal))
              "Source document pins changed")
    (require! (= expected-ids (set (:unresolved-requirement-ids proposal)))
              "Unresolved requirement population changed")
    (require! (= expected-ids (set (map :proposed-id units)))
              "Unit population changed")
    (require! (= (count expected-ids) (count units)) "Duplicate unit identity")
    (require! (= (get-in source-view [:expected :requirements]) (count units))
              "Unexpected requirement count")
    (require! (= (get-in source-view [:expected :edits]) (count (:edits proposal)))
              "Unexpected edit count")
    (require! (= (get-in source-view [:expected :source-documents]) (count docs))
              "Unexpected source-document count")
    (require! (= (get-in source-view [:expected :source-occurrences])
                 (count actual-occurrences))
              "Unexpected source-occurrence count")
    (require! (= (get-in source-view [:expected :source-facets]) (count facets))
              "Unexpected source-facet count")
    (require! (= (get-in source-view [:expected :source-dispositions])
                 (count dispositions))
              "Unexpected source-disposition count")
    (require! (= (get-in source-view [:expected :task-references])
                 task-reference-count)
              "Unexpected task-reference count")
    (require! (every? #(expected-ids (:id %))
                      (:edits proposal))
              "Edit escapes the current tooling task family")
    (require! (= (count (set (map (juxt :collection :id :field-path)
                                 (:edits proposal))))
                 (count (:edits proposal)))
              "Duplicate qualified field edit")
    (doseq [{:keys [id path current-sha256 original-sha256]} docs]
      (require! (= current-sha256 original-sha256 (sha256 (slurp path)))
                (str "Source document drift: " id)))
    (doseq [facet facets]
      (let [occurrence (actual-by-occurrence (:source-occurrence-id facet))
            doc (first (filter #(= (:document-id facet) (:id %)) docs))
            bytes (.getBytes ^String (slurp (:path doc)) "UTF-8")
            raw (String. bytes (int (:start-byte facet))
                        (int (- (:end-byte facet) (:start-byte facet))) "UTF-8")]
        (require! (and occurrence
                       (<= (:start-byte occurrence) (:start-byte facet))
                       (<= (:end-byte facet) (:end-byte occurrence))
                       (= (:raw-sha256 facet) (sha256 raw)))
                  "Wrong source facet span, occurrence or hash")))
    (doseq [unit units]
      (let [r (by-id (:proposed-id unit))
            qualified (:task-interface-qualification r)
            task-refs (into [(:task-reference r)] (:additional-task-interfaces r))
            historical-task (historical-task! source-view (first task-refs))
            current-reference (fn [reference]
                                (assoc (first (filter #(= (:path %) (:path reference))
                                                       (get-in source-view [:current-source :task-sources])))
                                       :id (:id reference)))
            current-task (current-task! (current-reference (first task-refs)))
            source-facet-ids (:source-facet-ids r)
            expected-facets (filterv #(contains? (set source-facet-ids) (:id %))
                                     facets)]
        (require! (= :proposed (:state qualified))
                  "Task interface is not proposal state")
        (require! (= :unassessed (:implementation-acceptance qualified))
                  "Task interface confers implementation acceptance")
        (require! (and (= (:title historical-task) (:owner-role qualified))
                       (= (:title current-task) (:owner-role qualified))
                       (= (:acceptance historical-task) (:acceptance qualified))
                       (= source-facet-ids (:source-facet-ids qualified)))
                  "Task interface loses owner, historical boundary or facets")
        (require! (and (= (:obligation unit) (:obligation r))
                       (seq source-facet-ids)
                       (= (count source-facet-ids)
                          (count (set source-facet-ids)))
                       (= expected-facets (:acceptance-facets unit))
                       (= (:dependency-boundary unit)
                          (:dependency-boundary qualified))
                       (= :unassessed (:current-acceptance unit))
                       (= :unreviewed (:nonoverlap unit))
                       (= :acceptance-qualification (get-in r [:task-gap :state]))
                       (= [{:state :acceptance-qualification
                            :qualification (get-in r [:task-gap :reason])}]
                          (:task-gaps unit)))
                  "Unit loses guarded acceptance residual")
        (require! (= task-refs (:task-references unit))
                  "Unit task interface population changed")
        (doseq [reference (:task-references unit)]
          (historical-task! source-view reference)
          (current-task! (current-reference reference)))
        (doseq [equivalence (:equivalence-proposals unit)]
          (require! (and (contains? expected-ids (:target equivalence))
                         (= :refinement (:kind equivalence))
                         (seq (:rationale equivalence))
                         (= :central-review-pending (:state equivalence)))
                    "Equivalence context loses pending review"))))
    (require! (= historical-ref
                 (get-in source-view [:historical-source :proposal :commit]))
              "Historical proposal source identity changed")
    {:documents (count docs)
     :occurrences (count actual-occurrences)
     :facets (count facets)
     :requirements (count units)
     :edits (count (:edits proposal))
     :task-references task-reference-count
     :historical-task-criteria :preserved-source-view-only
     :implementation-acceptance :unassessed
     :independent-review :pending
     :authority :proposal-only}))

(when (= ["--check"] (vec *command-line-args*))
  (let [input (immutable-inputs)]
    (prn (check! (:proposal input) input))
    (shutdown-agents)))
