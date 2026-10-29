(ns c11-relative-policy-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest]
            [gravity.c6-c10.source-contract :as c6-contract]
            [gravity.c11-mir.source-contract :as c11-contract]))

(def root "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/")
(defn forms [file]
  (with-open [reader (java.io.PushbackReader. (io/reader (str root file)))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) out (recur (conj out form))))))))
(defn definitions [form]
  ;; Carrier definitions are inside the existing lexical authority wrapper.
  ;; The selected carrier functions do not capture that wrapper's authority.
  (if (and (seq? form) (= 'let (first form)))
    (mapcat definitions (drop 2 form)) [form]))
(def owner (create-ns (gensym "c11-relative-policy-owner-")))
(defn evaluate [file predicate]
  (doseq [form (mapcat definitions (forms file)) :when (predicate form)]
    (binding [*ns* owner] (eval form))))
(defn named? [names]
  #(and (seq? %) (contains? names (second %))))
(defn definition? [form]
  (and (seq? form) (contains? '#{def defn defn- declare require} (first form))))
(binding [*ns* owner]
  (clojure.core/refer 'clojure.core)
  (alias 'str (ns-name (:ns (meta #'str/blank?))))
  (alias 'digest (ns-name (:ns (meta #'digest/sha256-output-hex))))
  (alias 'c6-source-contract 'gravity.c6-c10.source-contract)
  (alias 'c11-source-contract (ns-name (:ns (meta #'c11-contract/v2-source-contract))))
  ;; Only diagnostic transport/path selection is synthetic. Carrier traversal,
  ;; canonical encoding, hashing, fixed contracts and policy checks are actual.
  (intern owner 'p15-s23-c6c10-host-fail!
          (fn [rule _ fact _] (throw (ex-info "Synthetic diagnostic transport"
                                                            {:id rule :missing-fact fact}))))
  (intern owner 'p15-s23-c11-mir-fail!
          (fn [rule _ _ extra] (throw (ex-info "Synthetic diagnostic transport"
                                                         (assoc extra :id rule)))))
  (intern owner 'p15-s23-c11-ingress-source-path (constantly "<synthetic-policy-context>")))

(evaluate "00588-p15-s23-stage2-native-carrier-header.clj"
          (named? '#{p15-s23-stage2-native-hashset-field p15-s23-stage2-native-hashset-field-once
                     p15-s23-stage2-native-arraymap-field p15-s23-stage2-native-arraymap-field-once}))
(evaluate "00700-static-rebuild-token-009-p15-s23-stage2-closed-checked-core-verify-through-p15-s23-trusted-carrier-vector-classes.clj"
          (named? '#{p15-s23-trusted-carrier-map-classes p15-s23-trusted-carrier-vector-classes}))
(evaluate "00700-static-rebuild-token-010-p15-s23-trusted-carrier-set-classes-through-p15-s23-trusted-carrier-validation.clj" definition?)
(evaluate "00701-p15-s23-c6c10-source-relative-path.clj" #(and (seq? %) (= 'def (first %))))
(evaluate "00702-p15-s23-c6c10-host-fail.clj"
          #(and (definition? %) (not= 'p15-s23-c6c10-host-fail! (second %))))
(evaluate "00703-p15-s23-c6c10-canonical-form.clj" definition?)
(evaluate "00726-p15-s23-c6c10-gravity-candidate-verification.clj" #(and (seq? %) (= 'def (first %))))
(evaluate "00727-p15-s23-c11-mir-required-functions.clj" #(and (seq? %) (= 'def (first %))))
(evaluate "00731-p15-s23-c11-mir-diagnostic-message.clj"
          (named? '#{p15-s23-c11-mir-require-trusted-carrier!}))
(evaluate "00732-p15-s23-c11-mir-owned-upstream-diagnostic.clj"
          (named? '#{p15-s23-c11-mir-contract}))
(evaluate "00733-p15-s23-c11-mir-source-binding.clj" (named? '#{p15-s23-c11-mir-require!}))
(let [selected (drop-while #(not= 'p15-s23-c11-core-policy-keys (second %))
                           (forms "00740-p15-s23-c11-mir-validate-effect-edge-placement.clj"))]
  (binding [*ns* owner]
    (eval '(declare p15-s23-c11-mir-expected-ingress-semantic))
    (doseq [form selected] (eval form))))
(defn call [name & args] (apply (ns-resolve owner name) args))
(def core {:artifact-id "synthetic-core-id"})
(def context {:kind :synthetic-context :source-path "fixture.gravity"})
(def content-id (str "sha256:" (apply str (repeat 64 "a"))))
(defn fixture [version]
  (let [family :closed-flat-bool-literal-sequence
        receipt (cond->
                 {:source-interface {:interface :gravity/c6-c10 :version version}
                  :admission-status :development-unadmitted
                  :verification-scope :selected-current-source-host-seed
                  :reader-plan-binding {:instruction-summary-content-id content-id}
                  :reader-source-revision
                  {:source-content-hash (get-in c6-contract/current-reader-program [:binding :source-content-hash])
                   :function-shapes-content-id content-id}
                  :reader-result-identity
                  {:actual-sh03-reader-result-id "synthetic-sh03"
                   :actual-sh03-semantic-product-binding {:synthetic true}
                   :actual-c2-artifact-id "synthetic-c2"
                   :actual-reader-product-integrity-hash "synthetic-integrity"
                   :actual-reader-source-id "synthetic-reader"}
                  :authenticated-envelope
                  {:status :accepted :stage :c2-reader
                   :source-rule {:admission-status :development-unadmitted
                                 :function-shapes-content-id content-id
                                 :owner-source-rule-content-id content-id}
                   :gravity-template-replay {:checks-content-id content-id
                                            :diagnostics-content-id content-id}}
                  :envelope-verification :passed :self-hosted? false
                  :expression-binding
                  (merge (select-keys c6-contract/current-expression-program
                                      [:source-content-hash :semantic-hash :functions-semantic-hash
                                       :artifact-hash :function-count])
                         {:source-contract-version 2 :functions-content-id content-id
                          :declared-plan-compiler-labels-content-id content-id
                          :owner-binding-content-id content-id})
                  :owner-metadata-content-id content-id
                  :projection-contract @(ns-resolve owner 'p15-s23-c11-core-projection-contract)}
                  (= 3 version) (assoc :semantic-family family))
        program (assoc (select-keys (call 'p15-s23-c6c10-source-program-contract version)
                                   (cond-> [:source-byte-count :source-content-hash :plan-semantic-hash
                                            :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash]
                                     (= 3 version) (conj :semantic-family)))
                       :source-contract-version version)]
    (cond-> {:source-interface {:interface :gravity/c6-c10 :version version}
             :admission-status :development-unadmitted
             :verification-scope :selected-current-source-host-seed
             :source-policy-receipt receipt :compiler-program program
             :source-policy-binding-id
             (call 'p15-s23-c6c10-canonical-digest
                   {:domain (if (= 3 version) :gravity/c6-c10-source-policy-v3 :gravity/c6-c10-source-policy-v2)
                    :artifact-id (:artifact-id core) :context context :policy receipt})}
      (= 3 version) (assoc :semantic-family family))))
(defn validate [policy version]
  (call 'p15-s23-c11-mir-require-core-policy-data! core context policy version))
(defn diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest relative-v2-and-v3-coherence-and-distinct-bindings
  (doseq [version [2 3] :let [policy (fixture version)
                             binding (call 'p15-s23-c11-mir-core-policy-binding version policy)]]
    (is (identical? policy (validate policy version)))
    (is (= (:source-policy-binding-id policy) (:source-policy-binding-id binding)))
    (is (= (call 'p15-s23-c6c10-canonical-digest
                 {:domain (if (= 3 version) :gravity/c11-checked-core-policy-v3 :gravity/c11-checked-core-policy-v2)
                  :policy policy}) (:source-policy-hash binding)))
    (is (not= (:source-policy-binding-id binding) (:source-policy-hash binding)))
    (is (= binding (call 'p15-s23-c11-mir-core-policy-binding version
                        (into {} (reverse (seq policy))))))
    (is (= "C11-MODULE" (:id (diagnostic #(call 'p15-s23-c11-mir-require-core-policy-data!
                                                        (assoc core :artifact-id "changed") context policy version)))))
    (is (= "C11-MODULE" (:id (diagnostic #(call 'p15-s23-c11-mir-require-core-policy-data!
                                                        core (assoc context :source-path "changed") policy version)))))))

(deftest altered-policy-facts-are-rejected
  (doseq [version [2 3]
          mutate [#(assoc % :extra true)
                  #(dissoc % :admission-status)
                  #(assoc % :source-policy-binding-id content-id)
                  #(assoc-in % [:compiler-program :source-content-hash] content-id)
                  #(assoc-in % [:source-interface :version] (int version))
                  #(assoc-in % [:source-policy-receipt :self-hosted?] true)
                  #(assoc-in % [:source-policy-receipt :envelope-verification] :unchecked)
                  #(assoc-in % [:source-policy-receipt :owner-metadata-content-id] "invalid")
                  #(assoc-in % [:source-policy-receipt :projection-contract :schema-version] 1N)
                  #(assoc-in % [:source-policy-receipt :reader-source-revision :source-content-hash] content-id)
                  #(assoc-in % [:source-policy-receipt :expression-binding :source-contract-version] 3)]]
    (is (= {:id "C11-MODULE" :missing-fact :current-core-source-policy-data}
           (diagnostic #(validate (mutate (fixture version)) version)))))
  (is (= "C11-MODULE" (:id (diagnostic #(validate (assoc (fixture 3) :semantic-family :other) 3)))))
  (is (= "C11-MODULE" (:id (diagnostic #(validate (fixture 2) 3)))))
  (is (= "C11-VERIFY" (:id (diagnostic #(validate (fixture 2) 4))))))

(deftest actual-native-carrier-rejects-before-foreign-callbacks
  (let [calls (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! calls inc) {})
                  (hashCode [] (swap! calls inc) 0)
                  (equals [_] (swap! calls inc) true)
                  (toString [] (swap! calls inc) "foreign"))
        comparator (reify java.util.Comparator
                     (compare [_ _ _] (swap! calls inc) 0))
        policy (fixture 3)]
    (doseq [bad [foreign (with-meta policy {:caller true})
                 (assoc policy :foreign foreign)
                 (assoc policy :foreign (sorted-map-by comparator))]]
      (is (= :trusted-comparator-free-c11-carrier
             (:missing-fact (diagnostic #(validate bad 3)))))
      (is (= :trusted-comparator-free-c11-carrier
             (:missing-fact (diagnostic #(call 'p15-s23-c11-mir-core-policy-binding 3 bad))))))
    (is (zero? @calls))))

(deftest projection-is-relative-data-without-report-authentication
  (doseq [version [2 3] :let [policy (fixture version)
                             report (-> policy
                                        (dissoc :compiler-program)
                                        (assoc :source-binding (:compiler-program policy)
                                               :ignored-report-field :synthetic))]]
    ;; A synthetic report can project coherent DATA. This deliberately proves
    ;; that this helper does not confer fresh authentication or report authority.
    (is (= policy (call 'p15-s23-c11-mir-core-policy-projection core context report version)))
    (is (= "C11-MODULE"
           (:id (diagnostic #(call 'p15-s23-c11-mir-core-policy-projection
                                  core context (assoc report :source-policy-binding-id "changed") version)))))
    (is (= :trusted-comparator-free-c11-carrier
           (:missing-fact
            (diagnostic #(call 'p15-s23-c11-mir-core-policy-projection
                               core context (with-meta report {:caller true}) version)))))))

(let [result (run-tests 'c11-relative-policy-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :relative-policy-data-only :diagnostic-transport :synthetic
            :fixture-authority :synthetic :carrier-and-digest :actual
            :bootstrap-loaded false :source-authentication false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
