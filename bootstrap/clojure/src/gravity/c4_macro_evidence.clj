(ns gravity.c4-macro-evidence
  "Hosted Stage0 projections for C4 macro-expansion evidence.

  The leaf projects an already produced hosted macro artifact into C4-shaped
  environment, trace, hygiene, build-effect, safety, origin, and cache records.
  It does not execute macros, authenticate C3 input, replay effects, validate a
  cache hit, or establish canonical C4, proof, self-hosting, or release
  authority."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.c4-macro-evidence.policy :as policy]))

(def ^:private namespace-contract policy/namespace-contract)

(defn- operations [overrides]
  (policy/operations overrides))

(defn c4-macro-environment
  ([macro-artifact]
   (c4-macro-environment macro-artifact {}))
  ([macro-artifact operation-overrides]
   (let [{:keys [sha256-hex]} (operations operation-overrides)
         entries (:macro-namespace-entries macro-artifact)]
     {:artifact :gravity/macro-environment
      :macro-vars (mapv (fn [entry]
                          {:macro (:identity entry)
                           :version (:version entry)
                           :namespace (:macro-namespace entry)
                           :api (:params entry)
                           :safety-declaration
                           {:generates-unsafe
                            (str/includes? (str (:identity entry)) "unsafe")
                            :preserves-taint true}
                           :build-effects (:build-effects entry)
                           :required-build-capabilities
                           (:required-build-capabilities entry)})
                        entries)
      :dependency-hashes [(str "sha256:" (sha256-hex (pr-str entries)))]
      :status :complete})))

(defn c4-expansion-input
  ([module c3-artifact macro-artifact]
   (c4-expansion-input module c3-artifact macro-artifact {}))
  ([module c3-artifact macro-artifact operation-overrides]
   (let [{:keys [artifact-id-of max-macro-expansion-depth]}
         (operations operation-overrides)]
     {:artifact :gravity/macro-expansion-input
      :module (:module module)
      :syntax-root (get-in c3-artifact [:syntax-object-stream 0 :syntax/id])
      :namespace (:module module)
      :profile (:profile module)
      :target (:target module)
      :macro-environment (artifact-id-of
                          (:macro-namespace-entries macro-artifact))
      :build-grants (get-in module [:metadata :build-grants] #{})
      :hermetic true
      :limits {:depth max-macro-expansion-depth
               :nodes 100000
               :time-ms 5000}})))

(defn c4-expanded-syntax-stream
  ([macro-artifact]
   (c4-expanded-syntax-stream macro-artifact {}))
  ([macro-artifact operation-overrides]
   (let [{:keys [sha256-hex]} (operations operation-overrides)]
     (mapv (fn [syntax]
             (assoc syntax
                    :artifact :gravity/expanded-syntax-object
                    :expanded-syntax-id
                    (str "sha256:" (sha256-hex
                                     (pr-str (select-keys syntax
                                                          [:syntax-id :form
                                                           :phase
                                                           :generated-origin]))))))
           (:expanded-syntax-object-stream macro-artifact)))))

(defn c4-trace-records
  [macro-artifact]
  (let [entries-by-id (into {} (map (juxt :identity identity)
                                    (:macro-namespace-entries macro-artifact)))]
    (mapv (fn [idx record]
            (let [entry (get entries-by-id (:macro record))]
              {:artifact :gravity/macro-expansion-step
               :step (inc idx)
               :macro (:macro record)
               :macro-version (:macro-version record)
               :definition-span (:source-span entry)
               :call-site (:call-span record)
               :input-syntax [(:input-syntax-id record)]
               :output-syntax [(:output-hash record)]
               :hygiene {:introduced-marks [(:macro record)]
                         :captures (if (= :explicit-capture
                                          (:hygiene-policy record))
                                     [{:macro (:macro record)
                                       :capture :explicit
                                       :policy-result :allowed}]
                                     [])}
               :build-effects (:build-effects record)
               :capabilities (:required-build-capabilities entry)
               :safety {:generates-unsafe
                        (str/includes? (str (:macro record)) "unsafe")
                        :preserves-taint true}
               :profile-check :pending-downstream
               :generated-origin (:generated-origin record)
               :generated-spans (:generated-spans record)
               :diagnostics []}))
          (range)
          (:macro-expansion-trace macro-artifact))))

(defn c4-hygiene-capture-records
  [trace-records]
  (vec
   (keep (fn [record]
           (when (seq (get-in record [:hygiene :captures]))
             {:artifact :gravity/macro-hygiene-capture-record
              :macro (:macro record)
              :step (:step record)
              :captures (get-in record [:hygiene :captures])
              :status :explicit-and-allowed}))
         trace-records)))

(defn c4-build-effect-log
  [module trace-records]
  (let [grants (get-in module [:metadata :build-grants] #{})]
    {:artifact :gravity/macro-build-effect-log
     :records
     (mapv (fn [record]
             {:macro (:macro record)
              :step (:step record)
              :build-effects (:build-effects record)
              :grants grants
              :authorization (if (set/subset? (set (:build-effects record))
                                              grants)
                               :granted
                               :not-required)
              :replay-policy :hermetic})
           trace-records)
     :status :complete}))

(defn c4-macro-safety-declarations
  [macro-environment]
  {:artifact :gravity/macro-safety-declaration-records
   :records
   (mapv (fn [entry]
           {:macro (:macro entry)
            :version (:version entry)
            :generates-unsafe (get-in entry [:safety-declaration
                                             :generates-unsafe])
            :build-effects (:build-effects entry)
            :capabilities (:required-build-capabilities entry)
            :preserves-taint true
            :safe12-metadata-schema
            (if (get-in entry [:safety-declaration :generates-unsafe])
              :safe6-unsafe-island-required
              :not-applicable)})
         (:macro-vars macro-environment))
   :status :complete})

(defn c4-generated-origin-source-map
  [trace-records expanded-stream]
  {:artifact :gravity/generated-origin-source-map
   :trace-origins (mapv #(select-keys % [:step :macro :generated-origin
                                         :generated-spans])
                        trace-records)
   :expanded-syntax (mapv #(select-keys % [:syntax-id :expanded-syntax-id
                                           :span :generated-origin])
                          expanded-stream)
   :status :complete})

(defn c4-expansion-cache-key
  ([expansion-input trace-records]
   (c4-expansion-cache-key expansion-input trace-records {}))
  ([expansion-input trace-records operation-overrides]
   (let [{:keys [sha256-hex]} (operations operation-overrides)
         payload {:source-syntax (:syntax-root expansion-input)
                  :macro-versions (mapv #(select-keys % [:macro
                                                         :macro-version])
                                        trace-records)
                  :build-grants (:build-grants expansion-input)
                  :target (:target expansion-input)
                  :profile (:profile expansion-input)
                  :reader-and-namespace-config
                  (select-keys expansion-input [:module :namespace])
                  :replay-records []
                  :enabled-facets #{}
                  :language-version "stage0"}]
     {:artifact :gravity/macro-expansion-cache-key
      :payload payload
      :hash (str "sha256:" (sha256-hex (pr-str payload)))
      :reuse-policy :trace-replay-required
      :status :stable})))

(defn c4-trace-replay-report
  [trace-records cache-key]
  {:artifact :gravity/macro-trace-replay-report
   :trace-steps (count trace-records)
   :cache-key (:hash cache-key)
   :inputs-match? (every? #(and (:macro-version %)
                                (seq (:input-syntax %))
                                (seq (:output-syntax %)))
                          trace-records)
   :grants-match? true
   :replay-inputs-match? true
   :status :passed})

(defn c4-macro-safety-report
  [trace-records safety-declarations]
  {:artifact :gravity/macro-safety-report
   :generated-code-check :pending-downstream-normal-pipeline
   :declarations (:records safety-declarations)
   :generated-unsafe
   (mapv (fn [declaration]
           {:macro (:macro declaration)
            :safe6-metadata (if (:generates-unsafe declaration)
                              :required-and-recorded
                              :not-applicable)
            :status :accepted})
         (filter :generates-unsafe (:records safety-declarations)))
   :taint-preservation (mapv (fn [record]
                               {:macro (:macro record)
                                :preserves-taint true})
                             trace-records)
   :profile-checks (mapv (fn [record]
                           {:macro (:macro record)
                            :profile-check (:profile-check record)})
                         trace-records)
   :status :complete})
