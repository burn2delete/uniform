(ns gravity.bootstrap-compatibility.c4-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c4-macro-evidence :as c4-macro-evidence]
            [gravity.digest :as digest]
            [gravity.macro-expansion :as macro-expansion]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(defn- syntax
  [syntax-id form index]
  {:syntax-id syntax-id
   :form form
   :span {:source "bootstrap-macro.gravity"
          :form-index index
          :start {:line 1 :column 1}
          :end {:line 1 :column 2}}
   :namespace 'example.core
   :origin :source
   :profile :hosted
   :phase :read
   :hygiene []
   :metadata {}
   :generated-origin []})

(defn- expansion-fixture
  []
  (let [module {:module 'example.core
                :profile :hosted
                :target :jvm
                :metadata {}}
        definition (syntax "s-defmacro"
                           '(defmacro twice [x]
                              (syntax-quote (+ (unquote x) (unquote x))))
                           1)
        call (syntax "s-call" '(twice 4) 2)]
    {:module module
     :call call
     :registry (bootstrap/macro-registry module [definition call])}))

(deftest c4-macro-evidence-compatibility-wrappers-preserve-output-and-interposition
  (let [entry {:identity 'compat/macro
               :version "sha256:version"
               :macro-namespace 'compat
               :params ['form]
               :source-span {:form-index 1}
               :build-effects #{}
               :required-build-capabilities #{}}
        macro-artifact
        {:macro-namespace-entries [entry]
         :expanded-syntax-object-stream
         [{:syntax-id :expanded :form '(compat/macro x) :phase :expanded}]
         :macro-expansion-trace
         [{:macro 'compat/macro
           :macro-version "sha256:version"
           :input-syntax-id :input
           :output-hash "sha256:output"
           :hygiene-policy :hygienic
           :build-effects #{} }]}
        module {:module 'compat.core :profile :hosted :target :jvm}
        c3-artifact {:syntax-object-stream [{:syntax/id :root}]}
        environment (bootstrap/c4-macro-environment macro-artifact)
        input (bootstrap/c4-expansion-input module c3-artifact macro-artifact)
        expanded (bootstrap/c4-expanded-syntax-stream macro-artifact)
        trace (bootstrap/c4-trace-records macro-artifact)
        declarations (bootstrap/c4-macro-safety-declarations environment)
        cache-key (bootstrap/c4-expansion-cache-key input trace)]
    (doseq [[wrapper-var expected]
            [[#'bootstrap/c4-macro-environment '([macro-artifact])]
             [#'bootstrap/c4-expansion-input
              '([module c3-artifact macro-artifact])]
             [#'bootstrap/c4-expanded-syntax-stream '([macro-artifact])]
             [#'bootstrap/c4-trace-records '([macro-artifact])]
             [#'bootstrap/c4-hygiene-capture-records '([trace-records])]
             [#'bootstrap/c4-build-effect-log '([module trace-records])]
             [#'bootstrap/c4-macro-safety-declarations
              '([macro-environment])]
             [#'bootstrap/c4-generated-origin-source-map
              '([trace-records expanded-stream])]
             [#'bootstrap/c4-expansion-cache-key
              '([expansion-input trace-records])]
             [#'bootstrap/c4-trace-replay-report
              '([trace-records cache-key])]
             [#'bootstrap/c4-macro-safety-report
              '([trace-records safety-declarations])]]]
      (is (= expected (:arglists (meta wrapper-var)))))
    (is (= (c4-macro-evidence/c4-trace-records macro-artifact) trace))
    (is (= (c4-macro-evidence/c4-hygiene-capture-records trace)
           (bootstrap/c4-hygiene-capture-records trace)))
    (is (= (c4-macro-evidence/c4-build-effect-log module trace)
           (bootstrap/c4-build-effect-log module trace)))
    (is (= (c4-macro-evidence/c4-macro-safety-declarations environment)
           declarations))
    (is (= (c4-macro-evidence/c4-generated-origin-source-map trace expanded)
           (bootstrap/c4-generated-origin-source-map trace expanded)))
    (is (= (c4-macro-evidence/c4-trace-replay-report trace cache-key)
           (bootstrap/c4-trace-replay-report trace cache-key)))
    (is (= (c4-macro-evidence/c4-macro-safety-report trace declarations)
           (bootstrap/c4-macro-safety-report trace declarations)))
    (with-redefs [bootstrap/sha256-hex (constantly "interposed-hash")
                  bootstrap/c4-artifact-id (constantly "artifact:interposed")]
      (is (= ["sha256:interposed-hash"]
             (:dependency-hashes
              (bootstrap/c4-macro-environment macro-artifact))))
      (is (= "sha256:interposed-hash"
             (:expanded-syntax-id
              (first (bootstrap/c4-expanded-syntax-stream macro-artifact)))))
      (is (= "sha256:interposed-hash"
             (:hash (bootstrap/c4-expansion-cache-key input trace))))
      (is (= "artifact:interposed"
             (:macro-environment
              (bootstrap/c4-expansion-input
               module c3-artifact macro-artifact))))
      (is (= {:timeout-ms 5000}
             (:host-safeguards (bootstrap/c4-expansion-input
                                module c3-artifact macro-artifact)))))))

(deftest ordinary-bootstrap-expansion-shares-digest-computation
  (let [{:keys [module call registry]} (expansion-fixture)
        hash-inputs (atom [])
        trace (atom [])
        result
        (with-redefs [bootstrap/sha256-hex
                      (fn [value]
                        (swap! hash-inputs conj value)
                        (digest/sha256-hex value))]
          (bootstrap/expand-syntax-object registry module call trace 0))
        origin (first (:generated-origin result))
        record (first @trace)]
    (is (= [(pr-str '(twice 4)) (pr-str '(+ 4 4))]
           @hash-inputs))
    (is (= (:input-hash origin) (:input-hash record)))
    (is (= (:output-hash origin) (:output-hash record)))
    (is (= origin (first (:generated-origin record))))))

(deftest ordinary-bootstrap-custom-hooks-remain-interposable
  (let [{:keys [module call registry]} (expansion-fixture)
        hash-inputs (atom [])
        injected-origin [{:from :injected
                          :input-hash "sha256:forged-input"
                          :output-hash "sha256:forged-output"}]
        injected-trace {:kind :injected-trace}
        trace (atom [])
        result
        (with-redefs [bootstrap/sha256-hex
                      (fn [value]
                        (swap! hash-inputs conj value)
                        (digest/sha256-hex value))
                      bootstrap/expansion-generated-origin
                      (fn [& _] injected-origin)
                      bootstrap/expansion-trace-record
                      (fn [& _] injected-trace)]
          (bootstrap/expand-syntax-object registry module call trace 0))]
    (is (zero? (count @hash-inputs)))
    (is (= injected-origin (:generated-origin result)))
    (is (= [injected-trace] @trace))))

(deftest ordinary-bootstrap-trace-does-not-trust-forged-origin-digests
  (let [{:keys [module call registry]} (expansion-fixture)
        hash-inputs (atom [])
        injected-origin [{:from :injected
                          :input-hash "sha256:forged-input"
                          :output-hash "sha256:forged-output"}]
        trace (atom [])
        result
        (with-redefs [bootstrap/sha256-hex
                      (fn [value]
                        (swap! hash-inputs conj value)
                        (digest/sha256-hex value))
                      bootstrap/expansion-generated-origin
                      (fn [& _] injected-origin)]
          (bootstrap/expand-syntax-object registry module call trace 0))
        record (first @trace)]
    (is (= 2 (count @hash-inputs)))
    (is (= injected-origin (:generated-origin result)))
    (is (not= "sha256:forged-input" (:input-hash record)))
    (is (not= "sha256:forged-output" (:output-hash record)))
    (is (= (str "sha256:" (digest/sha256-hex (pr-str '(twice 4))))
           (:input-hash record)))
    (is (= (str "sha256:" (digest/sha256-hex (pr-str '(+ 4 4))))
           (:output-hash record)))))

(deftest macro-expansion-compatibility-wrappers-preserve-output-and-interposition
  (let [path (fixture "accepted/macro-expansion.gravity")
        source-text (slurp path)
        records (bootstrap/read-source-form-records path source-text)
        forms (mapv :form records)
        module (bootstrap/parse-module path forms)
        syntax (bootstrap/syntax-object-stream path records module)
        wrapped (bootstrap/macro-source-artifact-from-records
                 path source-text records)
        extracted (macro-expansion/macro-source-artifact-from-records
                   path source-text records module syntax
                   (bootstrap/macro-expansion-ops))]
    (is (= '([source-path source-text records])
           (:arglists (meta #'bootstrap/macro-source-artifact-from-records))))
    (is (= '([source-path source-text])
           (:arglists (meta #'bootstrap/macro-source-artifact))))
    (is (= '([path])
           (:arglists (meta #'bootstrap/macro-file-artifact))))
    (is (= wrapped extracted))
    (is (= :interposed
           (with-redefs [bootstrap/macro-env-value
                         (fn [_ _] :interposed)]
             (bootstrap/expand-template {} '(unquote ignored)))))
    (is (= [[:start :a] :b]
           (with-redefs [bootstrap/thread-first-step
                         (fn [value step] [value step])]
             (bootstrap/builtin-thread-first-output
              [:start :a :b] {:form-index 0}))))
    (let [interposed
          (with-redefs [bootstrap/macro-namespace-entry
                        (fn [macro] {:entry (:identity macro)})
                        bootstrap/macro-build-effect-record
                        (fn [macro] {:effect (:identity macro)})]
            (bootstrap/macro-source-artifact-from-records
             path source-text records))]
      (is (every? #(= #{:entry} (set (keys %)))
                  (:macro-namespace-entries interposed)))
      (is (every? #(= #{:effect} (set (keys %)))
                  (:macro-build-effect-records interposed))))))
