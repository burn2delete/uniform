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

(defn- shell-result!
  "Preserve shell/sh's inherited environment, closed stdin, complete streams,
  UTF8 stdout/default-charset stderr and wait/exit semantics. Normal bounded,
  strict-UTF8 capture is unsuitable for these Git protocol strings."
  [ports binding records argv]
  ((:verify-git! ports) binding)
  (let [process ((:launch ports) argv (:cwd binding))
        record (atom {:argv argv :cwd (:cwd binding) :process process
                      :root-wait-exited? :unknown :descendant-coverage :unknown})]
    ;; Keep the original Process even if retaining its ownership token refuses.
    (swap! records conj record)
    (let [owned (try ((:retain ports) process)
                     (catch Throwable error
                       (swap! record assoc :retain-error error :terminal :unproven)
                       (when (instance? InterruptedException error) (.interrupt (Thread/currentThread)))
                       (throw error)))
          primary (atom nil)]
      (swap! record assoc :retained-token owned)
      (try
        (swap! record assoc :raw-handles ((:handles ports) owned))
        (.close (.getOutputStream ^Process process))
        (with-open [stdout (.getInputStream ^Process process)
                    stderr (.getErrorStream ^Process process)]
          (let [out (future (stream-text stdout "UTF-8"))
                err (future (stream-text stderr (.name (Charset/defaultCharset))))
                exit (.waitFor ^Process process)
                _ (swap! record assoc :exit exit :root-wait-exited? true)
                result {:exit exit :out @out :err @err}]
            result))
        (catch Throwable error
          (reset! primary error)
          (swap! record assoc :error error)
          (throw error))
        (finally
          (try
            (swap! record assoc :finalization ((:finalize ports) owned))
            (swap! record assoc :raw-handles ((:handles ports) owned))
            (catch Throwable error
              (swap! record assoc :finalization-error error :terminal :unproven)
              (if-let [original @primary]
                (when-not (identical? original error) (.addSuppressed ^Throwable original error))
                ;; A nonzero Git exit keeps the original h/git! refusal. Its
                ;; cleanup failure remains a distinct terminal observation.
                (when (or (nil? (:exit @record)) (zero? (:exit @record)))
                  (reset! primary error) (throw error))))
            (finally
              (try
                ((:verify-git! ports) binding)
                (catch Throwable error
                  (swap! record assoc :identity-error error)
                  (if-let [original @primary]
                    (when-not (identical? original error) (.addSuppressed ^Throwable original error))
                    (when (or (nil? (:exit @record)) (zero? (:exit @record))) (throw error))))
                (finally
                  (when (instance? InterruptedException @primary)
                    (.interrupt (Thread/currentThread))))))))))))

(defn git! [ports binding records root & args]
  (h/ensure! (and (= root (:root binding)) (contains? commands (vec args)))
             :selected-C3-Git-command)
  (let [result (shell-result! ports binding records (into ["git" "-C" root] args))]
    (h/ensure! (zero? (:exit result)) :git-identity-read)
    (str/trim (:out result))))

(defn observation-view [records]
  ;; Raw Process/token/handles remain in the child's records, never rebuilt from
  ;; these serializable observations. Unknown coverage is never promoted.
  (mapv (fn [record]
          (let [r @record]
            (cond-> (select-keys r [:argv :cwd :exit :root-wait-exited? :finalization :descendant-coverage :terminal])
              (:error r) (assoc :error-class (.getName (class (:error r))))
              (:retain-error r) (assoc :retain-error-class (.getName (class (:retain-error r))))
              (:identity-error r) (assoc :identity-error-class (.getName (class (:identity-error r))))
              (:finalization-error r) (assoc :finalization-error-class (.getName (class (:finalization-error r)))))))
        @records))
