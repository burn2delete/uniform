(defn- sh03-reader-canonical-stream-scalar-value? [value]
  (or (nil? value) (boolean? value) (integer? value) (string? value)
      (char? value) (keyword? value) (symbol? value)))

;; Private per-pass counters: nodes, maximum depth, maximum width, total scalar
;; bytes, maximum scalar bytes, maximum integer bits. Fresh Object[] counters
;; preserve original inc/+/max numeric types, overflow and partial-error state.
;; Invoke their Vars as original update does: compiler-inline primitive max
;; would coerce an Integer bit count to Long even inside an Object[] counter.
(defn- sh03-reader-canonical-stats-map [^objects stats]
  {:nodes (aget stats 0) :maximum-depth (aget stats 1)
   :maximum-width (aget stats 2) :scalar-bytes (aget stats 3)
   :maximum-scalar-bytes (aget stats 4) :maximum-integer-bits (aget stats 5)})

(defn- sh03-reader-canonical-copy-stats! [^objects stats snapshot]
  (aset stats 0 (:nodes snapshot))
  (aset stats 1 (:maximum-depth snapshot))
  (aset stats 2 (:maximum-width snapshot))
  (aset stats 3 (:scalar-bytes snapshot))
  (aset stats 4 (:maximum-scalar-bytes snapshot))
  (aset stats 5 (:maximum-integer-bits snapshot)))

(defn- sh03-reader-canonical-fallback-form! [source-path ^objects stats depth value]
  ;; Fresh invocation-local atom, original depth, no root predebit or allowance
  ;; reset. Copy all partial fields on either outcome, without masking a primary
  ;; Throwable if copying back itself fails. No retained fallback state/cache.
  (let [legacy (atom (sh03-reader-canonical-stats-map stats))
        form (try
               (p15-s23-c6c10-canonical-form* source-path legacy depth value)
               (catch Throwable primary
                 (try (sh03-reader-canonical-copy-stats! stats @legacy)
                      (catch Throwable secondary
                        (when-not (identical? primary secondary)
                          (try (.addSuppressed primary secondary) (catch Throwable _ nil)))))
                 (throw primary)))]
    (sh03-reader-canonical-copy-stats! stats @legacy)
    form))

(defn- sh03-reader-canonical-visit! [source-path ^objects stats depth value]
  (aset stats 0 (#'clojure.core/inc (aget stats 0)))
  (aset stats 1 (#'clojure.core/max (aget stats 1) depth))
  (when (or (> (aget stats 0) p15-s23-c6c10-max-carrier-nodes)
            (> depth p15-s23-c6c10-max-carrier-depth))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :bounded-canonical-carrier
     {:nodes (aget stats 0) :maximum-depth (aget stats 1)}))
  (when (and (instance? clojure.lang.IObj value) (some? (meta value)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :metadata-free-canonical-value
     {:value-kind (some-> value class .getName)})))

(defn- sh03-reader-canonical-named-component? [value]
  ;; Same UTF-16 code-unit predicates as the old blank?/not-any? composition.
  ;; Empty fails; any whitespace/control/slash fails. Other units, including
  ;; unpaired surrogates, proceed to the separate original Unicode guard.
  (and (string? value) (pos? (.length ^String value))
       (loop [index 0]
         (if (= index (.length ^String value)) true
           (let [unit (.charAt ^String value index)]
             (if (or (Character/isWhitespace unit) (Character/isISOControl unit) (= \/ unit))
               false (recur (inc index))))))))

(defn- sh03-reader-canonical-utf8-length [^String value]
  ;; Called only after the unchanged length and complete Unicode guards.
  ;; Valid surrogate pairs consume two units. Match alength's boxed Integer.
  (loop [index 0 bytes 0]
    (if (= index (.length value)) (int bytes)
      (let [unit (int (.charAt value index))]
        (cond
          (<= unit 0x7f) (recur (inc index) (+ bytes 1))
          (<= unit 0x7ff) (recur (inc index) (+ bytes 2))
          (<= 0xd800 unit 0xdbff) (recur (+ index 2) (+ bytes 4))
          :else (recur (inc index) (+ bytes 3)))))))

(defn- sh03-reader-canonical-string! [source-path ^objects stats ^String value]
  (when (> (.length value) p15-s23-c6c10-max-scalar-bytes)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :maximum-scalar-characters
     {:observed-scalar-characters (.length value)
      :maximum-scalar-characters p15-s23-c6c10-max-scalar-bytes}))
  (when-not (p15-s23-c6c10-valid-unicode-string? value)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :well-formed-unicode-scalar-string {:value-kind :string}))
  (let [byte-count (sh03-reader-canonical-utf8-length value)]
    (when (> byte-count p15-s23-c6c10-max-scalar-bytes)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-scalar-bytes
       {:observed-scalar-bytes byte-count
        :maximum-scalar-bytes p15-s23-c6c10-max-scalar-bytes}))
    (aset stats 3 (#'clojure.core/+ (aget stats 3) byte-count))
    (aset stats 4 (#'clojure.core/max (aget stats 4) byte-count))
    (when (> (aget stats 3) p15-s23-c6c10-max-total-scalar-bytes)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-total-scalar-bytes
       {:observed-total-scalar-bytes (aget stats 3)}))))

(defn- sh03-reader-canonical-native-integer? [value]
  ;; These four classes are final; custom Number/BigInteger implementations
  ;; retain the original conversion and virtual method behavior.
  (or (instance? Long value) (instance? Integer value)
      (instance? Short value) (instance? Byte value)))

(defn- sh03-reader-canonical-integer-bits [value]
  (if (sh03-reader-canonical-native-integer? value)
    ;; abs(Long/MIN_VALUE) stays negative, yielding 64 bits as BigInteger.abs.
    (int (- 64 (Long/numberOfLeadingZeros (Math/abs (.longValue ^Number value)))))
    (.bitLength (.abs (biginteger value)))))

(defn- sh03-reader-canonical-integer-text [value]
  (if (sh03-reader-canonical-native-integer? value)
    (.toString ^Object value)
    (.toString (biginteger value))))

(defn- sh03-reader-canonical-integer! [source-path ^objects stats value]
  (let [bits (sh03-reader-canonical-integer-bits value)]
    (aset stats 5 (#'clojure.core/max (aget stats 5) bits))
    (when (> bits p15-s23-c6c10-max-integer-bits)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-integer-bits
       {:observed-integer-bits bits :maximum-integer-bits p15-s23-c6c10-max-integer-bits}))))

;; Validation-only equivalent of the common scalar cases in original 00703.
;; No tagged output vectors or integer decimal strings on this common V path.
(defn- sh03-reader-canonical-validate-scalar! [source-path ^objects stats depth value]
  (if-not (sh03-reader-canonical-stream-scalar-value? value)
    (do (sh03-reader-canonical-fallback-form! source-path stats depth value) nil)
    (do
      (sh03-reader-canonical-visit! source-path stats depth value)
      (cond
        (or (nil? value) (boolean? value)) nil

        (integer? value)
        (sh03-reader-canonical-integer! source-path stats value)

        (string? value)
        (sh03-reader-canonical-string! source-path stats value)

        (char? value)
        (let [code (int value)]
          (when (<= 0xD800 code 0xDFFF)
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :unicode-scalar-character {:code code})))

        (keyword? value)
        (let [namespace (namespace value)
              name (name value)]
          (when-not (and (sh03-reader-canonical-named-component? name)
                         (or (nil? namespace)
                             (sh03-reader-canonical-named-component? namespace)))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-keyword-components
             {:namespace namespace :name name}))
          (when namespace
            (sh03-reader-canonical-string! source-path stats namespace))
          (sh03-reader-canonical-string! source-path stats name))

        (symbol? value)
        (let [namespace (namespace value)
              name (name value)]
          (when-not (or (and (nil? namespace) (= "/" name))
                        (and (sh03-reader-canonical-named-component? name)
                             (or (nil? namespace)
                                 (sh03-reader-canonical-named-component? namespace))))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-symbol-components
             {:namespace namespace :name name}))
          (when namespace
            (sh03-reader-canonical-string! source-path stats namespace))
          (sh03-reader-canonical-string! source-path stats name)))
      nil)))

(defn- sh03-reader-canonical-key-form! [source-path ^objects stats depth value]
  (if-not (sh03-reader-canonical-stream-scalar-value? value)
    (sh03-reader-canonical-fallback-form! source-path stats depth value)
    (do
      ;; Every key occurrence pays full fresh validation before its exact old
      ;; tagged representation is constructed. Values use the nil-return V path.
      (sh03-reader-canonical-validate-scalar! source-path stats depth value)
      (cond
        (nil? value) [:nil]
        (boolean? value) [:boolean value]
        (integer? value) [:integer (sh03-reader-canonical-integer-text value)]
        (string? value) [:string value]
        (char? value) [:character (int value)]
        (keyword? value) [:keyword (namespace value) (name value)]
        (symbol? value) [:symbol (namespace value) (name value)]))))
