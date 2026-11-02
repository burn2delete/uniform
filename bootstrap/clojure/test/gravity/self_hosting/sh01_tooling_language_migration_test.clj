(ns gravity.self-hosting.sh01-tooling-language-migration-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.tooling.document-validation :as documents]
            [gravity.tooling.full-language-roadmap :as roadmap]
            [gravity.tooling.strict-json :as json])
  (:import (java.nio.file Files Path)
           (java.nio.file.attribute FileAttribute)))

(defn- temp-directory []
  (Files/createTempDirectory "gravity-tooling-migration-"
                             (make-array FileAttribute 0)))

(defn- write! [^Path root path text]
  (let [target (.resolve root path)]
    (Files/createDirectories (.getParent target)
                             (make-array FileAttribute 0))
    (spit (.toFile target) text)
    target))

(defn- json-string [value]
  (str "\"" (str/replace value "\"" "\\\"") "\""))

(defn- document-text [sequence]
  (str/join
   "\n"
   (concat
    [(str "# Document " sequence)
     "## Purpose" "Purpose text."
     "## Requirements" "Requirements text."
     "## Dependencies" "Dependencies text."
     "## Outputs and Artifacts" "Outputs text."
     "## Conformance Criteria" "Conformance text."]
    (repeat 70 "Normative contract line."))))

(defn- inventory-json [first-path]
  (str "["
       (str/join
        ","
        (for [sequence (range 1 241)
              :let [path (if (= sequence 1)
                           first-path
                           (format "docs/phase-%02d/doc-%03d.md"
                                   (mod sequence 19) sequence))]]
          (str "{\"sequence\":" sequence
               ",\"phase\":" (mod sequence 18)
               ",\"phaseName\":" (json-string "Phase")
               ",\"id\":" (json-string (str "D" sequence))
               ",\"title\":" (json-string (str "Document " sequence))
               ",\"path\":" (json-string path)
               ",\"category\":" (json-string "test") "}")))
       "]"))

(defn- repository-fixture [first-path]
  (let [root (temp-directory)]
    (doseq [phase (range 19)]
      (write! root (format "docs/phase-%02d/README.md" phase) "# Phase\n"))
    (doseq [sequence (range 1 241)
            :let [path (if (= sequence 1)
                         first-path
                         (format "docs/phase-%02d/doc-%03d.md"
                                 (mod sequence 19) sequence))]
            :when (str/starts-with? path "docs/")]
      (write! root path (document-text sequence)))
    (write! root "docs/document-inventory.json" (inventory-json first-path))
    root))

(deftest strict-json-rejects-duplicate-object-members
  (let [error (try
                (json/read-strict-json "{\"sequence\":1,\"sequence\":2}")
                nil
                (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "TOOL-JSON-001" (:diagnostic (ex-data error))))
    (is (str/includes? (.getMessage error) "repeats a key"))))

(deftest document-validator-accepts-the-required-contract-shape
  (let [root (repository-fixture "docs/phase-01/doc-001.md")]
    (is (= {:documents 240 :phase-indexes 19}
           (documents/validate-repository root false)))))

(deftest document-validator-rejects-inventory-path-escape
  (let [root (repository-fixture "docs/../escape.md")
        error (try
                (documents/validate-repository root false)
                nil
                (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "DOC005" (:diagnostic (ex-data error))))))

(deftest document-validator-requires-real-section-headings
  (let [root (repository-fixture "docs/phase-01/doc-001.md")]
    (write! root "docs/phase-01/doc-001.md"
            (str/replace (document-text 1) "## Purpose" "Text mentioning ## Purpose"))
    (let [error (try
                  (documents/validate-repository root false)
                  nil
                  (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "DOC009" (:diagnostic (ex-data error)))))))

(deftest roadmap-validator-preserves-positive-and-negative-fixtures
  (is (= {:accepted 1 :rejected 1} (roadmap/self-test)))
  (is (map? (roadmap/validate-current))))
