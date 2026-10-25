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

(defn- observe [name value depth seed]
  (let [stats (atom seed)]
    (try
      (let [returned ((operation name) path stats depth value)]
        {:stats @stats :returned returned})
      (catch clojure.lang.ExceptionInfo error
        {:stats @stats :error (ex-data error)}))))

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
      (is (some? (:error result))))))

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

(deftest scalar-validation-physical-duplicates-share-the-existing-stats-atom
  (let [value (keyword "λ" "🙂")
        old (atom (initial-stats)) new (atom (initial-stats))]
    (dotimes [_ 3]
      ((operation 'p15-s23-c6c10-canonical-form*) path old 4 value)
      (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!) path new 4 value))))
    (is (= @old @new))
    (is (= 3 (:nodes @new)))
    (is (= 18 (:scalar-bytes @new)))
    (is (= 4 (:maximum-scalar-bytes @new)))))

(deftest scalar-validation-no-old-normalizer-for-common-scalar-values
  (let [old-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)]
    (with-redefs-fn
      {old-var (fn [& _] (throw (ex-info "Old scalar normalizer called" {:tripwire true})))}
      (fn []
        (doseq [value [nil false true 123 "λ🙂" \u03bb :namespace/name 'namespace/name]]
          (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!)
                     path (atom (initial-stats)) 0 value))))))))

(deftest scalar-validation-does-not-format-integer-decimal-strings
  (let [probe (proxy [java.math.BigInteger] ["123456"]
                (toString
                  ([] (throw (ex-info "Integer formatting tripwire" {:formatted true})))
                  ([radix] (throw (ex-info "Integer formatting tripwire" {:formatted true})))))]
    (is (nil? ((operation 'sh03-reader-canonical-validate-scalar!)
               path (atom (initial-stats)) 0 probe)))
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

(deftest scalar-validation-stream-routing-keeps-keys-and-set-fallback-native
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
                    ((operation 'sh03-reader-canonical-stream-scalar-value?) item)
                    (not (contains? #{:outer :fallback} item)))
           (throw (ex-info "Scalar VALUE reached old V normalizer" {:value item})))
         (binding [*old-fallback* (or *old-fallback* (coll? item) (ratio? item)
                                     (instance? java.math.BigDecimal item))]
           (original source stats depth item)))}
      (fn []
        (is (= expected ((operation 'sh03-reader-canonical-stream-validation!) path value)))))
    (is (some #{:outer :fallback} @calls))
    (is (some #(= [99] %) @calls))
    (is (some set? @calls))
    (is (some ratio? @calls))
    (is (some #(instance? java.math.BigDecimal %) @calls))))
