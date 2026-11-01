(require (quote [gravity.c6-c10.source-interface :as c6-source-interface]))

;; Fixed contract DATA selection only; no source execution or admission.

(defn p15-s23-c6c10-source-program-contract [version]
  (when-not (identical? Long (class version))
    (p15-s23-c6c10-host-fail! "C6-VERIFY" "<gravity-checked-core>"
                             :explicit-c6-source-contract-version {}))
  (case (.longValue ^Long version)
    1 {:version 1
       :source-byte-count p15-s23-c6c10-source-byte-count
       :source-content-hash p15-s23-c6c10-expected-source-content-hash
       :plan-semantic-hash p15-s23-c6c10-expected-plan-semantic-hash
       :functions-semantic-hash p15-s23-c6c10-expected-functions-semantic-hash
       :builder-semantic-hash p15-s23-c6c10-expected-builder-semantic-hash
       :verifier-semantic-hash p15-s23-c6c10-expected-verifier-semantic-hash
       :required-functions p15-s23-c6c10-required-functions
       :function-count 139 :chunk-size 64}
    2 c6-source-contract/current-compiler-program
    3 c6-source-contract/flat-bool-compiler-program
    (p15-s23-c6c10-host-fail! "C6-VERIFY" "<gravity-checked-core>"
                             :explicit-c6-source-contract-version {})))

(defn p15-s23-c6c10-source-program-version [inputs]
  ;; Only the source-binding owner's own input records call this helper.
  (let [version (get inputs :source-contract-version 1)]
    (p15-s23-c6c10-source-program-contract version)
    version))
