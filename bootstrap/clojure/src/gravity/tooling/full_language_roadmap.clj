(ns gravity.tooling.full-language-roadmap
  "Fail-closed validation for full-language roadmap completion claims."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.tooling.strict-json :as json])
  (:import (java.nio.file Files LinkOption Path Paths)))

(def ^:private root
  (.normalize (.toAbsolutePath (Paths/get "" (make-array String 0)))))
(def ^:private docs (.resolve root "docs"))
(def ^:private roadmap (.resolve docs "full-language-implementation-gap-map.md"))
(def ^:private matrix (.resolve docs "artifacts/full-language/coverage/full-language-coverage-matrix.json"))
(def ^:private gaps (.resolve docs "artifacts/full-language/coverage/full-language-coverage-gaps.json"))
(def ^:private fixture-root (.resolve root "tools/fixtures/full_language_roadmap"))
(def ^:private final-seed-artifact
  (.resolve docs "artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn"))
(def ^:private final-seed-report
  (.resolve docs "artifacts/phase-15/reports/p15-s23-final-seed-retirement-proof-report.md"))
(def ^:private bootstrap-doc (.resolve docs "bootstrap/clojure-bootstrap.md"))
(def ^:private p15-roadmap (.resolve docs "phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md"))

(def ^:private task-pattern
  #"^- \[([ xX])\] `(FL-P\d{2}-T\d{2})` (.+)$")
(def ^:private ledger-pattern
  #"^\|\s*(\d{4}-\d{2}-\d{2})\s*\|\s*[^|]+\|\s*`(FL-P\d{2}-T\d{2})`\s*\|([^|]+)\|([^|]+)\|")
(def ^:private command-pattern
  #"\b(gravity|bin/gravity|clojure -M(?::gravity| tools/))[^`;|]*")

(def ^:private p00-required-tokens
  {"FL-P00-T00" ["public binary audit" "bin/gravity check"
                   "accepted fixture audit" "rejected fixture audit"
                   "feature-specific public diagnostics"]
   "FL-P00-T01" ["validate_full_language_roadmap.clj"
                   "full-language-coverage-matrix.json"
                   "full-language-coverage-gaps.json" "--self-test"
                   "coverage matrix"]
   "FL-P00-T02" ["validate_full_language_roadmap.clj"
                   "validate_gravity_docs.clj" "--self-test" "overclaim"
                   "reject"]})
(def ^:private non-p00-required
  ["accepted" "rejected" "diagnostic" "artifact" "provenance"])
(def ^:private simulated-only
  ["scaffold-only" "simulated proof" "generated manifest only"
   "proof metadata only"])
(def ^:private incomplete-markers
  [":status :incomplete"
   ":full-language-compiler-self-hosted? false"
   ":clojure-seed-retired? false"
   ":clojure-seed-boundary? true"])
(def ^:private forbidden-incomplete-claims
  {"p15-s23 final seed-retirement report"
   [#"(?s)Status:\s*complete"
    #"`:full-language-compiler-self-hosted\?\s+true`"
    #"`:clojure-seed-retired\?\s+true`"
    #"`:clojure-seed-boundary\?\s+false`"
    #"emitted status `:complete`"
    #"(?s)next required\s+capability `:advance_to_phase_16`"]
   "bootstrap clojure guide"
   [#"(?s)It emits status `:complete`, records all required P15-S23 evidence"
    #"(?s)accepts\s+the `:p15-s23-final-seed-retirement` candidate"
    #"(?s)records\s+`:full-language-compiler-self-hosted\?\s+true`,\s+`:clojure-seed-retired\?\s+true`,\s+and `:clojure-seed-boundary\?\s+false`"
    #"(?s)final seed-retirement proof now\s+completes the P15-S23 gate"]
   "phase 15 roadmap"
   [#"(?s)Status:\s*complete;\s*fail-closed evidence gate records whole-language compiler\s+self-hosting and Clojure seed retirement proof"]})

(defn- exists? [^Path path]
  (Files/exists path (make-array LinkOption 0)))

(defn- read-text [^Path path]
  (slurp (.toFile path) :encoding "UTF-8"))

(defn parse-tasks [text]
  (reduce
   (fn [tasks [index line]]
     (if-let [[_ mark task-id title] (re-matches task-pattern line)]
       (if (contains? tasks task-id)
         (throw (ex-info (str "duplicate task id " task-id " at line " index)
                         {:diagnostic "FLR001" :task-id task-id :line index}))
         (assoc tasks task-id {:id task-id
                               :line index
                               :complete (= "x" (str/lower-case mark))
                               :title (str/trim title)}))
       tasks))
   {}
   (map-indexed (fn [index line] [(inc index) line]) (str/split-lines text))))

(defn parse-ledger [text]
  (reduce
   (fn [rows [index line]]
     (if-let [[_ date task-id evidence result] (re-find ledger-pattern line)]
       (assoc rows task-id {:line index
                            :date date
                            :evidence (str/trim evidence)
                            :result (str/trim result)
                            :combined (str/trim (str evidence " " result))})
       rows))
   {}
   (map-indexed (fn [index line] [(inc index) line]) (str/split-lines text))))

(defn- phase-number [task-id]
  (Long/parseLong (subs (second (str/split task-id #"-")) 1)))

(defn validate-matrix
  ([] (validate-matrix matrix gaps))
  ([^Path matrix-path ^Path gaps-path]
   (cond-> []
     (not (exists? matrix-path))
     (conj (str "missing coverage matrix " (.relativize root matrix-path)))
     (not (exists? gaps-path))
     (conj (str "missing coverage gap report " (.relativize root gaps-path)))
     (and (exists? matrix-path) (exists? gaps-path))
     (into
      (let [matrix-data (json/load-json matrix-path)
            gaps-data (json/load-json gaps-path)
            entries (get matrix-data "entries" [])
            complete (filter #(true? (get % "fullLanguageComplete")) entries)]
        (concat
         (when-not (and (= 240 (get matrix-data "inventoryCount"))
                        (= 240 (count entries)))
           ["coverage matrix must enumerate exactly 240 normative documents"])
         (when-not (= 240 (get gaps-data "inventoryCount"))
           ["coverage gap report must record inventoryCount 240"])
         (when-not (= (count complete)
                      (get-in matrix-data ["summary" "fullLanguageCompleteDocuments"]))
           ["coverage matrix fullLanguageCompleteDocuments does not match entries"])
         (mapcat
          (fn [entry]
            (let [id (get entry "id")]
              (cond-> []
                (seq (get entry "gaps"))
                (conj (str id " is fullLanguageComplete while gaps remain"))
                (true? (get entry "scaffoldOnlyCoverage"))
                (conj (str id " is fullLanguageComplete from scaffold-only coverage"))
                (not (true? (get entry "publicAccepted")))
                (conj (str id " is fullLanguageComplete without public accepted proof"))
                (not (true? (get entry "publicRejectedSpecific")))
                (conj (str id " is fullLanguageComplete without public rejected diagnostic proof")))))
          complete)))))))

(defn seed-retirement-truth-errors [artifact-text documents]
  (if-not (every? #(str/includes? artifact-text %) incomplete-markers)
    []
    (mapcat
     (fn [[label patterns]]
       (if-let [text (get documents label)]
         (when (some #(re-find % text) patterns)
           [(str label " claims P15 final seed retirement is complete while the artifact is incomplete")])
         [(str "missing " label " for P15 final seed-retirement truth check")]))
     forbidden-incomplete-claims)))

(defn validate-seed-retirement-truth []
  (let [paths {"P15 final seed-retirement artifact" final-seed-artifact
               "p15-s23 final seed-retirement report" final-seed-report
               "bootstrap clojure guide" bootstrap-doc
               "phase 15 roadmap" p15-roadmap}
        missing (for [[label path] paths :when (not (exists? path))]
                  (str "missing " label " " (.relativize root path)))]
    (if (seq missing)
      (vec missing)
      (seed-retirement-truth-errors
       (read-text final-seed-artifact)
       {"p15-s23 final seed-retirement report" (read-text final-seed-report)
        "bootstrap clojure guide" (read-text bootstrap-doc)
        "phase 15 roadmap" (read-text p15-roadmap)}))))

(defn validate-completion-claims [text]
  (let [tasks (parse-tasks text)
        ledger (parse-ledger text)]
    (if (empty? tasks)
      ["no full-language tasks found"]
      (vec
       (mapcat
        (fn [[task-id task]]
          (cond
            (not (:complete task)) []
            (not (contains? ledger task-id))
            [(str task-id " is complete without an evidence-ledger row")]
            :else
            (let [row (get ledger task-id)
                  combined (str/lower-case (:combined row))]
              (concat
               (when-not (re-find command-pattern (:combined row))
                 [(str task-id " evidence row lacks command evidence")])
               (if (zero? (phase-number task-id))
                 (for [token (get p00-required-tokens task-id [])
                       :when (not (str/includes? combined (str/lower-case token)))]
                   (str task-id " evidence row lacks required audit token `" token "`"))
                 (concat
                  (for [term non-p00-required
                        :when (not (str/includes? combined term))]
                    (str task-id " evidence row lacks `" term "` evidence"))
                  (when (some #(str/includes? combined %) simulated-only)
                    [(str task-id " appears to rely on scaffold/proof-only evidence")])
                  (when (str/starts-with? task-id "FL-P18-")
                    (for [term ["gravity check" "gravity run" "gravity compile"]
                          :when (not (str/includes? combined term))]
                      (str task-id " final release evidence lacks `" term "`")))
                  (when (and (contains? #{"FL-P15-T03" "FL-P18-T02"} task-id)
                             (not (str/includes? combined ":clojure-seed-boundary? false")))
                    [(str task-id " lacks final Clojure seed-boundary proof")])))))))
        tasks)))))

(defn validate-text
  ([text] (validate-text text true))
  ([text check-matrix?]
   (vec (concat (validate-completion-claims text)
                (when check-matrix? (validate-matrix))
                (when check-matrix? (validate-seed-retirement-truth))))))

(defn validate-current []
  (when-not (exists? roadmap)
    (throw (ex-info (str "missing " (.relativize root roadmap))
                    {:diagnostic "FLR001"})))
  (let [errors (validate-text (read-text roadmap) true)]
    (when (seq errors)
      (throw (ex-info (str/join "; " errors)
                      {:diagnostic "FLR002" :errors errors})))
    {:tasks (count (parse-tasks (read-text roadmap)))}))

(defn- fixture-files [kind]
  (let [directory (.toFile (.resolve fixture-root kind))]
    (->> (or (.listFiles directory) (make-array java.io.File 0))
         (filter #(and (.isFile %) (str/ends-with? (.getName %) ".md")))
         (sort-by #(.getName %)))))

(defn self-test []
  (let [accepted (fixture-files "accepted")
        rejected (fixture-files "rejected")]
    (when (or (empty? accepted) (empty? rejected))
      (throw (ex-info "full-language roadmap validation fixtures are missing"
                      {:diagnostic "FLR003"})))
    (doseq [file accepted]
      (let [errors (validate-text (slurp file) false)]
        (when (seq errors)
          (throw (ex-info (str "accepted fixture failed " (.getPath file))
                          {:diagnostic "FLR004" :errors errors})))))
    (doseq [file rejected]
      (let [errors (validate-text (slurp file) false)]
        (when (empty? errors)
          (throw (ex-info (str "rejected fixture unexpectedly passed " (.getPath file))
                          {:diagnostic "FLR005"}))))
      )
    (let [stale (seed-retirement-truth-errors
                 (str/join "\n" incomplete-markers)
                 {"p15-s23 final seed-retirement report" "Status: complete\n`:clojure-seed-retired? true`"
                  "bootstrap clojure guide" "It emits status `:complete`, records all required P15-S23 evidence, accepts the `:p15-s23-final-seed-retirement` candidate"
                  "phase 15 roadmap" "Status: complete; fail-closed evidence gate records whole-language compiler self-hosting and Clojure seed retirement proof"})]
      (when-not (= 3 (count stale))
        (throw (ex-info "P15 final seed-retirement overclaim self-test did not reject stale claims"
                        {:diagnostic "FLR006" :errors stale}))))
    {:accepted (count accepted) :rejected (count rejected)}))
