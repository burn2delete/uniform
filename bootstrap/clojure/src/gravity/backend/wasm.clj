(ns gravity.backend.wasm
  "Wasm scalar bytes, canonical integers and native structural verification; no admission.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {parse-instructions! {:arglists ([operations bytes offset limit stack initialized stop-opcodes if-count]), :admission :none, :operation-inputs [:decode-u32 :decode-s32 :fail! :parse-instructions! :intersection]}
  evaluate-decoded-ast {:arglists ([evaluate-recursive ast locals stack]), :admission :none, :operation-inputs [:evaluate-recursive]}
  flatten-decoded-ast {:arglists ([flatten-recursive ast]), :admission :none, :operation-inputs [:flatten-recursive]}
  parse-module! {:arglists ([operations bytes expected]), :admission :none, :operation-inputs [:decode-u32 :parse-instructions! :evaluate-decoded-ast :flatten-decoded-ast :fail! :map-invert], :data-var-inputs [:comparison-opcodes]}
  block-order {:arglists ([mir function]), :admission :none},
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
 [:native-scalar-module-structural-verification
  :decoded-stack-and-local-evaluation
  :existing-scalar-core-module-emission
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
  :explicit-compatibility-operations
  :native-expected-module-packet
  :decoded-ast-stack-locals-and-parser-state],
 :namespace gravity.backend.wasm,
 :diagnostic-ownership
 {:stage
  ["B1-UNSUPPORTED"
   :unsupported-scalar-emission
   "B1-INPUT"
   :conditional-join-binding
   "B4-MANIFEST" "B4-TARGET" "B4-IMPORT" "B4-MEMORY" "B4-EXPORT"
   :native-sections-stack-initialization-byte-map-and-result-parity
   :octet-canonical-leb-and-target-width],
  :existing-owner
  [:diagnostic-construction-and-publication :input-admission]},
 :artifact-outputs
 [:wasm32-core-module-bytes
  :reconstruction-data
  :function-and-operation-bytes
  :canonical-leb-bytes
  :decoded-u32-or-s32-and-next-offset
  :octet-or-refusal
  :decoded-instruction-ast
  :decoded-stack-and-locals
  :relative-native-module-report],
 :issues-certificates? false,
 :contract-boundary
 :wasm32-scalar-emission-codecs-and-native-structural-verification}
))

(defn- owner-function [operation]
  (if (var? operation) (deref operation) operation))

(defn parse-instructions!
  [operations bytes offset limit stack initialized stop-opcodes if-count]
  (loop [offset offset stack stack initialized initialized ast []
         if-count if-count]
    (when (>= offset limit)
      ((owner-function (:fail! operations))
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :terminated-wasm-instruction-sequence}))
    (let [opcode (nth bytes offset)]
      (if (contains? stop-opcodes opcode)
        {:offset offset :stop opcode :stack stack
         :initialized initialized :ast ast :if-count if-count}
        (case opcode
          0x41
          (let [[value next]
                ((owner-function (:decode-s32 operations)) bytes (inc offset) limit)]
            (recur next (conj stack :i32) initialized
                   (conj ast {:op :i32.const :value value
                              :offset offset :end next}) if-count))

          0x20
          (let [[index next]
                ((owner-function (:decode-u32 operations)) bytes (inc offset) limit)]
            (when-not (contains? initialized index)
              ((owner-function (:fail! operations))
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :local-get-after-definite-initialization
                :operation-id index}))
            (recur next (conj stack :i32) initialized
                   (conj ast {:op :local.get :index index
                              :offset offset :end next}) if-count))

          0x21
          (let [[index next]
                ((owner-function (:decode-u32 operations)) bytes (inc offset) limit)]
            (when-not (= :i32 (peek stack))
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
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
              ((owner-function (:fail! operations))
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :at-most-one-structured-if}))
            (when-not (and (= :i32 (peek stack))
                           (< (inc offset) limit)
                           (= 0x7f (nth bytes (inc offset))))
              ((owner-function (:fail! operations))
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :structured-if-i32-contract}))
            (let [base-stack (pop stack)
                  then-result
                  ((owner-function (:parse-instructions! operations))
                   bytes (+ offset 2) limit base-stack initialized #{0x05} 1)
                  _ (when-not (and (= 0x05 (:stop then-result))
                                   (= (conj base-stack :i32)
                                      (:stack then-result)))
                      ((owner-function (:fail! operations))
                       "B4-MANIFEST" "<b4-wasm>" {}
                       {:missing-fact :structured-if-then-result}))
                  else-result
                  ((owner-function (:parse-instructions! operations))
                   bytes (inc (:offset then-result)) limit
                   base-stack initialized #{0x0b} 1)
                  _ (when-not (and (= 0x0b (:stop else-result))
                                   (= (conj base-stack :i32)
                                      (:stack else-result)))
                      ((owner-function (:fail! operations))
                       "B4-MANIFEST" "<b4-wasm>" {}
                       {:missing-fact :structured-if-else-result}))
                  next (inc (:offset else-result))
                  common-initialized
                  ((owner-function (:intersection operations)) (:initialized then-result)
                                    (:initialized else-result))]
              (recur next (conj base-stack :i32) common-initialized
                     (conj ast {:op :if-i32 :offset offset :end next
                                :then (:ast then-result)
                                :else (:ast else-result)}) 1)))

          ((owner-function (:fail! operations))
           "B4-MANIFEST" "<b4-wasm>" {}
           {:missing-fact :bounded-wasm-instruction-opcode
            :opcode opcode}))))))

(defn evaluate-decoded-ast [evaluate-recursive ast locals stack]
  (loop [remaining ast locals locals stack stack]
    (if (empty? remaining)
      {:locals locals :stack stack}
      (let [node (first remaining)]
        (case (:op node)
          :i32.const
          (recur (rest remaining) locals (conj stack (:value node)))
          :local.get
          (recur (rest remaining) locals
                 (conj stack (get locals (:index node))))
          :local.set
          (recur (rest remaining)
                 (assoc locals (:index node) (peek stack)) (pop stack))
          :i32.eq
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (= left right) 1 0))))
          :i32.lt-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (< left right) 1 0))))
          :i32.le-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (<= left right) 1 0))))
          :i32.gt-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (> left right) 1 0))))
          :i32.ge-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (>= left right) 1 0))))
          :if-i32
          (let [condition (peek stack)
                branch (if (zero? condition) (:else node) (:then node))
                result ((owner-function evaluate-recursive)
                        branch locals (pop stack))]
            (recur (rest remaining) (:locals result) (:stack result))))))))

(defn flatten-decoded-ast [flatten-recursive ast]
  (vec
   (mapcat (fn [node]
             (if (= :if-i32 (:op node))
               (concat ((owner-function flatten-recursive) (:then node))
                       ((owner-function flatten-recursive) (:else node))
                       [node])
               [node]))
           ast)))

(defn parse-module! [operations bytes expected]
  (when-not (and (vector? bytes)
                 (<= 8 (count bytes))
                 (every? #(and (integer? %) (<= 0 % 255)) bytes))
    ((owner-function (:fail! operations))
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-byte-bounds
      :byte-count (if (coll? bytes) (count bytes) 0)}))
  (when-not (= [0 0x61 0x73 0x6d] (subvec bytes 0 4))
    ((owner-function (:fail! operations))
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-magic}))
  (when-not (= [1 0 0 0] (subvec bytes 4 8))
    ((owner-function (:fail! operations))
     "B4-TARGET" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-core-version-one}))
  (let [sections
        (loop [offset 8 expected-ids [1 3 7 10] records []]
          (if (= offset (count bytes))
            (do
              (when-not (empty? expected-ids)
                ((owner-function (:fail! operations))
                 (if (= 7 (first expected-ids))
                   "B4-EXPORT" "B4-MANIFEST")
                 "<b4-wasm>" {}
                 {:missing-fact :complete-required-section-set}))
              records)
            (let [id (nth bytes offset)
                  _ (when-not (= id (first expected-ids))
                      ((owner-function (:fail! operations))
                       (cond (= 7 (first expected-ids)) "B4-EXPORT"
                             (= id 2) "B4-IMPORT"
                             (contains? #{4 5 9 11 12} id) "B4-MEMORY"
                             :else "B4-TARGET")
                       "<b4-wasm>" {}
                       {:missing-fact :exact-core-section-order}))
                  [size payload-start]
                  ((owner-function (:decode-u32 operations))
                   bytes (inc offset) (count bytes))
                  end (+ payload-start size)]
              (when (> end (count bytes))
                ((owner-function (:fail! operations))
                 "B4-MANIFEST" "<b4-wasm>" {}
                 {:missing-fact :section-size-within-module}))
              (recur end (rest expected-ids)
                     (conj records {:id id :offset offset
                                    :payload-start payload-start
                                    :end end
                                    :payload (subvec bytes payload-start end)})))))
        code (:payload (nth sections 3))
          [function-count body-size-offset]
          ((owner-function (:decode-u32 operations)) code 0 (count code))
          _function-count
          (when-not (and (= 1 function-count)
                         (< body-size-offset (count code)))
            ((owner-function (:fail! operations))
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :single-code-function-body}))
          [body-size body-start]
          ((owner-function (:decode-u32 operations))
           code body-size-offset (count code))
          body-end (+ body-start body-size)
          _body-bounds
          (when-not (and (<= body-start body-end (count code))
                         (< body-start body-end))
            ((owner-function (:fail! operations))
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :code-body-size-within-section}))
          [local-groups local-count-offset]
          ((owner-function (:decode-u32 operations)) code body-start body-end)
          [local-count local-type-offset]
          ((owner-function (:decode-u32 operations)) code local-count-offset body-end)
          instruction-start (inc local-type-offset)
          _ (when-not (and (= 1 function-count) (= body-end (count code))
                           (= 1 local-groups)
                           (= (:operation-count expected) local-count)
                           (< local-type-offset body-end)
                           (= 0x7f (nth code local-type-offset)))
              ((owner-function (:fail! operations))
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :exact-code-body-and-i32-locals}))
          decoded
          ((owner-function (:parse-instructions! operations))
           code instruction-start body-end [] #{} #{0x0b} 0)
          decoded-result
          ((owner-function (:evaluate-decoded-ast operations)) (:ast decoded) {} [])
          set-indices
          (letfn [(indices [nodes]
                    (mapcat (fn [node]
                              (concat
                               (when (= :local.set (:op node))
                                 [(:index node)])
                               (when (= :if-i32 (:op node))
                                 (concat (indices (:then node))
                                         (indices (:else node))))) )
                            nodes))]
            (vec (indices (:ast decoded))))
          flat-ast ((owner-function (:flatten-decoded-ast operations)) (:ast decoded))
          operation-id-by-index
          ((owner-function (:map-invert operations)) (:operation-index expected))
          operation-byte-map
          (mapv
           (fn [index]
             (let [position (first (keep-indexed
                                    (fn [position node]
                                     (when (and (= :local.set (:op node))
                                                 (= index (:index node)))
                                        position))
                                    flat-ast))
                   _ (when-not (and (integer? position) (pos? position))
                       ((owner-function (:fail! operations))
                        "B4-MANIFEST" "<b4-wasm>" {}
                        {:missing-fact :complete-operation-byte-map
                         :operation-id index}))
                   set-node (nth flat-ast position)
                   operation-id (get operation-id-by-index index)
                   mir-opcode
                   (get (:operation-opcodes expected) operation-id)
                   comparison?
                   (contains? @(:comparison-opcodes operations) mir-opcode)
                   value-position (if comparison? (- position 3) (dec position))
                   _ (when (neg? value-position)
                       ((owner-function (:fail! operations))
                        "B4-MANIFEST" "<b4-wasm>" {}
                        {:missing-fact :complete-operation-byte-map
                         :operation-id index}))
                   value-node (nth flat-ast value-position)]
               {:operation-id operation-id :local-index index
                :opcode mir-opcode
                :byte-start (:offset value-node) :byte-end (:end set-node)}))
           (range (:operation-count expected)))]
    (when-not (= [1 4 0x6d 0x61 0x69 0x6e 0 0]
                 (:payload (nth sections 2)))
      ((owner-function (:fail! operations))
       "B4-EXPORT" "<b4-wasm>" {}
       {:missing-fact :exact-main-function-export}))
    (when-not (and (= [1 0x60 0 1 0x7f] (:payload (nth sections 0)))
                   (= [1 0] (:payload (nth sections 1)))
                   (= 0x0b (:stop decoded))
                   (= (dec body-end) (:offset decoded))
                   (= [:i32] (:stack decoded))
                   (= (vec (range (:operation-count expected)))
                      (vec (sort set-indices)))
                   (= [(:expected-result expected)]
                      (:stack decoded-result))
                   (= bytes (:wasm-bytes expected)))
      ((owner-function (:fail! operations))
       "B4-MANIFEST" "<b4-wasm>" {}
       {:missing-fact :independent-byte-plan-and-module-parity
        :byte-count (count bytes)
        :expected-byte-count (count (:wasm-bytes expected))}))
    {:artifact :gravity/b4-independent-raw-module-verification
     :status :passed :format :webassembly-core-v1
     :section-ids (mapv :id sections)
     :section-offsets (mapv #(select-keys % [:id :offset :end]) sections)
     :function-count 1 :type-count 1 :export-count 1
     :imports [] :exports [{:name "main" :kind :function :index 0}]
     :memory-count 0 :table-count 0 :global-count 0
     :operation-count (:operation-count expected)
     :decoded-ast (:ast decoded)
     :decoded-result (peek (:stack decoded-result))
     :operation-byte-map operation-byte-map
     :operation-byte-map-coordinate :code-section-payload
     :byte-end-exclusive? true
     :definitely-initialized-locals (:initialized decoded)
     :expected-result (:expected-result expected)}))

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
