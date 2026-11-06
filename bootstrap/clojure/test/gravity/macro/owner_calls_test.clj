(ns gravity.macro.owner-calls-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.macro :as macro]))

(defn- input [key]
  (case key :sh05-macro-version "unit-version" :c4-macro-diagnostic-ids ["C4-TRACE"]))
(defn- owner [tag] (fn [& arguments] (into [tag] arguments)))
(defn- operations []
  {:input input :source-span #'b/source-span :c4-macro-fail! #'b/c4-macro-fail!})
(defn- rejection []
  {:diagnostics [{:rule "C4-RETURN" :diagnostic-id-request {:digest-ref 0}
                  :macro-name 'unit/native :macro-version "unit-version" :profile :meta :target :jvm
                  :severity :error :facts {:unit true} :remediation []}]})
(defn- diagnostic-routes []
  [(fn [ports] (macro/boundary-fail! "C4-TRACE" "owner.qst" :field :value {} ports))
   (fn [ports] (macro/raise-rejection! "owner.qst" (rejection) ["sha256:unit"] ports))])
(defn- runtime-operations []
  {:p15-s23-stage2-runtime-execute-function #'b/p15-s23-stage2-runtime-execute-function
   :sh04-syntax-strip-host-metadata #'b/sh04-syntax-strip-host-metadata
   :sh05-macro-boundary-fail! #'b/sh05-macro-boundary-fail!})

(deftest diagnostic-owner-is-selected-before-span-and-remains-late-next-call
  (doseq [route (diagnostic-routes)]
    (with-redefs [b/c4-macro-fail! (owner :original)
                  b/source-span (fn [path index]
                                  (alter-var-root #'b/c4-macro-fail! (constantly (owner :later)))
                                  {:source path :form-index index})]
      (let [ports (operations)]
        (is (= :original (first (route ports))))
        (is (= :later (first (route ports))))))))

(deftest boundary-owner-is-selected-before-the-input-callback
  (with-redefs [b/c4-macro-fail! (owner :original)
                b/source-span (fn [path index] {:source path :form-index index})]
    (let [ports (assoc (operations) :input
                        (fn [key]
                          (alter-var-root #'b/c4-macro-fail! (constantly (owner :later)))
                          (input key)))]
      (is (= :original (first (macro/boundary-fail! "C4-TRACE" "input.gravity" :field :value {} ports)))))))

(deftest ordinary-function-ports-keep-their-existing-capture
  (doseq [route (diagnostic-routes)]
    (with-redefs [b/c4-macro-fail! (owner :captured)
                  b/source-span (fn [path index]
                                  (alter-var-root #'b/c4-macro-fail! (constantly (owner :later)))
                                  {:source path :form-index index})]
      (let [ports (-> (operations) (update :source-span deref) (update :c4-macro-fail! deref))]
        (is (= :captured (first (route ports))))))))

(deftest outer-strip-owner-is-selected-before-nested-runtime-execution
  (with-redefs [b/sh04-syntax-strip-host-metadata identity
                b/p15-s23-stage2-runtime-execute-function
                (fn [& _]
                  (alter-var-root #'b/sh04-syntax-strip-host-metadata (constantly (fn [_] :later-strip)))
                  :native-result)]
    (let [ports (runtime-operations)]
      (is (= :native-result (macro/execute-plan! "outer.qst" {:plan :unit} 'unit [1] ports)))
      (is (= :later-strip (macro/execute-plan! "outer.qst" {:plan :unit} 'unit [1] ports))))))

(deftest runtime-owner-is-selected-before-inner-argument-stripping
  (with-redefs [b/p15-s23-stage2-runtime-execute-function (fn [& _] :original-runtime)
                b/sh04-syntax-strip-host-metadata
                (fn [value]
                  (alter-var-root #'b/p15-s23-stage2-runtime-execute-function
                                  (constantly (fn [& _] :later-runtime)))
                  value)]
    (let [ports (runtime-operations)]
      (is (= :original-runtime (macro/execute-plan! "inner.gravity" {:plan :unit} 'unit [] ports)))
      (is (= :later-runtime (macro/execute-plan! "inner.gravity" {:plan :unit} 'unit [] ports))))))

(deftest runtime-failure-owner-is-selected-before-cause-message
  (let [cause (proxy [Exception] []
                (getMessage []
                  (alter-var-root #'b/sh05-macro-boundary-fail! (constantly (owner :later)))
                  "native cause"))]
    (with-redefs [b/sh05-macro-boundary-fail! (owner :original)
                  b/sh04-syntax-strip-host-metadata identity
                  b/p15-s23-stage2-runtime-execute-function (fn [& _] (throw cause))]
      (is (= :original
             (first (macro/execute-plan! "cause.qst" {:plan :unit} 'unit [] (runtime-operations))))))))

(deftest contain-failure-owner-is-selected-before-throwable-message
  (let [cause (proxy [Error] []
                (getMessage []
                  (alter-var-root #'b/sh05-macro-boundary-fail! (constantly (owner :later)))
                  "contained native error"))]
    (with-redefs [b/sh05-macro-boundary-fail! (owner :original)]
      (is (= :original
             (first (macro/contain-operation! "contain.gravity" :unit-responsibility (fn [] (throw cause))
                                               {:input input :sh05-macro-boundary-fail! #'b/sh05-macro-boundary-fail!})))))))

(deftest diagnostic-fields-and-returning-failure-behavior-stay-identical
  (let [path (String. "facts.qst") span {:source path :form-index 7} facts {:sh05-boundary :supplied}
        ports {:input input :source-span (fn [& _] span) :c4-macro-fail! (fn [& args] args)}
        arguments (macro/boundary-fail! "C4-TRACE" path :field :value facts ports)]
    (is (= "C4-TRACE" (first arguments)))
    (is (identical? path (second arguments)))
    (is (identical? span (:source-span (nth arguments 2))))
    (is (= "unit-version" (:macro-version (nth arguments 2))))
    (is (= facts (:facts (nth arguments 3))))
    (is (= [:field] (:missing-fields (nth arguments 3))))))

(deftest macro-containment-keeps-known-diagnostics-and-broader-throwable-boundary
  (let [known (ex-info "known" {:id "C4-TRACE"}) foreign (ex-info "foreign" {:id "UNIT"})
        fatal (Error. "contained by Macro") value (Object.)
        ports {:input input :sh05-macro-boundary-fail! (fn [& args] args)}]
    (is (identical? value (macro/contain-operation! "value.qst" :responsibility (fn [] value) ports)))
    (is (identical? known (try (macro/contain-operation! "known.qst" :responsibility (fn [] (throw known)) ports)
                              nil (catch clojure.lang.ExceptionInfo error error))))
    (doseq [[error rule] [[foreign "C4-TRACE"] [fatal "C4-RETURN"] [(StackOverflowError. "stack") "C4-DEPTH"]]]
      (let [arguments (macro/contain-operation! "contained.qst" :responsibility (fn [] (throw error)) ports)]
        (is (= rule (first arguments)))
        (is (= :responsibility (nth arguments 2)))))))

(deftest execution-keeps-original-contained-classes-and-fatal-propagation
  (doseq [[error rule field]
          [[(StackOverflowError. "stack") "C4-DEPTH" :bounded-macro-host-stack]
           [(AssertionError. "assertion") "C4-RETURN" :contained-macro-assertion]
           [(LinkageError. "linkage") "C4-RETURN" :contained-macro-linkage]
           [(ex-info "diagnostic" {:id "UNIT"}) "C4-RETURN" :contained-macro-runtime-diagnostic]
           [(Exception. "host") "C4-RETURN" :contained-macro-host-failure]]]
    (let [arguments (macro/execute-plan! "execute.gravity" {:plan :unit} 'unit []
                                        {:sh04-syntax-strip-host-metadata identity
                                         :p15-s23-stage2-runtime-execute-function (fn [& _] (throw error))
                                         :sh05-macro-boundary-fail! (fn [& args] args)})]
      (is (= rule (first arguments)))
      (is (= field (nth arguments 2)))))
  (let [fatal (Error. "propagated by execute")]
    (is (identical? fatal
                    (try (macro/execute-plan! "fatal.qst" {:plan :unit} 'unit []
                                             {:sh04-syntax-strip-host-metadata identity
                                              :p15-s23-stage2-runtime-execute-function (fn [& _] (throw fatal))})
                         nil (catch Error error error))))))

(deftest interruption-propagates-with-flag-before-test-reporting
  (let [thread (Thread/currentThread) was-interrupted (.isInterrupted thread)]
    (try
      (doseq [route [:execute :contain]]
        (let [interrupted (InterruptedException. "unit interruption")
              observed (try
                         (case route
                           :execute (macro/execute-plan! "interrupt.qst" {:plan :unit} 'unit []
                                                         {:sh04-syntax-strip-host-metadata identity
                                                          :p15-s23-stage2-runtime-execute-function (fn [& _] (throw interrupted))})
                           :contain (macro/contain-operation! "interrupt.qst" :responsibility (fn [] (throw interrupted))
                                                             {:input input}))
                         nil
                         (catch InterruptedException error [error (.isInterrupted thread)]))]
          (is (identical? interrupted (first observed)))
          (is (true? (second observed)))
          (Thread/interrupted)))
      (finally (when was-interrupted (.interrupt thread))))))
