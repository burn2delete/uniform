(ns gravity.self-hosting.source-fragment-binding-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity]))

(defn- object [path source]
  (let [bytes (.getBytes source "UTF-8")]
    {:path path :mode "100644" :blob-oid (apply str (repeat 40 "a"))
     :byte-count (alength bytes)
     :raw-sha256 (str "sha256:" (digest/sha256-bytes-hex bytes)) :bytes bytes}))

(def root-path "bootstrap/clojure/src/test/checker.clj")
(def fragment-path "bootstrap/clojure/src/test/checker/body.clj")
(def nested-path "bootstrap/clojure/src/test/checker/nested.clj")

(defn- authority [fragment]
  {:content {:commit (apply str (repeat 40 "b"))
             :tree (apply str (repeat 40 "c"))}
   :source-objects
   {root-path (object root-path "(ns test.checker)\n(load \"/test/checker/body\")\n")
    fragment-path (object fragment-path fragment)
    nested-path (object nested-path "(def second-value 2)\n")}
   :external-source-records {}})

(deftest ordered-products-bind-every-authenticated-fragment
  (let [source (authority "(def first-value 1)\n(load \"/test/checker/nested\")\n")
        product (identity/source-product source root-path)]
    (is (= '[(ns test.checker) (def first-value 1) (def second-value 2)]
           (:forms product)))
    (is (= [fragment-path nested-path] (mapv :relative-path (:records product))))
    (is (= (set [root-path fragment-path nested-path])
           (set (map :relative-path
                     (:local-source-records
                      (identity/source-closure-manifest source [root-path]))))))))

(deftest objects-cannot-be-missing-substituted-or-executable
  (let [source (authority "(def first-value 1)\n")]
    (doseq [changed [(update source :source-objects dissoc fragment-path)
                     (assoc-in source [:source-objects fragment-path :mode] "100755")
                     (assoc-in source [:source-objects fragment-path :bytes]
                               (.getBytes "(def first-value 99)\n" "UTF-8"))]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (identity/source-product changed root-path))))))

(deftest unsafe-loads-cycles-and-namespace-substitution-reject
  (doseq [fragment ["(load \"../foreign\")"
                   "(load \"/test/../foreign\")"
                   "(load \"/test/checker\")"
                   "(ns substituted.namespace)\n(def first-value 1)"]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/source-product (authority fragment) root-path)))))

(deftest fragment-receipts-use-honest-owner-records
  (let [source (authority "(def first-value 1)\n")
        root {:namespace 'test.checker :relative-path root-path
              :logical-coordinate {:artifact 'project/bootstrap-clojure-src
                                   :commit (get-in source [:content :commit])
                                   :tree (get-in source [:content :tree])}}
        records (identity/loaded-source-fragments source [root root])
        fragment (first records)]
    (is (= 1 (count records)))
    (is (= #{:owner-namespace :logical-coordinate :relative-path
             :blob-oid :byte-count :sha256} (set (keys fragment))))
    (is (= 'test.checker (:owner-namespace fragment)))
    (is (= fragment-path (:relative-path fragment)))
    (is (= (get-in source [:source-objects fragment-path :raw-sha256])
           (:sha256 fragment)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/loaded-source-fragments
                  source [(assoc root :namespace 'substituted.owner)])))))

(deftest namespace-dependencies-in-fragments-remain-in-the-closure
  (let [dependency "bootstrap/clojure/src/test/dependency.clj"
        source (-> (authority "(require 'test.dependency)\n(def first-value 1)\n")
                   (assoc-in [:source-objects dependency]
                             (object dependency "(ns test.dependency)\n(def value 4)\n")))
        paths (set (map :relative-path
                        (:local-source-records
                         (identity/source-closure-manifest source [root-path]))))]
    (is (contains? paths dependency))))

(deftest fragment-semantic-changes-change-the-expanded-product
  (is (not= (:forms (identity/source-product
                    (authority "(def first-value 1)\n") root-path))
            (:forms (identity/source-product
                    (authority "(def first-value 2)\n") root-path)))))

(deftest facade-identities-bind-the-same-algorithm-and-distinct-source-closure
  (let [spec (-> (get identity/checker-specs :producer-census)
                 (assoc :namespace 'test.checker :entrypoint 'test.checker/main
                        :source-relative-path root-path))
        monolith (assoc-in (authority "(defn main [] 1)")
                          [:source-objects root-path]
                          (object root-path "(ns test.checker)\n(defn main [] 1)\n"))]
    (with-redefs [identity/checker-specs {:producer-census spec}]
      (let [original (identity/checker-identity-manifest monolith :producer-census)
            facade (identity/checker-identity-manifest
                    (authority "(defn main [] 1)") :producer-census)
            changed (identity/checker-identity-manifest
                     (authority "(defn main [] 2)") :producer-census)]
        (is (= (get-in original [:checker :semantic-id])
               (get-in facade [:checker :semantic-id])))
        (is (not= (get-in original [:checker :artifact-id])
                  (get-in facade [:checker :artifact-id])))
        (is (not= (get-in facade [:checker :semantic-id])
                  (get-in changed [:checker :semantic-id])))
        (is (not= (get-in facade [:checker :dependency-closure-id])
                  (get-in changed [:checker :dependency-closure-id])))))))
