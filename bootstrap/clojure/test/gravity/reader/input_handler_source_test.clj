(ns gravity.reader.input-handler-source-test
  "Isolated source model, not Gravity execution or provider authority."
  (:require [clojure.test :refer [deftest is]]))

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader.
                       (java.io.StringReader.
                        (slurp path)))]
      (loop [out []]
        (let [value (read {:eof ::end} reader)]
          (if (= ::end value) out (recur (conj out value))))))))

(defn- forms []
  (source-forms "bootstrap/gravity/src/gravity/reader.gravity"))

(defn- definition [forms name]
  (nth (first (filter #(and (seq? %) (= 'def (first %))
                           (= name (second %))) forms)) 2))

(deftest stage2-builtin-declarations-match-the-complete-seed-population
  ;; Read source DATA only: no Bootstrap, Gravity compiler or runtime load.
  (let [seed (second (definition
                      (source-forms
                       "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00516-defn-form.clj")
                      'stage0-builtin-functions))
        compiler (source-forms "bootstrap/gravity/p15_s23/compiler.gravity")
        emitter (definition compiler 'p15-s23-stage2-plan-emitter)
        runtime (definition compiler 'p15-s23-stage2-runtime-executor)
        complete? (fn [functions]
                    (and (= seed (set functions))
                         (= (count functions) (count (set functions)))))]
    (is (contains? seed 'build-read-declared-input))
    (doseq [functions [(get-in emitter [:call-rules :builtin-functions])
                       (get-in runtime [:builtin-rules :functions])]]
      (is (complete? functions))
      (is (= 1 (count (filter #{'build-read-declared-input} functions))))
      (is (not (complete? (remove #{'build-read-declared-input} functions))))
      (is (not (complete? (remove #{'get} functions))))
      (is (not (complete? (conj functions 'build-read-declared-input))))
      (is (not (complete? (conj functions 'unregistered-file-read)))))))

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
