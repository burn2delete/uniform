

(def sh06-resolution-component-paths
  [[:document-set [:document-set]]
   [:provenance [:provenance]]
   [:pass [:pass]]
   [:execution-boundary [:execution-boundary]]
   [:capability-based-proof [:capability-based-proof]]
   [:diagnostics [:diagnostics]]
   [:sh05-macro-artifact [:sh05-macro-artifact]]
   [:plan-binding [:gravity-resolution-boundary :plan-binding]]
   [:authenticated-resolution-request
    [:gravity-resolution-boundary :authenticated-resolution-request]]
   [:raw-template-result
    [:gravity-resolution-boundary :raw-template-result]]
   [:raw-analysis [:gravity-resolution-boundary :raw-analysis]]
   [:resolved-analysis [:gravity-resolution-boundary :resolved-analysis]]
   [:digest-requests [:gravity-resolution-boundary :digest-requests]]
   [:resolved-digests [:gravity-resolution-boundary :resolved-digests]]
   [:template-verification
    [:gravity-resolution-boundary :template-verification]]
   [:resolved-verification
    [:gravity-resolution-boundary :resolved-verification]]
   [:authenticated-envelope-descriptor
    [:gravity-resolution-boundary :authenticated-envelope-descriptor]]
   [:authenticated-envelope
    [:gravity-resolution-boundary :authenticated-envelope]]
   [:namespace-analysis [:namespace-analysis]]
   [:binding-table [:binding-table]]
   [:alias-table [:alias-table]]
   [:import-export-table [:import-export-table]]
   [:lexical-scope-graph [:lexical-scope-graph]]
   [:dependency-graph [:dependency-graph]]
   [:cross-profile-edge-report [:cross-profile-edge-report]]
   [:incremental-invalidation-keys [:incremental-invalidation-keys]]
   [:resolution-table [:resolution-table]]])

(defn sh06-resolution-carrier-validation
  [value bounds]
  (p15-s23-trusted-carrier-validation
   value :default-only
   (:maximum-carrier-nodes bounds)
   (:maximum-carrier-depth bounds)
   (:maximum-container-width bounds)))

(defn sh06-resolution-component-validations
  [artifact]
  (into
   (sorted-map)
   (map
    (fn [[component path]]
      [component
       (sh06-resolution-carrier-validation
        (get-in artifact path) sh06-resolution-transport-bounds)]))
   sh06-resolution-component-paths))

(defn- sh06-resolution-candidate-measurements
  [artifact]
  (let [aggregate
        (sh06-resolution-carrier-validation
         artifact sh06-resolution-diagnostic-measurement-bounds)
        trusted-aggregate? (= :passed (:status aggregate))
        boundary (when (and trusted-aggregate? (map? artifact))
                   (:gravity-resolution-boundary artifact))
        exact-shape?
        (and (map? artifact)
             (= sh06-resolution-artifact-keys (set (keys artifact)))
             (map? boundary)
             (= sh06-resolution-boundary-keys (set (keys boundary))))]
    {:aggregate aggregate
     :components
     (when exact-shape?
       (sh06-resolution-component-validations artifact))
     :trusted-exact-shape? (boolean exact-shape?)
     :measurement-only? true
     :authorizes-bound-change? false}))

(def sh06-resolution-pass-contract
  {:name :sh06-gravity-name-resolution
   :input :authenticated-sh05-expanded-syntax
   :output :authenticated-sh06-namespace-analysis
   :preserves [:source-spans :syntax-ids :macro-lineage
               :profile :target :effects :capabilities]
   :rejects c5-resolution-diagnostic-ids})

(def sh06-resolution-execution-boundary-contract
  {:resolution-authority :gravity
   :gravity-module 'gravity.resolution
   :plan-runner :clojure-stage0
   :digest-resolver :clojure-stage0
   :envelope-binder :clojure-stage0
   :compatibility-adapter :clojure-stage0
   :component-transport-bounds sh06-resolution-transport-bounds
   :aggregate-artifact-bounds sh06-resolution-artifact-bounds
   :self-hosted? false})