(ns gravity.self-hosting.sh03-canonical-stream-digest-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh03-complete-construction-census-test]))

(def ^:private source-path "/declared/canonical-stream.gravity")
(defrecord StreamRejectedRecord [value])

(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))
(defn- census-operation [name]
  (deref (ns-resolve 'gravity.self-hosting.sh03-complete-construction-census-test name)))

(defn- with-reader-limits [f]
  (with-bindings
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-nodes)
     bootstrap/sh03-reader-canonical-maximum-nodes
     (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-carrier-depth)
     bootstrap/sh03-reader-canonical-maximum-depth
     (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-container-width)
     bootstrap/sh03-reader-canonical-maximum-width
     (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-scalar-bytes)
     bootstrap/sh03-reader-canonical-maximum-scalar-bytes
     (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-total-scalar-bytes)
     bootstrap/sh03-reader-canonical-maximum-total-scalar-bytes}
    (f)))

(defn- old-record [value]
  (bootstrap/p15-s23-c6c10-canonical-record source-path value))

(defn- stream-record [value]
  ;; Internal capture for small differential fixtures; production never retains
  ;; canonical text. V/E are the same private functions used by the digest API.
  (let [validated ((operation 'sh03-reader-canonical-stream-validation!) source-path value)
        writer (java.io.StringWriter.)
        emitted ((operation 'sh03-reader-canonical-stream-write!) source-path value validated writer)]
    {:text (.toString writer) :stats emitted :validation-stats validated}))

(defn- check-compatible [value]
  (let [reference (old-record value)
        actual (stream-record value)
        expected-digest (str "sha256:" (digest/sha256-hex (:text reference)))]
    (is (= (:text reference) (:text actual)))
    (is (= (:stats reference) (:validation-stats actual) (:stats actual)))
    (is (= expected-digest
           ((operation 'sh03-reader-canonical-stream-digest) source-path value)))
    (is (= expected-digest (bootstrap/sh03-reader-canonical-digest source-path value)))))

(deftest canonical-stream-supported-domain-bytes-hashes-and-stats
  (with-reader-limits
    #(doseq [value [nil false true 0 -1 Long/MIN_VALUE Long/MAX_VALUE
                   (bigint (.subtract (.shiftLeft java.math.BigInteger/ONE 256)
                                      java.math.BigInteger/ONE))
                   2/3 -7/9 1.00M (java.math.BigDecimal. java.math.BigInteger/ONE -65536)
                   \a \newline \u03bb (char 0) "" "quotes\" slash\\ newline\n\r\t λ 🙂"
                   :plain :name.space/qualified 'plain 'name.space/qualified (symbol "/")
                   [] [nil true {:z [1 2] :a (list 3 "four")}]
                   (subvec [0 1 2 3] 1 3) (list) (list 1 :a [2])
                   {} (array-map :z 1 :a 2) (hash-map :z 1 :a 2)
                   (sorted-map "z" 1 "a" 2)
                   {[1 2] {:nested "value"}} {(list 1 2) :list-key}
                   #{} #{:b :a} (sorted-set "b" "a") (set [[1] (list 2)])]]
       (check-compatible value)))
  (with-reader-limits
    #(do (is (= (:text (stream-record (array-map :z 1 :a 2)))
                (:text (stream-record (array-map :a 2 :z 1)))))
         (is (not= (:text (stream-record [1 2])) (:text (stream-record (list 1 2))))))))

(deftest canonical-stream-production-utf8-bytes-ignore-caller-printer-context
  (let [value (array-map :z "λ🙂\"\\\n" :a [(list :x 1.00M) 'name/field])
        reference (old-record value)
        captured (atom nil)
        actual
        (binding [*print-length* 1 *print-level* 1 *print-meta* true
                  *print-readably* false *print-dup* true *print-namespace-maps* true]
          (with-redefs [digest/sha256-output-hex
                        (fn [producer]
                          (let [output (java.io.ByteArrayOutputStream.)]
                            (producer output)
                            (let [bytes (.toByteArray output)]
                              (reset! captured bytes)
                              ;; Capture the production writer while computing
                              ;; the real SHA-256, never a canned digest.
                              (digest/sha256-bytes-hex bytes))))]
            ((operation 'sh03-reader-canonical-stream-digest) source-path value)))
        expected (.getBytes ^String (:text reference) java.nio.charset.StandardCharsets/UTF_8)]
    (is (java.util.Arrays/equals ^bytes expected ^bytes @captured))
    (is (= (str "sha256:" (digest/sha256-bytes-hex expected)) actual))))

(def ^:private actual-reader-result
  (delay
    (let [text (str "; trivia\n[nil true 42 0xff 2/3 1/0 1.25e2 \"a\\nb🙂\" \\newline :a/b x/y]\n"
                    "{:a [1 2] :b #{3 4}} '(x) @x ^:private x `(~x ~@ys)\n"
                    "#uuid \"550e8400-e29b-41d4-a716-446655440000\"\n"
                    "#inst \"2024-01-02T03:04:05Z\"\n"
                    (apply str (repeat 257 "9")) "\n")
          arguments ((census-operation 'input) text true)
          result ((census-operation 'execute) 'sh03-read-source-unit arguments)]
      {:arguments arguments :result result})))

(deftest canonical-stream-actual-reader-rows-and-request-routing
  (let [{:keys [arguments result]} @actual-reader-result
        [unit bytes _] arguments]
    (is (= :accepted (:status result)))
    (with-reader-limits
      #(doseq [row (concat (:token-stream result) (:form-tree result)
                           (:semantic-value-table result) (:literal-decoding-records result)
                           (:reader-extension-invocation-records result))]
         (check-compatible row)))
    (let [resolved (bootstrap/sh03-reader-resolve-digest-requests!
                    source-path result (byte-array (map unchecked-byte bytes)))]
      (is (= 6 (count (:resolved-requests resolved))))
      (is (= (:source-content-hash unit) (first (:resolved-digests resolved))))
      (with-reader-limits
        #(doseq [request (rest (:resolved-requests resolved))]
           (check-compatible (:preimage request))
           (is (= (:digest request)
                  (str "sha256:" (digest/sha256-hex (:text (old-record (:preimage request))))))))))))

(defn- failure-data [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- first-failure [f]
  ;; Deliberately tiny fake limits can also reject the generic diagnostic's
  ;; own hasher. This control compares the FIRST native guard, not that hasher.
  (with-redefs-fn
    {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-host-fail!)
     (fn [rule path missing subject]
       (throw (ex-info "Isolated first canonical guard"
                       {:rule rule :source path :missing-fact missing :subject subject})))}
    #(failure-data f)))

(deftest canonical-stream-native-malformed-domain-matches-reference
  (with-reader-limits
    #(doseq [value [(Object.) 1.0 (float 1.0) (StreamRejectedRecord. 1)
                   (with-meta [1] {:metadata true}) (with-meta {:a 1} {:metadata true})
                   (with-meta #{1} {:metadata true}) (with-meta '(1) {:metadata true})
                   (lazy-seq [1]) (keyword "") (keyword "ns" "bad name")
                   (symbol "") (symbol "ns" "bad/name")
                   (char 0xD800) (String. (char-array [(char 0xD800)]))
                   (.shiftLeft java.math.BigInteger/ONE 256)
                   (java.math.BigDecimal. java.math.BigInteger/ONE 65537)
                   (array-map :first (Object.) :second (with-meta [] {:x true}))
                   (array-map :first (with-meta [] {:x true}) :second (Object.))
                   (array-map :z (Object.) :a (with-meta [] {:x true}))]]
       (let [expected (failure-data (fn [] (old-record value)))
             actual (failure-data (fn [] (stream-record value)))]
         (is (some? expected))
         (is (= expected actual))))))

(deftest canonical-stream-first-budget-guards-match-and-accept-boundary
  (with-reader-limits
    #(let [value (array-map :z ["λ🙂" {:a "value"}] :a (list 1 2))
           stats (:stats (old-record value))]
       (doseq [[symbol key] [['p15-s23-c6c10-max-carrier-nodes :nodes]
                            ['p15-s23-c6c10-max-carrier-depth :maximum-depth]
                            ['p15-s23-c6c10-max-container-width :maximum-width]
                            ['p15-s23-c6c10-max-scalar-bytes :maximum-scalar-bytes]
                            ['p15-s23-c6c10-max-total-scalar-bytes :scalar-bytes]]]
         (let [var (ns-resolve 'gravity.bootstrap symbol) exact (get stats key)]
           (with-bindings {var exact} (check-compatible value))
           (with-bindings {var (dec exact)}
             (let [expected (first-failure (fn [] (old-record value)))
                   actual (first-failure (fn [] (stream-record value)))]
               (is (some? expected))
               (is (= expected actual)))))))))

(deftest canonical-stream-duplicate-key-check-remains-after-children
  (with-reader-limits
    #(with-redefs-fn
       {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-sort-key) (constantly "collision")
        (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-sort-key) (constantly "collision")}
       (fn []
         (doseq [[value missing] [[(array-map :a 1 :b 2) :unique-canonical-map-keys]
                                  [(array-map :a 1 :b (Object.)) :canonical-value-domain]
                                  [#{1 2} :unique-canonical-set-items]]]
           (let [expected (first-failure (fn [] (old-record value)))
                 actual (first-failure (fn [] (stream-record value)))]
             (is (= missing (:missing-fact expected)))
             (is (= expected actual))))))))

(deftest canonical-stream-never-normalizes-whole-sequences-or-map-values
  (with-reader-limits
    #(let [value {:tokens (vec (repeat 200 {:raw {:byte-start 0 :byte-end 1}
                                          :descriptor {:kind :symbol :name-codepoints [97 98]}}))}
           reference (old-record value)
           var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
           original @var]
       (with-redefs-fn
         {var (fn [path stats depth item]
                (when (or (vector? item) (map? item) (list? item))
                  (throw (ex-info "Whole container normalization tripwire" {:tripwire true})))
                (original path stats depth item))}
         (fn []
           (let [actual (stream-record value)]
             (is (= (:text reference) (:text actual)))
             (is (= (:stats reference) (:validation-stats actual) (:stats actual))))
           ;; Negative control proves the tripwire intercepts the old route.
           (is (:tripwire (failure-data (fn [] (old-record value))))))))))

(deftest canonical-stream-class-guards-and-metadata-precedence
  (doseq [[name value] [['p15-s23-c6c10-canonical-vector-classes [1]]
                       ['p15-s23-c6c10-canonical-map-classes {:a 1}]
                       ['p15-s23-c6c10-canonical-list-classes (list 1)]
                       ['p15-s23-c6c10-canonical-set-classes #{1}]]]
    (with-redefs-fn
      {(ns-resolve 'gravity.bootstrap name) #{}}
      (fn []
        (let [expected (first-failure #(old-record value))
              actual (first-failure #(stream-record value))]
          (is (some? expected))
          (is (= expected actual)))
        (let [value (with-meta value {:present true})
              expected (first-failure #(old-record value))
              actual (first-failure #(stream-record value))]
          (is (= :metadata-free-canonical-value (:missing-fact expected)))
          (is (= expected actual)))))))

(deftest canonical-stream-aliases-retain-full-physical-statistics
  (let [shared {:a "value"} value [shared shared]]
    (is (= 12 (get-in (stream-record value) [:stats :scalar-bytes])))
    (check-compatible value)
    (is (= (:form (old-record value))
           (bootstrap/sh03-reader-canonical-identity source-path value)))))

(deftest canonical-stream-emission-statistics-mutation-is-rejected
  (with-reader-limits
    #(let [var (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-validation!)
           original @var
           error (with-redefs-fn
                   {var (fn [path value] (update (original path value) :nodes inc))}
                   (fn [] (first-failure
                           (fn [] ((operation 'sh03-reader-canonical-stream-digest)
                                   source-path {:a [1 2]})))))]
       (is (= :sh03-canonical-stream-statistics-coherence (:missing-fact error))))))

(deftest canonical-stream-validates-before-opening-output-and-closes-writer
  (let [opened (atom 0)]
    (with-redefs [digest/sha256-output-hex (fn [_] (swap! opened inc) "unused")]
      (is (= :canonical-value-domain
             (:missing-fact (first-failure
                             (fn [] ((operation 'sh03-reader-canonical-stream-digest)
                                     source-path (Object.)))))))
      (is (zero? @opened))))
  (let [closed (atom 0)
        output (proxy [java.io.ByteArrayOutputStream] []
                 (close [] (swap! closed inc)))]
    (with-redefs [digest/sha256-output-hex
                  (fn [producer] (producer output) (digest/sha256-bytes-hex (.toByteArray output)))]
      (is (= (str "sha256:" (digest/sha256-hex (:text (old-record {:a "value"}))))
             ((operation 'sh03-reader-canonical-stream-digest) source-path {:a "value"}))))
    (is (= 1 @closed)))
  (let [primary (ex-info "Primary output failure" {})
        secondary (ex-info "Secondary close failure" {})
        closed (atom 0)
        output (proxy [java.io.ByteArrayOutputStream] []
                 (close [] (swap! closed inc) (throw secondary)))
        thrown (with-redefs-fn
                 {(ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-write!)
                  (fn [& _] (throw primary))
                  #'digest/sha256-output-hex (fn [producer] (producer output) "unreachable")}
                 (fn [] (try ((operation 'sh03-reader-canonical-stream-digest) source-path 1)
                             nil (catch Throwable error error))))]
    (is (identical? primary thrown))
    (is (= 1 @closed))
    (is (= [secondary] (vec (.getSuppressed primary))))))
