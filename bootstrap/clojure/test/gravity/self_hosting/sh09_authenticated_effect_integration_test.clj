(ns gravity.self-hosting.sh09-authenticated-effect-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-authenticated-type-integration-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh09_authenticated_effect_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-09 authenticated integration test is not on the classpath"
        {:id "SH09-AUTH-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH09-AUTH-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- compile-plan
  [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private envelope-source
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")
(def ^:private c8-source
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-09/"
   "authenticated_effect_bridge.gravity"))

(def ^:private envelope-plan (delay (compile-plan envelope-source)))
(def ^:private c8-plan (delay (compile-plan c8-source)))
(def ^:private bridge-plan (delay (compile-plan bridge-source)))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh09-authenticated-effect-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- sh08-var
  [name]
  (or
   (ns-resolve
    'gravity.self-hosting.sh08-authenticated-type-integration-test
    name)
   (throw
    (ex-info
     "Required SH-08 integration helper is unavailable"
     {:id "SH09-AUTH-SH08-HELPER" :name name}))))

(defn- sh08-value
  [name]
  (var-get (sh08-var name)))

(defn- sh08-call
  [name & arguments]
  (apply (sh08-value name) arguments))

(defn- canonical-id
  [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh09-authenticated-effect-integration>" value))

(defn- identity-id
  [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- source-hash
  [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count
  [source-text]
  (alength
   (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- plan-semantic-id
  [plan]
  (bootstrap/p15-s23-c11-mir-digest
   (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan)))

(defn- function-shapes
  [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

(def ^:private envelope-bounds
  {:maximum-semantic-projections 64
   :maximum-fact-transitions 64
   :maximum-identity-subjects 64
   :maximum-lineage-records 32
   :maximum-reference-nodes 128
   :maximum-reference-edges 128
   :maximum-reference-depth 64
   :maximum-logical-source-path-code-units 128
   :maximum-reference-id-code-units 128
   :maximum-digest-requests 2048
   :maximum-carrier-nodes 65536
   :maximum-carrier-depth 64
   :maximum-container-width 128
   :maximum-scalar-bytes 65536
   :maximum-integer-bits 256})

(defn- bridge-source-revision
  []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        shapes (function-shapes plan)
        builder 'sh09-build-authenticated-effect-request]
    {:owner :sh-effects
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-09/authenticated-effect-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash (plan-semantic-id plan)
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) builder))
     :function-shapes shapes}))

(defn- authenticated-sh08-products
  [extension]
  (let [products
        (sh08-call
         'cached-fixture-products "accepted" extension)
        actual-path
        (str "/checkout-a/sh08-input" extension)
        descriptor
        (sh08-call 'descriptor products actual-path)
        authenticated
        (sh08-call 'authenticated-envelope descriptor)
        request
        (sh08-call
         'request products descriptor authenticated actual-path)
        run (sh08-call 'run-bridge request)
        verification
        (sh08-call
         'invoke
         (sh08-value 'bridge-plan)
         'sh08-verify-authenticated-type-result
         [request
          (:c7-request run)
          (:type-template run)
          (:type-verification run)
          (:result run)])]
    {:typed-core (:result run)
     :verification verification
     :source-extension extension}))

(def ^:private sh08-gravity
  (delay (authenticated-sh08-products ".gravity")))

(def ^:private sh08-qst
  (delay (authenticated-sh08-products ".qst")))

(defn- effect-authority
  [typed operation]
  (let [effectful? (not= operation :pure)
        namespace (get-in typed [:module :namespace])
        subject
        (when effectful?
          {:ir-level :typed-core
           :typed-artifact-id (:artifact-id typed)})]
    {:effect (when effectful? :compiler/read-ir)
     :required-capability
     (when effectful? :compiler/ir-read)
     :declared-effects
     (if effectful? #{:compiler/read-ir} #{})
     :package-effects
     (if effectful? #{:compiler/read-ir} #{})
     :deployment-effects
     (if effectful? #{:compiler/read-ir} #{})
     :declared-capabilities
     (if effectful? #{:compiler/ir-read} #{})
     :package-capabilities
     (if effectful? #{:compiler/ir-read} #{})
     :deployment-capabilities
     (if effectful? #{:compiler/ir-read} #{})
     :provider
     (when effectful?
       {:id :gravity.compiler/read-only-ir
        :effects #{:compiler/read-ir}
        :capabilities #{:compiler/ir-read}
        :profiles #{:meta}
        :targets #{:jvm}
        :phases #{:build}})
     :grant
     (when effectful?
       {:id :authenticated-compiler-read-grant
        :principal namespace
        :capability :compiler/ir-read
        :provider :gravity.compiler/read-only-ir
        :phase :build
        :scope subject})
     :resource-subject subject
     :build-policy
     {:hermetic true
      :allowed-effects
      (if effectful? #{:compiler/read-ir} #{})}
     :safety-allowed true
     :authority-mode
     (if effectful?
       (if (= operation :ambient-read) :ambient :explicit)
       :none)
     :replay-record nil
     :ordering (when effectful? :sequence)}))

(defn- c8-products
  [typed operation]
  (let [authority (effect-authority typed operation)
        effect-request
        (invoke
         bridge-plan
         'sh09-bridge-effect-request
         [typed operation authority])
        effect-result
        (invoke
         c8-plan 'sh09-check-effect-request
         [effect-request])
        effect-verification
        (invoke
         c8-plan 'sh09-verify-effect-result
         [effect-request effect-result])]
    {:effect-authority authority
     :effect-request effect-request
     :effect-result effect-result
     :effect-verification effect-verification}))

(defn- fact-transition
  [name value evidence-id]
  {:name name
   :disposition :preserved
   :input value
   :output value
   :input-count (count value)
   :output-count (count value)
   :evidence-ids [evidence-id]})

(defn- descriptor
  [sh08-products c8-products operation actual-path]
  (let [typed (:typed-core sh08-products)
        effect-result (:effect-result c8-products)
        typed-envelope-identity
        {:typed-artifact-id (:artifact-id typed)}
        effect-artifact-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh09-effect-legality-result-v2
          :result effect-result})
        effect-envelope-identity
        {:effect-result-id effect-artifact-id}
        typed-subject-id
        (identity-id
         :gravity/sh09-sh08-typed-core-v1
         typed-envelope-identity)
        effect-subject-id
        (identity-id
         :gravity/sh09-effect-result-v1
         effect-envelope-identity)
        evidence-id
        (canonical-id
         {:domain :gravity/sh09-authenticated-effect-evidence-v1
          :typed-subject-id typed-subject-id
          :effect-subject-id effect-subject-id
          :sh08-status
          (:status (:verification sh08-products))
          :c8-status
          (:status (:effect-verification c8-products))})
        fact-names [:type :effect :capability :ownership :safety]
        fact-values
        {:type {:typed-artifact-id (:artifact-id typed)}
         :effect
         {:direct-effects (:direct-effects effect-result)
          :residual-effects (:residual-effects effect-result)}
         :capability
         {:proof (:capability-proof effect-result)}
         :ownership {:status :pending-sh10}
         :safety {:status :preserved-from-sh08}}
        facts
        (mapv
         #(fact-transition % (get fact-values %) evidence-id)
         fact-names)]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh09-authenticated-effect-bridge
     :artifact-kind :gravity/sh09-authenticated-effect-boundary
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind
      :gravity/sh09-authenticated-effect-boundary-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh08-typed-core :sh09-effect-result]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh08-typed-core :sh09-effect-result]}
     :semantic-projections
     [{:name :sh08-typed-core
       :role :authenticated-type-input
       :entry-count (count typed-envelope-identity)
       :value typed-envelope-identity}
      {:name :sh09-effect-result
       :role :fresh-effect-legality-result
       :entry-count (count effect-envelope-identity)
       :value effect-envelope-identity}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts
      {:declared
       (get-in c8-products
               [:effect-request :declared-effects])
       :observed (:direct-effects effect-result)}
      :capability-facts
      {:required
       (get-in c8-products
               [:effect-request :declared-capabilities])
       :granted
       (get-in c8-products
               [:effect-request :deployment-capabilities])}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order
      (if (= nil (:ordering effect-result))
        []
        [(:ordering effect-result)])
      :provider-selections
      (if (= nil
             (get-in c8-products
                     [:effect-request :provider]))
        []
        [(get-in c8-products
                 [:effect-request :provider :id])])
      :grant-scopes
      (if (= nil
             (get-in c8-products
                     [:effect-request :grant]))
        []
        [(get-in c8-products
                 [:effect-request :grant :scope])])}
     :proof-composite
     {:proof-records
      [{:proof-id evidence-id
        :status :host-replayed
        :sh08-verification-status
        (:status (:verification sh08-products))
        :c8-verification-status
        (:status (:effect-verification c8-products))}]
      :proof-certificate-table
      {evidence-id
       {:status :host-replayed
        :sh08-verification-status
        (:status (:verification sh08-products))
        :c8-verification-status
        (:status (:effect-verification c8-products))}}
      :proof-summary {:required 2 :checked 2}
      :proof-usage
      [{:proof-id evidence-id
        :used-by :authenticated-effect-boundary}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:fresh-sh08-verification
       :fresh-c8-result
       :fresh-c8-verification]}
     :identity-subjects
     [{:name :sh08-typed-core
       :domain :gravity/sh09-sh08-typed-core-v1
       :preimage typed-envelope-identity
       :observed-id typed-subject-id}
      {:name :sh09-effect-result
       :domain :gravity/sh09-effect-result-v1
       :preimage effect-envelope-identity
       :observed-id effect-subject-id}]
     :lineage
     [{:stage :sh08-type-checking
       :artifact-kind :gravity/sh08-authenticated-typed-core
       :semantic-id typed-subject-id
       :artifact-id (:artifact-id typed)
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh09-effect-checking
       :artifact-kind :gravity/sh09-effect-legality-result
       :semantic-id effect-subject-id
       :artifact-id
       (canonical-id
        {:domain :gravity/sh09-effect-artifact-v1
         :semantic-id effect-subject-id})
       :verification-id evidence-id
       :relation :freshly-recomputed}]
     :reference-closure
     {:root-id "sh09-effect-bridge"
      :node-ids
      ["sh09-effect-bridge" "sh08-typed-core" "sh09-effect-result"]
      :edges
      [{:from "sh09-effect-bridge"
        :role :consumes :to "sh08-typed-core"}
       {:from "sh09-effect-bridge"
        :role :produces :to "sh09-effect-result"}]
      :fact-reference-ids
      ["fact/type" "fact/effect" "fact/capability"
       "fact/ownership" "fact/safety"]
      :origin-reference-ids ["origin/sh08"]
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 3
      :observed-edge-count 2
      :observed-maximum-depth 1}
     :actual-path-provenance
     {:source-path actual-path
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- authenticated-envelope
  [descriptor sh08-verification]
  (let [raw
        (invoke
         envelope-plan
         'authenticated-envelope-build-template
         [descriptor])
        replay
        (invoke
         envelope-plan
         'authenticated-envelope-verify-template
         [descriptor
          (:artifact-template raw)
          (:digest-requests raw)])
        sealed (sh08-call 'seal-builder-result! raw)]
    {:envelope (:template sealed)
     :template-replay replay
     :verification
     {:artifact :gravity/sh09-envelope-contextual-verification
      :status :contextual-verification-passed
      :artifact-template (:template sealed)
      :semantic-envelope-root (:semantic-root sealed)
      :provenance-binding-root (:provenance-root sealed)
      :identity-checks (:identity-checks sealed)
      :identity-enforcement :passed
      :eligible-for-contextual-acceptance? true
      :host-digest-resolution :passed
      :identity-subject-equality :passed
      :fresh-envelope-reconstruction :passed
      :sh08-verification sh08-verification}
     :semantic-root (:semantic-root sealed)
     :provenance-root (:provenance-root sealed)}))

(defn- request
  [sh08-products c8-products operation descriptor
   authenticated actual-source-path]
  {:artifact :gravity/sh09-authenticated-effect-bridge-request
   :schema-version 1
   :operation operation
   :effect-authority (:effect-authority c8-products)
   :sh08-typed-core (:typed-core sh08-products)
   :sh08-verification (:verification sh08-products)
   :expected-effect-identity-input
   (:identity-input (:effect-result c8-products))
   :expected-effect-result
   (:effect-result c8-products)
   :expected-effect-artifact-id
   (bootstrap/reader-canonical-hash
    {:domain :gravity/sh09-effect-legality-result-v2
     :result (:effect-result c8-products)})
   :envelope-descriptor descriptor
   :envelope (:envelope authenticated)
   :template-replay (:template-replay authenticated)
   :envelope-verification (:verification authenticated)
   :actual-source-path actual-source-path})

(defn- prepared-request
  [sh08-products operation actual-path]
  (let [c8 (c8-products (:typed-core sh08-products) operation)
        descriptor
        (descriptor sh08-products c8 operation actual-path)
        authenticated
        (authenticated-envelope
         descriptor (:verification sh08-products))]
    {:request
     (request
      sh08-products c8 operation descriptor authenticated actual-path)
     :descriptor descriptor
     :authenticated authenticated
     :preflight-c8 c8}))

(defn- run-bridge
  [prepared]
  (let [request (:request prepared)
        c8-request
        (invoke
         bridge-plan
         'sh09-build-authenticated-effect-request
         [request])
        fresh-result
        (invoke
         c8-plan 'sh09-check-effect-request
         [(:effect-request c8-request)])
        fresh-verification
        (invoke
         c8-plan 'sh09-verify-effect-result
         [(:effect-request c8-request) fresh-result])
        result
        (invoke
         bridge-plan
         'sh09-bind-authenticated-effect-result
         [request c8-request fresh-result fresh-verification])]
    {:c8-request c8-request
     :effect-result fresh-result
     :effect-verification fresh-verification
     :result result}))

(deftest sh09-authenticated-effect-bridge-compiles-with-narrow-api
  (doseq [plan [envelope-plan c8-plan bridge-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is (= {:arity 0 :params []}
           (select-keys
            (get functions 'sh09-authenticated-effect-bridge-policy)
            [:arity :params])))
    (is (= {:arity 1 :params ['request]}
           (select-keys
            (get functions 'sh09-build-authenticated-effect-request)
            [:arity :params])))
    (is (= {:arity 4
            :params
            ['request 'c8-request
             'effect-result 'effect-verification]}
           (select-keys
            (get functions 'sh09-bind-authenticated-effect-result)
            [:arity :params])))
    (is (= {:arity 5
            :params
            ['request 'c8-request 'effect-result
             'effect-verification 'candidate]}
           (select-keys
            (get functions 'sh09-verify-authenticated-effect-result)
            [:arity :params])))))

(deftest sh09-accepts-pure-and-explicit-compiler-read
  (doseq [[extension sh08-products]
          [[".gravity" @sh08-gravity]
           [".qst" @sh08-qst]]]
    (doseq [operation [:pure :compiler-read]]
      (let [prepared
            (prepared-request
             sh08-products operation
             (str "/checkout-a/effect-input" extension))
            run (run-bridge prepared)
            result (:result run)]
        (is (= :accepted (:status (:c8-request run)))
            [extension operation])
        (is (= :accepted (:status (:effect-result run)))
            [extension operation])
        (is (= :passed (:status (:effect-verification run)))
            [extension operation])
        (is (= :accepted (:status result))
            [extension operation])
        (is
         (=
          (:effect-authority (:request prepared))
          (select-keys
           (get-in run [:c8-request :effect-request])
           [:effect :required-capability
            :declared-effects :package-effects
            :deployment-effects :declared-capabilities
            :package-capabilities :deployment-capabilities
            :provider :grant :resource-subject :build-policy
            :safety-allowed :authority-mode
            :replay-record :ordering]))
         [extension operation])
        (is (= operation (:operation result)))
        (if (= operation :pure)
          (do
            (is (= #{} (get-in result
                               [:effect-result :direct-effects])))
            (is (nil? (get-in result
                              [:effect-result :capability-proof]))))
          (do
            (is (= #{:compiler/read-ir}
                   (get-in result
                           [:effect-result :direct-effects])))
            (is (= :compiler/ir-read
                   (get-in result
                           [:effect-result :capability-proof
                            :capability])))))
        (is (= :passed
               (:status
                (invoke
                 bridge-plan
                 'sh09-verify-authenticated-effect-result
                 [(:request prepared)
                  (:c8-request run)
                  (:effect-result run)
                  (:effect-verification run)
                  result]))))))))

(deftest sh09-rejects-ambient-authority-for-both-source-extensions
  (doseq [[extension sh08-products]
          [[".gravity" @sh08-gravity]
           [".qst" @sh08-qst]]]
    (let [prepared
          (prepared-request
           sh08-products :ambient-read
           (str "/checkout-a/ambient-input" extension))
          run (run-bridge prepared)
          result (:result run)]
      (is (= :accepted (:status (:c8-request run))) extension)
      (is (= :rejected (:status (:effect-result run))) extension)
      (is (= :passed (:status (:effect-verification run))) extension)
      (is (= :rejected (:status result)) extension)
      (is (= "STD09-BRIDGE-EFFECT"
             (get-in result [:diagnostics 0 :rule]))
          extension)
      (is (= "C8-CAPABILITY"
             (get-in result
                     [:diagnostics 0 :facts
                      :upstream-diagnostics 0 :rule]))
          extension))))

(deftest sh09-authenticated-effect-bridge-rejects-alteration
  (let [prepared
        (prepared-request
         @sh08-gravity :compiler-read
         "/checkout-a/effect-input.gravity")
        request (:request prepared)
        accepted (run-bridge prepared)
        altered-typed
        (assoc-in
         request [:sh08-typed-core :artifact-id]
         (str "sha256:" (apply str (repeat 64 "f"))))
        altered-envelope
        (assoc-in
         request [:envelope :semantic-envelope-id]
         (str "sha256:" (apply str (repeat 64 "e"))))
        shallow-sh08-verification
        (assoc-in
         request [:sh08-verification :checks]
         [:status-only-substitution])
        altered-authority
        (assoc-in
         request [:effect-authority :provider :id]
         :gravity.compiler/unbound-provider)
        altered-effect
        (assoc (:effect-result accepted)
               :direct-effects #{:build/network})
        shallow-c8-verification
        (assoc (:effect-verification accepted)
               :checks [:complete-recomputation])
        altered-result
        (assoc (:result accepted) :status :rejected)]
    (doseq [[label candidate expected-rule]
            [[:typed altered-typed "STD09-BRIDGE-SH08"]
             [:sh08-verification shallow-sh08-verification
              "STD09-BRIDGE-SH08"]
             [:authority altered-authority
              "STD09-BRIDGE-EFFECT"]
             [:envelope altered-envelope
              "STD09-BRIDGE-ENVELOPE"]]]
      (testing (name label)
        (let [result
              (invoke
               bridge-plan
               'sh09-build-authenticated-effect-request
               [candidate])]
          (is (= :rejected (:status result)))
          (is (= expected-rule
                 (get-in result [:diagnostics 0 :rule]))))))
    (let [effect-rejection
          (invoke
           bridge-plan
           'sh09-bind-authenticated-effect-result
           [request
            (:c8-request accepted)
            altered-effect
            (:effect-verification accepted)])
          result-rejection
          (invoke
           bridge-plan
           'sh09-verify-authenticated-effect-result
           [request
            (:c8-request accepted)
            (:effect-result accepted)
            (:effect-verification accepted)
            altered-result])
          verification-rejection
          (invoke
           bridge-plan
           'sh09-bind-authenticated-effect-result
           [request
            (:c8-request accepted)
            (:effect-result accepted)
            shallow-c8-verification])]
      (is (= :rejected (:status effect-rejection)))
      (is (= "STD09-BRIDGE-VERIFY"
             (get-in effect-rejection
                     [:diagnostics 0 :rule])))
      (is (= :rejected (:status verification-rejection)))
      (is (= "STD09-BRIDGE-VERIFY"
             (get-in verification-rejection
                     [:diagnostics 0 :rule])))
      (is (= :rejected (:status result-rejection))))))

(deftest sh09-identity-is-path-neutral-with-separate-provenance
  (let [sh08-products @sh08-gravity
        prepared-a
        (prepared-request
         sh08-products :compiler-read
         "/checkout-a/effect-input.gravity")
        prepared-b
        (prepared-request
         sh08-products :compiler-read
         "/checkout-b/effect-input.gravity")
        result-a (:result (run-bridge prepared-a))
        result-b (:result (run-bridge prepared-b))]
    (is (= (get-in prepared-a
                   [:authenticated :semantic-root])
           (get-in prepared-b
                   [:authenticated :semantic-root])))
    (is (not= (get-in prepared-a
                      [:authenticated :provenance-root])
              (get-in prepared-b
                      [:authenticated :provenance-root])))
    (is (= (:identity-input result-a)
           (:identity-input result-b)))
    (is (= (:artifact-id result-a)
           (:artifact-id result-b)))
    (is (not= (:provenance result-a)
              (:provenance result-b)))))

(deftest sh09-authenticated-effect-bridge-fails-closed-on-malformed-input
  (let [over-depth-carrier
        (nth (iterate vector nil) 130)]
    (doseq [value
            [nil
             {}
             {:artifact
              :gravity/sh09-authenticated-effect-bridge-request}
             {:artifact
              :gravity/sh09-authenticated-effect-bridge-request
              :schema-version 1
              :unexpected true}
             over-depth-carrier]]
      (let [result
            (invoke
             bridge-plan
             'sh09-build-authenticated-effect-request
             [value])]
        (is (= :rejected (:status result)))
        (is (= "STD09-BRIDGE-SCHEMA"
               (get-in result [:diagnostics 0 :rule])))))))
