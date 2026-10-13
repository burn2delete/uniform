(ns gravity.tools.sh07-b51-attempt32-coordinator-child
  "Fresh-process V9 coordinator for the isolated A, B, then M topology.

  Keep this namespace project-free.  Entry validation precedes every dynamic
  project load.  In particular this source never requires, resolves, or invokes
  either checker namespace or the checker-child namespace in M."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream ByteArrayOutputStream Closeable File
            InputStream InputStreamReader OutputStream]
           [java.lang ProcessHandle]
           [java.lang.management ManagementFactory]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption Path Paths StandardOpenOption]
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
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/01-transport-limits"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/02-eligibility-check-valid"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/03-run-project-free-process"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/04-pre-manifest-tcb"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/05-validate-complete-classpath"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/06-join-child"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/07-validate-child"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/08-run-m-session"
 "/gravity/tools/sh07_b51_attempt32_coordinator_child/09-m-namespace-receipts")
