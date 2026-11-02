

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
  (gravity.backend.js/canonical-target #'js-ts-backend-target-aliases #'js-ts-backend-target target))

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
  "Validate only the existing source-path/string and plan/map shape."
  [source-path plan]
  (gravity.backend.js/validate-plan! #'js-ts-backend-fail! source-path plan))

(defn js-ts-backend-bytes-source
  [name bytes indent]
  (gravity.backend.js/bytes-source name bytes indent))

(defn js-ts-backend-value-declaration
  [instruction counter indent]
  (gravity.backend.js/value-declaration #'js-ts-backend-bytes-source #'c-backend-runtime-bytes instruction counter indent))

(defn js-ts-backend-write-source
  [descriptor indent]
  (gravity.backend.js/write-source descriptor indent))

(defn js-ts-backend-test-source
  [instruction env]
  (gravity.backend.js/test-source instruction env))

(declare js-ts-backend-value-source)
(declare js-ts-backend-instruction-source)

(defn js-ts-backend-value-source
  [instruction counter indent env]
  (gravity.backend.js/value-source #'js-ts-backend-value-declaration #'js-ts-backend-write-source #'js-ts-backend-test-source #'js-ts-backend-value-source instruction counter indent env))

(defn js-ts-backend-instruction-source
  ([instruction counter indent]
   (gravity.backend.js/instruction-source #'js-ts-backend-bytes-source #'js-ts-backend-value-source #'js-ts-backend-test-source #'js-ts-backend-value-declaration #'js-ts-backend-instruction-source instruction counter indent))
  ([instruction counter indent env]
   (gravity.backend.js/instruction-source #'js-ts-backend-bytes-source #'js-ts-backend-value-source #'js-ts-backend-test-source #'js-ts-backend-value-declaration #'js-ts-backend-instruction-source instruction counter indent env)))

(defn js-ts-backend-source
  [plan]
  (gravity.backend.js/source #'js-ts-backend-instruction-source plan))

(def js-ts-backend-declaration-source gravity.backend.js/declaration-source)
