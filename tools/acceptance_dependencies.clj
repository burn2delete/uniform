(ns acceptance-dependencies
  "Read-only roadmap dependency audit. Graph edges point to prerequisites.
  No compiler execution, status mutation, receipt authentication or acceptance."
  (:require [clojure.string :as str]
            [clojure.set :as set]
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

(defn marked-task-ids [text marker]
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")
        starts (count (re-seq (re-pattern (java.util.regex.Pattern/quote start)) text))
        ends (count (re-seq (re-pattern (java.util.regex.Pattern/quote end)) text))]
    (if (and (zero? starts) (zero? ends))
      nil
      (do
        (require! (and (= 1 starts ends) (< (.indexOf text start) (.indexOf text end)))
                  (str "Malformed implementation region: " marker))
        (let [region (subs text (+ (.indexOf text start) (count start)) (.indexOf text end))
              rows (re-seq #"(?m)^- \[[^\n]*" region)
              parsed (mapv #(re-matches #"- \[[ xX]\] `([^\s`]+)`(?: .*)?" %) rows)
              ids (mapv second parsed)]
          ;; Preserve literal existing IDs, including backend-llvm. Naming is
          ;; not ownership: this read-only census must not invent a prefix
          ;; policy or become a second task authority.
          (require! (and (seq ids) (every? some? parsed)
                         (= (count ids) (count (set ids))))
                    (str "Missing, duplicate or malformed implementation IDs: " marker))
          ids)))))

(defn implementation-ids [text component]
  (marked-task-ids text (str component "-implementation-tasks")))

(def future-task-regions
  [["domains" "domains-wider-integration-proposals" 9]
   ["domains" "domains-math-schema-proposals" 7]
   ["release" "release-testing-proposals" 13]
   ["platform" "platform-product-implementation-tasks" 11]])

(def future-dependency-path "docs/acceptance-dependencies.md")

(def selected-parent-consumers
  #{"domain-ir-boundaries" "math"
    "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
    "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"
    "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
    "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
    "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"
    "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
    "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
    "types/current-evidence-and-remaining-work/types5-integrate-typed-core"
    "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"
    "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
    "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"
    "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
    "backend-gpu"
    "backend-hdl"
    "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
    "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
    "hardware"
    "gpu"
    "effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
    "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
    "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
    "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"
    "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
    "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
    "expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
    "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
    "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
    "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
    "workflow"
    "ai"
    "evolution/security-review"
    "evolution/change-review"
    "core/current-evidence-and-remaining-work/core1-lower-core-forms"
    "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
    "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
    "auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
    "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
    "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
    "devices"
    "legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
    "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
    "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
    "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
    "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"
    "release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance"
    "release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom"
    "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
    "release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate"
    "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution"})

(defn future-dependency-routes [text task-ids consumer-ids]
  (let [marker "future-composition-dependencies"
        start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        occurrences #(count (re-seq (re-pattern (java.util.regex.Pattern/quote %)) text))
        _ (require! (and (= 1 (occurrences start) (occurrences end))
                         (< (.indexOf text start) (.indexOf text end)))
                    "Malformed future dependency declaration region")
        lines (vec (remove str/blank? (str/split-lines
                 (subs text (+ (.indexOf text start) (count start)) (.indexOf text end)))))
        _ (require! (= ["| Consumer | Start results | Composition results | Selected output contract |"
                        "| --- | --- | --- | --- |"] (vec (take 2 lines)))
                    "Unknown future dependency table syntax")
        ids (fn [cell]
              (if (= "none" cell) []
                (let [parts (str/split cell #", " -1)
                      parsed (mapv #(second (re-matches #"`([^\s`]+)`" %)) parts)]
                  (require! (and (seq parsed) (every? some? parsed)
                                 (= (count parsed) (count (set parsed)))
                                 (every? task-ids parsed)) "Unknown or malformed future dependency ID")
                  parsed)))
        rows (mapv (fn [line]
                     (let [cells (str/split line #"\|" -1)
                           _ (require! (and (= 6 (count cells)) (= "" (first cells) (last cells)))
                                       "Malformed future dependency row")
                           [consumer start-results results contract] (mapv str/trim (subvec (vec cells) 1 5))
                           consumers (ids consumer) starts (ids start-results) composition (ids results)]
                       (require! (and (= 1 (count consumers)) (consumer-ids (first consumers))
                                      (not (str/blank? contract))
                                      (empty? (set/intersection (set starts) (set composition))))
                                 "Invalid future consumer/output declaration")
                       {:consumer (first consumers) :start-results starts :composition-results composition
                        :selected-output-contract contract
                        :source {:path future-dependency-path :sha256 (sha256 text) :declaration line}}))
                   (drop 2 lines))]
    (require! (and (seq rows) (= (count rows) (count (set (map :consumer rows)))))
              "Missing or duplicate future dependency consumer")
    rows))

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
  ;; history. Task rows and all other source-bound inputs remain exact below;
  ;; the separately classified historical ownership map is not a current claim.
  (or (str/starts-with? path "docs/component-roadmaps/")
      (re-matches #"specs/[a-z-]+-workflow/tasks\.md" path)))

(defn historical-sh-source-path? [path]
  ;; This ownership map is a historical SH routing input. Its live entries may
  ;; be retired without rebinding current task or acceptance authority; the
  ;; caller records that drift as unassessed instead of treating it as current.
  (= path "docs/self-hosting-slice-ownership.edn"))

(defn source-raw
  "Resolve a legacy inline source span or a normalized crosswalk snapshot.
  Snapshot references are content-addressed and therefore fail closed on a
  missing, substituted or internally inconsistent payload."
  ([source] (source-raw source nil))
  ([source snapshots]
   (cond
     (contains? source :raw)
     (do
       (require! (not (contains? source :source-snapshot-ref))
                 "Source span contains both inline and referenced payloads")
       (require! (string? (:raw source)) "Inline source span is not text")
       (:raw source))

     :else
     (let [reference (:source-snapshot-ref source)
           raw (get snapshots reference)]
       (require! (and (string? reference) (string? raw)
                      (= reference (sha256 raw)))
                 "Missing or tampered source snapshot")
       raw))))

(defn source-bound!
  ([source read-text] (source-bound! source nil read-text))
  ([{:keys [path start-byte end-byte sha256] :as source} snapshots read-text]
   (let [raw (source-raw source snapshots)
         text (read-text path) bytes (.getBytes text "UTF-8")]
     (require! (and (integer? start-byte) (integer? end-byte)
                    (<= 0 start-byte end-byte (alength bytes))) "Invalid source byte span")
     (let [actual (String. (java.util.Arrays/copyOfRange bytes start-byte end-byte) "UTF-8")]
       (require! (and (= raw actual) (= sha256 (acceptance-dependencies/sha256 actual)))
                 (str "Changed source span: " path))))))

(defn current-task-body!
  "Check a current task row's captured body without rebinding its history.
  Task-ledger preambles are navigation layout, so their edits can shift a row;
  the row itself must remain one unique exact captured byte sequence."
  ([source read-text] (current-task-body! source nil read-text))
  ([{:keys [path sha256] :as source} snapshots read-text]
   (let [raw (source-raw source snapshots)
         text (read-text path)
        occurrence-count (loop [offset 0 occurrences 0]
                           (let [found (.indexOf text raw offset)]
                             (if (neg? found)
                               occurrences
                               (recur (+ found (max 1 (clojure.core/count raw)))
                                      (inc occurrences)))))]
     (require! (= sha256 (acceptance-dependencies/sha256 raw))
               (str "Forged current task body digest: " path))
     (require! (= 1 occurrence-count)
               (str "Changed or ambiguous current task body: " path)))))

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
                          (= :pending (:dependency-review row)) :dependency-review-pending
                          (= :unresolved-future-composition (:scope row)) :dependency-review-pending
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

(defn math-output-view [tasks snapshots]
  (let [rows (vec
               (for [[task-id task] (sort-by key tasks)
                     :when (or (= "math" task-id) (str/starts-with? task-id "math/"))
                     :let [raw (source-raw (:source task) snapshots)
                           matcher (re-matcher #"(?ms)^Criterion `(math/[^`]+)` \((MATH\d+); Open\)\.\n.*?^Required future evidence: [^\n]*(?:\n|$)" raw)]
                     row (loop [found []]
                           (if (.find matcher)
                             (let [body (.group matcher)
                                   [_ inputs external] (re-find #"(?m)^Dependencies: (.+)\. (Types/Legality supplies .+)$" body)
                                   output (second (re-find #"(?m)^Outputs: (.+)$" body))
                                   roots "declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required"
                                   dependencies (if (= roots inputs) [] (str/split (or inputs "") #", " -1))]
                               (require! (and output external
                                              (contains? (set (map #(str "MATH" %) (range 1 12))) (.group matcher 2))
                                              (= 1 (count (re-seq #"(?m)^Dependencies:" body))
                                                   (count (re-seq #"(?m)^Outputs:" body)))
                                              (every? #(re-matches #"math/[a-z0-9-]+" %) dependencies)
                                              (= (count dependencies) (count (set dependencies))))
                                         "Malformed literal Math output dependency")
                               (recur (conj found
                                        {:id (.group matcher 1) :canonical-document (.group matcher 2)
                                         :task-id task-id :state :unassessed :output-declaration output
                                         :dependency-declaration inputs :external-input-contract external
                                         :dependencies (vec dependencies)
                                         :source {:task-source (source-ref (:source task))
                                                  :source-commit (:source-commit task)
                                                  :task-relative-byte-start (alength (.getBytes (subs raw 0 (.start matcher)) "UTF-8"))
                                                  :task-relative-byte-end (alength (.getBytes (subs raw 0 (.end matcher)) "UTF-8"))
                                                  :sha256 (sha256 body)}})))
                             found))]
                 row))
        by-id (into {} (map (juxt :id identity) rows))
        ;; This is the preserved source inventory, never a compiler allowance.
        _ (require! (= 48 (count rows) (count by-id)) "Missing or duplicated preserved Math output")
        _ (doseq [row rows input (:dependencies row)]
            (require! (contains? by-id input) "Unknown literal Math output dependency"))
        graph (into (sorted-map) (map (juxt :id #(set (:dependencies %))) rows))]
    {:authority :literal-source-output-declarations :outputs rows :adjacency graph :cycles (cycles graph)
     :cross-task-inputs
     (vec (for [row rows input (:dependencies row) :let [producer (by-id input)]
                :when (not= (:task-id row) (:task-id producer))]
            {:consumer-task (:task-id row) :consumer-output (:id row)
             :producer-task (:task-id producer) :producer-output input :source (:source row)}))
     :implementation-acceptance :unassessed
     :limits [:literal-dependencies-not-all-prose :proof-method-selected-per-claim
              :output-facts-not-tasks :source-declarations-not-execution]}))

(defn release-source-view [tasks snapshots]
  (let [rows (vec
               (mapcat
                 (fn [task]
                   (let [raw (source-raw (:source task) snapshots)
                         baseline (second (re-find #"(?m)^Evidence baseline: (.+)$" raw))
                         test-id (second (re-find #"\[TEST(\d+)\]" (or baseline "")))
                         matcher (re-matcher #"(Source|Bootstrap criterion) ([a-z0-9/-]+):" raw)
                         starts (loop [out []]
                                  (if (.find matcher)
                                    (recur (conj out {:id (.group matcher 2) :kind (.group matcher 1) :start (.start matcher)})) out))]
                     (mapv
                       (fn [row next-row]
                         (let [tail (or (str/index-of raw " This producer supplies conformance evidence;" (:start row)) (count raw))
                               end (or (:start next-row) tail)
                               body (str/trimr (subs raw (:start row) end))
                               end (+ (:start row) (count body))
                               bootstrap? (= "Bootstrap criterion" (:kind row))
                               allowed? (if bootstrap?
                                          (and (str/ends-with? (:id task) "implement-declared-seed-compiler-and-native-observations")
                                               (contains? #{"bootstrap/native-carrier-header" "bootstrap/native-reference-identity"
                                                            "bootstrap/native-symbol-units" "bootstrap/seed-subset"} (:id row)))
                                          (or (and test-id (str/starts-with? (:id row) (str "testing/test" test-id "/")))
                                              (and (= "13" test-id) (contains? #{"bootstrap/release-supply-chain" "bootstrap/trust-reduction"} (:id row)))))]
                           (require! (and allowed? baseline
                                          (str/includes? body (if bootstrap? "Required outputs:" "Concrete acceptance:")))
                                     "Malformed or misowned Release source criterion")
                           {:id (:id row) :task-id (:id task) :state :unassessed
                            :declaration-kind (if bootstrap? :bootstrap-criterion :test-source-clause)
                            :evidence-baseline baseline :baseline-authority :navigation-not-semantic-entailment
                            :source {:task-source (source-ref (:source task)) :source-commit (:source-commit task)
                                     :task-relative-byte-start (alength (.getBytes (subs raw 0 (:start row)) "UTF-8"))
                                     :task-relative-byte-end (alength (.getBytes (subs raw 0 end) "UTF-8"))
                                     :sha256 (sha256 body)}})) starts (concat (rest starts) [nil])))) tasks))
        _ (require! (= 77 (count rows) (count (set (map :id rows))))
                    "Missing or duplicate preserved Release source criterion")]
    {:authority :literal-resumed-task-criteria :criteria rows :implementation-acceptance :unassessed
     :limits [:source-criteria-not-tasks :baseline-links-not-canonical-entailment
              :runner-not-independent-admission :source-declarations-not-execution]}))

(defn future-task-view
  ([authorities current-tasks snapshots read-text]
   (future-task-view authorities current-tasks snapshots read-text selected-parent-consumers))
  ([authorities current-tasks snapshots read-text required-parent-consumers]
  (let [future-tasks (vec (mapcat val authorities))
        future-map (into {} (map (juxt :id identity) future-tasks))
        all-tasks (merge current-tasks future-map)
        _ (require! (= (set (map second future-task-regions)) (set (keys authorities)))
                    "Missing or unknown future task authority")
        _ (require! (= 40 (count future-tasks) (count future-map)) "Changed future task population")
        _ (require! (= (+ (count current-tasks) (count future-map)) (count all-tasks))
                    "Future task duplicates a current task authority")
        _ (doseq [[component region expected] future-task-regions
                  :let [rows (authorities region) path (str "specs/" component "-workflow/tasks.md")]]
            (require! (= expected (count rows)) "Changed marked future task population")
            (require-population! (set (map :id rows)) (marked-task-ids (read-text path) region)
                                 (str region " live future task"))
            (doseq [row rows]
              (require! (= [component region path :component-future-task-ledger]
                           [(:component row) (:region row) (:path row) (:authority row)])
                        "Future task owner/region mismatch")
              (current-task-body! (:source row) snapshots read-text)
              (let [[_ checked id] (re-find #"^- \[([ xX])\] `([^`]+)`"
                                          (source-raw (:source row) snapshots))]
                (require! (and (= id (:id row)) (= (:checked row) (not= " " checked)))
                          "Future task ID/status differs from authority"))))
        routes (future-dependency-routes (read-text future-dependency-path) (set (keys all-tasks))
                                         (set (keys all-tasks)))
        all-declared (set (map :consumer routes))
        declared (set/intersection all-declared (set (keys future-map)))
        unresolved (vec (sort (set/difference
                               (set/union (set (keys future-map)) (set required-parent-consumers))
                               all-declared)))
        math (math-output-view all-tasks snapshots)
        release (release-source-view (authorities "release-testing-proposals") snapshots)
        named-inputs (group-by (juxt :consumer-task :producer-task) (:cross-task-inputs math))
        output-graph (reduce (fn [graph {:keys [consumer start-results composition-results]}]
                               (assoc graph consumer (set (concat start-results composition-results))))
                             (zipmap (keys all-tasks) (repeat #{})) routes)
        selected (for [{:keys [consumer start-results composition-results source selected-output-contract]} routes
                       producer (concat start-results composition-results)]
                   {:id (str "result:" consumer ":" producer) :consumer consumer :producer producer
                    :source source :selected-output-contract selected-output-contract
                    :named-math-output-inputs (vec (sort (set (map :producer-output (named-inputs [consumer producer])))))
                    :producer-source (source-ref (:source (all-tasks producer)))})
        nodes (merge
                (into {} (for [row (:criteria release)]
                           [(str "source-criterion:" (:id row)) (assoc row :scope :selected-acceptance-facet)]))
                (into {} (for [row (:outputs math)]
                           [(str "output:" (:id row)) (assoc row :scope :selected-acceptance-facet)]))
                (into {} (for [[id row] future-map]
                           [(str "task:" id "/interface")
                            {:state :accepted :scope :declared-interface-only :task-id id :component (:component row)}]))
                (into {} (for [[id row] future-map]
                           [(str "task:" id "/implementation")
                            {:state :unassessed :scope :future-task-implementation :task-id id
                             :dependency-review (if (declared id) :declared :pending)
                             :recorded-checked (:checked row) :component (:component row)}]))
                (into {} (for [[id _] future-map]
                           [(str "task:" id "/composition")
                            {:state :unassessed :scope (if (declared id) :actual-selected-producer-consumer
                                                        :unresolved-future-composition) :task-id id}]))
                (into {} (for [row selected]
                           [(:id row) (assoc (dissoc row :id) :state :unassessed :scope :selected-acceptance-facet
                                            :task-id (:producer row))])))
        edges (vec (concat
                (for [row (:criteria release)]
                  {:from (str "task:" (:task-id row) "/composition") :to (str "source-criterion:" (:id row))
                   :kind :requires-source-criterion-execution-and-refusals :source (:source row)})
                (for [row (:outputs math) input (:dependencies row)]
                  {:from (str "output:" (:id row)) :to (str "output:" input)
                   :kind :literal-math-output-prerequisite :source (:source row)})
                (for [row selected input (:named-math-output-inputs row)]
                  {:from (:id row) :to (str "output:" input)
                   :kind :named-selected-producer-output :source (:source row)})
                (for [[id row] future-map]
                  {:from (str "task:" id "/implementation") :to (str "task:" id "/interface")
                   :kind :declared-interface :source (source-ref (:source row))})
                (for [[id row] future-map]
                  {:from (str "task:" id "/composition") :to (str "task:" id "/implementation")
                   :kind :actual-implementation :source (source-ref (:source row))})
                (mapcat (fn [{:keys [consumer start-results composition-results source]}]
                          (concat
                            (for [producer (concat start-results composition-results)]
                              {:from (str "task:" consumer "/implementation") :to (str "task:" producer "/interface")
                               :kind :declared-producer-interface :source source})
                            (for [producer start-results]
                              {:from (str "task:" consumer "/implementation") :to (str "result:" consumer ":" producer)
                               :kind :adopted-product-contract :source source})
                            (for [producer (concat start-results composition-results)]
                              {:from (str "task:" consumer "/composition") :to (str "result:" consumer ":" producer)
                               :kind :actual-selected-producer-result :source source}))) routes)))]
    {:nodes nodes :typed-edges edges
     :dependency-review {:task-count (count future-map) :declared-consumers (vec (sort declared))
                         :required-parent-consumers (vec (sort required-parent-consumers))
                         :declared-parent-consumers (vec (sort (set/difference all-declared declared)))
                         :unresolved-consumers unresolved :routes routes
                         :math-outputs math
                         :release-source-criteria release
                         :declared-output-adjacency output-graph :declared-output-cycles (cycles output-graph)
                         :authority :source-declarations-not-execution
                         :future-sh-and-full-language-joins :pending
                         :implementation-acceptance :unassessed}})))

(defn finish-line-routes [text task-ids consumers]
  (let [start "<!-- finish-line-dependencies:start -->" end "<!-- finish-line-dependencies:end -->"
        occurrences #(count (re-seq (re-pattern (java.util.regex.Pattern/quote %)) text))
        _ (require! (and (= 1 (occurrences start) (occurrences end)) (< (.indexOf text start) (.indexOf text end)))
                    "Malformed finish-line dependency region")
        lines (vec (remove str/blank? (str/split-lines (subs text (+ (.indexOf text start) (count start)) (.indexOf text end)))))
        _ (require! (= ["| Consumer | Selected producers | Condition | Required result contract |" "| --- | --- | --- | --- |"]
                       (vec (take 2 lines))) "Unknown finish-line dependency syntax")
        rows (mapv (fn [line]
                     (let [cells (str/split line #"\|" -1)
                           _ (require! (and (= 6 (count cells)) (= "" (first cells) (last cells))) "Malformed finish-line row")
                           [consumer producers condition contract] (mapv str/trim (subvec (vec cells) 1 5))
                           id (second (re-matches #"`([^\s`]+)`" consumer))
                           inputs (dependency-list producers #"`([^\s`]+)`" false)]
                       (require! (and (consumers id) (every? task-ids inputs)
                                      (#{"always" "platform-product-adopted"} condition) (not (str/blank? contract)))
                                 "Unknown finish-line consumer, producer or condition")
                       {:consumer id :producers (vec (sort inputs))
                        :condition (when-not (= "always" condition) (keyword condition)) :result-contract contract
                        :source {:path future-dependency-path :sha256 (sha256 text) :declaration line}})) (drop 2 lines))]
    (require! (= (count rows) (count (set (map (juxt :consumer :condition) rows)))) "Duplicate finish-line declaration")
    (require-population! consumers (map :consumer rows) "finish-line consumer")
    rows))

(defn finish-line-view [sources tasks snapshots read-text]
  (let [by-id (into {} (map (juxt :id identity) sources))
        fl-ids full-language-task-ids
        p18-ids (set (map #(format "P18-T%02d" %) (range 7)))
        terminal-sh (set (map #(format "SH-%02d" %) (range 21 30)))
        gates #{"P15-S23" "final-phase-18-completion-gate"}
        contexts #{"full-language-completion-rule" "full-language-staging-contract"}
        consumers (set/union fl-ids p18-ids terminal-sh gates)
        _ (require! (= 56 (count sources) (count by-id)) "Changed finish-line source population")
        _ (require-population! (set/union consumers contexts) (keys by-id) "finish-line source")
        _ (doseq [row sources
                  :let [id (:id row) kind (cond (fl-ids id) :full-language-task (p18-ids id) :phase18-task
                                                             (terminal-sh id) :self-hosting-milestone (gates id) :existing-terminal-gate
                                                             (= id "full-language-completion-rule") :shared-proof-contract
                                                             :else :shared-stage-contract)
                        path (cond (terminal-sh id) milestone-path
                                   (or (p18-ids id) (= id "final-phase-18-completion-gate"))
                                   "docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md"
                                   :else roadmap-path)]]
            (require! (= [kind path] [(:kind row) (:path row)]) "Misowned finish-line source")
            (source-bound! (:source row) snapshots read-text)
            (let [raw (source-raw (:source row) snapshots)
                  status (or (second (re-find #"(?m)^Status: ([^\n]+)" raw))
                             (when-let [[_ checked] (re-find #"^- \[([ xX])\]" raw)]
                               (if (= " " checked) "open" "checked-at-recorded-scope"))
                             "source-declared-only")]
              (require! (= status (:recorded-status row)) "Changed recorded finish-line status")))
        _ (doseq [[path rows] (group-by :path sources)]
            (require! (every? #(= (:sha256 %) (sha256 (read-text path))) rows) "Stale finish-line source file"))
        routes (finish-line-routes (read-text future-dependency-path) (set (keys tasks)) consumers)
        graph (full-language-graph (read-text roadmap-path))
        _ (require-population! fl-ids (keys graph) "full-language dependency")
        staged (merge (staged-graph graph) (staged-graph (zipmap p18-ids (repeat #{}))))
        stage-node (fn [id facet] (str id "/" facet))
        nodes (merge
                (into {} (for [id (set/union fl-ids p18-ids) facet ["interface" "implementation" "composition" "acceptance"]]
                           [(stage-node id facet) {:state (if (= facet "interface") :accepted :unassessed)
                                                  :scope (case facet "interface" :declared-interface-only
                                                                                "implementation" :roadmap-task-implementation
                                                                                "composition" :actual-selected-producer-consumer
                                                                                :current-acceptance)
                                                  :task-id id :recorded-status (:recorded-status (by-id id))
                                                  :source (source-ref (:source (by-id id)))}]))
                (into {} (for [id (set/union gates contexts)]
                           [id {:state (if (= id "full-language-staging-contract") :accepted :unassessed)
                                :scope (if (= id "full-language-staging-contract") :declared-interface-only :uniform-completion-gate)
                                :source (source-ref (:source (by-id id)))}]))
                (into {} (for [id (set/union fl-ids p18-ids)]
                           [(str "proof:" id) {:state :unassessed :scope :uniform-completion-gate
                                               :source (source-ref (:source (by-id id)))}]))
                (into {} (for [{:keys [consumer producers condition result-contract source]} routes producer producers]
                           [(str "finish-result:" consumer ":" producer ":" (or (some-> condition name) "always"))
                            {:state :unassessed :scope :selected-acceptance-facet :consumer consumer :task-id producer
                             :condition condition :result-contract result-contract :source source
                             :producer-source (source-ref (:source (tasks producer)))
                             :spec-kit-owner (str/replace (:path (tasks producer)) #"tasks\.md$" "spec.md")}])) )
        edges (vec (concat
                (for [[from inputs] staged to inputs]
                  {:from from :to to :kind :original-roadmap-stage-prerequisite
                   :source (source-ref (:source (by-id (first (str/split from #"/")))) )})
                (for [id (set/union fl-ids p18-ids)]
                  {:from (stage-node id "acceptance") :to (str "proof:" id) :kind :complete-original-task-proof
                   :source (source-ref (:source (by-id id)))})
                (for [id fl-ids]
                  {:from (stage-node id "acceptance") :to "full-language-completion-rule" :kind :original-full-language-completion-rule
                   :source (source-ref (:source (by-id "full-language-completion-rule")))})
                (mapcat (fn [{:keys [consumer producers condition source]}]
                          (concat
                            (when (or (fl-ids consumer) (p18-ids consumer))
                              (for [producer producers]
                                {:from (stage-node consumer "implementation") :to (str "task:" producer "/interface")
                                 :kind :declared-producer-interface :condition condition :source source}))
                            (for [producer producers]
                              {:from (if (or (fl-ids consumer) (p18-ids consumer)) (stage-node consumer "composition") consumer)
                               :to (str "finish-result:" consumer ":" producer ":" (or (some-> condition name) "always"))
                               :kind :actual-finish-line-producer-result :condition condition :source source}))) routes)
                (for [id ["FL-P15-T01" "FL-P15-T02" "FL-P15-T03"]]
                  {:from "P15-S23" :to (stage-node id "acceptance") :kind :complete-seed-retirement-conjunction
                   :source (source-ref (:source (by-id "P15-S23")))})
                (for [id ["P18-T03" "P18-T05" "P18-T06"]]
                  {:from (stage-node id "acceptance") :to "P15-S23" :kind :actual-p15-retirement-before-final-claim
                   :source (source-ref (:source (by-id id)))})
                (for [to (cons "P15-S23" (map #(stage-node % "acceptance") (sort p18-ids)))]
                  {:from "final-phase-18-completion-gate" :to to :kind :original-phase18-final-conjunction
                   :source (source-ref (:source (by-id "final-phase-18-completion-gate")))})
                (for [to (concat (map #(stage-node % "acceptance") (sort (disj fl-ids "FL-P18-T02")))
                                 ["SH-29" "final-phase-18-completion-gate"])]
                  {:from "FL-P18-T02/acceptance" :to to :kind :complete-project-finish-line-conjunction
                   :source (source-ref (:source (by-id "FL-P18-T02")))})))]
    {:nodes nodes :typed-edges edges :condition-values {:platform-product-adopted :unknown}
     :summary {:authority :source-declarations-not-acceptance :sources sources :routes routes
               :full-language-tasks (vec (sort fl-ids)) :phase18-tasks (vec (sort p18-ids))
               :terminal-sh-milestones (vec (sort terminal-sh)) :existing-terminal-gates (vec (sort gates))
               :final-root "FL-P18-T02/acceptance" :source-entailment :unassessed :implementation-acceptance :unassessed
               :limits [:original-task-authorities-retained :historical-status-not-current-execution
                        :first-seedless-release-not-full-language :selected-results-not-whole-task-start-gates]}}))

(defn task-sh-view [crosswalk read-text]
  (let [requirements (:requirements crosswalk)
        snapshots (:source-snapshots crosswalk)
        source-text #(source-raw % snapshots)
        shared-rule (:shared-completion-rule crosswalk)
        _ (source-bound! (:source shared-rule) snapshots read-text)
        _ (require! (and (= :sh03-and-later-uniform-completion (:id shared-rule))
                         (str/starts-with? (source-text (:source shared-rule))
                                           "## Three boundaries and one shared completion rule"))
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
            (current-task-body! (:source row) snapshots read-text)
            (let [[_ checked id] (re-find #"^- \[([ xX])\] `([^`]+)`" (source-text (:source row)))]
              (require! (and (= id (:id row)) (= (:checked row) (not= " " checked)))
                        "Task status/ID differs from actual authority")
              (when (:checked row)
                (require! (str/includes? (source-text (:source row)) "Accepted;")
                          "Checked task lacks explicit accepted status"))))
        future (future-task-view (:future-task-authorities crosswalk) task-map snapshots read-text)
        finish (finish-line-view (:finish-line-sources crosswalk)
                 (into task-map (for [rows (vals (:future-task-authorities crosswalk)) row rows] [(:id row) row])) snapshots read-text)
        _ (doseq [r requirements]
            (source-bound! (:acceptance-source r) snapshots read-text)
            (source-bound! (:context-source r) snapshots read-text)
            (when-let [history (get-in r [:historical-completion :evidence-source])]
              (source-bound! history snapshots read-text))
            (require! (= (set (:completion-prerequisites r)) (get original-sh (:milestone r)))
                      "SH criterion changed original completion prerequisites")
            (require! (= (:shared-completion-rule r)
                         (when (<= 3 (Long/parseLong (subs (:milestone r) 3))) (:id shared-rule)))
                      "Changed shared completion applicability")
            (require! (or (seq (:task-references r)) (= :missing-task-authority (:coverage r)))
                      "Unmapped SH criterion without explicit gap")
            (doseq [t (:task-references r)]
              (require! (= (:component t) (:component (task-map (:id t)))) "Unknown SH task owner/reference")))
        historical-source-applicability
        (into (sorted-map)
              (for [[path digest] (:source-digests crosswalk)
                    :when (historical-sh-source-path? path)]
                [path (evidence-source-state
                       {:path path
                        :sha256 digest
                        :source-commit (get-in crosswalk [:applying-source-pins path])
                        :current-applicability :unassessed}
                       read-text)]))
        ;; Navigation snapshots and the explicitly historical ownership input
        ;; report drift separately; current task/acceptance inputs stay exact.
        _ (doseq [[path digest] (:source-digests crosswalk)
                  :when (not (or (navigation-source-path? path)
                                 (historical-sh-source-path? path)))]
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
            (source-bound! (:source r) snapshots read-text)
            (let [[_ id _ _ declaration] (re-matches #"\| (SH07-C\d{2}) \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^\n]+)\|"
                                                    (source-text (:source r)))]
              (require! (and (= id (:id r)) (= (str/trim declaration) (:dependency-declaration r)))
                        "SH07 sequence metadata differs from original source")))
        sequence-nodes (into {} (for [r sequence] [(:id r) {:state :unassessed :scope :original-implementation-sequence}]))
        composition-nodes (into {} (for [[id _] task-map]
                                      [(str "task:" id "/composition")
                                       {:state :unassessed :scope :actual-selected-producer-consumer :task-id id}]))
        shared-gate-nodes (into {} (for [n (range 3 30)] [(format "uniform-gate:SH-%02d" n)
                                      {:state :unassessed :scope :uniform-completion-gate :rule (:id shared-rule)}]))
        nodes (merge interface-nodes implementation-nodes criterion-nodes milestone-nodes facet-nodes sequence-nodes composition-nodes shared-gate-nodes (:nodes future) (:nodes finish))
        evidence-applicability (mapv #(evidence-source-state % read-text)
                                    (sort-by (juxt :path :source-commit :sha256)
                                      (distinct (mapcat :evidence-source-locators requirements))))
        conditions (merge {:optimization-requested :unknown :runtime-service-required :unknown
                    :compiler-artifacts-consumed :unknown :build-command-selected :unknown
                    :execution-command-selected :unknown :stable-candidate-selection :unknown} (:condition-values finish))
        edges (vec (concat (:typed-edges future) (:typed-edges finish)
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
     :future-task-dependencies (:dependency-review future)
     :finish-line-composition (:summary finish)
     :condition-values conditions
     :evidence-source-applicability evidence-applicability
     :historical-source-applicability historical-source-applicability
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
