(ns gravity.effects.stage0-policy-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest]
            [gravity.effects :as effects]
            [gravity.module-analysis.effects :as legacy]))

(defn- observed-error [run] (try (run) nil (catch Throwable error error)))
(defn- source-module [path source]
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (let [expanded (b/macro-source-artifact path source)]
    (assoc (:module expanded) :forms (:expanded-forms expanded))))
(def ^:private missing-module
  {:source-path "policy.gravity" :forms ['(println "value")]
   :effects #{} :capabilities #{}})
(defn- defaults [] {:uses-println? b/uses-println? :fail! b/fail!})

(deftest contract-separates-callback-values-from-authority
  (let [names '[stage0-uses-println? infer-stage0-effects required-stage0-capabilities validate-stage0-module-effects!]
        contract (:gravity/namespace-contract (meta #'effects/validate-stage0-module-effects!))
        public (ns-publics 'gravity.effects)]
    (is (= 34 (count public) (count (:public-api contract))))
    (is (= 27 (count (filter (fn [[_ operation]] (fn? @operation)) public))))
    (is (= 7 (count (remove (fn [[_ operation]] (fn? @operation)) public))))
    (is (every? (set (:governing-contracts contract)) '[D0 D1 D2 D3 D6 D8 D9 L3 L6 L15 P4 C1 C8 C15 BOOT1 BOOT8 TEST2]))
    (doseq [name names :let [operation (ns-resolve 'gravity.effects name)
                            api (get-in contract [:public-api name])]]
      (is (= (:arglists (meta operation)) (:arglists api)))
      (is (= :stage0-native-effect-policy-kernel (:contract-boundary api)))
      (is (= :none (:admission api)))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-effect-legality? api)))
      (is (false? (:grants-authority? api))))
    (is (= :existing-entry-captured-values (get-in contract [:public-api 'validate-stage0-module-effects! :callback-selection])))
    (is (= ['P4-HOST-EFFECT 'P4-HOST-CAPABILITY] (get-in contract [:diagnostic-ownership :earlier-profile-owner])))
    (is (some #{'gravity.module-analysis.effects} (get-in contract [:dependency-direction :consumed-by])))
    (is (some #{:stage0-runtime-network-services} (:unsupported contract)))))

(deftest recursive-classification-preserves-native-truthy-and-empty-results
  (is (true? (b/uses-println? '(quote (println "quoted data"))))
      "the retained native scanner is conservative about quoted forms")
  (is (true? (b/uses-println? {:nested ['(println "nested")]})))
  (is (false? (effects/stage0-uses-println? nil 42)))
  (is (nil? (effects/stage0-uses-println? nil [])))
  (is (nil? (effects/stage0-uses-println? nil '())))
  (let [token (Object.) seen (atom [])]
    (is (identical? token
          (effects/stage0-uses-println? (fn [value] (swap! seen conj value) (when (= :b value) token)) [:a :b :c])))
    (is (= [:a :b] @seen)))
  (let [sentinel (ex-info "unused recursive callback" {})]
    (is (true? (effects/stage0-uses-println? (fn [_] (throw sentinel)) '(println value))))))

(deftest recursive-sequence-callback-order-and-exceptions-are-preserved
  (let [seen (atom []) sentinel (ex-info "recursive" {})]
    (is (nil? (effects/stage0-uses-println? (fn [value] (swap! seen conj value) nil) '(call x y))))
    (is (= ['call 'x 'y] @seen))
    (is (identical? sentinel
          (observed-error #(effects/stage0-uses-println? (fn [_] (throw sentinel)) [:value]))))))

(deftest inference-retains-classifier-precedence-and-network-fact-limit
  (let [seen (atom []) forms ['(network-listen) '(quote (network-listen)) '(println "x")]]
    (is (= #{:network/listen :io/write}
           (effects/infer-stage0-effects (fn [form] (swap! seen conj form) (b/uses-println? form)) forms)))
    (is (= forms @seen)))
  (is (= #{:io/write} (effects/infer-stage0-effects (constantly :truthy) ['(network-listen)])))
  (is (= #{} (effects/infer-stage0-effects b/uses-println? ['(quote (network-listen))])))
  (is (= #{} (effects/infer-stage0-effects nil [])))
  (let [sentinel (ex-info "classifier" {})]
    (is (identical? sentinel (observed-error #(effects/infer-stage0-effects (fn [_] (throw sentinel)) [:value]))))))

(deftest capability-projection-retains-call-order-nil-false-and-opaque-values
  (let [seen (atom []) token (Object.) catalog {:write :stdout :missing nil :false false :opaque token}
        lookup (fn [effect] (swap! seen conj effect) (get catalog effect))
        result (effects/required-stage0-capabilities lookup [:write :missing :false :write :opaque])]
    (is (= #{:stdout false token} result))
    (is (some #(identical? token %) result))
    (is (= [:write :missing :false :write :opaque] @seen)))
  (is (= #{} (effects/required-stage0-capabilities nil [])))
  (let [sentinel (ex-info "catalog" {})]
    (is (identical? sentinel (observed-error #(effects/required-stage0-capabilities (fn [_] (throw sentinel)) [:value]))))))

(deftest valid-and-nonwriting-modules-retain-nil-return
  (is (nil? (effects/validate-stage0-module-effects! (defaults)
              (assoc missing-module :effects #{:io/write} :capabilities #{:io/stdout}))))
  (is (nil? (effects/validate-stage0-module-effects!
              {:uses-println? (constantly false) :fail! (fn [& _] (throw (Exception. "unused failure")))}
              missing-module)))
  (is (nil? (effects/validate-stage0-module-effects! {} {:forms []}))))

(deftest effect-refusal-precedes-capability-and-preserves-exception-and-source-identity
  (let [path (Object.) sentinel (ex-info "failure" {}) calls (atom [])
        module (assoc missing-module :source-path path)]
    (is (identical? sentinel
          (observed-error #(effects/validate-stage0-module-effects!
                            {:uses-println? (constantly true)
                             :fail! (fn [id message facts] (swap! calls conj [id message facts]) (throw sentinel))}
                            module))))
    (is (= ["L6-EFFECT-UNDECLARED"] (mapv first @calls)))
    (is (identical? path (get-in @calls [0 2 :source-span :source])))
    (is (identical? (:effects module) (get-in @calls [0 2 :declared-effects])))
    (is (= :io/write (get-in @calls [0 2 :required-effect])))))

(deftest returning-failure-callbacks-preserve-compatibility-continuation
  (let [ids (atom []) fail (fn [id & _] (swap! ids conj id) false)]
    (is (= false (effects/validate-stage0-module-effects! {:uses-println? (constantly true) :fail! fail} missing-module)))
    (is (= ["L6-EFFECT-UNDECLARED" "L3-CAPABILITY-MISSING"] @ids))
    (reset! ids [])
    (is (nil? (effects/validate-stage0-module-effects!
                {:uses-println? (constantly true) :fail! fail}
                (assoc missing-module :capabilities #{:io/stdout}))))
    (is (= ["L6-EFFECT-UNDECLARED"] @ids))))

(deftest entry-captured-callbacks-and-opaque-map-lookups-are-preserved
  (doseq [validate [effects/validate-stage0-module-effects! legacy/validate-module-effects!]]
    (let [events (atom []) changed (atom false)
          selected-fail (fn [id & _] (swap! events conj [:selected id]) :selected)
          late-fail (fn [id & _] (swap! events conj [:late id]) :late)
          classifier (fn [_] (swap! events conj :classify) (reset! changed true) true)
          input (reify clojure.lang.ILookup
                  (valAt [_ key] (swap! events conj [:get key])
                    (case key :uses-println? classifier :fail! (if @changed late-fail selected-fail) nil))
                  (valAt [_ key not-found] (swap! events conj [:get key])
                    (case key :uses-println? classifier :fail! (if @changed late-fail selected-fail) not-found)))]
      (is (= :selected (validate input missing-module)))
      (is (= [[:get :uses-println?] [:get :fail!] :classify
              [:selected "L6-EFFECT-UNDECLARED"] [:selected "L3-CAPABILITY-MISSING"]] @events)))))

(deftest compatibility-adapters-retain-arglists-and-new-stage-interposition
  (is (= '([recur-check form]) (:arglists (meta #'legacy/uses-println?))))
  (is (= '([uses-println? forms]) (:arglists (meta #'legacy/infer-effects))))
  (is (= '([effect-capability effects]) (:arglists (meta #'legacy/required-capabilities-for-effects))))
  (is (= '([{:keys [uses-println? fail!]} module]) (:arglists (meta #'legacy/validate-module-effects!))))
  (let [callback (fn [_] nil) module (Object.) calls (atom [])]
    (with-redefs [effects/stage0-uses-println? (fn [selected form] (swap! calls conj [selected form]) :stage)
                  effects/infer-stage0-effects (fn [selected forms] (swap! calls conj [selected forms]) :inferred)
                  effects/required-stage0-capabilities (fn [catalog values] (swap! calls conj [catalog values]) :required)
                  effects/validate-stage0-module-effects! (fn [operations input] (swap! calls conj [operations input]) :validated)]
      (is (= :stage (legacy/uses-println? callback :form)))
      (is (= :inferred (legacy/infer-effects callback [:form])))
      (is (= :required (legacy/required-capabilities-for-effects {:effect :capability} [:effect])))
      (is (= :validated (legacy/validate-module-effects! {:uses-println? callback :fail! callback :unused :ignored} module)))
      (is (identical? callback (ffirst @calls)))
      (is (identical? module (get-in @calls [3 1])))
      (is (= #{:uses-println? :fail!} (set (keys (get-in @calls [3 0]))))))))

(deftest genuine-source-plans-retain-facts-ids-and-stdout
  (doseq [path ["examples/hello.gravity" "examples/hello.qst" "examples/core-app.gravity" "examples/core-app.qst"]]
    (testing path
      (let [source (slurp path :encoding "UTF-8") module (source-module path source)
            calls (atom []) original effects/validate-stage0-module-effects!
            plan-and-output
            (with-redefs [effects/validate-stage0-module-effects!
                          (fn [operations input] (swap! calls conj (:source-path input)) (original operations input))]
              (let [plan (b/stage0-compiled-core-plan path source module)]
                {:plan plan :stdout (b/execute-stage0-compiled-plan plan)}))
            plan (:plan plan-and-output)]
        (is (seq @calls))
        (is (every? #{path} @calls))
        (is (= #{:io/write} (b/infer-effects (:forms module))))
        (is (= #{:io/stdout} (b/required-capabilities-for-effects #{:io/write})))
        (is (= path (get-in plan [:source :path])))
        (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
        (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
        (is (= (if (.contains path "core-app") "core-app\ngravity:19:2\n(:ok 19)\n" "Hello Gravity\n")
               (:stdout plan-and-output)))))))

(deftest full-route-preserves-earlier-p4-refusals-without-emission
  (doseq [[path expected] [["bootstrap/clojure/fixtures/rejected/missing-effect.gravity" "P4-HOST-EFFECT"]
                           ["bootstrap/clojure/fixtures/rejected/missing-capability.gravity" "P4-HOST-CAPABILITY"]]]
    (let [source (slurp path :encoding "UTF-8") module (source-module path source)
          calls (atom 0) emitted (atom nil) original effects/validate-stage0-module-effects!
          error (with-redefs [effects/validate-stage0-module-effects! (fn [& args] (swap! calls inc) (apply original args))]
                  (observed-error #(reset! emitted (b/stage0-compiled-core-plan path source module))))]
      (is (= expected (:id (ex-data error))))
      (is (= path (get-in (ex-data error) [:source-span :source])))
      (is (zero? @calls))
      (is (nil? @emitted)))))

(deftest direct-effect-stage-refuses-genuine-unmodified-macro-modules
  (doseq [[path expected] [["bootstrap/clojure/fixtures/rejected/missing-effect.gravity" "L6-EFFECT-UNDECLARED"]
                           ["bootstrap/clojure/fixtures/rejected/missing-capability.gravity" "L3-CAPABILITY-MISSING"]]]
    (let [source (slurp path :encoding "UTF-8") module (source-module path source)
          seen (atom []) original effects/validate-stage0-module-effects!
          error (with-redefs [effects/validate-stage0-module-effects!
                              (fn [operations input] (swap! seen conj input) (original operations input))]
                  (observed-error #(b/validate-module-effects! module)))]
      (is (= 1 (count @seen)))
      (is (identical? module (first @seen)))
      (is (= expected (:id (ex-data error))))
      (is (= path (get-in (ex-data error) [:source-span :source]))))))
