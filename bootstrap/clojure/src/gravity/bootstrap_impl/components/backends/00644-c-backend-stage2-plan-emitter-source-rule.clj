(defn- p15-s23-h6-intake-fail! [reason field limit]
  (throw (ex-info "Retained source intake rejected"
                  {:id "SH07-B51-00945-GOVERNED-READ" :reason reason
                   :details {:ordinal nil :path nil :field field
                             :owner-id nil :limit limit}})))

(defn- p15-s23-h6-origin-source!
  "Bound one retained entry and join bytes/text; does not authenticate its proof."
  [path descriptor proof claimed-text]
  (let [plain-map? #(contains? #{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap}
                              (class %))
        reject #(p15-s23-h6-intake-fail! %1 %2 nil)]
    (when-not (and (plain-map? descriptor) (nil? (meta descriptor))
                   (plain-map? proof) (nil? (meta proof)))
      (reject :origin-shape-bound nil))
    (let [values (:blob-bytes proof) n (when (= clojure.lang.PersistentVector (class values))
                                           (count values))]
      ;; 16MiB is the origin raw-vector width, not plan fuel or aggregate authority.
      (when-not (and n (nil? (meta values)) (<= n 16777216))
        (p15-s23-h6-intake-fail! :origin-shape-bound [:blob-bytes]
                               {:name :max-source-entry-bytes :maximum 16777216 :observed n}))
      (when-not (every? #(and (integer? %) (<= 0 % 255)) values)
        (reject :origin-shape-bound [:blob-bytes]))
      (when-not (and (integer? (:byte-count descriptor)) (= n (:byte-count descriptor)))
        (reject :origin-source-binding [:byte-count]))
      (when-not (and (string? path) (<= (count path) 4096)
                     (string? (:path descriptor)) (<= (count (:path descriptor)) 4096)
                     (string? (:path proof)) (<= (count (:path proof)) 4096)
                     (= path (:path descriptor) (:path proof))
                     (let [raw-path (.getBytes ^String path java.nio.charset.StandardCharsets/UTF_8)]
                       (and (<= (alength raw-path) 4096)
                            (= path (String. raw-path java.nio.charset.StandardCharsets/UTF_8)))))
        (reject :origin-source-binding [:path]))
      (when-not (and (string? claimed-text) (<= (count claimed-text) 16777216)
                     (string? (:raw-sha256 descriptor))
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
  ;; Reserve lexical nodes before the host reader, including discarded forms
  ;; and abbreviation expansion. Text units have their separate 16MiB cap.
  ;; This finite retained seed subset excludes ambient namespace/alias resolution;
  ;; it defines no Gravity language grammar or profile.
  (let [fail #(p15-s23-h6-intake-fail! :origin-reader-contract nil
                                     {:name %1 :maximum %2 :observed %3})
        finish (fn [frames]
                 (loop [fs frames]
                   (if (= :prefix (:kind (peek fs)))
                     (if (= 1 (:left (peek fs))) (recur (pop fs))
                         (update-in fs [(dec (count fs)) :left] dec)) fs)))
        separator? #(or (Character/isWhitespace ^char %) (contains? #{\, \; \" \( \) \[ \] \{ \} \' \` \~ \@ \^} %))]
    (when (> (.length text) 16777216) (fail :max-source-text-units 16777216 (.length text)))
    (loop [i 0 mode :space escaped? false frames [{:kind :root :width 0}] nodes 1]
      (when (> nodes 524288) (fail :max-plan-nodes 524288 nodes))
      (when (> (dec (count frames)) 512) (fail :max-plan-depth 512 (dec (count frames))))
      (let [frame (peek frames) width (:width frame)
            width (if (= :map (:kind frame)) (quot (inc width) 2) width)]
        (when (> width 131072) (fail :max-plan-width 131072 width)))
      (when (< i (.length text))
        (let [c (.charAt text i) next-c (when (< (inc i) (.length text)) (.charAt text (inc i)))]
          (cond
            (= mode :comment) (recur (inc i) (if (contains? #{\newline \return} c) :space :comment) false frames nodes)
            (= mode :string) (cond escaped? (recur (inc i) :string false frames nodes)
                                      (= c \\) (recur (inc i) :string true frames nodes)
                                      (= c \") (recur (inc i) :space false (finish frames) nodes)
                                      :else (recur (inc i) :string false frames nodes))
            (= mode :char-first) (recur (inc i) :token false frames nodes)
            (= mode :token) (if (separator? c) (recur i :space false (finish frames) nodes)
                               (recur (inc i) :token false frames nodes))
            (or (Character/isWhitespace c) (= c \,)) (recur (inc i) :space false frames nodes)
            (= c \;) (recur (inc i) :comment false frames nodes)
            (contains? #{\) \] \}} c) (recur (inc i) :space false (finish (if (> (count frames) 1) (pop frames) frames)) nodes)
            :else
            (let [_ (when (> (count frames) 512) (fail :max-plan-depth 512 (count frames)))
                  _ (when (or (= c \`) (and (= c \:) (= next-c \:)))
                      (p15-s23-h6-intake-fail! :origin-reader-contract nil nil))
                  frames (update-in frames [(dec (count frames)) :width] inc)
                  dispatch? (= c \#)
                  _ (when (and dispatch? (not (contains? #{\{ \' \_} next-c)))
                      (p15-s23-h6-intake-fail! :origin-reader-contract nil nil))
                  prefix? (or (contains? #{\' \` \~ \@ \^} c)
                              (and dispatch? (contains? #{\' \_} next-c)))
                  open? (or (contains? #{\( \[ \{} c)
                            (and dispatch? (= \{ next-c)))
                  cost (if prefix? 2 1)
                  frames (cond prefix? (conj frames {:kind :prefix :left (if (= c \^) 2 1) :width 0})
                               open? (conj frames {:kind (if (= c \{) :map :collection)
                                                   :width 0}) :else frames)]
              (recur (+ i (if (and dispatch? (or prefix? open?)) 2 1))
                     (cond (or prefix? open?) :space (= c \") :string (= c \\) :char-first :else :token)
                     false frames (+ nodes cost)))))))))

(defn- p15-s23-h6-source-forms-preflight! [forms]
  ;; Reader metadata is retained and bounded too; no recursive host walk.
  (let [containers #{clojure.lang.PersistentVector clojure.lang.PersistentArrayMap
                     clojure.lang.PersistentHashMap clojure.lang.PersistentHashSet
                     clojure.lang.PersistentList clojure.lang.PersistentList$EmptyList clojure.lang.Cons}
        scalars #{java.lang.String java.lang.Character java.lang.Long java.lang.Integer
                  java.lang.Double java.lang.Float java.lang.Short java.lang.Byte java.lang.Boolean
                  java.math.BigInteger java.math.BigDecimal clojure.lang.BigInt clojure.lang.Ratio
                  clojure.lang.Keyword clojure.lang.Symbol}]
    (loop [pending [[forms 0]] nodes 0]
      (when-let [[value depth] (peek pending)]
        (let [reject #(p15-s23-h6-intake-fail! :origin-reader-contract nil
                                            {:name %1 :maximum %2 :observed %3})
              nodes (inc nodes) cls (class value)]
          (when (> nodes 524288) (reject :max-plan-nodes 524288 nodes))
          (when (> depth 512) (reject :max-plan-depth 512 depth))
          (when-not (or (nil? value) (contains? containers cls) (contains? scalars cls))
            (p15-s23-h6-intake-fail! :origin-reader-contract nil nil))
          (let [children (if (contains? containers cls)
                           (if (map? value) (mapcat identity value) value) [])
                pending (pop pending)
                pending (if (and (instance? clojure.lang.IMeta value) (some? (meta value)))
                          (conj pending [(meta value) (inc depth)]) pending)]
            (recur (loop [xs (seq children) width 0 stack pending]
                     (if (nil? xs) stack
                       (let [width (inc width)]
                         (when (and (or (list? value) (= cls clojure.lang.Cons))
                                    (not (contains? containers (class xs))))
                           (p15-s23-h6-intake-fail! :origin-reader-contract nil nil))
                         (let [observed (if (map? value) (quot (inc width) 2) width)]
                           (when (> observed 131072) (reject :max-plan-width 131072 observed)))
                         (when (> (+ nodes (count stack) 1) 524288)
                           (reject :max-plan-nodes 524288 (+ nodes (count stack) 1)))
                         (recur (next xs) width (conj stack [(first xs) (inc depth)]))))) nodes)))))))

(defn- p15-s23-h6-retained-source-form-records!
  "Fresh finite-subset seed parsing; origin proof and aggregate admission remain external."
  [path descriptor proof claimed-text]
  (let [source (p15-s23-h6-origin-source! path descriptor proof claimed-text)
        _ (p15-s23-h6-seed-text-preflight! (:source-text source))
        records (binding [*authenticated-source-form-records* nil *read-eval* false
                          *data-readers* {} *default-data-reader-fn* nil]
                  (read-source-form-records path (:source-text source)))
        forms (mapv :form records)]
    (p15-s23-h6-source-forms-preflight! forms)
    (assoc source :records records :forms forms)))

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
            (p15-s23-compiler-source-form-record-from-text
             compiler-source (:source-text pinned-source))
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
