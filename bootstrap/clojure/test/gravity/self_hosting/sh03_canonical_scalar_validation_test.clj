(ns gravity.self-hosting.sh03-canonical-scalar-validation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private path "/declared/scalar-validation.gravity")
(defrecord ScalarRejectedRecord [value])
(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))

(defn- observe [name value]
  (try {:returned ((operation name) path value)}
       (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))

(defn- compare-validation [value]
  (let [reference (observe 'p15-s23-c6c10-canonical-form* value)
        actual (observe 'sh03-reader-canonical-validate-scalar! value)
        key-form (observe 'sh03-reader-canonical-key-form! value)]
    (is (= (:error reference) (:error actual)))
    (is (= reference key-form))
    (when-not (:error actual) (is (nil? (:returned actual))))
    actual))

(defn- first-guard [operation]
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-host-fail!)
     (fn [rule source missing subject]
       (throw (ex-info "First scalar guard"
                       {:rule rule :source source :missing-fact missing :subject subject})))}
    operation))

(deftest scalar-validation-preserves-common-values-and-native-extremes
  (doseq [value [nil false true (byte Byte/MIN_VALUE) (byte Byte/MAX_VALUE)
                 (short Short/MIN_VALUE) (short Short/MAX_VALUE)
                 Integer/MIN_VALUE Integer/MAX_VALUE Long/MIN_VALUE Long/MAX_VALUE
                 0 -1 123456789012345678901234567890N
                 (.shiftLeft java.math.BigInteger/ONE 257)
                 "" "ASCII" "λ🙂\r\n\"\\" \a \newline \u03bb (char 0)
                 :plain :name.space/qualified (keyword "λ" "🙂")
                 'plain 'name.space/qualified (symbol "/") (symbol "λ" "🙂")]]
    (is (true? ((operation 'sh03-reader-canonical-stream-scalar-value?) value)))
    (is (nil? (:error (compare-validation value))))))

(deftest scalar-validation-preserves-semantic-errors-and-precedence
  (doseq [value [(keyword "") (keyword "ns" "bad/name") (keyword "bad ns" "name")
                 (symbol "") (symbol "ns" "bad/name") (symbol "bad ns" "name")
                 (with-meta 'good {:present true})
                 (with-meta (symbol "bad name") {:present true})
                 (char 0xD800) (char 0xDFFF)
                 (String. (char-array [(char 0xD800)]))
                 (String. (char-array [(char 0xDC00)]))]]
    (first-guard #(is (some? (:error (compare-validation value))))))
  (doseq [[value expected]
          [[(with-meta (symbol "bad name") {:present true}) :metadata-free-canonical-value]
           [(keyword "ns" "bad/name") :canonical-keyword-components]
           [(symbol "bad/ns" "name") :canonical-symbol-components]
           [(String. (char-array [(char 0xD800)])) :well-formed-unicode-scalar-string]
           [(char 0xD800) :unicode-scalar-character]]]
    (first-guard
     #(is (= expected (get-in (compare-validation value) [:error :missing-fact]))))))

(deftest scalar-validation-common-path-does-not-normalize-discarded-forms
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
     (fn [& _] (throw (ex-info "Common value reached fallback" {:tripwire true})))}
    #(doseq [value [nil true Long/MIN_VALUE "λ🙂" \u03bb :namespace/name 'namespace/name]]
       (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!) path value)))
       (is (vector? ((operation 'sh03-reader-canonical-key-form!) path value))))))

(deftest scalar-validation-fallback-retains-exact-values-and-native-errors
  (doseq [value [2/3 1.00M (java.math.BigDecimal. java.math.BigInteger/ONE 65537)
                 [1] (list :x "λ") #{:a :b} {:x [1 2]}
                 (ScalarRejectedRecord. 1) (Object.)
                 (array-map :z (Object.) :a (with-meta [] {:present true}))
                 (array-map :z (with-meta [] {:present true}) :a (Object.))]]
    (first-guard
     #(is (= (observe 'p15-s23-c6c10-canonical-form* value)
             (observe 'sh03-reader-canonical-fallback-form! value)))))
  (let [primary (ex-info "Primary fallback" {:original true})
        thrown (with-redefs-fn
                 {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
                  (fn [& _] (throw primary))}
                 #(try ((operation 'sh03-reader-canonical-fallback-form!) path 2/3)
                       nil (catch Throwable error error)))]
    (is (identical? primary thrown))))

(deftest scalar-name-validation-matches-complete-utf16-code-unit-domain
  (let [reference (operation 'p15-s23-c6c10-valid-named-component?)
        actual (operation 'sh03-reader-canonical-named-component?)
        mismatches (reduce (fn [out unit]
                             (let [text (str (char unit))]
                               (if (= (reference text) (actual text)) out (conj out unit))))
                           [] (range 65536))]
    (is (= [] mismatches)))
  (doseq [namespace ["ns" "λ" "" "bad/ns" "bad ns" (str (char 0xD800))]
          name ["name" "🙂" "" "/" "quote\"" "back\\slash" (str (char 0xDC00))]
          value [(keyword namespace name) (symbol namespace name)]]
    (first-guard #(compare-validation value))))

(deftest native-integer-text-preserves-real-native-representations
  (doseq [value [(byte -1) (short -1) (int -1) Long/MIN_VALUE Long/MAX_VALUE
                 123456789012345678901234567890N
                 (.shiftLeft java.math.BigInteger/ONE 257)]]
    (is (= (.toString (biginteger value))
           ((operation 'sh03-reader-canonical-integer-text) value))))
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'biginteger)
     (fn [& _] (throw (ex-info "Native integer converted" {:tripwire true})))}
    #(doseq [value [(byte -1) (short -1) (int -1) Long/MIN_VALUE 0]]
       (is (string? ((operation 'sh03-reader-canonical-integer-text) value))))))
