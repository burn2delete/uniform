(ns gravity.reader.core-session-invocation-test
  "SOURCE/injected-port checks only. No Bootstrap, Reader or Gravity plan load."
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]))

(def ^:private root "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/")
(defn- forms [file]
  (binding [*read-eval* false
            *reader-resolver* (reify clojure.lang.LispReader$Resolver
                               (currentNS [_] 'gravity.bootstrap)
                               (resolveAlias [_ _] nil)
                               (resolveClass [_ _] nil)
                               (resolveVar [_ _] nil))]
    (with-open [r (java.io.PushbackReader. (io/reader (str root file)))]
      (loop [result []]
        (let [form (read {:eof ::end} r)]
          (if (= ::end form) result (recur (conj result form))))))))
(def ^:private helpers (forms "00898-sh07-core-from-authenticated-request.clj"))
(def ^:private session-forms (forms "00899-sh07-core-verification-session.clj"))
(defn- definition [source name]
  (or (first (filter #(and (seq? %) (= name (second %))) source))
      (throw (ex-info "Missing source definition" {:name name}))))
(def ^:private session-body (definition session-forms 'sh07-core-verification-session))
(def ^:private helper-names
  '[sh07-core-verification-session-schema-version sh07-core-verification-session-kind
    sh07-core-verification-session-upstream-identity-keys
    sh07-core-verification-session-expected-integrity
    sh07-core-verification-session-upstream-snapshot sh07-core-verification-session-binding])
(defn- nested-definition [source name]
  (definition (mapcat #(tree-seq coll? seq %) source) name))
(defn- install-native-validator! [n]
  ;; Only the existing strict native checker and its fixed field-access helpers
  ;; are evaluated. No authority closure, Bootstrap or target module loads.
  (let [prefix "../bootstrap-p15/"
        fields (forms (str prefix "00588-p15-s23-stage2-native-carrier-header.clj"))
        maps (forms (str prefix "00700-static-rebuild-token-009-p15-s23-stage2-closed-checked-core-verify-through-p15-s23-trusted-carrier-vector-classes.clj"))
        native (forms (str prefix "00700-static-rebuild-token-010-p15-s23-trusted-carrier-set-classes-through-p15-s23-trusted-carrier-validation.clj"))]
    (intern n 'p15-s23-hosted-native-view-reason
            (fn [& _] (throw (AssertionError. "Hosted policy is outside this strict fixture"))))
    (binding [*ns* n]
      (doseq [name '[p15-s23-stage2-native-hashset-field p15-s23-stage2-native-hashset-field-once
                    p15-s23-stage2-native-arraymap-field p15-s23-stage2-native-arraymap-field-once]]
        (eval (nested-definition fields name)))
      (eval (nested-definition maps 'p15-s23-trusted-carrier-map-classes))
      (doseq [name '[p15-s23-trusted-carrier-set-classes p15-s23-trusted-carrier-list-classes
                    p15-s23-trusted-carrier-scalar-classes p15-s23-host-native-field
                    p15-s23-host-arraymap-storage-valid? p15-s23-host-carrier-header
                    p15-s23-trusted-carrier-validation]]
        (eval (nested-definition native name))))))
(def ^:private resolution
  {:artifact-id :fixture-resolution
   :provenance {:source-path "fixture.gravity"}
   :reader-binding {:source-path "selected-reader.gravity" :revision :fixture-current}
   :gravity-resolution-boundary {:plan-binding {:source-path "resolver.gravity" :source-content-hash :fixture-hash}}
   :retained {:original :source-product}})
(def ^:private invocation
  {:reader-source-path "selected-reader.gravity"
   :authenticated-envelope-options {:source :development-current}})
(defn- digest [value]
  ;; Fixture integrity only; never a Gravity identity or authority claim.
  (format "%064x" (java.math.BigInteger. 1
                    (.digest (java.security.MessageDigest/getInstance "SHA-256")
                             (.getBytes (pr-str value) "UTF-8")))))

(defn- with-model [body operation]
  (let [n (create-ns (gensym "session-interface-fixture-"))
        events (atom []) expected {:artifact-id :fixture-core :sh06-resolution-artifact resolution
                                  :core {:value :original :catalog [:fixture]}}
        report {:status :passed :checks {:fixture true}}
        verify-context (fn [artifact context]
                         (when-not (and (= resolution artifact) (identical? invocation context)
                                        (= (:reader-source-path context) (get-in artifact [:reader-binding :source-path]))
                                        (= :fixture-current (get-in artifact [:reader-binding :revision])))
                           (throw (ex-info "Strict fixture context refused" {:id "C2-HASH"}))))]
    (try
      (binding [*ns* n] (clojure.core/refer 'clojure.core))
      (install-native-validator! n)
      (doseq [[name value]
              {'reader-canonical-hash digest
               'sh07-core-exact-comparison-value identity
               'sh07-core-source-path-from-resolution #(get-in % [:provenance :source-path])
               'sh07-core-from-resolution-artifact
               (fn [& args]
                 (swap! events conj [:construct args])
                 (if (= 1 (count args))
                   (when-not (= :fixture-pinned (get-in (first args) [:reader-binding :revision]))
                     (throw (ex-info "Strict fixture pinned Reader refused" {})))
                   (verify-context (first args) (second args)))
                 expected)
               'sh06-resolution-artifact-verification
               (fn [& args]
                 (swap! events conj [:verify args])
                 (if (= 1 (count args))
                   (when-not (= :fixture-pinned (get-in (first args) [:reader-binding :revision]))
                     (throw (ex-info "Strict fixture pinned Reader refused" {})))
                   (do
                     (when-not (= :gravity.bootstrap/legacy (second args))
                       (throw (ex-info "Strict fixture legacy context refused" {})))
                     (verify-context (first args) (nth args 2 nil))))
                 report)
               'sh07-core-verification-report
               (fn [candidate fresh upstream]
                 ;; Stand-in report tests interface selection, not Core semantics.
                 {:status (if (and (= candidate fresh) (= :passed (:status upstream))) :passed :failed)
                  :fixture-checks {:same-expected (= candidate fresh)}})}]
        (intern n name value))
      (binding [*ns* n]
        (doseq [name helper-names] (eval (definition helpers name)))
        (doseq [name '[sh07-core-session-propagate-control! sh07-core-session-known-diagnostic]]
          (eval (definition session-forms name)))
        (doseq [form [body (definition session-forms 'sh07-core-verification-session-valid?)
                      (definition session-forms 'sh07-core-artifact-verification-against-expected)]]
          (eval form)))
      (operation {:create @(ns-resolve n 'sh07-core-verification-session)
                  :valid? @(ns-resolve n 'sh07-core-verification-session-valid?)
                  :replay @(ns-resolve n 'sh07-core-artifact-verification-against-expected)
                  :namespace n :events events :expected expected :report report})
      (finally (remove-ns (ns-name n))))))

(deftest original-invocation-reaches-both-existing-ports
  (with-model session-body
    (fn [{:keys [create events expected replay]}]
      (let [session (create resolution invocation)]
        (is (= :ready (:status session)))
        (is (not (contains? session :expected-build-failure)))
        (is (= [[:construct 2] [:verify 3]] (mapv (fn [[stage args]] [stage (count args)]) @events)))
        (is (identical? invocation (second (second (first @events)))))
        (is (identical? invocation (nth (second (second @events)) 2)))
        (is (= :passed (:status (replay expected session))))
        (is (= 2 (count @events)))
        (is (not (contains? session :invocation)))))))

(deftest one-argument-and-explicit-nil-retain-legacy-behavior
  (with-model session-body
    (fn [{:keys [create events namespace expected report]}]
      ;; A pinned fixture parent exercises the original unary ports.
      (let [pinned (assoc-in resolution [:reader-binding :revision] :fixture-pinned)
            expected (assoc expected :sh06-resolution-artifact pinned)]
        (intern namespace 'sh07-core-from-resolution-artifact
                (fn [& args] (swap! events conj [:construct args]) expected))
        (let [implicit (create pinned) explicit (create pinned nil)]
          (is (= implicit explicit))
          (is (= :ready (:status implicit)))
          (is (= expected (:expected implicit)))
          (is (= report (:upstream-verification implicit)))
          (is (= 2 (:schema-version implicit)))
          (is (= [1 1 1 1] (mapv #(count (second %)) @events))))))))

(deftest only-nil-selects-legacy-even-with-a-pinned-parent
  (doseq [[body corrected?] [[session-body true]
                            [(walk/postwalk-replace {'(some? invocation) 'invocation} session-body) false]]]
    (with-model body
      (fn [{:keys [create events namespace expected]}]
        (let [pinned (assoc-in resolution [:reader-binding :revision] :fixture-pinned)
              expected (assoc expected :sh06-resolution-artifact pinned)
              validator 'sh03-reader-authenticated-envelope-options]
          ;; The actual existing invocation validator body runs only with inert
          ;; native-carrier/source-join ports. No Reader source or plan executes.
          (intern namespace 'p15-s23-trusted-carrier-validation (fn [& _] {:status :passed}))
          (intern namespace 'sh03-reader-boundary-fail!
                  (fn [& _] (throw (ex-info "Fixture invocation refused" {:id "C2-HASH"}))))
          (intern namespace 'sh03-reader-invocation-source! (fn [& _] :fixture-source))
          (binding [*ns* namespace]
            (eval (definition (forms "00813-sh03-reader-canonical-identity.clj") validator)))
          (let [validate @(ns-resolve namespace validator)]
            (intern namespace 'sh07-core-from-resolution-artifact
                    (fn [& args]
                      (swap! events conj [:construct args])
                      (when (= 2 (count args)) (validate "fixture.gravity" (second args)))
                      expected))
            (intern namespace 'sh06-resolution-artifact-verification
                    (fn [& args]
                      (swap! events conj [:verify args])
                      (when (= 3 (count args))
                        (assert (= :gravity.bootstrap/legacy (second args)))
                        (validate "fixture.gravity" (nth args 2)))
                      {:status :passed :checks {:fixture true}}))
            (let [implicit (create pinned) explicit (create pinned nil)]
              (is (= :ready (:status implicit)))
              (is (= implicit explicit))
              (is (= [1 1 1 1] (mapv #(count (second %)) @events))))
          (doseq [context (if corrected?
                           [false 0 "malformed" [] {} {:reader-source-path "x" :authenticated-envelope-options {}}]
                           [false])]
            (reset! events [])
            (let [session (create pinned context)]
              (is (= (if corrected? :failed :ready) (:status session)))
              (is (= (if corrected? [2 3] [1 1]) (mapv #(count (second %)) @events)))
              (when corrected?
                (is (identical? context (second (second (first @events)))))
                (is (identical? context (nth (second (second @events)) 2))))))))))))

(deftest missing-different-and-drifted-contexts-fail-closed
  (doseq [context [nil {} (assoc invocation :reader-source-path "wrong-reader.gravity")
                    (into {} invocation)]]
    (with-model session-body
      (fn [{:keys [create]}]
        (is (= :failed (:status (create resolution context)))))))
  (with-model session-body
    (fn [{:keys [create]}]
      (is (= :failed (:status (create (assoc-in resolution [:reader-binding :revision] :drifted) invocation))))))
  (doseq [replacement ['(sh07-core-from-resolution-artifact resolution-artifact)
                       '(sh07-core-from-resolution-artifact resolution-artifact (into {} invocation))]]
    (with-model
      (walk/postwalk-replace {'(sh07-core-from-resolution-artifact resolution-artifact invocation) replacement} session-body)
      (fn [{:keys [create]}] (is (= :failed (:status (create resolution invocation)))))))
  (doseq [replacement ['(sh06-resolution-artifact-verification resolution-artifact)
                       '(sh06-resolution-artifact-verification resolution-artifact :wrong invocation)
                       '(sh06-resolution-artifact-verification resolution-artifact :gravity.bootstrap/legacy (into {} invocation))]]
    (with-model
      (walk/postwalk-replace {'(sh06-resolution-artifact-verification resolution-artifact :gravity.bootstrap/legacy invocation) replacement} session-body)
      (fn [{:keys [create]}] (is (= :failed (:status (create resolution invocation))))))))

(deftest retained-session-integrity-and-candidate-refusals
  (with-model session-body
    (fn [{:keys [create expected valid? replay]}]
      (let [session (create resolution invocation)]
        (doseq [changed [(assoc session :schema-version 3) (assoc session :status :failed)
                         (assoc session :expected-artifact-id :stale)
                         (assoc session :expected-integrity :forged)
                         (assoc session :session-binding :forged)
                         (assoc-in session [:upstream-verification :status] :failed)
                         (assoc-in session [:upstream-snapshot :resolution-artifact-integrity] :forged)
                         (assoc-in session [:expected :core :value] :substituted)
                         (assoc-in session [:expected :core :catalog] [:substituted])]]
          (is (false? (boolean (valid? expected changed))))
          (is (= :failed (:status (replay expected changed)))))
        (doseq [candidate [(assoc-in expected [:sh06-resolution-artifact :retained :original] :changed)
                           (assoc-in expected [:sh06-resolution-artifact :reader-binding :revision] :stale)
                           (assoc-in expected [:sh06-resolution-artifact :gravity-resolution-boundary :plan-binding :source-content-hash] :forged)
                           (assoc-in expected [:core :value] :changed)
                           (assoc-in expected [:core :catalog] [:substituted])]]
          (is (= :failed (:status (replay candidate session)))))))))

(deftest expected-construction-failures-are-distinct-redacted-and-invalid
  (doseq [[outcome diagnostic]
          [[nil {:origin :expected-core-construction :kind :nil-return}]
           [(ex-info "secret /private/path" {:id "untrusted" :secret :payload})
            {:origin :expected-core-construction :kind :exception
             :exception-class "clojure.lang.ExceptionInfo"}]
           [(java.io.IOException. "secret /private/path")
            {:origin :expected-core-construction :kind :exception
             :exception-class "java.io.IOException"}]]]
    (with-model session-body
      (fn [{:keys [create namespace expected valid? replay]}]
        (intern namespace 'sh07-core-from-resolution-artifact
                (fn [& _] (if (instance? Throwable outcome) (throw outcome) outcome)))
        (let [session (create resolution invocation)]
          (is (= :failed (:status session)))
          (is (= :expected-build-failed (:failure-reason session)))
          (is (= diagnostic (:expected-build-failure session)))
          (is (nil? (:expected session)))
          (is (nil? (:session-binding session)))
          (is (false? (boolean (valid? expected session))))
          (is (= :failed (:status (replay expected session)))))))))

(deftest construction-fatal-and-causal-interruption-propagate-original-object
  ;; Synthetic throwables only; no machine failure or target execution.
  (doseq [cause [(AssertionError. "fixture fatal")
                 (OutOfMemoryError. "synthetic only")
                 (Throwable. "fixture non-Exception")
                 (InterruptedException. "fixture interrupt")]
          wrapped? [false true]
          preinterrupted? [false true]]
    (with-model session-body
      (fn [{:keys [create namespace]}]
        (let [error (if wrapped? (ex-info "fixture wrapper" {} cause) cause)]
          (intern namespace 'sh07-core-from-resolution-artifact (fn [& _] (throw error)))
          (try
            (Thread/interrupted)
            (when preinterrupted? (.interrupt (Thread/currentThread)))
            (let [observed (try (create resolution invocation) nil
                                (catch Throwable caught
                                  {:error caught :interrupted (.isInterrupted (Thread/currentThread))}))]
              (Thread/interrupted)
              (is (identical? error (:error observed)))
              (is (= (or preinterrupted? (instance? InterruptedException cause))
                     (:interrupted observed))))
            (finally (Thread/interrupted)))))))
  (with-model session-body
    (fn [{:keys [create namespace]}]
      (let [outer (Exception. "fixture cyclic outer")
            inner (Exception. "fixture cyclic inner")]
        (.initCause outer inner)
        (.initCause inner outer)
        (intern namespace 'sh07-core-from-resolution-artifact (fn [& _] (throw outer)))
        (is (= {:origin :expected-core-construction :kind :exception
                :exception-class "java.lang.Exception"}
               (:expected-build-failure (create resolution invocation))))))))

(deftest complete-causal-scan-preserves-interruption-after-fatal-nodes
  (doseq [shape [:direct-fatal :wrapped-fatal :deeper-interrupt :interrupt-before-fatal :cycle]
          preinterrupted? [false true]]
    (with-model session-body
      (fn [{:keys [create namespace events]}]
        (let [interrupt (InterruptedException. "fixture interrupt")
              fatal (AssertionError. "synthetic fatal")
              _ (.initCause fatal interrupt)
              outer (case shape
                      :direct-fatal fatal
                      :wrapped-fatal (ex-info "fixture wrapper" {} fatal)
                      :deeper-interrupt (do (.initCause interrupt (Exception. "fixture tail"))
                                            (ex-info "fixture wrapper" {} (Exception. "fixture middle" fatal)))
                      :interrupt-before-fatal (do (.initCause interrupt fatal) interrupt)
                      :cycle (do (.initCause interrupt fatal) (ex-info "fixture wrapper" {} fatal)))
              original-cause (.getCause ^Throwable outer)]
          (intern namespace 'sh07-core-from-resolution-artifact (fn [& _] (throw outer)))
          (try
            (Thread/interrupted)
            (when preinterrupted? (.interrupt (Thread/currentThread)))
            (let [observed (try (create resolution invocation) nil
                                (catch Throwable caught
                                  {:error caught :interrupted (.isInterrupted (Thread/currentThread))}))]
              (Thread/interrupted)
              (is (identical? outer (:error observed)) (str shape))
              (is (true? (:interrupted observed)) (str shape))
              (is (identical? original-cause (.getCause ^Throwable (:error observed))))
              (is (empty? @events)))
            (finally (Thread/interrupted)))))))
  ;; Existing interrupt state must also survive ordinary containment.
  (with-model session-body
    (fn [{:keys [create namespace]}]
      (intern namespace 'sh07-core-from-resolution-artifact
              (fn [& _] (throw (java.io.IOException. "fixture ordinary"))))
      (try
        (.interrupt (Thread/currentThread))
        (let [session (create resolution invocation)
              interrupted? (.isInterrupted (Thread/currentThread))]
          (Thread/interrupted)
          (is (= :exception (get-in session [:expected-build-failure :kind])))
          (is (true? interrupted?)))
        (finally (Thread/interrupted))))))

(deftest upstream-error-containment-and-direct-interruption-are-preserved
  (with-model session-body
    (fn [{:keys [create namespace]}]
      (intern namespace 'sh06-resolution-artifact-verification
              (fn [& _] (throw (AssertionError. "fixture Error"))))
      (let [session (create resolution invocation)]
        (is (= :failed (:status session)))
        (is (= :upstream-verification-failed (:failure-reason session)))
        (is (not (contains? session :expected-build-failure))))))
  (doseq [port '[sh07-core-from-resolution-artifact sh06-resolution-artifact-verification]]
    (with-model session-body
      (fn [{:keys [create namespace]}]
        (let [original (InterruptedException. "fixture interrupt")]
        (intern namespace port (fn [& _] (throw original)))
        (try
          ;; Capture before clojure.test reporting can touch interruptible IO.
          (let [observed (try (create resolution invocation) nil
                             (catch InterruptedException error
                               {:error error :interrupted (.isInterrupted (Thread/currentThread))}))]
            (Thread/interrupted)
            (is (identical? original (:error observed)))
            (is (true? (:interrupted observed))))
          (finally (Thread/interrupted))))))))

(def ^:private diagnostic-contract
  (:code-proposal (edn/read-string (slurp "specs/reader-workflow/expected-core-failure-source-trace.edn"))))

(defn- with-failed-construction [error operation]
  (with-model session-body
    (fn [{:keys [create namespace] :as model}]
      (intern namespace 'sh07-core-from-resolution-artifact (fn [& _] (throw error)))
      (operation (assoc model :session (create resolution invocation))))))

(deftest only-known-literal-codes-and-top-level-reasons-are-projected
  (doseq [code (:code-whitelist diagnostic-contract) field [:id :rule]]
    (let [input (String. ^String code)]
      (with-failed-construction (ex-info "secret path" {field input :secret "private payload"})
        (fn [{:keys [session valid? replay expected]}]
          (let [known (get-in session [:expected-build-failure :known-diagnostic])]
            (is (= {:schema-version 1 :code code} known))
            (is (not (identical? input (:code known))))
            (is (= :expected-build-failed (:failure-reason session)))
            (is (false? (boolean (valid? expected session))))
            (is (= :failed (:status (replay expected session)))))))))
  (doseq [reason (:C6-VERIFY-top-level-reason-whitelist diagnostic-contract)]
    (with-failed-construction (ex-info "secret" {:id "C6-VERIFY" :reason reason :secret "private"})
      (fn [{:keys [session]}]
        (is (= {:schema-version 1 :code "C6-VERIFY" :reason reason}
               (get-in session [:expected-build-failure :known-diagnostic]))))))
  (doseq [[data expected]
          [[{:id "C6-VERIFY" :rule "C6-VERIFY"} {:schema-version 1 :code "C6-VERIFY"}]
           [{:id "C6-VERIFY" :reason :unknown} {:schema-version 1 :code "C6-VERIFY"}]
           [{:id "C3-ID" :reason :trusted-artifact-carrier} {:schema-version 1 :code "C3-ID"}]
           [{:id "C6-VERIFY" :facts {:reason :trusted-artifact-carrier}} {:schema-version 1 :code "C6-VERIFY"}]
           [{:id "secret-id" :reason :trusted-artifact-carrier} nil]
           [{:id "C6-VERIFY" :rule "C3-ID"} nil]
           [{:id "unknown" :rule "C6-VERIFY"} nil]
           [{:id false :rule "C6-VERIFY"} nil]
           [{:id 1} nil] [{} nil]]]
    (with-failed-construction (ex-info "secret" data)
      (fn [{:keys [session]}]
        (is (= expected (get-in session [:expected-build-failure :known-diagnostic])))))))

(deftest foreign-exception-and-data-callbacks-are-never-invoked
  (let [calls (atom 0)
        callback (fn [& _] (swap! calls inc) (throw (AssertionError. "foreign callback")))
        foreign-map (proxy [clojure.lang.APersistentMap clojure.lang.IObj] []
                      (valAt [& _] (callback)) (seq [] (callback)) (count [] (callback))
                      (assoc [_ _] (callback)) (without [_] (callback)) (empty [] (callback))
                      (entryAt [_] (callback)) (containsKey [_] (callback))
                      (equals [_] (callback)) (hashCode [] (callback))
                      (meta [] (callback)) (withMeta [_] (callback)))
        foreign-value (proxy [Object] [] (equals [_] (callback)) (hashCode [] (callback)))
        foreign-lookup (reify clojure.lang.ILookup
                         (valAt [_ _] (callback)) (valAt [_ _ _] (callback)))
        foreign-comparator (reify java.util.Comparator (compare [_ _ _] (callback)))
        raw-map (fn [entries] (clojure.lang.PersistentArrayMap. (object-array entries)))
        cyclic-array (object-array 4)
        cyclic-map (clojure.lang.PersistentArrayMap. cyclic-array)
        _ (aset cyclic-array 0 :id) _ (aset cyclic-array 1 "C6-VERIFY")
        _ (aset cyclic-array 2 :cycle) _ (aset cyclic-array 3 cyclic-map)
        errors [(proxy [clojure.lang.ExceptionInfo] ["secret" {}] (getData [] (callback)))
                (proxy [Exception clojure.lang.IExceptionInfo] ["secret"] (getData [] (callback)))]
        data [foreign-map (raw-map [:id "C6-VERIFY" foreign-value "secret"])
              {:id foreign-value} {:id "C6-VERIFY" :other foreign-lookup}
              {:id "C6-VERIFY" :other (sorted-map-by foreign-comparator)}
              (with-meta {:id "C6-VERIFY"} foreign-map)
              (raw-map [:id "C6-VERIFY" :dangling]) cyclic-map]]
    (doseq [error (concat errors (map #(ex-info "secret" %) data))]
      (with-failed-construction error
        (fn [{:keys [session]}]
          (is (= #{:origin :kind :exception-class} (set (keys (:expected-build-failure session)))))
          (is (= :failed (:status session)))
          (is (zero? @calls)))))
    (is (zero? @calls))))

(deftest projection-failures-propagate-with-complete-control-policy
  (doseq [kind [:ordinary :fatal :interrupt :fatal-before-interrupt :wrapped-fatal-before-interrupt]
          preinterrupted? [false true]]
    (with-model session-body
      (fn [{:keys [create namespace events]}]
        (let [interrupt (InterruptedException. "synthetic interrupt")
              fatal (doto (AssertionError. "synthetic fatal") (.initCause interrupt))
              error (case kind
                      :ordinary (java.io.IOException. "projection failed")
                      :fatal (AssertionError. "synthetic fatal only")
                      :interrupt interrupt
                      :fatal-before-interrupt fatal
                      :wrapped-fatal-before-interrupt (ex-info "wrapper" {} fatal))
              original-cause (.getCause ^Throwable error)]
          (intern namespace 'sh07-core-from-resolution-artifact
                  (fn [& _] (throw (ex-info "construction" {:id "C6-VERIFY"}))))
          (intern namespace 'p15-s23-trusted-carrier-validation (fn [& _] (throw error)))
          (try
            (Thread/interrupted)
            (when preinterrupted? (.interrupt (Thread/currentThread)))
            (let [observed (try (create resolution invocation) nil
                                (catch Throwable caught
                                  {:error caught :flag (.isInterrupted (Thread/currentThread))}))]
              (Thread/interrupted)
              (is (identical? error (:error observed)))
              (is (identical? original-cause (.getCause ^Throwable (:error observed))))
              (is (= (or preinterrupted? (contains? #{:interrupt :fatal-before-interrupt :wrapped-fatal-before-interrupt} kind))
                     (:flag observed)))
              (is (empty? @events)))
            (finally (Thread/interrupted))))))))
