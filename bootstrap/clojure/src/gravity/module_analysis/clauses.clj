(ns gravity.module-analysis.clauses)

(defn require-ns
  [{:keys [ns-form? fail! source-span]} source-path forms]
  (let [first-form (first forms)]
    (when-not (ns-form? first-form)
      (fail! "L3-NS-MISSING"
             "Gravity source must start with an ns form"
             {:source-span (source-span source-path 0)
              :remediation "Add an ns form with :profile, :effects, and :capabilities clauses."}))
    first-form))

(defn parse-clause
  [{:keys [fail! source-span]} source-path clause]
  (when-not (and (seq? clause) (keyword? (first clause)))
    (fail! "L3-NS-CLAUSE"
           "namespace clause must be a list starting with a keyword"
           {:source-span (source-span source-path 0)
            :clause clause
            :remediation "Use clauses such as (:profile :hosted) or (:effects #{:io/write})."}))
  [(first clause) (vec (rest clause))])

(defn single-clause-value
  [{:keys [fail! source-span]} source-path clause-map key required?]
  (let [values (get clause-map key)]
    (cond
      (and required? (empty? values))
      (fail! "L3-NS-MISSING"
             (str "namespace is missing " key " clause")
             {:source-span (source-span source-path 0)
              :missing key
              :remediation "Declare the required namespace clause."})

      (> (count values) 1)
      (fail! "L3-PROFILE-MULTIPLE"
             (str "namespace declares " key " more than once")
             {:source-span (source-span source-path 0)
              :clause key
              :remediation "Keep one active implementation profile/target clause."})

      :else (first values))))

(defn clause-args
  [clause-map key]
  (vec (mapcat identity (get clause-map key))))
