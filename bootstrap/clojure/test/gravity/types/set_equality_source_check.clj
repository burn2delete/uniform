(ns gravity.types.set-equality-source-check
  "Manual genuine-source consumer selector. Loading this namespace launches nothing.")

(def fixture-paths
  (mapv #(str "bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/"
              "local-runtime-bare-set-equality." %) ["gravity" "qst"]))

(defn- require-result! [condition stage]
  (when-not condition
    (throw (ex-info "DSE source consumer check failed" {:stage stage}))))

(defn check!
  "Execute only through the supplied operations. Genuine operations are selected
  by -main; cheap controls inject operations without loading Bootstrap."
  [{:keys [read-source read-forms source-request c7 body emit]}]
  (let [[g q] fixture-paths texts (mapv read-source fixture-paths)]
    (require-result! (= (first texts) (second texts)) :paired-bytes)
    (require-result! (= (read-forms g (first texts)) (read-forms q (second texts))) :paired-parser)
    (let [request (:request (source-request g (first texts)
                                          {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}))
          core (:canonical-core-artifact request)
          typed (c7 'sh08-function-type-core-artifact [request])]
      (require-result! (= :accepted (:status typed)) :c7-producer)
      (require-result! (= :passed (:status (c7 'sh08-verify-function-type-result [request typed]))) :c7-fresh-check)
      (let [types (:type-table typed)
            selected (filterv #(= :gravity.type/applied-checked-bare-set-equality
                                  (get-in types [(:operator-node-id %) :kind])) (:calls core))]
        (require-result! (= 1 (count selected)) :one-dse-call)
        (let [call (first selected) id (:core-node-id call) descriptor (types (:operator-node-id call))
              bindings (into {} (map (juxt :binding-id identity)
                                    (get-in request [:b47-context :authenticated-core-request :binding-table])))
              prepared (body 'body-ae-context request typed bindings 65536)]
          (require-result! (= [:gravity.type/set :gravity.type/set] (:argument-types descriptor)) :bare-operands)
          (require-result! (= :accepted (:status prepared)) :effects-context)
          (let [context (:context prepared) nodes (:nodes context) node (nodes id) edge (get (:edges context) id)
                facts (assoc typed :equality-context context)
                argument-bindings (mapv #(get-in nodes [% :attributes :binding-id]) (:argument-node-ids call))
                names (mapv #(get-in bindings [% :name]) argument-bindings)
                current (first (filter #(= (:caller-function-syntax-id edge) (:function-syntax-id %))
                                       (:function-records core)))
                lowering (assoc context :core core :types types :ledger (:constraint-ledger typed)
                                :call-facts (:call-type-facts typed) :current-function current
                                :call-order (into {} (map (juxt :call-core-node-id :ordinal) (:call-edges core)))
                                :locals (zipmap argument-bindings names)
                                :parameter-types (zipmap argument-bindings (:argument-types descriptor)))
                expected {:op :builtin-call :function '= :args (mapv #(hash-map :op :local :name %) names)}]
            (require-result! (every? symbol? names) :original-parameter-names)
            (require-result! (true? (body 'body-ae-call-valid? core facts bindings node call edge nodes)) :effects-call)
            (let [compiled (emit 'p15-s23-typed-compile-node lowering id 65536)]
              (require-result! (= :complete (:status compiled)) :typed-node)
              (require-result! (= expected (:value compiled)) :ordered-original-operands))
            ;; Replay the exact eight independent-review mutations against the
            ;; actual consumer functions, with the real source-derived product.
            (let [row (first (filter #(= id (get-in % [:lhs :core-node-id])) (:constraint-ledger typed)))
                  fact (first (filter #(= id (:call-core-node-id %)) (:call-type-facts typed)))
                  stripped-row (dissoc row :producer-rule :constraint-id-request)
                  stripped-fact (dissoc fact :dispatch :fact-id-request)]
              (doseq [invoke [body emit]
                      mutation [:duplicate-row :orphan-row :duplicate-fact :orphan-fact]]
                (let [ledger (case mutation
                               :duplicate-row (conj (:constraint-ledger typed) stripped-row)
                               :orphan-row (conj (:constraint-ledger typed) (assoc-in stripped-row [:lhs :core-node-id] :orphan))
                               (:constraint-ledger typed))
                      call-facts (case mutation
                                   :duplicate-fact (conj (:call-type-facts typed) stripped-fact)
                                   :orphan-fact (conj (:call-type-facts typed) (assoc stripped-fact :call-core-node-id :orphan))
                                   (:call-type-facts typed))
                      function (if (identical? invoke body) 'effects-set-equality-evidence 'emit-set-equality-evidence)
                      result (invoke function core ledger call-facts node call edge nodes bindings types)]
                  (require-result! (= [:rejected :invalid-checked-bare-set-equality-source]
                                      ((juxt :status :reason) result)) mutation))))
            {:status :passed :selector :current-g-dse-consumers
             :genuine-source-extension ".gravity" :companion-check :bytes-and-seed-parser-only
             :c7 :produced-and-freshly-verified :effects :context-and-call :emitter :typed-call
             :independent-review-regressions 8 :subject-runtime-executed false
             :paired-typed-acceptance :not-established :authority :development-only}))))))

(defn- live-operations []
  ;; Reuse the unchanged committed helpers and their original plan constructors.
  ;; This is the only require that loads Bootstrap, and only -main calls it.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [helper #(or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test %)
                   (throw (ex-info "Missing committed DSE verification helper" {:helper %})))]
    {:read-source slurp
     :read-forms (ns-resolve 'gravity.bootstrap 'read-forms)
     :source-request (helper 'function-work-policy-current-source)
     :c7 (helper 'invoke-c7)
     :body (helper 'pr359-descriptor-body)
     :emit (helper 'pr359-descriptor-emit)}))

(defn -main [& args]
  (require-result! (empty? args) :closed-selector)
  (prn (check! (live-operations))))
