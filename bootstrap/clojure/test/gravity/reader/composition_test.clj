(ns gravity.reader.composition-test
  "Compiler-free composition checks of exact named source bodies and admission
  predicates. Ports use synthetic DATA; no Bootstrap or gravity.syntax load."
  (:require [clojure.java.io :as io]
            [gravity.reader.pure-text :as pure-text]
            [clojure.test :refer [deftest is]]))

;; Source-evaluated bodies use their production alias in this isolated ns.
(alias 'reader-pure-text (ns-name (:ns (meta #'pure-text/admission-required?))))
(declare reader-artifact-view current-reader-identity-view)

(def ^:private component-root
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/")

(defn- definition [path name]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop []
        (let [form (read {:eof ::end} reader)]
          (when (= ::end form) (throw (ex-info "Missing source body" {:name name})))
          (if (and (seq? form) (= name (second form))) form (recur)))))))

(def ^:private view-body
  (definition "bootstrap/clojure/src/gravity/syntax.clj" 'reader-artifact-view))
(def ^:private identity-body
  (definition "bootstrap/clojure/src/gravity/syntax.clj" 'current-reader-identity-view))

(doseq [form [view-body identity-body]]
  (binding [*ns* (the-ns 'gravity.reader.composition-test)] (eval form)))

(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))

(def ^:private artifact
  {:kind :gravity/stage0-c2-reader-document-artifact
   :artifact-id "synthetic-artifact" :source-unit-record {:path "synthetic.gravity"}
   :reader-product-integrity {:integrity-hash "synthetic-integrity"}})

(defn- ports [calls]
  {:sh03-reader-internal-product-authority :synthetic-authority
   :p15-s23-trusted-carrier-validation (fn [& _] {:status :passed})
   :sh03-c3-precomputed-c2-verify!
   (fn [& args] (swap! calls conj [:reverification args]))
   :sh04-syntax-registered-literal-registry
   (fn [& _] (swap! calls conj :registry) {:bindings []})
   :sh04-syntax-semantic-source-id (fn [& _] :synthetic-source)
   :sh04-syntax-reader-binding
   (fn [& _] {:reader-binding :synthetic-binding :reader-source-revision :synthetic-revision})
   :p15-s23-c6c10-canonical-digest (fn [& _] "synthetic-digest")
   :sh04-syntax-current-sh03-product-binding (fn [& _] :synthetic-product)
   :sh04-syntax-project-registered-literal-values
   (fn [& _] (throw (ex-info "Unexpected projection in empty registry model" {})))})

(deftest standard-view-keeps-opaque-reverification-port
  (let [calls (atom []) invocation {:source :synthetic}
        result (reader-artifact-view artifact invocation (ports calls))]
    (is (= :reverification (ffirst @calls)))
    (is (= [:synthetic-authority "synthetic.gravity" artifact invocation]
           (second (first @calls))))
    (is (= (:artifact-id artifact) (:artifact-id result)))
    (is (not (contains? result :pure-extension-results)))
    (let [sentinel (ex-info "Opaque reverification refusal" {:id "C2-HASH"})]
      (is (identical? sentinel
                      (caught #(reader-artifact-view
                                 artifact invocation
                                 (assoc (ports calls) :sh03-c3-precomputed-c2-verify!
                                        (fn [& _] (throw sentinel))))))))))

(deftest unsealed-companion-refuses-before-view-projection
  (doseq [companion [nil [] [{:unsealed :synthetic-result}]]]
    (let [calls (atom [])
          error (caught #(reader-artifact-view
                           (assoc artifact :pure-extension-results companion)
                           {:source :synthetic} (ports calls)))]
      (is (= {:id "C2-HASH" :missing-fields [:authenticated-pure-extension-results]}
             (ex-data error)))
      (is (empty? @calls)))))

(deftest identity-view-conserves-companion-without-conferring-admission
  (let [calls (atom []) data (assoc artifact :pure-extension-results [:unsealed])]
    (is (identical? data (current-reader-identity-view "synthetic.gravity" data (ports calls))))
    (is (= [:unsealed] (:pure-extension-results data)))
    (let [projected (assoc data :registered-literal-projection :synthetic)]
      (is (identical? projected
                      (current-reader-identity-view "synthetic.gravity" projected (ports calls)))))))

(deftest precomputed-admission-has-actual-unsealed-field-refusal
  (let [body (definition
               (str component-root "self-hosting-sh03-sh07/00828-sh03-reader-internal-product-authority.clj")
               'sh03-reader-precomputed-products-verify!)
        guard (some #(when (= '(not (contains? products :pure-extension-results)) %) %)
                    (tree-seq coll? seq body))]
    (is (some? guard))
    ;; Execute only the exact field-admission predicate, not the verifier.
    (let [admitted? (eval (list 'fn '[products] guard))]
      (is (true? (admitted? {})))
      (doseq [companion [nil [] [:unsealed]]]
        (is (false? (admitted? {:pure-extension-results companion})))))))

(deftest removing-view-guard-demonstrates-silent-drop-regression
  (let [guard (some #(when (= '(contains? c2-artifact :pure-extension-results) (second %)) %)
                    (drop 3 view-body))
        _ (is (some #{:pure-extension-results} (tree-seq coll? seq guard)))
        mutant (binding [*ns* (the-ns 'gravity.reader.composition-test)]
                 (eval (list* 'fn (nth view-body 2) (remove #{guard} (drop 3 view-body)))))
        calls (atom [])
        result (mutant (assoc artifact :pure-extension-results [:unsealed]) nil (ports calls))]
    ;; Without the guard the inherited whitelist drops the field. The real
    ;; negative test requires refusal, so this mutant would violate its oracle.
    (is (not (contains? result :pure-extension-results)))
    (is (= [:registry] @calls))))
