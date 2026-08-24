(ns gravity.self-hosting.sh01-development-test-cache-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-development-test-cache :as cache])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path Paths StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.util.concurrent CountDownLatch TimeUnit]))

(def ^:private no-file-attributes (make-array FileAttribute 0))
(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private write-options
  (into-array java.nio.file.OpenOption
              [StandardOpenOption/CREATE
               StandardOpenOption/TRUNCATE_EXISTING
               StandardOpenOption/WRITE]))

(defn- sha [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- file-input [path character]
  {:path path :sha256 (sha character)})

(defn- named-input [id character]
  {:id id :sha256 (sha character)})

(defn- request [^Path directory test-id]
  {:cache-directory directory
   :repository-identity (sha \1)
   :test-identity (named-input test-id \7)
   :test-policy {:authority :non-authoritative
                 :deterministic? true
                 :performance? false
                 :proof? false
                 :freshness-required? false
                 :timeout-ms 60000}
   :dependencies
   {:complete? true
    :production-inputs [(file-input
                         "bootstrap/clojure/src/gravity/compiler.clj" \a)]
    :transitive-production-inputs [(file-input
                                    "bootstrap/clojure/src/gravity/digest.clj" \b)]
    :fixture-contract-inputs [(file-input "fixtures/sh01/example.edn" \c)
                              (file-input "contracts/sh01-example.edn" \d)]
    :runner-identity (named-input :clojure-test-runner \e)
    :classpath-inputs [(file-input "deps.edn" \6)
                       (file-input "bootstrap/clojure/test" \8)]
    :runtime-tool-inputs [(named-input :clojure \f)
                          (named-input :jdk \0)]}})

(defn- passed [value]
  {:status :passed
   :exit-code 0
   :authority :non-authoritative
   :authoritative? false
   :timed-out? false
   :nondeterministic? false
   :performance? false
   :proof? false
   :freshness-required? false
   :value value})

(defn- delete-tree! [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root
                                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defmacro with-cache-directory [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-development-test-cache-"
                   no-file-attributes)]
     (try ~@body (finally (delete-tree! ~binding)))))

(defn- entry-path [^Path directory selected]
  (.resolve (.resolve (.resolve directory "v2") "entries")
            (str (subs (cache/cache-key selected) 7) ".edn")))

(defn- lock-path [^Path directory selected]
  (.resolve (.resolve (.resolve directory "v2") "locks")
            (str (subs (cache/cache-key selected) 7) ".lock")))

(defn- read-text [^Path path]
  (String. (Files/readAllBytes path) StandardCharsets/UTF_8))

(defn- write-text! [^Path path text]
  (Files/write path (.getBytes ^String text StandardCharsets/UTF_8)
               write-options))

(defn- exception-data [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- await-path! [^Path path timeout-ms]
  (let [deadline (+ (System/nanoTime) (* timeout-ms 1000000))]
    (loop []
      (cond
        (Files/exists path no-links) true
        (< (System/nanoTime) deadline)
        (do (Thread/sleep 10) (recur))
        :else false))))

(deftest successful-result-is-an-immutable-nonauthoritative-hit
  (with-cache-directory [directory]
    (let [calls (atom 0)
          selected (request directory :example/test)
          operation #(do (swap! calls inc) (passed :fresh))
          first-run (cache/lookup-or-run! selected operation)
          second-run (cache/lookup-or-run! selected operation)
          third-run (cache/lookup-or-run! selected operation)
          stored-path (entry-path directory selected)]
      (is (= 1 @calls))
      (is (= :miss (get-in first-run [:receipt :decision])))
      (is (= :not-found (get-in first-run [:receipt :reason])))
      (is (true? (get-in first-run [:receipt :stored?])))
      (is (= :hit (get-in second-run [:receipt :decision])))
      (is (= (:receipt second-run) (:receipt third-run)))
      (is (= (:result first-run) (:result second-run)))
      (is (Files/isRegularFile stored-path no-links))
      (is (not (Files/exists (.resolve directory "results-v1.edn") no-links)))
      (doseq [receipt (map :receipt [first-run second-run third-run])]
        (is (= :non-authoritative (:authority receipt)))
        (is (false? (:authoritative? receipt)))
        (is (false? (:cache-authoritative? receipt)))
        (is (true? (:fresh-authoritative-run-required? receipt)))
        (is (false? (:release-authority? receipt)))
        (is (false? (:proof-authority? receipt)))
        (is (false? (:self-hosting-authority? receipt)))))))

(deftest content-key-is-cross-worktree-and-covers-the-complete-declared-identity
  (with-cache-directory [first-directory]
    (with-cache-directory [second-directory]
      (let [base (request first-directory :identity/test)
            other-root (assoc base :cache-directory second-directory)
            mutations
            [(assoc base :repository-identity (sha \2))
             (assoc-in base [:test-identity :id] :identity/other)
             (assoc-in base [:test-identity :sha256] (sha \3))
             (assoc-in base [:dependencies :production-inputs 0 :sha256] (sha \4))
             (assoc-in base [:dependencies :transitive-production-inputs 0
                             :sha256] (sha \5))
             (assoc-in base [:dependencies :fixture-contract-inputs 0 :sha256]
                       (sha \6))
             (assoc-in base [:dependencies :runner-identity :sha256] (sha \7))
             (assoc-in base [:dependencies :classpath-inputs 0 :sha256] (sha \9))
             (assoc-in base [:dependencies :runtime-tool-inputs 0 :sha256]
                       (sha \a))
             (assoc-in base [:test-policy :timeout-ms] 120000)]]
        (is (= (cache/cache-key base) (cache/cache-key other-root)))
        (is (every? #(not= (cache/cache-key base) (cache/cache-key %))
                    mutations))
        (is (= (cache/cache-key base)
               (cache/cache-key
                (-> base
                    (update-in [:dependencies :fixture-contract-inputs]
                               #(vec (reverse %)))
                    (update-in [:dependencies :classpath-inputs]
                               #(vec (reverse %)))
                    (update-in [:dependencies :runtime-tool-inputs]
                               #(vec (reverse %)))))))))))

(deftest receipts-are-deterministic-and-exclude-cache-and-worktree-paths
  (with-cache-directory [first-directory]
    (with-cache-directory [second-directory]
      (let [first (cache/lookup-or-run! (request first-directory :receipt/test)
                                        #(passed :same))
            second (cache/lookup-or-run! (request second-directory :receipt/test)
                                         #(passed :same))
            rendered (pr-str [(:receipt first) (:receipt second)])]
        (is (= (:receipt first) (:receipt second)))
        (is (not (str/includes? rendered (str first-directory))))
        (is (not (str/includes? rendered (str second-directory))))
        (is (= (:receipt-id (:receipt first))
               (:receipt-id (:receipt second))))))))

(deftest mutable-and-unsafe-path-identities-are-rejected-not-keyed
  (with-cache-directory [directory]
    (let [base (request directory :invalid-identity/test)
          invalid
          [[:branch (assoc base :branch-name "feature/cache")
            :unsupported-request-fields]
           [:worktree-root (assoc base :worktree-root "/tmp/checkout")
            :unsupported-request-fields]
           [:absolute (assoc-in base [:dependencies :production-inputs 0 :path]
                                "/tmp/source.clj")
            :incomplete-dependencies]
           [:traversal (assoc-in base [:dependencies :production-inputs 0 :path]
                                 "../source.clj")
            :incomplete-dependencies]
           [:dot-segment (assoc-in base [:dependencies :production-inputs 0 :path]
                                   "src/./source.clj")
            :incomplete-dependencies]
           [:backslash (assoc-in base [:dependencies :production-inputs 0 :path]
                                 "src\\source.clj")
            :incomplete-dependencies]
           [:drive (assoc-in base [:dependencies :production-inputs 0 :path]
                             "C:/source.clj")
            :incomplete-dependencies]]]
      (doseq [[label selected reason] invalid]
        (testing (name label)
          (let [data (exception-data #(cache/cache-key selected))
                calls (atom 0)
                first-run (cache/lookup-or-run!
                           selected #(passed (swap! calls inc)))
                second-run (cache/lookup-or-run!
                            selected #(passed (swap! calls inc)))]
            (is (= "DEV-TEST-CACHE-INELIGIBLE" (:id data)))
            (is (= reason (:reason data)))
            (is (= 2 @calls))
            (is (= reason (get-in first-run [:receipt :reason])))
            (is (= :miss (get-in second-run [:receipt :decision])))
            (is (false? (get-in first-run [:receipt :cacheable?])))))))))

(deftest invalid-unbounded-identities-still-return-a-bounded-fresh-receipt
  (with-cache-directory [directory]
    (let [selected (-> (request directory :invalid/test)
                       (assoc :repository-identity {:unsupported (Object.)})
                       (assoc :test-identity
                              {:id (apply str (repeat 513 "x"))
                               :sha256 (sha \1)}))
          calls (atom 0)
          result (cache/lookup-or-run! selected #(passed (swap! calls inc)))]
      (is (= 1 @calls))
      (is (= :incomplete-repository-identity
             (get-in result [:receipt :reason])))
      (is (= "DEV-TEST-CACHE-INELIGIBLE"
             (get-in result [:receipt :diagnostic-id])))
      (is (nil? (get-in result [:receipt :repository-identity])))
      (is (nil? (get-in result [:receipt :test-identity])))
      (is (string? (get-in result [:receipt :receipt-id]))))))

(deftest metadata-bearing-logical-identity-runs-fresh
  (with-cache-directory [directory]
    (let [selected (assoc-in (request directory :metadata/test)
                             [:test-identity :id]
                             (with-meta 'metadata/test {:source :mutable}))
          calls (atom 0)
          result (cache/lookup-or-run! selected #(passed (swap! calls inc)))]
      (is (= 1 @calls))
      (is (= :incomplete-test-identity (get-in result [:receipt :reason])))
      (is (false? (get-in result [:receipt :cacheable?])))
      (is (nil? (get-in result [:receipt :test-identity]))))))

(deftest aggregate-dependency-identity-bound-runs-fresh
  (with-cache-directory [directory]
    (let [inputs (mapv #(file-input (str "src/generated-" % ".clj") \a)
                       (range 4089))
          selected (assoc-in (request directory :bounded-closure/test)
                             [:dependencies :production-inputs]
                             inputs)
          calls (atom 0)
          first-run (cache/lookup-or-run! selected
                                          #(passed (swap! calls inc)))
          second-run (cache/lookup-or-run! selected
                                           #(passed (swap! calls inc)))]
      (is (= 2 @calls))
      (is (= :incomplete-dependencies
             (get-in first-run [:receipt :reason])))
      (is (false? (get-in first-run [:receipt :cacheable?])))
      (is (= :miss (get-in second-run [:receipt :decision]))))))

(deftest excluded-or-incomplete-requests-always-run-fresh
  (with-cache-directory [directory]
    (doseq [[label update-request expected-reason]
            [[:incomplete #(assoc-in % [:dependencies :complete?] false)
              :incomplete-dependencies]
             [:missing-classpath #(update % :dependencies dissoc
                                          :classpath-inputs)
              :incomplete-dependencies]
             [:authoritative #(assoc-in % [:test-policy :authority]
                                        :authoritative)
              :authoritative-test]
             [:nondeterministic #(assoc-in % [:test-policy :deterministic?]
                                           false)
              :nondeterministic-test]
             [:performance #(assoc-in % [:test-policy :performance?] true)
              :performance-test]
             [:proof #(assoc-in % [:test-policy :proof?] true)
              :proof-test]
             [:freshness #(assoc-in % [:test-policy :freshness-required?]
                                    true)
              :freshness-required-test]]]
      (testing (name label)
        (let [calls (atom 0)
              selected (update-request (request directory label))
              operation #(passed (swap! calls inc))
              first-run (cache/lookup-or-run! selected operation)
              second-run (cache/lookup-or-run! selected operation)]
          (is (= 2 @calls))
          (is (= expected-reason (get-in first-run [:receipt :reason])))
          (is (= "DEV-TEST-CACHE-INELIGIBLE"
                 (get-in first-run [:receipt :diagnostic-id])))
          (is (false? (get-in first-run [:receipt :stored?])))
          (is (= :miss (get-in second-run [:receipt :decision]))))))))

(deftest failed-timed-out-authoritative-and-unbounded-results-are-never-reused
  (with-cache-directory [directory]
    (doseq [[label result expected-reason]
            [[:failure {:status :failed :exit-code 1
                        :authority :non-authoritative
                        :authoritative? false :timed-out? false}
              :result-not-reusable]
             [:timeout (assoc (passed :timeout) :timed-out? true)
              :result-not-reusable]
             [:authoritative-result (assoc (passed :authority)
                                           :authority :authoritative
                                           :authoritative? true)
              :result-not-reusable]
             [:nondeterministic-result (assoc (passed :random)
                                              :nondeterministic? true)
              :result-not-reusable]
             [:performance-result (assoc (passed :timing) :performance? true)
              :result-not-reusable]
             [:proof-result (assoc (passed :theorem) :proof? true)
              :result-not-reusable]
             [:fresh-result (assoc (passed :fresh) :freshness-required? true)
              :result-not-reusable]
             [:malformed-exit (assoc (passed :exit) :exit-code "0")
              :result-not-reusable]
             [:unsupported-value (assoc (passed :double) :value 1.5)
              :result-not-persistable]
             [:oversized (assoc (passed :large) :value
                                (apply str (repeat (* 1024 1024) "x")))
              :result-not-persistable]]]
      (testing (name label)
        (let [calls (atom 0)
              selected (request directory label)
              operation #(do (swap! calls inc) result)
              first-run (cache/lookup-or-run! selected operation)
              second-run (cache/lookup-or-run! selected operation)]
          (is (= 2 @calls))
          (is (= expected-reason (get-in first-run [:receipt :reason])))
          (is (false? (get-in first-run [:receipt :stored?])))
          (is (= :miss (get-in second-run [:receipt :decision]))))))))

(deftest corrupt-entry-is-rejected-preserved-and-never-overwritten
  (with-cache-directory [directory]
    (let [selected (request directory :corrupt/test)
          initial (cache/lookup-or-run! selected #(passed :original))
          stored-path (entry-path directory selected)
          corrupt (str (read-text stored-path) " ")]
      (is (true? (get-in initial [:receipt :stored?])))
      (write-text! stored-path corrupt)
      (let [calls (atom 0)
            operation #(passed (swap! calls inc))
            first-run (cache/lookup-or-run! selected operation)
            second-run (cache/lookup-or-run! selected operation)]
        (is (= 2 @calls))
        (is (= :invalidation (get-in first-run [:receipt :decision])))
        (is (= :noncanonical-entry (get-in first-run [:receipt :reason])))
        (is (= "DEV-TEST-CACHE-CORRUPT"
               (get-in first-run [:receipt :diagnostic-id])))
        (is (false? (get-in first-run [:receipt :stored?])))
        (is (= :invalidation (get-in second-run [:receipt :decision])))
        (is (= corrupt (read-text stored-path)))))))

(deftest canonical-entry-with-a-tampered-result-digest-is-rejected
  (with-cache-directory [directory]
    (let [selected (request directory :digest-corruption/test)
          _ (cache/lookup-or-run! selected #(passed :original))
          stored-path (entry-path directory selected)
          original (read-text stored-path)
          corrupt (str/replace
                   original
                   #":result-sha256 \"sha256:[0-9a-f]{64}\""
                   (str ":result-sha256 \"" (sha \0) "\""))]
      (is (not= original corrupt))
      (write-text! stored-path corrupt)
      (let [result (cache/lookup-or-run! selected #(passed :fresh))]
        (is (= :invalidation (get-in result [:receipt :decision])))
        (is (= :entry-binding-mismatch (get-in result [:receipt :reason])))
        (is (= "DEV-TEST-CACHE-CORRUPT"
               (get-in result [:receipt :diagnostic-id])))
        (is (false? (get-in result [:receipt :stored?])))
        (is (= corrupt (read-text stored-path)))))))

(deftest deeply-nested-corrupt-entry-is-rejected-and-preserved
  (with-cache-directory [directory]
    (let [selected (request directory :deep-corruption/test)
          _ (cache/lookup-or-run! selected #(passed :original))
          stored-path (entry-path directory selected)
          corrupt (str (apply str (repeat 10000 "["))
                       "nil"
                       (apply str (repeat 10000 "]"))
                       "\n")]
      (write-text! stored-path corrupt)
      (let [result (cache/lookup-or-run! selected #(passed :fresh))]
        (is (= :invalidation (get-in result [:receipt :decision])))
        (is (= :invalid-entry-content (get-in result [:receipt :reason])))
        (is (= "DEV-TEST-CACHE-CORRUPT"
               (get-in result [:receipt :diagnostic-id])))
        (is (false? (get-in result [:receipt :stored?])))
        (is (= corrupt (read-text stored-path)))))))

(deftest symlink-entry-is-not-followed-or-overwritten
  (with-cache-directory [directory]
    (with-cache-directory [outside-directory]
      (let [selected (request directory :symlink/test)
            _ (cache/lookup-or-run! selected #(passed :initial))
            stored-path (entry-path directory selected)
            outside (.resolve outside-directory "outside.edn")
            outside-content "{:outside true}\n"]
        (write-text! outside outside-content)
        (Files/delete stored-path)
        (Files/createSymbolicLink stored-path outside no-file-attributes)
        (let [calls (atom 0)
              result (cache/lookup-or-run! selected
                                           #(passed (swap! calls inc)))]
          (is (= 1 @calls))
          (is (= :invalidation (get-in result [:receipt :decision])))
          (is (= :invalid-entry-file (get-in result [:receipt :reason])))
          (is (false? (get-in result [:receipt :stored?])))
          (is (Files/isSymbolicLink stored-path))
          (is (= outside-content (read-text outside))))))))

(deftest symlink-key-lock-is-rejected-before-the-producer-runs
  (with-cache-directory [directory]
    (with-cache-directory [outside-directory]
      (cache/lookup-or-run! (request directory :directory-setup/test)
                            #(passed :setup))
      (let [selected (request directory :symlink-lock/test)
            selected-lock (lock-path directory selected)
            outside (.resolve outside-directory "outside.lock")
            calls (atom 0)]
        (write-text! outside "outside-lock\n")
        (Files/createSymbolicLink selected-lock outside no-file-attributes)
        (let [data (exception-data
                    #(cache/lookup-or-run! selected
                                           (fn []
                                             (swap! calls inc)
                                             (passed :unsafe))))]
          (is (= 0 @calls))
          (is (= "DEV-TEST-CACHE-PATH" (:id data)))
          (is (= :key-lock (:path-label data)))
          (is (Files/isSymbolicLink selected-lock))
          (is (= "outside-lock\n" (read-text outside))))))))

(deftest symlink-cache-root-is-rejected
  (with-cache-directory [parent]
    (with-cache-directory [target]
      (let [root-link (.resolve parent "linked-cache")]
        (Files/createSymbolicLink root-link target no-file-attributes)
        (let [data (exception-data
                    #(cache/lookup-or-run! (request root-link :root-link/test)
                                           (fn [] (passed :unsafe))))]
          (is (= "DEV-TEST-CACHE-PATH" (:id data)))
          (is (= :cache-path-component (:path-label data))))))))

(deftest symlink-cache-ancestor-is-rejected-before-directory-creation
  (with-cache-directory [parent]
    (with-cache-directory [target]
      (let [ancestor-link (.resolve parent "linked-ancestor")
            selected-root (.resolve (.resolve ancestor-link "nested") "cache")]
        (Files/createSymbolicLink ancestor-link target no-file-attributes)
        (let [data (exception-data
                    #(cache/lookup-or-run!
                      (request selected-root :ancestor-link/test)
                      (fn [] (passed :unsafe))))]
          (is (= "DEV-TEST-CACHE-PATH" (:id data)))
          (is (= :cache-path-component (:path-label data)))
          (is (not (Files/exists (.resolve target "nested") no-links))))))))

(deftest regular-file-cache-root-is-rejected-with-a-stable-diagnostic
  (with-cache-directory [parent]
    (let [cache-file (.resolve parent "not-a-directory")]
      (write-text! cache-file "not a cache directory\n")
      (let [data (exception-data
                  #(cache/lookup-or-run! (request cache-file :file-root/test)
                                         (fn [] (passed :unsafe))))]
        (is (= "DEV-TEST-CACHE-PATH" (:id data)))
        (is (= :cache-path-component (:path-label data)))
        (is (= "not a cache directory\n" (read-text cache-file)))))))

(deftest same-key-threads-have-one-producer
  (with-cache-directory [directory]
    (let [selected (request directory :thread-race/test)
          calls (atom 0)
          producer-started (promise)
          release-producer (promise)
          operation #(do
                       (swap! calls inc)
                       (deliver producer-started true)
                       @release-producer
                       (passed :shared))
          first-run (future (cache/lookup-or-run! selected operation))]
      (is (= true (deref producer-started 3000 :timeout)))
      (let [second-run (future (cache/lookup-or-run! selected operation))]
        (Thread/sleep 100)
        (deliver release-producer true)
        (let [first-result (deref first-run 5000 :timeout)
              second-result (deref second-run 5000 :timeout)]
          (is (not= :timeout first-result))
          (is (not= :timeout second-result))
          (is (= 1 @calls))
          (is (= #{:miss :hit}
                 #{(get-in first-result [:receipt :decision])
                   (get-in second-result [:receipt :decision])}))
          (is (= #{true false}
                 #{(get-in first-result [:receipt :producer-executed?])
                   (get-in second-result [:receipt :producer-executed?])})))))))

(deftest different-key-threads-run-concurrently
  (with-cache-directory [directory]
    (let [entered (CountDownLatch. 2)
          release (CountDownLatch. 1)
          active (atom 0)
          maximum-active (atom 0)
          operation (fn [value]
                      (fn []
                        (let [current (swap! active inc)]
                          (swap! maximum-active max current)
                          (.countDown entered)
                          (try
                            (.await release 5 TimeUnit/SECONDS)
                            (passed value)
                            (finally (swap! active dec))))))
          first-run (future
                      (cache/lookup-or-run!
                       (request directory :parallel/one) (operation :one)))
          second-run (future
                       (cache/lookup-or-run!
                        (request directory :parallel/two) (operation :two)))
          both-entered? (.await entered 5 TimeUnit/SECONDS)]
      (.countDown release)
      (is both-entered?)
      (is (= 2 @maximum-active))
      (is (not= :timeout (deref first-run 5000 :timeout)))
      (is (not= :timeout (deref second-run 5000 :timeout))))))

(deftest producer-exception-releases-the-key-lock
  (with-cache-directory [directory]
    (let [selected (request directory :exception/test)]
      (is (thrown? Exception
                   (cache/lookup-or-run!
                    selected #(throw (ex-info "producer failed" {})))))
      (let [fresh (cache/lookup-or-run! selected #(passed :recovered))
            hit (cache/lookup-or-run! selected #(passed :unexpected))]
        (is (true? (get-in fresh [:receipt :stored?])))
        (is (= :hit (get-in hit [:receipt :decision])))
        (is (= :recovered (get-in hit [:result :value])))))))

(defn process-producer!
  "Subprocess fixture for the cross-JVM same-key race test."
  [cache-directory producer-log started-path producer-ready-path gate-path]
  (let [path #(Paths/get ^String % (make-array String 0))
        write-marker! (fn [value]
                        (Files/write (path value)
                                     (.getBytes "1" StandardCharsets/UTF_8)
                                     (into-array java.nio.file.OpenOption
                                                 [StandardOpenOption/CREATE_NEW
                                                  StandardOpenOption/WRITE])))]
    (write-marker! started-path)
    (let [result
          (cache/lookup-or-run!
           (request (path cache-directory) :process-race/test)
           (fn []
             (write-marker! producer-ready-path)
             (when-not (await-path! (path gate-path) 10000)
               (throw (ex-info "process gate timeout" {})))
             (Files/write (path producer-log)
                          (.getBytes "x" StandardCharsets/UTF_8)
                          (into-array java.nio.file.OpenOption
                                      [StandardOpenOption/CREATE
                                       StandardOpenOption/APPEND
                                       StandardOpenOption/WRITE]))
             (Thread/sleep 150)
             (passed :cross-process)))]
      (prn (select-keys (:receipt result)
                        [:decision :stored? :producer-executed?]))
      (shutdown-agents))))

(defn- start-process! [arguments]
  (let [expression
        (str "(require 'gravity.self-hosting.sh01-development-test-cache-test) "
             "(gravity.self-hosting.sh01-development-test-cache-test/"
             "process-producer! "
             (str/join " " (map pr-str arguments)) ")")
        command [(str (System/getProperty "java.home") "/bin/java")
                 "-cp" (System/getProperty "java.class.path")
                 "clojure.main" "-e" expression]]
    (.start (doto (ProcessBuilder. ^java.util.List command)
              (.redirectErrorStream true)))))

(defn- finish-process! [^Process process]
  (let [finished? (.waitFor process 15 TimeUnit/SECONDS)]
    (when-not finished?
      (.destroyForcibly process))
    {:finished? finished?
     :exit-code (when finished? (.exitValue process))
     :output (slurp (.getInputStream process))}))

(deftest same-key-clojure-processes-have-one-persistent-producer
  (with-cache-directory [directory]
    (let [producer-log (.resolve directory "producer.log")
          first-started (.resolve directory "first-started")
          second-started (.resolve directory "second-started")
          producer-ready (.resolve directory "producer-ready")
          impossible-ready (.resolve directory "second-producer-ready")
          gate (.resolve directory "producer-gate")
          common [(str directory) (str producer-log)]
          first-process (start-process!
                         (concat common [(str first-started)
                                         (str producer-ready) (str gate)]))]
      (is (await-path! producer-ready 5000))
      (let [second-process (start-process!
                            (concat common [(str second-started)
                                            (str impossible-ready) (str gate)]))]
        (is (await-path! second-started 5000))
        (Thread/sleep 150)
        (write-text! gate "open")
        (let [first-result (finish-process! first-process)
              second-result (finish-process! second-process)
              receipts (mapv #(edn/read-string (str/trim (:output %)))
                             [first-result second-result])]
          (is (:finished? first-result) (:output first-result))
          (is (:finished? second-result) (:output second-result))
          (is (= 0 (:exit-code first-result)) (:output first-result))
          (is (= 0 (:exit-code second-result)) (:output second-result))
          (is (= "x" (read-text producer-log)))
          (is (not (Files/exists impossible-ready no-links)))
          (is (= #{:miss :hit} (set (map :decision receipts))))
          (is (= #{true false} (set (map :producer-executed? receipts)))))))))

(deftest killed-producer-releases-the-persistent-key-lock
  (with-cache-directory [directory]
    (let [producer-log (.resolve directory "producer.log")
          killed-started (.resolve directory "killed-started")
          killed-ready (.resolve directory "killed-ready")
          killed-gate (.resolve directory "killed-gate")
          recovery-started (.resolve directory "recovery-started")
          recovery-ready (.resolve directory "recovery-ready")
          recovery-gate (.resolve directory "recovery-gate")
          killed-process
          (start-process! [(str directory) (str producer-log)
                           (str killed-started) (str killed-ready)
                           (str killed-gate)])]
      (is (await-path! killed-ready 5000))
      (.destroyForcibly killed-process)
      (is (.waitFor killed-process 5 TimeUnit/SECONDS))
      (is (not (.isAlive killed-process)))
      (write-text! recovery-gate "open")
      (let [recovery-process
            (start-process! [(str directory) (str producer-log)
                             (str recovery-started) (str recovery-ready)
                             (str recovery-gate)])]
        (is (await-path! recovery-ready 5000))
        (let [recovery (finish-process! recovery-process)
              receipt (edn/read-string (str/trim (:output recovery)))
              hit
              (cache/lookup-or-run!
               (request directory :process-race/test)
               #(passed :unexpected))]
          (is (:finished? recovery) (:output recovery))
          (is (= 0 (:exit-code recovery)) (:output recovery))
          (is (= {:decision :miss
                  :stored? true
                  :producer-executed? true}
                 receipt))
          (is (= "x" (read-text producer-log)))
          (is (= :hit (get-in hit [:receipt :decision])))
          (is (false? (get-in hit [:receipt :producer-executed?]))))))))

(deftest historical-v1-file-is-isolated-from-v2-storage
  (with-cache-directory [directory]
    (let [v1-path (.resolve directory "results-v1.edn")
          v1-content "not valid edn and not owned by v2\n"]
      (write-text! v1-path v1-content)
      (let [first-run (cache/lookup-or-run!
                       (request directory :v1-isolation/test)
                       #(passed :v2))
            second-run (cache/lookup-or-run!
                        (request directory :v1-isolation/test)
                        #(passed :unexpected))]
        (is (true? (get-in first-run [:receipt :stored?])))
        (is (= :hit (get-in second-run [:receipt :decision])))
        (is (= v1-content (read-text v1-path)))))))
