(ns gravity.backend.c.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.c :as c]
            [gravity.bootstrap :as bootstrap]))

(defn- native [name & args]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b2-c17-" name))) args))

(defn- operation [id opcode operands type value]
  {:op-id id :block-id "entry" :opcode opcode :operands operands :type type
   :source-operation :fixture-data :constant-payload {:value value}
   :source {:span {:source "emission-data.gravity" :start {:line 1 :column 1}
                   :end {:line 1 :column 2}}
            :generated-origin [{:kind :fixture-expansion :origin-id (str "parent-" id)}]}
   :facts {:source-origin-id (str "origin-" id) :safety-proof-id (str "proof-" id)
           :proof-certificate-ids [(str "certificate-" id)]}})

(defn- operations []
  (vec (concat
        (map-indexed #(operation (str "constant-" %1) :constant []
                                 (cond (nil? %2) :gravity/nil (boolean? %2) :gravity/bool
                                       :else :gravity/int) %2)
                     [0 7 -7 Long/MIN_VALUE Long/MAX_VALUE true false nil])
        (map-indexed #(operation (str "forward-" %1) %2 ["constant-1"] :gravity/int nil)
                     [:local :local-binding :sequence :lexical-scope :function-boundary])
        (map-indexed #(operation (str "truth-" %1) :truthiness [%2] :gravity/bool nil)
                     ["constant-1" "constant-5" "constant-7"])
        (map-indexed #(operation (str "comparison-" %1) %2 ["constant-1" "constant-2"]
                                 :gravity/bool nil)
                     [:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte])
        [(operation "join" :conditional-join ["comparison-0" "constant-1" "constant-2"]
                    :gravity/int nil)])))

(defn- fixture []
  (let [ops (operations)]
    {:operations ops :index (zipmap (map :op-id ops) (range))
     :function {:blocks {"entry" {:terminator {:operands ["join"]}}}}
     :b1 {:artifact-id "fixture-b1-data" :target {:triple "fixture-target" :data-layout "fixture-layout"}
          :abi {:id "fixture-abi"} :source-map {:id "fixture-map"}
          :runtime {:gravity-runtime-providers [] :platform-runtime-providers [:fixture-platform]}
          :providers {:id "fixture-provider"}
          :proofs {:certificates {:content-id "fixture-proof-table" :entry-count (count ops)}}}
     :mir {:module-id "fixture-module"}
     :dialect {:target-triple "fixture-target" :flags ["-std=c17"]}}))

(defn- rejection [expected actual]
  (try (native "assert-exact!" "emission-data.gravity" expected actual :exact-emission-data)
       nil (catch clojure.lang.ExceptionInfo exception (ex-data exception))))

(defn- capture-native []
  (let [{:keys [operations index function b1 mir dialect]} (fixture)
        records (mapv #(native "operation-record" % operations index) operations)
        return-id (native "return-id" function ["entry"])
        value-name (native "value-name" index return-id)]
    {:operations operations :operation-records records
     :source-text (native "source-text" records value-name)
     :header-text (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-header-text))
     :build (native "build-manifest-record" dialect)
     :abi (native "abi-layout-record" b1) :runtime (native "runtime-helper-record" b1)
     :debug-entries (native "source-debug-entries" operations records)
     :debug-map (native "source-debug-map-record" b1 operations records return-id value-name)
     :proof-bindings (native "operation-proof-bindings" operations records)
     :discard-casts (native "discard-cast-records" operations records)
     :assumptions (native "proof-to-c-assumption-record" b1 mir operations records return-id value-name 7)
     :return-id return-id :value-name value-name
     :exit-codes (mapv #(native "process-exit-code" %) [true false 0 7 255 nil])
     :exact-match (native "assert-exact!" "emission-data.gravity" records records :exact-emission-data)
     :rejection (rejection {:operation-count 22} {:operation-count 21})
     :fallback (native "operation-expression" (operation "unknown" :unknown [] :gravity/int nil)
                       operations index)}))

(def ^:private gravity-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "language/compiler/src/gravity/backend/b2_c_backend_design.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :c))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- gravity-call [name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-c-emission-data-development :compiler-artifact-plan? true}
   @gravity-plan name arguments))

(deftest emits-c-source-and-preserves-provenance
  (let [{:keys [operations operation-records source-text header-text debug-map assumptions rejection]}
        (capture-native)]
    (is (= 22 (count operations) (count operation-records)))
    (is (str/includes? source-text "int main(void)"))
    (is (str/includes? source-text "INT64_MIN"))
    (is (str/includes? source-text "INT64_C(9223372036854775807)"))
    (is (str/includes? header-text "int main(void);"))
    (is (= 22 (count (:entries debug-map)) (count (:operation-bindings assumptions))))
    (doseq [[op record entry binding] (map vector operations operation-records (:entries debug-map)
                                          (:operation-bindings assumptions))]
      (is (= (:op-id op) (:mir-operation-id record) (:mir-operation-id entry) (:mir-operation-id binding)))
      (is (= (get-in op [:source :span]) (:source-span entry)))
      (is (= (get-in op [:source :generated-origin]) (:generated-origin-chain entry)))
      (is (= (get-in op [:facts :source-origin-id]) (:source-origin-id entry) (:source-origin-id binding)))
      (is (= (get-in op [:facts :safety-proof-id]) (:safety-proof-id binding))))
    (is (= "B2-MANIFEST" (:id rejection)))
    (is (= :exact-emission-data (:missing-fact rejection)))
    (is (= :rejected (:fallback-status rejection)))))

(deftest current-gravity-kernel-emits-the-same-data-and-rejects-missing-input
  (let [{:keys [operations index function b1 mir dialect]} (fixture)
        native (capture-native)
        records (mapv #(gravity-call 'b2-c-operation-record [% operations index]) operations)
        return-id (gravity-call 'b2-return-id [function ["entry"]])
        value-name (gravity-call 'b2-c-value-name [index return-id])]
    (is (= (:operation-records native) records))
    (is (= (:source-text native) (gravity-call 'b2-source-text [records value-name])))
    (is (= (:header-text native) (gravity-call 'b2-header-text [])))
    (is (= (:build native) (gravity-call 'b2-c-build-manifest-record [dialect])))
    (is (= (:abi native) (gravity-call 'b2-abi-layout-record [b1])))
    (is (= (:runtime native) (gravity-call 'b2-runtime-helper-record [b1])))
    (is (= (:debug-map native) (gravity-call 'b2-source-debug-map-record
                                              [b1 operations records return-id value-name])))
    (is (= (:assumptions native) (gravity-call 'b2-proof-to-c-assumption-record
                                               [b1 mir operations records return-id value-name 7])))
    (is (= {:status :rejected :diagnostic "B1-INPUT" :missing-fact :exact-sealed-b1-c-packet
            :operation-id nil}
           (select-keys (gravity-call 'b2-build-bounded-hosted-c17 [nil])
                        [:status :diagnostic :missing-fact :operation-id])))))

(deftest compatibility-ports-preserve-var-interposition-and-order
  (let [events (atom [])
        value-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-value-name)
        constant-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-constant-expression)
        forwarded-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-forwarded-opcode?)
        expression-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-operation-expression)
        debug-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-source-debug-entries)
        proof-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-operation-proof-bindings)
        cast-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-discard-cast-records)
        op (first (operations))]
    (with-redefs-fn
      {value-var (fn [& args] (swap! events conj [:name args]) "ported-name")
       expression-var (fn [& args] (swap! events conj [:expression args]) "ported-expression")}
      #(let [record (native "operation-record" op [op] {(:op-id op) 0})]
         (is (= "ported-name" (:c-value record)))
         (is (= "ported-expression" (:c-expression record)))
         (is (= [:name :expression] (mapv first @events)))))
    (with-redefs-fn {constant-var (fn [_] "ported-constant")}
      #(is (= "ported-constant" (native "operation-expression" op [op] {}))))
    (with-redefs-fn {value-var (fn [& _] "ported-forward")
                    forwarded-var (fn [_] true)}
      #(is (= "ported-forward" (native "operation-expression"
                                       (assoc op :opcode :fixture-forward :operands ["operand"])
                                       [op] {}))))
    (with-redefs-fn {debug-var (fn [& _] [:ported-debug])}
      #(is (= [:ported-debug] (:entries (native "source-debug-map-record" {} [] [] "id" "name")))))
    (with-redefs-fn {proof-var (fn [& _] [:ported-proof])
                    cast-var (fn [& _] [:ported-cast])}
      #(let [record (native "proof-to-c-assumption-record" {} {} [] [] "id" "name" 0)]
         (is (= [:ported-proof] (:operation-bindings record)))
         (is (= :ported-cast (first (:generated-c-casts record))))))))

(deftest exact-comparison-rejects-real-data-drift
  (let [calls (atom [])
        mismatch-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-strict-first-mismatch)
        fail-var (ns-resolve 'gravity.bootstrap 'p15-s23-c-backend-fail!)]
    (doseq [[expected actual] [[{:id "a"} {:id "b"}] [[1 2] [1]]
                               [{:id "a"} {:id "a" :extra true}]]]
      (is (= "B2-MANIFEST" (:id (rejection expected actual)))))
    (is (nil? (rejection {:id "a"} {:id "a"})))
    (with-redefs-fn
      {mismatch-var (fn [& args] (swap! calls conj [:mismatch args])
                      {:path [:id] :expected-kind :string :actual-kind :keyword :secret :omit})
       fail-var (fn [& args] (swap! calls conj [:failure args]) :ported-failure)}
      #(is (= :ported-failure (native "assert-exact!" "source" {} {} :exact-data))))
    (is (= [:mismatch :failure] (mapv first @calls)))
    (is (= ["source" {} {} []] (second (first @calls))))
    (is (= ["B2-MANIFEST" "source" {}
            {:missing-fact :exact-data :bounded-reason {:path [:id] :expected-kind :string :actual-kind :keyword}}]
           (second (second @calls))))))

(deftest records-and-fallbacks-do-not-authenticate-supplied-facts
  (let [contract (:gravity/namespace-contract (meta #'c/operation-record))
        {:keys [b1 operations index]} (fixture)
        op (assoc (first operations) :type :unverified-fixture-type)
        record (native "operation-record" op operations index)]
    (is (= 'gravity.backend.c (:namespace contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (= :unverified-fixture-type (:mir-type record)))
    (is (= :proven-safe (:safety-outcome record)) "Historical data label, not an admission verdict")
    (is (= "INT64_C(0)" (:fallback (capture-native))) "Historical unknown-op fallback requires upstream rejection")
    (is (= :pending-gate-b-toolchain-verification (:status (c/abi-layout-record b1))))
    (is (= :not-performed-in-gate-a (:toolchain-verification (c/abi-layout-record b1))))
    (is (nil? (c/process-exit-code nil)) "Preserve the native helper's existing nil boundary")
    (is (= :pending-gate-b-toolchain-execution (:status (c/build-manifest-record {}))))))

;; These units inject anonymous operations and an opaque carrier. They never
;; invoke the native staging/process broker or Bootstrap compiler-call body.
(defn- sequencing-unit [execute? carrier process! fail! cleanup!]
  (c/run-compiler! nil (fn [_ _] carrier) process! fail! cleanup!
                   "unit.c" "unit-bin" "sequencing-unit.gravity" :unit-target execute?))

(deftest compiler-call-sequence-and-result-metadata
  (let [carrier (Object.) metadata (Object.) events (atom [])
        compiled {:exit 0 :out "compiler-out" :err "compiler-err" :caller-metadata metadata}
        runtime {:exit 0 :out "program-out" :err "program-err"}
        process! (fn [observed command source target phase]
                   (is (identical? carrier observed))
                   (swap! events conj [phase command source target])
                   (case phase :compile compiled :runtime runtime))
        cleanup! (fn [observed source target]
                   (is (identical? carrier observed))
                   (swap! events conj [:cleanup source target]))
        fail! (fn [& _] (throw (Exception. "unexpected unit refusal")))
        result (sequencing-unit true carrier process! fail! cleanup!)]
    (is (= [:compile :runtime :cleanup] (mapv first @events)))
    (is (= ["/usr/bin/cc" "-std=c11" "-Wall" "-Werror"
            (.getAbsolutePath (io/file "unit.c")) "-o" (.getAbsolutePath (io/file "unit-bin"))]
           (get-in @events [0 1])))
    (is (= [(.getAbsolutePath (io/file "unit-bin"))] (get-in @events [1 1])))
    (is (= ["sequencing-unit.gravity" :unit-target] (vec (drop 2 (first @events)))))
    (is (identical? metadata (:caller-metadata result)))
    (is (= (assoc compiled :compile-out "compiler-out" :compile-err "compiler-err"
                  :runtime-out "program-out" :runtime-err "program-err" :runtime-exit 0) result))
    (doseq [execute? [false nil]]
      (reset! events [])
      (is (identical? compiled (sequencing-unit execute? carrier process! fail! cleanup!)))
      (is (= [:compile :cleanup] (mapv first @events))))))

(deftest compiler-call-default-and-bootstrap-forwarding
  (let [marker (Object.) calls (atom [])
        forbidden (fn [& _] (throw (Exception. "unused unit operation invoked")))
        self! (fn [& args] (reset! calls (vec args)) marker)]
    (is (identical? marker
           (c/run-compiler! self! forbidden forbidden forbidden forbidden "a.c" "a-bin" "source.qst" :unit)))
    (is (= ["a.c" "a-bin" "source.qst" :unit false] @calls))
    (is (= '([c-source-path executable-path source-path target]
             [c-source-path executable-path source-path target execute?])
           (:arglists (meta #'bootstrap/c-backend-run-cc!))))
    ;; Only the forwarding wrapper runs; the stage is an inert observer.
    (doseq [arguments [["a.c" "a-bin" "source.qst" :unit]
                       ["a.c" "a-bin" "source.gravity" :unit true]]]
      (with-redefs [c/run-compiler! (fn [& args] (reset! calls (vec args)) marker)]
        (is (identical? marker (apply bootstrap/c-backend-run-cc! arguments))))
      (is (every? var? (take 5 @calls)))
      (is (= arguments (vec (drop 5 @calls)))))
    (let [contract (:gravity/namespace-contract (meta #'c/run-compiler!))]
      (is (= (set (keys (ns-publics 'gravity.backend.c))) (set (keys (:public-api contract)))))
      (is (= (:arglists (meta #'c/run-compiler!)) (get-in contract [:public-api 'run-compiler! :arglists])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-certificates? contract)))
      (is (some #{:native-execution} (:unsupported contract))))))

(deftest compiler-and-runtime-nonzero-decisions-precede-cleanup
  (doseq [[failed-phase fact message]
          [[:compile :c11-compiler-acceptance "host C compiler rejected generated C11 source"]
           [:runtime :c-runtime-execution "generated C executable failed at runtime"]]]
    (let [events (atom []) seen (atom nil) failure (ex-info "unit refusal" {:id :unit})
          process! (fn [_ _ _ _ phase] (swap! events conj phase)
                     {:exit (if (= phase failed-phase) 7 0) :out "before-refusal" :err "diagnostic"})
          fail! (fn [& args] (swap! events conj :refusal) (reset! seen (vec args)) (throw failure))]
      (is (identical? failure
             (try (sequencing-unit true (Object.) process! fail!
                                   (fn [& _] (swap! events conj :cleanup)))
                  nil (catch clojure.lang.ExceptionInfo ex ex))))
      (is (= (if (= failed-phase :compile) [:compile :refusal :cleanup]
                 [:compile :runtime :refusal :cleanup]) @events))
      (is (= ["B2-DIALECT" message "sequencing-unit.gravity" :unit-target nil] (subvec @seen 0 5)))
      (is (= fact (get-in @seen [5 :missing-fact])))
      (is (= "/usr/bin/cc" (get-in @seen [5 :compiler])))
      (is (= 7 (get-in @seen [5 (if (= failed-phase :compile) :compile-result :runtime-result) :exit]))))))

(deftest compiler-call-preserves-primary-and-cleanup-identities
  (doseq [primary [(ex-info "unit primary" {:id :primary}) (Error. "unit fatal")]]
    (let [cleanup (Exception. "unit cleanup") events (atom [])]
      (is (identical? primary
             (try (sequencing-unit false (Object.)
                                   (fn [& _] (swap! events conj :compile) (throw primary))
                                   (fn [& _] (throw (Exception. "unexpected remapping")))
                                   (fn [& _] (swap! events conj :cleanup) (throw cleanup)))
                  nil (catch Throwable observed observed))))
      (is (= [:compile :cleanup] @events))
      (is (= 1 (alength (.getSuppressed ^Throwable primary))))
      (is (identical? cleanup (aget (.getSuppressed ^Throwable primary) 0)))))
  (let [cleanup (Exception. "cleanup only")]
    (is (identical? cleanup
           (try (sequencing-unit false (Object.) (fn [& _] {:exit 0})
                                 (fn [& _] (throw (Exception. "unexpected remapping")))
                                 (fn [& _] (throw cleanup)))
                nil (catch Throwable observed observed))))))

(deftest compiler-call-generic-exception-keeps-original-suppression-boundary
  (let [primary (java.io.IOException. "unit IO failure") cleanup (Exception. "unit cleanup")
        transport (ex-info "unit diagnostic transport" {:id :transport}) seen (atom nil)]
    (is (identical? transport
           (try (sequencing-unit false (Object.) (fn [& _] (throw primary))
                                 (fn [& args] (reset! seen (vec args)) (throw transport))
                                 (fn [& _] (throw cleanup)))
                nil (catch Throwable observed observed))))
    (is (= "host C compiler is unavailable" (get @seen 1)))
    (is (= :c11-compiler (get-in @seen [5 :missing-fact])))
    (is (= "unit IO failure" (get-in @seen [5 :cause-message])))
    (is (identical? cleanup (aget (.getSuppressed primary) 0)))
    (is (zero? (alength (.getSuppressed transport))))))

(deftest compiler-call-interruption-is-restored-before-cleanup
  (let [thread (Thread/currentThread) prior (.isInterrupted thread)
        interrupted (InterruptedException. "unit interruption") cleanup (Exception. "unit cleanup")
        during-cleanup (atom false)]
    (try
      (let [observed (try (sequencing-unit false (Object.) (fn [& _] (throw interrupted))
                                         (fn [& _] (throw (Exception. "unexpected remapping")))
                                         (fn [& _] (reset! during-cleanup (.isInterrupted thread)) (throw cleanup)))
                          nil (catch Throwable error error))
            ;; Capture before reporting an assertion through the test framework.
            interrupted-after-call? (.isInterrupted thread)]
        (is (identical? interrupted observed))
        (is @during-cleanup)
        (is interrupted-after-call?)
        (is (identical? cleanup (aget (.getSuppressed interrupted) 0))))
      (finally (Thread/interrupted) (when prior (.interrupt thread))))))

(deftest compiler-call-staging-refusal-precedes-process-and-cleanup
  (let [events (atom []) failure (ex-info "unit staging refusal" {:id :unit-staging})
        forbidden (fn [& _] (swap! events conj :unexpected-native-operation)
                    (throw (Exception. "unused operation invoked")))]
    (is (identical? failure
           (try (c/run-compiler! nil
                  (fn [source target] (swap! events conj [:staging source target]) (throw failure))
                  forbidden forbidden forbidden "unit.c" "unit-bin" "source.gravity" :unit-target true)
                nil (catch clojure.lang.ExceptionInfo observed observed))))
    (is (= [[:staging "source.gravity" :unit-target]] @events))))

(deftest failure-function-is-selected-before-cause-message
  (let [events (atom [])
        diagnostic-arguments (atom nil)
        original-diagnostic (ex-info "original diagnostic" {:test :original})
        late-diagnostic (ex-info "late diagnostic" {:test :late})
        source "source.gravity"
        target :native
        cause (proxy [java.io.IOException] []
                (getMessage []
                  (swap! events conj :cause-message)
                  (alter-var-root
                   #'bootstrap/c-backend-fail!
                   (constantly (fn [& _]
                                 (swap! events conj :late-diagnostic)
                                 (throw late-diagnostic))))
                  "observed cause"))]
    (with-redefs [bootstrap/c-backend-fail!
                  (fn [& arguments]
                    (reset! diagnostic-arguments arguments)
                    (swap! events conj :original-diagnostic)
                    (throw original-diagnostic))]
      (let [result (try
                     (c/run-compiler! nil (fn [& _] (Object.))
                                      (fn [& _] (throw cause))
                                      #'bootstrap/c-backend-fail!
                                      (fn [& _] (swap! events conj :cleanup))
                                      "target/probe.c" "target/probe" source target false)
                     (catch Throwable error error))]
        (is (identical? original-diagnostic result))
        (is (= [:cause-message :original-diagnostic :cleanup] @events))
        (is (= ["B2-DIALECT" "host C compiler is unavailable" source target nil
                {:compiler "/usr/bin/cc" :cause-message "observed cause"
                 :missing-fact :c11-compiler}]
               (vec @diagnostic-arguments)))))))
