(ns gravity.self-hosting.list-prepend-source-consumer-current-test
  "Source-bound development coverage for the current List-prepend owners.

  This test uses the current reader/core/type/effect path for the accepted
  fixture.  It does not execute QST or claim backend, public C8/L5, runtime,
  migration, release, or self-hosting authority."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private upstream
  'gravity.self-hosting.sh08-function-call-type-test)

(def ^:private kernel-support
  'gravity.self-hosting.sh08-function-call-kernel-support)

(def ^:private fixture-stems
  ["list-prepend-bare-head"
   "list-prepend-wrong-member"
   "list-prepend-allocation"
   "list-prepend-result-annotation"
   "list-prepend-arity-one"
   "list-prepend-arity-three"
   "list-prepend-unknown-value"
   "list-prepend-nil-member"
   "list-prepend-homogeneous-to-bare"])

(defn- repository-root
  []
  (let [resource (io/resource
                  "gravity/self_hosting/list_prepend_source_consumer_current_test.clj")]
    (when-not resource
      (throw (ex-info "List-prepend source consumer test is not on the classpath"
                      {:id "PR502-LIST-PREPEND-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "PR502-LIST-PREPEND-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family stem extension]
  (path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/"
            family "/" stem extension)))

(defn- upstream-var
  [name]
  (require upstream)
  (or (ns-resolve upstream name)
      (throw (ex-info "Missing current SH-08 source helper" {:name name}))))

(defn- invoke-c7
  [function arguments]
  ((upstream-var 'invoke-c7) function arguments))

(defn- source-products
  [source-path]
  ;; This is the ordinary current C3 -> SH05 -> SH06 -> SH07 source route.
  ;; It does not use the restricted private-C5, stopped DSE, Macro/sysctl, or
  ;; native-runtime routes.
  ((upstream-var 'function-current-source) source-path))

(defn- typed-products
  [source-path]
  (let [source (source-products source-path)
        typed (invoke-c7 'sh08-function-type-core-artifact [(:request source)])
        verification (invoke-c7 'sh08-verify-function-type-result
                                [(:request source) typed])]
    (assoc source :typed typed :verification verification)))

(defn- effects-products
  [{:keys [request typed verification]}]
  (let [seal-binding (ns-resolve kernel-support 'seal-binding)
        invoke-effects (ns-resolve kernel-support 'leaf)
        binding (seal-binding request typed verification)
        effects (invoke-effects 'build-body-effect-result typed verification binding)
        effect-verification
        (invoke-effects 'verify-body-effect-result
                        typed verification binding effects)]
    {:binding binding
     :effects effects
     :effect-verification effect-verification}))

(defn- prepend-descriptors
  [facts]
  (mapv :applied-list-prepend
        (filter #(= :direct-list-prepend (:dispatch %))
                (:call-type-facts facts))))

(defn- bare-prepend-descriptor
  [core facts]
  (let [bare (some #(when (= 'bare-prepend (:definition-name %)) %)
                   (:function-type-table facts))
        owner (:function-syntax-id bare)
        call-ids (set (map :call-core-node-id
                           (filter #(= owner (:caller-function-syntax-id %))
                                   (:call-edges core))))]
    (or (some #(when (contains? call-ids (:call-core-node-id %)) %)
              (prepend-descriptors facts))
        ;; The current C7 function table intentionally exposes a core-node
        ;; owner rather than a syntax owner for this source function.  Its
        ;; bare List consumer is the unique direct-prepend fact whose first
        ;; argument is the bare list type.
        (some #(when (= :gravity.type/list (first (:argument-types %))) %)
              (prepend-descriptors facts)))))

(defn- descriptor-mutant
  [typed f]
  (update typed :call-type-facts
          (fn [facts]
            (mapv (fn [fact]
                    (if (:applied-list-prepend fact)
                      (update fact :applied-list-prepend f)
                      fact))
                  facts))))

(defn- positive-input
  []
  (typed-products (fixture-path "accepted" "local-runtime-list-prepend" ".gravity")))

(defn- current-emitter-products
  [source-path source]
  ;; Keep the source boundary and the lowering boundary joined by the actual
  ;; current Gravity-authored emitter.  This deliberately stops at the
  ;; emitted plan; it does not execute backend/native/runtime/QST output.
  (let [input (select-keys source [:request :typed :verification])
        artifact (get-in input [:request :b47-context :authenticated-wrapper
                                :sh06-resolution-artifact :sh05-macro-artifact])
        parsed (get-in artifact [:gravity-macro-boundary :authenticated-sh04-artifact
                                 :c2-reader-artifact :parsed-semantic-values])
        raw-module (assoc (bootstrap/parse-module source-path parsed)
                          :forms (:expanded-forms artifact))
        definitions (bootstrap/stage0-function-table raw-module)
        module (assoc raw-module :function-table definitions)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  emitter-path :jvm))
        ;; The typed operation derives a historical sibling path for C7 from
        ;; the bootstrap emitter path.  Keep the operation and its source
        ;; owner unchanged, while resolving that one relocated read to the
        ;; actual current C7 source file.
        original-read bootstrap/read-gravity-source-text
        bad-c7-path (.getPath
                     (io/file (.getParentFile (.getParentFile (io/file emitter-path)))
                              "src/gravity/compiler/c7_type_checker_engine.gravity"))
        current-c7-path (path "language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity")
        result (with-redefs [bootstrap/read-gravity-source-text
                             (fn [candidate]
                               (original-read (if (= candidate bad-c7-path)
                                                current-c7-path
                                                candidate)))]
                 (bootstrap/p15-s23-stage2-compile-function
                  emitter module (get definitions 'bare-prepend) input))]
    {:emitter-path emitter-path
     :module module
     :definitions definitions
     :result result}))

(defn- c7-diagnostic-signature
  [result]
  (mapv #(select-keys % [:rule :reason :stage :message])
        (:diagnostics result)))

(def ^:private expected-c7-diagnostics
  {"list-prepend-bare-head" #{["C7-TYPE-MISMATCH" :list-query-head-type-required]}
   "list-prepend-wrong-member" #{["C7-TYPE-MISMATCH" :list-prepend-element-type-mismatch]}
   "list-prepend-result-annotation" #{["C7-TYPE-MISMATCH" :function-result-type-mismatch]}
   "list-prepend-arity-one" #{["C7-TYPE-MISMATCH" :list-prepend-arity-mismatch]}
   "list-prepend-arity-three" #{["C7-TYPE-MISMATCH" :list-prepend-arity-mismatch]}
   "list-prepend-unknown-value" #{["C7-ANNOTATION" :unsupported-or-unresolved-core-node-type]}
   "list-prepend-nil-member" #{["C7-TYPE-MISMATCH" :list-prepend-element-type-mismatch]}
   "list-prepend-homogeneous-to-bare" #{["C7-TYPE-MISMATCH" :call-argument-type-mismatch]}})

(deftest current-list-prepend-source-fixtures-are-byte-paired
  (let [stems (conj fixture-stems "local-runtime-list-prepend")]
    (doseq [stem stems]
      (let [family (if (= stem "local-runtime-list-prepend") "accepted" "rejected")
            gravity (fixture-path family stem ".gravity")
            qst (fixture-path family stem ".qst")]
        (testing stem
          (is (.exists (io/file gravity)))
          (is (.exists (io/file qst)))
          (is (= (slurp gravity) (slurp qst))))))))

(deftest current-list-prepend-source-composition
  (let [gravity-path (fixture-path "accepted" "local-runtime-list-prepend" ".gravity")
        qst-path (fixture-path "accepted" "local-runtime-list-prepend" ".qst")
        gravity (positive-input)
        facts (get-in gravity [:typed :identity-input])
        core (:core gravity)
        descriptor (bare-prepend-descriptor core facts)
        call-id (:call-core-node-id descriptor)
        row (first (filter #(= call-id (get-in % [:lhs :core-node-id]))
                           (:constraint-ledger facts)))
        effects (effects-products gravity)
        emitted (current-emitter-products gravity-path gravity)
        emitted-result (:result emitted)]
    (is (= (slurp gravity-path) (slurp qst-path)))
    (is (= :accepted (get-in gravity [:resolved :status])))
    (is (= :accepted (get-in gravity [:typed :status]))
        (pr-str (get-in gravity [:typed :diagnostics])))
    (is (= :passed (get-in gravity [:verification :status])))
    (is (map? emitted-result))
    (is (= :current-gravity-typed-lowering
           (get-in emitted-result [:typed-lowering-provenance :boundary])))
    (is (= gravity-path
           (get-in emitted-result [:typed-lowering-provenance :source :path])))
    (is (map? (get-in emitted-result
                     [:typed-lowering-provenance :compiler-sources :lowering])))
    (is (= {:op :builtin-call :function 'conj}
           (select-keys (get-in emitted-result [:instructions 0 :args 0])
                        [:op :function])))
    (is (= :gravity.type/applied-list-prepend (:kind descriptor)))
    (is (= :sh08-list-prepend-v1 (:version descriptor)))
    (is (= ['conj 'gravity.collections]
           [(:operation descriptor) (:semantic-owner descriptor)]))
    (is (= [:gravity.type/list :gravity.type/string]
           (:argument-types descriptor)))
    (is (= :gravity.type/list (:result-type descriptor)))
    (is (= [:memory/allocate] (:direct-effects descriptor)))
    (is (= [] (:latent-effects descriptor) (:capabilities descriptor)
           (:thrown-error-effects descriptor)))
    (is (= :second-operand (get-in descriptor [:prepend-semantics :head])))
    (is (= :complete-ordered-input (get-in descriptor [:prepend-semantics :suffix])))
    (is (= true (get-in descriptor [:prepend-semantics :input-preserved?])))
    (is (= :once (get-in descriptor [:prepend-semantics :operand-evaluation])))
    (is (map? (:source-call descriptor)))
    (is (map? (:source-edge descriptor)))
    (is (map? (:source-binding descriptor)))
    (is (= descriptor (:dependencies row)))
    (is (some? (get-in descriptor [:source-call :ordered-evaluation-node-ids])))
    (is (= :accepted (:status (:effects effects)))
        (pr-str (:effects effects)))
    (is (= :passed (:status (:effect-verification effects))))
    (is (not-any? #(contains? descriptor %)
                  [:operand-type-work :result-type-work
                   :construction-work :work-count
                   :input-node-quota :depth-quota :width-quota
                   :scalar-quota :byte-quota :token-quota]))))

(deftest current-list-prepend-source-negatives-reject
  (let [negative-shapes
        {"list-prepend-bare-head" "(first (conj parts \"head\"))"
         "list-prepend-wrong-member" ":wrong"
         "list-prepend-allocation" "(:effects #{})"
         "list-prepend-result-annotation" ":- String (conj parts \"head\")"
         "list-prepend-arity-one" "(conj parts)"
         "list-prepend-arity-three" "(conj parts \"a\" \"b\")"
         "list-prepend-unknown-value" "(conj parts value)"
         "list-prepend-nil-member" "(conj parts nil)"
         "list-prepend-homogeneous-to-bare" "(defn consume [parts :- List]"}]
    (doseq [stem fixture-stems]
      (let [fixture (fixture-path "rejected" stem ".gravity")
            source (slurp fixture)
            companion (slurp (fixture-path "rejected" stem ".qst"))
            current (source-products fixture)
            request (:request current)
            c7 (invoke-c7 'sh08-function-type-core-artifact [request])
            c7-again (invoke-c7 'sh08-function-type-core-artifact [request])]
        (testing stem
          (is (= source companion))
          (is (.contains source (get negative-shapes stem)))
          (is (seq (bootstrap/read-forms fixture source)))
          (is (= (c7-diagnostic-signature c7)
                 (c7-diagnostic-signature c7-again)))
          (if (= "list-prepend-allocation" stem)
            (let [verification (invoke-c7 'sh08-verify-function-type-result
                                          [request c7])
                  effects (effects-products {:request request
                                             :typed c7
                                             :verification verification})]
              ;; Allocation is a valid C7 type program; its actual owning
              ;; semantic rejection is the current C8 effect consumer.
              (is (= :accepted (:status c7))
                  (pr-str (c7-diagnostic-signature c7)))
              (is (= :passed (:status verification)))
              (is (= :rejected (get-in effects [:effects :status]))
                  (pr-str effects))
              (is (= ["C8-UNDECLARED" :function-effect-outside-source-allowance]
                     ((juxt :rule :reason)
                      (first (get-in effects [:effects :diagnostics]))))))
            (let [expected (get expected-c7-diagnostics stem)
                  diagnostics (:diagnostics c7)]
              (is (= :rejected (:status c7))
                  (pr-str (c7-diagnostic-signature c7)))
              (is (some #(contains? expected ((juxt :rule :reason) %)) diagnostics)
                  (pr-str [stem (c7-diagnostic-signature c7)])))))))))

(deftest current-list-prepend-source-tamper-controls-reject
  (let [positive (positive-input)
        request (:request positive)
        typed (:typed positive)
        verify (fn [candidate]
                 (invoke-c7 'sh08-verify-function-type-result
                            [request candidate]))
        descriptor-removals
        [(fn [descriptor] (update descriptor :source-call dissoc :ordered-evaluation-node-ids))
         (fn [descriptor] (assoc descriptor :source-edge :forged))
         (fn [descriptor] (assoc descriptor :source-binding :forged))
         (fn [descriptor] (assoc-in descriptor
                                    [:source-call :ordered-evaluation-node-ids]
                                    []))
         (fn [descriptor] (assoc descriptor :version :opaque))]
        row-missing #(update % :constraint-ledger (fn [rows] (vec (butlast rows))))
        row-duplicate #(update % :constraint-ledger
                               (fn [rows] (into (vec rows) [(last rows)])))
        orphan-fact #(update % :call-type-facts
                             (fn [facts]
                               (into (vec facts)
                                     (for [fact facts :when (:applied-list-prepend fact)]
                                       (assoc fact :call-core-node-id :orphan)))))
        fact-opaque #(update % :call-type-facts
                             (fn [facts]
                               (mapv (fn [fact]
                                       (if (:applied-list-prepend fact)
                                         (assoc fact :dispatch :opaque)
                                         fact))
                                     facts)))]
    (doseq [mutate descriptor-removals]
      (is (= :rejected (:status (verify (descriptor-mutant typed mutate))))))
    (is (= :rejected (:status (verify (row-missing typed)))))
    (is (= :rejected (:status (verify (row-duplicate typed)))))
    (is (= :rejected (:status (verify (orphan-fact typed)))))
    (is (= :rejected (:status (verify (fact-opaque typed)))))))
