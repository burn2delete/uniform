(ns gravity.self-hosting.sh03-compact-character-storage-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Independent Java Unicode decoding supplies reference codepoints and raw
;; bytes; no authenticated binding or candidate acceptance is exercised here.
(def ^:private root
  (delay
    (loop [directory (.getParentFile
                      (io/file (.toURI
                                (io/resource
                                 "gravity/self_hosting/sh03_compact_character_storage_test.clj"))))]
      (if (.isFile (io/file directory "deps.edn")) directory
        (if-let [parent (.getParentFile directory)] (recur parent)
          (throw (ex-info "Repository root missing" {})))))))

(def ^:private current-plan
  (delay
    (let [file (io/file @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
          path (.getPath file)]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
       path (slurp file)))))

(defn- execute-plan [plan function arguments]
  (with-redefs [bootstrap/sh03-reader-current-binding!
                (fn [& _] (throw (ex-info "Unexpected authenticated route" {})))]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-compact-character-current-source-test
      :compiler-artifact-plan? true}
     plan function arguments)))

(defn- execute [function arguments]
  (execute-plan @current-plan function arguments))

(defn- unsigned-bytes [^String text]
  (mapv #(bit-and 255 %) (.getBytes text java.nio.charset.StandardCharsets/UTF_8)))

(defn- source-unit [bytes]
  {:artifact :gravity/sh03-source-unit :schema-version 1
   :project-root-id (str "sha256:" (apply str (repeat 64 "0")))
   :logical-source-id "src/compact-character.gravity"
   :source-content-hash (str "sha256:"
                            (bootstrap/sha256-bytes-hex
                             (byte-array (map unchecked-byte bytes))))
   :source-byte-count (count bytes) :encoding :utf-8
   :actual-path-provenance {:path "/declared/compact-character.gravity"
                            :extension ".gravity" :source-kind :gravity}})

(defn- reference-decoded [^String text]
  (let [points (vec (.toArray (.codePoints text)))
        position (fn [line column char byte]
                   {:line line :column column :column-unit :unicode-scalar
                    :char char :byte byte})]
    (loop [index 0 utf16 0 byte 0 line 1 column 1 characters []]
      (if (= index (count points))
        {:status :accepted :characters characters :character-count index
         :utf16-unit-count utf16 :source-byte-count byte}
        (let [cp (nth points index)
              spelling (String. (Character/toChars cp))
              raw (unsigned-bytes spelling)
              utf16-end (+ utf16 (Character/charCount cp))
              byte-end (+ byte (count raw))
              crlf (and (= cp 13) (= 10 (get points (inc index))))
              [next-line next-column]
              (cond (= cp 10) [(inc line) 1]
                    (= cp 13) (if crlf [line (inc column)] [(inc line) 1])
                    :else [line (inc column)])
              character {:index index :codepoint cp :raw-bytes raw
                         :span {:start (position line column utf16 byte)
                                :end (position next-line next-column utf16-end byte-end)
                                :byte-start byte :byte-end byte-end}}]
          (recur (inc index) utf16-end byte-end next-line next-column
                 (conj characters character)))))))

(defn- decoded [text]
  (let [bytes (unsigned-bytes text)]
    (execute 'sh03-decode-source-bytes [(source-unit bytes) bytes])))

(defn- physical-nodes [value]
  (let [checked (bootstrap/p15-s23-trusted-carrier-validation
                 value :default-only 167772160 2048 4194304)]
    (is (= :passed (:status checked)))
    (:observed-nodes checked)))

(defn- with-dispatch [overrides thunk]
  (let [dispatch (ns-resolve 'gravity.bootstrap
                            'p15-s23-stage2-runtime-execute-function-scoped)
        original @dispatch]
    (with-redefs-fn
      {dispatch (fn [runtime plan callee arguments scope]
                  (if-let [override (get overrides callee)]
                    (override arguments #(original runtime plan callee arguments scope))
                    (original runtime plan callee arguments scope)))}
      thunk)))

(deftest compact-character-views-match-independent-unicode-reference
  (doseq [text ["" "a\r\nb\rc\nd" "🙂x\r\nλ\r界\n"
                (apply str (map char (range 128)))
                (apply str (map #(String. (Character/toChars %))
                               [128 2047 2048 55295 57344 65535 65536 1114111]))]]
    (let [expected (reference-decoded text)
          actual (decoded text)
          rows (:characters actual)
          views (mapv #(execute 'sh03-character-at [rows %]) (range (count rows)))]
      (is (= (dissoc expected :characters) (dissoc actual :characters)))
      (is (= (:characters expected) views))
      (is (= (mapv :codepoint (:characters expected))
             (execute 'sh03-character-range-codepoints [rows 0 (count rows)])))
      (is (= (unsigned-bytes text)
             (execute 'sh03-character-range-bytes [rows 0 (count rows)])))
      ;; Existing helper callers may still supply the historical map views.
      (is (= views (mapv #(execute 'sh03-character-at [views %]) (range (count views)))))
      (is (= (unsigned-bytes text)
             (execute 'sh03-character-range-bytes [views 0 (count views)]))))))

(deftest compact-character-storage-retains-no-per-scalar-maps
  (doseq [text ["abc\r\n🙂λ" (apply str (repeat 100 "a🙂\r\n"))]]
    (let [rows (:characters (decoded text))
          reference (:characters (reference-decoded text))
          byte-count (count (unsigned-bytes text))]
      (is (every? #(and (vector? %) (= 7 (count %))) rows))
      (is (not-any? map? (tree-seq coll? seq rows)))
      (is (= (+ 1 (* 8 (count rows)) byte-count) (physical-nodes rows)))
      (is (= (+ 1 (* 37 (count rows)) byte-count) (physical-nodes reference)))
      (is (< (physical-nodes rows) (physical-nodes reference))))))

(deftest compact-character-malformed-utf8-facts-remain-exact
  (doseq [[invalid reason]
          [[[128] :unexpected-continuation-byte]
           [[192 128] :overlong-two-byte-sequence]
           [[194] :truncated-two-byte-sequence]
           [[194 65] :invalid-continuation-byte]
           [[224 159 128] :overlong-three-byte-sequence]
           [[237 160 128] :surrogate-codepoint]
           [[226 130] :truncated-three-byte-sequence]
           [[226 65 128] :invalid-continuation-byte]
           [[240 143 128 128] :overlong-four-byte-sequence]
           [[244 144 128 128] :codepoint-above-unicode-maximum]
           [[240 144 128] :truncated-four-byte-sequence]
           [[240 144 65 128] :invalid-continuation-byte]
           [[245 128 128 128] :invalid-leading-byte]]]
    (let [bytes (into [65 13 10] invalid)
          result (execute 'sh03-decode-source-bytes [(source-unit bytes) bytes])]
      (is (= :rejected (:status result)))
      (is (= reason (:reason result)))
      (is (= {:byte-position 3 :line 2 :column 1 :char-position 3 :scalar-index 3}
             (select-keys result [:byte-position :line :column :char-position :scalar-index]))))))

(deftest compact-character-limit-is-checked-before-decode-or-growth
  (let [bounds (execute 'sh03-bounds-record [])
        bytes (unsigned-bytes "a🙂")]
    (doseq [maximum [0 1]]
      (let [calls (atom [])
            plan (assoc-in @current-plan [:functions 'sh03-bounds-record :instructions]
                           [{:op :quote :value (assoc bounds :maximum-characters maximum)}])
            result (with-dispatch
                     {'sh03-decode-one-utf8-scalar
                      (fn [[_ position] continue]
                        (swap! calls conj position) (continue))}
                     #(execute-plan plan 'sh03-decode-source-bytes [(source-unit bytes) bytes]))]
        (is (= :character-count-limit (:reason result)))
        (is (= maximum (:scalar-index result)))
        (is (= (vec (range maximum)) @calls))
        (is (not (contains? result :characters)))))
    (let [calls (atom 0)
          invalid [65 256]
          result (with-dispatch
                   {'sh03-decode-one-utf8-scalar
                    (fn [_ continue] (swap! calls inc) (continue))}
                   #(execute 'sh03-decode-source-bytes [(source-unit invalid) invalid]))]
      (is (= :source-byte-vector-contract (:reason result)))
      (is (zero? @calls)))))

(deftest compact-character-codepoint-and-raw-paths-do-not-build-span-views
  (let [text "a🙂\r\nλ" rows (:characters (decoded text))
        forbidden (fn [_ _] (throw (ex-info "Unnecessary character/span view" {})))]
    (with-dispatch
      {'sh03-character-at forbidden 'sh03-character-span forbidden
       'sh03-scalar-span forbidden}
      #(do
         (is (= 128578 (execute 'sh03-codepoint-at [rows 1])))
         (is (= (unsigned-bytes text)
                (execute 'sh03-character-range-bytes [rows 0 (count rows)])))
         (is (= [97 128578 13 10 955]
                (execute 'sh03-character-range-codepoints [rows 0 (count rows)])))))))

(deftest compact-character-complete-results-match-reference-decoder
  (doseq [retain-trivia [false true]
          fixture ["complete-reader-surface" "unicode-supplementary" "newline-crlf"]]
    (testing (str fixture " retain-trivia=" retain-trivia)
      (let [text (slurp (io/file @root
                                (str "bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/"
                                     fixture ".gravity")))
            bytes (unsigned-bytes text)
            policy {:artifact :gravity/sh03-reader-policy :schema-version 1
                    :retain-trivia retain-trivia
                    :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}
            arguments [(source-unit bytes) bytes policy]
            actual (execute 'sh03-read-source-unit arguments)
            expected (with-dispatch
                       {'sh03-decode-source-bytes (fn [_ _] (reference-decoded text))}
                       #(execute 'sh03-read-source-unit arguments))]
        (is (= :accepted (:status actual)))
        ;; Includes all public records, source slices, digest preimages, spans,
        ;; semantic identities, and resource declarations, not just form values.
        (is (= expected actual))))))
