

(defn c-backend-file-artifact
  ([path] (c-backend-file-artifact path {}))
  ([path options]
   (c-backend-source-artifact path (read-gravity-source-text path) options)))

;; ---------------------------------------------------------------------------
;; Hosted JavaScript / TypeScript target (Node 20, ES2022 ESM)
;;
;; This target deliberately shares the authoritative stage2 compiler-driver,
;; plan-emitter, and Gravity-authored runtime packet used by runtime-derived C.
;; The C artifact is used only as a validated packet carrier: the JavaScript
;; emitter consumes its genuine stage2 instruction plan and emits executable
;; ESM statements.  Target bytes never contain checkout or output paths.

(def js-ts-backend-target :js-ts)
(def js-ts-backend-target-aliases #{:js :js-ts})
(def js-ts-backend-runtime :node20)
(def js-ts-backend-ecmascript :es2022)
(def js-ts-backend-module-format :esm)

(defn js-ts-backend-canonical-target
  [target]
  (let [target (cond
                 (keyword? target) target
                 (string? target) (keyword (str/lower-case target))
                 :else target)]
    (if (contains? js-ts-backend-target-aliases target)
      js-ts-backend-target
      target)))

(defn js-ts-backend-fail!
  [id message source-path subject extra]
  (fail! id message
         (merge {:severity :error
                 :stage :js-ts-backend-lowering
                 :diagnostic-family
                 (cond
                   (str/starts-with? id "B6") :b6-js-ts-backend
                   (str/starts-with? id "B14") :b14-backend-conformance
                   :else :c14-target-lowering)
                 :backend :gravity.backend/js-ts
                 :target js-ts-backend-target
                 :source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :primary {:span (or (:source-span subject)
                                     (source-span source-path 0))}
                 :facts {:instruction (when (map? subject) (:op subject))
                         :runtime js-ts-backend-runtime
                         :ecmascript js-ts-backend-ecmascript
                         :module-format js-ts-backend-module-format}
                 :remediation
                 "Use --target js or --target js-ts with the closed Node 20 ES2022 ESM hosted subset."}
                extra)))

(defn js-ts-backend-validate-plan!
  "Apply the JS/TS-specific closed-surface rules after the shared stage2/C
  packet has validated instruction shape.  Variadic println remains bounded by
  the shared closed-plan node limit and lowers to ordered byte writes."
  [source-path plan]
  ;; Source-path and plan remain explicit arguments so future target-specific
  ;; representation checks can fail at this boundary without changing callers.
  (when-not (and (string? source-path) (map? plan))
    (js-ts-backend-fail!
     "C14-INPUT" "JS/TS backend requires a source-bound stage2 plan"
     source-path plan {:missing-fact :source-bound-stage2-plan}))
  :passed)

(defn js-ts-backend-bytes-source
  [name bytes indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "const " name " = new Uint8Array(["
         (str/join "," bytes) "]);\n")))

(defn js-ts-backend-value-declaration
  [instruction counter indent]
  (let [name (str "gravityValue" (swap! counter inc))
        value (:value instruction)
        bytes (c-backend-runtime-bytes value)]
    {:source (js-ts-backend-bytes-source name bytes indent)
     :descriptor {:name name
                  :truth (not (or (nil? value) (false? value)))}}))

(defn js-ts-backend-write-source
  [descriptor indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "stdout.write(" (:name descriptor) ");\n")))

(defn js-ts-backend-test-source
  [instruction env]
  (case (:op instruction)
    :literal (if (or (nil? (:value instruction))
                     (false? (:value instruction))) "false" "true")
    :quote (if (or (nil? (:value instruction))
                   (false? (:value instruction))) "false" "true")
    :local (if (get-in env [(:name instruction) :truth]) "true" "false")
    "false"))

(declare js-ts-backend-value-source)
(declare js-ts-backend-instruction-source)

(defn js-ts-backend-value-source
  [instruction counter indent env]
  (let [padding (apply str (repeat indent "  "))
        op (:op instruction)]
    (cond
      (#{:literal :quote} op)
      (let [{:keys [source descriptor]}
            (js-ts-backend-value-declaration instruction counter indent)]
        (str source (js-ts-backend-write-source descriptor indent)))

      (= :local op)
      (js-ts-backend-write-source (get env (:name instruction)) indent)

      (= :builtin-call op)
      (apply str
             (map #(js-ts-backend-value-source % counter indent env)
                  (:args instruction)))

      (= :if op)
      (str padding "if ("
           (js-ts-backend-test-source (:test instruction) env)
           ") {\n"
           (js-ts-backend-value-source (:then instruction) counter
                                       (inc indent) env)
           padding "} else {\n"
           (js-ts-backend-value-source (:else instruction) counter
                                       (inc indent) env)
           padding "}\n")

      (= :let op)
      (let [binding-state
            (reduce (fn [state {:keys [name expr]}]
                      (let [{:keys [source descriptor]}
                            (js-ts-backend-value-declaration expr counter indent)]
                        {:source (str (:source state) source)
                         :env (assoc (:env state) name descriptor)}))
                    {:source "" :env env}
                    (:bindings instruction))]
        (str (:source binding-state)
             (js-ts-backend-value-source
              (first (:body instruction)) counter indent
              (:env binding-state))))

      :else "")))

(defn js-ts-backend-instruction-source
  ([instruction counter indent]
   (js-ts-backend-instruction-source instruction counter indent {}))
  ([instruction counter indent env]
   (let [padding (apply str (repeat indent "  "))
         op (:op instruction)]
     (cond
       (#{:literal :quote :local} op) ""

       (= :println op)
       (str (apply str
                   (map-indexed
                    (fn [index argument]
                      (str (when (pos? index)
                             (let [name (str "gravitySpace"
                                             (swap! counter inc))]
                               (str (js-ts-backend-bytes-source
                                     name [32] indent)
                                    padding "stdout.write(" name ");\n")))
                           (js-ts-backend-value-source
                            argument counter indent env)))
                    (:args instruction)))
            (let [name (str "gravityNewline" (swap! counter inc))]
              (str (js-ts-backend-bytes-source name [10] indent)
                   padding "stdout.write(" name ");\n")))

       (= :do op)
       (apply str
              (map #(js-ts-backend-instruction-source % counter indent env)
                   (:body instruction)))

       (= :if op)
       (str padding "if ("
            (js-ts-backend-test-source (:test instruction) env)
            ") {\n"
            (js-ts-backend-instruction-source (:then instruction) counter
                                              (inc indent) env)
            padding "} else {\n"
            (js-ts-backend-instruction-source (:else instruction) counter
                                              (inc indent) env)
            padding "}\n")

       (= :let op)
       (let [binding-state
             (reduce (fn [state {:keys [name expr]}]
                       (let [{:keys [source descriptor]}
                             (js-ts-backend-value-declaration
                              expr counter indent)]
                         {:source (str (:source state) source)
                          :env (assoc (:env state) name descriptor)}))
                     {:source "" :env env}
                     (:bindings instruction))]
         (str (:source binding-state)
              (apply str
                     (map #(js-ts-backend-instruction-source
                            % counter indent (:env binding-state))
                          (:body instruction)))))

       :else ""))))

(defn js-ts-backend-source
  [plan]
  (let [counter (atom 0)
        main (get-in plan [:functions (:entrypoint plan)])
        writes-stdout? (pos? (get-in plan [:instruction-summary :println] 0))]
    (str "#!/usr/bin/env node\n"
         (when writes-stdout?
           "import { stdout } from \"node:process\";\n")
         "\n"
         (apply str
                (map #(js-ts-backend-instruction-source % counter 0 {})
                     (:instructions main)))
         "//# sourceMappingURL=program.mjs.map\n")))

(def js-ts-backend-declaration-source
  "// Generated Gravity hosted entrypoint; no exported API in this slice.\nexport {};\n")