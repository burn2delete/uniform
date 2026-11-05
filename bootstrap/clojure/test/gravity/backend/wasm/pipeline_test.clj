(ns gravity.backend.wasm.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.backend.wasm :as wasm]
            [gravity.bootstrap :as bootstrap]))

(defn- native [name & arguments]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b4-wasm-" name))) arguments))

(defn- operation [id block opcode operands type value]
  {:op-id id :block-id block :opcode opcode :source-operation (if (= opcode :constant) :literal opcode)
   :operands operands :type type :effects #{} :capabilities #{}
   :constant-payload (if (= opcode :constant) {:present? true :value value} :not-applicable)
   :source {:span {:source "wasm-data.gravity" :start {:line 1 :column 1} :end {:line 1 :column 2}}
            :generated-origin [{:kind :fixture :origin-id (str "parent-" id)}]}
   :facts {:source-origin-id (str "source-" id) :safety-proof-id (str "proof-" id)}})

(defn- straight-operations []
  (vec (concat
        (map-indexed #(operation (str "c" %1) "entry" :constant []
                                 (cond (boolean? %2) :gravity/bool (nil? %2) :gravity/nil
                                       :else :gravity/integer) %2)
                     [0 7 -7 Integer/MIN_VALUE Integer/MAX_VALUE true false nil])
        (map-indexed #(operation (str "f" %1) "entry" %2 ["c1"] :gravity/integer nil)
                     [:local :local-binding :sequence :lexical-scope :function-boundary])
        (map-indexed #(operation (str "t" %1) "entry" :truthiness [%2] :gravity/bool nil)
                     ["c1" "c5" "c7"])
        (map-indexed #(operation (str "p" %1) "entry" %2 ["c1" "c2"] :gravity/bool nil)
                     [:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte]))))

(defn- fixtures []
  {"straight"
   {:functions {'main {:entry "entry" :blocks
                      {"entry" {:instructions (straight-operations)
                                  :terminator {:operands ["p4"]}}}}}}
   "diamond"
   {:control-flow-graph {:join {:block-id "join"}}
    :functions
    {'main {:entry "entry" :blocks
            {"entry" {:instructions [(operation "condition" "entry" :constant [] :gravity/bool true)]
                        :successors ["then" "else"] :terminator {:operands ["condition"]}}
             "then" {:instructions [(operation "left" "then" :constant [] :gravity/integer 7)]
                       :terminator {:operands ["left"]}}
             "else" {:instructions [(operation "right" "else" :constant [] :gravity/integer 9)]
                       :terminator {:operands ["right"]}}
             "join" {:instructions [(operation "result" "join" :conditional-join
                                                 ["condition" "left" "right"] :gravity/integer nil)]
                       :terminator {:operands ["result"]}}}}}}})

(defn- emission-input [mir]
  (let [function (get-in mir [:functions 'main])
        order (native "block-order" mir function)
        operations (native "operation-sequence" mir function order)]
    {:function function :block-order order :operations operations
     :operation-index (zipmap (map :op-id operations) (range))}))

(defn- refusal [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo exception (ex-data exception))))

(defn- capture-native [mir]
  (let [{:keys [operations operation-index] :as input} (emission-input mir)
        by-id (into {} (map (juxt :op-id identity) operations))
        module (native "reconstruct" input)
        bad-op (operation "bad" "entry" :unsupported [] :gravity/integer nil)]
    {:mir mir :input input :module module
     :function-bytes (native "function-bytes" input)
     :operation-bytes (mapv #(native "operation-bytes" % by-id operation-index)
                            (remove #(= :conditional-join (:opcode %)) operations))
     :u32 (mapv (fn [value] (let [bytes (native "u32-leb" value)]
                             [value bytes (native "decode-u32" bytes 0 (count bytes))]))
                 [0 1 127 128 16383 16384 0xffffffff])
     :s32 (mapv (fn [value] (let [bytes (native "s32-leb" value)]
                             [value bytes (native "decode-s32" bytes 0 (count bytes))]))
                 [0 1 -1 63 64 -64 -65 Integer/MIN_VALUE Integer/MAX_VALUE])
     :opcode-refusal (refusal #(native "operation-bytes" bad-op by-id operation-index))
     :format-refusals
     (mapv (fn [[codec bytes]] (refusal #(native codec bytes 0 (count bytes))))
           [["decode-u32" []] ["decode-u32" [128 0]]
            ["decode-u32" [255 255 255 255 16]] ["decode-u32" [128 128 128 128 128 0]]
            ["decode-s32" [255 127]] ["decode-s32" [255 255 255 255 15]]
            ["decode-s32" [128 128 128 128 128 0]] ["decode-u32" [false]]
            ["decode-u32" [0.0]] ["decode-s32" [256]]])
     :join-refusal
     (when (= 4 (count (:block-order input)))
       (refusal #(native "function-bytes"
                         (assoc-in input [:function :blocks "join" :instructions 0 :operands]
                                   ["condition" "right" "left"]))))}))

(def ^:private gravity-plan
  (delay
    (let [source (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b4_wasm_backend_design.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :wasm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- gravity-call [name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-wasm-codec-development :compiler-artifact-plan? true} @gravity-plan name arguments))

(defn- gravity-module [mir]
  (let [{:keys [function block-order operations operation-index] :as input} (emission-input mir)
        instructions (gravity-call 'b4-function-instructions [function block-order operations operation-index])
        body (vec (concat [1] (gravity-call 'b4-u32-leb [(count operations)])
                          [0x7f] instructions [0x0b]))
        code (vec (concat [1] (gravity-call 'b4-u32-leb [(count body)]) body))
        bytes (vec (concat [0 97 115 109 1 0 0 0]
                           (gravity-call 'b4-section [1 [1 0x60 0 1 0x7f]])
                           (gravity-call 'b4-section [3 [1 0]])
                           (gravity-call 'b4-section [7 [1 4 0x6d 0x61 0x69 0x6e 0 0]])
                           (gravity-call 'b4-section [10 code])))
        expected (native "reconstruct" input)]
    (is (= bytes (:wasm-bytes expected)))
    {:input input :bytes bytes :expected expected}))

(defn- large-module []
  (let [ops (mapv #(operation (str "large-" %) "entry" :constant []
                              :gravity/integer Integer/MAX_VALUE) (range 7500))
        mir {:functions {'main {:entry "entry" :blocks
                               {"entry" {:instructions ops :terminator {:operands ["large-7499"]}}}}}}
        expected (native "reconstruct" (emission-input mir))]
    {:bytes (:wasm-bytes expected) :expected expected}))

(deftest real-gravity-byte-modules-pass-relative-native-structural-verification
  ;; Gravity helpers emit the bytes; the expected packet is native fixture
  ;; data, with no B1 admission, source authentication or Node execution.
  (doseq [[label mir] (fixtures)
          :let [{:keys [input bytes expected]} (gravity-module mir)
                before (pr-str [input bytes expected])
                report (native "parse-module!" bytes expected)]]
    (is (= :passed (:status report)) label)
    (is (= [1 3 7 10] (:section-ids report)))
    (is (= (:expected-result expected) (:decoded-result report)))
    (is (= (count (:operations input)) (:operation-count report) (count (:operation-byte-map report))))
    (is (= (mapv :op-id (:operations input)) (mapv :operation-id (:operation-byte-map report))))
    (is (= :code-section-payload (:operation-byte-map-coordinate report)))
    (is (= before (pr-str [input bytes expected])))
    (let [section-offset (:offset (last (:section-offsets report)))
          [_ payload-start] (native "decode-u32" bytes (inc section-offset) (count bytes))
          instruction-start (+ payload-start (:byte-start (first (:operation-byte-map report))))]
      (doseq [[opcode fact] [[0x20 :local-get-after-definite-initialization]
                             [0x21 :local-set-i32-stack-value]
                             [0x46 :i32-comparison-two-stack-operands]]]
        (let [record (refusal #(native "parse-module!" (assoc bytes instruction-start opcode) expected))]
          (is (= "B4-MANIFEST" (:id record)))
          (is (= fact (:missing-fact record))))))
    (doseq [[changed id] [[(assoc bytes 1 0) "B4-MANIFEST"]
                          [(assoc bytes 4 2) "B4-TARGET"]
                          [(assoc bytes 8 2) "B4-IMPORT"]
                          [(assoc bytes 8 5) "B4-MEMORY"]
                          [(assoc bytes (dec (count bytes)) 0xff) "B4-MANIFEST"]]]
      (is (= id (:id (refusal #(native "parse-module!" changed expected))))))
    (is (= "B4-MANIFEST"
           (:id (refusal #(native "parse-module!" bytes (assoc expected :expected-result -999))))))))

(deftest valid-emitted-module-is-not-refused-by-the-retired-byte-quota
  (let [{:keys [bytes expected]} (large-module)
        report (native "parse-module!" bytes expected)]
    (is (> (count bytes) 65536))
    (is (= :passed (:status report)))
    (is (= 7500 (:operation-count report) (count (:operation-byte-map report))
           (count (:definitely-initialized-locals report))))
    (is (= Integer/MAX_VALUE (:decoded-result report)))
    (is (= :code-section-payload (:operation-byte-map-coordinate report)))
    (is (nil? (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-max-module-bytes)))))

(defn- parser-operations []
  (into {:intersection #'set/intersection :map-invert #'set/map-invert}
        (map (fn [[key name]]
               [key (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b4-wasm-" name)))]))
        [[:decode-u32 "decode-u32"] [:decode-s32 "decode-s32"] [:fail! "fail!"]
         [:parse-instructions! "parse-instructions!"] [:evaluate-decoded-ast "evaluate-decoded-ast"]
         [:flatten-decoded-ast "flatten-decoded-ast"] [:comparison-opcodes "comparison-opcodes"]]))

(deftest parser-contract-and-direct-composition-retain-relative-native-results
  (let [publics (ns-publics 'gravity.backend.wasm)
        contract (:gravity/namespace-contract (meta #'wasm/parse-module!))]
    (is (empty? (ns-aliases 'gravity.backend.wasm)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name function] publics]
      (is (= (:arglists (meta function)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))))
  (doseq [[_ mir] (fixtures)
          :let [{:keys [bytes expected]} (gravity-module mir)
                report (wasm/parse-module! (parser-operations) bytes expected)
                ast (:decoded-ast report)]]
    (is (= (native "parse-module!" bytes expected) report))
    (is (= (native "evaluate-decoded-ast" ast {} [])
           (wasm/evaluate-decoded-ast (:evaluate-decoded-ast (parser-operations)) ast {} [])))
    (is (= (native "flatten-decoded-ast" ast)
           (wasm/flatten-decoded-ast (:flatten-decoded-ast (parser-operations)) ast)))
    (is (= :passed (:status report)))
    (is (every? (fn [{:keys [byte-start byte-end]}] (< byte-start byte-end)) (:operation-byte-map report)))))

(deftest parser-recursion-keeps-the-live-bootstrap-vars-and-owner-exceptions
  (let [{:keys [bytes expected]} (gravity-module (get (fixtures) "diamond"))
        operations (parser-operations) calls (atom [])
        keys [:parse-instructions! :evaluate-decoded-ast :flatten-decoded-ast]
        original (into {} (map #(vector % @(get operations %)) keys))
        replacements (into {} (map (fn [key]
                                     [(get operations key)
                                      (fn [& args] (swap! calls conj key) (apply (get original key) args))]) keys))]
    (with-redefs-fn replacements #(is (= :passed (:status (native "parse-module!" bytes expected)))))
    (doseq [key keys] (is (< 1 (count (filter #{key} @calls)))))
    (let [failure (ex-info "original parser owner refusal" {})
          caught (try (wasm/parse-module!
                       (assoc operations :fail! (fn [& _] (throw failure))) (assoc bytes 1 0) expected)
                      nil (catch clojure.lang.ExceptionInfo error error))]
      (is (identical? failure caught)))
    (let [changed (assoc bytes 1 0) failures (atom [])
          report (wasm/parse-module!
                  (assoc operations :fail! (fn [& args] (swap! failures conj args) false))
                  changed (assoc expected :wasm-bytes changed))]
      (is (= :passed (:status report)))
      (is (= [:raw-wasm-magic] (mapv #(:missing-fact (nth % 3)) @failures))))))

(deftest module-parser-selects-callables-before-arguments-and-reads-the-late-catalog
  (let [{:keys [bytes expected]} (gravity-module (get (fixtures) "straight"))
        operations (parser-operations) invert-var (:map-invert operations)
        comparison-var (:comparison-opcodes operations)
        invert @invert-var comparison @comparison-var
        original-report (wasm/parse-module! operations bytes expected)
        calls (atom [])
        replacement (fn [index] (swap! calls conj :after) (invert index))
        observe (fn [key fallback]
                  (when (= :operation-index key)
                    (alter-var-root invert-var (constantly replacement)))
                  (get expected key fallback))
        observed (reify clojure.lang.ILookup
                   (valAt [_ key] (observe key nil))
                   (valAt [_ key fallback] (observe key fallback)))]
    (with-redefs-fn {invert-var (fn [index] (swap! calls conj :before) (invert index))}
      #(is (= original-report (wasm/parse-module! operations bytes observed))))
    (is (= [:before] @calls))
    (let [report (with-redefs-fn
                   {comparison-var comparison
                    invert-var (fn [index]
                                 (alter-var-root comparison-var (constantly {}))
                                 (invert index))}
                   #(wasm/parse-module! operations bytes expected))]
      (is (= :passed (:status report)))
      (is (not= (:operation-byte-map original-report) (:operation-byte-map report))))))

(deftest parser-facades-pass-unchanged-arguments-and-explicit-live-ports
  (doseq [[name stage-function arity owner-keys]
          [["parse-instructions!" #'wasm/parse-instructions! 7
            [:decode-u32 :decode-s32 :fail! :parse-instructions! :intersection]]
           ["parse-module!" #'wasm/parse-module! 2
            [:decode-u32 :parse-instructions! :evaluate-decoded-ast :flatten-decoded-ast
             :fail! :map-invert :comparison-opcodes]]]]
    (let [arguments (vec (repeatedly arity #(Object.))) observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage-function (fn [& args] (reset! observed args) marker)}
        #(is (identical? marker (apply native name arguments))))
      (is (= (select-keys (parser-operations) owner-keys) (first @observed)))
      (is (every? true? (map identical? arguments (rest @observed))))))
  (doseq [[name stage-function arity] [["evaluate-decoded-ast" #'wasm/evaluate-decoded-ast 3]
                                      ["flatten-decoded-ast" #'wasm/flatten-decoded-ast 1]]]
    (let [arguments (vec (repeatedly arity #(Object.))) observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage-function (fn [& args] (reset! observed args) marker)}
        #(is (identical? marker (apply native name arguments))))
      (is (identical? (get (parser-operations) (keyword name)) (first @observed)))
      (is (every? true? (map identical? arguments (rest @observed)))))))

(deftest emits-real-core-modules-and-refuses-invalid-operations-and-encodings
  (doseq [[label mir] (fixtures)
          :let [{:keys [input module opcode-refusal format-refusals join-refusal u32 s32]} (capture-native mir)]]
    (is (= #{:function :operations :operation-index :block-order} (set (keys input))))
    (is (= [0 97 115 109 1 0 0 0] (subvec (:wasm-bytes module) 0 8)) label)
    (is (= [] (:imports module) (:runtime-helpers module)))
    (is (= [{:name "main" :kind :function :index 0}] (:exports module)))
    (is (= "B1-UNSUPPORTED" (:id opcode-refusal)))
    (is (= :independent-operation-emission (:missing-fact opcode-refusal)))
    (is (every? #(= "B4-MANIFEST" (:id %)) format-refusals))
    (doseq [[value bytes [decoded end]] (concat u32 s32)]
      (is (= value decoded)) (is (= (count bytes) end)))
    (when join-refusal
      (is (= "B1-INPUT" (:id join-refusal)))
      (is (= :exact-conditional-join-binding (:missing-fact join-refusal))))))

(deftest current-gravity-byte-kernels-and-missing-input-refusal
  (doseq [[label mir] (fixtures)
          :let [{:keys [function block-order operations operation-index] :as input} (emission-input mir)
                captured (capture-native mir)
                gravity-instructions (gravity-call 'b4-function-instructions
                                                    [function block-order operations operation-index])]]
    (is (= (:function-bytes captured) gravity-instructions) label)
    (is (= (:expected-result (:module captured))
           (get (gravity-call 'b4-evaluate-operations [operations operations {}])
                (first (get-in function [:blocks (last block-order) :terminator :operands])))))
    (doseq [value [0 127 128 16384 0xffffffff]]
      (is (= (native "u32-leb" value) (gravity-call 'b4-u32-leb [value]))))
    (doseq [value [0 -1 63 64 -65 Integer/MIN_VALUE Integer/MAX_VALUE]]
      (is (= (native "s32-leb" value) (gravity-call 'b4-s32-leb [value]))))
    (is (= (native "section" 10 gravity-instructions)
           (gravity-call 'b4-section [10 gravity-instructions])))
    (is (= (native "function-bytes" input) gravity-instructions)))
  (is (= {:status :rejected :diagnostic "B1-INPUT" :missing-fact :verified-bounded-wasm-b1-packet}
         (select-keys (gravity-call 'b4-build-bounded-wasm32-core [nil])
                      [:status :diagnostic :missing-fact]))))

(deftest canonical-width-boundaries-and-byte-offsets
  (is (= [128 1] (native "u32-leb" 128)))
  (is (= [255 255 255 255 15] (native "u32-leb" 0xffffffff)))
  (is (= [191 127] (native "s32-leb" -65)))
  (is (= [128 128 128 128 120] (native "s32-leb" Integer/MIN_VALUE)))
  (is (= [255 255 255 255 7] (native "s32-leb" Integer/MAX_VALUE)))
  (is (= [128 3] (native "decode-u32" [99 128 1 99] 1 3)))
  (is (= [-65 3] (native "decode-s32" [99 191 127 99] 1 3)))
  (is (= :canonical-bounded-u32-leb
         (:missing-fact (refusal #(native "decode-u32" [128 1] 0 1)))))
  (is (= :canonical-bounded-s32-leb
         (:missing-fact (refusal #(native "decode-s32" [191 127] 0 1)))))
  (is (= 255 (native "canonical-bounded-octet!" 255N :octet)))
  (doseq [value [-1 256 true false nil 0.0]]
    (is (= "B4-MANIFEST" (:id (refusal #(native "canonical-bounded-octet!" value :octet)))))))

(deftest compatibility-callback-order-and-late-opcode-map-read
  (let [events (atom [])
        op (first (straight-operations))
        constant-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-constant-i32)
        signed-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-s32-leb)
        local-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-local)
        forward-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-forwarded?)
        comparison-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-comparison-opcodes)]
    (with-redefs-fn
      {constant-var (fn [_] (swap! events conj :constant) 42)
       signed-var (fn [value] (swap! events conj [:signed value]) [value])
       local-var (fn [opcode index] (swap! events conj [:local opcode index]) [opcode index])}
      #(is (= [65 42 33 0] (native "operation-bytes" op {} {(:op-id op) 0}))))
    (is (= [:constant [:signed 42] [:local 33 0]] @events))
    (with-redefs-fn
      {comparison-var {}
       forward-var (fn [_]
                     (alter-var-root comparison-var (constantly {:fixture-comparison 70})) false)
       local-var (fn [opcode index]
                   (when (= 0x20 opcode)
                     (alter-var-root comparison-var (constantly {:fixture-comparison 72})))
                   [opcode index])}
      #(is (= [32 0 32 1 72 33 2]
              (native "operation-bytes"
                      (operation "result" "entry" :fixture-comparison ["left" "right"] :gravity/bool nil)
                      {} {"left" 0 "right" 1 "result" 2}))
           "The comparison map is read at each original use, after earlier callbacks"))))

(deftest canonical-decoder-failure-ports-and-reconstruction-order
  (let [octet-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-canonical-bounded-octet!)
        canonical-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-parser-u32-bytes)
        fail-var (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-fail!)
        events (atom [])]
    (with-redefs-fn
      {octet-var (fn [value missing] (swap! events conj [:octet value missing]) value)
       canonical-var (fn [value] (swap! events conj [:canonical value]) [7])
       fail-var (fn [id source subject facts]
                  (swap! events conj [:failure id source subject facts])
                  (throw (ex-info "ported" {:id id :facts facts})))}
      #(is (= "B4-MANIFEST" (:id (refusal (fn [] (native "decode-u32" [1] 0 1)))))))
    (is (= [[:octet 1 :canonical-bounded-u32-leb] [:canonical 1]
            [:failure "B4-MANIFEST" "<b4-wasm>" {} {:missing-fact :noncanonical-u32-leb}]] @events)))
  (let [names ["function-bytes" "section" "evaluate"]
        vars (mapv #(ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b4-wasm-" %))) names)
        events (atom [])
        replacements (zipmap vars (map (fn [name f]
                                         (fn [& arguments]
                                           (swap! events conj [name (when (= name "section") (first arguments))])
                                           (apply f arguments))) names (map var-get vars)))]
    (with-redefs-fn replacements
      #(is (= 1 (:expected-result (native "reconstruct" (emission-input (get (fixtures) "straight")))))))
    (is (= [["function-bytes" nil] ["section" 1] ["section" 3] ["section" 7] ["section" 10] ["evaluate" nil]]
           @events))))

(deftest data-contract-preserves-input-provenance-without-admission
  (let [contract (:gravity/namespace-contract (meta #'wasm/reconstruct))
        mir (get (fixtures) "straight")
        input (emission-input mir)
        before (pr-str input)
        output (native "reconstruct" input)]
    (is (= 'gravity.backend.wasm (:namespace contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (= '([{:keys [function block-order operations operation-index]}])
           (:arglists (meta (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-function-bytes)))))
    (is (= '([{:keys [function block-order operations]}])
           (:arglists (meta (ns-resolve 'gravity.bootstrap 'p15-s23-b4-wasm-evaluate)))))
    (is (every? #(= :none (:admission %)) (vals (:public-api contract))))
    (is (= before (pr-str input)))
    (is (= (:operation-index input) (:operation-index output)))
    (is (= (:block-order input) (:block-order output)))
    (is (= (into {} (map (juxt :op-id :opcode) (:operations input))) (:operation-opcodes output)))
    (is (not (contains? output :verification-status)))
    (doseq [field [:component-model? :wit? :wasi?]] (is (false? (get output field))))))
