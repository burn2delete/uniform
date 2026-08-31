(ns gravity.darwin-publication.verification
  "Internal Darwin publication verification operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.native-runtime :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.specs :refer :all]
            [gravity.darwin-publication.file-io :refer :all]
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

(defn publisher-evidence-valid?
  [evidence]
  (and
   (= :gravity/darwin-descriptor-publication (:provider evidence))
   (= provider-version (:provider-version evidence))
   (= ["26.0.1" 26 true
       :jdk-26-foreign-function-and-memory
       :darwin-libsystem "renameatx_np" :darwin-renameatx-np
       :failure-only
       :held-parent-and-staging-directory-descriptors]
      ((juxt :jdk-version :jdk-feature :native-access-enabled?
             :ffi-provider :native-library :symbol :commit-primitive
             :errno-read-policy :path-identity-linearization)
       evidence))
   (= #{:descriptor-bound-parent :descriptor-relative-staging
        :resolve-beneath :no-symlink-traversal
        :exclusive-destination :unique-regular-files
        :exact-directory-inventory
        :no-extended-access-control-lists}
      (:guarantee-scope evidence))
   (= {:rename-excl rename-excl
       :rename-nofollow-any rename-nofollow-any
       :rename-resolve-beneath rename-resolve-beneath
       :combined exclusive-rename-flags}
      (:flags evidence))
   (string? (:parent-identity-hash evidence))
   (string? (:staging-identity-hash evidence))
   (true? (:source-directory-trailing-slash? evidence))
   (false? (:postcommit-close-failures-change-result? evidence))
   (false? (:crash-durable-publication? evidence))
   (false? (:same-euid-concurrent-mutation-resistant? evidence))))

(defn open-published-directory!
  [receipt]
  (let [location (output-location (:actual-output-directory receipt))
        runtime (native-runtime!)
        parent-descriptor (atom nil)
        published-descriptor (atom nil)]
    (try
      (let [parent
            (open-absolute-directory! runtime (:parent-path location))
            _ (reset! parent-descriptor parent)
            effective-user (effective-user-id! runtime)
            parent-stat (fstat! runtime parent :verify-parent)
            _ (assert-no-extended-acl! runtime parent :verify-parent)
            parent-path
            (descriptor-path! runtime parent :verify-parent)
            published
            (open-relative! runtime parent (:destination-leaf location)
                            relative-directory-open-flags 0
                            :verify-publication
                            :published-directory-open-failed)
            _ (reset! published-descriptor published)
            published-stat
            (fstat! runtime published :verify-publication)
            _ (assert-no-extended-acl!
               runtime published :verify-publication)
            published-path
            (descriptor-path! runtime published :verify-publication)]
        (when-not
         (and (= (:parent-path location) parent-path)
              (= (:destination-path location) published-path)
              (directory-stat-valid? parent-stat effective-user nil)
              (directory-stat-valid?
               published-stat effective-user published-directory-mode))
          (failure! :verify-publication
                    :published-directory-provenance-mismatch))
        {:runtime runtime
         :parent-descriptor parent
         :published-descriptor published
         :effective-user effective-user
         :parent-stat parent-stat
         :published-stat published-stat
         :location location})
      (catch Throwable error
        (close-fd-quietly! runtime @published-descriptor)
        (close-fd-quietly! runtime @parent-descriptor)
        (rethrow-interrupt! error)
        (throw error)))))

(defn verify-published-bundle!
  "Reopen and verify a published bundle through held descriptors."
  [receipt file-specs]
  (let [specs (normalized-file-specs file-specs)
        expected-file-records
        (into
         (sorted-map)
         (map (fn [[name spec]]
                [name (select-keys spec
                                   [:byte-count :content-hash :mode])]))
         specs)]
    (when-not
     (and (map? receipt)
          (= :published-atomically-after-final-verification
             (:status receipt))
          (= expected-file-records (:file-records receipt))
          (= {:directory "0755" :executable "0755"
              :nonexecutable "0644"}
             (:mode-policy receipt))
          (publisher-evidence-valid? (:publisher-evidence receipt)))
      (failure! :verify-publication :invalid-publication-receipt))
    (let [opened (open-published-directory! receipt)
          runtime (:runtime opened)]
      (try
        (let [publisher (:publisher-evidence receipt)
              inventory
              (directory-inventory! runtime (:published-descriptor opened))
              file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (verify-relative-file!
                    runtime (:published-descriptor opened)
                    (:effective-user opened) logical-path expected)]))
               specs)]
          (when-not
           (and (= fixed-file-names inventory)
                (= expected-file-records
                   (into
                    (sorted-map)
                    (map (fn [[name record]]
                           [name (select-keys record
                                              [:byte-count :content-hash
                                               :mode])]))
                    file-records))
                (= (:parent-identity-hash publisher)
                   (identity-hash (:parent-stat opened)))
                (= (:staging-identity-hash publisher)
                   (identity-hash (:published-stat opened))))
            (failure! :verify-publication
                      :published-bundle-content-or-identity-mismatch
                      {:expected-file-count 7
                       :observed-file-count (count inventory)}))
          {:status :passed
           :publication :descriptor-relative-exclusive-rename
           :file-count 7
           :file-records expected-file-records
           :publisher-evidence publisher})
        (finally
          (close-fd-quietly! runtime (:published-descriptor opened))
          (close-fd-quietly! runtime (:parent-descriptor opened)))))))
