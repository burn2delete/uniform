(ns gravity.build.invocation
  "Explicit local HOST build entry. Only the real CLI/host may call this API;
  it is not registered as a Gravity builtin or a source-data grant factory."
  (:require [clojure.edn :as edn]
            [gravity.build.declared-inputs :as inputs]))

(defn- reject! [id reason status]
  (throw (ex-info "Local declared-input build refused"
                  {:id id :reason reason :severity :error :profile :meta :target :jvm
                   :phase :build :requested-capability :fs/read
                   :required-effect :build/read-file :cli-exit-code status})))

(defn- usage! [] (reject! "T1006" :command-shape 2))
(defn- denied! [] (reject! "T1003" :local-build-read-denied 6))
(defn- freeze [value] (#'inputs/freeze value))
(defn- fields! [value fields]
  (when-not (and (map? value) (= fields (set (keys value)))) (usage!))
  value)

(defn- read-data [text]
  (try
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
      (let [eof (Object.)
            refuse-tag (fn [& _] (usage!))
            options {:eof eof :readers {'inst refuse-tag 'uuid refuse-tag}
                     :default refuse-tag}
            value (edn/read options reader)]
        (when (or (identical? eof value)
                  (not (identical? eof (edn/read options reader)))) (usage!))
        (freeze value)))
    (catch Exception _ (usage!))))

(defn- parse-command [arguments]
  ;; Guard the outer native vector before seq/lookup; no target is opened.
  (when-not (and (= clojure.lang.PersistentVector (class arguments))
                 (nil? (meta arguments))
                 (every? #(= String (class %)) arguments)) (usage!))
  (freeze arguments)
  (when-not (= "build" (first arguments)) (usage!))
  (let [options
        (loop [remaining (next arguments) options {}]
          (if (empty? remaining) options
            (let [[flag value & more] remaining]
              (when-not (and value (not (empty? value))
                             (contains? #{"--reader-inputs" "--principal" "--profile"
                                          "--target" "--allow-read" "--package-policy"
                                          "--workspace-policy" "--format"} flag))
                (usage!))
              (if (= "--allow-read" flag)
                (do (when (contains? (get options flag #{}) value) (usage!))
                    (recur more (update options flag (fnil conj #{}) value)))
                (do (when (contains? options flag) (usage!))
                    (recur more (assoc options flag value)))))))]
    (when-not (= #{"--reader-inputs" "--principal" "--profile" "--target"
                    "--allow-read" "--package-policy" "--workspace-policy" "--format"}
                  (set (keys options)))
      ;; A missing explicit permission or policy is an authority denial, not
      ;; something reconstructed from the request/project/source declaration.
      (if (or (nil? (get options "--allow-read"))
              (nil? (get options "--package-policy"))
              (nil? (get options "--workspace-policy"))) (denied!) (usage!)))
    (when-not (and (= "meta" (get options "--profile"))
                   (= "jvm" (get options "--target"))
                   (= "json" (get options "--format"))) (usage!))
    options))

(defn- constrain! [policy principal resources]
  ;; These are independently supplied HOST policy inputs, never authority
  ;; obtained by loading a project map or by trusting source declarations.
  (fields! policy #{:principals :profiles :targets :provider :provider-version
                    :phase :lifetime :scope :effects :capabilities :audit :replay})
  (when-not (and (set? (:principals policy)) (contains? (:principals policy) principal)
                 (set? (:profiles policy)) (contains? (:profiles policy) :meta)
                 (set? (:targets policy)) (contains? (:targets policy) :jvm)
                 (= 'gravity.build/hermetic-inputs (:provider policy))
                 (= "1" (:provider-version policy))
                 (= :build (:phase policy)) (= :invocation (:lifetime policy))
                 (set? (:scope policy)) (every? #(contains? (:scope policy) %) resources)
                 (= #{:build/read-file} (:effects policy))
                 (= #{:fs/read} (:capabilities policy))
                 (= :required (:audit policy)) (= :recorded (:replay policy)))
    (denied!)))

(defn- reader-build!
  [_capability _request]
  ;; Fixed production link, NOT caller-selected code or a host read dressed up
  ;; as a Gravity result. Stage2/Reader owners must replace this exact gap.
  (reject! "T1004" :reader-effectful-bridge-unavailable 4))

(defn run-declared-input-build!
  "Actual local host entry from gravity.cli.dispatch. Explicit argv grants
  are authority at this host boundary; policy/request DATA can only narrow it.
  No target file, filesystem provider, callback or result is accepted as proof.
  The fixed Reader bridge is still absent and production calls fail closed."
  [arguments]
  (let [options (parse-command arguments)
        principal (get options "--principal")
        resources (get options "--allow-read")
        package-policy (read-data (get options "--package-policy"))
        workspace-policy (read-data (get options "--workspace-policy"))
        request (read-data (get options "--reader-inputs"))]
    (constrain! package-policy principal resources)
    (constrain! workspace-policy principal resources)
    (fields! request #{:source :source-requirements :inputs :implementation :policy-id})
    (fields! (:source request) #{:path :text})
    (when-not (and (= String (class (get-in request [:source :path])))
                   (not (empty? (get-in request [:source :path])))
                   (= String (class (get-in request [:source :text])))) (usage!))
    (constrain! (:source-requirements request) principal resources)
    (let [grant {:id "local-command-build-read" :principal principal
                 :provider 'gravity.build/hermetic-inputs :provider-version "1"
                 :profile :meta :target :jvm :phase :build :lifetime :invocation
                 :scope resources :effects #{:build/read-file} :capabilities #{:fs/read}
                 :audit :required :replay :recorded :handler 'gravity.reader/read-input
                 :implementation (:implementation request) :policy-id (:policy-id request)}]
      ;; Issuance comes only after the outer command permission and all policy
      ;; intersections. The capability never appears in source or CLI output.
      (#'inputs/call-with-authorized-inputs
       grant (:inputs request)
       (fn [capability] (reader-build! capability request))))))

(defn diagnostic-json
  "Closed, redacted JSON failure report for this command; no input text/paths.
  Only implementation-owned error enums are rendered."
  [error]
  (let [data (ex-data error)
        reason (:reason data)
        known #{:command-shape :local-build-read-denied
                :reader-effectful-bridge-unavailable}
        reason (if (contains? known reason) reason :build-input-rejected)
        code (if (contains? #{"T1006" "T1003" "T1004"} (:id data)) (:id data) "T1003")
        status (case code "T1006" 2 "T1004" 4 6)]
    {:exit-code status
     :json (str "{\"status\":\"rejected\",\"code\":\"" code
                "\",\"reason\":\"" (name reason)
                "\",\"profile\":\"meta\",\"target\":\"jvm\","
                "\"phase\":\"build\",\"capability\":\"fs/read\","
                "\"exitCode\":" status ",\"runtimeAccepted\":false}")}))
