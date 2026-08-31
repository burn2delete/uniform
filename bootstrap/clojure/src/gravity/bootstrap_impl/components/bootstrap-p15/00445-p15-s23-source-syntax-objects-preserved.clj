

(defn p15-s23-source-syntax-objects-preserved?
  [source-unit syntax-stream serialization identity-record]
  (and (map? identity-record)
       (seq syntax-stream)
       (every? #(= :gravity/syntax-object (:artifact %)) syntax-stream)
       (every? #(re-find #"^sha256:" (str (:syntax/id %))) syntax-stream)
       (= (count syntax-stream) (count (set (map :syntax/id syntax-stream))))
       (every? true?
               (map #(true? (get identity-record %))
                    [:sh04-syntax-source-identities-preserved?
                     :sh04-span-file-identities-preserved?
                     :sh04-origin-producer-identities-preserved?
                     :sh04-origin-spans-preserved?
                     :c2-source-unit-identity-preserved?
                     :c2-source-unit-context-preserved?]))
       (every? #(p15-s23-source-syntax-span-resolves? (:span %))
               syntax-stream)
       (every? #(seq (:origin %)) syntax-stream)
       (true? (:syntax-object-roundtrip? serialization))
       (true? (:stable-syntax-ids? serialization))
       (true? (:origin-links-preserved? serialization))))

(defn p15-s23-source-syntax-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        source-unit (:source-unit-record candidate)
        syntax-stream (:syntax-object-stream candidate)
        serialization (:serialization-roundtrip-record candidate)
        c3-artifact (:c3-syntax-artifact candidate)
        identity-record
        (if (map? c3-artifact)
          (let [sh04-identity-record
                (p15-s23-source-syntax-sh04-identity-record
                 source-path source-unit syntax-stream)]
            (merge
             sh04-identity-record
             (p15-s23-source-syntax-c2-identity-record
              source-unit
              (:expected-sh04-semantic-source-id sh04-identity-record)
              c3-artifact)
             (p15-s23-source-syntax-c2-context-record
              source-path source-unit c3-artifact)))
          {:identity-record-present? false
           :identity-domain :gravity/sh04-co-canonical-source-v1
           :c2-identity-domain :gravity/sh03-adapted-source-unit-id-v2
           :sh04-syntax-source-identities-preserved? false
           :sh04-span-file-identities-preserved? false
           :sh04-origin-producer-identities-preserved? false
           :sh04-origin-spans-preserved? false
           :c2-source-unit-identity-preserved? false
           :c2-source-unit-context-preserved? false})
        verifier (:syntax-verification-report candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-source-syntax-required-preserves preserves)]
    (vec
     (concat
      (when-not (= :gravity/source-unit-and-syntax-serialization-proof
                   (:artifact proof-contract))
        [(p15-s23-source-syntax-diagnostic-record
          source-path "P15S23S001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not (p15-s23-source-syntax-source-unit-stable?
                 source-path source-unit serialization)
        [(p15-s23-source-syntax-diagnostic-record
          source-path "P15S23S002" source-unit
          {:source-unit-roundtrip? (:source-unit-roundtrip? serialization)
           :stable-source-id? (:stable-source-id? serialization)})])
      (when-not (and (p15-s23-source-syntax-objects-preserved?
                      source-unit syntax-stream serialization identity-record)
                     (empty? missing-preserves))
        [(p15-s23-source-syntax-diagnostic-record
          source-path "P15S23S003"
          (select-keys identity-record
                       [:expected-sh04-semantic-source-id
                        :observed-syntax-source-ids
                        :observed-span-file-ids
                        :observed-origin-producer-source-ids
                        :c2-source-unit-id
                        :expected-adapted-source-unit-id
                        :observed-adapted-source-unit-id
                        :observed-boundary-semantic-source-id
                        :observed-authenticated-source-path
                        :observed-authenticated-source-extension
                        :observed-authenticated-source-kind
                        :observed-authenticated-project-root
                        :observed-authenticated-project-root-record-path])
          (merge
           {:identity-domain (:identity-domain identity-record)
            :c2-identity-domain (:c2-identity-domain identity-record)
            :sh04-syntax-source-identities-preserved?
            (:sh04-syntax-source-identities-preserved?
             identity-record)
                        :sh04-span-file-identities-preserved?
            (:sh04-span-file-identities-preserved? identity-record)
            :sh04-origin-producer-identities-preserved?
            (:sh04-origin-producer-identities-preserved?
             identity-record)
            :sh04-origin-spans-preserved?
            (:sh04-origin-spans-preserved? identity-record)
            :c2-source-unit-identity-preserved?
            (:c2-source-unit-identity-preserved? identity-record)
            :c2-source-unit-context-preserved?
            (:c2-source-unit-context-preserved? identity-record)
            :syntax-object-artifacts-valid?
            (every? #(= :gravity/syntax-object (:artifact %)) syntax-stream)
            :syntax-ids-valid?
            (every? #(re-find #"^sha256:" (str (:syntax/id %))) syntax-stream)
            :syntax-ids-unique?
            (= (count syntax-stream) (count (set (map :syntax/id syntax-stream))))
            :syntax-spans-resolve?
            (every? #(p15-s23-source-syntax-span-resolves? (:span %))
                    syntax-stream)
            :syntax-origins-present?
            (every? #(seq (:origin %)) syntax-stream)
            :syntax-object-roundtrip?
            (:syntax-object-roundtrip? serialization)
            :stable-syntax-ids?
            (:stable-syntax-ids? serialization)
            :origin-links-preserved?
            (:origin-links-preserved? serialization)
            :missing-preserves (vec (sort missing-preserves))}))])
      (when-not (and (true? (:roundtrip? serialization))
                     (true? (:c3-serialization-roundtrip? serialization))
                     (= :passed (:status verifier)))
        [(p15-s23-source-syntax-diagnostic-record
          source-path "P15S23S004" serialization
          {:roundtrip? (:roundtrip? serialization)
           :c3-serialization-roundtrip?
           (:c3-serialization-roundtrip? serialization)
           :syntax-verifier-status (:status verifier)})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-source-syntax-diagnostic-record
          source-path "P15S23S005" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired? (:clojure-seed-retired? claims)})])))))

(defn p15-s23-source-syntax-summary
  [syntax-stream]
  (mapv #(select-keys % [:artifact :syntax/id :form :span :source :origin
                         :identity :facts :phase :profile :version
                         :immutable?])
        syntax-stream))

(defn p15-s23-source-syntax-c2-capability-proof
  [artifact]
  (let [source-unit (:source-unit-record artifact)
        hashes (:incremental-reader-hashes artifact)
        lexical (:lexical-product-validation artifact)]
    {:source-unit-hash-stable?
     (boolean
      (and (= :gravity/source-unit (:artifact source-unit))
           (p15-s23-source-syntax-normalized-source-id? source-unit)
           (re-find #"^sha256:" (str (:source-id source-unit)))))
     :token-and-form-spans-present?
     (and (every? #(and (:token-id %)
                        (get-in % [:span :byte-start])
                        (get-in % [:span :byte-end]))
                  (:token-stream artifact))
          (every? #(and (:form-id %)
                        (get-in % [:span :byte-start])
                        (get-in % [:span :byte-end]))
                  (:form-tree artifact)))
     :reader-source-map-present?
     (and (seq (:reader-source-map artifact))
          (every? #(and (:syntax-id %) (:span %))
                  (:reader-source-map artifact)))
     :syntax-seeds-preserve-origins?
     (every? #(contains? % :generated-origin)
             (:syntax-seed-stream artifact))
     :incremental-hashes-stable?
     (and (= :stable (:status hashes))
          (every? #(re-find #"^sha256:" (str (get hashes %)))
                  [:source-unit :token-stream :form-tree
                   :syntax-seed-stream :extension-invocation-set
                   :reader-diagnostics]))
     :lexical-token-stream?
     (every? true?
             (map lexical
                  [:ordered-token-ids-unique?
                   :token-raw-slices-exact?
                   :token-provenance-complete?
                   :no-token-contains-top-level-form?]))
     :nested-form-tree?
     (every? true?
             (map lexical
                  [:form-ids-unique?
                   :graph-valid?
                   :root-form-ids-resolve?
                   :form-raw-slices-exact?
                   :form-links-resolve?
                   :parent-spans-enclose-children?
                   :collection-delimiters-resolve?]))
     :representation-status :genuine-lexical-token-and-recursive-form-tree
     :status :partial}))