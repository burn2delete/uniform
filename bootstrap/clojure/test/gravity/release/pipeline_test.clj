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
    (is (= [:observed-file-bytes-and-digests] (:additional-contract-boundaries contract)))
    (is (= '#{jar-source-inventory-report jar-inventory-report file-bytes bytes-sha256 uniform-bytes-sha256 file-sha256}
           (set (keys (:public-api contract)))))
    (is (false? (:authenticates-inputs? contract)))
    (is (true? (:performs-io? contract)))
    (is (false? (:inventory-operations-perform-io? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= '[clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (= '([packaged-source-entries-supplier jar-entries])
           (get-in contract [:public-api 'jar-source-inventory-report :arglists])))
    (is (= '([expected-jar-file-entries-supplier jar-file-entries])
           (get-in contract [:public-api 'jar-inventory-report :arglists])))
    (is (= [:observed-native-entry-names :late-expected-inventory-supplier
            :caller-owned-path-or-byte-array :late-byte-and-file-digest-owner-vars]
           (:artifact-inputs contract)))
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

(deftest expected-inventory-suppliers-are-called-at-invocation
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

(deftest inventory-specific-wrappers-preserve-report-refusals
  (let [inventory [{:path "bootstrap/clojure/src/gravity/custom.clj"
                    :jar-entry "gravity/custom.clj"}]
        source-valid
        (bootstrap/p18-t02-jar-source-inventory-report
         inventory ["gravity/custom.clj"])
        source-duplicate
        (bootstrap/p18-t02-jar-source-inventory-report
         inventory ["gravity/custom.clj" "gravity/custom.clj"])
        file-valid
        (bootstrap/p18-t02-jar-inventory-report
         inventory ["META-INF/MANIFEST.MF" "gravity/custom.clj"])
        file-missing
        (bootstrap/p18-t02-jar-inventory-report
         inventory ["gravity/custom.clj"])]
    (is (true? (:valid? source-valid)))
    (is (= ["gravity/custom.clj"] (:expected source-valid)))
    (is (false? (:valid? source-duplicate)))
    (is (= {"gravity/custom.clj" 2}
           (:entry-frequencies source-duplicate)))
    (is (true? (:valid? file-valid)))
    (is (= ["META-INF/MANIFEST.MF" "gravity/custom.clj"]
           (:expected file-valid)))
    (is (false? (:valid? file-missing)))
    (is (= ["META-INF/MANIFEST.MF"] (:missing file-missing)))))

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

(deftest real-file-byte-and-digest-operations
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-release-file-test-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        file (io/file directory "observed-bytes")
        missing (io/file directory "missing")
        bytes (byte-array (map unchecked-byte [0 127 128 255]))
        expected "89273d2f70b93285bb7ddb4bcee86a5347ca7159352e3cbdd20c23e9d1e507d3"]
    (try
      (java.nio.file.Files/write (.toPath file) bytes (make-array java.nio.file.OpenOption 0))
      (is (= (vec bytes) (vec (release/file-bytes (.getPath file)))))
      (is (= (vec bytes) (vec (bootstrap/p18-file-bytes (.getPath file)))))
      (is (= expected (release/bytes-sha256 bytes) (bootstrap/p18-bytes-sha256 bytes)))
      (is (= (str "sha256:" expected)
             (release/file-sha256 #'bootstrap/p18-bytes-sha256 #'bootstrap/p18-file-bytes (.getPath file))
             (bootstrap/p18-file-sha256 (.getPath file))))
      (is (= "sha256:missing" (bootstrap/p18-file-sha256 (.getPath missing))))
      (is (thrown? java.nio.file.NoSuchFileException (release/file-bytes (.getPath missing))))
      (is (thrown? java.nio.file.NoSuchFileException (bootstrap/p18-file-bytes (.getPath missing))))
      (spit file "")
      (is (= "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
             (bootstrap/p18-file-sha256 (.getPath file))))
      (finally
        (doseq [entry (reverse (file-seq directory))]
          (java.nio.file.Files/deleteIfExists (.toPath entry)))))))

(deftest file-helper-wrapper-ports-and-byte-identity
  (doseq [[wrapped owner arguments port-count]
          [[#'bootstrap/p18-file-bytes #'release/file-bytes '[path] 0]
           [#'bootstrap/p18-bytes-sha256 #'release/bytes-sha256 '[bytes] 0]
           [#'bootstrap/p18-file-sha256 #'release/file-sha256 '[path] 2]]]
    (let [value (Object.) token (Object.) calls (atom nil)]
      (is (= (list arguments) (:arglists (meta wrapped))))
      (with-redefs-fn {owner (fn [& args] (reset! calls args) token)}
        #(is (identical? token (wrapped value))))
      (is (identical? value (last @calls)))
      (is (every? var? (take port-count @calls)))))
  (doseq [[name operation] (ns-publics 'gravity.release)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])) name))))

(deftest uniform-digest-transport-preserves-octets-and-executor-failures
  (let [expected (apply str (repeat 64 "a"))
        calls (atom [])
        execute (fn [arguments]
                  (swap! calls conj arguments)
                  {:status :accepted :value expected})
        sentinel (ex-info "original source execution failure" {:test :identity})]
    (with-redefs [release/bytes-sha256
                  (fn [_] (throw (AssertionError. "legacy digest must not run")))]
      (is (= expected (release/uniform-bytes-sha256
                       execute (byte-array (map unchecked-byte [0 127 128 255])))))
      (is (= expected (release/uniform-bytes-sha256 execute (byte-array 0))))
      (is (= [[[0 127 128 255]] [[]]] @calls))
      (is (identical? sentinel
                      (rejection #(release/uniform-bytes-sha256
                                   (fn [_] (throw sentinel)) (byte-array 0))))))))

(deftest uniform-digest-transport-rejects-wrong-input-and-malformed-output
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (toString [] (throw (AssertionError. "opaque input rendered"))))]
    (doseq [value [nil "abc" [0 1] '(0 1) (int-array [0 1]) opaque]]
      (is (= {:rule "SH20SHA001" :reason :expected-byte-array}
             (ex-data (rejection #(release/uniform-bytes-sha256
                                   (fn [_] (swap! calls inc)) value))))))
    (is (zero? @calls))
    (doseq [result [nil opaque "stdout digest" {}
                    {:status :rejected :diagnostics []}
                    {:status :accepted :value nil}
                    {:status :accepted :value (apply str (repeat 64 "A"))}
                    {:status :accepted :value (apply str (repeat 63 "a"))}
                    {:status :accepted :value (apply str (repeat 65 "a"))}
                    {:status :accepted :value (apply str (repeat 64 "a")) :extra true}]]
      (with-redefs [release/bytes-sha256
                    (fn [_] (throw (AssertionError. "legacy digest must not run")))]
        (is (= {:rule "SH20SHA001" :reason :invalid-kernel-output}
               (ex-data (rejection #(release/uniform-bytes-sha256
                                     (fn [_] result) (byte-array 0))))))))))

(deftest file-digest-preserves-operation-root-lookup-and-failure
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-release-file-order-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        file (io/file directory "input")
        bytes (byte-array [1 2 3])
        sentinel (ex-info "original file dependency failure" {:test :identity})]
    (try
      (spit file "actual regular file")
      (with-local-vars [digest nil read-bytes nil]
        (let [calls (atom [])]
          (var-set digest (fn [value]
                            (swap! calls conj :original-digest)
                            (is (identical? bytes value))
                            "observed-old-root"))
          (var-set read-bytes
                   (fn [path]
                     (swap! calls conj [:read path])
                     (var-set digest (fn [_] (throw (AssertionError. "late digest root"))))
                     bytes))
          (is (= "sha256:observed-old-root"
                 (release/file-sha256 digest read-bytes (.getPath file))))
          (is (= [[:read (.getPath file)] :original-digest] @calls)))
        (var-set digest (fn [_] (throw (AssertionError. "digest must not run"))))
        (var-set read-bytes (fn [_] (throw sentinel)))
        (is (identical? sentinel
                        (try (release/file-sha256 digest read-bytes (.getPath file))
                             (catch Throwable error error))))
        (is (= "sha256:missing"
               (release/file-sha256 digest read-bytes (.getPath (io/file directory "missing"))))))
      (finally
        (doseq [entry (reverse (file-seq directory))]
          (java.nio.file.Files/deleteIfExists (.toPath entry)))))))
