(ns gravity.self-hosting.sh01-development-test-cache-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-development-test-cache :as cache])
  (:import [java.nio.file Files LinkOption Path StandardOpenOption]
           [java.nio.file.attribute FileAttribute]))

(defn- sha [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- input [id character]
  {:id id :sha256 (sha character)})

(defn- request [^Path directory test-id]
  {:cache-directory directory
   :maximum-entries 2
   :test-identity (input test-id \7)
   :test-policy {:authority :non-authoritative
                 :deterministic? true
                 :performance? false
                 :proof? false
                 :freshness-required? false
                 :timeout-ms 60000}
   :dependencies
   {:complete? true
    :production-inputs [(input :source \a)]
    :transitive-production-inputs [(input :dependency \b)]
    :fixture-contract-inputs [(input :fixture \c) (input :contract \d)]
    :runner-identity (input :runner \e)
    :runtime-tool-inputs [(input :clojure \f) (input :jdk \0)]}})

(defn- passed [value]
  {:status :passed
   :exit-code 0
   :authority :non-authoritative
   :authoritative? false
   :timed-out? false
   :value value})

(defn- delete-tree! [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defmacro with-cache-directory [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-development-test-cache-"
                   (make-array FileAttribute 0))]
     (try ~@body (finally (delete-tree! ~binding)))))

(deftest successful-result-persists-and-reopens-as-a-nonauthoritative-hit
  (with-cache-directory [directory]
    (let [calls (atom 0)
          operation #(do (swap! calls inc) (passed :fresh))
          first-run (cache/lookup-or-run! (request directory :example/test)
                                          operation)
          second-run (cache/lookup-or-run! (request directory :example/test)
                                           operation)]
      (is (= 1 @calls))
      (is (= :miss (get-in first-run [:receipt :decision])))
      (is (= :not-found (get-in first-run [:receipt :reason])))
      (is (true? (get-in first-run [:receipt :stored?])))
      (is (= :hit (get-in second-run [:receipt :decision])))
      (is (= (:result first-run) (:result second-run)))
      (is (= :non-authoritative (get-in second-run [:receipt :authority])))
      (is (false? (get-in second-run [:receipt :authoritative?])))
      (is (true? (get-in second-run
                         [:receipt :fresh-authoritative-run-required?]))))))

(deftest key-covers-declared-transitive-fixture-contract-runner-and-runtime-inputs
  (with-cache-directory [directory]
    (let [base (request directory :identity/test)
          mutations
          [(assoc-in base [:test-identity :id] :identity/other)
           (assoc-in base [:test-identity :sha256] (sha \8))
           (assoc-in base [:dependencies :production-inputs 0 :sha256] (sha \1))
           (assoc-in base [:dependencies :transitive-production-inputs 0 :sha256] (sha \2))
           (assoc-in base [:dependencies :fixture-contract-inputs 0 :sha256] (sha \3))
           (assoc-in base [:dependencies :fixture-contract-inputs 1 :sha256] (sha \4))
           (assoc-in base [:dependencies :runner-identity :sha256] (sha \5))
           (assoc-in base [:dependencies :runtime-tool-inputs 0 :sha256] (sha \6))
           (assoc-in base [:test-policy :timeout-ms] 120000)]]
      (is (every? #(not= (cache/cache-key base) (cache/cache-key %)) mutations))
      (is (= (cache/cache-key base)
             (cache/cache-key
              (-> base
                  (update-in [:dependencies :fixture-contract-inputs]
                             #(vec (reverse %)))
                  (update-in [:dependencies :runtime-tool-inputs]
                             #(vec (reverse %))))))))))

(deftest changed-input-invalidates-the-prior-test-result
  (with-cache-directory [directory]
    (let [calls (atom 0)
          base (request directory :changed/test)
          changed (assoc-in base [:dependencies :transitive-production-inputs 0
                                  :sha256]
                            (sha \9))
          operation #(passed (swap! calls inc))]
      (cache/lookup-or-run! base operation)
      (let [invalidated (cache/lookup-or-run! changed operation)
            hit (cache/lookup-or-run! changed operation)]
        (is (= 2 @calls))
        (is (= :invalidation (get-in invalidated [:receipt :decision])))
        (is (= :input-closure-changed (get-in invalidated [:receipt :reason])))
        (is (= 1 (count (get-in invalidated
                                [:receipt :invalidated-cache-keys]))))
        (is (= :hit (get-in hit [:receipt :decision])))))))

(deftest incomplete-and-excluded-tests-always-run-fresh-and-never-store
  (with-cache-directory [directory]
    (doseq [[label update-request expected-reason]
            [[:incomplete #(assoc-in % [:dependencies :complete?] false)
              :incomplete-dependencies]
             [:authoritative #(assoc-in % [:test-policy :authority]
                                        :authoritative)
              :authoritative-test]
             [:nondeterministic #(assoc-in % [:test-policy :deterministic?]
                                          false)
              :nondeterministic-test]
             [:performance #(assoc-in % [:test-policy :performance?] true)
              :performance-test]
             [:fresh-proof #(-> %
                               (assoc-in [:test-policy :proof?] true)
                               (assoc-in [:test-policy :freshness-required?]
                                         true))
              :freshness-required-proof-test]]]
      (testing (name label)
        (let [calls (atom 0)
              selected (update-request (request directory label))
              operation #(passed (swap! calls inc))
              first-run (cache/lookup-or-run! selected operation)
              second-run (cache/lookup-or-run! selected operation)]
          (is (= 2 @calls))
          (is (= :miss (get-in first-run [:receipt :decision])))
          (is (= expected-reason (get-in first-run [:receipt :reason])))
          (is (false? (get-in first-run [:receipt :stored?])))
          (is (= :miss (get-in second-run [:receipt :decision]))))))))

(deftest failures-timeouts-and-authoritative-results-are-never-reused
  (with-cache-directory [directory]
    (doseq [[label result]
            [[:failure {:status :failed :exit-code 1
                        :authority :non-authoritative
                        :authoritative? false :timed-out? false}]
             [:timeout {:status :passed :exit-code 0
                        :authority :non-authoritative
                        :authoritative? false :timed-out? true}]
             [:authoritative-result {:status :passed :exit-code 0
                                     :authority :authoritative
                                     :authoritative? true :timed-out? false}]
             [:malformed-exit {:status :passed :exit-code "0"
                               :authority :non-authoritative
                               :authoritative? false :timed-out? false}]]]
      (testing (name label)
        (let [calls (atom 0)
              operation #(do (swap! calls inc) result)
              selected (request directory label)
              first-run (cache/lookup-or-run! selected operation)
              second-run (cache/lookup-or-run! selected operation)]
          (is (= 2 @calls))
          (is (= :result-not-reusable
                 (get-in first-run [:receipt :reason])))
          (is (false? (get-in first-run [:receipt :stored?])))
          (is (= :miss (get-in second-run [:receipt :decision]))))))))

(deftest persistent-entry-bound-evicts-the-oldest-success
  (with-cache-directory [directory]
    (let [calls (atom {})
          run! (fn [test-id]
                 (cache/lookup-or-run!
                  (assoc (request directory test-id) :maximum-entries 2)
                  #(do (swap! calls update test-id (fnil inc 0))
                       (passed test-id))))]
      (run! :test/one)
      (run! :test/two)
      (let [third (run! :test/three)]
        (is (= 1 (count (get-in third [:receipt :evicted-cache-keys])))))
      (run! :test/one)
      (is (= 2 (get @calls :test/one)))
      (let [stored (edn/read-string
                    (String. (Files/readAllBytes
                              (.resolve directory "results-v1.edn"))
                             java.nio.charset.StandardCharsets/UTF_8))]
        (is (= 2 (count (:entries stored))))))))

(deftest corrupt-persistence-fails-closed-without-overwrite-or-reuse
  (with-cache-directory [directory]
    (let [cache-file (.resolve directory "results-v1.edn")
          bytes (.getBytes "{:schema :tampered}" "UTF-8")]
      (Files/write cache-file bytes
                   (into-array java.nio.file.OpenOption
                               [StandardOpenOption/CREATE
                                StandardOpenOption/TRUNCATE_EXISTING
                                StandardOpenOption/WRITE]))
      (let [calls (atom 0)
            selected (request directory :corrupt/test)
            operation #(passed (swap! calls inc))
            result (cache/lookup-or-run! selected operation)]
        (is (= 1 @calls))
        (is (= :invalidation (get-in result [:receipt :decision])))
        (is (= :invalid-cache-content (get-in result [:receipt :reason])))
        (is (false? (get-in result [:receipt :stored?])))
        (is (= "{:schema :tampered}" (String. (Files/readAllBytes cache-file)
                                               "UTF-8")))))))
