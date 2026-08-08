(ns gravity.self-hosting.sh09-c7-effect-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh09_c7_effect_adapter_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-09 C7 adapter test source is not on the classpath"
        {:id "SH09-C7-ADAPTER-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH09-C7-ADAPTER-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")

(defn- compile-plan
  []
  (let [source-path (str (.resolve @root source-relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c8-plan (delay (compile-plan)))

(defn- invoke-c8
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh09-c7-effect-adapter-test
    :compiler-artifact-plan? true}
   @c8-plan function arguments))

(def ^:private sha-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def ^:private sha-b
  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
(def ^:private sha-c
  "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
(def ^:private sha-d
  "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
(def ^:private sha-e
  "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")

(defn- type-fact
  [node-id syntax-id type start]
  {:artifact :gravity/c7-type-fact
   :fact-id-request
   {:domain :gravity/sh08-primitive-type-fact-v1
    :core-node-id node-id
    :type type
    :producer :literal
    :dependencies []
    :profile :meta
    :target :jvm
    :effects []
    :capabilities []
    :origin-chain []
    :generated-origin nil}
   :core-node-id node-id
   :type type
   :producer-rule :literal
   :dependencies []
   :syntax-id syntax-id
   :source-span
   {:source "synthetic-sh09-adapter.gravity"
    :start-byte start
    :end-byte (+ start 1)}
   :origin-chain []
   :generated-origin nil
   :binding-id nil
   :profile :meta
   :target :jvm
   :effects []
   :capabilities []
   :ownership :pending-sh10
   :status :inferred})

(defn- typed-result
  [actual-path]
  (let [module
        {:namespace 'gravity.self-hosting.sh09.synthetic
         :profile :meta
         :target :jvm
         :effects []
         :capabilities []
         :safety :safe}
        facts
        [(type-fact sha-a sha-c :gravity.type/integer 0)
         (type-fact sha-b sha-d :gravity.type/bool 2)]
        table {sha-a :gravity.type/integer
               sha-b :gravity.type/bool}
        typed-core
        {:artifact :gravity/typed-core
         :core-input sha-e
         :module module
         :types table
         :constraints []
         :dynamic-boundaries []
         :casts []
         :diagnostics []}
        identity
        {:artifact :gravity/sh08-authenticated-typed-core
         :scope
         :primitive-literals-vector-map-set-definitions-equal-if-joins
         :sh07-artifact-id sha-e
         :typed-artifact-id sha-b
         :module module
         :type-table table
         :type-facts facts
         :typed-core typed-core
         :diagnostics []
         :semantic-envelope-id sha-c}]
    {:artifact :gravity/sh08-authenticated-typed-core
     :schema-version 1
     :status :accepted
     :scope
     :primitive-literals-vector-map-set-definitions-equal-if-joins
     :artifact-id sha-b
     :sh07-artifact-id sha-e
     :module module
     :type-table table
     :type-facts facts
     :typed-core typed-core
     :identity-input identity
     :provenance
     {:actual-source-path actual-path
      :provenance-binding-id sha-d}
     :diagnostics []
     :pending [:functions :calls :locals]
     :self-hosted? false
     :clojure-seed-boundary? true}))

(defn- upstream-verification
  [typed]
  {:artifact :gravity/sh08-authenticated-type-verification
   :status :passed
   :checks
   [:verified-sh07-canonical-core
    :authenticated-input-and-output-identities
    :fresh-c7-result-replay
    :exact-typed-result
    :path-neutral-identity
    :separate-actual-path-provenance]
   :expected typed
   :candidate typed})

(defn- function-typed-result
  [actual-path]
  (let [module
        {:namespace 'gravity.self-hosting.sh09.function-synthetic
         :profile :meta :target :jvm
         :effects [] :capabilities [] :safety :safe}
        functions
        [{:artifact :gravity/c7-function-type
          :function-id sha-a
          :function-core-node-id sha-b
          :body-core-node-id sha-c
          :latent-effects []
          :capabilities []
          :thrown-error-effects [:pending-sh09]
          :profile :meta
          :target :jvm
          :source-span {:source "function.gravity"
                        :start-byte 0 :end-byte 1}
          :status :inferred}]
        calls
        [{:artifact :gravity/c7-call-type-fact
          :fact-id-request
          {:domain :gravity/sh08-call-type-fact-v1
           :call-core-node-id sha-d}
          :call-core-node-id sha-d
          :caller-function-syntax-id sha-a
          :callee-function-syntax-id sha-a
          :syntax-id sha-e
          :source-span {:source "function.gravity"
                        :start-byte 2 :end-byte 3}
          :origin-chain []
          :generated-origin nil
          :profile :meta
          :target :jvm
          :effects []
          :capabilities []
          :status :inferred}]
        proof {:domain :gravity/sh08-authoritative-higher-order-proof-v1}
        typed-core
        {:artifact :gravity/typed-core
         :core-input sha-e
         :module module
         :types {sha-d :gravity.type/bool}
         :constraints []
         :function-types functions
         :local-bindings []
         :calls calls
         :diagnostics []
         :higher-order-proof proof
         :higher-order-call-facts []}
        function-products
        {:function-records []
         :call-edges []
         :recursion-components []
         :lexical-bindings []}
        identity
        {:domain :gravity/sh08-authoritative-higher-order-type-v1
         :sh07-shaped-artifact-id sha-e
         :module module
         :function-records (:function-records function-products)
         :call-edges (:call-edges function-products)
         :recursion-components (:recursion-components function-products)
         :lexical-bindings (:lexical-bindings function-products)
         :function-type-table functions
         :local-binding-facts []
         :call-type-facts calls
         :constraint-ledger []
         :type-table {sha-d :gravity.type/bool}
         :convergence {:status :converged}
         :pending [:records :unions]
         :higher-order-proof proof}]
    {:artifact :gravity/sh08-function-typed-core-template
     :schema-version 3
     :status :accepted
     :scope :capture-free-higher-order-fixed-arity-one-hop
     :authentication-status :host-resolved-b47-verification-boundary
     :module module
     :sh07-shaped-artifact-id sha-e
     :function-products function-products
     :function-type-table functions
     :local-binding-facts []
     :call-type-facts calls
     :constraint-ledger []
     :type-table {sha-d :gravity.type/bool}
     :convergence {:status :converged}
     :typed-core typed-core
     :diagnostics []
     :artifact-id-request identity
     :identity-input identity
     :identity-resolution :coordinator-digest-required
     :provenance {:actual-source-path actual-path}
     :pending [:records :unions]
     :higher-order-proof proof
     :higher-order-call-facts []}))

(defn- function-verification
  [typed]
  {:artifact :gravity/sh08-function-type-verification
   :status :passed
   :checks [:coherent-b47-function-records
            :path-neutral-identity-input]
   :expected typed
   :candidate typed})

(defn- build
  [typed verification]
  (invoke-c8
   'sh09-build-authenticated-pure-effect-result
   [typed verification]))

(deftest sh09-c7-adapter-source-structure-and-policy-are-exact
  (let [functions (:functions @c8-plan)
        policy
        (invoke-c8 'sh09-authenticated-sh08-adapter-policy [])]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @c8-plan)))
    (is (= {:arity 0 :params []}
           (select-keys
            (get functions 'sh09-authenticated-sh08-adapter-policy)
            [:arity :params])))
    (is (= {:arity 2 :params ['typed 'verification]}
           (select-keys
            (get functions 'sh09-build-authenticated-pure-effect-result)
            [:arity :params])))
    (is (= {:arity 3 :params ['typed 'verification 'candidate]}
           (select-keys
            (get functions 'sh09-verify-authenticated-pure-effect-result)
            [:arity :params])))
    (is (= {:arity 3 :params ['typed 'verification 'effected]}
           (select-keys
            (get functions 'sh09-authenticated-effect-identity-requests)
            [:arity :params])))
    (is (= {:arity 4
            :params ['typed 'verification 'effected 'resolved]}
           (select-keys
            (get functions 'sh09-bind-authenticated-effect-identities)
            [:arity :params])))
    (is (= {:arity 5
            :params
            ['typed 'verification 'effected 'resolved 'candidate]}
           (select-keys
            (get functions 'sh09-verify-authenticated-effect-identities)
            [:arity :params])))
    (is (some #{:pure-typed-core} (:accepted-effect-scopes policy)))
    (is (some #{:declared-pure-call-effects-with-thrown-effects-pending}
              (:accepted-effect-scopes policy)))
    (is (some #{:first-order-fixed-arity-functions-locals-calls}
              (:accepted-upstream-scopes policy)))
    (is (some #{:effectful-sh08-adapter} (:pending policy)))
    (is (some #{:trusted-digest-resolution} (:pending policy)))
    (is (some #{:authenticated-effectful-or-nonprimitive-sh09-adapter}
              (:pending policy)))
    (is (not-any? #{:authenticated-sh08-adapter}
                  (:pending (invoke-c8 'sh09-effect-policy []))))))

(deftest sh09-c7-adapter-derives-one-pure-effect-fact-per-type-fact
  (let [typed-a (typed-result "/checkout-a/input.gravity")
        typed-b (typed-result "/checkout-b/input.qst")
        result-a (build typed-a (upstream-verification typed-a))
        result-b (build typed-b (upstream-verification typed-b))
        graph (:effect-graph result-a)]
    (is (= :accepted (:status result-a) (:status result-b)))
    (is (= :pure-authenticated-sh08-primitive-typed-core
           (:scope result-a)))
    (is (= (:identity-input result-a) (:identity-input result-b)))
    (is (not= (:provenance result-a) (:provenance result-b)))
    (is (= (:type-facts typed-a) (:type-facts result-a)))
    (is (= (:typed-core typed-a) (:typed-core result-a)))
    (is (= 2 (count (:effect-requests result-a))))
    (is (= [:accepted :accepted]
           (mapv :status (:effect-legality-results result-a))))
    (is (= #{sha-a sha-b} (set (keys (:nodes graph)))))
    (doseq [[node-id node] (:nodes graph)]
      (is (= node-id (:core-node-id node)))
      (is (= #{} (:direct node) (:latent node) (:transitive node)))
      (is (= :pure (:ordering node)))
      (is (= :coordinator-digest-required
             (get-in node [:type-fact-id :resolution])))
      (is (map? (get-in node [:type-fact-id :identity-input]))))
    (is (= #{} (get-in graph [:namespace :declared])))
    (is (= #{} (get-in graph [:namespace :inferred])))
    (is (= #{} (:residual-effects graph)))
    (is (= :passed
           (:status
            (invoke-c8
             'sh09-verify-authenticated-pure-effect-result
             [typed-a (upstream-verification typed-a) result-a]))))))

(deftest sh09-c7-adapter-rejects-upstream-and-candidate-substitution
  (let [typed (typed-result "/checkout-a/input.gravity")
        verification (upstream-verification typed)
        accepted (build typed verification)
        hostile
        [[:failed-verification typed (assoc verification :status :rejected)]
         [:verification-candidate typed
          (assoc verification :candidate
                 (assoc typed :artifact-id sha-a))]
         [:module-effect
          (assoc-in typed [:module :effects] #{:compiler/read-ir})
          verification]
         [:fact-effect
          (assoc-in typed [:type-facts 0 :effects] #{:compiler/read-ir})
          verification]
         [:fact-capability
          (assoc-in typed [:type-facts 0 :capabilities]
                    #{:compiler/ir-read})
          verification]
         [:type-table
          (assoc-in typed [:type-table sha-a] :gravity.type/string)
          verification]
         [:typed-core
          (assoc-in typed [:typed-core :types sha-a] :gravity.type/string)
          verification]
         [:profile (assoc-in typed [:module :profile] :hosted)
          verification]
         [:duplicate-node
          (assoc typed :type-facts
                 [(first (:type-facts typed))
                  (first (:type-facts typed))])
          verification]
         [:provenance-binding
          (assoc-in typed [:provenance :provenance-binding-id] :not-a-digest)
          verification]]]
    (doseq [[label altered altered-verification] hostile]
      (testing (name label)
        (let [result (build altered altered-verification)]
          (is (= :rejected (:status result)))
          (is (= "C8-VERIFY"
                 (get-in result [:diagnostics 0 :rule]))))))
    (doseq [candidate
            [(assoc accepted :scope :effectful)
             (assoc-in accepted [:effect-graph :nodes sha-a :direct]
                       #{:compiler/read-ir})
             (assoc-in accepted [:identity-input :type-table sha-a]
                       :gravity.type/string)
             (assoc accepted :pending [])]]
      (is (= :rejected
             (:status
              (invoke-c8
               'sh09-verify-authenticated-pure-effect-result
               [typed verification candidate])))))))

(deftest sh09-c7-adapter-derives-declared-pure-function-call-effects
  (let [typed-a (function-typed-result "/checkout-a/function.gravity")
        typed-b (function-typed-result "/checkout-b/function.qst")
        result-a (build typed-a (function-verification typed-a))
        result-b (build typed-b (function-verification typed-b))
        first-order-identity
        (-> (:identity-input typed-a)
            (assoc :domain :gravity/sh08-function-typed-core-v3)
            (dissoc :higher-order-proof))
        first-order-typed
        (-> typed-a
            (assoc :scope :first-order-fixed-arity-functions-locals-calls
                   :identity-input first-order-identity
                   :artifact-id-request first-order-identity)
            (update :typed-core dissoc
                    :higher-order-proof :higher-order-call-facts)
            (dissoc :higher-order-proof :higher-order-call-facts))
        first-order-verification (function-verification first-order-typed)
        first-order-result (build first-order-typed first-order-verification)
        unsupported-typed (assoc typed-a :scope :unsupported-function-scope)
        unsupported-result
        (build unsupported-typed (function-verification unsupported-typed))
        forged-first-order
        (assoc first-order-typed
               :identity-input
               (assoc first-order-identity :type-table {})
               :artifact-id-request
               (assoc first-order-identity :type-table {}))
        forged-first-order-result
        (build forged-first-order
               (function-verification forged-first-order))]
    (is (= :accepted (:status result-a) (:status result-b)))
    (is (= :declared-pure-call-effects-with-thrown-effects-pending
           (:scope result-a)))
    (is (= (:identity-input result-a) (:identity-input result-b)))
    (is (not= (:provenance result-a) (:provenance result-b)))
    (is (= 1 (count (:effect-requests result-a))))
    (is (= :accepted
           (get-in result-a [:effect-legality-results 0 :status])))
    (is (= #{}
           (get-in result-a [:effect-graph :nodes sha-d :direct])))
    (is (= [:pending-sh09]
           (get-in result-a [:effect-graph :functions sha-a :throws])))
    (is (= :passed
           (:status
            (invoke-c8
             'sh09-verify-authenticated-pure-effect-result
             [typed-a (function-verification typed-a) result-a]))))
    (is (= :accepted (:status first-order-result)))
    (is (= 1 (count (:call-type-facts first-order-typed))
           (count (:effect-requests first-order-result))))
    (is (= :passed
           (:status
            (invoke-c8
             'sh09-verify-authenticated-pure-effect-result
             [first-order-typed first-order-verification
              first-order-result]))))
    (is (= :rejected (:status unsupported-result)))
    (is (= :rejected (:status forged-first-order-result)))))

(deftest sh09-c7-adapter-binds-ordered-effect-identities
  (let [typed-a (function-typed-result "/checkout-a/function.gravity")
        typed-b (function-typed-result "/checkout-b/function.qst")
        verification-a (function-verification typed-a)
        verification-b (function-verification typed-b)
        effected-a (build typed-a verification-a)
        effected-b (build typed-b verification-b)
        template-a
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed-a verification-a effected-a])
        template-b
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed-b verification-b effected-b])
        requests (:requests template-a)
        resolved
        (mapv (fn [request digest]
                {:request request :digest digest})
              requests [sha-a sha-b sha-c])
        bound-a
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed-a verification-a effected-a resolved])
        bound-b
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed-b verification-b effected-b resolved])]
    (is (= :accepted (:status template-a) (:status template-b)))
    (is (= (:requests template-a) (:requests template-b)))
    (is (= [:type-fact :effect-fact :capability-proof]
           (mapv :kind requests)))
    (is (= [sha-d sha-d sha-d]
           (mapv :core-node-id requests)))
    (is (= :accepted (:status bound-a) (:status bound-b)))
    (is (= {:type-fact-id sha-a
            :effect-fact-id sha-b
            :capability-proof-id sha-c}
           (get-in bound-a [:fact-identities sha-d])))
    (is (= (:identity-input bound-a) (:identity-input bound-b)))
    (is (not= (:provenance bound-a) (:provenance bound-b)))
    (is (not (contains? (:identity-input bound-a) :provenance)))
    (is (= :passed
           (:status
            (invoke-c8
             'sh09-verify-authenticated-effect-identities
             [typed-a verification-a effected-a resolved bound-a]))))
    (doseq [candidate
            [(vec (reverse resolved))
             (assoc-in resolved [0 :digest] "not-a-digest")
             (assoc-in resolved [0 :unexpected] true)]]
      (is (= :rejected
             (:status
              (invoke-c8
               'sh09-bind-authenticated-effect-identities
               [typed-a verification-a effected-a candidate])))))
    (is (= :rejected
           (:status
            (invoke-c8
             'sh09-verify-authenticated-effect-identities
             [typed-a verification-a effected-a resolved
              (assoc-in bound-a
                        [:fact-identities sha-d :effect-fact-id]
                        sha-e)]))))))

(deftest sh09-c7-adapter-authenticated-gravity-boundary
  ;; This is a separately selected cold boundary. It reuses one authenticated
  ;; SH-08 .gravity carrier and never dereferences the co-canonical .qst twin.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (require 'gravity.self-hosting.sh08-primitive-function-type-test)
  (let [fixture-namespace
        'gravity.self-hosting.sh08-function-call-type-test
        primitive-namespace
        'gravity.self-hosting.sh08-primitive-function-type-test
        fixture-artifact
        (var-get (ns-resolve fixture-namespace 'fixture-artifact))
        function-request
        (var-get (ns-resolve fixture-namespace 'function-request))
        invoke-c7
        (var-get (ns-resolve primitive-namespace 'invoke-c7))
        artifact
        (fixture-artifact
         "accepted" "function-value-typed-bool" ".gravity")
        request (function-request artifact)
        typed
        (invoke-c7 'sh08-function-type-core-artifact [request])
        verification
        (invoke-c7
         'sh08-verify-function-type-result [request typed])
        result (build typed verification)
        graph (:effect-graph result)
        fresh
        (invoke-c8
         'sh09-verify-authenticated-pure-effect-result
         [typed verification result])
        candidates
        [(assoc result :scope :effectful)
         (assoc-in result
                   [:effect-graph :nodes
                    (first (keys (:nodes graph))) :direct]
                   #{:compiler/read-ir})
         (assoc-in result [:identity-input :effect-requests 0 :effect]
                   :compiler/read-ir)
         (assoc result :pending [])]]
    (is (= :passed (:status verification)))
    (is (= :accepted (:status typed)))
    (is (= :gravity/sh08-function-typed-core-template (:artifact typed)))
    (is (= :accepted (:status result)))
    (is (= :declared-pure-call-effects-with-thrown-effects-pending
           (:scope result)))
    (is (= (:sh07-shaped-artifact-id typed)
           (:sh08-shaped-artifact-id result)))
    (is (= (:typed-core typed) (:typed-core result)))
    (is (= (:type-table typed) (:type-table result)))
    (is (= (:function-type-table typed)
           (:function-type-table result)))
    (is (= (:local-binding-facts typed)
           (:local-binding-facts result)))
    (is (= (:call-type-facts typed) (:call-type-facts result)))
    (is (= (count (:call-type-facts typed)) (count (:nodes graph))))
    (is (= (count (:call-type-facts typed))
           (count (:effect-requests result))
           (count (:effect-legality-results result))))
    (is (every? #(= :accepted (:status %))
                (:effect-legality-results result)))
    (is (every? #(= #{} (:direct %) (:latent %) (:transitive %))
                (vals (:nodes graph))))
    (is (= #{} (get-in graph [:namespace :declared])))
    (is (= #{} (get-in graph [:namespace :inferred])))
    (is (= #{} (:residual-effects graph)))
    (is (= (set (map :function-id (:function-type-table typed)))
           (set (keys (:functions graph)))))
    (is (every? #(= [:pending-sh09] (:throws %))
                (vals (:functions graph))))
    (is (= (:provenance typed) (:provenance result)))
    (is (not (contains? (:identity-input result) :provenance)))
    (is (= :passed (:status fresh)))
    (doseq [candidate candidates]
      (is (= :rejected
             (:status
              (invoke-c8
               'sh09-verify-authenticated-pure-effect-result
               [typed verification candidate])))))))
