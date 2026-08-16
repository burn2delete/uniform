(ns gravity.darwin-publication-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.darwin-publication :as publication])
  (:import [java.nio.file Files LinkOption OpenOption Path Paths
            StandardCopyOption StandardOpenOption]
           [java.nio.file.attribute FileAttribute PosixFilePermission]
           [java.nio.charset StandardCharsets]
           [java.util UUID]))

(def ^:private file-names
  ["program.c" "program.h" "program.o" "program"
   "manifest.edn" "provenance.edn" "conformance.edn"])

(def ^:private owner-only-permissions
  #{PosixFilePermission/OWNER_READ
    PosixFilePermission/OWNER_WRITE
    PosixFilePermission/OWNER_EXECUTE})

(defn- private-var
  [symbol]
  (or (get (ns-interns 'gravity.darwin-publication) symbol)
      (throw (AssertionError. (str "missing private var " symbol)))))

(defn- provider-state
  [context]
  ((var-get (private-var 'context-state!)) context :test-inspection))

(defn- test-file-specs
  []
  (into
   {}
   (map
    (fn [name]
      [name
       {:bytes (.getBytes (str "descriptor-publication:" name "\n")
                          StandardCharsets/UTF_8)
        :mode (if (= name "program") 0755 0644)}]))
   file-names))

(defn- delete-tree!
  [root]
  (when (and root
             (Files/exists root (make-array LinkOption 0)))
    (with-open [stream
                (Files/walk root
                            (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (iterator-seq (.iterator stream))))]
        (Files/deleteIfExists path)))))

(defn- with-private-temp-directory
  [prefix function]
  (let [root
        (Files/createTempDirectory
         (Paths/get "/private/tmp" (make-array String 0))
         prefix (make-array FileAttribute 0))]
    (Files/setPosixFilePermissions root owner-only-permissions)
    (try
      (function root)
      (finally
        (delete-tree! root)))))

(defn- failure-data
  [function]
  (try
    (function)
    ::unexpected-success
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- directory-names
  [directory]
  (with-open [stream (Files/list directory)]
    (set (map #(str (.getFileName ^Path %))
              (iterator-seq (.iterator stream))))))

(defn- write-bytes!
  [path bytes]
  (Files/write path bytes
               (into-array OpenOption
                           [StandardOpenOption/CREATE_NEW
                            StandardOpenOption/WRITE])))

(defn- run-command!
  [arguments]
  (let [process
        (.start
         (doto (ProcessBuilder. ^java.util.List (vec arguments))
           (.redirectErrorStream true)))
        _ (.close (.getOutputStream process))
        finished? (.waitFor process 30 java.util.concurrent.TimeUnit/SECONDS)]
    (when-not finished?
      (.destroyForcibly process)
      (.waitFor process 10 java.util.concurrent.TimeUnit/SECONDS))
    (let [output (slurp (.getInputStream process))
          exit-code (.exitValue process)]
      (when-not (and finished? (zero? exit-code))
        (throw (ex-info "test command failed"
                        {:arguments arguments :exit-code exit-code
                         :output output})))
      output)))

(def ^:private broad-everyone-acl
  (str "everyone allow read,write,execute,delete,append,readattr,writeattr,"
       "readextattr,writeextattr,readsecurity,writesecurity,chown"))

(def ^:private inheritable-everyone-acl
  (str broad-everyone-acl ",file_inherit,directory_inherit"))

(defn- add-acl!
  [path acl]
  (run-command! ["/bin/chmod" "+a" acl (.toString ^Path path)]))

(defn- remove-acls!
  [path]
  (when (Files/exists path (make-array LinkOption 0))
    (run-command! ["/bin/chmod" "-RN" (.toString ^Path path)])))

(defn- failure-record
  [function]
  (try
    (function)
    ::unexpected-success
    (catch clojure.lang.ExceptionInfo error
      {:data (ex-data error)
       :suppressed (mapv ex-data (.getSuppressed error))})))

(deftest namespace-contract-is-lazy-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.darwin-publication) 'namespace-contract)
        contract (var-get contract-var)
        source-path
        "bootstrap/clojure/src/gravity/darwin_publication.clj"
        source (slurp source-path)]
    (is (= #{'open-target! 'stage-bundle! 'commit-staged-bundle!
             'verify-published-bundle! 'abort-staged-bundle!}
           (set (keys (ns-publics 'gravity.darwin-publication)))))
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.darwin-publication (:namespace contract)))
    (is (= :darwin-descriptor-relative-publication
           (:contract-boundary contract)))
    (is (= #{'open-target! 'stage-bundle! 'commit-staged-bundle!
             'verify-published-bundle! 'abort-staged-bundle!}
           (set (keys (:public-api contract)))))
    (is (= ['gravity.bootstrap 'gravity.diagnostics 'gravity.cli]
           (:forbids contract)))
    (is (false? (:public? contract)))
    (is (false? (:release? contract)))
    (is (false? (:self-hosted? contract)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.diagnostics)))
    (is (nil? (find-ns 'gravity.cli)))
    (doseq [forbidden
            ["Files/createTempDirectory" "Files/write"
             "Files/readAllBytes" "Files/readAttributes"
             "Files/getPosixFilePermissions" "Files/setPosixFilePermissions"
             "Files/move" "Files/deleteIfExists" "Files/getFileStore"
             "Path.toRealPath" "renamex_np(AT_FDCWD"]]
      (is (not (str/includes? source forbidden)) forbidden))))

(deftest descriptor-publication-commits-and-verifies-exact-bundle
  (with-private-temp-directory
    "gravity-descriptor-publication-happy-"
    (fn [root]
      (let [output (.resolve root "bundle")
            specs (test-file-specs)
            checkpoints (atom [])
            checkpoint-var (private-var '*operation-checkpoint*)
            success (Object.)
            [receipt observed]
            (with-redefs-fn
             {checkpoint-var
              (fn [event context]
                (swap! checkpoints conj [event context]))}
             (fn []
               (let [target
                     (publication/open-target! (.toString output))
                     staged (publication/stage-bundle! target specs)
                     receipt (:publication-receipt staged)
                     observed
                     (publication/commit-staged-bundle! staged success)]
                 [receipt observed])))
            verification
            (publication/verify-published-bundle! receipt specs)]
        (is (identical? success observed))
        (is (= :passed (:status verification)))
        (is (= :descriptor-relative-exclusive-rename
               (:publication verification)))
        (is (= 7 (:file-count verification)))
        (is (= (set file-names) (directory-names output)))
        (is (= [:staging-handle-opened
                :before-final-staging-verification
                :before-final-name-binding
                :before-native-rename]
               (mapv first @checkpoints)))
        (is (every?
             #(= #{:requested-parent :staging-name :destination-name}
                 (set (keys (second %))))
             @checkpoints))
        (is (= {:rename-excl 4 :rename-nofollow-any 16
                :rename-resolve-beneath 32 :combined 52}
               (get-in receipt [:publisher-evidence :flags])))
        (is (= :held-parent-and-staging-directory-descriptors
               (get-in receipt
                       [:publisher-evidence :path-identity-linearization])))
        (is (contains?
             (get-in receipt [:publisher-evidence :guarantee-scope])
             :no-extended-access-control-lists))
        (is (true?
             (get-in receipt
                     [:publisher-evidence
                      :source-directory-trailing-slash?])))
        (is (false?
             (get-in receipt
                     [:publisher-evidence :crash-durable-publication?])))
        (is (false?
             (get-in receipt
                     [:publisher-evidence
                      :same-euid-concurrent-mutation-resistant?])))
        (is (not (str/includes? (pr-str receipt) ".gravity-c17-")))
        (is (empty?
             (filter #(str/starts-with? % ".gravity-c17-")
                     (directory-names root))))))))

(deftest descriptor-publication-context-is-opaque-and-content-authentic
  (testing "caller-owned input bytes are cloned before staging authority"
    (with-private-temp-directory
      "gravity-descriptor-publication-cloned-input-"
      (fn [root]
        (let [output (.resolve root "bundle")
              specs (test-file-specs)
              original-program-c
              (aclone ^bytes (get-in specs ["program.c" :bytes]))
              target (publication/open-target! (.toString output))
              staged (publication/stage-bundle! target specs)
              caller-bytes ^bytes (get-in specs ["program.c" :bytes])
              context-class (.getClass staged)
              public-instance-fields
              (vec
               (remove
                #(java.lang.reflect.Modifier/isStatic (.getModifiers %))
                (.getFields context-class)))
              public-static-fields
              (filterv
               #(java.lang.reflect.Modifier/isStatic (.getModifiers %))
               (.getFields context-class))
              side-table-var (private-var 'context-controls)
              side-table-var-exposed?
              (boolean
               (some
                #(identical? side-table-var (.get % nil))
                public-static-fields))
              forged-context
              (clojure.lang.Reflector/invokeConstructor
               context-class (object-array [{:forged-receipt true}]))]
          (aset-byte caller-bytes 0
                     (byte (bit-xor 1 (aget caller-bytes 0))))
          (is (not (associative? staged)))
          (is (= ["publication_receipt"]
                 (mapv #(.getName %) public-instance-fields)))
          (is (every?
               #(and (java.lang.reflect.Modifier/isPublic
                      (.getModifiers %))
                     (java.lang.reflect.Modifier/isFinal
                      (.getModifiers %)))
               public-instance-fields))
          (is (false? side-table-var-exposed?)
              (mapv #(.getName %) public-static-fields))
          (is (thrown? ClassCastException
                       (assoc staged :file-specs {} :file-records {})))
          (is (= :invalid-provider-context
                 (:reason
                  (failure-data
                   #(publication/abort-staged-bundle! forged-context)))))
          (publication/commit-staged-bundle! staged :ok)
          (is (java.util.Arrays/equals
               original-program-c
               (Files/readAllBytes (.resolve output "program.c"))))))))
  (testing "forged expected records cannot authorize mutated staged bytes"
    (with-private-temp-directory
      "gravity-descriptor-publication-forged-context-"
      (fn [root]
        (let [output (.resolve root "bundle")
              target (publication/open-target! (.toString output))
              staged (publication/stage-bundle! target (test-file-specs))
              state (provider-state staged)
              program-c
              (.resolve
               (.resolve (Paths/get (:parent-path state)
                                    (make-array String 0))
                         (:staging-leaf state))
               "program.c")
              bytes (Files/readAllBytes program-c)
              changed (aclone bytes)
              _ (aset-byte changed 0
                           (byte (bit-xor 1 (aget changed 0))))
              _ (Files/write
                 program-c changed
                 (into-array
                  OpenOption
                  [StandardOpenOption/TRUNCATE_EXISTING
                   StandardOpenOption/WRITE]))
              assoc-failure
              (try
                (assoc staged :file-specs {} :file-records {})
                ::unexpected-success
                (catch ClassCastException _ :rejected))
              data
              (failure-data
               #(publication/commit-staged-bundle! staged :impossible))]
          (is (= :rejected assoc-failure))
          (is (contains?
               #{:file-content-or-metadata-mismatch
                 :descriptor-or-content-identity-changed}
               (:reason data))
              data)
          (is (not (Files/exists output (make-array LinkOption 0))))
          (is (empty?
               (filter #(str/starts-with? % ".gravity-c17-")
                       (directory-names root)))))))))

(deftest descriptor-publication-rejects-inheritable-parent-acls
  (with-private-temp-directory
    "gravity-descriptor-publication-parent-acl-"
    (fn [root]
      (let [output (.resolve root "bundle")
            call-var (private-var 'int-call-result)
            original (var-get call-var)
            acl-free-count (atom 0)]
        (try
          (add-acl! root inheritable-everyone-acl)
          (let [data
                (with-redefs-fn
                 {call-var
                  (fn [runtime arena operation arguments]
                    (when (= :acl-free operation)
                      (swap! acl-free-count inc))
                    (original runtime arena operation arguments))}
                 #(failure-data
                   (fn []
                     (publication/open-target! (.toString output)))))]
            (is (= :authenticate-parent (:operation data)) data)
            (is (= :nontrivial-extended-access-control-list
                   (:reason data))
                data)
            (is (= 1 @acl-free-count))
            (is (not (Files/exists output (make-array LinkOption 0))))
            (is (= #{} (directory-names root))))
          (finally
            (remove-acls! root)))))))

(deftest descriptor-publication-exclusive-rename-contains-destination-race
  (with-private-temp-directory
    "gravity-descriptor-publication-collision-"
    (fn [root]
      (let [output (.resolve root "bundle")
            marker (.getBytes "attacker-marker\n" StandardCharsets/UTF_8)
            checkpoint-var (private-var '*operation-checkpoint*)
            injected? (atom false)
            target (publication/open-target! (.toString output))
            staged (publication/stage-bundle! target (test-file-specs))
            data
            (with-redefs-fn
             {checkpoint-var
              (fn [event _]
                (when (and (= :before-native-rename event)
                           (compare-and-set! injected? false true))
                  (Files/createDirectory
                   output (make-array FileAttribute 0))
                  (write-bytes! (.resolve output "marker") marker)))}
             #(failure-data
               (fn []
                 (publication/commit-staged-bundle! staged :impossible))))]
        (is (= [true :commit :destination-collision 17 true]
               [@injected? (:operation data) (:reason data)
                (:errno data) (:output-collision? data)]))
        (is (java.util.Arrays/equals
             marker (Files/readAllBytes (.resolve output "marker"))))
        (is (= #{"marker"} (directory-names output)))
        (is (empty?
             (filter #(str/starts-with? % ".gravity-c17-")
                     (directory-names root))))))))

(deftest descriptor-publication-rejects-hostile-staging-mutations
  (doseq [attack [:extra-entry :unexpected-directory :changed-bytes
                  :symlink :hard-link :wrong-file-mode
                  :wrong-directory-mode :file-acl :directory-acl]]
    (testing (name attack)
      (with-private-temp-directory
        (str "gravity-descriptor-publication-hostile-" (name attack) "-")
        (fn [root]
          (let [output (.resolve root "bundle")
                external (.resolve root "external-sentinel")
                external-bytes
                (.getBytes "external-sentinel\n" StandardCharsets/UTF_8)
                _ (write-bytes! external external-bytes)
                checkpoint-var (private-var '*operation-checkpoint*)
                injected? (atom false)
                data
                (with-redefs-fn
                 {checkpoint-var
                  (fn [event {:keys [requested-parent staging-name]}]
                    (when (and (= :before-final-staging-verification event)
                               (compare-and-set! injected? false true))
                      (let [staging
                            (.resolve (Paths/get requested-parent
                                                 (make-array String 0))
                                      staging-name)
                            program-c (.resolve staging "program.c")]
                        (case attack
                          :extra-entry
                          (write-bytes!
                           (.resolve staging "unexpected")
                           (.getBytes "unexpected\n"
                                      StandardCharsets/UTF_8))

                          :unexpected-directory
                          (let [unexpected
                                (Files/createDirectory
                                 (.resolve staging "unexpected-directory")
                                 (make-array FileAttribute 0))]
                            (write-bytes!
                             (.resolve unexpected "marker")
                             (.getBytes "nested-marker\n"
                                        StandardCharsets/UTF_8)))

                          :changed-bytes
                          (let [bytes (Files/readAllBytes program-c)
                                changed (aclone bytes)]
                            (aset-byte changed 0
                                       (byte (bit-xor 1 (aget changed 0))))
                            (Files/write
                             program-c changed
                             (into-array
                              OpenOption
                              [StandardOpenOption/TRUNCATE_EXISTING
                               StandardOpenOption/WRITE])))

                          :symlink
                          (do
                            (Files/delete program-c)
                            (Files/createSymbolicLink
                             program-c external
                             (make-array FileAttribute 0)))

                          :hard-link
                          (Files/createLink
                           (.resolve root "external-hard-link") program-c)

                          :wrong-file-mode
                          (Files/setPosixFilePermissions
                           program-c owner-only-permissions)

                          :wrong-directory-mode
                          (Files/setPosixFilePermissions
                           staging owner-only-permissions)

                          :file-acl
                          (add-acl! program-c broad-everyone-acl)

                          :directory-acl
                          (add-acl! staging broad-everyone-acl)))))}
                 #(failure-data
                   (fn []
                     (let [target
                           (publication/open-target! (.toString output))]
                       (publication/stage-bundle!
                        target (test-file-specs))))))]
            (is @injected?)
            (is (map? data) data)
            (is (true? (:gravity.darwin-publication/error data)) data)
            (is (contains?
                 #{:staging-contract-mismatch
                   :file-content-or-metadata-mismatch
                   :unique-file-open-failed
                   :nontrivial-extended-access-control-list}
                 (:reason data))
                data)
            (is (not (Files/exists output (make-array LinkOption 0))))
            (is (java.util.Arrays/equals
                 external-bytes (Files/readAllBytes external)))
            (is (empty?
                 (filter #(str/starts-with? % ".gravity-c17-")
                         (directory-names root))))))))))

(deftest descriptor-publication-binds-parent-and-staging-names
  (testing "renamed parent cannot redirect commit into a replacement"
    (let [root
          (Files/createTempDirectory
           (Paths/get "/private/tmp" (make-array String 0))
           "gravity-descriptor-publication-parent-"
           (make-array FileAttribute 0))
          moved (.resolve (.getParent root)
                          (str (.getFileName root) "-moved"))
          output (.resolve root "bundle")
          marker-bytes (.getBytes "replacement-parent\n"
                                  StandardCharsets/UTF_8)
          checkpoint-var (private-var '*operation-checkpoint*)]
      (Files/setPosixFilePermissions root owner-only-permissions)
      (try
        (let [target (publication/open-target! (.toString output))
              staged (publication/stage-bundle! target (test-file-specs))
              data
              (with-redefs-fn
               {checkpoint-var
                (fn [event _]
                  (when (= :before-final-name-binding event)
                    (Files/move root moved
                                (make-array StandardCopyOption 0))
                    (Files/createDirectory root
                                           (make-array FileAttribute 0))
                    (Files/setPosixFilePermissions root owner-only-permissions)
                    (write-bytes! (.resolve root "marker") marker-bytes)))}
               #(failure-data
                 (fn []
                   (publication/commit-staged-bundle!
                    staged :impossible))))]
          (is (= :descriptor-or-content-identity-changed (:reason data)) data)
          (is (= #{"marker"} (directory-names root)))
          (is (java.util.Arrays/equals
               marker-bytes (Files/readAllBytes (.resolve root "marker"))))
          (is (empty? (directory-names moved))))
        (finally
          (delete-tree! root)
          (delete-tree! moved)))))
  (testing "a replaced staging name never receives payload or cleanup"
    (with-private-temp-directory
      "gravity-descriptor-publication-staging-name-"
      (fn [root]
        (let [output (.resolve root "bundle")
              displaced (atom nil)
              marker-bytes (.getBytes "replacement-staging\n"
                                      StandardCharsets/UTF_8)
              checkpoint-var (private-var '*operation-checkpoint*)
              data
              (with-redefs-fn
               {checkpoint-var
                (fn [event {:keys [requested-parent staging-name]}]
                  (when (= :staging-handle-opened event)
                    (let [parent
                          (Paths/get requested-parent (make-array String 0))
                          original (.resolve parent staging-name)
                          moved (.resolve parent (str staging-name "-moved"))]
                      (reset! displaced moved)
                      (Files/move original moved
                                  (make-array StandardCopyOption 0))
                      (Files/createDirectory original
                                             (make-array FileAttribute 0))
                      (write-bytes! (.resolve original "marker")
                                    marker-bytes))))}
               #(failure-data
                 (fn []
                   (let [target
                         (publication/open-target! (.toString output))]
                     (publication/stage-bundle!
                      target (test-file-specs))))))
              attacker
              (first
               (filter #(and (str/starts-with? % ".gravity-c17-")
                             (not (str/ends-with? % "-moved")))
                       (directory-names root)))]
          (is (= :staging-contract-mismatch (:reason data)) data)
          (is (= #{"marker"} (directory-names (.resolve root attacker))))
          (is (java.util.Arrays/equals
               marker-bytes
               (Files/readAllBytes (.resolve (.resolve root attacker)
                                             "marker"))))
          (is (empty? (directory-names @displaced)))
          (is (not (Files/exists output (make-array LinkOption 0)))))))))

(deftest descriptor-publication-revalidates-after-the-final-checkpoint
  (with-private-temp-directory
    "gravity-descriptor-publication-final-substitution-"
    (fn [root]
      (let [output (.resolve root "bundle")
            marker-bytes (.getBytes "late-substitution\n"
                                    StandardCharsets/UTF_8)
            replacement (atom nil)
            displaced (atom nil)
            checkpoint-var (private-var '*operation-checkpoint*)
            target (publication/open-target! (.toString output))
            staged (publication/stage-bundle! target (test-file-specs))
            failure
            (with-redefs-fn
             {checkpoint-var
              (fn [event {:keys [requested-parent staging-name]}]
                (when (= :before-native-rename event)
                  (let [parent
                        (Paths/get requested-parent (make-array String 0))
                        original (.resolve parent staging-name)
                        moved (.resolve parent (str staging-name "-moved"))]
                    (reset! replacement original)
                    (reset! displaced moved)
                    (Files/move original moved
                                (make-array StandardCopyOption 0))
                    (Files/createDirectory original
                                           (make-array FileAttribute 0))
                    (write-bytes! (.resolve original "marker")
                                  marker-bytes))))}
             #(failure-record
               (fn []
                 (publication/commit-staged-bundle!
                  staged :impossible))))
            captured-var (private-var 'captured-call)
            captured-original (var-get captured-var)
            terminal-native-calls (atom 0)
            terminal-result
            (with-redefs-fn
             {captured-var
              (fn [runtime arena operation arguments]
                (swap! terminal-native-calls inc)
                (captured-original runtime arena operation arguments))}
             #(publication/abort-staged-bundle! staged))]
        (is (= :descriptor-or-content-identity-changed
               (get-in failure [:data :reason]))
            failure)
        (is (some #(and (= :cleanup-incomplete (:reason %))
                        (false? (:cleanup-complete? %))
                        (true? (:residue-possible? %)))
                  (:suppressed failure))
            failure)
        (is (not (Files/exists output (make-array LinkOption 0))))
        (is (= #{"marker"} (directory-names @replacement)))
        (is (java.util.Arrays/equals
             marker-bytes (Files/readAllBytes (.resolve @replacement "marker"))))
        (is (empty? (directory-names @displaced)))
        (is (= {:status :already-aborted :published? false
                :cleanup-complete? false :residue-possible? true
                :native-calls 0}
               terminal-result))
        (is (zero? @terminal-native-calls))))))

(deftest descriptor-publication-control-is-single-owner-and-terminal-idempotent
  (with-private-temp-directory
    "gravity-descriptor-publication-control-"
    (fn [root]
      (let [captured-var (private-var 'captured-call)
            captured-original (var-get captured-var)
            invalid-output (.resolve root "invalid")
            invalid-target
            (publication/open-target! (.toString invalid-output))
            old-parent-fd (:parent-descriptor (provider-state invalid-target))
            invalid-data
            (failure-data
             #(publication/stage-bundle! invalid-target {}))
            sentinel-output (.resolve root "sentinel")
            sentinel-target
            (publication/open-target! (.toString sentinel-output))
            reused-parent-fd
            (:parent-descriptor (provider-state sentinel-target))
            terminal-native-calls (atom 0)
            terminal-result
            (with-redefs-fn
             {captured-var
              (fn [runtime arena operation arguments]
                (swap! terminal-native-calls inc)
                (captured-original runtime arena operation arguments))}
             #(publication/abort-staged-bundle! invalid-target))]
        (is (= :invalid-file-set (:reason invalid-data)) invalid-data)
        (is (= :aborted (:phase (provider-state invalid-target))))
        (is (nil? (:parent-descriptor (provider-state invalid-target))))
        (is (= old-parent-fd reused-parent-fd))
        (is (= {:status :already-aborted :published? false
                :cleanup-complete? true :residue-possible? false
                :native-calls 0}
               terminal-result))
        (is (zero? @terminal-native-calls))
        (let [staged
              (publication/stage-bundle!
               sentinel-target (test-file-specs))
              cleanup
              ;; The original target and staged value share one control;
              ;; either can atomically consume the staged descriptors.
              (publication/abort-staged-bundle! sentinel-target)]
          (is (= :aborted (:status cleanup)))
          (is (true? (:cleanup-complete? cleanup)) cleanup)
          (is (= :already-aborted
                 (:status (publication/abort-staged-bundle! staged))))
          (is (empty? (directory-names root))))))))

(deftest descriptor-publication-postcommit-abort-never-reuses-descriptors
  (with-private-temp-directory
    "gravity-descriptor-publication-postcommit-control-"
    (fn [root]
      (let [output (.resolve root "bundle")
            specs (test-file-specs)
            target (publication/open-target! (.toString output))
            staged (publication/stage-bundle! target specs)
            closed-fds
            (select-keys (provider-state staged)
                         [:parent-descriptor :staging-descriptor])
            receipt (:publication-receipt staged)
            _ (publication/commit-staged-bundle! staged :ok)
            sentinel
            (publication/open-target! (.toString (.resolve root "sentinel")))
            reused (:parent-descriptor (provider-state sentinel))
            captured-var (private-var 'captured-call)
            original (var-get captured-var)
            native-calls (atom 0)
            result
            (with-redefs-fn
             {captured-var
              (fn [runtime arena operation arguments]
                (swap! native-calls inc)
                (original runtime arena operation arguments))}
             #(publication/abort-staged-bundle! staged))]
        (is (contains? (set (vals closed-fds)) reused))
        (is (= {:status :already-committed :published? true
                :cleanup-applicable? false :native-calls 0}
               result))
        (is (zero? @native-calls))
        (is (= :passed
               (:status
                (publication/verify-published-bundle! receipt specs))))
        (is (true?
             (:cleanup-complete?
              (publication/abort-staged-bundle! sentinel))))))))

(deftest descriptor-publication-in-progress-abort-loses-without-native-calls
  (with-private-temp-directory
    "gravity-descriptor-publication-in-progress-control-"
    (fn [root]
      (let [output (.resolve root "bundle")
            checkpoint-var (private-var '*operation-checkpoint*)
            captured-var (private-var 'captured-call)
            captured-original (var-get captured-var)
            active-context (atom nil)
            observations (atom [])
            success (Object.)]
        (with-redefs-fn
         {checkpoint-var
          (fn [event _]
            (when (contains? #{:staging-handle-opened
                               :before-native-rename}
                             event)
              (let [calls (atom 0)
                    data
                    (with-redefs-fn
                     {captured-var
                      (fn [runtime arena operation arguments]
                        (swap! calls inc)
                        (captured-original
                         runtime arena operation arguments))}
                     #(failure-data
                       (fn []
                         (publication/abort-staged-bundle!
                          @active-context))))]
                (swap! observations conj
                       {:event event :data data :native-calls @calls}))))}
         (fn []
           (let [target
                 (publication/open-target! (.toString output))]
             (reset! active-context target)
             (let [staged
                   (publication/stage-bundle! target (test-file-specs))]
               (reset! active-context staged)
               (is (identical?
                    success
                    (publication/commit-staged-bundle! staged success)))))))
        (is (= [:staging-handle-opened :before-native-rename]
               (mapv :event @observations)))
        (is (every? #(= :invalid-provider-lifecycle
                        (get-in % [:data :reason]))
                    @observations)
            @observations)
        (is (every? #(zero? (:native-calls %)) @observations)
            @observations)))))

(deftest descriptor-publication-loops-over-partial-native-io
  (with-private-temp-directory
    "gravity-descriptor-publication-partial-io-"
    (fn [root]
      (let [output (.resolve root "bundle")
            call-var (private-var 'long-call-result)
            original (var-get call-var)
            partial-writes (atom 0)
            partial-reads (atom 0)
            receipt
            (with-redefs-fn
             {call-var
              (fn [runtime arena operation arguments]
                (if (and (contains? #{:write :pread} operation)
                         (> (long (nth arguments 2)) 1))
                  (let [[descriptor segment _ & tail] arguments
                        narrowed
                        (into [descriptor (.asSlice segment (long 0) (long 1))
                               (long 1)] tail)]
                    (swap! (if (= :write operation)
                             partial-writes partial-reads) inc)
                    (original runtime arena operation narrowed))
                  (original runtime arena operation arguments)))}
             (fn []
               (let [target
                     (publication/open-target! (.toString output))
                     staged
                     (publication/stage-bundle! target (test-file-specs))
                     receipt (:publication-receipt staged)]
                 (publication/commit-staged-bundle! staged :ok)
                 receipt)))]
        (is (pos? @partial-writes))
        (is (pos? @partial-reads))
        (is (= :passed
               (:status
                (publication/verify-published-bundle!
                 receipt (test-file-specs)))))))))

(deftest descriptor-publication-handles-eintr-and-rejects-zero-progress-io
  (with-private-temp-directory
    "gravity-descriptor-publication-eintr-"
    (fn [root]
      (let [output (.resolve root "bundle")
            call-var (private-var 'long-call-result)
            original (var-get call-var)
            pending (atom #{:write :pread})
            specs (test-file-specs)
            receipt
            (with-redefs-fn
             {call-var
              (fn [runtime arena operation arguments]
                (if (contains? @pending operation)
                  (do (swap! pending disj operation)
                      {:value -1 :errno 4})
                  (original runtime arena operation arguments)))}
             (fn []
               (let [target
                     (publication/open-target! (.toString output))
                     staged (publication/stage-bundle! target specs)
                     receipt (:publication-receipt staged)]
                 (publication/commit-staged-bundle! staged :ok)
                 receipt)))]
        (is (empty? @pending))
        (is (= :passed
               (:status
                (publication/verify-published-bundle! receipt specs)))))))
  (doseq [[operation expected-reason]
          [[:write :zero-progress-file-write]
           [:pread :short-file-readback]]]
    (testing (name operation)
      (with-private-temp-directory
        (str "gravity-descriptor-publication-zero-" (name operation) "-")
        (fn [root]
          (let [output (.resolve root "bundle")
                call-var (private-var 'long-call-result)
                original (var-get call-var)
                injected? (atom false)
                data
                (with-redefs-fn
                 {call-var
                  (fn [runtime arena observed arguments]
                    (if (and (= operation observed)
                             (compare-and-set! injected? false true))
                      {:value 0}
                      (original runtime arena observed arguments)))}
                 #(failure-data
                   (fn []
                     (let [target
                           (publication/open-target! (.toString output))]
                       (publication/stage-bundle!
                        target (test-file-specs))))))]
            (is @injected?)
            (is (= expected-reason (:reason data)) data)
            (is (not (Files/exists output (make-array LinkOption 0))))
            (is (empty?
                 (filter #(str/starts-with? % ".gravity-c17-")
                         (directory-names root))))))))))

(deftest descriptor-publication-verifier-rejects-postcommit-mutations
  (doseq [attack [:extra-entry :changed-bytes :symlink :hard-link
                  :wrong-file-mode :wrong-directory-mode]]
    (testing (name attack)
      (with-private-temp-directory
        (str "gravity-descriptor-publication-verify-" (name attack) "-")
        (fn [root]
          (let [output (.resolve root "bundle")
                specs (test-file-specs)
                external (.resolve root "external-sentinel")
                external-bytes
                (.getBytes "verification-sentinel\n"
                           StandardCharsets/UTF_8)
                _ (write-bytes! external external-bytes)
                target (publication/open-target! (.toString output))
                staged (publication/stage-bundle! target specs)
                receipt (:publication-receipt staged)
                _ (publication/commit-staged-bundle! staged :ok)
                program-c (.resolve output "program.c")]
            (case attack
              :extra-entry
              (write-bytes!
               (.resolve output "unexpected")
               (.getBytes "unexpected\n" StandardCharsets/UTF_8))

              :changed-bytes
              (let [bytes (Files/readAllBytes program-c)
                    changed (aclone bytes)]
                (aset-byte changed 0
                           (byte (bit-xor 1 (aget changed 0))))
                (Files/write
                 program-c changed
                 (into-array
                  OpenOption
                  [StandardOpenOption/TRUNCATE_EXISTING
                   StandardOpenOption/WRITE])))

              :symlink
              (do
                (Files/delete program-c)
                (Files/createSymbolicLink
                 program-c external (make-array FileAttribute 0)))

              :hard-link
              (Files/createLink
               (.resolve root "external-hard-link") program-c)

              :wrong-file-mode
              (Files/setPosixFilePermissions
               program-c owner-only-permissions)

              :wrong-directory-mode
              (Files/setPosixFilePermissions
               output owner-only-permissions))
            (let [data
                  (failure-data
                   #(publication/verify-published-bundle! receipt specs))]
              (is (true? (:gravity.darwin-publication/error data)) data)
              (is (contains?
                   #{:published-bundle-content-or-identity-mismatch
                     :file-content-or-metadata-mismatch
                     :unique-file-open-failed
                     :published-directory-provenance-mismatch}
                   (:reason data))
                  data)
              (is (java.util.Arrays/equals
                   external-bytes (Files/readAllBytes external))))))))))

(deftest descriptor-publication-requires-native-access-before-filesystem
  (let [expression
        (str
         "(require 'gravity.darwin-publication) "
         "(try "
         " (gravity.darwin-publication/open-target! "
         "  \"/definitely/missing-parent/bundle\") "
         " (println :unexpected-success) "
         " (catch clojure.lang.ExceptionInfo e (prn (ex-data e))))")
        process
        (.start
         (doto
          (ProcessBuilder.
           ^java.util.List
           ["clojure" "-Sdeps"
            "{:paths [\"bootstrap/clojure/src\"]}"
            "-M" "-e" expression])
          (.directory (java.io.File. "."))
          (.redirectErrorStream false)))
        _ (.close (.getOutputStream process))
        finished?
        (.waitFor process 60 java.util.concurrent.TimeUnit/SECONDS)
        terminated?
        (if finished?
          true
          (do
            (.destroyForcibly process)
            (.waitFor process 10 java.util.concurrent.TimeUnit/SECONDS)))
        stdout (slurp (.getInputStream process))
        stderr (slurp (.getErrorStream process))
        exit-code (when terminated? (.exitValue process))
        record (when (seq (str/trim stdout))
                 (edn/read-string (str/trim stdout)))]
    (is finished?)
    (is (= 0 exit-code))
    (is (empty? stderr))
    (is (= [true :native-preflight :native-access-disabled false]
           ((juxt :gravity.darwin-publication/error
                  :operation :reason :native-access-enabled?)
            record)))
    (is (not (str/includes? stdout "missing-parent")))
    (is (= stdout (str (pr-str record) "\n")))))
