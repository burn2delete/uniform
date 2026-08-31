(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn- p15-s23-b4-wasm-require-authority! [candidate source-path operation]
  (when-not (identical? candidate p15-s23-b4-wasm-authority-token)
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" source-path {}
     {:missing-fact :opaque-authenticated-b4-authority
      :tool-step operation})))

(defn p15-s23-b4-wasm-node-execution-snapshot []
  @p15-s23-b4-wasm-node-state)

(defn- p15-s23-b4-wasm-resolve-source-path [candidate source-path]
  (p15-s23-b4-wasm-require-authority!
   candidate source-path :resolve-pinned-b4-source)
  (let [c11-file (java.io.File. (p15-s23-c11-mir-resolve-source-path))
        root (loop [directory (.getParentFile c11-file)]
               (if (or (nil? directory)
                       (.isFile (java.io.File.
                                 directory
                                 p15-s23-b4-wasm-source-relative-path)))
                 directory
                 (recur (.getParentFile directory))))]
    (if root
      (.getCanonicalPath
       (java.io.File. root p15-s23-b4-wasm-source-relative-path))
      p15-s23-b4-wasm-source-relative-path)))

(defn- p15-s23-b4-wasm-source-binding! [candidate request-source]
  (p15-s23-b4-wasm-require-authority!
   candidate request-source :load-pinned-b4-source)
  (let [source-path (p15-s23-b4-wasm-resolve-source-path
                     candidate request-source)
        file (java.io.File. source-path)
        byte-count (.length file)]
    (when-not (and (.isFile file)
                   (= byte-count p15-s23-b4-wasm-source-byte-count))
      (p15-s23-b4-wasm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :pinned-gravity-b4-source
        :byte-count byte-count
        :expected-byte-count p15-s23-b4-wasm-source-byte-count}))
    (let [bytes (java.nio.file.Files/readAllBytes (.toPath file))
          source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
          source-hash (str "sha256:" (sha256-bytes-hex bytes))
          rule (c-backend-stage2-plan-emitter-source-rule!
                request-source :wasm)
          plan (p15-s23-stage2-compiler-artifact-plan
                (:emitter rule) source-path source-text)
          functions (:functions plan)
          shapes (into {}
                       (map (fn [[name _]]
                              [name (select-keys (get functions name)
                                                 [:arity :params])]))
                       p15-s23-b4-wasm-required-functions)
          plan-hash (p15-s23-c11-mir-digest
                     (p15-s23-stage2-compiler-artifact-semantic-input plan))
          functions-hash (p15-s23-c11-mir-digest functions)
          builder-hash (p15-s23-c11-mir-digest
                        (get functions p15-s23-b4-wasm-builder-function))]
      (when-not (and (= source-hash
                         p15-s23-b4-wasm-expected-source-content-hash)
                     (= shapes p15-s23-b4-wasm-required-functions)
                     (= plan-hash
                        p15-s23-b4-wasm-expected-plan-semantic-hash)
                     (= functions-hash
                        p15-s23-b4-wasm-expected-functions-semantic-hash)
                     (= builder-hash
                        p15-s23-b4-wasm-expected-builder-semantic-hash))
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" request-source {}
         {:missing-fact :pinned-gravity-b4-source-identity
          :content-hash source-hash}))
      {:source-path source-path :source-byte-count byte-count
       :source-content-hash source-hash :plan plan
       :plan-semantic-hash plan-hash
       :functions-semantic-hash functions-hash
       :builder-semantic-hash builder-hash :function-shapes shapes})))

(defn p15-s23-b4-wasm-u32-leb [value]
  (loop [value value result []]
    (let [next (quot value 128)
          byte (mod value 128)]
      (if (zero? next)
        (conj result byte)
        (recur next (conj result (+ byte 128)))))))

(defn p15-s23-b4-wasm-s32-leb [value]
  (loop [value value result []]
    (let [q (quot value 128)
          r (- value (* q 128))
          [q r] (if (neg? r) [(dec q) (+ r 128)] [q r])
          done? (or (and (zero? q) (< r 64))
                    (and (= -1 q) (>= r 64)))]
      (if done?
        (conj result r)
        (recur q (conj result (+ r 128)))))))

(defn p15-s23-b4-wasm-section [id payload]
  (vec (concat [id] (p15-s23-b4-wasm-u32-leb (count payload)) payload)))

(defn p15-s23-b4-wasm-block-order [mir function]
  (let [entry (:entry function)
        blocks (:blocks function)]
    (if (= 1 (count blocks))
      [entry]
      [entry
       (first (get-in blocks [entry :successors]))
       (second (get-in blocks [entry :successors]))
       (get-in mir [:control-flow-graph :join :block-id])])))

(defn p15-s23-b4-wasm-operation-sequence [mir function block-order]
  (vec (mapcat #(get-in function [:blocks % :instructions]) block-order)))

(defn p15-s23-b4-wasm-constant-i32 [operation]
  (let [value (get-in operation [:constant-payload :value])]
    (cond (= :gravity/bool (:type operation)) (if value 1 0)
          (= :gravity/nil (:type operation)) 0
          :else value)))

(defn p15-s23-b4-wasm-preflight! [wasm-packet]
  (let [source-path (get-in wasm-packet [:actual-path-provenance :source])
        _ (p15-s23-c13-c14-b1-wasm-verification-preflight!
           source-path wasm-packet)
        b1 (:b1 wasm-packet)
        mir (get-in b1 [:bounded-lowering-payload :mir])
        function (get-in mir [:functions 'main])
        block-order (when function
                      (p15-s23-b4-wasm-block-order mir function))
        operations (when function
                     (p15-s23-b4-wasm-operation-sequence
                      mir function block-order))
        block-labels (p15-s23-b3-llvm-block-labels block-order)
        allowed #{:constant :local :local-binding :truthiness :sequence
                  :conditional-join :lexical-scope :function-boundary
                  :integer-eq :integer-lt :integer-lte
                  :integer-gt :integer-gte}]
    (when-not (and (= :gravity/p15-s23-c13-c14-b1-wasm-authenticated-packet
                      (:kind wasm-packet))
                   (= :accepted-for-bounded-wasm (:status wasm-packet))
                   (= :gravity/b1-verified-backend-input-packet
                      (:artifact b1))
                   (= :accepted-for-bounded-wasm (:status b1))
                   (= :gravity.backend/wasm
                      (get-in b1 [:backend-manifest :backend]))
                   (= :gravity/mir-module (:artifact mir))
                   (= :passed (:verification-status mir))
                   (= :hosted (:profile mir))
                   (= :jvm (:source-target mir))
                   (= :wasm (:target-request mir))
                   (true? (:target-independent? mir))
                   (= #{'main} (set (keys (:functions mir))))
                   (= #{} (:latent-effects function))
                   (= #{} (:capabilities function))
                   (= [] (:params function))
                   (contains? #{1 4} (count (:blocks function)))
                   (<= 1 (count operations) 127)
                   (= {} (:globals mir))
                   (= {} (:domain-anchors mir))
                   (= {} (:runtime-check-table mir))
                   (= mir (:optimized-mir wasm-packet)
                      (get-in wasm-packet [:c14 :bounded-lowering-payload :mir]))
                   (= (:artifact-id (:c13 wasm-packet))
                      (get-in b1 [:bounded-lowering-payload
                                  :c13-artifact-id]))
                   (= (:artifact-id (:c14 wasm-packet))
                      (get-in b1 [:backend-manifest :c14-artifact-id])))
      (p15-s23-b4-wasm-fail!
       "B1-INPUT" source-path wasm-packet
       {:missing-fact :exact-pure-wasm32-c11-envelope
        :requested-target (:target-request mir)}))
    (doseq [operation operations]
      (let [reason
            (p15-s23-c14-wasm-host-operation-rejection
             operations operation block-labels)]
      (when-not (and (nil? reason)
                     (contains? allowed (:opcode operation))
                     (= #{} (:effects operation))
                     (= #{} (:capabilities operation))
                     (nil? (:domain-anchor operation))
                     (= :passed (:verifier-status operation))
                     (or (= :function-boundary (:opcode operation))
                         (contains? #{:gravity/integer :gravity/bool
                                      :gravity/nil}
                                    (:type operation)))
                     (or (not= :constant (:opcode operation))
                         (let [value (p15-s23-b4-wasm-constant-i32 operation)]
                           (and (integer? value)
                                (<= Integer/MIN_VALUE value Integer/MAX_VALUE)))))
        (p15-s23-b4-wasm-fail!
         "B1-UNSUPPORTED" source-path operation
         {:missing-fact (or reason :bounded-pure-i32-mir-operation)
                    :operation-id (:op-id operation)
                    :opcode (:opcode operation)
                    :observed-type (:type operation)}))))
    {:mir mir :function function :block-order block-order
     :operations operations
     :operation-index (zipmap (map :op-id operations) (range))
     :block-labels block-labels :wasm-packet wasm-packet :b1 b1}))

(defn p15-s23-b4-wasm-local [opcode index]
  (vec (concat [opcode] (p15-s23-b4-wasm-u32-leb index))))

(defn p15-s23-b4-wasm-forwarded? [opcode]
  (contains? #{:local :local-binding :sequence :lexical-scope
               :function-boundary} opcode))

(def p15-s23-b4-wasm-comparison-opcodes
  {:integer-eq 0x46 :integer-lt 0x48 :integer-lte 0x4c
   :integer-gt 0x4a :integer-gte 0x4e}))
