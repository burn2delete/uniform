(ns gravity.release.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.release :as release]))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error error)))

(defn- with-disposable-package [f]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                           "gravity-release-inventory-"
                           (make-array java.nio.file.attribute.FileAttribute 0)))
        root (.getPath directory)]
    (try
      (with-redefs [bootstrap/p18-t02-build-root root
                    bootstrap/p18-t02-classes-dir (str root "/classes")
                    bootstrap/p18-t02-manifest-path (str root "/MANIFEST.MF")
                    bootstrap/p18-t02-jar-path (str root "/gravity-jvm-cli.jar")]
        (f directory))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (java.nio.file.Files/deleteIfExists (.toPath file)))))))

(deftest namespace-boundary-is-native-inventory-data
  (let [contract (:gravity/namespace-contract (meta #'release/jar-inventory-report))]
    (is (= :native-jar-inventory-reports (:contract-boundary contract)))
    (is (= '#{jar-source-inventory-report jar-inventory-report}
           (set (keys (:public-api contract)))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:performs-io? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= '[clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (= (:gravity/namespace-contract (meta #'release/jar-source-inventory-report)) contract))))

(deftest canonical-source-order-matches-the-existing-archive-reader
  (let [observed (vec (sort (map :jar-entry (bootstrap/p18-t02-packaged-source-entries))))
        direct (release/jar-source-inventory-report #'bootstrap/p18-t02-packaged-source-entries observed)
        wrapped (bootstrap/p18-t02-jar-source-inventory-report observed)]
    (is (= direct wrapped))
    (is (:valid? direct))
    (is (= observed (:expected direct) (:observed direct)))
    (is (= 1 (get-in direct [:entry-frequencies "gravity/darwin_publication.clj"])))
    (doseq [entries [(pop observed) (conj observed (first observed))
                     (conj observed "gravity/ambient.clj") (vec (reverse observed))]]
      (let [report (bootstrap/p18-t02-jar-source-inventory-report entries)]
        (is (false? (:valid? report)))
        (is (= report (release/jar-source-inventory-report
                       #'bootstrap/p18-t02-packaged-source-entries entries)))))))

(deftest whole-file-inventory-preserves-exact-membership-and-refusals
  (let [expected (bootstrap/p18-t02-expected-jar-file-entries)]
    (doseq [[entries valid?] [[expected true] [(vec (reverse expected)) true]
                              [(pop expected) false] [(conj expected (first expected)) false]
                              [(conj expected "ambient/Extra.class") false]
                              [(conj expected nil) false] [(conj expected :not-a-name) false]]]
      (let [report (bootstrap/p18-t02-jar-inventory-report entries)]
        (is (= report (release/jar-inventory-report
                       #'bootstrap/p18-t02-expected-jar-file-entries entries)))
        (is (= valid? (:valid? report)))))
    (is (= ["ambient/Extra.class"]
           (:unexpected (bootstrap/p18-t02-jar-inventory-report
                         (conj expected "ambient/Extra.class")))))
    (is (= [(last expected)]
           (:missing (bootstrap/p18-t02-jar-inventory-report (pop expected)))))))

(deftest expected-inventory-vars-are-read-at-invocation
  (doseq [[port direct wrapped expected]
          [[#'bootstrap/p18-t02-packaged-source-entries release/jar-source-inventory-report
            bootstrap/p18-t02-jar-source-inventory-report [{:jar-entry "z.clj"} {:jar-entry "a.clj"}]]
           [#'bootstrap/p18-t02-expected-jar-file-entries release/jar-inventory-report
            bootstrap/p18-t02-jar-inventory-report ["a.class" "z.class"]]]]
    (doseq [invoke [#(direct port %) wrapped]]
      (let [events (atom [])
            observed (lazy-seq (swap! events conj :observed)
                              (seq (if (map? (first expected)) ["a.clj" "z.clj"] expected)))]
        (with-redefs-fn {port (fn [] (swap! events conj :expected) expected)}
          #(is (:valid? (invoke observed))))
        (is (= [:expected :observed] @events)))
      (let [sentinel (ex-info "inventory dependency failed" {:test :sentinel})
            touched (atom false)
            observed (lazy-seq (reset! touched true) (seq ["unused"]))]
        (with-redefs-fn {port (fn [] (throw sentinel))}
          #(is (identical? sentinel (rejection (fn [] (invoke observed))))))
        (is (false? @touched))))))

(deftest real-disposable-jar-passes-both-inventory-reports
  (with-disposable-package
    (fn [_]
      (let [path (bootstrap/p18-t02-build-packaged-jvm-cli!)
            source-report (bootstrap/p18-t02-jar-source-inventory-report
                           (bootstrap/p18-t02-jar-entries))
            file-report (bootstrap/p18-t02-jar-inventory-report
                         (bootstrap/p18-t02-jar-file-entries))]
        (is (.isFile (io/file path)))
        (is (:valid? source-report))
        (is (:valid? file-report))
        (is (= (:expected file-report) (:observed file-report)))
        (is (re-matches #"sha256:[0-9a-f]{64}" (bootstrap/p18-file-sha256 path)))
        (with-open [jar (java.util.jar.JarFile. path)]
          (is (some? (.getJarEntry jar "gravity/cli/Main.class")))
          (is (some? (.getJarEntry jar "gravity/darwin_publication.clj"))))))))

(deftest real-unexpected-jar-entry-rejects-before-builder-return
  (with-disposable-package
    (fn [directory]
      (let [material (io/file directory "tamper-material")
            entry "ambient/Unexpected.class"
            original-shell bootstrap/p18-t02-shell!]
        (.mkdirs (io/file material "ambient"))
        (spit (io/file material entry) "unexpected material\n")
        (let [error
              (with-redefs [bootstrap/p18-t02-shell!
                            (fn [stage args]
                              (let [result (original-shell stage args)]
                                (when (= "p18-t02-jar" stage)
                                  (original-shell
                                   "actual-test-jar-tampering"
                                   ["jar" "--update" (str "--file=" bootstrap/p18-t02-jar-path)
                                    "--date=2026-01-01T00:00:00Z"
                                    "-C" (.getPath material) entry]))
                                result))]
                (rejection bootstrap/p18-t02-build-packaged-jvm-cli!))
              data (ex-data error)]
          (is (= "P18T02006" (:id data)))
          (is (= :exact-whole-jar-file-inventory (:missing-fact data)))
          (is (= [entry] (:unexpected-file-entries data)))
          (is (= [] (:missing-file-entries data)))
          (is (true? (:packaged-source-inventory-valid? data)))
          (is (= bootstrap/p18-t02-jar-path (get-in data [:source-span :source])))
          (is (= "packaged JVM CLI JAR inventory is invalid" (.getMessage error))))))))
