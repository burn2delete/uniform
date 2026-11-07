(ns gravity.safety.unsafe-audit-test
  "Native SAFE6 controls and ordinary current Stage0 source evidence."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.safety :as safety]))

(defn- observed-error [operation]
  (try (operation) nil (catch Throwable error error)))

(defn- ports [sink]
  {:arg-value (clojure.lang.Var/create (fn [args index] (:value (nth args index nil))))
   :record! (clojure.lang.Var/create sink)})

(defn- record-call [operations kind values]
  (safety/record-native-unsafe-audit-call!
   :checker (atom {:profile :native :target :jvm :safety :audited-unsafe})
   {:node-id :node :source-span :span :generated-origin [:origin]}
   'operator (mapv #(hash-map :value %) values) #{:memory/raw} #{:ffi/c} :Result
   {:safe6-kind kind} operations))

(deftest contract-keeps-record-labels-separate-from-approval-and-proof
  (let [contract (:gravity/namespace-contract (meta #'safety/native-unsafe-forms))
        supplied (var-get (ns-resolve 'gravity.bootstrap 'safe6-source-audit-operations))]
    (is (= 18 (count (ns-publics 'gravity.safety)) (count (:public-api contract))))
    (doseq [operation [#'safety/native-unsafe-forms #'safety/validate-native-unsafe-island!
                       #'safety/record-native-unsafe-audit-call!]]
      (let [api (get-in contract [:public-api (:name (meta operation))])]
        (is (= (:arglists (meta operation)) (:arglists api)))
        (is (= :none (:admission api)))
        (is (true? (:hosted-seed-only? api)))))
    (is (= {:arg-value #'b/dispatch-arg-value :record! #'b/record-checker!} supplied))
    (is (= '[SAFE6-UNSAFE-FORBIDDEN SAFE6-MISSING-METADATA]
           (get-in contract [:diagnostic-ownership :executable-unsafe-refusal-ids])))
    (is (= :existing-caller (get-in contract [:diagnostic-ownership :typed-unsafe-catalog-owner])))
    (is (= :required-key-presence-only (get-in contract [:public-api 'validate-native-unsafe-island! :metadata-check])))
    (is (= :unverified-data (get-in contract [:public-api 'record-native-unsafe-audit-call! :record-labels])))
    (is (= :existing-gravity-c10-and-checked-core-verifier (:outcome-classification-owner contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-verifier-certificates? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (some #{:unsafe-form-execution} (:unsupported contract)))
    (is (some #{:unsafe-review-or-certificate-authentication} (:unsupported contract)))))

(deftest scanner-retains-quote-boundaries-outer-islands-and-physical-forms
  (let [inner (list 'unsafe {:owner :inner} :inner-body)
        outer (with-meta (list 'unsafe {:owner :outer} inner) {:original :metadata})
        sibling (list 'unsafe {:owner :sibling} :sibling-body)
        map-island (list 'unsafe {:owner :map} :map-body)
        set-island (list 'unsafe {:owner :set} :set-body)
        source (list 'do (list 'quote inner) outer [sibling] {:nested map-island} #{set-island})
        found (vec (safety/native-unsafe-forms #'b/stage0-unsafe-forms source))]
    (is (= [outer sibling map-island set-island] found))
    (is (identical? outer (first found)))
    (is (identical? sibling (second found)))
    (is (identical? map-island (nth found 2)))
    (is (identical? set-island (nth found 3)))
    (is (= {:original :metadata} (meta (first found)))))
  (let [unreadable (reify clojure.lang.IDeref (deref [_] (throw (Exception. "unused recursion"))))]
    (doseq [form [nil false 17 "unsafe" 'unsafe '(quote (unsafe nil))]]
      (is (= [] (safety/native-unsafe-forms unreadable form))))
    (let [form '(unsafe nil :body)]
      (is (identical? form (first (safety/native-unsafe-forms unreadable form)))))))

(deftest recursive-operation-is-selected-before-lazy-mapcat-consumption
  (let [events (atom []) operation (clojure.lang.Var/create nil)
        replacement (fn [form] (swap! events conj [:replacement form]) [form])
        original (fn [form]
                   (swap! events conj [:original form])
                   (alter-var-root operation (constantly replacement)) [form])
        source '(a b c d e f g h)]
    (alter-var-root operation (constantly original))
    (let [result (safety/native-unsafe-forms operation source)]
      (is (not (vector? result)))
      (is (= (vec source) (vec result)))
      (is (= (mapv #(vector :original %) source) @events)))
    (reset! events [])
    (is (= (vec source) (vec (safety/native-unsafe-forms operation source))))
    (is (= (mapv #(vector :replacement %) source) @events)))
  (let [failure (Exception. "recursive owner") operation (clojure.lang.Var/create (fn [_] (throw failure)))]
    (is (identical? failure (observed-error #(doall (safety/native-unsafe-forms operation '(:child))))))))

(deftest executable-guards-check-modes-and-key-presence-without-authenticating-values
  (let [required (clojure.lang.Var/create #{:owner :invariant})
        failure (clojure.lang.Var/create (fn [id message data] (throw (ex-info message (assoc data :id id)))))]
    (doseq [mode [:safe :safe-optimized]]
      (let [error (observed-error #(safety/validate-native-unsafe-island!
                                    failure #'set/difference required {:source-path "source" :safety mode}
                                    '(unsafe {:owner :owner :invariant :invariant} :body)))]
        (is (= "SAFE6-UNSAFE-FORBIDDEN" (:id (ex-data error))))
        (is (= mode (:safety-mode (ex-data error))))
        (is (= :rejected (:safety-outcome (ex-data error))))))
    (doseq [value [nil false {} :forged-proof]]
      (is (nil? (safety/validate-native-unsafe-island! failure #'set/difference required
                                                       {:safety :audited-unsafe}
                                                       (list 'unsafe {:owner value :invariant value} :body)))))
    (let [error (observed-error #(safety/validate-native-unsafe-island! failure #'set/difference required
                                                                        {} '(unsafe {:owner :owner} :body)))]
      (is (= "SAFE6-MISSING-METADATA" (:id (ex-data error))))
      (is (= :invariant (:missing-fact (ex-data error)))))))

(deftest returning-failures-continue-through-original-independent-guards
  (let [calls (atom []) first-result (Object.) final-result (Object.)
        failure (clojure.lang.Var/create nil) required (clojure.lang.Var/create #{:z :a})
        path (Object.)]
    (alter-var-root failure
                    (constantly (fn [id _ data]
                                  (swap! calls conj [id data])
                                  (alter-var-root failure
                                                  (constantly (fn [next-id _ next-data]
                                                                (swap! calls conj [next-id next-data]) final-result)))
                                  first-result)))
    (is (identical? final-result
                    (safety/validate-native-unsafe-island! failure #'set/difference required
                                                           {:source-path path :safety :safe} '(unsafe nil))))
    (is (= ["SAFE6-UNSAFE-FORBIDDEN" "SAFE6-MISSING-METADATA" "SAFE6-MISSING-METADATA"]
           (mapv first @calls)))
    (is (= [nil :unsafe-audit-metadata :a] (mapv #(get-in % [1 :missing-fact]) @calls)))
    (is (every? #(identical? path (get-in % [1 :source-span :source])) @calls))))

(deftest first-executable-refusal-does-not-consume-later-module-forms
  (let [failure (Exception. "first refusal") later (atom false) calls (atom [])
        form '(unsafe {:owner :unverified} :body)
        forms (cons form (lazy-seq (reset! later true) (list '(unsafe nil :later))))]
    (is (identical? failure
                    (with-redefs [b/fail! (fn [id _ _] (swap! calls conj id) (throw failure))]
                      (observed-error #(b/validate-stage0-executable-safety!
                                        {:source-path "native" :safety :safe :forms forms})))))
    (is (= ["SAFE6-UNSAFE-FORBIDDEN"] @calls))
    (is (false? @later))))

(deftest difference-and-required-catalog-are-selected-before-metadata-keys
  (let [events (atom []) required (clojure.lang.Var/create #{:owner})
        difference (clojure.lang.Var/create (fn [a b] (swap! events conj [:original a b]) #{}))
        original-keys keys
        invoke #(safety/validate-native-unsafe-island! (clojure.lang.Var/create (fn [& _]))
                                                       difference required {} '(unsafe {:owner false}))]
    (with-redefs [clojure.core/keys
                  (fn [metadata]
                    (alter-var-root difference (constantly (fn [a b] (swap! events conj [:replacement a b]) #{})))
                    (alter-var-root required (constantly #{:later}))
                    (original-keys metadata))]
      (is (nil? (invoke))))
    (is (= [[:original #{:owner} #{:owner}]] @events))
    (is (nil? (invoke)))
    (is (= [:replacement #{:later} #{:owner}] (last @events)))))

(deftest all-ten-audit-families-keep-defaults-and-return-the-base-record
  (let [writes (atom []) sink-result (Object.)
        operations (ports (fn [& args] (swap! writes conj args) sink-result))
        cases [[:unsafe-island [:island :operation :reason :owner false false false :boundary :policy]
                :safe6-unsafe-island-records {:invariants #{} :preconditions #{} :postconditions #{}
                                              :review-state :approved :audit-status :passed}]
               [:safe-wrapper [:wrapper false :invariant false] :safe6-safe-wrapper-records
                {:unsafe-islands #{} :failure-behavior false :hides-raw-capabilities? true
                 :preconditions-enforced? true :cleanup-connected? true}]
               [:operation-inventory [:package false false false] :safe6-operation-inventories
                {:operation-families #{} :island-count 0 :safe-wrappers #{}}]
               [:review-status [:island :policy :reviewer false :expiration] :safe6-review-status-records
                {:state :approved :expiration :expiration :source-version-bound? true}]
               [:invariant-proof [:island :invariant false false] :safe6-invariant-proof-links
                {:evidence #{} :proof-status :recorded}]
               [:generated-provenance [:generator :source :generated :island] :safe6-generated-unsafe-provenance
                {:origin-status :preserved :diagnostic-source :generator-and-generated-form}]
               [:policy-decision [:policy :package false false] :safe6-policy-decision-records
                {:decision false :reason false :profile :native :target :jvm}]
               [:dependency-summary [:dependency false false false false false] :safe6-dependency-unsafe-summaries
                {:unsafe-island-count 0 :operation-families #{} :review-states #{} :safe-wrappers #{} :policy-status :accepted}]
               [:release-audit [:package false :unverified-review :unverified-certificate] :safe6-release-audit-reports
                {:unsafe-island-count 0 :review-status :unverified-review :certificate-status :unverified-certificate
                 :artifact-stable? true}]
               [:conformance [false] :safe6-conformance-records
                {:document :SAFE6 :status :complete :positive-fixtures :passed :negative-fixtures :passed}]]]
    (doseq [[kind values family expected] cases]
      (let [base (record-call operations kind values) [checker key record] (last @writes)]
        (is (= :checker checker))
        (is (= family key))
        (is (= expected (select-keys record (keys expected))))
        (is (= base (select-keys record (keys base))))
        (is (not (identical? sink-result base)))
        (is (not (contains? base :audit-status)))))
    (is (= 10 (count @writes)))))

(deftest record-sink-precedes-payload-and-argument-vars-remain-live
  (doseq [[kind arity] [[:unsafe-island 9] [:safe-wrapper 4] [:operation-inventory 4] [:review-status 5]
                        [:invariant-proof 4] [:generated-provenance 4] [:policy-decision 4]
                        [:dependency-summary 6] [:release-audit 4] [:conformance 1]]]
    (let [writes (atom []) reads (atom [])
          operations (ports (fn [& _] (swap! writes conj :original)))
          replacement (fn [_ index] (swap! reads conj [:replacement index]) :unverified)]
      (alter-var-root (:arg-value operations)
                      (constantly (fn [_ index]
                                    (swap! reads conj [:original index])
                                    (alter-var-root (:record! operations)
                                                    (constantly (fn [& _] (swap! writes conj :replacement))))
                                    (alter-var-root (:arg-value operations) (constantly replacement))
                                    :unverified)))
      (record-call operations kind [])
      (is (= [:original] @writes))
      (is (= arity (count @reads)))
      (is (= (set (range arity)) (set (map second @reads))))
      (is (= 1 (count (filter #(= :original (first %)) @reads))))
      (record-call operations kind [])
      (is (= [:original :replacement] @writes)))))

(deftest policy-context-reads-and-provenance-remain-distinct
  (let [reads (atom 0) writes (atom nil) span (Object.) origin (Object.) node-id (Object.)
        effects (Object.) capabilities (Object.) result-type (Object.)
        ctx (reify clojure.lang.IDeref
              (deref [_] (let [n (swap! reads inc)] {:profile [:profile n] :target [:target n] :safety [:safety n]})))
        result (safety/record-native-unsafe-audit-call!
                :checker ctx {:node-id node-id :source-span span :generated-origin origin}
                :operator [] effects capabilities result-type {:safe6-kind :policy-decision}
                (ports (fn [_ _ record] (reset! writes record))))]
    (is (= 5 @reads))
    (is (= #{1 2 3} (set (map second ((juxt :profile :target :safety-mode) result)))))
    (is (= #{4 5} (set (map second ((juxt :profile :target) @writes)))))
    (doseq [[key value] [[:source-span span] [:generated-origin-chain origin] [:node-id node-id]
                         [:effects effects] [:capabilities capabilities] [:return-type result-type]]]
      (is (identical? value (get result key)))
      (is (identical? value (get @writes key))))))

(deftest native-fast-paths-and-exceptions-retain-original-owner-behavior
  (let [error (Exception. "must not read")
        ctx (reify clojure.lang.IDeref (deref [_] (throw error)))
        node (reify clojure.lang.ILookup (valAt [_ _] (throw error)))]
    (doseq [spec [nil {} {:safe6-kind nil} {:safe6-kind false}]]
      (is (nil? (safety/record-native-unsafe-audit-call! nil ctx node nil nil nil nil nil spec nil))))
    (is (identical? error (observed-error #(safety/record-native-unsafe-audit-call!
                                            nil ctx {} nil nil nil nil nil {:safe6-kind :unknown} nil)))))
  (let [writes (atom 0) operations (ports (fn [& _] (swap! writes inc)))]
    (is (= :unknown (:safe6-kind (record-call operations :unknown []))))
    (is (zero? @writes)))
  (doseq [operation [:arg-value :record!]]
    (let [error (Exception. (name operation)) operations (ports (fn [& _]))]
      (alter-var-root (get operations operation) (constantly (fn [& _] (throw error))))
      (is (identical? error (observed-error #(record-call operations :unsafe-island []))))))
  (doseq [port [:failure :difference]]
    (let [error (Exception. (name port)) callback (clojure.lang.Var/create (fn [& _] (throw error)))
          failure (if (= port :failure) callback (clojure.lang.Var/create (fn [& _])))
          difference (if (= port :difference) callback #'set/difference)]
      (is (identical? error
                      (observed-error #(safety/validate-native-unsafe-island!
                                        failure difference (clojure.lang.Var/create #{:owner})
                                        {:safety :safe} '(unsafe {})))))))
  (doseq [metadata [17 :not-a-map "not-a-map"]]
    (let [callback (clojure.lang.Var/create (fn [& _]))
          direct (observed-error #(safety/validate-native-unsafe-island!
                                   callback #'set/difference #'b/stage0-unsafe-required-metadata
                                   {} (list 'unsafe metadata)))
          compatibility (with-redefs [b/fail! @callback]
                          (observed-error #(b/validate-stage0-unsafe-island! {} (list 'unsafe metadata))))]
      (is (some? direct))
      (is (= (class direct) (class compatibility)))
      (is (= (.getMessage ^Throwable direct) (.getMessage ^Throwable compatibility))))))

(deftest actual-audit-sources-emit-records-with-original-source-and-typed-identity
  (doseq [relative ["bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity" "examples/hello.gravity" "examples/hello.qst"]]
    (let [path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
          original safety/record-native-unsafe-audit-call! calls (atom 0)
          artifact (with-redefs [safety/record-native-unsafe-audit-call!
                                 (fn [& args] (swap! calls inc) (apply original args))]
                     (b/unsafe-audit-source-artifact path text))
          typed (b/typed-source-artifact path text)
          output (io/file "target/safety-unsafe-audit" (str "audit-" (.getName (io/file path)) ".edn"))
          observation {:source {:path path :text text :sha256 (str "sha256:" (b/sha256-hex text))}
                       :artifact artifact :authority :ordinary-current-host-stage0-only}]
      (is (pos? @calls))
      (is (= :gravity/stage0-unsafe-audit-artifact (:kind artifact)))
      (is (= path (get-in artifact [:module :source-path])))
      (is (= (str "sha256:" (b/sha256-hex (pr-str typed))) (:typed-core-artifact-hash artifact)))
      (when (= "unsafe-audit.gravity" (.getName (io/file path)))
        (doseq [family [:unsafe-island-records :safe-wrapper-records :unsafe-operation-inventory
                        :review-status-records :invariant-proof-links :generated-unsafe-provenance
                        :policy-decision-records :unsafe-dependency-summaries :release-audit-reports]]
          (is (= 1 (count (get artifact family))))
          (is (= path (get-in artifact [family 0 :source-span :source]))))
        (is (= 1 (count (:safe6-conformance-records typed))))
        (is (= :complete (get-in artifact [:safe6-conformance-fixture :status]))))
      (io/make-parents output)
      (spit output (pr-str observation))
      (is (= observation (edn/read-string (slurp output)))))))

(deftest actual-stage0-plans-emit-and-real-unsafe-syntax-rejects-before-execution
  (doseq [relative ["examples/hello.gravity" "examples/hello.qst"]]
    (let [path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
          macro-artifact (b/macro-source-artifact path text)
          module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
          original safety/native-unsafe-forms calls (atom 0)
          plan (with-redefs [safety/native-unsafe-forms (fn [& args] (swap! calls inc) (apply original args))]
                 (b/stage0-compiled-core-plan path text module))
          stdout (b/execute-stage0-compiled-plan plan)
          output (io/file "target/safety-unsafe-audit" (str "compiled-" (.getName (io/file path)) ".edn"))
          observation {:source {:path path :text text :sha256 (str "sha256:" (b/sha256-hex text))}
                       :plan plan :stdout stdout :authority :temporary-clojure-stage0-plan-only}]
      (is (pos? @calls))
      (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
      (is (= :clojure-bootstrap (get-in plan [:compiler :owner])))
      (is (= path (get-in plan [:source :path])))
      (is (= (str "sha256:" (b/sha256-hex text)) (get-in plan [:source :sha256])))
      (is (= (str "sha256:" (b/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
      (is (seq stdout))
      (io/make-parents output)
      (spit output (pr-str observation))
      (is (= observation (edn/read-string (slurp output))))))
  (doseq [[relative id missing]
          [["core-app-unsafe-forbidden.gravity" "SAFE6-UNSAFE-FORBIDDEN" nil]
           ["core-app-unsafe-metadata.gravity" "SAFE6-MISSING-METADATA" :capabilities]]]
    (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/rejected/" relative)))
          text (slurp path :encoding "UTF-8")
          original safety/validate-native-unsafe-island! calls (atom 0)
          error (with-redefs [safety/validate-native-unsafe-island!
                              (fn [& args] (swap! calls inc) (apply original args))]
                  (observed-error #(b/validate-stage0-source-safety! path text)))]
      (is (= 1 @calls))
      (is (instance? clojure.lang.ExceptionInfo error))
      (is (= id (:id (ex-data error))))
      (is (= missing (:missing-fact (ex-data error))))
      (is (= path (get-in (ex-data error) [:source-span :source])))
      (is (= :rejected (:safety-outcome (ex-data error))))
      (is (= (.getMessage ^Throwable error) (:message (ex-data error)))))))

(deftest existing-typed-catalog-refusals-retain-their-caller-owner
  (doseq [[fixture id] [["unsafe-forbidden" "SAFE6-UNSAFE-FORBIDDEN"]
                        ["missing-metadata" "SAFE6-MISSING-METADATA"] ["missing-owner" "SAFE6-MISSING-OWNER"]
                        ["missing-invariant" "SAFE6-MISSING-INVARIANT"] ["missing-boundary" "SAFE6-MISSING-BOUNDARY"]
                        ["review-required" "SAFE6-REVIEW-REQUIRED"] ["generated-unsafe" "SAFE6-GENERATED-UNSAFE"]
                        ["capability" "SAFE6-CAPABILITY"] ["dependency" "SAFE6-DEPENDENCY"]
                        ["certificate" "SAFE6-CERTIFICATE"]]]
    (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/rejected/typed-safe6-" fixture ".gravity")))
          original safety/validate-native-unsafe-island! calls (atom 0)
          error (with-redefs [safety/validate-native-unsafe-island!
                              (fn [& args] (swap! calls inc) (apply original args))]
                  (observed-error #(b/unsafe-audit-file-artifact path)))]
      (is (= id (:id (ex-data error))))
      (is (= path (get-in (ex-data error) [:source-span :source])))
      (is (= :typed-effected-core (:analyzer-stage (ex-data error))))
      (is (zero? @calls)))))
