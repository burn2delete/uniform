(ns gravity.self-hosting.sh12-checked-i64-add-producer-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

;; Current original producers, not a supplied Core/MIR or frozen admission.
(defn existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn invoke [name & args]
  ((existing 'invoke) (existing 'c11-plan) name (vec args)))

(defn producer [name & args]
  ((existing 'invoke) (existing 'parameter-c6-plan) name (vec args)))

(defn fixture [disposition extension]
  (str (.resolve @(existing 'root)
         (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/"
              disposition "/coordinator-checked-i64-add-producer." extension))))

(defn products [path text binding]
  ((existing 'parameter-source-products) path text binding
   (str (.resolve @(existing 'root) "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))))

(def current-pair
  (delay
    (let [path (fixture "accepted" "gravity") text (slurp path)
          first-product (products path text nil)
          binding (:compiled-binding-data first-product)
          raw (producer 'c6-c10-build-checked-core-template (:envelope first-product))
          fresh (products path text binding)
          replay (producer 'c6-c10-build-checked-core-template (:envelope fresh))
          verification (producer 'c6-c10-verify-checked-core-template
                         (:envelope fresh) (:artifact-template raw) (:digest-requests replay))
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact context binding (:ingress first-product) sealed)]
      {:path path :text text :products first-product :binding binding
       :raw raw :fresh fresh :replay replay :verification verification :core core})))

(deftest sh12-checked-i64-add-source-shape
  ;; Syntax/byte parity only. This selector executes no compiler plan.
  (doseq [disposition ["accepted" "rejected"]]
    (let [g (slurp (fixture disposition "gravity")) q (slurp (fixture disposition "qst"))]
      (is (= g q))
      (is (every? #(< (int %) 128) g))
      (let [[declaration function] (read-string (str "[" g "]"))]
        (is (= 'defn (first function)))
        (is (= '[left :- I64 right :- I64] (nth function 2)))
        (is (= '(+ left right) (nth function 3)))
        (is (= (if (= disposition "accepted") '(:effects #{:error/raise}) '(:effects #{}))
               (first (filter #(and (seq? %) (= :effects (first %))) declaration))))))))

(deftest sh12-checked-i64-add-produces-original-bound-facts
  (let [{:keys [raw replay verification core products fresh]} @current-pair
        nodes (:core-nodes core) ids (mapv :node-id nodes)
        add (get nodes 4) root (get nodes 5) check (get-in add [:safety :check])]
    (is (= :accepted (:status raw)))
    (is (= raw replay))
    (is (= (:envelope products) (:envelope fresh)))
    (is (= :passed (:status verification)))
    (is (= :bounded-checked-i64-add-hosted-safe-jvm-source (get-in products [:envelope :scope])))
    (is (= :checked-i64-add-v1 (get-in core [:scope :family])))
    (is (= [:parameter :parameter :local :local :integer-add :function] (mapv :source-operation nodes)))
    (is (= 6 (count nodes) (count (set ids)) (count (:source-origin-table core))))
    (is (= [:gravity/i64 :gravity/i64] (get-in root [:type :params])))
    (is (= :gravity/i64 (:type add) (get-in root [:type :return])))
    (is (= #{:error/raise} (get-in root [:type :latent-effects])
           (get-in core [:effect-facts (get ids 5) :latent])
           (get-in core [:effect-facts (get ids 5) :transitive])
           (get-in core [:effect-facts (get ids 5) :residual])))
    (is (= #{:error/numeric} (get-in root [:type :throws])
           (get-in core [:effect-graph :functions (get ids 5) :throws])))
    (is (= :runtime-checked (get-in add [:safety :outcome])
           (get-in core [:safety-facts (get ids 4) :outcome])))
    (is (nil? (get-in add [:safety :proof])))
    (is (nil? (get-in core [:proof-records (get ids 4)])))
    (is (= (subvec ids 2 4) (get-in check [:predicate :operand-ids])))
    (is (= [64 :signed :checked :error/numeric #{:error/raise}]
           ((juxt :bit-width :signedness :numeric-mode :failure :effects-introduced) check)))
    (is (= (:source add) (:source check)))
    (is (= (:check-id check) (get-in core [:safety-facts (get ids 4) :runtime-check])))
    (is (= 5 (count (get-in core [:dependency-order-graph :edges]))))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) ids))
    (is (true? (:clojure-seed-boundary? core)))
    (is (false? (:self-hosted? core)))))

(deftest sh12-checked-i64-add-replay-rejects-fact-and-check-substitution
  (let [{:keys [raw replay fresh]} @current-pair
        template (:artifact-template raw) nodes (:core-nodes template)
        add-id (:node-id (get nodes 4)) root-id (:node-id (get nodes 5))
        verify #(producer 'c6-c10-verify-checked-core-template
                  (:envelope fresh) % (:digest-requests replay))]
    (doseq [candidate [(assoc-in template [:core-nodes 4 :attributes :numeric-mode] :wrapping)
                       (assoc-in template [:core-nodes 4 :operands] (vec (reverse (:operands (get nodes 4)))))
                       (assoc-in template [:core-nodes 4 :safety :outcome] :proven-safe)
                       (update-in template [:core-nodes 4 :safety] dissoc :check)
                       (assoc-in template [:core-nodes 4 :safety :check :bit-width] 32)
                       (assoc-in template [:core-nodes 4 :safety :check :failure] :panic/numeric)
                       (assoc-in template [:core-nodes 4 :source :generated-origin] [:forged])
                       (assoc-in template [:type-facts add-id :type] :gravity/bool)
                       (assoc-in template [:effect-facts root-id :residual] #{})
                       (update-in template [:semantic-provenance :binding-pins] dissoc :builder-source-hash)]]
      (is (= :rejected (:status (verify candidate)))))))

(deftest sh12-checked-i64-add-rejects-undeclared-error-effect
  (let [{:keys [binding]} @current-pair
        path (fixture "rejected" "gravity") input (products path (slurp path) binding)
        result (producer 'c6-c10-build-checked-core-template (:envelope input))]
    (is (= :bounded-checked-i64-add-hosted-safe-jvm-source (get-in input [:envelope :scope])))
    (is (= :rejected (:status result)))
    (is (= "C8-UNDECLARED" (name (get-in result [:diagnostics 0 :rule]))))
    (is (nil? (:artifact-template result)))))

(deftest sh12-checked-i64-add-rejects-original-body-and-mode-mismatches
  (let [{:keys [products]} @current-pair input (:envelope products)
        build #(producer 'c6-c10-build-checked-core-template %)]
    ;; Mutated transport is rejected, never used as a fake accepted producer.
    (doseq [candidate [(assoc input :scope :bounded-pure-hosted-safe-jvm-source)
                       (assoc-in input [:stage2-plan :functions 'main :instructions 0 :args] [])
                       (update-in input [:stage2-plan :functions 'main :instructions 0 :args] #(vec (reverse %)))
                       (assoc-in input [:stage2-plan :functions 'main :instructions 0 :args 1 :name] 'left)
                       (assoc-in input [:stage2-plan :functions 'main :arity] 1)
                       (assoc-in input [:authoritative-module :safety] :unsafe)
                       (assoc-in input [:resolved-parameter :kind] :gravity/c6-c10-resolved-two-bool-parameters-v1)]]
      (is (= :rejected (:status (build candidate)))))))
