(ns gravity.c2-artifact-identity-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-artifact-identity :as identity]))

(defn- sha256-hex
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        bytes (.digest digest
                       (.getBytes (str value)
                                  java.nio.charset.StandardCharsets/UTF_8))]
    (format "%064x" (java.math.BigInteger. 1 bytes))))

(defn- span
  [source file start end]
  {:source source
   :file file
   :byte-start start
   :byte-end end
   :start {:line 1 :column (inc start)}
   :end {:line 1 :column (inc end)}})

(def source-a "/checkout/a/source.gravity")
(def source-b "/tmp/other-checkout/source.gravity")
(def source-id "sha256:source")
(def file-id "sha256:file")
(def source-span-a (span source-a file-id 0 3))
(def source-span-b (span source-b file-id 0 3))

(defn- base-source-unit
  ([retain-comments?]
   (base-source-unit retain-comments? source-a source-span-a))
  ([retain-comments? path first-span]
   {:path path
    :source-id source-id
    :identity-inputs {:project-relative-path "src/source.gravity"
                      :encoding :utf-8
                      :bytes-hash "sha256:bytes"
                      :reader-options {:retain-comments retain-comments?
                                       :enabled-features #{:standard-reader}
                                       :extension-policy "sha256:extension"}}
    :bytes-hash "sha256:bytes"
    :reader-options {:retain-comments retain-comments?
                     :enabled-features #{:standard-reader}
                     :extension-policy "sha256:extension"}
    :span first-span}))

(defn- hash-operations
  ([] (hash-operations {}))
  ([extra]
   (merge {:sha256-hex sha256-hex} extra)))

(defn- canonical-hash
  [value]
  (identity/with-operations
   (hash-operations)
   #(identity/reader-canonical-hash value)))

(def simple-token-stream-a
  [{:token-id :tok-0
    :kind :symbol
    :raw "x"
    :source-id source-id
    :source-path source-a
    :span source-span-a
    :trivia-before []}])

(def simple-form-tree-a
  [{:form-id :form-0
    :kind :symbol
    :collection-kind nil
    :children []
    :parent-form-id nil
    :value 'x
    :metadata {:line 1}
    :source-id source-id
    :source-path source-a
    :span source-span-a
    :surface-span source-span-a
    :origin {:kind :source
             :source-id source-id
             :source-path source-a}
    :generated-origin []}])

(def simple-syntax-seeds-a
  [{:syntax-id :syntax-0
    :form-id :form-0
    :span source-span-a
    :generated-origin [{:from source-span-a :reader-abbreviation :quote}]}])

(def simple-extension-invocations-a
  [{:extension-tag 'foo/bar
    :source-path source-a
    :span source-span-a
    :invocations [{:name 'bar :span source-span-a}]
    :payload {:stable true}}])

(def simple-diagnostics-a
  [{:id "C2-READ"
    :source-span source-span-a
    :primary {:message "bad form" :span source-span-a}
    :related [{:label "related" :span source-span-a}]
    :origin-chain [{:path source-a :span source-span-a :kind :source}]
    :other {:source source-a}}])

(defn- graph-operations
  ([metrics] (graph-operations metrics {}))
  ([metrics extra]
   (hash-operations
    (merge {:c2-form-graph-metrics (constantly metrics)
            :c2-reader-fail!
            (fn [& args]
              (throw (ex-info "C2-HASH" {:reader-fail-args args})))
            :source-span (fn [path form-index]
                           {:source path :form-index form-index})
            :max-reader-form-graph-depth 8}
           extra))))

(deftest canonical-value-is-typed-and-order-stable
  (let [left (array-map
              :b '(a [1])
              :a {:d 4 :c true}
              :c #{'z :x 1})
        right (array-map
               :c #{:x 1 'z}
               :a (hash-map :c true :d 4)
               :b (list 'a [1]))]
    (is (= [:map [[:a [:map [[:c true] [:d 4]]]]
                [:b [:list ['a [:vector [1]]]]]
                [:c [:set [1 :x 'z]]]]]
           (identity/reader-canonical-value left)))
    (is (= (identity/reader-canonical-value left)
           (identity/reader-canonical-value right)))
    (is (= [:vector [1 2]]
           (identity/reader-canonical-value [1 2])))
    (is (= [:list [1 2]]
           (identity/reader-canonical-value '(1 2))))
    (is (= [:set [1 :x 'z]]
           (identity/reader-canonical-value #{'z :x 1})))
    (is (= [:map [[:a 1] [:b 2]]]
           (identity/reader-canonical-value (array-map :b 2 :a 1)))))
  (identity/with-operations
   (hash-operations)
   #(let [left (array-map :b 2 :a 1)
          right (array-map :a 1 :b 2)]
      (is (= (identity/reader-canonical-hash left)
             (identity/reader-canonical-hash right)))
      (is (not= (identity/reader-canonical-hash [1 2])
                (identity/reader-canonical-hash '(1 2))))
      (is (not= (identity/reader-canonical-hash #{1 2})
                (identity/reader-canonical-hash [1 2])))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (identity/reader-canonical-hash left)))
      ;; Legacy canonical projection intentionally drops collection metadata.
      (is (= (identity/reader-canonical-hash {:a 1})
             (identity/reader-canonical-hash
              (with-meta {:a 1} {:tag 'annotated}))))
      ;; Scalar metadata remains visible because the scalar survives projection.
      (is (not= (identity/reader-canonical-hash 'x)
                (identity/reader-canonical-hash
                 (with-meta 'x {:tag 'annotated})))))))

(deftest path-neutral-span-and-token-projection-preserve-legacy-fields
  (let [neutral (identity/c2-path-neutral-span source-span-a)
        token-input (identity/c2-token-hash-input
                     [{:token-id :tok-0
                       :source-id source-id
                       :source-path source-a
                       :span source-span-a
                       :raw "x"
                       :extra :retained}])]
    (is (= {:file file-id
            :byte-start 0 :byte-end 3
            :start {:line 1 :column 1}
            :end {:line 1 :column 4}}
           neutral))
    (is (= :not-a-map (identity/c2-path-neutral-span :not-a-map)))
    (is (= [{:token-id :tok-0
             :source-id source-id
             :span {:file file-id
                    :byte-start 0 :byte-end 3
                    :start {:line 1 :column 1}
                    :end {:line 1 :column 4}}
             :raw "x"
             :extra :retained}]
           token-input))
    (is (not (contains? (first token-input) :source-path)))
    (is (= file-id (get-in token-input [0 :span :file])))))

(deftest form-and-syntax-projections-are-exact
  (let [surface (span source-a file-id 0 4)
        origin-span (span source-a "sha256:origin-file" 1 2)
        form-tree
        [{:form-id :form-0
          :kind :list
          :children [:form-1]
          :source-path source-a
          :source-id source-id
          :span source-span-a
          :surface-span surface
          :origin {:kind :source
                   :source-path source-a
                   :source source-a
                   :source-id source-id}
          :generated-origin [{:from origin-span
                              :reader-abbreviation :quote
                              :expanded-form '(quote x)}]
          :value '(x)
          :metadata {:line 1}
          :ignored :preserved}
         {:form-id :form-1
          :kind :symbol
          :children []
          :source-path source-a
          :span source-span-a}]
        projected (identity/c2-form-hash-input form-tree)
        seed-input (identity/c2-syntax-seed-hash-input
                    [{:syntax-id :syntax-0
                      :source-path source-a
                      :span source-span-a
                      :generated-origin [{:from origin-span :kind :source}]
                      :other :retained}
                     {:syntax-id :syntax-1 :span nil}])]
    (is (= (dissoc (first form-tree) :source-path
                   :span :surface-span :origin :generated-origin)
           (select-keys (first projected) [:form-id :kind :children :source-id
                                           :value :metadata :ignored])))
    (is (= {:form-id :form-0
            :kind :list
            :children [:form-1]
            :source-id source-id
            :span {:file file-id :byte-start 0 :byte-end 3
                   :start {:line 1 :column 1} :end {:line 1 :column 4}}
            :surface-span {:file file-id :byte-start 0 :byte-end 4
                           :start {:line 1 :column 1} :end {:line 1 :column 5}}
            :origin {:kind :source :source source-a :source-id source-id}
            :generated-origin [{:from {:file "sha256:origin-file"
                                       :byte-start 1 :byte-end 2
                                       :start {:line 1 :column 2}
                                       :end {:line 1 :column 3}}
                               :reader-abbreviation :quote
                               :expanded-form '(quote x)}]
            :value '(x)
            :metadata {:line 1}
            :ignored :preserved}
           (first projected)))
    (is (= [{:syntax-id :syntax-0
             :source-path source-a
             :span {:file file-id :byte-start 0 :byte-end 3
                    :start {:line 1 :column 1} :end {:line 1 :column 4}}
             :generated-origin [{:from {:file "sha256:origin-file"
                                        :byte-start 1 :byte-end 2
                                        :start {:line 1 :column 2}
                                        :end {:line 1 :column 3}}
                                :kind :source}]
             :other :retained}
            {:syntax-id :syntax-1 :span nil}]
           seed-input))
    (is (= source-a (:source-path (first (identity/c2-syntax-seed-hash-input
                                         [{:source-path source-a}])))))))

(deftest extension-and-diagnostic-projections-remove-their-designated-paths
  (let [extension (identity/c2-extension-hash-input simple-extension-invocations-a)
        diagnostics (identity/c2-diagnostic-hash-input simple-diagnostics-a)]
    (is (= [{:extension-tag 'foo/bar
             :span {:byte-start 0 :byte-end 3
                    :start {:line 1 :column 1}
                    :end {:line 1 :column 4}}
             :invocations [{:name 'bar
                            :span {:byte-start 0 :byte-end 3
                                   :start {:line 1 :column 1}
                                   :end {:line 1 :column 4}}}]
             :payload {:stable true}}]
           extension))
    (is (not (contains? (first extension) :source-path)))
    (is (= [{:id "C2-READ"
             :source-span {:file file-id :byte-start 0 :byte-end 3
                           :start {:line 1 :column 1}
                           :end {:line 1 :column 4}}
             :primary {:message "bad form"
                       :span {:file file-id :byte-start 0 :byte-end 3
                              :start {:line 1 :column 1}
                              :end {:line 1 :column 4}}}
             :related [{:label "related"
                        :span {:file file-id :byte-start 0 :byte-end 3
                               :start {:line 1 :column 1}
                               :end {:line 1 :column 4}}}]
             :origin-chain [{:span {:file file-id :byte-start 0 :byte-end 3
                                    :start {:line 1 :column 1}
                                    :end {:line 1 :column 4}}
                             :kind :source}]
             :other {:source source-a}}]
           diagnostics))
    (is (not (contains? (first (get-in diagnostics [0 :origin-chain])) :path)))
    (is (= source-a (get-in diagnostics [0 :other :source])))
    (is (= file-id (get-in diagnostics [0 :source-span :file])))))

(deftest projections-and-incremental-hashes-are-checkout-path-neutral
  (let [token-a simple-token-stream-a
        token-b (assoc-in token-a [0 :source-path] source-b)
        token-b (assoc-in token-b [0 :span] source-span-b)
        form-b (-> simple-form-tree-a
                   (assoc-in [0 :source-path] source-b)
                   (assoc-in [0 :span] source-span-b)
                   (assoc-in [0 :surface-span] source-span-b)
                   (assoc-in [0 :origin :source-path] source-b))
        syntax-b (-> simple-syntax-seeds-a
                     (assoc-in [0 :span] source-span-b)
                     (assoc-in [0 :generated-origin 0 :from] source-span-b))
        extension-b (-> simple-extension-invocations-a
                        (assoc-in [0 :source-path] source-b)
                        (assoc-in [0 :span] source-span-b)
                        (assoc-in [0 :invocations 0 :span] source-span-b))
        diagnostics-b (-> simple-diagnostics-a
                          (assoc-in [0 :source-span] source-span-b)
                          (assoc-in [0 :primary :span] source-span-b)
                          (assoc-in [0 :related 0 :span] source-span-b)
                          (assoc-in [0 :origin-chain 0 :path] source-b)
                          (assoc-in [0 :origin-chain 0 :span] source-span-b))
        result-a
        (identity/with-operations
         (graph-operations {:acyclic? true :max-form-depth 2})
         #(identity/c2-incremental-hashes
           (base-source-unit false source-a source-span-a)
           token-a simple-form-tree-a simple-syntax-seeds-a
           simple-extension-invocations-a simple-diagnostics-a))
        result-b
        (identity/with-operations
         (graph-operations {:acyclic? true :max-form-depth 2})
         #(identity/c2-incremental-hashes
           (base-source-unit false source-b source-span-b)
           token-b form-b syntax-b extension-b diagnostics-b))]
    (is (= result-a result-b))
    (is (= {:artifact :gravity/reader-incremental-hashes
            :source-unit source-id
            :token-stream (:token-stream result-a)
            :form-tree (:form-tree result-a)
            :syntax-seed-stream (:syntax-seed-stream result-a)
            :extension-invocation-set (:extension-invocation-set result-a)
            :reader-diagnostics (:reader-diagnostics result-a)
            :retained-trivia-affects-form-tree? false
            :status :stable}
           result-a))
    (is (= false (:retained-trivia-affects-form-tree? result-a)))
    (is (= (canonical-hash
            (identity/c2-semantic-form-hash-input simple-form-tree-a))
           (:form-tree result-a)))))

(deftest incremental-hashes-retain-trivia-switches-form-projection
  (let [without-trivia
        (identity/with-operations
         (graph-operations {:acyclic? true :max-form-depth 2})
         #(identity/c2-incremental-hashes
           (base-source-unit false)
           simple-token-stream-a simple-form-tree-a simple-syntax-seeds-a
           simple-extension-invocations-a simple-diagnostics-a))
        with-trivia
        (identity/with-operations
         (graph-operations {:acyclic? true :max-form-depth 2})
         #(identity/c2-incremental-hashes
           (base-source-unit true)
           simple-token-stream-a simple-form-tree-a simple-syntax-seeds-a
           simple-extension-invocations-a simple-diagnostics-a))]
    (is (false? (:retained-trivia-affects-form-tree? without-trivia)))
    (is (true? (:retained-trivia-affects-form-tree? with-trivia)))
    (is (= (canonical-hash
            (identity/c2-semantic-form-hash-input simple-form-tree-a))
           (:form-tree without-trivia)))
    (is (= (canonical-hash
            (identity/c2-form-hash-input simple-form-tree-a))
           (:form-tree with-trivia)))
    (is (not= (:form-tree without-trivia) (:form-tree with-trivia)))))

(deftest incremental-hashes-reject-reader-cycles-with-c2-hash
  (let [failures (atom [])
        operations
        (graph-operations
         {:acyclic? false :max-form-depth 2}
         {:c2-reader-fail!
          (fn [& args]
            (swap! failures conj args)
            (throw (ex-info "cycle" {:args args})))})
        ex (try
             (identity/with-operations
              operations
              #(identity/c2-incremental-hashes
                (base-source-unit false)
                simple-token-stream-a simple-form-tree-a [] [] []))
             nil
             (catch clojure.lang.ExceptionInfo error error))
        [id path subject extra] (first @failures)]
    (is (instance? clojure.lang.ExceptionInfo ex))
    (is (= "C2-HASH" id))
    (is (= source-a path))
    (is (= {:stage :read-source
            :source-id source-id
            :source-span source-span-a
            :reader-options (:reader-options (base-source-unit false))}
           subject))
    (is (= {:missing-fields [:acyclic-reader-form-graph]
            :facts {:failure-kind :reader-form-cycle}}
           extra))
    (is (= [id path subject extra]
           (-> ex ex-data :args)))))

(deftest incremental-hashes-reject-reader-depth-overflow-with-c2-hash
  (let [failures (atom [])
        operations
        (graph-operations
         {:acyclic? true :max-form-depth 9}
         {:max-reader-form-graph-depth 8
          :c2-reader-fail!
          (fn [& args]
            (swap! failures conj args)
            (throw (ex-info "depth" {:args args})))})
        ex (try
             (identity/with-operations
              operations
              #(identity/c2-incremental-hashes
                (base-source-unit false)
                simple-token-stream-a simple-form-tree-a [] [] []))
             nil
             (catch clojure.lang.ExceptionInfo error error))
        [id path subject extra] (first @failures)]
    (is (instance? clojure.lang.ExceptionInfo ex))
    (is (= "C2-HASH" id))
    (is (= source-a path))
    (is (= source-id (:source-id subject)))
    (is (= source-span-a (:source-span subject)))
    (is (= {:missing-fields [:bounded-reader-form-depth]
            :facts {:observed-form-depth 9
                    :maximum-form-depth 8
                    :failure-kind :reader-resource-depth-limit}}
           extra))
    (is (= [id path subject extra]
           (-> ex ex-data :args)))))

(deftest product-integrity-record-is-exact-and-path-neutral
  (let [source-unit (base-source-unit false)
        incremental {:artifact :gravity/reader-incremental-hashes
                     :source-unit source-id
                     :form-tree "sha256:forms"
                     :token-stream "sha256:tokens"}
        literal-records
        [{:literal-id :lit-0 :form-id :form-0 :kind :string
          :span source-span-a :decoded "x"}]
        deferred-records
        [{:literal-id :lit-1 :form-id :form-1 :kind :ratio
          :span (span source-a "sha256:deferred-file" 1 2)
          :decoded {:semantic-validation :deferred}}]
        operations (hash-operations)]
    (identity/with-operations
     operations
     #(let [record (identity/c2-reader-product-integrity-record
                    source-unit [:form-0] incremental
                    literal-records deferred-records)
            same-record (identity/c2-reader-product-integrity-record
                         (base-source-unit false source-b source-span-b)
                         [:form-0] incremental
                         (assoc-in literal-records [0 :span] source-span-b)
                         (assoc-in deferred-records [0 :span]
                                   (span source-b "sha256:deferred-file" 1 2)))
            input (:input record)]
        (is (= :gravity/c2-reader-product-integrity (:artifact record)))
        (is (= :sha256 (:algorithm record)))
        (is (= :verified (:status record)))
        (is (= {:source-id source-id
                :source-identity-inputs (:identity-inputs source-unit)
                :source-bytes-hash "sha256:bytes"
                :reader-options (:reader-options source-unit)
                :top-level-form-ids [:form-0]
                :incremental-reader-hashes incremental
                :literal-records-hash
                (identity/reader-canonical-hash
                 [{:literal-id :lit-0 :form-id :form-0 :kind :string
                   :span (dissoc source-span-a :source) :decoded "x"}])
                :deferred-literal-records-hash
                (identity/reader-canonical-hash
                 [{:literal-id :lit-1 :form-id :form-1 :kind :ratio
                   :span (dissoc (span source-a "sha256:deferred-file" 1 2)
                                 :source)
                   :decoded {:semantic-validation :deferred}}] )}
               input))
        (is (= (identity/reader-canonical-hash input)
               (:integrity-hash record)))
        (is (= (identity/reader-canonical-hash
                [{:literal-id :lit-0 :form-id :form-0 :kind :string
                  :span (dissoc source-span-a :source) :decoded "x"}])
               (get-in record [:input :literal-records-hash])))
        (is (= record same-record))))))

(deftest artifact-id-preimage-is-exact-and-checkout-path-neutral
  (let [integrity {:artifact :gravity/c2-reader-product-integrity
                   :integrity-hash "sha256:integrity"
                   :input {:source-id source-id}}
        hashes {:artifact :gravity/reader-incremental-hashes
                :source-unit source-id
                :form-tree "sha256:forms"}
        artifact
        {:kind :gravity/stage0-c2-reader-document-artifact
         :task :compile
         :document-set [:d0]
         :source-unit-record {:source-id source-id
                              :path source-a
                              :project-root-record {:path "/checkout"}}
         :reader-product-integrity integrity
         :incremental-reader-hashes hashes
         :representation-boundary {:schema :c2}
         :source-overrides {:retain-comments false}
         :capability-based-proof {:stable true}
         :ignored-path source-a}
        artifact-b (-> artifact
                       (assoc :ignored-path source-b)
                       (assoc-in [:source-unit-record :path] source-b)
                       (assoc-in [:source-unit-record :project-root-record :path]
                                 "/tmp/other-checkout"))]
    (identity/with-operations
     (hash-operations)
     #(let [id-a (identity/c2-reader-artifact-id artifact)
            id-b (identity/c2-reader-artifact-id artifact-b)
            preimage {:kind (:kind artifact)
                      :task (:task artifact)
                      :document-set (:document-set artifact)
                      :source-id source-id
                      :reader-product-integrity integrity
                      :incremental-reader-hashes hashes
                      :representation-boundary (:representation-boundary artifact)
                      :source-overrides (:source-overrides artifact)
                      :capability-based-proof (:capability-based-proof artifact)}]
        (is (= id-a id-b))
        (is (re-matches #"sha256:[0-9a-f]{64}" id-a))
        (is (= (identity/reader-canonical-hash preimage) id-a))
        (is (not= id-a
                  (identity/c2-reader-artifact-id
                   (assoc artifact :task :different-task))))))))

(deftest operation-map-validation-and-nested-interposition-are-strict
  (doseq [operations [nil
                      []
                      {:unknown identity}
                      {:sha256-hex :not-a-function}
                      {:max-reader-form-graph-depth 0}
                      {:max-reader-form-graph-depth -1}
                      {:max-reader-form-graph-depth :unbounded}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (identity/with-operations {} :not-a-function)))
  (let [failure
        (try
          (identity/with-operations
           {:sha256-hex sha256-hex
            :c2-form-graph-metrics
            (constantly {:acyclic? true :max-form-depth 0})}
           #(identity/c2-incremental-hashes
             (base-source-unit false) [] [] [] [] []))
          nil
          (catch clojure.lang.ExceptionInfo ex (ex-data ex)))]
    (is (= :max-reader-form-graph-depth (:operation failure))))
  (let [calls (atom [])
        operations
        (hash-operations
         {:reader-canonical-value
          (fn [value]
            (swap! calls conj [:canonical value])
            (identity/reader-canonical-value value))})]
    (identity/with-operations
     operations
     #(is (= (str "sha256:" (sha256-hex "[:map [[:a 1]]]"))
             (identity/reader-canonical-hash {:a 1}))))
    (is (= [[:canonical {:a 1}]] @calls)))
  (let [calls (atom [])
        operations
        (graph-operations
         {:acyclic? true :max-form-depth 2}
         {:c2-token-hash-input
          (fn [tokens]
            (swap! calls conj tokens)
            (identity/c2-token-hash-input tokens))})]
    (identity/with-operations
     operations
     #(identity/c2-incremental-hashes
       (base-source-unit false)
       simple-token-stream-a simple-form-tree-a [] [] []))
    (is (= [simple-token-stream-a] @calls))))

(deftest entrypoint-trampoline-preserves-recursive-var-interposition
  (let [calls (atom [])
        value (array-map :a [1 (array-map :b 2)])]
    (letfn [(replacement [item]
              (swap! calls conj item)
              (if (= 2 item)
                99
                (identity/call-entrypoint-body
                 :reader-canonical-value
                 identity/reader-canonical-value
                 [item])))]
      (identity/with-operations
       {:reader-canonical-value replacement}
       #(is (= [:map [[:a [:vector [1 [:map [[:b 99]]]]]]]]
               (identity/call-entrypoint-body
                :reader-canonical-value
                identity/reader-canonical-value
                [value])))))
    (is (= [:a [1 {:b 2}] 1 {:b 2} :b 2] @calls)))
  (doseq [[operation-key operation args]
          [[:unknown identity []]
           [:reader-canonical-value :not-a-function []]
           [:reader-canonical-value identity :not-sequential]]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/call-entrypoint-body
                  operation-key operation args)))))

(deftest private-contract-public-parity-dependency-and-authority-boundary
  (let [contract-var (get (ns-interns 'gravity.c2-artifact-identity)
                          'namespace-contract)
        contract (var-get contract-var)
        public-api (set (keys (:public-api contract)))]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c2-artifact-identity (:namespace contract)))
    (is (= public-api (set (keys (ns-publics 'gravity.c2-artifact-identity)))))
    (is (= #{:sha256-hex
             :c2-form-graph-metrics
             :c2-reader-fail!
             :source-span
             :reader-canonical-value
             :reader-canonical-hash
             :c2-semantic-form-hash-input
             :c2-path-neutral-span
             :c2-token-hash-input
             :c2-form-hash-input
             :c2-syntax-seed-hash-input
             :c2-extension-hash-input
             :c2-diagnostic-hash-input
             :c2-incremental-hashes
             :c2-reader-product-integrity-record
             :c2-reader-artifact-id
             :max-reader-form-graph-depth}
           (get-in contract [:operation-interposition :accepted-keys])))
    (is (true? (get-in contract
                       [:operation-interposition :partial-overrides?])))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['clojure.core]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :canonical-source-identity
                   :cache-reuse-authority
                   :diagnostic-policy
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (= [:hosted-c2-reader-records] (:artifact-inputs contract)))
    (is (= [:hosted-c2-incremental-hashes
            :hosted-c2-reader-product-integrity
            :hosted-c2-reader-artifact-id]
           (:artifact-outputs contract)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c2-authority? contract)))
    (is (false? (:cache-reuse-authority? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.diagnostics)))))
