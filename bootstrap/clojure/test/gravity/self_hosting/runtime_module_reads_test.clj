(ns gravity.self-hosting.runtime-module-reads-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private runtime-plan
  (delay
    (let [path "bootstrap/gravity/src/gravity/runtime.gravity"
          text (slurp path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text))))

(defn- invoke [name & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :runtime-module-reads-test} @runtime-plan name (vec arguments)))

(defn- reads [instructions definitions]
  (invoke 'runtime-module-reads {'main {:instructions instructions}} definitions))

(defn- nested [instruction depth]
  (reduce (fn [body _] {:op :do :body [body]}) instruction (range depth)))

(def ^:private literal {:op :literal :value true})
(def ^:private missing-read {:op :namespace-read :binding-id :missing})

(defn- read-fixture []
  (let [source {:form-id "definition-form" :syntax-id "definition-syntax"
                :semantic-span nil :origin-chain [] :generated-origin []}
        definition {:binding-id "binding" :definition-core-node-id "definition"
                    :definition-syntax-id "binding-syntax" :definition-source source
                    :value-core-node-id "initializer" :value-source source
                    :type :gravity.type/bool}
        read (assoc definition :op :namespace-read :version :p15-s23-namespace-read-v1
                    :reference-core-node-id "reference" :reference-source source)]
    {:definition definition :read read}))

(deftest module-read-traversal-has-no-work-or-depth-admission
  (testing "Complete traversal accepts valid inputs beyond the previous limits"
    (is (= {:status :accepted} (reads [(nested literal 130)] {})))
    (is (= {:status :accepted} (reads (vec (repeat 65537 literal)) {})))
    (let [children (vec (repeat 65536 literal))]
      (is (= {:status :accepted :children children}
             (invoke 'p15-s23-runtime-instruction-children {:op :do :body children})))))
  (testing "An invalid read remains visible beyond either removed limit"
    (doseq [instructions [[(nested missing-read 130)]
                          (conj (vec (repeat 65536 literal)) missing-read)
                          (into [missing-read] (repeat 65536 literal))]]
      (is (= {:status :rejected :reason :namespace-read-without-initialized-definition}
             (reads instructions {})))))
  (testing "Every function is visited"
    (is (= :rejected
           (:status (invoke 'runtime-module-reads
                            {'first {:instructions [literal]}
                             'last {:instructions [missing-read]}} {})))))
  (testing "Quoted values are data even when they resemble executable reads"
    (is (= {:status :accepted}
           (reads [{:op :quote :value (nested missing-read 130)}] {})))))

(deftest module-read-source-and-definition-checks-survive
  (let [{:keys [definition read]} (read-fixture)
        definitions {"binding" definition}]
    (is (= {:status :accepted} (reads [(nested read 130)] definitions)))
    (doseq [field [:binding-id :definition-core-node-id :definition-syntax-id
                   :definition-source :value-core-node-id :value-source :type
                   :reference-core-node-id :reference-source :version]]
      (is (= :namespace-read-without-initialized-definition
             (:reason (reads [(nested (assoc read field nil) 130)] definitions)))
          (str "Changed " field " must fail the actual source validator")))
    (is (= :rejected (:status (reads [(assoc read :extra true)] definitions))))))

(deftest module-read-enumeration-preserves-executable-positions
  (let [wrap (fn [children]
               (:children (invoke 'p15-s23-runtime-instruction-children children)))]
    (doseq [instruction [{:op :function-call :args [missing-read]}
                         {:op :builtin-call :args [missing-read]}
                         {:op :keyword-map-lookup :args [missing-read]}
                         {:op :recur :args [missing-read]}
                         {:op :println :args [missing-read]}
                         {:op :vector-literal :items [missing-read]}
                         {:op :set-literal :items [missing-read]}
                         {:op :do :body [missing-read]}
                         {:op :let :bindings [{:expr missing-read}] :body []}
                         {:op :loop :bindings [] :body [missing-read]}
                         {:op :if :test literal :then missing-read :else literal}
                         {:op :throw :value missing-read}
                         {:op :map-literal :entries [{:key literal :value missing-read}]}
                         {:op :try :protected literal :handlers [{:body missing-read}]}
                         {:op :structural-match :scrutinee literal
                          :clauses [{:guard missing-read :body literal}]}]]
      (is (some #(identical? missing-read %) (wrap instruction)))
      (is (= :namespace-read-without-initialized-definition
             (:reason (reads [instruction] {})))))
    (is (= [] (wrap {:op :quote :value missing-read})))))
