(ns domain-current-source-view
  "Validate PR562's historical domain proposal against the current ledgers.

  This is a read-only source view.  It authenticates the historical proposal
  and its source records, then separately authenticates the current task
  ledgers.  It deliberately does not run the retired projection scripts or
  confer implementation, denominator, product, or canonical-adoption credit."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]))

(def view-path "specs/requirement-reconciliation/domain-current-source-view.edn")
(def family-path "specs/requirement-reconciliation/domain-family-proposal.edn")
(def gap-path "specs/requirement-reconciliation/domain-gap-completion.edn")
(def published-commit "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(def published-path "contracts/requirement-reconciliation.edn")
(def zero-hex (apply str (repeat 64 "0")))

(defn require! [condition message]
  (when-not condition
    (throw (ex-info message {:diagnostic "DOMAIN-CURRENT-SOURCE-VIEW"}))))

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

(defn index! [rows key-fn message]
  (let [m (into {} (map (juxt key-fn identity) rows))]
    (require! (= (count rows) (count m)) message)
    m))

(defn byte-slice [text start end]
  (let [bytes (.getBytes ^String text "UTF-8")]
    (require! (<= 0 start end (alength bytes)) "Invalid source byte span")
    (String. (java.util.Arrays/copyOfRange bytes (int start) (int end)) "UTF-8")))

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
              {:id id :checked? (= "x" checked) :title title
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

(defn task-source! [commit entry]
  (let [text (git-text commit (:path entry))]
    (require! (= (:sha256 entry) (sha256 text))
              (str "Task source hash drift: " (:path entry)))
    (validate-task-records! text entry)))

(defn working-task-source! [entry]
  (let [text (slurp (:path entry))]
    (require! (= (:sha256 entry) (sha256 text))
              (str "Working task source drift: " (:path entry)))
    (validate-task-records! text entry)))

(defn verify-source-documents! [family view]
  (let [docs (:source-documents family)
        by-id (index! docs :id "Duplicate domain source document")
        base (get-in view [:base :commit])]
    (require! (= 21 (count docs)) "Domain source-document count changed")
    (require! (= (set (keys by-id)) (set (map #(str "DOM" %) (range 1 22))))
              "Domain source-document identity changed")
    (require! (= (:source-commit family) base) "Domain source baseline changed")
    (require! (= (:source-tree family) (get-in view [:base :tree]))
              "Domain source tree changed")
    (require! (= (:original-source-commit family)
                 (get-in view [:historical-source :source-documents :original-source-commit]))
              "Domain original source baseline changed")
    (doseq [[id doc] by-id]
      (require! (= "phase-09" (:source-family doc))
                (str "Wrong source family: " id))
      (let [original (git-text (:original-source-commit family) (:path doc))
            current (git-text (:source-commit family) (:path doc))]
        (require! (= (:original-sha256 doc) (sha256 original))
                  (str "Original domain source hash drift: " id))
        (require! (= (:current-sha256 doc) (sha256 current) (sha256 (slurp (:path doc))))
                  (str "Current domain source hash drift: " id))))
    by-id))

(defn verify-occurrences-and-facets! [family docs]
  (let [occurrences (:source-occurrences family)
        dispositions (:source-dispositions family)
        facets (:source-facets family)
        occurrence-map (index! occurrences :id "Duplicate domain source occurrence")
        facet-map (index! facets :id "Duplicate domain source facet")
        disposition-map (index! dispositions :occurrence-id
                               "Duplicate domain source disposition")]
    (require! (= 1533 (count occurrences)) "Domain source-occurrence count changed")
    (require! (= 777 (count dispositions)) "Domain source-disposition count changed")
    (require! (= 1838 (count facets)) "Domain source-facet count changed")
    (doseq [[id doc] docs]
      (let [source (git-text (:source-commit family) (:path doc))
            os (sort-by :start-byte (filter #(= id (:document-id %)) occurrences))]
        (require! (seq os) (str "No occurrences for domain document: " id))
        (require! (= 0 (:start-byte (first os))) (str "Occurrence coverage does not start at zero: " id))
        (require! (= (alength (.getBytes ^String source "UTF-8")) (:end-byte (last os)))
                  (str "Occurrence coverage is incomplete: " id))
        (doseq [[left right] (partition 2 1 os)]
          (require! (= (:end-byte left) (:start-byte right))
                    (str "Occurrence gap or overlap: " id)))
        (doseq [o os]
          (let [raw (byte-slice source (:start-byte o) (:end-byte o))
                expected-id (str id "/source/"
                                 (sha256 (str id "\u0000" (:source-sha256 o)
                                              "\u0000" (:start-byte o) ":" (:end-byte o))))]
            (require! (= (:source-sha256 o) (:current-sha256 doc))
                      (str "Occurrence source hash drift: " (:id o)))
            (require! (= (:raw-sha256 o) (sha256 raw))
                      (str "Occurrence raw hash drift: " (:id o)))
            (require! (= (:id o) expected-id)
                      (str "Occurrence identity drift: " (:id o)))))
        (doseq [f (filter #(= id (:document-id %)) facets)]
          (let [occ (get occurrence-map (:source-occurrence-id f))]
            (require! occ (str "Facet references unknown occurrence: " (:id f)))
            (require! (= (:raw-sha256 f)
                         (sha256 (byte-slice source (:start-byte f) (:end-byte f))))
                      (str "Facet raw hash drift: " (:id f)))))))
    (require! (= (set (map :id (remove #(= :layout (:structural-kind %)) occurrences)))
                 (set (keys disposition-map)))
              "Non-layout source occurrence/disposition conservation changed")
    {:occurrences occurrence-map :facets facet-map :dispositions disposition-map}))

(defn validate-family-task-references! [family view]
  (let [entries (get-in view [:historical-source :family-task-sources])
        by-path (index! entries :path "Duplicate historical family task source")
        units (index! (:units family) :proposed-id "Duplicate domain proposal unit")
        edits (:edits family)
        refs (filter #(= [:task-reference] (:field-path %)) edits)
        ref-ids (set (map :id refs))]
    (require! (= 139 (count units)) "Domain family unit count changed")
    (require! (= 64 (count edits)) "Domain family field-edit count changed")
    (require! (= 32 (count refs)) "Domain family task-reference count changed")
    (require! (= (count edits)
                 (count (set (map (juxt :collection :id :field-path) edits))))
              "Duplicate domain family field operation")
    (doseq [e edits]
      (require! (= :requirements (:collection e)) "Domain family edit crossed collection boundary")
      (require! (#{[:task-reference] [:task-gap]} (:field-path e))
                "Domain family edit crossed field boundary")
      (require! (contains? units (:id e)) "Domain family edit outside assigned unit"))
    (doseq [{:keys [proposed-id current-acceptance nonoverlap equivalence-proposals
                    task-gaps acceptance-facets task-references original-record-sha256
                    historical-support]} (vals units)]
      (require! (= :unassessed current-acceptance) (str "Unit promoted acceptance: " proposed-id))
      (require! (= :unreviewed nonoverlap) (str "Unit promoted overlap status: " proposed-id))
      (require! (empty? equivalence-proposals) (str "Unit introduced equivalence: " proposed-id))
      (require! (seq task-gaps) (str "Unit lost task gap: " proposed-id))
      (require! (string? original-record-sha256) (str "Unit lost original record hash: " proposed-id))
      (require! (= historical-support (get-in units [proposed-id :historical-support]))
                (str "Unit history changed: " proposed-id))
      (when-let [e (first (filter #(and (= proposed-id (:id %))
                                         (= [:task-reference] (:field-path %))) edits))]
        (require! (= [(:after e)] task-references)
                  (str "Family unit/task-reference operation disagrees: " proposed-id))))
    (doseq [e refs]
      (let [r (:after e)
            entry (get by-path (:path r))]
        (require! entry (str "Unpaired family task source: " (:path r)))
        (require! (= (:marker entry) (:marker r)) (str "Family task marker drift: " (:path r)))
        (require! (= (:sha256 entry) (:sha256 r))
                  (str "Family task hash is not pinned historical source: " (:path r)))
        (require! (some #{(:id r)} (:ordered-task-ids entry))
                  (str "Family task ID is not canonical: " (:id r)))))
    (require! (= (set (:unresolved-stable-ids family))
                 (set/difference (set (keys units)) ref-ids))
              "Domain family residual stable IDs changed")
    true))

(defn validate-family! [family view]
  (require! (= :gravity/bounded-family-requirement-proposal (:kind family))
            "Wrong domain family proposal kind")
  (require! (= 1 (:version family)) "Unsupported domain family proposal version")
  (require! (= "requirement-reconciliation/phase-09-domain-specific-computing-coverage"
               (:scope-key family)) "Wrong domain family scope")
  (require! (= :proposal-only (:authority family)) "Domain family proposal gained authority")
  (require! (= :unassessed (:current-acceptance family))
            "Domain family proposal gained acceptance")
  (require! (= (:source-occurrence-api family)
               {:ref (get-in view [:historical-source :occurrence-api :commit])
                :path (get-in view [:historical-source :occurrence-api :path])
                :function "requirement-reconciliation/source-occurrences"
                :sha256 (get-in view [:historical-source :occurrence-api :sha256])})
            "Occurrence API provenance changed")
  (let [api (get-in view [:historical-source :occurrence-api])
        input (get-in view [:historical-source :input-snapshot])
        input-text (git-text (:commit input) (:path input))
        source-docs (verify-source-documents! family view)]
    (verify-commit-tree! {:commit (:commit api) :tree (git-tree (:commit api))})
    (require! (= (:sha256 api) (sha256 (git-text (:commit api) (:path api))))
              "Occurrence API source hash drift")
    (verify-commit-tree! {:commit (:commit input) :tree (git-tree (:commit input))})
    (require! (= (:sha256 input) (sha256 input-text)) "Frozen input snapshot drift")
    (let [inputs (edn/read-string input-text)]
      (require! (= (select-keys inputs [:source-commit :source-tree :original-source-commit])
                   (select-keys family [:source-commit :source-tree :original-source-commit]))
                "Family source tuple differs from frozen input snapshot"))
    (verify-occurrences-and-facets! family source-docs)
    (validate-family-task-references! family view)
    (require! (= (get-in family [:manifest :sha256])
                 (get-in view [:historical-manifest :family :sha256]))
              "Family manifest pin changed")
    (require! (= (get-in family [:committed-manifest :sha256])
                 (get-in view [:historical-manifest :gap-published :sha256]))
              "Family committed-manifest history changed")
    (require! (= (:preserved-applying-manifest-sha256 family)
                 (get-in view [:historical-manifest :preserved-pr445 :sha256]))
              "Preserved manifest identity changed")
    (require! (= 21 (count (:source-documents family)))
              "Domain family source-document count changed")
    (require! (= 1533 (count (:source-occurrences family)))
              "Domain family source-occurrence count changed")
    (require! (= 777 (count (:source-dispositions family)))
              "Domain family disposition count changed")
    (require! (= 1838 (count (:source-facets family)))
              "Domain family facet count changed")
    (require! (= {:source-conservation :pending
                  :semantic-qualification
                  "Source-family task-interface review only; inherited semantic split, equivalence and unresolved specialized interfaces require independent adjudication. No implementation execution or acceptance."
                  :authority "Canonical owner alone adopts; no global count, denominator or rate."}
                 (:validation family))
              "Family validation qualification changed")
    true))

(defn published-manifest []
  (edn/read-string (git-text published-commit published-path)))

(defn validate-gap-record! [record requirements facets documents task-ids]
  (let [id (:historical-id record)
        before (:before record)
        after (:after record)
        original (get requirements id)]
    (require! original (str "Gap record has no canonical requirement: " id))
    (require! (= (:before-sha256 record) (sha256 (pr-str before)))
              (str "Gap before record hash drift: " id))
    (require! (= (:historical-record-sha256 record) (sha256 (pr-str original)))
              (str "Gap historical requirement hash drift: " id))
    (require! (= (:historical-obligation record) (:obligation original))
              (str "Gap historical obligation changed: " id))
    (require! (= (:source-facet-ids before) (mapv :id (:source-evidence record))
                 (:source-facet-ids after))
              (str "Gap source facets changed: " id))
    (doseq [f (:source-evidence record)]
      (let [old (get facets (:id f))
            doc (get documents (:document-id old))]
        (require! old (str "Gap source evidence has no canonical facet: " (:id f)))
        (require! doc (str "Gap source evidence has no canonical document: " (:id f)))
        (require! (= [(:path f) (:original-source-sha256 f)]
                     [(:path doc) (:sha256 doc)])
                  (str "Gap source path/hash drift: " (:id f)))
        (require! (= (select-keys old [:id :document-id :source-occurrence-id
                                       :start-byte :end-byte :start-line :end-line
                                       :raw-sha256 :kind :unresolved :unit-ids])
                     (select-keys f [:id :document-id :source-occurrence-id
                                     :start-byte :end-byte :start-line :end-line
                                     :raw-sha256 :kind :unresolved :unit-ids]))
                  (str "Gap source facet qualification drift: " (:id f)))))
    (require! (and (= :open (:status record)) (= :not-yet-produced (:evidence record))
                   (= :finite-actionable-task-interface-proposed (:coverage-disposition after))
                   (= :unassessed (:implementation-acceptance after))
                   (= :pending-independent-source-review (:whole-facet-entailment-acceptance after)))
              (str "Gap record gained acceptance: " id))
    (require! (= (:criterion-sha256 record) (sha256 (:criterion-text record))
                 (:criterion-sha256 after))
              (str "Gap criterion hash drift: " id))
    (require! (= (:criterion-text record) (:criterion-text after))
              (str "Gap criterion text drift: " id))
    (require! (= [(:criterion-id after) (:criterion-task-id after)]
                 [(str "acceptance/" id) (get-in record [:task-reference :id])])
              (str "Gap criterion/task binding drift: " id))
    (doseq [field [:positive-fixture :negative-fixture :required-output]]
      (require! (and (seq (get record field))
                     (str/includes? (:criterion-text record) (get record field)))
                (str "Gap criterion lost " (name field) ": " id)))
    (require! (and (seq (:dependencies record))
                   (every? #(str/includes? (:criterion-text record) %) (:dependencies record)))
              (str "Gap criterion lost dependency: " id))
    (require! (task-ids (get-in record [:task-reference :id]))
              (str "Gap criterion task is not an amendment/proposal: " id))
    true))

(defn validate-gap-tasks! [gap view]
  (let [predecessors (get-in view [:historical-source :gap-task-predecessors])
        predecessor-by-path (index! predecessors :path "Duplicate gap predecessor source")
        amendments (:task-amendments gap)
        new-tasks (:new-task-proposals gap)
        amendment-ids (set (map :id amendments))
        new-ids (set (map :id new-tasks))
        domain-text (slurp "specs/domains-workflow/tasks.md")]
    (require! (= 15 (count amendments)) "Gap task-amendment count changed")
    (require! (= 9 (count new-tasks)) "Gap new-task proposal count changed")
    (require! (= (count amendments) (count (set (map (juxt :path :id) amendments))))
              "Duplicate gap task amendment")
    (require! (= (count new-tasks) (count new-ids)) "Duplicate gap proposed task ID")
    (doseq [[path pin] (get gap :task-file-predecessors)]
      (let [entry (get predecessor-by-path path)]
        (require! entry (str "Gap predecessor missing from current source view: " path))
        (verify-commit-tree! {:commit (:source-commit pin)
                              :tree (git-tree (:source-commit pin))})
        (require! (= (:sha256 pin) (sha256 (git-text (:source-commit pin) path)))
                  (str "Gap predecessor hash drift: " path))
        (require! (= (:commit entry) (:source-commit pin))
                  (str "Gap predecessor commit changed: " path))
        (require! (= (:sha256 entry) (:sha256 pin))
                  (str "Gap predecessor view hash changed: " path))
        (task-source! (:source-commit pin)
                      (assoc entry :sha256 (:sha256 pin)))))
    (doseq [a amendments]
      (require! (and (= [:canonical-task-record] (:field-path a))
                     (true? (:before-present? a))
                     (re-matches #"specs/[a-z-]+-workflow/tasks\.md" (:path a)))
                (str "Gap amendment scope changed: " (:id a)))
      (require! (= (:before-sha256 a) (sha256 (:before a)))
                (str "Gap amendment before hash drift: " (:id a)))
      (require! (= (:after-sha256 a) (sha256 (:after a)))
                (str "Gap amendment after hash drift: " (:id a)))
      (require! (and (str/includes? (:before a) (str "`" (:id a) "`"))
                     (= (re-seq #"(?m)^Status: .+$" (:before a))
                        (re-seq #"(?m)^Status: .+$" (:after a))))
                (str "Gap amendment changed task identity/status: " (:id a))))
    (doseq [t new-tasks]
      (require! (and (= "specs/domains-workflow/tasks.md" (:path t))
                     (= "domains-implementation-tasks" (:marker t))
                     (= (:sha256 t) (sha256 (:raw t)))
                     (str/includes? (:raw t) "Status: Open")
                     (str/includes? (:raw t) "implementation acceptance unassessed"))
                (str "Gap new-task proposal is not an open proposal: " (:id t)))
      (require! (not (str/includes? domain-text (str "- [ ] `" (:id t) "`")))
                (str "Gap new task already exists in current ledger: " (:id t))))
    {:amendment-ids amendment-ids :new-task-ids new-ids}))

(defn validate-gap! [gap family view]
  (require! (= :domain-historical-gap-completion-patch (:kind gap))
            "Wrong domain gap proposal kind")
  (require! (= "requirement-reconciliation/phase-09-domain-specific-computing-coverage"
               (:scope-key gap)) "Wrong domain gap scope")
  (require! (= :proposal-only (:authority gap)) "Domain gap proposal gained authority")
  (require! (= (:prior-family-proposal-sha256 gap)
               (sha256 (git-text (get-in view [:historical-source :family-proposal :commit])
                                 (get-in view [:historical-source :family-proposal :path]))))
            "Gap does not bind the historical family proposal")
  (verify-commit-tree! {:commit (:source-commit gap) :tree (:source-tree gap)})
  (let [published (published-manifest)
        requirements (index! (:requirements published) :id "Duplicate published requirement")
        facets (index! (:source-facets published) :id "Duplicate published source facet")
        documents (index! (concat (:original-documents published)
                                  (:extension-sources published))
                          :id "Duplicate published source document")
        records (:records gap)
        operations (:coverage-operations gap)
        assigned (:assigned-ids gap)
        task-result (validate-gap-tasks! gap view)
        task-ids (set/union (:amendment-ids task-result) (:new-task-ids task-result))]
    (require! (= (:published-manifest-sha256 gap)
                 (sha256 (git-text published-commit published-path))
                 (get-in view [:historical-manifest :gap-published :sha256]))
              "Historical published manifest drift")
    (require! (= 70 (count assigned) (count records) (count operations))
              "Gap assigned population changed")
    (require! (= assigned (mapv :historical-id records) (mapv :id operations))
              "Gap assigned order or identity changed")
    (require! (= (count assigned) (count (set assigned))) "Duplicate gap assigned identity")
    (require! (= operations
                 (mapv (fn [r]
                         {:collection :historical-gap-traceability :id (:historical-id r)
                          :field-path [] :before-present? true
                          :before (:before r) :after (:after r)}) records))
              "Gap coverage write payload differs from records")
    (doseq [r records]
      (validate-gap-record! r requirements facets documents task-ids))
    (require! (= (:unresolved-semantic-ids gap)
                 (mapv :historical-id
                       (filter #(some :unresolved (:source-evidence %)) records)))
              "Gap unresolved-source qualification changed")
    (require! (= {:explicit-proposed-criteria 70 :new-task-proposals 9
                  :existing-task-amendments 15 :implementation-accepted 0
                  :assigned 70 :locally-prepared-interface-mappings 70
                  :independently-accepted-mappings 0 :proposed-task-count-delta 9
                  :actual-task-count-delta 0}
                 (:counts gap)) "Gap conservation counts changed")
    (require! (= 577 (get-in gap [:history-conservation :original-records]))
              "Gap historical record conservation changed")
    (require! (= 561 (get-in gap [:history-conservation :residual-before]))
              "Gap residual history conservation changed")
    (require! (= 13 (get-in gap [:history-conservation :existing-criterion-witnesses]))
              "Gap witness/retirement history changed")
    (require! (= 3 (get-in gap [:history-conservation :retirements]))
              "Gap retirement history changed")
    (require! (= :pending-independent-review-and-canonical-composition
                 (:adoption-state gap)) "Gap adoption state changed")
    (require! (some #(str/includes? % "not revived") (:limits gap))
              "Gap accounting-removal qualification lost")
    (require! (some #(str/includes? % "provider/target/protocol/crypto") (:limits gap))
              "Gap concrete domain constraint qualification lost")
    true))

(defn validate-current! [view]
  (let [current (get-in view [:current-source :task-sources])
        commit (get-in view [:current-source :commit])
        family (get-in view [:historical-source :family-task-sources])]
    (verify-commit-tree! {:commit commit :tree (get-in view [:current-source :tree])})
    (require! (= 6 (count current)) "Current task-source population changed")
    (require! (= (count current) (count (set (map :path current))))
              "Duplicate current task source")
    (doseq [entry current]
      (task-source! commit entry)
      (working-task-source! entry))
    (require! (= (mapv :path current)
                 ["specs/build-workflow/tasks.md"
                  "specs/compiler-api-workflow/tasks.md"
                  "specs/domains-workflow/tasks.md"
                  "specs/legality-workflow/tasks.md"
                  "specs/platform-workflow/tasks.md"
                  "specs/runtime-workflow/tasks.md"])
              "Current task-source order changed")
    (doseq [entry current]
      (when-let [historical (first (filter #(= (:path %) (:path entry)) family))]
        (require! (= (:marker historical) (:marker entry))
                  (str "Current task marker changed: " (:path entry)))))
    (doseq [path ["specs/domains-workflow/tasks.md" "specs/platform-workflow/tasks.md"]]
      (let [h (first (filter #(= path (:path %)) family))
            c (first (filter #(= path (:path %)) current))]
        (require! (= (:sha256 h) (:sha256 c))
                  (str "Family task criteria were not preserved in current ledger: " path))))
    (let [manifest (sha256 (slurp published-path))
          historical #{(get-in view [:historical-manifest :family :sha256])
                       (get-in view [:historical-manifest :gap-published :sha256])
                       (get-in view [:historical-manifest :preserved-pr445 :sha256])}]
      (require! (not (historical manifest))
                "Current manifest silently substituted a historical proposal manifest"))
    true))

(defn validate-view! [view family gap]
  (require! (= :gravity/noncanonical-domain-current-source-view (:kind view))
            "Wrong domain current-source-view kind")
  (require! (= 1 (:version view)) "Unsupported domain current-source-view version")
  (require! (= :proposal-only (:authority view)) "Domain current-source-view gained authority")
  (require! (= "requirement-reconciliation/phase-09-domain-specific-computing-coverage"
               (:scope-key view)) "Wrong domain current-source-view scope")
  (require! (= :open-and-unassessed (get-in view [:qualification-policy :current-task-status]))
            "Current task status qualification changed")
  (require! (= :proposal-only (get-in view [:qualification-policy :gap-task-amendments]))
            "Gap amendment authority changed")
  (require! (= :proposal-only (get-in view [:qualification-policy :gap-new-task-proposals]))
            "Gap new-task authority changed")
  (require! (= :unassessed (get-in view [:qualification-policy :implementation-acceptance]))
            "Implementation acceptance was asserted")
  (require! (set/subset? #{:canonical-adoption :implementation-acceptance :denominator
                           :aggregate-adoption :domain-execution :platform-product
                           :compiler-runtime-or-broker-execution :release :seed-retirement
                           :integration}
                         (set (:nonclaims view)))
            "Domain current-source-view lost a required nonclaim")
  (doseq [pin [(:base view)
               (get-in view [:historical-source :family-proposal])
               (get-in view [:historical-source :gap-proposal])]]
    (verify-commit-tree! pin))
  (require! (= (get-in view [:base :tree]) (git-tree (get-in view [:base :commit])))
            "Base source tree drift")
  (require! (= (get-in view [:historical-source :family-proposal :sha256])
               (sha256 (git-text (get-in view [:historical-source :family-proposal :commit]) family-path)))
            "Historical family proposal bytes drifted")
  (require! (= (get-in view [:historical-source :gap-proposal :sha256])
               (sha256 (git-text (get-in view [:historical-source :gap-proposal :commit]) gap-path)))
            "Historical gap proposal bytes drifted")
  (validate-family! family view)
  (validate-gap! gap family view)
  (validate-current! view)
  true)

(defn rejected? [f]
  (try (f) false (catch Exception _ true)))

(defn controls [view family gap]
  (let [current (get-in view [:current-source :task-sources])
        first-current (first current)
        first-text (slurp (:path first-current))]
    {:positive (do (validate-view! view family gap) true)
     :family-proposal-hash-tamper
     (rejected? #(validate-view! (assoc-in view [:historical-source :family-proposal :sha256] zero-hex)
                                 family gap))
     :gap-proposal-hash-tamper
     (rejected? #(validate-view! (assoc-in view [:historical-source :gap-proposal :sha256] zero-hex)
                                 family gap))
     :current-source-hash-tamper
     (rejected? #(validate-view! (assoc-in view [:current-source :task-sources 0 :sha256] zero-hex)
                                 family gap))
     :task-order-tamper
     (rejected? #(validate-task-records!
                  first-text
                  (update first-current :ordered-task-ids (fn [ids] (vec (reverse ids))))))
     :duplicate-task-id-tamper
     (rejected? #(validate-task-records!
                  (str/replace-first first-text
                                     #"(?m)^- \[ \] `[^`]+`"
                                     "- [ ] `build/duplicate-id`")
                  first-current))
     :closed-task-tamper
     (rejected? #(validate-task-records!
                  (str/replace-first first-text #"(?m)^Status: Open$" "Status: Closed")
                  first-current))
     :wrong-path-tamper
     (rejected? #(validate-view! (assoc-in view [:current-source :task-sources 0 :path]
                                           "specs/not-a-task-file.md")
                                 family gap))
     :canonical-adoption-closure
     (rejected? #(validate-view! (assoc view :authority :canonical-adoption)
                                 family gap))
     :implementation-closure
     (rejected? #(validate-view! (assoc-in view [:qualification-policy :implementation-acceptance]
                                            :accepted)
                                 family gap))}))

(let [args (vec *command-line-args*)]
  (require! (= ["--check"] args)
            "Usage: clojure -M tools/domain_current_source_view.clj --check")
  (let [view (edn/read-string (slurp view-path))
        family (edn/read-string
                (git-text (get-in view [:historical-source :family-proposal :commit]) family-path))
        gap (edn/read-string
             (git-text (get-in view [:historical-source :gap-proposal :commit]) gap-path))
        result (controls view family gap)]
    (require! (every? true? (vals result)) "A domain source-view control failed")
    (prn {:controls result
          :scope (:scope-key view)
          :historical-source-documents (get-in view [:expected :source-documents])
          :historical-source-occurrences (get-in view [:expected :source-occurrences])
          :historical-source-facets (get-in view [:expected :source-facets])
          :family-units (get-in view [:expected :family-units])
          :gap-assigned (get-in view [:expected :gap-assigned])
          :gap-unresolved (get-in view [:expected :gap-unresolved])
          :current-task-source-files (get-in view [:expected :task-source-files])
          :implementation-acceptance :unassessed
          :canonical-adoption :pending
          :denominator-credit :not-claimed
          :independent-review :pending})))
(shutdown-agents)
