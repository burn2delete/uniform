(ns gravity.self-hosting.sh03-current-decode-identity-test
  "Current Reader byte decoding and declared source identity; no Core admission."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh03-decode-reference :as reference]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(def ^:dynamic *phase* :population)
(def ^:dynamic *reference* nil)
(def ^:dynamic *observations* nil)
(def ^:dynamic *progress* (fn [_] nil))

(defn repository-root []
  (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh03_current_decode_identity_test.clj")))]
    (cond (nil? file) (throw (ex-info "Decode test source root is missing" {}))
          (.isFile (io/file file "deps.edn")) (.getCanonicalFile file)
          :else (recur (.getParentFile file)))))
(defn absolute [relative] (.getCanonicalPath (io/file (repository-root) relative)))
(defn- source-path [extension]
  (absolute (str "bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/current-decode-mixed" extension)))
(defn- reader-path [] (absolute b/sh03-reader-source-relative-path))
(defn- context []
  {:project-root-id (reference/sha256 (.getBytes "Reader decode declared project" "UTF-8"))
   :project-root-path (.getPath (repository-root))
   :project-relative-path "decoder/unit.gravity"})
(defn- source-bytes [path] (java.nio.file.Files/readAllBytes (.toPath (io/file path))))
(defn- check-span [positions span]
  (doseq [[key side] [[:byte-start :start] [:byte-end :end]]]
    (let [position (positions (get span key))]
      (is (some? position))
      (is (= (:line position) (get-in span [side :line])))
      (is (= (:column position) (get-in span [side :column])))
      (is (= (:utf16 position) (get-in span [side :char])))
      (is (= :unicode-scalar (get-in span [side :column-unit]))))))

(defn- observe [label extension options project]
  (*progress* label)
  (let [path (source-path extension) input (source-bytes path)
        before (aclone input) text (reference/strict-text input)
        positions (reference/positions text)
        _ (is (java.util.Arrays/equals input (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)))
        products (b/c2-reader-products path text options project (reader-path))
        raw (:sh03-reader-raw-result products)
        result (:sh03-reader-result products)
        verification (:sh03-reader-verification-report products)
        unit (:source-unit products)
        envelope (:sh02-reader-envelope products)
        descriptor (:sh02-reader-envelope-descriptor products)
        actual-envelope-verification
        (b/p15-s23-stage2-sh02-descriptor-envelope-verify!
          envelope :c2-reader :gravity/sh03-reader-products descriptor path
          {:source :development-current})]
    (is (java.util.Arrays/equals before input))
    (is (= :accepted (:status result)))
    (is (= :accepted (:status verification)))
    (is (true? (:verified? verification)))
    (is (= :accepted (:reader-result-status verification)))
    (is (= :passed actual-envelope-verification))
    (is (= :development-unadmitted (get-in envelope [:source-rule :admission-status])))
    (is (= :gravity/sh03-current-source-reader-plan-binding
           (get-in products [:sh03-reader-plan-binding :artifact])))
    (is (= (reference/sha256 (source-bytes (reader-path)))
           (get-in products [:sh03-reader-plan-binding :source-content-hash])))
    (is (= (reference/sha256 (source-bytes (absolute "bootstrap/gravity/p15_s23/emitter.gravity")))
           (:source-content-hash b/*p15-s23-stage2-compiler-artifact-binding*)))
    (is (= path (:path unit)))
    (is (= extension (:extension unit)))
    (is (= (case extension ".gravity" :gravity-branded-source ".qst" :qst-theory-source)
           (:source-kind unit)))
    (is (= {:path path :extension extension
            :source-kind (case extension ".gravity" :gravity ".qst" :qst)}
           (:actual-path-provenance raw)))
    (is (= :utf-8 (:encoding unit)))
    (is (= (:project-root-id project) (:project-root unit)))
    (is (= (:project-root-path project) (get-in unit [:project-root-record :path])))
    (is (= (:project-relative-path project) (:project-relative-path unit)))
    (is (= options (:reader-options unit) (get-in unit [:identity-inputs :reader-options])))
    (is (= (:enabled-features options) (:enabled-features unit)))
    (is (= (:extension-policy options) (:extension-policy unit)))
    (is (= (reference/sha256 input) (:bytes-hash unit)))
    (is (= (:retain-comments options) (get-in raw [:source-unit :reader-options :retain-trivia])))
    (doseq [record (concat (:token-stream raw) (:form-tree raw))]
      (let [slice (:raw record) span (:span record)
            start (positions (:byte-start slice)) end (positions (:byte-end slice))]
        (is (= :gravity/source-slice (:artifact slice)))
        (is (= (:scalar start) (:scalar-start slice)))
        (is (= (:scalar end) (:scalar-end slice)))
        (is (= (select-keys span [:byte-start :byte-end])
               (select-keys slice [:byte-start :byte-end])))
        (check-span positions span)))
    (doseq [record (concat (:token-stream products) (:form-tree products))]
      (is (= path (:source-path record)))
      (is (= (:source-id unit) (:source-id record)))
      (is (= (:raw record) (reference/slice input (:span record))))
      (check-span positions (:span record)))
    (if (:retain-comments options)
      (do (is (= text (apply str (map :raw (:token-stream products)))))
          (is (some :trivia? (:token-stream products))))
      (is (not-any? :trivia? (:token-stream products))))
    (*progress* {:case label :event :returned :source-id (:source-id unit)
                 :reader-status (:status result) :verification-status (:status verification)
                 :envelope-status actual-envelope-verification})
    {:source-unit unit
     :reader-identities (:incremental-reader-hashes result)
     :adapter-summary (:sh03-reader-adapter-descriptor products)
     :reader-binding (select-keys (:sh03-reader-plan-binding products)
                       [:artifact :source-content-hash :source-byte-count
                        :plan-semantic-hash :functions-semantic-hash])
     :emitter-binding (select-keys b/*p15-s23-stage2-compiler-artifact-binding*
                       [:source-path :source-content-hash :semantic-hash :artifact-hash])
     :envelope (select-keys envelope [:semantic-envelope-id :provenance-binding-id
                                      :actual-path-provenance])}))

(def ^:private invalid-encodings
  [[:overlong [0xc0 0xaf] 0 1]
   [:truncated [0xe2 0x82] 0 1]
   [:surrogate [0xed 0xa0 0x80] 0 1]
   [:out-of-range [0xf4 0x90 0x80 0x80] 0 1]
   [:after-newline [10 0x80] 1 2]])
(defn- rejected-bytes [options project]
  (doseq [[label values offset line] invalid-encodings]
    (*progress* label)
    (let [path (source-path ".gravity") input (byte-array (map unchecked-byte values))
          before (aclone input)
          resolved (b/sh03-reader-resolved-result! path input project options (reader-path))
          result (:result resolved) verification (:verification-report resolved)
          diagnostic (first (:diagnostics result))
          raised (try (b/sh03-reader-raise-rejection! path input options project result) nil
                      (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (testing (name label)
        (is (java.util.Arrays/equals before input))
        (is (= :rejected (:status result)))
        (is (= :accepted (:status verification)))
        (is (true? (:verified? verification)))
        (is (= :rejected (:reader-result-status verification)))
        (is (= "C2-ENCODING" (:id diagnostic) (:id raised)))
        (is (= offset (get-in diagnostic [:facts :byte-position])))
        (is (= line (get-in diagnostic [:facts :line])))
        (is (= path (get-in raised [:source-span :source])))
        (is (empty? (:token-stream result)))
        (is (empty? (:form-tree result)))
        (*progress* {:case label :event :returned :reader-status (:status result)
                     :verification-status (:status verification)})))))

(deftest selected-source-envelope-path-and-binding-guards-remain-fresh
  ;; Host path/hash checks only; these fixtures grant no product authority.
  (let [path (source-path ".gravity") selected (reader-path)
        input (source-bytes selected)
        expected {:artifact :gravity/sh03-current-source-reader-plan-binding
                  :source-content-hash (reference/sha256 input)
                  :source-byte-count (alength input)}
        invocation {:reader-source-path selected
                    :authenticated-envelope-options {:source :development-current}}
        options (var-get (ns-resolve 'gravity.bootstrap 'sh03-reader-authenticated-envelope-options))
        failure (fn [context binding]
                  (try (options path context binding) nil
                       (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= {:source :development-current} (options path invocation expected)))
    (doseq [binding [(assoc expected :artifact :other)
                     (assoc expected :source-content-hash "sha256:stale")
                     (update expected :source-byte-count inc)]]
      (is (= "C2-HASH" (:id (failure invocation binding)))))
    (doseq [context [(assoc invocation :reader-source-path (str selected ".missing"))
                     (assoc invocation :reader-source-path (source-path ".qst"))
                     (assoc invocation :reader-source-path nil)
                     (assoc invocation :extra true)
                     (assoc invocation :authenticated-envelope-options {})
                     (assoc invocation :authenticated-envelope-options {:source :unknown})]]
      (is (= "C2-HASH" (:id (failure context expected)))))
    (is (= {:source :development-current} (options path invocation expected)))))

(deftest selected-current-decoding-and-source-identity
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test
                                  'current-emitter))]
    (let [options b/standard-reader-options project (context)
          gravity (observe :gravity-keep ".gravity" options project)
          qst (observe :qst-keep ".qst" options project)
          counterparts {:gravity gravity :qst qst}]
      (is (= (get-in gravity [:source-unit :identity-inputs])
             (get-in qst [:source-unit :identity-inputs])))
      (is (= (get-in gravity [:source-unit :source-id]) (get-in qst [:source-unit :source-id])))
      (case *phase*
        :population
        (let [without-trivia (observe :without-trivia ".gravity" (assoc options :retain-comments false) project)
              other-root (observe :other-root ".gravity" options
                           (assoc project :project-root-id
                             (reference/sha256 (.getBytes "Other declared project" "UTF-8"))))
              other-logical (observe :other-logical ".gravity" options
                              (assoc project :project-relative-path "decoder/renamed.gravity"))]
          (doseq [changed [without-trivia other-root other-logical]]
            (is (not= (get-in gravity [:source-unit :source-id])
                      (get-in changed [:source-unit :source-id]))))
          (is (= (get-in gravity [:source-unit :bytes-hash])
                 (get-in without-trivia [:source-unit :bytes-hash])
                 (get-in other-root [:source-unit :bytes-hash])
                 (get-in other-logical [:source-unit :bytes-hash])))
          (rejected-bytes options project))
        :foreign-cwd
        (do (is (map? *reference*))
            (doseq [extension [:gravity :qst]]
              (let [original (get *reference* extension) current (get counterparts extension)]
                (is (= (:source-unit original) (:source-unit current)))
                (is (= (:reader-identities original) (:reader-identities current)))
                (is (= (:adapter-summary original) (:adapter-summary current)))
                (is (= (:reader-binding original) (:reader-binding current)))
                (is (= (:emitter-binding original) (:emitter-binding current)))
                (is (= (get-in original [:envelope :semantic-envelope-id])
                       (get-in current [:envelope :semantic-envelope-id]))))))
        (throw (ex-info "Unknown decode child phase" {:phase *phase*})))
      (when *observations* (reset! *observations* counterparts)))))
