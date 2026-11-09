(ns c6-public-report-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gravity.checked-core]))

(binding [*ns* *ns*] (load-file "tools/c6_reader_policy_test.clj"))
(def controls (find-ns 'c6-reader-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] (control 'diagnostic f))
(defn evaluate [file predicate] (control 'evaluate file predicate))
(defn named? [names] (control 'named? names))
(binding [*ns* owner]
  (eval '(declare p15-s23-c6c10-gravity-candidate-verification!
                  p15-s23-c6c10-fresh-construction
                  p15-s23-c6c10-validate-public-context!
                  p15-s23-c6c10-invoke-pinned-source-function!
                  p15-s23-c6c10-throw-sealed-rejection!
                  p15-s23-c6c10-seal-digest-request-result!)))
(evaluate "00701-p15-s23-c6c10-source-relative-path.clj"
          (named? '#{p15-s23-c6c10-verifier-function}))
(evaluate "00703a-p15-s23-c6c10-canonical-stream-scalars.clj" (constantly true))
(evaluate "00703b-p15-s23-c6c10-canonical-stream.clj" (constantly true))
(evaluate "00721-p15-s23-c6c10-digest-ref-shape.clj"
          (named? '#{p15-s23-c6c10-digest-ref-shape? p15-s23-c6c10-exact-digest-ref-ordinal!
                     p15-s23-c6c10-canonical-identity p15-s23-c6c10-unique-resolved-values!
                     p15-s23-c6c10-reuse-resolved-container
                     p15-s23-c6c10-resolve-digest-references!}))
(evaluate "00723-p15-s23-c6c10-seal-digest-request-result.clj"
          (named? '#{p15-s23-c6c10-physical-artifact-keys}))
(evaluate "00725-p15-s23-c6c10-exact-digest-ref-present.clj" (constantly true))
(evaluate "00726-p15-s23-c6c10-gravity-candidate-verification.clj" (constantly true))

(def context {:source-path "synthetic.gravity" :source-text "synthetic"
              :source-content-hash "synthetic-input-hash" :requested-target :jvm})
(def digest-value (str "sha256:" (apply str (repeat 64 "a"))))
(defn artifact [_version]
  (assoc {} :artifact-id digest-value :synthetic-payload [1 true]
         :target-request-metadata {:requested-target :jvm}
         :physical-provenance {:actual-paths {:gravity-c6-c10-source "synthetic-c6.gravity"}}))
(defn fresh [version]
  (let [core (artifact version)
        semantic (dissoc core :target-request-metadata :physical-provenance)
        raw (assoc semantic :artifact-id {:digest-ref 0})
        fixture ((ns-resolve 'c11-relative-policy-test 'fixture) (if (= 1 version) 2 version))]
    {:artifact core :context context :private-source-binding {:synthetic-private-binding true}
     :private-envelope {:synthetic-private-envelope true}
     :raw-result {:artifact-template raw :digest-requests [{:synthetic-request true}]
                  :digest-graph-root {:digest-ref 0} :digest-graph-roots [{:digest-ref 0}]}
     :sealed-result {:sealed-artifact-template semantic :resolved-digests [digest-value]}
     :source-binding (assoc (call 'p15-s23-c6c10-source-program-contract version) :source-contract-version version)
     :source-policy-receipt (:source-policy-receipt fixture)
     :binding-pins :synthetic-pins :graph-proof :synthetic-graph-proof
     :gravity-verification {:status :passed :semantic-authority :synthetic-replay}}))
(def events (atom []))
(defn with-report [version options f]
  (let [fresh-result (fresh version)]
    (reset! events [])
    (control 'redefine
      {'p15-s23-c6c10-validate-public-context! (fn [value] (swap! events conj :context) value)
       'p15-s23-c6c10-fresh-construction
       (fn [value selected]
         (swap! events conj [:fresh value selected])
         (when-let [error (:fresh-error options)] (throw error))
         fresh-result)
       'p15-s23-c6c10-invoke-pinned-source-function!
       (fn [_ _ function [envelope candidate requests] invocation]
         (swap! events conj [:gravity-candidate function envelope candidate requests invocation])
         (merge {:artifact-template candidate :checks [:fresh-template-replay :exact-digest-request-replay
                                                       :bounded-pure-c6-c10-scope]
                 :diagnostics [] :digest-graph-root {:digest-ref 0} :digest-graph-roots [{:digest-ref 0}]
                 :request-count 1 :semantic-authority :gravity-source :status :passed}
                (:candidate-report options)))}
      f)))
(defn report [version value]
  (call 'p15-s23-stage2-gravity-checked-core-verification-report value context
        {:interface :gravity/c6-c10 :version version}))

(deftest separately-fresh-report-preserves-version-policy-and-real-data-mechanisms
  (doseq [version [2 3]]
    (with-report version {}
      (fn []
        (let [value (artifact version) first-report (report version value)
              second-report (report version value)
              policy (call 'p15-s23-c11-mir-core-policy-projection value context first-report version)
              expected-id (call 'p15-s23-c6c10-canonical-digest
                                "synthetic.gravity"
                                {:domain (if (= 3 version)
                                           :gravity/c6-c10-source-policy-v3
                                           :gravity/c6-c10-source-policy-v2)
                                 :artifact-id digest-value :context context :policy (:source-policy-receipt (fresh version))})
              calls (filter #(and (vector? %) (= :gravity-candidate (first %))) @events)]
          (is (= first-report second-report))
          (is (= 2 (count (filter #(= [:fresh context version] %) @events))))
          (is (= 2 (count calls)))
          (is (= {:digest-ref 0} (get-in (first calls) [3 :artifact-id])))
          (is (= :gravity-source-candidate-verifier (last (first calls))))
          (is (= {:interface :gravity/c6-c10 :version version} (:source-interface first-report)))
          (is (= expected-id (:source-policy-binding-id first-report)))
          (is (= :development-unadmitted (:admission-status first-report)))
          (is (false? (:self-hosted? first-report)))
          (is (= :passed (get-in first-report [:semantic-structure :status])))
          (is (= :passed (get-in first-report [:target-request-metadata :status])))
          (is (= :passed (get-in first-report [:physical-provenance :status])))
          (is (identical? policy (call 'p15-s23-c11-mir-require-core-policy-data! value context policy version)))
          (is (not= (:source-policy-binding-id policy)
                    (:source-policy-hash (call 'p15-s23-c11-mir-core-policy-binding version policy))))
          (is (= (= 3 version) (contains? first-report :semantic-family))))))))

(deftest candidate-semantic-target-and-provenance-substitutions-refuse
  (doseq [version [2 3]
          [mutate fact]
          [[#(assoc % :synthetic-payload (list 1 true)) :fresh-type-sensitive-semantic-artifact-zipper]
           [#(assoc % :synthetic-payload [1.0M true]) :fresh-type-sensitive-semantic-artifact-zipper]
           [#(assoc % :artifact-id "changed-root") :fresh-type-sensitive-semantic-artifact-zipper]
           [#(assoc-in % [:target-request-metadata :requested-target] :c) :exact-physical-target-request-metadata]
           [#(assoc-in % [:physical-provenance :actual-paths :gravity-c6-c10-source] "other.gravity")
            :exact-physical-source-provenance]]]
    (with-report version {}
      #(is (= fact (:missing-fact (diagnostic (fn [] (report version (mutate (artifact version)))))))))))

(deftest candidate-carrier-and-raw-reference-refusals-precede-fresh-owner
  (doseq [version [2 3]
          [value fact] [[(assoc (artifact version) :unsealed {:digest-ref 0}) :public-candidate-free-of-raw-digest-refs]
                        [(with-meta (artifact version) {:foreign true}) :trusted-gravity-checked-core-carrier]
                        [(assoc (artifact version) :foreign (fn [] :unreachable)) :trusted-gravity-checked-core-carrier]]]
    (with-report version {}
      (fn []
        (is (= fact (:missing-fact (diagnostic #(report version value)))))
        (is (= [:context] @events))))))

(deftest malformed-header-refuses-before-context-or-candidate
  (doseq [function '[p15-s23-stage2-gravity-checked-core-verification-report
                     p15-s23-stage2-gravity-checked-core-verify! p15-s23-stage2-gravity-checked-core-authentic?]
          header [nil {} {:interface :gravity/c6-c10 :version 1}
                  {:interface :gravity/c6-c10 :version (int 2)}
                  {:interface :gravity/c6-c10 :version (int 3)}]]
    (with-report 2 {}
      (fn []
        (is (= :versioned-c6-source-interface
               (:missing-fact (diagnostic #(call function (fn [] :unreachable) context header)))))
        (is (empty? @events))))))

(deftest explicit-v3-report-verify-and-authentic-routing-is-current
  (with-report 3 {}
    (fn []
      (let [header {:interface :gravity/c6-c10 :version 3}]
        (is (= {:interface :gravity/c6-c10 :version 3}
               (:source-interface (report 3 (artifact 3)))))
        (is (= :passed (call 'p15-s23-stage2-gravity-checked-core-verify!
                             (artifact 3) context header)))
        (is (true? (call 'p15-s23-stage2-gravity-checked-core-authentic?
                         (artifact 3) context header)))))))

(deftest candidate-verifier-report-is-checked-before-public-success
  (doseq [version [2 3]
          change [{:request-count 99} {:semantic-authority :host-seed} {:checks [:forged]}
                  {:artifact-template {:forged true}} {:digest-graph-roots []}]]
    (with-report version {:candidate-report change}
      #(is (= :fresh-gravity-candidate-verifier-replay
              (:missing-fact (diagnostic (fn [] (report version (artifact version))))))))))

(deftest predicates-legacy-routing-and-interruption-retain-their-contracts
  (with-report 1 {}
    (fn []
      (let [result (call 'p15-s23-stage2-gravity-checked-core-verification-report (artifact 1) context)]
        (is (not (contains? result :source-interface)))
        (is (not (contains? result :source-policy-receipt)))
        (is (some #{[:fresh context 1]} @events)))))
  (doseq [version [2 3]]
    (with-report version {}
      (fn []
        (let [header {:interface :gravity/c6-c10 :version version}]
          (is (= :passed (call 'p15-s23-stage2-gravity-checked-core-verify! (artifact version) context header)))
          (is (true? (call 'p15-s23-stage2-gravity-checked-core-authentic? (artifact version) context header)))
          (is (false? (call 'p15-s23-stage2-gravity-checked-core-authentic?
                            (assoc (artifact version) :synthetic-payload []) context header)))
          (is (false? (call 'p15-s23-stage2-gravity-checked-core-authentic? (artifact version))))))))
  (doseq [version [2 3]]
    (try
      (with-report version {:fresh-error (InterruptedException. "Synthetic interruption")}
        (fn []
          ;; Capture before test reporting can perform interruptible output.
          (let [observed (try
                           (call 'p15-s23-stage2-gravity-checked-core-authentic? (artifact version) context
                                 {:interface :gravity/c6-c10 :version version})
                           :not-thrown
                           (catch InterruptedException _ {:interrupted? (.isInterrupted (Thread/currentThread))}))]
            (Thread/interrupted)
            (is (= {:interrupted? true} observed)))))
      (finally (Thread/interrupted)))))

(let [result (run-tests 'c6-public-report-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :public-report-routing-and-data-mechanisms
            :header-carrier-digest-rehydration-typed-zipper-and-policy-binding :actual
            :fresh-construction-context-and-gravity-invocation :declared-oracles
            :source-authentication false :gravity-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
