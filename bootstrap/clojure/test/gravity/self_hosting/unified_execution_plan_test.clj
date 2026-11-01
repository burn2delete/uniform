(ns gravity.self-hosting.unified-execution-plan-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [gravity.self-hosting.execution-lanes :as lanes]
            [gravity.self-hosting.sh07-conformance-table :as conformance]
            [gravity.self-hosting.unified-execution-plan :as plan]))

(defn- error-id [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:id (ex-data e)))))

(defn- error-data [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(defn- passing-result []
  {:status :passed :test-count 1 :pass 1 :fail 0 :error 0
   :diagnostics [] :evidence [:selected-tests :dependency-closure :diagnostics :receipt]
   :mutation-detected? false :fresh-replayable? true
   :provenance-checked? true :source-authenticated? true
   :closure-verified? true :cache-decision :miss})

(def ^:private cached-context
  {:request-schema-version 15
   :adapter :gravity/sh07-to-c6-core-products-v16
   :checked-core-source-content-hash
   "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092"
   :checked-core-plan-semantic-hash
   "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd"
   :java-runtime-version "test-jvm"
   :clojure-version "1.12.5"})

(def ^:private cached-counters
  {:sh06-hits 1 :sh06-misses 0
   :core-hits 0 :core-misses 1
   :verification-hits 0 :verification-misses 1})

(defn- cached-raw-result
  ([plan-value] (cached-raw-result plan-value {}))
  ([plan-value overrides]
   (let [test-vars (count (get-in plan-value [:selection :selectors]))]
     (merge {:test test-vars :pass test-vars :fail 0 :error 0
             :artifact :gravity/sh07-cached-shard-result
             :shard (get-in plan-value [:executor :request :shard])
             :test-vars test-vars
             :cache-authoritative? false
             :fresh-authoritative-run-required? true
             :cache-context cached-context
             :elapsed-ms 7
             :cache cached-counters}
            overrides))))

(defn- cached-observation
  ([plan-value] (cached-observation plan-value (cached-raw-result plan-value)))
  ([plan-value raw]
   {:schema :gravity/unified-cache-observation-v1
    :result-contract-id
    (get-in plan-value [:executor :request :result-contract-id])
    :artifact (:artifact raw)
    :shard (:shard raw)
    :context (:cache-context raw)
    :counters (:cache raw)
    :elapsed-ms (:elapsed-ms raw)
    :cache-authoritative? (:cache-authoritative? raw)
    :fresh-authoritative-run-required?
    (:fresh-authoritative-run-required? raw)}))

(defn- cached-result
  ([plan-value] (cached-result plan-value {}))
  ([plan-value overrides]
   (let [raw (cached-raw-result plan-value (select-keys overrides
                                                        [:test :pass :fail :error
                                                         :artifact :shard :test-vars
                                                         :cache-authoritative?
                                                         :fresh-authoritative-run-required?
                                                         :cache-context :elapsed-ms :cache]))]
     (merge {:status :passed :test-count (:test-vars raw)
             :pass (:pass raw) :fail 0 :error 0
             :diagnostics []
             :evidence [:selected-tests :fixture-pairing :diagnostics :receipt]
             :mutation-detected? false :fresh-replayable? false
             :provenance-checked? true :source-authenticated? true
             :closure-verified? true :cache-decision :hit
             :artifacts [raw]
             :cache-observation (cached-observation plan-value raw)}
            (dissoc overrides :test :pass :fail :error :artifact :shard :test-vars
                    :cache-authoritative? :fresh-authoritative-run-required?
                    :cache-context :elapsed-ms :cache)))))

(def ^:private artifact-row-id
  "sh07-b15/keyword-lookup-zero-arity")
(def ^:private artifact-stage-order
  [:source :reader :syntax :macroexpansion :resolution :checked-core])

(defn- artifact-sha
  [digit]
  (str "sha256:" (apply str (repeat 64 (str digit)))))

(defn- test-repository-root
  []
  (loop [path (.getCanonicalFile (io/file (System/getProperty "user.dir")))]
    (if (.isFile (io/file path "contracts/verification-catalog.edn"))
      path
      (if-let [parent (.getParentFile path)]
        (recur parent)
        (throw (ex-info "test repository root is unavailable" {}))))))

(defn- compact-artifact-reference
  "Build the compact value an authenticated lane would return after its
  already-validated worker projection.  The test deliberately does not pass a
  projection through UEP; the lane trust-point tests cover that larger shape."
  [artifact-plan stage]
  (let [invocation (:artifact-invocation artifact-plan)
        binding (:binding invocation)
        root (.getCanonicalPath (test-repository-root))
        provenance {:actual-path (.getCanonicalPath
                                  (io/file (test-repository-root)
                                           (:gravity-path binding)))
                    :project-root-path root
                    :project-relative-path (:gravity-path binding)
                    :byte-count (:fixture-bytes binding)
                    :content-hash (:fixture-sha256 binding)}
        base {:schema lanes/artifact-reference-schema
              :operation :run-fast-artifact
              :base-plan-id (:base-plan-id invocation)
              :binding-id (:binding-id invocation)
              :row-id (:row-id invocation)
              :stage stage
              :projection-id (artifact-sha "a")
              :result-id (artifact-sha "b")
              :selected-artifact-id (artifact-sha (inc (.indexOf artifact-stage-order stage)))
              :source-content-hash (:fixture-sha256 binding)
              :source-provenance-id (lanes/artifact-reference-id provenance)
              :source-relative-path (:gravity-path binding)
              :source-provenance provenance
              :cache-decision :miss
              :execution-boundary {:mode :fast :isolation :same-jvm
                                   :isolated? false
                                   :acceptance-authority? false
                                   :fresh-authoritative-run-required? true}
              :authority {:authoritative? false
                          :ceiling :non-authoritative
                          :development-only? true
                          :nonclaims (:nonclaims artifact-plan)}
              :status :passed}]
    (assoc base :reference-id (lanes/artifact-reference-id base))))

(def ^:private cached-request-oracle
  {"sh07-cached-accepted"
   {:operation :cached-shard :shard "accepted"
    :result-contract-id :gravity/sh07-cached-shard-result-v1}
   "sh07-cached-contract"
   {:operation :cached-shard :shard "contract"
    :result-contract-id :gravity/sh07-cached-shard-result-v1}
   "sh07-cached-identity"
   {:operation :cached-shard :shard "identity"
    :result-contract-id :gravity/sh07-cached-shard-result-v1}
   "sh07-cached-rejected"
   {:operation :cached-shard :shard "rejected"
    :result-contract-id :gravity/sh07-cached-shard-result-v1}
   "sh07-cached-verification"
   {:operation :cached-shard :shard "verification"
    :result-contract-id :gravity/sh07-cached-shard-result-v1}})

(def ^:private invalid-positive-bound-values
  [0 -1 1.5 (inc (bigint Long/MAX_VALUE))])

(def ^:private invalid-non-negative-count-values
  [-1 1.5 (inc (bigint Long/MAX_VALUE))])

(deftest catalog-is-closed-and-compatibility-scoped
  (is (= :gravity/verification-catalog-v1 (:schema plan/catalog)))
  (is (= false (:complete? plan/catalog)))
  (is (= :compatibility-phase (get-in plan/catalog [:coverage :completeness])))
  (is (= [:exclusive :memory-heavy :normal]
         (sort (keys (:resource-classes plan/catalog)))))
  (is (every? #(false? (:replaces? %)) (vals (:routes plan/catalog))))
  (is (= (set (keys (:routes plan/catalog)))
         (set (get-in plan/catalog [:coverage :route-ids])))))

(deftest cached-executor-requests-are-closed-manifest-data
  (let [actual
        (into {}
              (keep (fn [[route-id route]]
                      (when-let [request (get-in route [:executor :request])]
                        [route-id request])))
              (:routes plan/catalog))]
    (is (= cached-request-oracle actual))
    (is (= 5 (count actual)))
    (is (= 5 (count (set (map :shard (vals actual))))))
    (is (= #{:adapter :bounded? :fresh-capable? :authority :request}
           (set (keys (get-in plan/catalog
                              [:routes "sh07-cached-accepted" :executor])))))
    (is (= #{:adapter :bounded? :fresh-capable? :authority}
           (set (keys (get-in plan/catalog
                              [:routes "sh07-b11-contract" :executor])))))
    (is (= #{:adapter :bounded? :fresh-capable? :authority}
           (set (keys (get-in plan/catalog
                              [:routes "sh07-authoritative" :executor])))))))

(deftest cached-executor-request-shape-and-shard-identity-fail-closed
  (let [extra (assoc-in plan/catalog
                        [:routes "sh07-cached-accepted" :executor :request
                         :extra]
                        true)
        blank (assoc-in plan/catalog
                        [:routes "sh07-cached-accepted" :executor :request :shard]
                        "")
        duplicate (assoc-in plan/catalog
                            [:routes "sh07-cached-contract" :executor :request :shard]
                            "accepted")]
    (is (= "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST-KEYS"
           (error-id #(plan/validate-catalog extra))))
    (is (= "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST"
           (error-id #(plan/validate-catalog blank))))
    (is (= "UEP-CATALOG-CACHED-EXECUTOR-REQUEST"
           (error-id #(plan/validate-catalog duplicate))))))

(deftest module-artifact-request-stage-is-closed-and-plan-provenanced
  (let [stages [:source :reader :syntax :macroexpansion :resolution
                :checked-core]
        module-path [:modules "sh07-checked-core" :artifact-request]]
    (doseq [stage stages]
      (is (nil? (error-id #(plan/validate-catalog
                            (assoc-in plan/catalog module-path stage))))
          (str "catalog accepts artifact stage " stage)))
    (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
      (is (= {:module-id "sh07-checked-core"
              :module-owner :gravity
              :stage :checked-core
              :source-path "bootstrap/gravity/src/gravity/checked_core.gravity"}
             (:artifact-request p)))
      (is (= #{:module-id :module-owner :stage :source-path}
             (set (keys (:artifact-request p))))))))

(deftest plan-artifact-request-tampering-fails-closed
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        identity-of (deref (var gravity.self-hosting.unified-execution-plan/identity-of))
        altered (assoc-in p [:artifact-request :stage] :reader)
        altered (assoc altered :plan-id
                       (identity-of (dissoc altered :plan-id)))]
    (is (= "UEP-PLAN-ARTIFACT"
           (error-id #(plan/validate-plan altered))))))

(deftest fixture-source-binding-is-closed-and-plan-bound
  (let [p (plan/plan {:route-id "sh07-conformance-table" :lane :fast})
        row-id "sh07-b15/keyword-lookup-zero-arity"
        binding (plan/fixture-source-binding p row-id :checked-core)
        source-binding (plan/fixture-source-binding p row-id :source)]
    (is (= #{:schema :plan-id :catalog-id :route-id :suite-id :row-id :status
             :module-id :module-owner :stage :gravity-path :qst-path
             :fixture-sha256 :fixture-bytes :authenticated? :binding-id}
           (set (keys binding))))
    (is (= :gravity/fixture-source-binding-v1 (:schema binding)))
    (is (= (:plan-id p) (:plan-id binding)))
    (is (= (:catalog-id p) (:catalog-id binding)))
    (is (= "sh07-conformance-table" (:route-id binding)))
    (is (= "sh07-b15" (:suite-id binding)))
    (is (= row-id (:row-id binding)))
    (is (= :rejected (:status binding)))
    (is (= "sh07-checked-core" (:module-id binding)))
    (is (= :gravity (:module-owner binding)))
    (is (= :checked-core (:stage binding)))
    (is (= "bootstrap/clojure/fixtures/self-hosting/sh-07-b15/rejected/keyword-lookup-zero-arity.gravity"
           (:gravity-path binding)))
    (is (= "bootstrap/clojure/fixtures/self-hosting/sh-07-b15/rejected/keyword-lookup-zero-arity.qst"
           (:qst-path binding)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:fixture-sha256 binding)))
    (is (pos? (:fixture-bytes binding)))
    (is (<= (:fixture-bytes binding) conformance/maximum-fixture-peer-bytes))
    (is (true? (:authenticated? binding)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:binding-id binding)))
    ;; The plan identity is part of the binding preimage, while an earlier
    ;; requested stage receives a distinct authenticated identity.
    (is (not= (:binding-id binding) (:binding-id source-binding)))
    (is (= :source (:stage source-binding)))
    (is (= binding (plan/fixture-source-binding p row-id :checked-core)))))

(deftest fixture-source-binding-rejects-unknown-rows-and-stages
  (let [all-fixtures (plan/plan {:route-id "sh07-conformance-table" :lane :fast})
        partial (plan/plan {:route-id "sh07-parallel-regression-b" :lane :fast})]
    (is (= "UEP-FIXTURE-BINDING-ROW"
           (error-id #(plan/fixture-source-binding
                       all-fixtures "sh07-b15/not-reviewed" :source))))
    (is (= "UEP-FIXTURE-BINDING-ROW"
           (error-id #(plan/fixture-source-binding
                       partial "sh07-b15/keyword-lookup-zero-arity" :source))))
    (is (= "UEP-FIXTURE-BINDING-STAGE"
           (error-id #(plan/fixture-source-binding
                       all-fixtures "sh07-b15/keyword-lookup-zero-arity" :emission))))
    (is (= "UEP-FIXTURE-BINDING-ROW"
           (error-id #(plan/fixture-source-binding
                       all-fixtures nil :source))))))

(deftest fixture-source-binding-revalidates-canonical-conformance-table
  (let [p (plan/plan {:route-id "sh07-conformance-table" :lane :fast})
        calls (atom 0)
        original conformance/validate-table]
    (with-redefs [conformance/validate-table
                  (fn [table]
                    (swap! calls inc)
                    (original table))]
      (plan/fixture-source-binding
       p "sh07-b15/keyword-lookup-zero-arity" :checked-core))
    ;; One validation occurs while validating the plan's catalog and one at
    ;; the binding boundary immediately before downstream artifact use.
    (is (= 2 @calls))))

(deftest fixture-source-binding-id-includes-pair-content-identity
  (let [p (plan/plan {:route-id "sh07-conformance-table" :lane :fast})
        row-id "sh07-b15/keyword-lookup-zero-arity"
        baseline (plan/fixture-source-binding p row-id :checked-core)
        original conformance/fixture-pair-binding
        replacement (str "sha256:" (apply str (repeat 64 "0")))
        changed (with-redefs [conformance/fixture-pair-binding
                              (fn [table id]
                                (assoc (original table id) :sha256 replacement))]
                  (plan/fixture-source-binding p row-id :checked-core))]
    (is (re-matches #"sha256:[0-9a-f]{64}" (:fixture-sha256 baseline)))
    (is (= replacement (:fixture-sha256 changed)))
    (is (not= (:binding-id baseline) (:binding-id changed)))))

(deftest plans-are-deterministic-and-declarative
  (let [request {:route-id "sh07-b8-regression" :lane :fast}
        first-plan (plan/plan request)
        second-plan (plan/plan request)]
    (is (= first-plan second-plan))
    (is (= :fast (:lane first-plan)))
    (is (= :non-authoritative (:authority first-plan)))
    (is (false? (:authoritative? first-plan)))
    (is (= :eligible (get-in first-plan [:cache-policy :mode])))
    (is (= :warm-bounded-process (:execution-mode first-plan)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:plan-id first-plan)))
    (is (= true (get-in first-plan [:dependency-closure :complete?])))
    (is (= :pre-and-post-execution (get-in first-plan [:guards :source-authentication])))))

(deftest fresh-plan-is-acceptance-capable-but-not-proof
  (let [fresh (plan/plan {:route-id "sh07-authoritative" :lane :fresh})]
    (is (= :acceptance-capable (:authority fresh)))
    (is (false? (:authoritative? fresh)))
    (is (= :forbidden (get-in fresh [:cache-policy :mode])))
    (is (= :fresh-isolated-process (:execution-mode fresh)))
    (is (= true (:fresh-authoritative-gate-required? fresh)))
    (is (= :acceptance-only (:authority-ceiling fresh)))
    (is (= "UEP-REQUEST-CACHE"
           (error-id #(plan/plan {:route-id "sh07-authoritative"
                                  :lane :fresh
                                  :cache-policy {:mode :eligible}}))))))

(deftest unknown-and-tampered-inputs-fail-closed
  (is (= "UEP-ROUTE-UNKNOWN"
         (error-id #(plan/plan {:route-id "unknown" :lane :fast}))))
  (is (= "UEP-REQUEST-KEYS"
         (error-id #(plan/plan {:route-id "sh07-b8-regression" :lane :fast
                                :extra true}))))
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-PLAN-ID"
           (error-id #(plan/validate-plan (assoc p :lane :fresh)))))))

(deftest catalog-rejects-unbounded-executors-and-incomplete-cache-keys
  (let [fast (assoc-in plan/catalog [:routes "sh07-b8-regression" :executor :bounded?] false)
        fresh (assoc-in plan/catalog [:routes "sh07-authoritative" :executor :bounded?] false)
        cache (assoc-in plan/catalog [:routes "sh07-b8-regression" :cache-policy :key-inputs]
                        [:route-id])]
    (is (= "UEP-CATALOG-ROUTE-FAST-POLICY"
           (error-id #(plan/validate-catalog fast))))
    (is (= "UEP-CATALOG-ROUTE-FRESH-POLICY"
           (error-id #(plan/validate-catalog fresh))))
    (is (= "UEP-CATALOG-ROUTE-CACHE"
           (error-id #(plan/validate-catalog cache))))))

(deftest catalog-resource-bounds-reject-invalid-numeric-values
  (doseq [key [:max-concurrency :timeout-ms :max-output-bytes]
          value invalid-positive-bound-values]
    (is (= "UEP-CATALOG-RESOURCE-BOUND"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:resource-classes :normal key] value))))
        (str "catalog resource bound " key "=" value))))

(deftest request-resource-overrides-reject-invalid-numeric-values
  (let [base-policy {:resource-class :memory-heavy
                     :timeout-ms 1000
                     :max-output-bytes 65536}]
    (doseq [key [:timeout-ms :max-output-bytes]
            value invalid-positive-bound-values]
      (is (= "UEP-REQUEST-RESOURCE"
             (error-id #(plan/plan
                         {:route-id "sh07-b8-regression"
                          :lane :fast
                          :resource-policy (assoc base-policy key value)})))
          (str "request resource override " key "=" value)))))

(deftest catalog-coverage-counts-reject-invalid-numeric-values
  (doseq [value invalid-non-negative-count-values]
    (is (= "UEP-CATALOG-COVERAGE-ACCOUNTING"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:coverage :accounting :route-count]
                                 value))))
        (str "catalog coverage route-count=" value))))

(deftest numeric-bound-diagnostics-retain-path-and-value
  (let [value (inc (bigint Long/MAX_VALUE))
        catalog-error (error-data #(plan/validate-catalog
                                    (assoc-in plan/catalog
                                              [:resource-classes :normal :timeout-ms]
                                              value)))
        coverage-error (error-data #(plan/validate-catalog
                                     (assoc-in plan/catalog
                                               [:coverage :accounting :route-count]
                                               value)))
        request-error (error-data #(plan/plan
                                    {:route-id "sh07-b8-regression"
                                     :lane :fast
                                     :resource-policy
                                     {:resource-class :memory-heavy
                                      :timeout-ms value
                                      :max-output-bytes 65536}}))]
    (is (= {:id "UEP-CATALOG-RESOURCE-BOUND"
            :path [:resource-classes :normal :timeout-ms]
            :value value}
           (select-keys catalog-error [:id :path :value])))
    (is (= {:id "UEP-CATALOG-COVERAGE-ACCOUNTING"
            :path [:coverage :accounting :route-count]
            :value value}
           (select-keys coverage-error [:id :path :value])))
    (is (= {:id "UEP-REQUEST-RESOURCE"
            :path [:request :resource-policy :timeout-ms]
            :value value}
           (select-keys request-error [:id :path :value])))))

(deftest plan-rejects-invalid-recomputed-resource-bounds
  (let [identity-of (deref (var gravity.self-hosting.unified-execution-plan/identity-of))
        p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (doseq [k [:timeout-ms :max-output-bytes]
            value invalid-positive-bound-values]
      (let [altered (assoc-in p [:resource-policy k] value)
            altered (assoc altered :plan-id (identity-of (dissoc altered :plan-id)))]
        (is (= "UEP-PLAN-RESOURCE"
               (error-id #(plan/validate-plan altered)))
            (str "plan resource bound " k "=" value))))))

(deftest receipt-generation-preserves-integrity-and-authority-ceiling
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        receipt (plan/execute-plan p (fn [_] (passing-result)))]
    (is (= :gravity/unified-execution-receipt-v1 (:schema receipt)))
    (is (= :passed (:status receipt)))
    (is (= false (:authoritative? receipt)))
    (is (= :non-authoritative (:authority receipt)))
    (is (= :miss (get-in receipt [:cache :decision])))
    (is (= true (get-in receipt [:integrity :source-authenticated?])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:receipt-id receipt)))))

(deftest ordinary-receipt-shape-and-golden-identity-remain-byte-stable
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        receipt (plan/execute-plan p (constantly (passing-result)))]
    (is (= #{:schema :version :plan-id :route-id :lane :authority
             :authoritative? :authority-ceiling :summary :selection
             :closure-identity :cache :execution :status :diagnostics :evidence
             :integrity :fresh-replayable? :required-evidence :nonclaims
             :fresh-authoritative-gate-required? :receipt-id}
           (set (keys receipt))))
    ;; The additive iteration declaration changes the complete catalog identity.
    ;; Selection metadata stays authenticated; no identity-input exclusion.
    (is (= "sha256:aadaa54e4cacd4ecdd31d1594c3da011f16fe2e115558056467abd23474b9afb"
           (:receipt-id receipt)))
    (is (= receipt (plan/verify-receipt p receipt)))))

(deftest artifact-plans-bind-earliest-requested-stage-and-evidence
  (let [base (plan/plan {:route-id "sh07-conformance-table" :lane :fast})]
    (doseq [stage artifact-stage-order]
      (let [artifact (plan/artifact-plan
                      {:route-id "sh07-conformance-table"
                       :lane :fast
                       :artifact-invocation {:row-id artifact-row-id
                                             :stage stage}})]
        (is (= (:plan-id base)
               (:base-plan-id (:artifact-invocation artifact))))
        (is (= stage (get-in artifact [:artifact-invocation :stage])))
        (is (= [:source-identity :diagnostics :receipt]
               (:required-evidence artifact)))
        (is (= artifact (plan/validate-plan artifact)))))))

(deftest artifact-plan-rejects-fresh-lane
  (is (= "UEP-ARTIFACT-LANE"
         (error-id #(plan/artifact-plan
                     {:route-id "sh07-authoritative"
                      :lane :fresh
                      :artifact-invocation {:row-id artifact-row-id
                                            :stage :source}})))))

(deftest artifact-plan-preidentity-envelope-is-bounded
  (let [artifact (plan/artifact-plan
                  {:route-id "sh07-conformance-table"
                   :lane :fast
                   :artifact-invocation {:row-id artifact-row-id
                                         :stage :source}})
        malformed (assoc-in artifact [:artifact-invocation :binding :schema]
                            (iterate inc 0))]
    (is (= "UEP-ARTIFACT-BINDING"
           (error-id #(plan/validate-plan malformed))))))

(deftest artifact-execute-plan-is-closed-and-receipt-bound
  (let [artifact (plan/artifact-plan
                  {:route-id "sh07-conformance-table"
                   :lane :fast
                   :artifact-invocation {:row-id artifact-row-id
                                         :stage :source}})
        reference (compact-artifact-reference artifact :source)
        callback-calls (atom [])
        receipt (with-redefs [lanes/run-fast-artifact!
                              (fn [base row stage]
                                (swap! callback-calls conj [(:plan-id base) row stage])
                                {:status :passed
                                 :diagnostics []
                                 :evidence []
                                 :mutation-detected? false
                                 :fresh-replayable? false
                                 :provenance-checked? true
                                 :source-authenticated? true
                                 :closure-verified? true
                                 :cache-decision :miss
                                 :artifact-reference reference})]
                  (plan/execute-plan artifact))]
    (is (= [(:base-plan-id (:artifact-invocation artifact))
            artifact-row-id :source]
           (first @callback-calls)))
    (is (= :passed (:status receipt)))
    (is (= reference (get-in receipt [:summary :artifact])))
    (is (= [:source-identity :diagnostics :receipt]
           (:required-evidence receipt)))
    (is (= receipt (plan/verify-receipt artifact receipt)))
    (is (not (contains? receipt :projection)))
    (is (not (contains? receipt :artifact-reference)))))

(deftest artifact-two-arity-injected-seam-and-diagnostics-fail-closed
  (let [artifact (plan/artifact-plan
                  {:route-id "sh07-conformance-table"
                   :lane :fast
                   :artifact-invocation {:row-id artifact-row-id
                                         :stage :source}})]
    (is (= "UEP-ARTIFACT-EXECUTOR"
           (error-id #(plan/execute-plan artifact
                                          (constantly {:status :failed})))))
    (is (= "UEP-ARTIFACT-DIAGNOSTICS"
           (error-id #(with-redefs [lanes/run-fast-artifact!
                                    (fn [_ _ _]
                                      {:status :failed
                                       :diagnostics [{:id "x" :message "ok"
                                                      :extra true}]})]
                            (plan/execute-plan artifact)))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(with-redefs [lanes/run-fast-artifact!
                                    (fn [_ _ _]
                                      {:status :passed
                                       :diagnostics []
                                       :mutation-detected? false
                                       :fresh-replayable? false
                                       :provenance-checked? true
                                       :source-authenticated? true
                                       :closure-verified? true
                                       :cache-decision :miss})]
                            (plan/execute-plan artifact)))))
  ))

(deftest artifact-receipt-and-plan-rebinding-fail-closed
  (let [artifact (plan/artifact-plan
                  {:route-id "sh07-conformance-table"
                   :lane :fast
                   :artifact-invocation {:row-id artifact-row-id
                                         :stage :source}})
        reference (compact-artifact-reference artifact :source)
        receipt (with-redefs [lanes/run-fast-artifact!
                              (fn [_ _ _]
                                {:status :passed :diagnostics []
                                 :mutation-detected? false :fresh-replayable? false
                                 :provenance-checked? true :source-authenticated? true
                                 :closure-verified? true :cache-decision :miss
                                 :artifact-reference reference})]
                  (plan/execute-plan artifact))
        changed-reference (assoc-in receipt [:summary :artifact :source-content-hash]
                                     (artifact-sha "d"))
        rebound-reference
        (update-in changed-reference [:summary :artifact]
                   (fn [value]
                     (assoc value :reference-id
                            (lanes/artifact-reference-id
                             (dissoc value :reference-id)))))
        rebound-reference
        (assoc rebound-reference
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc rebound-reference :execution :receipt-id)))
        tampered-root
        (-> receipt
            (assoc-in [:summary :artifact :source-provenance :project-root-path]
                      "/wrong-root")
            (update-in [:summary :artifact]
                       (fn [value]
                         (assoc value :reference-id
                                (lanes/artifact-reference-id
                                 (dissoc value :reference-id))))))
        tampered-root
        (assoc tampered-root
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc tampered-root :execution :receipt-id)))
        tampered-actual
        (-> receipt
            (assoc-in [:summary :artifact :source-provenance :actual-path]
                      "/wrong-source")
            (update-in [:summary :artifact]
                       (fn [value]
                         (assoc value :reference-id
                                (lanes/artifact-reference-id
                                 (dissoc value :reference-id))))))
        tampered-actual
        (assoc tampered-actual
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc tampered-actual :execution :receipt-id)))
        tampered-provenance-id
        (-> receipt
            (assoc-in [:summary :artifact :source-provenance-id]
                      (artifact-sha "e"))
            (update-in [:summary :artifact]
                       (fn [value]
                         (assoc value :reference-id
                                (lanes/artifact-reference-id
                                 (dissoc value :reference-id))))))
        tampered-provenance-id
        (assoc tampered-provenance-id
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc tampered-provenance-id :execution :receipt-id)))
        tampered-authority
        (-> receipt
            (assoc-in [:summary :artifact :authority :authoritative?] true)
            (update-in [:summary :artifact]
                       (fn [value]
                         (assoc value :reference-id
                                (lanes/artifact-reference-id
                                 (dissoc value :reference-id))))))
        tampered-authority
        (assoc tampered-authority
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc tampered-authority :execution :receipt-id)))
        tampered-boundary
        (-> receipt
            (assoc-in [:summary :artifact :execution-boundary :isolated?] true)
            (update-in [:summary :artifact]
                       (fn [value]
                         (assoc value :reference-id
                                (lanes/artifact-reference-id
                                 (dissoc value :reference-id))))))
        tampered-boundary
        (assoc tampered-boundary
               :receipt-id
               ((deref (var gravity.self-hosting.unified-execution-plan/identity-of))
                (dissoc tampered-boundary :execution :receipt-id)))
        changed-plan (plan/artifact-plan
                      {:route-id "sh07-conformance-table" :lane :fast
                       :artifact-invocation {:row-id "sh07-b15/basic-keyword-map-lookup"
                                             :stage :source}})]
    (is (= "UEP-ARTIFACT-REFERENCE-ID"
           (error-id #(plan/verify-receipt changed-reference))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact rebound-reference))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact tampered-root))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact tampered-actual))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact tampered-provenance-id))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact tampered-authority))))
    (is (= "UEP-ARTIFACT-REFERENCE"
           (error-id #(plan/verify-receipt artifact tampered-boundary))))
    (is (= "UEP-RECEIPT-PLAN"
           (error-id #(plan/verify-receipt changed-plan receipt))))))

(deftest cache-observation-is-authenticated-closed-and-nonauthoritative
  (let [p (plan/plan {:route-id "sh07-cached-accepted" :lane :fast})
        valid (cached-result p)
        receipt (plan/execute-plan p (constantly valid))]
    (is (= (:cache-observation valid)
           (get-in receipt [:cache :observation])))
    (is (= #{:schema :result-contract-id :artifact :shard :context :counters
             :elapsed-ms :cache-authoritative?
             :fresh-authoritative-run-required?}
           (set (keys (:cache-observation valid)))))
    (is (false? (get-in valid [:cache-observation :cache-authoritative?])))
    (is (true? (get-in valid
                       [:cache-observation :fresh-authoritative-run-required?])))
    (is (= "accepted" (get-in valid [:cache-observation :shard])))
    (is (= :gravity/sh07-cached-shard-result-v1
           (get-in valid [:cache-observation :result-contract-id])))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
                       (constantly
                        (assoc (passing-result)
                               :cache-observation
                               (:cache-observation valid)))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       (plan/plan {:route-id "sh07-authoritative" :lane :fresh})
                       (constantly
                        (assoc (passing-result)
                               :cache-decision :forbidden
                               :cache-observation
                               (:cache-observation valid)))))))
    (let [tightened (plan/plan
                     {:route-id "sh07-cached-accepted" :lane :fast
                      :resource-policy {:resource-class :memory-heavy
                                        :timeout-ms 1000
                                        :max-output-bytes 262144}})]
      (is (= "UEP-RESULT-CACHE-OBSERVATION"
             (error-id #(plan/execute-plan
                         tightened
                         (constantly (cached-result tightened)))))))))

(deftest cache-observation-requires-raw-artifact-and-exact-bounds
  (let [p (plan/plan {:route-id "sh07-cached-accepted" :lane :fast})
        valid (cached-result p)]
    (is (= "UEP-RESULT-CACHE-OBSERVATION-REQUIRED"
           (error-id #(plan/execute-plan
                       p (constantly (dissoc valid :cache-observation))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       p (constantly (assoc (dissoc valid :artifacts)
                                            :cache-observation
                                            (:cache-observation valid)))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION-REQUIRED"
           (error-id #(plan/execute-plan
                       p (constantly (assoc (dissoc valid :artifacts
                                                     :cache-observation)
                                            :status :passed))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION-KEYS"
           (error-id #(plan/execute-plan
                       p (constantly (assoc valid :cache-observation
                                             (assoc (:cache-observation valid)
                                                    :extra true)))))))
    (doseq [value invalid-non-negative-count-values]
      (is (= "UEP-RESULT-CACHE-OBSERVATION-COUNTER"
             (error-id #(plan/execute-plan
                         p (constantly
                            (assoc-in valid
                                      [:cache-observation :counters :core-hits]
                                      value)))))
          (str "cache observation counter core-hits=" value)))
    (is (= "UEP-RESULT-CACHE-OBSERVATION-CONTEXT-KEYS"
           (error-id #(plan/execute-plan
                       p (constantly
                          (assoc-in valid
                                    [:cache-observation :context :extra]
                                    true))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION-AUTHORITY"
           (error-id #(plan/execute-plan
                       p (constantly
                          (assoc-in valid
                                    [:cache-observation :cache-authoritative?]
                                    true))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       p (constantly
                          (assoc-in valid
                                    [:cache-observation :result-contract-id]
                                    :wrong-contract))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       p (constantly
                          (assoc-in valid [:cache-observation :shard]
                                    "contract"))))))
    (is (= "UEP-RESULT-CACHE-OBSERVATION"
           (error-id #(plan/execute-plan
                       p (constantly
                          (update valid :artifacts conj (first (:artifacts valid))))))))))

(deftest cache-observation-receipt-identity-excludes-counters-and-time
  (let [p (plan/plan {:route-id "sh07-cached-accepted" :lane :fast})
        first-result (cached-result p {:elapsed-ms 7
                                       :cache (assoc cached-counters
                                                     :sh06-hits 1)})
        second-result (cached-result p {:elapsed-ms 700
                                        :cache (assoc cached-counters
                                                      :sh06-hits 2
                                                      :core-hits 3)})
        changed-context (cached-result
                         p {:cache-context (assoc cached-context
                                                   :java-runtime-version "other")})
        changed-decision (cached-result
                          p {:cache (assoc cached-counters
                                            :sh06-hits 0)
                             :cache-decision :miss})
        other-shard-plan (plan/plan {:route-id "sh07-cached-contract"
                                     :lane :fast})
        first-receipt (plan/execute-plan p (constantly first-result))
        second-receipt (plan/execute-plan p (constantly second-result))
        changed-context-receipt
        (plan/execute-plan p (constantly changed-context))
        changed-decision-receipt
        (plan/execute-plan p (constantly changed-decision))
        other-shard-receipt
        (plan/execute-plan other-shard-plan
                           (constantly (cached-result other-shard-plan)))]
    (is (= (:receipt-id first-receipt) (:receipt-id second-receipt)))
    (is (not= (:receipt-id first-receipt)
              (:receipt-id changed-context-receipt)))
    (is (not= (:receipt-id first-receipt)
              (:receipt-id changed-decision-receipt)))
    (is (not= (:receipt-id first-receipt)
              (:receipt-id other-shard-receipt)))
    (is (= :hit (get-in first-receipt [:cache :decision])))
    (is (= 700 (get-in second-receipt [:cache :observation :elapsed-ms])))
    (is (= 3 (get-in second-receipt
                     [:cache :observation :counters :core-hits])))))

(deftest generic-and-fresh-receipts-retain-cache-shape
  (let [fast (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        fresh (plan/plan {:route-id "sh07-authoritative" :lane :fresh})
        generic-receipt (plan/execute-plan fast (constantly (passing-result)))
        fresh-receipt
        (plan/execute-plan
         fresh
         (constantly {:status :blocked :cache-decision :forbidden}))]
    (is (= #{:mode :decision :authoritative?}
           (set (keys (:cache generic-receipt)))))
    (is (not (contains? (:cache generic-receipt) :observation)))
    (is (= #{:mode :decision :authoritative?}
           (set (keys (:cache fresh-receipt)))))
    (is (not (contains? (:cache fresh-receipt) :observation)))
    (is (= :forbidden (get-in fresh-receipt [:cache :decision])))
    (is (false? (:authoritative? fresh-receipt)))))

(deftest malformed-result-and-mutation-never-pass
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-RESULT-STATUS"
           (error-id #(plan/execute-plan p (fn [_] {:status :unknown})))))
    (is (= "UEP-RESULT-MUTATION"
           (error-id #(plan/execute-plan p
                                         (fn [_]
                                           (assoc (passing-result)
                                          :mutation-detected? true))))))))

(deftest result-counts-reject-invalid-numeric-values
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (doseq [key [:test-count :pass :fail :error]
            value invalid-non-negative-count-values]
      (is (= "UEP-RESULT-COUNT"
             (error-id #(plan/execute-plan
                         p (constantly (assoc (passing-result) key value)))))
          (str "result count " key "=" value)))))

(deftest passing-result-must-list-required-evidence
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-RESULT-EVIDENCE"
           (error-id #(plan/execute-plan p
                                         (fn [_]
                                           (assoc (passing-result)
                                                  :evidence [:selected-tests]))))))))

(deftest recomputed-plan-id-cannot-authorize-tampered-closure
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        altered (update-in p [:dependency-closure :paths] conj "unreviewed.txt")
        identity-of (deref (var gravity.self-hosting.unified-execution-plan/identity-of))
        altered (assoc altered :plan-id (identity-of (dissoc altered :plan-id)))]
    (is (= "UEP-PLAN-CLOSURE"
           (error-id #(plan/validate-plan altered))))))

(deftest interrupted-execution-is-rethrown-and-flag-restored
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        interrupted? (try
                       (plan/execute-plan p
                                          (fn [_] (throw (InterruptedException. "stop"))))
                       false
                       (catch InterruptedException _ true)
                       (finally (Thread/interrupted)))]
    (is interrupted?)))

(deftest current-jvm-adapter-requires-explicit-resource-context
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (plan/execute-plan p)]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (false? (get-in receipt [:integrity :source-authenticated?])))))

(deftest current-jvm-adapter-rejects-unproven-bounds
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (binding [plan/*resource-lease* {:bounded-executor? true
                                                   :timeout-enforced? false
                                                   :deadline-ns Long/MAX_VALUE}]
                  (plan/execute-plan p))]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (not (true? (get-in receipt [:integrity :closure-verified?]))))))

(deftest current-jvm-adapter-rejects-arbitrary-lease
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (binding [plan/*resource-lease* {:lease-id "unreviewed"}]
                  (plan/execute-plan p))]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))))

(deftest closure-snapshot-enforces-entry-bound-before-retention
  (let [root (java.nio.file.Files/createTempDirectory "uep-snapshot-" (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve root "oversize")
        snapshot (deref (var gravity.self-hosting.unified-execution-plan/closure-snapshot))
        max-bytes (var gravity.self-hosting.unified-execution-plan/max-snapshot-file-bytes)
        root-var (var gravity.self-hosting.unified-execution-plan/snapshot-root)]
    (spit (.toFile path) "0123456789")
    (try
      (is (= "UEP-SNAPSHOT-BOUND"
             (error-id #(with-redefs-fn {root-var (fn [] root)
                                         max-bytes 4}
                          (fn [] (snapshot {:dependency-closure {:paths ["oversize"]}}))))))
      (finally (java.nio.file.Files/deleteIfExists path)
               (java.nio.file.Files/deleteIfExists root)))))

(deftest fresh-executor-requires-explicit-admission-before-isolated-run
  (let [p (plan/plan {:route-id "sh07-authoritative" :lane :fresh})
        receipt (plan/execute-plan p)]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (= :forbidden (get-in receipt [:cache :decision])))
    (is (false? (:authoritative? receipt)))))

(deftest compatibility-routes-expose-existing-selector-shapes
  (testing "all catalog routes are deterministic and non-empty"
    (doseq [route-id (plan/route-names)]
      (let [route (plan/route route-id)]
        (is (= route-id (:id route)))
        (is (seq (get-in route [:selection :selectors])))
        (is (seq (:required-evidence route)))))))

(deftest manifest-projections-are-derived-and-complete
  (let [projection (plan/validate-manifest-projections!)
        accounting (:coverage projection)]
    (is (= :gravity/verification-manifest-projection-v1
           (:schema projection)))
    (is (= :non-authoritative (:authority projection)))
    (is (false? (:authoritative? projection)))
    (is (= (plan/manifest-id) (:manifest-id projection)))
    ;; The route projection is a generated view, not a second catalog owner.
    (is (= (:routes plan/catalog) (:routes projection)))
    (is (= (:routes plan/catalog)
           (plan/route-catalog plan/catalog)))
    (is (= ["sh07-b8-regression"]
           (get-in projection [:legacy-routes :impact-test-planner])))
    (is (= ["sh07-b13-accepted" "sh07-conformance-table"]
           (get-in projection [:legacy-routes :iteration-cache-runner])))
    (is (= ["sh07-b13-accepted" "sh07-cached-accepted"
            "sh07-cached-contract" "sh07-cached-identity"
            "sh07-cached-rejected" "sh07-cached-verification"
            "sh07-conformance-table"]
           (get-in projection [:legacy-routes :cached-shard-runner])))
    (is (= ["sh07-b10-regression" "sh07-conformance-table"
            "sh07-parallel-accepted"
            "sh07-parallel-b12-contract-accepted"
            "sh07-parallel-b12-identity-verification"
            "sh07-parallel-b12-rejected-regression"
            "sh07-parallel-identity-lineage"
            "sh07-parallel-regression-a"
            "sh07-parallel-regression-b"
            "sh07-parallel-rejected"
            "sh07-parallel-verification"]
           (get-in projection [:legacy-routes :parallel-shard-runner])))
    (is (= (set (mapcat :replacement-for (vals (:routes plan/catalog))))
           (set (keys (:legacy-routes projection)))))
    (is (= {:module-id "sh07-checked-core" :owner :gravity}
           (get-in projection [:module-ownership
                               "bootstrap/gravity/src/gravity/checked_core.gravity"])))
    (is (= (set (keys (:routes plan/catalog)))
           (set (keys (:dependency-closures projection)))))
    (is (= 21 (:route-count accounting)))
    (is (= 1 (:module-count accounting)))
    (is (= 2 (:suite-count accounting)))
    (is (= 109 (:selector-count accounting)))
    (is (= 108 (:exact-var-selector-count accounting)))
    (is (= 1 (:proof-contract-selector-count accounting)))
    (is (= 14 (:fixture-suite-count accounting)))
    (is (= (get-in plan/catalog [:conformance :coverage :accepted-count])
           (:accepted-fixture-count accounting)))
    (is (= (get-in plan/catalog [:conformance :coverage :rejected-count])
           (:rejected-fixture-count accounting)))
    (is (= (get-in plan/catalog [:conformance :coverage :pair-count])
           (:fixture-pair-count accounting)))))

(deftest namespace-authority-projection-is-fast-route-owned-and-closed
  (let [projection (plan/namespace-authority-projection)
        entries (:entries projection)
        by-namespace (into {} (map (juxt :namespace identity) entries))
        b10 (get by-namespace
                 'gravity.self-hosting.sh07-match-lowering-test)
        b11 (get by-namespace
                 'gravity.self-hosting.sh07-vector-pattern-test)
        b13 (get by-namespace
                 'gravity.self-hosting.sh07-module-fragment-test)
        fresh (get by-namespace
                   'gravity.self-hosting.sh07-authoritative-runner)]
    (is (= plan/namespace-authority-schema (:schema projection)))
    (is (= :non-authoritative (:authority projection)))
    (is (false? (:authoritative? projection)))
    (is (= (plan/manifest-id) (:manifest-id projection)))
    (is (nil? fresh))
    (is (= ["sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope"
            "sh07-b11-gravity-contract-names-l7"]
           (subvec (:selectors b11) 0 2)))
    (is (= ["sh07-b11-contract" "sh07-parallel-accepted"
            "sh07-parallel-identity-lineage" "sh07-parallel-rejected"
            "sh07-parallel-verification"]
           (vec (sort (:route-ids b11)))))
    ;; Compatibility routes intentionally overlap; selectors are a stable
    ;; union, while route ids preserve every contributing route owner.
    (is (= 21 (count (:selectors b10))))
    (is (= ["sh07-b10-regression" "sh07-parallel-regression-a"
            "sh07-parallel-regression-b"]
           (:route-ids b10)))
    (is (= ["sh07-b13-accepted" "sh07-cached-accepted"
            "sh07-cached-contract" "sh07-cached-identity"
            "sh07-cached-rejected" "sh07-cached-verification"]
           (:route-ids b13)))
    (is (every? #(= % (vec (sort %))) (map :route-ids entries)))
    (is (every? #(= (count %) (count (distinct %)))
                (map :selectors entries)))
    (is (= projection
           (plan/validate-namespace-authority-projection plan/catalog projection)))
    (let [identity-of (deref #'gravity.self-hosting.unified-execution-plan/identity-of)
          forged-value (update-in projection [:entries 0 :selectors]
                                  conj "forged-selector")
          forged (assoc forged-value
                        :projection-id (identity-of (dissoc forged-value :projection-id)))]
      ;; A matching manifest id and recomputed projection id do not authorize
      ;; a forged entry: the consumer compares against the deterministic build.
      (is (= "UEP-NAMESPACE-AUTHORITY-MANIFEST"
             (error-id #(plan/validate-namespace-authority-projection
                         plan/catalog forged))))
      (let [bad-route-value (assoc-in projection [:entries 0 :route-ids] [42])
            bad-route (assoc bad-route-value
                             :projection-id
                             (identity-of (dissoc bad-route-value :projection-id)))]
        (is (= "UEP-NAMESPACE-AUTHORITY-ROUTES"
               (error-id #(plan/validate-namespace-authority-projection
                           bad-route))))))))

(def ^:private expected-parallel-route-ids
  ["sh07-parallel-accepted"
   "sh07-parallel-b12-contract-accepted"
   "sh07-parallel-b12-identity-verification"
   "sh07-parallel-b12-rejected-regression"
   "sh07-parallel-identity-lineage"
   "sh07-parallel-regression-a"
   "sh07-parallel-regression-b"
   "sh07-parallel-rejected"
   "sh07-parallel-verification"])

(def ^:private expected-parallel-migration-route-ids
  ["sh07-b10-regression"
   "sh07-conformance-table"
   "sh07-parallel-accepted"
   "sh07-parallel-b12-contract-accepted"
   "sh07-parallel-b12-identity-verification"
   "sh07-parallel-b12-rejected-regression"
   "sh07-parallel-identity-lineage"
   "sh07-parallel-regression-a"
   "sh07-parallel-regression-b"
   "sh07-parallel-rejected"
   "sh07-parallel-verification"])

(deftest legacy-mechanism-projection-is-manifest-owned
  (let [projection (plan/legacy-mechanism-projection :parallel-shard-runner)]
    (is (= expected-parallel-route-ids (:route-ids projection)))
    (is (= :exact-historical-membership
           (:membership-semantics projection)))
    (is (= ["accepted" "b12-contract-accepted"
            "b12-identity-verification" "b12-rejected-regression"
            "identity-lineage" "regression-a" "regression-b"
            "rejected" "verification"]
           (:labels projection)))
    (is (= {:kind :strip-prefix :prefix "sh07-parallel-"}
           (:label-policy projection)))
    ;; Migration accounting intentionally remains broad (11 routes), while
    ;; the compatibility projection above retains the exact historical nine.
    (is (= expected-parallel-migration-route-ids
           (get (plan/legacy-route-catalog) :parallel-shard-runner)))
    (is (= 11 (count (get (plan/legacy-route-catalog)
                          :parallel-shard-runner))))
    (is (= 9 (count (:route-ids projection))))))

(deftest legacy-mechanism-declaration-fails-closed
  (let [declaration (get-in plan/catalog
                            [:legacy-mechanisms :parallel-shard-runner])]
    (is (= "UEP-CATALOG-LEGACY-MECHANISMS"
           (error-id #(plan/validate-catalog
                       (update plan/catalog :legacy-mechanisms
                               assoc :unknown declaration)))))
    (is (= "UEP-CATALOG-LEGACY-MECHANISM-KEYS"
           (error-id #(plan/validate-catalog
                       (update-in plan/catalog
                                  [:legacy-mechanisms :parallel-shard-runner]
                                  assoc :extra true)))))
    (is (= "UEP-CATALOG-LEGACY-MEMBERSHIP"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:legacy-mechanisms :parallel-shard-runner
                                  :membership-semantics]
                                 :broad-migration-membership)))))
    (is (= "UEP-CATALOG-LEGACY-LABEL-KEYS"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:legacy-mechanisms :parallel-shard-runner
                                  :label-policy :extra]
                                 true)))))
    (is (= "UEP-CATALOG-LEGACY-ROUTES"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:legacy-mechanisms :parallel-shard-runner
                                  :route-ids]
                                 (conj expected-parallel-route-ids
                                       "sh07-parallel-verification"))))))
    (is (= "UEP-CATALOG-LEGACY-ROUTE"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:legacy-mechanisms :parallel-shard-runner
                                  :route-ids]
                                 (conj (vec (butlast expected-parallel-route-ids))
                                       "sh07-parallel-unknown"))))))
    (is (= "UEP-CATALOG-LEGACY-ROUTE"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:routes "sh07-parallel-accepted"
                                  :replacement-for]
                                 [:cached-shard-runner])))))
    (is (= "UEP-CATALOG-ROUTE-MIGRATION"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:routes "sh07-parallel-accepted"
                                  :migration-status]
                                 :retired)))))
    (is (= "UEP-CATALOG-ROUTE-FAST-POLICY"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:routes "sh07-parallel-accepted"
                                  :cache-policy :authoritative?]
                                 true)))))
    (is (= "UEP-CATALOG-LEGACY-POLICY"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog
                                 [:routes "sh07-parallel-accepted"
                                  :selection :kind]
                                 :proof-contract-module-catalog)))))))

(deftest manifest-projection-validation-rejects-stale-declarations
  (let [stale-accepted (assoc-in plan/catalog
                                 [:conformance :coverage :accepted-count]
                                 67)
        duplicate-route (update-in plan/catalog
                                   [:coverage :route-ids]
                                   conj "sh07-authoritative")
        duplicate-suite-route (update-in plan/catalog
                                         [:suites 0 :route-ids]
                                         conj "sh07-b8-regression")
        stale-accounting (assoc-in plan/catalog
                                   [:coverage :accounting :selector-count]
                                   39)]
    (is (= "SH07-CONFORMANCE-COVERAGE"
           (error-id #(plan/validate-manifest-projections! stale-accepted))))
    (is (= "UEP-CATALOG-COVERAGE"
           (error-id #(plan/validate-manifest-projections! duplicate-route))))
    (is (= "UEP-CATALOG-SUITE-ROUTES"
           (error-id #(plan/validate-manifest-projections! duplicate-suite-route))))
    (is (= "UEP-MANIFEST-COVERAGE-ACCOUNTING"
           (error-id #(plan/validate-manifest-projections! stale-accounting))))))

(deftest manifest-paths-and-coverage-shapes-fail-closed
  (doseq [unsafe ["C:bootstrap/gravity/src/gravity/checked_core.gravity"
                  "bootstrap/gravity/src/gravity/checked_core.gravity\u0000"
                  "bootstrap/gravity/src/gravity/checked_core.gravity\u0001"]]
    (let [route-ids (keys (:routes plan/catalog))
          changed (reduce (fn [catalog route-id]
                            (update-in catalog [:routes route-id
                                                :dependency-closure :paths]
                                       (fn [paths]
                                         (mapv #(if (= "bootstrap/gravity/src/gravity/checked_core.gravity" %)
                                                  unsafe
                                                  %)
                                               paths))))
                          (assoc-in plan/catalog
                                    [:modules "sh07-checked-core" :source-paths]
                                    [unsafe])
                          route-ids)]
      (is (= "UEP-CATALOG-MODULE-SOURCES"
             (error-id #(plan/validate-catalog changed))))))
  (doseq [route-ids [(set (get-in plan/catalog [:coverage :route-ids]))
                     (seq (get-in plan/catalog [:coverage :route-ids]))]]
    (is (= "UEP-CATALOG-COVERAGE"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog [:coverage :route-ids]
                                 route-ids))))))
  (is (= "UEP-CATALOG-COVERAGE"
         (error-id #(plan/validate-catalog
                     (update-in plan/catalog [:coverage :route-ids]
                                (comp vec reverse)))))))

(deftest canonical-conformance-validation-is-required
  (is (= "SH07-CONFORMANCE-BASENAME"
         (error-id #(plan/validate-catalog
                     (assoc-in plan/catalog
                               [:conformance :suites 0 :accepted 0 :basename]
                               "../not-reviewed"))))))

(deftest conformance-suites-bind-to-manifest-module-ownership
  (let [renamed-module (get-in plan/catalog [:modules "sh07-checked-core"])
        route-ids (keys (:routes plan/catalog))
        changed (reduce (fn [catalog route-id]
                          (-> catalog
                              (assoc-in [:routes route-id :module-id]
                                        "other-module")
                              (assoc-in [:routes route-id :dependency-closure
                                         :module-ids]
                                        ["other-module"])))
                        (-> plan/catalog
                            (update :modules dissoc "sh07-checked-core")
                            (assoc-in [:modules "other-module"]
                                      (assoc renamed-module :id "other-module")))
                        route-ids)]
    (is (= "UEP-CATALOG-CLOSURE-FIXTURE-OWNER"
           (error-id #(plan/validate-manifest-projections! changed))))))

(deftest route-closures-bind-every-module-and-fixture-suite-owner
  (let [other {:id "other-module" :owner :gravity
               :source-paths ["bootstrap/gravity/src/gravity/other.gravity"]
               :artifact-request :checked-core :ownership-scope :sh07
               :required-evidence [:source-identity]}
        with-other (assoc-in plan/catalog [:modules "other-module"] other)
        missing-source
        (update-in with-other
                   [:routes "sh07-b8-regression" :dependency-closure :module-ids]
                   conj "other-module")
        wrong-fixture-owner
        (-> with-other
            (assoc-in [:routes "sh07-conformance-table" :module-id]
                      "other-module")
            (assoc-in [:routes "sh07-conformance-table" :dependency-closure
                       :module-ids]
                      ["other-module"])
            (update-in [:routes "sh07-conformance-table" :dependency-closure
                        :paths]
                       conj "bootstrap/gravity/src/gravity/other.gravity"))]
    (is (= "UEP-CATALOG-ROUTE-CLOSURE"
           (error-id #(plan/validate-catalog missing-source))))
    (is (= "UEP-CATALOG-CLOSURE-FIXTURE-OWNER"
           (error-id #(plan/validate-catalog wrong-fixture-owner))))))
