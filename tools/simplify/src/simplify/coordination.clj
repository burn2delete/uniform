(ns simplify.coordination
  "Shared writer claims and the main-write window for local harnesses."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.runtime :as rt])
  (:import [java.lang ProcessHandle]
           [java.nio.channels FileChannel OverlappingFileLockException]
           [java.nio.file StandardOpenOption]))

(defn with-file-lock [path f]
  (let [path (io/file (rt/canonical path))]
  (.mkdirs (.getParentFile path))
  (with-open [channel (FileChannel/open (.toPath path)
                                       (into-array StandardOpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE]))]
    (let [lock (try (.tryLock channel) (catch OverlappingFileLockException _ nil))]
      (when lock (try (f) (finally (.release lock))))))))

(defn transaction! [ctx f]
  (if-let [directory (:coordination-dir ctx)]
    ;; Short local registry transactions only; no model/GitHub calls under this lock.
    (loop []
      (let [result (with-file-lock (str (io/file directory "registry.lock"))
                     #(let [file (io/file directory "claims.edn")
                            state (if (.exists file) (rt/read-edn file) {:claims {} :controllers {}})
                            [updated result] (f state)]
                        (rt/write-edn! file updated)
                        {:value result}))]
        (if result (:value result) (do (Thread/sleep 25) (recur)))))
    (second (f {:claims {} :controllers {}}))))

(defn owner [ctx] (rt/canonical (:state-file ctx)))
(defn covers? [a b] (or (= a ".") (= a b) (str/starts-with? b (str a "/"))))
(defn overlaps? [a b] (some (fn [x] (some #(or (covers? x %) (covers? % x)) b)) a))

(defn claims [ctx] (transaction! ctx (fn [state] [state (vals (:claims state))])))

(defn claim! [ctx key paths]
  (when (empty? paths) (rt/fail! "Writer claim needs owned paths" {:key key}))
  (let [identity [(owner ctx) key]]
    (transaction! ctx
      (fn [state]
        (let [others (remove #(= identity (:identity %)) (vals (:claims state)))
              conflict (some #(when (overlaps? paths (:paths %)) %) others)]
          (if conflict [state false]
              [(assoc-in state [:claims identity] {:identity identity :key key :paths paths
                                                    :campaign (get-in ctx [:config :campaign])
                                                    :state-file (:state-file ctx)}) true]))))))

(defn release! [ctx key]
  (transaction! ctx (fn [state] [(update state :claims dissoc [(owner ctx) key]) true])))

(defn register! [ctx]
  (when-let [directory (:coordination-dir ctx)]
    ;; An older simplify process owns this same legacy lock but publishes no
    ;; protocol registration. Never start a second coordinator alongside it.
    (let [legacy (str (io/file directory ".." "simplify-harness.lock"))
          own? (= (rt/canonical legacy) (rt/canonical (:lock-file ctx)))]
      (when-not own?
        (let [available (with-file-lock legacy (constantly true))
              registered (transaction! ctx
                           (fn [state]
                             [state (some (fn [controller]
                                            (let [process (ProcessHandle/of (long (:pid controller)))]
                                              (and (= "simplify-harness.lock" (:lock controller))
                                                   (= 1 (:protocol controller)) (.isPresent process)
                                                   (.isAlive ^ProcessHandle (.get process)))))
                                          (vals (:controllers state)))]))]
          (when (and (not available) (not registered))
            (rt/fail! "Restart the running simplify harness with shared coordination before advancing" {})))))
    (transaction! ctx (fn [state]
                        [(assoc-in state [:controllers (owner ctx)]
                                   {:lock (.getName (io/file (:lock-file ctx)))
                                    :pid (.pid (ProcessHandle/current)) :protocol 1}) true]))))

(defn unregister! [ctx]
  (when (:coordination-dir ctx)
    (transaction! ctx (fn [state] [(update state :controllers dissoc (owner ctx)) true]))))

(defn with-merge [ctx f]
  (if-let [directory (:coordination-dir ctx)]
    (or (with-file-lock (str (io/file directory "merge.lock")) #(do (f) {:acquired true}))
        (rt/event! ctx :waiting {:reason "Another harness owns the main-write window"}))
    (f)))
