(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-validated-options!
  [source-path options]
  (let [trusted-map-class?
        (and (map? options)
             (contains? p15-s23-trusted-carrier-map-classes
                        (.getName (class options))))
        option-count (when trusted-map-class? (count options))
        exact-shape?
        (and trusted-map-class?
             (or (zero? option-count)
                 (and (= 1 option-count)
                      (identical? :output-directory
                                  (key (first (seq options)))))))]
    (when-not exact-shape?
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :exact-c17-gate-b-option-envelope}))
    (let [validation
          (p15-s23-trusted-carrier-validation options :default-only 32 4 8)]
    (when-not
     (and (= :passed (:status validation))
          exact-shape?)
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       (merge {:missing-fact :trusted-bounded-c17-gate-b-options}
              (select-keys validation
                           [:reason :observed-nodes :observed-depth
                            :maximum-nodes :maximum-depth :maximum-width]))))
    (let [output-directory (:output-directory options)
          character-count
          (when (string? output-directory) (count output-directory))
          utf8-byte-count
          (when (and (string? output-directory)
                     (<= 1 character-count 4096))
            (alength
             (.getBytes ^String output-directory
                        java.nio.charset.StandardCharsets/UTF_8)))]
      (when (contains? options :output-directory)
        (when-not
         (and (string? output-directory)
              (<= 1 character-count 4096)
              (<= 1 utf8-byte-count 4096)
              (not (str/blank? output-directory))
              (not (str/includes? output-directory "\u0000")))
          (p15-s23-c-backend-fail!
           "B2-MANIFEST" source-path {}
           {:missing-fact :bounded-c17-output-directory-option
            :maximum-byte-count 4096
            :observed-byte-count utf8-byte-count})))
      (let [path
            (when output-directory
              (try
                (java.nio.file.Paths/get
                 output-directory (make-array String 0))
                (catch Exception _ nil)))]
        (when (and output-directory
                   (not (and path (.isAbsolute path)
                             (= output-directory
                                (.toString (.normalize path))))))
          (p15-s23-c-backend-fail!
           "B2-MANIFEST" source-path {}
           {:missing-fact :absolute-normalized-c17-output-directory}))
        (if (contains? options :output-directory)
          {:output-directory output-directory}
          {}))))))

(declare ^:private p15-s23-b2-c17-gate-b-metadata-closure?)

(defn- p15-s23-b2-c17-gate-b-pre-effect-gate!
  [gate-a checked-core context options]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        validated-options
        (p15-s23-b2-c17-gate-b-validated-options! source-path options)
        before @p15-s23-b2-c17-gate-b-tool-state]
    (p15-s23-b2-c17-verification-preflight! source-path gate-a)
    (let [report
          (p15-s23-stage2-b2-c17-verification-report
           gate-a checked-core context)]
      (when-not
       (and (= :passed (:status report))
            (= (:artifact-id gate-a) (:artifact-id report))
            (= (:semantic-id gate-a) (:semantic-id report))
            (= before @p15-s23-b2-c17-gate-b-tool-state))
        (p15-s23-c-backend-fail!
         "B2-MANIFEST" source-path gate-a
         {:missing-fact :fresh-contextual-gate-a-before-c17-effects}))
      (when-not
       (p15-s23-b2-c17-gate-b-metadata-closure? gate-a report)
        (p15-s23-c-backend-fail!
         "B14-METADATA" source-path gate-a
         {:missing-fact :exact-c17-source-proof-metadata-closure}))
      (p15-s23-b2-c17-gate-b-host-runtime-preflight!
       p15-s23-b2-c17-gate-b-authority-token source-path)
      {:source-path source-path
       :options validated-options
       :gate-a-contextual-report report})))

(defn- p15-s23-b2-c17-gate-b-record-id
  [kind record]
  (p15-s23-c11-mir-digest {:kind kind :schema-version 1 :record record}))

(defn- p15-s23-b2-c17-gate-b-neutral-content-id
  [value]
  (p15-s23-c11-mir-digest
   (p15-s23-c13-c14-b1-path-neutral-value value)))

(defn- p15-s23-b2-c17-gate-b-metadata-closure?
  [gate-a contextual-report]
  (let [operation-count (count (:operation-records gate-a))
        semantic-closure (:semantic-pure-closure contextual-report)
        semantic-closure-base (dissoc semantic-closure :evidence-id)
        contextual-base (dissoc contextual-report :report-id)
        expected-semantic-closure-keys
        #{:artifact :schema-version :status :c11-artifact-id
          :c11-mir-id :mir-module-id :operation-count
          :effect-fact-row-count :capability-fact-row-count
          :runtime-check-count :capability-proof-count
          :semantic-effects :semantic-capabilities
          :semantic-effect-count :semantic-capability-count
          :main-latent-effects :main-capabilities
          :all-operation-effects-empty?
          :all-operation-capabilities-empty? :evidence-id}
        expected-contextual-keys
        #{:artifact :schema-version :status :artifact-id :semantic-id
          :fresh-c11 :fresh-c13 :fresh-c14 :fresh-b1
          :gravity-b2-source-replay :independent-c-reconstruction
          :semantic-pure-closure :external-tool-execution
          :public? :release? :self-hosted? :report-id}
        sha256-id?
        (fn [value]
          (and (string? value)
               (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))]
    (and (= expected-contextual-keys (set (keys contextual-report)))
         (= expected-semantic-closure-keys
            (set (keys semantic-closure)))
         (= :gravity/b2-c17-contextual-verification-report
            (:artifact contextual-report))
         (= :gravity/b2-c17-semantic-pure-closure-evidence
            (:artifact semantic-closure))
         (= 1 (:schema-version contextual-report)
            (:schema-version semantic-closure))
         (= :passed (:status contextual-report)
            (:status semantic-closure))
         (= :passed
            (:fresh-c11 contextual-report)
            (:fresh-c13 contextual-report)
            (:fresh-c14 contextual-report)
            (:fresh-b1 contextual-report)
            (:gravity-b2-source-replay contextual-report)
            (:independent-c-reconstruction contextual-report))
         (= :not-performed-in-gate-a
            (:external-tool-execution contextual-report))
         (false? (:public? contextual-report))
         (false? (:release? contextual-report))
         (false? (:self-hosted? contextual-report))
         (= (:artifact-id gate-a) (:artifact-id contextual-report))
         (= (:semantic-id gate-a) (:semantic-id contextual-report))
         (= (:c11-artifact-id semantic-closure)
            (get-in gate-a [:input-bindings :c11-artifact-id]))
         (= (get-in gate-a [:input-bindings :mir-module-id])
            (get-in gate-a [:verified-input-closure :mir-module-id])
            (:mir-module-id semantic-closure))
         (sha256-id? (:c11-mir-id semantic-closure))
         (= (:c11-artifact-id semantic-closure)
            (p15-s23-c11-mir-digest
             {:kind :gravity/p15-s23-c11-authenticated-mir-artifact
              :schema-version 1
              :mir-id (:c11-mir-id semantic-closure)}))
         (= operation-count
            (count (get-in gate-a [:source-debug-map :entries]))
            (count (get-in gate-a
                           [:proof-to-c-assumption-map
                            :operation-bindings]))
            (:operation-count semantic-closure))
         (= (get-in gate-a [:verified-input-closure :effect-count])
            (:effect-fact-row-count semantic-closure))
         (= (get-in gate-a [:verified-input-closure :capability-count])
            (:capability-fact-row-count semantic-closure))
         (= (get-in gate-a [:verified-input-closure :runtime-check-count])
            (:runtime-check-count semantic-closure))
         (every? #(and (integer? %) (not (neg? %)))
                 ((juxt :operation-count :effect-fact-row-count
                        :capability-fact-row-count :runtime-check-count
                        :capability-proof-count :semantic-effect-count
                        :semantic-capability-count)
                  semantic-closure))
         (zero? (:runtime-check-count semantic-closure))
         (zero? (:capability-proof-count semantic-closure))
         (= #{} (:semantic-effects semantic-closure)
            (:semantic-capabilities semantic-closure)
            (:main-latent-effects semantic-closure)
            (:main-capabilities semantic-closure))
         (zero? (:semantic-effect-count semantic-closure))
         (zero? (:semantic-capability-count semantic-closure))
         (true? (:all-operation-effects-empty? semantic-closure))
         (true? (:all-operation-capabilities-empty? semantic-closure))
         (= [] (get-in gate-a [:runtime-helper-manifest :helpers]))
         (= [] (get-in gate-a
                       [:runtime-helper-manifest
                        :gravity-runtime-providers]))
         (false? (get-in gate-a
                         [:runtime-helper-manifest
                          :hidden-runtime-dependence?]))
         (true? (get-in gate-a
                        [:proof-to-c-assumption-map
                         :all-casts-proof-authorized?]))
         (= (:evidence-id semantic-closure)
            (p15-s23-c11-mir-digest
             {:kind :gravity/b2-c17-semantic-pure-closure-evidence
              :schema-version 1 :record semantic-closure-base}))
         (= (:report-id contextual-report)
            (p15-s23-c11-mir-digest
             {:kind :gravity/b2-c17-contextual-verification-report
              :schema-version 1 :report contextual-base}))))))
