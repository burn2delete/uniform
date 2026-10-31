(ns gravity.reader.resource-preparation-test
  "Inert source models with injected decode/scan/parse ports; no Bootstrap,
  Gravity plan, actual Reader pipeline, provider or command execution."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]))

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (io/reader path))]
      (loop [out []]
        (let [form (read {:eof ::end} r)]
          (if (= ::end form) out (recur (conj out form))))))))

(def ^:private definitions
  (into {} (for [form (source-forms "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
                 :when (and (seq? form) (= 'defn (first form)))]
             [(second form) form])))

(def ^:private resource-tag [114 101 115 111 117 114 99 101])
(def ^:private span {:byte-start 0 :byte-end 18})
(def ^:private graph
  [{:form-id "form/0" :kind :tagged-literal :children ["form/1"]
    :tag-codepoints resource-tag :span span :raw [35 114]}
   {:form-id "form/1" :kind :string :children []
    :value {:kind :string :decoded-codepoints [105 110 112 117 116]}}])

(defn- model [ports run]
  (let [n (create-ns (gensym "resource-preparation-model-"))]
    (try
      (binding [*ns* n]
        (clojure.core/refer 'clojure.core)
        (eval (cons 'declare (filter #(nil? (namespace %)) (keys definitions))))
        (doseq [name '[sh03-exact-keys? sh03-vector-contains? sh03-all-unique?
                      sh03-valid-tag-codepoints? sh03-registered-tag?
                      sh03-reader-tags-registered-shape? sh03-reader-pure-tags?
                      sh03-reader-policy-valid? sh03-record-by-id
                      sh03-reader-tag-payload-valid? reader-resource-forms
                      reader-prepare-resources reader-verify-resource-preparation]]
          (eval (get definitions name)))
        (doseq [[name value] ports] (intern n name value)))
      (run #(ns-resolve n %))
      (finally (remove-ns (ns-name n))))))

(defn- preparation [forms run]
  (let [calls (atom [])]
    (model {'sh03-source-unit-valid? #(= {:source :original} %)
            'sh03-source-byte-vector-valid? (fn [_ bytes] (vector? bytes))
            'sh03-decode-source-bytes
            (fn [_ bytes] (swap! calls conj [:decode bytes])
              {:status :accepted :characters [:modeled-characters]})
            'sh03-scan-tokens
            (fn [characters] (swap! calls conj [:scan characters])
              {:status :accepted :tokens [:modeled-tokens]})
            'sh03-parse-tokens
            (fn [tokens policy] (swap! calls conj [:parse tokens policy])
              {:status :accepted :form-tree forms
               :top-level-form-ids ["form/0"] :top-level-values [:modeled-value]})}
           #(run % calls))))

(deftest preparation-is-pending-and-complete-replay-binds-original-inputs
  (preparation graph
    (fn [resolve-fn calls]
      (let [prepare (resolve-fn 'reader-prepare-resources)
            verify (resolve-fn 'reader-verify-resource-preparation)
            unit {:source :original} bytes [35 114]
            result (prepare unit bytes)]
        (is (= :prepared (:status result)))
        (is (= :gravity/reader-resource-preparation (:artifact result)))
        (is (= unit (:source-unit result)))
        (is (= bytes (:source-bytes result)))
        (is (= graph (get-in result [:parsed :form-tree])))
        (is (= [{:form-id "form/0" :payload-form-id "form/1" :span span
                 :raw [35 114] :input-codepoints [105 110 112 117 116]}]
               (:resources result)))
        (is (not-any? #(contains? result %) [:reader-extension-invocation-records
                                            :digest-requests :semantic-reader-template]))
        (is (= :accepted (:status (verify unit bytes result))))
        (is (= [:decode :scan :parse :decode :scan :parse] (mapv first @calls)))
        (doseq [changed [(assoc result :extra true)
                         (assoc result :status :accepted)
                         (assoc result :resources [])
                         (update result :resources #(into % %))
                         (assoc-in result [:resources 0 :span :byte-end] 19)
                         (assoc-in result [:resources 0 :input-codepoints] [120])
                         (assoc-in result [:resources 0 :payload-form-id] "other")
                         (assoc-in result [:parsed :form-tree] [])
                         (assoc-in result [:reader-policy :retain-trivia] false)
                         (assoc result :tokens [])
                         (assoc result :source-unit {:source :other})
                         (assoc result :source-bytes [0])]]
          (is (= :rejected (:status (verify unit bytes changed)))))
        ;; Even when modeled parse output is identical, the original bytes
        ;; are independently bound. A rejected source never verifies itself.
        (is (= :rejected (:status (verify unit [0] result))))
        (is (= :rejected (:status (verify {:source :other} bytes result))))
        (is (= :rejected (:status (verify {:source :other} bytes
                                         (prepare {:source :other} bytes)))))))))

(deftest full-population-rejection-keeps-no-partial-resources
  (doseq [bad [(assoc-in graph [0 :tag-codepoints] [120])
               (assoc-in graph [0 :children] [])
               (assoc-in graph [0 :children] ["form/1" "form/1"])
               (assoc-in graph [1 :kind] :integer)
               (assoc-in graph [1 :value :decoded-codepoints] [])
               (conj graph {:form-id "form/2" :kind :tagged-literal
                            :children ["form/1"] :tag-codepoints [120]})]]
    (preparation bad
      (fn [resolve-fn _]
        (let [result ((resolve-fn 'reader-prepare-resources) {:source :original} [1])]
          (is (= :rejected (:status result)))
          (is (= :resource-form-contract (get-in result [:failure :reason])))
          (is (not (contains? result :resources))))))))

(deftest distinct-occurrences-of-the-same-resource-remain-distinct
  (let [second-form (assoc (first graph) :form-id "form/2" :children ["form/3"]
                           :span {:byte-start 19 :byte-end 37})
        second-payload (assoc (second graph) :form-id "form/3")]
    (preparation (conj graph second-form second-payload)
      (fn [resolve-fn _]
        (let [result ((resolve-fn 'reader-prepare-resources) {:source :original} [1])]
          (is (= :prepared (:status result)))
          (is (= ["form/0" "form/2"] (mapv :form-id (:resources result))))
          (is (= ["form/1" "form/3"] (mapv :payload-form-id (:resources result))))
          (is (= [span (:span second-form)] (mapv :span (:resources result))))
          (is (apply = (map :input-codepoints (:resources result)))))))
    (preparation []
      (fn [resolve-fn _]
        (is (= [] (:resources ((resolve-fn 'reader-prepare-resources)
                              {:source :original} [1]))))))))

(deftest decoder-scanner-parser-errors-stop-at-the-owning-stage
  (doseq [[stage name] [[:decode 'sh03-decode-source-bytes]
                       [:scan 'sh03-scan-tokens] [:parse 'sh03-parse-tokens]]]
    (preparation graph
      (fn [resolve-fn _]
        (let [failure {:status :rejected :reason stage}
              v (resolve-fn name)]
          ;; The functions are ports in an inert namespace; replace only the
          ;; selected stage through with-redefs-fn, never a production loader.
          (is (some? v))
          (with-redefs-fn {v (fn [& _] failure)}
            #(let [result ((resolve-fn 'reader-prepare-resources) {:source :original} [1])]
               (is (= :rejected (:status result)))
               (is (= failure (:failure result))))))))))

(deftest normal-and-pure-policies-still-reject-resource-mode
  (model {} (fn [resolve-fn]
              (let [valid? (resolve-fn 'sh03-reader-policy-valid?)
                    payload? (resolve-fn 'sh03-reader-tag-payload-valid?)
                    resource-policy {:artifact :gravity/reader-resource-preparation-policy
                                     :schema-version 1 :retain-trivia true
                                     :enabled-reader-tags [resource-tag]
                                     :extension-policy :gravity/declared-input-reader-preparation}
                    payload (second graph)]
                (is (false? (valid? resource-policy)))
                (is (false? (valid? (assoc resource-policy :artifact :gravity/sh03-reader-policy))))
                (is (true? (payload? {:tag-codepoints resource-tag
                                     :extension-policy :gravity/declared-input-reader-preparation}
                                    payload)))
                (doseq [mode [nil :gravity/pure-text-reader :gravity/sealed-pure-text-reader]]
                  (is (false? (payload? {:tag-codepoints resource-tag :extension-policy mode}
                                       payload))))))))

(deftest actual-builder-selects-only-the-existing-closure-and-fixed-preparation-functions
  (let [forms (source-forms
               "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00809-sh03-reader-plan-instruction-summary.clj")
        builder (first (filter #(= 'sh03-reader-build-plan! (second %)) forms))
        bindings (second (nth builder 3))
        selection (get (apply hash-map bindings) 'selected-functions)
        predicate (second (nth selection 2))
        selected? (binding [*ns* (the-ns 'gravity.reader.resource-preparation-test)]
                    (eval (list 'let ['sh03-reader-function-prefix "sh03-"] predicate)))
        expected '#{reader-resource-forms reader-prepare-resources
                    reader-verify-resource-preparation}]
    (is (every? #(selected? [% {}]) expected))
    (is (selected? ['sh03-read-source-unit {}]))
    (doseq [other '[read-input reader-other stage1-read-source]]
      (is (not (selected? [other {}]))))
    (is (= expected (set (filter #(str/starts-with? (name %) "reader-")
                                (keys definitions)))))
    (is (not-any? #(= 'build-read-declared-input %)
                  (tree-seq coll? seq (map definitions expected))))))
