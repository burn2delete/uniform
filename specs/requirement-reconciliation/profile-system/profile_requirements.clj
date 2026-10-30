(ns profile-requirements
  "Read-only checks for the bounded original P1-P13 proposal. No compiler launch."
  (:require [clojure.edn :as edn] [clojure.string :as str]))

(def proposal-path "specs/requirement-reconciliation/profile-system/proposal.edn")
(defn sha256 [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String s "UTF-8")))))
(defn require! [ok message]
  (when-not ok (throw (ex-info message {:diagnostic "PROFILE-REQUIREMENTS"}))))
(defn git-text [ref path]
  (let [p (.start (ProcessBuilder. ^java.util.List ["git" "show" (str ref ":" path)]))
        text (slurp (.getInputStream p))]
    (require! (zero? (.waitFor p)) "Pinned Git source unavailable") text))
(defn pinned-edn [{:keys [ref path sha256]}]
  (let [text (git-text ref path)]
    (require! (= sha256 (profile-requirements/sha256 text)) "Pinned input digest differs")
    (edn/read-string text)))
(defn family? [id] (boolean (re-matches #"P(?:[1-9]|1[0-3])" id)))
(defn byte-slice [text start end]
  (String. (java.util.Arrays/copyOfRange (.getBytes ^String text "UTF-8") (int start) (int end)) "UTF-8"))

;; Execute only the three exact public helper declarations from the requested
;; occurrence API. The manifest/compiler/proof entrypoints are never loaded.
(def api-ref "c0dc5790197a1bbd3a8aa285e5458c272d877220")
(defn load-occurrence-api! []
  (with-open [r (java.io.PushbackReader.
                 (java.io.StringReader. (git-text api-ref "tools/requirement_reconciliation.clj")))]
    (doseq [form (take-while #(not= ::eof %) (repeatedly #(read {:eof ::eof} r)))
            :when (and (seq? form) (= 'defn (first form))
                       (#{'sha256 'require! 'source-occurrences} (second form)))]
      (binding [*ns* (the-ns 'profile-requirements)] (eval form)))))
(declare source-occurrences)

(defn task-record [text marker id]
  (let [start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")]
    (require! (and (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote start)) text)))
                   (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote end)) text))))
              "Missing or repeated task marker")
    (let [body (subs text (+ (.indexOf text start) (count start)) (.indexOf text end))
          records (str/split (str/trim body) #"\n\n(?=- \[[ x]\])")
          matches (filter #(re-find (re-pattern (str "^- \\[[ x]\\] `" (java.util.regex.Pattern/quote id) "` ")) %) records)]
      (require! (= 1 (count matches)) "Missing, renamed or repeated literal task ID")
      (first matches))))

(defn apply-field [rows {:keys [collection id field-path before-present? before after]}]
  (require! (= :requirements collection) "Unexpected edit collection")
  (require! (= [:profile-family-task-interface] field-path) "Edit would overwrite existing authority")
  (require! (contains? rows id) "Edit targets missing requirement")
  (let [record (get rows id) k (first field-path)]
    (require! (= before-present? (contains? record k)) "Stale or duplicate field application")
    (require! (= before (get record k)) "Field before value differs")
    (assoc rows id (assoc record k after))))

(defn validate! [p manifest inputs source-texts task-texts]
  (let [documents (filterv #(family? (:id %)) (:original-documents inputs))
        facets (filterv #(family? (:document-id %)) (:source-facets manifest))
        ids (set (mapcat :unit-ids facets))
        records (into {} (map (juxt :id identity) (:requirements manifest)))
        task-inputs (into {} (map (juxt (fn [t] [(:path t) (:id t)]) identity) (:canonical-task-inputs inputs)))
        dispositions (filterv #(re-find #"^P[0-9]+/source/" (:occurrence-id %)) (:source-dispositions manifest))
        occurrences (vec (mapcat (fn [d] (map #(dissoc % :context) (source-occurrences (:id d) (get source-texts (:id d))))) documents))]
    (require! (= "requirement-reconciliation/phase-03-profile-system" (:scope-key p)) "Wrong source family")
    (require! (= documents (:source-documents p)) "Lost or invented document pin")
    (doseq [d documents]
      (require! (= (:current-sha256 d) (:original-sha256 d) (sha256 (get source-texts (:id d)))) "Wrong current/original source hash"))
    (require! (= occurrences (:source-occurrences p)) "Lost or changed raw occurrence")
    (require! (= facets (:source-facets p)) "Lost facet, negative condition or source context")
    (require! (= dispositions (:source-dispositions p)) "Lost or changed source disposition")
    (let [aliases (filterv #(and (= :reviewed (:review-state %)) (or (ids (:id %)) (ids (:target %)))) (:aliases manifest))]
      (require! (= aliases (:aliases p)) "Alias changed or context discarded"))
    (doseq [f facets]
      (let [text (get source-texts (:document-id f))]
        (require! (= (:raw-sha256 f) (sha256 (byte-slice text (:start-byte f) (:end-byte f)))) "Wrong facet raw byte hash")
        (require! (or (= :context (:kind f)) (seq (:unit-ids f))) "Normative facet lacks an owner")))
    (require! (= ids (set (map :proposed-id (:units p)))) "Missing or invented semantic ID")
    (require! (= (count ids) (count (:units p)) (count (:edits p))) "Duplicate unit or edit")
    (require! (= (:inherited-decisions p)
                 (vec (for [r (:scope-model-reviews manifest) d (:decisions r) :when (ids (:id d))]
                        {:review (select-keys r [:family :candidate-ref :artifact-sha256 :qualification]) :decision d})))
              "Inherited decision lineage changed")
    (doseq [u (:units p)]
      (let [r (get records (:proposed-id u))]
        (require! (= (:obligation r) (:obligation u)) "Obligation/context changed")
        (require! (= (:inherited-record-sha256 u) (sha256 (pr-str r))) "Historical/decision lineage changed")
        (require! (= (:historical-support r) (:historical-support u)) "Historical support changed")
        (require! (= (select-keys r [:atomicity :nonoverlap]) (select-keys u [:atomicity :nonoverlap])) "Inherited review state changed")
        (require! (= (:equivalence-proposals u)
                     (vec (for [d (:inherited-decisions p) :when (= (:id r) (get-in d [:decision :id]))] (:decision d))))
                  "Unit lost its split/alias/qualification decision")
        (require! (= (mapv :id (filter #(some #{(:id r)} (:unit-ids %)) facets)) (:source-facet-ids u)) "Unit lost a contextual facet")
        (require! (= (mapv #(select-keys % [:id :source-occurrence-id :start-byte :end-byte :start-line :end-line :raw-sha256 :kind :unresolved])
                           (filter #(some #{(:id r)} (:unit-ids %)) facets)) (:acceptance-facets u))
                  "Unit acceptance span differs")
        (require! (= :unassessed (:current-acceptance u)) "Proposal inflated implementation acceptance")
        (require! (seq (:task-gaps u)) "Unassessed implementation qualification erased")
        (doseq [ref (concat (:task-references u) (:dependencies u))]
          (let [t (get task-inputs [(:path ref) (:id ref)]) text (get task-texts (:path ref))]
            (require! t "Task reference absent from frozen canonical population")
            (require! (= (:sha256 ref) (:file-sha256 t) (sha256 text)) "Wrong applying task hash")
            (require! (= (:source-ref ref) (:source-commit inputs)) "Wrong applying source commit")
            (require! (= (:marker ref) (str (:component t) "-implementation-tasks")) "Wrong task marker")
            (task-record text (:marker ref) (:id ref))))))
    (require! (= (:edits p)
                 (mapv (fn [u] {:collection :requirements :id (:proposed-id u) :field-path [:profile-family-task-interface]
                                :before-present? false :before nil
                                :after (select-keys u [:task-references :dependencies :task-amendment-id :task-gaps :current-acceptance])}) (:units p)))
              "Guarded field edit differs from reviewed unit proposal")
    (doseq [e (:task-edits p)]
      (require! (= [:text] (:field-path e)) "Unexpected task field")
      (require! (= :task-source-records (:collection e)) "Unexpected task collection")
      (require! (= (:source-sha256 e) (sha256 (get task-texts (:path e)))) "Stale task-source amendment")
      (require! (= (:before e) (task-record (get task-texts (:path e)) (:marker e) (:id e))) "Task amendment before differs")
      (let [expected (case (:insertion e)
                       :work-before-source-basis
                       (do (require! (= "platform-implementation-tasks" (:marker e)) "Work insertion has wrong task owner")
                           (require! (= 1 (count (re-seq #"\n\nSource basis:" (:before e)))) "Work insertion lacks exact evidence boundary")
                           (str/replace-first (:before e) "\n\nSource basis:" (str (:addition e) "\n\nSource basis:")))
                       :record-tail (str (:before e) (:addition e))
                       (throw (ex-info "Unknown task insertion" {})))]
        (require! (and (:before-present? e) (seq (:addition e)) (= expected (:after e)))
                  "Task amendment loses existing acceptance"))
      (require! (str/starts-with? (:after e) (str "- [ ] `" (:id e) "`")) "Task status or ID changed"))
    (reduce apply-field records (:edits p))
    {:documents (count documents) :occurrences (count occurrences) :facets (count facets)
     :units (count ids) :task-record-amendments (count (:task-edits p)) :requirement-field-edits (count (:edits p))}))

(defn reject! [label thunk]
  (require! (try (thunk) false (catch clojure.lang.ExceptionInfo _ true)) (str "Negative control accepted: " label))
  label)

(defn project-amendments! [p texts]
  (mapv
    (fn [[path edits]]
      (let [component (second (re-find #"specs/([a-z-]+)-workflow/tasks.md" path))
            tool (str "tools/" (str/replace component "-" "_") "_task_projection.clj")
            namespace (symbol (str component "-task-projection"))
            original (get texts path)
            after (reduce (fn [text e]
                            (require! (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (:before e))) text)))
                                      "Ambiguous task replacement span")
                            (str/replace-first text (:before e) (:after e))) original edits)
            roadmap (str "docs/component-roadmaps/" component ".md")]
        (binding [*command-line-args* nil] (load-file tool))
        (let [rows ((ns-resolve namespace 'records) after)
              before-rows ((ns-resolve namespace 'records) original)
              view ((ns-resolve namespace 'project) after (slurp roadmap))]
          (when (= "platform" component)
            (let [old (into {} (map (juxt :id identity) before-rows)) new (into {} (map (juxt :id identity) rows))]
              (doseq [e edits]
                (require! (= (get-in old [(:id e) :evidence]) (get-in new [(:id e) :evidence])) "Platform source basis changed")
                (require! (str/ends-with? (get-in new [(:id e) :work]) (:addition e)) "Platform acceptance landed outside work field"))))
          (require! (= (mapv #(select-keys % [:id :status :checked?]) rows)
                       (mapv #(select-keys % [:id :status :checked?]) before-rows)) "Projected task authority changed")
          (require! (= view ((ns-resolve namespace 'project) after view)) "Task projection not stable")
          {:path path :before-sha256 (sha256 original) :proposed-sha256 (sha256 after)
           :record-amendments (count edits) :projected-view-sha256 (sha256 view)})))
    (sort-by key (group-by :path (:task-edits p)))))

(defn run [args]
  (require! (or (empty? args) (= 1 (count args))) "Usage: clojure -M specs/requirement-reconciliation/profile-system/check.clj [preserved-manifest]")
  (load-occurrence-api!)
  (let [p (edn/read-string (slurp proposal-path)) m (pinned-edn (:input-manifest p)) i (pinned-edn (:frozen-inputs p))
        sources (into {} (map (fn [d] [(:id d) (slurp (:path d))]) (:source-documents p)))
        paths (set (concat (map :path (:task-edits p)) (map :path (mapcat #(concat (:task-references %) (:dependencies %)) (:units p)))))
        tasks (into {} (map (fn [path] [path (slurp path)]) paths))
        counts (validate! p m i sources tasks)
        validate (fn [q] (validate! q m i sources tasks))
        negatives [(reject! :wrong-source-hash #(validate! p m i (update sources "P1" str " ") tasks))
                   (reject! :lost-negative-condition #(validate (update p :source-facets (fn [fs] (vec (remove (fn [f] (= :negative-case (:kind f))) fs))))))
                   (reject! :wrong-facet-hash #(validate (assoc-in p [:source-facets 0 :raw-sha256] "wrong")))
                   (reject! :wrong-raw-occurrence #(validate (assoc-in p [:source-occurrences 0 :id] "invented")))
                   (reject! :alias-different-context #(validate (assoc-in p [:aliases 0 :target] "different-context")))
                   (reject! :missing-task #(validate (assoc-in p [:units 0 :task-references 0 :id] "missing")))
                   (reject! :renamed-task #(validate (assoc-in p [:units 0 :task-references 0 :path] "specs/missing/tasks.md")))
                   (reject! :wrong-task-hash #(validate (assoc-in p [:units 0 :task-references 0 :sha256] "wrong")))
                   (reject! :wrong-task-marker #(validate (assoc-in p [:units 0 :task-references 0 :marker] "wrong")))
                   (reject! :invented-semantic-id #(validate (assoc-in p [:units 0 :proposed-id] "invented")))
                   (reject! :lost-unit-context #(validate (assoc-in p [:units 0 :source-facet-ids] [])))
                   (reject! :false-acceptance #(validate (assoc-in p [:units 0 :current-acceptance] :accepted)))
                   (reject! :lost-split-lineage #(validate (assoc p :inherited-decisions [])))
                   (reject! :lost-historical-support #(validate (assoc-in p [:units 0 :historical-support] [{:scope :invented}])))
                   (reject! :task-loses-existing-negative #(validate (update-in p [:task-edits 0 :after] subs 5)))
                   (reject! :platform-acceptance-in-source-basis
                            #(validate (update p :task-edits
                                         (fn [es] (mapv (fn [e] (if (= :work-before-source-basis (:insertion e))
                                                                 (assoc e :after (str (:before e) (:addition e))) e)) es)))))
                   (reject! :stale-before #(validate (assoc-in p [:edits 0 :before-present?] true)))
                   (reject! :duplicate-application #(let [rows (into {} (map (juxt :id identity) (:requirements m)))
                                                         applied (reduce apply-field rows (:edits p))]
                                                     (reduce apply-field applied (:edits p))))]
        projections (project-amendments! p tasks)]
    (when-let [path (first args)]
      (let [text (slurp path) preserved (edn/read-string text)]
        (require! (= (get-in p [:applying-history :sha256]) (sha256 text)) "Wrong preserved applying history")
        (reduce apply-field (into {} (map (juxt :id identity) (:requirements preserved))) (:edits p))))
    (prn {:kind :profile-family-proposal-controls :authority :source-task-interface-proposal-only
          :proposal-sha256 (sha256 (slurp proposal-path)) :counts counts :negative-controls negatives
          :task-projection-previews projections :canonical-writes false :implementation-acceptance :unassessed
          :preserved-applying-history-guards (if (seq args) :verified :not-requested)
          :independent-review :pending :split-holds (mapv :proposed-id (filter #(some (fn [g] (= :nonoverlap (:kind g))) (:task-gaps %)) (:units p)))})))

(run *command-line-args*)
