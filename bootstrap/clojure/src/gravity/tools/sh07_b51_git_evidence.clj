(ns gravity.tools.sh07-b51-git-evidence
  "Bounded Git-object and filesystem publication seam for SH-07 B5.1 V9.

  This launcher authenticates storage, transport, and publication mechanics.
  It deliberately returns `:authority :none`: the full V9 runtime contract
  discharge and receipt-normalization implementation must join this seam before
  an E9 candidate can claim evidence authority."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io Closeable PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file DirectoryStream Files LinkOption OpenOption Path Paths
            StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes]
           [java.time Instant]
           [java.util Arrays Optional]
           [java.util.concurrent Callable ExecutionException
            ExecutorCompletionService Executors Future TimeUnit
            TimeoutException]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_git_evidence/00-owned-source-fragments"
 "/gravity/tools/sh07_b51_git_evidence/01-git-executable"
 "/gravity/tools/sh07_b51_git_evidence/02-row-plan-prefix"
 "/gravity/tools/sh07_b51_git_evidence/03-row-plan-suffix"
 "/gravity/tools/sh07_b51_git_evidence/04-predicate-spec-vector"
 "/gravity/tools/sh07_b51_git_evidence/05-evaluate-predicate-node"
 "/gravity/tools/sh07_b51_git_evidence/06-exact-keys"
 "/gravity/tools/sh07_b51_git_evidence/07-run-bounded-process"
 "/gravity/tools/sh07_b51_git_evidence/08-cat-file-operation"
 "/gravity/tools/sh07_b51_git_evidence/09-validate-source-object-proof"
 "/gravity/tools/sh07_b51_git_evidence/10-build-source-descriptors"
 "/gravity/tools/sh07_b51_git_evidence/11-build-a9-stage-result-e1"
 "/gravity/tools/sh07_b51_git_evidence/12-mask-span-for-rhs"
 "/gravity/tools/sh07_b51_git_evidence/13-build-normalized-mask-files"
 "/gravity/tools/sh07_b51_git_evidence/14-fenced-edn-after"
 "/gravity/tools/sh07_b51_git_evidence/15-reconstruct-pre-object-snapshot"
 "/gravity/tools/sh07_b51_git_evidence/16-validate-pre-discharge-envelope"
 "/gravity/tools/sh07_b51_git_evidence/17-validate-git-observation"
 "/gravity/tools/sh07_b51_git_evidence/18-build-evidence-eligibility"
 "/gravity/tools/sh07_b51_git_evidence/19-validate-runtime-lifecycle-ref"
 "/gravity/tools/sh07_b51_git_evidence/20-validate-eligibility-object"
 "/gravity/tools/sh07_b51_git_evidence/21-list-entry-names"
 "/gravity/tools/sh07_b51_git_evidence/22-validate-runtime-identity-receipt"
 "/gravity/tools/sh07_b51_git_evidence/23-cleanup-unpublished")
