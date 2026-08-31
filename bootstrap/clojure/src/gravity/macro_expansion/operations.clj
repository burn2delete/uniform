(ns gravity.macro-expansion.operations
  (:require [gravity.digest :as digest]))

(defn fail-default!
  [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn form-op?
  [op form]
  (and (seq? form) (= op (first form))))

(declare contains-form-op?)

(defn contains-form-op?
  [op form]
  (cond
    (form-op? op form) true
    (seq? form) (some #(contains-form-op? op %) form)
    (coll? form) (some #(contains-form-op? op %) form)
    :else false))

(defn collect-symbols
  [form]
  (cond
    (symbol? form) [form]
    (seq? form) (mapcat collect-symbols form)
    (coll? form) (mapcat collect-symbols form)
    :else []))

(defn local-macro-symbol
  [module name]
  (symbol (str (:module module)) (str name)))

(defn source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(defn default-ops
  []
  {:fail! fail-default!
   :form-op? form-op?
   :contains-form-op? contains-form-op?
   :collect-symbols collect-symbols
   :local-macro-symbol local-macro-symbol
   :source-span source-span
   :sha256-hex digest/sha256-hex
   :splice-key :gravity.macro-expansion/splice
   :max-macro-expansion-depth 16})

(defn operation
  [ops key fallback]
  (get ops key fallback))

(defn fail!
  [ops id message data]
  ((operation ops :fail! fail-default!) id message data))
