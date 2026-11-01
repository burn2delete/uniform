(ns gravity.self-hosting.sh09-c7-effect-adapter-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(def ^:private c8-plan kernel-support/c8-plan)

(defn- invoke-c8
  [function arguments]
  (kernel-support/invoke-c8 function arguments))

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
  (kernel-support/function-typed-result actual-path))

(defn- function-verification
  [typed]
  (kernel-support/function-verification typed))

(defn- build
  [typed verification]
  (invoke-c8
   'sh09-build-authenticated-pure-effect-result
   [typed verification]))

(deftest sh09-atomic-signature-identity-development-kernel
  ;; Normalized adapter inputs exercise identity reconstruction, not C7 proof.
  (let [higher-order (function-typed-result "/development/function.gravity")
        first-order
        (-> higher-order
            (assoc :scope :first-order-fixed-arity-functions-locals-calls)
            (dissoc :higher-order-proof :higher-order-call-facts)
            (update :typed-core dissoc :higher-order-proof :higher-order-call-facts))
        declaration {:source-child-index 1 :marker-form-id sha-a :marker-syntax-id sha-b
                     :type-form-id sha-c :type-syntax-id sha-d :type-symbol 'Bool
                     :type-binding-id sha-e :type-id :gravity.type/bool}
        signature {:rule-version :sh07-atomic-function-signature-v1
                   :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameter-source-child-indexes [0] :parameters [declaration]
                   :return (assoc declaration :source-child-index 2) :body-source-child-index 4}
        constraint {:constraint-id-request
                    {:domain :gravity/sh08-atomic-function-declaration-constraint-v1
                     :function-core-node-id sha-b :role :parameter :ordinal 0
                     :declaration declaration}
                    :kind :equality :producer-rule :sh08-atomic-function-signature-v1
                    :lhs sha-e :rhs :gravity.type/bool :source-syntax-id sha-d
                    :profile :meta :target :jvm :status :solved :diagnostics []}
        raw (-> first-order
                (assoc-in [:function-products :function-records] [{:signature signature}])
                (assoc-in [:function-type-table 0 :signature] signature)
                (assoc-in [:function-type-table 0 :declaration-rule-version]
                          :sh08-atomic-function-signature-v1)
                (assoc :constraint-ledger [constraint]))
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity
                     :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (function-verification typed)
        result (build typed verification)]
    (is (= :gravity/sh08-function-typed-core-v3
           (invoke-c8 'sh09-adapter-function-identity-domain [first-order])))
    (is (= :gravity/sh08-authoritative-higher-order-type-v1
           (invoke-c8 'sh09-adapter-function-identity-domain
                      [(assoc higher-order :function-products (:function-products raw))])))
    (is (= :gravity/sh08-atomic-function-signature-typed-core-v1 (:domain identity)))
    (is (= signature (get-in identity [:function-records 0 :signature])))
    (is (= [constraint] (:constraint-ledger identity)))
    (is (= :accepted (:status result)))
    (is (= (:typed-core typed) (:typed-core result)))
    (is (= (:function-type-table typed) (:function-type-table result)))
    (is (= [constraint] (get-in result [:typed-core :constraints])))
    (is (= identity (get-in result [:identity-input :sh08-identity-input])))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result
                                       [typed verification result]))))
    (doseq [altered [(-> typed
                        (assoc-in [:identity-input :domain] :gravity/sh08-function-typed-core-v3)
                        (assoc-in [:artifact-id-request :domain] :gravity/sh08-function-typed-core-v3))
                     (update-in typed [:function-products :function-records 0] dissoc :signature)
                     (update-in typed [:function-type-table 0] dissoc :signature)
                     (assoc-in typed [:function-products :function-records 0 :signature :return :type-id]
                               :gravity.type/i64)
                     (assoc typed :constraint-ledger [])]]
      (is (= :rejected (:status (build altered (function-verification altered))))))
    (doseq [records [0 nil {} [nil] [0] [{}]]]
      (let [altered (assoc-in typed [:function-products :function-records] records)]
        (is (= :gravity/sh08-function-typed-core-v3
               (invoke-c8 'sh09-adapter-function-identity-domain [altered])))
        (is (= :rejected (:status (build altered (function-verification altered)))))))
    (doseq [candidate [(update-in result [:function-type-table 0] dissoc :signature)
                       (assoc-in result [:typed-core :constraints] [])
                       (assoc-in result [:identity-input :sh08-identity-input :function-records] [])]]
      (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result
                                         [typed verification candidate])))))
    (is (= :rejected (:status (build typed (assoc verification :status :rejected)))))
    (is (= :rejected (:status (build typed (assoc verification :candidate raw)))))))

(deftest sh09-c7-adapter-source-structure-and-policy-are-exact
  (let [functions (:functions (kernel-support/module-root-plan @c8-plan))
        policy
        (invoke-c8 'sh09-authenticated-sh08-adapter-policy [])]
    (is (= :gravity/stage2-compiler-artifact-module-bundle (:kind @c8-plan)))
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

(deftest sh09-imported-type-only-diagnostic-development-kernel
  ;; DATA-only selection check; authentic C7/C8 composition lives in SH08.
  (let [base (function-typed-result "imported-types.gravity")
        local (assoc (first (:call-type-facts base)) :dispatch :direct-local-function)
        imported (assoc local :call-core-node-id sha-a :syntax-id sha-b :dispatch :direct-imported-function
                              :source-span {:source "imported-types.gravity" :start-byte 9 :end-byte 10})
        typed (assoc base :scope :direct-imported-functions-types-only :call-type-facts [local imported])
        result (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed (upstream-verification typed)])
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= :imported-body-effects-pending (:reason diagnostic)))
    (is (= sha-a (:core-node-id diagnostic)))
    (is (= sha-b (:syntax-id diagnostic)))
    (is (= (:source-span imported) (:source-span diagnostic)))
    (is (every? #(some? (get diagnostic %)) [:profile :target :namespace :function :remediation]))
    (let [top-level (assoc typed :call-type-facts [(assoc imported :caller-function-syntax-id nil)])
          d (first (:diagnostics (invoke-c8 'sh09-build-authenticated-pure-effect-result [top-level {}])))]
      (is (= (get-in typed [:module :namespace]) (:function d))))))

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
