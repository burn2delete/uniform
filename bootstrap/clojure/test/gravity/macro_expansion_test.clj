(ns gravity.macro-expansion-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.digest :as digest]
            [gravity.macro-expansion :as macro-expansion]
            [gravity.macro-expansion.engine :as engine]
            [gravity.macro-expansion.operations :as operations]))

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

(deftest expansion-digests-are-shared-between-origin-and-trace
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
        hash-inputs (atom [])
        ops {:sha256-hex (fn [value]
                           (swap! hash-inputs conj value)
                           (digest/sha256-hex value))}
        trace (atom [])
        result (macro-expansion/expand-syntax-object
                registry module call trace 0 ops)
        origin (first (:generated-origin result))
        record (first @trace)
        expected-input-hash (str "sha256:"
                                 (digest/sha256-hex (pr-str '(twice 4))))
        expected-output-hash (str "sha256:"
                                  (digest/sha256-hex (pr-str '(+ 4 4))))]
    (is (= [(pr-str '(twice 4)) (pr-str '(+ 4 4))]
           @hash-inputs))
    (is (= expected-input-hash (:input-hash origin) (:input-hash record)))
    (is (= expected-output-hash (:output-hash origin) (:output-hash record)))
    (is (= origin (first (:generated-origin record))))))

(deftest custom-origin-digests-do-not-supply-trace-digests
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
        hash-inputs (atom [])
        origin-calls (atom [])
        injected-origin [{:from :injected
                          :input-hash "sha256:forged-input"
                          :output-hash "sha256:forged-output"}]
        ops {:sha256-hex (fn [value]
                           (swap! hash-inputs conj value)
                           (digest/sha256-hex value))
             :expansion-generated-origin
             (fn [macro syntax input output]
               (swap! origin-calls conj [macro syntax input output])
               injected-origin)}
        trace (atom [])
        result (macro-expansion/expand-syntax-object
                registry module call trace 0 ops)
        record (first @trace)
        expected-input-hash (str "sha256:"
                                 (digest/sha256-hex (pr-str '(twice 4))))
        expected-output-hash (str "sha256:"
                                  (digest/sha256-hex (pr-str '(+ 4 4))))]
    (is (= 1 (count @origin-calls)))
    (is (= 2 (count @hash-inputs)))
    (is (= injected-origin (:generated-origin result)))
    (is (= injected-origin (:generated-origin record)))
    (is (= expected-input-hash (:input-hash record)))
    (is (= expected-output-hash (:output-hash record)))
    (is (not= "sha256:forged-input" (:input-hash record)))
    (is (not= "sha256:forged-output" (:output-hash record)))
    (is (= "s-call" (get-in (first @origin-calls) [1 :syntax-id])))))

(deftest var-backed-hash-interposition-does-not-poison-default-trace
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
        real-sha256 digest/sha256-hex
        hash-var #'digest/sha256-hex
        actual-inputs (atom [])
        ops (assoc (operations/default-ops)
                   :sha256-hex hash-var
                   :expansion-generated-origin
                   (fn [macro syntax input output]
                     (with-redefs [digest/sha256-hex (constantly "forged")]
                       (engine/generated-origin
                        macro syntax input output {:sha256-hex hash-var}))))
        trace (atom [])
        result
        (with-redefs [digest/sha256-hex
                      (fn [value]
                        (swap! actual-inputs conj value)
                        (real-sha256 value))]
          (engine/expand-syntax-object registry module call trace 0 ops))
        record (first @trace)]
    (is (= "sha256:forged"
           (get-in result [:generated-origin 0 :input-hash])))
    (is (= "sha256:forged"
           (get-in result [:generated-origin 0 :output-hash])))
    (is (= [(pr-str '(twice 4)) (pr-str '(+ 4 4))]
           @actual-inputs))
    (is (= (str "sha256:" (real-sha256 (pr-str '(twice 4))))
           (:input-hash record)))
    (is (= (str "sha256:" (real-sha256 (pr-str '(+ 4 4))))
           (:output-hash record)))
    (is (not= "sha256:forged" (:input-hash record)))
    (is (not= "sha256:forged" (:output-hash record)))))

(deftest custom-trace-hook-retains-injected-record-and-default-origin
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
        hash-inputs (atom [])
        trace-calls (atom [])
        injected-trace {:kind :injected-trace}
        ops {:sha256-hex (fn [value]
                           (swap! hash-inputs conj value)
                           (digest/sha256-hex value))
             :expansion-trace-record
             (fn [module macro syntax input output origin depth]
               (swap! trace-calls conj
                      [module macro syntax input output origin depth])
               injected-trace)}
        trace (atom [])
        result (macro-expansion/expand-syntax-object
                registry module call trace 0 ops)
        origin (first (:generated-origin result))]
    (is (= 2 (count @hash-inputs)))
    (is (= [injected-trace] @trace))
    (is (= 1 (count @trace-calls)))
    (is (= origin (get-in (first @trace-calls) [5 0])))
    (is (re-find #"^sha256:" (:input-hash origin)))
    (is (re-find #"^sha256:" (:output-hash origin)))))

(deftest empty-custom-origin-hook-still-rejects-provenance
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
        failure (fail-data
                 #(macro-expansion/expand-syntax-object
                   registry module call (atom []) 0
                   {:expansion-generated-origin (fn [& _] [])}))]
    (is (= "L4-PROVENANCE-MISSING" (:id failure)))))

(deftest cycle-detection-allows-siblings-and-rejects-mutual-recursion
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
        sibling-trace (atom [])
        sibling (macro-expansion/expand-syntax-object
                 registry module
                 (syntax "siblings" '(do (twice 4) (twice 5)) 3)
                 sibling-trace
                 0)
        macro-record
        (fn [identity output]
          {:identity identity
           :version "1"
           :macro-namespace 'example
           :kind :built-in
           :build-effects #{}
           :required-build-capabilities #{}
           :hygiene-policy :hygienic
           :expander (fn [_ _] output)})
        mutual-registry
        {'a (macro-record 'example/a '(b))
         'b (macro-record 'example/b '(a))}
        mutual-failure (fail-data
                        #(macro-expansion/expand-syntax-object
                          mutual-registry
                          module
                          (syntax "mutual" '(a) 4)
                          (atom [])
                          0))]
    (is (= '(do (+ 4 4) (+ 5 5)) (:form sibling)))
    (is (= 2 (count @sibling-trace)))
    (is (= "L4-EXPANSION-DEPTH" (:id mutual-failure)))
    (is (= :recursive-expansion-cycle (:reason mutual-failure)))))

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

(deftest finite-metadata-progress-is-not-a-recursive-cycle
  (let [call (fn [remaining]
               (list 'countdown (with-meta 'value {:remaining remaining})))
        macro {:identity 'example/countdown
               :version "1"
               :macro-namespace 'example
               :kind :built-in
               :build-effects #{}
               :required-build-capabilities #{}
               :hygiene-policy :hygienic
               :expander (fn [[argument] _]
                           (let [remaining (:remaining (meta argument))]
                             (if (pos? remaining)
                               (call (dec remaining))
                               :finished)))}
        trace (atom [])
        result (binding [*print-length* 1 *print-level* 1]
                 (macro-expansion/expand-syntax-object
                  {'countdown macro}
                  {:profile :hosted :metadata {}}
                  (syntax "metadata-progress" (call 3) 7)
                  trace 0))]
    (is (= :finished (:form result)))
    (is (= 4 (count @trace)))))

(deftest safety-cycle-and-provenance-rejections-remain-exact
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
        cycle (fail-data
               #(macro-expansion/expand-syntax-object
                 {'again recursive-macro}
                 {:profile :hosted :metadata {}}
                 (syntax "recursive" '(again) 5)
                 (atom [])
                 0))
        mutual-cycle (fail-data
                      #(macro-expansion/expand-syntax-object
                        {'left (assoc recursive-macro
                                      :expander (fn [_ _] '(right)))
                         'right (assoc recursive-macro
                                       :expander (fn [_ _] '(left)))}
                        {:profile :hosted :metadata {}}
                        (syntax "mutual-recursive" '(left) 5)
                        (atom []) 0))
        finite-length 40
        finite-registry
        (into {}
              (map (fn [index]
                     (let [name (symbol (str "m" index))
                           output (if (= index (dec finite-length))
                                    :finished
                                    (list (symbol (str "m" (inc index)))))]
                       [name {:identity (symbol "example" (str "m" index))
                              :version "1"
                              :macro-namespace 'example
                              :kind :built-in
                              :build-effects #{}
                              :required-build-capabilities #{}
                              :hygiene-policy :hygienic
                              :expander (fn [_ _] output)}]))
                   (range finite-length)))
        finite-trace (atom [])
        finite (macro-expansion/expand-syntax-object
                finite-registry
                {:profile :hosted :metadata {}}
                (syntax "finite" '(m0) 6)
                finite-trace
                0)
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
    (is (= "L4-EXPANSION-DEPTH" (:id cycle)))
    (is (= :recursive-expansion-cycle (:reason cycle)))
    (is (= "L4-EXPANSION-DEPTH" (:id mutual-cycle)))
    (is (= :recursive-expansion-cycle (:reason mutual-cycle)))
    (is (= :finished (:form finite)))
    (is (= finite-length (count @finite-trace)))
    (is (= (dec finite-length) (:depth (last @finite-trace))))
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
