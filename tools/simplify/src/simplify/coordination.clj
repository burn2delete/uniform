(ns simplify.coordination
  "Advance's coordination interface over the shared runtime protocol."
  (:require [clojure.java.io :as io]
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

(defn transaction! [ctx f] (rt/coordinate! ctx f))
(defn owner [ctx] (rt/canonical (:state-file ctx)))
(defn covers? [a b] (rt/covers? a b))
(defn overlaps? [a b] (rt/overlap? a b))

(defn claims [ctx]
  (transaction! ctx
                (fn [state]
                  [state (mapv #(assoc % :identity [(str (io/file (:state-dir %) "state.edn")) (:key %)]
                                       :state-file (str (io/file (:state-dir %) "state.edn")))
                               (vals (:writers state)))])))

(defn claim! [ctx key paths]
  (boolean (rt/claim-paths! ctx key paths :implementing)))

(defn release! [ctx key]
  (when-let [lease (get (some-> (:path-leases ctx) deref) key)]
    (rt/release-paths! ctx lease)
    true))

(defn register! [ctx]
  ;; Check actual old controller locks, without probing recorded PIDs.
  (when-not (some-> (:coordinator-owner ctx) deref) (rt/assert-compatible-controllers! ctx))
  (rt/import-state-coordination! ctx)
  ;; Retained in-flight work needs original-owner inspection before this
  ;; controller performs even unrelated provider setup. Attention holds and
  ;; other campaigns still reserve only their actual paths.
  (when-let [retained (some (fn [[key task]]
                             (when (#{:implementing :review} (:status task))
                               {:finding key :status (:status task)}))
                           (:tasks @(:state ctx)))]
    (rt/fail! "Retained worker requires inspected handover before provider setup" retained))
  (when-let [pending (transaction! ctx
                                  (fn [state]
                                    [state (first (filter #(= (rt/canonical (:state-dir ctx)) (:state-dir %))
                                                          (vals (:main-effects state))))]))]
    (rt/fail! "Unresolved main effect requires inspected handover before provider setup" {:effect (:id pending)}))
  (transaction! ctx
                (fn [state]
                  [(assoc-in state [:controllers (owner ctx)]
                             {:state-file (owner ctx) :lock (:lock-file ctx)
                              :pid (.pid (ProcessHandle/current)) :protocol 2}) true])))

(defn unregister! [ctx]
  (transaction! ctx (fn [state] [(update state :controllers dissoc (owner ctx)) true])))

(defn with-merge [ctx f] (rt/try-with-main-lock ctx f))
