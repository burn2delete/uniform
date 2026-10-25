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
(def observed-fresh-verification (atom nil))
(def ^:dynamic *development-public-product* nil)

(defn- current-public-product []
  ;; A bounded verifier run can supply a separately captured real CLI product.
  ;; The unchanged fresh verifier still reconstructs its expected Core artifact.
  (or *development-public-product*
      (b/sh07-public-core-file-artifact
       (str (io/file (root) "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.gravity"))
       {:reader :development-current})))

(defn- coherent-literal-substitution [artifact]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        index (first (keep-indexed (fn [index node]
                                    (when (and (= :literal (:core-form node))
                                               (instance? Boolean (get-in node [:attributes :value]))) index)) (:nodes core)))
        _ (assert (some? index) "Fixture must contain a Bool literal")
        nodes (update-in (:nodes core) [index :attributes :value] not)
        preimage (assoc (:identity-preimage core) :nodes nodes)
        id (b/reader-canonical-hash {:domain :gravity/sh07-declared-digest-v1
                                     :purpose :sh07-core-artifact-id :preimage preimage})]
    (-> artifact
        (assoc :artifact-id id)
        (assoc-in [:gravity-core-boundary :canonical-core-artifact]
                  (assoc core :artifact-id id :nodes nodes :identity-preimage preimage))
        (assoc-in [:gravity-core-boundary :authenticated-envelope :semantic-artifact-id] id)
        (assoc-in [:gravity-core-boundary :authenticated-envelope-descriptor :semantic-artifact-id] id))))

(deftest development-public-product-passes-fresh-verification
  (let [artifact (current-public-product)
        verified (b/sh07-core-artifact-verification artifact)]
    (is (= :accepted (:status artifact)))
    (is (= #{:source-path} (set (keys (:provenance artifact)))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= :passed (:status verified)))
    (is (empty? (:failed-checks verified)))
    (is (true? (get-in verified [:checks :authoritative-products-replay?])))
    (is (true? (get-in verified [:checks :stored-capability-proof-current?])))
    (reset! observed-fresh-verification {:genuine-report verified :artifact-id (:artifact-id artifact)})))

(deftest development-public-product-rejects-rehashed-core
  (let [artifact (current-public-product)
        substituted (coherent-literal-substitution artifact)
        rejected (b/sh07-core-artifact-verification substituted)]
    (is (not= (:artifact-id artifact) (:artifact-id substituted)))
    (is (= (:capability-based-proof artifact) (:capability-based-proof substituted)))
    (is (= :failed (:status rejected)))
    (is (true? (get-in rejected [:checks :semantic-artifact-id-current?])))
    (is (false? (get-in rejected [:checks :canonical-core-replays?])))
    (is (false? (get-in rejected [:checks :authoritative-products-replay?])))
    (reset! observed-fresh-verification {:substitution-report rejected :artifact-id (:artifact-id artifact)
                                       :substituted-artifact-id (:artifact-id substituted)})))

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
        (is (not (contains? (:provenance artifact) :source-ingress)))
        (is (= #{:source-path} (set (keys (:provenance artifact)))))
        (is (= (str "sha256:" (b/sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath (io/file reader)))))
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

(deftest c3-verification-callbacks-retain-the-host-invocation
  ;; Routing units only. These spies confer no Reader, C3 or AE acceptance.
  (let [operations (var-get (ns-resolve 'gravity.bootstrap 'c3-syntax-verification-ops))
        current {:reader-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
                 :authenticated-envelope-options {:source :development-current}}
        forged-artifact {:invocation-context current}
        cases [['c3-syntax-stream-reader-products-authentic?
                :c3-syntax-stream-reader-products-authentic? [::stream forged-artifact ::boundary]]
               ['c3-syntax-verification-report
                :c3-syntax-verification-report [::stream ::serialization forged-artifact ::boundary]]
               ['c3-syntax-capability-proof :c3-syntax-capability-proof [forged-artifact]]
               ['c3-syntax-validate! :c3-syntax-validate! ["source.gravity" forged-artifact]]]]
    (doseq [invocation [nil current]]
      (let [calls (atom [])
            callbacks (operations invocation)
            replacements
            (into {} (map (fn [[name operation _]]
                            [(ns-resolve 'gravity.bootstrap name)
                             (fn [& arguments]
                               (swap! calls conj [operation (vec arguments)])
                               :routed)])) cases)]
        (with-redefs-fn replacements
          (fn []
            (doseq [[_ operation arguments] cases]
              (is (= :routed (apply (get callbacks operation) arguments))))))
        (is (= (mapv (fn [[_ operation arguments]] [operation (conj arguments invocation)]) cases)
               @calls))))))
