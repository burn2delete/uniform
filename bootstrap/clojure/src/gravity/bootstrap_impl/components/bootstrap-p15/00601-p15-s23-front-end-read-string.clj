

(defn p15-s23-front-end-read-string
  [source-path state]
  (let [start state]
    (loop [state (p15-s23-front-end-advance state)
           chars []]
      (let [ch (p15-s23-front-end-current-char state)]
        (cond
          (nil? ch)
          (p15-s23-front-end-reader-error!
           source-path state {:reason :unclosed-string})
          (= \" ch)
          [(apply str chars) (p15-s23-front-end-advance state)]
          (= \\ ch)
          (let [escape-state (p15-s23-front-end-advance state)
                escape (p15-s23-front-end-current-char escape-state)]
            (when (nil? escape)
              (p15-s23-front-end-reader-error!
               source-path escape-state {:reason :unclosed-string-escape}))
            (recur (p15-s23-front-end-advance escape-state)
                   (conj chars
                         (case escape
                           \n \newline
                           \t \tab
                           \r \return
                           \" \"
                           \\ \\
                           escape))))
          :else
          (recur (p15-s23-front-end-advance state)
                 (conj chars ch)))))))

(defn p15-s23-front-end-read-token
  [state]
  (loop [state state
         chars []]
    (let [ch (p15-s23-front-end-current-char state)]
      (if (p15-s23-front-end-delimiter? ch)
        [(apply str chars) state]
        (recur (p15-s23-front-end-advance state) (conj chars ch))))))

(defn p15-s23-front-end-parse-token
  [source-path state token]
  (cond
    (= "nil" token) nil
    (= "true" token) true
    (= "false" token) false
    (str/starts-with? token ":") (keyword (subs token 1))
    (re-matches #"-?[0-9]+" token) (Long/parseLong token)
    (seq token) (symbol token)
    :else
    (p15-s23-front-end-reader-error!
     source-path state {:reason :empty-token})))

(defn p15-s23-front-end-read-coll
  [source-path state form-index close-ch kind]
  (loop [state (p15-s23-front-end-skip-ignored
                (p15-s23-front-end-advance state))
         values []]
    (let [ch (p15-s23-front-end-current-char state)]
      (cond
        (nil? ch)
        (p15-s23-front-end-reader-error!
         source-path state {:reason :unclosed-form :expected close-ch})
        (= close-ch ch)
        (let [end-state (p15-s23-front-end-advance state)]
          [(case kind
             :list (apply list values)
             :vector (vec values)
             :map (do
                    (when (odd? (count values))
                      (p15-s23-front-end-reader-error!
                       source-path state {:reason :odd-map}))
                    (into {} (partition 2 values)))
             :set (set values))
           end-state])
        (contains? #{\) \] \}} ch)
        (p15-s23-front-end-reader-error!
         source-path state {:reason :unexpected-close :found ch})
        :else
        (let [[record next-state]
              (p15-s23-front-end-read-form source-path state form-index)]
          (recur (p15-s23-front-end-skip-ignored next-state)
                 (conj values (:form record))))))))

(defn p15-s23-front-end-read-form
  [source-path state form-index]
  (let [state (p15-s23-front-end-skip-ignored state)
        start-state state
        ch (p15-s23-front-end-current-char state)]
    (when (nil? ch)
      (p15-s23-front-end-reader-error!
       source-path state {:reason :unexpected-eof}))
    (let [[form end-state]
          (case ch
            \( (p15-s23-front-end-read-coll
                source-path state form-index \) :list)
            \[ (p15-s23-front-end-read-coll
                source-path state form-index \] :vector)
            \{ (p15-s23-front-end-read-coll
                source-path state form-index \} :map)
            \" (p15-s23-front-end-read-string source-path state)
            \# (let [dispatch-state (p15-s23-front-end-advance state)
                     dispatch (p15-s23-front-end-current-char dispatch-state)]
                 (if (= \{ dispatch)
                   (p15-s23-front-end-read-coll
                    source-path dispatch-state form-index \} :set)
                   (p15-s23-front-end-reader-error!
                    source-path state
                    {:reason :unsupported-dispatch
                     :dispatch dispatch})))
            (if (contains? #{\) \] \}} ch)
              (p15-s23-front-end-reader-error!
               source-path state {:reason :unexpected-close :found ch})
              (let [[token end-state] (p15-s23-front-end-read-token state)]
                [(p15-s23-front-end-parse-token source-path state token)
                 end-state])))]
      [{:form form
        :span (p15-s23-front-end-span source-path form-index
                                      start-state end-state)
        :metadata {}
        :reader-origin {:kind :stage2-source-front-end
                        :raw-form-kind (form-kind form)
                        :engine :gravity-stage2-reader-rules-v1}
        :generated-origin []}
       end-state])))

(defn p15-s23-stage2-front-end-read-source-form-records
  [front-end source-path source-text]
  (when-not (= :gravity-stage2-reader-rules-v1
               (get-in front-end [:reader-rules :engine]))
    (p15-s23-stage2-source-front-end-fail!
     "P15S23F002" source-path front-end
     {:missing-fields [:reader-rules :engine]}))
  (loop [state (p15-s23-front-end-skip-ignored
                (p15-s23-front-end-state source-path source-text))
         form-index 0
         records []]
    (if (nil? (p15-s23-front-end-current-char state))
      records
      (let [[record next-state]
            (p15-s23-front-end-read-form source-path state form-index)]
        (recur (p15-s23-front-end-skip-ignored next-state)
               (inc form-index)
               (conj records record))))))

(defn p15-s23-stage2-front-end-expand-form
  [front-end syntax]
  (let [form (:form syntax)]
    (if (and (seq? form) (= 'defn (first form)))
      (let [[_ name params & body] form]
        (when-not (and (symbol? name) (vector? params))
          (p15-s23-stage2-source-front-end-fail!
           "P15S23F003" (get-in syntax [:span :source]) form
           {:reason :invalid-defn-shape}))
        (let [expanded (list 'def name (cons 'fn (cons params body)))]
          (-> syntax
              (assoc :form expanded
                     :origin :generated
                     :phase :macro-expanded
                     :macro-namespace 'gravity.core
                     :generated-origin
                     [{:from (:span syntax)
                       :macro 'gravity.core/defn
                       :macro-version "stage2-front-end"
                       :input-hash (str "sha256:"
                                        (sha256-hex (pr-str form)))
                       :output-hash (str "sha256:"
                                         (sha256-hex
                                          (pr-str expanded)))}])
              (update :hygiene conj {:macro 'gravity.core/defn
                                     :policy :hygienic}))))
      (assoc syntax :phase :macro-expanded))))

(defn p15-s23-stage2-c2-c3-records
  [c2-artifact rich-syntax]
  (let [form-by-id (into {} (map (juxt :form-id identity)
                                  (:form-tree c2-artifact)))
        token-by-id (into {} (map (juxt :token-id identity)
                                   (:token-stream c2-artifact)))]
    (mapv
     (fn [syntax]
       (let [form-id (get-in syntax [:source :form-id])
             token-id (get-in syntax [:source :token-id])
             form-record (get form-by-id form-id)
             token-record (get token-by-id token-id)]
         {:form (get-in syntax [:form :value])
          :span (get-in syntax [:span :primary])
          :origin :source
          :metadata (or (:metadata syntax) {})
          :reader-origin
          {:kind :stage2-source-front-end
           :engine :gravity-stage2-c2-c3-ingress
           :c3-origin (:origin syntax)}
          :source-origin (vec (or (:origin syntax) []))
          :generated-origin []
          :form-id form-id
          :token-id token-id
          :source-id (get-in syntax [:source :source-id])
          :c2-form-record form-record
          :c2-token-record token-record
          :c3-syntax-object syntax}))
     rich-syntax)))