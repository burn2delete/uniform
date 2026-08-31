(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-parse-module! [bytes expected]
  (when-not (and (vector? bytes)
                 (<= 8 (count bytes) p15-s23-b4-wasm-max-module-bytes)
                 (every? #(and (integer? %) (<= 0 % 255)) bytes))
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-byte-bounds
      :byte-count (if (coll? bytes) (count bytes) 0)}))
  (when-not (= [0 0x61 0x73 0x6d] (subvec bytes 0 4))
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-magic}))
  (when-not (= [1 0 0 0] (subvec bytes 4 8))
    (p15-s23-b4-wasm-fail!
     "B4-TARGET" "<b4-wasm>" {}
     {:missing-fact :raw-wasm-core-version-one}))
  (let [sections
        (loop [offset 8 expected-ids [1 3 7 10] records []]
          (if (= offset (count bytes))
            (do
              (when-not (empty? expected-ids)
                (p15-s23-b4-wasm-fail!
                 (if (= 7 (first expected-ids))
                   "B4-EXPORT" "B4-MANIFEST")
                 "<b4-wasm>" {}
                 {:missing-fact :complete-required-section-set}))
              records)
            (let [id (nth bytes offset)
                  _ (when-not (= id (first expected-ids))
                      (p15-s23-b4-wasm-fail!
                       (cond (= 7 (first expected-ids)) "B4-EXPORT"
                             (= id 2) "B4-IMPORT"
                             (contains? #{4 5 9 11 12} id) "B4-MEMORY"
                             :else "B4-TARGET")
                       "<b4-wasm>" {}
                       {:missing-fact :exact-core-section-order}))
                  [size payload-start]
                  (p15-s23-b4-wasm-decode-u32
                   bytes (inc offset) (count bytes))
                  end (+ payload-start size)]
              (when (> end (count bytes))
                (p15-s23-b4-wasm-fail!
                 "B4-MANIFEST" "<b4-wasm>" {}
                 {:missing-fact :section-size-within-module}))
              (recur end (rest expected-ids)
                     (conj records {:id id :offset offset
                                    :payload-start payload-start
                                    :end end
                                    :payload (subvec bytes payload-start end)})))))]
    (let [code (:payload (nth sections 3))
          [function-count body-size-offset]
          (p15-s23-b4-wasm-decode-u32 code 0 (count code))
          _function-count
          (when-not (and (= 1 function-count)
                         (< body-size-offset (count code)))
            (p15-s23-b4-wasm-fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :single-code-function-body}))
          [body-size body-start]
          (p15-s23-b4-wasm-decode-u32
           code body-size-offset (count code))
          body-end (+ body-start body-size)
          _body-bounds
          (when-not (and (<= body-start body-end (count code))
                         (< body-start body-end))
            (p15-s23-b4-wasm-fail!
             "B4-MANIFEST" "<b4-wasm>" {}
             {:missing-fact :code-body-size-within-section}))
          [local-groups local-count-offset]
          (p15-s23-b4-wasm-decode-u32 code body-start body-end)
          [local-count local-type-offset]
          (p15-s23-b4-wasm-decode-u32 code local-count-offset body-end)
          instruction-start (inc local-type-offset)
          _ (when-not (and (= 1 function-count) (= body-end (count code))
                           (= 1 local-groups)
                           (= (:operation-count expected) local-count)
                           (< local-type-offset body-end)
                           (= 0x7f (nth code local-type-offset)))
              (p15-s23-b4-wasm-fail!
               "B4-MANIFEST" "<b4-wasm>" {}
               {:missing-fact :exact-code-body-and-i32-locals}))
          decoded
          (p15-s23-b4-wasm-parse-instructions!
           code instruction-start body-end [] #{} #{0x0b} 0)
          decoded-result
          (p15-s23-b4-wasm-evaluate-decoded-ast (:ast decoded) {} [])
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
          flat-ast (p15-s23-b4-wasm-flatten-decoded-ast (:ast decoded))
          operation-id-by-index
          (set/map-invert (:operation-index expected))
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
                       (p15-s23-b4-wasm-fail!
                        "B4-MANIFEST" "<b4-wasm>" {}
                        {:missing-fact :complete-operation-byte-map
                         :operation-id index}))
                   set-node (nth flat-ast position)
                   operation-id (get operation-id-by-index index)
                   mir-opcode
                   (get (:operation-opcodes expected) operation-id)
                   comparison?
                   (contains? p15-s23-b4-wasm-comparison-opcodes mir-opcode)
                   value-position (if comparison? (- position 3) (dec position))
                   _ (when (neg? value-position)
                       (p15-s23-b4-wasm-fail!
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
      (p15-s23-b4-wasm-fail!
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
      (p15-s23-b4-wasm-fail!
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
     :expected-result (:expected-result expected)})))

(def p15-s23-b4-wasm-gravity-lowering-keys
  #{:artifact :status :source-mir-id :target :target-kind :features :abi
    :imports :exports :memory :table :globals :start :data :custom-sections
    :runtime-helpers :component-model? :wit? :wasi? :block-order
    :operation-index :wasm-bytes :expected-result :diagnostics
    :clojure-seed-boundary? :self-hosted?})

(defn p15-s23-b4-wasm-expected-gravity-lowering [mir expected]
  {:artifact :gravity/b4-bounded-wasm32-core-lowering
   :status :constructed-unverified
   :source-mir-id (:module-id mir)
   :target (:target expected) :target-kind (:target-kind expected)
   :features (:features expected) :abi (:abi expected)
   :imports (:imports expected) :exports (:exports expected)
   :memory (:memory expected) :table (:table expected)
   :globals (:globals expected) :start (:start expected)
   :data (:data expected) :custom-sections (:custom-sections expected)
   :runtime-helpers (:runtime-helpers expected)
   :component-model? (:component-model? expected)
   :wit? (:wit? expected) :wasi? (:wasi? expected)
   :block-order (:block-order expected)
   :operation-index (:operation-index expected)
   :wasm-bytes (:wasm-bytes expected)
   :expected-result (:expected-result expected)
   :diagnostics [] :clojure-seed-boundary? true :self-hosted? false})

(defn p15-s23-b4-wasm-gravity-lowering-valid? [result mir expected]
  (and (map? result)
       (= p15-s23-b4-wasm-gravity-lowering-keys (set (keys result)))
       (= (p15-s23-b4-wasm-expected-gravity-lowering mir expected)
          result))))
