(ns gravity.self-hosting.sh01-ownership-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting-test-runner :as runner]))

(import '(java.nio.file Files LinkOption Paths)
        '(java.nio.file.attribute FileAttribute))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh01_ownership_test.clj")]
    (when-not resource
      (throw (ex-info "SH-01 test source is not on the classpath"
                      {:id "SH01-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH01-REPOSITORY-ROOT"}))

        (and (.isFile (.toFile (.resolve path "deps.edn")))
             (.isFile
              (.toFile
               (.resolve path "docs/self-hosting-slice-ownership.edn"))))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.toFile (.resolve @root relative)))

(defn- ownership-record
  []
  (edn/read-string
   {:readers *data-readers*}
   (slurp (path "docs/self-hosting-slice-ownership.edn"))))

(defn- gravity-module-paths
  []
  (let [source-root (path "bootstrap/gravity/src")
        root-path @root]
    (->> (file-seq source-root)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".gravity"))
         (map #(str (.relativize root-path (.toPath %))))
         (map #(str/replace % java.io.File/separator "/"))
         set)))

(defn- top-level-paths
  "Returns exact, direct-child paths for a Stage0 root.

  The Stage0 component contract intentionally excludes nested runner and
  self-hosting infrastructure.  Keep this helper exact so an unrelated file
  cannot silently enter the owner projection.
  "
  [relative-root suffix]
  (let [root-file (path relative-root)
        root-path @root]
    (->> (.listFiles root-file)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) suffix))
         (map #(str (.relativize root-path (.toPath %))))
         (map #(str/replace % java.io.File/separator "/"))
         set)))

(defn- stage0-source-paths
  []
  (top-level-paths "bootstrap/clojure/src/gravity" ".clj"))

(defn- stage0-test-paths
  []
  (top-level-paths "bootstrap/clojure/test/gravity" "_test.clj"))

(defn- all-direct-stage0-test-paths
  []
  (top-level-paths "bootstrap/clojure/test/gravity" ".clj"))

(defn- stage0-component-id
  "Derives the reviewed Stage0 id from one exact source/test stem."
  [relative]
  (some-> (re-find
           #"^bootstrap/clojure/(?:src|test)/gravity/([^/]+?)(?:_test)?\.clj$"
           relative)
          second
          (str/replace "_test" "")
          (str/replace "_" "-")))

(defn- paired-stage0-path
  [relative]
  (when-let [id (stage0-component-id relative)]
    (let [stem (str/replace id "-" "_")]
      (cond
        (str/starts-with? relative "bootstrap/clojure/src/")
        (str "bootstrap/clojure/test/gravity/" stem "_test.clj")

        (str/starts-with? relative "bootstrap/clojure/test/")
        (str "bootstrap/clojure/src/gravity/" stem ".clj")))))

(defn- coordinator-support-paths
  "Return top-level Clojure coordinator reservations outside paired Stage0.

  Coordinator runners and P15 integration surfaces are intentionally not
  Stage0 components and must not be admitted by the leaf projection merely
  because they are direct children of a Stage0 source or test root.
  "
  [record]
  (->> (concat (get-in record [:coordinator-owned :central-routing])
               (get-in record
                       [:coordinator-owned
                        :p15-coordinator-integration-reservations])
               (get-in record
                       [:coordinator-owned
                        :coordinator-runner-integration-reservations]))
       (filter #(re-find
                 #"^bootstrap/clojure/(?:src|test)/gravity/[^/]+\.clj$"
                 %))
       set
       (#(disj %
               "bootstrap/clojure/src/gravity/bootstrap.clj"
               "bootstrap/clojure/test/gravity/bootstrap_test.clj"))))

(deftest ownership-record-is-total-and-unambiguous
  (let [record (ownership-record)
        module-owners (:module-owners record)
        actual-gravity-modules (gravity-module-paths)
        all-direct-stage0-sources (stage0-source-paths)
        all-direct-stage0-tests (all-direct-stage0-test-paths)
        support-paths (coordinator-support-paths record)
        support-test-paths
        (set (filter #(str/starts-with?
                      % "bootstrap/clojure/test/gravity/")
                     support-paths))
        actual-stage0-sources
        (set/difference all-direct-stage0-sources support-paths)
        actual-stage0-tests
        (set/difference all-direct-stage0-tests support-paths)
        actual-stage0-paths (set/union actual-stage0-sources actual-stage0-tests)
        declared-modules (set (keys module-owners))
        slice-ids (set (keys (:slice-owners record)))
        expected-slice-ids
        (set (map #(format "SH-%02d" %) (range 30)))
        declared-gravity-modules
        (set (filter #(str/starts-with? % "bootstrap/gravity/src/")
                     declared-modules))
        declared-stage0-paths
        (set/difference
         (set (filter #(re-find
                        #"^bootstrap/clojure/(?:src|test)/gravity/[^/]+$"
                        %)
                       declared-modules))
         support-paths)]
    (testing "the countable plan and all current Gravity modules have one owner"
      (is (= :gravity/self-hosting-slice-ownership-v1 (:schema record)))
      (is (= "SH-01" (:slice record)))
      (is (= :complete (:status record)))
      (is (= :master-coordinator (:integration-owner record)))
      (is (= expected-slice-ids slice-ids))
      (is (= actual-gravity-modules declared-gravity-modules))
      (is (= 60 (count actual-gravity-modules)))
      (is (= (set/difference (stage0-test-paths) support-paths)
             actual-stage0-tests))
      (is (= all-direct-stage0-tests
             (set/union actual-stage0-tests support-test-paths)))
      (is (empty? (set/intersection actual-stage0-tests support-paths)))
      (is (= 108 (count actual-stage0-paths)))
      (is (= actual-stage0-paths declared-stage0-paths))
      (is (= 193 (count module-owners)))
      (is (every? keyword? (vals module-owners))))
    (testing "coordinator support paths stay reserved outside paired Stage0"
      (is (= 14 (count support-paths)))
      (is (every? #(or (not (contains? module-owners %))
                       (= :master-coordinator (get module-owners %)))
                  support-paths))
      (is (empty? (set/intersection actual-stage0-paths support-paths))))
    (testing "all exact module-owner paths exist and source/test owners are paired"
      (doseq [relative declared-modules]
        (is (.isFile (path relative)) relative))
      (doseq [source actual-stage0-sources
              :let [test (paired-stage0-path source)]]
        (is (some? test) source)
        (is (= (get module-owners source)
               (get module-owners test))
            [source test]))
      (doseq [test actual-stage0-tests
              :let [source (paired-stage0-path test)]]
        (is (some? source) test)
        (is (= (get module-owners test)
               (get module-owners source))
            [test source])))
    (testing "coordinator-only integration modules are not assigned to a leaf"
      (doseq [module
              (get-in record [:coordinator-owned :integration-surfaces])
              :when (str/starts-with? module "bootstrap/gravity/src/")]
        (is (= :master-coordinator (get module-owners module)) module)))
    (testing "reserved Stage0 ids cover exactly the 54 source components"
      (let [reserved (:reserved-leaf-modules record)
            expected-ids (set (map stage0-component-id actual-stage0-sources))]
        (is (= 54 (count reserved)))
        (is (= expected-ids (set (keys reserved))))
        (doseq [source actual-stage0-sources
                :let [id (stage0-component-id source)
                      owner (get module-owners source)]]
          (is (some? id) source)
          (is (= owner (get reserved id)) [id source]))))))

(deftest coordinator-reservations-and-leaf-conventions-are-explicit
  (let [record (ownership-record)
        coordinator (:coordinator-owned record)
        conventions (:future-path-conventions record)
        reserved-paths
        (concat (:central-routing coordinator)
                (:host-boundary-modules coordinator)
                (:host-boundary-tests coordinator)
                (:legacy-fixture-prefixes coordinator)
                (:integration-surfaces coordinator)
                (:coordination-state coordinator)
                (:coverage-generator-paths coordinator))]
    (testing "all exact coordinator reservations exist"
      (doseq [reserved reserved-paths]
        (is (.exists (path reserved)) reserved)))
    (testing "generated evidence, coverage, and completion state stay central"
      (is (seq (:generated-evidence-prefixes coordinator)))
      (is (seq (:coverage-prefixes coordinator)))
      (is (seq (:completion-state-paths coordinator)))
      (is (every? #(= :master-coordinator (:integration-owner %))
                  (vals (:slice-owners record)))))
    (testing "leaf writers receive only disjoint module, fixture, and test shapes"
      (is (= [:reserved-leaf-module :new-fixture :dedicated-test]
             (:worker-may-create conventions)))
      (is (= "gravity/self_hosting" (:discovery-resource conventions)))
      (is (= 'gravity.self-hosting-test-runner
             (:test-runner conventions)))
      (is (some #{:another-leaf-owner}
                (:worker-must-not-edit conventions))))))

(deftest stage0-compatibility-tests-are-five-exact-owned-paths
  (let [record (ownership-record)
        compatibility (:bootstrap-compatibility-tests record)
        module-owners (:module-owners record)
        stage0-tests (stage0-test-paths)]
    (is (= 5 (count compatibility)))
    (is (= 5 (count (set compatibility))))
    (doseq [test-path compatibility
            :let [source-path (paired-stage0-path test-path)
                  owner (get module-owners test-path)]]
      (is (.isFile (path test-path)) test-path)
      (is (contains? stage0-tests test-path) test-path)
      (is (keyword? owner) test-path)
      (is (= owner (get module-owners source-path)) [test-path source-path]))))

(deftest bootstrap-free-stage0-catalog-is-exact-when-runner-is-available
  (testing "the optional bootstrap-free runner owns exactly the reviewed leaf tests"
    (if-not (io/resource "gravity/bootstrap_free_leaf_test_runner.clj")
      (is true "bootstrap-free runner is not present in this checkout")
      (do
        (require 'gravity.bootstrap-free-leaf-test-runner)
        (let [runner-ns (find-ns 'gravity.bootstrap-free-leaf-test-runner)
              catalog (var-get (ns-resolve runner-ns 'catalog))
              validate! (var-get (ns-resolve runner-ns 'validate-catalog!))
              record (ownership-record)
              stage0-tests (stage0-test-paths)
              expected
              (set/difference
               stage0-tests
               (set (:bootstrap-compatibility-tests record))
               #{"bootstrap/clojure/test/gravity/bootstrap_test.clj"}
               (set (map #(str "bootstrap/clojure/test/gravity/" %)
                         (var-get
                          (ns-resolve runner-ns
                                      'excluded-top-level-test-files)))))]
          (is (= 48 (count catalog)))
          (is (= expected (set (map :test-path catalog))))
          (is (true? (validate!)))
          (is (every? symbol? (map :namespace catalog)))
          (is (= (set (map #(symbol (str "gravity." (str/replace % "_" "-") "-test"))
                           (map #(str/replace % #"^bootstrap/clojure/test/gravity/|_test\.clj$" "")
                                expected)))
                 (set (map :namespace catalog)))))))))

(deftest dedicated-test-routing-does-not-use-the-monolithic-test-file
  (let [deps (edn/read-string (slurp (path "deps.edn")))
        main-options (get-in deps [:aliases :test :main-opts])
        bootstrap-test-source
        (slurp (path "bootstrap/clojure/test/gravity/bootstrap_test.clj"))
        dedicated (runner/dedicated-test-namespaces)]
    (is (= ["-m" "gravity.self-hosting-test-runner"] main-options))
    (is (some #{'gravity.self-hosting.sh01-ownership-test} dedicated))
    (is (= dedicated (vec (sort dedicated))))
    (is (= dedicated (vec (distinct dedicated))))
    (is (= '[gravity.diagnostics-test
             gravity.cli-test
             gravity.bootstrap-test]
           (subvec (runner/test-namespaces) 0 3)))
    (is (not
         (str/includes?
          bootstrap-test-source
          "gravity.self-hosting.sh01-ownership-test")))
    (is (= {:mode :run :namespaces dedicated}
           (select-keys (runner/select-tests ["--dedicated"])
                        [:mode :namespaces])))
    (is (= {:mode :run
            :namespaces ['gravity.self-hosting.sh01-ownership-test]}
           (select-keys
            (runner/select-tests
             ["--namespace"
              "gravity.self-hosting.sh01-ownership-test"])
            [:mode :namespaces])))
    (is (some
         #{'gravity.darwin-publication-test}
         (:namespaces (runner/select-tests ["--list"]))))
    (let [exception
          (try
            (runner/select-tests
             ["--namespace" "gravity.unowned-test"])
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-TEST-NAMESPACE" (:id (ex-data exception)))))))

(deftest repeatable-namespace-selection-is-strict-and-deterministic
  (let [selected
        (runner/select-tests
         ["--namespace" "gravity.self-hosting.sh01-ownership-test"
          "--namespace" "gravity.self-hosting.sh01-parallel-test-runner-test"
          "--fail-fast"])]
    (is (= [:run true]
           [(:mode selected) (:fail-fast? selected)]))
    (is (= ['gravity.self-hosting.sh01-ownership-test
            'gravity.self-hosting.sh01-parallel-test-runner-test]
           (:namespaces selected))))
  (doseq [[arguments expected-id]
          [[["--namespace" "gravity.self-hosting.sh01-ownership-test"
             "--namespace" "gravity.self-hosting.sh01-ownership-test"]
            "SH01-TEST-NAMESPACE-DUPLICATE"]
           [["--namespace" "gravity.self-hosting.sh01-does-not-exist"]
            "SH01-TEST-NAMESPACE"]]]
    (let [exception
          (try
            (runner/select-tests arguments)
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= expected-id (:id (ex-data exception)))))))

(deftest run-namespaces-preserves-order-output-and-fail-fast-tail
  (let [events (atom [])
        summary (fn [namespace]
                  (swap! events conj [:run namespace])
                  (binding [test/*test-out* *out*]
                    (println (str "report:" namespace)))
                  {:test 1 :pass 1 :fail 0 :error 0})
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test
                       'gravity.self-hosting.sh01-parallel-test-runner-test]
          :fail-fast? true
          :require-fn #(swap! events conj [:require %])
          :run-tests-fn summary})]
    (is (= [[:require 'gravity.self-hosting.sh01-ownership-test]
            [:run 'gravity.self-hosting.sh01-ownership-test]
            [:require 'gravity.self-hosting.sh01-parallel-test-runner-test]
            [:run 'gravity.self-hosting.sh01-parallel-test-runner-test]]
           @events))
    (is (= :passed (:status report)))
    (is (empty? (:skipped-namespaces report)))
    (is (str/includes? (get-in report [:namespace-results 0 :stdout :text])
                       "report:gravity.self-hosting.sh01-ownership-test"))))

(deftest fresh-progress-telemetry-is-bounded-and-path-contained
  (let [relative "target/validation/sh01-fresh-progress-test.edn"
        path (Paths/get relative (make-array String 0))
        outside "/tmp/gravity-fresh-progress-outside.edn"
        link (Paths/get "target/validation/sh01-progress-linkdir"
                        (make-array String 0))
        linked-progress (.resolve link "progress.edn")]
    (try
      (Files/deleteIfExists path)
      (Files/deleteIfExists link)
      (let [emit (#'runner/progress-emitter relative)]
        (is (fn? emit))
        (emit {:event :test-var-start
               :namespace "gravity.bootstrap-test"
               :test-var "gravity.bootstrap-test/p15-s23-whole-language-compiler-artifact-records-current-stage-proof"
               :phase "gravity.bootstrap-test/p15-s23-whole-language-compiler-artifact-records-current-stage-proof"
               :active? true})
        (is (Files/exists path (make-array LinkOption 0)))
        (let [record (edn/read-string (slurp relative))]
          (is (= :gravity/fresh-verification-progress-v1 (:schema record)))
          (is (= :test-var-start (:event record)))
          (is (= 1 (:sequence record)))
          (is (integer? (:timestamp-ms record)))
          (is (true? (:active? record)))))
      (is (nil? (#'runner/progress-emitter outside)))
      (Files/createDirectories (.getParent link)
                               (make-array java.nio.file.attribute.FileAttribute 0))
      (Files/createSymbolicLink link (Paths/get "/tmp" (make-array String 0))
                               (make-array FileAttribute 0))
      (is (nil? (#'runner/progress-emitter (str linked-progress))))
      (finally
        (Files/deleteIfExists path)
        (Files/deleteIfExists link)))))

(deftest fresh-progress-telemetry-identifies-test-var-phase
  (let [events (atom [])
        emit #(swap! events conj %)]
    (#'runner/report-progress-event
     emit
     'gravity.bootstrap-test
     {:type :begin-test-var
      :var #'run-namespaces-preserves-order-output-and-fail-fast-tail})
    (is (= [{:event :test-var-start
             :namespace "gravity.bootstrap-test"
             :test-var "gravity.self-hosting.sh01-ownership-test/run-namespaces-preserves-order-output-and-fail-fast-tail"
             :phase "gravity.self-hosting.sh01-ownership-test/run-namespaces-preserves-order-output-and-fail-fast-tail"
             :active? true}]
           @events))))

(deftest fresh-progress-telemetry-rechecks-parent-before-publication
  (let [directory (Paths/get "target/validation/sh01-progress-race-dir"
                            (make-array String 0))
        progress (.resolve directory "progress.edn")
        outside (Files/createTempDirectory
                 "gravity-fresh-progress-race-outside-"
                 (make-array FileAttribute 0))]
    (try
      (Files/deleteIfExists directory)
      (Files/createDirectories directory
                               (make-array FileAttribute 0))
      (let [emit (#'runner/progress-emitter (str progress))]
        (is (fn? emit))
        ;; Replace the already-validated parent after setup. The per-write
        ;; recheck must disable publication rather than follow the new link.
        (Files/deleteIfExists directory)
        (Files/createSymbolicLink directory outside
                                   (make-array FileAttribute 0))
        (emit {:event :test-var-start
               :namespace "gravity.bootstrap-test"
               :test-var "gravity.bootstrap-test/p15-s23-proof"
               :phase "gravity.bootstrap-test/p15-s23-proof"
               :active? true})
        (is (not (Files/exists (.resolve outside "progress.edn")
                               (make-array LinkOption 0)))))
      (finally
        (Files/deleteIfExists directory)
        (Files/deleteIfExists outside)))))

(deftest run-namespaces-fail-fast-does-not-load-tail-and-reports-error
  (let [events (atom [])
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test
                       'gravity.self-hosting.sh01-parallel-test-runner-test]
          :fail-fast? true
          :require-fn #(swap! events conj [:require %])
          :run-tests-fn
          (fn [namespace]
            (swap! events conj [:run namespace])
            (if (= namespace 'gravity.self-hosting.sh01-ownership-test)
              {:test 1 :pass 0 :fail 1 :error 0}
              {:test 1 :pass 1 :fail 0 :error 0}))})]
    (is (= [[:require 'gravity.self-hosting.sh01-ownership-test]
            [:run 'gravity.self-hosting.sh01-ownership-test]]
           @events))
    (is (= :failed (:status report)))
    (is (= 1 (:exit-code report)))
    (is (= ['gravity.self-hosting.sh01-parallel-test-runner-test]
           (:skipped-namespaces report)))
    (is (= :failed (get-in report [:namespace-results 0 :status])))))

(deftest namespace-output-cap-never-emits-a-split-utf8-codepoint
  (let [ascii (apply str (repeat 8191 "a"))
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test]
          :require-fn (fn [_] nil)
          :run-tests-fn
          (fn [_]
            (print (str ascii "€"))
            {:test 0 :pass 0 :fail 0 :error 0})})
        output (get-in report [:namespace-results 0 :stdout])]
    (is (= 8191 (:bytes output)))
    (is (= 8194 (:observed-bytes output)))
    (is (not (str/includes? (:text output) "�")))
    (is (= 8191 (count (:text output))))))

(deftest duplicate-dedicated-namespace-mappings-are-rejected
  (let [resource (io/resource "gravity/self_hosting")
        test-root (io/file (.toURI resource))
        directories-var
        (ns-resolve
         'gravity.self-hosting-test-runner
         'file-resource-directories)
        exception
        (with-redefs-fn
          {directories-var (fn [] [test-root test-root])}
          (fn []
            (try
              (runner/dedicated-test-namespaces)
              nil
              (catch clojure.lang.ExceptionInfo exception exception))))]
    (is (= "SH01-TEST-NAMESPACE-COLLISION"
           (:id (ex-data exception))))
    (is (contains?
         (:collisions (ex-data exception))
         'gravity.self-hosting.sh01-ownership-test))))
