(ns gravity.reader-primitives
  "Stage 0 reader metadata and surface-abbreviation primitives.

  This leaf classifies already-read Clojure values and derives bounded source
  excerpts and metadata used by reader artifacts. It does not read source,
  construct diagnostics, or depend on the bootstrap orchestrator."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  {:namespace 'gravity.reader-primitives
   :contract-boundary :stage0-reader-metadata-primitives
   :public-api
   {'form-kind {:arglists '([form])
                :returns :reader-form-kind}
    'safe-excerpt {:arglists '([source-text span])
                   :returns :bounded-source-excerpt}
    'abbreviation-kind {:arglists '([excerpt])
                        :returns :reader-abbreviation-kind-or-nil}
    'source-metadata {:arglists '([form])
                      :returns :source-metadata-map}}
   :artifact-inputs [:read-form :source-text :source-span :form-metadata]
   :artifact-outputs [:reader-form-kind :source-excerpt
                      :reader-abbreviation-kind :source-metadata]
   :ownership
   {:owns [:reader-form-kind-classification
           :bounded-source-excerpt
           :surface-abbreviation-classification
           :reader-location-metadata-removal]
    :does-not-own [:source-reading
                   :source-decoding
                   :source-span-construction
                   :reader-state
                   :syntax-object-construction
                   :diagnostic-construction
                   :diagnostic-policy
                   :bootstrap-orchestration]}
   :dependency-direction
   {:requires ['clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner
   'gravity.reader-primitives-test/reader-primitives-contract-is-narrow-and-acyclic
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn form-kind
  "Classify one already-read form using the stage0 reader kind vocabulary."
  [form]
  (cond
    (nil? form) :nil
    (true? form) :boolean
    (false? form) :boolean
    (integer? form) :integer
    (ratio? form) :ratio
    (float? form) :decimal
    (string? form) :string
    (char? form) :character
    (symbol? form) :symbol
    (keyword? form) :keyword
    (seq? form) :list
    (vector? form) :vector
    (map? form) :map
    (set? form) :set
    :else :unknown))

(defn safe-excerpt
  "Return the source text selected by a span, capped at 160 characters.

  Missing coordinates retain the bootstrap defaults. The end coordinate is
  clamped to the source length; other invalid span boundaries intentionally
  preserve `subs` exception behavior rather than introducing policy here."
  [source-text span]
  (let [start (get-in span [:start :char] 0)
        end (get-in span [:end :char] start)
        excerpt (subs source-text start (min end (count source-text)))]
    (if (> (count excerpt) 160)
      (str (subs excerpt 0 160) "...")
      excerpt)))

(defn abbreviation-kind
  "Classify a source excerpt's leading reader abbreviation, if any."
  [excerpt]
  (cond
    (str/starts-with? excerpt "~@") :splice-unquote
    (str/starts-with? excerpt "'") :quote
    (str/starts-with? excerpt "`") :syntax-quote
    (str/starts-with? excerpt "~") :unquote
    (str/starts-with? excerpt "^") :metadata
    (str/starts-with? excerpt "@") :deref
    :else nil))

(defn source-metadata
  "Return form metadata without Clojure reader location coordinates."
  [form]
  (apply dissoc (or (meta form) {}) [:line :column :end-line :end-column]))
