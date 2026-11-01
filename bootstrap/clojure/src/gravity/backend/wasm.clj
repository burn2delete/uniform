(ns gravity.backend.wasm
  "Wasm scalar byte emission and canonical integer data codecs; no input admission.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {block-order {:arglists ([mir function]), :admission :none},
  canonical-bounded-octet!
  {:arglists ([fail! value missing-fact]), :admission :none},
  comparison-opcodes {:kind :constant, :admission :none},
  constant-i32 {:arglists ([operation]), :admission :none},
  decode-s32
  {:arglists
   ([canonical-bounded-octet!
     parser-s32-bytes
     fail!
     bytes
     offset
     limit]),
   :admission :none},
  decode-u32
  {:arglists
   ([canonical-bounded-octet!
     parser-u32-bytes
     fail!
     bytes
     offset
     limit]),
   :admission :none},
  evaluate
  {:arglists
   ([constant-i32
     forwarded?
     {:keys [function block-order operations]}]),
   :admission :none},
  forwarded? {:arglists ([opcode]), :admission :none},
  function-bytes
  {:arglists
   ([operation-stream
     local
     fail!
     {:keys [function block-order operations operation-index]}]),
   :admission :none},
  local {:arglists ([u32-leb opcode index]), :admission :none},
  operation-bytes
  {:arglists
   ([s32-leb
     constant-i32
     forwarded?
     local
     comparison-opcodes
     fail!
     operation
     operations-by-id
     operation-index]),
   :admission :none},
  operation-sequence
  {:arglists ([_mir function block-order]), :admission :none},
  operation-stream
  {:arglists ([operation-bytes operations all-by-id operation-index]),
   :admission :none},
  parser-s32-bytes {:arglists ([value]), :admission :none},
  parser-u32-bytes {:arglists ([value]), :admission :none},
  reconstruct
  {:arglists ([u32-leb function-bytes section evaluate preflight]),
   :admission :none},
  s32-leb {:arglists ([value]), :admission :none},
  section {:arglists ([u32-leb id payload]), :admission :none},
  u32-leb {:arglists ([value]), :admission :none}},
 :supported
 [:existing-scalar-core-module-emission
  :single-block-and-diamond-data
  :canonical-u32-s32-leb-decoding
  :octet-and-target-width-validation
  :unchanged-source-associations-and-identity
  :compatibility-var-interposition],
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D6 D8 D9 C1 C11 C14 C15 B1 B4 B13 B14 TEST2],
 :unsupported
 [:untrusted-emitter-ingress
  :general-mir-validation
  :b1-c11-admission
  :source-authority-or-proof-authentication
  :complete-instruction-or-module-verification
  :host-or-native-execution
  :imports
  :memory
  :tables
  :wasi
  :wit
  :components
  :toolchain-or-abi-verification
  :publication
  :full-b4-conformance
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:explicit-operation-function-and-order-data
  :operation-index
  :unsigned-and-signed-integers-in-caller-owned-target-range
  :byte-vectors-and-offset-limits
  :explicit-compatibility-operations],
 :namespace gravity.backend.wasm,
 :diagnostic-ownership
 {:stage
  ["B1-UNSUPPORTED"
   :unsupported-scalar-emission
   "B1-INPUT"
   :conditional-join-binding
   "B4-MANIFEST"
   :octet-canonical-leb-and-target-width],
  :existing-owner
  [:diagnostic-construction-and-publication :input-admission]},
 :artifact-outputs
 [:wasm32-core-module-bytes
  :reconstruction-data
  :function-and-operation-bytes
  :canonical-leb-bytes
  :decoded-u32-or-s32-and-next-offset
  :octet-or-refusal],
 :issues-certificates? false,
 :contract-boundary
 :wasm32-scalar-emission-and-canonical-integer-codecs}
))

(defn u32-leb [value]
  (loop [value value result []]
    (let [next (quot value 128)
          byte (mod value 128)]
      (if (zero? next)
        (conj result byte)
        (recur next (conj result (+ byte 128)))))))

(defn s32-leb [value]
  (loop [value value result []]
    (let [q (quot value 128)
          r (- value (* q 128))
          [q r] (if (neg? r) [(dec q) (+ r 128)] [q r])
          done? (or (and (zero? q) (< r 64))
                    (and (= -1 q) (>= r 64)))]
      (if done?
        (conj result r)
        (recur q (conj result (+ r 128)))))))

(defn section [u32-leb id payload]
  (vec (concat [id] (u32-leb (count payload)) payload)))

(defn block-order [mir function]
  (let [entry (:entry function)
        blocks (:blocks function)]
    (if (= 1 (count blocks))
      [entry]
      [entry
       (first (get-in blocks [entry :successors]))
       (second (get-in blocks [entry :successors]))
       (get-in mir [:control-flow-graph :join :block-id])])))

(defn operation-sequence [_mir function block-order]
  (vec (mapcat #(get-in function [:blocks % :instructions]) block-order)))

(defn constant-i32 [operation]
  (let [value (get-in operation [:constant-payload :value])]
    (cond (= :gravity/bool (:type operation)) (if value 1 0)
          (= :gravity/nil (:type operation)) 0
          :else value)))

(defn local [u32-leb opcode index]
  (vec (concat [opcode] (u32-leb index))))

(defn forwarded? [opcode]
  (contains? #{:local :local-binding :sequence :lexical-scope
               :function-boundary} opcode))

(def comparison-opcodes
  {:integer-eq 0x46 :integer-lt 0x48 :integer-lte 0x4c
   :integer-gt 0x4a :integer-gte 0x4e})(defn operation-bytes
  [s32-leb constant-i32 forwarded? local comparison-opcodes fail! operation operations-by-id operation-index]
  (let [opcode (:opcode operation)
        operands (:operands operation)
        value-bytes
        (cond
          (= :constant opcode)
          (vec (concat [0x41]
                       (s32-leb
                        (constant-i32 operation))))

          (forwarded? opcode)
          (local
           0x20 (get operation-index (last operands)))

          (= :truthiness opcode)
          (let [operand (get operations-by-id (first operands))]
            (cond (= :gravity/bool (:type operand))
                  (local
                   0x20 (get operation-index (:op-id operand)))
                  (= :gravity/nil (:type operand)) [0x41 0]
                  :else [0x41 1]))

          (contains? @comparison-opcodes opcode)
          (vec (concat
                (local
                 0x20 (get operation-index (first operands)))
                (local
                 0x20 (get operation-index (second operands)))
                [(get @comparison-opcodes opcode)]))

          :else
          (fail!
           "B1-UNSUPPORTED" "<b4-wasm>" operation
           {:missing-fact :independent-operation-emission
            :opcode opcode :operation-id (:op-id operation)}))]
    (vec (concat value-bytes
                 (local
                  0x21 (get operation-index (:op-id operation)))))))

(defn operation-stream
  [operation-bytes operations all-by-id operation-index]
  (vec (mapcat #(operation-bytes
                 % all-by-id operation-index)
               operations)))

(defn function-bytes
  [operation-stream local fail! {:keys [function block-order operations operation-index]}]
  (let [blocks (:blocks function)
        by-id (into {} (map (juxt :op-id identity) operations))]
    (if (= 1 (count block-order))
      (let [block (get blocks (first block-order))
            return-id (first (get-in block [:terminator :operands]))]
        (vec (concat
              (operation-stream
               operations by-id operation-index)
              (local
               0x20 (get operation-index return-id)))))
      (let [[entry-id then-id else-id join-id] block-order
            entry (get blocks entry-id)
            then-block (get blocks then-id)
            else-block (get blocks else-id)
            join-block (get blocks join-id)
            join-op (first (filter #(= :conditional-join (:opcode %))
                                   (:instructions join-block)))
            remaining-join (vec (remove #(= :conditional-join (:opcode %))
                                        (:instructions join-block)))
            condition-id (first (get-in entry [:terminator :operands]))
            then-value (first (get-in then-block [:terminator :operands]))
            else-value (first (get-in else-block [:terminator :operands]))
            return-id (first (get-in join-block [:terminator :operands]))]
        (when-not (and join-op
                       (= [condition-id then-value else-value]
                          (:operands join-op)))
          (fail!
           "B1-INPUT" "<b4-wasm>" (or join-op {})
           {:missing-fact :exact-conditional-join-binding}))
        (vec
         (concat
          (operation-stream
           (:instructions entry) by-id operation-index)
          (local 0x20 (get operation-index condition-id))
          [0x04 0x7f]
          (operation-stream
           (:instructions then-block) by-id operation-index)
          (local 0x20 (get operation-index then-value))
          [0x05]
          (operation-stream
           (:instructions else-block) by-id operation-index)
          (local 0x20 (get operation-index else-value))
          [0x0b]
          (local 0x21 (get operation-index (:op-id join-op)))
          (operation-stream
           remaining-join by-id operation-index)
          (local 0x20 (get operation-index return-id))))))))

(defn evaluate
  [constant-i32 forwarded? {:keys [function block-order operations]}]
  (let [by-id (into {} (map (juxt :op-id identity) operations))
        values
        (reduce
         (fn [values operation]
           (let [opcode (:opcode operation)
                 operands (:operands operation)
                 value
                 (cond
                   (= :constant opcode)
                   (constant-i32 operation)
                   (forwarded? opcode)
                   (get values (last operands))
                   (= :truthiness opcode)
                   (let [operand (get by-id (first operands))]
                     (cond (= :gravity/nil (:type operand)) 0
                           (= :gravity/bool (:type operand))
                           (get values (:op-id operand))
                           :else 1))
                   (= :integer-eq opcode)
                   (if (= (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-lt opcode)
                   (if (< (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-lte opcode)
                   (if (<= (get values (first operands))
                           (get values (second operands))) 1 0)
                   (= :integer-gt opcode)
                   (if (> (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-gte opcode)
                   (if (>= (get values (first operands))
                           (get values (second operands))) 1 0)
                   (= :conditional-join opcode)
                   (if (zero? (get values (first operands)))
                     (get values (last operands))
                     (get values (second operands))))]
             (assoc values (:op-id operation) value)))
         {} operations)
        return-id (first (get-in function
                                 [:blocks (last block-order)
                                  :terminator :operands]))]
    (get values return-id)))

(defn reconstruct [u32-leb function-bytes section evaluate preflight]
  (let [operations (:operations preflight)
        body (vec (concat
                   [1]
                   (u32-leb (count operations))
                   [0x7f]
                   (function-bytes preflight)
                   [0x0b]))
        code-payload (vec (concat [1]
                                  (u32-leb (count body))
                                  body))
        bytes (vec (concat
                    [0 0x61 0x73 0x6d 1 0 0 0]
                    (section
                     1 [1 0x60 0 1 0x7f])
                    (section 3 [1 0])
                    (section
                     7 [1 4 0x6d 0x61 0x69 0x6e 0 0])
                    (section 10 code-payload)))
        result (evaluate preflight)]
    {:artifact :gravity/b4-independent-wasm32-reconstruction
     :target :wasm32-unknown-unknown
     :target-kind :core-module :features #{}
     :abi {:parameters [] :result :i32}
     :operation-count (count operations)
     :operation-opcodes (into {} (map (juxt :op-id :opcode) operations))
     :block-order (:block-order preflight)
     :operation-index (:operation-index preflight)
     :wasm-bytes bytes :expected-result result
     :imports [] :exports [{:name "main" :kind :function :index 0}]
     :memory nil :table nil :globals [] :start nil :data []
     :custom-sections [] :runtime-helpers []
     :component-model? false :wit? false :wasi? false}))

(defn parser-u32-bytes [value]
  (loop [value value result []]
    (let [q (quot value 128)
          r (mod value 128)]
      (if (zero? q)
        (conj result r)
        (recur q (conj result (bit-or r 0x80)))))))

(defn parser-s32-bytes [value]
  (loop [value value result []]
    (let [q0 (quot value 128)
          r0 (- value (* q0 128))
          q (if (neg? r0) (dec q0) q0)
          r (if (neg? r0) (+ r0 128) r0)
          done? (or (and (zero? q) (< r 64))
                    (and (= -1 q) (>= r 64)))]
      (if done?
        (conj result r)
        (recur q (conj result (bit-or r 0x80)))))))

(defn canonical-bounded-octet!
  [fail! value missing-fact]
  (when-not (and (integer? value) (<= 0 value 255))
    (fail!
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact missing-fact}))
  (long value))(defn decode-u32 [canonical-bounded-octet! parser-u32-bytes fail! bytes offset limit]
  (loop [offset offset shift 0 value 0 raw []]
    (when (or (>= offset limit) (>= (count raw) 5))
      (fail!
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :canonical-bounded-u32-leb}))
    (let [byte (canonical-bounded-octet!
                (nth bytes offset) :canonical-bounded-u32-leb)
          low (bit-and byte 0x7f)
          value (+ value (bit-shift-left low shift))
          raw (conj raw byte)]
      (if (zero? (bit-and byte 0x80))
        (do
          (when-not (and (<= 0 value 0xffffffff)
                         (or (< shift 28) (<= low 15))
                         (= raw (parser-u32-bytes value)))
            (fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :noncanonical-u32-leb}))
          [value (inc offset)])
        (recur (inc offset) (+ shift 7) value raw)))))

(defn decode-s32 [canonical-bounded-octet! parser-s32-bytes fail! bytes offset limit]
  (loop [offset offset shift 0 value 0 raw []]
    (when (or (>= offset limit) (>= (count raw) 5))
      (fail!
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :canonical-bounded-s32-leb}))
    (let [byte (canonical-bounded-octet!
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
                         (= raw (parser-s32-bytes signed)))
            (fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :noncanonical-or-overflowing-s32-leb}))
          [signed (inc offset)])))))


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
