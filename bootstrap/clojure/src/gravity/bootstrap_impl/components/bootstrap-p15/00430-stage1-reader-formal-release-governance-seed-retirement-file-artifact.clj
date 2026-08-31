

(defn stage1-reader-formal-release-governance-seed-retirement-file-artifact
  [path]
  (stage1-reader-formal-release-governance-seed-retirement-source-artifact
   path
   (slurp path)))

(defn p15-s23-whole-language-self-hosting-gate-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-whole-language-self-hosting-gate-diagnostic-messages
              id
              "P15-S23 whole-language self-hosting gate failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :p15-s23-whole-language-self-hosting-gate
                 :diagnostic-family
                 :p15-s23-whole-language-self-hosting-gate
                 :value value
                 :remediation "Provide the complete BOOT1/BOOT3/BOOT6/BOOT7/BOOT8, C1/C2/C3/C6/C15, R1/R11, PKG7, TEST13, GOV6, and GOV10 evidence bundle before claiming full compiler self-hosting or retiring the Clojure seed."}
                data)))

(defn p15-s23-candidate-evidence-value
  [candidate key]
  (or (get candidate key)
      (get-in candidate [:evidence key])))

(defn p15-s23-evidence-present?
  [value]
  (cond
    (true? value) true
    (map? value) (contains? #{:accepted :clear :complete :passed
                              :present :reproducible :verified}
                            (:status value))
    (and (sequential? value) (seq value)) true
    :else false))

(defn p15-s23-diagnostic-record
  [source-path candidate requirement]
  (let [diagnostic (:diagnostic requirement)
        key (:key requirement)]
    {:artifact :gravity/diagnostic
     :diagnostic-id (str "diag-" (str/lower-case diagnostic))
     :diagnostic diagnostic
     :severity :error
     :stage :p15-s23-whole-language-self-hosting-gate
     :source-span {:source source-path}
     :candidate-id (:candidate-id candidate)
     :evidence-key key
     :governing-documents (:governing-documents requirement)
     :message (get p15-s23-whole-language-self-hosting-gate-diagnostic-messages
                   diagnostic)
     :facts {:required-evidence (:description requirement)
             :observed (p15-s23-candidate-evidence-value candidate key)}
     :remediation :supply_required_evidence_before_self_hosting_claim}))

(defn p15-s23-whole-language-self-hosting-gate-diagnostics
  [source-path candidate]
  (let [missing-evidence
        (mapv #(p15-s23-diagnostic-record source-path candidate %)
              (filter
               (fn [{:keys [key]}]
                 (not (p15-s23-evidence-present?
                       (p15-s23-candidate-evidence-value candidate key))))
               p15-s23-whole-language-self-hosting-required-evidence))
        seed-retired?
        (and (true? (:clojure-seed-retired? candidate))
             (false? (:clojure-seed-boundary? candidate)))
        seed-diagnostic
        (when-not seed-retired?
          [(p15-s23-diagnostic-record
            source-path
            candidate
            {:key :clojure-seed-retired
             :diagnostic "P15S23014"
             :governing-documents ["BOOT1" "BOOT3" "BOOT6" "TEST13"]
             :description :clojure_seed_boundary_is_absent})])
        required-missing?
        (seq (concat missing-evidence seed-diagnostic))
        overclaim?
        (and (or (true? (:full-language-compiler-self-hosted? candidate))
                 (true? (:clojure-seed-retired? candidate)))
             required-missing?)
        overclaim-diagnostic
        (when overclaim?
          [(assoc
            (p15-s23-diagnostic-record
             source-path
             candidate
             {:key :self-hosting-claim
              :diagnostic "P15S23016"
              :governing-documents ["BOOT1" "BOOT7" "BOOT8" "TEST13"]
              :description :full_self_hosting_claim_requires_complete_bundle})
            :facts
            {:full-language-compiler-self-hosted?
             (:full-language-compiler-self-hosted? candidate)
             :clojure-seed-retired? (:clojure-seed-retired? candidate)
             :missing-diagnostics
             (mapv :diagnostic (concat missing-evidence seed-diagnostic))})])]
    (vec (concat missing-evidence seed-diagnostic overclaim-diagnostic))))

(defn p15-s23-assert-whole-language-self-hosting-candidate!
  [source-path candidate]
  (let [diagnostics
        (p15-s23-whole-language-self-hosting-gate-diagnostics
         source-path candidate)]
    (when (seq diagnostics)
      (let [id (or (some #(when (= "P15S23016" (:diagnostic %))
                           (:diagnostic %))
                         diagnostics)
                   (:diagnostic (first diagnostics)))]
        (p15-s23-whole-language-self-hosting-gate-fail!
         id source-path candidate
         {:diagnostics diagnostics
          :missing-evidence (mapv :evidence-key diagnostics)})))
    candidate))

(defn p15-s23-whole-language-self-hosting-diagnostic-stream
  [source-path candidate diagnostics]
  {:artifact :gravity/p15-s23-whole-language-self-hosting-diagnostic-stream
   :stage :p15-s23-whole-language-self-hosting-gate
   :source-path source-path
   :candidate-id (:candidate-id candidate)
   :diagnostics diagnostics
   :summary {:errors (count diagnostics)
             :required-evidence
             (count p15-s23-whole-language-self-hosting-required-evidence)
             :status (if (seq diagnostics) :blocked :complete)}
   :status (if (seq diagnostics) :blocked :complete)})

(defn p15-s23-compiler-pipeline-manifest-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-compiler-pipeline-manifest-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :manifest-id (:manifest-id artifact)
           :source-path source-path
           :pass-contract-count
           (get-in artifact
                   [:p15-s23-compiler-pipeline-manifest-results
                    :pass-contract-count])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-source-syntax-serialization-proof-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-source-syntax-serialization-proof-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :source-id (get-in artifact [:source-unit-record :source-id])
           :syntax-object-count
           (get-in artifact
                   [:p15-s23-source-syntax-serialization-results
                    :syntax-object-count])
           :serialization-id
           (get-in artifact [:serialization-roundtrip-record
                             :serialization-id])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-core-lowering-diagnostic-preservation-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"
        artifact-fn
        (resolve 'gravity.bootstrap/p15-s23-core-lowering-diagnostic-preservation-file-artifact)]
    (when (and artifact-fn (.isFile (java.io.File. source-path)))
      (try
        (let [artifact (artifact-fn source-path)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :core-node-count
           (get-in artifact
                   [:p15-s23-core-diagnostic-preservation-results
                    :core-node-count])
           :diagnostic-count
           (get-in artifact
                   [:p15-s23-core-diagnostic-preservation-results
                    :diagnostic-count])
           :c6-artifact-id
           (get-in artifact [:c6-core-lowering-artifact :artifact-id])
           :c15-artifact-id
           (get-in artifact [:c15-diagnostics-artifact :artifact-id])
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))