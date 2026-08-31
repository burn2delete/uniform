;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- id-filename
  [identity]
  (ensure-sha256-id! :storage-path-id identity)
  (str (subs identity (count "sha256:")) ".edn"))

(defn- identity-for-path
  [store path-key]
  (let [path (get store path-key)]
    (or (some #(when (= path (:path %)) %) (:directory-identities store))
        (cache-fail! "C16-POLICY" "store directory identity is missing"
                     {:path-key path-key}))))

(defn- open-secure-child!
  [^SecureDirectoryStream parent child-name identity]
  (let [raw-child (.newDirectoryStream parent (relative-name child-name)
                                       nofollow-links)
        child (require-secure-directory-stream!
               raw-child :open-secure-store-child)]
    (try
      (verify-secure-directory-handle! child identity)
      child
      (catch Throwable error
        (.close ^DirectoryStream raw-child)
        (throw error)))))

(defn- with-secure-store-directories
  [store operation]
  (with-contained-host-errors
   "C16-POLICY" :secure-store-traversal
   (fn []
     (verify-store-identity! store)
     (with-open [raw-base (Files/newDirectoryStream ^Path (:base store))]
       (let [base (require-secure-directory-stream!
                   raw-base :open-secure-store-base)]
         (verify-secure-directory-handle! base (identity-for-path store :base))
         (with-open [cpcache
                     (open-secure-child!
                      base ".cpcache" (identity-for-path store :cpcache))]
           (with-open [compiler-pass
                       (open-secure-child!
                        cpcache "compiler-pass"
                        (identity-for-path store :compiler-pass))]
             (with-open [root
                         (open-secure-child!
                          compiler-pass "v1"
                          (identity-for-path store :root))]
               (with-open [blobs
                           (open-secure-child!
                            root "blobs" (identity-for-path store :blobs))
                           entries
                           (open-secure-child!
                            root "entries" (identity-for-path store :entries))
                           locks
                           (open-secure-child!
                            root "locks" (identity-for-path store :locks))
                           staging
                           (open-secure-child!
                            root "staging"
                            (identity-for-path store :staging))]
                 (let [directories {:blobs blobs
                                    :entries entries
                                    :locks locks
                                    :staging staging}
                       result (operation directories)]
                   (doseq [[path-key directory] directories]
                     (verify-secure-directory-handle!
                      directory (identity-for-path store path-key)))
                   result))))))))))
