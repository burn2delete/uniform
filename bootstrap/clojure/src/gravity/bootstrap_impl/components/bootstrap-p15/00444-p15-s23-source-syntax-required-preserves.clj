

(def p15-s23-source-syntax-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity :origin-chain
    :stable-serialization-hash})

(def p15-s23-source-syntax-serialization-diagnostic-messages
  {"P15S23S001" "P15-S23 source/syntax serialization proof is missing"
   "P15S23S002" "P15-S23 source unit identity does not round-trip"
   "P15S23S003" "P15-S23 syntax identities, spans, or origins do not survive serialization"
   "P15S23S004" "P15-S23 syntax serialization fixture does not round-trip"
   "P15S23S005" "P15-S23 source/syntax serialization proof makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-source-syntax-serialization-diagnostic-ids
  ["P15S23S001" "P15S23S002" "P15S23S003" "P15S23S004"
   "P15S23S005"])

(defn p15-s23-source-syntax-serialization-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-source-syntax-serialization-diagnostic-messages
              id
              "P15-S23 source/syntax serialization proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-source-syntax-serialization-proof
                 :diagnostic-family
                 :p15-s23-source-syntax-serialization-proof
                 :value value
                 :remediation "Keep the P15-S23 source/syntax proof in Gravity-owned source, preserve source-unit identity, syntax ids, spans, origins, and round-tripping serialization, and keep self-hosting claims false until the complete evidence bundle exists."}
                data)))

(defn p15-s23-source-syntax-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-source-syntax-serialization-proof
   :source-span {:source source-path}
   :message
   (get p15-s23-source-syntax-serialization-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_gravity_source_syntax_serialization_proof})

(defn p15-s23-source-syntax-normalized-source-id?
  [source-unit]
  (and (map? (:identity-inputs source-unit))
       (= (:source-id source-unit)
          (reader-canonical-hash (:identity-inputs source-unit)))))

(defn p15-s23-source-syntax-source-unit-stable?
  [source-path source-unit serialization]
  (and (= :gravity/source-unit (:artifact source-unit))
       (= source-path (:path source-unit))
       (re-find #"^sha256:" (str (:source-id source-unit)))
       (p15-s23-source-syntax-normalized-source-id? source-unit)
       (true? (:source-unit-roundtrip? serialization))
       (true? (:stable-source-id? serialization))))

(defn p15-s23-source-syntax-span-resolves?
  [span]
  (let [valid-span?
        (fn [item]
          (or (and (map? item)
                   (:source item)
                   (re-find #"^sha256:" (str (:file item)))
                   (nat-int? (:byte-start item))
                   (nat-int? (:byte-end item))
                   (<= (:byte-start item) (:byte-end item)))
              (and (map? item)
                   (= :generated (:kind item))
                   (re-find #"^sha256:" (str (:producer-id item)))
                   (nat-int? (:ordinal item)))))]
    (and (map? span)
         (valid-span? (:primary span))
         (or (not (contains? span :all))
             (and (vector? (:all span))
                  (every? valid-span? (:all span)))))))

(defn p15-s23-source-syntax-sh04-identity-record
  [source-path source-unit syntax-stream]
  (let [semantic-source-id
        (sh04-syntax-semantic-source-id source-path source-unit)
        syntax-source-ids
        (vec (distinct (map #(get-in % [:source :source-id]) syntax-stream)))
        span-records
        (mapcat (fn [syntax]
                  (let [span (:span syntax)]
                    (concat [(:primary span)] (:all span))))
                syntax-stream)
        source-span-records
        (filter #(and (map? %)
                      (not= :generated (:kind %)))
                span-records)
        span-file-ids (vec (distinct (map :file source-span-records)))
        malformed-span-record?
        (some #(not (or (and (map? %)
                             (:source %)
                             (re-find #"^sha256:" (str (:file %)))
                             (contains? % :byte-start)
                             (contains? % :byte-end))
                        (and (map? %)
                             (= :generated (:kind %))
                             (re-find #"^sha256:" (str (:producer-id %)))
                             (nat-int? (:ordinal %)))))
              span-records)
        origin-producer-source-ids
        (->> syntax-stream
             (mapcat #(map (fn [origin]
                             (get-in origin [:producer :source-id]))
                           (:origin %)))
             distinct
             vec)
        origins (mapcat :origin syntax-stream)]
    {:identity-domain :gravity/sh04-co-canonical-source-v1
     :expected-sh04-semantic-source-id semantic-source-id
     :observed-syntax-source-ids syntax-source-ids
     :observed-span-file-ids span-file-ids
     :observed-origin-producer-source-ids origin-producer-source-ids
     :sh04-syntax-source-identities-preserved?
     (and (seq syntax-source-ids)
          (every? #(= semantic-source-id %) syntax-source-ids))
     :sh04-span-file-identities-preserved?
     (and (not malformed-span-record?)
          (seq source-span-records)
          (every? (comp some? :file) source-span-records)
          (every? #(= source-path (:source %)) source-span-records)
          (every? #(= semantic-source-id %) span-file-ids))
     :sh04-origin-producer-identities-preserved?
     (and (seq origin-producer-source-ids)
          (every? #(= semantic-source-id %)
                  origin-producer-source-ids))
     :sh04-origin-spans-preserved?
     (and (seq origins)
          (every?
           (fn [origin]
             (let [span (:span origin)
                   producer (:producer origin)]
               (and (map? span)
                    (map? producer)
                    (or (and (= :generated (:kind span))
                             (re-find #"^sha256:" (str (:producer-id span)))
                             (nat-int? (:ordinal span))
                             (= (:producer-id span) (:identity producer)))
                        (and (not= :generated (:kind span))
                             (= (:source span) source-path)
                             (re-find #"^sha256:" (str (:file span)))
                             (nat-int? (:byte-start span))
                             (nat-int? (:byte-end span))
                             (<= (:byte-start span) (:byte-end span))
                             (= (:file span) semantic-source-id))))))
           origins))}))

(defn p15-s23-source-syntax-c2-identity-record
  [source-unit expected-semantic-source-id c3-artifact]
  (let [authenticated-c2-artifact (:c2-reader-artifact c3-artifact)
        actual-provenance
        (get-in c3-artifact
                [:gravity-syntax-boundary
                 :reader-authentication-provenance])
        expected-binding
        (when (map? authenticated-c2-artifact)
          (sh04-syntax-current-sh03-product-binding
           authenticated-c2-artifact))
        actual-binding (:actual-sh03-semantic-product-binding
                        actual-provenance)
        c2-source-unit-id (:source-id source-unit)
        expected-adapted-source-unit-id c2-source-unit-id
        observed-adapted-source-unit-id
        (:adapted-source-unit-id actual-binding)]
    {:c2-identity-domain :gravity/sh03-adapted-source-unit-id-v2
     :c2-source-unit-id c2-source-unit-id
     :expected-adapted-source-unit-id expected-adapted-source-unit-id
     :observed-adapted-source-unit-id observed-adapted-source-unit-id
     :observed-boundary-semantic-source-id
     (:semantic-source-id actual-provenance)
     :c2-source-unit-identity-preserved?
     (and (map? authenticated-c2-artifact)
          (= source-unit
             (:source-unit-record authenticated-c2-artifact))
          (= expected-binding actual-binding)
          (= expected-adapted-source-unit-id
             observed-adapted-source-unit-id)
          (= expected-semantic-source-id
             (:semantic-source-id actual-provenance)))}))

(defn p15-s23-source-syntax-c2-context-record
  [source-path source-unit c3-artifact]
  (let [authenticated-source-unit
        (get-in c3-artifact [:c2-reader-artifact :source-unit-record])
        source-unit-fields
        [:path :extension :source-kind :project-relative-path
         :project-root :project-root-record]
        expected-context (reader-project-context-for-source source-path)
        expected-source-unit-fields
        {:path source-path
         :extension (gravity-source-extension source-path)
         :source-kind (gravity-source-kind source-path)
         :project-relative-path (:project-relative-path expected-context)
         :project-root (:project-root-id expected-context)
         :project-root-record (reader-project-root-record expected-context)}
        source-context-preserved?
        (and (= (select-keys source-unit source-unit-fields)
                expected-source-unit-fields)
             (map? authenticated-source-unit)
             (= (select-keys source-unit source-unit-fields)
                (select-keys authenticated-source-unit source-unit-fields)))]
    {:c2-source-context-fields source-unit-fields
     :observed-authenticated-source-path (:path authenticated-source-unit)
     :observed-authenticated-source-extension
     (:extension authenticated-source-unit)
     :observed-authenticated-source-kind (:source-kind authenticated-source-unit)
     :observed-authenticated-project-root
     (:project-root authenticated-source-unit)
     :observed-authenticated-project-root-record-path
     (get-in authenticated-source-unit [:project-root-record :path])
     :expected-source-unit-context expected-source-unit-fields
     :c2-source-unit-context-preserved? source-context-preserved?}))