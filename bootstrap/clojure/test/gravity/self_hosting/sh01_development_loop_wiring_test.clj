(ns gravity.self-hosting.sh01-development-loop-wiring-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.self-hosting.sh01-development-test-cache :as cache]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner])
  (:import [java.nio.file Files LinkOption Path StandardCopyOption]
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

(defmacro with-coordination-root
  [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-development-loop-wiring-"
                   no-file-attributes)]
     (try
       (Files/setPosixFilePermissions
        ~binding
        (PosixFilePermissions/fromString "rwx------"))
       ~@body
       (finally (delete-tree! ~binding)))))

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
     :component-id (if (= namespace 'gravity.c11-mir-test)
                     "c11-mir"
                     "compiler-pass-manifest")
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
   & [policy-overrides command]]
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
         :authority :non-authoritative
         :authoritative? false
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
          command (assoc :command command))))))

(deftest repository-snapshot-is-stable-and-complete-before-launch
  (let [first-snapshot (wiring/repository-snapshot ".")
        second-snapshot (wiring/repository-snapshot ".")]
    (is (= first-snapshot second-snapshot))
    (is (= :gravity/sh01-complete-repository-snapshot-v1
           (:schema first-snapshot)))
    (is (pos? (:path-count first-snapshot)))
    (is (boolean
         (re-matches #"sha256:[0-9a-f]{64}"
                     (:repository-identity first-snapshot))))
    (is (= "SH01-DEVELOPMENT-LOOP-ROOT"
           (:id (exception-data
                 #(wiring/repository-snapshot "bootstrap")))))))

(deftest repository-snapshot-detects-content-and-inventory-without-metadata-help
  (with-coordination-root [base]
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
      (let [first-snapshot (wiring/repository-snapshot repository-root)
            original-time (Files/getLastModifiedTime tracked no-links)]
        (Files/write tracked (.getBytes "other" "UTF-8")
                     (make-array java.nio.file.OpenOption 0))
        (Files/setLastModifiedTime tracked original-time)
        (let [content-snapshot (wiring/repository-snapshot repository-root)]
          (is (not= (:repository-identity first-snapshot)
                    (:repository-identity content-snapshot)))
          (Files/write untracked (.getBytes "inventory" "UTF-8")
                       (make-array java.nio.file.OpenOption 0))
          (let [inventory-snapshot
                (wiring/repository-snapshot repository-root)]
            (is (= (inc (:path-count content-snapshot))
                   (:path-count inventory-snapshot)))
            (is (not= (:repository-identity content-snapshot)
                      (:repository-identity inventory-snapshot)))))))))

(deftest external-classpath-command-bytes-and-mode-affect-identities
  (with-coordination-root [base]
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
          (System/setProperty "java.class.path" original-classpath))))))

(deftest repository-symlink-input-fails-closed
  (with-coordination-root [base]
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
                 "link.txt"))))))))

(deftest repository-executable-mode-affects-identity
  (with-coordination-root [base]
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
        (is (not= non-executable-identity (identity)))))))

(deftest reviewed-leaf-closure-is-path-neutral-and-content-complete
  (with-coordination-root [base]
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
                     "gravity.c11-mir-test"]))))))))

(deftest reviewed-leaf-closure-reuses-only-identical-declared-inputs
  (with-coordination-root [base]
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
            (is (= 2 @calls))))))))

(deftest invalid-or-undeclared-closure-retains-full-repository-key
  (with-coordination-root [base]
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
               repository-root reviewed-job full-id)))))))

(deftest explicit-closure-read-race-and-symlink-fail-closed
  (with-coordination-root [base]
    (let [repository-root (.resolve base "worktree")
          job (reviewed-closure-job 'gravity.c11-mir-test)
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
                      "bootstrap/clojure/src/gravity/c11_mir.clj")
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
                   repository-root job snapshot-id)))))))))

(deftest required-closure-input-deletion-cannot-produce-a-cache-request
  (with-coordination-root [base]
    (let [repository-root (.resolve base "worktree")
          coordination-root (.resolve base "deletion-race-coordination")
          job (reviewed-closure-job 'gravity.c11-mir-test)
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
          (is (empty? (vec (.toArray entries)))))))))

(deftest prepared-context-revalidates-external-inputs
  (with-coordination-root [base]
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
          (is (= :runtime-tool (:input-kind failure))))))))

(deftest injected-context-must-match-current-root-and-command
  (with-coordination-root [base]
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
                 #(validate-context working-directory options)))))))))

(deftest parent-hit-is-removed-before-broker-or-child-launch
  (with-coordination-root [base]
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
                           (:cache-receipts second-report))))))))

(deftest parent-hit-expands-a-stored-normal-batch-result
  (with-coordination-root [base]
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
      (is (zero? (:post-cache-planned-jvms second-report))))))

(deftest all-miss-parent-probe-defers-verification-to-producer
  (with-coordination-root [base]
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
             (:snapshot-telemetry report))))))

(deftest parent-probes-share-one-verification-before-hit-admission
  (with-coordination-root [base]
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
               (:snapshot-telemetry warm-report)))))))

(deftest mutation-during-parent-probes-rejects-before-result-or-launch
  (with-coordination-root [base]
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
               (:phases @(:snapshot-telemetry selected-context))))))))

(deftest standalone-and-secondary-hits-retain-race-validation
  (with-coordination-root [base]
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
             (:phases @(:snapshot-telemetry selected-context)))))))

(deftest concurrent-identical-checks-retain-one-producer
  (with-coordination-root [base]
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
                                  reports))))))))))

(deftest launcher-start-failure-has-no-false-launch-and-keeps-receipts
  (with-coordination-root [base]
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
      (is (false? (:child-jvm-launched? (first (:results report))))))))

(deftest repository-change-after-child-run-prevents-cache-publication
  (with-coordination-root [base]
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
        (is (zero? (:cache-hit-jvms retry-report)))))))

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
      (with-coordination-root [base]
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
              label))))))
