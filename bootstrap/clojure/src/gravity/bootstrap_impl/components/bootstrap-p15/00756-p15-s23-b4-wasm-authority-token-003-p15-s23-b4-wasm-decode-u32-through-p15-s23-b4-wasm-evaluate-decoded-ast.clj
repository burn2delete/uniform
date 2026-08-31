(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-decode-u32 [bytes offset limit]
  (loop [offset offset shift 0 value 0 raw []]
    (when (or (>= offset limit) (>= (count raw) 5))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :canonical-bounded-u32-leb}))
    (let [byte (p15-s23-b4-wasm-canonical-bounded-octet!
                (nth bytes offset) :canonical-bounded-u32-leb)
          low (bit-and byte 0x7f)
          value (+ value (bit-shift-left low shift))
          raw (conj raw byte)]
      (if (zero? (bit-and byte 0x80))
        (do
          (when-not (and (<= 0 value 0xffffffff)
                         (or (< shift 28) (<= low 15))
                         (= raw (p15-s23-b4-wasm-parser-u32-bytes value)))
            (p15-s23-b4-wasm-fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :noncanonical-u32-leb}))
          [value (inc offset)])
        (recur (inc offset) (+ shift 7) value raw)))))

(defn p15-s23-b4-wasm-decode-s32 [bytes offset limit]
  (loop [offset offset shift 0 value 0 raw []]
    (when (or (>= offset limit) (>= (count raw) 5))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :canonical-bounded-s32-leb}))
    (let [byte (p15-s23-b4-wasm-canonical-bounded-octet!
                (nth bytes offset) :canonical-bounded-s32-leb)
          low (bit-and byte 0x7f)
          next-shift (+ shift 7)
          unsigned (+ value (bit-shift-left low shift))
          continued? (pos? (bit-and byte 0x80))
          signed (if (and (not continued?)
                          (pos? (bit-and low 0x40))
                          (< next-shift 64))
                   (- unsigned (bit-shift-left 1 next-shift))
                   unsigned)
          raw (conj raw byte)]
      (if continued?
        (recur (inc offset) next-shift unsigned raw)
        (do
          (when-not (and (<= Integer/MIN_VALUE signed Integer/MAX_VALUE)
                         (= raw (p15-s23-b4-wasm-parser-s32-bytes signed)))
            (p15-s23-b4-wasm-fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :noncanonical-or-overflowing-s32-leb}))
          [signed (inc offset)])))))

(declare p15-s23-b4-wasm-parse-instructions!)

(defn p15-s23-b4-wasm-parse-instructions!
  [bytes offset limit stack initialized stop-opcodes if-count]
  (loop [offset offset stack stack initialized initialized ast []
         if-count if-count]
    (when (>= offset limit)
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :terminated-wasm-instruction-sequence}))
    (let [opcode (nth bytes offset)]
      (if (contains? stop-opcodes opcode)
        {:offset offset :stop opcode :stack stack
         :initialized initialized :ast ast :if-count if-count}
        (case opcode
          0x41
          (let [[value next]
                (p15-s23-b4-wasm-decode-s32 bytes (inc offset) limit)]
            (recur next (conj stack :i32) initialized
                   (conj ast {:op :i32.const :value value
                              :offset offset :end next}) if-count))

          0x20
          (let [[index next]
                (p15-s23-b4-wasm-decode-u32 bytes (inc offset) limit)]
            (when-not (contains? initialized index)
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :local-get-after-definite-initialization
                :operation-id index}))
            (recur next (conj stack :i32) initialized
                   (conj ast {:op :local.get :index index
                              :offset offset :end next}) if-count))

          0x21
          (let [[index next]
                (p15-s23-b4-wasm-decode-u32 bytes (inc offset) limit)]
            (when-not (= :i32 (peek stack))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :local-set-i32-stack-value
                :operation-id index}))
            (recur next (pop stack) (conj initialized index)
                   (conj ast {:op :local.set :index index
                              :offset offset :end next}) if-count))

          0x46
          (do
            (when-not (and (>= (count stack) 2)
                           (= :i32 (peek stack))
                           (= :i32 (peek (pop stack))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :i32-comparison-two-stack-operands
                :opcode opcode}))
            (recur (inc offset) (conj (pop (pop stack)) :i32)
                   initialized
                   (conj ast {:op :i32.eq :offset offset
                              :end (inc offset)}) if-count))

          0x48
          (do
            (when-not (and (>= (count stack) 2)
                           (= :i32 (peek stack))
                           (= :i32 (peek (pop stack))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :i32-comparison-two-stack-operands
                :opcode opcode}))
            (recur (inc offset) (conj (pop (pop stack)) :i32)
                   initialized
                   (conj ast {:op :i32.lt-s :offset offset
                              :end (inc offset)}) if-count))

          0x4c
          (do
            (when-not (and (>= (count stack) 2)
                           (= :i32 (peek stack))
                           (= :i32 (peek (pop stack))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :i32-comparison-two-stack-operands
                :opcode opcode}))
            (recur (inc offset) (conj (pop (pop stack)) :i32)
                   initialized
                   (conj ast {:op :i32.le-s :offset offset
                              :end (inc offset)}) if-count))

          0x4a
          (do
            (when-not (and (>= (count stack) 2)
                           (= :i32 (peek stack))
                           (= :i32 (peek (pop stack))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :i32-comparison-two-stack-operands
                :opcode opcode}))
            (recur (inc offset) (conj (pop (pop stack)) :i32)
                   initialized
                   (conj ast {:op :i32.gt-s :offset offset
                              :end (inc offset)}) if-count))

          0x4e
          (do
            (when-not (and (>= (count stack) 2)
                           (= :i32 (peek stack))
                           (= :i32 (peek (pop stack))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :i32-comparison-two-stack-operands
                :opcode opcode}))
            (recur (inc offset) (conj (pop (pop stack)) :i32)
                   initialized
                   (conj ast {:op :i32.ge-s :offset offset
                              :end (inc offset)}) if-count))

          0x04
          (do
            (when-not (zero? if-count)
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :at-most-one-structured-if}))
            (when-not (and (= :i32 (peek stack))
                           (< (inc offset) limit)
                           (= 0x7f (nth bytes (inc offset))))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :structured-if-i32-contract}))
            (let [base-stack (pop stack)
                  then-result
                  (p15-s23-b4-wasm-parse-instructions!
                   bytes (+ offset 2) limit base-stack initialized #{0x05} 1)
                  _ (when-not (and (= 0x05 (:stop then-result))
                                   (= (conj base-stack :i32)
                                      (:stack then-result)))
                      (p15-s23-b4-wasm-fail!
                       "B4-MANIFEST" "<b4-wasm>" {}
                       {:missing-fact :structured-if-then-result}))
                  else-result
                  (p15-s23-b4-wasm-parse-instructions!
                   bytes (inc (:offset then-result)) limit
                   base-stack initialized #{0x0b} 1)
                  _ (when-not (and (= 0x0b (:stop else-result))
                                   (= (conj base-stack :i32)
                                      (:stack else-result)))
                      (p15-s23-b4-wasm-fail!
                       "B4-MANIFEST" "<b4-wasm>" {}
                       {:missing-fact :structured-if-else-result}))
                  next (inc (:offset else-result))
                  common-initialized
                  (set/intersection (:initialized then-result)
                                    (:initialized else-result))]
              (recur next (conj base-stack :i32) common-initialized
                     (conj ast {:op :if-i32 :offset offset :end next
                                :then (:ast then-result)
                                :else (:ast else-result)}) 1)))

          (p15-s23-b4-wasm-fail!
           "B4-MANIFEST" "<b4-wasm>" {}
           {:missing-fact :bounded-wasm-instruction-opcode
            :opcode opcode}))))))

(declare p15-s23-b4-wasm-evaluate-decoded-ast))
