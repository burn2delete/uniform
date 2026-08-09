(ns gravity.c2-pass-cache-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.c2-pass-cache :as cache])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path StandardOpenOption]
           [java.nio.file.attribute PosixFilePermissions]
           [java.util.concurrent CountDownLatch TimeUnit]))

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defmacro with-temporary-directory
  [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-c2-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
     (try
       ~@body
       (finally
         (delete-tree! ~binding)))))

(defn- hash-id
  [value]
  (cache/canonical-content-id value))

(defn- with-different-owner
  [^java.nio.file.attribute.PosixFileAttributes attributes]
  (let [basic ^java.nio.file.attribute.BasicFileAttributes attributes
        owner (reify java.nio.file.attribute.UserPrincipal
                (getName [_] "different-cache-test-owner"))]
    (reify java.nio.file.attribute.PosixFileAttributes
      (owner [_] owner)
      (group [_] (.group attributes))
      (permissions [_] (.permissions attributes))
      (lastModifiedTime [_] (.lastModifiedTime basic))
      (lastAccessTime [_] (.lastAccessTime basic))
      (creationTime [_] (.creationTime basic))
      (isRegularFile [_] (.isRegularFile basic))
      (isDirectory [_] (.isDirectory basic))
      (isSymbolicLink [_] (.isSymbolicLink basic))
      (isOther [_] (.isOther basic))
      (size [_] (.size basic))
      (fileKey [_] (.fileKey basic)))))

(defn- key-request
  [snapshot]
  (let [bytes-hash (:bytes-hash snapshot)
        options {:retain-comments true
                 :enabled-features #{:standard-reader}
                 :extension-policy (hash-id :standard-reader-policy)}
        project-root-id (hash-id :project-root)
        relative-path "src/cache-fixture.gravity"
        identity-inputs {:project-root-id project-root-id
                         :project-relative-path relative-path
                         :encoding :utf-8
                         :bytes-hash bytes-hash
                         :reader-options options
                         :enabled-features (:enabled-features options)
                         :extension-policy (:extension-policy options)}]
    {:source-unit
     {:source-id (hash-id identity-inputs)
      :bytes-hash bytes-hash
      :reader-options options
      :identity-inputs identity-inputs}
     :source-snapshot (select-keys snapshot [:bytes-hash :byte-count])
     :reader-policy {:reader-options options
                     :extension-policy (:extension-policy options)}
     :project-binding {:project-root-id project-root-id
                       :project-relative-path relative-path}
     :compiler-binding {:compiler-id (hash-id :compiler-v1)
                        :sh03-binding-id (hash-id :sh03-v1)
                        :adapter-contract
                        :gravity/sh03-to-c2-reader-products-v2}
     :pass-binding {:pass-contract-id (hash-id :c2-pass-v1)
                    :pass :c2-reader}
     :dependency-binding {:identity (hash-id :c2-no-dependencies)
                          :dependencies :not-consumed-at-c2}
     :build-effect-binding {:identity (hash-id :no-build-effects)
                            :effects #{}}
     :capability-binding {:identity (hash-id :no-capabilities)
                          :capabilities #{}}
     :facet-binding {:identity (hash-id (:enabled-features options))
                     :facets (:enabled-features options)}
     :profile-binding {:applicability :not-applicable-at-c2}
     :target-binding {:applicability :not-applicable-at-c2}
     :boundary-binding
     (let [base {:slice :SH-03
                 :owner :gravity-source
                 :adapter-contract :gravity/sh03-to-c2-reader-products-v2
                 :plan-binding-id (hash-id :sh03-plan-v1)
                 :semantic-value-table-contract-id
                 (hash-id :semantic-value-table-contract-v1)
                 :authenticated-envelope-contract-id
                 (hash-id :authenticated-envelope-contract-v1)
                 :target-source-reread? false
                 :uncredited-source-models
                 {:status :not-executed
                  :self-hosting-credit? false
                  :seed-retirement-credit? false
                  :release-credit? false}
                 :clojure-adapter-residual? true
                 :self-hosted? false}]
       (assoc base :identity
              (hash-id {:domain :gravity/c2-pass-cache-boundary-binding-v1
                        :binding base})))
     :path-provenance {:canonical-path (:canonical-path snapshot)}}))

(defn- accepted-artifact
  [payload]
  (let [base {:kind :gravity/stage0-c2-reader-document-artifact
              :status :accepted
              :diagnostics []
              :gravity-reader-boundary
              {:slice :SH-03
               :owner :gravity-source
               :adapter-contract :gravity/sh03-to-c2-reader-products-v2
               :target-source-reread? false
               :clojure-adapter-residual? true
               :self-hosted? false
               :payload payload}
              :value (with-meta (list 'cached payload) {:line 7})}]
    (assoc base :artifact-id
           (hash-id (assoc (dissoc base :value) :payload payload)))))

(defn- validation-ops
  [compute]
  {:current-binding {:compiler-id (hash-id :compiler-v1)
                     :pass-contract-id (hash-id :c2-pass-v1)
                     :sh03-binding-id (hash-id :sh03-v1)}
   :artifact-id-of :artifact-id
   :boundary-projection-id-of
   (fn [artifact]
     (hash-id {:domain :test-boundary-projection-v1
               :boundary (:gravity-reader-boundary artifact)}))
   :validate-artifact!
   (fn [artifact _entry _key]
     (when-not (and (= :gravity/stage0-c2-reader-document-artifact
                       (:kind artifact))
                    (= :accepted (:status artifact))
                    (empty? (:diagnostics artifact))
                    (= (:artifact-id artifact)
                       (hash-id (assoc (dissoc artifact :artifact-id :value)
                                       :payload
                                       (second (:value artifact))))))
       (throw (ex-info "artifact rejected" {:id "C16-STALE"})))
     artifact)
   :compute! compute})

(deftest canonical-semantic-key-is-bounded-type-sensitive-and-path-scoped
  (testing "numeric types and collection order cannot alias"
    (is (not= (hash-id 1) (hash-id 1N)))
    (is (= (hash-id {:a 1 :b 2}) (hash-id {:b 2 :a 1})))
    (is (= (hash-id #{:a :b}) (hash-id #{:b :a}))))
  (testing "metadata, unsupported values, and nonfinite values fail closed"
    (doseq [value [(with-meta 'x {:line 1})
                   (Object.)
                   Double/NaN
                   Double/POSITIVE_INFINITY]]
      (is (= "C16-KEY"
             (:id (ex-data (try
                             (hash-id value)
                             nil
                             (catch clojure.lang.ExceptionInfo error error))))))))
  (with-temporary-directory [directory]
    (let [source-a (.resolve directory "a.gravity")
          source-b (.resolve directory "b.gravity")
          bytes (.getBytes "(ns cache.fixture)\n" StandardCharsets/UTF_8)
          _ (Files/write source-a bytes
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          _ (Files/write source-b bytes
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot-a (cache/bounded-source-snapshot! source-a)
          snapshot-b (cache/bounded-source-snapshot! source-b)
          request-a (key-request snapshot-a)
          request-b (assoc (key-request snapshot-b)
                           :source-unit (:source-unit request-a)
                           :source-snapshot (:source-snapshot request-a)
                           :reader-policy (:reader-policy request-a)
                           :project-binding (:project-binding request-a))
          key-a (cache/cache-key request-a)
          key-b (cache/cache-key request-b)]
      (is (= (:semantic-key-id key-a) (:semantic-key-id key-b)))
      (is (not= (:storage-key-id key-a) (:storage-key-id key-b))))))

(deftest bounded-source-snapshot-rejects-size-links-and-traversal
  (with-temporary-directory [directory]
    (let [source (.resolve directory "source.gravity")
          link (.resolve directory "source-link.gravity")
          hard-link (.resolve directory "source-hard.gravity")]
      (Files/write source (.getBytes "12345" StandardCharsets/UTF_8)
                   (into-array OpenOption
                               [StandardOpenOption/CREATE_NEW
                                StandardOpenOption/WRITE]))
      (is (= "C16-KEY"
             (:id (ex-data
                   (try
                     (cache/bounded-source-snapshot! source 4)
                     nil
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/createSymbolicLink link source
                                (make-array java.nio.file.attribute.FileAttribute 0))
      (is (= "C16-KEY"
             (:id (ex-data
                   (try
                     (cache/bounded-source-snapshot! link)
                     nil
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/createLink hard-link source)
      (is (= "C16-KEY"
             (:id (ex-data
                   (try
                     (cache/bounded-source-snapshot! source)
                     nil
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/delete hard-link)
      (let [secure-child-var
            (ns-resolve 'gravity.c2-pass-cache 'secure-child-attributes)
            original-secure-child (var-get secure-child-var)
            calls (atom 0)
            owner-change
            (try
              (with-redefs-fn
                {secure-child-var
                 (fn [directory relative]
                   (let [attributes
                         (original-secure-child directory relative)]
                     (if (= 2 (swap! calls inc))
                       (update attributes :posix with-different-owner)
                       attributes)))}
                #(cache/bounded-source-snapshot! source))
              nil
              (catch clojure.lang.ExceptionInfo error error))]
        (is (= "C16-KEY" (:id (ex-data owner-change)))))
      (is (= "C16-POLICY"
             (:id (ex-data
                   (try
                     (cache/open-local-store (.resolve directory "../escape"))
                     nil
                     (catch clojure.lang.ExceptionInfo error error)))))))))

(comment
  "Frozen legacy-v1 filesystem fixtures.  The compatibility API no longer
  executes these helpers; v1 bytes remain forensic-only and are never read or
  reinterpreted by the generic-v2 adapter."

(deftest persistent-store-misses-stores-and-hits-without-reexecution
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          calls (atom 0)
          artifact (accepted-artifact :first-run)
          first-store (cache/open-local-store directory)
          first-result
          (cache/lookup-or-compute!
           first-store key
           (validation-ops #(do (swap! calls inc) artifact)))
          fresh-store-instance (cache/open-local-store directory)
          second-result
          (cache/lookup-or-compute!
           fresh-store-instance key
           (validation-ops
            #(throw (ex-info "reader must not execute on hit"
                             {:id "CACHE-TRIPWIRE"}))))]
      (is (= 1 @calls))
      (is (= :stored (get-in first-result [:cache-evidence :status])))
      (is (true? (get-in first-result
                         [:cache-evidence :reader-executed?])))
      (is (= :hit (get-in second-result [:cache-evidence :status])))
      (is (false? (get-in second-result
                          [:cache-evidence :reader-executed?])))
      (is (true? (get-in second-result
                         [:cache-evidence :artifact-reused?])))
      (is (= artifact (:artifact second-result)))
      (is (= {:line 7} (meta (:value (:artifact second-result)))))
      (is (= 1 (get-in first-result
                       [:cache-evidence :post-publication-inventory
                        :entries :count])))
      (is (= 0 (get-in first-result
                       [:cache-evidence :post-publication-inventory
                        :staging :count])))
      (let [global-lock-var
            (ns-resolve 'gravity.c2-pass-cache 'with-global-store-lock)
            inventory-var
            (ns-resolve 'gravity.c2-pass-cache 'secure-store-inventory!)
            warmed-hit
            (with-redefs-fn
              {global-lock-var
               (fn [& _]
                 (throw (ex-info "warmed hit entered global admission"
                                 {:id "CACHE-GLOBAL-TRIPWIRE"})))
               inventory-var
               (fn [& _]
                 (throw (ex-info "warmed hit scanned full inventory"
                                 {:id "CACHE-INVENTORY-TRIPWIRE"})))}
              (fn []
                (cache/lookup!
                 fresh-store-instance key
                 (validation-ops (constantly nil)))))]
        (is (= :hit (get-in warmed-hit [:cache-evidence :status]))))
      (let [stale-boundary
            (cache/lookup!
             fresh-store-instance key
             (assoc (validation-ops (constantly nil))
                    :boundary-projection-id-of
                    (constantly (hash-id :different-boundary))))]
        (is (= :rejected
               (get-in stale-boundary [:cache-evidence :status])))
        (is (= "C16-STALE"
               (get-in stale-boundary
                       [:cache-evidence :contained-diagnostic]))))
      (doseq [path [(:root first-store) (:blobs first-store)
                    (:entries first-store) (:locks first-store)
                    (:staging first-store)]]
        (is (= "rwx------"
               (PosixFilePermissions/toString
                (Files/getPosixFilePermissions
                 path (make-array LinkOption 0))))))
      (with-open [paths (Files/walk (:root first-store)
                                    (make-array java.nio.file.FileVisitOption 0))]
        (doseq [path (filter #(Files/isRegularFile
                              ^Path % (make-array LinkOption 0))
                             (vec (.toArray paths)))]
          (is (= "rw-------"
                 (PosixFilePermissions/toString
                  (Files/getPosixFilePermissions
                   ^Path path (make-array LinkOption 0))))))))))

(deftest rejected-computations-and-corrupt-entries-are-never-published-or-reused
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          store (cache/open-local-store directory)
          rejection (try
                      (cache/lookup-or-compute!
                       store key
                       (validation-ops
                        #(throw (ex-info "reader rejection" {:id "C2-MAP"}))))
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
      (is (= "C2-MAP" (:id (ex-data rejection))))
      (is (empty? (filter #(str/ends-with? (str %) ".edn")
                          (file-seq (.toFile ^Path (:entries store))))))
      (cache/lookup-or-compute! store key
                                (validation-ops
                                 #(accepted-artifact :valid)))
      (let [entry (first (filter #(str/ends-with? (str %) ".edn")
                                 (file-seq (.toFile ^Path (:entries store)))))
            entry-path (.toPath entry)
            valid-text
            (String. (Files/readAllBytes entry-path)
                     StandardCharsets/UTF_8)
            unknown-schema-text
            (str/replace-first
             valid-text
             "[[:keyword nil \"schema-version\"] [:integer \"long\" \"1\"]]"
             "[[:keyword nil \"schema-version\"] [:integer \"long\" \"99\"]]")]
        (is (not= valid-text unknown-schema-text))
        (Files/write entry-path (.getBytes unknown-schema-text
                                           StandardCharsets/UTF_8)
                     (into-array OpenOption
                                 [StandardOpenOption/TRUNCATE_EXISTING
                                  StandardOpenOption/WRITE]))
        (let [unknown (cache/lookup! store key
                                     (validation-ops
                                      #(accepted-artifact :unused)))]
          (is (= :rejected (get-in unknown [:cache-evidence :status])))
          (is (= "C16-ENTRY"
                 (get-in unknown
                         [:cache-evidence :contained-diagnostic]))))
        (Files/write (.toPath entry) (.getBytes "not-canonical"
                                                StandardCharsets/UTF_8)
                     (into-array OpenOption
                                 [StandardOpenOption/TRUNCATE_EXISTING
                                  StandardOpenOption/WRITE]))
        (let [lookup (cache/lookup! store key
                                    (validation-ops
                                     #(accepted-artifact :unused)))
              repaired (cache/lookup-or-compute!
                        store key
                        (validation-ops
                         #(accepted-artifact :fresh-not-published)))]
          (is (= :rejected (get-in lookup [:cache-evidence :status])))
          (is (nil? (:artifact lookup)))
          (is (= :miss (get-in repaired [:cache-evidence :status])))
          (is (= :withheld
                 (get-in repaired [:cache-evidence :cache-publication])))
          (is (= :fresh-not-published
                 (second (:value (:artifact repaired))))))))))

(deftest concurrent-identical-writers-converge-and-conflicts-fail-closed
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          store-a (cache/open-local-store directory)
          store-b (cache/open-local-store directory)
          calls (atom 0)
          artifact (accepted-artifact :concurrent)
          run #(cache/lookup-or-compute!
                % key
                (validation-ops
                 (fn []
                   (swap! calls inc)
                   artifact)))
          first (future (run store-a))
          second (future (run store-b))
          results [@first @second]]
      (is (= 1 @calls))
      (is (= #{:stored :hit}
             (set (map #(get-in % [:cache-evidence :status]) results))))
      (let [conflict (try
                       (cache/store! store-a key
                                     (accepted-artifact :different)
                                     (validation-ops (constantly nil)))
                       nil
                       (catch clojure.lang.ExceptionInfo error error))]
        (is (= "C16-ENTRY" (:id (ex-data conflict)))))
      (is (empty?
           @(var-get
             (ns-resolve 'gravity.c2-pass-cache
                         'in-process-key-locks)))))))

(deftest different-key-misses-compute-concurrently
  (with-temporary-directory [directory]
    (let [source-a (.resolve directory "a.gravity")
          source-b (.resolve directory "b.gravity")
          _ (Files/write source-a (.getBytes "(ns cache.a)\n"
                                             StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          _ (Files/write source-b (.getBytes "(ns cache.b)\n"
                                             StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key-a (cache/cache-key
                 (key-request (cache/bounded-source-snapshot! source-a)))
          key-b (cache/cache-key
                 (key-request (cache/bounded-source-snapshot! source-b)))
          store-a (cache/open-local-store directory)
          store-b (cache/open-local-store directory)
          started-a (promise)
          started-b (promise)
          compute
          (fn [mine peer payload]
            (fn []
              (deliver mine true)
              (when (= ::timeout (deref peer 2000 ::timeout))
                (throw (ex-info "different-key compute was serialized"
                                {:id "CACHE-CONCURRENCY"})))
              (accepted-artifact payload)))
          result-a
          (future
            (cache/lookup-or-compute!
             store-a key-a
             (validation-ops (compute started-a started-b :a))))
          result-b
          (future
            (cache/lookup-or-compute!
             store-b key-b
             (validation-ops (compute started-b started-a :b))))]
      (is (= :stored (get-in @result-a [:cache-evidence :status])))
      (is (= :stored (get-in @result-b [:cache-evidence :status])))
      (is (realized? started-a))
      (is (realized? started-b)))))

(deftest filesystem-and-decoder-adversaries-fail-closed
  (testing "an unsafe base permission policy is rejected before store creation"
    (with-temporary-directory [directory]
      (Files/setPosixFilePermissions
       directory (PosixFilePermissions/fromString "rwxrwxrwx"))
      (is (= "C16-POLICY"
             (:id
              (ex-data
               (try
                 (cache/open-local-store directory)
                 nil
                 (catch clojure.lang.ExceptionInfo error error))))))))
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          store (cache/open-local-store directory)
          artifact (accepted-artifact :adversarial)
          ops (validation-ops (constantly artifact))]
      (cache/lookup-or-compute! store key ops)
      (testing "a replaced entries parent is caught by its pinned identity"
        (let [entries (:entries store)
              saved (.resolve ^Path (:root store) "entries-saved")
              attacker (.resolve directory "attacker-entries")]
          (Files/createDirectory
           attacker
           (into-array
            java.nio.file.attribute.FileAttribute
            [(PosixFilePermissions/asFileAttribute
              (PosixFilePermissions/fromString "rwx------"))]))
          (Files/move entries saved (make-array java.nio.file.CopyOption 0))
          (Files/createSymbolicLink
           entries attacker
           (make-array java.nio.file.attribute.FileAttribute 0))
          (try
            (is (= "C16-POLICY"
                   (:id
                    (ex-data
                     (try
                       (cache/lookup! store key ops)
                       nil
                       (catch clojure.lang.ExceptionInfo error error))))))
            (finally
              (Files/delete entries)
              (Files/move saved entries
                          (make-array java.nio.file.CopyOption 0))))))
      (testing "deep canonical-looking EDN is rejected before re-rendering"
        (let [entry (first (filter #(str/ends-with? (str %) ".edn")
                                   (file-seq
                                    (.toFile ^Path (:entries store)))))
              nested (reduce (fn [inner _]
                               (str "[:vector [" inner "]]"))
                             "[:nil]" (range 900))]
          (Files/write (.toPath entry)
                       (.getBytes nested StandardCharsets/UTF_8)
                       (into-array OpenOption
                                   [StandardOpenOption/TRUNCATE_EXISTING
                                    StandardOpenOption/WRITE]))
          (let [result (cache/lookup! store key ops)]
            (is (= :rejected (get-in result [:cache-evidence :status])))
            (is (= "C16-ENTRY"
                   (get-in result
                           [:cache-evidence :contained-diagnostic])))))))))

(deftest bootstrap-parent-swap-cannot-redirect-cache-directory-publication
  (with-temporary-directory [directory]
    (let [parent (.getParent directory)
          saved (.resolve parent
                          (str "saved-" (java.util.UUID/randomUUID)))
          attacker (Files/createTempDirectory
                    "gravity-c2-cache-attacker-"
                    (make-array java.nio.file.attribute.FileAttribute 0))
          move-var
          (ns-resolve 'gravity.c2-pass-cache 'secure-directory-move!)
          original-move (var-get move-var)
          swapped? (atom false)]
      (try
        (let [error
              (try
                (with-redefs-fn
                  {move-var
                   (fn [& arguments]
                     (when (compare-and-set! swapped? false true)
                       (Files/move directory saved
                                   (make-array java.nio.file.CopyOption 0))
                       (Files/createSymbolicLink
                        directory attacker
                        (make-array java.nio.file.attribute.FileAttribute 0)))
                     (apply original-move arguments))}
                  #(cache/open-local-store directory))
                nil
                (catch clojure.lang.ExceptionInfo error error))]
          (is (= "C16-POLICY" (:id (ex-data error))))
          (is (false? (Files/exists
                       (.resolve attacker ".cpcache")
                       (make-array LinkOption 0))))
          (is (true? (Files/exists
                      (.resolve saved ".cpcache")
                      (make-array LinkOption 0)))))
        (finally
          (when (Files/isSymbolicLink directory)
            (Files/delete directory))
          (when (Files/exists saved (make-array LinkOption 0))
            (Files/move saved directory
                        (make-array java.nio.file.CopyOption 0)))
          (delete-tree! attacker))))))

(deftest bounded-store-policy-rejects-unrecognized-files
  (with-temporary-directory [directory]
    (let [store (cache/open-local-store directory)
          unexpected (.resolve ^Path (:entries store) "unexpected")]
      (Files/write unexpected (byte-array 0)
                   (into-array OpenOption
                               [StandardOpenOption/CREATE_NEW
                                StandardOpenOption/WRITE]))
      (Files/setPosixFilePermissions
       unexpected (PosixFilePermissions/fromString "rw-------"))
      (let [source (.resolve directory "cache.gravity")
            _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                             StandardCharsets/UTF_8)
                           (into-array OpenOption
                                       [StandardOpenOption/CREATE_NEW
                                        StandardOpenOption/WRITE]))
            snapshot (cache/bounded-source-snapshot! source)
            key (cache/cache-key (key-request snapshot))
            error (try
                    (cache/lookup! store key
                                   (validation-ops (constantly nil)))
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "C16-POLICY" (:id (ex-data error))))
        (is (= {:maximum-entry-count 4096
                :maximum-blob-count 4096
                :maximum-lock-count 4097
                :maximum-staging-count 1
                :maximum-aggregate-bytes (* 256 1024 1024)}
               (:store-policy store)))))))

(deftest durable-directory-bootstrap-and-blob-first-publication-order
  (with-temporary-directory [directory]
    (let [directory-forces (atom [])
          secure-force-var
          (ns-resolve 'gravity.c2-pass-cache 'secure-fsync-directory!)
          original-secure-force (var-get secure-force-var)
          secure-self
          (var-get (ns-resolve 'gravity.c2-pass-cache
                               'secure-self-attributes))
          store
          (with-redefs-fn
            {secure-force-var
             (fn [secure-directory]
               (swap! directory-forces conj
                      (.fileKey (:basic
                                 (secure-self secure-directory))))
               (original-secure-force secure-directory))}
            #(cache/open-local-store directory))]
      (let [path-file-key
            (fn [path]
              (.fileKey
               (Files/readAttributes
                ^Path path java.nio.file.attribute.BasicFileAttributes
                (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))))
            label-by-file-key
            {(path-file-key (:base store)) :base
             (path-file-key (:cpcache store)) :cpcache
             (path-file-key (:compiler-pass store)) :compiler-pass
             (path-file-key (:root store)) :root}]
        (is (= [:base :base :cpcache :compiler-pass
                :root :root :root :root]
               (keep label-by-file-key @directory-forces))))
      (let [source (.resolve directory "cache.gravity")
            _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                             StandardCharsets/UTF_8)
                           (into-array OpenOption
                                       [StandardOpenOption/CREATE_NEW
                                        StandardOpenOption/WRITE]))
            snapshot (cache/bounded-source-snapshot! source)
            key (cache/cache-key (key-request snapshot))
            path-file-key
            (fn [path]
              (.fileKey
               (Files/readAttributes
                ^Path path java.nio.file.attribute.BasicFileAttributes
                (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))))
            label-by-file-key
            {(path-file-key (:locks store)) :locks
             (path-file-key (:blobs store)) :blobs
             (path-file-key (:entries store)) :entries
             (path-file-key (:staging store)) :staging}
            secure-forces (atom [])
            result
            (with-redefs-fn
              {secure-force-var
               (fn [secure-directory]
                 (swap! secure-forces conj
                        (get label-by-file-key
                             (.fileKey (:basic
                                        (secure-self secure-directory))))))}
              (fn []
                (cache/lookup-or-compute!
                 store key
                 (validation-ops
                  #(accepted-artifact :durable-publication)))))]
        (is (= :stored (get-in result [:cache-evidence :status])))
        (is (= 4097 (get-in store [:store-policy :maximum-lock-count])))
        (is (= [:locks :locks
                :staging :staging :blobs
                :staging :staging :entries]
               @secure-forces))))))

(deftest staged-publication-failure-cleans-and-crash-residue-recovers
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key (cache/cache-key
               (key-request (cache/bounded-source-snapshot! source)))
          store (cache/open-local-store directory)
          move-var (ns-resolve 'gravity.c2-pass-cache
                               'secure-publish-move!)
          injected
          (try
            (with-redefs-fn
              {move-var
               (fn [& _]
                 (throw (ex-info "precommit failpoint"
                                 {:id "CACHE-PRECOMMIT"})))}
              #(cache/lookup-or-compute!
                store key
                (validation-ops
                 (fn [] (accepted-artifact :precommit)))))
            nil
            (catch clojure.lang.ExceptionInfo error error))]
      (is (= "CACHE-PRECOMMIT" (:id (ex-data injected))))
      (is (empty? (seq (.list (.toFile ^Path (:staging store))))))
      (let [stored
            (cache/lookup-or-compute!
             store key
             (validation-ops
              (fn [] (accepted-artifact :precommit))))
            residue
            (.resolve ^Path (:staging store)
                      ".stage-00000000-0000-0000-0000-000000000000.tmp")]
        (is (= :stored (get-in stored [:cache-evidence :status])))
        (Files/write residue (.getBytes "crash" StandardCharsets/UTF_8)
                     (into-array OpenOption
                                 [StandardOpenOption/CREATE_NEW
                                  StandardOpenOption/WRITE]))
        (Files/setPosixFilePermissions
         residue (PosixFilePermissions/fromString "rw-------"))
        (let [second-source (.resolve directory "second.gravity")
              _ (Files/write second-source
                             (.getBytes "(ns cache.second)\n"
                                        StandardCharsets/UTF_8)
                             (into-array OpenOption
                                         [StandardOpenOption/CREATE_NEW
                                          StandardOpenOption/WRITE]))
              second-key
              (cache/cache-key
               (key-request
                (cache/bounded-source-snapshot! second-source)))]
          (is (= :miss
                 (get-in (cache/lookup!
                          store second-key
                          (validation-ops (constantly nil)))
                         [:cache-evidence :status]))))
        (is (= :hit
               (get-in (cache/lookup!
                        store key (validation-ops (constantly nil)))
                       [:cache-evidence :status])))
        (is (false? (Files/exists residue (make-array LinkOption 0))))))))

(deftest fatal-validation-errors-propagate-and-release-local-locks
  (with-temporary-directory [directory]
    (let [source (.resolve directory "cache.gravity")
          _ (Files/write source (.getBytes "(ns cache.fixture)\n"
                                           StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          store (cache/open-local-store directory)
          artifact (accepted-artifact :fatal-tripwire)
          ops (validation-ops (constantly artifact))]
      (cache/lookup-or-compute! store key ops)
      (is (thrown? ThreadDeath
                   (cache/lookup!
                    store key
                    (assoc ops :validate-artifact!
                           (fn [& _] (throw (ThreadDeath.)))))))
      (is (= :hit (get-in (cache/lookup! store key ops)
                          [:cache-evidence :status])))
      (is (empty?
           @(var-get
             (ns-resolve 'gravity.c2-pass-cache
                         'in-process-key-locks)))))))

)

(deftest generic-v2-adapter-preserves-c2-results-and-ignores-v1
  (with-temporary-directory [directory]
    (let [source (.resolve directory "adapter.gravity")
          _ (Files/write source (.getBytes "(ns adapter)\n"
                                                StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          snapshot (cache/bounded-source-snapshot! source)
          key (cache/cache-key (key-request snapshot))
          artifact (accepted-artifact :adapter)
          calls (atom 0)
          ops (validation-ops #(do (swap! calls inc) artifact))
          v1 (.resolve directory ".cpcache/compiler-pass/v1")
          _ (Files/createDirectories
             v1 (make-array java.nio.file.attribute.FileAttribute 0))
          sentinel (.resolve v1 "forensic-only.bin")
          sentinel-bytes (.getBytes "legacy-v1-must-not-be-read"
                                    StandardCharsets/UTF_8)
          _ (Files/write sentinel sentinel-bytes
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          store (cache/open-local-store directory)
          miss (cache/lookup! store key ops)
          stored (cache/lookup-or-compute! store key ops)
          hit (cache/lookup-or-compute!
               store key (assoc ops :compute!
                                #(throw (ex-info "producer reran" {}))))]
      (is (= ".cpcache/compiler-pass/v2"
             (:storage-root (cache/cache-contract))))
      (is (= :miss (get-in miss [:cache-evidence :status])))
      (is (= :stored (get-in stored [:cache-evidence :status])))
      (is (= :hit (get-in hit [:cache-evidence :status])))
      (is (= 1 @calls))
      (is (= artifact (:artifact stored) (:artifact hit)))
      (is (= {:line 7} (meta (:value (:artifact hit)))))
      (is (= :gravity/c2-local-pass-cache-evidence
             (get-in hit [:cache-evidence :artifact])))
      (is (false? (get-in hit [:cache-evidence :reader-executed?])))
      (is (true? (get-in hit [:cache-evidence :artifact-reused?])))
      (is (java.util.Arrays/equals
           sentinel-bytes (Files/readAllBytes sentinel)))
      (is (Files/isDirectory (.resolve directory
                                       ".cpcache/compiler-pass/v2")
                             (make-array LinkOption 0))))))

(deftest generic-v2-adapter-binds-current-c2-producer-policy
  (with-temporary-directory [directory]
    (let [source (.resolve directory "binding.gravity")
          _ (Files/write source (.getBytes "(ns binding)\n"
                                                StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key (cache/cache-key
               (key-request (cache/bounded-source-snapshot! source)))
          artifact-a (accepted-artifact :binding-a)
          artifact-b (accepted-artifact :binding-b)
          calls (atom [])
          base-ops (validation-ops #(do (swap! calls conj :a) artifact-a))
          changed-ops
          (assoc (validation-ops #(do (swap! calls conj :b) artifact-b))
                 :current-binding
                 (assoc (:current-binding base-ops)
                        :compiler-id (hash-id :compiler-v2)))
          store (cache/open-local-store directory)
          first-result (cache/lookup-or-compute! store key base-ops)
          changed-result (cache/lookup-or-compute! store key changed-ops)
          warm-result (cache/lookup-or-compute!
                       store key
                       (assoc changed-ops :compute!
                              #(throw (ex-info "changed producer reran" {}))))]
      (is (= [:a :b] @calls))
      (is (= :stored (get-in first-result [:cache-evidence :status])))
      (is (= :stored (get-in changed-result [:cache-evidence :status])))
      (is (= :hit (get-in warm-result [:cache-evidence :status])))
      (is (= artifact-b (:artifact warm-result)))
      (is (not= (get-in first-result [:cache-evidence :artifact-id])
                (get-in changed-result [:cache-evidence :artifact-id])))
      (is (not= (get-in first-result [:cache-evidence :entry-id])
                (get-in changed-result [:cache-evidence :entry-id]))))))

(deftest generic-v2-adapter-corruption-is-rejected-and-not-replaced
  (with-temporary-directory [directory]
    (let [source (.resolve directory "corrupt.gravity")
          _ (Files/write source (.getBytes "(ns corrupt)\n"
                                                StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key (cache/cache-key
               (key-request (cache/bounded-source-snapshot! source)))
          artifact (accepted-artifact :corrupt)
          calls (atom 0)
          ops (validation-ops #(do (swap! calls inc) artifact))
          store (cache/open-local-store directory)
          _ (cache/lookup-or-compute! store key ops)
          entry-path
          (with-open [entries (Files/list (:entries store))]
            (first (vec (.toArray entries))))
          corrupt-bytes (.getBytes "[:corrupt]" StandardCharsets/UTF_8)
          _ (Files/write ^Path entry-path corrupt-bytes
                         (into-array OpenOption
                                     [StandardOpenOption/TRUNCATE_EXISTING
                                      StandardOpenOption/WRITE]))
          rejected (cache/lookup! store key ops)
          withheld (cache/lookup-or-compute! store key ops)]
      (is (= :rejected (get-in rejected [:cache-evidence :status])))
      (is (= :miss (get-in withheld [:cache-evidence :status])))
      (is (= :withheld
             (get-in withheld [:cache-evidence :cache-publication])))
      (is (= artifact (:artifact withheld)))
      (is (= 2 @calls))
      (is (java.util.Arrays/equals
           corrupt-bytes (Files/readAllBytes ^Path entry-path))))))

(deftest generic-v2-adapter-direct-store-mints-a-current-receipt
  (with-temporary-directory [directory]
    (let [source (.resolve directory "store.gravity")
          _ (Files/write source (.getBytes "(ns store)\n"
                                                StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key (cache/cache-key
               (key-request (cache/bounded-source-snapshot! source)))
          artifact (accepted-artifact :direct-store)
          ops (validation-ops (constantly artifact))
          store (cache/open-local-store directory)
          stored (cache/store! store key artifact ops)
          hit (cache/lookup! store key ops)]
      (is (= :stored (get-in stored [:cache-evidence :status])))
      (is (= :published
             (get-in stored [:cache-evidence :blob-publication])))
      (is (= :published
             (get-in stored [:cache-evidence :entry-publication])))
      (is (= :hit (get-in hit [:cache-evidence :status])))
      (is (= artifact (:artifact hit)))
      (is (string? (get-in stored [:cache-evidence :entry-id])))
      (is (false? (get-in stored [:cache-evidence :release-authority?])))
      (is (false? (get-in stored [:cache-evidence :proof-authority?]))))))

(deftest generic-v2-adapter-same-key-concurrency-executes-one-c2-producer
  (with-temporary-directory [directory]
    (let [source (.resolve directory "concurrent.gravity")
          _ (Files/write source (.getBytes "(ns concurrent)\n"
                                                StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          key (cache/cache-key
               (key-request (cache/bounded-source-snapshot! source)))
          artifact (accepted-artifact :concurrent)
          calls (atom 0)
          ready (CountDownLatch. 2)
          start (CountDownLatch. 1)
          store (cache/open-local-store directory)
          task (fn []
                 (.countDown ready)
                 (.await start 10 TimeUnit/SECONDS)
                 (cache/lookup-or-compute!
                  store key
                  (validation-ops
                   #(do (swap! calls inc)
                        (Thread/sleep 100)
                        artifact))))
          left (future (task))
          right (future (task))]
      (is (.await ready 10 TimeUnit/SECONDS))
      (.countDown start)
      (let [results [(deref left 30000 ::timeout)
                     (deref right 30000 ::timeout)]]
        (is (not-any? #{::timeout} results))
        (is (= [:hit :stored]
               (sort (map #(get-in % [:cache-evidence :status]) results))))
        (is (= 1 @calls))
        (is (every? #(= artifact (:artifact %)) results))))))

(deftest generic-v2-adapter-preserves-opaque-c2-size-and-depth-profile
  (with-temporary-directory [directory]
    (let [large-source (.resolve directory "large.gravity")
          deep-source (.resolve directory "deep.gravity")
          _ (Files/write large-source (.getBytes "(ns large)\n"
                                                      StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          _ (Files/write deep-source (.getBytes "(ns deep)\n"
                                                     StandardCharsets/UTF_8)
                         (into-array OpenOption
                                     [StandardOpenOption/CREATE_NEW
                                      StandardOpenOption/WRITE]))
          large-key
          (cache/cache-key
           (key-request (cache/bounded-source-snapshot! large-source)))
          deep-key
          (cache/cache-key
           (key-request (cache/bounded-source-snapshot! deep-source)))
          large-payload (byte-array (* 10 1024 1024) (byte 37))
          deep-payload (nth (iterate #(list %) :leaf) 120)
          large-artifact (accepted-artifact large-payload)
          deep-artifact (accepted-artifact deep-payload)
          store (cache/open-local-store directory)
          _ (cache/lookup-or-compute!
             store large-key (validation-ops (constantly large-artifact)))
          _ (cache/lookup-or-compute!
             store deep-key (validation-ops (constantly deep-artifact)))
          large-hit
          (cache/lookup!
           store large-key (validation-ops (constantly large-artifact)))
          deep-hit
          (cache/lookup!
           store deep-key (validation-ops (constantly deep-artifact)))
          observed-large (second (:value (:artifact large-hit)))
          observed-deep (second (:value (:artifact deep-hit)))]
      (is (= :hit (get-in large-hit [:cache-evidence :status])))
      (is (= :hit (get-in deep-hit [:cache-evidence :status])))
      (is (= (:artifact-id large-artifact)
             (get-in large-hit [:artifact :artifact-id])))
      (is (= (:artifact-id deep-artifact)
             (get-in deep-hit [:artifact :artifact-id])))
      (is (java.util.Arrays/equals large-payload observed-large))
      (is (= deep-payload observed-deep))
      (is (= {:line 7} (meta (:value (:artifact large-hit)))))
      (is (= {:line 7} (meta (:value (:artifact deep-hit))))))))

(deftest leaf-contract-is-explicitly-local-and-nonauthoritative
  (let [contract (cache/cache-contract)]
    (is (= :hosted-c2-local-pass-cache-v2-adapter
           (:contract-boundary contract)))
    (is (= ".cpcache/compiler-pass/v2" (:storage-root contract)))
    (is (= ".cpcache/compiler-pass/v1" (:legacy-storage-root contract)))
    (is (= :forensic-only-never-read-or-reinterpreted
           (:legacy-storage-policy contract)))
    (is (= {:encoding :opaque-c2-canonical-envelope
            :metadata-preserving? true
            :maximum-c2-canonical-bytes (* 32 1024 1024)
            :maximum-v2-envelope-bytes (* 48 1024 1024)}
           (:adapter-artifact-policy contract)))
    (is (true? (get-in contract [:authority :local-development-only?])))
    (is (false? (get-in contract [:authority :release-authority?])))
    (doseq [nonclaim [:c2-reader-authority :c16-language-conformance
                      :reader-execution :artifact-validation
                      :release-publication :proof :equivalence
                      :self-hosting
                      :same-user-out-of-band-mutation-safety]]
      (is (some #{nonclaim} (:does-not-own contract))))
    (is (false?
         (get-in contract
                 [:threat-boundary
                  :same-user-out-of-band-mutation-safety
                  :protected?])))
    (is (false?
         (get-in contract
                 [:threat-boundary
                  :same-user-out-of-band-mutation-safety
                  :no-replace-guarantee?]))))
  (testing "the Clojure CLI .cpcache parent remains shared and unchanged"
    (with-temporary-directory [directory]
      (let [shared (.resolve directory ".cpcache")]
        (Files/createDirectory
         shared
         (into-array
          java.nio.file.attribute.FileAttribute
          [(PosixFilePermissions/asFileAttribute
            (PosixFilePermissions/fromString "rwxr-xr-x"))]))
        (let [store (cache/open-local-store directory)]
          (is (= "rwxr-xr-x"
                 (PosixFilePermissions/toString
                  (Files/getPosixFilePermissions
                   shared (make-array LinkOption 0)))))
          (is (= "rwx------"
                 (PosixFilePermissions/toString
                  (Files/getPosixFilePermissions
                   (:root store) (make-array LinkOption 0))))))))))

(deftest opt-in-bootstrap-integration-reuses-without-reader-execution
  ;; Resolve bootstrap only for this hosted smoke.  The focused leaf tests above
  ;; remain bootstrap-free and are run as their own bounded lane.
  (with-temporary-directory [directory]
    (let [source "bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity"
          cached-var
          (requiring-resolve
           'gravity.bootstrap/compiler-c2-reader-file-artifact-cached)
          uncached-var
          (requiring-resolve
           'gravity.bootstrap/compiler-c2-reader-file-artifact)
          first-result (cached-var source (str directory))
          first-artifact (:c2-reader-artifact first-result)
          second-result
          (with-redefs-fn
            {uncached-var
             (fn [& _]
               (throw
                (ex-info "C2 reader executed on a validated cache hit"
                         {:id "CACHE-READER-TRIPWIRE"})))}
            #(cached-var source (str directory)))]
      (is (= :stored (get-in first-result [:cache-evidence :status])))
      (is (true? (get-in first-result
                         [:cache-evidence :reader-executed?])))
      (is (= :hit (get-in second-result [:cache-evidence :status])))
      (is (false? (get-in second-result
                          [:cache-evidence :reader-executed?])))
      (is (= (:artifact-id first-artifact)
             (get-in second-result [:c2-reader-artifact :artifact-id])))
      (is (true? (:clojure-adapter-residual? second-result)))
      (is (false? (:self-hosted? second-result)))
      (is (false? (:release-authority? second-result))))))
