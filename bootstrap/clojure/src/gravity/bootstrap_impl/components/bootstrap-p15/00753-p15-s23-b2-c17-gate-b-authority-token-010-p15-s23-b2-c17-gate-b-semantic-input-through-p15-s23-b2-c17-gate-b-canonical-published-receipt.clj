(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-semantic-input
  [artifact]
  (-> artifact
      (dissoc :semantic-id :artifact-id :actual-path-provenance
              :actual-path-binding-id :publication-receipt)
      (update :toolchain-evidence dissoc :physical-tool-provenance)
      (update :gate-a-artifact
              p15-s23-c13-c14-b1-path-neutral-value)
      p15-s23-c13-c14-b1-path-neutral-value))

(defn- p15-s23-b2-c17-gate-b-artifact-id
  [artifact]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b2-hosted-c17-gate-b
    :schema-version 1
    :record (p15-s23-b2-c17-gate-b-semantic-input artifact)}))

(defn- p15-s23-b2-c17-gate-b-path-binding-id
  [semantic-id provenance receipt]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b2-hosted-c17-gate-b-path-binding
    :schema-version 1 :semantic-id semantic-id
    :actual-path-provenance provenance
    :publication-receipt receipt}))

(defn- p15-s23-b2-c17-gate-b-final-record
  [gate-a contextual-report transaction publication-receipt]
  (let [b14 (p15-s23-b2-c17-gate-b-b14-record
             gate-a contextual-report transaction)
        c18 (p15-s23-b2-c17-gate-b-c18-record
             gate-a contextual-report transaction)
        b13 (p15-s23-b2-c17-gate-b-b13-record
             gate-a transaction b14 c18)
        toolchain-evidence
        {:artifact :gravity/b2-c17-gate-b-toolchain-evidence
         :schema-version 1
         :toolchain-fingerprint (:toolchain-fingerprint transaction)
         :tool-records (:tool-records transaction)
         :artifact-files (:artifact-files transaction)
         :physical-tool-provenance
         (:physical-tool-provenance transaction)
         :abi-evidence (:abi-evidence transaction)
         :runtime-provider-evidence
         (:runtime-provider-evidence transaction)
         :process-evidence (:process-evidence transaction)
         :publication-intent? (:publication-intent? transaction)}
        provenance
        {:source (get-in gate-a [:actual-path-provenance :source])
         :c11-source (get-in gate-a [:actual-path-provenance :c11-source])
         :c13-source (get-in gate-a [:actual-path-provenance :c13-source])
         :c14-source (get-in gate-a [:actual-path-provenance :c14-source])
         :b1-source (get-in gate-a [:actual-path-provenance :b1-source])
         :b2-source (get-in gate-a [:actual-path-provenance :b2-source])
         :physical-tool-provenance
         (:physical-tool-provenance transaction)
         :actual-output-directory
         (:actual-output-directory publication-receipt)}
        base
        {:artifact :gravity/b2-hosted-c17-gate-b
         :schema-version 1
         :status :validated-bounded-internal-c17-candidate
         :policy p15-s23-b2-c17-gate-b-policy
         :gate-a-artifact gate-a
         :gate-a-contextual-report contextual-report
         :toolchain-evidence toolchain-evidence
         :b13-record b13 :b14-record b14 :c18-record c18
         :diagnostics [] :whole-b2? false :public? false
         :release? false :self-hosted? false
         :seed-boundary? true :clojure-seed-boundary? true}
        semantic-id (p15-s23-b2-c17-gate-b-artifact-id base)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/b2-hosted-c17-gate-b-artifact
          :schema-version 1 :semantic-id semantic-id})
        result
        (assoc base :semantic-id semantic-id :artifact-id artifact-id
               :actual-path-provenance provenance
               :actual-path-binding-id
               (p15-s23-b2-c17-gate-b-path-binding-id
                semantic-id provenance publication-receipt)
               :publication-receipt publication-receipt)]
    (when-not (= p15-s23-b2-c17-gate-b-final-artifact-keys
                 (set (keys result)))
      (p15-s23-c-backend-fail!
       "B13-SCHEMA" (:source provenance) result
       {:missing-fact :exact-c17-gate-b-final-envelope}))
    result))

(defn- p15-s23-b2-c17-gate-b-sha256-value?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- p15-s23-b2-c17-gate-b-absolute-normalized-path?
  [value]
  (and (string? value) (<= 1 (count value) 4096)
       (<= 1 (alength (.getBytes ^String value
                                java.nio.charset.StandardCharsets/UTF_8))
           4096)
       (not (str/blank? value))
       (not (str/includes? value "\u0000"))
       (try
         (let [path (java.nio.file.Paths/get value (make-array String 0))]
           (and (.isAbsolute path)
                (= value (.toString (.normalize path)))))
         (catch Exception _ false))))

(defn- p15-s23-b2-c17-gate-b-canonical-published-receipt?
  [receipt output-directory]
  (let [hash-record?
        (fn [logical-path record]
          (and (map? record)
               (= #{:logical-path :byte-count :content-hash}
                  (set (keys record)))
               (= logical-path (:logical-path record))
               (integer? (:byte-count record))
               (<= 1 (:byte-count record) (* 8 1024 1024))
               (p15-s23-b2-c17-gate-b-sha256-value?
                (:content-hash record))))
        evidence (:publisher-evidence receipt)
        fixed-evidence
        {:provider :gravity/darwin-descriptor-publication
         :provider-version 1
         :jdk-version "26.0.1" :jdk-feature 26
         :native-access-enabled? true
         :ffi-provider :jdk-26-foreign-function-and-memory
         :native-library :darwin-libsystem :symbol "renameatx_np"
         :errno-read-policy :failure-only
         :guarantee-scope
         #{:descriptor-bound-parent :descriptor-relative-staging
           :resolve-beneath :no-symlink-traversal
           :exclusive-destination :unique-regular-files
           :exact-directory-inventory
           :no-extended-access-control-lists}
         :path-identity-linearization
         :held-parent-and-staging-directory-descriptors
         :flags {:rename-excl 4 :rename-nofollow-any 16
                 :rename-resolve-beneath 32 :combined 52}
         :commit-primitive :darwin-renameatx-np
         :source-directory-trailing-slash? true
         :postcommit-close-failures-change-result? false
         :crash-durable-publication? false
         :same-euid-concurrent-mutation-resistant? false}
        sidecars (:sidecar-hashes receipt)]
    (and (map? receipt)
         (= #{:status :actual-output-directory :sidecar-hashes
              :publisher-evidence :mode-policy}
            (set (keys receipt)))
         (= :published-atomically-after-final-verification
            (:status receipt))
         (= output-directory (:actual-output-directory receipt))
         (p15-s23-b2-c17-gate-b-absolute-normalized-path? output-directory)
         (= {:directory "0755" :executable "0755"
             :nonexecutable "0644"}
            (:mode-policy receipt))
         (= #{:manifest :provenance :conformance} (set (keys sidecars)))
         (hash-record? "manifest.edn" (:manifest sidecars))
         (hash-record? "provenance.edn" (:provenance sidecars))
         (hash-record? "conformance.edn" (:conformance sidecars))
         (= #{:provider :provider-version
              :jdk-version :jdk-feature :native-access-enabled?
              :ffi-provider :native-library :symbol :errno-read-policy
              :guarantee-scope :path-identity-linearization :flags
              :commit-primitive :source-directory-trailing-slash?
              :postcommit-close-failures-change-result?
              :crash-durable-publication?
              :same-euid-concurrent-mutation-resistant?
              :parent-identity-hash :staging-identity-hash}
            (set (keys evidence)))
         (= fixed-evidence
            (dissoc evidence
                    :parent-identity-hash :staging-identity-hash))
         (p15-s23-b2-c17-gate-b-sha256-value?
          (:parent-identity-hash evidence))
         (p15-s23-b2-c17-gate-b-sha256-value?
          (:staging-identity-hash evidence))))))
