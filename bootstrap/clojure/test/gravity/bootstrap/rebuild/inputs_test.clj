(ns gravity.bootstrap.rebuild.inputs-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap.rebuild.inputs :as inputs])
  (:import [java.nio.file Files]))

(def ^:private manifest-path "contracts/bootstrap-inputs.edn")

(defn- temporary-manifest!
  [manifest]
  (let [file (Files/createTempFile "gravity-bootstrap-inputs-" ".edn"
                                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (spit (.toFile file) (pr-str manifest))
    (.toFile file)))

(defn- with-temporary-manifest
  [manifest f]
  (let [file (temporary-manifest! manifest)]
    (try
      (f (.getPath file))
      (finally
        (Files/deleteIfExists (.toPath file))))))

(defn- manifest
  []
  (edn/read-string (slurp manifest-path)))

(defn- with-pinned-root
  [manifest-path f]
  (let [declaration (inputs/read-manifest manifest-path)
        materialize! (deref (resolve 'gravity.bootstrap.rebuild.inputs/materialize-source!))
        delete-tree! (deref (resolve 'gravity.bootstrap.rebuild.inputs/delete-tree!))
        snapshot (materialize! "." declaration)]
    (try
      (f (.toString (:root snapshot)))
      (finally
        (delete-tree! (:root snapshot))
        (Files/deleteIfExists (:archive snapshot))))))

(deftest pinned-source-declaration-authenticates
  (with-pinned-root
    manifest-path
    (fn [root]
      (let [loaded (inputs/load-inputs! root manifest-path)
            entries (get-in loaded [:source-graph :entries])]
        (is (= :gravity/bootstrap-inputs-v2
               (get-in loaded [:manifest :schema])))
        (is (= "d351310d5821a983fac2ad0fee4c0be96711b4b4"
               (get-in loaded [:manifest :source-revision :commit])))
        (is (= "sha256:ced6c4ac05c3215a627f1bdac5d8e6f3cd966ad9be0f35fe9dbb555457c6b808"
               (:manifest-sha256 loaded)))
        (is (= "sha256:c78ccb773f2e89ee3c7aaf7190703d2a3ed23f301ed76fd0bf75135a08556c50"
               (get-in loaded [:source-graph :sha256])))
        (is (< 2000 (count entries)))
        (is (= (get-in loaded [:source-graph :sha256])
               (inputs/source-graph-sha256 entries)))))))

(deftest missing-and-substituted-inputs-fail-closed
  (testing "a changed lockfile byte is observed in the pinned snapshot"
    (with-pinned-root
      manifest-path
      (fn [root]
        (spit (str root "/deps.edn")
              (str (slurp (str root "/deps.edn")) "\n"))
        (let [error (try
                      (inputs/load-inputs! root manifest-path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-DIRECT-DIGEST-MISMATCH"
                 (:diagnostic (ex-data error))))))))
  (testing "a changed source graph identity is rejected"
    (with-pinned-root
      manifest-path
      (fn [root]
        (spit (str root "/bootstrap/gravity/source-graph-drift.gravity")
              "(ns source-graph-drift)\n")
        (let [error (try
                      (inputs/load-inputs! root manifest-path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-SOURCE-GRAPH-MISMATCH"
                 (:diagnostic (ex-data error)))))))))

(deftest declaration-substitution-fails-before-development-build
  (doseq [[label changed expected]
          [["source revision"
            (assoc-in (manifest) [:source-revision :commit] (apply str (repeat 40 "0")))
            "BOOT-INPUTS-MALFORMED"]
           ["scope profile"
            (assoc-in (manifest) [:scope :compiler-profiles] [:native])
            "BOOT-INPUTS-MALFORMED"]
           ["execution backend"
            (assoc-in (manifest) [:execution :backend] :wasm)
            "BOOT-INPUTS-INCOMPATIBLE"]
           ["direct input inventory"
            (assoc-in (manifest) [:direct-inputs 0 :path] "README.md")
            "BOOT-INPUTS-MALFORMED"]
           ["source graph declaration"
            (assoc-in (manifest) [:source-graph :sha256]
                      "sha256:0000000000000000000000000000000000000000000000000000000000000000")
            "BOOT-INPUTS-MALFORMED"]
           ["comparison artifact kind"
            (assoc-in (manifest) [:comparison :artifact-kinds]
                      [:compiler-artifact :diagnostic-stream])
            "BOOT-INPUTS-MALFORMED"]
           ["seed artifact"
            (assoc-in (manifest) [:toolchain :seed-artifact-id]
                      "sha256:0000000000000000000000000000000000000000000000000000000000000000")
            "BOOT-INPUTS-MALFORMED"]]]
    (testing label
      (with-temporary-manifest
        changed
        (fn [path]
          (let [error (try
                        (inputs/read-manifest path)
                        nil
                        (catch clojure.lang.ExceptionInfo error error))]
            (is error)
            (is (= expected (:diagnostic (ex-data error))))))))))

(deftest child-selected-classpath-substitution-fails-before-fixtures
  (let [declaration (inputs/read-manifest manifest-path)
        toolchain-record! (deref (resolve 'gravity.bootstrap.rebuild.inputs/toolchain-record!))]
    (doseq [[label changed]
            [["child-selected seed jar"
              (assoc-in declaration [:toolchain :seed-artifact-id]
                        "sha256:0000000000000000000000000000000000000000000000000000000000000000")]
             ["child dependency jar"
              (assoc-in declaration [:toolchain :dependency-jars 0 :sha256]
                        "sha256:0000000000000000000000000000000000000000000000000000000000000000")]]]
      (testing label
        (with-pinned-root
          manifest-path
          (fn [root]
            (let [error (try
                          (toolchain-record! root changed)
                          nil
                          (catch clojure.lang.ExceptionInfo error error))]
              (is error)
              (is (= "BOOT-INPUTS-TOOLCHAIN-MISMATCH"
                     (:diagnostic (ex-data error)))))))))))

(deftest development-build-consumes-real-compiler-fixtures
  (let [{:keys [inputs result]} (inputs/run-development-build! "." manifest-path)
        fixtures (:fixtures result)
        accepted (some #(when (= :accepted (:expect %)) %) fixtures)
        rejected (some #(when (= :rejected (:expect %)) %) fixtures)]
    (is (= (get-in inputs [:source-graph :sha256])
           (:source-graph-sha256 result)))
    (is (= "d351310d5821a983fac2ad0fee4c0be96711b4b4"
           (get-in result [:source-revision :commit])))
    (is (= "sha256:ced6c4ac05c3215a627f1bdac5d8e6f3cd966ad9be0f35fe9dbb555457c6b808"
           (:manifest-sha256 result)))
    (is (= "sha256:dfdee131460216a84fef22e537bb0d0579965f8f70338d748323dff26f8e5a8b"
           (get-in result [:toolchain :seed-artifact-id])))
    (is (= ["core.specs.alpha-0.4.74.jar" "spec.alpha-0.5.238.jar"]
           (mapv :file (get-in result [:toolchain :dependency-jars]))))
    (is (= [:directory :directory :jar :jar :jar]
           (mapv :kind (get-in result [:toolchain :classpath-entries]))))
    (is (= 0 (:exit-code accepted)))
    (is (= :gravity/stage0-core-artifact (:artifact-kind accepted)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id accepted)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:stdout-sha256 accepted)))
    (is (not= (:artifact-id accepted) (:stdout-sha256 accepted)))
    (is (not (zero? (:exit-code rejected))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:diagnostic-id rejected)))
    (is (= ["L2-UNKNOWN-CORE-FORM"] (:diagnostic-codes rejected)))
    (is (= (:artifact-id accepted) (:artifact-id result)))))
