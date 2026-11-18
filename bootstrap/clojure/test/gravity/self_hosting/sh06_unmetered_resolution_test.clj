(ns gravity.self-hosting.sh06-unmetered-resolution-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.development-test-runner :as development]
            [gravity.bootstrap :as b]))

;; L3/C5: execute the actual current Gravity leaf on normalized requests.
;; These are semantic checks, not authenticated source/corpus or migration proof.
(defn- id [n] (format "sha256:%064x" n))
(def ^:private source
  (.getCanonicalPath (io/file "language/compiler/src/gravity/resolution.gravity")))
(def ^:private resolver-binding (delay (b/sh06-resolution-current-binding! source)))
(defn- run [function & arguments]
  (b/sh06-resolution-execute! source @resolver-binding function (vec arguments)))

(defn- request []
  {:artifact :gravity/sh06-authenticated-c4-resolution-request :schema-version 1
   :module {:namespace 'unmetered.example :package 'unmetered :profile :hosted
            :target :jvm :safety :safe :effects [] :capabilities [] :exports []
            :source-revision-id (id 1) :c4-artifact-id (id 2)}
   :definitions [] :imports [] :import-bindings [] :lexical-scopes []
   :references [] :core-bindings [] :dependency-edges []
   :macro-expansion-binding {:artifact :gravity/sh05-macro-expansion-binding
                             :schema-version 1 :source-revision-id (id 1)
                             :c4-artifact-id (id 2) :macro-result-id (id 3)
                             :authenticated-envelope-id (id 4)}
   :provenance {:actual-source-path "unmetered.gravity"}})

(defn- definition [n]
  {:name 'value :kind :var :namespace 'unmetered.example :package 'unmetered
   :binding-class :namespace :visibility :public :profile-set [:hosted]
   :target-set [:jvm] :type-ref nil :effects [] :capabilities [] :safety :safe
   :semantic-span {:ordinal n} :source-span nil
   :definition-syntax-id (id (+ 100 n)) :definition-artifact-id (id 2)})

(defn- reference [chain]
  {:syntax-id (id 9) :symbol 'value :qualifier nil :name 'value
   :position :expression :scope-chain chain :semantic-span {} :source-span nil})

(defn- scopes [n]
  (mapv (fn [i] {:scope-id (id (+ 1000 i))
                 :parent-scope-id (when (pos? i) (id (+ 999 i)))
                 :owner-syntax-id (id (+ 2000 i)) :bindings []}) (range n)))

(deftest current-resolution-shapes-have-no-project-count-quotas
  ;; Shape checks deliberately do not claim that placeholder rows are valid records.
  (doseq [[field former-limit] [[:definitions 2048] [:imports 256]
                               [:import-bindings 2048] [:lexical-scopes 4096]
                               [:references 65536] [:dependency-edges 2048]]]
    (is (true? (run 'sh06-request-shape?
                    (assoc (request) field (vec (repeat (inc former-limit) nil)))))
        (name field)))
  (doseq [[field former-limit] [[:effects 128] [:capabilities 128] [:exports 2048]]]
    (is (true? (run 'sh06-module-shape?
                    (assoc (:module (request)) field
                           (vec (repeat (inc former-limit) :declared)))))
        (name field)))
  (is (false? (run 'sh06-request-shape? (assoc (request) :references '()))))
  (is (false? (run 'sh06-module-shape? (assoc (:module (request)) :effects nil)))))

(deftest long-scope-chains-resolve-and-real-cycles-reject
  (let [scope-rows (scopes 65)
        chain (mapv :scope-id (reverse scope-rows))
        original (assoc (request) :definitions [(definition 0)]
                         :lexical-scopes scope-rows :references [(reference chain)])
        result (run 'sh06-build-resolution-template original)]
    (is (= :accepted (:status result)))
    (is (not (contains? result :bounds)))
    (is (= :passed (:status (run 'sh06-verify-resolution-template
                                 (:namespace-analysis-template result) (:digest-requests result)))))
    (doseq [changed [(assoc-in original [:lexical-scopes 0 :parent-scope-id] (peek chain))
                     (assoc-in original [:lexical-scopes 0 :parent-scope-id] (first chain))
                     (assoc-in original [:lexical-scopes 0 :parent-scope-id] (id 9999))
                     (assoc-in original [:lexical-scopes 1 :scope-id] (:scope-id (first scope-rows)))
                     (assoc-in original [:references 0 :scope-chain] [(first chain) (first chain)])
                     (assoc-in original [:references 0 :scope-chain] [(first chain)])]]
      (is (= :rejected (:status (run 'sh06-build-resolution-template changed)))))))

(deftest scope-index-preserves-first-match-and-parent-graph-semantics
  (let [scope-rows (scopes 3)
        root-id (:scope-id (first scope-rows))
        child-id (:scope-id (second scope-rows))
        leaf-id (:scope-id (nth scope-rows 2))
        missing-id (id 9999)
        chain [leaf-id child-id root-id]
        self-cycle (assoc-in scope-rows [0 :parent-scope-id] root-id)
        two-scope-cycle (-> scope-rows
                            (assoc-in [0 :parent-scope-id] child-id)
                            (assoc-in [1 :parent-scope-id] root-id))
        missing-parent (assoc-in scope-rows [1 :parent-scope-id] missing-id)
        first-local {:name 'value :kind :local :semantic-span {:ordinal 1}
                     :source-span nil :binding-syntax-id (id 401)
                     :allow-shadow? true}
        second-local {:name 'value :kind :local :semantic-span {:ordinal 2}
                      :source-span nil :binding-syntax-id (id 402)
                      :allow-shadow? true}
        first-row (assoc (first scope-rows) :bindings [first-local])
        duplicate-row (assoc (second scope-rows) :scope-id root-id
                             :parent-scope-id missing-id :bindings [])
        duplicate-scopes [first-row duplicate-row]
        duplicate-request (assoc (request) :lexical-scopes duplicate-scopes)
        duplicate-reference (reference [root-id])
        first-binding {:binding-class :lexical :scope-id root-id :name 'value
                       :binding-id :first}
        shadow-request (assoc (request) :lexical-scopes
                              [(assoc (first scope-rows) :bindings [first-local])
                               (assoc (second scope-rows) :bindings [second-local])]
                              :references [(reference [child-id root-id])])
        accepted-shadow (run 'sh06-build-resolution-template shadow-request)]
    (is (true? (run 'sh06-scope-graph-valid? scope-rows [{:scope-chain chain}])))
    (is (false? (run 'sh06-scope-graph-valid?
                     scope-rows [{:scope-chain [leaf-id root-id]}])))
    (is (false? (run 'sh06-scope-graph-valid?
                     scope-rows [{:scope-chain [leaf-id missing-id]}])))
    (is (false? (run 'sh06-scope-graph-valid? self-cycle [])))
    (is (false? (run 'sh06-scope-graph-valid? two-scope-cycle [])))
    (is (false? (run 'sh06-scope-graph-valid? missing-parent [])))
    ;; Duplicate scope IDs are rejected by request validation. These direct
    ;; semantic lookups retain first-row behavior without treating this as a
    ;; valid request.
    (is (true? (run 'sh06-scope-graph-valid? duplicate-scopes [])))
    (is (= [:first]
           (mapv :binding-id
                 (run 'sh06-reference-candidates duplicate-request
                      [first-binding] duplicate-reference))))
    (is (= :rejected
           (:status (run 'sh06-build-resolution-template duplicate-request))))
    (is (= :accepted (:status accepted-shadow)))
    (is (= [{:digest-ref 1}]
           (mapv :binding-id
                 (get-in accepted-shadow
                         [:namespace-analysis-template :resolution-table]))))))

(deftest reference-lookup-keeps-nearest-lexical-binding-first
  (let [scope-rows (scopes 2)
        root-id (:scope-id (first scope-rows))
        child-id (:scope-id (second scope-rows))
        outer-local {:name 'value :kind :local :semantic-span {:ordinal 1}
                     :source-span nil :binding-syntax-id (id 501)
                     :allow-shadow? true}
        inner-local {:name 'value :kind :local :semantic-span {:ordinal 2}
                     :source-span nil :binding-syntax-id (id 502)
                     :allow-shadow? true}
        lexical-scopes [(assoc (first scope-rows) :bindings [outer-local])
                        (assoc (second scope-rows) :bindings [inner-local])]
        reference (reference [child-id root-id])
        request-with-scopes (assoc (request) :lexical-scopes lexical-scopes)
        bindings [{:binding-class :lexical :scope-id root-id :name 'value
                   :binding-id :outer}
                  {:binding-class :lexical :scope-id child-id :name 'value
                   :binding-id :inner}]
        shadowed (run 'sh06-reference-candidates
                      request-with-scopes bindings reference)
        unshadowed (run 'sh06-reference-candidates
                        (assoc request-with-scopes :lexical-scopes
                               (assoc lexical-scopes 1
                                      (assoc (second lexical-scopes) :bindings [])))
                        bindings reference)]
    (is (= [:inner] (mapv :binding-id shadowed)))
    (is (= [:outer] (mapv :binding-id unshadowed)))))

(deftest duplicate-same-scope-name-keeps-first-binding-candidate-order
  (let [scope-id (:scope-id (first (scopes 1)))
        first-local {:name 'value :kind :local :semantic-span {:ordinal 1}
                     :source-span nil :binding-syntax-id (id 601)
                     :allow-shadow? true}
        second-local {:name 'value :kind :local :semantic-span {:ordinal 2}
                      :source-span nil :binding-syntax-id (id 602)
                      :allow-shadow? true}
        local-scopes [(assoc (first (scopes 1)) :bindings
                             [first-local second-local])]
        ref (reference [scope-id])
        request-with-scopes (assoc (request) :lexical-scopes local-scopes)
        bindings [{:binding-class :lexical :scope-id scope-id :name 'value
                   :binding-id :first}
                  {:binding-class :lexical :scope-id scope-id :name 'value
                   :binding-id :second}]
        check (run 'sh06-reference-check request-with-scopes bindings ref)]
    (is (= "C5-AMBIGUOUS" (:rule check)))
    (is (= [:first :first]
           (mapv :binding-id (:candidate-bindings check))))))

(deftest accepted-template-resolves-repeated-lexical-references
  (let [scope-count 257
        reference-count 1024
        scope-rows
        (mapv (fn [i]
                {:scope-id (id (+ 1000 i)) :parent-scope-id nil
                 :owner-syntax-id (id (+ 2000 i))
                 :bindings (if (= i (dec scope-count))
                             [{:name 'value :kind :local
                               :semantic-span {:ordinal i} :source-span nil
                               :binding-syntax-id (id (+ 10000 i))
                               :allow-shadow? true}]
                             [])})
              (range scope-count))
        last-scope-id (:scope-id (last scope-rows))
        references
        (mapv (fn [i]
                {:syntax-id (id (+ 20000 i))
                 :symbol 'value :qualifier nil :name 'value
                 :position :expression :scope-chain [last-scope-id]
                 :semantic-span {:ordinal i} :source-span nil})
              (range reference-count))
        result (run 'sh06-build-resolution-template
                    (assoc (request) :lexical-scopes scope-rows
                           :references references))
        rows (get-in result [:namespace-analysis-template :resolution-table])]
    (is (= :accepted (:status result)))
    (is (= scope-count
           (count (get-in result [:namespace-analysis-template
                                  :lexical-scope-graph]))))
    (is (= reference-count (count rows)))
    (is (every? #(= :local-lexical-binding (:resolution-order %)) rows))
    (is (every? #(= {:digest-ref 0} (:binding-id %)) rows))))

(deftest ambiguous-diagnostics-retain-all-candidates-and-semantic-rejections
  (let [original (assoc (request) :definitions (mapv definition (range 33))
                         :references [(reference [])])
        result (run 'sh06-build-resolution-template original)
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= "C5-AMBIGUOUS" (:rule diagnostic)))
    (is (= 33 (count (:candidate-bindings diagnostic))))
    (is (= (mapv :definition-syntax-id (:definitions original))
           (mapv :definition-syntax-id (:candidate-bindings diagnostic))))
    (is (not (contains? result :bounds))))
  (let [original (assoc (request) :definitions [(definition 0)] :references [(reference [])])]
    (is (= :accepted (:status (run 'sh06-build-resolution-template original))))
    (is (= :rejected (:status (run 'sh06-build-resolution-template
                                  (assoc-in original [:module :source-revision-id] (id 99)))))))
  (let [edges [{:from 'a :to 'b :kind :namespace :boundary nil :semantic-span {} :source-span nil}
               {:from 'b :to 'a :kind :namespace :boundary nil :semantic-span {} :source-span nil}]
        result (run 'sh06-build-resolution-template (assoc (request) :dependency-edges edges))]
    (is (= :rejected (:status result)))
    (is (= "C5-CYCLE" (get-in result [:diagnostics 0 :rule])))))

(deftest current-binding-retains-actual-source-without-historical-admission
  (is (= :development-unadmitted (:revision-status @resolver-binding)))
  (is (= (:source-content-hash @resolver-binding)
         (get-in @resolver-binding [:plan :source :sha256])))
  (is (not= b/sh06-resolution-expected-source-content-hash (:source-content-hash @resolver-binding))))

(deftest removing-capability-count-quotas-does-not-widen-authority
  (let [capabilities (mapv #(keyword "required" (str %)) (range 129))
        dependency {:namespace 'library :alias 'lib :kind :namespace :profile :hosted
                    :targets [:jvm] :visibility :public :refer [] :effects []
                    :capabilities capabilities :safety :safe :boundary nil
                    :semantic-span {} :source-span nil :dependency-artifact-id (id 8)
                    :foreign-record-complete? false}
        original (-> (request) (assoc :imports [dependency])
                     (assoc-in [:module :capabilities] capabilities))
        accepted (run 'sh06-build-resolution-template original)
        missing (run 'sh06-build-resolution-template
                     (assoc-in original [:module :capabilities] (pop capabilities)))
        incompatible (run 'sh06-build-resolution-template
                          (assoc-in original [:imports 0 :targets] [:wasm]))]
    (is (= :accepted (:status accepted)))
    (is (= capabilities (get-in accepted [:namespace-analysis-template :module-contract :capabilities])))
    (is (= :rejected (:status missing)))
    (is (= "C5-CAPABILITY" (get-in missing [:diagnostics 0 :rule])))
    (is (= :rejected (:status incompatible)))
    (is (= "C5-TARGET" (get-in incompatible [:diagnostics 0 :rule])))))

(deftest actual-compatibility-facade-consumes-the-quota-free-owner-result
  (let [facade (.getCanonicalPath
                 (io/file "language/compiler/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"))
        emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! facade :jvm))
        plan (b/p15-s23-stage2-compiler-artifact-plan emitter facade (slurp facade :encoding "UTF-8"))
        result (run 'sh06-build-resolution-template (request))
        execute (fn [function value]
                  (b/p15-s23-stage2-runtime-execute-function
                    {:engine :sh06-compatibility-consumer :compiler-artifact-plan? true}
                    plan function [value]))
        packaged (execute 'c5-package-resolution-result result)]
    (is (true? (execute 'c5-compatible-resolution-result? result)))
    (is (false? (execute 'c5-compatible-resolution-result? (assoc result :bounds {}))))
    (is (= :compatible (:status packaged)))
    (is (false? (:authentication-credit? packaged)))
    (is (false? (:authoritative-resolution-result? packaged)))
    (is (= result (:resolution-result packaged)))))

(deftest structural-carriers-have-no-count-quotas-and-never-call-host-objects
  (doseq [value [(vec (repeat 131073 :wide))
                 (loop [n 96 value :leaf] (if (zero? n) value (recur (dec n) [value])))
                 (apply str (repeat 131073 "a"))]]
    (is (= {:status :passed} (b/sh06-resolution-carrier-validation value))))
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! calls inc) 7)
                 (equals [_] (swap! calls inc) false)
                 (toString [] (swap! calls inc) "opaque"))]
    (doseq [value [opaque (java.util.HashMap.) (with-meta [] {:poison opaque})
                   (lazy-seq (swap! calls inc) (list :lazy))]]
      (is (not= :passed (:status (b/sh06-resolution-carrier-validation value)))))
    (is (zero? @calls))))

(deftest digest-count-quota-is-gone-but-complete-ordered-requests-remain-required
  (let [refs (mapv #(hash-map :digest-ref %) (range 8192))
        analysis {:binding-table (mapv #(hash-map :binding-id %) refs)
                  :resolution-table [] :identity-preimage {:binding-identities refs :resolutions []}
                  :incremental-invalidation-inputs {:binding-ids refs}
                  :artifact-id {:digest-ref 8192}}
        requests (conj (mapv #(hash-map :ordinal % :purpose :sh06-binding-id :preimage {:ordinal %})
                             (range 8192))
                       {:ordinal 8192 :purpose :sh06-namespace-analysis-id :preimage {}})
        resolved (development/observe-resolution-digest-interval!
                   :positive-resolve
                   #(b/sh06-resolution-resolve-digest-requests! source analysis requests))
        failure (fn [values]
                  (try (b/sh06-resolution-resolve-digest-requests! source analysis values) nil
                       (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (development/observe-resolution-digest-interval!
      :count-and-hash-shape
      (fn [] (is (= 8193 (count resolved)))
             (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) resolved))))
    (development/observe-resolution-digest-interval!
      :ordinal-negative
      #(is (= [:ordered-resolution-digest-requests]
              (:missing-fields (failure (assoc-in requests [0 :ordinal] 1))))))
    (development/observe-resolution-digest-interval!
      :count-negative
      #(is (= [:complete-resolution-digest-requests] (:missing-fields (failure (pop requests))))))
    (development/observe-resolution-digest-interval!
      :shape-negative
      #(is (= [:resolution-digest-requests] (:missing-fields (failure '())))))))

(deftest actual-current-source-artifact-retains-authentication-and-roundtrip
  (let [fixture (.getCanonicalPath
                  (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.gravity"))
        paired (.getCanonicalPath
                 (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.qst"))
        reader (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        syntax (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"))
        invocation {:reader-source-path b/sh03-reader-source-relative-path
                    :authenticated-envelope-options {:source :development-current}
                    :syntax-source-selection {:schema :gravity/sh04-development-syntax-selection-v1
                                              :source-path syntax}
                    :macro-source-mode :development-current}
        artifact (development/observe-resolution-current-artifact-interval!
                  :construction
                  #(b/compiler-c5-resolution-source-artifact
                    fixture (slurp fixture :encoding "UTF-8")
                    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []} reader invocation))
        report (development/observe-resolution-current-artifact-interval!
                :verification
                #(b/sh06-resolution-artifact-verification artifact :gravity.bootstrap/legacy invocation))
        serialized (development/observe-resolution-current-artifact-interval!
                    :serialization #(b/sh06-resolution-serialize artifact invocation))
        changed (update-in artifact [:binding-table :bindings 0 :name] (constantly 'forged))]
    (is (= (slurp fixture :encoding "UTF-8") (slurp paired :encoding "UTF-8")))
    (is (= :accepted (:status artifact)))
    (is (= :passed (:status report)))
    (is (= fixture (get-in artifact [:provenance :source-path])))
    (is (= (:source-content-hash @resolver-binding)
           (get-in artifact [:gravity-resolution-boundary :plan-binding :source-content-hash])))
    (is (not (contains? (:execution-boundary artifact) :component-transport-bounds)))
    (is (not (contains? (:execution-boundary artifact) :aggregate-artifact-bounds)))
    (is (not (contains? (get-in artifact [:gravity-resolution-boundary :raw-template-result]) :bounds)))
    (is (= 2 (get-in artifact [:gravity-resolution-boundary :authenticated-envelope-descriptor :schema-version])))
    (is (not (contains? (get-in artifact [:gravity-resolution-boundary :authenticated-envelope-descriptor]) :bounds)))
    (is (= :development-unadmitted
           (get-in artifact [:gravity-resolution-boundary :authenticated-envelope :source-rule :admission-status])))
    (is (= 1 (get-in artifact [:gravity-resolution-boundary :authenticated-envelope :schema-version])))
    (is (not (contains? (get-in artifact [:gravity-resolution-boundary :plan-binding]) :reader-invocation)))
    (is (= artifact (b/sh06-resolution-deserialize serialized invocation)))
    (is (= :failed (:status (b/sh06-resolution-artifact-verification changed :gravity.bootstrap/legacy invocation))))
    (is (= :failed (:status (b/sh06-resolution-artifact-verification artifact))))
    (let [boundary (:gravity-resolution-boundary artifact)
          envelope (:authenticated-envelope boundary)
          default-failure
          (try (b/p15-s23-stage2-sh02-descriptor-envelope-verify!
                 envelope b/sh06-resolution-envelope-stage b/sh06-resolution-sealed-artifact-kind
                 (:authenticated-envelope-descriptor boundary) fixture)
               nil (catch clojure.lang.ExceptionInfo error (ex-data error)))
          old-hash b/p15-s23-sh02-expected-source-content-hash
          mixed (assoc-in artifact [:gravity-resolution-boundary :authenticated-envelope
                                    :source-rule :source-content-hash] old-hash)
          mixed-report (b/sh06-resolution-artifact-verification mixed :gravity.bootstrap/legacy invocation)]
      (is (= "B1-INPUT" (:id default-failure)))
      (is (= :pinned-sh02-source-snapshot (:missing-fact default-failure)))
      (is (not= old-hash (get-in envelope [:source-rule :source-content-hash])))
      (is (= :failed (:status mixed-report)))
      (is (false? (get-in mixed-report [:checks :fresh-sh02-envelope-verified?]))))))
