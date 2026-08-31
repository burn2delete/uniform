(ns gravity.darwin-publication.cleanup
  "Internal Darwin publication cleanup operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.context :refer :all]
            [gravity.darwin-publication.inventory :refer :all])
  (:import [java.lang.foreign Arena FunctionDescriptor Linker
            Linker$Option MemoryLayout MemoryLayout$PathElement MemorySegment
            ValueLayout]
           [java.lang.invoke VarHandle$AccessMode]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file InvalidPathException Paths]
           [java.security MessageDigest SecureRandom]
           [java.util Collections WeakHashMap]))

(defn unlink-relative-result
  [runtime directory-descriptor leaf flags]
  (with-open [arena (Arena/ofConfined)]
    (let [name (.allocateFrom arena ^String leaf)]
      (int-call-result runtime arena :unlinkat
                       [directory-descriptor name (int flags)]))))

(defn valid-descriptor?
  [descriptor]
  (and (integer? descriptor) (not (neg? descriptor))))

(defn close-owned-descriptor-result!
  [runtime descriptor]
  (if-not (valid-descriptor? descriptor)
    true
    (try
      (with-open [arena (Arena/ofConfined)]
        (not (neg? (:value
                    (int-call-result runtime arena :close [descriptor])))))
      (catch Exception error
        (rethrow-interrupt! error)
        false))))

(declare cleanup-directory-result! abort-staged-bundle!)

(defn cleanup-entry-result!
  [runtime directory-descriptor leaf depth remaining]
  (if-not (and (valid-leaf? leaf)
               (pos? (swap! remaining dec)))
    false
    (try
      (let [snapshot
            (fstatat-result runtime directory-descriptor leaf
                            relative-bounded-stat-flags)]
        (cond
          (= enoent (:errno snapshot)) true
          (neg? (:value snapshot)) false

          (= s-ifdir (bit-and s-ifmt (get-in snapshot [:stat :mode])))
          (if (>= depth maximum-cleanup-depth)
            false
            (let [child
                  (open-relative! runtime directory-descriptor leaf
                                  relative-directory-open-flags 0
                                  :cleanup :cleanup-directory-open-failed)
                  closed? (atom false)
                  cleaned?
                  (try
                    (cleanup-directory-result!
                     runtime child (inc depth) remaining)
                    (finally
                      ;; The descriptor is local ownership and is closed once,
                      ;; including when a fatal error escapes recursion.
                      (reset! closed?
                              (close-owned-descriptor-result!
                               runtime child))))
                  removed
                  (when (and cleaned? @closed?)
                    (unlink-relative-result
                     runtime directory-descriptor leaf
                     relative-cleanup-directory-flags))]
              (and cleaned? @closed? (zero? (:value removed)))))

          :else
          (let [removed
                (unlink-relative-result runtime directory-descriptor leaf
                                        relative-cleanup-file-flags)]
            (or (zero? (:value removed)) (= enoent (:errno removed))))))
      (catch Exception error
        (rethrow-interrupt! error)
        false))))

(defn cleanup-directory-result!
  [runtime directory-descriptor depth remaining]
  (let [complete? (atom true)
        names
        (try
          (directory-inventory! runtime directory-descriptor
                                maximum-cleanup-entries)
          (catch Exception error
            (rethrow-interrupt! error)
            (reset! complete? false)
            fixed-file-names))]
    (doseq [name (sort names)]
      (when-not
       (cleanup-entry-result! runtime directory-descriptor name
                              depth remaining)
        (reset! complete? false)))
    (try
      (when-not (empty?
                 (directory-inventory! runtime directory-descriptor
                                       maximum-cleanup-entries))
        (reset! complete? false))
      (catch Exception error
        (rethrow-interrupt! error)
        (reset! complete? false)))
    @complete?))

(defn claim-abort!
  [context]
  (loop []
    (let [state (context-state! context :abort)
          control (:control (context-entry context))]
        (case (:phase state)
          :committed {:claimed? false :terminal-state state}
          :aborted {:claimed? false :terminal-state state}
          (:target-open :staged :failed)
          (let [candidate
                (-> state
                    (assoc :phase :aborting
                           :parent-descriptor nil
                           :staging-descriptor nil)
                    (update :generation inc))]
            (if (compare-and-set! control state candidate)
              {:claimed? true :owned state}
              (recur)))
          (failure! :abort :invalid-provider-lifecycle)))))

(defn attach-incomplete-cleanup!
  [error cleanup]
  (when-not (:cleanup-complete? cleanup)
    (.addSuppressed
     ^Throwable error
     (failure-ex :cleanup :cleanup-incomplete
                 {:cleanup-complete? false
                  :residue-possible? true})))
  error)

(defn abort-after-failure!
  [context error]
  (try
    (attach-incomplete-cleanup! error (abort-staged-bundle! context))
    (catch Throwable cleanup-error
      (.addSuppressed ^Throwable error ^Throwable cleanup-error)))
  (rethrow-interrupt! error)
  (throw error))

(defn abort-staged-bundle!
  "Best-effort descriptor-relative cleanup for an open target or staging value.

  Cleanup never follows a replacement path and never removes a staging name
  whose current identity differs from the held staging descriptor."
  [target]
  (let [{:keys [claimed? terminal-state owned]} (claim-abort! target)]
    (if-not claimed?
      (if (= :committed (:phase terminal-state))
        {:status :already-committed
         :published? true
         :cleanup-applicable? false
         :native-calls 0}
        {:status :already-aborted
         :published? false
         :cleanup-complete?
         (true? (:cleanup-complete? terminal-state))
         :residue-possible?
         (true? (:residue-possible? terminal-state))
         :native-calls 0})
      (let [runtime (:runtime owned)
            complete? (atom true)
            remaining (atom (inc maximum-cleanup-entries))
            parent-descriptor (:parent-descriptor owned)
            staging-descriptor (:staging-descriptor owned)
            staging-leaf (:staging-leaf owned)
            staging-stat (:staging-stat owned)]
        (try
          (when (valid-descriptor? staging-descriptor)
            (when-not
             (cleanup-directory-result!
              runtime staging-descriptor 0 remaining)
              (reset! complete? false)))
          (finally
            (when-not
             (close-owned-descriptor-result! runtime staging-descriptor)
              (reset! complete? false))))
        (when (and (valid-descriptor? parent-descriptor)
                   (valid-leaf? staging-leaf))
          (if-not staging-stat
            (reset! complete? false)
            (try
              (let [current
                    (fstatat-result runtime parent-descriptor staging-leaf
                                    relative-bounded-stat-flags)]
                (cond
                  (= enoent (:errno current)) nil
                  (and (zero? (:value current))
                       (same-object? staging-stat (:stat current)))
                  (let [removed
                        (unlink-relative-result
                         runtime parent-descriptor staging-leaf
                         relative-cleanup-directory-flags)]
                    (when-not (or (zero? (:value removed))
                                  (= enoent (:errno removed)))
                      (reset! complete? false)))
                  :else (reset! complete? false)))
              (catch Exception error
                (rethrow-interrupt! error)
                (reset! complete? false)))))
        (when-not
         (close-owned-descriptor-result! runtime parent-descriptor)
          (reset! complete? false))
        (update-control! target #{:aborting}
                         #(assoc % :phase :aborted
                                 :staging-leaf nil :staging-stat nil
                                 :file-specs nil :file-records nil
                                 :publication-receipt nil
                                 :cleanup-complete? @complete?
                                 :residue-possible? (not @complete?))
                         :finish-abort)
        {:status :aborted
         :published? false
         :cleanup-complete? @complete?
         :residue-possible? (not @complete?)}))))
