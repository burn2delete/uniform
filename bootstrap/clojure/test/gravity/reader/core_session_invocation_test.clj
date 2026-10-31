(ns gravity.reader.core-session-invocation-test
  "SOURCE/injected-port checks only. No Bootstrap, Reader or Gravity plan load."
  (:require [clojure.java.io :as io]
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

(deftest existing-interruption-and-error-containment-are-preserved
  (doseq [port '[sh07-core-from-resolution-artifact sh06-resolution-artifact-verification]]
    (with-model session-body
      (fn [{:keys [create namespace]}]
        (intern namespace port (fn [& _] (throw (AssertionError. "fixture Error"))))
        (is (= :failed (:status (create resolution invocation))))))
    (with-model session-body
      (fn [{:keys [create namespace]}]
        (intern namespace port (fn [& _] (throw (InterruptedException. "fixture interrupt"))))
        (try
          ;; Capture before clojure.test reporting can touch interruptible IO.
          (let [observed (try (create resolution invocation) nil
                             (catch InterruptedException error
                               {:error error :interrupted (.isInterrupted (Thread/currentThread))}))]
            (Thread/interrupted)
            (is (instance? InterruptedException (:error observed)))
            (is (true? (:interrupted observed))))
          (finally (Thread/interrupted)))))))
