(ns gravity.bootstrap.rebuild.inputs-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
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

(deftest repository-declaration-authenticates
  (let [loaded (inputs/load-inputs! "." manifest-path)
        entries (get-in loaded [:source-graph :entries])]
    (is (= :gravity/bootstrap-inputs-v1
           (get-in loaded [:manifest :schema])))
    (is (= :gravity (get-in loaded [:compiler :source-language])))
    (is (= :clojure (get-in loaded [:compiler :seed-language])))
    (is (<= 1 (count entries)))
    (is (= (get-in loaded [:source-graph :sha256])
           (inputs/source-graph-sha256 entries)))))

(deftest rebuild-executor-receives-authenticated-inputs
  (let [seen (atom nil)
        loaded (inputs/load-inputs! "." manifest-path)
        graph-id (get-in loaded [:source-graph :sha256])
        result (inputs/run-rebuild!
                "."
                manifest-path
                (fn [verified]
                  (reset! seen verified)
                  {:artifact-id "sha256:synthetic-bootstrap-artifact"
                   :source-graph-sha256 (get-in verified [:source-graph :sha256])}))]
    (is (= graph-id (get-in @seen [:source-graph :sha256])))
    (is (= "sha256:synthetic-bootstrap-artifact"
           (get-in result [:result :artifact-id])))))

(deftest direct-input-substitution-is-rejected
  (let [changed (assoc-in (manifest) [:direct-inputs 0 :sha256]
                          (str "sha256:" (apply str (repeat 64 "0"))))]
    (with-temporary-manifest
      changed
      (fn [path]
        (let [error (try
                      (inputs/load-inputs! "." path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-DIRECT-DIGEST-MISMATCH"
                 (:diagnostic (ex-data error)))))))))

(deftest missing-source-root-is-rejected
  (let [changed (assoc-in (manifest) [:source-roots 0 :path]
                          "bootstrap/gravity/missing-source-root")]
    (with-temporary-manifest
      changed
      (fn [path]
        (let [error (try
                      (inputs/load-inputs! "." path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-MISSING-ROOT"
                 (:diagnostic (ex-data error)))))))))

(deftest incompatible-target-is-rejected-before-source-walk
  (let [changed (assoc-in (manifest) [:execution :backend] :wasm)]
    (with-temporary-manifest
      changed
      (fn [path]
        (let [error (try
                      (inputs/load-inputs! "." path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-INCOMPATIBLE"
                 (:diagnostic (ex-data error)))))))))

(deftest source-graph-identity-drift-is-rejected
  (let [changed (assoc-in (manifest) [:source-graph :sha256]
                          (str "sha256:" (apply str (repeat 64 "f"))))]
    (with-temporary-manifest
      changed
      (fn [path]
        (let [error (try
                      (inputs/load-inputs! "." path)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is error)
          (is (= "BOOT-INPUTS-SOURCE-GRAPH-MISMATCH"
                 (:diagnostic (ex-data error)))))))))
