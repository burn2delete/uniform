(ns schema-gap-completion
  "Five canonically assigned historical gaps, mapped to finite Open criteria."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str] [clojure.pprint :as pp]))

(def input-path "specs/requirement-reconciliation/schema-family/gap-completion-input.edn")
(def output-path "specs/requirement-reconciliation/schema-family/gap-completion.edn")
(def task-path "specs/domains-workflow/tasks.md")
(def predecessor "43598c5b87feb4d88928549bb20fc4089768718b")
(def assigned-ids ["schema/artifact-validation" "schema/configuration-reload"
                   "schema/database-mapping" "schema/graphql-generation"
                   "schema/release-evidence-gating"])
(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "SCHEMA-GAP-COMPLETION"}))))
(defn sha256 [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))
(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) "Missing exact Git predecessor") out))
(def input-text (slurp input-path))
(require! (= "d7651bffc08bf002b122a0a63154a8b86d4b02535f98f8541c22a968b7a2d419"
             (sha256 input-text)) "Changed frozen assignment/source input")
(def input (edn/read-string input-text))
(require! (= assigned-ids (get-in input [:assignment :ids])) "Wrong assigned ID population")
(require! (= "f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a"
             (sha256 (git-text (get-in input [:partition :ref]) (get-in input [:partition :path]))))
          "Changed canonical partition")
(doseq [[path digest]
        [["specs/requirement-reconciliation/schema-family/proposal.edn"
          "b77b3980b6789125006e95f9a0fa2797482cad2d69543b0e52b6db9f991a715d"]
         ["specs/requirement-reconciliation/schema-family/lineage.edn"
          "90d6fa1690ffbfffabc6bb916763398b2c9a761564ee94c23bbf899961f3a70f"]]]
  (require! (= digest (sha256 (slurp path))) "Changed preserved phase-family lineage"))
(defn schema-field [text]
  (let [start-marker "<!-- domains-implementation-tasks:start -->"
        end-marker "<!-- domains-implementation-tasks:end -->"
        start (.indexOf ^String text start-marker) end (.indexOf ^String text end-marker)]
    (require! (and (<= 0 start) (< start end)
                  (neg? (.indexOf ^String text start-marker (inc start)))
                  (neg? (.indexOf ^String text end-marker (inc end))))
              "Missing, renamed or duplicate task marker")
    (let [matches (re-seq #"(?ms)^- \[ \] `schema` Schema\n\nStatus: Open\n\nProjected task block:\n(.*?)(?=^- \[)"
                          (subs text (+ start (count start-marker)) end))]
    (require! (= 1 (count matches)) "Missing/renamed/duplicate Open schema task")
      (str/trim (second (first matches))))))
(def before-source (git-text predecessor task-path))
(def before (schema-field before-source))
(def task-text (slurp task-path))
(def after (schema-field task-text))
(require! (= "b9afa81873e742484b007edc7030974c5106712d30edc78f8a14ced4f787ca29"
             (sha256 before)) "Wrong schema predecessor field")
(require! (= "c37a0b2b4edf66db23acd01c6a1889f58963e9956c642f78c46ed52ffed27fd8"
             (sha256 after)) "Changed full criterion scope or negative condition")
(def criteria
  (into {} (map (fn [[_ id body]] [id (str "Criterion `" id "` (Open).\n" (str/trim body))])
                (re-seq #"(?ms)^Criterion `([^`]+)` \(Open\)\.\n(.*?)(?=^Criterion |\z)" after))))
(require! (= (set (map #(str % "/full-source-interface") assigned-ids)) (set (keys criteria)))
          "Missing or invented criterion")
(def facets-by-id (into {} (map (juxt :id identity) (:source-facets input))))
(doseq [{:keys [id partition-row historical-record before-coverage]} (:rows input)]
  (require! (= id (:id historical-record) (:id before-coverage) (:id partition-row)) "Wrong historical ID")
  (require! (#{:explicit-open-source-interface-work :no-same-id-open-row-and-no-full-coverage-witness}
             (:coverage-disposition before-coverage)) "Witness or retirement incorrectly reopened")
  (require! (= (:historical-record-sha256 partition-row) (sha256 (pr-str historical-record)))
            "Changed complete historical record")
  (let [compact (mapv #(select-keys (facets-by-id %) [:id :document-id :source-occurrence-id
                                                    :raw-sha256 :start-byte :end-byte])
                      (:source-facet-ids partition-row))]
    (require! (= (:source-facet-records-sha256 partition-row) (sha256 (pr-str compact)))
              "Changed or lost assigned source facet")))
(doseq [{:keys [id path sha256]} (:source-documents input)]
  (require! (= sha256 (schema-gap-completion/sha256 (slurp path))) (str "Changed source " id)))
(doseq [f (:source-facets input)]
  (require! (= (:raw-sha256 f) (sha256 (get-in input [:facet-raw-text (:id f)])))
            "Changed raw facet/negative condition"))
(defn criterion-span [raw]
  (let [start (.indexOf ^String task-text ^String raw)]
    (require! (and (not (neg? start)) (neg? (.indexOf ^String task-text ^String raw (inc start))))
              "Missing or duplicate criterion source text")
    {:start-byte (alength (.getBytes (subs task-text 0 start) "UTF-8"))
     :end-byte (alength (.getBytes (subs task-text 0 (+ start (count raw))) "UTF-8"))
     :raw-sha256 (sha256 raw)}))
(defn row-patch [{:keys [id partition-row before-coverage historical-record]}]
  (let [criterion-id (str id "/full-source-interface") text (get criteria criterion-id)]
    {:id id :operation :guarded-coverage-proposal-merge :preserve-unspecified-fields true
     :before before-coverage :before-sha256 (sha256 (pr-str before-coverage))
     :historical-record historical-record :partition-record partition-row
     :after (merge before-coverage
            {:coverage-disposition :finite-actionable-task-interface-proposal
             :criterion-id criterion-id :criterion-task-id "schema"
             :criterion-text text :criterion-sha256 (sha256 text)
             :criterion-source (merge {:path task-path :sha256 (sha256 task-text)
                                       :marker "domains-implementation-tasks" :task-id "schema"}
                                      (criterion-span text))
             :source-facet-ids (:source-facet-ids partition-row)
             :facet-witnesses (mapv (fn [fid]
                                     {:facet-id fid :source-record (facets-by-id fid)
                                      :raw-text (get-in input [:facet-raw-text fid])
                                      :criterion-id criterion-id :criterion-sha256 (sha256 text)
                                      :entailment :proposed-full-historical-scope})
                                   (:source-facet-ids partition-row))
             :status :open :implementation-acceptance :unassessed
             :independent-entailment-review :pending :canonical-adoption :pending
             :qualification "Finite source-bound criterion is authored in the existing Open schema task; full historical entailment and adoption require independent canonical review. No implementation acceptance is inferred."
             :outputs "The named generated artifacts, accepted/rejected fixture reports, diagnostics and exact source/compiler/policy hashes in this criterion. No execution is claimed."
             :historical-support-policy :retain-exact-prior-scope-no-new-inheritance})}))
(defn proposal []
  {:kind :schema-historical-gap-completion-proposal :schema-version 1
   :scope-key "requirement-reconciliation/phase-10-schema-data-and-interop"
   :inputs {:path input-path :sha256 (sha256 input-text) :partition (:partition input)
            :coverage (:coverage input) :original-manifest (:original-manifest input)}
   :retention (:retention input) :source-documents (:source-documents input)
   :task-field-operation {:path task-path :marker "domains-implementation-tasks" :id "schema"
                          :field :projection :operation :guarded-field-edit
                          :predecessor-ref predecessor :predecessor-source-sha256 (sha256 before-source)
                          :before-present? true :before before :before-sha256 (sha256 before)
                          :after-present? true :after after :after-sha256 (sha256 after)
                          :other-task-records-and-statuses :preserve}
   :mapping-operations (mapv row-patch (:rows input))
   :counts {:assigned-historical-gaps 5 :constructed-finite-open-interface-mappings 5
            :new-task-count 0 :new-criterion-count 5 :independently-accepted-mappings 0
            :unresolved-interface-placement-ids [] :pending-independent-entailment-ids assigned-ids}
   :nonclaims [:no-implementation-acceptance :no-global-residual-decrement
               :no-retirement-or-witness-reclassification :no-shared-checkout-write
               :no-other-family-dispatch :no-release-authority]})
(defn apply-field-operation [record operation]
  (require! (and (= (:id record) (:id operation))
                (= (:before-present? operation) (contains? record (:field operation)))
                (= (:before operation) (get record (:field operation)))) "Stale or incompatible task field")
  (assoc record (:field operation) (:after operation)))
(defn apply-mapping-operation [row operation]
  (require! (and (= (:id row) (:id operation)) (= row (:before operation))
                (= (sha256 (pr-str row)) (:before-sha256 operation)))
            "Stale, forged or already-replaced coverage predecessor")
  (assoc (:after operation) :id (:id row)))
(defn validate! [p]
  (require! (= (proposal) p) "Lost, duplicated, forged or altered coverage/source/criterion")
  (let [op (:task-field-operation p)]
    (require! (= after (:projection (apply-field-operation {:id "schema" :projection before} op)))
              "Task field patch failed"))
  (doseq [op (:mapping-operations p)] (apply-mapping-operation (:before op) op))
  true)
(defn negative-controls! [p]
  (let [bad [(update p :mapping-operations pop)
             (update p :mapping-operations conj (first (:mapping-operations p)))
             (assoc-in p [:mapping-operations 0 :id] "another-family")
             (assoc-in p [:mapping-operations 0 :before-sha256] "forged")
             (assoc-in p [:mapping-operations 0 :after :coverage-disposition] :concrete-task-criterion-witness)
             (assoc-in p [:mapping-operations 0 :after :implementation-acceptance] :accepted)
             (update-in p [:mapping-operations 0 :after :source-facet-ids] pop)
             (assoc-in p [:mapping-operations 0 :after :facet-witnesses 0 :raw-text] "negative condition lost")
             (assoc-in p [:mapping-operations 0 :after :criterion-text] "A broad link is enough")
             (assoc-in p [:mapping-operations 0 :after :criterion-source :task-id] "renamed")
             (assoc-in p [:mapping-operations 0 :after :criterion-source :sha256] "wrong")
             (assoc-in p [:task-field-operation :before] "later source change")
             (assoc-in p [:counts :new-task-count] 5)
             (assoc-in p [:retention :criterion-witnesses] 0)]]
    (doseq [b bad] (require! (try (validate! b) false (catch clojure.lang.ExceptionInfo _ true))
                           "Negative coverage control accepted"))
    (let [op (:task-field-operation p) m (first (:mapping-operations p))]
      (doseq [f [#(apply-field-operation {:id "schema" :projection "later edit"} op)
                 #(apply-mapping-operation (assoc (:before m) :coverage-disposition :concrete-task-criterion-witness) m)
                 #(apply-mapping-operation (apply-mapping-operation (:before m) m) m)
                 #(schema-field (str/replace task-text "<!-- domains-implementation-tasks:start -->" ""))
                 #(schema-field (str/replace task-text "`schema` Schema" "`renamed` Schema"))
                 #(schema-field (str task-text "<!-- domains-implementation-tasks:end -->"))]]
        (require! (try (f) false (catch clojure.lang.ExceptionInfo _ true)) "Stale/duplicate operation accepted")))
    (+ 6 (count bad))))
(when (seq *command-line-args*)
  (let [args (vec *command-line-args*) p (proposal)]
    (require! (#{["--write"] ["--check"]} args) "Usage: --write|--check")
    (validate! p)
    (if (= ["--write"] args) (spit output-path (with-out-str (pp/pprint p)))
        (validate! (edn/read-string (slurp output-path))))
    (prn {:assigned-ids assigned-ids :constructed-open-interface-mappings 5
          :independently-accepted-mappings 0 :task-count-delta 0 :new-criteria 5
          :unique-preserved-facets (count (:source-facets input))
          :negative-controls (negative-controls! p) :sha256 (sha256 (slurp output-path))})
    (shutdown-agents)))
