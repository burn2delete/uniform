(ns gravity.self-hosting.sh04-current-reader-provenance-test
  "Selected-current Reader -> Syntax provenance, not frozen public admission."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(def ^:private fixture-base
  "bootstrap/clojure/fixtures/self-hosting/sh-04/current-reader-provenance")
(defn- path [extension] (.getCanonicalPath (io/file (str fixture-base extension))))
(defn- reader-path []
  (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)))
(defn- invocation []
  {:reader-source-path (reader-path)
   :authenticated-envelope-options {:source :development-current}})
(defn- current-emitter []
  @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test
                        'current-emitter)))
(defn- construct [source text]
  ;; The seven-argument public source constructor builds its own actual C2.
  ;; There is no saved C2, caller report, token or separately built prefix.
  (b/compiler-c3-syntax-source-artifact
    source text (b/reader-project-context-for-source source)
    nil nil (reader-path) (invocation)))
(defn- source-syntax [artifact]
  (filterv #(not= :generated-form (get-in % [:form :kind]))
           (:syntax-object-stream artifact)))
(defn- position-at [bytes offset]
  (let [prefix (String. (java.util.Arrays/copyOfRange bytes 0 (int offset))
                       java.nio.charset.StandardCharsets/UTF_8)
        lines (str/split prefix #"\r\n|\r|\n" -1)
        last-line (peek lines)]
    {:line (count lines)
     :column (inc (.codePointCount ^String last-line 0 (.length ^String last-line)))}))
(defn- scalar-at [bytes offset]
  (let [prefix (String. (java.util.Arrays/copyOfRange bytes 0 (int offset))
                       java.nio.charset.StandardCharsets/UTF_8)]
    (.codePointCount prefix 0 (.length prefix))))

(deftest selected-current-reader-preserves-syntax-spans-origins-and-pair-identity
  (binding [b/*p15-s23-stage2-compiler-artifact-binding* (current-emitter)]
    (let [paths (mapv path [".gravity" ".qst"])
          texts (mapv #(slurp % :encoding "UTF-8") paths)
          _ (is (= (first texts) (second texts)))
          artifacts (mapv construct paths texts)
          proofs (mapv #(b/c3-syntax-capability-proof % (invocation)) artifacts)
          [gravity qst] artifacts
          source-stream (source-syntax gravity)]
      (is (= 2 (count source-stream)))
      (doseq [[source text artifact proof] (map vector paths texts artifacts proofs)]
        (testing source
          (is (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact)))
          (is (= :complete (:status proof)))
          (is (true? (:reader-products-authentic? proof)))
          (is (true? (:syntax-verifier-current? proof)))
          (is (= :development-unadmitted
                 (get-in artifact [:gravity-syntax-boundary :authenticated-envelope
                                   :source-rule :admission-status])))
          (is (false? (get-in artifact [:gravity-syntax-boundary :self-hosted?])))
          (let [bytes (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)
                c2 (:c2-reader-artifact artifact)
                seeds (:syntax-seed-stream c2)
                syntax (source-syntax artifact)
                reader-binding (get-in artifact [:gravity-syntax-boundary
                                                 :reader-semantic-binding])]
            (is (= source (get-in c2 [:source-unit-record :path])))
            (is (= (mapv :form-id seeds) (mapv #(get-in % [:source :form-id]) syntax)))
            (is (= (count seeds) (count syntax) 2))
            (doseq [[seed node] (map vector seeds syntax)]
              (let [span (get-in node [:span :primary])
                    start (:byte-start span) end (:byte-end span)
                    raw (get-in node [:form :raw])
                    origin (first (filter #(= :source (:kind %)) (:origin node)))
                    coordinates [:byte-start :byte-end :scalar-start :scalar-end]]
                (is (= source (:source span)))
                (is (<= 0 start end (alength bytes)))
                (is (= raw (String. (java.util.Arrays/copyOfRange bytes (int start) (int end))
                                    java.nio.charset.StandardCharsets/UTF_8)))
                (is (= (select-keys (:span seed) coordinates) (select-keys span coordinates)))
                (is (= (scalar-at bytes start) (:scalar-start span)))
                (is (= (scalar-at bytes end) (:scalar-end span)))
                (is (= (position-at bytes start)
                       {:line (:line-start span) :column (:column-start span)}))
                (is (= (position-at bytes end)
                       {:line (:line-end span) :column (:column-end span)}))
                (is (= (or (:metadata seed) {}) (:metadata node)))
                (is (= span (:span origin)))
                (is (= 'gravity.bootstrap.reader (get-in origin [:producer :name])))
                (is (= (:reader-result-id reader-binding) (get-in origin [:producer :identity])))))
            (let [node (first syntax) raw (get-in node [:form :raw])]
              (is (> (alength (.getBytes ^String raw java.nio.charset.StandardCharsets/UTF_8))
                     (.codePointCount ^String raw 0 (.length ^String raw))))
              (is (= {:doc "Reader spans" :reader/fixture true} (:metadata node))))
            (let [generated (filterv #(= :generated-form (get-in % [:form :kind]))
                                     (:syntax-object-stream artifact))
                  origin (first (:origin (first generated)))]
              (is (= 1 (count generated)))
              (is (= :generated (:kind origin)))
              (is (= 'gravity.bootstrap.syntax/generated-conformance (get-in origin [:producer :name])))
              (is (contains? (set (:input-syntax-ids origin))
                             (:syntax/id (first syntax))))))))
      (is (= (:artifact-id gravity) (:artifact-id qst)))
      (is (= (mapv :syntax/id (:syntax-object-stream gravity))
             (mapv :syntax/id (:syntax-object-stream qst))))
      (is (= (get-in gravity [:gravity-syntax-boundary :reader-semantic-binding])
             (get-in qst [:gravity-syntax-boundary :reader-semantic-binding])))
      (is (not= (get-in gravity [:gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])
                (get-in qst [:gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])))
      (doseq [[label changed]
              [[:span (update-in gravity [:syntax-object-stream 0 :span :primary :byte-end] inc)]
               [:origin (assoc-in gravity [:syntax-object-stream 0 :origin 0 :producer :identity] "sha256:substituted")]
               [:metadata (assoc-in gravity [:syntax-object-stream 0 :metadata :doc] "substituted")]]]
        (testing (name label)
          (let [proof (b/c3-syntax-capability-proof changed (invocation))]
            (is (= :failed (:status proof)))
            (is (false? (:reader-products-authentic? proof)))
            (is (false? (:syntax-verifier-current? proof))))))
      (let [worker-var (ns-resolve 'gravity.bootstrap 'sh04-syntax-build-from-fresh-c2!)
            worker @worker-var calls (atom 0)
            diagnostic
            (with-redefs-fn {worker-var (fn [& args] (swap! calls inc) (apply worker args))}
              #(try (construct (first paths) "12abc") nil
                    (catch clojure.lang.ExceptionInfo error (ex-data error))))]
        (is (= "C2-NUMERIC" (:id diagnostic)))
        (is (= "STAGE1READER007" (:reader-engine-diagnostic diagnostic)))
        (is (= :lexical-tokenization (get-in diagnostic [:reader-state :stage])))
        (is (= (first paths) (get-in diagnostic [:source-span :source])))
        (is (zero? @calls))
        (is (identical? worker @worker-var))))))
