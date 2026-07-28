(ns gravity.self-hosting.sh01-ownership-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting-test-runner :as runner]))

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

(deftest ownership-record-is-total-and-unambiguous
  (let [record (ownership-record)
        module-owners (:module-owners record)
        actual-modules (gravity-module-paths)
        declared-modules (set (keys module-owners))
        slice-ids (set (keys (:slice-owners record)))
        expected-slice-ids
        (set (map #(format "SH-%02d" %) (range 30)))]
    (testing "the countable plan and all current Gravity modules have one owner"
      (is (= :gravity/self-hosting-slice-ownership-v1 (:schema record)))
      (is (= "SH-01" (:slice record)))
      (is (= :complete (:status record)))
      (is (= :master-coordinator (:integration-owner record)))
      (is (= expected-slice-ids slice-ids))
      (is (= actual-modules declared-modules))
      (is (= 38 (count module-owners)))
      (is (every? keyword? (vals module-owners))))
    (testing "coordinator-only integration modules are not assigned to a leaf"
      (doseq [module
              (get-in record [:coordinator-owned :integration-surfaces])
              :when (str/starts-with? module "bootstrap/gravity/src/")]
        (is (= :master-coordinator (get module-owners module)) module)))
    (testing "reserved leaf modules do not collide with current modules"
      (is (empty?
           (set/intersection
            declared-modules
            (set (keys (:reserved-leaf-modules record)))))))))

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
           (runner/select-tests ["--dedicated"])))
    (is (= {:mode :run
            :namespaces ['gravity.self-hosting.sh01-ownership-test]}
           (runner/select-tests
            ["--namespace"
             "gravity.self-hosting.sh01-ownership-test"])))
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
