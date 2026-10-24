(ns gravity.self-hosting.sh12-checked-i64-mul-producer-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as add]))

;; SAFE9/C6/C10: fresh original source products, not normalized arithmetic DATA.
(defn fixture [disposition extension]
  (str (.resolve @(add/existing 'root)
         (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/" disposition
              "/coordinator-checked-i64-mul-producer." extension))))

(def current-pair
  (delay
    (let [path (fixture "accepted" "gravity") text (slurp path)
          products (add/products path text nil) binding (:compiled-binding-data products)
          raw (add/producer 'c6-c10-build-checked-core-template (:envelope products))
          fresh (add/products path text binding)
          replay (add/producer 'c6-c10-build-checked-core-template (:envelope fresh))
          verification (add/producer 'c6-c10-verify-checked-core-template
                         (:envelope fresh) (:artifact-template raw) (:digest-requests replay))
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact context binding (:ingress products) sealed)]
      {:path path :products products :binding binding :raw raw :fresh fresh
       :replay replay :verification verification :core core})))

(defn reversed-fixture [disposition extension]
  (str (.resolve @(add/existing 'root)
         (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/" disposition
              "/coordinator-reversed-checked-i64-mul-producer." extension))))

(def reversed-pair
  (delay
    (let [path (reversed-fixture "accepted" "gravity") text (slurp path)
          products (add/products path text nil) binding (:compiled-binding-data products)
          raw (add/producer 'c6-c10-build-checked-core-template (:envelope products))
          fresh (add/products path text binding)
          replay (add/producer 'c6-c10-build-checked-core-template (:envelope fresh))
          verification (add/producer 'c6-c10-verify-checked-core-template
                         (:envelope fresh) (:artifact-template raw) (:digest-requests replay))
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact context binding (:ingress products) sealed)]
      {:path path :products products :binding binding :raw raw :fresh fresh
       :replay replay :verification verification :core core})))

(deftest sh12-reversed-checked-i64-mul-paired-source-shape
  (doseq [disposition ["accepted" "rejected"]]
    (let [g (slurp (reversed-fixture disposition "gravity")) q (slurp (reversed-fixture disposition "qst"))
          [declaration function] (binding [*read-eval* false] (read-string (str "[" g "]")))]
      (is (= g q))
      (is (every? #(< (int %) 128) g))
      (is (= '[left :- I64 right :- I64] (nth function 2)))
      (is (= (if (= disposition "accepted") '(* right left) '(* right right)) (nth function 3)))
      (is (= '(:effects #{:error/raise})
             (first (filter #(and (seq? %) (= :effects (first %))) declaration)))))))

(deftest sh12-checked-i64-mul-paired-source-shape
  (doseq [disposition ["accepted" "rejected"]]
    (let [g (slurp (fixture disposition "gravity")) q (slurp (fixture disposition "qst"))
          [declaration function] (binding [*read-eval* false] (read-string (str "[" g "]")))]
      (is (= g q))
      (is (every? #(< (int %) 128) g))
      (is (= '[left :- I64 right :- I64] (nth function 2)))
      (is (= '(* left right) (nth function 3)))
      (is (= (if (= disposition "accepted") '(:effects #{:error/raise}) '(:effects #{}))
             (first (filter #(and (seq? %) (= :effects (first %))) declaration)))))))

(deftest sh12-checked-i64-mul-original-source-plan-kernel
  ;; Four exact original definitions on supplied C2/plan DATA only.
  (let [path (str (.resolve @(add/existing 'root) (add/existing 'parameter-c6-source)))
        names '#{c6c10-i64-order-body? c6c10-form-by-id c6c10-plain-form? c6c10-parameter-checks?}
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? names (second %))) forms)
        primitives '#{defn if let quote get count = first second rest vector? list?}
        callees (for [form selected expression (tree-seq coll? seq form)
                      :when (and (seq? expression) (symbol? (first expression)))] (first expression))
        text (str "(ns gravity.test.mul-source-order (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        _ (assert (= names (set (map second selected))))
        _ (assert (and (= 4 (count selected)) (< (count text) 8192)))
        _ (assert (every? #(or (contains? names %) (contains? primitives %)) callees))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "mul-source-order.gravity" text)
        data (fn [a b]
               [{:form-tree [{:form-id :body :kind :list :children [:operator :a :b] :metadata {}}
                             {:form-id :operator :kind :symbol :value '* :metadata {}}
                             {:form-id :a :kind :symbol :value a :metadata {}}
                             {:form-id :b :kind :symbol :value b :metadata {}}]
                 :resolved-parameter {:parameters [{:parameter-binding {:name 'left}} {:parameter-binding {:name 'right}}]}}
                {:instructions [{:op :builtin-call :function '* :args [{:op :local :name a} {:op :local :name b}]}]
                 :body [(list '* a b)]}
                {:children [:defn :name :parameters :body]}])
        call #(bootstrap/p15-s23-stage2-runtime-execute-function
                {:engine :sh12-mul-source-order-data :compiler-artifact-plan? true}
                plan 'c6c10-i64-order-body? %)]
    (doseq [[a b] [['left 'right] ['right 'left]]]
      (is (true? (call (data a b)))))
    (doseq [[a b] [['left 'left] ['right 'right] ['unknown 'right]]]
      (is (false? (call (data a b)))))
    (let [original (data 'left 'right) swapped (update-in original [1 :instructions 0 :args] #(vec (reverse %)))]
      (doseq [candidate [swapped (assoc-in swapped [1 :body] ['(* right left)])
                         (assoc-in original [1 :instructions 0 :args 1 :op] :literal)
                         (update-in original [1 :instructions 0 :args] pop)
                         (assoc-in original [0 :form-tree 2 :metadata] {:changed true})]]
        (is (false? (call candidate)))))))

(deftest sh12-checked-i64-mul-original-node-and-digest-kernel
  ;; Reuse the existing finite 22-definition original DATA kernel unchanged.
  (let [call ((var-get (ns-resolve 'gravity.self-hosting.sh12-checked-i64-add-producer-test 'original-node-kernel)))
        origin (call 'c6c10-add-digest-request {:requests [] :next-ordinal 0} {:origin :supplied-data})
        source {:origin-id (:ref origin) :span {:start 0 :end 1} :plan-path [:data]}
        origins {:origin-id (:ref origin) :semantic {:origin :supplied-data} :raw {} :source source}
        build (fn [product operation operands path]
                (call 'c6c10-add-node-with-origin product {:source-content-hash "supplied-data"}
                      {:span (:span source)} path :arithmetic operation true 1 operands
                      {:numeric-mode :checked} :gravity/i64 :arithmetic-result {} nil nil origins))
        first-node (build (call 'c6c10-empty-product (:state origin)) :literal [] [:input])
        product (build first-node :integer-mul [(:result-node-id first-node) (:result-node-id first-node)] [:mul])
        node (second (:nodes product)) check (get-in node [:safety :check])
        ordinal (:digest-ref (:check-id check)) request (get-in product [:state :requests ordinal])]
    (is (= :integer-mul (:source-operation node)))
    (is (= :runtime-checked (get-in node [:safety :outcome])))
    (is (= :checked-signed-i64-multiplication (get-in node [:safety :basis])))
    (is (nil? (get-in node [:safety :proof])))
    (is (= #{:error/raise} (:effects node)))
    (is (= :mul (get-in check [:predicate :operator])))
    (is (= :operands-in-declared-width-and-checked-mul-result-within-width (get-in check [:predicate :kind])))
    (is (= (:operands node) (get-in check [:predicate :operand-ids])))
    (is (= :gravity/c6-c10-checked-i64-mul-check-v1 (get-in request [:preimage :domain])))
    (is (= {:digest-ref (dec ordinal)} (get-in request [:preimage :ownership-digest])))))

(deftest sh12-checked-i64-mul-closed-host-envelope-kernel
  ;; Supplied bounded metadata only; this never establishes original Core.
  (let [envelope ((var-get (ns-resolve 'gravity.self-hosting.sh12-checked-i64-add-producer-test 'envelope-data)) true)
        sub (-> envelope
                (assoc-in [:authority :scope] :bounded-checked-i64-sub-c6-c10)
                (assoc-in [:artifact-template :status] :complete-for-checked-i64-sub-hosted-safe-jvm-slice)
                (assoc-in [:artifact-template :scope :family] :checked-i64-sub-v1)
                (assoc-in [:artifact-template :scope :source-operations] [:parameter :local :integer-sub :function]))
        mul (-> envelope
                (assoc-in [:authority :scope] :bounded-checked-i64-mul-c6-c10)
                (assoc-in [:artifact-template :status] :complete-for-checked-i64-mul-hosted-safe-jvm-slice)
                (assoc-in [:artifact-template :scope :family] :checked-i64-mul-v1)
                (assoc-in [:artifact-template :scope :source-operations] [:parameter :local :integer-mul :function]))
        validate #(bootstrap/p15-s23-c6c10-validate-builder-result-envelope! "mul-envelope-data.gravity" %)
        observe #(try (validate %) :accepted
                      (catch clojure.lang.ExceptionInfo error (select-keys (ex-data error) [:id :missing-fact])))]
    (is (= envelope (validate envelope)))
    (is (= sub (validate sub)))
    (is (= mul (validate mul)))
    (doseq [candidate [(assoc-in mul [:authority :scope] :bounded-pure-c6-c10)
                       (assoc-in mul [:authority :scope] :bounded-checked-i64-add-c6-c10)
                       (assoc-in mul [:authority :scope] :bounded-checked-i64-sub-c6-c10)
                       (assoc-in mul [:artifact-template :status] :complete-for-checked-i64-add-hosted-safe-jvm-slice)
                       (assoc-in mul [:artifact-template :status] :complete-for-checked-i64-sub-hosted-safe-jvm-slice)
                       (assoc-in mul [:artifact-template :scope :source-operations] [:parameter :local :integer-add :function])
                       (assoc-in mul [:artifact-template :scope :source-operations] [:parameter :local :integer-sub :function])
                       (assoc-in mul [:artifact-template :scope :safety] :proven-safe)
                       (assoc-in mul [:artifact-template :scope :intrinsic-effects] #{})]]
      (is (= {:id "C6-VERIFY" :missing-fact :exact-gravity-builder-result-envelope} (observe candidate))))))

(deftest sh12-checked-i64-mul-produces-original-bound-facts
  (let [{:keys [raw replay products fresh verification core]} @current-pair
        nodes (:core-nodes core) ids (mapv :node-id nodes) mul (get nodes 4) root (get nodes 5)
        check (get-in mul [:safety :check])]
    (is (= :accepted (:status raw)))
    (is (= raw replay))
    (is (= (:envelope products) (:envelope fresh)))
    (is (= :passed (:status verification)))
    (is (= :bounded-checked-i64-mul-hosted-safe-jvm-source (get-in products [:envelope :scope])))
    (is (= :bounded-checked-i64-mul-c6-c10 (get-in raw [:authority :scope])))
    (is (= :checked-i64-mul-v1 (get-in core [:scope :family])))
    (is (= [:parameter :parameter :local :local :integer-mul :function] (mapv :source-operation nodes)))
    (is (= 6 (count nodes) (count (set ids)) (count (:source-origin-table core))))
    (is (= [[(get ids 0)] [(get ids 1)]] (mapv :operands (subvec nodes 2 4))))
    (is (= [:gravity/i64 :gravity/i64] (get-in root [:type :params])))
    (is (= :gravity/i64 (:type mul) (get-in root [:type :return])))
    (is (= #{:error/raise} (get-in root [:type :latent-effects]) (:effects mul)))
    (is (= #{:error/numeric} (get-in root [:type :throws])))
    (is (= :runtime-checked (get-in mul [:safety :outcome])))
    (is (nil? (get-in mul [:safety :proof])))
    (is (nil? (get-in core [:proof-records (get ids 4)])))
    (is (= :mul (get-in check [:predicate :operator])))
    (is (= (subvec ids 2 4) (:operands mul) (get-in check [:predicate :operand-ids])))
    (is (= [64 :signed :checked :error/numeric #{:error/raise}]
           ((juxt :bit-width :signedness :numeric-mode :failure :effects-introduced) check)))
    (is (= (:source mul) (:source check)))
    (is (= (:check-id check) (get-in core [:safety-facts (get ids 4) :runtime-check])))
    (is (= 5 (count (get-in core [:dependency-order-graph :edges]))))
    (is (true? (:clojure-seed-boundary? core)))
    (is (false? (:self-hosted? core)))))

(deftest sh12-checked-i64-mul-rejects-undeclared-error-effect
  (let [binding (:binding @current-pair) path (fixture "rejected" "gravity")
        input (add/products path (slurp path) binding)
        result (add/producer 'c6-c10-build-checked-core-template (:envelope input))]
    (is (= :rejected (:status result)))
    (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])))
    (is (= :declared-numeric-error-effect (get-in result [:diagnostics 0 :facts :missing-fact])))
    (is (nil? (:artifact-template result)))))

(deftest sh12-checked-i64-mul-rejects-original-source-and-check-substitution
  (let [{:keys [products raw fresh replay]} @current-pair input (:envelope products)
        args [:stage2-plan :functions 'main :instructions 0 :args]
        swapped (update-in input args #(vec (reverse %)))]
    (doseq [candidate [swapped (assoc-in swapped [:stage2-plan :functions 'main :body] ['(* right left)])
                       (assoc-in input (conj args 1 :name) 'left)
                       (update-in input args pop) (assoc-in input (conj args 1 :op) :literal)
                       (assoc-in input [:stage2-plan :functions 'main :arity] 1)
                       (assoc input :scope :bounded-pure-hosted-safe-jvm-source)]]
      (let [result (add/producer 'c6-c10-build-checked-core-template candidate)]
        (is (= :rejected (:status result)))
        (is (nil? (:artifact-template result)))))
    (doseq [candidate [(assoc-in (:artifact-template raw) [:core-nodes 4 :safety :check :predicate :operator] :add)
                       (update-in (:artifact-template raw) [:core-nodes 4 :operands] #(vec (reverse %)))]]
      (is (= :rejected (:status (add/producer 'c6-c10-verify-checked-core-template
                                 (:envelope fresh) candidate (:digest-requests replay))))))))

(deftest sh12-reversed-checked-i64-mul-preserves-original-binding-order
  (let [{:keys [raw replay products fresh verification core]} @reversed-pair
        nodes (:core-nodes core) ids (mapv :node-id nodes) locals (subvec nodes 2 4)
        origins (:source-origin-table core) forms (get-in products [:envelope :front-end-products :form-tree])
        source-names (mapv (fn [node]
                            (let [form-id (get-in origins [(get-in node [:source :origin-id]) :c2-form-id])]
                              (:value (first (filter #(= form-id (:form-id %)) forms))))) locals)]
    (is (= :accepted (:status raw)))
    (is (= raw replay))
    (is (= (:envelope products) (:envelope fresh)))
    (is (= :passed (:status verification)))
    (is (= '[left right] (get-in nodes [5 :attributes :params])))
    (is (= '[right left] (mapv #(get-in % [:attributes :name]) locals) source-names))
    (is (= [[(get ids 1)] [(get ids 0)]] (mapv :operands locals)))
    (is (= 2 (count (set (map :node-id locals))) (count (set (map #(get-in % [:source :origin-id]) locals)))))
    (is (= (subvec ids 2 4) (get-in nodes [4 :operands])
           (get-in nodes [4 :safety :check :predicate :operand-ids])))
    (is (= [[:functions 'main :body 0 :args 0] [:functions 'main :body 0 :args 1]] (mapv :path locals)))
    (is (= :checked-i64-mul-v1 (get-in core [:scope :family])))
    (is (= :mul (get-in nodes [4 :safety :check :predicate :operator])))
    (is (= :runtime-checked (get-in nodes [4 :safety :outcome])))
    (is (nil? (get-in nodes [4 :safety :proof])))
    (is (= #{:error/raise} (get-in nodes [5 :type :latent-effects])))
    (is (= #{:error/numeric} (get-in nodes [5 :type :throws])))
    (let [verify #(add/producer 'c6-c10-verify-checked-core-template (:envelope fresh) % (:digest-requests replay))]
      (is (= :rejected (:status (verify (assoc-in (:artifact-template raw) [:core-nodes 2 :operands] [(get ids 0)])))))
      (is (= :rejected (:status (verify (update-in (:artifact-template raw) [:core-nodes 4 :operands] #(vec (reverse %))))))))))

(deftest sh12-reversed-checked-i64-mul-rejects-repeated-source-and-forged-plan
  (let [pair @reversed-pair path (reversed-fixture "rejected" "gravity")
        unsupported (add/products path (slurp path) (:binding pair))
        rejected (add/producer 'c6-c10-build-checked-core-template (:envelope unsupported))
        input (:envelope (:products pair)) args [:stage2-plan :functions 'main :instructions 0 :args]
        body [:stage2-plan :functions 'main :body] swapped (update-in input args #(vec (reverse %)))]
    (is (= :rejected (:status rejected)))
    (is (= "C6-LOWERING-GAP" (get-in rejected [:diagnostics 0 :rule])))
    (is (= :bounded-two-i64-strict-order (get-in rejected [:diagnostics 0 :facts :missing-fact])))
    (is (nil? (:artifact-template rejected)))
    (doseq [candidate [swapped (assoc-in input body ['(* left right)])
                       (assoc-in swapped body ['(* left right)])
                       (assoc-in input (conj args 1 :name) 'right)
                       (assoc-in input (conj args 0 :name) 'left)]]
      (let [result (add/producer 'c6-c10-build-checked-core-template candidate)]
        (is (= :rejected (:status result)))
        (is (= "C6-LOWERING-GAP" (get-in result [:diagnostics 0 :rule])))
        (is (nil? (:artifact-template result)))))))
