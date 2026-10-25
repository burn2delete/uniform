(ns gravity.self-hosting.sh07-public-development-ingress-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-INGRESS-ROOT"})))))

(defn- failure [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- temporary-root []
  (.toRealPath (java.nio.file.Files/createTempDirectory "sh07-public-ingress-"
                 (make-array java.nio.file.attribute.FileAttribute 0)) (make-array java.nio.file.LinkOption 0)))

(defn- write! [directory name bytes]
  (let [path (.resolve directory name)]
    (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
    (str path)))

(defn- delete! [directory]
  (with-open [paths (java.nio.file.Files/walk directory (make-array java.nio.file.FileVisitOption 0))]
    (doseq [path (reverse (iterator-seq (.iterator paths)))] (java.nio.file.Files/deleteIfExists path))))

(deftest public-api-options-are-closed-before-target-read
  (let [reads (atom 0)]
    (with-redefs [b/read-gravity-source-text (fn [_] (swap! reads inc) (throw (AssertionError. "Unexpected target read")))]
      (doseq [options [nil [] {:reader :current} {:reader "/tmp/reader.gravity"}
                       {:reader :development-current :reader-source-path "/tmp/reader.gravity"}
                       {:reader :development-current :revision-status :admitted} {:admitted? true}]]
        (is (= "SH07-API-OPTIONS" (:id (failure #(b/sh07-public-core-file-artifact "not-read.gravity" options)))))))
    (is (zero? @reads))))

(deftest default-public-api-retains-pinned-constructor
  (let [calls (atom [])]
    (with-redefs [b/read-gravity-source-text (fn [path] (swap! calls conj [:read path]) "retained source")
                  b/sh07-core-source-artifact (fn [path text] (swap! calls conj [:pinned path text]) :pinned-result)
                  b/sh03-reader-resolve-source-path (fn [] (throw (AssertionError. "Unexpected development selection")))]
      (is (= :pinned-result (b/sh07-public-core-file-artifact "module.gravity")))
      (is (= :pinned-result (b/sh07-public-core-file-artifact "module.qst" {}))))
    (is (= [[:read "module.gravity"] [:pinned "module.gravity" "retained source"]
            [:read "module.qst"] [:pinned "module.qst" "retained source"]] @calls))))

(deftest public-cli-options-have-exact-arity-and-spelling
  (let [parse (ns-resolve 'gravity.bootstrap 'sh07-public-core-cli-input)]
    (is (= ["module.gravity" {}] (parse ["sh07-core" "module.gravity"])))
    (is (= ["module.qst" {:reader :development-current}]
           (parse ["sh07-core" "module.qst" "--development-current-reader"])))
    (doseq [arguments [["sh07-core"] ["sh07-core" "--unknown"]
                      ["sh07-core" "module.gravity" "--unknown"]
                      ["sh07-core" "module.gravity" "extra.gravity"]
                      ["sh07-core" "module.gravity" "--development-current-reader" "extra"]
                      ["sh07-core" "module.gravity" "--development-current-reader" "--development-current-reader"]
                      ["sh07-core" "--development-current-reader" "module.gravity"]]]
      (is (= "SH07-CLI-ARGS" (:id (failure #(parse arguments))))))))

(deftest strict-public-source-policy-applies-to-both-modes
  (let [directory (temporary-root)
        invalid (byte-array [-61 40])
        text (.getBytes "(ns invalid.extension (:profile :meta))\n" java.nio.charset.StandardCharsets/UTF_8)]
    (try
      (doseq [[name bytes id end] [["module.txt" text "L1-SOURCE-EXTENSION" 0]
                                   ["invalid.gravity" invalid "L1-SOURCE-ENCODING" 1]
                                   ["invalid.qst" invalid "L1-SOURCE-ENCODING" 1]]
              :let [path (write! directory name bytes)
                    source-id (str "sha256:" (b/sha256-bytes-hex bytes))]
              options [{} {:reader :development-current}]]
        (let [diagnostic (failure #(b/sh07-public-core-file-artifact path options))]
          (is (= id (:id diagnostic)))
          (is (= [:read-source :error] ((juxt :stage :severity) diagnostic)))
          (is (= {:source path :byte-start 0 :byte-end end} (:source-span diagnostic)
                 (get-in diagnostic [:primary :span])))
          (is (= source-id (:source-id diagnostic) (get-in diagnostic [:primary :artifact])))
          (is (and (nil? (:profile diagnostic)) (nil? (:target diagnostic))))))
      (finally (delete! directory)))))

(def observed-snapshot-product (atom nil))

(deftest development-public-api-retains-the-single-target-snapshot
  (let [directory (temporary-root)
        fixture (io/file (root) "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.gravity")
        bytes (java.nio.file.Files/readAllBytes (.toPath fixture))
        path (write! directory "module.gravity" bytes)
        reads (atom []) parents (atom []) original-read b/read-gravity-source-text
        original-c5 b/compiler-c5-resolution-source-artifact]
    (try
      (let [artifact
            (with-redefs [b/read-gravity-source-text
                          (fn [requested]
                            (when (= path requested) (swap! reads conj requested))
                            (original-read requested))
                          b/compiler-c5-resolution-source-artifact
                          (fn [& arguments]
                            (let [resolved (apply original-c5 arguments)]
                              (swap! parents conj (:status resolved))
                              (java.nio.file.Files/delete (.toPath (io/file path)))
                              resolved))]
              (b/sh07-public-core-file-artifact path {:reader :development-current}))
            ingress (get-in artifact [:provenance :source-ingress])
            boundary (:gravity-core-boundary artifact)
            c2 (get-in artifact [:sh06-resolution-artifact :sh05-macro-artifact
                                :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
            reader (b/sh03-reader-resolve-source-path)]
        (is (= [path] @reads))
        (is (= [:accepted] @parents))
        (is (not (.exists (io/file path))))
        (is (= :accepted (:status artifact)))
        (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
        (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
        (is (= [:development-current-reader :development-unadmitted] ((juxt :mode :revision-status) ingress)))
        (is (= reader (:reader-source-path ingress)))
        (is (= (str "sha256:" (b/sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath (io/file reader)))))
               (get-in ingress [:reader-source-revision :source-content-hash])
               (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
        (is (= path (get-in artifact [:provenance :source-path])
               (get-in boundary [:canonical-core-artifact :provenance :actual-source-path])))
        (is (= (str "sha256:" (b/sha256-bytes-hex bytes)) (get-in c2 [:source-unit-record :bytes-hash])))
        (is (false? (:target-source-reread? boundary)))
        (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
        (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
        (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
               (get-in artifact [:execution-boundary :downstream-fact-statuses])))
        (reset! observed-snapshot-product artifact))
      (finally (delete! directory)))))
