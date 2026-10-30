(ns gravity.self-hosting.list-construction-contract-test
  "Reader-only schema checks, not Effects/emitter execution or admission.

  C7, Effects and the emitter independently reconstruct the complete descriptor.
  C7/C8, D1 and D9 require preserved semantic facts and honest evidence scope."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]])
  (:import [java.io PushbackReader]))

(def ^:private owners
  [{:path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
    :prefix "sh08-lc-"}
   {:path "bootstrap/gravity/src/gravity/effects.gravity"
    :prefix "body-lc-"}
   {:path "bootstrap/gravity/p15_s23/emitter.gravity"
    :prefix "p15-s23-lc-"}])

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (PushbackReader. (io/reader path))]
      (loop [forms []]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form) forms (recur (conj forms form))))))))

(defn- exactly-one [values context]
  (when-not (= 1 (count values))
    (throw (ex-info "Expected one source contract" {:context context
                                                    :count (count values)})))
  (first values))

(defn- definition [forms name]
  (exactly-one (filter #(and (seq? %) (= 'defn (first %))
                            (= name (second %))) forms)
               name))

(defn- descriptor [form]
  (exactly-one
   (filter #(and (map? %)
                 (= :gravity.type/applied-list-construction (:kind %)))
           (tree-seq coll? seq form))
   :list-construction-descriptor))

(defn- contract [{:keys [path prefix]}]
  (let [forms (source-forms path)
        named #(definition forms (symbol (str prefix %)))]
    {:descriptor (descriptor (named "source"))
     ;; Exclude only each function's defn/name, not parameters or body fields.
     :finish (nnext (named "finish"))
     :constraint (nnext (named "constraint"))
     :call-fact (nnext (named "call-fact"))}))

(def ^:private contracts (delay (mapv contract owners)))

(def ^:private descriptor-keys
  #{:kind :version :operation :semantic-owner :intrinsic :resolution
    :operator-node-id :operator-binding-id :call-core-node-id :source
    :operator-source :source-call :source-edge :source-binding :argument-types
    :construction-policy :value-origin :application :latent-effects
    :direct-effects :capabilities :thrown-error-effects :profile :target
    :invalidation})

(defn- same-contracts? [values]
  (and (= 3 (count values)) (apply = values)))

(deftest reconstructed-list-contracts-have-complete-source-parity
  (doseq [[owner value] (map vector owners @contracts)]
    (testing (:path owner)
      (is (= descriptor-keys (set (keys (:descriptor value)))))))
  (is (same-contracts? @contracts)
      "Compare complete maps, finishing expressions, constraints and call facts."))

(deftest complete-source-comparison-detects-semantic-contract-drift
  (let [baseline (first @contracts)]
    (is (same-contracts? [baseline baseline baseline]))
    (doseq [owner-index (range 3)
            key descriptor-keys]
      (testing (str "Owner " owner-index ", descriptor field " key)
        (doseq [changed [(update baseline :descriptor dissoc key)
                         (assoc-in baseline [:descriptor key] ::changed)]]
          (is (not (same-contracts?
                    (assoc [baseline baseline baseline] owner-index changed)))))))
    (doseq [extra [:operand-type-work ::unknown-field]]
      (is (not (same-contracts?
                [baseline (assoc-in baseline [:descriptor extra] 0) baseline]))))
    (doseq [component [:finish :constraint :call-fact]]
      (is (not (same-contracts?
                [baseline baseline (assoc baseline component ::changed)]))))
    (testing "Parameter, return and result type finishing remains exact"
      (is (= '([header]
               (let [type (if (= :normal (get header :application))
                            {:kind :gravity.type/list
                             :element-types (get header :argument-types)
                             :shape :literal}
                            :gravity.type/never)]
                 (assoc header :parameter-types (get header :argument-types)
                        :return type :result-type type)))
             (:finish baseline))))))

(deftest consumers-keep-full-descriptor-equality
  (let [[c7 effects emitter] (mapv (comp source-forms :path) owners)]
    (doseq [[forms name expected]
            [[c7 'sh08-lc-inferred-call-valid?
              '(= (get types (get call :operator-node-id))
                  (sh08-lc-finish header))]
             [effects 'body-lc-call-valid?
              '(= descriptor (body-lc-finish header))]
             [emitter 'p15-s23-typed-compile-list-construction
              '(= descriptor
                  (get (get context :types) (get call :operator-node-id)))]]]
      (is (some #{expected} (tree-seq coll? seq (definition forms name)))
          (str name " must compare the full reconstructed descriptor")))))

(deftest source-contract-selection-rejects-missing-or-duplicate-definitions
  (let [form '(defn example [x] x)]
    (is (= form (definition [form] 'example)))
    (is (thrown? clojure.lang.ExceptionInfo (definition [] 'example)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (definition [form form] 'example)))))
