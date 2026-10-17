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
(defn- build-leaf [products]
  (leaf 'build-body-effect-result (:typed products) (:verification products) (:binding products)))
(defn- coherent-typed-mutation [products mutate]
  (let [typed (mutate (:typed products))
        typed (assoc typed :typed-core (leaf 'sh09-adapter-function-typed-core typed))
        identity (leaf 'sh09-adapter-function-identity-input typed)
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
               :function-products (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])}]
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
                     (-> typed (assoc :function-type-table []) (assoc-in [:function-products :function-records] [])))))))

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
    (is (false? (run (assoc fact :argument-types 7))))))

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
