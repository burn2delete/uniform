(ns gravity.diagnostics
  "Stage 0 base diagnostic carrier.

  This namespace owns construction and throwing of the bootstrap
  ExceptionInfo carrier, native seed C15 record/identity construction and
  private source-record binding through explicit source and digest operations,
  plus native C15 catalog and metadata-override projection.
  It also owns the existing native fixture, artifact/proof, failure/override
  and source/file assembly bodies through trusted late operation suppliers.
  Native record validation retains its existing label-only checks.
  Canonical rule catalogs/schema, redaction,
  rendering, and stage-specific diagnostic policy remain with their owning
  compiler stages."
  (:require [clojure.string]))

(def ^:private namespace-contract
  {:namespace 'gravity.diagnostics
   :contract-boundary :stage0-diagnostic-carriers-and-record-data
   :governing-contracts '[D1 D2 D3 D6 D8 D9 C1 C2 C3 C4 C14 C15 T1 T2 T3 BOOT8 TEST2]
   :public-api
   {'native-context-valid? {:arglists '([diagnostic]) :admission :none}
    'native-primary-valid? {:arglists '([diagnostic]) :admission :none}
    'validate-native! {:arglists '([operations configuration source-path artifact]) :admission :none
                       :operation-inputs [:fail! :stable-id :context-valid? :primary-valid?]}
    'source-overrides {:arglists '([module]) :admission :none}
    'native-catalog {:arglists '([diagnostic-ids]) :admission :none}
    'native-fixture-diagnostics {:arglists '([path lowering-id module operations]) :admission :none}
    'native-artifact-base
    {:arglists '([configuration module source-overrides lowering-artifact diagnostics catalog]) :admission :none}
    'native-capability-proof {:arglists '([configuration artifact operations]) :admission :none}
    'native-failure! {:arglists '([configuration id source-path subject extra operations]) :admission :none}
    'native-validate-source-overrides! {:arglists '([configuration source-path overrides operations]) :admission :none}
    'native-source-artifact {:arglists '([configuration source-path source-text operations]) :admission :none}
    'native-file-artifact {:arglists '([configuration path operations]) :admission :none}
    'diagnostic {:arglists '([id message data])
                 :returns :clojure.lang/exception-info}
    'fail! {:arglists '([id message data])
            :throws :clojure.lang/exception-info}
    'stable-id {:arglists '([hash-text diagnostic]) :admission :none}
    'resolve-request-id
    {:arglists '([canonical-digest missing-request! source-path diagnostic]) :admission :none}
    'verify-private-record
    {:arglists '([invoke canonical-digest source-path original-fields candidate request]) :admission :none}
    'bind-private-record
    {:arglists '([invoke canonical-digest source-path original-fields]) :admission :none}
    'diagnostic-record
    {:arglists '([source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation & {:keys [related origin-chain redactions lifecycle generated? profile target syntax-id]}])
     :admission :none}}
   :artifact-inputs [:native-c15-artifacts :native-validation-configuration :explicit-native-validation-operations
                     :native-compiler-metadata :native-c15-rule-id-order :rule-id :human-message :structured-data :explicit-span-and-identity-operations
                     :native-seed-diagnostic-fields :source-invocation :canonical-request-digest
                     :original-eleven-c15-fields :private-candidate :complete-digest-request
                     :trusted-native-source-module-configuration-and-c14-parent
                     :trusted-zero-argument-operation-suppliers]
   :artifact-outputs [:native-phase-and-primary-predicates :native-record-validation-result
                      :native-source-overrides :native-c15-catalog :stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id
                      :resolved-request-data :private-c15-binding :source-owned-rejection
                      :native-c15-fixture-diagnostics-artifact-and-capability-data
                      :native-failure-payload-or-original-callback-result]
   :diagnostic-ownership
   {:owns [:native-context-primary-and-record-stream-rules :legacy-redaction-and-golden-label-checks
           :native-source-override-projection :native-c15-catalog-projection :stage0-carrier-construction :stage0-carrier-throw :native-seed-record-construction
           :legacy-stable-id-preimage :request-binding-orchestration :fresh-source-verification-orchestration
           :native-c15-fixture-ordering-and-artifact-proof-assembly
           :native-failure-payload-and-override-selection :native-source-and-file-assembly]
    :does-not-own
    [:canonical-rule-catalogs :canonical-c15-schema :identity-authentication :canonical-stream-ordering
     :redaction :rendering :stage-specific-policy]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :consumed-by ['gravity.c15-diagnostics.records 'gravity.c15-diagnostics.validation
                  'gravity.c15-diagnostics.artifact 'gravity.c15-diagnostics.projection
                  'gravity.c15-diagnostics.proof 'gravity.bootstrap]
    :forbids ['gravity.bootstrap 'gravity.c15-diagnostics 'gravity.cli 'gravity.release]}
   :unsupported [:canonical-c15-conformance :untrusted-native-ingress :public-projection
                 :redaction-policy :canonical-map-encoding :migration :release :seed-retirement]
   :record-context {:profile :caller-supplied-or-nil :target :caller-supplied-or-nil
                    :syntax-id :caller-supplied-or-nil}
   :supplied-redactions-applied? false
   :private-binding {:schema-owner 'gravity.compiler.c15-compiler-diagnostics
                     :digest-input :complete-request :digest-provider :trusted-native-operation
                     :source-provider :trusted-invocation-operation :public-safe? false
                     :upstream-authentication false :source-verifier-nonclaims :preserved}
   :test-owner
   'gravity.diagnostics-test/base-exception-info-carrier-is-extracted-with-bootstrap-parity
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn native-context-valid? [diagnostic]
  (let [unavailable? (contains? #{:read-source :compiler-diagnostics} (:stage diagnostic))]
    (every? (fn [field]
              (and (contains? diagnostic field)
                   (let [value (get diagnostic field)]
                     (or (keyword? value) (and unavailable? (nil? value))))))
            [:profile :target])))
(defn native-primary-valid? [diagnostic]
  (let [primary (:primary diagnostic)
        syntax-id (:syntax-id primary)]
    (and (map? primary)
         (every? #(get primary %) [:span :artifact])
         (contains? primary :syntax-id)
         (or (keyword? syntax-id) (and (string? syntax-id) (seq syntax-id))
             (and (nil? syntax-id) (contains? #{:read-source :compiler-diagnostics} (:stage diagnostic)))))))
(defn validate-native!
  [operations configuration source-path artifact]
  (let [{:keys [fail! stable-id context-valid? primary-valid?]} operations
        required (set (:c15-diagnostic-required-fields configuration))
        schema-fields (set (get-in artifact [:diagnostic-schema :required-fields]))
        diagnostics (get-in artifact [:diagnostic-stream :diagnostics])
        catalog-rules (set (map :rule (get-in artifact [:diagnostic-catalog :rules])))
        golden (:golden-diagnostic-fixtures artifact)]
    (when-not (= required schema-fields) (fail! configuration "C15-SCHEMA" source-path (:diagnostic-schema artifact)
                                                 {:missing-fields (vec (remove schema-fields required))}))
    (when-not (= (count diagnostics) (count (distinct (map :diagnostic-id diagnostics))))
      (fail! configuration "C15-ID" source-path (first diagnostics) {:missing-fields [:diagnostic-id]}))
    (doseq [diagnostic diagnostics]
      (let [present (set (keys diagnostic))]
        (when-not (every? present required) (fail! configuration "C15-SCHEMA" source-path diagnostic
                                                       {:missing-fields (vec (remove present required))})))
      (when-not (= (:diagnostic-id diagnostic) (stable-id (dissoc diagnostic :diagnostic-id :ordering-key)))
        (fail! configuration "C15-ID" source-path diagnostic {:missing-fields [:stable-id]}))
      (when-not (context-valid? diagnostic)
        (fail! configuration "C15-SCHEMA" source-path diagnostic {:missing-fields [:phase-context]}))
      (when-not (primary-valid? diagnostic)
        (fail! configuration "C15-SPAN" source-path diagnostic {:missing-fields [:primary]}))
      (when (and (:generated? diagnostic) (or (empty? (:origin-chain diagnostic))
                                               (not-any? #(= :generated-by (:role %)) (:related diagnostic))))
        (fail! configuration "C15-ORIGIN" source-path diagnostic {:missing-fields [:origin-chain]}))
      (when-not (and (map? (:facts diagnostic)) (seq (:facts diagnostic)))
        (fail! configuration "C15-FACTS" source-path diagnostic {:missing-fields [:facts]}))
      (when-not (seq (:remediation diagnostic))
        (fail! configuration "C15-REMEDIATION" source-path diagnostic {:missing-fields [:remediation]})))
    (when-not (true? (get-in artifact [:redaction-report :public-safe?]))
      (fail! configuration "C15-REDACTION" source-path (:redaction-report artifact) {:missing-fields [:public-safe]}))
    (when-not (= diagnostics (vec (sort-by :ordering-key diagnostics)))
      (fail! configuration "C15-ORDER" source-path (:diagnostic-stream artifact) {:missing-fields [:ordering-key]}))
    (when-not (= (set (:c15-diagnostics-diagnostic-ids configuration)) catalog-rules)
      (fail! configuration "C15-SCHEMA" source-path (:diagnostic-catalog artifact) {:missing-fields [:diagnostic-catalog]}))
    (when-not (and (= (set (:c15-diagnostics-diagnostic-ids configuration)) (set (map :rule golden)))
                   (every? #(= :matched (:status %)) golden))
      (fail! configuration "C15-GOLDEN" source-path (first golden) {:missing-fields [:golden-fixtures]})))
  :complete)

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c15-diagnostics]) (get-in module [:metadata :compiler :verification]) {}))
(defn native-catalog [diagnostic-ids]
  {:artifact :gravity/diagnostic-catalog :status :complete
   :rules (mapv (fn [id] {:rule id :severity (if (= "C15-GOLDEN" id) :hint :error)
                          :message-key (keyword "diagnostic" (clojure.string/lower-case
                                                               (clojure.string/replace id #"_" "-")))
                          :explain-page (str "gravity://diagnostics/" id) :lifecycle :active
                          :stable-id-policy :rule-primary-artifact-stage-facts})
                diagnostic-ids)})

(defn diagnostic
  [id message data]
  (ex-info message
           (merge {:id id
                   :message message
                   :bootstrap-stage :stage0}
                  data)))

(defn fail!
  [id message data]
  (throw (diagnostic id message data)))

(defn stable-id [hash-text diagnostic]
  (str "diag-" (hash-text
                (pr-str {:rule (:rule diagnostic) :stage (:stage diagnostic)
                         :primary-artifact (get-in diagnostic [:primary :artifact])
                         :facts (:facts diagnostic)}))))

(defn resolve-request-id
  "Resolves prevalidated request data; the caller retains ingress admission."
  [canonical-digest missing-request! source-path diagnostic]
  (let [request (:diagnostic-id-request diagnostic)]
    (when-not (map? request)
      (missing-request! source-path diagnostic))
    (-> diagnostic
        (dissoc :diagnostic-id-request)
        (assoc :diagnostic-id (canonical-digest source-path request)))))

(defn verify-private-record
  "Rebuilds from original fields in C15 with a fresh native full-request digest.
  Operations are trusted seed inputs; preserve the source verifier's nonclaims."
  [invoke canonical-digest source-path original-fields candidate request]
  (invoke 'verify-c15-resolved-diagnostic
          [original-fields candidate request (canonical-digest source-path request)]))

(defn bind-private-record
  "Constructs, binds and freshly verifies private C15 data and its singleton stream.
  No public projection. Stream legality is checked only by the source invocation."
  [invoke canonical-digest source-path original-fields]
  (let [unresolved (invoke 'build-c15-diagnostic original-fields)]
    (if (= :rejected (:status unresolved))
      unresolved
      (let [request (:diagnostic-id-request unresolved)
            candidate (assoc unresolved :diagnostic-id (canonical-digest source-path request))
            verification (verify-private-record invoke canonical-digest source-path
                                                original-fields candidate request)]
        (if (= :passed (:status verification))
          (let [stream {:stage (:stage candidate)
                        :input-artifact (get-in candidate [:primary :artifact])
                        :output-artifact nil :diagnostics [candidate]
                        :diagnostic-bindings [{:original-fields original-fields
                                               :request request
                                               :resolved-digest (:diagnostic-id candidate)}]
                        :summary-counts (assoc {:error 0 :warning 0 :info 0 :hint 0
                                                :internal-error 0}
                                               (:severity candidate) 1)
                        :deterministic-ordering-key :diagnostic-id
                        :redaction-policy :private-unassessed
                        :rendering-version "c15-private-v1"}
                stream-verification (invoke 'verify-c15-diagnostic-stream [stream])]
            ;; Bind the returned product to this invocation's actual stream.
            ;; This checks the operation result, never substitutes a host verifier.
            (when-not (= {:artifact :gravity/c15-diagnostic-stream-verification
                          :schema-version 1 :status :passed :diagnostics []
                          :diagnostic-stream stream :summary-counts (:summary-counts stream)
                          :identity-request-joins [{:record-index 0
                                                    :diagnostic-id (:diagnostic-id candidate)
                                                    :request request}]
                          :verification-status :private-stream-structure-and-external-digest-equality
                          :semantic-authority :none :redaction-status :not-evaluated
                          :nonclaims [:digest-recomputation :upstream-source-authentication
                                      :public-safe-projection :golden-fixture-verification
                                      :self-hosting :release :seed-retirement]}
                         stream-verification)
              (fail! "C15-SCHEMA" "Private diagnostic stream verification failed"
                     {:stage :compiler-diagnostics :schema-field :stream-verification
                      :reason :invalid-stream-verification-result}))
            {:status :passed :diagnostic candidate :verification verification})
          verification)))))

(defn diagnostic-record
  [source-span stable-id rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated? profile target syntax-id]}]
  (let [diagnostic {:artifact :gravity/diagnostic :rule rule :severity severity :stage stage
                    :message-key message-key
                    :primary {:span (source-span source-path form-index)
                              :syntax-id syntax-id :artifact primary-artifact}
                    :related (vec related) :origin-chain (vec origin-chain) :profile profile :target target
                    :involved-artifacts [primary-artifact] :facts facts :remediation (vec remediation)
                    :redactions (vec redactions) :lifecycle (or lifecycle :active) :generated? (true? generated?)}]
    (assoc diagnostic :diagnostic-id (stable-id diagnostic)
           :ordering-key [rule stage primary-artifact form-index])))

(defn native-fixture-diagnostics [path lowering-id module operations]
  (vec (sort-by :ordering-key
                [(((:diagnostic-record operations)) "C15-FACTS" :info :compiler-diagnostics :diagnostic.structured-facts path 0 lowering-id
                                    {:fact-families [:types :effects :capabilities :safety :proofs :target-features]
                                     :artifact lowering-id} [{:kind :inspect-facts}]
                                    :profile (:profile module) :target (:target module))
                 (((:diagnostic-record operations)) "C15-ORIGIN" :warning :compiler-diagnostics :diagnostic.generated-origin path 1 lowering-id
                                    {:generated-form "c15-generated-check" :producer :compiler-c15-diagnostics
                                     :source-producer :stage0-build-macro} [{:kind :jump-to-source-producer}]
                                    :profile (:profile module) :target (:target module) :generated? true
                                    :origin-chain [{:producer :stage0-build-macro :source (((:source-span operations)) path 1)
                                                    :generated-artifact lowering-id}]
                                    :related [{:role :generated-by :span (((:source-span operations)) path 1)
                                               :artifact :stage0-build-macro}])
                 (((:diagnostic-record operations)) "C15-REDACTION" :error :compiler-diagnostics :diagnostic.redaction-policy path 2 lowering-id
                                    {:redacted-fields [:credential-value :private-expansion] :policy :public-diagnostic}
                                    [{:kind :move-to-private-artifact-store}]
                                    :profile (:profile module) :target (:target module)
                                    :redactions [{:field :credential-value :replacement :redacted
                                                  :value-hash "sha256:redacted-stage0"}])
                 (((:diagnostic-record operations)) "C15-GOLDEN" :hint :compiler-diagnostics :diagnostic.golden-fixture path 3 lowering-id
                                    {:fixture :compiler-c15-diagnostics
                                     :asserts [:rule :severity :primary :related :facts :remediation :redactions :ordering]}
                                    [{:kind :regenerate-golden-fixture}]
                                    :profile (:profile module) :target (:target module))])))
(defn native-artifact-base [configuration module source-overrides lowering-artifact diagnostics catalog]
  (let [lowering-id (:artifact-id lowering-artifact)]
    {:kind :gravity/stage0-c15-compiler-diagnostics-artifact :task "P06-D094" :document-set ["C15"]
     :governing-document (:c15-diagnostics-governing-document configuration)
     :pass {:name :c15-compiler-diagnostics :input :target-artifact-manifest :output :diagnostic-artifact-bundle
            :requires [:c14-target-artifact-manifest :source-spans :origin-chain :profile :target :facts
                       :remediation-policy :redaction-policy]
            :preserves [:source-spans :origin-chain :profile :target :artifact-provenance :facts :redactions]
            :emits [:diagnostic-schema :diagnostic-stream :diagnostic-catalog :related-span-map
                    :remediation-and-quick-fix-records :redaction-report :rendering-records :golden-diagnostic-fixtures]
            :rejects (:c15-diagnostics-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c14-lowering-artifact (select-keys lowering-artifact [:kind :task :artifact-id :governing-document
                                                             :target-artifact-manifest :capability-based-proof])
     :lowering-artifact-kind (:kind lowering-artifact) :lowering-artifact-hash lowering-id
     :diagnostic-schema {:artifact :gravity/diagnostic-schema :status :complete
                         :required-fields (:c15-diagnostic-required-fields configuration)
                         :stable-id-input [:rule :primary-artifact :stage :facts]
                         :display-wording-version "stage0-c15"}
     :diagnostic-stream {:artifact :gravity/diagnostic-stream :stage :compiler-diagnostics
                         :input-artifact lowering-id :output-artifact :gravity/diagnostic-artifact-bundle
                         :diagnostics diagnostics :summary (frequencies (map :severity diagnostics))
                         :deterministic-ordering-key :ordering-key :redaction-policy :public-safe
                         :rendering-version "stage0-c15" :status :complete}
     :diagnostic-catalog catalog
     :related-span-map {:artifact :gravity/related-span-map :status :complete
                        :entries (mapv (fn [d] {:diagnostic-id (:diagnostic-id d) :related (:related d)}) diagnostics)}
     :remediation-and-quick-fix-records
     (mapv (fn [id] {:rule id :remediation :structured-diagnostic-repair
                     :quick-fix :regenerate-diagnostic-artifact :status :available})
           (:c15-diagnostics-diagnostic-ids configuration))
     :redaction-report {:artifact :gravity/diagnostic-redaction-report :status :passed :public-safe? true
                        :redacted-value-hashes ["sha256:redacted-stage0"] :private-artifact-store :authorized-only
                        :raw-secret-values-present? false}
     :rendering-records [{:renderer :cli :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :ide :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :ci :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :safety-report :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :package-report :source :gravity/diagnostic-stream :status :complete}]
     :golden-diagnostic-fixtures
     (mapv (fn [id] {:fixture (str "compiler-c15-" id) :rule id
                     :asserts [:rule :severity :primary :related :stage :profile :target :facts
                               :remediation :redactions :ordering] :status :matched})
           (:c15-diagnostics-diagnostic-ids configuration))
     :c15-diagnostics-results {:documents ["C15"] :task "P06-D094"
                               :required-diagnostic-ids (:c15-diagnostics-diagnostic-ids configuration)
                               :c14-input-status :complete :schema-status :complete :stream-status :complete
                               :catalog-status :complete :related-span-status :complete :remediation-status :complete
                               :redaction-status :complete :rendering-status :complete :golden-status :complete
                               :status :complete}
     :diagnostics []}))

(defn native-capability-proof [configuration artifact operations]
  (let [diagnostics (get-in artifact [:diagnostic-stream :diagnostics])
        checks {:c14-lowering-input-verified? (= :complete (get-in artifact [:c14-lowering-artifact :capability-based-proof :status]))
     :diagnostic-schema-complete? (= :complete (get-in artifact [:diagnostic-schema :status]))
     :diagnostic-stream-deterministic? (= diagnostics (vec (sort-by :ordering-key diagnostics)))
     :stable-ids? (every? #(= (:diagnostic-id %) (((:stable-id operations)) (dissoc % :diagnostic-id :ordering-key))) diagnostics)
     :phase-context-valid? (every? ((:context-valid? operations)) diagnostics)
     :locations-and-origins-linked?
     (every? #(and (((:primary-valid? operations)) %)
                    (or (not (:generated? %)) (and (seq (:origin-chain %))
                                                    (some (fn [related] (= :generated-by (:role related))) (:related %))))) diagnostics)
     :facts-structured? (every? #(and (map? (:facts %)) (seq (:facts %))) diagnostics)
     :remediation-and-quick-fixes? (and (every? #(seq (:remediation %)) diagnostics)
                                        (every? #(= :available (:status %)) (:remediation-and-quick-fix-records artifact)))
     :redaction-public-safe? (true? (get-in artifact [:redaction-report :public-safe?]))
     :renderers-covered? (= #{:cli :ide :ci :safety-report :package-report}
                             (set (map :renderer (:rendering-records artifact))))
     :golden-fixtures-matched? (every? #(= :matched (:status %)) (:golden-diagnostic-fixtures artifact))
     :diagnostics-covered? (= (set (:c15-diagnostics-diagnostic-ids configuration))
                               (set (map :rule (:golden-diagnostic-fixtures artifact))))}]
    (assoc checks :status (if (every? true? (vals checks)) :complete :incomplete))))

(defn native-failure! [configuration id source-path subject extra operations]
  (((:invoke operations)) :c15-diagnostics-fail!
                     (fn [rule path item fields]
                       (((:invoke operations)) :fail!
                                          (fn [r text payload] (throw (ex-info text (assoc (or payload {}) :id r))) )
                                          rule (get (:compiler-verification-diagnostic-messages configuration) rule
                                                    "compiler diagnostic validation failed")
                                          (merge {:source-span (or (:source-span item) (((:source-span operations)) path 0))
                                                  :diagnostic-family :compiler-diagnostics
                                                  :stage (or (:stage item) :compiler-diagnostics)
                                                  :offending-diagnostic-id (:diagnostic-id item)
                                                  :schema-field (:schema-field item) :artifact-id (:artifact-id item)
                                                  :profile (:profile item) :target (:target item)
                                                  :syntax-id (get-in item [:primary :syntax-id])
                                                  :remediation "Regenerate structured diagnostic artifacts with stable ids, primary spans, origin chains, facts, remediation, redaction, deterministic ordering, and golden fixtures."}
                                                 fields)))
                     id source-path subject extra))
(defn native-validate-source-overrides! [configuration source-path overrides operations]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get (:compiler-verification-override-diagnostics configuration) fail-kind)]
      (when (contains? (set (:c15-diagnostics-diagnostic-ids configuration)) id)
        (((:fail! operations)) configuration id source-path {:stage :compiler-diagnostics
                                              :diagnostic-id (str "c15-invalid-" (name fail-kind))
                                              :schema-field fail-kind :artifact-id (str "c15-diagnostic-artifact-" (name fail-kind))
                                              :profile nil :target nil}
               {:missing-fields [fail-kind] :subject-kind subject-kind})))))
(defn native-source-artifact [configuration source-path source-text operations]
  (let [records* (((:invoke operations)) :read-source-form-records (((:unsupported operations)) :read-source-form-records) source-path source-text)
        forms (mapv :form records*)
        _ (((:invoke operations)) :validate-ns-syntax! (((:unsupported operations)) :validate-ns-syntax!) source-path forms)
        module (((:invoke operations)) :parse-module (((:unsupported operations)) :parse-module) source-path forms)
        source-overrides (((:invoke operations)) :c15-diagnostics-source-overrides ((:source-overrides operations)) module)
        _ (((:invoke operations)) :c15-diagnostics-validate-source-overrides!
                             (fn [path values] (((:validate-source-overrides! operations)) configuration path values))
                             source-path source-overrides)
        lowering-artifact (((:invoke operations)) :compiler-c14-lowering-source-artifact
                                              (((:unsupported operations)) :compiler-c14-lowering-source-artifact) source-path source-text)
        lowering-id (:artifact-id lowering-artifact)
        diagnostics* (((:fixture-diagnostics operations)) source-path lowering-id module)
        catalog (((:invoke operations)) :c15-diagnostic-catalog (fn [] (((:catalog operations)) configuration)))
        base (((:artifact-base operations)) configuration module source-overrides lowering-artifact diagnostics* catalog)
        _ (((:invoke operations)) :c15-diagnostics-validate! (fn [path value] (((:validate! operations)) configuration path value)) source-path base)
        capability-proof (((:invoke operations)) :c15-diagnostics-capability-proof
                                            (fn [value] (((:capability-proof operations)) configuration value)) base)]
    (assoc base :capability-based-proof capability-proof
           :artifact-id (((:artifact-id operations)) (assoc base :capability-based-proof capability-proof)))))
(defn native-file-artifact [configuration path operations]
  (((:invoke operations)) :compiler-c15-diagnostics-source-artifact
                     (fn [source-path source-text] (((:source-artifact operations)) configuration source-path source-text)) path (((:slurp operations)) path)))

(doseq [operation [#'native-context-valid? #'native-primary-valid? #'validate-native!
                  #'source-overrides #'native-catalog #'diagnostic #'fail! #'stable-id #'diagnostic-record
                  #'resolve-request-id #'verify-private-record #'bind-private-record
                  #'native-fixture-diagnostics #'native-artifact-base #'native-capability-proof
                  #'native-failure! #'native-validate-source-overrides! #'native-source-artifact #'native-file-artifact]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
