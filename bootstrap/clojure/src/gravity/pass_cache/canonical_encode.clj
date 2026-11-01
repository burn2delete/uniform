(ns gravity.pass-cache.canonical-encode
  "Canonical semantic cache encoding and content identities."
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
  [value]
  (object-metadata! value)
  (cond
    (nil? value) [:nil]
    (true? value) [:boolean true]
    (false? value) [:boolean false]
    (string? value) [:string value]
    (char? value) [:character (int value)]
    (keyword? value) [:keyword (namespace value) (name value)]
    (symbol? value) [:symbol (namespace value) (name value)]
    (integral-tag value) [:integer (integral-tag value) (str value)]
    (ratio? value) [:ratio (str (numerator value)) (str (denominator value))]
    (instance? BigDecimal value) [:bigdecimal (str value)]
    (instance? Double value) (do (finite-double! value)
                                 (let [encoded (Long/toString
                                                (Double/doubleToRawLongBits value))]
                                   [:double encoded]))
    (instance? Float value) (do (finite-double! value)
                                (let [encoded (Integer/toString
                                               (Float/floatToRawIntBits value))]
                                  [:float encoded]))
    (instance? UUID value) [:uuid (str value)]
    (instance? Date value) [:date (.getTime ^Date value)]
    (byte-array? value) [:bytes (.encodeToString (Base64/getEncoder) ^bytes value)]
    (map? value)
    (let [entries (mapv (fn [[key item]]
                          [(canonical-node key)
                           (canonical-node item)])
                        value)]
      [:map (canonical-sort entries)])
    (set? value) [:set (canonical-sort (mapv canonical-node value))]
    (vector? value) [:vector (mapv canonical-node value)]
    (seq? value)
    [:list (mapv canonical-node value)]
    :else
    (fail! "C16-KEY" "unsupported value in semantic cache identity"
           {:value-class (.getName (class value))})))

(defn canonical-bytes
  ([value] (canonical-bytes value nil))
  ([value byte-limit]
   (let [node (canonical-node value)
         text (pr-str node)
         bytes (.getBytes text StandardCharsets/UTF_8)]
     (when (and byte-limit (> (alength bytes) byte-limit))
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
