(ns gravity.c3-reader-expansions-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c3-reader-expansions :as expansions]
            [gravity.c3-literal-projection :as literals]
            [gravity.reader-primitives :as primitives]))

(def ^:private seed
  {:form-id :root :span {:byte-start 100 :scalar-start 50 :start {:line 3 :column 5}}
   :reader-origin {:raw-excerpt "`(\"🙂\" ~x)"}})
(def ^:private forms
  [{:form-id :root :children [:child]
    :generated-origin [{:reason :syntax-quote :child-form-id :child
                        :from {:byte-start 100 :byte-end 101}
                        :expanded-form '(syntax-quote x)}]}
   {:form-id :child :parent-form-id :root :children [:value]
    :generated-origin [{:reason :unquote :child-form-id :value
                        :from {:byte-start 109 :byte-end 110}
                        :expanded-form '(unquote x)}]}
   {:form-id :value :parent-form-id :child :children []}
   {:form-id :other :children []
    :generated-origin [{:reason :deref :child-form-id :outside}]}])
(def ^:private c2 {:form-tree forms :syntax-seed-stream [seed]})
(defn- node [root]
  {:source {:form-id (:form-id root)}
   :facts [{:fact-kind :authenticated-reader-products
            :value {:reader-expansion-origins (:origins root)}}]})

(deftest root-relative-utf8-history-retains-order-inputs-and-surface-locations
  (let [roots (expansions/roots c2 "source-id")
        history (:origins (first roots))]
    (is (= [:syntax-quote :unquote] (mapv :reader-abbreviation history)))
    (is (= [:root :child] (mapv :form-id history)))
    (is (= [:child :value] (mapv :input-form-id history)))
    (is (= ['(syntax-quote x) '(unquote x)] (mapv :expanded-form history)))
    (is (= {:file "source-id" :byte-start 109 :byte-end 110
            :scalar-start 56 :scalar-end 57 :line-start 3 :column-start 11
            :line-end 3 :column-end 12}
           (:surface-span (second history))))
    (is (every? #(not (contains? (:surface-span %) :source)) history))
    (is (= roots (expansions/roots (assoc-in c2 [:syntax-seed-stream 0 :span :source] "/other")
                                  "source-id")))))

(deftest every-fact-and-reference-is-compared-with-the-current-history
  (let [roots (expansions/roots c2 "source-id") good (mapv node roots)]
    (is (true? (expansions/current? roots good)))
    (doseq [changed [(assoc-in good [0 :source :form-id] :other)
                     (assoc-in good [0 :facts 0 :value :reader-expansion-origins] [])
                     (update-in good [0 :facts 0 :value :reader-expansion-origins] reverse)
                     (update-in good [0 :facts 0 :value :reader-expansion-origins] #(conj % (first %)))
                     (assoc-in good [0 :facts 0 :value :reader-expansion-origins 0 :input-form-id] :other)
                     (update-in good [0 :facts 0 :value] dissoc :reader-expansion-origins)
                     (update-in good [0 :facts] #(conj % (first %)))
                     [] (conj good (first good))]]
      (is (false? (expansions/current? roots changed))))))

(deftest absent-history-does-not-create-a-fact-or-permit-an-extra-one
  (let [plain (assoc c2 :form-tree (mapv #(dissoc % :generated-origin) forms))
        roots (expansions/roots plain "source-id")
        good [{:source {:form-id :root}
               :facts [{:fact-kind :authenticated-reader-products :value {}}]}]]
    (is (= [{:form-id :root :origins []}] roots))
    (is (true? (expansions/current? roots good)))
    (is (false? (expansions/current? roots (assoc-in good [0 :facts 0 :value :reader-expansion-origins] []))))))

(deftest exact-decimals-use-the-existing-classification-and-interposition-boundary
  (doseq [[value expected] [[1.25M :decimal] [(bigdec "1E+8") :decimal]
                            [1.25 :decimal] [42N :integer] [(Object.) :unknown]]]
    (is (= expected (primitives/form-kind value))))
  (let [calls (atom []) failure (ex-info "second classification rejected" {})
        operation (fn [value]
                    (swap! calls conj value)
                    (if (= 1 (count @calls)) :interposed (throw failure)))
        classify #(literals/c3-source-form-kind {:form 1.25M} {:kind :decimal}
                    {:form-tree [] :token-stream []} {:authentic? true})]
    (literals/with-operations {:form-kind operation}
      #(do (is (= :interposed (classify)))
           (is (identical? failure (try (classify) nil (catch Throwable error error))))))
    (is (= [1.25M 1.25M] @calls))))
