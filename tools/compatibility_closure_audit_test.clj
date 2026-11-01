(ns compatibility-closure-audit-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io])
  (:import [java.nio.file Files Path]
           [java.nio.file.attribute FileAttribute]))

(binding [*command-line-args* nil] (load-file "tools/compatibility_closure_audit.clj"))
(require '[compatibility-closure-audit :as audit])

(def component "bootstrap/clojure/src/gravity/bootstrap_impl/components/probe.clj")
(def second-component "bootstrap/clojure/src/gravity/bootstrap_impl/components/second.clj")
(def data-path "bootstrap/clojure/src/gravity/bootstrap_impl/data/probe.edn")
(def test-path "bootstrap/clojure/test/gravity/probe_test.clj")
(def manifest-path (str "bootstrap/clojure/src/" audit/manifest-resource))
(def options {:namespace 'gravity.probe-test})

(defn put! [root path text]
  (let [file (io/file root path)]
    (io/make-parents file)
    (spit file text)))

(defmacro with-fixture [[root] & body]
  `(let [~root (.toFile (Files/createTempDirectory
                        "compatibility-closure-audit-" (make-array FileAttribute 0)))]
     (try
       (put! ~root test-path "(ns gravity.probe-test (:require [gravity.probe :as p]))")
       (put! ~root "bootstrap/clojure/src/gravity/probe.clj"
             "(ns gravity.probe (:require [gravity.nested :as nested] [clojure.string :as str]))")
       (put! ~root "bootstrap/clojure/src/gravity/nested.clj" "(ns gravity.nested)")
       (put! ~root audit/loader-path "(ns gravity.bootstrap-impl.loader)")
       (put! ~root component
             "(gravity.bootstrap-impl.loader/append-data! :probe \"gravity/bootstrap_impl/data/probe.edn\")")
       (put! ~root second-component "(def probe 2)")
       (put! ~root data-path "[:first]")
       (put! ~root audit/accepted-fixture "(module accepted)")
       (put! ~root manifest-path
             (pr-str [{:resource "gravity/bootstrap_impl/components/probe.clj"}
                      {:resource "gravity/bootstrap_impl/components/second.clj"}]))
       ~@body
       (finally
         (with-open [paths# (Files/walk (.toPath ~root)
                                       (make-array java.nio.file.FileVisitOption 0))]
           (doseq [path# (reverse (vec (.toArray paths#)))]
             (Files/deleteIfExists ^Path path#)))))))

(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo exception (:reason (ex-data exception)))))

(deftest observations-bind-real-inputs-without-cache-authority
  (with-fixture [root]
    (let [report (audit/audit root options)
          paths (set (map :path (:inputs report)))]
      (is (contains? paths "bootstrap/clojure/src/gravity/nested.clj"))
      (is (every? paths [test-path audit/loader-path manifest-path component data-path
                        audit/accepted-fixture]))
      (is (= ['clojure.string] (:external-namespaces report)))
      (is (= 2 (count (:ordered-components report))))
      (is (= :none (:authority report)))
      (is (false? (:closure-complete? report)))
      (is (false? (:cache-narrowing-authorized? report)))
      (is (false? (get-in report [:representation :existing-flat-declaration-eligible?])))
      (is (= :bounded-flat-explicit-inputs (get-in report [:representation :current-kind])))
      (is (seq (:unresolved report)))
      (is (= report (audit/audit root options))))))

(deftest order-bytes-and-included-fixture-changes-alter-observations
  (with-fixture [root]
    (let [original (audit/audit root options)
          fingerprint (:observation-sha256 original)]
      (doseq [path [component data-path audit/accepted-fixture
                    "bootstrap/clojure/src/gravity/nested.clj"]]
        (let [file (io/file root path) before (slurp file)]
          (try
            (spit file (str before "\n; changed"))
            (is (not= fingerprint (:observation-sha256 (audit/audit root options))) path)
            (finally (spit file before)))))
      (let [file (io/file root manifest-path) before (slurp file)]
        (spit file (pr-str (vec (reverse (clojure.edn/read-string before)))))
        (let [changed (audit/audit root options)]
          (is (not= (:ordered-components original) (:ordered-components changed)))
          (is (not= fingerprint (:observation-sha256 changed))))))))

(deftest missing-shadowed-and-escaping-inputs-reject
  (with-fixture [root]
    (let [file (io/file root data-path) before (slurp file)]
      (io/delete-file file)
      (is (= :missing-input (reason #(audit/audit root options))))
      (spit file before))
    (let [shadow (io/file root "bootstrap/clojure/src/gravity/nested.cljc")]
      (spit shadow "(ns gravity.nested)")
      (is (= :namespace-resolution (reason #(audit/audit root options))))
      (io/delete-file shadow))
    (put! root manifest-path "[{:resource \"../outside.clj\"}]")
    (is (= :relative-path (reason #(audit/audit root options))))))

(deftest source-is-never-evaluated-and-dynamic-inputs-remain-unresolved
  (with-fixture [root]
    (let [sentinel (io/file root "must-not-exist")]
      (put! root test-path
            (str "(ns gravity.probe-test)\n#=(spit " (pr-str (str sentinel)) " \"ran\")"))
      (let [report (audit/audit root options)]
        (is (not (.exists sentinel)))
        (is (false? (:closure-complete? report))))
      (put! root test-path
            (str "(ns gravity.probe-test #=(spit " (pr-str (str sentinel)) " \"ran\"))"))
      (is (= :namespace-read (reason #(audit/audit root options))))
      (is (not (.exists sentinel))))
    (put! root test-path "(ns gravity.probe-test)")
    (put! root component "(gravity.bootstrap-impl.loader/append-data! :probe (str \"computed\"))")
    (let [report (audit/audit root options)]
      (is (contains? (set (:unresolved report)) :computed-resource-paths))
      (is (some #(= component (:path %)) (:read-candidates report)))
      (is (false? (:cache-narrowing-authorized? report))))))

(deftest symbolic-link-input-rejects
  (with-fixture [root]
    (let [target (.toPath (io/file root data-path))
          other (.toPath (io/file root "other.edn"))]
      (spit (.toFile other) "[:other]")
      (Files/delete target)
      (Files/createSymbolicLink target other (make-array FileAttribute 0))
      (is (= :symbolic-link (reason #(audit/audit root options)))))))

(deftest literal-initialization-loads-are-recursive-and-closed
  (with-fixture [root]
    (let [path "bootstrap/clojure/src/gravity/loaded.clj"
          nested "bootstrap/clojure/src/gravity/loaded_nested.clj"]
      (put! root "bootstrap/clojure/src/gravity/nested.clj"
            "(ns gravity.nested)\n(load \"/gravity/loaded\")")
      (put! root path "(load \"/gravity/loaded_nested\")")
      (put! root nested "(def loaded-value :one)")
      (let [report (audit/audit root options)]
        (is (= [path nested] (:literal-load-candidates report)))
        (is (every? (set (map :path (:inputs report))) [path nested]))
        (put! root nested "(def loaded-value :two)")
        (is (not= (:observation-sha256 report)
                  (:observation-sha256 (audit/audit root options)))))
      (put! root "bootstrap/clojure/src/gravity/loaded.cljc" "(def shadow true)")
      (is (= :literal-load-resolution (reason #(audit/audit root options))))
      (io/delete-file (io/file root "bootstrap/clojure/src/gravity/loaded.cljc"))
      (io/delete-file (io/file root nested))
      (is (= :literal-load-resolution (reason #(audit/audit root options)))))))

(deftest multiline-read-sites-remain-visible
  (with-fixture [root]
    (put! root component
          "(gravity.bootstrap-impl.loader/append-data!\n :probe\n \"gravity/bootstrap_impl/data/probe.edn\")")
    (is (some #(and (= component (:path %)) (= 1 (:line %)))
              (:read-candidates (audit/audit root options))))))

(deftest all-flat-literal-load-arguments-are-observed
  (with-fixture [root]
    (let [first-path "bootstrap/clojure/src/gravity/first_load.clj"
          second-path "bootstrap/clojure/src/gravity/second_load.clj"]
      (put! root "bootstrap/clojure/src/gravity/nested.clj"
            "(ns gravity.nested)\n(load \"/gravity/first_load\"\n \"/gravity/second_load\")")
      (put! root first-path "(def first-value true)")
      (put! root second-path "(def second-value true)")
      (is (= [first-path second-path] (:literal-load-candidates (audit/audit root options))))
      (io/delete-file (io/file root second-path))
      (is (= :literal-load-resolution (reason #(audit/audit root options))))
      (put! root "bootstrap/clojure/src/gravity/nested.clj"
            "(ns gravity.nested)\n(load \"relative\" (str \"computed\"))")
      (let [report (audit/audit root options)]
        (is (contains? (set (:unresolved report)) :computed-relative-or-unparsed-load-forms))
        (is (false? (:closure-complete? report)))))))

(deftest built-in-and-custom-tag-readers-are-not-invoked
  (with-fixture [root]
    (doseq [literal ["#inst \"2026-10-26\""
                     "#uuid \"00000000-0000-0000-0000-000000000000\""
                     "#probe/custom \"value\""]]
      (put! root manifest-path
            "[{:resource \"gravity/bootstrap_impl/components/probe.clj\"}]")
      (put! root test-path (str "(ns gravity.probe-test (:probe " literal "))"))
      (is (= :namespace-read (reason #(audit/audit root options))))
      (put! root test-path "(ns gravity.probe-test)")
      (put! root manifest-path
            (str "[{:resource \"gravity/bootstrap_impl/components/probe.clj\" :probe " literal "}]"))
      (is (= :tagged-manifest (reason #(audit/audit root options)))))))

(deftest symlinked-audit-root-rejects-before-reading
  (with-fixture [root]
    (let [alias (.toPath (io/file root "root-alias"))]
      (Files/createSymbolicLink alias (.toPath root) (make-array FileAttribute 0))
      (is (= :symbolic-root (reason #(audit/audit (.toFile alias) options)))))))

(let [result (run-tests 'compatibility-closure-audit-test)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
