(ns gravity.self-hosting.sh03-canonical-scalar-validation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private path "/declared/scalar-validation.gravity")
(defrecord ScalarRejectedRecord [value])
(def ^:dynamic *old-fallback* false)

(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))
(defn- initial-stats []
  {:nodes 0 :maximum-depth 0 :maximum-width 0 :scalar-bytes 0
   :maximum-scalar-bytes 0 :maximum-integer-bits 0})

(defn- stats-array [seed]
  (object-array (map seed [:nodes :maximum-depth :maximum-width :scalar-bytes
                           :maximum-scalar-bytes :maximum-integer-bits])))

(defn- stats-map [stats] ((operation 'sh03-reader-canonical-stats-map) stats))

(defn- stats-types [snapshot]
  (into {} (map (fn [[field value]] [field (class value)]) snapshot)))

(defn- observe [name value depth seed]
  (let [legacy? (= name 'p15-s23-c6c10-canonical-form*)
        stats (if legacy? (atom seed) (stats-array seed))
        snapshot (fn [] (if legacy? @stats (stats-map stats)))
        captured (fn [result]
                   (let [state (snapshot)]
                     (assoc result :stats state :stats-types (stats-types state))))]
    (try
      (let [returned ((operation name) path stats depth value)]
        (captured {:returned returned}))
      (catch clojure.lang.ExceptionInfo error
        (captured {:error (ex-data error)}))
      (catch Throwable error
        (captured {:error {:throwable-class (.getName (class error))
                           :message (.getMessage error)}})))))

(defn- compare-validation [value depth seed]
  (let [old (observe 'p15-s23-c6c10-canonical-form* value depth seed)
        new (observe 'sh03-reader-canonical-validate-scalar! value depth seed)]
    (is (= (dissoc old :returned) (dissoc new :returned)))
    (when-not (:error new) (is (nil? (:returned new))))
    new))

(defn- first-guard [f]
  ;; Artificial tiny capacities can also reject the generic diagnostic's own
  ;; hash. Isolate that FIRST guard; native diagnostic parity is tested below.
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-host-fail!)
     (fn [rule source missing subject]
       (throw (ex-info "First scalar guard"
                       {:rule rule :source source :missing-fact missing :subject subject})))}
    f))

(deftest scalar-validation-common-domain-all-six-statistics
  (doseq [value [nil false true (byte 1) (short 2) (int 3) (long 4)
                 Long/MIN_VALUE Long/MAX_VALUE 0 -1 123456789012345678901234567890N
                 (.subtract (.shiftLeft java.math.BigInteger/ONE 256) java.math.BigInteger/ONE)
                 "" "ASCII" "λ🙂\r\n\"\\" \a \newline \u03bb (char 0)
                 :plain :name.space/qualified (keyword "λ" "🙂")
                 'plain 'name.space/qualified (symbol "/") (symbol "λ" "🙂")]
          depth [0 7]]
    (testing (str (class value) " depth=" depth)
      (is (true? ((operation 'sh03-reader-canonical-stream-scalar-value?) value)))
      (compare-validation value depth (initial-stats))
      (is (= (observe 'p15-s23-c6c10-canonical-form* value depth (initial-stats))
             (observe 'sh03-reader-canonical-key-form! value depth (initial-stats))))
      ;; Existing shared invocation counters must be updated, never reset.
      (compare-validation value depth
                          {:nodes 13 :maximum-depth 9 :maximum-width 5
                           :scalar-bytes 11 :maximum-scalar-bytes 8 :maximum-integer-bits 17}))))

(deftest scalar-validation-native-malformed-errors-and-order-match
  (doseq [value [(keyword "") (keyword "ns" "bad/name") (keyword "bad ns" "name")
                 (symbol "") (symbol "ns" "bad/name") (symbol "bad ns" "name")
                 (with-meta 'good {:present true}) (with-meta (symbol "bad name") {:present true})
                 (char 0xD800) (char 0xDFFF)
                 (String. (char-array [(char 0xD800)]))
                 (String. (char-array [(char 0xDC00)]))
                 (.shiftLeft java.math.BigInteger/ONE 256)]]
    (let [result (compare-validation value 0 (initial-stats))]
      (is (some? (:error result)))
      (is (= (observe 'p15-s23-c6c10-canonical-form* value 0 (initial-stats))
             (observe 'sh03-reader-canonical-key-form! value 0 (initial-stats)))))))

(deftest scalar-validation-exact-and-minus-one-guards
  (doseq [value [nil true 123 "λ🙂" :namespace/name (symbol "namespace" "λ🙂") \u03bb]]
    (let [stats (:stats (observe 'p15-s23-c6c10-canonical-form* value 7 (initial-stats)))]
      (doseq [[name field] [['p15-s23-c6c10-max-carrier-nodes :nodes]
                           ['p15-s23-c6c10-max-carrier-depth :maximum-depth]
                           ['p15-s23-c6c10-max-scalar-bytes :maximum-scalar-bytes]
                           ['p15-s23-c6c10-max-total-scalar-bytes :scalar-bytes]]
              :let [maximum (get stats field)]
              :when (pos? maximum)]
        (let [var (ns-resolve 'gravity.bootstrap name)]
          (with-bindings {var maximum}
            (is (nil? (:error (compare-validation value 7 (initial-stats))))))
          (with-bindings {var (dec maximum)}
            (first-guard
             #(is (some? (:error (compare-validation value 7 (initial-stats))))))))))))

(deftest scalar-validation-competing-failures-preserve-original-precedence
  (let [nodes (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-nodes)
        scalar (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-scalar-bytes)]
    (doseq [[value bindings expected]
            [[(with-meta (symbol "bad name") {:present true}) {nodes 0} :bounded-canonical-carrier]
             [(with-meta (symbol "bad name") {:present true}) {} :metadata-free-canonical-value]
             [(keyword (apply str (repeat 40 "a")) "bad/name") {scalar 1} :canonical-keyword-components]
             [(symbol "bad/name" "long-name") {scalar 1} :canonical-symbol-components]
             [(String. (char-array [(char 0xD800)])) {scalar 0} :maximum-scalar-characters]
             [(String. (char-array [(char 0xD800)])) {scalar 1} :well-formed-unicode-scalar-string]
             [(char 0xD800) {nodes 0} :bounded-canonical-carrier]]]
      (with-bindings bindings
        (first-guard
         #(let [result (compare-validation value 0 (initial-stats))]
            (is (= expected (get-in result [:error :missing-fact])))))))))

(deftest scalar-validation-physical-duplicates-share-the-existing-stats-array
  (let [value (keyword "λ" "🙂")
        old (atom (initial-stats)) new (stats-array (initial-stats))]
    (dotimes [_ 3]
      ((operation 'p15-s23-c6c10-canonical-form*) path old 4 value)
      (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!) path new 4 value))))
    (is (= @old (stats-map new)))
    (is (= 3 (:nodes (stats-map new))))
    (is (= 18 (:scalar-bytes (stats-map new))))
    (is (= 4 (:maximum-scalar-bytes (stats-map new))))))

(deftest scalar-validation-no-old-normalizer-for-common-scalar-values
  (let [old-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)]
    (with-redefs-fn
      {old-var (fn [& _] (throw (ex-info "Old scalar normalizer called" {:tripwire true})))}
      (fn []
        (doseq [value [nil false true 123 "λ🙂" \u03bb :namespace/name 'namespace/name]]
          (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!)
                     path (stats-array (initial-stats)) 0 value)))
          (is (vector? ((operation 'sh03-reader-canonical-key-form!)
                        path (stats-array (initial-stats)) 0 value))))))))

(deftest scalar-validation-does-not-format-integer-decimal-strings
  (let [probe (proxy [java.math.BigInteger] ["123456"]
                (toString
                  ([] (throw (ex-info "Integer formatting tripwire" {:formatted true})))
                  ([radix] (throw (ex-info "Integer formatting tripwire" {:formatted true})))))]
    (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!)
               path (stats-array (initial-stats)) 0 probe)))
    ;; The reference still formats a decimal string: the probe is effective.
    (is (:formatted (:error (observe 'p15-s23-c6c10-canonical-form* probe 0 (initial-stats)))))))

(deftest scalar-validation-unsupported-and-compound-domains-keep-old-fallback
  (doseq [value [2/3 1.00M #{:a} [1] (list 1) {:a 1}
                 (ScalarRejectedRecord. 1) 1.0 (Object.)]]
    (is (false? ((operation 'sh03-reader-canonical-stream-scalar-value?) value)))
    (let [var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
          original @var calls (atom 0)
          reference (observe 'p15-s23-c6c10-canonical-form* value 0 (initial-stats))
          actual (with-redefs-fn
                   {var (fn [& arguments] (swap! calls inc) (apply original arguments))}
                   #(observe 'sh03-reader-canonical-validate-scalar! value 0 (initial-stats)))]
      (is (pos? @calls))
      (is (= (dissoc reference :returned) (dissoc actual :returned))))))

(deftest scalar-validation-stream-routing-optimizes-common-keys-and-keeps-fallback-native
  (let [value {:outer [nil true 123 "value" 'value/symbol :value-key \u03bb {[99] "child"}]
               :fallback #{2/3 1.00M}}
        expected (:stats (bootstrap/p15-s23-c6c10-canonical-record path value))
        var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
        original @var calls (atom [])]
    (with-redefs-fn
      {var
       (fn [source stats depth item]
         (swap! calls conj item)
         (when (and (not *old-fallback*)
                    ((operation 'sh03-reader-canonical-stream-scalar-value?) item))
           (throw (ex-info "Common scalar reached old V normalizer" {:value item})))
         (binding [*old-fallback* (or *old-fallback* (coll? item) (ratio? item)
                                     (instance? java.math.BigDecimal item))]
           (original source stats depth item)))}
      (fn []
        (is (= expected ((operation 'sh03-reader-canonical-stream-validation!) path value)))))
    (is (not-any? #{:outer :fallback} @calls))
    (is (some #(= [99] %) @calls))
    (is (some set? @calls))
    (is (some ratio? @calls))
    (is (some #(instance? java.math.BigDecimal %) @calls))))

(deftest scalar-validation-fallback-copies-all-partial-error-statistics
  (let [nodes (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-nodes)
        scalar (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-scalar-bytes)
        total (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-total-scalar-bytes)
        value [1 "λ🙂" (with-meta 'x {:present true})]]
    (doseq [bindings [{} {nodes 2} {scalar 5} {total 5}]]
      (with-bindings bindings
        (first-guard
         #(let [expected (observe 'p15-s23-c6c10-canonical-form* value 3 (initial-stats))
                actual (observe 'sh03-reader-canonical-fallback-form! value 3 (initial-stats))]
            (is (some? (:error expected)))
            (is (= expected actual))))))))

(deftest scalar-validation-fallback-atoms-and-pass-arrays-are-independent
  (let [var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
        original @var legacy-atoms (atom [])
        first-pass (stats-array (initial-stats))
        second-pass (stats-array (initial-stats))]
    (is (not (identical? first-pass second-pass)))
    (with-redefs-fn
      {var (fn [source legacy depth value]
             (swap! legacy-atoms conj legacy)
             (original source legacy depth value))}
      (fn []
        ((operation 'sh03-reader-canonical-fallback-form!) path first-pass 0 2/3)
        ((operation 'sh03-reader-canonical-fallback-form!) path first-pass 0 1.00M)))
    (is (= 2 (count @legacy-atoms)))
    (is (not (identical? (first @legacy-atoms) (second @legacy-atoms))))
    (is (= (initial-stats) (stats-map second-pass)))
    (let [before (stats-map first-pass)]
      (swap! (first @legacy-atoms) assoc :nodes 9999)
      (is (= before (stats-map first-pass))))
    ((operation 'sh03-reader-canonical-validate-scalar!) path second-pass 0 "separate")
    (is (= 1 (:nodes (stats-map second-pass))))
    (is (= 2 (:nodes (stats-map first-pass))))))

(deftest scalar-validation-key-forms-preserve-compound-fallback-and-key-guard-stats
  (doseq [value [[1] (list :x "λ") #{:a :b} 2/3 1.00M {:x 1}]]
    (is (= (observe 'p15-s23-c6c10-canonical-form* value 2 (initial-stats))
           (observe 'sh03-reader-canonical-key-form! value 2 (initial-stats)))))
  (let [total (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-total-scalar-bytes)]
    (with-bindings {total 3}
      (first-guard
       #(let [value (keyword "ns" "name")]
          (is (= (observe 'p15-s23-c6c10-canonical-form* value 4 (initial-stats))
                 (observe 'sh03-reader-canonical-key-form! value 4 (initial-stats)))))))))

(deftest scalar-validation-object-counters-preserve-seeded-numeric-types-and-overflow
  (let [nodes (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-nodes)
        total (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-total-scalar-bytes)
        beyond (+ (bigint Long/MAX_VALUE) 10)
        seed {:nodes beyond :maximum-depth 9N :maximum-width 5N
              :scalar-bytes beyond :maximum-scalar-bytes 8N :maximum-integer-bits 17N}]
    (with-bindings {nodes (+ beyond 10) total (+ beyond 100)}
      (doseq [value [nil 123 "λ🙂" :namespace/name 'namespace/name]]
        (compare-validation value 7 seed)
        (is (= (observe 'p15-s23-c6c10-canonical-form* value 7 seed)
               (observe 'sh03-reader-canonical-key-form! value 7 seed)))))
    (doseq [[field value] [[:nodes nil] [:scalar-bytes "x"]]]
      (let [seed (assoc (initial-stats) field Long/MAX_VALUE)
            old (observe 'p15-s23-c6c10-canonical-form* value 7 seed)
            new (observe 'sh03-reader-canonical-validate-scalar! value 7 seed)]
        (is (= old new))
        (is (= "java.lang.ArithmeticException" (get-in old [:error :throwable-class])))
        ;; No coercion/promotion or late maximum-scalar update after overflow.
        (is (= Long/MAX_VALUE (get-in new [:stats field])))
        (is (= 0 (get-in new [:stats :maximum-scalar-bytes])))))))

(deftest scalar-validation-fallback-keeps-throwable-identity-and-suppresses-copy-error
  (let [normalizer (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
        copier (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-copy-stats!)]
    (doseq [primary [(ex-info "Primary fallback" {:original true}) (AssertionError. "Primary assertion")]]
      (let [stats (stats-array (initial-stats))
            partial {:nodes 3N :maximum-depth 4N :maximum-width 5N
                     :scalar-bytes 6N :maximum-scalar-bytes 7N :maximum-integer-bits 8N}
            thrown (with-redefs-fn
                     {normalizer (fn [_ legacy _ _] (reset! legacy partial) (throw primary))}
                     (fn [] (try ((operation 'sh03-reader-canonical-fallback-form!) path stats 3 2/3)
                                 nil (catch Throwable error error))))]
        (is (identical? primary thrown))
        (is (= partial (stats-map stats)))
        (is (= (stats-types partial) (stats-types (stats-map stats))))))
    (let [primary (ex-info "Primary fallback" {:original true})
          secondary (ex-info "Copy failure" {})
          thrown (with-redefs-fn
                   {normalizer (fn [& _] (throw primary))
                    copier (fn [& _] (throw secondary))}
                   (fn [] (try ((operation 'sh03-reader-canonical-fallback-form!)
                                path (stats-array (initial-stats)) 0 2/3)
                               nil (catch Throwable error error))))]
      (is (identical? primary thrown))
      (is (= {:original true} (ex-data thrown)))
      (is (= [secondary] (vec (.getSuppressed primary)))))))

(deftest scalar-validation-fallback-nested-multifault-prefix-matches-original
  (doseq [value [(array-map :z (Object.) :a (with-meta [] {:present true}))
                 (array-map :z (with-meta [] {:present true}) :a (Object.))
                 (ScalarRejectedRecord. 1) #{2/3 1.00M} [1 (list {:x "λ🙂"})]]]
    (let [seed {:nodes 2N :maximum-depth 1N :maximum-width 1N
                :scalar-bytes 2N :maximum-scalar-bytes 1N :maximum-integer-bits 1N}]
      (is (= (observe 'p15-s23-c6c10-canonical-form* value 2 seed)
             (observe 'sh03-reader-canonical-fallback-form! value 2 seed)))))
  (let [width (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-container-width)
        depth (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-depth)]
    (doseq [bindings [{width 0} {depth 1}]]
      (with-bindings bindings
        (first-guard
         #(is (= (observe 'p15-s23-c6c10-canonical-form* {:x [1]} 2 (initial-stats))
                 (observe 'sh03-reader-canonical-fallback-form! {:x [1]} 2 (initial-stats)))))))))

(deftest scalar-validation-snapshots-and-concurrent-passes-do-not-share-counters
  (let [a (stats-array (initial-stats)) b (stats-array (initial-stats))
        before (stats-map a)
        tasks (mapv (fn [stats]
                      (future
                        (dotimes [_ 100]
                          ((operation 'sh03-reader-canonical-validate-scalar!) path stats 0 "x"))
                        (stats-map stats))) [a b])]
    (is (= [(assoc (initial-stats) :nodes 100 :scalar-bytes 100 :maximum-scalar-bytes 1)
            (assoc (initial-stats) :nodes 100 :scalar-bytes 100 :maximum-scalar-bytes 1)]
           (mapv deref tasks)))
    (is (= (initial-stats) before))
    ((operation 'sh03-reader-canonical-validate-scalar!) path a 0 123)
    (is (= 101 (:nodes (stats-map a))))
    (is (= 100 (:nodes (stats-map b))))))

(defn- bounded-mismatches [values compatible?]
  ;; Exhaust the finite corpus without one reporting transaction per code unit.
  (reduce (fn [out value]
            (if (compatible? value) out
              (cond-> (update out :count inc)
                (< (count (:examples out)) 8) (update :examples conj value))))
          {:count 0 :examples []} values))

(deftest scalar-validation-direct-name-guard-matches-all-utf16-units
  (let [old (operation 'p15-s23-c6c10-valid-named-component?)
        optimized (operation 'sh03-reader-canonical-named-component?)
        summary (bounded-mismatches
                 (range 65536)
                 (fn [code]
                   (let [unit (str (char code)) embedded (str "a" unit "z")]
                     (and (= (old unit) (optimized unit))
                          (= (old embedded) (optimized embedded))))))]
    (is (zero? (:count summary)) (pr-str summary))
    (doseq [value [nil false 1 :keyword 'symbol [] "" " " "\t" "/" "a/b"
                   "quote\"" "back\\slash" "λ🙂" "\u00a0" "\u200b" "\ufeff"
                   (String. (char-array [(char 0xD800)]))]]
      (is (= (old value) (optimized value))))))

(deftest scalar-validation-direct-utf8-length-matches-valid-unicode-and-boxed-type
  (let [old (operation 'utf8-byte-count)
        optimized (operation 'sh03-reader-canonical-utf8-length)
        compatible? (fn [text]
                      (let [expected (old text) actual (optimized text)]
                        (and (= expected actual) (= Integer (class actual) (class expected)))))
        bmp (bounded-mismatches (remove #(<= 0xD800 % 0xDFFF) (range 65536))
                               (fn [code] (compatible? (str (char code)))))]
    (is (zero? (:count bmp)) (pr-str bmp))
    (doseq [high [0xD800 0xD801 0xDBFE 0xDBFF]
            low [0xDC00 0xDC01 0xDFFE 0xDFFF]]
      (let [pair (String. (char-array [(char high) (char low)]))]
        (is (compatible? pair))
        (is (compatible? (str "aλ\"\\" pair "\r\nz")))))
    (doseq [text ["" "ASCII\r\n\t" "\u0000\u007f\u0080\u07ff\u0800\ud7ff\ue000\uffff"
                  "quotes\" backslash\\ λ🙂"]]
      (is (compatible? text))
      (compare-validation text 3 (initial-stats)))))

(deftest scalar-validation-direct-name-and-length-preserve-native-key-state
  (doseq [namespace ["ns" "λ" "" "bad/ns" "bad ns" (str (char 0xD800))]
          name ["name" "🙂" "" "/" "quote\"" "back\\slash" (str (char 0xDC00))]
          value [(keyword namespace name) (symbol namespace name)]]
    (compare-validation value 2 (initial-stats))))

(deftest scalar-validation-length-only-counter-is-after-length-and-unicode-guards
  (let [counter (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-utf8-length)
        scalar (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-scalar-bytes)]
    (with-redefs-fn
      {counter (fn [& _] (throw (ex-info "Premature UTF8 length call" {:tripwire true})))}
      (fn []
        (with-bindings {scalar 1}
          (first-guard
           (fn []
             (doseq [value [(String. (char-array [(char 0xD800)])) "xx"
                            (keyword "ns" "bad/name") (symbol "bad/ns" "name")
                            (symbol (str (char 0xD800)) "x")]]
               (is (some? (:error (compare-validation value 0 (initial-stats)))))))))))))

(deftest scalar-validation-hot-common-path-avoids-string-seqs-and-encoded-byte-arrays
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-valid-named-component?)
     (fn [& _] (throw (ex-info "Old String-sequence name guard" {:tripwire true})))
     (ns-resolve 'gravity.bootstrap 'utf8-byte-count)
     (fn [& _] (throw (ex-info "Old encoded-byte-array count" {:tripwire true})))}
    (fn []
      (doseq [value ["ASCIIλ🙂" :namespace/name (keyword "λ" "🙂")
                     'namespace/name (symbol "λ" "🙂") (symbol "/")]]
        (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!)
                   path (stats-array (initial-stats)) 0 value)))))))

(defn- native-wrapper-cases [value]
  (cond-> [(long value)]
    (<= Byte/MIN_VALUE value Byte/MAX_VALUE) (conj (byte value))
    (<= Short/MIN_VALUE value Short/MAX_VALUE) (conj (short value))
    (<= Integer/MIN_VALUE value Integer/MAX_VALUE) (conj (int value))))

(deftest scalar-validation-native-integer-bits-and-text-match-every-bit-boundary
  (let [minimum (biginteger Long/MIN_VALUE) maximum (biginteger Long/MAX_VALUE)
        values (mapcat
                native-wrapper-cases
                (for [bit (range 64) delta [-1 0 1] sign [-1 1]
                      :let [positive (.add (.shiftLeft java.math.BigInteger/ONE bit)
                                           (java.math.BigInteger/valueOf delta))
                            value (if (= -1 sign) (.negate positive) positive)]
                      :when (and (<= (.compareTo minimum value) 0)
                                 (<= (.compareTo value maximum) 0))]
                  (.longValue value)))
        summary (bounded-mismatches
                 values
                 (fn [value]
                   (let [expected (.bitLength (.abs (biginteger value)))
                         actual ((operation 'sh03-reader-canonical-integer-bits) value)]
                     (and (= expected actual) (= Integer (class actual))
                          (= (.toString (biginteger value))
                             ((operation 'sh03-reader-canonical-integer-text) value))))))]
    (is (zero? (:count summary)) (pr-str summary)))
  (doseq [value [(byte Byte/MIN_VALUE) (byte Byte/MAX_VALUE)
                 (short Short/MIN_VALUE) (short Short/MAX_VALUE)
                 Integer/MIN_VALUE Integer/MAX_VALUE Long/MIN_VALUE Long/MAX_VALUE]]
    (compare-validation value 0 (initial-stats))
    (is (= (observe 'p15-s23-c6c10-canonical-form* value 0 (initial-stats))
           (observe 'sh03-reader-canonical-key-form! value 0 (initial-stats)))))
  (let [var (ns-resolve 'gravity.bootstrap 'biginteger)]
    ;; Native wrapper paths must not allocate a BigInteger merely to validate
    ;; bits or render decimal text. The unchanged reference uses this Var.
    (with-redefs-fn
      {var (fn [& _] (throw (ex-info "Native integer converted to BigInteger" {:tripwire true})))}
      (fn []
        (doseq [value [(byte -1) (short -1) (int -1) Long/MIN_VALUE 0]]
          (is (integer? ((operation 'sh03-reader-canonical-integer-bits) value)))
          (is (string? ((operation 'sh03-reader-canonical-integer-text) value))))))))

(deftest scalar-validation-native-integer-bit-cap-and-big-boundaries-preserve-state
  (let [var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-integer-bits)]
    (doseq [limit [64 63]]
      (with-redefs-fn
        {var limit}
        (fn []
          (let [old (observe 'p15-s23-c6c10-canonical-form* Long/MIN_VALUE 0 (initial-stats))
                new (observe 'sh03-reader-canonical-key-form! Long/MIN_VALUE 0 (initial-stats))]
            (is (= old new))
            (is (= 64 (get-in new [:stats :maximum-integer-bits])))
            (is (= Integer (get-in new [:stats-types :maximum-integer-bits])))
            (is (= (= limit 63) (boolean (:error new)))))))))
  (doseq [bits [256 257]
          value [(let [power (.shiftLeft java.math.BigInteger/ONE (dec bits))]
                   (.add power java.math.BigInteger/ONE))
                 (bigint (.add (.shiftLeft java.math.BigInteger/ONE (dec bits))
                               java.math.BigInteger/ONE))]]
    (is (false? ((operation 'sh03-reader-canonical-native-integer?) value)))
    (compare-validation value 0 (initial-stats))
    (is (= (observe 'p15-s23-c6c10-canonical-form* value 0 (initial-stats))
           (observe 'sh03-reader-canonical-key-form! value 0 (initial-stats))))))

(deftest scalar-validation-custom-big-integer-callbacks-remain-native
  (let [calls (atom [])
        probe (proxy [java.math.BigInteger] ["123456"]
                (abs [] (swap! calls conj :abs) this)
                (bitLength [] (swap! calls conj :bits) 37)
                (toString
                  ([] (swap! calls conj :text) "custom-original-spelling")
                  ([radix] (swap! calls conj :text-radix) "custom-original-spelling")))
        old (observe 'p15-s23-c6c10-canonical-form* probe 0 (initial-stats))
        old-calls @calls]
    (reset! calls [])
    (is (false? ((operation 'sh03-reader-canonical-native-integer?) probe)))
    (is (= old (observe 'sh03-reader-canonical-key-form! probe 0 (initial-stats))))
    (is (= old-calls @calls [:abs :bits :text]))
    (is (= [:integer "custom-original-spelling"] (:returned old)))
    (is (= 37 (get-in old [:stats :maximum-integer-bits]))))
  (doseq [failure [:bits :text]]
    (let [probe (proxy [java.math.BigInteger] ["123456"]
                  (bitLength [] (if (= failure :bits)
                                  (throw (ex-info "Custom bit callback" {:callback :bits})) 17))
                  (toString [] (if (= failure :text)
                                 (throw (ex-info "Custom text callback" {:callback :text})) "123456")))]
      (is (= (observe 'p15-s23-c6c10-canonical-form* probe 0 (initial-stats))
             (observe 'sh03-reader-canonical-key-form! probe 0 (initial-stats)))))))
