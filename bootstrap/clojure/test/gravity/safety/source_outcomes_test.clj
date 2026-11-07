(ns gravity.safety.source-outcomes-test
  "SAFE1 native compatibility controls and ordinary Stage0 source evidence."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.safety :as safety]))

(defn- observed-error [operation]
  (try (operation) nil (catch Throwable error error)))

(defn- native-ports [sink diagnostic]
  {:arg-value (clojure.lang.Var/create (fn [args index] (:value (nth args index nil))))
   :record! (clojure.lang.Var/create sink)
   :diagnostic! (clojure.lang.Var/create diagnostic)
   :check-outcome! (clojure.lang.Var/create (fn [& _] nil))
   :legal-outcomes (clojure.lang.Var/create #{:proven-safe :runtime-checked :rejected :unsafe-island})})

(defn- record-call [ports kind values]
  (safety/record-native-outcome-call!
   :checker (atom {:profile :application :target :native :safety :audited})
   {:node-id :node :source-span :span :generated-origin [:origin]}
   'operator (mapv #(hash-map :value %) values) #{:effect} #{:capability} :Result
   {:safe1-kind kind} ports))

(deftest contract-names-the-three-source-apis-and-retains-canonical-owner
  (let [contract (:gravity/namespace-contract (meta #'safety/record-native-outcome-call!))
        ports (var-get (ns-resolve 'gravity.bootstrap 'safe1-source-outcome-operations))]
    (is (= 15 (count (ns-publics 'gravity.safety)) (count (:public-api contract))))
    (doseq [operation [#'safety/emit-native-outcome-diagnostic! #'safety/check-native-outcome!
                       #'safety/record-native-outcome-call!]]
      (let [api (get-in contract [:public-api (:name (meta operation))])]
        (is (= (:arglists (meta operation)) (:arglists api)))
        (is (= :none (:admission api)))
        (is (true? (:hosted-seed-only? api)))))
    (is (= #{:arg-value :check-outcome! :diagnostic! :record! :legal-outcomes} (set (keys ports))))
    (is (every? var? (vals ports)))
    (is (identical? #'b/check-safe1-outcome! (:check-outcome! ports)))
    (is (identical? #'b/safe1-diagnostic! (:diagnostic! ports)))
    (is (identical? #'b/safe1-legal-outcomes (:legal-outcomes ports)))
    (is (= :existing-gravity-c10-and-checked-core-verifier (:outcome-classification-owner contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-verifier-certificates? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (some #{'gravity.bootstrap} (get-in contract [:dependency-direction :forbids])))
    (is (every? (set (:governing-contracts contract)) '[SAFE1 SAFE6 SAFE15 C7 C8 C10 C15]))
    (is (some #{:source-proof-and-audit-label-authentication} (:unsupported contract)))))

(deftest diagnostic-defaults-overrides-and-callee-selection-preserve-identities
  (let [span (Object.) profile (Object.) fact (Object.) remediation (Object.)
        node {:profile profile :source-span span}
        callback (clojure.lang.Var/create (fn [& args] args))]
    (doseq [data [nil {} {:safe-rule false :active-profile nil :source-span false :fact fact}]]
      (let [[id message actual-node actual-remediation payload]
            (safety/emit-native-outcome-diagnostic! callback "id" "message" node remediation data)
            expected (merge {:safe-rule :SAFE1 :active-profile profile :source-span span} data)]
        (is (= ["id" "message"] [id message]))
        (is (identical? node actual-node))
        (is (identical? remediation actual-remediation))
        (is (= expected payload))
        (doseq [key [:active-profile :source-span :fact]]
          (is (identical? (get expected key) (get payload key)))))))
  (let [events (atom []) first-result (Object.) later-result (Object.)
        callback (clojure.lang.Var/create (fn [& _] first-result))
        node (reify clojure.lang.ILookup
               (valAt [_ key]
                 (swap! events conj key)
                 (alter-var-root callback (constantly (fn [& _] later-result)))
                 key))
        data (lazy-seq (swap! events conj :data)
                       (list (clojure.lang.MapEntry/create :fact :retained)))]
    (is (identical? first-result (safety/emit-native-outcome-diagnostic! callback "id" "message" node nil data)))
    (is (= [:profile :source-span :data] @events))
    (is (identical? later-result (safety/emit-native-outcome-diagnostic! callback "id" "message" {} nil nil)))))

(deftest outcome-guards-check-membership-and-presence-with-live-independent-diagnostics
  (let [legal (clojure.lang.Var/create #{:proven-safe :runtime-checked :rejected :unsafe-island})
        calls (atom []) diagnostic (clojure.lang.Var/create (fn [& args] (swap! calls conj args) :diagnostic))]
    (doseq [outcome @legal evidence [false {} :unverified-reference]]
      (is (nil? (safety/check-native-outcome! legal diagnostic {} :op outcome evidence))))
    (is (empty? @calls))
    (doseq [outcome [:fifth nil]]
      (is (nil? (safety/check-native-outcome! legal diagnostic {} :op outcome nil)))
      (is (= "SAFE1-NO-OUTCOME" (first (last @calls)))))
    (is (= :diagnostic (safety/check-native-outcome! legal diagnostic {} :op :proven-safe nil)))
    (is (= "SAFE1-PROOF-MISSING" (first (last @calls)))))
  (let [legal (clojure.lang.Var/create #{}) calls (atom []) final-result (Object.)
        diagnostic (clojure.lang.Var/create nil)]
    (alter-var-root diagnostic
                    (constantly (fn [id & _]
                                  (swap! calls conj id)
                                  (alter-var-root diagnostic
                                                  (constantly (fn [next-id & _]
                                                                (swap! calls conj next-id) final-result)))
                                  :discarded)))
    (is (identical? final-result (safety/check-native-outcome! legal diagnostic {} :op :proven-safe nil)))
    (is (= ["SAFE1-NO-OUTCOME" "SAFE1-PROOF-MISSING"] @calls))
    (alter-var-root legal (constantly #{:new-tag}))
    (is (nil? (safety/check-native-outcome! legal diagnostic {} :op :new-tag nil)))))

(deftest absent-kind-does-not-read-context-node-or-ports
  (let [failure (Exception. "must not read")
        ctx (reify clojure.lang.IDeref (deref [_] (throw failure)))
        node (reify clojure.lang.ILookup (valAt [_ _] (throw failure)))]
    (doseq [spec [nil {} {:safe1-kind nil} {:safe1-kind false}]]
      (is (nil? (safety/record-native-outcome-call! nil ctx node nil nil nil nil nil spec nil)))))
  (let [reads (atom 0) ctx (reify clojure.lang.IDeref (deref [_] (swap! reads inc) {}))]
    (is (= {:node-id nil :operator nil :safe1-kind :unknown :profile nil :target nil :safety-mode nil
            :effects nil :capabilities nil :return-type nil :source-span nil :generated-origin-chain nil}
           (safety/record-native-outcome-call! nil ctx nil nil nil nil nil nil {:safe1-kind :unknown} nil)))
    (is (= 3 @reads))))

(deftest all-seven-record-families-preserve-base-and-unverified-default-semantics
  (let [writes (atom []) sink-result (Object.)
        ports (native-ports (fn [& args] (swap! writes conj args) sink-result)
                            (fn [& _] (throw (Exception. "unexpected diagnostic"))))
        cases [[:classification [:op :proven-safe false {:forged-proof true} false]
                :safe1-safety-classification-records {:outcome :proven-safe :proof-reference {:forged-proof true}}]
               [:runtime-check [:op false false] :safe1-runtime-check-records
                {:condition false :failure-behavior :panic/safety-check}]
               [:unsafe-island [:op false false false false false] :safe1-unsafe-island-audit-records
                {:owner false :reason false :invariant false :safe-boundary false :evidence #{} :audit-status :passed}]
               [:generated-provenance [:generator :source :generated false] :safe1-generated-code-safety-provenance
                {:origin-status :preserved :diagnostic-source :generator-and-generated-form}]
               [:optimization-erasure [:op :check false false] :safe1-optimization-check-erasure-justifications
                {:outcome :proven-safe :proof false :proof-preserved? false}]
               [:dependency-mode [:dependency :safe :unsafe false] :safe1-dependency-safety-mode-records
                {:status :accepted :certificate :recorded}]
               [:conformance [false false] :safe1-conformance-records
                {:declared-outcomes @(:legal-outcomes ports) :status :complete
                 :positive-fixtures :passed :negative-fixtures :passed}]]]
    (doseq [[kind values family expected] cases]
      (let [base (record-call ports kind values) [checker key record] (last @writes)]
        (is (= :checker checker))
        (is (= family key))
        (is (= expected (select-keys record (keys expected))))
        (is (= base (select-keys record (keys base))))
        (is (not (identical? sink-result base)))
        (is (not (contains? base :outcome)))))
    (is (= 7 (count @writes))))
  (let [span (Object.) origin (Object.) effects (Object.) capabilities (Object.) result-type (Object.)
        node-id (Object.) writes (atom nil)
        ports (native-ports (fn [_ _ record] (reset! writes record)) (fn [& _]))
        result (safety/record-native-outcome-call! :checker (atom {})
                                                   {:node-id node-id :source-span span :generated-origin origin}
                                                   :op [{:value :g}] effects capabilities result-type
                                                   {:safe1-kind :generated-provenance} ports)]
    (doseq [[key expected] [[:node-id node-id] [:source-span span] [:generated-origin-chain origin]
                            [:effects effects] [:capabilities capabilities] [:return-type result-type]]]
      (is (identical? expected (get result key)))
      (is (identical? expected (get @writes key))))))

(deftest sink-and-argument-vars-are-selected-at-the-original-branch-position
  (doseq [[kind values expected-first] [[:generated-provenance [:g :s :f :preserved] :original]
                                        [:conformance [#{} :complete] :original]
                                        [:runtime-check [:op :condition :failure] :replacement]]]
    (let [writes (atom []) ports (native-ports (fn [& _] (swap! writes conj :original)) (fn [& _]))
          selector @(:arg-value ports)]
      (alter-var-root (:arg-value ports)
                      (constantly (fn [args index]
                                    (alter-var-root (:record! ports)
                                                    (constantly (fn [& _] (swap! writes conj :replacement))))
                                    (selector args index))))
      (record-call ports kind values)
      (record-call ports kind values)
      (is (= [expected-first :replacement] @writes))))
  (let [indices (atom []) writes (atom nil)
        ports (native-ports (fn [_ _ record] (reset! writes record)) (fn [& _]))]
    (alter-var-root (:arg-value ports)
                    (constantly (fn [_ index]
                                  (swap! indices conj [:original index])
                                  (alter-var-root (:arg-value ports)
                                                  (constantly (fn [_ next-index]
                                                                (swap! indices conj [:replacement next-index])
                                                                :later)))
                                  :first)))
    (record-call ports :runtime-check [])
    (is (= [[:original 0] [:replacement 1] [:replacement 2]] @indices))
    (is (= [:first :later :later] ((juxt :operation :condition :failure-behavior) @writes))))
  (let [ports (native-ports (fn [& _]) (fn [& _]))]
    (is (map? (record-call (assoc ports :legal-outcomes nil) :conformance [#{} :complete])))
    (alter-var-root (:legal-outcomes ports) (constantly #{:live-unverified-tag}))
    (let [record (atom nil)]
      (alter-var-root (:record! ports) (constantly (fn [_ _ payload] (reset! record payload))))
      (record-call ports :conformance [false false])
      (is (= #{:live-unverified-tag} (:declared-outcomes @record))))))

(deftest unsafe-policy-reads-live-context-after-arguments-and-keeps-original-base
  (let [events (atom []) reads (atom 0) current (atom {:safety :audited})
        ctx (reify clojure.lang.IDeref
              (deref [_]
                (let [n (swap! reads inc)]
                  (swap! events conj [:context n])
                  (if (< n 4)
                    {:profile [:profile n] :target [:target n] :safety [:safety n]}
                    @current))))
        writes (atom nil) diagnostics (atom [])
        ports (native-ports (fn [_ _ record] (swap! events conj :sink) (reset! writes record))
                            (fn [id & _] (swap! events conj :diagnostic) (swap! diagnostics conj id)))
        selector @(:arg-value ports)]
    (alter-var-root (:arg-value ports)
                    (constantly (fn [args index]
                                  (swap! events conj [:arg index])
                                  (reset! current {:safety :safe}) (selector args index))))
    (let [base (safety/record-native-outcome-call! :checker ctx {} :op
                                                   (mapv #(hash-map :value %) [:op :owner :reason :invariant :boundary #{}])
                                                   #{} #{} :Unit {:safe1-kind :unsafe-island} ports)]
      (is (= #{1 2 3} (set (map second ((juxt :profile :target :safety-mode) base)))))
      (is (= base (select-keys @writes (keys base)))))
    (is (= ["SAFE1-UNSAFE-POLICY"] @diagnostics))
    (is (= [[:context 1] [:context 2] [:context 3]
            [:arg 0] [:arg 1] [:arg 2] [:arg 3] [:arg 4] [:arg 5]
            [:context 4] :diagnostic :sink] @events))))

(deftest refusals-and-host-exceptions-preserve-order-and-object-identity
  (let [error (Exception. "diagnostic owner") writes (atom [])
        ports (native-ports (fn [& args] (swap! writes conj args)) (fn [& _] (throw error)))]
    (doseq [[kind values] [[:runtime-check [:op nil]]
                           [:unsafe-island [:op nil]]
                           [:optimization-erasure [:op :check :proven-safe nil]]
                           [:dependency-mode [:dep :safe :unsafe :rejected]]]]
      (is (identical? error (observed-error #(record-call ports kind values))))
      (is (empty? @writes)))
    (is (identical? error
                    (observed-error #(safety/emit-native-outcome-diagnostic! (:diagnostic! ports) "id" "message" {} nil {}))))
    (is (identical? error
                    (observed-error #(safety/check-native-outcome! (:legal-outcomes ports) (:diagnostic! ports)
                                                                   {} :op :fifth nil)))))
  (doseq [port [:arg-value :record! :check-outcome!]]
    (let [error (Exception. (name port)) ports (native-ports (fn [& _]) (fn [& _]))]
      (alter-var-root (get ports port) (constantly (fn [& _] (throw error))))
      (is (identical? error (observed-error #(record-call ports :classification [:op :proven-safe nil :proof]))))))
  (let [error (Exception. "node lookup") ports (native-ports (fn [& _]) (fn [& _]))
        node (reify clojure.lang.ILookup (valAt [_ _] (throw error)))]
    (is (identical? error (observed-error #(safety/record-native-outcome-call!
                                            nil (atom {}) node nil nil nil nil nil {:safe1-kind :runtime-check} ports)))))
  (doseq [bad [17 "not-map" :not-map]]
    (let [callback (clojure.lang.Var/create (fn [& _] :unreachable))
          direct (observed-error #(safety/emit-native-outcome-diagnostic! callback "id" "message" {} nil bad))
          compatibility (with-redefs [b/typed-diagnostic! @callback]
                          (observed-error #(b/safe1-diagnostic! "id" "message" {} nil bad)))]
      (is (some? direct))
      (is (= (class direct) (class compatibility)))
      (is (= (.getMessage ^Throwable direct) (.getMessage ^Throwable compatibility))))))

(deftest ordinary-source-artifacts-reach-stage-recording-with-original-provenance
  (doseq [relative ["bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity"
                    "examples/hello.gravity" "examples/hello.qst"]]
    (let [path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
          original safety/record-native-outcome-call! calls (atom 0)
          artifact (with-redefs [safety/record-native-outcome-call!
                                 (fn [& args] (swap! calls inc) (apply original args))]
                     (b/safety-source-artifact path text))
          typed (b/typed-source-artifact path text)
          record {:source {:path path :text text :sha256 (str "sha256:" (b/sha256-hex text))}
                  :artifact artifact :authority :ordinary-stage0-host-source-only
                  :observation-sha256 (b/sha256-hex (pr-str (b/c-backend-canonical-value artifact)))}
          output (io/file "target/safety-source-outcomes" (str (.getName (io/file path)) ".edn"))]
      (is (pos? @calls))
      (is (= :gravity/stage0-safety-artifact (:kind artifact)))
      (is (= path (get-in artifact [:module :source-path])))
      (is (= (str "sha256:" (b/sha256-hex (pr-str typed))) (:typed-core-artifact-hash artifact)))
      (when (= "safety-outcomes.gravity" (.getName (io/file path)))
        (is (= #{:proven-safe :runtime-checked :rejected :unsafe-island}
               (set (map :outcome (:safety-classification-records artifact)))))
        (is (= 4 (count (:safety-classification-records artifact))))
        (doseq [family [:runtime-check-manifest :unsafe-island-audit-records
                        :generated-code-safety-provenance :optimization-check-erasure-justifications
                        :dependency-safety-mode-records]]
          (is (= 1 (count (get artifact family))))
          (is (= path (get-in artifact [family 0 :source-span :source]))))
        (is (= 1 (count (:safe1-conformance-records typed))))
        (is (= :complete (get-in artifact [:safe1-conformance-fixture :status]))))
      (io/make-parents output)
      (spit output (pr-str record))
      (is (= record (edn/read-string (slurp output)))))))

(deftest actual-source-refusals-distinguish-seven-moved-and-two-early-owners
  (doseq [[fixture id moved?]
          [["no-outcome" "SAFE1-NO-OUTCOME" true] ["proof-missing" "SAFE1-PROOF-MISSING" true]
           ["check-missing" "SAFE1-CHECK-MISSING" true] ["check-illegal" "SAFE1-CHECK-ILLEGAL" false]
           ["unsafe-policy" "SAFE1-UNSAFE-POLICY" true] ["unsafe-metadata" "SAFE1-UNSAFE-METADATA" true]
           ["generated-provenance" "SAFE1-GENERATED-PROVENANCE" false]
           ["optimization-proof" "SAFE1-OPTIMIZATION-PROOF" true] ["dependency-mode" "SAFE1-DEPENDENCY-MODE" true]]]
    (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/rejected/typed-safe1-" fixture ".gravity")))
          original safety/emit-native-outcome-diagnostic! calls (atom 0)
          error (with-redefs [safety/emit-native-outcome-diagnostic!
                              (fn [& args] (swap! calls inc) (apply original args))]
                  (observed-error #(b/safety-file-artifact path)))
          data (ex-data error)]
      (is (instance? clojure.lang.ExceptionInfo error))
      (is (= id (:id data)))
      (is (= moved? (pos? @calls)))
      (is (= :SAFE1 (:safe-rule data)))
      (is (= :typed-effected-core (:analyzer-stage data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= (.getMessage ^Throwable error) (:message data)))
      (is (string? (:remediation data))))))
