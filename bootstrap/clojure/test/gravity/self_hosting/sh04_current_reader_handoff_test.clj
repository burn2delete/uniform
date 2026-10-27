(ns gravity.self-hosting.sh04-current-reader-handoff-test
  "Finite fresh Syntax handoff population; no Reader release or public admission."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(def ^:private fixture-base
  "bootstrap/clojure/fixtures/self-hosting/sh-04/current-reader-handoff")
(defn- path [extension] (.getCanonicalPath (io/file (str fixture-base extension))))
(defn- reader-path [] (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)))
(defn- invocation []
  {:reader-source-path (reader-path)
   :authenticated-envelope-options {:source :development-current}})
(defn- construct [source text]
  (b/compiler-c3-syntax-source-artifact
    source text (b/reader-project-context-for-source source)
    nil nil (reader-path) (invocation)))
(defn- source-syntax [artifact]
  (filterv #(not= :generated-form (get-in % [:form :kind]))
           (:syntax-object-stream artifact)))
(defn- proof [artifact] (b/c3-syntax-capability-proof artifact (invocation)))
(defn- coordinate-at [bytes offset]
  (let [prefix (String. (java.util.Arrays/copyOfRange bytes 0 (int offset))
                       java.nio.charset.StandardCharsets/UTF_8)
        lines (str/split prefix #"\r\n|\r|\n" -1)
        last-line (peek lines)]
    {:scalar (.codePointCount prefix 0 (.length prefix))
     :line (count lines)
     :column (inc (.codePointCount ^String last-line 0 (.length ^String last-line)))}))
(defn- history-for [c2 root-id semantic-source-id bytes]
  (let [forms (:form-tree c2) by-id (into {} (map (juxt :form-id identity)) forms)
        descendant? (fn [id]
                      (loop [id id]
                        (cond (= root-id id) true (nil? id) false
                              :else (recur (:parent-form-id (by-id id))))))]
    (vec
      (for [form forms :when (descendant? (:form-id form))
            origin (:generated-origin form)
            :let [span (:from origin)
                  start (coordinate-at bytes (:byte-start span))
                  end (coordinate-at bytes (:byte-end span))]]
        {:form-id (:form-id form) :input-form-id (:child-form-id origin)
         :reader-abbreviation (:reason origin)
         :surface-span {:file semantic-source-id
                        :byte-start (:byte-start span) :byte-end (:byte-end span)
                        :scalar-start (:scalar start) :scalar-end (:scalar end)
                        :line-start (:line start) :column-start (:column start)
                        :line-end (:line end) :column-end (:column end)}
         :expanded-form (:expanded-form origin)}))))
(defn- expansion-facts [node]
  (:value (first (filter #(= :authenticated-reader-products (:fact-kind %)) (:facts node)))))

(def ^:private expected-kinds
  [:nil :boolean :boolean :integer :keyword :symbol :character :list :map :set
   :tagged-literal :tagged-literal :list :list :symbol :list :vector :decimal :string])
(def ^:private rejected-sources
  [["delimiter" "[)" "C2-DELIMITER"]
   ["map" "{:a}" "C2-MAP"]
   ["set" "#{:a :a}" "C2-SET"]
   ["string" "\"\\q\"" "C2-STRING"]
   ["character" "\\uD800" "C2-STRING"]
   ["identifier" ":" "C2-IDENTIFIER"]
   ["metadata" "^1 item" "C2-METADATA"]
   ["abbreviation" "~item" "C2-ABBREV"]
   ["namespace" "(ns)" "C2-NS-SHAPE"]
   ["extension" "#unknown/tag \"x\"" "C2-EXTENSION"]])

(deftest selected-current-reader-family-population-reaches-fresh-syntax
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test
                                  'current-emitter))]
    (let [paths (mapv path [".gravity" ".qst"])
          texts (mapv #(slurp % :encoding "UTF-8") paths)
          _ (is (= (first texts) (second texts)))
          artifacts (mapv construct paths texts)
          [gravity qst] artifacts]
      (doseq [[source text artifact] (map vector paths texts artifacts)]
        (let [c2 (:c2-reader-artifact artifact)
              seeds (:syntax-seed-stream c2)
              nodes (source-syntax artifact)
              forms (into {} (map (juxt :form-id identity) (:form-tree c2)))
              fresh (proof artifact)
              bytes (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
          (is (= expected-kinds (mapv #(get-in % [:form :kind]) nodes)))
          (is (= :complete (:status fresh)))
          (is (true? (:reader-products-authentic? fresh)))
          (is (true? (:syntax-verifier-current? fresh)))
          (is (= :development-unadmitted
                 (get-in artifact [:gravity-syntax-boundary :authenticated-envelope
                                   :source-rule :admission-status])))
          (is (= (mapv :form-id seeds) (mapv #(get-in % [:source :form-id]) nodes)))
          (doseq [[seed node] (map vector seeds nodes)]
            (let [span (get-in node [:span :primary])
                  raw (get-in node [:form :raw])
                  origin (first (filter #(= :source (:kind %)) (:origin node)))
                  expected-history (history-for c2 (:form-id seed) (:file span) bytes)
                  facts (expansion-facts node)]
              (is (= (select-keys (:span seed) [:byte-start :byte-end :scalar-start :scalar-end])
                     (select-keys span [:byte-start :byte-end :scalar-start :scalar-end])))
              (is (= (coordinate-at bytes (:byte-start span))
                     {:scalar (:scalar-start span) :line (:line-start span)
                      :column (:column-start span)}))
              (is (= (coordinate-at bytes (:byte-end span))
                     {:scalar (:scalar-end span) :line (:line-end span)
                      :column (:column-end span)}))
              (is (= source (:source span)))
              (is (= raw (String. (java.util.Arrays/copyOfRange bytes
                                   (int (:byte-start span)) (int (:byte-end span)))
                                 java.nio.charset.StandardCharsets/UTF_8)))
              (is (= (:metadata seed) (:metadata node)))
              (is (= span (:span origin)))
              (is (= 'gravity.bootstrap.reader (get-in origin [:producer :name])))
              (is (= (boolean (seq expected-history))
                     (contains? facts :reader-expansion-origins)))
              (when (seq expected-history)
                (is (= expected-history (:reader-expansion-origins facts))))))
          (doseq [[ordinal tag payload]
                  [[10 'uuid "550e8400-e29b-41d4-a716-446655440000"]
                   [11 'inst "2026-10-27T00:00:00.000-00:00"]]]
            (let [descriptor (get-in nodes [ordinal :form :value])]
              (is (= :gravity/tagged-literal-descriptor (:artifact descriptor)))
              (is (= tag (:tag descriptor)))
              (is (= payload (:payload descriptor)))
              (is (= :accepted (:semantic-validation descriptor)))))
          (doseq [[ordinal abbreviation] [[12 :syntax-quote] [13 :deref]
                                          [14 :metadata] [15 :quote]]]
            (let [seed (nth seeds ordinal)
                  record (forms (:form-id seed))
                  generated (:generated-origin seed)]
              (is (= abbreviation (:abbrev record)))
              (is (= abbreviation (get-in seed [:reader-origin :abbreviation])))
              (is (seq generated))
              (is (= (mapv (fn [origin]
                             {:from (:from origin)
                              :reader-abbreviation (:reason origin)
                              :expanded-form (:value record)})
                           (:generated-origin record))
                     generated))
              (is (= (:form seed) (:value record) (get-in nodes [ordinal :form :value])))
              (is (= (:surface-span record) (:from (first generated))))))
          (is (= {:fresh true} (:metadata (nth nodes 14))))
          (is (= #{:syntax-quote :unquote :splice-unquote :deref :metadata :quote}
                 (set (keep :abbrev (:form-tree c2)))))
          (is (= (get-in artifact [:gravity-syntax-boundary :gravity-syntax-serialization
                                  :semantic-payload])
                 (get-in artifact [:gravity-syntax-boundary :gravity-syntax-deserialization
                                  :semantic-payload])))
          (is (= 1 (count (filter #(= :generated-form (get-in % [:form :kind]))
                                 (:syntax-object-stream artifact)))))))
      (is (= (:artifact-id gravity) (:artifact-id qst)))
      (is (= (mapv :syntax/id (:syntax-object-stream gravity))
             (mapv :syntax/id (:syntax-object-stream qst))))
      (is (not= (get-in gravity [:gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])
                (get-in qst [:gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])))
      (doseq [[label changed]
              [[:missing-seed (update-in gravity [:c2-reader-artifact :syntax-seed-stream] pop)]
               [:duplicate-seed (update-in gravity [:c2-reader-artifact :syntax-seed-stream]
                                   #(conj % (first %)))]
               [:foreign-source (assoc gravity :c2-reader-artifact (:c2-reader-artifact qst))]
               [:tag-payload (assoc-in gravity [:syntax-object-stream 10 :form :value :payload]
                                      "substituted")]]]
        (testing (name label)
          (let [fresh (proof changed)]
            (is (= :failed (:status fresh)))
            (is (false? (:reader-products-authentic? fresh)))
            (is (false? (:syntax-verifier-current? fresh))))))
      (let [worker-var (ns-resolve 'gravity.bootstrap 'sh04-syntax-build-from-fresh-c2!)
            worker @worker-var calls (atom 0)]
        (doseq [[label text id] rejected-sources]
          (testing label
            (reset! calls 0)
            (let [diagnostic
                  (with-redefs-fn {worker-var (fn [& args] (swap! calls inc) (apply worker args))}
                    #(try (construct (first paths) text) nil
                          (catch clojure.lang.ExceptionInfo error (ex-data error))))]
              (is (= id (:id diagnostic)))
              (is (= (first paths) (get-in diagnostic [:source-span :source])))
              (is (zero? @calls))
              (is (identical? worker @worker-var)))))))))
