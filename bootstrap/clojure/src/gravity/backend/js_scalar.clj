(ns gravity.backend.js-scalar
  "Syntax and source-map transport for the Uniform-owned B6 scalar program.
   Input legality and all lowering choices belong to C14/B1/B6, not this printer."
  (:require [clojure.string :as str]
            [gravity.backend.js :as js]))

(defn json-source
  "Encode the immutable scalar/map/vector wire data used by generated metadata."
  [value]
  (cond
    (nil? value) "null"
    (true? value) "true"
    (false? value) "false"
    (string? value) (js/json-string value)
    (or (keyword? value) (symbol? value)) (js/json-string (str value))
    (integer? value) (str value)
    (map? value)
    (str "{" (str/join "," (for [[key child] (sort-by (comp str key) value)]
                               (str (js/json-string (if (keyword? key) (name key) (str key)))
                                    ":" (json-source child)))) "}")
    (sequential? value) (str "[" (str/join "," (map json-source value)) "]")
    (set? value) (json-source (sort-by str value))
    :else (throw (ex-info "Unsupported JSON metadata carrier" {:id "B6-MANIFEST"}))))

(defn- location [source]
  (let [span (:span source)]
    {:line (or (:line span) (:line-start span) (:start-line span) (get-in span [:start :line]))
     :column (or (:column span) (:column-start span) (:start-column span) (get-in span [:start :column]))}))

(defn- identifier [prefix index] (str "uniform_" prefix index))

(defn- function-values [function]
  (distinct
   (concat (map :value-id (:parameters function))
           (mapcat :arguments (:blocks function))
           (mapcat #(keep :result (:statements %)) (:blocks function)))))

(defn- source-origin [source function-id operation-id core-id]
  (let [{:keys [line column]} (location source)]
    (when (and (integer? line) (pos? line) (integer? column) (pos? column))
      {:sourceLine line :sourceColumn (dec column)
       :functionId function-id :mirInstructionId operation-id :coreNodeId core-id
       :originChain (:generated-origin source)
       :sourceSpan (:span source)
       :syntaxId (:enclosing-syntax-origin-id source) :originId (:origin-id source)})))

(defn- error-runtime [program]
  (let [numeric (get-in program [:error-contract :numeric])
        abi (get-in program [:error-contract :abi])]
    [(str "function uniform_numeric_error(operationId, checkId, left, right, source) {")
     "  const error = new Error(\"Checked I64 addition overflow\");"
     (str "  error.name = " (json-source (:name numeric)) ";")
     (str "  error.type = " (json-source (:type numeric)) ";")
     (str "  error.kind = " (json-source (:kind numeric)) ";")
     (str "  error.numericMode = " (json-source (:numericMode numeric)) ";")
     (str "  error.width = " (:width numeric) ";")
     (str "  error.signed = " (json-source (:signed numeric)) ";")
     (str "  error.operation = " (json-source (:operation numeric)) ";")
     "  error.operationId = operationId;"
     "  error.checkId = checkId;"
     "  error.operands = [left, right];"
     "  error.source = source;"
     "  throw error;"
     "}"
     "function uniform_abi_error(kind, fn, index, expected, actualType, actualArity, source) {"
     "  const error = new Error(\"Invalid Uniform exported-function arguments\");"
     (str "  error.name = " (json-source (:name abi)) ";")
     (str "  error.type = " (json-source (:type abi)) ";")
     "  error.kind = kind;"
     "  error.function = fn;"
     "  error.argumentIndex = index;"
     "  error.expected = expected;"
     "  error.actualType = actualType;"
     "  if (kind === \":arity\") error.actualArity = actualArity;"
     "  error.source = source;"
     "  throw error;"
     "}"
     ""] ))

(defn program-source
  "Print statically named functions/blocks and source locations from B6 output.
   Block functions share SSA storage in their enclosing source function. Edges
   pass explicit successor arguments; there is no instruction/opcode runtime."
  [program]
  (let [lines (atom [])
        line! (fn [text origin]
                (swap! lines conj {:text text :origin origin}))
        functions (:functions program)
        function-names (into {} (map-indexed (fn [index function]
                                               [(:function-id function) (identifier "fn" index)]) functions))
        minimum (str (get-in program [:numeric :minimum]) "n")
        maximum (str (get-in program [:numeric :maximum]) "n")]
    (line! "// Generated from freshly verified Uniform scalar MIR. Node 20 / ES2022 / ESM." nil)
    (doseq [line (error-runtime program)] (line! line nil))
    (doseq [[function-index function] (map-indexed vector functions)]
      (let [parameters (:parameters function)
            parameter-ids (mapv :value-id parameters)
            values (vec (function-values function))
            value-names (into {} (map-indexed (fn [index id] [id (identifier "v" index)]) values))
            block-names (into {} (map-indexed (fn [index block]
                                              [(:block-id block) (identifier "block" index)]) (:blocks function)))
            params (str/join ", " (map value-names parameter-ids))
            fn-id (:function-id function)
            function-origin (source-origin (:source function) fn-id fn-id fn-id)
            call (fn [block-id args]
                   (str (get block-names block-id) "(" (str/join ", " (map value-names args)) ")"))]
        (line! (str "function " (get function-names fn-id) "(" params ") {") function-origin)
        (when-let [locals (seq (remove (set parameter-ids) values))]
          (line! (str "  let " (str/join ", " (map value-names locals)) ";") nil))
        (doseq [block (:blocks function)]
          (let [block-params (mapv #(identifier "edge" %) (range (count (:arguments block))))]
            (line! (str "  function " (get block-names (:block-id block)) "("
                        (str/join ", " block-params) ") {") nil)
            (doseq [[id parameter] (map vector (:arguments block) block-params)]
              (line! (str "    " (get value-names id) " = " parameter ";") nil))
            (doseq [statement (:statements block)]
              (let [result (get value-names (:result statement))
                    origin (source-origin (:source statement) fn-id (:operation-id statement)
                                          (or (:guarded-operation-id statement)
                                              (:core-node-id (:source statement)) (:operation-id statement)))]
                (case (:kind statement)
                  :constant
                  (line! (str "    " result " = "
                              (case (:type statement)
                                :bigint (str (:value statement) "n")
                                :boolean (json-source (:value statement))) ";") origin)
                  :copy (line! (str "    " result " = " (get value-names (:value statement)) ";") origin)
                  :call (line! (str "    " result " = " (get function-names (:function-id statement)) "("
                                    (str/join ", " (map value-names (:arguments statement))) ");") origin)
                  :checked-add-guard
                  (let [[left right] (map value-names (:operands statement))]
                    (line! (str "    " result " = " left " + " right ";") origin)
                    (line! (str "    if (" result " < " minimum " || " result " > " maximum ") {") origin)
                    (line! (str "      uniform_numeric_error("
                                (json-source (:guarded-operation-id statement)) ", "
                                (json-source (:check-id statement)) ", " left ", " right ", "
                                (json-source (:error-source statement)) ");") origin)
                    (line! "    }" nil))
                  :guarded-add
                  (line! (str "    " result " = " (get value-names (:checked-sum statement)) ";") origin)
                  (throw (ex-info "B6 returned an unprintable static statement"
                                  {:id "B6-MANIFEST" :kind (:kind statement)})))))
            (let [term (:terminator block)
                  successors (:successors term)
                  arguments (:successor-arguments term)
                  origin (source-origin (:source term) fn-id (:operation-id term)
                                        (or (:core-node-id (:source term)) fn-id))]
              (case (:kind term)
                :return (line! (str "    return " (get value-names (first (:operands term))) ";") origin)
                :branch (line! (str "    return " (call (first successors) (first arguments)) ";") origin)
                :conditional-branch
                (do (line! (str "    if (" (get value-names (first (:operands term))) ") {") origin)
                    (line! (str "      return " (call (first successors) (first arguments)) ";") origin)
                    (line! "    } else {" nil)
                    (line! (str "      return " (call (second successors) (second arguments)) ";") origin)
                    (line! "    }" nil))
                (throw (ex-info "B6 returned an unprintable static terminator"
                                {:id "B6-MANIFEST" :kind (:kind term)}))))
            (line! "  }" nil)))
        (line! (str "  return " (call (:entry function) []) ";") function-origin)
        (line! "}" nil)
        (when (:exported? function)
          (let [wrapper (identifier "export" function-index)
                export-name (:export-name function)
                source (json-source (:error-source function))
                error-call (fn [kind index expected actual-type actual-arity]
                             (str "uniform_abi_error(" (json-source kind) ", " (json-source export-name) ", "
                                  (json-source index) ", " (json-source expected) ", " actual-type ", " actual-arity ", " source ");"))]
            (line! (str "function " wrapper "(" params ") {") function-origin)
            (line! (str "  if (arguments.length !== " (:arity function) ") "
                        (error-call ":arity" nil (:arity function) "null" "arguments.length")) function-origin)
            (doseq [[index parameter] (map-indexed vector parameters)]
              (let [value (get value-names (:value-id parameter))
                    type (name (:type parameter))
                    actual (str "(" value " === null ? \"null\" : typeof " value ")")]
                (line! (str "  if (typeof " value " !== " (json-source type) ") "
                            (error-call ":type" index type actual "null")) function-origin)
                (when (= :bigint (:type parameter))
                  (line! (str "  if (" value " < " minimum " || " value " > " maximum ") "
                              (error-call ":range" index "signed I64" "\"bigint\"" "null")) function-origin))))
            (line! (str "  return " (get function-names fn-id) "(" params ");") function-origin)
            (line! "}" nil)
            (line! (str "export { " wrapper " as " (json-source export-name) " };") function-origin)))
        (line! "" nil)))
    (line! "//# sourceMappingURL=program.mjs.map" nil)
    {:javascript (str (str/join "\n" (map :text @lines)) "\n")
     :lines @lines}))

(defn declaration-source [program]
  (str "// Uniform checked scalar API. I64 is bigint within signed 64-bit range.\n"
       "// Invalid foreign arguments throw UniformABIError; checked overflow throws UniformNumericError.\n"
       (apply str
              (for [[index function] (map-indexed vector (:functions program))
                    :when (:exported? function)]
                (let [internal (identifier "export" index)]
                  (str "declare function " internal "("
                       (str/join ", " (map-indexed (fn [index parameter]
                                                      (str "arg" index ": " (name (:type parameter))))
                                                    (:parameters function)))
                       "): " (name (:return-type function)) ";\n"
                       "export { " internal " as " (json-source (:export-name function)) " };\n"))))))

(def ^:private base64-digits "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")

(defn- append-vlq! [^StringBuilder mappings number]
  (loop [value (if (neg? number) (inc (* -2 number)) (* 2 number))]
    (let [digit (bit-and value 31)
          remainder (unsigned-bit-shift-right value 5)
          more? (not (zero? remainder))]
      (.append mappings (.charAt ^String base64-digits (if more? (bit-or digit 32) digit)))
      (when more?
        (recur remainder)))))

(defn source-map-source [program source-text emitted]
  (let [lines (:lines emitted)
        source-lines (str/split source-text #"\r\n|\r|\n" -1)
        origins (into []
                      (keep-indexed
                       (fn [index {:keys [text origin]}]
                         (when origin
                           (let [line (get source-lines (dec (:sourceLine origin)))
                                 column (:sourceColumn origin)
                                 ;; C2 columns count Unicode scalar values. Source
                                 ;; Map v3 original columns count UTF-16 code units.
                                 column (if (= :unicode-scalar (or (get-in origin [:sourceSpan :start :column-unit])
                                                                 (get-in origin [:sourceSpan :column-unit])))
                                          (.offsetByCodePoints ^String line 0 column) column)]
                             (assoc origin :generatedLine (inc index)
                                           :generatedColumn (- (count text) (count (str/triml text)))
                                           :sourceColumn column :sourceColumnUnit "utf16-code-unit")))))
                      lines)
        ;; The printer terminates its output with a newline, leaving a final empty line.
        final-line (inc (count lines))
        result (StringBuilder.)
        mappings (loop [remaining (seq origins) previous-generated-line 1 source-line 0 source-column 0]
                   (let [origin (first remaining)
                         generated-line (or (:generatedLine origin) final-line)]
                     ;; Each separator advances one generated line. Only segments change source deltas.
                     (dotimes [_ (- generated-line previous-generated-line)]
                       (.append result \;))
                     (if origin
                       (let [next-source-line (dec (:sourceLine origin))
                             next-source-column (:sourceColumn origin)]
                         (append-vlq! result (:generatedColumn origin))
                         (.append result \A)
                         (append-vlq! result (- next-source-line source-line))
                         (append-vlq! result (- next-source-column source-column))
                         (recur (next remaining) generated-line next-source-line next-source-column))
                       (str result))))]
    (str (json-source
          {:version 3 :file "program.mjs" :sources [(get-in program [:source :path])]
           :sourcesContent [source-text] :names [] :mappings mappings
           :x_gravity {:coverage "functions-operations-guards-and-terminators"
                       :sourceContentHash (get-in program [:source :content-hash])
                       :perFormOriginPreserved true :generatedOrigins origins}}) "\n")))
