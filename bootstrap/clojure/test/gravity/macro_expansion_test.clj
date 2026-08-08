(ns gravity.macro-expansion-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.macro-expansion :as macro-expansion]))

(defn- syntax
  [syntax-id form index]
  {:syntax-id syntax-id
   :form form
   :span {:source "macro.gravity"
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

(defn- fail-data
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(deftest macro-expansion-contract-is-narrow-and-acyclic
  (let [contract-var (get (ns-interns 'gravity.macro-expansion)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.macro-expansion (:namespace contract)))
    (is (= :hosted-stage0-macro-expansion-engine
           (:contract-boundary contract)))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.macro-expansion)))))
    (doseq [[symbol {:keys [arglists]}] (:public-api contract)
            :when arglists]
      (is (every? (set (:arglists (meta (get (ns-publics 'gravity.macro-expansion)
                                              symbol))))
                  arglists)
          symbol))
    (is (true? (get-in contract [:operation-interposition
                                 :partial-overrides?])))
    (is (true? (get-in contract [:operation-interposition
                                 :leaf-functions-add-final-operations-argument?])))
    (is (= ['clojure.core 'clojure.set 'gravity.digest]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [nonclaim [:canonical-c4-authority
                      :gravity-sh05-macro-authority
                      :source-authentication
                      :macro-proof-authority
                      :self-hosting
                      :equivalence
                      :release
                      :seed-retirement
                      :downstream-profile-type-effect-or-safety-checks]]
      (is (some #{nonclaim}
                (get-in contract [:ownership :does-not-own]))
          nonclaim))
    (is (not-any? #(#{'gravity.bootstrap 'gravity.diagnostics}
                    (ns-name (val %)))
                  (ns-aliases 'gravity.macro-expansion)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c4-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:proof-authority? contract)))))

(deftest template-expansion-preserves-stage0-shapes
  (let [expanded
        (macro-expansion/expand-template
         {'value 7
          'body ['a 'b]}
         '(do (unquote value) (splice-unquote body)))]
    (is (= '(do 7 a b) expanded))
    (is (= {:fixed ['x]
            :rest 'body}
           (macro-expansion/parse-param-list '[x & body])))
    (is (= {:fixed ['x 'y]
            :rest nil}
           (macro-expansion/parse-param-list '[x y])))
    (is (= {'x 1 'body [2 3]}
           (macro-expansion/bind-macro-arguments
            {:params {:fixed ['x] :rest 'body}}
            [1 2 3]
            {:form-index 0})))
    (is (= '(def add (fn [x] (+ x 1)))
           (macro-expansion/builtin-defn-output
            ['add '[x] '(+ x 1)]
            {:form-index 1})))
    (is (= '(if true (do :ok) nil)
           (macro-expansion/builtin-when-output
            [true :ok]
            {:form-index 2})))
    (is (= '(inc value)
           (macro-expansion/builtin-thread-first-output
            ['value '(inc)]
            {:form-index 3})))))

(deftest registry-and-expansion-preserve-trace-and-origin
  (let [module {:module 'example.core
                :profile :hosted
                :target :jvm
                :metadata {}}
        definition (syntax "s-defmacro"
                           '(defmacro twice [x]
                              (syntax-quote (+ (unquote x) (unquote x))))
                           1)
        call (syntax "s-call" '(twice 4) 2)
        registry (macro-expansion/macro-registry module [definition call])
        trace (atom [])
        result (macro-expansion/expand-syntax-object
                registry module call trace 0)]
    (is (= 'example.core/twice
           (:identity (get registry 'twice))))
    (is (= '(+ 4 4) (:form result)))
    (is (= :generated (:origin result)))
    (is (= :macro-expanded (:phase result)))
    (is (= 1 (count @trace)))
    (is (= 'example.core/twice (:macro (first @trace))))
    (is (= "s-call" (:input-syntax-id (first @trace))))
    (is (seq (:generated-origin (first @trace))))
    (is (re-find #"^sha256:" (:input-hash (first @trace))))
    (is (re-find #"^sha256:" (:output-hash (first @trace))))
    (is (= :macro-definition
           (:phase (macro-expansion/expand-syntax-object
                    registry module definition (atom []) 0))))))

(deftest guards-use-injected-bootstrap-operations
  (let [failures (atom [])
        ops {:fail! (fn [id message data]
                      (swap! failures conj [id message data])
                      (throw (ex-info message (assoc data :id id))))}
        invalid-params (fail-data
                        #(macro-expansion/parse-param-list
                          '[x 1]
                          ops))
        invalid-template (fail-data
                          #(macro-expansion/expand-template
                            {}
                            '(unquote missing)
                            ops))
        invalid-profile (fail-data
                         #(macro-expansion/assert-generated-profile!
                           {:profile :core}
                           {:identity 'example/m}
                           '(host-reflect secret)
                           {:form-index 4}
                           ops))]
    (is (= "L4-MACRO-NOT-SYNTAX" (:id invalid-params)))
    (is (= "L4-MACRO-NOT-SYNTAX" (:id invalid-template)))
    (is (= "L4-GENERATED-PROFILE" (:id invalid-profile)))
    (is (= ["L4-MACRO-NOT-SYNTAX"
            "L4-MACRO-NOT-SYNTAX"
            "L4-GENERATED-PROFILE"]
           (mapv first @failures)))))

(deftest safety-depth-and-provenance-rejections-remain-exact
  (let [span {:source "macro.gravity" :form-index 4}
        effect :build/env
        undeclared (fail-data
                    #(macro-expansion/assert-build-effects!
                      {:metadata {:build-grants #{effect}}}
                      {:identity 'example/effect
                       :build-effects #{}
                       :uses-build-effects #{effect}}
                      span))
        ungranted (fail-data
                   #(macro-expansion/assert-build-effects!
                     {:metadata {:build-grants #{}}}
                     {:identity 'example/effect
                      :build-effects #{effect}
                      :uses-build-effects #{effect}}
                     span))
        capture (fail-data
                 #(macro-expansion/assert-hygiene!
                   {:identity 'example/capture
                    :hygiene-policy :hygienic}
                   ['captured]
                   '(let [captured 1] captured)
                   span))
        generated-unsafe (fail-data
                          #(macro-expansion/assert-generated-unsafe!
                            {:safety :safe}
                            {:identity 'example/unsafe
                             :allow-unsafe? false}
                            '(unsafe operation)
                            span))
        recursive-macro {:identity 'example/again
                         :version "1"
                         :macro-namespace 'example
                         :kind :built-in
                         :build-effects #{}
                         :required-build-capabilities #{}
                         :hygiene-policy :hygienic
                         :expander (fn [_ _] '(again))}
        depth (fail-data
               #(macro-expansion/expand-syntax-object
                 {'again recursive-macro}
                 {:profile :hosted :metadata {}}
                 (syntax "recursive" '(again) 5)
                 (atom [])
                 0
                 {:max-macro-expansion-depth 1}))
        missing-origin (fail-data
                        #(macro-expansion/expand-syntax-object
                          {'missing {:identity 'example/missing
                                     :version "1"
                                     :macro-namespace 'example
                                     :kind :built-in
                                     :build-effects #{}
                                     :required-build-capabilities #{}
                                     :hygiene-policy :hygienic
                                     :omit-generated-origin? true
                                     :expander (fn [_ _] :done)}}
                          {:profile :hosted :metadata {}}
                          (syntax "missing-origin" '(missing) 6)
                          (atom [])
                          0))
        malformed-return (fail-data
                          #(macro-expansion/parse-syntax-template
                            {:identity 'example/not-syntax
                             :body ['(+ 1 2)]}
                            span))]
    (is (= "L4-BUILD-EFFECT" (:id undeclared)))
    (is (= effect (:effect undeclared)))
    (is (= "L4-BUILD-EFFECT" (:id ungranted)))
    (is (= effect (:effect ungranted)))
    (is (= "L4-HYGIENE-CAPTURE" (:id capture)))
    (is (= 'captured (:symbol capture)))
    (is (= "L4-GENERATED-UNSAFE" (:id generated-unsafe)))
    (is (= "L4-EXPANSION-DEPTH" (:id depth)))
    (is (= 1 (:limit depth)))
    (is (= "L4-PROVENANCE-MISSING" (:id missing-origin)))
    (is (= "L4-MACRO-NOT-SYNTAX" (:id malformed-return)))))

(deftest helper-interposition-and-operation-validation-are-explicit
  (let [calls (atom [])
        template (macro-expansion/expand-template
                  {}
                  '(unquote ignored)
                  {:macro-env-value (fn [env symbol]
                                      (swap! calls conj [:env env symbol])
                                      :interposed)})
        threaded (macro-expansion/builtin-thread-first-output
                  [:start :a :b]
                  {:form-index 0}
                  {:thread-first-step (fn [value step]
                                        (swap! calls conj [:thread value step])
                                        [value step])})
        invalid (fail-data
                 #(macro-expansion/parse-param-list
                   '[x]
                   {:unexpected identity}))]
    (is (= :interposed template))
    (is (= [[:start :a] :b] threaded))
    (is (= [[:env {} 'ignored]
            [:thread :start :a]
            [:thread [:start :a] :b]]
           @calls))
    (is (= "STAGE0-MACRO-EXPANSION-OPERATIONS" (:id invalid)))
    (is (= #{:unexpected} (:unexpected-operation-keys invalid)))))

(deftest artifact-projection-is-ordered-and-nonauthoritative
  (let [module {:module 'example.core
                :source-path "macro.gravity"
                :profile :hosted
                :target :jvm
                :effects #{}
                :capabilities #{}
                :safety :safe
                :metadata {}}
        records [(syntax "s-ns" '(ns example.core) 0)
                 (syntax "s-defmacro"
                         '(defmacro twice [x]
                            (syntax-quote (+ (unquote x) (unquote x))))
                         1)
                 (syntax "s-call" '(twice 4) 2)]
        artifact (macro-expansion/macro-source-artifact-from-records
                  "macro.gravity" "" records module records)]
    (is (= :gravity/stage0-macro-artifact (:kind artifact)))
    (is (= :macro-expansion (get-in artifact [:pass :name])))
    (is (= [(list '+ 4 4)]
           (:expanded-forms artifact)))
    (is (= '(+ 4 4) (first (:expanded-forms artifact))))
    (is (= 1 (count (:macro-expansion-trace artifact))))
    (is (= [:source :generated]
           (mapv :origin (:expanded-syntax-object-stream artifact))))
    (is (seq (:generated-origin-source-map artifact)))
    (is (= :hosted (get-in artifact [:module :profile])))
    (is (not (contains? artifact :canonical-c4-authority?)))
    (is (not (contains? artifact :self-hosted?)))))
