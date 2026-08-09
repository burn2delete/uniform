(ns gravity.reader-host-oracle
  "Clojure-hosted reference oracle for stage0 source-form records.

  This leaf retains the seed reader loop used to compare later readers against
  the Clojure host. It is neither the canonical C2 reader nor an authenticated
  or self-hosted reader implementation."
  (:require [gravity.diagnostics :as diagnostics]
            [gravity.reader-cursor :as reader-cursor]
            [gravity.reader-diagnostic-policy :as reader-diagnostic-policy]
            [gravity.reader-primitives :as reader-primitives]
            [gravity.source-span :as source-span])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io StringReader]))

(def ^:private required-operation-keys
  #{:line-start-indices
    :skip-ignored!
    :source-span
    :safe-excerpt
    :abbreviation-kind
    :source-metadata
    :form-kind
    :classify-reader-diagnostic
    :fail!})

(def ^:private namespace-contract
  {:namespace 'gravity.reader-host-oracle
   :contract-boundary :stage0-clojure-host-reference-reader-oracle
   :public-api
   {'read-source-form-records-host-oracle
    {:arglists '([source-path source-text]
                 [source-path source-text operations])
     :returns :source-form-records}}
   :artifact-inputs [:source-path :decoded-source-text]
   :artifact-outputs [:clojure-host-reference-source-form-records]
   :ownership
   {:owns [:clojure-host-reference-reading
           :host-reader-record-assembly]
    :does-not-own [:canonical-c2-reader-authority
                   :authenticated-source-form-records
                   :self-hosted-reader
                   :source-decoding
                   :bootstrap-orchestration
                   :clojure-read-function
                   :reader-eval-policy
                   :eof-identity]}
   :operation-interposition
   {:required-keys required-operation-keys
    :excludes [:read :eof :read-eval-policy]}
   :dependency-direction
   {:requires ['gravity.diagnostics
               'gravity.reader-cursor
               'gravity.reader-diagnostic-policy
               'gravity.reader-primitives
               'gravity.source-span]
    :forbids ['gravity.bootstrap]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-reader? false
   :authenticated-reader? false
   :self-hosted? false})

(defn- default-operations
  []
  {:line-start-indices source-span/line-start-indices
   :skip-ignored! reader-cursor/skip-ignored!
   :source-span source-span/source-span
   :safe-excerpt reader-primitives/safe-excerpt
   :abbreviation-kind reader-primitives/abbreviation-kind
   :source-metadata reader-primitives/source-metadata
   :form-kind reader-primitives/form-kind
   :classify-reader-diagnostic
   reader-diagnostic-policy/classify-reader-diagnostic
   :fail! diagnostics/fail!})

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "reader host oracle operations must be a map"
                    {:id "STAGE0-READER-HOST-ORACLE-OPERATIONS"
                     :required-operation-keys required-operation-keys
                     :provided-operations operations})))
  (let [provided-keys (set (keys operations))
        missing (sort (remove provided-keys required-operation-keys))
        unexpected (sort (remove required-operation-keys provided-keys))
        non-functions (->> required-operation-keys
                           (filter provided-keys)
                           (remove #(fn? (get operations %)))
                           sort)]
    (when (or (seq missing) (seq unexpected) (seq non-functions))
      (throw (ex-info "reader host oracle operations are invalid"
                      {:id "STAGE0-READER-HOST-ORACLE-OPERATIONS"
                       :required-operation-keys required-operation-keys
                       :missing-operation-keys (vec missing)
                       :unexpected-operation-keys (vec unexpected)
                       :non-function-operation-keys (vec non-functions)}))))
  operations)

(defn read-source-form-records-host-oracle
  "Read source records with Clojure's hosted reader as a reference oracle.

  The three-argument form exists only so bootstrap compatibility wrappers and
  focused tests can interpose the extracted helper functions. Clojure `read`,
  the EOF sentinel, and the `*read-eval*` denial policy are deliberately not
  injectable."
  ([source-path source-text]
   (read-source-form-records-host-oracle
    source-path source-text (default-operations)))
  ([source-path source-text operations]
   (let [{:keys [line-start-indices skip-ignored! source-span safe-excerpt
                 abbreviation-kind source-metadata form-kind
                 classify-reader-diagnostic fail!]}
         (validate-operations! operations)]
     (try
       (let [eof (Object.)
             line-starts (line-start-indices source-text)
             rdr (LineNumberingPushbackReader. (StringReader. source-text))]
         (loop [idx 0
                records []]
           (case (skip-ignored! rdr)
             :eof records
             :form
             (let [start-line (.getLineNumber rdr)
                   start-column (.getColumnNumber rdr)
                   form (binding [*read-eval* false]
                          (read {:eof eof} rdr))]
               (if (identical? eof form)
                 records
                 (let [end-line (.getLineNumber rdr)
                       end-column (.getColumnNumber rdr)
                       span (source-span source-path source-text line-starts idx
                                         start-line start-column
                                         end-line end-column)
                       excerpt (safe-excerpt source-text span)
                       abbreviation (abbreviation-kind excerpt)
                       metadata (source-metadata form)]
                   (recur (inc idx)
                          (conj records
                                {:form form
                                 :span span
                                 :metadata metadata
                                 :reader-origin
                                 {:kind :source
                                  :raw-form-kind (form-kind form)
                                  :raw-excerpt excerpt
                                  :abbreviation abbreviation}
                                 :generated-origin
                                 (if abbreviation
                                   [{:from span
                                     :reader-abbreviation abbreviation
                                     :expanded-form form}]
                                   [])}))))))))
       (catch Exception ex
         (let [[id message] (classify-reader-diagnostic source-text ex)]
           (fail! id message
                  {:source-span {:source source-path}
                   :reader-state {:stage :read-source-forms}
                   :cause-message (.getMessage ex)
                   :remediation
                   "Fix delimiter, string, collection, metadata, or reader-extension syntax before compilation."})))))))
