(ns gravity.self-hosting.sh09-body-effect-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- path [relative] (kernel-support/path relative))
(def module-root-entry kernel-support/module-root-entry)
(def module-root-plan kernel-support/module-root-plan)
(def compile-c8-module-bundle kernel-support/compile-c8-module-bundle)
(defn- leaf [function & args] (apply kernel-support/leaf function args))
(defn- sha [n] (str "sha256:" (format "%064x" n)))
(defn- sh08-helper [name]
  (or (case name
        coordinator-verification kernel-support/coordinator-verification
        b47-context kernel-support/b47-context
        invoke-c7 kernel-support/invoke-c7
        nil)
      (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Missing existing C7 helper" {:name name}))))
(defn- seal-binding [request typed verification]
  (kernel-support/seal-binding request typed verification))
(defn- fresh-type-products [relative]
  (kernel-support/fresh-type-products relative))

(defn- fresh-products [relative]
  (let [{:keys [request typed verification] :as products} (fresh-type-products relative)]
    (assoc products :binding (seal-binding request typed verification))))

(defn- fresh-request [c5]
  (let [artifact (bootstrap/sh07-core-from-resolution-artifact c5)
        report (bootstrap/sh07-core-artifact-verification artifact)
        coordinator ((sh08-helper 'coordinator-verification) artifact report)
        context ((sh08-helper 'b47-context) artifact report coordinator)]
    {:canonical-core-artifact (:canonical-core-artifact context)
     :b47-context context :coordinator-verification coordinator}))

(defn- fresh-import-products [extension]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [prefix "bootstrap/clojure/fixtures/self-hosting/sh-08/modules/"
        producer-c5 (bootstrap/compiler-c5-resolution-file-artifact (path (str prefix "inferred-values" extension)))
        producer-request (fresh-request producer-c5)
        c5 (bootstrap/compiler-c5-resolution-file-artifact (path (str prefix "inferred-consumer" extension))
             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [producer-c5]})
        request (assoc (fresh-request c5) :module-type-inputs
                  {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [producer-request]})
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :producer-c5 producer-c5 :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))

(defn- fresh-recursive-import-products [extension]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [producers (mapv #(bootstrap/compiler-c5-resolution-file-artifact
                         (path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/" % extension)))
                       ["function-self-recursive-type" "function-self-recursive-string-type"])
        requests (mapv fresh-request producers)
        c5 (bootstrap/compiler-c5-resolution-file-artifact
             (path (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/recursive-imported-body" extension))
             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts producers})
        request (assoc (fresh-request c5) :module-type-inputs
                  {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests requests})
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))

(defn- fresh-callable-import-products [extension]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [producer (bootstrap/compiler-c5-resolution-file-artifact
                   (path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-call-nonlocal" extension)))
        producer-request (fresh-request producer)
        c5 (bootstrap/compiler-c5-resolution-file-artifact
             (path (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/callable-imported-body" extension))
             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [producer]})
        request (assoc (fresh-request c5) :module-type-inputs
                  {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [producer-request]})
        invoke (sh08-helper 'invoke-c7)
        typed (invoke 'sh08-function-type-core-artifact [request])
        verification (invoke 'sh08-verify-function-type-result [request typed])]
    {:c5 c5 :request request :typed typed :verification verification
     :binding (seal-binding request typed verification)}))
(defn- build-leaf [products]
  (leaf 'build-body-effect-result (:typed products) (:verification products) (:binding products)))
(defn- coherent-typed-mutation [products mutate]
  (let [typed (mutate (:typed products))
        typed (assoc typed :typed-core (leaf 'body-caller-typed-core typed))
        identity (leaf 'body-caller-identity typed)
        typed (assoc typed :identity-input identity :artifact-id-request identity)
        verification (assoc (:verification products) :expected typed :candidate typed)]
    (assoc products :typed typed :verification verification
           :binding (seal-binding (:request products) typed verification))))
(defn- reseal-effect-graph [candidate graph]
  (kernel-support/reseal-effect-graph candidate graph))
(defn- node [n kind children]
  {:node-id (sha n) :core-form kind :children (mapv sha children) :attributes {}
   :source {:syntax-id (sha (+ n 100)) :form-id (sha (+ n 200))
            :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil}
   :evaluation {:kind (case kind :fn :function-creation :quote :no-evaluation :left-to-right)
                :order (if (#{:fn :quote} kind) []
                         (mapv (fn [index n] {:index index :core-node-id (sha n)}) (range) children))}})

(deftest fresh-type-products-construction-development-kernel
  ;; Mocked control-flow DATA checks work preservation, not authentication.
  (let [relative "normalized-type-products.gravity" events (atom []) seals (atom 0)
        c5 {:normalized :c5} artifact {:normalized :c6} report {:normalized :c6-report}
        coordinator {:normalized :coordinator} core {:normalized :core}
        context {:canonical-core-artifact core} typed {:normalized :typed}
        verification {:normalized :verification}
        request {:canonical-core-artifact core :b47-context context :coordinator-verification coordinator}
        expected {:c5 c5 :artifact artifact :report report :request request :typed typed :verification verification}
        cycle [[:c5 (path relative)] [:c6 c5] [:c6-verify artifact] [:coordinator artifact report]
               [:context artifact report coordinator]
               [:c7 'sh08-function-type-core-artifact [request]]
               [:c7 'sh08-verify-function-type-result [request typed]]]]
    (with-redefs [bootstrap/compiler-c5-resolution-file-artifact (fn [source] (swap! events conj [:c5 source]) c5)
                  bootstrap/sh07-core-from-resolution-artifact (fn [input] (swap! events conj [:c6 input]) artifact)
                  bootstrap/sh07-core-artifact-verification (fn [input] (swap! events conj [:c6-verify input]) report)
                  kernel-support/coordinator-verification
                  (fn [input proof] (swap! events conj [:coordinator input proof]) coordinator)
                  kernel-support/b47-context
                  (fn [input proof binding] (swap! events conj [:context input proof binding]) context)
                  kernel-support/invoke-c7
                  (fn [function args]
                    (swap! events conj [:c7 function args])
                    (case function sh08-function-type-core-artifact typed
                          sh08-verify-function-type-result verification))
                  seal-binding (fn [& _] (swap! seals inc) (throw (ex-info "Unexpected type-only seal" {})))]
      (is (= expected (fresh-type-products relative)))
      (is (= cycle @events))
      (is (= expected (fresh-type-products relative)))
      (is (= (vec (concat cycle cycle)) @events))
      (is (= 0 @seals))
      (reset! events [])
      (let [failure (ex-info "C6 verification failed" {:normalized :failure})
            observed (with-redefs [bootstrap/sh07-core-artifact-verification
                                   (fn [input] (swap! events conj [:c6-verify input]) (throw failure))]
                       (try (fresh-type-products relative) (catch Throwable error error)))]
        (is (identical? failure observed))
        (is (= (subvec cycle 0 3) @events))
        (is (= 0 @seals))))))

(deftest fresh-products-seal-consumer-development-kernel
  ;; The existing C8 helper still receives the original triplet and exact seal.
  (let [relative "normalized-sealed-products.gravity" builds (atom []) seals (atom []) consumers (atom [])
        request {:normalized :request} typed {:normalized :typed} verification {:normalized :verification}
        products {:c5 {:normalized :c5} :artifact {:normalized :c6} :report {:normalized :c6-report}
                  :request request :typed typed :verification verification}
        original-seal seal-binding
        preimage {:domain :gravity/sh09-sh08-function-binding-v1 :request request :typed typed :verification verification}]
    (with-redefs [fresh-type-products (fn [source] (swap! builds conj source) products)
                  seal-binding (fn [r t v] (swap! seals conj [r t v]) (original-seal r t v))
                  leaf (fn [function & args] (swap! consumers conj [function (vec args)]) :observed)]
      (doseq [_ (range 2)]
        (let [sealed (fresh-products relative) binding (:binding sealed)]
          (is (= products (dissoc sealed :binding)))
          (is (= preimage (get-in binding [:digest-resolution :preimage])))
          (is (identical? request (get-in binding [:digest-resolution :preimage :request])))
          (is (identical? typed (get-in binding [:digest-resolution :preimage :typed])))
          (is (identical? verification (get-in binding [:digest-resolution :preimage :verification])))
          (is (= :passed (:status binding) (get-in binding [:digest-resolution :status])))
          (is (= :observed (build-leaf sealed)))
          (is (= ['build-body-effect-result [typed verification binding]] (last @consumers)))))
      (is (= [relative relative] @builds))
      (is (= [[request typed verification] [request typed verification]] @seals))
      (is (= 2 (count @consumers))))))

(deftest body-integral-ordering-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'integral-ordering-input)
        c7 (ns-resolve upstream 'invoke-c7)
        base (input-fn '< [:gravity.type/i64 :gravity.type/i64])
        id (zipmap [:operator :arg0 :arg1 :call] (map sha (range 900 904)))
        convert-node (fn [node] (-> node (update :node-id id) (update :children #(mapv id %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id id)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id id) (update :operator-node-id id)
                 (update :argument-node-ids #(mapv id %)) (update :ordered-evaluation-node-ids #(mapv id %)))
        edge (-> (:edge base) (update :call-core-node-id id) (update :argument-core-node-ids #(mapv id %))
                 (update :ordered-evaluation-node-ids #(mapv id %)))
        input (assoc base :core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
                     :node (last node-list) :nodes (into {} (map (juxt :node-id identity) node-list)) :call call :edge edge
                     :types (into {} (map (fn [[k v]] [(id k) v]) (:types base))))
        {:keys [core node call edge nodes bindings types module]} input
        descriptor (c7 'sh08-io-descriptor [core node call edge nodes bindings types module])
        facts {:module module :type-table (assoc types (id :operator) descriptor (id :call) :gravity.type/bool)
               :function-type-table [] :call-type-facts [(c7 'sh08-io-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-io-constraint [descriptor])]}
        valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
        graph (leaf 'body-derive-nodes-with-foreign core facts {})]
    (is (true? (valid facts)))
    (is (= :accepted (:status graph)))
    (is (= #{} (get-in graph [:nodes (id :call) :direct])))
    (is (= #{} (get-in graph [:nodes (id :call) :transitive])))
    (is (nil? (get-in graph [:nodes (id :call) :call-body-dependency])))
    (is (= (mapv id [:operator :arg0 :arg1]) (get-in graph [:nodes (id :call) :dependencies])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
    (doseq [changed [(assoc descriptor :operation '> :intrinsic :gravity.math.core/integral-greater-than)
                     (assoc descriptor :semantic-owner 'gravity.compare)
                     (assoc descriptor :profile :hosted) (assoc descriptor :target :wasm)
                     (assoc descriptor :numeric-mode :wrapping) (assoc descriptor :safety :unsafe-island)
                     (assoc descriptor :safety-rule :target-default)
                     (assoc descriptor :parameter-types [:gravity.type/u64 :gravity.type/u64])
                     (assoc descriptor :result-type :gravity.type/i64)
                     (assoc-in descriptor [:source-binding :binding-class] :import)
                     (assoc-in descriptor [:source-binding :package :version] "forged")
                     (assoc-in descriptor [:source-call :argument-node-ids] (vec (reverse (:argument-node-ids call))))
                     (assoc-in descriptor [:source :syntax-id] :forged)
                     (assoc descriptor :extra true) (dissoc descriptor :invalidation)]]
      (let [candidate (-> facts (assoc-in [:type-table (id :operator)] changed)
                          (assoc :call-type-facts [(c7 'sh08-io-call-fact [changed module])]
                                 :constraint-ledger [(c7 'sh08-io-constraint [changed])]))]
        (is (false? (valid candidate)) (pr-str changed))))
    (doseq [candidate [(assoc-in facts [:type-table (id :operator) :numeric-mode] :wrapping)
                       (assoc-in facts [:type-table (id :operator) :safety-rule] :backend-default)
                       (assoc-in facts [:type-table (id :operator) :latent-effects] [:io/write])
                       (assoc-in facts [:type-table (id :operator) :extra] true)
                       (assoc-in facts [:call-type-facts 0 :result-type] :gravity.type/i64)
                       (assoc facts :call-type-facts [])
                       (assoc facts :constraint-ledger [])
                       (update facts :constraint-ledger conj (assoc (first (:constraint-ledger facts)) :lhs {:core-node-id :nonexistent :projection :integral-ordering}))
                       (update facts :constraint-ledger into (:constraint-ledger facts))]]
      (is (false? (valid candidate))))
    (doseq [key [:numeric-mode :safety-rule :latent-effects :extra]]
      (is (= :rejected (:status (leaf 'body-derive-nodes-with-foreign core
                                     (assoc-in facts [:type-table (id :operator) key] :forged) {})))))
    (let [graph (assoc (:nodes graph) (id :arg0) {:transitive #{:error/throw} :escaping-thrown-error-types [:gravity.type/string]})
          row (leaf 'body-tc-constraint core edge {(id :call) call} nodes {} bindings (:type-table facts) {} graph)]
      (is (= [:gravity.type/string] (:rhs row)))
      (is (= [] (get-in row [:dependencies :callee-error-types])))
      (is (nil? (get-in row [:dependencies :callee-source-body-id])))
      (is (= descriptor (get-in row [:dependencies :callee-normal-type]))))))

(deftest body-map-association-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-association-input) derive (ns-resolve upstream 'map-association-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [never? [false true]]
      (let [base (input-fn :new (if never? :gravity.type/never :gravity.type/bool) false)
            ids (zipmap (keys (:nodes base)) (map sha (range 950 970)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :association) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ma-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ma-constraint [descriptor])]}
            source-context (leaf 'body-ma-context request facts (:bindings base))
            facts (assoc facts :association-context (:context source-context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :association)])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :accepted (:status source-context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= (if never? #{} #{:memory/allocate}) (:direct record)))
        (is (= #{:memory/allocate} (:transitive record)))
        (is (= (not never?) (get-in record [:allocation :normal-completion-possible?])))
        (is (= (mapv ids [:operator :input :key :value]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))

        (doseq [forms [nil 7 {}]]
          (is (= :body-map-association-source-forms-required
                 (:reason (leaf 'body-ma-context (assoc-in request [:b47-context :authenticated-core-request :forms] forms)
                                facts (:bindings base))))))
        (is (= :incomplete-canonical-core-coverage
               (:reason (leaf 'body-analyze-module (assoc-in request [:canonical-core-artifact :calls] 7) facts {}))))
        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core) (assoc descriptor :application (if never? :normal :not-invoked))
                         (assoc descriptor :application (if never? :normal :not-invoked) :direct-effects (if never? [:memory/allocate] []))
                         (assoc descriptor :direct-effects [:io/write]) (assoc descriptor :latent-effects [])
                         (assoc descriptor :capabilities [:memory/write]) (assoc descriptor :thrown-error-effects [:error/throw])
                         (assoc descriptor :extra true) (dissoc descriptor :invalidation)
                         (assoc-in descriptor [:source :syntax-id] :sibling)
                         (assoc-in descriptor [:source-binding :package :version] "forged")
                         (assoc-in descriptor [:argument-types 2] :gravity.type/string)]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ma-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ma-constraint [changed])]))]
            (is (false? (valid candidate)) (pr-str changed))))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger into (:constraint-ledger facts))
                           (assoc facts :call-type-facts []) (assoc-in facts [:association-context :forms :key :value] :sibling)]]
          (is (false? (valid candidate))))
        (doseq [changed [(dissoc record :allocation) (assoc record :direct #{} :transitive #{} :dependencies [])
                         (assoc-in record [:allocation :normal-completion-possible?] never?)
                         (assoc-in record [:allocation :provider] :observed-host-allocation)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc (:nodes graph) (ids :association) changed)))))
        (let [operand-graph (assoc (:nodes graph) (ids :value) {:type :gravity.type/never :transitive #{:error/throw}
                                                                 :escaping-thrown-errors #{:gravity.type/string}
                                                                 :escaping-thrown-error-types [:gravity.type/string]})
              stopped (leaf 'body-node-record (:node input) nil operand-graph :gravity.type/never)]
          (is (= #{} (:direct stopped)))
          (is (= #{:memory/allocate :error/throw} (:transitive stopped)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types stopped))))))))

(deftest body-map-removal-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-removal-input) derive (ns-resolve upstream 'map-removal-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[key empty?] [[:old false] [:missing false] [:missing true]]]
      (let [base (input-fn key empty?)
            ids (zipmap (keys (:nodes base)) (map sha (range 980 1000)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :removal) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-mr-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-mr-constraint [descriptor])]}
            source-context (leaf 'body-mr-context request facts (:bindings base))
            facts (assoc facts :removal-context (:context source-context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :removal)])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :accepted (:status source-context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= #{:memory/allocate} (:direct record) (:transitive record)))
        (is (= true (get-in record [:allocation :normal-completion-possible?])))
        (is (= :static-persistent-map-removal (get-in record [:allocation :behavior])))
        (is (= (:allocation-policy descriptor) (get-in record [:allocation :allocation-policy])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= (mapv ids [:operator :input :key]) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= source-context (leaf 'body-mr-context request facts (:bindings base))))

        (doseq [forms [nil 7 {}]]
          (is (= :body-map-removal-source-forms-required
                 (:reason (leaf 'body-mr-context (assoc-in request [:b47-context :authenticated-core-request :forms] forms)
                                facts (:bindings base))))))
        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core) (assoc descriptor :application :not-invoked :direct-effects [])
                         (assoc descriptor :direct-effects []) (assoc descriptor :latent-effects [])
                         (assoc descriptor :capabilities [:memory/write]) (assoc descriptor :extra true)
                         (dissoc descriptor :invalidation) (assoc-in descriptor [:source :syntax-id] :sibling)
                         (assoc-in descriptor [:source-binding :package :version] "forged")
                         (assoc-in descriptor [:allocation-policy :absent-key] :none)
                         (assoc-in descriptor [:storage-policy :payloads] :evaluated)
                         (assoc-in descriptor [:input-shape :call-join-work] "forged")
                         (assoc-in descriptor [:input-shape :call-join-work] -1)
                         (assoc-in descriptor [:input-shape :call-join-work] nil)
                         (assoc-in descriptor [:transition :kind] :forged)
                         (assoc-in descriptor [:transition :survivors] [])
                         (assoc-in descriptor [:result-shape :constructor-origin] :persistent-map-association)]]
          ;; Forge the row and call fact together: independent reconstruction must still reject.
          (when-not (= descriptor changed)
            (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                                (assoc :call-type-facts [(c7 'sh08-mr-call-fact [changed (:module base)])]
                                       :constraint-ledger [(c7 'sh08-mr-constraint [changed])]))]
              (is (false? (valid candidate)) (pr-str changed)))))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger into (:constraint-ledger facts))
                           (assoc facts :call-type-facts []) (assoc-in facts [:removal-context :forms :key :value] :sibling)]]
          (is (false? (valid candidate))))
        (doseq [changed [(dissoc record :allocation) (assoc record :direct #{} :transitive #{} :dependencies [])
                         (assoc-in record [:allocation :normal-completion-possible?] false)
                         (assoc-in record [:allocation :allocation-policy :empty-map] :none)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc (:nodes graph) (ids :removal) changed)))))
        (let [never-descriptor (assoc descriptor :application :not-invoked :direct-effects [])
              operand-graph (-> (:nodes graph)
                                (assoc-in [(ids :operator) :type] never-descriptor)
                                (assoc (ids :key) {:type :gravity.type/never :transitive #{:error/throw}
                                                  :escaping-thrown-errors #{:gravity.type/string}
                                                  :escaping-thrown-error-types [:gravity.type/string]}))
              stopped (leaf 'body-node-record (:node input) nil operand-graph :gravity.type/never)]
          (is (= #{} (:direct stopped)))
          (is (= #{:memory/allocate :error/throw} (:transitive stopped)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types stopped))))))))

(deftest body-map-family-removal-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'map-family-removal-input) derive (sh08-helper 'map-removal-derived) c7 (sh08-helper 'invoke-c7)]
    (doseq [never-key? [false true] never-input? [false true]]
      (let [base (input-fn :removed never-key? never-input?)
            ids (zipmap (keys (:nodes base)) (map sha (range 1100 1120)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :removal) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-mr-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-mr-constraint [descriptor])]}
            context (leaf 'body-mr-context request facts (:bindings base))
            facts (assoc facts :removal-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :removal)]) normal? (not (or never-input? never-key?))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str [result context graph]))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= (:allocation-policy descriptor) (get-in record [:allocation :allocation-policy])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= context (leaf 'body-mr-context request facts (:bindings base))))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (when-not never-input?
          (doseq [changed [(dissoc descriptor :input-type-evidence)
                           (assoc-in descriptor [:input-type-evidence :node-id] (sha 9999))
                           (assoc-in descriptor [:input-type-evidence :source :syntax-id] :sibling)
                           (assoc-in descriptor [:input-type-evidence :kind] :constructor)
                           (assoc descriptor :input-shape {:status :accepted :shape-kind :map :parts []})
                           (assoc descriptor :transition {:kind :absent :parts []})
                           (assoc descriptor :result-shape {:status :accepted :shape-kind :map :parts []})
                           (assoc descriptor :direct-effects (if normal? [] [:memory/allocate]))
                           (assoc descriptor :call-join-work 1)]]
            (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                                (assoc :call-type-facts [(c7 'sh08-mr-call-fact [changed (:module base)])]
                                       :constraint-ledger [(c7 'sh08-mr-constraint [changed])]))]
              (is (false? (valid candidate))))))))))

(deftest body-map-family-count-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'list-query-input) c7 (sh08-helper 'invoke-c7)]
    (doseq [type [:gravity.type/map :gravity.type/never]]
      (let [{:keys [core node call edge nodes bindings types forms]} (input-fn 'count [type])
            result (c7 'sh08-cq-derive [core node call edge nodes bindings types forms])
            descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-cq-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-cq-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-cq-context request facts bindings)
            facts (assoc facts :count-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                     :arg0 {:type type :transitive #{}}} (:type result))]
        (is (= :accepted (:status result) (:status context)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= context (leaf 'body-cq-context request facts bindings)))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger into (:constraint-ledger facts))
                           (assoc-in facts [:type-table :operator :direct-effects] [:memory/allocate])
                           (assoc-in facts [:count-context :forms :arg0 :parent-form-id] :sibling)]]
          (is (false? (valid candidate))))
        (doseq [wrong [:gravity.type/vector :gravity.type/string :gravity.type/nil :gravity.type/unknown]]
          (is (nil? (leaf 'body-cq-source core node call edge nodes bindings (assoc types :arg0 wrong) (:module core)))))
        (let [set-input (input-fn 'count [:gravity.type/set])
              source (leaf 'body-cq-source (:core set-input) (:node set-input) (:call set-input)
                           (:edge set-input) (:nodes set-input) (:bindings set-input)
                           (:types set-input) (:module (:core set-input)))]
          (is (= [:gravity.type/set] (:argument-types source)))
          (is (= :established-persistent-set (get-in source [:query-semantics :normal-input]))))))))

(deftest body-map-association-catch-effects-development-kernel
  ;; Current C8 on normalized DATA: catching a source error does not discharge
  ;; the conservative effect label or an earlier constructor's allocation.
  (let [try-node (node 3 :try [1 2])
        graph {(sha 1) {:type :gravity.type/never :transitive #{:memory/allocate :error/throw}
                        :escaping-thrown-errors #{:gravity.type/string} :escaping-thrown-error-types [:gravity.type/string]}
               (sha 2) {:type :gravity.type/string :transitive #{} :escaping-thrown-errors #{} :escaping-thrown-error-types []}}
        facts {:atomic-typed-handlers {(sha 3) [{:error-type :gravity.type/string :handler-core-node-id (sha 2) :record {}}]}
               :type-table {(sha 1) :gravity.type/never (sha 2) :gravity.type/string (sha 3) :gravity.type/string}}
        caught (leaf 'body-control-node-record try-node nil graph :gravity.type/string facts {} {} nil)
        functions (leaf 'body-function-effects [{:function-id (sha 50) :body-core-node-id (sha 3)
                                                 :latent-effects [:memory/allocate :error/throw] :thrown-error-effects [] :capabilities []}]
                        (assoc graph (sha 3) caught))]
    (is (= #{:memory/allocate :error/throw} (:transitive caught)))
    (is (= [] (:escaping-thrown-error-types caught)))
    (is (= #{} (:escaping-thrown-errors caught)))
    (is (= [:gravity.type/string] (get-in caught [:covered-source-errors 0 :matched-error-types])))
    (is (= :pending-profile-handler-capability-authority (:effect-label-discharge caught)))
    (is (= #{:memory/allocate :error/throw} (get-in functions [(sha 50) :inferred])))
    (is (= [] (get-in functions [(sha 50) :throws])))))

(deftest body-bare-list-parameter-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'bare-list-parameter-input)
        constrain (ns-resolve upstream 'bare-list-parameter-constraints)
        actual {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/string] :shape :literal}
        {:keys [core call edge callee nodes bindings types] :as input} (input-fn actual)
        row (first (:constraints (constrain input)))
        facts {:constraint-ledger [row] :type-table types :function-type-table [callee]}
        request {:canonical-core-artifact core}
        context (leaf 'body-lp-context request facts bindings)
        facts (assoc facts :list-parameter-context (:context context))
        fact {:argument-types [actual]}
        valid #(leaf 'body-local-arguments-valid? core % bindings edge call fact callee)]
    (is (= :accepted (:status context)) (pr-str context))
    (is (= row (get-in context [:context :rows :call])))
    (is (true? (valid facts)))
    (is (= context (leaf 'body-lp-context request facts bindings)))
    (doseq [changed [(assoc row :kind :equality)
                     (update row :rhs #(mapv (fn [pair] (dissoc pair :compatibility)) %))
                     (assoc-in row [:rhs 0 :compatibility :actual-type] :gravity.type/list)
                     (assoc-in row [:rhs 0 :compatibility :argument-index] 1)
                     (assoc-in row [:rhs 0 :compatibility :declaration :type-symbol] 'Vector)
                     (assoc-in row [:rhs 0 :compatibility :preserves] [])
                     (assoc row :compatibility-rule-version :forged)]]
      (let [candidate (assoc facts :constraint-ledger [changed])
            checked (leaf 'body-lp-context request candidate bindings)
            candidate (assoc candidate :list-parameter-context (:context checked))]
        (is (or (= :rejected (:status checked)) (false? (valid candidate))))))
    (is (false? (valid (dissoc facts :list-parameter-context))))
    (is (false? (valid (assoc facts :constraint-ledger []))))
    (is (false? (leaf 'body-normal-compatible? actual :gravity.type/list)))
    (is (= :rejected (:status (leaf 'body-lp-context request facts (assoc-in bindings [:parameter :binding-class] :namespace)))))
    (is (= :rejected (:status (leaf 'body-lp-context request (assoc-in facts [:type-table :argument] :gravity.type/unknown) bindings))))
    (is (false? (leaf 'body-arguments-valid? [:argument] [actual] [{:binding-id :parameter :type :gravity.type/list}] types)))))

(deftest body-mixed-map-list-parameter-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'mixed-map-parameter-input) constrain (sh08-helper 'bare-list-parameter-constraints)]
    (doseq [order [[:record :forms :parts :suffix] [:parts :suffix :forms :record] [:forms :parts :record :suffix]]
            never-map? [false true]]
      (let [base (input-fn order) map-index (.indexOf order :record) map-id (get-in base [:call :argument-node-ids map-index])
            input (if never-map? (assoc-in base [:types map-id] :gravity.type/never) base)
            {:keys [core call edge callee nodes bindings types]} input
            row (first (:constraints (constrain input)))
            facts {:constraint-ledger [row] :type-table types :function-type-table [callee]}
            request {:canonical-core-artifact core}
            arguments (mapv types (:argument-node-ids call))
            context (leaf 'body-lp-context request facts bindings)
            valid (fn [candidate]
                    (let [derived (leaf 'body-lp-context request candidate bindings)]
                      (and (= :accepted (:status derived))
                           (true? (leaf 'body-local-arguments-valid? core (assoc candidate :list-parameter-context (:context derived))
                                        bindings edge call {:argument-types arguments} callee)))))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= row (get-in context [:context :rows :call])))
        (is (true? (valid facts)))
        (is (= (if never-map? :sh08-list-parameter-compatibility-v2 :sh08-map-list-parameter-compatibility-v1)
               (:compatibility-rule-version row)))
        (is (= context (leaf 'body-lp-context request facts bindings)))
        (doseq [changed [(assoc row :kind :equality)
                         (assoc row :compatibility-rule-version :sh08-bare-list-parameter-v1)
                         (update row :rhs #(mapv (fn [pair] (dissoc pair :compatibility)) %))
                         (update row :rhs #(vec (reverse %)))
                         (assoc-in row [:rhs (.indexOf order :forms) :compatibility :argument-index] 99)
                         (assoc-in row [:rhs (.indexOf order :parts) :compatibility :element-binding :kind] :var)]]
          (is (false? (valid (assoc facts :constraint-ledger [changed])))))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger conj row)
                           (update facts :constraint-ledger conj (assoc-in row [:constraint-id-request :call-core-node-id] :orphan))
                           (assoc-in facts [:type-table (get-in call [:argument-node-ids (.indexOf order :suffix)])] :gravity.type/bool)]]
          (is (false? (valid candidate))))
        (when-not never-map?
          (doseq [changed [(assoc-in row [:rhs map-index :compatibility :version] :sh08-bare-list-parameter-v1)
                           (assoc-in row [:rhs map-index :compatibility :declared-type] :gravity.type/list)
                           (assoc-in row [:rhs map-index :compatibility :parameter-binding-id] :suffix)
                           (assoc-in row [:rhs map-index :compatibility :declaration :extra] true)
                           (assoc-in row [:rhs map-index :compatibility :forgets] [:schema :ownership])]]
            (is (false? (valid (assoc facts :constraint-ledger [changed]))))))))))

(deftest body-homogeneous-list-parameter-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'homogeneous-list-parameter-input)
        constrain (sh08-helper 'bare-list-parameter-constraints)
        s :gravity.type/string positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        type {:kind :gravity.type/list :shape :homogeneous :element-type s}]
    (doseq [members [[] [s] [s s]]]
      (let [actual (positional members) {:keys [core call edge callee nodes bindings types] :as input} (input-fn actual)
            row (first (:constraints (constrain input)))
            facts {:constraint-ledger [row] :type-table types :function-type-table [callee]}
            request {:canonical-core-artifact core}
            context (leaf 'body-lp-context request facts bindings)
            checked-facts (assoc facts :list-parameter-context (:context context))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= row (get-in context [:context :rows :call])))
        (is (true? (leaf 'body-local-arguments-valid? core checked-facts bindings edge call {:argument-types [actual]} callee)))
        (is (= context (leaf 'body-lp-context request facts bindings)))
        (doseq [changed [(assoc row :kind :equality)
                         (update row :rhs #(mapv (fn [pair] (dissoc pair :compatibility)) %))
                         (assoc-in row [:rhs 0 :compatibility :version] :sh08-bare-list-parameter-v1)
                         (assoc-in row [:rhs 0 :compatibility :checked-element-type] :gravity.type/keyword)
                         (assoc-in row [:rhs 0 :compatibility :checked-element-count] (inc (count members)))
                         (assoc-in row [:rhs 0 :compatibility :element-binding :kind] :var)
                         (assoc-in row [:rhs 0 :compatibility :forgets] [:positional-element-types :static-length])]]
          (let [modified (assoc facts :constraint-ledger [changed])
                derived (leaf 'body-lp-context request modified bindings)]
            (is (or (= :rejected (:status derived))
                    (false? (leaf 'body-local-arguments-valid? core (assoc modified :list-parameter-context (:context derived))
                                  bindings edge call {:argument-types [actual]} callee))))))
        (doseq [bad-members [42 s '(x) #{s} {:member s} nil [:gravity.type/unknown] [s :gravity.type/keyword]]]
          (is (= :rejected (:status (leaf 'body-lp-compatibility core call edge callee 0 (positional bad-members) nodes bindings)))))
        (is (= :not-applicable (:status (leaf 'body-lp-compatibility core call edge callee 0 :gravity.type/list nodes bindings))))))
    (is (= {:status :accepted} (leaf 'body-lc-validate-types [type])))

    (is (= :accepted (:status (leaf 'body-lc-validate-types [{:kind :gravity.type/vector :shape :literal :element-types [type]}]))))
    (doseq [bad [(assoc type :element-type :gravity.type/nil) (assoc type :element-type type) (assoc type :ownership :owned)]]
      (is (= :rejected (:status (leaf 'body-lc-validate-types [bad])))))))

(deftest body-homogeneous-list-head-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'homogeneous-list-head-input) derive (sh08-helper 'list-query-derived) c7 (sh08-helper 'invoke-c7)]
    (doseq [branch-index [1 2]]
      (let [{:keys [core node call edge nodes bindings types forms] :as input} (input-fn branch-index)
            result (derive input) descriptor (:descriptor result) condition (get-in input [:predicate-derived :descriptor])
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :constraint-ledger (mapv #(c7 'sh08-lq-constraint [%]) [condition descriptor])
                   :call-type-facts (mapv #(c7 'sh08-lq-call-fact [% (:module core)]) [condition descriptor])}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-lq-context request facts bindings)
            facts (assoc facts :list-query-context (:context context))
            valid #(every? (fn [source-call]
                             (leaf 'body-lq-call-valid? core % bindings (get nodes (:core-node-id source-call)) source-call
                                   (first (filter (fn [entry] (= (:core-node-id source-call) (:call-core-node-id entry))) (:call-edges core))) nodes))
                           (:calls core))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= (:head-proof descriptor) (get-in context [:context :type-checks :call :head-proof])))
        (is (true? (valid facts)))
        (is (= context (leaf 'body-lq-context request facts bindings)))
        (doseq [changed [(dissoc descriptor :head-proof)
                         (assoc-in descriptor [:head-proof :knowledge] :forged)
                         (assoc-in descriptor [:head-proof :binding-id] :sibling)
                         (assoc-in descriptor [:head-proof :branch-child-index] (- 3 branch-index))
                         (assoc-in descriptor [:head-proof :result-type] :gravity.type/keyword)
                         (assoc-in descriptor [:head-proof :ancestry] [])]]
          (let [candidate (-> facts (assoc-in [:type-table :operator] changed)
                              (assoc :constraint-ledger (mapv #(c7 'sh08-lq-constraint [%]) [condition changed])
                                     :call-type-facts (mapv #(c7 'sh08-lq-call-fact [% (:module core)]) [condition changed])))]
            (is (false? (valid candidate)))))
        (doseq [form-id [:arg0 :predicate-receiver :operator :predicate-operator]]
          (is (false? (valid (assoc-in facts [:list-query-context :forms form-id :parent-form-id] :sibling)))))
        (let [changed (assoc-in facts [:type-table :condition] :gravity.type/never)]
          (is (= :rejected (:status (leaf 'body-lq-context request changed bindings)))))))))

(deftest body-list-query-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-query-input) derive (ns-resolve upstream 'list-query-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['first {:kind :gravity.type/list :element-types [] :shape :literal}]
                                   ['rest {:kind :gravity.type/list :element-types [] :shape :literal}]
                                   ['first {:kind :gravity.type/list :element-types [:gravity.type/bool :gravity.type/nil] :shape :literal}]
                                   ['rest {:kind :gravity.type/list :element-types [:gravity.type/bool :gravity.type/nil] :shape :literal}]
                                   ['rest :gravity.type/list] ['first :gravity.type/never] ['rest :gravity.type/never]
                                   ['empty? :gravity.type/list]
                                   ['empty? {:kind :gravity.type/list :element-types [] :shape :literal}]
                                   ['empty? :gravity.type/never]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lq-constraint [descriptor])]}
            context (leaf 'body-lq-context request facts (:bindings base))
            facts (assoc facts :list-query-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (= [] (:latent-effects descriptor)))
        (is (nil? (:allocation record)))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= #{} (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (nil? (:allocation derived))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-query-source-forms-required
               (:reason (leaf 'body-lq-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lq-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :rest] :copy)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lq-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= input-type :gravity.type/list)
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :accepted (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))


(deftest body-vector-empty-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-query-input) derive (ns-resolve upstream 'list-query-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['empty? :gravity.type/vector]
                                   ['empty? {:kind :gravity.type/vector :element-types [] :shape :literal}]
                                   ['empty? {:kind :gravity.type/vector :element-types [:gravity.type/bool :gravity.type/nil] :shape :literal}]
                                   ['empty? {:kind :gravity.type/vector :element-types [{:kind :gravity.type/vector :element-types [] :shape :literal}] :shape :literal}]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lq-constraint [descriptor])]}
            context (leaf 'body-lq-context request facts (:bindings base))
            facts (assoc facts :list-query-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (= [] (:latent-effects descriptor)))
        (is (nil? (:allocation record)))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= #{} (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (nil? (:allocation derived))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-query-source-forms-required
               (:reason (leaf 'body-lq-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lq-context request facts (:bindings base))))

        (doseq [changed [((ns-resolve upstream 'vector-empty-cross-family-descriptor) descriptor)
                         (assoc descriptor :producer-version :sh08-list-query-v1)
                         (assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :rest] :copy)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lq-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [mutate [#(assoc % :constraint-ledger [])
                        #(update % :constraint-ledger into (:constraint-ledger %))
                        #(assoc-in % [:constraint-ledger 0 :producer-rule] :sh08-list-query-v1)
                        #(assoc-in % [:constraint-ledger 0 :lhs :core-node-id] (sha 1299))
                        #(assoc % :call-type-facts [])
                        #(update % :call-type-facts into (:call-type-facts %))
                        #(assoc-in % [:call-type-facts 0 :dispatch] :direct-list-query)
                        #(assoc-in % [:call-type-facts 0 :call-core-node-id] (sha 1299))]]
          (let [candidate (mutate facts)]
            (is (= :rejected (:status (leaf 'body-lq-context request candidate (:bindings base)))))
            (is (false? (valid candidate)))))
        (doseq [field [:calls :call-edges]]
          (is (= :rejected (:status (leaf 'body-lq-context
                                     (update-in request [:canonical-core-artifact field] #(into % %))
                                     facts (:bindings base))))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "source hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "source equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "source format" {}))))]
          (doseq [[changed-request changed-facts changed-bindings]
                  [[request (assoc-in facts [:type-table (ids :arg0)] (array-map opaque [])) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in request [:canonical-core-artifact :module :profile] opaque) facts (:bindings base)]
                   [(assoc-in request [:b47-context :authenticated-core-request :forms 0 :form-id] opaque) facts (:bindings base)]
                   [request (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [request (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                           (conj (vec (repeat 2048 :meta)) opaque))]]]
            (is (= :rejected (:status (leaf 'body-lq-context changed-request changed-facts changed-bindings))))
            (is (empty? @callbacks))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "cold hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "cold equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "cold format" {}))))]
          (doseq [[changed-core changed-facts changed-bindings]
                  [[core facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in core [:module :profile] opaque) facts (:bindings base)]
                   [(assoc-in core [:nodes 0 :node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :call-core-node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :callee-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :ordinal] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :classification] opaque) facts (:bindings base)]
                   [(assoc-in core [:calls 0 :operator-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:module :target] opaque) facts (:bindings base)]
                   [core (assoc facts :function-type-table [{:function-id opaque}]) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :name] opaque)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :binding-class] opaque)]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque)))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] (conj (vec (repeat 40000 :meta)) opaque))) ]
                   [(assoc core :function-records [{:function-syntax-id opaque :function-core-node-id :owner}]) facts (:bindings base)]
                   [core (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                        (conj (vec (repeat 40000 :meta)) opaque))]]]
            (is (false? (leaf 'body-call-joins-with-foreign-valid? changed-core changed-facts changed-bindings {})))
            (is (empty? @callbacks))))
        (when (= input-type :gravity.type/vector)
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :accepted (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))


(deftest body-list-set-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (fn [_ types] ((ns-resolve upstream 'set-conversion-input) types))
        derive (fn [input] ((ns-resolve upstream 'set-conversion-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['set {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword}] ['set :gravity.type/never]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-st-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-st-constraint [descriptor])]}
            context (leaf 'body-st-context request facts (:bindings base))
            facts (assoc facts :set-conversion-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= [(if normal? :sh09-list-set-conversion-allocation-v1 :sh09-set-conversion-allocation-v1) normal? :set] ((juxt :rule-version :normal-completion-possible? :collection-kind) (:allocation record))))
        (is (= (if normal? :possible-persistent-set-from-keyword-list-construction :persistent-set-construction)
               (get-in record [:allocation :behavior])))
        (is (nil? (get-in record [:allocation :extent])))
        (is (= :body-list-set-conversion-source-mismatch
               (:reason (leaf 'body-st-context request
                          (assoc-in facts [:type-table (ids :arg0)] {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword :extra true})
                          (:bindings base)))))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= (get-in descriptor [:allocation-policy :empty-result]) (if normal? :possible nil)))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= (if normal? #{:memory/allocate} #{}) (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (= (if normal? :sh09-list-set-conversion-allocation-v1 :sh09-set-conversion-allocation-v1) (get-in derived [:allocation :rule-version]))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-set-conversion-source-forms-required
               (:reason (leaf 'body-st-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-st-context request facts (:bindings base))))

        (doseq [changed (cond-> [((ns-resolve upstream 'list-set-cross-family-descriptor) descriptor)
                         (assoc descriptor :version :sh08-list-query-v1)
                         (assoc-in descriptor [:construction-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :copy] :none)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]
                         (not normal?) (conj ((ns-resolve upstream 'list-set-stale-string-never-descriptor) descriptor)))]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-st-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-st-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [mutate [#(assoc % :constraint-ledger [])
                        #(update % :constraint-ledger into (:constraint-ledger %))
                        #(assoc-in % [:constraint-ledger 0 :producer-rule] :sh08-list-query-v1)
                        #(assoc-in % [:constraint-ledger 0 :lhs :core-node-id] (sha 1299))
                        #(assoc % :call-type-facts [])
                        #(update % :call-type-facts into (:call-type-facts %))
                        #(assoc-in % [:call-type-facts 0 :dispatch] :direct-list-query)
                        #(assoc-in % [:call-type-facts 0 :call-core-node-id] (sha 1299))]]
          (let [candidate (mutate facts)]
            ;; The shared Never context carries type facts, not ledger/fact admission.
            (is (= (if normal? :rejected :accepted) (:status (leaf 'body-st-context request candidate (:bindings base)))))
            (is (false? (valid candidate)))))
        (doseq [field [:calls :call-edges]]
          (is (= :rejected (:status (leaf 'body-st-context
                                     (update-in request [:canonical-core-artifact field] #(into % %))
                                     facts (:bindings base))))))
        (when normal? (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "source hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "source equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "source format" {}))))]
          (doseq [[changed-request changed-facts changed-bindings]
                  [[request (assoc-in facts [:type-table (ids :arg0)] (array-map opaque [])) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in request [:canonical-core-artifact :module :profile] opaque) facts (:bindings base)]
                   [(assoc-in request [:b47-context :authenticated-core-request :forms 0 :form-id] opaque) facts (:bindings base)]
                   [request (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [request (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                           (conj (vec (repeat 2048 :meta)) opaque))]]]
            (is (= :rejected (:status (leaf 'body-st-context changed-request changed-facts changed-bindings))))
            (is (empty? @callbacks)))))
        (when normal? (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "cold hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "cold equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "cold format" {}))))]
          (doseq [[changed-core changed-facts changed-bindings]
                  [[core facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in core [:module :profile] opaque) facts (:bindings base)]
                   [(assoc-in core [:nodes 0 :node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :call-core-node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :callee-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :ordinal] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :classification] opaque) facts (:bindings base)]
                   [(assoc-in core [:calls 0 :operator-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:module :target] opaque) facts (:bindings base)]
                   [core (assoc facts :function-type-table [{:function-id opaque}]) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :name] opaque)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :binding-class] opaque)]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque)))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] (conj (vec (repeat 40000 :meta)) opaque))) ]
                   [(assoc core :function-records [{:function-syntax-id opaque :function-core-node-id :owner}]) facts (:bindings base)]
                   [core (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                        (conj (vec (repeat 40000 :meta)) opaque))]]]
            (is (false? (leaf 'body-call-joins-with-foreign-valid? changed-core changed-facts changed-bindings {})))
            (is (empty? @callbacks)))))
        (when normal?
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :rejected (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-private-map-keys-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        {:keys [core node call edge nodes bindings forms types facts type-result descriptor join-result]}
        ((ns-resolve upstream 'private-map-keys-typed-input))
        c7 (ns-resolve upstream 'invoke-c7)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        parameter-context (leaf 'body-lp-context request facts bindings)
        facts (assoc facts :list-parameter-context (:context parameter-context))
        prepare #(leaf 'body-lq-context request % bindings)
        context (prepare facts) facts (assoc facts :list-query-context (:context context))
        valid #(leaf 'body-lq-call-valid? core % bindings node call edge nodes)
        coherent (fn [changed]
                   (-> facts (assoc-in [:type-table :operator] changed)
                       (assoc-in [:constraint-ledger 0] (c7 'sh08-mkeys-constraint [changed]))
                       (assoc-in [:call-type-facts 0] (c7 'sh08-mkeys-call-fact [changed (:module core)]))))
        graph (into {} (map (fn [[id type]] [id {:type type :direct #{} :transitive #{}}]) types))
        record (leaf 'body-node-record node nil graph (:type type-result))]

    (is (= [] (:diagnostics join-result)))
    (is (= :accepted (:status parameter-context)) (pr-str parameter-context))
    (is (true? (valid facts)))
    (is (true? (leaf 'body-call-joins-with-foreign-valid? core facts bindings {})))


    (is (= :gravity.type/map (get-in facts [:type-table :input])))
    (is (= #{:memory/allocate} (:direct record) (:transitive record)))
    (is (= :sh09-private-map-keys-allocation-v1 (get-in record [:allocation :rule-version])))
    (is (nil? (get-in record [:allocation :extent])))
    (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
    (is (= (:key-domain-proof descriptor) (get-in record [:allocation :key-domain-proof])))
    (let [inherited (leaf 'body-node-record node nil (assoc-in graph [:input :transitive] #{:error/throw}) (:type type-result))]
      (is (= #{:memory/allocate} (:direct inherited)))
      (is (= #{:memory/allocate :error/throw} (:transitive inherited))))
    (is (true? (valid (assoc-in facts [:list-query-context :type-checks :present-get :status] :rejected))))
    (is (true? (valid (assoc-in facts [:list-query-context :type-checks] {}))))
    (doseq [changed [((ns-resolve upstream 'private-map-keys-cross-family-descriptor) descriptor)
                     (assoc descriptor :entry-proof-work 0)
                     (assoc-in descriptor [:key-domain-proof :normal-witness-count] 0)
                     (update-in descriptor [:key-domain-proof :entries] pop)
                     (assoc-in descriptor [:key-domain-proof :entries 0 :compatibility :actual-type] :gravity.type/map)
                     (assoc descriptor :result-count 0)
                     (assoc descriptor :latent-effects [] :direct-effects [])]]
      (is (false? (valid (coherent changed)))))
    (doseq [mutate [#(update % :call-type-facts (fn [items] (vec (concat (subvec items 0 1) (subvec items 2)))))
                    #(update % :call-type-facts conj (get-in % [:call-type-facts 1]))
                    #(assoc-in % [:call-type-facts 1 :argument-types] [:gravity.type/map])
                    #(assoc-in % [:call-type-facts 1 :syntax-id] :orphan)
                    #(assoc-in % [:constraint-ledger 1 :rhs 0 :chosen] (get-in descriptor [:key-domain-proof :entries 0 :argument-type]))
                    #(assoc-in % [:constraint-ledger 1 :rhs 0 :compatibility :forgets] [:all-value-obligations])
                    #(assoc-in % [:constraint-ledger 1 :constraint-id-request :call-core-node-id] :orphan)
                    #(update % :constraint-ledger (fn [items] (vec (concat (subvec items 0 1) (subvec items 2)))))]]
      (let [candidate (mutate facts)]
        (is (= :rejected (:status (prepare candidate))))
        (is (false? (valid candidate)))))
    (let [callbacks (atom [])
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "private keys hash" {})))
                   (equals [_] (swap! callbacks conj :equals) (throw (ex-info "private keys equals" {})))
                   (toString [] (swap! callbacks conj :format) (throw (ex-info "private keys format" {}))))]
      (doseq [[changed-request changed-facts changed-bindings]
              [[request (assoc-in facts [:type-table :input-0] opaque) bindings]
               [request (assoc-in facts [:type-table :input-0 :member-types] [:gravity.type/keyword opaque]) bindings]
               [request (assoc-in facts [:call-type-facts 1 :result-type] opaque) bindings]
               [request (assoc-in facts [:constraint-ledger 1 :rhs 0 :compatibility] opaque) bindings]
               [(assoc-in request [:canonical-core-artifact :module :namespace] opaque) facts bindings]
               [(assoc-in request [:canonical-core-artifact :module :effects] opaque) facts bindings]
               [request facts (assoc-in bindings [:ordering-binding :profile-set] [opaque])]
               [request facts (assoc-in bindings [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque))]]]
        (is (= :rejected (:status (leaf 'body-lq-context changed-request changed-facts changed-bindings))))
        (is (empty? @callbacks))))))

(deftest body-map-keys-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-query-input) derive (ns-resolve upstream 'list-query-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['keys {:kind :gravity.type/map :member-types [] :shape :literal}]
                                   ['keys {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/bool] :shape :literal}]
                                   ['keys {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/bool :gravity.type/keyword :gravity.type/nil] :shape :literal}]
                                   ['keys {:kind :gravity.type/map :member-types [:gravity.type/keyword {:kind :gravity.type/vector :element-types [] :shape :literal}] :shape :literal}] ['keys :gravity.type/never]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lq-constraint [descriptor])]}
            context (leaf 'body-lq-context request facts (:bindings base))
            facts (assoc facts :list-query-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= [:sh09-map-keys-allocation-v1 normal? :list] ((juxt :rule-version :normal-completion-possible? :collection-kind) (:allocation record))))
        (is (= :possible-persistent-map-key-list-construction
               (get-in record [:allocation :behavior]) (get-in descriptor [:allocation-policy :behavior])))
        (is (= :body-map-keys-source-mismatch
               (:reason (leaf 'body-lq-context request
                          (assoc-in facts [:type-table (ids :arg0)] {:kind :gravity.type/map :shape :literal :member-types [] :extra true})
                          (:bindings base)))))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= (get-in descriptor [:allocation-policy :empty-result]) :possible))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= (if normal? #{:memory/allocate} #{}) (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (= :sh09-map-keys-allocation-v1 (get-in derived [:allocation :rule-version]))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-query-source-forms-required
               (:reason (leaf 'body-lq-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lq-context request facts (:bindings base))))

        (doseq [changed [((ns-resolve upstream 'map-keys-cross-family-descriptor) descriptor)
                         (assoc descriptor :version :sh08-list-query-v1)
                         (assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :copy] :none)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lq-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [mutate [#(assoc % :constraint-ledger [])
                        #(update % :constraint-ledger into (:constraint-ledger %))
                        #(assoc-in % [:constraint-ledger 0 :producer-rule] :sh08-list-query-v1)
                        #(assoc-in % [:constraint-ledger 0 :lhs :core-node-id] (sha 1299))
                        #(assoc % :call-type-facts [])
                        #(update % :call-type-facts into (:call-type-facts %))
                        #(assoc-in % [:call-type-facts 0 :dispatch] :direct-list-query)
                        #(assoc-in % [:call-type-facts 0 :call-core-node-id] (sha 1299))]]
          (let [candidate (mutate facts)]
            (is (= :rejected (:status (leaf 'body-lq-context request candidate (:bindings base)))))
            (is (false? (valid candidate)))))
        (doseq [field [:calls :call-edges]]
          (is (= :rejected (:status (leaf 'body-lq-context
                                     (update-in request [:canonical-core-artifact field] #(into % %))
                                     facts (:bindings base))))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "source hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "source equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "source format" {}))))]
          (doseq [[changed-request changed-facts changed-bindings]
                  [[request (assoc-in facts [:type-table (ids :arg0)] (array-map opaque [])) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in request [:canonical-core-artifact :module :profile] opaque) facts (:bindings base)]
                   [(assoc-in request [:b47-context :authenticated-core-request :forms 0 :form-id] opaque) facts (:bindings base)]
                   [request (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [request (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                           (conj (vec (repeat 2048 :meta)) opaque))]]]
            (is (= :rejected (:status (leaf 'body-lq-context changed-request changed-facts changed-bindings))))
            (is (empty? @callbacks))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "cold hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "cold equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "cold format" {}))))]
          (doseq [[changed-core changed-facts changed-bindings]
                  [[core facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in core [:module :profile] opaque) facts (:bindings base)]
                   [(assoc-in core [:nodes 0 :node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :call-core-node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :callee-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :ordinal] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :classification] opaque) facts (:bindings base)]
                   [(assoc-in core [:calls 0 :operator-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:module :target] opaque) facts (:bindings base)]
                   [core (assoc facts :function-type-table [{:function-id opaque}]) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :name] opaque)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :binding-class] opaque)]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque)))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] (conj (vec (repeat 40000 :meta)) opaque))) ]
                   [(assoc core :function-records [{:function-syntax-id opaque :function-core-node-id :owner}]) facts (:bindings base)]
                   [core (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                        (conj (vec (repeat 40000 :meta)) opaque))]]]
            (is (false? (leaf 'body-call-joins-with-foreign-valid? changed-core changed-facts changed-bindings {})))
            (is (empty? @callbacks))))
        (when (= input-type {:kind :gravity.type/map :member-types [] :shape :literal})
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :rejected (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-vector-rest-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-query-input) derive (ns-resolve upstream 'list-query-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['rest {:kind :gravity.type/vector :element-types [] :shape :literal}]
                                   ['rest {:kind :gravity.type/vector :element-types [:gravity.type/bool] :shape :literal}]
                                   ['rest {:kind :gravity.type/vector :element-types [:gravity.type/bool :gravity.type/nil] :shape :literal}]
                                   ['rest {:kind :gravity.type/vector :element-types [{:kind :gravity.type/vector :element-types [] :shape :literal} :gravity.type/character] :shape :literal}]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lq-constraint [descriptor])]}
            context (leaf 'body-lq-context request facts (:bindings base))
            facts (assoc facts :list-query-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{:memory/allocate} (:direct record) (:transitive record)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= [:sh09-vector-rest-allocation-v1 true :list] ((juxt :rule-version :normal-completion-possible? :collection-kind) (:allocation record))))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= (get-in descriptor [:allocation-policy :empty-result]) :possible))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= #{:memory/allocate} (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (= :sh09-vector-rest-allocation-v1 (get-in derived [:allocation :rule-version]))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-query-source-forms-required
               (:reason (leaf 'body-lq-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lq-context request facts (:bindings base))))

        (doseq [changed [((ns-resolve upstream 'vector-rest-cross-family-descriptor) descriptor)
                         (assoc descriptor :version :sh08-list-query-v1)
                         (assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :copy] :none)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lq-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [mutate [#(assoc % :constraint-ledger [])
                        #(update % :constraint-ledger into (:constraint-ledger %))
                        #(assoc-in % [:constraint-ledger 0 :producer-rule] :sh08-list-query-v1)
                        #(assoc-in % [:constraint-ledger 0 :lhs :core-node-id] (sha 1299))
                        #(assoc % :call-type-facts [])
                        #(update % :call-type-facts into (:call-type-facts %))
                        #(assoc-in % [:call-type-facts 0 :dispatch] :direct-list-query)
                        #(assoc-in % [:call-type-facts 0 :call-core-node-id] (sha 1299))]]
          (let [candidate (mutate facts)]
            (is (= :rejected (:status (leaf 'body-lq-context request candidate (:bindings base)))))
            (is (false? (valid candidate)))))
        (doseq [field [:calls :call-edges]]
          (is (= :rejected (:status (leaf 'body-lq-context
                                     (update-in request [:canonical-core-artifact field] #(into % %))
                                     facts (:bindings base))))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "source hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "source equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "source format" {}))))]
          (doseq [[changed-request changed-facts changed-bindings]
                  [[request (assoc-in facts [:type-table (ids :arg0)] (array-map opaque [])) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in request [:canonical-core-artifact :module :profile] opaque) facts (:bindings base)]
                   [(assoc-in request [:b47-context :authenticated-core-request :forms 0 :form-id] opaque) facts (:bindings base)]
                   [request (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [request (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                           (conj (vec (repeat 2048 :meta)) opaque))]]]
            (is (= :rejected (:status (leaf 'body-lq-context changed-request changed-facts changed-bindings))))
            (is (empty? @callbacks))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "cold hash" {})))
                       (equals [other] (swap! callbacks conj :equals) (throw (ex-info "cold equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "cold format" {}))))]
          (doseq [[changed-core changed-facts changed-bindings]
                  [[core facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in core [:module :profile] opaque) facts (:bindings base)]
                   [(assoc-in core [:nodes 0 :node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :call-core-node-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :callee-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :ordinal] opaque) facts (:bindings base)]
                   [(assoc-in core [:call-edges 0 :classification] opaque) facts (:bindings base)]
                   [(assoc-in core [:calls 0 :operator-binding-id] opaque) facts (:bindings base)]
                   [(assoc-in core [:module :target] opaque) facts (:bindings base)]
                   [core (assoc facts :function-type-table [{:function-id opaque}]) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :name] opaque)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :binding-class] opaque)]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] [opaque]))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque)))]
                   [core facts (-> (:bindings base) (assoc-in [:ordering-binding :binding-class] :bogus)
                                   (assoc-in [:ordering-binding :target-set] (conj (vec (repeat 40000 :meta)) opaque))) ]
                   [(assoc core :function-records [{:function-syntax-id opaque :function-core-node-id :owner}]) facts (:bindings base)]
                   [core (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque) (:bindings base)]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :profile-set]
                                        (conj (vec (repeat 40000 :meta)) opaque))]]]
            (is (false? (leaf 'body-call-joins-with-foreign-valid? changed-core changed-facts changed-bindings {})))
            (is (empty? @callbacks))))
        (when (= input-type {:kind :gravity.type/vector :element-types [] :shape :literal})
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :rejected (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-list-construction-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-construction-input) derive (ns-resolve upstream 'list-construction-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[] [:gravity.type/string] [:gravity.type/nil :gravity.type/bool :gravity.type/string]
                       [:gravity.type/map {:kind :gravity.type/list :element-types [:gravity.type/string] :shape :literal}]
                       [:gravity.type/never :gravity.type/string] [:gravity.type/string :gravity.type/never]
                       [:gravity.type/never :gravity.type/never]]]
      (let [base (-> (input-fn arguments) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lc-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lc-constraint [descriptor])]}
            context (leaf 'body-lc-context request facts (:bindings base))
            facts (assoc facts :list-construction-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not-any? #{:gravity.type/never} arguments)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= {:unit :elements :count (count arguments)} (get-in record [:allocation :extent])))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-construction-source-forms-required
               (:reason (leaf 'body-lc-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:construction-policy :allocation] :none)
                         (assoc-in descriptor [:construction-policy :order] :reverse)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [])
                         (assoc descriptor :return :gravity.type/bool :result-type :gravity.type/bool)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lc-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lc-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (empty? arguments)
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :rejected (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-string-concat-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'string-concat-input) derive (ns-resolve upstream 'string-concat-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[:gravity.type/string :gravity.type/string] [:gravity.type/never :gravity.type/string]
                       [:gravity.type/string :gravity.type/never] [:gravity.type/never :gravity.type/never]]]
      (let [base (-> (input-fn arguments) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-sc-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-sc-constraint [descriptor])]}
            context (leaf 'body-sc-context request facts (:bindings base))
            facts (assoc facts :string-concat-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (= arguments [:gravity.type/string :gravity.type/string])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= {:unit :concatenation-operands :count 2} (get-in record [:allocation :extent])))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-sc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:concatenation-policy :allocation] :none)
                         (assoc-in descriptor [:concatenation-policy :coercion] :format)
                         (assoc-in descriptor [:concatenation-policy :normalization] :nfc)
                         (assoc descriptor :latent-effects [])
                         (assoc descriptor :return :gravity.type/bool :result-type :gravity.type/bool)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-sc-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-sc-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= arguments [:gravity.type/string :gravity.type/string])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :rejected (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-private-entry-get-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input ((ns-resolve upstream 'private-entry-get-input))
        derive (ns-resolve upstream 'map-present-get-derived) c7 (ns-resolve upstream 'invoke-c7)
        {:keys [core node nodes bindings forms types module call edge]} input
        result (derive input) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        facts {:type-table (assoc types :operator descriptor :present-get (:type result))
               :call-type-facts [(c7 'sh08-mg-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-mg-constraint [descriptor])]}
        context (leaf 'body-mg-context request facts bindings)
        facts (assoc facts :map-get-context (:context context))
        valid (fn [d] (leaf 'body-mg-present-call-valid? core
                            (assoc facts :type-table (assoc (:type-table facts) :operator d)
                                         :call-type-facts [(c7 'sh08-mg-call-fact [d module])]
                                         :constraint-ledger [(c7 'sh08-mg-constraint [d])])
                            bindings node call edge nodes))]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :accepted (:status context)))
    (is (true? (valid descriptor)))
    (let [flat (-> core (assoc :resolution-table (get-in core [:preserved-resolution :resolution-table]))
                         (update :preserved-resolution dissoc :resolution-table))]
      (is (= :rejected (:status (leaf 'body-mg-context (assoc request :canonical-core-artifact flat) facts bindings))))
      (is (false? (leaf 'body-mg-present-call-valid? flat facts bindings node call edge nodes))))
    (is (true? (leaf 'body-mg-present-call-valid?
                    (assoc core :recursion-components [{:function-syntax-ids [:caller-0]}])
                    facts bindings node call edge nodes)))
    (is (= context (leaf 'body-mg-context request facts bindings)))


    (doseq [changed [(update-in descriptor [:presence-proof :entries] pop)
                     (assoc-in descriptor [:presence-proof :entries] :invalid)
                     (assoc-in descriptor [:presence-proof :entries] (vec (repeat 10 (first (get-in descriptor [:presence-proof :entries])))))
                     (assoc-in descriptor [:presence-proof :entries 1 :argument-shape :parts 0 :key] :absent)
                     (assoc-in descriptor [:presence-proof :entries 1 :selected-part :key] :absent)
                     (assoc-in descriptor [:presence-proof :entries 1 :selected-coordinate] 99)
                     (assoc-in descriptor [:presence-proof :entries 1 :argument-source] {})
                     (assoc-in descriptor [:presence-proof :entry-census :exports] ['without-path])
                     (assoc descriptor :version :sh08-map-get-present-v1)]]
      (is (false? (valid changed))))))

(deftest body-map-present-get-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-present-get-input) derive (ns-resolve upstream 'map-present-get-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[key never-input? never-key?] [[:file false false] [:line false false] [:kind false false]
                                          [:enabled false false] [:payload false false]
                                          [:file true false] [:file false true] [:file true true]]]
      (let [base (input-fn key never-input? never-key?)
            ids (zipmap (keys (:nodes base)) (map sha (range 1500 1550)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :present-get) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-mg-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-mg-constraint [descriptor])]}
            context (leaf 'body-mg-context request facts (:bindings base))
            facts (assoc facts :map-get-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :present-get)])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :accepted (:status context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= #{} (:direct record)))
        (is (= (if never-input? #{} #{:memory/allocate}) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :input :key]) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mg-context request facts (:bindings base))))

        (doseq [changed (concat [(assoc descriptor :direct-effects [:memory/allocate])
                                 (assoc-in descriptor [:lookup-policy :default] :host-nil)
                                 (assoc-in descriptor [:lookup-policy :operand-evaluation] :key-then-map)
                                 (assoc descriptor :version :sh08-map-get-v1)]
                                (when-not (or never-input? never-key?)
                                  [(assoc-in descriptor [:presence-proof :key] :absent)
                                   (assoc-in descriptor [:presence-proof :selected-coordinate] 99)
                                   (assoc-in descriptor [:presence-proof :selected-part :key] :absent)
                                   (assoc-in descriptor [:presence-proof :selected-type] :gravity.type/symbol)
                                   (assoc-in descriptor [:presence-proof :operand-node-id] (sha 9997))
                                   (dissoc descriptor :presence-proof)]))]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-mg-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-mg-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger into (:constraint-ledger facts))
                           (assoc facts :call-type-facts []) (assoc-in facts [:map-get-context :forms :key :parent-form-id] :sibling)]]
          (is (false? (valid candidate))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                         (assoc-in (:nodes graph) [(ids :present-get) :direct] #{:memory/allocate}))))))))

(deftest body-map-get-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-get-input) derive (ns-resolve upstream 'map-get-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/string]} :gravity.type/string :gravity.type/string]
                       [{:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/bool]} :gravity.type/keyword :gravity.type/bool]
                       [:gravity.type/map :gravity.type/string :gravity.type/never]
                       [:gravity.type/map :gravity.type/never :gravity.type/string]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-mg-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-mg-constraint [descriptor])]}
            context (leaf 'body-mg-context request facts (:bindings base))
            facts (assoc facts :map-get-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1 :arg2]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mg-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:lookup-policy :key-equality] :identity)
                         (assoc-in descriptor [:lookup-policy :default] :lazy)
                         (assoc-in descriptor [:lookup-policy :normalization] :nfc)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-mg-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-mg-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= :gravity.type/string (last arguments))
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-map-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (ns-resolve upstream 'map-contains-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[:gravity.type/map :gravity.type/string] [:gravity.type/map :gravity.type/keyword]
                       [{:kind :gravity.type/map :member-types [] :shape :independent} :gravity.type/string]
                       [:gravity.type/never :gravity.type/string] [:gravity.type/map :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-mc-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-mc-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :key-equality] :identity)
                         (assoc-in descriptor [:membership-policy :presence] :truthy-value)
                         (assoc-in descriptor [:membership-policy :normalization] :nfc)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-mc-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-mc-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= arguments [:gravity.type/map :gravity.type/string])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-keyword-set-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/keyword]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/keyword]} :gravity.type/keyword]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]} :gravity.type/never]
                       [:gravity.type/never :gravity.type/keyword]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string)]]
          (is (false? (valid candidate))))
        (when (= arguments [{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/keyword])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-not-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'not-input) derive (ns-resolve upstream 'not-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[:gravity.type/string] [:gravity.type/character] [:gravity.type/nil] [:gravity.type/bool]
                       [:gravity.type/integer] [{:kind :gravity.type/vector :element-types [] :shape :literal}] [:gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-nt-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-nt-constraint [descriptor])]}
            context (leaf 'body-nt-context request facts (:bindings base))
            facts (assoc facts :not-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-nt-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:truthiness-policy :falsy-values] [nil false 0])
                         (assoc-in descriptor [:truthiness-policy :operand-evaluation] :twice)
                         (assoc-in descriptor [:truthiness-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-nt-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-nt-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= arguments [:gravity.type/string])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-integral-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        families [:gravity.type/integer :gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                  :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64]]
    (doseq [family families arguments [[family family] [family :gravity.type/never] [:gravity.type/never family]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1450 1460)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-ie-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ie-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ie-constraint [descriptor])]}
            context (leaf 'body-ae-context request facts (:bindings base))
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-ie-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-ie-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (nil? (get-in context [:context :vector-domains])))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-ae-context request facts (:bindings base))))

        (doseq [changed [(assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :version :sh08-integral-ordering-v1)
                         (assoc descriptor :numeric-mode :arbitrary-precision)
                         (assoc descriptor :safety :runtime-checked)
                         (assoc descriptor :safety-rule :host-default)
                         (assoc descriptor :integer-type :gravity.type/string)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc descriptor :layer0-owner 'host.math)
                         (assoc-in descriptor [:equality-policy :conversion] :widen)
                         (assoc-in descriptor [:equality-policy :comparison] :type-based-constant)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (assoc-in facts [:type-table (ids :arg0)] :gravity.type/unknown)
                         (assoc-in facts [:type-table (ids :arg0)] :gravity.type/string)
                         (assoc-in facts [:type-table (ids :arg0)] :gravity.type/never)
                         (assoc-in facts [:call-type-facts 0 :dispatch] :direct-atomic-equality)]]
          (when-not (= changed facts) (is (false? (valid changed)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest body-bare-keyword-anchor-set-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string]
    (doseq [arguments [[:gravity.type/set (finite [k k])] [(finite [k]) :gravity.type/set]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-kse-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-kse-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-kse-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :keyword-set-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-kse-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-kse-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (get-in validation [:descriptor :argument-types])))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed ((sh08-helper 'bare-keyword-anchor-forgeries) descriptor)]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]
          (is (= :rejected (:status (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :rejected (:status (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (let [callbacks (atom [])
              opaque (proxy [Object] []
                       (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "body hash" {})))
                       (equals [_] (swap! callbacks conj :equals) (throw (ex-info "body equals" {})))
                       (toString [] (swap! callbacks conj :format) (throw (ex-info "body format" {}))))]
          (doseq [[changed-request changed-facts changed-bindings]
                  [[request (assoc-in facts [:type-table (ids :arg0)] (array-map opaque [])) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in request [:canonical-core-artifact :module :profile] opaque) facts (:bindings base)]
                   [(assoc-in request [:b47-context :authenticated-core-request :forms 0 :form-id] opaque) facts (:bindings base)]
                   [request (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [request (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]
                   [request facts (assoc-in (:bindings base) [:ordering-binding :profile-set] (conj (vec (repeat 40000 :meta)) opaque))]]]
            (is (= :rejected (:status (leaf 'body-ae-context changed-request changed-facts changed-bindings))))
            (is (empty? @callbacks)))
          (doseq [[changed-core changed-facts changed-bindings]
                  [[core facts (assoc-in (:bindings base) [:ordering-binding :profile-set] [opaque])]
                   [core facts (assoc-in (:bindings base) [:ordering-binding :target-set] [opaque])]
                   [(assoc-in core [:module :profile] opaque) facts (:bindings base)]
                   [(assoc-in core [:nodes 0 :node-id] opaque) facts (:bindings base)]
                   [core (assoc-in facts [:constraint-ledger 0 :producer-rule] opaque) (:bindings base)]
                   [core (assoc-in facts [:call-type-facts 0 :dispatch] opaque) (:bindings base)]]]
            (is (false? (leaf 'body-call-joins-with-foreign-valid? changed-core changed-facts changed-bindings {})))
            (is (empty? @callbacks))))
        (is (true? (valid (assoc-in facts [:equality-context :keyword-set-domains] {}))))
        (is (true? (valid (assoc-in facts [:equality-context :keyword-set-domains (ids :call) :status] :rejected))))
        (doseq [mutate [#(assoc % :call-type-facts [])
                        #(update % :call-type-facts into (:call-type-facts %))
                        #(assoc-in % [:call-type-facts 0 :dispatch] :direct-keyword-set-equality)
                        #(assoc-in % [:constraint-ledger 0 :producer-rule] :sh08-keyword-set-equality-v1)]]
          (let [candidate (mutate facts)]
            (is (= :rejected (:status (prepare candidate))))
            (is (false? (valid candidate)))))
        (let [inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [(ids :arg0) :transitive] inherited)
              actual (leaf 'body-node-record node nil seeded :gravity.type/bool)]
          (is (= #{} (:direct actual)))
          (is (= inherited (:transitive actual))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest body-keyword-set-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string]
    (doseq [arguments [[(finite []) (finite [])] [(finite [k k]) (finite [k])]
                       [:gravity.type/never (finite [k])] [(finite [k]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-kse-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-kse-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-kse-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :keyword-set-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-kse-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-kse-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :keyword-set-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]
          (is (= :body-keyword-set-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest body-vector-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/vector :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string]
    (doseq [arguments [[(finite []) (finite [])] [(finite [k s]) (finite [s k])]
                       [(finite [(finite [(nullable s)])]) (finite [k])]
                       [:gravity.type/never (finite [k])] [(finite [s]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-ve-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ve-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ve-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :vector-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-ve-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-ve-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :vector-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]
          (is (= :body-vector-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest body-atomic-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-equality-input) derive (ns-resolve upstream 'atomic-equality-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[:gravity.type/string :gravity.type/string] [:gravity.type/character :gravity.type/character]
                       [:gravity.type/nil :gravity.type/bool] [:gravity.type/symbol :gravity.type/keyword]
                       [:gravity.type/never :gravity.type/string] [:gravity.type/string :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ae-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ae-constraint [descriptor])]}
            context (leaf 'body-ae-context request facts (:bindings base))
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-ae-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:equality-policy :same-family] :identity)
                         (assoc-in descriptor [:equality-policy :cross-family] :coerce)
                         (assoc-in descriptor [:equality-policy :normalization] :nfc)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ae-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ae-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (when (= arguments [:gravity.type/string :gravity.type/string])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(defn- prepend-effect-input [receiver value ordinal]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'list-prepend-input) c7 (sh08-helper 'invoke-c7)
        base (input-fn receiver value) ids (zipmap (keys (:nodes base)) (map sha (range (+ 1800 (* 10 ordinal)) (+ 1810 (* 10 ordinal)))))
        source #(-> % (update :form-id ids) (update :syntax-id ids))
        node-list (mapv (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %)) (update :source source)
                                       (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %)))) (get-in base [:core :nodes]))
        forms (into {} (for [[id form] (:forms base)]
                         [(ids id) (-> form (update :form-id ids) (update :syntax-id ids) (update :parent-form-id ids)
                                      (update :child-form-ids #(mapv ids %)))]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (assoc :ordinal ordinal) (update :call-core-node-id ids)
                 (update :argument-core-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        module (assoc (:module base) :effects [:memory/allocate])
        core (assoc (:core base) :module module :nodes node-list :calls [call] :call-edges [edge])
        nodes (into {} (map (juxt :node-id identity) node-list)) node (get nodes (:core-node-id call))
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        prepend-owner? (or (= receiver :gravity.type/list) (= :gravity.type/list (:kind receiver))
                           (= receiver :gravity.type/set) (= :gravity.type/set (:kind receiver)))
        prefix (if prepend-owner? "sh08-pl-" "sh08-va-")
        result (c7 (symbol (str prefix "derive")) [core node call edge nodes (:bindings base) types forms])
        descriptor (:descriptor result)
        facts {:module module :type-table (assoc types (:operator-node-id call) descriptor (:node-id node) (:type result))
               :function-type-table [] :call-type-facts [(c7 (symbol (str prefix "call-fact")) [descriptor module])]
               :constraint-ledger [(c7 (symbol (str prefix "constraint")) [descriptor])]}]
    {:core core :nodes nodes :node node :forms forms :call call :edge edge :ids ids :bindings (:bindings base)
     :descriptor descriptor :result result :facts facts
     :request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}}))

(deftest body-nil-if-join-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input (sh08-helper 'list-if-join-input) c7 (sh08-helper 'invoke-c7)
        n :gravity.type/nil k :gravity.type/keyword s :gravity.type/string
        nk {:kind :gravity.type/nil-or :value-type k}
        data [(input n k 0) (input s n 1) (input nk k 2)
              (input {:kind :gravity.type/list :shape :literal :element-types [s]} {:kind :gravity.type/list :shape :literal :element-types []} 3)]
        checked (mapv (fn [{:keys [core node nodes types bindings forms]}]
                        (c7 'sh08-ij-derive [core node nodes types bindings forms])) data)
        core (assoc (:core (first data)) :nodes (vec (mapcat #(get-in % [:core :nodes]) data))
                                             :function-records (vec (mapcat #(get-in % [:core :function-records]) data))
                                             :control-flow (vec (mapcat #(get-in % [:core :control-flow]) data)))
        forms (apply merge (map :forms data)) bindings (apply merge (map :bindings data))
        types (apply merge (map (fn [data checked] (assoc (:types data) (get-in data [:node :node-id]) (:type checked))) data checked))
        rows (mapv #(c7 'sh08-ij-row [(:premise %)]) checked)
        facts {:type-table types :constraint-ledger rows}
        run #(leaf 'body-ij-context core % bindings forms)
        result (run facts) id (get-in data [0 :node :node-id])]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= [:sh08-nil-if-join-v1 :sh08-nil-if-join-v1 :sh08-nil-if-join-v1 :sh08-list-if-join-v1] (mapv :producer-rule rows)))


    (doseq [changed [(assoc facts :constraint-ledger []) (update facts :constraint-ledger conj (first rows))
                     (assoc facts :constraint-ledger (vec (reverse rows)))
                     (assoc-in facts [:constraint-ledger 0 :kind] :equality)
                     (assoc-in facts [:constraint-ledger 0 :producer-rule] :wrong)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :nil-type] :host-null)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :value-type] s)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :expected-return-premise] :declared)
                     (assoc-in facts [:type-table id :extra] true)]]
      (is (= :rejected (:status (run changed)))))
    (doseq [bad [(assoc nk :extra true) (assoc nk :value-type n) (assoc nk :value-type :gravity.type/integer)]]
      (is (false? (leaf 'body-first-order-value-type? bad)))
      (is (false? (leaf 'body-normal-compatible? bad bad)))
      (is (= :rejected (:status (leaf 'body-lc-validate-types [bad])))))
    (is (true? (leaf 'body-normal-compatible? nk nk)))
    (is (false? (leaf 'body-normal-compatible? nk k)))))

(deftest body-nil-atomic-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)]
    (doseq [[a b] [[(nullable :gravity.type/keyword) (nullable :gravity.type/string)]
                    [(nullable :gravity.type/bool) :gravity.type/nil] [:gravity.type/nil (nullable :gravity.type/character)]]]
      (let [{:keys [core node call edge nodes bindings types module]} (input-fn [a b])
            source (c7 'sh08-ae-source [core node call edge nodes bindings types module])
            actual (leaf 'body-ae-source core node call edge nodes bindings types module)]
        (is (= source actual))
        (is (= :sh08-nil-atomic-equality-v1 (:version actual)))
        (is (= :actual-value-families (get-in actual [:equality-policy :cross-family])))))))

(deftest body-list-if-join-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input (sh08-helper 'list-if-join-input) c7 (sh08-helper 'invoke-c7)
        s :gravity.type/string finite #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}
        data [(input (finite [s]) (finite []) 0) (input homogeneous (finite [s s]) 1)]
        checked (mapv (fn [{:keys [core node nodes types bindings forms]}]
                        (c7 'sh08-ij-derive [core node nodes types bindings forms])) data)
        core (assoc (:core (first data)) :nodes (vec (mapcat #(get-in % [:core :nodes]) data))
                                             :function-records (vec (mapcat #(get-in % [:core :function-records]) data))
                                             :control-flow (vec (mapcat #(get-in % [:core :control-flow]) data)))
        forms (apply merge (map :forms data)) bindings (apply merge (map :bindings data))
        types (apply merge (map (fn [data checked] (assoc (:types data) (get-in data [:node :node-id]) (:type checked))) data checked))
        rows (mapv #(c7 'sh08-ij-row [(:premise %)]) checked)
        facts {:type-table types :constraint-ledger rows}
        run #(leaf 'body-ij-context core % bindings (vec (vals forms)))
        result (run facts) ids (mapv #(get-in % [:node :node-id]) data)]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= (set ids) (set (keys (get-in result [:context :premises])))))
    (is (= (mapv :premise checked) (mapv #(get-in result [:context :premises %]) ids)))


    (doseq [changed [(assoc facts :constraint-ledger [])
                     (assoc facts :constraint-ledger (vec (reverse rows)))
                     (update facts :constraint-ledger conj (first rows))
                     (update facts :constraint-ledger conj (assoc-in (first rows) [:lhs :core-node-id] :orphan))
                     (assoc-in facts [:constraint-ledger 0 :kind] :equality)
                     (assoc-in facts [:constraint-ledger 0 :producer-rule] :wrong)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :expected-return-premise] :declared-return)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :profile] :hosted)
                     (assoc-in facts [:constraint-ledger 0 :dependencies :target] :llvm)
                     (assoc-in facts [:type-table (first ids) :element-type] :gravity.type/keyword)
                     (assoc-in facts [:type-table (get-in data [0 :nids :then]) :element-types] [:gravity.type/keyword])]]
      (is (= :rejected (:status (run changed)))))
    (let [first-data (first data) owner (:definition first-data)
          altered (assoc-in bindings [owner :binding-class] :core)]
      (is (= :list-if-join-source-mismatch
             (:reason (leaf 'body-ij-context core facts altered forms)))))
    (doseq [[left right condition expected] [[(finite [s]) (finite [s]) :gravity.type/bool (finite [s])]
                                            [:gravity.type/never (finite []) :gravity.type/bool (finite [])]
                                            [(finite [s]) (finite []) :gravity.type/never :gravity.type/never]
                                            [:gravity.type/never :gravity.type/never :gravity.type/bool :gravity.type/never]]]
      (let [{:keys [core node nodes types bindings forms nids]} (input left right)
            facts {:type-table (assoc types (:node-id node) expected (nids :condition) condition) :constraint-ledger []}
            run #(leaf 'body-ij-context core facts bindings forms)
            result (run)]
        (is (= :accepted (:status result)))
        (is (= {} (get-in result [:context :premises])))

))))

(deftest body-homogeneous-list-return-function-joins-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'homogeneous-list-return-definition-input)
        s :gravity.type/string homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}]
    (doseq [actual [{:kind :gravity.type/list :shape :literal :element-types []}
                   {:kind :gravity.type/list :shape :literal :element-types [s]} homogeneous :gravity.type/never]]
      (let [{:keys [core facts bindings forms record]} (input-fn actual)
            returns (leaf 'body-lr-context core facts bindings forms)
            facts (assoc facts :return-context (:context returns))
            check #(leaf 'body-function-joins-valid? core % bindings)]
        (is (= :accepted (:status returns)) (pr-str returns))
        (is (true? (check facts)))
        (doseq [changed [(dissoc facts :return-context)
                         (assoc-in facts [:function-type-table 0 :return-compatibility :relation] :parameter-compatibility)
                         (assoc-in facts [:type-table (:function-core-node-id record) :return-actual-type] :gravity.type/list)
                         (assoc-in facts [:function-type-table 0 :return-actual-type] :gravity.type/list)]]
          (is (false? (check changed))))))))

(deftest body-homogeneous-list-return-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [products (sh08-helper 'homogeneous-list-return-products)
        positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        atomics [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                 :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64 :gravity.type/f32 :gravity.type/f64
                 :gravity.type/bool :gravity.type/string :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio]]
    (doseq [element atomics members [[] [element] (vec (repeat 1024 element))]]
      (let [type {:kind :gravity.type/list :shape :homogeneous :element-type element}
            result (leaf 'body-lr-validate-types type (positional members))]
        (is (= {:status :accepted :relation :all-positional-elements-match
                 :checked-member-count (count members)} result))))
    (let [s :gravity.type/string type {:kind :gravity.type/list :shape :homogeneous :element-type s}
          actuals (vec (concat [(positional (vec (repeat 1024 s))) type :gravity.type/never] (repeat 9 (positional []))))
          {:keys [inputs core forms bindings functions checked facts]} (products actuals s)
          ids (mapv :function-syntax-id (:function-records core))
          run (fn [facts] (leaf 'body-lr-context core facts bindings (vec (vals forms))))
          result (run facts) rows (:constraint-ledger facts)
          first-id (first ids) index (.indexOf (mapv :function-id (:function-type-table facts)) first-id)
          body-id (get-in inputs [0 :record :body-core-node-id])]
      (is (= :accepted (:status checked) (:status result)) (pr-str result))
      (is (= 12 (count rows) (count (get-in result [:context :premises]))))
      (is (not= ids (vec (keys functions))))
      (is (= ids (mapv #(get-in % [:lhs :function-id]) rows)))
      (is (= (set ids) (set (keys (get-in result [:context :premises])))))


      (doseq [changed [(assoc facts :constraint-ledger [])
                       (update facts :constraint-ledger conj (first rows))
                       (assoc facts :constraint-ledger (vec (reverse rows)))
                       (update facts :constraint-ledger conj (assoc-in (first rows) [:lhs :function-id] :orphan))
                       (assoc-in facts [:constraint-ledger 0 :producer-rule] :sh08-list-parameter-compatibility-v2)
                       (assoc-in facts [:constraint-ledger 0 :kind] :equality)
                       (assoc-in facts [:constraint-ledger 0 :dependencies :actual-body-type] :gravity.type/list)
                       (assoc-in facts [:function-type-table index :return-compatibility :version] :sh08-homogeneous-list-parameter-v1)
                       (assoc-in facts [:function-type-table index :return-actual-type] :gravity.type/list)
                       (assoc-in facts [:function-type-table index :return] :gravity.type/list)
                       (assoc-in facts [:type-table body-id] (positional [:gravity.type/keyword]))]]
        (is (= :rejected (:status (run changed)))))
      (doseq [changed-bindings [(assoc-in bindings [(get-in inputs [0 :declaration :element-binding-id]) :kind] :var)]]
        (is (= :list-return-source-mismatch
               (:reason (leaf 'body-lr-context core facts changed-bindings (vec (vals forms)))))))
      (is (= :list-return-constraint-census
             (:reason (leaf 'body-lr-context (assoc core :function-records []) facts bindings (vec (vals forms)))))))
    (doseq [actual [:gravity.type/list :gravity.type/nil :gravity.type/unknown
                    (positional [:gravity.type/never]) (positional [:gravity.type/nil]) (positional [:gravity.type/keyword])
                    (positional [:gravity.type/unknown]) (positional [{:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}])]]
      (is (= :rejected (:status (leaf 'body-lr-validate-types {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string} actual)))))))

(deftest body-list-prepend-development-kernel
  (let [s :gravity.type/string homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}
        positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)]
    (doseq [[receiver value] [[:gravity.type/list :gravity.type/nil] [(positional [s]) s] [homogeneous s]
                              [homogeneous :gravity.type/never]]]
      (let [{:keys [core node call edge nodes bindings descriptor facts request ids]} (prepend-effect-input receiver value 0)
            context (leaf 'body-va-context request facts bindings)
            facts (assoc facts :append-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes (:node-id node)])
            normal? (not= value :gravity.type/never)]
        (is (= :accepted (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (valid facts)))
        (is (= :list-prepend (get-in context [:context :families (:node-id node)])))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= :sh09-list-prepend-allocation-v1 (get-in record [:allocation :rule-version])))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= context (leaf 'body-va-context request facts bindings)))

        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [changed [(update-in graph [:nodes (:node-id node)] dissoc :allocation)
                         (assoc-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?] (not normal?))
                         (assoc-in graph [:nodes (:node-id node) :allocation :storage-policy :suffix] :copies-all)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (:nodes changed)))))
        (is (false? (valid (assoc-in facts [:append-context :forms (get-in node [:source :form-id]) :child-form-ids] []))))
        (is (= :rejected (:status (leaf 'body-va-context request (assoc-in facts [:type-table (ids :arg1)] :gravity.type/unknown) bindings))))
        (when-not normal?
          (let [input-id (ids :arg0) value-id (ids :arg1) operator-id (ids :operator)
                operands {operator-id {:type descriptor :direct #{} :transitive #{}}
                          input-id {:type receiver :direct #{:io/write} :transitive #{:io/write}}
                          value-id {:type :gravity.type/never :direct #{:error/throw} :transitive #{:error/throw}
                                    :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
                derived (leaf 'body-node-record node nil operands :gravity.type/never)]
            (is (= #{} (:direct derived)))
            (is (= #{:io/write :error/throw} (:transitive derived)))
            (is (= [s] (:escaping-thrown-error-types derived)))))))
    (let [nested {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 1024 s))}
          inputs [(prepend-effect-input homogeneous s 0)
                  (prepend-effect-input (positional []) nested 1)
                  (prepend-effect-input {:kind :gravity.type/vector :shape :literal :element-types [s]} s 2)]
          core (assoc (:core (first inputs)) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs))
                                           :calls (mapv :call inputs) :call-edges (mapv :edge inputs))
          forms (apply merge (map :forms inputs)) bindings (:bindings (first inputs))
          facts {:module (:module core) :type-table (apply merge (map #(get-in % [:facts :type-table]) inputs))
                 :function-type-table [] :call-type-facts (vec (mapcat #(get-in % [:facts :call-type-facts]) inputs))
                 :constraint-ledger (vec (mapcat #(get-in % [:facts :constraint-ledger]) inputs))}
          request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          context (leaf 'body-va-context request facts bindings)
          facts (assoc facts :append-context (:context context))
          id (get-in inputs [0 :node :node-id]) nested-id (get-in inputs [1 :node :node-id])
          row (first (:constraint-ledger facts))]
      (is (= :accepted (:status context)) (pr-str context))
      (is (= {:list-prepend 2 :vector-append 1} (frequencies (vals (get-in context [:context :families])))))
      (let [checked (get-in context [:context :type-checks nested-id])]
        (is (not-any? #(contains? checked %) [:work-count :construction-work]))
        (is (= (positional [nested]) (:result-type checked)))
        (is (not-any? #(contains? checked %) [:operand-work :result-work])))
      (is (true? (leaf 'body-call-joins-with-foreign-valid? core facts bindings {})))
      (is (= context (leaf 'body-va-context request facts bindings)))

      (doseq [ledger [(subvec (:constraint-ledger facts) 1)
                      (conj (:constraint-ledger facts) row)
                      (conj (:constraint-ledger facts) (assoc row :lhs {:core-node-id :orphan :projection :list-prepend}))
                      (assoc (:constraint-ledger facts) 0 (assoc row :producer-rule :sh08-vector-append-v1 :lhs {:core-node-id id :projection :vector-append}))]]
        (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger ledger) bindings)))))
      (let [empty-core (assoc core :calls [] :nodes [] :call-edges [])]
        (is (= :body-conj-constraint-census (:reason (leaf 'body-va-context (assoc request :canonical-core-artifact empty-core) facts bindings)))))
      (let [invalid (assoc-in request [:canonical-core-artifact :calls 0 :argument-node-ids] 42)]
        (is (= :body-conj-call-shape-required (:reason (leaf 'body-va-context invalid facts bindings)))))
      (doseq [children [nil 42 :children '(:operator :arg0 :arg1) [] (vec (repeat 8192 :arg0))]]
        (let [index (.indexOf (mapv :node-id (:nodes core)) id)
              invalid (assoc-in request [:canonical-core-artifact :nodes index :children] children)]
          (is (= :body-conj-call-shape-required (:reason (leaf 'body-va-context invalid facts bindings))))))
)))

(deftest body-vector-append-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'vector-append-input) derive (ns-resolve upstream 'vector-append-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [never-operand [nil :input :value]]
      (let [base (cond-> (input-fn [:gravity.type/keyword] :gravity.type/keyword)
                   never-operand (assoc-in [:types never-operand] :gravity.type/never))
            ids (zipmap (keys (:nodes base)) (map sha (range 1200 1210)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :association) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-va-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-va-constraint [descriptor])]}
            context (leaf 'body-va-context request facts (:bindings base))
            facts (assoc facts :append-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :association)])
            equations #(leaf 'body-graph-equations-valid? core facts {} %)]
        (is (= :accepted (:status result)))
        (is (= :accepted (:status context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= (if never-operand #{} #{:memory/allocate}) (:direct record) (:transitive record)))
        (is (= :static-persistent-vector-append (get-in record [:allocation :behavior])))
        (is (= (nil? never-operand) (get-in record [:allocation :normal-completion-possible?])))
        (is (= {:unit :elements :count (when-not never-operand 2)} (get-in record [:allocation :extent])))
        (is (= :pending-runtime-provider (get-in record [:allocation :provider])))
        (is (= (mapv ids [:operator :input :value]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (equations (:nodes graph))))

        (when-not never-operand
          (let [core (merge core {:root-core-node-ids [(ids :association)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records
                                                   :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                                   {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (true? (leaf 'body-shape-valid? core facts)))
            (is (= :accepted (:status analyzed)) (pr-str analyzed))))
        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core)
                         (assoc descriptor :application (if never-operand :normal :not-invoked))
                         (assoc descriptor :direct-effects [:io/write])
                         (assoc descriptor :input-extent 9) (assoc descriptor :result-extent 9)
                         (assoc descriptor :append-semantics {:operation :destructive-append})
                         (assoc descriptor :result-type :gravity.type/string :return :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-va-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-va-constraint [changed])]))]
            (is (false? (valid candidate)) (pr-str changed))))
        (doseq [changed [(assoc-in (:nodes graph) [(ids :association) :direct] #{:io/write})
                         (assoc-in (:nodes graph) [(ids :association) :allocation :extent :count] 9)
                         (assoc-in (:nodes graph) [(ids :association) :allocation :normal-completion-possible?] (boolean never-operand))
                         (assoc-in (:nodes graph) [(ids :association) :dependencies] [])]]
          (is (false? (equations changed))))))))

(defn- list-count-effect-input [type]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [base ((sh08-helper 'list-count-input) type) c7 (sh08-helper 'invoke-c7)
        ids (zipmap (keys (:nodes base)) (map sha (range 2100 2110)))
        source #(-> % (update :form-id ids) (update :syntax-id ids))
        node-list (mapv #(-> % (update :node-id ids) (update :children (fn [children] (mapv ids children))) (update :source source)
                              (update-in [:evaluation :order] (fn [order] (mapv (fn [entry] (update entry :core-node-id ids)) order))))
                        (get-in base [:core :nodes]))
        forms (into {} (for [[id form] (:forms base)]
                         [(ids id) (-> form (update :form-id ids) (update :syntax-id ids) (update :parent-form-id ids)
                                       (update :child-form-ids #(mapv ids %)))]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
        nodes (into {} (map (juxt :node-id identity) node-list)) node (nodes (:core-node-id call))
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        result (c7 'sh08-cq-derive [core node call edge nodes (:bindings base) types forms]) descriptor (:descriptor result)
        facts {:module (:module core) :function-type-table [] :type-table (assoc types (:operator-node-id call) descriptor (:node-id node) (:type result))
               :call-type-facts [(c7 'sh08-cq-call-fact [descriptor (:module core)])] :constraint-ledger [(c7 'sh08-cq-constraint [descriptor])]}]
    {:core core :nodes nodes :node node :forms forms :bindings (:bindings base) :call call :edge edge
     :descriptor descriptor :facts facts :ids ids :request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}}))

(deftest body-list-count-development-kernel
  (let [s :gravity.type/string homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}
        positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)]
    (doseq [type [:gravity.type/list homogeneous (positional []) (positional [s :gravity.type/nil :gravity.type/bool]) {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} :gravity.type/never]]
      (let [{:keys [core nodes node forms bindings call edge descriptor facts request ids]} (list-count-effect-input type)
            context (leaf 'body-cq-context request facts bindings) facts (assoc facts :count-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes (:node-id node)])]
        (is (= :accepted (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= context (leaf 'body-cq-context request facts bindings)))

        (is (= (if (= type :gravity.type/never) :established-map-or-vector
                 (if (= :gravity.type/set (:kind type)) :established-persistent-set :established-persistent-list))
               (get-in descriptor [:query-semantics :normal-input])))
        (doseq [changed [(assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :cardinality 0)
                         (assoc-in descriptor [:query-semantics :normal-input] :arbitrary-sequence)
                         (assoc-in descriptor [:query-semantics :operand-evaluation] :twice)]]
          (is (false? (valid (assoc-in facts [:type-table (ids :operator)] changed)))))
        (doseq [args [nil 42 :arguments [] [:one :two] (vec (repeat 8192 :operand))]]
          (is (nil? (leaf 'body-cq-source core node (assoc call :argument-node-ids args) edge nodes bindings (:type-table facts) (:module core)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc-in (:nodes graph) [(:node-id node) :allocation] {:construction :none}))))
        (let [operand (ids :arg0) operator (ids :operator)
              graph {operator {:type descriptor :direct #{} :transitive #{}}
                     operand {:type :gravity.type/never :direct #{:io/write :error/throw} :transitive #{:io/write :error/throw}
                              :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
              stopped (leaf 'body-node-record node nil graph :gravity.type/never)]
          (is (= #{} (:direct stopped)))
          (is (= #{:io/write :error/throw} (:transitive stopped)))
          (is (= [s] (:escaping-thrown-error-types stopped))))))))

(deftest body-collection-count-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'collection-count-input) derive (ns-resolve upstream 'collection-count-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [kind [:map :vector] never? [false true]]
      (let [base (cond-> (input-fn kind false) never? (assoc-in [:types :input] :gravity.type/never))
            ids (zipmap (keys (:nodes base)) (map sha (range 980 990)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :association) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-cq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-cq-constraint [descriptor])]}
            context (leaf 'body-cq-context request facts (:bindings base))
            facts (assoc facts :count-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :association)])]
        (is (= :accepted (:status result)))
        (is (= :accepted (:status context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (is (= #{:memory/allocate} (:transitive record)))
        (is (= (mapv ids [:operator :input]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))

        (doseq [forms [nil 7 {}]]
          (is (= :body-collection-count-source-forms-required
                 (:reason (leaf 'body-cq-context (assoc-in request [:b47-context :authenticated-core-request :forms] forms)
                                facts (:bindings base))))))
        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core)
                         (assoc descriptor :application (if never? :normal :not-invoked))
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :latent-effects [:memory/allocate])
                         (assoc descriptor :query-semantics {:operation :cached-cardinality :construction :none})
                         (assoc descriptor :cardinality 1) (assoc descriptor :input-shape {})
                         (assoc descriptor :return :gravity.type/i64 :result-type :gravity.type/i64)
                         (assoc-in descriptor [:source :syntax-id] :sibling)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-cq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-cq-constraint [changed])]))]
            (is (false? (valid candidate)) (pr-str changed))))
        (doseq [candidate [(assoc facts :constraint-ledger []) (update facts :constraint-ledger into (:constraint-ledger facts))
                           (assoc facts :call-type-facts []) (assoc-in facts [:count-context :forms :input :parent-form-id] :sibling)
                           (assoc-in facts [:count-context :forms :association :child-form-ids] 7)
                           (assoc-in facts [:call-type-facts 0 :effects] [:io/write])]]
          (is (false? (valid candidate))))
        (doseq [changed [(assoc record :transitive #{} :escaping #{} :dependencies [])
                         (assoc record :direct #{:memory/allocate}) (assoc record :allocation {:construction :none})]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc (:nodes graph) (ids :association) changed)))))
        (let [operand-graph (assoc (:nodes graph) (ids :input) {:type :gravity.type/never :transitive #{:memory/allocate :error/throw}
                                                                 :escaping-thrown-errors #{:gravity.type/string}
                                                                 :escaping-thrown-error-types [:gravity.type/string]})
              stopped (leaf 'body-node-record (:node input) nil operand-graph :gravity.type/never)]
          (is (= #{} (:direct stopped)))
          (is (= #{:memory/allocate :error/throw} (:transitive stopped)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types stopped))))))))

(deftest body-vector-nth-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'vector-nth-input) derive (ns-resolve upstream 'vector-nth-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [never-ids [[] [:input] [:index] [:input :index]]]
      (let [base (reduce #(assoc-in %1 [:types %2] :gravity.type/never) (input-fn [:gravity.type/string] 0) never-ids)
            ids (zipmap (keys (:nodes base)) (map sha (range 1000 1010)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base)))
                         :products (assoc (:products base) :calls [call] :call-edges [edge]))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :association) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-vn-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-vn-constraint [descriptor])]}
            context (leaf 'body-vn-context request facts (:bindings base))
            facts (assoc facts :vector-nth-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :association)])]
        (is (= :accepted (:status result)))
        (is (= :accepted (:status context)))
        (is (true? (valid facts)))
        (is (= :accepted (:status graph)) (pr-str graph))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :input :index]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))

        (doseq [forms [nil 7 {}]]
          (is (= :body-vector-nth-source-forms-required
                 (:reason (leaf 'body-vn-context (assoc-in request [:b47-context :authenticated-core-request :forms] forms)
                                facts (:bindings base))))))
        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core) (assoc descriptor :application (if (seq never-ids) :normal :not-invoked))
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :thrown-error-effects [:error/throw])
                         (assoc descriptor :return :gravity.type/i64 :result-type :gravity.type/i64)
                         (assoc descriptor :index-value 1) (assoc descriptor :vector-length 7)
                         (assoc descriptor :selected-type :gravity.type/bool)
                         (assoc-in descriptor [:bounds-proof :outcome] :unsafe-island)
                         (assoc-in descriptor [:bounds-proof :check-elision] :authorized)
                         (assoc-in descriptor [:bounds-proof :index-value] 1)
                         (assoc descriptor :input-shape {})
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-vn-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-vn-constraint [changed])]))]
            (is (false? (valid candidate)) (pr-str changed))))
        (doseq [[label candidate] [[:missing-row (assoc facts :constraint-ledger [])]
                                  [:duplicate-row (update facts :constraint-ledger into (:constraint-ledger facts))]
                                  [:missing-fact (assoc facts :call-type-facts [])]
                                  [:malformed-source-children (assoc-in facts [:vector-nth-context :forms :association :child-form-ids] 7)]
                                  [:index-source-value (assoc-in facts [:vector-nth-context :forms :index :value] 1)]
                                  [:own-effects (assoc-in facts [:call-type-facts 0 :effects] [:io/write])]]]
          (when-not (= candidate facts)
            ;; A Never index has no literal-index proof. C7 owns complete
            ;; source/type consistency; this synthetic value is not an effect premise.
            (is (= (if (= label :index-source-value) (contains? (set never-ids) :index) false) (valid candidate))
                (pr-str {:never-ids never-ids :case label}))))
        (let [operand-graph (assoc (:nodes graph) (ids :input) {:type (get-in facts [:type-table (ids :input)]) :transitive #{:memory/allocate}}
                                               (ids :index) {:type :gravity.type/never :transitive #{:error/throw}
                                                             :escaping-thrown-errors #{:gravity.type/string}
                                                             :escaping-thrown-error-types [:gravity.type/string]})
              stopped (leaf 'body-node-record (:node input) nil operand-graph :gravity.type/never)]
          (is (= #{} (:direct stopped)))
          (is (= #{:memory/allocate :error/throw} (:transitive stopped)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types stopped))))))))

(deftest body-bounded-successor-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'bounded-successor-input) derive (ns-resolve upstream 'bounded-successor-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [extent [0 1 3]]
      (let [{:keys [core nodes bindings types forms] :as input} (input-fn extent)
            result (derive input) descriptor (:descriptor result)
            count-result (c7 'sh08-cq-derive [core (nodes :count-query) (first (:calls core)) (first (:call-edges core)) nodes bindings types forms])
            count-descriptor (:descriptor count-result)
            ordering (c7 'sh08-io-descriptor [core (nodes :guard) (second (:calls core)) (second (:call-edges core)) nodes bindings types (:module core)])
            facts {:module (:module core) :type-table (assoc types :inc-op descriptor :count-op count-descriptor :less-op ordering)
                   :function-type-table []
                   :call-type-facts [(c7 'sh08-cq-call-fact [count-descriptor (:module core)]) (c7 'sh08-io-call-fact [ordering (:module core)])
                                     (c7 'sh08-si-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-cq-constraint [count-descriptor]) (c7 'sh08-io-constraint [ordering]) (c7 'sh08-si-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            counts (leaf 'body-cq-context request facts bindings)
            context (leaf 'body-si-context request facts bindings)
            facts (assoc facts :count-context (:context counts) :successor-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})]
        (is (= :accepted (:status result) (:status count-result) (:status context)))
        (is (true? (valid facts)))

        (is (= context (leaf 'body-si-context request facts bindings)))
        (doseq [changed [(assoc descriptor :numeric-mode :wrapping) (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :thrown-error-effects [:error/throw])
                         (assoc descriptor :return :gravity.type/i64 :result-type :gravity.type/i64)
                         (assoc-in descriptor [:range-proof :vector-extent] (inc extent))
                         (assoc-in descriptor [:range-proof :invariant :upper] (inc extent))
                         (assoc-in descriptor [:range-proof :backedge-paths] [])
                         (assoc-in descriptor [:range-proof :backedge-paths] [7])
                         (assoc-in descriptor [:range-proof :backedge-paths] [nil])
                         (assoc-in descriptor [:range-proof :backedge-paths] [{}])
                         (assoc-in descriptor [:range-proof :backedge-paths] ["path"])
                         (assoc-in descriptor [:range-proof :incoming-transfers] [])
                         (assoc-in descriptor [:range-proof :normal-exit :core-node-id] :inc-slot)
                         (assoc-in descriptor [:range-proof :check-elision] :authorized)]]
          (let [candidate (-> facts (assoc-in [:type-table :inc-op] changed)
                              (assoc-in [:call-type-facts 2] (c7 'sh08-si-call-fact [changed (:module core)]))
                              (assoc-in [:constraint-ledger 2] (c7 'sh08-si-constraint [changed])))]
            (is (false? (valid candidate)) (pr-str changed))))
        (doseq [candidate [(update facts :constraint-ledger pop) (update facts :constraint-ledger conj (last (:constraint-ledger facts)))
                           (assoc-in facts [:successor-context :forms :inc-op :value] 'forged)
                           (assoc-in facts [:type-table :count-query] :gravity.type/i64)]]
          (is (false? (valid candidate))))))))

(defn- body-unit-step-products [input access]
  ;; Normalized current-source proof consumers, not an authenticated module product.
  (let [c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        {:keys [core nodes bindings types forms]} input
        count-result (c7 'sh08-cq-derive [core (nodes :count-query) (first (:calls core)) (first (:call-edges core)) nodes bindings types forms])
        count-descriptor (:descriptor count-result)
        ordering (c7 'sh08-io-descriptor [core (nodes :guard) (second (:calls core)) (second (:call-edges core)) nodes bindings types (:module core)])
        types (cond-> (assoc types :count-op count-descriptor :less-op ordering) access (assoc :nth-op access))
        descriptor (fn [call] (get types (:operator-node-id call)))
        fact (fn [call] (case (:core-node-id call)
                         :count-query (c7 'sh08-cq-call-fact [(descriptor call) (:module core)])
                         :guard (c7 'sh08-io-call-fact [(descriptor call) (:module core)])
                         :read (c7 'sh08-vn-call-fact [(descriptor call) (:module core)])
                         (c7 'sh08-si-call-fact [(descriptor call) (:module core)])))
        row (fn [call] (case (:core-node-id call)
                        :count-query (c7 'sh08-cq-constraint [(descriptor call)])
                        :guard (c7 'sh08-io-constraint [(descriptor call)])
                        :read (c7 'sh08-vn-constraint [(descriptor call)])
                        (c7 'sh08-si-constraint [(descriptor call)])))
        facts {:module (:module core) :type-table types :function-type-table []
               :call-type-facts (mapv fact (:calls core)) :constraint-ledger (mapv row (:calls core))}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        counts (leaf 'body-cq-context request facts bindings)
        successors (leaf 'body-si-context request facts bindings)
        accesses (leaf 'body-vn-context request facts bindings)]
    {:request request :successors successors :accesses accesses
     :facts (assoc facts :count-context (:context counts) :successor-context (:context successors) :vector-nth-context (:context accesses))}))

(deftest body-bounded-unit-addition-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        source (ns-resolve upstream 'bounded-successor-input) two (ns-resolve upstream 'bounded-successor-two-backedges)
        convert (ns-resolve upstream 'bounded-unit-addition-input) complete (ns-resolve upstream 'bounded-unit-addition-completed)
        nth-source (ns-resolve upstream 'vector-loop-nth-input) get-source (ns-resolve upstream 'vector-loop-get-input)
        access-derive (ns-resolve upstream 'vector-loop-nth-derived) c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[base access?] [[(source 3) false] [(two (source 3)) false]
                           [(nth-source 3 false) true] [(get-source 3) true]]]
      (let [{:keys [core nodes bindings] :as input} (complete (convert base :increment))
            access (when access? (:descriptor (access-derive input)))
            {:keys [request facts successors accesses]} (body-unit-step-products input access)
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            descriptor (get-in facts [:type-table :inc-op]) id (:call-core-node-id descriptor)
            replace-step (fn [changed]
                           (-> facts (assoc-in [:type-table :inc-op] changed)
                               (update :call-type-facts #(mapv (fn [fact] (if (= id (:call-core-node-id fact))
                                                                          (c7 'sh08-si-call-fact [changed (:module core)]) fact)) %))
                               (update :constraint-ledger #(mapv (fn [row] (if (= id (get-in row [:lhs :core-node-id]))
                                                                           (c7 'sh08-si-constraint [changed]) row)) %))))]
        (is (= :accepted (:status successors) (:status accesses)))
        (is (true? (valid facts)))
        (is (= successors (leaf 'body-si-context request facts bindings)))
        (doseq [steps [7 nil {} [nil] (vec (repeat 2048 (first (get-in descriptor [:range-proof :step-sources]))))]]
          (is (false? (leaf 'body-si-step-forms-valid? (assoc (:range-proof descriptor) :step-sources steps) core nodes
                            (get-in facts [:successor-context :forms])))))

        (doseq [changed [(assoc descriptor :kind :gravity.type/applied-bounded-successor :version :sh08-bounded-successor-v1 :operation 'inc)
                         (assoc-in descriptor [:range-proof :version] :sh08-bounded-successor-v1)
                         (assoc-in descriptor [:range-proof :step-sources] [])
                         (assoc-in descriptor [:range-proof :step-sources 0 :unit-source :value] 2)
                         (assoc-in descriptor [:range-proof :step-sources 0 :unit-node :attributes :value] 2)
                         (assoc-in descriptor [:range-proof :check-elision] :authorized)
                         (assoc descriptor :numeric-mode :checked)]]
          (is (false? (valid (replace-step changed)))))
        (doseq [candidate [(update facts :constraint-ledger #(filterv (fn [row] (not= id (get-in row [:lhs :core-node-id]))) %))
                           (update facts :constraint-ledger conj (c7 'sh08-si-constraint [descriptor]))
                           (update facts :constraint-ledger conj
                             (assoc-in (c7 'sh08-si-constraint [descriptor]) [:lhs :core-node-id] :orphan))
                           (assoc-in facts [:successor-context :forms :increment-unit :value] 2)
                           (assoc-in facts [:type-table :increment-unit] :gravity.type/never)]]
          (is (false? (valid candidate))))
        (when access?
          (is (true? (leaf 'body-vn-node? (nodes :read) facts core)))
          (is (true? (leaf 'body-vl-proof-valid? access core (nodes :read) nodes bindings (:type-table facts))))
          (is (false? (leaf 'body-vn-node? (nodes :read)
                            (assoc-in facts [:vector-nth-context :forms :increment-unit :value] 2) core)))
          (is (= accesses (leaf 'body-vn-context request facts bindings)))
)))))

(defn- body-vector-loop-nth-products [input descriptor]
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        {:keys [core nodes bindings types forms]} input
        count-result (c7 'sh08-cq-derive [core (nodes :count-query) (get (:calls core) 0) (get (:call-edges core) 0) nodes bindings types forms])
        count-descriptor (:descriptor count-result)
        ordering (c7 'sh08-io-descriptor [core (nodes :guard) (get (:calls core) 1) (get (:call-edges core) 1) nodes bindings types (:module core)])
        invariant (get types :inc-op)
        facts {:module (:module core) :type-table (assoc types :nth-op descriptor :count-op count-descriptor :less-op ordering)
               :function-type-table []
               :call-type-facts [(c7 'sh08-cq-call-fact [count-descriptor (:module core)]) (c7 'sh08-io-call-fact [ordering (:module core)])
                                 (c7 'sh08-vn-call-fact [descriptor (:module core)]) (c7 'sh08-si-call-fact [invariant (:module core)])]
               :constraint-ledger [(c7 'sh08-cq-constraint [count-descriptor]) (c7 'sh08-io-constraint [ordering])
                                   (c7 'sh08-vn-constraint [descriptor]) (c7 'sh08-si-constraint [invariant])]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        counts (leaf 'body-cq-context request facts bindings) context (leaf 'body-vn-context request facts bindings)
        successors (leaf 'body-si-context request facts bindings)
        facts (assoc facts :count-context (:context counts) :vector-nth-context (:context context) :successor-context (:context successors))]
    {:facts facts :request request :context context :count-result count-result}))

(deftest body-vector-loop-nth-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        source (ns-resolve upstream 'vector-loop-nth-input) complete (ns-resolve upstream 'vector-loop-nth-completed)
        derive (ns-resolve upstream 'vector-loop-nth-derived) c7 (ns-resolve upstream 'invoke-c7)
        alias (ns-resolve upstream 'bounded-successor-alias)]
    (doseq [input [(complete (source 3 false)) (complete (source 3 true)) (complete (alias (source 3 false)))]]
      (let [{:keys [core nodes bindings types]} input descriptor (:descriptor (derive input))
            {:keys [facts request context count-result]} (body-vector-loop-nth-products input descriptor)
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            replace-access (fn [changed] (-> facts (assoc-in [:type-table :nth-op] changed)
                                            (assoc-in [:call-type-facts 2] (c7 'sh08-vn-call-fact [changed (:module core)]))
                                            (assoc-in [:constraint-ledger 2] (c7 'sh08-vn-constraint [changed]))))]
        (is (= :accepted (:status context) (:status count-result)))
        (is (true? (valid facts)))
        (is (= context (leaf 'body-vn-context request facts bindings)))

        (doseq [changed [(assoc descriptor :version :sh08-vector-nth-v1)
                         (assoc descriptor :selected-type :gravity.type/string :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc-in descriptor [:bounds-proof :index-node-id] :increment)
                         (assoc-in descriptor [:bounds-proof :slot-binding-id] :shadowed)
                         (assoc-in descriptor [:bounds-proof :guard-path] 7)
                         (assoc-in descriptor [:bounds-proof :guard-path] [7])
                         (assoc-in descriptor [:bounds-proof :guard-path] [nil])
                         (assoc-in descriptor [:bounds-proof :guard-path] [{}])
                         (assoc-in descriptor [:bounds-proof :guard-path] ["path"])
                         (assoc-in descriptor [:bounds-proof :guard-path 1 :child-index] 2)
                         (assoc-in descriptor [:bounds-proof :vector-origin :node] (nodes :loop))
                         (assoc-in descriptor [:bounds-proof :check-elision] :authorized)]]
          (is (false? (valid (replace-access changed)))))
        (let [invariant (get types :inc-op) changed (assoc-in invariant [:range-proof :target :target-id] :wrong-target)
              descriptor (assoc-in descriptor [:bounds-proof :loop-invariant] changed)
              candidate (-> (replace-access descriptor) (assoc-in [:type-table :inc-op] changed)
                            (assoc-in [:call-type-facts 3] (c7 'sh08-si-call-fact [changed (:module core)]))
                            (assoc-in [:constraint-ledger 3] (c7 'sh08-si-constraint [changed])))]
          (is (false? (valid candidate))))
        (doseq [candidate [(update facts :constraint-ledger #(vec (concat (take 2 %) (drop 3 %))))
                           (update facts :constraint-ledger conj (get-in facts [:constraint-ledger 2]))
                           (assoc-in facts [:vector-nth-context :forms :nth-index :value] 'shadowed)]]
          (is (false? (valid candidate))))
        (let [children {:nth-op {:transitive #{}}
                        :nth-vector {:transitive #{:memory/allocate}}
                        :nth-index {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string}
                                    :escaping-thrown-error-types [:gravity.type/string]}}
              record (leaf 'body-node-record (nodes :read) nil children :gravity.type/bool)]
          (is (= #{} (:direct record)))
          (is (= #{:memory/allocate :error/throw} (:transitive record)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types record)))
          (is (= [:nth-op :nth-vector :nth-index] (:dependencies record))))))))


(deftest body-vector-loop-get-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        source (ns-resolve upstream 'vector-loop-get-input) complete (ns-resolve upstream 'vector-loop-nth-completed)
        derive (ns-resolve upstream 'vector-loop-nth-derived) c7 (ns-resolve upstream 'invoke-c7)
        alias (ns-resolve upstream 'bounded-successor-alias)]
    (doseq [input [(complete (source 3)) (complete (source 1)) (complete (alias (source 3)))]]
      (let [{:keys [core nodes bindings types]} input descriptor (:descriptor (derive input))
            {:keys [facts request context count-result]} (body-vector-loop-nth-products input descriptor)
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            replace-access (fn [changed] (-> facts (assoc-in [:type-table :nth-op] changed)
                                            (assoc-in [:call-type-facts 2] (c7 'sh08-vn-call-fact [changed (:module core)]))
                                            (assoc-in [:constraint-ledger 2] (c7 'sh08-vn-constraint [changed]))))]
        (is (= :accepted (:status context) (:status count-result)))
        (is (true? (valid facts)))
        (is (= context (leaf 'body-vn-context request facts bindings)))

        (doseq [changed [(assoc descriptor :version :sh08-vector-nth-v1)
                         (assoc descriptor :kind :gravity.type/applied-vector-nth :version :sh08-vector-nth-loop-v1
                                           :operation 'nth :intrinsic :gravity.collections/proven-vector-nth)
                         (assoc descriptor :selected-type :gravity.type/string :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc-in descriptor [:bounds-proof :index-node-id] :increment)
                         (assoc-in descriptor [:bounds-proof :slot-binding-id] :shadowed)
                         (assoc-in descriptor [:bounds-proof :guard-path] 7)
                         (assoc-in descriptor [:bounds-proof :guard-path] [7])
                         (assoc-in descriptor [:bounds-proof :guard-path] [nil])
                         (assoc-in descriptor [:bounds-proof :guard-path] [{}])
                         (assoc-in descriptor [:bounds-proof :guard-path] ["path"])
                         (assoc-in descriptor [:bounds-proof :guard-path 1 :child-index] 2)
                         (assoc-in descriptor [:bounds-proof :vector-origin :node] (nodes :loop))
                         (assoc-in descriptor [:bounds-proof :check-elision] :authorized)]]
          (is (false? (valid (replace-access changed)))))
        (let [invariant (get types :inc-op) changed (assoc-in invariant [:range-proof :target :target-id] :wrong-target)
              descriptor (assoc-in descriptor [:bounds-proof :loop-invariant] changed)
              candidate (-> (replace-access descriptor) (assoc-in [:type-table :inc-op] changed)
                            (assoc-in [:call-type-facts 3] (c7 'sh08-si-call-fact [changed (:module core)]))
                            (assoc-in [:constraint-ledger 3] (c7 'sh08-si-constraint [changed])))]
          (is (false? (valid candidate))))
        (doseq [candidate [(update facts :constraint-ledger #(vec (concat (take 2 %) (drop 3 %))))
                           (update facts :constraint-ledger conj (get-in facts [:constraint-ledger 2]))
                           (assoc-in facts [:vector-nth-context :forms :nth-index :value] 'shadowed)
                           (assoc-in facts [:call-type-facts 2 :dispatch] :direct-vector-nth)
                           (update facts :constraint-ledger conj
                             (assoc-in (get-in facts [:constraint-ledger 2]) [:lhs :core-node-id] :orphan))]]
          (is (false? (valid candidate))))
        (let [children {:nth-op {:transitive #{}}
                        :nth-vector {:transitive #{:memory/allocate}}
                        :nth-index {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string}
                                    :escaping-thrown-error-types [:gravity.type/string]}}
              record (leaf 'body-node-record (nodes :read) nil children :gravity.type/keyword)]
          (is (= #{} (:direct record)))
          (is (= #{:memory/allocate :error/throw} (:transitive record)))
          (is (= [:gravity.type/string] (:escaping-thrown-error-types record)))
          (is (= [:nth-op :nth-vector :nth-index] (:dependencies record))))))))



(deftest body-vector-loop-nth-throw-control-authenticated-composition
  ;; One affected source consumer after the retained failed full composition.
  ;; No lowering/runtime/negative/public-boundary replay belongs in this test.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [products (fresh-type-products "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-vector-loop-nth.gravity")
        {:keys [request typed verification]} products
        binding (seal-binding request typed verification)
        result (leaf 'build-body-effect-result typed verification binding)
        graph (:effect-graph result) facts (:identity-input typed)
        descriptor (:applied-vector-nth (first (filter #(= :direct-vector-nth (:dispatch %)) (:call-type-facts facts))))
        functions (into {} (map (juxt :definition-name identity) (:function-type-table facts)))
        core (:canonical-core-artifact request)]
    (is (= :accepted (:status typed)))
    (is (= :passed (:status verification)))
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (when-not (and (= :accepted (:status typed) (:status result)) (map? graph) (seq (:nodes graph)))
      (throw (ex-info "Current-source loop nth C8 consumer rejected" {:diagnostics (:diagnostics result)})))
    (is (= :passed (:status (leaf 'verify-body-effect-result typed verification binding result))))
    (is (= :checked (get-in graph [:loop-fixed-point :equations])))
    (is (= 2 (count (get-in graph [:loop-fixed-point :recur-transfers]))))
    (is (= :sh08-vector-nth-loop-v1 (:version descriptor)))
    (let [record (get-in graph [:nodes (:call-core-node-id descriptor)])]
      (is (map? record))
      (is (= #{} (:direct record)))
      (is (= #{} (:transitive record)))
      (is (nil? (:allocation record)))
      (is (nil? (:call-body-dependency record)))
      (is (= (get-in descriptor [:source-call :ordered-evaluation-node-ids]) (:dependencies record))))
    (doseq [name ['scan 'caught 'main]]
      (is (= #{:memory/allocate :error/throw} (get-in graph [:functions (get-in functions [name :function-id]) :inferred]))))
    (is (= [:gravity.type/string] (get-in graph [:functions (get-in functions ['scan :function-id]) :throws])))
    (is (= [] (get-in graph [:functions (get-in functions ['caught :function-id]) :throws])))
    (let [caught (get-in functions ['caught :function-id])
          attempt (first (filter #(and (= :try (:core-form %))
                                        (= caught (get-in % [:evaluation :owner-function-syntax-id]))) (:nodes core)))]
      (is (map? attempt))
      (is (= [] (get-in graph [:nodes (:node-id attempt) :escaping-thrown-error-types])))
      (is (= #{:memory/allocate :error/throw} (get-in graph [:nodes (:node-id attempt) :transitive]))))
    (let [id (:call-core-node-id descriptor)
          erased (-> graph
                     (update :nodes #(into {} (map (fn [[id record]] [id (-> record (dissoc :allocation)
                                                                            (assoc :direct #{} :transitive #{} :escaping #{}))]) %)))
                     (update :functions #(into {} (map (fn [[id record]] [id (assoc record :inferred #{} :latent #{})]) %))))]
      (doseq [changed [(assoc-in graph [:nodes id :direct] #{:memory/allocate})
                       (assoc-in graph [:nodes id :dependencies] []) erased]]
        (is (= :rejected (:status (leaf 'verify-body-effect-result typed verification binding (reseal-effect-graph result changed)))))))))

(deftest body-constructor-allocation-development-kernel
  ;; Current Gravity algorithms on normalized DATA, not source admission.
  (doseq [kind [:vector :map :set] empty? [true false]]
    (let [children (if empty? [] [1 2])
          constructor (assoc (node 3 :collection-literal children) :attributes {:literal-kind kind :element-count (count children)})
          core {:nodes [(node 1 :literal []) (node 2 :literal []) constructor] :call-edges []}
          facts {:type-table {(sha 1) :gravity.type/integer (sha 2) :gravity.type/bool (sha 3) :collection}}
          result (leaf 'body-derive-nodes core facts) record (get-in result [:nodes (sha 3)])
          metadata {:rule-version :sh09-evaluated-collection-allocation-v1 :collection-kind kind
                    :behavior :static-constructor-allocation :condition :normal-operand-completion :normal-completion-possible? true
                    :extent {:unit (if (= kind :map) :entries :elements) :count (if empty? 0 (if (= kind :map) 1 2))}
                    :placement :pending-profile-representation :provider :pending-runtime-provider :ownership :pending-ownership-analysis}]
      (is (= :accepted (:status result)))
      (is (= metadata (:allocation record)))
      (is (= #{:memory/allocate} (:direct record) (:transitive record)))
      (is (= (mapv sha children) (:evaluated-children record) (:dependencies record)))
      (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes result))))
      (is (= result (leaf 'body-derive-nodes-with-foreign core facts {})))

      (doseq [changed [(dissoc record :allocation)
                       (assoc record :direct #{} :transitive #{} :escaping #{})
                       (assoc-in record [:allocation :normal-completion-possible?] false)
                       (assoc-in record [:allocation :extent :unit] :heap-bytes)
                       (assoc-in record [:allocation :extent :count] 99)
                       (assoc-in record [:allocation :provider] :observed-jvm-allocation)
                       (assoc-in record [:allocation :extra] true)]]
        (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc (:nodes result) (sha 3) changed)))))))
  (let [constructor (assoc (node 3 :collection-literal [1 2]) :attributes {:literal-kind :map :element-count 2})
        graph {(sha 1) {:type :gravity.type/never :transitive #{:error/throw}
                        :escaping-thrown-errors #{:gravity.type/string} :escaping-thrown-error-types [:gravity.type/string]}
               (sha 2) {:type :gravity.type/integer :transitive #{:memory/allocate :compiler/read-ir}}}
        record (leaf 'body-node-record constructor nil graph :gravity.type/never)
        normal (leaf 'body-node-record constructor nil (assoc-in graph [(sha 1) :type] :gravity.type/keyword) :collection)
        quote-node (assoc (node 4 :quote []) :attributes {:quoted-value {:nested [#{:data}]}})
        quote-record (leaf 'body-node-record quote-node nil {} :collection)]
    (is (= #{} (:direct record)))
    (is (false? (get-in record [:allocation :normal-completion-possible?])))
    (is (= #{:error/throw :memory/allocate :compiler/read-ir} (:transitive record)))
    (is (= [:gravity.type/string] (:escaping-thrown-error-types record)))
    (is (= (mapv sha [1 2]) (:dependencies record) (:evaluated-children record)))
    (is (= #{:memory/allocate} (:direct normal)))
    (is (= #{:error/throw :memory/allocate :compiler/read-ir} (:transitive normal)))
    (is (nil? (:allocation quote-record)))
    (is (= #{} (:direct quote-record) (:transitive quote-record))))
  (doseq [attributes [{:literal-kind :unknown :element-count 2} {:literal-kind :map :element-count 3}
                      {:literal-kind :map :element-count 1} {:literal-kind :vector :element-count nil}]]
    (is (false? (leaf 'body-node-supported? (assoc (node 3 :collection-literal [1 2]) :attributes attributes)))))
  (is (false? (leaf 'body-node-supported? (assoc (node 3 :collection-literal [1 2 4])
                                               :attributes {:literal-kind :map :element-count 3}))))
  (let [literal (node 1 :literal []) key (node 2 :literal [])
        inner (assoc (node 3 :collection-literal [1]) :attributes {:literal-kind :vector :element-count 1})
        outer (assoc (node 4 :collection-literal [2 3]) :attributes {:literal-kind :map :element-count 2})
        creation (node 5 :fn [4]) definition (node 6 :def [5])
        operator (node 7 :reference []) call (node 8 :call [7])
        core {:module {:effects [:memory/allocate]} :nodes [literal key inner outer creation definition operator call]
              :function-records [{:function-syntax-id (sha 20) :function-core-node-id (sha 5)}]
              :root-core-node-ids [(sha 6)] :call-edges [{:call-core-node-id (sha 8) :callee-function-syntax-id (sha 20)}]}
        facts {:module (:module core) :function-type-table [{:function-id (sha 20) :body-core-node-id (sha 4) :latent-effects [:memory/allocate]}]
               :type-table (zipmap (map sha (range 1 9)) (repeat :value))}
        result (leaf 'body-derive-nodes core facts) graph (leaf 'body-build-graph core facts (:nodes result))
        function-id (:function-id (first (:function-type-table facts)))]
    (is (= :accepted (:status result)))
    (is (= #{:memory/allocate} (get-in graph [:nodes (sha 3) :direct]) (get-in graph [:nodes (sha 4) :direct])))
    (is (= #{} (get-in graph [:nodes (sha 5) :direct]) (get-in graph [:nodes (sha 5) :transitive])))
    (is (= #{:memory/allocate} (get-in graph [:nodes (sha 5) :latent]) (get-in graph [:nodes (sha 8) :transitive])))
    (is (= #{:memory/allocate} (get-in graph [:functions function-id :inferred]) (get-in graph [:functions function-id :latent])))
    (is (= #{} (get-in graph [:namespace :inferred])))
    (is (= :accepted (:status (leaf 'body-allowance-check core facts graph))))
    (is (= "C8-UNDECLARED" (:rule (leaf 'body-allowance-check core
                                   (assoc-in facts [:function-type-table 0 :latent-effects] [])
                                   (assoc-in graph [:functions (sha 20) :declared] #{})))))
    (let [namespace-core (assoc core :function-records [])]
      (is (= "C8-UNDECLARED" (:rule (leaf 'body-allowance-check namespace-core (assoc-in facts [:module :effects] []) graph)))))))

(deftest body-effect-operation-development-kernel
  ;; Actual Gravity algorithm, normalized graph DATA; no authentication claim.
  (let [literal (node 1 :literal []) fn-node (node 2 :fn [1])
        quote-node (assoc (node 3 :quote []) :attributes {:quoted-value '(throw unresolved/data)})
        reference (node 4 :reference []) call (node 5 :call [4])
        core {:nodes [call quote-node fn-node reference literal]
              :call-edges [{:call-core-node-id (sha 5) :callee-function-syntax-id (sha 20)}]}
        typed {:function-type-table [{:function-id (sha 20) :body-core-node-id (sha 1)}] :type-table {}}
        result (leaf 'body-derive-nodes core typed)]
    (is (= :accepted (:status result)))
    (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes result)))))
    (is (= (sha 1) (get-in result [:nodes (sha 5) :call-body-dependency])))
    (is (= [] (get-in result [:nodes (sha 2) :evaluated-children])))
    (is (= [(sha 1)] (get-in result [:nodes (sha 2) :latent-children])))
    (is (= [] (get-in result [:nodes (sha 3) :dependencies])))
    (let [effectful {(sha 1) {:transitive #{:compiler/read-ir}}}
          construction (leaf 'body-node-record fn-node nil effectful :function)]
      (is (= #{} (:transitive construction)))
      (is (= #{:compiler/read-ir} (:latent construction)))
      (is (= #{:compiler/read-ir}
             (:transitive (leaf 'body-node-record call (sha 1) (assoc effectful (sha 4) {:transitive #{}}) :value)))))
    (doseq [operation [:throw :try :recur :set! :unknown]]
      (is (= :unsupported-body-operation
             (:reason (leaf 'body-derive-nodes {:nodes [(node 1 operation [])] :call-edges []} typed)))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes (update core :nodes #(filterv (fn [n] (not= (sha 1) (:node-id n))) %)) typed))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes core (assoc-in typed [:function-type-table 0 :body-core-node-id] (sha 5))))))))

(deftest body-effect-coverage-development-kernel
  (let [module {:namespace 'example.body :profile :meta :target :jvm :effects [] :capabilities []}
        nodes [(node 1 :literal []) (node 2 :fn [1]) (node 3 :def [2])]
        record {:function-syntax-id (sha 12) :function-core-node-id (sha 2)}
        core {:artifact-id (sha 30) :module module :nodes nodes :root-core-node-ids [(sha 3)]
              :definitions [] :function-records [record] :call-edges [] :calls [] :recursion-components []
              :lexical-bindings [] :match-branch-records []
              :evaluation-order [{:core-node-id (sha 3) :index 0}]}
        seal (fn [c] (assoc c :identity-preimage (select-keys c [:nodes :root-core-node-ids :definitions :function-records
                                                              :call-edges :calls :lexical-bindings :match-branch-records :evaluation-order])))
        core (seal core)
        typed {:module module :sh07-shaped-artifact-id (sha 30)
               :function-type-table [{:function-id (sha 12) :parameters [] :parameter-binding-ids []
                                      :latent-effects [] :capabilities []}]
               :type-table {(sha 1) :int (sha 2) :fn (sha 3) :fn}
               :call-type-facts [] :local-binding-facts []
               :function-records (:function-records core) :call-edges [] :recursion-components [] :lexical-bindings []}]
    (is (true? (leaf 'body-shape-valid? core typed)))
    (is (false? (leaf 'body-shape-valid? core (update typed :type-table #(-> % (dissoc (sha 1)) (assoc (sha 99) :int))))))
    (is (false? (leaf 'body-shape-valid? (seal (update core :nodes conj (node 9 :literal [])))
                     (assoc-in typed [:type-table (sha 9)] :int))))
    (is (false? (leaf 'body-shape-valid? (seal (assoc core :root-core-node-ids [])) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:nodes 1 :children] 7) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:nodes 1 :evaluation :order] 7) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc core :evaluation-order []) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc-in core [:evaluation-order 0 :core-node-id] (sha 999)) typed)))
    (is (false? (leaf 'body-shape-valid? (assoc core :evaluation-order 7) typed)))
    (is (false? (leaf 'body-function-joins-valid? core (assoc-in typed [:type-table (sha 2)] 7) {})))
    (is (false? (leaf 'body-call-valid? {} {} {:argument-types 7} {} {} {} {} {})))
    (is (false? (leaf 'body-shape-valid? (seal (assoc core :function-records []))
                     (-> typed (assoc :function-type-table []) (assoc :function-records [])))))))

(deftest body-effect-guard-development-kernel
  (let [match-node (assoc (node 1 :match [2 3 4 5])
                         :attributes {:branch-count 2 :branch-child-indexes [1 2] :guard-child-indexes [3]})
        branches [{:core-node-id (sha 1) :clause-ordinal 0 :branch-child-index 1 :branch-core-node-id (sha 3)
                   :scrutinee-core-node-id (sha 2) :guard {:node-id (sha 5) :child-index 3}}
                  {:core-node-id (sha 1) :clause-ordinal 1 :branch-child-index 2 :branch-core-node-id (sha 4)
                   :scrutinee-core-node-id (sha 2)}]
        core {:match-branch-records branches
              :match-decision-skeletons [{:core-node-id (sha 1) :scrutinee-core-node-id (sha 2)
                                          :branch-core-node-ids [(sha 3) (sha 4)]}]}
        types {(sha 5) :gravity.type/bool}
        match-node (assoc match-node :evaluation {:kind :scrutinee-then-source-ordered-pattern-candidates
                                                  :order [{:index 0 :core-node-id (sha 2)}]})]
    (is (true? (leaf 'body-match-valid? match-node core types)))
    (is (true? (leaf 'body-match-valid? match-node core (assoc types (sha 5) :gravity.type/never))))
    (is (false? (leaf 'body-match-valid? (update match-node :children pop) core types)))
    (is (false? (leaf 'body-match-valid? match-node core (assoc types (sha 5) :gravity.type/integer))))
    (let [graph (zipmap (map sha [2 3 4 5]) (repeat {:transitive #{}}))
          record (leaf 'body-node-record match-node nil graph :value)
          conditional (assoc (node 6 :if [2 3 4]) :evaluation {:kind :condition-then-exclusive-branch
                                                            :order [{:index 0 :core-node-id (sha 2)}]})
          if-record (leaf 'body-node-record conditional nil graph :value)]
      (is (= [(sha 2)] (:evaluated-children record) (:evaluated-children if-record)))
      (is (= [:branch :branch :guard] (mapv :role (:conditional-coverage record))))
      (is (= [:then :else] (mapv :role (:conditional-coverage if-record))))
      (is (= (mapv sha [3 4 5]) (mapv :node-id (:conditional-coverage record)))))
    (let [graph {(sha 2) {:transitive #{}}
                 (sha 3) {:transitive #{:error/throw} :escaping-thrown-error-types [:gravity.type/bool] :escaping-thrown-errors #{:gravity.type/bool}}
                 (sha 4) {:transitive #{}}
                 (sha 5) {:transitive #{:error/throw} :escaping-thrown-error-types [:gravity.type/string] :escaping-thrown-errors #{:gravity.type/string}}}
          record (leaf 'body-node-record match-node nil graph :gravity.type/never)]
      (is (= (mapv sha [2 3 4 5]) (:dependencies record)))
      (is (= [:gravity.type/bool :gravity.type/string] (:escaping-thrown-error-types record)))
      (is (= #{:error/throw} (:transitive record))))
    (is (false? (leaf 'body-match-valid? match-node (update-in core [:match-branch-records 0] dissoc :guard) types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] []) core types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] [2]) core types)))
    (is (false? (leaf 'body-match-valid? (assoc-in match-node [:attributes :guard-child-indexes] 7) core types)))))

(deftest body-effect-final-input-fences-development-kernel
  (let [callable {:artifact :gravity/c7-function-type :parameters [] :return :gravity.type/integer}
        collection {:kind :gravity.type/vector :element-types [:gravity.type/integer :gravity.type/bool] :shape :literal}
        function {:parameters [{:binding-id (sha 1) :type :gravity.type/bool}] :parameter-binding-ids [(sha 1)]
                  :return collection :latent-effects [:compiler/read-ir] :capabilities [:compiler/ir-read]}]
    (doseq [type [:gravity.type/bool :gravity.type/integer :gravity.type/string :gravity.type/symbol collection
                 {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/symbol] :shape :literal}
                 {:kind :gravity.type/map :entries [{:key :payload :value-type collection}]}]]
      (is (true? (leaf 'body-first-order-value-type? type))))
    (doseq [type [callable {:kind :gravity.type/vector :element-types [callable]}
                 {:kind :gravity.type/map :entries [{:key :payload :value-type callable}]}
                 {:kind :union :members [collection callable]}]]
      (is (false? (leaf 'body-first-order-value-type? type))))
    (is (true? (leaf 'body-function-shapes? [function])))
    (is (false? (leaf 'body-function-shapes? [(assoc function :return callable)])))
    (is (false? (leaf 'body-function-shapes? [(assoc-in function [:parameters 0 :type] {:kind :list :members [callable]})])))
    (let [typed {:module {:namespace 'example.body :profile :meta :target :jvm}}
          known (assoc-in (node 1 :throw []) [:evaluation :owner-function-syntax-id] (sha 77))]
      (is (= (sha 77) (get-in (leaf 'body-error typed :unsupported-body-operation known) [:diagnostics 0 :function])))
      (is (nil? (get-in (leaf 'body-error typed :unsupported-body-operation (node 1 :throw [])) [:diagnostics 0 :function])))
      (is (= 'example.body (get-in (leaf 'body-error typed :unsupported-body-operation known) [:diagnostics 0 :namespace]))))))

(deftest body-effect-call-metadata-development-kernel
  (let [reference (assoc (node 1 :reference []) :attributes {:binding-id (sha 50)})
        argument (node 2 :literal []) call-node (node 3 :call [1 2]) source (:source call-node)
        target {:function-id (sha 20) :parameters [{:binding-id (sha 51) :type :gravity.type/bool}]
                :return :gravity.type/bool :latent-effects [] :capabilities [] :profile :meta :target :jvm}
        record {:definition-binding-id (sha 50) :function-core-node-id (sha 10)}
        edge {:classification :local-function :call-core-node-id (sha 3)
              :callee-binding-id (sha 50) :callee-definition-syntax-id (sha 60)
              :callee-function-syntax-id (sha 20) :callee-function-core-node-id (sha 10)
              :caller-function-syntax-id nil :caller-function-core-node-id nil
              :argument-core-node-ids [(sha 2)] :ordered-evaluation-node-ids [(sha 1) (sha 2)]
              :evaluation-order :operator-then-arguments}
        call {:core-node-id (sha 3) :operator-binding-id (sha 50) :argument-node-ids [(sha 2)]
              :ordered-evaluation-node-ids [(sha 1) (sha 2)] :evaluation-order :operator-then-arguments}
        fact {:artifact :gravity/c7-call-type-fact :status :inferred :call-core-node-id (sha 3)
              :callee-function-syntax-id (sha 20) :caller-function-syntax-id nil
              :argument-types [:gravity.type/bool] :result-type :gravity.type/bool
              :evaluation-order :operator-then-arguments :syntax-id (:syntax-id source)
              :source-span (:semantic-span source) :origin-chain [] :generated-origin nil
              :effects [] :capabilities [] :profile :meta :target :jvm
              :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id (sha 3)
                                :callee-function-syntax-id (sha 20) :argument-types [:gravity.type/bool] :result-type :gravity.type/bool}}
        run (fn [f] (leaf 'body-call-valid? edge call f
                          {(sha 1) reference (sha 2) argument (sha 3) call-node}
                          {(sha 20) target} {(sha 20) record} {(sha 50) {:definition-syntax-id (sha 60)}}
                          {(sha 2) :gravity.type/bool (sha 3) :gravity.type/bool}))]
    (is (true? (run fact)))
    (is (false? (run (assoc fact :profile :hosted))))
    (is (false? (run (assoc fact :target :javascript))))
    (is (false? (run (assoc fact :argument-types 7))))
    (let [coordinate {:binding-id (sha 50) :producer-sh06-artifact-id (sha 60)
                      :producer-sh07-artifact-id (sha 61) :definition-binding-id (sha 70) :function-id (sha 20)}
          imported {:coordinate coordinate :binding {:definition-syntax-id (sha 60)}
                    :function (assoc target :body-core-node-id (sha 80)) :body-core-node-id (sha 80)
                    :summary {:function-id (sha 20) :body-core-node-id (sha 80)
                              :inferred #{} :throws [:pending-sh09]}}
          imported-edge (assoc edge :classification :nonlocal-or-nonfunction
                                    :callee-function-syntax-id nil :callee-function-core-node-id nil)
          imported-fact (-> fact (assoc :callee-function-syntax-id nil :dispatch :direct-imported-function
                                        :imported-function coordinate :effect-status :pending-sh09-imported-body
                                        :legality-status :pending-imported-effects :effect-facts-source :callee-module-declarations)
                            (update :fact-id-request assoc :callee-function-syntax-id nil :imported-function coordinate
                                    :effect-status :pending-sh09-imported-body :legality-status :pending-imported-effects))
          run-import (fn [f foreign]
                       (leaf 'body-import-call-valid? imported-edge call f
                             {(sha 1) reference (sha 2) argument (sha 3) call-node} {} foreign
                             {(sha 2) :gravity.type/bool (sha 3) :gravity.type/bool}))]
      (is (true? (run-import imported-fact imported)))
      (doseq [f [(assoc imported-fact :callee-function-syntax-id (sha 20))
                 (assoc-in imported-fact [:imported-function :producer-sh06-artifact-id] (sha 99))
                 (assoc-in imported-fact [:fact-id-request :callee-function-syntax-id] (sha 20))
                 (assoc imported-fact :argument-types 7)
                 (assoc imported-fact :profile :hosted) (assoc imported-fact :target :native)]]
        (is (false? (run-import f imported))))
      (doseq [foreign [(assoc-in imported [:summary :body-core-node-id] (sha 99))
                       (assoc-in imported [:summary :function-id] (sha 99))
                       (assoc-in imported [:summary :inferred] [])
                       (assoc-in imported [:summary :throws] [])]]
        (is (false? (run-import imported-fact foreign)))))))

(deftest body-effect-import-origin-development-kernel
  ;; Normalized retained C5/C6 data and flat C7 facts; no accepted producer wrapper.
  (let [module {:namespace 'producer.values :package nil :profile :meta :target :jvm :exports ['ready]}
        original {:binding-id (sha 1) :name 'ready :kind :function :namespace 'producer.values :package nil
                  :binding-class :namespace :visibility :public :definition-artifact-id (sha 2) :definition-syntax-id (sha 3)}
        origin {:ordinary-module-artifact-id (sha 4) :origin-binding-id (sha 1) :origin-analysis-id (sha 5)
                :origin-definition-artifact-id (sha 2) :origin-definition-syntax-id (sha 3)
                :origin-definition (into {} (map (fn [k] [k (get original k)])
                     [:name :kind :namespace :package :visibility :profile-set :target-set :type-ref :effects :capabilities :safety]))}
        binding {:binding-id (sha 6) :upstream-binding-id (sha 7) :name 'ready :namespace 'producer.values
                 :package nil :visibility :public :binding-class :import :semantic-span origin}
        caller-c5 {:binding-table {:bindings [(assoc binding :binding-id (sha 7))]}}
        producer-c5 {:artifact-id (sha 4) :binding-table {:bindings [original]}
                     :gravity-resolution-boundary {:resolved-analysis {:artifact-id (sha 5) :module-contract module}}}
        function {:artifact :gravity/c7-function-type :function-id (sha 10) :body-core-node-id (sha 11)
                  :function-core-node-id (sha 12) :definition-name 'ready :parameter-binding-ids [] :parameters []
                  :return :gravity.type/bool :fixed-arity 0 :signature nil :latent-effects [] :capabilities []
                  :ownership-constraints [] :profile-constraints [] :thrown-error-effects [:pending-sh09] :status :inferred}
        entry {:producer-sh06-artifact-id (sha 4) :producer-sh07-artifact-id (sha 8) :module-key [(sha 4) (sha 8)]
               :request {:canonical-core-artifact {:function-records [{:definition-binding-id (sha 9) :function-syntax-id (sha 10)}]}
                         :b47-context {:authenticated-wrapper {:sh06-resolution-artifact producer-c5}
                                       :authenticated-core-request {:binding-table [{:binding-id (sha 9) :upstream-binding-id (sha 1)}]}}}
               :facts {:function-type-table [function]}}
        request {:canonical-core-artifact {:module module}
                 :b47-context {:authenticated-wrapper {:sh06-resolution-artifact caller-c5}
                               :authenticated-core-request {:binding-table [binding]}}}
        proof {:status :accepted :binding-id (sha 6) :origin origin :producer-sh06-artifact-id (sha 4)
               :producer-sh07-artifact-id (sha 8) :definition-binding-id (sha 9) :function-type function
               :effect-status :pending-sh09-imported-body :legality-status :pending-imported-effects}
        joined (leaf 'body-import-origin request binding entry proof)]
    (is (= (sha 11) (:body-core-node-id joined)))
    (is (= (leaf 'body-import-coordinate proof) (:coordinate joined)))
    (let [callable-target (assoc function :fixed-arity 1 :parameter-binding-ids [(sha 90)]
                                         :parameters [{:binding-id (sha 90) :type {:artifact :gravity/c7-function-type}}])]
      (is (nil? (leaf 'body-import-origin request binding
                       (assoc-in entry [:facts :function-type-table] [callable-target])
                       (assoc proof :function-type callable-target)))))
    (doseq [p [(assoc proof :origin {}) (assoc proof :producer-sh06-artifact-id (sha 99))
               (assoc proof :producer-sh07-artifact-id (sha 99)) (assoc proof :definition-binding-id (sha 99))
               (assoc-in proof [:function-type :body-core-node-id] (sha 99))
               (assoc-in proof [:function-type :function-id] (sha 99))]]
      (is (nil? (leaf 'body-import-origin request binding entry p))))
    (doseq [e [(assoc-in entry [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :binding-table :bindings 0 :visibility] :private)
               (assoc-in entry [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :gravity-resolution-boundary :resolved-analysis :module-contract :exports] [])
               (assoc-in entry [:request :b47-context :authenticated-core-request :binding-table 0 :upstream-binding-id] (sha 99))]]
      (is (nil? (leaf 'body-import-origin request binding e proof))))
    (let [key (:module-key entry)
          bank {key (assoc entry :graph {:functions {(sha 10) {:function-id (sha 10) :body-core-node-id (sha 11)
                                                             :inferred #{} :throws [:pending-sh09]}}})}
          facts {:imported-function-types [proof]}]
      (is (= :accepted (:status (leaf 'body-foreign-bindings request facts bank))))
      (doseq [bad [(assoc-in bank [key :graph :functions (sha 10) :body-core-node-id] (sha 99))
                   (assoc-in bank [key :graph :functions (sha 10) :throws] []) {}]]
        (is (= :rejected (:status (leaf 'body-foreign-bindings request facts bad))))))))

(deftest body-effect-import-bounds-development-kernel
  (is (true? (leaf 'body-import-tables-valid? {:module-type-producers [] :imported-function-types []})))
  (doseq [field [:module-type-producers :imported-function-types]
          malformed [7 nil {} [7] [nil] (vec (repeat 257 {}))]]
    (is (false? (leaf 'body-import-tables-valid?
                     (assoc {:module-type-producers [] :imported-function-types []} field malformed)))))
  (let [entry {:request {:canonical-core-artifact {:nodes []}} :facts {:function-type-table []}}]
    (is (true? (leaf 'body-module-vectors-valid? {:nodes []} {:function-type-table []} [])))
    (is (false? (leaf 'body-module-vectors-valid? {:nodes nil} {:function-type-table []} [])))
    (is (false? (leaf 'body-module-vectors-valid? {:nodes []} {:function-type-table nil} [])))
    (is (false? (leaf 'body-module-vectors-valid? {:nodes []} {:function-type-table []}
                      [(assoc-in entry [:request :canonical-core-artifact :nodes] nil)])))))

(deftest body-effect-foreign-summary-development-kernel
  (let [coordinate {:binding-id (sha 50) :producer-sh06-artifact-id (sha 60)
                    :producer-sh07-artifact-id (sha 61) :definition-binding-id (sha 70) :function-id (sha 20)}
        imported {:coordinate coordinate :body-core-node-id (sha 1)
                  :function {:latent-effects [:declared-only]}
                  :summary {:inferred #{:compiler/read-ir} :throws [:pending-sh09]}}
        core {:nodes [(node 3 :call [1]) (node 1 :reference [])]
              :call-edges [{:call-core-node-id (sha 3) :callee-binding-id (sha 50)}]}
        result (leaf 'body-derive-nodes-with-foreign core {:function-type-table [] :type-table {}} {(sha 50) imported})
        call (get-in result [:nodes (sha 3)])]
    (is (= :accepted (:status result)))
    (is (= #{:compiler/read-ir} (:transitive call)))
    (is (= [(sha 1)] (:dependencies call)))
    (is (nil? (:call-body-dependency call)))
    (is (= (assoc coordinate :body-core-node-id (sha 1)) (:foreign-body-dependency call)))
    (let [other (assoc coordinate :producer-sh06-artifact-id (sha 62))
          a [(:producer-sh06-artifact-id coordinate) (:producer-sh07-artifact-id coordinate)]
          b [(:producer-sh06-artifact-id other) (:producer-sh07-artifact-id other)]
          graphs (leaf 'body-producer-graph-identities
                       {a {:facts {:module :a} :graph {:functions {(sha 20) {:inferred #{:a}}}}}
                        b {:facts {:module :b} :graph {:functions {(sha 20) {:inferred #{:b}}}}}})]
      (is (= 2 (count graphs)))
      (is (= #{:a} (get-in graphs [a :effect-graph :functions (sha 20) :inferred])))
      (is (= #{:b} (get-in graphs [b :effect-graph :functions (sha 20) :inferred]))))))

(defn- recursive-kernel-data []
  (let [owner (fn [n] (assoc-in n [:evaluation :owner-function-syntax-id] (sha 20)))
        nodes [(owner (assoc (node 1 :literal []) :attributes {:literal-kind :boolean :value false}))
               (owner (assoc (node 2 :reference []) :attributes {:binding-id (sha 50)}))
               (owner (assoc (node 3 :reference []) :attributes {:binding-id (sha 51)}))
               (owner (node 4 :call [2 3]))
               (owner (assoc (node 5 :literal []) :attributes {:literal-kind :integer :value 0}))
               (owner (assoc (node 6 :if [1 4 5]) :evaluation
                        {:kind :condition-then-exclusive-branch :owner-function-syntax-id (sha 20)
                         :order [{:index 0 :core-node-id (sha 1)}]}))
               (node 7 :fn [6]) (node 8 :def [7])
               (assoc (node 9 :reference []) :attributes {:binding-id (sha 50)})
               (assoc (node 10 :literal []) :attributes {:literal-kind :integer :value 7}) (node 11 :call [9 10])]
        module {:namespace 'example.recursive :profile :meta :target :jvm :effects [] :capabilities []}
        edge {:ordinal 0 :call-core-node-id (sha 4) :caller-function-syntax-id (sha 20) :caller-function-core-node-id (sha 7)
              :callee-binding-id (sha 50) :callee-definition-syntax-id (sha 80) :callee-function-syntax-id (sha 20)
              :callee-function-core-node-id (sha 7) :argument-core-node-ids [(sha 3)]
              :ordered-evaluation-node-ids [(sha 2) (sha 3)] :evaluation-order :operator-then-arguments :classification :local-function}
        external-edge (assoc edge :ordinal 1 :call-core-node-id (sha 11) :caller-function-syntax-id nil
                             :caller-function-core-node-id nil :argument-core-node-ids [(sha 10)]
                             :ordered-evaluation-node-ids [(sha 9) (sha 10)])
        call {:core-node-id (sha 4) :argument-node-ids [(sha 3)]}
        external-call {:core-node-id (sha 11) :argument-node-ids [(sha 10)]
                       :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments}
        source (:source (get nodes 3)) external-source (:source (get nodes 10))
        proof {:domain :gravity/sh08-authoritative-recursive-proof-v1 :function-syntax-id (sha 20)
               :function-core-node-id (sha 7) :definition-binding-id (sha 50) :definition-syntax-id (sha 80)
               :parameter-binding-ids [(sha 51)] :fixed-arity 1 :recursive-call-core-node-id (sha 4)
               :recursive-argument-node-ids [(sha 3)] :recursive-edge edge
               :recursive-call-source {:syntax-id (:syntax-id source) :source-span (:semantic-span source)
                                       :origin-chain [] :generated-origin nil}
               :recursive-call-profile :meta :recursive-call-target :jvm :recursive-call-effects [] :recursive-call-capabilities []
               :base-node-id (sha 5) :base-type :gravity.type/integer :recursive-definition-binding-id (sha 50)
               :parameter-evidence-call-core-node-id (sha 11)
               :parameter-evidence-call {:call-core-node-id (sha 11) :caller-function-syntax-id nil :callee-function-syntax-id (sha 20)
                 :argument-node-ids [(sha 10)] :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments
                 :argument-types [:gravity.type/integer] :result-type :gravity.type/integer
                 :syntax-id (:syntax-id external-source) :source-span (:semantic-span external-source) :origin-chain [] :generated-origin nil
                 :profile :meta :target :jvm :effects [] :capabilities [] :edge external-edge}
               :parameter-types [:gravity.type/integer]
               :proof-bound :one-named-self-recursive-positive-fixed-arity-positional-literal-base
               :captures? false :higher-order? false :monotone-fixed-point? true}
        core {:nodes nodes :module module :call-edges [edge external-edge] :calls [call external-call]
              :definitions [{:binding-id (sha 50) :syntax-id (sha 80)}]
              :function-records [{:function-syntax-id (sha 20) :function-core-node-id (sha 7) :body-core-node-id (sha 6)
                                  :definition-binding-id (sha 50) :definition-kind :named-top-level :fixed-arity 1
                                  :parameter-binding-ids [(sha 51)]}]
              :recursion-components [{:ordinal 0 :kind :self-recursive :function-syntax-ids [(sha 20)]
                                       :function-core-node-ids [(sha 7)] :internal-call-edge-ordinals [0]}]}
        facts {:recursive-proof proof :function-type-table [{:function-id (sha 20) :body-core-node-id (sha 6)
                  :parameters [{:binding-id (sha 51) :type :gravity.type/integer}] :return :gravity.type/integer}]
               :type-table {(sha 1) :gravity.type/bool} :call-type-facts []}]
    [core facts]))

(defn- private-self-throw-data [data]
  (require 'clojure.walk 'gravity.self-hosting.sh08-recursive-function-type-test)
  (let [make-node (ns-resolve 'gravity.self-hosting.sh08-recursive-function-type-test 'node)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        make (fn [id kind children attrs] (assoc (make-node id kind children attrs :helper-form) :source (source id)))
        additions [(make :argument-condition :literal [] {:literal-kind :boolean :value true})
                   (make :argument-payload :literal [] {:literal-kind :string :value "deep"})
                   (make :argument-transfer :throw [:argument-payload] {})
                   (make :argument-normal :reference [] {:symbol 'span :position :expression :binding-id :span-binding
                                                          :binding-class :lexical :definition-syntax-id nil})
                   (make :self-argument-if :if [:argument-condition :argument-transfer :argument-normal] {})]
        core (-> (:core data)
                 (update :function-records #(vector (first %)))
                 (assoc-in [:module :exports] [])
                 (assoc-in [:module :effects] [:error/throw])
                 (update :nodes #(into additions %))
                 (update :nodes #(mapv (fn [n]
                                         (let [n (if (= :self-call (:node-id n))
                                                   (assoc n :children [:self-op :self-argument-if]) n)
                                               owner (get-in n [:evaluation :owner-function-syntax-id])
                                               owner (when-not (contains? #{:caller-0 :caller-1} owner) owner)
                                               children (:children n) kind (:core-form n)
                                               ordered (cond (= kind :fn) [] (= kind :if) [(first children)] :else children)]
                                           (assoc n :evaluation
                                                  {:owner-function-syntax-id owner
                                                   :kind (case kind :call :operator-then-arguments :if :condition-then-exclusive-branch :left-to-right)
                                                   :order (mapv (fn [i id] {:index i :core-node-id id}) (range) ordered)}))) %))
                 (update :calls #(mapv (fn [c] (if (= :self-call (:core-node-id c))
                                                 (assoc c :argument-node-ids [:self-argument-if]
                                                          :ordered-evaluation-node-ids [:self-op :self-argument-if]) c)) %))
                 (update :call-edges #(mapv (fn [e] (if (= :self-call (:call-core-node-id e))
                                                      (assoc e :argument-core-node-ids [:self-argument-if]
                                                               :ordered-evaluation-node-ids [:self-op :self-argument-if])
                                                      (assoc e :caller-function-syntax-id nil :caller-function-core-node-id nil))) %)))
        forms (mapv #(if (= :self-call (:form-id %)) (assoc % :child-form-ids [:self-op :self-argument-if]) %) (:forms data))
        forms (conj forms {:form-id :self-argument-if :syntax-id :self-argument-if :kind :list :parent-form-id :self-call
                           :source-span nil :origin-chain [] :generated-origin nil
                           :child-form-ids [:argument-condition :argument-transfer :argument-normal]})
        ids (into {} (map-indexed (fn [i n] [(:node-id n) (sha (+ 30000 i))]) (:nodes core)))
        renamed (clojure.walk/postwalk-replace ids (assoc data :core core :forms forms))]
    (assoc renamed :node-id-map ids)))

(deftest current-gravity-set-conversion-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'set-conversion-input) derive (ns-resolve upstream 'set-conversion-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [type [{:kind :gravity.type/vector :shape :literal :element-types []}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 3 :gravity.type/string))}
                  :gravity.type/never]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-st-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-st-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-st-context request facts bindings)
            checked (assoc facts :set-conversion-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            invoked? (not= :gravity.type/never type)
            operand-effects (if invoked? #{:io/write} #{:error/throw})
            graph {:operator {:type descriptor :transitive #{}} :arg0 {:type type :transitive operand-effects}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-st-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-st-node? node checked core)))
        (is (= (if invoked? #{:memory/allocate} #{}) (:direct record)))
        (is (= (if invoked? #{:memory/allocate :io/write} #{:error/throw}) (:transitive record)))
        (is (= invoked? (get-in record [:allocation :normal-completion-possible?])))
        (is (= (when invoked? :runtime-value-dependent) (get-in record [:allocation :extent :result-cardinality])))
        (is (= (:input-element-count descriptor) (get-in record [:allocation :extent :upper-bound])))
        (when invoked?
          (is (= :all-string-members-before-hash
                 (get-in descriptor [:construction-policy :validation]))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:io/write])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc-in descriptor [:construction-policy :order] :input-order)
                         (assoc-in descriptor [:construction-policy :validation] :first-member-before-hash)
                         (assoc descriptor :input-element-count 123)
                         (assoc descriptor :direct-effects [:io/write])]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-st-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-st-constraint [changed])]))]
            (is (false? (valid forged)))))
        (is (= :rejected (:status (leaf 'body-st-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7) facts bindings))))
        (is (= :rejected (:status (leaf 'body-st-context request (assoc-in facts [:type-table :arg0] :gravity.type/unknown) bindings))))))))

(deftest current-gravity-keyword-vector-set-conversion-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'set-conversion-input) derive (ns-resolve upstream 'set-conversion-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [type [{:kind :gravity.type/vector :shape :literal :element-types []}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 3 :gravity.type/keyword))}
                  :gravity.type/never]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-st-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-st-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-st-context request facts bindings)
            checked (assoc facts :set-conversion-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            invoked? (not= :gravity.type/never type)
            operand-effects (if invoked? #{:io/write} #{:error/throw})
            graph {:operator {:type descriptor :transitive #{}} :arg0 {:type type :transitive operand-effects}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-st-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-st-node? node checked core)))
        (is (= (if invoked? #{:memory/allocate} #{}) (:direct record)))
        (is (= (if invoked? #{:memory/allocate :io/write} #{:error/throw}) (:transitive record)))
        (is (= invoked? (get-in record [:allocation :normal-completion-possible?])))
        (is (= (get-in descriptor [:construction-policy :operation]) (get-in record [:allocation :behavior])))
        (is (= (when invoked? :runtime-value-dependent) (get-in record [:allocation :extent :result-cardinality])))
        (is (= (:input-element-count descriptor) (get-in record [:allocation :extent :upper-bound])))
        (when invoked?
          (is (= :all-keyword-members-before-print-compare-hash-or-insert
                 (get-in descriptor [:construction-policy :validation])))
          (is (not (contains? (get descriptor :input-policy) :maximum-input-elements))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:io/write])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc-in descriptor [:construction-policy :order] :input-order)
                         (assoc-in descriptor [:construction-policy :validation] :first-member-before-hash)
                         (assoc descriptor :input-element-count 123)
                         (assoc descriptor :direct-effects [:io/write])
                         (assoc descriptor :version :forged)
                         (assoc-in descriptor [:input-policy :version] :forged)
                         (assoc-in descriptor [:input-policy :output-refinements] :literal-members)
                         (assoc-in descriptor [:construction-policy :duplicates] :printed-spelling)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-st-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-st-constraint [changed])]))]
            (is (false? (valid forged)))))
        (is (= :rejected (:status (leaf 'body-st-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7) facts bindings))))
        (is (= :rejected (:status (leaf 'body-st-context request (assoc-in facts [:type-table :arg0] :gravity.type/unknown) bindings))))))))

(deftest current-gravity-keyword-vector-set-conversion-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base (-> ((ns-resolve upstream 'set-conversion-input) [{:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/keyword :gravity.type/keyword]}])
                 (assoc-in [:module :effects] [:memory/allocate])
                 (assoc-in [:core :module :effects] [:memory/allocate]))
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'set-conversion-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-st-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-st-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-keyword-set-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs ((ns-resolve upstream 'keyword-set-inequality-type-pairs))]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-keyword-set-inequality-v1 (get-in descriptor [:keyword-set-equality :version])))
        (is (= (:keyword-set-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :keyword-set-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :domain-validation] :cardinality-first)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :keyword-set-equality)
                         (assoc-in descriptor [:keyword-set-equality :version] :forged)
                         (assoc-in descriptor [:keyword-set-equality :operand-counts] [])
                         (assoc-in descriptor [:keyword-set-equality :equality-policy :domain-validation] :first-mismatch)
                         (assoc-in descriptor [:keyword-set-equality :equality-policy :member-uniqueness] :assumed)
                         (assoc-in descriptor [:keyword-set-equality :equality-policy :order] :enumeration)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))


(deftest current-gravity-keyword-set-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [((ns-resolve upstream 'inequality-keyword-set-type) (repeat 32 :gravity.type/keyword)) ((ns-resolve upstream 'inequality-keyword-set-type) [])])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (is (= #{} (get-in analyzed [:graph :namespace :inferred])))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-set-count-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'integral-ordering-input) c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/set
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/integer]}
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/unknown]}]]
      (let [{:keys [core node call edge nodes bindings types forms module]} (input-fn 'count [type])
            result (c7 'sh08-cq-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-cq-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-cq-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-cq-context request facts bindings)
            checked (assoc facts :count-context (:context context))]
        (is (= :accepted (:status context)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (= (c7 'sh08-cq-finish [(c7 'sh08-cq-source [core node call edge nodes bindings types module])])
               (leaf 'body-cq-finish (leaf 'body-cq-source core node call edge nodes bindings types module))))
        (let [record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}} :arg0 {:type type :transitive #{}}}
                           :gravity.type/integer)]
          (is (= #{} (:direct record) (:transitive record)))
          (is (nil? (:allocation record))))))))

(deftest current-gravity-map-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-predicate-input)
        derive (ns-resolve upstream 'map-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/map :gravity.type/keyword :gravity.type/never {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-mapp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-mapp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-mapp-context request facts bindings)
            checked (assoc facts :map-predicate-context (:context context))
            valid #(leaf 'body-mapp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-mapp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-mapp-node? node checked core)))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-mapp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-mapp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-mapp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-set-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'set-predicate-input)
        derive (ns-resolve upstream 'set-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-setp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-setp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-setp-context request facts bindings)
            checked (assoc facts :set-predicate-context (:context context))
            valid #(leaf 'body-setp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-setp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-setp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-setp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-setp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-setp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-setp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-setp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-symbol-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'symbol-predicate-input)
        derive (ns-resolve upstream 'symbol-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-symp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-symp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-symp-context request facts bindings)
            checked (assoc facts :symbol-predicate-context (:context context))
            valid #(leaf 'body-symp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-symp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-symp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-symp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-symp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-symp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-symp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-symp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-boolean-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'boolean-predicate-input)
        derive (ns-resolve upstream 'boolean-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/bool :gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-boolp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-boolp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-boolp-context request facts bindings)
            checked (assoc facts :boolean-predicate-context (:context context))
            valid #(leaf 'body-boolp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-boolp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-boolp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-boolp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-boolp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-boolp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-boolp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-boolp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-boolean-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'boolean-predicate-input) [:gravity.type/bool])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'boolean-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-boolp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-boolp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-character-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'character-predicate-input)
        derive (ns-resolve upstream 'character-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/character :gravity.type/nil
                  {:kind :gravity.type/nil-or :value-type :gravity.type/bool}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/character}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/string}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/symbol}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}
                  :gravity.type/bool :gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-chp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-chp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-chp-context request facts bindings)
            checked (assoc facts :character-predicate-context (:context context))
            valid #(leaf 'body-chp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-chp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-chp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-chp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-chp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:type-table :operator :character-query-classification :case] :forged)
                         (assoc-in checked [:call-type-facts 0 :character-query-classification :version] :forged)
                         (assoc-in checked [:constraint-ledger 0 :solution :character-query-classification :lowering] :forged)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-chp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-chp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-chp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-character-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'character-predicate-input) [{:kind :gravity.type/nil-or :value-type :gravity.type/keyword}])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'character-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-chp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-chp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-nil-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'nil-predicate-input)
        derive (ns-resolve upstream 'nil-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/nil
                  {:kind :gravity.type/nil-or :value-type :gravity.type/bool}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/character}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/string}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/symbol}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}
                  :gravity.type/bool :gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-nilp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-nilp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-nilp-context request facts bindings)
            checked (assoc facts :nil-predicate-context (:context context))
            valid #(leaf 'body-nilp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-nilp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-nilp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-nilp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-nilp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:type-table :operator :nil-query-classification :case] :forged)
                         (assoc-in checked [:call-type-facts 0 :nil-query-classification :version] :forged)
                         (assoc-in checked [:constraint-ledger 0 :solution :nil-query-classification :lowering] :forged)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-nilp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-nilp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-nilp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-nil-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'nil-predicate-input) [{:kind :gravity.type/nil-or :value-type :gravity.type/keyword}])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'nil-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-nilp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-nilp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-fixed-integral-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs (for [family [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64 :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64] pair [[family family] [family :gravity.type/never] [:gravity.type/never family]]] pair)]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-fixed-integral-inequality-v1 (get-in descriptor [:numeric-equality :version])))
        (is (= (:numeric-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :numeric-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :numeric-equality)
                         (assoc-in descriptor [:numeric-equality :version] :forged)
                         (assoc-in descriptor [:numeric-equality :numeric-mode] :wrapping)
                         (assoc-in descriptor [:numeric-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:numeric-equality :integer-type]
                                   (if (= :gravity.type/i8 (get-in descriptor [:numeric-equality :integer-type])) :gravity.type/u8 :gravity.type/i8))
                         (assoc-in descriptor [:numeric-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest current-gravity-fixed-integral-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [:gravity.type/i8 :gravity.type/i8])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (is (= #{} (get-in analyzed [:graph :namespace :inferred])))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-integer-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs [[:gravity.type/integer :gravity.type/integer] [:gravity.type/integer :gravity.type/never] [:gravity.type/never :gravity.type/integer]]]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-integer-inequality-v1 (get-in descriptor [:numeric-equality :version])))
        (is (= (:numeric-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :numeric-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :numeric-equality)
                         (assoc-in descriptor [:numeric-equality :version] :forged)
                         (assoc-in descriptor [:numeric-equality :numeric-mode] :wrapping)
                         (assoc-in descriptor [:numeric-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:numeric-equality :integer-type] :gravity.type/u64)
                         (assoc-in descriptor [:numeric-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported (if (= other :gravity.type/never) :gravity.type/decimal :gravity.type/i8)]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest current-gravity-integer-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [:gravity.type/integer :gravity.type/integer])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (is (= #{} (get-in analyzed [:graph :namespace :inferred])))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-atomic-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        scalar [:gravity.type/nil :gravity.type/bool :gravity.type/character :gravity.type/string :gravity.type/symbol :gravity.type/keyword]
        nullable (mapv #(hash-map :kind :gravity.type/nil-or :value-type %) (rest scalar))
        pairs (concat (map vector scalar scalar) (map #(vector % :gravity.type/nil) nullable)
                      [[{:kind :gravity.type/nil-or :value-type :gravity.type/string} {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}]
                       [:gravity.type/never :gravity.type/keyword] [:gravity.type/keyword :gravity.type/never]])]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported (if (= other :gravity.type/never) :gravity.type/decimal :gravity.type/integer)]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest current-gravity-atomic-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [{:kind :gravity.type/nil-or :value-type :gravity.type/string} {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (is (= #{} (get-in analyzed [:graph :namespace :inferred])))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-symbol-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'symbol-predicate-input) [:gravity.type/symbol])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'symbol-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-symp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-symp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-set-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'set-predicate-input) [:gravity.type/set])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'set-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-setp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-setp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-keyword-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'keyword-predicate-input)
        derive (ns-resolve upstream 'keyword-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/keyword :gravity.type/nil :gravity.type/map :gravity.type/never
                  {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}
                  {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/keyword]}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-kp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-kp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-kp-context request facts bindings)
            checked (assoc facts :keyword-predicate-context (:context context))
            valid #(leaf 'body-kp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-kp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-kp-node? node checked core)))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.collections)
                         (assoc descriptor :capabilities [:io/stdout])
                         (assoc-in descriptor [:query-semantics :refinement] :keyword)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(invoke 'sh08-kp-call-fact [changed module])]
                                  :constraint-ledger [(invoke 'sh08-kp-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :vector)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-kp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-kp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-kp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-string-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'string-predicate-input)
        derive (ns-resolve upstream 'string-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/string :gravity.type/keyword :gravity.type/map :gravity.type/never
                  {:kind :gravity.type/nil-or :value-type :gravity.type/string}
                  {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/string]}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-strp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-strp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-strp-context request facts bindings)
            checked (assoc facts :string-predicate-context (:context context))
            valid #(leaf 'body-strp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-strp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-strp-node? node checked core)))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc descriptor :semantic-owner 'gravity.core)
                         (assoc descriptor :capabilities [:io/stdout])
                         (assoc-in descriptor [:query-semantics :normalization] :nfc)
                         (assoc-in descriptor [:query-semantics :unicode-boundary] :proven)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(invoke 'sh08-strp-call-fact [changed module])]
                                  :constraint-ledger [(invoke 'sh08-strp-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :vector)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-strp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-strp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-strp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-integer-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'integer-predicate-input)
        derive (ns-resolve upstream 'integer-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/integer :gravity.type/i8 :gravity.type/u64 :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-ip-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-ip-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ip-context request facts bindings)
            checked (assoc facts :integer-predicate-context (:context context))
            valid #(leaf 'body-ip-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ip-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-ip-node? node checked core)))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :vector)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-ip-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-ip-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-ip-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-vector-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'vector-predicate-input)
        derive (ns-resolve upstream 'vector-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-vp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-vp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-vp-context request facts bindings)
            checked (assoc facts :vector-predicate-context (:context context))
            valid #(leaf 'body-vp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-vp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-vp-node? node checked core)))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :vector)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-vp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-vp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-vp-context request {} bindings)]

      (is (= 0 @visits)))))

(defn- private-self-value-kernel-data [kind]
  (require 'gravity.self-hosting.sh08-recursive-function-type-test)
  (let [ns 'gravity.self-hosting.sh08-recursive-function-type-test
        invoke (ns-resolve ns 'invoke-c7)
        indexed? (contains? #{:indexed :indexed-sha} kind)
        data ((ns-resolve ns (if indexed? 'indexed-private-self-data
                                (if (= kind :prior-three) 'private-self-three-parameter-false-base-data
                                  (if (= kind :head) 'private-self-homogeneous-head-data 'private-self-string-proof-data)))))
        data (if (= kind :throw) (private-self-throw-data data) data)
        ;; Rename canonical fixture inputs before deriving proofs and facts.
        ;; Derived schema keys and map-order-dependent source vectors are not IDs.
        data (if (= kind :indexed-sha)
               (let [ids (set (concat (map :node-id (get-in data [:core :nodes]))
                                      (map :form-id (:forms data))
                                      (map :binding-id (:bindings data))
                                      (map :function-syntax-id (get-in data [:core :function-records]))))
                     renamed (into {} (map-indexed (fn [i id] [id (sha (+ 51000 i))])
                                                  (sort-by str (filter keyword? ids))))]
                 (assoc (clojure.walk/postwalk-replace renamed data) :node-id-map renamed)) data)
        {:keys [core bindings forms]} data
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        shape {:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
        proof (:recursive-proof (invoke 'sh08-ft-proof-results [shape (assoc core :node-table nodes) bindings (:module core)]))
        skeletons (invoke 'sh08-ft-function-skeletons [(:function-records core) (:module core)])
        functions (invoke 'sh08-ft-complete-function-skeletons [skeletons proof {}])
        function (get functions (:function-syntax-id proof))
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise proof))
        cold (invoke 'sh08-is-cold-infer [working core (first (:function-records core)) nodes bindings forms {}
                                         :body (:parameter-types proof)])
        return (:inferred-return-type proof)
        completed-types (clojure.walk/postwalk-replace (get data :node-id-map {})
                        (merge
                     {:self-op function :entry-operator-0 function :entry-operator-1 function
                      :self-call return :entry-0 return :entry-1 return :recursive-body return
                      :helper-fn function :helper-def function}
                     (when (= kind :head) {:head-else return})
                     (when indexed? {:ix-inner return})))
        types (merge (get-in cold [:inference :type-table]) completed-types)
        ledger (invoke 'sh08-ft-recursive-constraint-ledger [proof])
        call-facts (invoke 'sh08-ft-call-facts [(:calls core) (:call-edges core) nodes types functions (:module core)])
        facts {:domain (if indexed? :gravity/sh08-indexed-private-self-value-typed-core-v1
                           :gravity/sh08-private-self-value-typed-core-v1)
               :recursive-proof (invoke 'sh08-ft-recursive-proof-input [proof])
               :function-type-table (if indexed?
                                      (invoke 'sh08-ft-function-facts [functions nodes (:module core)])
                                      (vec (vals functions)))
               :type-table types
               :call-type-facts call-facts :constraint-ledger (into ledger (get-in cold [:inference :constraints]))}]
    {:core core :facts facts :bindings (into {} (map (juxt :binding-id identity) bindings))
     :forms forms :nodes nodes :proof proof :cold cold :invoke invoke :data data}))

(deftest current-gravity-private-three-parameter-source-context-development-kernel
  (let [{:keys [core facts bindings forms proof]} (private-self-value-kernel-data :prior-three)
        context (leaf 'body-is-context core facts bindings forms)]
    (is (= :sh08-private-self-value-v1 (:rule-version proof)))

    (is (= :accepted (:status context)) (pr-str context))
    (is (= context (leaf 'body-is-context core facts bindings forms)))))

(deftest current-gravity-indexed-private-source-context-development-kernel
  (let [{:keys [core facts bindings forms nodes proof]} (private-self-value-kernel-data :indexed)
        context (leaf 'body-is-context core facts bindings forms)
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise context))
        request {:canonical-core-artifact working :b47-context {:authenticated-core-request {:forms forms}}}
        successor (leaf 'body-si-context request facts bindings)
        getter (leaf 'body-mg-context request facts bindings)
        equality (leaf 'body-ae-context request facts bindings)
        counted (leaf 'body-cq-context request facts bindings)
        checked (assoc facts :successor-context (:context successor) :map-get-context (:context getter)
                             :equality-context (:context equality) :count-context (:context counted))
        call #(first (filter (fn [c] (= % (:core-node-id c))) (:calls core)))
        edge #(first (filter (fn [e] (= % (:call-core-node-id e))) (:call-edges core)))]
    (is (= :accepted (:status proof)))
    (is (= :accepted (:status context)) (pr-str context))
    (is (= :gravity/sh08-indexed-private-self-entry-premise-v1 (get-in context [:parameter-value-premise :domain])))
    (is (= context (leaf 'body-is-context core facts bindings forms)))
    (is (= [:accepted :accepted :accepted :accepted]
           (mapv :status [successor getter equality counted])))
    (is (true? (leaf 'body-si-call-valid? working checked bindings (nodes :ix-step) (call :ix-step) (edge :ix-step) nodes)))
    (is (true? (leaf 'body-mg-call-valid? working checked bindings (nodes :ix-get) (call :ix-get) (edge :ix-get) nodes)))
    (is (true? (leaf 'body-call-joins-with-foreign-valid? working checked bindings {}))
        (pr-str
         (let [functions (into {} (map (juxt :function-id identity) (:function-type-table checked)))
               records (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
               facts-by-id (into {} (map (juxt :call-core-node-id identity) (:call-type-facts checked)))]
           {:calls (mapv (fn [e]
                           (let [id (:call-core-node-id e) c (call id) f (facts-by-id id)
                                 local? (= :local-function (:classification e))
                                 validator (case (:dispatch f)
                                             :direct-collection-count 'body-cq-call-valid?
                                             :direct-integral-equality 'body-ie-call-valid?
                                             :direct-atomic-equality 'body-ae-call-valid?
                                             :direct-vector-get 'body-mg-call-valid?
                                             :direct-bounded-unit-addition 'body-si-call-valid?
                                             nil)]
                             {:id id :dispatch (:dispatch f)
                              :valid (if local?
                                       [(leaf 'body-call-valid? e c f nodes functions records bindings (:type-table checked))
                                        (leaf 'body-local-arguments-valid? working checked bindings e c f
                                              (functions (:callee-function-syntax-id e)))]
                                       (when validator (leaf validator working checked bindings (nodes id) c e nodes)))}))
                         (:call-edges core))
            :ledger (mapv #(select-keys % [:kind :producer-rule :lhs]) (:constraint-ledger checked))})))
    (doseq [id [:ix-step :ix-get]]
      (let [producer (if (= id :ix-step) :sh08-indexed-private-self-unit-addition-v1 :sh08-vector-get-indexed-private-self-v1)
            row (first (filter #(= producer (:producer-rule %)) (:constraint-ledger checked)))
            missing (update checked :constraint-ledger #(vec (remove (fn [r] (= row r)) %)))
            duplicate (update checked :constraint-ledger conj row)
            orphan (update checked :constraint-ledger conj (assoc-in row [:lhs :core-node-id] :orphan))]
        (is (map? row))
        (doseq [changed [missing duplicate orphan]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? working changed bindings {}))))))
    (doseq [changed [(assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :normal-entry-count] 0)
                     (assoc-in facts [:recursive-proof :parameter-value-premise :source-induction :vector-extent] 8)
                     (assoc-in facts [:recursive-proof :return-witness :witnesses 0 :node-id] :ix-true)]]
      (is (= :rejected (:status (leaf 'body-is-context core changed bindings forms)))))))

(deftest current-gravity-private-self-effects-development-kernel
  (let [{:keys [core facts bindings forms proof data]} (private-self-value-kernel-data :throw)
        ids (:node-id-map data)
        self (ids :self-call) body (ids :recursive-body)
        _ (leaf 'body-is-context core facts bindings forms)
        derived (leaf 'body-derive-recursive-nodes core facts {})
        graph (:nodes derived)
        checked (leaf 'body-tc-verify core facts bindings graph {:active? true})]
    (is (= :accepted (:status proof)))


    (is (= :effects-and-escaping-error-types (:summary-domain derived)))
    (is (> (:fixed-point-rounds derived) 1))
    (is (= #{:error/throw} (get-in graph [self :transitive])))
    (is (= [:gravity.type/string] (get-in graph [self :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [body :escaping-thrown-error-types])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status checked)))
    (is (= {:helper-form [:gravity.type/string]} (:functions checked)))
    (is (= 3 (count (:rows checked))))
    (is (every? #(= :sh09-private-self-error-equation-v1 (:producer-rule %)) (:rows checked)))
    (is (= [:incoming-private-entry-call :incoming-private-entry-call :self-recursive-call]
           (mapv :role (leaf 'body-recursive-call-facts facts))))
    (is (= :accepted (:status (leaf 'body-derive-recursive-nodes core facts {}))))

    (is (= :rejected (:status (leaf 'body-tc-verify core facts bindings
                                   (assoc-in graph [self :escaping-thrown-error-types] []) {:active? true}))))
    (let [inflated (leaf 'body-derive-nodes-pass core facts {} (:recursive-proof facts)
                         {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string :gravity.type/bool}
                          :escaping-thrown-error-types [:gravity.type/string :gravity.type/bool]})]
      (is (= :accepted (:status inflated)))
      (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes inflated))))
      (is (not= graph (:nodes inflated))))))

(deftest current-gravity-private-self-source-context-development-kernel
  (let [{:keys [core facts bindings forms proof cold]} (private-self-value-kernel-data :string)
        derive (fn [c f bs fs] (leaf 'body-is-context c f bs fs))
        result (derive core facts bindings forms)]
    (is (= :accepted (:status proof)))
    (is (= :accepted (:status cold)))
    (is (= :gravity/sh08-private-self-value-proof-v1 (get-in facts [:recursive-proof :domain])))
    (is (true? (leaf 'body-is-proof-valid? core facts)))

    (is (= (:parameter-value-premise proof) (:parameter-value-premise result)))



    (let [never-result (assoc-in facts [:type-table :entry-0] :gravity.type/never)]
      (is (= result (derive core never-result bindings forms))))
    (doseq [bad [(assoc-in facts [:type-table :input-0] :gravity.type/bool)
                 (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :entries] 7)
                 (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :entries]
                           (vec (repeat 2048 (get-in facts [:recursive-proof :parameter-value-premise :entry-values :entries 0]))))
                 (assoc-in facts [:recursive-proof :parameter-value-premise :cold-entry-derivation :source-argument-call-node-ids] [:forged])
                 (assoc-in facts [:recursive-proof :cold-body-derivation :cut :excluded-node-ids] [])
                 (assoc-in facts [:recursive-proof :return-witness :return-type] :gravity.type/bool)
                 ;; Removed C7 accounting fields remain invalid as unknown proof keys.
                 (assoc-in facts [:recursive-proof :work-count] nil)
                 (assoc facts :constraint-ledger [])]]
      (is (= :rejected (:status (derive core bad bindings forms)))))
    (is (= :rejected (:status (derive (assoc-in core [:module :exports] ['without-path]) facts bindings forms))))
    (is (= :rejected (:status (derive core facts (assoc-in bindings [:helper-binding :visibility] :public) forms))))
    (is (= :rejected (:status (derive core facts bindings (mapv #(if (= :self-op (:form-id %)) (assoc % :value 'forged) %) forms)))))))

(deftest current-gravity-private-self-head-source-context-development-kernel
  (let [{:keys [core facts bindings forms nodes proof]} (private-self-value-kernel-data :head)
        context (leaf 'body-is-context core facts bindings forms)
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise context))
        call (first (filter #(= :call (:core-node-id %)) (:calls core)))
        node (nodes :call)
        types (:type-table facts)
        head (leaf 'body-lq-head-proof working node call nodes bindings types)]
    (is (= :accepted (:status proof)))

    (is (= :accepted (:status head)))
    (is (= :nonempty (get-in head [:proof :knowledge])))
    (is (= (:parameter-value-premise proof) (get-in head [:proof :immutable-binding-record :parameter-value-premise])))
    (is (= (get-in types [:operator :head-proof]) (:proof head)))
    (is (= :list-head-branch-proof-required
           (:reason (leaf 'body-lq-head-proof core node call nodes bindings types))))
    (is (= :rejected (:status (leaf 'body-is-context core
                                    (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :normal-entry-count] 0)
                                    bindings forms))))))

(defn- declared-self-kernel-data []
  (let [[old-core old-facts] (recursive-kernel-data)
        owner #(assoc-in % [:evaluation :owner-function-syntax-id] (sha 20))
        signature {:parameters [{:type-id :gravity.type/string}]
                   :return {:type-id :gravity.type/string}}
        function {:function-id (sha 20) :body-core-node-id (sha 6)
                  :parameters [{:binding-id (sha 51) :type :gravity.type/string}]
                  :return :gravity.type/string :latent-effects [:error/throw]
                  :thrown-error-effects [:pending-sh09]}
        payload (owner (assoc (node 12 :literal []) :attributes {:literal-kind :string :value "deep"}))
        nodes (-> (:nodes old-core)
                  (assoc 2 (owner (assoc (node 3 :literal []) :attributes {:literal-kind :string :value "next"})))
                  (assoc 4 (owner (node 5 :throw [12])))
                  (assoc-in [9 :attributes] {:literal-kind :string :value "entry"})
                  (conj payload))
        calls [(assoc (get-in old-core [:calls 0])
                      :operator-binding-id (sha 50)
                      :ordered-evaluation-node-ids (mapv sha [2 3])
                      :evaluation-order :operator-then-arguments)
               (assoc (get-in old-core [:calls 1]) :operator-binding-id (sha 50))]
        core (-> old-core (assoc :nodes nodes :calls calls :lexical-bindings [])
                 (assoc-in [:module :effects] [:error/throw])
                 (assoc-in [:function-records 0 :signature] signature))
        bindings {(sha 50) {:binding-id (sha 50)} (sha 51) {:binding-id (sha 51)}}
        proof {:domain :gravity/sh08-declared-self-value-proof-v1
               :function-syntax-id (sha 20) :function-core-node-id (sha 7)
               :definition-binding-id (sha 50) :definition-syntax-id (sha 80)
               :body-core-node-id (sha 6) :parameter-binding-ids [(sha 51)]
               :parameter-types [:gravity.type/string] :declared-return-type :gravity.type/string
               :signature signature :component (first (:recursion-components core))
               :self-call-core-node-id (sha 4) :self-call (first calls) :self-edge (first (:call-edges core))
               :self-call-source (:source (get nodes 3))
               :owned-node-ids (mapv :node-id (filter #(= (sha 20) (get-in % [:evaluation :owner-function-syntax-id])) nodes))
               :incoming-call-node-ids (mapv sha [4 11])
               :value-check :complete-body-and-all-incoming-arguments
               :effect-error-status :pending-sh09-recursive-body
               :captures :own-parameters-and-lexical-locals-only}
        types (merge (zipmap (map :node-id nodes) (repeat :gravity.type/string))
                     {(sha 1) :gravity.type/bool (sha 5) :gravity.type/never (sha 7) function (sha 8) function})
        facts {:recursive-proof proof :function-type-table [function] :type-table types :constraint-ledger []
               :call-type-facts [{:call-core-node-id (sha 4) :result-type :gravity.type/string}
                                {:call-core-node-id (sha 11) :result-type :gravity.type/string}]}]
    [core facts bindings]))

(deftest current-gravity-declared-self-effects-development-kernel
  (let [[core facts bindings] (declared-self-kernel-data)
        derived (leaf 'body-derive-recursive-nodes core facts {})
        graph (:nodes derived)
        checked (leaf 'body-tc-verify core facts bindings graph {:active? true})]
    (is (true? (leaf 'body-recursive-proof-valid? core facts)))
    (is (true? (leaf 'body-ds-captures-valid? core facts bindings)))
    (is (= :accepted (:status derived)))
    (is (= :effects-and-escaping-error-types (:summary-domain derived)))
    (is (> (:fixed-point-rounds derived) 1))
    (is (= #{:error/throw} (get-in graph [(sha 4) :transitive])))
    (is (= [:gravity.type/string] (get-in graph [(sha 4) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 6) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 11) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 4) :throw-call-dependency :callee-error-types])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status checked)))
    (is (= {(sha 20) [:gravity.type/string]} (:functions checked)))
    (is (= 2 (count (:rows checked))))
    (is (every? #(= :sh09-declared-self-error-equation-v1 (:producer-rule %)) (:rows checked)))
    (is (= [:self-recursive-call :incoming-declared-call]
           (mapv :role (leaf 'body-recursive-call-facts facts))))
    (is (= :accepted (:status (leaf 'body-derive-recursive-nodes core facts {}))))

    (doseq [bad [(assoc-in facts [:recursive-proof :self-call-core-node-id] (sha 99))
                 (assoc-in facts [:recursive-proof :owned-node-ids] [])
                 (assoc-in facts [:recursive-proof :incoming-call-node-ids] [(sha 4)])
                 (assoc-in facts [:recursive-proof :self-edge :ordinal] 9)
                 (assoc-in facts [:recursive-proof :declared-return-type] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])]]
      (is (false? (leaf 'body-recursive-proof-valid? core bad))))
    (let [bad (assoc-in core [:nodes 2 :attributes :binding-id] (sha 99))
          reference (assoc-in bad [:nodes 2 :core-form] :reference)]
      (is (false? (leaf 'body-ds-captures-valid? reference facts bindings))))
    (is (= :rejected
           (:status (leaf 'body-tc-verify core facts bindings
                          (assoc-in graph [(sha 4) :escaping-thrown-error-types] [])
                          {:active? true}))))
    (let [inflated (leaf 'body-derive-nodes-pass core facts {} (:recursive-proof facts)
                         {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string :gravity.type/bool}
                          :escaping-thrown-error-types [:gravity.type/string :gravity.type/bool]})]
      (is (= :accepted (:status inflated)))
      (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes inflated))))
      (is (not= graph (:nodes inflated))))))

(deftest body-effect-recursive-proof-development-kernel
  (let [[core facts] (recursive-kernel-data)]
    (is (true? (leaf 'body-recursive-proof-valid? core facts)))
    (let [truthy-core (-> core (assoc-in [:nodes 0 :core-form] :reference)
                              (assoc-in [:nodes 0 :attributes] {:binding-id (sha 51)}))
          truthy-facts (assoc-in facts [:type-table (sha 1)] :gravity.type/integer)]
      (is (true? (leaf 'body-recursive-proof-valid? truthy-core truthy-facts)))
      (is (= :accepted (:status (leaf 'body-derive-recursive-nodes truthy-core truthy-facts {})))))
    (doseq [bad [{:recursive-proof {:parameter-types []} :call-type-facts [7]}
                 {:recursive-proof {:parameter-types []} :call-type-facts [nil]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [7]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [nil]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts []}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [{:call-core-node-id (sha 4)}]}
                 {:recursive-proof (:recursive-proof facts) :call-type-facts [{:call-core-node-id (sha 11)}]}]]
      (is (nil? (leaf 'body-recursive-call-facts bad))))
    (doseq [mutate [(fn [f] (assoc f :recursive-proof 7))
                    (fn [f] (assoc-in f [:recursive-proof :parameter-types] 7))
                    (fn [f] (assoc-in f [:recursive-proof :recursive-call-core-node-id] (sha 99)))
                    (fn [f] (assoc-in f [:recursive-proof :base-node-id] (sha 10)))
                    (fn [f] (assoc-in f [:recursive-proof :recursive-edge :callee-function-syntax-id] (sha 99)))
                    (fn [f] (assoc-in f [:recursive-proof :parameter-evidence-call :argument-types] [:gravity.type/string]))
                    (fn [f] (assoc-in f [:recursive-proof :captures?] true))]]
      (is (false? (leaf 'body-recursive-proof-valid? core (mutate facts)))))
    (doseq [mutate [(fn [c] (assoc-in c [:recursion-components 0 :kind] :mutually-recursive))
                    (fn [c] (assoc-in c [:recursion-components 0 :internal-call-edge-ordinals] []))
                    (fn [c] (assoc-in c [:recursion-components 0 :function-syntax-ids] 7))
                    (fn [c] (assoc-in c [:nodes 2 :attributes :binding-id] (sha 99)))
                    (fn [c] (assoc-in c [:nodes 9 :attributes :literal-kind] :string))
                    (fn [c] (update c :nodes #(filterv (fn [n] (not= (sha 5) (:node-id n))) %)))]]
      (is (false? (leaf 'body-recursive-proof-valid? (mutate core) facts))))))

(deftest body-effect-recursive-fixed-point-development-kernel
  (let [[core facts] (recursive-kernel-data)
        derived (leaf 'body-derive-recursive-nodes core facts {})
        graph (:nodes derived)
        scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        empty-pass {:index-calls {:call-core-node-id 0 :function-id 0}
                    :index-rows {:call-core-node-id 0 :function-id 0}
                    :ready-attempts 0 :ready-false 0
                    :predicates {'body-call-invokes? 0 'body-node-supported? 0 'body-order-valid? 0
                                 'body-call-valid? 0 'body-import-call-valid? 0}}
        observe (fn []
                  (let [counts (atom {:passes [] :equation-checks 0})
                        active-pass (atom nil)
                        result
                        (with-redefs-fn
                          {scoped
                           (fn [& args]
                             (let [function (nth args 2) inputs (nth args 3)]
                               (if (= 'body-derive-nodes-pass function)
                                 (let [parent @active-pass index (count (:passes @counts))]
                                   (swap! counts update :passes conj empty-pass)
                                   (reset! active-pass index)
                                   (try (apply original args)
                                        (finally (reset! active-pass parent))))
                                 (let [index @active-pass
                                       result (apply original args)]
                                   (when (= 'body-graph-equations-valid? function)
                                     (swap! counts update :equation-checks inc))
                                   (when (some? index)
                                     (cond
                                       (= 'body-index function)
                                       (let [[rows field] inputs]
                                         (when (contains? (:index-calls empty-pass) field)
                                           (swap! counts update-in [:passes index :index-calls field] inc)
                                           (swap! counts update-in [:passes index :index-rows field] + (count rows))))
                                       (= 'body-ready? function)
                                       (do (swap! counts update-in [:passes index :ready-attempts] inc)
                                           (when (false? result)
                                             (swap! counts update-in [:passes index :ready-false] inc)))
                                       (contains? (:predicates empty-pass) function)
                                       (swap! counts update-in [:passes index :predicates function] inc)))
                                   result))))}
                          #(leaf 'body-derive-recursive-nodes core facts {}))]
                    (assoc @counts :result result)))
        expected-pass {:index-calls {:call-core-node-id 1 :function-id 1}
                       :index-rows {:call-core-node-id 2 :function-id 1}
                       :ready-attempts 11 :ready-false 0
                       :predicates {'body-call-invokes? 13 'body-node-supported? 11 'body-order-valid? 11
                                    'body-call-valid? 0 'body-import-call-valid? 0}}]
    (is (= :accepted (:status derived)))
    (is (= (set (map :node-id (:nodes core))) (set (keys graph))))
    (is (= (sha 6) (get-in graph [(sha 4) :call-body-dependency])))
    (is (= (mapv sha [2 3 6]) (get-in graph [(sha 4) :dependencies])))
    (is (= #{} (get-in graph [(sha 6) :transitive])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    ;; Observe the same semantic helper traversal once; no work quota varies
    ;; the result or the fixed-point proof.
    (let [observed (observe)]
      (is (= derived (:result observed)))
      (is (= [expected-pass expected-pass] (:passes observed)) (pr-str observed))
      (is (= 1 (:equation-checks observed))))
    ;; Equations alone permit a nonleast solution. Fresh derivation starts at bottom.
    (let [inflated (:nodes (leaf 'body-derive-nodes-pass core facts {} (:recursive-proof facts) #{:compiler/read-ir}))]
      (is (true? (leaf 'body-graph-equations-valid? core facts {} inflated)))
      (is (not= graph inflated)))
    (doseq [bad [(update core :nodes conj (node 99 :throw []))
                 (update core :nodes #(filterv (fn [n] (not= (sha 2) (:node-id n))) %))
                 (assoc-in core [:nodes 0 :children] [(sha 1)])]]
      (is (= :rejected (:status (leaf 'body-derive-recursive-nodes bad facts {})))))
    (is (= :cyclic-or-missing-body-effect-dependencies
           (:reason (leaf 'body-derive-nodes-with-foreign core facts {}))))))

(defn- callable-kernel-data []
  (let [owned (fn [id n] (assoc-in n [:evaluation :owner-function-syntax-id] (sha id)))
        ;; Keep the named function values separate from calls that execute them.
        nodes [(owned 20 (assoc (node 1 :reference []) :attributes {:binding-id (sha 51)}))
               (assoc-in (node 2 :fn [1]) [:source :syntax-id] (sha 20)) (node 3 :def [2])
               (owned 30 (assoc (node 4 :reference []) :attributes {:binding-id (sha 61)}))
               (owned 30 (assoc (node 5 :reference []) :attributes {:binding-id (sha 62)}))
               (owned 30 (node 6 :call [4 5]))
               (assoc-in (node 7 :fn [6]) [:source :syntax-id] (sha 30)) (node 8 :def [7])
               (assoc (node 9 :reference []) :attributes {:binding-id (sha 60)})
               (assoc (node 10 :reference []) :attributes {:binding-id (sha 50)})
               (assoc (node 11 :literal []) :attributes {:literal-kind :integer :value 7}) (node 12 :call [9 10 11])]
        function-type (fn [id core body name ids parameters]
                        {:artifact :gravity/c7-function-type :function-id (sha id) :function-core-node-id (sha core)
                         :body-core-node-id (sha body) :definition-name name :parameter-binding-ids (mapv sha ids)
                         :parameters parameters :return :gravity.type/integer :return-conflict nil :fixed-arity (count ids)
                         :latent-effects [] :capabilities [] :ownership-constraints :pending-sh10 :profile-constraints [:meta]
                         :thrown-error-effects [:pending-sh09] :source-span (get-in nodes [(dec core) :source :semantic-span])
                         :syntax-id (sha id) :origin-chain [] :generated-origin nil :profile :meta :target :jvm :status :inferred})
        identity-type (function-type 20 2 1 'identity [51] [{:binding-id (sha 51) :type :gravity.type/integer}])
        apply-type (function-type 30 7 6 'apply-one [61 62] [{:binding-id (sha 61) :type identity-type}
                                                           {:binding-id (sha 62) :type :gravity.type/integer}])
        inner-edge {:ordinal 0 :call-core-node-id (sha 6) :caller-function-syntax-id (sha 30) :caller-function-core-node-id (sha 7)
                    :callee-binding-id (sha 61) :callee-definition-syntax-id (sha 90) :callee-function-syntax-id nil
                    :callee-function-core-node-id nil :argument-core-node-ids [(sha 5)]
                    :ordered-evaluation-node-ids [(sha 4) (sha 5)] :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        outer-edge (assoc inner-edge :ordinal 1 :call-core-node-id (sha 12) :caller-function-syntax-id nil :caller-function-core-node-id nil
                         :callee-binding-id (sha 60) :callee-definition-syntax-id (sha 91) :callee-function-syntax-id (sha 30)
                         :callee-function-core-node-id (sha 7) :argument-core-node-ids [(sha 10) (sha 11)]
                         :ordered-evaluation-node-ids [(sha 9) (sha 10) (sha 11)] :classification :local-function)
        inner {:core-node-id (sha 6) :operator-binding-id (sha 61) :argument-node-ids [(sha 5)]
               :ordered-evaluation-node-ids [(sha 4) (sha 5)] :evaluation-order :operator-then-arguments}
        outer {:core-node-id (sha 12) :operator-binding-id (sha 60) :argument-node-ids [(sha 10) (sha 11)]
               :ordered-evaluation-node-ids [(sha 9) (sha 10) (sha 11)] :evaluation-order :operator-then-arguments}
        proof {:domain :gravity/sh08-authoritative-higher-order-proof-v1
               :identity-function-syntax-id (sha 20) :identity-function-core-node-id (sha 2)
               :identity-definition-binding-id (sha 50) :identity-parameter-binding-id (sha 51) :identity-body-core-node-id (sha 1)
               :apply-function-syntax-id (sha 30) :apply-function-core-node-id (sha 7) :apply-definition-binding-id (sha 60)
               :apply-callable-binding-id (sha 61) :apply-value-binding-id (sha 62) :apply-body-core-node-id (sha 6)
               :inner-call-core-node-id (sha 6) :inner-operator-binding-id (sha 61) :inner-argument-node-ids [(sha 5)]
               :inner-evaluation-order :operator-then-arguments :inner-source-span (get-in nodes [5 :source :semantic-span]) :inner-edge inner-edge
               :outer-call-core-node-id (sha 12) :outer-operator-binding-id (sha 60) :outer-argument-node-ids [(sha 10) (sha 11)]
               :outer-evaluation-order :operator-then-arguments :outer-source-span (get-in nodes [11 :source :semantic-span]) :outer-edge outer-edge
               :outer-value-argument-node-id (sha 11) :outer-value-literal-kind :integer :identity-parameter-type :gravity.type/integer
               :apply-callable-parameter-type identity-type :apply-value-parameter-type :gravity.type/integer
               :identity-function-type identity-type :apply-function-type apply-type
               :inner-result-type :gravity.type/integer :outer-result-type :gravity.type/integer}
        fact {:artifact :gravity/c7-call-type-fact :status :inferred :call-core-node-id (sha 6)
              :caller-function-syntax-id (sha 30) :callee-function-syntax-id nil
              :argument-types [:gravity.type/integer] :result-type :gravity.type/integer
              :evaluation-order :operator-then-arguments :syntax-id (get-in nodes [5 :source :syntax-id])
              :source-span (get-in nodes [5 :source :semantic-span]) :origin-chain [] :generated-origin nil
              :effects [] :capabilities [] :profile :meta :target :jvm
              :dispatch :indirect-function-value :higher-order-call? true :b47-edge inner-edge
              :callable-parameter-binding-id (sha 61) :selected-identity-function-syntax-id (sha 20)
              :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id (sha 6)
                                :callee-function-syntax-id nil :argument-types [:gravity.type/integer]
                                :result-type :gravity.type/integer :higher-order-call? true}}
        core {:nodes nodes :calls [inner outer] :call-edges [inner-edge outer-edge] :definitions [] :recursion-components []
              :function-records [{:function-syntax-id (sha 20) :function-core-node-id (sha 2) :body-core-node-id (sha 1)
                                  :definition-binding-id (sha 50) :definition-kind :named-top-level :fixed-arity 1 :parameter-binding-ids [(sha 51)]}
                                 {:function-syntax-id (sha 30) :function-core-node-id (sha 7) :body-core-node-id (sha 6)
                                  :definition-binding-id (sha 60) :definition-kind :named-top-level :fixed-arity 2 :parameter-binding-ids [(sha 61) (sha 62)]}]}
        facts {:higher-order-proof proof :function-type-table [identity-type apply-type]
               :module {:effects [] :capabilities [] :profile :meta :target :jvm}
               :type-table {(sha 4) identity-type (sha 5) :gravity.type/integer (sha 6) :gravity.type/integer}
               :call-type-facts [fact]}]
    [core facts {(sha 61) {:definition-syntax-id (sha 90)}}]))

(deftest body-effect-callable-proof-development-kernel
  (let [[core facts bindings] (callable-kernel-data)]
    (is (true? (leaf 'body-ho-proof-valid? core facts bindings)))
    (is (true? (leaf 'body-function-shapes-with-proof? facts)))
    (is (false? (leaf 'body-function-shapes? (:function-type-table facts))))
    (doseq [mutate [(fn [f] (assoc f :higher-order-proof 7))
                    (fn [f] (assoc-in f [:higher-order-proof :inner-argument-node-ids] 7))
                    (fn [f] (assoc-in f [:higher-order-proof :identity-function-syntax-id] (sha 99)))
                    (fn [f] (assoc-in f [:higher-order-proof :inner-edge :callee-function-syntax-id] (sha 20)))
                    (fn [f] (assoc-in f [:higher-order-proof :identity-function-type :return] :gravity.type/bool))
                    (fn [f] (assoc-in f [:function-type-table 1 :parameters 1 :type] (get-in f [:higher-order-proof :identity-function-type])))]]
      (is (false? (leaf 'body-ho-proof-valid? core (mutate facts) bindings))))
    (doseq [mutate [(fn [c] (assoc-in c [:nodes 9 :attributes :binding-id] (sha 60)))
                    (fn [c] (assoc-in c [:nodes 4 :attributes :binding-id] (sha 99)))
                    (fn [c] (assoc-in c [:nodes 10 :attributes :literal-kind] :string))
                    (fn [c] (assoc-in c [:calls 0 :argument-node-ids] 7))
                    (fn [c] (assoc c :recursion-components [{}]))]]
      (is (false? (leaf 'body-ho-proof-valid? (mutate core) facts bindings))))
    (is (false? (leaf 'body-function-shapes-with-proof?
                     (assoc-in facts [:function-type-table 1 :parameters 1 :type] (get-in facts [:higher-order-proof :identity-function-type])))))
    (is (false? (leaf 'body-function-shapes-with-proof?
                     (assoc-in facts [:function-type-table 0 :return] (get-in facts [:higher-order-proof :identity-function-type])))))))

(deftest body-effect-callable-dependency-development-kernel
  (let [[core facts _] (callable-kernel-data)
        inner (first (:calls core)) edge (first (:call-edges core)) fact (first (:call-type-facts facts))
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        run #(leaf 'body-ho-inner-call-valid? edge inner % nodes facts)
        derived (leaf 'body-derive-nodes-with-foreign core facts {}) graph (:nodes derived)]
    (is (true? (run fact)))
    (doseq [bad [(assoc fact :selected-identity-function-syntax-id (sha 99))
                 (assoc fact :callee-function-syntax-id (sha 20)) (assoc fact :profile :hosted)
                 (assoc-in fact [:fact-id-request :callee-function-syntax-id] (sha 20))
                 (assoc fact :argument-types 7) 7 nil]]
      (is (false? (run bad))))
    (is (= :accepted (:status derived)))
    (is (nil? (get-in graph [(sha 6) :call-body-dependency])))
    (is (= (sha 1) (get-in graph [(sha 6) :selected-body-dependency :body-core-node-id])))
    (is (= edge (get-in graph [(sha 6) :selected-body-dependency :source-edge])))
    (is (= (mapv sha [4 5 1]) (get-in graph [(sha 6) :dependencies])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))

    (let [seed {(sha 1) {:transitive #{:compiler/read-ir}} (sha 4) {:transitive #{}} (sha 5) {:transitive #{}}}
          call (leaf 'body-local-node-record (get nodes (sha 6)) (sha 1) seed :gravity.type/integer facts edge)
          value (leaf 'body-local-node-record (get nodes (sha 10)) nil seed :callable facts nil)
          construction (leaf 'body-local-node-record (get nodes (sha 2)) nil seed :callable facts nil)]
      (is (= #{:compiler/read-ir} (:transitive call)))
      (is (= #{} (:transitive value) (:transitive construction)))
      (is (= #{:compiler/read-ir} (:latent construction))))))

(deftest body-effect-public-forwarding-source-boundary
  (let [forms (read-string (str "[" (slurp (path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")) "]"))
        definitions (into {} (keep #(when (and (seq? %) (= 'defn (first %))) [(second %) %]) forms))
        namespace (first (filter #(and (seq? %) (= 'ns (first %))) forms))
        exports (second (first (filter #(and (seq? %) (= :exports (first %))) namespace)))]
    (is (some #{'(:requires [gravity.effects :as effects])} namespace))
    (is (every? (set exports) '[sh09-build-authenticated-body-effect-result sh09-verify-authenticated-body-effect-result]))
    (is (= '(defn sh09-build-authenticated-body-effect-result [typed verification binding]
              (effects/build-body-effect-result typed verification binding))
           (get definitions 'sh09-build-authenticated-body-effect-result)))
    (is (= '(defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]
              (effects/verify-body-effect-result typed verification binding candidate))
           (get definitions 'sh09-verify-authenticated-body-effect-result)))))

(defn- assert-body-products [products result]
  (let [typed (:typed products) core (get-in products [:request :canonical-core-artifact]) graph (:effect-graph result)]
    (is (= :accepted (:status (:c5 products)) (:status (:artifact products)) (:status typed)))
    (is (= :passed (:status (:report products)) (:status (:verification products))))
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (when-not (= :accepted (:status result))
      (throw (ex-info "Body leaf rejected actual products before costly C8 loading" {:diagnostics (:diagnostics result)})))
    (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes graph)))))
    (is (= #{:compiler/read-ir :memory/allocate} (get-in graph [:namespace :declared])))
    (is (= #{} (get-in graph [:namespace :inferred])))
    (is (= (count (:function-type-table typed)) (count (:functions graph))))
    (is (every? #(= [:pending-sh09] (:throws %)) (vals (:functions graph))))
    (is (every? #(= #{:compiler/read-ir :memory/allocate} (:declared %)) (vals (:functions graph))))
    (doseq [function (:function-type-table typed)]
      (is (= (if (#{'choose 'assemble} (:definition-name function)) #{:memory/allocate} #{})
             (get-in graph [:functions (:function-id function) :inferred]))))
    (is (some #(and (= :quote (:operation %)) (empty? (:dependencies %))) (vals (:nodes graph))))
    (is (some #(and (= :fn (:operation %)) (empty? (:evaluated-children %)) (seq (:latent-children %))) (vals (:nodes graph))))
    (is (some #(= :guard (:role %)) (mapcat :conditional-coverage (vals (:nodes graph)))))))

(defn- assert-coherent-body-rejections [products result]
  (let [typed (:typed products) core (get-in products [:request :canonical-core-artifact])
        call (first (:call-type-facts typed))
        guard-id (some #(get-in % [:guard :core-node-id]) (:constraint-ledger typed))
        literal-id (:node-id (first (filter #(= :literal (:core-form %)) (:nodes core))))
        function-index (first (keep-indexed #(when (= (:function-id %2) (:callee-function-syntax-id call)) %1) (:function-type-table typed)))
        mutations [(fn [t] (update t :type-table #(-> % (dissoc literal-id) (assoc (sha 999) :gravity.type/integer))))
                   (fn [t] (update t :call-type-facts #(filterv (fn [f] (not= guard-id (:call-core-node-id f))) %)))
                   (fn [t] (assoc-in t [:function-type-table function-index :body-core-node-id] literal-id))
                   (fn [t] (assoc-in t [:function-type-table function-index :return] :gravity.type/forged))
                   (fn [t] (assoc-in t [:function-type-table function-index :origin-chain] [:substituted]))
                   (fn [t] (assoc-in t [:call-type-facts 0 :callee-function-syntax-id] (sha 888)))
                   (fn [t] (assoc-in t [:function-products :recursion-components] [{:cycle true}]))
                   (fn [t] (assoc t :scope :capture-free-higher-order-fixed-arity-one-hop))]]
    (doseq [mutate mutations]
      (let [rejected (build-leaf (coherent-typed-mutation products mutate))]
        (is (= :rejected (:status rejected)))
        (is (= "C8-VERIFY" (get-in rejected [:diagnostics 0 :rule])))
        (when-not (= :rejected (:status rejected))
          (throw (ex-info "Body mutation accepted before costly module loading" {})))))
    (let [graph (:effect-graph result)
          erased-graph (assoc graph :nodes {} :functions {})
          erased-throws (update graph :functions #(into {} (map (fn [[id f]] [id (assoc f :throws [])])) %))]
      (doseq [candidate [(reseal-effect-graph result erased-graph) (reseal-effect-graph result erased-throws)]]
        (is (= :rejected (:status (leaf 'verify-body-effect-result (:typed products) (:verification products) (:binding products) candidate)))))))
  (is (= :imported-body-effects-pending
         (get-in (leaf 'build-body-effect-result {:scope :direct-imported-functions-types-only} {} {}) [:diagnostics 0 :reason]))))

(deftest body-effect-callable-authenticated-paired-composition
  (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-call-nonlocal" %))
                       [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (let [typed (:typed p) proof (:higher-order-proof typed) graph (:effect-graph result)
            core (get-in p [:request :canonical-core-artifact]) facts (:identity-input typed)
            inner (:inner-call-core-node-id proof) selected (get-in graph [:nodes inner :selected-body-dependency])]
        (is (= :accepted (:status typed) (:status result)) (pr-str (:diagnostics result)))
        (when-not (= :accepted (:status result))
          (throw (ex-info "Callable leaf rejected actual products" {:diagnostics (:diagnostics result)})))
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :capture-free-one-hop-body-effects-with-thrown-effects-pending (:scope result)))
        (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes graph)))))
        (is (= (:identity-body-core-node-id proof) (:body-core-node-id selected)))
        (is (= (:identity-function-syntax-id proof) (:function-id selected)))
        (is (= (:inner-edge proof) (:source-edge selected)))
        (is (nil? (get-in graph [:nodes inner :call-body-dependency])))
        (is (nil? (get-in selected [:source-edge :callee-function-syntax-id])))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (every? #(and (= #{} (:inferred %)) (= [:pending-sh09] (:throws %))) (vals (:functions graph))))
        (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (doseq [mutate [(fn [t] (assoc-in t [:higher-order-proof :identity-function-syntax-id] (sha 999)))
                        (fn [t] (assoc-in t [:higher-order-proof :identity-function-type :return] :gravity.type/bool))
                        (fn [t] (assoc-in t [:higher-order-proof :inner-edge :callee-function-syntax-id] (:identity-function-syntax-id proof)))
                        (fn [t] (assoc-in t [:higher-order-call-facts 0 :selected-identity-function-syntax-id] (sha 999)))
                        (fn [t] (update t :call-type-facts
                                  #(mapv (fn [f] (if (= inner (:call-core-node-id f))
                                                  (assoc f :selected-identity-function-syntax-id (sha 999)) f)) %)))
                        (fn [t] (assoc t :higher-order-proof 7))
                        (fn [t] (assoc t :higher-order-call-facts [7]))]]
          (is (= :rejected (:status (build-leaf (coherent-typed-mutation p mutate))))))
        (doseq [candidate [(reseal-effect-graph result (assoc-in graph [:nodes inner :selected-body-dependency :body-core-node-id] (sha 999)))
                           (reseal-effect-graph result (assoc-in graph [:nodes inner :transitive] #{:compiler/read-ir}))
                           (reseal-effect-graph result (update graph :functions
                             #(into {} (map (fn [[id f]] [id (assoc f :throws [])]) %))))]]
          (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))))
    (is (= (:identity-input (first results)) (:identity-input (second results)))))
  (let [products (mapv fresh-callable-import-products [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (let [producer (first (vals (:producer-effect-graphs result)))
            proof (get-in producer [:sh08-identity-input :higher-order-proof])
            producer-graph (:effect-graph producer)]
        (is (= :accepted (get-in p [:typed :status]) (:status result)) (pr-str (:diagnostics result)))
        (when-not (= :accepted (:status result))
          (throw (ex-info "Scalar import from callable producer rejected" {:diagnostics (:diagnostics result)})))
        (is (= :passed (get-in p [:verification :status])))
        (is (= 1 (count (:producer-effect-graphs result)) (count (:imported-body-dependencies result))))
        (is (= :gravity/sh08-authoritative-higher-order-type-v1 (get-in producer [:sh08-identity-input :domain])))
        (is (= 2 (count (:functions producer-graph))))
        (is (= (:identity-body-core-node-id proof)
               (get-in producer-graph [:nodes (:inner-call-core-node-id proof) :selected-body-dependency :body-core-node-id])))
        (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :passed (:status (leaf 'verify-body-effect-result (:typed p) (:verification p) (:binding p) result))))
        (is (= :rejected (:status (build-leaf (coherent-typed-mutation p
                    #(assoc-in % [:module-type-producers 0 :identity-input :higher-order-proof :inner-call-core-node-id] (sha 999)))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))))

(defn- assert-current-c7-callable-rejections [cases]
  (doseq [[basename expected-rule expected-reason] cases
          extension [".gravity" ".qst"]]
    (let [p (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" basename extension))
          diagnostic (get-in p [:typed :diagnostics 0])]
      (println "Current C7 callable rejection" (pr-str {:fixture basename :extension extension
                   :diagnostic (select-keys diagnostic [:rule :reason :expected-type :actual-type])}))
      (is (= :rejected (get-in p [:typed :status])))
      (is (= [expected-rule expected-reason] [(:rule diagnostic) (:reason diagnostic)]))
      (is (= :rejected (:status (build-leaf p)))))))

(deftest body-effect-callable-current-c7-rejections
  (assert-current-c7-callable-rejections
    [["function-value-nonfunction" "C7-ANNOTATION" :unsupported-nonlocal-call]
     ["function-value-capture" "C7-TYPE-MISMATCH" :function-result-type-mismatch]]))

(deftest body-effect-callable-capture-current-c7-rejection
  (assert-current-c7-callable-rejections
    [["function-value-capture" "C7-TYPE-MISMATCH" :function-result-type-mismatch]]))

(deftest body-effect-recursive-authenticated-paired-composition
  (doseq [basename ["function-self-recursive-type" "function-self-recursive-string-type"]]
    (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/" basename %))
                         [".gravity" ".qst"])
          results (mapv build-leaf products)]
      (doseq [[p result] (map vector products results)]
        (let [typed (:typed p) core (get-in p [:request :canonical-core-artifact])
              facts (:identity-input typed) proof (:recursive-proof typed) graph (:effect-graph result)
              recursive-call (:recursive-call-core-node-id proof)]
          (is (= :accepted (:status typed) (:status result)) (pr-str (:diagnostics result)))
          (when-not (= :accepted (:status result))
            (throw (ex-info "Recursive leaf rejected actual products" {:diagnostics (:diagnostics result)})))
          (is (= :passed (:status (:report p)) (:status (:verification p))))
          (is (= :bounded-self-recursive-body-effects-with-thrown-effects-pending (:scope result)))
          (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
          (is (= (set (map :node-id (:nodes core))) (set (keys (:nodes graph)))))
          (is (= :checked (get-in graph [:recursive-fixed-point :equations])))
          (is (= (:body-core-node-id (first (:function-type-table typed)))
                 (get-in graph [:nodes recursive-call :call-body-dependency])))
          (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
          (is (= #{} (get-in graph [:namespace :inferred])))
          (is (every? #(and (= #{} (:inferred %)) (= [:pending-sh09] (:throws %))) (vals (:functions graph))))
          (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))
          (doseq [mutate [(fn [t] (assoc-in t [:recursive-proof :base-node-id] (sha 999)))
                          (fn [t] (assoc-in t [:recursive-proof :recursive-edge :callee-function-syntax-id] (sha 999)))
                          (fn [t] (assoc-in t [:recursive-proof :parameter-evidence-call :argument-types] [:gravity.type/bool]))
                          (fn [t] (assoc-in t [:recursive-call-facts 0 :dispatch] :forged))
                          (fn [t] (assoc t :scope :first-order-fixed-arity-functions-locals-calls))]]
            (is (= :rejected (:status (build-leaf (coherent-typed-mutation p mutate))))))
          (let [inflated (:nodes (leaf 'body-derive-nodes-pass core facts {} proof #{:compiler/read-ir}))
                forged (assoc (leaf 'body-build-graph core facts inflated) :recursive-fixed-point (:recursive-fixed-point graph))]
            (is (true? (leaf 'body-graph-equations-valid? core facts {} inflated)))
            (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p)
                                           (reseal-effect-graph result forged))))))
          (let [erased (update graph :functions #(into {} (map (fn [[id f]] [id (assoc f :throws [])]) %)))]
            (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p)
                                           (reseal-effect-graph result erased))))))))
      (is (= (:identity-input (first results)) (:identity-input (second results))))))
  (let [products (mapv fresh-recursive-import-products [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (is (= :accepted (get-in p [:typed :status]) (:status result)) (pr-str (:diagnostics result)))
      (when-not (= :accepted (:status result))
        (throw (ex-info "Imported recursive producer rejected" {:diagnostics (:diagnostics result)})))
      (is (= :passed (get-in p [:verification :status])))
      (is (= 2 (count (:producer-effect-graphs result)) (count (:imported-body-dependencies result))))
      (is (every? #(= :gravity/sh08-recursive-function-typed-core-v1 (get-in % [:sh08-identity-input :domain]))
                  (vals (:producer-effect-graphs result))))
      (is (every? #(= :checked (get-in % [:effect-graph :recursive-fixed-point :equations]))
                  (vals (:producer-effect-graphs result))))
      (is (= :passed (:status (leaf 'verify-body-effect-result (:typed p) (:verification p) (:binding p) result))))
      (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
      (is (= :rejected (:status (build-leaf (coherent-typed-mutation p
                    #(assoc-in % [:module-type-producers 0 :identity-input :recursive-proof :base-node-id] (sha 999))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results)))))
  (doseq [extension [".gravity" ".qst"]]
    (let [p (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-07/b47-function-call-recursion/accepted/function-call-recursion" extension))]
      (is (= :rejected (get-in p [:typed :status])))
      (is (= :mutual-recursion-unsupported (get-in p [:typed :diagnostics 0 :actual-type]))))))

(deftest body-effect-imported-authenticated-paired-composition
  ;; One invocation-local pair: actual C5/C6, fresh recursive C7 verification,
  ;; full original four-key binding, and current Gravity effects source.
  (let [products (mapv fresh-import-products [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (let [typed (:typed p) request (:request p) graph (:effect-graph result)
            producer-request (first (get-in request [:module-type-inputs :module-requests]))
            producer-core (:canonical-core-artifact producer-request)
            facts (get-in typed [:module-type-producers 0 :identity-input])
            key [(get-in typed [:module-type-producers 0 :producer-sh06-artifact-id]) (:artifact-id producer-core)]
            producer-graph (get-in result [:producer-effect-graphs key :effect-graph])
            private-function (first (filter #(= 'integer-base (:definition-name %)) (:function-type-table facts)))
            chain-function (first (filter #(= 'integer-chain (:definition-name %)) (:function-type-table facts)))]
        (is (= :accepted (:status (:c5 p)) (:status (:producer-c5 p)) (:status typed)))
        (is (= :passed (:status (:verification p))))
        (is (= request (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (when-not (= :accepted (:status result))
          (throw (ex-info "Imported leaf rejected actual products" {:diagnostics (:diagnostics result)})))
        (is (= :direct-imported-body-effects-with-thrown-effects-pending (:scope result)))
        (is (= :gravity/sh09-direct-imported-body-effects-v1 (get-in result [:identity-input :domain])))
        (is (= (set (map :node-id (get-in request [:canonical-core-artifact :nodes]))) (set (keys (:nodes graph)))))
        (is (= (set (map :node-id (:nodes producer-core))) (set (keys (:nodes producer-graph)))))
        (is (= (count (:function-type-table facts)) (count (:functions producer-graph))))
        (is (map? private-function))
        (is (= (:body-core-node-id private-function)
               (get-in producer-graph [:nodes (:body-core-node-id chain-function) :call-body-dependency])))
        (is (= 4 (count (:imported-body-dependencies result))))
        (is (= 4 (count (filter :foreign-body-dependency (vals (:nodes graph))))))
        (is (every? #(= #{} (:inferred %)) (concat (vals (:functions graph)) (vals (:functions producer-graph)))))
        (is (every? #(= [:pending-sh09] (:throws %)) (concat (vals (:functions graph)) (vals (:functions producer-graph)))))
        (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (is (false? (leaf 'sh09-adapter-compiler-read-request-valid? request typed)))
        (doseq [field [:module-type-producers :imported-function-types]
                malformed [7 [7]]]
          (let [rejected (build-leaf (coherent-typed-mutation p #(assoc % field malformed)))]
            (is (= :rejected (:status rejected)))
            (is (= "C8-VERIFY" (get-in rejected [:diagnostics 0 :rule])))))
        (doseq [mutate [(fn [t] (assoc-in t [:imported-function-types 0 :origin] {}))
                        (fn [t] (assoc-in t [:imported-function-types 0 :function-type :body-core-node-id] (sha 999)))
                        (fn [t] (assoc-in t [:call-type-facts 0 :imported-function :producer-sh06-artifact-id] (sha 999)))
                        (fn [t] (assoc-in t [:module-type-producers 0 :identity-input :function-type-table 0 :body-core-node-id] (sha 999)))]]
          (is (= :rejected (:status (build-leaf (coherent-typed-mutation p mutate))))))
        (doseq [candidate [(-> result (assoc :producer-effect-graphs {}) (assoc-in [:identity-input :producer-effect-graphs] {}))
                           (-> result (assoc :imported-body-dependencies {}) (assoc-in [:identity-input :imported-body-dependencies] {}))
                           (reseal-effect-graph result (assoc graph :nodes {}))
                           (reseal-effect-graph result (update graph :functions
                             #(into {} (map (fn [[id f]] [id (assoc f :throws [])]) %))))]]
          (is (= :rejected (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))
        (let [bad-request (assoc-in request [:module-type-inputs :module-requests] [])
              rejected (leaf 'build-body-effect-result typed (:verification p) (seal-binding bad-request typed (:verification p)))]
          (is (= :rejected (:status rejected))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))))

(deftest body-allocation-fixtures-development-kernel
  (doseq [relative (into ["sh-09/accepted/local-body-effects"]
                        (map #(str "sh-08/accepted/local-runtime-" %)
                             ["evaluated-vector" "evaluated-map" "keyword-map-lookup" "returned-map-shape"
                              "boolean-match" "structural-match" "root-map-guards"]))]
    (let [base (str "bootstrap/clojure/fixtures/self-hosting/" relative)
          source-path (path (str base ".gravity")) text (slurp source-path)
          ;; Read only the declaration here. Later fixture DATA includes forms
          ;; whose duplicate keys belong to the Gravity reader contract.
          namespace-form (binding [*read-eval* false]
                           (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))] (read reader)))
          module (bootstrap/parse-module source-path [namespace-form])]
      (is (= text (slurp (path (str base ".qst")))))
      (is (contains? (:effects module) :memory/allocate))))
  (let [base "bootstrap/clojure/fixtures/self-hosting/sh-09/rejected/undeclared-constructor-allocation"
        source-path (path (str base ".gravity")) text (slurp source-path)]
    (is (= text (slurp (path (str base ".qst")))))
    (is (= #{} (:effects (bootstrap/parse-module source-path (bootstrap/read-forms source-path text)))))))

(deftest body-allocation-authenticated-paired-composition
  ;; Reuse fresh source/type products. This directly exercises the effect owner
  ;; and whole-result verifier without rebuilding the unchanged C8 facade.
  (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/local-body-effects" %))
                       [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)]
      (assert-body-products p result)
      (let [typed (:typed p) core (get-in p [:request :canonical-core-artifact]) graph (:effect-graph result)
            constructors (filterv #(= :collection-literal (:core-form %)) (:nodes core))
            functions (into {} (map (juxt :definition-name identity) (:function-type-table typed)))
            choose-id (get-in functions ['choose :function-id])
            caller (first (filter #(= choose-id (:callee-function-syntax-id %)) (:call-type-facts typed)))]
        (is (= {:map 2 :vector 1 :set 1} (frequencies (map #(get-in % [:attributes :literal-kind]) constructors))))
        (doseq [node constructors]
          (let [record (get-in graph [:nodes (:node-id node)]) map? (= :map (get-in node [:attributes :literal-kind]))]
            (is (= #{:memory/allocate} (:direct record)))
            (is (true? (get-in record [:allocation :normal-completion-possible?])))
            (is (= :normal-operand-completion (get-in record [:allocation :condition])))
            (is (= {:unit (if map? :entries :elements) :count (if map? (quot (count (:children node)) 2) (count (:children node)))}
                   (get-in record [:allocation :extent])))))
        (is (= #{:memory/allocate} (get-in graph [:nodes (:call-core-node-id caller) :transitive])))
        (doseq [record (filter #(= :quote (:operation %)) (vals (:nodes graph)))]
          (is (nil? (:allocation record)))
          (is (= #{} (:direct record) (:transitive record))))
        (is (= :passed (:status (leaf 'verify-body-effect-result typed (:verification p) (:binding p) result))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (let [p (first products) result (first results) graph (:effect-graph result)
          id (first (keep (fn [[id record]] (when (:allocation record) id)) (:nodes graph)))
          erased (update graph :nodes
                         #(into {} (map (fn [[id record]]
                                          [id (reduce (fn [out field] (update out field disj :memory/allocate))
                                                      (dissoc record :allocation) [:direct :latent :transitive :escaping])]) %)))
          erased (update erased :functions
                         #(into {} (map (fn [[id function]] [id (-> function (update :inferred disj :memory/allocate)
                                                                              (update :latent disj :memory/allocate))]) %)))]
      (doseq [changed [erased (update-in graph [:nodes id] dissoc :allocation)
                       (assoc-in graph [:nodes id :allocation :normal-completion-possible?] false)
                       (assoc-in graph [:nodes id :allocation :extent :unit] :heap-bytes)
                       (assoc-in graph [:nodes id :allocation :provider] :observed-heap)
                       (assoc-in graph [:nodes id :allocation :extra] true)]]
        (is (= :rejected (:status (leaf 'verify-body-effect-result (:typed p) (:verification p) (:binding p)
                                       (reseal-effect-graph result changed)))))))
    (doseq [extension [".gravity" ".qst"]]
      (let [p (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/rejected/undeclared-constructor-allocation" extension))
            result (build-leaf p) diagnostic (first (:diagnostics result))]
        (is (= :accepted (get-in p [:typed :status])))
        (is (= :passed (get-in p [:verification :status])))
        (is (= [:rejected "C8-UNDECLARED" :function-effect-outside-source-allowance :memory/allocate]
               [(:status result) (:rule diagnostic) (:reason diagnostic) (:effect diagnostic)]))
        (is (= :passed (:status (leaf 'verify-body-effect-result (:typed p) (:verification p) (:binding p) result))))))))

(deftest body-effect-authenticated-paired-composition
  (let [products (mapv #(fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/local-body-effects" %))
                       [".gravity" ".qst"])
        results (mapv build-leaf products)]
    (doseq [[p result] (map vector products results)] (assert-body-products p result))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (when-not (= (:identity-input (first results)) (:identity-input (second results)))
      (throw (ex-info "Paired body identity differs before costly module loading" {})))
    (assert-coherent-body-rejections (first products) (first results))
    (doseq [extension [".gravity" ".qst"]]
      (let [rejected (fresh-products (str "bootstrap/clojure/fixtures/self-hosting/sh-09/rejected/local-body-guard" extension))]
        (is (= :rejected (get-in rejected [:typed :status])))
        (is (= "L7-GUARD-TYPE" (get-in rejected [:typed :diagnostics 0 :rule])))
        (when-not (= "L7-GUARD-TYPE" (get-in rejected [:typed :diagnostics 0 :rule]))
          (throw (ex-info "Unexpected non-Bool guard result before costly module loading" {})))))
    ;; Force the real compiler module only after source/type/leaf joins pass.
    (let [bundle (compile-c8-module-bundle (path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"))
          root (module-root-entry bundle) plan (:plan root)
          invoke (fn [function args] (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :body-effect-authenticated-development} bundle function args))]
      (is (= 6 (count (filter #(= :var (:kind %)) (:exports root)))))
      (is (= 3 (get-in plan [:functions 'sh09-build-authenticated-body-effect-result :arity])))
      (is (= 4 (get-in plan [:functions 'sh09-verify-authenticated-body-effect-result :arity])))
      (doseq [[p result] (map vector products results)]
        (let [actual (invoke 'sh09-build-authenticated-body-effect-result [(:typed p) (:verification p) (:binding p)])
              checked (invoke 'sh09-verify-authenticated-body-effect-result [(:typed p) (:verification p) (:binding p) actual])]
          (is (= result actual))
          (is (= :accepted (:status actual)))
          (is (= :passed (:status checked))))))))

(deftest current-gravity-vector-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs ((ns-resolve upstream 'vector-inequality-type-pairs))]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-vector-inequality-v1 (get-in descriptor [:vector-equality :version])))
        (is (= (:vector-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :vector-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :vector-equality)
                         (assoc-in descriptor [:vector-equality :version] :forged)
                         (assoc-in descriptor [:vector-equality :validation-mode] :first-mismatch)
                         (assoc-in descriptor [:vector-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:vector-equality :integer-type]
                                   (if (= :gravity.type/i8 (get-in descriptor [:vector-equality :integer-type])) :gravity.type/u8 :gravity.type/i8))
                         (assoc-in descriptor [:vector-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))


(deftest body-flat-integer-vector-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/vector :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string i :gravity.type/integer]
    (doseq [arguments [[(finite [i k]) (finite [i k])] [(finite [i]) (finite [i i])]
                       [(finite []) (finite [i])] [:gravity.type/never (finite [i])] [(finite [i]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-ve-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ve-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ve-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :vector-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-ve-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-ve-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :vector-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]
          (is (= :body-vector-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest current-gravity-flat-integer-vector-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        v #(hash-map :kind :gravity.type/vector :shape :literal :element-types (vec %))
        i :gravity.type/integer k :gravity.type/keyword
        pairs [[(v [i k]) (v [i k])] [(v []) (v [i])] [(v [i]) (v [i i])]
               [:gravity.type/never (v [i])] [(v [i]) :gravity.type/never]]]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-flat-integer-vector-inequality-v1 (get-in descriptor [:vector-equality :version])))
        (is (= (:vector-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :vector-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :vector-equality)
                         (assoc-in descriptor [:vector-equality :version] :forged)
                         (assoc-in descriptor [:vector-equality :validation-mode] :first-mismatch)
                         (assoc-in descriptor [:vector-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:vector-equality :integer-type]
                                   (if (= :gravity.type/i8 (get-in descriptor [:vector-equality :integer-type])) :gravity.type/u8 :gravity.type/i8))
                         (assoc-in descriptor [:vector-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))


(deftest current-gravity-vector-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [((ns-resolve upstream 'inequality-vector-type) (repeat 32 :gravity.type/keyword)) ((ns-resolve upstream 'inequality-vector-type) [])])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (is (= #{} (get-in analyzed [:graph :namespace :inferred])))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest body-character-set-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/character]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/character :gravity.type/character]} :gravity.type/character]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/character]} :gravity.type/never]
                       [:gravity.type/never :gravity.type/character]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string)]]
          (is (false? (valid candidate))))
        (when (= arguments [{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/character])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))


(deftest body-string-set-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/string]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/string :gravity.type/string]} :gravity.type/string]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/string]} :gravity.type/never]
                       [:gravity.type/never :gravity.type/string]
                       [{:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} :gravity.type/string]
                       [{:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/character)]]
          (is (false? (valid candidate))))
        (when (= arguments [{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/string])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-vector-first-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-query-input) derive (ns-resolve upstream 'list-query-derived)
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [[operation input-type] [['first {:kind :gravity.type/vector :element-types [:gravity.type/nil] :shape :literal}]
                                   ['first {:kind :gravity.type/vector :element-types [:gravity.type/bool :gravity.type/nil] :shape :literal}]
                                   ['first {:kind :gravity.type/vector :element-types [:gravity.type/character :gravity.type/character] :shape :literal}]
                                   ['first {:kind :gravity.type/vector :element-types [{:kind :gravity.type/list :shape :literal :element-types [:gravity.type/symbol :gravity.type/string]} :gravity.type/bool] :shape :literal}]]]
      (let [base (-> (input-fn operation [input-type]) (assoc-in [:module :effects] [:memory/allocate])
                     (assoc-in [:core :module :effects] [:memory/allocate])) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-lq-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-lq-constraint [descriptor])]}
            context (leaf 'body-lq-context request facts (:bindings base))
            facts (assoc facts :list-query-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)]) normal? (not= :gravity.type/never input-type)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (= [] (:latent-effects descriptor)))
        (is (nil? (:allocation record)))
        (let [operand (ids :arg0)
              inherited #{:memory/allocate :error/throw}
              seeded (assoc-in (:nodes graph) [operand :transitive] inherited)
              derived (leaf 'body-node-record (:node input) nil seeded (:type result))]
          (is (= #{} (:direct derived)))
          (is (= inherited (:transitive derived)))
          (is (nil? (:allocation derived))))
        (is (= (mapv ids (get-in base [:node :children])) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= :body-list-query-source-forms-required
               (:reason (leaf 'body-lq-context (assoc-in request [:b47-context :authenticated-core-request :forms] 7)
                          facts (:bindings base)))))
        (is (= context (leaf 'body-lq-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:bounds-proof :vector-length] 0)
                         (assoc-in descriptor [:bounds-proof :selected-index] 1)
                         (assoc-in descriptor [:bounds-proof :selected-type] :gravity.type/unknown)
                         (assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :rest] :copy)
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc descriptor :latent-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-lq-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-lq-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (is (false? (valid (update facts :constraint-ledger conj (first (:constraint-ledger facts))))))
        (is (= :body-vector-first-nonempty-complete-positional-type-required
               (:reason (leaf 'body-lq-context request
                          (assoc-in facts [:type-table (ids :arg0)] {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/character :gravity.type/unknown]})
                          (:bindings base)))))
        (when (= [:gravity.type/bool :gravity.type/nil] (:element-types input-type))
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))



              (is (= :accepted (:status (leaf 'body-analyze-module
                                          (assoc-in request [:canonical-core-artifact :module :effects] [])
                                          (assoc-in facts [:module :effects] []) {})))))))))))

(deftest body-private-entry-vector-get-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input ((ns-resolve upstream 'private-entry-vector-get-input) [[(array-map :algorithm :sha256 :status :verified)]
                                                                   [(array-map :algorithm :sha512 :status :rejected)]])
        derive (ns-resolve upstream 'map-present-get-derived) c7 (ns-resolve upstream 'invoke-c7)
        {:keys [core node nodes bindings forms types module call edge]} input
        result (derive input) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        facts {:type-table (assoc types :operator descriptor :present-get (:type result))
               :call-type-facts [(c7 'sh08-mg-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-mg-constraint [descriptor])]}
        context (leaf 'body-mg-context request facts bindings)
        facts (assoc facts :map-get-context (:context context))
        valid (fn [d] (leaf 'body-mg-present-call-valid? core
                            (assoc facts :type-table (assoc (:type-table facts) :operator d)
                                         :call-type-facts [(c7 'sh08-mg-call-fact [d module])]
                                         :constraint-ledger [(c7 'sh08-mg-constraint [d])])
                            bindings node call edge nodes))]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :accepted (:status context)))
    (is (true? (valid descriptor)))
    (let [flat (-> core (assoc :resolution-table (get-in core [:preserved-resolution :resolution-table]))
                         (update :preserved-resolution dissoc :resolution-table))]
      (is (= :rejected (:status (leaf 'body-mg-context (assoc request :canonical-core-artifact flat) facts bindings))))
      (is (false? (leaf 'body-mg-present-call-valid? flat facts bindings node call edge nodes))))
    (is (true? (leaf 'body-mg-present-call-valid?
                    (assoc core :recursion-components [{:function-syntax-ids [:caller-0]}])
                    facts bindings node call edge nodes)))
    (is (= context (leaf 'body-mg-context request facts bindings)))


    (let [good (:type result)
          bad {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/unknown]}
          ;; Public entry always rebuilds this private, same-invocation context.
          coherent (assoc (clojure.walk/postwalk-replace {good bad} facts)
                          :map-get-context (:map-get-context facts))
          changed-context (leaf 'body-mg-context request coherent bindings)]
      (is (= :rejected (:status changed-context)))
      (is (false? (leaf 'body-mg-present-call-valid? core coherent bindings node call edge nodes))))
    (doseq [id [:input :input-0 :input-1]]
      (let [changed (assoc-in facts [:type-table id :member-types 1]
                             {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/character]})]
        (is (false? (leaf 'body-mg-present-call-valid? core changed bindings node call edge nodes)))))
    (doseq [type [{:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/string]}
                  {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}
                  {:kind :gravity.type/nil-or :value-type :gravity.type/string}]]
      (let [data ((ns-resolve upstream 'finite-present-return-type-control) type)
            d (:descriptor data) core (:core data) facts {:type-table (assoc (:types data) :operator d :present-get type)
                                                         :call-type-facts [(c7 'sh08-mg-call-fact [d (:module data)])]
                                                         :constraint-ledger [(c7 'sh08-mg-constraint [d])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms data)))}}}
            context (leaf 'body-mg-context request facts (:bindings data))]
        (is (= :accepted (:status context)))
        (is (true? (leaf 'body-mg-present-call-valid? core (assoc facts :map-get-context (:context context))
                          (:bindings data) (:node data) (:call data) (:edge data) (:nodes data))))))
    (doseq [changed [(update-in descriptor [:presence-proof :entries] pop)
                     (assoc-in descriptor [:presence-proof :entries] :invalid)
                     (assoc-in descriptor [:presence-proof :entries] (vec (repeat 10 (first (get-in descriptor [:presence-proof :entries])))))
                     (assoc-in descriptor [:presence-proof :entries 1 :argument-shape :parts 0 :key] :absent)
                     (assoc-in descriptor [:presence-proof :entries 1 :selected-part :key] :absent)
                     (assoc-in descriptor [:presence-proof :entries 1 :selected-coordinate] 99)
                     (assoc-in descriptor [:presence-proof :entries 1 :argument-source] {})
                     (assoc-in descriptor [:presence-proof :entry-census :exports] ['without-path])
                     (assoc descriptor :version :sh08-map-get-present-v1)]]
      (is (false? (valid changed))))))

(deftest body-vector-index-get-map-context-compatibility-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'vector-loop-get-input) 3)
        input ((ns-resolve upstream 'vector-loop-nth-completed) base)
        result ((ns-resolve upstream 'vector-loop-nth-derived) input)
        core (:core input) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms input)))}}}
        facts {:type-table (assoc (:types input) :nth-op descriptor :read (:type result))}
        context (leaf 'body-mg-context request facts (:bindings input))]
    (is (= :accepted (:status result) (:status context)))
    (is (= :sh08-vector-get-loop-v1 (:version descriptor)))
    (is (= {} (get-in context [:context :private-selected-checks])))))

(deftest body-private-first-entry-composed-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input ((ns-resolve upstream 'private-first-entry-source-input) [])
        result ((ns-resolve upstream 'private-first-entry-derived) input)
        facts ((ns-resolve upstream 'private-first-entry-facts) input result)
        {:keys [core node call edge nodes bindings forms]} input
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        build (fn [r f] (leaf 'body-mg-context r f bindings))
        context (build request facts)
        current (assoc facts :map-get-context (:context context))
        valid (fn [c f] (leaf 'body-mg-present-call-valid? c f bindings node call edge nodes))]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :accepted (:status context)) (pr-str context))
    (when (= :accepted (:status result) (:status context))
      (is (= :sh08-map-get-private-first-entry-v1 (get-in context [:context :private-selected-checks :inner-get :version])))
      (is (true? (valid core current)))
      ;; A parent absent from the earlier loop, or a mismatched prior call,
      ;; takes the fully charged fresh path; local DATA reuse has the same result.
      (let [route (leaf 'body-pc-route core node call nodes bindings)
            parent (get-in context [:context :private-selected-checks :present-get])
            check (fn [prior]
                    (leaf 'body-pc-full-work route prior core node call edge nodes bindings
                      (:type-table facts) forms (:call-type-facts facts) (:constraint-ledger facts)))
            fresh (check nil)
            reused (check parent)]
        (is (= :accepted (:status fresh)) (pr-str fresh))
        (is (= :accepted (:status reused)) (pr-str reused))
        (is (= (:descriptor fresh) (:descriptor reused)))

        (is (= fresh (check (assoc-in parent [:source-call :core-node-id] :changed-parent))))
        (is (= fresh (check (assoc parent :kind :gravity.type/applied-vector-first)))))


      (doseq [f [(assoc-in current [:type-table :operator :key] :changed)
                 (assoc-in current [:type-table :input :member-types 1] :gravity.type/unknown)
                 (assoc-in current [:type-table :input-1 :member-types 1] :gravity.type/unknown)
                 (assoc-in current [:type-table :signatures-reference :element-types 0] :gravity.type/unknown)
                 (assoc-in current [:type-table :first-operator :bounds-proof :selected-index] 1)
                 (assoc-in current [:call-type-facts 0 :effects] [:io/write])
                 (assoc-in current [:constraint-ledger 1 :rhs] :gravity.type/string)
                 (update current :call-type-facts conj (first (:call-type-facts current)))
                 (update current :constraint-ledger conj (second (:constraint-ledger current)))]]
        (is (false? (valid core f))))
      (is (false? (valid (assoc-in core [:calls 0 :argument-node-ids 1] :inner-key) current)))
      (let [first-edge-index (first (keep-indexed #(when (= :first-call (:call-core-node-id %2)) %1) (:call-edges core)))
            payload-index (first (keep-indexed #(when (= :entry-value-0-0 (:node-id %2)) %1) (:nodes core)))]
        (is (false? (valid (assoc-in core [:call-edges first-edge-index :ordinal] 99) current)))
        (is (false? (valid (assoc-in core [:mutations] [{:target-binding-id :signature-binding}]) current)))
        (is (= :rejected (:status (build (assoc request :canonical-core-artifact
                       (assoc-in core [:nodes payload-index :attributes :quoted-value]
                         [(array-map :algorithm :sha512 :status :verified)])) facts))))
        (is (false? (leaf 'body-mg-present-call-valid? core current
                          (assoc-in bindings [:first-binding :definition-syntax-id] :changed) node call edge nodes))))
      (let [parent-index (first (keep-indexed #(when (= :present-get (:node-id %2)) %1) (:nodes core)))]
        (is (false? (valid (assoc-in core [:nodes parent-index :source :syntax-id] :changed) current))))
      (doseq [f [(assoc-in facts [:type-table :inner-operator :presence-proof :entries] [])
                 (assoc-in facts [:type-table :inner-operator :presence-proof :get-call-core-node-id] :first-call)
                 (update facts :call-type-facts #(vec (rest %)))
                 (update facts :constraint-ledger #(vec (remove (fn [r] (= :sh08-vector-first-v1 (:producer-rule r))) %)))]]
        (is (= :rejected (:status (build request f)))))
      (is (= :rejected (:status (build (assoc-in request [:b47-context :authenticated-core-request :forms]
                                               (mapv #(if (= :inner-key (:form-id %)) (assoc % :value :scheme) %) (vals forms))) facts))))
      ;; Every supplied carrier copy is checked, even when its parent ID is unchanged.
      (doseq [path [[:call-type-facts 0 :fact-id-request :application :key]
                    [:call-type-facts 0 :applied-map-get :call-core-node-id]
                    [:call-type-facts 1 :fact-id-request :application :operator-node-id]
                    [:call-type-facts 1 :applied-vector-first :result-type]
                    [:constraint-ledger 0 :constraint-id-request :application :call-core-node-id]
                    [:constraint-ledger 0 :dependencies :key-node-id]
                    [:constraint-ledger 1 :constraint-id-request :application :operator-binding-id]
                    [:constraint-ledger 1 :dependencies :profile]]]
        (let [failed (build request (assoc-in facts path :changed-copy))]
          (is (= :rejected (:status failed)))
))
      (let [changed-request (assoc-in request [:canonical-core-artifact :module :exports] ['without-path])]
        (is (= :rejected (:status (build changed-request facts))))))))

(deftest body-private-first-entry-selector-guards-development-kernel
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque selector compared")))
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque selector hashed")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque selector printed"))))
        check (fn [facts ledger]
                (leaf 'body-pc-parent-rows-work {} {} {} {} facts ledger {} :algorithm))]
    (doseq [[facts ledger]
            [[[{:call-core-node-id opaque}] []]
             [[] [{:producer-rule opaque :lhs {:core-node-id :outer :projection :map-get}}]]
             [[] [{:producer-rule :sh08-map-get-v1 :lhs {:core-node-id opaque :projection :map-get}}]]
             [[] [{:producer-rule :sh08-map-get-v1 :lhs {:core-node-id :outer :projection opaque}}]]
             [[] [{:producer-rule :sh08-map-get-v1
                   :lhs {:core-node-id :outer :projection :map-get :extra [opaque]}}]]]]
      (let [result (check facts ledger)]
        (is (= :map-get-source-mismatch (:reason result)))
        (is (zero? @callbacks))))


    (is (zero? @callbacks))))

(deftest body-private-first-entry-reference-values-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input ((ns-resolve upstream 'private-first-entry-source-input) [])
        derived ((ns-resolve upstream 'private-first-entry-derived) input)
        _ (is (= :accepted (:status derived)))
        _ (when-not (= :accepted (:status derived))
            (throw (ex-info "Private first-entry reference guard prerequisite rejected" {})))
        facts ((ns-resolve upstream 'private-first-entry-facts) input derived)
        {:keys [core node call nodes bindings]} input
        route (leaf 'body-pc-route core node call nodes bindings)
        descriptor (get (:type-table facts) (:operator-node-id (:get-call route)))
        check (fn [value] (leaf 'body-pc-carried-parent-work value :applied-map-get :algorithm))
        checked (check descriptor)
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque reference compared")))
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque reference hashed")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque reference printed"))))
        paths [[:kind] [:version] [:operation] [:semantic-owner] [:intrinsic] [:resolution]
               [:call-core-node-id] [:operator-node-id] [:operator-binding-id] [:key-node-id] [:key]
               [:application] [:profile] [:target] [:result-type] [:latent-effects] [:direct-effects]
               [:capabilities] [:thrown-error-effects] [:source :form-id] [:source :syntax-id]
               [:operator-source :form-id] [:operator-source :syntax-id]
               [:source-binding :binding-id] [:source-binding :definition-syntax-id]]]
    (is (= :accepted (:status checked)))
    (is (= (leaf 'body-pc-parent-reference descriptor) (:value checked)))


    (is (= false (get-in (check (assoc descriptor :application false)) [:value :application])))
    (is (= nil (get-in (check (assoc descriptor :key nil)) [:value :key])))
    ;; Each dynamic slot and every nested raw key/value is still walked.
    (doseq [path paths]
      (is (= :map-get-source-mismatch (:reason (check (assoc-in descriptor path opaque)))))
      (is (zero? @callbacks)))
    (doseq [value [(assoc descriptor :result-type {:kind :gravity.type/vector :element-types [opaque]})
                   (assoc descriptor :capabilities [opaque])
                   (assoc descriptor :result-type (array-map opaque :gravity.type/keyword))]]
      (is (= :map-get-source-mismatch (:reason (check value))))
      (is (zero? @callbacks)))

    (is (zero? @callbacks))))

(deftest body-flat-keyword-set-vector-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/vector :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string i :gravity.type/integer
        ks {:kind :gravity.type/set :shape :literal :element-types [k]}]
    (doseq [arguments [[(finite [ks]) (finite [ks])] [(finite [ks i k]) (finite [ks i k])]
                       [(finite []) (finite [ks])] [(finite [ks]) (finite [ks ks])]
                       [:gravity.type/never (finite [ks])] [(finite [ks]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-ve-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ve-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ve-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :vector-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-ve-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-ve-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc-in descriptor [:keyword-set-leaf-equality :per-leaf-reset] :fresh-65536)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :vector-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]
          (is (= :body-vector-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))



(deftest current-gravity-flat-keyword-set-vector-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        v #(hash-map :kind :gravity.type/vector :shape :literal :element-types (vec %))
        i :gravity.type/integer k :gravity.type/keyword
        ks {:kind :gravity.type/set :shape :literal :element-types [k]}
        pairs [[(v [ks]) (v [ks])] [(v [ks i k]) (v [ks i k])]
               [(v []) (v [ks])] [(v [ks]) (v [ks ks])]
               [:gravity.type/never (v [ks])] [(v [ks]) :gravity.type/never]]]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-flat-keyword-set-vector-inequality-v1 (get-in descriptor [:vector-equality :version])))
        (is (= (:vector-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :vector-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc-in descriptor [:vector-equality :keyword-set-leaf-equality :per-leaf-reset] :fresh-65536)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :vector-equality)
                         (assoc-in descriptor [:vector-equality :version] :forged)
                         (assoc-in descriptor [:vector-equality :validation-mode] :first-mismatch)
                         (assoc-in descriptor [:vector-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:vector-equality :integer-type]
                                   (if (= :gravity.type/i8 (get-in descriptor [:vector-equality :integer-type])) :gravity.type/u8 :gravity.type/i8))
                         (assoc-in descriptor [:vector-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest body-string-set-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/string s :gravity.type/keyword]
    (doseq [arguments [[(finite [k]) (finite [])] [(finite []) (finite [k])] [(finite [k k]) (finite [k])]
                       [:gravity.type/never (finite [k])] [(finite [k]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-kse-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-kse-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-kse-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :keyword-set-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-kse-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-kse-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :keyword-set-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]
          (is (= :body-keyword-set-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest current-gravity-string-set-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs [[((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string]) ((ns-resolve upstream 'inequality-keyword-set-type) [])]
                 [((ns-resolve upstream 'inequality-keyword-set-type) []) ((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string])]
                 [((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string :gravity.type/string]) ((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string])]
                 [((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string]) :gravity.type/never]
                 [:gravity.type/never ((ns-resolve upstream 'inequality-keyword-set-type) [:gravity.type/string])]]]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-ane-context request facts bindings)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (true? (valid checked)))
        (is (= :sh08-string-set-inequality-v1 (get-in descriptor [:string-set-equality :version])))
        (is (= (:string-set-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :string-set-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :domain-validation] :cardinality-first)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :string-set-equality)
                         (assoc-in descriptor [:string-set-equality :version] :forged)
                         (assoc-in descriptor [:string-set-equality :operand-counts] [])
                         (assoc-in descriptor [:string-set-equality :equality-policy :domain-validation] :first-mismatch)
                         (assoc-in descriptor [:string-set-equality :equality-policy :member-uniqueness] :assumed)
                         (assoc-in descriptor [:string-set-equality :equality-policy :order] :enumeration)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest body-string-set-insertion-development-kernel
  (let [s :gravity.type/string finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string}]
    (doseq [[receiver value] [[(finite []) s] [(finite [s s]) s] [(finite [s]) :gravity.type/never] [carrier s] [carrier :gravity.type/never]]]
      (let [{:keys [core node call edge nodes bindings descriptor facts request ids]} (prepend-effect-input receiver value 0)
            context (leaf 'body-va-context request facts bindings)
            facts (assoc facts :append-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (:node-id node)]) normal? (not= value :gravity.type/never)
            coherent (fn [changed]
                       (let [fact ((sh08-helper 'invoke-c7) 'sh08-pl-call-fact [changed (:module core)])
                             row ((sh08-helper 'invoke-c7) 'sh08-pl-constraint [changed])]
                         (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                             (assoc :call-type-facts [fact] :constraint-ledger [row])
                             (assoc-in [:append-context :call-facts (:node-id node)] fact)
                             (assoc-in [:append-context :conj-rows (:node-id node)] row))))]
        (is (= :accepted (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (valid facts)))
        (is (= :string-set-insertion (get-in context [:context :families (:node-id node)])))
        (is (= (if normal? carrier :gravity.type/never) (:result-type descriptor)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= :sh09-string-set-insertion-allocation-v1 (get-in record [:allocation :rule-version])))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= :runtime-value-dependent (get-in record [:allocation :extent :result-cardinality])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= context (leaf 'body-va-context request facts bindings)))

        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [changed [(assoc descriptor :kind :gravity.type/applied-list-prepend)
                         (assoc descriptor :result-type (finite [s]) :return (finite [s]))
                         (assoc descriptor :result-extent 1)
                         (assoc-in descriptor [:insertion-semantics :present-class] :reject-duplicate)
                         (assoc-in descriptor [:allocation-policy :behavior] :never)
                         (assoc-in descriptor [:storage-policy :physical-sharing] :required)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(update-in graph [:nodes (:node-id node)] dissoc :allocation)
                         (assoc-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?] (not normal?))
                         (assoc-in graph [:nodes (:node-id node) :allocation :storage-policy :physical-sharing] :required)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (:nodes changed)))))
        (is (= :rejected (:status (leaf 'body-va-context request (assoc-in facts [:type-table (ids :arg0)] :gravity.type/set) bindings))))
        (is (false? (valid (assoc-in facts [:type-table (ids :arg1)] :gravity.type/keyword))))
        (when-not normal?
          (let [operands {(:operator-node-id call) {:type descriptor :direct #{} :transitive #{}}
                          (ids :arg0) {:type receiver :direct #{:io/write} :transitive #{:io/write}}
                          (ids :arg1) {:type :gravity.type/never :direct #{:error/throw} :transitive #{:error/throw}
                                       :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
                derived (leaf 'body-node-record node nil operands :gravity.type/never)]
            (is (= #{} (:direct derived)))
            (is (= #{:io/write :error/throw} (:transitive derived)))
            (is (= [s] (:escaping-thrown-error-types derived)))))))
    (let [inputs [(prepend-effect-input (finite [s]) s 0)
                  (prepend-effect-input {:kind :gravity.type/list :shape :homogeneous :element-type s} s 1)
                  (prepend-effect-input {:kind :gravity.type/vector :shape :literal :element-types [s]} s 2)]
          core (assoc (:core (first inputs)) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs))
                      :calls (mapv :call inputs) :call-edges (mapv :edge inputs))
          forms (apply merge (map :forms inputs)) bindings (:bindings (first inputs))
          facts {:module (:module core) :type-table (apply merge (map #(get-in % [:facts :type-table]) inputs))
                 :function-type-table [] :call-type-facts (vec (mapcat #(get-in % [:facts :call-type-facts]) inputs))
                 :constraint-ledger (vec (mapcat #(get-in % [:facts :constraint-ledger]) inputs))}
          request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          context (leaf 'body-va-context request facts bindings) id (get-in inputs [0 :node :node-id])
          checked (assoc facts :append-context (:context context)) row (first (:constraint-ledger facts))]
      (is (= :accepted (:status context)) (pr-str context))
      (is (= {:string-set-insertion 1 :list-prepend 1 :vector-append 1} (frequencies (vals (get-in context [:context :families])))))
      (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
      (doseq [ledger [(subvec (:constraint-ledger facts) 1) (conj (:constraint-ledger facts) row)
                      (conj (:constraint-ledger facts) (assoc-in row [:lhs :core-node-id] :orphan))
                      (assoc (:constraint-ledger facts) 0 (assoc row :producer-rule :sh08-vector-append-v1 :lhs {:core-node-id id :projection :vector-append}))]]
        (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger ledger) bindings)))))
      (is (= :body-conj-constraint-family (:reason (leaf 'body-pl-conj-rows [row] {id :unknown-family}))))
      (let [callbacks (atom 0) opaque (proxy [Object] [] (hashCode [] (swap! callbacks inc) (throw (AssertionError. "conj row ID hash"))))]
        (doseq [changed [(assoc row :producer-rule opaque)
                         (assoc-in row [:lhs :core-node-id] opaque)
                         (assoc-in row [:lhs :core-node-id] [opaque])
                         (assoc-in row [:lhs :core-node-id] {:opaque opaque})]]
          (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger (assoc (:constraint-ledger facts) 0 changed)) bindings)))))
        (is (zero? @callbacks))))))

(deftest body-keyword-set-insertion-id-guard-development-kernel
  (let [finite {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}
        {:keys [core node call edge nodes bindings facts request ids]} (prepend-effect-input finite :gravity.type/keyword 0)
        prepared (leaf 'body-va-context request facts bindings)
        facts (assoc facts :append-context (:context prepared))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID printing"))))]
    (is (= :accepted (:status prepared)))
    (is (true? (leaf 'body-pl-call-valid? core facts bindings node call edge nodes)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:operator-node-id] [:operator-binding-id] [:argument-node-ids 0] [:argument-node-ids 1]]]
      (is (false? (leaf 'body-pl-call-valid? core facts bindings node (assoc-in call path bad) edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:node-id] [:children 0] [:children 1] [:children 2] [:source :form-id] [:source :syntax-id]]]
      (is (false? (leaf 'body-pl-call-valid? core facts bindings (assoc-in node path bad) call edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}] i (range 3)]
      (let [changed (assoc-in facts [:append-context :forms (ids :call) :child-form-ids i] bad)]
        (is (false? (leaf 'body-pl-call-valid? core changed bindings node call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (is (false? (leaf 'body-pl-call-valid? core
                        (assoc-in facts [:append-context :forms (ids :operator) :value] bad) bindings node call edge nodes)))
      (is (false? (leaf 'body-pl-call-valid? core facts bindings node call edge
                        (assoc-in nodes [(ids :operator) :attributes :symbol] bad))))
      (is (zero? @callbacks)))))

(deftest body-integer-set-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/integer]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/integer :gravity.type/integer]} :gravity.type/integer]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/integer]} :gravity.type/never]
                       [:gravity.type/never :gravity.type/integer]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string)]]
          (is (false? (valid candidate))))
        (when (= arguments [{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/integer])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-sha-suffix-character-membership-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'sha-suffix-character-membership-input)
        establish (ns-resolve upstream 'sha-suffix-character-membership-established)
        text "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        wrong (str (subs text 0 7) "g" (subs text 8))]
    (doseq [[value positions] [[text nil] [wrong nil] [text 0] [text 1] [text 2]]]
      (let [base (input value 7 71 positions) ids (zipmap (keys (:nodes base)) (map sha (range 4000 4100)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges)
            base (assoc base :core core :nodes nodes :node (nodes (ids :suffix-member)) :call (last calls) :edge (last edges)
                             :types (into {} (map (fn [[id type]] [(ids id) type]) (:types base))))
            {:keys [types child slice first-result membership]} (establish base)
            descriptors [(:descriptor child) (:descriptor slice) (:descriptor first-result) (:descriptor membership)]
            bindings (:bindings base) forms (:forms base) id (ids :suffix-member) first-id (ids :suffix-first)
            call (last calls)
            facts {:module (:module core) :type-table types :function-type-table []
                   :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                         ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-lq-call-fact 'sh08-ksm-call-fact] descriptors)
                   :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                           ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-lq-constraint 'sh08-ksm-constraint] descriptors)}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            queries (leaf 'body-lq-context request facts bindings)
            members (leaf 'body-mc-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient) :list-query-context (:context queries)
                              :map-contains-context (:context members))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes id]) first-record (get-in graph [:nodes first-id])]
        (is (= :accepted (:status child) (:status slice) (:status first-result) (:status membership)
                        (:status quotient) (:status queries) (:status members) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:direct first-record)))
        (is (nil? (:allocation record)))
        (is (nil? (:allocation first-record)))
        (is (= #{:memory/allocate} (get-in graph [:nodes (ids :suffix-hex-set) :direct])))
        (is (= (if (nil? positions) #{:memory/allocate} #{:memory/allocate :error/throw}) (:transitive record)))
        (is (= (cond (= positions 0) #{:error/throw} (nil? positions) #{:memory/allocate}
                    :else #{:memory/allocate :error/throw}) (:transitive first-record)))
        (is (= (not= positions 0) (get-in graph [:nodes (ids :call) :allocation :normal-completion-possible?])))
        (is (= (nil? positions) (get-in graph [:nodes (ids :slice-call) :allocation :normal-completion-possible?])))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 4)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index fact-name constraint-name path] [[0 'sh08-scv-call-fact 'sh08-scv-constraint [:conversion-proof :scalar-count]]
                                                       [1 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :result-count]]
                                                       [2 'sh08-lq-call-fact 'sh08-lq-constraint [:bounds-proof :vector-length]]
                                                       [3 'sh08-ksm-call-fact 'sh08-ksm-constraint [:membership-policy :comparison]]]]
          (let [changed (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (:operator-node-id changed)] changed)
                              (assoc-in [:call-type-facts index] (c7 fact-name [changed (:module core)]))
                              (assoc-in [:constraint-ledger index] (c7 constraint-name [changed])))]
            (is (false? (valid candidate)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))

(deftest body-sha-prefix-comparison-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'sha-prefix-comparison-input)
        text "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"]
    (doseq [[value positions] [[text nil] [(str "sha257:" (subs text 7)) nil] [text 0] [text 1] [text 2]]]
      (let [base (input value "sha256:" positions) ids (zipmap (keys (:nodes base)) (map sha (range 3700 3800)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges)
            bindings (:bindings base) forms (:forms base)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            child-call (first calls) child-edge (first edges) child-id (:core-node-id child-call)
            child (c7 'sh08-scv-derive [core (nodes child-id) child-call child-edge nodes bindings types forms])
            types (assoc types (:operator-node-id child-call) (:descriptor child) child-id (:type child))
            slice-call (second calls) slice-edge (second edges) slice-id (:core-node-id slice-call)
            slice (c7 'sh08-vsl-derive [core (nodes slice-id) slice-call slice-edge nodes bindings types forms])
            types (assoc types (:operator-node-id slice-call) (:descriptor slice) slice-id (:type slice))
            call (last calls) edge (last edges) id (:core-node-id call) node (nodes id)
            comparison (c7 'sh08-ve-derive [core node call edge nodes bindings types forms])
            descriptors [(:descriptor child) (:descriptor slice) (:descriptor comparison)]
            facts {:module (:module core) :type-table (assoc types (:operator-node-id call) (:descriptor comparison) id (:type comparison))
                   :function-type-table []
                   :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                         ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-ve-call-fact] descriptors)
                   :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                           ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-ve-constraint] descriptors)}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            equality (leaf 'body-ae-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient) :equality-context (:context equality))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])]
        (is (= :accepted (:status child) (:status slice) (:status comparison) (:status quotient) (:status equality) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (is (= (if (nil? positions) #{:memory/allocate} #{:memory/allocate :error/throw}) (:transitive record)))
        (is (= (get-in call [:ordered-evaluation-node-ids]) (:dependencies record) (:evaluated-children record)))
        (is (= #{:memory/allocate} (get-in graph [:nodes (ids :prefix-expected) :direct])))
        (is (= (not= positions 0) (get-in graph [:nodes child-id :allocation :normal-completion-possible?])))
        (is (= (nil? positions) (get-in graph [:nodes slice-id :allocation :normal-completion-possible?])))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (is (= equality (leaf 'body-ae-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 3)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index operator fact-name constraint-name path] [[0 :operator 'sh08-scv-call-fact 'sh08-scv-constraint [:conversion-proof :scalar-count]]
                                                                 [1 :slice-operator 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :result-count]]
                                                                 [2 :prefix-operator 'sh08-ve-call-fact 'sh08-ve-constraint [:equality-policy :leaves]]]]
          (let [changed (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (ids operator)] changed)
                              (assoc-in [:call-type-facts index] (c7 fact-name [changed (:module core)]))
                              (assoc-in [:constraint-ledger index] (c7 constraint-name [changed])))]
            (is (false? (valid candidate)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))

(defn- let-vector-slice-first-membership-body-products [value members positions]
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        base ((ns-resolve upstream 'let-vector-slice-first-membership-input) value members (if (nil? positions) 0 20) 1 positions)
        ids (zipmap (keys (:nodes base)) (map sha (range 8000 (+ 8000 (count (:nodes base))))))
        convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                          (update-in [:evaluation :order] (fn [xs] (mapv (fn [entry] (update entry :core-node-id ids)) xs))))
        convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                           (update :argument-node-ids (fn [xs] (mapv ids xs))) (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                           (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
        calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
        records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids)) (get-in base [:core :lexical-bindings]))
        core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
        bindings (:bindings base) forms (:forms base) module (:module core)
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derive (fn [name id types] (let [call (first (filter #(= id (:core-node-id %)) calls))
                                         edge (first (filter #(= id (:call-core-node-id %)) edges))
                                         args [core (nodes id) call edge nodes bindings types forms]]
                                     (c7 name args)))
        child (when (:child base) (derive 'sh08-scv-derive (ids :call) types))
        types (if child (assoc types (ids :operator) (:descriptor child) (ids :call) (:type child) (ids :slice-ref) (:type child)) types)
        slice (derive 'sh08-vsl-derive (ids :slice-call) types)
        types (assoc types (ids :slice-operator) (:descriptor slice) (ids :slice-call) (:type slice))
        query (derive 'sh08-lq-derive (ids :let-first-call) types)
        types (assoc types (ids :let-first-operator) (:descriptor query) (ids :let-first-call) (:type query) (ids :slice-let) (:type query))
        membership (derive 'sh08-ksm-derive (ids :let-member-call) types)
        types (assoc types (ids :let-member-operator) (:descriptor membership) (ids :let-member-call) (:type membership) (ids :slice-let) (:type membership))
        descriptors (if child [(:descriptor child) (:descriptor slice) (:descriptor query) (:descriptor membership)] [(:descriptor slice) (:descriptor query) (:descriptor membership)])
        fact-names (if child ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-lq-call-fact 'sh08-ksm-call-fact] ['sh08-vsl-call-fact 'sh08-lq-call-fact 'sh08-ksm-call-fact])
        row-names (if child ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-lq-constraint 'sh08-ksm-constraint] ['sh08-vsl-constraint 'sh08-lq-constraint 'sh08-ksm-constraint])
        facts {:module module :type-table types :function-type-table []
               :local-binding-facts (c7 'sh08-ft-local-facts [records types {:slice-let-binding (types (ids :call))} nodes module])
               :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor module])) fact-names descriptors)
               :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor])) row-names descriptors)}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}]
    {:core core :nodes nodes :bindings bindings :forms forms :module module :ids ids :child child :slice slice :query query :membership membership
     :descriptors descriptors :facts facts :request request :fact-names fact-names :row-names row-names}))

(deftest body-let-vector-slice-first-membership-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        hex (vec "0123456789abcdef")]
    (doseq [[value members positions] [["sha256:a" hex nil] ["sha256:g" hex nil] ["a" [] nil]
                                      ["a" hex 0] ["a" hex 1] ["a" hex 2]
                                      ["a" [] 0] ["a" [] 1] ["a" [] 2] [[65 66] [] 1]]]
      (let [{:keys [core nodes bindings module ids child slice query membership descriptors facts request fact-names row-names]}
            (let-vector-slice-first-membership-body-products value members positions)
            contexts (fn [facts] (assoc facts :quotient-context (:context (leaf 'body-nq-context request facts bindings))
                                            :list-query-context (:context (leaf 'body-lq-context request facts bindings))
                                            :map-contains-context (:context (leaf 'body-mc-context request facts bindings))))
            facts (contexts facts) valid #(leaf 'body-call-joins-with-foreign-valid? core (contexts %) bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            id (ids :let-member-call) first-id (ids :let-first-call) record (get-in graph [:nodes id]) first-record (get-in graph [:nodes first-id])
            n (count descriptors) slice-index (- n 3) query-index (- n 2) member-index (dec n)]
        (is (= :accepted (:status slice) (:status query) (:status membership) (:status graph)) (pr-str graph))
        (when child (is (= :accepted (:status child))))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:direct first-record) (get-in graph [:nodes (ids :slice-let) :direct])))
        (is (nil? (:allocation record)))
        (is (nil? (:allocation first-record)))
        (is (= #{:memory/allocate} (get-in graph [:nodes (ids :let-member-set) :direct])))
        (is (= [(ids :let-member-operator) (ids :let-member-set) first-id] (:dependencies record) (:evaluated-children record)))
        (is (= (if (contains? #{nil 0} positions) #{:memory/allocate} #{:memory/allocate :error/throw}) (:transitive record)))
        (is (= (if (nil? positions) #{:memory/allocate} #{:memory/allocate :error/throw})
               (get-in graph [:nodes (ids :slice-let) :transitive])))
        (is (= (cond (= positions 0) #{} (nil? positions) #{:memory/allocate} :else #{:error/throw})
               (:transitive first-record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [field [:call-type-facts :constraint-ledger] index (range n)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index operator path] (concat [[query-index :let-first-operator [:storage-policy :selection]]
                                              [member-index :let-member-operator [:membership-policy :comparison]]]
                                             (when-not (= positions 0)
                                               [[query-index :let-first-operator [:bounds-proof :selected-index]]
                                                [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :node :evaluation :order]]
                                                [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :membership-container :node :evaluation :order]]
                                                [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :membership-container :set-node :attributes :source-value]]
                                                [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :membership-container :source-forms]]]))]
          (let [descriptor (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (ids operator)] descriptor)
                              (assoc-in [:call-type-facts index] (c7 (get fact-names index) [descriptor module]))
                              (assoc-in [:constraint-ledger index] (c7 (get row-names index) [descriptor])))]
            (is (false? (valid candidate)))))
        (when (nil? positions)
          (is (false? (valid (assoc-in facts [:type-table (ids :let-member-set) :element-types] [:gravity.type/string])))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))


(defn- let-vector-slice-first-body-products [value positions]
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        base ((ns-resolve upstream 'let-vector-slice-first-input) value (if (nil? positions) 1 20) (if (nil? positions) 2 1) positions)
        ids (zipmap (keys (:nodes base)) (map sha (range 6800 6900)))
        convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                          (update-in [:evaluation :order] (fn [xs] (mapv (fn [entry] (update entry :core-node-id ids)) xs))))
        convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                           (update :argument-node-ids (fn [xs] (mapv ids xs))) (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                           (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
        calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
        records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids)) (get-in base [:core :lexical-bindings]))
        core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
        bindings (:bindings base) forms (:forms base) module (:module core)
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derive (fn [name id types] (let [call (first (filter #(= id (:core-node-id %)) calls))
                                         edge (first (filter #(= id (:call-core-node-id %)) edges))
                                         args [core (nodes id) call edge nodes bindings types forms]]
                                     (c7 name args)))
        child (when (:child base) (derive 'sh08-scv-derive (ids :call) types))
        types (if child (assoc types (ids :operator) (:descriptor child) (ids :call) (:type child) (ids :slice-ref) (:type child)) types)
        slice (derive 'sh08-vsl-derive (ids :slice-call) types)
        types (assoc types (ids :slice-operator) (:descriptor slice) (ids :slice-call) (:type slice))
        query (derive 'sh08-lq-derive (ids :let-first-call) types)
        types (assoc types (ids :let-first-operator) (:descriptor query) (ids :let-first-call) (:type query) (ids :slice-let) (:type query))
        descriptors (if child [(:descriptor child) (:descriptor slice) (:descriptor query)] [(:descriptor slice) (:descriptor query)])
        fact-names (if child ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-lq-call-fact] ['sh08-vsl-call-fact 'sh08-lq-call-fact])
        row-names (if child ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-lq-constraint] ['sh08-vsl-constraint 'sh08-lq-constraint])
        facts {:module module :type-table types :function-type-table []
               :local-binding-facts (c7 'sh08-ft-local-facts [records types {:slice-let-binding (types (ids :call))} nodes module])
               :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor module])) fact-names descriptors)
               :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor])) row-names descriptors)}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}]
    {:core core :nodes nodes :bindings bindings :forms forms :module module :ids ids :child child :slice slice :query query
     :descriptors descriptors :facts facts :request request :fact-names fact-names :row-names row-names}))

(deftest body-let-vector-slice-first-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)]
    (doseq [value ["abc" [65 66 67]] positions [nil 0 1 2]]
      (let [{:keys [core nodes bindings module ids child slice query descriptors facts request fact-names row-names]}
            (let-vector-slice-first-body-products value positions)
            contexts (fn [facts] (assoc facts :quotient-context (:context (leaf 'body-nq-context request facts bindings))
                                            :equality-context (:context (leaf 'body-ae-context request facts bindings))
                                            :list-query-context (:context (leaf 'body-lq-context request facts bindings))))
            facts (contexts facts) valid #(leaf 'body-call-joins-with-foreign-valid? core (contexts %) bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            id (ids :let-first-call) record (get-in graph [:nodes id])
            n (count descriptors) slice-index (- n 2) query-index (dec n)]
        (is (= :accepted (:status slice) (:status query) (:status graph)) (pr-str graph))
        (when child (is (= :accepted (:status child))))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (get-in graph [:nodes (ids :slice-let) :direct])))
        (is (nil? (:allocation record)))
        (is (= [(ids :let-first-operator) (ids :slice-call)] (:dependencies record) (:evaluated-children record)))
        (is (= (:transitive record) (get-in graph [:nodes (ids :slice-call) :transitive])))
        (is (= (cond (nil? positions) #{:memory/allocate} (= positions 0) #{:error/throw} :else #{:memory/allocate :error/throw})
               (get-in graph [:nodes (ids :slice-let) :transitive])))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [field [:call-type-facts :constraint-ledger] index (range n)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index operator path] (concat [[query-index :let-first-operator [:storage-policy :selection]]]
                                              (when-not (= positions 0)
                                                [[query-index :let-first-operator [:bounds-proof :selected-index]]
                                                 [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :node :evaluation :order]]
                                                 [slice-index :slice-operator [:bounds-proof :receiver :body-consumer :source-forms]]]))]
          (let [descriptor (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (ids operator)] descriptor)
                              (assoc-in [:call-type-facts index] (c7 (get fact-names index) [descriptor module]))
                              (assoc-in [:constraint-ledger index] (c7 (get row-names index) [descriptor])))]
            (is (false? (valid candidate)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))


(deftest body-let-vector-slice-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'let-vector-slice-equality-input)
        text "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"]
    (doseq [[value positions] [[text nil] [(str "sha257:" (subs text 7)) nil] [text 0] [text 1] [text 2]]]
      (let [base (input value "sha256:" positions) ids (zipmap (keys (:nodes base)) (map sha (range 6200 6300)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids)) (get-in base [:core :lexical-bindings]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
            bindings (:bindings base) forms (:forms base)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            child-call (first calls) child-edge (first edges) child-id (:core-node-id child-call)
            child (c7 'sh08-scv-derive [core (nodes child-id) child-call child-edge nodes bindings types forms])
            types (assoc types (:operator-node-id child-call) (:descriptor child) child-id (:type child) (ids :slice-ref) (:type child))
            slice-call (second calls) slice-edge (second edges) slice-id (:core-node-id slice-call)
            slice (c7 'sh08-vsl-derive [core (nodes slice-id) slice-call slice-edge nodes bindings types forms])
            types (assoc types (:operator-node-id slice-call) (:descriptor slice) slice-id (:type slice))
            call (last calls) edge (last edges) id (:core-node-id call) node (nodes id)
            comparison (c7 'sh08-ve-derive [core node call edge nodes bindings types forms])
            descriptors [(:descriptor child) (:descriptor slice) (:descriptor comparison)]
            facts {:module (:module core) :type-table (assoc types (:operator-node-id call) (:descriptor comparison) id (:type comparison) (ids :slice-let) (:type comparison))
                   :local-binding-facts (c7 'sh08-ft-local-facts [records types {:slice-let-binding (:type child)} nodes (:module core)])
                   :function-type-table []
                   :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                         ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-ve-call-fact] descriptors)
                   :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                           ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-ve-constraint] descriptors)}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            equality (leaf 'body-ae-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient) :equality-context (:context equality))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])]
        (is (= :accepted (:status child) (:status slice) (:status comparison) (:status quotient) (:status equality) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (get-in graph [:nodes (ids :slice-ref) :direct]) (get-in graph [:nodes (ids :slice-let) :direct])))
        (is (nil? (get-in graph [:nodes (ids :slice-ref) :allocation])))
        (is (nil? (get-in graph [:nodes (ids :slice-let) :allocation])))
        ;; Graph unions are conservative; runtime traces check actual abort order.
        (is (= (if (nil? positions) #{:memory/allocate} #{:memory/allocate :error/throw})
               (get-in graph [:nodes (ids :slice-let) :transitive])))
        (is (nil? (:allocation record)))
        (is (= (if (contains? #{nil 0} positions) #{:memory/allocate} #{:memory/allocate :error/throw}) (:transitive record)))
        (is (= (get-in call [:ordered-evaluation-node-ids]) (:dependencies record) (:evaluated-children record)))
        (is (= #{:memory/allocate} (get-in graph [:nodes (ids :prefix-expected) :direct])))
        (is (= (not= positions 0) (get-in graph [:nodes child-id :allocation :normal-completion-possible?])))
        (is (= (nil? positions) (get-in graph [:nodes slice-id :allocation :normal-completion-possible?])))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (is (= equality (leaf 'body-ae-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 3)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index operator fact-name constraint-name path] [[0 :operator 'sh08-scv-call-fact 'sh08-scv-constraint [:conversion-proof :scalar-count]]
                                                                 [1 :slice-operator 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :result-count]]
                                                                 [2 :prefix-operator 'sh08-ve-call-fact 'sh08-ve-constraint [:equality-policy :leaves]]]]
          (let [changed (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (ids operator)] changed)
                              (assoc-in [:call-type-facts index] (c7 fact-name [changed (:module core)]))
                              (assoc-in [:constraint-ledger index] (c7 constraint-name [changed])))]
            (is (false? (valid candidate)))))
        (when-not (= positions 0)
          (doseq [path [[:bounds-proof :receiver :body-consumer :node :evaluation :order]
                        [:bounds-proof :receiver :body-consumer :right :node-id]
                        [:bounds-proof :receiver :body-consumer :literal-type]]]
            (let [changed (assoc-in (:descriptor slice) path :forged)
                  candidate (-> facts (assoc-in [:type-table (ids :slice-operator)] changed)
                                (assoc-in [:call-type-facts 1] (c7 'sh08-vsl-call-fact [changed (:module core)]))
                                (assoc-in [:constraint-ledger 1] (c7 'sh08-vsl-constraint [changed])))]
              (is (false? (valid candidate))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))

(deftest body-let-prefix-gate-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'let-prefix-gate-input)
        text "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"]
    (doseq [[value positions] [[text nil] [(str "sha257:" (subs text 7)) nil] [text 0] [text 1] [text 2]]]
      (let [base (input value "sha256:" true false positions) ids (zipmap (keys (:nodes base)) (map sha (range 6400 6500)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids)) (get-in base [:core :lexical-bindings]))
            controls (mapv #(-> % (update :core-node-id ids) (update :condition-node-id ids)
                                (update :branches (fn [branches] (mapv (fn [branch] (update branch :core-node-id ids)) branches)))) (get-in base [:core :control-flow]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records :control-flow controls)
            bindings (:bindings base) forms (:forms base)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            child-call (first calls) child-edge (first edges) child-id (:core-node-id child-call)
            child (c7 'sh08-scv-derive [core (nodes child-id) child-call child-edge nodes bindings types forms])
            types (assoc types (:operator-node-id child-call) (:descriptor child) child-id (:type child) (ids :slice-ref) (:type child))
            slice-call (second calls) slice-edge (second edges) slice-id (:core-node-id slice-call)
            slice (c7 'sh08-vsl-derive [core (nodes slice-id) slice-call slice-edge nodes bindings types forms])
            types (assoc types (:operator-node-id slice-call) (:descriptor slice) slice-id (:type slice))
            call (last calls) edge (last edges) id (:core-node-id call) node (nodes id)
            comparison (c7 'sh08-ve-derive [core node call edge nodes bindings types forms])
            types (assoc types (:operator-node-id call) (:descriptor comparison) id (:type comparison))
            gate-type (c7 'sh08-ft-infer-node-with-context [(nodes (ids :prefix-if)) types {} {} edges {}])
            descriptors [(:descriptor child) (:descriptor slice) (:descriptor comparison)]
            facts {:module (:module core) :type-table (assoc types (ids :prefix-if) gate-type (ids :slice-let) gate-type)
                   :local-binding-facts (c7 'sh08-ft-local-facts [records types {:slice-let-binding (:type child)} nodes (:module core)])
                   :function-type-table []
                   :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                         ['sh08-scv-call-fact 'sh08-vsl-call-fact 'sh08-ve-call-fact] descriptors)
                   :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                           ['sh08-scv-constraint 'sh08-vsl-constraint 'sh08-ve-constraint] descriptors)}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            equality (leaf 'body-ae-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient) :equality-context (:context equality))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id]) gate-record (get-in graph [:nodes (ids :prefix-if)])]
        (is (= :accepted (:status child) (:status slice) (:status comparison) (:status quotient) (:status equality) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct gate-record)))
        (is (nil? (:allocation gate-record)))
        (is (= [id] (:evaluated-children gate-record)))
        (is (= [id (ids :prefix-then) (ids :prefix-else)] (:dependencies gate-record)))
        (is (= [{:node-id (ids :prefix-then) :child-index 1 :role :then} {:node-id (ids :prefix-else) :child-index 2 :role :else}] (:conditional-coverage gate-record)))
        (is (= (:transitive record) (:transitive gate-record)))
        (is (= #{} (:direct record) (get-in graph [:nodes (ids :slice-ref) :direct]) (get-in graph [:nodes (ids :slice-let) :direct])))
        (is (nil? (get-in graph [:nodes (ids :slice-ref) :allocation])))
        (is (nil? (get-in graph [:nodes (ids :slice-let) :allocation])))
        ;; Graph unions are conservative; runtime traces check actual abort order.
        (is (= (if (nil? positions) #{:memory/allocate} #{:memory/allocate :error/throw})
               (get-in graph [:nodes (ids :slice-let) :transitive])))
        (is (nil? (:allocation record)))
        (is (= (if (contains? #{nil 0} positions) #{:memory/allocate} #{:memory/allocate :error/throw}) (:transitive record)))
        (is (= (get-in call [:ordered-evaluation-node-ids]) (:dependencies record) (:evaluated-children record)))
        (is (= #{:memory/allocate} (get-in graph [:nodes (ids :prefix-expected) :direct])))
        (is (= (not= positions 0) (get-in graph [:nodes child-id :allocation :normal-completion-possible?])))
        (is (= (nil? positions) (get-in graph [:nodes slice-id :allocation :normal-completion-possible?])))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (is (= equality (leaf 'body-ae-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 3)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (doseq [[index operator fact-name constraint-name path] [[0 :operator 'sh08-scv-call-fact 'sh08-scv-constraint [:conversion-proof :scalar-count]]
                                                                 [1 :slice-operator 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :result-count]]
                                                                 [2 :prefix-operator 'sh08-ve-call-fact 'sh08-ve-constraint [:equality-policy :leaves]]]]
          (let [changed (assoc-in (get descriptors index) path :forged)
                candidate (-> facts (assoc-in [:type-table (ids operator)] changed)
                              (assoc-in [:call-type-facts index] (c7 fact-name [changed (:module core)]))
                              (assoc-in [:constraint-ledger index] (c7 constraint-name [changed])))]
            (is (false? (valid candidate)))))
        (when-not (= positions 0)
          (doseq [path [[:bounds-proof :receiver :body-consumer :node :evaluation :order]
                        [:bounds-proof :receiver :body-consumer :right :node-id]
                        [:bounds-proof :receiver :body-consumer :literal-type]
                        [:bounds-proof :receiver :body-consumer :condition-container :control :branch-exclusivity]
                        [:bounds-proof :receiver :body-consumer :condition-container :else :evaluation :region :role]]]
            (let [changed (assoc-in (:descriptor slice) path :forged)
                  candidate (-> facts (assoc-in [:type-table (ids :slice-operator)] changed)
                                (assoc-in [:call-type-facts 1] (c7 'sh08-vsl-call-fact [changed (:module core)]))
                                (assoc-in [:constraint-ledger 1] (c7 'sh08-vsl-constraint [changed])))]
              (is (false? (valid candidate))))))
        (when-not (= positions 0)
          (doseq [mutate [#(assoc-in % [:canonical-core-artifact :control-flow] [])
                          #(update-in % [:canonical-core-artifact :control-flow] conj (first controls))
                          #(assoc-in % [:canonical-core-artifact :control-flow 0 :branches 1 :role] :then)]]
            (is (= :rejected (:status (leaf 'body-nq-context (mutate request) facts bindings))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [id :direct] #{:memory/allocate}))))))))

(defn- parameter-vector-count-slice-body-products
  ([text start]
   (require 'gravity.self-hosting.sh08-function-call-type-test)
   (parameter-vector-count-slice-body-products ((sh08-helper 'parameter-vector-count-slice-products) text start)))
  ([input]
  (require 'gravity.self-hosting.sh08-function-call-type-test 'clojure.walk)
  (let [ids (zipmap (keys (:nodes input)) (map sha (range 8400 8500)))
        scalar-fields [:node-id :core-node-id :call-core-node-id :operator-node-id :function-core-node-id
                       :body-core-node-id :definition-core-node-id :value-node-id :initializer-node-id :let-core-node-id
                       :caller-function-core-node-id :callee-function-core-node-id :receiver-node-id :argument-node-id :condition-node-id]
        vector-fields [:children :argument-node-ids :ordered-evaluation-node-ids :argument-core-node-ids]
        convert (fn [value]
                  (if (map? value)
                    (let [value (reduce (fn [out key] (if (contains? out key) (update out key #(get ids % %)) out)) value scalar-fields)
                          value (reduce (fn [out key] (if (vector? (get out key))
                                                       (update out key #(mapv (fn [id] (get ids id id)) %)) out)) value vector-fields)]
                      (reduce (fn [out key]
                                (if (map? (get out key))
                                  (update out key #(into {} (map (fn [[id item]] [(get ids id id) item])) %)) out))
                              value [:nodes :types :type-table])) value))
        converted (clojure.walk/postwalk convert input)]
    (assoc converted :ids ids))))

(defn- let-vector-slice-call-body-products [text start end callee-start]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (parameter-vector-count-slice-body-products
    ((sh08-helper 'let-vector-slice-call-products) text start end callee-start)))

(defn- prefix-gated-slice-call-body-products [text expected start end]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (parameter-vector-count-slice-body-products ((sh08-helper 'prefix-gated-slice-call-products) text expected start end)))

(deftest body-prefix-gated-slice-call-development-kernel
  ;; Conditional normalized C7 facts; full public C8 intake remains separate.
  (doseq [[text expected start end] [["sha256:0af" "sha256:" 7 10] ["sha257:0af" "sha256:" 7 10] ["" "" 0 0]]]
    (let [{:keys [core bindings request facts inference ids]} (prefix-gated-slice-call-body-products text expected start end)
          fresh (fn [candidate]
                  (let [cq (leaf 'body-cq-context request candidate bindings)
                        candidate (assoc candidate :count-context (:context cq))
                        nq (leaf 'body-nq-context request candidate bindings)
                        candidate (assoc candidate :quotient-context (:context nq))
                        ae (leaf 'body-ae-context request candidate bindings)]
                    [cq nq ae (assoc candidate :equality-context (:context ae))]))
          [cq nq ae complete] (fresh facts)
          valid (fn [candidate]
                  (let [[cq nq ae candidate] (fresh candidate)]
                    (and (= :accepted (:status cq) (:status nq) (:status ae))
                         (leaf 'body-function-joins-valid? core candidate bindings)
                         (leaf 'body-local-joins-valid? core candidate bindings)
                         (leaf 'body-call-joins-with-foreign-valid? core candidate bindings {}))))
          graph (leaf 'body-derive-nodes-with-foreign core complete {})
          gate (get-in graph [:nodes (ids :prefix-if)])
          entry (first (filter #(= (ids :parameter-entry) (:node-id %)) (:nodes core)))]
      (is (= :converged (:convergence-status inference)))
      (is (= :accepted (:status cq) (:status nq) (:status ae) (:status graph)))
      (is (true? (valid facts)))
      (is (= [(ids :prefix-call)] (:evaluated-children gate)))
      (is (= [(ids :prefix-call) (ids :parameter-entry) (ids :prefix-else)] (:dependencies gate)))
      (is (= [{:node-id (ids :parameter-entry) :child-index 1 :role :then}
              {:node-id (ids :prefix-else) :child-index 2 :role :else}] (:conditional-coverage gate)))
      (is (= #{} (:direct gate) (get-in graph [:nodes (ids :parameter-entry) :direct])))
      (is (= #{:memory/allocate} (:transitive gate) (get-in graph [:nodes (ids :parameter-entry) :transitive])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :gate-slice-call) :direct])
             (get-in graph [:nodes (ids :argument-slice-call) :direct])))
      (is (true? (leaf 'body-call-invokes? entry (:type-table facts))))
      (is (false? (leaf 'body-call-invokes? entry (assoc (:type-table facts) (ids :argument-slice-call) :gravity.type/never))))
      (is (true? (leaf 'body-graph-equations-valid? core complete {} (:nodes graph))))
      (doseq [id [:gate-slice-call :prefix-call :argument-slice-call :parameter-entry]]
        (is (false? (valid (update facts :call-type-facts
                            (fn [rows] (vec (remove (fn [row] (= (ids id) (:call-core-node-id row))) rows))))))))
      (doseq [operator [:gate-slice-operator :argument-slice-operator]]
        (is (false? (valid (assoc-in facts [:type-table (ids operator) :bounds-proof :receiver :body-consumer :condition-container :then :evaluation :region :role] :else)))))
      (is (= nq (leaf 'body-nq-context request (assoc facts :count-context (:context cq)) bindings)))
      (is (= :rejected (:status (leaf 'body-nq-context request (assoc facts :count-context (:context cq)) bindings))))
      (is (= ae (leaf 'body-ae-context request (assoc facts :quotient-context (:context nq)) bindings)))
      (is (= :rejected (:status (leaf 'body-ae-context request (assoc facts :quotient-context (:context nq)) bindings)))))))

(deftest body-let-vector-slice-call-argument-development-kernel
  (doseq [[text start end callee-start] [["sha256:0af" 7 10 1] ["f" 0 1 1] ["" 0 0 0]]]
    (let [{:keys [core bindings request facts inference ids]} (let-vector-slice-call-body-products text start end callee-start)
          fresh (fn [candidate]
                  (let [cq (leaf 'body-cq-context request candidate bindings)
                        candidate (assoc candidate :count-context (:context cq))
                        nq (leaf 'body-nq-context request candidate bindings)]
                    [cq nq (assoc candidate :quotient-context (:context nq))]))
          [cq nq complete] (fresh facts)
          valid (fn [candidate]
                  (let [[cq nq candidate] (fresh candidate)]
                    (and (= :accepted (:status cq) (:status nq))
                         (leaf 'body-function-joins-valid? core candidate bindings)
                         (leaf 'body-local-joins-valid? core candidate bindings)
                         (leaf 'body-call-joins-with-foreign-valid? core candidate bindings {}))))
          graph (leaf 'body-derive-nodes-with-foreign core complete {})
          entry (first (filter #(= (ids :parameter-entry) (:node-id %)) (:nodes core)))]
      (is (= :converged (:convergence-status inference)))
      (is (= :accepted (:status cq) (:status nq) (:status graph)))
      (is (true? (valid facts)))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :argument-slice-call) :direct])))
      (is (= #{} (get-in graph [:nodes (ids :parameter-entry) :direct])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :parameter-entry) :transitive])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :argument-let) :transitive])))
      (is (true? (leaf 'body-call-invokes? entry (:type-table facts))))
      (is (false? (leaf 'body-call-invokes? entry (assoc (:type-table facts) (ids :argument-slice-call) :gravity.type/never))))
      (is (true? (leaf 'body-graph-equations-valid? core complete {} (:nodes graph))))
      (doseq [mutate [#(update % :call-type-facts (fn [rows] (vec (remove (fn [row] (= (ids :parameter-entry) (:call-core-node-id row))) rows))))
                      #(update % :constraint-ledger conj (first (filter (fn [row] (= {:core-node-id (ids :argument-slice-call) :projection :vector-slice} (:lhs row))) (:constraint-ledger %))))
                      #(assoc-in % [:type-table (ids :argument-slice-operator) :bounds-proof :receiver :body-consumer :edge :callee-function-syntax-id] :sibling)
                      #(assoc-in % [:local-binding-facts 0 :type] :gravity.type/string)]]
        (is (false? (valid (mutate facts)))))
      (is (= nq (leaf 'body-nq-context request (assoc facts :count-context (:context cq)) bindings)))
      (is (= :rejected (:status (leaf 'body-nq-context request (assoc facts :count-context (:context cq)) bindings)))))))

(deftest body-parameter-vector-count-slice-development-kernel
  ;; These are original caller-derived normalized products, not C7 authority.
  (doseq [[text start] [["0af" 1] ["0" 1] ["" 0]]]
    (let [{:keys [core bindings request facts inference ids]} (parameter-vector-count-slice-body-products text start)
          fresh (fn [candidate]
                  (let [counts (leaf 'body-cq-context request candidate bindings)
                        candidate (assoc candidate :count-context (:context counts))
                        quotient (leaf 'body-nq-context request candidate bindings)]
                    [counts quotient (assoc candidate :quotient-context (:context quotient))]))
          [counts quotient complete] (fresh facts)
          valid (fn [candidate]
                  (let [[cq nq candidate] (fresh candidate)]
                    (and (= :accepted (:status cq) (:status nq))
                         (leaf 'body-function-joins-valid? core candidate bindings)
                         (leaf 'body-call-joins-with-foreign-valid? core candidate bindings {}))))
          graph (leaf 'body-derive-nodes-with-foreign core complete {})]
      (is (= :converged (:convergence-status inference)))
      (is (= :accepted (:status counts) (:status quotient) (:status graph)))
      (is (true? (valid facts)))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :slice-call) :direct])))
      (is (= #{} (get-in graph [:nodes (ids :slice-end) :direct])))
      (is (nil? (get-in graph [:nodes (ids :slice-end) :allocation])))
      (is (true? (leaf 'body-graph-equations-valid? core complete {} (:nodes graph))))
      (doseq [mutate [#(update % :call-type-facts pop)
                      #(update % :constraint-ledger conj (first (filter (fn [row] (= :sh08-vector-slice-v1 (:producer-rule row))) (:constraint-ledger %))))
                      #(assoc-in % [:type-table (ids :parameter-fn) :parameters 0 :type] :gravity.type/string)
                      #(assoc-in % [:function-type-table 0 :parameters 0 :binding-id] :sibling)]]
        (is (false? (valid (mutate facts)))))
      (is (= quotient (leaf 'body-nq-context request (assoc facts :count-context (:context counts)) bindings)))
      (is (= :rejected (:status (leaf 'body-nq-context request (assoc facts :count-context (:context counts)) bindings)))))))

(defn- let-vector-count-slice-body-products [text start position]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        c7 (ns-resolve upstream 'invoke-c7)
        base ((ns-resolve upstream 'let-vector-count-slice-input) text start position)
        ids (zipmap (keys (:nodes base)) (map sha (range 8000 8100)))
        convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                          (update-in [:evaluation :order] (fn [xs] (mapv (fn [x] (update x :core-node-id ids)) xs))))
        convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                          (update :argument-node-ids (fn [xs] (mapv ids xs)))
                          (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                          (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        nodes (into {} (map (juxt :node-id identity) node-list))
        calls (mapv convert-call (get-in base [:core :calls]))
        edges (mapv convert-edge (get-in base [:core :call-edges]))
        records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids))
                      (get-in base [:core :lexical-bindings]))
        core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
        forms (:forms base) bindings (:bindings base)
        initial-types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derived (reduce
                 (fn [{:keys [types results]} [name call edge]]
                   (let [args [core (nodes (:core-node-id call)) call edge nodes bindings types forms]
                         result (c7 name args)]
                     {:types (assoc types (:operator-node-id call) (:descriptor result) (:core-node-id call) (:type result))
                      :results (conj results result)}))
                 {:types initial-types :results []}
                 (map vector ['sh08-scv-derive 'sh08-cq-derive 'sh08-vsl-derive] calls edges))
        results (:results derived) descriptors (mapv :descriptor results)
        types (assoc (:types derived) (ids :slice-let) (:type (last results)))
        facts {:module (:module core) :function-type-table [] :type-table types
               :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                     ['sh08-scv-call-fact 'sh08-cq-call-fact 'sh08-vsl-call-fact] descriptors)
               :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                       ['sh08-scv-constraint 'sh08-cq-constraint 'sh08-vsl-constraint] descriptors)
               :local-binding-facts (c7 'sh08-ft-local-facts
                                       [records types {:slice-let-binding (types (ids :call))} nodes (:module core)])}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}]
    {:core core :nodes nodes :calls calls :edges edges :forms forms :bindings bindings :ids ids
     :facts facts :request request :results results :descriptors descriptors}))

(defn- let-vector-count-slice-first-body-products [text start position]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        c7 (ns-resolve upstream 'invoke-c7)
        base ((ns-resolve upstream 'let-vector-count-slice-first-input) text start position)
        ids (zipmap (keys (:nodes base)) (map sha (range 8000 8100)))
        convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                          (update-in [:evaluation :order] (fn [xs] (mapv (fn [x] (update x :core-node-id ids)) xs))))
        convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                          (update :argument-node-ids (fn [xs] (mapv ids xs)))
                          (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                          (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        nodes (into {} (map (juxt :node-id identity) node-list))
        calls (mapv convert-call (get-in base [:core :calls]))
        edges (mapv convert-edge (get-in base [:core :call-edges]))
        records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids))
                      (get-in base [:core :lexical-bindings]))
        core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
        forms (:forms base) bindings (:bindings base)
        initial-types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derived (reduce
                 (fn [{:keys [types results]} [name call edge]]
                   (let [args [core (nodes (:core-node-id call)) call edge nodes bindings types forms]
                         result (c7 name args)]
                     {:types (assoc types (:operator-node-id call) (:descriptor result) (:core-node-id call) (:type result))
                      :results (conj results result)}))
                 {:types initial-types :results []}
                 (map vector ['sh08-scv-derive 'sh08-cq-derive 'sh08-vsl-derive 'sh08-lq-derive] calls edges))
        results (:results derived) descriptors (mapv :descriptor results)
        types (assoc (:types derived) (ids :slice-let) (:type (last results)))
        facts {:module (:module core) :function-type-table [] :type-table types
               :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                     ['sh08-scv-call-fact 'sh08-cq-call-fact 'sh08-vsl-call-fact 'sh08-lq-call-fact] descriptors)
               :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                       ['sh08-scv-constraint 'sh08-cq-constraint 'sh08-vsl-constraint 'sh08-lq-constraint] descriptors)
               :local-binding-facts (c7 'sh08-ft-local-facts
                                       [records types {:slice-let-binding (types (ids :call))} nodes (:module core)])}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}]
    {:core core :nodes nodes :calls calls :edges edges :forms forms :bindings bindings :ids ids
     :facts facts :request request :results results :descriptors descriptors}))

(deftest body-let-vector-count-slice-first-development-kernel
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test]
    (doseq [[text start position] [["0af" 1 nil] ["f" 0 nil] ["0af" 9 0] ["0af" 9 1]]]
      (let [{:keys [core bindings ids facts request results descriptors]}
            (let-vector-count-slice-first-body-products text start position)
            c7 (ns-resolve upstream 'invoke-c7)
            fresh (fn [candidate]
                    (let [counts (leaf 'body-cq-context request candidate bindings)
                          quotient (leaf 'body-nq-context request candidate bindings)
                          queries (leaf 'body-lq-context request candidate bindings)]
                      [[counts quotient queries]
                       (assoc candidate :count-context (:context counts) :quotient-context (:context quotient)
                                        :list-query-context (:context queries))]))
            [contexts complete] (fresh facts)
            valid (fn [candidate]
                    (let [[contexts candidate] (fresh candidate)]
                      (and (every? #(= :accepted (:status %)) contexts)
                           (leaf 'body-call-joins-with-foreign-valid? core candidate bindings {}))))
            graph (leaf 'body-derive-nodes-with-foreign core complete {})
            first-record (get-in graph [:nodes (ids :let-first-call)])
            slice-record (get-in graph [:nodes (ids :slice-call)])
            let-record (get-in graph [:nodes (ids :slice-let)])]
        (is (every? #(= :accepted (:status %)) results))
        (is (every? #(= :accepted (:status %)) contexts))
        (is (= :accepted (:status graph)))
        (is (true? (valid facts)))
        (is (= #{} (:direct first-record) (get-in graph [:nodes (ids :slice-end) :direct])))
        (is (nil? (:allocation first-record)))
        (is (= (:transitive slice-record) (:transitive first-record)))
        (is (= [(ids :let-first-operator) (ids :slice-call)] (:evaluated-children first-record)))
        (is (= (case position 0 #{:error/throw} 1 #{:memory/allocate :error/throw} #{:memory/allocate}) (:transitive let-record)))
        (is (= (nil? position) (get-in slice-record [:allocation :normal-completion-possible?])))
        (is (true? (leaf 'body-graph-equations-valid? core complete {} (:nodes graph))))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 4)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (when (nil? position)
          (doseq [[index operator fact-name row-name path]
                  [[2 :slice-operator 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :receiver :body-consumer :node :children]]
                   [2 :slice-operator 'sh08-vsl-call-fact 'sh08-vsl-constraint [:bounds-proof :end :value]]
                   [3 :let-first-operator 'sh08-lq-call-fact 'sh08-lq-constraint [:bounds-proof :selected-index]]
                   [3 :let-first-operator 'sh08-lq-call-fact 'sh08-lq-constraint [:bounds-proof :vector-length]]]]
            (let [changed (assoc-in (descriptors index) path :forged)
                  candidate (-> facts (assoc-in [:type-table (ids operator)] changed)
                                (assoc-in [:call-type-facts index] (c7 fact-name [changed (:module core)]))
                                (assoc-in [:constraint-ledger index] (c7 row-name [changed])))]
              (is (false? (valid candidate))))))
        (is (false? (leaf 'body-graph-equations-valid? core complete {}
                          (assoc-in (:nodes graph) [(ids :let-first-call) :direct] #{:memory/allocate}))))))))

(deftest body-let-vector-count-slice-development-kernel
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test]
    (doseq [[text start position] [["0af" 1 nil] ["" 0 nil] ["0" 1 nil] ["0af" 9 0] ["0af" 9 1]]]
      (let [{:keys [core bindings ids facts request results descriptors]}
            (let-vector-count-slice-body-products text start position)
            c7 (ns-resolve upstream 'invoke-c7)
            fresh (fn [candidate]
                    (let [counts (leaf 'body-cq-context request candidate bindings)
                          candidate (assoc candidate :count-context (:context counts))
                          context (leaf 'body-nq-context request candidate bindings)]
                      [counts context (assoc candidate :quotient-context (:context context))]))
            [counts quotient complete] (fresh facts)
            valid (fn [candidate]
                    (let [[counts context candidate] (fresh candidate)]
                      (and (= :accepted (:status counts) (:status context))
                           (leaf 'body-call-joins-with-foreign-valid? core candidate bindings {}))))
            graph (leaf 'body-derive-nodes-with-foreign core complete {})
            count-record (get-in graph [:nodes (ids :slice-end)])
            slice-record (get-in graph [:nodes (ids :slice-call)])
            let-record (get-in graph [:nodes (ids :slice-let)])]
        (is (every? #(= :accepted (:status %)) results))
        (is (= :accepted (:status counts) (:status quotient) (:status graph)))
        (is (true? (valid facts)))
        (is (= #{} (:direct count-record) (:transitive count-record)))
        (is (nil? (:allocation count-record)))
        (doseq [id [:slice-ref :slice-count-reference]]
          (is (= #{} (get-in graph [:nodes (ids id) :direct]) (get-in graph [:nodes (ids id) :transitive]))))
        (is (= (if (nil? position) #{:memory/allocate} #{}) (:direct slice-record)))
        (is (= (case position 0 #{} 1 #{:error/throw} #{:memory/allocate}) (:transitive slice-record)))
        (is (= (case position 0 #{:error/throw} 1 #{:memory/allocate :error/throw} #{:memory/allocate}) (:transitive let-record)))
        (is (= (nil? position) (get-in slice-record [:allocation :normal-completion-possible?])))
        (is (true? (leaf 'body-graph-equations-valid? core complete {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (is (= counts (leaf 'body-cq-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger] index (range 3)]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (when (nil? position)
          (doseq [path [[:bounds-proof :end :value] [:bounds-proof :end :reference :node-id]
                        [:bounds-proof :end :cardinality-proof :descriptor :query-semantics :operation]]]
            (let [changed (assoc-in (descriptors 2) path :forged)
                  candidate (-> facts (assoc-in [:type-table (ids :slice-operator)] changed)
                                (assoc-in [:call-type-facts 2] (c7 'sh08-vsl-call-fact [changed (:module core)]))
                                (assoc-in [:constraint-ledger 2] (c7 'sh08-vsl-constraint [changed])))]
              (is (false? (valid candidate))))))
        (is (false? (leaf 'body-graph-equations-valid? core complete {}
                          (assoc-in (:nodes graph) [(ids :slice-end) :direct] #{:memory/allocate}))))))))

(deftest body-let-vector-slice-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'let-vector-slice-input) transfer (ns-resolve upstream 'vector-slice-transfer-input)]
    (doseq [[value start end position] [["sha256:abcd" 1 2 nil] [[65 66 67] 1 2 nil] ["" 0 0 nil] [[] 0 0 nil]
                                       ["sha256:abcd" 20 1 0] ["sha256:abcd" 20 1 1] ["sha256:abcd" 20 1 2]]]
      (let [base (input (if (nil? position) value (transfer value start end position)) start end)
            ids (zipmap (keys (:nodes base)) (map sha (range 6000 6100)))
            convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                              (update-in [:evaluation :order] (fn [xs] (mapv (fn [x] (update x :core-node-id ids)) xs))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                              (update :argument-node-ids (fn [xs] (mapv ids xs))) (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                              (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            records (mapv #(-> % (update :let-core-node-id ids) (update :initializer-node-id ids)) (get-in base [:core :lexical-bindings]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges :lexical-bindings records)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            init-id (ids :call) ref-id (ids :slice-ref) id (ids :slice-call) let-id (ids :slice-let)
            forms (:forms base) bindings (:bindings base)
            child-call (first (filter #(= init-id (:core-node-id %)) calls))
            child (when child-call (c7 'sh08-scv-derive [core (nodes init-id) child-call
                                                        (first (filter #(= init-id (:call-core-node-id %)) edges)) nodes bindings types forms]))
            types (if child (assoc types (:operator-node-id child-call) (:descriptor child) init-id (:type child) ref-id (:type child)) types)
            call (last calls) edge (last edges)
            result (c7 'sh08-vsl-derive [core (nodes id) call edge nodes bindings types forms]) descriptor (:descriptor result)
            types (assoc types (:operator-node-id call) descriptor id (:type result) let-id (:type result))
            descriptors (if child [(:descriptor child) descriptor] [descriptor])
            facts {:module (:module core) :function-type-table [] :type-table types
                   :call-type-facts (mapv (fn [name descriptor] (c7 name [descriptor (:module core)]))
                                         (if child ['sh08-scv-call-fact 'sh08-vsl-call-fact] ['sh08-vsl-call-fact]) descriptors)
                   :constraint-ledger (mapv (fn [name descriptor] (c7 name [descriptor]))
                                           (if child ['sh08-scv-constraint 'sh08-vsl-constraint] ['sh08-vsl-constraint]) descriptors)
                   :local-binding-facts (c7 'sh08-ft-local-facts [records types {:slice-let-binding (types init-id)} nodes (:module core)])}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])
            init-record (get-in graph [:nodes init-id]) ref-record (get-in graph [:nodes ref-id]) let-record (get-in graph [:nodes let-id])]
        (is (= :accepted (:status result) (:status quotient) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if child 2 1) (count calls) (count (:call-type-facts facts)) (count (:constraint-ledger facts))))
        (is (= #{} (:direct ref-record) (:transitive ref-record) (:direct let-record)))
        (is (nil? (:allocation ref-record)))
        (is (nil? (:allocation let-record)))
        (is (= (if (= position 0) #{} #{:memory/allocate}) (:direct init-record)))
        (is (= (if (nil? position) #{:memory/allocate} #{}) (:direct record)))
        (is (= (cond (= position 0) #{} (nil? position) #{:memory/allocate} :else #{:error/throw}) (:transitive record)))
        (is (= (cond (= position 0) #{:error/throw} (nil? position) #{:memory/allocate} :else #{:memory/allocate :error/throw}) (:transitive let-record)))
        (is (= (nil? position) (get-in record [:allocation :normal-completion-possible?])))
        ;; Graph order is declarative; dynamic abort order is checked at runtime.
        (is (= [init-id id] (:dependencies let-record) (:evaluated-children let-record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger] index (range (count descriptors))]
          (is (false? (valid (update facts field #(vec (concat (subvec % 0 index) (subvec % (inc index))))))))
          (is (false? (valid (update facts field conj (get (get facts field) index))))))
        (when-not (= position 0)
          (is (= :rejected (:status (leaf 'body-nq-context (assoc-in request [:canonical-core-artifact :lexical-bindings 0 :binding-scope-id] :sibling)
                                         facts bindings))))
          (doseq [path [[:bounds-proof :receiver :record :binding-scope-id] [:bounds-proof :receiver :reference :attributes :symbol]
                        [:bounds-proof :receiver :origin :kind] [:bounds-proof :result-count]]]
            (let [changed (assoc-in descriptor path :forged)
                  candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                                (assoc-in [:call-type-facts (dec (count descriptors))] (c7 'sh08-vsl-call-fact [changed (:module core)]))
                                (assoc-in [:constraint-ledger (dec (count descriptors))] (c7 'sh08-vsl-constraint [changed])))]
              (is (false? (valid candidate))))))))))

(deftest body-literal-vector-slice-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'literal-vector-slice-input) transfer (ns-resolve upstream 'literal-vector-slice-transfer-input)
        construction-transfer (ns-resolve upstream 'literal-vector-slice-construction-transfer-input)]
    (doseq [[values start end positions] [[[65 66 67] 1 2 nil] [[] 0 0 nil] [[65 66 67] 0 3 nil] [[65 66 67] 3 3 nil]
                                         [[-5 0 999999999999999999999999999999999999N] 0N 3N nil]
                                         [[65 66 67] 20 1 [0]] [[65 66 67] 20 1 [1]] [[65 66 67] 20 1 [2]]
                                         [[65 66 67] 20 1 [0 1 2]] [[65 66] 99 1 :construction]]]
      (let [construction? (= :construction positions)
            base (cond construction? (construction-transfer) positions (transfer values start end positions) :else (input values start end))
            ids (zipmap (keys (:nodes base)) (map sha (range 5000 5100)))
            convert-node #(-> % (update :node-id ids) (update :children (fn [xs] (mapv ids xs)))
                              (update-in [:evaluation :order] (fn [xs] (mapv (fn [x] (update x :core-node-id ids)) xs))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                              (update :argument-node-ids (fn [xs] (mapv ids xs))) (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids) (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                              (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            call (convert-call (:call base)) edge (convert-edge (:edge base))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            id (ids :slice-call) receiver-id (ids :call) node (nodes id) forms (:forms base) bindings (:bindings base)
            result (c7 'sh08-vsl-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:module (:module core) :function-type-table []
                   :type-table (assoc types (:operator-node-id call) descriptor id (:type result))
                   :call-type-facts [(c7 'sh08-vsl-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-vsl-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            quotient (leaf 'body-nq-context request facts bindings)
            facts (assoc facts :quotient-context (:context quotient))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])]
        (is (= :accepted (:status result) (:status quotient) (:status graph)))
        (is (true? (valid facts)))
        (is (= (if positions #{} #{:memory/allocate}) (:direct record)))
        (is (= (cond construction? #{:error/throw} (some #{0} positions) #{:error/throw} positions #{:memory/allocate :error/throw}
                    :else #{:memory/allocate}) (:transitive record)))
        (is (= (nil? positions) (get-in record [:allocation :normal-completion-possible?])))
        (is (= (if positions nil {:unit :elements :count (- end start)})
               (get-in record [:allocation :extent])))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= quotient (leaf 'body-nq-context request facts bindings)))
        (doseq [field [:call-type-facts :constraint-ledger]]
          (is (false? (valid (assoc facts field []))))
          (is (false? (valid (update facts field conj (first (get facts field)))))))
        (doseq [path [[:bounds-proof :receiver :kind] [:bounds-proof :receiver :node :attributes :element-count]
                      [:bounds-proof :result-count] [:bounds-proof :selected-types] [:slice-policy :receiver] [:storage-policy :allocation]]]
          (let [changed (assoc-in descriptor path :forged)
                candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                              (assoc :call-type-facts [(c7 'sh08-vsl-call-fact [changed (:module core)])])
                              (assoc :constraint-ledger [(c7 'sh08-vsl-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {} (assoc-in (:nodes graph) [id :direct] #{:io/write}))))
        (when-not (or construction? (some #{0} positions))
          (is (= #{:memory/allocate} (get-in graph [:nodes receiver-id :direct]))))))))

(deftest body-vector-slice-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'vector-slice-input) transfer (ns-resolve upstream 'vector-slice-transfer-input)]
    (doseq [[text start end positions] [["a\u00e9\u00ff" 0 3 nil] ["a\u00e9\u00ff" 1 2 nil] ["" 0 0 nil] ["sha256:abcd" 0 7 nil]
                                       ["sha256:abcd" 7 11 nil] ["sha256:abcd" 11 11 nil]
                                       ["sha256:abcd" 0 11 nil] ["sha256:abcd" 20 1 0]
                                       ["sha256:abcd" 20 1 1] ["sha256:abcd" 20 1 2]
                                       ["sha256:abcd" 20 1 [0 1 2]]]]
      (let [base (if (nil? positions) (input text start end) (transfer text start end positions))
            ids (zipmap (keys (:nodes base)) (map sha (range 3100 3200)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in base [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in base [:core :calls])) edges (mapv convert-edge (get-in base [:core :call-edges]))
            core (assoc (:core base) :nodes node-list :calls calls :call-edges edges)
            bindings (:bindings base) forms (:forms base)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            child-call (first calls) child-edge (first edges) child-id (:core-node-id child-call)
            child (c7 'sh08-scv-derive [core (get nodes child-id) child-call child-edge nodes bindings types forms])
            types (assoc types (:operator-node-id child-call) (:descriptor child) child-id (:type child))
            call (last calls) edge (last edges) id (:core-node-id call) node (get nodes id)
            result (c7 'sh08-vsl-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:module (:module core) :type-table (assoc types (:operator-node-id call) descriptor id (:type result))
                   :function-type-table []
                   :call-type-facts [(c7 'sh08-scv-call-fact [(:descriptor child) (:module core)])
                                     (c7 'sh08-vsl-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-scv-constraint [(:descriptor child)]) (c7 'sh08-vsl-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-nq-context request facts bindings)
            facts (assoc facts :quotient-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])
            normal? (nil? positions) child-normal? (= :normal (get-in child [:descriptor :application]))]
        (is (= :accepted (:status child) (:status result) (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (leaf 'body-vsl-call-valid? core facts bindings node call edge nodes)))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record)))
        (is (= (cond-> #{} child-normal? (conj :memory/allocate) (not normal?) (conj :error/throw)) (:transitive record)))
        (is (= [:sh09-vector-slice-allocation-v1 :vector :possible-vector-slice-storage normal?]
               ((juxt :rule-version :value-kind :behavior :normal-completion-possible?) (:allocation record))))
        (is (= (when normal? {:unit :elements :count (- end start)}) (get-in record [:allocation :extent])))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-nq-context request facts bindings)))
        (doseq [changed [(dissoc descriptor :kind) (assoc descriptor :kind nil)
                         (assoc descriptor :kind :gravity.type/applied-string-character-vector)
                         (assoc-in descriptor [:bounds-proof :result-count] 99)
                         (assoc-in descriptor [:bounds-proof :selected-types] [:gravity.type/string])
                         (assoc descriptor :direct-effects (if normal? [] [:memory/allocate]))
                         (assoc-in descriptor [:storage-policy :allocation] :none)]]
          (let [candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                              (update :call-type-facts #(assoc % 1 (c7 'sh08-vsl-call-fact [changed (:module core)])))
                              (update :constraint-ledger #(assoc % 1 (c7 'sh08-vsl-constraint [changed]))))]
            (is (false? (valid candidate)))))
        (when normal?
          (let [slice-core (assoc core :nodes [node] :function-records [])
                allowed (leaf 'body-allowance-check slice-core facts graph)
                denied (leaf 'body-allowance-check slice-core (assoc-in facts [:module :effects] []) graph)]
            (is (= :accepted (:status allowed)))
            (is (= [:rejected "C8-UNDECLARED" :memory/allocate id]
                   [(:status denied) (:rule denied) (:effect denied) (get-in denied [:node :node-id])]))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger subvec 1)
                           (update facts :constraint-ledger subvec 0 1)
                           (update facts :constraint-ledger conj (first (:constraint-ledger facts)))
                           (update facts :constraint-ledger conj (last (:constraint-ledger facts)))
                           (assoc facts :call-type-facts [])
                           (update facts :call-type-facts subvec 1)
                           (update facts :call-type-facts subvec 0 1)
                           (update facts :call-type-facts conj (first (:call-type-facts facts)))
                           (update facts :call-type-facts conj (last (:call-type-facts facts)))
                           (assoc facts :quotient-context nil)]]
          (is (false? (valid candidate))))
        (when (= [text start end positions] ["sha256:abcd" 0 7 nil])
          (let [callbacks (atom 0) opaque (proxy [Object] []
                                         (hashCode [] (swap! callbacks inc) (throw (AssertionError. "C8 slice hash")))
                                         (equals [_] (swap! callbacks inc) (throw (AssertionError. "C8 slice equality"))))]
            (doseq [ordinal [nil -1 opaque]]
              (let [changed (assoc-in descriptor [:source-edge :ordinal] ordinal)
                    candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                                  (update :call-type-facts #(assoc % 1 (c7 'sh08-vsl-call-fact [changed (:module core)])))
                                  (update :constraint-ledger #(assoc % 1 (c7 'sh08-vsl-constraint [changed]))))]
                (is (false? (leaf 'body-vsl-call-valid? core candidate bindings node call (assoc edge :ordinal ordinal) nodes)))))
            (doseq [ordinal [nil -1 opaque]]
              (let [changed-child (assoc-in (:descriptor child) [:source-edge :ordinal] ordinal)
                    changed (-> descriptor (assoc-in [:bounds-proof :receiver :descriptor] changed-child)
                                (assoc-in [:bounds-proof :receiver :call-fact] (c7 'sh08-scv-call-fact [changed-child (:module core)]))
                                (assoc-in [:bounds-proof :receiver :constraint] (c7 'sh08-scv-constraint [changed-child])))
                    changed-core (assoc-in core [:call-edges 0 :ordinal] ordinal)
                    candidate (-> facts (assoc-in [:type-table (:operator-node-id child-call)] changed-child)
                                  (assoc-in [:type-table (:operator-node-id call)] changed)
                                  (assoc :call-type-facts [(c7 'sh08-scv-call-fact [changed-child (:module core)])
                                                          (c7 'sh08-vsl-call-fact [changed (:module core)])]
                                         :constraint-ledger [(c7 'sh08-scv-constraint [changed-child]) (c7 'sh08-vsl-constraint [changed])]))]
                (is (false? (leaf 'body-vsl-call-valid? changed-core candidate bindings node call edge nodes)))
                (is (= :rejected (:status (leaf 'body-nq-context (assoc request :canonical-core-artifact changed-core) candidate bindings))))))
            (let [changed (assoc descriptor :kind opaque)
                  candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                                (update :call-type-facts #(assoc % 1 (c7 'sh08-vsl-call-fact [changed (:module core)])))
                                (update :constraint-ledger #(assoc % 1 (c7 'sh08-vsl-constraint [changed]))))]
              (is (false? (valid candidate))))
            (is (true? (valid facts))) (is (zero? @callbacks)))
          (let [changed-child (assoc-in (:descriptor child) [:conversion-proof :scalar-count] 99)
                changed (-> descriptor (assoc-in [:bounds-proof :receiver :descriptor] changed-child)
                            (assoc-in [:bounds-proof :receiver :call-fact] (c7 'sh08-scv-call-fact [changed-child (:module core)]))
                            (assoc-in [:bounds-proof :receiver :constraint] (c7 'sh08-scv-constraint [changed-child])))
                candidate (-> facts (assoc-in [:type-table (:operator-node-id child-call)] changed-child)
                              (assoc-in [:type-table (:operator-node-id call)] changed)
                              (assoc :call-type-facts [(c7 'sh08-scv-call-fact [changed-child (:module core)])
                                                      (c7 'sh08-vsl-call-fact [changed (:module core)])]
                                     :constraint-ledger [(c7 'sh08-scv-constraint [changed-child]) (c7 'sh08-vsl-constraint [changed])]))]
            (is (false? (valid candidate)))
            (is (= :rejected (:status (leaf 'body-nq-context request candidate bindings)))))
          (let [core (merge core {:root-core-node-ids [id] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals bindings))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))


              (is (= #{:memory/allocate} (get-in analyzed [:graph :namespace :inferred]))))))))))

(deftest body-ascii-string-vector-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        c7 (ns-resolve upstream 'invoke-c7) input (ns-resolve upstream 'literal-string-vector-input)
        transfer (ns-resolve upstream 'literal-string-vector-transfer-input)]
    (doseq [value ["" "sha256:" (apply str (map char (range 128))) "\u00e9" (apply str (map char (range 256))) nil]]
      (let [base (if (nil? value) (transfer) (input value))
            ids (zipmap (keys (:nodes base)) (map sha (range 2900 3000)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            nodes (into {} (map (juxt :node-id identity) node-list))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            node (nodes (ids :call)) bindings (:bindings base) forms (:forms base)
            result (c7 'sh08-scv-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:module (:module core) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-scv-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-scv-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-nq-context request facts bindings)
            facts (assoc facts :quotient-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes (ids :call)])
            normal? (some? value)]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (leaf 'body-scv-call-valid? core facts bindings node call edge nodes)))
        (is (true? (valid facts)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record)))
        (is (if normal? (= #{:memory/allocate} (:transitive record)) (contains? (:transitive record) :error/throw)))
        (is (= [:sh09-string-character-vector-allocation-v1 :vector :possible-character-vector-storage normal?]
               ((juxt :rule-version :value-kind :behavior :normal-completion-possible?) (:allocation record))))
        (is (= (when normal? {:unit :characters :count (count value)}) (get-in record [:allocation :extent])))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-nq-context request facts bindings)))
        (doseq [changed [((ns-resolve upstream 'literal-string-vector-cross-domain-descriptor) descriptor) (dissoc descriptor :kind) (assoc descriptor :kind nil)
                         (assoc descriptor :kind (Object.))
                         (assoc descriptor :kind :gravity.type/applied-string-concat)
                         (assoc-in descriptor [:conversion-proof :scalar-count] 99)
                         (assoc-in descriptor [:conversion-policy :utf16-splitting] :allowed)
                         (assoc descriptor :direct-effects (if normal? [] [:memory/allocate]))
                         (assoc-in descriptor [:storage-policy :allocation] :none)]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-scv-call-fact [changed (:module core)])]
                                     :constraint-ledger [(c7 'sh08-scv-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger conj (first (:constraint-ledger facts)))
                           (assoc facts :call-type-facts [])
                           (update facts :call-type-facts conj (first (:call-type-facts facts)))
                           (assoc facts :quotient-context nil)]]
          (is (false? (valid candidate))))
        (when (contains? #{"sha256:" "\u00e9"} value)
          (let [callbacks (atom 0) opaque (proxy [Object] []
                                         (hashCode [] (swap! callbacks inc) (throw (AssertionError. "C8 ASCII hash")))
                                         (equals [_] (swap! callbacks inc) (throw (AssertionError. "C8 ASCII equality"))))
                changed (assoc descriptor :kind opaque)
                candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-scv-call-fact [changed (:module core)])]
                                     :constraint-ledger [(c7 'sh08-scv-constraint [changed])]))
                bad-request (update-in request [:b47-context :authenticated-core-request :forms]
                              #(mapv (fn [form] (if (= :operator (:form-id form)) (assoc form :value opaque) form)) %))]
            (doseq [ordinal [nil -1 opaque]]
              (let [edge (assoc edge :ordinal ordinal)
                    changed (assoc-in descriptor [:source-edge :ordinal] ordinal)
                    candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                                  (assoc :call-type-facts [(c7 'sh08-scv-call-fact [changed (:module core)])]
                                         :constraint-ledger [(c7 'sh08-scv-constraint [changed])]))]
                (is (false? (leaf 'body-scv-call-valid? core candidate bindings node call edge nodes)))))
            (is (true? (leaf 'body-scv-call-valid? core facts bindings node call (assoc edge :ordinal 0) nodes)))
            (is (false? (valid candidate)))
            (is (= :rejected (:status (leaf 'body-nq-context bad-request facts bindings))))
            (is (true? (valid facts)))
            (is (zero? @callbacks)))
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals bindings))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))


              (is (= #{:memory/allocate} (get-in analyzed [:graph :namespace :inferred]))))))))))

(deftest body-count-quotient-scaling-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        input (ns-resolve upstream 'count-quotient-scaling-input)
        transfer (ns-resolve upstream 'count-quotient-scaling-transfer-input)]
    (doseq [[factor divisor kind empty? positions] [[2 4 :vector false] [0 4 :vector true] [4 4 :map false]
                                                    [3 7 :vector false] [2 4 :vector false 0]
                                                    [5 4 :vector false 1] [2 4 :vector false [0 1]]]]
      (let [original (if (nil? positions) (input factor divisor kind empty?) (transfer factor divisor positions))
            ids (zipmap (keys (:nodes original)) (map sha (range 2400 2500)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                       (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in original [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in original [:core :calls])) edges (mapv convert-edge (get-in original [:core :call-edges]))
            core (assoc (:core original) :nodes node-list :calls calls :call-edges edges)
            forms (:forms original) bindings (:bindings original)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types original)))
            count-call (first calls) count-id (:core-node-id count-call)
            count-result (c7 'sh08-cq-derive [core (get nodes count-id) count-call (first edges) nodes bindings types forms])
            types (assoc types (:operator-node-id count-call) (:descriptor count-result) count-id (:type count-result))
            qcall (second calls) qid (:core-node-id qcall)
            qresult (c7 'sh08-nq-derive [core (get nodes qid) qcall (second edges) nodes bindings types forms])
            types (assoc types (:operator-node-id qcall) (:descriptor qresult) qid (:type qresult))
            call (last calls) edge (last edges) id (:core-node-id call) node (get nodes id)
            result (c7 'sh08-cqs-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:module (:module core) :type-table (assoc types (:operator-node-id call) descriptor id (:type result))
                   :function-type-table []
                   :call-type-facts [(c7 'sh08-cq-call-fact [(:descriptor count-result) (:module core)])
                                     (c7 'sh08-nq-call-fact [(:descriptor qresult) (:module core)])
                                     (c7 'sh08-cqs-call-fact [descriptor (:module core)])]
                   :constraint-ledger [(c7 'sh08-cq-constraint [(:descriptor count-result)])
                                       (c7 'sh08-nq-constraint [(:descriptor qresult)])
                                       (c7 'sh08-cqs-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            numeric (leaf 'body-nq-context request facts bindings)
            counts (leaf 'body-cq-context request facts bindings)
            facts (assoc facts :quotient-context (:context numeric) :count-context (:context counts))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])]
        (is (= :accepted (:status count-result) (:status qresult) (:status result) (:status numeric) (:status counts) (:status graph))
            (pr-str [numeric graph]))
        (is (true? (leaf 'body-nq-call-valid? core facts bindings (get nodes qid) qcall (second edges) nodes)))
        (is (true? (leaf 'body-cqs-call-valid? core facts bindings node call edge nodes)))
        (is (true? (valid facts)))
        (is (= (if (= :normal (:application descriptor)) #{:memory/allocate} #{}) (:direct record)))
        (is (if (= :normal (:application descriptor))
              (= #{:memory/allocate} (:transitive record)) (contains? (:transitive record) :error/throw)))
        (is (= [:sh09-count-quotient-scaling-allocation-v1 :integer :possible-integer-result-storage (= :normal (:application descriptor))]
               ((juxt :rule-version :value-kind :behavior :normal-completion-possible?) (:allocation record))))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= numeric (leaf 'body-nq-context request facts bindings)))
        (doseq [changed [(dissoc descriptor :kind)
                         (assoc descriptor :kind nil)
                         (assoc descriptor :kind (Object.))
                         (assoc descriptor :kind :gravity.type/applied-string-concat)
                         (assoc descriptor :numeric-mode :wrapping)
                         (assoc descriptor :direct-effects (if (= :normal (:application descriptor)) [] [:memory/allocate]))
                         (assoc-in descriptor [:storage-policy :allocation] :none)
                         (assoc-in descriptor [:range-proof :factor :value] (inc factor))
                         (assoc-in descriptor [:range-proof :quotient :descriptor :range-proof :law] :forged)
                         (assoc-in descriptor [:scaling-policy :premise] :quotient-at-most-count)]]
          (let [candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                              (update :call-type-facts #(conj (vec (butlast %)) (c7 'sh08-cqs-call-fact [changed (:module core)])))
                              (update :constraint-ledger #(conj (vec (butlast %)) (c7 'sh08-cqs-constraint [changed]))))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger conj (first (:constraint-ledger facts)))
                           (update facts :constraint-ledger conj (second (:constraint-ledger facts)))
                           (update facts :constraint-ledger conj (last (:constraint-ledger facts)))
                           (update facts :call-type-facts conj (first (:call-type-facts facts)))
                           (update facts :call-type-facts conj (second (:call-type-facts facts)))
                           (update facts :call-type-facts conj (last (:call-type-facts facts)))
                           (assoc facts :call-type-facts (subvec (:call-type-facts facts) 1))
                           (assoc facts :quotient-context nil) (assoc facts :count-context nil)]]
          (is (false? (valid candidate))))
        (when (= [factor divisor kind empty? positions] [2 4 :vector false nil])
          (let [callbacks (atom 0) opaque (proxy [Object] []
                                         (hashCode [] (swap! callbacks inc) (throw (AssertionError. "C8 scaling hash")))
                                         (equals [_] (swap! callbacks inc) (throw (AssertionError. "C8 scaling equality")))
                                         (toString [] (swap! callbacks inc) (throw (AssertionError. "C8 scaling formatting"))))
                changed (assoc descriptor :kind opaque)
                candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                              (update :call-type-facts #(conj (vec (butlast %)) (c7 'sh08-cqs-call-fact [changed (:module core)])))
                              (update :constraint-ledger #(conj (vec (butlast %)) (c7 'sh08-cqs-constraint [changed]))))
                bad-request (update-in request [:b47-context :authenticated-core-request :forms]
                              #(mapv (fn [form] (if (= :scale-operator (:form-id form)) (assoc form :value opaque) form)) %))]
            (is (false? (valid candidate)))
            (is (= :rejected (:status (leaf 'body-nq-context bad-request facts bindings))))
            (is (true? (valid facts)))
            (is (zero? @callbacks)))
          (let [core (merge core {:root-core-node-ids [id] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals bindings))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request %1 {})
                analyzed (analyze facts)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))


              (is (= #{:memory/allocate} (get-in analyzed [:graph :namespace :inferred]))))))))))

(deftest body-nonnegative-integer-quotient-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test c7 (ns-resolve upstream 'invoke-c7)
        literal (ns-resolve upstream 'nonnegative-integer-quotient-input)
        counted (ns-resolve upstream 'nonnegative-integer-count-quotient-input)
        transfer (ns-resolve upstream 'nonnegative-integer-quotient-transfer-input)]
    (doseq [original [(literal [127 10]) (counted :vector false 2) (counted :map true 4)
                     (transfer [127 10] 0) (transfer [127 10] 1)]]
      (let [ids (zipmap (keys (:nodes original)) (map sha (range 2200 2300)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                       (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            convert-call #(-> % (update :core-node-id ids) (update :operator-node-id ids)
                                (update :argument-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            convert-edge #(-> % (update :call-core-node-id ids)
                                (update :argument-core-node-ids (fn [xs] (mapv ids xs)))
                                (update :ordered-evaluation-node-ids (fn [xs] (mapv ids xs))))
            node-list (mapv convert-node (get-in original [:core :nodes])) nodes (into {} (map (juxt :node-id identity) node-list))
            calls (mapv convert-call (get-in original [:core :calls])) edges (mapv convert-edge (get-in original [:core :call-edges]))
            core (assoc (:core original) :nodes node-list :calls calls :call-edges edges)
            forms (:forms original) bindings (:bindings original)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types original)))
            count-call (when (:count-id original) (first calls)) count-id (:core-node-id count-call)
            count-result (when count-call (c7 'sh08-cq-derive [core (get nodes count-id) count-call (first edges) nodes bindings types forms]))
            types (if count-call (assoc types (:operator-node-id count-call) (:descriptor count-result) count-id (:type count-result)) types)
            call (last calls) edge (last edges) id (:core-node-id call) node (get nodes id)
            result (c7 'sh08-nq-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
            facts {:module (:module core) :type-table (assoc types (:operator-node-id call) descriptor id (:type result))
                   :function-type-table []
                   :call-type-facts (cond-> [] count-call (conj (c7 'sh08-cq-call-fact [(:descriptor count-result) (:module core)]))
                                            true (conj (c7 'sh08-nq-call-fact [descriptor (:module core)])))
                   :constraint-ledger (cond-> [] count-call (conj (c7 'sh08-cq-constraint [(:descriptor count-result)]))
                                               true (conj (c7 'sh08-nq-constraint [descriptor])))}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            q-context (leaf 'body-nq-context request facts bindings)
            count-context (leaf 'body-cq-context request facts bindings)
            facts (assoc facts :quotient-context (:context q-context) :count-context (:context count-context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {}) record (get-in graph [:nodes id])]
        (is (= :accepted (:status result) (:status q-context) (:status count-context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= (if (= :normal (:application descriptor)) #{:memory/allocate} #{}) (:direct record)))
        (is (= (if (= :normal (:application descriptor)) #{:memory/allocate} #{:error/throw}) (:transitive record)))
        (is (= :integer (get-in record [:allocation :value-kind])))
        (is (= :possible-integer-result-storage (get-in record [:allocation :behavior])))
        (is (= (= :normal (:application descriptor)) (get-in record [:allocation :normal-completion-possible?])))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= q-context (leaf 'body-nq-context request facts bindings)))
        (when (and (nil? count-call) (= :normal (:application descriptor)))
          (let [core (merge core {:root-core-node-ids [id] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals bindings))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
            (when (= :accepted (:status analyzed))


              (is (= #{:memory/allocate} (get-in analyzed [:graph :namespace :inferred]))))))
        (doseq [changed [(assoc descriptor :numeric-mode :wrapping)
                         (assoc descriptor :direct-effects [])
                         (assoc-in descriptor [:storage-policy :allocation] :none)
                         (assoc-in descriptor [:range-proof :divisor :value] 0)
                         (assoc-in descriptor [:quotient-policy :conversion] :i64)]]
          (when-not (= descriptor changed)
          (let [candidate (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                              (update :call-type-facts #(conj (vec (butlast %)) (c7 'sh08-nq-call-fact [changed (:module core)])))
                              (update :constraint-ledger #(conj (vec (butlast %)) (c7 'sh08-nq-constraint [changed]))))]
            (is (false? (valid candidate))))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :call-type-facts into (:call-type-facts facts))
                           (assoc facts :quotient-context nil)]]
          (is (false? (valid candidate))))))))

(deftest body-symbol-set-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)]
    (doseq [arguments [[{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/symbol]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/symbol :gravity.type/symbol]} :gravity.type/symbol]
                       [{:kind :gravity.type/set :shape :literal :element-types [:gravity.type/symbol]} :gravity.type/never]
                       [:gravity.type/never :gravity.type/symbol]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string)]]
          (is (false? (valid candidate))))
        (when (= arguments [{:kind :gravity.type/set :shape :literal :element-types []} :gravity.type/symbol])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
))))))

(deftest body-symbol-set-contains-id-guard-development-kernel
  (let [finite {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/symbol]}
        _ (require 'gravity.self-hosting.sh08-function-call-type-test)
        input-fn (sh08-helper 'map-contains-input) c7 (sh08-helper 'invoke-c7)
        {:keys [core node call edge nodes bindings types forms module]} (input-fn [finite :gravity.type/symbol])
        result (c7 'sh08-ksm-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        ids identity
        facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
               :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
        prepared (leaf 'body-mc-context request facts bindings)
        facts (assoc facts :map-contains-context (:context prepared))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID printing"))))]
    ;; Existing zero-argument local-call constraints have no producer rule.
    ;; This selector leaves those rows to the existing owning validators.
    (let [membership (first (:constraint-ledger facts))
          local-row {:constraint-id-request {:domain :gravity/sh08-function-call-constraint-v1
                                            :call-core-node-id :local-call :callee-function-syntax-id :callee}
                     :kind :equality :lhs :callee :rhs [] :origin []
                     :profile :meta :target :jvm :status :solved :diagnostics []}]
      (let [with-local-row (update facts :constraint-ledger conj local-row)
            recomputed (leaf 'body-mc-context request with-local-row bindings)
            joined (assoc with-local-row :map-contains-context (:context recomputed))]
        (is (= :accepted (:status recomputed)))
        (is (= recomputed (leaf 'body-mc-context request with-local-row bindings)))
        (is (true? (leaf 'body-ksm-call-valid? core joined bindings node call edge nodes))))
      (doseq [bad [nil opaque [opaque] {:opaque opaque}]]
        (is (false? (leaf 'body-ksm-call-valid? core
                          (assoc-in facts [:constraint-ledger 0 :producer-rule] bad) bindings node call edge nodes)))
        (is (zero? @callbacks)))
      (is (true? (leaf 'body-sysm-ledger-ids? [local-row membership])))
      (is (true? (leaf 'body-sysm-ledger-ids? [membership local-row])))
      (is (true? (leaf 'body-sysm-ledger-ids? [(assoc local-row :producer-rule nil) membership])))
      (doseq [bad [opaque [opaque] {:opaque opaque}]]
        (is (false? (leaf 'body-sysm-ledger-ids? [(assoc local-row :producer-rule bad) membership])))
        (is (zero? @callbacks))
        (is (false? (leaf 'body-sysm-ledger-ids? [(assoc-in membership [:lhs :core-node-id] bad) local-row])))
        (is (zero? @callbacks))))
    (is (= :accepted (:status prepared)))
    (is (true? (leaf 'body-ksm-call-valid? core facts bindings node call edge nodes)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:operator-node-id] [:operator-binding-id] [:argument-node-ids 0] [:argument-node-ids 1]]]
      (is (false? (leaf 'body-ksm-call-valid? core facts bindings node (assoc-in call path bad) edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:node-id] [:children 0] [:children 1] [:children 2] [:source :form-id] [:source :syntax-id]]]
      (is (false? (leaf 'body-ksm-call-valid? core facts bindings (assoc-in node path bad) call edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}] i (range 3)]
      (let [changed (assoc-in facts [:map-contains-context :forms (ids :call) :child-form-ids i] bad)]
        (is (false? (leaf 'body-ksm-call-valid? core changed bindings node call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (is (false? (leaf 'body-ksm-call-valid? core
                        (assoc-in facts [:map-contains-context :forms (ids :operator) :value] bad) bindings node call edge nodes)))
      (is (false? (leaf 'body-ksm-call-valid? core facts bindings node call edge
                        (assoc-in nodes [(ids :operator) :attributes :symbol] bad))))
      (is (zero? @callbacks)))))

(deftest body-string-set-carrier-private-values-development-kernel
  ;; Independent original C8 reconstruction of inferred entry and return domains.
  (let [invoke (fn [function args] (apply leaf function args))
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} b :gravity.type/bool s :gravity.type/string
        record {:function-syntax-id :helper :fixed-arity 3 :body-core-node-id :body}
        call (fn [id args] {:core-node-id id :argument-node-ids args})
        edge (fn [id caller] {:call-core-node-id id :callee-function-syntax-id :helper
                             :caller-function-syntax-id caller :classification :local-function})
        census {:function-record record
                :calls [(call :entry-0 [:again :value :seen]) (call :entry-1 [:other-again :other-value :other-seen])
                        (call :self [:again :value :inserted])]
                :edges [(edge :entry-0 :main) (edge :entry-1 :main) (edge :self :helper)]}
        nodes {:entry-0 {:source {:form-id :entry-0}} :entry-1 {:source {:form-id :entry-1}}}
        types {:again b :value s :seen carrier :other-again b :other-value s :other-seen carrier
               :entry-0 :gravity.type/never :entry-1 b}
        entries (fn [types*] (invoke 'body-is-entry-values [census nodes types*]))
        result (entries types)
        return-nodes {:body {:node-id :body :core-form :if :children [:guard :self :answer]}
                      :guard {:node-id :guard :core-form :reference :children []}
                      :self {:node-id :self :core-form :call :children []}
                      :answer {:node-id :answer :core-form :reference :children []}}
        return-types {:guard b :self :gravity.type/unknown :answer carrier}
        witness (fn [types*] (invoke 'body-is-return-witness [record return-nodes types*]))
        returned (witness return-types)]
    (is (true? (invoke 'body-ss-type? [carrier])))
    (is (true? (invoke 'body-is-value-type? [carrier])))
    (is (false? (invoke 'body-ds-type? [carrier])))
    (let [produced (:result-type (invoke (quote body-ssin-validate-types) [[{:kind :gravity.type/set, :shape :literal, :element-types []} s]]))
          checked (invoke 'body-lc-validate-types [[produced]])
          nested {:kind :gravity.type/list :shape :literal :element-types [produced]}]
      (is (= carrier produced))
      (is (= {:status :accepted} checked))

      (is (= {:status :accepted} (invoke 'body-lc-validate-types [[nested]])))

      (require 'gravity.self-hosting.sh08-function-call-type-test)
      (doseq [[input-name family expected] [['list-construction-input "lc" nested]
                                           ['keyword-predicate-input "kp" b]
                                           ['set-predicate-input "setp" b]]]
        (let [input ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test input-name) [produced])
              {:keys [core node call edge nodes bindings types module]} input
              header (invoke (symbol (str "body-" family "-source")) [core node call edge nodes bindings types module checked])
              descriptor (invoke (symbol (str "body-" family "-finish")) [header])]
          (is (map? header))
          (is (= [produced] (:argument-types descriptor)))
          (is (= expected (:result-type descriptor) (:return descriptor))))))
    (is (= :accepted (:status result)))
    (is (= [b s carrier] (:parameter-types result)))
    (is (= 2 (:normal-entry-count result)))
    (is (every? :normal-invocation? (:entries result)))



    (is (= :private-self-entry-type-conflict (:reason (entries (assoc types :other-value :gravity.type/keyword)))))
    (doseq [wrong [:gravity.type/set :gravity.type/unknown
                   (assoc carrier :extra true) (assoc carrier :element-type :gravity.type/keyword)
                   (assoc carrier :element-type carrier) (assoc carrier :kind :gravity.type/vector)]]
      (is (false? (invoke 'body-is-value-type? [wrong])))
      (is (= :private-self-independent-entry-types-required (:reason (entries (assoc types :seen wrong))))))
    (let [abort (assoc types :again :gravity.type/never :other-again :gravity.type/never)]
      (is (= :private-self-normal-entry-required (:reason (entries abort)))))
    (let [abort (assoc types :other-again :gravity.type/never :other-seen :gravity.type/unknown)
          retained (entries abort)]
      (is (= :accepted (:status retained)))
      (is (= 1 (:normal-entry-count retained)))
      (is (= [b s carrier] (:parameter-types retained))))
    (is (= :accepted (:status returned)))
    (is (= carrier (:return-type returned)))
    (is (= [:self] (:pending-return-node-ids returned)))


    (is (= :private-self-independent-return-required (:reason (witness (assoc return-types :answer :gravity.type/never)))))
    (is (= :private-self-return-value-type-required (:reason (witness (assoc return-types :answer :gravity.type/set)))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "carrier hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "carrier equality"))))]
      (doseq [wrong [(assoc carrier :element-type opaque) (assoc carrier :element-type [opaque])
                     (assoc carrier :shape opaque) (assoc carrier :kind opaque)
                     (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/list :shape :homogeneous opaque :gravity.type/string]))
                     (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous opaque :gravity.type/string]))
                     {:kind :gravity.type/list :shape :homogeneous :element-type [opaque]}]]
        (is (false? (invoke 'body-is-value-type? [wrong])))
        (is (= :rejected (:status (invoke 'body-lc-validate-types [[wrong]]))))
        (is (zero? @callbacks))))))
(deftest body-string-set-carrier-comparison-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} s :gravity.type/string
        finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        c7 (sh08-helper 'invoke-c7)]
    (doseq [operation ['= 'not=]
            pair [[carrier carrier] [carrier (finite [])] [(finite []) carrier]
                  [carrier (finite [s s])] [(finite [s]) carrier]
                  [carrier :gravity.type/never] [:gravity.type/never carrier]]]
      (let [equality? (= operation '=) family (if equality? "kse" "ane")
            base ((sh08-helper (if equality? 'atomic-equality-input 'atomic-inequality-input)) pair)
            ids (zipmap (keys (:nodes base)) (map sha (range 1820 1830)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            mapped-call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                            (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            mapped-edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                            (update :ordered-evaluation-node-ids #(mapv ids %)))
            input (assoc base :core (assoc (:core base) :nodes node-list :calls [mapped-call] :call-edges [mapped-edge])
                              :node (last node-list) :nodes (into {} (map (juxt :node-id identity) node-list))
                              :call mapped-call :edge mapped-edge
                              :types (into {} (map (fn [[id type]] [(ids id) type]) (:types base))))
            {:keys [core node call edge nodes bindings types forms]} input
            result (c7 (symbol (str "sh08-" family "-derive")) [core node call edge nodes bindings types forms])
            descriptor (:descriptor result) id (:node-id node) op-id (:operator-node-id call)
            fact #(c7 (symbol (str "sh08-" family "-call-fact")) [% (:module core)])
            row #(c7 (symbol (str "sh08-" family "-constraint")) [%])
            facts {:module (:module core) :type-table (assoc types op-id descriptor id (:type result))
                   :function-type-table [] :call-type-facts [(fact descriptor)] :constraint-ledger [(row descriptor)]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context-name (if equality? 'body-ae-context 'body-ane-context)
            context-key (if equality? :equality-context :atomic-inequality-context)
            prepare #(leaf context-name request %1 bindings) context (prepare facts)
            checked (assoc facts context-key (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core checked {}) record (get-in graph [:nodes id])
            coherent (fn [changed] (-> checked (assoc-in [:type-table op-id] changed)
                                      (assoc :call-type-facts [(fact changed)] :constraint-ledger [(row changed)])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid checked)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (:ordered-evaluation-node-ids call) (:dependencies record) (:evaluated-children record)))
        (is (true? (leaf 'body-graph-equations-valid? core checked {} (:nodes graph))))


        (doseq [ledger [[] (into (:constraint-ledger checked) (:constraint-ledger checked))
                        (conj (:constraint-ledger checked) (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))]]
          (is (false? (valid (assoc checked :constraint-ledger ledger)))))
        (doseq [changed [(assoc descriptor :version (if equality? :sh08-string-set-equality-v1 :sh08-atomic-inequality-v1))
                         (if equality? (assoc descriptor :operand-counts [0 0])
                           (assoc-in descriptor [:string-set-equality :operand-counts] [0 0]))
                         (assoc descriptor :direct-effects [:memory/allocate])]]
          (is (false? (valid (coherent changed)))))
        (is (false? (valid (assoc-in checked [:type-table (first (:argument-node-ids call))] :gravity.type/set))))
        (when (= (second pair) :gravity.type/never)
          (let [operands {op-id {:type descriptor :transitive #{}}
                          (first (:argument-node-ids call)) {:type carrier :direct #{:io/write} :transitive #{:io/write}}
                          (second (:argument-node-ids call)) {:type :gravity.type/never :direct #{:error/throw} :transitive #{:error/throw}
                                                             :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
                transferred (leaf 'body-node-record node nil operands :gravity.type/never)]
            (is (= #{} (:direct transferred)))
            (is (= #{:io/write :error/throw} (:transitive transferred)))
            (is (= [s] (:escaping-thrown-error-types transferred)))))))))

(deftest body-string-set-carrier-equality-source-id-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string} c7 (sh08-helper 'invoke-c7)
        {:keys [core node call edge nodes bindings types forms]} ((sh08-helper 'atomic-equality-input) [carrier carrier])
        validation (c7 'sh08-sse-domain [[carrier carrier]])
        result (c7 'sh08-kse-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        facts {:module (:module core) :type-table (assoc types :operator descriptor :call :gravity.type/bool)
               :function-type-table [] :call-type-facts [(c7 'sh08-kse-call-fact [descriptor (:module core)])]
               :constraint-ledger [(c7 'sh08-kse-constraint [descriptor])]}
        context (leaf 'body-ae-context request facts bindings) checked (assoc facts :equality-context (:context context))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "C8 equality ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "C8 equality ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "C8 equality ID print"))))]
    (is (= :accepted (:status result) (:status context)))

    (doseq [bad [opaque [opaque] {:opaque opaque}]
            [changed-node changed-call]
            [[node (assoc call :argument-node-ids [bad :arg1])] [node (assoc call :argument-node-ids [:arg0 bad])]
             [node (assoc call :operator-node-id bad)] [node (assoc call :operator-binding-id bad)] [node (assoc call :core-node-id bad)]
             [(assoc node :node-id bad) call] [(assoc node :children [bad :arg0 :arg1]) call]
             [(assoc node :children [:operator bad :arg1]) call] [(assoc node :children [:operator :arg0 bad]) call]
             [(assoc-in node [:source :form-id] bad) call] [(assoc-in node [:source :syntax-id] bad) call]]]
      (let [changed-core (assoc core :nodes (mapv #(if (= :call (:node-id %)) changed-node %) (:nodes core)) :calls [changed-call])
            changed-request (assoc request :canonical-core-artifact changed-core)]
        (is (= :rejected (:status (leaf 'body-ae-context changed-request facts bindings))))
        (is (zero? @callbacks))
        (is (nil? (leaf 'body-kse-source core changed-node changed-call edge nodes bindings types (:module core) validation)))
        (is (false? (leaf 'body-kse-call-valid? core checked bindings changed-node changed-call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (let [owner-id (get-in node [:source :form-id]) changed (assoc-in forms [owner-id :child-form-ids] [:operator bad :arg1])
            changed-request (assoc-in request [:b47-context :authenticated-core-request :forms] (vec (vals changed)))
            changed-facts (assoc-in checked [:equality-context :forms] changed)]
        (is (= :rejected (:status (leaf 'body-ae-context changed-request facts bindings))))
        (is (false? (leaf 'body-kse-call-valid? core changed-facts bindings node call edge nodes)))
        (is (zero? @callbacks))))))

(deftest current-gravity-list-predicate-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'list-predicate-input)
        derive (ns-resolve upstream 'list-predicate-derived)
        invoke (ns-resolve upstream 'invoke-c7)]
    (doseq [type [:gravity.type/list :gravity.type/set :gravity.type/map :gravity.type/keyword :gravity.type/never
                  {:kind :gravity.type/list :shape :literal :element-types []}
                  {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string :gravity.type/bool]}
                  {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
                  {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword :gravity.type/string]} {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                  {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}]]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [type])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result))
                   :function-type-table [] :call-type-facts [(invoke 'sh08-listp-call-fact [descriptor module])]
                   :constraint-ledger [(invoke 'sh08-listp-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            context (leaf 'body-listp-context request facts bindings)
            checked (assoc facts :list-predicate-context (:context context))
            valid #(leaf 'body-listp-call-valid? core % bindings node call edge nodes)]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-listp-context request facts bindings)))
        (is (true? (valid checked)))
        (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
        (is (true? (leaf 'body-listp-node? node checked core)))
        (when (= :gravity.type/set type)
          (let [callbacks (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID compared")))
                          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "malformed predicate ID hashed"))))]
            (doseq [args [7 [] (vec (repeat 1025 hostile))]]
              (is (false? (leaf 'body-listp-call-valid? core checked bindings node (assoc call :argument-node-ids args) edge nodes))))
            (is (false? (leaf 'body-listp-call-valid? core checked bindings (assoc node :children 7) call edge nodes)))
            (is (= 0 @callbacks))))
        (let [operand (if (= :gravity.type/never type) #{:error/throw} #{:io/write})
              record (leaf 'body-node-record node nil {:operator {:type descriptor :transitive #{}}
                                                       :arg0 {:type type :transitive operand}}
                           (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool))]
          (is (= #{} (:direct record)))
          (is (= operand (:transitive record)))
          (is (nil? (:allocation record))))

        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:type-table :operator :query-semantics :refinement] :key-presence)
                         (assoc-in checked [:type-table :operator :query-semantics :operand-evaluation] :twice)
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? core changed bindings {}))))
        (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms] 7)]
          (is (= :rejected (:status (leaf 'body-listp-context changed facts bindings)))))
        (let [changed (assoc-in facts [:type-table :arg0] :gravity.type/unknown)]
          (is (= :rejected (:status (leaf 'body-listp-context request changed bindings)))))))
    (let [visits (atom 0)
          hostile (reify clojure.lang.Counted (count [_] 2048)
                         clojure.lang.Seqable (seq [_] (swap! visits inc) (throw (AssertionError. "unreserved C8 predicate traversal"))))
          {:keys [core bindings forms]} (input-fn [:gravity.type/keyword])
          request {:canonical-core-artifact (assoc core :calls hostile)
                   :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          _ (leaf 'body-listp-context request {} bindings)]

      (is (= 0 @visits)))))

(deftest current-gravity-list-predicate-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'list-predicate-input) [:gravity.type/list])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'list-predicate-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-listp-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-listp-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest current-gravity-list-predicate-id-guard-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        {:keys [core node call edge nodes bindings types forms module]} ((ns-resolve upstream 'list-predicate-input) [:gravity.type/list])
        c7 (ns-resolve upstream 'invoke-c7) result (c7 'sh08-listp-derive [core node call edge nodes bindings types forms])
        descriptor (:descriptor result) facts {:type-table (assoc types :operator descriptor :call (:type result))
                                             :function-type-table [] :call-type-facts [(c7 'sh08-listp-call-fact [descriptor module])]
                                             :constraint-ledger [(c7 'sh08-listp-constraint [descriptor])]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        context (leaf 'body-listp-context request facts bindings) checked (assoc facts :list-predicate-context (:context context))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "C8 List ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "C8 List ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "C8 List ID printing"))))]
    (is (= :accepted (:status context)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            [changed-node changed-call]
            [[node (assoc call :argument-node-ids [bad])] [node (assoc call :operator-node-id bad)]
             [node (assoc call :core-node-id bad)] [(assoc node :node-id bad) call]
             [(assoc node :children [bad :arg0]) call] [(assoc node :children [:operator bad]) call]
             [(assoc-in node [:source :form-id] bad) call] [(assoc-in node [:source :syntax-id] bad) call]]]
      (is (false? (leaf 'body-listp-call-valid? core checked bindings changed-node changed-call edge nodes)))
      (let [changed-core (assoc core :calls [changed-call] :nodes (mapv #(if (= :call (:node-id %)) changed-node %) (:nodes core)))
            changed-request (assoc request :canonical-core-artifact changed-core)]
        (is (= :rejected (:status (leaf 'body-listp-context changed-request facts bindings))))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}] index [0 1]]
      (let [changed (assoc-in checked [:list-predicate-context :forms :call :child-form-ids index] bad)]
        (is (false? (leaf 'body-listp-call-valid? core changed bindings node call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (let [changed-call (assoc call :operator-binding-id bad)
            changed-request (assoc-in request [:canonical-core-artifact :calls] [changed-call])
            empty-context (leaf 'body-listp-context changed-request facts bindings)]
        (is (= :accepted (:status empty-context)))
        (is (nil? (:context empty-context)))
        (is (false? (leaf 'body-listp-call-valid? core checked bindings node changed-call edge nodes)))
        (is (zero? @callbacks))))))

(deftest body-list-equality-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/list :shape :literal :element-types (vec %))
        nullable #(hash-map :kind :gravity.type/nil-or :value-type %)
        k :gravity.type/keyword s :gravity.type/string]
    (doseq [arguments [[(finite []) (finite [])] [(finite [k s]) (finite [s k])]
                       [(finite [(finite [s])]) (finite [(finite [k])])]
                       [:gravity.type/never (finite [k])] [(finite [s]) :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1350 1360)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            nodes (into {} (map (juxt :node-id identity) node-list)) node (last node-list)
            types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
            result (c7 'sh08-le-derive [core node call edge nodes (:bindings base) types (:forms base)])
            descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc types (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-le-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-le-constraint [descriptor])]}
            prepare #(leaf 'body-ae-context request % (:bindings base))
            context (prepare facts)
            facts (assoc facts :equality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])
            validation (get-in context [:context :list-domains (ids :call)])
            coherent (fn [changed]
                       (-> facts (assoc-in [:type-table (ids :operator)] changed)
                           (assoc :call-type-facts [(c7 'sh08-le-call-fact [changed (:module base)])]
                                  :constraint-ledger [(c7 'sh08-le-constraint [changed])])))]
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (= :accepted (:status validation)))
        (is (= arguments (:argument-types validation)))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))


        (doseq [changed [(assoc descriptor :version :sh08-atomic-equality-v1)
                         (assoc descriptor :kind :gravity.type/applied-atomic-equality)
                         (assoc descriptor :semantic-owner 'gravity.core)
                         (assoc-in descriptor [:equality-policy :leaves] :type-family-constants)
                         (assoc-in descriptor [:equality-policy :identity] :observed)
                         (assoc-in descriptor [:equality-policy :nested-vector-versus-atomic] :coerce)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(assoc facts :constraint-ledger [])
                         (update facts :constraint-ledger into (:constraint-ledger facts))
                         (update-in facts [:equality-context :list-domains] dissoc (ids :call))
                         (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/integer]))]]
          (is (false? (valid changed))))
        (let [bad (assoc-in facts [:type-table (ids :arg0)] (finite [:gravity.type/i64]))]
          (is (= :body-list-equality-operand-type-required (:reason (prepare bad)))))
        (let [wrong-arity (assoc-in request [:canonical-core-artifact :nodes 3 :children] [(ids :operator)])]
          (is (= :body-equality-binary-call-required
                 (:reason (leaf 'body-ae-context wrong-arity facts (:bindings base))))))
        (is (false? (leaf 'body-graph-equations-valid? core facts {}
                          (assoc-in (:nodes graph) [(ids :call) :direct] #{:memory/allocate}))))))))

(deftest current-gravity-list-equality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-equality-input) [{:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}
                                                                 {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'list-equality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-le-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-le-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest body-list-equality-id-guard-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-equality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/list :shape :literal :element-types (vec %))
        {:keys [core node call edge nodes bindings types forms module]} (input-fn [(finite [:gravity.type/string]) (finite [:gravity.type/string])])
        result (c7 'sh08-le-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        facts {:type-table (assoc types :operator descriptor :call :gravity.type/bool)
               :call-type-facts [(c7 'sh08-le-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-le-constraint [descriptor])]}
        prepared (leaf 'body-ae-context request facts bindings)
        facts (assoc facts :equality-context (:context prepared))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "List effects ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "List effects ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "List effects ID printing"))))]
    (is (= :accepted (:status prepared)))
    (is (true? (leaf 'body-le-call-valid? core facts bindings node call edge nodes)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            slot [[:calls 0 :core-node-id] [:calls 0 :operator-node-id] [:calls 0 :operator-binding-id]
                  [:calls 0 :argument-node-ids 0] [:calls 0 :argument-node-ids 1]
                  [:nodes 3 :node-id] [:nodes 3 :children 0] [:nodes 3 :children 1] [:nodes 3 :children 2]
                  [:nodes 3 :source :form-id] [:nodes 3 :source :syntax-id]]]
      (let [changed (assoc-in core slot bad)]
        (is (= :rejected (:status (leaf 'body-ae-context (assoc request :canonical-core-artifact changed) facts bindings))))
        (is (false? (leaf 'body-le-call-valid? changed facts bindings (last (:nodes changed)) (first (:calls changed)) edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}] i (range 3)]
      (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms]
                              (mapv #(if (= :call (:form-id %)) (assoc-in % [:child-form-ids i] bad) %) (vals forms)))]
        (is (= :rejected (:status (leaf 'body-ae-context changed facts bindings))))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (let [changed-nodes (assoc-in nodes [:operator :attributes :symbol] bad)
            validation (get-in prepared [:context :list-domains :call])]
        (is (nil? (leaf 'body-le-source core node call edge changed-nodes bindings types module validation)))
        (is (false? (leaf 'body-le-call-valid? core facts bindings node call edge changed-nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (is (false? (leaf 'body-le-call-valid? core (assoc-in facts [:equality-context :forms :operator :value] bad)
                        bindings node call edge nodes)))
      (is (zero? @callbacks)))))

(deftest current-gravity-list-inequality-source-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'atomic-inequality-input) derive (ns-resolve upstream 'atomic-inequality-derived)
        c7 (ns-resolve upstream 'invoke-c7)
        pairs ((ns-resolve upstream 'list-inequality-type-pairs))
        scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped) original @scoped
        observe (fn [thunk]
                  (let [calls (atom 0)
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when (= 'body-le-domain (nth args 2)) (swap! calls inc))
                                           (apply original args))} thunk)]
                    {:result result :calls @calls}))]
    (doseq [[left right] pairs]
      (let [{:keys [core node call edge nodes bindings types forms module] :as input} (input-fn [left right])
            result (derive input) descriptor (:descriptor result)
            facts {:type-table (assoc types :operator descriptor :call (:type result)) :function-type-table []
                   :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
                   :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
            measured (observe #(leaf 'body-ane-context request facts bindings))
            context (:result measured)
            checked (assoc facts :atomic-inequality-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            normal? (not-any? #{:gravity.type/never} [left right])
            graph {:operator {:type descriptor :transitive #{}}
                   :arg0 {:type left :transitive #{:io/write}} :arg1 {:type right :transitive #{:memory/allocate}}}
            record (leaf 'body-node-record node nil graph (:type result))]
        (is (= :accepted (:status context)) (pr-str context))
        (is (= context (leaf 'body-ane-context request facts bindings)))
        (is (= 1 (:calls measured)))
        (let [check (observe #(valid checked))]
          (is (true? (:result check)))
          (is (zero? (:calls check))))
        (let [failed (observe #(leaf 'body-ane-context request facts bindings))]


          (is (= 1 (:calls failed))))
        (is (= :sh08-list-inequality-v1 (get-in descriptor [:list-equality :version])))
        (is (= (:list-equality descriptor) (get-in (first (:constraint-ledger facts)) [:solution :list-equality])))
        (is (true? (leaf 'body-ane-node? node checked core)))
        (is (= #{} (:direct record)))
        (is (nil? (:allocation record)))
        (when normal? (is (= #{:io/write :memory/allocate} (:transitive record))))
        (doseq [changed [(assoc checked :constraint-ledger [])
                         (update checked :constraint-ledger into (:constraint-ledger checked))
                         (update checked :constraint-ledger conj (assoc-in (first (:constraint-ledger checked)) [:lhs :core-node-id] :orphan))
                         (assoc-in checked [:call-type-facts 0 :effects] [:memory/allocate])]]
          (is (false? (valid changed))))
        (doseq [changed [(assoc descriptor :operation '=) (assoc descriptor :version :forged)
                         (assoc-in descriptor [:inequality-policy :cross-family] :always-unequal)
                         (assoc-in descriptor [:inequality-policy :coercion] :host)
                         (assoc-in descriptor [:inequality-policy :operand-position] :tail)
                         (assoc descriptor :direct-effects [:memory/allocate]) (assoc descriptor :operand-type-work 0)
                         (dissoc descriptor :list-equality)
                         (assoc-in descriptor [:list-equality :version] :forged)
                         (assoc-in descriptor [:list-equality :validation-mode] :first-mismatch)
                         (assoc-in descriptor [:list-equality :safety] :runtime-checked)
                         (assoc-in descriptor [:list-equality :integer-type]
                                   (if (= :gravity.type/i8 (get-in descriptor [:list-equality :integer-type])) :gravity.type/u8 :gravity.type/i8))
                         (assoc-in descriptor [:list-equality :equality-policy :conversion] :widen)]]
          (let [forged (-> checked (assoc-in [:type-table :operator] changed)
                           (assoc :call-type-facts [(c7 'sh08-ane-call-fact [changed module])]
                                  :constraint-ledger [(c7 'sh08-ane-constraint [changed])]))]
            (is (false? (valid forged)))))
        (doseq [id [:arg0 :arg1]]
          (let [other (if (= id :arg0) right left)
                unsupported :gravity.type/decimal]
            (is (= :rejected (:status (leaf 'body-ane-context request (assoc-in facts [:type-table id] unsupported) bindings))))))))))

(deftest current-gravity-list-inequality-consumer-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        base ((ns-resolve upstream 'atomic-inequality-input) [{:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}
                                                                 {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}])
        ids (zipmap (keys (:nodes base)) (map sha (range 1300 1310)))
        convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                    (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert-node (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base) {:nodes node-list :calls [call] :call-edges [edge]
                                 :root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                          :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
        result ((ns-resolve upstream 'atomic-inequality-derived) input) descriptor (:descriptor result)
        invoke (ns-resolve upstream 'invoke-c7)
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                     {:module (:module base)
                      :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                      :function-type-table [] :local-binding-facts []
                      :call-type-facts [(invoke 'sh08-ane-call-fact [descriptor (:module base)])]
                      :constraint-ledger [(invoke 'sh08-ane-constraint [descriptor])]})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                           :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                        :nodes (leaf 'body-throw-semantic-nodes core))
        core (assoc core :identity-preimage preimage)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                    {:forms (vec (vals (:forms base))) :binding-table (vec (vals (:bindings base)))}}}
        analyze #(leaf 'body-analyze-module request %1 {})
        analyzed (analyze facts)]
    (is (= :accepted (:status result) (:status analyzed)) (pr-str analyzed))
    (when (= :accepted (:status analyzed))
      (is (= analyzed (analyze facts))))
    (doseq [changed [(assoc facts :constraint-ledger [])
                     (update facts :constraint-ledger into (:constraint-ledger facts))
                     (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] (sha 1399)))]]
      (is (= :rejected (:status (analyze changed)))))))

(deftest body-list-inequality-id-guard-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [input-fn (sh08-helper 'atomic-inequality-input) c7 (sh08-helper 'invoke-c7)
        finite #(hash-map :kind :gravity.type/list :shape :literal :element-types (vec %))
        {:keys [core node call edge nodes bindings types forms module]} (input-fn [(finite [:gravity.type/string]) (finite [:gravity.type/string])])
        result (c7 'sh08-ane-derive [core node call edge nodes bindings types forms]) descriptor (:descriptor result)
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
        facts {:type-table (assoc types :operator descriptor :call :gravity.type/bool)
               :call-type-facts [(c7 'sh08-ane-call-fact [descriptor module])]
               :constraint-ledger [(c7 'sh08-ane-constraint [descriptor])]}
        prepared (leaf 'body-ane-context request facts bindings)
        facts (assoc facts :atomic-inequality-context (:context prepared))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "List effects ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "List effects ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "List effects ID printing"))))]
    (is (= :accepted (:status prepared)))
    (is (true? (leaf 'body-ane-call-valid? core facts bindings node call edge nodes)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            slot [[:calls 0 :core-node-id] [:calls 0 :operator-node-id] [:calls 0 :operator-binding-id]
                  [:calls 0 :argument-node-ids 0] [:calls 0 :argument-node-ids 1]
                  [:nodes 3 :node-id] [:nodes 3 :children 0] [:nodes 3 :children 1] [:nodes 3 :children 2]
                  [:nodes 3 :source :form-id] [:nodes 3 :source :syntax-id]]]
      (let [changed (assoc-in core slot bad)]
        (is (= :rejected (:status (leaf 'body-ane-context (assoc request :canonical-core-artifact changed) facts bindings))))
        (is (false? (leaf 'body-ane-call-valid? changed facts bindings (last (:nodes changed)) (first (:calls changed)) edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}] i (range 3)]
      (let [changed (assoc-in request [:b47-context :authenticated-core-request :forms]
                              (mapv #(if (= :call (:form-id %)) (assoc-in % [:child-form-ids i] bad) %) (vals forms)))
            changed-context (leaf 'body-ane-context changed facts bindings)]
        (is (= :accepted (:status changed-context)))
        (is (false? (leaf 'body-ane-call-valid? core
                          (assoc facts :atomic-inequality-context (:context changed-context)) bindings node call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (let [changed-nodes (assoc-in nodes [:operator :attributes :symbol] bad)
            validation (get-in prepared [:context :checks :call :validation])]
        (is (nil? (leaf 'body-ane-source core node call edge changed-nodes bindings types module validation)))
        (is (false? (leaf 'body-ane-call-valid? core facts bindings node call edge changed-nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (is (false? (leaf 'body-ane-call-valid? core (assoc-in facts [:atomic-inequality-context :forms :operator :value] bad)
                        bindings node call edge nodes)))
      (is (zero? @callbacks)))))

(deftest body-keyword-set-insertion-development-kernel
  (let [s :gravity.type/keyword finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        carrier :gravity.type/set]
    (doseq [[receiver value] [[(finite []) s] [(finite [s s]) s] [(finite [s]) :gravity.type/never]]]
      (let [{:keys [core node call edge nodes bindings descriptor facts request ids]} (prepend-effect-input receiver value 0)
            context (leaf 'body-va-context request facts bindings)
            facts (assoc facts :append-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (:node-id node)]) normal? (not= value :gravity.type/never)
            coherent (fn [changed]
                       (let [fact ((sh08-helper 'invoke-c7) 'sh08-pl-call-fact [changed (:module core)])
                             row ((sh08-helper 'invoke-c7) 'sh08-pl-constraint [changed])]
                         (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                             (assoc :call-type-facts [fact] :constraint-ledger [row])
                             (assoc-in [:append-context :call-facts (:node-id node)] fact)
                             (assoc-in [:append-context :conj-rows (:node-id node)] row))))]
        (is (= :accepted (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (valid facts)))
        (is (= :keyword-set-insertion (get-in context [:context :families (:node-id node)])))
        (is (= (if normal? carrier :gravity.type/never) (:result-type descriptor)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= :sh09-keyword-set-insertion-allocation-v1 (get-in record [:allocation :rule-version])))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= :runtime-value-dependent (get-in record [:allocation :extent :result-cardinality])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= context (leaf 'body-va-context request facts bindings)))

        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [changed [(assoc descriptor :kind :gravity.type/applied-list-prepend)
                         (assoc descriptor :result-type (finite [s]) :return (finite [s]))
                         (assoc descriptor :result-extent 1)
                         (assoc-in descriptor [:insertion-semantics :present-class] :reject-duplicate)
                         (assoc-in descriptor [:allocation-policy :behavior] :never)
                         (assoc-in descriptor [:storage-policy :physical-sharing] :required)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(update-in graph [:nodes (:node-id node)] dissoc :allocation)
                         (assoc-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?] (not normal?))
                         (assoc-in graph [:nodes (:node-id node) :allocation :storage-policy :physical-sharing] :required)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (:nodes changed)))))
        (is (= :rejected (:status (leaf 'body-va-context request (assoc-in facts [:type-table (ids :arg0)] :gravity.type/set) bindings))))
        (is (false? (valid (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string))))
        (when-not normal?
          (let [operands {(:operator-node-id call) {:type descriptor :direct #{} :transitive #{}}
                          (ids :arg0) {:type receiver :direct #{:io/write} :transitive #{:io/write}}
                          (ids :arg1) {:type :gravity.type/never :direct #{:error/throw} :transitive #{:error/throw}
                                       :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
                derived (leaf 'body-node-record node nil operands :gravity.type/never)]
            (is (= #{} (:direct derived)))
            (is (= #{:io/write :error/throw} (:transitive derived)))
            (is (= [s] (:escaping-thrown-error-types derived)))))))
    (let [inputs [(prepend-effect-input (finite [s]) s 0)
                  (prepend-effect-input {:kind :gravity.type/list :shape :homogeneous :element-type s} s 1)
                  (prepend-effect-input {:kind :gravity.type/vector :shape :literal :element-types [s]} s 2)]
          core (assoc (:core (first inputs)) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs))
                      :calls (mapv :call inputs) :call-edges (mapv :edge inputs))
          forms (apply merge (map :forms inputs)) bindings (:bindings (first inputs))
          facts {:module (:module core) :type-table (apply merge (map #(get-in % [:facts :type-table]) inputs))
                 :function-type-table [] :call-type-facts (vec (mapcat #(get-in % [:facts :call-type-facts]) inputs))
                 :constraint-ledger (vec (mapcat #(get-in % [:facts :constraint-ledger]) inputs))}
          request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          context (leaf 'body-va-context request facts bindings) id (get-in inputs [0 :node :node-id])
          checked (assoc facts :append-context (:context context)) row (first (:constraint-ledger facts))]
      (is (= :accepted (:status context)) (pr-str context))
      (is (= {:keyword-set-insertion 1 :list-prepend 1 :vector-append 1} (frequencies (vals (get-in context [:context :families])))))
      (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
      (doseq [ledger [(subvec (:constraint-ledger facts) 1) (conj (:constraint-ledger facts) row)
                      (conj (:constraint-ledger facts) (assoc-in row [:lhs :core-node-id] :orphan))
                      (assoc (:constraint-ledger facts) 0 (assoc row :producer-rule :sh08-vector-append-v1 :lhs {:core-node-id id :projection :vector-append}))]]
        (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger ledger) bindings)))))
      (is (= :body-conj-constraint-family (:reason (leaf 'body-pl-conj-rows [row] {id :unknown-family}))))
      (let [callbacks (atom 0) opaque (proxy [Object] [] (hashCode [] (swap! callbacks inc) (throw (AssertionError. "conj row ID hash"))))]
        (doseq [changed [(assoc row :producer-rule opaque)
                         (assoc-in row [:lhs :core-node-id] opaque)
                         (assoc-in row [:lhs :core-node-id] [opaque])
                         (assoc-in row [:lhs :core-node-id] {:opaque opaque})]]
          (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger (assoc (:constraint-ledger facts) 0 changed)) bindings)))))
        (is (zero? @callbacks))))))

(deftest body-symbol-set-insertion-development-kernel
  (let [s :gravity.type/symbol finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/symbol}]
    (doseq [[receiver value] [[(finite []) s] [(finite [s s]) s] [(finite [s]) :gravity.type/never] [carrier s] [carrier :gravity.type/never]]]
      (let [{:keys [core node call edge nodes bindings descriptor facts request ids]} (prepend-effect-input receiver value 0)
            context (leaf 'body-va-context request facts bindings)
            facts (assoc facts :append-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % bindings {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (:node-id node)]) normal? (not= value :gravity.type/never)
            coherent (fn [changed]
                       (let [fact ((sh08-helper 'invoke-c7) 'sh08-pl-call-fact [changed (:module core)])
                             row ((sh08-helper 'invoke-c7) 'sh08-pl-constraint [changed])]
                         (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                             (assoc :call-type-facts [fact] :constraint-ledger [row])
                             (assoc-in [:append-context :call-facts (:node-id node)] fact)
                             (assoc-in [:append-context :conj-rows (:node-id node)] row))))]
        (is (= :accepted (:status context) (:status graph)) (pr-str [context graph]))
        (is (true? (valid facts)))
        (is (= :symbol-set-insertion (get-in context [:context :families (:node-id node)])))
        (is (= (if normal? carrier :gravity.type/never) (:result-type descriptor)))
        (is (= (if normal? #{:memory/allocate} #{}) (:direct record) (:transitive record)))
        (is (= :sh09-symbol-set-insertion-allocation-v2 (get-in record [:allocation :rule-version])))
        (is (= normal? (get-in record [:allocation :normal-completion-possible?])))
        (is (= :runtime-value-dependent (get-in record [:allocation :extent :result-cardinality])))
        (is (= (:storage-policy descriptor) (get-in record [:allocation :storage-policy])))
        (is (= context (leaf 'body-va-context request facts bindings)))

        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (doseq [changed [(assoc descriptor :version :sh08-symbol-set-insertion-v1)
                         (assoc-in descriptor [:insertion-semantics :symbol-equality] :display-string)
                         (assoc-in descriptor [:insertion-semantics :resolution-authority] :resolve)
                         (assoc-in descriptor [:insertion-semantics :normalization] :nfc)
                         (assoc descriptor :kind :gravity.type/applied-list-prepend)
                         (assoc descriptor :result-type (finite [s]) :return (finite [s]))
                         (assoc descriptor :result-extent 1)
                         (assoc-in descriptor [:insertion-semantics :present-class] :reject-duplicate)
                         (assoc-in descriptor [:allocation-policy :behavior] :never)
                         (assoc-in descriptor [:storage-policy :physical-sharing] :required)]]
          (is (false? (valid (coherent changed)))))
        (doseq [changed [(update-in graph [:nodes (:node-id node)] dissoc :allocation)
                         (assoc-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?] (not normal?))
                         (assoc-in graph [:nodes (:node-id node) :allocation :storage-policy :physical-sharing] :required)]]
          (is (false? (leaf 'body-graph-equations-valid? core facts {} (:nodes changed)))))
        (is (= :rejected (:status (leaf 'body-va-context request (assoc-in facts [:type-table (ids :arg0)] :gravity.type/set) bindings))))
        (is (false? (valid (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string))))
        (when-not normal?
          (let [operands {(:operator-node-id call) {:type descriptor :direct #{} :transitive #{}}
                          (ids :arg0) {:type receiver :direct #{:io/write} :transitive #{:io/write}}
                          (ids :arg1) {:type :gravity.type/never :direct #{:error/throw} :transitive #{:error/throw}
                                       :escaping-thrown-error-types [s] :escaping-thrown-errors #{s}}}
                derived (leaf 'body-node-record node nil operands :gravity.type/never)]
            (is (= #{} (:direct derived)))
            (is (= #{:io/write :error/throw} (:transitive derived)))
            (is (= [s] (:escaping-thrown-error-types derived)))))))
    (let [inputs [(prepend-effect-input (finite [s]) s 0)
                  (prepend-effect-input {:kind :gravity.type/list :shape :homogeneous :element-type s} s 1)
                  (prepend-effect-input {:kind :gravity.type/vector :shape :literal :element-types [s]} s 2)]
          core (assoc (:core (first inputs)) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs))
                      :calls (mapv :call inputs) :call-edges (mapv :edge inputs))
          forms (apply merge (map :forms inputs)) bindings (:bindings (first inputs))
          facts {:module (:module core) :type-table (apply merge (map #(get-in % [:facts :type-table]) inputs))
                 :function-type-table [] :call-type-facts (vec (mapcat #(get-in % [:facts :call-type-facts]) inputs))
                 :constraint-ledger (vec (mapcat #(get-in % [:facts :constraint-ledger]) inputs))}
          request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
          context (leaf 'body-va-context request facts bindings) id (get-in inputs [0 :node :node-id])
          checked (assoc facts :append-context (:context context)) row (first (:constraint-ledger facts))]
      (is (= :accepted (:status context)) (pr-str context))
      (is (= {:symbol-set-insertion 1 :list-prepend 1 :vector-append 1} (frequencies (vals (get-in context [:context :families])))))
      (is (true? (leaf 'body-call-joins-with-foreign-valid? core checked bindings {})))
      (doseq [ledger [(subvec (:constraint-ledger facts) 1) (conj (:constraint-ledger facts) row)
                      (conj (:constraint-ledger facts) (assoc-in row [:lhs :core-node-id] :orphan))
                      (assoc (:constraint-ledger facts) 0 (assoc row :producer-rule :sh08-vector-append-v1 :lhs {:core-node-id id :projection :vector-append}))]]
        (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger ledger) bindings)))))
      (is (= :body-conj-constraint-family (:reason (leaf 'body-pl-conj-rows [row] {id :unknown-family}))))
      (let [callbacks (atom 0) opaque (proxy [Object] [] (hashCode [] (swap! callbacks inc) (throw (AssertionError. "conj row ID hash"))))]
        (doseq [changed [(assoc row :producer-rule opaque)
                         (assoc-in row [:lhs :core-node-id] opaque)
                         (assoc-in row [:lhs :core-node-id] [opaque])
                         (assoc-in row [:lhs :core-node-id] {:opaque opaque})]]
          (is (= :rejected (:status (leaf 'body-va-context request (assoc facts :constraint-ledger (assoc (:constraint-ledger facts) 0 changed)) bindings)))))
        (is (zero? @callbacks))))))

(deftest body-symbol-set-insertion-id-guard-development-kernel
  (let [finite {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/symbol]}
        {:keys [core node call edge nodes bindings facts request ids]} (prepend-effect-input finite :gravity.type/symbol 0)
        prepared (leaf 'body-va-context request facts bindings)
        facts (assoc facts :append-context (:context prepared))
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "effects insertion ID printing"))))]
    (is (= :accepted (:status prepared)))
    (is (true? (leaf 'body-pl-call-valid? core facts bindings node call edge nodes)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:operator-node-id] [:operator-binding-id] [:argument-node-ids 0] [:argument-node-ids 1]]]
      (is (false? (leaf 'body-pl-call-valid? core facts bindings node (assoc-in call path bad) edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}]
            path [[:node-id] [:children 0] [:children 1] [:children 2] [:source :form-id] [:source :syntax-id]]]
      (is (false? (leaf 'body-pl-call-valid? core facts bindings (assoc-in node path bad) call edge nodes)))
      (is (zero? @callbacks)))
    (doseq [bad [opaque [opaque] {:opaque opaque}] i (range 3)]
      (let [changed (assoc-in facts [:append-context :forms (ids :call) :child-form-ids i] bad)]
        (is (false? (leaf 'body-pl-call-valid? core changed bindings node call edge nodes)))
        (is (zero? @callbacks))))
    (doseq [bad [opaque [opaque] {:opaque opaque}]]
      (is (false? (leaf 'body-pl-call-valid? core
                        (assoc-in facts [:append-context :forms (ids :operator) :value] bad) bindings node call edge nodes)))
      (is (false? (leaf 'body-pl-call-valid? core facts bindings node call edge
                        (assoc-in nodes [(ids :operator) :attributes :symbol] bad))))
      (is (zero? @callbacks)))))

(deftest body-symbol-set-carrier-development-kernel
  (let [invoke (fn [function args] (apply leaf function args))
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/symbol}
        string-carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string}
        s :gravity.type/symbol finite #(hash-map :kind :gravity.type/set :shape :literal :element-types (vec %))
        nested {:kind :gravity.type/list :shape :literal :element-types [carrier]}
        work #(invoke 'body-lc-validate-types [%1])
        produced (:result-type (invoke (quote body-syin-validate-types) [[(finite []) s]]))]
    (is (= carrier produced))
    (is (true? (invoke 'body-syc-type? [produced])))
    (is (false? (invoke 'body-ss-type? [produced])))
    (is (false? (invoke 'body-is-value-type? [produced])))
    (is (false? (invoke 'body-ds-type? [produced])))
    (is (= {:status :accepted} (work [produced])))


    (is (= {:status :accepted} (work [nested])))

    (is (= {:status :accepted} (work [string-carrier])))
    (is (= {:status :accepted} (work [:gravity.type/set])))
    (is (= {:status :accepted} (work [:gravity.type/never])))
    (is (= :nested-never (:reason (work [{:kind :gravity.type/list :shape :literal :element-types [:gravity.type/never]}]))))
    (is (= :rejected (:status (invoke (quote body-syin-validate-types) [[:gravity.type/set s]]))))
    (is (= :rejected (:status (invoke (quote body-syin-validate-types) [[:gravity.type/unknown s]]))))
    (doseq [value [s :gravity.type/never]]
      (let [checked (invoke (quote body-syin-validate-types) [[carrier value]]) expected (if (= value :gravity.type/never) :gravity.type/never carrier)]
        (is (= :accepted (:status checked)))
        (is (= expected (:result-type checked)))
        (is (nil? (:input-extent checked)))
        (is (not-any? #(contains? checked %) [:work-count :operand-work :result-work :construction-work]))))
    (is (= :accepted (:status (invoke 'body-ksm-domain [[carrier s]]))))
    (is (= :rejected (:status (invoke 'body-kse-domain [[carrier carrier]]))))
    (is (= :rejected (:status (invoke 'body-kse-domain [[carrier (finite [s])]]))))
    (require 'gravity.self-hosting.sh08-function-call-type-test)
    (doseq [[input-name family expected] [['list-construction-input "lc" nested]
                                         ['set-predicate-input "setp" :gravity.type/bool]
                                         ['list-predicate-input "listp" :gravity.type/bool]
                                         ['keyword-predicate-input "kp" :gravity.type/bool]]]
      (let [input ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test input-name) [produced])
            {:keys [core node call edge nodes bindings types module]} input
            checked (work [produced])
            header (invoke (symbol (str "body-" family "-source")) [core node call edge nodes bindings types module checked])
            descriptor (invoke (symbol (str "body-" family "-finish")) [header])]
        (is (map? header))
        (is (= [produced] (:argument-types descriptor)))
        (is (= expected (:result-type descriptor) (:return descriptor)))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "Symbol carrier hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "Symbol carrier equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "Symbol carrier printing"))))
          bad [(assoc carrier :extra true) (dissoc carrier :element-type)
               (assoc carrier :kind :gravity.type/vector) (assoc carrier :shape :literal)
               (assoc carrier :element-type :gravity.type/keyword) (assoc carrier :element-type :gravity.type/unknown)
               (assoc carrier :element-type carrier) (assoc carrier :element-type opaque)
               (assoc carrier :element-type [opaque]) (assoc carrier :element-type {:opaque opaque})
               (assoc carrier :kind opaque) (assoc carrier :shape opaque)
               (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous opaque s]))
               (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous [opaque] s]))
               (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous {:opaque opaque} s]))]]
      (doseq [wrong bad]
        (is (false? (invoke 'body-syc-type? [wrong])))
        (is (= :rejected (:status (work [wrong]))))
        (is (zero? @callbacks))))
    (let [nest (fn [depth value] (loop [remaining depth result value] (if (zero? remaining) result (recur (dec remaining) {:kind :gravity.type/list :shape :literal :element-types [result]}))))
                     wide {:kind :gravity.type/list :shape :literal :element-types (vec (repeat 1025 carrier))}
                     late (assoc wide :element-types (assoc (:element-types wide) 1024 :gravity.type/unknown))]
                 (doseq [value [(nest 127 carrier) (nest 129 carrier) wide]]
                   (is (= {:status :accepted} (work [value]))))
                 (is (= :unsupported-type (:reason (work [late])))))))
(deftest body-symbol-set-carrier-contains-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        input-fn (ns-resolve upstream 'map-contains-input) derive (fn [input] ((ns-resolve upstream 'keyword-set-contains-derived) input))
        c7 (ns-resolve upstream 'invoke-c7)
        carrier (:result-type (c7 (quote sh08-syin-validate-types) [[{:kind :gravity.type/set, :shape :literal, :element-types []} :gravity.type/symbol]]))]
    (doseq [arguments [[carrier :gravity.type/symbol]
                       [carrier :gravity.type/never]]]
      (let [base (input-fn arguments) ids (zipmap (keys (:nodes base)) (map sha (range 1250 1260)))
            convert-node (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                                        (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
            node-list (mapv convert-node (get-in base [:core :nodes]))
            call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                     (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
            edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                     (update :ordered-evaluation-node-ids #(mapv ids %)))
            core (assoc (:core base) :nodes node-list :calls [call] :call-edges [edge])
            input (assoc base :core core :nodes (into {} (map (juxt :node-id identity) node-list)) :node (last node-list)
                         :call call :edge edge :types (into {} (map (fn [[k v]] [(ids k) v]) (:types base))))
            result (derive input) descriptor (:descriptor result)
            request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
            facts {:module (:module base) :type-table (assoc (:types input) (ids :operator) descriptor (ids :call) (:type result))
                   :function-type-table [] :call-type-facts [(c7 'sh08-ksm-call-fact [descriptor (:module base)])]
                   :constraint-ledger [(c7 'sh08-ksm-constraint [descriptor])]}
            context (leaf 'body-mc-context request facts (:bindings base))
            facts (assoc facts :map-contains-context (:context context))
            valid #(leaf 'body-call-joins-with-foreign-valid? core % (:bindings base) {})
            graph (leaf 'body-derive-nodes-with-foreign core facts {})
            record (get-in graph [:nodes (ids :call)])]
        (is (= :sh08-symbol-set-contains-v2 (:version descriptor)))
        (is (= :gravity/homogeneous-symbol-set-type-v1 (get-in descriptor [:membership-policy :carrier-schema])))
        (is (some #{:inferred-member-carrier} (:invalidation descriptor)))
        (is (nil? (:set-member-count descriptor)))
        (is (not (contains? descriptor :type-domain-work-count)))
        (is (= :accepted (:status result) (:status context) (:status graph)) (pr-str graph))
        (is (true? (valid facts)))
        (is (= #{} (:direct record) (:transitive record)))
        (is (nil? (:allocation record)))
        (is (= (mapv ids [:operator :arg0 :arg1]) (:dependencies record) (:evaluated-children record)))
        (is (nil? (:call-body-dependency record)))
        (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes graph))))
        (is (= context (leaf 'body-mc-context request facts (:bindings base))))

        (doseq [changed [(assoc descriptor :version :sh08-symbol-set-contains-v1)
                         (update descriptor :membership-policy dissoc :carrier-schema)
                         (assoc-in descriptor [:membership-policy :comparison] :identity)
                         (assoc-in descriptor [:membership-policy :member-uniqueness] :not-validated)
                         (assoc-in descriptor [:membership-policy :budget] :traversal-dependent)
                         (assoc descriptor :type-domain-work-count 0 :set-member-count 99)
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string)
                         (assoc-in descriptor [:source-binding :package :version] "forged")]]
          (let [candidate (-> facts (assoc-in [:type-table (ids :operator)] changed)
                              (assoc :call-type-facts [(c7 'sh08-ksm-call-fact [changed (:module base)])]
                                     :constraint-ledger [(c7 'sh08-ksm-constraint [changed])]))]
            (is (false? (valid candidate)))))
        (doseq [candidate [(assoc-in facts [:constraint-ledger 0 :producer-rule] :sh08-symbol-set-contains-v1)
                           (assoc-in facts [:call-type-facts 0 :fact-id-request :domain] :gravity/sh08-symbol-set-contains-call-type-fact-v1)
                           (assoc-in facts [:constraint-ledger 0 :constraint-id-request :domain] :gravity/sh08-symbol-set-contains-constraint-v1)
                           (assoc facts :constraint-ledger [])
                           (update facts :constraint-ledger into (:constraint-ledger facts))
                           (update facts :constraint-ledger conj (assoc-in (first (:constraint-ledger facts)) [:lhs :core-node-id] :orphan))
                           (update-in facts [:map-contains-context :keyword-set-domains] dissoc (ids :call))
                           (assoc-in facts [:type-table (ids :arg1)] :gravity.type/string)]]
          (is (false? (valid candidate))))
        (when (= arguments [carrier :gravity.type/symbol])
          (let [core (merge core {:root-core-node-ids [(ids :call)] :definitions [] :lexical-bindings []
                                 :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                                 :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
                preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                                   :evaluation-order :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                                :nodes (leaf 'body-throw-semantic-nodes core))
                core (assoc core :identity-preimage preimage)
                request (-> request (assoc :canonical-core-artifact core)
                            (assoc-in [:b47-context :authenticated-core-request :binding-table] (vec (vals (:bindings base)))))
                facts (merge facts (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]) {:local-binding-facts []})
                analyze #(leaf 'body-analyze-module request facts {})
                analyzed (analyze)]
            (is (= :accepted (:status analyzed)) (pr-str analyzed))
)))))
  (let [invoke (fn [name args] (apply leaf name args))
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/symbol}
        callbacks (atom 0) opaque (proxy [Object] []
          (hashCode [] (swap! callbacks inc) (throw (AssertionError. "membership carrier hash")))
          (equals [_] (swap! callbacks inc) (throw (AssertionError. "membership carrier equality")))
          (toString [] (swap! callbacks inc) (throw (AssertionError. "membership carrier printing"))))
        bad [(assoc carrier :extra true) (dissoc carrier :element-type)
             (assoc carrier :kind :gravity.type/vector) (assoc carrier :shape :literal)
             (assoc carrier :element-type :gravity.type/unknown) (assoc carrier :element-type opaque)
             (assoc carrier :element-type [opaque]) (assoc carrier :kind opaque) (assoc carrier :shape opaque)
             (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous opaque :gravity.type/symbol]))
             (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous [opaque] :gravity.type/symbol]))
             (clojure.lang.PersistentArrayMap. (object-array [:kind :gravity.type/set :shape :homogeneous {:opaque opaque} :gravity.type/symbol]))]]
    (doseq [needle [:gravity.type/symbol :gravity.type/never]]
        (let [checked (invoke 'body-ksm-domain [[carrier needle]])]
        (is (= :accepted (:status checked)))
        (is (nil? (:set-member-count checked)))
          (is (= checked (invoke 'body-ksm-domain [[carrier needle]])))))
    (doseq [width [0 2 4 65536]]
      (let [walks (atom 0) reads (atom 0)
            receiver (proxy [clojure.lang.APersistentMap] []
                       (count [] width)
                       (seq [] (swap! walks inc) (throw (AssertionError. "non-three-field membership record traversed")))
                       (valAt ([key] (swap! reads inc) (throw (AssertionError. "non-three-field record read")))
                              ([key fallback] (swap! reads inc) (throw (AssertionError. "non-three-field record read")))))]
        (doseq [needle [:gravity.type/symbol :gravity.type/keyword :gravity.type/string :gravity.type/character :gravity.type/never]]
          (is (= :rejected (:status (invoke 'body-ksm-domain [[receiver needle]]))))
          (is (zero? @walks))
          (is (zero? @reads)))))
    (doseq [wrong bad]
      (is (= :rejected (:status (invoke 'body-ksm-domain [[wrong :gravity.type/symbol]]))))
      (is (zero? @callbacks)))
    (doseq [wrong [opaque [opaque] {:opaque opaque}]]
      (is (= :rejected (:status (invoke 'body-ksm-domain [[carrier wrong]]))))
      (is (zero? @callbacks)))
    (doseq [needle [:gravity.type/keyword :gravity.type/string :gravity.type/character :gravity.type/unknown]]
      (is (= :rejected (:status (invoke 'body-ksm-domain [[carrier needle]])))))
    ))

(defn- vector-first-symbol-loop-c8-cold-input []
  ;; Complete normalized DATA only. No C5/C6, artifact seal, or admission claim.
  (require 'gravity.self-hosting.sh08-loop-recur-type-test)
  (let [owner 'gravity.self-hosting.sh08-loop-recur-type-test
        invoke (ns-resolve owner 'c7-invoke)
        input ((ns-resolve owner 'vector-first-symbol-loop-cold-source-shape)
               ((ns-resolve owner 'vector-first-symbol-loop-cold-data)))
        core (:core input) nodes (:nodes input) forms (:forms input) bindings (:bindings input)
        quotes (invoke 'sh08-qd-products [(:nodes core) (vec (vals forms)) (:module core)])
        skeletons (invoke 'sh08-ft-function-skeletons [(:function-records core) (:module core)])
        context {:ordering-core core :loop-core core :lookup-nodes nodes :source-node-table nodes
                 :lookup-forms (vec (vals forms)) :lookup-bindings (vec (vals bindings))
                 :boolean-matches [] :quote-types (:type-table quotes)
                 :shape-call-products {:module (:module core) :calls (:calls core) :call-edges (:call-edges core)
                   :definitions (:definitions core)
                   :function-index (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
                   :recursion-components []}}
        inference (invoke 'sh08-ft-infer-acyclic-with-context
                    [(:nodes core) nodes (:lexical-bindings core) (:definitions core) (:calls core) (:call-edges core)
                     skeletons {} context])
        record (first (:function-records core))
        record (assoc record :ordinal 0 :definition-kind :named-top-level
                       :definition-core-node-id (:core-node-id (first (:definitions core)))
                       :function-form-id (get-in nodes [(:function-core-node-id record) :source :form-id]))
        locals (mapv (fn [record]
                       (let [owner (some (fn [node] (when (and (= :let (:core-form node))
                                                       (some #{(:initializer-node-id record)} (:children node)))
                                                     (:node-id node))) (:nodes core))]
                         (assoc record :name (:name (bindings (:binding-id record))) :let-core-node-id owner)))
                     (:lexical-bindings core))
        core (merge core {:function-records [record] :lexical-bindings locals
                    :root-core-node-ids [(:definition-core-node-id record)]
                    :nodes (mapv (fn [node] (if (= (:node-id node) (:definition-core-node-id record))
                                             (assoc-in node [:attributes :name-syntax-id] (:definition-syntax-id record)) node)) (:nodes core))
                    :match-branch-records [] :match-pattern-records [] :match-decision-skeletons []
                    :evaluation-order [] :recursion-components [] :error-transfers [] :error-handlers []})
        core (assoc core :identity-preimage
               (merge (:identity-preimage core)
                 (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls :evaluation-order
                                    :lexical-bindings :match-branch-records :match-decision-skeletons :match-pattern-records])
                 {:nodes (leaf 'body-throw-semantic-nodes core)}))
        functions (invoke 'sh08-ft-final-functions [(invoke 'sh08-ft-function-facts [(:function-types inference) nodes (:module core)])])
        facts (merge (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings])
                {:module (:module core) :type-table (:type-table inference) :function-type-table functions
                 :local-binding-facts (invoke 'sh08-ft-local-facts [locals (:type-table inference) (:binding-types inference) nodes (:module core)])
                 :call-type-facts (invoke 'sh08-ft-call-facts-with-context [(:calls core) (:call-edges core) nodes (:type-table inference)
                                    (:function-types inference) (:module core) (:shape-call-context inference)])
                 :constraint-ledger (into (:constraints inference) (:constraints quotes))})
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request
                   {:forms (vec (vals forms)) :binding-table (vec (vals bindings))}}}]
    {:request request :facts facts :inference inference :input input}))

(deftest body-private-evidence-keyword-work-development-kernel
  ;; One logical scalar visit; String/Symbol formatting remains separately observed.
  (require 'gravity.self-hosting.sh08-loop-recur-type-test)
  (let [owner 'gravity.self-hosting.sh08-loop-recur-type-test
        observe (ns-resolve owner 'vector-first-symbol-loop-observe-data-work)
        cases ((ns-resolve owner 'vector-first-symbol-loop-keyword-cases))]
    (doseq [[label value] cases]
      (let [{:keys [formats]} (observe 'body-pc-evidence-work #(leaf 'body-pc-evidence-work value))]

        (is (= :rejected (:status (leaf 'body-pc-evidence-work value))) (str label))
        (is (zero? (:keyword formats)) (str label))))
    (doseq [value ["source text" 'library/member]]
      (let [{:keys [formats]} (observe 'body-pc-evidence-work #(leaf 'body-pc-evidence-work value))]


        (is (zero? (:keyword formats)))
        (when (symbol? value) (is (= 1 (:symbol formats))))))
    (let [data {:kind :closed :source ["source" 'library/member 7] :nested {:status :normal}}
          {:keys [result formats]} (observe 'body-pc-evidence-work #(leaf 'body-pc-evidence-work data))]
      (is (= :accepted (:status result)))
      (is (zero? (:keyword formats)))
      (is (= result (leaf 'body-pc-evidence-work data))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "evidence hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "evidence equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "evidence printing"))))
          deferred (lazy-seq (swap! callbacks inc) (list opaque))]
      (doseq [value [opaque [opaque] {:field opaque} (list opaque) #{:closed} deferred]]
        (is (= :rejected (:status (leaf 'body-pc-evidence-work value))))
        (is (zero? @callbacks)))

      (is (zero? @callbacks)))
    (is (= :map-get-source-mismatch
           (:reason (leaf 'body-pc-evidence-work (nth (iterate vector :closed) 129)))))))

(deftest body-vector-first-symbol-loop-cumulative-work-development-kernel
  (let [{:keys [request facts inference]} (vector-first-symbol-loop-c8-cold-input)
        core (:canonical-core-artifact request)
        observe (ns-resolve 'gravity.self-hosting.sh08-loop-recur-type-test 'vector-first-symbol-loop-observe-data-work)
        analyze #(leaf 'body-analyze-module request %1 {})
        {:keys [result formats]} (observe 'body-pc-evidence-work #(analyze facts))]
    (is (= :converged (:convergence-status inference)))
    (is (= [] (:diagnostics inference)))
    (is (true? (leaf 'body-shape-valid? core facts)))

    (is (= result (analyze facts)))
    (is (zero? (:keyword formats)))
    (is (pos? (:symbol formats)))
    (when (= :accepted (:status result))
      (let [graph (:graph result)
            first-id (:core-node-id (first (:calls core)))
            vector-id (first (:argument-node-ids (first (:calls core))))
            function-id (:function-syntax-id (first (:function-records core)))
            invariant-row (first (keep-indexed (fn [i row] (when (get-in row [:solution :member-invariant]) i)) (:constraint-ledger facts)))]
        (is (= result (analyze facts)))

        (is (= #{} (get-in graph [:nodes first-id :direct])))
        (is (= #{:memory/allocate} (get-in graph [:nodes vector-id :direct])))
        (is (= #{:memory/allocate} (get-in graph [:functions function-id :inferred])))
        (is (= :checked (get-in graph [:loop-fixed-point :equations])))
        (is (integer? invariant-row))
        (is (= :rejected (:status (analyze (assoc-in facts [:constraint-ledger invariant-row :solution :member-invariant :grounding :member-path] [])))))
        (is (= :rejected (:status (analyze (assoc-in facts [:type-table (:operator-node-id (first (:calls core))) :bounds-proof :selected-index] 1)))))))
    (println (pr-str {:vector-first-symbol-loop-normalized-c8 (select-keys result [:status :reason])
                     :formats formats :source-credit false :authentication-credit false}))))

(deftest cold-quote-metadata-effects-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  ;; Preserve the historical test name while exercising only semantic cold evidence.
  (let [{:keys [core facts record bindings forms input]} ((sh08-helper 'cold-metadata-input))
        cut {:status :accepted :mode (get-in input [:cut :mode])
             :excluded-node-ids (get-in input [:cut :excluded-node-ids])}
        cold {:mode (:mode input) :cut cut
              :source-argument-call-node-ids (:source-argument-call-node-ids input)}
        run #(leaf 'body-is-cold-metadata-valid? core facts record bindings forms % cut)]
    (is (true? (run cold)))
    (doseq [changed ((sh08-helper 'cold-metadata-invalid-inputs) cold)]
      (is (false? (run changed))))))
