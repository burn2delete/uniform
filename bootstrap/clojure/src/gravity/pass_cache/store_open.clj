(ns gravity.pass-cache.store-open
  "Secure local v2 store bootstrap and startup recovery."
  (:require [gravity.pass-cache.locking :refer :all]
            [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.recovery :refer :all]
            [gravity.pass-cache.secure-io :refer :all]
            [gravity.pass-cache.store-directories :refer :all])
  (:import [java.nio.file Files Path]
           [java.nio.file SecureDirectoryStream]))

(defn open-local-store
  "Open the explicit local cache below exactly `.cpcache/compiler-pass/v2`."
  [base-path]
  (let [base (absolute-base-path! base-path)]
    (when-not (Files/exists base nofollow-links)
      (fail! "C16-POLICY" "explicit cache base must already exist"
             {:path (str base)}))
    (verify-directory! base false)
    ;; Bootstrap the namespace through a held descriptor.  Path-based checks
    ;; below only materialize retained projections; creation itself is
    ;; anchored and fails closed when SecureDirectoryStream is unavailable.
    (let [base-identity (identity-of base false)]
      (with-open [raw-base (Files/newDirectoryStream base)]
        (let [base-directory (require-secure-directory-stream!
                              raw-base :cache-directory-bootstrap-base)]
          (verify-secure-directory-handle! base-directory base-identity)
          (with-cache-bootstrap-lock
           base base-directory
           (fn []
             (with-open [cpcache (secure-ensure-child-directory!
                                  base-directory ".cpcache" false)
                         compiler-pass (secure-ensure-child-directory!
                                        cpcache "compiler-pass" false)
                         root (secure-ensure-child-directory!
                               compiler-pass "v2" true)]
               (doseq [child ["blobs" "entries" "receipts" "locks" "staging"]]
                 (with-open [owned (secure-ensure-child-directory!
                                    root child true)]
                   (verify-secure-child-directory!
                    root (relative-name! child) true)))))))))
    (let [private-store (create-private-tree! base)
          store (assoc private-store
                       :schema-version schema-version
                       :store-policy store-policy
                       :directory-identities
                       (mapv (fn [[path owned?]] (identity-of path owned?))
                             [[(:base private-store) false]
                              [(:cpcache private-store) false]
                              [(:compiler-pass private-store) false]
                              [(:root private-store) true]
                              [(:blobs private-store) true]
                              [(:entries private-store) true]
                              [(:receipts private-store) true]
                              [(:locks private-store) true]
                              [(:staging private-store) true]]))]
      (with-global-store-lock store
        #(do (clean-staging! store)
             (inventory! store)))
      store)))
