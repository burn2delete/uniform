(ns gravity.darwin-publication.contract
  "Internal Darwin publication contract operations."
  (:require [clojure.string :as str])
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

(def namespace-contract
  {:namespace 'gravity.darwin-publication
   :contract-boundary :darwin-descriptor-relative-publication
   :public-api
   {'open-target! {:arglists '([output-directory])}
    'stage-bundle! {:arglists '([target file-specs])}
    'commit-staged-bundle! {:arglists '([staged success-value])}
    'verify-published-bundle! {:arglists '([receipt file-specs])}
    'abort-staged-bundle! {:arglists '([target-or-staged])}}
   :owns
   [:native-runtime-preflight :held-parent-descriptor
    :descriptor-relative-staging :descriptor-relative-file-io
    :descriptor-relative-inventory :exclusive-native-rename
    :descriptor-relative-cleanup :descriptor-relative-verification
    :raw-bounded-failure-carriers]
   :does-not-own
   [:compiler-semantics :artifact-identity :diagnostic-rule-selection
    :governance-authority :cli-presentation :public-exposure
    :release-credit :self-hosting-credit]
   :requires ['clojure.core 'clojure.string]
   :forbids ['gravity.bootstrap 'gravity.diagnostics 'gravity.cli]
   :host {:jdk-feature 26 :jdk-version "26.0.1"
          :os-name "Mac OS X" :os-arch "aarch64"}
   :public? false :release? false :self-hosted? false})

(def provider-version 1)
(def maximum-file-bytes (* 8 1024 1024))
(def maximum-path-bytes 4096)
(def maximum-leaf-bytes 255)
(def maximum-cleanup-entries 64)
(def maximum-cleanup-depth 4)
(def stat-byte-count 144)
(def dirent-maximum-byte-count 1048)
(def path-buffer-byte-count 4096)

(def absolute-directory-open-flags 0x21100000)
(def relative-directory-open-flags 0x21103000)
(def exclusive-file-open-flags 0x21003a01)
(def unique-file-read-flags 0x21003000)

(def at-removedir 0x0080)
(def relative-unique-stat-flags 0xa800)
(def relative-bounded-stat-flags 0x2800)
(def relative-cleanup-file-flags 0)
(def relative-cleanup-directory-flags at-removedir)

(def f-getpath 50)
(def rename-excl 0x04)
(def rename-nofollow-any 0x10)
(def rename-resolve-beneath 0x20)
(def exclusive-rename-flags 0x34)

(def s-ifmt 0xf000)
(def s-ifdir 0x4000)
(def s-ifreg 0x8000)
(def owner-only-directory-mode 0700)
(def published-directory-mode 0755)
(def executable-file-mode 0755)
(def nonexecutable-file-mode 0644)

(def enoent 2)
(def eintr 4)
(def eexist 17)
(def acl-type-extended 0x100)

(def fixed-file-names
  #{"program.c" "program.h" "program.o" "program"
    "manifest.edn" "provenance.edn" "conformance.edn"})

(def ^:dynamic *operation-checkpoint*
  (fn [_event _bounded-context] nil))
