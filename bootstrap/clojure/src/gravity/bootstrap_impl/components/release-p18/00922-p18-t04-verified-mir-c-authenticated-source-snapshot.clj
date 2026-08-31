

(defn- p18-t04-verified-mir-c-authenticated-source-snapshot!
  [source-path source-text evidence]
  (let [source-bytes
        (when (string? source-text)
          (.getBytes ^String source-text
                     java.nio.charset.StandardCharsets/UTF_8))
        snapshot-policy
        (:source-snapshot-policy
         p18-t04-experimental-verified-mir-c-route-policy)
        expected-policy-id (p15-s23-c11-mir-digest snapshot-policy)
        expected-content-hash
        (when source-bytes
          (str "sha256:" (sha256-bytes-hex source-bytes)))
        expected-phase-count
        (count (:identity-observation-phases snapshot-policy))]
    (when-not
     (and (string? source-path)
          source-bytes
          (map? evidence)
          (= p18-t04-verified-mir-c-source-snapshot-evidence-keys
             (set (keys evidence)))
          (= :gravity/p18-t04-bounded-source-snapshot (:kind evidence))
          (= 1 (:schema-version evidence))
          (= expected-policy-id (:policy-id evidence))
          (= source-path (:actual-path evidence))
          (string? (:file-key-hash evidence))
          (re-matches #"sha256:[0-9a-f]{64}" (:file-key-hash evidence))
          (= (alength ^bytes source-bytes) (:byte-count evidence))
          (<= 0 (:byte-count evidence)
              p18-t04-verified-mir-c-maximum-source-bytes)
          (= expected-content-hash (:content-hash evidence))
          (= (:capture-provider snapshot-policy)
             (:capture-provider evidence))
          (= (:native-functions snapshot-policy)
             (:native-functions evidence))
          (= expected-phase-count
             (:identity-observation-phase-count evidence))
          (= 0 (:source-byte-path-reopen-count evidence))
          (= :native-fstat-device-and-inode
             (:opened-handle-file-key-observation evidence))
          (true? (:opened-handle-size-parity? evidence))
          (true? (:path-and-descriptor-identity-parity? evidence))
          (= (.isNativeAccessEnabled (.getModule clojure.lang.RT))
             (:native-access-enabled? evidence))
          (= :captured (:status evidence)))
      (p18-t04-fail!
       "P18T04001"
       {:source (if (string? source-path)
                  source-path
                  "<verified-mir-c-source>")
        :missing-fields
        [:authenticated-descriptor-bound-source-snapshot-evidence]}))
    {:source-snapshot-evidence evidence
     :source-snapshot-evidence-id
     (p15-s23-c11-mir-digest
      {:kind :gravity/p18-t04-authenticated-source-snapshot-evidence
       :schema-version 1
       :evidence evidence})}))

(defn- p18-t04-experimental-verified-mir-c-gate-b-handoff!
  [source-path source-text source-snapshot-evidence output-directory gate-b]
  (let [{:keys [source-snapshot-evidence source-snapshot-evidence-id]}
        (p18-t04-verified-mir-c-authenticated-source-snapshot!
         source-path source-text source-snapshot-evidence)
        _ (p15-s23-b2-c17-gate-b-integrity-preflight! source-path gate-b)
        receipt (:publication-receipt gate-b)
        provenance (:actual-path-provenance gate-b)
        expected-semantic-id
        (p15-s23-b2-c17-gate-b-artifact-id gate-b)
        expected-artifact-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/b2-hosted-c17-gate-b-artifact
          :schema-version 1 :semantic-id expected-semantic-id})
        expected-path-binding-id
        (p15-s23-b2-c17-gate-b-path-binding-id
         expected-semantic-id provenance receipt)]
    (when-not
     (and (= p15-s23-b2-c17-gate-b-final-artifact-keys
             (set (keys gate-b)))
          (= :gravity/b2-hosted-c17-gate-b (:artifact gate-b))
          (= 1 (:schema-version gate-b))
          (= :validated-bounded-internal-c17-candidate (:status gate-b))
          (= p15-s23-b2-c17-gate-b-policy (:policy gate-b))
          (= source-path (:source provenance))
          (= output-directory (:actual-output-directory provenance))
          (= output-directory (:actual-output-directory receipt))
          (p15-s23-b2-c17-gate-b-canonical-published-receipt?
           receipt output-directory)
          (= [expected-semantic-id expected-artifact-id
              expected-path-binding-id]
             ((juxt :semantic-id :artifact-id :actual-path-binding-id)
              gate-b))
          (= [] (:diagnostics gate-b))
          (true? (get-in gate-b
                         [:toolchain-evidence :publication-intent?]))
          (false? (:whole-b2? gate-b))
          (false? (:public? gate-b))
          (false? (:release? gate-b))
          (false? (:self-hosted? gate-b))
          (true? (:seed-boundary? gate-b))
          (true? (:clojure-seed-boundary? gate-b))
          (not (contains? gate-b :publication-payload))
          (not (contains? (:toolchain-evidence gate-b)
                          :publication-payload)))
      (p18-t04-fail!
       "P18T04001"
       {:source source-path
        :missing-fields [:exact-published-c17-gate-b-handoff]}))
    {:gate-b gate-b
     :source-snapshot-evidence source-snapshot-evidence
     :source-snapshot-evidence-id source-snapshot-evidence-id
     :publication-receipt-id
     (p15-s23-c11-mir-digest
      {:kind :gravity/b2-c17-gate-b-publication-receipt
       :schema-version 1 :receipt receipt})}))

(defn- p18-t04-experimental-verified-mir-c-gate-b-summary
  [gate-b publication-receipt-id]
  {:artifact (:artifact gate-b)
   :schema-version (:schema-version gate-b)
   :status (:status gate-b)
   :semantic-id (:semantic-id gate-b)
   :artifact-id (:artifact-id gate-b)
   :actual-path-binding-id (:actual-path-binding-id gate-b)
   :policy (:policy gate-b)
   :publication-receipt (:publication-receipt gate-b)
   :publication-receipt-id publication-receipt-id
   :whole-b2? (:whole-b2? gate-b)
   :public? (:public? gate-b)
   :release? (:release? gate-b)
   :self-hosted? (:self-hosted? gate-b)
   :seed-boundary? (:seed-boundary? gate-b)
   :clojure-seed-boundary? (:clojure-seed-boundary? gate-b)})

(defn- p18-t04-experimental-verified-mir-c-route-record
  [source-path source-text source-snapshot-evidence output-directory gate-b]
  (let [{:keys [gate-b publication-receipt-id
                source-snapshot-evidence source-snapshot-evidence-id]}
        (p18-t04-experimental-verified-mir-c-gate-b-handoff!
         source-path source-text source-snapshot-evidence
         output-directory gate-b)
        summary
        (p18-t04-experimental-verified-mir-c-gate-b-summary
         gate-b publication-receipt-id)
        semantic-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/p18-t04-experimental-verified-mir-c-route
          :schema-version 1
          :route-policy p18-t04-experimental-verified-mir-c-route-policy
          :gate-b
          (select-keys summary [:semantic-id :artifact-id])})
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/p18-t04-experimental-verified-mir-c-route-artifact
          :schema-version 1 :semantic-id semantic-id})
        actual-path-provenance
        {:source source-path
         :source-snapshot-evidence-id source-snapshot-evidence-id
         :output-directory output-directory}
        actual-path-binding-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/p18-t04-experimental-verified-mir-c-route-path-binding
          :schema-version 1 :semantic-id semantic-id
          :actual-path-provenance actual-path-provenance
          :gate-b-actual-path-binding-id (:actual-path-binding-id summary)
          :gate-b-publication-receipt-id publication-receipt-id})
        result
        {:kind :gravity/p18-t04-experimental-verified-mir-c-route
         :schema-version 1 :task "P18-T04"
         :status
         :implemented-internal-candidate-public-exposure-disabled
         :route-policy p18-t04-experimental-verified-mir-c-route-policy
         :source
         {:kind (gravity-source-kind source-path)
          :extension (gravity-source-extension source-path)
          :content-hash (str "sha256:" (sha256-hex source-text))}
         :source-snapshot-evidence source-snapshot-evidence
         :source-snapshot-evidence-id source-snapshot-evidence-id
         :source-target :jvm :requested-target :c :profile :hosted
         :lowering-mode :verified-mir
         :command-boundary
         {:grammar (:command-grammar
                    p18-t04-experimental-verified-mir-c-route-policy)
          :output-kind :bundle-directory
          :experimental? true
          :governance-status :pending-feature-specific-review
          :activation :internal-proof-only
          :replacement :established-bootstrap-compile-routes}
         :gate-b-summary summary
         :semantic-id semantic-id :artifact-id artifact-id
         :actual-path-provenance actual-path-provenance
         :actual-path-binding-id actual-path-binding-id
         :diagnostics []
         :governance-status :pending-feature-specific-review
         :governance-conforming? false
         :security-review-complete? false
         :unsafe-review-complete? false
         :target-support-record-complete? false
         :t1-cli-conformance? false
         :p18-t04-proof-credited? false
         :experimental-use-notice
         (:experimental-use-notice
          p18-t04-experimental-verified-mir-c-route-policy)
         :public-command-route? false
         :public-target-support-claim? false
         :whole-b2? false :public? false :release? false :self-hosted? false
         :seed-boundary? true :clojure-seed-boundary? true}]
    (when-not
     (= p18-t04-experimental-verified-mir-c-route-artifact-keys
        (set (keys result)))
      (p18-t04-fail!
       "P18T04001"
       {:source source-path
        :missing-fields [:exact-experimental-verified-mir-c-route-envelope]}))
    result))