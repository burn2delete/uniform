(ns accounting-gap-completion
  "Guard eight accounting-source criterion witnesses without executing their acceptance paths."
  (:require [clojure.edn :as edn] [clojure.java.shell :as shell]
            [clojure.string :as str] [clojure.pprint :as pp] [clojure.java.io :as io]))

(def directory "specs/requirement-reconciliation/accounting-gap-completion/")
(def task-path "specs/accounting-removal/tasks.md")
(def audit-sha "a7e82612a715713799be4db46cc3e911aff0cb5a2525d4555c8f8e6ded87101f")
(def partition-sha "f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a")
(def original-sha "f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690")
(def task-before-sha "f26670d438f5f4d57a1aab5ba6d5382d573c1f30684eed33042e091c210f7d51")
(def task-after-sha "e0ce960cf0ab30c25f1698f01f7ae28e7a23c331f52aecde91b11cba8abe9663")
(def routes
  {"policy/accounting/source-grounded-inventory" ["ACC-01" "accounting-source-inventory"]
   "policy/accounting/remove-artificial-refusals" ["ACC-12" "accounting-artificial-refusal-removal"]
   "policy/accounting/preserve-semantic-validation" ["ACC-12" "accounting-semantic-validation-preservation"]
   "policy/accounting/host-external-boundary-separation" ["ACC-12" "accounting-host-external-boundaries"]
   "policy/accounting/beyond-limit-and-late-invalid-evidence" ["ACC-12" "accounting-beyond-limit-late-invalid"]
   "policy/accounting/closure-evidence-record" ["ACC-12" "accounting-closure-evidence"]
   "policy/development/autonomy-and-conflict-coordination" ["ACC-12" "development-autonomy-conflict-coordination"]
   "policy/development/exact-candidate-admission" ["ACC-12" "development-exact-candidate-admission"]})
(defn ck [p message] (when-not p (throw (ex-info message {}))))
(defn sha [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String s "UTF-8")))))
(defn pinned-text [s hash] (ck (= hash (sha s)) "Wrong immutable input bytes") s)
(defn pinned [path hash] (edn/read-string (pinned-text (slurp path) hash)))
(defn git-source [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (ck (zero? exit) "Missing immutable Git source") out))
(defn slice [s {:keys [start-byte end-byte]}]
  (String. (.getBytes ^String s "UTF-8") (int start-byte) (int (- end-byte start-byte)) "UTF-8"))
(defn criterion [text id task criterion-id]
  (let [pattern (re-pattern (str "(?s)### " criterion-id "\n.*?(?=\n### |\n<!-- accounting-source-criteria:end -->)"))
        matches (re-seq pattern text) raw (first matches)]
    (ck (= 1 (count matches)) "Missing or duplicate criterion")
    (ck (and (str/includes? raw (str "\nTask: " task "\n"))
             (str/includes? raw (str "\nSource requirement: " id "\n"))
             (every? #(str/includes? raw %) ["\nAcceptance: " "\nNegative evidence: " "\nOutputs: " "\nDependencies: "]))
        "Wrong task, source or missing acceptance/negative/output/dependency")
    (ck (= 1 (count (re-seq (re-pattern (str "(?m)^- \\[ \\] " task " .+$")) text)))
        "Task missing, renamed or no longer unchecked")
    (let [start (.indexOf text raw)]
      {:path task-path :sha256 task-after-sha :marker "accounting-source-criteria"
       :task-id task :criterion-id criterion-id :start-byte (alength (.getBytes (subs text 0 start) "UTF-8"))
       :end-byte (alength (.getBytes (subs text 0 (+ start (count raw))) "UTF-8"))
       :raw-sha256 (sha raw) :raw raw})))
(defn task-edit [before after]
  (pinned-text before task-before-sha)
  (pinned-text after task-after-sha)
  (let [section (re-find #"(?s)## Source-bound accounting and development criteria\n.*?<!-- accounting-source-criteria:end -->\n\n" after)]
    (ck (and section (= before (str/replace-first after section ""))) "Changed task statuses, rows or historical suffix")
    {:path task-path :before-sha256 task-before-sha :after-sha256 task-after-sha
     :insert-before "## ACC-05 shared PL helper successor - 2026-10-28 UTC\n"
     :insert-text section}))
(defn apply-task-edit [text e]
  (ck (= (:before-sha256 e) (sha text)) "Stale or duplicate task-source application")
  (ck (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote (:insert-before e))) text))) "Changed task insertion point")
  (let [after (str/replace-first text (:insert-before e) (str (:insert-text e) (:insert-before e)))]
    (ck (= (:after-sha256 e) (sha after)) "Forged task-source edit") after))
(def allowed-fields #{[:coverage-disposition] [:criterion-source] [:criterion-text] [:criterion-task-id] [:qualification]})
(defn apply-field [rows {:keys [collection id field-path before-present? before after]}]
  (ck (and (= :historical-gap-traceability collection) (contains? routes id) (allowed-fields field-path)) "Unassigned semantic/history/status edit")
  (let [i (.indexOf (mapv :id rows) id) _ (ck (<= 0 i) "Missing historical ID")
        old (get-in (nth rows i) field-path ::absent)]
    (ck (and (= before-present? (not= ::absent old)) (= before (if (= ::absent old) nil old)))
        "Stale or duplicate projection application")
    (assoc-in rows (into [i] field-path) after)))
(defn source-row [original id]
  (first (filter #(= id (:id %)) (:requirements original))))
(defn make-proposal [audit partition original before after]
  (ck (= 577 (count (:historical-gap-traceability audit))) "Lost original history")
  (ck (= {:historical 577 :criterion-witnesses 13 :retired-history 3 :unverified-residual 561} (:counts partition)) "Changed partition counts")
  (let [parts (filterv #(#{"accounting-policy" "accounting-removal"} (:family %)) (:partitions partition))
        ids (set (mapcat :ids parts))
        _ (ck (and (= [6 2] (mapv :count parts)) (= ids (set (keys routes)))) "Changed exact assignment")
        ri (into {} (map (juxt :id identity) (:residual-records partition)))
        hi (into {} (map (juxt :id identity) (:historical-gap-traceability audit)))
        fi (into {} (map (juxt :id identity) (:source-facets original)))
        sources (filterv #(#{"accounting-policy" "accounting-removal"} (:id %)) (:extension-sources original))
        _ (ck (= 2 (count sources)) "Missing source extension")
        _ (doseq [s sources] (pinned-text (slurp (:path s)) (:sha256 s)))
        witnesses
        (mapv (fn [id]
                (let [[task criterion-id] (routes id) r (ri id) h (hi id) old (source-row original id)
                      fs (mapv fi (:source-facet-ids old)) c (criterion after id task criterion-id)]
                  (ck (= (:historical-record-sha256 r) (sha (pr-str old))) "Changed historical semantic record")
                  (ck (= (:source-facet-ids r) (:source-facet-ids old) (:source-facet-ids h)) "Lost original facet membership")
                  (ck (#{:explicit-open-source-interface-work :no-same-id-open-row-and-no-full-coverage-witness} (:coverage-disposition h)) "Already witnessed or retired ID")
                  (doseq [f fs]
                    (let [s (first (filter #(= (:document-id f) (:id %)) sources))]
                      (ck (and s (= (:raw-sha256 f) (sha (slice (slurp (:path s)) f)))) "Wrong source context/span/hash")))
                  {:id id :historical-before h :historical-source-record old :source-facets fs
                   :affected-source-families (:affected-source-families r)
                   :criterion-witness c :disposition :proposed-concrete-task-criterion-witness
                   :implementation-acceptance :unchanged-not-inferred
                   :whole-shared-facet-acceptance :not-conferred
                   :qualification "The full assigned obligation has explicit criteria, negatives, outputs and dependencies. Shared source facets retain all their original memberships; this is not acceptance of every co-located requirement."})) (sort ids))
        fields
        (vec (mapcat (fn [{:keys [id historical-before criterion-witness]}]
                       (let [changes {:coverage-disposition :concrete-task-criterion-witness
                                      :criterion-source (dissoc criterion-witness :raw)
                                      :criterion-text (:raw criterion-witness)
                                      :criterion-task-id (:task-id criterion-witness)
                                      :qualification "Proposed full assigned-obligation criterion witness in the existing ACC ledger; independent source review/adoption pending. No implementation or whole-shared-facet acceptance."}]
                         (for [field [:coverage-disposition :criterion-source :criterion-text :criterion-task-id :qualification]]
                           {:collection :historical-gap-traceability :id id :field-path [field]
                            :before-present? (contains? historical-before field) :before (get historical-before field)
                            :after (get changes field)}))) witnesses))]
    {:kind :accounting-historical-gap-criterion-completion-proposal :schema-version 1
     :authority :guarded-projection-and-task-interface-proposal-only
     :partition-input {:ref "bd37ab50c50d27094dce664b34831fcc43c3e9c3" :sha256 partition-sha :assignments parts}
     :coverage-input {:sha256 audit-sha :historical-records 577 :existing-witnesses 13 :existing-retirements 3}
     :original-manifest {:ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30" :sha256 original-sha}
     :source-documents sources :assigned-ids (vec (sort ids)) :witnesses witnesses
     :task-source-edit (task-edit before after) :field-edits fields
     :counts {:assigned 8 :concrete-mappings-proposed 8 :independently-accepted-mappings 0
              :unresolved-assigned 0 :new-task-count 0 :new-criterion-count 8 :existing-task-ids ["ACC-01" "ACC-12"]
              :existing-acc-task-count 13 :component-census-registration :canonical-owner-determines}
     :unresolved-ids [] :task-count-delta 0
     :preservation [:all-577-original-histories :thirteen-existing-witnesses :three-retirements
                    :all-240-source-contracts :existing-task-statuses :ACC05-cross-family-amendments]
     :projection-qualification "Field operations amend only a derived current coverage projection after independent adoption. The original review receipt and its historical 577 rows remain immutable; this does not mutate canonical source-dispositions or task-baseline."
     :review :pending-canonical-owner-routing :implementation-acceptance :not-conferred}))
(defn validate! [p expected audit]
  (ck (= expected p) "Forged coverage/source/criterion/assignment or predecessor declaration")
  (let [before (git-source "43598c5b87feb4d88928549bb20fc4089768718b" task-path)
        after (slurp task-path)
        _ (ck (= after (apply-task-edit before (:task-source-edit p))) "Task patch differs")
        rows (:historical-gap-traceability audit)
        applied (reduce apply-field rows (:field-edits p))
        allowed (set (keys routes))]
    (ck (= 577 (count applied)) "Lost historical row")
    (ck (= (filterv #(not (allowed (:id %))) rows) (filterv #(not (allowed (:id %))) applied)) "Changed other family or terminal history")
    (doseq [[b a] (map vector rows applied)]
      (ck (= (dissoc b :coverage-disposition :criterion-source :criterion-text :criterion-task-id :qualification)
             (dissoc a :coverage-disposition :criterion-source :criterion-text :criterion-task-id :qualification))
          "Changed semantic membership or implementation status"))) true)
(defn rejected? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(let [[mode audit-path partition-path] *command-line-args*
      audit (pinned audit-path audit-sha) partition (pinned partition-path partition-sha)
      original (edn/read-string (pinned-text (git-source "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30" "contracts/requirement-reconciliation.edn") original-sha))
      before (git-source "43598c5b87feb4d88928549bb20fc4089768718b" task-path) after (slurp task-path)
      expected (make-proposal audit partition original before after)
      p (if (= mode "--build") expected (edn/read-string (slurp (str directory "proposal.edn"))))
      rows (:historical-gap-traceability audit)
      controls {:positive (validate! p expected audit)
                :stale-task (rejected? #(apply-task-edit (str before "\n") (:task-source-edit p)))
                :duplicate-task (rejected? #(apply-task-edit after (:task-source-edit p)))
                :duplicate-projection (rejected? #(reduce apply-field (reduce apply-field rows (:field-edits p)) (:field-edits p)))
                :forged-witness (rejected? #(validate! (assoc-in p [:witnesses 0 :criterion-witness :raw] "broad link only") expected audit))
                :wrong-source-hash (rejected? #(validate! (assoc-in p [:source-documents 0 :sha256] "wrong") expected audit))
                :lost-negative (rejected? #(validate! (assoc-in p [:field-edits 2 :after] "accept only") expected audit))
                :wrong-task-id (rejected? #(validate! (assoc-in p [:witnesses 0 :criterion-witness :task-id] "ACC-99") expected audit))
                :lost-context (rejected? #(validate! (update-in p [:witnesses 0 :source-facets] pop) expected audit))
                :unassigned-row (rejected? #(apply-field rows (assoc (first (:field-edits p)) :id "ai/profile-tool-contract")))
                :implementation-credit (rejected? #(validate! (assoc p :implementation-acceptance :accepted) expected audit))
                :stale-projection (let [op (first (:field-edits p)) i (.indexOf (mapv :id rows) (:id op))]
                                    (rejected? #(apply-field (assoc-in rows [i :coverage-disposition] :already-reviewed) op)))}]
  (ck (#{"--build" "--check"} mode) "Usage: accounting_gap_completion.clj --build|--check AUDIT PARTITION")
  (ck (every? true? (vals controls)) "Negative control failed")
  (when (= "--build" mode)
    (io/make-parents (str directory "proposal.edn"))
    (spit (str directory "proposal.edn") (with-out-str (pp/pprint p))))
  (prn {:controls controls :counts (:counts p) :unresolved-ids (:unresolved-ids p)
        :proposal-sha256 (sha (slurp (str directory "proposal.edn"))) :task-count-delta 0}))
(shutdown-agents)
