
(defn reconstruct-pre-object-snapshot!
  "Reconstruct the literal E4 C-object PRE carriers with exactly 209 bounded
  Git operations.  Terminal A9 and the acyclic pre-mask are authenticated
  solely from the requested C/T objects.  The result grants no authority."
  [{:keys [repository-root content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (require-git-id! (:commit content) :commit "SH07-B51-GIT-SNAPSHOT")
  (require-git-id! (:tree content) :tree "SH07-B51-GIT-SNAPSHOT")
  (let [repository (canonical-directory!
                    repository-root :repository-root
                    "SH07-B51-GIT-SNAPSHOT")
        show-c (show-operation! repository (:commit content))
        tree-c (ls-tree-operation! repository (:commit content) pre-c-paths)
        _ (when-not (= content
                       (select-keys (:parsed-result show-c) [:commit :tree]))
            (fail! "SH07-B51-GIT-SNAPSHOT" :content-coordinate
                   {:expected content :actual (:parsed-result show-c)}))
        rows (get-in tree-c [:parsed-result :rows])
        _ (doseq [[path row] (map vector pre-c-paths rows)]
            (require-present-blob-row! row path "SH07-B51-GIT-SNAPSHOT"))
        rows-by-path (into {} (map (juxt :path identity) rows))
        cat-results
        (mapv #(cat-file-bytes-operation!
                repository (:oid (get rows-by-path %)))
              pre-c-paths)
        bytes-by-path
        (into {} (map vector pre-c-paths (map :bytes cat-results)))
        show-base (show-operation! repository pre-baseline-commit)
        base-tree (get-in show-base [:parsed-result :tree])
        tree-base (ls-tree-operation! repository pre-baseline-commit
                                      [boot5-path])
        baseline-row (first (get-in tree-base [:parsed-result :rows]))
        _ (require-present-blob-row! baseline-row boot5-path
                                     "SH07-B51-GIT-SNAPSHOT")
        baseline-cat (cat-file-bytes-operation! repository (:oid baseline-row))
        observations
        (mapv (fn [ordinal observation] (assoc observation :ordinal ordinal))
              (range 209)
              (concat [show-c tree-c]
                      (map :observation cat-results)
                      [show-base tree-base (:observation baseline-cat)]))
        static-paths
        (vec (concat (map second document-paths) owned-c-paths a9-pre-paths))
        decoded-static
        (into {}
              (map (fn [path]
                     [path (decode-utf8!
                            (get bytes-by-path path)
                            "SH07-B51-GIT-SNAPSHOT" :pre-static-utf8)]))
              static-paths)
        document-read-receipt
        (build-document-read-receipt content rows-by-path bytes-by-path)
        source-descriptors
        (build-source-descriptors rows-by-path bytes-by-path)
        owned-c-descriptors
        (build-owned-c-descriptors rows-by-path bytes-by-path)
        boot5-compatibility
        (build-boot5-compatibility
         content (:parsed-result show-base)
         (get rows-by-path boot5-path) (get bytes-by-path boot5-path)
         baseline-row (:bytes baseline-cat))
        a9-stage-result
        (build-a9-stage-result-e1
         content rows-by-path bytes-by-path decoded-static)
        pre-masked-root
        (build-pre-masked-root bytes-by-path decoded-static)
        ownership-plan (build-v9-ownership-plan-e4)
        registries
        (build-registries-e4 (get decoded-static architecture-report-path))
        foundation-static
        (build-foundation-static content (:documents document-read-receipt))
        catalog-projection
        (build-catalog-projection document-read-receipt decoded-static)
        proof-scope (build-proof-scope catalog-projection decoded-static)
        publication-boundary (build-publication-boundary content)]
    (when-not (and (= 209 (count observations))
                   (= (vec (range 209)) (mapv :ordinal observations))
                   (= 196 (count static-paths))
                   (= 196 (count (distinct static-paths)))
                   (= 33 (count (:documents document-read-receipt)))
                   (= 33 (count (:events document-read-receipt)))
                   (= 13 (count source-descriptors))
                   (= resolution-source-descriptor
                      (nth source-descriptors 12))
                   (= 2 (count (:preimage-files pre-masked-root)))
                   (= 160 (count owned-c-descriptors))
                   (= 3 (count (:a9-artifact-descriptors a9-stage-result))))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :pre-object-census))
    {:content content
     :baseline {:commit pre-baseline-commit :tree base-tree}
     :pre-c-paths pre-c-paths
     :observations observations
     :git-operation-count 209
     :document-read-receipt document-read-receipt
     :source-descriptors source-descriptors
     :source-descriptors-root
     (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
               source-descriptors])
     :owned-c-descriptors owned-c-descriptors
     :owned-c-root
     (codec/H [:gravity/sh07-b51-owned-c-descriptors-root-v1
               owned-c-descriptors])
     :boot5-compatibility boot5-compatibility
     :pre-masked-root pre-masked-root
     :a9-stage-result a9-stage-result
     :ownership-plan ownership-plan
     :governance-rules governance-rules-e1
     :v9-tag-registry (:tag-registry registries)
     :identity-domain-registry (:identity-domain-registry registries)
     :foundation-static foundation-static
     :catalog-projection catalog-projection
     :proof-scope proof-scope
     :publication-boundary publication-boundary
     :progress progress
     :static-decode-count (count decoded-static)
     :decoded-static decoded-static
     :completion-gaps []
     :authority :none}))

(defn validate-content-object!
  "Validate exact immutable C/T with the closed show grammar."
  [{:keys [repository-root content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (require-git-id! (:commit content) :commit "SH07-B51-GIT-SNAPSHOT")
  (require-git-id! (:tree content) :tree "SH07-B51-GIT-SNAPSHOT")
  (let [repository (canonical-directory!
                    repository-root :repository-root
                    "SH07-B51-GIT-SNAPSHOT")
        observation (show-operation! repository (:commit content))]
    (when-not (= content (select-keys (:parsed-result observation)
                                     [:commit :tree]))
      (fail! "SH07-B51-GIT-SNAPSHOT" :content-coordinate
             {:expected content :actual (:parsed-result observation)}))
    {:content content
     :parents (get-in observation [:parsed-result :parents])
     :observation (assoc observation :ordinal 0)
     :authority :none}))

(def eligibility-coordinate-keys
  #{:schema :commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256 :receipt-id})

(def evidence-eligibility-keys
  #{:coordinate :receipt-id :freeze-state :freeze-record-id
    :source-descriptors-root :pre-masked-root :pre-discharge-id
    :participant-check-roots})

(def runtime-carrier-keys
  #{:source-session-a :source-session-b :source-session-m
    :launcher :coordinator :checker-a :checker-b :runtime-masked-root
    :foundation-runtime :bootstrap-runtime :differential-runtime
    :verification-runtime :self-hosting-runtime :semantic-witness-runtime
    :contract-test-results :proof-scope :progress :nonclaims})

(def runtime-phase-carrier-keys
  (conj runtime-carrier-keys :content :evidence-eligibility))

(def phase-evidence-keys #{:schema :phase :row-plan-id :carriers})

(def contract-discharge-keys
  #{:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
    :resolved-rows :resolved-root :authority :self-id-omits :discharge-id})

(def runtime-prior-ref-keys
  #{:phase :container :value-path :discharge-id :lifecycle-ref
    :forbidden-path-observations})

(def ^:private eligibility-check-keys
  #{:coordinate :observations :pre-discharge-observations :decode-count
    :receipt-id-check-count :pre-discharge-reconstruction-count
    :pre-discharge-git-operation-count
    :pre-discharge-document-descriptor-count
    :pre-discharge-source-descriptor-count
    :pre-discharge-pre-masked-file-count
    :pre-discharge-owned-c-artifact-count
    :pre-discharge-a9-artifact-count
    :pre-discharge-baseline-boot5-count
    :pre-discharge-static-decode-count :content-match
    :source-descriptors-match :source-descriptors-root-match
    :pre-masked-root-match :owned-c-root-match :pre-discharge-match})

(def eligibility-participant-order [:l :m :a :b])

(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :pre-masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})

(def ^:private pre-discharge-keys
  #{:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
    :resolved-rows :resolved-root :authority :self-id-omits :discharge-id})

(def ^:private pre-carrier-keys
  #{:content :document-read-receipt :source-descriptors
    :source-descriptors-root :pre-masked-root :owned-c-descriptors :owned-c-root
    :row-plan :foundation-static :boot5-compatibility :ownership-plan
    :catalog-projection :governance-rules :identity-domain-registry
    :publication-boundary :proof-scope :progress :nonclaims :a9-stage-result})

(defn- validate-source-descriptors!
  [descriptors]
  (when-not (and (vector? descriptors) (= 13 (count descriptors)))
    (fail! "SH07-B51-RECEIPT-JOIN" :source-descriptor-count))
  (doseq [[expected descriptor] (map vector source-specs descriptors)]
    (exact-keys! descriptor
                 #{:ordinal :kind :module-ordinal :path :mode :blob-oid
                   :byte-count :raw-sha256}
                 "SH07-B51-RECEIPT-JOIN" :source-descriptor-keys)
    (let [[ordinal kind module-ordinal path] expected]
      (when-not (and (= [ordinal kind module-ordinal path "100644"]
                        ((juxt :ordinal :kind :module-ordinal :path :mode)
                         descriptor))
                     (git-id? (:blob-oid descriptor))
                     (integer? (:byte-count descriptor))
                     (not (neg? (:byte-count descriptor)))
                     (sha256-id? (:raw-sha256 descriptor)))
        (fail! "SH07-B51-RECEIPT-JOIN" :source-descriptor
               {:ordinal ordinal :descriptor descriptor}))))
  descriptors)
