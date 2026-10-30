(ns gravity.self-hosting.list-query-contract-test
  "Reader-only reconstruction parity; no Gravity consumer is executed.

  Explicit helper-name correspondence is not evidence of helper equivalence.
  D1/C7/C8/D9 still require runtime composition before integration acceptance."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk])
  (:import [java.io PushbackReader]))

(def ^:private owners
  [{:path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
    :prefix "sh08-lq-" :aliases {}}
   {:path "bootstrap/gravity/src/gravity/effects.gravity" :prefix "body-lq-"
    :aliases '{body-io-child-types sh08-ft-ordered-child-types
               body-all? sh08-ft-all-true?
               body-lc-native-map? sh08-lc-native-map?
               body-lq-binding? sh08-lq-binding?
               body-hl-type? sh08-hl-type?
               body-mkeys-constraint sh08-mkeys-constraint
               body-vrest-constraint sh08-vrest-constraint
               body-vemp-constraint sh08-vemp-constraint
               body-mkeys-call-fact sh08-mkeys-call-fact
               body-vrest-call-fact sh08-vrest-call-fact
               body-vemp-call-fact sh08-vemp-call-fact}}
   {:path "bootstrap/gravity/p15_s23/emitter.gravity" :prefix "p15-s23-lq-"
    :aliases '{p15-s23-io-child-types sh08-ft-ordered-child-types
               p15-s23-typed-all sh08-ft-all-true?
               p15-s23-lc-native-map? sh08-lc-native-map?
               p15-s23-lq-binding? sh08-lq-binding?
               p15-s23-hl-type? sh08-hl-type?
               p15-s23-mkeys-constraint sh08-mkeys-constraint
               p15-s23-vrest-constraint sh08-vrest-constraint
               p15-s23-vemp-constraint sh08-vemp-constraint
               p15-s23-mkeys-call-fact sh08-mkeys-call-fact
               p15-s23-vrest-call-fact sh08-vrest-call-fact
               p15-s23-vemp-call-fact sh08-vemp-call-fact}}])

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (PushbackReader. (io/reader path))]
      (loop [forms []]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form) forms (recur (conj forms form))))))))

(defn- exactly-one [values context]
  (when-not (= 1 (count values))
    (throw (ex-info "Expected one source contract" {:context context})))
  (first values))

(defn- definition [forms name]
  (exactly-one (filter #(and (seq? %) (= 'defn (first %))
                            (= name (second %))) forms) name))

(defn- header [source]
  (exactly-one (filter #(and (map? %)
                            (= :gravity.type/applied-list-query (:kind %)))
                      (tree-seq coll? seq source)) :list-query-header))

(defn- normalize [aliases form]
  ;; Never normalize prefixes, keywords, field values or unknown helper names.
  (walk/postwalk #(if (symbol? %) (get aliases % %) %) form))

(defn- routing-and-leaf [body]
  ;; This existing outer branch delegates the map-key collection family. Keep
  ;; its exact predicate independently visible; complete wrapper parity and
  ;; private-map-key routing are checked separately from scoped leaf parity.
  (when-not (and (= 2 (count body)) (vector? (first body)))
    (throw (ex-info "Expected one function body expression" {})))
  (loop [expression (second body) remaining 1 routing []]
    (if (zero? remaining)
      {:routing routing :leaf [(first body) expression]}
      (do
        (when-not (and (seq? expression) (= 'if (first expression))
                       (= 4 (count expression)))
          (throw (ex-info "Changed collection-family dispatch" {})))
        (recur (nth expression 3) (dec remaining)
               (conj routing [(second expression) (nth expression 2)]))))))

(defn- contract [{:keys [path prefix aliases]}]
  (let [forms (source-forms path)]
    (into {}
          (for [part [:source :finish :constraint :call-fact]]
            (let [body (normalize aliases
                                  (nnext (definition forms
                                                     (symbol (str prefix (name part))))))]
              [part (if (#{:constraint :call-fact} part)
                      (:leaf (routing-and-leaf body)) body)])))))

(def ^:private contracts (delay (mapv contract owners)))

(def ^:private header-keys
  #{:kind :version :operation :semantic-owner :intrinsic :resolution
    :operator-node-id :operator-binding-id :call-core-node-id :source
    :operator-source :source-call :source-edge :source-binding :argument-types
    :query-policy :storage-policy :value-origin :application :latent-effects
    :direct-effects :capabilities :thrown-error-effects :profile :target
    :invalidation})

(defn- same-contracts? [values]
  (and (= 3 (count values)) (apply = values)))

(defn- change-header [value f]
  (let [original (header (:source value))]
    (update value :source
            #(walk/postwalk (fn [form] (if (= original form) (f form) form)) %))))

(deftest complete-list-query-reconstruction-expressions-agree
  (doseq [value @contracts]
    (is (= header-keys (set (keys (header (:source value)))))))
  (is (same-contracts? @contracts)
      "Includes full source/finish and complete non-map-key constraint/fact tails."))

(deftest source-comparison-detects-header-proof-and-result-drift
  (let [baseline (first @contracts)]
    (is (same-contracts? [baseline baseline baseline]))
    (doseq [owner-index (range 3)
            key header-keys
            change [#(dissoc % key) #(assoc % key ::changed)]]
      (is (not (same-contracts?
                (assoc [baseline baseline baseline] owner-index
                       (change-header baseline change))))))
    (doseq [extra [:operand-type-work ::unknown-field]]
      (is (not (same-contracts?
                [baseline (change-header baseline #(assoc % extra 0)) baseline]))))
    (doseq [[part keys] [[:source [:bounds-proof :head-proof :selected-index
                                 :selected-type :runtime-guards :check-elision
                                 :nonempty-evidence :control-flow :mutations
                                 :branch-facts]]
                       [:finish [:parameter-types :return :result-type :head-proof]]
                       [:constraint [:bounds-proof :dependencies]]
                       [:call-fact [:result-type :applied-vector-first]]]
            key keys]
      (is (some #{key} (tree-seq coll? seq (get baseline part))))
      (let [changed (update baseline part #(walk/postwalk-replace {key ::changed} %))]
        (is (not (same-contracts? [baseline baseline changed]))
            (str part " retains " key))))))

(deftest map-key-routing-and-complete-wrappers-agree
  (doseq [{:keys [path prefix aliases]} owners
          part [:constraint :call-fact]]
    (let [body (normalize aliases
                          (nnext (definition (source-forms path)
                                             (symbol (str prefix (name part))))))
          args (if (= :constraint part) '[descriptor] '[descriptor module])
          first-predicate
          '(if (keyword? (get descriptor :kind))
             (contains? #{:gravity.type/applied-map-keys
                          :gravity.type/applied-private-map-keys}
                        (get descriptor :kind)) false)
          expected
          [[first-predicate (apply list (symbol (str "sh08-mkeys-" (name part))) args)]]]
      (is (= args (first body)))
      (is (= expected (:routing (routing-and-leaf body)))
          "Every owner preserves the repaired public/private Map-key routing.")
      (doseq [[position changed] [[1 false] [2 '(unrecognized-delegate descriptor)]]]
        (let [mutated (list (first body)
                            (apply list (assoc (vec (second body)) position changed)))]
          (is (not (= expected (:routing (routing-and-leaf mutated))))
              "Separating the outer branch cannot hide predicate or delegate drift.")))))
  (doseq [part [:constraint :call-fact]]
    (is (apply =
               (for [{:keys [path prefix aliases]} owners]
                 (normalize aliases
                            (nnext (definition (source-forms path)
                                               (symbol (str prefix (name part))))))))
        "Complete wrappers agree independently of scoped leaf parity.")))

(deftest routing-extraction-rejects-missing-extra-or-malformed-expressions
  (let [body '([descriptor] (if condition (delegate descriptor) result))]
    (is (= {:routing [['condition '(delegate descriptor)]]
            :leaf ['[descriptor] 'result]}
           (routing-and-leaf body)))
    (doseq [invalid ['([descriptor])
                     '([descriptor] (if condition (delegate descriptor) result) extra)
                     '([descriptor] (if condition (delegate descriptor)))
                     '([descriptor] (when condition (delegate descriptor) result))
                     '(descriptor (if condition (delegate descriptor) result))]]
      (is (thrown? clojure.lang.ExceptionInfo (routing-and-leaf invalid))))))

(deftest explicit-helper-normalization-does-not-hide-other-changes
  (doseq [{:keys [aliases]} owners]
    (is (every? symbol? (concat (keys aliases) (vals aliases))))
    (is (= [:bounds-proof :head-proof :runtime-guards :retained
            'body-unrecognized-helper? 'p15-s23-unrecognized-helper?]
           (normalize aliases
                      [:bounds-proof :head-proof :runtime-guards :retained
                       'body-unrecognized-helper? 'p15-s23-unrecognized-helper?]))))
  (let [baseline (first @contracts)
        changed (update baseline :source
                        #(walk/postwalk-replace
                          {'sh08-hl-type? 'unrecognized-homogeneous-helper?} %))]
    (is (not (same-contracts? [baseline changed baseline])))))

(deftest genuine-query-guards-and-proof-outcomes-remain-in-source
  (doseq [value @contracts]
    (let [source (:source value) nodes (tree-seq coll? seq source)]
      (doseq [required ['(= (count args) 1)
                        '(> (count (get input :element-types)) 0)
                        '(map? (get validation :head-proof))
                        '(= :accepted (get validation :status))]]
        (is (some #{required} nodes)))
      (let [proof (exactly-one
                   (filter #(and (map? %)
                                 (= :gravity.type/vector-first-nonempty (:kind %)))
                           nodes) :vector-first-proof)]
        (is (= :proven-safe (:outcome proof)))
        (is (= :not-authorized (:check-elision proof)))
        (is (= :retained (:runtime-guards proof)))))))

(deftest consumers-retain-full-query-descriptor-equality
  (doseq [[owner name expected]
          [[(first owners) 'sh08-lq-inferred-call-valid?
            '(= (get types (get call :operator-node-id)) (sh08-lq-finish header))]
           [(second owners) 'body-lq-call-valid?
            '(= descriptor (body-lq-finish header))]
           [(nth owners 2) 'p15-s23-typed-compile-list-query
            '(= descriptor (get (get context :types) (get call :operator-node-id)))]]]
    (is (some #{expected}
              (tree-seq coll? seq (definition (source-forms (:path owner)) name)))
        (str name " retains full equality, not a selected-key comparison"))))

(deftest native-map-guard-bodies-and-private-routing-remain-explicit
  (let [names '[sh08-lc-native-map? body-lc-native-map? p15-s23-lc-native-map?]
        bodies (mapv (fn [owner name] (nnext (definition (source-forms (:path owner)) name))) owners names)]
    (is (= 3 (count bodies)))
    (is (apply = bodies))
    (doseq [body bodies]
      (is (some #{'bootstrap-native-carrier-header} (tree-seq coll? seq body)))
      (is (not= (first bodies) (walk/postwalk-replace {'bootstrap-native-carrier-header 'identity} body)))))
  (doseq [{:keys [path prefix aliases]} owners
          part [:constraint :call-fact]]
    (let [body (normalize aliases (nnext (definition (source-forms path) (symbol (str prefix (name part))))))
          changed (walk/postwalk-replace {:gravity.type/applied-private-map-keys :foreign-family} body)]
      (is (not= (:routing (routing-and-leaf body)) (:routing (routing-and-leaf changed)))))))
