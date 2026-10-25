(defn- sh03-reader-canonical-stream-scalar-value? [value]
  (or (nil? value) (boolean? value) (integer? value) (string? value)
      (char? value) (keyword? value) (symbol? value)))

;; Validation-only equivalent of the common scalar cases in original 00703.
;; Same mutable statistics, guard helpers, order and domain. No tagged output
;; vectors or integer decimal strings are constructed by the common path.
;; Other domains retain the existing normalizer, including its own root visit.
(defn- sh03-reader-canonical-validate-scalar! [source-path stats depth value]
  (if-not (sh03-reader-canonical-stream-scalar-value? value)
    (do (p15-s23-c6c10-canonical-form* source-path stats depth value) nil)
    (do
      (swap! stats update :nodes inc)
      (swap! stats update :maximum-depth max depth)
      (when (or (> (:nodes @stats) p15-s23-c6c10-max-carrier-nodes)
                (> depth p15-s23-c6c10-max-carrier-depth))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :bounded-canonical-carrier
         (select-keys @stats [:nodes :maximum-depth])))
      (when (and (instance? clojure.lang.IObj value) (some? (meta value)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :metadata-free-canonical-value
         {:value-kind (some-> value class .getName)}))
      (cond
        (or (nil? value) (boolean? value)) nil

        (integer? value)
        (p15-s23-c6c10-bounded-integer! source-path stats value)

        (string? value)
        (p15-s23-c6c10-bounded-string-bytes! source-path stats value)

        (char? value)
        (let [code (int value)]
          (when (<= 0xD800 code 0xDFFF)
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :unicode-scalar-character {:code code})))

        (keyword? value)
        (let [namespace (namespace value)
              name (name value)]
          (when-not (and (p15-s23-c6c10-valid-named-component? name)
                         (or (nil? namespace)
                             (p15-s23-c6c10-valid-named-component? namespace)))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-keyword-components
             {:namespace namespace :name name}))
          (when namespace
            (p15-s23-c6c10-bounded-string-bytes! source-path stats namespace))
          (p15-s23-c6c10-bounded-string-bytes! source-path stats name))

        (symbol? value)
        (let [namespace (namespace value)
              name (name value)]
          (when-not (or (and (nil? namespace) (= "/" name))
                        (and (p15-s23-c6c10-valid-named-component? name)
                             (or (nil? namespace)
                                 (p15-s23-c6c10-valid-named-component? namespace))))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :canonical-symbol-components
             {:namespace namespace :name name}))
          (when namespace
            (p15-s23-c6c10-bounded-string-bytes! source-path stats namespace))
          (p15-s23-c6c10-bounded-string-bytes! source-path stats name)))
      nil)))
