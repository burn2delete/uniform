(ns gravity.darwin-publication.stage
  "Internal Darwin publication stage operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.context :refer :all]
            [gravity.darwin-publication.specs :refer :all]
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

(defn stage-bundle!
  "Create, write, sync, inventory, and verify a seven-file private bundle.

  The result remains private.  Its publication receipt is only valid if a
  later `commit-staged-bundle!` succeeds."
  [target file-specs]
  (let [specs
        (try
          (normalized-file-specs file-specs)
          (catch Throwable error
            (abort-after-failure! target error)))
        starting
        (update-control! target #{:target-open}
                         #(assoc % :phase :staging)
                         :stage-bundle)
        runtime (:runtime starting)
        parent-descriptor (:parent-descriptor starting)
        unowned-staging-descriptor (atom nil)]
    (try
      (let [staging-leaf
            (mkdir-relative! runtime parent-descriptor)
            _ (update-control! target #{:staging}
                               #(assoc % :staging-leaf staging-leaf)
                               :record-staging-name)
            descriptor
            (open-relative! runtime parent-descriptor
                            staging-leaf relative-directory-open-flags 0
                            :open-staging
                            :staging-directory-open-failed)
            _ (reset! unowned-staging-descriptor descriptor)
            _ (update-control! target #{:staging}
                               #(assoc % :staging-descriptor descriptor)
                               :record-staging-descriptor)
            _ (reset! unowned-staging-descriptor nil)
              stat (fstat! runtime descriptor :authenticate-staging)
              _ (assert-no-extended-acl!
                 runtime descriptor :authenticate-staging)
              descriptor-path
              (descriptor-path! runtime descriptor :authenticate-staging)
              expected-path (str (:parent-path starting) "/" staging-leaf)
              name-stat
              (fstatat-result runtime parent-descriptor
                              staging-leaf relative-unique-stat-flags)
              _
              (when-not
               (and (= expected-path descriptor-path)
                    (directory-stat-valid?
                     stat (:effective-user starting)
                     owner-only-directory-mode)
                    (zero? (:value name-stat))
                    (same-identity? stat (:stat name-stat)))
                (failure! :authenticate-staging
                          :untrusted-staging-descriptor))
              authenticated
              (update-control! target #{:staging}
                               #(assoc % :staging-stat stat)
                               :record-staging-identity)
              _ (checkpoint! :staging-handle-opened authenticated)
              initial-file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (create-relative-file!
                    runtime descriptor (:effective-user starting)
                    logical-path expected)]))
               specs)
              _ (chmod-fd! runtime descriptor published-directory-mode
                           :finalize-staging-mode)
              _ (assert-no-extended-acl!
                 runtime descriptor :finalize-staging-mode)
              _ (fsync-fd! runtime descriptor :sync-staging-directory)
              before-final-state (context-state! target :stage-bundle)
              _ (checkpoint! :before-final-staging-verification
                             before-final-state)
              final-stat (fstat! runtime descriptor :verify-staging)
              _ (assert-no-extended-acl!
                 runtime descriptor :verify-staging)
              final-name-stat
              (fstatat-result runtime parent-descriptor
                              staging-leaf relative-unique-stat-flags)
              inventory (directory-inventory! runtime descriptor)
              final-file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (verify-relative-file!
                    runtime descriptor (:effective-user starting)
                    logical-path expected)]))
               specs)
              _
              (when-not
               (and (= fixed-file-names inventory)
                    (= initial-file-records final-file-records)
                    (directory-stat-valid?
                     final-stat (:effective-user starting)
                     published-directory-mode)
                    (zero? (:value final-name-stat))
                    (same-identity? final-stat (:stat final-name-stat)))
                (failure! :verify-staging :staging-contract-mismatch
                          {:expected-file-count 7
                           :observed-file-count (count inventory)
                           :expected-mode published-directory-mode
                           :observed-mode
                           (bit-and 0x0fff (:mode final-stat))}))
              publisher-evidence
              {:provider :gravity/darwin-descriptor-publication
               :provider-version provider-version
               :jdk-version "26.0.1" :jdk-feature 26
               :native-access-enabled? true
               :ffi-provider :jdk-26-foreign-function-and-memory
               :native-library :darwin-libsystem
               :symbol "renameatx_np"
               :commit-primitive :darwin-renameatx-np
               :errno-read-policy :failure-only
               :path-identity-linearization
               :held-parent-and-staging-directory-descriptors
                 :guarantee-scope
               #{:descriptor-bound-parent :descriptor-relative-staging
                 :resolve-beneath :no-symlink-traversal
                 :exclusive-destination :unique-regular-files
                 :exact-directory-inventory
                 :no-extended-access-control-lists}
               :flags
               {:rename-excl rename-excl
                :rename-nofollow-any rename-nofollow-any
                :rename-resolve-beneath rename-resolve-beneath
                :combined exclusive-rename-flags}
               :parent-identity-hash (:parent-identity-hash starting)
               :staging-identity-hash (identity-hash final-stat)
               :source-directory-trailing-slash? true
               :postcommit-close-failures-change-result? false
               :crash-durable-publication? false
               :same-euid-concurrent-mutation-resistant? false}
              receipt
              {:status :published-atomically-after-final-verification
               :actual-output-directory (:destination-path starting)
               :file-records
               (into
                (sorted-map)
                (map (fn [[name record]]
                       [name (select-keys record
                                          [:byte-count :content-hash :mode])]))
                final-file-records)
               :publisher-evidence publisher-evidence
               :mode-policy
               {:directory "0755" :executable "0755"
                :nonexecutable "0644"}}
              _ (update-control! target #{:staging}
                                 #(assoc % :phase :staged
                                           :staging-stat final-stat
                                           :file-specs specs
                                           :file-records final-file-records
                                           :publication-receipt receipt)
                                 :finish-staging)]
        (let [{:keys [control token]} (context-entry target)]
          (register-context! (publication-context receipt) control token)))
      (catch Throwable error
        (when-let [descriptor @unowned-staging-descriptor]
          (close-owned-descriptor-result! runtime descriptor))
        (try
          (mark-failed! target :staging)
          (catch Throwable transition-error
            (.addSuppressed ^Throwable error ^Throwable transition-error)))
        (abort-after-failure! target error)))))
