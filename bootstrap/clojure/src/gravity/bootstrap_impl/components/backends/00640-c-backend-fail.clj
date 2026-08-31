
(declare c-backend-fail!)

(defn c-backend-output-path-allowed?
  "Keep generated target artifacts inside the repository target tree or a
  caller-owned temporary directory.  Relative one-segment paths are retained
  for parity with the existing bootstrap command contract; absolute paths are
  accepted only below the host temporary directory used by tests/tools."
  [path]
  (when (string? path)
    (let [file (java.io.File. path)
          segments (vec (remove str/blank?
                               (str/split path #"/+")))
          canonical (try (.getCanonicalPath file)
                         (catch Exception _ nil))
          temp-root (try (.getCanonicalPath
                          (java.io.File. (System/getProperty "java.io.tmpdir")))
                         (catch Exception _ nil))]
      (and (not (str/blank? path))
           (not-any? #{".."} segments)
           (or (and (not (.isAbsolute file))
                    (or (str/starts-with? path "target/")
                        (= 1 (count segments))))
               (and (.isAbsolute file)
                    canonical temp-root
                    (or (= canonical temp-root)
                        (str/starts-with?
                         canonical
                         (str temp-root java.io.File/separator)))))))))

(defn c-backend-validate-output-paths!
  [source-path target paths]
  (doseq [[kind path] paths]
    (when (and path (not (c-backend-output-path-allowed? path)))
      (c-backend-fail! "C14-INPUT"
                       "C backend output path is outside the declared target roots"
                       source-path target nil
                       {:output-kind kind
                        :output-path path
                        :allowed-output-roots ["target/" "<current-directory>"
                                               (System/getProperty "java.io.tmpdir")]
                        :missing-fact :output-path-containment
                        :remediation "Use target/ or a caller-owned temporary directory for C backend outputs."}))))

(defn c-backend-canonical-value
  "Canonicalize artifact inputs without allowing host map/set iteration order
  to influence hashes or emitted manifests."
  [value]
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[k v]] [(c-backend-canonical-value k)
                            (c-backend-canonical-value v)]))
          value)
    (set? value)
    (vec (sort-by pr-str (map c-backend-canonical-value value)))
    (vector? value)
    (mapv c-backend-canonical-value value)
    (seq? value)
    (mapv c-backend-canonical-value value)
    :else value))

(defn c-backend-instruction-children
  [instruction]
  (case (:op instruction)
    :println (:args instruction)
    :do (:body instruction)
    :if [(:test instruction) (:then instruction) (:else instruction)]
    :let (concat (map :expr (:bindings instruction)) (:body instruction))
    :builtin-call (:args instruction)
    :function-call (:args instruction)
    :vector-literal (:items instruction)
    :set-literal (:items instruction)
    :map-literal (mapcat (fn [{:keys [key value]}] [key value])
                          (:entries instruction))
    []))

(defn c-backend-fail!
  [id message source-path target subject extra]
  (fail! id message
         (merge {:severity :error
                 :stage :c-backend-lowering
                 :diagnostic-family (if (str/starts-with? id "C14")
                                      :c14-target-lowering
                                      :b2-c-backend)
                 :backend :c
                 :target target
                 :source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :primary {:span (or (:source-span subject)
                                     (source-span source-path 0))}
                 :related []
                 :facts {:instruction (when (map? subject) (:op subject))
                         :dialect :c11}
                 :remediation "Select the hosted C11 target and keep the source inside the verified stage0 instruction subset."}
                extra)))

(defn c-backend-validate-plan!
  "Validate the actual stage0 instruction plan iteratively before lowering."
  [source-path target plan]
  (when-not (contains? c-backend-supported-targets target)
    (c-backend-fail! "C14-TARGET"
                     "hosted C backend target is unsupported"
                     source-path target nil
                     {:supported-targets (vec (sort c-backend-supported-targets))
                      :missing-fact :supported-target
                      :remediation "Request :c, :c-hosted, or :c11 explicitly."}))
  (loop [pending (vec (mapcat (comp :instructions val) (:functions plan)))]
    (when-let [instruction (peek pending)]
      (let [pending (pop pending)]
        (when-not (map? instruction)
          (c-backend-fail! "B2-UNSUPPORTED"
                           "C backend received a malformed instruction"
                           source-path target instruction
                           {:missing-fact :instruction-record}))
        (when-not (contains? c-backend-supported-instructions (:op instruction))
          (c-backend-fail! "B2-UNSUPPORTED"
                           "C backend instruction is outside the hosted subset"
                           source-path target instruction
                           {:unsupported-op (:op instruction)
                            :missing-fact :c-lowering-rule}))
        (when (and (= :builtin-call (:op instruction))
                   (not (contains? stage0-builtin-functions
                                  (:function instruction))))
          (c-backend-fail! "B2-UNSUPPORTED"
                           "C backend builtin is outside the hosted subset"
                           source-path target instruction
                           {:unsupported-op (:function instruction)
                            :missing-fact :builtin-lowering-rule}))
        (recur (into pending (remove nil?
                                    (c-backend-instruction-children instruction)))))))
  :passed)

(defn c-backend-c-escape
  [text]
  (apply str
         (map (fn [ch]
                (cond
                  (= ch \\) "\\\\"
                  (= ch \") "\\\""
                  (= ch \newline) "\\n"
                  (= ch \return) "\\r"
                  (= ch \tab) "\\t"
                  (< (int ch) 32) (format "\\%03o" (int ch))
                  :else (str ch)))
              (str text))))

(defn c-backend-runtime-literal?
  "Return true when a scalar value has a stable hosted `str` spelling that
  can be represented by a runtime fwrite operation.  Collections are kept
  out of this first runtime-derived slice; their semantics remain on the
  verified stage0 fallback path."
  [value]
  (or (nil? value)
      (string? value)
      (number? value)
      (boolean? value)
      (char? value)
      (keyword? value)
      (symbol? value)))

(defn c-backend-runtime-binding-names
  [bindings]
  (mapv :name bindings))

(defn c-backend-runtime-local-kinds
  "Normalize the validator's lexical-local state to a name -> kind map.

  Older callers passed a set of names while the runtime-derived validator now
  needs the scalar kind to prove that `str` only consumes byte-string locals.
  Treating a legacy set as unknown scalar state preserves its presence checks
  without accidentally granting the stronger string capability."
  [locals]
  (if (map? locals)
    locals
    (into {} (map (fn [name] [name :scalar]) locals))))

(defn c-backend-runtime-local-present?
  [locals name]
  (contains? (c-backend-runtime-local-kinds locals) name))

(defn c-backend-runtime-string-expression-supported?
  "Return true only for a byte-string literal/quote or a local proven to be
  bound to one.  This intentionally excludes numeric/collection values,
  nested calls, and general control-flow expressions from the `str` operand
  surface."
  [instruction locals]
  (and (map? instruction)
       (let [op (:op instruction)
             locals (c-backend-runtime-local-kinds locals)]
         (cond
           (#{:literal :quote} op)
           (string? (:value instruction))
           (= :local op)
           (= :string (get locals (:name instruction)))
           :else false))))

(defn c-backend-runtime-next-local-kinds
  [locals bindings]
  (reduce (fn [scope {:keys [name expr]}]
            (assoc scope name
                   (if (and (map? expr)
                            (#{:literal :quote} (:op expr))
                            (string? (:value expr)))
                     :string
                     :scalar)))
          (c-backend-runtime-local-kinds locals)
          bindings))

(declare c-backend-runtime-test-expression-supported?)