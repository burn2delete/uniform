(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-parse-module!
  [bytes expected]
  (gravity.backend.wasm/parse-module!
   {:decode-u32 #'p15-s23-b4-wasm-decode-u32
    :parse-instructions! #'p15-s23-b4-wasm-parse-instructions!
    :evaluate-decoded-ast #'p15-s23-b4-wasm-evaluate-decoded-ast
    :flatten-decoded-ast #'p15-s23-b4-wasm-flatten-decoded-ast
    :fail! #'p15-s23-b4-wasm-fail!
    :map-invert #'set/map-invert
    :comparison-opcodes #'p15-s23-b4-wasm-comparison-opcodes}
   bytes expected))

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
