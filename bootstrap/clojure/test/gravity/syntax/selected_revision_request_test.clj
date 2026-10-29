(ns gravity.syntax.selected-revision-request-test
  "Request/file controls only: no owner, launcher, broker or profiler."
  (:require [clojure.test :refer [deftest is]] [clojure.java.io :as io]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-acceptance :as acceptance]
            [gravity.syntax.selected-revision-request :as request]))

(deftest direct-request-is-exact-and-unlaunchable-with-missing-finalizer
  (let [directory (.getCanonicalFile (.toFile (java.nio.file.Files/createTempDirectory "C3-request-control-"
                           (make-array java.nio.file.attribute.FileAttribute 0))))
        path #(str (io/file directory %))
        _ (.mkdirs (io/file directory "jdk/bin"))
        _ (.mkdirs (io/file directory "broker"))
        _ (doseq [p ["jdk/bin/java" "clojure.jar" "deps.edn" "broker/policy.edn"]] (spit (path p) "fixture"))
        source {:head "fixture-head" :tree "fixture-tree"}
        env {:Java-options (zipmap request/environment-keys (repeat nil)) :inherited-sha256 "fixture"}
        options {:root (str directory) :source source :directory (path "request")
                 :java-home (path "jdk") :jars [(path "clojure.jar")]
                 :coordination-root (path "broker")}]
    (try
      (with-redefs [h/prepare-inputs! (fn [root source] {:root root :source source})
                    h/recheck-inputs! (fn [_] nil)
                    acceptance/source-closure-paths (fn [_] [(path "deps.edn")])
                    request/environment (constantly env)]
        (let [r (request/prepare! options)
              input (:prepared-input r)]
          (is (= source (:source r)))
          (is (= (str directory) (:cwd r)))
          (is (= [(path "jdk/bin/java") "-Xmx2g" "-cp"] (subvec (:argv r) 0 3)))
          (is (= 7 (count (:argv r))))
          (is (false? (:launchable r)))
          (is (= :never-submitted (:execution r)))
          (is (= :not-yet-published (get-in r [:missing-interfaces :retained-finalizer :API])))
          (is (= :unbound (get-in r [:missing-interfaces :whole-watchdog :API])))
          (is (= [:C :L] (get-in r [:guard-proposal :acquire-order])))
          (is (= [:L :C] (get-in r [:guard-proposal :release-order])))
          (is (false? (get-in r [:participation :profiler])))
          (is (false? (get-in r [:participation :warm-session])))
          (is (every? #(not (.exists (io/file %))) (vals (:outputs r))))
          (is (= (:sha256 (:request-file r)) (:sha256 (request/fingerprint (path "request/request.edn")))))
          (is (= (:argv r) (get-in (request/read-prepared! (:path input) (:sha256 input)) [:closure :argv])))
          (is (= :request-directory-already-exists
                 (try (request/prepare! options) nil
                      (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
          (with-redefs [request/environment (constantly (assoc env :inherited-sha256 "changed"))]
            (is (= :launcher-environment-drift
                   (try (request/read-prepared! (:path input) (:sha256 input)) nil
                        (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
          (spit (:path input) "changed")
          (is (= :request-input-drift
                 (try (request/child! (:path input) (:sha256 input)) nil
                      (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
          (is (nil? (find-ns 'gravity.bootstrap)))
          (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker)))
          (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))))
      (finally (doseq [f (reverse (file-seq directory))] (.delete ^java.io.File f))))))
