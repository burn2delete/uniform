(ns gravity.source-interface-header-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.source-interface-header :as header]))

(defn- reason [thunk]
  (try (thunk) nil
       (catch clojure.lang.ExceptionInfo error
         (let [data (ex-data error)]
           (is (= ["C11-VERIFY" :versioned-c11-interface]
                  ((juxt :id :missing-fact) data)))
           (:reason data)))))

(deftest explicit-native-source-selectors
  (doseq [interface [:gravity/c11-mir :gravity/c6-c10]]
    (is (= 2 (header/interface-version!
              {:interface interface :version 2} interface))))
  (doseq [[value expected]
          [[{} :interface-header-keyset]
           [{:interface :gravity/c11-mir :version (int 2)}
            :interface-version-class-or-value]
           [{:interface :gravity/c11-mir :version 1} :explicit-interface-version]
           [{:interface :other :version 2} :interface-id]
           [(with-meta {:interface :gravity/c11-mir :version 2} {:extra true})
            :interface-header-metadata]]]
    (is (= expected (reason #(header/interface-version! value))))))

(deftest duplicate-native-storage-is-not-a-valid-header
  (let [value (clojure.lang.PersistentArrayMap.
               (object-array [:interface :gravity/c11-mir
                              :interface :gravity/c11-mir]))]
    (is (= :interface-header-keyset
           (reason #(header/interface-version! value))))))

(deftest foreign-values-are-rejected-before-metadata-or-equality
  (let [calls (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! calls inc) {})
                  (equals [_] (swap! calls inc) false)
                  (hashCode [] (swap! calls inc) 1)
                  (toString [] (swap! calls inc) "foreign"))]
    (is (= :interface-map-class
           (reason #(header/interface-version! foreign))))
    (is (= :interface-id
           (reason #(header/interface-version! {:interface foreign :version 2}))))
    (is (zero? @calls))))
