
(defn- sh06-resolution-b51-callsite-census!
  [source-path modules]
  (let [names (vec (mapcat (comp keys :table) modules))]
    (when-not (= (count names) (count (set names)))
      (sh06-resolution-c5-fail!
       source-path :duplicate-provider-definition {}))
    (let [owner-by-name
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
                                    (if (= 'quote (first value)) []
                                        (let [callee (first value)
                                              callee-owner
                                              (get owner-by-name callee)
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
                                                  (sh06-resolution-b51-owner-edge
                                                   (:owner spec)
                                                   (:owner callee-owner))}])]
                                          (into (or row [])
                                                (mapcat
                                                 (fn [index child]
                                                   (walk child (conj path index)))
                                                 (range) value))))
                                    (vector? value)
                                    (mapcat (fn [index child]
                                              (walk child (conj path index)))
                                            (range) value)
                                    (map? value)
                                    (let [entries
                                          (sort-by #(pr-str
                                                     (reader-canonical-value %))
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
                                            (sort-by #(pr-str
                                                       (reader-canonical-value %))
                                                     value))
                                    :else []))]
                          (walk body-form [body-index])))
                      (range) (:body definition))))
                 (sort-by str (keys table)))))
            modules))
          ordered (vec (sort-by (juxt :caller-module-ordinal
                                      (comp str :caller-function)
                                      (comp count :callsite-path)
                                      :callsite-path) rows))]
      (when-not
       (and (= 1480 (count ordered))
            (every? :owner-edge ordered)
            (= (count ordered)
               (count (set (map (juxt :caller-module-ordinal
                                      :caller-function :callsite-path)
                                ordered)))))
        (sh06-resolution-c5-fail!
         source-path :unauthorized-cross-fragment-edge
         {:edge-count (count ordered)}))
      (mapv #(assoc %1 :ordinal %2) ordered (range)))))

(defn- sh06-resolution-b51-derive-m0-m1!
  [source-path modules]
  (let [census (sh06-resolution-b51-callsite-census! source-path modules)
        by-ordinal (into {} (map (fn [m]
                                  [(get-in m [:spec :module-ordinal]) m])
                                modules))
        definitions
        (mapv
         (fn [{:keys [spec module]}]
           (mapv
            (fn [form]
              (let [definition (function-definition-form
                                (:logical-source-path spec) form)]
                {:name (:name definition)
                 :arity (count (:params definition))
                 :visibility (if (contains? (set (:exports spec))
                                            (:name definition))
                               :public :private)
                 :source-hash
                 (sh06-resolution-b51-definition-hash
                  (assoc definition :arity (count (:params definition))))}))
            (filter #(function-definition-form
                      (:logical-source-path spec) %)
                    (:forms module))))
         modules)
        external-symbols
        (mapv
         (fn [ordinal]
           (vec (sort-by str
                         (set (map :callee-function
                                   (filter #(= ordinal
                                               (:caller-module-ordinal %))
                                           census))))))
         (range 7))
        m0-bases
        (mapv
         (fn [ordinal {:keys [spec descriptor bytes module table] :as input}]
           (let [binding-census
                 (sh06-resolution-b51-binding-census
                  (:logical-source-path spec) module ordinal)
                 externals
                 (mapv
                  (fn [symbol]
                    (let [sample
                          (first (filter #(and (= ordinal
                                                  (:caller-module-ordinal %))
                                               (= symbol (:callee-function %)))
                                         census))
                          callee (get by-ordinal
                                      (:callee-module-ordinal sample))
                          definition
                          (first (filter #(= symbol (:name %))
                                         (nth definitions
                                              (:callee-module-ordinal sample))))]
                      {:symbol symbol
                       :callee-module-ordinal (:callee-module-ordinal sample)
                       :callee-module (get-in callee [:spec :module])
                       :callee-source-sha256 (:source-sha256 callee)
                       :callee-definition-source-hash (:source-hash definition)
                       :arity (:arity sample)
                       :owner-edge (:owner-edge sample)}))
                  (nth external-symbols ordinal))
                 census-counts binding-census]
             {:artifact :gravity/sh07-b51-module-source-v1
              :schema-version 1 :module-source-id nil
              :module-ordinal ordinal :module (:module spec)
              :logical-source-path (:logical-source-path spec)
              :owner (:owner spec) :source-byte-count (count bytes)
              :source-sha256 (:raw-sha256 descriptor)
              :profile (:profile module) :target (:target module)
              :effects (:effects module) :capabilities (:capabilities module)
              :safety (:safety module) :definitions (nth definitions ordinal)
              :exports (:exports spec)
              :core-binding-count (:core-binding-count census-counts)
              :lexical-binding-count (:lexical-binding-count census-counts)
              :external-bindings externals
              :aggregate-binding-count
              (+ (:core-binding-count census-counts)
                 (:definition-count census-counts)
                 (:lexical-binding-count census-counts)
                 (count externals))
              :builtin-calls (sh06-resolution-b51-builtin-census table)}))
         (range 7) modules)
        m0s (mapv #(assoc % :module-source-id
                           (reader-canonical-hash
                            (dissoc % :module-source-id)))
                   m0-bases)
        edges
        (mapv
         (fn [row]
           (let [caller (nth m0s (:caller-module-ordinal row))
                 callee (nth m0s (:callee-module-ordinal row))
                 caller-definition
                 (first (filter #(= (:caller-function row) (:name %))
                                (:definitions caller)))
                 callee-definition
                 (first (filter #(= (:callee-function row) (:name %))
                                (:definitions callee)))]
             {:ordinal (:ordinal row)
              :caller-module-source-id (:module-source-id caller)
              :caller-function (:caller-function row)
              :caller-function-source-hash (:source-hash caller-definition)
              :callsite-path (:callsite-path row)
              :callee-module-source-id (:module-source-id callee)
              :callee-function (:callee-function row)
              :callee-function-source-hash (:source-hash callee-definition)
              :arity (:arity row) :owner-edge (:owner-edge row)
              :authorization :authenticated-b51-namespace-external}))
         census)
        m1-base {:artifact :gravity/sh07-b51-link-manifest-v1
                 :schema-version 1
                 :manifest-id nil
                 :module-source-ids (mapv :module-source-id m0s)
                 :edges edges}
        m1 (assoc m1-base :manifest-id
                  (reader-canonical-hash (dissoc m1-base :manifest-id)))]
    (when-not (and (every? #(= sh06-resolution-b51-m0-keys
                                (set (keys %))) m0s)
                   (= sh06-resolution-b51-m1-keys (set (keys m1))))
      (sh06-resolution-c5-fail! source-path :derived-m0-m1-shape {}))
    {:module-source-descriptors m0s :link-manifest m1}))
