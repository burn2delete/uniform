(ns gravity.self-hosting.sh07-b51-v9-sh06-external-binding-seam-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "required SH06 lexical seam is absent"
                      {:symbol symbol}))))

(defn- invoke-private [symbol & arguments]
  (apply (required-var symbol) arguments))

(defn- c5-failure? [thunk]
  (try
    (thunk)
    false
    (catch clojure.lang.ExceptionInfo error
      (= "C5-UNRESOLVED" (:id (ex-data error))))))

(deftest strict-context-arities-are-explicit-and-nil-fails-closed
  (testing "the governed lexical arities are present"
    (is (= '([source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists (meta (required-var 'sh06-resolution-request)))))
    (is (= '([artifact] [artifact context])
           (:arglists
            (meta (required-var 'sh06-resolution-artifact-verification)))))
    (is (= '([resolution-artifact] [resolution-artifact context])
           (:arglists (meta (required-var 'sh07-core-authenticated-request)))))
    (is (= '([sh05-artifact]
             [source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists
            (meta
             (required-var
              'sh06-resolution-artifact-from-sh05-artifact))))))
  (testing "nil never selects an ordinary implementation"
    (is (c5-failure?
         #(bootstrap/sh06-resolution-request "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-artifact-from-sh05-artifact
           "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-root nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-plan-binding!
           "module.gravity" nil)))))

(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- m0-row [ordinal module owner]
  {:module-source-id (digest (char (+ (int \a) ordinal)))
   :module-ordinal ordinal :module module :owner owner
   :logical-source-path (str "module-" ordinal ".gravity")
   :profile :meta :target :jvm :effects #{} :capabilities #{}
   :safety :safe :definitions [] :external-bindings []})

(defn- minimal-authority-family []
  (let [symbol 'provider
        source-hash (digest \f)
        caller-base (m0-row 0 'gravity.caller :r1-producer)
        callee-base (m0-row 1 'gravity.provider :r1-producer)
        external {:symbol symbol :callee-module-ordinal 1
                  :callee-module (:module callee-base)
                  :callee-source-sha256 (digest \b)
                  :callee-definition-source-hash source-hash
                  :arity 1 :owner-edge :producer-internal}
        caller (assoc caller-base :external-bindings [external])
        callee (assoc callee-base
                      :source-sha256 (digest \b)
                      :definitions [{:name symbol :arity 1
                                     :visibility :private
                                     :source-hash source-hash}])
        descriptors (into [caller callee]
                          (mapv (fn [ordinal]
                                  (m0-row ordinal
                                          (symbol (str "gravity.m" ordinal))
                                          :r1-producer))
                                (range 2 7)))
        edge {:ordinal 0
              :caller-module-source-id (:module-source-id caller)
              :caller-function 'caller
              :caller-function-source-hash (digest \e)
              :callsite-path [0]
              :callee-module-source-id (:module-source-id callee)
              :callee-function symbol
              :callee-function-source-hash source-hash
              :arity 1 :owner-edge :producer-internal
              :authorization :authenticated-b51-namespace-external}
        manifest {:manifest-id (digest \9) :edges [edge]}]
    {:descriptors descriptors :manifest manifest}))

(deftest visibility-is-granted-only-by-an-exact-authenticated-edge
  (let [{:keys [descriptors manifest]} (minimal-authority-family)
        project
        #(invoke-private 'sh06-resolution-c5-expected-projection!
                         "module-0.gravity" 0 descriptors %)]
    (testing "an exact M1 grant promotes the provider into a public SH06 import"
      (let [projection (project manifest)
            binding (first (:import-bindings projection))]
        (is (= 'provider (:name binding)))
        (is (= :public (:visibility binding)))
        (is (= :import (:binding-class binding)))
        (is (= {:source :external-binding} (:source-span binding)))
        (is (= [0]
               (get-in binding
                       [:semantic-span :authorized-edge-ordinals])))))
    (testing "removing authorization rejects before a projection is returned"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :authorization]
                               :unauthorized)))))
    (testing "changing the owner edge also rejects"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :owner-edge]
                               :root6-internal)))))))

(deftest ordinary-and-strict-paths-have-no-ambient-authority-seam
  (let [bounded (meta (required-var
                       'sh06-resolution-artifact-verification-bounded*))
        candidate (meta (required-var
                         'sh06-resolution-source-artifact-candidate))]
    (is (:private bounded))
    (is (:private candidate))
    (is (= '([artifact construction?]
             [artifact construction? context])
           (:arglists bounded)))
    (is (= '([source-path sh05-artifact legacy?]
             [source-path sh05-artifact legacy? context])
           (:arglists candidate)))))

;; C6 mapping erratum V1. Exercise the real producer and the exact consumer
;; admission helper, without replacing C2-C6 or claiming source-session closure.
(def ^:private mapping-fixture
  "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/macro-def-fn-literals.gravity")
(def ^:private mapping-artifact
  (delay (bootstrap/sh07-core-source-artifact mapping-fixture (slurp mapping-fixture))))
(def mapping-evidence (atom {}))

(defn- mapping-values []
  (let [boundary (:gravity-core-boundary @mapping-artifact)
        d (:digest-requests boundary) z (:resolved-digests boundary)
        t (get-in boundary [:raw-template-result :core-template])
        rc (:value (bootstrap/sh07-core-execute!
                    mapping-fixture 'sh07-resolve-core-template [t z]))]
    {:q (:authenticated-core-request boundary) :t t :rc rc :d d :z z}))

(defn- mapping-admit [q rc d z]
  (invoke-private 'p15-s23-sh07-b51-verify-module-source-core-map!
                  mapping-fixture q rc d z))

(defn- mapping-rejection [q rc d z]
  (try (mapping-admit q rc d z) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest c6-map-real-producer-is-total-with-consumed-and-generated-forms
  (let [{:keys [q t rc d z]} (mapping-values)
        ordinal (dec (count d)) preimage (:preimage (last d))
        sidecar (assoc (:value (bootstrap/sh07-core-execute!
                        mapping-fixture 'sh07-resolve-source-core-map-preimage
                        [preimage (subvec z 0 ordinal)])) :artifact-id (last z))
        rows (:records sidecar)
        emitted (filter #(= :emitted (:disposition %)) rows)
        consumed (filter #(= :consumed (:disposition %)) rows)
        pass (mapping-admit q rc d z)]
    (is (= :accepted (:status @mapping-artifact)))
    (is (= [49 21 24 28] [(count (:forms q)) (count (:nodes rc)) (count d) (count consumed)]))
    (is (= 49 (count rows)))
    (is (= (mapv :form-id (:forms q)) (mapv #(get-in % [:source :form-id]) rows)))
    (is (= (set (map :node-id (:nodes rc))) (set (mapcat :emitted-node-ids emitted))))
    (is (= 21 (count (mapcat :emitted-node-ids emitted))))
    (is (= 2 (count (filter :generated? (:nodes rc)))))
    (is (every? #(and (seq (:anchor-node-ids %)) (empty? (:emitted-node-ids %))) consumed))
    (is (= :sh07-source-core-map-id (:purpose (last d))))
    (is (= (:artifact-id-request rc) (:core-artifact-id sidecar)))
    (is (= (conj (set (keys preimage)) :artifact-id) (set (keys sidecar))))
    (is (= (last z) (:artifact-id sidecar)))
    (is (= {:artifact :gravity/sh07-source-core-map-verification :schema-version 1
            :status :passed :rule nil :reason nil :details {}} pass))
    (is (= z (bootstrap/sh07-core-digest-requests mapping-fixture d)))
    (swap! mapping-evidence assoc :real-producer
           {:forms 49 :nodes 21 :dispositions 49 :consumed 28 :generated 2
            :digest-requests 24 :map-id (last z) :core-id (:artifact-id-request rc)
            :carrier (bootstrap/sh07-core-execute!
                      mapping-fixture 'sh07-generated-digest-carrier-preflight [d])})))

(deftest c6-map-coherently-rehashed-metadata-and-digest-mutations-reject
  (let [{:keys [q t rc d z]} (mapping-values)
        n (dec (count d)) rows (get-in d [n :preimage :records])
        emitted (first (keep-indexed #(when (= :emitted (:disposition %2)) %1) rows))
        generated (first (keep-indexed #(when (seq (get-in %2 [:source :generated-origin])) %1) rows))
        consumed (first (keep-indexed #(when (= :consumed (:disposition %2)) %1) rows))
        mutate #(update-in d [n :preimage :records] %)
        mutations
        {:missing-row (mutate pop)
         :extra-row (mutate #(conj % (assoc (first %) :ordinal (count %))))
         :row-order (mutate #(assoc % 0 (get % 1) 1 (get % 0)))
         :parent (assoc-in d [n :preimage :records consumed :parent-form-id] (digest \0))
         :role (assoc-in d [n :preimage :records consumed :reason] :unknown-role)
         :semantic-child-consumed (-> d
                                     (assoc-in [n :preimage :records emitted :disposition] :consumed)
                                     (assoc-in [n :preimage :records emitted :reason] :quoted-data)
                                     (assoc-in [n :preimage :records emitted :emitted-node-ids] []))
         :foreign-owner (assoc-in d [n :preimage :records consumed :owner-form-id] (digest \0))
         :missing-anchor (assoc-in d [n :preimage :records consumed :anchor-node-ids] [])
         :duplicate-emitted (update-in d [n :preimage :records emitted :emitted-node-ids]
                                       #(conj % (first %)))
         :origin (assoc-in d [n :preimage :records generated :source :generated-origin] [])
         :span (update-in d [n :preimage :records emitted :source :semantic-span :byte-start] inc)
         :foreign-core-reference (assoc-in d [n :preimage :core-artifact-id] (get-in t [:provenance-binding-id-request]))
         :forward-node-reference (assoc-in d [n :preimage :records consumed :anchor-node-ids 0]
                                          {:artifact :gravity/sh07-internal-digest-reference
                                           :schema-version 1 :ordinal n :authority :sh07-digest-resolver})
         :unknown-purpose (assoc-in d [n :purpose] :caller-purpose)}]
    (doseq [[label altered] mutations]
      (let [failure (mapping-rejection q rc altered z)]
        (is (= "SH07-B51-00945-GOVERNED-READ" (:id failure)) (str label))
        (is (= :module-source-core-mapping (:reason failure))))
      (let [result (try
                     (let [new-z (bootstrap/sh07-core-digest-requests mapping-fixture altered)
                           new-rc (:value (bootstrap/sh07-core-execute!
                                           mapping-fixture 'sh07-resolve-core-template [t new-z]))]
                       (mapping-rejection q new-rc altered new-z))
                     (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (contains? #{"C6-VERIFY" "SH07-B51-00945-GOVERNED-READ"} (:id result)) (str label))
        (swap! mapping-evidence assoc-in [:mutations label] (select-keys result [:id :reason :details]))))
    (doseq [[label altered-rc altered-z]
            [[:sha-shaped-substitution rc (assoc z 0 (digest \0))]
             [:sidecar-sha-substitution rc (assoc z n (digest \0))]
             [:namespace (assoc-in rc [:module :namespace] 'gravity.altered) z]
             [:source-map (assoc-in rc [:source-map 0 :form-id] (digest \0)) z]
             [:semantic-record (assoc-in rc [:definitions 0 :name] 'altered) z]]]
      (let [result (mapping-rejection q altered-rc d altered-z)]
        (is (= "SH07-B51-00945-GOVERNED-READ" (:id result)) (str label))
        (is (= :module-source-core-mapping (:reason result)))
        (swap! mapping-evidence assoc-in [:mutations label] (select-keys result [:id :reason :details]))))))

(deftest c6-map-carrier-capacity-rejects-through-the-existing-preflight
  (let [{:keys [d]} (mapping-values)
        too-wide (assoc (:preimage (last d)) :records (vec (repeat 65537 nil)))
        preflight (bootstrap/sh07-core-execute!
                   mapping-fixture 'sh07-generated-digest-carrier-preflight [too-wide])
        resolved (bootstrap/sh07-core-execute!
                  mapping-fixture 'sh07-resolve-source-core-map-preimage [too-wide []])]
    (is (= :rejected (:status preflight)))
    (is (= {:status :rejected :reason :source-core-map-carrier-bound} resolved))
    (swap! mapping-evidence assoc :capacity preflight)))

(deftest c6-map-pure-gravity-joins-reject-semantic-record-and-authority-mutations
  (let [{:keys [q t rc d z]} (mapping-values)
        base (:value (bootstrap/sh07-core-execute!
                      mapping-fixture 'sh07-resolve-source-core-map-preimage
                      [(:preimage (last d)) (pop z)]))
        rows (:records base)
        row-index (bootstrap/sh07-core-execute! mapping-fixture 'sh07-source-core-map-row-index [rows])
        execute #(bootstrap/sh07-core-execute! mapping-fixture %1 %2)
        quote-index (first (keep-indexed #(when (= :quote (:core-form %2)) %1) (:nodes rc)))
        def-index (first (keep-indexed #(when (= :def (:core-form %2)) %1) (:nodes rc)))]
    (is (true? (execute 'sh07-source-core-map-semantic-joins? [q rows rc])))
    (is (false? (execute 'sh07-source-core-map-semantic-joins?
                        [q rows (assoc-in rc [:definitions 0 :name] 'wrong)])))
    (is (false? (execute 'sh07-source-core-map-semantic-joins?
                        [q rows (assoc-in rc [:function-records 0 :parameter-binding-ids] [])])))
    (is (false? (execute 'sh07-source-core-map-semantic-joins?
                        [q rows (assoc-in rc [:nodes quote-index :attributes :quoted-value] :wrong)])))
    (is (false? (execute 'sh07-source-core-map-semantic-joins?
                        [q rows (assoc-in rc [:nodes def-index :core-form] :call)])))
    (is (false? (execute 'sh07-source-core-map-source-joins?
                        [q (assoc base :root-form-ids []) row-index])))
    (is (false? (execute 'sh07-source-core-map-reference-purposes?
                        [(assoc (:preimage (last d)) :core-artifact-id (:provenance-binding-id-request t))
                         d (count (:nodes rc))])))
    (is (false? (execute 'sh07-source-core-node-identities-unique?
                        [(conj (:nodes rc) (first (:nodes rc)))])))))

(deftest c6-map-every-consumed-role-has-a-genuine-semantic-record-join
  (let [roles (atom #{})
        cases [["sh-07" "quoted-carrier-payloads" nil]
               ["sh-07" "latent-function-order" [:function-records 0 :body-core-node-id]]
               ["sh-07-b3" "one-binding-let" [:lexical-bindings 0 :definition-syntax-id]]
               ["sh-07-b4" "sequential-loop-bindings" [:loop-bindings 0 :definition-syntax-id]]
               ["sh-07-b5" "same-namespace-var-object" [:var-references 0 :operand-syntax-id]]
               ["sh-07-b6" "function-var-mutation" [:mutations 0 :target-syntax-id]]
               ["sh-07-b9" "two-catches-source-order" [:error-handlers 0 :catch-binding-syntax-id]]
               ["sh-07-b11" "nested-vector-patterns" [:match-pattern-records 0 :pattern-syntax-id]]]]
    (doseq [[family name mutation-path] cases]
      (let [path (str "bootstrap/clojure/fixtures/self-hosting/" family "/accepted/" name ".gravity")
            artifact (bootstrap/sh07-core-source-artifact path (slurp path))
            boundary (:gravity-core-boundary artifact)
            q (:authenticated-core-request boundary) d (:digest-requests boundary) z (:resolved-digests boundary)
            t (get-in boundary [:raw-template-result :core-template])
            rc (:value (bootstrap/sh07-core-execute! path 'sh07-resolve-core-template [t z]))
            base (:value (bootstrap/sh07-core-execute! path 'sh07-resolve-source-core-map-preimage
                                                      [(:preimage (last d)) (pop z)]))
            rows (:records base)
            pass (invoke-private 'p15-s23-sh07-b51-verify-module-source-core-map! path q rc d z)]
        (is (= :accepted (:status artifact)) name)
        (is (= :passed (:status pass)) name)
        (is (= (count (:forms q)) (count rows)) name)
        (is (true? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-record-joins? [rc rc])) name)
        (doseq [field-path (case family
                            "sh-07-b3" [[:lexical-bindings 0 :binding-scope-id]]
                            "sh-07-b4" [[:loop-bindings 0 :binding-scope-id]]
                            "sh-07-b5" [[:var-references 0 :symbol] [:var-references 0 :binding-class]
                                        [:var-references 0 :definition-kind] [:var-references 0 :resolution-order]]
                            "sh-07-b6" [[:mutations 0 :target-binding-class]]
                            "sh-07-b9" [[:error-handlers 0 :protected-core-node-id] [:error-handlers 0 :handler-child-index]
                                        [:error-handlers 0 :clause-count] [:error-handlers 0 :catch-clause-syntax-id]]
                            "sh-07-b11" [[:match-pattern-records 0 :parent-ordinal] [:match-pattern-records 0 :root-ordinal]
                                         [:match-pattern-records 0 :pattern-binding-scope-id]
                                         [:match-pattern-records 0 :pattern-binding-use-syntax-ids]] [])]
          (is (contains? (get-in rc (pop field-path)) (peek field-path)) (str name " " field-path))
          (is (false? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-record-joins?
                        [(assoc-in rc field-path :altered-record-field) rc])) (str name " " field-path)))
        (swap! roles into (map :reason rows))
        (when mutation-path
          (is (false? (bootstrap/sh07-core-execute!
                       path 'sh07-source-core-map-semantic-joins?
                       [q rows (assoc-in rc mutation-path (digest \0))])) name))
        (when (= family "sh-07-b6")
          (is (false? (bootstrap/sh07-core-execute!
                       path 'sh07-source-core-map-semantic-joins?
                       [q rows (assoc-in rc [:mutations 0 :target-syntax-id] (digest \0))]))))
        (when (= name "latent-function-order")
          (let [i (first (keep-indexed #(when (= :fn (:core-form %2)) %1) (:nodes rc)))
                ids (vec (reverse (get-in rc [:nodes i :resolved-binding-ids])))
                altered (-> rc (assoc-in [:nodes i :resolved-binding-ids] ids)
                            (assoc-in [:function-records 0 :parameter-binding-ids] ids))]
            (is (false? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-joins?
                                                    [q rows altered])))))
        (when (= name "sequential-loop-bindings")
          (let [other (second (:loop-bindings rc))
                altered (update-in rc [:loop-bindings 0] merge
                          (select-keys other [:ordinal :initializer-form-id :initializer-syntax-id :initializer-node-id]))]
            (is (false? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-joins?
                                                    [q rows altered])))))
        (when (= family "sh-07-b9")
          (let [i (first (keep-indexed #(when (= :try (:core-form %2)) %1) (:nodes rc)))
                [protected left right] (get-in rc [:nodes i :children])
                altered (-> rc (assoc-in [:nodes i :children] [protected right left])
                            (assoc-in [:error-handlers 0 :handler-core-node-id] right)
                            (assoc-in [:error-handlers 1 :handler-core-node-id] left))]
            (is (false? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-joins?
                                                    [q rows altered])))))
        (when (= family "sh-07-b11")
          (let [other (second (:match-branch-records rc))
                altered (update-in rc [:match-branch-records 0] merge
                          (select-keys other [:branch-form-id :branch-syntax-id]))]
            (is (false? (bootstrap/sh07-core-execute! path 'sh07-source-core-map-semantic-joins?
                                                    [q rows altered])))))
        (swap! mapping-evidence assoc-in [:role-fixtures name]
               {:forms (count (:forms q)) :nodes (count (:nodes rc))
                :dispositions (count rows) :digests (count d) :map-id (last z)})))
    (let [macro-rows (get-in (:d (mapping-values)) [(dec (count (:d (mapping-values)))) :preimage :records])]
      (swap! roles into (map :reason macro-rows)))
    (is (= #{:core-node-source :core-operator :definition-name :parameter-vector :parameter
             :binding-vector :binding-name :quoted-data :var-target :mutation-target
             :catch-clause :catch-operator :catch-type :catch-binding :match-pattern} @roles))
    (swap! mapping-evidence assoc :covered-reasons @roles)))

(deftest c6-map-direct-roots-and-executable-child-source-joins-are-exact
  (let [{:keys [q rc d z]} (mapping-values)
        base (:value (bootstrap/sh07-core-execute! mapping-fixture 'sh07-resolve-source-core-map-preimage
                                                 [(:preimage (last d)) (pop z)]))
        rows (:records base)
        index (bootstrap/sh07-core-execute! mapping-fixture 'sh07-source-core-map-row-index [rows])
        invoke #(bootstrap/sh07-core-execute! mapping-fixture %1 %2)
        def-index (first (keep-indexed #(when (= :def (:core-form %2)) %1) (:nodes rc)))
        foreign-child (:node-id (last (:nodes rc)))]
    (is (true? (invoke 'sh07-source-core-map-roots-join? [q rc index])))
    (is (false? (invoke 'sh07-source-core-map-roots-join? [q (assoc rc :root-core-node-ids []) index])))
    (is (false? (invoke 'sh07-source-core-map-roots-join?
                       [q (assoc-in rc [:fragment-manifest 0 :root-node-ids] [foreign-child]) index])))
    (is (false? (invoke 'sh07-source-core-map-semantic-joins?
                       [q rows (assoc-in rc [:nodes def-index :children] [foreign-child])])))
    (is (= "SH07-B51-00945-GOVERNED-READ" (:id (mapping-rejection q rc [] []))))
    (is (= :source-core-map-digest-recomputation
           (get-in (mapping-rejection q rc d (assoc z (dec (count z)) (digest \0))) [:details :reason])))))

(deftest c6-map-full-fresh-record-correspondence-and-duplicate-keys-are-exact
  (let [{:keys [q rc d z]} (mapping-values)
        invoke #(bootstrap/sh07-core-execute! mapping-fixture %1 %2)
        base (:value (invoke 'sh07-resolve-source-core-map-preimage [(:preimage (last d)) (pop z)]))
        rows (:records base)
        duplicate (update rc :definitions #(conj % (first %)))
        index (invoke 'sh07-source-core-map-record-index [(:definitions duplicate) :core-node-id nil])
        empty-key (assoc rc :var-references nil)]
    (is (true? (invoke 'sh07-source-core-map-semantic-record-joins? [rc rc])))
    (is (> (count (:nodes rc)) 1))
    (is (false? (invoke 'sh07-source-core-map-semantic-record-joins?
                        [(update rc :nodes #(vec (reverse %))) rc])))
    (is (contains? rc :evaluation-order))
    (is (false? (invoke 'sh07-source-core-map-semantic-record-joins?
                        [(assoc rc :evaluation-order [:foreign-evaluation-record]) rc])))
    (is (false? (invoke 'sh07-source-core-map-semantic-record-joins?
                        [(assoc-in rc [:function-records 0 :definition-kind] :foreign) rc])))
    (is (= {:source-core-map-index-error :duplicate-record-key} index))
    (is (false? (invoke 'sh07-source-core-map-semantic-record-joins? [duplicate duplicate])))
    (is (false? (invoke 'sh07-source-core-map-semantic-joins? [q rows duplicate])))
    (is (false? (invoke 'sh07-source-core-map-semantic-record-joins? [empty-key rc])))))

(deftest c6-map-mixed-emission-owner-filtering-keeps-order-and-rejects-empty-or-duplicate-anchors
  ;; Synthetic grouping exercises the relation helper with genuine node IDs.
  ;; It does not claim that this grouping is an authenticated producer output.
  (let [{:keys [q rc d z]} (mapping-values)
        invoke #(bootstrap/sh07-core-execute! mapping-fixture %1 %2)
        base (:value (invoke 'sh07-resolve-source-core-map-preimage [(:preimage (last d)) (pop z)]))
        rows (:records base)
        row (first (filter #(= :definition-name (:reason %)) rows))
        owner-id (:owner-form-id row)
        def-id (first (:anchor-node-ids row))
        fn-id (:node-id (first (filter #(= :fn (:core-form %)) (:nodes rc))))
        index (invoke 'sh07-source-core-map-row-index [rows])
        nodes (invoke 'sh07-source-core-map-node-index [(:nodes rc)])
        check (invoke 'sh07-input-check [q])
        indexed (assoc q :internal-indexes (:indexes check))
        mixed {owner-id [fn-id def-id]}]
    (is (= [def-id] (invoke 'sh07-source-core-map-owner-anchors [indexed row [fn-id def-id] nodes])))
    (is (true? (invoke 'sh07-source-core-map-rows-join? [q [row] mixed index nodes])))
    (is (false? (invoke 'sh07-source-core-map-rows-join? [q [row] {owner-id [fn-id]} index nodes])))
    (is (false? (invoke 'sh07-source-core-map-rows-join?
                        [q [(assoc row :anchor-node-ids [def-id def-id])]
                         {owner-id [fn-id def-id def-id]} index nodes])))))
