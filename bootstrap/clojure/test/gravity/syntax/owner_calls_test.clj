(ns gravity.syntax.owner-calls-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.syntax :as syntax]))

(defn- caught [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo ex ex)))

(defn- diagnostic-operations []
  {:c3-syntax-fail! #'b/c3-syntax-fail! :source-span #'b/source-span
   :sh04-syntax-require-carrier! (fn [_ _ value] value)})

(defn- diagnostic-routes []
  [(fn [operations] (syntax/boundary-fail! "C3-ID" "owner.qst" :field :observed {} operations))
   (fn [operations] (syntax/registered-literal-fail! "owner.qst" {} [:literal] {} operations))
   (fn [operations] (syntax/accept-result! "owner.qst" {:status :rejected :diagnostics []} operations))])

(deftest diagnostic-vars-capture-the-owner-before-span-arguments
  (doseq [route (diagnostic-routes)]
    (with-redefs [b/c3-syntax-fail! (fn [& _] (throw (ex-info "original" {:owner :original})))
                  b/source-span
                  (fn [path index]
                    (alter-var-root #'b/c3-syntax-fail!
                                    (constantly (fn [& _] (throw (ex-info "later" {:owner :later})))))
                    {:source path :form-index index})]
      (let [operations (diagnostic-operations)
            first-error (caught #(route operations))
            later-error (caught #(route operations))]
        (is (= :original (:owner (ex-data first-error))))
        (is (= :later (:owner (ex-data later-error))))))))

(deftest supplied-function-values-retain-their-existing-capture
  (doseq [route (diagnostic-routes)]
    (with-redefs [b/c3-syntax-fail! (fn [& _] (throw (ex-info "captured" {:owner :captured})))
                  b/source-span
                  (fn [path index]
                    (alter-var-root #'b/c3-syntax-fail!
                                    (constantly (fn [& _] (throw (ex-info "later" {:owner :later})))))
                    {:source path :form-index index})]
      (let [operations (update (update (diagnostic-operations) :c3-syntax-fail! deref) :source-span deref)]
        (is (= :captured (:owner (ex-data (caught #(route operations))))))))))

(deftest explicit-spans-short-circuit-and-returning-failures-preserve-data
  (let [path (String. "identity.gravity") span {:source path :byte-start 0 :byte-end 3}
        facts {:missing :fact} calls (atom [])
        failure (fn [& arguments] (swap! calls conj arguments) :returned-failure)
        operations {:c3-syntax-fail! failure
                    :source-span (fn [& _] (throw (AssertionError. "Explicit span must be retained")))
                    :sh04-syntax-require-carrier! (fn [_ _ value] value)}
        rejected {:status :rejected :diagnostics [{:rule "C3-ID" :primary {:span span :syntax-id "syntax-id"}
                                                 :facts {:form-kind :symbol :missing-fields [:source]}}]}]
    (is (= :returned-failure (syntax/registered-literal-fail! path {:span span} [:literal] facts operations)))
    (is (identical? rejected (syntax/accept-result! path rejected operations)))
    (is (= ["C3-FACT-STALE" "C3-ID"] (mapv first @calls)))
    (doseq [arguments @calls]
      (is (identical? path (second arguments)))
      (is (identical? span (:source-span (nth arguments 2)))))
    (is (identical? facts (:facts (nth (first @calls) 3))))))

(deftest boundary-facts-precedence-and-owner-exception-identity-survive
  (let [span {:source "facts.qst"} facts {:sh04-boundary :supplied :reason :kept}
        sentinel (ex-info "owner identity" {})
        operations {:source-span (fn [& _] span) :c3-syntax-fail! (fn [& args] args)}
        arguments (syntax/boundary-fail! "C3-ID" "facts.qst" :field :observed facts operations)]
    (is (= facts (:facts (nth arguments 3))))
    (is (identical? span (:source-span (nth arguments 2))))
    (is (identical? sentinel
                    (caught #(syntax/boundary-fail! "C3-ID" "facts.qst" :field :observed facts
                                                   (assoc operations :c3-syntax-fail! (fn [& _] (throw sentinel)))))))))

(deftest nested-runtime-cleaning-captures-each-owner-before-execution
  (with-redefs [b/sh04-syntax-strip-host-metadata identity
                b/p15-s23-stage2-runtime-execute-function
                (fn [& _]
                  (alter-var-root #'b/sh04-syntax-strip-host-metadata (constantly (fn [_] :later-cleaned)))
                  :native-result)]
    (let [operations {:sh04-syntax-strip-host-metadata #'b/sh04-syntax-strip-host-metadata
                      :p15-s23-stage2-runtime-execute-function #'b/p15-s23-stage2-runtime-execute-function
                      :sh04-syntax-boundary-fail! (fn [& _] (throw (AssertionError. "No runtime refusal")))}]
      (is (= :native-result (syntax/execute-plan! "runtime.qst" {:plan :unit-control} 'unit [1] operations)))
      (is (= :later-cleaned (syntax/execute-plan! "runtime.qst" {:plan :unit-control} 'unit [1] operations))))))

(deftest runtime-error-owner-is-captured-before-cause-message
  (let [cause (proxy [Exception] []
                (getMessage []
                  (alter-var-root #'b/sh04-syntax-boundary-fail!
                                  (constantly (fn [& _] (throw (ex-info "later" {:owner :later})))))
                  "cause"))]
    (with-redefs [b/sh04-syntax-boundary-fail! (fn [& _] (throw (ex-info "original" {:owner :original})))]
      (let [operations {:sh04-syntax-strip-host-metadata identity
                        :p15-s23-stage2-runtime-execute-function (fn [& _] (throw cause))
                        :sh04-syntax-boundary-fail! #'b/sh04-syntax-boundary-fail!}]
        (is (= :original (:owner (ex-data (caught #(syntax/execute-plan! "error.qst" {:plan :unit-control} 'unit [] operations))))))))))

(deftest native-carriers-and-results-retain-real-refusals-without-authentication
  (let [path "native.gravity" value {:status :accepted :value [1 true nil]}
        operations {:p15-s23-trusted-carrier-validation #'b/p15-s23-trusted-carrier-validation
                    :sh04-syntax-boundary-fail! #'b/sh04-syntax-boundary-fail!
                    :sh04-syntax-require-carrier! #'b/sh04-syntax-require-carrier!
                    :c3-syntax-fail! #'b/c3-syntax-fail! :source-span #'b/source-span}]
    (is (identical? value (syntax/require-result-carrier! path :native-result value operations)))
    (is (identical? value (syntax/accept-result! path value operations)))
    (doseq [foreign [(Object.) (with-meta {:value 1} {:host-only true})]]
      (let [failure (caught #(syntax/require-result-carrier! path :foreign-result foreign operations))]
        (is (= "C3-SHAPE" (:id (ex-data failure))))
        (is (= [:bounded-syntax-result-carrier] (:missing-fields (ex-data failure))))))))

(deftest runtime-containment-and-interruption-keep-their-original-boundaries
  (doseq [[error field]
          [[(StackOverflowError. "stack") :bounded-syntax-host-stack]
           [(AssertionError. "assertion") :contained-syntax-assertion]
           [(LinkageError. "linkage") :contained-syntax-linkage]
           [(ex-info "diagnostic" {:id "NATIVE"}) :contained-syntax-runtime-diagnostic]
           [(Exception. "host") :contained-syntax-host-failure]]]
    (let [result (syntax/execute-plan! "contained.qst" {:plan :unit-control} 'unit []
                                       {:sh04-syntax-strip-host-metadata identity
                                        :p15-s23-stage2-runtime-execute-function (fn [& _] (throw error))
                                        :sh04-syntax-boundary-fail! (fn [& args] args)})]
      (is (= "C3-SHAPE" (first result)))
      (is (= field (nth result 2)))))
  (let [interrupted (InterruptedException. "interrupt") fatal (Error. "fatal")
        thread (Thread/currentThread) was-interrupted (.isInterrupted thread)]
    (try
      (doseq [error [interrupted fatal]]
        (let [propagated (try
                             (syntax/execute-plan! "propagate.qst" {:plan :unit-control} 'unit []
                                                   {:sh04-syntax-strip-host-metadata identity
                                                    :p15-s23-stage2-runtime-execute-function (fn [& _] (throw error))
                                                    :sh04-syntax-boundary-fail! (fn [& _] (throw (AssertionError. "Must propagate")))})
                             nil
                             (catch InterruptedException ex {:error ex :interrupted? (.isInterrupted thread)})
                             (catch Error ex {:error ex}))]
          (is (identical? error (:error propagated)))
          (when (identical? interrupted error)
            (is (:interrupted? propagated)))))
      (finally (when-not was-interrupted (Thread/interrupted))))))
