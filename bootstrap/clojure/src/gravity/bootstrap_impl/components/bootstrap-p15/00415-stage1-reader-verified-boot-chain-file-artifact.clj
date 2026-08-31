

(defn stage1-reader-verified-boot-chain-file-artifact
  [path]
  (stage1-reader-verified-boot-chain-source-artifact path (slurp path)))

(def stage1-reader-diverse-bootstrap-verification-required-operations
  [:seed-built-rebuild
   :self-built-rebuild
   :clean-environment-rebuild
   :diverse-toolchain-rebuild
   :compare-bootstrap-traces
   :verify-provenance
   :record-independent-audit])

(defn stage1-reader-diverse-bootstrap-verification-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))