

(defn p15-s23-c6c10-normalize-deferred-ratios-in-plan
  [source-path literal-authentication raw-plan]
  (let [body-form-ids
        (:body-form-ids
         (p15-s23-c6c10-main-source-body
          source-path literal-authentication))
        instructions (get-in raw-plan [:functions 'main :instructions])]
    (when-not (= (count body-form-ids) (count instructions))
      (p15-s23-c6c10-plan-shape-fail!
       source-path
       {:normalization-operation :function-body
        :body-form-count (count body-form-ids)
        :instruction-count (count instructions)}))
    (assoc-in
     raw-plan [:functions 'main :instructions]
     (mapv #(p15-s23-c6c10-normalize-deferred-ratio-instruction
             source-path literal-authentication %1 %2)
           instructions body-form-ids))))

(defn p15-s23-c6c10-private-stage2-plan-occurrences
  [source-path literal-authentication plan]
  (let [forms-by-id (:forms-by-id literal-authentication)
        main-source
        (p15-s23-c6c10-main-source-body source-path literal-authentication)
        main-root (:main-root main-source)
        body-form-ids (:body-form-ids main-source)
        main-plan (get-in plan [:functions 'main])
        body (:body main-plan)
        instructions (:instructions main-plan)
        _ (when-not (and (= (count body-form-ids) (count body))
                         (= (count body-form-ids) (count instructions))
                         (= (vec body)
                            (mapv #(get-in forms-by-id [% :value])
                                  body-form-ids)))
            (p15-s23-c6c10-plan-shape-fail!
             source-path
             {:main-form-id (:form-id main-root)
              :body-form-ids body-form-ids
              :plan-body-count (count body)
              :instruction-count (count instructions)}))
        body-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (map-indexed
          (fn [index form-id]
            (p15-s23-c6c10-prefix-occurrences
             [:plan :functions 'main :body index]
             (p15-s23-c6c10-form-value-numeric-occurrences
              source-path literal-authentication form-id)))
          body-form-ids))
        instruction-occurrences
        (apply
         p15-s23-c6c10-merge-occurrences
         (map-indexed
          (fn [index [instruction form-id]]
            (p15-s23-c6c10-prefix-occurrences
             [:plan :functions 'main :instructions index]
             (p15-s23-c6c10-instruction-numeric-occurrences
              source-path literal-authentication instruction form-id)))
          (map vector instructions body-form-ids)))]
    (p15-s23-c6c10-merge-occurrences
     body-occurrences instruction-occurrences)))

(defn p15-s23-c6c10-project-plan
  [source-path source-content-hash literal-authentication raw-plan]
  (let [raw-plan
        (p15-s23-c6c10-normalize-deferred-ratios-in-plan
         source-path literal-authentication raw-plan)
        raw-main (get-in raw-plan [:functions 'main])
        projected-main
        (-> (select-keys raw-main p15-s23-c6c10-function-projection-keys)
            (update :binding select-keys
                    p15-s23-c6c10-binding-projection-keys))
        selected
        (-> (select-keys raw-plan p15-s23-c6c10-plan-projection-keys)
            (assoc :functions {'main projected-main})
            (update :source select-keys [:sha256])
            (update :module select-keys
                    [:capabilities :module :exports :effects
                     :safety :target :profile]))
        neutral (p15-s23-c6c10-path-neutral-value
                 source-content-hash selected)
        plan-authentication
        (p15-s23-c6c10-authentication-with-occurrences
         literal-authentication :private-stage2-plan
         (p15-s23-c6c10-private-stage2-plan-occurrences
          source-path literal-authentication neutral))
        plan-id
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path plan-authentication :private-stage2-plan
         {:domain :gravity/c6-c10-private-stage2-plan-v1
          :plan (dissoc neutral :plan-id)})]
    (assoc neutral :plan-id plan-id)))

(defn p15-s23-c6c10-project-authoritative-module
  [source-content-hash raw-module front-end]
  (p15-s23-c6c10-path-neutral-value
   source-content-hash
   (merge {:requires [] :imports [] :providers []
           :metadata {} :doc nil}
          (select-keys raw-module
                       [:module :profile :target :effects :capabilities
                        :exports :safety :requires :imports :providers
                        :metadata :doc])
          {:forms (vec (rest (:forms front-end)))})))

(defn- p15-s23-c6c10-private-stage2-plan
  [emitter source-path source-text authoritative-module]
  ;; The module is parsed exactly once from genuine C2/C3 forms.  Invoke only
  ;; the pinned Gravity compiler artifact's construction seam: public P1/math,
  ;; effect, capability, and safety admission belongs after this boundary to
  ;; the Gravity C6-C10 module.
  (let [function-table (stage0-function-table authoritative-module)]
    (p15-s23-stage2-construct-emitted-core-plan
     emitter source-path source-text authoritative-module function-table)))

(defn- p15-s23-c6c10-private-ingress-products
  [source-path source-text]
  (when-not (and (string? source-path)
                 (qst-or-gravity-source? source-path)
                 (string? source-text))
    (p15-s23-c6c10-host-fail!
     "C6-CORE-SHAPE"
     (if (string? source-path) source-path "<c6-c10>")
     :co-canonical-source-path-and-text
     {:source-path source-path}))
  ;; C2/C3 construction is deliberately first and occurs exactly once under
  ;; the actual normative project context.  The private projection then
  ;; rebuilds every path-sensitive derived hash rather than lying about the
  ;; source unit's project-relative path or parsing a virtual second source.
  (binding [*print-length* nil
            *print-level* nil
            *print-meta* false
            *print-dup* false
            *print-readably* true
            *print-namespace-maps* false]
    (let [raw-front-end
          (p15-s23-stage2-c2-c3-front-end-products
           source-path source-text
           (reader-project-context-for-source source-path)
           true)
          source-content-hash
          (get-in raw-front-end [:source-unit-record :bytes-hash])
          literal-authentication
          (p15-s23-c6c10-literal-authentication source-path raw-front-end)
          front-end
          (p15-s23-c6c10-project-front-end source-path raw-front-end)
          _ (validate-ns-syntax! source-path (:forms raw-front-end))
          raw-module (parse-module source-path (:forms raw-front-end))
          emitter-rule
          (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
          raw-plan
          (p15-s23-c6c10-private-stage2-plan
           (:emitter emitter-rule) source-path source-text raw-module)
          plan
          (p15-s23-c6c10-project-plan
           source-path source-content-hash literal-authentication raw-plan)
          authoritative-module
          (p15-s23-c6c10-project-authoritative-module
           source-content-hash raw-module front-end)
          carrier-subject
          {:front-end-products front-end
           :stage2-plan plan
           :authoritative-module authoritative-module}
          carrier-validation
          (p15-s23-c6c10-carrier-validation source-path carrier-subject)]
      {:source-content-hash source-content-hash
       :front-end-products front-end
       :stage2-plan plan
       :authoritative-module authoritative-module
       :carrier-validation carrier-validation
       :raw-plan-id (:plan-id raw-plan)
       :plan-emitter-rule
       {:source-rule-hash (:source-rule-hash emitter-rule)
        :rule-record (:rule-record emitter-rule)}})))

(defn p15-s23-c6c10-resolve-source-path
  []
  (let [relative p15-s23-c6c10-source-relative-path
        classpath-roots
        (keep (fn [entry]
                (let [file (java.io.File. entry)]
                  (when (.isDirectory file) file)))
              (str/split (System/getProperty "java.class.path" "")
                         (re-pattern (java.io.File/pathSeparator))))
        roots (distinct
               (cons (java.io.File. (System/getProperty "user.dir"))
                     classpath-roots))]
    (or (some (fn [root]
                (loop [directory root]
                  (let [candidate (java.io.File. directory relative)]
                    (cond
                      (.isFile candidate) (.getPath candidate)
                      (.getParentFile directory)
                      (recur (.getParentFile directory))
                      :else nil))))
              roots)
        relative)))

(defn p15-s23-c6c10-function-order-key
  [source-path function-name]
  (p15-s23-c6c10-canonical-sort-key
   (:form (p15-s23-c6c10-canonical-record
           source-path function-name))))