
(def co-canonical-source-extensions
  source-unit/co-canonical-source-extensions)

(defn gravity-source-extension
  [path]
  (source-unit/gravity-source-extension path))

(defn qst-or-gravity-source?
  [path]
  (source-unit/qst-or-gravity-source? path))

(defn gravity-source-kind
  [path]
  (source-unit/gravity-source-kind path))

(defn diagnostic
  [id message data]
  (diagnostics/diagnostic id message data))

(defn fail!
  [id message data]
  (throw (diagnostic id message data)))

(defn line-terminator-char?
  [ch]
  (source-span/line-terminator-char? ch))

(defn line-start-indices
  [source-text]
  (source-span/line-start-indices source-text))

(defn char-index-at
  [line-starts line column]
  (source-span/char-index-at line-starts line column))

(defn utf8-byte-count
  [text]
  (source-span/utf8-byte-count text))

(defn source-location
  [source-text line-starts line column]
  (source-span/source-location source-text line-starts line column))

(defn source-span
  ([source form-index]
   (source-span/source-span source form-index))
  ([source-path source-text line-starts form-index start-line start-column end-line end-column]
   (source-span/source-span source-path source-text line-starts form-index
                            start-line start-column end-line end-column)))

(defn form-kind
  [form]
  (reader-primitives/form-kind form))

(defn safe-excerpt
  [source-text span]
  (reader-primitives/safe-excerpt source-text span))

(defn abbreviation-kind
  [excerpt]
  (reader-primitives/abbreviation-kind excerpt))

(defn source-metadata
  [form]
  (reader-primitives/source-metadata form))

(defn skip-line-comment!
  [^LineNumberingPushbackReader rdr]
  (reader-cursor/skip-line-comment! rdr))

(defn skip-ignored!
  [^LineNumberingPushbackReader rdr]
  (reader-cursor/skip-ignored! rdr))

(defn classify-reader-diagnostic
  [source-text ex]
  (reader-diagnostic-policy/classify-reader-diagnostic source-text ex))

(defn read-source-form-records-host-oracle
  [source-path source-text]
  (reader-host-oracle/read-source-form-records-host-oracle
   source-path source-text
   {:line-start-indices line-start-indices
    :skip-ignored! skip-ignored!
    :source-span source-span
    :safe-excerpt safe-excerpt
    :abbreviation-kind abbreviation-kind
    :source-metadata source-metadata
    :form-kind form-kind
    :classify-reader-diagnostic classify-reader-diagnostic
    :fail! fail!}))

(def ^:dynamic *authenticated-source-form-records* nil)

(defn read-source-form-records
  [source-path source-text]
  (let [authenticated *authenticated-source-form-records*
        same-source?
        (and (map? authenticated)
             (string? (:source-path authenticated))
             (= source-text (:source-text authenticated))
             (try
               (= (.getCanonicalPath (java.io.File. source-path))
                  (.getCanonicalPath
                   (java.io.File. (:source-path authenticated))))
               (catch Exception _ false)))]
    (if same-source?
      (:records authenticated)
      (read-source-form-records-host-oracle source-path source-text))))

(defn read-forms
  "Read source into Lisp forms. Stage 0 delegates lexical reading to Clojure's
  reader because the bootstrap language subset is intentionally Clojure-shaped."
  [source-path source-text]
  (mapv :form (read-source-form-records source-path source-text)))

(defn ns-form?
  [form]
  (reader-namespace/ns-form? form))

(def allowed-ns-clauses
  reader-namespace/allowed-ns-clauses)

(defn fail-ns-shape!
  [source-path clause remediation]
  (reader-namespace/fail-ns-shape!
   source-path clause remediation
   {:source-span source-span
    :fail! fail!}))

(defn validate-ns-syntax!
  [source-path forms]
  (reader-namespace/validate-ns-syntax!
   source-path forms
   {:ns-form? ns-form?
    :allowed-ns-clause? #(contains? allowed-ns-clauses %)
    :fail-ns-shape! fail-ns-shape!}))

(defn reader-module-context
  [forms]
  (reader-namespace/reader-module-context
   forms
   {:ns-form? ns-form?
    :form-kind form-kind}))

(defn syntax-object-stream
  ([source-path form-records]
   (syntax-object-stream source-path form-records nil))
  ([source-path form-records module-context]
   (syntax-object-stream/syntax-object-stream
    source-path form-records module-context)))

(declare l1-source-unit-artifacts
         reader-project-context-for-source
         standard-reader-options)

;; Keep the seed dependency at this compatibility seam, outside active reader
;; adapter work in bootstrap.clj. The stage never loads the orchestrator.
(clojure.core/require 'gravity.reader)

(defn read-source-artifact
  ([source-path source-text]
   (read-source-artifact source-path source-text
                         (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (gravity.reader/read-source-artifact
    source-path source-text project-context
    {:read-source-form-records read-source-form-records
     :validate-ns-syntax! validate-ns-syntax!
     :reader-module-context reader-module-context
     :syntax-object-stream syntax-object-stream
     :l1-source-unit-artifacts l1-source-unit-artifacts
     :standard-reader-options standard-reader-options
     :gravity-source-extension gravity-source-extension
     :gravity-source-kind gravity-source-kind
     :utf8-byte-count utf8-byte-count})))

(def effect-capability
  {:io/write :io/stdout
   :network/listen :network/listener})
