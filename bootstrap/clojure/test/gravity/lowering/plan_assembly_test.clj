(ns gravity.lowering.plan-assembly-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.set :as set]
            [gravity.bootstrap :as b]
            [gravity.compiler-pass-manifest :as manifest]
            [gravity.digest :as digest]
            [gravity.optimization :as optimization]
            [gravity.lowering :as lowering]))

(def ^:private operations
  {:function-table #'b/stage0-function-table :profile! #'b/validate-stage0-compiled-profile!
   :executable-profile! #'b/executable-profile! :effects! #'b/validate-module-effects!
   :safety! #'b/validate-stage0-executable-safety! :performance! #'b/validate-stage0-compiled-performance!
   :math! #'b/validate-stage0-compiled-math! :compiler! #'b/validate-stage0-compiled-compiler!
   :backend! #'b/validate-stage0-compiled-backend! :runtime! #'b/validate-stage0-compiled-runtime!
   :domain! #'b/validate-stage0-compiled-domain! :schema! #'b/validate-stage0-compiled-schema!
   :ai! #'b/validate-stage0-compiled-ai! :package! #'b/validate-stage0-compiled-package!
   :tooling! #'b/validate-stage0-compiled-tooling! :conformance! #'b/validate-stage0-compiled-conformance!
   :compile-function #'b/compile-stage0-function :instruction-summary #'b/stage0-instruction-summary
   :fail! #'b/fail! :sha256-hex #'b/sha256-hex})
(def ^:private gates
  [:profile! :executable-profile! :effects! :safety! :performance! :math! :compiler!
   :backend! :runtime! :domain! :schema! :ai! :package! :tooling! :conformance!])
(def ^:private module {:module 'plan.native :source-path "native.gravity" :forms []
                      :profile :hosted :target :jvm :effects #{:io/write}
                      :capabilities #{:io/stdout} :providers {:stdout :hosted} :safety :safe})
(def ^:private main-definition {:name 'main :params [] :arity 0 :instructions [{:op :literal :value 1}]})
(defn- fixture [table overrides run]
  (let [events (atom [])
        defaults
        (merge (into {} (for [gate gates] [gate (fn [& args] (swap! events conj [gate (vec args)]) :ignored)]))
               {:function-table (fn [input] (swap! events conj [:table input]) table)
                :compile-function (fn [_ definition]
                                    (swap! events conj [:compile (:name definition)])
                                    (assoc definition :binding {:name (:name definition)}))
                :instruction-summary (fn [instruction]
                                       (swap! events conj [:summary (:op instruction)])
                                       {(:op instruction) 1})
                :fail! (fn [id message facts] (swap! events conj [:failure id facts])
                         (throw (ex-info message (assoc facts :id id))))
                :sha256-hex (fn [text] (swap! events conj [:hash text]) (digest/sha256-hex text))})
        replacements (merge defaults overrides)]
    (with-redefs-fn (into {} (for [[key operation] replacements] [(get operations key) operation]))
      #(run events))))
(defn- assemble [] (lowering/assemble-native-stage0-plan operations "native.gravity" "source" module))
(defn- error-data [run] (try (run) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(def ^:private source-header
  "(ns plan.assembly (:profile :hosted) (:target :jvm) (:effects #{:io/write}) (:capabilities #{:io/stdout}) (:safety :safe))\n")
(defn- compile-source [path source]
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (let [expanded (b/macro-source-artifact path source)
        input (assoc (:module expanded) :forms (:expanded-forms expanded))]
    {:module input :plan (b/stage0-compiled-core-plan path source input)}))

(deftest plan-boundary-is-explicit-and-non-authenticating
  (let [contract (:gravity/namespace-contract (meta #'lowering/assemble-native-stage0-plan))
        entry (get-in contract [:public-api 'assemble-native-stage0-plan])]
    (is (= 25 (count (ns-publics 'gravity.lowering)) (count (:public-api contract))))
    (is (= (:arglists (meta #'lowering/assemble-native-stage0-plan)) (:arglists entry)))
    (is (= :stage0-native-plan-assembly (:contract-boundary entry)))
    (is (= (set (keys operations)) (set (:required-operations entry))))
    (is (= 20 (count operations)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (some #{'D0} (:governing-contracts contract)))
    (is (some #{'C18} (:governing-contracts contract)))))

(deftest policy-calls-retain-exact-order-and-module-context
  (let [table {'main main-definition}]
    (fixture table {}
             (fn [events]
               (let [plan (assemble) context (assoc module :function-table table)
                     calls (filter #(contains? (set gates) (first %)) @events)]
                 (is (= gates (mapv first calls)))
                 (is (= [:table module] (first @events)))
                 (doseq [[gate args] calls]
                   (is (= (if (= :executable-profile! gate) ["native.gravity" context []] [context]) args)))
                 (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
                 (is (= 'main (:entrypoint plan)))
                 (is (= module (assoc (:module plan) :forms [])))
                 (is (= (str "sha256:" (digest/sha256-hex "source")) (get-in plan [:source :sha256])))
                 (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
                 (is (= #{:io/write} (get-in plan [:effect-summary :declared])))
                 (is (= #{} (get-in plan [:effect-summary :inferred]))))))))

(deftest every-policy-failure-stops-before-compilation-and-hashing
  (doseq [[index gate] (map-indexed vector gates)]
    (let [sentinel (ex-info "policy" {:gate gate})]
      (fixture {'main main-definition} {gate (fn [& _] (throw sentinel))}
               (fn [events]
                 (is (identical? sentinel (try (assemble) (catch clojure.lang.ExceptionInfo error error))))
                 (is (= (subvec gates 0 index) (mapv first (rest @events))))
                 (is (not-any? #{:compile :summary :hash} (map first @events))))))))

(deftest policy-vars-are-selected-at-their-original-use
  (let [late-calls (atom [])]
    (fixture {'main main-definition}
             {:profile! (fn [_] (alter-var-root #'b/validate-module-effects!
                                              (constantly (fn [input] (swap! late-calls conj input)))))
              :effects! (fn [& _] (throw (Exception. "stale effects callback")))}
             (fn [_]
               (is (= :gravity/stage0-hosted-core-compiled-plan (:kind (assemble))))
               (is (= [(assoc module :function-table {'main main-definition})] @late-calls))))))

(deftest function-compilation-order-and-late-replacement-are-preserved
  (let [table (array-map 'zeta (assoc main-definition :name 'zeta) 'main main-definition)
        compiled (atom [])
        late (fn [_ definition] (swap! compiled conj [:late (:name definition)])
               (assoc definition :binding {:name (:name definition)} :late true))]
    (fixture table
             {:compile-function (fn [_ definition]
                                  (swap! compiled conj [:first (:name definition)])
                                  (alter-var-root #'b/compile-stage0-function (constantly late))
                                  (assoc definition :binding {:name (:name definition)}))}
             (fn [_]
               (let [plan (assemble)]
                 (is (= [[:first 'zeta] [:late 'main]] @compiled))
                 (is (= ['main 'zeta] (vec (keys (:functions plan)))))
                 (is (= ['main 'zeta] (mapv :name (:binding-table plan))))
                 (is (true? (get-in plan [:functions 'main :late]))))))))

(deftest summary-map-retains-its-selected-function-across-children
  (let [seen (atom []) definition (assoc main-definition :instructions [{:op :a} {:op :b}])]
    (fixture {'main definition}
             {:instruction-summary (fn [instruction]
                                     (swap! seen conj (:op instruction))
                                     (alter-var-root #'b/stage0-instruction-summary (constantly (fn [_] {:late 1})))
                                     {:selected 1})}
             (fn [_]
               (is (= {:selected 2} (:instruction-summary (assemble))))
               (is (= [:a :b] @seen))
               (is (= {:late 1} (b/stage0-instruction-summary {:op :after})))))))

(deftest digest-callee-is-selected-before-plan-serialization
  (let [calls (atom []) late (fn [_] (swap! calls conj :late) "late")
        carrier (proxy [Object] [] (toString [] (alter-var-root #'b/sha256-hex (constantly late)) "carrier"))
        definition (assoc main-definition :opaque carrier)
        selected (fn [text] (swap! calls conj :selected) (digest/sha256-hex text))]
    (fixture {'main definition} {:sha256-hex selected}
             (fn [_]
               (let [plan (assemble)]
                 (is (= [:selected :selected] @calls))
                 (is (identical? late (var-get #'b/sha256-hex)))
                 (is (identical? carrier (get-in plan [:functions 'main :opaque])))
                 (is (string? (:plan-id plan))))))))

(deftest source-and-failure-values-retain-opaque-identity
  (let [source-path (Object.) sentinel (ex-info "entry failure" {}) captured (atom nil)]
    (fixture {}
             {:fail! (fn [id message facts] (reset! captured [id message facts]) (throw sentinel))}
             (fn [_]
               (is (identical? sentinel
                               (try (lowering/assemble-native-stage0-plan operations source-path "source" module)
                                    (catch clojure.lang.ExceptionInfo error error))))
               (is (= "L3-UNKNOWN-ALIAS" (first @captured)))
               (is (identical? source-path (get-in @captured [2 :source-span :source])))))))

(deftest returning-failure-values-do-not-create-new-admission
  (let [refusals (atom [])]
    (fixture {} {:fail! (fn [id & _] (swap! refusals conj id) false)}
             (fn [_]
               (let [plan (assemble)]
                 (is (= {} (:functions plan)))
                 (is (= ['main "L3-UNKNOWN-ALIAS"] [(:entrypoint plan) (first @refusals)])
                     "returning callbacks are compatibility data, not successful admission"))))))

(deftest real-source-plans-and-qualified-reports-are-deterministic
  (doseq [path ["examples/hello.gravity" "examples/hello.qst" "examples/core-app.gravity" "examples/core-app.qst"]]
    (testing path
      (let [source (slurp path :encoding "UTF-8") {:keys [plan module]} (compile-source path source)
            report (b/stage0-compiled-plan-compiler-report plan module)
            emit (first (filter #(= :stage0-emit-instruction-plan (:pass %)) (get-in report [:pass-contract-report :contracts])))]
        (is (= plan (:plan (compile-source path source))))
        (is (= path (get-in plan [:source :path])))
        (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
        (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
        (is (= (:plan-id plan) (:compiled-plan-id report)))
        (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc report :report-id)))) (:report-id report)))
        (is (not (contains? (:preserves emit) :source-spans)))
        (is (every? (:preserves emit) [:source-path :source-content-hash]))
        (is (contains? (:unsupported-preservation emit) :instruction-source-spans))
        (is (contains? (get-in report [:provenance-boundary :unsupported]) :generated-origin-chains))
        (is (false? (get-in report [:provenance-boundary :authenticates-source?])))
        (is (= (if (.contains path "core-app") "core-app\ngravity:19:2\n(:ok 19)\n" "Hello Gravity\n")
               (b/execute-stage0-compiled-plan plan)))))))

(deftest real-entry-refusals-follow-function-compilation
  (doseq [[body expected] [["(defn helper [] 1)" "L3-UNKNOWN-ALIAS"]
                           ["(defn main [value] value)" "L2-MAIN-ARITY"]]]
    (let [compiled (atom []) original b/compile-stage0-function path "plan-entry.gravity"
          data (with-redefs [b/compile-stage0-function (fn [input definition]
                                                       (swap! compiled conj (:name definition))
                                                       (original input definition))]
                 (error-data #(compile-source path (str source-header body "\n"))))]
      (is (= expected (:id data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= 1 (count @compiled))))))

(deftest real-executor-refusals-remain-downstream-of-plan-emission
  (doseq [[path expected] [["bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity" "L2-FUNCTION-ARITY"]
                           ["bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity" "L2-BUILTIN-ARITY"]]]
    (let [plan (:plan (compile-source path (slurp path :encoding "UTF-8")))
          data (error-data #(b/execute-stage0-compiled-plan plan))]
      (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
      (is (= expected (:id data)))
      (is (= path (get-in data [:source-span :source]))))))

(deftest real-header-and-policy-refusals-stop-before-function-compilation
  (doseq [[path source expected]
          [["plan-duplicate.gravity" (str source-header "(defn main [] 1)\n(defn main [] 2)\n") "L2-DUPLICATE-FUNCTION"]
           ["bootstrap/clojure/fixtures/rejected/core-app-compiler-evidence-drop.gravity"
            (slurp "bootstrap/clojure/fixtures/rejected/core-app-compiler-evidence-drop.gravity" :encoding "UTF-8")
            "C1-EVIDENCE-DROP"]]]
    (let [calls (atom 0) original b/compile-stage0-function
          data (with-redefs [b/compile-stage0-function (fn [& args] (swap! calls inc) (apply original args))]
                 (error-data #(compile-source path source)))]
      (is (= expected (:id data)))
      (is (= path (get-in data [:source-span :source])))
      (is (zero? @calls)))))

(def ^:private legacy-compiler-gate
  {:pass-contracts
   [{:pass :native/compile :owner-doc :C1 :input :macro-expanded-core
     :output :stage0-instruction-plan :requires #{} :preserves #{:profile :target}
     :invalidates #{:types :source-spans} :regenerates #{:types} :capabilities #{}
     :profiles #{:hosted} :emits #{:source-spans} :rejects #{"L2-MAIN-ARITY"}
     :verifier-gate? true :risk :high :evidence-class #{:fixture}}]
   :verification-risk
   [{:pass :native/compile :risk :high :artifact-id :native/pass
     :minimum-evidence #{:fixture :translation-validation}
     :available-evidence #{:fixture :translation-validation}}
    {:pass :native/recheck :risk :critical :version "native-v1" :artifact-id :native/recheck
     :minimum-evidence #{:fixture} :available-evidence #{:fixture}}]})

(deftest real-legacy-compiler-gates-emit-deterministic-plans-with-native-metadata
  ;; Matching labels are metadata, not authenticated verification evidence.
  (doseq [extension [".gravity" ".qst"]
          [label compiler present?] [["nonempty" {:compiled-gate legacy-compiler-gate} true]
                                     ["empty" {:compiled-gate {}} true]
                                     ["nil" {:compiled-gate nil} true]
                                     ["absent" {} false]]]
    (let [path (str "legacy-compiler-" label extension)
          source (str "(ns compiler.native (:profile :hosted) (:target :jvm) (:effects #{:io/write}) "
                      "(:capabilities #{:io/stdout}) (:metadata " (pr-str {:compiler compiler}) "))\n"
                      "(defn main [] (println (+ 2 3)))\n")
          {:keys [module plan]} (compile-source path source)
          report (b/stage0-compiled-plan-compiler-report plan module)]
      (is (= present? (b/stage0-compiled-compiler-suite-present? module)))
      (is (= (get compiler :compiled-gate {}) (b/stage0-compiled-compiler-suite module)))
      (is (= {:profile :hosted :target :jvm :source-effects #{:io/write}
              :source-capabilities #{:io/stdout} :metadata {:compiler compiler}}
             (b/stage0-compiled-compiler-manifest module)))
      (is (= plan (:plan (compile-source path source))))
      (is (= path (get-in plan [:source :path])))
      (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
      (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
      (is (= (:plan-id plan) (:compiled-plan-id report)))
      (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc report :report-id)))) (:report-id report)))
      (is (= {:emitted? false :reason :stage0-instruction-plan-path
              :target-specific-opcode-policy :rejected :required-next :build-verified-mir}
             (:mir-status report)))
      (is (= "5\n" (b/run-main module) (b/execute-stage0-compiled-plan plan))))))

(deftest real-legacy-compiler-refusals-retain-family-and-stop-before-compilation
  (doseq [extension [".gravity" ".qst"]
          [suffix expected family] [["pass-contract" "C1-PASS-CONTRACT" :compiler-pass-contract]
                                    ["evidence-drop" "C1-EVIDENCE-DROP" :compiler-pass-contract]
                                    ["verification-evidence" "C18-EVIDENCE" :compiler-pass-correctness]
                                    ["mir-target-leak" "C11-TARGET-LEAK" nil]
                                    ["lowering-input" "C14-INPUT" nil]
                                    ["proof-metadata" "C14-PROOF-METADATA" nil]]]
    (let [path (str "legacy-compiler-" suffix extension)
          source (slurp (str "bootstrap/clojure/fixtures/rejected/core-app-compiler-" suffix ".gravity") :encoding "UTF-8")
          calls (atom 0) original b/compile-stage0-function
          failure (with-redefs [b/compile-stage0-function (fn [& args] (swap! calls inc) (apply original args))]
                    (error-data #(compile-source path source)))]
      (is (= expected (:id failure)))
      (when family (is (= family (:diagnostic-family failure))))
      ;; C11's explicit fixture span is preserved rather than replaced by this caller path.
      (when (contains? #{"C14-INPUT" "C14-PROOF-METADATA"} expected)
        (is (= path (get-in failure [:source-span :source]))))
      (is (zero? @calls)))))

(deftest legacy-compiler-adapters-forward-original-arguments-and-live-vars
  (let [input (Object.) projection (Object.) record (Object.) marker (Object.) calls (atom [])]
    (with-redefs [manifest/native-stage0-compiler-manifest (fn [& args] (swap! calls conj [:manifest args]) marker)
                  manifest/native-stage0-compiler-suite (fn [& args] (swap! calls conj [:suite args]) marker)
                  manifest/native-stage0-compiler-suite-present? (fn [& args] (swap! calls conj [:present args]) marker)
                  manifest/validate-native-stage0-pass-contract! (fn [& args] (swap! calls conj [:c1 args]) marker)
                  manifest/validate-native-stage0-verification-risk! (fn [& args] (swap! calls conj [:c18 args]) marker)]
      (is (identical? marker (b/stage0-compiled-compiler-manifest input)))
      (is (identical? marker (b/stage0-compiled-compiler-suite input)))
      (is (identical? marker (b/stage0-compiled-compiler-suite-present? input)))
      (is (identical? marker (b/validate-stage0-compiled-pass-contract! input projection record)))
      (is (identical? marker (b/validate-stage0-compiled-verification-risk! input record)))
      (is (= [:manifest :suite :present :c1 :c18] (mapv first @calls)))
      (doseq [[_ args] (take 3 @calls)] (is (identical? input (first args))))
      (let [[c1 module manifest contract] (second (nth @calls 3))
            [c18 risk-module risk] (second (nth @calls 4))]
        (is (= {:missing-fields #'b/compiler-pass-missing-fields
                :required-fields #'b/compiler-pass-contract-required-fields
                :durable-facts #'b/compiler-pass-durable-facts :intersection #'set/intersection
                :difference #'set/difference :fail! #'b/compiler-pass-fail!} c1))
        (is (= {:subset? #'set/subset? :difference #'set/difference :fail! #'b/c18-verification-fail!} c18))
        (is (identical? input module))
        (is (identical? projection manifest))
        (is (identical? record contract))
        (is (identical? input risk-module))
        (is (identical? record risk))))))

(deftest legacy-compiler-aggregate-retains-cross-stage-order
  ;; Native orchestration fixture; this does not admit a source artifact.
  (let [pass (Object.) mir (Object.) target (Object.) risk (Object.) events (atom [])
        input {:metadata {:compiler {:compiled-gate {:pass-contracts [pass] :mir-operations [mir]
                                                    :target-lowering [target] :verification-risk [risk]}}}}]
    (with-redefs [b/validate-stage0-compiled-pass-contract! (fn [module _ record] (swap! events conj [:c1 module record]))
                  b/validate-stage0-compiled-mir! (fn [module _ record] (swap! events conj [:c11 module record]))
                  b/validate-stage0-compiled-target-lowering! (fn [module record] (swap! events conj [:c14 module record]))
                  b/validate-stage0-compiled-verification-risk! (fn [module record] (swap! events conj [:c18 module record]))]
      (is (nil? (b/validate-stage0-compiled-compiler! input)))
      (is (= [:c1 :c11 :c14 :c18] (mapv first @events)))
      (doseq [[[_ module actual] expected] (map vector @events [pass mir target risk])]
        (is (identical? input module))
        (is (identical? expected actual))))))

(def ^:private elision-marker-records
  (conj (mapv (fn [field] {:erased? true :check-class (keyword "native" (name field))
                           :operation :fixture/op :ir-node :fixture/node field :native/marker})
              [:proof-id :proof :certificate :certificate-id :dominating-proof])
        {:erased? false :erased-checks [:native/check] :proof-id :native/marker}))
(defn- elision-source [performance]
  (str "(ns elision.native (:profile :hosted) (:target :jvm) (:effects #{:io/write}) "
       "(:capabilities #{:io/stdout}) (:safety :safe) (:metadata " (pr-str {:performance performance}) "))\n"
       "(defn main [] (println (+ 2 3)))\n"))

(deftest real-elision-metadata-gates-run-before-deterministic-plan-execution
  (doseq [extension [".gravity" ".qst"]
          [label records] [["empty" []] ["markers" elision-marker-records]
                           ["nonerased" [{:erased? false :erased-checks []}]]
                           ["empty-string-is-present" [{:erased? true :certificate-id ""}]]]]
    (let [path (str "elision-" label extension) source (elision-source {:check-elision records})
          seen (atom []) original b/validate-stage0-compiled-check-elision!
          {:keys [module plan]}
          (with-redefs [b/validate-stage0-compiled-check-elision!
                        (fn [input record] (swap! seen conj [input record]) (original input record))]
            (compile-source path source))
          report (b/stage0-compiled-plan-performance-report plan)]
      (is (= records (mapv second @seen)))
      (doseq [[input _] @seen]
        (is (= path (:source-path input)))
        (is (= records (get-in input [:metadata :performance :check-elision]))))
      (is (= records (get-in module [:metadata :performance :check-elision])))
      (is (= plan (:plan (compile-source path source))))
      (is (= path (get-in plan [:source :path])))
      (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
      (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
      (is (= (:plan-id plan) (:compiled-plan-id report)))
      (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc report :report-id)))) (:report-id report)))
      (is (= :none (get-in report [:performance-contract-manifest :optimization-mode])))
      (is (= :retain-safe-semantics (:decision (last (:optimization-decision-log report)))))
      ;; The existing plan projection omits metadata. Do not inject it to
      ;; manufacture report coverage or claim certificate-backed check removal.
      (is (not (contains? (:module plan) :metadata)))
      (is (= :no-checks-elided (get-in report [:check-elision-report :status])))
      (is (= "5\n" (b/run-main module) (b/execute-stage0-compiled-plan plan))))))

(deftest real-elision-and-performance-refusals-stop-before-function-compilation
  (doseq [extension [".gravity" ".qst"]
          [label source expected]
          (concat
           (for [[name id] [["elision" "PERF10-PROOF-MISSING"] ["claim" "PERF1-CLAIM"] ["target" "PERF1-TARGET"]]]
             [name (slurp (str "bootstrap/clojure/fixtures/rejected/core-app-performance-" name ".gravity") :encoding "UTF-8") id])
           [["erased-checks" (elision-source {:check-elision [{:erased? false :erased-checks [:bounds]}]})
             "PERF10-PROOF-MISSING"]
            ["empty-collections" (elision-source {:check-elision [{:erased? true :proof-id nil :proof []
                                                                  :certificate {} :certificate-id nil :dominating-proof #{}}]})
             "PERF10-PROOF-MISSING"]])]
    (let [path (str "elision-refusal-" label extension) calls (atom 0) original b/compile-stage0-function
          failure (with-redefs [b/compile-stage0-function (fn [& args] (swap! calls inc) (apply original args))]
                    (error-data #(compile-source path source)))]
      (is (= expected (:id failure)))
      (is (= path (get-in failure [:source-span :source])))
      (is (zero? @calls)))))

(deftest real-perf1-claim-refusal-precedes-elision-records
  (doseq [extension [".gravity" ".qst"]]
    (let [path (str "elision-claim-order" extension)
          source (elision-source {:claim {:claim-id :native/incomplete}
                                  :check-elision [{:erased? true}]})
          calls (atom [])
          failure (with-redefs [b/validate-stage0-compiled-check-elision! (fn [& _] (swap! calls conj :elision))
                                b/compile-stage0-function (fn [& _] (swap! calls conj :compile))]
                    (error-data #(compile-source path source)))]
      (is (= "PERF1-CLAIM" (:id failure)))
      (is (= path (get-in failure [:source-span :source])))
      (is (empty? @calls)))))

(deftest elision-bootstrap-adapters-forward-original-objects-and-live-vars
  (let [input (Object.) record (Object.) marker (Object.) calls (atom [])]
    (with-redefs [optimization/native-stage0-check-elision-proof-present?
                  (fn [& args] (swap! calls conj [:presence (vec args)]) marker)
                  optimization/validate-native-stage0-check-elision!
                  (fn [& args] (swap! calls conj [:validation (vec args)]) marker)]
      (is (identical? marker (b/stage0-compiled-check-elision-proof-present? record)))
      (is (identical? marker (b/validate-stage0-compiled-check-elision! input record)))
      (is (= [:presence :validation] (mapv first @calls)))
      (is (identical? #'b/perf-present? (get-in @calls [0 1 0])))
      (is (identical? record (get-in @calls [0 1 1])))
      (is (identical? #'b/fail! (get-in @calls [1 1 0])))
      (is (identical? #'b/stage0-compiled-check-elision-proof-present? (get-in @calls [1 1 1])))
      (is (identical? input (get-in @calls [1 1 2])))
      (is (identical? record (get-in @calls [1 1 3]))))))
