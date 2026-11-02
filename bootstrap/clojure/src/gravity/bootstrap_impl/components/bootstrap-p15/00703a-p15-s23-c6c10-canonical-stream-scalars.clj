(defn- p15-s23-c6c10-streaming-scalar-value? [value]
  (or (nil? value) (boolean? value) (integer? value) (string? value)
      (char? value) (keyword? value) (symbol? value)))

(defn- p15-s23-c6c10-streaming-fallback-form! [source-path value]
  (p15-s23-c6c10-canonical-form* source-path value))

(defn- p15-s23-c6c10-streaming-metadata! [source-path value]
  ;; Reuse the owning native/class/backing/comparator/metadata checks. This
  ;; projects immediate children only; it does not normalize nested values.
  (p15-s23-c6c10-canonical-carrier source-path value)
  nil)

(defn- p15-s23-c6c10-streaming-named-component? [value]
  (and (string? value) (pos? (.length ^String value))
       (loop [index 0]
         (if (= index (.length ^String value)) true
           (let [unit (.charAt ^String value index)]
             (if (or (Character/isWhitespace unit) (Character/isISOControl unit) (= \/ unit))
               false (recur (inc index))))))))

(defn- p15-s23-c6c10-streaming-string! [source-path value]
  (p15-s23-c6c10-require-unicode-string! source-path value))

(defn- p15-s23-c6c10-streaming-native-integer? [value]
  (or (instance? Long value) (instance? Integer value)
      (instance? Short value) (instance? Byte value)))

(defn- p15-s23-c6c10-streaming-integer-text [value]
  (if (p15-s23-c6c10-streaming-native-integer? value)
    (.toString ^Object value)
    (.toString (biginteger value))))

(defn- p15-s23-c6c10-streaming-validate-scalar! [source-path value]
  (if-not (p15-s23-c6c10-streaming-scalar-value? value)
    (do (p15-s23-c6c10-streaming-fallback-form! source-path value) nil)
    (do
      (p15-s23-c6c10-streaming-metadata! source-path value)
      (cond
        (or (nil? value) (boolean? value) (integer? value)) nil
        (string? value) (p15-s23-c6c10-streaming-string! source-path value)
        (char? value)
        (let [code (int value)]
          (when (<= 0xD800 code 0xDFFF)
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :unicode-scalar-character {:code code})))
        (keyword? value)
        (let [namespace (namespace value) name (name value)]
          (when-not (and (p15-s23-c6c10-streaming-named-component? name)
                         (or (nil? namespace)
                             (p15-s23-c6c10-streaming-named-component? namespace)))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-keyword-components
             {:namespace namespace :name name}))
          (when namespace (p15-s23-c6c10-streaming-string! source-path namespace))
          (p15-s23-c6c10-streaming-string! source-path name))
        (symbol? value)
        (let [namespace (namespace value) name (name value)]
          (when-not (or (and (nil? namespace) (= "/" name))
                        (and (p15-s23-c6c10-streaming-named-component? name)
                             (or (nil? namespace)
                                 (p15-s23-c6c10-streaming-named-component? namespace))))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-symbol-components
             {:namespace namespace :name name}))
          (when namespace (p15-s23-c6c10-streaming-string! source-path namespace))
          (p15-s23-c6c10-streaming-string! source-path name)))
      nil)))

(defn- p15-s23-c6c10-streaming-key-form! [source-path value]
  (if-not (p15-s23-c6c10-streaming-scalar-value? value)
    (p15-s23-c6c10-streaming-fallback-form! source-path value)
    (do
      (p15-s23-c6c10-streaming-validate-scalar! source-path value)
      (cond
        (nil? value) [:nil]
        (boolean? value) [:boolean value]
        (integer? value) [:integer (p15-s23-c6c10-streaming-integer-text value)]
        (string? value) [:string value]
        (char? value) [:character (int value)]
        (keyword? value) [:keyword (namespace value) (name value)]
        (symbol? value) [:symbol (namespace value) (name value)]))))
