
(defn build-evidence-eligibility
  "Build E1 EVIDENCE-ELIGIBILITY only from a complete Q receipt and the
  independently completed L/M/A/B eligibility checks."
  ([coordinate receipt participant-results]
   (validate-eligibility-coordinate! (:content receipt) coordinate)
   (validate-eligibility-receipt! (:content receipt) receipt)
   (validate-participant-checks! coordinate participant-results)
   (let [pre-discharge (:pre-evidence-contract-discharge receipt)
         eligibility
         {:coordinate coordinate
          :receipt-id (:receipt-id receipt)
          :freeze-state (get-in receipt [:freeze :state])
          :freeze-record-id
          (get-in receipt [:freeze :active-record-ref :record-id])
          :source-descriptors-root (:source-descriptors-root receipt)
          :pre-masked-root (:pre-masked-root receipt)
          :pre-discharge-id (:discharge-id pre-discharge)
          :participant-check-roots
          (mapv :eligibility-check-root participant-results)}]
     (validate-evidence-eligibility!
      eligibility {:content (:content receipt)
                   :receipt receipt
                   :participant-results participant-results})))
  ([input]
   (exact-keys! input #{:coordinate :receipt :participant-results}
                "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-input-keys)
   (build-evidence-eligibility (:coordinate input) (:receipt input)
                               (:participant-results input))))

(defn build-evidence-eligibility!
  [& arguments]
  (apply build-evidence-eligibility arguments))

(def ^:private runtime-back-edge-keys
  #{:identity-receipt :runtime-contract-discharge :enclosing-discharge
    :enclosing-identity-receipt :enclosing-receipt :enclosing-receipt-id
    :identity-receipt-id :e9 :e9-identity-receipt :e9-identity-receipt-id
    :g9 :g9-lifecycle :g9-lifecycle-id :p9 :p9-publication :p9-final-discharge
    :p9-final-discharge-id :evidence-receipt :evidence-receipt-id
    :evidence-commit :evidence-tree :discharge :discharge-id})

(defn- reject-runtime-back-edges!
  ([value] (reject-runtime-back-edges! value []))
  ([value path]
   (cond
     (map? value)
     (doseq [[key child] value]
       (when (contains? runtime-back-edge-keys key)
         (fail! "SH07-B51-RECEIPT-JOIN" :runtime-identity-cycle
                {:path (conj path key)}))
       (reject-runtime-back-edges! child (conj path key)))

     (vector? value)
     (doseq [[ordinal child] (map-indexed vector value)]
       (reject-runtime-back-edges! child (conj path ordinal)))

     (list? value)
     (doseq [[ordinal child] (map-indexed vector value)]
       (reject-runtime-back-edges! child (conj path ordinal)))

     (set? value)
     (doseq [child value]
       (reject-runtime-back-edges! child (conj path :member)))

     :else nil)
   value))

(defn- validate-runtime-content!
  [content]
  (exact-keys! content #{:commit :tree}
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-content-keys)
  (require-git-id! (:commit content) :commit
                   "SH07-B51-CONTRACT-DISCHARGE")
  (require-git-id! (:tree content) :tree
                   "SH07-B51-CONTRACT-DISCHARGE")
  content)

(defn- validate-runtime-carrier-pool!
  [carriers]
  (exact-keys! carriers runtime-carrier-keys
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-carrier-keys)
  (doseq [[key value] carriers]
    (when (nil? value)
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-carrier-nil
             {:carrier key})))
  (validate-runtime-masked-root! (:runtime-masked-root carriers))
  (reject-runtime-back-edges! carriers [])
  carriers)

(defn- validate-runtime-phase-carriers!
  [carriers]
  (exact-keys! carriers runtime-phase-carrier-keys
               "SH07-B51-CONTRACT-DISCHARGE"
               :runtime-phase-carrier-keys)
  (validate-runtime-content! (:content carriers))
  (validate-evidence-eligibility! (:evidence-eligibility carriers))
  (validate-runtime-carrier-pool!
   (dissoc carriers :content :evidence-eligibility))
  (reject-runtime-back-edges! carriers [])
  carriers)

(defn validate-phase-evidence!
  "Validate the closed runtime PHASE-EVIDENCE wrapper.

  This runtime seam deliberately rejects PRE/POST carrier pools; later G9/P9
  validators must admit their own exact phase-specific maps."
  [phase-evidence]
  (exact-keys! phase-evidence phase-evidence-keys
               "SH07-B51-CONTRACT-DISCHARGE" :phase-evidence-keys)
  (when-not (and (= :gravity/sh07-b51-phase-evidence-v1
                    (:schema phase-evidence))
                 (= :runtime (:phase phase-evidence))
                 (= row-plan-id (:row-plan-id phase-evidence)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-phase-evidence-header))
  (validate-runtime-phase-carriers! (:carriers phase-evidence))
  phase-evidence)

(defn build-runtime-phase-evidence
  "Build the runtime PHASE-EVIDENCE wrapper from a strict 19-key runtime
  carrier pool plus its immutable C/T and EVIDENCE-ELIGIBILITY joins."
  ([content evidence-eligibility runtime-carriers]
   (validate-runtime-content! content)
   (validate-evidence-eligibility! evidence-eligibility)
   (validate-runtime-carrier-pool! runtime-carriers)
   (validate-phase-evidence!
    {:schema :gravity/sh07-b51-phase-evidence-v1
     :phase :runtime
     :row-plan-id row-plan-id
     :carriers (merge {:content content
                       :evidence-eligibility evidence-eligibility}
                      runtime-carriers)}))
  ([input]
   (exact-keys! input #{:content :evidence-eligibility :runtime-carriers}
                "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-phase-evidence-input-keys)
   (build-runtime-phase-evidence (:content input)
                                 (:evidence-eligibility input)
                                 (:runtime-carriers input))))

(defn build-runtime-phase-evidence!
  [& arguments]
  (apply build-runtime-phase-evidence arguments))

(def runtime-authority-attained
  [:a9-terminal-record-integrated :c-content-frozen
   :q-evidence-eligible :fresh-runtime-complete])

(def runtime-authority-deferred
  [:e9-evidence-immutable :i9-integrated :g9-integrated :p9-integrated
   :final-cleanup-complete])

(defn runtime-authority []
  {:phase :runtime
   :attained runtime-authority-attained
   :deferred runtime-authority-deferred
   :grants [:contract-discharge-only]
   :aggregate-sh07 progress
   :nonclaims nonclaims})

(def ^:private identity-receipt-normalization
  {:algorithm :gravity/sh07-b51-explicit-semantic-projection-v1
   :receipt-only :gravity/receipt-only
   :child-indexes [0 1]
   :classpath-indexes [0 1 2 3]
   :coordinator-classpath-phases
   [:m-entry :pre-a :post-a :pre-b :post-b :pre-source-session
    :post-source-session :m-exit]
   :recompute-order
   [:payload-value-ids :classpath-roots :argv-ids :request-ids
    :tcb-manifest-ids :response-values-and-transport :process-handle-ids
    :source-session-ids :coordinator-event-ids :coordinator-event-root
    :coordinator-receipt :m-response :launcher-validation-input
    :contract-validation :launcher-receipt-id
    :contract-document-event-root :contract-row-plan-id
    :pre-resolved-row-root :pre-discharge-id :runtime-resolved-row-root
    :runtime-discharge-id :receipt-id]})

(def ^:private prior-ref-container-keys
  #{:commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256})

(def ^:private runtime-i9-suffix
  (subvec (vec i9-lifecycle-paths) 160))

(def ^:private runtime-forbidden-path-refs
  [[:e9 0] [:e9 1] [:e9 2] [:e9 3] [:e9 4] [:e9 5] [:e9 6]
   [:i9-suffix 1] [:i9-suffix 2] [:i9-suffix 3]
   [:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
   [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]])

(def ^:private runtime-lifecycle-ref-keys
  #{:stage :path-ref :container :value-path :required-result :receipt-id})

(def ^:private runtime-forbidden-observation-keys
  #{:ordinal :path-ref :resolved-path :argv :exit-code
    :stdout-byte-count :stdout-raw-sha256 :parsed-result})

(defn- validate-reference-container!
  [container reason]
  (exact-keys! container prior-ref-container-keys
               "SH07-B51-CONTRACT-DISCHARGE" reason)
  (when-not
   (and (git-id? (:commit container))
        (git-id? (:tree container))
        (vector? (:parents container))
        (every? git-id? (:parents container))
        (safe-relative-path? (:path container))
        (= "100644" (:mode container))
        (= "blob" (:type container))
        (git-id? (:blob-oid container))
        (integer? (:byte-count container))
        (not (neg? (:byte-count container)))
        (sha256-id? (:raw-sha256 container)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" reason
           {:container container}))
  container)

(defn- runtime-forbidden-path
  [path-ref]
  (let [[domain ordinal] path-ref]
    (case domain
      :e9 (nth evidence-paths ordinal nil)
      :i9-suffix (nth runtime-i9-suffix ordinal nil)
      :g9 (nth g9-lifecycle-paths ordinal nil)
      :p9 (nth p9-lifecycle-paths ordinal nil)
      nil)))

(defn- valid-governance-resolved-path?
  [spec resolved-path]
  (and (string? resolved-path)
       (safe-relative-path? resolved-path)
       (str/starts-with?
        resolved-path (str (:directory spec) "/" (:workstream-id spec) "-"))
       (str/ends-with? resolved-path ".json")))
