(ns gravity.pass-cache.canonical-encode
  "Canonical, bounded semantic cache encoding and content identities."
  (:require [gravity.digest :as digest]
            [gravity.pass-cache.canonical-scalar :refer :all]
            [gravity.pass-cache.policy :refer :all])
  (:import [java.math BigDecimal]
           [java.nio.charset StandardCharsets]
           [java.util Base64 Date UUID]))

(declare canonical-node)

(defn canonical-sort
  [nodes]
  (let [with-text (mapv (fn [node] [node (pr-str node)]) nodes)]
    (mapv first (sort-by second with-text))))

(defn canonical-node
  [value state depth]
  (when (> depth maximum-depth)
    (fail! "C16-KEY" "canonical value exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  (object-metadata! value)
  (account-node! state)
  ;; Account the tagged-node/container syntax independently of node count.
  ;; 64 bytes conservatively covers every fixed tag and bracket/space
  ;; scaffold in this closed tagged-node grammar (including :biginteger).
  (account-bytes! state 64)
  (cond
    (nil? value) (do (account-bytes! state 3) [:nil])
    (true? value) (do (account-bytes! state 4) [:boolean true])
    (false? value) (do (account-bytes! state 5) [:boolean false])
    (string? value) (do (account-text! state value)
                        [:string value])
    (char? value) (do (account-text! state (int value))
                      [:character (int value)])
    (keyword? value) (do (account-text! state (name value))
                         (when (namespace value)
                           (account-text! state (namespace value)))
                         [:keyword (namespace value) (name value)])
    (symbol? value) (do (account-text! state (name value))
                         (when (namespace value)
                           (account-text! state (namespace value)))
                         [:symbol (namespace value) (name value)])
    (integral-tag value) (do (account-integral-text! state :integer value)
                             [:integer (integral-tag value) (str value)])
    (ratio? value) (do (account-integral-text! state :ratio-numerator
                                               (numerator value))
                       (account-integral-text! state :ratio-denominator
                                               (denominator value))
                       [:ratio (str (numerator value)) (str (denominator value))])
    (instance? BigDecimal value) (do (account-bigdecimal-text! state value)
                                     [:bigdecimal (str value)])
    (instance? Double value) (do (finite-double! value)
                                 (let [encoded (Long/toString
                                                (Double/doubleToRawLongBits value))]
                                   (account-text! state encoded)
                                   [:double encoded]))
    (instance? Float value) (do (finite-double! value)
                                (let [encoded (Integer/toString
                                               (Float/floatToRawIntBits value))]
                                  (account-text! state encoded)
                                  [:float encoded]))
    (instance? UUID value) (do (account-text! state value) [:uuid (str value)])
    (instance? Date value) (do (account-text! state (.getTime ^Date value))
                               [:date (.getTime ^Date value)])
    (byte-array? value) (do
                          (when (> (alength ^bytes value)
                                   (quot (* maximum-canonical-bytes 3) 4))
                            (fail! "C16-KEY"
                                   "byte-array exceeds canonical preflight bound"
                                   {:maximum-bytes maximum-canonical-bytes}))
                          (let [encoded (.encodeToString (Base64/getEncoder)
                                                          ^bytes value)]
                            (account-text! state encoded)
                            [:bytes encoded]))
    (map? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical map exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      (let [entries (mapv (fn [[key item]]
                            [(canonical-node key state (inc depth))
                             (canonical-node item state (inc depth))])
                          value)]
        [:map (canonical-sort entries)]))
    (set? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical set exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      [:set (canonical-sort
             (mapv #(canonical-node % state (inc depth)) value))])
    (vector? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical vector exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      [:vector (mapv #(canonical-node % state (inc depth)) value)])
    (seq? value)
    (let [items (loop [remaining (seq value) result []]
                  (when (> (count result) maximum-nodes)
                    (fail! "C16-KEY" "canonical list exceeds its cardinality bound"
                           {:maximum-cardinality maximum-nodes}))
                  (if remaining
                    (if (= (count result) maximum-nodes)
                      (fail! "C16-KEY"
                             "canonical list exceeds its cardinality bound"
                             {:maximum-cardinality maximum-nodes})
                      (recur (next remaining)
                             (conj result
                                   (canonical-node (first remaining)
                                                   state (inc depth)))))
                    result))]
      (account-bytes! state (+ 8 (count items)))
      [:list items])
    :else
    (fail! "C16-KEY" "unsupported value in semantic cache identity"
           {:value-class (.getName (class value))})))

(defn canonical-bytes
  ([value] (canonical-bytes value maximum-canonical-bytes))
  ([value byte-limit]
   (let [node (canonical-node value (atom {:nodes 0 :bytes 0
                                           :limit byte-limit}) 0)
         text (pr-str node)
         bytes (.getBytes text StandardCharsets/UTF_8)]
     (when (> (alength bytes) byte-limit)
       (fail! "C16-KEY" "canonical value exceeds its byte bound"
              {:maximum-bytes byte-limit :observed-bytes (alength bytes)}))
     bytes)))

(defn content-id
  [domain value]
  (str "sha256:" (digest/sha256-bytes-hex
                   (canonical-bytes
                    {:domain domain
                     :canonicalizer-version canonicalizer-version
                     :value value}))))

(defn canonical-text
  [node]
  (pr-str node))
