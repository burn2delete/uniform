(ns gravity.darwin-publication.commit
  "Internal Darwin publication commit operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.context :refer :all]
            [gravity.darwin-publication.file-io :refer :all]
            [gravity.darwin-publication.inventory :refer :all]
            [gravity.darwin-publication.cleanup :refer :all]
            [gravity.darwin-publication.name-binding :refer :all])
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

(defn revalidate-staged-bundle!
  [staged state check-destination?]
  (let [runtime (:runtime state)
        parent-descriptor (:parent-descriptor state)
        staging-descriptor (:staging-descriptor state)
        parent-stat
        (fstat! runtime parent-descriptor :revalidate-parent)
        _ (assert-no-extended-acl!
           runtime parent-descriptor :revalidate-parent)
        staging-stat
        (fstat! runtime staging-descriptor :revalidate-staging)
        _ (assert-no-extended-acl!
           runtime staging-descriptor :revalidate-staging)
        inventory (directory-inventory! runtime staging-descriptor)
        file-records
        (into
         (sorted-map)
         (map
          (fn [[logical-path expected]]
            [logical-path
             (verify-relative-file!
              runtime staging-descriptor
              (:effective-user state) logical-path expected)]))
         (:file-specs state))]
    (when-not
     (and (= (:publication-receipt state)
             (:publication-receipt staged))
          (same-identity? (:parent-stat state) parent-stat)
          (same-identity? (:staging-stat state) staging-stat)
          (descriptor-paths-stable? state)
          (staging-name-bound-to-descriptor? state)
          (= fixed-file-names inventory)
          (= (:file-records state) file-records)
          (or (not check-destination?)
              (destination-absent? state)))
      (failure! :precommit :descriptor-or-content-identity-changed
                {:expected-file-count 7
                 :observed-file-count (count inventory)}))
    staged))

(defn commit-staged-bundle!
  "Publish a verified private bundle and return an already-built success value.

  The native rename is the only publication linearization point.  Descriptor
  cleanup after success is best effort and cannot change the returned result."
  [staged success-value]
  (let [committing
        (update-control! staged #{:staged}
                         #(assoc % :phase :committing)
                         :commit)]
    (try
      (checkpoint! :before-final-name-binding committing)
      (revalidate-staged-bundle! staged committing true)
      (checkpoint! :before-native-rename committing)
      ;; Permit the destination checkpoint race so RENAME_EXCL remains the
      ;; collision linearizer, then reverify every other staged fact.
      (revalidate-staged-bundle! staged committing false)
      (let [runtime (:runtime committing)
            result
            (with-open [arena (Arena/ofConfined)]
              (let [source
                    ;; Darwin RENAME_NOFOLLOW_ANY renames a final symlink.
                    ;; A trailing slash requires the source itself to be a
                    ;; directory, while the validated base remains one leaf.
                    (.allocateFrom arena
                                   (str (:staging-leaf committing) "/"))
                    destination
                    (.allocateFrom arena
                                   ^String (:destination-leaf committing))]
                (int-call-result
                 runtime arena :renameatx-np
                 [(:parent-descriptor committing) source
                  (:parent-descriptor committing) destination
                  (int exclusive-rename-flags)])))]
        (when (neg? (:value result))
          (failure! :commit
                    (if (= eexist (:errno result))
                      :destination-collision :exclusive-rename-failed)
                    (assoc result
                           :output-collision? (= eexist (:errno result)))))
        ;; Consume fd ownership in the control atom before closing.  A close
        ;; failure may already have released and recycled the integer.
        (update-control! staged #{:committing}
                         #(assoc % :phase :committed
                                   :parent-descriptor nil
                                   :staging-descriptor nil
                                   :staging-leaf nil
                                   :staging-stat nil
                                   :file-specs nil
                                   :file-records nil
                                   :publication-receipt nil)
                         :finish-commit)
        (close-owned-descriptor-result!
         runtime (:staging-descriptor committing))
        (close-owned-descriptor-result!
         runtime (:parent-descriptor committing))
        success-value)
      (catch Throwable error
        (when (= :committing
                 (:phase (context-state! staged :commit-failure)))
          (try
            (mark-failed! staged :committing)
            (catch Throwable transition-error
              (.addSuppressed ^Throwable error ^Throwable transition-error))))
        (abort-after-failure! staged error)))))
