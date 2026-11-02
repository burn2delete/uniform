(ns gravity.p15-native-plan-specialization.contract
  (:require [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]))

(def max-plan-instructions 128)
(def max-generated-source-bytes 65536)
(def max-reference-output-bytes 8192)
(def max-scalar-bytes 1024)
(def max-helper-source-bytes 65536)
(def helper-source-relative
  "bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity")
(def helper-function 'p15-s23-native-c-emit-plan)
(def helper-contract :p15-s23-native-plan-c-emitter-v1)
(def helper-function-shape
  {:function helper-function
   :arity 1
   :params ['request]})
(def helper-source-content-hash
  "sha256:04645a82e66d024c6505ea3ec80c9789a7d0545ef8e7222c5d78cad68fc92adc")

(defn utf8-bytes
  [value]
  (.getBytes ^String (str value) StandardCharsets/UTF_8))

(defn printable-ascii-string?
  [value]
  (and (string? value)
       (every? (fn [character]
                 (<= 0x20 (int character) 0x7e))
               value)
       ;; C11 recognizes trigraphs before parsing string escapes. The helper
       ;; delegates literal spelling to the stage0 `pr-str` primitive.
       (not (re-find #"\?\?[=/'()!<>-]" value))))

(defn helper-scalar-safe?
  [instruction]
  (case (:op instruction)
    :literal (printable-ascii-string? (:value instruction))
    :quote (printable-ascii-string? (:value instruction))
    :builtin-call
    (and (= 'str (:function instruction))
         (seq (:args instruction))
         (every? helper-scalar-safe? (:args instruction)))
    false))

(defn helper-statement-safe?
  [instruction]
  (case (:op instruction)
    :literal true
    :quote true
    :println (every? helper-scalar-safe? (:args instruction))
    :do (every? helper-statement-safe? (:body instruction))
    false))

(defn helper-safety-proof
  [plan helper-contract-value]
  (let [entrypoint (:entrypoint plan)
        entry-function (get-in plan [:functions entrypoint])
        instructions (:instructions entry-function)
        safe? (and (vector? instructions)
                   (every? helper-statement-safe? instructions))
        facts {:contract helper-contract-value
               :proof :printable-ascii-string-println-str
               :non-ascii-allowed? false
               :control-allowed? false
               :nul-allowed? false
               :c11-trigraph-sequence-allowed? false}]
    {:safe? safe?
     :facts (if safe?
              facts
              (assoc facts
                     :missing-fact
                     :gravity-c-emitter-printable-ascii-subset
                     :observed-instructions instructions))}))

(defn helper-function-semantic-hash
  [definition]
  (str "sha256:"
       (bootstrap/sha256-hex
        (pr-str (bootstrap/c-backend-canonical-value definition)))))

(defn helper-contract-hash
  [source-content-hash function-shape contract]
  (str "sha256:"
       (bootstrap/sha256-hex
        (pr-str
         (bootstrap/c-backend-canonical-value
          {:source-content-hash source-content-hash
           :function-shape function-shape
           :contract contract})))))

(defn scalar-value?
  [value]
  (or (nil? value)
      (boolean? value)
      (string? value)
      (number? value)
      (char? value)
      (keyword? value)
      (symbol? value)))

(defn scalar-bound!
  [source-path value maximum-scalar-bytes bounds-fail!]
  (when (and (scalar-value? value)
             (> (alength (utf8-bytes (if (nil? value) "nil" value)))
                maximum-scalar-bytes))
    (bounds-fail! source-path
                  "native plan scalar exceeds the bounded value size"
                  {:maximum-scalar-bytes maximum-scalar-bytes
                   :observed-scalar-bytes
                   (alength (utf8-bytes (if (nil? value) "nil" value)))
                   :missing-fact :bounded-native-scalar})))
