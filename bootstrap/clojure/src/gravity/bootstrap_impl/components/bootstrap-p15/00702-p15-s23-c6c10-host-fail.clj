

(defn p15-s23-c6c10-host-fail!
  [rule source-path missing-fact subject]
  (let [subject (if (map? subject) subject {})
        contract (p15-s23-c6c10-upstream-diagnostic-contract rule)
        span (or (:source-span subject)
                 (:span subject)
                 {:source (or source-path "<c6-c10>")})
        generated-origin-chain
        (let [origin (or (:generated-origin-chain subject)
                         (:generated-origin subject))]
          (if (vector? origin) origin []))
        semantic-identities
        (into {}
              (keep (fn [key]
                      (when-let [identity
                                 (p15-s23-c6c10-diagnostic-semantic-id
                                  (get subject key))]
                        [key identity])))
              [:syntax-id :core-node-id :operation-id :origin-id])
        stable-input
        {:domain :gravity/c6-c10-upstream-diagnostic-id-v1
         :rule rule
         :stage (:stage contract)
         :primary-span
         (p15-s23-c6c10-diagnostic-semantic-span span)
         :semantic-identities semantic-identities
         :missing-fact
         (if (keyword? missing-fact) missing-fact :invalid-missing-fact)}
        data
        (p15-s23-c6c10-owned-upstream-data
         (merge
          {:id rule
          :rule rule
          :diagnostic-id
          (p15-s23-c6c10-canonical-digest
           (or source-path "<c6-c10>") stable-input)
          :bootstrap-stage :stage0
          :stage (:stage contract)
          :diagnostic-family (:family contract)
          :document-id (:document-id contract)
          :expected-document (:expected-document contract)
          :source-span span
          :profile (if (keyword? (:profile subject))
                     (:profile subject) :hosted)
          :target (let [candidate (or (:requested-target subject)
                                      (:target subject))]
                    (if (keyword? candidate) candidate :jvm))
          :missing-fact missing-fact
          :facts (assoc subject :missing-fact missing-fact)
          :subject subject
          :generated-origin-chain generated-origin-chain
          :remediation
          "Regenerate the bounded product from fresh C2/C3 products, the pinned stage2 plan, and the pinned Gravity C6-C10 module."}
          semantic-identities))]
    (throw
     (ex-info
      (str rule " at bounded Gravity C6-C10 bridge")
      data))))

(defn p15-s23-c6c10-valid-unicode-string?
  [^String value]
  (loop [index 0]
    (if (= index (.length value))
      true
      (let [code (int (.charAt value index))]
        (cond
          (<= 0xD800 code 0xDBFF)
          (and (< (inc index) (.length value))
               (let [low (int (.charAt value (inc index)))]
                 (and (<= 0xDC00 low 0xDFFF)
                      (recur (+ index 2)))))

          (<= 0xDC00 code 0xDFFF) false
          :else (recur (inc index)))))))

(defn- p15-s23-c6c10-require-unicode-string!
  [source-path value]
  (when-not (p15-s23-c6c10-valid-unicode-string? value)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :well-formed-unicode-scalar-string
     {:value-kind :string})))

(declare p15-s23-c6c10-canonical-form*)

(def ^:private p15-s23-c6c10-lowercase-hex-digits
  "0123456789abcdef")

(defn- p15-s23-c6c10-lowercase-hex
  [^bytes bytes]
  (let [byte-count (alength bytes)
        characters (char-array (* 2 byte-count))]
    (loop [byte-index 0]
      (if (= byte-index byte-count)
        (String. characters)
        (let [value (bit-and (aget bytes byte-index) 0xff)
              character-index (* 2 byte-index)]
          (aset-char
           characters character-index
           (.charAt ^String p15-s23-c6c10-lowercase-hex-digits
                    (unsigned-bit-shift-right value 4)))
          (aset-char
           characters (inc character-index)
           (.charAt ^String p15-s23-c6c10-lowercase-hex-digits
                    (bit-and value 0x0f)))
          (recur (inc byte-index)))))))

(defn p15-s23-c6c10-canonical-sort-key
  [form]
  (let [text
        (binding [*print-length* nil
                  *print-level* nil
                  *print-meta* false
                  *print-dup* false
                  *print-readably* true
                  *print-namespace-maps* false]
          (pr-str [:gravity/canonical-sort-v1 form]))]
    (p15-s23-c6c10-lowercase-hex
     (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))))

(defn p15-s23-c6c10-valid-named-component?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (not-any? #(or (Character/isWhitespace ^Character %)
                      (Character/isISOControl ^Character %)
                      (= \/ %))
                 value)))

(defn- p15-s23-c6c10-native-vector-backing?
  [value]
  (let [seen (java.util.IdentityHashMap.)]
    (loop [value value]
      (cond
        (identical? clojure.lang.PersistentVector (class value)) true
        (not (identical? clojure.lang.APersistentVector$SubVector (class value))) false
        (.containsKey seen value) false
        :else
        (let [^clojure.lang.APersistentVector$SubVector view value
              backing (.-v view)]
          (.put seen value true)
          (and (<= 0 (.-start view) (.-end view))
               (or (identical? clojure.lang.PersistentVector (class backing))
                   (identical? clojure.lang.APersistentVector$SubVector (class backing)))
               (<= (.-end view) (.count ^clojure.lang.Counted backing))
               (recur backing)))))))

(defn- p15-s23-c6c10-canonical-list-items
  [source-path value]
  (let [seen (java.util.IdentityHashMap.)]
    (loop [cursor value items []]
      (let [kind (class cursor) class-name (some-> kind .getName)]
        (cond
          (or (nil? cursor) (identical? clojure.lang.PersistentList$EmptyList kind)) items
          (not (and (contains? p15-s23-c6c10-canonical-list-classes class-name)
                    (or (identical? clojure.lang.PersistentList kind)
                        (identical? clojure.lang.Cons kind))))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path :exact-persistent-list-class {:class class-name})
          (some? (.meta ^clojure.lang.IObj cursor))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path :metadata-free-canonical-value {:value-kind class-name})
          (.containsKey seen cursor)
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path :cyclic-canonical-carrier {})
          :else
          (do (.put seen cursor true)
              (recur (if (identical? clojure.lang.Cons kind)
                       (.more ^clojure.lang.Cons cursor)
                       (.next ^clojure.lang.PersistentList cursor))
                     (conj items (.first ^clojure.lang.ISeq cursor)))))))))

(defn- p15-s23-c6c10-canonical-carrier
  [source-path value]
  (let [class-name (some-> value class .getName)
        [native-kind metadata? comparator] (p15-s23-host-carrier-header value)
        [kind allowed? missing]
        (cond
          (record? value) [nil false :record-free-canonical-value]
          (vector? value) [:vector (and (contains? p15-s23-c6c10-canonical-vector-classes class-name)
                                       (p15-s23-c6c10-native-vector-backing? value))
                           :exact-persistent-vector-class]
          (map? value) [:map (contains? p15-s23-c6c10-canonical-map-classes class-name)
                       :exact-persistent-map-class]
          (set? value) [:set (contains? p15-s23-c6c10-canonical-set-classes class-name)
                       :exact-persistent-set-class]
          (list? value) [:list (contains? p15-s23-c6c10-canonical-list-classes class-name)
                        :exact-persistent-list-class]
          :else [nil true :canonical-value-domain])]
    (when (or (not allowed?) (= :unsupported native-kind))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path missing {:class class-name}))
    (when metadata?
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :metadata-free-canonical-value {:value-kind class-name}))
    (when (and (contains? #{:tree-map :tree-set} native-kind)
               (not= :default-comparator comparator))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :default-native-comparator {:class class-name}))
    {:kind kind
     :children (case kind
                 :map (reduce-kv (fn [items key child] (conj items key child)) [] value)
                 :vector (into [] value)
                 :set (into [] value)
                 :list (p15-s23-c6c10-canonical-list-items source-path value)
                 nil)}))

;; Fixed contract DATA selection only; no source execution or admission.
(require '[gravity.c6-c10.source-contract :as c6-source-contract])

(defn p15-s23-c6c10-source-program-contract [version]
  (when-not (identical? Long (class version))
    (p15-s23-c6c10-host-fail! "C6-VERIFY" "<gravity-checked-core>"
                             :explicit-c6-source-contract-version {}))
  (case (.longValue ^Long version)
    1 {:version 1
       :source-byte-count p15-s23-c6c10-source-byte-count
       :source-content-hash p15-s23-c6c10-expected-source-content-hash
       :plan-semantic-hash p15-s23-c6c10-expected-plan-semantic-hash
       :functions-semantic-hash p15-s23-c6c10-expected-functions-semantic-hash
       :builder-semantic-hash p15-s23-c6c10-expected-builder-semantic-hash
       :verifier-semantic-hash p15-s23-c6c10-expected-verifier-semantic-hash
       :required-functions p15-s23-c6c10-required-functions
       :function-count 139 :chunk-size 64}
    2 c6-source-contract/current-compiler-program
    3 c6-source-contract/flat-bool-compiler-program
    (p15-s23-c6c10-host-fail! "C6-VERIFY" "<gravity-checked-core>"
                             :explicit-c6-source-contract-version {})))
