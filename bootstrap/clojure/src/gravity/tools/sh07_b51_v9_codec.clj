(ns gravity.tools.sh07-b51-v9-codec
  "Closed canonical identity and bounded EDN transport seam for SH-07 B5.1 V9.

  This Clojure leaf is bootstrap TCB substrate.  It supplies no Gravity
  semantic authority and makes no release, self-hosting, seed-retirement,
  hostile-host, or aggregate SH-07 claim."
  (:require [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest])
  (:import [java.io ByteArrayOutputStream InputStream PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]))

;; Ordered implementation files retain this namespace and its API.
(load
 "/gravity/tools/sh07_b51_v9_codec/01-transport-limits"
 "/gravity/tools/sh07_b51_v9_codec/02-read-bounded-bytes"
 "/gravity/tools/sh07_b51_v9_codec/03-unsigned-wire-sort")
