(ns acceptance-dependencies
  "Read-only roadmap dependency audit. Graph edges point to prerequisites.
  No compiler execution, status mutation, receipt authentication or acceptance."
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [gravity.tooling.strict-json :as json]))

(def roadmap-path "docs/full-language-implementation-gap-map.md")
(def milestone-path "docs/self-hosting-milestone-completion-plan.md")

;; Fixed existing completion population, not a quota on new language work.
;; Any intentional population amendment must update its owning roadmap and this
;; explicit identity set in the same reviewed change; deleting a leaf is not closure.
(def full-language-task-ids
  #{"FL-P00-T00" "FL-P00-T01" "FL-P00-T02"
    "FL-P01-T01" "FL-P01-T02" "FL-P01-T03"
    "FL-P02-T01" "FL-P02-T02" "FL-P03-T01" "FL-P03-T02"
    "FL-P04-T01" "FL-P04-T02" "FL-P05-T01" "FL-P05-T02"
    "FL-P06-T01" "FL-P06-T02" "FL-P06-T03"
    "FL-P07-T01" "FL-P07-T02" "FL-P08-T01" "FL-P08-T02"
    "FL-P09-T01" "FL-P10-T01" "FL-P11-T01" "FL-P12-T01"
    "FL-P13-T01" "FL-P13-T02" "FL-P14-T01" "FL-P14-T02"
    "FL-P15-T01" "FL-P15-T02" "FL-P15-T03" "FL-P16-T01"
    "FL-P17-T01" "FL-P18-T01" "FL-P18-T02"})

(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "ACCEPTANCE-DEPENDENCY"}))))

(defn require-population! [expected actual label]
  (require! (= expected (set actual)) (str "Missing or unknown " label " identity")))

(defn dependency-list [declaration token-pattern terminal-period?]
  (let [text (str/trim declaration)
        _ (when terminal-period?
            (require! (str/ends-with? text ".") "Dependency declaration must end with one period"))
        body (if terminal-period? (subs text 0 (dec (count text))) text)
        parts (str/split body #"," -1)
        parsed (mapv #(re-matches token-pattern (str/trim %)) parts)]
    (require! (and (seq parts) (every? some? parsed))
              (str "Malformed dependency list: " declaration))
    (let [ids (mapv second parsed)]
      (require! (= (count ids) (count (set ids))) "Duplicate dependency token")
      (set ids))))

(defn full-language-graph [text]
  (let [phases (vec (for [[_ phase body]
                         (re-seq #"(?ms)^## Phase (\d{2})[^\n]*\n(.*?)(?=^## |\z)" text)]
                     {:phase phase :body body
                      :ids (mapv second (re-seq #"(?m)^- \[[ xX]\] `(FL-P\d{2}-T\d{2})`" body))}))
        ids (vec (mapcat :ids phases))]
    (require! (and (seq ids) (= (count ids) (count (set ids)))) "Missing or duplicate full-language task IDs")
    (doseq [{:keys [phase body ids]} phases]
      (require! (and (seq ids) (every? #(str/starts-with? % (str "FL-P" phase "-")) ids))
                (str "Missing or wrong-phase task IDs: " phase))
      (doseq [line (re-seq #"(?m)^- \[[^\n]*" body)]
        (require! (re-matches #"- \[[ xX]\] `FL-P\d{2}-T\d{2}` .+" line)
                  (str "Malformed task row: " line))))
    (into (sorted-map)
          (mapcat (fn [{:keys [phase body ids]}]
                    (let [declaration (second (re-find #"(?s)Dependencies: (.*?)(?:\n\n|\z)" body))
                          _ (require! declaration (str "Missing phase dependency declaration: " phase))
                          dependencies
                          (cond
                            (= "none." declaration) #{}
                            (= "all feature phases whose behavior is under test." declaration)
                            (set (mapcat :ids (filter #(not (#{"00" "14" "15" "17" "18"} (:phase %))) phases)))
                            (= "all previous full-language tasks." declaration)
                            (set (mapcat :ids (filter #(neg? (compare (:phase %) phase)) phases)))
                            :else (dependency-list declaration #"`(FL-P\d{2}-T\d{2})`" true))]
                      (require! (every? (set (mapcat :ids phases)) dependencies)
                                (str "Unknown full-language prerequisite in phase " phase))
                      (map (fn [id] [id dependencies]) ids))) phases))))

(defn milestone-graph [text]
  (let [rows (re-seq #"(?m)^\| \[(SH-\d{2})\]\(#[a-z0-9-]+\) \| [^|\n]+ \| ([^|\n]+) \| [^|\n]+ \|$" text)
        row-lines (re-seq #"(?m)^\| \[?SH-[^\n]*" text)
        expected (set (map #(format "SH-%02d" %) (range 30)))
        ids (mapv second rows)]
    (require! (and (= expected (set ids)) (= (count rows) (count row-lines)))
              "Missing, unknown or malformed milestone row")
    (require! (and (= expected (set ids))
                   (= (count ids) (count (set ids))))
              "Missing or duplicate milestone rows")
    (into (sorted-map)
          (for [[_ id dependencies] rows]
            (let [refs (if (= "none" (str/trim dependencies)) #{}
                         (dependency-list dependencies #"(SH-\d{2})" false))]
              (require! (every? expected refs) (str "Unknown milestone prerequisite: " id))
              [id refs])))))

(defn validate-graph [graph]
  (require! (seq graph) "Empty dependency graph")
  (doseq [[id dependencies] graph]
    (require! (and (string? id) (set? dependencies)) "Malformed dependency node")
    (doseq [dependency dependencies]
      (require! (contains? graph dependency) (str "Unknown prerequisite: " id " -> " dependency))))
  graph)

(defn reachable [graph start]
  (loop [pending [start] seen #{}]
    (if-let [node (peek pending)]
      (if (seen node)
        (recur (pop pending) seen)
        (recur (into (pop pending) (get graph node)) (conj seen node)))
      seen)))

(defn strongly-connected [graph]
  (validate-graph graph)
  (let [closure (into {} (map (fn [id] [id (reachable graph id)]) (keys graph)))]
    (loop [remaining (set (keys graph)) components []]
      (if-let [id (first (sort remaining))]
        (let [component (set (filter #(and ((closure id) %) ((closure %) id)) remaining))]
          (recur (reduce disj remaining component) (conj components (vec (sort component)))))
        components))))

(defn cycles [graph]
  (filterv (fn [component]
             (or (> (count component) 1)
                 (contains? (get graph (first component)) (first component))))
           (strongly-connected graph)))

(defn staged-graph [graph]
  ;; Construction depends on declared interfaces, never downstream final credit.
  ;; Composition requires executable producers, not a producer checkbox. Every
  ;; original edge survives as a real composition prerequisite. Final acceptance
  ;; additionally requires the task's unchanged proof gate on that exact closure.
  (validate-graph graph)
  (into (sorted-map)
        (mapcat (fn [[id dependencies]]
                  [[(str id "/interface") #{}]
                   [(str id "/implementation")
                    (set (map #(str % "/interface") (conj dependencies id)))]
                   [(str id "/composition")
                    (set (map #(str % "/implementation") (conj dependencies id)))]
                   [(str id "/acceptance") #{(str id "/composition")}]] ) graph)))

(defn sha256 [text]
  (format "%064x" (java.math.BigInteger. 1
                    (.digest (java.security.MessageDigest/getInstance "SHA-256")
                             (.getBytes text java.nio.charset.StandardCharsets/UTF_8)))))

(defn implementation-ids [text component]
  (let [start (str "<!-- " component "-implementation-tasks:start -->")
        end (str "<!-- " component "-implementation-tasks:end -->")
        starts (count (re-seq (re-pattern (java.util.regex.Pattern/quote start)) text))
        ends (count (re-seq (re-pattern (java.util.regex.Pattern/quote end)) text))]
    (if (and (zero? starts) (zero? ends))
      nil
      (do
        (require! (and (= 1 starts ends) (< (.indexOf text start) (.indexOf text end)))
                  (str "Malformed implementation region: " component))
        (let [region (subs text (+ (.indexOf text start) (count start)) (.indexOf text end))
              rows (re-seq #"(?m)^- \[[^\n]*" region)
              parsed (mapv #(re-matches #"- \[[ xX]\] `([^\s`]+)`(?: .*)?" %) rows)
              ids (mapv second parsed)]
          ;; Preserve literal existing IDs, including backend-llvm. Naming is
          ;; not ownership: this read-only census must not invent a prefix
          ;; policy or become a second task authority.
          (require! (and (seq ids) (every? some? parsed)
                         (= (count ids) (count (set ids))))
                    (str "Missing, duplicate or malformed implementation IDs: " component))
          ids)))))

(defn component-census [registry read-text exists?]
  (let [components (mapv second (re-seq #"(?m)^\| [^|\n]+ / `([a-z-]+)` \|" registry))]
    (require! (and (seq components) (= (count components) (count (set components))))
              "Missing or duplicate component registry entries")
    (into (sorted-map)
          (for [component components
                :let [roadmap (str "docs/component-roadmaps/" component ".md")
                      text (read-text roadmap)
                      tasks (str "specs/" component "-workflow/tasks.md")
                      task-text (when (exists? tasks) (read-text tasks))
                      ids (when task-text (implementation-ids task-text component))]]
            [component {:roadmap roadmap :source-sha256 (sha256 text)
                        :task-source (if ids tasks roadmap)
                        :implementation-ids ids
                        :migration (if ids :implementation-ledger-present :roadmap-remains-authority)
                        :reference-links (vec (sort (set (for [[_ target]
                                     (re-seq #"\]\(([a-z-]+)\.md#[^)]*\)" text)
                                     :when (contains? (set components) target)] target))))}]))))

(defn retained-packages [mapping inventory read-text]
  (let [packages (mapv second (re-seq #"(?m)^### Package ([a-z0-9-]+)$" mapping))
        _ (require! (and (seq packages) (= (count packages) (count (set packages))))
                    "Missing or duplicate retained package routes")
        artifacts (into (sorted-map)
                        (for [package packages
                              artifact ["spec" "plan" "tasks" "evidence"]
                              :let [root (if (#{"naming-pilot" "reader-core-invocation"} package) ".specify" "specs")
                                    path (str root "/" package "/" artifact ".md")]]
                          [path (read-text path)]))
        references (set (for [[_ text] artifacts [_ target] (re-seq #"\]\(([^)#]+)(?:#[^)]*)?\)" text)] target))
        linked (set (for [row inventory :when (references (str "../../" (get row "path")))] (get row "id")))
        unlinked (sort (remove linked (map #(get % "id") inventory)))]
    {:packages packages :artifact-source-digests (into (sorted-map) (map (fn [[path text]] [path (sha256 text)]) artifacts))
     :directly-linked-contracts (count linked) :without-direct-package-links (vec unlinked)
     :authority :navigation-not-contract-replacement}))

;; SH criteria and task status are derived views of the original source and the
;; existing component authorities. This is the only dependency representation;
;; crosswalk routes are incidence, not a second editable task/status graph.
(def sh-crosswalk-path "specs/sh-family-crosswalk/crosswalk.edn")

(defn navigation-source-path? [path]
  ;; Component roadmaps and task-ledger preambles are navigation-only views of
  ;; the exact task rows. Their captured digests stay in the crosswalk as
  ;; historical provenance, but layout edits must not rebind SH acceptance
  ;; history. Task rows and all other source-bound inputs remain exact below.
  (or (str/starts-with? path "docs/component-roadmaps/")
      (re-matches #"specs/[a-z-]+-workflow/tasks\.md" path)))

(defn source-bound! [{:keys [path start-byte end-byte sha256 raw]} read-text]
  (let [text (read-text path) bytes (.getBytes text "UTF-8")]
    (require! (and (integer? start-byte) (integer? end-byte)
                   (<= 0 start-byte end-byte (alength bytes))) "Invalid source byte span")
    (let [actual (String. (java.util.Arrays/copyOfRange bytes start-byte end-byte) "UTF-8")]
      (require! (and (= raw actual) (= sha256 (acceptance-dependencies/sha256 actual)))
                (str "Changed source span: " path)))))

(defn current-task-body!
  "Check a current task row's captured body without rebinding its history.
  Task-ledger preambles are navigation layout, so their edits can shift a row;
  the row itself must remain one unique exact captured byte sequence."
  [{:keys [path sha256 raw]} read-text]
  (let [text (read-text path)
        occurrence-count (loop [offset 0 occurrences 0]
                           (let [found (.indexOf text raw offset)]
                             (if (neg? found)
                               occurrences
                               (recur (+ found (max 1 (clojure.core/count raw)))
                                      (inc occurrences)))))]
    (require! (= sha256 (acceptance-dependencies/sha256 raw))
              (str "Forged current task body digest: " path))
    (require! (= 1 occurrence-count)
              (str "Changed or ambiguous current task body: " path))))

(defn typed-adjacency [nodes edges condition-values]
  (let [ids (set (keys nodes))]
    (require! (every? #(contains? #{true false :unknown} %) (vals condition-values)) "Invalid condition state")
    (reduce (fn [g {:keys [from to condition kind source]}]
              (require! (and (ids from) (ids to) (keyword? kind) source)
                        "Unknown or unbound typed edge")
              ;; Unknown conditions are conserved in the conservative graph.
              ;; Only a specifically false declared condition disables an edge.
              (if (and condition (= false (get condition-values condition :unknown))) g
                  (update g from conj to)))
            (into (sorted-map) (map (fn [id] [id #{}]) ids)) edges)))

(defn advisory-frontier [nodes edges condition-values]
  (let [graph (typed-adjacency nodes edges condition-values)
        unresolved (set (for [e edges :when (and (:condition e)
                          (= :unknown (get condition-values (:condition e) :unknown)))] (:from e)))]
    (into (sorted-map)
          (for [[id row] nodes]
            [id {:state (cond
                          (= :accepted (:state row)) :accepted-recorded-scope
                          (= :missing-task-authority (:coverage row)) :missing-task-authority
                          (#{:selected-acceptance-facet :uniform-completion-gate} (:scope row)) :execution-and-review-unassessed
                          (unresolved id) :unknown-condition
                          (some #(not= :accepted (:state (nodes %))) (graph id)) :prerequisite-not-accepted
                          (= :current-acceptance (:scope row)) :execution-and-review-unassessed
                          :else :eligible-for-declared-facet)
                 :prerequisites (vec (sort (graph id)))
                 :authority :advisory-not-execution-permission}]))))

(defn sh07-prerequisites [declaration]
  (let [prefix (first (str/split declaration #";"))
        parts (str/split prefix #", ")]
    (if (= "none" prefix) []
      (vec (mapcat (fn [part]
                    (let [[_ a b] (re-matches #"C(\d{2})(?:-C(\d{2}))?" part)]
                      (require! a "Unknown SH07 dependency declaration")
                      (let [start (Long/parseLong a) end (if b (Long/parseLong b) start)]
                        (require! (<= 1 start end 18) "Invalid SH07 dependency range")
                        (map #(format "SH07-C%02d" %) (range start (inc end)))))) parts)))))

(defn source-ref [source] (dissoc source :raw))

(defn task-key [task-map routing component ordinal]
  (let [id (get-in routing [component (dec ordinal)]) row (task-map id)]
    (require! (= component (:component row)) "Missing semantic task route") id))

(defn evidence-source-state [{:keys [path sha256 source-commit current-applicability]} read-text]
  (let [current (try (acceptance-dependencies/sha256 (read-text path))
                    (catch java.io.IOException _ nil))
        state (cond (nil? current) :absent-from-current-checkout
                    (= sha256 current) :same-bytes-as-inspection
                    :else :changed-since-inspection)]
    ;; These records are explicitly pinned source inspections, not current
    ;; execution evidence. Preserve the old commit/hash and expose drift; a
    ;; normal compiler edit cannot silently become acceptance or invalidate
    ;; that historical source identity. Current-applicable claims stay strict.
    (when-not (= :unassessed current-applicability)
      (require! (= sha256 current) (str "Changed current-applicable evidence source: " path)))
    {:path path :inspected-commit source-commit :inspected-sha256 sha256
     :current-sha256 current :state state :current-applicability current-applicability}))

;; These selected-output composition requirements come from the consuming
;; task's own acceptance block, never from Markdown cross-reference counts.
;; No edge requires final component release merely to develop another task.
(def composition-routes
  [["syntax" 5 "reader" 6 nil]
   ["expand" 5 "syntax" 5 nil]
   ["resolve" 5 "expand" 5 nil]
   ["core" 5 "resolve" 5 nil]
   ["types" 5 "core" 5 nil]
   ["effects" 5 "types" 5 nil]
   ["auth" 2 "types" 5 nil]
   ["auth" 2 "effects" 5 nil]
   ["auth" 2 "legality" 1 nil]
   ["mir" 5 "types" 5 nil]
   ["mir" 5 "effects" 5 nil]
   ["mir" 5 "legality" 4 nil]
   ["mir" 5 "auth" 2 nil]
   ["opt" 5 "mir" 5 nil]
   ["lower" 5 "mir" 5 nil]
   ["lower" 5 "opt" 5 :optimization-requested]
   ["lower" 3 "runtime" 1 :runtime-service-required]
   ["emit" 5 "lower" 5 nil]
   ["build" 2 "emit" 5 :compiler-artifacts-consumed]
   ["cli" 2 "build" 2 :build-command-selected]
   ["cli" 2 "runtime" 2 :execution-command-selected]
   ["release" 3 "build" 2 nil]
   ["release" 3 "cli" 5 nil]
   ["release" 3 "runtime" 2 nil]])

(defn task-sh-view [crosswalk read-text]
  (let [requirements (:requirements crosswalk)
        shared-rule (:shared-completion-rule crosswalk)
        _ (source-bound! (:source shared-rule) read-text)
        _ (require! (and (= :sh03-and-later-uniform-completion (:id shared-rule))
                         (str/starts-with? (get-in shared-rule [:source :raw]) "## Three boundaries and one shared completion rule"))
                    "Missing original shared completion rule")
        original-sh (milestone-graph (read-text milestone-path))
        _ (require-population! (set (for [n (range 30) r (range 1 7)] (format "SH-%02d.%d" n r)))
                               (map :id requirements) "SH acceptance criterion")
        _ (require! (= 180 (count requirements)) "Duplicate SH criterion")
        tasks (vec (mapcat val (:task-authorities crosswalk)))
        task-map (into {} (map (juxt :id identity) tasks))
        routing (:stable-task-routing-keys crosswalk)
        _ (require! (= (count tasks) (count task-map)) "Duplicate global literal task ID")
        _ (doseq [[component rows] (:task-authorities crosswalk)]
            (require-population! (set (map :id rows)) (get routing component) (str component " fixed routing key"))
            (require-population! (set (map :id rows))
              (implementation-ids (read-text (str "specs/" component "-workflow/tasks.md")) component)
              (str component " live implementation task")))
        _ (doseq [row tasks]
            (current-task-body! (:source row) read-text)
            (let [[_ checked id] (re-find #"^- \[([ xX])\] `([^`]+)`" (get-in row [:source :raw]))]
              (require! (and (= id (:id row)) (= (:checked row) (not= " " checked)))
                        "Task status/ID differs from actual authority")
              (when (:checked row)
                (require! (str/includes? (get-in row [:source :raw]) "Accepted;")
                          "Checked task lacks explicit accepted status"))))
        _ (doseq [r requirements]
            (source-bound! (:acceptance-source r) read-text)
            (source-bound! (:context-source r) read-text)
            (when-let [history (get-in r [:historical-completion :evidence-source])]
              (source-bound! history read-text))
            (require! (= (set (:completion-prerequisites r)) (get original-sh (:milestone r)))
                      "SH criterion changed original completion prerequisites")
            (require! (= (:shared-completion-rule r)
                         (when (<= 3 (Long/parseLong (subs (:milestone r) 3))) (:id shared-rule)))
                      "Changed shared completion applicability")
            (require! (or (seq (:task-references r)) (= :missing-task-authority (:coverage r)))
                      "Unmapped SH criterion without explicit gap")
            (doseq [t (:task-references r)]
              (require! (= (:component t) (:component (task-map (:id t)))) "Unknown SH task owner/reference")))
        _ (doseq [[path digest] (:source-digests crosswalk)
                  :when (not (navigation-source-path? path))]
            (require! (= digest (sha256 (read-text path))) (str "Stale SH input: " path)))
        ;; Contract declarations permit interface work; they do not attest an
        ;; implementation. Acceptance facets remain unknown without exact receipts.
        interface-nodes (into {} (for [[id row] task-map]
                                  [(str "task:" id "/interface")
                                   {:state :accepted :scope :declared-interface-only :task-id id :component (:component row)}]))
        implementation-nodes (into {} (for [[id row] task-map]
                                       [(str "task:" id "/implementation")
                                        {:state (if (:checked row) :accepted :unassessed)
                                         :scope :literal-task-record :task-id id :component (:component row)}]))
        criterion-nodes (into {} (for [r requirements]
                                  [(:id r) {:state :unassessed :scope :current-acceptance :milestone (:milestone r)
                                            :coverage (:coverage r)}]))
        milestone-nodes (into {} (for [id (sort (set (map :milestone requirements)))]
                                   [id {:state :unassessed :scope :current-completion-credit}]))
        facet-nodes (into {} (for [r requirements t (:task-references r)]
                              [(str "facet:" (:id r) ":" (:id t))
                               {:state :unassessed :scope :selected-acceptance-facet
                                :task-id (:id t) :criterion (:id r)}]))
        sequence (:sh07-sequence crosswalk)
        _ (require-population! (set (map #(format "SH07-C%02d" %) (range 1 19))) (map :id sequence) "SH07 sequence")
        _ (require! (= 18 (count sequence)) "Duplicate SH07 sequence row")
        _ (doseq [r sequence]
            (source-bound! (:source r) read-text)
            (let [[_ id _ _ declaration] (re-matches #"\| (SH07-C\d{2}) \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^\n]+)\|"
                                                    (get-in r [:source :raw]))]
              (require! (and (= id (:id r)) (= (str/trim declaration) (:dependency-declaration r)))
                        "SH07 sequence metadata differs from original source")))
        sequence-nodes (into {} (for [r sequence] [(:id r) {:state :unassessed :scope :original-implementation-sequence}]))
        composition-nodes (into {} (for [[id _] task-map]
                                      [(str "task:" id "/composition")
                                       {:state :unassessed :scope :actual-selected-producer-consumer :task-id id}]))
        shared-gate-nodes (into {} (for [n (range 3 30)] [(format "uniform-gate:SH-%02d" n)
                                      {:state :unassessed :scope :uniform-completion-gate :rule (:id shared-rule)}]))
        nodes (merge interface-nodes implementation-nodes criterion-nodes milestone-nodes facet-nodes sequence-nodes composition-nodes shared-gate-nodes)
        evidence-applicability (mapv #(evidence-source-state % read-text)
                                    (sort-by (juxt :path :source-commit :sha256)
                                      (distinct (mapcat :evidence-source-locators requirements))))
        conditions {:optimization-requested :unknown :runtime-service-required :unknown
                    :compiler-artifacts-consumed :unknown :build-command-selected :unknown
                    :execution-command-selected :unknown :stable-candidate-selection :unknown}
        edges (vec (concat
          [{:from "SH-07" :to "SH07-C18" :kind :single-milestone-completion-decision
            :source (source-ref (:source (last sequence)))}]
          (for [n (range 3 30)]
            {:from (format "SH-%02d" n) :to (format "uniform-gate:SH-%02d" n)
             :kind :applicable-uniform-completion-rule :source (source-ref (:source shared-rule))})
          (for [[id row] task-map]
            {:from (str "task:" id "/implementation") :to (str "task:" id "/interface")
             :kind :declared-interface :source (source-ref (:source row))})
          (for [[id row] task-map]
            {:from (str "task:" id "/composition") :to (str "task:" id "/implementation")
             :kind :actual-implementation :source (source-ref (:source row))})
          (for [[consumer n producer p condition] composition-routes
                :let [from (task-key task-map routing consumer n) to (task-key task-map routing producer p)]]
            {:from (str "task:" from "/composition") :to (str "task:" to "/implementation")
             :kind :selected-producer-implementation :condition condition
             :source (source-ref (:source (task-map from)))})
          (mapcat (fn [r]
                    (let [id (:id r)
                          prerequisites (cond-> (sh07-prerequisites (:dependency-declaration r))
                                          (= id "SH07-C18") (into (map #(format "SH07-C%02d" %) (range 1 17))))]
                      (concat
                       (for [p (distinct prerequisites)]
                         {:from id :to p :kind :original-sequence-prerequisite :source (source-ref (:source r))})
                       (when (= id "SH07-C12")
                         [{:from id :to "SH07-C11" :kind :final-selection-only
                           :condition :stable-candidate-selection :source (source-ref (:source r))}])))) sequence)
          (mapcat (fn [r]
                    (concat
                     [{:from (:milestone r) :to (:id r) :kind :acceptance-conjunction :source (source-ref (:acceptance-source r))}]
                     (for [p (:completion-prerequisites r)]
                       {:from (:id r) :to p :kind :completion-credit-only :source (source-ref (:context-source r))})
                     (for [t (:task-references r)]
                       {:from (:id r) :to (str "facet:" (:id r) ":" (:id t))
                        :kind :requires-selected-execution-and-review :source (source-ref (:acceptance-source r))}))) requirements)))
        adjacency (typed-adjacency nodes edges conditions)]
    (require! (empty? (cycles adjacency)) "SH/task current acceptance cycle")
    {:schema-version 1 :authority :structural-only :nodes nodes :typed-edges edges
     :condition-values conditions
     :evidence-source-applicability evidence-applicability
     :mapping-ready? (every? #(= :owner-and-independent-reviewed (:coverage %)) requirements)
     :coverage-gaps (mapv :id (filter #(= :missing-task-authority (:coverage %)) requirements))
     :adjacency adjacency :cycles (cycles adjacency) :frontier (advisory-frontier nodes edges conditions)
     :incidence (into (sorted-map) (map (fn [r] [(:id r) (mapv :id (:task-references r))]) requirements))
     :limits [:task-interface-is-not-execution :selected-facet-not-whole-task
              :historical-acceptance-does-not-set-current-state :semantic-edge-review-pending
              :frontier-is-not-platform-permission]}))

(defn audit []
  (let [roadmap (slurp roadmap-path)
        milestones (slurp milestone-path)
        graph (validate-graph (full-language-graph roadmap))
        _ (require-population! full-language-task-ids (keys graph) "full-language task")
        _ (require! (and (str/includes? roadmap "### Dependency stages and finite acceptance")
                         (every? #(str/includes? roadmap %) ["| Interface |" "| Implementation |" "| Composition |" "| Acceptance |"
                                                                  "Every original dependency survives at composition."]))
                    "Staged graph requires the owning roadmap's explicit evidence-stage contract")
        staged (staged-graph graph)
        sh (validate-graph (milestone-graph milestones))
        inventory (json/load-json (.toPath (io/file "docs/document-inventory.json")))
        components (component-census (slurp "docs/component-roadmaps/README.md") slurp #(.isFile (io/file %)))
        component-graph (into (sorted-map) (map (fn [[id row]] [id (set (:reference-links row))]) components))
        contract-inputs (into (sorted-map)
                              (for [row inventory :let [path (get row "path")]]
                                [path (sha256 (slurp path))]))]
    (require! (= 240 (count inventory) (count (set (map #(get % "id") inventory))))
              "Canonical contract identity changed")
    (require! (empty? (cycles sh)) "Self-hosting completion cycle")
    (require! (empty? (cycles staged)) "Staged acceptance cycle")
    {:authority :structural-only
     :source-digests (sorted-map roadmap-path (sha256 roadmap) milestone-path (sha256 milestones))
     :full-language {:edges graph :coarse-acceptance-cycles (cycles graph)
                     :staged-edges staged :staged-cycles (cycles staged)}
     :self-hosting {:edges sh :cycles (cycles sh)}
     :components {:sources components :reference-cycles (cycles component-graph)
                  :edge-class :document-links-not-acceptance-prerequisites}
     :canonical-contract-source-digests contract-inputs
     :spec-kit-task-self-hosting
     (task-sh-view (edn/read-string (slurp sh-crosswalk-path)) slurp)
     :retained-spec-kit (retained-packages (slurp ".specify/contract-mapping.md") inventory slurp)
     :limits [:no-semantic-acceptance :no-task-status-change :no-receipt-authentication
              :interfaces-do-not-satisfy-composition :proof-gates-remain-required]}))

(defn run [args]
  (require! (= ["--check"] (vec args)) "Usage: clojure -M tools/acceptance_dependencies.clj --check")
  (prn (audit)))

(when (seq *command-line-args*) (run *command-line-args*))
