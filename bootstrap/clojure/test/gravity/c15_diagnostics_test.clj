(ns gravity.c15-diagnostics-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c15-diagnostics :as c15]
            [gravity.diagnostics :as diagnostics]
            [gravity.digest :as digest]))

(def module
  {:module 'gravity.c15-test
   :source-path "c15-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c14-artifact
  {:kind :gravity/stage0-c14-target-lowering-artifact
   :task "P06-D093"
   :artifact-id "sha256:c14"
   :governing-document "docs/c14.md"
   :target-artifact-manifest {:artifact :gravity/target-artifact-manifest}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c15-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c14-lowering-source-artifact (fn [_ _] c14-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c15/c15-engine-contract)
        publics (ns-publics 'gravity.c15-diagnostics)]
    (is (= :hosted-stage0-c15-compiler-diagnostics
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c15-authority? contract)))
    (is (false? (:diagnostic-system-complete? contract)))
    (is (some #{:redaction-policy-authority} (:does-not-own contract)))
    (is (some #{:renderer-authority} (:does-not-own contract)))
    (is (= #{'clojure.string 'gravity.compiler-verification-shared
             'gravity.digest 'gravity.diagnostics}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c15/public-api))))
    (doseq [[name spec] c15/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-variadic-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c15-diagnostic-record :keyword-is-invokable}
           {:c15-diagnostics-governing-document ""}
           {:c15-diagnostics-diagnostic-ids [:bad]}
           {:c15-diagnostic-required-fields [:artifact "bad"]}
           {:compiler-verification-diagnostic-messages {"C15-OK" :bad}}
           {:compiler-verification-override-diagnostics
            {:c15-schema ["C15-SCHEMA" "bad"]}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c15/with-operations operation-map
                   (constantly :unreachable)))))
  (let [received (atom nil)
        sentinel {:artifact :sentinel}]
    (is (= sentinel
           (c15/with-operations
             {:c15-diagnostic-record
              (fn [& args]
                (reset! received args)
                sentinel)}
             #(c15/c15-diagnostic-record
               "C15-SCHEMA" :error :c15 "message" "c15.gravity" 0
               "artifact" {:field :schema} [{:kind :repair}]
               :generated? true :related [{:role :generated-by}]))))
    (is (= [:generated? true :related [{:role :generated-by}]]
           (vec (drop 9 @received)))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c15/with-operations
             {:compiler-c15-diagnostics-source-artifact (fn [_ _] sentinel)}
             #(c15/compiler-c15-diagnostics-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity"))))))

(deftest diagnostics-engine-emits-complete-products
  (let [artifact
        (c15/with-operations
          (operations)
          #(c15/compiler-c15-diagnostics-source-artifact
            "c15-test.gravity" "source"))
        diagnostics (get-in artifact [:diagnostic-stream :diagnostics])]
    (is (= :gravity/stage0-c15-compiler-diagnostics-artifact
           (:kind artifact)))
    (is (= :gravity/stage0-c14-target-lowering-artifact
           (:lowering-artifact-kind artifact)))
    (is (= :complete (get-in artifact [:diagnostic-schema :status])))
    (is (= 4 (count diagnostics)))
    (is (every? #(= [:hosted :jvm nil]
                   [(:profile %) (:target %) (get-in % [:primary :syntax-id])]) diagnostics))
    (is (= diagnostics (vec (sort-by :ordering-key diagnostics))))
    (is (= :compiler-diagnostics (get-in artifact [:diagnostic-stream :stage])))
    (is (every? #(re-matches #"diag-[0-9a-f]{64}" (:diagnostic-id %))
                diagnostics))
    (is (= 9 (count (get-in artifact [:diagnostic-catalog :rules]))))
    (is (= 9 (count (:remediation-and-quick-fix-records artifact))))
    (is (true? (get-in artifact [:redaction-report :public-safe?])))
    (is (= #{:cli :ide :ci :safety-report :package-report}
           (set (map :renderer (:rendering-records artifact)))))
    (is (= 9 (count (:golden-diagnostic-fixtures artifact))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c15-identities
  (doseq [[failure expected-id]
          [[:c15-schema "C15-SCHEMA"]
           [:c15-id "C15-ID"]
           [:c15-span "C15-SPAN"]
           [:c15-origin "C15-ORIGIN"]
           [:c15-facts "C15-FACTS"]
           [:c15-remediation "C15-REMEDIATION"]
           [:c15-redaction "C15-REDACTION"]
           [:c15-order "C15-ORDER"]
           [:c15-golden "C15-GOLDEN"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c15-diagnostics :fail]
                      failure)
            error
            (try
              (c15/with-operations
                (operations failed-module)
                #(c15/compiler-c15-diagnostics-source-artifact
                  "c15-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (every? #(and (contains? (ex-data error) %) (nil? (get (ex-data error) %)))
                    [:profile :target :syntax-id]))
        (is (= :compiler-diagnostics (:stage (ex-data error))))))))

(deftest stage-records-preserve-identity-inputs-and-private-data
  (doseq [operation [#'diagnostics/diagnostic #'diagnostics/fail!
                    #'diagnostics/stable-id #'diagnostics/diagnostic-record]]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.diagnostics (:namespace contract)))
      (is (= (:arglists (meta operation))
             (get-in contract [:public-api (:name (meta operation)) :arglists])))))
  (let [events (atom [])
        span {:source "actual.qst" :start-byte 7 :end-byte 12}
        origin [{:producer :macro :syntax-id "source-syntax"}]
        facts (sorted-map :expected :integer :actual :keyword)
        record (diagnostics/diagnostic-record
                 (fn [path index] (swap! events conj [:span path index]) span)
                 (fn [value] (swap! events conj [:identity value])
                   (diagnostics/stable-id digest/sha256-hex value))
                 "C7-TYPE" :error :type-check :type.mismatch "actual.qst" 3 "core-id"
                 facts [{:kind :change-type}]
                 :related [{:role :generated-by :span span}] :origin-chain origin
                 :redactions [{:field :private :replacement :redacted}]
                 :lifecycle :stale-after-edit :generated? true)
        identity-input (second (second @events))]
    (is (= [:span "actual.qst" 3] (first @events)))
    (is (= 2 (count @events)))
    (is (not (contains? identity-input :diagnostic-id)))
    (is (not (contains? identity-input :ordering-key)))
    (is (= span (get-in record [:primary :span])))
    (is (= origin (:origin-chain record)))
    (is (identical? facts (:facts record)))
    (is (= ["C7-TYPE" :type-check "core-id" 3] (:ordering-key record)))
    (is (= :stale-after-edit (:lifecycle record)))
    (is (true? (:generated? record)))
    (is (= (:diagnostic-id record)
           (diagnostics/stable-id digest/sha256-hex (assoc record :message-key :new.wording))))
    (doseq [changed [(assoc record :rule "C8-EFFECT") (assoc record :stage :effect-check)
                     (assoc-in record [:primary :artifact] "other-core")
                     (assoc record :facts {:different :facts})]]
      (is (not= (:diagnostic-id record) (diagnostics/stable-id digest/sha256-hex changed))))
    (is (= (str "diag-" (pr-str {:rule "C7-TYPE" :stage :type-check :primary-artifact "core-id" :facts facts}))
           (diagnostics/stable-id identity record)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest stage-record-wrapper-preserves-nested-operation-interposition
  (let [span {:source "interposed.gravity" :form-index 9}
        calls (atom [])
        record (c15/with-operations
                 {:source-span (fn [path index] (swap! calls conj [:span path index]) span)
                  :sha256-hex (fn [text] (swap! calls conj [:hash text]) "test-hash")}
                 #(c15/c15-diagnostic-record "C15-FACTS" :info :c15 :facts "given.gravity" 2
                                            "artifact" {:fact :preserved} [{:kind :inspect}]))]
    (is (= span (get-in record [:primary :span])))
    (is (= "diag-test-hash" (:diagnostic-id record)))
    (is (= [:span "given.gravity" 2] (first @calls)))
    (is (= :hash (first (second @calls))))
    (is (= 2 (count @calls))))
  (is (= "custom-id"
         (:diagnostic-id
           (c15/with-operations
             {:c15-stable-diagnostic-id (constantly "custom-id")}
             #(c15/c15-diagnostic-record "C15-FACTS" :info :c15 :facts "given.qst" 0
                                        "artifact" {:fact :preserved} [{:kind :inspect}]))))))

(deftest phase-context-is-explicit-and-never-fabricated
  (let [span {:source "early.qst" :start-byte 3 :end-byte 8}
        make-record (fn [& options]
                      (c15/with-operations
                        {:source-span (fn [_ _] span)}
                        #(apply c15/c15-diagnostic-record
                                "C15-FACTS" :error :read-source :reader.invalid "early.qst" 5
                                "source-id" {:expected :delimiter} [{:kind :close-delimiter}]
                                options)))
        early (make-record)
        known (make-record :profile :kernel :target :wasm :syntax-id "actual-syntax-id")]
    (is (= span (get-in early [:primary :span])))
    (doseq [record [early known]]
      (is (contains? record :profile))
      (is (contains? record :target))
      (is (contains? (:primary record) :syntax-id)))
    (is (= [nil nil nil] [(:profile early) (:target early) (get-in early [:primary :syntax-id])]))
    (is (= [:kernel :wasm "actual-syntax-id"]
           [(:profile known) (:target known) (get-in known [:primary :syntax-id])]))
    ;; The legacy native ID protocol remains rule/stage/artifact/facts.
    (is (= (:diagnostic-id early) (:diagnostic-id known)))
    (is (= early (make-record :profile nil :target nil :syntax-id nil))))
  (doseq [context [{:profile :kernel :target :llvm} {}]]
    (let [artifact (c15/with-operations
                     (operations (merge (dissoc module :profile :target) context))
                     #(c15/compiler-c15-diagnostics-source-artifact "early.gravity" "source"))]
      (doseq [record (get-in artifact [:diagnostic-stream :diagnostics])]
        (is (= (:profile context) (:profile record)))
        (is (= (:target context) (:target record)))
        (is (contains? (:primary record) :syntax-id))
        (is (= :compiler-diagnostics (:stage record)))
        (is (nil? (get-in record [:primary :syntax-id])))))))

(deftest native-validation-distinguishes-unavailable-and-missing-context
  (let [artifact (c15/with-operations
                   (operations (dissoc module :profile :target))
                   #(c15/compiler-c15-diagnostics-source-artifact "early.gravity" "source"))
        first-record [:diagnostic-stream :diagnostics 0]
        ;; Reidentify each malformed record to reach location validation after
        ;; the independent stable-ID check (artifact participates in that ID).
        update-record (fn [f]
                        (update-in artifact first-record
                                   #(let [changed (f %)]
                                      (assoc changed :diagnostic-id (c15/c15-stable-diagnostic-id changed)))))
        rejection (fn [value]
                    (try (c15/c15-diagnostics-validate! "early.gravity" value) nil
                         (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= :complete (c15/c15-diagnostics-validate! "early.gravity" artifact)))
    (is (true? (:phase-context-valid? (c15/c15-diagnostics-capability-proof artifact))))
    (is (true? (:locations-and-origins-linked? (c15/c15-diagnostics-capability-proof artifact))))
    (doseq [field [:profile :target]]
      (let [error (rejection (update-record #(dissoc % field)))]
        (is (= "C15-SCHEMA" (:id error)))
        (is (= [field] (:missing-fields error)))))
    (doseq [invalid [(update-record #(update % :primary dissoc :syntax-id))
                     (update-record #(assoc-in % [:primary :syntax-id] false))
                     (update-record #(update % :primary dissoc :span))
                     (update-record #(update % :primary dissoc :artifact))]]
      (is (= "C15-SPAN" (:id (rejection invalid))))
      (is (false? (:locations-and-origins-linked? (c15/c15-diagnostics-capability-proof invalid)))))
    (doseq [stage [:read-source :compiler-diagnostics]]
      (is (= :complete (c15/c15-diagnostics-validate! "early.gravity" (update-record #(assoc % :stage stage))))))
    (doseq [stage [:effect-check :type-check :reader :c15-compiler-diagnostics]]
      (let [invalid (update-record #(assoc % :stage stage))]
        (is (= "C15-SCHEMA" (:id (rejection invalid))))
        (is (false? (:phase-context-valid? (c15/c15-diagnostics-capability-proof invalid))))
        (is (= :incomplete (:status (c15/c15-diagnostics-capability-proof invalid))))))
    (let [semantic (update-record #(assoc % :stage :effect-check :profile :kernel :target :llvm))]
      (is (= "C15-SPAN" (:id (rejection semantic))))
      (is (true? (:phase-context-valid? (c15/c15-diagnostics-capability-proof semantic))))
      (doseq [syntax-id [nil false "" 3]]
        (is (= "C15-SPAN" (:id (rejection (assoc-in semantic (conj first-record :primary :syntax-id) syntax-id))))))
      (doseq [syntax-id ["actual-syntax-id" :actual-syntax-id]]
        (is (= :complete (c15/c15-diagnostics-validate!
                           "early.gravity" (assoc-in semantic (conj first-record :primary :syntax-id) syntax-id)))))
      (doseq [field [:profile :target] value [false "hosted" 3]]
        (let [invalid (assoc-in semantic (conj first-record field) value)]
          (is (= "C15-SCHEMA" (:id (rejection invalid))))
          (is (false? (:phase-context-valid? (c15/c15-diagnostics-capability-proof invalid)))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))
