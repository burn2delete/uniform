(ns gravity.bootstrap-compatibility.module-analysis-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.module-analysis :as module-analysis]))

(def source-path "synthetic/module-analysis.gravity")
(def source-text "synthetic module analysis input")

(def forms
  ['(ns demo.compatibility
      (:profile :hosted)
      (:target :wasm)
      (:effects #{:io/write})
      (:capabilities #{:io/stdout})
      (:requires [demo.core :as core :profile :core])
      (:exports [main])
      (:safety :safe)
      (:metadata {:package demo/compatibility}))
   '(defn main [] (println (quote demo.core/value)))])

(def records
  (mapv (fn [index form]
          {:form-id (keyword (str "form-" index))
           :form form
           :span {:source source-path :form-index index}
           :metadata nil
           :reader-origin :source
           :generated-origin nil})
        (range)
        forms))

(defn diagnostic-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(defn module-analysis-operations
  []
  {:fail! bootstrap/fail!
   :source-span bootstrap/source-span
   :ns-form? bootstrap/ns-form?
   :bootstrap-target-supported? bootstrap/bootstrap-target-supported?
   :validate-ns-syntax! bootstrap/validate-ns-syntax!
   :syntax-object-stream bootstrap/syntax-object-stream
   :sha256-hex bootstrap/sha256-hex
   :known-source-profiles bootstrap/known-source-profiles
   :supported-profiles bootstrap/supported-profiles
   :supported-targets (set/union bootstrap/supported-targets
                                 bootstrap/*additional-bootstrap-targets*)
   :effect-capability bootstrap/effect-capability
   :profile-direct-imports bootstrap/profile-direct-imports
   :require-ns bootstrap/require-ns
   :parse-clause bootstrap/parse-clause
   :single-clause-value bootstrap/single-clause-value
   :clause-args bootstrap/clause-args
   :parse-options bootstrap/parse-options
   :parse-dependency-entry bootstrap/parse-dependency-entry
   :parse-dependencies bootstrap/parse-dependencies
   :top-level-definition bootstrap/top-level-definition
   :definition-table bootstrap/definition-table
   :collect-symbols bootstrap/collect-symbols
   :collect-code-symbols bootstrap/collect-code-symbols
   :infer-effects bootstrap/infer-effects
   :required-capabilities-for-effects
   bootstrap/required-capabilities-for-effects
   :profile-direct-import-allowed? bootstrap/profile-direct-import-allowed?
   :assert-unique-aliases! bootstrap/assert-unique-aliases!
   :assert-referred-names-unambiguous!
   bootstrap/assert-referred-names-unambiguous!
   :assert-qualified-symbols-resolve!
   bootstrap/assert-qualified-symbols-resolve!
   :assert-profile-boundaries! bootstrap/assert-profile-boundaries!
   :assert-namespace-effect-and-capability!
   bootstrap/assert-namespace-effect-and-capability!
   :parse-module bootstrap/parse-module
   :uses-println? bootstrap/uses-println?
   :validate-module-effects! bootstrap/validate-module-effects!
   :module-source-artifact-from-records
   bootstrap/module-source-artifact-from-records})

(deftest module-analysis-compatibility-wrappers-preserve-arglists-output-and-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/require-ns '([source-path forms])]
           [#'bootstrap/parse-clause '([source-path clause])]
           [#'bootstrap/single-clause-value
            '([source-path clause-map key required?])]
           [#'bootstrap/clause-args '([clause-map key])]
           [#'bootstrap/parse-options '([source-path entry option-items])]
           [#'bootstrap/parse-dependency-entry '([source-path kind entry])]
           [#'bootstrap/parse-dependencies '([source-path kind entries])]
           [#'bootstrap/top-level-definition '([syntax])]
           [#'bootstrap/definition-table '([syntax module])]
           [#'bootstrap/collect-symbols '([form])]
           [#'bootstrap/collect-code-symbols '([form])]
           [#'bootstrap/infer-effects '([forms])]
           [#'bootstrap/required-capabilities-for-effects '([effects])]
           [#'bootstrap/profile-direct-import-allowed?
            '([consumer-profile producer-profile])]
           [#'bootstrap/assert-unique-aliases!
            '([source-path dependencies])]
           [#'bootstrap/assert-referred-names-unambiguous!
            '([source-path dependencies])]
           [#'bootstrap/assert-qualified-symbols-resolve!
            '([source-path forms module dependencies])]
           [#'bootstrap/assert-profile-boundaries!
            '([source-path module dependencies])]
           [#'bootstrap/assert-namespace-effect-and-capability!
            '([source-path module inferred-effects])]
           [#'bootstrap/parse-module '([source-path forms])]
           [#'bootstrap/uses-println? '([form])]
           [#'bootstrap/validate-module-effects! '([module])]
           [#'bootstrap/module-source-artifact-from-records
            '([source-path source-text records])]]]
    (is (= expected (:arglists (meta wrapper-var)))))

  (binding [bootstrap/*additional-bootstrap-targets* #{:wasm}]
    (with-redefs [bootstrap/supported-targets #{:jvm :native}]
      (let [wrapper-artifact
            (bootstrap/module-source-artifact-from-records
             source-path source-text records)
            direct-artifact
            (module-analysis/with-operations
             (module-analysis-operations)
             #(module-analysis/call-entrypoint-body
               :module-source-artifact-from-records
               module-analysis/module-source-artifact-from-records
               [source-path source-text records]))]
        (is (= wrapper-artifact direct-artifact))
        (is (= :wasm
               (get-in wrapper-artifact [:module-artifact :target])))
        (is (= :wasm
               (:target (bootstrap/parse-module source-path forms)))))))

  (let [helper-calls (atom [])
        captured-parse-options bootstrap/parse-options]
    (with-redefs [bootstrap/parse-options
                  (fn [path entry option-items]
                    (swap! helper-calls conj [path entry option-items])
                    (captured-parse-options path entry option-items))]
      (bootstrap/parse-dependency-entry
       source-path :require '[demo.core :as core :profile :core]))
    (is (= [[source-path
             '[demo.core :as core :profile :core]
             '(:as core :profile :core)]]
           @helper-calls)))

  (let [operation-bindings (atom 0)
        captured-with-operations module-analysis/with-operations]
    (with-redefs [module-analysis/with-operations
                  (fn [operations thunk]
                    (swap! operation-bindings inc)
                    (captured-with-operations operations thunk))]
      (bootstrap/parse-dependency-entry
       source-path :require '[demo.core :as core :profile :core]))
    (is (= 1 @operation-bindings))))

(deftest bootstrap-owned-policy-map-redefs-reach-helpers-and-downstream-checks
  (let [captured-required-capabilities
        bootstrap/required-capabilities-for-effects
        module {:module 'demo.compatibility
                :profile :hosted
                :effects #{:io/write}
                :capabilities #{:custom/stdout}}]
    (with-redefs [bootstrap/effect-capability
                  {:io/write :custom/stdout}]
      (is (= #{:custom/stdout}
             (captured-required-capabilities #{:io/write})))
      (is (nil? (diagnostic-id
                 #(bootstrap/assert-namespace-effect-and-capability!
                   source-path module #{:io/write}))))
      (is (= "L3-CAPABILITY-MISSING"
             (diagnostic-id
              #(bootstrap/assert-namespace-effect-and-capability!
                source-path
                (assoc module :capabilities #{:io/stdout})
                #{:io/write}))))))
  (let [captured-profile-direct-import-allowed?
        bootstrap/profile-direct-import-allowed?
        module {:module 'demo.compatibility :profile :hosted}
        dependency {:module 'demo.native
                    :profile :native
                    :boundary nil}]
    (with-redefs [bootstrap/profile-direct-imports
                  {:hosted #{:core :hosted :native}}]
      (is (true? (captured-profile-direct-import-allowed?
                  :hosted :native)))
      (is (nil? (diagnostic-id
                 #(bootstrap/assert-profile-boundaries!
                   source-path module [dependency]))))
      (is (= "L3-CROSS-PROFILE"
             (diagnostic-id
              #(bootstrap/assert-profile-boundaries!
                source-path
                (assoc module :profile :native)
                [(assoc dependency :profile :hosted)])))))))
