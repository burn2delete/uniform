
(defn- p15-s23-sh07-b51-derive-callsite-census-from-modules
  [modules]
  (let [all-function-names
        (vec (mapcat (comp keys :table) modules))
        _ (when-not (= (count all-function-names)
                       (count (set all-function-names)))
            (throw (ex-info "SH-07 B51 module definitions are duplicated"
                            {:id "L2-UNKNOWN-CORE-FORM"
                             :reason :unauthenticated-b51-external-link})))
        owner-by-function
        (into {} (for [{:keys [spec table]} modules, name (keys table)]
                   [name {:module-ordinal (:module-ordinal spec)
                          :module (:module spec) :owner (:owner spec)}]))
        rows
        (vec
         (mapcat
          (fn [{:keys [spec table]}]
            (let [locals (set (keys table))]
              (mapcat
               (fn [caller]
                 (let [definition (get table caller)]
                   (mapcat
                    (fn [body-index body-form]
                      (letfn [(walk [value path]
                                (cond
                                  (seq? value)
                                  (if (= 'quote (first value))
                                    []
                                    (let [callee (first value)
                                          callee-owner (get owner-by-function callee)
                                          row
                                          (when (and callee-owner
                                                     (not (contains? locals callee)))
                                            [{:caller-module-ordinal
                                              (:module-ordinal spec)
                                              :caller-module (:module spec)
                                              :caller-function caller
                                              :callsite-path path
                                              :callee-module-ordinal
                                              (:module-ordinal callee-owner)
                                              :callee-module (:module callee-owner)
                                              :callee-function callee
                                              :arity (count (rest value))
                                              :owner-edge
                                              (cond
                                                (= (:owner spec)
                                                   (:owner callee-owner))
                                                (if (= :r1-producer (:owner spec))
                                                  :producer-internal
                                                  :root6-internal)
                                                (and (= :r8-finalizer (:owner spec))
                                                     (= :r1-producer
                                                        (:owner callee-owner)))
                                                :root8-to-producer-interface
                                                :else :forbidden-owner-edge)}])]
                                      (into (or row [])
                                            (mapcat (fn [index child]
                                                      (walk child
                                                            (conj path index)))
                                                    (range) value))))

                                  (vector? value)
                                  (mapcat (fn [index child]
                                            (walk child (conj path index)))
                                          (range) value)

                                  (map? value)
                                  (let [entries
                                        (sort-by
                                         #(pr-str (reader-canonical-value %))
                                         value)]
                                    (mapcat
                                     (fn [entry-index [key item]]
                                       (concat
                                        (walk key (conj path entry-index 0))
                                        (walk item (conj path entry-index 1))))
                                     (range) entries))

                                  (set? value)
                                  (mapcat (fn [index item]
                                            (walk item (conj path index)))
                                          (range)
                                          (sort-by
                                           #(pr-str (reader-canonical-value %))
                                           value))
                                  :else []))]
                        (walk body-form [body-index])))
                    (range) (:body definition))))
               (sort-by str (keys table)))))
          modules))
        ordered
        (vec (sort-by (juxt :caller-module-ordinal
                            (comp str :caller-function)
                            (comp count :callsite-path)
                            :callsite-path)
                      rows))]
    (when-not (and (= 1480 (count ordered))
                   (not-any? #(= :forbidden-owner-edge (:owner-edge %))
                             ordered)
                   (= (count ordered)
                      (count (set (map (juxt :caller-module-ordinal
                                             :caller-function :callsite-path)
                                       ordered)))))
      (throw (ex-info "SH-07 B51 source callsite census is not governed"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :observed-count (count ordered)})))
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn p15-s23-sh07-b51-derive-callsite-census []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :callsite-census)
        (let [modules
              (mapv
               (fn [spec]
                 (let [module-ordinal (:module-ordinal spec)
                       source
                       (p15-s23-sh07-b51-session-checked-product
                        [:module-input module-ordinal])]
                   (when-not source
                     (p15-s23-sh07-b51-governed-read-fail!
                      :module-validation-not-consumed
                      {:module-ordinal module-ordinal}))
                   (p15-s23-sh07-b51-governed-source-event!
                    :callsite-census (+ 3 module-ordinal) module-ordinal
                    'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census
                    (fn [entry]
                      (when-not (and (= (:source-descriptor source)
                                         (:descriptor entry))
                                     (= (:text source) (:text entry)))
                        (p15-s23-sh07-b51-governed-read-fail!
                         :cached-source-mismatch
                         {:module-ordinal module-ordinal
                          :domain :callsite-census}))
                      true))
                   source))
               p15-s23-sh07-b51-module-specs)
              census
              (p15-s23-sh07-b51-derive-callsite-census-from-modules modules)]
          (p15-s23-sh07-b51-cache-session-product!
           :callsite-census census)))
    (p15-s23-sh07-b51-derive-callsite-census-from-modules
     (mapv p15-s23-sh07-b51-compatibility-module-input
           p15-s23-sh07-b51-module-specs))))

(defn- p15-s23-sh07-b51-build-link-authority-from-census
  [census-edges]
  (let [sources
        (if *p15-s23-sh07-b51-governed-source-session*
          (mapv
           (fn [spec]
             (let [module-ordinal (:module-ordinal spec)
                   source
                   (p15-s23-sh07-b51-session-checked-product
                    [:module-input module-ordinal])]
               (when-not source
                 (p15-s23-sh07-b51-governed-read-fail!
                  :module-validation-not-consumed
                  {:module-ordinal module-ordinal}))
               (p15-s23-sh07-b51-governed-source-event!
                :link-authority (+ 3 module-ordinal) module-ordinal
                'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census
                (fn [entry]
                  (when-not (and (= (:source-descriptor source)
                                     (:descriptor entry))
                                 (= (:text source) (:text entry)))
                    (p15-s23-sh07-b51-governed-read-fail!
                     :cached-source-mismatch
                     {:module-ordinal module-ordinal
                      :domain :link-authority}))
                  true))
               source))
           p15-s23-sh07-b51-module-specs)
          (mapv p15-s23-sh07-b51-compatibility-module-input
                p15-s23-sh07-b51-module-specs))
        by-ordinal (into {} (map (fn [source]
                                  [(get-in source [:spec :module-ordinal])
                                   source])
                                sources))
        external-symbols
        (into {}
              (map (fn [ordinal]
                     [ordinal
                      (vec (sort-by str
                                    (set (map :callee-function
                                              (filter #(= ordinal
                                                          (:caller-module-ordinal %))
                                                      census-edges)))))]))
              (range 7))
        descriptors
        (mapv
         (fn [{:keys [spec path text module table]}]
           (let [ordinal (:module-ordinal spec)
                 externals
                 (mapv
                  (fn [symbol]
                    (let [sample (first (filter
                                         #(and (= ordinal
                                                  (:caller-module-ordinal %))
                                               (= symbol (:callee-function %)))
                                         census-edges))
                          callee-source (get by-ordinal
                                             (:callee-module-ordinal sample))
                          callee-definition (get (:table callee-source) symbol)]
                      {:symbol symbol
                       :callee-module-ordinal (:callee-module-ordinal sample)
                       :callee-module (get-in callee-source [:spec :module])
                       :callee-source-sha256 (:source-sha256 callee-source)
                       :callee-definition-source-hash
                       (p15-s23-sh07-source-definition-hash callee-definition)
                       :arity (:arity sample)
                       :owner-edge (:owner-edge sample)}))
                  (get external-symbols ordinal))
                 builtins (p15-s23-sh07-b51-builtin-call-census table)]
             (p15-s23-sh07-b51-build-module-source
              path text module
              (p15-s23-sh07-b51-binding-census
               path module (:module-ordinal spec))
              externals builtins)))
         sources)
        edges (p15-s23-sh07-b51-enrich-census-edges descriptors census-edges)
        manifest (p15-s23-sh07-b51-seal-link-manifest descriptors edges)]
    {:module-source-descriptors descriptors
     :link-manifest manifest
     :sources sources}))

(defn p15-s23-sh07-b51-build-link-authority []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :link-authority)
        (p15-s23-sh07-b51-cache-session-product!
         :link-authority
         (p15-s23-sh07-b51-build-link-authority-from-census
          (p15-s23-sh07-b51-derive-callsite-census))))
    (p15-s23-sh07-b51-build-link-authority-from-census
     (p15-s23-sh07-b51-derive-callsite-census))))

(def ^:private p15-s23-sh07-b51-compiler-source-path
  "bootstrap/gravity/p15_s23/compiler.gravity")
