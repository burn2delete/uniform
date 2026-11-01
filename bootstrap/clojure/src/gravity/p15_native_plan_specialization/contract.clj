(ns gravity.p15-native-plan-specialization.contract
  (:require [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]))

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
  ;; Keep nested :str operands off the host call stack.  This is a safety
  ;; predicate, not a depth or work budget: every supplied operand is still
  ;; visited and checked against the same printable-ASCII contract.
  (loop [pending [instruction]]
    (if-let [current (peek pending)]
      (let [pending (pop pending)]
        (case (:op current)
          (:literal :quote)
          (if (printable-ascii-string? (:value current))
            (recur pending)
            false)

          :builtin-call
          (if (and (= 'str (:function current))
                   (seq (:args current)))
            (recur (into pending (:args current)))
            false)

          false))
      true)))

(defn helper-statement-safe?
  [instruction]
  ;; The helper subset permits arbitrarily nested statement blocks.  Traverse
  ;; those blocks explicitly so malformed or deep-but-valid plans cannot leak
  ;; a host StackOverflowError from this admission predicate.
  (loop [pending [instruction]]
    (if-let [current (peek pending)]
      (let [pending (pop pending)]
        (case (:op current)
          (:literal :quote)
          (recur pending)

          :println
          (if (every? helper-scalar-safe? (:args current))
            (recur pending)
            false)

          :do
          (recur (into pending (:body current)))

          false))
      true)))

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
