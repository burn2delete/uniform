(ns gravity.syntax.selected-revision-request-test
  "Request/file controls only: no owner, launcher, broker or profiler."
  (:require [clojure.test :refer [deftest is]] [clojure.java.io :as io]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-acceptance :as acceptance]
            [gravity.syntax.selected-revision-request :as request]))

(deftest direct-request-is-exact-and-unlaunchable-with-missing-C3-coverage
  (let [directory (.getCanonicalFile (.toFile (java.nio.file.Files/createTempDirectory "C3-request-control-"
                           (make-array java.nio.file.attribute.FileAttribute 0))))
        path #(str (io/file directory %))
        _ (.mkdirs (io/file directory "jdk/bin"))
        _ (.mkdirs (io/file directory "broker"))
        _ (doseq [p ["jdk/bin/java" "clojure.jar" "deps.edn" "broker/policy.edn"]] (spit (path p) "fixture"))
        source {:head "fixture-head" :tree "fixture-tree"}
        env {:Java-options (zipmap request/environment-keys (repeat nil)) :inherited-sha256 "fixture"}
        options {:root (str directory) :source source :directory (path "request")
                 :java-home (path "jdk") :jars [(path "clojure.jar")]
                 :coordination-root (path "broker")}]
    (try
      (with-redefs [h/prepare-inputs! (fn [root source] {:root root :source source})
                    h/recheck-inputs! (fn [_] nil)
                    acceptance/source-closure-paths (fn [_] [(path "deps.edn")])
                    request/runner-binding! (fn [_] {:fixture true})
                    request/git-binding! (fn [root _] {:root root :fixture true})
                    request/environment (constantly env)]
        (let [r (request/prepare! options)
              input (:prepared-input r)]
          (is (= source (:source r)))
          (is (= (str directory) (:cwd r)))
          (is (= [(path "jdk/bin/java") "-Xmx2g" "-cp"] (subvec (:argv r) 0 3)))
          (is (= 7 (count (:argv r))))
          (is (false? (:launchable r)))
          (is (= :never-submitted (:execution r)))
          (is (not (contains? (:missing-interfaces r) :retained-finalizer)))
          (is (= 'gravity.self-hosting.sh01-parallel-test-runner/process-result (get-in r [:existing-ports :wait])))
          (is (= :unestablished (get-in r [:C3-creation-coverage :status])))
          (is (= :unbound (get-in r [:missing-interfaces :whole-watchdog :API])))
          (is (= [:C :L] (get-in r [:guard-proposal :acquire-order])))
          (is (= [:L :C] (get-in r [:guard-proposal :release-order])))
          (is (false? (get-in r [:participation :profiler])))
          (is (false? (get-in r [:participation :warm-session])))
          (is (every? #(not (.exists (io/file %))) (vals (:outputs r))))
          (is (= (:sha256 (:request-file r)) (:sha256 (request/fingerprint (path "request/request.edn")))))
          (is (= (:argv r) (get-in (request/read-prepared! (:path input) (:sha256 input)) [:closure :argv])))
          (is (= :request-directory-already-exists
                 (try (request/prepare! options) nil
                      (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
          (with-redefs [request/environment (constantly (assoc env :inherited-sha256 "changed"))]
            (is (= :launcher-environment-drift
                   (try (request/read-prepared! (:path input) (:sha256 input)) nil
                        (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
          (spit (:path input) "changed")
          (is (= :request-input-drift
                 (try (request/child! (:path input) (:sha256 input)) nil
                      (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
          (is (nil? (find-ns 'gravity.bootstrap)))
          (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker)))
          (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))))
      (finally (doseq [f (reverse (file-seq directory))] (.delete ^java.io.File f))))))

(deftest exact-merged-public-source-signatures-without-loading-runner
  (let [root (.getCanonicalPath (io/file ".")) binding (request/runner-binding! root)]
    (is (= request/runner-source-sha (get-in binding [:source :sha256])))
    (is (= request/runner-signatures (:arglists binding)))
    (is (= :unknown (:global-descendant-coverage binding)))
    (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
    (is (= :public-runner-source-drift
           (with-redefs [h/file-input! (fn [_] {:sha256 "changed"})]
             (try (request/runner-binding! root) nil
                  (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))))

(deftest same-raw-token-through-wait-and-cached-finalization-before-release
  (let [process (Object.) token (Object.) handle (Object.) events (atom [])
        cleanup (atom nil) finalized (atom 0)
        finalize (fn [owned]
                   (is (identical? token owned))
                   (swap! events conj :finalize)
                   (or @cleanup (do (swap! finalized inc)
                                    (reset! cleanup {:tracked-complete? true :complete? nil
                                                     :descendant-coverage :unknown}))))
        ports {:retain (fn [p] (is (identical? process p)) (swap! events conj :retain) token)
               :handles (fn [owned] (is (identical? token owned)) (swap! events conj :handles) [handle])
               :wait (fn [p opts owned]
                       (is (identical? process p)) (is (identical? token owned))
                       (is (= {:timeout-ms 120000} opts)) (swap! events conj :wait)
                       {:root-exited? true :tracked-owned-processes-terminal? true
                        :cleanup (finalize owned) :descendant-coverage :unknown})
               :finalize finalize}
        owned ((:retain ports) process)
        result (request/wait-C3-child! ports process owned {:timeout-ms 120000})]
    (swap! events conj :release-L :release-C)
    (is (identical? token (:retained-token result)))
    (is (identical? handle (first (:raw-handles result))))
    (is (identical? @cleanup (:finalization result)))
    (is (= 1 @finalized))
    (is (= [:retain :wait :finalize :handles :finalize :release-L :release-C] @events))
    (is (false? (:release-authorized? result)))
    (is (= :unknown (:global-descendant-coverage result)))))

(deftest original-wait-error-and-interruption-survive-finalization-failure
  (doseq [original [(ex-info "wait failed" {}) (InterruptedException. "interrupted")]]
    (let [owned (Object.) cleanup (ex-info "cleanup failed" {}) seen (atom nil)]
      (try
        (let [[result interrupted?] (try (request/wait-C3-child!
                           {:wait (fn [_ _ token] (is (identical? owned token)) (throw original))
                            :finalize (fn [token] (reset! seen token) (throw cleanup))}
                           (Object.) owned {})
                         nil (catch Throwable error [error (.isInterrupted (Thread/currentThread))]))]
          (is (identical? original result))
          (is (identical? owned @seen))
          (is (some #(identical? cleanup %) (.getSuppressed ^Throwable result)))
          (when (instance? InterruptedException original)
            (is interrupted?)))
        (finally (Thread/interrupted))))))
