(ns gravity.self-hosting.sh08-loop-recur-type-test
  (:require [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- repository-root []
  (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh08_loop_recur_type_test.clj")))]
    (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file)))))
(defn- path [relative] (.getPath (io/file (repository-root) relative)))
(defn- effects [function & args]
  (apply (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) function args))

(defn- body-helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh09-body-effect-test name)
      (throw (ex-info "Missing existing body-effect test helper" {:helper name}))))

(defn- c7-invoke [function arguments]
  ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7) function arguments))

(defn- fixture [disposition basename extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename extension))

(defn- seed-run [relative]
  (let [source (path relative) text (slurp source)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
        observed (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
        value (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                            (fn [values] (swap! observed conj (vec values)) (original values))]
                (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :loop-recur-explicit-seed-interpreter} plan 'run []))]
    {:value value :transfers @observed}))

(defn- node [id kind children scope attrs]
  {:node-id id :core-form kind :children children :binding-context scope :attributes attrs :resolved-binding-ids []
   :source {:form-id (keyword (str (name id) "-form")) :syntax-id id}
   :evaluation {:kind :left-to-right
                :order (mapv (fn [i child] {:index i :core-node-id child}) (range)
                             (cond (= kind :fn) [] (= kind :if) [(first children)] :else children))}})

(defn- loop-data []
  (let [nodes [(node :again-init :literal [] :initializers {}) (node :value-init :literal [] :initializers {})
               (node :condition :reference [] :loop-body {:binding-id :again})
               (node :next-again :literal [] :loop-body {}) (node :next-value :reference [] :loop-body {:binding-id :value})
               (-> (node :transfer :recur [:next-again :next-value] :loop-body
                         {:target-id :loop-target :target-kind :loop :arity 2 :type-compatibility :pending-sh08})
                   (assoc :resolved-binding-ids [:again :value])
                   (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
               (node :result :reference [] :loop-body {:binding-id :value})
               (node :branch :if [:condition :transfer :result] :loop-body {})
               (-> (node :loop-target :loop [:again-init :value-init :branch] :function-body
                         {:target-id :loop-target :binding-count 2 :body-count 1 :initializer-child-indexes [0 1]
                          :body-child-indexes [2] :result-policy {:kind :last-body :child-index 2}})
                   (assoc :resolved-binding-ids [:again :value])
                   (assoc-in [:evaluation :kind] :initializers-then-body-left-to-right))
               (node :function-target :fn [:loop-target] :namespace {:recur-target-id :function-target})
               (node :definition :def [:function-target] :namespace {})]
        target (fn [id kind bindings scope parent]
                 {:target-id id :target-kind kind :owner-core-node-id id :owner-form-id (keyword (str (name id) "-form"))
                  :owner-syntax-id id :arity (count bindings) :binding-ids bindings :body-scope-id scope
                  :parent-target-id parent :type-compatibility :pending-sh08})
        record (fn [id init ordinal prior]
                 {:loop-core-node-id :loop-target :ordinal ordinal :name (symbol (name id)) :binding-id id
                  :definition-form-id id :definition-syntax-id id :binding-scope-id :loop-body
                  :initializer-form-id (keyword (str (name init) "-form")) :initializer-syntax-id init
                  :initializer-scope-id :initializers :initializer-node-id init :visible-prior-binding-ids prior :mutability :immutable})
        core {:nodes nodes :loop-bindings [(record :again :again-init 0 []) (record :value :value-init 1 [:again])]
              :recur-targets [(target :loop-target :loop [:again :value] :loop-body :function-target)
                             (target :function-target :function [] :function-body nil)]
              :recur-transfers [{:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                                :target-id :loop-target :target-kind :loop :argument-node-ids [:next-again :next-value] :arity 2
                                :tail-position true :evaluation-order :arguments-left-to-right
                                :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}]
              :binding-table [{:binding-id :again :name 'again :scope-id :loop-body :definition-syntax-id :again}
                              {:binding-id :value :name 'value :scope-id :loop-body :definition-syntax-id :value}]}
        table {:again-init :gravity.type/bool :value-init :gravity.type/integer
               :condition :gravity.type/bool :next-again :gravity.type/bool :next-value :gravity.type/integer
               :transfer :gravity.type/never :result :gravity.type/integer :branch :gravity.type/integer
               :loop-target :gravity.type/integer}]
    [core table]))

(deftest loop-recur-authenticated-paired-composition
  ;; This selector authenticates real user products through current C7 and C8.
  ;; Kernel selectors exercise the same C7 source with normalized DATA only.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [products (mapv #((body-helper 'fresh-products) (fixture "accepted" "loop-recur-types" %)) [".gravity" ".qst"])
        results (mapv #((body-helper 'build-leaf) %) products)]
    (is (= (slurp (path (fixture "accepted" "loop-recur-types" ".gravity")))
           (slurp (path (fixture "accepted" "loop-recur-types" ".qst")))))
    (doseq [[p result extension] (map vector products results [".gravity" ".qst"])]
      (let [typed (:typed p) core (get-in p [:request :canonical-core-artifact])
            transfer (first (:recur-transfers core)) id (:recur-core-node-id transfer)
            target (first (filter #(= (:target-id transfer) (:target-id %)) (:recur-targets core)))
            graph (:effect-graph result)]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Actual loop typing/effects rejected"
                         {:typed-status (:status typed) :effect-status (:status result)
                          :type-diagnostics (:diagnostics typed) :effect-diagnostics (:diagnostics result)})))
        (is (= :gravity.type/integer (:return (first (:function-type-table typed)))))
        (is (= :gravity.type/never (get-in typed [:type-table id])))
        (is (= 3 (count (filter #(= :sh08-loop-target-types-v1 (:producer-rule %)) (:constraint-ledger typed)))))
        (is (= :checked (get-in graph [:loop-fixed-point :equations])))
        (is (= target (get-in graph [:nodes id :loop-backedge :source-target])))
        (is (= transfer (get-in graph [:nodes id :loop-backedge :source-transfer])))
        (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (is (= {:value 7 :transfers [[false 7]]} (seed-run (fixture "accepted" "loop-recur-types" extension))))
        (doseq [candidate [((body-helper 'reseal-effect-graph) result (update graph :nodes dissoc id))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :loop-backedge :target-id] "forged"))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :dependencies] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :transitive] #{:compiler/read-ir}))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:loop-fixed-point :equations] :unchecked))]]
          (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))))
    (is (= (get-in products [0 :typed :identity-input]) (get-in products [1 :typed :identity-input])))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (let [p (first products) typed (:typed p) request (:request p) core (:canonical-core-artifact request)
          transfer (first (:recur-transfers core)) slot (first (:loop-bindings core))
          id (:recur-core-node-id transfer)]
      (is (= :gravity.type/integer
             (c7-invoke 'sh08-ft-normal-return-type [:if [:gravity.type/bool :gravity.type/never :gravity.type/integer]
                                                    :gravity.type/unknown])))
      (is (= :accepted
             (:status (c7-invoke 'sh08-ft-loop-type-step
                        [(c7-invoke 'sh08-ft-loop-working-core
                           [core (get-in request [:b47-context :authenticated-core-request :binding-table])])
                         (:type-table typed) {} 65536]))))
      (doseq [candidate [(assoc-in typed [:type-table (:initializer-node-id slot)] :gravity.type/string)
                         (assoc-in typed [:type-table id] :gravity.type/integer)
                         (update typed :type-table dissoc id)
                         (update typed :constraint-ledger
                           #(mapv (fn [row] (if (= id (:lhs row))
                                             (assoc-in row [:dependencies :loop-premise :source-target :target-id] "forged") row)) %))
                         (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))
                         (update typed :constraint-ledger
                           #(mapv (fn [row] (if (= id (:lhs row))
                                             (assoc-in row [:dependencies :loop-premise :source-transfer :argument-node-ids]
                                                       (vec (reverse (:argument-node-ids transfer)))) row)) %))]]
        (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [request candidate]))))))
    (doseq [[basename reason] [["loop-recur-slot-type" :loop-recur-slot-type-mismatch]
                               ["loop-recur-return-type" :function-result-type-mismatch]]
            extension [".gravity" ".qst"]]
      (let [c5 (bootstrap/compiler-c5-resolution-file-artifact (path (fixture "rejected" basename extension)))
            request ((body-helper 'fresh-request) c5)
            result (c7-invoke 'sh08-function-type-core-artifact [request]) diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= ["C7-TYPE-MISMATCH" reason] [(:rule diagnostic) (:reason diagnostic)]))
        (when (= basename "loop-recur-slot-type")
          (let [node (first (filter #(= (:core-node-id diagnostic) (:node-id %))
                                   (get-in request [:canonical-core-artifact :nodes])))]
            (is (= "wrong" (get-in node [:attributes :value])))))))))

(deftest loop-c6-controls-precede-types-current-source
  (require 'gravity.self-hosting.sh07-loop-recur-test)
  (let [diagnostic-result (ns-resolve 'gravity.self-hosting.sh07-loop-recur-test 'diagnostic-result)
        diagnostic-data (ns-resolve 'gravity.self-hosting.sh07-loop-recur-test 'diagnostic-data)]
    (doseq [[basename reason] [["recur-without-target" :recur-target-required]
                               ["loop-recur-arity-mismatch" :recur-arity-mismatch]
                               ["recur-not-tail" :recur-tail-position-required]
                               ["outer-loop-hidden-by-function" :recur-arity-mismatch]]]
      (let [source (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07-b4/rejected/" basename ".gravity"))
            diagnostic (diagnostic-data (diagnostic-result #(bootstrap/sh07-core-file-artifact source)))]
        (is (= ["C6-VERIFY" reason] [(:rule diagnostic) (get-in diagnostic [:facts :reason])])))))
  (doseq [[disposition basename] [["accepted" "loop-recur-types"]
                                  ["rejected" "loop-recur-slot-type"]
                                  ["rejected" "loop-recur-return-type"]]]
    (is (= (slurp (path (fixture disposition basename ".gravity")))
           (slurp (path (fixture disposition basename ".qst")))))))

(deftest normal-return-bottom-development-kernel
  (doseq [[form children nominal expected]
          [[:if [:gravity.type/bool :gravity.type/never :gravity.type/integer] :gravity.type/unknown :gravity.type/integer]
           [:if [:gravity.type/integer :gravity.type/string :gravity.type/never] :gravity.type/unknown :gravity.type/string]
           [:if [:gravity.type/never :gravity.type/integer :gravity.type/integer] :gravity.type/unknown :gravity.type/never]
           [:if [:gravity.type/nil :gravity.type/never :gravity.type/never] :gravity.type/unknown :gravity.type/never]
           [:if [:gravity.type/bool :gravity.type/unknown :gravity.type/never] :gravity.type/unknown :gravity.type/unknown]
           [:do [:gravity.type/never :gravity.type/integer] :gravity.type/integer :gravity.type/never]
           [:let [:gravity.type/never :gravity.type/integer] :gravity.type/integer :gravity.type/never]
           [:collection-literal [:gravity.type/integer :gravity.type/never] :collection :gravity.type/never]
           [:alternatives [] :gravity.type/unknown :gravity.type/unknown]
           [:fn [:gravity.type/never] :function-value :function-value]]]
    (is (= expected (c7-invoke 'sh08-ft-normal-return-type [form children nominal])))))

(defn- effect-loop-data []
  (let [[core table] (loop-data)
        ids (zipmap (concat (map :node-id (:nodes core)) [:again :value])
                    (map #(str "sha256:" (format "%064x" %)) (range 1 100)))
        [core table] (walk/postwalk-replace ids [core table])
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        module {:profile :meta :target :jvm :effects [] :capabilities []}
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        rows (mapv (fn [p] (effects 'body-loop-row p (get nodes (or (:initializer-node-id p)
                                                            (get-in p [:source-transfer :recur-core-node-id]))) module))
                   (:constraints checked))]
    [core {:module module :type-table table :constraint-ledger rows :function-type-table []} ids]))

(deftest loop-effect-backedge-development-kernel
  (let [[core facts ids] (effect-loop-data)
        result (effects 'body-derive-loop-nodes core facts {} 65536)
        graph (:nodes result) transfer (ids :transfer) body (ids :branch)]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= [(ids :next-again) (ids :next-value) body] (get-in graph [transfer :dependencies])))
    (is (= [body] (get-in graph [transfer :loop-backedge :body-core-node-ids])))
    (is (= (first (:recur-transfers core)) (get-in graph [transfer :loop-backedge :source-transfer])))
    (is (= :gravity.type/never (get-in graph [transfer :type])))
    (is (= #{} (get-in graph [transfer :transitive])))
    (is (= (mapv ids [:again-init :value-init :branch]) (get-in graph [(ids :loop-target) :evaluated-children])))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status (effects 'body-derive-loop-nodes core facts {} (:work-count result)))))
    (is (= :body-deferred-work-limit
           (:reason (effects 'body-derive-loop-nodes core facts {} (dec (:work-count result))))))
    (doseq [budget [0 2 6]]
      (is (= :body-deferred-work-limit (:reason (effects 'body-loop-rows core facts budget)))))
    (doseq [indexes [[0] [1] [2 2] [3] [-1] ["2"] [nil] 2]]
      (let [bad (update core :nodes #(mapv (fn [node] (if (= :loop (:core-form node))
                                                        (assoc-in node [:attributes :body-child-indexes] indexes) node)) %))]
        (is (= :incoherent-loop-result-type (:reason (effects 'body-derive-loop-nodes bad facts {} 65536))))))
    (let [seal (fn [c] (assoc c :identity-preimage (select-keys c [:loop-bindings :recur-targets :recur-transfers])))
          recur-node (first (filter #(= :recur (:core-form %)) (:nodes core)))
          duplicate (seal (-> core (update :nodes conj (assoc recur-node :node-id "sha256:duplicate-recur"))
                                  (update :recur-transfers conj (first (:recur-transfers core)))))
          duplicate-facts (update facts :constraint-ledger conj (last (:constraint-ledger facts)))]
      (is (= :incomplete-loop-control-coverage (:reason (effects 'body-loop-joins duplicate duplicate-facts 65536))))
      (doseq [bad [(assoc core :recur-targets [7])
                   (assoc-in core [:recur-targets 0 :arity] nil)
                   (assoc-in core [:recur-targets 0 :arity] "two")
                   (assoc-in core [:recur-targets 0 :binding-ids] 7)
                   (assoc-in core [:loop-bindings 0 :ordinal] "zero")
                   (assoc-in core [:recur-transfers 0 :argument-node-ids] 7)
                   (-> core (assoc :loop-bindings [])
                            (assoc-in [:recur-targets 0 :binding-ids] [])
                            (update-in [:recur-targets 0] dissoc :arity))]]
        (is (= :rejected (:status (effects 'body-derive-loop-nodes (seal bad) facts {} 65536))))))
    (doseq [[c f] [[core (assoc-in facts [:type-table (ids :value-init)] :gravity.type/string)]
                   [core (assoc-in facts [:type-table transfer] :gravity.type/integer)]
                   [core (assoc-in facts [:type-table (ids :loop-target)] :gravity.type/string)]
                   [core (assoc facts :constraint-ledger [])]
                   [core (assoc-in facts [:constraint-ledger 0 :solution :type] :gravity.type/string)]
                   [(assoc-in core [:recur-transfers 0 :target-id] (ids :function-target)) facts]
                   [(update core :nodes #(filterv (fn [n] (not= body (:node-id n))) %)) facts]]]
      (is (= :rejected (:status (effects 'body-derive-loop-nodes c f {} 65536)))))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                        (assoc-in graph [transfer :loop-backedge :body-core-node-ids] []))))
    (let [inflated (:nodes (effects 'body-derive-nodes-pass core facts {} 65536
                              {:loop-summaries {body #{:compiler/read-ir}}} #{}))]
      (is (true? (effects 'body-graph-equations-valid? core facts {} inflated)))
      (is (not= graph inflated)))
    (is (true? (effects 'body-normal-compatible? :gravity.type/never :gravity.type/integer)))
    (is (false? (effects 'body-normal-compatible? :gravity.type/unknown :gravity.type/integer)))))

(deftest loop-working-core-retains-authenticated-bindings-development-kernel
  (let [[normalized table] (loop-data)
        canonical (dissoc normalized :binding-table)
        bindings (:binding-table normalized)
        working (c7-invoke 'sh08-ft-loop-working-core [canonical bindings])
        checked (c7-invoke 'sh08-ft-loop-type-step [working table {} 65536])]
    (is (not (contains? canonical :binding-table)))
    (is (= canonical (dissoc working :binding-table)))
    (is (= bindings (:binding-table working)))
    (is (= :accepted (:status checked)))
    (is (= (c7-invoke 'sh08-ft-loop-type-step [normalized table {} 65536]) checked))
    (doseq [invalid [nil {} [] [(assoc (first bindings) :scope-id :forged)]]]
      (let [bad-working (c7-invoke 'sh08-ft-loop-working-core [canonical invalid])
            result (c7-invoke 'sh08-ft-loop-type-step [bad-working table {} 65536])]
        (is (= invalid (:binding-table bad-working)))
        (is (= :rejected (:status result)))))
    (is (not (contains? canonical :binding-table)))))

(deftest loop-slot-transfer-development-kernel
  (let [[core table] (loop-data) result (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])]
    (is (= :accepted (:status result)))
    (is (= {:again :gravity.type/bool :value :gravity.type/integer} (:bindings result)))
    (is (= [:solved :solved :solved] (mapv :status (:constraints result))))
    (is (= :gravity.type/never (:result-type (last (:constraints result)))))
    (is (= :loop-type-work-limit (get-in (c7-invoke 'sh08-ft-loop-type-step [core table {} (dec (:work-count result))]) [:issues 0 :reason])))
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core table {} (:work-count result)]))))
    (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/string) {} 65536])]
      (is (= ["C7-TYPE-MISMATCH" :next-value :gravity.type/integer :gravity.type/string]
             ((juxt :rule :core-node-id :expected :actual) (first (:issues bad))))))
    (let [pending (c7-invoke 'sh08-ft-loop-type-step [core (dissoc table :value-init) {} 65536])]
      (is (= :gravity.type/unknown (get-in pending [:bindings :value])))
      (is (= :pending (:status (last (:constraints pending))))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {} 65536])]
      (is (= :accepted (:status never)))
      (is (= [:gravity.type/bool :gravity.type/never] (:argument-types (last (:constraints never)))))
      (is (= false (:normal-transfer? (last (:constraints never)))))
      (is (= :solved (:status (last (:constraints never))))))
    (let [sequential (-> core (assoc-in [:nodes 1 :core-form] :reference)
                              (assoc-in [:nodes 1 :attributes] {:binding-id :again}))
          checked (c7-invoke 'sh08-ft-loop-type-step [sequential (assoc table :value-init :gravity.type/bool :next-value :gravity.type/bool) {} 65536])]
      (is (= :accepted (:status checked)))
      (is (= :gravity.type/bool (get-in checked [:bindings :value]))))
    (doseq [bad [(assoc-in core [:loop-bindings 1 :visible-prior-binding-ids] [])
                 (assoc-in core [:recur-transfers 0 :target-id] :function-target)
                 (assoc-in core [:recur-transfers 0 :tail-position] false)
                 (assoc-in core [:recur-transfers 0 :argument-node-ids] [:next-value :next-again])
                 (assoc-in core [:nodes 5 :attributes :target-id] :wrong)
                 (assoc-in core [:loop-bindings 0 :initializer-node-id] :value-init)
                 (assoc-in core [:recur-targets 0 :body-scope-id] :forged)
                 (-> core (assoc-in [:nodes 0 :core-form] :reference) (assoc-in [:nodes 0 :attributes] {:binding-id :value}))
                 (-> core (assoc-in [:recur-transfers 0 :argument-node-ids] [:next-again])
                     (assoc-in [:nodes 5 :children] [:next-again])
                     (assoc-in [:nodes 5 :evaluation :order] [{:index 0 :core-node-id :next-again}]))]]
      (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [bad table {} 65536])))))))

(deftest zero-slot-nested-nearest-development-kernel
  (let [transfer (-> (node :transfer :recur [] :inner-body
                           {:target-id :inner :target-kind :loop :arity 0 :type-compatibility :pending-sh08})
                     (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
        loop-node (fn [id child scope]
                    (assoc-in (node id :loop [child] scope {:target-id id :binding-count 0 :body-count 1
                                                           :initializer-child-indexes [] :body-child-indexes [0]
                                                           :result-policy {:kind :last-body :child-index 0}})
                              [:evaluation :kind] :initializers-then-body-left-to-right))
        target (fn [id scope parent]
                 {:target-id id :target-kind :loop :owner-core-node-id id :owner-form-id (keyword (str (name id) "-form"))
                  :owner-syntax-id id :arity 0 :binding-ids [] :body-scope-id scope :parent-target-id parent :type-compatibility :pending-sh08})
        core {:nodes [transfer (loop-node :inner :transfer :outer-body) (loop-node :outer :inner :namespace)]
              :loop-bindings [] :binding-table [] :recur-targets [(target :inner :inner-body :outer) (target :outer :outer-body nil)]
              :recur-transfers [{:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                :target-id :inner :target-kind :loop :argument-node-ids [] :arity 0 :tail-position true
                :evaluation-order :arguments-left-to-right :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}]}
        result (c7-invoke 'sh08-ft-loop-type-step [core {:transfer :gravity.type/never} {} 65536])]
    (is (= :accepted (:status result)))
    (is (= :solved (:status (first (:constraints result)))))
    (is (= [] (:argument-types (first (:constraints result)))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :outer) {} {} 65536]))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-targets 0 :parent-target-id] nil) {} {} 65536]))))))

(deftest function-recur-authenticated-paired-composition
  ;; This checks compiler acceptance/rejection. Annotated seed execution is
  ;; a separate boundary and is not replaced with an untyped runtime fixture.
  (let [products (mapv #((body-helper 'fresh-products) (fixture "accepted" "function-recur-types" %)) [".gravity" ".qst"])
        results (mapv #((body-helper 'build-leaf) %) products)]
    (is (= (slurp (path (fixture "accepted" "function-recur-types" ".gravity")))
           (slurp (path (fixture "accepted" "function-recur-types" ".qst")))))
    (doseq [[p result] (map vector products results)]
      (let [typed (:typed p) request (:request p) core (:canonical-core-artifact request)
            transfer (first (:recur-transfers core)) id (:recur-core-node-id transfer)
            target (first (filter #(= (:target-id transfer) (:target-id %)) (:recur-targets core)))
            function (first (filter #(= 'again (:definition-name %)) (:function-type-table typed)))
            function-index (first (keep-indexed #(when (= 'again (:definition-name %2)) %1) (:function-type-table typed)))
            rows (filter #(= :sh08-loop-target-types-v1 (:producer-rule %)) (:constraint-ledger typed))
            graph (:effect-graph result)]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Actual function recur typing/effects rejected"
                         {:type-diagnostics (:diagnostics typed) :effect-diagnostics (:diagnostics result)})))
        (is (= :function (:target-kind transfer) (:target-kind target)))
        (is (= [:gravity.type/bool :gravity.type/string] (mapv :type (:parameters function))))
        (is (= (:binding-ids target) (:parameter-binding-ids function)))
        (is (= :gravity.type/string (:return function)))
        (is (= :gravity.type/never (get-in typed [:type-table id])))
        (is (= 1 (count rows)))
        (is (= [:gravity.type/bool :gravity.type/string] (get-in (first rows) [:solution :slot-types])))
        (is (= :checked (get-in graph [:loop-fixed-point :equations])))
        (is (= [(:body-core-node-id function)] (get-in graph [:nodes id :loop-backedge :body-core-node-ids])))
        (is (= target (get-in graph [:nodes id :loop-backedge :source-target])))
        (is (= transfer (get-in graph [:nodes id :loop-backedge :source-transfer])))
        (is (= request (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (doseq [candidate [(assoc-in typed [:type-table id] :gravity.type/string)
                           (assoc-in typed [:function-type-table function-index :parameters 1 :type] :gravity.type/bool)
                           (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))
                           (update typed :constraint-ledger
                             #(mapv (fn [row] (if (= id (:lhs row))
                                               (assoc-in row [:dependencies :loop-premise :source-target :target-kind] :loop) row)) %))]]
          (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [request candidate])))))
        (doseq [candidate [((body-helper 'reseal-effect-graph) result (update graph :nodes dissoc id))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :loop-backedge :target-id] "forged"))
                           ((body-helper 'reseal-effect-graph) result
                            (assoc-in graph [:nodes id :loop-backedge :body-core-node-ids] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :dependencies] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:loop-fixed-point :equations] :unchecked))]]
          (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))))
    (is (= (get-in products [0 :typed :identity-input]) (get-in products [1 :typed :identity-input])))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (doseq [[basename reason rule] [["function-recur-slot-type" :function-recur-slot-type-mismatch "C7-TYPE-MISMATCH"]
                                    ["function-recur-return-type" :function-result-type-mismatch "C7-TYPE-MISMATCH"]
                                    ["function-recur-unresolved-slot" :function-recur-parameter-types-required "C7-ANNOTATION"]]
            extension [".gravity" ".qst"]]
      (let [source (fixture "rejected" basename extension)
            c5 (bootstrap/compiler-c5-resolution-file-artifact (path source))
            request ((body-helper 'fresh-request) c5)
            result (c7-invoke 'sh08-function-type-core-artifact [request])
            diagnostic (first (filter #(= reason (:reason %)) (:diagnostics result)))]
        (is (= :rejected (:status result)))
        (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str (:diagnostics result)))
        (is (= (slurp (path (fixture "rejected" basename ".gravity")))
               (slurp (path (fixture "rejected" basename ".qst")))))))))

(defn- typed-throw-data []
  (let [attrs {:value-child-index 0 :evaluation-order :value-then-transfer
               :construction-order :postorder-after-value :runtime-reachability :not-asserted-by-sh07-b7
               :transfer-policy :error-transfer :result-policy :never :required-effect :error/throw
               :authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection
               :type-legality :pending-sh08 :effect-registry-legality :pending-sh09
               :effect-profile-capability-legality :pending-sh09 :profile-error-lowering-legality :pending-sh09
               :ownership-legality :pending-sh10 :safety-classification :pending-sh11}
        source (fn [id] {:form-id (keyword (str (name id) "-form")) :syntax-id id
                         :semantic-span nil :origin-chain nil :generated-origin nil})
        payload (assoc (node :payload :reference [] :function-body {:binding-id :problem}) :source (source :payload))
        transfer-node (-> (node :transfer :throw [:payload] :function-body attrs)
                          (assoc :source (source :transfer))
                          (assoc :evaluation {:kind :value-then-transfer :order [{:index 0 :core-node-id :payload}]
                                              :region :function-body :owner-function-syntax-id :function}))
        function (assoc (node :function :fn [:transfer] :namespace {}) :source (source :function))
        definition (assoc (node :definition :def [:function] :namespace {}) :source (source :definition))
        transfer (merge (dissoc attrs :value-child-index :evaluation-order)
                        {:ordinal 0 :core-node-id :transfer :form-id :transfer-form :syntax-id :transfer
                         :value-core-node-id :payload :evaluated-children [:payload] :evaluation-region :function-body
                         :evaluation-owner-function-syntax-id :function :ordered-steps [:evaluate-value :transfer-error]})
        forms [{:form-id :transfer-form :syntax-id :transfer :kind :list :value '(throw problem)
                :child-form-ids [:operator-form :payload-form]}
               {:form-id :operator-form :syntax-id :operator :kind :symbol :value 'throw :child-form-ids [] :parent-form-id :transfer-form}
               {:form-id :payload-form :syntax-id :payload :kind :symbol :value 'problem :child-form-ids [] :parent-form-id :transfer-form}]
        core {:nodes [payload transfer-node function definition] :root-core-node-ids [:definition]
              :module {:profile :meta :target :jvm :effects [:error/throw]}
              :error-transfers [transfer] :identity-preimage {:error-transfers [(assoc transfer :authenticated-sh06-artifact-id :projection)]}
              :calls [] :call-edges [] :error-handlers []}
        types {:payload :gravity.type/string :transfer :gravity.type/never}]
    [core forms types]))

(deftest direct-typed-throw-source-and-type-development-kernel
  (let [[core forms types] (typed-throw-data)
        checked (c7-invoke 'sh08-th-products [core forms types 65536])
        invoke #(c7-invoke 'sh08-th-products [% forms types 65536])]
    (is (= :accepted (:status checked)))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (= :sh08-direct-typed-throw-v1 (-> checked :constraints first :producer-rule)))
    (is (= :equality (-> checked :constraints first :kind)))
    (is (contains? (first (:constraints checked)) :origin))
    (is (= [:error-transfers :throw-source :payload-source :payload-type :function-owner :evaluation-order]
           (-> checked :constraints first :invalidation)))
    (is (= :gravity.type/never (c7-invoke 'sh08-ft-infer-node-nominal [(second (:nodes core)) types {} {} [] {}])))
    (is (= :gravity.type/string (c7-invoke 'sh08-ft-normal-return-type [:if [:gravity.type/bool :gravity.type/never :gravity.type/string] :gravity.type/unknown])))
    (doseq [bad [(assoc core :error-transfers [])
                 (update core :error-transfers conj (first (:error-transfers core)))
                 (assoc core :error-transfers 7)
                 (assoc-in core [:error-transfers 0 :value-core-node-id] :function)
                 (assoc-in core [:error-transfers 0 :evaluation-owner-function-syntax-id] :forged)
                 (assoc-in core [:error-transfers 0 :ordered-steps] [:transfer-error :evaluate-value])
                 (assoc-in core [:nodes 1 :evaluation :order 0 :core-node-id] :function)]]
      (is (= "C7-VERIFY" (-> (invoke bad) :diagnostics first :rule))))
    (is (= ["C7-ANNOTATION" :typed-throw-payload-type-required]
           ((juxt :rule :reason) (-> (c7-invoke 'sh08-th-products [core forms (assoc types :payload :gravity.type/unknown) 65536]) :diagnostics first))))
    (is (= "C7-VERIFY" (-> (c7-invoke 'sh08-th-products [core (assoc-in forms [1 :value] 'identity) types 65536]) :diagnostics first :rule)))
    (is (= "C7-GENERIC" (-> (c7-invoke 'sh08-th-products [core forms types 1]) :diagnostics first :rule)))
    (let [never (c7-invoke 'sh08-th-products [core forms (assoc types :payload :gravity.type/never) 65536])]
      (is (= {:function []} (:throws never)))
      (is (false? (-> never :constraints first :solution :normal-transfer?))))
    (is (= [{:function-id :plain :thrown-error-effects [:pending-sh09]}]
           (c7-invoke 'sh08-th-functions [[{:function-id :plain :thrown-error-effects [:pending-sh09]}] (:throws checked)])))
    (is (= "C7-ANNOTATION" (-> (c7-invoke 'sh08-tc-scope
      [(assoc core :call-edges [{:classification :nonlocal-or-nonfunction :caller-function-syntax-id :function}]) (:throws checked) 65536])
      :diagnostics first :rule)))
    (is (false? (:active? (c7-invoke 'sh08-tc-scope
      [(assoc core :call-edges [{:classification :local-function :caller-function-syntax-id :other :callee-function-syntax-id :plain}]) (:throws checked) 65536]))))))

(deftest direct-typed-throw-effect-replay-development-kernel
  ;; Exercise the real coverage guard with distinct authenticated occurrence
  ;; and semantic projection ids, exactly as the existing C6 producer emits.
  (let [[core _ types] (typed-throw-data)
        record {:function-syntax-id :function :function-core-node-id :function}
        core (merge core {:artifact-id :canonical-core :function-records [record] :definitions []
                          :lineage {:authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection}
                          :lexical-bindings [] :match-branch-records [] :evaluation-order [] :recursion-components []})
        core (assoc-in core [:identity-preimage :lineage]
                       {:authenticated-sh06-artifact-id :projection :sh06-semantic-projection-id :projection})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                          :lexical-bindings :match-branch-records :evaluation-order])
                        :lineage (get-in core [:identity-preimage :lineage])
                        :nodes (effects 'body-throw-semantic-nodes core)
                        :error-transfers (effects 'body-throw-semantic-transfers core))
        core (assoc core :identity-preimage preimage)
        facts {:module (:module core) :sh07-shaped-artifact-id :canonical-core
               :function-type-table [{:function-id :function :parameters [] :parameter-binding-ids []
                                      :latent-effects [:error/throw] :capabilities []}]
               :type-table (assoc types :function :fn :definition :fn) :call-type-facts [] :local-binding-facts []
               :function-records [record] :call-edges [] :recursion-components [] :lexical-bindings []}]
    (is (not= (:nodes core) (get-in core [:identity-preimage :nodes])))
    (is (true? (effects 'body-shape-valid? core facts)))
    (doseq [bad [(assoc-in core [:identity-preimage :lineage :sh06-semantic-projection-id] :wrong-projection)
                 (assoc-in core [:nodes 1 :attributes :result-policy] :normal-value)
                 (assoc-in core [:nodes 1 :attributes :forged-field] true)
                 (-> core (assoc-in [:nodes 1 :attributes :authenticated-sh06-artifact-id] :wrong-authenticated)
                          (assoc-in [:error-transfers 0 :authenticated-sh06-artifact-id] :wrong-authenticated))]]
      (is (false? (effects 'body-shape-valid? bad facts)))))
  (let [[core forms types] (typed-throw-data)
        c7 (c7-invoke 'sh08-th-products [core forms types 65536])
        facts {:constraint-ledger (:constraints c7) :type-table types
               :function-type-table [{:function-id :function :thrown-error-effects [:gravity.type/string]}]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms forms}}}
        check #(effects 'body-throw-joins %1 %2 65536)]
    (is (= :accepted (:status (check request facts))))
    (is (= :accepted (:status (check (assoc-in request [:canonical-core-artifact :call-edges]
                                             [{:caller-function-syntax-id :other :callee-function-syntax-id :plain}]) facts))))
    (doseq [bad [(assoc-in request [:canonical-core-artifact :error-transfers] [])
                 (assoc-in request [:canonical-core-artifact :error-transfers] 7)
                 (update-in request [:canonical-core-artifact :error-transfers] conj (first (:error-transfers core)))
                 (assoc-in request [:b47-context :authenticated-core-request :forms 1 :value] 'identity)
                 (assoc-in request [:canonical-core-artifact :call-edges] [{:classification :nonlocal-or-nonfunction :caller-function-syntax-id :function}])]]
      (is (= :rejected (:status (check bad facts)))))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (assoc-in facts [:constraint-ledger 0 :solution :payload-type] :gravity.type/bool)
                 (assoc-in facts [:type-table :payload] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [:pending-sh09])]]
      (is (= :rejected (:status (check request bad)))))
    (doseq [ledger [7 nil {} [7]]
            throw? [true false]]
      (let [request (if throw? request (assoc request :canonical-core-artifact {:nodes [] :error-transfers []}))
            rejected (check request (assoc facts :constraint-ledger ledger))]
        (is (= :rejected (:status rejected)))
        (is (= :malformed-thrown-error-constraint-ledger (:reason rejected)))))
    (let [node (second (:nodes core)) graph {:payload {:type :gravity.type/string :transitive #{} :escaping-thrown-errors #{}}}
          derived (effects 'body-control-node-record node nil graph :gravity.type/never facts {} core nil)]
      (is (= #{:error/throw} (:direct derived) (:transitive derived) (:escaping derived)))
      (is (= #{:gravity.type/string} (:direct-thrown-errors derived) (:escaping-thrown-errors derived)))
      (is (= [:evaluate-value :transfer-error] (get-in derived [:error-transfer :ordered-steps])))
      (is (= (first (:error-transfers core)) (get-in derived [:error-transfer :source-transfer])))
      (let [outer (effects 'body-control-node-record node nil (assoc-in graph [:payload :type] :gravity.type/never)
                      :gravity.type/never facts {} core nil)]
        (is (= #{} (:direct outer) (:direct-thrown-errors outer)))
        (is (false? (get-in outer [:error-transfer :normal-transfer?])))))))

(deftest direct-typed-throw-nested-and-allowance-development-kernel
  (let [[core forms types] (typed-throw-data)
        inner (second (:nodes core))
        outer (-> inner (assoc :node-id :outer-transfer :children [:transfer])
                  (assoc :source {:form-id :outer-transfer-form :syntax-id :outer-transfer
                                  :semantic-span nil :origin-chain nil :generated-origin nil})
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :transfer}]))
        outer-record (-> (first (:error-transfers core))
                         (assoc :ordinal 1 :core-node-id :outer-transfer :form-id :outer-transfer-form
                                :syntax-id :outer-transfer :value-core-node-id :transfer :evaluated-children [:transfer]))
        core (-> core (assoc :nodes [(first (:nodes core)) inner outer
                                    (assoc (nth (:nodes core) 2) :children [:outer-transfer]) (last (:nodes core))])
                      (update :error-transfers conj outer-record))
        core (assoc-in core [:identity-preimage :error-transfers]
                       (mapv #(assoc % :authenticated-sh06-artifact-id :projection) (:error-transfers core)))
        forms (-> forms (assoc-in [0 :parent-form-id] :outer-transfer-form)
                        (conj {:form-id :outer-transfer-form :syntax-id :outer-transfer :kind :list :value '(throw (throw problem))
                               :child-form-ids [:outer-operator-form :transfer-form]}
                              {:form-id :outer-operator-form :syntax-id :outer-operator :kind :symbol :value 'throw
                               :child-form-ids [] :parent-form-id :outer-transfer-form}))
        types (assoc types :outer-transfer :gravity.type/never)
        checked (c7-invoke 'sh08-th-products [core forms types 65536])
        facts {:constraint-ledger (:constraints checked) :type-table types
               :function-type-table [{:function-id :function :thrown-error-effects [:gravity.type/string]}]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms forms}}}
        inner-graph (effects 'body-control-node-record inner nil {:payload {:type :gravity.type/string :transitive #{}}}
                             :gravity.type/never facts {} core nil)
        outer-graph (effects 'body-control-node-record outer nil {:transfer inner-graph}
                             :gravity.type/never facts {} core nil)]
    (is (= :accepted (:status checked)))
    (is (= 2 (count (:constraints checked))))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (false? (get-in checked [:constraints 1 :solution :normal-transfer?])))
    (is (= :accepted (:status (effects 'body-throw-joins request facts 65536))))
    (is (= #{} (:direct outer-graph) (:direct-thrown-errors outer-graph)))
    (is (= #{:error/throw} (:escaping outer-graph)))
    (is (= #{:gravity.type/string} (:escaping-thrown-errors outer-graph))))
  (let [[core forms types] (typed-throw-data)
        outer (-> (nth (:nodes core) 2) (assoc :node-id :outer :children [:function])
                  (assoc-in [:source :syntax-id] :outer) (assoc-in [:source :form-id] :outer-form))
        nested (-> core (update :nodes #(vec (concat (take 3 %) [outer (assoc (last %) :children [:outer])]))))
        checked (c7-invoke 'sh08-th-products [nested forms types 65536])]
    (is (= :accepted (:status checked)))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (= [{:function-id :outer :thrown-error-effects [:pending-sh09]}
            {:function-id :function :thrown-error-effects [:gravity.type/string]}]
           (c7-invoke 'sh08-th-functions [[{:function-id :outer :thrown-error-effects [:pending-sh09]}
                                           {:function-id :function :thrown-error-effects [:pending-sh09]}] (:throws checked)]))))
  (let [core {:nodes [{:node-id :first} {:node-id :second} {:node-id :top}]
              :function-records [{:function-syntax-id :first} {:function-syntax-id :second}]}
        facts {:module {:effects []} :function-type-table [{:function-id :second :function-core-node-id :second}
                                                          {:function-id :first :function-core-node-id :first}]}
        graph {:functions {:first {:declared #{} :inferred #{:error/throw}}
                           :second {:declared #{} :inferred #{:error/throw}}}
               :nodes {:first {:direct #{}} :second {:direct #{}} :top {:direct #{:error/throw}}}}
        first-failure (effects 'body-allowance-check core facts graph 65536)
        namespace-failure (effects 'body-allowance-check core facts
                            (-> graph (assoc-in [:functions :first :declared] #{:error/throw})
                                      (assoc-in [:functions :second :declared] #{:error/throw})) 65536)]
    (is (= "C8-UNDECLARED" (:rule first-failure) (:rule namespace-failure)))
    (is (= :first (get-in first-failure [:node :node-id])))
    (is (= :first (:function first-failure)))
    (is (= {:rule "C8-UNDECLARED" :function :first :core-node-id :first}
           (select-keys (-> (effects 'body-error-result facts first-failure) :diagnostics first)
                        [:rule :function :core-node-id])))
    (is (= :top (get-in namespace-failure [:node :node-id])))
    (is (= :error/throw (:effect namespace-failure)))
    (is (= "C8-UNDECLARED" (-> (effects 'body-error-result facts namespace-failure) :diagnostics first :rule)))
    (is (= "C8-VERIFY" (-> (effects 'body-error-result facts {:reason :forged}) :diagnostics first :rule)))
    (is (= :rejected (:status (effects 'body-allowance-check core facts graph 0))))))

(defn- local-throw-kernel-data []
  (let [id (fn [n] ((body-helper 'sha) n))
        fid #(id (+ 100 %)) fnid #(id (+ 200 %)) defid #(id (+ 300 %)) binding #(id (+ 400 %)) syntax #(id (+ 500 %))
        name-syntax #(id (+ 700 %)) function-name #(symbol (str "function-" %))
        make (fn [n kind children owner attrs]
               {:node-id (id n) :core-form kind :children (mapv id children) :attributes attrs :resolved-binding-ids []
                :source {:form-id (id (+ 10000 n)) :syntax-id (id (+ 20000 n))
                         :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil}
                :evaluation {:kind (if (= kind :fn) :function-creation :left-to-right)
                             :owner-function-syntax-id (when owner (fid owner))
                             :order (mapv (fn [index child] {:index index :core-node-id (id child)})
                                          (range) (if (= kind :fn) [] children))}})
        call (fn [n operator args caller callee]
               (assoc (make n :call (into [operator] args) caller {}) :resolved-binding-ids [(binding callee)]))
        bodies {1 19 2 4 3 12 4 6 5 10 6 15 7 16}
        nodes [(make 1 :literal [] 1 {:literal-kind :string :value "problem"}) (make 2 :throw [1] 1 {})
               (make 3 :reference [] 2 {:binding-id (binding 1)}) (call 4 3 [] 2 1)
               (make 11 :reference [] 3 {:binding-id (binding 2)}) (call 12 11 [] 3 2)
               (make 5 :literal [] 4 {:literal-kind :boolean :value true}) (make 6 :throw [5] 4 {})
               (make 7 :reference [] 5 {:binding-id (binding 4)})
               (make 8 :literal [] 5 {:literal-kind :string :value "argument"}) (make 9 :throw [8] 5 {}) (call 10 7 [9] 5 4)
               (make 13 :literal [] 6 {:literal-kind :string :value "isolated"})
               (make 14 :throw [13] 6 {}) (make 15 :loop [14] 6 {})
               (make 16 :literal [] 7 {:literal-kind :string :value "pure"})
               (make 17 :reference [] 1 {:binding-id (binding 7)}) (call 18 17 [] 1 7) (make 19 :do [18 2] 1 {})]
        records (mapv (fn [f] {:ordinal (dec f) :function-syntax-id (fid f) :function-core-node-id (fnid f)
                               :definition-core-node-id (defid f) :definition-binding-id (binding f)
                               :body-core-node-id (id (bodies f)) :fixed-arity (if (= f 4) 1 0) :parameter-binding-ids []}) (range 1 8))
        definitions (mapv (fn [f] {:name (function-name f) :binding-id (binding f) :core-node-id (defid f) :syntax-id (syntax f)
                                   :value-node-id (fnid f)}) (range 1 8))
        functions (into {} (map (fn [f] [(fid f) {:artifact :gravity/c7-function-type :function-id (fid f)
                            :body-core-node-id (id (bodies f)) :function-core-node-id (fnid f)
                            :parameters (if (= f 4) [{:binding-id (id 900) :type :gravity.type/bool}] [])
                            :return :gravity.type/string :fixed-arity (if (= f 4) 1 0) :latent-effects [:error/throw]
                            :capabilities [] :thrown-error-effects [:pending-sh09]}]) (range 1 8)))
        specs [[4 3 [] 2 1] [12 11 [] 3 2] [10 7 [9] 5 4] [18 17 [] 1 7]]
        calls (mapv (fn [[n operator args _ callee]]
                       {:core-node-id (id n) :operator-node-id (id operator) :operator-binding-id (binding callee)
                        :argument-node-ids (mapv id args) :ordered-evaluation-node-ids (mapv id (into [operator] args))
                        :evaluation-order :operator-then-arguments :result-policy :call-result}) specs)
        edges (mapv (fn [ordinal [n operator args caller callee]]
                       {:ordinal ordinal :call-core-node-id (id n) :caller-function-syntax-id (fid caller)
                        :caller-function-core-node-id (fnid caller) :callee-binding-id (binding callee)
                        :callee-definition-syntax-id (name-syntax callee) :callee-function-syntax-id (fid callee)
                        :callee-function-core-node-id (fnid callee) :argument-core-node-ids (mapv id args)
                        :ordered-evaluation-node-ids (mapv id (into [operator] args)) :evaluation-order :operator-then-arguments
                        :classification :local-function}) (range) specs)
        nodes (into nodes (mapcat (fn [f]
                         [(-> (make (+ 200 f) :fn [(bodies f)] nil {}) (assoc-in [:source :syntax-id] (fid f)))
                          (-> (make (+ 300 f) :def [(+ 200 f)] nil {:name (function-name f) :name-syntax-id (name-syntax f)})
                              (assoc :resolved-binding-ids [(binding f)]) (assoc-in [:source :syntax-id] (syntax f)))]) (range 1 8)))
        types (into {} (map (fn [node] [(:node-id node) :gravity.type/string]) nodes))
        types (merge types {(id 2) :gravity.type/never (id 5) :gravity.type/bool (id 6) :gravity.type/never
                            (id 9) :gravity.type/never (id 10) :gravity.type/never (id 14) :gravity.type/never})
        core {:nodes nodes :module {:profile :meta :target :jvm :effects [:error/throw]}
              :root-core-node-ids (mapv defid (range 1 8)) :function-records records :definitions definitions
              :calls calls :call-edges edges :recursion-components []
              :error-transfers (mapv (fn [[n value]] {:core-node-id (id n) :value-core-node-id (id value)}) [[2 1] [6 5] [9 8] [14 13]])}
        direct {:status :accepted :throws {(fid 1) [:gravity.type/string] (fid 4) [:gravity.type/bool]
                                          (fid 5) [:gravity.type/string] (fid 6) [:gravity.type/string]}
                :constraints [] :diagnostics [] :work-count 0}
        bindings (mapv (fn [f] {:name (function-name f) :binding-id (binding f) :definition-syntax-id (name-syntax f)}) (range 1 8))]
    {:core core :types types :functions functions :direct direct :bindings bindings :id id :fid fid}))

(deftest loop-called-source-error-development-kernel
  ;; Normalized source graph DATA, not an authenticated input. The existing
  ;; call joins are unchanged; loop/recur add only lexical child dependencies.
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        backedge (node :backedge :recur [(id 2)] :loop-body {:target-kind :loop})
        core (update core :nodes #(mapv (fn [n] (if (= (id 19) (:node-id n))
                                                (assoc n :core-form :loop :children [(id 18) :backedge]) n)) (conj % backedge)))
        types (assoc types :backedge :gravity.type/never (id 19) :gravity.type/never)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct 65536])]
    (is (= :accepted (:status result)))
    (doseq [f [1 2 3]] (is (= [:gravity.type/string] (get-in result [:throws (fid f)]))))
    (is (= 4 (count (:constraints result))))
    (let [row (first (filter #(= (id 4) (get-in % [:lhs :core-node-id])) (:constraints result)))]
      (is (= [:gravity.type/string] (get-in row [:dependencies :callee-error-types])))
      (is (= [:gravity.type/string] (get-in row [:solution :thrown-error-effects]))))
    (is (= "C7-GENERIC" (get-in (c7-invoke 'sh08-tc-products [core types functions bindings direct 1]) [:diagnostics 0 :rule])))
    (is (= :unsupported-throwing-body-operation
           (get-in (c7-invoke 'sh08-tc-products
                     [(update core :nodes #(mapv (fn [n] (if (= :backedge (:node-id n)) (assoc n :core-form :set!) n)) %))
                      types functions bindings direct 65536]) [:diagnostics 0 :reason])))))

(deftest local-throw-call-closure-development-kernel
  ;; Normalized DATA exercises the actual Gravity algorithms, not authentication.
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct 65536])]
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (doseq [f [1 2 3 5 6]] (is (= [:gravity.type/string] (get-in result [:throws (fid f)]))))
    (is (= [:gravity.type/bool] (get-in result [:throws (fid 4)])))
    (is (= [] (get-in result [:throws (fid 7)])))
    (is (= 4 (count (:constraints result))))
    (let [row (first (filter #(= (id 10) (get-in % [:lhs :core-node-id])) (:constraints result)))]
      (is (= {:core-node-id (id 10) :projection :thrown-error-effects} (:lhs row)))
      (is (false? (get-in row [:dependencies :normal-invocation?])))
      (is (= [] (get-in row [:dependencies :callee-error-types])))
      (is (= :gravity.type/never (get-in row [:solution :normal-result-type]))))
    (is (= (c7-invoke 'sh08-tc-products [core types functions bindings (assoc direct :throws {}) 65536])
           (assoc direct :throws {})))
    (is (= "C7-GENERIC" (-> (c7-invoke 'sh08-tc-products [core types functions bindings direct 1]) :diagnostics first :rule)))
    (let [cost (:work-count result)]
      (is (= :accepted (:status (c7-invoke 'sh08-tc-products [core types functions bindings direct (inc cost)])))))
    (let [direct (assoc direct :work-count 17)
          baseline (:work-count result)
          accepted (c7-invoke 'sh08-tc-products [core types functions bindings direct (inc baseline)])]
      (is (= :accepted (:status accepted)))
      (is (= (+ 17 baseline) (:work-count accepted)))
      (is (= "C7-GENERIC" (-> (c7-invoke 'sh08-tc-products [core types functions bindings direct (dec baseline)]) :diagnostics first :rule))))
    (let [isolated (assoc direct :throws {(fid 6) [:gravity.type/string]} :work-count 17)
          no-touch (c7-invoke 'sh08-tc-products [core types functions bindings isolated 65536])]
      (is (= (:throws isolated) (:throws no-touch)))
      (is (= (+ 17 (count (:call-edges core))) (:work-count no-touch))))
    (let [scoped (c7-invoke 'sh08-tc-scope [core (:throws direct) 65536])
          initial (+ (:work-count scoped) (* 8 (count (:nodes core))) (* 70 (count (:call-edges core)))
                     (count bindings) (count (:definitions core)) (* 3 (count (:function-records core)))
                     (* (count (:calls core)) (+ (count bindings) (count (:definitions core)) (* 2 (count (:function-records core))))))
          exhausted (c7-invoke 'sh08-tc-products [core types functions bindings direct (inc initial)])]
      (is (= "C7-GENERIC" (-> exhausted :diagnostics first :rule)))
      (is (= :typed-throw-work-limit (-> exhausted :diagnostics first :reason))))
    (is (= "C7-VERIFY" (-> (c7-invoke 'sh08-tc-products [(assoc-in core [:call-edges 0 :callee-binding-id] :forged) types functions bindings direct 65536]) :diagnostics first :rule)))
    (let [definition (first (:definitions core)) binding (first bindings)
          def-index (first (keep-indexed #(when (= (:core-node-id definition) (:node-id %2)) %1) (:nodes core)))]
      (is (not= (:definition-syntax-id binding) (:syntax-id definition)))
      (doseq [[bad-core bad-bindings]
              [[(assoc-in core [:call-edges 0 :callee-definition-syntax-id] (:syntax-id definition)) bindings]
               [core (assoc-in bindings [0 :definition-syntax-id] (:syntax-id definition))]
               [(assoc-in core [:nodes def-index :attributes :name-syntax-id] (:syntax-id definition)) bindings]
               [(assoc-in core [:definitions 0 :syntax-id] (:definition-syntax-id binding)) bindings]
               [(assoc-in core [:function-records 0 :body-core-node-id] (id 6)) bindings]]]
        (let [bad (c7-invoke 'sh08-tc-products [bad-core types functions bad-bindings direct 65536])]
          (is (= "C7-VERIFY" (-> bad :diagnostics first :rule)))
          (is (= (id 4) (get-in bad [:diagnostics 0 :node :node-id]))))))
    (is (= "C7-ANNOTATION" (-> (c7-invoke 'sh08-tc-products [(assoc core :recursion-components [{:function-syntax-ids [(fid 1)]}]) types functions bindings direct 65536]) :diagnostics first :rule)))))

(deftest local-throw-call-existing-type-guards-development-kernel
  (let [{:keys [core types functions id]} (local-throw-kernel-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        call (last (butlast (:calls core))) edge (last (butlast (:call-edges core)))
        invoke (fn [call edge] (c7-invoke 'sh08-ft-call-constraints-with-context
                                [[call] [edge] nodes types {(id 900) :gravity.type/bool} functions {}]))]
    (is (= [] (:diagnostics (invoke call edge))))
    (is (= :gravity.type/never
           (c7-invoke 'sh08-ft-infer-node-with-context [(get nodes (id 10)) types {} functions (:call-edges core) {}])))
    (is (= :call-arity-mismatch
           (-> (invoke (assoc call :argument-node-ids []) (assoc edge :argument-core-node-ids [])) :diagnostics first :reason)))
    (is (= :call-argument-type-mismatch
           (-> (invoke (assoc call :argument-node-ids [(id 1)]) (assoc edge :argument-core-node-ids [(id 1)])) :diagnostics first :reason)))))

(defn- typed-handler-kernel-data []
  (let [[core forms types] (typed-throw-data)
        source (fn [id] {:form-id (keyword (str (name id) "-form")) :syntax-id id
                         :semantic-span nil :origin-chain nil :generated-origin nil})
        clause {:clause-ordinal 0 :handler-child-index 1 :catch-clause-form-id :catch-form :catch-clause-syntax-id :catch
                :error-type-form-id :type-form :error-type-syntax-id :type :error-type-binding-id :string-type
                :catch-binding-form-id :binder-form :catch-binding-syntax-id :binder :catch-binding-id :caught :catch-binding-scope-id :catch-scope}
        attrs {:protected-child-index 0 :handler-count 1 :handler-child-indexes [1] :handler-clauses [clause]
               :evaluation-order :protected-then-ordered-typed-handler-candidates :runtime-reachability :not-asserted-by-sh07-b9
               :selection-policy :source-ordered-typed-handler-candidates :result-policy :protected-or-selected-handler-last
               :authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection
               :type-coverage-legality :pending-sh08 :result-type-join-legality :pending-sh08
               :effect-registry-legality :pending-sh09 :effect-profile-capability-legality :pending-sh09
               :profile-error-lowering-legality :pending-sh09 :ownership-legality :pending-sh10 :safety-classification :pending-sh11}
        handler (assoc (node :handler :reference [] :catch-scope {:binding-id :caught}) :source (source :handler))
        try-node (-> (node :try :try [:transfer :handler] :function-body attrs) (assoc :source (source :try))
                     (assoc :resolved-binding-ids [:string-type :caught])
                     (assoc :evaluation {:kind :protected-then-ordered-typed-handler-candidates
                                         :order [{:index 0 :core-node-id :transfer}] :region :function-body :owner-function-syntax-id :function}))
        record (merge (dissoc attrs :protected-child-index :handler-count :handler-child-indexes :handler-clauses :evaluation-order)
                      (dissoc clause :clause-ordinal)
                      {:ordinal 0 :clause-ordinal 0 :clause-count 1 :core-node-id :try :form-id :try-form :syntax-id :try
                       :protected-core-node-id :transfer :handler-core-node-id :handler :catch-binding-use-syntax-ids [:handler]
                       :candidate-error-transfers [{:ordinal 0 :core-node-id :transfer}]
                       :evaluation-region :function-body :evaluation-owner-function-syntax-id :function
                       :ordered-steps [:evaluate-protected :conditionally-select-source-ordered-typed-handler]
                       :construction-order :protected-then-handlers-source-order-then-try
                       :runtime-evaluation :protected-then-conditional-handler-chain})
        core (-> core (assoc :nodes [(first (:nodes core)) (second (:nodes core)) handler try-node
                                    (assoc (nth (:nodes core) 2) :children [:try]) (last (:nodes core))])
                      (assoc :error-handlers [record]
                             :function-records [{:function-syntax-id :function :function-core-node-id :function :body-core-node-id :try}])
                      (assoc-in [:identity-preimage :error-handlers] [(assoc record :authenticated-sh06-artifact-id :projection)]))
        forms (-> forms (assoc-in [0 :parent-form-id] :try-form)
                  (into [{:form-id :try-form :syntax-id :try :kind :list :value '(try (throw problem) (catch String e e))
                          :child-form-ids [:try-op-form :transfer-form :catch-form]}
                         {:form-id :try-op-form :syntax-id :try-op :kind :symbol :value 'try :child-form-ids [] :parent-form-id :try-form}
                         {:form-id :catch-form :syntax-id :catch :kind :list :value '(catch String e e)
                          :child-form-ids [:catch-op-form :type-form :binder-form :handler-form] :parent-form-id :try-form}
                         {:form-id :catch-op-form :syntax-id :catch-op :kind :symbol :value 'catch :child-form-ids [] :parent-form-id :catch-form}
                         {:form-id :type-form :syntax-id :type :kind :symbol :value 'String :child-form-ids [] :parent-form-id :catch-form}
                         {:form-id :binder-form :syntax-id :binder :kind :symbol :value 'e :child-form-ids [] :parent-form-id :catch-form :scope-id :catch-scope}
                         {:form-id :handler-form :syntax-id :handler :kind :symbol :value 'e :child-form-ids [] :parent-form-id :catch-form :scope-id :catch-scope}]))
        bindings [{:binding-id :string-type :name 'String :binding-class :core :kind :type :namespace 'gravity.core}
                  {:binding-id :caught :name 'e :binding-class :lexical :kind :local :definition-syntax-id :binder :scope-id :catch-scope}]]
    {:core core :forms forms :bindings bindings :types (assoc types :handler :gravity.type/string :try :gravity.type/string)}))

(defn- loop-handler-kernel-data []
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        loop-node (-> (node :protected-loop :loop [:transfer] :function-body
                           {:target-id :protected-loop :binding-count 0 :body-count 1 :initializer-child-indexes []
                            :body-child-indexes [0] :result-policy {:kind :last-body :child-index 0}})
                      (assoc-in [:evaluation :kind] :initializers-then-body-left-to-right)
                      (assoc :source {:form-id :protected-loop-form :syntax-id :protected-loop
                                      :semantic-span nil :origin-chain nil :generated-origin nil}))
        target (fn [id kind parent] {:target-id id :target-kind kind :owner-core-node-id id
                                     :owner-form-id (keyword (str (name id) "-form")) :owner-syntax-id id
                                     :arity 0 :binding-ids [] :body-scope-id :function-body :parent-target-id parent
                                     :type-compatibility :pending-sh08})
        record (assoc (first (:error-handlers core)) :protected-core-node-id :protected-loop)
        core (-> core
                 (update :nodes #(vec (mapcat (fn [n] (cond
                           (= :try (:node-id n)) [loop-node (-> n (assoc-in [:children 0] :protected-loop)
                                                              (assoc-in [:evaluation :order 0 :core-node-id] :protected-loop))]
                           (= :function (:node-id n)) [(assoc-in n [:attributes :recur-target-id] :function)]
                           :else [n])) %)))
                 (assoc :loop-bindings [] :recur-transfers [] :binding-table []
                        :recur-targets [(target :protected-loop :loop :function) (target :function :function nil)]
                        :error-handlers [record])
                 (assoc-in [:identity-preimage :error-handlers] [(assoc record :authenticated-sh06-artifact-id :projection)]))
        forms (into (mapv (fn [f] (cond
                         (= :transfer-form (:form-id f)) (assoc f :parent-form-id :protected-loop-form)
                         (= :try-form (:form-id f)) (assoc f :value '(try (loop [] (throw problem)) (catch String e e))
                                                                        :child-form-ids [:try-op-form :protected-loop-form :catch-form])
                         :else f)) forms)
                [{:form-id :protected-loop-form :syntax-id :protected-loop :kind :list :value '(loop [] (throw problem))
                  :child-form-ids [:loop-op-form :loop-bindings-form :transfer-form] :parent-form-id :try-form}
                 {:form-id :loop-op-form :syntax-id :loop-op :kind :symbol :value 'loop :child-form-ids [] :parent-form-id :protected-loop-form}
                 {:form-id :loop-bindings-form :syntax-id :loop-bindings :kind :vector :value [] :child-form-ids [] :parent-form-id :protected-loop-form}])]
    {:core core :forms forms :bindings bindings :types (assoc types :protected-loop :gravity.type/never)}))

(deftest loop-handler-source-error-development-kernel
  ;; No calls: a handler owner alone activates the actual source-error graph.
  (let [{:keys [core forms bindings types]} (loop-handler-kernel-data)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {} 65536])
        source (c7-invoke 'sh08-eh-source-products [core forms bindings 65536])
        independent (effects 'body-eh-source-products core forms bindings 65536)
        direct (c7-invoke 'sh08-th-products [core forms types 65536])
        products (c7-invoke 'sh08-tc-products [core types {} bindings direct 65536])
        owners (c7-invoke 'sh08-th-owners [core 65536])
        derived (c7-invoke 'sh08-tc-graph [core types (:owners owners) #{:function} 65536 (:groups source)])
        rows (filterv #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products))
        facts {:type-table types :constraint-ledger rows :atomic-typed-handlers (:groups independent)}
        graph (reduce (fn [g n] (assoc g (:node-id n)
                                 (effects 'body-control-node-record n nil g (get types (:node-id n)) facts {} core nil))) {} (:nodes core))
        request {:b47-context {:authenticated-core-request {:forms forms :binding-table bindings
                    :resolution-table [{:reference-syntax-id :type :position :type :symbol 'String :binding-id :string-type}
                                       {:reference-syntax-id :handler :position :value :symbol 'e :binding-id :caught}]}}}
        verify #(effects 'body-eh-verify request core %1 (:groups independent) %2 65536)]
    (is (= :accepted (:status checked)))
    (is (= :accepted (:status source) (:status independent) (:status direct) (:status products) (:status derived))
        (pr-str {:source source :independent independent}))
    (is (= [] (:call-edges core)))
    (is (= [:gravity.type/string] (get-in derived [:nodes :protected-loop])))
    (is (= [] (get-in derived [:nodes :try]) (get-in products [:throws :function])))
    (is (= 1 (count rows)))
    (is (= [:gravity.type/string] (get-in rows [0 :dependencies :child-source-error-types 0])))
    (is (= [] (get-in rows [0 :solution :thrown-error-effects])))
    (is (= :gravity.type/string (get-in rows [0 :solution :normal-result-type])))
    (is (= :accepted (:status (verify facts graph))))
    (is (= #{:error/throw} (get-in graph [:try :escaping])))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (assoc-in facts [:constraint-ledger 0 :dependencies :child-source-error-types 0] [])
                 (assoc-in facts [:constraint-ledger 0 :solution :thrown-error-effects] [:gravity.type/bool])
                 (assoc-in facts [:constraint-ledger 0 :dependencies :source-node :source :syntax-id] :forged)]]
      (is (= :rejected (:status (verify bad graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [:protected-loop :escaping-thrown-error-types] [])))))
    (let [clauses [{:record :first :error-type :gravity.type/string :handler-core-node-id :rethrow}
                   {:record :sibling :error-type :gravity.type/bool :handler-core-node-id :sibling}]
          g (assoc (:nodes derived) :rethrow [:gravity.type/bool] :sibling [])
          partition (c7-invoke 'sh08-eh-partition [(get g :protected-loop) clauses g])]
      (is (= [:gravity.type/bool] (:residual partition)))
      (is (= [[:gravity.type/string] []] (mapv :matched-error-types (:handled partition)))))
    (is (= "C7-GENERIC" (get-in (c7-invoke 'sh08-tc-products [core types {} bindings direct 1]) [:diagnostics 0 :rule])))))

(deftest atomic-typed-handler-source-development-kernel
  (let [{:keys [core forms bindings]} (typed-handler-kernel-data)
        check #(c7-invoke 'sh08-eh-source-products [%1 %2 %3 65536])
        result (check core forms bindings)]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :accepted (:status (effects 'body-eh-source-products core forms bindings 65536))))
    (is (= {:caught :gravity.type/string} (:seeds result)))
    (is (= [:gravity.type/string] (mapv :error-type (get-in result [:groups :try]))))
    (doseq [[bad-core bad-forms bad-bindings]
            [[(assoc-in core [:error-handlers 0 :candidate-error-transfers] []) forms bindings]
             [(assoc-in core [:error-handlers 0 :clause-ordinal] 1) forms bindings]
             [(assoc-in core [:error-handlers 0 :catch-binding-id] :other) forms bindings]
             [(assoc-in core [:nodes 3 :attributes :handler-child-indexes] [0]) forms bindings]
             [(assoc core :error-handlers 7) forms bindings]
             [(assoc-in core [:nodes 3 :children] 7) forms bindings]
             [(assoc-in core [:nodes 3 :attributes :handler-clauses] 7) forms bindings]
             [(assoc-in core [:nodes 3 :resolved-binding-ids] [:caught :string-type]) forms bindings]
             [(assoc-in core [:nodes 3 :evaluation :owner-function-syntax-id] :other) forms bindings]
             [(assoc-in core [:nodes 0 :attributes :binding-id] :caught) forms bindings]
             [core forms (assoc-in bindings [0 :kind] :function)]
             [core forms (assoc-in bindings [1 :scope-id] :sibling)]
             [core (assoc-in forms [7 :value] 'Bool) bindings]]]
      (is (= "C7-VERIFY" (get-in (check bad-core bad-forms bad-bindings) [:diagnostics 0 :rule])))
      (is (= :rejected (:status (effects 'body-eh-source-products bad-core bad-forms bad-bindings 65536)))))
    (is (= "C7-GENERIC" (get-in (c7-invoke 'sh08-eh-source-products [core forms bindings 1]) [:diagnostics 0 :rule])))
    (let [plain (assoc core :nodes [(first (:nodes core))] :error-handlers [])
          result (check plain [] [])]
      (is (= 1 (:work-count result))))))

(deftest atomic-typed-handler-source-scope-integration
  (doseq [name ["atomic-catch-unresolved-type" "atomic-catch-protected-scope" "atomic-catch-sibling-scope" "atomic-catch-outside-scope"]
          extension [".gravity" ".qst"]]
    (let [failure (try (bootstrap/compiler-c5-resolution-file-artifact (path (fixture "rejected" name extension)))
                       nil (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= "C5-UNRESOLVED" (:id failure)) (pr-str failure)))))

(deftest atomic-typed-handler-downstream-boundary-development-kernel
  (let [emitter-path (path "bootstrap/gravity/p15_s23/emitter.gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter emitter-path (slurp emitter-path))
        lowered (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :typed-catch-downstream-boundary} compiled 'p15-s23-compile-expr
                  [emitter {:module 'example.try :profile :meta :target :jvm} '#{problem} '(try (throw problem) (catch String e e))])
        runtime-path (path "bootstrap/gravity/src/gravity/runtime.gravity")
        runtime-text (slurp runtime-path)
        runtime-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter runtime-path runtime-text)
        result (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :typed-catch-downstream-boundary} runtime-plan
                 'p15-s23-runtime-evaluate-closed-instruction
                 [{:op :try :body [{:op :println :args [{:op :literal :value "later"}] :effect :io/write :capability :io/stdout}]} {} ""])]
    (is (= :rejected (:status lowered)))
    (is (= :unauthenticated-b51-external-link (get-in lowered [:facts :reason])))
    (is (= (str "sha256:" (bootstrap/sha256-hex runtime-text)) (get-in runtime-plan [:source :sha256])))
    (is (= 22 (count (:functions runtime-plan))))
    (is (= ["" :rejected] (subvec result 1)))
    (is (= "P15S23X002" (get-in result [0 :diagnostic])))))

(deftest atomic-typed-handler-products-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        direct {:status :accepted :throws {:function [:gravity.type/string]} :constraints [] :diagnostics [] :work-count 0}
        products (c7-invoke 'sh08-tc-products [core types {} bindings direct 65536])
        source (c7-invoke 'sh08-eh-source-products [core forms bindings 65536])
        initial (c7-invoke 'sh08-eh-seed-bindings [{:problem :gravity.type/string} (:seeds source)])
        inferred (c7-invoke 'sh08-ft-infer-pass-with-context [(:nodes core) [] [] [] [] {} initial {}])]
    (is (= :accepted (:status products)) (pr-str products))
    (is (= [] (get-in products [:throws :function])))
    (is (= 1 (count (filter #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products)))))
    (is (= :gravity.type/string (get-in inferred [:type-table :handler]) (get-in inferred [:type-table :try])))
    (is (= :gravity.type/never (get-in inferred [:type-table :transfer])))
    (let [core (-> core
                   (update :nodes #(mapv (fn [node] (if (= :try (:node-id node)) (assoc node :children [:payload :handler]) node))
                                        (remove (fn [node] (= :transfer (:node-id node))) %)))
                   (assoc :error-transfers [])
                   (assoc-in [:error-handlers 0 :protected-core-node-id] :payload)
                   (assoc-in [:error-handlers 0 :candidate-error-transfers] []))
          products (c7-invoke 'sh08-tc-products [core (dissoc types :transfer) {} bindings (assoc direct :throws {}) 65536])]
      (is (= :accepted (:status products)) (pr-str products))
      (is (= [] (get-in products [:throws :function])))
      (is (= 1 (count (filter #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products))))))))

(deftest atomic-typed-handler-diagnostic-provenance-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        source (c7-invoke 'sh08-eh-source-products [core forms bindings 65536])
        span {:source "handler-source.gravity" :byte-start 21 :byte-end 99}
        origins [{:kind :macro-expansion :syntax-id :generated-parent}]
        generated {:kind :macro-expansion :from-syntax-id :source-try}
        context {:scope-id :original-try-scope :region :function-body}
        original (-> (nth (:nodes core) 3)
                     (assoc :binding-context context)
                     (assoc-in [:source :semantic-span] span)
                     (assoc-in [:source :origin-chain] origins)
                     (assoc-in [:source :generated-origin] generated))
        nodes (assoc (into {} (map (juxt :node-id identity) (:nodes core))) :try original)
        checked (c7-invoke 'sh08-eh-type-checks [(:groups source) (assoc types :transfer :gravity.type/string :handler :gravity.type/bool) nodes])
        diagnostic (first (:diagnostics checked))
        rendered (first (c7-invoke 'sh08-ft-render-inference-diagnostics [(:diagnostics checked) nodes (:module core)]))]
    (is (= original (:node diagnostic)))
    (is (= context (get-in diagnostic [:node :binding-context])))
    (is (= ["C7-TYPE-MISMATCH" :try-normal-result-type-mismatch :compatible-protected-and-handler-normal-results
            [:gravity.type/string :gravity.type/bool]] ((juxt :rule :reason :expected :actual) diagnostic)))
    (is (= :try (:syntax-id rendered)))
    (is (= span (:source-span rendered)))
    (is (= origins (:origin-chain rendered)))
    (is (= generated (:generated-origin rendered)))
    (is (= :string-type (:binding-id rendered)))
    (is (= (select-keys rendered [:syntax-id :source-span :origin-chain :generated-origin :binding-id :reason :rule :expected-type :actual-type])
           (select-keys (:diagnostic-id-request rendered) [:syntax-id :source-span :origin-chain :generated-origin :binding-id :reason :rule :expected-type :actual-type])))
    (is (= {:diagnostics []} (c7-invoke 'sh08-eh-type-checks [(:groups source) types nodes])))))

(deftest atomic-typed-handler-body-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        source (c7-invoke 'sh08-eh-source-products [core forms bindings 65536])
        independent (effects 'body-eh-source-products core forms bindings 65536)
        owners (c7-invoke 'sh08-th-owners [core 65536])
        derived (c7-invoke 'sh08-tc-graph [core types (:owners owners) #{:function} 65536 (:groups source)])
        rows (c7-invoke 'sh08-eh-constraints [core (:groups source) types (:nodes derived) []])
        facts {:type-table types :constraint-ledger rows :atomic-typed-handlers (:groups independent)}
        graph (reduce (fn [graph node]
                        (assoc graph (:node-id node)
                               (effects 'body-control-node-record node nil graph (get types (:node-id node)) facts {} core nil))) {} (:nodes core))
        request {:b47-context {:authenticated-core-request {:forms forms :binding-table bindings
                    :resolution-table [{:reference-syntax-id :type :position :type :symbol 'String :binding-id :string-type}
                                       {:reference-syntax-id :handler :position :value :symbol 'e :binding-id :caught}]}}}
        verify #(effects 'body-eh-verify request core %1 (:groups independent) %2 65536)]
    (is (= :accepted (:status derived)))
    (is (= [] (get-in derived [:nodes :try])))
    (is (= :accepted (:status (verify facts graph))) (pr-str (verify facts graph)))
    (is (= #{:error/throw} (get-in graph [:try :transitive]) (get-in graph [:try :escaping])))
    (is (= [] (get-in graph [:try :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [:try :covered-source-errors 0 :matched-error-types])))
    (doseq [mutate [(fn [f] (assoc f :constraint-ledger []))
                    (fn [f] (assoc-in f [:constraint-ledger 0 :solution :thrown-error-effects] [:gravity.type/bool]))
                    (fn [f] (assoc-in f [:type-table :handler] :gravity.type/bool))
                    (fn [f] (assoc-in f [:type-table :try] :gravity.type/never))]]
      (is (= :rejected (:status (verify (mutate facts) graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [:try :escaping-thrown-error-types] [:gravity.type/bool])))))
    (is (= :rejected (:status (effects 'body-eh-verify (assoc-in request [:b47-context :authenticated-core-request :resolution-table 0 :position] :value)
                              core facts (:groups independent) graph 65536))))
    (is (= :gravity.type/string (c7-invoke 'sh08-ft-normal-return-type [:try [:gravity.type/never :gravity.type/string] :gravity.type/unknown])))
    (is (= "C7-TYPE-MISMATCH" (get-in (c7-invoke 'sh08-eh-type-checks [(:groups source) (assoc types :transfer :gravity.type/string :handler :gravity.type/bool)
                                            (into {} (map (juxt :node-id identity) (:nodes core)))]) [:diagnostics 0 :rule])))))

(deftest atomic-typed-handler-partition-development-kernel
  (let [string :gravity.type/string bool :gravity.type/bool
        clauses [{:record :first :error-type string :handler-core-node-id :handler-a}
                 {:record :second :error-type string :handler-core-node-id :handler-b}
                 {:record :third :error-type bool :handler-core-node-id :handler-c}]
        errors {:handler-a [string] :handler-b [bool] :handler-c []}
        graph (into {} (map (fn [[id errors]] [id {:escaping-thrown-error-types errors}]) errors))
        check (fn [protected clauses]
                (let [typed (c7-invoke 'sh08-eh-partition [protected clauses errors])
                      effected (effects 'body-eh-partition protected clauses graph)]
                  (is (= typed effected)) typed))]
    (is (= [] (:residual (check [] clauses))))
    (let [result (check [string] clauses)]
      (is (= [string] (:residual result)))
      (is (= [[string] [] []] (mapv :matched-error-types (:handled result))))
      (is (= [[string] [] []] (mapv :handler-error-types (:handled result)))))
    (is (= [bool string] (:residual (check [string bool] (subvec clauses 0 2)))))
    (is (= [string] (:residual (check [string bool] clauses))))
    (is (= [] (:residual (check [string] [(assoc (first clauses) :handler-core-node-id :handler-c)])))))
  (let [core {:nodes [] :error-handlers []}]
    (is (= :accepted (:status (c7-invoke 'sh08-eh-source-products [core [] [] 65536]))))
    (is (= "C7-VERIFY" (get-in (c7-invoke 'sh08-eh-source-products [(assoc core :error-handlers [{}]) [] [] 65536]) [:diagnostics 0 :rule])))))

(deftest local-throw-final-signature-status-development-kernel
  (let [function {:parameters [{:binding-id :unused :type :gravity.type/unknown}]
                  :return :gravity.type/string :status :inferred}]
    (is (= :unresolved (:status (first (c7-invoke 'sh08-ft-final-functions [[function]])))))
    (is (= :inferred (:status (first (c7-invoke 'sh08-ft-final-functions
                                       [[(assoc-in function [:parameters 0 :type] :gravity.type/bool)]])))))))

(deftest local-throw-source-body-index-parity-development-kernel
  (let [{:keys [core types functions direct bindings]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct 65536])
        facts {:function-type-table (c7-invoke 'sh08-th-functions [(vec (vals functions)) (:throws result)])
               :type-table types :constraint-ledger (:constraints result)}
        [ho-core ho-facts _] ((body-helper 'callable-kernel-data))]
    ;; Compare the old unique-vector lookup with the indexed input, then rebuild
    ;; every record using the old lookup against the complete derived graph.
    (doseq [[core facts] [[core facts] [ho-core ho-facts]]]
      (let [table (:function-type-table facts) index (effects 'body-index table :function-id)
            edges (effects 'body-index (:call-edges core) :call-core-node-id)
            derived (effects 'body-derive-nodes-with-foreign core facts {} 65536) graph (:nodes derived)
            old-source #(get (effects 'body-by table :function-id (:callee-function-syntax-id %)) :body-core-node-id)
            indexed-source #(get-in index [(:callee-function-syntax-id %) :body-core-node-id])
            record (fn [node edge source]
                     (let [target (when (and (= :call (:core-form node))
                                             (effects 'body-call-invokes? node (:type-table facts)))
                                    (:body-core-node-id (effects 'body-selected-function edge facts index)))]
                       (effects 'body-control-node-record node target graph (get (:type-table facts) (:node-id node))
                                facts edge core source)))
            old-graph (into {} (map (fn [node] (let [id (:node-id node) edge (get edges id)]
                                               [id (record node edge (old-source edge))])) (:nodes core)))]
        (is (= :accepted (:status derived)))
        (is (= graph old-graph))
        (is (true? (effects 'body-graph-equations-valid? core facts {} old-graph)))
        (doseq [node (:nodes core)
                edge [(get edges (:node-id node)) {} {:callee-function-syntax-id :missing} nil]]
          (is (= (old-source edge) (indexed-source edge)))
          (is (= (record node edge (old-source edge)) (record node edge (indexed-source edge)))))))
    (is (nil? (effects 'body-index (conj (:function-type-table facts) (first (:function-type-table facts))) :function-id))))
  (let [module {:namespace 'example.body :profile :meta :target :jvm :effects [] :capabilities []}
        sha (body-helper 'sha) make (body-helper 'node)
        nodes [(make 1 :literal []) (make 2 :fn [1]) (make 3 :def [2])]
        core {:module module :nodes nodes :root-core-node-ids [(sha 3)] :definitions []
              :function-records [{:function-syntax-id (sha 12) :function-core-node-id (sha 2)}]
              :call-edges [] :calls [] :recursion-components [] :lexical-bindings [] :match-branch-records []
              :evaluation-order [{:core-node-id (sha 3) :index 0}]}
        core (assoc core :identity-preimage (select-keys core [:nodes :root-core-node-ids :definitions :function-records
                                                              :call-edges :calls :evaluation-order :lexical-bindings :match-branch-records]))
        facts {:module module :function-records (:function-records core) :call-edges [] :recursion-components [] :lexical-bindings []
               :function-type-table [{:function-id (sha 12) :parameters [] :parameter-binding-ids [] :latent-effects [] :capabilities []}]
               :type-table {(sha 1) :int (sha 2) :fn (sha 3) :fn} :local-binding-facts [] :call-type-facts []}]
    (is (true? (effects 'body-shape-valid? core facts)))
    (is (false? (effects 'body-shape-valid? core (update facts :function-type-table conj (first (:function-type-table facts))))))))

(defn- local-throw-loop-effect-data []
  ;; Current C7/C8 algorithms on normalized DATA, not source authentication.
  (let [[base base-facts ids] (effect-loop-data)
        extra (into {} (map-indexed (fn [i name] [name ((body-helper 'sha) (+ 1000 i))])
                           [:problem :operator :call :handler :try :caller :caller-definition :callee-binding :caller-binding]))
        ids (merge ids extra) callee (ids :function-target) caller (ids :caller)
        make (fn [name kind children attrs]
               {:node-id (ids name) :core-form kind :children (mapv ids children) :attributes attrs :resolved-binding-ids []
                :binding-context (if (= name :problem) :loop-body caller)
                :source {:form-id (ids name) :syntax-id (ids name) :semantic-span nil :origin-chain [] :generated-origin nil}
                :evaluation {:kind (if (= kind :fn) :function-creation :left-to-right)
                             :order (mapv (fn [i child] {:index i :core-node-id (ids child)})
                                          (range) (cond (= kind :fn) [] (= kind :try) [(first children)] :else children))}})
        problem (make :problem :literal [] {:literal-kind :string :value "loop source error"})
        stop (-> (first (filter #(= (ids :result) (:node-id %)) (:nodes base)))
                 (assoc :core-form :throw :children [(ids :problem)] :attributes {} :resolved-binding-ids [])
                 (assoc :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id (ids :problem)}]}))
        added [(make :operator :reference [] {:binding-id (ids :callee-binding)})
               (make :call :call [:operator] {}) (make :handler :literal [] {:literal-kind :string :value "handled"})
               (make :try :try [:call :handler] {})
               (make :caller :fn [:try] {:recur-target-id caller})
               (make :caller-definition :def [:caller] {})]
        core (-> base
                 (update :nodes #(into (vec (mapcat (fn [node] (if (= (ids :result) (:node-id node)) [problem stop] [node])) %)) added))
                 (assoc :root-core-node-ids [(ids :definition) (ids :caller-definition)]
                        :module {:profile :meta :target :jvm :effects [:error/throw] :capabilities []}
                        :function-records [{:function-syntax-id callee :function-core-node-id callee :body-core-node-id (ids :loop-target)
                                            :definition-binding-id (ids :callee-binding) :fixed-arity 0 :parameter-binding-ids []}
                                           {:function-syntax-id caller :function-core-node-id caller :body-core-node-id (ids :try)
                                            :definition-binding-id (ids :caller-binding) :fixed-arity 0 :parameter-binding-ids []}]
                        :definitions [] :recursion-components []
                        :calls [{:core-node-id (ids :call) :operator-node-id (ids :operator) :operator-binding-id (ids :callee-binding)
                                 :argument-node-ids [] :ordered-evaluation-node-ids [(ids :operator)]
                                 :evaluation-order :operator-then-arguments :result-policy :call-result}]
                        :call-edges [{:ordinal 0 :call-core-node-id (ids :call) :caller-function-syntax-id caller :caller-function-core-node-id caller
                                      :callee-binding-id (ids :callee-binding) :callee-definition-syntax-id callee
                                      :callee-function-syntax-id callee :callee-function-core-node-id callee :argument-core-node-ids []
                                      :ordered-evaluation-node-ids [(ids :operator)] :evaluation-order :operator-then-arguments :classification :local-function}]
                        :error-transfers [{:core-node-id (ids :result) :value-core-node-id (ids :problem)}])
                 (update :recur-targets conj {:target-id caller :target-kind :function :owner-core-node-id caller
                                             :owner-form-id caller :owner-syntax-id caller :arity 0 :binding-ids []
                                             :body-scope-id caller :parent-target-id nil :type-compatibility :pending-sh08}))
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        types (merge (:type-table base-facts)
                     {(ids :problem) :gravity.type/string (ids :result) :gravity.type/never
                      (ids :branch) :gravity.type/never (ids :loop-target) :gravity.type/never
                      (ids :operator) :function (ids :call) :gravity.type/never (ids :handler) :gravity.type/string
                      (ids :try) :gravity.type/string caller :function (ids :caller-definition) :function})
        table [{:function-id callee :function-core-node-id callee :body-core-node-id (ids :loop-target)
                :parameters [] :return :gravity.type/never :fixed-arity 0 :latent-effects [:error/throw] :capabilities [] :thrown-error-effects [:gravity.type/string]}
               {:function-id caller :function-core-node-id caller :body-core-node-id (ids :try)
                :parameters [] :return :gravity.type/string :fixed-arity 0 :latent-effects [:error/throw] :capabilities [] :thrown-error-effects []}]
        bindings {(ids :callee-binding) {:binding-id (ids :callee-binding)}}
        groups {(ids :try) [{:record :string-catch :error-type :gravity.type/string :handler-core-node-id (ids :handler)}]}
        owners (effects 'body-throw-owners core 65536)
        scoped (effects 'body-tc-scope core {callee [:gravity.type/string]} 65536)
        joined (assoc scoped :direct-throws {callee [:gravity.type/string]} :owners (:owners owners))
        source (c7-invoke 'sh08-tc-graph [core types (:owners owners) (:scope scoped) 65536 groups])
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {} 65536])
        loop-rows (mapv (fn [premise] (effects 'body-loop-row premise (get nodes (or (:initializer-node-id premise)
                                                                                 (get-in premise [:source-transfer :recur-core-node-id]))) (:module core)))
                        (:constraints checked))
        call-row (c7-invoke 'sh08-tc-constraint [core (first (:call-edges core)) (into {} (map (juxt :core-node-id identity) (:calls core)))
                                               nodes (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
                                               bindings types (into {} (map (juxt :function-id identity) table)) (:nodes source)])
        facts {:module (:module core) :type-table types :function-type-table table
               :constraint-ledger (conj loop-rows call-row) :atomic-typed-handlers groups}]
    {:core core :facts facts :bindings bindings :joined joined :ids ids :checked checked :source source}))

(deftest local-throw-call-effect-equations-development-kernel
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct 65536])
        function-table (c7-invoke 'sh08-th-functions [(vec (vals functions)) (:throws result)])
        facts {:function-type-table function-table :type-table types :constraint-ledger (:constraints result)}
        scoped (effects 'body-tc-scope core (:throws direct) 65536)
        owners (effects 'body-throw-owners core 65536)
        joined (assoc scoped :direct-throws (:throws direct) :owners (:owners owners))
        derived (effects 'body-derive-nodes core facts) graph (:nodes derived)
        verify #(effects 'body-tc-verify core % (into {} (map (juxt :binding-id identity) bindings)) %2 joined 65536)]
    (is (= :accepted (:status derived)))
    (is (not (contains? (:scope scoped) (fid 6))))
    (is (= :accepted (:status (verify facts graph))))
    (is (= [] (get-in (verify facts graph) [:functions (fid 7)])))
    (let [pure-index (first (keep-indexed #(when (= (fid 7) (:function-id %2)) %1) function-table))]
      (is (= :rejected (:status (verify (assoc-in facts [:function-type-table pure-index :thrown-error-effects] [:pending-sh09]) graph)))))
    (is (= [:gravity.type/string] (get-in graph [(id 12) :escaping-thrown-error-types])))
    (is (= #{} (get-in graph [(id 12) :direct-thrown-errors])))
    (is (= [:gravity.type/string] (get-in graph [(id 10) :escaping-thrown-error-types])))
    (is (nil? (get-in graph [(id 10) :call-body-dependency])))
    (is (false? (get-in graph [(id 10) :throw-call-dependency :normal-invocation?])))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (update facts :constraint-ledger conj (first (:constraint-ledger facts)))
                 (assoc-in facts [:constraint-ledger 0 :dependencies :callee-source-body-id] :forged)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [:gravity.type/integer])]]
      (is (= :rejected (:status (verify bad graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [(id 12) :escaping-thrown-error-types] [])))))
    (let [node {:core-form :call :children [:operator :argument]} graph {:operator [] :argument [:gravity.type/string :gravity.type/bool]
                                                                       :target [:gravity.type/string :gravity.type/integer]}]
      (is (= [:gravity.type/string :gravity.type/bool :gravity.type/integer]
             (c7-invoke 'sh08-tc-node-errors [node graph :target {:argument :gravity.type/string} {}])))
      (is (= [:gravity.type/string :gravity.type/bool]
             (c7-invoke 'sh08-tc-node-errors [node graph nil {:argument :gravity.type/never} {}])))))
  (let [{:keys [core facts bindings joined ids checked source]} (local-throw-loop-effect-data)
        derive #(effects 'body-derive-module-nodes %1 %2 {} 65536)
        derived (derive core facts) graph (:nodes derived)
        verify #(effects 'body-tc-verify %1 %2 bindings %3 joined 65536)
        replace-node (fn [c id f] (update c :nodes #(mapv (fn [node] (if (= id (:node-id node)) (f node) node)) %)))
        reseal (fn [c] (assoc c :identity-preimage (select-keys c [:loop-bindings :recur-targets :recur-transfers])))]
    (is (= :accepted (:status checked) (:status source) (:status derived)) (pr-str {:checked checked :source source :derived derived}))
    (is (true? (:recur-control? derived)))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status (effects 'body-loop-joins core facts 65536))))
    (is (= :accepted (:status (verify core facts graph))))
    (is (= {(:function-target ids) [:gravity.type/string] (:caller ids) []} (:functions (verify core facts graph))))
    (doseq [name [:result :branch :loop-target :call]]
      (is (= [:gravity.type/string] (get-in graph [(ids name) :escaping-thrown-error-types]))))
    (is (= [] (effects 'body-error-types graph (ids :transfer))))
    (is (= [] (get-in graph [(ids :try) :escaping-thrown-error-types])))
    (is (= #{:error/throw} (get-in graph [(ids :try) :transitive]) (get-in graph [(ids :transfer) :transitive])))
    (is (= (first (:recur-transfers core)) (get-in graph [(ids :transfer) :loop-backedge :source-transfer])))
    (when (= :accepted (:status derived))
      (is (= derived (effects 'body-derive-module-nodes core facts {} (:work-count derived))))
      (is (= :body-deferred-work-limit (:reason (effects 'body-derive-module-nodes core facts {} (dec (:work-count derived)))))))
    (doseq [bad [(assoc-in facts [:constraint-ledger (dec (count (:constraint-ledger facts))) :dependencies :callee-error-types] [])
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])
                 (assoc-in facts [:function-type-table 1 :thrown-error-effects] [:gravity.type/string])]]
      (is (= :rejected (:status (verify core bad graph)))))
    (doseq [name [:loop-target :call]]
      (is (= :rejected (:status (verify core facts (assoc-in graph [(ids name) :escaping-thrown-error-types] []))))))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                        (assoc-in graph [(ids :result) :escaping-thrown-error-types] []))))
    (doseq [bad [(assoc-in core [:recur-transfers 0 :argument-node-ids] [(ids :next-value) (ids :next-again)])
                 (assoc-in core [:recur-transfers 0 :target-id] (ids :function-target))
                 (assoc-in core [:recur-targets 0 :binding-ids] [(ids :value) (ids :again)])]]
      (is (= :rejected (:status (derive (reseal bad) facts)))))
    (let [bad (replace-node core (ids :problem) #(assoc % :core-form :set!))]
      (is (= :rejected (:status (derive bad facts))))
      (is (= :unsupported-throwing-call-component (:reason (verify bad facts graph)))))))

(defn- local-throw-change-summary [typed function-id errors]
  (-> typed
      (update :function-type-table #(mapv (fn [f] (if (= function-id (:function-id f)) (assoc f :thrown-error-effects errors) f)) %))
      (update :type-table #(into {} (map (fn [[id type]] [id (if (and (map? type) (= function-id (:function-id type)))
                                                                            (assoc type :thrown-error-effects errors) type)])) %))))

(deftest atomic-typed-catch-authenticated-paired-composition
    (doseq [extension [".gravity" ".qst"]]
      (let [relative (fixture "accepted" "atomic-typed-catch" extension)
            p ((body-helper 'fresh-products) relative) typed (:typed p)
            core (get-in p [:request :canonical-core-artifact])
            result ((body-helper 'build-leaf) p) graph (:effect-graph result)
            functions (into {} (map (juxt :definition-name identity) (:function-type-table typed)))
            rows (filterv #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraint-ledger typed))]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Atomic typed catch composition failed" {:typed (:diagnostics typed) :effects (:diagnostics result)})))
        (is (= 8 (count rows)))
        (is (= 10 (count (:error-handlers core))))
        (doseq [[name errors] [['recover []] ['partial [:gravity.type/bool]] ['nested []]
                              ['ordered [:gravity.type/string]] ['rethrow [:gravity.type/string]] ['bypass []] ['call-catch []]]]
          (let [function (functions name)]
            (is (= :gravity.type/string (:return function)))
            (is (= errors (:thrown-error-effects function)))
            (is (= errors (get-in graph [:functions (:function-id function) :throws])))))
        (doseq [row rows]
          (let [id (get-in row [:lhs :core-node-id])]
            (is (= (:rhs row) (get-in graph [:nodes id :escaping-thrown-error-types])))
            (is (= (get-in row [:solution :normal-result-type]) (get-in typed [:type-table id])))
            (is (= (get-in row [:solution :handled-source-errors]) (get-in graph [:nodes id :covered-source-errors])))
            (is (= #{:error/throw} (get-in graph [:nodes id :transitive]) (get-in graph [:nodes id :escaping])))
            (is (= :pending-profile-handler-capability-authority (get-in graph [:nodes id :effect-label-discharge])))))
        (doseq [record (:error-handlers core)
                syntax (:catch-binding-use-syntax-ids record)]
          (let [reference (first (filter #(= syntax (get-in % [:source :syntax-id])) (:nodes core)))
                binding (first (filter #(= (:error-type-binding-id record) (:binding-id %))
                                      (get-in p [:request :b47-context :authenticated-core-request :binding-table])))
                expected (c7-invoke 'sh08-ft-atomic-type-id [(:name binding)])]
            (is (= expected (get-in typed [:type-table (:node-id reference)])))))
        (let [ordered-id (:body-core-node-id (functions 'ordered))
              index (first (keep-indexed #(when (= ordered-id (get-in %2 [:lhs :core-node-id])) %1) (:constraint-ledger typed)))
              ordered-row (get (:constraint-ledger typed) index)
              recover-id (:body-core-node-id (functions 'recover))
              recover-index (first (keep-indexed #(when (= recover-id (get-in %2 [:lhs :core-node-id])) %1) (:constraint-ledger typed)))
              reference-id (:node-id (first (filter #(= (first (:catch-binding-use-syntax-ids (first (:error-handlers core))))
                                                              (get-in % [:source :syntax-id])) (:nodes core))))
              mutations [#(assoc-in % [:constraint-ledger index :lhs :core-node-id] recover-id)
                         #(assoc-in % [:constraint-ledger index :dependencies :catch-types 0] :gravity.type/bool)
                         #(assoc-in % [:constraint-ledger index :dependencies :source-handlers 0 :catch-binding-id] :forged)
                         #(assoc-in % [:constraint-ledger index :dependencies :source-handlers 0 :handler-child-index] 0)
                         #(update-in % [:constraint-ledger index :dependencies :source-handlers] (comp vec reverse))
                         #(assoc-in % [:constraint-ledger index :dependencies :partition :handled] [])
                         #(local-throw-change-summary % (:function-id (functions 'partial)) [])
                         #(local-throw-change-summary % (:function-id (functions 'rethrow)) [])
                         #(assoc-in % [:type-table reference-id] :gravity.type/bool)
                         #(assoc-in % [:constraint-ledger recover-index :solution :normal-result-type] :gravity.type/never)]]
          (is (= [[:gravity.type/string] [:gravity.type/bool] []]
                 (mapv :matched-error-types (get-in ordered-row [:solution :handled-source-errors]))))
          (is (= [:gravity.type/string] (:rhs ordered-row)))
          (doseq [mutate mutations]
            (let [coherent ((body-helper 'coherent-typed-mutation) p mutate)]
              (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) (:typed coherent)]))))
              (is (= :rejected (:status ((body-helper 'build-leaf) coherent))))))
          (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
          (doseq [bad [(assoc-in graph [:nodes recover-id :transitive] #{})
                       (assoc-in graph [:nodes recover-id :escaping] #{})
                       (assoc-in graph [:nodes ordered-id :escaping-thrown-error-types] [])
                       (assoc-in graph [:nodes ordered-id :covered-source-errors] [])]]
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p)
                                       ((body-helper 'reseal-effect-graph) result bad)))))))
        ))
    (doseq [extension [".gravity" ".qst"]]
      (doseq [[name rule reason] [["atomic-catch-result-type" "C7-TYPE-MISMATCH" :try-normal-result-type-mismatch]
                                ["atomic-catch-unsupported-type" "C7-ANNOTATION" :unsupported-atomic-catch-type]]]
        (let [p ((body-helper 'fresh-products) (fixture "rejected" name extension))]
          (is (= :rejected (get-in p [:typed :status])))
          (is (= [rule reason] ((juxt :rule :reason) (first (get-in p [:typed :diagnostics])))))))
      (let [p ((body-helper 'fresh-products) (fixture "rejected" "atomic-catch-undeclared" extension))
            result ((body-helper 'build-leaf) p)]
        (is (= :accepted (get-in p [:typed :status])))
        (is (= :passed (get-in p [:verification :status])))
        (is (= :rejected (:status result)))
        (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])))))
    (doseq [[disposition name] [["accepted" "atomic-typed-catch"] ["rejected" "atomic-catch-result-type"]
                              ["rejected" "atomic-catch-unsupported-type"] ["rejected" "atomic-catch-undeclared"]]]
      (is (= (slurp (path (fixture disposition name ".gravity"))) (slurp (path (fixture disposition name ".qst")))))))

(deftest local-throw-propagation-authenticated-paired-composition
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (fixture "accepted" "local-throw-propagation" extension)
          p ((body-helper 'fresh-products) relative) typed (:typed p)
          core (get-in p [:request :canonical-core-artifact]) result ((body-helper 'build-leaf) p) graph (:effect-graph result)
          functions (into {} (map (juxt :definition-name identity) (:function-type-table typed)))
          rows (filterv #(= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule %)) (:constraint-ledger typed))]
      (is (= :passed (:status (:report p)) (:status (:verification p))))
      (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
      (when-not (= :accepted (:status typed) (:status result))
        (throw (ex-info "Local typed throw propagation failed" {:typed-diagnostics (:diagnostics typed) :effects-diagnostics (:diagnostics result)})))
      (doseq [name '[choose relay relay2 abort]]
        (let [function (functions name)]
          (is (= :gravity.type/string (:return function)))
          (is (= [:gravity.type/string] (:thrown-error-effects function)))
          (is (= [:gravity.type/string] (get-in graph [:functions (:function-id function) :throws])))))
      (is (= [:gravity.type/bool] (:thrown-error-effects (functions 'bool-choice))))
      (is (= [] (:thrown-error-effects (functions 'pure-value))))
      (is (= [] (get-in graph [:functions (:function-id (functions 'pure-value)) :throws])))
      (is (= 5 (count rows)))
      (is (= #{} (get-in graph [:namespace :inferred])))
      (doseq [function (vals functions)]
        (is (= #{} (get-in graph [:nodes (:function-core-node-id function) :transitive]))))
      (let [abort-id (:body-core-node-id (functions 'abort))
            relay-id (:body-core-node-id (functions 'relay2))
            row-index (first (keep-indexed #(when (and (= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule %2))
                                                       (= abort-id (get-in %2 [:lhs :core-node-id]))) %1) (:constraint-ledger typed)))
            row (get (:constraint-ledger typed) row-index)]
        (is (= :gravity.type/never (get (:type-table typed) abort-id)))
        (is (= :gravity.type/never (get-in row [:solution :normal-result-type])))
        (is (false? (get-in row [:dependencies :normal-invocation?])))
        (is (= [] (get-in row [:dependencies :callee-error-types])))
        (is (= [:gravity.type/string] (:rhs row)))
        (is (nil? (get-in graph [:nodes abort-id :call-body-dependency])))
        (is (false? (get-in graph [:nodes abort-id :throw-call-dependency :normal-invocation?])))
        (is (= [:gravity.type/string] (get-in graph [:nodes abort-id :escaping-thrown-error-types])))
        (is (= #{} (get-in graph [:nodes relay-id :direct-thrown-errors])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (doseq [bad [(local-throw-change-summary typed (:function-id (functions 'relay2)) [])
                     (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule row))) %))
                     (assoc-in typed [:constraint-ledger row-index :dependencies :callee-source-body-id] relay-id)
                     (assoc-in typed [:constraint-ledger row-index :dependencies :source-edge :callee-binding-id] :forged)
                     (update-in typed [:constraint-ledger row-index :dependencies :source-edge :ordered-evaluation-node-ids] #(vec (reverse %)))]]
          (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) bad])))))
        (doseq [mutate [#(local-throw-change-summary % (:function-id (functions 'relay2)) [])
                        #(local-throw-change-summary % (:function-id (functions 'pure-value)) [:pending-sh09])
                        #(update % :constraint-ledger (fn [rows] (filterv (fn [row] (not= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule row))) rows)))
                        #(assoc-in % [:constraint-ledger row-index :dependencies :callee-source-body-id] relay-id)
                        #(assoc-in % [:constraint-ledger row-index :dependencies :source-edge :callee-binding-id] :forged)]]
          (is (= :rejected (:status ((body-helper 'build-leaf) ((body-helper 'coherent-typed-mutation) p mutate))))))
        (doseq [bad-graph [(assoc-in graph [:nodes relay-id :call-body-dependency] nil)
                           (assoc-in graph [:nodes relay-id :escaping-thrown-error-types] [])
                           (assoc-in graph [:nodes abort-id :throw-call-dependency :normal-invocation?] true)
                           (assoc-in graph [:functions (:function-id (functions 'pure-value)) :throws] [:pending-sh09])]]
          (let [stale (assoc result :effect-graph bad-graph)
                coherent ((body-helper 'reseal-effect-graph) result bad-graph)]
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) stale))))
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) coherent)))))))
      ;; Seed observations reuse these accepted bytes and products; no second
      ;; compiler composition is constructed for the execution observations.
      (let [source (path relative) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            transport (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw!) original (var-get transport)
            token (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
            payload (String. "problem") seen (atom [])
            escape (fn [name args] (try (bootstrap/p15-s23-stage2-runtime-execute-function
                                         {:engine :local-typed-throw-seed-observation} compiled name args)
                                       nil (catch clojure.lang.ExceptionInfo error error)))
            [normal thrown aborted]
            (with-redefs-fn {transport (fn [active-plan value] (swap! seen conj value) (original active-plan value))}
              #(vector (bootstrap/p15-s23-stage2-runtime-execute-function
                         {:engine :local-typed-throw-seed-observation} compiled 'relay2 [false payload])
                       (escape 'relay2 [true payload]) (escape 'abort [])))]
        (is (= "8649cc5e1742c00befa785f740840ce2e73ec917028c03f1d4243780659a65a2" (bootstrap/sha256-hex text)))
        (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
        (is (identical? payload normal))
        (is (identical? payload (:payload (ex-data thrown))))
        (is (= "argument" (:payload (ex-data aborted))))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data thrown))))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data aborted))))
        (is (= [payload "argument"] @seen))))
    (doseq [[name rule] [["local-throw-argument-type" "C7-TYPE-MISMATCH"] ["local-throw-result-type" "C7-TYPE-MISMATCH"]
                         ["local-throw-unknown-slot" "C7-GENERIC"]]]
      (let [p ((body-helper 'fresh-products) (fixture "rejected" name extension))]
        (is (= :rejected (get-in p [:typed :status])))
        (is (= rule (get-in p [:typed :diagnostics 0 :rule])) (pr-str (get-in p [:typed :diagnostics])))
        (when (= name "local-throw-unknown-slot")
          (is (= :unresolved-function-type (get-in p [:typed :diagnostics 0 :reason]))))))
    (let [p ((body-helper 'fresh-products) (fixture "rejected" "local-throw-undeclared" extension))
          result ((body-helper 'build-leaf) p)]
      (is (= :accepted (get-in p [:typed :status])))
      (is (= :passed (get-in p [:verification :status])))
      (is (= :rejected (:status result)))
      (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])) (pr-str (:diagnostics result)))))
  (doseq [[disposition name] [["accepted" "local-throw-propagation"] ["rejected" "local-throw-argument-type"]
                              ["rejected" "local-throw-result-type"] ["rejected" "local-throw-unknown-slot"] ["rejected" "local-throw-undeclared"]]]
    (is (= (slurp (path (fixture disposition name ".gravity"))) (slurp (path (fixture disposition name ".qst")))))))

(deftest direct-typed-throw-authenticated-paired-composition
  ;; Actual source/compiler products; no seed execution or host exception claim.
  (doseq [extension [".gravity" ".qst"]]
    (let [p ((body-helper 'fresh-products) (fixture "accepted" "direct-typed-throw" extension))
          typed (:typed p) core (get-in p [:request :canonical-core-artifact])
          transfer (first (:error-transfers core)) id (:core-node-id transfer) payload (:value-core-node-id transfer)
          rows (filterv #(= :sh08-direct-typed-throw-v1 (:producer-rule %)) (:constraint-ledger typed))
          result ((body-helper 'build-leaf) p) graph (:effect-graph result)
          function (first (:function-type-table typed))]
      (is (= :passed (:status (:report p)) (:status (:verification p))))
      (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
      (when-not (= :accepted (:status typed) (:status result))
        (throw (ex-info "Direct typed throw composition failed" {:typed typed :result result})))
      (is (= :gravity.type/string (get (:type-table typed) payload) (:return function)))
      (is (= :gravity.type/never (get (:type-table typed) id)))
      (is (= [:gravity.type/string] (:thrown-error-effects function)))
      (is (= 1 (count rows)))
      (is (= transfer (get-in (first rows) [:dependencies :source-transfer])))
      (is (= #{:error/throw} (get-in graph [:nodes id :direct]) (get-in graph [:nodes id :escaping])
             (get-in graph [:functions (:function-id function) :inferred])))
      (is (= #{:gravity.type/string} (get-in graph [:nodes id :direct-thrown-errors])
             (get-in graph [:nodes id :escaping-thrown-errors])))
      (is (= [:gravity.type/string] (get-in graph [:functions (:function-id function) :throws])))
      (is (= [:evaluate-value :transfer-error] (get-in graph [:nodes id :error-transfer :ordered-steps])))
      (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
      (doseq [bad [(assoc-in typed [:type-table payload] :gravity.type/bool)
                   (assoc typed :constraint-ledger (filterv #(not= :sh08-direct-typed-throw-v1 (:producer-rule %)) (:constraint-ledger typed)))
                   (assoc-in typed [:function-type-table 0 :thrown-error-effects] [])]]
        (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) bad])))))
      (doseq [bad [(assoc-in result [:effect-graph :nodes id :direct] #{})
                   (assoc-in result [:effect-graph :nodes id :escaping-thrown-errors] #{:gravity.type/bool})
                   (assoc-in result [:effect-graph :nodes id :error-transfer :ordered-steps] [:transfer-error :evaluate-value])
                   (assoc-in result [:effect-graph :functions (:function-id function) :throws] [])]]
        (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) bad)))))
      (doseq [mutate [#(assoc-in % [:function-type-table 0 :thrown-error-effects] [])
                      #(assoc % :constraint-ledger (filterv (fn [row] (not= :sh08-direct-typed-throw-v1 (:producer-rule row))) (:constraint-ledger %)))]]
        (is (= :rejected (:status ((body-helper 'build-leaf) ((body-helper 'coherent-typed-mutation) p mutate)))))))
    (doseq [[basename expected] [["direct-throw-unknown-payload" "C7-ANNOTATION"]
                                 ["direct-throw-result-type" "C7-TYPE-MISMATCH"]]]
      (let [p ((body-helper 'fresh-products) (fixture "rejected" basename extension))]
        (is (= :rejected (get-in p [:typed :status])))
        (is (= expected (get-in p [:typed :diagnostics 0 :rule])) (pr-str (get-in p [:typed :diagnostics])))))
    (let [p ((body-helper 'fresh-products) (fixture "rejected" "direct-throw-undeclared" extension))
          result ((body-helper 'build-leaf) p)]
      (is (= :accepted (get-in p [:typed :status])))
      (is (= :passed (get-in p [:verification :status])))
      (is (= :rejected (:status result)))
      (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])) (pr-str (:diagnostics result)))))
  (doseq [[disposition basename] [["accepted" "direct-typed-throw"] ["rejected" "direct-throw-unknown-payload"]
                                  ["rejected" "direct-throw-result-type"] ["rejected" "direct-throw-undeclared"]]]
    (is (= (slurp (path (fixture disposition basename ".gravity")))
           (slurp (path (fixture disposition basename ".qst")))))))

(deftest direct-typed-throw-annotated-seed-execution
  ;; Reuse completed paired C5-C8 typing/body-effect/source-allowance evidence for these exact
  ;; bytes. This observes the temporary single-source Clojure seed, not a JVM ABI.
  (let [transport (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw!)
        token (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
        original (var-get transport)
        expected "755e5b876b8fde5f1be48b0dce2cea96c17da24d967b4d9e641c720a46617f23"
        gravity (slurp (path (fixture "accepted" "direct-typed-throw" ".gravity")))]
    (doseq [extension [".gravity" ".qst"]]
      (let [source (path (fixture "accepted" "direct-typed-throw" extension)) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            payload (String. "problem") seen (atom [])
            [normal failure]
            (with-redefs-fn {transport (fn [active-plan value] (swap! seen conj value) (original active-plan value))}
              #(let [normal (bootstrap/p15-s23-stage2-runtime-execute-function
                              {:engine :direct-typed-throw-annotated-seed-observation} compiled 'choose [false payload])
                     failure (try (bootstrap/p15-s23-stage2-runtime-execute-function
                                    {:engine :direct-typed-throw-annotated-seed-observation} compiled 'choose [true payload])
                                  nil (catch clojure.lang.ExceptionInfo error error))]
                 [normal failure]))]
        (is (= gravity text))
        (is (= expected (bootstrap/sha256-hex text)))
        (is (= (str "sha256:" expected) (get-in compiled [:source :sha256])))
        (is (= {:parameters '[flag :- Bool problem :- String] :return '[typed/return (quote String)]}
               (get-in compiled [:functions 'choose :source-declarations])))
        (is (= '[flag problem] (get-in compiled [:functions 'choose :params])))
        (is (identical? payload normal))
        (is (instance? clojure.lang.ExceptionInfo failure))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data failure))))
        (is (identical? payload (:payload (ex-data failure))))
        (is (= [payload] @seen))
        (is (= (:source compiled) (:source (ex-data failure))))
        (is (= :clojure-stage0-seed (get-in compiled [:compiler :owner])))
        (is (= :temporary-single-source-seed-throw (get-in compiled [:compiler :lowering-boundary])))
        (is (= :pending-seed-effect-inference (get-in compiled [:effect-summary :inferred])))))))

(deftest function-recur-annotated-seed-execution
  ;; Legality is covered by function-recur-authenticated-paired-composition
  ;; over these exact files. This selector executes their unchanged bytes.
  (let [gravity (slurp (path (fixture "accepted" "function-recur-types" ".gravity")))
        qst (slurp (path (fixture "accepted" "function-recur-types" ".qst")))]
    (is (= gravity qst))
    (is (= "c9068acec85998d8dcba1b22e165b66ca9e73f5fae04f17229611ed6ad1d63f9"
           (bootstrap/sha256-hex gravity)))
    (doseq [extension [".gravity" ".qst"]]
      (let [source (path (fixture "accepted" "function-recur-types" extension)) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            observed (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
            value (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                               (fn [values] (swap! observed conj values) (original values))]
                    (bootstrap/p15-s23-stage2-runtime-execute-function
                     {:engine :function-recur-explicit-annotated-seed-interpreter} compiled 'run []))]
        (is (= text gravity))
        (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
        (is (= {:parameters '[flag :- Bool value :- String] :return '[typed/return (quote String)]}
               (get-in compiled [:functions 'again :source-declarations])))
        (is (= '[flag value] (get-in compiled [:functions 'again :params])))
        (is (= "done" value))
        (is (= [[false "done"]] @observed))))))

(defn- function-recur-data []
  (let [signature {:parameters [{:type-id :gravity.type/bool} {:type-id :gravity.type/string}]
                   :return {:type-id :gravity.type/string}}
        nodes [(node :condition :reference [] :function-body {:binding-id :flag})
               (node :next-flag :literal [] :function-body {})
               (node :next-value :reference [] :function-body {:binding-id :value})
               (-> (node :transfer :recur [:next-flag :next-value] :function-body
                         {:target-id :function-target :target-kind :function :arity 2 :type-compatibility :pending-sh08})
                   (assoc :resolved-binding-ids [:flag :value])
                   (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
               (node :result :reference [] :function-body {:binding-id :value})
               (node :branch :if [:condition :transfer :result] :function-body {})
               (assoc (node :function-target :fn [:branch] :namespace
                            {:parameters ['flag 'value] :recur-target-id :function-target :signature signature})
                      :resolved-binding-ids [:flag :value])]
        target {:target-id :function-target :target-kind :function :owner-core-node-id :function-target
                :owner-form-id :function-target-form :owner-syntax-id :function-target :arity 2
                :binding-ids [:flag :value] :body-scope-id :function-body :parent-target-id nil :type-compatibility :pending-sh08}
        transfer {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                  :target-id :function-target :target-kind :function :argument-node-ids [:next-flag :next-value] :arity 2
                  :tail-position true :evaluation-order :arguments-left-to-right
                  :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        core {:nodes nodes :loop-bindings [] :recur-targets [target] :recur-transfers [transfer]
              :binding-table [{:binding-id :flag :name 'flag} {:binding-id :value :name 'value}]}
        table {:condition :gravity.type/bool :next-flag :gravity.type/bool :next-value :gravity.type/string
               :transfer :gravity.type/never :result :gravity.type/string :branch :gravity.type/string}]
    [core table]))

(deftest loop-lexical-source-error-development-kernel
  ;; The actual validating loop kernel runs before the actual source-error
  ;; graph here, as in public C7. These normalized premises confer no authority.
  (let [[base table] (loop-data)
        locations [[:value-init :initializers :gravity.type/string]
                   [:next-value :loop-body :gravity.type/bool]
                   [:result :loop-body :gravity.type/keyword]]
        branches (into {} (map (fn [[id scope _]]
                                 [id (node id :if [(keyword (str (name id) "-test"))
                                                   (keyword (str (name id) "-throw"))
                                                   (keyword (str (name id) "-normal"))] scope {})]) locations))
        additions (mapcat (fn [[id scope _]]
                            [(node (keyword (str (name id) "-test")) :literal [] scope {})
                             (node (keyword (str (name id) "-payload")) :literal [] scope {})
                             (node (keyword (str (name id) "-throw")) :throw [(keyword (str (name id) "-payload"))] scope {})
                             (node (keyword (str (name id) "-normal")) :literal [] scope {})]) locations)
        core (assoc base :nodes (into (vec additions) (map #(get branches (:node-id %) %) (:nodes base))))
        types (reduce (fn [out [id _ payload-type]]
                        (assoc out (keyword (str (name id) "-test")) :gravity.type/bool
                                   (keyword (str (name id) "-payload")) payload-type
                                   (keyword (str (name id) "-throw")) :gravity.type/never
                                   (keyword (str (name id) "-normal")) :gravity.type/integer)) table locations)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {} 65536])
        owners (zipmap (map :node-id (:nodes core)) (repeat :function-target))
        derive #(c7-invoke 'sh08-tc-graph [%1 %2 owners #{:function-target} %3 {}])
        graph (derive core types 65536)]
    (is (= :accepted (:status checked)))
    (is (= true (:normal-transfer? (last (:constraints checked)))))
    (is (= :accepted (:status graph)))
    (is (= [:gravity.type/bool] (get-in graph [:nodes :transfer])))
    (is (= [:gravity.type/string :gravity.type/bool :gravity.type/keyword] (get-in graph [:nodes :loop-target])))
    (is (= [] (get-in graph [:nodes :function-target])))
    (is (not (some #{:gravity.type/never} (mapcat val (:nodes graph)))))
    (let [never-core (update base :nodes
                       #(into [(node :inner-problem :literal [] :loop-body {})
                               (node :inner-throw :throw [:inner-problem] :loop-body {})]
                          (map (fn [n] (if (= :next-value (:node-id n))
                                         (node :next-value :throw [:inner-throw] :loop-body {}) n)) %)))
          never-types (assoc table :next-value :gravity.type/never :inner-problem :gravity.type/string :inner-throw :gravity.type/never)
          never (c7-invoke 'sh08-ft-loop-type-step [never-core never-types {} 65536])
          errors (c7-invoke 'sh08-tc-graph [never-core never-types
                    (zipmap (map :node-id (:nodes never-core)) (repeat :function-target)) #{:function-target} 65536 {}])]
      (is (= :accepted (:status never)))
      (is (= false (:normal-transfer? (last (:constraints never)))))
      (is (= [:gravity.type/string] (get-in errors [:nodes :transfer])))
      (is (= [:gravity.type/string] (get-in errors [:nodes :loop-target])))
      (is (not (some #{:gravity.type/never} (mapcat val (:nodes errors))))))
    (doseq [bad [(assoc-in core [:recur-transfers 0 :target-id] :function-target)
                 (assoc-in core [:recur-transfers 0 :tail-position] false)
                 (assoc-in core [:loop-bindings 1 :initializer-syntax-id] :forged)]]
      (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [bad types {} 65536])))))
    (is (= "C7-TYPE-MISMATCH"
           (get-in (c7-invoke 'sh08-ft-loop-type-step [core (assoc types :next-value :gravity.type/string) {} 65536]) [:issues 0 :rule])))
    (is (= "C7-GENERIC" (get-in (derive core types 1) [:diagnostics 0 :rule])))
    (is (= :unsupported-throwing-body-operation
           (get-in (derive (update core :nodes #(mapv (fn [n] (if (= :transfer (:node-id n)) (assoc n :core-form :set!) n)) %)) types 65536)
                   [:diagnostics 0 :reason]))))
  (let [[base table] (function-recur-data)
        payload (node :problem :literal [] :function-body {})
        core (update base :nodes #(into [payload] (map (fn [n] (if (= :next-value (:node-id n))
                                                               (node :next-value :throw [:problem] :function-body {}) n)) %)))
        types (assoc table :problem :gravity.type/string :next-value :gravity.type/never)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {} 65536])
        graph (c7-invoke 'sh08-tc-graph [core types (zipmap (map :node-id (:nodes core)) (repeat :function-target))
                                         #{:function-target} 65536 {}])]
    (is (= :accepted (:status checked)))
    (is (= false (:normal-transfer? (first (:constraints checked)))))
    (is (= :accepted (:status graph)))
    (is (= [:gravity.type/string] (get-in graph [:nodes :transfer])))
    (is (= :gravity.type/never (:transfer types)))
    (is (= :gravity.type/string (:branch types)))))

(deftest function-recur-node-inference-development-kernel
  (let [[core prefilled] (function-recur-data)
        table (dissoc prefilled :transfer)
        transfer (first (filter #(= :recur (:core-form %)) (:nodes core)))
        branch (first (filter #(= :if (:core-form %)) (:nodes core)))
        nominal (c7-invoke 'sh08-ft-infer-node-nominal [transfer table {} {} {} {}])
        inferred (c7-invoke 'sh08-ft-infer-node-with-context [transfer table {} {} {} {}])
        joined (assoc table :transfer inferred)]
    (is (not (contains? table :transfer)))
    (is (= :gravity.type/never nominal inferred))
    (is (= :gravity.type/string
           (c7-invoke 'sh08-ft-infer-node-with-context [branch joined {} {} {} {}])))
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core joined {} 65536]))))
    (is (= :gravity.type/never
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :loop) table {} {} {} {}])))
    (is (= :gravity.type/unknown
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :forged) table {} {} {} {}])))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:recur-transfers 0 :target-id] :forged) joined {} 65536]) [:issues 0 :rule])))
    (is (= "C7-ANNOTATION"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(update-in core [:nodes 6 :attributes] dissoc :signature) joined
                      {:flag :gravity.type/bool :value :gravity.type/string} 65536]) [:issues 0 :rule])))))

(deftest function-recur-slots-development-kernel
  (let [[core table] (function-recur-data)
        result (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        premise (first (:constraints result))]
    (is (= :accepted (:status result)))
    (is (= [:gravity.type/bool :gravity.type/string] (:slot-types premise) (:argument-types premise)))
    (is (= :gravity.type/never (:result-type premise)))
    (is (= :solved (:status premise)))
    (is (= :loop-type-work-limit
           (get-in (c7-invoke 'sh08-ft-loop-type-step [core table {} (dec (:work-count result))]) [:issues 0 :reason])))
    (is (= result (c7-invoke 'sh08-ft-loop-type-step [core table {} (:work-count result)])))
    (doseq [[id type expected] [[:next-value :gravity.type/bool :gravity.type/string]
                               [:next-flag :gravity.type/string :gravity.type/bool]]]
      (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table id type) {} 65536])]
        (is (= ["C7-TYPE-MISMATCH" :function-recur-slot-type-mismatch id expected type]
               ((juxt :rule :reason :core-node-id :expected :actual) (first (:issues bad)))))))
    (let [unannotated (update-in core [:nodes 6 :attributes] dissoc :signature)
          caller-types {:flag :gravity.type/bool :value :gravity.type/string}
          pending (c7-invoke 'sh08-ft-loop-type-step [unannotated table caller-types 65536])]
      (is (= ["C7-ANNOTATION" :function-recur-parameter-types-required]
             ((juxt :rule :reason) (first (:issues pending)))))
      (is (= "C7-VERIFY"
             (get-in (c7-invoke 'sh08-ft-loop-type-step
                       [(assoc-in unannotated [:recur-transfers 0 :target-id] :forged) table caller-types 65536]) [:issues 0 :rule]))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {} 65536])]
      (is (= :accepted (:status never)))
      (is (= false (:normal-transfer? (first (:constraints never))))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:binding-table 1 :name] 'forged) table {} 65536]) [:issues 0 :rule])))))

(deftest function-recur-backedge-development-kernel
  (let [[core table] (function-recur-data)
        ids (zipmap (concat (map :node-id (:nodes core)) [:flag :value])
                    (map #(str "sha256:" (format "%064x" %)) (range 101 200)))
        [core table] (walk/postwalk-replace ids [core table])
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        owner (last (:nodes core)) target (first (:recur-targets core))
        function {:function-id (:node-id owner) :function-core-node-id (:node-id owner)
                  :body-core-node-id (ids :branch) :parameter-binding-ids (mapv ids [:flag :value])
                  :parameters [{:binding-id (ids :flag) :type :gravity.type/bool}
                               {:binding-id (ids :value) :type :gravity.type/string}]
                  :signature (get-in owner [:attributes :signature])}
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        module {:profile :meta :target :jvm :effects [] :capabilities []}
        transfer-node (first (filter #(= :recur (:core-form %)) (:nodes core)))
        rows (mapv #(effects 'body-loop-row % transfer-node module) (:constraints checked))
        facts {:module module :type-table table :constraint-ledger rows :function-type-table [function]}
        derived (effects 'body-derive-module-nodes core facts {} 65536)
        graph (:nodes derived)]
    (is (= [:gravity.type/bool :gravity.type/string] (effects 'body-function-recur-slot-types target owner function)))
    (is (empty? (filter #(= :loop (:core-form %)) (:nodes core))))
    (is (true? (:recur-control? derived)))
    (is (= :accepted (:status derived)) (pr-str derived))
    (is (= [(ids :branch)] (get-in graph [(ids :transfer) :loop-backedge :body-core-node-ids])))
    (is (= (first (:recur-transfers core)) (get-in graph [(ids :transfer) :loop-backedge :source-transfer])))
    (is (= target (get-in graph [(ids :transfer) :loop-backedge :source-target])))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                  (assoc-in graph [(ids :transfer) :dependencies] [(ids :next-flag) (ids :next-value)]))))
    (doseq [bad [(assoc-in facts [:function-type-table 0 :parameters 1 :type] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :body-core-node-id] (ids :next-value))
                 (assoc-in facts [:function-type-table 0 :parameter-binding-ids] (mapv ids [:value :flag]))
                 (assoc-in facts [:constraint-ledger 0 :dependencies :loop-premise :slot-types] [])]]
      (is (= :rejected (:status (effects 'body-loop-joins core bad 65536)))))
    (is (nil? (effects 'body-function-recur-slot-types target owner (assoc function :parameters 3))))
    (doseq [[kind value reason] [[:empty [] :incomplete-loop-control-coverage]
                                 [:nil nil :incomplete-loop-control-tables]
                                 [:absent nil :incomplete-loop-control-tables]]]
      (let [missing (if (= kind :absent)
                      (-> core (dissoc :recur-transfers) (update :identity-preimage dissoc :recur-transfers))
                      (-> core (assoc :recur-transfers value) (assoc-in [:identity-preimage :recur-transfers] value)))
            rejected (effects 'body-derive-module-nodes missing facts {} 65536)]
        (is (true? (:recur-control? rejected)))
        (is (= [:rejected reason] ((juxt :status :reason) rejected)))))
    (doseq [malformed [7 {} "bad"]]
      (is (= [true :rejected :incomplete-loop-control-tables]
             ((juxt :recur-control? :status :reason)
              (effects 'body-derive-module-nodes (assoc core :recur-transfers malformed) facts {} 65536)))))
    (let [plain {:nodes [(first (:nodes core))] :call-edges [] :recur-transfers []}
          plain-facts (assoc facts :function-type-table [])]
      (doseq [ordinary [plain (dissoc plain :recur-transfers) (assoc plain :recur-transfers nil)]]
        (let [derived (effects 'body-derive-module-nodes ordinary plain-facts {} 65536)]
          (is (= :accepted (:status derived)))
          (is (false? (:recur-control? derived)))
          (is (= (effects 'body-derive-nodes-with-foreign ordinary plain-facts {} 65536) (dissoc derived :recur-control?))))))
    (let [[loop-core loop-facts] (effect-loop-data)
          derived (effects 'body-derive-module-nodes loop-core loop-facts {} 65536)]
      (is (true? (:recur-control? derived)))
      (is (= (effects 'body-derive-loop-nodes loop-core loop-facts {} 65536) (dissoc derived :recur-control?))))
    (let [[recursive-core recursive-facts] ((body-helper 'recursive-kernel-data))
          derived (effects 'body-derive-module-nodes recursive-core recursive-facts {} 65536)]
      (is (false? (:recur-control? derived)))
      (is (= (effects 'body-derive-recursive-nodes recursive-core recursive-facts {} 65536)
             (dissoc derived :recur-control?))))))

(deftest function-target-reset-is-not-forgery-development-kernel
  (let [transfer (-> (node :transfer :recur [] :function-body
                           {:target-id :function-target :target-kind :function :arity 0 :type-compatibility :pending-sh08})
                     (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
        owner (node :function-target :fn [:transfer] :namespace {:recur-target-id :function-target})
        target {:target-id :function-target :target-kind :function :owner-core-node-id :function-target
                :owner-form-id :function-target-form :owner-syntax-id :function-target :arity 0 :binding-ids []
                :body-scope-id :function-body :parent-target-id nil :type-compatibility :pending-sh08}
        record {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                :target-id :function-target :target-kind :function :argument-node-ids [] :arity 0 :tail-position true
                :evaluation-order :arguments-left-to-right :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        core {:nodes [transfer owner] :binding-table [] :loop-bindings [] :recur-targets [target] :recur-transfers [record]}]
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core {} {} 65536]))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :forged) {} {} 65536]) [:issues 0 :rule])))
    (let [outer (assoc-in (node :outer :loop [:function-target] :namespace
                                {:target-id :outer :binding-count 0 :body-count 1 :initializer-child-indexes []
                                 :body-child-indexes [0] :result-policy {:kind :last-body :child-index 0}})
                          [:evaluation :kind] :initializers-then-body-left-to-right)
          outer-target {:target-id :outer :target-kind :loop :owner-core-node-id :outer :owner-form-id :outer-form
                        :owner-syntax-id :outer :arity 0 :binding-ids [] :body-scope-id :outer-body
                        :parent-target-id nil :type-compatibility :pending-sh08}
          nested (-> core (assoc-in [:nodes 1 :binding-context] :outer-body) (update :nodes conj outer)
                         (update :recur-targets conj outer-target))]
      (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [nested {} {} 65536]))))
      (is (= "C7-VERIFY" (get-in (c7-invoke 'sh08-ft-loop-type-step [(-> nested
                                     (assoc-in [:recur-transfers 0 :target-id] :outer)
                                     (assoc-in [:recur-transfers 0 :target-kind] :loop)
                                     (assoc-in [:nodes 0 :attributes :target-id] :outer)
                                     (assoc-in [:nodes 0 :attributes :target-kind] :loop)) {} {} 65536]) [:issues 0 :rule]))))))
