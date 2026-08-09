(ns gravity.module-analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.module-analysis :as analysis]))

(def source-path "synthetic/module.gravity")

(defn diagnostic
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))))

(defn diagnostic-id
  [thunk]
  (:id (diagnostic thunk)))

(defn source-span
  [source form-index]
  {:source source :form-index form-index})

(defn records
  [forms]
  (mapv (fn [idx form]
          {:form-id (keyword (str "form-" idx))
           :form form
           :span {:source source-path :form-index idx}
           :metadata nil
           :reader-origin :source
           :generated-origin nil})
        (range)
        forms))

(defn syntax-stream
  [_source-path form-records _module]
  (mapv (fn [{:keys [form span] :as record}]
          (assoc record :form form :span span))
        form-records))

(defn artifact-operations
  []
  {:validate-ns-syntax! (fn [_ _] nil)
   :syntax-object-stream syntax-stream})

(def valid-ns
  '(ns demo.main
     (:profile :hosted)
     (:target :jvm)
     (:effects #{:io/write})
     (:capabilities #{:io/stdout})
     (:requires [demo.core :as core :profile :core])
     (:imports [host.api :as host])
     (:exports [main])
     (:safety :safe)
     (:metadata {:package demo/app})))

(def valid-forms
  [valid-ns
   '(defconst version 1)
   '(defn private-helper [] (quote demo.core/value))
   '(defn main [] (println "hello"))])

(defn ns-replaced
  [& replacements]
  (apply list (apply assoc (vec valid-ns) replacements)))

(deftest public-contract-is-private-and-acyclic
  (let [contract-var (get (ns-interns 'gravity.module-analysis)
                          'namespace-contract)
        contract (var-get contract-var)
        public-api analysis/public-api]
    (is (true? (:private (meta contract-var))))
    (is (= (set (keys public-api))
           (set (keys (ns-publics 'gravity.module-analysis)))))
    (is (= public-api
           (:public-api (analysis/module-analysis-engine-contract))))
    (doseq [[name {:keys [arglists]}] public-api
            :when arglists]
      (is (= arglists
             (:arglists (meta (get (ns-publics 'gravity.module-analysis)
                                   name))))
          (str name " arglists must match Var metadata")))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= :keyword-to-keyword-map
           (get-in contract
                   [:operation-interposition :scalar-values-must-satisfy
                    :effect-capability])))
    (is (= :keyword-to-non-empty-keyword-set-map
           (get-in contract
                   [:operation-interposition :scalar-values-must-satisfy
                    :profile-direct-imports])))
    (is (every? #(contains?
                  (get-in contract
                          [:operation-interposition :accepted-keys])
                  %)
                [:effect-capability :profile-direct-imports]))
    (is (false? (:canonical-l3-authority? contract)))
    (is (false? (:source-reading? contract)))
    (is (false? (:macro-execution? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.diagnostics)))))

(deftest strict-operation-and-scalar-shapes
  (doseq [operations [nil
                      {:unknown identity}
                      {:fail! :not-a-function}
                      {:known-source-profiles #{:hosted "bad"}}
                      {:supported-profiles []}
                      {:supported-targets #{}}
                      {:effect-capability []}
                      {:effect-capability {"io/write" :io/stdout}}
                      {:effect-capability {:io/write "io/stdout"}}
                      {:profile-direct-imports []}
                      {:profile-direct-imports {'hosted #{:core}}}
                      {:profile-direct-imports {:hosted #{}}}
                      {:profile-direct-imports {:hosted #{"core"}}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (analysis/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (analysis/with-operations {} :not-a-function)))
  (is (thrown? clojure.lang.ExceptionInfo
               (analysis/call-entrypoint-body :unknown identity [])))
  (is (thrown? clojure.lang.ExceptionInfo
               (analysis/call-entrypoint-body :parse-module :not-a-function [])))
  (is (thrown? clojure.lang.ExceptionInfo
               (analysis/call-entrypoint-body :parse-module identity :not-seq))))

(deftest clauses-and-dependencies-preserve-shapes-and-l3-diagnostics
  (is (= [:profile [:hosted]]
         (analysis/parse-clause source-path '(:profile :hosted))))
  (is (= {:as 'core :profile :core}
         (analysis/parse-options source-path
                                 '[demo.core :as core :profile :core]
                                 '(:as core :profile :core))))
  (is (= {:kind :require
          :module 'demo.core
          :alias 'core
          :refer []
          :profile :core
          :boundary nil
          :edge nil
          :facade nil
          :evidence #{}
          :artifact nil
          :artifact-schema nil
          :runtime nil
          :memory nil
          :generated? false
          :matrix-override nil
          :producer-effects #{}
          :producer-capabilities #{}
          :safety-evidence #{}
          :provider nil
          :effects #{}
          :capabilities #{}
          :visibility :public}
         (analysis/parse-dependency-entry
          source-path :require '[demo.core :as core :profile :core])))
  (doseq [[thunk expected]
          [[#(analysis/require-ns source-path []) "L3-NS-MISSING"]
           [#(analysis/parse-clause source-path :not-a-clause) "L3-NS-CLAUSE"]
           [#(analysis/parse-options source-path '[demo.core :as] '(:as))
            "L3-UNKNOWN-ALIAS"]
           [#(analysis/parse-options source-path '[demo.core as core]
                                      '(as core))
            "L3-UNKNOWN-ALIAS"]
           [#(analysis/parse-dependency-entry source-path :require :bad)
            "L3-UNKNOWN-ALIAS"]
           [#(analysis/parse-dependency-entry source-path :require
                                              '[demo.core :refer :all])
            "L3-AMBIGUOUS-NAME"]
           [#(analysis/parse-dependency-entry source-path :require
                                              '[demo.core :refer [private-value]])
            "L3-PRIVATE-IMPORT"]]]
    (testing expected
      (is (= expected (:id (diagnostic thunk)))))))

(deftest parse-module-profile-and-target-checks
  (let [module (analysis/parse-module source-path valid-forms)]
    (is (= {:module 'demo.main
            :source-path source-path
            :profile :hosted
            :target :jvm
            :effects #{:io/write}
            :capabilities #{:io/stdout}
            :requires [{:kind :require :module 'demo.core :alias 'core
                        :refer [] :profile :core :boundary nil :edge nil
                        :facade nil :evidence #{} :artifact nil
                        :artifact-schema nil :runtime nil :memory nil
                        :generated? false :matrix-override nil
                        :producer-effects #{} :producer-capabilities #{}
                        :safety-evidence #{} :provider nil :effects #{}
                        :capabilities #{} :visibility :public}]
            :imports [{:kind :import :module 'host.api :alias 'host
                       :refer [] :profile nil :boundary nil :edge nil
                       :facade nil :evidence #{} :artifact nil
                       :artifact-schema nil :runtime nil :memory nil
                       :generated? false :matrix-override nil
                       :producer-effects #{} :producer-capabilities #{}
                       :safety-evidence #{} :provider nil :effects #{}
                       :capabilities #{} :visibility :public}]
            :exports '[main]
            :safety :safe
            :providers []
            :metadata {:package 'demo/app}
            :doc nil
            :forms (vec (rest valid-forms))}
           module)))
  (doseq [[forms expected]
          [[(vector (ns-replaced 2 '(:profile :hosted)
                                 3 '(:profile :native)))
            "L3-PROFILE-MULTIPLE"]
           [(vector (ns-replaced 2 '(:profiles #{:hosted :native})))
            "L3-NS-MISSING"]
           [(vector (ns-replaced 1 :bad)) "L3-NS-MISSING"]
           [(vector (ns-replaced 3 '(:target :wasm)))
            "B1-TARGET-UNSUPPORTED"]
           [(vector (ns-replaced 2 '(:profile :unknown)))
            "P1-PROFILE-UNSUPPORTED"]]]
    (testing expected
      (is (= expected
             (:id (diagnostic #(analysis/parse-module source-path forms)))))))
  (let [supported-targets #{:jvm :wasm}
        target-supported? #(contains? supported-targets %)
        accepted
        (analysis/with-operations
         {:supported-targets supported-targets
          :bootstrap-target-supported? target-supported?}
         (fn []
           (analysis/parse-module
            source-path
            [(ns-replaced 3 '(:target :wasm))])))
        rejected
        (analysis/with-operations
         {:supported-targets supported-targets
          :bootstrap-target-supported? target-supported?}
         (fn []
           (diagnostic
            (fn []
              (analysis/parse-module
               source-path
               [(ns-replaced 3 '(:target :llvm))])))))]
    (is (= :wasm (:target accepted)))
    (is (= "B1-TARGET-UNSUPPORTED" (:id rejected)))
    (is (= supported-targets (:supported rejected)))))

(deftest quoted-symbols-do-not-trigger-alias-resolution
  (let [module (analysis/parse-module source-path valid-forms)
        quoted '(do (quote missing.alias/value))]
    (is (= ['do 'quote]
           (analysis/collect-code-symbols quoted)))
    (is (nil? (diagnostic #(analysis/assert-qualified-symbols-resolve!
                            source-path [quoted] module (:requires module))))))
  (let [module (analysis/parse-module source-path valid-forms)
        executable '(do missing/value)]
    (is (= "L3-UNKNOWN-ALIAS"
           (:id (diagnostic #(analysis/assert-qualified-symbols-resolve!
                              source-path [executable] module (:requires module))))))))

(deftest effect-capability-and-profile-boundary-checks
  (let [module (analysis/parse-module source-path valid-forms)]
    (is (nil? (diagnostic
               #(analysis/assert-namespace-effect-and-capability!
                 source-path module #{:io/write}))))
    (is (= "L3-EFFECT-WIDEN"
           (diagnostic-id
            #(analysis/assert-namespace-effect-and-capability!
              source-path module #{:network/listen}))))
    (is (= "L3-CAPABILITY-MISSING"
           (diagnostic-id
            #(analysis/assert-namespace-effect-and-capability!
              source-path (assoc module :capabilities #{}) #{:io/write})))))
  (let [module {:module 'demo.main :profile :hosted}
        dependency {:module 'demo.kernel :profile :kernel :boundary nil}]
    (is (= "L3-CROSS-PROFILE"
           (diagnostic-id
            #(analysis/assert-profile-boundaries!
              source-path module [dependency]))))
    (is (nil? (diagnostic
               #(analysis/assert-profile-boundaries!
                 source-path module
                 [(assoc dependency :boundary :typed-schema)])))))
  (let [module {:source-path source-path :forms ['(println "x")]
                :effects #{} :capabilities #{}}]
    (is (= "L6-EFFECT-UNDECLARED"
           (diagnostic-id #(analysis/validate-module-effects! module))))
    (is (= "L3-CAPABILITY-MISSING"
           (diagnostic-id
            #(analysis/validate-module-effects!
              (assoc module :effects #{:io/write})))))))

(deftest policy-map-scalar-interposition-reaches-helpers-and-downstream-checks
  (let [calls (atom 0)
        captured-required-capabilities
        analysis/required-capabilities-for-effects
        module {:module 'demo.main
                :profile :hosted
                :effects #{:io/write}
                :capabilities #{:custom/stdout}}]
    (analysis/with-operations
     {:effect-capability {:io/write :custom/stdout}
      :required-capabilities-for-effects
      (fn [effects]
        (swap! calls inc)
        (captured-required-capabilities effects))}
     (fn []
       (is (= #{:custom/stdout}
              (analysis/required-capabilities-for-effects #{:io/write})))
       (is (nil? (diagnostic
                  #(analysis/assert-namespace-effect-and-capability!
                    source-path module #{:io/write}))))
       (is (= "L3-CAPABILITY-MISSING"
              (diagnostic-id
               #(analysis/assert-namespace-effect-and-capability!
                 source-path
                 (assoc module :capabilities #{:io/stdout})
                 #{:io/write}))))))
    (is (= 3 @calls)))
  (let [calls (atom 0)
        captured-profile-direct-import-allowed?
        analysis/profile-direct-import-allowed?
        module {:module 'demo.main :profile :hosted}
        dependency {:module 'demo.native
                    :profile :native
                    :boundary nil}]
    (analysis/with-operations
     {:profile-direct-imports {:hosted #{:core :hosted :native}}
      :profile-direct-import-allowed?
      (fn [consumer-profile producer-profile]
        (swap! calls inc)
        (captured-profile-direct-import-allowed?
         consumer-profile producer-profile))}
     (fn []
       (is (true? (analysis/profile-direct-import-allowed?
                   :hosted :native)))
       (is (nil? (diagnostic
                  #(analysis/assert-profile-boundaries!
                    source-path module [dependency]))))
       (is (= "L3-CROSS-PROFILE"
              (diagnostic-id
               #(analysis/assert-profile-boundaries!
                 source-path
                 (assoc module :profile :native)
                 [(assoc dependency :profile :hosted)]))))))
    (is (= 3 @calls))))

(deftest exact-module-artifact-preserves-identity-and-provenance
  (let [forms (conj (vec (butlast valid-forms))
                    '(defn main [] (println "hello")))
        source-text "stable-source"
        artifact
        (analysis/with-operations
         (artifact-operations)
         #(analysis/module-source-artifact-from-records
           source-path source-text (records forms)))]
    (is (= :gravity/stage0-module-artifact (:kind artifact)))
    (is (= {:name :namespace-analyzer
            :input :syntax-object-stream
            :output :module-artifact
            :requires [:reader]
            :preserves [:source-spans :profile :target :effects :capabilities]
            :rejects ["L3-NS-MISSING" "L3-PROFILE-MULTIPLE" "L3-UNKNOWN-ALIAS"
                      "L3-AMBIGUOUS-NAME" "L3-PRIVATE-IMPORT" "L3-CROSS-PROFILE"
                      "L3-EFFECT-WIDEN" "L3-CAPABILITY-MISSING"]}
           (:pass artifact)))
    (is (= [{:name 'demo.main :package 'demo/app :profile :hosted :target :jvm
             :source-path source-path :safety :safe :metadata {:package 'demo/app}}]
           (:namespace-table artifact)))
    (is (= [{:alias 'core :module 'demo.core :kind :require :profile :core}
            {:alias 'host :module 'host.api :kind :import :profile nil}]
           (:alias-table artifact)))
    (is (= {:module 'demo.main
            :dependencies [{:kind :require :module 'demo.core :alias 'core
                            :profile :core :boundary nil :effects #{}
                            :capabilities #{}
                            }
                           {:kind :import :module 'host.api :alias 'host
                            :profile nil :boundary nil :effects #{}
                            :capabilities #{}}]
            :acyclic true}
           (:module-dependency-graph artifact)))
    (is (= #{:io/write} (get-in artifact [:namespace-effect-summary :inferred])))
    (is (= #{:io/stdout}
           (get-in artifact [:namespace-capability-summary :required])))
    (is (= [{:module 'demo.core :from-profile :hosted :to-profile :core
             :boundary :pure-core}]
           (:profile-boundary-records artifact)))
    (is (= {:module 'demo.main :package 'demo/app :profile :hosted :target :jvm
            :exports '[main]
            :requires [{:module 'demo.core :profile :core :effects #{}}]
            :imports [{:module 'host.api :profile nil :effects #{} :boundary nil}]
            :effects #{:io/write} :capabilities #{:io/stdout} :safety :safe
            :source-hash (str "sha256:"
                              "87e1ad836e61c9b0ae4da002550e8ed5ae1605fafbd541abb8905f1b9a7090dd")
            :definitions (get-in artifact [:module-artifact :definitions])}
           (assoc (:module-artifact artifact)
                  :source-hash
                  (get-in artifact [:module-artifact :source-hash]))))
    (is (= [] (:diagnostics artifact)))
    (is (every? #(contains? % :source-span) (:definitions artifact)))
    (is (= (filterv #(= :public (:visibility %)) (:definitions artifact))
           (filterv #(= :public (:visibility %))
                    (get-in artifact [:public-api-manifest :exports]))))))

(deftest nested-and-captured-original-interposition-is-one-shot
  (let [calls (atom [])
        original-parse-options @#'analysis/parse-options]
    (analysis/with-operations
     {:parse-options
      (fn [source entry items]
        (swap! calls conj [:parse-options entry items])
        (original-parse-options source entry items))}
     #(analysis/call-entrypoint-body
       :parse-dependency-entry
       @#'analysis/parse-dependency-entry
       [source-path :require '[demo.core :as core]]))
    (is (= 1 (count @calls)))
    (is (= [:parse-options '[demo.core :as core] '(:as core)]
           (first @calls))))
  (let [calls (atom 0)
        original @#'analysis/collect-symbols]
    (analysis/with-operations
     {:collect-symbols
      (fn [form]
        (swap! calls inc)
        (original form))}
     #(analysis/call-entrypoint-body
       :collect-symbols original ['((a b) c)]))
    (is (= 2 @calls)))
  (let [calls (atom 0)
        original-parse-clause @#'analysis/parse-clause
        native-ns (ns-replaced 2 '(:profile :native))]
    (analysis/with-operations
     {:known-source-profiles #{:native}
      :supported-targets #{:jvm}}
     #(analysis/with-operations
       {:parse-clause
        (fn [source clause]
          (swap! calls inc)
          (original-parse-clause source clause))}
       (fn []
         (let [module (analysis/parse-module source-path [native-ns])]
           (is (= :native (:profile module)))
           (is (= :jvm (:target module)))))))
    (is (pos? @calls))))
