(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-integrity-preflight!
  [source-path artifact]
  (let [validation
        (p15-s23-trusted-carrier-validation
         artifact :default-only
         p15-s23-c13-c14-b1-max-carrier-nodes
         p15-s23-c13-c14-b1-max-carrier-depth
         p15-s23-c13-c14-b1-max-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-c-backend-fail!
       "B13-SCHEMA" source-path {}
       (merge {:missing-fact :trusted-bounded-c17-gate-b-final-carrier}
              (select-keys validation
                           [:reason :observed-nodes :observed-depth
                            :maximum-nodes :maximum-depth :maximum-width]))))
    (try
      (p15-s23-c11-mir-bounded-value!
       source-path :c17-gate-b-final-artifact artifact
       p15-s23-c13-c14-b1-max-carrier-nodes
       p15-s23-c13-c14-b1-max-carrier-depth)
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-c-backend-fail!
         "B13-SCHEMA" source-path {}
         {:missing-fact :bounded-c17-gate-b-final-scalars
          :bounded-reason
          (or (:missing-fact (ex-data exception))
              :contained-c17-gate-b-carrier)})))
    (when-not
     (and (map? artifact)
          (contains? p15-s23-trusted-carrier-map-classes
                     (.getName (class artifact)))
          (= p15-s23-b2-c17-gate-b-final-artifact-keys
             (set (keys artifact)))
          (= :gravity/b2-hosted-c17-gate-b (:artifact artifact))
          (= 1 (:schema-version artifact))
          (= :validated-bounded-internal-c17-candidate (:status artifact))
          (= p15-s23-b2-c17-gate-b-policy (:policy artifact))
          (= [] (:diagnostics artifact))
          (true? (:seed-boundary? artifact))
          (true? (:clojure-seed-boundary? artifact))
          (false? (:whole-b2? artifact)) (false? (:public? artifact))
          (false? (:release? artifact)) (false? (:self-hosted? artifact))
          (= :passed (get-in artifact
                             [:gate-a-contextual-report :status]))
          (= (:artifact-id (:gate-a-artifact artifact))
             (get-in artifact [:gate-a-contextual-report :artifact-id]))
          (= (:semantic-id (:gate-a-artifact artifact))
             (get-in artifact [:gate-a-contextual-report :semantic-id]))
          (= :content-addressed-internal-candidate
             (get-in artifact [:b13-record :status]))
          (= :passed-for-bounded-positive-slice
             (get-in artifact [:b14-record :status]))
          (= :passed-for-experimental-bounded-slice
             (get-in artifact [:c18-record :status]))
          (= :blocked (get-in artifact [:c18-record :release-result])))
      (p15-s23-c-backend-fail!
       "B13-SCHEMA" source-path artifact
       {:missing-fact :exact-c17-gate-b-final-envelope}))
    (p15-s23-b2-c17-verification-preflight!
     source-path (:gate-a-artifact artifact))
    (let [toolchain (:toolchain-evidence artifact)
          provenance (:actual-path-provenance artifact)
          receipt (:publication-receipt artifact)
          publication-intent? (:publication-intent? toolchain)
          output-directory (:actual-output-directory provenance)
          expected-toolchain-keys
          #{:artifact :schema-version :toolchain-fingerprint :tool-records
            :artifact-files :physical-tool-provenance :abi-evidence
            :runtime-provider-evidence :process-evidence :publication-intent?}
          expected-provenance-keys
          #{:source :c11-source :c13-source :c14-source :b1-source
            :b2-source :physical-tool-provenance
            :actual-output-directory}
          receipt-keys (when (map? receipt) (set (keys receipt)))
          ephemeral-receipt
          {:status :ephemeral-conformance-artifacts
           :actual-output-directory nil
           :sidecar-hashes {}}]
      (when-not
       (and (map? toolchain)
            (= expected-toolchain-keys (set (keys toolchain)))
            (= :gravity/b2-c17-gate-b-toolchain-evidence
               (:artifact toolchain))
            (= 1 (:schema-version toolchain))
            (map? provenance)
            (= expected-provenance-keys (set (keys provenance)))
            (= (select-keys provenance
                            [:source :c11-source :c13-source :c14-source
                             :b1-source :b2-source])
               (select-keys (:actual-path-provenance
                             (:gate-a-artifact artifact))
                            [:source :c11-source :c13-source :c14-source
                             :b1-source :b2-source]))
            (= (:physical-tool-provenance provenance)
               (:physical-tool-provenance toolchain))
            (or
             (and (false? publication-intent?)
                  (nil? output-directory)
                  (= ephemeral-receipt receipt))
             (and (true? publication-intent?)
                  (some? receipt-keys)
                  (p15-s23-b2-c17-gate-b-canonical-published-receipt?
                   receipt output-directory))))
        (p15-s23-c-backend-fail!
         "B13-SCHEMA" source-path artifact
         {:missing-fact :exact-c17-gate-b-evidence-and-provenance-envelope})))
    (let [toolchain (:toolchain-evidence artifact)
          transaction
          (select-keys
           toolchain
           [:tool-records :toolchain-fingerprint :artifact-files
            :physical-tool-provenance :abi-evidence
            :runtime-provider-evidence :process-evidence :publication-intent?])
          gate-a (:gate-a-artifact artifact)
          contextual (:gate-a-contextual-report artifact)
          expected-b14
          (p15-s23-b2-c17-gate-b-b14-record
           gate-a contextual transaction)
          expected-c18
          (p15-s23-b2-c17-gate-b-c18-record
           gate-a contextual transaction)
          expected-b13
          (p15-s23-b2-c17-gate-b-b13-record
           gate-a transaction expected-b14 expected-c18)]
      (when-not (= [expected-b13 expected-b14 expected-c18]
                   ((juxt :b13-record :b14-record :c18-record) artifact))
        (p15-s23-c-backend-fail!
         "B13-EVIDENCE" source-path artifact
         {:missing-fact :strictly-reconstructed-c17-gate-b-evidence})))
    (doseq [[kind record]
            [[:gravity/b14-bounded-hosted-c17-conformance
              (:b14-record artifact)]
             [:gravity/c18-bounded-hosted-c17-verification
              (:c18-record artifact)]
             [:gravity/b13-bounded-hosted-c17-emission
              (:b13-record artifact)]]]
      (when-not (= (:record-id record)
                   (p15-s23-b2-c17-gate-b-record-id
                    kind (dissoc record :record-id)))
        (p15-s23-c-backend-fail!
         "B13-HASH" source-path artifact
         {:missing-fact :recomputable-c17-gate-b-evidence-record})))
    (let [b13 (:b13-record artifact)
          files (:artifact-files b13)
          content-hashes
          (into (sorted-map)
                (map (fn [[kind record]]
                       [kind (:content-hash record)]))
                files)]
      (when-not
       (and (= content-hashes (:content-hashes b13))
            (= #{:source :header :object :executable}
               (set (keys files)))
            (= ["program.c" "program.h" "program.o" "program"
                "manifest.edn" "provenance.edn" "conformance.edn"]
               (:publication-file-set b13))
            (= (:record-id (:b14-record artifact))
               (get-in b13 [:evidence :conformance]))
            (= (:record-id (:c18-record artifact))
               (get-in b13 [:evidence :compiler-verification]))
            (= (:process-evidence (:toolchain-evidence artifact))
               (get-in artifact [:b14-record :process-evidence])))
        (p15-s23-c-backend-fail!
         "B13-GRAPH" source-path artifact
         {:missing-fact :content-bound-c17-gate-b-artifact-graph})))
    (let [semantic-id (p15-s23-b2-c17-gate-b-artifact-id artifact)
          artifact-id
          (p15-s23-c11-mir-digest
           {:kind :gravity/b2-hosted-c17-gate-b-artifact
            :schema-version 1 :semantic-id semantic-id})
          binding-id
          (p15-s23-b2-c17-gate-b-path-binding-id
           semantic-id (:actual-path-provenance artifact)
           (:publication-receipt artifact))]
      (when-not (= [semantic-id artifact-id binding-id]
                   ((juxt :semantic-id :artifact-id
                          :actual-path-binding-id) artifact))
        (p15-s23-c-backend-fail!
         "B13-HASH" source-path artifact
         {:missing-fact :recomputable-c17-gate-b-final-identities})))
    :passed))

(defn- p15-s23-b2-c17-gate-b-output-preflight!
  [candidate output-directory source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :exclusive-c17-publication-output-preflight)
  (when output-directory
    ;; Opening the provider target authenticates native authority, parent and
    ;; destination before compiler tool effects.  It returns an opaque held
    ;; descriptor context, not a caller-authorized path map.
    (p15-s23-b2-c17-gate-b-provider-call!
     source-path :open
     #(darwin-publication/open-target! output-directory))))

(defn- p15-s23-b2-c17-gate-b-canonical-sidecar
  [logical-path record]
  (let [canonical (c-backend-canonical-value record)
        bytes
        (.getBytes (str (pr-str canonical) "\n")
                   java.nio.charset.StandardCharsets/UTF_8)]
    (when-not (and (contains? #{"manifest.edn" "provenance.edn"
                                "conformance.edn"}
                              logical-path)
                   (<= 1 (alength ^bytes bytes) (* 8 1024 1024)))
      (p15-s23-c-backend-fail!
       "B13-SCHEMA" "<c17-sidecar>" {}
       {:missing-fact :bounded-canonical-c17-sidecar
        :logical-path logical-path
        :maximum-byte-count (* 8 1024 1024)
        :observed-byte-count (alength ^bytes bytes)}))
    {:record canonical
     :bytes bytes
     :hash-record
     {:logical-path logical-path
      :byte-count (alength ^bytes bytes)
      :content-hash
      (p15-s23-b2-c17-gate-b-sha256-bytes bytes)}})))
