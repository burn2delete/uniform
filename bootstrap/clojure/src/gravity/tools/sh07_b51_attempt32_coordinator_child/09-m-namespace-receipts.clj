
(defn- m-namespace-receipts [options codec topology]
  (let [libraries (loaded-libs)
        projects (->> libraries (filter project-library?) (sort-by str) vec)
        external (->> libraries (remove project-library?) (sort-by str) vec)
        forbidden
        (->> [(get-in topology
                      [:a :response :tcb :source-closure
                       :forbidden-namespaces 0])
              (get-in topology
                      [:b :response :tcb :source-closure
                       :forbidden-namespaces 0])]
             (filter symbol?) (sort-by str) vec)
        overlap (vec (filter (set forbidden) projects))
        records (loaded-project-source-records options projects)
        fragments
        ((ns-resolve 'gravity.tools.sh07-b51-attempt32-checker-identity
                     'loaded-source-fragments)
         (:source-authority options) records)
        loaded-base
        {:role :m :project-namespaces projects
         :external-namespaces external :source-records records
         :source-fragments fragments
         :forbidden-overlap overlap}
        _ (when (seq overlap)
            (reject! :m-checker-loaded {:overlap overlap}))
        loaded (assoc loaded-base :root
                      ((:H codec)
                       [:gravity/sh07-b51-loaded-namespace-root-v2
                        loaded-base]))
        source-base
        {:role :m
         :entrypoint 'gravity.tools.sh07-b51-attempt32-coordinator-child
         :static-project-namespaces []
         :dynamic-project-namespaces
         (vec (remove
               #{'gravity.tools.sh07-b51-attempt32-coordinator-child}
               projects))
         :source-records records :source-fragments fragments
         :forbidden-namespaces forbidden}
        source (assoc source-base :root
                      ((:H codec)
                       [:gravity/sh07-b51-source-closure-root-v2
                        source-base]))]
    {:loaded loaded :source source}))

(defn build-m-response
  "Stage six semantic payloads and construct M's closed response."
  [options codec topology]
  (let [descriptors (stage-payloads! options codec topology)
        a-receipt (child-process-receipt codec (:a topology) [2 3 4])
        b-receipt (child-process-receipt codec (:b topology) [6 7 8])
        source-receipt (get-in topology [:m-session :source-read-receipt])
        namespaces (m-namespace-receipts options codec topology)
        coordinator-base
        {:schema :gravity/sh07-b51-coordinator-receipt-v1
         :process {:role :m :pid (.pid (ProcessHandle/current))}
         :runtime-mxbean {:name (.getName (ManagementFactory/getRuntimeMXBean))}
         :java {:canonical-path (first (child-argv options :producer-census))}
         :git {:canonical-path "/usr/bin/git"}
         :pre-manifest-tcb (:pre-manifest-tcb options)
         :eligibility-check (:eligibility-preflight options)
         :classpath-checks []
         :environment (:environment options)
         :source-closure (:source namespaces)
         :load-events []
         :loaded-namespace-receipt (:loaded namespaces)
         :host-surface {:policy
                        :gravity/sh07-b51-trusted-clojure-host-surface-v1
                        :dynamic-load-primitives ['require 'load]}
         :source-read-receipt source-receipt
         :child-process-receipts [a-receipt b-receipt]
         :process-joins [(:process-join a-receipt)
                         (:process-join b-receipt)]
         :coordinator-events (:events topology)
         :coordinator-event-root (:event-root topology)
         :counters
         {:work {:vector [3 3 3 2 2] :total 13}
          :source-session (:counters source-receipt)
          :process {:builder-count 2 :child-count 2 :start-count 2
                    :exit-count 2 :overlap-count 0}
          :ipc {:request-count 2 :response-count 2
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576 :deadline-ms 3600000}}
         :a-validated-before-b-builder true
         :b-validated-before-m-session true
         :launcher-process-handle-id
         (current-process-handle-id options codec)
         :launcher-argv-id
         ((:H codec)
          [:gravity/sh07-b51-process-argv-id-v1 (:m-argv options)])
         :self-id-omits [[:receipt-id]]}
        coordinator-receipt
        (assoc coordinator-base :receipt-id
               ((:H codec)
                [:gravity/sh07-b51-coordinator-receipt-v1 coordinator-base]))
        base
        {:schema :gravity/sh07-b51-m-response-v1
         :status :accepted
         :content (:content options)
         :payload-descriptors descriptors
         :staging-receipt
         {:staging-path (:staging-root options)
          :evidence-relative-root (:evidence-relative-root options)
          :directory-mode "0700" :symlink-free true
          :payload-names (mapv :name descriptors)
          :payload-descriptors descriptors}
         :coordinator-receipt coordinator-receipt
         :source-read-receipt source-receipt
         :counters (:counters coordinator-receipt)
         :transport
         {:response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}}
        response-id
        ((:H codec)
         [:gravity/sh07-b51-response-preimage-id-v1
          (update base :transport dissoc :response-preimage-id)])]
    (assoc-in base [:transport :response-preimage-id] response-id)))

(defn- actual-preflight-input [arguments]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        process-arguments (some-> (optional-value (.arguments info)) vec)]
    (when-not (and command process-arguments)
      (reject! :m-argv-self-report))
    {:arguments (vec arguments)
     :environment (into {} (System/getenv))
     :working-directory
     (str (.normalize (.toAbsolutePath
                       (Paths/get (System/getProperty "user.dir")
                                  (make-array String 0)))))
     :classpath (System/getProperty "java.class.path")
     :process-argv (into [command] process-arguments)}))

(defn -main [& arguments]
  (try
    (let [preflight
          (validate-entry-preflight! (actual-preflight-input arguments))
          pre-manifest
          (pre-manifest-tcb!
           preflight
           'gravity.tools.sh07-b51-attempt32-coordinator-child
           "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj")
          codec (load-codec!)
          classpath (validate-complete-classpath! preflight codec)
          preflight (merge preflight classpath
                           {:pre-manifest-tcb pre-manifest})
          preflight (authenticate-eligibility-input! preflight codec)
          topology (run-process-topology! preflight codec)
          response (build-m-response preflight codec topology)
          bytes ((:bounded-wire-bytes codec) :response response)]
      (.write System/out bytes)
      (.flush System/out))
    (catch Throwable _
      ;; L owns the stable diagnostic.  Rejected M output is empty.
      (System/exit 2))))
