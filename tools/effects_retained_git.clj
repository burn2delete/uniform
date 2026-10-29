(ns effects-retained-git
  "Selected PMK Git bookends through supplied canonical SH-01 ports."
  (:require [clojure.java.shell :as shell]
            [effects-acceptance :as a]))

(defn- require! [condition reason]
  (when-not condition
    (throw (ex-info "Effects acceptance binding rejected" {:reason reason}))))
(defn- interrupted? [error]
  (loop [cause error]
    (cond (nil? cause) false
          (instance? InterruptedException cause) true
          :else (recur (.getCause ^Throwable cause)))))

(defn read-output
  "Keep Git argv/config/environment semantics; return only complete UTF-8 output.
  The selected profile has no sh :in/:dir/:env overrides. Its explicit cwd
  equals the original JVM working directory. No real ports are resolved here."
  [profile {:keys [start retain abort-unretained close-stdin result terminal finalize restore-interrupt observe]} root arguments]
  (require! (every? fn? [start retain abort-unretained close-stdin result terminal finalize restore-interrupt observe]) :git-ports)
  (require! (and (nil? shell/*sh-dir*) (nil? shell/*sh-env*)
                (= "UTF-8" (.name (java.nio.charset.Charset/defaultCharset)))
                (= (:cwd profile) (str root))
                (vector? arguments) (every? string? arguments)
                (#{"rev-parse" "status" "show"} (first arguments))) :git-profile)
  (let [options (:options profile)]
    (require! (every? #(and (integer? %) (pos? %))
                     ((juxt :timeout-ms :capture-wait-ms :output-limit-bytes :output-limit-chars) options)) :git-profile)
    (let [argv (into ["git" "-C" (str root)] arguments)
          raw (atom nil) owned (atom nil) primary (atom nil) cleanup (atom nil)
          finalized? (atom false) aborted? (atom false) observed-result (atom nil)
          finish! (fn []
                    (when (and @raw (nil? @owned))
                      (reset! aborted? true)
                      (abort-unretained @raw))
                    (when @owned
                      (let [state (terminal @owned)]
                        (cond
                          (:error state) (throw (:error state))
                          (:result state) (reset! cleanup (:result state))
                          (nil? state) (when-not @finalized?
                                         (reset! finalized? true)
                                         (reset! cleanup (finalize @owned)))
                          :else (require! false :git-finalization)))))
          secondary! (fn [error]
                       (when (interrupted? error) (restore-interrupt error))
                       (if-let [original @primary]
                         (when-not (identical? original error)
                           (.addSuppressed ^Throwable original error))
                         (do (reset! primary error) (throw error))))]
      (try
        (a/remaining-wait-ms (:timeout-ms options) (:accounted-reclamation-base-ms a/process-bounds))
        (reset! raw (start argv (:cwd profile)))
        (let [token (retain @raw)]
          (require! (and (some? token) (identical? @raw (:process token))) :git-ownership)
          (reset! owned token))
        (close-stdin @raw)
        (let [r (result @raw (assoc options :timeout-ms
                                   (a/remaining-wait-ms (:timeout-ms options)
                                     (:accounted-reclamation-base-ms a/process-bounds))) @owned)
              state (terminal @owned)
              c (:result state)]
          (reset! observed-result r)
          (when (:wait-interrupted? r)
            (throw (if (instance? Throwable (:wait-error r))
                     (:wait-error r) (InterruptedException. "Git wait interrupted"))))
          (require! (not= :timeout (:status r)) :git-timeout)
          (require! (and (map? c) (identical? c (:cleanup r))
                        (true? (:tracked-complete? c)) (not (:root-alive? c))
                        (= 0 (:descendants-alive c)) (not (:discovery-error c)) (not (:cleanup-error c))
                        (= :unknown (:descendant-coverage c))) :git-finalization)
          (require! (and (= :complete (:stdout-capture-status r)) (= :complete (:stderr-capture-status r))
                         (not (:output-truncated? r)) (not (:capture-failed? r))
                         (not (:stdout-capture-forced-close? r)) (not (:stderr-capture-forced-close? r))) :git-capture)
          (require! (and (= :passed (:status r)) (= 0 (:exit-code r))
                         (:root-exited? r) (:tracked-owned-processes-terminal? r)
                         (not (:cleanup-failed? r)) (not (:timeout-containment-unproven? r))) :git-read)
          (require! (string? (:stdout r)) :git-capture)
          (a/remaining-wait-ms 1 0)
          (:stdout r))
        (catch Throwable error
          (reset! primary error)
          (when (interrupted? error) (restore-interrupt error))
          (throw error))
        (finally
          (try
            (try (finish!) (catch Throwable error (secondary! error)))
            (finally
              (try
                (observe {:argv argv :cwd (:cwd profile) :raw-process-returned? (some? @raw)
                          :owned-token-retained? (some? @owned) :result @observed-result
                          :cleanup @cleanup :direct-finalizer-attempted? @finalized?
                          :unretained-abort-attempted? @aborted?
                          :global-descendant-coverage :unknown :failed? (some? @primary)})
                (catch Throwable error (secondary! error))))))))))
