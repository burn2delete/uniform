(ns c6-reader-policy-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gravity.digest :as digest]
            [gravity.c6-c10.source-contract :as contract]
            [gravity.c6-c10.source-interface :as c6-interface]
            [gravity.source-interface-header :as header]))

;; Reuse the existing relative-policy owner, but evaluate the current split
;; owner files.  The production helpers moved from PR597's old 00723 location
;; into 00720b/c/d on main; this test must not resurrect the old component.
(binding [*ns* *ns*] (load-file "tools/c11_relative_policy_test.clj"))
(def controls (find-ns 'c11-relative-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn evaluate [file predicate] ((ns-resolve controls 'evaluate) file predicate))
(defn named? [names] ((ns-resolve controls 'named?) names))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] ((ns-resolve controls 'diagnostic) f))
(defn redefine [functions f]
  (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) functions)) f))

(binding [*ns* owner]
  (alias 'source-interface-header (ns-name (:ns (meta #'header/pairs!))))
  (intern owner 'sha256-hex digest/sha256-hex)
  (eval '(declare p15-s23-c6c10-authenticated-source-binding-inputs!
                  p15-s23-stage2-plan-emitter-rule-record
                  p15-s23-stage2-sh02-descriptor-envelope-verify!
                  p15-s23-c11-mir-digest)))

(evaluate "../backends/00640-c-backend-fail.clj"
          (named? '#{c-backend-canonical-value}))
(evaluate "00645-p15-s23-stage2-runtime-artifact-source-relative-path.clj"
          (named? '#{p15-s23-stage2-compiler-expected-source-content-hash
                     p15-s23-stage2-compiler-expected-source-byte-count}))
(evaluate "00719-p15-s23-c6c10-record-compiled-binding-metric.clj"
          (named? '#{p15-s23-c6c10-canonical-file-path}))
(evaluate "00727-p15-s23-c11-mir-required-functions.clj"
          (named? '#{p15-s23-c11-mir-digest}))
(evaluate "00723-p15-s23-c6c10-seal-digest-request-result.clj"
          (named? '#{p15-s23-c6c10-require-trusted-carrier!}))
(evaluate "00720d-p15-s23-c6c10-reader-owner-projection.clj"
          (constantly true))
(evaluate "00720b-p15-s23-c6c10-current-reader-program.clj"
          (constantly true))
(evaluate "00720c-p15-s23-c6c10-current-reader-envelope.clj"
          (constantly true))

(def reader-path (call 'p15-s23-c6c10-canonical-file-path "synthetic-reader.gravity"))
(def syntax-path (call 'p15-s23-c6c10-canonical-file-path "synthetic-syntax.gravity"))
(def emitter {:synthetic-unit-emitter true})
(def rule-hash (str "sha256:" (digest/sha256-hex (pr-str (call 'c-backend-canonical-value emitter)))))
(def rule-record {:status :complete :synthetic-unit-rule-record true})
(def emitter-rule {:source-path "synthetic-compiler.gravity" :source-rule-hash rule-hash
                   :emitter emitter :rule-record rule-record})
(def fixture-contract (assoc-in contract/current-reader-program [:plan-emitter :source-rule-hash] rule-hash))
(defn program-binding []
  (assoc (:binding fixture-contract) :source-path reader-path
         :plan {:functions (:exports fixture-contract)
                :compiler (assoc (:declared-expression-labels fixture-contract)
                                 :emitter-source-rule-hash rule-hash)}))
(defn inputs [version source-mode]
  {:source-snapshot {:source-path reader-path :canonical-source-path reader-path
                     :source-byte-count (:source-byte-count (call 'p15-s23-c6c10-source-program-contract version))
                     :source-content-hash (:source-content-hash (call 'p15-s23-c6c10-source-program-contract version))}
   :source-mode source-mode
   :emitter-rule emitter-rule})
(def events (atom []))
(defn with-reader-oracles [options f]
  (reset! events [])
  (with-redefs [contract/current-reader-program fixture-contract]
    (redefine
     {'p15-s23-c6c10-authenticated-source-binding-inputs!
      (fn [_ version source-mode]
        (swap! events conj [:input-owner version source-mode])
        (let [n (count (filter #(= :input-owner (first %)) @events))]
          (if-let [change (:input-change options)]
            (change n (inputs version source-mode))
            (inputs version source-mode))))
      'sh03-reader-resolve-source-path (constantly reader-path)
      'sh04-syntax-resolve-source-path (constantly syntax-path)
      'sh03-reader-current-binding!
      (fn [_ path]
        (swap! events conj [:reader-owner path])
        ((or (:binding-change options) identity) (program-binding)))
      'p15-s23-stage2-plan-emitter-rule-record (constantly rule-record)}
     f)))

(defn products []
  (let [selected (dissoc (program-binding) :plan)
        revision (merge (select-keys selected [:source-content-hash :source-byte-count
                                               :plan-semantic-hash :functions-semantic-hash])
                        {:builder-semantic-hash (:entrypoint-semantic-hash selected)
                         :function-shapes (sorted-map 'sh03-verify-reader-result
                                                      {:semantic-hash (:verifier-semantic-hash selected)})})
        descriptor {:source-revision revision :synthetic-unit-descriptor true}
        envelope {:artifact :synthetic-envelope :schema-version 1 :stage :c2-reader
                  :status :accepted :semantic-authority :gravity-source
                  :semantic-envelope-id "synthetic-envelope-id" :provenance-binding-id "synthetic-provenance-id"
                  :source-rule {:admission-status :development-unadmitted :function-shapes (sorted-map 'reader {:arity 3})}
                  :gravity-template-replay {:status :passed :checks [:synthetic] :diagnostics []}}
        provenance {:actual-sh03-envelope-descriptor descriptor
                    :actual-sh03-authenticated-envelope envelope
                    :actual-c2-artifact-id "synthetic-c2"
                    :actual-reader-product-integrity-hash "synthetic-integrity"
                    :actual-reader-source-id "synthetic-source-id"
                    :actual-sh03-reader-result-id "synthetic-result-id"
                    :actual-sh03-semantic-envelope-id "synthetic-envelope-id"
                    :actual-sh03-provenance-binding-id "synthetic-provenance-id"
                    :actual-sh03-semantic-product-binding {:adapter-contract :synthetic}}
        c2 {:artifact-id "synthetic-c2"
            :reader-product-integrity {:integrity-hash "synthetic-integrity"}
            :source-unit-record {:source-id "synthetic-source-id"}
            :sh03-reader-authentication {:reader-source-revision revision
                                         :reader-result-id "synthetic-result-id"
                                         :semantic-envelope-id "synthetic-envelope-id"
                                         :provenance-binding-id "synthetic-provenance-id"}}]
    {:binding selected
     :raw {:c2-reader-artifact c2
           :authenticated-c3-source-artifact
           {:gravity-syntax-boundary {:reader-authentication-provenance provenance}}}}))
(def envelope-calls (atom []))
(defn with-envelope-oracle [result f]
  (reset! envelope-calls [])
  (redefine {'p15-s23-stage2-sh02-descriptor-envelope-verify!
             (fn [& args] (swap! envelope-calls conj args) result)} f))
(defn receipt [product]
  (call 'p15-s23-c6c10-current-reader-envelope-state!
        "synthetic.gravity" (:raw product) reader-path (:binding product)))
(defn expression-projection []
  (call 'p15-s23-c6c10-project-expression-owner-metadata
        "synthetic.gravity"
        (merge (select-keys contract/current-expression-program
                             [:source-content-hash :semantic-hash :functions-semantic-hash
                              :artifact-hash :function-count])
               {:source-contract-version 2 :source-mode :development-current
                :source-program {:version 2}
                :source-path "synthetic-emitter.gravity" :self-hosted? false
                :functions (into (sorted-map) (:required-functions contract/current-expression-program))
                :declared-plan-compiler-labels (:declared-plan-compiler-labels contract/current-expression-program)})))

(deftest selected-reader-plan-keeps-source-input-bookends-and-nonclaims
  (with-reader-oracles {}
    (fn []
      (let [result (call 'p15-s23-c6c10-current-reader-program! "synthetic.gravity")]
        (is (= [[:input-owner 2 :development-current]
                [:reader-owner reader-path]
                [:input-owner 2 :development-current]] @events))
        (is (false? (:reader-result-executed? result)))
        (is (false? (:envelope-state-observed? result)))
        (is (= {:source :development-current}
               (get-in result [:invocation :authenticated-envelope-options])))))))

(deftest selected-program-input-path-export-and-emitter-drift-refuse
  (doseq [[options fact]
          [[{:input-change (fn [n input]
                             (cond-> input (= 2 n)
                               (assoc-in [:source-snapshot :source-content-hash] "changed")))}
            :stable-current-reader-dependency-inputs]
           [{:binding-change #(assoc % :source-path "changed.gravity")}
            :selected-current-reader-source-path]
           [{:binding-change #(assoc % :source-content-hash "changed")}
            :selected-current-reader-program-identities]
           [{:binding-change #(assoc-in % [:plan :functions 'sh03-read-source-unit :arity] 99)}
            :selected-current-reader-export-shapes]
           [{:binding-change #(assoc-in % [:plan :compiler :emitter-source-content-hash] "changed")}
            :selected-current-reader-plan-emitter-join]]]
    (with-reader-oracles options
      (fn []
        (is (= fact
               (:missing-fact
                (diagnostic
                 (fn [] (call 'p15-s23-c6c10-current-reader-program!
                              "synthetic.gravity"))))))))))

(deftest public-c6-source-version-and-private-default-routing
  (with-reader-oracles {}
    (fn []
      (let [result (call 'p15-s23-c6c10-current-reader-program! "synthetic.gravity")]
        (is (= syntax-path (get-in result [:invocation :syntax-source-selection :source-path])))
        (is (= :gravity/sh04-development-syntax-selection-v1
               (get-in result [:invocation :syntax-source-selection :schema]))))
      (is (= 2 (second (last @events))))))
  (with-envelope-oracle :passed
    (fn []
      (is (= 2 (get-in (receipt (products)) [:source-interface :version])))))
  (doseq [version [1 3 nil (int 3) 3N]]
    (is (some? (try (c6-interface/interface-version!
                    {:interface :gravity/c6-c10 :version version})
                   nil
                   (catch clojure.lang.ExceptionInfo error (ex-data error)))))))

(deftest projected-reader-expression-receipt-keeps-owner-schema
  (with-envelope-oracle :passed
    (fn []
      (let [reader-receipt (receipt (products))
            complete (assoc reader-receipt :expression-binding (expression-projection))]
        (is (= :gravity/c6-reader-owner-metadata-projection
               (get-in complete [:projection-contract :artifact])))
        (is (contains? (:reader-plan-binding complete) :instruction-summary-content-id))
        (is (contains? (:reader-source-revision complete) :function-shapes-content-id))
        (is (= :passed (:envelope-verification complete)))
        (is (false? (:self-hosted? complete)))))))

(deftest raw-product-joins-and-envelope-owner-failure-refuse
  (doseq [change [#(assoc-in % [:raw :c2-reader-artifact :artifact-id] "changed")
                  #(assoc-in % [:raw :c2-reader-artifact :reader-product-integrity :integrity-hash] "changed")
                  #(assoc-in % [:raw :c2-reader-artifact :source-unit-record :source-id] "changed")
                  #(assoc-in % [:raw :c2-reader-artifact :sh03-reader-authentication :reader-result-id] "changed")]]
    (with-envelope-oracle :passed
      (fn []
        (is (= :actual-selected-current-reader-products
               (:missing-fact (diagnostic #(receipt (change (products)))))))
        (is (empty? @envelope-calls)))))
  (with-envelope-oracle :failed
    (fn []
      (is (= :actual-current-reader-envelope-verification
             (:missing-fact (diagnostic (fn [] (receipt (products)))))))))
  (with-envelope-oracle :passed
    (fn []
      (let [changed (assoc-in (products)
                              [:raw :authenticated-c3-source-artifact :gravity-syntax-boundary
                               :reader-authentication-provenance :actual-sh03-authenticated-envelope
                               :source-rule :admission-status] :frozen-admitted)]
        (is (= :actual-current-reader-envelope-admission-state
               (:missing-fact (diagnostic #(receipt changed)))))))))

(deftest header-and-metadata-class-checks-do-not-call-foreign-code
  (let [callbacks (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! callbacks inc) {})
                  (hashCode [] (swap! callbacks inc) 0)
                  (equals [_] (swap! callbacks inc) true)
                  (toString [] (swap! callbacks inc) "foreign"))
        comparator (reify java.util.Comparator
                     (compare [_ _ _] (swap! callbacks inc) 0))]
    (with-envelope-oracle :passed
      (fn []
        (doseq [raw [foreign
                     (clojure.lang.PersistentArrayMap. (object-array [foreign nil]))
                     (clojure.lang.PersistentArrayMap. (object-array [:duplicate nil :duplicate nil]))]]
          (is (= :reader-owner-product-header
                 (:missing-fact (diagnostic #(receipt (assoc (products) :raw raw)))))))
        (doseq [change [#(assoc-in % [:binding :foreign] foreign)
                        #(update % :binding with-meta {:caller true})
                        #(assoc-in % [:binding :instruction-summary] (sorted-map-by comparator))]]
          (is (= :trusted-current-reader-owner-metadata
                 (:missing-fact (diagnostic #(receipt (change (products))))))))
        (is (empty? @envelope-calls))
        (is (= :passed (:envelope-verification (receipt (assoc-in (products)
                                                                  [:raw :unused-source-forms]
                                                                  foreign)))))
        (is (zero? @callbacks))))))

(let [result (run-tests 'c6-reader-policy-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :current-reader-policy-mechanism-controls
            :owner-files ["00720b" "00720c" "00720d"]
            :native-header-carrier-projection-and-digests :actual
            :reader-input-emitter-and-envelope-owners :declared-oracles
            :fixtures :synthetic :source-authentication false
            :reader-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
