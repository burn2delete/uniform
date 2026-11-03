(ns gravity.source-input
  "Invocation-local source products for the hosted bootstrap.

  The bootstrap foundation owns the captured default callbacks.  This leaf
  keeps only an invocation-local context and products returned by those
  callbacks; callers cannot register defaults or provide a product directly.")

(def ^:private ^:dynamic *context* nil)
(def ^:dynamic *module-analysis-interposed?* false)
(def ^:dynamic *product-mode* :reader)

(defn- bootstrap-defaults []
  (when-let [reader-var
             (ns-resolve 'gravity.bootstrap
                         'bootstrap-default-read-source-form-records)]
    {:reader @reader-var
     :parser @(ns-resolve 'gravity.bootstrap
                          'bootstrap-default-parse-module)
     :module-analysis-default?
     (if-let [state (ns-resolve 'gravity.bootstrap
                                'default-module-analysis-state)]
       (boolean (@state))
       true)
     :module-analysis-config
     (when-let [config (ns-resolve 'gravity.bootstrap
                                   'default-module-analysis-config-state)]
       (@config))}))

(defn with-context
  "Run thunk with one invocation-local product context.

  Nested stages share the outer context.  Standalone calls have no bootstrap
  defaults and therefore invoke every supplied callback."
  [thunk]
  (if *context*
    (thunk)
    (binding [*context* (atom {:defaults (bootstrap-defaults)})]
      (thunk))))

(defn with-module-analysis-interposition
  "Disable module-product reuse while module-analysis operations are injected."
  [thunk]
  (binding [*module-analysis-interposed?* true]
    (thunk)))

(defn with-product-mode
  "Bind a distinct source-product mode, such as C6 expanded syntax."
  [mode thunk]
  (binding [*product-mode* mode]
    (thunk)))

(defn- default-callback?
  [context key callback]
  (let [defaults (:defaults context)]
    (and (some? defaults)
         (identical? callback (get defaults key)))))

(defn- module-analysis-default?
  [context]
  (let [defaults (:defaults context)]
    (and (true? (:module-analysis-default? defaults))
         (let [state (ns-resolve 'gravity.bootstrap
                                 'default-module-analysis-state)
               config (ns-resolve 'gravity.bootstrap
                                  'default-module-analysis-config-state)]
           (and (or (nil? state) (boolean (@state)))
                (or (nil? config)
                    (= (:module-analysis-config defaults) (@config))))))))

(defn- same-source?
  [context source-path source-text]
  (and (some? source-path)
       (some? source-text)
       (some? (:source-path context))
       (some? (:source-text context))
       (= source-path (:source-path context))
       (= source-text (:source-text context))))

(defn- same-form-products?
  [context forms]
  (let [expected (:form-identities context)]
    (and (vector? forms)
         (vector? expected)
         (= (count forms) (count expected))
         (every? true? (map identical? forms expected)))))

(defn- invalidate-records!
  []
  (when *context*
    (swap! *context* dissoc :source-path :source-text :records
           :form-identities :module)))

(defn records
  "Invoke reader, reusing only the captured bootstrap reader product.

  The optional reuse? flag is false for authenticated reader bindings, which
  both prevents reuse and invalidates any ordinary product already retained by
  this invocation."
  ([reader source-path source-text]
   (records reader source-path source-text true))
  ([reader source-path source-text reuse?]
   (let [context (when *context* @*context*)
         cached (get context :records)
         default? (and reuse?
                       (not *module-analysis-interposed?*)
                       (default-callback? context :reader reader))]
     (if (and default?
              cached
              (same-source? context source-path source-text))
       (:value cached)
       (do
         (when (and *context* (not default?))
           (invalidate-records!))
         (let [value (reader source-path source-text)]
           (when (and *context* default?)
             (swap! *context* assoc
                    :source-path source-path
                    :source-text source-text
                    :records {:reader reader :value value}
                    :form-identities (mapv :form value)
                    :module nil))
           value))))))

(defn module
  "Invoke parser, reusing only the captured bootstrap module product."
  [parser source-path forms]
  (let [context (when *context* @*context*)
        cached (get context :module)
        default? (and (not *module-analysis-interposed?*)
                      (module-analysis-default? context)
                      (default-callback? context :parser parser))
        reusable? (and default?
                       (same-source? context source-path
                                     (:source-text context))
                       (same-form-products? context forms))]
    (if (and reusable?
             cached
             (= *product-mode* (:mode cached)))
      (:value cached)
      (do
        (when (and *context* (not default?))
          (swap! *context* dissoc :module))
        (let [value (parser source-path forms)]
          (when (and *context*
                     reusable?
                     (= *product-mode* :reader))
            (swap! *context* assoc
                   :module {:parser parser
                            :mode *product-mode*
                            :value value}))
          value)))))
