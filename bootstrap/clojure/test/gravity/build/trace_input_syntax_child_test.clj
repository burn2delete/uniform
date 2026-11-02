(ns gravity.build.trace-input-syntax-child-test
  "Injected DATA/IO controls only. Never call the genuine selector."
  (:require [clojure.edn :as edn]
            [clojure.test :as test :refer [deftest is testing]]
            [gravity.build.trace-input-syntax-child :as child]
            [gravity.build.trace-input-syntax-receipt :as receipt]
            [gravity.development-test-runner :as development])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption SecureDirectoryStream]
           [java.nio.file.attribute FileAttribute]))

(defn- data-observation []
  (let [entry (fn [n] {:event :trace-id-enter :ordinal n :resolve-returns (dec n)
                       :stored-id "DATA-stored" :invocation-current? true
                       :proof-transaction-absent? true :observed-roots-stable? true})
        prefix [{:event :macro-enter :same-parent? true} (entry 1)
                {:event :trace-id-return :ordinal 1 :result "DATA-stored"}
                {:event :resolve-return} (entry 2)]]
    {:phase :trace-control-complete :field :input-syntax-id
     :positive-events (conj prefix {:event :trace-id-return :ordinal 2 :result "DATA-stored"}
                            {:event :macro-return :report {:status :passed}})
     :public-positive-report {:status :passed :fixture :DATA}
     :changed-events (conj prefix {:event :trace-field-mutated :ordinal 2}
                           {:event :trace-id-return :ordinal 2 :result "DATA-changed"}
                           {:event :macro-return :report {:status :failed
                                                         :checks {:trace-replay-current? false}
                                                         :failed-checks [:trace-replay-current]}})
     :changed-error (ex-info "DATA original changed refusal" {:id "C5-UNRESOLVED"})
     :reached-second-id? true :earlier-gate-refusal? false
     :before "DATA-before" :after "DATA-after" :alias-restored? true
     :no-verifier-report-substitution? true :whole-rv1-closed? false
     :callback-printer-control-implemented? false :nested-child-alias-control-implemented? false}))

(defn- provenance []
  {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
   :test-source-sha256 (apply str (repeat 64 "c")) :selector receipt/selector
   :request-id "DATA-child-binding-not-an-execution" :evidence-kind :data-unit-control})

(defn- machine-result []
  {:schema development/machine-result-schema :authority :non-authoritative :authoritative? false
   :selection {:namespaces [(namespace receipt/selector)] :test-vars [(str receipt/selector)]
               :fail-fast? false}
   :summary {:test 1 :pass 27 :fail 0 :error 0} :outcome :passed :exit-code 0})

(defn- with-directory [f]
  (let [path (Files/createTempDirectory "trace-child-DATA-" (make-array FileAttribute 0))]
    (try
      (with-open [directory (Files/newDirectoryStream path)]
        (is (instance? SecureDirectoryStream directory))
        (f path directory))
      (finally
        (with-open [entries (Files/newDirectoryStream path)]
          (doseq [entry entries] (Files/delete entry)))
        (Files/delete path)))))

(defn- read-receipt [path]
  (edn/read-string (String. (Files/readAllBytes (.resolve path receipt/receipt-name))
                           StandardCharsets/UTF_8)))

(defn- data-runner [result]
  (fn [args]
    (is (= ["--namespace" (namespace receipt/selector) "--exact" (name receipt/selector)] args))
    (development/*machine-result-sink* result)
    (:exit-code result)))

(deftest actual-injected-sink-is-bound-once-and-collection-follows-return-and-restoration
  (with-directory
    (fn [path directory]
      (let [events (atom []) observation (data-observation) machine (machine-result)
            source (reify clojure.lang.IDeref
                     (deref [_] (swap! events conj :deref) observation))
            prior-sink (fn [result] (is (identical? machine result)) (swap! events conj :prior-sink))
            prior-report test/report]
        (binding [development/*machine-result-sink* prior-sink]
          (with-redefs [development/run-cli!
                        (fn [args]
                          (is (= ["--namespace" (namespace receipt/selector)
                                  "--exact" (name receipt/selector)] args))
                          (try (swap! events conj :run)
                               (finally (swap! events conj :fixtures-finally)))
                          (development/*machine-result-sink* machine)
                          (swap! events conj :return) 0)]
            (let [result (child/invoke! source directory
                                        (fn [n]
                                          (is (pos? n))
                                          (is (identical? prior-sink development/*machine-result-sink*))
                                          (is (identical? prior-report test/report))
                                          (swap! events conj :admit)) (provenance))
                  decoded (read-receipt path)]
              (is (= [:run :fixtures-finally :prior-sink :return :deref :admit] @events))
              (is (= 0 (:run-return result)))
              (is (= :consistent-supplied-observation (:binding-status result)))
              (is (identical? observation (get-in result [:collected :observation])))
              (is (identical? machine (get-in result [:collected :completion :machine-results 0])))
              (is (identical? (:changed-error observation)
                              (get-in result [:collected :observation :changed-error])))
              (is (= :data-unit-control (get-in decoded [:provenance :evidence-kind])))
              (is (= :constructed-unadmitted (:method-status decoded)))
              (is (false? (:genuine-execution-established? result)))
              (is (false? (:cache-admission? result)))
              (is (not (contains? decoded :cleanup-complete?)))
              (is (identical? prior-sink development/*machine-result-sink*))
              (is (identical? prior-report test/report)))))))))

(deftest missing-duplicate-and-incomplete-machine-results-stay-nonpassing
  (doseq [[label results return]
          [[:missing [] 0] [:duplicate [(machine-result) (machine-result)] 0]
           [:schema [(assoc (machine-result) :schema :OTHER)] 0]
           [:extra-field [(assoc (machine-result) :genuine true)] 0]
           [:extra-counter [(assoc-in (machine-result) [:summary :extra] 1)] 0]
           [:selection [(assoc-in (machine-result) [:selection :test-vars] ["DATA/other"])] 0]
           [:counters [(assoc-in (machine-result) [:summary :test] 0)] 0]
           [:missing-counters [(dissoc (machine-result) :summary)] 0]
           [:counter-shape [(assoc-in (machine-result) [:summary :pass] "DATA")] 0]
           [:outcome [(assoc (machine-result) :outcome :failed)] 0]
           [:return [(machine-result)] 7]]]
    (testing (name label)
      (with-directory
        (fn [path directory]
          (with-redefs [development/run-cli! (fn [_]
                                               (doseq [result results]
                                                 (development/*machine-result-sink* result)) return)]
            (let [result (child/invoke! (atom (data-observation)) directory (fn [_]) (provenance))
                  decoded (read-receipt path)]
              (is (= return (:run-return result)))
              (is (= :nonpassing (:binding-status result)))
              (is (= :nonpassing (:validation decoded)))
              (is (= (count results) (count (get-in decoded [:completion :machine-results])))))))))))

(deftest child-local-report-port-retains-raw-error-and-delegates-original-reporter
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA caught by test-var" {:fixture :DATA})
            report {:type :error :actual original :fixture :DATA}
            delegated (atom []) original-report test/report
            reporter (fn [value] (if (= :DATA (:fixture value))
                                   (swap! delegated conj value) (original-report value)))
            machine (assoc (machine-result) :summary {:test 1 :pass 2 :fail 0 :error 1}
                           :outcome :failed :exit-code 1)]
        (binding [test/report reporter]
          (with-redefs [development/run-cli! (fn [_]
                                               (test/report report)
                                               (development/*machine-result-sink* machine) 1)]
            (let [result (child/invoke! (atom nil) directory (fn [_]) (provenance))]
              (is (= 1 (:run-return result)))
              (is (= :nonpassing (:binding-status result)))
              (is (identical? report (first @delegated)))
              (is (identical? report (get-in result [:collected :completion :failure-reports 0])))
              (is (identical? original (get-in result [:collected :completion :error])))
              (is (= {:fixture :DATA} (get-in (read-receipt path) [:completion :error :data])))
              (is (identical? reporter test/report)))))))))

(deftest passing-machine-counters-cannot-promote-absent-or-incomplete-observations
  (doseq [observation [nil {:phase :constructing-fresh-trace-parent}
                       (assoc (data-observation) :field :output-syntax-id)
                       (assoc (data-observation) :alias-restored? false)]]
    (with-directory
      (fn [path directory]
        (with-redefs [development/run-cli! (data-runner (machine-result))]
          (let [result (child/invoke! (atom observation) directory (fn [_]) (provenance))]
            (is (= 0 (:run-return result)))
            (is (= :nonpassing (:binding-status result)))
            (is (identical? observation (get-in result [:collected :observation])))
            (is (= :nonpassing (:validation (read-receipt path))))))))))

(deftest original-runner-throw-is-published-nonpassing-and-rethrown-identically
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA fixture throw" {:fixture :DATA})
            sink (fn [_] (throw (AssertionError. "must not invent a sink call")))
            reporter test/report]
        (binding [development/*machine-result-sink* sink]
          (with-redefs [development/run-cli! (fn [_] (throw original))]
            (is (identical? original
                            (try (child/invoke! (atom nil) directory (fn [_]) (provenance))
                                 (catch Throwable error error))))
            (is (identical? sink development/*machine-result-sink*))
            (is (identical? reporter test/report))))
        (let [decoded (read-receipt path)]
          (is (= :nonpassing (:validation decoded)))
          (is (false? (get-in decoded [:completion :returned?])))
          (is (= [] (get-in decoded [:completion :machine-results])))
          (is (= {:fixture :DATA} (get-in decoded [:completion :run-error :data]))))))))

(deftest original-prior-sink-throw-is-not-replaced-or-retried
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA prior sink refusal" {:fixture :DATA}) calls (atom 0)
            sink (fn [_] (swap! calls inc) (throw original))]
        (binding [development/*machine-result-sink* sink]
          (with-redefs [development/run-cli! (data-runner (machine-result))]
            (is (identical? original
                            (try (child/invoke! (atom (data-observation)) directory (fn [_]) (provenance))
                                 (catch Throwable error error))))
            (is (= 1 @calls))
            (is (identical? sink development/*machine-result-sink*))))
        (is (= :nonpassing (:validation (read-receipt path))))))))

(deftest owner-capture-refusal-retains-run-return-and-raw-error-without-output
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA byte admission refusal" {:fixture :DATA})]
        (with-redefs [development/run-cli! (data-runner (machine-result))]
          (let [result (child/invoke! (atom (data-observation)) directory
                                      (fn [_] (throw original)) (provenance))]
            (is (= 0 (:run-return result)))
            (is (= :nonpassing (:binding-status result)))
            (is (identical? original (:publication-error result)))
            (is (false? (Files/exists (.resolve path receipt/receipt-name) (make-array LinkOption 0))))))))))

(deftest reporter-throw-restores-both-prior-bindings-and-preserves-original-error
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA prior reporter throw" {})
            ordinary test/report
            reporter (fn [report] (if (= :DATA (:fixture report)) (throw original) (ordinary report)))
            sink (fn [_])]
        (binding [test/report reporter development/*machine-result-sink* sink]
          (with-redefs [development/run-cli! (fn [_] (test/report {:type :error :fixture :DATA
                                                                  :actual original}))]
            (is (identical? original
                            (try (child/invoke! (atom nil) directory (fn [_]) (provenance))
                                 (catch Throwable error error))))
            (is (identical? reporter test/report))
            (is (identical? sink development/*machine-result-sink*))))
        (is (= :nonpassing (:validation (read-receipt path))))))))

(deftest observation-read-throw-is-retained-without-a-second-read-or-publication
  (with-directory
    (fn [path directory]
      (let [original (ex-info "DATA observation read throw" {}) reads (atom 0)
            source (reify clojure.lang.IDeref (deref [_] (swap! reads inc) (throw original)))]
        (with-redefs [development/run-cli! (data-runner (machine-result))]
          (let [result (child/invoke! source directory
                                      (fn [_] (throw (AssertionError. "must not publish"))) (provenance))]
            (is (= 1 @reads))
            (is (= 0 (:run-return result)))
            (is (= :nonpassing (:binding-status result)))
            (is (identical? original (:collection-error result)))
            (is (false? (Files/exists (.resolve path receipt/receipt-name) (make-array LinkOption 0))))))))))

(deftest evidence-failure-is-suppressed-on-original-runner-error
  (with-directory
    (fn [_ directory]
      (let [original (ex-info "DATA run throw" {}) capture (ex-info "DATA capture refusal" {})]
        (with-redefs [development/run-cli! (fn [_] (throw original))]
          (is (identical? original
                          (try (child/invoke! (atom nil) directory (fn [_] (throw capture)) (provenance))
                               (catch Throwable error error))))
          (is (= 1 (alength (.getSuppressed original))))
          (is (identical? capture (first (.getSuppressed original)))))))))

(deftest create-new-refusal-never-overwrites-or-retries
  (with-directory
    (fn [path directory]
      (let [calls (atom 0) target (.resolve path receipt/receipt-name)
            bytes (.getBytes "DATA prior receipt" StandardCharsets/UTF_8)]
        (Files/write target bytes (make-array java.nio.file.OpenOption 0))
        (with-redefs [development/run-cli! (fn [_] (swap! calls inc)
                                            (development/*machine-result-sink* (machine-result)) 0)]
          (let [result (child/invoke! (atom (data-observation)) directory (fn [_]) (provenance))]
            (is (= 1 @calls))
            (is (= 0 (:run-return result)))
            (is (= :nonpassing (:binding-status result)))
            (is (instance? java.nio.file.FileAlreadyExistsException (:publication-error result)))
            (is (= (vec bytes) (vec (Files/readAllBytes target))))))))))

(deftest no-heavy-namespace-is-loaded-and-invalid-ports-do-not-call-runner
  (with-redefs [development/run-cli! (fn [_] (throw (AssertionError. "must not execute")))]
    (is (thrown? clojure.lang.ExceptionInfo (child/invoke! (atom nil) nil (fn [_]) (provenance)))))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.self-hosting.resolution-parent-mutation-test))))
