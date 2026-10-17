(ns gravity.self-hosting.sh01-stage2-runtime-iteration-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh09-body-effect-test :as body-effects]))

(def ^:private runtime {:engine :stage2-runtime-iteration-test})
(def ^:private plan {:source {:path "stage2-runtime-iteration-test.gravity"}})
(def ^:private compiler-plan
  {:compiler-artifact-plan? true
   :kind :gravity/stage2-compiler-artifact-plan
   :module {:profile :meta}
   :compiler {:stage :p15-s23-stage2-expression-lowering}
   :source {:path "stage2-runtime-iteration-test.gravity"}})

(deftype ^:private ThrowingAssociative [failure]
  clojure.lang.Associative
  (containsKey [_ _] false)
  (entryAt [_ _] nil)
  (assoc [_ _ _] (throw failure))
  clojure.lang.ILookup
  (valAt [_ _] nil)
  (valAt [_ _ not-found] not-found)
  clojure.lang.IPersistentCollection
  (count [_] 0)
  (cons [_ _] (throw failure))
  (empty [_] {})
  (equiv [this other] (identical? this other))
  clojure.lang.Seqable
  (seq [_] nil))

(deftype ^:private HostilePersistentCallee [equiv-calls hash-calls failure]
  clojure.lang.IPersistentCollection
  (count [_] 0)
  (cons [_ _] (throw failure))
  (empty [_] (throw failure))
  (equiv [_ _]
    (swap! equiv-calls inc)
    (throw (AssertionError. "hostile persistent equiv invoked")))
  clojure.lang.Seqable
  (seq [_] (throw failure))
  Object
  (hashCode [_]
    (swap! hash-calls inc)
    (throw (AssertionError. "hostile persistent hash invoked"))))

(defn- literal-instructions
  [values]
  (mapv (fn [value] {:op :literal :value value}) values))

(defn- diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- interpreted-builtin
  [active-plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-instruction
   runtime active-plan {}
   {:op :builtin-call
    :function function
    :args (literal-instructions arguments)}))

(defn- module-private [name] (ns-resolve 'gravity.bootstrap name))
(defn- module-fixture [name extension]
  (loop [directory (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"))))]
    (if (.isFile (io/file directory "deps.edn"))
      (.getPath (io/file directory (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/" name extension)))
      (recur (.getParentFile directory)))))

(deftest compiler-module-literal-data-development-kernel
  (let [path (module-fixture "seed-library" ".gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        source "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog {:nested [nil true 7 \"value\" #{:a}]}) (def secret :private) (defn shadow [catalog] catalog) (defn main [value] [catalog (let [catalog value] catalog) (shadow value) (quote catalog) secret])"
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path source)]
    (is (= {:nested [nil true 7 "value" #{:a}]} (get-in compiled [:functions 'main :instructions 0 :items 0 :value])))
    (is (= [{:nested [nil true 7 "value" #{:a}]} :argument :argument 'catalog :private]
           (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'main [:argument])))
    (is (= :literal (get-in compiled [:functions 'main :instructions 0 :items 0 :op])))
    (is (= '(data (quote untouched) (dangerous call))
           (bootstrap/p15-s23-stage2-module-literal! '(quote (data (quote untouched) (dangerous call))))))
    (is (= {:symbol 'catalog :forms '(call argument)}
           (bootstrap/p15-s23-stage2-module-literal! '{:symbol (quote catalog) :forms (quote (call argument))})))
    (doseq [body ["(def catalog other)" "(def catalog (identity 1))" "(def catalog (quote data extra))"
                  "(def catalog {:nested [other]})" "(def catalog other) (def other 1)"
                  "(def catalog 1) (def catalog 2)" "(def catalog 1) (defn catalog [x] x)"]]
      (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                           emitter path (str "(ns example.literal (:profile :meta) (:target :jvm)) " body)))))))
    (is (= :unsupported-literal-data
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (nth (iterate vector :leaf) 66))) [:facts :reason])))
    (is (= :unsupported-literal-data
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (apply str (repeat 65537 "x")))) [:facts :reason])))
    (is (= :literal-data-size-limit
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (vec (repeat 65537 nil)))) [:facts :reason])))
    (is (= :duplicate-module-definition
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literals '[(def value 1) (defn value [x] x)])) [:facts :reason])))
    (is (= "L2-DUPLICATE-FUNCTION"
           (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                              emitter path "(ns example.literal (:profile :meta) (:target :jvm)) (defn same [x] x) (defn same [x] x)")))))
    (is (= 42 (bootstrap/p15-s23-stage2-runtime-execute-function
               runtime (bootstrap/p15-s23-stage2-compiler-artifact-plan
                        emitter path "(ns example.literal (:profile :meta) (:target :jvm)) (def identity (fn [x] x))")
               'identity [42])))
    (let [compile (fn [literal used?]
                    (bootstrap/p15-s23-stage2-compiler-artifact-plan
                     emitter path (str "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog " literal
                                       ") (defn main [x] " (if used? "catalog" "x") ")")))
          unused-a (compile 1 false) unused-b (compile 2 false)
          used-a (compile 1 true) used-b (compile 2 true)]
      (is (not= (:plan-id unused-a) (:plan-id unused-b)))
      (is (= (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input unused-a)
             (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input unused-b)))
      (is (not= (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input used-a)
                (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input used-b))))))

(deftest compiler-module-literal-data-existing-source-kernel
  (doseq [relative ["gravity/src/gravity/checked_core.gravity"]]
    (let [path (str/replace (module-fixture "seed-library" ".gravity")
                           "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity" relative)
          text (slurp path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
          compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text)]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
      (is (seq (:functions compiled)))
      (is (seq (bootstrap/p15-s23-stage2-module-literals (bootstrap/read-forms path text))))
      (is (not (contains? compiled :literal-data)))))
  (let [path (str/replace (module-fixture "seed-library" ".gravity")
                          "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                          bootstrap/p15-s23-sh02-source-relative-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))]
    (is (= bootstrap/p15-s23-sh02-expected-plan-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input compiled))))
    (is (= bootstrap/p15-s23-sh02-expected-functions-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (:functions compiled))))
    (is (= bootstrap/p15-s23-sh02-expected-plan-id
           (str "sha256:" (bootstrap/sha256-hex
                            (pr-str (bootstrap/c-backend-canonical-value
                                     (assoc-in (dissoc compiled :plan-id) [:source :byte-count]
                                               bootstrap/p15-s23-sh02-source-byte-count)))))))))

(deftest compiler-module-route-development-kernel
  ;; Normalized routing DATA, not authenticated module input or admission.
  (let [instruction {:op :compiler-module-call :route-id :route :function 'library/identity
                     :caller-function 'relay :callsite-path [0]
                     :args (literal-instructions [:left :right])}
        caller-plan {:module {:module 'example.caller} :functions {'relay {:arity 2 :params '[x y] :instructions [instruction]}}}
        callee-plan {:module {:module 'example.library} :functions {'identity {:arity 2 :params '[x y] :instructions [{:op :local :name 'y}]}}}
        exported {:binding-id :export :name 'identity :kind :function :visibility :public :namespace 'example.library}
        caller {:module-id :caller :plan caller-plan}
        callee {:module-id :callee :plan callee-plan :exports [exported]
                :resolution-artifact {:gravity-resolution-boundary {:resolved-analysis {:module-contract {:namespace 'example.library}}}}}
        route {:route-id :route :caller-module-id :caller :caller-function 'relay :callsite-path [0]
               :symbol 'library/identity :callee-module-id :callee :callee-binding-id :export :callee-function 'identity :arity 2}
        bundle {:modules [caller callee] :routes [route]}
        scope {:kind :gravity/stage2-module-invocation :bundle bundle :module-id :caller :function 'relay}
        execute (module-private 'p15-s23-stage2-module-execute-call)
        execute-value (module-private 'p15-s23-stage2-runtime-execute-value-scoped)
        original @execute-value seen (atom [])
        run (fn [p instruction s]
              (with-redefs-fn {execute-value (fn [r p e arg reason s]
                                              (when (= reason :recur-inside-function-argument) (swap! seen conj (:value arg)))
                                              (original r p e arg reason s))}
                #(execute runtime p {} instruction s)))]
    (is (= :right (run caller-plan instruction scope)))
    (is (= [:left :right] @seen))
    (doseq [[p i s] [[caller-plan (assoc instruction :route-id :wrong) scope]
                     [caller-plan (assoc instruction :function 'identity) scope]
                     [caller-plan (assoc instruction :args []) scope]
                     [caller-plan instruction (assoc scope :module-id :callee)]
                     [caller-plan instruction (assoc scope :function 'identity)]
                     [callee-plan instruction scope]
                     [caller-plan instruction (assoc-in scope [:bundle :routes 0 :arity] 3)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :visibility] :private)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :kind] :var)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :namespace] 'example.wrong)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :semantic-span :ordinary-module-artifact-id] :caller)]
                     [caller-plan instruction (assoc-in scope [:bundle :routes 0 :callee-binding-id] :wrong)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :plan :functions 'identity :arity] 1)]]]
      (reset! seen [])
      (is (= "BOOT2006" (:id (diagnostic #(run p i s)))))
      (is (= [] @seen)))
    (reset! seen [])
    (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-instruction
                                          (assoc runtime :module-invocation scope) caller-plan {} instruction)))))
    (is (= [] @seen))
    (is (= "BOOT2006" (:id (diagnostic #(run caller-plan instruction (Object.))))))
    (doseq [key [:sh07-b51-runtime-context-token :sh07-b51-linked-carrier-v4 :sh07-b51-execution-bundle]]
      (is (= :admission-runtime-forbidden
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function
                                   (assoc runtime key {}) {:kind :gravity/stage2-compiler-artifact-module-bundle} 'relay [])) [:facts :reason]))))))

(deftest compiler-module-root-private-entry-development-kernel
  ;; Normalized development DATA; full reconstruction is exercised separately.
  (let [validate (module-private 'p15-s23-stage2-module-validate-bundle!)
        entry-scope (module-private 'p15-s23-stage2-module-entry-scope!)
        original-entry @entry-scope
        private {:arity 1 :params '[value] :instructions [{:op :local :name 'value}]}
        public {:arity 1 :params '[value] :instructions [{:op :function-call :function 'private-entry
                                                       :args [{:op :local :name 'value}]}]}
        root-plan {:module {:module 'example.root}
                   :functions (sorted-map 'private-entry private 'public-entry public 'catalog :data)}
        dependency {:module-id :dependency :plan {:functions {'dependency-only private}} :exports []}
        root {:module-id :root :plan root-plan :exports [{:name 'public-entry :kind :function} {:name 'catalog :kind :var}]}
        bundle {:kind :gravity/stage2-compiler-artifact-module-bundle :root-module-id :root :modules [dependency root] :routes []}
        seen (atom [])
        run (fn [b function args]
              (reset! seen [])
              (with-redefs-fn
                {validate (fn [value] (swap! seen conj :validated) value)
                 entry-scope (fn [p f as scope]
                               (let [next (original-entry p f as scope)]
                                 (swap! seen conj [f (:module-id next) (:function next)]) next))}
                #(bootstrap/p15-s23-stage2-runtime-execute-function runtime b function args)))]
    (is (= :private (run bundle 'private-entry [:private])))
    (is (= [:validated ['private-entry :root 'private-entry]] @seen))
    (is (= :public (run bundle 'public-entry [:public])))
    (is (= [:validated ['public-entry :root 'public-entry] ['private-entry :root 'private-entry]] @seen))
    (doseq [function ['missing 'dependency-only 'catalog 'library/identity 'example.root/private-entry
                      nil :private-entry 7 "private-entry" {} [] true (Object.)]]
      (is (= :root-function-member-required (get-in (diagnostic #(run bundle function [])) [:facts :reason])))
      (is (= [:validated] @seen)))
    (is (= "L2-FUNCTION-ARITY" (:id (diagnostic #(run bundle 'private-entry [])))))
    (is (= :runtime-plan-or-function-member
           (get-in (diagnostic #(run (assoc-in bundle [:modules 1 :plan :functions 'private-entry :arity] 2)
                                   'private-entry [1])) [:facts :reason])))
    (is (= :runtime-module-member
           (get-in (diagnostic #(run (assoc bundle :root-module-id :missing) 'private-entry [1])) [:facts :reason])))
    (is (= :runtime-module-member
           (get-in (diagnostic #(run (update bundle :modules conj root) 'private-entry [1])) [:facts :reason])))
    (with-redefs-fn {validate (fn [_] (throw (ex-info "Reconstruction denied" {:id :reconstruction-denied})))}
      #(is (= :reconstruction-denied
              (:id (diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle :bad [])))))))))

(deftest compiler-module-b51-dispatch-development-kernel
  (let [guard (module-private 'p15-s23-sh07-b51-assert-scoped-function-entry!)
        scoped (module-private 'p15-s23-stage2-runtime-execute-function-scoped)
        opaque (Object.) seen (atom nil)
        raw {:functions {'identity {:params [] :instructions [{:op :literal :value :raw}]}}}]
    (is (= :raw (with-redefs-fn {guard (fn [_ _ _ _ scope] (reset! seen scope))}
                  #(scoped runtime raw 'identity [] opaque))))
    (is (identical? opaque @seen)))
  (let [authorize (module-private 'p15-s23-sh07-b51-runtime-authorize-external-link)
        values (module-private 'p15-s23-stage2-runtime-execute-values-scoped)
        evaluated (atom 0)
        denied (with-redefs-fn {authorize (fn [& _] (throw (ex-info "strict route denied" {:id "STRICT-ROUTE"})))
                               values (fn [& _] (swap! evaluated inc) [])}
                 #(diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-instruction
                                      runtime plan {} {:op :function-call :function 'identity :external-link {}
                                                       :args [{:op :literal :value :argument}]}))))]
    (is (= "STRICT-ROUTE" (:id denied)))
    (is (zero? @evaluated))))

(deftest compiler-module-seed-shape-development-kernel
  (is (= :unsupported-nested-module-call
         (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites
                               {:name 'relay :body '[(let [x 1] (library/identity x))]})) [:facts :reason])))
  (is (bootstrap/p15-s23-stage2-module-fixed-definition? '(defn identity [x] x)))
  (doseq [form ['(defn collect [& xs] xs) '(defn identity [x :- I64] x) '(defn identity [x x] x)
                '(defn identity [x] :- I64 x) '(def initialized (identity 1))]]
    (is (not (bootstrap/p15-s23-stage2-module-fixed-definition? form))))
  (doseq [kind [:namespace :local]]
    (let [source {:request {:references [{:syntax-id :use :symbol 'str :position :operator}]}
                  :artifact {:binding-table {:bindings [{:binding-id :local-str :binding-class kind}]}
                             :gravity-resolution-boundary {:resolved-analysis {:resolution-table [{:syntax-id :use :binding-id :local-str}]}}}}]
      (is (= :unsupported-seed-operator-shadowing
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check!
                                   source {:call-rules {:builtin-functions '[str]}})) [:facts :reason])))))
  (let [source {:module {:namespace 'example.shadow} :functions {'helper {}}
                :request {:references [{:syntax-id :use :symbol 'helper :position :operator}]}
                :artifact {:binding-table {:bindings [{:binding-id :parameter :binding-class :local}]}
                           :gravity-resolution-boundary {:resolved-analysis {:resolution-table [{:syntax-id :use :binding-id :parameter}]}}}}]
    (is (= :unsupported-seed-operator-shadowing
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check! source {})) [:facts :reason]))))
  (doseq [instruction [{:op :function-call :function 'identity :external-link {}}
                       {:op :builtin-call :function 'get :compiler-internal-provenance {}}
                       {:op :builtin-call :function 'sh07-b51-admitted-executable-carrier-validation?}
                       {:op :literal :source-authority {}} {:op :literal :special-role :producer}]]
    (is (= :admission-instruction-forbidden
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-instructions
                                 {:functions {'identity {:instructions [instruction]}}})) [:facts :reason]))))
  (is (= [{:op :quote :value {:external-link {:special-role :data}}}]
         (bootstrap/p15-s23-stage2-module-instructions
          {:functions {'identity {:instructions [{:op :quote :value {:external-link {:special-role :data}}}]}}})))
  (doseq [inputs [nil true {} {:caller-resolution-artifact {} :dependency-plans true}
                 {:caller-resolution-artifact {} :dependency-plans [] :authority true}]]
    (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan {} "module.gravity" "" inputs))))))
  (let [source "(ns example.seed (:profile :meta) (:target :jvm)) (defn identity [x] x)"
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! "module.gravity" :jvm))
        forbidden (fn [& _] (throw (ex-info "Raw seed reached a checked frontdoor" {})))
        raw (with-redefs [bootstrap/compiler-c4-macro-source-artifact forbidden
                          bootstrap/compiler-c5-resolution-source-artifact forbidden
                          bootstrap/compiler-c6-lowering-source-artifact forbidden]
              (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "module.gravity" source))]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind raw)))
    (is (= :clojure-stage0-seed (get-in raw [:compiler :compiled-by])))
    (is (= {:nested [1 {:data #{:x :y}}]}
           (bootstrap/p15-s23-stage2-runtime-execute-function runtime raw 'identity [{:nested [1 {:data #{:x :y}}]}])))))

(deftest compiler-module-root-private-authenticated-paired-composition
  (let [validate (module-private 'p15-s23-stage2-module-validate-bundle!)
        original-validate @validate
        cases (mapv
                (fn [extension]
                  (let [library-path (module-fixture "seed-library" extension)
                        caller-path (module-fixture "private-root-consumer" extension)
                        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                        library (bootstrap/compiler-c5-resolution-file-artifact library-path)
                        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                        caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
                        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                        root (body-effects/module-root-entry bundle)
                        validations (atom 0) value {:nested [1 #{:value}]}]
                    (is (= :accepted (:status library) (:status caller)))
                    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
                    (is (= :not-checked (get-in bundle [:boundary :effects])))
                    (is (false? (get-in bundle [:boundary :self-hosted?])))
                    (is (= #{['public-entry :function] ['catalog :var]} (set (map (juxt :name :kind) (:exports root)))))
                    (is (= :private (:visibility (first (filter #(= 'private-relay (:name %)) (get-in caller [:binding-table :bindings]))))))
                    (is (map? (get-in root [:plan :functions 'private-relay])))
                    (is (= 'private-relay (get-in bundle [:routes 0 :caller-function])))
                    (with-redefs-fn {validate (fn [candidate] (swap! validations inc) (original-validate candidate))}
                      #(do
                         (is (= [:callee value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'private-relay [value])))
                         (is (= [:callee value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'public-entry [value])))
                         (is (= :root-function-member-required
                                (get-in (diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'catalog []))) [:facts :reason])))))
                    (is (= 3 @validations))
                    {:bundle bundle :caller-path caller-path :c5-inputs c5-inputs :emitter emitter}))
                [".gravity" ".qst"])
        {:keys [bundle caller-path c5-inputs emitter]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (doseq [function ['identity 'library/identity 'example.private-root-consumer/private-relay :private-relay]]
      (is (= :root-function-member-required
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle function [])) [:facts :reason]))))
    (is (= "L2-FUNCTION-ARITY"
           (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'private-relay [])))))
    (is (= "C5-PRIVATE"
           (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                               caller-path (str/replace (slurp caller-path) "library/identity" "library/hidden") c5-inputs)))))
    (let [entries (mapv (fn [entry]
                          (if (= (:root-module-id bundle) (:module-id entry))
                            (let [changed (assoc-in (:plan entry) [:functions 'private-relay :instructions] [{:op :literal :value :forged}])
                                  changed (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex
                                             (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
                              (assoc entry :plan changed)) entry)) (:modules bundle))
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id bundle) entries (:routes bundle))
          forged (assoc bundle :modules entries :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'private-relay [1])) [:facts :reason]))))
    (let [other-id (:module-id (first (remove #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))))
          identity (bootstrap/p15-s23-stage2-module-identity emitter other-id (:modules bundle) (:routes bundle))
          forged (assoc bundle :root-module-id other-id :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'hidden [1]))))))))

(deftest compiler-module-literal-data-authenticated-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-path (module-fixture "literal-library" extension)
                 caller-path (module-fixture "literal-consumer" extension)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 library (bootstrap/compiler-c5-resolution-file-artifact library-path)
                 module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path module-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                 root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
                 value {:argument [42 #{:nested}]}]
             (is (= :accepted (:status library) (:status caller)))
             (is (= [{:module :caller} value value 'catalog :caller-private]
                    (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'identity [value])))
             (is (= [{:module :library :nested [nil true 7 "value" #{:a :b}]} :library-private value]
                    (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [value])))
             (is (= #{['catalog :var] ['identity :function] ['relay :function]}
                    (set (map (juxt :name :kind) (:exports root)))))
             (is (= :private (:visibility (first (filter #(= 'secret (:name %)) (get-in caller [:binding-table :bindings]))))))
             {:bundle bundle :caller-path caller-path :emitter emitter :inputs inputs :module-inputs module-inputs}))
         [".gravity" ".qst"])
        {:keys [bundle caller-path emitter inputs module-inputs]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (is (= :root-function-member-required
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'catalog [])) [:facts :reason])))
    (doseq [mutate [(fn [entry] (assoc-in entry [:plan :functions 'identity :instructions 0 :items 0 :value] :tampered))
                    (fn [entry] (update entry :exports #(filterv (fn [binding] (not= :var (:kind binding))) %)))]]
      (let [tampered (update bundle :modules (fn [entries] (mapv #(if (= (:root-module-id bundle) (:module-id %)) (mutate %) %) entries)))]
        (is (= :module-bundle-reconstruction-mismatch
               (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime tampered 'identity [1])) [:facts :reason])))))
    (let [text (str/replace (slurp caller-path) "(library/identity value)" "library/catalog")
          artifact (bootstrap/compiler-c5-resolution-source-artifact caller-path text module-inputs)]
      (is (= :accepted (:status artifact)))
      (is (= :qualified-direct-import-calls-only
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                   emitter caller-path text (assoc inputs :caller-resolution-artifact artifact))) [:facts :reason]))))))

(deftest compiler-module-literal-data-actual-c8-composition
  (let [path (str/replace (module-fixture "seed-library" ".gravity")
                          "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                          "gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
        bundle (body-effects/compile-c8-module-bundle path)
        entry (body-effects/module-root-entry bundle)
        artifact (:resolution-artifact entry)
        literals (bootstrap/p15-s23-stage2-module-literals (bootstrap/read-forms path (slurp path)))
        catalogs '#{c8-effect-graph-contract c8-effect-legality-contract c8-capability-proof-contract
                    c8-build-effect-contract c8-replay-ordering-contract c8-effect-diagnostic-catalog}]
    (is (= :accepted (:status artifact)))
    (is (= catalogs (set (keys literals))))
    (is (= catalogs (set (map :name (filter #(= :var (:kind %)) (:exports entry))))))
    (is (= (count (get-in artifact [:gravity-resolution-boundary :resolved-analysis :module-contract :exports]))
           (count (:exports entry))))
    (let [result (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'verify-c8-effect-checker [:input])]
      (is (= :stage1-source-owned (:status result)))
      (is (= 'typed-core-artifact (:typed-core-artifact result)))
      (is (= 'c8-effect-diagnostic-catalog (:diagnostics result))))))

(deftest compiler-module-authenticated-source-and-runtime-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-path (module-fixture "seed-library" extension)
                 caller-path (module-fixture "seed-consumer" extension)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 library (bootstrap/sh06-resolution-file-artifact library-path)
                 c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                 nested {:payload [1 {:nested #{:a :b}}] :symbol 'data/identity :quoted '(external-link :special-role)}]
             (is (= :accepted (:status library) (:status caller)))
             (is (= :gravity/stage2-compiler-artifact-module-bundle (:kind bundle)))
             (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
             (is (= :not-checked (get-in bundle [:boundary :effects])))
             (is (= 2 (count (:modules bundle)) (count (set (map :module-id (:modules bundle))))))
             (is (= 1 (count (:routes bundle))))
             (is (= :stage2-local (get-in dependency [:functions 'identity :binding :visibility])))
             (is (= [:callee 41] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [41])))
             (is (= [:callee nested] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [nested])))
             (is (= [:caller nested] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'identity [nested])))
             (is (= (:artifact-id library) (get-in bundle [:routes 0 :callee-module-id])))
             (is (= :public (:visibility (first (:exports (first (filter #(= (:artifact-id library) (:module-id %)) (:modules bundle))))))))
             {:bundle bundle :caller-path caller-path :library library :emitter emitter :inputs inputs :c5-inputs c5-inputs :dependency dependency}))
         [".gravity" ".qst"])
        {:keys [bundle caller-path library emitter inputs c5-inputs dependency]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (doseq [[name rule] [["hidden" "C5-PRIVATE"] ["missing" "C5-UNRESOLVED"]]]
      (is (= rule (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                                    caller-path (str/replace (slurp caller-path) "library/identity" (str "library/" name)) c5-inputs))))))
    (is (= :source-emitter-or-dependency-set
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                 emitter caller-path (str (slurp caller-path) " ") inputs)) [:facts :reason])))
    (let [changed (assoc-in dependency [:functions 'identity :instructions] [{:op :literal :value :forged}])
          forged (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex
                                                         (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
      (is (= :independent-raw-plan-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                   emitter caller-path (slurp caller-path) (assoc inputs :dependency-plans [forged]))) [:facts :reason]))))
    (let [forged (assoc-in bundle [:routes 0 :callee-module-id] (:root-module-id bundle))
          identity (assoc (:identity-input forged) :routes (:routes forged))
          forged (assoc forged :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'relay [41])) [:facts :reason]))))
    (doseq [body ["(defn collect [& xs] xs)"
                  "(defn str [x] x) (defn identity [x] (str x))"
                  "(defn identity [str] (str 2))"
                  "(defn helper [x] 1) (defn identity [helper] (helper 2))"]]
      (let [text (str "(ns example.seed-rejected (:profile :meta) (:target :jvm) (:exports ["
                      (if (str/includes? body "collect") "collect" "identity") "])) " body)
            artifact (bootstrap/compiler-c5-resolution-source-artifact caller-path text)]
        (is (= :accepted (:status artifact)))
        (if (str/includes? body "collect")
          (is (= :function-only-unannotated-seed-subset
                 (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-source artifact)) [:facts :reason])))
          (is (= :unsupported-seed-operator-shadowing
                 (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check!
                                       (bootstrap/p15-s23-stage2-module-source artifact) emitter)) [:facts :reason]))))))))

(deftest small-argument-carriers-preserve-values-and-order
  (doseq [values [[] [1] [1 2] [1 2 3] [1 2 3 4]]]
    (is (= values
           (bootstrap/p15-s23-stage2-runtime-execute-values
            runtime plan {} (literal-instructions values) :test)))))

(deftest vector-and-list-instruction-bodies-remain-equivalent
  (let [instructions (literal-instructions [1 2 3 4])]
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} instructions)))
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} (apply list instructions))))))

(deftest non-tail-recur-remains-rejected-on-both-traversal-paths
  (let [instructions
        [{:op :recur :args [{:op :literal :value 1}]}
         {:op :literal :value :unreachable}]]
    (doseq [body [instructions (apply list instructions)]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-execute-instructions
               runtime plan {} body))]
        (is (= "L2-RECUR-TARGET" (:id data)))
        (is (= :non-tail-sequential-position (:reason data)))))))

(deftest recur-carrier-is-distinct-from-ordinary-runtime-maps
  (is (true?
       (bootstrap/p15-s23-stage2-runtime-recur-signal?
        (bootstrap/p15-s23-stage2-runtime-recur-signal [1 2 3]))))
  (doseq [value [{:values [1 2 3]}
                 {:p15-s23/recur-token (Object.) :values [1 2 3]}
                 (sorted-map 'artifact :unrelated)]]
    (is (false?
         (bootstrap/p15-s23-stage2-runtime-recur-signal? value)))))

(deftest hot-get-builtin-preserves-two-and-three-argument-semantics
  (is (= 1
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{:key 1} :key])))
  (is (= :missing
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{} :key :missing])))
  (testing "arity diagnostics remain fail closed"
    (doseq [arguments [[{}] [{} :key :fallback :extra]]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'get arguments))]
        (is (= "L2-BUILTIN-ARITY" (:id data)))
        (is (= (count arguments) (:actual-arity data)))))))

(deftest interpreted-get-avoids-carrier-without-changing-observable-semantics
  (let [invoke
        (fn [arguments]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args (literal-instructions arguments)}))]
    (is (= 1 (invoke [{:key 1} :key])))
    (is (= :missing (invoke [{} :key :missing])))
    (is (nil? (invoke [{} :key])))
    (is (nil? (invoke [{:key nil} :key :missing])))
    (is (nil? (invoke [nil :key])))
    (is (= :missing (invoke [nil :key :missing])))
    (testing "wrong arities retain generic diagnostics"
      (doseq [arguments [[] [{}] [{} :key :missing :extra]]]
        (let [data (diagnostic #(invoke arguments))]
          (is (= "L2-BUILTIN-ARITY" (:id data)))
          (is (= (count arguments) (:actual-arity data))))))
    (testing "lookup failures retain generic builtin error mapping"
      (let [arguments [(sorted-map 1 :one) :key]
            data (diagnostic #(invoke arguments))
            generic-data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'get arguments))]
        (is (= "L2-BUILTIN-ERROR" (:id data)))
        (is (= 'get (:function data)))
        (is (= generic-data data))))))

(deftest map-literal-carriers-preserve-order-duplicates-and-recur-boundaries
  (let [entry
        (fn [key value]
          {:key {:op :literal :value key}
           :value {:op :literal :value value}})
        instruction
        (fn [entries]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :map-literal :entries entries}))]
    (is (= {:first 1 :second 2}
           (instruction [(entry :first 1) (entry :second 2)])))
    (is (= {:same :last}
           (instruction [(entry :same :first) (entry :same :last)])))
    (is (= {:first 1 :second 2}
           (instruction (apply list [(entry :first 1) (entry :second 2)]))))
    (testing "malformed list entries fail closed instead of ending traversal"
      (doseq [entries [(list nil (entry :second 2))
                       (list false (entry :second 2))
                       (list (entry :first 1) nil)
                       (list (entry :first 1) false)]]
        (let [data (diagnostic #(instruction entries))]
          (is (= "L2-UNKNOWN-CORE-FORM" (:id data)) entries))))
    (testing "key evaluation precedes value evaluation and preserves recur rejection"
      (let [data
            (diagnostic
             #(instruction
               [{:key {:op :recur :args [{:op :literal :value :key}]}
                 :value {:op :literal :value :value}}]))]
        (is (= "L2-RECUR-TARGET" (:id data)))
        (is (= :recur-inside-map-key (:reason data)))))))

(deftest interpreted-get-evaluates-arguments-left-to-right-exactly-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function 'get
         :args [{:op :literal :value {:key 1}}
                {:op :literal :value :key}
                {:op :literal :value :missing}]}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             (:value argument))}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (= 1 result))
    (is (= [[{:key 1} :recur-inside-builtin-argument]
            [:key :recur-inside-builtin-argument]
            [:missing :recur-inside-builtin-argument]]
           @seen))))

(deftest interpreted-get-preserves-recur-and-exceptioninfo-boundaries
  (let [recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args [{:op :recur :args [{:op :literal :value {}}]}
                   {:op :literal :value :key}]}))
        exceptional
        (reify clojure.lang.ILookup
          (valAt [_ _]
            (throw (ex-info "lookup failed" {:id "TEST-LOOKUP"})))
          (valAt [_ _ _]
            (throw (ex-info "lookup failed" {:id "TEST-LOOKUP"}))))
        exception-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args (literal-instructions [exceptional :key])}))]
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data)))
    (is (= "TEST-LOOKUP" (:id exception-data)))))

(deftest unary-collection-instructions-preserve-values-and-arity-diagnostics
  (let [invoke
        (fn [function arguments]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function function
            :args (literal-instructions arguments)}))]
    (is (= 2 (invoke 'count [[1 2]])))
    (is (= 1 (invoke 'first [[1 2]])))
    (is (= 2 (invoke 'second [[1 2]])))
    (is (= (list 2) (invoke 'rest [[1 2]])))
    (testing "wrong arities retain the generic fail-closed path"
      (doseq [[function arguments]
              [['count []]
               ['first [[1] [2]]]
               ['second []]
               ['rest [[1] [2]]]]]
        (let [data (diagnostic #(invoke function arguments))]
          (is (= "L2-BUILTIN-ARITY" (:id data)))
          (is (= (count arguments) (:actual-arity data))))))))

(def ^:private direct-unary-cases
  [['count [1 2] 2 false]
   ['first [1 2] 1 false]
   ['second [1 2] 2 false]
   ['rest [1 2] (list 2) false]
   ['symbol? 'value true true]
   ['keyword? :value true true]
   ['char? \x true true]
   ['number? 1 true true]
   ['seq? (list 1) true true]
   ['list? (list 1) true true]
   ['vector? [1] true true]
   ['map? {:key 1} true true]
   ['set? #{1} true true]
   ['string? "value" true true]
   ['even? 2 true true]
   ['integer? 2 true true]
   ['boolean? false true true]
   ['keys (array-map :a 1 :b 2) (list :a :b) true]
   ['set [1 1 2] #{1 2} true]
   ['sort-by-pr-str [2 1] (list 1 2) true]
   ['vec (list 1 2) [1 2] true]])

(deftest direct-unary-allowlist-matches-generic-builtin-semantics
  (doseq [[function value expected compiler-only?] direct-unary-cases]
    (let [active-plan (if compiler-only? compiler-plan plan)
          generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           active-plan function [value])
          interpreted (interpreted-builtin active-plan function [value])]
      (is (= expected generic) [function :generic])
      (is (= generic interpreted) [function :interpreted]))))

(deftest direct-unary-evaluates-pure-data-arguments-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value-scoped)
        calls (atom [])
        data
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env instruction reason _public-scope]
             (swap! calls conj [instruction reason])
             (:value instruction))}
          #(interpreted-builtin plan 'map? [{:evaluated true}]))]
    (is (= [[{:op :literal :value {:evaluated true}}
             :recur-inside-builtin-argument]]
           @calls))
    (is (true? data))
    (is (false? (bootstrap/p15-s23-stage2-compiler-artifact-plan-context? plan))))
  (testing "argument diagnostics propagate before builtin execution"
    (let [data
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-execute-instruction
             runtime plan {}
             {:op :builtin-call
              :function 'map?
              :args [{:op :local :name 'missing}]}))]
      (is (= "L2-UNKNOWN-SYMBOL" (:id data))))))

(deftest malformed-nonsymbol-callee-cannot-hash-before-argument-evaluation
  (let [hash-calls (atom 0)
        malformed
        (proxy [Object] []
          (hashCode []
            (swap! hash-calls inc)
            (throw (AssertionError. "callee hash ran before argument"))))
        data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id data)))
    (is (zero? @hash-calls)))
  (testing "dispatch failures after successful evaluation are contained"
    (let [hash-calls (atom 0)
          malformed
          (proxy [Object] []
            (hashCode []
              (swap! hash-calls inc)
              (throw (RuntimeException. "malformed callee hash"))))
          data (diagnostic #(interpreted-builtin plan malformed [:value]))]
      (is (= "L2-BUILTIN-ERROR" (:id data)))
      (is (identical? malformed (:function data)))
      (is (pos? @hash-calls)))))

(deftest unary-nonallowlisted-builtins-retain-generic-semantics
  (doseq [[function value expected]
          [['+ 7 7]
           ['* 7 7]
           ['= 7 true]
           ['vector 7 [7]]
           ['list 7 (list 7)]
           ['pr-str :value ":value"]]]
    (is (= expected (interpreted-builtin plan function [value])) function)
    (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
            plan function [value])
           (interpreted-builtin plan function [value]))
        function)))

(deftest direct-unary-preserves-recur-and-exception-boundaries
  (let [recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime compiler-plan {}
           {:op :builtin-call
            :function 'map?
            :args [{:op :recur :args [{:op :literal :value {}}]}]}))
        exceptional
        (reify clojure.lang.Seqable
          (seq [_]
            (throw (ex-info "sequence failed" {:id "TEST-SEQUENCE"}))))
        direct-data (diagnostic #(interpreted-builtin plan 'first [exceptional]))
        generic-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan 'first [exceptional]))]
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data)))
    (is (= "TEST-SEQUENCE" (:id direct-data)))
    (is (= generic-data direct-data))))

(deftest direct-unary-wraps-host-errors-identically-to-generic-dispatch
  (doseq [[function value active-plan]
          [['count 1 plan]
           ['first 1 plan]
           ['second 1 plan]
           ['rest 1 plan]
           ['even? "not-an-integer" compiler-plan]
           ['keys 1 compiler-plan]
           ['set (Object.) compiler-plan]
           ['sort-by-pr-str (Object.) compiler-plan]
           ['vec (Object.) compiler-plan]]]
    (let [direct (diagnostic #(interpreted-builtin active-plan function [value]))
          generic
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
             active-plan function [value]))]
      (is (= "L2-BUILTIN-ERROR" (:id direct)) function)
      (is (= generic direct) function))))

(deftest direct-unary-wrong-arities-remain-on-generic-path
  (doseq [[function value _expected compiler-only?] direct-unary-cases
          arguments [[] [value value]]]
    (let [active-plan (if compiler-only? compiler-plan plan)
          data (diagnostic #(interpreted-builtin active-plan function arguments))]
      (is (= "L2-BUILTIN-ARITY" (:id data)) [function arguments])
      (is (= (count arguments) (:actual-arity data)) [function arguments]))))

(deftest direct-binary-equality-matches-generic-semantics
  (doseq [[left right]
          [[nil nil]
           [nil false]
           [1 1]
           [1 1N]
           [1 2]
           [:value :value]
           ['value 'value]
           [[1 2] (list 1 2)]
           [(with-meta [1 2] {:side :left})
            (with-meta [1 2] {:side :right})]
           [[1 {:key :value}] [1 {:key :value}]]
           [{:key [1 2]} {:key [1 2]}]
           [#{1 2} #{2 1}]]]
    (let [generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [left right])
          interpreted (interpreted-builtin plan '= [left right])]
      (is (= generic interpreted) [left right])))
  (let [same-object (Object.)]
    (is (true? (interpreted-builtin plan '= [same-object same-object]))))
  (testing "equality remains valid outside compiler-artifact context"
    (is (true? (interpreted-builtin plan '= [:same :same])))
    (is (true?
         (interpreted-builtin {:compiler-artifact-plan? true}
                              '= [:same :same]))))
  (testing "list argument carriers retain the same semantics"
    (is (true?
         (bootstrap/p15-s23-stage2-runtime-execute-instruction
          runtime plan {}
          {:op :builtin-call
           :function '=
           :args (apply list (literal-instructions [[1] [1]]))})))))

(deftest direct-binary-equality-evaluates-left-to-right-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function '=
         :args [{:op :literal :value :left}
                {:op :literal :value :right}]}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             :same)}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (true? result))
    (is (= [[:left :recur-inside-builtin-argument]
            [:right :recur-inside-builtin-argument]]
           @seen))))

(deftest direct-binary-equality-preserves-argument-and-recur-boundaries
  (let [missing-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :local :name 'missing-left}
                   {:op :local :name 'missing-right}]}))
        recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :recur :args [{:op :literal :value :left}]}
                   {:op :local :name 'missing-right}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id missing-data)))
    (is (= 'missing-left (:symbol missing-data)))
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data))))
  (let [right-recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :literal :value :left}
                   {:op :recur :args [{:op :literal :value :right}]}]}))]
    (is (= "L2-RECUR-TARGET" (:id right-recur-data)))
    (is (= :recur-inside-builtin-argument (:reason right-recur-data))))
  (testing "a first-argument error prevents second-argument evaluation"
    (let [execute-value
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-execute-value)
          seen (atom [])
          data
          (with-redefs-fn
            {execute-value
             (fn [_runtime _plan _env argument _reason]
               (swap! seen conj (:value argument))
               (if (= :left (:value argument))
                 (throw (ex-info "first failed" {:id "TEST-EQUALITY-FIRST"}))
                 :unreachable))}
            #(diagnostic
              (fn []
                (interpreted-builtin plan '= [:left :right]))))]
      (is (= "TEST-EQUALITY-FIRST" (:id data)))
      (is (= [:left] @seen)))))

(deftest direct-binary-equality-preserves-error-mapping
  (let [runtime-error (RuntimeException. "host equality failed")
        hostile
        (proxy [Object] []
          (equals [_] (throw runtime-error)))
        direct-data (diagnostic #(interpreted-builtin plan '= [hostile :right]))
        generic-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [hostile :right]))]
    (is (= "L2-BUILTIN-ERROR" (:id direct-data)))
    (is (= '= (:function direct-data)))
    (is (= generic-data direct-data)))
  (let [info-error (ex-info "host equality diagnostic" {:id "TEST-EQUALITY"})
        hostile
        (proxy [Object] []
          (equals [_] (throw info-error)))
        observed
        (try
          (interpreted-builtin plan '= [hostile :right])
          nil
          (catch clojure.lang.ExceptionInfo error error))
        generic-observed
        (try
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [hostile :right])
          nil
          (catch clojure.lang.ExceptionInfo error error))]
    (is (identical? info-error observed))
    (is (identical? info-error generic-observed))
    (is (identical? generic-observed observed)))
  (let [fatal-error (AssertionError. "host equality fatal")
        hostile
        (proxy [Object] []
          (equals [_] (throw fatal-error)))
        observed
        (try
          (interpreted-builtin plan '= [hostile :right])
          nil
          (catch AssertionError error error))]
    (is (identical? fatal-error observed))))

(deftest direct-binary-equality-keeps-other-arities-and-callees-generic
  (is (true? (interpreted-builtin plan '= [7])))
  (is (true? (interpreted-builtin plan '= [7 7 7])))
  (is (false? (interpreted-builtin plan '= [7 7 8])))
  (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan '= [7 7 7 7])
         (interpreted-builtin plan '= [7 7 7 7])))
  (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan '= [7 7 7 8])
         (interpreted-builtin plan '= [7 7 7 8])))
  (is (= "L2-BUILTIN-ARITY"
         (:id (diagnostic #(interpreted-builtin plan '= [])))))
  (let [hash-calls (atom 0)
        malformed
        (proxy [Object] []
          (hashCode []
            (swap! hash-calls inc)
            (throw (RuntimeException. "malformed binary callee"))))
        argument-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}
                   {:op :literal :value :right}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id argument-data)))
    (is (zero? @hash-calls))
    (let [dispatch-data
          (diagnostic #(interpreted-builtin plan malformed [:left :right]))]
      (is (= "L2-BUILTIN-ERROR" (:id dispatch-data)))
      (is (identical? malformed (:function dispatch-data)))
      (is (pos? @hash-calls)))))

(deftest direct-three-argument-assoc-matches-generic-semantics
  (doseq [arguments
          [[nil :key :value]
           [{} :key :value]
           [{:existing 1} :key [1 2]]
           [[0 1 2] 1 :replacement]
           [(with-meta {:existing 1} {:source :fixture}) :key :value]]]
    (let [generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan 'assoc arguments)
          interpreted (interpreted-builtin plan 'assoc arguments)]
      (is (= generic interpreted) arguments)
      (is (= (meta generic) (meta interpreted)) arguments)))
  (testing "assoc has no compiler-artifact context requirement"
    (is (= {:key :value}
           (interpreted-builtin plan 'assoc [{} :key :value])))
    (is (= {:key :value}
           (interpreted-builtin compiler-plan 'assoc [{} :key :value]))))
  (testing "a distinct but equal assoc symbol selects the direct operation"
    (is (= {:key :value}
           (interpreted-builtin plan (symbol "assoc")
                                [{} :key :value]))))
  (testing "list argument carriers retain the same semantics"
    (is (= {:key :value}
           (bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {}
            {:op :builtin-call
             :function 'assoc
             :args (apply list
                          (literal-instructions [{} :key :value]))})))))

(deftest direct-three-argument-assoc-evaluates-left-to-right-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function 'assoc
         :args (literal-instructions [:collection :key :value])}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             (case (:value argument)
               :collection {}
               :key :key
               :value :value))}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (= {:key :value} result))
    (is (= [[:collection :recur-inside-builtin-argument]
            [:key :recur-inside-builtin-argument]
            [:value :recur-inside-builtin-argument]]
           @seen))))

(deftest direct-three-argument-assoc-preserves-argument-and-recur-boundaries
  (doseq [[position expected]
          [[0 [:collection]]
           [1 [:collection :key]]
           [2 [:collection :key :value]]]]
    (let [execute-value
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-execute-value)
          labels [:collection :key :value]
          seen (atom [])
          marker (nth labels position)
          observed
          (with-redefs-fn
            {execute-value
             (fn [_runtime _plan _env argument _reason]
               (let [label (:value argument)]
                 (swap! seen conj label)
                 (if (= marker label)
                   (throw (ex-info "argument failed"
                                   {:id "TEST-ASSOC-ARGUMENT"
                                    :position position}))
                   (case label :collection {} :key :key :value :value))))}
            #(diagnostic
              (fn []
                (interpreted-builtin plan 'assoc labels))))]
      (is (= "TEST-ASSOC-ARGUMENT" (:id observed)) position)
      (is (= position (:position observed)) position)
      (is (= expected @seen) position)))
  (doseq [position (range 3)]
    (let [arguments
          (assoc (vec (literal-instructions [{} :key :value]))
                 position
                 {:op :recur
                  :args [{:op :literal :value position}]})
          data
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-execute-instruction
             runtime plan {}
             {:op :builtin-call :function 'assoc :args arguments}))]
      (is (= "L2-RECUR-TARGET" (:id data)) position)
      (is (= :recur-inside-builtin-argument (:reason data)) position))))

(deftest direct-three-argument-assoc-preserves-error-mapping
  (let [assoc-var (ns-resolve 'clojure.core 'assoc)
        runtime-error (RuntimeException. "host assoc failed")
        direct
        (with-redefs-fn
          {assoc-var (fn [& _] (throw runtime-error))}
          #(diagnostic
            (fn [] (interpreted-builtin plan 'assoc [{} :key :value]))))
        generic
        (with-redefs-fn
          {assoc-var (fn [& _] (throw runtime-error))}
          #(diagnostic
            (fn []
              (bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'assoc [{} :key :value]))))]
    (is (= "L2-BUILTIN-ERROR" (:id direct)))
    (is (= 'assoc (:function direct)))
    (is (= generic direct)))
  (doseq [error [(ex-info "assoc diagnostic" {:id "TEST-ASSOC"})
                 (AssertionError. "assoc fatal")]]
    (let [assoc-var (ns-resolve 'clojure.core 'assoc)
          observed
          (with-redefs-fn
            {assoc-var (fn [& _] (throw error))}
            #(try
               (interpreted-builtin plan 'assoc [{} :key :value])
               nil
               (catch Throwable thrown thrown)))]
      (is (identical? error observed))))
  (testing "custom Associative throwables retain exact identity"
    (doseq [error [(ex-info "custom associative diagnostic"
                            {:id "TEST-ASSOCIATIVE"})
                   (AssertionError. "custom associative fatal")]]
      (let [carrier (ThrowingAssociative. error)
            direct
            (try
              (interpreted-builtin plan 'assoc [carrier :key :value])
              nil
              (catch Throwable thrown thrown))
            generic
            (try
              (bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'assoc [carrier :key :value])
              nil
              (catch Throwable thrown thrown))]
        (is (identical? error direct))
        (is (identical? error generic))
        (is (identical? generic direct))))))

(deftest direct-three-argument-assoc-keeps-other-arities-and-callees-generic
  (is (= {:a 1 :b 2}
         (interpreted-builtin plan 'assoc [{} :a 1 :b 2])))
  (doseq [arguments [[] [{}] [{} :key]
                     [{} :key :value :extra]
                     [{} :a 1 :b 2 :dangling]]]
    (let [data (diagnostic #(interpreted-builtin plan 'assoc arguments))]
      (is (= "L2-BUILTIN-ARITY" (:id data)) arguments)
      (is (= (count arguments) (:actual-arity data)) arguments)))
  (let [arguments [7 :key :value]
        generic (diagnostic
                 #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
                   plan 'assoc arguments))
        direct (diagnostic #(interpreted-builtin plan 'assoc arguments))]
    (is (= "L2-BUILTIN-ERROR" (:id direct)))
    (is (= generic direct)))
  (let [hash-calls (atom 0)
        equals-calls (atom 0)
        malformed
        (proxy [Object] []
          (equals [_]
            (swap! equals-calls inc)
            (throw (RuntimeException. "malformed assoc equality")))
          (hashCode []
            (swap! hash-calls inc)
            (throw (RuntimeException. "malformed assoc callee"))))
        argument-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}
                   {:op :literal :value :key}
                   {:op :literal :value :value}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id argument-data)))
    (is (zero? @hash-calls))
    (is (zero? @equals-calls))
    (let [dispatch-data
          (diagnostic
           #(interpreted-builtin malformed malformed [{} :key :value]))]
      (is (= "L2-BUILTIN-ERROR" (:id dispatch-data)))
      (is (identical? malformed (:function dispatch-data)))
      (is (pos? @hash-calls))))
  (testing "persistent hostile callees cannot run equiv or hash before args"
    (doseq [arity [1 2 3]]
      (let [equiv-calls (atom 0)
            hash-calls (atom 0)
            failure (AssertionError. "hostile persistent callee")
            malformed
            (HostilePersistentCallee. equiv-calls hash-calls failure)
            arguments
            (into [{:op :local :name 'missing}]
                  (repeat (dec arity) {:op :literal :value :unreachable}))
            observed
            (try
              (bootstrap/p15-s23-stage2-runtime-execute-instruction
               runtime plan {}
               {:op :builtin-call
                :function malformed
                :args arguments})
              nil
              (catch Throwable thrown thrown))]
        (is (instance? clojure.lang.ExceptionInfo observed) arity)
        (is (= "L2-UNKNOWN-SYMBOL" (:id (ex-data observed))) arity)
        (is (zero? @equiv-calls) arity)
        (is (zero? @hash-calls) arity)))))

(deftest runtime-artifact-str-remains-on-specialized-generic-path
  (let [artifact-invoke
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-artifact-invoke)
        calls (atom [])
        artifact-runtime (assoc runtime :runtime-artifact-plan true)
        invoke
        (fn [arguments]
          (with-redefs-fn
            {artifact-invoke
             (fn [_runtime function values]
               (swap! calls conj [function values])
               :artifact-result)}
            #(bootstrap/p15-s23-stage2-runtime-execute-instruction
              artifact-runtime plan {}
              {:op :builtin-call
               :function 'str
               :args (literal-instructions arguments)})))]
    (is (= :artifact-result (invoke ["a"])))
    (is (= :artifact-result (invoke ["a" "b"])))
    (is (= 2 (count @calls)))
    (is (= [["a"] ["a" "b"]] (mapv second @calls))))
  (testing "unary artifact exceptions retain the pre-fast-path boundary"
    (let [artifact-invoke
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-artifact-invoke)
          artifact-runtime (assoc runtime :runtime-artifact-plan true)
          invoke
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            artifact-runtime plan {}
            {:op :builtin-call
             :function 'str
             :args (literal-instructions [:value])})
          runtime-error (RuntimeException. "artifact runtime failure")
          observed-runtime
          (with-redefs-fn
            {artifact-invoke (fn [& _] (throw runtime-error))}
            #(try (invoke)
                  nil
                  (catch RuntimeException error error)))
          info-error (ex-info "artifact diagnostic" {:id "TEST-ARTIFACT"})
          observed-info
          (with-redefs-fn
            {artifact-invoke (fn [& _] (throw info-error))}
            #(try (invoke)
                  nil
                  (catch clojure.lang.ExceptionInfo error error)))]
      (is (identical? runtime-error observed-runtime))
      (is (identical? info-error observed-info)))))

(deftest small-function-and-loop-binders-preserve-scope-and-recur
  (let [function-plan
        {:source {:path "stage2-runtime-iteration-test.gravity"}
         :functions
         {'sum-to
          {:params ['n 'sum]
           :instructions
           [{:op :if
             :test {:op :builtin-call
                    :function '>
                    :args [{:op :local :name 'n}
                           {:op :literal :value 0}]}
             :then {:op :recur
                    :args
                    [{:op :builtin-call
                      :function '-
                      :args [{:op :local :name 'n}
                             {:op :literal :value 1}]}
                     {:op :builtin-call
                      :function '+
                      :args [{:op :local :name 'sum}
                             {:op :local :name 'n}]}]}
             :else {:op :local :name 'sum}}]}
          'loop-scope
          {:params ['outer]
           :instructions
           [{:op :loop
             :bindings [{:name 'index
                         :expr {:op :literal :value 0}}]
             :body
             [{:op :if
               :test {:op :builtin-call
                      :function '<
                      :args [{:op :local :name 'index}
                             {:op :literal :value 2}]}
               :then {:op :recur
                      :args [{:op :builtin-call
                              :function '+
                              :args [{:op :local :name 'index}
                                     {:op :literal :value 1}]}]}
               :else {:op :vector-literal
                      :items [{:op :local :name 'outer}
                              {:op :local :name 'index}]}}]}]}
          'caller
          {:params ['value]
           :instructions
           [{:op :function-call
             :function 'sum-to
             :args [{:op :local :name 'value}
                    {:op :literal :value 0}]}]}}}]
    (is (= 15
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'sum-to [5 0])))
    (is (= [9 2]
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'loop-scope [9])))
    (is (= 6
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'caller [3])))))

(deftest common-builtin-arities-preserve-hosted-core-semantics
  (let [compiler-plan
        {:compiler-artifact-plan? true
         :kind :gravity/stage2-compiler-artifact-plan
         :module {:profile :meta}
         :compiler {:stage :p15-s23-stage2-expression-lowering}
         :source {:path "stage2-runtime-iteration-test.gravity"}}
        invoke
        (fn [callee arguments]
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan callee arguments))]
    (doseq [[callee arguments expected]
            [['+ [] 0]
             ['+ [7] 7]
             ['+ [7 5] 12]
             ['+ [7 5 3] 15]
             ['+ [7 5 3 2] 17]
             ['- [7] -7]
             ['- [7 5] 2]
             ['- [7 5 3] -1]
             ['* [] 1]
             ['* [7] 7]
             ['* [7 5] 35]
             ['/ [4] 1/4]
             ['/ [12 3] 4]
             ['/ [24 3 2] 4]
             ['= [1] true]
             ['= [1 1] true]
             ['= [1 1 2] false]
             ['< [1 2] true]
             ['< [1 2 3] true]
             ['> [3 2 1] true]
             ['<= [1 1 2] true]
             ['>= [2 2 1] true]
             ['str [] ""]
             ['str ["a"] "a"]
             ['str ["a" 1] "a1"]
             ['str ["a" 1 :b] "a1:b"]
             ['first [[1 2]] 1]
             ['second [[1 2]] 2]
             ['count [[1 2]] 2]]]
      (is (= expected (invoke callee arguments))
          [callee arguments]))
    (is (= (list 2) (invoke 'rest [[1 2]])))
    (doseq [[callee arguments expected]
            [['symbol? ['value] true]
             ['keyword? [:value] true]
             ['vector? [[1 2]] true]
             ['map? [{:key 1}] true]
             ['contains? [{:key 1} :key] true]
             ['even? [2] true]
             ['integer? [2] true]
             ['keys [{:key 1}] (list :key)]
             ['quot [7 2] 3]
             ['subvec [[1 2 3] 1 3] [2 3]]]]
      (is (= expected
             (bootstrap/p15-s23-stage2-runtime-invoke-builtin
              compiler-plan callee arguments))
          [callee arguments]))
    (doseq [[callee arguments]
            [['- []] ['/ []] ['= []] ['< [1]] ['> [1]]]]
      (is (= "L2-BUILTIN-ARITY"
             (:id (diagnostic #(invoke callee arguments))))))))
