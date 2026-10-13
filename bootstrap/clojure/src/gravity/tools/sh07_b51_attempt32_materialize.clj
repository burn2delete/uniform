(ns gravity.tools.sh07-b51-attempt32-materialize
  "CLI publication boundary for binding-replay successor evidence."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity]
            [gravity.tools.sh07-b51-attempt32-tier0-materializer :as tier0])
  (:import [java.io PushbackReader StringReader]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths
            StandardCopyOption StandardOpenOption]
           [java.nio.file.attribute FileAttribute]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_attempt32_materialize/01-fail"
 "/gravity/tools/sh07_b51_attempt32_materialize/02-reload-validate-output")
