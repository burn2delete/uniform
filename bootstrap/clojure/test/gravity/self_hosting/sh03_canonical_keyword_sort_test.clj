(ns gravity.self-hosting.sh03-canonical-keyword-sort-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh03-canonical-stream-digest-test]))

(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))
(defn- fixture-operation [name]
  (deref (ns-resolve 'gravity.self-hosting.sh03-canonical-stream-digest-test name)))
(def ^:private path "/declared/keyword-sort.gravity")

(defn- prototype-sort-key [original form]
  ;; Input is ONLY the result of the unchanged fresh key normalizer. Reuse the
  ;; existing host printer's String escaping, including Unicode and controls.
  (if (= :keyword (first form))
    (let [[_ namespace name] form
          ;; Only String/nil components are printed here. Length/level/meta
          ;; and namespace-map settings do not alter the String printer.
          text (binding [*print-dup* false *print-readably* true]
                 (str "[:gravity/canonical-sort-v1 [:keyword "
                      (if (nil? namespace) "nil" (pr-str namespace))
                      " " (pr-str name) "]]"))]
      ((operation 'p15-s23-c6c10-lowercase-hex)
       (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)))
    (original form)))

(defn- normalized [value]
  ((operation 'p15-s23-c6c10-canonical-form*)
   path (atom {:nodes 0 :maximum-depth 0 :maximum-width 0 :scalar-bytes 0
               :maximum-scalar-bytes 0 :maximum-integer-bits 0}) 0 value))

(deftest keyword-sort-prototype-exact-encoding-and-fallback
  ((fixture-operation 'with-reader-limits)
   #(let [original (operation 'p15-s23-c6c10-canonical-sort-key)]
      (doseq [value [:field :ns/field (keyword "quoted\"name") (keyword "back\\slash")
                     (keyword "ns\"\\" "field\"\\") (keyword "λ" "🙂")
                     (keyword "nil") (keyword "\uE000") (keyword "🙂")
                     nil true 42 2/3 1.00M "field" 'field [1] (list 1) #{1} {:a 1}]]
        (let [form (normalized value)]
          (is (= (original form) (prototype-sort-key original form)))
          (is (= (original form) ((operation 'sh03-reader-canonical-stream-sort-key) form))))))))

(deftest typed-hex-stores-match-existing-byte-order-for-complete-byte-domain
  (let [reference (operation 'p15-s23-c6c10-lowercase-hex)
        actual (operation 'sh03-reader-canonical-stream-hex)]
    (doseq [values [[] (range 256) (reverse (range 256))
                    (concat (range 256) (range 256))]]
      (let [bytes (byte-array (map unchecked-byte values))]
        (is (= (reference bytes) (actual bytes)))))
    (doseq [value (range 256)]
      (let [bytes (byte-array [(unchecked-byte value)])]
        (is (= (reference bytes) (actual bytes)))))))

(defn- outcome [value prototype?]
  (let [variable (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-sort-key)
        original @variable]
    (try
      (with-redefs-fn
        {variable (if prototype? #(prototype-sort-key original %) original)}
        #(let [record (bootstrap/p15-s23-c6c10-canonical-record path value)]
           {:text (:text record) :stats (:stats record)}))
      (catch clojure.lang.ExceptionInfo error {:error (ex-data error)}))))

(deftest keyword-sort-malformed-components-and-failure-priority
  ;; Canonical key validation must reject each malformed original key BEFORE
  ;; any fast formatting; no candidate-supplied normalized form is admitted.
  (let [malformed [(keyword "") (keyword "" "name") (keyword "ns" "")
                   (keyword "bad/ns" "name") (keyword "ns" "bad/name")
                   (keyword "bad\nname") (keyword "bad\tname")
                   (keyword (str "bad" (char 0) "name"))
                   (keyword (String. (char-array [(char 0xD800)])))
                   (keyword "ns" (String. (char-array [(char 0xDC00)])))]]
    (doseq [key malformed]
      (let [value (array-map key (Object.))
            reference (outcome value false)
            calls (atom [])
            variable (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-sort-key)
            original @variable
            actual (with-redefs-fn
                     {variable (fn [form] (swap! calls conj form) (prototype-sort-key original form))}
                     #(outcome value false))]
        (is (some? (:error reference)))
        (is (= reference actual))
        ;; Native diagnostic construction has its own valid keys. The invalid
        ;; original key must never reach formatting, even though they do.
        (is (not-any? #(= [:keyword (namespace key) (name key)] %) @calls))))
    (let [bad-child (Object.)
          first-key :valid
          later-key (keyword "bad/name" "field")
          value (array-map first-key bad-child later-key 1)]
      (is (= (outcome value false) (outcome value true))))))

(deftest keyword-sort-prototype-ignores-hostile-printer-context
  (let [original (operation 'p15-s23-c6c10-canonical-sort-key)]
    (doseq [key [:field (keyword "ns\\\"" "λ🙂\\\"")]]
      (let [form (normalized key) expected (original form)]
        (binding [*print-length* 0 *print-level* 0 *print-meta* true
                  *print-dup* true *print-readably* false *print-namespace-maps* true]
          (is (= expected (prototype-sort-key original form))))))))

(defn- observe-stream [value prototype?]
  (let [variable (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-sort-key)
        original @variable generic (operation 'p15-s23-c6c10-canonical-sort-key)
        counts (atom {:keyword 0 :fallback 0})
        started (System/nanoTime)]
    (with-redefs-fn
      {variable (fn [form]
                  (swap! counts update (if (= :keyword (first form)) :keyword :fallback) inc)
                  (if prototype? (original form) (generic form)))}
      #(let [validated ((operation 'sh03-reader-canonical-stream-validation!) path value)
             after-v (System/nanoTime)
             writer (java.io.StringWriter.)
             emitted ((operation 'sh03-reader-canonical-stream-write!) path value validated writer)
             after-e (System/nanoTime)]
         {:text (.toString writer) :validation validated :emission emitted :counts @counts
          :v-ns (- after-v started) :e-ns (- after-e after-v)}))))

(deftest keyword-sort-real-reader-micro-bytes-hash-statistics-and-call-count
  ((fixture-operation 'with-reader-limits)
   #(let [reader-delay (fixture-operation 'actual-reader-result)
          result (:result @reader-delay)
          value {:token-stream (vec (take 1024 (cycle (:token-stream result))))}
          baseline (observe-stream value false)
          prototype (observe-stream value true)]
      (is (= :accepted (:status result)))
      (is (= (:text baseline) (:text prototype)))
      (is (= (digest/sha256-hex (:text baseline)) (digest/sha256-hex (:text prototype))))
      (is (= (:validation baseline) (:emission baseline)
             (:validation prototype) (:emission prototype)))
      (is (= (:counts baseline) (:counts prototype)))
      (is (pos? (get-in prototype [:counts :keyword])))
      (prn {:observation :bounded-real-reader-combined-engine
            :token-rows 1024 :characters (count (:text baseline))
            :generic-key-ordering (dissoc baseline :text)
            :optimized-key-ordering (dissoc prototype :text)}))))

(deftest combined-engine-common-values-and-keys-never-build-tagged-fallbacks
  (let [value {:nil nil :boolean true :integer 123 :string "λ🙂\"\\\n"
               :character \u03bb :keyword (keyword "ns\\\"" "field\\\"")
               :symbol (symbol "ns\\\"" "field\\\"")}
        reference (bootstrap/p15-s23-c6c10-canonical-record path value)]
    (with-redefs-fn
      {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
       (fn [& _] (throw (ex-info "Common scalar reached old tagged-form normalizer" {})))}
      #(let [actual ((fixture-operation 'stream-record) value)]
         (is (= (:text reference) (:text actual)))
         (is (= (:stats reference) (:validation-stats actual) (:stats actual)))))))

(deftest combined-engine-malformed-original-keys-stop-before-private-formatting
  (doseq [key [(keyword "") (keyword "" "field") (keyword "bad/ns" "field")
               (keyword "ns" "bad/name") (keyword "line\nbreak")
               (keyword "back\bspace") (keyword "tab\tname")
               (keyword (String. (char-array [(char 0xD800)])))
               (keyword "ns" (String. (char-array [(char 0xDC00)])))]]
    (let [value (array-map key (Object.)) reference (outcome value false)
          variable (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-sort-key)
          original @variable calls (atom 0)
          actual (with-redefs-fn
                   {variable (fn [form] (swap! calls inc) (original form))}
                   #(try ((operation 'sh03-reader-canonical-stream-validation!) path value) nil
                         (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))]
      (is (some? (:error reference)))
      (is (= reference actual))
      (is (zero? @calls)))))

(deftest combined-engine-statistics-retain-original-numeric-classes
  (let [types (fn [stats] (into {} (map (fn [[key value]] [key (class value)]) stats)))]
    (doseq [value [[] {} (list) [nil true 123 "value" :ns/name 'ns/name]
                   {:outer [{:inner "λ🙂"} (list 42)]} #{2/3 1.00M}]]
      (let [reference ((fixture-operation 'old-record) value)
            actual ((fixture-operation 'stream-record) value)]
        (is (= (:stats reference) (:validation-stats actual) (:stats actual)))
        (is (= (types (:stats reference)) (types (:validation-stats actual))
               (types (:stats actual))))))))

(defn- quoted-text [text]
  (let [writer (java.io.StringWriter.)]
    ((operation 'sh03-reader-canonical-stream-quoted!) writer text)
    (.toString writer)))

(deftest combined-string-writer-matches-existing-escaping-for-all-utf16-units
  (let [mismatches
        (binding [*print-length* nil *print-level* nil *print-meta* false
                  *print-dup* false *print-readably* true *print-namespace-maps* false]
          (reduce (fn [errors code]
                    (let [text (str "a" (char code) "z") expected (pr-str text)
                          actual (quoted-text text)]
                      (if (and (= expected actual)
                               (java.util.Arrays/equals
                                (.getBytes ^String expected java.nio.charset.StandardCharsets/UTF_8)
                                (.getBytes ^String actual java.nio.charset.StandardCharsets/UTF_8)))
                        errors
                        (cond-> (update errors :count inc)
                          (< (count (:examples errors)) 8) (update :examples conj code)))))
                  {:count 0 :examples []} (range 65536)))]
    (is (zero? (:count mismatches)) (pr-str mismatches)))
  (doseq [text ["" "λ🙂\"\\\b\f\r\n\t" (str (apply str (repeat 8191 "x")) "🙂")]]
    (let [expected (binding [*print-readably* true *print-dup* false] (pr-str text))]
      (binding [*print-length* 0 *print-level* 0 *print-meta* true
                *print-dup* true *print-readably* false *print-namespace-maps* true]
        (is (= expected (quoted-text text)))))))

(deftest combined-integer-writer-retains-custom-big-integer-spelling-fallback
  (doseq [text ["123" "not\"numeric\\\n\t" "λ🙂" (String. (char-array [(char 0xD800)]))]]
    (let [value (proxy [java.math.BigInteger] ["123"] (toString [] text))
          reference ((fixture-operation 'old-record) value)
          actual ((fixture-operation 'stream-record) value)]
      (is (= (:text reference) (:text actual)))
      (is (= (:stats reference) (:validation-stats actual) (:stats actual)))
      (is (= (digest/sha256-hex (:text reference))
             ((fn [hash] (subs hash (count "sha256:")))
              ((operation 'sh03-reader-canonical-stream-digest) path value)))))))
