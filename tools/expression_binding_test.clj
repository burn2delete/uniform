(ns expression-binding-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.digest :as digest]
            [gravity.c6-c10.source-contract :as contract]
            [gravity.source-interface-header :as header]))

(binding [*ns* *ns*] (load-file "tools/c11_relative_policy_test.clj"))
(def controls (find-ns 'c11-relative-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn evaluate [file predicate] ((ns-resolve controls 'evaluate) file predicate))
(defn named? [names] ((ns-resolve controls 'named?) names))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] ((ns-resolve controls 'diagnostic) f))
(defn redefine [functions f]
  (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) functions)) f))
(binding [*ns* owner]
  (alias 'source-interface-header (ns-name (:ns (meta #'header/pairs!))))
  (intern owner 'sha256-hex digest/sha256-hex)
  (intern owner 'sha256-bytes-hex digest/sha256-bytes-hex)
  (intern owner 'p15-s23-stage2-plan-emitter-fail!
          (fn [id _ _ data] (throw (ex-info "Synthetic diagnostic transport" (assoc data :id id)))))
  (eval '(def ^:dynamic *p15-s23-sh07-b51-governed-source-session* nil))
  (eval '(def ^:dynamic *p15-s23-stage2-compiler-artifact-binding* nil))
  (eval '(declare p15-s23-stage2-compiler-artifact-source-path
                  p15-s23-stage2-compiler-artifact-plan p15-s23-sh07-b51-compatibility-source-record
                  p15-s23-sh07-b51-active-module-ordinal p15-s23-sh07-b51-governed-source-event!
                  p15-s23-stage2-compiler-artifact-augmented-emitter p15-s23-sh07-compiler-source-authority
                  p15-s23-stage2-assemble-plan-products c4-artifact-id)))
(evaluate "../backends/00640-c-backend-fail.clj" (named? '#{c-backend-canonical-value}))
(evaluate "00576-p15-s23-stage2-seed-compile-expr.clj" #(and (seq? %) (= 'def (first %))))
(evaluate "00645-p15-s23-stage2-runtime-artifact-source-relative-path.clj"
          (named? '#{p15-s23-stage2-compiler-expected-source-content-hash p15-s23-stage2-compiler-expected-source-byte-count}))
(evaluate "00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj"
          (named? '#{p15-s23-stage2-expression-source-contract p15-s23-stage2-compiler-artifact-expected-artifact-hash
                     p15-s23-stage2-compiler-artifact-record-keys p15-s23-stage2-native-keyword-pairs
                     p15-s23-stage2-compiler-artifact-record-authentic? p15-s23-stage2-plan-compiler-view
                     p15-s23-stage2-compiler-artifact-record-matches-plan? p15-s23-stage2-compiler-artifact-semantic-input}))
(let [forms ((ns-resolve controls 'forms) "00581-p15-s23-stage2-compiler-artifact-binding.clj")
      selected (take-while #(not= 'p15-s23-stage2-compiler-artifact-invoke (second %)) forms)]
  (binding [*ns* owner] (doseq [form selected] (eval form))))
(evaluate "00582-p15-s23-stage2-construct-emitted-core-plan.clj"
          (named? '#{p15-s23-stage2-runtime-artifact-expected-source-content-hash p15-s23-stage2-construct-emitted-core-plan}))
(defn hash-value [value] (str "sha256:" (digest/sha256-hex (pr-str (call 'c-backend-canonical-value value)))))
(def text "fixture\n")
(def source-hash (str "sha256:" (digest/sha256-hex text)))
(def functions {'p15-s23-compile-expr {:arity 4 :params ['emitter 'module 'locals 'form] :body :synthetic}})
(def plan-base {:source {:sha256 source-hash} :module {:module 'synthetic :profile :hosted :target :jvm}
                :functions functions :instruction-summary {:literal 1} :effect-summary {}
                :compiler {:synthetic-plan-label true}})
(def plan (assoc plan-base :plan-id (hash-value plan-base)))
(def emitter {:synthetic-rule true})
(def emitter-rule {:emitter emitter :source-rule-hash (hash-value emitter) :rule-record {:status :complete}})
(def fixture-contract
  (merge contract/current-expression-program
         {:source-byte-count 8 :source-content-hash source-hash
          :semantic-hash (hash-value (call 'p15-s23-stage2-compiler-artifact-semantic-input plan))
          :artifact-hash (hash-value {:source-content-hash source-hash
                                     :semantic-hash (hash-value (call 'p15-s23-stage2-compiler-artifact-semantic-input plan))})
          :functions-semantic-hash (hash-value functions) :function-count 1
          :function-names-hash (hash-value (vec (keys functions)))
          :instruction-summary (:instruction-summary plan)
          :required-functions (into {} (map (fn [[name definition]] [name (select-keys definition [:arity :params])]) functions))
          :required-export-semantic-hashes (into {} (map (fn [[name definition]] [name (hash-value definition)]) functions))
          :declared-plan-compiler-labels (:compiler plan)
          :plan-emitter (assoc (:plan-emitter contract/current-expression-program) :source-rule-hash (hash-value emitter))}))
(def events (atom []))
(defn with-fixture [options f]
  (let [directory (java.nio.file.Files/createTempDirectory "expression-control-" (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve directory "emitter.gravity")]
    (reset! events [])
    (try
      (java.nio.file.Files/write path (.getBytes text "UTF-8") (make-array java.nio.file.OpenOption 0))
      (when-let [before (:before options)] (before path))
      (with-redefs [contract/current-expression-program fixture-contract]
        (redefine
         {'p15-s23-stage2-compiler-artifact-source-path (constantly (str path))
          'c-backend-stage2-plan-emitter-source-rule!
          (fn [& _] (swap! events conj :emitter-owner) (or (:rule options) emitter-rule))
          'p15-s23-sh07-b51-compatibility-source-record
          (fn [source-path]
            (swap! events conj :source-reader)
            (let [bytes (java.nio.file.Files/readAllBytes (.toPath (io/file source-path)))]
              {:path source-path :bytes bytes :text (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)}))
          'p15-s23-stage2-compiler-artifact-plan
          (fn [& _]
            (swap! events conj :compiler)
            (when-let [during (:during-compile options)] (during path))
            (or (:plan options) plan))}
         #(f path)))
      (finally (doseq [file (reverse (file-seq (.toFile directory)))] (io/delete-file file true))))))
(defn bind-current [] (call 'p15-s23-stage2-compiler-artifact-binding! emitter "request.gravity" :jvm 2))

(deftest explicit-binding-checks-real-source-and-complete-program-without-execution-claim
  (with-fixture {}
    (fn [path]
      (let [result (bind-current)]
        (is (= 2 (:source-contract-version result)))
        (is (= (str path) (:source-path result)))
        (is (= source-hash (:source-content-hash result)))
        (is (= (:functions-semantic-hash fixture-contract) (:functions-semantic-hash result)))
        (is (identical? plan (:plan result)))
        (is (false? (:invoked? result)))
        (is (false? (:self-hosted? result)))
        (is (= [:emitter-owner :source-reader :compiler :source-reader] @events))))))

(deftest version-host-context-and-emitter-refusals-precede-source-read
  (doseq [[version target source fact]
          [[3 :jvm "request.gravity" :expression-lowering-source-contract-version]
           [(int 2) :jvm "request.gravity" :expression-lowering-source-contract-version]
           [2 :native "request.gravity" :current-expression-selected-host-source-context]
           [2 :jvm {} :current-expression-request-source-context]]]
    (with-fixture {}
      (fn [_]
        (is (= fact (:missing-fact (diagnostic #(call 'p15-s23-stage2-compiler-artifact-binding! emitter source target version)))))
        (is (empty? @events)))))
  (with-fixture {}
    (fn [_]
      (with-bindings {(ns-resolve owner '*p15-s23-sh07-b51-governed-source-session*) :synthetic-session}
        (is (= :current-expression-selected-host-source-context (:missing-fact (diagnostic bind-current)))))
      (is (empty? @events))))
  (with-fixture {:rule (assoc emitter-rule :source-rule-hash "changed")}
    (fn [_]
      (is (= :current-expression-plan-emitter-join (:missing-fact (diagnostic bind-current))))
      (is (= [:emitter-owner] @events)))))

(deftest pinned-source-and-compilation-bookends-refuse-drift
  (with-fixture {:before #(java.nio.file.Files/write % (.getBytes "changed\n" "UTF-8") (make-array java.nio.file.OpenOption 0))}
    (fn [_]
      (is (= :stable-current-expression-source-snapshot (:missing-fact (diagnostic bind-current))))
      (is (= [:emitter-owner :source-reader] @events))))
  (with-fixture {:during-compile #(java.nio.file.Files/write % (.getBytes "changed\n" "UTF-8") (make-array java.nio.file.OpenOption 0))}
    (fn [_] (is (= :stable-current-expression-source-snapshot (:missing-fact (diagnostic bind-current))))))
  (with-fixture {:before (fn [path]
                          (let [target (.resolveSibling path "target.gravity")]
                            (java.nio.file.Files/move path target (make-array java.nio.file.CopyOption 0))
                            (java.nio.file.Files/createSymbolicLink path target (make-array java.nio.file.attribute.FileAttribute 0))))}
    (fn [_]
      (is (= :current-expression-source-regular-file (:missing-fact (diagnostic bind-current))))
      (is (= [:emitter-owner] @events)))))

(deftest complete-program-identities-and-plan-id-are-independently-checked
  (with-fixture {}
    (fn [_]
      (doseq [change [#(assoc-in % [:source :sha256] "changed")
                      #(assoc-in % [:functions 'p15-s23-compile-expr :body] :changed)
                      #(assoc % :instruction-summary {:literal 2})
                      #(assoc % :compiler {:changed true})]]
        (is (= :stage2-current-expression-complete-program-identities
               (:missing-fact (diagnostic #(call 'p15-s23-stage2-validate-current-expression-program! "fixture.gravity" (change plan)))))))
      (is (= :current-expression-recomputable-plan-id
             (:missing-fact (diagnostic #(call 'p15-s23-stage2-validate-current-expression-program!
                                              "fixture.gravity" (assoc plan :plan-id "changed")))))))))

(defn record []
  (merge (zipmap @(ns-resolve owner 'p15-s23-stage2-compiler-artifact-record-keys) (repeat true))
         {:artifact :gravity/p15-s23-stage2-expression-lowering-binding :source-content-hash source-hash
          :semantic-hash (:semantic-hash fixture-contract) :artifact-hash (:artifact-hash fixture-contract)
          :functions (:required-functions fixture-contract) :self-hosted? false :source-contract-version 2
          :functions-semantic-hash (:functions-semantic-hash fixture-contract) :function-count 1
          :plan-assembly-function @(ns-resolve owner 'p15-s23-stage2-compiler-artifact-plan-assembly-function)
          :plan-assembly-artifact-hash (:artifact-hash fixture-contract) :plan-assembly-source-content-hash source-hash
          :plan-assembly-semantic-hash (:semantic-hash fixture-contract)}))
(defn matching-plan [record]
  {:compiler (merge (select-keys record [:plan-assembly-function :plan-assembly-artifact-hash
                                        :plan-assembly-source-content-hash :plan-assembly-semantic-hash
                                        :plan-assembly-invoked? :plan-assembly-generic-bridge-residual?])
                    {:expression-lowering-source-contract-version 2 :expression-lowering-function-count 1
                     :expression-lowering-functions-semantic-hash (:functions-semantic-hash record)
                     :expression-lowering-artifact-hash (:artifact-hash record)
                     :expression-lowering-source-content-hash (:source-content-hash record)
                     :expression-lowering-semantic-hash (:semantic-hash record)})})

(deftest native-metadata-predicates-do-not-admit-plan-payload-or-call-foreign-code
  (with-fixture {}
    (fn [_]
      (let [callbacks (atom 0)
            foreign (proxy [Object clojure.lang.IMeta] []
                      (meta [] (swap! callbacks inc) {})
                      (hashCode [] (swap! callbacks inc) 0)
                      (equals [_] (swap! callbacks inc) true)
                      (toString [] (swap! callbacks inc) "foreign"))
            r (record) p (matching-plan r)]
        (is (true? (call 'p15-s23-stage2-compiler-artifact-record-authentic? r 2)))
        (is (true? (call 'p15-s23-stage2-compiler-artifact-record-matches-plan? r (assoc p :functions foreign) 2)))
        (doseq [bad [foreign (with-meta r {:caller true}) (assoc r :foreign foreign)
                     (clojure.lang.PersistentArrayMap. (object-array [foreign nil]))]]
          (is (not (call 'p15-s23-stage2-compiler-artifact-record-authentic? bad 2))))
        (is (not (call 'p15-s23-stage2-compiler-artifact-record-matches-plan? r (assoc-in p [:compiler :foreign] foreign) 2)))
        (is (not (call 'p15-s23-stage2-compiler-artifact-record-authentic? (assoc r :source-contract-version (int 2)) 2)))
        (is (zero? @callbacks))))))

(deftest legacy-public-arity-and-explicit-version-one-retain-existing-route
  (let [calls (atom [])]
    (redefine {'p15-s23-sh07-b51-compatibility-source-record (constantly {:path "legacy.gravity" :text "synthetic"})
               'p15-s23-stage2-compiler-artifact-source-path (constantly "legacy.gravity")
               'p15-s23-stage2-compiler-artifact-binding-from-source!
               (fn [& args] (swap! calls conj args) :synthetic-legacy-result)}
      (fn []
        (is (= :synthetic-legacy-result (call 'p15-s23-stage2-compiler-artifact-binding! emitter "request.gravity" :jvm)))
        (is (= :synthetic-legacy-result (call 'p15-s23-stage2-compiler-artifact-binding! emitter "request.gravity" :jvm 1)))
        (is (= 2 (count @calls)))
        (is (= (first @calls) (second @calls)))
        (is (= 5 (count (first @calls))))))))

(deftest constructed-plan-propagates-only-the-native-version-two-metadata
  (with-fixture {}
    (fn [_]
      (redefine {'p15-s23-stage2-assemble-plan-products
                 (fn [_ _ definitions]
                   (is (= [['main {:params []}]] definitions))
                   {:functions {'main {:params []}} :instruction-summary {} :effect-summary {} :binding-table {}})
                 'c4-artifact-id hash-value}
        (fn []
          (doseq [version [2 (int 2) nil]]
            (let [native-version? (identical? Long (class version))
                  r (record)
                  module {:module 'synthetic :profile :hosted :target :jvm
                          :p15-s23-sh07-compiler-artifact-binding (assoc r :source-contract-version version)}
                  output (call 'p15-s23-stage2-construct-emitted-core-plan emitter "synthetic.gravity" text module {'main {:params []}})]
              (is (= (when native-version? 2) (get-in output [:compiler :expression-lowering-source-contract-version])))
              (is (= (when native-version? (:functions-semantic-hash fixture-contract))
                     (get-in output [:compiler :expression-lowering-functions-semantic-hash])))
              (is (= (when native-version? 1) (get-in output [:compiler :expression-lowering-function-count])))
              (is (= native-version? (boolean (call 'p15-s23-stage2-compiler-artifact-record-matches-plan? r output 2)))))))))))

(let [result (run-tests 'expression-binding-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :versioned-expression-binding-mechanism-controls
            :native-carriers-source-io-digests-and-validation :actual
            :compiler-source-reader-and-emitter-owner :declared-oracles :fixture-contract :synthetic
            :source-authentication false :expression-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
