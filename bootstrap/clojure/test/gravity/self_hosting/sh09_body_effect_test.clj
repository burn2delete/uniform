(ns gravity.self-hosting.sh09-body-effect-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh09_body_effect_test.clj")))]
    (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file)))))
(defn- path [relative] (.getPath (io/file (repository-root) relative)))
(def ^:private leaf-path "bootstrap/gravity/src/gravity/effects.gravity")
(defn module-root-entry [bundle]
  (let [entries (filterv #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))]
    (when-not (= 1 (count entries)) (throw (ex-info "Expected one actual bundle root" {})))
    (first entries)))
(defn module-root-plan [bundle] (:plan (module-root-entry bundle)))
(defn compile-c8-module-bundle [source-path]
  (let [effects-path (path leaf-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        dependency (bootstrap/compiler-c5-resolution-file-artifact effects-path)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [dependency]}
        caller (bootstrap/compiler-c5-resolution-file-artifact source-path inputs)
        dependency-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter effects-path (slurp effects-path))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path (slurp source-path)
      {:caller-resolution-artifact caller :dependency-plans [dependency-plan]})))
(def ^:private leaf-plan
  (delay (let [source (path leaf-path)
               emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
           (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))
(defn- leaf [function & args]
  (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :body-effect-development-kernel} @leaf-plan function (vec args)))
(defn- sha [n] (str "sha256:" (format "%064x" n)))
(defn- sh08-helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Missing existing C7 helper" {:name name}))))
(defn- seal-binding [request typed verification]
  (let [preimage {:domain :gravity/sh09-sh08-function-binding-v1
                  :request request :typed typed :verification verification}]
    {:artifact :gravity/sh09-authenticated-sh08-function-binding :schema-version 1
     :boundary :clojure-coordinator-verifier :status :passed
     :digest-resolution {:algorithm :sha256 :encoding :gravity/canonical-edn-v1 :preimage preimage
                         :digest (bootstrap/p15-s23-c11-mir-digest preimage) :status :passed}}))
(defn- fresh-products [relative]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [c5 (bootstrap/compiler-c5-resolution-file-artifact (path relative))
        artifact (bootstrap/sh07-core-from-resolution-artifact c5)
        report (bootstrap/sh07-core-artifact-verification artifact)
        coordinator ((sh08-helper 'coordinator-verification) artifact report)
        context ((sh08-helper 'b47-context) artifact report coordinator)
        request {:canonical-core-artifact (:canonical-core-artifact context)
                 :b47-context context :coordinator-verification coordinator}
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :artifact artifact :report report :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))

(defn- fresh-request [c5]
  (let [artifact (bootstrap/sh07-core-from-resolution-artifact c5)
        report (bootstrap/sh07-core-artifact-verification artifact)
        coordinator ((sh08-helper 'coordinator-verification) artifact report)
        context ((sh08-helper 'b47-context) artifact report coordinator)]
    {:canonical-core-artifact (:canonical-core-artifact context)
     :b47-context context :coordinator-verification coordinator}))

(defn- fresh-import-products [extension]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [prefix "bootstrap/clojure/fixtures/self-hosting/sh-08/modules/"
        producer-c5 (bootstrap/compiler-c5-resolution-file-artifact (path (str prefix "inferred-values" extension)))
        producer-request (fresh-request producer-c5)
        c5 (bootstrap/compiler-c5-resolution-file-artifact (path (str prefix "inferred-consumer" extension))
             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [producer-c5]})
        request (assoc (fresh-request c5) :module-type-inputs
                  {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [producer-request]})
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :producer-c5 producer-c5 :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))

(defn- fresh-recursive-import-products [extension]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [producers (mapv #(bootstrap/compiler-c5-resolution-file-artifact
                         (path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/" % extension)))
                       ["function-self-recursive-type" "function-self-recursive-string-type"])
        requests (mapv fresh-request producers)
        c5 (bootstrap/compiler-c5-resolution-file-artifact
             (path (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/recursive-imported-body" extension))
             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts producers})
        request (assoc (fresh-request c5) :module-type-inputs
                  {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests requests})
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))
(defn- build-leaf [products]
  (leaf 'build-body-effect-result (:typed products) (:verification products) (:binding products)))
(defn- coherent-typed-mutation [products mutate]
  (let [typed (mutate (:typed products))
        typed (assoc typed :typed-core (leaf 'body-caller-typed-core typed))
        identity (leaf 'body-caller-identity typed)
        typed (assoc typed :identity-input identity :artifact-id-request identity)
        verification (assoc (:verification products) :expected typed :candidate typed)]
    (assoc products :typed typed :verification verification
           :binding (seal-binding (:request products) typed verification))))
(defn- reseal-effect-graph [candidate graph]
  (-> candidate (assoc :effect-graph graph) (assoc-in [:identity-input :effect-graph] graph)))
(defn- node [n kind children]
  {:node-id (sha n) :core-form kind :children (mapv sha children) :attributes {}
   :source {:syntax-id (sha (+ n 100)) :form-id (sha (+ n 200))
            :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil}
   :evaluation {:kind (case kind :fn :function-creation :quote :no-evaluation :left-to-right)
                :order (if (#{:fn :quote} kind) []
                         (mapv (fn [index n] {:index index :core-node-id (sha n)}) (range) children))}})

(deftest body-effect-operation-development-kernel
  ;; Actual Gravity algorithm, normalized graph DATA; no authentication claim.
  (let [literal (node 1 :literal []) fn-node (node 2 :fn [1])
        quote-node (assoc (node 3 :quote []) :attributes {:quoted-value '(throw unresolved/data)})
        reference (node 4 :reference []) call (node 5 :call [4])
        core {:nodes [call quote-node fn-node reference literal]
              :call-edges [{:call-core-node-id (sha 5) :callee-function-syntax-id (sha 20)}]}
        typed {:function-type-table [{:function-id (sha 20) :body-core-node-id (sha 1)}] :type-table {}}
        result (leaf 'body-derive-nodes core typed)]
    (is (= :accepted (:status result)))
    (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes result)))))
    (is (= (sha 1) (get-in result [:nodes (sha 5) :call-body-dependency])))
    (is (= [] (get-in result [:nodes (sha 2) :evaluated-children])))
    (is (= [(sha 1)] (get-in result [:nodes (sha 2) :latent-children])))
    (is (= [] (get-in result [:nodes (sha 3) :dependencies])))
    (let [effectful {(sha 1) {:transitive #{:compiler/read-ir}}}
          construction (leaf 'body-node-record fn-node nil effectful :function)]
      (is (= #{} (:transitive construction)))
      (is (= #{:compiler/read-ir} (:latent construction)))
      (is (= #{:compiler/read-ir}
             (:transitive (leaf 'body-node-record call (sha 1) (assoc effectful (sha 4) {:transitive #{}}) :value)))))
    (doseq [operation [:throw :try :recur :set! :unknown]]
      (is (= :unsupported-body-operation
             (:reason (leaf 'body-derive-nodes {:nodes [(node 1 operation [])] :call-edges []} typed)))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes (update core :nodes #(filterv (fn [n] (not= (sha 1) (:node-id n))) %)) typed))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes core (assoc-in typed [:function-type-table 0 :body-core-node-id] (sha 5))))))))

(deftest body-effect-coverage-development-kernel
  (let [module {:namespace 'example.body :profile :meta :target :jvm :effects [] :capabilities []}
        nodes [(node 1 :literal []) (node 2 :fn [1]) (node 3 :def [2])]
        record {:function-syntax-id (sha 12) :function-core-node-id (sha 2)}
        core {:artifact-id (sha 30) :module module :nodes nodes :root-core-node-ids [(sha 3)]
              :definitions [] :function-records [record] :call-edges [] :calls [] :recursion-components []
              :lexical-bindings [] :match-branch-records []
              :evaluation-order [{:core-node-id (sha 3) :index 0}]}
        seal (fn [c] (assoc c :identity-preimage (select-keys c [:nodes :root-core-node-ids :definitions :function-records
                                                              :call-edges :calls :lexical-bindings :match-branch-records :evaluation-order])))
        core (seal core)
        typed {:module module :sh07-shaped-artifact-id (sha 30)
               :function-type-table [{:function-id (sha 12) :parameters [] :parameter-binding-ids []
                                      :latent-effects [] :capabilities []}]
               :type-table {(sha 1) :int (sha 2) :fn (sha 3) :fn}
               :call-type-facts [] :local-binding-facts []
               :function-records (:function-records core) :call-edges [] :recursion-components [] :lexical-bindings []}]
    (is (true? (leaf 'body-shape-valid? core typed)))
    (is (false? (leaf 'body-shape-valid? core (update typed :type-table #(-> % (dissoc (sha 1)) (assoc (sha 99) :int))))))
    (is (false? (leaf 'body-shape-valid? (seal (update core :nodes conj (node 9 :literal [])))
                     (assoc-in typed [:type-table (sha 9)] :int))))
    (is (false? (leaf 'body-shape-valid? (seal (assoc core :root-core-node-ids [])) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:nodes 1 :children] 7) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:nodes 1 :evaluation :order] 7) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc core :evaluation-order []) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:evaluation-order 0 :core-node-id] (sha 999)) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc core :evaluation-order 7) typed)))
    (is (false? (leaf 'body-function-joins-valid? core (assoc-in typed [:type-table (sha 2)] 7) {})))
    (is (false? (leaf 'body-call-valid? {} {} {:argument-types 7} {} {} {} {} {})))
    (is (false? (leaf 'body-shape-valid? (seal (assoc core :function-records []))
                     (-> typed (assoc :function-type-table []) (assoc :function-records [])))))))

(deftest body-effect-guard-development-kernel
  (let [match-node (assoc (node 1 :match [2 3 4 5])
                         :attributes {:branch-count 2 :branch-child-indexes [1 2] :guard-child-indexes [3]})
        branches [{:core-node-id (sha 1) :clause-ordinal 0 :branch-child-index 1 :branch-core-node-id (sha 3)
                   :scrutinee-core-node-id (sha 2) :guard {:node-id (sha 5) :child-index 3}}
                  {:core-node-id (sha 1) :clause-ordinal 1 :branch-child-index 2 :branch-core-node-id (sha 4)
                   :scrutinee-core-node-id (sha 2)}]
        core {:match-branch-records branches
              :match-decision-skeletons [{:core-node-id (sha 1) :scrutinee-core-node-id (sha 2)
                                          :branch-core-node-ids [(sha 3) (sha 4)]}]}
        types {(sha 5) :gravity.type/bool}
        match-node (assoc match-node :evaluation {:kind :scrutinee-then-source-ordered-pattern-candidates
                                                  :order [{:index 0 :core-node-id (sha 2)}]})]
    (is (true? (leaf 'body-match-valid? match-node core types)))
    (is (false? (leaf 'body-match-valid? (update match-node :children pop) core types)))
    (is (false? (leaf 'body-match-valid? match-node core (assoc types (sha 5) :gravity.type/integer))))
    (let [graph (zipmap (map sha [2 3 4 5]) (repeat {:transitive #{}}))
          record (leaf 'body-node-record match-node nil graph :value)
          conditional (assoc (node 6 :if [2 3 4]) :evaluation {:kind :condition-then-exclusive-branch
                                                            :order [{:index 0 :core-node-id (sha 2)}]})
          if-record (leaf 'body-node-record conditional nil graph :value)]
      (is (= [(sha 2)] (:evaluated-children record) (:evaluated-children if-record)))
      (is (= [:branch :branch :guard] (mapv :role (:conditional-coverage record))))
      (is (= [:then :else] (mapv :role (:conditional-coverage if-record))))
      (is (= (mapv sha [3 4 5]) (mapv :node-id (:conditional-coverage record)))))
    (is (false? (leaf 'body-match-valid? match-node (update-in core [:match-branch-records 0] dissoc :guard) types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] []) core types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] [2]) core types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] 7) core types)))))

(deftest body-effect-final-input-fences-development-kernel
  (let [callable {:artifact :gravity/c7-function-type :parameters [] :return :gravity.type/integer}
        collection {:kind :gravity.type/vector :element-types [:gravity.type/integer :gravity.type/bool] :shape :literal}
        function {:parameters [{:binding-id (sha 1) :type :gravity.type/bool}] :parameter-binding-ids [(sha 1)]
                  :return collection :latent-effects [:compiler/read-ir] :capabilities [:compiler/ir-read]}]
    (doseq [type [:gravity.type/bool :gravity.type/integer :gravity.type/string :gravity.type/symbol collection
                 {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/symbol] :shape :literal}
                 {:kind :gravity.type/map :entries [{:key :payload :value-type collection}]}]]
      (is (true? (leaf 'body-first-order-value-type? type))))
    (doseq [type [callable {:kind :gravity.type/vector :element-types [callable]}
                 {:kind :gravity.type/map :entries [{:key :payload :value-type callable}]}
                 {:kind :union :members [collection callable]}]]
      (is (false? (leaf 'body-first-order-value-type? type))))
    (is (true? (leaf 'body-function-shapes? [function])))
    (is (false? (leaf 'body-function-shapes? [(assoc function :return callable)])))
    (is (false? (leaf 'body-function-shapes? [(assoc-in function [:parameters 0 :type] {:kind :list :members [callable]})])))
    (let [typed {:module {:namespace 'example.body :profile :meta :target :jvm}}
          known (assoc-in (node 1 :throw []) [:evaluation :owner-function-syntax-id] (sha 77))]
      (is (= (sha 77) (get-in (leaf 'body-error typed :unsupported-body-operation known) [:diagnostics 0 :function])))
      (is (nil? (get-in (leaf 'body-error typed :unsupported-body-operation (node 1 :throw [])) [:diagnostics 0 :function])))
      (is (= 'example.body (get-in (leaf 'body-error typed :unsupported-body-operation known) [:diagnostics 0 :namespace]))))))

(deftest body-effect-call-metadata-development-kernel
  (let [reference (assoc (node 1 :reference []) :attributes {:binding-id (sha 50)})
        argument (node 2 :literal []) call-node (node 3 :call [1 2]) source (:source call-node)
        target {:function-id (sha 20) :parameters [{:binding-id (sha 51) :type :gravity.type/bool}]
                :return :gravity.type/bool :latent-effects [] :capabilities [] :profile :meta :target :jvm}
        record {:definition-binding-id (sha 50) :function-core-node-id (sha 10)}
        edge {:classification :local-function :call-core-node-id (sha 3)
              :callee-binding-id (sha 50) :callee-definition-syntax-id (sha 60)
              :callee-function-syntax-id (sha 20) :callee-function-core-node-id (sha 10)
              :caller-function-syntax-id nil :caller-function-core-node-id nil
              :argument-core-node-ids [(sha 2)] :ordered-evaluation-node-ids [(sha 1) (sha 2)]
              :evaluation-order :operator-then-arguments}
        call {:core-node-id (sha 3) :operator-binding-id (sha 50) :argument-node-ids [(sha 2)]
              :ordered-evaluation-node-ids [(sha 1) (sha 2)] :evaluation-order :operator-then-arguments}
        fact {:artifact :gravity/c7-call-type-fact :status :inferred :call-core-node-id (sha 3)
              :callee-function-syntax-id (sha 20) :caller-function-syntax-id nil
              :argument-types [:gravity.type/bool] :result-type :gravity.type/bool
              :evaluation-order :operator-then-arguments :syntax-id (:syntax-id source)
              :source-span (:semantic-span source) :origin-chain [] :generated-origin nil
              :effects [] :capabilities [] :profile :meta :target :jvm
              :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id (sha 3)
                                :callee-function-syntax-id (sha 20) :argument-types [:gravity.type/bool] :result-type :gravity.type/bool}}
        run (fn [f] (leaf 'body-call-valid? edge call f
                          {(sha 1) reference (sha 2) argument (sha 3) call-node}
                          {(sha 20) target} {(sha 20) record} {(sha 50) {:definition-syntax-id (sha 60)}}
                          {(sha 2) :gravity.type/bool (sha 3) :gravity.type/bool}))]
    (is (true? (run fact)))
    (is (false? (run (assoc fact :profile :hosted))))
    (is (false? (run (assoc fact :target :javascript))))
    (is (false? (run (assoc fact :argument-types 7))))
    (let [coordinate {:binding-id (sha 50) :producer-sh06-artifact-id (sha 60)
                      :producer-sh07-artifact-id (sha 61) :definition-binding-id (sha 70) :function-id (sha 20)}
          imported {:coordinate coordinate :binding {:definition-syntax-id (sha 60)}
                    :function (assoc target :body-core-node-id (sha 80)) :body-core-node-id (sha 80)
                    :summary {:function-id (sha 20) :body-core-node-id (sha 80)
                              :inferred #{} :throws [:pending-sh09]}}
          imported-edge (assoc edge :classification :nonlocal-or-nonfunction
                                    :callee-function-syntax-id nil :callee-function-core-node-id nil)
          imported-fact (-> fact (assoc :callee-function-syntax-id nil :dispatch :direct-imported-function
                                        :imported-function coordinate :effect-status :pending-sh09-imported-body
                                        :legality-status :pending-imported-effects :effect-facts-source :callee-module-declarations)
                            (update :fact-id-request assoc :callee-function-syntax-id nil :imported-function coordinate
                                    :effect-status :pending-sh09-imported-body :legality-status :pending-imported-effects))
          run-import (fn [f foreign]
                       (leaf 'body-import-call-valid? imported-edge call f
                             {(sha 1) reference (sha 2) argument (sha 3) call-node} {} foreign
                             {(sha 2) :gravity.type/bool (sha 3) :gravity.type/bool}))]
      (is (true? (run-import imported-fact imported)))
      (doseq [f [(assoc imported-fact :callee-function-syntax-id (sha 20))
                 (assoc-in imported-fact [:imported-function :producer-sh06-artifact-id] (sha 99))
                 (assoc-in imported-fact [:fact-id-request :callee-function-syntax-id] (sha 20))
                 (assoc imported-fact :argument-types 7)
                 (assoc imported-fact :profile :hosted) (assoc imported-fact :target :native)]]
        (is (false? (run-import f imported))))
      (doseq [foreign [(assoc-in imported [:summary :body-core-node-id] (sha 99))
                       (assoc-in imported [:summary :function-id] (sha 99))
                       (assoc-in imported [:summary :inferred] [])
                       (assoc-in imported [:summary :throws] [])]]
        (is (false? (run-import imported-fact foreign)))))))

(deftest body-effect-import-origin-development-kernel
  ;; Normalized retained C5/C6 data and flat C7 facts; no accepted producer wrapper.
  (let [module {:namespace 'producer.values :package nil :profile :meta :target :jvm :exports ['ready]}
        original {:binding-id (sha 1) :name 'ready :kind :function :namespace 'producer.values :package nil
                  :binding-class :namespace :visibility :public :definition-artifact-id (sha 2) :definition-syntax-id (sha 3)}
        origin {:ordinary-module-artifact-id (sha 4) :origin-binding-id (sha 1) :origin-analysis-id (sha 5)
                :origin-definition-artifact-id (sha 2) :origin-definition-syntax-id (sha 3)
                :origin-definition (into {} (map (fn [k] [k (get original k)])
                     [:name :kind :namespace :package :visibility :profile-set :target-set :type-ref :effects :capabilities :safety]))}
        binding {:binding-id (sha 6) :upstream-binding-id (sha 7) :name 'ready :namespace 'producer.values
                 :package nil :visibility :public :binding-class :import :semantic-span origin}
        caller-c5 {:binding-table {:bindings [(assoc binding :binding-id (sha 7))]}}
        producer-c5 {:artifact-id (sha 4) :binding-table {:bindings [original]}
                     :gravity-resolution-boundary {:resolved-analysis {:artifact-id (sha 5) :module-contract module}}}
        function {:artifact :gravity/c7-function-type :function-id (sha 10) :body-core-node-id (sha 11)
                  :function-core-node-id (sha 12) :definition-name 'ready :parameter-binding-ids [] :parameters []
                  :return :gravity.type/bool :fixed-arity 0 :signature nil :latent-effects [] :capabilities []
                  :ownership-constraints [] :profile-constraints [] :thrown-error-effects [:pending-sh09] :status :inferred}
        entry {:producer-sh06-artifact-id (sha 4) :producer-sh07-artifact-id (sha 8) :module-key [(sha 4) (sha 8)]
               :request {:canonical-core-artifact {:function-records [{:definition-binding-id (sha 9) :function-syntax-id (sha 10)}]}
                         :b47-context {:authenticated-wrapper {:sh06-resolution-artifact producer-c5}
                                       :authenticated-core-request {:binding-table [{:binding-id (sha 9) :upstream-binding-id (sha 1)}]}}}
               :facts {:function-type-table [function]}}
        request {:canonical-core-artifact {:module module}
                 :b47-context {:authenticated-wrapper {:sh06-resolution-artifact caller-c5}
                               :authenticated-core-request {:binding-table [binding]}}}
        proof {:status :accepted :binding-id (sha 6) :origin origin :producer-sh06-artifact-id (sha 4)
               :producer-sh07-artifact-id (sha 8) :definition-binding-id (sha 9) :function-type function
               :effect-status :pending-sh09-imported-body :legality-status :pending-imported-effects}
        joined (leaf 'body-import-origin request binding entry proof)]
    (is (= (sha 11) (:body-core-node-id joined)))
    (is (= (leaf 'body-import-coordinate proof) (:coordinate joined)))
    (doseq [p [(assoc proof :origin {}) (assoc proof :producer-sh06-artifact-id (sha 99))
               (assoc proof :producer-sh07-artifact-id (sha 99)) (assoc proof :definition-binding-id (sha 99))
               (assoc-in proof [:function-type :body-core-node-id] (sha 99))
               (assoc-in proof [:function-type :function-id] (sha 99))]]
      (is (nil? (leaf 'body-import-origin request binding entry p))))
    (doseq [e [(assoc-in entry [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :binding-table :bindings 0 :visibility] :private)
               (assoc-in entry [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :gravity-resolution-boundary :resolved-analysis :module-contract :exports] [])
               (assoc-in entry [:request :b47-context :authenticated-core-request :binding-table 0 :upstream-binding-id] (sha 99))]]
      (is (nil? (leaf 'body-import-origin request binding e proof))))
    (let [key (:module-key entry)
          bank {key (assoc entry :graph {:functions {(sha 10) {:function-id (sha 10) :body-core-node-id (sha 11)
                                                             :inferred #{} :throws [:pending-sh09]}}})}
          facts {:imported-function-types [proof]}]
      (is (= :accepted (:status (leaf 'body-foreign-bindings request facts bank))))
      (doseq [bad [(assoc-in bank [key :graph :functions (sha 10) :body-core-node-id] (sha 99))
                   (assoc-in bank [key :graph :functions (sha 10) :throws] []) {}]]
        (is (= :rejected (:status (leaf 'body-foreign-bindings request facts bad))))))))

(deftest body-effect-import-bounds-development-kernel
  (is (true? (leaf 'body-import-tables-valid? {:module-type-producers [] :imported-function-types []})))
  (doseq [field [:module-type-producers :imported-function-types]
          malformed [7 nil {} [7] [nil] (vec (repeat 257 {}))]]
    (is (false? (leaf 'body-import-tables-valid?
                     (assoc {:module-type-producers [] :imported-function-types []} field malformed)))))
  (let [entry {:request {:canonical-core-artifact {:nodes []}} :facts {:function-type-table []}}]
    (is (true? (leaf 'body-aggregate-valid? {:nodes (vec (repeat 65536 {}))} {:function-type-table (vec (repeat 256 {}))} [])))
    (is (false? (leaf 'body-aggregate-valid? {:nodes (vec (repeat 65536 {}))} {:function-type-table []}
                      [(assoc-in entry [:request :canonical-core-artifact :nodes] [{}])])) )
    (is (false? (leaf 'body-aggregate-valid? {:nodes []} {:function-type-table (vec (repeat 256 {}))}
                      [(assoc-in entry [:facts :function-type-table] [{}])]))))
  (let [core {:nodes [(node 2 :def [1]) (node 1 :literal [])] :call-edges []}
        facts {:function-type-table [] :type-table {}}]
    (is (= :body-deferred-work-limit (:reason (leaf 'body-derive-nodes-with-foreign core facts {} 2))))
    (is (= 3 (:work-count (leaf 'body-derive-nodes-with-foreign core facts {} 3))))) )

(deftest body-effect-foreign-summary-development-kernel
  (let [coordinate {:binding-id (sha 50) :producer-sh06-artifact-id (sha 60)
                    :producer-sh07-artifact-id (sha 61) :definition-binding-id (sha 70) :function-id (sha 20)}
        imported {:coordinate coordinate :body-core-node-id (sha 1)
                  :function {:latent-effects [:declared-only]}
                  :summary {:inferred #{:compiler/read-ir} :throws [:pending-sh09]}}
        core {:nodes [(node 3 :call [1]) (node 1 :reference [])]
              :call-edges [{:call-core-node-id (sha 3) :callee-binding-id (sha 50)}]}
        result (leaf 'body-derive-nodes-with-foreign core {:function-type-table [] :type-table {}} {(sha 50) imported} 65536)
        call (get-in result [:nodes (sha 3)])]
    (is (= :accepted (:status result)))
    (is (= #{:compiler/read-ir} (:transitive call)))
    (is (= [(sha 1)] (:dependencies call)))
    (is (nil? (:call-body-dependency call)))
    (is (= (assoc coordinate :body-core-node-id (sha 1)) (:foreign-body-dependency call)))
    (let [other (assoc coordinate :producer-sh06-artifact-id (sha 62))
          a [(:producer-sh06-artifact-id coordinate) (:producer-sh07-artifact-id coordinate)]
          b [(:producer-sh06-artifact-id other) (:producer-sh07-artifact-id other)]
          graphs (leaf 'body-producer-graph-identities
                       {a {:facts {:module :a} :graph {:functions {(sha 20) {:inferred #{:a}}}}}
                        b {:facts {:module :b} :graph {:functions {(sha 20) {:inferred #{:b}}}}}})]
      (is (= 2 (count graphs)))
      (is (= #{:a} (get-in graphs [a :effect-graph :functions (sha 20) :inferred])))
      (is (= #{:b} (get-in graphs [b :effect-graph :functions (sha 20) :inferred]))))))

(defn- recursive-kernel-data []
  (let [owner (fn [n] (assoc-in n [:evaluation :owner-function-syntax-id] (sha 20)))
        nodes [(owner (assoc (node 1 :literal []) :attributes {:literal-kind :boolean :value false}))
               (owner (assoc (node 2 :reference []) :attributes {:binding-id (sha 50)}))
               (owner (assoc (node 3 :reference []) :attributes {:binding-id (sha 51)}))
               (owner (node 4 :call [2 3]))
               (owner (assoc (node 5 :literal []) :attributes {:literal-kind :integer :value 0}))
               (owner (assoc (node 6 :if [1 4 5]) :evaluation
                        {:kind :condition-then-exclusive-branch :owner-function-syntax-id (sha 20)
                         :order [{:index 0 :core-node-id (sha 1)}]}))
               (node 7 :fn [6]) (node 8 :def [7])
               (assoc (node 9 :reference []) :attributes {:binding-id (sha 50)})
               (assoc (node 10 :literal []) :attributes {:literal-kind :integer :value 7}) (node 11 :call [9 10])]
        module {:namespace 'example.recursive :profile :meta :target :jvm :effects [] :capabilities []}
        edge {:ordinal 0 :call-core-node-id (sha 4) :caller-function-syntax-id (sha 20) :caller-function-core-node-id (sha 7)
              :callee-binding-id (sha 50) :callee-definition-syntax-id (sha 80) :callee-function-syntax-id (sha 20)
              :callee-function-core-node-id (sha 7) :argument-core-node-ids [(sha 3)]
              :ordered-evaluation-node-ids [(sha 2) (sha 3)] :evaluation-order :operator-then-arguments :classification :local-function}
        external-edge (assoc edge :ordinal 1 :call-core-node-id (sha 11) :caller-function-syntax-id nil
                             :caller-function-core-node-id nil :argument-core-node-ids [(sha 10)]
                             :ordered-evaluation-node-ids [(sha 9) (sha 10)])
        call {:core-node-id (sha 4) :argument-node-ids [(sha 3)]}
        external-call {:core-node-id (sha 11) :argument-node-ids [(sha 10)]
                       :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments}
        source (:source (get nodes 3)) external-source (:source (get nodes 10))
        proof {:domain :gravity/sh08-authoritative-recursive-proof-v1 :function-syntax-id (sha 20)
               :function-core-node-id (sha 7) :definition-binding-id (sha 50) :definition-syntax-id (sha 80)
               :parameter-binding-ids [(sha 51)] :fixed-arity 1 :recursive-call-core-node-id (sha 4)
               :recursive-argument-node-ids [(sha 3)] :recursive-edge edge
               :recursive-call-source {:syntax-id (:syntax-id source) :source-span (:semantic-span source)
                                       :origin-chain [] :generated-origin nil}
               :recursive-call-profile :meta :recursive-call-target :jvm :recursive-call-effects [] :recursive-call-capabilities []
               :base-node-id (sha 5) :base-type :gravity.type/integer :recursive-definition-binding-id (sha 50)
               :parameter-evidence-call-core-node-id (sha 11)
               :parameter-evidence-call {:call-core-node-id (sha 11) :caller-function-syntax-id nil :callee-function-syntax-id (sha 20)
                 :argument-node-ids [(sha 10)] :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments
                 :argument-types [:gravity.type/integer] :result-type :gravity.type/integer
                 :syntax-id (:syntax-id external-source) :source-span (:semantic-span external-source) :origin-chain [] :generated-origin nil
                 :profile :meta :target :jvm :effects [] :capabilities [] :edge external-edge}
               :parameter-types [:gravity.type/integer]
               :proof-bound :one-named-self-recursive-positive-fixed-arity-positional-literal-base
               :captures? false :higher-order? false :monotone-fixed-point? true}
        core {:nodes nodes :module module :call-edges [edge external-edge] :calls [call external-call]
              :definitions [{:binding-id (sha 50) :syntax-id (sha 80)}]
              :function-records [{:function-syntax-id (sha 20) :function-core-node-id (sha 7) :body-core-node-id (sha 6)
                                  :definition-binding-id (sha 50) :definition-kind :named-top-level :fixed-arity 1
                                  :parameter-binding-ids [(sha 51)]}]
              :recursion-components [{:ordinal 0 :kind :self-recursive :function-syntax-ids [(sha 20)]
                                       :function-core-node-ids [(sha 7)] :internal-call-edge-ordinals [0]}]}
        facts {:recursive-proof proof :function-type-table [{:function-id (sha 20) :body-core-node-id (sha 6)
                  :parameters [{:binding-id (sha 51) :type :gravity.type/integer}] :return :gravity.type/integer}]
               :type-table {(sha 1) :gravity.type/bool} :call-type-facts []}]
    [core facts]))

(deftest body-effect-recursive-proof-development-kernel
  (let [[core facts] (recursive-kernel-data)]
    (is (true? (leaf 'body-recursive-proof-valid? core facts)))
    (let [truthy-core (-> core (assoc-in [:nodes 0 :core-form] :reference)
                              (assoc-in [:nodes 0 :attributes] {:binding-id (sha 51)}))
          truthy-facts (assoc-in facts [:type-table (sha 1)] :gravity.type/integer)]
      (is (true? (leaf 'body-recursive-proof-valid? truthy-core truthy-facts)))
      (is (= :accepted (:status (leaf 'body-derive-recursive-nodes truthy-core truthy-facts {} 65536)))))
    (doseq [bad [{:recursive-proof {:parameter-types []} :call-type-facts [7]}
                 {:recursive-proof {:parameter-types []} :call-type-facts [nil]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [7]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [nil]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts []}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [{:call-core-node-id (sha 4)}]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [{:call-core-node-id (sha 11)}]}]]
      (is (nil? (leaf 'body-recursive-call-facts bad))))
    (doseq [mutate [(fn [f] (assoc f :recursive-proof 7))
                    (fn [f] (assoc-in f [:recursive-proof :parameter-types] 7))
                    (fn [f] (assoc-in f [:recursive-proof :recursive-call-core-node-id] (sha 99)))
                    (fn [f] (assoc-in f [:recursive-proof :base-node-id] (sha 10)))
                    (fn [f] (assoc-in f [:recursive-proof :recursive-edge :callee-function-syntax-id] (sha 99)))
                    (fn [f] (assoc-in f [:recursive-proof :parameter-evidence-call :argument-types] [:gravity.type/string]))
                    (fn [f] (assoc-in f [:recursive-proof :captures?] true))]]
      (is (false? (leaf 'body-recursive-proof-valid? core (mutate facts)))))
    (doseq [mutate [(fn [c] (assoc-in c [:recursion-components 0 :kind] :mutually-recursive))
                    (fn [c] (assoc-in c [:recursion-components 0 :internal-call-edge-ordinals] []))
                    (fn [c] (assoc-in c [:recursion-components 0 :function-syntax-ids] 7))
                    (fn [c] (assoc-in c [:nodes 2 :attributes :binding-id] (sha 99)))
                    (fn [c] (assoc-in c [:nodes 9 :attributes :literal-kind] :string))
                    (fn [c] (update c :nodes #(filterv (fn [n] (not= (sha 5) (:node-id n))) %)))]]
      (is (false? (leaf 'body-recursive-proof-valid? (mutate core) facts))))))

(deftest body-effect-recursive-fixed-point-development-kernel
  (let [[core facts] (recursive-kernel-data)
        derived (leaf 'body-derive-recursive-nodes core facts {} 65536)
        graph (:nodes derived)]
    (is (= :accepted (:status derived)))
    (is (= (set (map :node-id (:nodes core))) (set (keys graph))))
    (is (= (sha 6) (get-in graph [(sha 4) :call-body-dependency])))
    (is (= (mapv sha [2 3 6]) (get-in graph [(sha 4) :dependencies])))
    (is (= #{} (get-in graph [(sha 6) :transitive])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    (is (= :body-deferred-work-limit (:reason (leaf 'body-derive-recursive-nodes core facts {} (dec (:work-count derived))))))
    (is (= :accepted (:status (leaf 'body-derive-recursive-nodes core facts {} (:work-count derived)))))
    (doseq [budget [0 (dec (count (:nodes core)))]]
      (is (= :body-deferred-work-limit
             (:reason (leaf 'body-derive-recursive-nodes (update core :nodes conj (node 99 :throw [])) facts {} budget)))))
    ;; Equations alone permit a nonleast solution. Fresh derivation starts at bottom.
    (let [inflated (:nodes (leaf 'body-derive-nodes-pass core facts {} 65536 (:recursive-proof facts) #{:compiler/read-ir}))]
      (is (true? (leaf 'body-graph-equations-valid? core facts {} inflated)))
      (is (not= graph inflated)))
    (doseq [bad [(update core :nodes conj (node 99 :throw []))
                 (update core :nodes #(filterv (fn [n] (not= (sha 2) (:node-id n))) %))
                 (assoc-in core [:nodes 0 :children] [(sha 1)])]]
      (is (= :rejected (:status (leaf 'body-derive-recursive-nodes bad facts {} 65536)))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes-with-foreign core facts {} 65536))))))

(deftest body-effect-public-forwarding-source-boundary
  (let [forms (read-string (str "[" (slurp (path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")) "]"))
        definitions (into {} (keep #(when (and (seq? %) (= 'defn (first %))) [(second %) %]) forms))
        namespace (first (filter #(and (seq? %) (= 'ns (first %))) forms))
        exports (second (first (filter #(and (seq? %) (= :exports (first %))) namespace)))]
    (is (some #{'(:requires [gravity.effects :as effects])} namespace))
    (is (every? (set exports) '[sh09-build-authenticated-body-effect-result sh09-verify-authenticated-body-effect-result]))
    (is (= '(defn sh09-build-authenticated-body-effect-result [typed verification binding]
              (effects/build-body-effect-result typed verification binding))
           (get definitions 'sh09-build-authenticated-body-effect-result)))
    (is (= '(defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]
              (effects/verify-body-effect-result typed verification binding candidate))
           (get definitions 'sh09-verify-authenticated-body-effect-result)))))

(defn- assert-body-products [products result]
  (let [typed (:typed products) core (get-in products [:request :canonical-core-artifact]) graph (:effect-graph result)]
    (is (= :accepted (:status (:c5 products)) (:status (:artifact products)) (:status typed)))
    (is (= :passed (:status (:report products)) (:status (:verification products))))
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (when-not (= :accepted (:status result))
      (throw (ex-info "Body leaf rejected actual products before costly C8 loading" {:diagnostics (:diagnostics result)})))
    (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes graph)))))
    (is (= #{:compiler/read-ir} (get-in graph [:namespace :declared])))
    (is (= #{} (get-in graph [:namespace :inferred])))
    (is (= (count (:function-type-table typed)) (count (:functions graph))))
    (is (every? #(= [:pending-sh09] (:throws %)) (vals (:functions graph))))
    (is (every? #(= #{:compiler/read-ir} (:declared %)) (vals (:functions graph))))
    (is (every? #(= #{} (:inferred %)) (vals (:functions graph))))
    (is (some #(and (= :quote (:operation %)) (empty? (:dependencies %))) (vals (:nodes graph))))
    (is (some #(and (= :fn (:operation %)) (empty? (:evaluated-children %)) (seq (:latent-children %))) (vals (:nodes graph))))
    (is (some #(= :guard (:role %)) (mapcat :conditional-coverage (vals (:nodes graph)))))))

(defn- assert-coherent-body-rejections [products result]
  (let [typed (:typed products) core (get-in products [:request :canonical-core-artifact])
        call (first (:call-type-facts typed))
        guard-id (some #(get-in % [:guard :core-node-id]) (:constraint-ledger typed))
        literal-id (:node-id (first (filter #(= :literal (:core-form %)) (:nodes core))))
        function-index (first (keep-indexed #(when (= (:function-id %2) (:callee-function-syntax-id call)) %1) (:function-type-table typed)))
        mutations [(fn [t] (update t :type-table #(-> % (dissoc literal-id) (assoc (sha 999) :gravity.type/integer))))
                   (fn [t] (update t :call-type-facts #(filterv (fn [f] (not= guard-id (:call-core-node-id f))) %)))
                   (fn [t] (assoc-in t [:function-type-table function-index :body-core-node-id] literal-id))
                   (fn [t] (assoc-in t [:function-type-table function-index :return] :gravity.type/forged))
                   (fn [t] (assoc-in t [:function-type-table function-index :origin-chain] [:substituted]))
                   (fn [t] (assoc-in t [:call-type-facts 0 :callee-function-syntax-id] (sha 888)))
                   (fn [t] (assoc-in t [:function-products :recursion-components] [{:cycle true}]))
                   (fn [t] (assoc t :scope :capture-free-higher-order-fixed-arity-one-hop))]]
    (doseq [mutate mutations]
      (let [rejected (build-leaf (coherent-typed-mutation products mutate))]
        (is (= :rejected (:status rejected)))
        (is (= "C8-VERIFY" (get-in rejected [:diagnostics 0 :rule])))
        (when-not (= :rejected (:status rejected))
          (throw (ex-info "Body mutation accepted before costly module loading" {})))))
    (let [graph (:effect-graph result)
          erased-graph (assoc graph :nodes {} :functions {})
          erased-throws (update graph :functions #(into {} (map (fn [[id f]] [id (assoc f :throws [])])) %))]
      (doseq [candidate [(reseal-effect-graph result erased-graph) (reseal-effect-graph result erased-throws)]]
        (is (= :rejected (:status (leaf 'verify-body-effect-result (:typed products) (:verification products) (:binding products) candidate)))))))
  (is (= :imported-body-effects-pending
         (get-in (leaf 'build-body-effect-result {:scope :direct-imported-functions-types-only} {} {}) [:diagnostics 0 :reason]))))

(deftest body-effect-recursive-authenticated-paired-composition
  (doseq [basename ["function-self-recursive-type" "function-self-recursive-string-type"]]
    (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/" basename %))
                         [".gravity" ".qst"])
          results (mapv build-leaf products)]
      (doseq [[p result] (map vector products results)]
        (let [typed (:typed p) core (get-in p [:request :canonical-core-artifact])
              facts (:identity-input typed) proof (:recursive-proof typed) graph (:effect-graph result)
              recursive-call (:recursive-call-core-node-id proof)]
          (is (= :accepted (:status typed) (:status result)) (pr-str (:diagnostics result)))
          (when-not (= :accepted (:status result))
            (throw (ex-info "Recursive leaf rejected actual products" {:diagnostics (:diagnostics result)})))
          (is (= :passed (:status (:report p)) (:status (:verification p))))
          (is (= :bounded-self-recursive-body-effects-with-thrown-effects-pending (:scope result)))
          (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
          (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes graph)))))
          (is (= :checked (get-in graph [:recursive-fixed-point :equations])))
          (is (= (:body-core-node-id (first (:function-type-table typed)))
                 (get-in graph [:nodes recursive-call :call-body-dependency])))
          (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
          (is (= #{} (get-in graph [:namespace :inferred])))
          (is (every? #(and (= #{} (:inferred %)) (= [:pending-sh09] (:throws %))) (vals (:functions graph))))
          (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))
          (doseq [mutate [(fn [t] (assoc-in t [:recursive-proof :base-node-id] (sha 999)))
                          (fn [t] (assoc-in t [:recursive-proof :recursive-edge :callee-function-syntax-id] (sha 999)))
                          (fn [t] (assoc-in t [:recursive-proof :parameter-evidence-call :argument-types] [:gravity.type/bool]))
                          (fn [t] (assoc-in t [:recursive-call-facts 0 :dispatch] :forged))
                          (fn [t] (assoc t :scope :first-order-fixed-arity-functions-locals-calls))]]
            (is (= :rejected (:status (build-leaf (coherent-typed-mutation p mutate))))))
          (let [inflated (:nodes (leaf 'body-derive-nodes-pass core facts {} 65536 proof #{:compiler/read-ir}))
                forged (assoc (leaf 'body-build-graph core facts inflated) :recursive-fixed-point (:recursive-fixed-point graph))]
            (is (true? (leaf 'body-graph-equations-valid? core facts {} inflated)))
            (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p)
                                           (reseal-effect-graph result forged))))))
          (let [erased (update graph :functions #(into {} (map (fn [[id f]] [id (assoc f :throws [])]) %)))]
            (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p)
                                           (reseal-effect-graph result erased))))))))
      (is (= (:identity-input (first results)) (:identity-input (second results))))))
  (let [products (mapv fresh-recursive-import-products [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (is (= :accepted (get-in p [:typed :status]) (:status result)) (pr-str (:diagnostics result)))
      (when-not (= :accepted (:status result))
        (throw (ex-info "Imported recursive producer rejected" {:diagnostics (:diagnostics result)})))
      (is (= :passed (get-in p [:verification :status])))
      (is (= 2 (count (:producer-effect-graphs result)) (count (:imported-body-dependencies result))))
      (is (every? #(= :gravity/sh08-recursive-function-typed-core-v1 (get-in % [:sh08-identity-input :domain]))
                  (vals (:producer-effect-graphs result))))
      (is (every? #(= :checked (get-in % [:effect-graph :recursive-fixed-point :equations]))
                  (vals (:producer-effect-graphs result))))
      (is (= :passed (:status (leaf 'verify-body-effect-result (:typed p) (:verification p) (:binding p) result))))
      (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
      (is (= :rejected (:status (build-leaf (coherent-typed-mutation p
                    #(assoc-in % [:module-type-producers 0 :identity-input :recursive-proof :base-node-id] (sha 999))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results)))))
  (doseq [extension [".gravity" ".qst"]]
    (let [p (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-07/b47-function-call-recursion/accepted/function-call-recursion" extension))]
      (is (= :rejected (get-in p [:typed :status])))
      (is (= :mutual-recursion-unsupported (get-in p [:typed :diagnostics 0 :actual-type]))))))

(deftest body-effect-imported-authenticated-paired-composition
  ;; One invocation-local pair: actual C5/C6, fresh recursive C7 verification,
  ;; full original four-key binding, and current Gravity effects source.
  (let [products (mapv fresh-import-products [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (let [typed (:typed p) request (:request p) graph (:effect-graph result)
            producer-request (first (get-in request [:module-type-inputs :module-requests]))
            producer-core (:canonical-core-artifact producer-request)
            facts (get-in typed [:module-type-producers 0 :identity-input])
            key [(get-in typed [:module-type-producers 0 :producer-sh06-artifact-id]) (:artifact-id producer-core)]
            producer-graph (get-in result [:producer-effect-graphs key :effect-graph])
            private-function (first (filter #(= 'integer-base (:definition-name %)) (:function-type-table facts)))
            chain-function (first (filter #(= 'integer-chain (:definition-name %)) (:function-type-table facts)))]
        (is (= :accepted (:status (:c5 p)) (:status (:producer-c5 p)) (:status typed)))
        (is (= :passed (:status (:verification p))))
        (is (= request (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (when-not (= :accepted (:status result))
          (throw (ex-info "Imported leaf rejected actual products" {:diagnostics (:diagnostics result)})))
        (is (= :direct-imported-body-effects-with-thrown-effects-pending (:scope result)))
        (is (= :gravity/sh09-direct-imported-body-effects-v1 (get-in result [:identity-input :domain])))
        (is (= (set (map :node-id (get-in request [:canonical-core-artifact :nodes]))) (set (keys (:nodes graph)))))
        (is (= (set (map :node-id (:nodes producer-core))) (set (keys (:nodes producer-graph)))))
        (is (= (count (:function-type-table facts)) (count (:functions producer-graph))))
        (is (map? private-function))
        (is (= (:body-core-node-id private-function)
               (get-in producer-graph [:nodes (:body-core-node-id chain-function) :call-body-dependency])))
        (is (= 4 (count (:imported-body-dependencies result))))
        (is (= 4 (count (filter :foreign-body-dependency (vals (:nodes graph))))))
        (is (every? #(= #{} (:inferred %)) (concat (vals (:functions graph)) (vals (:functions producer-graph)))))
        (is (every? #(= [:pending-sh09] (:throws %)) (concat (vals (:functions graph)) (vals (:functions producer-graph)))))
        (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (is (false? (leaf 'sh09-adapter-compiler-read-request-valid? request typed)))
        (doseq [field [:module-type-producers :imported-function-types]
                malformed [7 [7]]]
          (let [rejected (build-leaf (coherent-typed-mutation p #(assoc % field malformed)))]
            (is (= :rejected (:status rejected)))
            (is (= "C8-VERIFY" (get-in rejected [:diagnostics 0 :rule])))))
        (doseq [mutate [(fn [t] (assoc-in t [:imported-function-types 0 :origin] {}))
                        (fn [t] (assoc-in t [:imported-function-types 0 :function-type :body-core-node-id] (sha 999)))
                        (fn [t] (assoc-in t [:call-type-facts 0 :imported-function :producer-sh06-artifact-id] (sha 999)))
                        (fn [t] (assoc-in t [:module-type-producers 0 :identity-input :function-type-table 0 :body-core-node-id] (sha 999)))]]
          (is (= :rejected (:status (build-leaf (coherent-typed-mutation p mutate))))))
        (doseq [candidate [(-> result (assoc :producer-effect-graphs {}) (assoc-in [:identity-input :producer-effect-graphs] {}))
                           (-> result (assoc :imported-body-dependencies {}) (assoc-in [:identity-input :imported-body-dependencies] {}))
                           (reseal-effect-graph result (assoc graph :nodes {}))
                           (reseal-effect-graph result (update graph :functions
                             #(into {} (map (fn [[id f]] [id (assoc f :throws [])]) %))))]]
          (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))
        (let [bad-request (assoc-in request [:module-type-inputs :module-requests] [])
              rejected (leaf 'build-body-effect-result typed (:verification p) (seal-binding bad-request typed (:verification p)))]
          (is (= :rejected (:status rejected))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))))

(deftest body-effect-authenticated-paired-composition
  (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/local-body-effects" %))
                       [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)] (assert-body-products p result))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (when-not (= (:identity-input (first results)) (:identity-input (second results)))
      (throw (ex-info "Paired body identity differs before costly module loading" {})))
    (assert-coherent-body-rejections (first products) (first results))
    (doseq [extension [".gravity" ".qst"]]
      (let [rejected (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/rejected/local-body-guard" extension))]
        (is (= :rejected (get-in rejected [:typed :status])))
        (is (= "L7-GUARD-TYPE" (get-in rejected [:typed :diagnostics 0 :rule])))
        (when-not (= "L7-GUARD-TYPE" (get-in rejected [:typed :diagnostics 0 :rule]))
          (throw (ex-info "Unexpected non-Bool guard result before costly module loading" {})))))
    ;; Force the real compiler module only after source/type/leaf joins pass.
    (let [bundle (compile-c8-module-bundle (path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"))
          root (module-root-entry bundle) plan (:plan root)
          invoke (fn [function args] (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :body-effect-authenticated-development} bundle function args))]
      (is (= 6 (count (filter #(= :var (:kind %)) (:exports root)))))
      (is (= 3 (get-in plan [:functions 'sh09-build-authenticated-body-effect-result :arity])))
      (is (= 4 (get-in plan [:functions 'sh09-verify-authenticated-body-effect-result :arity])))
      (doseq [[p result] (map vector products results)]
        (let [actual (invoke 'sh09-build-authenticated-body-effect-result [(:typed p) (:verification p) (:binding p)])
              checked (invoke 'sh09-verify-authenticated-body-effect-result [(:typed p) (:verification p) (:binding p) actual])]
          (is (= result actual))
          (is (= :accepted (:status actual)))
          (is (= :passed (:status checked))))))))
