(ns gravity.types.private-map-cycle-current-test
  "Compiler-free current-owner controls for the quota-free PMK cycle premise.
  Native admission and composed compiler execution remain separate gates."
  (:require [clojure.test :refer [deftest is]]))

(def owners
  [{:prefix "sh08" :path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"}
   {:prefix "body" :path "bootstrap/gravity/src/gravity/effects.gravity"}
   {:prefix "p15-s23" :path "bootstrap/gravity/p15_s23/emitter.gravity"}])

(defn definitions [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader. (slurp path)))]
      (loop [out {}]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form)
            out
            (recur (if (and (seq? form) (= 'defn (first form)))
                     (assoc out (second form) form)
                     out))))))))

(def models
  (delay
    (mapv (fn [{:keys [prefix path]}]
            (let [space (create-ns
                         (symbol (str "gravity.types.private-map-cycle-current-"
                                      prefix)))]
              (binding [*ns* space]
                (refer 'clojure.core)
                (eval (get (definitions path)
                           (symbol (str prefix "-pmk-acyclic")))))
              {:prefix prefix :space space}))
          owners)))

(defn invoke [model & args]
  (apply (var-get (ns-resolve (:space model)
                             (symbol (str (:prefix model) "-pmk-acyclic"))))
         args))

(defn edge [caller callee]
  {:caller-function-syntax-id caller
   :callee-function-syntax-id callee})

(defn reachable-cycle? [edges root]
  (loop [pending [root] seen #{}]
    (if-let [at (peek pending)]
      (let [tail (pop pending)
            children (keep #(when (= at (:caller-function-syntax-id %))
                              (:callee-function-syntax-id %))
                           edges)]
        (if (some #{root} children)
          true
          (recur (into tail (remove seen children)) (conj seen at))))
      false)))

(deftest finite-graph-premise-matches-independent-search
  (let [vertices [:a :b :c]
        possible (vec (for [caller vertices callee vertices]
                        (edge caller callee)))]
    (doseq [mask (range 512)
            root vertices
            :let [edges (vec (keep-indexed
                              #(when (bit-test mask %1) %2)
                              possible))
                  expected (if (reachable-cycle? edges root)
                             :rejected
                             :accepted)]]
      (doseq [model @models]
        (is (= expected (:status (invoke model {:call-edges edges} root))))))))

(deftest late-cycle-shared-descendant-and-opaque-fields
  (let [vertices (mapv #(keyword (str "f" %)) (range 140))
        chain (mapv edge (butlast vertices) (rest vertices))
        callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_]
                   (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality" {})))
                 (hashCode []
                   (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash" {})))
                 (toString []
                   (swap! callbacks conj :format)
                   (throw (ex-info "opaque rendering" {}))))]
    (doseq [model @models]
      (is (= {:status :accepted}
             (invoke model {:call-edges (vec (reverse chain))}
                     (first vertices))))
      (is (= {:status :rejected
              :reason :private-map-keys-entry-domain-required}
             (invoke model
                     {:call-edges (conj chain (edge (last vertices)
                                                     (first vertices)))}
                     (first vertices))))
      (is (= {:status :accepted}
             (invoke model
                     {:call-edges [(edge :a :b) (edge :a :c)
                                   (edge :b :d) (edge :c :d)
                                   (edge :x :x) (edge :a nil)]
                      :unobserved opaque}
                     :a))))
    (is (empty? @callbacks))))
