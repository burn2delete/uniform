
(defn construct-negative-edge-evidence!
  "Dependency-safe adapter for the receipt-free unauthorized-edge builder."
  [construct! context edge-bases operation-manifest fuel-receipts]
  (require! (fn? construct!) :negative-edge-constructor)
  (let [evidence
        (construct! context edge-bases operation-manifest fuel-receipts)]
    (validate-bad-actual! (:bad-actual evidence)
                          (:bad-actual-expected evidence))
    evidence))

;; Compatibility names fail closed: V9 never constructs both roles in one JVM.
(defn materializer-dependency-commitments [& _]
  (reject! :legacy-materializer-retired))
(defn materializer-tool-closure-id-from-commitments [& _]
  (reject! :legacy-materializer-retired))
(defn materializer-tool-closure-id [& _]
  (reject! :legacy-materializer-retired))
(defn validate-reloaded-evidence! [& _]
  (reject! :legacy-materializer-retired))
(defn materialize-current [& _]
  (reject! :parent-safe-external-child-required))
