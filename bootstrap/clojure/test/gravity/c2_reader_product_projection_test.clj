(ns gravity.c2-reader-product-projection-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-reader-product-projection :as projection]))

(def diagnostic-ids ["C2-ABBREV" "C2-EXTENSION"])
(def standard-policy
  {:policy :gravity/standard-reader
   :version 1
   :registered-tags ['inst 'uuid]
   :ambient-authority :denied})

(defn- contract []
  (var-get (ns-resolve 'gravity.c2-reader-product-projection
                       'namespace-contract)))

(defn- exception-data [f]
  (try (f) nil
       (catch clojure.lang.ExceptionInfo exception (ex-data exception))))

(defn- with-required-operations [operations thunk]
  (projection/with-operations
   (merge {:syntax-object-stream (fn [& _] nil)
           :c2-literal-records (fn [_] [])
           :c2-reader-diagnostic-ids diagnostic-ids
           :standard-reader-policy standard-policy}
          operations)
   thunk))

(deftest contract-is-hosted-projection-only
  (let [value (contract)]
    (is (= :hosted-c2-reader-product-compatibility-projection
           (:contract-boundary value)))
    (is (= ['clojure.core] (get-in value [:dependency-direction :requires])))
    (is (true? (:bootstrap-hosted? value)))
    (is (true? (:hosted-compatibility-projection? value)))
    (doseq [claim [:canonical-c2-authority? :sh03-product-authority?
                   :source-reading? :filesystem-access?
                   :cache-reuse-authority? :proof-authority?
                   :self-hosted? :release-authority?]]
      (is (false? (claim value)) claim))))

(deftest public-api-and-operation-boundary-are-exact
  (let [expected {'with-operations '([operations thunk])
                  'call-entrypoint-body '([operation-key operation args])
                  'c2-syntax-seed-stream '([source-path products module-context])
                  'c2-deferred-semantic-literals '([form-tree])
                  'c2-top-level-products '([artifact])
                  'c2-reader-capability-proof '([artifact])
                  'c2-reader-overrides-from-forms '([forms])
                  'c2-reader-extension-invocations '([form-tree])}]
    (is (= expected
           (into {} (map (fn [[name spec]] [name (:arglists spec)]))
                 (:public-api (contract)))))
    (doseq [[name arglists] expected]
      (is (= arglists (:arglists (meta (ns-resolve
                                        'gravity.c2-reader-product-projection
                                        name)))) name)))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest operation-validation-fails-closed
  (is (some? (exception-data #(projection/with-operations [] identity))))
  (is (= [:unknown]
         (:unknown-keys
          (exception-data #(projection/with-operations {:unknown identity}
                                                       identity)))))
  (doseq [operations [{:syntax-object-stream :not-a-function}
                      {:c2-reader-diagnostic-ids []}
                      {:standard-reader-policy (assoc standard-policy
                                                      :ambient-authority
                                                      :granted)}]]
    (is (some? (exception-data #(projection/with-operations operations
                                                           identity)))))
  (is (some? (exception-data #(projection/with-operations {} :not-a-function))))
  (is (= :missing
         (:operation
          (exception-data #(projection/call-entrypoint-body
                            :missing identity []))))))

(deftest synthetic-reader-product-projections-are-exact
  (let [captured (atom nil)
        span {:byte-start 0 :byte-end 3}
        products {:form-tree [{:form-id :f0 :kind :abbreviation :raw "'x"
                               :value '(quote x) :span span :abbrev :quote
                               :metadata {:m true}
                               :generated-origin [{:from :f0 :reason :quote}]}]
                  :parsed-records [{:form-id :f0 :form '(quote x)}]}
        seed (with-required-operations
               {:syntax-object-stream
                (fn [& args] (reset! captured args) :seed-stream)}
               #(projection/c2-syntax-seed-stream "src.g" products {:module 'm}))
        [path records context] @captured
        form-tree [{:form-id :n :kind :integer :raw "12" :span span
                    :value {:semantic-validation :deferred
                            :artifact :gravity/deferred-numeric-literal}}
                   {:form-id :s :kind :symbol :raw "x" :span span :value 'x}]
        artifact {:form-tree [{:form-id :f :open-token :t}]
                  :token-stream [{:token-id :t :raw "x"}]
                  :top-level-form-ids [:f]}]
    (is (= :seed-stream seed))
    (is (= ["src.g" {:module 'm}] [path context]))
    (is (= [{:form '(quote x) :form-id :f0
             :span (assoc span :form-index 0) :metadata {:m true}
             :reader-origin {:kind :source :raw-form-kind :abbreviation
                             :raw-excerpt "'x" :abbreviation :quote}
             :generated-origin [{:from :f0 :reader-abbreviation :quote
                                 :expanded-form '(quote x)}]}]
           records))
    (is (= [(select-keys (first form-tree) [:form-id :kind :raw :value :span])]
           (projection/c2-deferred-semantic-literals form-tree)))
    (is (= [{:form-record {:form-id :f :open-token :t}
             :token-record {:token-id :t :raw "x"}}]
           (projection/c2-top-level-products artifact)))))

(deftest proof-overrides-and-extension-projections-are-exact
  (let [span {:byte-start 0 :byte-end 2}
        lexical (zipmap [:ordered-token-ids-unique?
                         :token-raw-slices-exact?
                         :token-provenance-complete?
                         :no-token-contains-top-level-form?
                         :form-ids-unique? :graph-valid?
                         :root-form-ids-resolve? :form-raw-slices-exact?
                         :form-links-resolve? :parent-spans-enclose-children?
                         :collection-delimiters-resolve?]
                        (repeat true))
        literal {:literal-id :l :form-id :f :kind :integer :raw "1"
                 :span span :decoded 1 :facts {}}
        artifact {:source-unit-record {:source-id "sha256:source"
                                       :reader-options {:retain-comments true}}
                  :token-stream [{:token-id :t :span span :trivia? true}]
                  :form-tree [{:form-id :f :kind :abbreviation :span span
                               :generated-origin [{:from :f}]}]
                  :literal-decoding-records [literal]
                  :trivia-retention-records [{:trivia-id :t}]
                  :reader-extension-policy {:status :registered}
                  :incremental-reader-hashes
                  (merge {:status :stable}
                         (zipmap [:source-unit :token-stream :form-tree
                                  :syntax-seed-stream :extension-invocation-set
                                  :reader-diagnostics]
                                 (repeat "sha256:x")))
                  :rejected-design-coverage
                  (mapv #(hash-map :diagnostic %) diagnostic-ids)
                  :semantic-error-deferment-record {:deferred? true}
                  :lexical-product-validation lexical}
        proof (with-required-operations
                {:c2-literal-records (fn [_] [literal])}
                #(projection/c2-reader-capability-proof artifact))
        forms [(list 'ns 'demo
                     (list :metadata {:compiler {:c2-reader {:mode :strict}}}))]
        tagged [{:form-id :tagged :tag 'uuid :span span :raw "#uuid x"}]
        extensions (with-required-operations
                     {} #(projection/c2-reader-extension-invocations tagged))]
    (is (every? true? (vals (dissoc proof :representation-status :status))))
    (is (= :partial (:status proof)))
    (is (= {:mode :strict}
           (projection/c2-reader-overrides-from-forms forms)))
    (is (= ['inst 'uuid] (mapv :tag extensions)))
    (is (= [:registered-not-invoked :invoked] (mapv :status extensions)))
    (is (= [{:form-id :tagged :span span :raw "#uuid x"}]
           (:invocations (second extensions))))))

(deftest moved-entrypoints-preserve-interposition-and-captured-originals
  (is (= :interposed
         (projection/with-operations
          {:c2-top-level-products (fn [_] :interposed)}
          #(projection/c2-top-level-products {}))))
  (let [original projection/c2-deferred-semantic-literals
        calls (atom 0)
        input [{:form-id :n :kind :integer :raw "1" :span {}
                :value {:semantic-validation :deferred
                        :artifact :gravity/deferred-numeric-literal}}]
        result (projection/with-operations
                {:c2-deferred-semantic-literals
                 (fn [& args]
                   (swap! calls inc)
                   (apply original args))}
                #(projection/c2-deferred-semantic-literals input))]
    (is (= 1 @calls))
    (is (= [:n] (mapv :form-id result)))))
