(ns gravity.types.ordinary-test-support
  "Pure parser and equation support for inert ordinary-source tests.

  This namespace has no test vars and does not load Gravity definitions,
  adapters, or project models. Historical source revisions remain explicit
  arguments so each consumer can retain its own provenance pin."
  (:require [clojure.java.shell :as shell]
            [clojure.walk :as walk]))

(def owners [["sh08" "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"]
             ["body" "bootstrap/gravity/src/gravity/effects.gravity"]
             ["p15-s23" "bootstrap/gravity/p15_s23/emitter.gravity"]])

(defn before
  [base path]
  (let [r (shell/sh "git" "show" (str base ":" path))]
    (assert (zero? (:exit r)))
    (:out r)))

(defn read-forms
  [text]
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop [out []]
        (let [x (read {:eof ::end} r)]
          (if (= ::end x)
            out
            (recur (conj out x))))))))

(defn definitions
  [text]
  (reduce (fn [out x]
            (if (and (seq? x) (contains? '#{defn defn-} (first x)))
              (assoc out (second x) x)
              out))
          {}
          (read-forms text)))

(defn nodes
  [x]
  (tree-seq coll? seq x))

(def old-width
  '[(vector? (get parameter-form :value))
    (= (count names) (count (get parameter-form :value)))])

(def width
  '(let [values (get parameter-form :value)]
     (if (vector? values)
       (= (count names) (count values))
       false)))

(def correspondence
  '(let [values (get parameter-form :value)]
     (loop [i 0]
       (if (= i (count names))
         true
         (let [form (get forms (get names i)) value (get values i)]
           (if (map? form)
             (let [kind (get form :kind) original (get form :value)]
               (if (if (= :symbol kind)
                     (if (symbol? original)
                       (if (symbol? value) (= original value) false)
                       false)
                     (if (= :keyword kind)
                       (if (keyword? original)
                         (if (keyword? value) (= original value) false)
                         false)
                       false))
                 (recur (+ i 1))
                 false))
             false))))))

(def failure
  {:status :rejected :reason :private-map-keys-source-mismatch})

(defn expected
  [old]
  (let [accepted (filter #(and (map? %) (= :accepted (:status %))) (nodes old))]
    (assert (= 1 (count accepted)))
    (assert (= 1 (count (for [x (nodes old) :when (vector? x)
                              pair (partition 2 1 x)
                              :when (= old-width (vec pair))]
                          pair))))
    (assert (some #(and (map? %)
                        (= :rejected (:status %) (:status failure))
                        (= :private-map-keys-source-mismatch (:reason %)))
                  (nodes old)))
    (walk/postwalk
     (fn [x]
       (cond (= x (first accepted)) (list 'if correspondence x failure)
             (vector? x) (loop [xs (seq x) out []]
                           (if-not xs
                             out
                             (if (= old-width (vec (take 2 xs)))
                               (recur (nnext xs) (conj out width))
                               (recur (next xs) (conj out (first xs))))))
             :else x))
     old)))

(defn erase
  [new]
  (walk/postwalk
   (fn [x]
     (cond (and (seq? x) (= 'if (first x)) (= correspondence (second x)))
           (nth x 2)
           (and (vector? x) (some #{width} x))
           (vec (mapcat #(if (= width %) old-width [%]) x))
           :else x))
   new))

(defn replace-data
  [x old new]
  (walk/postwalk-replace {old new} x))

(defn without-accounting
  [form]
  (let [drop-base (fn [bindings]
                    (vec (mapcat (fn [[name value]]
                                   (if (= 'base name) [] [name value]))
                                 (partition 2 bindings))))
        strip (walk/postwalk
               (fn [x]
                 (if (map? x) (dissoc x :work-count) x))
               form)
        args (nth strip 2)
        body (walk/postwalk
              (fn [x]
                (if (and (seq? x) (= 'if (first x))
                         (= '(> base budget) (second x)))
                  (nth x 3)
                  x))
              (nth strip 3))
        body (if (and (seq? body) (= 'let (first body))
                      (vector? (second body)))
               (apply list (assoc (vec body) 1 (drop-base (second body))))
               body)]
    (apply list (assoc (vec strip) 2 (vec (butlast args)) 3 body))))

(defn expected-for
  [old]
  (without-accounting (expected old)))
