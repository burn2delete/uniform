(ns gravity.reader.gravity-pure-text-test
  "SOURCE/MODELED checks: selected Gravity forms evaluated as Clojure with
  synthetic parsed graphs and explicit ports. No Gravity plan, parser, normal
  Reader, Bootstrap, provider, envelope implementation or native run executes."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]
            [gravity.reader.pure-text :as reader-pure-text]
            [gravity.reader.normal-dispatch-test :as normal]
            [gravity.reader.dispatch :as dispatch]))

(alias 'reader-dispatch 'gravity.reader.dispatch)

;; These names are installed from source below; declare direct test references
;; explicitly so static CI can also resolve the isolated model's calls.
(declare sh03-reader-read-text sh03-construct-parsed-source-unit
         sh03-reader-policy-valid? sh03-verify-reader-result sh03-digest-requests
         sh03-reader-sealed-text-record sh03-c3-precomputed-c2-verify!)

(defn- forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [f (read {:eof ::end} r)]
          (if (= ::end f) out (recur (conj out f))))))))

(def ^:private table
  (into {} (for [f (forms "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
                 :when (and (seq? f) (= 'defn (first f)))] [(second f) f])))

;; Declarations only for the source bodies' port dependencies. Unbound ports
;; fail if reached unexpectedly. No application namespace/load order is used.
(binding [*ns* (the-ns 'gravity.reader.gravity-pure-text-test)]
  (eval (cons 'declare (filter #(nil? (namespace %)) (keys table))))
  (doseq [name '[sh03-exact-keys? sh03-vector-contains? sh03-all-unique? sh03-valid-tag-codepoints?
                sh03-registered-tag? sh03-reader-tags-registered-shape?
                sh03-reader-pure-tags? sh03-reader-policy-valid?
                sh03-record-by-id sh03-reader-sealed-text-forms-valid?
                sh03-reader-read-text sh03-reader-sealed-text-record
                sh03-semantic-extension-invocations sh03-semantic-extension-records
                sh03-extension-records-have-invocations? sh03-digest-requests
                sh03-construct-parsed-source-unit sh03-verify-reader-result]]
    (eval (get table name))))

(def ^:private policy
  {:artifact :gravity/sh03-reader-policy :schema-version 1 :retain-trivia true
   :enabled-reader-tags [[116 101 120 116]] :extension-policy :gravity/sealed-pure-text-reader})
(def ^:private span {:byte-start 0 :byte-end 9})
(def ^:private source-input
  {:source-content-hash "synthetic-content" :project-root-id "synthetic-parent"})
(def ^:private graph
  [{:form-id "form/0" :kind :tagged-literal :tag-codepoints [116 101 120 116]
    :children ["form/1"] :span span :raw {:source :synthetic}}
   {:form-id "form/1" :kind :string :children [] :span span
    :value {:kind :string :decoded-codepoints [120]}}])

(defn- caught [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(defn- with-ports [ports f]
  (with-redefs-fn
    (into {} (for [[name value] ports]
               [(ns-resolve 'gravity.reader.gravity-pure-text-test name) value])) f))

(defn- construction [parsed-graph selected-policy f]
  (let [calls (atom []) handler sh03-reader-read-text
        source source-input
        build #(sh03-construct-parsed-source-unit
                source [1 2 3] selected-policy []
                {:form-tree parsed-graph :top-level-form-ids ["form/0"] :top-level-values []})]
    (with-ports
      {'sh03-reader-read-text (fn [seed] (swap! calls conj :handler) (handler seed))
       'sh03-filter-output-tokens (fn [tokens _] tokens)
       'sh03-semantic-value-table (fn [& _] [])
       'sh03-semantic-value-token-index (fn [& _] {})
       'sh03-semantic-value-form-index (fn [& _] {})
       'sh03-compact-token-records (fn [tokens _] tokens)
       'sh03-compact-form-records (fn [_ raw] raw)
       'sh03-literal-records (fn [& _] [])
       'sh03-deferred-literal-records (fn [& _] [])
       'sh03-source-map-record (fn [& _] {})
       'sh03-accepted-semantic-template (fn [& _] {:top-level-form-ids ["form/0"]})
       'sh03-semantic-source-unit (fn [unit p] (assoc unit :reader-options p))
       'sh03-actual-provenance (fn [& _] {})
       'sh03-top-level-records (fn [& _] [])
       'sh03-top-level-value-references (fn [& _] [])
       'sh03-bounds-record (fn [] {})
       'sh03-execution-boundary-record (fn [] {})
       'sh03-empty-span-at-byte (fn [_] span)
       'sh03-rejected-result (fn [_ _ _ rejection] rejection)
       'sh03-semantic-form-tree identity
       'sh03-semantic-value-identities identity
       'sh03-result-preflight? (constantly true)
       'sh03-digest-request-summaries (fn [requests] (mapv :key requests))
       'sh03-diagnostic (fn [& _] {:id "C2-HASH"})
       'sh03-read-source-unit (fn [unit bytes p]
                                (swap! calls conj :reconstruct)
                                (sh03-construct-parsed-source-unit
                                 unit bytes p [] {:form-tree parsed-graph
                                                  :top-level-form-ids ["form/0"]
                                                  :top-level-values []}))}
      #(f build calls))))

(deftest fixed-policy-and-standard-selection
  (is (true? (sh03-reader-policy-valid? policy)))
  (doseq [tags [[] [[105 110 115 116]] [[116 101 120 116] [114 101 115]]]]
    (is (false? (sh03-reader-policy-valid? (assoc policy :enabled-reader-tags tags)))))
  (is (true? (sh03-reader-policy-valid?
               (dissoc (assoc policy :enabled-reader-tags [[105 110 115 116] [117 117 105 100]])
                       :extension-policy)))))

(deftest construction-invokes-and-verifier-reconstructs
  (construction graph policy
    (fn [build calls]
      (let [result (build) record (first (:reader-extension-invocation-records result))
            invocation (first (:invocations record))]
        (is (= [:handler] @calls))
        (is (= (:input-seed invocation) (:returned-seed invocation)))
        (is (= 'sh03-reader-read-text (get-in invocation [:invocation :handler])))
        (is (= :accepted (:status (sh03-verify-reader-result source-input [1 2 3] policy result))))
        (is (= [:handler :reconstruct :handler] @calls))
        (doseq [changed [(assoc-in result [:reader-extension-invocation-records 0 :policy] {})
                         (assoc-in result [:reader-extension-invocation-records 0 :handler] 'other)
                         (assoc-in result [:reader-extension-invocation-records 0 :invocations 0 :returned-seed :datum] {})
                         (assoc-in result [:reader-extension-invocation-records 0 :invocations 0 :input-seed :source-id] "other")
                         (assoc-in result [:reader-extension-invocation-records 0 :invocations] [])
                         (update-in result [:reader-extension-invocation-records 0 :invocations] #(into % %))]]
          (is (= :rejected (:status (sh03-verify-reader-result source-input [1 2 3] policy changed)))))
        (doseq [[unit bytes p] [[(assoc source-input :project-root-id "other") [1 2 3] policy]
                              [source-input [3 2 1] policy]
                              [source-input [1 2 3] (assoc policy :retain-trivia false)]]]
          (is (= :rejected (:status (sh03-verify-reader-result unit bytes p result)))))
        (with-ports {'sh03-reader-read-text #(assoc % :datum {:kind :string :decoded-codepoints [121]})}
          #(is (= :rejected (:status (sh03-verify-reader-result source-input [1 2 3] policy result)))))
        ;; The actual digest preimage preserves the full result record, even
        ;; when ordinary trivia projection would omit occurrence fields.
        (doseq [retain [true false]]
          (let [p (assoc policy :retain-trivia retain)
                records [record]
                requests (sh03-digest-requests {} [] p {:top-level-form-ids []} [] [] [] records)]
            (is (= records (get-in requests [4 :preimage :records])))
            (is (= [0 1 3] (get-in requests [4 :depends-on])))
            (is (= [1 2 3 4] (get-in requests [5 :depends-on])))))))))

(deftest complete-population-refuses-before-modeled-handler
  (doseq [bad [(assoc-in graph [0 :tag-codepoints] [114 101 115])
               (conj graph {:form-id "form/2" :kind :tagged-literal
                            :tag-codepoints [114 101 115] :children ["form/1"]})
               (assoc-in graph [1 :kind] :integer)]]
    (construction bad policy
      (fn [build calls]
        (is (= "C2-EXTENSION" (:rule (build))))
        (is (empty? @calls))))))

(defn- resolved-model []
  (let [record (walk/postwalk
                #(case % {:digest-ref 0} "synthetic-content"
                         {:digest-ref 1} "synthetic-source" %)
                (sh03-reader-sealed-text-record graph policy))]
    {:source-unit {:source-id "synthetic-source" :bytes-hash "synthetic-content"
                   :reader-options policy}
     :form-tree graph :reader-extension-invocation-records [record]}))

(deftest adapter-projects-verified-return-and-refuses-substitution
  (let [result (resolved-model)]
    (is (= {"form/0" {:kind :string :decoded-codepoints [120]}}
           (reader-pure-text/returned-data! result)))
    (doseq [[path value]
            [[[:reader-extension-invocation-records 0 :handler] 'other]
             [[:reader-extension-invocation-records 0 :policy] {}]
             [[:reader-extension-invocation-records 0 :invocations 0 :returned-seed :datum] {}]
             [[:reader-extension-invocation-records 0 :invocations 0 :payload-form-id] "other"]
             [[:source-unit :source-id] "other"]
             [[:source-unit :bytes-hash] "other"]]]
      (is (= "C2-HASH" (:id (caught #(reader-pure-text/record! (assoc-in result path value)))))))
    (let [text "#text \"x\"" bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
          atomic {:artifact :gravity/semantic-value-reference :schema-version 1
                  :value-id "v" :field :descriptor}
          modeled-graph [(first graph) (assoc (second graph) :open-token "t" :value atomic)]
          index {:by-value-id {"v" {:form-id "form/1" :token-id "t" :kind :string
                                    :descriptor {:kind :string :decoded-codepoints [120]}}}}
          result (assoc result :form-tree modeled-graph)
          unit {:reader-options {:extension-policy (reader-pure-text/policy-id)}}]
      (with-redefs [dispatch/dispatch-pure-tag (fn [& _] (throw (ex-info "Old handler reached" {})))
                    dispatch/dispatch-standard-tag (fn [& _] (throw (ex-info "Old handler reached" {})))
                    normal/sh03-reader-host-atomic-value! (fn [& _] "x")]
        (let [values (normal/sh03-reader-host-values!
                      "model.gravity" bytes "synthetic-content" {} modeled-graph index unit {} result)]
          (is (= "x" (get-in values [:values "form/0"])))
          (is (empty? (:extension-dispatch-observations values)))
          (is (not (contains? values :pure-extension-results))))))))

(declare sh03-reader-internal-product-authority sh03-reader-verification-invocation!
         sh03-reader-authenticated-envelope-options p15-s23-trusted-carrier-validation
         reader-pure-source-artifact p15-s23-sh02-stage-envelope-keys
         p15-s23-stage2-sh02-descriptor-envelope-verify! c2-reader-fail!
         source-span standard-reader-options)

(defn reader-resource-admission-required? [_] false)
(declare reader-resource-consume! reader-resource-record!)

(binding [*ns* (the-ns 'gravity.reader.gravity-pure-text-test)]
  (eval (first (filter #(and (seq? %) (= 'sh03-c3-precomputed-c2-verify! (second %)))
                       (forms (str "bootstrap/clojure/src/gravity/bootstrap_impl/components/"
                                   "self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj"))))))

(deftest normal-consumer-port-reconstructs-before-admission
  (let [authority (Object.) calls (atom [])
        envelope {:status :accepted :stage :c2-reader
                  :sealed-artifact {:artifact-kind :gravity/sh03-reader-products}}
        artifact {:kind :gravity/stage0-c2-reader-document-artifact
                  :source-unit-record {:reader-options {:extension-policy (reader-pure-text/policy-id)}}
                  :reader-extension-invocation-records [{:schema-version 2}]
                  :gravity-reader-boundary
                  {:slice :SH-03 :owner :gravity-source
                   :adapter-contract :gravity/sh03-to-c2-reader-products-v2
                   :plan-binding {:source :synthetic-current}
                   :authenticated-envelope envelope :authenticated-envelope-descriptor {}
                   :pure-text-reconstruction-input {:source-text "original" :project-context {}}}}
        invocation {:reader-source-path "synthetic-current.gravity"}]
    (with-ports
      {'sh03-reader-internal-product-authority authority
       'p15-s23-sh02-stage-envelope-keys (set (keys envelope))
       'standard-reader-options {}
       'source-span (fn [& _] {})
       'c2-reader-fail! (fn [id _ _ facts] (throw (ex-info id (assoc facts :id id))))
       'p15-s23-trusted-carrier-validation (fn [& _] {:status :passed})
       'sh03-reader-authenticated-envelope-options (fn [& _] (swap! calls conj :source-selection))
       'sh03-reader-verification-invocation! (fn [& _] {:binding {:source :synthetic-current}})
       'p15-s23-stage2-sh02-descriptor-envelope-verify! (fn [& _] (swap! calls conj :envelope) :passed)
       'reader-pure-source-artifact (fn [path text context source]
                                     (swap! calls conj [:fresh path text context source]) artifact)}
      (fn []
        (is (= artifact (sh03-c3-precomputed-c2-verify! authority "pair.gravity" artifact invocation)))
        (is (= [:source-selection
                [:fresh "pair.gravity" "original" {} "synthetic-current.gravity"] :envelope] @calls))
        (doseq [changed [(assoc artifact :artifact-id "changed")
                         (assoc-in artifact [:reader-extension-invocation-records 0 :returned-seed] {})
                         (assoc-in artifact [:gravity-reader-boundary :pure-text-reconstruction-input :source-text] "changed")
                         (assoc-in artifact [:source-unit-record :reader-options :extension-policy] "changed")]]
          (reset! calls [])
          (is (= [:pure-text-fresh-reader-product-equality]
                 (:missing-fields (caught #(sh03-c3-precomputed-c2-verify!
                                             authority "pair.gravity" changed invocation)))))
          (is (not (some #{:envelope} @calls))))
        (reset! calls [])
        (is (= [:pure-text-fresh-reader-reconstruction]
               (:missing-fields (caught #(sh03-c3-precomputed-c2-verify!
                                           authority "pair.gravity" artifact nil)))))
        (is (empty? @calls))))))

(deftest changed-host-definitions-compile-with-unbound-source-ports
  ;; Compile named definitions only, without evaluating their application
  ;; bodies. Port declarations expose unresolved joins without Bootstrap load.
  (let [root "bootstrap/clojure/src/gravity/bootstrap_impl/"
        files ["components/compiler/00829-compiler-c2-reader-source-artifact.clj"
               "components/self-hosting-sh03-sh07/00812-sh03-reader-input-source-unit.clj"
               "components/self-hosting-sh03-sh07/00817-sh03-reader-adapt-evidence.clj"
               "semantic_late/146156-sh03-reader-adapt-identities.clj"]
        definitions (for [file files f (forms (str root file))
                          :when (and (seq? f) (contains? #{'defn 'defn- 'clojure.core/defn} (first f)))] f)]
    (binding [*ns* (the-ns 'gravity.reader.gravity-pure-text-test)]
      (let [unknown (distinct
                      (filter #(and (symbol? %) (nil? (namespace %))
                                    (not= '& %) (not (.startsWith (name %) "."))
                                    (not (special-symbol? %)) (nil? (resolve %)))
                              (tree-seq coll? seq definitions)))]
        (eval (cons 'declare unknown)))
      (doseq [definition definitions]
        (eval definition)
        (is (fn? (var-get (ns-resolve *ns* (second definition)))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(declare sh03-reader-path-span sh03-reader-accepted-raw-text! c2-reader-extension-invocations)

(deftest adapted-occurrences-retain-actual-seeds-and-invocation
  (let [definition (first (filter #(and (seq? %) (= 'sh03-reader-adapt-evidence! (second %)))
                                  (forms (str "bootstrap/clojure/src/gravity/bootstrap_impl/components/"
                                              "self-hosting-sh03-sh07/00817-sh03-reader-adapt-evidence.clj"))))
        bindings (second (nth definition 3))
        expression (get (into {} (map vec (partition 2 bindings))) 'extension-records)
        project (binding [*ns* (the-ns 'gravity.reader.gravity-pure-text-test)]
                  (eval (list 'fn '[sealed? resource? result form-id-map source-path source-id
                                   source-bytes source-content-id scalar-boundaries form-tree]
                              expression)))
        result (resolved-model)
        actual (get-in result [:reader-extension-invocation-records 0 :invocations 0])]
    (with-ports
      {'sh03-reader-path-span (fn [_ _ span] (assoc span :file :synthetic-source))
       'sh03-reader-accepted-raw-text! (fn [& _] "#text \"x\"")}
      (fn []
        (let [record (first (project true false result {"form/0" :form-0 "form/1" :form-1}
                                    "pair.gravity" :synthetic-source nil nil nil nil))
              projected (first (:invocations record))]
          (is (= :form-0 (:form-id projected)))
          (is (= :form-1 (:payload-form-id projected)))
          (is (= "#text \"x\"" (:raw projected)))
          (doseq [key [:input-seed :returned-seed :invocation]]
            (is (identical? (get actual key) (get projected key)))))))))

(deftest genuine-integration-definition-is-readable-and-unrun
  (let [definitions (forms "bootstrap/clojure/test/gravity/reader/sealed_pure_integration_test.clj")]
    (is (= 'gravity.reader.sealed-pure-integration-test (second (first definitions))))
    (is (= 2 (count (filter #(and (seq? %) (= 'deftest (first %))) definitions))))
    (is (not (some #{'with-redefs 'with-redefs-fn} (tree-seq coll? seq definitions)))))
  (doseq [stem ["normal-pure" "normal-unknown" "normal-effect"]]
    (let [root "bootstrap/clojure/fixtures/reader-extension-binding/"]
      (is (= (slurp (str root stem ".qst")) (slurp (str root stem ".gravity")))))))
