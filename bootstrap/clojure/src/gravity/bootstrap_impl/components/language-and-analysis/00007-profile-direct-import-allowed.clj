

(defn profile-direct-import-allowed?
  [consumer-profile producer-profile]
  (module-analysis-call
   :profile-direct-import-allowed?
   module-analysis/profile-direct-import-allowed?
   consumer-profile producer-profile))