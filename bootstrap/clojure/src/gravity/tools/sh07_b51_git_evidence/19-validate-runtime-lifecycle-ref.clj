
(defn- validate-runtime-lifecycle-ref!
  [lifecycle-ref eligibility]
  (exact-keys! lifecycle-ref runtime-lifecycle-ref-keys
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-lifecycle-ref-keys)
  (validate-reference-container!
   (:container lifecycle-ref) :runtime-lifecycle-ref-container)
  (let [expected-container
        (select-keys (:coordinate eligibility) prior-ref-container-keys)
        expected-path (runtime-forbidden-path [:i9-suffix 0])]
    (when-not
     (and (= :pre-evidence (:stage lifecycle-ref))
          (= [:i9-suffix 0] (:path-ref lifecycle-ref))
          (= [] (:value-path lifecycle-ref))
          (= :evidence-eligible (:required-result lifecycle-ref))
          (sha256-id? (:receipt-id lifecycle-ref))
          (= expected-container (:container lifecycle-ref))
          (= expected-path (:path (:container lifecycle-ref)))
          (= (:freeze-record-id eligibility) (:receipt-id lifecycle-ref)))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-lifecycle-ref
             {:lifecycle-ref lifecycle-ref}))
    lifecycle-ref))

(defn- validate-runtime-forbidden-observations!
  [observations prior-container]
  (when-not (and (vector? observations)
                 (= (count runtime-forbidden-path-refs)
                    (count observations)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE"
           :runtime-forbidden-path-observation-count))
  (let [repository
        (atom nil)]
    (doseq [[ordinal [observation path-ref]]
            (map-indexed vector
                         (map vector observations
                              runtime-forbidden-path-refs))]
      (exact-keys! observation runtime-forbidden-observation-keys
                   "SH07-B51-CONTRACT-DISCHARGE"
                   :runtime-forbidden-path-observation-keys)
      (let [expected-path (runtime-forbidden-path path-ref)
            resolved-path (:resolved-path observation)
            argv (:argv observation)]
        (when-not
         (and (= ordinal (:ordinal observation))
              (= path-ref (:path-ref observation))
              (or (= expected-path resolved-path)
                  (and (map? expected-path)
                       (valid-governance-resolved-path?
                        expected-path resolved-path)))
              (vector? argv)
              (= 8 (count argv))
              (= git-executable (nth argv 0))
              (= "-C" (nth argv 1))
              (string? (nth argv 2))
              (not (str/blank? (nth argv 2)))
              (= "ls-tree" (nth argv 3))
              (= "-z" (nth argv 4))
              (= (:commit prior-container) (nth argv 5))
              (= "--" (nth argv 6))
              (= resolved-path (nth argv 7))
              (= 0 (:exit-code observation))
              (= 0 (:stdout-byte-count observation))
              (= empty-raw-sha256 (:stdout-raw-sha256 observation))
              (= [] (:parsed-result observation)))
          (fail! "SH07-B51-CONTRACT-DISCHARGE"
                 :runtime-forbidden-path-observation
                 {:ordinal ordinal :path-ref path-ref}))
        (if (nil? @repository)
          (reset! repository (nth argv 2))
          (when-not (= @repository (nth argv 2))
            (fail! "SH07-B51-CONTRACT-DISCHARGE"
                   :runtime-forbidden-path-repository
                   {:ordinal ordinal}))))))
  observations)

(defn validate-runtime-prior-ref!
  "Validate the exact Q PRE-DISCHARGE prior reference used by runtime."
  ([prior-ref]
   (validate-runtime-prior-ref! prior-ref nil nil))
  ([prior-ref eligibility]
   (validate-runtime-prior-ref!
    prior-ref eligibility
    (when eligibility (:pre-discharge-id eligibility))))
  ([prior-ref eligibility pre-discharge-id]
   (exact-keys! prior-ref runtime-prior-ref-keys
                "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref-keys)
   (validate-reference-container!
    (:container prior-ref) :runtime-prior-ref-container-keys)
   (let [container (:container prior-ref)]
     (when-not
      (and (= :runtime (:phase prior-ref))
           (= [:pre-evidence-contract-discharge]
              (:value-path prior-ref))
           (git-id? (:commit container))
           (git-id? (:tree container))
           (vector? (:parents container))
           (every? git-id? (:parents container))
           (safe-relative-path? (:path container))
           (= "100644" (:mode container))
           (= "blob" (:type container))
           (git-id? (:blob-oid container))
           (integer? (:byte-count container))
           (not (neg? (:byte-count container)))
           (sha256-id? (:raw-sha256 container))
           (sha256-id? (:discharge-id prior-ref))
           (map? (:lifecycle-ref prior-ref))
           (vector? (:forbidden-path-observations prior-ref)))
       (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref))
     (when eligibility
       (validate-evidence-eligibility! eligibility)
       (when-not (= (select-keys (:coordinate eligibility)
                                  prior-ref-container-keys)
                    container)
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-container-eligibility))
       (validate-runtime-lifecycle-ref!
        (:lifecycle-ref prior-ref) eligibility)
       (when-not (= (:discharge-id prior-ref)
                    (:pre-discharge-id eligibility))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-eligibility-id)))
     (when pre-discharge-id
       (when-not (= (:discharge-id prior-ref) pre-discharge-id)
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-discharge-id)))
     (validate-runtime-forbidden-observations!
      (:forbidden-path-observations prior-ref) container)
     (reject-runtime-back-edges! (:lifecycle-ref prior-ref) [:lifecycle-ref])
     prior-ref)))

(defn validate-runtime-contract-discharge!
  "Validate the complete runtime discharge, including its Q prior-ref,
  resolved dense row vector/root, authority prefix, and self-ID equation."
  ([discharge]
   (validate-runtime-contract-discharge! discharge nil))
  ([discharge {:keys [content evidence-eligibility runtime-carriers prior-ref]}]
   (exact-keys! discharge contract-discharge-keys
                "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-keys)
   (when-not (and (= :gravity/sh07-b51-runtime-contract-discharge-v1
                     (:artifact discharge))
                  (= 1 (:schema-version discharge))
                  (= :runtime (:phase discharge))
                  (= row-plan-id (:row-plan-id discharge))
                  (= [[:discharge-id]] (:self-id-omits discharge))
                  (sha256-id? (:resolved-root discharge))
                  (sha256-id? (:discharge-id discharge)))
     (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-header))
   (validate-runtime-prior-ref! (:prior-ref discharge)
                                (:evidence-eligibility
                                 (get-in discharge [:evidence :carriers]))
                                (get-in discharge
                                        [:evidence :carriers
                                         :evidence-eligibility
                                         :pre-discharge-id]))
   (validate-phase-evidence! (:evidence discharge))
   (let [carriers (get-in discharge [:evidence :carriers])
         resolved (resolve-contract-phase! :runtime carriers)
         expected-without-id (dissoc discharge :discharge-id)
         expected-id
         (codec/H [:gravity/sh07-b51-runtime-contract-discharge-v1
                   expected-without-id])]
     (when-not
      (and (= (:resolved-rows resolved) (:resolved-rows discharge))
           (= (:resolved-root resolved) (:resolved-root discharge))
           (= (runtime-authority) (:authority discharge))
           (= expected-id (:discharge-id discharge)))
       (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-identity))
     (when content
       (validate-runtime-content! content)
       (when-not (= content (:content carriers))
         (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-content-join)))
     (when evidence-eligibility
       (validate-evidence-eligibility! evidence-eligibility)
       (when-not (= evidence-eligibility
                    (:evidence-eligibility carriers))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-eligibility-join)))
     (when runtime-carriers
       (validate-runtime-carrier-pool! runtime-carriers)
       (when-not (= runtime-carriers
                    (dissoc carriers :content :evidence-eligibility))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-carrier-join)))
     (when prior-ref
       (validate-runtime-prior-ref! prior-ref evidence-eligibility)
       (when-not (= prior-ref (:prior-ref discharge))
         (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref-join)))
     discharge)))

(defn build-runtime-contract-discharge
  "Construct RUNTIME-CONTRACT-DISCHARGE only after the complete Q-derived
  eligibility and closed runtime carrier pool are available.  The caller must
  supply the complete PRIOR-REF, including its immutable lifecycle and
  forbidden-path observations; this function never fabricates those facts."
  ([content evidence-eligibility runtime-carriers prior-ref]
   (validate-runtime-content! content)
   (validate-evidence-eligibility! evidence-eligibility)
   (validate-runtime-carrier-pool! runtime-carriers)
   (validate-runtime-prior-ref! prior-ref evidence-eligibility)
   (let [evidence (build-runtime-phase-evidence
                   content evidence-eligibility runtime-carriers)
         resolved (resolve-contract-phase! :runtime (:carriers evidence))
         without-id
         {:artifact :gravity/sh07-b51-runtime-contract-discharge-v1
          :schema-version 1
          :phase :runtime
          :row-plan-id row-plan-id
          :prior-ref prior-ref
          :evidence evidence
          :resolved-rows (:resolved-rows resolved)
          :resolved-root (:resolved-root resolved)
          :authority (runtime-authority)
          :self-id-omits [[:discharge-id]]}
         discharge
         (assoc without-id :discharge-id
                (codec/H [:gravity/sh07-b51-runtime-contract-discharge-v1
                          without-id]))]
     (validate-runtime-contract-discharge!
      discharge {:content content
                 :evidence-eligibility evidence-eligibility
                 :runtime-carriers runtime-carriers
                 :prior-ref prior-ref})))
  ([input]
   (exact-keys! input #{:content :evidence-eligibility :runtime-carriers
                         :prior-ref}
                "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-discharge-input-keys)
   (build-runtime-contract-discharge (:content input)
                                     (:evidence-eligibility input)
                                     (:runtime-carriers input)
                                     (:prior-ref input))))

(defn build-runtime-contract-discharge!
  [& arguments]
  (apply build-runtime-contract-discharge arguments))
