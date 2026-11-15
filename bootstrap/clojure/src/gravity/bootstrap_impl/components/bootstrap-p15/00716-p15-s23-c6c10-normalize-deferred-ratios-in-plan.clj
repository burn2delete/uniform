(defn p15-s23-c6c10-resolved-parameter-row
  [path parameters original projected source-rows bindings resolutions index]
  (let [start (* 3 index) forms (subvec projected start (+ start 3))
        source (subvec source-rows start (+ start 3)) [name-form marker type-form] forms
        parameter (first (filter #(= (:syntax-id name-form) (:definition-syntax-id %)) bindings))
        reference (first (filter #(= (:syntax-id type-form) (:reference-syntax-id %)) resolutions))
        type (first (filter #(= (:binding-id reference) (:binding-id %)) bindings))]
    (when-not (and (= [:symbol :keyword :symbol] (mapv :kind source))
                  (= (mapv :value source) (mapv :value forms)) (= :- (:value marker))
                  (= [:lexical :local (:value name-form) (:scope-id name-form)]
                     ((juxt :binding-class :kind :name :scope-id) parameter))
                  (= (:scope-id parameter) (:scope-id type-form))
                  (= [:type (:value type-form) (:binding-id type)]
                     ((juxt :position :symbol :binding-id) reference))
                  (= [:core :type 'gravity.core] ((juxt :binding-class :kind :namespace) type))
                  (or (= 'Bool (:name type))
                      (and (contains? #{3 6} (count projected)) (= 'I64 (:name type))
                           (every? #(contains? '#{I64 gravity.core/I64} (:value (get projected %)))
                                   (if (= 3 (count projected)) [2] [2 5])))))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" path :resolved-single-bool-parameter {}))
    {:parameter-index index :parameter-binding parameter :type-binding type :type-resolution reference
     :coordinates (mapv (fn [role c2 form]
                          {:role role :c2-form-id (:form-id c2) :projected-form-id (:form-id form)
                           :projected-syntax-id (:syntax-id form)})
                        [:parameter-vector :parameter-name :annotation-marker :annotation-type]
                        (into [original] source) (into [parameters] forms))}))

(defn- p15-s23-c6c10-resolved-scalar-functions
  [source-path front-end c3-parent invocation]
  ;; C5 and its authenticated projection own declarations and lexical lookup.
  ;; This projection transports their actual identities and the existing
  ;; defn -> def/fn source correspondence; it does not infer types or effects.
  (let [reader-invocation (dissoc invocation :macro-source-mode)
        parent (sh05-macro-artifact-from-c3-artifact
                source-path c3-parent reader-invocation (:macro-source-mode invocation))
        resolved (sh06-resolution-artifact-from-sh05-artifact
                  source-path parent
                  {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                  (:reader-source-path invocation) invocation)
        request (sh07-core-authenticated-request resolved ::legacy invocation)
        forms (into {} (map (juxt :form-id identity)) (:forms request))
        sources (into {} (map (juxt :form-id identity)) (:form-tree front-end))
        bindings (:binding-table request)
        resolutions (:resolution-table request)
        definitions (into {} (map (juxt :definition-syntax-id identity)) bindings)
        references (into {} (map (juxt :reference-syntax-id identity)) resolutions)
        binding-ids (into {} (map (juxt :binding-id identity)) bindings)
        source-roots (mapv sources (rest (:top-level-form-ids front-end)))
        projected-roots (mapv forms (:top-level-form-ids request))
        hash (get-in request [:lineage :source-revision-id])]
    (when-not (and (= hash (get-in front-end [:source-unit-record :bytes-hash]))
                   (= (count source-roots) (count projected-roots)))
      (p15-s23-c6c10-plan-shape-fail!
       source-path {:operation :declaration-projection
                    :source-count (count source-roots) :projected-count (count projected-roots)}))
    (letfn [(coordinate [source projected]
              [(:form-id source) {:projected-form-id (:form-id projected)
                                  :projected-syntax-id (:syntax-id projected)}])
            (paired-tree [source projected]
              (let [source (if (= :metadata-wrapper (:kind source))
                             (get sources (last (:children source))) source)
                    children (mapv sources (:children source))
                    projected-children (mapv forms (:child-form-ids projected))]
                (when-not (and source projected (= (count children) (count projected-children)))
                  (p15-s23-c6c10-plan-shape-fail!
                   source-path {:operation :c5-source-correspondence
                                :source-form-id (:form-id source)
                                :projected-form-id (:form-id projected)}))
                (into [(coordinate source projected)]
                      (mapcat paired-tree children projected-children))))
            (declaration [source projected]
              (let [source (if (= :metadata-wrapper (:kind source))
                             (get sources (last (:children source))) source)
                    source-children (mapv sources (:children source))
                    projected-children (mapv forms (:child-form-ids projected))
                    function (get projected-children 2)
                    function-children (mapv forms (:child-form-ids function))
                    original-parameters (get source-children 2)
                    parameters (get function-children 1)
                    parameter-children (mapv forms (:child-form-ids parameters))
                    original-children (mapv sources (:children original-parameters))
                    parameter-positions
                    (keep-indexed (fn [index form]
                                    (when (= :lexical (:binding-class (get definitions (:syntax-id form))))
                                      index)) parameter-children)]
                (when-not (and (= 'defn (:value (first source-children)))
                               (= 'def (:value (first projected-children)))
                               (= 'fn (:value (first function-children)))
                               (= (count (drop 2 source-children)) (count (rest function-children))))
                  (p15-s23-c6c10-plan-shape-fail!
                   source-path {:operation :c5-defn-source-correspondence
                                :source-form-id (:form-id source)}))
                {:declaration
                 {:name (:value (get source-children 1))
                  :function-form-id (:form-id source)
                  :function-syntax-id (:syntax-id function)
                  :function-scope-id (:scope-id function)
                  :function-binding (get definitions (:syntax-id (get projected-children 1)))
                  :parameters
                  (mapv (fn [index position]
                          (let [name-form (get parameter-children position)
                                type-form (get parameter-children (+ position 2))
                                reference (get references (:syntax-id type-form))]
                            {:parameter-index index
                             :parameter-binding (get definitions (:syntax-id name-form))
                             :type-binding (get binding-ids (:binding-id reference))
                             :type-resolution reference
                             :coordinates
                             (mapv (fn [role original projected]
                                     {:role role :c2-form-id (:form-id original)
                                      :projected-form-id (:form-id projected)
                                      :projected-syntax-id (:syntax-id projected)})
                                   [:parameter-vector :parameter-name :annotation-marker :annotation-type]
                                   (into [original-parameters]
                                         (map #(get original-children %) (range position (+ position 3))))
                                   (into [parameters]
                                         (map #(get parameter-children %) (range position (+ position 3)))))}))
                        (range) parameter-positions)}
                 :coordinates
                 (into [(coordinate source projected)
                        (coordinate (first source-children) (first projected-children))
                        (coordinate (get source-children 1) (get projected-children 1))]
                       (mapcat paired-tree (drop 2 source-children) (rest function-children)))}))]
      (let [rows (mapv declaration source-roots projected-roots)]
        {:kind :gravity/c6-c10-resolved-scalar-functions-v1
         :source-content-hash hash :sh06-artifact-id (:artifact-id resolved)
         :sh06-semantic-projection-id (get-in request [:lineage :sh06-semantic-projection-id])
         :projection-binding (:projection-binding request)
         :root-syntax-id (:syntax-id (first projected-roots))
         :root-syntax-ids (mapv :syntax-id projected-roots)
         :declarations (mapv :declaration rows)
         :bindings bindings :resolutions resolutions
         :coordinates (into {} (mapcat :coordinates rows))}))))

(defn- p15-s23-c6c10-project-scalar-plan
  [source-path source-content-hash literal-authentication raw-plan]
  ;; The existing seed assembler supplies a module-shaped transport plan.
  ;; Authenticate every declaration's source occurrences, not a synthetic main.
  (let [forms (:forms-by-id literal-authentication)
        source-functions
        (into {}
              (keep (fn [form]
                      (let [value (:value form)
                            definition (if (= :metadata-wrapper (:kind form))
                                         (get forms (last (:children form))) form)]
                        (when (and (nil? (:parent-form-id form)) (seq? value) (= 'defn (first value)))
                          [(second value) {:source form :body-ids (vec (drop 3 (:children definition)))}]))))
              (vals forms))
        _ (when-not (= (set (keys source-functions)) (set (keys (:functions raw-plan))))
            (p15-s23-c6c10-plan-shape-fail!
             source-path {:operation :module-functions
                          :source-functions (set (keys source-functions))
                          :plan-functions (set (keys (:functions raw-plan)))}))
        functions
        (into (sorted-map)
              (map (fn [[name function]]
                     (let [ids (:body-ids (get source-functions name))
                           instructions (:instructions function)]
                       (when-not (and (= (count ids) (count instructions) (count (:body function)))
                                      (= (vec (:body function)) (mapv #(get-in forms [% :value]) ids)))
                         (p15-s23-c6c10-plan-shape-fail!
                          source-path {:operation :function-body :function name
                                       :source-form-count (count ids) :instruction-count (count instructions)}))
                       [name (-> (select-keys function p15-s23-c6c10-function-projection-keys)
                                 (update :binding select-keys p15-s23-c6c10-binding-projection-keys)
                                 (assoc :instructions
                                        (mapv #(p15-s23-c6c10-normalize-deferred-ratio-instruction
                                                source-path literal-authentication %1 %2)
                                              instructions ids)))])))
              (:functions raw-plan))
        selected (-> (select-keys raw-plan p15-s23-c6c10-raw-plan-projection-keys)
                     (dissoc :entrypoint)
                     (assoc :functions functions)
                     (update :source select-keys [:sha256])
                     (update :module select-keys [:capabilities :module :exports :effects :safety :target :profile]))
        neutral (p15-s23-c6c10-path-neutral-value source-content-hash selected)
        occurrences
        (apply p15-s23-c6c10-merge-occurrences
               (mapcat (fn [[name function]]
                         (mapcat
                          (fn [index form-id instruction]
                            [(p15-s23-c6c10-prefix-occurrences
                              [:plan :functions name :body index]
                              (p15-s23-c6c10-form-value-numeric-occurrences
                               source-path literal-authentication form-id))
                             (p15-s23-c6c10-prefix-occurrences
                              [:plan :functions name :instructions index]
                              (p15-s23-c6c10-instruction-numeric-occurrences
                               source-path literal-authentication instruction form-id))])
                          (range) (:body-ids (get source-functions name)) (:instructions function)))
                       (:functions neutral)))
        authentication (p15-s23-c6c10-authentication-with-occurrences
                        literal-authentication :private-stage2-plan occurrences)
        plan-id (p15-s23-c6c10-authenticated-semantic-digest
                 source-path authentication :private-stage2-plan
                 {:domain :gravity/c6-c10-private-scalar-module-plan-v1
                  :plan (dissoc neutral :plan-id)})]
    (assoc neutral :plan-id plan-id)))

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
        (-> (select-keys raw-plan
                        (if (= :gravity/stage2-compiler-artifact-plan (:kind raw-plan))
                          p15-s23-c6c10-raw-plan-projection-keys
                          p15-s23-c6c10-plan-projection-keys))
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

(declare sh03-reader-resolve-source-path p15-s23-c6c10-source-program-contract
         ^:private p15-s23-c6c10-current-reader-envelope-state!
         ^:private p15-s23-c6c10-project-expression-owner-metadata)

(defn- p15-s23-c6c10-private-ingress-products
  ([source-path source-text] (p15-s23-c6c10-private-ingress-products source-path source-text nil))
  ([source-path source-text reader-source-path]
   (p15-s23-c6c10-private-ingress-products source-path source-text reader-source-path nil))
  ([source-path source-text reader-source-path source-policy-version]
   (p15-s23-c6c10-private-ingress-products source-path source-text reader-source-path source-policy-version nil))
  ([source-path source-text reader-source-path source-policy-version selected-reader-binding]
   (p15-s23-c6c10-private-ingress-products
    source-path source-text reader-source-path source-policy-version
    selected-reader-binding nil))
  ([source-path source-text reader-source-path source-policy-version
    selected-reader-binding selected-reader-invocation]
  (when-not (and (string? source-path)
                 (qst-or-gravity-source? source-path)
                 (string? source-text))
    (p15-s23-c6c10-host-fail!
     "C6-CORE-SHAPE"
     (if (string? source-path) source-path "<c6-c10>")
     :co-canonical-source-path-and-text
     {:source-path source-path}))
  (when (some? source-policy-version)
    ;; The source-only application selects current parents without claiming
    ;; either historical public Boolean interface for its numeric Core.
    (when-not (= :compiler-application source-policy-version)
      (p15-s23-c6c10-source-program-contract source-policy-version))
    (when-not (and (contains? #{2 3 :compiler-application} source-policy-version)
                   (string? reader-source-path)
                   (= (.getCanonicalPath (java.io.File. (sh03-reader-resolve-source-path)))
                      (.getCanonicalPath (java.io.File. reader-source-path))))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :selected-current-reader-invocation-policy {})))
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
    (let [;; Selection is host invocation context, never artifact authority.
          invocation (or selected-reader-invocation
                          (when reader-source-path
                            {:reader-source-path reader-source-path
                             :authenticated-envelope-options {:source :development-current}}))
          ;; C2/C3 only accept the Reader/Syntax selection.  Macro mode is a
          ;; separate explicit C4 choice and must continue through the later
          ;; current-source resolver without being presented to C2.
          reader-invocation (if (map? invocation)
                              (dissoc invocation :macro-source-mode)
                              invocation)
          raw-front-end
          (p15-s23-stage2-c2-c3-front-end-products
           source-path source-text
           (reader-project-context-for-source source-path)
           true reader-source-path reader-invocation)
          source-content-hash
          (get-in raw-front-end [:source-unit-record :bytes-hash])
          literal-authentication
          (p15-s23-c6c10-literal-authentication source-path raw-front-end reader-invocation)
          front-end
          (p15-s23-c6c10-project-front-end source-path raw-front-end reader-invocation)
          _ (validate-ns-syntax! source-path (:forms raw-front-end))
          raw-module (parse-module source-path (:forms raw-front-end))
          application? (= :compiler-application source-policy-version)
          parameter? (and (not application?)
                          (contains? #{1 2 3} (get-in (stage0-function-table raw-module) ['main :arity])))
          resolved-functions (when application?
                               (p15-s23-c6c10-resolved-scalar-functions
                                source-path front-end (:authenticated-c3-source-artifact raw-front-end) invocation))
          resolved-parameter (when parameter?
                               (p15-s23-c6c10-resolved-parameter source-path source-text front-end
                                 (when (some? reader-source-path) (:authenticated-c3-source-artifact raw-front-end))
                                 invocation))
          emitter-rule
          (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
          ;; The application compiles a module, so it uses the existing seed
          ;; assembler without the executable-only main entrypoint check.
          ;; C6-C10 owns all scalar semantic admission after source binding.
          raw-plan
          (if (or application? parameter?)
            (p15-s23-stage2-compiler-artifact-plan
             (:emitter emitter-rule) source-path source-text)
            (p15-s23-c6c10-private-stage2-plan
             (:emitter emitter-rule) source-path source-text raw-module))
          plan
          ((if application? p15-s23-c6c10-project-scalar-plan p15-s23-c6c10-project-plan)
           source-path source-content-hash literal-authentication
           (if application?
             (assoc-in raw-plan [:module :exports] (:exports raw-module))
             raw-plan))
          authoritative-module
          (p15-s23-c6c10-project-authoritative-module
           source-content-hash raw-module front-end)
          carrier-subject
          (cond-> {:front-end-products front-end :stage2-plan plan
                   :authoritative-module authoritative-module}
            parameter? (assoc :resolved-parameter resolved-parameter)
            application? (assoc :resolved-functions resolved-functions))
          carrier-validation
          (p15-s23-c6c10-carrier-validation source-path carrier-subject)]
      (cond-> {:source-content-hash source-content-hash
       :front-end-products front-end
       :stage2-plan plan
       :authoritative-module authoritative-module
       :carrier-validation carrier-validation
       :raw-plan-id (:plan-id raw-plan)
       :plan-emitter-rule
       {:source-rule-hash (:source-rule-hash emitter-rule)
        :rule-record (:rule-record emitter-rule)}}
        parameter? (assoc :resolved-parameter resolved-parameter)
        application? (assoc :resolved-functions resolved-functions)
        (contains? #{2 3 :compiler-application} source-policy-version)
        (assoc :source-policy-receipt
               (p15-s23-c6c10-current-reader-envelope-state!
                source-path raw-front-end reader-source-path selected-reader-binding
                source-policy-version)))))))

(defn p15-s23-c6c10-resolve-source-path
  []
  (let [relative p15-s23-c6c10-source-relative-path
        classpath-roots
        (keep (fn [entry]
                (let [file (java.io.File. entry)]
                  (when (.isDirectory file) file)))
              (str/split (System/getProperty "java.class.path" "")
                         (re-pattern java.io.File/pathSeparator)))
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
