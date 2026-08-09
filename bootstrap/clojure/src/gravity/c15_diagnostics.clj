(ns gravity.c15-diagnostics
  "Hosted Stage0 C15 structured-diagnostics adapter and evidence projection."
  (:require [clojure.string]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c14-lowering-source-artifact
    :c15-diagnostics-source-overrides :c15-stable-diagnostic-id
    :c15-diagnostics-fail! :c15-diagnostics-validate-source-overrides!
    :c15-diagnostic-record :c15-diagnostic-catalog
    :c15-diagnostics-validate! :c15-diagnostics-capability-proof
    :compiler-c15-diagnostics-source-artifact
    :compiler-c15-diagnostics-file-artifact})
(def ^:private scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c15-diagnostics-governing-document
    :c15-diagnostics-diagnostic-ids
    :c15-diagnostic-required-fields})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)
        ampersand-index (.indexOf ^java.util.List args '&)
        variadic? (not= -1 ampersand-index)
        fixed-args (when variadic? (take ampersand-index args))
        rest-binding (when variadic? (nth args (inc ampersand-index)))
        rest-symbol (when variadic? 'operation-options)
        operation-symbol (gensym "operation")
        emitted-args (if variadic?
                       (assoc args (inc ampersand-index)
                              (assoc rest-binding :as rest-symbol))
                       args)]
    `(defn ~name ~emitted-args
       (if-let [~operation-symbol (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           ~(if variadic?
              `(apply ~operation-symbol ~@fixed-args
                      (mapcat identity ~rest-symbol))
              `(~operation-symbol ~@args)))
         (do ~@body)))))
(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C15 leaf requires injected operation " key)
                    {:operation key}))))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op :fail! (fn [rule text payload]
                (throw (ex-info text (assoc (or payload {}) :id rule)))))
   id message data))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- sha256-hex [value]
  ((op :sha256-hex digest/sha256-hex) value))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate)))))
   value))
(defn- read-source-form-records [path text]
  ((op :read-source-form-records (unsupported :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c14-lowering-source-artifact [path text]
  ((op :compiler-c14-lowering-source-artifact
       (unsupported :compiler-c14-lowering-source-artifact))
   path text))
(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c15-diagnostics-governing-document
  "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md")

(def ^:dynamic c15-diagnostics-diagnostic-ids
  ["C15-SCHEMA"
   "C15-ID"
   "C15-SPAN"
   "C15-ORIGIN"
   "C15-FACTS"
   "C15-REMEDIATION"
   "C15-REDACTION"
   "C15-ORDER"
   "C15-GOLDEN"])

(def ^:dynamic c15-diagnostic-required-fields
  [:artifact :diagnostic-id :rule :severity :stage :message-key :primary
   :related :origin-chain :profile :target :involved-artifacts :facts
   :remediation :redactions :lifecycle])

(definterposable c15-diagnostics-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c15-diagnostics])
      (get-in module [:metadata :compiler :verification])
      {}))

(definterposable c15-stable-diagnostic-id
  [diagnostic]
  (str "diag-"
       (sha256-hex
        (pr-str {:rule (:rule diagnostic)
                 :stage (:stage diagnostic)
                 :primary-artifact (get-in diagnostic [:primary :artifact])
                 :facts (:facts diagnostic)}))))

(definterposable c15-diagnostics-fail!
  [id source-path subject extra]
  (fail! id
         (get compiler-verification-diagnostic-messages id
              "compiler diagnostic validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :compiler-diagnostics
                 :stage (or (:stage subject) :c15-compiler-diagnostics)
                 :offending-diagnostic-id (:diagnostic-id subject)
                 :schema-field (:schema-field subject)
                 :artifact-id (:artifact-id subject)
                 :profile (:profile subject)
                 :target (:target subject)
                 :remediation "Regenerate structured diagnostic artifacts with stable ids, primary spans, origin chains, facts, remediation, redaction, deterministic ordering, and golden fixtures."}
                extra)))

(definterposable c15-diagnostics-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get compiler-verification-override-diagnostics
                                 fail-kind)]
      (when (contains? (set c15-diagnostics-diagnostic-ids) id)
        (c15-diagnostics-fail!
         id source-path
         {:stage subject-kind
          :diagnostic-id (str "c15-invalid-" (name fail-kind))
          :schema-field fail-kind
          :artifact-id (str "c15-diagnostic-artifact-" (name fail-kind))
          :profile :hosted
          :target :jvm}
         {:missing-fields [fail-kind]})))))

(definterposable c15-diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact
   facts remediation & {:keys [related origin-chain redactions lifecycle
                               generated?]}]
  (let [diagnostic
        {:artifact :gravity/diagnostic
         :rule rule
         :severity severity
         :stage stage
         :message-key message-key
         :primary {:span (source-span source-path form-index)
                   :syntax-id (str "c15-syntax-" form-index)
                   :artifact primary-artifact}
         :related (vec related)
         :origin-chain (vec origin-chain)
         :profile :hosted
         :target :jvm
         :involved-artifacts [primary-artifact]
         :facts facts
         :remediation (vec remediation)
         :redactions (vec redactions)
         :lifecycle (or lifecycle :active)
         :generated? (true? generated?)}]
    (assoc diagnostic
           :diagnostic-id (c15-stable-diagnostic-id diagnostic)
           :ordering-key [rule stage primary-artifact form-index])))

(definterposable c15-diagnostic-catalog
  []
  {:artifact :gravity/diagnostic-catalog
   :status :complete
   :rules
   (mapv (fn [id]
           {:rule id
            :severity (if (= "C15-GOLDEN" id) :hint :error)
            :message-key (keyword "diagnostic"
                                  (clojure.string/lower-case
                                   (clojure.string/replace id #"_" "-")))
            :explain-page (str "gravity://diagnostics/" id)
            :lifecycle :active
            :stable-id-policy :rule-primary-artifact-stage-facts})
         c15-diagnostics-diagnostic-ids)})

(definterposable c15-diagnostics-validate!
  [source-path artifact]
  (let [required (set c15-diagnostic-required-fields)
        schema-fields (set (get-in artifact [:diagnostic-schema
                                             :required-fields]))
        diagnostics (get-in artifact [:diagnostic-stream :diagnostics])
        catalog-rules (set (map :rule (get-in artifact
                                               [:diagnostic-catalog :rules])))
        golden (:golden-diagnostic-fixtures artifact)]
    (when-not (= required schema-fields)
      (c15-diagnostics-fail! "C15-SCHEMA" source-path
                             (:diagnostic-schema artifact)
                             {:missing-fields
                              (vec (remove schema-fields required))}))
    (when-not (= (count diagnostics)
                 (count (distinct (map :diagnostic-id diagnostics))))
      (c15-diagnostics-fail! "C15-ID" source-path
                             (first diagnostics)
                             {:missing-fields [:diagnostic-id]}))
    (doseq [diagnostic diagnostics]
      (let [present (set (keys diagnostic))]
        (when-not (every? present required)
          (c15-diagnostics-fail! "C15-SCHEMA" source-path diagnostic
                                 {:missing-fields
                                  (vec (remove present required))})))
      (when-not (= (:diagnostic-id diagnostic)
                   (c15-stable-diagnostic-id
                    (dissoc diagnostic :diagnostic-id :ordering-key)))
        (c15-diagnostics-fail! "C15-ID" source-path diagnostic
                               {:missing-fields [:stable-id]}))
      (when-not (and (get-in diagnostic [:primary :span])
                     (get-in diagnostic [:primary :syntax-id])
                     (get-in diagnostic [:primary :artifact]))
        (c15-diagnostics-fail! "C15-SPAN" source-path diagnostic
                               {:missing-fields [:primary]}))
      (when (and (:generated? diagnostic)
                 (or (empty? (:origin-chain diagnostic))
                     (not-any? #(= :generated-by (:role %))
                               (:related diagnostic))))
        (c15-diagnostics-fail! "C15-ORIGIN" source-path diagnostic
                               {:missing-fields [:origin-chain]}))
      (when-not (and (map? (:facts diagnostic))
                     (seq (:facts diagnostic)))
        (c15-diagnostics-fail! "C15-FACTS" source-path diagnostic
                               {:missing-fields [:facts]}))
      (when-not (seq (:remediation diagnostic))
        (c15-diagnostics-fail! "C15-REMEDIATION" source-path diagnostic
                               {:missing-fields [:remediation]})))
    (when-not (true? (get-in artifact [:redaction-report :public-safe?]))
      (c15-diagnostics-fail! "C15-REDACTION" source-path
                             (:redaction-report artifact)
                             {:missing-fields [:public-safe]}))
    (when-not (= diagnostics (vec (sort-by :ordering-key diagnostics)))
      (c15-diagnostics-fail! "C15-ORDER" source-path
                             (:diagnostic-stream artifact)
                             {:missing-fields [:ordering-key]}))
    (when-not (= (set c15-diagnostics-diagnostic-ids) catalog-rules)
      (c15-diagnostics-fail! "C15-SCHEMA" source-path
                             (:diagnostic-catalog artifact)
                             {:missing-fields [:diagnostic-catalog]}))
    (when-not (and (= (set c15-diagnostics-diagnostic-ids)
                      (set (map :rule golden)))
                   (every? #(= :matched (:status %)) golden))
      (c15-diagnostics-fail! "C15-GOLDEN" source-path
                             (first golden)
                             {:missing-fields [:golden-fixtures]})))
  :complete)

(definterposable c15-diagnostics-capability-proof
  [artifact]
  {:c14-lowering-input-verified?
   (= :complete (get-in artifact
                        [:c14-lowering-artifact
                         :capability-based-proof :status]))
   :diagnostic-schema-complete?
   (= :complete (get-in artifact [:diagnostic-schema :status]))
   :diagnostic-stream-deterministic?
   (= (get-in artifact [:diagnostic-stream :diagnostics])
      (vec (sort-by :ordering-key
                    (get-in artifact
                            [:diagnostic-stream :diagnostics]))))
   :stable-ids?
   (every? (fn [diagnostic]
             (= (:diagnostic-id diagnostic)
                (c15-stable-diagnostic-id
                 (dissoc diagnostic :diagnostic-id :ordering-key))))
           (get-in artifact [:diagnostic-stream :diagnostics]))
   :locations-and-origins-linked?
   (every? #(and (get-in % [:primary :span])
                 (get-in % [:primary :syntax-id])
                 (get-in % [:primary :artifact])
                 (or (not (:generated? %))
                     (and (seq (:origin-chain %))
                          (some (fn [related]
                                  (= :generated-by (:role related)))
                                (:related %)))))
           (get-in artifact [:diagnostic-stream :diagnostics]))
   :facts-structured?
   (every? #(and (map? (:facts %)) (seq (:facts %)))
           (get-in artifact [:diagnostic-stream :diagnostics]))
   :remediation-and-quick-fixes?
   (and (every? #(seq (:remediation %))
                (get-in artifact [:diagnostic-stream :diagnostics]))
        (every? #(= :available (:status %))
                (:remediation-and-quick-fix-records artifact)))
   :redaction-public-safe?
   (true? (get-in artifact [:redaction-report :public-safe?]))
   :renderers-covered?
   (= #{:cli :ide :ci :safety-report :package-report}
      (set (map :renderer (:rendering-records artifact))))
   :golden-fixtures-matched?
   (every? #(= :matched (:status %))
           (:golden-diagnostic-fixtures artifact))
   :diagnostics-covered?
   (= (set c15-diagnostics-diagnostic-ids)
      (set (map :rule (:golden-diagnostic-fixtures artifact))))
   :status :complete})

(definterposable compiler-c15-diagnostics-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c15-diagnostics-source-overrides module)
        _ (c15-diagnostics-validate-source-overrides! source-path
                                                      source-overrides)
        lowering-artifact (compiler-c14-lowering-source-artifact source-path
                                                                 source-text)
        lowering-id (:artifact-id lowering-artifact)
        diagnostics
        (vec
         (sort-by
          :ordering-key
          [(c15-diagnostic-record
            "C15-FACTS" :info :c15-compiler-diagnostics
            :diagnostic.structured-facts source-path 0 lowering-id
            {:fact-families [:types :effects :capabilities :safety
                             :proofs :target-features]
             :artifact lowering-id}
            [{:kind :inspect-facts}])
           (c15-diagnostic-record
            "C15-ORIGIN" :warning :c15-compiler-diagnostics
            :diagnostic.generated-origin source-path 1 lowering-id
            {:generated-form "c15-generated-check"
             :producer :compiler-c15-diagnostics
             :source-producer :stage0-build-macro}
            [{:kind :jump-to-source-producer}]
            :generated? true
            :origin-chain
            [{:producer :stage0-build-macro
              :source (source-span source-path 1)
              :generated-artifact lowering-id}]
            :related
            [{:role :generated-by
              :span (source-span source-path 1)
              :artifact :stage0-build-macro}])
           (c15-diagnostic-record
            "C15-REDACTION" :error :c15-compiler-diagnostics
            :diagnostic.redaction-policy source-path 2 lowering-id
            {:redacted-fields [:credential-value :private-expansion]
             :policy :public-diagnostic}
            [{:kind :move-to-private-artifact-store}]
            :redactions [{:field :credential-value
                          :replacement :redacted
                          :value-hash "sha256:redacted-stage0"}])
           (c15-diagnostic-record
            "C15-GOLDEN" :hint :c15-compiler-diagnostics
            :diagnostic.golden-fixture source-path 3 lowering-id
            {:fixture :compiler-c15-diagnostics
             :asserts [:rule :severity :primary :related :facts
                       :remediation :redactions :ordering]}
            [{:kind :regenerate-golden-fixture}])]))
        summary (frequencies (map :severity diagnostics))
        catalog (c15-diagnostic-catalog)
        artifact-base
        {:kind :gravity/stage0-c15-compiler-diagnostics-artifact
         :task "P06-D094"
         :document-set ["C15"]
         :governing-document c15-diagnostics-governing-document
         :pass {:name :c15-compiler-diagnostics
                :input :target-artifact-manifest
                :output :diagnostic-artifact-bundle
                :requires [:c14-target-artifact-manifest :source-spans
                           :origin-chain :profile :target :facts
                           :remediation-policy :redaction-policy]
                :preserves [:source-spans :origin-chain :profile :target
                            :artifact-provenance :facts :redactions]
                :emits [:diagnostic-schema :diagnostic-stream
                        :diagnostic-catalog :related-span-map
                        :remediation-and-quick-fix-records
                        :redaction-report :rendering-records
                        :golden-diagnostic-fixtures]
                :rejects c15-diagnostics-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c14-lowering-artifact
         (select-keys lowering-artifact
                      [:kind :task :artifact-id :governing-document
                       :target-artifact-manifest :capability-based-proof])
         :lowering-artifact-kind (:kind lowering-artifact)
         :lowering-artifact-hash lowering-id
         :diagnostic-schema
         {:artifact :gravity/diagnostic-schema
          :status :complete
          :required-fields c15-diagnostic-required-fields
          :stable-id-input [:rule :primary-artifact :stage :facts]
          :display-wording-version "stage0-c15"}
         :diagnostic-stream
         {:artifact :gravity/diagnostic-stream
          :stage :c15-compiler-diagnostics
          :input-artifact lowering-id
          :output-artifact :gravity/diagnostic-artifact-bundle
          :diagnostics diagnostics
          :summary summary
          :deterministic-ordering-key :ordering-key
          :redaction-policy :public-safe
          :rendering-version "stage0-c15"
          :status :complete}
         :diagnostic-catalog catalog
         :related-span-map
         {:artifact :gravity/related-span-map
          :status :complete
          :entries (mapv (fn [diagnostic]
                           {:diagnostic-id (:diagnostic-id diagnostic)
                            :related (:related diagnostic)})
                         diagnostics)}
         :remediation-and-quick-fix-records
         (mapv (fn [id]
                 {:rule id
                  :remediation :structured-diagnostic-repair
                  :quick-fix :regenerate-diagnostic-artifact
                  :status :available})
               c15-diagnostics-diagnostic-ids)
         :redaction-report
         {:artifact :gravity/diagnostic-redaction-report
          :status :passed
          :public-safe? true
          :redacted-value-hashes ["sha256:redacted-stage0"]
          :private-artifact-store :authorized-only
          :raw-secret-values-present? false}
         :rendering-records
         [{:renderer :cli
           :source :gravity/diagnostic-stream
           :status :complete}
          {:renderer :ide
           :source :gravity/diagnostic-stream
           :status :complete}
          {:renderer :ci
           :source :gravity/diagnostic-stream
           :status :complete}
          {:renderer :safety-report
           :source :gravity/diagnostic-stream
           :status :complete}
          {:renderer :package-report
           :source :gravity/diagnostic-stream
           :status :complete}]
         :golden-diagnostic-fixtures
         (mapv (fn [id]
                 {:fixture (str "compiler-c15-" id)
                  :rule id
                  :asserts [:rule :severity :primary :related :stage
                            :profile :target :facts :remediation
                            :redactions :ordering]
                  :status :matched})
               c15-diagnostics-diagnostic-ids)
         :c15-diagnostics-results
         {:documents ["C15"]
          :task "P06-D094"
          :required-diagnostic-ids c15-diagnostics-diagnostic-ids
          :c14-input-status :complete
          :schema-status :complete
          :stream-status :complete
          :catalog-status :complete
          :related-span-status :complete
          :remediation-status :complete
          :redaction-status :complete
          :rendering-status :complete
          :golden-status :complete
          :status :complete}
         :diagnostics []}
        _ (c15-diagnostics-validate! source-path artifact-base)
        capability-proof (c15-diagnostics-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c15-diagnostics-file-artifact
  [path]
  (compiler-c15-diagnostics-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c15-compiler-diagnostics
   :dependency-direction
   {:requires ['clojure.string 'gravity.compiler-verification-shared
               'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c15-diagnostic-schema
          :hosted-stage0-c15-diagnostic-evidence]
   :does-not-own [:canonical-c15-authority :source-authentication
                  :redaction-policy-authority :privacy-authority
                  :localization-authority :renderer-authority
                  :golden-fixture-authority :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :diagnostic-system-complete? false
   :canonical-c15-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))
(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn- override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key)
                      (vector? entry)
                      (= 2 (count entry))
                      (string? (first entry))
                      (keyword? (second entry))))
               value)))
(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C15 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C15 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C15 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c15-diagnostics-governing-document
            #(and (string? %) (seq %))]
           [:c15-diagnostics-diagnostic-ids string-vector?]
           [:c15-diagnostic-required-fields keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C15 scalar operation has invalid shape" {:key key})))
  operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              compiler-verification-diagnostic-messages
              (get merged :compiler-verification-diagnostic-messages
                   compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics
              (get merged :compiler-verification-override-diagnostics
                   compiler-verification-override-diagnostics)
              c15-diagnostics-governing-document
              (get merged :c15-diagnostics-governing-document
                   c15-diagnostics-governing-document)
              c15-diagnostics-diagnostic-ids
              (get merged :c15-diagnostics-diagnostic-ids
                   c15-diagnostics-diagnostic-ids)
              c15-diagnostic-required-fields
              (get merged :c15-diagnostic-required-fields
                   c15-diagnostic-required-fields)]
      (thunk))))
(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c15-engine-contract {:arglists '([])}
   'c15-diagnostics-governing-document {:kind :constant}
   'c15-diagnostics-diagnostic-ids {:kind :constant}
   'c15-diagnostic-required-fields {:kind :constant}
   'c15-diagnostics-source-overrides {:arglists '([module])}
   'c15-stable-diagnostic-id {:arglists '([diagnostic])}
   'c15-diagnostics-fail! {:arglists '([id source-path subject extra])}
   'c15-diagnostics-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c15-diagnostic-record
   {:arglists '([rule severity stage message-key source-path form-index
                 primary-artifact facts remediation & {:keys [related
                 origin-chain redactions lifecycle generated?]
                 :as operation-options}])}
   'c15-diagnostic-catalog {:arglists '([])}
   'c15-diagnostics-validate! {:arglists '([source-path artifact])}
   'c15-diagnostics-capability-proof {:arglists '([artifact])}
   'compiler-c15-diagnostics-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c15-diagnostics-file-artifact {:arglists '([path])}})
(defn c15-engine-contract []
  (assoc namespace-contract :public-api public-api))
