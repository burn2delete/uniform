(ns profile-gap-completion
  "Check the exact 34 allocated historical gaps and preview guarded task criteria."
  (:require [clojure.edn :as edn] [clojure.string :as str]))

(def proposal-path "specs/requirement-reconciliation/profile-system/gap-completion.edn")
(def partition-sha "f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a")
(def coverage-sha "a7e82612a715713799be4db46cc3e911aff0cb5a2525d4555c8f8e6ded87101f")
(def predecessor "43598c5b87feb4d88928549bb20fc4089768718b")
(defn sha256 [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes ^String s "UTF-8")))))
(defn ensure! [condition reason] (when-not condition (throw (ex-info reason {:diagnostic "PROFILE-GAP-COMPLETION"}))))
(defn git-text [ref path]
  (let [p (.start (ProcessBuilder. ^java.util.List ["git" "show" (str ref ":" path)])) s (slurp (.getInputStream p))]
    (ensure! (zero? (.waitFor p)) "Pinned source unavailable") s))
(def pinned-source (memoize git-text))
(defn record-text [text marker id]
  (let [a (str "<!-- " marker ":start -->") b (str "<!-- " marker ":end -->")
        quote-pattern #(re-pattern (java.util.regex.Pattern/quote %))]
    (ensure! (and (= 1 (count (re-seq (quote-pattern a) text))) (= 1 (count (re-seq (quote-pattern b) text)))) "Task marker missing or repeated")
    (let [body (subs text (+ (.indexOf text a) (count a)) (.indexOf text b))
          rs (str/split (str/trim body) #"\n\n(?=- \[[ x]\])")
          found (filter #(str/starts-with? % (str "- [ ] `" id "` ")) rs)]
      (ensure! (= 1 (count found)) "Literal Open task missing, renamed or repeated") (first found))))
(defn apply-task-file [text edits]
  (ensure! (every? #(= (:sha256 %) (sha256 text)) edits) "Later predecessor task hash differs")
  (ensure! (= (count edits) (count (set (map :id edits)))) "Repeated task edit")
  (reduce (fn [current e]
            (ensure! (= (:before e) (record-text current (:marker e) (:id e))) "Task before record differs")
            (ensure! (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (:before e))) current))) "Ambiguous task text")
            (str/replace-first current (:before e) (:after e))) text edits))
(defn apply-decision [rows e]
  (ensure! (and (= :remaining-decisions (:collection e)) (= [:mapping-completion-proposal] (:field-path e))) "Wrong decision collection or field")
  (let [r (get rows (:id e)) k (first (:field-path e))]
    (ensure! r "Decision absent")
    (ensure! (= (:predecessor-record-sha256 e) (sha256 (pr-str r))) "Later predecessor decision differs")
    (ensure! (= (:before-present? e) (contains? r k)) "Stale or duplicate decision edit")
    (ensure! (= (:before e) (get r k)) "Decision before value differs")
    (assoc rows (:id e) (assoc r k (:after e)))))
(defn read-inputs [p receipt-path]
  (let [part (:partition p) part-text (pinned-source (:ref part) (:path part))
        receipt-text (slurp receipt-path)
        manifest-text (pinned-source "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30" "contracts/requirement-reconciliation.edn")
        family (:accepted-family-input p) family-text (pinned-source (:ref family) (:path family))
        decisions-text (pinned-source predecessor "specs/requirement-reconciliation/source-dispositions.edn")
        baseline-text (pinned-source predecessor "specs/requirement-reconciliation/task-baseline.edn")]
    (ensure! (= partition-sha (:sha256 part) (sha256 part-text)) "Partition identity differs")
    (ensure! (= coverage-sha (get-in p [:coverage-input :sha256]) (sha256 receipt-text)) "Coverage receipt identity differs")
    (ensure! (= "30896820a63f0803dba866b7123a3a7c12b9f1e38927fd1958f5c3bc511879fc" (:sha256 family) (sha256 family-text)) "Accepted family proposal differs")
    (ensure! (= "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690" (sha256 manifest-text)) "Published manifest identity differs")
    {:partition (edn/read-string part-text) :coverage (edn/read-string receipt-text)
     :manifest (edn/read-string manifest-text) :family (edn/read-string family-text)
     :dispositions (edn/read-string decisions-text) :dispositions-sha (sha256 decisions-text)
     :baseline (edn/read-string baseline-text)}))
(defn check-reference! [ref tasks]
  (let [t (get tasks (:id ref)) src (:task-source t) text (pinned-source predecessor (:path ref))]
    (ensure! t "Missing task identity")
    (ensure! (= predecessor (:ref ref)) "Wrong later predecessor")
    (ensure! (= (:path src) (:path ref)) "Wrong task path")
    (ensure! (= (:marker src) (:marker ref)) "Wrong task marker")
    (ensure! (= (:sha256 ref) (sha256 text)) "Wrong task-source hash")
    (record-text text (:marker ref) (:id ref))))
(defn validate! [p inputs]
  (let [{:keys [partition coverage manifest dispositions dispositions-sha baseline]} inputs
        assigned (first (filter #(= "phase-03" (:family %)) (:partitions partition)))
        ids (:ids assigned) id-set (set ids)
        historical (into {} (map (juxt :id identity) (:historical-gap-traceability coverage)))
        allocated (into {} (map (juxt :id identity) (:residual-records partition)))
        facets (into {} (map (juxt :id identity) (:source-facets manifest)))
        documents (into {} (map (juxt :id identity) (:original-documents manifest)))
        tasks (into {} (map (juxt :id identity) (:tasks baseline)))
        decisions (into {} (map (juxt :id identity) (:remaining-decisions dispositions)))
        historical-rows (:historical-gap-traceability coverage)]
    (ensure! (= predecessor (get-in p [:predecessor :ref]) (:candidate-source partition)) "Wrong composition predecessor")
    (ensure! (= dispositions-sha (get-in p [:predecessor :source-dispositions-sha256])) "Wrong disposition predecessor hash")
    (ensure! (= ids (get-in p [:partition :assigned-ids]) (mapv :id (:mappings p))) "Missing, duplicated or unassigned gap")
    (ensure! (= 34 (count ids) (count (set ids)) (get-in p [:counts :completed-proposal-mappings])) "Assigned count differs")
    (ensure! (= 0 (get-in p [:counts :existing-full-criterion-witnesses]) (get-in p [:counts :adopted-mapping-completions])
                  (get-in p [:counts :implementation-accepted]) (get-in p [:counts :canonical-task-count-delta])) "False coverage/task/implementation credit")
    (ensure! (= ["ai/profile-tool-contract"] (mapv :id (get-in p [:unresolved :source-policy]))) "Lost actionable mixed-policy residual")
    (ensure! (= :preserved-in-accepted-family-input-not-adjudicated-by-this-patch (get-in p [:unresolved :semantic-splits])) "Lost inherited split qualification")
    (ensure! (= (frequencies (map :coverage-disposition historical-rows))
                 {:explicit-open-source-interface-work 365 :no-same-id-open-row-and-no-full-coverage-witness 196
                  :retired-record-retained-as-history 3 :concrete-task-criterion-witness 13}) "577 histories changed")
    (ensure! (= (sha256 (pr-str historical-rows)) (get-in p [:history-conservation :all-historical-records-sha256])) "History digest differs")
    (ensure! (= (mapv :id (remove #(id-set (:id %)) historical-rows)) (get-in p [:history-conservation :unassigned-and-terminal-ids])) "Lost unassigned or terminal history")
    (ensure! (= (:retained-terminal-history partition) (get-in p [:history-conservation :terminal-history])) "Witness/retirement history changed")
    (doseq [m (:mappings p)]
      (let [id (:id m) a (:after m) original (historical id) dispatch (allocated id) ref (:task-reference a)
            criterion (str "profile-gap/" id) source-fids (:source-facet-ids dispatch)]
        (ensure! (= original (:before m)) "Historical before record differs")
        (ensure! (= dispatch (:dispatch m)) "Dispatch source record differs")
        (ensure! (#{:explicit-open-source-interface-work :no-same-id-open-row-and-no-full-coverage-witness} (:coverage-disposition original)) "Terminal record misassigned as residual")
        (ensure! (= :finite-actionable-task-interface-proposed (:coverage-disposition a)) "Forged existing full-criterion coverage")
        (ensure! (= criterion (:criterion-id a)) "Invented or changed criterion identity")
        (ensure! (and (= :open (:status a)) (= :unassessed (:implementation-acceptance a))
                      (= :not-conferred (:whole-facet-entailment-acceptance a))) "Proposal inflated acceptance")
        (doseq [k [:inputs :acceptance :negative-cases :outputs :acceptance-text]]
          (ensure! (and (string? (k a)) (not (str/blank? (k a)))) "Missing concrete criterion field"))
        (doseq [k [:inputs :acceptance :negative-cases :outputs]]
          (ensure! (str/includes? (:acceptance-text a) (k a)) "Criterion text loses concrete semantics"))
        (check-reference! ref tasks)
        (ensure! (= source-fids (mapv :id (:source-predicates a))) "Lost/duplicated context or negative facet")
        (doseq [f (:source-predicates a)]
          (let [canonical (facets (:id f)) d (documents (:document-id f))
                text (pinned-source (:source-baseline manifest) (:path d)) bs (.getBytes ^String text "UTF-8")
                raw (String. (java.util.Arrays/copyOfRange bs (int (:start-byte f)) (int (:end-byte f))) "UTF-8")]
            (ensure! (= canonical (dissoc f :raw-text :acceptance-role)) "Facet/occurrence/raw-span identity changed")
            (ensure! (= (:raw-text f) raw) "Source predicate text differs")
            (ensure! (= (:raw-sha256 f) (sha256 raw)) "Wrong raw source hash")
            (ensure! (= (:acceptance-role f) (case (:kind f) :context :supporting-context
                                              :syntax-or-schema-example :illustrative-shape-not-mandatory-literal
                                              :required-source-predicate)) "Source example/context silently promoted or condition dropped")))
        (doseq [d (:dependencies a)] (check-reference! (:task-reference d) tasks))
        (let [e (:decision-edit m)]
          (ensure! (= (str "interface/" id) (:id e)) "Wrong remaining-decision ID")
          (ensure! (= {:criterion-id criterion :task-reference ref :status :open :source-facet-ids source-fids
                       :coverage-disposition :finite-actionable-task-interface-proposed :implementation-acceptance :unassessed} (:after e))
                    "Decision criterion differs")
          (apply-decision decisions e))))
    (doseq [e (:task-edits p)]
      (check-reference! e tasks)
      (ensure! (and (= :task-source-records (:collection e)) (= [:text] (:field-path e)) (:before-present? e)) "Wrong task edit operation")
      (ensure! (= (:before e) (record-text (pinned-source predecessor (:path e)) (:marker e) (:id e))) "Task record before differs")
      (let [ms (filter #(= (:id e) (get-in % [:after :task-reference :id])) (:mappings p))
            text (str/join " " (map #(get-in % [:after :acceptance-text]) ms))
            platform? (= "specs/platform-workflow/tasks.md" (:path e))
            expected (if platform?
                       (str/replace-first (:before e) "\n\nSource basis:" (str ". " text "\n\nSource basis:"))
                       (str (:before e) (if (= "specs/domains-workflow/tasks.md" (:path e)) "\n\n" " ") text))]
        (ensure! (seq ms) "Task edit outside assigned mapping interfaces")
        (ensure! (= (mapv :id ms) (:historical-ids e)) "Task loses assigned gap")
        (ensure! (= (mapv #(get-in % [:after :criterion-id]) ms) (:criterion-ids e)) "Task loses criterion ID")
        (ensure! (= expected (:after e)) "Task changes original semantics or puts criterion in wrong field")))
    (ensure! (= (set (map #(get-in % [:after :criterion-id]) (:mappings p))) (set (mapcat :criterion-ids (:task-edits p)))) "A mapping has no actual task-field amendment")
    (reduce apply-decision decisions (map :decision-edit (:mappings p)))
    (ensure! (= (set (map :document-id (mapcat #(get-in % [:after :source-predicates]) (:mappings p))))
                 (set (map :id (:source-documents p)))) "Document population differs from assigned source facets")
    (doseq [d (:source-documents p)]
      (ensure! (= d (documents (:id d))) "Source document pin differs")
      (ensure! (= (:sha256 d) (sha256 (pinned-source predecessor (:path d)))) "Current source document differs"))
    {:assigned-gaps 34 :completed-open-interface-proposals 34 :new-task-ids 0 :adopted-mappings 0 :implementation-credit 0}))
(defn preview [p]
  (mapv
    (fn [[path edits]]
      (let [component (second (re-find #"specs/([a-z-]+)-workflow/tasks.md" path))
            namespace (symbol (str component "-task-projection"))
            original (pinned-source predecessor path) after (apply-task-file original edits)
            tool (str "tools/" (str/replace component "-" "_") "_task_projection.clj")]
        ;; Project with the later task source's actual parser. Its two local
        ;; helper dependencies must match the same immutable predecessor.
        (doseq [helper ["tools/task_projection.clj" "tools/task_projection_links.clj"]]
          (ensure! (= (sha256 (slurp helper)) (sha256 (pinned-source predecessor helper))) "Projector helper version differs"))
        (binding [*command-line-args* nil] (load-string (pinned-source predecessor tool)))
        (let [before-rows ((ns-resolve namespace 'records) original) rows ((ns-resolve namespace 'records) after)
              before-by-id (into {} (map (juxt :id identity) before-rows)) by-id (into {} (map (juxt :id identity) rows))
              roadmap (str "docs/component-roadmaps/" component ".md")
              view ((ns-resolve namespace 'project) after (pinned-source predecessor roadmap))]
          (ensure! (= (mapv #(select-keys % [:id :status :checked?]) before-rows) (mapv #(select-keys % [:id :status :checked?]) rows)) "Preview changed task population or status")
          (doseq [e edits]
            (let [field (if (= component "platform") :work (if (= component "domains") :projection :acceptance))
                  body (get-in by-id [(:id e) field])]
              (when (= component "platform") (ensure! (= (get-in before-by-id [(:id e) :evidence]) (get-in by-id [(:id e) :evidence])) "Source basis changed"))
              (doseq [id (:criterion-ids e)] (ensure! (str/includes? body id) "Criterion outside actual acceptance field"))))
          (ensure! (= view ((ns-resolve namespace 'project) after view)) "Projection not stable")
          {:path path :predecessor-sha256 (sha256 original) :proposed-sha256 (sha256 after)
           :projector {:path tool :ref predecessor :sha256 (sha256 (pinned-source predecessor tool))}
           :projected-view-sha256 (sha256 view) :task-ids (mapv :id edits)
           :criterion-evidence
           (mapv (fn [m]
                   (let [text (get-in m [:after :acceptance-text]) start (.indexOf after text)
                         end (+ start (count text))]
                     (ensure! (and (not (neg? start)) (= start (.lastIndexOf after text))) "Criterion text has missing/ambiguous span")
                     {:id (:id m) :before-disposition (get-in m [:before :coverage-disposition])
                      :before-criterion-source (get-in m [:before :criterion-source])
                      :after-disposition :finite-actionable-task-interface-proposed
                      :criterion-id (get-in m [:after :criterion-id])
                      :task-id (get-in m [:after :task-reference :id]) :path path
                      :proposed-file-sha256 (sha256 after)
                      :start-byte (alength (.getBytes (subs after 0 start) "UTF-8"))
                      :end-byte (alength (.getBytes (subs after 0 end) "UTF-8"))
                      :raw-sha256 (sha256 text) :status :open :authority :proposed-source-span-only}))
                 (filter #(= path (get-in % [:after :task-reference :path])) (:mappings p)))
           :criterion-ids (vec (mapcat :criterion-ids edits))})))
    (sort-by key (group-by :path (:task-edits p)))))
(defn reject! [label f] (ensure! (try (f) false (catch clojure.lang.ExceptionInfo _ true)) (str "Negative control accepted: " label)) label)
(defn run [args]
  (ensure! (= 1 (count args)) "Pass the exact frozen coverage receipt as the sole argument")
  (let [p (edn/read-string (slurp proposal-path)) inputs (read-inputs p (first args))
        valid (fn [q] (validate! q inputs)) counts (valid p)
        negatives
        [(reject! :lost-assigned-id #(valid (update p :mappings subvec 1)))
         (reject! :foreign-family-id #(valid (assoc-in p [:mappings 0 :id] "language/errors/normalize-foreign-failure")))
         (reject! :forged-full-coverage #(valid (assoc-in p [:mappings 0 :after :coverage-disposition] :concrete-task-criterion-witness)))
         (reject! :false-implementation-acceptance #(valid (assoc-in p [:mappings 0 :after :implementation-acceptance] :accepted)))
         (reject! :lost-negative-case #(valid (assoc-in p [:mappings 0 :after :negative-cases] "")))
         (reject! :lost-context-facet #(valid (update-in p [:mappings 0 :after :source-predicates] subvec 1)))
         (reject! :wrong-facet-hash #(valid (assoc-in p [:mappings 0 :after :source-predicates 0 :raw-sha256] "wrong")))
         (reject! :wrong-task-id #(valid (assoc-in p [:mappings 0 :after :task-reference :id] "missing")))
         (reject! :wrong-task-marker #(valid (assoc-in p [:mappings 0 :after :task-reference :marker] "wrong")))
         (reject! :stale-task-predecessor #(valid (assoc-in p [:mappings 0 :after :task-reference :sha256] "wrong")))
         (reject! :wrong-later-source #(valid (assoc-in p [:predecessor :ref] "6276c94b0b753c888ef8fecc794bcc81245ea3b3")))
         (reject! :lost-history #(valid (assoc-in p [:history-conservation :unassigned-and-terminal-ids] [])))
         (reject! :changed-retirement #(valid (assoc-in p [:history-conservation :terminal-history] [])))
         (reject! :lost-policy-residual #(valid (assoc-in p [:unresolved :source-policy] [])))
         (reject! :lost-task-criterion #(valid (update p :task-edits subvec 1)))
         (reject! :platform-source-basis-corruption
                  #(valid (update p :task-edits (fn [es] (mapv (fn [e] (if (= "platform-implementation-tasks" (:marker e))
                                                                      (assoc e :after (str (:before e) " misplaced acceptance")) e)) es)))))
         (reject! :duplicate-task-application #(let [es (val (first (group-by :path (:task-edits p)))) text (pinned-source predecessor (:path (first es))) after (apply-task-file text es)] (apply-task-file after es)))
         (reject! :duplicate-decision-application #(let [rs (into {} (map (juxt :id identity) (:remaining-decisions (:dispositions inputs))))
                                                        es (map :decision-edit (:mappings p)) applied (reduce apply-decision rs es)] (reduce apply-decision applied es)))]
        previews (preview p)]
    (prn {:kind :profile-gap-completion-controls :proposal-sha256 (sha256 (slurp proposal-path))
          :partition-sha256 partition-sha :coverage-sha256 coverage-sha :predecessor predecessor
          :counts counts :negative-controls negatives :task-previews previews
          :mapping-ids (mapv :id (:mappings p)) :mapping-unresolved-ids []
          :semantic-policy-unresolved-ids (mapv :id (get-in p [:unresolved :source-policy]))
          :independent-review :pending :canonical-writes false :implementation-acceptance :not-conferred})))

(run *command-line-args*)
