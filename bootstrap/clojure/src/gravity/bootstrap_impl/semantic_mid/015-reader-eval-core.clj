(defn- semantic-mid-reader-eval-let
  [reader-source-path definitions env form]
  (let [[_ bindings & body] form]
    (when-not (and (vector? bindings) (even? (count bindings)))
      (stage1-reader-algorithm-fail!
       "STAGE1ALGO002" reader-source-path form
       {:missing-fields [:let-bindings]}))
    (let [env (reduce (fn [env [name expr]]
                        (when-not (symbol? name)
                          (stage1-reader-algorithm-fail!
                           "STAGE1ALGO002" reader-source-path form
                           {:missing-fields [:binding-symbol]}))
                        (assoc env name
                               (stage1-reader-eval-gravity
                                reader-source-path definitions env expr)))
                      env
                      (partition 2 bindings))]
      (stage1-reader-eval-body reader-source-path definitions env body))))

(defn- semantic-mid-reader-eval-if
  [reader-source-path definitions env form]
  (let [[_ test then else] form]
    (if (stage1-reader-eval-gravity reader-source-path definitions env test)
      (stage1-reader-eval-gravity reader-source-path definitions env then)
      (stage1-reader-eval-gravity reader-source-path definitions env else))))

(defn- semantic-mid-reader-trace!
  [primitive entries]
  (when *stage1-reader-pipeline-trace*
    (swap! *stage1-reader-pipeline-trace*
           (fn [trace]
             (reduce (fn [trace [key value]]
                       (assoc trace key value))
                     (update (or trace {}) :host-primitives
                             (fnil conj []) primitive)
                     entries)))))

(defn- semantic-mid-reader-eval-read-with-table
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr table-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        table (stage1-reader-eval-gravity
               reader-source-path definitions env table-expr)]
    (stage1-reader-table-driven-records source-path source-text table)))

(defn- semantic-mid-reader-eval-scan-tokens
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr table-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        table (stage1-reader-eval-gravity
               reader-source-path definitions env table-expr)
        token-stream (stage1-reader-token-stream source-path source-text
                                                 table)]
    (semantic-mid-reader-trace!
     :reader/scan-tokens [[:token-stream token-stream]])
    token-stream))

(defn- semantic-mid-reader-eval-source-characters
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        character-stream (stage1-reader-character-stream source-path
                                                         source-text)]
    (semantic-mid-reader-trace!
     :reader/source-characters [[:character-stream character-stream]])
    character-stream))
