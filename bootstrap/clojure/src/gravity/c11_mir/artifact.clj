(ns
 gravity.c11-mir.artifact
 (:require
  [gravity.c11-mir.operations :as operations]
  [gravity.c11-mir.policy :as policy]
  [gravity.c11-mir.projection :as projection]
  [gravity.c11-mir.records :as records]
  [gravity.c11-mir.validation :as validation]
  [gravity.digest :as digest]))
(defn-
 aid
 [x]
 (operations/invoke
  :c4-artifact-id
  (fn [v] (str "sha256:" (digest/sha256-hex (pr-str v))))
  x))
(defn
 source-artifact
 [configuration source-path source-text]
 (let
  [rec
   (operations/invoke
    :read-source-form-records
    (policy/unsupported :read-source-form-records)
    source-path
    source-text)
   forms
   (mapv :form rec)
   _
   (operations/invoke
    :validate-ns-syntax!
    (policy/unsupported :validate-ns-syntax!)
    source-path
    forms)
   module
   (operations/invoke
    :parse-module
    (policy/unsupported :parse-module)
    source-path
    forms)
   overrides
   (operations/invoke
    :c11-mir-source-overrides
    records/source-overrides
    module)
   _
   (operations/invoke
    :c11-mir-validate-overrides!
    (fn [p m o] (validation/validate-overrides! configuration p m o))
    source-path
    module
    overrides)
   c10
   (operations/invoke
    :compiler-c10-safety-source-artifact
    (policy/unsupported :compiler-c10-safety-source-artifact)
    source-path
    source-text)
   outcomes
   (vec (get-in c10 [:safety-outcome-records :records]))
   span
   (records/source-span source-path 0)
   ops
   (mapv
    (fn
     [i family]
     (operations/invoke
      :c11-mir-operation
      records/operation
      module
      span
      outcomes
      i
      family))
    (range)
    (:c11-mir-required-operation-families configuration))
   mir
   (operations/invoke
    :c11-mir-module-record
    records/module-record
    module
    c10
    ops)
   flow
   (operations/invoke :c11-data-flow-graph records/data-flow ops)
   anchors
   (operations/invoke :c11-domain-anchor-table records/anchors)
   diagnostics
   (operations/invoke
    :c11-mir-diagnostics
    (fn [p] (records/diagnostics configuration p))
    source-path)
   verifier
   (operations/invoke
    :c11-mir-verifier-report
    (fn [m o d a x] (validation/verifier configuration m o d a x))
    mir
    ops
    flow
    anchors
    diagnostics)
   base
   (projection/artifact-base
    configuration
    module
    c10
    ops
    mir
    flow
    anchors
    diagnostics
    verifier
    overrides)
   _
   (operations/invoke
    :c11-mir-validate!
    (fn [p x] (validation/validate! configuration p x))
    source-path
    base)
   proof
   (operations/invoke :c11-mir-capability-proof validation/proof base)]
  (assoc
   base
   :capability-based-proof
   proof
   :artifact-id
   (aid (assoc base :capability-based-proof proof)))))
(defn
 file-artifact
 [configuration path]
 (operations/invoke
  :compiler-c11-mir-source-artifact
  (fn [p t] (source-artifact configuration p t))
  path
  (slurp path)))
