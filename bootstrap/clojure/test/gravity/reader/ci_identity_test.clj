(ns gravity.reader.ci-identity-test
  "Compiler-free source projections for current Reader identity dependencies."
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.walk :as walk]
            [gravity.reader.identity-expectations :as expected]))

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. (slurp path)))]
      (loop [result []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) result (recur (conj result form))))))))

(defn- named-form [forms name]
  (or (some #(when (and (seq? %) (= name (second %))) %) forms)
      (throw (ex-info "Missing inspected source form" {:name name}))))

(def ^:private model-names (atom []))

(use-fixtures :each
  (fn [test]
    (try (test)
         (finally
           (doseq [name @model-names] (remove-ns name))
           (reset! model-names [])))))

(defn- model-function [form ports]
  (let [model (create-ns (gensym "reader-ci-identity-model-"))]
    (swap! model-names conj (ns-name model))
    (binding [*ns* model]
        (clojure.core/refer 'clojure.core)
        (doseq [[name function] ports] (intern model name function))
        (eval form)
        (var-get (ns-resolve model (second form))))))

(deftest syntax-native-admission-dependency-is-explicit-and-acyclic
  (let [forms (source-forms "bootstrap/clojure/src/gravity/syntax.clj")
        requires (->> (first forms) (filter seq?) (filter #(= :require (first %)))
                      first rest (mapv first))
        contract (second (nth (named-form forms 'namespace-contract) 2))
        dependency (source-forms "bootstrap/clojure/src/gravity/reader/pure_text.clj")
        leaf-requires (->> (first dependency) (filter seq?) (filter #(= :require (first %)))
                           first rest (mapv first))]
    (is (= '[clojure.string gravity.reader.pure-text gravity.c3-reader-expansions gravity.c3-syntax-verification]
           requires))
    (is (= (vec (cons 'clojure.core requires)) (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.c2-artifact-identity.canonical gravity.digest] leaf-requires))
    (is (not-any? #{'gravity.bootstrap 'gravity.cli 'gravity.release} leaf-requires))
    (is (not= (vec (cons 'clojure.core (remove #{'gravity.reader.pure-text} requires)))
              (get-in contract [:dependency-direction :requires])))))

(deftest macro-parent-and-implementation-identities-survive-the-actual-projection
  (let [forms (source-forms "bootstrap/clojure/src/gravity/macro.clj")
        form (named-form forms 'artifact-payload)
        operations {:sh05-macro-run-semantic-summary identity
                    :sh05-ordered-identity-chunks (fn [_ _] [])
                    :sh05-path-neutral-semantic-value identity}
        project (model-function form {})
        fixture {:gravity-macro-boundary {:plan-binding {:source-content-hash "Macro-implementation-A"}}
                 :sh04-syntax-artifact {:artifact-id "C3-A" :syntax-stream-id "syntax-A"}}
        expected (:sh04-syntax-artifact fixture)
        preserves? (fn [f] (= expected (:sh04-syntax-artifact (f fixture operations))))]
    (is (preserves? project))
    (doseq [path [[:sh04-syntax-artifact :artifact-id]
                  [:sh04-syntax-artifact :syntax-stream-id]
                  [:gravity-macro-boundary :plan-binding :source-content-hash]]]
      (is (not= (project fixture operations) (project (assoc-in fixture path "changed") operations))))
    ;; A production mutation that drops the authenticated C3 parent is detected.
    (let [mutant (walk/postwalk #(if (map? %) (dissoc % :sh04-syntax-artifact) %) form)]
      (is (false? (preserves? (model-function mutant {})))))
    (let [mutant (walk/postwalk #(if (= :source-content-hash %) :lost-source-hash %) form)
          actual ((model-function mutant {}) fixture operations)]
      (is (not= "Macro-implementation-A" (get-in actual [:plan-binding :source-content-hash]))))))

(deftest resolution-retains-every-upstream-join-in-the-actual-preimage
  (let [forms (source-forms "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00877-sh06-resolution-product-projections.clj")
        form (named-form forms 'sh06-resolution-artifact-identity-input-with-chunks)
        ;; Identity instead of hashing exposes the complete dependency DATA.
        ports {'reader-canonical-hash identity}
        project (model-function form ports)
        fixture {:gravity-resolution-boundary
                 {:resolved-analysis {:artifact-id "analysis-A"}
                  :authenticated-resolution-request {:module {:source-revision-id "source-A"}}}
                 :sh05-macro-artifact {:artifact-id "macro-A" :expanded-syntax-stream-id "syntax-A"
                                      :macro-expansion-trace-id "trace-A"}}
        expected {:analysis-artifact-id "analysis-A" :source-revision-id "source-A"
                  :upstream-artifact-id "macro-A" :upstream-syntax-stream-id "syntax-A"
                  :upstream-trace-id "trace-A"}
        fields (set (keys expected))
        preserves? (fn [f] (= expected (select-keys (f fixture {}) fields)))]
    (is (preserves? project))
    (doseq [field fields]
      (let [mutant (walk/postwalk #(if (map? %) (dissoc % field) %) form)]
        (is (false? (preserves? (model-function mutant ports))) (str "lost join " field))))))

(defn- changed-map-field [form field action]
  (walk/postwalk (fn [x]
                   (if (and (map? x) (contains? x field))
                     (case action :drop (dissoc x field) :alter (assoc x field ::altered))
                     x)) form))

(defn- macro-projector
  ([form] (macro-projector form {}))
  ([form overrides]
  (let [forms (source-forms "bootstrap/clojure/src/gravity/macro.clj")
        project (model-function form {})
        summarize (model-function (get overrides :summary (named-form forms 'run-summary)) {})
        group (model-function (get overrides :chunks (named-form forms 'identity-chunks)) {})
        neutral (model-function (named-form forms 'path-neutral-value) {})]
    (letfn [(clean [v] (neutral v {:sh05-path-neutral-semantic-value clean}))]
      (fn [artifact]
        (project artifact {:sh05-macro-run-semantic-summary summarize
                           :sh05-ordered-identity-chunks
                           (fn [values projector] (group values projector {:input (constantly 16)}))
                           :sh05-path-neutral-semantic-value clean}))))))

(defn- macro-fixture []
  {:kind :macro :slice :SH-05 :task "macro" :status :accepted
   :document-set ["C4" "L4"] :governing-document "C4"
   :execution-boundary {:owner :gravity :self-hosted? false}
   :macro-environment {:macros [{:name 'defn :version "one"}]}
   :pass {:input :syntax :output :expanded} :diagnostics []
   :expanded-defn-count 18 :expanded-syntax-stream-id "expanded-A"
   :macro-expansion-trace-id "trace-A"
   :sh04-syntax-artifact {:artifact-id "dynamic-C3-A" :syntax-stream-id "dynamic-syntax-A"}
   :gravity-macro-boundary
   {:slice :SH-05 :owner :gravity :adapter-contract :macro-v2 :target-source-reread? false
    :clojure-adapter-residual? true :self-hosted? false
    :plan-binding (zipmap [:artifact :status :semantic-authority :compiled-by :executed-by
                          :generic-bridge-residual? :self-hosted? :source-language :source-byte-count
                          :source-content-hash :plan-semantic-hash :functions-semantic-hash
                          :function-count :function-names-hash :function-shapes-hash
                          :public-function-hashes :public-function-shapes]
                         [:plan :complete :gravity :seed :runner true false :gravity 12
                          "source-A" "plan-A" "functions-A" 1 "names-A" "shapes-A" {} {}])
    :expansion-runs
    (mapv (fn [n]
            {:raw-template-result {:artifact :template :schema-version 1 :status :accepted}
             :digest-requests [{:ordinal n :purpose :result :ignored "not-an-identity-input"}]
             :resolved-digests [(str "digest-" n)]
             :resolved-expansion {:artifact :expansion :schema-version 1 :artifact-id (str "result-" n)
                                  :status :accepted :macro 'defn :macro-version "one" :input-syntax-id (str "input-" n)
                                  :output-syntax-id (str "output-" n) :semantic-binding {:source "logical"}
                                  :profile :meta :target :jvm :phase :macro-expansion :bounds {}
                                  :macro-expansion-trace [{:trace-replay-id (str "replay-" n)}]}
             :template-verification {:status :passed} :resolved-verification {:status :passed}})
          (range 18))}})

(deftest macro-complete-equation-rejects-loss-or-alteration-of-every-field
  (let [form (named-form (source-forms "bootstrap/clojure/src/gravity/macro.clj") 'artifact-payload)
        project (macro-projector form) artifact (macro-fixture)
        preimage (expected/macro-preimage artifact)]
    (is (= 19 (count preimage)))
    (is (= preimage (project artifact)))
    (is (= (expected/macro-id artifact) (expected/macro-hash (project artifact))))
    (is (= [16 2] (mapv :item-count (:expansion-run-chunks preimage))))
    (is (= [0 16] (mapv :start-ordinal (:expansion-run-chunks preimage))))
    (doseq [field (keys preimage) action [:drop :alter]]
      (let [mutated ((macro-projector (changed-map-field form field action)) artifact)]
        (is (not= preimage mutated) (str "complete Macro equation catches " action " " field))
        (is (not= (expected/macro-id artifact) (expected/macro-hash mutated)))))
    (doseq [path [[:sh04-syntax-artifact :artifact-id] [:sh04-syntax-artifact :syntax-stream-id]
                  [:gravity-macro-boundary :plan-binding :source-content-hash]]]
      (let [changed (assoc-in artifact path "dynamic-B")]
        (is (= (expected/macro-preimage changed) (project changed)))
        (is (not= (expected/macro-id artifact) (expected/macro-id changed)))))
    (doseq [field (keys (get-in artifact [:gravity-macro-boundary :plan-binding]))]
      (let [mutant (walk/postwalk
                    (fn [x]
                      (if (and (seq? x) (= 'select-keys (first x)) (= '(:plan-binding boundary) (second x)))
                        (list 'select-keys (second x) (vec (remove #{field} (nth x 2)))) x)) form)]
        (is (not= preimage ((macro-projector mutant) artifact)))))
    (let [forms (source-forms "bootstrap/clojure/src/gravity/macro.clj")
          summary (named-form forms 'run-summary) chunks (named-form forms 'identity-chunks)]
      (doseq [field [:raw-result :digest-contract :semantic-resolved-digest :resolved-expansion
                     :trace-replay-id :template-verification :resolved-verification]
              action [:drop :alter]]
        (is (not= preimage ((macro-projector form {:summary (changed-map-field summary field action)}) artifact))))
      (doseq [field [:artifact :schema-version :artifact-id :status :macro :macro-version
                     :input-syntax-id :output-syntax-id :semantic-binding :profile :target :phase :bounds
                     :ordinal :purpose]]
        (let [mutant (walk/postwalk
                      (fn [x] (if (and (seq? x) (= 'select-keys (first x)) (vector? (nth x 2 nil)))
                                (list 'select-keys (second x) (vec (remove #{field} (nth x 2)))) x)) summary)]
          (is (not= preimage ((macro-projector form {:summary mutant}) artifact)))))
      (doseq [field [:artifact :schema-version :status] action [:drop :alter]]
        (is (not= preimage ((macro-projector form {:summary (changed-map-field summary field action)}) artifact))))
      (doseq [field [:chunk-index :start-ordinal :item-count :items] action [:drop :alter]]
        (is (not= preimage ((macro-projector form {:chunks (changed-map-field chunks field action)}) artifact)))))
    (let [reordered (update-in artifact [:gravity-macro-boundary :expansion-runs] #(vec (reverse %)))]
      (is (= (expected/macro-preimage reordered) (project reordered)))
      (is (not= (expected/macro-id artifact) (expected/macro-id reordered))))))

(defn- resolution-projector [form]
  (let [forms (source-forms "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00877-sh06-resolution-product-projections.clj")
        chunks (model-function (named-form forms 'sh06-resolution-identity-chunks)
                               {'reader-canonical-hash expected/resolution-hash})
        products (model-function (named-form forms 'sh06-resolution-product-identity-chunks)
                                 {'sh06-resolution-identity-chunks chunks})
        project (model-function form {'reader-canonical-hash expected/resolution-hash})]
    (fn [artifact] (project artifact (products artifact)))))

(defn- resolution-fixture []
  {:kind :resolution :slice :SH-06 :task "resolution"
   :gravity-resolution-boundary {:adapter-contract :resolution-v1
                                 :resolved-analysis {:artifact-id "dynamic-analysis-A"}
                                 :authenticated-resolution-request {:module {:source-revision-id "dynamic-source-A"}}}
   :sh05-macro-artifact {:artifact-id "dynamic-macro-A" :expanded-syntax-stream-id "dynamic-stream-A"
                        :macro-expansion-trace-id "dynamic-trace-A"}
   :binding-table {:bindings (mapv #(hash-map :binding-id (str "binding-" %)) (range 18))}
   :resolution-table (mapv #(hash-map :name (symbol (str "value" %)) :binding-id (str "binding-" %)
                                    :source-span {:byte-start %}) (range 18))
   :dependency-graph {:nodes ["first" "second"] :edges [{:from "first" :to "second" :source-span {:line 1}}]}
   :incremental-invalidation-keys {:invalidation-id "invalidation-A"}})

(deftest resolution-complete-equation-rejects-loss-or-alteration-of-every-field
  (let [form (named-form (source-forms "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00877-sh06-resolution-product-projections.clj")
                         'sh06-resolution-artifact-identity-input-with-chunks)
        project (resolution-projector form) artifact (resolution-fixture)
        preimage (expected/resolution-preimage artifact)]
    (is (= 14 (count preimage)))
    (is (= preimage (project artifact)))
    (is (= (expected/resolution-id artifact) (expected/resolution-hash (project artifact))))
    (is (= [16 2] (mapv :entry-count (:binding-id-chunks preimage))))
    (doseq [field (keys preimage) action [:drop :alter]]
      (let [mutated ((resolution-projector (changed-map-field form field action)) artifact)]
        (is (not= preimage mutated) (str "complete Resolution equation catches " action " " field))
        (is (not= (expected/resolution-id artifact) (expected/resolution-hash mutated)))))
    (doseq [path [[:gravity-resolution-boundary :resolved-analysis :artifact-id]
                  [:gravity-resolution-boundary :authenticated-resolution-request :module :source-revision-id]
                  [:sh05-macro-artifact :artifact-id] [:sh05-macro-artifact :expanded-syntax-stream-id]
                  [:sh05-macro-artifact :macro-expansion-trace-id] [:incremental-invalidation-keys :invalidation-id]]]
      (let [changed (assoc-in artifact path "dynamic-B")]
        (is (= (expected/resolution-preimage changed) (project changed)))
        (is (not= (expected/resolution-id artifact) (expected/resolution-id changed)))))
    (let [reordered (update artifact :resolution-table #(vec (reverse %)))]
      (is (= (expected/resolution-preimage reordered) (project reordered)))
      (is (not= (expected/resolution-id artifact) (expected/resolution-id reordered))))))

(deftest native-encoding-and-print-environment-are-fixed-for-the-Macro-equation
  (let [forms (source-forms "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00703-p15-s23-c6c10-canonical-form.clj")
        scalar (model-function (named-form forms 'p15-s23-c6c10-canonical-scalar-form)
                               {'p15-s23-c6c10-require-unicode-string! (fn [_ value] (assert (string? value)))
                                'p15-s23-c6c10-valid-named-component? (constantly true)
                                'p15-s23-c6c10-host-fail! (fn [& _] (throw (AssertionError. "invalid scalar")))})
        record (model-function (named-form forms 'p15-s23-c6c10-canonical-record)
                               {'p15-s23-c6c10-canonical-form* (fn [_ value] (expected/native-form value))})
        value {:z #{'value :value} :a [nil true 12 3/7 1.20M "text" \a (list 1 2)]}
        normal (expected/macro-hash value)]
    (is (= "[:gravity/canonical-edn-v1 [:nil]]" (:text (record "MODEL" nil))))
    (doseq [scalar-value [nil true false 12 -7 3/7 1.20M "text" \a :name/key 'name/value]]
      (is (= (scalar "MODEL" scalar-value) (expected/native-form scalar-value))))
    (is (= [:map [[:entry [:keyword nil "a"] [:integer "1"]]]]
           (expected/native-form {:a 1})))
    (is (= (expected/native-form value) (:form (record "MODEL" value))))
    (is (= normal (binding [*print-length* 1 *print-level* 1 *print-meta* true *print-namespace-maps* true]
                     (expected/macro-hash value))))
    (is (= normal (expected/macro-hash (into (array-map) (reverse (seq value))))))
    (is (not= (expected/macro-hash [1 2]) (expected/macro-hash (list 1 2))))
    (is (thrown? AssertionError (expected/native-form (with-meta [1] {:caller :metadata}))))
    (is (thrown? AssertionError (expected/native-form (sorted-map-by #(compare %2 %1) :a 1))))))
