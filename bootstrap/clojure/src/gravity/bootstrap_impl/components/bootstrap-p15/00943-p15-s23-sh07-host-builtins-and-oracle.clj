;; The load manifest places SH-07 before the late compiler-artifact components.
;; Define the private dynamic seam here so every later component resolves the
;; same Var.  Only the arity-two wrapper in 00945 ever installs a non-nil root.
(def ^:dynamic ^:private *p15-s23-sh07-b51-governed-source-session* nil)

(declare ^:private p15-s23-sh07-b51-governed-source-event!)
(declare ^:private p15-s23-sh07-b51-governed-source-entry)
(declare ^:private p15-s23-sh07-b51-active-module-ordinal)
(declare ^:private p15-s23-sh07-b51-compatibility-source-record)
(declare compiler-c2-reader-source-artifact)
(declare compiler-c3-syntax-source-artifact)
(declare ^:private sh03-reader-internal-product-authority)
(declare ^:private sh05-macro-artifact-from-c3-artifact*)
(declare ^:private sh06-resolution-source-artifact-candidate)
(declare ^:private sh06-resolution-finalize-candidate)
(declare sh07-core-authenticated-request)
(declare sh07-core-execute!)
(declare sh07-core-digest-requests)
(declare sh07-core-plan-identities)
(declare sh07-core-expected-source-byte-count)
(declare sh07-core-expected-source-content-hash)
(declare sh07-core-expected-plan-semantic-hash)
(declare sh07-core-expected-functions-semantic-hash)
(declare sh07-core-expected-function-count)
(declare sh07-core-expected-function-names-hash)
(declare sh07-core-expected-function-shapes-hash)
(declare sh07-core-public-function-hashes)
(declare sh07-core-public-function-shapes)
(declare p15-s23-stage2-compiler-artifact-plan)
(declare p15-s23-stage2-compiler-artifact-binding!)

(def p15-s23-sh07-declared-builtin-exact-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def p15-s23-sh07-canonical-text-compare-direct-callers
  '#{sh07-b51-producer-insert-decoration
     sh07-b51-r6-decoration-insert
     p15-s23-sh07-canonical-decoration})

(def p15-s23-sh07-declared-digest-hash-direct-callers
  '#{sh07-b51-success-digest-replay-exact?
     sh07-b51-finalize-rejection
     sh07-b51-producer-decimal-admission
     sh07-b51-producer-decimal-projection-authentic?
     sh07-b51-r6-replay-digests
     sh07-b51-r6-decimal-projection-authentic?
     sh07-b51-r6-predecessor-closure-authentic?
     sh07-b51-r6-projection-binding
     sh07-b51-r6-projected-form-id
     sh07-b51-r6-projected-syntax-id
     sh07-b51-r6-project-decimal
     sh07-b51-r6-projected-macro-traces
     sh07-b51-r6-project-lineage
     sh07-b51-r6-replay-consume-scope
     sh07-b51-r6-replay-reference
     sh07-b51-r6-project-forms
     sh07-b51-r6-project-definition-syntax-id
     sh07-b51-r6-project-scope-catalog
     sh07-b51-r6-project-bindings
     sh07-b51-r6-v18-executable-authentic?})

(def p15-s23-sh07-observe-utf8-byte-count-direct-callers
  '#{sh07-b51-producer-observe-decoration-bytes
     sh07-b51-r6-observe-scalar-bytes
     p15-s23-sh07-o-append-scalar-fragment!
     p15-s23-sh07-combine-decorations
     p15-s23-sh07-o-c2-decoration})

(def p15-s23-sh07-admitted-carrier-validation-direct-callers
  '#{sh07-b51-root1-admitted-raw-carrier-shape?
     sh07-b51-r6-c30-request-proof?})

(def p15-s23-sh07-declared-builtin-contracts
  {'sh07-canonical-text-compare
   {:arity 2
    :input [:string :string]
    :result [:enum -1 0 1]
    :effects #{}
    :capabilities #{}
    :order :unsigned-lexicographic-strict-utf8
    :resource-authority :none
    :byte-census-authority :none
    :malformed-unicode-diagnostic "L2-BUILTIN-ERROR"
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :callers p15-s23-sh07-canonical-text-compare-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-declared-digest-hash
   {:arity 1
    :input [:reader-canonical-value]
    :result :sha256-digest-id
    :effects #{}
    :capabilities #{}
    :identity :reader-canonical-hash
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :callers p15-s23-sh07-declared-digest-hash-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-observe-utf8-byte-count
   {:arity 3
    :implementation :p15-s23-sh07-observe-utf8-byte-count-v1
    :input [:string :nonnegative-exact-integer
            :nonnegative-exact-integer]
    :result
    {:artifact :gravity/sh07-utf8-byte-observation-v1
     :keys #{:artifact :status :reason :maximum :observed-before
             :observed-after :prospective-after :utf16-units-scanned
             :unicode-scalars-scanned}
     :status [:enum :accepted :rejected]
     :reasons #{nil :utf8-byte-limit
                :invalid-unicode-scalar-sequence}}
    :effects #{}
    :capabilities #{}
    :maximum 1073741824
    :maximum-prospective-after 1073741828
    :algorithm :strict-unicode-scalar-utf8-prospective-census
    :encoded-allocation :forbidden
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :wrong-input-diagnostic "L2-BUILTIN-ERROR"
    :callers p15-s23-sh07-observe-utf8-byte-count-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-b51-admitted-executable-carrier-validation?
   {:arity 1
    :implementation :p15-s23-sh07-b51-first-eval-validation-receipt-v3
    :input [:identity-bound-executable-carrier]
    :result [:literal true]
    :effects #{}
    :capabilities #{}
    :fact-projections
    #{:gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v3
      :gravity/sh07-b51-attempt32-r6-nested-carrier-facts-v3}
    :one-shot true
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :wrong-input-diagnostic "L2-BUILTIN-ERROR"
    :callers p15-s23-sh07-admitted-carrier-validation-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}})

(def ^:private p15-s23-sh07-checked-core-source-relative-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")

(def ^:private p15-s23-sh07-compiler-source-authority-v1-keys
  #{:artifact :source-relative-path :source-sha256 :source-byte-count
    :module :function-source-hashes})

(def ^:private p15-s23-sh07-compiler-source-authority-v2-keys
  #{:artifact :manifest-id :module-source-id :source-sha256
    :function-source-hashes :module :logical-source-path})

(def ^:private p15-s23-sh07-instruction-provenance-v1-keys
  #{:artifact :source-relative-path :source-sha256 :function
    :function-source-hash :callee})

(def ^:private p15-s23-sh07-instruction-provenance-v2-keys
  #{:artifact :manifest-id :module-source-id :source-sha256 :function
    :function-source-hash :callee})

(declare p15-s23-stage2-compiler-artifact-source-path)
(declare p15-s23-sh07-b51-module-source-valid?)
(declare p15-s23-sh07-b51-manifest-id)

(defn- p15-s23-sh07-source-definition-hash
  [definition]
  (reader-canonical-hash
   (select-keys definition [:name :arity :params :body])))

(defn- p15-s23-sh07-governed-source-file
  [logical-source-path]
  (when (string? logical-source-path)
    (let [anchor (java.io.File.
                  (p15-s23-stage2-compiler-artifact-source-path))
          start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
      (loop [directory start]
        (when directory
          (let [candidate
                (java.io.File. directory logical-source-path)]
            (if (.isFile candidate)
              (.getCanonicalFile candidate)
              (recur (.getParentFile directory)))))))))

(defn- p15-s23-sh07-source-authority-fail!
  [message source-path]
  (throw (ex-info message
                  {:id "L2-UNKNOWN-CORE-FORM"
                   :reason :unauthenticated-compiler-provenance
                   :source-path source-path})))

(defn- p15-s23-sh07-b51-source-input-authentic?
  [source-path supplied-bytes source-sha256 module descriptor manifest]
  (try
    (let [logical-source-path (:logical-source-path descriptor)]
      (and (= logical-source-path source-path)
           (p15-s23-sh07-b51-module-source-valid? descriptor)
           (map? manifest)
           (= (:manifest-id manifest)
              (p15-s23-sh07-b51-manifest-id manifest))
           (= (:module-source-id descriptor)
              (some #{(:module-source-id descriptor)}
                    (:module-source-ids manifest)))
           (= (:source-sha256 descriptor) source-sha256)
           (= (:source-byte-count descriptor) (alength supplied-bytes))
           (= (:module descriptor) (:module module))
           (= :meta (:profile module))
           (= :jvm (:target module))
           (= #{} (:effects module))
           (= #{} (:capabilities module))
           (= :safe (:safety module))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-current-module-source-authority
  [source-path source-text module function-table]
  (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
        manifest (:p15-s23-sh07-link-manifest module)
        supplied-bytes (.getBytes ^String source-text
                                  java.nio.charset.StandardCharsets/UTF_8)
        source-sha256 (str "sha256:" (sha256-bytes-hex supplied-bytes))]
    (when-not
     (and descriptor
          (p15-s23-sh07-b51-source-input-authentic?
           source-path supplied-bytes source-sha256 module descriptor manifest))
      (p15-s23-sh07-source-authority-fail!
       "compiler-internal builtins require authenticated B51 source"
       source-path))
    {:artifact :gravity/sh07-compiler-source-authority-v2
     :manifest-id (:manifest-id manifest)
     :module-source-id (:module-source-id descriptor)
     :logical-source-path (:logical-source-path descriptor)
     :source-sha256 source-sha256
     :module (:module descriptor)
     :function-source-hashes
     (into (sorted-map)
           (map (fn [[name definition]]
                  [name (p15-s23-sh07-source-definition-hash definition)]))
           function-table)}))

(defn p15-s23-sh07-compiler-source-authority
  [source-path source-text module function-table]
  (if-let [descriptor (:p15-s23-sh07-module-source-descriptor module)]
    (if *p15-s23-sh07-b51-governed-source-session*
      (p15-s23-sh07-b51-governed-source-event!
       :source-authority (+ 3 (:module-ordinal descriptor))
       (:module-ordinal descriptor)
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority
       (fn [{cached-descriptor :descriptor cached-text :text}]
         (when-not (and (= (:logical-source-path descriptor)
                            (:path cached-descriptor))
                        (= source-path (:path cached-descriptor))
                        (= source-text cached-text))
           (p15-s23-sh07-source-authority-fail!
            "compiler-internal builtins require the authenticated C object"
            source-path))
         (p15-s23-sh07-b51-current-module-source-authority
          source-path cached-text module function-table)))
      (let [{:keys [path text]}
            (p15-s23-sh07-b51-compatibility-source-record
             (:logical-source-path descriptor))]
        (when-not (= source-text text)
          (p15-s23-sh07-source-authority-fail!
           "compiler-internal builtins require authenticated B51 source"
           source-path))
        (p15-s23-sh07-b51-current-module-source-authority
         (:logical-source-path descriptor) text module function-table)))
    (let [{:keys [path bytes text]}
          (if *p15-s23-sh07-b51-governed-source-session*
            (let [{:keys [descriptor bytes text]}
                  (p15-s23-sh07-b51-governed-source-entry 2)]
              {:path (:path descriptor) :bytes bytes :text text})
            (p15-s23-sh07-b51-compatibility-source-record
             p15-s23-sh07-checked-core-source-relative-path))
          source-bytes (.getBytes ^String source-text
                                  java.nio.charset.StandardCharsets/UTF_8)
          source-sha256 (str "sha256:" (sha256-bytes-hex source-bytes))]
      (when-not
       (and (= source-text text)
            (if *p15-s23-sh07-b51-governed-source-session*
              (= source-path path)
              (= (.getCanonicalFile (java.io.File. source-path))
                 (.getCanonicalFile (java.io.File. path))))
            (= 'gravity.checked-core (:module module))
            (= :meta (:profile module)) (= :jvm (:target module))
            (= #{} (:effects module)) (= #{} (:capabilities module))
            (= :safe (:safety module)))
        (p15-s23-sh07-source-authority-fail!
         "compiler-internal builtins require checked-core source"
         source-path))
      {:artifact :gravity/sh07-compiler-source-authority-v1
       :source-relative-path p15-s23-sh07-checked-core-source-relative-path
       :source-sha256 source-sha256
       :source-byte-count (alength source-bytes)
       :module 'gravity.checked-core
       :function-source-hashes
       (into (sorted-map)
             (map (fn [[name definition]]
                    [name (p15-s23-sh07-source-definition-hash definition)]))
             function-table)})))

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

(defn- p15-s23-sh07-p-decimal-magnitude-compare [left right]
  (cond
    (< (count left) (count right)) -1
    (> (count left) (count right)) 1
    :else
    (loop [index 0]
      (if (= index (count left)) 0
          (let [l (nth left index) r (nth right index)]
            (cond (< l r) -1 (> l r) 1 :else (recur (inc index))))))))

(defn- p15-s23-sh07-p-decimal-add-magnitudes [left right]
  (loop [li (dec (count left)) ri (dec (count right)) carry 0 out ()]
    (if (and (neg? li) (neg? ri) (zero? carry))
      (vec out)
      (let [l (if (neg? li) 0 (- (nth left li) 48))
            r (if (neg? ri) 0 (- (nth right ri) 48))
            total (+ l r carry)]
        (recur (dec li) (dec ri) (quot total 10)
               (conj out (+ 48 (mod total 10))))))))

(defn- p15-s23-sh07-p-decimal-subtract-magnitudes [larger smaller]
  (loop [li (dec (count larger)) si (dec (count smaller)) borrow 0 out ()]
    (if (neg? li)
      (let [digits (vec (drop-while #(= 48 %) out))]
        (if (seq digits) digits [48]))
      (let [l (- (nth larger li) 48 borrow)
            s (if (neg? si) 0 (- (nth smaller si) 48))
            borrowed? (< l s)
            digit (- (if borrowed? (+ l 10) l) s)]
        (recur (dec li) (dec si) (if borrowed? 1 0)
               (conj out (+ 48 digit)))))))

(defn- p15-s23-sh07-p-decimal-signed [negative? digits]
  (if (= digits [48]) {:negative? false :digits [48]}
      {:negative? (boolean negative?) :digits digits}))

(defn- p15-s23-sh07-p-decimal-signed-add [left right]
  (if (= (:negative? left) (:negative? right))
    (p15-s23-sh07-p-decimal-signed
     (:negative? left)
     (p15-s23-sh07-p-decimal-add-magnitudes (:digits left) (:digits right)))
    (let [comparison
          (p15-s23-sh07-p-decimal-magnitude-compare
           (:digits left) (:digits right))]
      (cond
        (zero? comparison) (p15-s23-sh07-p-decimal-signed false [48])
        (pos? comparison)
        (p15-s23-sh07-p-decimal-signed
         (:negative? left)
         (p15-s23-sh07-p-decimal-subtract-magnitudes
          (:digits left) (:digits right)))
        :else
        (p15-s23-sh07-p-decimal-signed
         (:negative? right)
         (p15-s23-sh07-p-decimal-subtract-magnitudes
          (:digits right) (:digits left)))))))

(defn- p15-s23-sh07-p-decimal-small-digits [value]
  (mapv #(+ 48 (- (int %) 48)) (str value)))

(defn- p15-s23-sh07-p-decimal-power-parts [power]
  (cond
    (and (integer? power)
         (<= -999999999999999999 power 999999999999999999))
    (let [negative? (neg? power)
          text (str power)
          digits (mapv int (if negative? (subs text 1) text))]
      (when (<= (count digits) 18)
        (p15-s23-sh07-p-decimal-signed negative? digits)))

    (and (map? power)
         (= #{:artifact :sign :digit-codepoints} (set (keys power)))
         (= :gravity/signed-decimal-power (:artifact power))
         (contains? #{:negative :positive} (:sign power))
         (vector? (:digit-codepoints power))
         (<= (count (:digit-codepoints power)) 258)
         (p15-s23-sh07-p-decimal-digits? (:digit-codepoints power))
         (not= 48 (first (:digit-codepoints power))))
    (p15-s23-sh07-p-decimal-signed
     (= :negative (:sign power)) (:digit-codepoints power))

    :else nil))

(defn- p15-s23-sh07-p-decimal-key? [key]
  (and (map? key)
       (= #{:kind :sign :coefficient :decimal-power} (set (keys key)))
       (= :decimal (:kind key))
       (contains? #{-1 1} (:sign key))
       (vector? (:coefficient key))
       (<= (count (:coefficient key)) 257)
       (p15-s23-sh07-p-decimal-digits? (:coefficient key))
       (or (= [48] (:coefficient key))
           (and (not= 48 (first (:coefficient key)))
                (not= 48 (peek (:coefficient key)))))
       (some? (p15-s23-sh07-p-decimal-power-parts (:decimal-power key)))
       (if (= [48] (:coefficient key))
         (and (= 1 (:sign key)) (= 0 (:decimal-power key))) true)))

(defn- p15-s23-sh07-p-decimal-power-value [signed]
  (if (= [48] (:digits signed))
    0
    (if (<= (count (:digits signed)) 18)
      (let [magnitude
            (reduce (fn [result code] (+ (* result 10) (- code 48)))
                    0 (:digits signed))]
        (if (:negative? signed) (- magnitude) magnitude))
      {:artifact :gravity/signed-decimal-power
       :sign (if (:negative? signed) :negative :positive)
       :digit-codepoints (:digits signed)})))

(defn p15-s23-sh07-p-decimal-spell [key]
  (when-not (p15-s23-sh07-p-decimal-key? key)
    (throw (ex-info "unsupported producer decimal semantic key"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= [48] (:coefficient key))
    "0E+0"
    (let [coefficient (:coefficient key)
          power (p15-s23-sh07-p-decimal-power-parts (:decimal-power key))
          adjustment
          (p15-s23-sh07-p-decimal-signed
           false (p15-s23-sh07-p-decimal-small-digits (dec (count coefficient))))
          exponent (p15-s23-sh07-p-decimal-signed-add power adjustment)
          anticipated-count
          (+ (if (= -1 (:sign key)) 1 0)
             (count coefficient)
             (if (> (count coefficient) 1) 1 0)
             2
             (count (:digits exponent)))
          _
          (when (> anticipated-count 261)
            (throw (ex-info "producer decimal spelling exceeds G19 domain"
                            {:reason :unsupported-decimal-readable-spelling})))
          text
          (str (when (= -1 (:sign key)) "-")
               (char (first coefficient))
               (when (> (count coefficient) 1)
                 (str "." (apply str (map char (rest coefficient)))))
               "E" (if (:negative? exponent) "-" "+")
               (apply str (map char (:digits exponent))))]
      (when (> (count text) 261)
        (throw (ex-info "producer decimal spelling exceeds G19 domain"
                        {:reason :unsupported-decimal-readable-spelling})))
      text)))

(defn p15-s23-sh07-p-decimal-inverse [text]
  (when-not (and (string? text) (<= 1 (count text) 261)
                 (every? #(<= (int %) 127) text))
    (throw (ex-info "malformed producer decimal inverse input"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= text "0E+0")
    {:kind :decimal :sign 1 :coefficient [48] :decimal-power 0}
    (let [characters (vec text)
          negative? (= \- (first characters))
          start (if negative? 1 0)
          first-digit (get characters start)
          e-index (.indexOf ^java.util.List characters \E)]
      (when-not (and first-digit (<= (int \1) (int first-digit) (int \9))
                     (> e-index start) (= e-index (.lastIndexOf ^java.util.List characters \E)))
        (throw (ex-info "noncanonical producer decimal mantissa"
                        {:reason :unsupported-decimal-readable-spelling})))
      (let [mantissa (subvec characters start e-index)
            exponent-part (subvec characters (inc e-index))
            dotted? (and (> (count mantissa) 1) (= \. (second mantissa)))
            coefficient-chars (if dotted? (into [(first mantissa)] (subvec mantissa 2)) mantissa)
            exponent-sign (first exponent-part)
            exponent-chars (subvec exponent-part 1)]
        (when-not
         (and (or (= 1 (count mantissa))
                  (and dotted? (> (count mantissa) 2)))
              (every? #(<= (int \0) (int %) (int \9)) coefficient-chars)
              (not= \0 (last coefficient-chars))
              (contains? #{\- \+} exponent-sign)
              (seq exponent-chars)
              (every? #(<= (int \0) (int %) (int \9)) exponent-chars)
              (or (= 1 (count exponent-chars)) (not= \0 (first exponent-chars))))
          (throw (ex-info "noncanonical producer decimal grammar"
                          {:reason :unsupported-decimal-readable-spelling})))
        (let [coefficient (mapv int coefficient-chars)
              exponent
              (p15-s23-sh07-p-decimal-signed
               (= \- exponent-sign) (mapv int exponent-chars))
              adjustment
              (p15-s23-sh07-p-decimal-signed
               true (p15-s23-sh07-p-decimal-small-digits (dec (count coefficient))))
              key {:kind :decimal
                   :sign (if negative? -1 1)
                   :coefficient coefficient
                   :decimal-power
                   (p15-s23-sh07-p-decimal-power-value
                    (p15-s23-sh07-p-decimal-signed-add exponent adjustment))}]
          (when-not (= text (p15-s23-sh07-p-decimal-spell key))
            (throw (ex-info "producer decimal inverse does not respell"
                            {:reason :unsupported-decimal-readable-spelling})))
          key)))))

(declare c2-reader-artifact-id
         sh03-reader-final-decimal-occurrence-projection-valid?)

(defn- p15-s23-sh07-p-decimal-occurrence
  [evidence]
  (when-not (and (map? evidence)
                 (= #{:c2-reader-artifact :adapted-form-id
                      :adapted-token-id}
                    (set (keys evidence))))
    (throw (ex-info "producer decimal projection evidence is malformed"
                    {:reason :unsupported-decimal-readable-spelling})))
  (let [artifact (:c2-reader-artifact evidence)
        form-id (:adapted-form-id evidence)
        token-id (:adapted-token-id evidence)
        projection (:sh07-decimal-occurrence-projection artifact)
        selected
        (filterv #(and (= form-id (:adapted-form-id %))
                       (= token-id (:adapted-token-id %)))
                 (:occurrences projection))]
    (when-not
     (and (= (:artifact-id artifact) (c2-reader-artifact-id artifact))
          (sh03-reader-final-decimal-occurrence-projection-valid? artifact)
          (= 1 (count selected)))
      (throw (ex-info "producer decimal projection authentication failed"
                      {:reason :unsupported-decimal-readable-spelling})))
    (first selected)))

(defn- p15-s23-sh07-p-decimal-occurrence-raw
  [evidence occurrence]
  (let [form-id (:adapted-form-id occurrence)
        token-id (:adapted-token-id occurrence)
        forms
        (filterv #(and (= form-id (:form-id %))
                       (= token-id (:open-token %)))
                 (get-in evidence [:c2-reader-artifact :form-tree]))
        literals
        (filterv #(= form-id (:form-id %))
                 (get-in evidence
                         [:c2-reader-artifact :literal-decoding-records]))]
    (when-not (and (= 1 (count forms)) (= 1 (count literals))
                   (= (:raw (first forms)) (:raw (first literals))))
      (throw (ex-info "producer decimal occurrence text is not unique"
                      {:reason :unsupported-decimal-readable-spelling})))
    (:raw (first forms))))

(defn- p15-s23-sh07-p-decimal-occurrence-identity
  [evidence occurrence]
  [(get-in evidence [:c2-reader-artifact :artifact-id])
   (:adapted-form-id occurrence)
   (:adapted-token-id occurrence)])

(defn- p15-s23-sh07-p-decimal-c2-evidence?
  [text key evidence]
  (try
    (let [occurrence (p15-s23-sh07-p-decimal-occurrence evidence)
          entry (:semantic-value-entry occurrence)
          descriptor (:descriptor entry)]
      (and (= text (p15-s23-sh07-p-decimal-occurrence-raw
                    evidence occurrence))
           (if (<= (count text) 256)
        (and (p15-s23-sh07-p-decimal-key? (:semantic-key entry))
             (= key (:semantic-key entry) (:semantic-key descriptor))
             (nil? (:normalization-reason descriptor)))
        (and (= :deferred (:semantic-validation descriptor))
             (= :reader-semantic-work-boundary
                (:normalization-reason descriptor))
             (= {:kind :decimal
                 :normalization :reader-semantic-work-boundary
                 :bounded-semantic-key (:semantic-key descriptor)}
                (:semantic-key entry))))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch Throwable _ false)))

(defn p15-s23-sh07-p-decimal-c2-branch [text key evidence]
  (let [inverse (p15-s23-sh07-p-decimal-inverse text)]
    (when-not (and (= key inverse)
                   (p15-s23-sh07-p-decimal-c2-evidence? text key evidence))
      (throw (ex-info "producer decimal C2 evidence mismatch"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (<= (count text) 256)
      {:status :exact-normalized :semantic-key key}
      {:status :normalization-deferred
       :normalization-reason :reader-semantic-work-boundary
       :semantic-key key})))

(defn p15-s23-sh07-p-decimal-decoration [key evidence]
  (let [text (p15-s23-sh07-p-decimal-spell key)
        branch (p15-s23-sh07-p-decimal-c2-branch text key evidence)]
    (when-not (= key (:semantic-key branch))
      (throw (ex-info "producer decimal branch lost semantic identity"
                      {:reason :unsupported-decimal-readable-spelling})))
    text))

(defn p15-s23-sh07-p-canonical-decimal-decoration
  [source-evidence replay-evidence]
  (let [occurrence (p15-s23-sh07-p-decimal-occurrence source-evidence)
        replay-occurrence
        (p15-s23-sh07-p-decimal-occurrence replay-evidence)
        key (get-in occurrence [:semantic-value-entry :semantic-key])]
    (when-not (p15-s23-sh07-p-decimal-key? key)
      (throw (ex-info "producer source occurrence has no exact decimal key"
                      {:reason :unsupported-decimal-readable-spelling})))
    (when (= (p15-s23-sh07-p-decimal-occurrence-identity
              source-evidence occurrence)
             (p15-s23-sh07-p-decimal-occurrence-identity
              replay-evidence replay-occurrence))
      (throw (ex-info "producer decimal replay must be a fresh occurrence"
                      {:reason :unsupported-decimal-readable-spelling})))
    (p15-s23-sh07-p-decimal-decoration key replay-evidence)))

;; Independently authored source-oracle decimal algebra.  Keep changes here
;; mechanically separate from the producer model above.
(defn- p15-s23-sh07-o-decimal-digits? [digits]
  (and (vector? digits) (seq digits)
       (loop [remaining digits]
         (if (empty? remaining) true
             (let [code (first remaining)]
               (if (and (integer? code) (<= 48 code 57))
                 (recur (rest remaining)) false))))))

(defn- p15-s23-sh07-o-decimal-compare [left right]
  (if (= (count left) (count right))
    (loop [l left r right]
      (if (empty? l) 0
          (cond (< (first l) (first r)) -1
                (> (first l) (first r)) 1
                :else (recur (rest l) (rest r)))))
    (if (< (count left) (count right)) -1 1)))

(defn- p15-s23-sh07-o-decimal-plus [left right]
  (loop [l (reverse left) r (reverse right) carry 0 result []]
    (if (and (empty? l) (empty? r) (zero? carry))
      (vec (reverse result))
      (let [total (+ carry
                     (if (empty? l) 0 (- (first l) 48))
                     (if (empty? r) 0 (- (first r) 48)))]
        (recur (rest l) (rest r) (quot total 10)
               (conj result (+ 48 (mod total 10))))))))

(defn- p15-s23-sh07-o-decimal-minus [larger smaller]
  (loop [l (reverse larger) r (reverse smaller) borrow 0 result []]
    (if (empty? l)
      (let [forward (reverse result)
            normalized (drop-while #(= 48 %) forward)]
        (vec (if (seq normalized) normalized [48])))
      (let [raw (- (- (first l) 48) borrow)
            subtrahend (if (empty? r) 0 (- (first r) 48))
            take? (< raw subtrahend)]
        (recur (rest l) (rest r) (if take? 1 0)
               (conj result (+ 48 (- (if take? (+ raw 10) raw)
                                      subtrahend))))))))

(defn- p15-s23-sh07-o-decimal-normal-signed [sign digits]
  {:sign (if (= digits [48]) :positive sign) :digits digits})

(defn- p15-s23-sh07-o-decimal-sum [left right]
  (if (= (:sign left) (:sign right))
    (p15-s23-sh07-o-decimal-normal-signed
     (:sign left) (p15-s23-sh07-o-decimal-plus (:digits left) (:digits right)))
    (let [order (p15-s23-sh07-o-decimal-compare (:digits left) (:digits right))]
      (cond
        (zero? order) (p15-s23-sh07-o-decimal-normal-signed :positive [48])
        (pos? order)
        (p15-s23-sh07-o-decimal-normal-signed
         (:sign left) (p15-s23-sh07-o-decimal-minus (:digits left) (:digits right)))
        :else
        (p15-s23-sh07-o-decimal-normal-signed
         (:sign right) (p15-s23-sh07-o-decimal-minus (:digits right) (:digits left)))))))

(defn- p15-s23-sh07-o-decimal-count-digits [value]
  (mapv int (str value)))

(defn- p15-s23-sh07-o-decimal-power [power]
  (if (and (integer? power)
           (<= -999999999999999999 power 999999999999999999))
    (let [negative? (neg? power)
          digits (mapv int (str (if negative? (- power) power)))]
      (when (<= (count digits) 18)
        (p15-s23-sh07-o-decimal-normal-signed
         (if negative? :negative :positive) digits)))
    (when (and (map? power)
               (= #{:artifact :sign :digit-codepoints} (set (keys power)))
               (= :gravity/signed-decimal-power (:artifact power))
               (contains? #{:negative :positive} (:sign power))
               (vector? (:digit-codepoints power))
               (<= (count (:digit-codepoints power)) 258)
               (p15-s23-sh07-o-decimal-digits? (:digit-codepoints power))
               (not= 48 (first (:digit-codepoints power))))
      (p15-s23-sh07-o-decimal-normal-signed
       (:sign power) (:digit-codepoints power)))))

(defn- p15-s23-sh07-o-decimal-authentic-key? [key]
  (and (map? key)
       (= #{:kind :sign :coefficient :decimal-power} (set (keys key)))
       (= :decimal (:kind key))
       (contains? #{-1 1} (:sign key))
       (vector? (:coefficient key))
       (<= (count (:coefficient key)) 257)
       (p15-s23-sh07-o-decimal-digits? (:coefficient key))
       (if (= [48] (:coefficient key))
         (and (= 1 (:sign key)) (= 0 (:decimal-power key)))
         (and (not= 48 (first (:coefficient key)))
              (not= 48 (last (:coefficient key)))))
       (some? (p15-s23-sh07-o-decimal-power (:decimal-power key)))))

(defn- p15-s23-sh07-o-decimal-encoded-power [signed]
  (if (= [48] (:digits signed)) 0
      (if (<= (count (:digits signed)) 18)
        (let [magnitude
              (loop [remaining (:digits signed) result 0]
                (if (empty? remaining) result
                    (recur (rest remaining)
                           (+ (* result 10) (- (first remaining) 48)))))]
          (if (= :negative (:sign signed)) (- magnitude) magnitude))
        {:artifact :gravity/signed-decimal-power
         :sign (:sign signed) :digit-codepoints (:digits signed)})))

(defn p15-s23-sh07-o-decimal-spell [key]
  (when-not (p15-s23-sh07-o-decimal-authentic-key? key)
    (throw (ex-info "source oracle rejected decimal key"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= [48] (:coefficient key)) "0E+0"
      (let [coefficient (:coefficient key)
            base (p15-s23-sh07-o-decimal-power (:decimal-power key))
            shift (p15-s23-sh07-o-decimal-normal-signed
                   :positive
                   (p15-s23-sh07-o-decimal-count-digits
                    (dec (count coefficient))))
            exponent (p15-s23-sh07-o-decimal-sum base shift)
            anticipated-count
            (+ (if (= -1 (:sign key)) 1 0)
               (count coefficient)
               (if (= 1 (count coefficient)) 0 1)
               2
               (count (:digits exponent)))
            _
            (when (> anticipated-count 261)
              (throw (ex-info "source oracle decimal exceeds bound"
                              {:reason :unsupported-decimal-readable-spelling})))
            result (str (if (= -1 (:sign key)) "-" "")
                        (char (first coefficient))
                        (if (= 1 (count coefficient)) ""
                            (str "." (apply str (map char (rest coefficient)))))
                        "E" (if (= :negative (:sign exponent)) "-" "+")
                        (apply str (map char (:digits exponent))))]
        (when (> (count result) 261)
          (throw (ex-info "source oracle decimal exceeds bound"
                          {:reason :unsupported-decimal-readable-spelling})))
        result)))

(defn p15-s23-sh07-o-decimal-inverse [text]
  (let [chars (when (string? text) (vec text))]
    (when-not (and chars (<= 1 (count chars) 261)
                   (every? #(<= (int %) 127) chars))
      (throw (ex-info "source oracle decimal inverse domain"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (= chars [\0 \E \+ \0])
      {:kind :decimal :sign 1 :coefficient [48] :decimal-power 0}
      (let [negative? (= \- (first chars))
            offset (if negative? 1 0)
            e-positions (keep-indexed #(when (= %2 \E) %1) chars)]
        (when-not (= 1 (count e-positions))
          (throw (ex-info "source oracle decimal exponent marker"
                          {:reason :unsupported-decimal-readable-spelling})))
        (let [e-position (first e-positions)
              mantissa (subvec chars offset e-position)
              exponent-part (subvec chars (inc e-position))
              dot-positions (keep-indexed #(when (= %2 \.) %1) mantissa)
              dotted? (= 1 (count dot-positions))
              coefficient-chars
              (if dotted?
                (let [dot (first dot-positions)]
                  (when-not (= dot 1)
                    (throw (ex-info "source oracle decimal point"
                                    {:reason :unsupported-decimal-readable-spelling})))
                  (into (subvec mantissa 0 dot) (subvec mantissa (inc dot))))
                mantissa)
              exponent-sign (first exponent-part)
              exponent-chars (subvec exponent-part 1)]
          (when-not
           (and (seq coefficient-chars)
                (<= (int \1) (int (first coefficient-chars)) (int \9))
                (every? #(<= (int \0) (int %) (int \9)) coefficient-chars)
                (not= \0 (last coefficient-chars))
                (or (= 1 (count coefficient-chars)) dotted?)
                (or (= exponent-sign \+) (= exponent-sign \-))
                (seq exponent-chars)
                (every? #(<= (int \0) (int %) (int \9)) exponent-chars)
                (or (= 1 (count exponent-chars))
                    (not= \0 (first exponent-chars))))
            (throw (ex-info "source oracle noncanonical decimal"
                            {:reason :unsupported-decimal-readable-spelling})))
          (let [coefficient (mapv int coefficient-chars)
                exponent (p15-s23-sh07-o-decimal-normal-signed
                          (if (= exponent-sign \-) :negative :positive)
                          (mapv int exponent-chars))
                shift (p15-s23-sh07-o-decimal-normal-signed
                       :negative
                       (p15-s23-sh07-o-decimal-count-digits
                        (dec (count coefficient))))
                key {:kind :decimal :sign (if negative? -1 1)
                     :coefficient coefficient
                     :decimal-power
                     (p15-s23-sh07-o-decimal-encoded-power
                      (p15-s23-sh07-o-decimal-sum exponent shift))}]
            (when-not (= text (p15-s23-sh07-o-decimal-spell key))
              (throw (ex-info "source oracle decimal respelling mismatch"
                              {:reason :unsupported-decimal-readable-spelling})))
            key))))))

(defn- p15-s23-sh07-o-decimal-occurrence
  [evidence]
  (when-not (and (map? evidence)
                 (= 3 (count evidence))
                 (contains? evidence :c2-reader-artifact)
                 (contains? evidence :adapted-form-id)
                 (contains? evidence :adapted-token-id))
    (throw (ex-info "source oracle projection evidence is malformed"
                    {:reason :unsupported-decimal-readable-spelling})))
  (let [artifact (get evidence :c2-reader-artifact)
        wanted-form (get evidence :adapted-form-id)
        wanted-token (get evidence :adapted-token-id)
        candidates
        (loop [remaining
               (get-in artifact
                       [:sh07-decimal-occurrence-projection :occurrences])
               result []]
          (if (empty? remaining)
            result
            (let [candidate (first remaining)]
              (recur
               (rest remaining)
               (if (and (= wanted-form (get candidate :adapted-form-id))
                        (= wanted-token (get candidate :adapted-token-id)))
                 (conj result candidate)
                 result)))))]
    (when-not (and (= (get artifact :artifact-id)
                      (c2-reader-artifact-id artifact))
                   (true?
                    (sh03-reader-final-decimal-occurrence-projection-valid?
                     artifact))
                   (= 1 (count candidates)))
      (throw (ex-info "source oracle projection authentication failed"
                      {:reason :unsupported-decimal-readable-spelling})))
    (first candidates)))

(defn- p15-s23-sh07-o-decimal-occurrence-text
  [evidence occurrence]
  (let [artifact (get evidence :c2-reader-artifact)
        form-id (get occurrence :adapted-form-id)
        token-id (get occurrence :adapted-token-id)
        forms
        (loop [remaining (get artifact :form-tree) selected []]
          (if (empty? remaining) selected
              (let [form (first remaining)]
                (recur (rest remaining)
                       (if (and (= form-id (get form :form-id))
                                (= token-id (get form :open-token)))
                         (conj selected form) selected)))))
        literals
        (loop [remaining (get artifact :literal-decoding-records) selected []]
          (if (empty? remaining) selected
              (let [literal (first remaining)]
                (recur (rest remaining)
                       (if (= form-id (get literal :form-id))
                         (conj selected literal) selected)))))]
    (when-not (and (= 1 (count forms)) (= 1 (count literals))
                   (= (get (first forms) :raw)
                      (get (first literals) :raw)))
      (throw (ex-info "source oracle occurrence text is not unique"
                      {:reason :unsupported-decimal-readable-spelling})))
    (get (first forms) :raw)))

(defn- p15-s23-sh07-o-decimal-occurrence-identity
  [evidence occurrence]
  [(get-in evidence [:c2-reader-artifact :artifact-id])
   (get occurrence :adapted-form-id)
   (get occurrence :adapted-token-id)])

(defn- p15-s23-sh07-o-decimal-c2-evidence?
  [text key evidence]
  (try
    (let [occurrence (p15-s23-sh07-o-decimal-occurrence evidence)
          entry (get occurrence :semantic-value-entry)
          descriptor (get entry :descriptor)]
      (and (= text (p15-s23-sh07-o-decimal-occurrence-text
                    evidence occurrence))
           (if (<= (count text) 256)
        (and (p15-s23-sh07-o-decimal-authentic-key?
              (get entry :semantic-key))
             (= key (get entry :semantic-key))
             (= key (get descriptor :semantic-key))
             (= nil (get descriptor :normalization-reason)))
        (and (= :deferred (get descriptor :semantic-validation))
             (= :reader-semantic-work-boundary
                (get descriptor :normalization-reason))
             (= {:kind :decimal
                 :normalization :reader-semantic-work-boundary
                 :bounded-semantic-key (get descriptor :semantic-key)}
                (get entry :semantic-key))))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch Throwable _ false)))

(defn p15-s23-sh07-o-decimal-c2-branch [text key evidence]
  (let [inverse-key (p15-s23-sh07-o-decimal-inverse text)]
    (when-not (and (= key inverse-key)
                   (p15-s23-sh07-o-decimal-c2-evidence? text key evidence))
      (throw (ex-info "source oracle decimal C2 evidence mismatch"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (<= (count text) 256)
      {:status :exact-normalized :semantic-key key}
      {:status :normalization-deferred
       :normalization-reason :reader-semantic-work-boundary
       :semantic-key key})))

(defn p15-s23-sh07-o-decimal-decoration [key evidence]
  (let [text (p15-s23-sh07-o-decimal-spell key)
        branch (p15-s23-sh07-o-decimal-c2-branch text key evidence)]
    (when-not (= key (:semantic-key branch))
      (throw (ex-info "source oracle decimal branch lost semantic identity"
                      {:reason :unsupported-decimal-readable-spelling})))
    text))

(defn- p15-s23-sh07-o-scalar-kind
  [value]
  (cond
    (nil? value) :nil
    (instance? java.lang.Boolean value) :boolean
    (or (instance? java.lang.Byte value)
        (instance? java.lang.Short value)
        (instance? java.lang.Integer value)
        (instance? java.lang.Long value)
        (instance? java.math.BigInteger value)
        (instance? clojure.lang.BigInt value)) :integer
    (instance? clojure.lang.Ratio value) :ratio
    (instance? java.math.BigDecimal value) :decimal
    (instance? java.lang.Double value) :floating
    (instance? java.lang.String value) :string
    (instance? java.lang.Character value) :character
    (instance? clojure.lang.Keyword value) :keyword
    (instance? clojure.lang.Symbol value) :symbol
    (instance? java.util.Date value) :instant
    (instance? java.util.UUID value) :uuid
    (vector? value) :vector
    (list? value) :list
    (map? value) :map
    (set? value) :set
    :else nil))

(defn- p15-s23-sh07-o-scalar-writer
  []
  {:builder (StringBuilder.) :bytes (volatile! 0)})

(defn- p15-s23-sh07-o-append-scalar-fragment!
  [writer fragment]
  (let [observation
        (p15-s23-sh07-observe-utf8-byte-count
         fragment @(:bytes writer) 32768)]
    (when-not (= :accepted (:status observation))
      (throw
       (ex-info "source oracle scalar exceeded its G20 boundary"
                {:reason
                 (if (= :invalid-unicode-scalar-sequence
                        (:reason observation))
                   :invalid-unicode-scalar :scalar-byte-limit)})))
    ;; G20 admission is prospective: allocation into the output builder occurs
    ;; only after the exact observation accepts this fragment.
    (.append ^StringBuilder (:builder writer) ^String fragment)
    (vreset! (:bytes writer) (:observed-after observation))
    writer))

(defn- p15-s23-sh07-o-finish-scalar
  [writer]
  {:text (.toString ^StringBuilder (:builder writer))
   :output-utf8-bytes @(:bytes writer)})

(defn- p15-s23-sh07-o-integer-text
  [value]
  (let [integer (biginteger value)
        negative? (neg? (.signum integer))
        magnitude (.abs integer)
        ten (java.math.BigInteger/valueOf 10)
        writer (p15-s23-sh07-o-scalar-writer)
        initial-count (if negative? 1 0)]
    (if (zero? (.signum magnitude))
      (do (p15-s23-sh07-o-append-scalar-fragment! writer "0")
          (p15-s23-sh07-o-finish-scalar writer))
      (loop [remaining magnitude digits () digit-count initial-count]
        (if (zero? (.signum remaining))
          (do
            (when negative?
              (p15-s23-sh07-o-append-scalar-fragment! writer "-"))
            (doseq [digit digits]
              (p15-s23-sh07-o-append-scalar-fragment!
               writer (str digit)))
            (p15-s23-sh07-o-finish-scalar writer))
          (let [admission
                (p15-s23-sh07-observe-utf8-byte-count
                 "0" digit-count 32768)
                _ (when-not (= :accepted (:status admission))
                    (throw
                     (ex-info "integer exceeded prospective scalar boundary"
                              {:reason :scalar-byte-limit})))
                digit (.intValue (.remainder remaining ten))]
            (recur (.divide remaining ten)
                   (conj digits (.charAt "0123456789" digit))
                   (:observed-after admission))))))))

(defn- p15-s23-sh07-o-append-hex4!
  [writer code]
  (doseq [shift [12 8 4 0]]
    (p15-s23-sh07-o-append-scalar-fragment!
     writer
     (str (.charAt "0123456789abcdef"
                   (bit-and 15 (bit-shift-right code shift))))))
  writer)

(defn- p15-s23-sh07-o-string-text
  [^String value]
  (let [writer (p15-s23-sh07-o-scalar-writer)]
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (loop [index 0]
      (when (< index (.length value))
        (let [[codepoint next-index]
              (p15-s23-sh07-next-scalar value index)]
          (case codepoint
            8 (p15-s23-sh07-o-append-scalar-fragment! writer "\\b")
            9 (p15-s23-sh07-o-append-scalar-fragment! writer "\\t")
            10 (p15-s23-sh07-o-append-scalar-fragment! writer "\\n")
            12 (p15-s23-sh07-o-append-scalar-fragment! writer "\\f")
            13 (p15-s23-sh07-o-append-scalar-fragment! writer "\\r")
            34 (p15-s23-sh07-o-append-scalar-fragment! writer "\\\"")
            92 (p15-s23-sh07-o-append-scalar-fragment! writer "\\\\")
            (if (or (< codepoint 32) (= codepoint 127))
              (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\u")
                  (p15-s23-sh07-o-append-hex4! writer codepoint))
              (p15-s23-sh07-o-append-scalar-fragment!
               writer (String. (Character/toChars codepoint)))))
          (recur next-index))))
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-character-text
  [value]
  (let [unit (char value)
        named (case (int unit)
                8 "\\backspace" 9 "\\tab" 10 "\\newline"
                12 "\\formfeed" 13 "\\return" 32 "\\space" nil)
        writer (p15-s23-sh07-o-scalar-writer)]
    (if named
      (p15-s23-sh07-o-append-scalar-fragment! writer named)
      (if (or (Character/isWhitespace unit)
              (Character/isISOControl unit)
              (contains? #{\, \; \" \' \` \~ \^ \@ \\ \#} unit))
        (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\u")
            (p15-s23-sh07-o-append-hex4! writer (int unit)))
        (do (p15-s23-sh07-o-append-scalar-fragment! writer "\\")
            (p15-s23-sh07-o-append-scalar-fragment! writer (str unit)))))
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-append-fixed-decimal!
  [writer value width]
  (let [text (Long/toString (long value))]
    (dotimes [_ (- width (.length text))]
      (p15-s23-sh07-o-append-scalar-fragment! writer "0"))
    (p15-s23-sh07-o-append-scalar-fragment! writer text))
  writer)

(defn- p15-s23-sh07-o-instant-text
  [value]
  (let [calendar
        (doto (java.util.GregorianCalendar.
               (java.util.TimeZone/getTimeZone "GMT") java.util.Locale/ROOT)
          (.setGregorianChange (java.util.Date. -12219292800000))
          (.setLenient false)
          (.setTime ^java.util.Date value))
        era (.get calendar java.util.Calendar/ERA)
        calendar-year (.get calendar java.util.Calendar/YEAR)
        year (cond
               (and (= era java.util.GregorianCalendar/BC)
                    (= calendar-year 1)) 0
               (= era java.util.GregorianCalendar/AD) calendar-year
               :else nil)]
    (when-not (and year (<= 0 year 9999))
      (throw (ex-info "source oracle instant range"
                      {:reason :unsupported-value-carrier})))
    (let [writer (p15-s23-sh07-o-scalar-writer)]
      (p15-s23-sh07-o-append-scalar-fragment! writer "#inst \"")
      (p15-s23-sh07-o-append-fixed-decimal! writer year 4)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (inc (.get calendar java.util.Calendar/MONTH)) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/DAY_OF_MONTH) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer "T")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/HOUR_OF_DAY) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ":")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/MINUTE) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ":")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/SECOND) 2)
      (p15-s23-sh07-o-append-scalar-fragment! writer ".")
      (p15-s23-sh07-o-append-fixed-decimal!
       writer (.get calendar java.util.Calendar/MILLISECOND) 3)
      (p15-s23-sh07-o-append-scalar-fragment! writer "-00:00\"")
      (p15-s23-sh07-o-finish-scalar writer))))

(defn- p15-s23-sh07-o-uuid-text
  [value]
  (let [digits "0123456789abcdef"
        append-range!
        (fn [writer bits high low]
          (doseq [shift (range high (dec low) -4)]
            (p15-s23-sh07-o-append-scalar-fragment!
             writer
             (str (.charAt digits
                           (int (bit-and 15
                                         (bit-shift-right (long bits)
                                                          shift)))))))
          writer)
        most (.getMostSignificantBits ^java.util.UUID value)
        least (.getLeastSignificantBits ^java.util.UUID value)
        writer (p15-s23-sh07-o-scalar-writer)]
    (p15-s23-sh07-o-append-scalar-fragment! writer "#uuid \"")
    (append-range! writer most 60 32)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer most 28 16)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer most 12 0)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer least 60 48)
    (p15-s23-sh07-o-append-scalar-fragment! writer "-")
    (append-range! writer least 44 0)
    (p15-s23-sh07-o-append-scalar-fragment! writer "\"")
    (p15-s23-sh07-o-finish-scalar writer)))

(defn- p15-s23-sh07-o-scalar-text
  [value]
  (case (p15-s23-sh07-o-scalar-kind value)
    :nil (let [writer (p15-s23-sh07-o-scalar-writer)]
           (p15-s23-sh07-o-append-scalar-fragment! writer "nil")
           (p15-s23-sh07-o-finish-scalar writer))
    :boolean (let [writer (p15-s23-sh07-o-scalar-writer)]
               (p15-s23-sh07-o-append-scalar-fragment!
                writer (if value "true" "false"))
               (p15-s23-sh07-o-finish-scalar writer))
    :integer (p15-s23-sh07-o-integer-text value)
    :ratio (let [writer (p15-s23-sh07-o-scalar-writer)]
             (p15-s23-sh07-o-append-scalar-fragment!
              writer (:text (p15-s23-sh07-o-integer-text
                             (.numerator ^clojure.lang.Ratio value))))
             (p15-s23-sh07-o-append-scalar-fragment! writer "/")
             (p15-s23-sh07-o-append-scalar-fragment!
              writer (:text (p15-s23-sh07-o-integer-text
                             (.denominator ^clojure.lang.Ratio value))))
             (p15-s23-sh07-o-finish-scalar writer))
    :floating (let [number (double value)
                    writer (p15-s23-sh07-o-scalar-writer)]
                (when-not (Double/isFinite number)
                  (throw (ex-info "source oracle floating range"
                                  {:reason :unsupported-value-carrier})))
                (p15-s23-sh07-o-append-scalar-fragment!
                 writer (Double/toString number))
                (p15-s23-sh07-o-finish-scalar writer))
    :string (p15-s23-sh07-o-string-text value)
    :character (p15-s23-sh07-o-character-text value)
    :instant (p15-s23-sh07-o-instant-text value)
    :uuid (p15-s23-sh07-o-uuid-text value)
    :keyword (let [writer (p15-s23-sh07-o-scalar-writer)]
               (p15-s23-sh07-o-append-scalar-fragment! writer ":")
               (when-let [ns-text (namespace value)]
                 (p15-s23-sh07-o-append-scalar-fragment! writer ns-text)
                 (p15-s23-sh07-o-append-scalar-fragment! writer "/"))
               (p15-s23-sh07-o-append-scalar-fragment!
                writer (name value))
               (p15-s23-sh07-o-finish-scalar writer))
    :symbol (let [writer (p15-s23-sh07-o-scalar-writer)]
              (when-let [ns-text (namespace value)]
                (p15-s23-sh07-o-append-scalar-fragment! writer ns-text)
                (p15-s23-sh07-o-append-scalar-fragment! writer "/"))
              (p15-s23-sh07-o-append-scalar-fragment! writer (name value))
              (p15-s23-sh07-o-finish-scalar writer))
    (throw (ex-info "source oracle scalar is unsupported"
                    {:reason :unsupported-value-carrier}))))

;; This Clojure closure is the Attempt-17 independent source oracle only.
;; Producer and Root 6 carry separately authored Gravity implementations; no
;; generic Stage0/Stage2 collection compiler or sort primitive calls this
;; oracle.
(def p15-s23-sh07-canonical-decoration-limits
  {:maximum-nodes 4096
   :maximum-depth 96
   :maximum-collection-width 512
   :maximum-scalar-bytes 32768
   :maximum-output-bytes 262144})

(defn- p15-s23-sh07-decoration-failed [reason]
  {:status :failed :reason reason})

(defn- p15-s23-sh07-active-copy [^java.util.IdentityHashMap active]
  (doto (java.util.IdentityHashMap.) (.putAll active)))

(defn- p15-s23-sh07-insert-by
  [compare-record ordered record]
  (loop [remaining ordered prefix []]
    (if (empty? remaining)
      (conj prefix record)
      (if (neg? (compare-record record (first remaining)))
        (into (conj prefix record) remaining)
        (recur (rest remaining) (conj prefix (first remaining)))))))

(defn- p15-s23-sh07-stable-order-by
  [compare-record records]
  (reduce (partial p15-s23-sh07-insert-by compare-record) [] records))

(declare p15-s23-sh07-canonical-decoration)

(defn- p15-s23-sh07-combine-decorations
  [depth node-budget open separator close decorations]
  (let [node-count
        (inc (reduce + 0 (map :node-occurrences decorations)))
        maximum-depth
        (reduce max depth (map :maximum-depth decorations))]
    (if (> node-count node-budget)
      (p15-s23-sh07-decoration-failed :node-count-limit)
      (let [maximum-output-bytes
            (:maximum-output-bytes
             p15-s23-sh07-canonical-decoration-limits)
            builder (StringBuilder.)
            append!
            (fn [bytes text]
              (let [observation
                    (p15-s23-sh07-observe-utf8-byte-count
                     text bytes maximum-output-bytes)]
                (if (not= :accepted (:status observation))
                  nil
                  (do (.append builder ^String text)
                      (:observed-after observation)))))
            first-bytes (append! 0 open)]
        (if (nil? first-bytes)
          (p15-s23-sh07-decoration-failed :output-byte-limit)
          (loop [remaining decorations bytes first-bytes first? true]
            (if (empty? remaining)
              (if-let [complete-bytes (append! bytes close)]
                {:status :decorated
                 :text (.toString builder)
                 :node-occurrences node-count
                 :maximum-depth maximum-depth
                 :output-utf8-bytes complete-bytes}
                (p15-s23-sh07-decoration-failed :output-byte-limit))
              (let [with-separator
                    (if first? bytes (append! bytes separator))
                    with-text
                    (when with-separator
                      (append! with-separator (:text (first remaining))))]
                (if with-text
                  (recur (rest remaining) with-text false)
                  (p15-s23-sh07-decoration-failed
                   :output-byte-limit))))))))))

(def ^:private p15-s23-sh07-o-c2-carrier-keys
  #{:artifact :c2-reader-artifact :root-adapted-form-id
    :decimal-replay-evidence})

(defn- p15-s23-sh07-o-c2-carrier?
  [carrier]
  (and (map? carrier)
       (= p15-s23-sh07-o-c2-carrier-keys (set (keys carrier)))
       (= :gravity/sh07-oracle-c2-carrier-v1 (:artifact carrier))
       (map? (:decimal-replay-evidence carrier))))

(defn- p15-s23-sh07-o-c2-decoration
  [carrier]
  (try
    (let [artifact (:c2-reader-artifact carrier)
          forms (:form-tree artifact)
          adapted-forms
          (mapv #(-> %
                     (assoc :child-form-ids (:children %))
                     (dissoc :children))
                forms)
          forms-by-id (into {} (map (juxt :form-id identity)) adapted-forms)
          root-id (:root-adapted-form-id carrier)
          replays (:decimal-replay-evidence carrier)
          limits p15-s23-sh07-canonical-decoration-limits]
      (when-not
       (and (= (:artifact-id artifact) (c2-reader-artifact-id artifact))
            (sh03-reader-final-decimal-occurrence-projection-valid? artifact)
            (= (count adapted-forms) (count forms-by-id))
            (map? (get forms-by-id root-id)))
        (throw (ex-info "source oracle C2 carrier is unauthenticated"
                        {:reason :source-integrity-mismatch})))
      (letfn
          [(decorate-scalar [form depth]
             (let [kind (:kind form)
                   rendered
                   (if (= :decimal kind)
                     (let [source-evidence
                           {:c2-reader-artifact artifact
                            :adapted-form-id (:form-id form)
                            :adapted-token-id (:open-token form)}
                           source-occurrence
                           (p15-s23-sh07-o-decimal-occurrence source-evidence)
                           source-entry
                           (:semantic-value-entry source-occurrence)
                           source-key (:semantic-key source-entry)
                           _
                           (when-not
                            (p15-s23-sh07-o-decimal-authentic-key? source-key)
                             (throw
                              (ex-info
                               "source State C has no exact current-C2 key"
                               {:reason
                                :unsupported-decimal-readable-spelling})))
                           key source-key
                           replay-evidence
                           (get replays [(:form-id form) (:open-token form)])
                           replay-occurrence
                           (p15-s23-sh07-o-decimal-occurrence replay-evidence)]
                       (when (= (p15-s23-sh07-o-decimal-occurrence-identity
                                 source-evidence source-occurrence)
                                (p15-s23-sh07-o-decimal-occurrence-identity
                                 replay-evidence replay-occurrence))
                         (throw (ex-info "source oracle replay is not fresh"
                                         {:reason
                                          :unsupported-decimal-readable-spelling})))
                       (let [text (p15-s23-sh07-o-decimal-decoration
                                   key replay-evidence)
                             observation
                             (p15-s23-sh07-observe-utf8-byte-count
                              text 0 (:maximum-scalar-bytes limits))]
                         (when-not (= :accepted (:status observation))
                           (throw (ex-info "decimal scalar G20 rejection"
                                           {:reason :scalar-byte-limit})))
                         {:text text
                          :output-utf8-bytes
                          (:observed-after observation)}))
                     (p15-s23-sh07-o-scalar-text (:value form)))
                   text (:text rendered)]
               {:status :decorated :text text :node-occurrences 1
                :maximum-depth depth
                :output-utf8-bytes (:output-utf8-bytes rendered)}))
           (decorate-form [form-id depth node-budget active]
             (if (<= node-budget 0)
               (p15-s23-sh07-decoration-failed :node-count-limit)
               (if (> depth (:maximum-depth limits))
                 (p15-s23-sh07-decoration-failed :depth-limit)
                 (let [form (get forms-by-id form-id)
                       collection-kind (or (:collection-kind form)
                                           (when (contains? #{:vector :list
                                                              :set :map}
                                                            (:kind form))
                                             (:kind form)))
                       child-ids (:child-form-ids form)]
                   (if-not form
                     (p15-s23-sh07-decoration-failed
                      :source-integrity-mismatch)
                     (if-not collection-kind
                       (decorate-scalar form depth)
                       (if (contains? active form-id)
                         (p15-s23-sh07-decoration-failed :cyclic-value)
                         (if (> (count child-ids)
                                (:maximum-collection-width limits))
                           (p15-s23-sh07-decoration-failed
                            :collection-width-limit)
                           (let [next-active (conj active form-id)
                                 decorated
                                 (mapv #(decorate-form
                                         % (inc depth) (dec node-budget)
                                         next-active)
                                       child-ids)
                                 failed? (some #(not= :decorated (:status %))
                                               decorated)]
                             (if failed?
                               (p15-s23-sh07-decoration-failed
                                (if (contains? #{:set :map} collection-kind)
                                  :unordered-decoration-failure
                                  (:reason (first
                                            (filter #(not= :decorated
                                                           (:status %))
                                                    decorated)))))
                               (case collection-kind
                                 :vector
                                 (p15-s23-sh07-combine-decorations
                                  depth node-budget "[" " " "]" decorated)
                                 :list
                                 (p15-s23-sh07-combine-decorations
                                  depth node-budget "(" " " ")" decorated)
                                 :set
                                 (let [ordered
                                       (p15-s23-sh07-stable-order-by
                                        #(p15-s23-sh07-canonical-text-compare
                                          (:text %1) (:text %2))
                                        decorated)]
                                   (if (some true?
                                             (map #(= (:text %1) (:text %2))
                                                  ordered (rest ordered)))
                                     (p15-s23-sh07-decoration-failed
                                      :source-integrity-mismatch)
                                     (p15-s23-sh07-combine-decorations
                                      depth node-budget "#{" " " "}" ordered)))
                                 :map
                                 (if (odd? (count decorated))
                                   (p15-s23-sh07-decoration-failed
                                    :source-integrity-mismatch)
                                   (let [entries
                                         (mapv (fn [[key-result value-result]]
                                                 {:key-result key-result
                                                  :value-result value-result})
                                               (partition 2 decorated))
                                         ordered
                                         (p15-s23-sh07-stable-order-by
                                          (fn [left right]
                                            (let [key-order
                                                  (p15-s23-sh07-canonical-text-compare
                                                   (get-in left
                                                           [:key-result :text])
                                                   (get-in right
                                                           [:key-result :text]))]
                                              (if (zero? key-order)
                                                (p15-s23-sh07-canonical-text-compare
                                                 (get-in left
                                                         [:value-result :text])
                                                 (get-in right
                                                         [:value-result :text]))
                                                key-order)))
                                          entries)
                                         collisions?
                                         (some true?
                                               (map #(= (get-in %1
                                                                [:key-result
                                                                 :text])
                                                        (get-in %2
                                                                [:key-result
                                                                 :text]))
                                                    ordered (rest ordered)))]
                                     (if collisions?
                                       (p15-s23-sh07-decoration-failed
                                        :source-integrity-mismatch)
                                       (p15-s23-sh07-combine-decorations
                                        depth node-budget "{" ", " "}"
                                        (mapv
                                         (fn [entry]
                                           {:text
                                            (str (get-in entry
                                                         [:key-result :text])
                                                 " "
                                                 (get-in entry
                                                         [:value-result :text]))
                                            :node-occurrences
                                            (+ (:node-occurrences
                                                (:key-result entry))
                                               (:node-occurrences
                                                (:value-result entry)))
                                            :maximum-depth
                                            (max (:maximum-depth
                                                  (:key-result entry))
                                                 (:maximum-depth
                                                  (:value-result entry)))})
                                         ordered))))))))))))))))]
        (decorate-form root-id 0 (:maximum-nodes limits) #{})))
    (catch clojure.lang.ExceptionInfo error
      (p15-s23-sh07-decoration-failed
       (or (:reason (ex-data error)) :contained-decoration-failure)))
    (catch Exception _
      (p15-s23-sh07-decoration-failed :contained-decoration-failure))))

(defn- p15-s23-sh07-probe-child
  [source-path child depth active node-budget decimal-occurrences]
  (try
    (p15-s23-sh07-canonical-decoration
     source-path child depth (p15-s23-sh07-active-copy active) node-budget
     decimal-occurrences)
    (catch clojure.lang.ExceptionInfo error
      (p15-s23-sh07-decoration-failed
       (or (:reason (ex-data error)) :contained-decoration-failure)))
    (catch Exception _
      (p15-s23-sh07-decoration-failed :contained-decoration-failure))))

(defn p15-s23-sh07-canonical-decoration
  ([source-path value]
   (p15-s23-sh07-canonical-decoration
    source-path value 0 (java.util.IdentityHashMap.)
    (:maximum-nodes p15-s23-sh07-canonical-decoration-limits) []))
  ([source-path value decimal-occurrences]
   (if (p15-s23-sh07-o-c2-carrier? decimal-occurrences)
     (p15-s23-sh07-o-c2-decoration decimal-occurrences)
     (p15-s23-sh07-canonical-decoration
      source-path value 0 (java.util.IdentityHashMap.)
      (:maximum-nodes p15-s23-sh07-canonical-decoration-limits)
      decimal-occurrences)))
  ([source-path value depth ^java.util.IdentityHashMap active]
   (p15-s23-sh07-canonical-decoration
    source-path value depth active
    (:maximum-nodes p15-s23-sh07-canonical-decoration-limits) []))
  ([source-path value depth ^java.util.IdentityHashMap active node-budget]
   (p15-s23-sh07-canonical-decoration
    source-path value depth active node-budget []))
  ([source-path value depth ^java.util.IdentityHashMap active node-budget
    decimal-occurrences]
   (let [{:keys [maximum-depth maximum-collection-width
                 maximum-scalar-bytes]}
         p15-s23-sh07-canonical-decoration-limits]
     (if (<= node-budget 0)
       (p15-s23-sh07-decoration-failed :node-count-limit)
       (if (> depth maximum-depth)
       (p15-s23-sh07-decoration-failed :depth-limit)
       (let [host-decimal? (instance? java.math.BigDecimal value)
             kind
             (try
               ;; A host decimal does not identify a source occurrence.  G19
               ;; admission is available only through the producer/oracle
               ;; entry points that require authenticated adapted form/token
               ;; identities.
               (when-not host-decimal?
                 (p15-s23-sh07-o-scalar-kind value))
               (catch Exception _ nil))
             collection? (contains? #{:vector :list :map :set} kind)]
         (if (= kind nil)
           (p15-s23-sh07-decoration-failed
            (if host-decimal?
              :unsupported-decimal-readable-spelling
              :unsupported-value-carrier))
           (if-not collection?
             (try
               (let [rendered (p15-s23-sh07-o-scalar-text value)]
                 {:status :decorated :text (:text rendered)
                  :node-occurrences 1
                  :maximum-depth depth
                  :output-utf8-bytes (:output-utf8-bytes rendered)})
               (catch clojure.lang.ExceptionInfo error
                 (p15-s23-sh07-decoration-failed
                  (or (:reason (ex-data error))
                      :contained-decoration-failure)))
               (catch Exception _
                 (p15-s23-sh07-decoration-failed
                  :contained-decoration-failure)))
             (if (.containsKey active value)
               (p15-s23-sh07-decoration-failed :cyclic-value)
               (let [width (count value)]
                 (if (> width maximum-collection-width)
                   (p15-s23-sh07-decoration-failed
                    :collection-width-limit)
                   (let [next-active (p15-s23-sh07-active-copy active)]
                     (.put next-active value Boolean/TRUE)
                     (case kind
                       :vector
                       (loop [remaining (vec value) decorations [] used 1]
                         (if (empty? remaining)
                           (p15-s23-sh07-combine-decorations
                            depth node-budget "[" " " "]" decorations)
                           (let [decorated
                                 (p15-s23-sh07-probe-child
                                  source-path (first remaining)
                                  (inc depth) next-active
                                  (- node-budget used) decimal-occurrences)]
                             (if (= :decorated (:status decorated))
                               (recur (rest remaining)
                                      (conj decorations decorated)
                                      (+ used (:node-occurrences decorated)))
                               decorated))))

                       :list
                       (loop [remaining (seq value) decorations [] used 1]
                         (if (nil? remaining)
                           (p15-s23-sh07-combine-decorations
                            depth node-budget "(" " " ")" decorations)
                           (let [decorated
                                 (p15-s23-sh07-probe-child
                                 source-path (first remaining)
                                  (inc depth) next-active
                                  (- node-budget used) decimal-occurrences)]
                             (if (= :decorated (:status decorated))
                               (recur (next remaining)
                                      (conj decorations decorated)
                                      (+ used (:node-occurrences decorated)))
                               decorated))))

                       :set
                       (let [results
                             (mapv
                              #(p15-s23-sh07-probe-child
                                source-path % (inc depth) next-active
                                (dec node-budget) decimal-occurrences)
                              (vec value))
                             any-failed?
                             (reduce
                              (fn [failed? result]
                                (or failed?
                                    (not= :decorated (:status result))))
                              false results)]
                         (if any-failed?
                           (p15-s23-sh07-decoration-failed
                            :unordered-decoration-failure)
                           (let [ordered
                                 (p15-s23-sh07-stable-order-by
                                  (fn [left right]
                                    (p15-s23-sh07-canonical-text-compare
                                     (:text left) (:text right)))
                                  results)]
                             (if (some true?
                                       (map #(= (:text %1) (:text %2))
                                            ordered (rest ordered)))
                               (p15-s23-sh07-decoration-failed
                                :source-integrity-mismatch)
                               (p15-s23-sh07-combine-decorations
                                depth node-budget "#{" " " "}" ordered)))))

                       :map
                       (let [results
                             (mapv
                              (fn [entry]
                                {:key-result
                                 (p15-s23-sh07-probe-child
                                  source-path (key entry)
                                  (inc depth) next-active (dec node-budget)
                                  decimal-occurrences)
                                 :value-result
                                 (p15-s23-sh07-probe-child
                                  source-path (val entry)
                                  (inc depth) next-active (dec node-budget)
                                  decimal-occurrences)
                                 :key (key entry)})
                              (vec value))
                             any-failed?
                             (reduce
                              (fn [failed? entry]
                                (or failed?
                                    (not= :decorated
                                          (get-in entry [:key-result :status]))
                                    (not= :decorated
                                          (get-in entry
                                                  [:value-result :status]))))
                              false results)]
                         (if any-failed?
                           (p15-s23-sh07-decoration-failed
                            :unordered-decoration-failure)
                           (let [ordered
                                 (p15-s23-sh07-stable-order-by
                                  (fn [left right]
                                    (let [key-order
                                          (p15-s23-sh07-canonical-text-compare
                                           (get-in left [:key-result :text])
                                           (get-in right [:key-result :text]))]
                                      (if (zero? key-order)
                                        (p15-s23-sh07-canonical-text-compare
                                         (get-in left [:value-result :text])
                                         (get-in right [:value-result :text]))
                                        key-order)))
                                  results)
                                 key-collision?
                                 (some
                                  true?
                                  (map
                                   #(and
                                     (= (get-in %1 [:key-result :text])
                                        (get-in %2 [:key-result :text]))
                                     (not= (:key %1) (:key %2)))
                                   ordered (rest ordered)))]
                             (if key-collision?
                               (p15-s23-sh07-decoration-failed
                                :source-integrity-mismatch)
                               (let [entries
                                     (mapv
                                      (fn [entry]
                                        (let [key-result (:key-result entry)
                                              value-result
                                              (:value-result entry)
                                              pair
                                              (p15-s23-sh07-combine-decorations
                                               (min (:maximum-depth key-result)
                                                    (:maximum-depth value-result))
                                               (+ 1
                                                  (:node-occurrences key-result)
                                                  (:node-occurrences value-result))
                                               "" " " ""
                                               [key-result value-result])]
                                          (if (= :decorated (:status pair))
                                            (update pair :node-occurrences dec)
                                            pair)))
                                      ordered)]
                                 (if-let [failed
                                          (first
                                           (filter #(not= :decorated
                                                          (:status %))
                                                   entries))]
                                   failed
                                   (p15-s23-sh07-combine-decorations
                                    depth node-budget "{" ", " "}"
                                    entries))))))))))))))))))))
