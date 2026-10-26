(ns acceptance-dependencies
  "Read-only roadmap dependency audit. Graph edges point to prerequisites.
  No compiler execution, status mutation, receipt authentication or acceptance."
  (:require [clojure.string :as str]
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
                            :else (let [refs (set (re-seq #"FL-P\d{2}-T\d{2}" declaration))]
                                    (require! (and (seq refs)
                                                   (re-matches #"[\s`,.FLPT0-9-]+" declaration))
                                              (str "Unknown dependency syntax: " declaration))
                                    refs))]
                      (map (fn [id] [id dependencies]) ids))) phases))))

(defn milestone-graph [text]
  (let [rows (re-seq #"(?m)^\| \[?(SH-\d{2})\]?[^|]*\|[^|]*\| ([^|]+)\|[^|]*\|$" text)
        ids (mapv second rows)]
    (require! (and (= (set (map #(format "SH-%02d" %) (range 30))) (set ids))
                   (= (count ids) (count (set ids))))
              "Missing or duplicate milestone rows")
    (into (sorted-map)
          (for [[_ id dependencies] rows]
            [id (set (re-seq #"SH-\d{2}" dependencies))]))))

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
              ids (mapv second (re-seq #"(?m)^- \[[ xX]\] `([^`]+)`" region))]
          (require! (and (seq ids) (= (count ids) (count (set ids)))
                         (every? #(str/starts-with? % (str component "/")) ids))
                    (str "Missing, duplicate or foreign implementation IDs: " component))
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
     :retained-spec-kit (retained-packages (slurp ".specify/contract-mapping.md") inventory slurp)
     :limits [:no-semantic-acceptance :no-task-status-change :no-receipt-authentication
              :interfaces-do-not-satisfy-composition :proof-gates-remain-required]}))

(defn run [args]
  (require! (= ["--check"] (vec args)) "Usage: clojure -M tools/acceptance_dependencies.clj --check")
  (prn (audit)))

(when (seq *command-line-args*) (run *command-line-args*))
