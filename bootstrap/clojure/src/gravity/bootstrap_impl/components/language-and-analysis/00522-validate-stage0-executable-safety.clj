

(defn validate-stage0-executable-safety!
  [module]
  (doseq [form (:forms module)
          unsafe-form (stage0-unsafe-forms form)]
    (validate-stage0-unsafe-island! module unsafe-form)))

(defn validate-stage0-source-safety!
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)]
    (validate-stage0-executable-safety! module)))

(defn validate-stage0-compiled-profile!
  [module]
  (let [source-path (:source-path module)
        writes? (some uses-println? (:forms module))]
    (when-not (= :hosted (:profile module))
      (fail! "P1-RUNTIME"
             "stage0 compiled execution requires the :hosted profile"
             {:source-span {:source source-path}
              :active-profile (:profile module)
              :target (:target module)
              :requested-runtime :clojure-stage0-jvm
              :policy-layer :profile
              :remediation "Use (:profile :hosted) for stage0 compiled executable modules or consume the output as an artifact boundary."}))
    (when (and writes? (not (contains? (:effects module) :io/write)))
      (fail! "P4-HOST-EFFECT"
             "hosted stdout requires the declared :io/write effect"
             {:source-span {:source source-path}
              :active-profile (:profile module)
              :target (:target module)
              :requested-effect :io/write
              :policy-layer :source
              :remediation "Add :io/write to the namespace effects before compiling the hosted executable."}))
    (when (and writes? (not (contains? (:capabilities module) :io/stdout)))
      (fail! "P4-HOST-CAPABILITY"
             "hosted stdout requires the :io/stdout capability"
             {:source-span {:source source-path}
              :active-profile (:profile module)
              :target (:target module)
              :requested-capability :io/stdout
              :policy-layer :source
              :remediation "Add :io/stdout to the namespace capabilities before compiling the hosted executable."}))))

(defn validate-stage0-source-profile!
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)]
    (validate-stage0-compiled-profile! module)))

(defn stage0-compiled-performance-manifest
  [module]
  {:profile (:profile module)
   :target (:target module)
   :source-effects (:effects module)
   :source-capabilities (:capabilities module)
   :metadata (:metadata module)})