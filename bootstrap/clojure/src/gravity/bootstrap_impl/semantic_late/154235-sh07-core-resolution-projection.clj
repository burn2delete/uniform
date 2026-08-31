; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-projection
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-revision-id
     trees
     resolved-analysis
     root-by-upstream-id
     declaration-syntax-by-upstream-id
     projected-scope-by-upstream
     scope-by-syntax-id
     occurrences]}
   state]
  (let
   [all-records
    (vec (mapcat :records trees))
    form-by-syntax
    (into {} (map (juxt :syntax-id identity)) all-records)
    namespace-definition-syntax
    (into
     {}
     (keep
      (fn
       [[upstream-id root]]
       (let
        [name-form
         (get
          (into {} (map (juxt :form-id identity)) all-records)
          (second (:child-form-ids root)))]
        (when name-form [upstream-id (:syntax-id name-form)]))))
     root-by-upstream-id)
    binding-projections
    (mapv
     (fn
      [binding]
      (let
       [definition-syntax-id
        (or
         (get @declaration-syntax-by-upstream-id (:definition-syntax-id binding))
         (get namespace-definition-syntax (:definition-syntax-id binding))
         (:definition-syntax-id binding))
        semantic-binding
        {:capabilities (:capabilities binding),
         :package (:package binding),
         :name (:name binding),
         :target-set (:target-set binding),
         :upstream-binding-id (:binding-id binding),
         :type-ref (:type-ref binding),
         :definition-artifact-id (:definition-artifact-id binding),
         :effects (:effects binding),
         :safety (:safety binding),
         :kind (:kind binding),
         :semantic-span (:semantic-span binding),
         :scope-id
         (when
          (= :lexical (:binding-class binding))
          (get @projected-scope-by-upstream (:scope-id binding))),
         :definition-syntax-id definition-syntax-id,
         :profile-set (:profile-set binding),
         :namespace (:namespace binding),
         :visibility (:visibility binding),
         :binding-class (:binding-class binding)}
        projected-binding-id
        (reader-canonical-hash
         {:domain :gravity/sh07-projected-sh06-binding-v1,
          :source-revision-id source-revision-id,
          :binding (dissoc semantic-binding :upstream-binding-id :definition-artifact-id)})]
       {:upstream-binding-id (:binding-id binding),
        :projected-binding (assoc semantic-binding :binding-id projected-binding-id)}))
     (:binding-table resolved-analysis))
    projected-binding-id-by-upstream
    (into
     {}
     (map (juxt :upstream-binding-id (comp :binding-id :projected-binding)))
     binding-projections)
    bindings
    (mapv :projected-binding binding-projections)
    forms
    (mapv
     (fn [form] (assoc form :scope-id (get @scope-by-syntax-id (:syntax-id form))))
     all-records)
    resolutions
    (mapv
     (fn
      [resolution]
      (let
       [projected-binding-id
        (get projected-binding-id-by-upstream (:binding-id resolution))
        projected-resolution
        (assoc
         resolution
         :upstream-binding-id
         (:binding-id resolution)
         :binding-id
         projected-binding-id)]
       (when-not
        (and
         projected-binding-id
         (get form-by-syntax (:reference-syntax-id projected-resolution))
         (some (fn* [p1__1295#] (= projected-binding-id (:binding-id p1__1295#))) bindings))
        (throw
         (ex-info
          "SH-07 projected resolution is dangling"
          {:id "C6-VERIFY",
           :stage :core-lowering,
           :source-path source-path,
           :reason :sh06-projected-resolution-dangling,
           :resolution resolution})))
       projected-resolution))
     @occurrences)]
   (when-not
    (and
     (= (count binding-projections) (count projected-binding-id-by-upstream))
     (= (count bindings) (count (set (map :binding-id bindings)))))
    (throw
     (ex-info
      "SH-07 binding projection is not bijective"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :reason :sh06-binding-projection-not-bijective,
       :binding-count (count bindings),
       :upstream-count (count projected-binding-id-by-upstream)})))
   {:forms forms,
    :binding-table bindings,
    :alias-table (vec (:alias-table resolved-analysis)),
    :resolution-table resolutions})))
