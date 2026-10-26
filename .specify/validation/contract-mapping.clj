(require '[clojure.java.io :as io]
         '[clojure.java.shell :as shell]
         '[clojure.string :as str]
         '[gravity.tooling.strict-json :as json])

;; Read-only documentation check, run from the repository root. No agents,
;; compiler execution, generated results, network calls or semantic acceptance.
(def mapping-path ".specify/contract-mapping.md")
(def mapping (slurp mapping-path))
(def baseline (second (re-find #"Source baseline: `([0-9a-f]{40})`" mapping)))
(def inventory (json/load-json (.toPath (io/file "docs/document-inventory.json"))))
(def gap-ids #{"GOV1" "GOV6" "GOV7" "GOV8"})
(def added-ids #{"D5" "D6" "L7" "L19" "PERF1" "PERF2" "PERF3" "PERF4"
                 "PERF6" "PERF7" "PERF8" "TEST1" "TEST3" "TEST6" "TEST13"
                 "GOV2" "GOV5" "GOV10"})
(defn check! [condition message]
  (when-not condition (throw (ex-info message {}))))
(defn git [& args]
  (let [{:keys [exit out err]} (apply shell/sh "git" args)]
    (check! (zero? exit) (str "Git read failed: " err))
    out))
(defn markdown-targets [text]
  (map second (re-seq #"\]\(([^)]+)\)" text)))
(defn slug [text]
  (-> text str/lower-case (str/replace #"[^a-z0-9 _-]" "")
      (str/replace " " "-")))
(defn anchors [text]
  (:anchors
   (reduce (fn [{:keys [counts anchors]} [_ heading]]
             (let [base (slug heading) n (get counts base 0)]
               {:counts (assoc counts base (inc n))
                :anchors (conj anchors (str base (when (pos? n) (str "-" n))))}))
           {:counts {} :anchors #{}}
           (re-seq #"(?m)^#{1,6} (.+)$" text))))
(def root (.getCanonicalFile (io/file ".")))
(defn resolve-link [path target]
  (let [[file anchor] (str/split target #"#" 2)
        resolved (.getCanonicalFile
                  (if (str/blank? file) (io/file path)
                      (io/file (.getParentFile (io/file path)) file)))]
    (and (.startsWith (.toPath resolved) (.toPath root))
         (.exists resolved)
         (or (nil? anchor)
             (and (.isFile resolved)
                  (contains? (anchors (slurp resolved)) anchor))))))
(defn local-target? [target]
  (not (re-find #"^[a-zA-Z][a-zA-Z0-9+.-]*:" target)))
(def specs-package-paths
  (vec (sort (for [f (file-seq (io/file "specs")) :when (.isFile f)] (.getPath f)))))
(def historical-package-paths
  (vec (sort (for [pkg ["naming-pilot" "reader-core-invocation"]
                   artifact ["spec" "plan" "tasks" "evidence"]]
               (str ".specify/" pkg "/" artifact ".md")))))
(def package-paths (vec (sort (concat specs-package-paths historical-package-paths))))
(def baseline-package-paths
  (vec (sort (concat historical-package-paths
                     (str/split-lines (git "ls-tree" "-r" "--name-only" baseline "--" "specs"))))))
(def current-text (into {} (map (fn [p] [p (slurp p)]) package-paths)))
(def baseline-text
  (into {} (map (fn [p] [p (git "show" (str baseline ":" p))]) baseline-package-paths)))
(defn package-refs [texts d]
  (set (for [[path text] texts
             target (markdown-targets text)
             :when (= (str "../../" (get d "path")) (first (str/split target #"#")))]
         (second (str/split path #"/")))))
(def row-lines (vec (filter #(re-find #"^\| \[[A-Z]+[0-9]+\]\(" %) (str/split-lines mapping))))
(defn parse-row [line]
  (let [[_ id source criteria interfaces basis disposition]
        (re-matches #"\| \[([A-Z]+[0-9]+)\]\(([^)]+)\) / \[criteria\]\(([^)]+)\) \| (.*?) \| (.*?) \| (.*?) \|" line)]
    (check! id (str "Malformed mapping row: " line))
    {:id id :source source :criteria criteria :basis basis :disposition disposition
     :packages (set (map second (re-seq #"\[([^]]+)\]\(#package-[^)]+\)" interfaces)))}))
(def rows (mapv parse-row row-lines))
(defn validate-rows! [rs]
  (check! (= 240 (count rs) (count (set (map :id rs)))) "Missing or duplicate contract ID")
  (check! (= (mapv #(get % "id") inventory) (mapv :id rs)) "Inventory order/ID mismatch")
  (doseq [[d row] (map vector inventory rs)]
    (let [id (get d "id") refs (package-refs current-text d)]
      (check! (= (:source row) (str "../" (get d "path") "#requirements")) (str id " source mismatch"))
      (check! (= (:criteria row) (str "../" (get d "path") "#conformance-criteria")) (str id " acceptance mismatch"))
      (check! (not (str/blank? (:basis row))) (str id " missing rationale"))
      (check! (= refs (:packages row)) (str id " package routes differ from actual links"))
      (check! (= (:disposition row) (cond (contains? gap-ids id) "Gap"
                                         (contains? added-ids id) "Added"
                                         (contains? #{"L9" "L11" "L12" "L16" "L18" "SAFE12" "SAFE14" "PERF10"} id) "Extended" :else "Retained"))
              (str id " false disposition"))
      (check! (= (empty? refs) (contains? gap-ids id)) (str id " unrecorded package gap")))))
(defn rejected? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(defn validate-registry! [text]
  (let [sections (rest (str/split text #"(?m)^### Package "))
        entries (into {} (map #(str/split % #"\n" 2) sections))
        packages (set (map #(second (str/split % #"/")) package-paths))]
    (check! (= packages (set (keys entries))) "Package accountability registry mismatch")
    (doseq [pkg packages :when (str/ends-with? pkg "-workflow")]
      (check! (str/includes? (first (str/split (get entries pkg) #"(?m)^## "))
                            "Accountable component role:")
              (str pkg " incorrectly classified as a historical finite increment")))))

(check! (empty? *command-line-args*) "No arguments supported")
(check! (= package-paths baseline-package-paths) "Package files added/removed")
(check! (= 116 (count specs-package-paths)) "Expected 29 specs-only four-artifact packages")
(check! (= 124 (count package-paths)) "Expected 31 total packages including two historical .specify packages")
(doseq [p historical-package-paths]
  (check! (= (get current-text p) (get baseline-text p)) (str "Historical package changed: " p)))
(doseq [[pkg paths] (group-by #(second (str/split % #"/")) package-paths)]
  (check! (= #{"spec.md" "plan.md" "tasks.md" "evidence.md"}
             (set (map #(last (str/split % #"/")) paths))) (str pkg " artifact set mismatch")))
(def changed-plans
  (vec (for [p package-paths :when (not= (get current-text p) (get baseline-text p))] p)))
(check! (= 15 (count changed-plans)) "Expected 15 minimally extended package plans")
(doseq [p changed-plans]
  (check! (and (str/ends-with? p "/plan.md")
               (str/starts-with? (get current-text p)
                                (str (get baseline-text p) "\n## Additional contract interfaces\n")))
          (str "Existing package text changed: " p)))
(def allowed (set (concat changed-plans [mapping-path ".specify/validation/contract-mapping.clj"])))
(def changed (set (remove str/blank? (str/split-lines (git "diff" "--name-only" baseline)))))
(def untracked (set (remove str/blank? (str/split-lines (git "ls-files" "--others" "--exclude-standard")))))
(check! (every? allowed (concat changed untracked)) "Change outside mapping scope")
(validate-rows! rows)
(validate-registry! mapping)
(check! (= 218 (count (filter #(seq (package-refs baseline-text %)) inventory))) "Baseline coverage drift")
(check! (= 236 (count (filter #(seq (package-refs current-text %)) inventory))) "Candidate coverage drift")
(def paths-to-check (concat [mapping-path] changed-plans historical-package-paths))
(def links (for [p paths-to-check target (markdown-targets (slurp p)) :when (local-target? target)] [p target]))
(doseq [p (conj (vec paths-to-check) ".specify/validation/contract-mapping.clj")]
  (check! (every? #(<= (int %) 127) (slurp p)) (str "Non-ASCII: " p)))
(doseq [[p target] links] (check! (resolve-link p target) (str "Broken local path/heading: " p " -> " target)))
(check! (rejected? #(validate-rows! (pop rows))) "Missing-ID negative control failed")
(check! (rejected? #(validate-rows! (assoc rows 1 (first rows)))) "Duplicate-ID negative control failed")
(check! (rejected? #(validate-rows! (assoc-in rows [0 :disposition] "Added"))) "False-disposition negative control failed")
(check! (not (resolve-link mapping-path "../specs/absent-contract-fixture/spec.md")) "Missing-file negative control failed")
(check! (not (resolve-link mapping-path "contract-mapping.md#absent-contract-fixture")) "Missing-heading negative control failed")
(check! (rejected? #(validate-registry!
                    (str/replace mapping "Accountable component role: Diagnostic infrastructure maintainer."
                                 "Accountability: historical finite increment.")))
        "Workflow misclassification negative control failed")
(prn {:scope :contract-navigation-only :source-baseline baseline
      :contracts 240 :specs-only-packages 29 :specs-only-artifacts 116
      :historical-specify-packages 2 :historical-specify-artifacts 8
      :total-packages 31 :total-package-artifacts 124
      :baseline-linked 218 :candidate-linked 236 :explicit-gaps (sort gap-ids)
      :newly-linked 18 :retained 218 :accepted-replacements 0
      :local-path-and-heading-checks (count links) :negative-controls 6
      :preserved-package-files (- 124 (count changed-plans)) :append-only-plans (count changed-plans)
      :independent-review :pending :implementation-acceptance :unassessed})
(shutdown-agents)
