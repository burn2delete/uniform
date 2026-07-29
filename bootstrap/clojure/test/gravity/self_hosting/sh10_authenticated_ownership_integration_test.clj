(ns gravity.self-hosting.sh10-authenticated-ownership-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-authenticated-type-integration-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh10_authenticated_ownership_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-10 authenticated integration test is not on the classpath"
        {:id "SH10-AUTH-TEST-SOURCE"})))
    (loop [candidate
           (.getParent
            (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH10-AUTH-REPOSITORY-ROOT"}))

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

(def ^:private envelope-source
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")

(def ^:private c9-source
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")

(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-10/"
   "authenticated_ownership_bridge.gravity"))

(def ^:private envelope-plan
  (delay (compile-plan envelope-source)))

(def ^:private c9-plan
  (delay (compile-plan c9-source)))

(def ^:private bridge-plan
  (delay (compile-plan bridge-source)))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-authenticated-ownership-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- sh08-var [name]
  (or
   (ns-resolve
    'gravity.self-hosting.sh08-authenticated-type-integration-test
    name)
   (throw
    (ex-info
     "Required SH-08 integration helper is unavailable"
     {:id "SH10-AUTH-SH08-HELPER" :name name}))))

(defn- sh08-value [name]
  (var-get (sh08-var name)))

(defn- sh08-call [name & arguments]
  (apply (sh08-value name) arguments))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh10-authenticated-ownership-integration>" value))

(defn- identity-id [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- source-hash [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count [source-text]
  (alength
   (.getBytes
    source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- plan-semantic-id [plan]
  (bootstrap/p15-s23-c11-mir-digest
   (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
    plan)))

(defn- function-shapes [plan]
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

(defn- bridge-source-revision []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        builder 'sh10-build-authenticated-ownership-request]
    {:owner :sh-ownership
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-10/authenticated-ownership-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash (plan-semantic-id plan)
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) builder))
     :function-shapes (function-shapes plan)}))

(defn- authenticated-sh08-products [extension]
  (let [products
        (sh08-call
         'cached-fixture-products "accepted" extension)
        actual-path (str "/checkout-a/sh08-input" extension)
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

(defn- fact-transition [name value evidence-id]
  {:name name
   :disposition :preserved
   :input value
   :output value
   :input-count (count value)
   :output-count (count value)
   :evidence-ids [evidence-id]})

(defn- ownership-links [typed]
  (let [fact (first (:type-facts typed))]
    {:type-fact-id
     (identity-id
      :gravity/sh10-type-fact-link-v1
      {:fact fact})
     :effect-fact-id
     (identity-id
      :gravity/sh10-effect-fact-link-v1
      {:effects (:effects fact)})
     :capability-proof-id
     (identity-id
      :gravity/sh10-capability-proof-link-v1
      {:capabilities (:capabilities fact)})}))

(defn- ownership-operation [operation]
  {:operation operation
   :type-fact-index 0
   :owner-id "sh10-owner-local"
   :ownership-kind :owned-mutable
   :initialization :uninitialized
   :availability :available
   :owner-lifetime-end 64
   :immutable-borrow-id :borrow-a
   :mutable-borrow-id :borrow-b
   :destination-owner "sh10-owner-destination"})

(defn- normalized-ownership-request
  [typed ownership-operation links]
  (invoke
   bridge-plan
   'sh10-bridge-ownership-request
   [typed
    ownership-operation
    (:type-fact-id links)
    (:effect-fact-id links)
    (:capability-proof-id links)]))

(defn- c9-products [typed ownership-operation links]
  (let [request
        (normalized-ownership-request
         typed ownership-operation links)
        result
        (invoke
         c9-plan 'sh10-check-ownership-request [request])
        verification
        (invoke
         c9-plan
         'sh10-verify-ownership-result [request result])]
    {:ownership-request request
     :ownership-result result
     :ownership-verification verification}))

(defn- descriptor
  [sh08-products c9-products ownership-operation links
   actual-path]
  (let [typed (:typed-core sh08-products)
        ownership-result (:ownership-result c9-products)
        typed-envelope-identity
        {:typed-artifact-id (:artifact-id typed)}
        ownership-artifact-id
        (identity-id
         :gravity/sh10-ownership-result-v2
         ownership-result)
        ownership-operation-id
        (identity-id
         :gravity/sh10-ownership-operation-v1
         ownership-operation)
        ownership-envelope-identity
        {:ownership-result-id ownership-artifact-id}
        typed-subject-id
        (identity-id
         :gravity/sh10-sh08-typed-core-v1
         {:typed-artifact-id (:artifact-id typed)
          :identity-input (:identity-input typed)})
        ownership-subject-id
        (identity-id
         :gravity/sh10-ownership-result-v2
         ownership-result)
        evidence-id
        (canonical-id
         {:domain
          :gravity/sh10-authenticated-ownership-evidence-v1
          :typed-subject-id typed-subject-id
          :ownership-subject-id ownership-subject-id
          :operation (:operation ownership-operation)
          :sh08-status
          (:status (:verification sh08-products))
          :c9-status
          (:status (:ownership-verification c9-products))})
        fact-names
        [:type :effect :capability :ownership :safety]
        fact-values
        {:type {:fact-id (:type-fact-id links)}
         :effect {:fact-id (:effect-fact-id links)}
         :capability
         {:proof-id (:capability-proof-id links)}
         :ownership
         {:result-id ownership-subject-id}
         :safety {:status :pending-sh11}}
        facts
        (mapv
         #(fact-transition % (get fact-values %) evidence-id)
         fact-names)]
    {:artifact
     :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh10-authenticated-ownership-bridge
     :artifact-kind
     :gravity/sh10-authenticated-ownership-boundary
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind
      :gravity/sh10-authenticated-ownership-boundary-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh08-typed-core :sh10-ownership-result
       :sh10-ownership-verification
       :sh10-ownership-operation]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh08-typed-core :sh10-ownership-result
       :sh10-type-fact-link :sh10-effect-fact-link
       :sh10-capability-proof-link
       :sh10-ownership-operation]}
     :semantic-projections
     [{:name :sh08-typed-core
       :role :verified-typed-core-identity
       :entry-count (count typed-envelope-identity)
       :value typed-envelope-identity}
      {:name :sh10-ownership-result
       :role :fresh-ownership-result-identity
       :entry-count (count ownership-envelope-identity)
       :value ownership-envelope-identity}
      {:name :sh10-ownership-verification
       :role :fresh-ownership-verification
       :entry-count (count
                     (:ownership-verification c9-products))
       :value (:ownership-verification c9-products)}
      {:name :sh10-ownership-operation
       :role :explicit-ownership-operation
       :entry-count (count ownership-operation)
       :value ownership-operation}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts
      {:declared (set (get-in typed [:module :effects]))
       :observed (set (get-in typed [:module :effects]))}
      :capability-facts
      {:required (set (get-in typed [:module :capabilities]))
       :granted (set (get-in typed [:module :capabilities]))}
      :capability-proof-facts
      {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records
      [{:proof-id evidence-id
        :status :host-replayed
        :sh08-verification-status
        (:status (:verification sh08-products))
        :c9-verification-status
        (:status (:ownership-verification c9-products))}]
      :proof-certificate-table
      {evidence-id
       {:status :host-replayed
        :sh08-verification-status
        (:status (:verification sh08-products))
        :c9-verification-status
        (:status (:ownership-verification c9-products))}}
      :proof-summary {:required 2 :checked 2}
      :proof-usage
      [{:proof-id evidence-id
        :used-by :authenticated-ownership-boundary}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:fresh-sh08-verification
       :fresh-c9-result
       :fresh-c9-verification]}
     :identity-subjects
      [{:name :sh08-typed-core
       :domain :gravity/sh10-sh08-typed-core-v1
       :preimage
       {:typed-artifact-id (:artifact-id typed)
        :identity-input (:identity-input typed)}
       :observed-id typed-subject-id}
      {:name :sh10-ownership-result
       :domain :gravity/sh10-ownership-result-v2
       :preimage ownership-result
       :observed-id ownership-artifact-id}
      {:name :sh10-type-fact-link
       :domain :gravity/sh10-type-fact-link-v1
       :preimage {:fact (first (:type-facts typed))}
       :observed-id (:type-fact-id links)}
      {:name :sh10-effect-fact-link
       :domain :gravity/sh10-effect-fact-link-v1
       :preimage
       {:effects (:effects (first (:type-facts typed)))}
       :observed-id (:effect-fact-id links)}
      {:name :sh10-capability-proof-link
       :domain :gravity/sh10-capability-proof-link-v1
       :preimage
       {:capabilities
        (:capabilities (first (:type-facts typed)))}
       :observed-id (:capability-proof-id links)}
      {:name :sh10-ownership-operation
       :domain :gravity/sh10-ownership-operation-v1
       :preimage ownership-operation
       :observed-id ownership-operation-id}]
     :lineage
     [{:stage :sh08-type-checking
       :artifact-kind :gravity/sh08-authenticated-typed-core
       :semantic-id typed-subject-id
       :artifact-id (:artifact-id typed)
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh10-ownership-checking
       :artifact-kind :gravity/sh10-ownership-analysis-result
       :semantic-id ownership-subject-id
       :artifact-id ownership-artifact-id
       :verification-id evidence-id
       :relation :freshly-recomputed}]
     :reference-closure
     {:root-id "sh10-ownership-bridge"
      :node-ids
      ["sh10-ownership-bridge"
       "sh08-typed-core"
       "sh10-ownership-result"]
      :edges
      [{:from "sh10-ownership-bridge"
        :role :consumes :to "sh08-typed-core"}
       {:from "sh10-ownership-bridge"
        :role :produces :to "sh10-ownership-result"}]
      :fact-reference-ids
      [(:type-fact-id links)
       (:effect-fact-id links)
       (:capability-proof-id links)]
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
     {:artifact
      :gravity/sh10-envelope-contextual-verification
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
  [sh08-products c9-products ownership-operation links descriptor
   authenticated actual-source-path]
  {:artifact
   :gravity/sh10-authenticated-ownership-bridge-request
   :schema-version 1
   :ownership-operation ownership-operation
   :ownership-operation-id
   (identity-id
    :gravity/sh10-ownership-operation-v1
    ownership-operation)
   :sh08-typed-core (:typed-core sh08-products)
   :sh08-verification (:verification sh08-products)
   :type-fact-id (:type-fact-id links)
   :effect-fact-id (:effect-fact-id links)
   :capability-proof-id (:capability-proof-id links)
   :expected-ownership-result
   (:ownership-result c9-products)
   :expected-ownership-verification
   (:ownership-verification c9-products)
   :expected-ownership-artifact-id
   (identity-id
    :gravity/sh10-ownership-result-v2
    (:ownership-result c9-products))
   :envelope-descriptor descriptor
   :envelope (:envelope authenticated)
   :template-replay (:template-replay authenticated)
   :envelope-verification (:verification authenticated)
   :actual-source-path actual-source-path})

(defn- prepared-request
  [sh08-products operation actual-path]
  (let [typed (:typed-core sh08-products)
        operation-input (ownership-operation operation)
        links (ownership-links typed)
        c9 (c9-products typed operation-input links)
        descriptor
        (descriptor
         sh08-products c9 operation-input links actual-path)
        authenticated
        (authenticated-envelope
         descriptor (:verification sh08-products))]
    {:request
     (request
      sh08-products c9 operation-input links descriptor
      authenticated actual-path)
     :links links
     :c9 c9
     :descriptor descriptor
     :authenticated authenticated}))

(defn- run-bridge [prepared]
  (let [request (:request prepared)
        c9-request
        (invoke
         bridge-plan
         'sh10-build-authenticated-ownership-request
         [request])
        fresh-result
        (invoke
         c9-plan
         'sh10-check-ownership-request
         [(:ownership-request c9-request)])
        fresh-verification
        (invoke
         c9-plan
         'sh10-verify-ownership-result
         [(:ownership-request c9-request) fresh-result])
        result
        (invoke
         bridge-plan
         'sh10-bind-authenticated-ownership-result
         [request c9-request fresh-result fresh-verification])]
    {:c9-request c9-request
     :ownership-result fresh-result
     :ownership-verification fresh-verification
     :result result}))

(deftest sh10-authenticated-ownership-bridge-compiles-with-narrow-api
  (doseq [plan [envelope-plan c9-plan bridge-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan
           (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is (= {:arity 0 :params []}
           (select-keys
            (get
             functions
             'sh10-authenticated-ownership-bridge-policy)
            [:arity :params])))
    (is (= {:arity 1 :params ['request]}
           (select-keys
            (get
             functions
             'sh10-build-authenticated-ownership-request)
            [:arity :params])))
    (is
     (=
      {:arity 4
       :params
       ['request 'c9-request
        'ownership-result 'ownership-verification]}
      (select-keys
       (get
        functions
        'sh10-bind-authenticated-ownership-result)
       [:arity :params])))
    (is
     (=
      {:arity 5
       :params
       ['request 'c9-request 'ownership-result
        'ownership-verification 'candidate]}
      (select-keys
       (get
        functions
        'sh10-verify-authenticated-ownership-result)
       [:arity :params])))))

(deftest sh10-accepts-authenticated-read-borrow-and-move
  (doseq [[extension sh08-products]
          [[".gravity" @sh08-gravity]
           [".qst" @sh08-qst]]]
    (doseq [operation
            [:initialized-read :immutable-borrow-move]]
      (let [prepared
            (prepared-request
             sh08-products operation
             (str "/checkout-a/ownership-input" extension))
            run (run-bridge prepared)
            result (:result run)]
        (is (= :accepted (:status (:c9-request run)))
            [extension operation])
        (is (= :accepted
               (:status (:ownership-result run)))
            [extension operation])
        (is (= :passed
               (:status (:ownership-verification run)))
            [extension operation])
        (is (= :accepted (:status result))
            [extension operation])
        (is (= operation (:operation result)))
        (if (= operation :initialized-read)
          (is (= [:initialize :read]
                 (mapv
                  :operation
                  (get-in
                   result
                   [:ownership-result :ownership-facts]))))
          (do
            (is (= :moved
                   (get-in
                    result
                    [:ownership-result :state :availability])))
            (is (= "sh10-owner-destination"
                   (get-in
                    result
                    [:ownership-result
                     :state :current-owner])))))
        (is
         (= :passed
            (:status
             (invoke
              bridge-plan
              'sh10-verify-authenticated-ownership-result
              [(:request prepared)
               (:c9-request run)
               (:ownership-result run)
               (:ownership-verification run)
               result]))))))))

(deftest sh10-rejects-authentic-mutable-alias-for-both-extensions
  (doseq [[extension sh08-products]
          [[".gravity" @sh08-gravity]
           [".qst" @sh08-qst]]]
    (let [prepared
          (prepared-request
           sh08-products :mutable-alias
           (str "/checkout-a/alias-input" extension))
          run (run-bridge prepared)
          result (:result run)]
      (is (= :accepted (:status (:c9-request run))) extension)
      (is (= :rejected
             (:status (:ownership-result run)))
          extension)
      (is (= :passed
             (:status (:ownership-verification run)))
          extension)
      (is (= :rejected (:status result)) extension)
      (is (= "STD10-BRIDGE-OWNERSHIP"
             (get-in result [:diagnostics 0 :rule]))
          extension)
      (is (= "C9-MUT-ALIAS"
             (get-in
              result
              [:diagnostics 0 :facts
               :upstream-diagnostics 0 :rule]))
          extension))))

(deftest sh10-authenticated-ownership-bridge-rejects-alteration
  (let [prepared
        (prepared-request
         @sh08-gravity :immutable-borrow-move
         "/checkout-a/ownership-input.gravity")
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
        altered-link
        (assoc
         request :effect-fact-id
         (str "sha256:" (apply str (repeat 64 "d"))))
        altered-operation
        (assoc-in
         request [:ownership-operation :owner-id]
         "substituted-owner")
        altered-expected-verification
        (assoc-in
         request
         [:expected-ownership-verification :checks 0]
         :substituted-check)
        altered-descriptor
        (assoc-in request
                  [:envelope-descriptor :unexpected]
                  true)
        altered-ownership
        (assoc-in
         (:ownership-result accepted)
         [:state :availability]
         :available)
        altered-result
        (assoc (:result accepted) :status :rejected)]
    (doseq [[label candidate expected-rule]
            [[:typed altered-typed "STD10-BRIDGE-SH08"]
             [:envelope altered-envelope
              "STD10-BRIDGE-ENVELOPE"]
             [:link altered-link "STD10-BRIDGE-ENVELOPE"]
             [:operation altered-operation
              "STD10-BRIDGE-ENVELOPE"]
             [:verification altered-expected-verification
              "STD10-BRIDGE-ENVELOPE"]
             [:descriptor altered-descriptor
              "STD10-BRIDGE-ENVELOPE"]]]
      (testing (name label)
        (let [result
              (invoke
               bridge-plan
               'sh10-build-authenticated-ownership-request
               [candidate])]
          (is (= :rejected (:status result)))
          (is (= expected-rule
                 (get-in result [:diagnostics 0 :rule]))))))
    (let [ownership-rejection
          (invoke
           bridge-plan
           'sh10-bind-authenticated-ownership-result
           [request
            (:c9-request accepted)
            altered-ownership
            (:ownership-verification accepted)])
          verification-rejection
          (invoke
           bridge-plan
           'sh10-bind-authenticated-ownership-result
           [request
            (:c9-request accepted)
            (:ownership-result accepted)
            (assoc
             (:ownership-verification accepted)
             :checks
             [:complete-recomputation])])
          result-rejection
          (invoke
           bridge-plan
           'sh10-verify-authenticated-ownership-result
           [request
            (:c9-request accepted)
            (:ownership-result accepted)
            (:ownership-verification accepted)
            altered-result])]
      (is (= :rejected (:status ownership-rejection)))
      (is (= "STD10-BRIDGE-VERIFY"
             (get-in
              ownership-rejection
              [:diagnostics 0 :rule])))
      (is (= :rejected (:status verification-rejection)))
      (is (= "STD10-BRIDGE-VERIFY"
             (get-in
              verification-rejection
              [:diagnostics 0 :rule])))
      (is (= :rejected (:status result-rejection))))))

(deftest sh10-identity-is-path-neutral-with-separate-provenance
  (let [sh08-products @sh08-gravity
        prepared-a
        (prepared-request
         sh08-products :immutable-borrow-move
         "/checkout-a/ownership-input.gravity")
        prepared-b
        (prepared-request
         sh08-products :immutable-borrow-move
         "/checkout-b/ownership-input.gravity")
        result-a (:result (run-bridge prepared-a))
        result-b (:result (run-bridge prepared-b))]
    (is (= (get-in
            prepared-a [:authenticated :semantic-root])
           (get-in
            prepared-b [:authenticated :semantic-root])))
    (is
     (not=
      (get-in
       prepared-a [:authenticated :provenance-root])
      (get-in
       prepared-b [:authenticated :provenance-root])))
    (is (= (:identity-input result-a)
           (:identity-input result-b)))
    (is (= (:artifact-id result-a)
           (:artifact-id result-b)))
    (is (not= (:provenance result-a)
              (:provenance result-b)))))

(deftest sh10-authenticated-ownership-bridge-fails-closed-on-malformed-input
  (doseq [value
          [nil
           {}
           {:artifact
            :gravity/sh10-authenticated-ownership-bridge-request}
           {:artifact
            :gravity/sh10-authenticated-ownership-bridge-request
            :schema-version 1
            :unexpected true}]]
    (let [result
          (invoke
           bridge-plan
           'sh10-build-authenticated-ownership-request
           [value])]
      (is (= :rejected (:status result)))
      (is (= "STD10-BRIDGE-SCHEMA"
             (get-in result [:diagnostics 0 :rule]))))))

(deftest sh10-authenticated-ownership-bridge-bounds-carriers
  (let [deep (nth (iterate vector :leaf) 130)
        result
        (invoke
         bridge-plan
         'sh10-build-authenticated-ownership-request
         [deep])]
    (is (= :rejected (:status result)))
    (is (= "STD10-BRIDGE-SCHEMA"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :request-carrier-bound
           (get-in result
                   [:diagnostics 0 :facts :reason])))))
