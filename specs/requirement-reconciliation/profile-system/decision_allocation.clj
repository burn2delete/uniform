(ns decision-allocation
  "Read-only guards and criterion previews for the exact language/profile allocation."
  (:require [clojure.edn :as edn] [clojure.string :as str]))
(def path "specs/requirement-reconciliation/profile-system/decision-allocation.edn")
(def base "86747916c4c876d707a1548113409fb939d854c9")
(defn sha [s] (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes ^String s "UTF-8")))))
(defn require! [p reason] (when-not p (throw (ex-info reason {:diagnostic "DECISION-ALLOCATION"}))))
(defn git-source [ref file] (let [p (.start (ProcessBuilder. ^java.util.List ["git" "show" (str ref ":" file)])) text (slurp (.getInputStream p))] (require! (zero? (.waitFor p)) "Pinned source absent") text))
(def source (memoize git-source))
(defn record-text [text r]
  (let [start (str "<!-- " (:marker r) ":start -->") end (str "<!-- " (:marker r) ":end -->")
        a (.indexOf text start) b (.indexOf text end)]
    (require! (and (not (neg? a)) (> b a)) "Wrong task marker")
    (let [body (subs text (+ a (count start)) b)
          found (filter #(str/starts-with? % (str "- [ ] `" (:id r) "` ")) (str/split (str/trim body) #"\n\n(?=- \[[ x]\])"))]
      (require! (= 1 (count found)) "Task absent, accepted, renamed or repeated") (first found))))
(defn ref! [r tasks]
  (let [task (tasks (:id r)) original (:task-source task)]
    (require! task "Unknown task ID")
    (require! (= base (:ref r)) "Wrong published task predecessor")
    (require! (= (:path r) (:path original)) "Wrong task path")
    (require! (= (:marker r) (:marker original)) "Wrong task marker")
    (require! (= (:sha256 r) (sha (source base (:path r)))) "Task hash drift")
    (record-text (source base (:path r)) r)))
(defn field-value [record label]
  (if (= label "Projected task block:")
    (let [i (.indexOf record label)] (require! (not (neg? i)) "Missing projected task field") (str/trim (subs record (+ i (count label)))))
    (let [m (re-find (re-pattern (str "(?m)^" (java.util.regex.Pattern/quote label) " (.+)$")) record)]
      (require! m "Missing actual acceptance field") (second m))))
(defn apply-one [rows e]
  (require! (and (= base (:source e)) (= :remaining-decisions (:collection e)) (= [:criterion-task-allocation] (:field-path e))) "Wrong operation source/collection/field")
  (let [r (rows (:id e))]
    (require! r "Decision absent")
    (require! (= (:predecessor-record-sha256 e) (sha (pr-str r))) "Stale or duplicate decision predecessor")
    (require! (= (:before-present? e) (contains? r :criterion-task-allocation)) "Wrong field presence")
    (require! (= (:before e) (:criterion-task-allocation r)) "Wrong actual before value")
    (assoc-in rows [(:id e) :criterion-task-allocation] (:after e))))
(defn validate! [p part sd tasks reused]
  (let [allocated (filterv #(#{"phase-01" "phase-03"} (:primary-dispatch-family %)) (:records part))
        by-id (into {} (map (juxt :id identity) allocated)) rows (into {} (map (juxt :id identity) (:remaining-decisions sd)))
        documents (into {} (map (juxt :id identity) (:original-documents sd)))
        reused-by-id (into {} (map (juxt :id identity) (:mappings reused)))]
    (require! (= 833 (count rows)) "Decision population changed")
    (require! (every? #(= :open (:status %)) (vals rows)) "Input decision status differs")
    (require! (= {"phase-01" 64 "phase-03" 10} (frequencies (map :family (:rows p)))) "Wrong family allocation")
    (require! (= (mapv :id allocated) (mapv :id (:rows p))) "Lost, duplicated or foreign decision ID")
    (require! (= base (get-in p [:predecessor :source])) "Wrong current source")
    (require! (= {:assigned 74 :reused-reviewed-interface-proposals 31 :existing-current-criterion-witnesses 1
                 :new-criterion-proposals 42 :canonical-task-count-delta 0 :implementation-accepted 0 :canonical-adopted 0} (:counts p)) "False scope, task or adoption count")
    (require! (= (mapv #(select-keys % [:id :unresolved-facet-metadata-ids]) (filter #(seq (:unresolved-facet-metadata-ids %)) allocated))
                 (:unresolved-source-index p)) "Unresolved source-index summary changed")
    (require! (= (sha (source base "specs/requirement-reconciliation/source-dispositions.edn")) (get-in p [:predecessor :source-dispositions-sha256])) "Wrong source-dispositions hash")
    (doseq [r (:rows p)]
      (let [a (by-id (:id r)) e (:operation r) v (:after e) sid (:semantic-source-id a)]
        (require! (= (:id r) (:id e)) "Operation targets a different decision")
        (require! (= (:before-record-sha256 r) (:predecessor-record-sha256 e) (sha (pr-str (rows (:id r))))) "Actual current record identity differs")
        (require! (= (:partition-record-sha256 r) (sha (pr-str a))) "Partition record changed")
        (require! (= (:family r) (:primary-dispatch-family a)) "Dispatch family changed")
        (require! (= (:dispatch r) (select-keys a [:primary-dispatch-family :affected-source-families :source-contracts :required-output :decision-kind])) "Required source predicate changed")
        (require! (= (:source-facet-ids a) (:source-facet-ids v)) "Facet IDs lost or invented")
        (require! (= (:unresolved-facet-metadata-ids a) (:unresolved-facet-metadata-ids v)) "Unresolved index identities changed")
        (require! (= (if (seq (:unresolved-facet-metadata-ids a)) :explicit-unresolved-adjudication-required :indexed-known-facets-bound)
                     (:source-index-state v)) "Forged source-index completeness")
        (require! (= (:unresolved-metadata-records r) (filterv #(nil? (:document-id %)) (:source-facet-metadata a))) "Unresolved metadata silently filled or dropped")
        (require! (= (mapv :facet (:known-source-bindings r)) (filterv :document-id (:source-facet-metadata a))) "Known facet metadata changed")
        (doseq [s (:known-source-bindings r)]
          (let [text (source base (:path s)) f (:facet s) b (.getBytes text "UTF-8")
                raw (String. (java.util.Arrays/copyOfRange b (int (:start-byte f)) (int (:end-byte f))) "UTF-8")]
            (require! (= (:path s) (:path (documents (:document-id f)))) "Wrong source document path")
            (require! (= (:ref s) base) "Wrong facet source")
            (require! (= (:file-sha256 s) (sha text)) "Wrong source file hash")
            (require! (= raw (:raw s)) "Lost source condition")
            (require! (= (:raw-sha256 f) (sha raw)) "Wrong raw facet hash")))
        (require! (and (= :open (:status v)) (= :unassessed (:implementation-acceptance v)) (= :pending (:canonical-adoption v))) "Acceptance inflated")
        (require! (= :not-inferred (:full-attached-facet-entailment v)) "Forged full-facet entailment")
        (require! (keyword? (:owner-role v)) "Missing actual role")
        (case (:disposition v)
          :reuse-reviewed-finite-interface-pending-current-composition
          (do (require! (= (:reviewed-proposed-interface v) (:after (reused-by-id sid))) "Reviewed historical interface changed")
              (require! (= :not-conferred (:current-criterion-entailment v)) "Old witness promoted to current proof")
              (when-let [tr (:current-task-context-reference v)] (ref! tr tasks))
              (doseq [tr (:current-dependencies v)] (ref! tr tasks)))
          :proposed-minimal-acceptance-interface
          (let [record (ref! (:task-reference v) tasks) c (:criterion-proposal v)]
            (require! (= (str "decision-criterion/" sid) (:id c)) "Wrong proposed criterion identity")
            (require! (= :not-conferred (:current-criterion-entailment v)) "Proposed criterion marked proved")
            (doseq [k [:positive :negative :outputs]]
              (require! (and (seq (get v k)) (str/includes? (:text c) (get v k))) "Criterion omits positive/refusal/output") )
            (require! (= (:before-value c) (field-value record (:field c))) "Task field before differs")
            (require! (= (:before-sha256 c) (sha (:before-value c))) "Wrong before-field hash")
            (require! (= (:source-file-sha256 c) (get-in v [:task-reference :sha256])) "Wrong criterion source hash")
            (require! (= (:append c) (:text c)) "Wrong criterion append")
            (doseq [dep (:dependencies v)] (ref! (:task-reference dep) tasks)))
          :existing-current-criterion-bound
          (let [record (ref! (:task-reference v) tasks) w (:criterion-witness v) text (source base (:path w)) b (.getBytes text "UTF-8")
                raw (String. (java.util.Arrays/copyOfRange b (int (:start-byte w)) (int (:end-byte w))) "UTF-8")]
            (require! (= "language/provider/initialize-runtime-contract" sid) "Unreviewed current witness routing")
            (require! (= raw (:text w)) "Wrong current criterion span")
            (require! (= (:raw-sha256 w) (sha raw)) "Wrong current criterion digest")
            (require! (str/includes? record raw) "Current criterion outside named task")
            (doseq [condition ["before user code" "provider existence" "artifact versions" "grant scopes" "compiled contracts" "profile/target compatibility" "safety/audit" "reject a missing required provider" "declared fallback"]]
              (require! (str/includes? raw condition) "Current criterion does not entail initialization boundary")))
          (throw (ex-info "Unknown allocation disposition" {})))
        (apply-one rows e)))
    (require! (= {:reuse-reviewed-finite-interface-pending-current-composition 31 :proposed-minimal-acceptance-interface 42 :existing-current-criterion-bound 1}
                 (frequencies (map #(get-in % [:operation :after :disposition]) (:rows p)))) "Disposition counts changed")
    (let [after (reduce apply-one rows (map :operation (:rows p)))]
      (doseq [[id r] rows] (require! (= r (dissoc (after id) :criterion-task-allocation)) "Existing decision/status field modified"))
      after)))
(defn preview [p]
  (let [proposed (filter #(= :proposed-minimal-acceptance-interface (get-in % [:operation :after :disposition])) (:rows p))]
    (mapv (fn [[path rs]]
            (let [text (source base path)
                  after (reduce (fn [text [_id grouped]]
                                  (let [v (get-in (first grouped) [:operation :after]) tr (:task-reference v)
                                        record (record-text text tr) c (:criterion-proposal v) before (:before-value c)
                                        added (str/join " " (map #(get-in % [:operation :after :criterion-proposal :append]) grouped))
                                        _ (require! (every? #(= before (get-in % [:operation :after :criterion-proposal :before-value])) grouped) "Conflicting field predecessors")
                                        _ (require! (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote before)) record))) "Ambiguous field replacement")
                                        changed (str/replace-first record before (str before " " added))]
                                    (require! (str/includes? changed before) "Existing qualifier removed")
                                    (str/replace-first text record changed))) text (sort-by key (group-by #(get-in % [:operation :after :task-reference :id]) rs)))
                  component (second (re-find #"specs/([a-z-]+)-workflow/" path))
                  tool (str "tools/" (str/replace component "-" "_") "_task_projection.clj") namespace (symbol (str component "-task-projection"))]
              (doseq [helper ["tools/task_projection.clj" "tools/task_projection_links.clj"]]
                (require! (= (sha (slurp helper)) (sha (source base helper))) "Local helper differs from pinned projector closure"))
              (binding [*command-line-args* nil] (load-string (source base tool)))
              (let [old-rows ((ns-resolve namespace 'records) text) new-rows ((ns-resolve namespace 'records) after)
                    roadmap (str "docs/component-roadmaps/" component ".md")
                    view ((ns-resolve namespace 'project) after (source base roadmap))]
                (require! (= (mapv #(select-keys % [:id :status :checked?]) old-rows) (mapv #(select-keys % [:id :status :checked?]) new-rows)) "Preview changed task IDs or statuses")
                (require! (= view ((ns-resolve namespace 'project) after view)) "Unstable task projection")
                {:path path :predecessor-sha256 (sha text) :proposed-sha256 (sha after) :view-sha256 (sha view)
                 :decision-ids (mapv :id rs) :task-count-delta 0})))
          (sort-by key (group-by #(get-in % [:operation :after :task-reference :path]) proposed)))))
(defn reject! [label f] (require! (try (f) false (catch clojure.lang.ExceptionInfo _ true)) (str "Negative accepted: " label)) label)
(defn run [args]
  (require! (= 3 (count args)) "Pass PARTITION GUARDED_CORE31_SUCCESSOR CORE31_REVIEW_RECEIPT")
  (let [p (edn/read-string (slurp path)) pt (slurp (first args)) gt (slurp (second args)) rt (slurp (nth args 2))
        _ (require! (= "179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74" (sha pt)) "Partition pin differs")
        _ (require! (= "4b93cefdd91b6d50e81dba4999c7015882e86f599043f8e46cedce270ce642ee" (sha gt)) "Reviewed Core31 successor differs")
        _ (require! (= "abeb0a9283e6a59a7041cecfdd4a27d7898e0cdd564f023d5e9624f6f220dada" (sha rt)) "Core31 review differs")
        part (edn/read-string pt) g (edn/read-string gt)
        reused (edn/read-string (:after (first (filter #(= "specs/requirement-reconciliation/core-language-gap-completion.edn" (:path %)) (:file-edits g)))))
        sd (edn/read-string (source base "specs/requirement-reconciliation/source-dispositions.edn"))
        baseline (edn/read-string (source base "specs/requirement-reconciliation/task-baseline.edn"))
        tasks (into {} (map (juxt :id identity) (:tasks baseline)))
        valid #(validate! % part sd tasks reused) after (valid p)
        new-index (.indexOf (mapv #(get-in % [:operation :after :disposition]) (:rows p)) :proposed-minimal-acceptance-interface)
        unknown-index (.indexOf (mapv #(boolean (seq (get-in % [:operation :after :unresolved-facet-metadata-ids]))) (:rows p)) true)
        negative [(reject! :missing-id #(valid (update p :rows subvec 1)))
                  (reject! :foreign-id #(valid (assoc-in p [:rows 0 :id] "interface/foreign")))
                  (reject! :wrong-guard #(valid (assoc-in p [:rows 0 :operation :predecessor-record-sha256] "wrong")))
                  (reject! :wrong-decision-target #(valid (assoc-in p [:rows 0 :operation :id] "interface/another-decision")))
                  (reject! :wrong-before #(valid (assoc-in p [:rows 0 :operation :before-present?] true)))
                  (reject! :implementation-promotion #(valid (assoc-in p [:rows 0 :operation :after :implementation-acceptance] :accepted)))
                  (reject! :forged-full-facet-proof #(valid (assoc-in p [:rows 0 :operation :after :full-attached-facet-entailment] :accepted)))
                  (reject! :dropped-facet #(valid (assoc-in p [:rows 0 :operation :after :source-facet-ids] [])))
                  (reject! :invented-metadata #(valid (assoc-in p [:rows unknown-index :unresolved-metadata-records] [])))
                  (reject! :lost-index-residual #(valid (assoc-in p [:rows unknown-index :operation :after :unresolved-facet-metadata-ids] [])))
                  (reject! :forged-index-completeness #(valid (assoc-in p [:rows unknown-index :operation :after :source-index-state] :indexed-known-facets-bound)))
                  (reject! :false-adoption-count #(valid (assoc-in p [:counts :canonical-adopted] 74)))
                  (reject! :lost-negative #(valid (assoc-in p [:rows new-index :operation :after :negative] "other refusal")))
                  (reject! :wrong-task #(valid (assoc-in p [:rows new-index :operation :after :task-reference :id] "missing")))
                  (reject! :wrong-task-marker #(valid (assoc-in p [:rows new-index :operation :after :task-reference :marker] "wrong")))
                  (reject! :stale-task-field #(valid (assoc-in p [:rows new-index :operation :after :criterion-proposal :before-value] "later field")))
                  (reject! :duplicate-application #(reduce apply-one after (map :operation (:rows p))))]
        previews (preview p)]
    (prn {:kind :language-profile-allocation-controls :proposal-sha256 (sha (slurp path)) :source base :partition-sha256 (sha pt)
          :counts (:counts p) :negative-controls negative :previews previews :decision-field-proposals 74
          :unresolved-index-decisions (:unresolved-source-index p) :all833-statuses :unchanged :accepted430 :untouched
          :canonical-writes false :independent-review :pending :implementation-acceptance :not-conferred})))
(run *command-line-args*)
