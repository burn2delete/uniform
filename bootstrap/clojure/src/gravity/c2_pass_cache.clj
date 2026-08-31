(ns gravity.c2-pass-cache
  "Bounded, persistent local-development cache for accepted C2 reader artifacts."
  (:require [clojure.edn :as edn]
            [gravity.digest :as digest]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution])
  (:import [java.math BigDecimal BigInteger]
           [java.nio ByteBuffer]
           [java.nio.channels FileChannel SeekableByteChannel]
           [java.nio.charset StandardCharsets]
           [java.nio.file DirectoryStream Files LinkOption Path Paths
            SecureDirectoryStream StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes BasicFileAttributeView
            FileAttribute PosixFileAttributeView PosixFileAttributes
            PosixFilePermissions]
           [java.util Base64 Date HashSet UUID]
           [java.util.concurrent.locks ReentrantLock]))

(load "/gravity/c2_pass_cache/foundation")
(load "/gravity/c2_pass_cache/canonical_encode")
(load "/gravity/c2_pass_cache/canonical_decode")
(load "/gravity/c2_pass_cache/source_security")
(load "/gravity/c2_pass_cache/source_snapshot")
(load "/gravity/c2_pass_cache/key")
(load "/gravity/c2_pass_cache/directories")
(load "/gravity/c2_pass_cache/store_handles")
(load "/gravity/c2_pass_cache/secure_io")
(load "/gravity/c2_pass_cache/locking")
(load "/gravity/c2_pass_cache/lookup")
(load "/gravity/c2_pass_cache/publication")
(load "/gravity/c2_pass_cache/adapter_request")
(load "/gravity/c2_pass_cache/adapter_projection")
