(ns gravity.self-hosting.sh01-development-loop-wiring-test
  (:require [clojure.edn :as edn]
            [clojure.java.basis :as basis]
            [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.self-hosting.sh01-development-test-cache :as cache]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]
            [gravity.self-hosting-test-runner :as child-runner])
  (:import [java.net URL URLClassLoader]
           [java.util.jar Attributes$Name JarEntry JarOutputStream Manifest]
           [java.nio.file Files LinkOption Path StandardCopyOption]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]
           [java.util.concurrent CountDownLatch TimeUnit]))

(def ^:private no-file-attributes (make-array FileAttribute 0))
(def ^:private no-links (make-array LinkOption 0))
(def ^:private snapshot-id
  (str "sha256:" (apply str (repeat 64 "a"))))

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root no-links)
    (with-open [paths (Files/walk root
                                  (make-array
                                   java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defn with-coordination-root
  [operation]
  (let [base (Files/createTempDirectory
              "gravity-development-loop-wiring-"
              no-file-attributes)]
    (try
      (Files/setPosixFilePermissions
       base
       (PosixFilePermissions/fromString "rwx------"))
      (operation base)
      (finally (delete-tree! base)))))

(defn- context
  [^Path base]
  (let [cache-directory (.resolve base "cache")
        broker-root (.resolve base "broker")]
    (Files/createDirectories cache-directory no-file-attributes)
    (Files/createDirectories broker-root no-file-attributes)
    (Files/setPosixFilePermissions
     broker-root
     (PosixFilePermissions/fromString "rwx------"))
    {:schema :gravity/sh01-development-loop-context-v1
     :test-only? true
     :working-directory "/tmp"
     :cache-directory cache-directory
     :broker-root broker-root
     :timeout-ms 1000
     :identity-options {}
     :snapshot
     {:schema :gravity/sh01-complete-repository-snapshot-v1
      :repository-identity snapshot-id
      :path-count 1}
     :snapshot-telemetry
     (atom {:full-snapshot-invocations 0
            :full-snapshot-path-observations 0
            :phases {}})
     :launch-count (atom 0)
     :cache-receipts (atom [])}))

(defn- exception-data
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- reviewed-policy
  ([] (reviewed-policy {}))
  ([overrides]
   (merge {:deterministic? true
           :performance? false
           :proof? false
           :freshness-required? false}
          overrides)))

(defn- impact-plan
  ([namespace] (impact-plan namespace {}))
  ([namespace overrides]
   (merge
    {:schema :gravity/sh01-impact-test-plan-v1
     :authority :non-authoritative
     :authoritative? false
     :namespaces [namespace]
     :shards
     [{:namespace namespace
       :slice "SH-07"
       :resource-class :memory-heavy
       :component-id "wiring-test"
       :batch-key (str "component/wiring-test/" namespace)
       :test-policy (reviewed-policy)}]}
    overrides)))

(defn- execute
  [plan selected-context launcher & [extra-options]]
  (runner/execute-plan
   plan
   (merge
    {:development-loop? true
     :development-loop-context selected-context
     :development-operation-identity
     {:id :wiring-test-launcher :sha256 snapshot-id}
     :working-directory "/tmp"
     :process-launcher launcher}
    extra-options)))

(defn- one-member-batch-result
  [job]
  (let [namespace (:namespace job)
        output {:text "" :bytes 0 :observed-bytes 0
                :limit-bytes 8192 :truncated? false}]
    {:status :passed
     :exit-code 0
     :result
     {:schema :gravity/self-hosting-test-report-v2
      :authority :non-authoritative
      :authoritative? false
      :status :passed
      :exit-code 0
      :namespaces [namespace]
      :namespace-results
      [{:namespace namespace
        :status :passed
        :exit-code 0
        :attempted? true
        :elapsed-ms 1
        :test 1 :pass 1 :fail 0 :error 0
        :summary {:test 1 :pass 1 :fail 0 :error 0}
        :stdout output :stderr output}]
      :skipped-namespaces []
      :fail-fast? false
      :summary {:test 1 :pass 1 :fail 0 :error 0}}}))

(defn- reviewed-closure-job
  [namespace]
  (let [closure
        ((requiring-resolve
          'gravity.self-hosting.sh01-component-test-dependencies/reviewed-cache-closure)
         namespace)]
    {:namespace namespace
     :slice "SH-00"
     :resource-class :normal
     :component-id
     (cond
       (= 'gravity.c2-artifact-identity-test namespace) "c2-artifact-identity"
       (contains? #{'gravity.c11-mir-test
                      'gravity.bootstrap-compatibility.c11-test}
                    namespace)
       "c11-mir"
       :else "compiler-pass-manifest")
     :batch-key (str "closure/" namespace)
     :cache-closure closure
     :cache-closure-authorized? true
     :test-policy (reviewed-policy)}))

(defn- copy-reviewed-closure!
  [^Path target-root job]
  (let [source-root (.toRealPath (.toPath (java.io.File. ".")) no-links)]
    (doseq [{:keys [path]} (get-in job [:cache-closure :inputs])]
      (let [source (.resolve source-root ^String path)
            target (.resolve target-root ^String path)]
        (Files/createDirectories (.getParent target) no-file-attributes)
        (Files/copy source target
                    (into-array StandardCopyOption
                                [StandardCopyOption/REPLACE_EXISTING
                                 StandardCopyOption/COPY_ATTRIBUTES])))))
  target-root)

(defn- closure-cache-request
  [^Path coordination-root ^Path repository-root job repository-id
   & [policy-overrides command authoritative?]]
  (let [selected-context
        (assoc (context coordination-root)
               :working-directory (str repository-root)
               :snapshot {:schema :gravity/sh01-complete-repository-snapshot-v1
                          :repository-identity repository-id
                          :path-count 1}
               :runtime-tool-inputs
               [{:id :test-runtime :sha256 snapshot-id}])
        selected-job
        (update job :test-policy merge (or policy-overrides {}))
        selected-plan
        {:schema :gravity/sh01-impact-test-plan-v1
         :authority (if authoritative? :authoritative :non-authoritative)
         :authoritative? (boolean authoritative?)
         :namespaces [(:namespace selected-job)]
         :shards [selected-job]}
        explicit-classpath-identities
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'explicit-classpath-identities)]
    (with-redefs-fn
      {explicit-classpath-identities
       (fn [_ closure-root]
         [{:path "classpath/entry-0000" :sha256 closure-root}])}
      #(wiring/cache-request
        selected-context selected-plan selected-job
        (cond->
         {:development-operation-identity
          {:id :closure-test-operation :sha256 snapshot-id}}
          command (assoc :command command)
          authoritative? (assoc :authoritative? true))))))

(deftest repository-snapshot-is-stable-and-complete-before-launch
  (let [first-snapshot (wiring/repository-snapshot ".")
        second-snapshot (wiring/repository-snapshot ".")]
    (is (= first-snapshot second-snapshot))
    (is (= :gravity/sh01-incremental-repository-snapshot-v2
           (:schema first-snapshot)))
    (is (= :git-index-overlay-v2 (:snapshot-engine first-snapshot)))
    (is (<= (:hashed-path-count first-snapshot)
            (:overlay-path-count first-snapshot)))
    (is (pos? (:path-count first-snapshot)))
    (is (boolean
         (re-matches #"sha256:[0-9a-f]{64}"
                     (:repository-identity first-snapshot))))
    (is (= "SH01-DEVELOPMENT-LOOP-ROOT"
           (:id (exception-data
                 #(wiring/repository-snapshot "bootstrap")))))))

(deftest repository-snapshot-detects-content-and-inventory-without-metadata-help
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve (.toRealPath base no-links) "repository")
            tracked (.resolve repository-root "tracked.txt")
            untracked (.resolve repository-root "untracked.txt")]
        (Files/createDirectories repository-root no-file-attributes)
        (is (zero? (:exit (shell/sh "git" "init" "-q"
                                    :dir (str repository-root)))))
        (Files/write tracked (.getBytes "first" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (is (zero? (:exit (shell/sh "git" "add" "tracked.txt"
                                    :dir (str repository-root)))))
        (is (zero? (:exit (shell/sh "git" "-c" "user.name=Gravity"
                                    "-c" "user.email=gravity@example.invalid"
                                    "commit" "-qm" "fixture"
                                    :dir (str repository-root)))))
        (let [first-snapshot (wiring/repository-snapshot repository-root)
              original-time (Files/getLastModifiedTime tracked no-links)]
          (is (= :git-index-overlay-v2 (:snapshot-engine first-snapshot)))
          (is (zero? (:overlay-path-count first-snapshot)))
          (is (zero? (:hashed-path-count first-snapshot)))
          (is (zero? (:hashed-byte-count first-snapshot)))
          (Files/write tracked (.getBytes "other" "UTF-8")
                       (make-array java.nio.file.OpenOption 0))
          (Files/setLastModifiedTime tracked original-time)
          (let [content-snapshot (wiring/repository-snapshot repository-root)]
            (is (= 1 (:overlay-path-count content-snapshot)))
            (is (= 1 (:hashed-path-count content-snapshot)))
            (is (= 5 (:hashed-byte-count content-snapshot)))
            (is (not= (:repository-identity first-snapshot)
                      (:repository-identity content-snapshot)))
            (is (zero? (:exit (shell/sh "git" "add" "tracked.txt"
                                        :dir (str repository-root)))))
            (let [staged-snapshot (wiring/repository-snapshot repository-root)]
              (is (not= (:repository-identity content-snapshot)
                        (:repository-identity staged-snapshot))))
            (Files/write untracked (.getBytes "inventory" "UTF-8")
                         (make-array java.nio.file.OpenOption 0))
            (let [inventory-snapshot
                  (wiring/repository-snapshot repository-root)]
              (is (= (inc (:path-count content-snapshot))
                     (:path-count inventory-snapshot)))
              (is (not= (:repository-identity content-snapshot)
                        (:repository-identity inventory-snapshot)))
              (Files/delete tracked)
              (let [deleted-snapshot
                    (wiring/repository-snapshot repository-root)]
                (is (not= (:repository-identity inventory-snapshot)
                          (:repository-identity deleted-snapshot)))
                (is (= 1 (:hashed-path-count deleted-snapshot)))))))))))

(deftest repository-snapshot-falls-back-for-index-flags
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve (.toRealPath base no-links) "repository")
            tracked (.resolve repository-root "tracked.txt")]
        (Files/createDirectories repository-root no-file-attributes)
        (is (zero? (:exit (shell/sh "git" "init" "-q"
                                    :dir (str repository-root)))))
        (Files/write tracked (.getBytes "tracked" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (is (zero? (:exit (shell/sh "git" "add" "tracked.txt"
                                    :dir (str repository-root)))))
        (is (zero? (:exit (shell/sh "git" "update-index" "--skip-worktree"
                                    "tracked.txt" :dir (str repository-root)))))
        (let [snapshot (wiring/repository-snapshot repository-root)
              alternate-blob
              (str/trim
               (:out (shell/sh "git" "hash-object" "-w" "--stdin"
                               :in "alternate" :dir (str repository-root))))]
          (is (= :gravity/sh01-incremental-repository-snapshot-v2
                 (:schema snapshot)))
          (is (= :git-index-full-content-v2 (:snapshot-engine snapshot)))
          (is (= 1 (:hashed-path-count snapshot)))
          (is (= 7 (:hashed-byte-count snapshot)))
          (is (zero? (:exit (shell/sh "git" "update-index"
                                      "--no-skip-worktree" "tracked.txt"
                                      :dir (str repository-root)))))
          (is (zero? (:exit (shell/sh "git" "update-index" "--cacheinfo"
                                      "100644" alternate-blob "tracked.txt"
                                      :dir (str repository-root)))))
          (is (zero? (:exit (shell/sh "git" "update-index" "--skip-worktree"
                                      "tracked.txt" :dir (str repository-root)))))
          (is (not= (:repository-identity snapshot)
                    (:repository-identity
                    (wiring/repository-snapshot repository-root)))))))))

(deftest repository-snapshot-falls-back-when-git-ignores-file-mode
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve (.toRealPath base no-links) "repository")
            tracked (.resolve repository-root "tracked.txt")]
        (Files/createDirectories repository-root no-file-attributes)
        (is (zero? (:exit (shell/sh "git" "init" "-q"
                                    :dir (str repository-root)))))
        (Files/write tracked (.getBytes "tracked" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/setPosixFilePermissions
         tracked (PosixFilePermissions/fromString "rw-------"))
        (is (zero? (:exit (shell/sh "git" "add" "tracked.txt"
                                    :dir (str repository-root)))))
        (is (zero? (:exit (shell/sh "git" "config" "core.filemode" "false"
                                    :dir (str repository-root)))))
        (let [before (wiring/repository-snapshot repository-root)]
          (is (= :git-index-full-content-v2 (:snapshot-engine before)))
          (Files/setPosixFilePermissions
           tracked (PosixFilePermissions/fromString "rwx------"))
          (let [after (wiring/repository-snapshot repository-root)]
            (is (not= (:repository-identity before)
                      (:repository-identity after)))))))))

(deftest repository-snapshot-falls-back-for-clean-checkout-transforms
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve (.toRealPath base no-links) "repository")
            tracked (.resolve repository-root "tracked.txt")
            attributes (.resolve repository-root ".gitattributes")]
        (Files/createDirectories repository-root no-file-attributes)
        (is (zero? (:exit (shell/sh "git" "init" "-q"
                                    :dir (str repository-root)))))
        (Files/write tracked (.getBytes "line-one\nline-two\n" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (is (zero? (:exit (shell/sh "git" "add" "tracked.txt"
                                    :dir (str repository-root)))))
        (let [plain (wiring/repository-snapshot repository-root)]
          (is (= :git-index-overlay-v2 (:snapshot-engine plain)))
          (is (zero? (:exit (shell/sh "git" "config" "core.autocrlf" "input"
                                      :dir (str repository-root)))))
          (is (= :git-index-full-content-v2
                 (:snapshot-engine
                  (wiring/repository-snapshot repository-root))))
          (is (zero? (:exit (shell/sh "git" "config" "core.autocrlf" "true"
                                      :dir (str repository-root)))))
          (Files/delete tracked)
          (is (zero? (:exit (shell/sh "git" "checkout" "--" "tracked.txt"
                                      :dir (str repository-root)))))
          (let [crlf (wiring/repository-snapshot repository-root)]
            (is (= :git-index-full-content-v2 (:snapshot-engine crlf)))
            (is (not= (:repository-identity plain)
                      (:repository-identity crlf)))))
        (is (zero? (:exit (shell/sh "git" "config" "core.autocrlf" "false"
                                    :dir (str repository-root)))))
        (Files/write attributes (.getBytes "*.txt text eol=crlf\n" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (is (zero? (:exit (shell/sh "git" "add" ".gitattributes"
                                    :dir (str repository-root)))))
        (is (= :git-index-full-content-v2
               (:snapshot-engine
                (wiring/repository-snapshot repository-root))))))))

(deftest repository-snapshot-rejects-invalid-git-path-encoding-and-early-limit
  (let [nul-separated-paths
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'nul-separated-paths)
        repository-paths
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'repository-paths)
        run-git-bytes!
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'run-git-bytes!)
        enforce-limit
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'enforce-nul-entry-limit!)
        invalid-path (byte-array [(unchecked-byte 0xff) (byte 0)])
        over-limit (byte-array 100001)]
    (is (= "SH01-DEVELOPMENT-LOOP-GIT"
           (:id (exception-data #(nul-separated-paths invalid-path)))))
    (is (= "SH01-DEVELOPMENT-LOOP-GIT"
           (:id
            (with-redefs-fn
              {run-git-bytes! (fn [_ _] invalid-path)}
              #(exception-data (fn [] (repository-paths ".")))))))
    (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-LIMIT"
           (:id (exception-data #(enforce-limit over-limit)))))))

(deftest repository-snapshot-wraps-git-input-failures
  (let [run-git-bytes!
        (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                    'run-git-bytes!)]
    (is (= "SH01-DEVELOPMENT-LOOP-GIT"
           (:id (exception-data
                 #(run-git-bytes! "." ["version"]
                                  (byte-array (* 4 1024 1024)))))))))

(deftest external-classpath-command-bytes-and-mode-affect-identities
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "repository")
            external-file (.resolve base "external.jar")
            command-file (.resolve base "tool")
            classpath-identities
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'classpath-identities)
            runtime-identities
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'base-runtime-identities)
            regular-file-identity
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'regular-file-identity)
            original-classpath (System/getProperty "java.class.path")
            separator (System/getProperty "path.separator")]
        (Files/createDirectories repository-root no-file-attributes)
        (Files/write external-file (.getBytes "first" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/write command-file (.getBytes "tool-first" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/setPosixFilePermissions
         command-file (PosixFilePermissions/fromString "rwx------"))
        (try
          (System/setProperty "java.class.path"
                              (str repository-root separator external-file))
          (let [first-classpath
                (classpath-identities repository-root snapshot-id)
                first-runtime
                (runtime-identities repository-root
                                    {:command [(str command-file)]})]
            (Files/write external-file (.getBytes "second" "UTF-8")
                         (make-array java.nio.file.OpenOption 0))
            (Files/write command-file (.getBytes "tool-second" "UTF-8")
                         (make-array java.nio.file.OpenOption 0))
            (let [second-classpath
                  (classpath-identities repository-root snapshot-id)
                  second-runtime
                  (runtime-identities repository-root
                                      {:command [(str command-file)]})]
              (is (not= (get-in first-classpath [1 :sha256])
                        (get-in second-classpath [1 :sha256])))
              (is (not= (:sha256 (last first-runtime))
                        (:sha256 (last second-runtime))))
              (Files/setPosixFilePermissions
               command-file (PosixFilePermissions/fromString "rw-------"))
              (is (not= (:sha256 (last second-runtime))
                        (regular-file-identity command-file)))
              (is (= "SH01-DEVELOPMENT-LOOP-TOOL"
                     (:id
                      (exception-data
                       #(runtime-identities
                         repository-root
                         {:command [(str command-file)]})))))))
          (finally
            (System/setProperty "java.class.path" original-classpath)))))))

(deftest repository-symlink-input-fails-closed
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "repository")
            target (.resolve repository-root "target.txt")
            link (.resolve repository-root "link.txt")
            update-file
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'update-file!)]
        (Files/createDirectories repository-root no-file-attributes)
        (Files/write target (.getBytes "target" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/createSymbolicLink link (.getFileName target)
                                  no-file-attributes)
        (is (= "SH01-DEVELOPMENT-LOOP-SYMLINK"
               (:id
                (exception-data
                 #(update-file
                   (java.security.MessageDigest/getInstance "SHA-256")
                   repository-root
                   "link.txt")))))))))

(deftest repository-executable-mode-affects-identity
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "repository")
            source (.resolve repository-root "tool.clj")
            update-file
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'update-file!)
            identity
            (fn []
              (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
                (update-file digest repository-root "tool.clj")
                (vec (.digest digest))))]
        (Files/createDirectories repository-root no-file-attributes)
        (Files/write source (.getBytes "(println :tool)" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/setPosixFilePermissions
         source (PosixFilePermissions/fromString "rw-------"))
        (let [non-executable-identity (identity)]
          (Files/setPosixFilePermissions
           source (PosixFilePermissions/fromString "rwx------"))
          (is (not= non-executable-identity (identity))))))))

(deftest reviewed-leaf-closure-is-path-neutral-and-content-complete
  (with-coordination-root
    (fn [base]
      (let [first-root (.resolve base "first-worktree")
            second-root (.resolve base "second-worktree")
            first-coordination (.resolve base "first-coordination")
            second-coordination (.resolve base "second-coordination")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            full-id-a snapshot-id
            full-id-b (str "sha256:" (apply str (repeat 64 "b")))]
        (Files/createDirectories first-root no-file-attributes)
        (Files/createDirectories second-root no-file-attributes)
        (copy-reviewed-closure! first-root job)
        (copy-reviewed-closure! second-root job)
        (Files/write (.resolve second-root "unrelated.txt")
                     (.getBytes "unrelated" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (let [first-request
              (closure-cache-request first-coordination first-root job full-id-a)
              unrelated-request
              (closure-cache-request second-coordination second-root job full-id-b)
              source-relative "bootstrap/clojure/src/gravity/c11_mir.clj"
              second-source (.resolve second-root source-relative)
              source-bytes (Files/readAllBytes second-source)
              source-time (Files/getLastModifiedTime second-source no-links)]
          (is (not= full-id-a full-id-b))
          (is (= (:repository-identity first-request)
                 (:repository-identity unrelated-request)))
          (is (= (cache/cache-key first-request)
                 (cache/cache-key unrelated-request)))
          (is (not (str/includes? (pr-str first-request) (str first-root))))
          (aset-byte source-bytes 0
                     (byte (bit-xor 1 (bit-and 0xff (aget source-bytes 0)))))
          (Files/write second-source source-bytes
                       (make-array java.nio.file.OpenOption 0))
          (Files/setLastModifiedTime second-source source-time)
          (let [changed-request
                (closure-cache-request second-coordination second-root job full-id-b)]
            (is (not= (:repository-identity first-request)
                      (:repository-identity changed-request)))
            (is (not= (cache/cache-key first-request)
                      (cache/cache-key changed-request))))
          (Files/copy (.resolve first-root source-relative) second-source
                      (into-array StandardCopyOption
                                  [StandardCopyOption/REPLACE_EXISTING
                                   StandardCopyOption/COPY_ATTRIBUTES]))
          (Files/setPosixFilePermissions
           second-source (PosixFilePermissions/fromString "rwx------"))
          (is (not= (cache/cache-key first-request)
                    (cache/cache-key
                     (closure-cache-request second-coordination second-root
                                            job full-id-b))))
          (Files/copy (.resolve first-root source-relative) second-source
                      (into-array StandardCopyOption
                                  [StandardCopyOption/REPLACE_EXISTING
                                   StandardCopyOption/COPY_ATTRIBUTES]))
          (doseq [relative
                  ["contracts/stage0-incremental-test-dependencies.edn"
                   "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"]]
            (let [target (.resolve second-root relative)
                  original (Files/readAllBytes target)]
              (Files/write target (byte-array (concat original [(byte 10)]))
                           (make-array java.nio.file.OpenOption 0))
              (is (not= (cache/cache-key first-request)
                        (cache/cache-key
                         (closure-cache-request second-coordination second-root
                                                job full-id-b)))
                  relative)
              (Files/copy (.resolve first-root relative) target
                          (into-array StandardCopyOption
                                      [StandardCopyOption/REPLACE_EXISTING
                                       StandardCopyOption/COPY_ATTRIBUTES]))))
          (doseq [relative
                  ["bootstrap/clojure/test/gravity/self_hosting/new_test.clj"
                   "bootstrap/clojure/src/gravity/unexpected.class"
                   "bootstrap/clojure/src/data_readers.clj"]]
            (let [target (.resolve second-root relative)]
              (Files/createDirectories (.getParent target) no-file-attributes)
              (Files/write target (.getBytes "inventory change" "UTF-8")
                           (make-array java.nio.file.OpenOption 0))
              (is (not= (cache/cache-key first-request)
                        (cache/cache-key
                         (closure-cache-request second-coordination second-root
                                                job full-id-b)))
                  relative)
              (Files/delete target)))
          (let [fresh-request
                (closure-cache-request second-coordination second-root job
                                       full-id-b
                                       {:freshness-required? true})]
            (is (not= (:test-identity first-request)
                      (:test-identity fresh-request)))
            (is (= :freshness-required-test
                   (get-in (cache/lookup-or-run!
                            fresh-request
                            (fn [] {:status :passed :exit-code 0}))
                           [:receipt :reason]))))
          (is (not= (:test-identity first-request)
                    (:test-identity
                     (closure-cache-request
                      second-coordination second-root job full-id-b nil
                      ["clojure" "-M:test" "--namespace"
                       "gravity.c11-mir-test"])))))))))

(deftest reviewed-leaf-closure-reuses-only-identical-declared-inputs
  (with-coordination-root
    (fn [base]
      (let [first-root (.resolve base "producer-worktree")
            second-root (.resolve base "consumer-worktree")
            job (reviewed-closure-job 'gravity.compiler-pass-manifest-test)
            calls (atom 0)]
        (Files/createDirectories first-root no-file-attributes)
        (Files/createDirectories second-root no-file-attributes)
        (copy-reviewed-closure! first-root job)
        (copy-reviewed-closure! second-root job)
        (let [first-request
              (closure-cache-request (.resolve base "producer-coordination")
                                     first-root job snapshot-id)
              second-request
              (assoc
               (closure-cache-request (.resolve base "consumer-coordination")
                                      second-root job
                                      (str "sha256:"
                                           (apply str (repeat 64 "b"))))
               :cache-directory (:cache-directory first-request))
              operation
              (fn []
                (swap! calls inc)
                {:status :passed
                 :exit-code 0
                 :authority :non-authoritative
                 :authoritative? false
                 :timed-out? false
                 :nondeterministic? false
                 :performance? false
                 :proof? false
                 :freshness-required? false})]
          (is (= :miss
                 (get-in (cache/lookup-or-run! first-request operation)
                         [:receipt :decision])))
          (is (= :hit
                 (get-in (cache/lookup-or-run! second-request operation)
                         [:receipt :decision])))
          (is (= 1 @calls))
          (let [source
                (.resolve second-root
                          "bootstrap/clojure/src/gravity/compiler_pass_manifest.clj")]
            (Files/write source (.getBytes "relevant change" "UTF-8")
                         (make-array java.nio.file.OpenOption 0))
            (let [changed-request
                  (assoc
                   (closure-cache-request
                    (.resolve base "changed-coordination") second-root job
                    (str "sha256:" (apply str (repeat 64 "c"))))
                   :cache-directory (:cache-directory first-request))]
              (is (= :miss
                     (get-in (cache/lookup-or-run! changed-request operation)
                             [:receipt :decision])))
              (is (= 2 @calls)))))))))

(deftest bootstrap-compatibility-fallback-binds-loader-resources-and-rejects-stale-hits
  (doseq [namespace ['gravity.bootstrap-compatibility.c11-test
                    'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test]]
    (with-coordination-root
      (fn [base]
        (let [root (.toRealPath
                    (Files/createDirectories (.resolve base "repository")
                                             no-file-attributes)
                    no-links)
              paths ["bootstrap/clojure/src/gravity/bootstrap_impl/loader.clj"
                     "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
                     "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/fixture.clj"
                     "bootstrap/clojure/src/gravity/bootstrap_impl/data/foundation/fixture.edn"
                     "bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity"
                     "bootstrap/clojure/fixtures/accepted/compiler-passes.gravity"]
              job (reviewed-closure-job namespace)
              calls (atom 0)]
          (is (zero? (:exit (shell/sh "git" "init" "-q" :dir (str root)))))
          (doseq [relative paths]
            (let [path (.resolve root relative)]
              (Files/createDirectories (.getParent path) no-file-attributes)
              (spit (str path) "alpha")))
          (is (zero? (:exit (shell/sh "git" "add" "." :dir (str root)))))
          (is (zero? (:exit (shell/sh "git" "-c" "user.name=Gravity"
                                      "-c" "user.email=gravity@example.invalid"
                                      "commit" "-qm" "fixture" :dir (str root)))))
          (is (nil? (:cache-closure job)))
          (let [snapshot (wiring/repository-snapshot root)
                selected-context (assoc (context base) :working-directory (str root)
                                        :snapshot snapshot :revalidate-snapshot? true)
                selected-plan (impact-plan namespace)
                attached (wiring/attach-request selected-context selected-plan {} job)
                request (:development-cache-request attached)
                operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
            (is (= (:repository-identity snapshot) (:repository-identity request)))
            (is (= ["repository/complete-production-closure-v1"]
                   (mapv :path (get-in request [:dependencies :production-inputs]))))
            (is (= :passed (:status (wiring/run-unit! selected-context attached operation))))
            (is (:hit-result (wiring/probe selected-context attached)))
            (doseq [relative paths]
              (let [path (.resolve root relative)
                    original (Files/readAllBytes path)
                    timestamp (Files/getLastModifiedTime path no-links)
                    before @calls]
                (try
                  ;; Equal length and preserved mtime must not manufacture a hit.
                  (spit (str path) "omega")
                  (Files/setLastModifiedTime path timestamp)
                  (let [changed (wiring/repository-snapshot root)
                        later (assoc selected-context :snapshot changed)
                        renewed (wiring/attach-request later selected-plan {} job)]
                    (is (not= (:repository-identity snapshot) (:repository-identity changed)) relative)
                    (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                           (:id (exception-data #(wiring/probe selected-context attached)))) relative)
                    (is (not= (cache/cache-key request)
                              (cache/cache-key (:development-cache-request renewed))) relative)
                    (is (= :passed (:status (wiring/run-unit! later renewed operation))) relative)
                    (is (= (inc before) @calls) relative))
                  (finally
                    (Files/write path original (make-array java.nio.file.OpenOption 0))
                    (Files/setLastModifiedTime path timestamp)))))
            (is (= (inc (count paths)) @calls))
            (is (:hit-result (wiring/probe selected-context attached)))))))))

(deftest explicit-closure-falls-back-for-mixed-fresh-and-authoritative-work
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "worktree")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            full-id (str "sha256:" (apply str (repeat 64 "b")))]
        (Files/createDirectories repository-root no-file-attributes)
        (copy-reviewed-closure! repository-root job)
        (doseq [request
                [(closure-cache-request (.resolve base "fresh-coordination")
                                        repository-root job full-id
                                        {:freshness-required? true})
                 (closure-cache-request (.resolve base "authority-coordination")
                                        repository-root job full-id nil nil true)
                 (closure-cache-request
                  (.resolve base "mixed-coordination") repository-root
                  (assoc job :batch-jobs
                         [job {:namespace 'gravity.unknown-test
                               :test-policy (reviewed-policy)}])
                  full-id)]]
          (is (= full-id (:repository-identity request))))))))

(deftest invalid-or-undeclared-closure-retains-full-repository-key
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "worktree")
            reviewed-job (reviewed-closure-job 'gravity.c11-mir-test)
            full-id (str "sha256:" (apply str (repeat 64 "b")))]
        (Files/createDirectories repository-root no-file-attributes)
        (copy-reviewed-closure! repository-root reviewed-job)
        (doseq [job [(dissoc reviewed-job :cache-closure-authorized?)
                     (dissoc reviewed-job :cache-closure)
                     (assoc reviewed-job :cache-closure
                            (dissoc (:cache-closure reviewed-job) :schema))
                     (assoc reviewed-job :namespace 'gravity.unknown-test)]]
          (let [request
                (closure-cache-request (.resolve base (str (gensym "cache-")))
                                       repository-root job full-id)]
            (is (= full-id (:repository-identity request)))
            (is (= full-id
                   (get-in request
                           [:dependencies :runner-identity :sha256])))))
        (let [selected-context
              (assoc (context (.resolve base "ambiguous-coordination"))
                     :working-directory (str repository-root)
                     :snapshot
                     {:schema :gravity/sh01-complete-repository-snapshot-v1
                      :repository-identity full-id
                      :path-count 1})
              plan (impact-plan (:namespace reviewed-job))
              explicit-classpath-identities
              (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                          'explicit-classpath-identities)
              request
              (with-redefs-fn
                {explicit-classpath-identities (fn [_ _] nil)}
                #(wiring/cache-request selected-context plan reviewed-job {}))]
          (is (= full-id (:repository-identity request))))
        (Files/delete
         (.resolve repository-root
                   "bootstrap/clojure/src/gravity/c11_mir.clj"))
        (is (= full-id
               (:repository-identity
                (closure-cache-request
                 (.resolve base "missing-coordination")
                 repository-root reviewed-job full-id))))))))

(deftest explicit-closure-read-race-and-symlink-fail-closed
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "worktree")
            job
            (reviewed-closure-job
             'gravity.compiler-pass-manifest-test)
            update-file
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'update-file!)
            closure-inventory
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'closure-inventory)]
        (Files/createDirectories repository-root no-file-attributes)
        (copy-reviewed-closure! repository-root job)
        (let [failure
              (with-redefs-fn
                {update-file
                 (fn [_ _ path]
                   (throw (ex-info "injected closure race"
                                   {:id "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                                    :path path})))}
                #(exception-data
                  (fn []
                    (closure-cache-request (.resolve base "race-coordination")
                                           repository-root job snapshot-id))))]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure))))
        (let [inventory-calls (atom 0)
              inventory-function @closure-inventory
              failure
              (with-redefs-fn
                {closure-inventory
                 (fn [root declaration]
                   (let [inventory (inventory-function root declaration)]
                     (if (even? (swap! inventory-calls inc))
                       (update inventory :paths conj "injected-new-test.clj")
                       inventory)))}
                #(exception-data
                  (fn []
                    (closure-cache-request
                     (.resolve base "inventory-race-coordination")
                     repository-root job snapshot-id))))]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure))))
        (let [source
              (.resolve repository-root
                        "bootstrap/clojure/src/gravity/compiler_pass_manifest.clj")
              target (.resolve repository-root "replacement.clj")]
          (Files/write target (.getBytes "replacement" "UTF-8")
                       (make-array java.nio.file.OpenOption 0))
          (Files/delete source)
          (Files/createSymbolicLink source target no-file-attributes)
          (is (= "SH01-DEVELOPMENT-LOOP-SYMLINK"
                 (:id
                  (exception-data
                   #(closure-cache-request
                     (.resolve base "symlink-coordination")
                     repository-root job snapshot-id))))))))))

(deftest required-closure-input-deletion-cannot-produce-a-cache-request
  (with-coordination-root
    (fn [base]
      (let [repository-root (.resolve base "worktree")
            coordination-root (.resolve base "deletion-race-coordination")
            job
            (reviewed-closure-job 'gravity.c11-mir-test)
            source-relative "bootstrap/clojure/src/gravity/c11_mir.clj"
            source (.resolve repository-root source-relative)
            update-file
            (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                        'update-file!)
            update-file-function @update-file
            deleted? (atom false)
            operations (atom 0)]
        (Files/createDirectories repository-root no-file-attributes)
        (copy-reviewed-closure! repository-root job)
        (let [failure
              (with-redefs-fn
                {update-file
                 (fn [message-digest root relative]
                   (when (and (= source-relative relative)
                              (compare-and-set! deleted? false true))
                     (Files/delete source))
                   (update-file-function message-digest root relative))}
                #(exception-data
                  (fn []
                    (cache/lookup-or-run!
                     (closure-cache-request coordination-root repository-root
                                            job snapshot-id)
                     (fn []
                       (swap! operations inc)
                       {:status :passed :exit-code 0})))))]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure)))
          (is (true? @deleted?))
          (is (zero? @operations))
          (with-open [entries (Files/list (.resolve coordination-root "cache"))]
            (is (empty? (vec (.toArray entries))))))))))

(deftest prepared-context-revalidates-external-inputs
  (with-coordination-root
    (fn [base]
      (let [command-file (.resolve base "external-command")]
        (Files/write command-file (.getBytes "command-v1" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/setPosixFilePermissions
         command-file (PosixFilePermissions/fromString "rwx------"))
        (let [selected-context
              (wiring/prepare-context
               "."
               {:cache-directory (str (.resolve base "cache"))
                :broker-root (str (.resolve base "broker"))
                :command [(str command-file)]})
              verify-snapshot
              (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                          'verify-snapshot!)]
          (is (nil? (verify-snapshot selected-context)))
          (Files/write command-file (.getBytes "command-v2" "UTF-8")
                       (make-array java.nio.file.OpenOption 0))
          (let [failure (exception-data #(verify-snapshot selected-context))]
            (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure)))
            (is (= :runtime-tool (:input-kind failure)))))))))

(deftest runtime-identities-and-cache-requests-bind-jvm-configuration
  (with-coordination-root
    (fn [base]
      (let [resolve-wiring #(ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring %)
            observed-arguments (atom ["-Xss256k" "-Dfixture.private=not-for-receipts"])
            selected-context (context base)
            job (first (:shards (impact-plan 'gravity.self-hosting.sh01-jvm-config-test)))
            plan (impact-plan (:namespace job))
            root (.normalize (.toAbsolutePath (.toPath (java.io.File. "."))))]
        (with-redefs-fn
          {(resolve-wiring 'jvm-input-arguments) (fn [] @observed-arguments)}
          (fn []
            (let [first-runtime ((resolve-wiring 'base-runtime-identities) root {})
                  first-fallback ((resolve-wiring 'runtime-identities) {})
                  first-request (wiring/cache-request selected-context plan job {})]
              (reset! observed-arguments ["-Xss16m" "-Dfixture.private=not-for-receipts"])
              (let [second-runtime ((resolve-wiring 'base-runtime-identities) root {})
                    second-fallback ((resolve-wiring 'runtime-identities) {})
                    second-request (wiring/cache-request selected-context plan job {})]
                (is (not= first-runtime second-runtime))
                (is (not= first-fallback second-fallback))
                (is (not= (cache/cache-key first-request) (cache/cache-key second-request)))
                (is (not (str/includes? (pr-str [first-runtime second-runtime
                                                first-request second-request])
                                       "not-for-receipts")))
                (is (= first-runtime
                       (do (reset! observed-arguments ["-Xss256k" "-Dfixture.private=not-for-receipts"])
                           ((resolve-wiring 'base-runtime-identities) root {}))))))))))))

(deftest bootstrap-compatibility-effects-stay-fresh-before-cache-observation
  (let [c11 'gravity.bootstrap-compatibility.c11-test
        manifest 'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test
        leaf 'gravity.c2-artifact-identity-test
        observations (atom [])
        operations (atom 0)
        resolve-wiring #(ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring %)]
    (with-redefs-fn
      {(resolve-wiring 'supported-resource-loaders?) (constantly true)
       (resolve-wiring 'child-startup-resources-absent?)
       (fn [& _] (swap! observations conj :startup) true)
       (resolve-wiring 'effective-child-runtime)
       (fn [& _] (swap! observations conj :runtime) nil)
       #'basis/current-basis (fn [] (swap! observations conj :basis) nil)
       #'wiring/repository-snapshot (fn [& _] (swap! observations conj :snapshot) nil)
       #'cache/lookup-or-run! (fn [& _] (swap! observations conj :lookup) nil)}
      (fn []
        (doseq [selection [[c11] [manifest] [c11 manifest] [manifest c11]
                          [leaf c11] [manifest leaf]]
                status [:passed :error]]
          (let [result (wiring/run-child-unit!
                        selection {:arguments ["not-the-actual-cli"]}
                        #(do (swap! operations inc) {:status status :sentinel ::result}))]
            (is (= status (:status result)))
            (is (= ::result (:sentinel result)))
            (is (true? (:tests-executed? result)))
            (is (false? (:child-cache-hit? result)))
            (is (= :unreviewed-bootstrap-initialization-effects
                   (get-in result [:child-cache-receipt :reason])))))
        (is (= 12 @operations))
        (is (empty? @observations))
        (let [failure (ex-info "fresh operation error" {:id ::operation-error})]
          (is (identical? failure
                          (try
                            (wiring/run-child-unit! [c11] {} #(throw failure))
                            (catch clojure.lang.ExceptionInfo observed observed)))))
        (is (= :unreviewed-or-fresh-test-policy
               (get-in (wiring/run-child-unit! [leaf] {} (constantly {:status :passed}))
                       [:child-cache-receipt :reason])))
        (is (empty? @observations))
        ;; The two cache-eligible leaves still reach the existing runtime observer.
        ;; No saved result or successful compiler execution is supplied here.
        (doseq [selection [['gravity.c11-mir-test]
                           ['gravity.compiler-pass-manifest-test]]]
          (let [result (wiring/run-child-unit! selection {} (constantly {:status :passed}))]
            (is (= :unknown-child-configuration
                   (get-in result [:child-cache-receipt :reason])))))
        (is (= [:startup :runtime :startup :runtime] @observations))))))

(deftest componentized-leaf-closures-bind-the-nested-implementation
  (doseq [[namespace directory expected-count]
          [['gravity.c2-artifact-identity-test "c2_artifact_identity" 5]
           ['gravity.c11-mir-test "c11_mir" 6]
           ['gravity.compiler-pass-manifest-test "compiler_pass_manifest" 15]]]
    (with-coordination-root
      (fn [base]
        (let [root (.resolve base "repository")
              coordination (.resolve base "coordination")
              job (reviewed-closure-job namespace)
              prefix (str "bootstrap/clojure/src/gravity/" directory "/")
              inputs (filter #(str/starts-with? (:path %) prefix)
                             (get-in job [:cache-closure :inputs]))]
          (Files/createDirectories root no-file-attributes)
          (copy-reviewed-closure! root job)
          (is (= expected-count (count inputs)))
          (is (every? #(= :transitive (:role %)) inputs))
          (let [before (closure-cache-request coordination root job snapshot-id)]
            (doseq [{:keys [path]} inputs]
              (let [target (.resolve root path)
                    original (Files/readAllBytes target)]
                (try
                  (Files/write target (byte-array (concat original [(byte 10)]))
                               (make-array java.nio.file.OpenOption 0))
                  (is (not= (cache/cache-key before)
                            (cache/cache-key
                             (closure-cache-request coordination root job snapshot-id)))
                      path)
                  (finally
                    (Files/write target original
                                 (make-array java.nio.file.OpenOption 0))))))))))))

(deftest reader-identity-reuse-keeps-producer-and-consumed-inputs-exact
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c2-artifact-identity-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            unrelated (.resolve root "bootstrap/clojure/test/gravity/self_hosting/unrelated_test.clj")
            notes (.resolve root "notes.md")
            _ (spit (str unrelated) "; existing unrelated test body")
            selected-context (assoc (context base) :working-directory (str root)
                                    :revalidate-snapshot? true
                                    :revision {:commit "producer" :tree "producer-tree"})
            classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                  'explicit-classpath-identities)
            calls (atom 0)]
        (with-redefs-fn
          {classpath (fn [_ closure-root] [{:path "classpath/entry-0000" :sha256 closure-root}])
           #'wiring/repository-snapshot (fn [_] (throw (Error. "unexpected broad snapshot")))}
          (fn []
            (let [attached (wiring/attach-request selected-context (impact-plan (:namespace job)) {} job)
                  operation #(do (swap! calls inc) {:status :passed :exit-code 0})
                  original (wiring/run-unit! selected-context attached operation)]
              (spit (str notes) "unrelated documentation")
              (spit (str unrelated) "; changed unrelated test body")
              (let [later (assoc selected-context :revision {:commit "applying" :tree "applying-tree"})
                    reused (wiring/run-unit! later attached operation)]
                (is (= 1 @calls))
                (is (true? (:cache-hit? reused)))
                (is (= (:execution-provenance original) (:execution-provenance reused)))
                (is (= (:revision selected-context)
                       (get-in reused [:execution-provenance :revision])))
                (is (false? (:authoritative? reused))))
              (spit (str (.resolve root "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"))
                    "\n; changed consumed implementation\n" :append true)
              (is (= :explicit-closure
                     (:input-kind (exception-data #(wiring/probe selected-context attached)))))
              (let [renewed (wiring/attach-request selected-context (impact-plan (:namespace job)) {} job)]
                (is (not= (cache/cache-key (:development-cache-request attached))
                          (cache/cache-key (:development-cache-request renewed))))
                (is (= :passed (:status (wiring/run-unit! selected-context renewed operation))))
                (is (= 2 @calls))))))))))

(defn- literal-require-gaps
  "Check literal repository ns dependencies, not dynamic reads or effect closure."
  [source text declared-paths available?]
  (let [form (binding [*read-eval* false *data-readers* {}
                       *default-data-reader-fn*
                       (fn [tag _] (throw (ex-info "Unsupported dependency source tag" {:tag tag})))]
               (read (java.io.PushbackReader. (java.io.StringReader. text))))]
    (when-not (and (seq? form) (= 'ns (first form)))
      (throw (ex-info "Expected an explicit namespace declaration" {:source source})))
    (vec
     (for [clause (drop 2 form)
           :when (and (seq? clause) (#{:require :use} (first clause)))
           spec (rest clause)
           :let [namespace (cond (symbol? spec) spec
                                 (and (vector? spec) (symbol? (first spec))) (first spec)
                                 :else (throw (ex-info "Unreviewed namespace dependency form"
                                                       {:source source :spec spec})))]
           :when (str/starts-with? (str namespace) "gravity.")
           :let [relative (str (-> (str namespace) (str/replace "." "/")
                                   (str/replace "-" "_")) ".clj")
                 candidates (filterv available?
                                     (mapv #(str % relative)
                                           ["bootstrap/clojure/src/" "bootstrap/clojure/test/"]))]
           :when (or (not= 1 (count candidates))
                     (not (contains? declared-paths (first candidates))))]
       {:source source :namespace namespace :candidates candidates
        :reason (if (= 1 (count candidates)) :undeclared-input :unresolved-or-shadowed-input)}))))

(deftest reviewed-leaf-closures-cover-literal-repository-requires
  ;; This is a regression fence for the three existing declarations. Passing
  ;; does not authorize new closures, dynamic imports, callbacks or reuse.
  (doseq [namespace '[gravity.c11-mir-test gravity.compiler-pass-manifest-test
                      gravity.c2-artifact-identity-test]
          :let [job (reviewed-closure-job namespace)
                inputs (get-in job [:cache-closure :inputs])
                declared (set (map :path inputs))]
          {:keys [path]} inputs
          :when (str/ends-with? path ".clj")]
    (is (empty? (literal-require-gaps path (slurp path) declared #(.isFile (io/file %))))
        (str namespace " consumes " path))))

(deftest literal-require-guard-rejects-mutation-unknown-and-shadowed-inputs
  (let [source "fixture.clj"
        original "(ns fixture (:require [gravity.digest :as digest]))"
        changed "(ns fixture (:require [gravity.digest :as digest] [gravity.new-helper :as helper]))"
        digest "bootstrap/clojure/src/gravity/digest.clj"
        helper "bootstrap/clojure/src/gravity/new_helper.clj"
        available #{digest helper}
        gaps #(literal-require-gaps source % #{digest} available)]
    (is (empty? (gaps original)))
    (is (= [{:source source :namespace 'gravity.new-helper :candidates [helper]
             :reason :undeclared-input}] (gaps changed)))
    (is (empty? (literal-require-gaps source changed #{digest helper} available)))
    (is (= :unresolved-or-shadowed-input
           (:reason (first (literal-require-gaps source changed #{digest} #{digest})))))
    (is (= :unresolved-or-shadowed-input
           (:reason (first (literal-require-gaps source original #{digest}
                            #{digest "bootstrap/clojure/test/gravity/digest.clj"})))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (literal-require-gaps source "(ns fixture (:require (gravity digest)))"
                                      #{digest} available)))
    (is (thrown? RuntimeException
                 (literal-require-gaps source "#=(throw (Exception.))" #{digest} available)))
    (let [calls (atom 0)]
      (binding [*data-readers* {'fixture/run (fn [_] (swap! calls inc))}]
        (is (thrown? clojure.lang.ExceptionInfo
                     (literal-require-gaps source "#fixture/run []" #{digest} available))))
      (is (zero? @calls)))))

(deftest reader-identity-lexical-inputs-invalidate-reuse
  ;; Hypothetical admitted-policy mechanics only. The actual catalog policy
  ;; remains fresh until real expanded-closure producer evidence is reviewed.
  ;; The identity test calls the lexical facade, which requires all three
  ;; focused implementations. A comment preserves behavior but changes evidence.
  (doseq [relative ["bootstrap/clojure/src/gravity/c2_lexical_validation.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/operations.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/primitives.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/product.clj"]]
    (with-coordination-root
      (fn [base]
        (let [root (.resolve base "repository")
              job (reviewed-closure-job 'gravity.c2-artifact-identity-test)
              _ (Files/createDirectories root no-file-attributes)
              _ (copy-reviewed-closure! root job)
              selected-context (assoc (context base) :working-directory (str root)
                                      :revalidate-snapshot? true)
              classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                    'explicit-classpath-identities)
              calls (atom 0)]
          (with-redefs-fn
            {classpath (fn [_ closure-root] [{:path "classpath/entry-0000" :sha256 closure-root}])}
            (fn []
              (let [plan (impact-plan (:namespace job))
                    attached (wiring/attach-request selected-context plan {} job)
                    operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
                (is (true? (get-in attached [:development-cache-request :test-policy :deterministic?])))
                (is (= :passed (:status (wiring/run-unit! selected-context attached operation))))
                (is (true? (:cache-hit? (wiring/run-unit! selected-context attached operation))))
                (is (= 1 @calls))
                (spit (str (.resolve root relative)) "\n; consumed lexical implementation changed\n" :append true)
                (is (= :explicit-closure
                       (:input-kind (exception-data #(wiring/probe selected-context attached)))))
                (let [renewed (wiring/attach-request selected-context plan {} job)]
                  (is (not= (cache/cache-key (:development-cache-request attached))
                            (cache/cache-key (:development-cache-request renewed)))))
                (Files/delete (.resolve root relative))
                (is (= :explicit-closure
                       (:input-kind (exception-data #(wiring/probe selected-context attached)))))
                (is (= 1 @calls))))))))))

(deftest reader-identity-digest-runner-input-invalidates-reuse
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c2-artifact-identity-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            selected-context (assoc (context base) :working-directory (str root)
                                    :revalidate-snapshot? true)
            classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                  'explicit-classpath-identities)
            calls (atom 0)]
        (with-redefs-fn
          {classpath (fn [_ closure-root] [{:path "classpath/entry-0000" :sha256 closure-root}])}
          (fn []
            (let [plan (impact-plan (:namespace job))
                  attached (wiring/attach-request selected-context plan {} job)
                  operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
              (is (some #(= "bootstrap/clojure/src/gravity/digest.clj" (:path %))
                        (get-in attached [:development-cache-request :dependencies :production-inputs])))
              (is (= :passed (:status (wiring/run-unit! selected-context attached operation))))
              ;; Preserve every hash output: a source comment must still invalidate
              ;; evidence because both consumed runner namespaces load this code.
              (spit (str (.resolve root "bootstrap/clojure/src/gravity/digest.clj"))
                    "\n; consumed runner digest implementation changed\n" :append true)
              (is (= :explicit-closure
                     (:input-kind (exception-data #(wiring/probe selected-context attached)))))
              (let [renewed (wiring/attach-request selected-context plan {} job)]
                (is (not= (cache/cache-key (:development-cache-request attached))
                          (cache/cache-key (:development-cache-request renewed))))
                (is (= :passed (:status (wiring/run-unit! selected-context renewed operation))))
                (is (= 2 @calls))))))))))

(deftest reader-identity-declared-lexical-input-deletion-refuses-before-execution
  ;; Audit the retained declaration without enabling the real catalog policy.
  (doseq [relative ["bootstrap/clojure/src/gravity/c2_lexical_validation.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/operations.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/primitives.clj"
                    "bootstrap/clojure/src/gravity/c2_lexical_validation/product.clj"]]
    (with-coordination-root
      (fn [base]
        (let [root (.resolve base "repository")
              job (reviewed-closure-job 'gravity.c2-artifact-identity-test)
              selected-context (assoc (context base) :working-directory (str root)
                                                    :revalidate-snapshot? true)
              classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                    'explicit-classpath-identities)
              verify-inputs (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                        'verify-inputs!)]
          (Files/createDirectories root no-file-attributes)
          (copy-reviewed-closure! root job)
          (with-redefs-fn
            {classpath (fn [_ closure-root] [{:path "classpath/entry-0000" :sha256 closure-root}])}
            (fn []
              (let [attached (wiring/attach-request selected-context
                               (impact-plan (:namespace job)) {} job)]
                (Files/delete (.resolve root relative))
                (is (= :explicit-closure
                       (:input-kind (exception-data
                                      #(verify-inputs selected-context :control [attached])))))))))))))

(deftest reader-identity-catalog-policy-prevents-parent-cache-reuse
  (with-coordination-root
    (fn [base]
      (let [namespace 'gravity.c2-artifact-identity-test
            plan ((requiring-resolve 'gravity.self-hosting.sh01-impact-test-planner/build-plan)
                  {:changed-paths ["bootstrap/clojure/src/gravity/c2_artifact_identity.clj"]})
            job (first (:shards plan))
            attached (wiring/attach-request (context base) plan
                       {:freshness-required? false :fresh? false} job)
            request (:development-cache-request attached)
            calls (atom 0)
            operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
        (is (= namespace (:namespace job)))
        (is (true? (get-in job [:test-policy :freshness-required?])))
        (is (false? (:cache-closure-authorized? job)))
        (is (true? (get-in request [:test-policy :freshness-required?])))
        (doseq [_ (range 2)]
          (let [outcome (cache/lookup-or-run! request operation)]
            (is (= :miss (get-in outcome [:receipt :decision])))
            (is (= :freshness-required-test (get-in outcome [:receipt :reason])))
            (is (false? (get-in outcome [:receipt :cacheable?])))
            (is (false? (get-in outcome [:receipt :stored?])))))
        (is (= 2 @calls))))))

(deftest reader-identity-catalog-policy-prevents-child-cache-reuse
  (let [calls (atom 0)
        forbidden (fn [& _] (throw (ex-info "Fresh selection must not prepare cache/runtime state" {})))
        resolve-wiring #(ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring %)]
    (with-redefs-fn
      {(resolve-wiring 'supported-resource-loaders?) (constantly true)
       (resolve-wiring 'child-startup-resources-absent?) forbidden
       (resolve-wiring 'effective-child-runtime) forbidden
       #'wiring/prepare-context forbidden}
      (fn []
        (doseq [_ (range 2)]
          (let [result (wiring/run-child-unit!
                        ['gravity.c2-artifact-identity-test] {}
                        #(do (swap! calls inc) {:status :passed :exit-code 0}))]
            (is (true? (:tests-executed? result)))
            (is (false? (:child-cache-hit? result)))
            (is (= :fresh (get-in result [:child-cache-receipt :decision])))
            (is (= :unreviewed-or-fresh-test-policy
                   (get-in result [:child-cache-receipt :reason])))
            (is (false? (get-in result [:child-cache-receipt :cacheable?])))
            (is (false? (get-in result [:child-cache-receipt :stored?])))))
        (is (= 2 @calls))))))

(deftest project-startup-resource-absence-is-explicit-and-link-safe
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")]
        (Files/createDirectories root no-file-attributes)
        (is (true? (wiring/project-startup-resources-absent? root)))
        (doseq [relative ["bootstrap/clojure/src/user.clj"
                          "bootstrap/clojure/test/user.clj"
                          "bootstrap/clojure/src/clojure/core/server.clj"
                          "bootstrap/clojure/test/clojure/core/server.clj"]]
          (let [path (.resolve root relative)
                cleanup (if (str/includes? relative "/clojure/core/")
                          (.getParent (.getParent path)) path)]
            (Files/createDirectories (.getParent path) no-file-attributes)
            (try
              ;; These are inert fixture bytes outside the actual classpath.
              (spit (str path) "; startup fixture\n")
              (is (false? (wiring/project-startup-resources-absent? root)) relative)
              (finally
                (if (Files/isDirectory cleanup no-links) (delete-tree! cleanup)
                    (Files/deleteIfExists cleanup))))))
        (let [path (.resolve root "bootstrap/clojure/src/user.clj")]
          (Files/createSymbolicLink path (.resolve base "absent-target.clj") no-file-attributes)
          (is (false? (wiring/project-startup-resources-absent? root)))
          (Files/delete path))
        (spit (str (.resolve root "bootstrap/clojure/src/user.cljc")) "; not the RT startup name\n")
        (is (true? (wiring/project-startup-resources-absent? root)))))))

(deftest startup-resources-disable-command-reuse-before-child-startup
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            namespace 'gravity.compiler-pass-manifest-test
            cache-options {:cache-directory (str (.resolve base "cache"))
                           :timeout-ms 1000 :repository-identity snapshot-id}
            options {:working-directory (str root) :child-development-cache cache-options}
            commands (fn [opts]
                       [(runner/namespace-command namespace opts)
                        (runner/normal-batch-command [{:namespace namespace}]
                                                    (str (.resolve base "report.edn")) opts)])]
        (Files/createDirectories root no-file-attributes)
        (doseq [command (commands options)]
          (is (some #{"--development-cache"} command)))
        (let [startup (.resolve root "bootstrap/clojure/src/user.clj")]
          (Files/createDirectories (.getParent startup) no-file-attributes)
          (spit (str startup) "; never executed\n")
          (doseq [command (commands options)]
            (is (not-any? #{"--development-cache"} command))
            (is (some #{"--namespace"} command)))
          ;; Dropping a malformed optimization would hide its existing refusal.
          (doseq [invalid [(assoc cache-options :extra true)
                           (assoc cache-options :timeout-ms 0)
                           (dissoc cache-options :repository-identity)]
                  command (commands (assoc options :child-development-cache invalid))]
            (is (some #{"--development-cache"} command))
            (is (= "SH01-TEST-USAGE"
                   (:id (exception-data #(child-runner/select-tests (vec (drop 2 command)))))))))))))

(deftest resolved-startup-resources-and-observation-errors-bypass-all-cache-lookup
  (let [resource io/resource
        runtime-var (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                'effective-child-runtime)
        operations (atom 0)
        measurements (atom 0)
        lookups (atom 0)]
    (doseq [startup [(java.net.URL. "file:/unopened-startup-fixture") ::unavailable]]
      (with-redefs-fn
        {#'io/resource (fn [name]
                         (if (= "user.clj" name)
                           (if (= ::unavailable startup)
                             (throw (ex-info "fixture resource lookup failed" {})) startup)
                           (resource name)))
         runtime-var (fn [& _] (swap! measurements inc)
                       (throw (ex-info "runtime measurement must not run" {})))
         #'cache/lookup-or-run! (fn [& _] (swap! lookups inc)
                                 (throw (ex-info "cache lookup must not run" {})))}
        (fn []
          (dotimes [_ 2]
            (let [result (wiring/run-child-unit!
                          ['gravity.compiler-pass-manifest-test]
                          {:arguments ["not-the-actual-cli"]}
                          #(do (swap! operations inc) {:status :passed :exit-code 0}))]
              (is (true? (:tests-executed? result)))
              (is (false? (:child-cache-hit? result)))
              (is (= :unreviewed-startup-resources (get-in result [:child-cache-receipt :reason])))
              (is (false? (get-in result [:child-cache-receipt :stored?]))))))))
    (is (= 4 @operations))
    (is (zero? @measurements))
    (is (zero? @lookups))))

(deftest runtime-startup-revalidation-propagates-interruption-without-execution
  (let [resource io/resource]
    (doseq [failure [(InterruptedException. "startup observation interrupted")
                     (ex-info "startup observation unavailable" {})]]
      (let [observations (atom 0)
            operations (atom 0)
            lookups (atom 0)]
        (try
          (with-redefs [basis/current-basis (constantly {})
                        wiring/project-startup-resources-absent? (constantly true)
                        io/resource (fn [name]
                                      (if (= "user.clj" name)
                                        (when (= 2 (swap! observations inc)) (throw failure))
                                        (resource name)))
                        cache/lookup-or-run! (fn [& _] (swap! lookups inc)
                                              (throw (ex-info "cache lookup must not run" {})))]
            (try
              (let [result (wiring/run-child-unit!
                            ['gravity.compiler-pass-manifest-test]
                            {:arguments ["not-the-actual-cli"]}
                            #(do (swap! operations inc) {:status :passed :exit-code 0}))]
                (is (not (instance? InterruptedException failure)))
                (is (= :unknown-child-configuration
                       (get-in result [:child-cache-receipt :reason])))
                (is (true? (:tests-executed? result))))
              (catch InterruptedException interrupted
                (let [interrupted? (Thread/interrupted)]
                  (is (identical? failure interrupted))
                  (is interrupted?)))))
          (is (= 2 @observations))
          (is (= (if (instance? InterruptedException failure) 0 1) @operations))
          (is (zero? @lookups))
          (finally (Thread/interrupted)))))))

(deftest child-jvm-file-loading-and-unknown-options-stay-fresh
  (let [supported? (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                              'supported-child-jvm-argument?)]
    (doseq [argument ["-Xmx512m" "-Xms64m" "-Xss2m"
                      "-XX:-OmitStackTraceInFastThrow"]]
      (is (true? (supported? argument)) argument))
    (doseq [argument ["-javaagent:/tmp/agent.jar" "-agentpath:/tmp/native.so"
                      "--patch-module=java.base=/tmp/patch"
                      "-Djava.security.properties=/tmp/policy"
                      "-Dfixture.unknown=private-value" "-Xbootclasspath/a:/tmp/classes"]]
      (is (false? (supported? argument)) argument)))
  (let [calls (atom 0)
        operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
    (dotimes [_ 2]
      (let [result (wiring/run-child-unit!
                    ['gravity.compiler-pass-manifest-test]
                    {:arguments ["not-the-actual-cli"]} operation)]
        (is (true? (:tests-executed? result)))
        (is (false? (:child-cache-hit? result)))
        (is (= :unknown-child-configuration
               (get-in result [:child-cache-receipt :reason])))
        (is (false? (get-in result [:child-cache-receipt :stored?])))))
    (is (= 2 @calls))))

(deftest child-cache-cli-options-are-closed-and-explicit
  (let [options {:cache-directory "/tmp/child-cache" :timeout-ms 1000
                 :repository-identity snapshot-id}
        selection ["--namespace" "gravity.compiler-pass-manifest-test"]
        parsed (child-runner/select-tests
                (into selection ["--development-cache" (pr-str options)]))]
    (is (= options (:development-cache-options parsed)))
    (is (= ['gravity.compiler-pass-manifest-test] (:namespaces parsed)))
    (doseq [arguments [["--development-cache" (pr-str options)]
                       ["--list" "--development-cache" (pr-str options)]
                       (into selection ["--development-cache" "{}"])
                       (into selection ["--development-cache" (str (pr-str options) " {}")])
                       (into selection ["--development-cache"
                                        (pr-str (assoc options :trusted? true))])
                       (into selection ["--development-cache" (pr-str options)
                                        "--development-cache" (pr-str options)])]]
      (is (= "SH01-TEST-USAGE"
             (:id (exception-data #(child-runner/select-tests arguments))))))))

(deftest parent-launch-authority-and-expanded-child-provenance-remain-distinct
  (with-coordination-root
    (fn [base]
      (let [namespace 'gravity.self-hosting.sh01-child-policy-test
            plan (assoc-in (impact-plan namespace) [:shards 0 :resource-class] :normal)
            launch-options (atom [])
            test-producer {:revision {:commit "original-test-producer"}}
            selected-context (assoc (context base) :test-only? false
                                    :revision {:commit "current-parent"})]
        (with-redefs [wiring/prepare-context (fn [& _] selected-context)
                      runner/run-normal-batch-process
                      (fn [jobs options]
                        (swap! launch-options conj options)
                        (-> (one-member-batch-result (first jobs))
                            (assoc :child-jvm-launched? true)
                            (assoc-in [:result :child-cache-hit?] true)
                            (assoc-in [:result :tests-executed?] false)
                            (assoc-in [:result :test-producer-provenance] test-producer)))]
          (let [report (runner/execute-plan plan {:development-loop? true})
                result (first (:results report))]
            (is (= snapshot-id (get-in (first @launch-options)
                                      [:child-development-cache :repository-identity])))
            (is (false? (:cache-hit? result)))
            (is (true? (:child-cache-hit? result)))
            (is (false? (:tests-executed? result)))
            (is (= test-producer (:test-producer-provenance result)))
            (is (= {:commit "current-parent"}
                   (get-in result [:execution-provenance :revision]))))
          (doseq [[selected-plan options]
                  [[(assoc plan :authoritative? true) {}]
                   [(assoc plan :authority :authoritative) {}]
                   [plan {:fresh? true}]
                   [plan {:proof? true}]
                   [plan {:performance? true}]
                   [plan {:nondeterministic? true}]
                   [plan {:command ["custom-runner"]}]
                   [(assoc-in plan [:shards 0 :freshness-required?] true) {}]
                   [(assoc-in plan [:shards 0 :test-policy :proof?] true) {}]
                   [(assoc-in plan [:shards 0 :test-policy :deterministic?] false) {}]
                   [(assoc plan :authoritative? true)
                    {:child-development-cache {:forged true}}]]]
            (runner/execute-plan selected-plan (assoc options :development-loop? true))
            (is (nil? (:child-development-cache (last @launch-options))))))))))

(deftest unidentified-child-launch-never-reuses-production-results
  (with-coordination-root
    (fn [base]
      (let [external-deps (.resolve base "external-user-deps.edn")
            options {:cache-directory (str (.resolve base "cache"))
                     :broker-root (str (.resolve base "broker"))}
            selected-context (wiring/prepare-context "." options)
            plan (impact-plan 'gravity.self-hosting.sh01-unidentified-child-test)
            job (first (:shards plan))
            calls (atom 0)
            operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
        (is (= "SH01-DEVELOPMENT-LOOP-CONTEXT"
               (:id (exception-data
                     #(wiring/validate-context!
                       (assoc selected-context :test-only? true) "." {})))))
        (spit (str external-deps) "{:aliases {:test {:jvm-opts [\"-Xss256k\"]}}}")
        (let [first-request (wiring/cache-request selected-context plan job {})
              first-result (wiring/run-unit!
                            selected-context (assoc job :development-cache-request first-request)
                            operation)]
          ;; Neither a parent JVM identity nor a whole-repository snapshot binds
          ;; this external child-launch input. Do not pretend that it does.
          (spit (str external-deps) "{:aliases {:test {:jvm-opts [\"-Xss16m\"]}}}")
          (let [second-request (wiring/cache-request selected-context plan job {})
                second-result (wiring/run-unit!
                               selected-context (assoc job :development-cache-request second-request)
                               operation)]
            (is (= first-request second-request))
            (is (true? (get-in first-request [:test-policy :freshness-required?])))
            (is (= 2 @calls))
            (is (= "DEV-TEST-CACHE-INELIGIBLE"
                   (:id (exception-data #(cache/cache-key second-request)))))
            (doseq [result [first-result second-result]]
              (is (= :passed (:status result)))
              (is (false? (:cache-hit? result)))
              (is (true? (get-in result [:cache-receipt :producer-executed?])))
              (is (false? (get-in result [:cache-receipt :cacheable?])))
              (is (false? (get-in result [:cache-receipt :stored?])))
              (is (= :freshness-required-test (get-in result [:cache-receipt :reason]))))
            (is (nil? (:result (cache/lookup! second-request))))))))))

(deftest injected-context-must-match-current-root-and-command
  (with-coordination-root
    (fn [base]
      (let [selected-context (context base)
            validate-context
            #(wiring/validate-context! selected-context %1 %2)]
        (is (= selected-context
               (validate-context
                "/tmp"
                {:process-launcher identity
                 :development-operation-identity
                 {:id :test-operation :sha256 snapshot-id}})))
        (doseq [[working-directory options]
                [["/" {:process-launcher identity
                        :development-operation-identity
                        {:id :test-operation :sha256 snapshot-id}}]
                 ["/tmp" {:command ["/bin/false"]
                           :process-launcher identity
                           :development-operation-identity
                           {:id :test-operation :sha256 snapshot-id}}]]]
          (is (= "SH01-DEVELOPMENT-LOOP-CONTEXT"
                 (:id
                  (exception-data
                   #(validate-context working-directory options))))))))))

(deftest parent-hit-is-removed-before-broker-or-child-launch
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-hit-test)
            launches (atom 0)
            launcher (fn [_ _]
                       (swap! launches inc)
                       {:status :passed :exit-code 0})
            first-report (execute plan (context base) launcher)
            second-report (execute plan (context base) launcher)]
        (is (= 1 @launches))
        (is (= 1 (:planned-jvms first-report)))
        (is (= 1 (:post-cache-planned-jvms first-report)))
        (is (= 1 (:launched-jvms first-report)))
        (is (zero? (:post-cache-planned-jvms second-report)))
        (is (zero? (:launched-jvms second-report)))
        (is (= 1 (:cache-hit-jvms second-report)))
        (is (true? (:cache-hit? (first (:results second-report)))))
        (is (false? (:child-jvm-launched?
                     (first (:results second-report)))))
        (is (= :passed (:status second-report)))
        (is (= (:cache-receipts second-report)
               (vec (sort-by (juxt :namespaces :phase)
                             (:cache-receipts second-report)))))))))

(deftest parent-hit-expands-a-stored-normal-batch-result
  (with-coordination-root
    (fn [base]
      (let [namespace 'gravity.self-hosting.sh01-wiring-batch-hit-test
            plan (-> (impact-plan namespace)
                     (assoc-in [:shards 0 :slice] "SH-01")
                     (assoc-in [:shards 0 :resource-class] :normal))
            calls (atom 0)
            batch-worker (fn [jobs]
                           (swap! calls inc)
                           (one-member-batch-result (first jobs)))
            options {:normal-batch-worker batch-worker}
            first-report
            (execute plan (context base) (fn [_ _] (throw (Error.))) options)
            second-report
            (execute plan (context base) (fn [_ _] (throw (Error.))) options)]
        (is (= 1 @calls))
        (is (= :passed (:status first-report)))
        (is (= :passed (:status second-report)))
        (is (= [namespace] (mapv :namespace (:results second-report))))
        (is (= 1 (:cache-hit-jvms second-report)))
        (is (zero? (:post-cache-planned-jvms second-report)))))))

(deftest emitted-runner-report-separates-producer-and-applying-revisions
  (doseq [normal-batch? [false true]]
    (with-coordination-root
      (fn [base]
        (let [namespace 'gravity.self-hosting.sh01-wiring-revision-test
              plan (cond-> (impact-plan namespace)
                     normal-batch?
                     (assoc-in [:shards 0 :resource-class] :normal))
              producer {:commit "producer-A" :tree "producer-tree-A"}
              applying {:commit "applying-B" :tree "applying-tree-B"}
              calls (atom 0)
              launcher (fn [_ _]
                         (swap! calls inc)
                         {:status :passed :exit-code 0})
              options (when normal-batch?
                        {:normal-batch-worker
                         (fn [jobs]
                           (swap! calls inc)
                           (one-member-batch-result (first jobs)))})
              original (execute plan (assoc (context base) :revision producer)
                                launcher options)
              reused (execute plan (assoc (context base) :revision applying)
                              launcher options)
              ;; Exercise the serialized public report, not only report-facts.
              emitted (edn/read-string (pr-str reused))]
          (is (= 1 @calls))
          (is (= :passed (:status original)))
          (is (= :passed (:status emitted)))
          (is (= producer (:revision original)))
          (is (= applying (:revision emitted)))
          (is (= producer (get-in emitted [:results 0 :execution-provenance :revision])))
          (is (= (:snapshot (context base))
                 (get-in emitted [:results 0 :execution-provenance :repository-snapshot])))
          (is (= 1 (:cache-hit-jvms emitted)))
          (is (zero? (:launched-jvms emitted)))
          (is (false? (get-in emitted [:results 0 :child-jvm-launched?])))
          (is (false? (:authoritative? emitted))))))))

(deftest all-miss-parent-probe-defers-verification-to-producer
  (with-coordination-root
    (fn [base]
      (let [namespace 'gravity.self-hosting.sh07-wiring-all-miss-test
            plan (impact-plan namespace)
            selected-context (assoc (context base) :revalidate-snapshot? true)
            report
            (with-redefs [wiring/repository-snapshot
                          (fn [_] (:snapshot selected-context))]
              (execute plan selected-context
                       (fn [_ _] {:status :passed :exit-code 0})))]
        (is (= :passed (:status report)))
        (is (= [namespace] (mapv :namespace (:results report))))
        (is (= 1 (:launched-jvms report)))
        (is (zero? (:cache-hit-jvms report)))
        (is (= [:miss :miss]
               (mapv #(get-in % [:receipt :decision])
                     (:cache-receipts report))))
        (is (= {:full-snapshot-invocations 1
                :full-snapshot-path-observations 1
                :phases {:producer-post-operation 1}}
               (:snapshot-telemetry report)))))))

(deftest conserved-explicit-inputs-admit-real-results-across-unrelated-edits
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            selected-context (assoc (context base)
                                    :working-directory (str root)
                                    :revalidate-snapshot? true
                                    :revision {:commit "original-commit" :tree "original-tree"}
                                    :runtime-tool-inputs
                                    [{:id :test-runtime :sha256 snapshot-id}])
            plan (impact-plan (:namespace job))
            classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                  'explicit-classpath-identities)
            calls (atom 0)
            unrelated (.resolve root "unrelated.md")]
        (with-redefs-fn
          {classpath (fn [_ closure-root]
                       [{:path "classpath/entry-0000" :sha256 closure-root}])
           #'wiring/repository-snapshot
           (fn [_] (throw (Error. "explicit work read the whole tree")))}
          (fn []
            (let [attached (wiring/attach-request selected-context plan {} job)
                  operation (fn []
                              (swap! calls inc)
                              (spit (str unrelated) "unrelated change during execution")
                              {:status :passed :exit-code 0})
                  result (wiring/run-unit! selected-context attached operation)]
              (is (= :passed (:status result)))
              (is (true? (get-in result [:cache-receipt :stored?])))
              (spit (str unrelated) "another unrelated revision")
              (is (:hit-result (wiring/probe selected-context attached)))
              (is (:hit-result (first (wiring/probe-batch selected-context [attached]))))
              (let [later-context (assoc selected-context :revision
                                         {:commit "later-commit" :tree "later-tree"})
                    reused (wiring/run-unit! later-context attached operation)]
                (is (:cache-hit? reused))
                (is (= (:revision selected-context)
                       (get-in reused [:execution-provenance :revision])))
                (is (= (:repository-identity (:development-cache-request attached))
                       (get-in reused [:execution-provenance :consumed-input-identity])))
                (is (= (:revision later-context)
                       (:revision (wiring/report-facts later-context)))))
              (is (= 1 @calls))
              (is (false? (:authoritative? result)))
              (is (false? (get-in result [:cache-receipt :proof-authority?]))))))))))

(deftest changed-explicit-inputs-reject-hits-and-reexecute-new-requests
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            selected-context (assoc (context base)
                                    :working-directory (str root)
                                    :revalidate-snapshot? true
                                    :runtime-tool-inputs
                                    [{:id :test-runtime :sha256 snapshot-id}])
            plan (impact-plan (:namespace job))
            classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                  'explicit-classpath-identities)
            calls (atom 0)
            operation #(do (swap! calls inc) {:status :passed :exit-code 0})]
        (with-redefs-fn
          {classpath (fn [_ closure-root]
                       [{:path "classpath/entry-0000" :sha256 closure-root}])}
          (fn []
            (doseq [role [:production :test :runner :contract :classpath]
                    :let [relative (:path (first (filter #(= role (:role %))
                                                         (get-in job [:cache-closure :inputs]))))
                          path (.resolve root ^String relative)
                          original (Files/readAllBytes path)
                          attached (wiring/attach-request selected-context plan {} job)]]
              (wiring/run-unit! selected-context attached operation)
              (spit (str path) "\n; changed consumed input\n" :append true)
              (let [failure (exception-data #(wiring/probe selected-context attached))
                    renewed (wiring/attach-request selected-context plan {} job)
                    before @calls]
                (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure)) (str role))
                (is (= :explicit-closure (:input-kind failure)) (str role))
                (is (not= (cache/cache-key (:development-cache-request attached))
                          (cache/cache-key (:development-cache-request renewed))))
                (is (= :passed (:status (wiring/run-unit! selected-context renewed operation))))
                (is (= (inc before) @calls)))
              (Files/write path original (make-array java.nio.file.OpenOption 0)))))))))

(deftest explicit-producer-mutation-never-publishes-a-pass
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            selected-context (assoc (context base) :working-directory (str root)
                                    :revalidate-snapshot? true
                                    :runtime-tool-inputs
                                    [{:id :test-runtime :sha256 snapshot-id}])
            classpath (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                  'explicit-classpath-identities)]
        (with-redefs-fn
          {classpath (fn [_ closure-root]
                       [{:path "classpath/entry-0000" :sha256 closure-root}])}
          (fn []
            (let [attached (wiring/attach-request selected-context (impact-plan (:namespace job)) {} job)
                  failure (exception-data
                           #(wiring/run-unit!
                             selected-context attached
                             (fn []
                               (spit (str (.resolve root "bootstrap/clojure/src/gravity/c11_mir.clj"))
                                     "\n; mutation during execution\n" :append true)
                               {:status :passed :exit-code 0})))]
              (is (= :explicit-closure (:input-kind failure)))
              (is (= :miss (get-in (cache/lookup! (:development-cache-request attached))
                                   [:receipt :decision]))))))))))

(deftest explicit-closure-keeps-runtime-shadow-inventory-and-freshness-guards
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            job (reviewed-closure-job 'gravity.c11-mir-test)
            _ (Files/createDirectories root no-file-attributes)
            _ (copy-reviewed-closure! root job)
            selected-context (assoc (context base) :working-directory (str root)
                                    :revalidate-snapshot? true
                                    :runtime-tool-inputs
                                    [{:id :test-runtime :sha256 snapshot-id}])
            plan (impact-plan (:namespace job))
            resolve-wiring #(ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring %)
            verify-inputs (resolve-wiring 'verify-inputs!)]
        (with-redefs-fn
          {(resolve-wiring 'explicit-classpath-identities)
           (fn [_ closure-root]
             [{:path "classpath/entry-0000" :sha256 closure-root}])
           (resolve-wiring 'base-runtime-identities) (fn [& _] [])
           #'wiring/repository-snapshot
           (fn [_] (assoc (:snapshot selected-context) :repository-identity "changed-tree"))}
          (fn []
            (let [attached (wiring/attach-request selected-context plan {} job)]
              (is (= :runtime-tool
                     (:input-kind
                      (exception-data
                       #(verify-inputs (assoc selected-context :revalidate-external-inputs? true)
                                       :runtime-check [attached])))))
              (doseq [relative ["bootstrap/clojure/src/gravity/c11_mir.cljc"
                                "bootstrap/clojure/test/gravity/self_hosting/sh01_added_test.clj"]]
                (let [path (.resolve root ^String relative)]
                  (spit (str path) "; new shadow or inventory input\n")
                  (is (= :explicit-closure
                         (:input-kind (exception-data
                                       #(verify-inputs selected-context :changed-input [attached]))))
                      relative)
                  (Files/delete path)))
              (doseq [options [{:fresh? true} {:proof? true} {:authoritative? true}]]
                (let [fresh (wiring/attach-request selected-context plan options job)]
                  (is (= :repository
                         (:input-kind (exception-data
                                       #(verify-inputs selected-context :fresh-check [fresh]))))
                      (pr-str options))
                  (is (= :repository
                         (:input-kind (exception-data
                                       #(verify-inputs selected-context :mixed-check [attached fresh]))))
                      (pr-str options)))))))))))

(deftest parent-probes-share-one-verification-before-hit-admission
  (with-coordination-root
    (fn [base]
      (let [namespaces
            '[gravity.self-hosting.sh07-wiring-batch-a-test
              gravity.self-hosting.sh07-wiring-batch-b-test
              gravity.self-hosting.sh07-wiring-batch-c-test]
            shards
            (mapv (fn [namespace]
                    {:namespace namespace
                     :slice "SH-07"
                     :resource-class :memory-heavy
                     :component-id (str namespace)
                     :batch-key (str "component/wiring-test/" namespace)
                     :test-policy (reviewed-policy)})
                  namespaces)
            full-plan {:schema :gravity/sh01-impact-test-plan-v1
                       :authority :non-authoritative
                       :authoritative? false
                       :namespaces namespaces
                       :shards shards}
            first-plan (assoc full-plan
                              :namespaces [(first namespaces)]
                              :shards [(first shards)])
            launches (atom 0)
            launcher (fn [_ _]
                       (swap! launches inc)
                       {:status :passed :exit-code 0})]
        (execute first-plan (context base) launcher)
        (let [mixed-context (assoc (context base) :revalidate-snapshot? true)
              mixed-report
              (with-redefs [wiring/repository-snapshot
                            (fn [_] (:snapshot mixed-context))]
                (execute full-plan mixed-context launcher))]
          (is (= 3 @launches))
          (is (= 1 (:cache-hit-jvms mixed-report)))
          (is (= 2 (:launched-jvms mixed-report)))
          (is (= {:full-snapshot-invocations 3
                  :full-snapshot-path-observations 3
                  :phases {:parent-probe-batch 1
                           :producer-post-operation 2}}
                 (:snapshot-telemetry mixed-report))))
        (let [warm-context (assoc (context base) :revalidate-snapshot? true)
              warm-report
              (with-redefs [wiring/repository-snapshot
                            (fn [_] (:snapshot warm-context))]
                (execute full-plan warm-context launcher))]
          (is (= 3 @launches))
          (is (= 3 (:cache-hit-jvms warm-report)))
          (is (zero? (:launched-jvms warm-report)))
          (is (= {:full-snapshot-invocations 1
                  :full-snapshot-path-observations 1
                  :phases {:parent-probe-batch 1}}
                 (:snapshot-telemetry warm-report))))))))

(deftest mutation-during-parent-probes-rejects-before-result-or-launch
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-probe-race-test)
            launches (atom 0)
            launcher (fn [_ _]
                       (swap! launches inc)
                       {:status :passed :exit-code 0})]
        (execute plan (context base) launcher)
        (let [selected-context (assoc (context base) :revalidate-snapshot? true)
              changed-snapshot
              (assoc (:snapshot selected-context)
                     :repository-identity
                     (str "sha256:" (apply str (repeat 64 "b"))))
              failure
              (with-redefs [wiring/repository-snapshot
                            (fn [_] changed-snapshot)]
                (exception-data #(execute plan selected-context launcher)))]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" (:id failure)))
          (is (= :repository (:input-kind failure)))
          (is (= 1 @launches))
          (is (= {:parent-probe-batch 1}
                 (:phases @(:snapshot-telemetry selected-context)))))))))

(deftest standalone-and-secondary-hits-retain-race-validation
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-secondary-hit-test)
            selected-context (assoc (context base) :revalidate-snapshot? true)
            job (wiring/attach-request
                 selected-context plan
                 {:development-operation-identity
                  {:id :test-operation :sha256 snapshot-id}}
                 (first (:shards plan)))
            changed-snapshot
            (assoc (:snapshot selected-context)
                   :repository-identity
                   (str "sha256:" (apply str (repeat 64 "b"))))
            hit {:receipt {:decision :hit
                           :producer-executed? false
                           :cacheable? true}
                 :result {:status :passed :exit-code 0}}]
        (with-redefs [cache/lookup! (fn [_] hit)
                      wiring/repository-snapshot (fn [_] changed-snapshot)]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                 (:id (exception-data #(wiring/probe selected-context job))))))
        (with-redefs [cache/lookup-or-run! (fn [_ _] hit)
                      wiring/repository-snapshot (fn [_] changed-snapshot)]
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                 (:id
                  (exception-data
                   #(wiring/run-unit!
                     selected-context job
                     (fn [] (throw (Error. "secondary hit ran producer")))))))))
        (is (= {:standalone-parent-hit 1
                :secondary-singleflight-hit 1}
               (:phases @(:snapshot-telemetry selected-context))))))))

(deftest concurrent-identical-checks-retain-one-producer
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-race-test)
            launches (atom 0)
            ready (CountDownLatch. 2)
            release (CountDownLatch. 1)
            launcher (fn [_ _]
                       (swap! launches inc)
                       (.countDown ready)
                       (.await release 500 TimeUnit/MILLISECONDS)
                       {:status :passed :exit-code 0})
            first-context (assoc (context base) :revalidate-snapshot? true)
            second-context (assoc (context base) :revalidate-snapshot? true)]
        (with-redefs [wiring/repository-snapshot
                      (fn [_] (:snapshot first-context))]
          (let [first (future (execute plan first-context launcher))
                second (future (execute plan second-context launcher))]
            ;; Only the producer reaches the launch boundary; release it after
            ;; the second parent has entered the same-key cache path.
            (.await ready 100 TimeUnit/MILLISECONDS)
            (Thread/sleep 50)
            (.countDown release)
            (let [reports [@first @second]]
              (is (= 1 @launches))
              (is (every? #(= :passed (:status %)) reports))
              (is (= 1 (reduce + (map :launched-jvms reports))))
              (is (= 1 (reduce + (map :cache-hit-jvms reports))))
              (is (zero? (reduce +
                                  (map #(get-in % [:snapshot-telemetry
                                                   :phases
                                                   :parent-probe-batch]
                                                0)
                                       reports))))
              (is (= 1 (reduce +
                               (map #(get-in % [:snapshot-telemetry
                                                :phases
                                                :producer-post-operation]
                                             0)
                                    reports))))
              (is (= 1 (reduce +
                               (map #(get-in % [:snapshot-telemetry
                                                :phases
                                                :secondary-singleflight-hit]
                                             0)
                                    reports)))))))))))

(deftest launcher-start-failure-has-no-false-launch-and-keeps-receipts
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-start-error-test)
            attempts (atom 0)
            report
            (execute
             plan
             (context base)
             (fn [_ _]
               (swap! attempts inc)
               (throw (java.io.IOException. "injected start failure"))))]
        (is (= 1 @attempts))
        (is (zero? (:launched-jvms report)))
        (is (= :failed (:status report)))
        (is (= 1 (count (:cache-receipts report))))
        (is (= :miss
               (get-in report [:cache-receipts 0 :receipt :decision])))
        (is (= [:admitted :released]
               (mapv :outcome (:broker-receipts report))))
        (is (false? (:child-jvm-launched? (first (:results report)))))))))

(deftest repository-change-after-child-run-prevents-cache-publication
  (with-coordination-root
    (fn [base]
      (let [plan (impact-plan 'gravity.self-hosting.sh07-wiring-race-close-test)
            selected-context (assoc (context base) :revalidate-snapshot? true)
            launches (atom 0)
            changed-snapshot
            (assoc (:snapshot selected-context)
                   :repository-identity
                   (str "sha256:" (apply str (repeat 64 "b"))))]
        (with-redefs [wiring/repository-snapshot
                      (fn [_] changed-snapshot)]
          (let [report
                (execute plan selected-context
                         (fn [_ _]
                           (swap! launches inc)
                           {:status :passed :exit-code 0}))]
            (is (= :failed (:status report)))
            (is (= 1 (:launched-jvms report)))
            (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                   (:diagnostic-id (first (:results report)))))
            (is (true? (:child-jvm-launched? (first (:results report)))))
            (is (= [:admitted :released]
                   (mapv :outcome (:broker-receipts report))))))
        (let [retry-report
              (execute plan (context base)
                       (fn [_ _]
                         (swap! launches inc)
                         {:status :passed :exit-code 0}))]
          (is (= :passed (:status retry-report)))
          (is (= 2 @launches))
          (is (zero? (:cache-hit-jvms retry-report))))))))

(deftest failure-timeout-and-excluded-policies-always-execute
  (doseq [{:keys [label plan-options policy-options runner-options result
                  reason]}
          [{:label :failure
            :result {:status :failed :exit-code 1}
            :reason :result-not-reusable}
           {:label :timeout
            :result {:status :timeout :exit-code 124}
            :reason :result-not-reusable}
           {:label :fresh
            :runner-options {:fresh? true}
            :result {:status :passed :exit-code 0}
            :reason :freshness-required-test}
           {:label :authoritative
            :plan-options {:authority :authoritative
                           :authoritative? true}
            :result {:status :passed :exit-code 0}
            :reason :authoritative-test}
           {:label :authoritative-option
            :runner-options {:authoritative? true}
            :result {:status :passed :exit-code 0}
            :reason :authoritative-test}
           {:label :performance
            :policy-options {:performance? true}
            :result {:status :passed :exit-code 0}
            :reason :performance-test}
           {:label :nondeterministic
            :policy-options {:deterministic? false}
            :result {:status :passed :exit-code 0}
            :reason :nondeterministic-test}
           {:label :unidentified-launcher
            :runner-options {:development-operation-identity nil}
            :result {:status :passed :exit-code 0}
            :reason :nondeterministic-test}]]
    (testing (name label)
      (with-coordination-root
        (fn [base]
          (let [namespace (symbol (str "gravity.self-hosting.sh07-wiring-"
                                       (name label) "-test"))
                plan (impact-plan namespace plan-options)
                plan (assoc-in plan [:shards 0 :test-policy]
                               (reviewed-policy policy-options))
                launches (atom 0)
                launcher (fn [_ _] (swap! launches inc) result)
                reports
                [(execute plan (context base) launcher runner-options)
                 (execute plan (context base) launcher runner-options)]]
            (is (= 2 @launches) label)
            (is (= [1 1] (mapv :launched-jvms reports)) label)
            (is (every? zero? (map :cache-hit-jvms reports)) label)
            (is (= reason
                   (->> (:cache-receipts (second reports))
                        (filter #(= :execution (:phase %)))
                        first
                        :receipt
                        :reason))
                label)))))))

(defn- write-loader-archive! [^Path path class-path entries]
  (let [manifest (Manifest.)
        attributes (.getMainAttributes manifest)]
    (.put attributes Attributes$Name/MANIFEST_VERSION "1.0")
    (when (some? class-path)
      (.put attributes Attributes$Name/CLASS_PATH class-path))
    (with-open [archive (JarOutputStream. (Files/newOutputStream path
                                          (make-array java.nio.file.OpenOption 0)) manifest)]
      (doseq [[name content] entries]
        (.putNextEntry archive (JarEntry. name))
        (.write archive (.getBytes ^String content "UTF-8"))
        (.closeEntry archive)))))

(deftest manifest-class-path-resources-are-not-covered-by-listed-archive-bytes
  (with-coordination-root
    (fn [base]
      (let [root (.resolve base "repository")
            archive (.resolve base "listed.jar")
            dependency (.resolve base "dependency.jar")
            malformed (.resolve base "malformed.jar")
            link (.resolve base "archive-link.jar")
            accepted? (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                 'archive-loader-inputs-contained?)
            identities (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                   'explicit-classpath-identities)
            file-identity (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                      'regular-file-identity)
            old-classpath (System/getProperty "java.class.path")
            separator (System/getProperty "path.separator")
            resource-content (fn []
                               (with-open [loader (URLClassLoader.
                                                    (into-array URL [(.toURL (.toUri archive))])
                                                    nil)]
                                 (slurp (.getResource loader "dependency-marker.txt"))))]
        (Files/createDirectories root no-file-attributes)
        (write-loader-archive! archive nil {})
        (try
          (System/setProperty "java.class.path"
            (str (.resolve root "bootstrap/clojure/src") separator
                 (.resolve root "bootstrap/clojure/test") separator archive))
          (is (true? (accepted? archive)))
          (is (= 3 (count (identities root snapshot-id))))
          (write-loader-archive! archive "dependency.jar" {})
          (write-loader-archive! dependency nil {"dependency-marker.txt" "first"})
          (let [listed-before (file-identity archive)]
            (is (= "first" (resource-content)))
            (is (false? (accepted? archive)))
            (is (nil? (identities root snapshot-id)))
            (write-loader-archive! dependency nil {"dependency-marker.txt" "other"})
            (is (= "other" (resource-content)))
            (is (= listed-before (file-identity archive)))
            (is (false? (accepted? archive)))
            (is (nil? (identities root snapshot-id))))
          ;; Even empty or unresolvable declarations are outside this domain.
          (doseq [value ["" "absent.jar" "https://example.invalid/dependency.jar" "../classes/"]]
            (write-loader-archive! archive value {})
            (is (false? (accepted? archive)))
            (is (nil? (identities root snapshot-id))))
          (spit (str malformed) "not an archive")
          (Files/createSymbolicLink link archive no-file-attributes)
          (doseq [path [malformed link root (.resolve base "missing.jar")]]
            (is (false? (accepted? path))))
          (finally (System/setProperty "java.class.path" old-classpath)))))))

(deftest actual-runtime-observer-rejects-transitive-and-unknown-archives
  (with-coordination-root
    (fn [base]
      (let [archive (.resolve base "runtime.jar")
            root (.resolve base "repository")
            basis-path (.resolve base "basis.edn")
            current {:basis-config {} :argmap {:main-opts ["-m" "gravity.self-hosting-test-runner"]}}
            arguments ["--namespace" "gravity.compiler-pass-manifest-test"]
            observe (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                'effective-child-runtime)
            jvm-args (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                 'jvm-input-arguments)
            startup (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                'child-startup-resources-absent?)
            keys ["java.class.path" "sun.java.command" "clojure.basis"]
            original (into {} (map (fn [key] [key (System/getProperty key)]) keys))
            separator (System/getProperty "path.separator")]
        (Files/createDirectories root no-file-attributes)
        (spit (str basis-path) "{}")
        (write-loader-archive! archive nil {})
        (try
          (System/setProperty "java.class.path"
            (str (.resolve root "bootstrap/clojure/src") separator
                 (.resolve root "bootstrap/clojure/test") separator archive))
          (System/setProperty "clojure.basis" (str basis-path))
          (System/setProperty "sun.java.command"
            (str/join " " (into ["clojure.main" "-m" "gravity.self-hosting-test-runner"] arguments)))
          (with-redefs-fn {#'basis/current-basis (constantly current)
                          #'basis/initial-basis (constantly current)
                          jvm-args (constantly []) startup (constantly true)}
            (fn []
              (is (= :actual-child-runtime-v1 (:id (observe (str root) arguments))))
              (write-loader-archive! archive "outside.jar" {})
              (is (nil? (observe (str root) arguments)))
              (spit (str archive) "unreadable archive structure")
              (is (nil? (observe (str root) arguments)))
              (Files/delete archive)
              (is (nil? (observe (str root) arguments)))))
          (finally
            (doseq [[key value] original]
              (if value (System/setProperty key value) (System/clearProperty key)))))))))

(deftest unlisted-context-resources-change-without-changing-the-declared-classpath
  (with-coordination-root
    (fn [base]
      (let [thread (Thread/currentThread)
            original (.getContextClassLoader thread)
            classpath (System/getProperty "java.class.path")
            marker (.resolve base "unlisted-loader-marker.txt")
            supported? (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                   'supported-resource-loaders?)
            observations (atom 0)
            lookups (atom 0)
            operations (atom 0)]
        (with-open [loader (URLClassLoader. (into-array URL [(.toURL (.toUri base))])
                                           (ClassLoader/getSystemClassLoader))]
          (try
            (.setContextClassLoader thread loader)
            (spit (str marker) "first")
            (is (= "first" (slurp (io/resource "unlisted-loader-marker.txt"))))
            (spit (str marker) "other")
            (is (= "other" (slurp (io/resource "unlisted-loader-marker.txt"))))
            (is (= classpath (System/getProperty "java.class.path")))
            (is (false? (supported?)))
            (with-redefs [io/resource (fn [& _] (swap! observations inc)
                                       (throw (ex-info "startup resource lookup must not run" {})))
                          basis/current-basis (fn [] (swap! observations inc)
                                                (throw (ex-info "basis lookup must not run" {})))
                          cache/lookup-or-run! (fn [& _] (swap! lookups inc)
                                                (throw (ex-info "cache lookup must not run" {})))]
              (dotimes [_ 2]
                (let [result (wiring/run-child-unit!
                               ['gravity.compiler-pass-manifest-test]
                               {:arguments ["not-the-actual-cli"]}
                               #(do (swap! operations inc) {:status :passed :exit-code 0}))]
                  (is (true? (:tests-executed? result)))
                  (is (false? (:child-cache-hit? result)))
                  (is (= :unreviewed-resource-loaders
                         (get-in result [:child-cache-receipt :reason]))))))
            (is (= 2 @operations))
            (is (zero? @observations))
            (is (zero? @lookups))
            (finally (.setContextClassLoader thread original))))))))

(deftest compiler-resource-loader-url-addition-invalidates-the-reviewed-domain
  (with-coordination-root
    (fn [base]
      (let [thread (Thread/currentThread)
            original (.getContextClassLoader thread)
            system (ClassLoader/getSystemClassLoader)
            supported? (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                   'supported-resource-loaders?)
            observe (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                                'effective-child-runtime)
            marker (.resolve base "compiler-loader-marker.txt")
            callbacks (atom 0)]
        (spit (str marker) "compiler resource")
        (try
          (.setContextClassLoader thread system)
          (with-open [loader (clojure.lang.DynamicClassLoader. system)]
            (with-bindings {clojure.lang.Compiler/LOADER loader}
              (is (true? (supported?)))
              (is (nil? (.getResource loader "compiler-loader-marker.txt")))
              (.addURL loader (.toURL (.toUri base)))
              (is (false? (supported?)))
              (with-open [input (clojure.lang.RT/resourceAsStream
                                  (clojure.lang.RT/baseLoader) "compiler-loader-marker.txt")]
                (is (= "compiler resource" (slurp input))))
              (with-redefs [basis/current-basis (fn [] (swap! callbacks inc)
                                                  (throw (ex-info "must reject before basis" {})))]
                (is (nil? (observe "." []))))))
          ;; Exact class matters: an empty URL list cannot authorize arbitrary delegation.
          (with-open [custom (proxy [clojure.lang.DynamicClassLoader] [system]
                               (getURLs [] (swap! callbacks inc)
                                 (throw (ex-info "custom loader callback must not run" {}))))]
            (with-bindings {clojure.lang.Compiler/LOADER custom}
              (is (false? (supported?))))
            (.setContextClassLoader thread custom)
            (is (false? (supported?))))
          (.setContextClassLoader thread nil)
          (is (false? (supported?)))
          (.setContextClassLoader thread system)
          (with-bindings {clojure.lang.Compiler/LOADER nil}
            (is (false? (supported?))))
          (is (zero? @callbacks))
          (finally (.setContextClassLoader thread original)))))))
