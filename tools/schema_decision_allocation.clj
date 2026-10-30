(ns schema-decision-allocation
  "Bounded proposals for the exact phase-10 current-decision allocation."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str] [clojure.pprint :as pp]))

(def base "86747916c4c876d707a1548113409fb939d854c9")
(def source-path "specs/requirement-reconciliation/source-dispositions.edn")
(def plan-path "specs/requirement-reconciliation/schema-family/decision-allocation-plan.edn")
(def output-path "specs/requirement-reconciliation/schema-family/decision-allocation.edn")
(defn require! [ok message]
  (when-not ok (throw (ex-info message {:diagnostic "SCHEMA-DECISION-ALLOCATION"}))))
(defn sha [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))
(def git-text
  (memoize (fn [ref path]
             (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
               (require! (zero? exit) "Missing exact immutable Git source") out))))
(defn normalize [s] (str/lower-case (str/replace s #"\s+" " ")))
(defn byte-span [text raw]
  (let [start (.indexOf ^String text ^String raw)]
    (require! (and (not (neg? start)) (neg? (.indexOf ^String text ^String raw (inc start))))
              "Missing or duplicate exact source witness")
    {:start-byte (alength (.getBytes (subs text 0 start) "UTF-8"))
     :end-byte (alength (.getBytes (subs text 0 (+ start (count raw))) "UTF-8"))
     :raw-sha256 (sha raw)}))
(defn task-record [path marker id]
  (let [text (git-text base path) a (str "<!-- " marker ":start -->")
        b (str "<!-- " marker ":end -->") start (.indexOf ^String text a) end (.indexOf ^String text b)]
    (require! (and (<= 0 start) (< start end)
                  (neg? (.indexOf ^String text a (inc start))) (neg? (.indexOf ^String text b (inc end))))
              "Missing/duplicate task marker")
    (let [region (subs text (+ start (count a)) end)
          matches (re-seq (re-pattern (str "(?ms)^- \\[ \\] `" (java.util.regex.Pattern/quote id)
                                          "`[^\\n]*\\n.*?(?=^- \\[|\\z)")) region)]
      (require! (= 1 (count matches)) "Missing, checked or duplicate actual task ID")
      {:path path :source base :file-sha256 (sha text) :marker marker :id id
       :record (str/trim (first matches)) :status :open})))
(defn criterion-text [text prefix]
  (let [start (.indexOf ^String text ^String prefix)]
    (require! (and (<= 0 start) (neg? (.indexOf ^String text ^String prefix (inc start))))
              "Missing/duplicate criterion")
    (let [end (.indexOf ^String text "\n\n" start)]
      (subs text start (if (neg? end) (count text) end)))))
(defn supplement [g]
  (str "Criterion `" (:criterion-id g) "` (Open): Input: " (:inputs g)
       " Outputs: " (str/join "; " (:outputs g)) ". Accept: " (:acceptance g)
       " Refuse: " (str/join "; " (:refusals g)) ". Dependencies consume actual artifacts: "
       (str/join "; " (:dependencies g)) ". Diagnostics: " (:diagnostics g)
       " These are required future products/results; no execution or implementation acceptance is claimed."))
(defn task-amendments [groups]
  (mapv
    (fn [[[path marker id label] gs]]
      (let [record (task-record path marker id) prefix (str label ":")
            index (.indexOf ^String (:record record) prefix)]
        (require! (<= 0 index) "Missing actual task acceptance field")
        (let [before (str/trim (subs (:record record) (+ index (count prefix))))
              joiner (if (= label "Projected task block") "\n\n" " ")
              after (str before joiner (str/join joiner (map supplement gs)))]
          {:operation :guarded-task-acceptance-field-append :path path :source base
           :marker marker :id id :field-label label :file-before-sha256 (:file-sha256 record)
           :record-before-sha256 (sha (:record record)) :before-present? true :before before
           :before-sha256 (sha before) :after after :after-sha256 (sha after)
           :criterion-ids (mapv :criterion-id gs) :new-task-count 0
           :preserve-other-fields-records-statuses-and-later-text true})))
    (sort-by (comp pr-str key) (group-by (juxt :task-path :marker :task-id :field-label) groups))))
(defn existing-witness [prefix]
  (let [path "specs/domains-workflow/tasks.md" text (git-text base path)
        raw (criterion-text text prefix)]
    (merge {:kind :existing-current-criterion :path path :source base
            :file-sha256 (sha text) :task-id "schema" :marker "domains-implementation-tasks"
            :text raw :implementation-status :open}
           (byte-span text raw))))
(defn proposed-witness [group amendment]
  (let [raw (supplement group)]
    (merge {:kind :proposed-task-field-relative-criterion
            :path (:task-path group) :source-base base :task-id (:task-id group)
            :marker (:marker group) :field-label (:field-label group)
            :criterion-id (:criterion-id group) :text raw
            :field-after-sha256 (:after-sha256 amendment) :implementation-status :open
            :adoption :pending}
           (byte-span (:after amendment) raw))))
(defn proposal [partition-path]
  (let [partition-text (slurp partition-path)
        _ (require! (= "179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74" (sha partition-text))
                    "Changed or wrong exact-530 partition")
        partition (edn/read-string partition-text)
        assignment (first (filter #(= "phase-10" (:family %)) (:partitions partition)))
        assigned (set (:ids assignment))
        _ (require! (= 25 (count assigned) (:count assignment)) "Wrong phase-10 allocation")
        source-text (git-text base source-path) source (edn/read-string source-text)
        decisions (:remaining-decisions source) by-id (into {} (map (juxt :id identity) decisions))
        records (filterv #(assigned (:id %)) (:records partition))
        plan-text (slurp plan-path)
        _ (require! (= "8f26903ddb6a30b9deda537c9f698e740a4c7bcd0cedad831032cd8bb30db5ab" (sha plan-text))
                    "Changed authored criterion/output/refusal plan")
        plan (edn/read-string plan-text) groups (:groups plan)
        group-by-id (into {} (for [g groups id (keys (:roles g))] [id g]))
        reuse-by-id (into {} (map (juxt :id identity) (:existing-bindings plan)))
        all-plan-ids (concat (mapcat #(keys (:roles %)) groups) (keys reuse-by-id))
        _ (require! (and (= assigned (set all-plan-ids)) (= 25 (count all-plan-ids))) "Lost/duplicate/invented allocation")
        _ (require! (and (= 833 (count decisions)) (every? #(= :open (:status %)) decisions)) "Changed833 Open states")
        acceptance-path (get-in source [:current-criterion-bindings :accepted-manifest])
        accepted-text (git-text base acceptance-path) accepted (edn/read-string accepted-text)
        _ (require! (= 430 (get-in accepted [:counts :accepted-mappings])) "Wrong accepted430 baseline")
        amendments (task-amendments groups)
        docs (into {} (map (juxt :id identity) (:original-documents source)))
        facets (vec (vals (into (sorted-map) (map (juxt :id identity) (mapcat :source-facet-metadata records)))))
        facet-witnesses
        (mapv (fn [f]
                (let [d (get docs (:document-id f)) text (when d (slurp (:path d)))
                      _ (require! (and d (= (:current-sha256 d) (sha text))) "Changed/missing actual source document")
                      bytes (.getBytes ^String text "UTF-8")
                      raw (String. (java.util.Arrays/copyOfRange bytes (int (:start-byte f)) (int (:end-byte f))) "UTF-8")]
                  (require! (= (:raw-sha256 f) (sha raw)) "Wrong source facet raw bytes")
                  {:metadata f :path (:path d) :document-sha256 (:current-sha256 d) :raw raw
                   :role-policy "Context remains conditional context, not whole-output proof; source predicates qualify the named producer/checker/consumer. Unresolved metadata is retained verbatim."})) facets)
        operations
        (mapv
          (fn [dispatch]
            (let [id (:id dispatch) before (get by-id id) group (group-by-id id) reuse (reuse-by-id id)
                  _ (require! (and before (= :open (:status before))
                                    (not (contains? before :criterion-task-allocation))) "Missing or already allocated current decision")
                  _ (require! (= (:required-output dispatch) (:required-output before)) "Changed required-output predicate")
                  _ (require! (= (set (:source-facet-ids dispatch)) (set (:source-facet-ids before))) "Changed current source facet membership")
                  amendment (when group (first (filter #(some #{(:criterion-id group)} (:criterion-ids %)) amendments)))
                  witnesses (if group [(proposed-witness group amendment)]
                              (mapv existing-witness (:criterion-prefixes reuse)))
                  joined (normalize (str/join " " (map :text witnesses)))
                  _ (doseq [literal (concat (:positive reuse) (:negative reuse))]
                      (require! (str/includes? joined (normalize literal)) "Missing exact positive/negative current criterion witness"))
                  task-path (:path (first witnesses)) task-id (:task-id (first witnesses))
                  dependency-path (str/replace task-path #"tasks\.md$" "plan.md")
                  dependency-text (git-text base dependency-path)
                  allocation
                  {:state (if group :proposed-minimal-criterion-supplement :proposed-existing-exact-criterion-binding)
                   :required-output (:required-output before) :semantic-source-id (:semantic-source-id before)
                   :primary-task {:path task-path :id task-id :marker (:marker (first witnesses))
                                  :source base :file-sha256 (sha (git-text base task-path))
                                  :implementation-status :open}
                   :role (if group (get (:roles group) id) (:role reuse))
                   :criterion-witnesses witnesses :positive-witnesses (or (:positive reuse) [(:acceptance group)])
                   :refusals (or (:negative reuse) (:refusals group))
                   :outputs (or (:outputs group) (:outputs reuse))
                   :artifact-dependencies (or (:dependencies group) (:dependencies reuse)
                                             [(str "Consume the actual dependency artifacts explicitly named in these exact current criterion witnesses: " (str/join " " (map :text witnesses)))])
                   :dependency-source {:path dependency-path :source base :sha256 (sha dependency-text)}
                   :source-contracts (:source-contracts dispatch) :source-facet-ids (:source-facet-ids before)
                   :source-facet-metadata (:source-facet-metadata dispatch)
                   :unresolved-facet-metadata-ids (:unresolved-facet-metadata-ids dispatch)
                   :unresolved-metadata-policy :retained-blocks-applicable-proof-no-bulk-reclassification
                   :legacy-accountable-task-policy :preserved-context-never-substitutes-operation-witness
                   :accepted-whole-output-proof :not-conferred :whole-attached-facet-proof :not-conferred
                   :independent-review :pending :canonical-adoption :pending :implementation-acceptance :unassessed}]
              {:id id :collection :remaining-decisions :field-path [:criterion-task-allocation]
               :operation :guarded-field-edit :source base :record-before-sha256 (sha (pr-str before))
               :before-present? false :before nil :after-present? true :after allocation
               :before-record before :dispatch-record dispatch :prior-guard (:guard dispatch)
               :preserve-other-record-fields-and-open-state true})) records)
        prior-review (filterv #(= "phase-10" (:family %)) (:reviewed-family-inputs source))
        prior-historical (filterv #(= "phase-10" (:family %)) (:historical-interface-compositions source))]
    {:kind :noncanonical-schema-current-decision-field-proposals :schema-version 1
     :scope "phase-10" :assignment assignment
     :inputs {:partition-name "own-decision530-partition-8b4543c3.edn" :partition-sha256 (sha partition-text)
              :partition-source (:source partition) :base base :source-path source-path :source-sha256 (sha source-text)
              :plan-path plan-path :plan-sha256 (sha plan-text)
              :accepted430 {:path acceptance-path :sha256 (sha accepted-text) :counts (:counts accepted)}}
     :reused-reviewed-source-family-inputs prior-review :reused-historical-compositions prior-historical
     :source-facet-witnesses facet-witnesses :task-field-proposals amendments :decision-field-proposals operations
     :counts {:assigned-decisions 25 :completed-duplicate-decisions 0 :existing-exact-criterion-bindings 8
              :decisions-needing-proposed-supplements 17 :new-criterion-groups 9 :task-field-amendments 3
              :new-task-count 0 :accepted430-unchanged true :implementation-decisions-open 833
              :unplaced-decision-ids [] :pending-independent-review-ids (:ids assignment)}
     :nonclaims [:not-additional-historical131-mappings :not-task-or-implementation-completion
                 :not-whole-attached-facet-entailment :no-shared-canonical-file-write
                 :no-sh-source-version-successor :canonical-owner-adopts] }))
(defn apply-decision [rows op]
  (let [index (first (keep-indexed #(when (= (:id op) (:id %2)) %1) rows)) r (when index (nth rows index))]
    (require! (and r (= (:record-before-sha256 op) (sha (pr-str r)))
                  (= (:before-present? op) (contains? r :criterion-task-allocation))
                  (= (:before op) (:criterion-task-allocation r))) "Stale/duplicate/incompatible decision predecessor")
    (assoc rows index (assoc r :criterion-task-allocation (:after op)))))
(defn apply-task-field [value op]
  (require! (and (= value (:before op)) (= (sha value) (:before-sha256 op))
                (str/starts-with? (:after op) value)) "Stale/nonappend task field proposal")
  (:after op))
(defn validate! [p expected]
  (require! (= expected p) "Lost/forged allocation, predicate, refusal, role, facet, state or dependency")
  (doseq [op (:task-field-proposals p)]
    (require! (= (:after op) (apply-task-field (:before op) op)) "Task field proposal failed"))
  (let [rows (:remaining-decisions (edn/read-string (git-text base source-path)))
        after (reduce apply-decision rows (:decision-field-proposals p))]
    (require! (= (mapv (juxt :id :status) rows) (mapv (juxt :id :status) after)) "Changed833 identities/Open states")
    (require! (= 25 (count (filter #(contains? % :criterion-task-allocation) after))) "Wrong allocation application population"))
  true)
(defn controls! [p]
  (let [bad [(update p :decision-field-proposals pop)
             (update p :decision-field-proposals conj (first (:decision-field-proposals p)))
             (assoc-in p [:decision-field-proposals 0 :id] "unassigned")
             (assoc-in p [:decision-field-proposals 0 :after :role] :context-is-proof)
             (assoc-in p [:decision-field-proposals 0 :after :primary-task :id] "renamed")
             (assoc-in p [:decision-field-proposals 0 :after :criterion-witnesses 0 :raw-sha256] "forged")
             (assoc-in p [:decision-field-proposals 0 :after :refusals] [])
             (assoc-in p [:decision-field-proposals 0 :after :artifact-dependencies] [])
             (assoc-in p [:decision-field-proposals 0 :after :source-facet-ids] [])
             (assoc-in p [:decision-field-proposals 0 :after :implementation-acceptance] :accepted)
             (assoc-in p [:decision-field-proposals 0 :after :accepted-whole-output-proof] :accepted)
             (assoc-in p [:counts :implementation-decisions-open] 808)
             (assoc-in p [:task-field-proposals 0 :after] "Broad heading only")
             (assoc-in p [:counts :new-task-count] 25)]]
    (doseq [x bad] (require! (try (validate! x p) false (catch clojure.lang.ExceptionInfo _ true)) "Negative control accepted"))
    (let [op (first (:decision-field-proposals p)) rows (:remaining-decisions (edn/read-string (git-text base source-path)))
          modified (mapv #(if (= (:id op) (:id %)) (assoc % :required-output "later text") %) rows)]
      (doseq [attempt [#(apply-decision modified op) #(apply-decision (apply-decision rows op) op)
                       #(apply-task-field "later task text" (first (:task-field-proposals p)))
                       #(apply-task-field (:after (first (:task-field-proposals p))) (first (:task-field-proposals p)))]]
        (require! (try (attempt) false (catch clojure.lang.ExceptionInfo _ true)) "Stale or duplicate application accepted")))
    (+ 4 (count bad))))
(when (seq *command-line-args*)
  (let [[mode partition & extra] *command-line-args*]
    (require! (and (#{"--write" "--check"} mode) partition (empty? extra)) "Usage: --write|--check PARTITION_EDN")
    (let [p (proposal partition)]
      (validate! p p)
      (if (= "--write" mode) (spit output-path (with-out-str (pp/pprint p)))
          (validate! (edn/read-string (slurp output-path)) p))
      (prn {:counts (:counts p) :unique-facets (count (:source-facet-witnesses p))
            :negative-controls (controls! p) :sha256 (sha (slurp output-path))})
      (shutdown-agents))))
