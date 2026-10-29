(ns gravity.pmk.first-request-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pmk.acceptance :as pmk]
            [gravity.pmk.first-request :as first-request]))

(def h (apply str (repeat 64 "a")))
(def git-id {:head (apply str (repeat 40 "b")) :tree (apply str (repeat 40 "c"))})
(def closure (into [{:selector first-request/selector :owner-apis {:c7 first-request/required-apis}}]
                   (for [i (range 1 15)] {:selector (symbol (str "synthetic-" i))})))
(def parent
  {:schema :pmk-acceptance-plan-v1 :execution :unrun :root "file:/synthetic/"
   :source-revision git-id :checkout git-id :inputs-sha256 h :runtime {:synthetic true}
   :selection (mapv #(str pmk/test-ns "/" (:selector %)) closure)
   :closure closure})
(def outputs {:result "/synthetic/one/result.edn" :stdout "/synthetic/one/stdout"
              :stderr "/synthetic/one/stderr" :terminal "/synthetic/one/terminal.edn"})

(defn request []
  ;; Only the input-adapter result is synthetic; no owner is mocked or loaded.
  (with-redefs [pmk/prepare (fn [& _] parent)]
    (first-request/prepare {} {} {} "one-new-operation" outputs)))

(defn binding-input [request]
  {:request-sha256 (first-request/digest request)
   :runtime {:ordered-classpath [{:path "/synthetic/src" :sha256 h}]
             :java {:path "/synthetic/java" :sha256 h}
             :clojure {:path "/synthetic/clojure" :sha256 h}
             :config-sha256 h :environment-sha256 h}
   :controller {:path "/synthetic/controller.clj" :sha256 h
                :ports (zipmap first-request/required-ports (repeat h))}
   :guards [{:id :existing-common :root "/synthetic/existing-root"
             :resource-class :exclusive :policy-sha256 h}]
   :coverage {:authority-sha256 h :required-guards [:existing-common]
              :acquisition-order [:existing-common]
              :participants [{:id :synthetic-owner :guards [:existing-common]}]}
   :limits {:heap-bytes 2147483648 :whole-timeout-ms 120000 :capture-bytes 1048576
            :containment-sha256 h :deadline-policy :before-acquisition-through-release
            :release-policy :terminal-before-publication}
   :history {:operation-id "one-new-operation" :state :never-submitted
             :original-handle nil :evidence-sha256 h}
   :outputs {:paths outputs :state :absent :observation-sha256 h}})

(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))

(defn fake-verifier [calls]
  (fn [request expected]
    (swap! calls inc)
    {:request-sha256 (first-request/digest request)
     :binding-sha256 (first-request/digest expected) :status :verified-for-submission}))

(deftest exact-one-projection-retains-parent-and-negative-coverage
  (let [r (request)]
    (is (= parent (:parent-plan r)))
    (is (= [(str pmk/test-ns "/" first-request/selector)] (:selection r)))
    (is (= ["--exact" (str first-request/selector)] (subvec (:argv r) 5)))
    (is (= 7 (count (:argv r))))
    (is (= :unrun (:execution r)))
    (doseq [bad [(assoc outputs :result "relative")
                 (assoc outputs :stdout (:stderr outputs)) (dissoc outputs :terminal)]]
      (with-redefs [pmk/prepare (fn [& _] parent)]
        (is (= :outputs (reason #(first-request/prepare {} {} {} "one" bad))))))
    (with-redefs [pmk/prepare (fn [& _] (assoc-in parent [:closure 0 :owner-apis] {:effects first-request/required-apis}))]
      (is (= :first-selector (reason #(first-request/prepare {} {} {} "one" outputs)))))
    (doseq [changed [(assoc parent :closure [(first closure)])
                     (update parent :selection #(assoc % 14 "foreign/selector"))]]
      (with-redefs [pmk/prepare (fn [& _] changed)]
        (is (= :first-selector (reason #(first-request/prepare {} {} {} "one" outputs))))))))

(deftest missing-or-inconsistent-bindings-stop-before-verifier
  (let [r (request) expected (binding-input r) calls (atom 0) verify (fake-verifier calls)]
    (doseq [[why changed]
            [[:runtime-binding (assoc expected :runtime {})]
             [:runtime-binding (assoc-in expected [:runtime :ordered-classpath] [])]
             [:controller-binding (update-in expected [:controller :ports] dissoc :release)]
             [:controller-binding (update-in expected [:controller :ports] dissoc :claim)]
             [:guard-binding (assoc expected :guards [])]
             [:guard-binding (assoc-in expected [:guards 0 :root] "relative")]
             [:participant-coverage (assoc-in expected [:coverage :acquisition-order] [:other])]
             [:participant-coverage (assoc-in expected [:coverage :participants] [])]
             [:participant-coverage (assoc-in expected [:coverage :participants 0 :guards] [])]
             [:host-limits (assoc-in expected [:limits :whole-timeout-ms] 0)]
             [:host-limits (assoc-in expected [:limits :containment-sha256] nil)]
             [:host-limits (assoc-in expected [:limits :deadline-policy] :child-only)]
             [:host-limits (assoc-in expected [:limits :release-policy] :after-publication)]
             [:operation-history (assoc-in expected [:history :state] :denied)]
             [:operation-history (assoc-in expected [:history :state] :pending)]
             [:operation-history (assoc-in expected [:history :state] :uncertain)]
             [:operation-history (assoc-in expected [:history :state] :executed)]
             [:operation-history (assoc-in expected [:history :original-handle] 22232)]
             [:output-binding (assoc-in expected [:outputs :state] :exists)]]]
      ;; Changing supplied observations coherently cannot bypass required fields.
      (is (= why (reason #(first-request/bind-inputs r changed changed verify))) (str why)))
    (is (= :observed-binding
           (reason #(first-request/bind-inputs r expected (assoc-in expected [:runtime :java :sha256] (apply str (repeat 64 "f"))) verify))))
    (is (= :request-binding
           (reason #(first-request/bind-inputs (assoc r :operation-id "another") expected expected verify))))
    (doseq [bad [(assoc r :execution :passed) (assoc r :selection []) (assoc r :argv (:selection parent))
                 (assoc r :cwd "/other") (assoc r :inputs-sha256 "changed")]]
      (is (= :request-drift (reason #(first-request/bind-inputs bad expected expected verify)))))
    (is (zero? @calls))))

(deftest verifier-is-required-and-cannot-grant-execution
  (let [r (request) b (binding-input r) calls (atom 0)]
    (is (= :normal-verifier-unavailable (reason #(first-request/bind-inputs r b b nil))))
    (is (= :normal-verifier-refused (reason #(first-request/bind-inputs r b b (fn [& _] true)))))
    (let [result (first-request/bind-inputs r b b (fake-verifier calls))]
      (is (= 1 @calls))
      (is (= :ready-for-platform-assessment (:status result)))
      (is (= :unrun (:execution result)))
      (is (false? (:authenticated? result)))
      (is (= r (:request result))))))
