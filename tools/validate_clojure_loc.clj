(ns gravity.clojure-source-loc
  "Fail-closed physical LOC policy for bootstrap Clojure implementation files."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)
           (java.nio.file Path)))

(def ^:private baseline-path "contracts/clojure-source-loc-baseline.edn")
(def ^:private baseline-schema :gravity/clojure-source-loc-baseline-v1)
(def ^:private report-schema :gravity/clojure-source-loc-report-v1)
(def ^:private required-keys
  #{:schema :maximum-physical-lines :source-roots :extensions :oversized-files})

(defn- issue [id message data]
  (assoc data :id id :message message))

(defn- normalized-relative-path? [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not (some #{".."} (str/split value #"/")))
       (= value (str/replace value #"/+" "/"))))

(defn- source-extension? [extensions path]
  (boolean (some #(str/ends-with? path %) extensions)))

(defn- configured-source-path? [roots extensions path]
  (and (normalized-relative-path? path)
       (source-extension? extensions path)
       (some #(or (= path %)
                  (str/starts-with? path (str % "/")))
             roots)))

(defn- configuration-errors [baseline]
  (let [roots (:source-roots baseline)
        extensions (:extensions baseline)
        exceptions (:oversized-files baseline)]
    (vec
     (concat
      (when-not (and (map? baseline) (= required-keys (set (keys baseline))))
        [(issue "LOC001" "baseline must have the closed required shape" {})])
      (when-not (= baseline-schema (:schema baseline))
        [(issue "LOC001" "baseline schema is unknown" {:schema (:schema baseline)})])
      (when-not (and (integer? (:maximum-physical-lines baseline))
                     (pos? (:maximum-physical-lines baseline)))
        [(issue "LOC001" "maximum physical lines must be a positive integer" {})])
      (when-not (and (vector? roots) (seq roots)
                     (= roots (vec (sort roots)))
                     (= (count roots) (count (distinct roots)))
                     (every? normalized-relative-path? roots))
        [(issue "LOC001" "source roots must be sorted safe relative paths" {})])
      (when-not (and (vector? extensions) (seq extensions)
                     (= extensions (vec (sort extensions)))
                     (= (count extensions) (count (distinct extensions)))
                     (every? #(and (string? %) (re-matches #"\.[a-z0-9]+" %))
                             extensions))
        [(issue "LOC001" "extensions must be sorted unique Clojure suffixes" {})])
      (when-not (map? exceptions)
        [(issue "LOC001" "oversized files must be a path-to-line-count map" {})])
      (when (map? exceptions)
        (mapcat
         (fn [[path maximum]]
           (cond
             (not (configured-source-path? (or roots []) (or extensions []) path))
             [(issue "LOC001" "baseline path is outside configured Clojure source roots"
                     {:path path})]

             (not (and (integer? maximum)
                       (> maximum (or (:maximum-physical-lines baseline) 0))))
             [(issue "LOC001" "baseline line count must exceed the policy maximum"
                     {:path path :maximum maximum})]

             :else []))
         exceptions))))))

(defn- relative-path [^File root ^File file]
  (-> (.relativize (.toPath root) (.toPath file))
      str
      (str/replace File/separator "/")))

(defn- as-file [value]
  (if (instance? Path value)
    (.toFile ^Path value)
    (io/file value)))

(defn- physical-line-count [file]
  (with-open [reader (io/reader file)]
    (count (line-seq reader))))

(defn- source-files [root roots extensions]
  (->> roots
       (map #(io/file root %))
       (filter #(.isDirectory ^File %))
       (mapcat file-seq)
       (filter #(.isFile ^File %))
       (map #(vector (relative-path root %) %))
       (filter #(source-extension? extensions (first %)))
       (into (sorted-map))))

(defn- source-root-errors [root roots]
  (->> roots
       (remove #(.isDirectory ^File (io/file root %)))
       (mapv #(issue "LOC002" "configured source root does not exist" {:path %}))))

(defn validate-tree
  "Returns a deterministic LOC report for ROOT and a decoded BASELINE map.

  A baseline exception must represent a currently oversized source file and
  may only shrink. Source files absent from the baseline must not exceed the
  configured maximum."
  [root baseline]
  (let [configuration (configuration-errors baseline)]
    (if (seq configuration)
      {:schema report-schema
       :status :failed
       :violations configuration}
      (let [root (as-file root)
            roots (:source-roots baseline)
            extensions (:extensions baseline)
            maximum (:maximum-physical-lines baseline)
            exceptions (:oversized-files baseline)
            root-errors (source-root-errors root roots)
            files (source-files root roots extensions)
            counts (into (sorted-map)
                         (map (fn [[path file]] [path (physical-line-count file)])
                              files))
            exception-errors
            (mapcat
             (fn [[path recorded]]
               (let [actual (get counts path)]
                 (cond
                   (nil? actual)
                   [(issue "LOC002" "baseline source file is missing" {:path path})]

                   (<= actual maximum)
                   [(issue "LOC004" "baseline exception is stale; remove it"
                           {:path path :actual actual :maximum maximum})]

                   (> actual recorded)
                   [(issue "LOC005" "baselined source file grew"
                           {:path path :actual actual :baseline recorded})]

                   :else [])))
             exceptions)
            new-errors
            (for [[path actual] counts
                  :when (and (> actual maximum) (not (contains? exceptions path)))]
              (issue "LOC003" "source file exceeds the LOC limit without a baseline"
                     {:path path :actual actual :maximum maximum}))
            violations (vec (concat root-errors exception-errors new-errors))]
        {:schema report-schema
         :status (if (seq violations) :failed :passed)
         :maximum-physical-lines maximum
         :source-file-count (count counts)
         :baseline-count (count exceptions)
         :violations violations}))))

(defn read-baseline
  "Reads a data-only LOC baseline without evaluating reader forms."
  ([root] (read-baseline root baseline-path))
  ([root path]
   (let [file (io/file (as-file root) path)]
     (try
       (edn/read-string {:readers {}} (slurp file))
       (catch Throwable error
         (throw (ex-info "LOC baseline could not be read"
                         {:id "LOC001" :path path}
                         error)))))))

(defn validate-repository
  "Validates the canonical baseline from ROOT, defaulting to the current tree."
  ([] (validate-repository "."))
  ([root] (validate-tree root (read-baseline root))))

(defn- parse-options [arguments]
  (if (empty? arguments)
    {:root "."}
    (if (and (= ["--root" (second arguments)] arguments)
             (string? (second arguments)))
      {:root (second arguments)}
      (throw (ex-info "usage: clojure -M tools/validate_clojure_loc.clj [--root PATH]"
                      {:id "LOC001" :arguments arguments})))))

(defn -main [& arguments]
  (try
    (let [{:keys [root]} (parse-options arguments)
          report (validate-repository root)]
      (if (= :passed (:status report))
        (do
          (println (str "Clojure LOC validation passed: " (:source-file-count report)
                        " source files, " (:baseline-count report)
                        " baselined exceptions, maximum "
                        (:maximum-physical-lines report) " physical lines"))
          0)
        (do
          (binding [*out* *err*]
            (doseq [violation (:violations report)]
              (println (:id violation) (:message violation) (dissoc violation :id :message))))
          1)))
    (catch clojure.lang.ExceptionInfo error
      (binding [*out* *err*]
        (println (or (:id (ex-data error)) "LOC001") (.getMessage error)))
      2)))

(when-not (= "true" (System/getProperty "gravity.clojure-source-loc.library"))
  (System/exit (int (apply -main *command-line-args*))))
