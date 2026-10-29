(ns gravity.self-hosting.sh17-target-lowering-hardening-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh17_target_lowering_hardening_test.clj")]
    (when-not resource
      (throw (ex-info "SH-17 test source is not on the classpath"
                      {:id "SH17-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH17-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private engine-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")

(defn- compile-plan []
  (let [source-path (path engine-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(defn- source-forms []
  (with-open [reader
              (java.io.PushbackReader.
               (io/reader (path engine-path)))]
    (loop [result []]
      (let [form (read {:eof ::eof} reader)]
        (if (= form ::eof)
          result
          (recur (conj result form)))))))

(defn- calls-named [name forms]
  (let [calls (atom [])]
    (doseq [form forms]
      (walk/postwalk
       (fn [value]
         (when (and (seq? value) (= name (first value)))
           (swap! calls conj value))
         value)
       form))
    @calls))

(def ^:private engine-plan (delay (compile-plan)))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh17-target-lowering-hardening
    :compiler-artifact-plan? true}
   @engine-plan function arguments))

(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))

(deftest sh17-equality-delta-positions-follow-vector-bounds
  (doseq [index [0 65536 1000000000]]
    (is (true? (invoke 'c14-equality-index? [index]))))
  (doseq [index [-1 0.5 nil "0"]]
    (is (false? (invoke 'c14-equality-index? [index]))))
  (let [rows (vec (repeat 65536 :existing))
        restored (invoke 'c14-equality-restore-rows [rows [{:index 65536 :edge :inserted}] :edge])]
    (is (= 65537 (count restored)))
    (is (= :inserted (peek restored)))
    (is (= rows (pop restored))))
  (doseq [additions [[{:index 2 :edge :outside}]
                     [{:index -1 :edge :negative}]
                     [{:index 0 :edge :one} {:index 0 :edge :duplicate}]]]
    (is (nil? (invoke 'c14-equality-restore-rows [[] additions :edge])))))

(deftest sh17-host-and-gravity-target-policies-agree
  (doseq [[function policy unsupported]
          [['c14-policy-scope-valid? (bootstrap/p15-s23-c14-target-policy)
            bootstrap/p15-s23-c14-llvm-unsupported-surface]
           ['c14-c-policy-scope-valid? (bootstrap/p15-s23-c14-c-target-policy)
            bootstrap/p15-s23-c14-c-unsupported-surface]]]
    (let [input {:target-policy policy :unsupported-surface unsupported
                 :proof-target-metadata {:entries [] :proofless-metadata-rejected? true}
                 :request-id (digest "a")}]
      (is (true? (invoke function [input])))
      (is (false? (invoke function [(assoc-in input [:target-policy :unexpected] true)])))))
  (is (not (contains? (bootstrap/p15-s23-c14-wasm-target-policy) :maximum-operation-count))))

(defn- target [backend]
  (case backend
    :llvm {:backend :llvm
           :canonical-target :llvm-x86_64-linux
           :triple "x86_64-unknown-linux-gnu"
           :architecture :x86_64
           :object-format :elf}
    :c {:backend :c
        :triple "arm64-apple-macosx14.0.0"
        :architecture :arm64
        :object-format :c17-source}
    :wasm {:backend :wasm
           :triple "wasm32-unknown-unknown"
           :architecture :wasm32
           :object-format :wasm-module}))

(defn- abi [backend]
  {:calling-convention (if (= backend :wasm) :wasm-core
                           (if (= backend :llvm) :sysv-amd64 :c))
   :pointer-width (if (= backend :wasm) 32 64)
   :endianness :little
   :layout-id (digest "a")})

(defn- operation [operation-id opcode operands result-id source-id]
  {:operation-id operation-id
   :opcode opcode
   :operands operands
   :result-id result-id
   :type :gravity/integer
   :effects []
   :capabilities []
   :source-id source-id})

(defn- source-entry [operation-id source-id checkout]
  {:operation-id operation-id
   :source-id source-id
   :span {:source-id source-id
          :start-byte 0
          :end-byte 8
          :actual-source-path (str checkout "/program.gravity")}
   :origin-chain
   [{:generator-id (digest "b")
     :anchor-id (digest "c")
     :actual-source-path (str checkout "/generated.gravity")}]})

(defn- request [backend checkout]
  (let [constant-source (digest "d")
        return-source (digest "e")
        operations
        [(operation :op/constant :constant [] :value/constant constant-source)
         (assoc
          (operation :op/return :return [:value/constant]
                     :value/return return-source)
          :effects [:io/write]
          :capabilities [:stdio/write])]]
    {:artifact :gravity/sh17-target-lowering-request
     :schema-version 1
     :backend backend
     :profile :hosted
     :target (target backend)
     :abi (abi backend)
     :runtime {:family :minimal-native :services []}
     :providers [{:effect :io/write
                  :capability :stdio/write
                  :provider-id (digest "9")}]
     :effects [:io/write]
     :capabilities [:stdio/write]
     :c11-revision (invoke 'sh17-current-c11-revision [])
     :mir {:artifact :gravity/verified-target-independent-mir
           :schema-version 1
           :verified? true
           :mir-id (digest "f")
           :entrypoint :function/main
           :functions
           [{:function-id :function/main
             :entry-block :block/entry
             :return-type :gravity/integer
             :blocks
             [{:block-id :block/entry
               :operations operations
               :terminator {:kind :return
                            :value-id :value/constant}}]}]}
     :proofs
     [{:proof-id (digest "1")
       :operation-id :op/constant
       :claim :integer-constant-representable
       :status :verified}]
     :source-map
     [(source-entry :op/constant constant-source checkout)
      (source-entry :op/return return-source checkout)]
     :actual-path-provenance
     [{:kind :actual-source-path
       :path (str checkout "/program.gravity")}] }))

(defn- build [request]
  (invoke 'sh17-build-target-lowering [request]))

(defn- verify [request candidate]
  (invoke 'sh17-verify-target-lowering [request candidate]))

(defn- reason [result]
  (get-in result [:diagnostics 0 :reason]))

(defn- c11-source-rule []
  (let [revision (invoke 'sh17-current-c11-revision [])]
    {:artifact :gravity/c11-pinned-source-rule
     :owner :gravity-source
     :source-content-hash (:source-content-hash revision)
     :source-byte-count (:source-byte-count revision)
     :plan-semantic-hash (:plan-semantic-hash revision)
     :functions-semantic-hash (:functions-semantic-hash revision)
     :builder-function (:builder-function revision)
     :builder-semantic-hash (:builder-semantic-hash revision)
     :verifier-function (:verifier-function revision)
     :verifier-semantic-hash (:verifier-semantic-hash revision)
     :function-shapes (:function-shapes revision)
     :compiled-by :clojure-stage0-seed
     :executed-by :clojure-stage0-rule-runner
     :clojure-seed-boundary? true
     :self-hosted? false}))

(defn- check-scalar-forwarding [opcode-sources]
  ;; Original C14 whole-surface gates and evaluator on supplied MIR DATA, not
  ;; a freshly authenticated C11/C13 packet or a native backend execution.
  (let [core "core/local-forward" entry (str core ":mir:entry")
        scalar-values [[:gravity/integer 42] [:gravity/bool false] [:gravity/nil nil]]
        operation (fn [id opcode source-operation type operands payload]
                    {:artifact :gravity/mir-operation :op-id id :block-id entry
                     :opcode opcode :source-operation source-operation :type type
                     :operands operands :constant-payload payload :verifier-status :passed
                     :effects #{} :capabilities #{} :domain-anchor nil})
        fixture (fn [backend opcode source-operation type value many?]
                  (let [discarded (operation "discard" :constant :literal :gravity/integer []
                                             {:present? true :value 7})
                        input (operation "input" :constant :literal type [] {:present? true :value value})
                        local (operation "local" opcode source-operation
                                         type (if many? ["discard" "input"] ["input"]) :not-applicable)
                        result (operation "result" :constant :literal :gravity/integer []
                                          {:present? true :value 17})
                        operations [discarded input local result]]
                    {:optimized-mir
                     {:source-core core :profile :hosted
                      :target-request (if (= backend :llvm) :llvm-x86_64-linux backend)
                      :target-independent? true :runtime-check-table {} :domain-anchors {} :globals {}
                      :control-flow-graph {:entry entry :edges [] :dominance {entry [entry]} :join nil}
                      :functions {'main {:params [] :latent-effects #{} :capabilities #{}
                                         :blocks {entry {:instructions operations
                                                         :terminator {:opcode :return :operands ["result"]}}}}}}
                     :policy {:supported-operation-ids (mapv :op-id operations)}}))
        eligible (fn [backend subject]
                   (invoke (case backend :c 'c14-c-bounded-mir-surface-valid?
                                         :llvm 'c14-llvm-bounded-mir-surface-valid?
                                         :wasm 'c14-wasm-bounded-mir-surface-valid?)
                           [(dissoc subject :policy) (:policy subject)]))
        operation-path [:optimized-mir :functions 'main :blocks entry :instructions]]
    (doseq [backend [:c :llvm :wasm] [opcode source-operation] opcode-sources
            many? [false true] [type value] scalar-values]
      (let [subject (fixture backend opcode source-operation type value many?)
            operations (get-in subject operation-path)
            by-id (into {} (map (juxt :op-id identity) operations))
            evaluated (invoke 'c14-c-evaluate-operations [operations by-id {}])]
        (is (true? (eligible backend subject)))
        (is (= value (get evaluated "local")))
        ;; Do not coerce a forwarded value to match a false declared type.
        (doseq [wrong-type (concat (remove #{type} (map first scalar-values))
                                  [:unsupported/scalar "gravity/integer" nil])]
          (is (false? (eligible backend (assoc-in subject (conj operation-path 2 :type) wrong-type)))))
        (is (false? (eligible backend (assoc-in subject (conj operation-path 1 :type) :unsupported/scalar))))))
    (doseq [backend [:c :llvm :wasm] [opcode source-operation] opcode-sources]
      (let [subject (fixture backend opcode source-operation :gravity/integer 42 false)]
        (doseq [[field value] [[:verifier-status :rejected] [:effects #{:io/write}]
                               [:capabilities #{:io/stdout}] [:domain-anchor :unsupported]
                               [:source-operation :quote] [:operands ["missing"]] [:operands []]]]
          (is (false? (eligible backend (assoc-in subject (conj operation-path 2 field) value)))))))))

(deftest sh17-local-forwarding-preserves-the-last-operand-scalar-type
  (check-scalar-forwarding [[:local :local] [:local-binding :let-binding]]))

(deftest sh17-sequence-and-scope-preserve-the-last-operand-scalar-type
  ;; L2 do/let and the original C6/C11 producers retain the last body type;
  ;; multioperand forwarding must not use the discarded first operand's type.
  (check-scalar-forwarding [[:sequence :do] [:lexical-scope :let]]))

(deftest sh17-development-c-process-results-use-existing-b2-boolean-exit-codes
  ;; UNIT supplied MIR/preflight premises, not authenticated C11/B1 input,
  ;; whole independent-raw execution, emitted C, or native execution.
  (let [core "core/c-process-result" entry (str core ":mir:entry")
        op (fn [id opcode type operands payload]
             {:artifact :gravity/mir-operation :op-id id :block-id entry
              :opcode opcode :source-operation (if (= opcode :constant) :literal opcode)
              :type type :operands operands :constant-payload payload
              :verifier-status :passed :effects #{} :capabilities #{} :domain-anchor nil})
        literal (fn [type value]
                  [(op "result" :constant type [] {:present? true :value value})])
        comparison (fn [left right]
                     [(op "left" :constant :gravity/integer [] {:present? true :value left})
                      (op "right" :constant :gravity/integer [] {:present? true :value right})
                      (op "result" :integer-lt :gravity/bool ["left" "right"] :not-applicable)])
        mir (fn [operations]
              {:source-core core :profile :hosted :source-target :jvm :target-request :c
               :target-independent? true :verification-status :passed :diagnostics []
               :runtime-check-table {} :domain-anchors {} :globals {}
               :functions {'main {:entry entry :params [] :latent-effects #{} :capabilities #{}
                                  :blocks {entry {:instructions operations
                                                  :terminator {:opcode :return :operands ["result"]}}}}}})
        eligible? (fn [operations]
                    (invoke 'c14-c-bounded-mir-surface-valid?
                            [{:optimized-mir (mir operations)}
                             {:supported-operation-ids (mapv :op-id operations)}]))
        preflight (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-c13-c14-b1-c-preflight!))
        checked (fn [operations]
                  (try {:value (preflight (path engine-path)
                                         {:kind :gravity/p15-s23-c11-authenticated-mir-artifact
                                          :mir-module (mir operations)
                                          :b1-preflight {:status :verified-mir-candidate-for-b1}})}
                       (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))
        convert-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-process-exit-code)
        b2-path (path "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity")
        b2-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan
                 (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! b2-path :jvm))
                 b2-path (slurp b2-path))
        b2-exit (fn [value]
                  (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh17-b2-exit-code-unit :compiler-artifact-plan? true}
                   b2-plan 'b2-exit-code [value]))]
    (is (some? convert-var))
    (doseq [[operations value] [[(literal :gravity/bool true) true]
                               [(literal :gravity/bool false) false]
                               [(comparison 2 4) true] [(comparison 4 2) false]
                               [(literal :gravity/integer 0) 0]
                               [(literal :gravity/integer 255) 255]]]
      (let [result (checked operations)]
        (is (true? (eligible? operations)))
        (is (nil? (:error result)))
        (when-let [product (:value result)]
          (is (= value (:semantic-result product)))
          (is (= (mir operations) (:mir product)))
          (is (= operations (:operations product))))
        (when convert-var
          (is (= (b2-exit value) (convert-var value)))
          (is (integer? (convert-var value))))))
    (doseq [[type value reason] [[:gravity/nil nil :process-result-outside-0-to-255]
                                [:gravity/integer -1 :process-result-outside-0-to-255]
                                [:gravity/integer 256 :process-result-outside-0-to-255]
                                [:gravity/bool 1 :bounded-scalar-constant-payload]
                                [:gravity/integer true :bounded-scalar-constant-payload]]]
      (let [operations (literal type value) result (checked operations)]
        (is (false? (eligible? operations)))
        (is (nil? (:value result)))
        (is (= "C14-UNSUPPORTED" (get-in result [:error :id])))
        (is (= reason (get-in result [:error :missing-fact])))))
    ;; B2's general nil conversion does not grant this C14 eligibility.
    (is (= 0 (b2-exit nil)))
    (when convert-var (is (nil? (convert-var nil))))
    (let [relative "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-024-p15-s23-b2-c17-proof-to-c-assumption-record-through-p15-s23-b2-c17-independent-raw.clj"
          forms (binding [*read-eval* false]
                  (with-open [reader (java.io.PushbackReader. (io/reader (path relative)))]
                    (loop [forms []]
                      (let [form (read {:eof ::eof} reader)]
                        (if (= ::eof form) forms (recur (conj forms form)))))))
          definitions (filter #(and (seq? %) (= 'defn- (first %))
                                    (= 'p15-s23-b2-c17-independent-raw (second %)))
                              (tree-seq coll? seq forms))]
      (is (= 1 (count definitions)))
      (let [definition (first definitions)
            bindings (partition 2 (second (first (drop 3 definition))))
            result-map (last (first (drop 3 definition)))]
        (is (= '(p15-s23-b2-c17-process-exit-code (:semantic-result preflight))
               (second (first (filter #(= 'semantic-result (first %)) bindings)))))
        (is (= 'semantic-result (:semantic-result result-map)))
        (is (= 'semantic-result (:expected-exit-code result-map)))
        (is (= 'semantic-result
               (last (first (calls-named 'p15-s23-b2-c17-proof-to-c-assumption-record
                                        [definition])))))))))

(deftest sh17-source-compiles-and-c11-revision-is-complete
  (let [plan @engine-plan
        if-calls (calls-named 'if (source-forms))
        revision (invoke 'sh17-current-c11-revision [])
        source-rule (c11-source-rule)
        changed-shapes
        (assoc (:function-shapes source-rule)
               'c11-build-data-flow-for-node
               {:arity 2 :params '[node result]})]
    (is (= 330835 (:source-byte-count revision)))
    (is (= 351 (:function-count revision)))
    (is (= 'verify-c11-mir-module (:verifier-function revision)))
    (is (= 27 (count (:function-shapes revision))))
    (is (pos? (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (contains? (:functions plan) 'sh17-build-target-lowering))
    (is (contains? (:functions plan) 'sh17-verify-target-lowering))
    (is (true? (invoke 'c14-c11-source-rule-valid? [source-rule])))
    (is (false?
         (invoke 'c14-c11-source-rule-valid?
                 [(assoc source-rule :function-shapes changed-shapes)])))))

(deftest sh17-lowers-and-verifies-all-three-bounded-targets
  (doseq [backend [:llvm :c :wasm]]
    (testing (name backend)
      (let [input (request backend "/checkout-a")
            result (build input)
            verification (verify input result)]
        (is (= :accepted (:status result)))
        (is (= backend (:backend result)))
        (is (= backend
               (first
                (get-in result
                        [:target-program 0 :operations 0
                         :target-opcode]))))
        (is (= (:c11-revision input)
               (get-in result [:identity-input :c11-revision])))
        (is (= (get-in input [:mir :mir-id])
               (get-in result [:identity-input :mir-id])))
        (is (= (:proofs input)
               (get-in result [:identity-input :proofs])))
        (is (= (:providers input)
               (get-in result [:identity-input :providers])))
        (is (= (:effects input)
               (get-in result [:identity-input :effects])))
        (is (= (:capabilities input)
               (get-in result [:identity-input :capabilities])))
        (is (= [:io/write]
               (get-in result
                       [:target-program 0 :operations 1 :effects])))
        (is (= [:stdio/write]
               (get-in result
                       [:target-program 0 :operations 1 :capabilities])))
        (is (= (get-in input
                       [:mir :functions 0 :blocks 0 :operations 1
                        :source-id])
               (get-in result
                       [:target-program 0 :operations 1 :source-id])))
        (is (= :passed (:status verification)))
        (is (empty? (:diagnostics verification)))))))

(deftest sh17-enforces-exact-normalized-schemas-and-references
  (let [base (request :llvm "/checkout-a")
        cases
        [(assoc base :unexpected true)
         (assoc base :c11-revision
                (assoc (:c11-revision base) :function-count 138))
         (assoc-in base [:mir :unexpected] true)
         (assoc-in base [:mir :functions 0 :unexpected] true)
         (assoc-in base [:mir :functions 0 :blocks 0 :unexpected] true)
         (assoc-in base [:mir :functions 0 :blocks 0 :operations 0
                         :unexpected] true)
         (assoc-in base [:source-map 0 :unexpected] true)
         (assoc-in base [:source-map 0 :span :unexpected] true)
         (assoc-in base [:source-map 0 :origin-chain 0 :unexpected] true)
         (assoc-in base [:source-map 0 :source-id] (digest "8"))
         (assoc-in base [:proofs 0 :unexpected] true)
         (assoc base :providers
                [{:effect :io/read
                  :capability :fs/read
                  :provider-id (digest "2")
                  :unexpected true}])
         (assoc-in base [:providers 0 :effect] :io/read)
         (assoc base :actual-path-provenance [])
         (assoc-in base [:actual-path-provenance 0 :unexpected] true)
         (assoc-in base [:mir :entrypoint] :function/not-main)
         (assoc-in base
                   [:mir :functions 0 :blocks]
                   (conj
                    (get-in base [:mir :functions 0 :blocks])
                    {:block-id :block/second
                     :operations
                     [(operation :op/constant :integer-add
                                 [:value/constant] :value/second
                                 (digest "7"))]
                     :terminator {:kind :return
                                  :value-id :value/second}}))
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :operands]
                   [:value/missing])
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :result-id]
                   :value/constant)
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :terminator :value-id]
                   :value/missing)
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 0 :operands]
                   [:value/return])
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :effects]
                   [:io/read])
         (assoc-in base [:source-map 0 :operation-id] :op/missing)
         (assoc-in base [:proofs 0 :operation-id] :op/missing)]]
    (doseq [hostile cases]
      (let [result (build hostile)]
        (is (= :rejected (:status result)))
        (is (contains? #{:invalid-normalized-request
                         :cross-reference-mismatch}
                       (reason result)))))))

(deftest sh17-validates-complete-carriers-without-quotas
  (let [base (request :llvm "/checkout-a")
        deep (loop [n 120 value nil] (if (zero? n) value (recur (dec n) [value])))
        many (vec (repeat 17 (vec (range 600))))
        late-opaque (assoc-in many [16 599] (Object.))
        old-id (get-in base [:mir :functions 0 :blocks 0 :operations 0 :operation-id])
        long-id (keyword (apply str (repeat 300 "x")))
        subject (walk/postwalk #(if (= old-id %) long-id %) base)
        result (build subject)]
    (doseq [carrier [deep (vec (range 1025)) many (apply str (repeat 65537 "x"))]]
      (is (= {:status :accepted} (invoke 'sh17-structural-preflight [carrier]))))
    (is (= :accepted (:status result)))
    (is (= :passed (:status (verify subject result))))
    (is (= {:status :rejected :reason :structure-noncanonical-scalar}
           (invoke 'sh17-structural-preflight [late-opaque])))
    (is (= {:status :rejected :reason :structure-sequence-carrier}
           (invoke 'sh17-structural-preflight [(iterate inc 0)])))
    (doseq [subject [(assoc-in base [:mir :functions 0 :blocks 0 :operations 0 :operands]
                              (vec (repeat 17 :value/x)))
                     (assoc-in base [:source-map 0 :span :end-byte] 9223372036854775808N)]]
      (is (= :rejected (:status (build subject)))))))

(deftest sh17-linux-llvm-target-contract-rejects-cross-target-substitution
  (let [base (request :llvm "/checkout-a")
        hostile-targets
        [(assoc-in base [:target :canonical-target] :llvm-x86-64-linux)
         (assoc-in base [:target :canonical-target] :darwin-arm64)
         (assoc-in base [:target :triple] "arm64-apple-macosx14.0.0")
         (assoc-in base [:target :architecture] :arm64)
         (assoc-in base [:target :architecture] :aarch64)
         (assoc-in base [:target :object-format] :mach-o)
         (update base :target dissoc :canonical-target)
         (assoc-in base [:target :triple] "wasm32-unknown-unknown")]]
    (doseq [hostile hostile-targets]
      (let [result (build hostile)]
        (is (= :rejected (:status result)))
        (is (= "C14-TARGET"
               (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (= :target-contract-mismatch
               (reason result)))))))

(deftest sh17-identity-is-path-neutral-and-provenance-retains-paths
  (doseq [backend [:llvm :c :wasm]]
    (let [left (build (request backend "/checkout-left"))
          right (build (request backend "/checkout-right"))]
      (is (= :accepted (:status left) (:status right)))
      (is (= (:identity-input left) (:identity-input right)))
      (is (not= (:provenance left) (:provenance right)))
      (is (not (.contains (pr-str (:identity-input left))
                          "/checkout-left")))
      (is (.contains (pr-str (:provenance left)) "/checkout-left")))))

(deftest sh17-verifier-rejects-result-substitution-before-comparison
  (let [input (request :wasm "/checkout-a")
        result (build input)
        altered (assoc-in result [:target-program 0 :operations 0
                                  :target-opcode]
                          [:llvm :constant])
        deep-candidate
        (assoc result :padding
               (loop [remaining 10000 value nil]
                 (if (zero? remaining)
                   value
                   (recur (dec remaining) [value]))))
        sequence-candidate (assoc result :padding (iterate inc 0))]
    (is (= :passed (:status (verify input result))))
    (is (= :rejected (:status (verify input altered))))
    (is (= :result-substitution (reason (verify input altered))))
    (is (= :result-substitution
           (reason (verify input deep-candidate))))
    (is (= :candidate-structural-bound
           (reason (verify input sequence-candidate))))))

(deftest sh17-return-types-match-the-defined-returned-value
  ;; Supplied normalized MIR declarations, not fresh authenticated SH16 input.
  (doseq [backend [:llvm :c :wasm]]
    (let [base (request backend "/checkout-return-type")
          function-path [:mir :functions 0]
          operations-path (conj function-path :blocks 0 :operations)
          terminator-path (conj function-path :blocks 0 :terminator)
          bool-request (-> base
                           (assoc-in (conj function-path :return-type)
                                     :gravity/bool)
                           (assoc-in (conj operations-path 0 :type)
                                     :gravity/bool)
                           (assoc-in (conj operations-path 1 :type)
                                     :gravity/bool))
          check-rejected
          (fn [input]
            (let [result (build input)]
              (is (= :rejected (:status result)))
              (is (= "C14-INPUT" (get-in result [:diagnostics 0 :diagnostic-id])))
              (is (= :invalid-normalized-request (reason result)))
              (is (nil? (:target-program result)))
              (is (= :passed (:status (verify input result))))))]
      (doseq [input [base bool-request]]
        (let [result (build input)]
          (is (= :accepted (:status result)))
          (is (= :passed (:status (verify input result))))
          (is (= (get-in input (conj operations-path 0 :type))
                 (get-in result [:target-program 0 :operations 0 :type])))
          (is (= (get-in base [:c11-revision])
                 (get-in result [:identity-input :c11-revision])))))
      ;; Do not mistake the last operation for the terminator-selected value.
      (check-rejected (assoc-in base (conj function-path :return-type)
                                :gravity/bool))
      (check-rejected (assoc-in bool-request (conj function-path :return-type)
                                :gravity/integer))
      (check-rejected (assoc-in base (conj operations-path 0 :type)
                                :gravity/bool))
      (check-rejected (assoc-in base (conj terminator-path :value-id)
                                :value/missing))
      (check-rejected (assoc-in base (conj operations-path 1 :result-id)
                                :value/constant))
      (check-rejected (assoc-in base (conj operations-path 0 :operands)
                                [:value/return]))
      (let [incompatible (assoc-in base (conj function-path :return-type)
                                   :gravity/bool)
            accepted (build base)]
        (is (= :rejected (:status (verify incompatible accepted))))
        (is (= :result-substitution (reason (verify incompatible accepted))))))))

(deftest sh17-return-operations-preserve-one-defined-operand-type
  ;; Supplied normalized declarations, not native or authenticated MIR input.
  (doseq [backend [:llvm :c :wasm]]
    (let [base (request backend "/checkout-return-signature")
          function-path [:mir :functions 0]
          ops-path (conj function-path :blocks 0 :operations)
          bool-input (-> base
                         (assoc-in (conj function-path :return-type) :gravity/bool)
                         (assoc-in (conj ops-path 0 :type) :gravity/bool)
                         (assoc-in (conj ops-path 1 :type) :gravity/bool))]
      (doseq [input [base bool-input]]
        (let [result (build input)
              original (get-in input (conj ops-path 1))
              lowered (get-in result [:target-program 0 :operations 1])
              check-rejected
              (fn [hostile]
                (let [rejected (build hostile)]
                  (is (= :rejected (:status rejected)))
                  (is (= "C14-INPUT" (get-in rejected [:diagnostics 0 :diagnostic-id])))
                  (is (= :invalid-normalized-request (reason rejected)))
                  (is (nil? (:target-program rejected)))
                  (is (= :passed (:status (verify hostile rejected))))
                  (is (= :rejected (:status (verify hostile result))))))]
          (is (= :accepted (:status result)))
          (is (= (:type original) (:type lowered)))
          (is (= (:operands original) (:operands lowered)))
          (is (= (:source-id original) (:source-id lowered)))
          (is (= (:effects original) (:effects lowered)))
          (is (= :passed (:status (verify input result))))
          (doseq [operands [[] [:value/constant :value/constant]
                            [:value/missing] [:value/return]]]
            (check-rejected (assoc-in input (conj ops-path 1 :operands) operands)))
          (check-rejected
           (assoc-in input (conj ops-path 1 :type)
                     (if (= (:type original) :gravity/bool)
                       :gravity/integer :gravity/bool)))))
      ;; The terminator, not the return operation or final operation, selects
      ;; the function's returned value in this bounded normalized carrier.
      (let [bool-source (digest "6")
            later-source (digest "7")
            bool-value (assoc (operation :op/bool :constant [] :value/bool bool-source)
                              :type :gravity/bool)
            returned (assoc (get-in base (conj ops-path 1))
                            :type :gravity/bool :operands [:value/bool])
            later (operation :op/later :constant [] :value/later later-source)
            input (-> base
                      (assoc-in ops-path [(get-in base (conj ops-path 0))
                                          bool-value returned later])
                      (update :source-map into
                              [(source-entry :op/bool bool-source "/checkout-return-signature")
                               (source-entry :op/later later-source "/checkout-return-signature")]))
            result (build input)]
        (is (= :accepted (:status result)))
        (is (= :gravity/bool (get-in result [:target-program 0 :operations 2 :type])))
        (is (= :value/constant (get-in result [:target-program 0 :terminator :value-id])))
        (is (= :passed (:status (verify input result))))))))

(deftest sh17-integer-operations-check-their-declared-signatures
  ;; Normalized declarations only: no integer evaluation, widths or overflow.
  (doseq [backend [:llvm :c :wasm]
          [opcode result-type] [[:integer-add :gravity/integer]
                                [:integer-eq :gravity/bool]]]
    (let [base (request backend "/checkout-integer-signature")
          function-path [:mir :functions 0]
          ops-path (conj function-path :blocks 0 :operations)
          left (get-in base (conj ops-path 0))
          right (operation :op/right :constant [] :value/right (digest "6"))
          arithmetic (assoc (operation :op/arithmetic opcode
                                       [:value/constant :value/right]
                                       :value/arithmetic (digest "7"))
                            :type result-type)
          returned (assoc (get-in base (conj ops-path 1))
                          :type result-type :operands [:value/arithmetic])
          input (-> base
                    (assoc-in ops-path [left right arithmetic returned])
                    (assoc-in (conj function-path :return-type) result-type)
                    (assoc-in (conj function-path :blocks 0 :terminator :value-id)
                              :value/arithmetic)
                    (assoc :source-map
                           [(get-in base [:source-map 0])
                            (source-entry :op/right (digest "6") "/checkout-integer-signature")
                            (source-entry :op/arithmetic (digest "7") "/checkout-integer-signature")
                            (get-in base [:source-map 1])]))
          result (build input)
          check-rejected
          (fn [hostile]
            (let [rejected (build hostile)]
              (is (= :rejected (:status rejected)))
              (is (= :invalid-normalized-request (reason rejected)))
              (is (nil? (:target-program rejected)))
              (is (= :passed (:status (verify hostile rejected))))))]
      (is (= :accepted (:status result)))
      (is (= [backend opcode]
             (get-in result [:target-program 0 :operations 2 :target-opcode])))
      (is (= result-type (get-in result [:target-program 0 :operations 2 :type])))
      (is (= :passed (:status (verify input result))))
      (doseq [operands [[] [:value/constant]
                        [:value/constant :value/right :value/right]
                        [:value/missing :value/right]
                        [:value/return :value/right]]]
        (check-rejected (assoc-in input (conj ops-path 2 :operands) operands)))
      (doseq [index [0 1] type [:gravity/bool :gravity/nil]]
        (check-rejected (assoc-in input (conj ops-path index :type) type)))
      (let [wrong-type (if (= result-type :gravity/bool)
                         :gravity/integer :gravity/bool)
            hostile (-> input
                        (assoc-in (conj ops-path 2 :type) wrong-type)
                        (assoc-in (conj ops-path 3 :type) wrong-type)
                        (assoc-in (conj function-path :return-type) wrong-type))]
        (check-rejected hostile)
        (is (= :rejected (:status (verify hostile result)))))
      (check-rejected (assoc-in input (conj ops-path 2 :operation-id) :op/constant)))))
