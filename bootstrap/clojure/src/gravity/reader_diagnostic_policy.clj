(ns gravity.reader-diagnostic-policy
  "Stage 0 policy for classifying Clojure seed reader failures.

  This private contract classifies an already-caught reader exception and
  source text into a stable diagnostic id/message pair. It does not read
  source, construct or throw diagnostics, or claim canonical C2 authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  {:namespace 'gravity.reader-diagnostic-policy
   :contract-boundary :stage0-reader-diagnostic-policy
   :public-api
   {'classify-reader-diagnostic
    {:arglists '([source-text ex])
     :returns :diagnostic-id-and-message}}
   :artifact-inputs [:source-text :caught-reader-exception]
   :artifact-outputs [:diagnostic-id-and-message]
   :ownership
   {:owns [:stage0-reader-failure-classification]
    :does-not-own [:source-reading
                   :reader-cursor-state
                   :diagnostic-construction
                   :diagnostic-throwing
                   :canonical-c2-reader-authority
                   :bootstrap-orchestration]}
   :dependency-direction
   {:requires ['clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner
   'gravity.reader-diagnostic-policy-test/reader-diagnostic-policy-contract-is-narrow-and-private
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn classify-reader-diagnostic
  "Classify one caught seed-reader exception without constructing a diagnostic."
  [source-text ex]
  (let [message (or (.getMessage ex) "")
        lower (str/lower-case message)
        trimmed (str/trim source-text)]
    (cond
      (or (str/includes? lower "reader function for tag")
          (str/includes? lower "unknown reader tag"))
      ["L1-READER-EXTENSION"
       "reader extension tag is not registered for the stage0 build policy"]

      (or (str/includes? lower "metadata")
          (and (str/includes? lower "eof while reading")
               (str/starts-with? trimmed "^")))
      ["L1-METADATA"
       "metadata form is malformed or unattached"]

      (or (str/includes? lower "map literal must contain")
          (str/includes? lower "map literal contains"))
      ["L1-MAP-ARITY"
       "map literal contains an odd number of forms"]

      (or (str/includes? lower "invalid number")
          (str/includes? lower "invalid numeric")
          (str/includes? lower "number format"))
      ["L1-NUMERIC"
       "numeric candidate fails every enabled numeric literal grammar"]

      (str/includes? lower "invalid token")
      ["L1-IDENTIFIER"
       "symbol or keyword has an invalid surface spelling"]

      (or (str/includes? lower "unsupported escape character")
          (str/includes? lower "invalid unicode escape")
          (str/includes? lower "string"))
      ["L1-STRING"
       "string or character literal is malformed"]

      (str/includes? lower "eof while reading")
      ["L1-DELIMITER"
       "source has an unbalanced or mismatched delimiter"]

      :else
      ["C2-READER"
       "source could not be read by the stage0 bootstrap reader"])))
