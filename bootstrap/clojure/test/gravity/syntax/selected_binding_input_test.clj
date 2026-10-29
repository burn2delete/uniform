(ns gravity.syntax.selected-binding-input-test
  "Fake owner ports for selected Syntax public DATA and exact export admission."
  (:require [clojure.test :refer [deftest is]]))

(def binding-path "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/sh04-development-syntax-binding.clj")
(def syntax-path "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")
(defn forms [text]
  (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
    (binding [*read-eval* false]
      (vec (take-while #(not= ::end %) (repeatedly #(read {:eof ::end} r)))))))
(defn native-data? [x]
  (cond
    (or (nil? x) (string? x) (keyword? x) (symbol? x) (number? x) (boolean? x)) true
    (#{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap} (class x))
    (every? (fn [[k v]] (and (native-data? k) (native-data? v))) x)
    (#{clojure.lang.PersistentVector clojure.lang.PersistentHashSet} (class x)) (every? native-data? x)
    :else false))
(defn with-input-ports [source-forms f]
  (let [space (create-ns (gensym "selected-binding-input-"))
        needed '#{sh04-development-syntax-contract sh04-development-syntax-diagnostics
                  sh04-development-syntax-data! sh04-development-syntax-interface!}
        definitions (filter #(and (seq? %) (#{'def 'defn-} (first %)) (needed (second %)))
                            (forms (slurp binding-path)))
        names '[c3-syntax-build-template c3-generated-syntax-template c3-syntax-verify-template
                c3-syntax-verify-resolved c3-syntax-stream-build-template c3-syntax-stream-verify-resolved
                c3-syntax-serialize-template c3-syntax-deserialize-template c3-syntax-graph-verify-template]
        shapes (zipmap names (repeat {:arity 1 :params ['fixture]}))]
    (try
      (binding [*ns* space]
        (clojure.core/refer 'clojure.core)
        (intern space 'read-forms (fn [& _] source-forms))
        (intern space 'p15-s23-trusted-carrier-validation
                (fn [x _] {:status (if (native-data? x) :passed :failed)}))
        (intern space 'sh04-syntax-boundary-fail!
                (fn [id _ reason _ _] (throw (ex-info "input refused" {:id id :reason reason}))))
        (intern space 'sh04-syntax-public-function-shapes shapes)
        (doseq [definition definitions] (eval definition)))
      (f (fn [name & args] (apply (ns-resolve space name) args)) shapes)
      (finally (remove-ns (ns-name space))))))
(defn plan [data shapes]
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:module 'gravity.bootstrap.syntax :profile :meta :target :jvm
            :effects #{} :capabilities #{} :safety :safe
            :exports (vec (concat (keys shapes) (keys data)))}})
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo e (= "C3-ID" (:id (ex-data e))))))

(deftest actual-candidate-retains-legacy-data-with-exact-twelve-exports
  (with-input-ports (forms (slurp syntax-path))
    (fn [call shapes]
      (let [data (call 'sh04-development-syntax-data! "fixture" {:source-path syntax-path :source-text "owned-port"})
            actual (nth (first (filter #(and (seq? %) (= 'def (first %)) (= 'c3-syntax-bounds (second %)))
                                      (forms (slurp syntax-path)))) 2)]
        (is (= actual (data 'c3-syntax-bounds)))
        (is (= 3 (count data)))
        (is (= 12 (count (call 'sh04-development-syntax-interface! "fixture" (plan data shapes) data {:public-function-shapes shapes}))))
        (is (rejects? #(call 'sh04-development-syntax-interface! "fixture"
                       (update-in (plan data shapes) [:module :exports] pop) data {:public-function-shapes shapes})))
        (is (rejects? #(call 'sh04-development-syntax-interface! "fixture"
                       (update-in (plan data shapes) [:module :exports] conj 'unknown) data {:public-function-shapes shapes})))
        (is (rejects? #(call 'sh04-development-syntax-interface! "fixture"
                       (assoc-in (plan data shapes) [:module :effects] #{:io}) data {:public-function-shapes shapes})))
        (is (rejects? #(call 'sh04-development-syntax-interface! "fixture"
                       (plan data shapes) data {:public-function-shapes {}})))))))
(deftest retained-eleven-export-input-needs-no-bounds-data
  (let [source (remove #(and (seq? %) (= 'def (first %)) (= 'c3-syntax-bounds (second %))) (forms (slurp syntax-path)))]
    (with-input-ports source
      (fn [call shapes]
        (let [data (call 'sh04-development-syntax-data! "fixture" {})]
          (is (= 2 (count data)))
          (is (not (contains? data 'c3-syntax-bounds)))
          (is (= 11 (count (call 'sh04-development-syntax-interface! "fixture" (plan data shapes) data {:public-function-shapes shapes})))))))))
(deftest malformed-or-opaque-data-fails-before-any-owner-construction
  (let [source (forms (slurp syntax-path)) bounds (first (filter #(and (seq? %) (= 'def (first %)) (= 'c3-syntax-bounds (second %))) source))
        without (remove #{bounds} source) callbacks (atom 0)
        opaque (proxy [Object] [] (hashCode [] (swap! callbacks inc) 1) (toString [] (swap! callbacks inc) "opaque"))]
    (doseq [changed [(conj source bounds) (conj (vec without) '(def c3-syntax-bounds []))
                     (conj (vec without) (list 'def 'c3-syntax-bounds opaque))
                     (conj (vec without) (list 'def 'c3-syntax-bounds {:opaque opaque}))
                     (conj source '(def c3-syntax-object-contract {}))]]
      (with-input-ports changed
        (fn [call _] (is (rejects? #(call 'sh04-development-syntax-data! "fixture" {}))))))
    (is (zero? @callbacks)))
  (is (nil? (find-ns 'gravity.bootstrap))))
