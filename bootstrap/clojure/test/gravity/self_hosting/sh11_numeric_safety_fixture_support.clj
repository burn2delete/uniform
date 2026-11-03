(ns gravity.self-hosting.sh11-numeric-safety-fixture-support
  "Shared SH-11 numeric-safety plans and normalized request adapters.

  Both the SH-11 safety suite and the safety pipeline use these delayed
  positive/negative fixtures.  The support namespace contains no tests and
  exposes the original adapters as ordinary public test support."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh11_numeric_safety_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-11 test source is not on the classpath"
                {:id "SH11-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH11-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c10-source-relative-path
  "language/compiler/src/gravity/compiler/c10_safety_analysis_pipeline.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-11")

(defn fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def c10-plan
  (delay (compile-plan c10-source-relative-path)))

(def accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "numeric-safety-outcomes" ".gravity"))))

(def accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "numeric-safety-outcomes" ".qst"))))

(def rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-numeric-safety" ".gravity"))))

(def rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-numeric-safety" ".qst"))))

(defn invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-numeric-safety-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn invoke-c10
  [function arguments]
  (invoke c10-plan function arguments))

(defn request
  [plan function]
  (invoke plan function []))

(defn classify
  [request]
  (invoke-c10 'sh11-classify-operation [request]))

(def accepted-functions
  '[sh11-proven-index-request
    sh11-proven-overflow-request
    sh11-proven-wrapping-request
    sh11-proven-division-request
    sh11-proven-cast-request
    sh11-proven-shift-request
    sh11-runtime-division-request
    sh11-unsafe-overflow-request])

(def rejected-cases
  {'sh11-out-of-bounds-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :bounds-safety]
   'sh11-overflow-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-OVERFLOW]
   'sh11-zero-divisor-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-DIV-ZERO]
   'sh11-narrowing-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-NARROW]
   'sh11-invalid-shift-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-SHIFT]
   'sh11-invalid-check-request
   ["C10-CHECK" :invalid-runtime-check :SAFE9-DIV-ZERO]
   'sh11-unsafe-safe-mode-request
   ["C10-UNSAFE"
    :unsafe-island-policy-or-metadata-gap
    :SAFE9-OVERFLOW]
   'sh11-incomplete-audit-request
   ["C10-UNSAFE"
    :unsafe-island-policy-or-metadata-gap
    :SAFE9-OVERFLOW]
   'sh11-ambiguous-evidence-request
   ["C10-NO-OUTCOME"
    :multiple-outcome-evidence
    :SAFE9-DIV-ZERO]
   'sh11-malformed-request
   ["C10-NO-OUTCOME"
    :malformed-safety-operation
    :bounds-safety]})
