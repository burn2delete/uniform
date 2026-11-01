(ns gravity.c11-mir.source-interface
  "C11 dispatch discriminants, not source or report admission."
  (:require [gravity.source-interface-header :as header]))

(def legacy-version 1)
(def ^:private artifact-kind :gravity/p15-s23-c11-authenticated-mir-artifact)

(defn interface-version! [value]
  (header/source-interface-version! value :gravity/c11-mir))

(defn artifact-version!
  "Inspect only native top-level discriminants, never Core/MIR/report payload.
   This does not validate the artifact's complete keyset or semantic contents."
  [artifact]
  (let [[seen [kind version]]
        (reduce (fn [[seen out] [k v]]
                  (when-not (header/native-keyword? k) (header/reject! :artifact-header-key-class))
                  (let [slot (cond (identical? :kind k) 0
                                   (identical? :schema-version k) 1
                                   :else nil)]
                    (if (nil? slot) [seen out]
                      (do (when (bit-test seen slot)
                            (header/reject! :artifact-header-keyset))
                          [(bit-set seen slot) (assoc out slot v)]))))
                [0 [nil nil]] (header/pairs! artifact nil))]
    (when-not (= 3 seen) (header/reject! :artifact-header-keyset))
    (when-not (and (header/native-keyword? kind) (identical? artifact-kind kind))
      (header/reject! :artifact-header-kind))
    (header/supported-source-version! version)))

(defn require-matching-artifact-version!
  "An early dispatch check only; full typed validation/authentication still follows."
  [artifact selected-version]
  (header/supported-source-version! selected-version)
  (let [observed (artifact-version! artifact)]
    (when-not (== (.longValue ^Long selected-version) (.longValue ^Long observed))
      (header/reject! :mixed-c11-interface-versions)))
  nil)

(defn reject-recognized-v2-on-legacy!
  "Keep malformed legacy inputs on their original owning validation path.
   Only a safely recognized explicit-source discriminant is an early rejection."
  [artifact]
  (let [observed (try (artifact-version! artifact)
                      (catch clojure.lang.ExceptionInfo _ nil))]
    (when (and (some? observed)
               (or (== 2 (.longValue ^Long observed)) (== 3 (.longValue ^Long observed))))
      (header/reject! :mixed-c11-interface-versions)))
  nil)
