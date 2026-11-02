(ns runtime-current-source-view
  "Authenticate PR561's historical Runtime proposal against current main.

  This checker deliberately does not regenerate the proposal or apply any of
  its edits.  The old allocation loader depends on source files that are no
  longer present on current main; the proposal remains immutable history and
  this current-source view is the bounded review surface."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def source-view-path
  "specs/requirement-reconciliation/runtime-current-source-view.edn")
(def task-marker "runtime-implementation-tasks")
(def expected-base
  {:commit "dbeda7c7dc9a7bb880baa90af1a89046828db327"
   :tree "576075197b5b4e815b78bae7d949bfb5fe2d0437"})
(def expected-task-ids
  ["runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
   "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
   "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
   "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
   "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"])
(def expected-current-task-source
  {:commit "42c34ab55af0000dfaaf73d96d7ee09ec22c243e"
   :tree "e8f1cc090800f9fcd9e81b7a5bcea32a88101526"
   :path "specs/runtime-workflow/tasks.md"
   :sha256 "b6157bb289fb1c731fc74158aa6fbd510f3f2e07614357abd34f23a3fc83d82c"
   :marker "runtime-implementation-tasks"})
(def expected-current-contracts
  [{:id :runtime-workflow-spec
    :path "specs/runtime-workflow/spec.md"
    :sha256 "9fbc0b7f482814f31ad58a102d7f6ce92c5aad6d66df7b497f6ac040c4209b22"}
   {:id :runtime-workflow-plan
    :path "specs/runtime-workflow/plan.md"
    :sha256 "feaaab16d4bec0a4c015816e560e503a0e50d6ef6f7ffbe5623d21484e1b2832"}
   {:id :phase-08-runtime-readme
    :path "docs/phase-08-runtime-architecture/README.md"
    :sha256 "339b1cf4dedbc7a6dade0c3ab971e1a2e169aa565cc3471f0d799439fa6666fe"}
   {:id :runtime-roadmap
    :path "docs/component-roadmaps/runtime.md"
    :sha256 "ac591226bd3c75ef9df212ee348d1d6746c3c0237c7956b8efd7339bfa8f0b9d"}])
(def expected-negative-controls
  {:status :must-reject-drift
   :order :must-reject-drift
   :id :must-reject-drift
   :tamper :must-reject-drift})
(def expected-historical-source
  {:proposal {:commit "88b877f8cf43909431604b7fc12d2cb05b950915"
              :tree "ad515c6001c162d72194521dcefbc0e7b0d6cd41"
              :path "specs/requirement-reconciliation/runtime-decision-allocation.edn"
              :sha256 "a3c4e4e0f506c11aabc5c0bc57df9f5a21b9a2c350a5955398fc9555fd4b1fb5"}
   :description {:commit "88b877f8cf43909431604b7fc12d2cb05b950915"
                 :tree "ad515c6001c162d72194521dcefbc0e7b0d6cd41"
                 :path "specs/requirement-reconciliation/runtime-decision-allocation.md"
                 :sha256 "0c58998a2d4dc27f0422c9964ae062a3113edee71a347697d957d298d3ee40f3"}
   :generator {:commit "88b877f8cf43909431604b7fc12d2cb05b950915"
               :tree "ad515c6001c162d72194521dcefbc0e7b0d6cd41"
               :path "tools/runtime_decision_allocation.clj"
               :sha256 "a44a93b4a15e3a3bfc24afad6869002f469c7250ba71b01fcfccf0a713b453bc"}
   :test {:commit "88b877f8cf43909431604b7fc12d2cb05b950915"
          :tree "ad515c6001c162d72194521dcefbc0e7b0d6cd41"
          :path "tools/runtime_decision_allocation_test.clj"
          :sha256 "e5d3a77f12a3b4709a5c438c316b2cbb67705cdf3a1e7480828455bbbf8d9a28"}
   :source-decision {:commit "86747916c4c876d707a1548113409fb939d854c9"
                     :tree "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b"
                     :path "specs/requirement-reconciliation/source-dispositions.edn"
                     :sha256 "448b46b360669a80328bd98abc24f39b39577091c81a396284c46bcaa65f13fe"}
   :task-source {:commit "86747916c4c876d707a1548113409fb939d854c9"
                 :tree "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b"
                 :path "specs/runtime-workflow/tasks.md"
                 :sha256 "da3305176c678e513b9c41a85138099f84d9464d4a37ce03e6ac14c2cb3693e9"}
   :plan-source {:commit "86747916c4c876d707a1548113409fb939d854c9"
                 :tree "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b"
                 :path "specs/runtime-workflow/plan.md"
                 :sha256 "6714be6a4bad82620ee0e3aec2d26db995e14e9ac4d05a9ea602d1b5af97063c"}
   :accepted-430 {:commit "86747916c4c876d707a1548113409fb939d854c9"
                 :tree "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b"
                 :path "specs/requirement-reconciliation/accepted-historical-mappings-430.edn"
                 :sha256 "996a2d56fe9f58a5e776e1c7f071d5e0b1fc289cd100f88b7cf16da96f0b6ff0"}})
(def expected-proposal-kind :runtime-current-decision-allocation-proposal)
(def expected-unresolved-ids
  #{"interface/runtime/durable-failure-recovery"
    "interface/runtime/forbidden-service-rejection"
    "interface/runtime/task-lifecycle"})

(defn require! [ok message]
  (when-not ok
    (throw (ex-info message {:diagnostic "RUNTIME-CURRENT-SOURCE-VIEW"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1
                       (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                (.getBytes ^String text "UTF-8")))))

(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable Runtime source: " ref ":" path))
    out))

(defn git-tree [commit]
  (let [{:keys [exit out]} (shell/sh "git" "rev-parse" "--verify"
                                     (str commit "^{tree}"))]
    (require! (zero? exit) (str "Missing Git tree for Runtime source: " commit))
    (str/trim out)))

(defn verify-identity! [{:keys [commit tree] :as identity} label]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit)
                 (string? tree) (re-matches #"[0-9a-f]{40}" tree))
            (str "Invalid " label " commit/tree identity"))
  (let [{:keys [exit out]} (shell/sh "git" "cat-file" "-t" commit)]
    (require! (and (zero? exit) (= "commit" (str/trim out)))
              (str "Missing " label " commit: " commit)))
  (require! (= tree (git-tree commit))
            (str label " commit/tree mismatch: " commit))
  identity)

(defn verify-artifact! [identity label]
  (verify-identity! identity label)
  (require! (= (:sha256 identity)
               (sha256 (git-text (:commit identity) (:path identity))))
            (str label " bytes drifted: " (:path identity)))
  identity)

(defn view []
  (edn/read-string (slurp source-view-path)))

(defn marker-body [text marker]
  (let [start-token (str "<!-- " marker ":start -->")
        end-token (str "<!-- " marker ":end -->")
        starts (str/split text (re-pattern (java.util.regex.Pattern/quote start-token)) -1)]
    (require! (= 2 (count starts)) "Runtime task marker is missing or duplicated")
    (let [ends (str/split (second starts)
                          (re-pattern (java.util.regex.Pattern/quote end-token)) -1)]
      (require! (= 2 (count ends)) "Runtime task end marker is missing or duplicated")
      (first ends))))

(defn task-rows [text]
  (let [body (marker-body text task-marker)
        pattern #"(?ms)^- \[([ xX])\] `([^`]+)` ([^\n]+)\n(.*?)(?=^- \[[ xX]\] `|\z)"]
    (mapv (fn [[_ checkbox id title details]]
            {:checkbox checkbox
             :id id
             :title title
             :details details
             :status (second (re-find #"(?m)^Status: ([^\n]+)$" details))})
          (re-seq pattern body))))

(defn validate-task-source! [source-view]
  (let [reference (get-in source-view [:current-source :task-source])
        text (slurp (:path reference))
        rows (task-rows text)
        ids (mapv :id rows)
        statuses (mapv :status rows)
        expected-statuses (:statuses reference)]
    (require! (= expected-current-task-source
                 (select-keys reference [:commit :tree :path :sha256 :marker]))
              "Current Runtime task source identity changed")
    (verify-identity! (select-keys reference [:commit :tree])
                      "current Runtime task source")
    (require! (= text (git-text (:commit reference) (:path reference)))
              "Working Runtime task source differs from authenticated current source")
    (require! (= (:sha256 reference) (sha256 text))
              "Current Runtime task source bytes drifted")
    (require! (= expected-task-ids (:ordered-task-ids reference))
              "Current Runtime task identity view changed")
    (require! (= expected-task-ids ids)
              "Current Runtime task order or identity changed")
    (require! (= (count ids) (count (set ids)))
              "Current Runtime task IDs are duplicated")
    (require! (= expected-statuses statuses)
              "Current Runtime task status changed")
    (require! (every? #(= "Open" %) statuses)
              "Current Runtime task is not Open")
    (require! (every? #(= " " (:checkbox %)) rows)
              "Current Runtime task checkbox changed")
    {:path (:path reference)
     :rows rows
     :ids ids
     :statuses statuses}))

(defn validate-contracts! [source-view]
  (require! (= expected-current-contracts
               (vec (get-in source-view [:current-source :contracts])))
            "Current Runtime contract identity set changed")
  (doseq [{:keys [path id] expected-sha :sha256}
          (get-in source-view [:current-source :contracts])]
    (require! (and (string? path) (not (str/starts-with? path "/")))
              (str "Runtime contract path is not repository-relative: " id))
    (require! (= expected-sha (sha256 (slurp path)))
              (str "Current Runtime contract bytes drifted: " id)))
  true)

(defn validate-proposal-shape! [source-view proposal]
  (let [shape (:proposal-shape source-view)
        mappings (:mappings proposal)
        edits (:decision-field-edits proposal)
        task-edits (:task-criterion-edits proposal)
        mapping-ids (mapv :id mappings)
        mapping-id-set (set mapping-ids)
        task-ids (set (map #(get-in % [:task :id]) mappings))]
    (require! (= expected-proposal-kind (:kind proposal))
              "Historical Runtime proposal kind changed")
    (require! (= "86747916c4c876d707a1548113409fb939d854c9" (:base proposal))
              "Historical Runtime proposal base changed")
    (require! (= (:assigned shape) (count mappings) (count edits))
              "Runtime proposal assigned population changed")
    (require! (= (:new-tasks shape) (get-in proposal [:preservation :new-tasks]) 0)
              "Runtime proposal introduced task population")
    (require! (= (:accepted-allocations shape)
                 (get-in proposal [:preservation :accepted-change])
                 (get-in proposal [:counts :accepted-allocations])
                 0)
              "Runtime proposal accepted an allocation")
    (require! (= (:accepted-historical-mappings shape)
                 (get-in proposal [:preservation :accepted-historical-mappings])
                 430)
              "Runtime historical acceptance population changed")
    (require! (= (:decision-records shape) (get-in proposal [:preservation :decisions]) 833)
              "Runtime decision denominator changed")
    (require! (= (:proposed-existing-criterion-witnesses shape)
                 (get-in proposal [:counts :proposed-existing-criterion-witnesses]) 6)
              "Runtime witness count changed")
    (require! (= (:proposed-missing-criteria shape)
                 (get-in proposal [:counts :proposed-missing-criteria]) 11)
              "Runtime criterion count changed")
    (require! (= (:source-metadata-adjudications shape)
                 (get-in proposal [:counts :source-metadata-adjudications]) 3)
              "Runtime metadata-adjudication count changed")
    (require! (= expected-unresolved-ids (set (:unresolved-ids proposal)))
              "Runtime unresolved metadata identities changed")
    (require! (= 20 (count mapping-id-set))
              "Runtime mapping IDs are missing or duplicated")
    (require! (= mapping-id-set (set (map :id edits)))
              "Runtime field-edit identities do not match mappings")
    (require! (every? #{:open} (map #(get-in % [:task :status]) mappings))
              "Runtime proposal changes task status")
    (require! (every? #(= :unassessed (:implementation-acceptance %)) mappings)
              "Runtime proposal confers implementation acceptance")
    (require! (every? #(= :pending-independent-review (:canonical-adoption %)) mappings)
              "Runtime proposal confers canonical adoption")
    (require! (= (set expected-task-ids) task-ids)
              "Runtime proposal task interfaces do not cover the current five task IDs")
    (require! (= 3 (count task-edits))
              "Runtime task criterion edit population changed")
    (require! (every? #(and (= "specs/runtime-workflow/tasks.md" (:path %))
                            (= task-marker (:marker %))
                            (= [:acceptance] (:field-path %))
                            (= :canonical-tasks (:collection %)))
                    task-edits)
              "Runtime task edit escapes the authenticated task interface")
    (require! (every? #(and (= :remaining-decisions (:collection %))
                            (= [:criterion-task-allocation] (:field-path %))
                            (false? (:before-present? %))
                            (contains? mapping-id-set (:id %)))
                    edits)
              "Runtime decision edit is stale or outside the bounded field")
    (require! (every? #(and (= :unassessed (get-in % [:after :implementation-acceptance]))
                            (= :pending-independent-review (get-in % [:after :canonical-adoption])))
                    edits)
              "Runtime decision edit weakens the acceptance boundary")
    {:mappings (count mappings)
     :decision-field-edits (count edits)
     :task-criterion-edits (count task-edits)
     :task-ids (vec (sort task-ids))
     :unresolved-ids (vec (sort (:unresolved-ids proposal)))
     :authority :proposal-only}))

(defn validate-historical-source! [source-view]
  (let [historical (:historical-source source-view)
        proposal-ref (:proposal historical)
        old-base (:commit (:task-source historical))
        proposal (edn/read-string (git-text (:commit proposal-ref) (:path proposal-ref)))]
    (doseq [[label identity]
            [[:proposal (:proposal historical)]
             [:description (:description historical)]
             [:generator (:generator historical)]
             [:test (:test historical)]
             [:source-decision (:source-decision historical)]
             [:task-source (:task-source historical)]
             [:plan-source (:plan-source historical)]
             [:accepted-430 (:accepted-430 historical)]]]
      (verify-artifact! identity (name label)))
    (require! (= (:sha256 (:source-decision historical))
                 (:source-decision-file-sha256 proposal))
              "Historical Runtime decision source identity changed")
    (require! (= (:sha256 (:task-source historical))
                 (get-in proposal [:task-source :sha256]))
              "Historical Runtime task source identity changed")
    (require! (= (:sha256 (:accepted-430 historical))
                 (get-in proposal [:accepted430-input :sha256]))
              "Historical accepted-430 identity changed")
    (require! (= old-base (:base proposal))
              "Historical Runtime proposal source base changed")
    (require! (= {:source "8b4543c35a1c267722428691ef7da62c5feb9d87"
                  :sha256 "179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74"}
                 (:partition historical))
              "Runtime partition identity is not stable")
    ;; Every source document was authored against the frozen source snapshot.
    ;; Verify those bytes from Git rather than accepting a refreshed live hash.
    (doseq [{:keys [path original-sha256 current-sha256 id]} (:source-documents proposal)]
      (let [source (git-text old-base path)
            actual (sha256 source)]
        (require! (= original-sha256 current-sha256 actual)
                  (str "Historical Runtime source document drifted: " id))))
    (validate-proposal-shape! source-view proposal)))

(defn validate-view! [source-view]
  (require! (= :gravity/noncanonical-runtime-current-source-view (:kind source-view))
            "Wrong Runtime current-source-view kind")
  (require! (= 1 (:version source-view)) "Unsupported Runtime source-view version")
  (require! (= :proposal-only (:authority source-view))
            "Runtime current-source-view is not proposal-only")
  (require! (= "requirement-reconciliation/phase-08-runtime-architecture"
              (:scope-key source-view))
            "Wrong Runtime current-source-view scope")
  (require! (= expected-base (:base source-view))
            "Runtime current-source-view base identity changed")
  (require! (= expected-historical-source
               (select-keys (:historical-source source-view)
                            (keys expected-historical-source)))
            "Historical Runtime source identity changed")
  (require! (= expected-negative-controls (:negative-controls source-view))
            "Runtime negative-control contract changed")
  (verify-identity! (:base source-view) "current Runtime base")
  (validate-contracts! source-view)
  (let [tasks (validate-task-source! source-view)
        proposal-check (validate-historical-source! source-view)]
    (require! (= :removed-as-admission-requirement
                 (get-in source-view [:semantic-boundary :project-accounting]))
              "Runtime source-view reintroduces project accounting")
    (require! (= :unassessed
                 (get-in source-view [:qualification-policy :implementation-acceptance]))
              "Runtime source-view claims implementation acceptance")
    (require! (= :pending-independent-review
                 (get-in source-view [:qualification-policy :canonical-adoption]))
              "Runtime source-view claims canonical adoption")
    (require! (= :source-qualified-open
                 (get-in source-view [:qualification-policy :current-task-acceptance]))
               "Runtime source-view does not retain the source-qualified Open task status")
    (require! (every? #(and (keyword? %) (not= :accepted %)) (:nonclaims source-view))
              "Runtime source-view nonclaims are missing or assert acceptance")
    {:kind (:kind source-view)
     :base (:base source-view)
     :task-count (count (:rows tasks))
     :task-ids (:ids tasks)
     :task-statuses (:statuses tasks)
     :proposal proposal-check
     :authority (:authority source-view)
     :implementation-acceptance :unassessed
     :current-task-acceptance :source-qualified-open
     :canonical-adoption :pending-independent-review
     :nonclaims (:nonclaims source-view)}))

(defn refuses? [f]
  (try
    (f)
    false
    (catch clojure.lang.ExceptionInfo _ true)))

(defn negative-controls [source-view]
  (let [mutations
        {:status (assoc-in source-view
                           [:current-source :task-source :statuses 0] "Closed")
         :order (update-in source-view
                           [:current-source :task-source :ordered-task-ids]
                           #(vec (reverse %)))
         :id (assoc-in source-view
                       [:current-source :task-source :ordered-task-ids 0]
                       "runtime/renamed-task")
         :tamper (assoc-in source-view
                           [:historical-source :proposal :sha256]
                           (apply str (repeat 64 "0")))}]
    (into {} (map (fn [[control mutated]] [control (refuses? #(validate-view! mutated))])
                  mutations))))

(when (= ["--check"] (vec *command-line-args*))
  (prn (validate-view! (view)))
  (shutdown-agents))
