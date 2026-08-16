(ns gravity.self-hosting.sh08-authenticated-type-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_authenticated_type_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-08 authenticated integration test is not on the classpath"
        {:id "SH08-AUTH-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH08-AUTH-REPOSITORY-ROOT"}))

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
(def ^:private c7-source
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-08/"
   "authenticated_type_bridge.gravity"))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")

(def ^:private envelope-plan (delay (compile-plan envelope-source)))
(def ^:private c7-plan (delay (compile-plan c7-source)))
(def ^:private bridge-plan (delay (compile-plan bridge-source)))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-authenticated-type-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- canonical-id
  [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh08-authenticated-type-integration>" value))

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
      "Invalid SH-08 authenticated digest reference"
      {:id "SH08-AUTH-DIGEST-REFERENCE" :value value})))
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
        "Invalid SH-08 authenticated envelope template"
        {:id "SH08-AUTH-ENVELOPE-TEMPLATE"})))
    (doseq [[ordinal request] (map-indexed vector requests)]
      (let [references
            (bootstrap/p15-s23-c6c10-collect-digest-ref-ordinals!
             "<sh08-authenticated-envelope>"
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
            "Invalid SH-08 authenticated digest graph"
            {:id "SH08-AUTH-DIGEST-GRAPH"
             :ordinal ordinal})))))
    (when-not
     (= (set (range request-count))
        (reachable-ordinals requests roots))
      (throw
       (ex-info
        "Unreachable SH-08 authenticated digest request"
        {:id "SH08-AUTH-DIGEST-REACHABILITY"})))
    (let [resolved
          (reduce
           (fn [digests request]
             (let [ordinal (:ordinal request)
                   preimage
                   (bootstrap/p15-s23-c6c10-resolve-digest-references!
                    "<sh08-authenticated-envelope>"
                    (:preimage request)
                    request-count ordinal digests)]
               (conj digests (canonical-id preimage))))
           [] requests)
          resolve-final
          (fn [value]
            (bootstrap/p15-s23-c6c10-resolve-digest-references!
             "<sh08-authenticated-envelope>"
             value request-count nil resolved))
          checks (resolve-final (:identity-checks raw))
          mismatches
          (filterv
           #(not= (:computed-id %) (:observed-id %))
           checks)]
      (when (seq mismatches)
        (throw
         (ex-info
          "SH-08 authenticated identity subject mismatch"
          {:id "SH08-AUTH-IDENTITY"
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

(defn- bridge-source-revision
  []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        shapes (function-shapes plan)
        builder 'sh08-build-authenticated-type-request]
    {:owner :sh-types
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-08/authenticated-type-bridge"
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

(defn- core-artifact
  [source-path]
  (bootstrap/sh07-core-file-artifact source-path))

(defn- core
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :canonical-core-artifact]))

(defn- c7-result
  [artifact]
  (let [canonical-core (core artifact)
        result
        (invoke c7-plan 'sh08-type-core-artifact [canonical-core])
        verification
        (invoke
         c7-plan 'sh08-verify-type-result
         [canonical-core result])]
    {:type-template result
     :type-verification verification}))

(defn- products
  [source-path]
  (let [artifact (core-artifact source-path)
        verification
        (bootstrap/sh07-core-artifact-verification artifact)
        typed (c7-result artifact)]
    (merge
     {:source-path source-path
      :sh07-artifact artifact
      :sh07-verification verification}
     typed)))

(defn- descriptor
  [products actual-path]
  (let [canonical-core (core (:sh07-artifact products))
        core-identity
        {:artifact-id (:artifact-id canonical-core)}
        type-identity
        (:identity-input (:type-template products))
        typed-result-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh08-primitive-typed-core-v2
          :semantic-input type-identity})
        type-envelope-identity
        {:typed-result-id typed-result-id}
        core-subject-id
        (identity-id
         :gravity/sh08-sh07-canonical-core-v1
         core-identity)
        type-subject-id
        (identity-id
         :gravity/sh08-typed-result-v1
         type-envelope-identity)
        evidence-id
        (canonical-id
         {:domain :gravity/sh08-authenticated-type-evidence-v1
          :core-subject-id core-subject-id
          :type-subject-id type-subject-id
          :sh07-status
          (:status (:sh07-verification products))
          :c7-status
          (:status (:type-verification products))})
        fact-names [:type :effect :capability :ownership :safety]
        module (:module canonical-core)
        fact-values
        {:type {:typed-result-id type-subject-id}
         :effect {:effects (:effects module)}
         :capability {:capabilities (:capabilities module)}
         :ownership {:status :pending-sh10}
         :safety {:status (:safety module)}}
        facts
        (mapv
         #(fact-transition % (get fact-values %) evidence-id)
         fact-names)]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh08-authenticated-type-bridge
     :artifact-kind :gravity/sh08-authenticated-type-boundary
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind
      :gravity/sh08-authenticated-type-boundary-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh07-canonical-core :sh08-type-result]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh07-canonical-core :sh08-type-result]}
     :semantic-projections
     [{:name :sh07-canonical-core
       :role :verified-core-identity
       :entry-count (count core-identity)
       :value core-identity}
      {:name :sh08-type-result
       :role :fresh-type-result-identity
       :entry-count (count type-envelope-identity)
       :value type-envelope-identity}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts
      {:declared (set (:effects module))
       :observed (set (:effects module))}
      :capability-facts
      {:required (set (:capabilities module))
       :granted (set (:capabilities module))}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records
      [{:proof-id evidence-id
        :status :host-replayed
        :sh07-verification-status
        (:status (:sh07-verification products))
        :c7-verification-status
        (:status (:type-verification products))}]
      :proof-certificate-table
      {evidence-id
       {:status :host-replayed
        :sh07-verification-status
        (:status (:sh07-verification products))
        :c7-verification-status
        (:status (:type-verification products))}}
      :proof-summary {:required 2 :checked 2}
      :proof-usage
      [{:proof-id evidence-id
        :used-by :authenticated-type-boundary}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:fresh-sh07-verification
       :fresh-c7-result
       :fresh-c7-verification]}
     :identity-subjects
     [{:name :sh07-canonical-core
       :domain :gravity/sh08-sh07-canonical-core-v1
       :preimage core-identity
       :observed-id core-subject-id}
      {:name :sh08-type-result
       :domain :gravity/sh08-typed-result-v1
       :preimage type-envelope-identity
       :observed-id type-subject-id}]
     :lineage
     [{:stage :sh07-core
       :artifact-kind :gravity/sh07-canonical-core-artifact
       :semantic-id core-subject-id
       :artifact-id (:artifact-id canonical-core)
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh08-type-checking
       :artifact-kind :gravity/sh08-primitive-typed-core-template
       :semantic-id type-subject-id
       :artifact-id
       (canonical-id
        {:domain :gravity/sh08-typed-template-artifact-v1
         :semantic-id type-subject-id})
       :verification-id evidence-id
       :relation :freshly-recomputed}]
     :reference-closure
     {:root-id "sh08-type-bridge"
      :node-ids
      ["sh08-type-bridge" "sh07-core" "sh08-type-result"]
      :edges
      [{:from "sh08-type-bridge"
        :role :consumes :to "sh07-core"}
       {:from "sh08-type-bridge"
        :role :produces :to "sh08-type-result"}]
      :fact-reference-ids
      ["fact/type" "fact/effect" "fact/capability"
       "fact/ownership" "fact/safety"]
      :origin-reference-ids ["origin/sh07"]
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
     {:artifact :gravity/sh08-envelope-contextual-verification
      :status :contextual-verification-passed
      :artifact-template (:template sealed)
      :semantic-envelope-root (:semantic-root sealed)
      :provenance-binding-root (:provenance-root sealed)
      :identity-checks (:identity-checks sealed)
      :identity-enforcement :passed
      :eligible-for-contextual-acceptance? true
      :host-digest-resolution :passed
      :identity-subject-equality :passed
      :fresh-envelope-reconstruction :passed}
     :semantic-root (:semantic-root sealed)
     :provenance-root (:provenance-root sealed)}))

(defn- request
  [products descriptor authenticated actual-source-path]
  {:artifact :gravity/sh08-authenticated-type-bridge-request
   :schema-version 1
   :sh07-artifact (:sh07-artifact products)
   :sh07-verification (:sh07-verification products)
   :expected-type-identity-input
   (:identity-input (:type-template products))
   :expected-type-artifact-id
   (bootstrap/reader-canonical-hash
    {:domain :gravity/sh08-primitive-typed-core-v2
     :semantic-input
     (:identity-input (:type-template products))})
   :envelope-descriptor descriptor
   :envelope (:envelope authenticated)
   :template-replay (:template-replay authenticated)
   :envelope-verification
   (assoc
    (:verification authenticated)
    :sh07-verification (:sh07-verification products))
   :actual-source-path actual-source-path})

(defn- run-bridge
  [request]
  (let [c7-request
        (invoke
         bridge-plan
         'sh08-build-authenticated-type-request
         [request])
        fresh-template
        (invoke
         c7-plan 'sh08-type-core-artifact
         [(:core-artifact c7-request)])
        fresh-verification
        (invoke
         c7-plan 'sh08-verify-type-result
         [(:core-artifact c7-request) fresh-template])
        result
        (invoke
         bridge-plan
         'sh08-bind-authenticated-type-result
         [request c7-request fresh-template fresh-verification])]
    {:c7-request c7-request
     :type-template fresh-template
     :type-verification fresh-verification
     :result result}))

(defn- fixture-products
  [family basename extension]
  (products (fixture-path family basename extension)))

(def ^:private accepted-gravity-products
  (delay
   (fixture-products
    "accepted" "primitive-if-join" ".gravity")))

(def ^:private accepted-qst-products
  (delay
   (fixture-products
    "accepted" "primitive-if-join" ".qst")))

(def ^:private rejected-gravity-products
  (delay
   (fixture-products
    "rejected" "if-branch-type-mismatch" ".gravity")))

(def ^:private rejected-qst-products
  (delay
   (fixture-products
    "rejected" "if-branch-type-mismatch" ".qst")))

(defn- cached-fixture-products
  [family extension]
  (if (= family "accepted")
    (if (= extension ".gravity")
      @accepted-gravity-products
      @accepted-qst-products)
    (if (= extension ".gravity")
      @rejected-gravity-products
      @rejected-qst-products)))

(deftest sh08-authenticated-type-bridge-compiles-with-narrow-api
  (doseq [plan [envelope-plan c7-plan bridge-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is (= {:arity 0 :params []}
           (select-keys
            (get functions 'sh08-authenticated-type-bridge-policy)
            [:arity :params])))
    (is (= {:arity 1 :params ['request]}
           (select-keys
            (get functions 'sh08-build-authenticated-type-request)
            [:arity :params])))
    (is (= {:arity 4
            :params
            ['request 'c7-request
             'type-template 'type-verification]}
           (select-keys
            (get functions 'sh08-bind-authenticated-type-result)
            [:arity :params])))
    (is (= {:arity 5
            :params
            ['request 'c7-request 'type-template
             'type-verification 'candidate]}
           (select-keys
            (get functions 'sh08-verify-authenticated-type-result)
            [:arity :params])))))

(deftest sh08-accepts-co-canonical-authenticated-type-results
  (let [gravity
        (cached-fixture-products "accepted" ".gravity")
        qst
        (cached-fixture-products "accepted" ".qst")
        descriptor-gravity
        (descriptor gravity "/checkout-a/primitive-if-join.gravity")
        descriptor-qst
        (descriptor qst "/checkout-a/primitive-if-join.qst")
        authenticated-gravity
        (authenticated-envelope descriptor-gravity)
        authenticated-qst
        (authenticated-envelope descriptor-qst)
        request-gravity
        (request
         gravity descriptor-gravity authenticated-gravity
         "/checkout-a/primitive-if-join.gravity")
        request-qst
        (request
         qst descriptor-qst authenticated-qst
         "/checkout-a/primitive-if-join.qst")
        run-gravity (run-bridge request-gravity)
        run-qst (run-bridge request-qst)
        result (:result run-gravity)]
    (is (= :passed
           (:status (:sh07-verification gravity))
           (:status (:sh07-verification qst))))
    (is (= :accepted (:status (:c7-request run-gravity))))
    (is (= :accepted (:status (:type-template run-gravity))))
    (is (= :passed (:status (:type-verification run-gravity))))
    (is (= :accepted (:status result)))
    (is (= :gravity/sh08-authenticated-typed-core
           (:artifact result)))
    (is (= (:artifact-id (core (:sh07-artifact gravity)))
           (:sh07-artifact-id result)))
    (is (= (:type-table (:type-template gravity))
           (:type-table result)))
    (is (= (:type-facts (:type-template gravity))
           (:type-facts result)))
    (is (= (:identity-input result)
           (get-in run-qst [:result :identity-input])))
    (is (= (:artifact-id result)
           (get-in run-qst [:result :artifact-id])))
    (is (= :passed
           (:status
            (invoke
             bridge-plan
             'sh08-verify-authenticated-type-result
             [request-gravity
              (:c7-request run-gravity)
              (:type-template run-gravity)
              (:type-verification run-gravity)
              result]))))
    (is (some #{:functions} (:pending result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))))

(deftest sh08-preserves-c7-rejection-for-both-source-extensions
  (doseq [extension [".gravity" ".qst"]]
    (let [products
          (cached-fixture-products "rejected" extension)
          descriptor
          (descriptor
           products (str "/checkout-a/type-mismatch" extension))
          authenticated (authenticated-envelope descriptor)
          request
          (request
           products descriptor authenticated
           (str "/checkout-a/type-mismatch" extension))
          run (run-bridge request)
          result (:result run)]
      (is (= :accepted (:status (:c7-request run))) extension)
      (is (= :rejected (:status (:type-template run))) extension)
      (is (= :passed (:status (:type-verification run))) extension)
      (is (= :rejected (:status result)) extension)
      (is (= "STD08-BRIDGE-TYPE"
             (get-in result [:diagnostics 0 :rule]))
          extension)
      (is (= "C7-TYPE-MISMATCH"
             (get-in result
                     [:diagnostics 0 :facts
                      :upstream-diagnostics 0 :rule]))
          extension))))

(deftest sh08-authenticated-type-bridge-rejects-alteration
  (let [products
        (cached-fixture-products "accepted" ".gravity")
        descriptor
        (descriptor products "/checkout-a/primitive-if-join.gravity")
        authenticated (authenticated-envelope descriptor)
        request
        (request
         products descriptor authenticated
         "/checkout-a/primitive-if-join.gravity")
        accepted (run-bridge request)
        altered-core
        (assoc-in
         request
         [:sh07-artifact :gravity-core-boundary
          :canonical-core-artifact :artifact-id]
         (str "sha256:" (apply str (repeat 64 "f"))))
        altered-sh07-verification
        (assoc-in
         request
         [:sh07-verification :checks :canonical-core-replays?]
         false)
        altered-envelope
        (assoc-in
         request [:envelope :semantic-envelope-id]
         (str "sha256:" (apply str (repeat 64 "e"))))
        altered-template
        (assoc (:type-template accepted) :status :rejected)
        altered-type-table
        (assoc-in
         (:type-template accepted)
         [:type-table
          (first (keys (:type-table (:type-template accepted))))]
         :gravity.type/substituted)
        altered-type-verification
        (assoc
         (:type-verification accepted)
         :checks [:status-only-substitution])
        altered-result
        (assoc (:result accepted) :status :rejected)]
    (doseq [[label candidate expected-rule]
            [[:core altered-core "STD08-BRIDGE-SH07"]
             [:sh07-verification
              altered-sh07-verification
              "STD08-BRIDGE-SH07"]
             [:envelope altered-envelope "STD08-BRIDGE-ENVELOPE"]]]
      (testing (name label)
        (let [result
              (invoke
               bridge-plan
               'sh08-build-authenticated-type-request
               [candidate])]
          (is (= :rejected (:status result)))
          (is (= expected-rule
                 (get-in result [:diagnostics 0 :rule]))))))
    (let [template-rejection
          (invoke
           bridge-plan
           'sh08-bind-authenticated-type-result
           [request
            (:c7-request accepted)
            altered-template
            (:type-verification accepted)])
          table-rejection
          (invoke
           bridge-plan
           'sh08-bind-authenticated-type-result
           [request
            (:c7-request accepted)
            altered-type-table
            (:type-verification accepted)])
          verification-rejection
          (invoke
           bridge-plan
           'sh08-bind-authenticated-type-result
           [request
            (:c7-request accepted)
            (:type-template accepted)
            altered-type-verification])
          result-rejection
          (invoke
           bridge-plan
           'sh08-verify-authenticated-type-result
           [request
            (:c7-request accepted)
            (:type-template accepted)
            (:type-verification accepted)
            altered-result])]
      (is (= :rejected (:status template-rejection)))
      (is (= "STD08-BRIDGE-VERIFY"
             (get-in template-rejection
                     [:diagnostics 0 :rule])))
      (is (= :rejected (:status table-rejection)))
      (is (= "STD08-BRIDGE-VERIFY"
             (get-in table-rejection
                     [:diagnostics 0 :rule])))
      (is (= :rejected (:status verification-rejection)))
      (is (= "STD08-BRIDGE-VERIFY"
             (get-in verification-rejection
                     [:diagnostics 0 :rule])))
      (is (= :rejected (:status result-rejection))))))

(deftest sh08-identity-is-path-neutral-with-separate-provenance
  (let [products
        (cached-fixture-products "accepted" ".gravity")
        descriptor-a
        (descriptor products "/checkout-a/type-input.gravity")
        descriptor-b
        (descriptor products "/checkout-b/type-input.gravity")
        authenticated-a (authenticated-envelope descriptor-a)
        authenticated-b (authenticated-envelope descriptor-b)
        request-a
        (request
         products descriptor-a authenticated-a
         "/checkout-a/type-input.gravity")
        request-b
        (request
         products descriptor-b authenticated-b
         "/checkout-b/type-input.gravity")
        result-a (:result (run-bridge request-a))
        result-b (:result (run-bridge request-b))]
    (is (= (:semantic-root authenticated-a)
           (:semantic-root authenticated-b)))
    (is (not= (:provenance-root authenticated-a)
              (:provenance-root authenticated-b)))
    (is (= (:identity-input result-a)
           (:identity-input result-b)))
    (is (= (:artifact-id result-a)
           (:artifact-id result-b)))
    (is (not= (:provenance result-a)
              (:provenance result-b)))))

(deftest sh08-authenticated-type-bridge-fails-closed-on-malformed-input
  (doseq [value
          [nil
           {}
           {:artifact
            :gravity/sh08-authenticated-type-bridge-request}
           {:artifact
            :gravity/sh08-authenticated-type-bridge-request
            :schema-version 1
            :unexpected true}]]
    (let [result
          (invoke
           bridge-plan
           'sh08-build-authenticated-type-request
           [value])]
      (is (= :rejected (:status result)))
      (is (= "STD08-BRIDGE-SCHEMA"
             (get-in result [:diagnostics 0 :rule]))))))
