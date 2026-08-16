(ns gravity.self-hosting.sh08-higher-order-function-value-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_higher_order_function_value_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 HO1 test source is not on the classpath"
                {:id "SH08-HO1-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH08-HO1-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
(def ^:private bridge-relative-path
  (str fixture-root "/higher_order_function_value_bridge.gravity"))
(def ^:private envelope-relative-path
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")
(def ^:private bridge-plan
  (delay
    (let [source-path (str (.resolve @root bridge-relative-path))
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))
(def ^:private envelope-plan
  (delay
    (let [source-path (str (.resolve @root envelope-relative-path))
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- invoke-bridge [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-higher-order-function-value
    :compiler-artifact-plan? true}
   @bridge-plan function arguments))

(defn- invoke-envelope [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-higher-order-function-value-envelope
    :compiler-artifact-plan? true}
   @envelope-plan function arguments))

(defn- fixture-artifact [family basename extension]
  (bootstrap/sh07-core-file-artifact
   (path (str fixture-root "/" family "/" basename extension))))

(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh08-ho1-test>" value))

(defn- sha256-id [value]
  (canonical-id value))

(defn- identity-id [domain value]
  (canonical-id {:domain domain :semantic-input value}))

(defn- hostile-assoc [value key replacement]
  (if (associative? value)
    (assoc value key replacement)
    {:invalid-hostile-association-target value}))

(defn- hostile-assoc-in [value path replacement]
  (if (associative? (get-in value (butlast path)))
    (assoc-in value path replacement)
    (hostile-assoc value :invalid-hostile-association-path path)))

(defn- source-hash [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count [source-text]
  (alength
   (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)))

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

(defn- function-shapes [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

(defn- compiled-local-call-arity-mismatches [plan]
  (let [arities (into {}
                      (map (fn [[name function]]
                             [name (:arity function)]))
                      (:functions plan))
        mismatches (atom [])]
    (walk/prewalk
     (fn [value]
       (when (and (map? value)
                  (= :function-call (:op value))
                  (contains? arities (:function value))
                  (not= (get arities (:function value))
                        (count (:args value))))
         (swap! mismatches conj
                {:function (:function value)
                 :expected (get arities (:function value))
                 :actual (count (:args value))}))
       value)
     (mapv :instructions (vals (:functions plan))))
    @mismatches))

(defn- bridge-source-revision []
  (let [source-text (slurp (path bridge-relative-path))
        plan @bridge-plan
        builder 'sh08-build-higher-order-function-value-request]
    {:owner :sh-types
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-08/higher-order-function-value-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan))
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) builder))
     :function-shapes (function-shapes plan)}))

(defn- fact-transition [name evidence-id]
  (let [value {:family name :entries []}]
    {:name name
     :disposition :preserved
     :input value
     :output value
     :input-count (count value)
     :output-count (count value)
     :evidence-ids [evidence-id]}))

(defn- descriptor [artifact actual-path]
  (let [canonical-core (core artifact)
        core-id (:artifact-id canonical-core)
        evidence-id
        (canonical-id
         {:domain :gravity/sh08-ho1-evidence-v1
          :core-artifact-id core-id
          :source-path-class :fixture})
        plan-preimage
        {:domain :gravity/sh08-ho1-plan-v1
         :core-artifact-id core-id
         :dispatch :direct-function-value}
        core-subject-id
        (identity-id :gravity/sh08-ho1-sh07-core-v1
                     {:artifact-id core-id})
        plan-subject-id
        (identity-id :gravity/sh08-ho1-plan-v1 plan-preimage)
        facts (mapv #(fact-transition % evidence-id)
                    [:type :effect :ownership :capability :safety])]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh08-ho1-function-value-bridge
     :artifact-kind :gravity/sh08-ho1-function-value-boundary
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind :gravity/sh08-ho1-envelope-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections
      [:sh07-canonical-core :sh08-ho1-function-value]
      :required-fact-families [:type :effect :ownership :capability :safety]
      :required-identity-subjects [:sh07-canonical-core :sh08-ho1-plan]}
     :semantic-projections
     [{:name :sh07-canonical-core
       :role :verified-core-identity
       :entry-count 1
       :value {:artifact-id core-id}}
      {:name :sh08-ho1-function-value
       :role :function-value-boundary
       :entry-count 3
       :value {:domain :gravity/sh08-ho1-function-value-v1
               :dispatch :direct-function-value
               :path-neutral true}}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :checked}]
      :proof-certificate-table {evidence-id {:status :checked}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage [{:proof-id evidence-id
                     :used-by :sh08-ho1-function-value}]}
     :preservation
     {:requires [:type :effect :ownership :capability :safety]
      :preserves [:type :effect :ownership :capability :safety]
      :invalidates []
      :regenerates []
      :residual-checks [:sh07-verification :sh02-envelope
                        :function-value-call-shape]}
     :identity-subjects
     [{:name :sh07-canonical-core
       :domain :gravity/sh08-ho1-sh07-core-v1
       :preimage {:artifact-id core-id}
       :observed-id core-subject-id}
      {:name :sh08-ho1-plan
       :domain :gravity/sh08-ho1-plan-v1
       :preimage plan-preimage
       :observed-id plan-subject-id}]
     :lineage
     [{:stage :sh07-core
       :artifact-kind :gravity/sh07-canonical-core-artifact
       :semantic-id core-subject-id
       :artifact-id core-id
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh08-ho1
       :artifact-kind :gravity/sh08-ho1-function-value-boundary
       :semantic-id plan-subject-id
       :artifact-id
       (canonical-id {:domain :gravity/sh08-ho1-plan-v1
                      :semantic-id plan-subject-id})
       :verification-id evidence-id
       :relation :fixture-local-adapter}]
     :reference-closure
     {:root-id "sh08-ho1"
      :node-ids ["sh08-ho1" "sh07-core" "function-value" "call"]
      :edges [{:from "sh08-ho1" :role :consumes :to "sh07-core"}
              {:from "sh08-ho1" :role :produces :to "function-value"}
              {:from "function-value" :role :used-by :to "call"}]
      :fact-reference-ids ["fact/type" "fact/effect"
                           "fact/ownership" "fact/capability" "fact/safety"]
      :origin-reference-ids ["origin/sh07" "origin/sh08-ho1"]
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 4
      :observed-edge-count 3
      :observed-maximum-depth 2}
     :actual-path-provenance
     {:source-path actual-path
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- seal-envelope [raw]
  (let [requests (:digest-requests raw)
        request-count (count requests)
        resolved
        (reduce
         (fn [values request]
           (let [ordinal (:ordinal request)
                 preimage
                 (bootstrap/p15-s23-c6c10-resolve-digest-references!
                  "<sh08-ho1-envelope>" (:preimage request)
                  request-count ordinal values)]
             (conj values (canonical-id preimage))))
         [] requests)
        resolve-value
        (fn [value]
          (bootstrap/p15-s23-c6c10-resolve-digest-references!
           "<sh08-ho1-envelope>" value request-count nil resolved))]
    {:envelope (resolve-value (:artifact-template raw))
     :semantic-root (resolve-value (:semantic-envelope-root raw))
     :provenance-root (resolve-value (:provenance-binding-root raw))
     :identity-checks (resolve-value (:identity-checks raw))}))

(defn- contextual-envelope [descriptor-value]
  (let [raw (invoke-envelope
             'authenticated-envelope-build-template [descriptor-value])
        replay (invoke-envelope
                'authenticated-envelope-verify-template
                [descriptor-value (:artifact-template raw)
                 (:digest-requests raw)])
        sealed (seal-envelope raw)]
    {:raw raw
     :replay replay
     :sealed sealed
     :verification
     {:artifact :gravity/sh08-ho1-envelope-contextual-verification
      :status :contextual-verification-passed
     :semantic-envelope-root (:semantic-root sealed)
     :provenance-binding-root (:provenance-root sealed)
     :identity-enforcement :passed
      :host-digest-resolution :passed
      :identity-checks (:identity-checks sealed)
      :eligible-for-contextual-acceptance? true}}))

(defn- b47-context [artifact verification]
  (let [canonical-core (core artifact)]
    {:artifact-id (:artifact-id artifact)
     :artifact-status (:status artifact)
     :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (:identity-preimage canonical-core)
     :authenticated-core-request
     (get-in artifact [:gravity-core-boundary :authenticated-core-request])
     :canonical-core-artifact canonical-core
     :canonical-identity-preimage (:identity-preimage canonical-core)
     :provenance (:provenance artifact)
     :lineage (get-in artifact
                      [:gravity-core-boundary
                       :authenticated-core-request :lineage])
     :authenticated-wrapper artifact
     :verification {:status (:status verification)
                    :checks (:checks verification)
                    :failed-checks (:failed-checks verification)
                    :verified-artifact-id (:verified-artifact-id verification)
                    :verification-report-id
                    (:verification-report-id verification)
                    :check-catalog (:check-catalog verification)}}))

(defn- b47-verification [artifact]
  (let [report (bootstrap/sh07-core-artifact-verification artifact)]
    (assoc report
           :verified-artifact-id (:artifact-id artifact)
           :check-catalog (set (keys (:checks report)))
           :verification-report-id
           (canonical-id
            {:domain :gravity/sh08-ho1-b47-verification-v1
             :verified-artifact-id (:artifact-id artifact)
             :checks (:checks report)
             :failed-checks (:failed-checks report)}))))

(defn- request-for [family basename extension function-name apply-name]
  (let [actual-path (path (str fixture-root "/" family "/" basename extension))
        artifact (fixture-artifact family basename extension)
        verification (b47-verification artifact)
        context (b47-context artifact verification)
        descriptor-value (descriptor artifact actual-path)
        envelope (contextual-envelope descriptor-value)]
    {:artifact :gravity/sh08-higher-order-function-value-request
     :schema-version 1
     :sh07-artifact artifact
     :sh07-verification verification
     :sh07-check-catalog (:check-catalog verification)
     :sh07-context context
     :envelope-descriptor descriptor-value
     :envelope (get-in envelope [:sealed :envelope])
     :envelope-replay (:replay envelope)
     :envelope-verification
     (assoc (:verification envelope)
            :semantic-envelope-root
            (get-in envelope [:sealed :semantic-root])
            :provenance-binding-root
            (get-in envelope [:sealed :provenance-root])
            :sh07-verification verification
            :artifact-template (get-in envelope [:sealed :envelope])
            :identity-subject-equality :passed
            :fresh-envelope-reconstruction :passed
            :source-revision (:source-revision descriptor-value)
            :source-revision-id
            (get-in envelope [:sealed :envelope :source-revision-id])
            :replay-artifact-template
            (get-in envelope [:replay :artifact-template])
            :replay-semantic-envelope-root
            (get-in envelope [:replay :semantic-envelope-root])
            :replay-provenance-binding-root
            (get-in envelope [:replay :provenance-binding-root])
            :replay-identity-checks
            (get-in envelope [:replay :identity-checks]))
     :function-value-name function-name
     :apply-function-name apply-name
     :actual-source-path actual-path}))

(defn- run-request [request]
  (let [bridge-request
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [request])
        result
        (invoke-bridge
         'sh08-bind-higher-order-function-value-result
         [request bridge-request])]
    {:bridge-request bridge-request
     :result result
     :verification
     (invoke-bridge
      'sh08-verify-higher-order-function-value-result
      [request bridge-request result])}))

(deftest sh08-ho1-bridge-has-narrow-exported-api
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @bridge-plan)))
  (is (= {:arity 0 :params []}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-higher-order-function-value-policy])
          [:arity :params])))
  (is (= {:arity 1 :params ['request]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-build-higher-order-function-value-request])
          [:arity :params])))
  (is (= {:arity 2 :params ['request 'bridge-request]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-bind-higher-order-function-value-result])
          [:arity :params])))
  (is (= {:arity 3 :params ['request 'bridge-request 'candidate]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-verify-higher-order-function-value-result])
          [:arity :params])))
  (let [instructions
        (get-in @bridge-plan
                [:functions
                 'sh08-build-higher-order-function-value-request
                 :instructions])]
    (is (= 1 (count instructions)))
    (is (= :let (get-in instructions [0 :op])))
    (is (= 1 (count (get-in instructions [0 :body]))))
    (is (= :if (get-in instructions [0 :body 0 :op])))
    (is (= {:op :local :name 'validation}
           (get-in instructions [0 :body 0 :else]))))
  (is (= [] (compiled-local-call-arity-mismatches @bridge-plan))))

(deftest sh08-ho1-context-authenticated-core-request-is-linked
  (let [verification
        {:status :passed
         :checks {:bound true}
         :failed-checks []
         :verified-artifact-id "artifact-id"
         :verification-report-id "verification-id"
         :check-catalog #{:bound}}
        artifact
        {:artifact-id "artifact-id"
         :status :passed
         :gravity-core-boundary
         {:authenticated-core-request :authenticated-request}}
        canonical-core
        {:artifact-id "artifact-id"
         :identity-preimage {:semantic :core}}
        context
        {:artifact-id "artifact-id"
         :artifact-status :passed
         :input-domain :gravity/sh07-b47-canonical-core-v16
         :identity-input {:semantic :core}
         :canonical-core-artifact canonical-core
         :authenticated-core-request :authenticated-request
         :canonical-identity-preimage {:semantic :core}
         :provenance {:source-path "/physical/source.gravity"}
         :lineage []
         :verification verification
         :authenticated-wrapper artifact}]
    (is (= true
           (invoke-bridge
            'ho1-context-valid? [context artifact verification])))))

(deftest sh08-ho1-hostile-mutations-remain-data
  (is (= {:invalid-hostile-association-target :accepted}
         (hostile-assoc :accepted :dispatch :dynamic)))
  (is (= {:carrier :accepted
          :invalid-hostile-association-path [:carrier :status]}
         (hostile-assoc-in {:carrier :accepted}
                           [:carrier :status]
                           :rejected)))
  (is (= {:carrier {:status :rejected}}
         (hostile-assoc-in {:carrier {:status :accepted}}
                           [:carrier :status]
                           :rejected))))

(deftest sh08-ho1-invalid-sh07-report-is-structured-data
  (let [request
        {:artifact :gravity/sh08-higher-order-function-value-request
         :schema-version 1
         :sh07-artifact {}
         :sh07-verification {}
         :sh07-check-catalog #{}
         :sh07-context {}
         :envelope-descriptor {}
         :envelope {}
         :envelope-replay {}
         :envelope-verification {}
         :function-value-name 'function-value
         :apply-function-name 'apply-function
         :actual-source-path "/physical/source.gravity"}
        result
        (invoke-bridge 'ho1-request-validation [request])]
    (is (= :rejected (:status result)))
    (is (= "STD08-HO-SH07" (get-in result [:diagnostics 0 :rule])))
    (is (= :verified-b47-report-required
           (get-in result [:diagnostics 0 :reason])))
    (is (= :passed (get-in result [:diagnostics 0 :expected])))
    (is (nil? (get-in result [:diagnostics 0 :actual])))))

(deftest sh08-ho1-accepted-shape-validator-returns-true
  (let [function-fact
        {:function-value-id "function-id"
         :definition-binding-id "function-binding"
         :fixed-arity 1
         :dispatch :direct-function-value
         :mutability :immutable
         :type {:kind :function-value}}
        call-fact
        {:call-core-node-id "inner-call"
         :dispatch :direct-function-value
         :evaluation-order :operator-then-arguments
         :fixed-arity 1
         :parameter-type :gravity.type/unknown
         :argument-types [:gravity.type/i64]
         :result-type :gravity.type/unknown
         :operator-binding-id "callable-parameter"
         :callable-parameter-binding-id "callable-parameter"
         :outer-apply-call-core-node-id "outer-call"
         :outer-function-value-argument-core-node-id "outer-argument"
         :outer-function-value-binding-id "function-binding"
         :outer-apply-operator-binding-id "apply-binding"
         :outer-call-evaluation-order :operator-then-arguments
         :outer-function-value-argument-ordinal 0}
        result
        {:status :accepted
         :canonical-core-artifact-id "core-id"
         :function-value-facts [function-fact]
         :call-facts [call-fact]}]
    (is (= true
           (invoke-bridge 'ho1-accepted-result-valid? [result])))
    (is (= false
           (invoke-bridge
            'ho1-accepted-result-valid?
            [(assoc result :status :rejected)])))
    (is (= false
           (invoke-bridge
            'ho1-accepted-result-valid?
            [(assoc result :function-value-facts
                    [function-fact function-fact])])))
    (is (= false
           (invoke-bridge
            'ho1-accepted-result-valid?
            [(assoc result :canonical-core-artifact-id "function-id")])))
    (is (= false
           (invoke-bridge
            'ho1-accepted-result-valid?
            [(assoc result :call-facts
                    [(assoc call-fact
                            :outer-apply-call-core-node-id nil)])])))
    (is (= {:status :accepted
            :canonical-core-artifact-id "core-id"
            :function-value-fact-count 1
            :call-fact-count 1
            :function-value-id "function-id"
            :call-core-node-id "inner-call"}
           (invoke-bridge 'ho1-result-shape-summary [result])))))

(deftest sh08-ho1-fixture-pairs-are-co-canonical
  (doseq [[family basename]
          [["accepted" "function-value-call"]
           ["rejected" "function-value-nonfunction"]
           ["rejected" "function-value-capture"]]]
    (is (= (slurp (path (str fixture-root "/" family "/" basename ".gravity")))
           (slurp (path (str fixture-root "/" family "/" basename ".qst")))))))

(deftest sh08-ho1-accepts-authenticated-function-value-call
  (doseq [extension [".gravity" ".qst"]]
    (let [request
          (request-for "accepted" "function-value-call" extension
                       'sh08-ho1-identity 'sh08-ho1-apply-one)
          run (run-request request)
          bridge-request (:bridge-request run)
          result (:result run)
          diagnostic (first (:diagnostics bridge-request))]
      (is (= :passed
             (get-in request [:sh07-verification :status]))
          extension)
      (is (= (bridge-source-revision)
             (get-in request
                     [:envelope-descriptor :source-revision]))
          extension)
      (is (not=
           (get-in request
                   [:envelope-descriptor :source-revision
                    :logical-source-path])
           (get-in request
                   [:envelope-descriptor :actual-path-provenance
                    :source-path]))
          extension)
      (is (= :accepted (:status bridge-request))
          [extension
           (select-keys diagnostic [:rule :reason :expected :actual])])
      (when (= :accepted (:status bridge-request))
        (is (= :accepted (:status result))
            [extension (:diagnostics result)])
        (is (= :passed (:status (:verification run)))
            [extension (:diagnostics (:verification run))])
        (is (= :direct-function-value (:dispatch result)) extension)
        (is (= 1 (count (:function-value-facts result))) extension)
        (is (= 1 (count (:call-facts result))) extension)
        (is (= :operator-then-arguments
               (get-in result [:call-facts 0 :evaluation-order]))
            extension)
        (is (= :immutable
               (get-in result [:function-value-facts 0 :mutability]))
            extension)
        (is (= (get-in result
                       [:identity-input :function-value :function-value-id])
               (get-in result [:function-value-facts 0
                               :function-value-id]))
            "identity binds the function value, not a physical source path")
        (is (= (:canonical-core-artifact-id result)
               (get-in request [:sh07-context
                                :canonical-core-artifact :artifact-id])))
        (is (not= (:canonical-core-artifact-id result)
                  (get-in result [:function-value-facts 0
                                  :function-value-id])))))))

(deftest sh08-ho1-rejects-nonfunction-and-capture
  (doseq [[basename reason rule upstream-rule upstream-reason]
          [["function-value-nonfunction"
            :callable-argument-not-function "STD08-HO-TYPE"
            "C7-TYPE-MISMATCH" :callable-parameter-not-function]
           ["function-value-capture"
            :closure-free-capture-unsupported "STD08-HO-CAPTURE"
            nil nil]]
          extension [".gravity" ".qst"]]
    (let [request
          (request-for "rejected" basename extension
                       'sh08-ho1-identity 'sh08-ho1-apply-one)
          run (run-request request)]
      (is (= :rejected (:status (:bridge-request run)))
          [basename extension])
      (is (= rule
             (get-in (:bridge-request run) [:diagnostics 0 :rule]))
          [basename extension])
      (is (= reason
             (get-in (:bridge-request run) [:diagnostics 0 :reason]))
          [basename extension])
      (when upstream-rule
        (is (= upstream-rule
               (get-in (:bridge-request run)
                       [:diagnostics 0 :facts :upstream-diagnostics 0
                        :rule]))
            [basename extension])
        (is (= upstream-reason
               (get-in (:bridge-request run)
                       [:diagnostics 0 :facts :upstream-diagnostics 0
                        :reason]))
            [basename extension]))
      (is (= :passed (:status (:verification run)))
          [basename extension]))))

(deftest sh08-ho1-rejects-altered-envelope-and-result
  (let [request
        (request-for "accepted" "function-value-call" ".gravity"
                     'sh08-ho1-identity 'sh08-ho1-apply-one)
        run (run-request request)
        altered-envelope
        (hostile-assoc-in request [:envelope :semantic-envelope-id]
                          (sha256-id {:altered true}))
        altered-provenance
        (hostile-assoc-in request [:envelope :provenance-binding-id]
                          (sha256-id {:altered-provenance true}))
        altered-subject
        (hostile-assoc-in
         request
         [:envelope-descriptor :identity-subjects 0 :observed-id]
         (sha256-id {:altered-subject true}))
        altered-verification
        (hostile-assoc-in
         request [:sh07-verification :verified-artifact-id]
         (sha256-id {:altered-verification true}))
        altered-catalog
        (assoc request :sh07-check-catalog #{})
        altered-descriptor-source
        (hostile-assoc-in
         request
         [:envelope-descriptor :source-revision :source-content-hash]
         (sha256-id {:altered-bridge-source true}))
        altered-replay
        (hostile-assoc-in request [:envelope-replay :checks 0]
                          :substituted)
        altered-replay-template
        (hostile-assoc-in
         request [:envelope-replay :artifact-template :artifact-id]
         (sha256-id {:altered-template true}))
        altered-replay-root
        (hostile-assoc-in
         request [:envelope-replay :semantic-envelope-root]
         (sha256-id {:altered-root true}))
        altered-result (hostile-assoc (:result run) :dispatch :dynamic)
        altered-request
        (assoc request :function-value-name 'sh08-ho1-missing)
        altered-request-schema
        (assoc request :unexpected true)
        altered-descriptor-schema
        (hostile-assoc-in request [:envelope-descriptor :unexpected] true)
        altered-envelope-schema
        (hostile-assoc-in request [:envelope :unexpected] true)
        altered-verification-schema
        (hostile-assoc-in request [:envelope-verification :unexpected] true)
        altered-identity-input
        (hostile-assoc-in request [:sh07-context :identity-input]
                          {:substituted-identity true})
        envelope-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [altered-envelope])
        provenance-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [altered-provenance])
        subject-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [altered-subject])
        verification-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-verification])
        catalog-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [altered-catalog])
        replay-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request [altered-replay])
        descriptor-source-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-descriptor-source])
        replay-template-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-replay-template])
        replay-root-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-replay-root])
        result-rejection
        (invoke-bridge
         'sh08-verify-higher-order-function-value-result
         [request (:bridge-request run) altered-result])
        request-rejection
        (invoke-bridge
         'sh08-verify-higher-order-function-value-result
         [altered-request (:bridge-request run) (:result run)])
        request-schema-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-request-schema])
        descriptor-schema-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-descriptor-schema])
        envelope-schema-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-envelope-schema])
        verification-schema-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-verification-schema])
        identity-input-rejection
        (invoke-bridge
         'sh08-build-higher-order-function-value-request
         [altered-identity-input])]
    (is (map? (:bridge-request run)))
    (is (map? (:result run)))
    (is (= :rejected (:status envelope-rejection)))
    (is (= "STD08-HO-ENVELOPE"
           (get-in envelope-rejection [:diagnostics 0 :rule])))
    (is (= :rejected (:status result-rejection)))
    (is (= "STD08-HO-VERIFY"
           (get-in result-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in provenance-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in subject-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-SH07"
           (get-in verification-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-SH07"
           (get-in catalog-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in replay-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in descriptor-source-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in replay-template-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in replay-root-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-VERIFY"
           (get-in request-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-SCHEMA"
           (get-in request-schema-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in descriptor-schema-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in envelope-schema-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-ENVELOPE"
           (get-in verification-schema-rejection [:diagnostics 0 :rule])))
    (is (= "STD08-HO-SH07"
           (get-in identity-input-rejection [:diagnostics 0 :rule])))))

(deftest sh08-ho1-identity-is-path-neutral
  (let [a (request-for "accepted" "function-value-call" ".gravity"
                      'sh08-ho1-identity 'sh08-ho1-apply-one)
        b (assoc (request-for "accepted" "function-value-call" ".qst"
                              'sh08-ho1-identity 'sh08-ho1-apply-one)
                 :actual-source-path "/checkout-b/function-value-call.qst")
        ra (:result (run-request a))
        rb (:result (run-request b))]
    (is (= (:identity-input ra) (:identity-input rb)))
    (is (not= (:provenance ra) (:provenance rb)))
    (is (= (get-in a [:envelope-descriptor :source-revision])
           (get-in b [:envelope-descriptor :source-revision])))
    (is (not=
         (get-in a [:envelope-descriptor :actual-path-provenance])
         (get-in b [:envelope-descriptor :actual-path-provenance])))
    (is (= (get-in ra [:identity-input :dispatch])
           :direct-function-value))))
