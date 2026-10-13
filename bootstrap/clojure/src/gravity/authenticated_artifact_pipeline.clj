(ns gravity.authenticated-artifact-pipeline
  "The single hosted source-to-checked-core artifact pipeline.

  This namespace is an orchestration facade, not a replacement compiler.  Each
  stage delegates to the existing Gravity bootstrap entrypoint and wraps its
  result in a small authenticated envelope.  The envelope makes the stage
  graph, source identity, policy, provenance, cache decision, and receipt
  explicit.  Fast reuse is process-local and non-authoritative; fresh requests
  never consult the store and execute the same entrypoints again.  Each stage
  consumes the immediately preceding authenticated artifact; transitive
  lineage remains embedded in that owner artifact and is not relabeled as a
  direct edge.

  No envelope produced here grants SH, proof, integration, release,
  self-hosting, seed-retirement, safety, or performance authority."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-pass-cache :as c2-pass-cache]
            [gravity.digest :as digest])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file Files Path]
           [java.util Arrays]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/authenticated_artifact_pipeline/01-pipeline-schema-version"
 "/gravity/authenticated_artifact_pipeline/02-clear-cache"
 "/gravity/authenticated_artifact_pipeline/03-revalidate-payload"
 "/gravity/authenticated_artifact_pipeline/04-envelope-valid"
 "/gravity/authenticated_artifact_pipeline/05-run-pipeline")
