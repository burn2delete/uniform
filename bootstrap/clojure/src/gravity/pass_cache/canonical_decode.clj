(ns gravity.pass-cache.canonical-decode
  "Strict canonical cache decoding and EDN preflight."
  (:require [clojure.edn :as edn]
            [gravity.pass-cache.canonical-encode :refer :all]
            [gravity.pass-cache.policy :refer :all])
  (:import [java.math BigDecimal BigInteger]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction StandardCharsets]
           [java.util Base64 Date UUID]))

(defn expect-node!
  [node tag arity]
  (when-not (and (vector? node) (= arity (count node)) (= tag (first node)))
    (fail! "C16-ENTRY" "malformed canonical cache node"
           {:expected-tag tag :expected-arity arity})))

(defn sorted-node-vector!
  [field value]
  (when-not (vector? value)
    (fail! "C16-ENTRY" "canonical collection payload must be a vector"
           {:field field}))
  (let [texts (mapv canonical-text value)]
    (when-not (every? (fn [[a b]] (neg? (compare a b)))
                      (partition 2 1 texts))
      (fail! "C16-ENTRY" "canonical collection nodes are not sorted"
             {:field field})))
  value)

(defn parse-integer
  [tag text]
  (try
    (case tag
      :byte (Byte/valueOf text)
      :short (Short/valueOf text)
      :int (Integer/valueOf text)
      :long (Long/valueOf text)
      :bigint (clojure.lang.BigInt/fromBigInteger (BigInteger. text))
      :biginteger (BigInteger. text)
      (fail! "C16-ENTRY" "unknown canonical integer tag" {:tag tag}))
    (catch NumberFormatException error
      (fail! "C16-ENTRY" "canonical integer is malformed"
             {:tag tag :message (.getMessage error)}))))

(declare decode-node)

(defn decode-node
  [node state depth]
  (when (> depth maximum-depth)
    (fail! "C16-ENTRY" "decoded cache data exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  (let [nodes (swap! state inc)]
    (when (> nodes maximum-nodes)
      (fail! "C16-ENTRY" "decoded cache data exceeds its node bound"
             {:maximum-nodes maximum-nodes})))
  (when-not (and (vector? node) (keyword? (first node)))
    (fail! "C16-ENTRY" "cache data is not a canonical tagged node" {}))
  (case (first node)
    :nil (do (expect-node! node :nil 1) nil)
    :boolean (do (expect-node! node :boolean 2)
                 (when-not (boolean? (second node))
                   (fail! "C16-ENTRY" "malformed boolean node" {}))
                 (second node))
    :string (do (expect-node! node :string 2)
                (when-not (string? (second node))
                  (fail! "C16-ENTRY" "malformed string node" {}))
                (second node))
    :character (do (expect-node! node :character 2)
                   (try (char (int (second node)))
                        (catch Throwable _
                          (fail! "C16-ENTRY" "malformed character node" {}))))
    :keyword (do (expect-node! node :keyword 3)
                 (when-not (or (nil? (second node)) (string? (second node)))
                   (fail! "C16-ENTRY" "malformed keyword namespace" {}))
                 (when-not (string? (nth node 2))
                   (fail! "C16-ENTRY" "malformed keyword name" {}))
                 (if-let [ns-name (second node)]
                   (keyword ns-name (nth node 2))
                   (keyword (nth node 2))))
    :symbol (do (expect-node! node :symbol 3)
                (when-not (or (nil? (second node)) (string? (second node)))
                  (fail! "C16-ENTRY" "malformed symbol namespace" {}))
                (when-not (string? (nth node 2))
                  (fail! "C16-ENTRY" "malformed symbol name" {}))
                (if-let [ns-name (second node)]
                  (symbol ns-name (nth node 2))
                  (symbol (nth node 2))))
    :integer (do (expect-node! node :integer 3)
                 (parse-integer (second node) (nth node 2)))
    :ratio (do (expect-node! node :ratio 3)
               (try (clojure.lang.Ratio. (BigInteger. (second node))
                                          (BigInteger. (nth node 2)))
                    (catch Throwable _
                      (fail! "C16-ENTRY" "malformed ratio node" {}))))
    :bigdecimal (do (expect-node! node :bigdecimal 2)
                    (try (BigDecimal. ^String (second node))
                         (catch Throwable _
                           (fail! "C16-ENTRY" "malformed decimal node" {}))))
    :double (do (expect-node! node :double 2)
                (try (let [value (Double/longBitsToDouble
                                  (Long/parseLong (second node)))]
                      (when-not (Double/isFinite value)
                        (fail! "C16-ENTRY" "decoded double is nonfinite" {}))
                      value)
                     (catch NumberFormatException _
                       (fail! "C16-ENTRY" "malformed double node" {}))))
    :float (do (expect-node! node :float 2)
               (try (let [value (Float/intBitsToFloat
                                 (Integer/parseInt (second node)))]
                     (when-not (Float/isFinite value)
                       (fail! "C16-ENTRY" "decoded float is nonfinite" {}))
                     value)
                    (catch NumberFormatException _
                      (fail! "C16-ENTRY" "malformed float node" {}))))
    :uuid (do (expect-node! node :uuid 2)
              (try (UUID/fromString (second node))
                   (catch Throwable _
                     (fail! "C16-ENTRY" "malformed UUID node" {}))))
    :date (do (expect-node! node :date 2)
              (when-not (integer? (second node))
                (fail! "C16-ENTRY" "malformed date node" {}))
              (Date. (long (second node))))
    :bytes (do (expect-node! node :bytes 2)
               (when-not (string? (second node))
                 (fail! "C16-ENTRY" "malformed byte-array node" {}))
               (try (.decode (Base64/getDecoder) ^String (second node))
                    (catch Throwable _
                      (fail! "C16-ENTRY" "malformed byte-array node" {}))))
    :vector (do (expect-node! node :vector 2)
                (when-not (vector? (second node))
                  (fail! "C16-ENTRY" "canonical vector payload must be a vector" {}))
                (mapv #(decode-node % state (inc depth)) (second node)))
    :list (do (expect-node! node :list 2)
              (when-not (vector? (second node))
                (fail! "C16-ENTRY" "canonical list payload must be a vector" {}))
              (apply list (map #(decode-node % state (inc depth)) (second node))))
    :set (do (expect-node! node :set 2)
             (sorted-node-vector! :set (second node))
             (set (map #(decode-node % state (inc depth)) (second node))))
    :map (do (expect-node! node :map 2)
             (sorted-node-vector! :map (second node))
             (reduce (fn [result entry]
                       (when-not (and (vector? entry) (= 2 (count entry)))
                         (fail! "C16-ENTRY" "malformed canonical map entry" {}))
                       (let [key (decode-node (first entry) state (inc depth))
                             value (decode-node (second entry) state (inc depth))]
                         (when (contains? result key)
                           (fail! "C16-ENTRY" "duplicate canonical map key" {}))
                         (assoc result key value)))
                     {} (second node)))
    (fail! "C16-ENTRY" "unknown canonical cache node"
           {:node-tag (first node)})))

(defn utf8-string
  [^bytes bytes diagnostic]
  (try
    (let [decoder (.newDecoder StandardCharsets/UTF_8)]
      (.onMalformedInput decoder CodingErrorAction/REPORT)
      (.onUnmappableCharacter decoder CodingErrorAction/REPORT)
      (str (.toString (.decode decoder (ByteBuffer/wrap bytes)))))
    (catch CharacterCodingException _
      (fail! diagnostic "cache file is not valid UTF-8" {}))))

(defn preflight-edn-text!
  [^String text]
  ;; Count containers and scalar tokens before invoking the EDN reader.  A
  ;; delimiter-only scan does not bound a huge flat vector of atoms.
  (letfn [(account-node! [nodes]
            (let [next-nodes (inc nodes)]
              (when (> next-nodes maximum-nodes)
                (fail! "C16-ENTRY" "cache EDN exceeds its node bound"
                       {:maximum-nodes maximum-nodes}))
              next-nodes))]
   (loop [index 0 depth 0 nodes 0 quoted? false escaped? false
          comment? false token? false]
    (if (= index (.length text))
      (do
        (when token? (account-node! nodes))
        true)
      (let [ch (.charAt text index)]
        (cond
          comment?
          (recur (inc index) depth nodes false false
                 (not (or (= ch \newline) (= ch \return))) false)
          escaped? (recur (inc index) depth nodes true false false false)
          (and quoted? (= ch \\))
          (recur (inc index) depth nodes true true false false)
          (and quoted? (= ch \"))
          (recur (inc index) depth (account-node! nodes) false false false false)
          quoted? (recur (inc index) depth nodes true false false false)
          (= ch \;)
          (recur (inc index) depth (if token? (account-node! nodes) nodes)
                 false false true false)
          (= ch \" )
          (recur (inc index) depth (if token? (account-node! nodes) nodes)
                 true false false false)
          (#{\[ \{ \(} ch)
          (let [next-depth (inc depth)
                next-nodes (account-node!
                            (if token? (account-node! nodes) nodes))]
            (when (> next-depth maximum-depth)
              (fail! "C16-ENTRY" "cache EDN exceeds its depth bound"
                     {:maximum-depth maximum-depth}))
            (recur (inc index) next-depth next-nodes false false false false))
          (#{\] \} \)} ch)
          (recur (inc index) (max 0 (dec depth))
                 (if token? (account-node! nodes) nodes)
                 false false false false)
          (or (Character/isWhitespace ch) (= ch \,))
          (recur (inc index) depth
                 (if token? (account-node! nodes) nodes)
                 false false false false)
          :else (recur (inc index) depth nodes false false false true)))))))

(defn decode-canonical-bytes
  [^bytes bytes byte-limit]
  (when (> (alength bytes) byte-limit)
    (fail! "C16-ENTRY" "cache file exceeds its read bound"
           {:maximum-bytes byte-limit :observed-bytes (alength bytes)}))
  (let [text (utf8-string bytes "C16-ENTRY")
        _ (preflight-edn-text! text)
        node (try
               (edn/read-string
                {:readers {}
                 :default (fn [tag _]
                            (fail! "C16-ENTRY" "tagged EDN is forbidden"
                                   {:tag tag}))}
                text)
               (catch clojure.lang.ExceptionInfo error (throw error))
               (catch StackOverflowError _
                 (fail! "C16-ENTRY" "cache EDN exceeds host stack bound" {}))
               (catch Throwable error
                 (if (fatal? error)
                   (throw error)
                   (fail! "C16-ENTRY" "cache EDN is malformed"
                          {:contained-host-error (.getName (class error))}))))
        value (try (decode-node node (atom 0) 0)
                   (catch StackOverflowError _
                     (fail! "C16-ENTRY" "decoded cache data exceeds host stack bound" {})))
        canonical (canonical-bytes value byte-limit)]
    (when-not (java.util.Arrays/equals bytes canonical)
      (fail! "C16-ENTRY" "cache EDN is not in canonical form" {}))
    value))

(defn encoded-value
  [value byte-limit]
  (canonical-bytes value byte-limit))
