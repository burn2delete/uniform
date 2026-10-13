(ns gravity.self-hosting.sh07-b51-attempt28-root6-independent-replay-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def root6-paths
  ["bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"])

(defn root6-forms [path]
  (bootstrap/read-forms path (slurp path)))

(def definitions
  (delay
    (into {}
          (map (fn [form] [(second form) form]))
          (filter #(and (seq? %) (= 'defn (first %)))
                  (mapcat root6-forms root6-paths)))))

(defn call-heads [form]
  (set
   (keep (fn [value]
           (when (and (seq? value) (symbol? (first value)))
             (first value)))
         (tree-seq coll? seq form))))

(defn definition-closure [root]
  (loop [pending [root] members #{}]
    (if (empty? pending)
      members
      (let [name (first pending)]
        (if (contains? members name)
          (recur (rest pending) members)
          (let [calls (filter #(contains? @definitions %)
                              (call-heads (get @definitions name)))]
            (recur (into (vec (rest pending)) calls)
                   (conj members name))))))))

(def runtime
  (delay
    (let [runtime-ns (create-ns
                      (gensym "gravity.sh07.b51.attempt28.root6-"))]
      (binding [*ns* runtime-ns]
        (clojure.core/refer 'clojure.core)
        (intern runtime-ns 'sh07-declared-digest-hash
                bootstrap/reader-canonical-hash)
        (intern runtime-ns 'sh07-canonical-text-compare
                bootstrap/p15-s23-sh07-canonical-text-compare)
        (intern runtime-ns 'sh07-observe-utf8-byte-count
                bootstrap/p15-s23-sh07-observe-utf8-byte-count)
        (intern runtime-ns 'sh07-b51-admitted-executable-carrier-validation?
                (fn [& _]
                  (throw
                   (ex-info "receipt-only builtin reached in compact replay"
                            {:function
                             'sh07-b51-admitted-executable-carrier-validation?}))))
        (intern runtime-ns 'sort-by-pr-str
                (fn [values] (apply list (sort-by pr-str values))))
        (doseq [[name] @definitions]
          (intern runtime-ns name))
        (doseq [[_ form] @definitions]
          (eval form)))
      runtime-ns)))

(defn invoke [function & arguments]
  (apply (deref (ns-resolve @runtime function)) arguments))

(deftest root6-linked-replay-has-independent-closed-surface
  (let [entrypoint 'sh07-b51-build-independent-verifier-binding
        closure (definition-closure entrypoint)
        calls (mapcat #(call-heads (get @definitions %)) closure)
        linked-definitions
        (filter #(or (.startsWith (name %) "sh07-b51-r6-linked-")
                     (= % 'sh07-b51-r6-v18-executable-authentic?)
                     (= % 'sh07-b51-r6-monolithic-executable-rejected?))
                (keys @definitions))]
    (is (contains? closure entrypoint))
    (is (contains? closure 'sh07-b51-r6-v18-executable-authentic?))
    (is (not-any? #(or (.startsWith (name %) "sh07-b51-producer-")
                       (.startsWith (name %) "sh07-b51-root8-"))
                  calls))
    (is (= #{'sh07-b51-r6-v18-executable-authentic?}
           (set
            (for [name linked-definitions
                  :when (contains?
                         (call-heads (get @definitions name))
                         'sh07-declared-digest-hash)]
              name))))))

(deftest root6-linked-schedule-and-row-schemas-are-exact
  (let [schedule (invoke 'sh07-b51-r6-linked-schedule)
        examples
        {:roots {:root-ordinal 1 :coordinate [3 'root] :arity 1}
         :members {:member-ordinal 0 :coordinate [3 'member]}
         :functions {:member-ordinal 0 :coordinate [3 'member]
                     :definition-source-hash "sha256:function"}
         :local-runtime-call-edges
         {:edge-ordinal 0 :caller [3 'caller] :callee [3 'callee]
          :arity 1 :source-callsite-ordinal 0}
         :external-runtime-call-edges
         {:semantic-edge-ordinal 0 :m1-edge-ordinal 19
          :manifest-id "sha256:manifest" :caller [3 'caller]
          :callee [4 'callee] :arity 1 :owner-edge :root6-internal
          :route {:from 3 :to 4}}
         :referenced-builtins
         {:occurrence-ordinal 0 :caller [3 'caller] :builtin 'map?
          :arity 1 :compiler-internal? false :source-callsite-ordinal 0}
         :module-contracts
         {:module-ordinal 3 :module-source-id "sha256:module"}}]
    (is (= 35 (count schedule)))
    (is (= [16 16 3 7 65536]
           (mapv schedule
                 [:rows-per-leaf :fold-fanout :maximum-fold-levels
                  :category-count :maximum-total-rows])))
    (doseq [[category row] examples]
      (is (true? (invoke 'sh07-b51-r6-linked-row-shape? category row)))
      (is (false? (invoke 'sh07-b51-r6-linked-row-shape?
                          category (assoc row :forged true)))))))

(deftest root6-m2-preorder-and-m0-builtin-order-are-independent
  (let [definition
        {:instructions
         [{:op :if
           :test {:op :builtin-call :function '= :args []}
           :then {:op :function-call :function 'callee
                  :args [{:op :builtin-call :function 'get :args []}]}
           :else {:op :literal :value false}}]}
        occurrences
        (:occurrences
         (invoke 'sh07-b51-r6-linked-function-occurrences definition))
        descriptor
        {:module-ordinal 0 :module-source-id "sha256:m0"
         :source-sha256 "sha256:source"
         :definitions
         [{:name 'caller :arity 0 :visibility :private
           :source-hash "sha256:function"}]
         ;; Deliberately differs from M2 preorder. M0 owns builtin row order.
         :builtin-calls
         [{:caller 'caller :caller-function-source-hash "sha256:function"
           :builtin 'get :arity 0 :compiler-internal? false}
          {:caller 'caller :caller-function-source-hash "sha256:function"
           :builtin '= :arity 0 :compiler-internal? false}]}
        plan-carriers
        [{:module-ordinal 0 :module-source-id "sha256:m0" :plan-id "sha256:m2"
          :plan {:plan-id "sha256:m2"
                 :sh07-b51-link-manifest-id "sha256:m1"
                 :functions {'caller definition}}}]
        rows
        (invoke 'sh07-b51-r6-linked-builtin-rows
                [descriptor] plan-carriers ['caller])]
    (is (= [['= :builtin] ['callee :function] ['get :builtin]]
           (mapv (juxt :function :kind) occurrences)))
    (is (vector? rows))
    (is (= ['get '=] (mapv :builtin rows)))
    (is (= [0 1] (mapv :source-callsite-ordinal rows)))
    (is (= :linked-builtin-m2-multiset
           (:reason
            (invoke 'sh07-b51-r6-linked-builtin-rows
                    [(update descriptor :builtin-calls pop)]
                    plan-carriers ['caller]))))))

(deftest root6-linked-profile-enforces-prospective-scalar-bound
  (let [schedule (invoke 'sh07-b51-r6-linked-schedule)
        at-limit (apply str (repeat 4096 "a"))
        over-limit (str at-limit "a")
        digest-text (str "sha256:" (apply str (repeat 4096 "9")))]
    (is (= :rejected
           (:status (invoke 'sh07-b51-r6-ratio-text digest-text))))
    (is (true? (invoke 'sh07-b51-r6-linked-class-bounds?
                       :row schedule at-limit)))
    (is (false? (invoke 'sh07-b51-r6-linked-class-bounds?
                        :row schedule over-limit)))))

(deftest root6-readable-name-admits-only-the-special-bare-slash
  (is (= {:status :accepted :text "/"}
         (invoke 'sh07-b51-r6-readable-full-name (symbol "/") false)))
  (doseq [value [(symbol "//") (symbol "/x") (symbol "x//y")]]
    (is (= :unreadable-name
           (:reason
            (invoke 'sh07-b51-r6-readable-full-name value false)))))
  (is (= :unreadable-name
         (:reason
          (invoke 'sh07-b51-r6-readable-full-name (keyword "/") true)))))

(deftest root6-old-monolithic-carrier-is-recognized-only-for-rejection
  (testing "the obsolete spelling is detected without digest authority"
    (is (true?
         (invoke 'sh07-b51-r6-monolithic-executable-rejected?
                 {:v18-executable-descriptor
                  {:domain
                   :gravity/sh07-b51-v18-executable-closure-carrier-v18
                   :schema-version 18}})))
    (doseq [descriptor
            [nil {}
             {:domain
              :gravity/sh07-b51-v18-executable-closure-carrier-v18
              :schema-version 17}
             {:domain
              :gravity/sh07-b51-v18-linked-executable-closure-carrier-v3
              :schema-version 3}]]
      (is (false?
           (invoke 'sh07-b51-r6-monolithic-executable-rejected?
                   {:v18-executable-descriptor descriptor})))))
  (is (not (contains?
            (call-heads
             (get @definitions
                  'sh07-b51-r6-monolithic-executable-rejected?))
            'sh07-declared-digest-hash))))
