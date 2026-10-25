(ns gravity.self-hosting.sh12-zero-arity-bool-diamond-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn- invoke [name & arguments]
  ((existing 'invoke) (existing 'c11-plan) name (vec arguments)))

(def ^:private current-emitter
  (delay
    ;; Existing lexical execution seam, with actual current identities. This
    ;; record grants no pinned-source admission or migration authority.
    (let [relative "bootstrap/gravity/p15_s23/emitter.gravity"
          path (str (.resolve @(existing 'root) relative))
          plan ((existing 'compile-plan) relative)
          source-hash (get-in plan [:source :sha256])
          digest #(str "sha256:" (bootstrap/sha256-hex
                                  (pr-str (bootstrap/c-backend-canonical-value %))))
          semantic (digest (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan))]
      {:artifact :gravity/p15-s23-stage2-expression-lowering-binding
       :status :complete :source-path path :source-content-hash source-hash
       :semantic-hash semantic
       :artifact-hash (digest {:source-content-hash source-hash :semantic-hash semantic})
       :plan plan :invoked? true :generic-bridge-residual? true
       :clojure-seed-boundary? true :self-hosted? false})))

(defn- original-pair [condition extension]
  ;; Current original source products and original C6 replay, not normalized
  ;; Core DATA or pinned public module admission. D1, C6 and C11 own this edge.
  (binding [bootstrap/*p15-s23-stage2-compiler-artifact-binding* @current-emitter]
   (let [path (str (.resolve @(existing 'root)
                           (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
                                "zero_arity_bool_" condition "_source." extension)))
        text (slurp path)
        reader (str (.resolve @(existing 'root) "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        products ((existing 'parameter-source-products) path text nil reader)
        call (existing 'invoke) plan (existing 'parameter-c6-plan)
        raw (call plan 'c6-c10-build-checked-core-template [(:envelope products)])
        fresh ((existing 'parameter-source-products) path text (:compiled-binding-data products) reader)
        replay (call plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
        upstream (call plan 'c6-c10-verify-checked-core-template
                       [(:envelope fresh) (:artifact-template raw) (:digest-requests replay)])
        sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
        core (bootstrap/p15-s23-c6c10-public-artifact
              context (:compiled-binding-data products) (:ingress products) sealed)]
    {:raw raw :upstream upstream :input (:envelope fresh) :requests (:digest-requests replay)
     :core core :mir (invoke 'sh12-build-branch-join-mir core)})))

(defn- check-original [condition extension negatives?]
  (let [{:keys [raw upstream input requests core mir]} (original-pair condition extension)
        nodes (:core-nodes core) ids (mapv :node-id nodes)
        verify #(invoke 'sh12-verify-branch-join-mir core %)
        execute #(invoke 'sh13-run-branch-join-mir-with-arguments core % [])
        expected (= condition "true")]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream)))
    (is (= [:hosted :jvm] ((juxt :profile :source-target) core)))
    (is (= [:literal :truthy :literal :literal :if :function]
           (mapv :source-operation (:core-nodes core))))
    (is (= [] (get-in core [:core-nodes 5 :type :params])))
    (is (= :gravity/bool (get-in core [:core-nodes 5 :type :return])))
    (is (= :gravity/mir-module (:artifact mir))
        (pr-str {:diagnostics (:diagnostics mir)
                 :gates (let [view (invoke 'sh12-c6p-role-view core)]
                          {:top (invoke 'sh12-c6p-top-shape? core)
                           :lineage (invoke 'sh12-c6p-lineage-valid? core)
                           :metadata (invoke 'sh12-c6p-producer-metadata-valid? core)
                           :shape (invoke 'sh12-c6p-shape-valid? core view)
                           :roles (invoke 'sh12-c6p-role-nodes-valid? core 0)
                           :nodes (invoke 'sh12-c6p-nodes-problem core nodes 0)
                           :graphs (invoke 'sh12-c6p-graphs-valid? core view)})}))
    (when (= :gravity/mir-module (:artifact mir))
      (let [report (verify mir) result (execute mir)
            context (invoke 'sh13-branch-join-context core mir)
            function (:function context) entry (:entry function)
            blocks (:blocks function) then-id (get-in context [:then :block-id])
            else-id (get-in context [:else :block-id]) join-id (get-in context [:join :block-id])
            snapshot [core mir]]
        (is (= :passed (:status report)) (pr-str (:diagnostics report)))
        (is (= :single-conditional-branch-join (:scope report)))
        (is (= :pending (:verification-status mir)))
        (is (= :constructed-unverified (get-in mir [:pass-execution-record :status])))
        (is (= (:artifact-id core) (:source-core mir)))
        (is (= [] (:params function) (get-in result [:projection :parameters])))
        (is (= :gravity/bool (:returns function)))
        (is (= 4 (count blocks) (count (get-in mir [:control-flow-graph :edges]))))
        (is (= [2 1 1 2] (mapv #(count (get-in context [% :instructions])) [:entry :then :else :join])))
        (is (= ids (mapv :op-id (:operations context))))
        (is (= (get-in nodes [5 :type]) (get-in context [:join :instructions 1 :type])))
        (doseq [node nodes]
          (let [id (:node-id node)]
            (is (= (get-in core [:type-facts id])
                   (get-in mir [:type-table (get-in core [:type-facts id :fact-id])])))))
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (is (= expected (get-in result [:execution :value])))
        (is (= (mapv ids [0 1 (if expected 2 3)])
               (vec (keep :operation (get-in result [:execution :trace])))))
        (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments core mir [] result))))
        (when negatives?
          ;; Original C6 source replay owns source-to-Core substitution. C11
          ;; then compares output against that unchanged original Core.
          (doseq [index [0 2 3]]
            (let [candidate (update-in (:artifact-template raw) [:core-nodes index :attributes :value] not)]
              (is (= :rejected
                     (:status ((existing 'invoke) (existing 'parameter-c6-plan)
                               'c6-c10-verify-checked-core-template [input candidate requests]))))))
          (doseq [[bad-core diagnostic reason]
                  [[(assoc-in core [:core-nodes 0 :source :span :byte-start] -1)
                    "C11-ORIGIN" :c6-parameter-source-origin]
                   [(assoc-in core [:type-facts (ids 0) :type] :gravity/integer)
                    "C11-TYPE" :c6-parameter-type-fact]
                   [(assoc-in core [:effect-facts (ids 0) :direct] #{:error/raise})
                    "C11-EFFECT" :c6-parameter-effect-fact]
                   [(assoc-in core [:safety-facts (ids 0) :outcome] :runtime-checked)
                    "C11-SAFETY" :c6-parameter-safety-proof]
                   [(assoc-in core [:core-nodes 5 :type :return] :gravity/integer)
                    "C11-MODULE" :original-c6-bool-parameter-family]
                   [(assoc core :resolved-parameter {}) "C11-MODULE" :bounded-original-c6-carrier]
                   [(assoc-in core [:target-request-metadata :requested-target] :wasm)
                    "C11-MODULE" :original-c6-bool-parameter-family]
                   [(update-in core [:dependency-order-graph :edges] #(vec (reverse %)))
                    "C11-VERIFY" :original-c6-parameter-graphs]]]
            (let [rejected (invoke 'sh12-verify-branch-join-mir bad-core mir)]
              (is (= :rejected (:status rejected)))
              (is (= [diagnostic reason] ((juxt :diagnostic-id :missing-fact) (first (:diagnostics rejected)))))))
          (doseq [candidate [(update-in mir [:functions 'main :blocks entry :instructions 0 :constant-payload :value] not)
                             (update-in mir [:functions 'main :blocks then-id :instructions 0 :constant-payload :value] not)
                             (update-in mir [:functions 'main :blocks else-id :instructions 0 :constant-payload :value] not)
                             (assoc-in mir [:functions 'main :blocks entry :terminator :successors] [else-id then-id])
                             (assoc-in mir [:functions 'main :blocks join-id :terminator :operands] [(ids 2)])
                             (assoc-in mir [:functions 'main :params] [(ids 0)])]]
            (let [rejected (verify candidate) execution (execute candidate)]
              (is (= :rejected (:status rejected) (:status execution)))
              (is (= (:diagnostics rejected) (:diagnostics execution)))
              (is (nil? (:execution execution)))))
          (doseq [arguments [nil {} [true] [false] [nil] [true false]]]
            (let [rejected (invoke 'sh13-run-branch-join-mir-with-arguments core mir arguments)]
              (is (= :rejected (:status rejected)))
              (is (= ["C11-TYPE" :zero-entry-arguments]
                     ((juxt :diagnostic-id :missing-fact) (first (:diagnostics rejected)))))
              (is (nil? (:execution rejected)))))
          (doseq [candidate [(update-in result [:execution :value] not)
                             (assoc-in result [:execution :trace] [])
                             (assoc-in result [:projection :parameters] [{:parameter-index 0}])]]
            (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments core mir [] candidate)))))
          (is (= snapshot [core mir])))))))

(deftest sh12-original-zero-arity-bool-diamond (check-original "true" "gravity" true))
(deftest sh12-original-zero-arity-false-bool-diamond (check-original "false" "gravity" false))
(deftest sh12-original-zero-arity-bool-diamond-qst (check-original "true" "qst" false))
(deftest sh12-original-zero-arity-false-bool-diamond-qst (check-original "false" "qst" false))

(deftest sh12-original-parameter-carrier-keeps-early-map-diagnostic
  ;; One original current C6 producer call, not a normalized/retagged Core.
  ;; This structural regression does not need a second source/replay build.
  (let [path (str (.resolve @(existing 'root)
                           "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        reader (str (.resolve @(existing 'root) "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        text (slurp path) products ((existing 'parameter-source-products) path text nil reader)
        raw ((existing 'invoke) (existing 'parameter-c6-plan)
             'c6-c10-build-checked-core-template [(:envelope products)])
        sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
        core (bootstrap/p15-s23-c6c10-public-artifact
              context (:compiled-binding-data products) (:ingress products) sealed)
        mir (invoke 'sh12-build-branch-join-mir core)
        result (invoke 'sh13-run-branch-join-mir-with-arguments core mir [true])]
    (is (= :accepted (:status raw)))
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
    (is (= :accepted (:status result)))
    (is (= true (get-in result [:execution :value])))
    (doseq [value [nil false []]]
      (let [invalid (assoc core :resolved-parameter value)]
        (is (false? (invoke 'sh12-c6p-top-shape? invalid)))
        (doseq [report [(invoke 'sh12-build-branch-join-mir invalid)
                        (invoke 'sh12-verify-branch-join-mir invalid mir)
                        (invoke 'sh13-run-branch-join-mir-with-arguments invalid mir [true])]]
          (is (= :rejected (:status report)))
          (is (= ["C11-MODULE" :bounded-original-c6-carrier]
                 ((juxt :diagnostic-id :missing-fact) (first (:diagnostics report)))))
          (is (nil? (:execution report))))))))
