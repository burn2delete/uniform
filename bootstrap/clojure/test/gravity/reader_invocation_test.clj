(ns gravity.reader-invocation-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- selection-result [selected invocation]
  ;; Observe only the constructor's host-selection gate. This sentinel replaces
  ;; product construction and supplies no artifact or authentication acceptance.
  (let [calls (atom [])
        result
        (with-redefs [b/c2-reader-products
                      (fn [& arguments]
                        (swap! calls conj (last arguments))
                        (throw (UnsupportedOperationException. "selection reached products")))]
          (try
            (b/compiler-c2-reader-source-artifact
             "invocation.gravity" "[1]" {} nil nil selected invocation)
            :unexpected-return
            (catch UnsupportedOperationException _ :products-reached)
            (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    {:result result :calls @calls}))

(deftest reader-constructor-matches-authenticated-canonical-source-paths
  (let [relative b/sh03-reader-source-relative-path
        absolute (.getCanonicalPath (io/file relative))
        invocation (fn [path]
                     {:reader-source-path path
                      :authenticated-envelope-options {:source :development-current}})]
    (doseq [[selected context] [[relative absolute] [absolute relative]
                                [relative relative] [absolute absolute]]]
      (let [result (selection-result selected (invocation context))]
        (is (= :products-reached (:result result)))
        (is (= [selected] (:calls result)))))
    (doseq [selected [nil "other-reader.gravity" "invalid\u0000reader.gravity"]]
      (let [result (selection-result selected (invocation relative))]
        (is (= "C2-HASH" (get-in result [:result :id])))
        (is (= [:matching-host-reader-invocation]
               (get-in result [:result :missing-fields])))
        (is (empty? (:calls result)))))
    (let [result (selection-result relative (invocation "other-reader.gravity"))]
      (is (= "C2-HASH" (get-in result [:result :id])))
      (is (empty? (:calls result))))))
