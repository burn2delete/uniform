
(defn build-response
  "Construct the closed child wire envelope from process-local results."
  [preflight codec request execution]
  (let [H (:H codec)
        role (:role execution)
        process (self-process role)
        argv (into [(:command process)] (:arguments process))
        argv-id (H [:gravity/sh07-b51-process-argv-id-v1 argv])
        request-id
        (H [:gravity/sh07-b51-checker-request-id-v1 request])
        loaded (:loaded-namespace-receipt execution)
        source-closure-base
        {:role role
         :entrypoint
         'gravity.tools.sh07-b51-attempt32-checker-child
         :static-project-namespaces []
         :dynamic-project-namespaces
         (vec (remove
               #{'gravity.tools.sh07-b51-attempt32-checker-child}
               (:project-namespaces loaded)))
         :source-records (:source-records loaded)
         :source-fragments (:source-fragments loaded)
         :forbidden-namespaces
         [(symbol (:forbidden-namespace (get role-specs role)))]}
        source-closure
        (assoc source-closure-base :root
               (H [:gravity/sh07-b51-source-closure-root-v2
                   source-closure-base]))
        mxbean (runtime-mxbean)
        host-surface-base
        {:policy :gravity/sh07-b51-trusted-clojure-host-surface-v1
         :macro-vars [] :interop-members [] :state-primitives []
         :dynamic-load-primitives ['require 'load] :class-definitions []}
        host-surface
        (assoc host-surface-base :root
               (H [:gravity/sh07-b51-host-surface-root-v1
                   host-surface-base]))
        tcb-base
        {:role role :process process :runtime-mxbean mxbean
         :java {:logical-role :java :canonical-path (:command process)}
         :git {:logical-role :git :canonical-path "/usr/bin/git"}
         :classpath (:classpath-receipt preflight)
         :pre-manifest-tcb (:pre-manifest-tcb preflight)
         :eligibility-check (:eligibility-preflight preflight)
         :environment (:environment preflight)
         :source-closure source-closure
         :loaded-namespace-receipt loaded
         :host-surface host-surface}
        tcb (assoc tcb-base :manifest-id
                   (H [:gravity/sh07-b51-checker-child-tcb-manifest-v1
                       tcb-base]))
        base
        {:schema :gravity/sh07-b51-checker-child-response-v1
         :role role
         :transport
         {:argv-id argv-id
          :request-id request-id
          :response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}
         :result
         (select-keys execution
                      [:checker-identity :checker-output :source-read-receipt
                       :production :sentinel])
         :counters
         {:work {:vector [3 3 3 2 2] :total 13}
          :source-session
          (get-in execution [:source-read-receipt :counters])
          :process {:child-count 1 :start-count 1 :exit-count 1
                    :overlap-count 0}
          :ipc {:request-count 1 :response-count 1
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576
                :deadline-ms 3600000}}
         :tcb tcb
         :diagnostics []}
        response-id
        (H [:gravity/sh07-b51-response-preimage-id-v1
            (update base :transport dissoc :response-preimage-id)])]
    (assoc-in base [:transport :response-preimage-id] response-id)))

(defn validate-response-shape!
  "Small parent-safe top-level response check used by process tests."
  [expected-role response]
  (when-not (and (exact-keys? response checker-response-keys)
                 (= :gravity/sh07-b51-checker-child-response-v1
                    (:schema response))
                 (= expected-role (:role response))
                 (empty? (:diagnostics response)))
    (reject! :response-shape {:role expected-role}))
  response)

(defn- actual-preflight-input [arguments]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        process-arguments (some-> (optional-value (.arguments info)) vec)]
    (when-not (and command process-arguments)
      (reject! :child-argv-self-report))
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
    (let [preflight (validate-entry-preflight!
                     (actual-preflight-input arguments))
          request-frame (read-request-before-codec! System/in)
          request (:value request-frame)
          _ (validate-checker-request! (:role preflight) request)
          preflight (assoc preflight :content (:content request))
          pre-manifest (pre-manifest-tcb! preflight)
          codec (load-codec!)
          decoded ((:strict-read-wire codec) :request (:bytes request-frame))
          _ (when-not (= request decoded) (reject! :request-codec-parity))
          classpath (validate-complete-classpath! preflight codec)
          preflight (merge preflight classpath
                           {:pre-manifest-tcb pre-manifest})
          _ (validate-checker-request! (:role preflight) request)
          eligibility
          (validate-eligibility-five-operations! preflight codec request)
          preflight
          (assoc preflight
                 :eligibility-preflight (:eligibility-check eligibility)
                 :eligibility-check-root (:eligibility-check-root eligibility)
                 :pre-object-snapshot (:pre-object-snapshot eligibility))
          execution (execute-request! preflight codec request)
          response (build-response preflight codec request execution)
          bytes ((:W codec) response)]
      ((:bounded-byte-count! codec) :response (alength bytes))
      (.write System/out bytes)
      (.flush System/out))
    (catch Throwable _
      ;; Child rejection is deliberately payload-free: M owns diagnostics.
      (System/exit 2))))
