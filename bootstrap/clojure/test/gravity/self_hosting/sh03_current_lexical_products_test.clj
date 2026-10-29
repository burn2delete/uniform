(ns gravity.self-hosting.sh03-current-lexical-products-test
  "Finite lexical/product census through the selected-current Reader, not Core admission."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh03-decode-reference :as reference]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(def ^:dynamic *progress* (fn [_] nil))
(defn- root []
  (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh03_current_lexical_products_test.clj")))]
    (cond (nil? file) (throw (ex-info "Lexical test root is missing" {}))
          (.isFile (io/file file "deps.edn")) (.getCanonicalFile file)
          :else (recur (.getParentFile file)))))
(defn- absolute [path] (.getCanonicalPath (io/file (root) path)))
(defn- fixture [disposition name extension]
  (absolute (str "bootstrap/clojure/fixtures/self-hosting/sh-03/" disposition "/" name extension)))
(defn- source-bytes [path] (java.nio.file.Files/readAllBytes (.toPath (io/file path))))
(defn- reader [] (absolute b/sh03-reader-source-relative-path))
(defn- codepoints [text] (vec (.toArray (.codePoints ^String text))))
(defn- raw-text [input slice] (reference/slice input slice))
(defn- current-emitter []
  @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter)))

(defn- construct [label path options]
  (*progress* {:case label :event :started})
  (let [input (source-bytes path) before (aclone input)
        text (reference/strict-text input)
        products (b/c2-reader-products path text options
                   (b/reader-project-context-for-source path) (reader))
        result (:sh03-reader-result products)
        report (:sh03-reader-verification-report products)]
    (is (java.util.Arrays/equals before input))
    (is (= :accepted (:status result) (:status report)))
    (is (true? (:verified? report)))
    (is (= :accepted (:reader-result-status report)))
    (is (= :passed
           (b/p15-s23-stage2-sh02-descriptor-envelope-verify!
             (:sh02-reader-envelope products) :c2-reader :gravity/sh03-reader-products
             (:sh02-reader-envelope-descriptor products) path {:source :development-current})))
    (is (= :development-unadmitted
           (get-in products [:sh02-reader-envelope :source-rule :admission-status])))
    (is (= path (get-in products [:source-unit :path])))
    (*progress* {:case label :event :returned})
    {:input input :text text :products products :result result}))

(def expected-form-kinds
  #{:nil :boolean :integer :decimal :ratio :string :character :symbol :keyword
    :list :vector :map :set :abbreviation :metadata-wrapper :tagged-literal})
(def expected-abbreviations #{:quote :syntax-quote :unquote :splice-unquote :deref :metadata})
(def expected-literals
  {"nil" {:kind :nil :value nil}
   "true" {:kind :boolean :value true}
   "false" {:kind :boolean :value false}
   "-7" {:kind :integer :radix 10 :value -7}
   "+9" {:kind :integer :radix 10 :value 9}
   "0xFF" {:kind :integer :radix 16 :value 255}
   "0b1010" {:kind :integer :radix 2 :value 10}
   "-2.5E-4" {:kind :decimal :integer-spelling [45 50] :fraction-spelling [53]
              :exponent-spelling [45 52] :semantic-validation :accepted}
   "3/4" {:kind :ratio :numerator-spelling [51] :denominator-spelling [52]
          :numerator 3 :denominator 4 :semantic-validation :accepted}
   "1/0" {:kind :ratio :numerator-spelling [49] :denominator-spelling [48]
          :semantic-validation :deferred :reason :zero-denominator}
   "1e1000000000000000000" {:kind :decimal :integer-spelling [49]
                           :fraction-spelling [] :semantic-validation :deferred
                           :reason :host-independent-decimal-range}
   "\\a" {:kind :character :escape-kind :scalar :codepoint 97}
   "\\newline" {:kind :character :escape-kind :named :codepoint 10}
   "\\u03bb" {:kind :character :escape-kind :unicode :codepoint 955}
   "\\o141" {:kind :character :escape-kind :octal :codepoint 97}
   "reader/value" {:kind :symbol :name-codepoints (codepoints "reader/value")}
   ":reader/value" {:kind :keyword :name-codepoints (codepoints "reader/value")}
   "\"unicode escape: \\u03bb\"" {:kind :string :decoded-codepoints (codepoints "unicode escape: λ")}})

(defn- check-products [{:keys [input text products result]} retain?]
  (let [tokens (:token-stream products) forms (:form-tree products)
        by-token (into {} (map (juxt :token-id identity)) tokens)
        by-form (into {} (map (juxt :form-id identity)) forms)
        semantic (group-by #(raw-text input (get-in % [:descriptor :raw]))
                           (:semantic-value-table result))]
    (is (= expected-form-kinds (set (map :kind forms))))
    (is (= expected-abbreviations (set (keep :abbrev forms))))
    (is (= (count tokens) (count by-token)))
    (is (= (count forms) (count by-form)))
    (is (= (:root-form-ids products)
           (mapv :form-id (filter #(nil? (:parent-form-id %)) forms))))
    (let [starts (mapv #(get-in (by-form %) [:span :byte-start]) (:root-form-ids products))]
      (is (apply < starts)))
    (doseq [record (concat tokens forms)]
      (is (= (:raw record) (reference/slice input (:span record)))))
    (doseq [form forms]
      (is (contains? by-token (:open-token form)))
      (is (contains? by-token (:close-token form)))
      (when-let [parent (:parent-form-id form)]
        (is (contains? by-form parent))
        (is (= 1 (count (filter #{(:form-id form)} (:children (by-form parent)))))))
      (doseq [child (:children form)]
        (is (= (:form-id form) (:parent-form-id (by-form child)))))
      (doseq [id (concat (:leading-trivia-token-ids form) (:trailing-trivia-token-ids form))]
        (is (true? (:trivia? (by-token id)))))
      (doseq [origin (:generated-origin form)]
        (is (= :reader-abbreviation (:producer origin)))
        (is (= (:abbrev form) (:reason origin)))
        (is (some #{(:child-form-id origin)} (:children form)))
        (is (= (:surface-span form) (:from origin)))
        (is (= ({:quote "'" :syntax-quote "`" :unquote "~" :splice-unquote "~@"
                 :deref "@" :metadata "^"} (:reason origin))
               (reference/slice input (:from origin))))
        (is (= (:value form) (:expanded-form form)))
        (when-not (= :metadata (:abbrev form))
          (is (= ({:quote 'quote :syntax-quote 'syntax-quote :unquote 'unquote
                   :splice-unquote 'splice-unquote :deref 'deref} (:abbrev form))
                 (first (:expanded-form form)))))))
    (doseq [[spelling expected] expected-literals]
      (testing spelling
        (is (seq (semantic spelling)))
        (doseq [entry (semantic spelling)]
          (is (= expected (select-keys (:descriptor entry) (keys expected)))))))
    (let [large "123456789012345678901234567890"
          value (get-in (first (semantic large)) [:descriptor :value])]
      (is (= :gravity/arbitrary-precision-integer (:artifact value)))
      (is (= 10 (:radix value)))
      (is (= (codepoints large) (:digit-codepoints value))))
    (doseq [form (filter #(= :metadata-wrapper (:kind %)) forms)]
      (let [target (by-form (second (:children form)))]
        (is (= (:metadata form) (meta (:value form))))
        (is (= (:value form) (:value target)))
        (is (seq (:metadata form)))))
    (is (some #(= {:doc "Map metadata" 'compiler/owner :reader} (:metadata %)) forms))
    (is (some #(true? (get-in % [:metadata :private])) forms))
    (is (some #(= 'ReaderTag (get-in % [:metadata :tag])) forms))
    (is (= #{"1/0" "1e1000000000000000000"}
           (set (map :raw (:deferred-literal-records products)))))
    (doseq [literal (:literal-decoding-records products)]
      (is (= (:raw literal) (:raw (by-form (:form-id literal))))))
    (let [spelling (str "\"escapes:" "\\n" "\\r" "\\t" "\\b" "\\f" "\\\\" "\\\"" "\"")
          record (first (filter #(= spelling (:raw %)) (:literal-decoding-records products)))]
      (is (= spelling (:raw record)))
      (is (= (str "escapes:\n\r\t\b\f" "\\" "\"") (:decoded record)))
      (is (= [{:raw "\\n" :character-offset 9} {:raw "\\r" :character-offset 11}
              {:raw "\\t" :character-offset 13} {:raw "\\b" :character-offset 15}
              {:raw "\\f" :character-offset 17} {:raw "\\\\" :character-offset 19}
              {:raw "\\\"" :character-offset 21}]
             (get-in record [:facts :escapes]))))
    (doseq [[spelling facts]
            [["0xFF" {:radix 16 :sign :unsigned :exact? true}]
             ["-7" {:radix 10 :sign :negative :exact? true}]
             ["+9" {:radix 10 :sign :explicit-positive :exact? true}]
             ["-2.5E-4" {:exponent-spelling "E-4" :exact? false}]
             ["3/4" {:numerator-spelling "3" :denominator-spelling "4" :exact? true}]]]
      (let [records (filter #(= spelling (:raw %)) (:literal-decoding-records products))]
        (is (seq records))
        (doseq [record records] (is (= facts (:facts record))))))
    (let [tagged (filter #(= :tagged-literal (:kind %)) forms)
          payloads {'inst "2026-07-18T12:34:56.000-00:00"
                    'uuid "123e4567-e89b-12d3-a456-426614174000"}]
      (is (= (set (keys payloads)) (set (map :tag tagged))))
      (doseq [form tagged]
        (is (= (payloads (:tag form)) (:value (by-form (first (:children form))))))
        (case (:tag form)
          inst (is (= (java.util.Date/from (java.time.Instant/parse "2026-07-18T12:34:56Z"))
                      (:value form)))
          uuid (is (= (java.util.UUID/fromString "123e4567-e89b-12d3-a456-426614174000")
                      (:value form))))))
    (if retain?
      (do (is (= text (apply str (map :raw tokens))))
          (is (some #(= :comment (:kind %)) tokens))
          (is (some #(and (:trivia? %) (.contains ^String (:raw %) ",")) tokens)))
      (is (not-any? :trivia? tokens)))))

(deftest selected-current-complete-lexical-products-and-origins
  (binding [b/*p15-s23-stage2-compiler-artifact-binding* (current-emitter)]
    (let [gravity (construct :complete-gravity (fixture "accepted" "complete-reader-surface" ".gravity") b/standard-reader-options)
          qst (construct :complete-qst (fixture "accepted" "complete-reader-surface" ".qst") b/standard-reader-options)
          filtered (construct :complete-filtered (fixture "accepted" "complete-reader-surface" ".gravity")
                     (assoc b/standard-reader-options :retain-comments false))]
      (is (java.util.Arrays/equals (:input gravity) (:input qst)))
      (doseq [observation [gravity qst]] (check-products observation true))
      (check-products filtered false)
      (is (= (get-in gravity [:products :parsed-values]) (get-in qst [:products :parsed-values])
             (get-in filtered [:products :parsed-values])))
      (is (= (mapv #(select-keys % [:kind :raw]) (remove :trivia? (get-in gravity [:products :token-stream])))
             (mapv #(select-keys % [:kind :raw]) (get-in filtered [:products :token-stream]))))
      (let [supplementary (construct :supplementary-character
                            (fixture "accepted" "unicode-supplementary" ".gravity") b/standard-reader-options)
            descriptors (map :descriptor (get-in supplementary [:result :semantic-value-table]))]
        (is (some #(= {:kind :character :escape-kind :scalar :codepoint 128512}
                      (select-keys % [:kind :escape-kind :codepoint])) descriptors))))))

(deftest selected-current-malformed-numeric-grammars-reject-before-symbol-adaptation
  (binding [b/*p15-s23-stage2-compiler-artifact-binding* (current-emitter)]
    (doseq [[label name extension spelling]
            [[:invalid-radix "numeric-invalid-radix" ".gravity" "0b102"]
             [:incomplete-exponent "numeric-incomplete-exponent" ".qst" "1e+"]
             [:repeated-exponent "malformed-numeric" ".gravity" "1e2e3"]]]
      (*progress* {:case label :event :started})
      (let [source (fixture "rejected" name extension) input (source-bytes source) before (aclone input)
            project (b/reader-project-context-for-source source)
            resolved (b/sh03-reader-resolved-result! source input project b/standard-reader-options (reader))
            result (:result resolved) report (:verification-report resolved)
            raised (try (b/sh03-reader-raise-rejection! source input b/standard-reader-options project result) nil
                        (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (java.util.Arrays/equals before input))
        (is (= :rejected (:status result)))
        (is (= :accepted (:status report)))
        (is (true? (:verified? report)))
        (is (= :rejected (:reader-result-status report)))
        (is (= "C2-NUMERIC" (:id raised)))
        (is (= "STAGE1READER007" (:reader-engine-diagnostic raised)))
        (is (= spelling (:raw-spelling raised)))
        (is (= source (get-in raised [:source-span :source])))
        (is (= [0 (count spelling)] ((juxt :byte-start :byte-end) (:source-span raised))))
        (is (empty? (:top-level-form-ids result)))
        (is (empty? (:form-tree result)))
        (*progress* {:case label :event :returned})))))

(defn- reconstruction-reference-candidates [raw]
  (let [forms (:form-tree raw)
        at (fn [predicate]
             (let [positions (keep-indexed #(when (predicate %2) %1) forms)]
               (assert (seq positions) "Reference fixture lacks its required form")
               (first positions)))
        metadata (at #(= :metadata-wrapper (:kind %)))
        tagged (at #(= :tagged-literal (:kind %)))
        abbreviation (at #(seq (:generated-origin %)))
        parented (at #(some? (:parent-form-id %)))
        root (first (:top-level-form-ids raw))
        other-root (second (:top-level-form-ids raw))
        missing "form/original-reconstruction-missing"]
    (assert (and root other-root (not= root other-root)))
    (assert (not-any? #(= missing (:form-id %)) forms))
    (assert (seq (:semantic-value-table raw)))
    (assert (seq (:literal-decoding-records raw)))
    [[:missing-token (update raw :token-stream #(into [] (rest %)))]
     [:duplicated-token (update raw :token-stream #(conj % (first %)))]
     [:missing-form (update raw :form-tree #(into [] (rest %)))]
     [:duplicated-form (update raw :form-tree #(conj % (first %)))]
     [:missing-value (update raw :semantic-value-table pop)]
     [:duplicated-value (update raw :semantic-value-table #(conj % (first %)))]
     [:missing-root-reference (assoc-in raw [:top-level-form-ids 0] missing)]
     [:duplicated-root-reference (assoc-in raw [:top-level-form-ids 0] other-root)]
     [:substituted-value-reference (assoc-in raw [:semantic-value-table 0 :form-id] missing)]
     [:substituted-parent-reference (assoc-in raw [:form-tree parented :parent-form-id] missing)]
     [:substituted-metadata-reference (assoc-in raw [:form-tree metadata :children 0] other-root)]
     [:substituted-metadata-target (assoc-in raw [:form-tree metadata :children 1] other-root)]
     [:substituted-tag-payload (assoc-in raw [:form-tree tagged :children 0] other-root)]
     [:substituted-generated-origin
      (assoc-in raw [:form-tree abbreviation :generated-origin 0 :child-form-id] missing)]
     [:substituted-source-map-token (assoc-in raw [:reader-source-map :token-spans 0 :token-id] missing)]
     [:substituted-source-map-form (assoc-in raw [:reader-source-map :form-spans 0 :form-id] missing)]
     [:substituted-literal-reference (assoc-in raw [:literal-decoding-records 0 :form-id] missing)]
     [:stale-form-hash (assoc-in raw [:incremental-reader-hashes :form-tree] {:digest-ref 2})]
     [:stale-source-digest (assoc-in raw [:digest-requests 0 :observed-id]
                                   (str "sha256:" (apply str (repeat 64 "0"))))]]))

(deftest selected-current-original-reconstruction-rejects-reference-substitutions
  ;; The normal producer owns original execution, fresh verification and AE.
  ;; The additional verifier calls receive the original unit, every original
  ;; byte and policy, followed by the untrusted raw candidate. No authority
  ;; token, resolved-product substitute or caller-supplied report is used.
  (binding [b/*p15-s23-stage2-compiler-artifact-binding* (current-emitter)]
    (let [source (fixture "accepted" "original-reconstruction-references" ".gravity")
          {:keys [input products]} (construct :original-reconstruction source b/standard-reader-options)
          raw (:sh03-reader-raw-result products)
          original raw
          project (b/reader-project-context-for-source source)
          unit (b/sh03-reader-input-source-unit source input project)
          policy (b/sh03-reader-input-policy b/standard-reader-options)
          bytes (mapv #(bit-and 255 %) input)
          binding (b/sh03-reader-current-binding! source (reader))
          identity-fields [:source-path :source-content-hash :source-byte-count
                           :plan-semantic-hash :functions-semantic-hash]
          verify (fn [candidate]
                   (b/sh03-reader-result-preflight! source unit policy candidate)
                   (b/sh03-reader-execute-plan! source (:plan binding) 'sh03-verify-reader-result
                                               [unit bytes policy candidate]))
          fresh (verify raw)]
      (is (every? #(some? (get binding %)) identity-fields))
      (is (= (select-keys (:sh03-reader-plan-binding products) identity-fields)
             (select-keys binding identity-fields)))
      (is (= :accepted (:status fresh)))
      (is (true? (:verified? fresh)))
      (is (= fresh (b/sh03-reader-verifier-preflight! source raw fresh)))
      (doseq [[label candidate] (reconstruction-reference-candidates raw)]
        (testing (name label)
          (is (not= raw candidate) "Mutation must change the actual original raw result")
          (*progress* {:case label :event :started})
          (let [report (verify candidate)
                rejected (try (b/sh03-reader-verifier-preflight! source candidate report) nil
                              (catch clojure.lang.ExceptionInfo error (ex-data error)))]
            (is (= :rejected (:status report)))
            (is (false? (:verified? report)))
            (is (= ["C2-HASH"] (mapv :id (:diagnostics report))))
            (is (= [:reader-result-replay-mismatch]
                   (mapv #(get-in % [:reader-state :reason]) (:diagnostics report))))
            (is (= "C2-HASH" (:id rejected)))
            (is (= [:fresh-gravity-sh03-reader-result-replay] (:missing-fields rejected)))
            (is (= source (get-in rejected [:source-span :source]))))
          (*progress* {:case label :event :returned})))
      ;; Negative controls cannot alter the genuine producer's products, and
      ;; the original still verifies after every candidate was rejected.
      (is (= original (:sh03-reader-raw-result products)))
      (is (= fresh (verify raw)))
      (is (= (select-keys binding identity-fields)
             (select-keys (b/sh03-reader-current-binding! source (reader)) identity-fields))))))
