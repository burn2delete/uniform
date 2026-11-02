(ns gravity.diagnostics
  "Stage 0 base diagnostic carrier.

  This namespace owns construction and throwing of the bootstrap
  ExceptionInfo carrier, native seed C15 record/identity construction and
  private source-record binding through explicit source and digest operations,
  plus native C15 catalog and metadata-override projection.
  Native record validation retains its existing label-only checks.
  Canonical rule catalogs/schema, redaction,
  rendering, and stage-specific diagnostic policy remain with their owning
  compiler stages."
  (:require [clojure.string]))

(def ^:private namespace-contract
  {:namespace 'gravity.diagnostics
   :contract-boundary :stage0-diagnostic-carriers-and-record-data
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 BOOT8 TEST2]
   :public-api
   {'native-context-valid? {:arglists '([diagnostic]) :admission :none}
    'native-primary-valid? {:arglists '([diagnostic]) :admission :none}
    'validate-native! {:arglists '([operations configuration source-path artifact]) :admission :none
                       :operation-inputs [:fail! :stable-id :context-valid? :primary-valid?]}
    'source-overrides {:arglists '([module]) :admission :none}
    'native-catalog {:arglists '([diagnostic-ids]) :admission :none}
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
                     :original-eleven-c15-fields :private-candidate :complete-digest-request]
   :artifact-outputs [:native-phase-and-primary-predicates :native-record-validation-result
                      :native-source-overrides :native-c15-catalog :stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id
                      :resolved-request-data :private-c15-binding :source-owned-rejection]
   :diagnostic-ownership
   {:owns [:native-context-primary-and-record-stream-rules :legacy-redaction-and-golden-label-checks
           :native-source-override-projection :native-c15-catalog-projection :stage0-carrier-construction :stage0-carrier-throw :native-seed-record-construction
           :legacy-stable-id-preimage :request-binding-orchestration :fresh-source-verification-orchestration]
    :does-not-own
    [:canonical-rule-catalogs :canonical-c15-schema :identity-authentication :stream-ordering
     :redaction :rendering :stage-specific-policy]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :consumed-by ['gravity.c15-diagnostics.records 'gravity.c15-diagnostics.validation 'gravity.bootstrap]
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
  "Constructs, binds and freshly verifies private C15 data. No public projection."
  [invoke canonical-digest source-path original-fields]
  (let [unresolved (invoke 'build-c15-diagnostic original-fields)]
    (if (= :rejected (:status unresolved))
      unresolved
      (let [request (:diagnostic-id-request unresolved)
            candidate (assoc unresolved :diagnostic-id (canonical-digest source-path request))
            verification (verify-private-record invoke canonical-digest source-path
                                                original-fields candidate request)]
        (if (= :passed (:status verification))
          {:status :passed :diagnostic candidate :verification verification}
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

(doseq [operation [#'native-context-valid? #'native-primary-valid? #'validate-native!
                  #'source-overrides #'native-catalog #'diagnostic #'fail! #'stable-id #'diagnostic-record
                  #'resolve-request-id #'verify-private-record #'bind-private-record]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
