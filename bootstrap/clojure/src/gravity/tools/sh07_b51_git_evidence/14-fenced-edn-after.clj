
(defn- fenced-edn-after! [text marker]
  (let [marker-start (str/index-of text marker)
        fence-start (when marker-start
                      (str/index-of text "```clojure\n" marker-start))
        value-start (when fence-start (+ fence-start 11))
        value-end (when value-start (str/index-of text "\n```" value-start))]
    (when-not (and marker-start fence-start value-end)
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :registry-literal
             {:marker marker}))
    (binding [*read-eval* false]
      (edn/read-string (subs text value-start value-end)))))

(defn- build-registries-e4 [architecture-report]
  (let [old-tags (fenced-edn-after! architecture-report
                                      "`V9-TAG-REGISTRY` is")
        old-domains (fenced-edn-after! architecture-report
                                         "`H-DOMAIN-REGISTRY` has")
        member-match
        (re-find #"(?s)Member kinds are exactly\s+`(\[.*?\])`"
                 architecture-report)
        member-kinds
        (when member-match
          (binding [*read-eval* false] (edn/read-string (second member-match))))
        sorted-keywords
        (fn [values]
          (vec (sort #(unsigned-utf8-compare (str %1) (str %2)) values)))
        tags
        (sorted-keywords
         (conj (remove #{:gravity/sh07-b51-a9-lifecycle-receipt-v1
                         :gravity/sh07-b51-contract-predicate-fixture-v1
                         :gravity/sh07-b51-v9-ownership-plan-v1
                         :gravity/sh07-b51-v9-ownership-plan-e1
                         :gravity/sh07-b51-v9-ownership-plan-e2-v1
                         :gravity/sh07-b51-v9-ownership-plan-e3-v1}
               old-tags)
               :gravity/sh07-b51-a9-stage-result-e1
               :gravity/sh07-b51-contract-predicate-fixture-v2
               :gravity/sh07-b51-v9-ownership-plan-e4-v1
               :gravity/sh07-b51-loaded-namespace-root-v2
               :gravity/sh07-b51-source-closure-root-v2))
        domains
        (sorted-keywords
         (conj (remove #{:gravity/sh07-b51-a9-lifecycle-receipt-v1
                         :gravity/sh07-b51-contract-predicate-fixture-v1
                         :gravity/sh07-b51-v9-ownership-plan-v1
                         :gravity/sh07-b51-v9-ownership-plan-e1
                         :gravity/sh07-b51-v9-ownership-plan-e2-v1
                         :gravity/sh07-b51-v9-ownership-plan-e3-v1}
               old-domains)
               :gravity/sh07-b51-contract-predicate-fixture-v2
               :gravity/sh07-b51-v9-ownership-plan-e4-v1
               :gravity/sh07-b51-loaded-namespace-root-v2
               :gravity/sh07-b51-source-closure-root-v2))]
    (when-not
     (and (vector? member-kinds)
          (= (count tags) (count (distinct tags)))
          (= (count domains) (count (distinct domains)))
          (every? (set tags) domains)
          (contains? (set tags) :gravity/sh07-b51-a9-stage-result-e1)
          (contains? (set tags)
                     :gravity/sh07-b51-v9-ownership-plan-e4-v1)
          (contains? (set tags)
                     :gravity/sh07-b51-contract-predicate-fixture-v2)
          (contains? (set domains)
                     :gravity/sh07-b51-v9-ownership-plan-e4-v1)
          (contains? (set domains)
                     :gravity/sh07-b51-contract-predicate-fixture-v2)
          (not (contains? (set tags)
                          :gravity/sh07-b51-contract-predicate-fixture-v1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-contract-predicate-fixture-v1))
          (not (contains? (set tags)
                          :gravity/sh07-b51-v9-ownership-plan-e2-v1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-v9-ownership-plan-e2-v1))
          (not (contains? (set tags)
                          :gravity/sh07-b51-v9-ownership-plan-e3-v1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-v9-ownership-plan-e3-v1))
          (not (contains? (set tags)
                          :gravity/sh07-b51-v9-ownership-plan-e1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-v9-ownership-plan-e1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-a9-stage-result-e1)))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :registry-delta))
    {:tag-registry tags
     :identity-domain-registry
     {:domains domains :member-kinds member-kinds}}))

(defn- read-static-edn! [text reason]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (binding [*read-eval* false *data-readers* {}
              *default-data-reader-fn*
              (fn [tag _]
                (fail! "SH07-B51-CONTRACT-DISCHARGE" reason {:tag tag}))]
      (let [value (read {:eof ::eof :read-cond :allow} reader)
            trailing (read {:eof ::eof :read-cond :allow} reader)]
        (when (or (= ::eof value) (not= ::eof trailing))
          (fail! "SH07-B51-CONTRACT-DISCHARGE" reason))
        value))))

(def ^:private catalog-route-ids
  ["sh07-authoritative" "sh07-b10-regression" "sh07-b11-contract"
   "sh07-b13-accepted" "sh07-b8-regression" "sh07-cached-accepted"
   "sh07-cached-contract" "sh07-cached-identity" "sh07-cached-rejected"
   "sh07-cached-verification" "sh07-conformance-parity"
   "sh07-conformance-table" "sh07-parallel-accepted"
   "sh07-parallel-b12-contract-accepted"
   "sh07-parallel-b12-identity-verification"
   "sh07-parallel-b12-rejected-regression"
   "sh07-parallel-identity-lineage" "sh07-parallel-regression-a"
   "sh07-parallel-regression-b" "sh07-parallel-rejected"
   "sh07-parallel-verification"])

(defn- build-foundation-static [content documents]
  (let [seven-descriptors (subvec documents 1 8)]
    {:content content
     :evidence-kind :validation
     :canonical-pipeline-contract-id
     (codec/H [:gravity/sh07-b51-foundation-contract-root-v1
               seven-descriptors])
     :safe-semantics
     {:allowed-outcomes
      [:proven-safe :runtime-checked :rejected :unsafe-island]
      :claims []}
     :replay-key-fields
     [:content-commit :content-tree :source-root :checker-root
      :architecture-report-sha256]
     :invalidation-key-fields
     [:content-commit :content-tree :source-root :checker-root
      :architecture-report-sha256 :governing-document-root]}))

(defn- build-catalog-projection
  [document-read-receipt decoded-static]
  (let [catalog
        (read-static-edn!
         (get decoded-static "contracts/verification-catalog.edn")
         :verification-catalog-edn)
        actual-route-ids
        (->> (:routes catalog)
             (keep (fn [[id route]]
                     (when (= "sh07-checked-core" (:module-id route)) id)))
             (sort unsigned-utf8-compare)
             vec)]
    (when-not (= catalog-route-ids actual-route-ids)
      (fail! "SH07-B51-VERIFY-CATALOG" :applicable-route-census
             {:expected catalog-route-ids :actual actual-route-ids}))
    {:schema (:schema catalog)
     :version (:version catalog)
     :authority (:authority catalog)
     :authoritative? (:authoritative? catalog)
     :source-descriptor
     (nth (:documents document-read-receipt) 27)
     :module (get-in catalog [:modules "sh07-checked-core"])
     :applicable-routes
     (mapv (fn [id] [id (get-in catalog [:routes id])]) catalog-route-ids)}))

(defn- build-proof-scope [catalog-projection decoded-static]
  (let [contract
        (read-static-edn!
         (get decoded-static
              "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
         :proof-contract-edn)
        boundary (:boundary contract)]
    {:evidence-kind :validation
     :formal-proof false
     :candidate-scope :sh07-b51-git-object-publication
     :lowering-boundary :verified-core
     :mir-artifacts []
     :backend-artifacts []
     :stage-support-claims []
     :proof-contract
     {:artifact (:artifact contract)
      :schema-version (:schema-version contract)
      :status (:status contract)
      :coverage-milestone (:coverage-milestone contract)
      :task (:task boundary)
      :input-task (:input-task boundary)
      :scope (:scope boundary)
      :application :preserved-out-of-scope}
     :verification-catalog catalog-projection
     :adopted-independent-controls
     [:fresh-authoritative-process :immutable-source-snapshot
      :immutable-receipts :bounded-execution :finally-cleanup]
     :b51-proof-contract-revision-required true}))

(defn- build-publication-boundary [content]
  {:content content
   :scope :verified-core
   :created-paths evidence-paths
   :created-kinds
   [:checker-identity :checker-identity :checker-output :checker-output
    :certificate-pair :linked-carrier :identity-receipt]
   :mir-artifacts [] :backend-artifacts [] :stage-support-claims []})

(def ^:private progress
  {:slice :SH-07 :status :partial
   :completed-stage-count 4 :total-stage-count 8})

(defn- pre-evidence-authority []
  {:phase :pre-evidence
   :attained [:a9-terminal-record-integrated]
   :deferred [:c-content-frozen :q-evidence-eligible
              :fresh-runtime-complete :e9-evidence-immutable
              :i9-integrated :g9-integrated :p9-integrated
              :final-cleanup-complete]
   :grants [:contract-discharge-only]
   :aggregate-sh07 progress
   :nonclaims nonclaims})
