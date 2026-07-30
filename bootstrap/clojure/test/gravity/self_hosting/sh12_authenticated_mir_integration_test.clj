(ns gravity.self-hosting.sh12-authenticated-mir-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh11-authenticated-safety-integration-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh12_authenticated_mir_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-12 authenticated integration test is not on the classpath"
        {:id "SH12-AUTH-TEST-SOURCE"})))
    (loop [candidate
           (.getParent
            (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH12-AUTH-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-12/"
   "authenticated_mir_bridge.gravity"))

(def ^:private c11-source
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")

(def ^:private bridge-plan
  (delay (compile-plan bridge-source)))

(def ^:private c11-plan
  (delay (compile-plan c11-source)))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh12-authenticated-mir-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- resolved-value [namespace name]
  (or
   (some-> (ns-resolve namespace name) var-get)
   (throw
    (ex-info
     "Required authenticated integration helper is unavailable"
     {:id "SH12-AUTH-HELPER"
      :namespace namespace
      :name name}))))

(defn- sh11-value [name]
  (resolved-value
   'gravity.self-hosting.sh11-authenticated-safety-integration-test
   name))

(defn- call-private [namespace name & arguments]
  (apply (resolved-value namespace name) arguments))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh12-authenticated-mir-integration>" value))

(defn- identity-id [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- source-hash [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count [source-text]
  (alength
   (.getBytes
    source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- function-shapes [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

(defn- bridge-source-revision []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        builder 'sh12-build-checked-core]
    {:owner :sh-mir
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-12/authenticated-mir-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
       plan))
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
     (get (:functions plan) builder))
     :function-shapes (function-shapes plan)}))

(defn- c11-source-revision []
  (let [source-text (slurp (path c11-source))
        plan @c11-plan
        builder 'c11-build-target-independent-mir
        verifier 'verify-c11-mir-module]
    {:artifact :gravity/sh12-c11-source-revision
     :schema-version 1
     :owner :sh-mir
     :logical-source-path
     "compiler/c11-mir-specification"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
       plan))
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :function-count (count (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) builder))
     :verifier-function verifier
     :verifier-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) verifier))}))

(defn- sh08-products [extension]
  @(sh11-value
    (if (= extension ".gravity")
      'sh08-gravity
      'sh08-qst)))

(defn- prepared-sh11
  [extension outcome actual-path]
  (let [products (sh08-products extension)]
    (if (= outcome :proven-safe)
      (call-private
       'gravity.self-hosting.sh11-authenticated-safety-integration-test
       'prepared-request products :proven-overflow actual-path)
      (call-private
       'gravity.self-hosting.sh11-authenticated-safety-integration-test
       'prepared-request products :unsafe-overflow actual-path))))

(defn- verified-sh11 [extension outcome actual-path]
  (let [prepared
        (prepared-sh11 extension outcome actual-path)
        run
        (call-private
         'gravity.self-hosting.sh11-authenticated-safety-integration-test
         'run-bridge prepared)
        verification
        ((sh11-value 'invoke)
         (sh11-value 'bridge-plan)
         'sh11-verify-authenticated-safety-result
         [(:request prepared)
          (:c10-request run)
          (:safety-result run)
          (:safety-verification run)
          (:result run)])]
    {:safe-core (:result run)
     :verification verification}))

(def ^:private authentic-product-delays
  (into
   {}
   (for [extension [".gravity" ".qst"]
         outcome [:proven-safe :unsafe-island]]
     [[extension outcome]
      (delay
        (verified-sh11
         extension outcome
         (str "/checkout-a/sh11-input" extension
              "/" (name outcome))))])))

(defn- authentic-product [extension outcome]
  @(get authentic-product-delays [extension outcome]))

(defn- rebind-safe-core [products safe-core]
  {:safe-core safe-core
   :verification
   (-> (:verification products)
       (assoc :expected safe-core)
       (assoc :candidate safe-core))})

(defn- sh12-request
  [products extension outcome actual-path]
  (let [safe-core (:safe-core products)
        semantic
        {:safe-artifact-id (:artifact-id safe-core)
         :outcome outcome}
        checked-core-identity
        {:safe-artifact-id (:artifact-id safe-core)
         :safe-outcome (:outcome safe-core)
         :c11-revision (c11-source-revision)
         :typed-artifact-id (:typed-artifact-id safe-core)
         :effect-artifact-id (:effect-artifact-id safe-core)
         :ownership-artifact-id
         (:ownership-artifact-id safe-core)
         :safety-artifact-id (:artifact-id safe-core)}]
    {:artifact :gravity/sh12-authenticated-mir-request
     :schema-version 1
     :safe-core safe-core
     :safe-verification (:verification products)
     :checked-core-artifact-id
     (identity-id :gravity/sh12-checked-core-v1 semantic)
     :checked-core-mapping-id
     (identity-id :gravity/sh12-checked-core-mapping-v1 semantic)
     :expected-mir-id
     (identity-id
      :gravity/sh12-authenticated-mir-v1
      {:source-safe-core (:artifact-id safe-core)
       :checked-core checked-core-identity})
     :c11-revision (c11-source-revision)
     :entrypoint 'gravity.self-hosting.sh12/main
     :module-id 'gravity.self-hosting.sh12.fixture
     :actual-source-path actual-path}))

(defn- run-sh12 [products extension outcome actual-path]
  (let [request
        (sh12-request products extension outcome actual-path)
        checked-core
        (invoke
         bridge-plan 'sh12-build-checked-core [request])
        mir
        (invoke
         c11-plan
         'c11-build-target-independent-mir
         [checked-core])
        mir-verification
        (invoke
         c11-plan 'verify-c11-mir-module [mir])
        mir-id (:expected-mir-id request)
        result
        (invoke
         bridge-plan
         'sh12-bind-authenticated-mir
         [request checked-core mir mir-verification mir-id])
        verification
        (invoke
         bridge-plan
         'sh12-verify-authenticated-mir
         [request checked-core mir
          mir-verification mir-id result])]
    {:request request
     :checked-core checked-core
     :mir mir
     :mir-verification mir-verification
     :mir-id mir-id
     :result result
     :verification verification}))

(deftest sh12-co-canonical-source-extensions-have-equal-identities
  (doseq [outcome [:proven-safe :unsafe-island]]
    (let [gravity
          (run-sh12
           (authentic-product ".gravity" outcome)
           ".gravity" outcome
           "/checkout-a/sh12-input.gravity")
          qst
          (run-sh12
           (authentic-product ".qst" outcome)
           ".qst" outcome
           "/checkout-a/sh12-input.qst")]
      (is (= (get-in gravity
                     [:checked-core :identity-input])
             (get-in qst
                     [:checked-core :identity-input])))
      (is (= (:mir-id gravity) (:mir-id qst)))
      (is (= (get-in gravity [:result :identity-input])
             (get-in qst [:result :identity-input]))))))

(deftest sh12-authenticated-mir-bridge-compiles-with-narrow-api
  (doseq [plan [bridge-plan c11-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan
           (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is
     (=
      {'sh12-authenticated-mir-bridge-policy
       {:arity 0 :params []}
       'sh12-build-checked-core
       {:arity 1 :params ['request]}
       'sh12-bind-authenticated-mir
       {:arity 5
        :params
        ['request 'checked-core 'mir
         'mir-verification 'mir-id]}
       'sh12-verify-authenticated-mir
       {:arity 6
        :params
        ['request 'checked-core 'mir
         'mir-verification 'mir-id 'candidate]}}
      (select-keys
       (function-shapes @bridge-plan)
       ['sh12-authenticated-mir-bridge-policy
        'sh12-build-checked-core
        'sh12-bind-authenticated-mir
        'sh12-verify-authenticated-mir]))))
  (let [policy
        (invoke
         bridge-plan
         'sh12-authenticated-mir-bridge-policy
         [])]
    (is (= [:proven-safe :runtime-checked
            :unsafe-island]
           (:accepted-outcomes policy)))
    (is (false? (:self-hosted? policy)))
    (is (some #{:sh12-completion} (:pending policy)))))

(deftest sh12-builds-c11-mir-for-both-source-extensions
  (doseq [extension [".gravity" ".qst"]
          outcome [:proven-safe :unsafe-island]]
    (testing (str extension " " outcome)
      (let [products
            (authentic-product extension outcome)
            run
            (run-sh12
             products extension outcome
             (str "/checkout-a/sh12-input" extension
                  "/" (name outcome)))
            checked-core (:checked-core run)
            mir (:mir run)
            result (:result run)
            preserves
            (get-in products
                    [:safe-core :safety-result :preserves])
            literal-id
            (get-in checked-core [:core-nodes 0 :node-id])
            literal-safety
            (get-in checked-core
                    [:safety-facts literal-id])]
        (is (= :accepted
               (get-in products [:safe-core :status])))
        (is (= outcome
               (get-in products [:safe-core :outcome])))
        (is (= :passed
               (get-in products [:verification :status])))
        (is (= :accepted (:status checked-core)))
        (is (= :gravity/mir-module (:artifact mir)))
        (is (true? (:target-independent? mir)))
        (is (= (:artifact-id checked-core)
               (:source-core mir)))
        (is (= :stage1-source-owned
               (get-in run
                       [:mir-verification :status])))
        (is (= :accepted (:status result)))
        (is (= :passed
               (get-in run [:verification :status])))
        (is (= outcome
               (get-in checked-core
                       [:identity-input
                        :safe-outcome])))
        (is (empty? (:runtime-check-table mir)))
        (is (nil? (:sh12-runtime-check-lowering mir)))
        (doseq [[_ fact] (:type-facts checked-core)]
          (is (contains?
               (:type-table mir)
               (:fact-id fact))))
        (is (= (:type-fact-id preserves)
               (get-in checked-core
                       [:type-facts literal-id :fact-id])))
        (is (= (:effect-fact-id preserves)
               (get-in checked-core
                       [:effect-facts literal-id :fact-id])))
        (is (= (:ownership-fact-id preserves)
               (get-in checked-core
                       [:ownership-facts literal-id :fact-id])))
        (is (contains?
             (:capability-proof-table mir)
             (:capability-proof-id preserves)))
        (is (contains?
             (:safety-table mir)
             (:outcome-id literal-safety)))
        (if (= outcome :proven-safe)
          (do
            (is (map? (:proof literal-safety)))
            (is (nil? (:unsafe-audit literal-safety))))
          (do
            (is (nil? (:proof literal-safety)))
            (is (map? (:unsafe-audit literal-safety)))))
        (is (= (:actual-source-path
                (:provenance checked-core))
               (str "/checkout-a/sh12-input"
                    extension "/" (name outcome))))))))

(deftest sh12-rejects-self-consistent-malformed-outcome-products
  (let [proven-products
        (authentic-product ".gravity" :proven-safe)
        proven (:safe-core proven-products)
        proof
        (get-in proven [:safety-result :proofs 0])
        outcome
        (get-in proven [:safety-result :outcomes 0])
        unsafe-products
        (authentic-product ".gravity" :unsafe-island)
        unsafe (:safe-core unsafe-products)
        audit
        (get-in unsafe
                [:safety-result :unsafe-islands 0])
        unsafe-outcome
        (get-in unsafe
                [:safety-result :outcomes 0])
        malformed
        [[:nil-proof
          proven-products
          (-> proven
              (assoc-in [:safety-result :proofs] [nil])
              (assoc-in
               [:safety-result :outcomes 0 :proof] nil)
              (assoc-in [:identity-input :proofs] [nil])
              (assoc-in
               [:identity-input :outcomes 0 :proof] nil))
          :proven-safe]
         [:missing-proof-field
          proven-products
          (let [altered (dissoc proof :provider)]
            (-> proven
                (assoc-in
                 [:safety-result :proofs 0] altered)
                (assoc-in
                 [:safety-result :outcomes 0 :proof]
                 altered)
                (assoc-in
                 [:identity-input :proofs 0] altered)
                (assoc-in
                 [:identity-input :outcomes 0 :proof]
                 altered)))
          :proven-safe]
         [:extra-proof-field
          proven-products
          (let [altered (assoc proof :unexpected true)]
            (-> proven
                (assoc-in
                 [:safety-result :proofs 0] altered)
                (assoc-in
                 [:safety-result :outcomes 0 :proof]
                 altered)
                (assoc-in
                 [:identity-input :proofs 0] altered)
                (assoc-in
                 [:identity-input :outcomes 0 :proof]
                 altered)))
          :proven-safe]
         [:altered-proof-method
          proven-products
          (let [altered
                (assoc proof :method :unverified-method)]
            (-> proven
                (assoc-in
                 [:safety-result :proofs 0] altered)
                (assoc-in
                 [:safety-result :outcomes 0 :proof]
                 altered)
                (assoc-in
                 [:identity-input :proofs 0] altered)
                (assoc-in
                 [:identity-input :outcomes 0 :proof]
                 altered)))
          :proven-safe]
         [:coordinated-overflowing-proof
          proven-products
          (let [altered-facts
                (-> (get-in
                     proven
                     [:safety-result :identity-input
                      :request :facts])
                    (assoc :left 2147483647)
                    (assoc :right 1))
                altered-request
                (assoc-in
                 (get-in
                  proven
                  [:safety-result :identity-input
                   :request])
                 [:facts]
                 altered-facts)
                altered-proof
                (-> proof
                    (assoc :facts altered-facts)
                    (assoc-in
                     [:proof-id-request :facts]
                     altered-facts))
                altered-outcome
                (-> outcome
                    (assoc :facts altered-facts)
                    (assoc :proof altered-proof))]
            (-> proven
                (assoc-in
                 [:safety-result :identity-input
                  :request]
                 altered-request)
                (assoc-in
                 [:safety-result :proofs 0]
                 altered-proof)
                (assoc-in
                 [:safety-result :outcomes 0]
                 altered-outcome)
                (assoc-in
                 [:identity-input :proofs 0]
                 altered-proof)
                (assoc-in
                 [:identity-input :outcomes 0]
                 altered-outcome)))
          :proven-safe]
         [:list-shaped-proofs
          proven-products
          (-> proven
              (assoc-in
               [:safety-result :proofs] (list proof))
              (assoc-in
               [:identity-input :proofs] (list proof)))
          :proven-safe]
         [:wrong-outcome-operation
          proven-products
          (let [altered
                (assoc
                 outcome :operation-id
                 "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")]
            (-> proven
                (assoc-in
                 [:safety-result :outcomes 0] altered)
                (assoc-in
                 [:identity-input :outcomes 0] altered)))
          :proven-safe]
         [:altered-specialized-rule
          proven-products
          (let [altered
                (assoc outcome
                       :specialized-safe-rule
                       :SAFE1-NO-OUTCOME)]
            (-> proven
                (assoc-in
                 [:safety-result :outcomes 0] altered)
                (assoc-in
                 [:identity-input :outcomes 0] altered)))
          :proven-safe]
         [:malformed-preserves
          proven-products
          (let [altered
                (dissoc
                 (get-in proven
                         [:safety-result :preserves])
                 :target)]
            (-> proven
                (assoc-in
                 [:safety-result :preserves] altered)
                (assoc-in
                 [:identity-input :preserves] altered)))
          :proven-safe]
         [:nil-unsafe-audit
          unsafe-products
          (-> unsafe
              (assoc-in
               [:safety-result :unsafe-islands] [nil])
              (assoc-in
               [:safety-result :outcomes 0
                :unsafe-audit] nil)
              (assoc-in
               [:identity-input :unsafe-islands] [nil])
              (assoc-in
               [:identity-input :outcomes 0
                :unsafe-audit] nil))
          :unsafe-island]
         [:wrong-unsafe-operation
          unsafe-products
          (let [altered
                (assoc
                 audit :operation-id
                 "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                altered-outcome
                (assoc unsafe-outcome
                       :unsafe-audit altered)]
            (-> unsafe
                (assoc-in
                 [:safety-result :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:safety-result :outcomes 0]
                 altered-outcome)
                (assoc-in
                 [:identity-input :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:identity-input :outcomes 0]
                 altered-outcome)))
          :unsafe-island]
         [:altered-unsafe-policy
          unsafe-products
          (let [altered
                (assoc-in
                 audit
                 [:policy :package-unsafe-approved]
                 false)
                altered-outcome
                (assoc unsafe-outcome
                       :unsafe-audit altered)]
            (-> unsafe
                (assoc-in
                 [:safety-result :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:safety-result :outcomes 0]
                 altered-outcome)
                (assoc-in
                 [:identity-input :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:identity-input :outcomes 0]
                 altered-outcome)))
          :unsafe-island]
         [:coordinated-invalid-unsafe-effect
          unsafe-products
          (let [altered
                (-> audit
                    (assoc :effects #{"not-a-keyword"})
                    (assoc-in
                     [:policy :empty-effects-approved]
                     false))
                altered-request
                (assoc
                 (get-in
                  unsafe
                  [:safety-result :identity-input
                   :request])
                 :unsafe-audit altered)
                altered-outcome
                (assoc unsafe-outcome
                       :unsafe-audit altered)]
            (-> unsafe
                (assoc-in
                 [:safety-result :identity-input
                  :request]
                 altered-request)
                (assoc-in
                 [:safety-result :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:safety-result :outcomes 0]
                 altered-outcome)
                (assoc-in
                 [:identity-input :unsafe-islands 0]
                 altered)
                (assoc-in
                 [:identity-input :outcomes 0]
                 altered-outcome)))
          :unsafe-island]]]
    (doseq [[label products safe-core selected]
            malformed]
      (testing (name label)
        (let [request
              (sh12-request
               (rebind-safe-core products safe-core)
               ".gravity" selected
               (str "/checkout-a/sh12-malformed/"
                    (name label) ".gravity"))
              checked-core
              (invoke
               bridge-plan
               'sh12-build-checked-core
               [request])]
          (is (= :rejected (:status checked-core)))
          (is (= "STD12-BRIDGE-LINEAGE"
                 (get-in checked-core
                         [:diagnostics 0 :rule]))))))))

(deftest sh12-rejects-checked-core-and-mir-alteration
  (let [products
        (authentic-product ".gravity" :proven-safe)
        run
        (run-sh12
         products ".gravity" :proven-safe
         "/checkout-a/sh12-input.gravity")
        altered-core
        (assoc-in
         (:checked-core run)
         [:identity-input :safe-outcome]
         :unsafe-island)
        altered-mir
        (assoc (:mir run) :target-independent? false)
        altered-sh11
        (assoc-in
         (:request run)
         [:safe-verification :expected :pending]
         [:altered])
        altered-c11-verification
        (assoc (:mir-verification run)
               :mir-module altered-mir)
        altered-c11-revision
        (assoc-in
         (:request run)
         [:c11-revision :verifier-semantic-hash]
         (identity-id
          :gravity/sh12-substituted-c11-verifier-v1
          :different))]
    (is
     (= "STD12-BRIDGE-LINEAGE"
        (get-in
         (invoke
          bridge-plan 'sh12-build-checked-core
          [altered-sh11])
         [:diagnostics 0 :rule])))
    (is
     (= :rejected
        (:status
         (invoke
          bridge-plan
          'sh12-bind-authenticated-mir
          [(:request run)
           altered-core
           (:mir run)
           (:mir-verification run)
           (:mir-id run)]))))
    (is
     (= "STD12-BRIDGE-MIR"
        (get-in
         (invoke
          bridge-plan
          'sh12-bind-authenticated-mir
          [(:request run)
           (:checked-core run)
           altered-mir
           (:mir-verification run)
           (:mir-id run)])
         [:diagnostics 0 :rule])))
    (is
     (= "STD12-BRIDGE-MIR"
        (get-in
         (invoke
          bridge-plan
          'sh12-bind-authenticated-mir
          [(:request run)
           (:checked-core run)
           (:mir run)
           altered-c11-verification
           (:mir-id run)])
         [:diagnostics 0 :rule])))
    (is
     (= :rejected
        (:status
         (invoke
          bridge-plan
          'sh12-bind-authenticated-mir
          [(:request run)
           (:checked-core run)
           (:mir run)
           (:mir-verification run)
           (identity-id
            :gravity/sh12-substituted-mir-v1
            :different)]))))
    (is
     (= :rejected
        (:status
         (invoke
          bridge-plan
          'sh12-verify-authenticated-mir
          [(:request run)
           (:checked-core run)
           (:mir run)
           (:mir-verification run)
           (:mir-id run)
           (assoc (:result run)
                  :artifact-id
                  (identity-id
                   :gravity/sh12-altered-v1
                   :altered))]))))
    (is
     (= :rejected
        (:status
         (invoke
          bridge-plan
          'sh12-verify-authenticated-mir
          [(:request run)
           (:checked-core run)
           (:mir run)
           (:mir-verification run)
           (:mir-id run)
           (assoc (:result run)
                  :mir-module altered-mir)]))))
    (is
     (= "STD12-BRIDGE-LINEAGE"
        (get-in
         (invoke
          bridge-plan 'sh12-build-checked-core
          [altered-c11-revision])
         [:diagnostics 0 :rule])))))

(deftest sh12-binds-c11-diagnostics-and-uses-outcome-specific-validation
  (doseq [outcome [:proven-safe :unsafe-island]]
    (let [run
          (run-sh12
           (authentic-product ".gravity" outcome)
           ".gravity" outcome
           (str "/checkout-a/sh12-validation/"
                (name outcome)))
          diagnostic-catalog
          (invoke
           bridge-plan
           'sh12-c11-diagnostic-catalog
           [])
          linked-verification
          (assoc
           (:mir-verification run)
           :mir-module (:mir run)
           :diagnostics diagnostic-catalog)
          altered-diagnostics
          (assoc linked-verification
                 :diagnostics
                 (assoc diagnostic-catalog
                        :diagnostics ["C11-ALTERED"]))
          altered-runtime-table
          (assoc
           (:mir run)
           :runtime-check-table
           {:unexpected {:check-id :unexpected}})
          altered-fields
          [(assoc-in
            (:mir run)
            [:control-flow-graph :entry]
            :altered-entry)
           (assoc-in
            (:mir run)
            [:data-flow-graph :edges]
            [{:from :altered}])
           (assoc
            (:mir run)
            :type-table
            {:altered {:fact-id :altered}})
           (assoc
            (:mir run)
            :source-map
            {:altered {:source :altered}})]]
      (is
       (true?
        (invoke
         bridge-plan
         'sh12-mir-legacy-valid?
         [(:checked-core run)
          (:mir run)
          linked-verification])))
      (is
       (true?
        (invoke
         bridge-plan
         'sh12-mir-valid?
         [(:checked-core run)
          (:mir run)
          linked-verification])))
      (is
       (false?
        (invoke
         bridge-plan
         'sh12-mir-verification-valid?
         [(:mir run) altered-diagnostics])))
      (is
       (false?
        (invoke
         bridge-plan
         'sh12-mir-valid?
         [(:checked-core run)
          altered-runtime-table
          (assoc linked-verification
                 :mir-module altered-runtime-table)])))
      (doseq [altered altered-fields]
        (is
         (false?
          (invoke
           bridge-plan
           'sh12-mir-valid?
           [(:checked-core run)
            altered
            linked-verification])))))))

(deftest sh12-bridge-fails-closed-on-malformed-and-overdeep-input
  (doseq [value
          [nil
           {}
           {:artifact :gravity/sh12-authenticated-mir-request}
           {:artifact :gravity/sh12-authenticated-mir-request
            :schema-version 1
            :unexpected true}]]
    (let [result
          (invoke bridge-plan 'sh12-build-checked-core [value])]
      (is (= :rejected (:status result)))
      (is (= "STD12-BRIDGE-SCHEMA"
             (get-in result [:diagnostics 0 :rule])))))
  (let [over-depth
        (reduce (fn [value _] [value])
                :leaf
                (range 130))
        result
        (invoke bridge-plan 'sh12-build-checked-core [over-depth])]
    (is (= :rejected (:status result)))
    (is (= "STD12-BRIDGE-SCHEMA"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :request-carrier-bound
           (get-in result
                   [:diagnostics 0 :facts :reason])))))

(deftest sh12-verifier-omits-overdeep-candidate-carriers
  (let [run
        (run-sh12
         (authentic-product ".gravity" :proven-safe)
         ".gravity" :proven-safe
         "/checkout-a/sh12-overdeep-candidate.gravity")
        over-depth
        (reduce (fn [value _] [value])
                :leaf
                (range 130))
        accepted-shaped
        (assoc (:result run) :mir-module over-depth)
        rejected-shaped
        {:artifact :gravity/sh12-authenticated-mir
         :schema-version 1
         :status :rejected
         :diagnostics
         [{:rule "STD12-BRIDGE-VERIFY"
           :facts {:nested over-depth}}]}
        verify
        (fn [candidate]
          (invoke
           bridge-plan
           'sh12-verify-authenticated-mir
           [(:request run)
            (:checked-core run)
            (:mir run)
            (:mir-verification run)
            (:mir-id run)
            candidate]))]
    (doseq [candidate [accepted-shaped rejected-shaped]]
      (let [verification (verify candidate)]
        (is (= :rejected (:status verification)))
        (is (= :candidate-result-carrier-bound
               (:reason verification)))
        (is (= :carrier-depth-bound
               (get-in verification
                       [:candidate-preflight :reason])))
        (is (= :omitted (:candidate-echo verification)))
        (is (= "STD12-BRIDGE-VERIFY"
               (get-in verification
                       [:diagnostics 0 :rule])))
        (is (not (contains? verification :candidate)))
        (is (not (contains? verification :expected)))))))

(deftest sh12-identities-are-path-neutral-and-provenance-is-distinct
  (doseq [extension [".gravity" ".qst"]
          outcome [:proven-safe :unsafe-island]]
    (let [products
          (authentic-product extension outcome)
          left
          (run-sh12
           products extension outcome
           (str "/checkout-a/sh12" extension))
          right
          (run-sh12
           products extension outcome
           (str "/checkout-b/sh12" extension))]
      (is (= (:mir-id left) (:mir-id right)))
      (is (= (get-in left [:result :identity-input])
             (get-in right [:result :identity-input])))
      (is (not=
           (get-in left [:result :provenance
                         :actual-source-path])
           (get-in right [:result :provenance
                          :actual-source-path])))
      (is (= :passed
             (get-in left [:verification :status])))
      (is (= :passed
             (get-in right [:verification :status]))))))

(deftest sh12-bridge-source-identity-is-deterministic
  (let [first-revision (bridge-source-revision)
        second-revision (bridge-source-revision)
        expected-c11-revision
        (invoke
         bridge-plan
         'sh12-expected-c11-revision
         [])]
    (is (= first-revision second-revision))
    (is (= (c11-source-revision)
           expected-c11-revision))
    (is (= :sh-mir (:owner first-revision)))
    (is (re-matches
         #"sha256:[0-9a-f]{64}"
         (:source-content-hash first-revision)))
    (is (re-matches
         #"sha256:[0-9a-f]{64}"
         (:plan-semantic-hash first-revision)))
    (is (re-matches
         #"sha256:[0-9a-f]{64}"
         (:functions-semantic-hash first-revision)))
    (is (re-matches
         #"sha256:[0-9a-f]{64}"
         (:builder-semantic-hash first-revision)))))
