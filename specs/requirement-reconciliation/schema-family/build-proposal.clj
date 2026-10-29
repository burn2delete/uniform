(ns build-proposal
  "Bounded S1-S9 proposal; reads immutable PR445 inputs and never adopts them."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str] [clojure.pprint :as pp]))

(def input-ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(def task-base "6276c94b0b753c888ef8fecc794bcc81245ea3b3")
(def manifest-path "contracts/requirement-reconciliation.edn")
(def output-path "specs/requirement-reconciliation/schema-family/proposal.edn")
(def task-path "specs/domains-workflow/tasks.md")
(def lineage-path "specs/requirement-reconciliation/schema-family/lineage.edn")
(defn require! [ok message]
  (when-not ok (throw (ex-info message {:diagnostic "SCHEMA-FAMILY"}))))
(defn git-text [ref path]
  (let [{:keys [exit out err]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable input " ref ":" path " " err)) out))
(defn sha256 [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))
(def manifest-text (git-text input-ref manifest-path))
(require! (= "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690"
             (sha256 manifest-text)) "PR445 manifest mismatch")
(def manifest (edn/read-string manifest-text))

;; Evaluate only the exact existing pure occurrence API, not its audit/CLI.
(def occurrence-form
  (with-open [r (java.io.PushbackReader. (java.io.StringReader.
                  (git-text input-ref "tools/requirement_reconciliation.clj")))]
    (first (filter #(and (seq? %) (= 'defn (first %)) (= 'source-occurrences (second %)))
             (take-while #(not= ::eof %) (repeatedly #(read r false ::eof)))))))
(require! occurrence-form "Missing preserved occurrence API")
(def source-occurrences
  (binding [*ns* (the-ns 'build-proposal)] (var-get (eval occurrence-form))))
(def source-ids (set (map #(str "S" %) (range 1 10))))
(def sources (filterv #(source-ids (:id %)) (:original-documents manifest)))
(def lineage (edn/read-string (slurp lineage-path)))
(require! (= "90d6fa1690ffbfffabc6bb916763398b2c9a761564ee94c23bbf899961f3a70f"
             (sha256 (slurp lineage-path))) "Delivered lineage excerpt mismatch")
(def frozen-input-ref "53ae6a403b20d15155bb5b04911fe7ac0092b921")
(def frozen-input-path "specs/requirement-reconciliation/source-task-inputs.edn")
(def frozen-input-text (git-text frozen-input-ref frozen-input-path))
(require! (= "423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94"
             (sha256 frozen-input-text)) "Frozen source/task input mismatch")
(doseq [s sources
        :let [pin (first (filter #(= (:id s) (:id %))
                          (:original-documents (edn/read-string frozen-input-text))))]]
  (require! (= (:sha256 s) (:original-sha256 pin) (:current-sha256 pin))
            "Frozen source pin drift"))
(def facets (:source-facets-after lineage))
(def facet-ids (set (map :id facets)))
(def inherited (:active-records lineage))
(def unit-ids (set (map :id inherited)))
(def source-texts (into {} (map (fn [s] [(:id s) (slurp (:path s) :encoding "UTF-8")]) sources)))
(defn raw-slice [facet]
  (let [bytes (.getBytes ^String (get source-texts (:document-id facet)) "UTF-8")]
    (String. (java.util.Arrays/copyOfRange bytes (int (:start-byte facet)) (int (:end-byte facet))) "UTF-8")))
(def occurrences (vec (mapcat #(source-occurrences (:id %) (get source-texts (:id %))) sources)))
(def occurrence-ids (set (map :id occurrences)))
(def dispositions
  (let [prior (into {} (map (juxt :occurrence-id identity) (:source-dispositions-after lineage)))]
    (mapv (fn [o]
            (or (get prior (:id o))
                (do (require! (= :layout (:structural-kind o)) "Unclassified nonlayout occurrence")
                    {:occurrence-id (:id o) :kind :layout :unit-ids [] :facet-ids []
                     :rationale "Exact original blank layout retained; no semantic unit inferred."}))) occurrences)))
(defn task-ref [path marker id]
  {:path path :sha256 (sha256 (slurp path)) :marker marker :id id})
(defn unit [r]
  (let [local (filterv #(some #{(:id r)} (:unit-ids %)) facets)
        schema? (str/starts-with? (:id r) "schema/")
        prior (:task-reference r)
        reference (cond schema? (task-ref task-path "domains-implementation-tasks" "schema")
                        prior (task-ref (:path prior) (:marker prior) (:id prior)))]
    {:proposed-id (:id r) :obligation (:obligation r)
     :inherited-record r
     :acceptance-facets (mapv #(assoc % :qualification (raw-slice %)) local)
     :task-references (if reference [reference] [])
     :task-gaps (if schema?
                  ["Open grouped schema interface now explicitly names S1-S9 acceptance. Per-target/provider implementation and exact accepted evidence remain unassessed. No new task or requirement is accepted."]
                  [(if prior
                     "Existing Runtime interface is retained as a partial consumer boundary; exact source-context entailment and implementation evidence remain unassessed."
                     "No complete primary source-context interface is established here. Preserve inherited supplementary interfaces and unresolved owner entailment; do not substitute the schema producer or migration T001-T004.")])
     :equivalence-proposals
     (if (= "schema/release-evidence-gating" (:id r))
       [{:target "Release policy decision consumer" :kind :refinement
         :rationale "S9 schema validation checks required evidence shape; actual release authorization remains a separate consumer decision. Central owner adjudicates overlap."}]
       [])
     :historical-support (:historical-support r)
     :atomicity (:atomicity r) :nonoverlap :unreviewed :current-acceptance :unassessed}))
(defn field-operations []
  (vec
    (mapcat
      (fn [r]
        (let [reference (assoc (task-ref task-path "domains-implementation-tasks" "schema")
                         :scope :source-qualified-grouped-interface
                         :current-acceptance :unassessed)
              before (:additional-task-interfaces r)
              after (conj (vec before) reference)
              gap {:state :partial-acceptance-interface
                   :reason "The existing Open schema task now names the S1-S9 predicates. Concrete target/provider implementations and exact accepted results remain unassessed; supplementary interfaces and historical support are preserved."}]
          (for [[field value] [[:additional-task-interfaces after] [:task-gap gap]]]
            {:id (:id r) :field field :operation :guarded-field-edit
             :before-present? (contains? r field) :before (get r field)
             :after-present? true :after value :other-fields :preserve})))
      (filter #(str/starts-with? (:id %) "schema/") inherited))))
(defn proposal []
  {:kind :gravity/noncanonical-requirement-proposal :version 1
   :scope-key "requirement-reconciliation/phase-10-schema-data-and-interop"
   :authority :discovery-and-semantic-proposal-only
   :input {:ref input-ref :path manifest-path :sha256 (sha256 manifest-text)
           :frozen-input-ref frozen-input-ref :frozen-input-path frozen-input-path
           :frozen-input-sha256 (sha256 frozen-input-text)
           :format-path ".specify/requirement-proposal-format.edn"
           :format-sha256 (sha256 (git-text input-ref ".specify/requirement-proposal-format.edn"))
           :task-base task-base :task-base-tree "9f464ed0f03866bd4978e4213a488f3b1ae48683"
           :occurrence-api-sha256 (sha256 (pr-str occurrence-form))}
   :delivered-lineage {:path lineage-path :sha256 (sha256 (slurp lineage-path))
                       :inputs (:inputs lineage)
                       :qualification "Delivered Language295 splits are consumed as input. This patch neither re-adopts nor re-adjudicates them. Guarded operations apply only after central composition matches each before field."}
   :source-documents sources :source-occurrences occurrences :source-facets facets
   :units (mapv unit inherited) :source-dispositions dispositions
   :requirement-field-operations (field-operations)
   :composition-holds (:occurrence-predecessor-conflicts lineage)
   :inherited-batch (first (filter #(= "runtime-domains-schema" (:id %)) (:proposal-batches manifest)))
   :scope-decisions
   [{:id "schema-family/retained-source-accounting-clause" :state :unresolved
     :source-facet-ids (mapv :id (filter #(and (= "S2" (:document-id %))
                                             (str/includes? (raw-slice %) "recursion limits")) facets))
     :reason "S2 recursion/length text is retained byte-exact. Input/work quotas are removed; independently meaningful representation and validation rules survive. Do not blanket supersede this mixed decoder condition."}
    {:id "schema-family/cross-family-equivalence" :state :unresolved
     :reason "Inherited boundary-validation, compatibility, Runtime and release relationships retain their target IDs in the inherited batch. Central owner alone adjudicates equivalence; no deletion or join is proposed here."}]
   :validation {:source-conservation :checked-by-build-proposal
                :semantic-qualification "Manual S1-S9 source review; existing grouped schema task expanded. Inherited semantic IDs, facets, records and decisions conserved, not extracted or counted again."
                :authority "No aggregate manifest adoption, independent acceptance, implementation completion, denominator or rate."}})

(defn validate! [p]
  (require! (= sources (:source-documents p)) "Source pin/ID/path mismatch")
  (doseq [s sources]
    (require! (= (:sha256 s) (sha256 (get source-texts (:id s)))) "Wrong source hash"))
  (require! (= occurrences (:source-occurrences p)) "Lost or invented source occurrence")
  (require! (= facets (:source-facets p)) "Lost or modified source facet/negative condition")
  (require! (= (set (map :id (:source-facets-before lineage))) facet-ids)
            "Delivered normalization lost an original source facet")
  (require! (= dispositions (:source-dispositions p)) "Lost source disposition/lineage")
  (require! (= unit-ids (set (map :proposed-id (:units p)))) "Lost or invented semantic ID")
  (require! (= (count inherited) (count (:units p))) "Duplicate semantic ID")
  (doseq [u (:units p)
          :let [r (first (filter #(= (:id %) (:proposed-id u)) inherited))
                expected (unit r)]]
    (require! (= expected u) "Changed context, alias, acceptance, facet binding or inherited record")
    (doseq [f (:acceptance-facets u)]
      (require! (occurrence-ids (:source-occurrence-id f)) "Invented occurrence ID")
      (require! (= (:raw-sha256 f) (sha256 (raw-slice f))) "Wrong raw UTF-8 span hash"))
    (doseq [{:keys [path sha256 marker id]} (:task-references u)
            :let [text (slurp path)]]
      (require! (= sha256 (build-proposal/sha256 text)) "Wrong task source hash")
      (require! (and (str/includes? text (str "<!-- " marker ":start -->"))
                    (str/includes? text (str "<!-- " marker ":end -->"))
                    (str/includes? text (str "`" id "`"))) "Missing task marker or literal ID")))
  (require! (= (:scope-decisions (proposal)) (:scope-decisions p)) "Lost unresolved mixed condition")
  (require! (= (field-operations) (:requirement-field-operations p)) "Changed field operation or predecessor")
  (require! (= (:delivered-lineage (proposal)) (:delivered-lineage p)) "Changed delivered lineage")
  (require! (= (:composition-holds (proposal)) (:composition-holds p)) "Lost predecessor conflict")
  (require! (= (proposal) p) "Changed bounded input, historical lineage or proposal metadata")
  true)

(defn apply-field-operations [records operations]
  (reduce (fn [by-id {:keys [id field before-present? before after-present? after]}]
            (let [r (get by-id id)]
              (require! (and r (= before-present? (contains? r field))
                            (= before (get r field))) "Incompatible later field delta")
              (assoc by-id id (if after-present? (assoc r field after) (dissoc r field)))))
          records operations))

(defn negative-controls! [p]
  (let [bad [(update p :source-documents pop)
             (assoc-in p [:source-documents 0 :sha256] "wrong")
             (update p :source-occurrences pop) (update p :source-facets pop)
             (update p :units pop)
             (assoc-in p [:units 0 :task-references 0 :path] "missing-task.md")
             (assoc-in p [:units 0 :task-references 0 :sha256] "wrong")
             (assoc-in p [:units 0 :task-references 0 :marker] "renamed")
             (assoc-in p [:units 0 :task-references 0 :id] "renamed")
             (assoc-in p [:units 0 :acceptance-facets 0 :qualification] "Negative condition lost")
             (assoc-in p [:units 0 :equivalence-proposals]
                       [{:target "schema/boundary-validation" :kind :alias :rationale "Different context"}])
             (assoc-in p [:units 0 :current-acceptance] :accepted)
             (update p :composition-holds pop)
             (assoc-in p [:requirement-field-operations 0 :before] :incompatible)]]
    (doseq [b bad]
      (require! (try (validate! b) false (catch clojure.lang.ExceptionInfo _ true))
                "Negative control accepted"))
    (let [records (into {} (map (juxt :id identity) inherited))
          operations (field-operations)
          result (apply-field-operations records operations)
          id (:id (first operations)) field (:field (first operations))]
      (require! (= (set (keys records)) (set (keys result))) "Field patch changed unit population")
      (require! (try (apply-field-operations (assoc-in records [id field] :later-delta) operations)
                    false (catch clojure.lang.ExceptionInfo _ true)) "Stale predecessor accepted")
      (inc (count bad)))))

(when (seq *command-line-args*)
  (let [p (proposal) args (vec *command-line-args*)]
    (require! (#{["--write"] ["--check"]} args) "Usage: --write|--check")
    (validate! p)
    (if (= ["--write"] args)
      (spit output-path (with-out-str (pp/pprint p)))
      (validate! (edn/read-string (slurp output-path))))
    (prn {:documents (count sources) :occurrences (count occurrences) :facets (count facets)
          :inherited-active-candidates (count inherited) :schema-task-interfaces 28
          :guarded-field-operations (count (field-operations))
          :occurrence-composition-holds (count (:occurrence-predecessor-conflicts lineage))
          :unresolved-build-interface "integration/build-input-capture"
          :negative-controls (negative-controls! p)
          :current-acceptance :unassessed :sha256 (sha256 (slurp output-path))})
    (shutdown-agents)))
