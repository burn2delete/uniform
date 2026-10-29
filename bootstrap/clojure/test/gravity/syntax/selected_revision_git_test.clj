(ns gravity.syntax.selected-revision-git-test
  "Actual shell form with only exec replaced; no target process is started."
  (:require [clojure.test :refer [deftest is]] [clojure.java.io :as io]
            [clojure.java.shell :as shell] [clojure.walk :as walk]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-git :as git]
            [gravity.syntax.selected-revision-request :as request])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)))

(defn- fake-process [out err exit events wait-error]
  (let [stdin (proxy [ByteArrayOutputStream] []
                (close [] (swap! events conj :stdin-closed)))]
    (proxy [Process] []
      (getOutputStream [] stdin)
      (getInputStream [] (ByteArrayInputStream. out))
      (getErrorStream [] (ByteArrayInputStream. err))
      (waitFor [] (swap! events conj :wait)
        (if wait-error (throw wait-error) exit))
      (exitValue [] exit) (isAlive [] false) (destroy [] nil))))

(defn- legacy-sh [launch]
  (let [form (with-open [r (java.io.PushbackReader. (io/reader (io/resource "clojure/java/shell.clj")))]
               (loop [] (let [form (read {:eof ::eof} r)]
                          (cond (= ::eof form) (throw (ex-info "Missing actual shell form" {}))
                                (and (seq? form) (= 'defn (first form)) (= 'sh (second form))) form
                                :else (recur)))))
        replaced (atom 0)
        body (walk/postwalk (fn [x]
                              (if (and (seq? x) (= '.exec (first x)))
                                (do (swap! replaced inc) (cons 'launch-only (drop 2 x))) x))
                            (drop-while #(or (string? %) (map? %)) (nnext form)))
        name (gensym "shell_actual_form_") n (create-ns name)]
    (h/ensure! (= 1 @replaced) :actual-shell-fake-exec-port)
    (is (= 1 @replaced))
    (binding [*ns* n]
      (clojure.core/refer 'clojure.core)
      (doseq [s '[parse-args as-env-strings stream-to-enc stream-to-string]]
        (intern n s (var-get (ns-resolve 'clojure.java.shell s))))
      (intern n 'copy io/copy) (intern n 'as-file io/as-file)
      (intern n 'launch-only launch)
      (let [f (eval (cons 'fn body))]
        (fn [& args] (try (apply f args) (finally (remove-ns name))))))))

(defn- controls [process events owned finalize-error]
  {:verify-git! (fn [_] (swap! events conj :verify))
   :launch (fn [argv cwd] (swap! events conj [:launch argv cwd]) process)
   :retain (fn [p] (is (identical? process p)) (swap! events conj :retain) owned)
   :handles (fn [token] (is (identical? owned token)) [process])
   :finalize (fn [token] (is (identical? owned token)) (swap! events conj :finalize)
               (if finalize-error (throw finalize-error)
                   {:tracked-complete? true :complete? nil :descendant-coverage :unknown}))})

(deftest actual-shell-form-compares-complete-output-decoding-exit-and-stdin
  (let [root (.getCanonicalPath (io/file ".")) argv ["git" "-C" root "rev-parse" "HEAD"]]
    (doseq [[out err exit] [[(.getBytes "  identity\n" "UTF-8") (.getBytes "warning" "UTF-8") 0]
                          [(byte-array [(byte 97) (unchecked-byte 0xc3) (byte 40)]) (byte-array 0) 9]
                          [(.getBytes (apply str (repeat 1048577 "x")) "UTF-8") (byte-array 0) 0]]]
      (let [old-events (atom []) new-events (atom []) records (atom []) owned (Object.)
            old (legacy-sh (fn [command env cwd]
                             (is (= argv (vec command))) (is (nil? env)) (is (nil? cwd))
                             (fake-process out err exit old-events nil)))
            process (fake-process out err exit new-events nil)
            result (#'git/shell-result! (controls process new-events owned nil)
                                      {:root root :cwd root} records argv)]
        (is (= (apply old argv) result))
        (is (= [:stdin-closed :wait] @old-events))
        (is (= [:verify [:launch argv root] :retain :stdin-closed :wait :finalize :verify] @new-events))
        (is (identical? process (:process @(first @records))))
        (is (identical? owned (:retained-token @(first @records))))
        (is (= :unknown (get-in (git/observation-view records) [0 :descendant-coverage])))
        (is (nil? (get-in (git/observation-view records) [0 :finalization :complete?])))))))

(deftest original-harness-command-order-and-nonzero-refusal-are-preserved
  (let [root (.getCanonicalPath (io/file ".")) calls (atom []) records (atom [])
        old h/git!]
    (doseq [args (sort git/commands)]
      (let [events (atom []) p (fake-process (.getBytes "  identity\n" "UTF-8") (byte-array 0) 0 events nil)
            ports (controls p events (Object.) nil)
            expected (with-redefs [shell/sh (fn [& command] (swap! calls conj (vec command))
                                              {:exit 0 :out "  identity\n" :err ""})]
                       (apply old root args))]
        (is (= expected (apply git/git! ports {:root root :cwd root} records root args)))
        (is (= (last @calls) (second (second @events))))))
    (let [events (atom []) cleanup (ex-info "cleanup failure" {})
          p (fake-process (byte-array 0) (.getBytes "Git refusal" "UTF-8") 7 events nil)]
      (is (= :git-identity-read
             (try (git/git! (controls p events (Object.) cleanup) {:root root :cwd root} records root "rev-parse" "HEAD")
                  nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
      (is (identical? cleanup (:finalization-error @(last @records)))))))

(deftest command-and-identity-refusals-cannot-launch
  (let [calls (atom 0) records (atom []) ports {:launch (fn [& _] (swap! calls inc))}
        binding {:root "/fixture" :cwd "/fixture"}]
    (doseq [[root args] [["/other" ["rev-parse" "HEAD"]] ["/fixture" ["config" "--list"]]]]
      (is (= :selected-C3-Git-command
             (try (apply git/git! ports binding records root args) nil
                  (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
    (is (= :identity-changed
           (try (git/git! (assoc ports :verify-git! (fn [_] (throw (ex-info "drift" {:reason :identity-changed}))))
                          binding records "/fixture" "rev-parse" "HEAD")
                nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (zero? @calls)) (is (empty? @records))))

(deftest original-launch-error-and-post-command-identity-failure-are-preserved
  (let [original (java.io.IOException. "launch failed") records (atom [])
        old (legacy-sh (fn [& _] (throw original)))
        ports {:verify-git! (fn [_] nil) :launch (fn [& _] (throw original))}]
    (is (identical? original (try (old "git") nil (catch Throwable error error))))
    (is (identical? original (try (#'git/shell-result! ports {:cwd "/fixture"} records ["git"])
                                 nil (catch Throwable error error))))
    (is (empty? @records)))
  (doseq [exit [0 7]]
    (let [events (atom []) records (atom []) verification (atom 0)
          drift (ex-info "configuration changed during command" {:reason :identity-after-drift})
          p (fake-process (.getBytes "head" "UTF-8") (byte-array 0) exit events nil)
          ports (assoc (controls p events (Object.) nil)
                       :verify-git! (fn [_] (when (= 2 (swap! verification inc)) (throw drift))))]
      (is (= (if (zero? exit) :identity-after-drift :git-identity-read)
             (try (git/git! ports {:root "/fixture" :cwd "/fixture"} records "/fixture" "rev-parse" "HEAD")
                  nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
      (is (identical? drift (:identity-error @(first @records))))
      (is (true? (:root-wait-exited? @(first @records)))))))

(deftest raw-process-and-original-errors-survive-retain-wait-and-cleanup-failures
  (doseq [phase [:retain :wait]]
    (let [original (InterruptedException. "original") cleanup (ex-info "cleanup" {})
          events (atom []) records (atom []) owned (Object.)
          p (fake-process (byte-array 0) (byte-array 0) 0 events (when (= phase :wait) original))
          ports (cond-> (controls p events owned cleanup)
                  (= phase :retain) (assoc :retain (fn [_] (throw original))))]
      (try
        (let [[error interrupted?] (try (#'git/shell-result! ports {:cwd "/fixture"} records ["git"])
                                       nil (catch Throwable e [e (.isInterrupted (Thread/currentThread))]))]
          (is (identical? original error))
          (is (identical? p (:process @(first @records))))
          (if (= phase :wait)
            (do (is interrupted?)
                (is (some #(identical? cleanup %) (.getSuppressed ^Throwable error))))
            (do (is interrupted?) (is (not-any? #{:finalize} @events)))))
        (finally (Thread/interrupted))))))

(deftest cleanup-and-identity-interruptions-survive-nonzero-and-mixed-errors
  (doseq [phase [:cleanup-nonzero :cleanup-mixed :identity-nonzero]]
    (let [events (atom []) records (atom []) verification (atom 0)
          wait-error (when (= phase :cleanup-mixed) (java.io.IOException. "original wait error"))
          interruption (InterruptedException. "secondary interruption")
          p (fake-process (byte-array 0) (byte-array 0) 7 events wait-error)
          ports (cond-> (controls p events (Object.) (when (not= phase :identity-nonzero) interruption))
                  (= phase :identity-nonzero)
                  (assoc :verify-git! (fn [_] (when (= 2 (swap! verification inc)) (throw interruption)))))]
      (try
        (let [[error interrupted?] (try (git/git! ports {:root "/fixture" :cwd "/fixture"}
                                                 records "/fixture" "rev-parse" "HEAD")
                                       nil (catch Throwable e [e (.isInterrupted (Thread/currentThread))]))]
          (is interrupted?)
          (if wait-error
            (do (is (identical? wait-error error))
                (is (some #(identical? interruption %) (.getSuppressed ^Throwable error))))
            (is (= :git-identity-read (:reason (ex-data error)))))
          (is (identical? interruption (get @(first @records)
                                          (if (= phase :identity-nonzero) :identity-error :finalization-error)))))
        (finally (Thread/interrupted))))))

(deftest fatal-cleanup-never-becomes-an-ordinary-Git-refusal
  (doseq [make-fatal [#(LinkageError. "constructed linkage error")
                     #(OutOfMemoryError. "constructed VM error") #(ThreadDeath.)]
          wait-failed? [false true]]
    (let [events (atom []) records (atom []) fatal (make-fatal)
          original (when wait-failed? (java.io.IOException. "original wait error"))
          p (fake-process (byte-array 0) (byte-array 0) 7 events original)
          error (try (git/git! (controls p events (Object.) fatal) {:root "/fixture" :cwd "/fixture"}
                                 records "/fixture" "rev-parse" "HEAD")
                       nil (catch Throwable e e))]
        (is (identical? fatal error))
        (is (identical? fatal (:finalization-error @(first @records))))
        (when original (is (some #(identical? original %) (.getSuppressed ^Throwable fatal)))))))

(deftest selected-observation-write-keeps-original-error-and-interrupted-state
  (doseq [writer-error [(java.io.IOException. "observation write failed")
                       (LinkageError. "constructed writer linkage error")
                       (OutOfMemoryError. "constructed writer VM error")]]
    (let [original (ex-info "original Git refusal" {:reason :git-identity-read}) old h/git!
          fatal? (not (instance? java.io.IOException writer-error))]
      (try
        (with-redefs-fn {#'request/read-input! (fn [_ _] {:root "/fixture" :outputs {:git-observations "/fixture/out"}})
                        #'request/runner-ports! (fn [_] {})
                        #'request/read-prepared! (fn [_ _] (.interrupt (Thread/currentThread)) (throw original))
                        #'request/create-edn! (fn [_ _] (Thread/interrupted) (throw writer-error))}
          (fn []
            (let [[error interrupted?] (try (request/child! "fixture" "hash") nil
                                            (catch Throwable e [e (.isInterrupted (Thread/currentThread))]))]
              (is (identical? (if fatal? writer-error original) error))
              (is interrupted?)
              (is (some #(identical? (if fatal? original writer-error) %) (.getSuppressed ^Throwable error))))))
        (is (identical? old h/git!))
        (finally (Thread/interrupted))))))

(deftest selected-child-routes-initial-bookend-and-restores-shared-Var-on-failure
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory "selected-Git-scope-"
                           (make-array java.nio.file.attribute.FileAttribute 0)))
        path (str (io/file directory "observations.edn")) root "/fixture"
        events (atom []) p (fake-process (.getBytes "head\n" "UTF-8") (byte-array 0) 0 events nil)
        original (ex-info "stop before owner" {}) old h/git!]
    (try
      (with-redefs-fn {#'request/read-input! (fn [_ _] {:root root :git-binding {:root root :cwd root}
                                                     :outputs {:git-observations path}})
                      #'request/runner-ports! (fn [_] (controls p events (Object.) nil))
                      #'request/verify-git-binding! (fn [_] nil)
                      #'request/read-prepared! (fn [_ _] (is (= "head" (h/git! root "rev-parse" "HEAD")))
                                                (throw original))}
        #(is (identical? original (try (request/child! "fixture" "hash") nil (catch Throwable e e)))))
      (is (identical? old h/git!))
      (is (.isFile (io/file path)))
      (is (not (.contains (slurp path) "#object")))
      (is (nil? (find-ns 'gravity.bootstrap)))
      (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
      (finally (doseq [f (reverse (file-seq directory))] (.delete ^java.io.File f))))))

(deftest explicit-absolute-executable-binding-avoids-path-selection
  (let [directory (.getCanonicalFile (.toFile (java.nio.file.Files/createTempDirectory
                     "selected-Git-direct-" (make-array java.nio.file.attribute.FileAttribute 0))))
        path #(str (io/file directory %)) root (.getCanonicalPath (io/file "."))
        executable (io/file directory "git")
        _ (spit executable "inert binary fixture") _ (.setExecutable executable true)
        _ (spit (path "config") "[core]\n")
        identity {:invocation :absolute-executable :executable (path "git")
                  :dispatch-files [] :selection-links [] :configuration-files [(path "config")]}
        env {:fixture :inherited}]
    (try
      (with-redefs-fn {#'request/git-path (fn [] (throw (ex-info "PATH must not be selected" {})))
                      #'request/environment (constantly env)}
        (fn []
          (let [binding (request/git-binding! root identity)]
            (is (= (path "git") (:argv-executable binding)))
            (is (nil? (:path-selected-entry binding)))
            (is (= :inherit-unchanged (:environment-policy binding)))
            (is (nil? (request/verify-git-binding! binding)))
            (let [launches (atom 0)
                  ports {:verify-git! request/verify-git-binding!
                         :launch (fn [& _] (swap! launches inc))}]
              (is (= :selected-Git-identity-drift
                     (try (git/git! ports (assoc binding :argv-executable "git")
                                    (atom []) root "rev-parse" "HEAD") nil
                          (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
              (is (zero? @launches)))
            (doseq [command git/commands]
              (let [events (atom []) records (atom [])
                    process (fake-process (.getBytes " direct-result\n" "UTF-8") (byte-array 0) 0 events nil)
                    ports (controls process events (Object.) nil)]
                (is (= "direct-result" (apply git/git! ports binding records root command)))
                (is (some #{[:launch (into [(path "git") "-C" root] command) root]} @events))))
            (spit (path "config") "[core]\nchanged=true\n")
            (is (= :selected-Git-identity-drift
                   (try (request/verify-git-binding! binding) nil
                        (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
            (spit (path "config") "[core]\n")
            (with-redefs [request/environment (constantly {:fixture :changed})]
              (is (= :selected-Git-identity-drift
                     (try (request/verify-git-binding! binding) nil
                          (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
            (doseq [bad [(assoc identity :invocation :unknown)
                         (assoc identity :executable "git")
                         (assoc identity :executable (path "missing"))
                         (assoc identity :executable (path "./git"))]]
              (is (= :selected-Git-executable
                     (try (request/git-binding! root bad) nil
                          (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
            (spit executable "changed inert binary")
            (is (= :selected-Git-identity-drift
                   (try (request/verify-git-binding! binding) nil
                        (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
            (.setExecutable executable false)
            (is (= :selected-Git-executable
                   (try (request/git-binding! root identity) nil
                        (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))))
      (finally (doseq [f (reverse (file-seq directory))] (.delete ^java.io.File f))))))

(deftest executable-configuration-and-absence-drift-refuse-without-target-execution
  (let [directory (.getCanonicalFile (.toFile (java.nio.file.Files/createTempDirectory "selected-Git-identity-"
                           (make-array java.nio.file.attribute.FileAttribute 0))))
        path #(str (io/file directory %)) root (.getCanonicalPath (io/file "."))
        _ (spit (path "git") "binary fixture") _ (spit (path "config") "[core]\n")
        identity {:executable (path "git") :dispatch-files [] :selection-links []
                  :configuration-files [(path "config") (path "absent")]}
        env {:fixture :inherited}]
    (try
      (with-redefs-fn {#'request/git-path (constantly (path "git"))
                      #'request/environment (constantly env)}
        (fn []
          (let [binding (request/git-binding! root identity)
                reason #(try (request/verify-git-binding! binding) nil
                             (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))]
            (is (nil? (reason)))
            (spit (path "config") "[core]\nchanged=true\n")
            (is (= :selected-Git-identity-drift (reason)))
            (spit (path "config") "[core]\n")
            (spit (path "absent") "new config")
            (is (= :selected-Git-identity-drift (reason)))
            (.delete (io/file (path "absent")))
            (with-redefs [shell/*sh-env* {"changed" "environment"}]
              (is (= :selected-Git-identity-drift (reason))))
            (spit (path "git") "different executable")
            (is (= :selected-Git-identity-drift (reason))))))
      (finally (doseq [f (reverse (file-seq directory))] (.delete ^java.io.File f))))))
