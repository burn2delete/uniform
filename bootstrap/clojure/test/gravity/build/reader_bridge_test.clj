(ns gravity.build.reader-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [gravity.build.declared-inputs :as inputs]
            [gravity.build.declared-inputs-test :as fixture]
            [gravity.build.reader-bridge :as bridge]))

(def instruction {:op :builtin-call :function 'build-read-declared-input
                  :args [{:op :literal :value "schema"}]})
(def runtime {:engine :injected-model})
(def module {:profile :meta :target :jvm :effects #{:build/read-file} :capabilities #{:fs/read}})
(def plan {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
           :module module :functions {'gravity.reader/read-input
                                     {:instructions [instruction]
                                      :body (list (list 'build-read-declared-input "schema"))
                                      :native-literals [1N 1/2 \a 1.0M]}}})
(def rows [{:instruction instruction :request fixture/request}])
(defn failure [run]
  (try (run) :unexpected-success
       (catch clojure.lang.ExceptionInfo error (:reason (ex-data error)))))
(defn with-scope [run]
  (fixture/host-call fixture/grant fixture/declared
    (fn [cap] (#'bridge/call-with-scope cap runtime plan rows run))))
(defn form-call [scope mode run]
  (#'bridge/call-with-form scope fixture/request mode run))

(deftest fixed-read-captures-once-and-verification-only-looks-up-original
  (let [escaped (atom nil)]
    (with-scope
      (fn [scope]
        (reset! escaped scope)
        (is (bridge/assert-function-entry! runtime plan 'gravity.reader/read-input [] scope))
        (is (= "schema text" (form-call scope :construct
                               #(bridge/read! runtime plan instruction ["schema"] scope))))
        (is (= :reader-duplicate-effect
               (failure #(form-call scope :construct
                            (fn [] (bridge/read! runtime plan instruction ["schema"] scope))))))
        (with-redefs-fn {#'inputs/input-entry (fn [& _] (throw (Error. "provider replay")))}
          #(is (= "schema text" (form-call scope :verify
                                  (fn [] (bridge/read! runtime plan instruction ["schema"] scope))))))))
    (is (false? (bridge/registered-scope? @escaped)))
    (is (= :reader-scope-identity
           (failure #(bridge/read! runtime plan instruction ["schema"] @escaped))))))

(deftest identity-current-form-and-native-refusals
  (with-scope
    (fn [scope]
      (is (= :reader-original-result-missing
             (failure #(form-call scope :verify
                          (fn [] (bridge/read! runtime plan instruction ["schema"] scope))))))
      (is (= :reader-current-form
             (failure #(bridge/read! runtime plan instruction ["schema"] scope))))
      (doseq [[r p i args s] [[runtime plan instruction ["schema"] (Object.)]
                              [(into {} runtime) plan instruction ["schema"] scope]
                              [runtime (into {} plan) instruction ["schema"] scope]
                              [runtime plan (assoc instruction :op :builtin-call :copy true) ["schema"] scope]
                              [runtime plan instruction ["other"] scope]
                              [runtime plan instruction [1] scope]
                              [runtime plan instruction ["schema" "extra"] scope]]]
        (is (not= :unexpected-success
                  (failure #(form-call scope :construct (fn [] (bridge/read! r p i args s)))))))
      (is (= :reader-function-entry
             (failure #(bridge/assert-function-entry! runtime plan 'unknown [] scope))))))
  (let [called (atom 0)
        foreign (reify clojure.lang.ILookup
                  (valAt [_ _] (swap! called inc))
                  (valAt [_ _ _] (swap! called inc)))]
    (is (= :native-carrier (failure #(bridge/read! runtime plan foreign [] nil))))
    (is (= :reader-native-scope (failure #(bridge/native-scope! foreign))))
    (is (zero? @called))))

(deftest all-grants-and-callsite-membership-preflight-before-reads
  (let [reads (atom 0)]
    (with-redefs-fn {#'inputs/input-entry (fn [& _] (swap! reads inc))}
      #(fixture/host-call fixture/grant fixture/declared
         (fn [cap]
           (is (= :request-scope
                  (failure (fn [] (#'bridge/call-with-scope
                                    cap runtime plan
                                    (conj rows {:instruction instruction
                                                :request (assoc fixture/request :input "late-invalid")})
                                    identity)))))
           (is (= :reader-handler-occurrence
                  (failure (fn [] (#'bridge/call-with-scope
                                    cap runtime plan
                                    [{:instruction (assoc instruction :copy true) :request fixture/request}]
                                    identity)))))
           (is (= :duplicate-reader-form
                  (failure (fn [] (#'bridge/call-with-scope cap runtime plan (conj rows (first rows)) identity))))))))
    (is (zero? @reads)))
  (doseq [bad [(assoc module :profile :hosted) (assoc module :target :wasm)
               (assoc module :effects #{}) (assoc module :capabilities #{})]]
    (is (= :read-legality (failure #(bridge/validate-call! bad 1))))))

(def component-root "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/")
(defn forms [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader. (io/reader path))]
      (let [eof (Object.)]
        (loop [out []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form) out (recur (conj out form)))))))))

(deftest isolated-dispatch-model-preserves-compatibility-and-original-context
  ;; Evaluate ONLY the selected Clojure dispatch definition with injected
  ;; helpers in a fresh model namespace, never Bootstrap or a Gravity plan.
  (let [form (first (filter #(and (seq? %) (= 'defn (first %))
                                  (= 'p15-s23-stage2-runtime-invoke-builtin (second %)))
                           (forms (str component-root "00588-p15-s23-stage2-runtime-invoke-builtin.clj"))))
        model (create-ns (gensym "reader.dispatch.model."))]
    (try
      (binding [*ns* model]
        (refer 'clojure.core)
        (alias 'declared-reader-bridge 'gravity.build.reader-bridge)
        (doseq [symbol (distinct (filter #(and (symbol? %) (nil? (namespace %))
                                              (.startsWith (name %) "p15-s23-"))
                                        (tree-seq coll? seq form)))]
          (intern model symbol (fn [& _] (throw (Error. "unselected model helper")))))
        (intern model 'p15-s23-stage2-runtime-assert-exact-arity!
                (fn [_ _ args n] (when-not (= n (count args))
                                   (throw (ex-info "arity" {:id "L2-BUILTIN-ARITY"})))))
        (eval form)
        (let [dispatch (ns-resolve model 'p15-s23-stage2-runtime-invoke-builtin)]
          (is (= :reader-scope-identity (failure #(dispatch plan 'build-read-declared-input ["schema"]))))
          (is (= :reader-scope-identity (failure #(dispatch runtime plan 'build-read-declared-input ["schema"]))))
          (let [observed (atom nil) scope (Object.)]
            (with-redefs [bridge/read! (fn [& actual] (reset! observed actual) "model")]
              (is (= "model" (dispatch runtime plan 'build-read-declared-input ["schema"] instruction scope))))
            (is (identical? instruction (nth @observed 2)))
            (is (identical? scope (nth @observed 4))))))
      (finally (remove-ns (ns-name model))))))

(deftest changed-source-forms-read-without-target-evaluation
  (doseq [path [(str component-root "00590-p15-s23-stage2-runtime-recur-signal.clj")
                (str component-root "00593-p15-s23-stage2-runtime-execute-function.clj")
                (str component-root "00576-p15-s23-stage2-seed-compile-expr.clj")
                "bootstrap/gravity/p15_s23/emitter.gravity"]]
    (is (seq (forms path)))))
