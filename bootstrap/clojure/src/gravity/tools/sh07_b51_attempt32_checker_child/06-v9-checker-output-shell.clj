(defn- validate-sh06-handoff! [role request products]
  (let [valid? (resolve-public! bootstrap-namespace
                "p15-s23-sh07-b51-sh06-authority-carrier-valid?")
        content (:content request)]
    (when-not (valid? (:commit content) (:tree content) products
                     (:sh06-authority-carrier products))
      (reject! :sh06-authority-carrier {:role role}))))

(defn- v9-checker-output-shell? [output]
  (let [certificate (:certificate output)]
    (and (= :accepted (:status output))
         (map? certificate)
         (contains? certificate :semantic-witness)
         (map? (:semantic-witness certificate))
         (= :gravity/sh07-b51-authentic-lowering-witness-v1
            (get-in certificate [:semantic-witness :schema]))
         (sha256-id? (get-in certificate [:semantic-witness :witness-id]))
         (contains? (:inputs certificate) :lowering-product-id)
         (sha256-id? (get-in certificate [:inputs :lowering-product-id])))))

(defn- result-record [H role status authority]
  (let [base {:schema (if authority
                        :gravity/sh07-b51-checker-production-v1
                        :gravity/sh07-b51-checker-sentinel-v1)
              :role role :status status :authority authority}
        artifact-id
        (H [:gravity/sh07-b51-checker-result-artifact-id-v1 base])
        with-artifact (assoc base :artifact-id artifact-id)
        output-id
        (H [:gravity/sh07-b51-checker-result-output-id-v1
            with-artifact])]
    (assoc with-artifact :output-id output-id)))

(defn- optional-value [^Optional value]
  (when (.isPresent value) (.get value)))

(defn self-process
  "Capture the exact current process self-report (without exit state)."
  [role]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        parent (optional-value (.parent handle))
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        started (optional-value (.startInstant info))]
    (when-not (and parent command arguments started)
      (reject! :self-process))
    {:role role
     :pid (.pid handle)
     :parent-pid (.pid ^ProcessHandle parent)
     :command command
     :arguments arguments
     :start-instant (str ^Instant started)}))

(defn runtime-mxbean []
  (let [bean (ManagementFactory/getRuntimeMXBean)
        name (.getName bean)
        [_ pid host] (re-matches #"([0-9]+)@(.+)" name)]
    (when-not (and pid host) (reject! :runtime-mxbean {:name name}))
    {:name name
     :pid (Long/parseLong pid)
     :host host
     :start-time-millis (.getStartTime bean)
     :vm-name (.getVmName bean)
     :vm-vendor (.getVmVendor bean)
     :vm-version (.getVmVersion bean)
     :java-version (System/getProperty "java.version")
     :java-vendor (System/getProperty "java.vendor")
     :java-class-path (System/getProperty "java.class.path")}))

(defn- project-library? [namespace]
  (str/starts-with? (str namespace) "gravity."))

(defn- source-record-index [source-authority]
  (reduce
   (fn [result record]
     (if (or (str/ends-with? (:path record) ".clj")
             (str/ends-with? (:path record) ".cljc"))
       (if-let [namespace
                (some-> (source-namespace-core! (:bytes record) (:path record))
                        :namespace)]
         (do
           (when (contains? result namespace)
             (reject! :loaded-namespace-source-duplicate
                      {:namespace namespace}))
           (assoc result namespace record))
         result)
       result))
   {} (vals (:source-objects source-authority))))

(defn- loaded-project-source-records
  [source-authority project-namespaces]
  (let [index (source-record-index source-authority)
        coordinate
        {:artifact 'project/bootstrap-clojure-src
         :commit (get-in source-authority [:content :commit])
         :tree (get-in source-authority [:content :tree])}]
    (mapv
     (fn [namespace]
       (let [record (get index namespace)]
         (when-not record
           (reject! :loaded-namespace-source-missing
                    {:namespace namespace}))
         {:namespace namespace :logical-coordinate coordinate
          :relative-path (:path record) :blob-oid (:blob-oid record)
          :byte-count (:byte-count record) :sha256 (:raw-sha256 record)}))
     project-namespaces)))

(defn- loaded-namespace-receipt
  [H role selected forbidden source-authority]
  (let [libraries (loaded-libs)
        projects (->> libraries (filter project-library?) (sort-by str) vec)
        external (->> libraries (remove project-library?) (sort-by str) vec)
        records (loaded-project-source-records source-authority projects)
        fragments ((resolve-public! identity-namespace "loaded-source-fragments")
                   source-authority records)
        base {:role role
              :project-namespaces projects
              :external-namespaces external
              :source-records records
              :source-fragments fragments
              :forbidden-overlap
              (vec (filter #{(symbol forbidden)} projects))}]
    (when-not (and (some #{(symbol selected)} projects)
                   (empty? (:forbidden-overlap base)))
      (reject! :checker-isolation
               {:selected selected :forbidden forbidden
                :loaded projects}))
    (assoc base :root
           (H [:gravity/sh07-b51-loaded-namespace-root-v2 base]))))

(defn execute-request!
  "Run one selected checker after strict request and role validation.

  The selected checker is loaded before the arity-two source constructor so
  that the constructor can privately derive the process-local role.  Checker
  semantics run only after the constructor returns its finalized receipt."
  [preflight codec request]
  (let [role (:role preflight)
        spec (get role-specs role)
        H (:H codec)]
    (validate-checker-request! role request)
    (require (symbol bootstrap-namespace))
    (require (symbol identity-namespace))
    ;; This is the sole role-dependent dynamic project load in the child.
    (require (symbol (:checker-namespace spec)))
    (when (contains? (loaded-libs) (symbol (:forbidden-namespace spec)))
      (reject! :checker-isolation {:role role}))
    (let [builder
          (resolve-public!
           bootstrap-namespace
           "p15-s23-sh07-b51-build-receipted-bundle-core-products")
          checker-fn
          (resolve-public! (:checker-namespace spec)
                           (:checker-entrypoint spec))
          identity-fn
          (resolve-public! identity-namespace "checker-identity-manifest")
          content (:content request)
          products
          (builder (:commit content) (:tree content))
          _ (validate-receipted-products! H role request products)
          manifest
          (identity-fn (:source-authority preflight) role)
          envelope (build-checker-envelope H role request products manifest
                                           preflight)
          output (checker-fn envelope)
          _ (when-not (v9-checker-output-shell? output)
              (reject! :checker-output {:role role :status (:status output)}))
          production (result-record H role :accepted true)
          sentinel (result-record H role :accepted false)
          loaded
          (loaded-namespace-receipt H role (:checker-namespace spec)
                                    (:forbidden-namespace spec)
                                    (:source-authority preflight))]
      {:role role
       :checker-identity manifest
       :checker-output output
       :source-read-receipt (:source-read-receipt products)
       :source-session-id
       (source-session-id H (:source-read-receipt products))
       :production production
       :sentinel sentinel
       :loaded-namespace-receipt loaded})))
