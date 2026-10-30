(ns gravity.syntax.selected-revision-git
  "Only selected C3 harness bookends. Loading this namespace starts nothing."
  (:require [clojure.java.io :as io] [clojure.string :as str]
            [gravity.resolve.selected-source-harness :as h])
  (:import (java.io StringWriter) (java.nio.charset Charset)))

(def commands
  #{["rev-parse" "HEAD"] ["rev-parse" "HEAD^{tree}"]
    ["status" "--porcelain" "--untracked-files=all"]
    ["ls-files" "bootstrap/clojure/src" "bootstrap/gravity" "bootstrap/clojure/test" "deps.edn"]})

(defn- stream-text [stream encoding]
  (with-open [out (StringWriter.)]
    (io/copy stream out :encoding encoding)
    (.toString out)))

(defn- remaining-ms [ports]
  (when-let [remaining (:remaining-ms ports)]
    (let [value (remaining)]
      (h/ensure! (and (integer? value) (<= 1 value Long/MAX_VALUE)) :selected-Git-whole-deadline)
      (long value))))

(defn- wait-exit [ports ^Process process]
  (if-let [remaining (remaining-ms ports)]
    (if (.waitFor process remaining java.util.concurrent.TimeUnit/MILLISECONDS)
      (.exitValue process)
      (throw (ex-info "Selected Git exceeded the shared deadline" {:reason :selected-Git-whole-deadline})))
    (.waitFor process)))

(defn- captured [ports task]
  (if-let [remaining (remaining-ms ports)]
    (let [value (deref task remaining ::timeout)]
      (h/ensure! (not= ::timeout value) :selected-Git-whole-deadline)
      value)
    @task))

(defn- interrupted-error? [error]
  (loop [e error]
    (cond (nil? e) false (instance? InterruptedException e) true
          :else (recur (.getCause ^Throwable e)))))

(defn- fatal-cause [error]
  (loop [e error]
    (cond (nil? e) nil
          (or (instance? ThreadDeath e) (instance? VirtualMachineError e) (instance? LinkageError e)) e
          :else (recur (.getCause ^Throwable e)))))

(defn- terminal-error! [record primary interrupted field error]
  (swap! record assoc field error :terminal :unproven)
  (when (or (interrupted-error? error) (.isInterrupted (Thread/currentThread))) (reset! interrupted true))
  (let [original @primary fatal (fatal-cause error)]
    (cond
      (and fatal (not (fatal-cause original)))
      (do (when (and original (not (identical? original fatal)))
            (.addSuppressed ^Throwable fatal original))
          (reset! primary fatal) (throw fatal))
      original (when-not (identical? original error) (.addSuppressed ^Throwable original error))
      (or (nil? (:exit @record)) (zero? (:exit @record)))
      (do (reset! primary error) (throw error))
      ;; A nonzero Git exit retains the original h/git! refusal when the
      ;; secondary cleanup/identity error is nonfatal. Interruption is separate.
      :else nil)))

(defn- shell-result!
  "Preserve shell/sh's inherited environment, closed stdin, complete streams,
  UTF8 stdout/default-charset stderr and wait/exit semantics. Normal bounded,
  strict-UTF8 capture is unsuitable for these Git protocol strings."
  [ports binding records argv]
  ((:verify-git! ports) binding)
  (remaining-ms ports)
  (let [process ((:launch ports) argv (:cwd binding))
        record (atom {:argv argv :cwd (:cwd binding) :process process
                      :root-wait-exited? :unknown :descendant-coverage :unknown})]
    ;; Keep the original Process even if retaining its ownership token refuses.
    (swap! records conj record)
    (let [owned (try ((:retain ports) process)
                     (catch Throwable error
                       (swap! record assoc :retain-error error :terminal :unproven)
                       (when (interrupted-error? error) (.interrupt (Thread/currentThread)))
                       (throw error)))
          primary (atom nil) interrupted (atom (.isInterrupted (Thread/currentThread)))]
      (swap! record assoc :retained-token owned)
      (try
        (swap! record assoc :raw-handles ((:handles ports) owned))
        (.close (.getOutputStream ^Process process))
        (with-open [stdout (.getInputStream ^Process process)
                    stderr (.getErrorStream ^Process process)]
          (let [out (future (stream-text stdout "UTF-8"))
                err (future (stream-text stderr (.name (Charset/defaultCharset))))
                _ (swap! record assoc :stdout-task out :stderr-task err)
                exit (wait-exit ports process)
                _ (swap! record assoc :exit exit :root-wait-exited? true)
                result {:exit exit :out (captured ports out) :err (captured ports err)}
                _ (swap! record assoc :capture-complete? true)]
            result))
        (catch Throwable error
          (reset! primary error)
          (when (or (interrupted-error? error) (.isInterrupted (Thread/currentThread))) (reset! interrupted true))
          (swap! record assoc :error error)
          (throw error))
        (finally
          (try
            (swap! record assoc :finalization ((:finalize ports) owned))
            (swap! record assoc :raw-handles ((:handles ports) owned))
            (catch Throwable error
              (terminal-error! record primary interrupted :finalization-error error))
            (finally
              (try
                ((:verify-git! ports) binding)
                (catch Throwable error
                  (terminal-error! record primary interrupted :identity-error error))
                (finally
                  (when @interrupted
                    (.interrupt (Thread/currentThread))))))))))))

(defn git! [ports binding records root & args]
  (h/ensure! (and (= root (:root binding)) (contains? commands (vec args)))
             :selected-C3-Git-command)
  (let [result (shell-result! ports binding records
                             (into [(get binding :argv-executable "git") "-C" root] args))]
    (h/ensure! (zero? (:exit result)) :git-identity-read)
    (str/trim (:out result))))

(defn observation-view [records]
  ;; Raw Process/token/handles remain in the child's records, never rebuilt from
  ;; these serializable observations. Unknown coverage is never promoted.
  (mapv (fn [record]
          (let [r @record]
            (cond-> (select-keys r [:argv :cwd :exit :root-wait-exited? :capture-complete? :finalization :descendant-coverage :terminal])
              (:error r) (assoc :error-class (.getName (class (:error r))))
              (:retain-error r) (assoc :retain-error-class (.getName (class (:retain-error r))))
              (:identity-error r) (assoc :identity-error-class (.getName (class (:identity-error r))))
              (:finalization-error r) (assoc :finalization-error-class (.getName (class (:finalization-error r)))))))
        @records))
