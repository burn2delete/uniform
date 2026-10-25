(defn- p15-s23-h6-intake-fail! [reason field]
  (throw (ex-info "Retained source intake rejected"
                  {:id "SH07-B51-00945-GOVERNED-READ" :reason reason
                   :details {:ordinal nil :path nil :field field :owner-id nil}})))

(declare p15-s23-trusted-carrier-validation)

(defn- p15-s23-h6-origin-source!
  "Validate one retained entry and join bytes/text; does not authenticate its proof."
  [path descriptor proof claimed-text]
  (let [plain-map? #(contains? #{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap}
                              (class %))
        reject #(p15-s23-h6-intake-fail! %1 %2)]
    (when-not (and (plain-map? descriptor) (nil? (meta descriptor))
                   (plain-map? proof) (nil? (meta proof))
                   (= :passed (:status (p15-s23-trusted-carrier-validation
                                        [descriptor proof] :default-only))))
      (reject :origin-shape-bound nil))
    (let [values (:blob-bytes proof)
          n (when (= clojure.lang.PersistentVector (class values)) (count values))]
      (when-not (and n (nil? (meta values))
                     (every? #(and (integer? %) (<= 0 % 255)) values))
        (reject :origin-shape-bound [:blob-bytes]))
      (when-not (and (integer? (:byte-count descriptor)) (= n (:byte-count descriptor)))
        (reject :origin-source-binding [:byte-count]))
      (when-not (and (string? path) (string? (:path descriptor)) (string? (:path proof))
                     (= path (:path descriptor) (:path proof))
                     (let [raw-path (.getBytes ^String path java.nio.charset.StandardCharsets/UTF_8)]
                       (= path (String. raw-path java.nio.charset.StandardCharsets/UTF_8))))
        (reject :origin-source-binding [:path]))
      (when-not (and (string? claimed-text) (string? (:raw-sha256 descriptor))
                     (boolean (re-matches #"sha256:[0-9a-f]{64}" (:raw-sha256 descriptor))))
        (reject :origin-source-binding [:raw-sha256]))
      (let [raw (byte-array n)
            _ (dotimes [i n] (aset-byte raw i (unchecked-byte (nth values i))))
            text (try (str (.decode (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                                              (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                                              (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
                                    (java.nio.ByteBuffer/wrap raw)))
                      (catch java.nio.charset.CharacterCodingException _
                        (reject :origin-source-binding [:blob-bytes])))
            encoded (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
        (when-not (and (= n (alength encoded)) (java.util.Arrays/equals raw encoded)
                       (= text claimed-text)
                       (= (:raw-sha256 descriptor) (str "sha256:" (sha256-bytes-hex raw))))
          (reject :origin-source-binding [:raw-sha256]))
        {:source-path path :source-text text :source-byte-count n
         :source-content-hash (:raw-sha256 descriptor)}))))

(defn- p15-s23-h6-seed-text-preflight! [^String text]
  ;; Reject ambient namespace/alias expansion before the host reader. Grammar
  ;; and delimiter errors remain the reader's responsibility; no project quota.
  (let [separator? #(or (Character/isWhitespace ^char %)
                        (contains? #{\, \; \" \( \) \[ \] \{ \} \' \` \~ \@ \^} %))]
    (loop [i 0 mode :space escaped? false]
      (when (< i (.length text))
        (let [c (.charAt text i) next-c (when (< (inc i) (.length text)) (.charAt text (inc i)))]
          (cond
            (= mode :comment)
            (recur (inc i) (if (contains? #{\newline \return} c) :space :comment) false)
            (= mode :string)
            (cond escaped? (recur (inc i) :string false)
                  (= c \\) (recur (inc i) :string true)
                  (= c \") (recur (inc i) :space false)
                  :else (recur (inc i) :string false))
            (= mode :char-first) (recur (inc i) :token false)
            (= mode :token) (if (separator? c) (recur i :space false)
                               (recur (inc i) :token false))
            (or (Character/isWhitespace c) (= c \,)) (recur (inc i) :space false)
            (= c \;) (recur (inc i) :comment false)
            (contains? #{\) \] \}} c) (recur (inc i) :space false)
            :else
            (let [dispatch? (= c \#)
                  _ (when (or (= c \`) (and (= c \:) (= next-c \:))
                              (and dispatch? (not (contains? #{\{ \' \_} next-c))))
                      (p15-s23-h6-intake-fail! :origin-reader-contract nil))
                  prefix? (or (contains? #{\' \~ \@ \^} c)
                              (and dispatch? (contains? #{\' \_} next-c)))
                  open? (or (contains? #{\( \[ \{} c) (and dispatch? (= \{ next-c)))]
              (recur (+ i (if (and dispatch? (or prefix? open?)) 2 1))
                     (cond (or prefix? open?) :space (= c \") :string
                           (= c \\) :char-first :else :token)
                     false))))))))

(defn- p15-s23-h6-source-forms-preflight! [forms]
  ;; Retain native reader metadata, reject foreign callbacks and true ancestor
  ;; cycles, and revisit shared immutable values on each independent path.
  (let [containers #{clojure.lang.PersistentVector clojure.lang.PersistentArrayMap
                     clojure.lang.PersistentHashMap clojure.lang.PersistentHashSet
                     clojure.lang.PersistentList clojure.lang.PersistentList$EmptyList clojure.lang.Cons}
        scalars #{java.lang.String java.lang.Character java.lang.Long java.lang.Integer
                  java.lang.Double java.lang.Float java.lang.Short java.lang.Byte java.lang.Boolean
                  java.math.BigInteger java.math.BigDecimal clojure.lang.BigInt clojure.lang.Ratio
                  clojure.lang.Keyword clojure.lang.Symbol}
        active (java.util.IdentityHashMap.)]
    (loop [pending [[forms false]]]
      (when-let [[value exit?] (peek pending)]
        (let [pending (pop pending)]
          (if exit?
            (do (.remove active value) (recur pending))
            (let [cls (class value)]
              (when-not (or (nil? value) (contains? containers cls) (contains? scalars cls))
                (p15-s23-h6-intake-fail! :origin-reader-contract nil))
              ;; This host-reader preflight has no compiler plan. Inspect only
              ;; exact native carriers; do not invent a plan to call the
              ;; compiler-artifact builtin header adapter.
              (when (or (and (map? value)
                             (nil? (p15-s23-stage2-native-map-extent value)))
                        (and (= clojure.lang.PersistentHashSet cls)
                             (let [backing (p15-s23-stage2-native-hashset-backing value)]
                               (or (not (contains? #{clojure.lang.PersistentArrayMap
                                                     clojure.lang.PersistentHashMap}
                                                   (class backing)))
                                   (nil? (p15-s23-stage2-native-map-extent backing))))))
                (p15-s23-h6-intake-fail! :origin-reader-contract nil))
              (let [metadata (when (instance? clojure.lang.IMeta value) (meta value))
                    branch? (or (contains? containers cls) (some? metadata))]
                (when (and branch? (.containsKey active value))
                  (p15-s23-h6-intake-fail! :origin-reader-contract nil))
                (when branch? (.put active value true))
                (let [children (cond (= clojure.lang.PersistentList cls)
                                     [(.first ^clojure.lang.PersistentList value)
                                      (.next ^clojure.lang.PersistentList value)]
                                     (= clojure.lang.Cons cls)
                                     [(.first ^clojure.lang.Cons value)
                                      (.more ^clojure.lang.Cons value)]
                                     (= clojure.lang.PersistentList$EmptyList cls) []
                                     (map? value) (mapcat identity value)
                                     (contains? containers cls) value :else [])
                      pending (if branch? (conj pending [value true]) pending)
                      pending (if (some? metadata) (conj pending [metadata false]) pending)]
                  (recur (reduce #(conj %1 [%2 false]) pending children)))))))))))

(defn- p15-s23-h6-fresh-source-form-records! [path text]
  (let [_ (p15-s23-h6-seed-text-preflight! text)
        records (binding [*authenticated-source-form-records* nil *read-eval* false
                          *data-readers* {} *default-data-reader-fn* nil]
                  (read-source-form-records path text))
        forms (mapv :form records)]
    (p15-s23-h6-source-forms-preflight! forms)
    {:source-text text :records records :forms forms}))

(defn- p15-s23-h6-retained-source-form-records!
  "Fresh finite-subset seed parsing; origin proof and aggregate admission remain external."
  [path descriptor proof claimed-text]
  (let [source (p15-s23-h6-origin-source! path descriptor proof claimed-text)]
    (merge source (p15-s23-h6-fresh-source-form-records! path (:source-text source)))))

(defn c-backend-stage2-plan-emitter-source-rule!
  "Load and validate the Gravity-authored P15-S23 stage2 plan-emitter rule.

  Runtime-derived C lowering is deliberately bound to this source rule rather
  than silently reusing the Clojure stage0 plan emitter.  The returned rule
  hash is path-neutral and is carried into manifests/provenance so the exact
  Gravity rule set used for lowering is auditable."
  [source-path target]
  (let [compiler-source (c-backend-resolve-p15-s23-compiler-source-path)]
    (when-not (.isFile (java.io.File. compiler-source))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q001"
       compiler-source
       nil
       {:missing-fields [:compiler-source]
        :requested-source source-path
        :target target
        :missing-fact :stage2-plan-emitter-source}))
    (let [pinned-source
          (p15-s23-stage2-compiler-pinned-source!
           compiler-source source-path target "P15S23Q001"
           p15-s23-stage2-plan-emitter-fail!)
          source-data
          (try
            ;; The existing fixed compiler byte/hash pin authenticates this route.
            (let [data (p15-s23-h6-fresh-source-form-records!
                        compiler-source (:source-text pinned-source))]
              (validate-ns-syntax! compiler-source (:forms data))
              (assoc data :module (parse-module compiler-source (:forms data))))
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-plan-emitter-source
                :cause-message (.getMessage ex)})))
          emitter
          (try
            (p15-s23-compiler-def-value compiler-source
                                         (:forms source-data)
                                         'p15-s23-stage2-plan-emitter)
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-plan-emitter-definition
                :cause-message (.getMessage ex)})))]
      (when-not (map? emitter)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q001"
         compiler-source
         emitter
         {:requested-source source-path
          :target target
          :missing-fields [:stage2-plan-emitter]
          :missing-fact :stage2-plan-emitter-definition}))
      (let [rule-record (p15-s23-stage2-plan-emitter-rule-record emitter)]
        (when-not (= :complete (:status rule-record))
          (p15-s23-stage2-plan-emitter-fail!
           "P15S23Q002"
           compiler-source
           rule-record
           {:requested-source source-path
            :target target
            :missing-fact :stage2-plan-emitter-rule-set}))
        {:emitter emitter
         :source-path compiler-source
         :rule-record rule-record
         :source-rule-hash
         (str "sha256:"
              (sha256-hex
               (pr-str (c-backend-canonical-value emitter))))}))))
