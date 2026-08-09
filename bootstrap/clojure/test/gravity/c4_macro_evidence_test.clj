(ns gravity.c4-macro-evidence-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c4-macro-evidence :as evidence]))

(def macro-entry
  {:identity 'demo/unsafe-macro
   :version "sha256:macro"
   :macro-namespace 'demo
   :params ['form]
   :source-span {:form-index 1}
   :build-effects #{:source-read}
   :required-build-capabilities #{:build/source-read}})

(def macro-artifact
  {:macro-namespace-entries [macro-entry]
   :expanded-syntax-object-stream
   [{:syntax-id :expanded
     :form '(unsafe-macro x)
     :phase :macro-expanded
     :span {:form-index 2}
     :generated-origin [{:reason :macro-expansion}]}]
   :macro-expansion-trace
   [{:macro 'demo/unsafe-macro
     :macro-version "sha256:macro"
     :call-span {:form-index 2}
     :input-syntax-id :input
     :output-hash "sha256:output"
     :hygiene-policy :explicit-capture
     :build-effects #{:source-read}
     :generated-origin [{:reason :macro-expansion}]
     :generated-spans ["generated:demo/unsafe-macro:1"]}]})

(def module
  {:module 'demo.core
   :profile :hosted
   :target :jvm
   :metadata {:build-grants #{:source-read}}})

(deftest macro-evidence-projections-preserve-c4-records-and-order
  (let [environment (evidence/c4-macro-environment
                     macro-artifact {:sha256-hex (constantly "env")})
        input (evidence/c4-expansion-input
               module
               {:syntax-object-stream [{:syntax/id :root}]}
               macro-artifact
               {:artifact-id-of (constantly "sha256:environment")
                :max-macro-expansion-depth 31})
        expanded (evidence/c4-expanded-syntax-stream
                  macro-artifact {:sha256-hex (constantly "expanded")})
        trace (evidence/c4-trace-records macro-artifact)
        captures (evidence/c4-hygiene-capture-records trace)
        build-log (evidence/c4-build-effect-log module trace)
        declarations (evidence/c4-macro-safety-declarations environment)
        origin-map (evidence/c4-generated-origin-source-map trace expanded)
        cache-key (evidence/c4-expansion-cache-key
                   input trace {:sha256-hex (constantly "cache")})
        replay (evidence/c4-trace-replay-report trace cache-key)
        safety (evidence/c4-macro-safety-report trace declarations)]
    (is (= ["sha256:env"] (:dependency-hashes environment)))
    (is (= 'demo/unsafe-macro
           (get-in environment [:macro-vars 0 :macro])))
    (is (true? (get-in environment
                       [:macro-vars 0 :safety-declaration
                        :generates-unsafe])))
    (is (= "sha256:environment" (:macro-environment input)))
    (is (= 31 (get-in input [:limits :depth])))
    (is (= "sha256:expanded" (:expanded-syntax-id (first expanded))))
    (is (= 1 (:step (first trace))))
    (is (= [{:macro 'demo/unsafe-macro
             :capture :explicit
             :policy-result :allowed}]
           (get-in trace [0 :hygiene :captures])))
    (is (= :explicit-and-allowed (:status (first captures))))
    (is (= :granted (get-in build-log [:records 0 :authorization])))
    (is (= :safe6-unsafe-island-required
           (get-in declarations [:records 0 :safe12-metadata-schema])))
    (is (= [{:step 1
             :macro 'demo/unsafe-macro
             :generated-origin [{:reason :macro-expansion}]
             :generated-spans ["generated:demo/unsafe-macro:1"]}]
           (:trace-origins origin-map)))
    (is (= "sha256:cache" (:hash cache-key)))
    (is (= :trace-replay-required (:reuse-policy cache-key)))
    (is (true? (:inputs-match? replay)))
    (is (= :required-and-recorded
           (get-in safety [:generated-unsafe 0 :safe6-metadata])))
    (is (= :pending-downstream
           (get-in safety [:profile-checks 0 :profile-check])))))

(deftest empty-and-nil-inputs-retain-legacy-projection-boundaries
  (is (= [] (:macro-vars (evidence/c4-macro-environment nil))))
  (is (= [] (evidence/c4-expanded-syntax-stream nil)))
  (is (= [] (evidence/c4-trace-records nil)))
  (is (= [] (evidence/c4-hygiene-capture-records nil)))
  (is (= [] (get-in (evidence/c4-build-effect-log nil nil) [:records])))
  (is (= [] (:records (evidence/c4-macro-safety-declarations nil))))
  (is (= [] (:trace-origins
             (evidence/c4-generated-origin-source-map nil nil))))
  (is (true? (:inputs-match?
              (evidence/c4-trace-replay-report nil {:hash nil}))))
  (is (= [] (:generated-unsafe
             (evidence/c4-macro-safety-report nil nil)))))

(deftest operation-map-is-strict-and-interposable
  (doseq [operations [nil
                      {:unknown identity}
                      {:sha256-hex :keyword-is-invokable-but-not-a-function}
                      {:artifact-id-of {}}
                      {:max-macro-expansion-depth 0}
                      {:max-macro-expansion-depth 1.5}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (evidence/c4-macro-environment macro-artifact operations))))
  (let [hash-inputs (atom [])
        artifact-inputs (atom [])]
    (is (= ["sha256:observed"]
           (:dependency-hashes
            (evidence/c4-macro-environment
             macro-artifact
             {:sha256-hex (fn [value]
                            (swap! hash-inputs conj value)
                            "observed")}))))
    (is (= 1 (count @hash-inputs)))
    (is (= "artifact:observed"
           (:macro-environment
            (evidence/c4-expansion-input
             module {} macro-artifact
             {:artifact-id-of (fn [value]
                                (swap! artifact-inputs conj value)
                                "artifact:observed")}))))
    (is (= [(:macro-namespace-entries macro-artifact)] @artifact-inputs))))

(deftest contract-is-hosted-projection-and-denies-authority
  (let [contract-var (get (ns-interns 'gravity.c4-macro-evidence)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c4-macro-evidence (:namespace contract)))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c4-macro-evidence)))))
    (is (= #{:sha256-hex :artifact-id-of :max-macro-expansion-depth}
           (get-in contract [:operation-interposition :accepted-keys])))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c4-macro-expansion-authority
                   :macro-execution
                   :c3-input-authentication
                   :hygiene-authority
                   :build-effect-authorization
                   :trace-replay-execution
                   :cache-storage
                   :cache-hit-validation
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c4-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
