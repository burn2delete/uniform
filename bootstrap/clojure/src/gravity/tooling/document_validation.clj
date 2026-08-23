(ns gravity.tooling.document-validation
  "Structural validation for the normative Gravity document set."
  (:require [clojure.string :as str]
            [gravity.tooling.full-language-roadmap :as roadmap]
            [gravity.tooling.strict-json :as json])
  (:import (java.nio ByteBuffer)
           (java.nio.charset CharacterCodingException CodingErrorAction StandardCharsets)
           (java.nio.file Files LinkOption Path Paths)))

(def ^:private forbidden-pattern
  #"(?i)\b(?:TODO|TBD|FIXME|placeholder|lorem ipsum)\b")
(def ^:private required-section-groups
  [["## Purpose"]
   ["## Requirements" "## Document-Specific Rules" "## Concrete Requirements"]
   ["## Dependencies" "## Semantic Dependencies"]
   ["## Outputs and Artifacts" "## Artifact Expectations" "## Required Outputs"]
   ["## Conformance Criteria" "## Conformance Checks" "## Acceptance Criteria"]])

(defn- fail! [diagnostic message data]
  (throw (ex-info message (assoc data :diagnostic diagnostic))))

(defn- exists? [^Path path]
  (Files/exists path (make-array LinkOption 0)))

(defn- strict-utf8 [^Path path]
  (let [bytes (Files/readAllBytes path)
        decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (fail! "DOC010" (str "invalid UTF-8 text in " path)
               {:path (str path) :cause (.getMessage exception)})))))

(defn- assert-ascii! [^Path path]
  (let [text (strict-utf8 path)]
    (when-let [entry (first (keep-indexed (fn [index character]
                                            (when (> (int character) 127)
                                              [index (int character)]))
                                          text))]
      (fail! "DOC011" (str "non-ASCII text in " path)
             {:path (str path) :offset (first entry) :codepoint (second entry)}))))

(defn- heading-lines [text]
  (->> (str/split-lines text)
       (filter #(str/starts-with? % "## "))
       set))

(defn- resolve-inventory-path [^Path root ^Path docs-root path-text]
  (when-not (and (string? path-text)
                 (str/starts-with? path-text "docs/")
                 (str/ends-with? path-text ".md"))
    (fail! "DOC005" "inventory path must be a docs-relative Markdown path"
           {:path path-text}))
  (let [resolved (.normalize (.resolve root path-text))]
    (when-not (.startsWith resolved docs-root)
      (fail! "DOC005" "inventory path escapes docs/" {:path path-text}))
    resolved))

(defn- validate-entry! [^Path root ^Path docs-root entry expected-sequence]
  (when-not (map? entry)
    (fail! "DOC002" "inventory entry must be an object"
           {:sequence expected-sequence}))
  (doseq [key ["sequence" "phase" "phaseName" "id" "title" "path" "category"]]
    (when-not (contains? entry key)
      (fail! "DOC002" (str "inventory entry is missing " key)
             {:sequence expected-sequence :key key})))
  (when-not (= expected-sequence (get entry "sequence"))
    (fail! "DOC003" "document sequence must be exactly 1..240"
           {:expected expected-sequence :actual (get entry "sequence")}))
  (when-not (and (string? (get entry "id"))
                 (not (str/blank? (get entry "id"))))
    (fail! "DOC002" "document id must be a nonempty string"
           {:sequence expected-sequence}))
  (let [path (resolve-inventory-path root docs-root (get entry "path"))]
    (when-not (exists? path)
      (fail! "DOC006" (str "missing document " path) {:path (str path)}))
    (when-not (.startsWith (.toRealPath path (make-array LinkOption 0))
                           (.toRealPath docs-root (make-array LinkOption 0)))
      (fail! "DOC005" "inventory path resolves outside docs/"
             {:path (get entry "path")}))
    (let [text (strict-utf8 path)
          headings (heading-lines text)]
      (when (< (count (str/split-lines text)) 80)
        (fail! "DOC007" (str "document is too thin: " path)
               {:path (str path)}))
      (when (re-find forbidden-pattern text)
        (fail! "DOC008" (str "forbidden placeholder marker in " path)
               {:path (str path)}))
      (doseq [group required-section-groups]
        (when-not (some headings group)
          (fail! "DOC009" (str "missing section equivalent to " (first group)
                                " in " path)
                 {:path (str path) :accepted-headings group}))))
    path))

(defn- phase-readmes [^Path docs-root]
  (->> (.listFiles (.toFile docs-root))
       (filter #(and (.isDirectory %)
                     (str/starts-with? (.getName %) "phase-")))
       (map #(.resolve (.toPath %) "README.md"))
       (filter exists?)
       sort
       vec))

(defn- files-with-extension [^Path directory extensions]
  (if-not (exists? directory)
    []
    (->> (file-seq (.toFile directory))
         (filter #(.isFile %))
         (filter #(some (fn [extension] (str/ends-with? (.getName %) extension))
                        extensions))
         (map #(.toPath %)))))

(defn- direct-files-with-extension [^Path directory extensions]
  (if-not (exists? directory)
    []
    (->> (or (.listFiles (.toFile directory)) (make-array java.io.File 0))
         (filter #(.isFile %))
         (filter #(some (fn [extension] (str/ends-with? (.getName %) extension))
                        extensions))
         (map #(.toPath %)))))

(defn validate-repository
  ([] (validate-repository (.normalize (.toAbsolutePath (Paths/get "" (make-array String 0)))) true))
  ([^Path root run-roadmap?]
   (let [root (.normalize (.toAbsolutePath root))
         docs-root (.resolve root "docs")
         manifest (.resolve docs-root "document-inventory.json")]
     (when-not (exists? manifest)
       (fail! "DOC001" (str "missing manifest " manifest) {:path (str manifest)}))
     (let [inventory (json/load-json manifest)]
       (when-not (vector? inventory)
         (fail! "DOC002" "document inventory must be a JSON array" {}))
       (when-not (= 240 (count inventory))
         (fail! "DOC002" (str "expected 240 inventory entries, found " (count inventory))
                {:count (count inventory)}))
       (let [paths (mapv #(validate-entry! root docs-root %1 %2)
                         inventory (range 1 241))
             ids (mapv #(get % "id") inventory)]
         (when-not (= (count ids) (count (set ids)))
           (fail! "DOC004" "document ids must be unique" {}))
         (when-not (= (count paths) (count (set paths)))
           (fail! "DOC005" "document paths must be unique" {}))))
     (let [readmes (phase-readmes docs-root)]
       (when-not (= 19 (count readmes))
         (fail! "DOC012" (str "expected 19 phase README files, found " (count readmes))
                {:count (count readmes)})))
     (doseq [path (concat (files-with-extension docs-root [".md"])
                          (direct-files-with-extension (.resolve root "tools") [".py" ".clj"])
                          (files-with-extension (.resolve root "bootstrap/clojure/src/gravity/tooling") [".clj"]))]
       (assert-ascii! path))
     (when run-roadmap? (roadmap/validate-current))
     {:documents 240 :phase-indexes 19})))
