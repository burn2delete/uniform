(ns gravity.types.pr502-preserved-fixture-consumer-test
  "Consume the preserved bare-Set source through current C7, Effects, and
  typed-emitter owners.

  This is a development source-composition consumer.  It does not establish
  public B47/C8/L5 authority, a runtime certificate, migration support, or
  release readiness."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Required current source/C7 helper is absent" {:name name}))))

(defn- c7 [function arguments]
  ((helper 'invoke-c7) function arguments))

(defn- body [function & arguments]
  (apply (or (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
             (throw (ex-info "Required current Effects helper is absent" {})))
         function arguments))

(def ^:private lowering-plan
  (delay
    (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                              path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))

(defn- lower [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :pr502-preserved-dse-fixture-consumer}
   @lowering-plan function arguments))

(defn- by-id [rows key]
  (into {} (map (juxt key identity) rows)))

(defn- source-products []
  (let [source-path ((helper 'fixture-path)
                     "preserved" "local-runtime-bare-set-equality" ".gravity")
        qst-path ((helper 'fixture-path)
                  "preserved" "local-runtime-bare-set-equality" ".qst")
        source-text (slurp source-path)
        qst-text (slurp qst-path)
        {:keys [request]} ((helper 'function-current-source) source-path)
        core (:canonical-core-artifact request)
        result (c7 'sh08-function-type-core-artifact [request])
        nodes (by-id (:nodes core) :node-id)
        calls (by-id (:calls core) :core-node-id)
        edges (by-id (:call-edges core) :call-core-node-id)
        forms (by-id (get-in request [:b47-context :authenticated-core-request :forms])
                     :form-id)
        bindings (by-id (get-in request [:b47-context :authenticated-core-request :binding-table])
                        :binding-id)
        selected (filter #(= :gravity.type/applied-checked-bare-set-equality
                             (get-in result [:type-table (:operator-node-id %)
                                             :kind]))
                         (:calls core))]
    (when-not (= 1 (count selected))
      (throw (ex-info "Preserved source did not yield one checked bare-Set call"
                      {:selected-count (count selected)})))
    (let [call (first selected)
          id (:core-node-id call)
          node (get nodes id)
          edge (get edges id)
          descriptor (get-in result [:type-table (:operator-node-id call)])
          owner-function-syntax-id (get-in node [:evaluation :owner-function-syntax-id])
          current-function (first (filter #(= owner-function-syntax-id
                                              (:function-syntax-id %))
                                          (:function-records core)))
          locals (into {}
                       (keep (fn [argument-id]
                               (let [argument (get nodes argument-id)
                                     binding-id (get-in argument [:attributes :binding-id])]
                                 (when binding-id
                                   [binding-id (:name (get bindings binding-id))]))))
                       (:argument-node-ids call))
          parameter-types (into {}
                               (keep (fn [argument-id]
                                       (let [argument (get nodes argument-id)
                                             binding-id (get-in argument [:attributes :binding-id])]
                                         (when binding-id
                                           [binding-id (get-in result [:type-table argument-id])]))))
                               (:argument-node-ids call))
          context {:core core :node node :call call :edge edge
                   :nodes nodes :forms forms :bindings bindings
                   :types (:type-table result) :calls calls :edges edges
                   :call-order (into {}
                                     (map-indexed (fn [index current]
                                                    [(:core-node-id current) index])
                                                  (:calls core)))
                   :current-function current-function :locals locals
                   :parameter-types parameter-types
                   :ledger (:constraint-ledger result)
                   :call-facts (:call-type-facts result)}
          ledger (:constraint-ledger result)
          facts (:call-type-facts result)
          row (first (filter #(and (= id (get-in % [:lhs :core-node-id]))
                                   (= :sh08-checked-bare-set-equality-v1
                                      (:producer-rule %)))
                             ledger))
          fact (first (filter #(and (= id (:call-core-node-id %))
                                    (= :direct-checked-bare-set-equality
                                       (:dispatch %)))
                              facts))]
      (when-not (and row fact current-function)
        (throw (ex-info "Current C7 result lacks the source DSE products"
                        {:row? (boolean row) :fact? (boolean fact)
                         :current-function? (boolean current-function)})))
      {:source-path source-path :qst-path qst-path
       :source-text source-text :qst-text qst-text
       :request request :core core :result result
       :nodes nodes :calls calls :edges edges :forms forms :bindings bindings
       :call call :id id :node node :edge edge :descriptor descriptor
       :ledger ledger :facts facts :row row :fact fact :context context})))

(def ^:private products (delay (source-products)))

(defn- effects-evidence [{:keys [core ledger facts node call edge nodes bindings result]}]
  (body 'body-bka-evidence core ledger facts node call edge nodes bindings
        (:type-table result)))

(defn- effects-call-valid? [{:keys [core ledger facts forms node call edge nodes bindings result]}]
  (body 'body-bka-call-valid?
        core {:constraint-ledger ledger
              :call-type-facts facts
              :type-table (:type-table result)
              :equality-context {:forms forms}}
        bindings node call edge nodes))

(defn- effects-row-census [{:keys [core ledger nodes bindings result calls edges id descriptor]}]
  (body 'effects-set-equality-row-census core ledger nodes bindings
        (:type-table result) calls edges id descriptor))

(defn- effects-fact-census [{:keys [core facts descriptor id]}]
  (body 'effects-set-equality-fact-census facts (:module core) {id descriptor} id))

(defn- emitter-row-census [{:keys [core ledger nodes bindings result calls edges id descriptor]}]
  (lower 'emit-set-equality-row-census
         [core ledger nodes bindings (:type-table result) calls edges id descriptor]))

(defn- emitter-fact-census [{:keys [core facts descriptor id]}]
  (lower 'emit-set-equality-fact-census
         [facts (:module core) {id descriptor} id]))

(defn- emitter-compile [{:keys [context id]}]
  (lower 'p15-s23-typed-compile-node [context id]))

(defn- with-ledger-facts [p ledger facts]
  (assoc p :ledger ledger :facts facts
         :context (assoc (:context p) :ledger ledger :call-facts facts)))

(defn- with-types [p types]
  (assoc p :result (assoc (:result p) :type-table types)
         :context (assoc (:context p) :types types)))

(defn- rejected-status? [value]
  (= :rejected (:status value)))

(defn- opaque-value [callbacks]
  (proxy [Object] []
    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
    (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
    (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque spelling")))))

(deftest preserved-bare-set-source-composes-through-current-owners
  (let [p @products
        {:keys [source-path qst-path source-text qst-text core result call node edge
                descriptor row fact]} p
        expected-compile {:op :builtin-call :function '=
                          :args [{:op :local :name 'left}
                                 {:op :local :name 'right}]}
        body-header (body 'effects-set-equality-header core node call edge
                          (:nodes p) (:bindings p) (:type-table result)
                          (:module core))
        body-result (effects-evidence p)
        row-result (effects-row-census p)
        fact-result (effects-fact-census p)
        emitted-row (emitter-row-census p)
        emitted-fact (emitter-fact-census p)
        compiled (emitter-compile p)
        verification (c7 'sh08-verify-function-type-result [(:request p) result])]
    (testing "the preserved source/QST bytes are the exact paired input"
      (is (= "a65f559b1bb75251268783e6addc406a5eea5359f97ea041b008157631f0f373"
             (digest/sha256-hex source-text)))
      (is (= source-text qst-text))
      (is (.exists (java.io.File. source-path)))
      (is (.exists (java.io.File. qst-path))))
    (testing "C7 derives one source-bound DSE descriptor and canonical products"
      (is (= :accepted (:status result)))
      (is (empty? (:diagnostics result)))
      (is (= 1 (count (filter #(= :gravity.type/applied-checked-bare-set-equality
                                   (get-in result [:type-table (:operator-node-id %)
                                                   :kind]))
                              (:calls core)))))
      (is (= [:gravity.type/applied-checked-bare-set-equality
              :sh08-checked-bare-set-equality-v1]
             ((juxt :kind :version) descriptor)))
      (is (= [:gravity.type/set :gravity.type/set] (:argument-types descriptor)))
      (is (= :gravity.type/bool (:result-type descriptor)))
      (is (= [nil nil] (:operand-counts descriptor)))
      (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:effects fact)))
      (is (= call (:source-call descriptor)))
      (is (= edge (:source-edge descriptor)))
      (is (= (get (:bindings p) (:operator-binding-id call))
             (:source-binding descriptor)))
      (is (= (:source node) (:source descriptor)))
      (is (= (:children node) (:ordered-evaluation-node-ids call)))
      (is (= (:children node) (:ordered-evaluation-node-ids edge)))
      (is (= (:source-call descriptor)
             (:source-call (:applied-checked-bare-set-equality fact))))
      (is (= row (first (filter #(= (:core-node-id call)
                                   (get-in % [:lhs :core-node-id]))
                               (:constraint-ledger result)))))
      (is (= fact (first (filter #(= (:core-node-id call)
                                    (:call-core-node-id %))
                                (:call-type-facts result))))))
    (testing "fresh C7 verification authenticates the exact source joins"
      (is (= :passed (:status verification)))
      (doseq [[field value]
              [[:source-call (assoc call :forged true)]
               [:source-edge (assoc edge :forged true)]
               [:source-binding (assoc (:source-binding descriptor) :forged true)]]]
        (let [mutated (assoc-in result
                                [:type-table (:operator-node-id call) field]
                                value)
              rejected (c7 'sh08-verify-function-type-result
                          [(:request p) mutated])]
          (is (= :rejected (:status rejected))
              (str "C7 accepted forged " field)))))
    (testing "Effects routes the source products through its checked owner"
      (is (= :accepted (:status body-header)))
      (is (= descriptor (:descriptor body-header)))
      (is (= :accepted (:status body-result)))
      (is (= descriptor (:descriptor body-result)))
      (is (true? (effects-call-valid? p)))
      (is (= :accepted (:status row-result)))
      (is (= [row] (:items row-result)))
      (is (= :accepted (:status fact-result)))
      (is (= [fact] (:items fact-result))))
    (testing "the current typed emitter consumes the same source joins"
      (is (= :accepted (:status emitted-row)))
      (is (= [row] (:items emitted-row)))
      (is (= :accepted (:status emitted-fact)))
      (is (= [fact] (:items emitted-fact)))
      (is (= :complete (:status compiled)))
      (is (= expected-compile (:value compiled))))))

(deftest preserved-bare-set-source-rejects-ledger-and-fact-shapes
  (let [p @products
        {:keys [ledger facts row fact]} p
        candidates
        [{:label :missing-producer
          :p (with-ledger-facts p
               (mapv #(if (= % row) (dissoc % :producer-rule) %) ledger) facts)}
         {:label :missing-dispatch
          :p (with-ledger-facts p ledger
               (mapv #(if (= % fact) (dissoc % :dispatch) %) facts))}
         {:label :duplicate-row
          :p (with-ledger-facts p (conj ledger row) facts)}
         {:label :duplicate-fact
          :p (with-ledger-facts p ledger (conj facts fact))}
         {:label :orphan-row
          :p (with-ledger-facts p
               (mapv #(if (= % row)
                        (assoc-in % [:lhs :core-node-id]
                                  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        %) ledger) facts)}
         {:label :orphan-fact
          :p (with-ledger-facts p ledger
               (mapv #(if (= % fact)
                        (assoc % :call-core-node-id
                               "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        %) facts))}
         {:label :forged-dse-producer
          :p (with-ledger-facts p
               (mapv #(if (= % row) (assoc % :producer-rule :forged) %) ledger) facts)}
         {:label :forged-dse-dispatch
          :p (with-ledger-facts p ledger
               (mapv #(if (= % fact) (assoc % :dispatch :forged) %) facts))}
         {:label :unknown-foreign-producer
          :p (with-ledger-facts p
               (mapv #(if (= % row)
                        (assoc (assoc % :producer-rule :forged)
                               :constraint-id-request {:domain :gravity/unknown-constraint})
                        %) ledger) facts)}
         {:label :unknown-foreign-dispatch
          :p (with-ledger-facts p ledger
               (mapv #(if (= % fact)
                        (assoc (assoc % :dispatch :forged)
                               :fact-id-request {:domain :gravity/unknown-fact})
                        %) facts))}]]
    (doseq [{:keys [label p]} candidates]
      (testing (str "Effects rejects " label)
        (is (rejected-status? (effects-evidence p)))
        (is (rejected-status? (emitter-compile p)))))))

(deftest preserved-bare-set-source-rejects-source-integrity
  (let [p @products
        {:keys [call descriptor result]} p
        source-mutants
        [[:source-call (assoc call :forged true)]
         [:source-edge (assoc (:edge p) :forged true)]
         [:source-binding (assoc (:source-binding descriptor) :forged true)]]]
    (doseq [[field value] source-mutants]
      (let [mutated (assoc descriptor field value)
            candidate (with-types p (assoc (:type-table result)
                                           (:operator-node-id call) mutated))]
        (testing (str "source-integrity mutation " field)
          (is (rejected-status? (effects-evidence candidate)))
          (is (rejected-status? (emitter-compile candidate))))))))

(deftest preserved-bare-set-source-rejects-stale-evaluation-joins
  (let [p @products
        changed-call (assoc (:call p) :ordered-evaluation-node-ids [])
        changed-edge (assoc (:edge p) :ordered-evaluation-node-ids [])]
    (testing "source/evaluation joins reject stale call and edge ordering"
      (is (rejected-status?
           (body 'body-bka-evidence (:core p) (:ledger p) (:facts p)
                 (:node p) changed-call (:edge p) (:nodes p) (:bindings p)
                 (:type-table (:result p)))))
      (is (rejected-status?
           (body 'body-bka-evidence (:core p) (:ledger p) (:facts p)
                 (:node p) (:call p) changed-edge (:nodes p) (:bindings p)
                 (:type-table (:result p))))))))

(deftest preserved-bare-set-source-rejects-opaque-producer-and-dispatch
  (let [p @products
        {:keys [ledger facts row fact]} p
        callbacks (atom 0)
        opaque (opaque-value callbacks)
        opaque-row (mapv #(if (= % row) (assoc % :producer-rule opaque) %) ledger)
        opaque-facts (mapv #(if (= % fact) (assoc % :dispatch opaque) %) facts)]
    (is (rejected-status? (effects-evidence (with-ledger-facts p opaque-row facts))))
    (is (rejected-status? (emitter-compile (with-ledger-facts p opaque-row facts))))
    (is (rejected-status? (effects-evidence (with-ledger-facts p ledger opaque-facts))))
    (is (rejected-status? (emitter-compile (with-ledger-facts p ledger opaque-facts))))
    (is (zero? @callbacks))))
