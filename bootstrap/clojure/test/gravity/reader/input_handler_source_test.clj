(ns gravity.reader.input-handler-source-test
  "Isolated source model, not Gravity execution or provider authority."
  (:require [clojure.test :refer [deftest is]]))

(defn- forms []
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader.
                       (java.io.StringReader.
                        (slurp "bootstrap/gravity/src/gravity/reader.gravity")))]
      (loop [out []]
        (let [value (read {:eof ::end} reader)]
          (if (= ::end value) out (recur (conj out value))))))))

(deftest handler-has-the-fixed-build-contract-and-one-original-builtin-occurrence
  (let [[module handler] (forms)
        options (into {} (map (fn [[k value]] [k value]) (drop 2 module)))
        calls (filter #(and (seq? %) (= 'build-read-declared-input (first %)))
                      (tree-seq coll? seq handler))]
    (is (= 'gravity.reader (second module)))
    (is (= {:profile :meta :target :jvm :exports '[read-input]
            :effects #{:build/read-file} :capabilities #{:fs/read} :safety :safe}
           options))
    (is (= '[defn read-input [seed]] (take 3 handler)))
    (is (= '((build-read-declared-input (get seed :datum))) calls))))

(deftest selected-Gravity-definition-preserves-seed-and-uses-actual-modeled-return
  (let [model (create-ns (gensym "input-handler-source-model-"))
        calls (atom []) result (atom "")]
    (try
      (binding [*ns* model]
        (clojure.core/refer 'clojure.core)
        (intern model 'build-read-declared-input
                (fn [input] (swap! calls conj input) @result))
        (eval (second (forms))))
      (let [handler (var-get (ns-resolve model 'read-input))
            seed {:source-id "source-A" :form-id "form-A" :payload-form-id "payload-A"
                  :span {:byte-start 0 :byte-end 18} :datum "schema"}]
        (doseq [returned ["" "schema text" (str (char 0x03bb))]]
          (reset! calls []) (reset! result returned)
          (is (= (assoc seed :datum returned) (handler seed)))
          (is (= ["schema"] @calls)))
        (reset! result "different return")
        (is (not= seed (handler seed))))
      (finally (remove-ns (ns-name model))))))
