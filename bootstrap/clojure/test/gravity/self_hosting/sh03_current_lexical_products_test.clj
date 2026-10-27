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
    (is (= (set (:root-form-ids products))
           (set (map :form-id (filter #(nil? (:parent-form-id %)) forms)))))
    (doseq [record (concat tokens forms)]
      (is (= (:raw record) (reference/slice input (:span record)))))
    (doseq [form forms]
      (is (contains? by-token (:open-token form)))
      (is (contains? by-token (:close-token form)))
      (doseq [child (:children form)]
        (is (= (:form-id form) (:parent-form-id (by-form child)))))
      (doseq [id (concat (:leading-trivia-token-ids form) (:trailing-trivia-token-ids form))]
        (is (true? (:trivia? (by-token id)))))
      (doseq [origin (:generated-origin form)]
        (is (= :reader-abbreviation (:producer origin)))
        (is (= (:abbrev form) (:reason origin)))
        (is (some #{(:child-form-id origin)} (:children form)))
        (is (= (:surface-span form) (:from origin)))
        (is (= (:value form) (:expanded-form form)))))
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
        (is (= (:metadata form) (:metadata target)))
        (is (seq (:metadata form)))))
    (is (some #(= {:doc "Map metadata" 'compiler/owner :reader} (:metadata %)) forms))
    (is (some #(true? (get-in % [:metadata :private])) forms))
    (is (some #(= 'ReaderTag (get-in % [:metadata :tag])) forms))
    (is (= #{"1/0" "1e1000000000000000000"}
           (set (map :raw (get-in products [:semantic-error-deferment-record :deferred-literal-records])))))
    (doseq [literal (:literal-decoding-records products)]
      (is (= (:raw literal) (:raw (by-form (:form-id literal))))))
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
        (is (= spelling (:raw-spelling raised)))
        (is (= source (get-in raised [:source-span :source])))
        (is (empty? (:top-level-form-ids result)))
        (*progress* {:case label :event :returned})))))
