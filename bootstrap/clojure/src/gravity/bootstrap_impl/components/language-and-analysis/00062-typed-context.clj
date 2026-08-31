

(defn typed-context
  [module]
  (atom {:profile (:profile module)
         :target (:target module)
         :safety (:safety module)
         :declared-effects (:effects module)
         :declared-capabilities (:capabilities module)
         :build-grants (or (get-in module [:metadata :build-grants]) #{})
         :replay-records (or (get-in module [:metadata :replay-records]) #{})
         :replay-policy (or (get-in module [:metadata :replay-policy]) #{})
         :declared-inputs (or (get-in module [:metadata :declared-inputs]) #{})
         :target-manifests (or (get-in module [:metadata :target-manifests]) #{})
         :hermetic? (true? (get-in module [:metadata :hermetic]))
         :cache-policy (or (get-in module [:metadata :cache-policy]) :default)
         :language-facets (or (get-in module [:metadata :language-facets]) #{})
         :provider-policy (or (get-in module [:metadata :provider-policy]) {})
         :provider-grants (or (get-in module [:metadata :provider-grants]) #{})
         :handler-grants (or (get-in module [:metadata :handler-grants]) #{})
         :handler-covered-effects #{}
         :handler-covered-capabilities #{}
         :variable-types {}
         :linear-resources {}
         :moved-values {}
         :task-scope-id nil
         :in-task-scope? false}))