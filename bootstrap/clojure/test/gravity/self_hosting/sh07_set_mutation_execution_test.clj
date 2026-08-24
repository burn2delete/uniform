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
     "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity")))
(def ^:private l2-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity")))
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
