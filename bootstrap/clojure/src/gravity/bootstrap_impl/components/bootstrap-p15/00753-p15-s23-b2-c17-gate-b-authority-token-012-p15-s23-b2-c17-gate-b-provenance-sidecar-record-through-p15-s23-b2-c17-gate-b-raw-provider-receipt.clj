(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-provenance-sidecar-record
  [artifact]
  {:artifact :gravity/b13-final-bounded-hosted-c17-provenance
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :semantic-id (:semantic-id artifact)
   :build-id (get-in artifact [:b13-record :build-id])
   :b13-record (:b13-record artifact)
   :c18-record (:c18-record artifact)})

(defn- p15-s23-b2-c17-gate-b-conformance-sidecar-record
  [artifact]
  {:artifact :gravity/b14-final-bounded-hosted-c17-conformance
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :semantic-id (:semantic-id artifact)
   :b14-record (:b14-record artifact)})

(defn- p15-s23-b2-c17-gate-b-manifest-sidecar-record
  [artifact provenance-hash conformance-hash]
  {:artifact :gravity/b13-final-bounded-hosted-c17-manifest
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :semantic-id (:semantic-id artifact)
   :core-artifacts
   (select-keys (get-in artifact [:b13-record :artifact-files])
                [:source :header :object :executable])
   :sidecars {:provenance provenance-hash
              :conformance conformance-hash}
   :graph
   [{:from "program.h" :to "program.c" :edge :include}
    {:from ["program.c" "program.h"]
     :to "program.o" :edge :compile}
    {:from "program.o" :to "program" :edge :link}
    {:from "provenance.edn" :to "manifest.edn" :edge :hash-bound}
    {:from "conformance.edn" :to "manifest.edn" :edge :hash-bound}]})

(defn- p15-s23-b2-c17-gate-b-publication-file-records
  [file-specs]
  (into
   (sorted-map)
   (map
    (fn [[logical-path {:keys [bytes mode]}]]
      [logical-path
       {:byte-count (alength ^bytes bytes)
        :content-hash
        (p15-s23-b2-c17-gate-b-sha256-bytes bytes)
        :mode mode}]))
   file-specs))

(defn- p15-s23-b2-c17-gate-b-publication-material
  [artifact payload source-path]
  (let [core-specifications
        [[:source "program.c" 0644]
         [:header "program.h" 0644]
         [:object "program.o" 0644]
         [:executable "program" 0755]]
        core-file-specs
        (into
         (sorted-map)
         (map
          (fn [[kind logical-path mode]]
            (let [bytes (get payload kind)
                  expected
                  (select-keys
                   (get-in artifact [:b13-record :artifact-files kind])
                   [:logical-path :byte-count :content-hash])
                  observed
                  {:logical-path logical-path
                   :byte-count (when (bytes? bytes)
                                 (alength ^bytes bytes))
                   :content-hash
                   (when (bytes? bytes)
                     (p15-s23-b2-c17-gate-b-sha256-bytes bytes))}]
              (when-not
               (and (bytes? bytes)
                    (<= 1 (alength ^bytes bytes) (* 8 1024 1024))
                    (= expected observed))
                (p15-s23-c-backend-fail!
                 "B13-HASH" source-path artifact
                 {:missing-fact :buffered-c17-artifact-before-publication
                  :logical-path logical-path
                  :maximum-byte-count (* 8 1024 1024)
                  :observed-byte-count
                  (when (bytes? bytes) (alength ^bytes bytes))}))
              [logical-path {:bytes bytes :mode mode}]))
          core-specifications))
        provenance
        (p15-s23-b2-c17-gate-b-canonical-sidecar
         "provenance.edn"
         (p15-s23-b2-c17-gate-b-provenance-sidecar-record artifact))
        conformance
        (p15-s23-b2-c17-gate-b-canonical-sidecar
         "conformance.edn"
         (p15-s23-b2-c17-gate-b-conformance-sidecar-record artifact))
        manifest
        (p15-s23-b2-c17-gate-b-canonical-sidecar
         "manifest.edn"
         (p15-s23-b2-c17-gate-b-manifest-sidecar-record
          artifact (:hash-record provenance) (:hash-record conformance)))
        sidecars
        {:manifest manifest :provenance provenance
         :conformance conformance}
        file-specs
        (into
         core-file-specs
         (map
          (fn [[_ {:keys [bytes hash-record]}]]
            [(:logical-path hash-record) {:bytes bytes :mode 0644}]))
         sidecars)]
    {:file-specs file-specs
     :file-records
     (p15-s23-b2-c17-gate-b-publication-file-records file-specs)
     :sidecars sidecars}))

(defn- p15-s23-b2-c17-gate-b-publication-material-equivalent?
  [left right]
  (and
   (= (:file-records left) (:file-records right))
   (= (into (sorted-map)
            (map (fn [[kind sidecar]]
                   [kind (select-keys sidecar [:record :hash-record])]))
            (:sidecars left))
      (into (sorted-map)
            (map (fn [[kind sidecar]]
                   [kind (select-keys sidecar [:record :hash-record])]))
            (:sidecars right)))
   (every?
    (fn [logical-path]
      (java.util.Arrays/equals
       ^bytes (get-in left [:file-specs logical-path :bytes])
       ^bytes (get-in right [:file-specs logical-path :bytes])))
    (keys (:file-specs left)))))

(defn- p15-s23-b2-c17-gate-b-canonical-provider-receipt
  [raw-receipt material source-path]
  (let [raw-receipt-keys
        #{:status :actual-output-directory :file-records
          :publisher-evidence :mode-policy}
        envelope-valid?
        (and (map? raw-receipt)
             (= raw-receipt-keys (set (keys raw-receipt))))
        sidecars (:sidecars material)
        receipt
        {:status (:status raw-receipt)
         :actual-output-directory
         (:actual-output-directory raw-receipt)
         :sidecar-hashes
         {:manifest (get-in sidecars [:manifest :hash-record])
          :provenance (get-in sidecars [:provenance :hash-record])
          :conformance (get-in sidecars [:conformance :hash-record])}
         :publisher-evidence (:publisher-evidence raw-receipt)
         :mode-policy (:mode-policy raw-receipt)}]
    (when-not envelope-valid?
      (p15-s23-c-backend-fail!
       "B13-SCHEMA"
       source-path {}
       {:missing-fact :canonical-darwin-publication-receipt-envelope}))
    (when-not (= (:file-records material) (:file-records raw-receipt))
      (p15-s23-c-backend-fail!
       "B13-HASH" source-path {}
       {:missing-fact :content-bound-darwin-publication-file-records}))
    (when-not
     (p15-s23-b2-c17-gate-b-canonical-published-receipt?
      receipt (:actual-output-directory receipt))
      (p15-s23-c-backend-fail!
       "B13-SCHEMA" source-path {}
       {:missing-fact :canonical-darwin-publication-receipt-values}))
    receipt))

(defn- p15-s23-b2-c17-gate-b-raw-provider-receipt
  [receipt material source-path]
  (let [sidecars (:sidecars material)
        expected-sidecar-hashes
        {:manifest (get-in sidecars [:manifest :hash-record])
         :provenance (get-in sidecars [:provenance :hash-record])
         :conformance (get-in sidecars [:conformance :hash-record])}]
    (when-not
     (and (p15-s23-b2-c17-gate-b-canonical-published-receipt?
           receipt (:actual-output-directory receipt))
          (= expected-sidecar-hashes (:sidecar-hashes receipt)))
      (p15-s23-c-backend-fail!
       "B13-HASH" source-path {}
       {:missing-fact :reconstructable-darwin-publication-receipt}))
    {:status (:status receipt)
     :actual-output-directory (:actual-output-directory receipt)
     :file-records (:file-records material)
     :publisher-evidence (:publisher-evidence receipt)
     :mode-policy (:mode-policy receipt)})))
