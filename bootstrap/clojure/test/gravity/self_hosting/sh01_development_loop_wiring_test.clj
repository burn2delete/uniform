(ns gravity.self-hosting.sh01-development-loop-wiring-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner])
  (:import [java.nio.file Files LinkOption Path]
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
     :working-directory "/tmp"
     :cache-directory cache-directory
     :broker-root broker-root
     :timeout-ms 1000
     :snapshot
     {:schema :gravity/sh01-complete-repository-snapshot-v1
      :repository-identity snapshot-id
      :path-count 1}
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
          first (future (execute plan (context base) launcher))
          second (future (execute plan (context base) launcher))]
      ;; Only the producer reaches the launch boundary; release it after the
      ;; second parent has had time to enter the same-key cache path.
      (.await ready 100 TimeUnit/MILLISECONDS)
      (Thread/sleep 50)
      (.countDown release)
      (let [reports [@first @second]]
        (is (= 1 @launches))
        (is (every? #(= :passed (:status %)) reports))
        (is (= 1 (reduce + (map :launched-jvms reports))))
        (is (= 1 (reduce + (map :cache-hit-jvms reports))))))))

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
          changed-snapshot
          (assoc (:snapshot selected-context)
                 :repository-identity
                 (str "sha256:" (apply str (repeat 64 "b"))))]
      (with-redefs [wiring/repository-snapshot
                    (fn [_] changed-snapshot)]
        (let [report
              (execute plan selected-context
                       (fn [_ _] {:status :passed :exit-code 0}))]
          (is (= :failed (:status report)))
          (is (= 1 (:launched-jvms report)))
          (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                 (:diagnostic-id (first (:results report)))))
          (is (true? (:child-jvm-launched? (first (:results report)))))
          (is (= [:admitted :released]
                 (mapv :outcome (:broker-receipts report)))))))))

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
