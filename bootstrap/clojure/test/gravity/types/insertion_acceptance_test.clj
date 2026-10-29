(ns gravity.types.insertion-acceptance-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.types.insertion-acceptance :as selection]))

(def fixture-identity
  {:head (apply str (repeat 40 "1")) :tree (apply str (repeat 40 "2"))
   :main (apply str (repeat 40 "3")) :main-tree (apply str (repeat 40 "4"))
   :inputs {selection/owner-path (apply str (repeat 64 "a"))
            selection/source-path (apply str (repeat 64 "b"))
            "deps.edn" (apply str (repeat 64 "c"))}})
(defn request [lane] (assoc (get selection/lanes lane) :lane lane))
(defn definitions []
  (into {} (for [selector (mapcat :selectors (vals selection/lanes))]
             [selector (list 'deftest selector)])))
(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))
(defn prepared []
  (selection/prepare fixture-identity fixture-identity (definitions) (request :outer)))

(deftest preparation-is-nonlaunching-and-exact
  (doseq [lane (keys selection/lanes)]
    (let [r (selection/prepare fixture-identity fixture-identity (definitions) (request lane))]
      (is (= :unrun (:execution r)))
      (is (= (:selectors (request lane)) (:selectors r)))
      (is (= (count (:selectors r)) (count (filter #{"--exact"} (:command r)))))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest missing-and-substituted-identities-refuse-before-selection
  (doseq [field [:head :tree :main :main-tree :inputs]]
    (is (= :missing-identity
           (reason #(selection/prepare (dissoc fixture-identity field) fixture-identity
                                       (definitions) (request :outer))))))
  (doseq [changed [(assoc fixture-identity :head (apply str (repeat 40 "f")))
                   (assoc-in fixture-identity [:inputs selection/source-path] (apply str (repeat 64 "f")))
                   (update fixture-identity :inputs dissoc "deps.edn")]]
    (is (= :identity-mismatch
           (reason #(selection/prepare fixture-identity changed (definitions) (request :outer)))))))

(deftest wrong-selector-consumer-and-missing-real-test-refuse
  (doseq [[change expected] [[#(assoc % :lane :initializer) :unknown-lane]
                            [#(assoc % :consumer 'runtime-initializer) :wrong-consumer]
                            [#(update % :selectors conj 'unrelated-test) :wrong-selectors]
                            [#(update % :selectors pop) :wrong-selectors]]]
    (is (= expected (reason #(selection/prepare fixture-identity fixture-identity
                                               (definitions) (change (request :outer)))))))
  (is (= :missing-test
         (reason #(selection/prepare fixture-identity fixture-identity {}
                                     (request :outer))))))

(deftest setup-failure-cannot-be-misattributed-to-an-owner-test
  (let [p (prepared)
        result (merge (select-keys p [:source :lane :consumer :selectors])
                      {:phase :namespace-load :outcome :failed})]
    (is (= :pre-test-failure (:classification (selection/attribute-result p result))))
    (is (= :reported-test-result
           (:classification (selection/attribute-result p (assoc result :phase :test)))))
    (is (= :setup-not-acceptance
           (reason #(selection/attribute-result p (assoc result :outcome :passed)))))
    (is (= :result-source
           (reason #(selection/attribute-result p (dissoc result :source)))))
    (is (= :result-selection
           (reason #(selection/attribute-result p (assoc result :consumer 'wrong-owner)))))
    (is (= :result-selection
           (reason #(selection/attribute-result p (assoc result :selectors ['wrong-test])))))
    (is (= :result-phase
           (reason #(selection/attribute-result p (assoc result :phase :synthetic-model)))))
    (is (= :unverified-reported-result
           (:authority (selection/attribute-result p result))))))
