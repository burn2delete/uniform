

(defn p15-s23-source-syntax-c3-artifact
  [source-path c2-artifact]
  (let [_ (c3-validate-c2-reader-artifact! source-path c2-artifact)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)
        source-unit (:source-unit-record c2-artifact)
        project-context
        {:project-root-id (:project-root source-unit)
         :project-root-path (get-in source-unit [:project-root-record :path])
         :project-relative-path (:project-relative-path source-unit)}
        fresh-c2-artifact
        (p15-s23-source-syntax-c2-artifact source-path source-text
                                           project-context)
        _ (when-not (= fresh-c2-artifact c2-artifact)
            (c3-syntax-fail!
             "C3-FACT-STALE" source-path
             {:stage :syntax-object-model :producer :p15-compatibility}
             {:missing-fields
              [:fresh-authenticated-sh03-c2-artifact-equality]}))
        authoritative
        (compiler-c3-syntax-source-artifact source-path source-text
                                            project-context c2-artifact
                                            sh03-reader-internal-product-authority)
        _ (when-not (= (:artifact-id c2-artifact)
                       (get-in authoritative
                               [:c2-reader-artifact :artifact-id]))
            (c3-syntax-fail!
             "C3-FACT-STALE" source-path
             {:stage :syntax-object-model :producer :p15-compatibility}
             {:missing-fields [:exact-precomputed-c2-artifact-binding]}))
        verifier (:syntax-verification-report authoritative)
        artifact
        (assoc authoritative
               :task "P15-S23"
               :p15-compatibility-boundary
               {:status :routed-through-gravity-sh04
                :supplied-c2-artifact-id (:artifact-id c2-artifact)
                :authoritative-c2-artifact-id
                (get-in authoritative [:c2-reader-artifact :artifact-id])
                :exact-precomputed-c2-consumed? true
                :legacy-c3-constructor-invoked? false
                :authoritative-result? true}
               :p15-s23-source-syntax-results
               {:syntax-object-count
                (count (:syntax-object-stream authoritative))
                :serialization-status :complete
                :verifier-status (:status verifier)
                :status (if (= :passed (:status verifier))
                          :complete :failed)})]
    (assoc artifact :artifact-id (c3-artifact-id artifact))))

(defn p15-s23-source-syntax-serialization-roundtrip-record
  [c2-artifact c3-artifact]
  (let [source-unit (:source-unit-record c2-artifact)
        syntax-summary
        (p15-s23-source-syntax-summary (:syntax-object-stream c3-artifact))
        payload {:artifact
                 :gravity/p15-s23-source-syntax-serialization-payload
                 :source-unit source-unit
                 :reader-source-map (:reader-source-map c2-artifact)
                 :syntax-summary syntax-summary
                 :syntax-serialization-fixture
                 (:syntax-serialization-fixture c3-artifact)}
        identity-payload
        (-> payload
            (update :source-unit
                    #(-> %
                         (dissoc :path)
                         (update :project-root-record dissoc :path)))
            (update :reader-source-map
                    (fn [records]
                      (mapv #(update % :span c2-path-neutral-span) records)))
            (update :syntax-summary
                    #(mapv c3-path-neutral-syntax-object %)))
        canonical (pr-str payload)
        roundtrip (read-string canonical)
        syntax-ids (mapv :syntax/id syntax-summary)]
    {:artifact :gravity/p15-s23-source-syntax-serialization-roundtrip
     :format :edn
     :serialization-id (reader-canonical-hash identity-payload)
     :payload payload
     :canonical canonical
     :roundtrip? (= payload roundtrip)
     :source-unit-roundtrip?
     (= (:source-unit payload) (:source-unit roundtrip))
     :syntax-object-roundtrip?
     (= (:syntax-summary payload) (:syntax-summary roundtrip))
     :stable-source-id?
     (and (re-find #"^sha256:" (str (:source-id source-unit)))
          (p15-s23-source-syntax-normalized-source-id? source-unit))
     :stable-syntax-ids?
     (and (every? #(re-find #"^sha256:" (str %)) syntax-ids)
          (= (count syntax-ids) (count (set syntax-ids))))
     :origin-links-preserved?
     (every? #(seq (:origin %)) syntax-summary)
     :c3-serialization-roundtrip?
     (true? (get-in c3-artifact [:syntax-serialization-fixture :roundtrip?]))
     :syntax-verifier-passed?
     (= :passed (get-in c3-artifact [:syntax-verification-report :status]))
     :status :complete}))

(def p15-s23-source-syntax-rejected-candidates
  [{:fixture :internal-p15-s23-source-syntax-missing-proof
    :candidate {}
    :expected-diagnostic "P15S23S001"}
   {:fixture :internal-p15-s23-source-syntax-source-unit-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/source-unit-and-syntax-serialization-proof
                 :preserves p15-s23-source-syntax-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :source-unit-record {:artifact :gravity/source-unit
                                     :path p15-s23-compiler-source-path
                                     :source-id "unstable"
                                     :bytes-hash "different"}
                :serialization-roundtrip-record
                {:source-unit-roundtrip? false
                 :stable-source-id? false}}
    :expected-diagnostic "P15S23S002"}
   {:fixture :internal-p15-s23-source-syntax-identity-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/source-unit-and-syntax-serialization-proof
                 :preserves #{:source-spans}
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :source-unit-record {:artifact :gravity/source-unit
                                     :path p15-s23-compiler-source-path
                                     :source-id "sha256:stable"
                                     :bytes-hash "sha256:stable"}
                :syntax-object-stream
                [{:artifact :gravity/syntax-object
                  :syntax/id "unstable"
                  :source {:source-id "sha256:stable"}
                  :span {:primary {}}}]
                :serialization-roundtrip-record
                {:source-unit-roundtrip? true
                 :stable-source-id? true
                 :syntax-object-roundtrip? false
                 :stable-syntax-ids? false
                 :origin-links-preserved? false}}
    :expected-diagnostic "P15S23S003"}
   {:fixture :internal-p15-s23-source-syntax-serialization-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/source-unit-and-syntax-serialization-proof
                 :preserves p15-s23-source-syntax-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :source-unit-record {:artifact :gravity/source-unit
                                     :path p15-s23-compiler-source-path
                                     :source-id "sha256:stable"
                                     :bytes-hash "sha256:stable"}
                :syntax-object-stream
                [{:artifact :gravity/syntax-object
                  :syntax/id "sha256:syntax"
                  :source {:source-id "sha256:stable"}
                  :span {:primary {:source p15-s23-compiler-source-path
                                   :byte-start 0
                                   :byte-end 1}}
                  :origin [{:kind :source}]}]
                :serialization-roundtrip-record
                {:source-unit-roundtrip? true
                 :stable-source-id? true
                 :syntax-object-roundtrip? true
                 :stable-syntax-ids? true
                 :origin-links-preserved? true
                 :roundtrip? false
                 :c3-serialization-roundtrip? false}
                :syntax-verification-report {:status :failed}}
    :expected-diagnostic "P15S23S004"}
   {:fixture :internal-p15-s23-source-syntax-overclaim
    :candidate {:proof-contract
                {:artifact
                 :gravity/source-unit-and-syntax-serialization-proof
                 :preserves p15-s23-source-syntax-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? true
                  :clojure-seed-retired? true}}
                :source-unit-record {:artifact :gravity/source-unit
                                     :path p15-s23-compiler-source-path
                                     :source-id "sha256:stable"
                                     :bytes-hash "sha256:stable"}
                :syntax-object-stream
                [{:artifact :gravity/syntax-object
                  :syntax/id "sha256:syntax"
                  :source {:source-id "sha256:stable"}
                  :span {:primary {:source p15-s23-compiler-source-path
                                   :byte-start 0
                                   :byte-end 1}}
                  :origin [{:kind :source}]}]
                :serialization-roundtrip-record
                {:source-unit-roundtrip? true
                 :stable-source-id? true
                 :syntax-object-roundtrip? true
                 :stable-syntax-ids? true
                 :origin-links-preserved? true
                 :roundtrip? true
                 :c3-serialization-roundtrip? true}
                :syntax-verification-report {:status :passed}}
    :expected-diagnostic "P15S23S005"}])

(defn p15-s23-source-syntax-rejected-records
  [source-path]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-source-syntax-proof-diagnostics source-path candidate)})
        p15-s23-source-syntax-rejected-candidates))

(defn p15-s23-source-syntax-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-source-syntax-serialization-diagnostic-stream
   :stage :p15-s23-source-syntax-serialization-proof
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-source-syntax-serialization-proof
            :message
            (get p15-s23-source-syntax-serialization-diagnostic-messages
                 id)})
         p15-s23-source-syntax-serialization-diagnostic-ids)
   :status :complete})