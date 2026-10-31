(ns gravity.bootstrap-c13-comparison
  "Compatibility ports for native C13 reconstruction data; no compiler authority."
  (:require [gravity.optimization :as optimization]))

(defn selection [mir]
  (optimization/selection mir))

(defn rewrite [mir selected]
  (optimization/rewrite mir selected))

(defn restore [output delta]
  (optimization/restore output delta))

(defn output-id [output]
  (optimization/output-id
   #'gravity.bootstrap/p15-s23-c11-mir-digest
   #'gravity.bootstrap/p15-s23-c11-mir-path-neutral-value output))

(defn extend-evidence [mir base]
  (optimization/extend-evidence #'selection #'rewrite #'restore #'output-id mir base))

(defn translated-record [mir evidence base]
  (optimization/translated-record #'selection #'rewrite #'restore #'output-id mir evidence base))
