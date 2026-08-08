(ns gravity.self-hosting.sh07-c11-mir-source-preflight-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c11_mir_source_preflight_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C11 source preflight is not on the classpath"
        {:id "SH07-C11-PREFLIGHT-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C11-PREFLIGHT-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")
(def ^:private expected-source-byte-count 113008)
(def ^:private expected-source-revision-id
  "sha256:95fd82d9484d0a1b7a93b3da10ed6c490c7b051e253da0eb1eb58f0f08334fe3")
(def ^:private invalid-export-clause ::invalid-export-clause)

(defn- source-path
  []
  (.resolve @root source-relative-path))

(defn- source-bytes
  []
  (java.nio.file.Files/readAllBytes (source-path)))

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- read-forms
  [reader]
  (binding [*read-eval* false]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))

(defn- source-forms
  []
  (with-open
   [reader
    (clojure.lang.LineNumberingPushbackReader.
     (io/reader (.toFile (source-path))))]
    (read-forms reader)))

(defn- invalid-source-if-forms
  [forms]
  (let [invalid (volatile! [])]
    (walk/postwalk
     (fn [form]
       (when (and (seq? form)
                  (= 'if (first form))
                  (not= 4 (count form)))
         (vswap! invalid conj form))
       form)
     forms)
    @invalid))

(defn- definition-names
  [forms]
  (into
   #{}
   (keep
    (fn [form]
      (when (and (seq? form)
                 (#{'def 'defn} (first form))
                 (symbol? (second form)))
        (second form))))
   forms))

(defn- export-names
  [forms]
  (let [namespace-form (first forms)]
    (if (and (seq? namespace-form)
             (= 'ns (first namespace-form))
             (symbol? (second namespace-form)))
      (let [clauses
            (filter
             #(and (seq? %) (= :exports (first %)))
             (drop 2 namespace-form))]
        (if (= 1 (count clauses))
          (let [clause (first clauses)
                values (second clause)]
            (if (and (= 2 (count clause))
                     (vector? values)
                     (seq values)
                     (every? symbol? values)
                     (= (count values) (count (set values))))
              values
              invalid-export-clause))
          invalid-export-clause))
      invalid-export-clause)))

(defn- missing-export-definitions
  [forms]
  (let [exports (export-names forms)]
    (if (= invalid-export-clause exports)
      invalid-export-clause
      (set (remove (definition-names forms) exports)))))

(deftest sh07-c11-source-control-form-arities-are-exact
  (let [bytes (source-bytes)
        forms (source-forms)]
    (is (= expected-source-byte-count (alength bytes)))
    (is (= expected-source-revision-id (sha256-id bytes)))
    (is (empty? (invalid-source-if-forms forms)))
    (testing "both under- and over-arity if forms fail the same source-only gate"
      (is (= 1 (count (invalid-source-if-forms '[(if true)]))))
      (is (= 1
             (count
              (invalid-source-if-forms
               '[(if true :then :else :extra)])))))))

(deftest sh07-c11-source-exports-have-definitions
  (let [forms (source-forms)
        exports (export-names forms)]
    (is (vector? exports))
    (is (seq exports))
    (is (empty? (missing-export-definitions forms))))
  (testing "malformed namespace and export clauses fail closed"
    (doseq [forms
            ['[(not-ns example (:exports [x])) (def x 1)]
             '[(ns example (:exports [x] :trailing)) (def x 1)]
             '[(ns example (:exports [x]) (:exports [y])) (def x 1) (def y 2)]
             '[(ns example (:exports (x))) (def x 1)]
             '[(ns example (:exports [x :not-a-symbol])) (def x 1)]
             '[(ns example (:exports []))]
             '[(ns example (:exports [x x])) (def x 1)]
             '[(ns example) (def x 1)]]]
      (is (= invalid-export-clause (missing-export-definitions forms)))))
  (testing "a structurally valid export must resolve to a top-level definition"
    (is (= '#{missing}
           (missing-export-definitions
            '[(ns example (:exports [present missing]))
              (def present 1)])))
    (is (empty?
         (missing-export-definitions
          '[(ns example (:exports [value function]))
            (def value 1)
            (defn function [] value)])))))
