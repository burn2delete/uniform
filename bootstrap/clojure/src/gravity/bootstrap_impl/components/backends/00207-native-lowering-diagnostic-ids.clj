

(def native-lowering-diagnostic-ids
  ["B2-DIALECT"
   "B2-UB"
   "B2-ABI"
   "B2-POINTER"
   "B2-NUMERIC"
   "B2-RUNTIME"
   "B2-FFI"
   "B2-MMIO"
   "B2-MANIFEST"
   "B3-TARGET"
   "B3-METADATA"
   "B3-UB"
   "B3-POINTER"
   "B3-NUMERIC"
   "B3-ATOMIC"
   "B3-RUNTIME"
   "B3-ABI"
   "B3-PASS"
   "B3-MANIFEST"
   "B7-DIALECT"
   "B7-VERIFY"
   "B7-CONVERSION"
   "B7-METADATA"
   "B7-EFFECT"
   "B7-NUMERIC"
   "B7-ALIAS"
   "B7-PASS"
   "B7-HANDOFF"
   "B7-MANIFEST"
   "B13-SCHEMA"
   "B13-HASH"
   "B13-PROVENANCE"
   "B13-SOURCEMAP"
   "B13-EVIDENCE"
   "B13-TARGET"
   "B13-CONFORMANCE"
   "B13-REPRODUCIBILITY"
   "B13-RELEASE"
   "B13-GRAPH"
   "B14-POSITIVE"
   "B14-NEGATIVE"
   "B14-DIFFERENTIAL"
   "B14-NONDETERMINISM"
   "B14-SKIP"
   "B14-EVIDENCE"])

(def native-lowering-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             native-lowering-diagnostic-ids)))

(defn native-lowering-backend-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B2-") :gravity.backend/c
    (str/starts-with? id "B3-") :gravity.backend/llvm
    (str/starts-with? id "B7-") :gravity.backend/mlir
    (str/starts-with? id "B13-") :gravity.backend/artifact-emission
    :else :gravity.backend/conformance))

(defn native-lowering-stage-for-diagnostic
  [id]
  (cond
    (str/starts-with? id "B2-") :c-backend
    (str/starts-with? id "B3-") :llvm-backend
    (str/starts-with? id "B7-") :mlir-backend
    (str/starts-with? id "B13-") :artifact-emission
    :else :backend-conformance))

(defn native-lowering-diagnostic-message
  [id]
  (cond
    (str/starts-with? id "B2-") "C backend lowering contract failed"
    (str/starts-with? id "B3-") "LLVM backend lowering contract failed"
    (str/starts-with? id "B7-") "MLIR backend lowering contract failed"
    (str/starts-with? id "B13-") "backend artifact emission contract failed"
    :else "backend conformance contract failed"))