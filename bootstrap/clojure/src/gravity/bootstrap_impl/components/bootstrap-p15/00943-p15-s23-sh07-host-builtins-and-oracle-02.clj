
(defn- p15-s23-sh07-form-contains-symbol?
  [form wanted]
  (cond
    (= form wanted) true
    (map? form) (or (some #(p15-s23-sh07-form-contains-symbol? % wanted)
                          (keys form))
                    (some #(p15-s23-sh07-form-contains-symbol? % wanted)
                          (vals form)))
    (coll? form) (some #(p15-s23-sh07-form-contains-symbol? % wanted) form)
    :else false))

(defn- p15-s23-sh07-v2-caller-authority-authentic?
  [module definition callee caller authority]
  (try
    (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
          manifest (:p15-s23-sh07-link-manifest module)
          definition-hash (p15-s23-sh07-source-definition-hash definition)
          definition-row
          (first (filter #(= caller (:name %)) (:definitions descriptor)))
          builtin-rows
          (filter #(and (= caller (:caller %))
                        (= callee (:builtin %)))
                  (:builtin-calls descriptor))]
      (and (= p15-s23-sh07-compiler-source-authority-v2-keys
              (set (keys authority)))
           (= :gravity/sh07-compiler-source-authority-v2
              (:artifact authority))
           (p15-s23-sh07-b51-module-source-valid? descriptor)
           (map? manifest)
           (= (:manifest-id manifest)
              (p15-s23-sh07-b51-manifest-id manifest))
           (= (:module-source-id descriptor)
              (:module-source-id authority))
           (= (:manifest-id manifest) (:manifest-id authority))
           (= (:source-sha256 descriptor) (:source-sha256 authority))
           (= (:logical-source-path descriptor)
              (:logical-source-path authority))
           (= (:module descriptor) (:module authority) (:module module))
           (= (:module-source-id descriptor)
              (some #{(:module-source-id descriptor)}
                    (:module-source-ids manifest)))
           (= definition-hash
              (get-in authority [:function-source-hashes caller]))
           (= {:name caller
               :arity (:arity definition)
               :visibility (:visibility definition-row)
               :source-hash definition-hash}
              definition-row)
           (seq builtin-rows)
           (every?
            #(= {:caller caller
                 :caller-function-source-hash definition-hash
                 :builtin callee
                 :arity
                 (get p15-s23-sh07-declared-builtin-exact-arities callee)
                 :compiler-internal? true}
                %)
            builtin-rows)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-compiler-internal-caller-authorized?
  [module definition callee caller]
  (let [authority (:p15-s23-sh07-source-authority module)]
    (true?
     (and (map? authority)
       (or
        (and (= p15-s23-sh07-compiler-source-authority-v1-keys
                (set (keys authority)))
             (= :gravity/sh07-compiler-source-authority-v1
                (:artifact authority))
             (= p15-s23-sh07-checked-core-source-relative-path
                (:source-relative-path authority))
             (= 'gravity.checked-core (:module authority) (:module module)))
        (p15-s23-sh07-v2-caller-authority-authentic?
         module definition callee caller authority))
       (= (get-in authority [:function-source-hashes caller])
          (p15-s23-sh07-source-definition-hash definition))
       (= caller (:name definition))
       (p15-s23-sh07-form-contains-symbol? (:body definition) callee)
       (= :meta (:profile module))
       (= :jvm (:target module))
       (= #{} (:effects module))
       (= #{} (:capabilities module))
       (= :safe (:safety module))
       (symbol? caller)
       (or (= :gravity/sh07-compiler-source-authority-v1 (:artifact authority))
           (some (fn [row]
                   (and (= caller (:caller row))
                        (= callee (:builtin row))
                        (= (get p15-s23-sh07-declared-builtin-exact-arities callee)
                           (:arity row))
                        (= (p15-s23-sh07-source-definition-hash definition)
                           (:caller-function-source-hash row))
                        (true? (:compiler-internal? row))))
                 (get-in module [:p15-s23-sh07-module-source-descriptor
                                 :builtin-calls])))
       (contains? (get-in p15-s23-sh07-declared-builtin-contracts
                          [callee :callers]
                          #{})
                  caller)))))

(defn p15-s23-sh07-compiler-internal-provenance
  [module definition callee caller]
  (when-not (p15-s23-sh07-compiler-internal-caller-authorized?
             module definition callee caller)
    (throw (ex-info "compiler-internal builtin source is unauthorized"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :function callee :caller caller})))
  (let [authority (:p15-s23-sh07-source-authority module)]
    (if (= :gravity/sh07-compiler-source-authority-v1 (:artifact authority))
      {:artifact :gravity/sh07-compiler-internal-call-provenance-v1
       :source-relative-path (:source-relative-path authority)
       :source-sha256 (:source-sha256 authority)
       :function caller
       :function-source-hash
       (get-in authority [:function-source-hashes caller])
       :callee callee}
      {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
       :manifest-id (:manifest-id authority)
       :module-source-id (:module-source-id authority)
       :source-sha256 (:source-sha256 authority)
       :function caller
       :function-source-hash
       (get-in authority [:function-source-hashes caller])
       :callee callee})))
(defn- p15-s23-sh07-next-scalar
  [^String text index]
  (let [length (.length text)
        unit (int (.charAt text index))]
    (cond
      (<= 0xd800 unit 0xdbff)
      (if (< (inc index) length)
        (let [low (int (.charAt text (inc index)))]
          (if (<= 0xdc00 low 0xdfff)
            [(+ 0x10000
                (bit-shift-left (- unit 0xd800) 10)
                (- low 0xdc00))
             (+ index 2)]
            (throw (IllegalArgumentException.
                    "SH-07 comparator received malformed Unicode"))))
        (throw (IllegalArgumentException.
                "SH-07 comparator received malformed Unicode")))

      (<= 0xdc00 unit 0xdfff)
      (throw (IllegalArgumentException.
              "SH-07 comparator received malformed Unicode"))

      :else [unit (inc index)])))

(defn p15-s23-sh07-canonical-text-compare
  [left right]
  (when-not (and (string? left) (string? right))
    (throw (IllegalArgumentException.
            "sh07-canonical-text-compare requires two strings")))
  ;; Strict UTF-8 preserves Unicode-scalar order.  Compare scalar values
  ;; directly so this ordering-only primitive never counts, limits, caches,
  ;; allocates, or corroborates bytes; G20 must already have admitted text.
  (loop [left-index 0 right-index 0]
    (cond
      (= left-index (.length ^String left))
      (if (= right-index (.length ^String right)) 0 -1)

      (= right-index (.length ^String right)) 1

      :else
      (let [[left-scalar next-left]
            (p15-s23-sh07-next-scalar left left-index)
            [right-scalar next-right]
            (p15-s23-sh07-next-scalar right right-index)]
        (cond
          (< left-scalar right-scalar) -1
          (> left-scalar right-scalar) 1
          :else (recur next-left next-right))))))

(defn p15-s23-sh07-observe-utf8-byte-count
  [text observed-before maximum]
  (when-not
   (and (string? text)
        (integer? observed-before)
        (integer? maximum)
        (<= 0 observed-before maximum 1073741824))
    (throw
     (IllegalArgumentException.
      "sh07-observe-utf8-byte-count requires a string and governed counters")))
  (let [length (.length ^String text)
        record
        (fn [status reason observed-after prospective units scalars]
          {:artifact :gravity/sh07-utf8-byte-observation-v1
           :status status
           :reason reason
           :maximum maximum
           :observed-before observed-before
           :observed-after observed-after
           :prospective-after prospective
           :utf16-units-scanned units
           :unicode-scalars-scanned scalars})]
    (loop [index 0 observed-after observed-before scalars 0]
      (if (= index length)
        (record :accepted nil observed-after observed-after index scalars)
        (let [unit (int (.charAt ^String text index))]
          (cond
            (<= 0xd800 unit 0xdbff)
            (if (and (< (inc index) length)
                     (let [low (int (.charAt ^String text (inc index)))]
                       (<= 0xdc00 low 0xdfff)))
              (let [width 4
                    remaining (- maximum observed-after)
                    prospective (+ observed-after width)]
                (if (<= width remaining)
                  (recur (+ index 2) prospective (inc scalars))
                  (record :rejected :utf8-byte-limit observed-after
                          prospective index scalars)))
              (record :rejected :invalid-unicode-scalar-sequence
                      observed-after nil index scalars))

            (<= 0xdc00 unit 0xdfff)
            (record :rejected :invalid-unicode-scalar-sequence
                    observed-after nil index scalars)

            :else
            (let [width (cond (<= unit 0x7f) 1
                              (<= unit 0x7ff) 2
                              :else 3)
                  remaining (- maximum observed-after)
                  prospective (+ observed-after width)]
              (if (<= width remaining)
                (recur (inc index) prospective (inc scalars))
                (record :rejected :utf8-byte-limit observed-after
                        prospective index scalars)))))))))

;; G19 keeps decimal text out of the generic seed printer.  These two closures
;; consume only the authenticated four-key C2 semantic value.  They are
;; deliberately duplicated: the C11/producer model and the source oracle must
;; not share decimal grammar, arithmetic, inversion, or rendering authority.
(defn- p15-s23-sh07-p-decimal-digits? [digits]
  (and (vector? digits) (seq digits)
       (every? #(and (integer? %) (<= 48 % 57)) digits)))
