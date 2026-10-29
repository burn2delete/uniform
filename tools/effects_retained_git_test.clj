(ns effects-retained-git-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [effects-acceptance :as a]
            [effects-retained-git :as git]))

(def profile {:cwd (System/getProperty "user.dir")
              :options {:timeout-ms 30000 :capture-wait-ms 5000
                        :output-limit-bytes 2008930 :output-limit-chars 2008930}})
(def clean {:tracked-complete? true :complete? nil :root-alive? false :descendants-alive 0
            :descendant-coverage :unknown})
(def output {:status :passed :exit-code 0 :root-exited? true :tracked-owned-processes-terminal? true
             :stdout-capture-status :complete :stderr-capture-status :complete
             :cleanup clean :stdout "lambda \u03bb, pair \ud83d\ude00\n\n" :stderr ""})
(defn harness []
  (let [calls (atom []) raw (Object.) token {:process raw :terminal (atom nil)}
        ports {:start (fn [argv cwd] (swap! calls conj [:start argv cwd]) raw)
               :retain (fn [p] (swap! calls conj [:retain p]) token)
               :abort-unretained (fn [p] (swap! calls conj [:abort-unretained p]))
               :close-stdin (fn [p] (swap! calls conj [:close-stdin p]))
               :result (fn [p opts owned]
                         (swap! calls conj [:result p opts owned])
                         (reset! (:terminal owned) {:result clean}) output)
               :terminal #(deref (:terminal %))
               :finalize (fn [owned] (swap! calls conj [:finalize owned])
                           (reset! (:terminal owned) {:result clean}) clean)
               :restore-interrupt (fn [error] (swap! calls conj [:restore error]))
               :observe (fn [value] (swap! calls conj [:observe value]))}]
    {:raw raw :token token :calls calls :ports ports}))
(defn read! [h changes]
  (git/read-output profile (merge (:ports h) changes) (:cwd profile) ["show" "HEAD:source"]))
(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))

(def ^:dynamic *fake-exec* (fn [& _] (throw (Exception. "Unbound fake exec"))))
(def legacy-sh-with-fake-exec
  (let [source (slurp (io/resource "clojure/java/shell.clj"))
        _ (assert (= "10d4ef260aa5525f127c512240e7a96610edc09d4e01050d9490d9bfd8f6b8d9" (a/sha256 source)))
        form (get (a/definitions source 'defn) 'sh)
        replaced (atom 0)
        changed (walk/postwalk
                  (fn [node]
                    (if (and (seq? node) (= '.exec (first node)) (= '(Runtime/getRuntime) (second node)))
                      (do (swap! replaced inc)
                          (list 'effects-retained-git-test/*fake-exec* (nth node 2) (nth node 3) (nth node 4)))
                      node)) form)
        changed (apply list (assoc (vec changed) 1 'ACC08-legacy-sh-with-fake-exec))]
    (assert (= 1 @replaced))
    (binding [*ns* (the-ns 'clojure.java.shell)] (eval changed))
    (ns-resolve 'clojure.java.shell 'ACC08-legacy-sh-with-fake-exec)))
(defn fake-legacy-process [exit out err calls]
  (let [stdin (proxy [java.io.ByteArrayOutputStream] []
                (close [] (swap! calls conj [:stdin-close])))
        stdout (java.io.ByteArrayInputStream. (.getBytes ^String out "UTF-8"))
        stderr (java.io.ByteArrayInputStream. (.getBytes ^String err (java.nio.charset.Charset/defaultCharset)))]
    (proxy [Process] []
      (getOutputStream [] stdin)
      (getInputStream [] stdout)
      (getErrorStream [] stderr)
      (waitFor [] (swap! calls conj [:wait]) exit)
      (exitValue [] exit)
      (destroy [] (swap! calls conj [:destroy])))))

(deftest pinned-actual-sh-body-matches-selected-retained-form
  (doseq [exit [0 9]]
    (let [legacy-calls (atom []) h (harness)
          legacy (binding [*fake-exec* (fn [argv env dir]
                                        (swap! legacy-calls conj [:exec (vec argv) env dir])
                                        (fake-legacy-process exit (:stdout output) "diagnostic" legacy-calls))]
                   (apply legacy-sh-with-fake-exec ["git" "-C" (:cwd profile) "show" "HEAD:source"]))
          retained (try
                     (read! h {:result (fn [_ _ owned]
                                        (reset! (:terminal owned) {:result clean})
                                        (assoc output :status (if (zero? exit) :passed :failed)
                                                      :exit-code exit :stderr "diagnostic"))})
                     (catch clojure.lang.ExceptionInfo error (:reason (ex-data error))))]
      (is (= ["git" "-C" (:cwd profile) "show" "HEAD:source"] (second (first @legacy-calls))))
      (is (nil? (nth (first @legacy-calls) 2)))
      (is (nil? (nth (first @legacy-calls) 3)))
      (is (= 1 (count (filter #{[:stdin-close]} @legacy-calls))))
      (is (= 1 (count (filter #{[:wait]} @legacy-calls))))
      (is (= (:stdout output) (:out legacy)))
      (is (= "diagnostic" (:err legacy)))
      (is (= exit (:exit legacy)))
      (is (= (if (zero? exit) (:out legacy) :git-read) retained)))))

(deftest actual-legacy-and-bound-forms-preserve-Git-semantics
  (let [legacy-calls (atom []) h (harness)
        legacy (with-redefs [shell/sh (fn [& args] (swap! legacy-calls conj (vec args))
                                       {:exit 0 :out (:stdout output) :err ""})]
                 (binding [a/*git-reader* nil]
                   (a/git-output (:cwd profile) "show" "HEAD:source")))
        retained (binding [a/*git-reader* #(git/read-output profile (:ports h) %1 %2)]
                   (a/git-output (:cwd profile) "show" "HEAD:source"))
        calls @(:calls h)]
    (is (= legacy retained (:stdout output)))
    (is (= (first @legacy-calls) (second (first calls))))
    (is (= ["git" "-C" (:cwd profile) "show" "HEAD:source"] (second (first calls))))
    (is (= (:cwd profile) (last (first calls))))
    (is (= [:start :retain :close-stdin :result :observe] (mapv first calls)))
    (is (identical? (:raw h) (second (nth calls 1))))
    (is (identical? (:raw h) (second (nth calls 2))))
    (is (identical? (:raw h) (second (nth calls 3))))
    (is (identical? (:token h) (nth (nth calls 3) 3)))
    (is (= (:options profile) (nth (nth calls 3) 2)))
    (is (= :unknown (get-in (last calls) [1 :global-descendant-coverage])))
    (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest nonzero-Git-exit-retains-the-original-refusal
  (let [legacy (with-redefs [shell/sh (fn [& _] {:exit 9 :out "" :err "Git failed"})]
                 (binding [a/*git-reader* nil]
                   (reason #(a/git-output (:cwd profile) "status"))))
        h (harness)
        result (reason #(read! h {:result (fn [_ _ owned]
                                           (reset! (:terminal owned) {:result clean})
                                           (assoc output :status :failed :exit-code 9))}))]
    (is (= :git-read legacy result))
    (is (= 0 (count (filter #(= :finalize (first %)) @(:calls h)))))))

(deftest capture-timeout-and-owned-failures-never-return-source
  (doseq [[change expected]
          [[{:output-truncated? true} :git-capture]
           [{:stdout-capture-status :error} :git-capture]
           [{:stderr-capture-status :capture-timeout} :git-capture]
           [{:stdout-capture-forced-close? true} :git-capture]
           [{:status :timeout :exit-code 124} :git-timeout]
           [{:root-exited? false} :git-read]
           [{:tracked-owned-processes-terminal? false} :git-read]]]
    (let [h (harness)]
      (is (= expected (reason #(read! h {:result (fn [_ _ owned]
                                                  (reset! (:terminal owned) {:result clean})
                                                  (merge output change))}))))))
  (let [h (harness) dirty (assoc clean :discovery-error "enumeration failed")]
    (is (= :git-finalization
           (reason #(read! h {:result (fn [_ _ owned]
                                        (reset! (:terminal owned) {:result dirty})
                                        (assoc output :cleanup dirty))}))))))

(deftest interruption-and-pre-wait-failure-finalize-original-token-once
  (let [h (harness) original (InterruptedException. "original wait")
        caught (try (read! h {:result (fn [& _] (throw original))})
                    (catch InterruptedException error error))]
    (is (identical? original caught))
    (is (= 1 (count (filter #(= :restore (first %)) @(:calls h)))))
    (is (= [[:finalize (:token h)]] (filterv #(= :finalize (first %)) @(:calls h)))))
  (let [h (harness) original (Exception. "stdin close")
        caught (try (read! h {:close-stdin (fn [_] (throw original))})
                    (catch Exception error error))]
    (is (identical? original caught))
    (is (not-any? #(= :result (first %)) @(:calls h)))
    (is (= 1 (count (filter #(= :finalize (first %)) @(:calls h))))))
  (let [h (harness) original (InterruptedException. "wait") cleanup-error (Exception. "cleanup")
        caught (try (read! h {:result (fn [& _] (throw original))
                              :finalize (fn [_] (throw cleanup-error))})
                    (catch InterruptedException error error))]
    (is (identical? original caught))
    (is (= [cleanup-error] (vec (.getSuppressed caught))))
    (is (= :observe (first (last @(:calls h)))))))

(deftest failed-retention-or-terminal-error-is-not-retried
  (let [h (harness) failure (Exception. "retain")]
    (is (identical? failure (try (read! h {:retain (fn [_] (throw failure))})
                                (catch Exception error error))))
    (is (not-any? #(#{:result :finalize} (first %)) @(:calls h)))
    (is (false? (get-in (last @(:calls h)) [1 :owned-token-retained?]))))
  (let [h (harness) failure (Exception. "cached finalizer failure")]
    (is (= :git-finalization
           (reason #(read! h {:result (fn [_ _ owned]
                                        (reset! (:terminal owned) {:error failure}) output)
                               :observe (fn [_] nil)}))))
    (is (not-any? #(= :finalize (first %)) @(:calls h)))))

(deftest unsupported-override-and-argv-refuse-before-start
  (doseq [run [(fn [h] (binding [shell/*sh-env* {"PATH" "/other"}] (read! h {})))
               (fn [h] (binding [shell/*sh-dir* "/other"] (read! h {})))
               (fn [h] (git/read-output profile (:ports h) "/other" ["status"]))
               (fn [h] (git/read-output profile (:ports h) (:cwd profile) ["commit"]))]]
    (let [h (harness)]
      (is (= :git-profile (reason #(run h))))
      (is (empty? @(:calls h))))))

(deftest Git-waits-share-the-operation-clock-and-recompute-after-start
  (let [time (atom 0) h (harness)
        deadline {:now #(deref time) :expires-ms 25000 :reserve-ms 15000}]
    (binding [a/*operation-deadline* deadline]
      (is (= 10000 (a/remaining-wait-ms 30000 11800)))
      (is (= (:stdout output)
             (read! h {:start (fn [& _] (reset! time 4000) (:raw h))})))
      (is (= 6000 (get-in (first (filter #(= :result (first %)) @(:calls h))) [2 :timeout-ms])))
      (reset! time 9000)
      (let [next (harness)]
        (read! next {})
        (is (= 1000 (get-in (first (filter #(= :result (first %)) @(:calls next))) [2 :timeout-ms])))))))

(deftest expired-operation-refuses-before-start-or-after-return-without-renewal
  (let [time (atom 10000) h (harness)
        deadline {:now #(deref time) :expires-ms 25000 :reserve-ms 15000}]
    (binding [a/*operation-deadline* deadline]
      (is (= :operation-deadline (reason #(read! h {}))))
      (is (not-any? #(= :start (first %)) @(:calls h)))
      (reset! time 0)
      (let [late (harness)]
        (is (= :operation-deadline
               (reason #(read! late {:start (fn [& _] (reset! time 10001) (:raw late))}))))
        (is (= 1 (count (filter #(= :finalize (first %)) @(:calls late))))))
      (reset! time 0)
      (let [late (harness)]
        (is (= :operation-deadline
               (reason #(read! late {:result (fn [& _] (reset! time 25000)
                                              (reset! (:terminal (:token late)) {:result clean}) output)}))))
        (is (not-any? #(= :finalize (first %)) @(:calls late)))))))

(deftest retention-failure-aborts-the-same-raw-once-without-retrying
  (doseq [mode [:throw :nil :wrong]]
    (let [h (harness) original (Exception. "retain")
          error (try (read! h {:retain (fn [_] (case mode :nil nil :wrong {:process (Object.)} (throw original)))})
                     nil (catch Throwable error error))]
      (is (if (= :throw mode) (identical? original error) (= :git-ownership (:reason (ex-data error)))))
      (is (= [[:abort-unretained (:raw h)]]
             (filter #(= :abort-unretained (first %)) @(:calls h))))
      (is (not-any? #(#{:result :finalize} (first %)) @(:calls h)))))
  (let [h (harness) original (Exception. "retain") secondary (Exception. "abort")
        error (try (read! h {:retain (fn [_] (throw original))
                             :abort-unretained (fn [_] (throw secondary))})
                   nil (catch Throwable error error))]
    (is (identical? original error))
    (is (= [secondary] (vec (.getSuppressed original))))))

(let [r (run-tests 'effects-retained-git-test)]
  (prn {:real-process-ports-called 0 :scope :selected-PMK-Git-binding})
  (shutdown-agents)
  (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))
