(ns gravity.self-hosting.sh22-authenticated-library-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh22_authenticated_library_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-22 authenticated integration test is not on the classpath"
        {:id "SH22-AUTH-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH22-AUTH-REPOSITORY-ROOT"}))

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
(def ^:private sh13-source
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")
(def ^:private sh14-source
  "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")
(def ^:private sh22-source
  "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")
(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-22/"
   "authenticated_vector_bridge.gravity"))
(def ^:private sh13-fixture
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-13/accepted/"
   "control-flow-modules.gravity"))
(def ^:private sh14-fixture
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-14/accepted/"
   "data-layouts.gravity"))

(def ^:private envelope-plan (delay (compile-plan envelope-source)))
(def ^:private sh13-plan (delay (compile-plan sh13-source)))
(def ^:private sh14-plan (delay (compile-plan sh14-source)))
(def ^:private sh22-plan (delay (compile-plan sh22-source)))
(def ^:private bridge-plan (delay (compile-plan bridge-source)))
(def ^:private sh13-fixture-plan (delay (compile-plan sh13-fixture)))
(def ^:private sh14-fixture-plan (delay (compile-plan sh14-fixture)))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-authenticated-library-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- canonical-id
  [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh22-authenticated-library-integration>" value))

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

(defn- digest-ref-ordinal
  [value]
  (when-not
   (and
    (map? value)
    (= #{:digest-ref} (set (keys value)))
    (nat-int? (:digest-ref value)))
    (throw
     (ex-info
      "Invalid SH-22 authenticated digest reference"
      {:id "SH22-AUTH-DIGEST-REFERENCE" :value value})))
  (:digest-ref value))

(defn- reachable-ordinals
  [requests roots]
  (loop [pending (mapv digest-ref-ordinal roots)
         reachable #{}]
    (if (empty? pending)
      reachable
      (let [ordinal (peek pending)
            remaining (pop pending)]
        (if (contains? reachable ordinal)
          (recur remaining reachable)
          (recur
           (into remaining (:depends-on (get requests ordinal)))
           (conj reachable ordinal)))))))

(defn- seal-builder-result!
  [raw]
  (let [requests (:digest-requests raw)
        request-count (count requests)
        roots (:digest-graph-roots raw)]
    (when-not
     (and
      (= :accepted (:status raw))
      (vector? requests)
      (pos? request-count)
      (<= request-count 2048)
      (= 2 (count roots))
      (= (:semantic-envelope-root raw) (first roots))
      (= (:provenance-binding-root raw) (second roots)))
      (throw
       (ex-info
        "Invalid SH-22 authenticated envelope template"
        {:id "SH22-AUTH-ENVELOPE-TEMPLATE"})))
    (doseq [[ordinal request] (map-indexed vector requests)]
      (let [references
            (bootstrap/p15-s23-c6c10-collect-digest-ref-ordinals!
             "<sh22-authenticated-envelope>"
             (:preimage request)
             request-count ordinal)]
        (when-not
         (and
          (= #{:algorithm :depends-on :encoding :key
               :ordinal :preimage}
             (set (keys request)))
          (= ordinal (:key request) (:ordinal request))
          (= :sha256 (:algorithm request))
          (= :gravity/canonical-edn-v1 (:encoding request))
          (= (:depends-on request)
             (vec (sort (distinct (:depends-on request)))))
          (= (set references) (set (:depends-on request)))
          (every? #(< % ordinal) (:depends-on request)))
          (throw
           (ex-info
            "Invalid SH-22 authenticated digest graph"
            {:id "SH22-AUTH-DIGEST-GRAPH" :ordinal ordinal})))))
    (when-not
     (= (set (range request-count))
        (reachable-ordinals requests roots))
      (throw
       (ex-info
        "Unreachable SH-22 authenticated digest request"
        {:id "SH22-AUTH-DIGEST-REACHABILITY"})))
    (let [resolved
          (reduce
           (fn [digests request]
             (let [ordinal (:ordinal request)
                   preimage
                   (bootstrap/p15-s23-c6c10-resolve-digest-references!
                    "<sh22-authenticated-envelope>"
                    (:preimage request)
                    request-count ordinal digests)]
               (conj digests (canonical-id preimage))))
           [] requests)
          resolve-final
          (fn [value]
            (bootstrap/p15-s23-c6c10-resolve-digest-references!
             "<sh22-authenticated-envelope>"
             value request-count nil resolved))
          checks (resolve-final (:identity-checks raw))
          mismatches
          (filterv
           #(not= (:computed-id %) (:observed-id %))
           checks)]
      (when (seq mismatches)
        (throw
         (ex-info
          "SH-22 authenticated identity subject mismatch"
          {:id "SH22-AUTH-IDENTITY"
           :subjects (mapv :name mismatches)})))
      {:template (resolve-final (:artifact-template raw))
       :semantic-root
       (resolve-final (:semantic-envelope-root raw))
       :provenance-root
       (resolve-final (:provenance-binding-root raw))
       :identity-checks checks
       :requests requests
       :resolved-digests resolved})))

(defn- fact-transition
  [name value evidence-id]
  {:name name
   :disposition :preserved
   :input value
   :output value
   :input-count (count value)
   :output-count (count value)
   :evidence-ids [evidence-id]})

(defn- upstream-products
  []
  (let [module
        (invoke sh13-fixture-plan 'sh13-branch-module [])
        arguments [true]
        execution
        (invoke sh13-plan 'sh13-run-module [module arguments])
        execution-verification
        (invoke
         sh13-plan 'sh13-verify-execution
         [module arguments execution])
        layout-request
        (invoke sh14-fixture-plan 'sh14-vector-request [])
        layout
        (invoke sh14-plan 'sh14-build-layout [layout-request])
        layout-verification
        (invoke
         sh14-plan 'sh14-verify-layout
         [layout-request layout])]
    {:module module
     :arguments arguments
     :execution execution
     :execution-verification execution-verification
     :layout-request layout-request
     :layout layout
     :layout-verification layout-verification}))

(defn- path-neutral-execution
  [execution]
  (dissoc execution :provenance))

(defn- path-neutral-layout
  [layout]
  (dissoc layout :provenance))

(defn- bridge-source-revision
  []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        shapes (function-shapes plan)
        builder 'sh22-build-authenticated-vector-request]
    {:owner :sh-standard-library
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-22/authenticated-vector-bridge"
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

(defn- descriptor
  [products source-path]
  (let [execution (path-neutral-execution (:execution products))
        layout (path-neutral-layout (:layout products))
        execution-preimage
        {:artifact :gravity/sh13-control-flow-execution
         :value execution}
        layout-preimage
        {:artifact :gravity/sh14-data-layout
         :value layout}
        execution-id
        (identity-id
         :gravity/sh22-sh13-execution-v1 execution-preimage)
        layout-id
        (identity-id
         :gravity/sh22-sh14-layout-v1 layout-preimage)
        evidence-id
        (canonical-id
         {:domain :gravity/sh22-authenticated-upstream-evidence-v1
          :execution-id execution-id
          :layout-id layout-id
          :sh13-verification (:execution-verification products)
          :sh14-verification (:layout-verification products)})
        fact-names [:type :effect :ownership :capability :safety]
        fact-values
        {:type {:id (get-in layout [:facts :type-fact-id])}
         :effect {:id (get-in layout [:facts :effect-fact-id])}
         :ownership {:id (get-in layout [:facts :ownership-fact-id])}
         :capability
         {:id
          (get-in execution
                  [:identity-input :capability-table-id])}
         :safety {:id (get-in layout [:facts :safety-fact-id])}}
        facts
        (mapv
         #(fact-transition % (get fact-values %) evidence-id)
         fact-names)]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh22-authenticated-vector-bridge
     :artifact-kind :gravity/sh22-authenticated-vector-upstream
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind
      :gravity/sh22-authenticated-vector-upstream-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh13-execution :sh14-layout]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh13-execution :sh14-layout]}
     :semantic-projections
     [{:name :sh13-execution
       :role :verified-control-flow-result
       :entry-count (count execution)
       :value execution}
      {:name :sh14-layout
       :role :verified-vector-layout
       :entry-count (count layout)
       :value layout}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records
      [{:proof-id evidence-id
        :status :host-replayed
        :sh13-verification (:execution-verification products)
        :sh14-verification (:layout-verification products)}]
      :proof-certificate-table
      {evidence-id
       {:status :host-replayed
        :sh13-verification (:execution-verification products)
        :sh14-verification (:layout-verification products)}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage
      [{:proof-id evidence-id
        :used-by :authenticated-vector-assoc}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:sh13-verifier-replay
       :sh14-verifier-replay
       :sh22-verifier-replay]}
     :identity-subjects
     [{:name :sh13-execution
       :domain :gravity/sh22-sh13-execution-v1
       :preimage execution-preimage
       :observed-id execution-id}
      {:name :sh14-layout
       :domain :gravity/sh22-sh14-layout-v1
       :preimage layout-preimage
       :observed-id layout-id}]
     :lineage
     [{:stage :sh13-control-flow
       :artifact-kind :gravity/sh13-control-flow-execution
       :semantic-id execution-id
       :artifact-id
       (canonical-id
        {:domain :gravity/sh22-sh13-artifact-v1
         :semantic-id execution-id})
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh14-layout
       :artifact-kind :gravity/sh14-data-layout
       :semantic-id layout-id
       :artifact-id
       (canonical-id
        {:domain :gravity/sh22-sh14-artifact-v1
         :semantic-id layout-id})
       :verification-id evidence-id
       :relation :verified-upstream}]
     :reference-closure
     {:root-id "sh22-vector-bridge"
      :node-ids
      ["sh22-vector-bridge" "sh13-execution" "sh14-layout"]
      :edges
      [{:from "sh22-vector-bridge"
        :role :uses
        :to "sh13-execution"}
       {:from "sh22-vector-bridge"
        :role :uses
        :to "sh14-layout"}]
      :fact-reference-ids
      ["fact/type" "fact/effect" "fact/ownership"
       "fact/capability" "fact/safety"]
      :origin-reference-ids ["origin/sh13" "origin/sh14"]
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 3
      :observed-edge-count 2
      :observed-maximum-depth 1}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- authenticated-envelope
  [descriptor]
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
        sealed (seal-builder-result! raw)]
    {:envelope (:template sealed)
     :template-replay replay
     :verification
     {:artifact :gravity/sh22-envelope-contextual-verification
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
      :sh13-verification
      (get-in descriptor
              [:proof-composite :proof-records 0
               :sh13-verification])
      :sh14-verification
      (get-in descriptor
              [:proof-composite :proof-records 0
               :sh14-verification])}
     :semantic-root (:semantic-root sealed)
     :provenance-root (:provenance-root sealed)}))

(defn- bridge-request
  [products descriptor authenticated actual-source-path]
  {:artifact :gravity/sh22-authenticated-vector-bridge-request
   :schema-version 1
   :sh13-module (:module products)
   :sh13-arguments (:arguments products)
   :sh13-execution (:execution products)
   :sh13-verification (:execution-verification products)
   :sh14-request (:layout-request products)
   :sh14-layout (:layout products)
   :sh14-verification (:layout-verification products)
   :envelope-descriptor descriptor
   :envelope (:envelope authenticated)
   :envelope-template-replay (:template-replay authenticated)
   :envelope-verification (:verification authenticated)
   :operation-spec
   (:operation-spec
    (invoke
     bridge-plan
     'sh22-authenticated-vector-bridge-policy
     []))
   :actual-source-path actual-source-path})

(defn- run-bridge
  [request]
  (let [library-request
        (invoke
         bridge-plan
         'sh22-build-authenticated-vector-request
         [request])
        library-result
        (invoke sh22-plan 'sh22-handle-request [library-request])
        library-verification
        (invoke
         sh22-plan 'sh22-verify-result
         [library-request library-result])
        bridge-result
        (invoke
         bridge-plan
         'sh22-bind-authenticated-vector-result
         [request library-result library-verification])]
    {:library-request library-request
     :library-result library-result
     :library-verification library-verification
     :bridge-result bridge-result}))

(deftest sh22-authenticated-bridge-compiles-with-narrow-function-shapes
  (doseq [plan
          [envelope-plan sh13-plan sh14-plan sh22-plan bridge-plan
           sh13-fixture-plan sh14-fixture-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is (= {:arity 0 :params []}
           (select-keys
            (get functions 'sh22-authenticated-vector-bridge-policy)
            [:arity :params])))
    (is (= {:arity 1 :params ['request]}
           (select-keys
            (get functions 'sh22-build-authenticated-vector-request)
            [:arity :params])))
    (is (= {:arity 3
            :params ['request 'library-result 'library-verification]}
           (select-keys
            (get functions 'sh22-bind-authenticated-vector-result)
            [:arity :params])))
    (is (= {:arity 4
            :params
            ['request 'library-result 'library-verification 'candidate]}
           (select-keys
            (get functions 'sh22-verify-authenticated-vector-result)
            [:arity :params])))))

(deftest sh22-authenticated-bridge-carrier-bounds-cover-genuine-request
  (let [products (upstream-products)
        descriptor
        (descriptor products "/checkout-a/sh22-bridge.gravity")
        authenticated (authenticated-envelope descriptor)
        request
        (bridge-request
         products descriptor authenticated
         "/checkout-a/library-call.gravity")
        genuine-census
        (invoke
         bridge-plan
         'sh22-bridge-carrier-preflight
         [request])
        scalar-chunk (apply str (repeat 2048 "a"))
        inclusive-carrier (vec (repeat 64 scalar-chunk))
        over-limit-carrier
        (assoc inclusive-carrier 0 (str scalar-chunk "a"))
        inclusive
        (invoke
         bridge-plan
         'sh22-bridge-carrier-preflight
         [inclusive-carrier])
        over-limit
        (invoke
         bridge-plan
         'sh22-bridge-carrier-preflight
         [over-limit-carrier])]
    (is (= {:maximum-depth 15
            :maximum-width 36
            :nodes 10592
            :scalar-units 103173
            :status :accepted}
           genuine-census))
    (is (= :accepted (:status inclusive)))
    (is (= 65 (:nodes inclusive)))
    (is (= 1 (:maximum-depth inclusive)))
    (is (= 64 (:maximum-width inclusive)))
    (is (= 131072 (:scalar-units inclusive)))
    (is (= :rejected (:status over-limit)))
    (is (= :carrier-scalar-bound (:reason over-limit)))))

(deftest sh22-executes-one-authenticated-pure-vector-operation
  (let [products (upstream-products)
        descriptor
        (descriptor products "/checkout-a/sh22-bridge.gravity")
        authenticated (authenticated-envelope descriptor)
        request
        (bridge-request
         products descriptor authenticated
         "/checkout-a/library-call.gravity")
        result (run-bridge request)
        bridge-result (:bridge-result result)]
    (is (= :accepted (:status (:execution products))))
    (is (= :passed
           (:status (:execution-verification products))))
    (is (= :accepted (:status (:layout products))))
    (is (= :passed (:status (:layout-verification products))))
    (is (= :template-replay-passed
           (get-in request [:envelope-template-replay :status])))
    (is (= :pending-host-resolution
           (get-in request
                   [:envelope-template-replay
                    :identity-enforcement])))
    (is (false?
         (get-in request
                 [:envelope-template-replay
                  :eligible-for-contextual-acceptance?])))
    (is (= :contextual-verification-passed
           (get-in request [:envelope-verification :status])))
    (is (= :passed
           (get-in request
                   [:envelope-verification :identity-enforcement])))
    (is (= [[1 2 3] 1 42]
           (:arguments (:library-request result))))
    (is (= :accepted (:status (:library-result result))))
    (is (= [1 42 3] (:value (:library-result result))))
    (is (= :passed (:status (:library-verification result))))
    (is (= :accepted (:status bridge-result)))
    (is (= :vector-assoc (:operation bridge-result)))
    (is (= [1 42 3]
           (get-in bridge-result [:library-result :value])))
    (is (= []
           (:diagnostics bridge-result)))
    (is (true? (:clojure-seed-boundary? bridge-result)))
    (is (false? (:self-hosted? bridge-result)))
    (is (some #{:authenticated-sh19-runtime-services}
              (:pending bridge-result)))
    (is (= :passed
           (:status
            (invoke
             bridge-plan
             'sh22-verify-authenticated-vector-result
             [request
              (:library-result result)
              (:library-verification result)
              bridge-result]))))))

(deftest sh22-authenticated-bridge-rejects-upstream-and-result-alteration
  (let [products (upstream-products)
        descriptor
        (descriptor products "/checkout-a/sh22-bridge.gravity")
        authenticated (authenticated-envelope descriptor)
        request
        (bridge-request
         products descriptor authenticated
         "/checkout-a/library-call.gravity")
        accepted (run-bridge request)
        altered-execution
        (assoc-in request
                  [:sh13-execution :execution :value]
                  99)
        altered-layout
        (assoc-in request
                  [:sh14-layout :facts :safety-fact-id]
                  "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        altered-envelope
        (assoc-in request
                  [:envelope :semantic-id]
                  "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        altered-sh13-verification
        (assoc-in request
                  [:sh13-verification :status]
                  :rejected)
        altered-operation-spec
        (assoc-in request [:operation-spec :index] 2)
        substituted-library
        (assoc (:library-result accepted) :value [1 99 3])
        substituted-bridge
        (assoc (:bridge-result accepted) :status :rejected)]
    (doseq [[label altered expected-rule]
            [[:execution altered-execution "STD22-BRIDGE-ENVELOPE"]
             [:layout altered-layout "STD22-BRIDGE-SH14"]
             [:envelope altered-envelope "STD22-BRIDGE-ENVELOPE"]
             [:sh13-verification
              altered-sh13-verification
              "STD22-BRIDGE-SH13"]
             [:operation-spec
              altered-operation-spec
              "STD22-BRIDGE-SCHEMA"]]]
      (testing (name label)
        (let [rejection
              (invoke
               bridge-plan
               'sh22-build-authenticated-vector-request
               [altered])]
          (is (= :rejected (:status rejection)))
          (is (= expected-rule
                 (get-in rejection [:diagnostics 0 :rule]))))))
    (let [library-rejection
          (invoke
           bridge-plan
           'sh22-bind-authenticated-vector-result
           [request
            substituted-library
            (:library-verification accepted)])
          verifier-rejection
          (invoke
           bridge-plan
           'sh22-verify-authenticated-vector-result
           [request
            (:library-result accepted)
            (:library-verification accepted)
            substituted-bridge])]
      (is (= :rejected (:status library-rejection)))
      (is (= "STD22-BRIDGE-LIBRARY"
             (get-in library-rejection [:diagnostics 0 :rule])))
      (is (= :rejected (:status verifier-rejection)))
      (is (= "STD22-BRIDGE-VERIFY"
             (get-in verifier-rejection [:diagnostics 0 :rule]))))))

(deftest sh22-authenticated-bridge-is-path-neutral-with-separate-provenance
  (let [products (upstream-products)
        descriptor-a
        (descriptor products "/checkout-a/sh22-bridge.gravity")
        descriptor-b
        (descriptor products "/checkout-b/sh22-bridge.gravity")
        authenticated-a (authenticated-envelope descriptor-a)
        authenticated-b (authenticated-envelope descriptor-b)
        request-a
        (bridge-request
         products descriptor-a authenticated-a
         "/checkout-a/library-call.gravity")
        request-b
        (bridge-request
         products descriptor-b authenticated-b
         "/checkout-b/library-call.gravity")
        result-a (:bridge-result (run-bridge request-a))
        result-b (:bridge-result (run-bridge request-b))]
    (is (= (:semantic-root authenticated-a)
           (:semantic-root authenticated-b)))
    (is (not= (:provenance-root authenticated-a)
              (:provenance-root authenticated-b)))
    (is (= (:identity-input result-a)
           (:identity-input result-b)))
    (is (= #{:semantic-id :artifact-id :semantic-envelope-id}
           (set (keys (:upstream-envelope-identity result-a)))
           (set (keys (:upstream-envelope-identity result-b)))))
    (is (not= (:provenance result-a)
              (:provenance result-b)))
    (is (= [1 42 3]
           (get-in result-a [:library-result :value])
           (get-in result-b [:library-result :value])))))

(deftest sh22-authenticated-bridge-fails-closed-on-malformed-input
  (doseq [value
          [nil
           {}
           {:artifact
            :gravity/sh22-authenticated-vector-bridge-request}
           {:artifact
            :gravity/sh22-authenticated-vector-bridge-request
            :schema-version 1
            :unexpected true}
           (nth (iterate vector :over-bound) 70)]]
    (let [result
          (invoke
           bridge-plan
           'sh22-build-authenticated-vector-request
           [value])]
      (is (= :rejected (:status result)))
      (is (= "STD22-BRIDGE-SCHEMA"
             (get-in result [:diagnostics 0 :rule]))))))
