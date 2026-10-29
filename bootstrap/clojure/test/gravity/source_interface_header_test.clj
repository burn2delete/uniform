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

(deftest source-successor-discriminants-do-not-change-generic-owner-versions
  (doseq [interface [:gravity/c11-mir :gravity/c6-c10]
          version [2 3]]
    (is (= version (header/source-interface-version!
                   {:interface interface :version version} interface))))
  (is (= :interface-version-class-or-value
         (reason #(header/supported-version! 3))))
  (is (= :interface-version-class-or-value
         (reason #(header/interface-version! {:interface :gravity/c11-mir :version 3}))))
  (doseq [version [(int 3) 3N 3.0 nil 0 4]]
    (is (= :interface-version-class-or-value
           (reason #(header/source-interface-version!
                     {:interface :gravity/c11-mir :version version}))))))

(deftest successor-source-header-hostile-values-have-zero-callbacks
  (let [calls (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! calls inc) {})
                  (equals [_] (swap! calls inc) false)
                  (hashCode [] (swap! calls inc) 1)
                  (toString [] (swap! calls inc) "foreign"))]
    (doseq [[value expected]
            [[foreign :interface-map-class]
             [{:interface foreign :version 3} :interface-id]
             [{:interface :gravity/c11-mir :version foreign} :interface-version-class-or-value]
             [(clojure.lang.PersistentArrayMap.
               (object-array [foreign 3 :version 3])) :interface-header-key-class]
             [(with-meta {:interface :gravity/c11-mir :version 3} {:extra foreign})
              :interface-header-metadata]
             [(clojure.lang.PersistentArrayMap.
               (object-array [:version 3 :version 3])) :interface-header-keyset]]]
      (is (= expected (reason #(header/source-interface-version! value)))))
    (is (zero? @calls))))
