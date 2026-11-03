(ns gravity.bootstrap.rebuild.component-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap.rebuild.component :as component])
  (:import [java.nio.file Files LinkOption Path]))

(def ^:private manifest-path "contracts/bootstrap-inputs.edn")

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [^Path path (sort-by #(.getNameCount ^Path %) >
                                  (iterator-seq (.iterator paths)))]
        (Files/deleteIfExists path)))))

(defmacro ^{:clj-kondo/lint-as 'clojure.core/fn} with-output-root
  [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-bootstrap-component-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
     (try
       ~@body
       (finally
         (delete-tree! ~binding)))))

(deftest persisted-component-is-reloaded-for-accepted-and-rejected-inputs
  (with-output-root [output-root]
    (let [result (component/run-seed-built-component!
                  "." manifest-path (component/seed-built-component)
                  (.toString output-root))
          record (:record result)]
      (is (= :complete-for-seed-built-plan (:status record)))
      (is (= :gravity/stage0-hosted-core-compiled-plan
             (get-in record [:plan :kind])))
      (is (true? (get-in record [:execution-child :reloaded-plan?])))
      (is (= "P15GCE002" (get-in record [:diagnostics :code])))
      (is (= :jvm (get-in record [:boundary :input-target])))
      (is (= :c11 (get-in record [:boundary :output-target])))
      (is (string? (get-in result [:output-sha256 :plan])))
      (is (string? (get-in result [:output-sha256 :source])))
      (is (string? (get-in result [:output-sha256 :record])))
      (is (Files/isRegularFile
           (.toPath (java.io.File. (:plan-path result)))
           (make-array LinkOption 0)))
      (is (Files/isRegularFile
           (.toPath (java.io.File. (:source-path result)))
           (make-array LinkOption 0)))
      (is (Files/isRegularFile
           (.toPath (java.io.File. (:record-path result)))
           (make-array LinkOption 0))))))

(deftest component-declaration-substitution-fails-before-child-execution
  (with-output-root [output-root]
    (let [error (try
                  (component/run-seed-built-component!
                   "." manifest-path
                   (assoc (component/seed-built-component)
                          :source-sha256
                          "sha256:0000000000000000000000000000000000000000000000000000000000000000")
                   (.toString output-root))
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is error)
      (is (= "BOOT-COMPONENT-BUILD" (:diagnostic (ex-data error))))
      (is (= :component-declaration (:reason (ex-data error)))))))

(deftest existing-component-output-is-preserved-on-collision
  (with-output-root [output-root]
    (let [output-dir (.resolve ^Path output-root "target/bootstrap-rebuild")
          plan-file (.resolve output-dir
                              "native-plan-c-emitter.plan.edn")
          sentinel "pre-existing-output\n"]
      (Files/createDirectories output-dir
                               (make-array java.nio.file.attribute.FileAttribute 0))
      (spit (.toFile plan-file) sentinel)
      (let [error (try
                    (component/run-seed-built-component!
                     "." manifest-path (component/seed-built-component)
                     (.toString output-root))
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is error)
        (is (= :output-exists (:reason (ex-data error))))
        (is (= sentinel (slurp (.toFile plan-file))))))))

(deftest tampered-persisted-plan-is-rejected-before-public-success
  (with-output-root [output-root]
    (let [writer-var (ns-resolve 'gravity.bootstrap.rebuild.component
                                 'write-new!)
          original-writer @writer-var
          error (try
                  (with-redefs-fn
                    {writer-var
                     (fn [file value]
                       (let [result (original-writer file value)]
                         (when (= "native-plan-c-emitter.plan.edn"
                                  (.getName ^java.io.File file))
                           (spit file "{:tampered true}"))
                         result))}
                    #(component/run-seed-built-component!
                      "." manifest-path (component/seed-built-component)
                      (.toString output-root)))
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is error)
      (is (= "BOOT-COMPONENT-BUILD" (:diagnostic (ex-data error))))
      (is (= :plan-readback-shape (:reason (ex-data error)))))))

(deftest substituted-persisted-plan-source-is-rejected
  (with-output-root [output-root]
    (let [writer-var (ns-resolve 'gravity.bootstrap.rebuild.component
                                 'write-new!)
          original-writer @writer-var
          error (try
                  (with-redefs-fn
                    {writer-var
                     (fn [file value]
                       (let [result (original-writer file value)]
                         (when (= "native-plan-c-emitter.plan.edn"
                                  (.getName ^java.io.File file))
                           (spit file
                                 (pr-str
                                  (assoc-in (edn/read-string
                                             (slurp file))
                                            [:source :sha256]
                                            "sha256:0000000000000000000000000000000000000000000000000000000000000000"))))
                         result))}
                    #(component/run-seed-built-component!
                      "." manifest-path (component/seed-built-component)
                      (.toString output-root)))
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is error)
      (is (= "BOOT-COMPONENT-BUILD" (:diagnostic (ex-data error))))
      (is (= :plan-readback-source-binding (:reason (ex-data error)))))))
