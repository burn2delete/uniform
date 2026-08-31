(ns gravity.darwin-publication.target
  "Internal Darwin publication target operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.native-runtime :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.context :refer :all])
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

(defn open-target!
  "Open and authenticate an output parent without following symlinks.

  The returned value is an opaque, single-use provider context.  Callers must
  pass it to `stage-bundle!` or `abort-staged-bundle!`."
  [output-directory]
  (let [location (output-location output-directory)
        runtime (native-runtime!)
        parent-descriptor (atom nil)]
    (try
      (let [descriptor
            (open-absolute-directory! runtime (:parent-path location))
            _ (reset! parent-descriptor descriptor)
            effective-user (effective-user-id! runtime)
            parent-stat (fstat! runtime descriptor :authenticate-parent)
            _ (assert-no-extended-acl!
               runtime descriptor :authenticate-parent)
            parent-path
            (descriptor-path! runtime descriptor :authenticate-parent)
            destination
            (fstatat-result runtime descriptor
                            (:destination-leaf location)
                            relative-unique-stat-flags)]
        (when-not
         (and (= (:parent-path location) parent-path)
              (directory-stat-valid? parent-stat effective-user nil))
          (failure! :authenticate-parent :untrusted-parent-descriptor))
        (cond
          (zero? (:value destination))
          (failure! :authenticate-destination :destination-exists
                    {:output-collision? true})

          (not= enoent (:errno destination))
          (failure! :authenticate-destination
                    :destination-absence-check-failed destination))
        (let [token (Object.)
              control
              (atom {:provider :gravity/darwin-descriptor-publication
                     :provider-version provider-version
                     :phase :target-open
                     :generation 0
                     :token token
                     :runtime runtime
                     :parent-descriptor descriptor
                     :staging-descriptor nil
                     :staging-leaf nil
                     :staging-stat nil
                     :parent-path parent-path
                     :destination-path (:destination-path location)
                     :destination-leaf (:destination-leaf location)
                     :effective-user effective-user
                     :parent-stat parent-stat
                     :parent-identity-hash (identity-hash parent-stat)})]
          (register-context! (publication-context nil) control token)))
      (catch Throwable error
        (when-let [descriptor @parent-descriptor]
          (close-fd-quietly! runtime descriptor))
        (rethrow-interrupt! error)
        (throw error)))))
