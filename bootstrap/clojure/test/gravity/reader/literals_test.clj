(ns gravity.reader.literals-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.bootstrap :as b]
            [gravity.reader :as reader]))

(defn- operations []
  {:starts-with-var #'str/starts-with? :split-var #'str/split
   :malformed-numeric-var #'b/stage1-reader-malformed-numeric!})

(defn- span [path raw]
  (b/stage1-reader-span path raw (b/line-start-indices raw) 0 (count raw)))

(defn- atom-value [path kind raw]
  (reader/decode-compatibility-atom (operations) path kind raw (span path raw) :tok-0))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo ex ex)))

(deftest literal-contracts-are-explicit-and-do-not-authenticate
  (let [contract (:gravity/namespace-contract (meta #'reader/classify-compatibility-token))]
    (doseq [name '[classify-compatibility-token reject-compatibility-numeric! decode-compatibility-atom
                  decode-compatibility-string decode-compatibility-character]]
      (let [api (get-in contract [:public-api name])]
        (is (= :none (:admission api)))
        (is (false? (:authenticates-inputs? api)))
        (is (false? (:opens-source? api)))
        (is (false? (:callback-type-checks? api)))
        (is (= 'gravity.reader.literals-test (:test-owner api)))))
    (is (= (set (keys (operations)))
           (set (get-in contract [:public-api 'decode-compatibility-atom :operation-inputs]))))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))))

(deftest existing-classification-and-native-values-are-preserved
  (doseq [[raw kind value] [["nil" :nil nil] ["true" :boolean true] ["false" :boolean false]
                           [":a/b" :keyword :a/b] ["sym" :symbol 'sym] ["42" :integer 42N]
                           ["-0x2a" :integer -42N] ["+0b11" :integer 3N]
                           ["1/2" :ratio 1/2] ["-4/2" :ratio -2N] ["2.5" :decimal 2.5]]]
    (is (= kind (reader/classify-compatibility-token #'str/starts-with? raw)))
    (is (= kind (b/stage1-reader-token-kind raw)))
    (is (= value (atom-value "literal.gravity" kind raw)))
    (is (= value (b/stage1-reader-decode-atom "literal.gravity" kind raw (span "literal.gravity" raw) :tok-0))))
  (is (= :malformed-number (reader/classify-compatibility-token #'str/starts-with? "1z")))
  (is (Double/isInfinite (atom-value "overflow.qst" :decimal "1e999")))
  (is (= (bigint (apply str (repeat 512 "9")))
         (atom-value "large.qst" :integer (apply str (repeat 512 "9"))))))

(deftest deferred-ratio-keeps-components-and-spelling
  (let [value (atom-value "ratio.qst" :ratio "+1/00")]
    (is (= {:artifact :gravity/deferred-ratio-literal :kind :ratio :raw "+1/00"
            :numerator-spelling "+1" :denominator-spelling "00" :numerator 1N :denominator 0N
            :semantic-validation :deferred :reason :zero-denominator} value))))

(deftest actual-malformed-literals-retain-diagnostic-data-and-spans
  (doseq [extension [".gravity" ".qst"]]
    (let [path (str "bad" extension)]
      (doseq [raw ["1z" "0x" "1e"]]
        (let [source-span (span path raw)
              direct (rejection #(reader/decode-compatibility-atom (operations) path :malformed-number raw source-span :tok-0))
              facade (rejection #(b/stage1-reader-decode-atom path :malformed-number raw source-span :tok-0))]
          (is (= "STAGE1READER007" (:id (ex-data direct))))
          (is (= (ex-data direct) (ex-data facade)))
          (is (= source-span (:source-span (ex-data direct))))
          (is (= {:literal-kind :numeric :raw-spelling raw} (:facts (ex-data direct))))))
      (doseq [[raw decode] [["\"bad\\q\"" reader/decode-compatibility-string]
                            ["\"unterminated" reader/decode-compatibility-string]
                            ["\\not-a-character" reader/decode-compatibility-character]
                            ["\"x\"" reader/decode-compatibility-character]]]
        (let [failure (ex-data (rejection #(decode #'clojure.core/read-string #'b/stage1-reader-fail!
                                                  path raw (span path raw) :tok-0)))]
          (is (= "STAGE1READER003" (:id failure)))
          (is (= path (get-in failure [:source-span :source])))
          (is (= :tok-0 (:token-id failure)))
          (is (= raw (:raw failure))))))))

(deftest strings-and-characters-use-the-same-read-eval-binding-and-native-class
  (let [path "binding.qst" observed (atom [])]
    (with-redefs [clojure.core/read-string (fn [raw] (swap! observed conj [raw *read-eval*]) (if (= raw "character") \a "decoded"))]
      (binding [*read-eval* true]
        (is (= "decoded" (reader/decode-compatibility-string #'clojure.core/read-string #'b/stage1-reader-fail!
                                                            path "string" {} :tok-0)))
        (is (true? *read-eval*))
        (is (= \a (reader/decode-compatibility-character #'clojure.core/read-string #'b/stage1-reader-fail!
                                                       path "character" {} :tok-0)))
        (is (true? *read-eval*))))
    (is (= [["string" false] ["character" false]] @observed)))
  (is (= "line\n" (reader/decode-compatibility-string #'clojure.core/read-string #'b/stage1-reader-fail!
                                                      "string.gravity" "\"line\\n\"" {} :tok-0)))
  (is (= \newline (reader/decode-compatibility-character #'clojure.core/read-string #'b/stage1-reader-fail!
                                                        "character.gravity" "\\newline" {} :tok-0)))
  (is (= "STAGE1READER003"
         (:id (ex-data (rejection #(reader/decode-compatibility-string #'clojure.core/read-string #'b/stage1-reader-fail!
                                                                      "eval.qst" "#=(+ 1 2)" {} :tok-0)))))))

(deftest starts-with-port-is-late-and-constant-branches-do-not-call-it
  (let [ports (operations) starts str/starts-with? calls (atom [])]
    (with-redefs [str/starts-with?
                  (fn [s prefix]
                    (swap! calls conj [:first prefix])
                    (alter-var-root #'str/starts-with? (constantly (fn [v p] (swap! calls conj [:later p]) (starts v p))))
                    (starts s prefix))]
      (is (= -42N (reader/decode-compatibility-atom ports "late.gravity" :integer "-0x2a" {} :tok-0)))
      (is (= [[:first "-"] [:later "+"]] @calls)))
    (with-redefs [str/starts-with? (fn [& _] (throw (AssertionError. "No prefix test for constant token")))]
      (doseq [[raw kind] [["nil" :nil] ["true" :boolean] ["false" :boolean]]]
        (is (= kind (reader/classify-compatibility-token #'str/starts-with? raw)))))))

(deftest numeric-catches-repeat-the-current-malformed-owner-with-original-inputs
  (doseq [exception [(NumberFormatException. "first") (ArithmeticException. "first")]]
    (let [ports (operations) calls (atom []) source-span {:source "numeric.qst"}]
      (with-redefs [b/stage1-reader-malformed-numeric!
                    (fn [& args]
                      (swap! calls conj [:first args])
                      (alter-var-root #'b/stage1-reader-malformed-numeric!
                                      (constantly (fn [& later] (swap! calls conj [:later later]) :late-refusal)))
                      (throw exception))]
        (is (= :late-refusal (reader/decode-compatibility-atom ports "numeric.qst" :malformed-number "1z" source-span :tok-0)))
        (is (= 2 (count @calls)))
        (is (= [:first :later] (mapv first @calls)))
        (is (= "first" (last (second (second @calls)))))
        (is (every? #(identical? source-span (nth (second %) 2)) @calls))))))

(deftest refusal-owner-references-and-exception-identity-survive
  (let [source-span {:source "owner.gravity"} sentinel (ex-info "owner exception" {:owner :literal})]
    (with-redefs [b/stage1-reader-fail! (fn [_ _ _ data] data)]
      (let [result (reader/reject-compatibility-numeric! #'b/stage1-reader-fail!
                                                       "owner.gravity" "1z" source-span :tok-0 "cause")]
        (is (identical? source-span (:source-span result)))
        (is (= :tok-0 (:token-id result)))
        (is (= "1z" (:raw result)))
        (is (= "cause" (:cause-message result)))))
    (with-redefs [b/stage1-reader-fail! (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(reader/reject-compatibility-numeric! #'b/stage1-reader-fail!
                                                                              "owner.gravity" "1z" source-span :tok-0 "cause"))))
      (is (identical? sentinel (rejection #(reader/decode-compatibility-character #'clojure.core/read-string #'b/stage1-reader-fail!
                                                                                "owner.gravity" "\"x\"" source-span :tok-0)))))))

(deftest native-decoding-is-not-a-literal-grammar-or-source-authentication-proof
  (let [unexpected (fn [& _] (throw (AssertionError. "No source/table discovery")))]
    (with-redefs [b/read-source-form-records unexpected b/stage1-reader-table unexpected]
      (is (= "opaque" (atom-value "untrusted.qst" :unrecognized "opaque")))
      (is (= 42 (reader/decode-compatibility-string #'clojure.core/read-string #'b/stage1-reader-fail!
                                                   "untrusted.qst" "42" {} :tok-0))))))

(deftest bootstrap-literal-facade-arities-remain-unchanged
  (is (= '([token]) (:arglists (meta #'b/stage1-reader-token-kind))))
  (is (= '([source-path raw span token-id cause]) (:arglists (meta #'b/stage1-reader-malformed-numeric!))))
  (is (= '([source-path kind raw span token-id]) (:arglists (meta #'b/stage1-reader-decode-atom))))
  (is (= '([source-path raw span token-id]) (:arglists (meta #'b/stage1-reader-decode-string))))
  (is (= '([source-path raw span token-id]) (:arglists (meta #'b/stage1-reader-decode-character)))))

(deftest refusal-owner-is-captured-before-cause-message-evaluation
  (doseq [decode [reader/decode-compatibility-string reader/decode-compatibility-character]]
    (let [cause (proxy [Exception] []
                  (getMessage []
                    (alter-var-root #'b/stage1-reader-fail!
                                    (constantly (fn [& _] (throw (ex-info "later owner" {:selected :later})))))
                    "cause"))]
      (with-redefs [clojure.core/read-string (fn [& _] (throw cause))
                    b/stage1-reader-fail! (fn [& _] (throw (ex-info "original owner" {:selected :original})))]
        (is (= :original (:selected (ex-data (rejection #(decode #'clojure.core/read-string #'b/stage1-reader-fail!
                                                                  "owner.qst" "raw" {} :tok-0)))))))))
  (let [cause (proxy [NumberFormatException] []
                (getMessage []
                  (alter-var-root #'b/stage1-reader-malformed-numeric! (constantly (fn [& _] :later)))
                  "numeric cause"))]
    (with-redefs [str/starts-with? (fn [& _] (throw cause))
                  b/stage1-reader-malformed-numeric! (fn [& _] :original)]
      (is (= :original (atom-value "owner.qst" :integer "42"))))))
