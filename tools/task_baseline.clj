(ns task-baseline
  "Derived census of canonical task records and finite source dispositions."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.pprint :as pp]))

(binding [*command-line-args* nil]
  (load-file "tools/sh_crosswalk.clj")
  (load-file "tools/platform_task_projection.clj"))
(require '[sh-crosswalk] '[sh-inventory] '[platform-task-projection])

(def output-path "specs/requirement-reconciliation/task-baseline.edn")
(def disposition-path "specs/requirement-reconciliation/source-dispositions.edn")
(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "TASK-BASELINE"}))))
(defn status [checked text]
  (let [value (some-> (re-find #"(?m)^(?:Status|Current evidence/status): ([^\n]+)" text) second str/trim)
        accepted? (boolean (and value (str/starts-with? value "Accepted;")))]
    (require! (= checked accepted?) "Task checkbox and declared acceptance disagree")
    {:state (cond accepted? :accepted
                  (and value (str/starts-with? value "Open")) :open
                  (and value (str/starts-with? value "Partial")) :partial
                  (and value (or (str/starts-with? value "Blocked") (str/starts-with? value "Held"))) :blocked
                  :else :unassessed)
     :declared value}))
(defn pinned-text [ref path]
  (let [text (slurp path)]
    (require! (= text (sh-crosswalk/git-source ref path)) (str "Uncommitted or stale source: " path))
    text))
(defn decision-task-ids [decision]
  (if-let [allocation (:criterion-task-allocation decision)]
    (let [links (:task-links allocation)]
      (require! (and (seq links) (every? #(and (string? (:task-id %)) (not (str/blank? (:task-id %)))) links))
                "Criterion allocation needs explicit task identities")
      (set (map :task-id links)))
    (set (concat (when-let [id (:accountable-task decision)] [id]) (:supporting-task-ids decision)))))
(defn task-decisions [decisions task-id]
  (vec (sort (for [d decisions :when (contains? (decision-task-ids d) task-id)] (:id d)))))
(defn criterion-allocation-state [decisions]
  (let [allocated (filter :criterion-task-allocation decisions)]
    (doseq [d allocated] (decision-task-ids d))
    (cond (empty? allocated) :unassessed
          (= (count allocated) (count decisions)) :criterion-witness-bound
          :else :partially-criterion-witness-bound)))
(defn progress [tasks]
  (let [ids (map :id tasks) counts (frequencies (map #(get-in % [:status :state]) tasks))
        accepted (get counts :accepted 0) total (count tasks)]
    (require! (= total (count (set ids))) "Duplicate canonical task identity")
    {:accepted-complete-in-declared-task-scope accepted :total total :remaining (- total accepted)
     :open (get counts :open 0) :partial (get counts :partial 0)
     :recorded-blocked (get counts :blocked 0) :status-unassessed (get counts :unassessed 0)
     :unweighted-ratio {:numerator accepted :denominator total}
     :qualification "Provisional scoped task states only, not a whole-project ratio. Task granularity and complete source-to-task entailment remain unassessed. Scoped accepted tasks do not close components, normative documents, requirements, milestones or release. Unrecorded dependency blocking and whole-requirement evidence remain unassessed."}))
(defn build [ref]
  (let [source (edn/read-string (pinned-text ref disposition-path))
        docs (:original-documents source)
        inventory (pinned-text ref "docs/document-inventory.json")
        registry (pinned-text ref "docs/component-roadmaps/README.md")
        components (mapv second (re-seq #"(?m)^\| [^|\n]+ / `([a-z-]+)` \|" registry))
        rows (vec (mapcat sh-crosswalk/task-records components))
        product-text (pinned-text ref platform-task-projection/task-path)
        product (platform-task-projection/product-records product-text)
        decisions (:remaining-decisions source)
        task (fn [row]
               (let [path (:path row) text (pinned-text ref path)
                     plan (str "specs/" (:component row) "-workflow/plan.md")
                     plan-text (pinned-text ref plan)]
                 {:id (:id row) :component (:component row)
                  :status (status (:checked row) (get-in row [:source :raw]))
                  :criteria-and-evidence-source (dissoc (:source row) :raw)
                  :task-source {:path path :marker (str (:component row) "-implementation-tasks")
                                :sha256 (sh-inventory/sha256 text) :commit ref}
                  :dependencies-source {:path plan :sha256 (sh-inventory/sha256 plan-text) :commit ref}
                  :remaining-decision-ids
                  (task-decisions decisions (:id row))}))
        tasks (into (mapv task rows)
                    (for [row product]
                      {:id (:id row) :component "platform-product"
                       :status (status (str/starts-with? (:status row) "Accepted;") (str "Status: " (:status row)))
                       :criteria (:acceptance row) :dependencies (:dependencies row)
                       :remaining-decision-ids (task-decisions decisions (:id row))
                       :task-source {:path platform-task-projection/task-path :marker "platform-product-implementation-tasks"
                                     :sha256 (sh-inventory/sha256 product-text) :commit ref}}))]
    (require! (= 240 (count docs) (count (set (map :id docs)))) "Original contract disposition loss or duplication")
    (require! (= (set (range 1 241)) (set (map :sequence docs))) "Original document sequence changed")
    (when-let [w (:current-criterion-bindings source)]
      (require! (= (:sha256 w) (sh-inventory/sha256 (pinned-text ref (:path w))))
                "Stale current criterion witness artifact"))
    (doseq [{:keys [path current-sha256]} docs]
      (require! (= current-sha256 (sh-inventory/sha256 (pinned-text ref path))) (str "Contract source drift: " path)))
    (require! (every? (set (map :id tasks)) (keep :accountable-task decisions)) "Missing accountable task for remaining decision")
    (require! (every? (set (map :id tasks)) (mapcat decision-task-ids decisions)) "Missing task for criterion allocation")
    {:kind :provisional-derived-task-census :schema-version 1 :authority :read-only-derived-task-census
     :source-commit ref :inventory-sha256 (sh-inventory/sha256 inventory)
     :dispositions {:path disposition-path :sha256 (sh-inventory/sha256 (slurp disposition-path))}
     :scope {:original-contracts 240 :original-source-families 18
             :whole-project-task-coverage :unestablished
             :task-granularity :pending-concrete-interface-and-coverage-review
             :component-task-count (count rows)
             :release-task-ids (mapv :id (filter #(= "release" (:component %)) rows))
             :release-counting :already-included-in-component-task-count
             :platform-product-task-count (count product)
             :platform-target-task-ids ["hardware" "firmware" "kernel" "devices"]
             :platform-counting :product-interfaces-additional-to-four-existing-target-tasks
             :requirement-candidate-denominator :not-adopted
             :phase-and-migration-checkboxes :not-counted}
     :progress (progress tasks) :tasks (vec (sort-by :id tasks))
     :remaining-decisions {:count (count decisions) :source disposition-path
                           :without-supporting-task-references (count (filter #(empty? (:supporting-task-ids %)) decisions))
                           :criterion-witness-bound (count (filter :criterion-task-allocation decisions))
                           :criterion-allocation-review-pending
                           (count (filter #(= :pending-independent-current-composition-review
                                              (get-in % [:criterion-task-allocation :allocation-review])) decisions))
                           :remaining-criterion-allocation-work (count (remove :criterion-task-allocation decisions))
                           :allocation-to-counted-tasks (criterion-allocation-state decisions)
                           :allocation-review-qualification "Concrete criterion spans do not confer independent composition review or full attached-facet entailment; predecessor allocations retain their recorded review scope."
                           :full-attached-facet-entailment :pending-complete-source-disposition-audit
                           :counting :necessary-work-addressed-through-task-criteria-not-additional-task-units}
     :independent-composed-review :required-separately
     :nonclaims [:no-implementation-credit-from-proposals :no-document-or-milestone-completion
                 :no-seedless-release-or-runtime-claim :no-target-or-product-selection]}))
(defn run [args]
  (let [[mode ref] args]
    (require! (or (and (= "--write" mode) ref (= 2 (count args)))
                  (= ["--check"] (vec args))) "Use --write SOURCE_COMMIT or --check")
    (let [old (when (= "--check" mode) (edn/read-string (slurp output-path)))
          ref (or ref (:source-commit old))
          _ (require! (boolean (re-matches #"[0-9a-f]{40}" ref)) "Use an immutable full source commit")
          result (build ref)]
      (if old (require! (= old result) "Stale or manually changed derived task baseline")
        (spit output-path (with-out-str (pp/pprint result))))
      (prn (select-keys result [:source-commit :scope :progress :remaining-decisions])))))
(when (seq *command-line-args*) (run *command-line-args*) (shutdown-agents))
