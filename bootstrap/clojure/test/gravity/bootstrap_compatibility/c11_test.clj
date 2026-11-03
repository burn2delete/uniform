(ns gravity.bootstrap-compatibility.c11-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c11-mir :as c11]
            [gravity.c11-mir.artifact :as artifact]
            [gravity.c10-safety-analysis :as c10]
            [gravity.module-analysis :as module-analysis]
            [gravity.module-analysis.module :as module-leaf]
            [gravity.source-input :as source-input]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c11-mir-compatibility-wrappers-preserve-interposition
  (is (= '([module span outcome-by-index index family])
         (:arglists (meta #'bootstrap/c11-mir-operation))))
  (is (= '([]) (:arglists (meta #'bootstrap/c11-domain-anchor-table))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c11-mir-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c11-mir-file-artifact))))
  (let [calls (atom [])
        bindings (atom 0)
        with-operations c11/with-operations
        operation
        (with-redefs [bootstrap/c11-family-effects
                      (fn [family]
                        (swap! calls conj family)
                        #{:interposed/effect})
                      c11/with-operations
                      (fn [operations thunk]
                        (swap! bindings inc)
                        (with-operations operations thunk))]
          (bootstrap/c11-mir-operation
           {:profile :hosted}
           {:source "c11-probe.gravity" :form-index 0}
           [{:operation "op-safe" :proof "proof-safe"}]
           0 :constant))]
    (is (= #{:interposed/effect} (:effects operation)))
    (is (= [:constant] @calls))
    (is (= 1 @bindings)))
  ;; Facade/configuration oracle only: the real wrapper and operation bindings
  ;; run, but no C11 product or upstream authenticated artifact is constructed.
  (let [calls (atom [])
        sentinel (Object.)
        source-path (fixture "accepted/compiler-c11-mir-spec.gravity")
        result
        (with-redefs [bootstrap/c11-mir-diagnostic-ids ["C11-SENTINEL"]
                      bootstrap/c11-mir-rejected-designs
                      [{:diagnostic "C11-SENTINEL"}]
                      bootstrap/c11-mir-governing-document
                      "docs/c11-sentinel.md"
                      artifact/file-artifact
                      (fn [configuration path]
                        (swap! calls conj [configuration path])
                        sentinel)]
          (bootstrap/compiler-c11-mir-file-artifact source-path))
        [configuration path] (first @calls)]
    (is (identical? sentinel result))
    (is (= 1 (count @calls)))
    (is (= source-path path))
    (is (= "docs/c11-sentinel.md" (:c11-mir-governing-document configuration)))
    (is (= ["C11-SENTINEL"] (:c11-mir-diagnostic-ids configuration)))
    (is (= [{:diagnostic "C11-SENTINEL"}] (:c11-mir-rejected-designs configuration))))
  (is (= (:public-api (c11/c11-engine-contract)) c11/public-api)))

(deftest hosted-source-products-reuse-only-bootstrap-defaults
  (let [source-path "c11-source-input.gravity"
        source-text "(ns c11.source-input (:profile :hosted) (:target :jvm))"
        reader-calls (atom 0)
        parser-calls (atom 0)
        original-reader bootstrap/read-source-form-records-host-oracle
        original-parser module-leaf/parse-module]
    (with-redefs [bootstrap/read-source-form-records-host-oracle
                  (fn [path text]
                    (swap! reader-calls inc)
                    (original-reader path text))
                  module-leaf/parse-module
                  (fn [& args]
                    (swap! parser-calls inc)
                    (apply original-parser args))]
      (source-input/with-context
       (fn []
         (let [records (bootstrap/read-source-form-records source-path source-text)
               forms (mapv :form records)
               module (bootstrap/parse-module source-path forms)]
           (is (identical? records
                          (bootstrap/read-source-form-records
                           source-path source-text)))
           (is (identical? module
                          (bootstrap/parse-module source-path forms)))
           (is (not (identical? records
                               (bootstrap/read-source-form-records
                                "./c11-source-input.gravity" source-text))))
           (is (= 2 @reader-calls))
           (is (= 1 @parser-calls))))))))

(deftest c11-composition-reuses-default-products-and-keeps-expanded-products-separate
  (let [source-path "c11-composition.gravity"
        source-text "(ns c11.composition (:profile :hosted) (:target :jvm))"
        module {:module 'c11.composition
                :source-path source-path
                :profile :hosted
                :target :jvm
                :effects #{}
                :capabilities #{}
                :requires []
                :imports []
                :exports []
                :safety :safe
                :providers []
                :metadata {}}
        records [{:form '(ns c11.composition
                          (:profile :hosted) (:target :jvm))}]
        c9-artifact {:kind :gravity/stage0-c9-ownership-checker-artifact
                     :artifact-id "sha256:c9"
                     :capability-based-proof {:status :complete}}
        c10-artifact
        (c10/with-operations
          {:read-source-form-records (fn [_ _] records)
           :validate-ns-syntax! (fn [_ _] nil)
           :parse-module (fn [_ _] module)
           :compiler-c9-ownership-source-artifact
           (fn [_ _] c9-artifact)}
          #(c10/compiler-c10-safety-source-artifact source-path source-text))
        reader-calls (atom 0)
        parser-calls (atom 0)
        original-reader bootstrap/read-source-form-records-host-oracle
        original-parser module-leaf/parse-module
        original-bootstrap-reader bootstrap/read-source-form-records
        original-bootstrap-parser bootstrap/parse-module
        sh06-artifact {:kind :gravity/sh06-resolution-artifact
                       :sh05-macro-artifact
                       {:expanded-syntax-stream
                        [{:form '(ns c11.composition
                                   (:profile :hosted) (:target :jvm))
                          :syntax/id "c11-composition-syntax"
                          :origin []}]}}]
    (with-redefs-fn
      {#'bootstrap/read-source-form-records-host-oracle
       (fn [path text]
         (swap! reader-calls inc)
         (original-reader path text))
       #'module-leaf/parse-module
       (fn [& args]
         (swap! parser-calls inc)
         (apply original-parser args))
       #'bootstrap/compiler-c10-safety-source-artifact
       (fn [_ _] c10-artifact)
       #'bootstrap/compiler-c5-resolution-source-artifact
       (fn [_ _] sh06-artifact)
       #'bootstrap/sh06-resolution-artifact-verification
       (fn [_] {:status :passed})
       #'bootstrap/c6-call
       (fn [& _] {:kind :c6-sentinel})}
      (fn []
        (source-input/with-context
         (fn []
           (let [first-artifact
                 (bootstrap/compiler-c11-mir-source-artifact
                  source-path source-text)
                 second-artifact
                 (bootstrap/compiler-c11-mir-source-artifact
                  source-path source-text)
                 expanded-artifact
                 (binding [bootstrap/*compiler-c6-authenticated-resolution-input*
                           sh06-artifact]
                   (bootstrap/compiler-c6-lowering-source-artifact
                    source-path source-text))]
             (is (= :gravity/stage0-c11-mir-spec-artifact
                    (:kind first-artifact)))
             (is (= (:kind first-artifact) (:kind second-artifact)))
             (is (vector? (get-in first-artifact
                                  [:mir-diagnostic-stream :diagnostics])))
             (is (= :c6-sentinel (:kind expanded-artifact)))
             (is (= 1 @reader-calls))
             (is (= 2 @parser-calls)))))))
    (let [redefined-reader-calls (atom 0)
          redefined-parser-calls (atom 0)]
      (with-redefs-fn
        {#'bootstrap/read-source-form-records
         (fn [& args]
           (swap! redefined-reader-calls inc)
           (apply original-bootstrap-reader args))
         #'bootstrap/parse-module
         (fn [& args]
           (swap! redefined-parser-calls inc)
           (apply original-bootstrap-parser args))
         #'bootstrap/compiler-c10-safety-source-artifact
         (fn [_ _] c10-artifact)}
        (fn []
          (source-input/with-context
           (fn []
             (let [redefined-artifact
                   (bootstrap/compiler-c11-mir-source-artifact
                    source-path source-text)
                   repeated-artifact
                   (bootstrap/compiler-c11-mir-source-artifact
                    source-path source-text)]
               (is (= :gravity/stage0-c11-mir-spec-artifact
                      (:kind redefined-artifact)))
               (is (= (:kind redefined-artifact)
                      (:kind repeated-artifact)))
               (is (vector? (get-in redefined-artifact
                                    [:mir-diagnostic-stream :diagnostics]))))))))
      (is (= 2 @redefined-reader-calls))
      (is (= 2 @redefined-parser-calls)))))

(deftest hosted-source-products-fall-back-for-injected-and-redefined-callbacks
  (let [source-path "c11-source-input-custom.gravity"
        source-text "(ns c11.source-input.custom (:profile :hosted) (:target :jvm))"
        records [{:form '(ns c11.source-input.custom
                          (:profile :hosted) (:target :jvm))}]
        forms (mapv :form records)
        module {:module 'c11.source-input.custom
                :source-path source-path
                :profile :hosted
                :target :jvm
                :effects #{}
                :capabilities #{}}
        read-calls (atom 0)
        parse-calls (atom 0)
        custom-read (fn [_ _] (swap! read-calls inc) records)
        custom-parse (fn [_ _] (swap! parse-calls inc) module)]
    (source-input/with-context
     (fn []
       (source-input/records custom-read source-path source-text)
       (source-input/records custom-read source-path source-text)
       (source-input/module custom-parse source-path forms)
       (source-input/module custom-parse source-path forms)))
    (is (= 2 @read-calls))
    (is (= 2 @parse-calls))
    (let [redefined-read-calls (atom 0)
          redefined-parse-calls (atom 0)
          original-read bootstrap/read-source-form-records
          original-parse bootstrap/parse-module]
      (with-redefs [bootstrap/read-source-form-records
                    (fn [& args]
                      (swap! redefined-read-calls inc)
                      (apply original-read args))
                    bootstrap/parse-module
                    (fn [& args]
                      (swap! redefined-parse-calls inc)
                      (apply original-parse args))]
        (source-input/with-context
         (fn []
           (source-input/records bootstrap/read-source-form-records
                                 source-path source-text)
           (source-input/records bootstrap/read-source-form-records
                                 source-path source-text)
           (source-input/module bootstrap/parse-module source-path forms)
           (source-input/module bootstrap/parse-module source-path forms))))
      (is (= 2 @redefined-read-calls))
      (is (= 2 @redefined-parse-calls)))
    (let [interposed-calls (atom 0)
          original-parse @#'module-analysis/parse-module]
      (with-redefs [module-analysis/parse-module
                    (fn [& args]
                      (swap! interposed-calls inc)
                      (apply original-parse args))]
        (source-input/with-context
         (fn []
           (source-input/records bootstrap/read-source-form-records
                                 source-path source-text)
           (source-input/module bootstrap/parse-module source-path forms)
           (source-input/module bootstrap/parse-module source-path forms))))
      (is (= 2 @interposed-calls)))
    (let [reader-calls (atom 0)
          original-reader bootstrap/read-source-form-records-host-oracle
          authenticated-records [{:form '(ns c11.source-input.custom
                                          (:profile :hosted) (:target :jvm))}]]
      (with-redefs [bootstrap/read-source-form-records-host-oracle
                    (fn [path text]
                      (swap! reader-calls inc)
                      (original-reader path text))]
        (source-input/with-context
         (fn []
           (bootstrap/read-source-form-records source-path source-text)
           (binding [bootstrap/*authenticated-source-form-records*
                     {:source-path source-path
                      :source-text source-text
                      :records authenticated-records}]
             (is (identical?
                  authenticated-records
                  (bootstrap/read-source-form-records source-path source-text))))
           (bootstrap/read-source-form-records source-path source-text))))
      (is (= 2 @reader-calls)))
    (let [parser-calls (atom 0)
          original-parser module-leaf/parse-module]
      (with-redefs [module-leaf/parse-module
                    (fn [& args]
                      (swap! parser-calls inc)
                      (apply original-parser args))]
        (source-input/with-context
         (fn []
           (source-input/records bootstrap/read-source-form-records
                                 source-path source-text)
           (source-input/module bootstrap/parse-module source-path forms)
           (binding [bootstrap/*additional-bootstrap-targets* #{:extra}]
             (source-input/module bootstrap/parse-module source-path forms))
           (source-input/module bootstrap/parse-module source-path forms))))
      (is (= 3 @parser-calls)))))
