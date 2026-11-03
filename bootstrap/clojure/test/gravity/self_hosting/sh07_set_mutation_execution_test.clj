(ns gravity.self-hosting.sh07-set-mutation-execution-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_set_mutation_execution_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 mutation execution test source is not on the classpath"
        {:id "SH07-SET-EXECUTION-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "SH-07 mutation execution repository root was not found"
          {:id "SH07-SET-EXECUTION-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b49-set-mutation-execution")
(def ^:private extensions [".gravity" ".qst"])

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c6-plan
  (delay
    (compile-plan
     "language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity")))
(def ^:private l2-plan
  (delay
    (compile-plan
     "language/compiler/src/gravity/compiler/l2_core_language_semantics.gravity")))
(def ^:private fixture-plans
  (delay
    (into
     {}
     (for [family ["accepted" "rejected"]
           extension extensions]
       [[family extension]
        (compile-plan
         (str fixture-root "/" family
              "/set-mutation-execution" extension))]))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-set-mutation-execution
    :compiler-artifact-plan? true}
   plan function arguments))

(defn- fixture-path [family extension]
  (path
   (str fixture-root "/" family
        "/set-mutation-execution" extension)))

(defn- fixture-request [family extension]
  (assoc
   (invoke
    (get @fixture-plans [family extension])
    'mutation-execution-request
    [])
   :source-path (fixture-path family extension)))

(defn- c6-verify [request]
  (invoke
   @c6-plan 'verify-c6-set-mutation-execution-request [request]))

(defn- expanded-set-request [extension]
  (let [base (fixture-request "accepted" extension)
        namespace (:namespace base)
        depth 33
        literal-id "node/deep-literal"
        set-id (fn [index] (str "node/deep-set-" index))
        child-id (fn [index]
                   (if (= index (dec depth))
                     literal-id
                     (set-id (inc index))))
        node-source
        (fn [syntax-id line]
          {:syntax-id syntax-id
           :semantic-span {:line-start line
                           :column-start 1
                           :line-end line
                           :column-end 2}
           :generated-origin []})
        literal
        {:artifact :gravity/sh07-core-node
         :node-id literal-id
         :core-form :literal
         :children []
         :attributes {:literal-kind :integer :value 7}
         :source (node-source "syntax/deep-literal" 1)}
        nodes
        (vec
         (cons
          literal
          (for [index (range depth)]
            (let [node-id (set-id index)
                  child (child-id index)]
              {:artifact :gravity/sh07-core-node
               :node-id node-id
               :core-form :set!
               :children [child]
               :attributes {:target-binding-id (str "binding/deep-" index)
                            :target-evaluation :none
                            :result-policy :unit
                            :required-effect :state/write}
               :source (node-source (str "syntax/deep-set-" index)
                                    (+ 2 index))
               :evaluation {:kind :value-then-write
                            :order [{:index 0 :core-node-id child}]}}))))
        mutations
        (vec
         (for [index (range depth)]
           (let [node-id (set-id index)
                 child (child-id index)]
             {:core-node-id node-id
              :value-core-node-id child
              :target-binding-id (str "binding/deep-" index)
              :target-namespace namespace
              :target-binding-class :namespace
              :target-location :top-level-var
              :target-evaluated-children []
              :evaluated-children [child]
              :ordered-steps [:evaluate-value :write-target :yield-unit]
              :result-policy :unit
              :required-effect :state/write
              :type-legality :pending-sh08
              :effect-registry-legality :pending-sh09
              :effect-profile-capability-legality :pending-sh09
              :ownership-legality :pending-sh10
              :safety-classification :pending-sh11})))
        store
        (vec
         (for [index (range depth)]
           {:binding-id (str "binding/deep-" index)
            :namespace namespace
            :location :top-level-var
            :value 0}))]
    (assoc base
           :root-node-id (set-id 0)
           :nodes nodes
           :mutations mutations
           :store store)))

(defn- long-identifier-request [extension]
  (let [request (fixture-request "accepted" extension)
        long-id (fn [prefix]
                  (str prefix (apply str (repeat 257 "x"))))
        ids (into {}
                  (map (fn [value] [value (long-id value)])
                       ["node/literal" "node/inner-set" "node/outer-set"
                        "syntax/literal" "syntax/inner-set" "syntax/outer-set"
                        "binding/inner" "binding/outer"]))
        remap (fn [value] (get ids value value))
        remap-node
        (fn [node]
          (let [node (update node :node-id remap)
                node (update node :children #(mapv remap %))
                node (update-in node [:source :syntax-id] remap)]
            (if (= :set! (:core-form node))
              (-> node
                  (update-in [:attributes :target-binding-id] remap)
                  (update-in [:evaluation :order]
                             (fn [order]
                               (mapv #(update % :core-node-id remap)
                                     order))))
              node)))
        remap-mutation
        (fn [mutation]
          (-> mutation
              (update :core-node-id remap)
              (update :value-core-node-id remap)
              (update :target-binding-id remap)
              (update :evaluated-children #(mapv remap %))))
        remap-store
        (fn [entry] (update entry :binding-id remap))]
    (-> request
        (update :root-node-id remap)
        (update :nodes #(mapv remap-node %))
        (update :mutations #(mapv remap-mutation %))
        (update :store #(mapv remap-store %)))))

(defn- l2-execute [verified]
  (invoke @l2-plan 'execute-l2-set-mutation [verified]))

(defn- store-values [result]
  (into {}
        (map (juxt :binding-id :value))
        (:store result)))

(defn- update-request-node [request node-id f]
  (update
   request :nodes
   (fn [nodes]
     (mapv
      (fn [node]
        (if (= node-id (:node-id node)) (f node) node))
      nodes))))

(defn- update-request-mutation [request node-id f]
  (update
   request :mutations
   (fn [mutations]
     (mapv
      (fn [mutation]
        (if (= node-id (:core-node-id mutation))
          (f mutation)
          mutation))
      mutations))))

(defn- malformed-probes [request]
  (let [literal (first (:nodes request))
        inner-set (second (:nodes request))
        inner-mutation (first (:mutations request))
        cycle
        (-> request
            (update-request-node
             "node/inner-set"
             #(-> %
                  (assoc :children ["node/outer-set"])
                  (assoc-in [:evaluation :order]
                            [{:index 0
                              :core-node-id "node/outer-set"}])))
            (update-request-mutation
             "node/inner-set"
             #(assoc %
                     :value-core-node-id "node/outer-set"
                     :evaluated-children ["node/outer-set"])))
        unreachable-literal
        (-> literal
            (assoc :node-id "node/unreachable-literal")
            (assoc-in [:source :syntax-id]
                      "syntax/unreachable-literal"))
        unreachable-set-node
        (-> inner-set
            (assoc :node-id "node/unreachable-set")
            (assoc-in [:source :syntax-id] "syntax/unreachable-set"))
        unreachable-set-mutation
        (assoc inner-mutation :core-node-id "node/unreachable-set")]
    [{:name :cycle
      :request cycle
      :reason :cyclic-core-child-graph
      :core-node-id "node/outer-set"
      :syntax-id "syntax/outer-set"}
     {:name :unreachable-literal
      :request (update request :nodes conj unreachable-literal)
      :reason :unreachable-core-node-carrier
      :core-node-id "node/unreachable-literal"
      :syntax-id "syntax/unreachable-literal"}
     {:name :unreachable-set-and-mutation
      :request (-> request
                   (update :nodes conj unreachable-set-node)
                   (update :mutations conj unreachable-set-mutation))
      :reason :unreachable-core-node-carrier
      :core-node-id "node/unreachable-set"
      :syntax-id "syntax/unreachable-set"}
     {:name :extra-mutation
      :request
      (update
       request :mutations conj
       (assoc inner-mutation :core-node-id "node/extra-mutation"))
      :reason :set-mutation-node-set-mismatch
      :core-node-id "node/outer-set"
      :syntax-id "syntax/outer-set"}
     {:name :missing-mutation
      :request (assoc request :mutations [(first (:mutations request))])
      :reason :set-mutation-node-set-mismatch
      :core-node-id "node/outer-set"
      :syntax-id "syntax/outer-set"}
     {:name :missing-literal-value
      :request
      (update-request-node
       request "node/literal"
       #(update % :attributes dissoc :value))
      :reason :literal-value-required
      :core-node-id "node/literal"
      :syntax-id "syntax/literal"}
     {:name :missing-literal-kind
      :request
      (update-request-node
       request "node/literal"
       #(update % :attributes dissoc :literal-kind))
      :reason :literal-kind-required
      :core-node-id "node/literal"
      :syntax-id "syntax/literal"}
     {:name :missing-node-source
      :request
      (update-request-node request "node/literal" #(dissoc % :source))
      :rule "C6-ORIGIN"
      :reason :core-node-source-required
      :core-node-id "node/literal"
      :syntax-id nil}
     {:name :invalid-source-span
      :request
      (update-request-node
       request "node/literal"
       #(assoc-in % [:source :semantic-span :line-start] 0))
      :rule "C6-ORIGIN"
      :reason :core-node-source-invalid
      :core-node-id "node/literal"
      :syntax-id "syntax/literal"}
     {:name :malformed-generated-origin
      :request
      (update-request-node
       request "node/literal"
       #(assoc-in % [:source :generated-origin] [{}]))
      :rule "C6-ORIGIN"
      :reason :core-node-source-invalid
      :core-node-id "node/literal"
      :syntax-id "syntax/literal"}
     {:name :literal-kind-value-mismatch
      :request
      (update-request-node
       request "node/literal"
       #(assoc-in % [:attributes :value] nil))
      :reason :literal-kind-value-mismatch
      :core-node-id "node/literal"
      :syntax-id "syntax/literal"}
     {:name :forged-pending-legality
      :request
      (update-request-mutation
       request "node/inner-set"
       #(assoc % :safety-classification :proven-safe))
      :reason :set-pending-legality-mismatch
      :core-node-id "node/inner-set"
      :syntax-id "syntax/inner-set"}]))

(deftest sh07-set-mutation-execution-fixtures-are-paired
  (doseq [family ["accepted" "rejected"]]
    (let [gravity (fixture-path family ".gravity")
          qst (fixture-path family ".qst")]
      (is (= (vec (source-bytes gravity))
             (vec (source-bytes qst)))))))

(deftest sh07-set-mutation-executes-value-once-before-explicit-writes
  (let [results
        (mapv
         (fn [extension]
           (let [verified (c6-verify (fixture-request "accepted" extension))]
             (is (= :accepted (:status verified)))
             (is (= [] (:diagnostics verified)))
             (l2-execute verified)))
         extensions)
        expected-steps
        [:evaluate-value :evaluate-value :evaluate-literal
         :write-target :yield-unit :write-target :yield-unit]]
    (is (= (first results) (second results)))
    (doseq [result results]
      (is (= :accepted (:status result)))
      (is (= :unit (:value result)))
      (is (= {:types :pending-sh08
              :effects-and-capabilities :pending-sh09
              :ownership :pending-sh10
              :safety :pending-sh11}
             (:downstream-legality result)))
      (is (= [] (:target-evaluated-children result)))
      (is (= :state/write (:required-effect result)))
      (is (= expected-steps (mapv :step (:trace result))))
      (is (= (vec (range 7)) (mapv :ordinal (:trace result))))
      (is (= 1
             (count
              (filter #(= :evaluate-literal (:step %))
                      (:trace result)))))
      (is (empty? (filter #(= :evaluate-target (:step %))
                          (:trace result))))
      (is (= {"binding/inner" 7
              "binding/outer" :unit}
             (store-values result)))
      (is (= [{:ordinal 3
               :step :write-target
               :core-node-id "node/inner-set"
               :binding-id "binding/inner"
               :value 7}
              {:ordinal 5
               :step :write-target
               :core-node-id "node/outer-set"
               :binding-id "binding/outer"
               :value :unit}]
             (filterv #(= :write-target (:step %))
                      (:trace result)))))))

(deftest sh07-set-mutation-accepts-request-beyond-retired-work-ceilings
  (let [request (expanded-set-request ".gravity")
        verified (c6-verify request)
        result (l2-execute verified)]
    (is (= 34 (count (:nodes request))))
    (is (= 33 (count (:mutations request))))
    (is (= 33 (count (:store request))))
    (is (= :accepted (:status verified)))
    (is (= :accepted (:status result)))
    (is (= :unit (:value result)))
    (is (= 100 (count (:trace result))))
    (is (= 33
           (count (filter #(= :write-target (:step %))
                          (:trace result)))))
    (is (= 7
           (get (store-values result) "binding/deep-32")))))

(deftest sh07-set-mutation-accepts-long-authenticated-identifiers
  (let [request (long-identifier-request ".gravity")
        verified (c6-verify request)
        result (l2-execute verified)]
    (is (every? #(> (count %) 256)
                (concat [(:root-node-id request)]
                        (map :node-id (:nodes request))
                        (map #(get-in % [:source :syntax-id]) (:nodes request))
                        (map :core-node-id (:mutations request))
                        (map :target-binding-id (:mutations request))
                        (map :binding-id (:store request)))))
    (is (= :accepted (:status verified)))
    (is (= :accepted (:status result)))
    (is (= :unit (:value result)))))

(deftest sh07-set-mutation-rejects-empty-and-nonstring-identifiers
  (let [request (fixture-request "accepted" ".gravity")]
    (doseq [[name malformed]
            [[:empty (assoc request :root-node-id "")]
             [:nonstring (assoc request :root-node-id 7)]]]
      (testing (str name)
        (let [result (c6-verify malformed)]
          (is (= :rejected (:status result)))
          (is (= :execution-request-shape
                 (get-in result [:diagnostics 0 :facts :reason]))))))))
(deftest sh07-set-mutation-present-nil-literal-remains-a-value
  (doseq [extension extensions]
    (let [request
          (update-request-node
           (fixture-request "accepted" extension)
           "node/literal"
           #(assoc % :attributes {:literal-kind :nil :value nil}))
          verified (c6-verify request)
          result (l2-execute verified)]
      (is (= :accepted (:status verified)))
      (is (= :accepted (:status result)))
      (is (= :unit (:value result)))
      (is (= nil
             (get (store-values result) "binding/inner")))
      (is (= 1
             (count
              (filter
               #(if (= :evaluate-literal (:step %))
                  (= nil (:value %))
                  false)
               (:trace result))))))))

(deftest sh07-set-mutation-substituted-value-link-is-stable-c6-rejection
  (doseq [extension extensions]
    (testing extension
      (let [source-path (fixture-path "rejected" extension)
            result (c6-verify (fixture-request "rejected" extension))
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (nil? (:verified-request result)))
        (is (= :gravity/c6-set-mutation-execution-diagnostic
               (:artifact diagnostic)))
        (is (= "SH07-C6-SET-MUTATION-EXECUTION"
               (:diagnostic-id diagnostic)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= "syntax/inner-set" (:syntax-id diagnostic)))
        (is (= "node/inner-set" (:core-node-id diagnostic)))
        (is (= source-path
               (get-in diagnostic [:source-span :source])))
        (is (= :meta (:profile diagnostic)))
        (is (= :jvm (:target diagnostic)))
        (is (= :sh07-core-mutation-execution-v1
               (:lowering-rule diagnostic)))
        (is (= :set-value-link-mismatch
               (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))
        (is (string? (:remediation diagnostic)))
        (is (seq (:remediation diagnostic)))))))

(deftest sh07-set-mutation-malformed-graphs-fail-closed-in-c6
  (doseq [extension extensions
          {:keys [name request rule reason core-node-id syntax-id]}
          (malformed-probes (fixture-request "accepted" extension))]
    (testing (str extension " " name)
      (let [result (c6-verify request)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (nil? (:verified-request result)))
        (is (= 1 (count (:diagnostics result))))
        (is (= :gravity/c6-set-mutation-execution-diagnostic
               (:artifact diagnostic)))
        (is (= "SH07-C6-SET-MUTATION-EXECUTION"
               (:diagnostic-id diagnostic)))
        (is (= (or rule "C6-VERIFY") (:rule diagnostic)))
        (is (= reason (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))
        (is (= core-node-id (:core-node-id diagnostic)))
        (is (= syntax-id (:syntax-id diagnostic)))
        (is (= (:source-path request)
               (get-in diagnostic [:source-span :source])))
        (is (= :sh07-core-mutation-execution-v1
               (:lowering-rule diagnostic)))))))

(deftest sh07-set-mutation-l2-rejects-unverified-input
  (let [verified
        (c6-verify (fixture-request "accepted" ".gravity"))
        result (l2-execute (assoc verified :status :rejected))
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= "L2-LOWERING-GAP" (:rule diagnostic)))
    (is (= :accepted-c6-verification-required
           (get-in diagnostic [:facts :reason])))
    (is (true? (get-in diagnostic [:facts :fail-closed])))))

(deftest sh07-set-mutation-l2-rejects-forged-accepted-envelopes
  (doseq [extension extensions]
    (let [verified
          (c6-verify (fixture-request "accepted" extension))
          cases
          [{:name :missing-carrier
            :verified (dissoc verified :verification-carrier)
            :reason :verified-envelope-shape-mismatch}
           {:name :request-only-mutation
            :verified
            (assoc-in
             verified [:verified-request :store 0 :value] 999)
            :reason :verified-c6-carrier-mismatch}
           {:name :carrier-only-mutation
            :verified
            (assoc-in
             verified
             [:verification-carrier :verified-request :store 0 :value]
             999)
            :reason :verified-c6-carrier-mismatch}
           {:name :extra-envelope-field
            :verified (assoc verified :forged true)
            :reason :verified-envelope-shape-mismatch}
           {:name :schema-spoof
            :verified (assoc verified :schema-version 2)
            :reason :verified-schema-version}
           {:name :artifact-spoof
            :verified (assoc verified :artifact :gravity/forged-c6-result)
            :reason :verified-c6-artifact-required}]]
      (is (= :accepted (:status verified)))
      (doseq [{:keys [name verified reason]} cases]
        (testing (str extension " " name)
          (let [result (l2-execute verified)
                diagnostic (first (:diagnostics result))]
            (is (= :rejected (:status result)))
            (is (nil? (:value result)))
            (is (nil? (:store result)))
            (is (= [] (:trace result)))
            (is (= 1 (count (:diagnostics result))))
            (is (= :gravity/l2-set-mutation-execution-diagnostic
                   (:artifact diagnostic)))
            (is (= "SH07-L2-SET-MUTATION-EXECUTION"
                   (:diagnostic-id diagnostic)))
            (is (= "L2-LOWERING-GAP" (:rule diagnostic)))
            (is (= reason (get-in diagnostic [:facts :reason])))
            (is (true? (get-in diagnostic [:facts :fail-closed])))
            (is (= (:source-path (:verified-request verified))
                   (get-in diagnostic [:source-span :source])))))))))
