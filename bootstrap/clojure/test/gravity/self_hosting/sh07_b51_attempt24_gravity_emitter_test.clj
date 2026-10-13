(ns gravity.self-hosting.sh07-b51-attempt24-gravity-emitter-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(def emitter-source "bootstrap/gravity/p15_s23/emitter.gravity")
(def compiler-source "bootstrap/gravity/p15_s23/compiler.gravity")

(def emitter
  (delay
    (let [forms
          (:forms
           (bootstrap/p15-s23-compiler-source-form-record-from-text
            compiler-source (slurp compiler-source)))]
      (bootstrap/p15-s23-compiler-def-value
       compiler-source forms 'p15-s23-stage2-plan-emitter))))

(def emitter-plan
  (delay
    (let [plan
          (bootstrap/p15-s23-stage2-compiler-artifact-plan
           @emitter emitter-source (slurp emitter-source))]
      ;; The frozen compiler-only collection primitive boundary still names
      ;; checked-core.  This pin-independent harness changes only the runtime
      ;; context record so it can execute the newly compiled Gravity emitter;
      ;; final acceptance remains blocked on the governed artifact pin refresh.
      (assoc plan :module
             {:module 'gravity.checked-core
              :profile :meta :target :jvm
              :effects #{} :capabilities #{} :safety :safe}))))

(def augmented-emitter
  (delay
    (bootstrap/p15-s23-stage2-compiler-artifact-augmented-emitter @emitter)))

(def link-authority
  (delay (bootstrap/p15-s23-sh07-b51-build-link-authority)))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :attempt24-gravity-emitter-test
    :compiler-artifact-plan? true}
   @emitter-plan function arguments))

(def digest-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def digest-b
  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
(def digest-c
  "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
(def digest-d
  "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
(def digest-e
  "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
(def digest-f
  "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")

(defn- fixture [body path]
  (let [edge
        {:ordinal 0
         :caller-module-source-id digest-a
         :caller-function 'caller
         :caller-function-source-hash digest-b
         :callsite-path path
         :callee-module-source-id digest-d
         :callee-function 'external
         :callee-function-source-hash digest-e
         :arity 1
         :owner-edge :producer-internal
         :authorization :authenticated-b51-namespace-external}
        edges
        (mapv
         (fn [ordinal]
           (if (zero? ordinal)
             edge
             (assoc edge
                    :ordinal ordinal
                    :callsite-path [ordinal])))
         (range 1423))
        binding
        {:symbol 'external
         :callee-module-ordinal 1
         :callee-module 'callee.module
         :callee-source-sha256 digest-f
         :callee-definition-source-hash digest-e
         :arity 1
         :owner-edge :producer-internal}
        descriptor
        {:artifact :gravity/sh07-b51-module-source-v1
         :schema-version 1
         :module-source-id digest-a
         :module-ordinal 0
         :module 'caller.module
         :logical-source-path "caller.gravity"
         :owner :r1-producer
         :source-byte-count 1
         :source-sha256 digest-a
         :profile :meta :target :jvm
         :effects #{} :capabilities #{} :safety :safe
         :definitions
         [{:name 'caller :arity 1 :visibility :private
           :source-hash digest-b}]
         :exports []
         :core-binding-count 262
         :lexical-binding-count 1
         :external-bindings [binding]
         :aggregate-binding-count 265
         :builtin-calls []}
        manifest
        {:artifact :gravity/sh07-b51-link-manifest-v1
         :schema-version 1
         :manifest-id digest-c
         :module-source-ids
         [digest-a digest-d :module-2 :module-3 :module-4 :module-5 :module-6]
         :edges edges}
        definition
        {:name 'caller :params ['x] :arity 1 :body body}
        module
        {:module 'caller.module
         :profile :meta :target :jvm
         :effects #{} :capabilities #{} :safety :safe :exports []
         :function-table {}
         :p15-s23-sh07-module-source-descriptor descriptor
         :p15-s23-sh07-link-manifest manifest}]
    {:edge edge :binding binding :descriptor descriptor :manifest manifest
     :definition definition :module module}))

(defn- compile-fixture [{:keys [module definition descriptor manifest]}]
  (invoke
   'p15-s23-compile-function
   [@emitter
    (assoc module
           :p15-s23-sh07-module-source-descriptor descriptor
           :p15-s23-sh07-link-manifest manifest)
    definition]))

(defn- only-instruction [result]
  (first (:instructions (:value result))))

(defn- compile-authentic-module [ordinal]
  (let [{:keys [module-source-descriptors link-manifest sources]}
        @link-authority
        descriptor (nth module-source-descriptors ordinal)
        source (nth sources ordinal)
        module
        (assoc (:module source)
               :function-table (:table source)
               :p15-s23-sh07-module-source-descriptor descriptor
               :p15-s23-sh07-link-manifest link-manifest)
        authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         (:path source) (:text source) module (:table source))
        module (assoc module :p15-s23-sh07-source-authority authority)
        definitions
        (mapv (fn [[name definition]] [name definition])
              (sort-by key (:table source)))]
    (invoke 'p15-s23-assemble-plan-products
            [@augmented-emitter module definitions])))

(defn- authentic-function-context [ordinal function-name]
  (let [{:keys [module-source-descriptors link-manifest sources]}
        @link-authority
        descriptor (nth module-source-descriptors ordinal)
        source (nth sources ordinal)
        module
        (assoc (:module source)
               :function-table (:table source)
               :p15-s23-sh07-module-source-descriptor descriptor
               :p15-s23-sh07-link-manifest link-manifest)
        authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         (:path source) (:text source) module (:table source))]
    {:module (assoc module :p15-s23-sh07-source-authority authority)
     :definition (get (:table source) function-name)}))

(defn- compile-authentic-function [{:keys [module definition]}]
  (invoke 'p15-s23-compile-function
          [@augmented-emitter module definition]))

(defn- internal-instructions [products]
  (vec
   (for [[_ definition] (get-in products [:value :functions])
         value (tree-seq coll? seq (:instructions definition))
         :when (and (map? value)
                    (= :builtin-call (:op value))
                    (contains? value :compiler-internal-provenance))]
     value)))

(defn- external-instructions [products]
  (vec
   (for [[_ definition] (get-in products [:value :functions])
         value (tree-seq coll? seq (:instructions definition))
         :when (and (map? value)
                    (= :function-call (:op value))
                    (contains? value :external-link))]
     value)))

(deftest canonical-reader-traversal-matches-independent-reader-carrier
  (doseq [value
          [{:z #{[:b 2] [:a 1]}
            :a {:nested [(list 'external 'x) {:k :v}]}}
           #{[:z {:b 2 :a 1}] [:a #{3 2 1}]}
           [(list 'quote {:b 2 :a 1}) {:right 2 :left 1}]]]
    (is (= (bootstrap/reader-canonical-value value)
           (invoke 'p15-s23-reader-canonical-value [value]))))
  (let [{:keys [descriptor manifest]} (fixture [(list 'external 'x)] [0])
        index-result
        (invoke 'p15-s23-build-external-edge-index [descriptor manifest])]
    (is (= :complete (:status index-result)))
    (is (= 1423 (count (:value index-result))))))

(deftest b51-module-assembly-does-not-invent-a-main-entrypoint
  (let [{:keys [module definition descriptor manifest]}
        (fixture [(list 'external 'x)] [0])
        module
        (assoc module
               :p15-s23-sh07-module-source-descriptor descriptor
               :p15-s23-sh07-link-manifest manifest)
        assembled
        (invoke 'p15-s23-assemble-plan-products
                [@emitter module [['caller definition]]])]
    (is (= :complete (:status assembled)))
    (is (nil? (get-in assembled [:value :entrypoint])))
    (is (= 0
           (get-in assembled
                   [:value :functions 'caller :instructions 0
                    :external-link :edge-ordinal])))))

(deftest canonical-path-and-external-link-are-gravity-owned
  (let [quoted (list 'quote (list 'external 'x))
        body-a [(array-map :z quoted :a (list 'external 'x))]
        body-b [(array-map :a (list 'external 'x) :z quoted)]
        expected-path [0 0 1]
        left (compile-fixture (fixture body-a expected-path))
        right (compile-fixture (fixture body-b expected-path))
        left-entry (first (:entries (only-instruction left)))
        right-entry (first (:entries (only-instruction right)))
        left-call (:value left-entry)
        right-call (:value right-entry)]
    (is (= :complete (:status left)))
    (is (= :complete (:status right)))
    (is (= left-entry right-entry))
    (is (= {:artifact :gravity/sh07-b51-external-call-v1
            :manifest-id digest-c
            :edge-ordinal 0}
           (:external-link left-call)))
    (is (= (:external-link left-call) (:external-link right-call)))
    (is (= :quote (:op (:value (second (:entries (only-instruction left)))))))
    (is (nil? (:external-link
               (:value (second (:entries (only-instruction left)))))))))

(deftest external-link-mutations-reject-before-an-instruction-exists
  (let [base (fixture [(list 'external 'x)] [0])
        rejects?
        (fn [candidate]
          (= :rejected (:status (compile-fixture candidate))))]
    (is (= :complete (:status (compile-fixture base))))
    (testing "path, order, duplicate, arity, owner, and closed keys"
      (is (rejects? (assoc-in base [:manifest :edges 0 :callsite-path] [1])))
      (is (rejects? (assoc-in base [:manifest :edges 0 :ordinal] 1)))
      (is (rejects? (assoc-in base [:manifest :edges 1]
                              (assoc (get-in base [:manifest :edges 0])
                                     :ordinal 1))))
      (is (rejects? (assoc-in base [:manifest :edges 0 :arity] 2)))
      (is (rejects? (assoc-in base [:manifest :edges 0 :owner-edge]
                              :root8-to-producer-interface)))
      (is (rejects?
           (-> base
               (assoc-in [:manifest :edges 0 :owner-edge]
                         :root8-to-producer-interface)
               (assoc-in [:descriptor :external-bindings 0 :owner-edge]
                         :root8-to-producer-interface))))
      (is (rejects? (assoc-in base [:manifest :edges 0 :extra] :forbidden)))
      (is (rejects? (update base :manifest dissoc :manifest-id)))
      (is (rejects? (update base :descriptor dissoc :external-bindings))))))

(deftest local-and-quoted-calls-never-gain-links
  (let [local-definition
        {:name 'caller :params ['x] :arity 1
         :body [(list 'local-fn 'x) (list 'quote (list 'external 'x))]}
        local-module
        {:module 'caller.module :profile :meta :target :jvm
         :effects #{} :capabilities #{} :safety :safe :exports []
         :function-table {'local-fn {:name 'local-fn :arity 1}}}
        result
        (invoke 'p15-s23-compile-function
                [@emitter local-module local-definition])
        instructions (:instructions (:value result))]
    (is (= :complete (:status result)))
    (is (= :function-call (:op (first instructions))))
    (is (not (contains? (first instructions) :external-link)))
    (is (= :quote (:op (second instructions))))
    (is (not (contains? (second instructions) :external-link)))))

(deftest authentic-seven-module-plans-carry-every-governed-link
  (let [products (mapv compile-authentic-module (range 7))
        internal (vec (mapcat internal-instructions products))
        external (vec (mapcat external-instructions products))]
    (is (every? #(= :complete (:status %)) products))
    (is (= [0 3 4 2 33 11 1]
           (mapv #(count (internal-instructions %)) products)))
    (is (= 54 (count internal)))
    (is (= {'sh07-canonical-text-compare 4
            'sh07-declared-digest-hash 48
            'sh07-observe-utf8-byte-count 2}
           (frequencies (map :function internal))))
    (is (= 2
           (count
            (filter #(= 'sh07-b51-producer-insert-decoration
                        (:compiler-internal-caller %))
                    internal))))
    (is (every?
         #(= #{:op :function :compiler-internal-caller
               :compiler-internal-provenance :args}
             (set (keys %)))
         internal))
    (is (every?
         #(= {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
              :callee (:function %)
              :function (:compiler-internal-caller %)}
             (select-keys (:compiler-internal-provenance %)
                          [:artifact :callee :function]))
         internal))
    (is (every?
         #(= #{:artifact :manifest-id :module-source-id :source-sha256
               :function :function-source-hash :callee}
             (set (keys (:compiler-internal-provenance %))))
         internal))
    (is (= 1423 (count external)))
    (is (= (range 1423)
           (sort (map #(get-in % [:external-link :edge-ordinal])
                      external))))))

(deftest compiler-internal-provenance-is-source-and-occurrence-bound
  (let [caller 'sh07-b51-producer-insert-decoration
        callee 'sh07-canonical-text-compare
        {:keys [module definition] :as context}
        (authentic-function-context 1 caller)
        paths [[0 2 3 1 5] [0 2 3 1 7 2]]
        provenance-module
        (assoc module
               :p15-s23-sh07-enclosing-function caller
               :p15-s23-sh07-enclosing-definition definition)
        provenance
        (fn [candidate path]
          (invoke 'p15-s23-internal-call-provenance
                  [candidate caller callee 2 path]))
        reject? #(= :rejected
                    (:status
                     (compile-authentic-function
                      (assoc context :module %))))
        matching-row?
        #(and (= caller (:caller %)) (= callee (:builtin %)))
        builtin-rows
        (get-in module
                [:p15-s23-sh07-module-source-descriptor :builtin-calls])
        one-matching-row-removed
        (loop [remaining builtin-rows removed? false result []]
          (if (empty? remaining)
            result
            (let [row (first remaining)]
              (if (and (not removed?) (matching-row? row))
                (recur (rest remaining) true result)
                (recur (rest remaining) removed? (conj result row))))))]
    (is (= :complete (:status (compile-authentic-function context))))
    (is (every? map? (map #(provenance provenance-module %) paths)))
    (is (nil? (provenance provenance-module [0 2 3 1 6])))
    (is (nil?
         (invoke 'p15-s23-internal-call-provenance
                 [provenance-module 'fabricated-caller callee 2
                  (first paths)])))
    (testing "M0, M1, source, function, and closed-row mutations reject"
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-source-authority :manifest-id]
                             digest-a)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-source-authority :module-source-id]
                             digest-a)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-source-authority :source-sha256]
                             digest-a)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-source-authority
                              :function-source-hashes caller]
                             digest-a)))
      (is (reject? (update module :p15-s23-sh07-source-authority
                           assoc :extra :forbidden)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-link-manifest :manifest-id]
                             digest-a)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-module-source-descriptor
                              :source-sha256]
                             digest-a)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-module-source-descriptor
                              :builtin-calls 0 :extra]
                             :forbidden)))
      (is (reject? (assoc-in module
                             [:p15-s23-sh07-module-source-descriptor
                              :builtin-calls]
                             one-matching-row-removed))))
    (testing "wrong internal arity rejects before any ordinary fallback"
      (let [changed? (atom false)
            malformed
            (walk/postwalk
             (fn [value]
               (if (and (not @changed?)
                        (seq? value)
                        (= callee (first value)))
                 (do (reset! changed? true) (list callee (second value)))
                 value))
             definition)
            result
            (compile-authentic-function
             (assoc context :definition malformed))]
        (is @changed?)
        (is (= :rejected (:status result)))
        (is (= "L2-BUILTIN-ARITY" (:diagnostic result)))))))
