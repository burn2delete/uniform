(ns gravity.self-hosting.sh01-typed-lowering-work-policy-test
  "Trusted caller policy at the existing typed-lowering interface.

  Bridge units mock execution and establish only argument/data flow. The
  current-source tests construct real upstream products and execute current
  Gravity C7 and lowering afresh; they are ordinary development, not migration."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]))

(def ^:private options {:type-inference-work-allowance 1048576})
(def ^:private fixture
  "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-work-policy-minimal-map-get.gravity")

(defn- failure [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- stage [label operation]
  (println :typed-work-policy-before label)
  (flush)
  (let [started (System/nanoTime) result (operation)]
    (println :typed-work-policy-after label (- (System/nanoTime) started))
    (flush)
    result))

(defn- existing [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Required existing C7 helper is absent" {:name name}))))

(defn- current-products []
  (let [path (.getCanonicalPath (io/file fixture)) text (slurp path)
        products ((existing 'function-work-policy-current-source)
                  path text {:artifact :gravity/sh06-module-inputs
                             :schema-version 1 :module-artifacts []} stage)
        invoke (existing 'invoke-c7)
        typed (:result (stage :c7-producer
                              #(invoke 'sh08-function-type-core-artifact-with-work-policy
                                       [(:request products) options])))
        verification (:verification
                      (stage :c7-input-verification
                             #(invoke 'sh08-verify-function-type-result-with-work-policy
                                      [(:request products) typed options])))
        input {:request (:request products) :typed typed :verification verification}
        macro-artifact (get-in input [:request :b47-context :authenticated-wrapper
                                     :sh06-resolution-artifact :sh05-macro-artifact])
        reader (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact
                                       :c2-reader-artifact])
        module (assoc (bootstrap/parse-module path (:parsed-semantic-values reader))
                      :forms (:expanded-forms macro-artifact))
        definitions (bootstrap/stage0-function-table module)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))]
    (is (= :accepted (:status typed)))
    (is (= :passed (:status verification)))
    {:input input :module (assoc module :function-table definitions)
     :definition (get definitions 'kind) :ordered (vec (sort-by key definitions))
     :path path :text text :emitter emitter}))

(defn- observe-current-c7 [observed operation]
  ;; Passive instrumentation: every call delegates to the original executor.
  (let [execute bootstrap/p15-s23-stage2-runtime-execute-function]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [runner plan function arguments]
                    (let [result (execute runner plan function arguments)]
                      (when (contains? '#{sh08-verify-function-type-result
                                          sh08-verify-function-type-result-with-work-policy} function)
                        (swap! observed conj {:entrypoint function
                                              :options (if (= 3 (count arguments)) (last arguments) {})
                                              :source (:source plan)
                                              :verification (if (= 3 (count arguments))
                                                              (:verification result) result)}))
                      result))]
      (operation))))

(deftest bridge-unit-forwards-only-caller-options-and-unwraps-fresh-verification
  ;; These mocks are transport oracles, not accepted compiler products.
  (let [input (-> {:typed {:status :accepted :effective-work-policy :candidate-policy}
                   :verification {:status :passed :effective-work-policy :supplied-policy}}
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact
                             :sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact
                             :c2-reader-artifact :source-unit-record :path] "unit.gravity"))
        calls (atom []) observations (atom []) reads (atom [])
        execute (fn [_ plan function arguments]
                  (if (contains? '#{sh08-verify-function-type-result
                                    sh08-verify-function-type-result-with-work-policy} function)
                    (let [report {:status :passed :fresh-invocation (inc (count @calls))}]
                      (swap! calls conj {:entrypoint function :source (:source plan)
                                         :arguments arguments :report report})
                      (if (= 3 (count arguments))
                        {:effective-work-policy :untrusted-envelope :verification report}
                        report))
                    (do (swap! observations conj {:function function :arguments arguments})
                        {:status :complete :value :lowered-unit})))
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "policy hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "policy equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "policy spelling"))))
        policies [{} options nil [] {:type-inference-work-allowance 0} {:unknown 9} :legacy opaque]]
    (with-redefs [bootstrap/read-gravity-source-text
                  (fn [path] (swap! reads conj path) "current-source")
                  bootstrap/p15-s23-stage2-compiler-artifact-plan
                  (fn [_ path text] {:source {:path path :sha256 text} :plan-id path})
                  bootstrap/c2-reader-artifact-source-text (fn [_ _] "unit source")
                  bootstrap/p15-s23-stage2-runtime-execute-function execute]
      (doseq [policy policies]
        (is (= :lowered-unit
               (bootstrap/p15-s23-stage2-compile-function {} {} :definition input policy)))
        (is (= :lowered-unit
               (bootstrap/p15-s23-stage2-assemble-plan-products {} {} [] input policy))))
      (is (= :lowered-unit (bootstrap/p15-s23-stage2-compile-function {} {} :definition input)))
      (is (= :lowered-unit (bootstrap/p15-s23-stage2-assemble-plan-products {} {} [] input))))
    (is (= (* 2 (count policies)) (count (drop-last 2 @calls))))
    (is (every? true? (map identical? (mapcat #(vector % %) policies)
                          (map #(last (:arguments %)) (drop-last 2 @calls)))))
    (is (zero? @callbacks))
    (is (every? #(= 'sh08-verify-function-type-result-with-work-policy (:entrypoint %))
                (drop-last 2 @calls)))
    (is (every? #(= 'sh08-verify-function-type-result (:entrypoint %)) (take-last 2 @calls)))
    (is (= [2 2] (mapv #(count (:arguments %)) (take-last 2 @calls))))
    (is (= (* 2 (count @calls)) (count @reads)))
    (doseq [[call observation] (map vector @calls @observations)]
      (is (= [(:request input) (:typed input)] (take 2 (:arguments call))))
      (is (identical? input (nth (:arguments observation) 3)))
      (is (= (:report call) (get-in observation [:arguments 4 :verification])))
      (is (= #{:verification :source :compiler-sources}
             (set (keys (nth (:arguments observation) 4))))))))

(deftest bridge-unit-preserves-untyped-entrypoint-arguments-and-results
  (let [calls (atom [])]
    (with-redefs [bootstrap/p15-s23-stage2-compiler-artifact-invoke
                  (fn [function arguments]
                    (swap! calls conj [function arguments])
                    {:status :complete :value :legacy})]
      (is (= :legacy (bootstrap/p15-s23-stage2-compile-function :emitter {} :definition)))
      (is (= :legacy (bootstrap/p15-s23-stage2-assemble-plan-products :emitter {} :definitions))))
    (is (= [['p15-s23-compile-function [:emitter {} :definition]]
            ['p15-s23-assemble-plan-products [:emitter {} :definitions]]] @calls))))

(deftest observer-unit-distinguishes-legacy-report-and-policy-envelope
  (let [report {:status :passed} observed (atom [])]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [_ _ _ args]
                    (if (= 2 (count args)) report
                        {:verification report :effective-work-policy :outside-report}))]
      (doseq [[function args] [['sh08-verify-function-type-result [:request :typed]]
                              ['sh08-verify-function-type-result-with-work-policy
                               [:request :typed options]]]]
        (observe-current-c7 observed
          #(bootstrap/p15-s23-stage2-runtime-execute-function {} {:source :current} function args))))
    (is (= [report report] (mapv :verification @observed)))
    (is (= [{} options] (mapv :options @observed)))))

(deftest current-gravity-selected-policy-preserves-default-lowering-products
  (let [{:keys [input module definition ordered emitter path text]} (current-products)
        observed (atom [])
        invoke (fn [label operation] (stage label #(observe-current-c7 observed operation)))
        default (invoke :default-function
                        #(bootstrap/p15-s23-stage2-compile-function emitter module definition input))
        assembled (invoke :explicit-default-assembly
                          #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module ordered input {}))
        selected (get-in assembled [:functions 'kind])]
    (is (= #{:request :typed :verification} (set (keys input))))
    (is (= default selected))
    (is (= (pr-str default) (pr-str selected)))
    (is (seq (:instructions selected)))
    (is (= [{} {}] (mapv :options @observed)))
    (is (= '[sh08-verify-function-type-result sh08-verify-function-type-result-with-work-policy]
           (mapv :entrypoint @observed)))
    (is (= 2 (count @observed)))
    (doseq [{:keys [source verification]} @observed]
      (is (true? (= (:verification input) verification)))
      (is (true? (= (:typed input) (:expected verification) (:candidate verification))))
      (is (= (str "sha256:" (bootstrap/sha256-hex
                             (slurp "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")))
             (:sha256 source))))
    (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))}
           (get-in selected [:typed-lowering-provenance :source])))
    (is (= :non-authoritative (get-in selected [:typed-lowering-provenance :authority])))
    (is (not (contains? (:typed input) :effective-work-policy)))))

(defn- check-current-rejections [policy?]
  (let [{:keys [input module definition emitter]} (current-products)
        observed (atom [])
        cases (if policy?
                [[:tiny module input {:type-inference-work-allowance 1} :rejected]
                 [:malformed module input {:unknown 1} :rejected]
                 [:candidate-policy module (assoc-in input [:typed :effective-work-policy] options) {} :rejected]]
                [[:input-policy module (assoc input :effective-work-policy options) {} :passed]
                 [:supplied-policy module (assoc-in input [:verification :effective-work-policy] options) {} :passed]
                 [:source-join (assoc module :source-text "substituted source") input options :passed]])]
    (doseq [[label candidate-module candidate-input policy status] cases]
      (let [before (count @observed)
            rejected (stage label (fn [] (observe-current-c7
                                         observed
                                         (fn [] (failure
                                                 (fn [] (bootstrap/p15-s23-stage2-compile-function
                                                         emitter candidate-module definition candidate-input policy)))))))]
        (is (= "P15S23Q002" (:id rejected)) (pr-str [label rejected]))
        (is (= :typed-input-source-or-verification-mismatch (:reason rejected)))
        (is (= 1 (- (count @observed) before)))
        (is (= policy (:options (last @observed))))
        (is (= status (get-in (last @observed) [:verification :status])))))
    (when policy?
      (is (some #(= :structural-call-index-work-limit (:reason %))
                (get-in (first @observed) [:verification :expected :diagnostics])))
      (is (= :invalid-type-inference-work-policy
             (get-in (second @observed) [:verification :diagnostics 0 :reason]))))))

(deftest current-gravity-rejects-small-malformed-and-candidate-policies
  (check-current-rejections true))

(deftest current-gravity-rejects-input-report-policy-and-source-substitution
  (check-current-rejections false))
