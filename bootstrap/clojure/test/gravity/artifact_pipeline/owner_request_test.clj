(ns gravity.artifact-pipeline.owner-request-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [gravity.artifact-pipeline.owner-request :as owner]
            [gravity.artifact-pipeline.parent-controller :as controller]))

(use-fixtures :each
  (fn [test]
    (test)
    (doseq [namespace '[gravity.bootstrap gravity.self-hosting.sh01-host-resource-broker
                        gravity.self-hosting.sh01-parallel-test-runner gravity.development-test-runner]]
      (is (nil? (find-ns namespace))))))

(defn fixture []
  (let [calls (atom []) lease (Object.)
        request {:cwd "/fake/child" :argv controller/argv :outputs {:attempt "/fake/claim" :stdout "/fake/stdout"
                                                                    :stderr "/fake/stderr" :final "/fake/final"}
                 :guards [{:kind :broker :acquisition :broker :resource-class :exclusive :root "/fake/git-common/broker"}
                          {:kind :canonical :acquisition :non-broker :root owner/canonical-path}]}
        proposal {:request request :actual-child-command ["/fake/java" "-Xmx2g" "clojure.main"]}
        normal {:acquire (fn [& args] (swap! calls conj [:acquire args]) lease)
                :validate (fn [handle & args] (swap! calls conj [:validate args]) handle)
                :release (fn [handle] (is (identical? lease handle)) {:outcome :released :capacity 1N})
                :start (fn [& args] (swap! calls conj [:start args]) :fake-process)
                :supervise (fn [& args] (swap! calls conj [:supervise args]) {:stdout "captured" :stderr "" :opaque lease})}]
    {:calls calls :lease lease :proposal proposal :ports (#'owner/assemble-ports proposal normal)}))

(deftest broker-ports-preserve-real-arity-root-class-and-raw-handle
  (let [{:keys [ports proposal lease calls]} (fixture) guard (get-in proposal [:request :guards 0])]
    (is (identical? lease ((:acquire ports) guard 123)))
    (is (identical? lease ((:validate ports) guard lease)))
    (is (= {:status :released :broker-receipt {:outcome :released :capacity 1N}} ((:release ports) guard lease)))
    (is (= [[:acquire (list {:coordination-root "/fake/git-common/broker" :timeout-ms 123} :exclusive)]
            [:validate (list "/fake/git-common/broker" :exclusive)]] @calls))))

(deftest canonical-dispatch-is-separate-and-retains-the-original-handle
  (let [{:keys [ports proposal calls]} (fixture) guard (get-in proposal [:request :guards 1]) handle (Object.)]
    (with-redefs [owner/canonical-acquire! (fn [] (swap! calls conj :canonical) handle)
                  owner/canonical-validate! (fn [value] (is (identical? handle value)) value)
                  owner/canonical-release! (fn [value] (is (identical? handle value)) {:status :released})]
      (is (identical? handle ((:acquire-guard ports) guard 100)))
      (is (identical? handle ((:validate ports) guard handle)))
      (is (= :released (:status ((:release ports) guard handle))))
      (is (= [:canonical] @calls))
      (is (thrown? clojure.lang.ExceptionInfo ((:acquire ports) guard 100)))
      (is (= [:canonical] @calls)))))

(deftest exact-start-arguments-and-source-observation-precede-launch
  (let [{:keys [ports proposal calls]} (fixture) observations (atom 0)]
    (with-redefs [owner/verify! (fn [value] (is (= proposal value)) (swap! observations inc))]
      (is (thrown? clojure.lang.ExceptionInfo ((:start ports) ["foreign"] "/fake/child" 100)))
      (is (thrown? clojure.lang.ExceptionInfo ((:start ports) controller/argv "/foreign" 100)))
      (is (empty? @calls)) (is (zero? @observations))
      (is (= :fake-process ((:start ports) controller/argv "/fake/child" 100)))
      (is (= 1 @observations))
      (is (= [[:start (list (:actual-child-command proposal) "/fake/child")]] @calls)))))

(deftest capture-option-mapping-and-publication-retain-raw-results
  (let [{:keys [ports calls lease]} (fixture) writes (atom [])
        limits (assoc owner/proposed-limits :timeout-ms 20000)]
    (with-redefs [owner/create-output! (fn [path bytes] (swap! writes conj [path (String. ^bytes bytes "UTF-8")]))]
      (let [result ((:supervise ports) :fake-process limits)]
        (is (identical? lease (:opaque result)))
        (is (empty? @writes))
        (is (= [[:supervise (list :fake-process {:timeout-ms 8200 :max-capture-bytes 1048576
                                                 :max-capture-chars 1048576 :capture-wait-ms 5000})]] @calls))
        (is (= {:status :published} ((:publish ports) "/fake/final" {:status :failed})))
        (is (= [["/fake/stdout" "captured"] ["/fake/stderr" ""] ["/fake/final" "{:status :failed}\n"]] @writes))
        (is (thrown? clojure.lang.ExceptionInfo ((:supervise ports) :fake-process limits)))
        (is (thrown? clojure.lang.ExceptionInfo ((:publish ports) "/fake/final" {})))
        (is (= 3 (count @writes)))))))

(deftest insufficient-remaining-time-refuses-before-normal-supervision
  (let [{:keys [ports calls]} (fixture)]
    (is (thrown? clojure.lang.ExceptionInfo ((:supervise ports) :fake-process {:timeout-ms 11800})))
    (is (empty? @calls))))

(deftest parent-acceptance-is-not-minted-by-proposal-data
  (let [{:keys [ports calls]} (fixture)
        error (try ((:admit ports) {:accepted true} {:accepted true} 100) nil
                   (catch clojure.lang.ExceptionInfo error error))]
    (is (= :own-profile-unaccepted (:reason (ex-data error))))
    (is (empty? @calls))))

(deftest finalizer-consumes-once-without-inventing-descendant-completeness
  (let [{:keys [ports]} (fixture) calls (atom []) original (Exception. "original") cleanup (Object.)]
    (with-redefs [owner/owned-finalizer (fn [& args] (swap! calls conj args) {:status :unknown})]
      (is (= {:status :unknown} ((:finish ports) :fake-process {:cleanup cleanup} original {:cleanup-ms 1500})))
      (is (identical? cleanup (:cleanup (second (first @calls)))))
      (is (identical? original (nth (first @calls) 2)))
      (is (thrown? clojure.lang.ExceptionInfo ((:finish ports) :fake-process nil nil nil)))
      (is (= 1 (count @calls)))))
  (let [process (proxy [Process] [] (isAlive [] false)) cleanup (Object.)
        result (owner/owned-finalizer process {:cleanup cleanup} nil {})]
    (is (= :unknown (:status result)))
    (is (false? (:owned-descendants-complete? result)))
    (is (false? (:root-alive? result)))
    (is (identical? cleanup (:runner-cleanup result)))))

(deftest mutated-request-or-command-refuses-before-any-input-read-or-resolution
  (doseq [proposal [{:request {:id "changed"} :request-sha256 "foreign"}
                    {:request {} :request-sha256 (owner/sha256 (.getBytes "{}" "UTF-8"))
                     :actual-child-command ["foreign"] :command-sha256 "foreign"}]]
    (is (thrown? clojure.lang.ExceptionInfo (owner/operation-ports proposal)))))
