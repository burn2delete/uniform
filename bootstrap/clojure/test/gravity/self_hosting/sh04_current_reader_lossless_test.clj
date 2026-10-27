(ns gravity.self-hosting.sh04-current-reader-lossless-test
  "Existing exact/deferred ratio products through the current Reader/C3 route."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- path [extension]
  (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-04/current-reader-lossless" extension))))
(defn- reader [] (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)))
(defn- invocation [] {:reader-source-path (reader)
                     :authenticated-envelope-options {:source :development-current}})
(defn- emitter []
  @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter)))

(deftest current-reader-lossless-and-deferred-ratios-retain-authenticated-seed-spans
  (binding [b/*p15-s23-stage2-compiler-artifact-binding* (emitter)]
    (let [source (path ".gravity") text (slurp source :encoding "UTF-8")
          ;; Pair bytes are checked; only the .gravity C3 composition is executed.
          _ (is (= text (slurp (path ".qst") :encoding "UTF-8")))
          artifact (b/compiler-c3-syntax-source-artifact
                     source text (b/reader-project-context-for-source source)
                     nil nil (reader) (invocation))
          proof (b/c3-syntax-capability-proof artifact (invocation))
          c2 (:c2-reader-artifact artifact)
          seeds (:syntax-seed-stream c2)
          nodes (filterv #(not= :generated-form (get-in % [:form :kind])) (:syntax-object-stream artifact))
          descriptors (mapv #(get-in % [:form :value]) nodes)
          deferred (get-in c2 [:semantic-error-deferment-record :deferred-literal-records])]
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact)))
      (is (= :complete (:status proof)))
      (is (true? (:reader-products-authentic? proof)))
      (is (true? (:syntax-verifier-current? proof)))
      (is (= :development-unadmitted
             (get-in artifact [:gravity-syntax-boundary :authenticated-envelope :source-rule :admission-status])))
      (is (= 2 (count nodes) (count seeds)))
      (is (= [:ratio :ratio] (mapv #(get-in % [:form :kind]) nodes)))
      (is (= [:gravity/ratio-literal :gravity/deferred-ratio-literal] (mapv :artifact descriptors)))
      (is (= [:accepted :deferred] (mapv :semantic-validation descriptors)))
      (is (= ["1/2" "1/0"] (mapv :raw descriptors)))
      (is (= ["1" "1"] (mapv :numerator-spelling descriptors)))
      (is (= ["2" "0"] (mapv :denominator-spelling descriptors)))
      (is (= [1N 1N] (mapv :numerator descriptors)))
      (is (= [2N 0N] (mapv :denominator descriptors)))
      (is (= :zero-denominator (:reason (second descriptors))))
      (is (= ["1/0"] (mapv :raw deferred)))
      (is (= (:value (first deferred)) (second descriptors)))
      (doseq [[seed node descriptor expected] (map vector seeds nodes descriptors [[0 3] [4 7]])]
        (is (= (:form-id seed) (get-in node [:source :form-id])))
        (is (= expected ((juxt :scalar-start :scalar-end) (:span seed))))
        (is (= expected ((juxt :scalar-start :scalar-end) (get-in node [:span :primary]))))
        (is (= source (get-in node [:span :primary :source])))
        (is (= descriptor
               (:reader-literal-descriptor
                 (:value (first (filter #(= :authenticated-reader-products (:fact-kind %)) (:facts node))))))))
      (doseq [changed [(assoc-in artifact [:syntax-object-stream 0 :form :value :denominator] 3N)
                       (assoc-in artifact [:syntax-object-stream 1 :form :value :semantic-validation] :accepted)
                       (update-in artifact [:c2-reader-artifact :syntax-seed-stream 0 :span :scalar-end] inc)]]
        (let [proof (b/c3-syntax-capability-proof changed (invocation))]
          (is (= :failed (:status proof)))
          (is (false? (:reader-products-authentic? proof)))
          (is (false? (:syntax-verifier-current? proof))))))))
