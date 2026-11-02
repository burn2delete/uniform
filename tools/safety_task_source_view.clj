(ns safety-task-source-view
  "Validate the PR560 safety proposal against an explicit current-source view.

  The historical proposal remains immutable and proposal-only.  This checker
  binds its task references to the candidate source bytes, then separately
  authenticates the current-main composition and its task rows."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]))

(def view-path "specs/requirement-reconciliation/phase-02-safety/current-source-view.edn")
(def proposal-path "specs/requirement-reconciliation/phase-02-safety/proposal.edn")
(def zero-hex (apply str (repeat 64 "0")))

(defn require! [condition message]
  (when-not condition
    (throw (ex-info message {:diagnostic "SAFETY-TASK-SOURCE-VIEW"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1
                       (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                (.getBytes ^String text "UTF-8")))))

(defn git-text [commit path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str commit ":" path))]
    (require! (zero? exit) (str "Missing immutable source: " commit ":" path))
    out))

(defn git-tree [commit]
  (let [{:keys [exit out]} (shell/sh "git" "rev-parse" "--verify" (str commit "^{tree}"))]
    (require! (zero? exit) (str "Missing immutable commit: " commit))
    (str/trim out)))

(defn verify-commit-tree! [{:keys [commit tree]}]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit)
                 (string? tree) (re-matches #"[0-9a-f]{40}" tree))
            "Invalid pinned commit/tree")
  (require! (= tree (git-tree commit)) (str "Pinned commit/tree drift: " commit)))

(defn marker-body [text marker]
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")
        starts (str/split text (re-pattern (java.util.regex.Pattern/quote start)) -1)
        ends (str/split text (re-pattern (java.util.regex.Pattern/quote end)) -1)]
    (require! (= 2 (count starts)) (str "Missing or duplicate task start marker: " marker))
    (require! (= 2 (count ends)) (str "Missing or duplicate task end marker: " marker))
    (let [parts (str/split (second starts) (re-pattern (java.util.regex.Pattern/quote end)) -1)]
      (require! (= 2 (count parts)) (str "Reversed task markers: " marker))
      (first parts))))

(def task-heading-re #"(?m)^- \[([ x])\] `([^`]+)` ([^\n]+)")

(defn task-records [text marker]
  (let [blocks (->> (str/split (marker-body text marker) #"(?m)(?=^- \[[ x]\] `)")
                    (filter #(re-find task-heading-re %))
                    vec)]
    (require! (seq blocks) (str "No canonical task rows: " marker))
    (mapv (fn [block]
            (let [[_ checked id title] (re-find task-heading-re block)
                  statuses (re-seq #"(?m)^Status: ([^\n]+)$" block)]
              (require! (= 1 (count statuses))
                        (str "Task lacks a unique Status line: " id))
              {:id id
               :checked? (= "x" checked)
               :title title
               :status (second (first statuses))}))
          blocks)))

(defn validate-task-records! [text {:keys [path marker ordered-task-ids]}]
  (let [records (task-records text marker)
        ids (mapv :id records)]
    (require! (= ids ordered-task-ids)
              (str "Task order or population drift: " path
                   " actual=" (pr-str ids) " expected=" (pr-str ordered-task-ids)))
    (require! (= (count ids) (count (set ids)))
              (str "Duplicate task identity: " path))
    (doseq [{:keys [id checked? status]} records]
      (require! (false? checked?) (str "Task is closed or accepted: " path "#" id))
      (require! (str/starts-with? status "Open")
                (str "Task is not Open: " path "#" id)))
    records))

(defn task-source-entry! [commit entry]
  (let [text (git-text commit (:path entry))]
    (require! (= (:sha256 entry) (sha256 text))
              (str "Task source hash drift: " (:path entry)))
    (validate-task-records! text entry)))

(defn validate-versioned-task-source-transition! [view]
  (let [transitions (get-in view [:current-source :versioned-task-source-transitions])
        current-commit (get-in view [:current-source :commit])
        current-tree (get-in view [:current-source :tree])
        current-entries (into {} (map (juxt :path identity)
                                      (get-in view [:current-source :task-sources])))]
    (require! (= 1 (count transitions))
              "Safety current-source transition population changed")
    (doseq [{:keys [path from to changed-record-ids] :as transition} transitions]
      (let [entry (get current-entries path)]
        (require! entry (str "Safety transition has no current task source: " path))
        (require! (= "specs/runtime-workflow/tasks.md" path)
                  (str "Safety transition path is not the authenticated Runtime task source: " path))
        (require! (= "runtime-implementation-tasks" (:marker entry))
                  (str "Safety transition marker is not the authenticated Runtime task marker: " path))
        (verify-commit-tree! from)
        (verify-commit-tree! to)
        (require! (= (:commit to) current-commit)
                  (str "Safety transition target commit is not current source: " path))
        (require! (= (:tree to) current-tree)
                  (str "Safety transition target tree is not current source: " path))
        (require! (= (:sha256 to) (:sha256 entry))
                  (str "Safety transition target hash is not current entry: " path))
        (let [from-text (git-text (:commit from) path)
              to-text (git-text (:commit to) path)
              current-records (task-source-entry! (:commit to) entry)
              current-record-ids (mapv :id current-records)]
          (require! (= (:sha256 from) (sha256 from-text))
                    (str "Safety transition source bytes drifted: " path))
          (require! (= (:sha256 to) (sha256 to-text))
                    (str "Safety transition target bytes drifted: " path))
          (require! (not= from-text to-text)
                    (str "Safety transition does not identify a source change: " path))
          (require! (= to-text (slurp path))
                    (str "Safety transition target is not the working source: " path))
          (require! (= 5 (count current-record-ids))
                    (str "Safety Runtime current task row population changed: " path))
          (require! (= current-record-ids (vec (distinct current-record-ids)))
                    (str "Safety Runtime current task row identities are not unique: " path))
          (require! (= current-record-ids changed-record-ids)
                    (str "Safety Runtime transition IDs do not match authenticated current rows: " path)))))
    {:count (count transitions)
     :paths (mapv :path transitions)
     :historical-intermediate :authenticated
     :current-target :authenticated}))

(defn unique-by! [rows key message]
  (require! (= (count rows) (count (set (map key rows)))) message))

(def expected-policy
  {:historical-task-criteria :preserved-proposal-source-view-only
   :current-task-status :open-and-unassessed
   :current-task-acceptance :not-replaced
   :implementation-acceptance :unassessed
   :independent-review :pending})

(def expected-nonclaims
  #{:canonical-adoption :implementation-acceptance :global-nonoverlap
    :aggregate-adoption :denominator :release :seed-retirement
    :current-task-criteria :independent-review :integration})

(defn validate-view! [view proposal]
  (require! (= :gravity/noncanonical-safety-current-source-view (:kind view))
            "Wrong safety current-source-view kind")
  (require! (= 1 (:version view)) "Unsupported safety current-source-view version")
  (require! (= :proposal-only (:authority view))
            "Safety current-source-view is not proposal-only")
  (require! (= "requirement-reconciliation/phase-02-safety" (:scope-key view))
            "Wrong safety current-source-view scope")
  (require! (= expected-policy (:qualification-policy view))
            "Safety qualification policy changed")
  (let [nonclaims (set (:nonclaims view))]
    (require! (set/subset? expected-nonclaims nonclaims)
              "Safety source view lost a required nonclaim")
    (require! (not (contains? nonclaims :canonical-adoption-accepted))
              "Safety source view asserts canonical adoption"))
  (verify-commit-tree! (:base view))
  (let [historical (get-in view [:historical-source :task-sources])
        base (get-in view [:base-source :task-sources])
        current (get-in view [:current-source :task-sources])
        current-commit (get-in view [:current-source :commit])
        proposal-pin (get-in view [:historical-source :proposal])]
    (verify-commit-tree! proposal-pin)
    (verify-commit-tree! {:commit current-commit :tree (get-in view [:current-source :tree])})
    (validate-versioned-task-source-transition! view)
    (require! (= 6 (count historical) (count base) (count current))
              "Safety task source population changed")
    (unique-by! historical :path "Duplicate historical safety task source")
    (unique-by! base :path "Duplicate base safety task source")
    (unique-by! current :path "Duplicate current safety task source")
    (require! (= (set (map :path historical)) (set (map :path base))
                (set (map :path historical)) (set (map :path current)))
              "Safety task source path pairing changed")
    (doseq [entry historical]
      (task-source-entry! (:commit entry) entry))
    (doseq [entry base]
      (let [text (git-text (get-in view [:base :commit]) (:path entry))]
        (require! (= (:sha256 entry) (sha256 text))
                  (str "Base safety task source drift: " (:path entry)))))
    (doseq [entry current]
      (let [historical-entry (first (filter #(= (:path %) (:path entry)) historical))]
        (require! (= (:ordered-task-ids historical-entry) (:ordered-task-ids entry))
                  (str "Current task row order changed: " (:path entry)))
        (task-source-entry! current-commit entry))
      (let [working (slurp (:path entry))]
        (require! (= (:sha256 entry) (sha256 working))
                  (str "Working safety task source drift: " (:path entry)))))
    (require! (= (mapv :path historical) (mapv :path current))
              "Safety task source order changed")
    (require! (= (mapv :marker historical) (mapv :marker current))
              "Safety task marker pairing changed")
    (require! (= (get-in view [:historical-source :proposal :sha256])
                 (sha256 (git-text (:commit proposal-pin) (:path proposal-pin))))
              "Historical safety proposal bytes drifted")
    (require! (= (sha256 (slurp proposal-path))
                 (get-in view [:historical-source :proposal :sha256]))
              "Working safety proposal is not the pinned historical proposal")
    (require! (and (= (:source-baseline proposal) (get-in view [:base :commit]))
                   (= (:source-tree proposal) (get-in view [:base :tree])))
              "Safety proposal source baseline changed")
    (require! (= :source-family-proposal-only (:authority proposal))
              "Safety proposal authority changed")
    (require! (= :unassessed (get-in proposal [:validation :current-acceptance]))
              "Safety proposal confers current acceptance")
    (require! (= :pending (get-in proposal [:validation :canonical-adoption]))
              "Safety proposal confers canonical adoption")
    (let [refs (vec (mapcat :task-references (:units proposal)))
          by-path (into {} (map (juxt :path identity) historical))
          pairs (mapv (juxt :path :id) refs)]
      (require! (= 119 (count (:units proposal))) "Safety unit population changed")
      (require! (= 237 (count refs)) "Safety task-reference population changed")
      (require! (= 18 (count (set pairs))) "Safety task interface identity changed")
      (require! (= 329 (count (:field-edits proposal))) "Safety field-edit population changed")
      (doseq [{:keys [path marker id sha256]} refs]
        (let [entry (get by-path path)]
          (require! entry (str "Unpaired safety task source: " path))
          (require! (= marker (:marker entry)) (str "Safety task marker drift: " path))
          (require! (= sha256 (:sha256 entry)) (str "Safety proposal task hash is not historical: " path))
          (require! (some #{id} (:ordered-task-ids entry))
                    (str "Safety proposal task ID is not canonical: " path "#" id))))))
  (let [expected (:expected view)]
    (require! (and (= 16 (:source-documents expected))
                   (= 1908 (:source-occurrences expected))
                   (= 962 (:source-dispositions expected))
                   (= 1055 (:source-facets expected))
                   (= 119 (:inherited-unit-records expected))
                   (= 329 (:field-edits expected))
                   (= 237 (:task-references expected))
                   (= 18 (:unique-task-interfaces expected))
                   (= 6 (:task-source-files expected)))
              "Safety source-view conservation counts changed")
    (require! (= 16 (count (:source-documents proposal)))
              "Safety source-document count changed")
    (require! (= 1908 (count (:source-occurrences proposal)))
              "Safety source-occurrence count changed")
    (require! (= 962 (count (:source-dispositions proposal)))
              "Safety source-disposition count changed")
    (require! (= 1055 (count (:source-facets proposal)))
              "Safety source-facet count changed"))
  true)

(defn rejected? [f]
  (try (f) false (catch Exception _ true)))

(defn controls [view proposal]
  (let [first-current (first (get-in view [:current-source :task-sources]))
        current-text (slurp (:path first-current))]
    {:positive (do (validate-view! view proposal) true)
     :proposal-hash-tamper
     (rejected? #(validate-view! (assoc-in view [:historical-source :proposal :sha256] zero-hex)
                                 proposal))
     :current-source-pair-tamper
     (rejected? #(validate-view! (assoc-in view [:current-source :task-sources 0 :sha256] zero-hex)
                                 proposal))
     :runtime-transition-forged-id-tamper
     (rejected? #(validate-view!
                  (update-in view [:current-source :versioned-task-source-transitions 0 :changed-record-ids]
                             assoc 0 "runtime/forged-current-row")
                  proposal))
     :runtime-transition-duplicate-id-tamper
     (rejected? #(validate-view!
                  (update-in view [:current-source :versioned-task-source-transitions 0 :changed-record-ids]
                             (fn [ids] (vec (repeat (count ids) (first ids)))))
                  proposal))
     :task-order-tamper
     (rejected? #(validate-view! (update-in view [:historical-source :task-sources 0 :ordered-task-ids]
                                            (fn [ids] (vec (reverse ids))))
                                 proposal))
     :duplicate-task-id-tamper
     (rejected? #(validate-task-records!
                  (str/replace-first current-text
                                     #"(?m)^- \[ \] `[^`]+`"
                                     "- [ ] `auth/duplicate-id`")
                  first-current))
     :closed-task-tamper
     (rejected? #(validate-task-records!
                  (str/replace-first current-text #"(?m)^Status: Open$" "Status: Closed")
                  first-current))
     :canonical-adoption-closure
     (rejected? #(validate-view! (assoc view :authority :canonical-adoption) proposal))
     :implementation-closure
     (rejected? #(validate-view! (assoc-in view [:qualification-policy :implementation-acceptance]
                                            :accepted)
                                 proposal))}))

(let [args (vec *command-line-args*)]
  (require! (= ["--check"] args) "Usage: clojure -M tools/safety_task_source_view.clj --check")
  (let [view (edn/read-string (slurp view-path))
        proposal (edn/read-string (slurp proposal-path))
        results (controls view proposal)]
    (require! (every? true? (vals results)) "A safety source-view control failed")
    (prn {:controls results
          :scope (:scope-key view)
          :historical-task-source-files (count (get-in view [:historical-source :task-sources]))
          :current-task-source-files (count (get-in view [:current-source :task-sources]))
          :task-references (get-in view [:expected :task-references])
          :unique-task-interfaces (get-in view [:expected :unique-task-interfaces])
          :implementation-acceptance :unassessed
          :canonical-adoption :pending
          :independent-review :pending})))
