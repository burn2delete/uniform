(ns gravity.tools.sh07-b51-attempt32-checker-child
  "Fresh-process entrypoint for one V9 SH-07 B5.1 census checker.

  Keep this namespace project-free.  The entry preflight runs before the
  shared codec, bootstrap constructor, checker-identity helper, or selected
  checker is loaded from the C-derived classpath."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream ByteArrayOutputStream Closeable File
            InputStream InputStreamReader OutputStream]
           [java.lang ProcessHandle]
           [java.lang.management ManagementFactory]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.nio.file.attribute BasicFileAttributes]
           [java.security MessageDigest]
           [java.time Instant]
           [java.util Arrays Optional]
           [java.util.concurrent Callable ExecutionException
            ExecutorCompletionService Executors Future TimeUnit
            TimeoutException]
           [java.util.jar JarFile]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt32_checker_child/01-transport-limits"
 "/gravity/tools/sh07_b51_attempt32_checker_child/02-source-event-plan"
 "/gravity/tools/sh07_b51_attempt32_checker_child/03-git-operation-core"
 "/gravity/tools/sh07_b51_attempt32_checker_child/04-validate-complete-classpath"
 "/gravity/tools/sh07_b51_attempt32_checker_child/05-validate-source-read-receipt"
 "/gravity/tools/sh07_b51_attempt32_checker_child/06-v9-checker-output-shell"
 "/gravity/tools/sh07_b51_attempt32_checker_child/07-build-response")
