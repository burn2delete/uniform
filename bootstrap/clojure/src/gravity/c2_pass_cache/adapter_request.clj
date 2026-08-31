;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(def ^:private adapter-version 2)
(def ^:private adapter-input-kind :gravity/c2-source-unit)
(def ^:private adapter-output-kind
  :gravity/stage0-c2-reader-document-artifact)
(def ^:private adapter-input-facts
  #{:source-unit :reader-policy :source-snapshot :project-binding
    :compiler-binding :pass-binding :dependency-binding
    :build-effect-binding :capability-binding :facet-binding
    :profile-binding :target-binding :boundary-binding})

(def ^:private adapter-pass-contract
  {:pass :c2-reader-cache-adapter
   :version "2"
   :order 2
   :input adapter-input-kind
   :output adapter-output-kind
   :requires adapter-input-facts
   :preserves adapter-input-facts
   :invalidates #{}
   :regenerates #{:c2-reader-artifact}
   :replacement-evidence {}
   :emits #{adapter-output-kind}
   :effects #{}
   :capabilities #{}
   :profiles #{}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def ^:private legacy-request-fields
  [:source-unit :source-snapshot :reader-policy :project-binding
   :compiler-binding :pass-binding :dependency-binding
   :build-effect-binding :capability-binding :facet-binding
   :profile-binding :target-binding :boundary-binding])

(defn- validated-legacy-key-request!
  [key]
  (when-not (map? key)
    (cache-fail! "C16-KEY" "C2 cache key must be a map" {}))
  (let [preimage (:semantic-preimage key)
        request (assoc (select-keys preimage legacy-request-fields)
                       :path-provenance (:path-provenance key))
        recomputed (cache-key request)]
    (when-not (= key recomputed)
      (cache-fail! "C16-STALE"
                   "C2 cache key contains a stale compatibility projection"
                   {:semantic-key-id (:semantic-key-id key)}))
    request))

(defn- adapter-projection-id
  [domain value]
  (canonical-content-id {:domain domain
                         :adapter-version adapter-version
                         :value value}))

(defn- adapter-stage-request!
  [legacy-key operations]
  (let [legacy-request (validated-legacy-key-request! legacy-key)
        operations (required-validation-ops! operations)
        source-id (get-in legacy-request [:source-unit :source-id])
        reader-policy (:reader-policy legacy-request)
        adapter-id (adapter-projection-id
                    :gravity/c2-pass-cache-v2-request legacy-key)
        producer-id (adapter-projection-id
                     :gravity/c2-pass-cache-v2-producer-binding
                     {:current-binding (:current-binding operations)
                      :compiler-binding (:compiler-binding legacy-request)
                      :pass-binding (:pass-binding legacy-request)
                      :boundary-binding (:boundary-binding legacy-request)})
        diagnostic-schema-id
        (let [candidate (:standard-reader-policy-id reader-policy)]
          (if (sha256-id? candidate)
            candidate
            (adapter-projection-id
             :gravity/c2-pass-cache-v2-diagnostic-schema reader-policy)))
        policy-ids
        (->> [(:extension-policy reader-policy)
              (:standard-reader-policy-id reader-policy)
              adapter-id]
             (filter sha256-id?)
             distinct
             sort
             vec)]
    {:stage (:pass adapter-pass-contract)
     :contract adapter-pass-contract
     :producer-binding-id producer-id
     :input-artifact-ids [source-id]
     :input-facts adapter-input-facts
     :external-root-inputs
     {source-id {:kind adapter-input-kind :facts adapter-input-facts}}
     :semantic-bindings
     {:compiler-id (get-in legacy-request [:compiler-binding :compiler-id])
      :capability-policy-id
      (get-in legacy-request [:capability-binding :identity])
      :facet-set-id (get-in legacy-request [:facet-binding :identity])
      :provider-manifest-id adapter-id
      :package-lock-id (get-in legacy-request [:dependency-binding :identity])
      :diagnostic-schema-id diagnostic-schema-id}
     :dependency-graph-id
     (get-in legacy-request [:dependency-binding :identity])
     :build-effect-replay-id
     (get-in legacy-request [:build-effect-binding :identity])
     :profile-id
     (adapter-projection-id :gravity/c2-pass-cache-v2-profile
                            (:profile-binding legacy-request))
     :target-id
     (adapter-projection-id :gravity/c2-pass-cache-v2-target
                            (:target-binding legacy-request))
     :policy-ids policy-ids
     :provenance
     {:provenance-id
      (adapter-projection-id :gravity/c2-pass-cache-v2-path-provenance
                             (:path-provenance legacy-request))
      :source-path (get-in legacy-request [:path-provenance :canonical-path])
      :metadata {:legacy-semantic-key-id (:semantic-key-id legacy-key)
                 :legacy-storage-key-id (:storage-key-id legacy-key)}}
     :diagnostic-stream-id
     (adapter-projection-id :gravity/c2-pass-cache-v2-diagnostic-stream
                            {:reader-policy reader-policy
                             :source-id source-id})
     :execution-mode :executed
     :authority {:input-authorities {source-id :none}
                 :claimed-level :none
                 :scope :c2-reader-cache}}))

(def ^:private adapter-envelope-fields
  #{:artifact :schema-version :adapter-version :c2-artifact-id
    :boundary-projection-id :payload-id :payload-bytes})

(defn- adapter-envelope!
  [artifact _legacy-key operations _entry]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        boundary-id ((:boundary-projection-id-of operations) artifact)
        _ (ensure-sha256-id! :artifact-id artifact-id)
        _ (ensure-sha256-id! :boundary-projection-id boundary-id)
        payload (encode-canonical-bytes artifact {:reject-metadata? false}
                                        maximum-blob-bytes)
        payload-id (str "sha256:" (digest/sha256-bytes-hex payload))]
    {:artifact :gravity/c2-pass-cache-v2-envelope
     :schema-version 1
     :adapter-version adapter-version
     :c2-artifact-id artifact-id
     :boundary-projection-id boundary-id
     :payload-id payload-id
     :payload-bytes payload}))
