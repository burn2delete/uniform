(defn-
 p15-s23-b3-llvm-linux-toolchain-transaction!
 [candidate source-path lowering]
 (let
  [workspace
   (java.nio.file.Files/createTempDirectory
    "gravity-b3-linux-"
    (make-array java.nio.file.attribute.FileAttribute 0))
   ir-path
   (.resolve workspace "program.ll")
   object-path
   (.resolve workspace "program.o")
   executable-path
   (.resolve workspace "program")
   image
   p15-s23-b3-llvm-linux-image
   target
   (:target-triple p15-s23-b3-llvm-policy)
   primary-failure
   (atom nil)]
  (try
   (java.nio.file.Files/setPosixFilePermissions
    workspace
    p15-s23-b3-llvm-private-directory-permissions)
   (java.nio.file.Files/write
    ir-path
    (.getBytes
     (:llvm-ir lowering)
     java.nio.charset.StandardCharsets/UTF_8)
    (into-array
     java.nio.file.OpenOption
     [java.nio.file.StandardOpenOption/CREATE_NEW
      java.nio.file.StandardOpenOption/WRITE]))
   (java.nio.file.Files/setPosixFilePermissions
    ir-path
    p15-s23-b3-llvm-nonexecutable-permissions)
   (let
    [execution-state
     (semantic-llvm-linux-toolchain-execution!
      candidate
      source-path
      lowering
      workspace
      ir-path
      object-path
      executable-path
      image
      target
      primary-failure)
     observation-state
     (semantic-llvm-linux-toolchain-observations!
      candidate
      source-path
      lowering
      workspace
      ir-path
      object-path
      executable-path
      image
      target
      primary-failure
      execution-state)]
    (semantic-llvm-linux-toolchain-record
     candidate
     source-path
     lowering
     workspace
     ir-path
     object-path
     executable-path
     image
     target
     primary-failure
     observation-state))
   (catch
    InterruptedException
    interrupted
    (.interrupt (Thread/currentThread))
    (throw interrupted))
   (catch Throwable error (reset! primary-failure error) (throw error))
   (finally
    (try
     (p15-s23-b3-llvm-delete-tree! candidate workspace source-path)
     (catch
      Throwable
      cleanup
      (if-let
       [error @primary-failure]
       (.addSuppressed error cleanup)
       (throw cleanup))))))))
