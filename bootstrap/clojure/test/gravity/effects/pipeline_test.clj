(ns gravity.effects.pipeline-test
  "Native effect-premise checks; metadata records grant no live authority."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.effects :as effects]))

(defn- checker []
  (atom {:build-effect-log [] :replay-effect-log []
         :provider-selection-records [] :capability-records []}))

(defn- context [changes]
  (atom (merge {:profile :hosted :target :jvm :declared-effects #{:io/write}
                :declared-capabilities #{:io/stdout} :build-grants #{} :replay-records #{}
                :handler-covered-effects #{} :handler-covered-capabilities #{}}
               changes)))

(def ^:private node
  (with-meta {:node-id "native-effects-node"
              :source-span {:source "native-effects.gravity" :start {:line 3 :column 2}
                            :end {:line 3 :column 12}}
              :generated-origin [{:kind :macro :parent "native-parent"}]
              :unsafe-island :preserved :runtime-checked true :ai/human-review :preserved}
    {:caller-source :preserved}))

(defn- ports []
  {:registry-entry #'b/effect-registry-entry :diagnostic! #'b/typed-diagnostic!
   :build-effect? #'b/build-effect? :record! #'b/record-checker!
   :replay-sensitive-effect? #'b/replay-sensitive-effect?
   :needs-replay? #'b/active-profile-needs-replay?
   :check-registry! #'b/check-effect-registry! :handled-effect? #'b/handled-effect?
   :profile-denied-effects #'b/profile-denied-effects :internal-effects #'b/typed-internal-effects
   :check-build-effects! #'b/check-build-effects! :check-replay-effects! #'b/check-replay-effects!})

(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest native-contract-marks-checks-and-keeps-provider-authority-separate
  (let [contract (:gravity/namespace-contract (meta #'effects/check-native-effect-legality!))]
    (is (= 30 (count (ns-publics 'gravity.effects)) (count (:public-api contract))))
    (doseq [name '[native-handled-effect? check-native-registry! check-native-build-effects!
                  check-native-replay-effects! check-native-effect-legality!]]
      (let [operation (ns-resolve 'gravity.effects name) api (get-in contract [:public-api name])]
        (is (= (:arglists (meta operation)) (:arglists api)))
        (is (= contract (:gravity/namespace-contract (meta operation))))
        (is (= :none (:admission api)))
        (is (false? (:seed-compatibility-data-only? api)))
        (is (true? (:seed-compatibility-checks-only? api)))
        (is (false? (:grants-authority? api)))
        (is (false? (:proves-effect-legality? api)))
        (is (every? (set (keys (ports))) (:operation-inputs api)))))
    (is (every? var? (vals (ports))))
    (is (false? (:legacy-build-grants-prove-performed-effects? contract)))
    (is (some #{:hermetic-input-authentication} (:unsupported contract)))
    (is (= '([checker ctx node effects capabilities] [checker ctx node effects capabilities opts])
           (:arglists (meta #'b/check-effects-and-capabilities!))))))

(deftest real-typed-fixture-emits-effect-records-through-the-moved-rules
  ;; The ordinary legacy typed route reads source with the Stage0 host oracle;
  ;; it builds typed-core data and performs no live effect/provider operation.
  (let [path "bootstrap/clojure/fixtures/accepted/effect-system.gravity"
        original b/check-effect-registry! calls (atom 0)
        artifact (with-redefs [b/check-effect-registry!
                               (fn [& arguments] (swap! calls inc) (apply original arguments))]
                   (b/typed-file-artifact path))]
    (is (pos? @calls))
    (is (= :gravity/stage0-typed-core-artifact (:kind artifact)))
    (is (= :type-effect-capability-check (get-in artifact [:pass :name])))
    (is (= 'effect.system (get-in artifact [:module :module])))
    (is (= :ai (get-in artifact [:module :profile])))
    (is (seq (:build-effect-log artifact)))
    (is (seq (:replay-effect-log artifact)))
    (is (seq (:handled-effect-table artifact)))
    (is (seq (:mir-effect-annotations artifact)))
    (is (some #{:ai/human-review} (get-in artifact [:namespace-effect-summary :inferred])))
    (is (every? :node-id (:build-effect-log artifact)))
    (is (every? #(= path (get-in % [:source-span :source])) (:type-facts artifact)))))

(deftest real-typed-fixtures-reject-the-original-l6-failures
  (doseq [[filename expected]
          [["typed-effect-undeclared.gravity" "L6-EFFECT-UNDECLARED"]
           ["typed-effect-profile.gravity" "L6-EFFECT-PROFILE"]
           ["typed-build-effect.gravity" "L6-BUILD-EFFECT"]
           ["typed-replay-effect.gravity" "L6-REPLAY-EFFECT"]
           ["typed-effect-unknown.gravity" "L6-EFFECT-UNKNOWN"]]]
    (let [path (str "bootstrap/clojure/fixtures/rejected/" filename)
          result (diagnostic #(b/typed-file-artifact path))]
      (is (= expected (:id result)))
      (is (= :typed-effected-core (:analyzer-stage result)))
      (is (= path (get-in result [:source-span :source])))
      (is (string? (:remediation result))))))

(deftest actual-paired-plans-keep-effect-summary-and-deterministic-c-emission
  (let [products
        (mapv (fn [extension]
                (let [path (.getCanonicalPath (io/file (str "examples/hello" extension)))
                      text (slurp path :encoding "UTF-8")
                      typed (b/typed-source-artifact path text)
                      emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! path :jvm))
                      plan (b/p15-s23-stage2-compiler-artifact-plan emitter path text)
                      ;; The fixed-seed executable plan does not yet infer IO
                      ;; here. Effect checks consume actual typed-core facts.
                      summary (select-keys (:namespace-effect-summary typed) [:declared :inferred])
                      source-node (assoc node :node-id (:plan-id plan)
                                        :source-span {:source path :start {:line 1 :column 1}})
                      capabilities (get-in typed [:module :capabilities])
                      ctx (context {:declared-effects (get-in typed [:module :effects])
                                    :declared-capabilities capabilities})
                      before (binding [*print-meta* true] (pr-str [typed plan source-node @ctx]))
                      observed (checker)]
                  (is (= #{:io/write} (:inferred summary)))
                  (is (nil? (b/check-effects-and-capabilities!
                             observed ctx source-node (:inferred summary) capabilities)))
                  (is (= before (binding [*print-meta* true] (pr-str [typed plan source-node @ctx]))))
                  (is (= :io/stdout (get-in @observed [:capability-records 0 :capability])))
                  (is (= "L6-EFFECT-UNDECLARED"
                         (:id (diagnostic #(b/check-effects-and-capabilities!
                                            (checker) (context {:declared-effects #{}})
                                            source-node (:inferred summary) capabilities)))))
                  (is (= :passed (b/c-backend-validate-runtime-plan! path :c plan)))
                  (let [source (b/c-backend-runtime-source plan)]
                    (is (str/includes? source "fwrite("))
                    (is (= source (b/c-backend-runtime-source plan)))
                    {:plan-id (:plan-id plan) :typed-summary summary
                     :non-authoritative-seed-summary (:effect-summary plan) :source source})))
              [".gravity" ".qst"])]
    (is (= (first products) (second products)))))

(deftest native-build-and-replay-records-preserve-order-and-labels
  (let [build [:build/read-file :build/write-artifact]
        replay [:time/read :random/read :ai/human-review]
        observed (checker)
        ctx (context {:profile :ai :declared-effects (set (concat build replay))
                      :build-grants (set build) :replay-records (set replay)})
        before (binding [*print-meta* true] (pr-str [node @ctx]))]
    (is (nil? (effects/check-native-effect-legality! (ports) observed ctx node (concat build replay) {})))
    (is (= build (mapv :effect (:build-effect-log @observed))))
    (is (= replay (mapv :effect (:replay-effect-log @observed))))
    (is (every? #(= :metadata-build-grant (:grant %)) (:build-effect-log @observed)))
    (is (every? #(= :granted (:status %)) (:build-effect-log @observed)))
    (is (every? #(= :metadata-replay-record (:replay-record %)) (:replay-effect-log @observed)))
    (is (every? #(= :recorded (:mode %)) (:replay-effect-log @observed)))
    (is (every? #(= (:node-id node) (:node-id %)) (concat (:build-effect-log @observed) (:replay-effect-log @observed))))
    (is (= before (binding [*print-meta* true] (pr-str [node @ctx])))))
  (doseq [[changes inferred opts expected]
          [[{:declared-effects #{:build/read-file}} [:build/read-file]
            {:build-grant-diagnostic "CUSTOM-BUILD"} "CUSTOM-BUILD"]
           [{:profile :ai :declared-effects #{:ai/human-review}} [:ai/human-review] {} "L6-REPLAY-EFFECT"]]]
    (let [result (diagnostic #(effects/check-native-effect-legality!
                              (ports) (checker) (context changes) node inferred opts))]
      (is (= expected (:id result)))
      (is (= (:source-span node) (:source-span result))))))

(deftest handled-effects-neither-bypass-profile-nor-grant-capabilities
  (let [ctx (context {:declared-effects #{} :handler-covered-effects #{:io/write}
                      :declared-capabilities #{}})]
    (is (true? (effects/native-handled-effect? ctx :io/write)))
    (is (nil? (effects/check-native-effect-legality! (ports) (checker) ctx node [:io/write] {})))
    (is (= "L15-CAPABILITY-MISSING"
           (:id (diagnostic #(b/check-effects-and-capabilities! (checker) ctx node [:io/write] [:io/stdout])))))
    (swap! ctx assoc :profile :core)
    (is (= "L6-EFFECT-PROFILE"
           (:id (diagnostic #(effects/check-native-effect-legality! (ports) (checker) ctx node [:io/write] {})))))))

(deftest effect-build-replay-checks-precede-the-existing-capability-loop
  (let [events (atom [])]
    (with-redefs [b/check-effect-registry! (fn [& _] (swap! events conj :registry))
                  b/check-build-effects! (fn [& _] (swap! events conj :build) false)
                  b/check-replay-effects! (fn [& _] (swap! events conj :replay) false)
                  b/handled-capability? (fn [& _] (swap! events conj :capability) true)]
      (is (nil? (b/check-effects-and-capabilities! (checker) (context {}) node [:io/write] [:io/stdout]))))
    (is (= [:registry :build :replay :capability :capability :capability] @events))))

(deftest registry-and-profile-catalog-ports-retain-late-var-selection
  (let [operations (ports) original b/effect-registry-entry calls (atom [])]
    (with-redefs [b/profile-denied-effects {}]
      (let [ctx (context {})]
        (with-redefs [b/effect-registry-entry
                      (fn [label]
                        (swap! calls conj label)
                        (alter-var-root #'b/profile-denied-effects (constantly {:hosted #{:io/write}}))
                        (original label))]
          (is (= "L6-EFFECT-PROFILE"
                 (:id (diagnostic #(effects/check-native-effect-legality! operations (checker) ctx node [:io/write] {}))))))))
    (is (= [:io/write] @calls))))

(deftest diagnostic-callable-is-selected-before-override-argument-lookup
  (let [events (atom []) original (ex-info "original diagnostic" {:identity :original})
        late (ex-info "late diagnostic" {:identity :late})
        options (reify clojure.lang.ILookup
                  (valAt [_ key]
                    (swap! events conj [:argument key])
                    (alter-var-root #'b/typed-diagnostic! (constantly (fn [& _] (throw late))))
                    "CUSTOM-BUILD")
                  (valAt [_ _ default] default))]
    (with-redefs [b/typed-diagnostic! (fn [& _] (swap! events conj :original) (throw original))]
      (is (identical? original
                      (try (b/check-build-effects! (checker) (context {}) node [:build/read-file] options)
                           nil (catch Throwable error error)))))
    (is (= [[:argument :build-grant-diagnostic] :original] @events))))

(deftest owner-node-identity-exceptions-and-returning-failures-are-preserved
  (let [seen (atom nil) marker (Object.) failure (Error. "owner fatal")]
    (is (identical? marker
                    (effects/check-native-registry!
                     {:registry-entry (constantly nil)
                      :diagnostic! (fn [_ _ subject _ _] (reset! seen subject) marker)} node :unknown)))
    (is (identical? node @seen))
    (is (identical? failure
                    (try (effects/check-native-registry!
                          {:registry-entry (fn [_] (throw failure))} node :unknown)
                         nil (catch Throwable error error)))))
  (let [observed (checker) errors (atom []) operations (ports)]
    (with-redefs [b/typed-diagnostic! (fn [id & _] (swap! errors conj id) false)]
      (is (nil? (effects/check-native-build-effects! operations observed (context {}) node [:build/read-file] {}))))
    (is (= ["L6-BUILD-EFFECT"] @errors))
    (is (= [{:node-id (:node-id node) :effect :build/read-file :phase :build
             :grant :metadata-build-grant :status :granted}] (:build-effect-log @observed)))
    (is (false? (:legacy-build-grants-prove-performed-effects?
                 (:gravity/namespace-contract (meta #'effects/check-native-build-effects!)))))))
