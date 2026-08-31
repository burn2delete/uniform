(let [p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper]
(defn p15-s23-stage2-closed-checked-core-source-artifact
  ([source-path source-text requested-target]
   (p15-s23-stage2-closed-checked-core-source-artifact-internal
    source-path source-text requested-target nil
    :authoritative-artifact-construction nil nil))
  ([source-path source-text requested-target authority-record]
   (p15-s23-stage2-closed-checked-core-source-artifact-internal
    source-path source-text requested-target authority-record
    :authoritative-artifact-construction nil nil)))

(defn p15-s23-stage2-closed-checked-core-context
  ([source-path source-text requested-target]
   (p15-s23-closed-core-source-request-bounds!
    source-path source-text requested-target)
   (p15-s23-stage2-closed-checked-core-context
    source-path source-text requested-target nil))
  ([source-path source-text requested-target authority-record]
   (p15-s23-closed-core-source-request-bounds!
    source-path source-text requested-target)
   (if (some? authority-record)
     (do
       ;; A fourth value is out of band for a genuinely pure source unit.  Do
       ;; not inspect or bound that value before the pure admission boundary;
       ;; ask the same authoritative constructor to emit the canonical C8
       ;; rejection.  Nonempty declarations continue into the typed effectful
       ;; authority path below, where the value is bounded before use.
       (let [early-module-products
             (p15-s23-closed-core-early-module-products
              source-path source-text requested-target)
             module-attempt (:module-attempt early-module-products)
             module (:module module-attempt)]
         (when (and (= :valid (:status module-attempt))
                    (empty? (:effects module))
                    (empty? (:capabilities module)))
           (p15-s23-stage2-closed-checked-core-source-artifact
            source-path source-text requested-target authority-record)
           (p15-s23-closed-core-fail!
            "C8-CAPABILITY" source-path {}
            {:missing-fact
             :pure-fourth-authority-rejection-must-not-return})))
       (when-not (p15-s23-checked-core-authority-small-map?
                  authority-record 36)
         (p15-s23-closed-core-fail!
          "C8-CAPABILITY" source-path {:requested-target requested-target}
          {:missing-fact :bounded-typed-fourth-authority-context}))
       (try
         (p15-s23-reference-runtime-bounded-value!
          source-path :jvm :checked-core-context-authority
          authority-record p15-s23-reference-runtime-max-contract-nodes
          p15-s23-reference-runtime-max-contract-depth)
         (catch Exception _
           (p15-s23-closed-core-fail!
            "C8-CAPABILITY" source-path {:requested-target requested-target}
            {:missing-fact :bounded-typed-fourth-authority-context})))
       (let [expected
             (p15-s23-stage2-closed-checked-core-authority-binding
              source-path source-text requested-target
              p15-s23-checked-core-reference-policy-selector)]
         (when-not (= expected authority-record)
           (p15-s23-closed-core-fail!
            "C8-CAPABILITY" source-path {:requested-target requested-target}
            {:missing-fact :independently-reissued-authority-context
             :expected-authority-record-id (:authority-record-id expected)
             :observed-authority-record-id
             (:authority-record-id authority-record)}))
         {:source-path source-path
          :source-text source-text
          :source-content-hash (str "sha256:" (sha256-hex source-text))
          :requested-target requested-target
          :authority-record authority-record}))
     (p15-s23-closed-runtime-packet-context
      source-path source-text requested-target)))))
