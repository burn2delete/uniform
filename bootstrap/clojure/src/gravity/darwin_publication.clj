(ns gravity.darwin-publication
  "JDK 26/Darwin descriptor-relative bundle publication facade."
  (:require [gravity.darwin-publication.cleanup :as cleanup]
            [gravity.darwin-publication.commit :as commit]
            [gravity.darwin-publication.context :as context]
            [gravity.darwin-publication.contract :as contract]
            [gravity.darwin-publication.failure :as failure]
            [gravity.darwin-publication.file-io :as file-io]
            [gravity.darwin-publication.inventory :as inventory]
            [gravity.darwin-publication.name-binding :as name-binding]
            [gravity.darwin-publication.native-call :as native-call]
            [gravity.darwin-publication.native-runtime :as native-runtime]
            [gravity.darwin-publication.path :as path]
            [gravity.darwin-publication.specs :as specs]
            [gravity.darwin-publication.stage :as stage]
            [gravity.darwin-publication.stat :as stat]
            [gravity.darwin-publication.target :as target]
            [gravity.darwin-publication.verification :as verification]))

;; Preserve the original private vars while implementation ownership lives in
;; the component namespaces. Fault-injection seams are rebound at the facade.
(def ^:private namespace-contract contract/namespace-contract)
(def ^:private provider-version contract/provider-version)
(def ^:private maximum-file-bytes contract/maximum-file-bytes)
(def ^:private maximum-path-bytes contract/maximum-path-bytes)
(def ^:private maximum-leaf-bytes contract/maximum-leaf-bytes)
(def ^:private maximum-cleanup-entries contract/maximum-cleanup-entries)
(def ^:private maximum-cleanup-depth contract/maximum-cleanup-depth)
(def ^:private stat-byte-count contract/stat-byte-count)
(def ^:private dirent-maximum-byte-count contract/dirent-maximum-byte-count)
(def ^:private path-buffer-byte-count contract/path-buffer-byte-count)
(def ^:private absolute-directory-open-flags contract/absolute-directory-open-flags)
(def ^:private relative-directory-open-flags contract/relative-directory-open-flags)
(def ^:private exclusive-file-open-flags contract/exclusive-file-open-flags)
(def ^:private unique-file-read-flags contract/unique-file-read-flags)
(def ^:private at-removedir contract/at-removedir)
(def ^:private relative-unique-stat-flags contract/relative-unique-stat-flags)
(def ^:private relative-bounded-stat-flags contract/relative-bounded-stat-flags)
(def ^:private relative-cleanup-file-flags contract/relative-cleanup-file-flags)
(def ^:private relative-cleanup-directory-flags contract/relative-cleanup-directory-flags)
(def ^:private f-getpath contract/f-getpath)
(def ^:private rename-excl contract/rename-excl)
(def ^:private rename-nofollow-any contract/rename-nofollow-any)
(def ^:private rename-resolve-beneath contract/rename-resolve-beneath)
(def ^:private exclusive-rename-flags contract/exclusive-rename-flags)
(def ^:private s-ifmt contract/s-ifmt)
(def ^:private s-ifdir contract/s-ifdir)
(def ^:private s-ifreg contract/s-ifreg)
(def ^:private owner-only-directory-mode contract/owner-only-directory-mode)
(def ^:private published-directory-mode contract/published-directory-mode)
(def ^:private executable-file-mode contract/executable-file-mode)
(def ^:private nonexecutable-file-mode contract/nonexecutable-file-mode)
(def ^:private enoent contract/enoent)
(def ^:private eintr contract/eintr)
(def ^:private eexist contract/eexist)
(def ^:private acl-type-extended contract/acl-type-extended)
(def ^:private fixed-file-names contract/fixed-file-names)
(def ^:private context-controls context/context-controls)
(def ^:private register-context! context/register-context!)
(def ^:private context-entry context/context-entry)
(def ^:private context-state! context/context-state!)
(def ^:private update-control! context/update-control!)
(def ^:private mark-failed! context/mark-failed!)
(def ^:private failure-ex failure/failure-ex)
(def ^:private failure! failure/failure!)
(def ^:private interrupt-like? failure/interrupt-like?)
(def ^:private rethrow-interrupt! failure/rethrow-interrupt!)
(def ^:private utf8-byte-count path/utf8-byte-count)
(def ^:private valid-leaf? path/valid-leaf?)
(def ^:private output-location path/output-location)
(def ^:private function-descriptor native-runtime/function-descriptor)
(def ^:private symbol! native-runtime/symbol!)
(def ^:private bind-captured! native-runtime/bind-captured!)
(def ^:private bind-direct! native-runtime/bind-direct!)
(def ^:private native-runtime! native-runtime/native-runtime!)
(def ^:private captured-errno native-call/captured-errno)
(def ^:private null-address? native-call/null-address?)
(def ^:private int-call! native-call/int-call!)
(def ^:private address-call-result native-call/address-call-result)
(def ^:private close-fd! native-call/close-fd!)
(def ^:private close-fd-quietly! native-call/close-fd-quietly!)
(def ^:private sha256-bytes stat/sha256-bytes)
(def ^:private sha256-text stat/sha256-text)
(def ^:private stat-record stat/stat-record)
(def ^:private fstat! stat/fstat!)
(def ^:private fstatat-result stat/fstatat-result)
(def ^:private descriptor-path! stat/descriptor-path!)
(def ^:private effective-user-id! stat/effective-user-id!)
(def ^:private assert-no-extended-acl! stat/assert-no-extended-acl!)
(def ^:private identity-record stat/identity-record)
(def ^:private identity-hash stat/identity-hash)
(def ^:private directory-stat-valid? stat/directory-stat-valid?)
(def ^:private regular-stat-valid? stat/regular-stat-valid?)
(def ^:private open-absolute-directory! stat/open-absolute-directory!)
(def ^:private open-relative! stat/open-relative!)
(def ^:private same-identity? stat/same-identity?)
(def ^:private same-object? stat/same-object?)
(def ^:private normalized-file-specs specs/normalized-file-specs)
(def ^:private random-staging-leaf specs/random-staging-leaf)
(def ^:private mkdir-relative! file-io/mkdir-relative!)
(def ^:private write-all! file-io/write-all!)
(def ^:private pread-exact! file-io/pread-exact!)
(def ^:private chmod-fd! file-io/chmod-fd!)
(def ^:private fsync-fd! file-io/fsync-fd!)
(def ^:private verify-relative-file! file-io/verify-relative-file!)
(def ^:private create-relative-file! file-io/create-relative-file!)
(def ^:private strict-utf8 file-io/strict-utf8)
(def ^:private dirent-name! file-io/dirent-name!)
(def ^:private directory-inventory! inventory/directory-inventory!)
(def ^:private unlink-relative-result cleanup/unlink-relative-result)
(def ^:private valid-descriptor? cleanup/valid-descriptor?)
(def ^:private close-owned-descriptor-result! cleanup/close-owned-descriptor-result!)
(def ^:private cleanup-entry-result! cleanup/cleanup-entry-result!)
(def ^:private cleanup-directory-result! cleanup/cleanup-directory-result!)
(def ^:private claim-abort! cleanup/claim-abort!)
(def ^:private attach-incomplete-cleanup! cleanup/attach-incomplete-cleanup!)
(def ^:private abort-after-failure! cleanup/abort-after-failure!)
(def ^:private staging-name-bound-to-descriptor? name-binding/staging-name-bound-to-descriptor?)
(def ^:private descriptor-paths-stable? name-binding/descriptor-paths-stable?)
(def ^:private destination-absent? name-binding/destination-absent?)
(def ^:private checkpoint! stat/checkpoint!)
(def ^:private revalidate-staged-bundle! commit/revalidate-staged-bundle!)
(def ^:private publisher-evidence-valid? verification/publisher-evidence-valid?)
(def ^:private open-published-directory! verification/open-published-directory!)
(def ^:private ->PublicationContext context/publication-context)
(def ^:dynamic ^:private *operation-checkpoint* contract/*operation-checkpoint*)
(def ^:private captured-call native-call/captured-call)
(def ^:private int-call-result native-call/int-call-result)
(def ^:private long-call-result native-call/long-call-result)

(defn- with-interposed-operations [thunk]
  (binding [native-call/captured-call captured-call
            native-call/int-call-result int-call-result
            native-call/long-call-result long-call-result
            contract/*operation-checkpoint* *operation-checkpoint*]
    (thunk)))

(defn open-target! [output-directory]
  (with-interposed-operations #(target/open-target! output-directory)))

(defn stage-bundle! [target file-specs]
  (with-interposed-operations #(stage/stage-bundle! target file-specs)))

(defn commit-staged-bundle! [staged success-value]
  (with-interposed-operations
   #(commit/commit-staged-bundle! staged success-value)))

(defn verify-published-bundle! [receipt file-specs]
  (with-interposed-operations
   #(verification/verify-published-bundle! receipt file-specs)))

(defn abort-staged-bundle! [target-or-staged]
  (with-interposed-operations
   #(cleanup/abort-staged-bundle! target-or-staged)))
