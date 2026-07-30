(ns gravity.self-hosting.sh26-stage-rebuild-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh02-authenticated-envelope-test]))

(defn- repository-root []
  (let [resource
        (io/resource "gravity/self_hosting/sh26_stage_rebuild_test.clj")]
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root not found" {:id "SH26-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(defn- path [relative] (str (.resolve @root relative)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-26")
(def ^:private sh25-root
  "bootstrap/clojure/fixtures/self-hosting/sh-25")
(def ^:private envelope-source
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan (str fixture-root "/stage_rebuild_engine.gravity"))))
(def ^:private accepted-gravity-plan
  (delay (compile-plan (str fixture-root "/accepted/stage-rebuild.gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan (str fixture-root "/accepted/stage-rebuild.qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (str fixture-root "/rejected/invalid-stage-rebuild.gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (str fixture-root "/rejected/invalid-stage-rebuild.qst"))))
(def ^:private sh25-engine-plan
  (delay (compile-plan (str sh25-root "/component_build_engine.gravity"))))
(def ^:private sh25-accepted-plan
  (delay (compile-plan
          (str sh25-root "/accepted/component-builds.gravity"))))
(def ^:private envelope-plan
  (delay (compile-plan envelope-source)))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh26-stage-rebuild-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh26-stage-rebuild-test>" value))

(defn- identity-id [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- fact-transition [name evidence-id]
  (let [value {:family name :entries []}]
    {:name name
     :disposition :preserved
     :input value
     :output value
     :input-count (count value)
     :output-count (count value)
     :evidence-ids [evidence-id]}))

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

(defn- sh26-envelope-descriptor
  [request result verification projection ids]
  (let [fact-names
        [:type :effect :ownership :capability :safety]
        evidence-id
        (canonical-id
         {:domain :gravity/sh26-envelope-evidence-v1
          :ids ids})
        context-preimage
        {:sh25-request-id (:request-id ids)
         :complete-result-id (:result-id ids)
         :verification-id (:verification-id ids)
         :projection-id (:projection-id ids)
         :provenance-root (:provenance-root ids)}
        subjects
        [{:name :sh25-request
          :domain :gravity/sh26-sh25-request-v1
          :preimage {:content-id (:request-id ids)}
          :observed-id
          (identity-id
           :gravity/sh26-sh25-request-v1
           {:content-id (:request-id ids)})}
         {:name :sh25-result
          :domain :gravity/sh26-sh25-result-v1
          :preimage {:content-id (:result-id ids)}
          :observed-id
          (identity-id
           :gravity/sh26-sh25-result-v1
           {:content-id (:result-id ids)})}
         {:name :sh25-verification
          :domain :gravity/sh26-sh25-verification-v1
          :preimage {:content-id (:verification-id ids)}
          :observed-id
          (identity-id
           :gravity/sh26-sh25-verification-v1
           {:content-id (:verification-id ids)})}
         {:name :sh25-projection
          :domain :gravity/sh26-sh25-projection-v1
          :preimage {:content-id (:projection-id ids)}
          :observed-id
          (identity-id
           :gravity/sh26-sh25-projection-v1
           {:content-id (:projection-id ids)})}
         {:name :sh25-complete-context
          :domain :gravity/sh26-sh25-complete-context-v1
          :preimage context-preimage
          :observed-id
          (identity-id
           :gravity/sh26-sh25-complete-context-v1
           context-preimage)}]
        source-text (slurp (path envelope-source))
        plan @envelope-plan
        source-hash
        (str "sha256:" (bootstrap/sha256-hex source-text))
        plan-hash
        (bootstrap/p15-s23-c11-mir-digest
         (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
          plan))
        functions-hash
        (bootstrap/p15-s23-c11-mir-digest (:functions plan))]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh26-stage-rebuild
     :artifact-kind :gravity/sh26-sh25-complete-context
     :source-revision
     {:owner :sh-envelope
      :source-language :gravity
      :logical-source-path
      "gravity/compiler/authenticated-envelope"
      :source-content-hash source-hash
      :source-byte-count
      (alength
       (.getBytes source-text
                  java.nio.charset.StandardCharsets/UTF_8))
      :plan-semantic-hash plan-hash
      :functions-semantic-hash functions-hash
      :builder-function 'authenticated-envelope-build-template
      :builder-semantic-hash
      (bootstrap/p15-s23-c11-mir-digest
       (get (:functions plan)
            'authenticated-envelope-build-template))
      :function-shapes
      (into (sorted-map)
            (map
             (fn [[name function]]
               [name (select-keys function [:arity :params])]))
            (:functions plan))}
     :projection-contract
     {:contract-kind :gravity/sh26-sh25-complete-context-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh25-request :sh25-result :sh25-verification
       :sh25-projection]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh25-request :sh25-result :sh25-verification
       :sh25-projection :sh25-complete-context]}
     :semantic-projections
     [{:name :sh25-request :role :exact-request
       :entry-count 1 :value {:content-id (:request-id ids)}}
      {:name :sh25-result :role :exact-result
       :entry-count 1 :value {:content-id (:result-id ids)}}
      {:name :sh25-verification :role :fresh-verification
       :entry-count 1
       :value {:content-id (:verification-id ids)}}
      {:name :sh25-projection :role :final-projection
       :entry-count 1 :value {:content-id (:projection-id ids)}}]
     :fact-transitions
     (mapv #(fact-transition % evidence-id) fact-names)
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :host-replayed}]
      :proof-certificate-table
      {evidence-id {:status :host-replayed}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage
      [{:proof-id evidence-id :used-by :sh26-stage-rebuild}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:fresh-sh25-verification
       :exact-sh25-result
       :exact-sh25-projection]}
     :identity-subjects subjects
     :lineage
     [{:stage :sh25-component-build
       :artifact-kind :gravity/sh25-component-build-result
       :semantic-id (:result-id ids)
       :artifact-id (:projection-id ids)
       :verification-id (:verification-id ids)
       :relation :freshly-verified-upstream}]
     :reference-closure
     {:root-id "sh26-stage-rebuild"
      :node-ids
      ["sh26-stage-rebuild" "sh25-request" "sh25-result"
       "sh25-verification" "sh25-projection"]
      :edges
      [{:from "sh26-stage-rebuild" :role :consumes
        :to "sh25-request"}
       {:from "sh26-stage-rebuild" :role :consumes
        :to "sh25-result"}
       {:from "sh26-stage-rebuild" :role :verifies
        :to "sh25-verification"}
       {:from "sh26-stage-rebuild" :role :consumes
        :to "sh25-projection"}]
      :fact-reference-ids ["fact/type" "fact/effect"]
      :origin-reference-ids ["origin/sh25"]
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 5
      :observed-edge-count 4
      :observed-maximum-depth 1}
     :actual-path-provenance
     {:source-path
      (path (str fixture-root "/stage_rebuild_engine.gravity"))
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- authenticate-sh25-context
  [request result verification projection ids]
  (let [descriptor
        (sh26-envelope-descriptor
         request result verification projection ids)
        revision-pin
        {:artifact :gravity/sh26-envelope-revision-pin
         :schema-version 1
         :source-revision (:source-revision descriptor)
         :verifier-function
         'authenticated-envelope-verify-template
         :verifier-semantic-hash
         (bootstrap/p15-s23-c11-mir-digest
          (get (:functions @envelope-plan)
               'authenticated-envelope-verify-template))}
        raw
        (invoke envelope-plan
                'authenticated-envelope-build-template
                [descriptor])
        replay
        (invoke envelope-plan
                'authenticated-envelope-verify-template
                [descriptor
                 (:artifact-template raw)
                 (:digest-requests raw)])
        seal
        (ns-resolve
         'gravity.self-hosting.sh02-authenticated-envelope-test
         'seal-builder-result!)
        seed-sealed (seal raw)
        request-resolved-pairs
        (mapv
         (fn [ordinal request resolved-digest]
           {:ordinal ordinal
            :purpose (get-in request [:preimage :domain])
            :request request
            :resolved-digest resolved-digest})
         (range (count (:requests seed-sealed)))
         (:requests seed-sealed)
         (:resolved-digests seed-sealed))
        resolution-binding-preimage
        {:domain :gravity/sh26-host-digest-resolution-binding-v1
         :request-count (count request-resolved-pairs)
         :request-resolved-pairs request-resolved-pairs}
        resolution-binding
        {:artifact :gravity/sh26-host-digest-resolution-binding
         :schema-version 1
         :boundary :trusted-clojure-seed
         :request-count (count request-resolved-pairs)
         :request-resolved-pairs request-resolved-pairs
         :binding-preimage resolution-binding-preimage
         :binding-root (canonical-id resolution-binding-preimage)}
        context-subjects
        {:host-resolution-binding-root
         (:binding-root resolution-binding)
         :request-count (count request-resolved-pairs)
         :semantic-envelope-root (:semantic-root seed-sealed)
         :provenance-binding-root (:provenance-root seed-sealed)}
        sealed
        (assoc seed-sealed
               :host-resolution-binding resolution-binding
               :context-subjects context-subjects)
        contextual
        {:artifact :gravity/sh26-envelope-contextual-verification
         :schema-version 1
         :status :contextual-verification-passed
         :builder-result raw
         :template-replay replay
         :sealed-record sealed
         :artifact-template (:template sealed)
         :semantic-envelope-root (:semantic-root sealed)
         :provenance-binding-root (:provenance-root sealed)
         :identity-checks (:identity-checks sealed)
         :host-resolution-binding resolution-binding
         :context-subjects context-subjects
         :identity-enforcement :passed
         :eligible-for-contextual-acceptance? true
         :host-digest-resolution :passed
         :identity-subject-equality :passed
         :fresh-envelope-reconstruction :passed}]
    (let [observed
          {:descriptor descriptor
           :envelope-revision-pin revision-pin
           :builder-result raw
           :sealed-record sealed
           :envelope (:template sealed)
           :template-replay replay
           :contextual-verification contextual}
          trusted-preimage
          {:domain :gravity/sh26-coordinator-trusted-context-v1
           :resolution-binding-root
           (:binding-root resolution-binding)
           :descriptor-id (canonical-id descriptor)
           :sealed-record-id (canonical-id sealed)
           :template-replay-id (canonical-id replay)
           :contextual-verification-id
           (canonical-id contextual)}
          trusted
          {:artifact :gravity/sh26-coordinator-trusted-context
           :schema-version 1
           :authority :coordinator-held-clojure-seed
           :resolution-binding resolution-binding
           :resolution-binding-root
           (:binding-root resolution-binding)
           :descriptor descriptor
           :descriptor-id (:descriptor-id trusted-preimage)
           :sealed-record sealed
           :sealed-record-id (:sealed-record-id trusted-preimage)
           :template-replay replay
           :template-replay-id (:template-replay-id trusted-preimage)
           :contextual-verification contextual
           :contextual-verification-id
           (:contextual-verification-id trusted-preimage)
           :trusted-context-preimage trusted-preimage
           :trusted-context-id (canonical-id trusted-preimage)}]
      {:observed observed :trusted trusted})))

(defn- sh25-ingress []
  (let [request
        (invoke sh25-accepted-plan 'sh25-component-build-request [])
        result
        (invoke sh25-engine-plan
                'sh25-build-authoritative-components [request])
        verification
        (invoke sh25-engine-plan
                'sh25-verify-component-build [request result])
        projection
        (invoke sh25-engine-plan
                'sh25-project-verified-sh26-components
                [request result verification])
        request-id (canonical-id request)
        result-id (canonical-id result)
        verification-id (canonical-id verification)
        projection-id (canonical-id projection)
        provenance-root (canonical-id (:provenance result))
        ids {:request-id request-id
             :result-id result-id
             :verification-id verification-id
             :projection-id projection-id
             :provenance-root provenance-root}
        authentication-pair
        (authenticate-sh25-context
         request result verification projection ids)
        ingress
        {:artifact :gravity/sh25-complete-ingress
         :schema-version 1
         :request request
         :complete-result result
         :verification verification
         :projection projection
         :request-id request-id
         :complete-result-id result-id
         :verification-id verification-id
         :projection-id projection-id
         :sh25-provenance-root provenance-root
         :authentication (:observed authentication-pair)}]
    {:ingress ingress :trusted (:trusted authentication-pair)}))

(def ^:private authentic-context (delay (sh25-ingress)))
(def ^:private authentic-ingress
  (delay (:ingress @authentic-context)))
(def ^:private trusted-context
  (delay (:trusted @authentic-context)))

(defn- accepted-request
  ([]
   (invoke accepted-gravity-plan
           'sh26-stage-rebuild-request [@authentic-ingress]))
  ([plan function]
   (invoke plan function [@authentic-ingress])))

(defn- build [request]
  (invoke engine-plan
          'sh26-build-next-stage [request @trusted-context]))

(defn- build-with-trusted [request trusted]
  (invoke engine-plan
          'sh26-build-next-stage [request trusted]))

(defn- verify [request candidate]
  (invoke engine-plan 'sh26-verify-stage-rebuild
          [request @trusted-context candidate]))

(defn- verify-with-trusted [request trusted candidate]
  (invoke engine-plan 'sh26-verify-stage-rebuild
          [request trusted candidate]))

(defn- rejected-request [plan function base]
  (invoke plan function [base]))

(defn- rebind-context-without-resealing [base changed-ingress]
  (let [request (:request changed-ingress)
        result (:complete-result changed-ingress)
        verification (:verification changed-ingress)
        projection (:projection changed-ingress)
        ids {:request-id (canonical-id request)
             :result-id (canonical-id result)
             :verification-id (canonical-id verification)
             :projection-id (canonical-id projection)
             :provenance-root (canonical-id (:provenance result))}
        descriptor
        (sh26-envelope-descriptor
         request result verification projection ids)
        ingress
        (-> changed-ingress
            (assoc :request-id (:request-id ids))
            (assoc :complete-result-id (:result-id ids))
            (assoc :verification-id (:verification-id ids))
            (assoc :projection-id (:projection-id ids))
            (assoc :sh25-provenance-root (:provenance-root ids))
            (assoc-in [:authentication :descriptor] descriptor))]
    (-> base
        (assoc :sh25-ingress ingress)
        (assoc-in [:stage-compiler :sh25-request-id]
                  (:request-id ids))
        (assoc-in [:stage-compiler :sh25-complete-result-id]
                  (:result-id ids))
        (assoc-in [:stage-compiler :sh25-verification-id]
                  (:verification-id ids))
        (assoc-in [:stage-compiler :sh25-projection-id]
                  (:projection-id ids))
        (assoc-in [:stage-compiler :sh25-provenance-root]
                  (:provenance-root ids)))))

(defn- bounded-result [f]
  (let [task (future
               (try
                 {:value (f) :throwable nil}
                 (catch Throwable throwable
                   {:value nil :throwable throwable})))
        result (deref task 10000 ::timeout)]
    (when (= ::timeout result)
      (future-cancel task))
    result))

(deftest sh26-engine-and-all-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan
                sh25-engine-plan sh25-accepted-plan envelope-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (let [policy (invoke engine-plan 'sh26-stage-rebuild-policy [])
        catalog
        (invoke engine-plan 'sh26-authoritative-component-catalog [])]
    (is (= 2 (:schema-version policy)))
    (is (= 42 (:required-component-count policy)))
    (is (= 42 (count catalog)))
    (is (= :authenticated-envelope (ffirst catalog)))
    (is (= :runtime-subset (first (last catalog))))
    (is (some #{:external-sh25-recomputation} (:pending policy)))
    (is (some #{:candidate-action-execution} (:pending policy)))
    (is (false? (:self-hosted? policy))))
  (let [functions (:functions @engine-plan)]
    (is (= {:arity 2 :params ['request 'trusted-context]}
           (select-keys
            (get functions 'sh26-build-next-stage)
            [:arity :params])))
    (is (= {:arity 3
            :params ['request 'trusted-context 'candidate]}
           (select-keys
            (get functions 'sh26-verify-stage-rebuild)
            [:arity :params])))))

(deftest sh26-trust-anchor-is-out-of-band-and-result-bound
  (let [request (accepted-request)
        trusted @trusted-context
        observed
        (get-in request [:sh25-ingress :authentication])
        result (build request)
        verification (verify request result)
        malformed
        [(dissoc trusted :resolution-binding-root)
         (assoc trusted :authority :request-controlled)
         (assoc-in
          trusted
          [:resolution-binding :request-resolved-pairs
           2 :resolved-digest]
          "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
         (assoc trusted :trusted-context-id "")]]
    (is (not (contains? observed :trusted-resolution-binding)))
    (is (not (contains? observed
                        :trusted-resolution-binding-root)))
    (is (= (:trusted-context-id trusted)
           (:trusted-context-id result)))
    (is (= (:trusted-context-id trusted)
           (:trusted-context-id verification)))
    (doseq [changed malformed]
      (let [rejected (build-with-trusted request changed)]
        (is (= :rejected (:status rejected)))
        (is (= :invalid-sh25-ingress
               (get-in rejected
                       [:diagnostics 0 :facts :reason])))))
    (is (= :rejected
           (:status
            (verify-with-trusted
             request
             (dissoc trusted :trusted-context-id)
             result))))))

(deftest sh26-pins-authoritative-envelope-source-revision
  (let [base (accepted-request)
        revision
        (get-in base
                [:sh25-ingress :authentication :descriptor
                 :source-revision])
        pin
        (get-in base
                [:sh25-ingress :authentication
                 :envelope-revision-pin])]
    (is (= :sh-envelope (:owner revision)))
    (is (= "gravity/compiler/authenticated-envelope"
           (:logical-source-path revision)))
    (is (= "sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71"
           (:source-content-hash revision)))
    (is (= 59495 (:source-byte-count revision)))
    (is (= 72 (count (:function-shapes revision))))
    (is (= revision (:source-revision pin)))
    (is (= 'authenticated-envelope-verify-template
           (:verifier-function pin)))
    (is (= "sha256:e52b201a81ef82f857aaabc68cb2d6a8f0f4505f853c555816023f5dad294a77"
           (:verifier-semantic-hash pin)))
    (doseq [[label path value]
            [[:owner [:owner] :different-owner]
             [:source-hash [:source-content-hash]
              "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
             [:byte-count [:source-byte-count] 59494]
             [:plan [:plan-semantic-hash]
              "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"]
             [:functions [:functions-semantic-hash]
              "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"]
             [:builder [:builder-semantic-hash]
              "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"]
             [:builder-shape
              [:function-shapes
               'authenticated-envelope-build-template :arity]
              2]
             [:function-name
              [:function-shapes 'ae-append]
              nil]]]
      (let [request
            (assoc-in
             base
             (into
              [:sh25-ingress :authentication :descriptor
               :source-revision]
              path)
             value)
            result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= :invalid-sh25-ingress
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))
    (doseq [[label path value]
            [[:verifier-hash [:verifier-semantic-hash]
              "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"]
             [:verifier-function [:verifier-function]
              'different-verifier]
             [:pinned-private-shape
              [:source-revision :function-shapes
               'ae-reference-graph-walk :arity]
              3]]]
      (let [request
            (assoc-in
             base
             (into
              [:sh25-ingress :authentication
               :envelope-revision-pin]
              path)
             value)
            result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= :invalid-sh25-ingress
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))))

(deftest sh26-cross-binds-builder-replay-seal-and-context
  (let [base (accepted-request)
        authentication
        (get-in base [:sh25-ingress :authentication])
        builder (:builder-result authentication)
        replay (:template-replay authentication)
        sealed (:sealed-record authentication)
        contextual (:contextual-verification authentication)
        changed-template
        (assoc (:artifact-template builder)
               :stage :different-stage)
        paired-template
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :builder-result
              :artifact-template]
             changed-template)
            (assoc-in
             [:sh25-ingress :authentication :template-replay
              :artifact-template]
             changed-template))
        fewer-requests (pop (:digest-requests builder))
        paired-requests
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :builder-result
              :digest-requests]
             fewer-requests)
            (assoc-in
             [:sh25-ingress :authentication :template-replay
              :request-count]
             (count fewer-requests)))
        changed-checks
        (assoc-in (:identity-checks builder)
                  [0 :observed-id]
                  "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        paired-checks
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :builder-result
              :identity-checks]
             changed-checks)
            (assoc-in
             [:sh25-ingress :authentication :template-replay
              :identity-checks]
             changed-checks))
        changed-request
        (assoc-in
         (get (:digest-requests builder) 2)
         [:preimage :semantic-value :content-id]
         "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        changed-builder
        (assoc-in builder [:digest-requests 2] changed-request)
        changed-sealed-request
        (assoc-in sealed [:requests 2] changed-request)
        coordinated-request
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :builder-result]
             changed-builder)
            (assoc-in
             [:sh25-ingress :authentication :sealed-record]
             changed-sealed-request)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification :builder-result]
             changed-builder)
            (assoc-in
             [:sh25-ingress :authentication
             :contextual-verification :sealed-record]
             changed-sealed-request))
        changed-request-binding
        (-> (:host-resolution-binding sealed)
            (assoc-in
             [:request-resolved-pairs 2 :request]
             changed-request)
            (assoc-in
             [:binding-preimage :request-resolved-pairs
              2 :request]
             changed-request))
        fully-changed-sealed-request
        (assoc changed-sealed-request
               :host-resolution-binding
               changed-request-binding)
        fully-coordinated-request
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :builder-result]
             changed-builder)
            (assoc-in
             [:sh25-ingress :authentication :sealed-record]
             fully-changed-sealed-request)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification :builder-result]
             changed-builder)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification :sealed-record]
             fully-changed-sealed-request)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification
              :host-resolution-binding]
             changed-request-binding))
        changed-resolved
        "sha256:abababababababababababababababababababababababababababababababab"
        changed-sealed-resolved
        (assoc-in sealed [:resolved-digests 2] changed-resolved)
        coordinated-resolved
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :sealed-record]
             changed-sealed-resolved)
            (assoc-in
             [:sh25-ingress :authentication
             :contextual-verification :sealed-record]
             changed-sealed-resolved))
        changed-resolved-binding
        (-> (:host-resolution-binding sealed)
            (assoc-in
             [:request-resolved-pairs 2 :resolved-digest]
             changed-resolved)
            (assoc-in
             [:binding-preimage :request-resolved-pairs
              2 :resolved-digest]
             changed-resolved))
        fully-changed-sealed-resolved
        (assoc changed-sealed-resolved
               :host-resolution-binding
               changed-resolved-binding)
        fully-coordinated-resolved
        (-> base
            (assoc-in
             [:sh25-ingress :authentication :sealed-record]
             fully-changed-sealed-resolved)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification :sealed-record]
             fully-changed-sealed-resolved)
            (assoc-in
             [:sh25-ingress :authentication
              :contextual-verification
              :host-resolution-binding]
             changed-resolved-binding))
        cases
        [[:paired-template paired-template]
         [:paired-digest-requests paired-requests]
         [:paired-identity-checks paired-checks]
         [:coordinated-request coordinated-request]
         [:fully-coordinated-request
          fully-coordinated-request]
         [:coordinated-resolved coordinated-resolved]
         [:fully-coordinated-resolved
          fully-coordinated-resolved]
         [:trusted-binding-root
          (assoc-in
           base
           [:sh25-ingress :authentication
            :trusted-resolution-binding-root]
           "sha256:cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd")]
         [:replay-root
          (assoc-in
           base
           [:sh25-ingress :authentication :template-replay
            :semantic-envelope-root]
           {:digest-ref 0})]
         [:replay-diagnostics
          (assoc-in
           base
           [:sh25-ingress :authentication :template-replay
            :diagnostics]
           [:altered])]
         [:replay-authority
          (assoc-in
           base
           [:sh25-ingress :authentication :template-replay
            :semantic-authority]
           :host)]
         [:sealed-template
          (assoc-in
           base
           [:sh25-ingress :authentication :sealed-record
            :template :stage]
           :altered)]
         [:context-builder
          (assoc-in
           base
           [:sh25-ingress :authentication
            :contextual-verification :builder-result :status]
           :rejected)]]]
    (doseq [[label request] cases]
      (let [result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= :invalid-sh25-ingress
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))))

(deftest sh26-consumes-the-final-authenticated-sh25-projection
  (let [gravity-request (accepted-request)
        qst-request
        (accepted-request accepted-qst-plan
                          'sh26-stage-rebuild-request)
        gravity (build gravity-request)
        qst (build qst-request)
        verification (verify gravity-request gravity)]
    (is (= :accepted
           (get-in gravity-request
                   [:sh25-ingress :complete-result :status])))
    (is (= :passed
           (get-in gravity-request
                   [:sh25-ingress :verification :status])))
    (is (= :accepted
           (get-in gravity-request
                   [:sh25-ingress :projection :status])))
    (is (= (canonical-id
            (get-in gravity-request [:sh25-ingress :request]))
           (get-in gravity-request [:sh25-ingress :request-id])))
    (is (= (canonical-id
            (get-in gravity-request
                    [:sh25-ingress :complete-result]))
           (get-in gravity-request
                   [:sh25-ingress :complete-result-id])))
    (is (= (canonical-id
            (get-in gravity-request
                    [:sh25-ingress :verification]))
           (get-in gravity-request
                   [:sh25-ingress :verification-id])))
    (is (= (canonical-id
            (get-in gravity-request [:sh25-ingress :projection]))
           (get-in gravity-request
                   [:sh25-ingress :projection-id])))
    (is (= (get-in gravity-request
                   [:sh25-ingress :complete-result-id])
           (get-in gravity-request
                   [:sh25-ingress :authentication :descriptor
                    :identity-subjects 4 :preimage
                    :complete-result-id])))
    (is (= :gravity/authenticated-envelope
           (get-in gravity-request
                   [:sh25-ingress :authentication :envelope
                    :artifact])))
    (is (= :template-replay-passed
           (get-in gravity-request
                   [:sh25-ingress :authentication
                    :template-replay :status])))
    (is (= :contextual-verification-passed
           (get-in gravity-request
                   [:sh25-ingress :authentication
                    :contextual-verification :status])))
    (is (= 42
           (count
            (get-in gravity-request
                    [:sh25-ingress :projection :components]))))
    (is (= gravity-request qst-request))
    (is (= gravity qst))
    (is (= :accepted (:status gravity)))
    (is (= 42 (count (:actions gravity))))
    (is (= (mapv :component-id
                 (get-in gravity-request
                         [:sh25-ingress :projection :components]))
           (mapv :component-id (:actions gravity))))
    (is (every? #(= :gravity (:executor %)) (:actions gravity)))
    (is (every? #(= :pending (:execution-status %))
                (:actions gravity)))
    (is (= 42
           (count
            (get-in gravity
                    [:stage-manifest :component-output-ids]))))
    (is (= :pending
           (get-in gravity [:rebuild-record :execution-status])))
    (is (= :passed (:status verification)))
    (is (some #{:exact-sh25-complete-result}
              (:checks verification)))
    (is (some #{:complete-42-component-catalog}
              (:checks verification)))))

(deftest sh26-lineage-manifest-and-provenance-are-complete
  (let [request (accepted-request)
        result (build request)
        lineage (:prior-stage-lineage result)
        transition (:stage-transition result)
        manifest (:stage-manifest result)
        paths (get-in result [:provenance :component-sources])]
    (is (= 2 (count lineage)))
    (is (= (get-in lineage [0 :compiler-id])
           (get-in lineage [1 :parent-compiler-id])))
    (is (= (get-in lineage [0 :stage-output-id])
           (get-in lineage [1 :input-stage-output-id])))
    (is (= :compiled-by-prior-stage (:semantics transition)))
    (is (= (get-in lineage [1 :stage-output-id])
           (:input-stage-output-id transition)))
    (is (= 42 (count paths)))
    (is (every? #(and (keyword? (:component-id %))
                      (seq (:actual-source-path %)))
                paths))
    (is (= "sha256:stage-n-source" (:source-hash manifest)))
    (is (= "sha256:stage-n-compiler" (:compiler-hash manifest)))
    (is (= "sha256:bootstrap-lock" (:lock-hash manifest)))
    (is (= "sha256:sh26-target" (:target-hash manifest)))
    (is (= "sha256:stage-n-plus-one-output"
           (:output-hash manifest)))
    (is (= 42 (get-in manifest [:build-logs :action-count])))
    (is (= :pending
           (get-in manifest [:build-logs :execution-status])))))

(deftest sh26-identities-are-path-neutral
  (let [left (build (accepted-request))
        right
        (build
         (accepted-request
          accepted-gravity-plan
          'sh26-stage-rebuild-alternate-path-request))]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (= 42
           (count
            (get-in left [:provenance :component-sources]))))))

(deftest sh26-rejects-closed-request-families
  (let [base (accepted-request)]
    (doseq [[function rule reason]
            [['sh26-uncontrolled-environment-request
              "BOOT6001" :uncontrolled-environment]
             ['sh26-unlocked-dependency-request
              "BOOT6001" :uncontrolled-environment]
             ['sh26-missing-lineage-request
              "BOOT6002" :invalid-compiler-lineage]
             ['sh26-host-language-request
              "BOOT3002" :candidate-host-authority]
             ['sh26-altered-sh25-projection-request
              "BOOT3001" :invalid-sh25-ingress]
             ['sh26-incomplete-component-request
              "BOOT3001" :invalid-sh25-ingress]
             ['sh26-lineage-cycle-request
              "BOOT6002" :invalid-compiler-lineage]
             ['sh26-missing-provenance-request
              "BOOT3001" :invalid-sh25-ingress]
             ['sh26-invalid-manifest-request
              "BOOT1001" :rebuild-evidence]]]
      (testing (str function)
        (let [gravity
              (build
               (rejected-request rejected-gravity-plan
                                 function base))
              qst
              (build
               (rejected-request rejected-qst-plan
                                 function base))]
          (is (= gravity qst))
          (is (= :rejected (:status gravity)))
          (is (= rule (get-in gravity [:diagnostics 0 :rule])))
          (is (= reason
                 (get-in gravity
                         [:diagnostics 0 :facts :reason])))
          (is (true?
               (get-in gravity
                       [:diagnostics 0 :facts :fail-closed]))))))))

(deftest sh26-rejects-paired-sh25-alterations
  (let [base (accepted-request)
        cases
        [[:template
          (assoc-in
          base
          [:sh25-ingress :complete-result
           :sh26-component-templates 0 :output-id]
          "sha256:altered-template")
          :invalid-sh25-ingress]
         [:action
          (assoc-in
          base
          [:sh25-ingress :complete-result :actions 0 :output-id]
          "sha256:altered-action")
          :invalid-sh25-ingress]
         [:verification
          (assoc-in
          base
          [:sh25-ingress :verification :component-count] 41)
          :invalid-sh25-ingress]
         [:projection-verification
          (assoc-in
          base
          [:sh25-ingress :projection :verification
           :component-count] 41)
          :invalid-sh25-ingress]
         [:compiler-projection-binding
          (assoc-in
          base
          [:stage-compiler :sh25-projection-id]
          "sha256:altered-projection-id")
          :invalid-compiler-lineage]]]
    (doseq [[label request reason] cases]
      (let [result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= reason
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label " " (pr-str
                            (get-in result [:diagnostics 0]))))))))

(deftest sh26-rejects-complete-sh25-record-and-context-alterations
  (let [base (accepted-request)
        altered-census
        (assoc-in
         base
         [:sh25-ingress :complete-result :request-census :nodes]
         1)
        altered-verification
        (assoc-in
         base
         [:sh25-ingress :verification :candidate-census :nodes]
         1)
        paired-verification
        (assoc-in
         altered-verification
         [:sh25-ingress :projection :verification]
         (get-in altered-verification
                 [:sh25-ingress :verification]))
        cases
        [[:result-identity
          (assoc-in
           base
           [:sh25-ingress :complete-result :identity-input
            :build-id]
           :altered)
          :invalid-sh25-ingress]
         [:request-census altered-census :invalid-sh25-ingress]
         [:pending
          (assoc-in
           base
           [:sh25-ingress :complete-result :pending 0]
           :altered)
          :invalid-sh25-ingress]
         [:seed-flag
          (assoc-in
           base
           [:sh25-ingress :complete-result
            :clojure-seed-boundary?]
           false)
          :invalid-sh25-ingress]
         [:paired-verification-projection
          paired-verification
          :invalid-sh25-ingress]
         [:authentication-subject
          (assoc-in
           base
           [:sh25-ingress :authentication :descriptor
            :identity-subjects 4 :preimage :complete-result-id]
           "sha256:altered")
          :invalid-sh25-ingress]]]
    (doseq [[label request reason] cases]
      (let [result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= reason
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))))

(deftest sh26-genuine-envelope-rejects-self-consistent-context-substitution
  (let [base (accepted-request)
        ingress (:sh25-ingress base)
        altered-result-provenance
        (assoc-in
         ingress
         [:complete-result :provenance :physical
          :actual-build-root]
         "/alternate/build")
        altered-result-identity
        (assoc-in
         ingress
         [:complete-result :identity-input :build-id]
         :alternate-build)
        altered-request-component
        (assoc-in
         ingress
         [:request :components 0 :source-identity :content-hash]
         "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        altered-verification
        (assoc-in
         ingress
         [:verification :candidate-census :nodes]
         15084)
        altered-verification-projection
        (assoc-in
         altered-verification
         [:projection :verification]
         (:verification altered-verification))
        cases
        [[:result-provenance altered-result-provenance]
         [:result-identity altered-result-identity]
         [:request-component altered-request-component]
         [:verification-projection
          altered-verification-projection]]]
    (doseq [[label changed-ingress] cases]
      (let [request
            (rebind-context-without-resealing
             base changed-ingress)
            result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= :invalid-sh25-ingress
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))))

(deftest sh26-cross-binds-evidence-and-pending-process-records
  (let [base (accepted-request)
        cases
        [[:lock-hash
          (assoc-in base [:rebuild-evidence :lock-hash]
                    "sha256:different-lock")
          :rebuild-evidence]
         [:target-hash
          (assoc-in base [:rebuild-evidence :target-hash]
                    "sha256:different-target")
          :rebuild-evidence]
         [:target-profile
          (assoc-in base [:target :profile] :runtime)
          :target]
         [:target-backend
          (assoc-in base [:target :backend] :wasm)
          :target]
         [:target-id
          (assoc-in base [:target :target-id] "")
          :target]
         [:runtime-id
          (assoc-in base [:target :runtime-id] "")
          :target]
         [:toolchain-identity
          (assoc-in base
                    [:rebuild-evidence :toolchain :toolchain-id]
                    "gravityc:different")
          :rebuild-evidence]
         [:toolchain-target
          (assoc-in base
                    [:rebuild-evidence :toolchain :target]
                    "different:target")
          :rebuild-evidence]
         [:stdout-before-execution
          (assoc-in base
                    [:rebuild-evidence :build-logs :stdout
                     :content-id]
                    "sha256:premature-stdout")
          :rebuild-evidence]
         [:stderr-status
          (assoc-in base
                    [:rebuild-evidence :build-logs :stderr :status]
                    :accepted)
          :rebuild-evidence]
         [:exit-before-execution
          (assoc-in base
                    [:rebuild-evidence :build-logs :exit :exit-code]
                    0)
          :rebuild-evidence]
         [:duplicate-stage-output
          (assoc-in
           base [:stage-lineage 1 :stage-output-id]
           (get-in base [:stage-lineage 0 :stage-output-id]))
          :invalid-compiler-lineage]
         [:duplicate-next-output
          (assoc-in
           base [:rebuild-evidence :output-hash]
           (get-in base
                   [:sh25-ingress :projection :components
                    0 :output-id]))
          :rebuild-evidence]]]
    (doseq [[label request reason] cases]
      (let [result (build request)]
        (is (= :rejected (:status result)) (str label))
        (is (= reason
               (get-in result
                       [:diagnostics 0 :facts :reason]))
            (str label))))))

(deftest sh26-carrier-boundaries-fail-closed-before-replay
  (let [request (accepted-request)
        lazy-carrier (iterate inc 0)
        throwing-carrier
        (lazy-seq (throw (ex-info "must not realize" {})))
        oversized-carrier
        (assoc request :actual-rebuild-root
               (apply str (repeat 262145 "x")))]
    (doseq [[label carrier reason]
            [[:lazy lazy-carrier :carrier-sequence-unsupported]
             [:throwing throwing-carrier
              :carrier-sequence-unsupported]
             [:oversized oversized-carrier
              :carrier-scalar-bound]]]
      (testing (str label " build")
        (let [outcome (bounded-result #(build carrier))]
          (is (not= ::timeout outcome))
          (is (nil? (:throwable outcome)))
          (is (= :rejected (get-in outcome [:value :status])))
          (is (= reason
                 (get-in outcome
                         [:value :diagnostics 0 :facts :reason])))))
      (testing (str label " verify candidate")
        (let [outcome (bounded-result #(verify request carrier))]
          (is (not= ::timeout outcome))
          (is (nil? (:throwable outcome)))
          (is (= :rejected (get-in outcome [:value :status])))
          (is (= reason
                 (get-in outcome
                         [:value :diagnostics 0 :facts :reason])))))
      (testing (str label " verify request")
        (let [outcome (bounded-result #(verify carrier {}))]
          (is (not= ::timeout outcome))
          (is (nil? (:throwable outcome)))
          (is (= :rejected (get-in outcome [:value :status])))
          (is (= reason
                 (get-in outcome
                         [:value :diagnostics 0 :facts :reason]))))))))

(deftest sh26-result-substitution-fails
  (let [request (accepted-request)
        result (build request)
        alterations
        [(assoc-in result [:actions 0 :executor] :clojure)
         (assoc-in result [:stage-manifest :output-hash]
                   "sha256:altered-output")
         (assoc-in result
                   [:provenance :component-sources 0
                    :actual-source-path]
                   "/different/source.gravity")
         (assoc-in result
                   [:rebuild-record :execution-status] :passed)]]
    (doseq [altered alterations]
      (let [verification (verify request altered)]
        (is (= :rejected (:status verification)))
        (is (= :stage-rebuild-result-altered
               (get-in verification
                       [:diagnostics 0 :facts :reason])))))))

(deftest sh26-fixture-pairs-are-byte-identical
  (is (= (slurp (path (str fixture-root
                            "/accepted/stage-rebuild.gravity")))
         (slurp (path (str fixture-root
                            "/accepted/stage-rebuild.qst")))))
  (is (= (slurp
          (path (str fixture-root
                     "/rejected/invalid-stage-rebuild.gravity")))
         (slurp
          (path (str fixture-root
                     "/rejected/invalid-stage-rebuild.qst"))))))
