(ns safety-task-interfaces
  "Bounded source/task proposal construction and read-only conservation checks."
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [clojure.java.shell :as shell] [clojure.string :as str]
            [clojure.pprint :as pp]))

(def directory "specs/requirement-reconciliation/phase-02-safety/")
(def baseline-sha "aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980")
(def inputs-sha "423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94")
(binding [*command-line-args* nil] (load-file "tools/task_projection.clj"))
(defn require! [p message] (when-not p (throw (ex-info message {}))))
(defn sha256 [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String s "UTF-8")))))
(defn checked [path hash]
  (let [s (slurp path)] (require! (= hash (sha256 s)) "Pinned input changed") (edn/read-string s)))
(defn git-source [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable Git source: " ref ":" path)) out))

;; Evaluate only the three pure definitions from the required immutable API.
;; Its namespace and compiler/evidence loaders are deliberately not executed.
(def occurrence-api-source
  (git-source "c0dc5790197a1bbd3a8aa285e5458c272d877220" "tools/requirement_reconciliation.clj"))
(with-open [r (java.io.PushbackReader. (java.io.StringReader. occurrence-api-source))]
  (loop []
    (let [f (read {:eof ::eof} r)]
      (when-not (= ::eof f)
        (when (and (seq? f) (= 'defn (first f)) (#{'require! 'sha256 'source-occurrences} (second f)))
          (eval f))
        (recur)))))

(defn raw-slice [text {:keys [start-byte end-byte]}]
  (String. (.getBytes ^String text "UTF-8") (int start-byte) (int (- end-byte start-byte)) "UTF-8"))
(defn family-facets [m]
  (filterv #(re-matches #"SAFE[0-9]+" (:document-id % "")) (:source-facets m)))
(defn family-units [m]
  (let [ids (set (mapcat :unit-ids (family-facets m)))]
    (filterv #(ids (:id %)) (:requirements m))))
(defn family-dispositions [m occurrences]
  (let [ids (set (map :id occurrences))]
    (filterv #(ids (:occurrence-id %)) (:source-dispositions m))))
(defn task-ref [inputs component suffix]
  (let [row (first (filter #(and (= component (:component %))
                                (str/ends-with? (:id %) suffix)) (:canonical-task-inputs inputs)))]
    (require! row (str "Missing frozen task: " component "/" suffix))
    {:path (:path row) :marker (str component "-implementation-tasks") :id (:id row)
     :sha256 (sha256 (slurp (:path row)))}))
(defn refs-for [inputs r]
  (let [id (:id r) doc (second (str/split id #"/"))
        old (:task-reference r)
        legality (assoc (dissoc old :source-commit) :sha256 (sha256 (slurp (:path old))))
        auth #(task-ref inputs "auth" %)
        runtime #(task-ref inputs "runtime" %)
        build #(task-ref inputs "build" %)
        domain #(task-ref inputs "domains" %)
        refs
        (cond
          (= doc "safe10")
          [(auth "enforce-grant-intersection-attenuation-revocation-phase-and-lifetime")
           (auth "bind-compiler-decisions-to-runtimebuild-enforcement-hooks") legality]
          (= doc "safe13") [(domain "ai") (runtime "connect-distributedai-replay-and-interactive-execution")
                            (auth "bind-compiler-decisions-to-runtimebuild-enforcement-hooks") legality]
          (= doc "safe14")
          [(build (case (last (str/split id #"/"))
                    "build-authority" "execute-typed-graph-under-declared-build-effects"
                    "generated-provenance" "produce-and-verify-packageprovenancesigning-records"
                    "pinned-trust" "produce-and-verify-packageprovenancesigning-records"
                    "implement-projectlock-normalization-and-constraint-resolution"))
           (auth "bind-compiler-decisions-to-runtimebuild-enforcement-hooks") legality]
          (= doc "safe9") [(domain "math") legality]
          (= doc "safe12")
          [(task-ref inputs "expand"
             (case (last (str/split id #"/"))
               "phase-authority" "expand3-authorize-compile-time-effects"
               "expansion-checks" "expand5-integrate-generated-programs"
               "engine-equivalence" "expand5-integrate-generated-programs"
               "expand2-record-hygiene-and-safety")) legality]
          (= id "safety/safe8/workflow-replay")
          [(runtime "connect-distributedai-replay-and-interactive-execution") (domain "workflow") legality]
          (#{"safe2" "safe3" "safe4" "safe5" "safe7" "safe8"} doc)
          [legality (runtime "implement-memory-concurrency-and-typed-ffi-boundaries")]
          :else [legality])]
    (vec (distinct refs))))

(defn operation [r field after]
  {:collection :requirements :id (:id r) :field-path [field]
   :before-present? (contains? r field) :before (get r field) :after after})
(defn apply-operation [m {:keys [collection id field-path before-present? before after]}]
  (require! (and (= :requirements collection)
                 (#{[:task-reference] [:additional-task-interfaces] [:task-gap]} field-path))
            "Out-of-scope semantic or alias edit")
  (let [i (.indexOf (mapv :id (:requirements m)) id)
        _ (require! (<= 0 i) "Missing requirement")
        row (get-in m [:requirements i]) sentinel ::absent
        value (get-in row field-path sentinel)]
    (require! (and (= before-present? (not= sentinel value))
                   (= before (if (= sentinel value) nil value))) "Stale or duplicate field application")
    (assoc-in m (into [:requirements i] field-path) after)))

(defn build-proposal [m inputs]
  (let [docs (filterv #(= "phase-02" (:source-family %)) (:original-documents inputs))
        occurrences (vec (mapcat #(source-occurrences (:id %) (slurp (:path %))) docs))
        fs (family-facets m) units (family-units m)
        assessments
        (mapv (fn [r]
                (let [ids (set (:source-facet-ids r))
                      local (filterv #(ids (:id %)) fs)]
                  {:proposed-id (:id r) :obligation (:obligation r)
                   :acceptance-facets local :task-references (refs-for inputs r)
                   :dependencies {:source-contracts "Retain the Dependencies section in the pinned assigned document and all facet context."
                                  :handoff "Types/Effects/Auth facts feed compile-time Legality; generated forms pass Expand and ordinary checks; Runtime consumes checked facts; Build consumes verified package/proof metadata; SAFE16 consumes actual emitted and executed evidence."
                                  :qualification "Task-reference order is not scheduling, acceptance or readiness evidence. No whole-component completion cycle is introduced."}
                   :prior-task-gap (:task-gap r)
                   :task-gaps (cond-> ["Assigned-source task interface proposal awaits independent exact-source entailment review; implementation and evidence remain unassessed."]
                                (some #(not (contains? (set (map :id fs)) %)) (:source-facet-ids r))
                                (conj "Additional source contexts outside this assigned family remain preserved and require their owners' entailment review.")
                                (some :unresolved local)
                                (conj "Inherited SAFE5 lease-renewal or SAFE9 explicit-narrowing qualification remains; consume reviewed qualification lineage without deleting either source condition."))
                   :equivalence-proposals []
                   :preserved-external-unit-ids (:external-unit-ids r)
                   :historical-support (:historical-discovery-support r)
                   :atomicity (:atomicity r) :nonoverlap (:nonoverlap r)
                   :current-acceptance :unassessed})) units)
        operations
        (vec (mapcat (fn [r a]
                       (let [refs (:task-references a)
                             ;; Keep old supplementary contexts. Refresh only identical task identities.
                             same? #(= (select-keys %1 [:path :marker :id])
                                       (select-keys %2 [:path :marker :id]))
                             additional (mapv (fn [old] (or (first (filter #(same? old %) refs)) old))
                                              (:additional-task-interfaces r))
                             additional (vec (distinct (concat additional (rest refs))))]
                         (filterv #(not= (:before %) (:after %))
                                  [(operation r :task-reference (first refs))
                                   (operation r :additional-task-interfaces additional)
                                   (operation r :task-gap (:task-gaps a))]))) units assessments))]
    {:kind :gravity/noncanonical-requirement-proposal :version 1
     :scope-key "requirement-reconciliation/phase-02-safety"
     :authority :source-family-proposal-only
     :source-baseline (:source-commit inputs) :source-tree (:source-tree inputs)
     :manifest-input {:sha256 baseline-sha :kind :preserved-uncommitted-pr445-history}
     :committed-pr445-manifest-sha256 "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690"
     :frozen-inputs {:ref "53ae6a403b20d15155bb5b04911fe7ac0092b921" :sha256 inputs-sha}
     :occurrence-api {:ref "c0dc5790197a1bbd3a8aa285e5458c272d877220"
                      :path "tools/requirement_reconciliation.clj" :sha256 (sha256 occurrence-api-source)}
     :source-documents docs :source-occurrences occurrences :source-facets fs
     :source-dispositions (family-dispositions m occurrences)
     :units assessments :field-edits operations
     :retained-semantic-lineage
     [{:artifact "reviewed-safety-slices-composition.edn" :sha256 "c9ff30754dd62845e299d8aa6651f01baf64ad1135cf91ab41e14c876aeb80eb"}
      {:artifact "reviewed-stage-safety-composition.edn" :sha256 "c8aa2dde3d6c897f909602bec153b983667a2f208992fc9e28f4c3e433e803e1"}
      {:artifact "reviewed-foundation-stage-safety-prompt-composition.edn" :sha256 "c156dd647a1092a7a828dce4fd379e7c96cac393fafa68206ecf03d65f17257c"}]
     :scope-decisions ["No semantic/alias/atomicity/nonoverlap or source-disposition edits. Later D8/D9 safe API versus internal-only and reviewed Safety qualification/atomicity decisions remain central composition inputs."
                       "The 119 rows are inherited trace records touching this family, not a new task population or requirement denominator."
                       "Task references entail the assigned source-qualified compiler/service/domain acceptance interface only as a proposal. No check, source test or linked receipt establishes implementation acceptance."]
     :validation {:semantic-qualification :manual-full-family-source-and-task-review
                  :routing :explicit-document-and-behavior-case-table
                  :independent-exact-source-review :pending
                  :canonical-adoption :pending :current-acceptance :unassessed}}))

(defn validate! [m p]
  (let [input-text (git-source "53ae6a403b20d15155bb5b04911fe7ac0092b921"
                               "specs/requirement-reconciliation/source-task-inputs.edn")
        _ (require! (= inputs-sha (sha256 input-text)) "Wrong frozen input bytes")
        expected (build-proposal m (edn/read-string input-text))]
    (require! (= (:source-documents expected) (:source-documents p)) "Changed assigned source population")
    (require! (= (:units expected) (:units p)) "Changed source-qualified unit assessment")
    (require! (= (:field-edits expected) (:field-edits p)) "Changed field edits or supplementary contexts")
    (require! (= (:retained-semantic-lineage expected) (:retained-semantic-lineage p)) "Lost reviewed lineage"))
  (require! (= (family-facets m) (:source-facets p)) "Lost or changed source facet/negative condition")
  (require! (= (mapv :id (family-units m)) (mapv :proposed-id (:units p))) "Lost or invented unit")
  (require! (= 16 (count (:source-documents p))) "Wrong source family")
  (require! (= (sha256 occurrence-api-source) (get-in p [:occurrence-api :sha256])) "Wrong occurrence API")
  (doseq [d (:source-documents p)]
    (require! (= (:original-sha256 d) (:current-sha256 d) (sha256 (slurp (:path d)))) "Source hash changed"))
  (let [occurrences (vec (mapcat #(source-occurrences (:id %) (slurp (:path %))) (:source-documents p)))
        oi (into {} (map (juxt :id identity) occurrences))
        sources (into {} (map (juxt :id #(slurp (:path %))) (:source-documents p)))]
    (require! (= occurrences (:source-occurrences p)) "Occurrence identity or span changed")
    (require! (= (family-dispositions m occurrences) (:source-dispositions p)) "Lost source disposition")
    (require! (= (set (map :id (remove #(= :layout (:structural-kind %)) occurrences)))
                 (set (map :occurrence-id (:source-dispositions p))))
              "Missing nonlayout occurrence disposition")
    (require! (every? #(or (not= :acceptance-rule (:kind %)) (seq (:unit-ids %))) (:source-facets p))
              "Missing normative facet owner")
    (doseq [f (:source-facets p)]
      (let [o (oi (:source-occurrence-id f))]
        (require! (and o (<= (:start-byte o) (:start-byte f) (:end-byte f) (:end-byte o))
                       (= (:raw-sha256 f) (sha256 (raw-slice (sources (:document-id f)) f))))
                  "Wrong facet occurrence, raw bytes or hash"))))
  (doseq [[r u] (map vector (family-units m) (:units p))]
    (require! (and (= (:obligation r) (:obligation u))
                   (= (:external-unit-ids r) (:preserved-external-unit-ids u))
                   (= (filterv (comp (set (:source-facet-ids r)) :id) (family-facets m))
                      (:acceptance-facets u))
                   (empty? (:equivalence-proposals u))
                   (= :unassessed (:current-acceptance u))) "Changed semantic context or acceptance")
    (doseq [{:keys [path marker id sha256]} (:task-references u)]
      (let [s (slurp path)
            body (:body (task-projection/region s marker))]
        (require! (and (= sha256 (safety-task-interfaces/sha256 s))
                       (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (str "`" id "`"))) body))))
                  "Missing, renamed or wrong-hash task"))))
  (let [after (reduce apply-operation m (:field-edits p))
        allowed #{:task-reference :additional-task-interfaces :task-gap}]
    (require! (= (dissoc m :requirements) (dissoc after :requirements)) "Changed non-task collections")
    (require! (= (mapv #(apply dissoc % allowed) (:requirements m))
                 (mapv #(apply dissoc % allowed) (:requirements after))) "Changed semantic record fields")
    (doseq [[r u] (map vector (family-units after) (:units p))]
      (require! (= (:task-reference r) (first (:task-references u))) "Task proposal and patch disagree")))
  true)

(defn rejected? [f] (try (f) false (catch Exception _ true)))
(defn controls [m p]
  {:positive (validate! m p)
   :missing-task (rejected? #(validate! m (assoc-in p [:units 0 :task-references 0 :id] "missing-task")))
   :wrong-marker (rejected? #(validate! m (assoc-in p [:units 0 :task-references 0 :marker] "missing-marker")))
   :wrong-task-hash (rejected? #(validate! m (assoc-in p [:units 0 :task-references 0 :sha256] "wrong")))
   :wrong-source-hash (rejected? #(validate! m (assoc-in p [:source-documents 0 :current-sha256] "wrong")))
   :wrong-occurrence (rejected? #(validate! m (assoc-in p [:source-occurrences 0 :id] "invented")))
   :lost-negative-facet (rejected? #(validate! m (update p :source-facets pop)))
   :lost-unit (rejected? #(validate! m (update p :units pop)))
   :alias-with-different-context (rejected? #(validate! m (assoc-in p [:units 0 :equivalence-proposals]
                                                                [{:target "safety/safe13/tool-grants" :kind :alias}])))
   :duplicate-application (rejected? #(reduce apply-operation (reduce apply-operation m (:field-edits p)) (:field-edits p)))
   :stale-predecessor (let [op (first (:field-edits p))
                            i (.indexOf (mapv :id (:requirements m)) (:id op))]
                        (rejected? #(apply-operation (assoc-in m [:requirements i :task-reference] :changed) op)))})

(let [[mode manifest-path input-path] *command-line-args*
      m (checked manifest-path baseline-sha)
      p (if (= "--build" mode)
          (build-proposal m (checked input-path inputs-sha))
          (edn/read-string (slurp (str directory "proposal.edn"))))
      results (controls m p)]
  (require! (#{"--build" "--check"} mode) "Usage: check.clj --build MANIFEST INPUTS | --check MANIFEST")
  (require! (every? true? (vals results)) "A negative control failed")
  (when (= "--build" mode)
    (with-open [w (io/writer (str directory "proposal.edn"))]
      (binding [*out* w] (pp/pprint p))))
  (prn {:controls results :documents (count (:source-documents p))
        :occurrences (count (:source-occurrences p)) :facets (count (:source-facets p))
        :units (count (:units p)) :field-edits (count (:field-edits p))
        :task-interfaces (count (set (mapcat :task-references (:units p))))
        :inherited-unresolved-facet-ids (mapv :id (filter :unresolved (:source-facets p)))
        :proposal-sha256 (sha256 (slurp (str directory "proposal.edn")))
        :implementation-acceptance :unassessed :independent-review :pending}))
(shutdown-agents)
