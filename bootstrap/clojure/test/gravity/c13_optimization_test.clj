(ns gravity.c13-optimization-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c13-optimization :as c13]
            [gravity.optimization :as optimization]
            [gravity.optimization-lowering :as shared]
            [gravity.c13-optimization.artifact :as artifact]
            [gravity.c13-optimization.operations :as c13-ops]
            [gravity.c13-optimization.projection :as projection]
            [gravity.c13-optimization.validation :as validation]))

(def module
  {:module 'gravity.c13-test
   :source-path "c13-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c12-artifact
  {:kind :gravity/stage0-c12-domain-ir-architecture-artifact
   :task "P06-D091"
   :artifact-id "sha256:c12"
   :governing-document "docs/c12.md"
   :domain-verifier-report {:status :passed}
   :semantic-anchor-map [{:domain :efir :syntax-id "syntax-0"}]
   :capability-based-proof {:status :complete}
   :domain-ir-artifacts
   [{:source {:syntax-id "syntax-0"
              :span {:source "c13-test.gravity" :form-index 0}}}]})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c13-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c12-domain-ir-source-artifact (fn [_ _] c12-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c13/c13-engine-contract)
        publics (ns-publics 'gravity.c13-optimization)]
    (is (= :hosted-stage0-c13-optimization (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c13-authority? contract)))
    (is (false? (:optimization-model-complete? contract)))
    (is (some #{:check-elision-authority} (:does-not-own contract)))
    (is (some #{:target-lowering-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest 'gravity.optimization-lowering 'gravity.optimization}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c13/public-api))))
    (doseq [[name spec] c13/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c13-optimization-validate! :keyword-is-invokable}
           {:c13-optimization-governing-document ""}
           {:c13-optimization-diagnostic-ids [:bad]}
           {:optimization-lowering-diagnostic-messages {"C13-OK" :bad}}
           {:optimization-pass-contract-seed []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c13/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        artifact
        (c13/with-operations
          (assoc (operations)
                 :optimization-decision-record
                 (fn [domain input index contract]
                   (swap! calls inc)
                   (shared/optimization-decision-record
                    domain input index contract)))
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))]
    (is (= 6 @calls))
    (is (= 6 (count (:optimization-decision-log artifact)))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c13/with-operations
             {:compiler-c13-optimization-source-artifact
              (fn [_ _] sentinel)}
             #(c13/compiler-c13-optimization-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity"))))))

(deftest optimization-engine-emits-complete-products
  (let [artifact
        (c13/with-operations
          (operations)
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))
        decisions (:optimization-decision-log artifact)]
    (is (= :gravity/stage0-c13-mir-optimization-artifact (:kind artifact)))
    (is (= :gravity/stage0-c12-domain-ir-architecture-artifact
           (:domain-ir-artifact-kind artifact)))
    (is (= 6 (count (:optimization-pass-registry artifact))))
    (is (= 6 (count decisions)))
    (is (= 6 (count (:invalidated-fact-ledger artifact))))
    (is (= 6 (count (:analysis-cache-records artifact))))
    (is (= 6 (count (:proof-and-certificate-usage artifact))))
    (is (= 6 (count (:post-pass-verifier-reports artifact))))
    (is (some seq (map :changed-ops decisions)))
    (is (some empty? (map :changed-ops decisions)))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (not (contains? artifact :residual-cost-report)))
    (is (not-any? #{:residual-cost-report} (get-in artifact [:pass :emits])))
    (is (not (contains? (:capability-based-proof artifact) :residual-cost-visible?)))
    (is (not (contains? (:c13-optimization-results artifact) :residual-cost-status)))
    (doseq [check [:proof-evidence-present? :check-elision-proof?
                   :effect-order-preserved? :safety-outcomes-current?
                   :post-pass-verifiers-passed?]]
      (is (true? (get-in artifact [:capability-based-proof check]))))
    (is (= (set shared/c13-optimization-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:optimization-diagnostic-stream
                              :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c13-identities
  (doseq [[failure expected-id]
          [[:contract "C13-CONTRACT"]
           [:preserve "C13-PRESERVE"]
           [:invalidate "C13-INVALIDATE"]
           [:proof "C13-PROOF"]
           [:check-elision "C13-CHECK-ELISION"]
           [:effect "C13-EFFECT"]
           [:safety "C13-SAFETY"]
           [:domain "C13-DOMAIN"]
           [:nondeterminism "C13-NONDETERMINISM"]
           [:verify "C13-VERIFY"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module
                      [:metadata :compiler :c13-optimization :fail]
                      failure)
            error
            (try
              (c13/with-operations
                (operations failed-module)
                #(c13/compiler-c13-optimization-source-artifact
                  "c13-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))

(deftest native-file-dispatch-captures-invoke-before-reading-arguments
  (let [marker (Object.) calls (atom [])
        replacement (fn [& _] (throw (AssertionError. "Late invoke root was used")))
        original (fn [key _ & values] (swap! calls conj [key values]) marker)]
    (with-redefs [c13-ops/invoke original
                  clojure.core/slurp
                  (fn [path]
                    (is (= "native-port.qst" path))
                    (alter-var-root #'c13-ops/invoke (constantly replacement))
                    "native source text")]
      (is (identical? marker (artifact/file-artifact {} "native-port.qst"))))
    (is (= [[:compiler-c13-optimization-source-artifact
             ["native-port.qst" "native source text"]]] @calls))))

(deftest private-digest-root-is-captured-before-serialization-callback
  ;; Trusted native callback sequencing only; these supplied digest strings
  ;; are not accepted source identities or proof/certificate authority.
  (let [hash-var (ns-resolve 'gravity.c13-optimization.projection 'sha256-hex)
        calls (atom [])
        old-hash (fn [_] (swap! calls conj :old) "old")
        new-hash (fn [_] (swap! calls conj :new) "new")
        pass (proxy [Object] []
               (toString [] (alter-var-root hash-var (constantly new-hash)) "pass"))
        decision {:pass pass :output-mir :output :decision-id :decision :proofs-used []}
        result (with-redefs-fn {hash-var old-hash}
                 #(projection/artifact-base {} "source" {} {} {} :input
                                            [] [decision] {}))]
    (is (= [:old :old] @calls))
    (is (= "sha256:old" (get-in result [:analysis-cache-records 0 :cache-key])))))

(deftest source-assembly-keeps-public-leaf-roots-live-between-steps
  (let [calls (atom []) base {:native :base} proof {:native :projection}
        after (fn [configuration path value]
                (is (map? configuration)) (is (= "late-source.gravity" path))
                (is (identical? base value)) (swap! calls conj :late-validation) :complete)
        result
        (with-redefs [projection/artifact-base
                      (fn [& _]
                        (alter-var-root #'validation/validate! (constantly after))
                        (swap! calls conj :projection) base)
                      validation/validate!
                      (fn [& _] (throw (AssertionError. "Early validation root was retained")))
                      validation/capability-proof
                      (fn [_ value] (is (identical? base value)) proof)]
          (c13/with-operations
            (assoc (operations) :c4-artifact-id (constantly "native-port-result"))
            #(c13/compiler-c13-optimization-source-artifact "late-source.gravity" "source")))]
    (is (= [:projection :late-validation] @calls))
    (is (= {:native :base :capability-based-proof proof :artifact-id "native-port-result"} result))))

(deftest native-validation-retains-all-refusal-order-and-callback-errors
  (let [value (c13/with-operations (operations)
                #(c13/compiler-c13-optimization-source-artifact "c13-test.gravity" "source"))
        changed (-> value
                    (update-in [:optimization-pass-registry 0] dissoc :pass)
                    (assoc-in [:optimization-pipeline-manifest :ordering] :changed)
                    (assoc-in [:optimization-decision-log 0 :preserved] [])
                    (assoc :invalidated-fact-ledger [] :analysis-cache-records []
                           :proof-and-certificate-usage [])
                    (assoc-in [:optimization-decision-log 0 :proofs-used] [])
                    (assoc-in [:check-elision-record :status] :changed)
                    (assoc-in [:effect-reordering-record :status] :changed)
                    (assoc-in [:safety-outcome-refresh-report :status] :changed)
                    (assoc-in [:domain-anchor-transform-report :status] :changed)
                    (assoc-in [:optimization-replay-record :status] :changed)
                    (assoc-in [:post-pass-verifier-reports 0 :status] :failed)
                    (assoc-in [:optimization-diagnostic-stream :diagnostics] []))
        calls (atom []) marker (Object.)]
    ;; A returning failure callback retains the old complete marker, not a
    ;; certificate. Actual throwing refusals are also preserved separately.
    (is (= :complete
           (c13/with-operations
             {:optimization-lowering-fail!
              (fn [id path actual _ _]
                (is (= "ordered-native.qst" path)) (is (identical? changed actual))
                (swap! calls conj id) marker)}
             #(c13/c13-optimization-validate! "ordered-native.qst" changed))))
    (is (= ["C13-CONTRACT" "C13-CONTRACT" "C13-NONDETERMINISM" "C13-PRESERVE"
            "C13-INVALIDATE" "C13-INVALIDATE" "C13-PROOF" "C13-PROOF"
            "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY" "C13-DOMAIN"
            "C13-NONDETERMINISM" "C13-VERIFY" "C13-CONTRACT"] @calls))
    (let [error (ex-info "Original failure callback" {})
          observed (try
                     (c13/with-operations {:optimization-lowering-fail! (fn [& _] (throw error))}
                       #(c13/c13-optimization-validate! "throwing-native.gravity" changed))
                     nil (catch clojure.lang.ExceptionInfo observed observed))]
      (is (identical? error observed)))))

(def ^:private elision-fields [:proof-id :proof :certificate :certificate-id :dominating-proof])
(defn- elision-present [_] false)
(defn- elision-proof [_] false)
(defn- elision-failure [id message data] (throw (ex-info message (assoc data :id id))))
(def ^:private elision-module {:source-path "native-elision.gravity" :profile :hosted :target :jvm})

(deftest stage0-elision-owner-declares-metadata-without-facade-authority
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (empty? (ns-aliases 'gravity.optimization)))
  (is (= 23 (count (ns-publics 'gravity.optimization))))
  (doseq [operation [#'optimization/native-stage0-check-elision-proof-present?
                     #'optimization/validate-native-stage0-check-elision!]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (true? (:native-data-only? entry)))
      (is (= :stage0-check-elision-metadata-presence-and-refusal (:contract-boundary entry)))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:safety-check-elision} (:unsupported contract)))
      (is (some #{:proof-provider-validation} (:unsupported contract)))
      (is (not (contains? (ns-publics 'gravity.c13-optimization) (:name (meta operation))))))))

(deftest stage0-elision-presence-retains-field-order-short-circuits-and-values
  (doseq [hit (range 5)]
    (let [values (vec (repeatedly 5 #(Object.))) marker (Object.) events (atom [])
          lookup (zipmap elision-fields values)
          input (reify clojure.lang.ILookup
                  (valAt [_ key] (swap! events conj [:get key]) (get lookup key))
                  (valAt [_ _ _] (throw (AssertionError. "unexpected defaulted presence lookup"))))]
      (with-redefs [elision-present (fn [value] (swap! events conj [:call value])
                                     (when (identical? value (nth values hit)) marker))]
        (is (identical? marker (optimization/native-stage0-check-elision-proof-present? #'elision-present input)))
        (is (= (vec (mapcat (fn [field value] [[:get field] [:call value]])
                           (take (inc hit) elision-fields) (take (inc hit) values))) @events)))))
  (doseq [result [nil false]]
    (let [calls (atom [])]
      (with-redefs [elision-present (fn [value] (swap! calls conj value) result)]
        (is (= result (optimization/native-stage0-check-elision-proof-present? #'elision-present {})))
        (is (= [nil nil nil nil nil] @calls))))))

(deftest stage0-elision-presence-selects-callee-before-getter-and-reloads-next-field
  (let [events (atom []) first-value (Object.) second-value (Object.) marker (Object.)
        late (fn [value] (swap! events conj [:late value]) marker)
        input (reify clojure.lang.ILookup
                (valAt [_ key]
                  (swap! events conj [:get key])
                  (case key :proof-id (do (alter-var-root #'elision-present (constantly late)) first-value)
                    :proof second-value (throw (AssertionError. "presence failed to short circuit"))))
                (valAt [_ _ _] (throw (AssertionError. "unexpected defaulted lookup"))))]
    (with-redefs [elision-present (fn [value] (swap! events conj [:selected value]) false)]
      (is (identical? marker (optimization/native-stage0-check-elision-proof-present? #'elision-present input)))
      (is (= [[:get :proof-id] [:selected first-value] [:get :proof] [:late second-value]] @events)))))

(deftest stage0-elision-validation-keeps-strict-erasure-and-unconditional-presence
  (doseq [[flag checks erased?] [[true nil true] [true (Object.) true]
                                 [false nil false] [nil [] false] [:truthy nil false] [0 nil false]
                                 [false [:bounds] true] [nil '(:bounds) true]]
          present? [false true]]
    (let [record {:erased? flag :erased-checks checks :check-class :bounds :operation :at :ir-node :node}
          marker (Object.) proofs (atom []) failures (atom [])]
      (with-redefs [elision-proof (fn [actual] (swap! proofs conj actual) present?)
                    elision-failure (fn [& args] (swap! failures conj (vec args)) marker)]
        (let [value (optimization/validate-native-stage0-check-elision!
                      #'elision-failure #'elision-proof elision-module record)]
          (is (= 1 (count @proofs)))
          (is (identical? record (first @proofs)))
          (if (and erased? (not present?))
            (do (is (identical? marker value))
                (is (= [["PERF10-PROOF-MISSING" "compiled performance metadata erases a check without proof"
                         {:source-span {:source "native-elision.gravity"} :profile :hosted :target :jvm
                          :check-class :bounds :operation :at :ir-node :node :missing-proof :dominating-proof
                          :pass-id :stage0-compiled-performance-gate :diagnostic-family :compiled-performance-validation
                          :remediation "Keep the runtime check or attach a dominating proof and certificate before claiming check elision."}]]
                       @failures)))
            (do (is (nil? value)) (is (empty? @failures)))))))))

(deftest stage0-elision-accepted-records-do-not-touch-failure-or-payload-fields
  (let [module (reify clojure.lang.ILookup
                 (valAt [_ key] (if (= :source-path key) "native-elision.gravity"
                                   (throw (AssertionError. "accepted record read failure payload"))))
                 (valAt [_ _ _] (throw (AssertionError. "defaulted module lookup"))))
        unused-failure (reify clojure.lang.IDeref
                         (deref [_] (throw (AssertionError. "accepted record selected failure"))))
        calls (atom [])]
    (doseq [record [{:erased? false} {:erased? true}]]
      (with-redefs [elision-proof (fn [actual] (swap! calls conj actual) true)]
        (is (nil? (optimization/validate-native-stage0-check-elision!
                    unused-failure #'elision-proof module record)))))
    (is (= [{:erased? false} {:erased? true}] @calls))))

(deftest stage0-elision-validation-selects-late-proof-and-captures-failure-before-payload
  (let [events (atom []) marker (Object.) calls (atom []) path (Object.) profile (Object.) target (Object.)
        check (Object.) operation (Object.) node (Object.)
        late-failure (fn [& _] (throw (AssertionError. "failure changed while constructing payload")))
        selected-failure (fn [& args] (swap! events conj :failure) (swap! calls conj (vec args)) marker)
        late-proof (fn [record] (swap! events conj [:proof record])
                     (alter-var-root #'elision-failure (constantly selected-failure)) false)
        record (reify clojure.lang.ILookup
                 (valAt [_ key]
                   (swap! events conj [:record key])
                   (case key :erased? false
                     :erased-checks (do (alter-var-root #'elision-proof (constantly late-proof)) [:bounds])
                     :check-class check :operation operation :ir-node node
                     (throw (AssertionError. "unexpected validator record field"))))
                 (valAt [_ _ _] (throw (AssertionError. "defaulted record lookup"))))
        module (reify clojure.lang.ILookup
                 (valAt [_ key]
                   (swap! events conj [:module key])
                   (case key :source-path path
                     :profile (do (alter-var-root #'elision-failure (constantly late-failure)) profile)
                     :target target (throw (AssertionError. "unexpected module field"))))
                 (valAt [_ _ _] (throw (AssertionError. "defaulted module lookup"))))]
    (with-redefs [elision-proof (fn [& _] (throw (AssertionError. "proof selected before erased fields")))
                  elision-failure (fn [& _] (throw (AssertionError. "failure selected before proof helper")))]
      (is (identical? marker (optimization/validate-native-stage0-check-elision!
                              #'elision-failure #'elision-proof module record)))
      (is (= [[:module :source-path] [:record :erased?] [:record :erased-checks] [:proof record]]
             (subvec @events 0 4)))
      ;; Do not infer payload map expression order from its source spelling.
      (is (= :failure (last @events)))
      (is (= 1 (count @calls)))
      (let [facts (get-in @calls [0 2])]
        (doseq [[expected actual] [[path (get-in facts [:source-span :source])] [profile (:profile facts)]
                                   [target (:target facts)] [check (:check-class facts)]
                                   [operation (:operation facts)] [node (:ir-node facts)]]]
          (is (identical? expected actual)))))))

(deftest stage0-elision-retains-original-owner-exceptions-and-errors
  (doseq [sentinel [(Exception. "native elision exception") (Error. "native elision error")]]
    (with-redefs [elision-present (fn [& _] (throw sentinel))]
      (is (identical? sentinel
                      (try (optimization/native-stage0-check-elision-proof-present? #'elision-present {})
                           (catch Throwable error error)))))
    (with-redefs [elision-proof (fn [& _] (throw sentinel))]
      (is (identical? sentinel
                      (try (optimization/validate-native-stage0-check-elision!
                             #'elision-failure #'elision-proof elision-module {:erased? false})
                           (catch Throwable error error)))))
    (with-redefs [elision-proof (constantly false) elision-failure (fn [& _] (throw sentinel))]
      (is (identical? sentinel
                      (try (optimization/validate-native-stage0-check-elision!
                             #'elision-failure #'elision-proof elision-module {:erased? true})
                           (catch Throwable error error)))))))

(def ^:private performance-fields optimization/native-performance-required-claim-fields)
(defn- performance-present [value] (optimization/native-performance-present? value))
(defn- performance-missing [claim]
  (optimization/native-performance-missing-claim-fields #'performance-present #'performance-fields claim))
(defn- performance-fingerprint [claim] (optimization/native-performance-target-fingerprint claim))
(defn- performance-failure [id _ _ _ extra] (throw (ex-info id (assoc extra :id id))))
(def ^:private performance-claim
  {:claim-id :native/claim :profile :hosted :target :native/unverified-target
   :target-features #{:native/feature} :input-shape {:entrypoint :main} :layout :native/layout
   :safety-mode :safe :benchmark {:target-fingerprint :native/fingerprint}
   :semantic-proof #{:native/semantic} :safety-proof #{:native/safety}
   :artifacts #{:optimized-mir :proof-index :benchmark-report}})
(def ^:private performance-manifest
  {:profile :hosted :target :jvm :source-effects #{:io/write} :source-capabilities #{:io/stdout}})
(defn- validate-performance [performance claim]
  (optimization/validate-native-performance-claim!
    #'performance-missing #'performance-fingerprint #'performance-present #'performance-failure
    "native-performance.gravity" performance-manifest performance claim))

(deftest performance-owner-declares-complete-native-family-without-authority
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (empty? (ns-aliases 'gravity.optimization)))
  (let [publics (ns-publics 'gravity.optimization)
        contract (:gravity/namespace-contract (meta #'optimization/validate-native-performance-claim!))]
    (is (= 23 (count publics) (count (:public-api contract))))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name operation] publics]
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission]))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-verifier-certificates? contract)))
    (is (some #{:performance-certification} (:unsupported contract)))
    (is (some #{'PERF1} (:governing-contracts contract)))))

(deftest performance-presence-aliases-and-fingerprint-preserve-native-semantics
  (doseq [value [nil [] {} #{} '()]] (is (false? (performance-present value))))
  (doseq [value [false "" 0 :native (Object.) [nil]]] (is (true? (performance-present value))))
  (let [primary (Object.) fallback (Object.) input (Object.) extra (Object.)
        normalized (optimization/native-performance-normalize-claim
                     {:claim-id primary :claim fallback :input-domain input :extra extra})]
    (is (identical? primary (:claim-id normalized)))
    (is (identical? input (:input-shape normalized)))
    (is (identical? extra (:extra normalized)))
    (is (identical? fallback (optimization/native-performance-target-fingerprint
                              {:benchmark {:target-fingerprint false :environment-fingerprint fallback}})))
    (is (false? (optimization/native-performance-target-fingerprint
                  {:benchmark {:environment-fingerprint false}})))
    (is (= {:claim-id nil :input-shape nil} (optimization/native-performance-normalize-claim nil)))))

(deftest performance-missing-fields-reload-presence-after-each-getter
  (let [events (atom []) input (reify clojure.lang.ILookup
                              (valAt [_ key]
                                (swap! events conj [:get key])
                                (alter-var-root #'performance-present
                                                (constantly (fn [value] (swap! events conj [:late value]) true)))
                                key)
                              (valAt [_ _ _] (throw (AssertionError. "defaulted lookup"))))]
    (with-redefs [performance-fields [:first :second]
                  performance-present (fn [value] (swap! events conj [:selected value]) false)]
      (is (= [:first] (performance-missing input)))
      (is (= [[:get :first] [:selected :first] [:get :second] [:late :second]] @events)))))

(deftest performance-validation-rejects-each-real-rule-and-preserves-first-refusal
  (doseq [[id performance claim]
          [["PERF1-CLAIM" {} {:claim-id :native/incomplete}]
           ["PERF1-PROFILE" {} (assoc performance-claim :profile :native)]
           ["PERF1-TARGET" {} (assoc performance-claim :benchmark {:harness :native/test})]
           ["PERF1-EVIDENCE" {} (assoc performance-claim :semantic-proof #{})]
           ["PERF1-SAFETY" {:lost-safety-facts [:bounds]} performance-claim]
           ["PERF1-EFFECT" {:expanded-effects [:io/read]} performance-claim]
           ["PERF1-CAPABILITY" {:expanded-capabilities [:io/stdin]} performance-claim]
           ["PERF1-NUMERIC" {:numeric-mode-change {:source-mode :strict :optimized-mode :fast}} performance-claim]
           ["PERF1-VARIANT" {:generated-variants [{:variant-id :native/unguarded}]} performance-claim]]]
    (is (= id (try (validate-performance performance claim) nil
                   (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))))
  (let [ids (atom []) marker (Object.)
        invalid {:lost-safety-facts [:bounds] :expanded-effects [:io/read] :expanded-capabilities [:io/stdin]
                 :numeric-mode-change {:source-mode :strict :optimized-mode :fast}
                 :generated-variants [{:variant-id :bad}]}]
    (with-redefs [performance-failure (fn [id & _] (swap! ids conj id) marker)]
      ;; Returning failures never became an implicit early return in the seed.
      (is (= :complete (validate-performance invalid {:profile :native})))
      (is (= ["PERF1-CLAIM" "PERF1-PROFILE" "PERF1-TARGET" "PERF1-EVIDENCE" "PERF1-SAFETY"
              "PERF1-EFFECT" "PERF1-CAPABILITY" "PERF1-NUMERIC" "PERF1-VARIANT"] @ids))))
  (is (= :complete (validate-performance {} performance-claim))))

(deftest performance-failure-selects-owner-before-payload-and-retains-extra-precedence
  (let [marker (Object.) selected (atom nil) profile (Object.) source (Object.) extra (Object.)
        manifest (reify clojure.lang.ILookup
                   (valAt [_ key]
                     (alter-var-root #'elision-failure (constantly (fn [& _] (throw (AssertionError. "late owner")))))
                     (case key :profile profile :target :native/target))
                   (valAt [_ _ _] (throw (AssertionError. "defaulted lookup"))))]
    (with-redefs [elision-failure (fn [& args] (reset! selected args) marker)]
      (is (identical? marker
                      (optimization/native-performance-failure!
                        #'elision-failure "PERF1-EFFECT" source manifest {:claim-id :native/claim}
                        {:profile extra :source-span {:source source}})))
      (is (= "optimization hides or expands effects" (second @selected)))
      (is (identical? extra (:profile (nth @selected 2))))
      (is (identical? source (get-in (nth @selected 2) [:source-span :source]))))))

(deftest performance-validator-reloads-ports-and-retains-opaque-owner-errors
  (let [events (atom [])]
    (with-redefs [performance-missing
                  (fn [_] (swap! events conj :missing)
                    (alter-var-root #'performance-fingerprint
                                    (constantly (fn [_] (swap! events conj :late-fingerprint) :native/fingerprint))) [])
                  performance-fingerprint (fn [_] (throw (AssertionError. "early fingerprint")))]
      (is (= :complete (validate-performance {} performance-claim)))
      (is (= [:missing :late-fingerprint] @events))))
  (doseq [owner [#'performance-missing #'performance-fingerprint #'performance-present #'performance-failure]
          sentinel [(Exception. "opaque performance exception") (Error. "opaque performance error")]]
    (with-redefs-fn {owner (fn [& _] (throw sentinel))}
      #(is (identical? sentinel
                       (try (validate-performance {} (if (= owner #'performance-failure) {} performance-claim))
                            (catch Throwable error error)))))))
