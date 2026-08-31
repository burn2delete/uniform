

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
          (if (and (vector? origin) (<= (count origin) 64)) origin []))
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

(defn p15-s23-c6c10-bounded-string-bytes!
  [source-path stats value]
  (when (> (.length ^String value) p15-s23-c6c10-max-scalar-bytes)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :maximum-scalar-characters
     {:observed-scalar-characters (.length ^String value)
      :maximum-scalar-characters p15-s23-c6c10-max-scalar-bytes}))
  (when-not (p15-s23-c6c10-valid-unicode-string? value)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" source-path :well-formed-unicode-scalar-string
     {:value-kind :string}))
  (let [byte-count (utf8-byte-count value)]
    (when (> byte-count p15-s23-c6c10-max-scalar-bytes)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-scalar-bytes
       {:observed-scalar-bytes byte-count
        :maximum-scalar-bytes p15-s23-c6c10-max-scalar-bytes}))
    (swap! stats update :scalar-bytes + byte-count)
    (swap! stats update :maximum-scalar-bytes max byte-count)
    (when (> (:scalar-bytes @stats)
             p15-s23-c6c10-max-total-scalar-bytes)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-total-scalar-bytes
       {:observed-total-scalar-bytes (:scalar-bytes @stats)}))))

(defn p15-s23-c6c10-bounded-integer!
  [source-path stats value]
  (let [bits (.bitLength (.abs (biginteger value)))]
    (swap! stats update :maximum-integer-bits max bits)
    (when (> bits p15-s23-c6c10-max-integer-bits)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :maximum-integer-bits
       {:observed-integer-bits bits
        :maximum-integer-bits p15-s23-c6c10-max-integer-bits}))))

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

(defn p15-s23-c6c10-canonical-sequence
  [source-path stats depth values]
  (mapv #(p15-s23-c6c10-canonical-form*
          source-path stats (inc depth) %)
        values))