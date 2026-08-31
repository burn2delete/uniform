

(def p15-s23-stage2-reader-compatibility-diagnostic-map
  {"STAGE1READER001"
   {:c2-id "C2-DELIMITER"
    :remapped-from "L1-DELIMITER"
    :reader-state-stages #{:recursive-form-building :read-source}
    :fact-key-sets #{#{:actual-delimiter}
                     #{:expected-delimiter :actual-delimiter}
                     #{:observed-close-codepoint :token-id :form-id}
                     #{:observed-close-codepoint :expected-close-codepoint
                       :token-id :form-id}}}
   "STAGE1READER002"
   {:c2-id "C2-DELIMITER"
    :remapped-from "L1-DELIMITER"
    :reader-state-stages #{:recursive-form-building :read-source}
    :fact-key-sets #{#{:expected-delimiter :open-token}
                     #{:open-form-id :token-id :form-id}}}
   "STAGE1READER003"
   {:c2-id "C2-STRING"
    :remapped-from "L1-STRING"
    :reader-state-stages #{:lexical-tokenization :read-source}
    :fact-key-sets #{#{}}}
   "STAGE1READER004"
   {:c2-id "C2-EXTENSION"
    :remapped-from "L1-READER-EXTENSION"
    :reader-state-stages #{:lexical-tokenization :read-source}
    :fact-key-sets #{#{}
                     #{:tag-codepoints :token-id :form-id}}}
   "STAGE1READER005"
   {:c2-id "C2-MAP"
    :remapped-from "L1-MAP-ARITY"
    :reader-state-stages #{:recursive-form-building :read-source}
    :fact-key-sets #{#{:entry-count}
                     #{:observed-child-count :token-id :form-id}}}
   "STAGE1READER007"
   {:c2-id "C2-NUMERIC"
    :remapped-from "L1-NUMERIC"
    :reader-state-stages #{:lexical-tokenization :read-source}
    :fact-key-sets #{#{:literal-kind :raw-spelling}}}})

(defn p15-s23-stage2-reader-replayed-diagnostic
  [source-path source-text]
  (try
    (compiler-c2-reader-source-artifact source-path source-text)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))
    (catch Exception _
      nil)))

(defn p15-s23-stage2-reader-safe-location
  [location]
  (when (map? location)
    (select-keys location [:line :column :column-unit :char :byte])))

(defn p15-s23-stage2-reader-safe-span
  [span]
  (when (map? span)
    (cond-> (select-keys span [:source :byte-start :byte-end :file])
      (:start span)
      (assoc :start (p15-s23-stage2-reader-safe-location (:start span)))
      (:end span)
      (assoc :end (p15-s23-stage2-reader-safe-location (:end span))))))

(def p15-s23-stage2-legacy-reader-state-keys
  #{:artifact :stage :byte-offset :line :column :token-id :form-id})

(def p15-s23-stage2-gravity-reader-state-keys
  #{:artifact :owner :stage :reason :byte-offset :char-position
    :line :column :column-unit :token-id :form-id :result-committed?})

(defn p15-s23-stage2-reader-state-authentic?
  [reader-state span data allowed-stages]
  (let [keys (set (keys reader-state))
        common?
        (and (= :gravity/reader-state (:artifact reader-state))
             (contains? allowed-stages (:stage reader-state))
             (= (:byte-start span) (:byte-offset reader-state))
             (= (get-in span [:start :line]) (:line reader-state))
             (= (get-in span [:start :column]) (:column reader-state))
             (= (:token-id data) (:token-id reader-state))
             (= (:form-id data) (:form-id reader-state)))]
    (and
     common?
     (or
      (= p15-s23-stage2-legacy-reader-state-keys keys)
      (and
       (= p15-s23-stage2-gravity-reader-state-keys keys)
       (= :gravity-source (:owner reader-state))
       (keyword? (:reason reader-state))
       (= :unicode-scalar (:column-unit reader-state))
       (integer? (:char-position reader-state))
       (= (get-in span [:start :char]) (:char-position reader-state))
       (false? (:result-committed? reader-state)))))))

(defn p15-s23-stage2-reader-diagnostic-authentic?
  [source-path source-text data replayed-data]
  (try
    (let [engine-id (when (map? data)
                      (:reader-engine-diagnostic data))
          contract
          (get p15-s23-stage2-reader-compatibility-diagnostic-map engine-id)
          c2-id (:c2-id contract)
          span (:source-span data)
          facts (:facts data)
          reader-state (:reader-state data)]
      (and (map? data)
           (map? replayed-data)
           contract
           (map? span)
           (map? (:start span))
           (map? (:end span))
           (map? facts)
           (map? reader-state)
           ;; A second direct C2 replay binds every copied value to the
           ;; supplied source.  The structural checks below remain as an
           ;; independent defense against a weakened replay implementation.
           (= replayed-data data)
           (let [source-id
                 (:source-id
                  (c2-source-unit-record source-path source-text
                                         standard-reader-options))
                 expected-diagnostic-id
                 (reader-canonical-hash
                  {:rule (keyword c2-id)
                   :primary-artifact source-id
                   :stage :read-source
                   :span (dissoc span :source)
                   :token-id (:token-id data)
                   :form-id (:form-id data)
                   :facts facts})]
             (and
              (= :gravity/diagnostic (:artifact data))
              (= c2-id (:id data))
              (= (keyword c2-id) (:rule data))
              (= (c2-reader-message c2-id) (:message data))
              (= :error (:severity data))
              (= :c2-reader (:diagnostic-family data))
              (= :read-source (:stage data))
              (= "C2" (:document-id data))
              (= c2-reader-governing-document (:expected-document data))
              (= source-id (:source-id data))
              (= source-path (:source span))
              (= source-id (:file span))
              (= {:span span :artifact source-id} (:primary data))
              (= [{:kind :source :source-id source-id :path source-path}]
                 (:origin-chain data))
              (= [source-id] (:involved-artifacts data))
              (= [] (:related data))
              (= :stage0 (:bootstrap-stage data))
              (= (:remapped-from contract) (:remapped-from data))
              (= expected-diagnostic-id (:diagnostic-id data))
              (contains? (:fact-key-sets contract) (set (keys facts)))
              (= standard-reader-options (:reader-options data))
              (= #{:source :start :end :byte-start :byte-end :file}
                 (set (keys span)))
              (= #{:line :column :column-unit :char :byte}
                 (set (keys (:start span))))
              (= #{:line :column :column-unit :char :byte}
                 (set (keys (:end span))))
              (= :unicode-scalar (get-in span [:start :column-unit]))
              (= :unicode-scalar (get-in span [:end :column-unit]))
              (= (:byte-start span) (get-in span [:start :byte]))
              (= (:byte-end span) (get-in span [:end :byte]))
              (p15-s23-stage2-reader-state-authentic?
               reader-state span data (:reader-state-stages contract))))))
    (catch Exception _
      false)))

(defn p15-s23-stage2-canonical-c2-diagnostic-authentic?
  "Authenticate every canonical C2 reader rejection by exact bounded replay.
  Internal engine labels remain provenance only; the replayed canonical C2
  record is the authority after the Gravity reader cutover."
  [source-path source-text data replayed-data]
  (try
    (let [id (:id data)
          span (:source-span data)
          facts (:facts data)
          reader-state (:reader-state data)
          source-id
          (:source-id
           (c2-source-unit-record source-path source-text
                                  standard-reader-options))
          expected-diagnostic-id
          (when (and (string? id) (map? span) (map? facts))
            (reader-canonical-hash
             {:rule (keyword id)
              :primary-artifact source-id
              :stage :read-source
              :span (dissoc span :source)
              :token-id (:token-id data)
              :form-id (:form-id data)
              :facts facts}))
          compatibility-engine?
          (contains? p15-s23-stage2-reader-compatibility-diagnostic-map
                     (:reader-engine-diagnostic data))]
      (and
       (map? data)
       (map? replayed-data)
       (= replayed-data data)
       (contains? (set c2-reader-diagnostic-ids) id)
       (= :gravity/diagnostic (:artifact data))
       (= (keyword id) (:rule data))
       (= (c2-reader-message id) (:message data))
       (= :error (:severity data))
       (= :c2-reader (:diagnostic-family data))
       (= :read-source (:stage data))
       (= "C2" (:document-id data))
       (= c2-reader-governing-document (:expected-document data))
       (= source-id (:source-id data))
       (map? span)
       (= source-path (:source span))
       (= source-id (:file span))
       (= {:span span :artifact source-id} (:primary data))
       (= [{:kind :source :source-id source-id :path source-path}]
          (:origin-chain data))
       (= [source-id] (:involved-artifacts data))
       (= expected-diagnostic-id (:diagnostic-id data))
       (= standard-reader-options (:reader-options data))
       (= :stage0 (:bootstrap-stage data))
       (map? reader-state)
       (p15-s23-stage2-reader-state-authentic?
        reader-state span data
        (or (get-in p15-s23-stage2-reader-compatibility-diagnostic-map
                    [(:reader-engine-diagnostic data) :reader-state-stages])
            #{:lexical-tokenization :recursive-form-building
              :source-unit-policy :source-decoding}))
       (or (not compatibility-engine?)
           (p15-s23-stage2-reader-diagnostic-authentic?
            source-path source-text data replayed-data))))
    (catch StackOverflowError _
      false)
    (catch Exception _
      false)))