(ns gravity.self-hosting.list-return-premise-test
  "Normalized actual-owner LR joins; no whole-source admission authority."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- helper [name]
  (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name))
(defn- c7 [name & args] ((helper 'invoke-c7) name (vec args)))
(defn- body [name & args]
  (apply (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) name args))
(def ^:private emitter-plan
  (delay
    (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))
(defn- emit [name & args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :list-return-premise-test} @emitter-plan name (vec args)))
(defn- positional [members] {:kind :gravity.type/list :shape :literal :element-types members})
(def ^:private homogeneous
  {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string})
(defn- derives [input]
  (let [{:keys [core record function nodes types bindings forms]} input
        args [core record function nodes types bindings forms 65536]]
    [(apply c7 'sh08-lr-derive args)
     (apply body 'body-lr-derive args) (apply emit 'p15-s23-lr-derive args)]))
(defn- contexts [products facts]
  (let [args [(:core products) facts (:bindings products) (:forms products) 65536]]
    [(apply body 'body-lr-context args) (apply emit 'p15-s23-lr-context args)]))
(defn- premise? [value]
  (and (map? value) (= :sh08-homogeneous-list-return-v2 (:version value))))
(defn- change-premises [facts transform]
  (walk/postwalk #(if (premise? %) (transform %) %) facts))

(deftest complete-return-premises-and-consumers-agree
  (doseq [actual [(positional []) (positional [:gravity.type/string])
                  (positional [:gravity.type/string :gravity.type/string])
                  (positional (vec (repeat 1024 :gravity.type/string)))
                  homogeneous :gravity.type/never]]
    (let [input ((helper 'homogeneous-list-return-input) actual :gravity.type/string 0)
          results (derives input) premises (mapv :premise results) premise (first premises)
          expected-count (when (= :literal (:shape actual)) (count (:element-types actual)))
          products ((helper 'homogeneous-list-return-products) [actual] :gravity.type/string)
          joins (contexts products (:facts products))
          definition ((helper 'homogeneous-list-return-definition-input) actual)
          returns (second (contexts definition (:facts definition)))
          joined (emit 'p15-s23-typed-definition
                       (assoc (:context definition) :return-context (:context returns))
                       (:definition-name definition))]
      (is (every? #(= :accepted (:status %)) results))
      (is (apply = premises))
      (is (= :sh08-homogeneous-list-return-v2 (:version premise)))
      (is (not-any? #(contains? premise %) [:type-work :member-work]))
      (is (= expected-count (:checked-element-count premise)))
      (is (= :gravity.type/string (:checked-element-type premise)))
      (is (= (not= :gravity.type/never actual) (:normal-completion? premise)))
      (is (every? #(= :accepted (:status %)) joins))
      (is (= (:context (first joins)) (:context (second joins))))
      (is (= (c7 'sh08-lr-row premise) (body 'body-lr-row premise) (emit 'p15-s23-lr-row premise)))
      (is (= :gravity/sh08-homogeneous-list-return-constraint-v2
             (get-in (c7 'sh08-lr-row premise) [:constraint-id-request :domain])))
      (is (= :accepted (:status returns)))
      (is (= :complete (:status joined)))
      (is (= (get-in definition [:function :return-compatibility])
             (get-in joined [:value :function :return-compatibility]))))))

(deftest return-contexts-rederive-complete-current-evidence
  (let [products ((helper 'homogeneous-list-return-products)
                  [(positional [:gravity.type/string :gravity.type/string]) (positional [])]
                  :gravity.type/string)
        facts (:facts products) rows (:constraint-ledger facts)
        bad-facts
        [(change-premises facts #(assoc % :checked-element-count 99))
         (change-premises facts #(assoc % :checked-element-type :gravity.type/keyword))
         (change-premises facts #(assoc % :relation :homogeneous-list-identity))
         (change-premises facts #(assoc % :type-work 0))
         (change-premises facts #(assoc % :member-work 2))
         (change-premises facts #(assoc % :extra true))
         (change-premises facts #(assoc % :version :sh08-homogeneous-list-return-v1))
         (assoc facts :constraint-ledger (conj rows (first rows)))
         (assoc facts :constraint-ledger (vec (reverse rows)))
         (assoc-in facts [:constraint-ledger 0 :lhs :function-id] :orphan)
         (assoc-in facts [:constraint-ledger 0 :producer-rule] :sh08-homogeneous-list-return-v1)
         (assoc-in facts [:constraint-ledger 0 :constraint-id-request :domain]
                   :gravity/sh08-homogeneous-list-return-constraint-v1)
         (update facts :constraint-ledger subvec 1)
         (walk/postwalk
          #(cond
             (premise? %) (assoc % :version :sh08-homogeneous-list-return-v1 :type-work 0 :member-work 0)
             (and (map? %) (= :sh08-homogeneous-list-return-v2 (:producer-rule %)))
             (assoc % :producer-rule :sh08-homogeneous-list-return-v1)
             (and (map? %) (= :gravity/sh08-homogeneous-list-return-constraint-v2 (:domain %)))
             (assoc % :domain :gravity/sh08-homogeneous-list-return-constraint-v1)
             :else %) facts)]]
    (is (every? #(= :accepted (:status %)) (contexts products facts)))
    (doseq [[index changed] (map-indexed vector bad-facts)]
      (is (every? #(= :rejected (:status %)) (contexts products changed))
          (str "Coherent or mixed stale evidence mutation " index " must reject")))))

(deftest return-validation-and-source-checks-remain-effective
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque formatting"))))]
    (doseq [actual [(positional [:gravity.type/string :gravity.type/keyword])
                    (positional [:gravity.type/string :gravity.type/unknown])
                    (positional (assoc (vec (repeat 1024 :gravity.type/string)) 1023 :gravity.type/keyword))
                    (positional [:gravity.type/string opaque])
                    (positional (assoc (vec (repeat 1024 :gravity.type/string)) 1023 opaque))
                    (positional '()) (positional [homogeneous])]]
      (let [args [homogeneous actual 65536]
            results [(apply c7 'sh08-lr-type-work args)
                     (apply body 'body-lr-type-work args) (apply emit 'p15-s23-lr-type-work args)]]
        (is (every? #(= :rejected (:status %)) results))))
    (is (= 0 @callbacks)))
  (let [input ((helper 'homogeneous-list-return-input) (positional [:gravity.type/string]) :gravity.type/string 0)
        declaration (:declaration input) record (:record input)
        changes [#(assoc-in % [:record :signature :return :element-binding-id] :forged)
                 #(assoc-in % [:bindings (:element-binding-id declaration) :kind] :var)
                 #(assoc-in % [:forms (:element-form-id declaration) :value] 'Keyword)
                 #(assoc-in % [:forms (:type-form-id declaration) :child-form-ids] [])
                 #(assoc-in % [:forms (:constructor-form-id declaration) :scope-id] :sibling)
                 #(assoc-in % [:nodes (:body-core-node-id record) :source :form-id] :sibling)]]
    (is (every? #(= :accepted (:status %)) (derives input)))
    (doseq [change changes]
      (is (every? #(= :rejected (:status %)) (derives (change input)))))))

(deftest legacy-return-producers-cannot-escape-the-row-census
  (let [products ((helper 'homogeneous-list-return-products)
                  [(positional [:gravity.type/string])] :gravity.type/string)
        facts (:facts products)
        legacy (walk/postwalk
                #(cond
                   (premise? %) (assoc % :version :sh08-homogeneous-list-return-v1 :type-work 0 :member-work 1)
                   (and (map? %) (= :sh08-homogeneous-list-return-v2 (:producer-rule %)))
                   (assoc % :producer-rule :sh08-homogeneous-list-return-v1)
                   (and (map? %) (= :gravity/sh08-homogeneous-list-return-constraint-v2 (:domain %)))
                   (assoc % :domain :gravity/sh08-homogeneous-list-return-constraint-v1)
                   :else %) (first (:constraint-ledger facts)))]
    (is (every? #(= :accepted (:status %)) (contexts products facts)))
    (doseq [row [legacy (assoc legacy :kind :equality)
                 (assoc legacy :kind :unrecognized)
                 (dissoc legacy :kind)]]
      (let [results (contexts products (update facts :constraint-ledger conj row))]
        (is (every? #(= :rejected (:status %)) results))
        (is (every? #(= :list-return-constraint-census (:reason %)) results))))))
