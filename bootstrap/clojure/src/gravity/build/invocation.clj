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
  [capability request]
  ;; Resolve only this fixed normal constructor, after permission/preflight and
  ;; inside the original lexical issuer. Do not wrap its original failures.
  ((requiring-resolve 'gravity.bootstrap/reader-declared-input-build!) capability request))

(defn run-declared-input-build!
  "Actual local host entry from gravity.cli.dispatch. Explicit argv grants
  are authority at this host boundary; policy/request DATA can only narrow it.
  No target file, filesystem provider, callback or result is accepted as proof.
  The fixed normal Reader constructor must complete within the issuer lifetime."
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

(defn success-json
  "Closed output projection of the constructor's verified C2 value. This is
  presentation, not a grant or artifact authentication API. Never serialize
  source text, input contents, internal envelopes or authority objects."
  [artifact]
  (letfn [(native-map! [value]
            (when-not (and (contains? #{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap}
                                     (class value))
                           (nil? (meta value))
                           (every? #(= clojure.lang.Keyword (class %)) (keys value)))
              (reject! "T1004" :reader-build-result-invalid 4)))
          (hash! [value]
            (when-not (and (= String (class value))
                           (re-matches #"sha256:[0-9a-f]{64}" value))
              (reject! "T1004" :reader-build-result-invalid 4))
            value)]
    (native-map! artifact)
    (when-not (and (= clojure.lang.Keyword (class (:kind artifact)))
                   (= :gravity/stage0-c2-reader-document-artifact (:kind artifact))
                   (or (nil? (:status artifact))
                       (and (= clojure.lang.Keyword (class (:status artifact)))
                            (contains? #{:accepted :passed} (:status artifact))))
                   (or (not (contains? artifact :exit-code))
                       (and (= Long (class (:exit-code artifact))) (= 0 (:exit-code artifact)))))
      (reject! "T1004" :reader-build-result-invalid 4))
    (let [unit (:source-unit-record artifact)]
      (native-map! unit)
      (let [artifact-id (hash! (:artifact-id artifact))
            source-id (hash! (:source-id unit))
            bytes-hash (hash! (:bytes-hash unit))]
        {:exit-code 0
         :json (str "{\"status\":\"passed\",\"code\":null,"
                    "\"artifactKind\":\"gravity/stage0-c2-reader-document-artifact\","
                    "\"artifactId\":\"" artifact-id "\",\"sourceId\":\"" source-id
                    "\",\"sourceBytesHash\":\"" bytes-hash
                    "\",\"profile\":\"meta\",\"target\":\"jvm\",\"phase\":\"build\","
                    "\"capability\":\"fs/read\",\"exitCode\":0,\"runtimeAccepted\":false}")}))))

(defn diagnostic-json
  "Closed, redacted JSON failure report for this command; no input text/paths.
  Only implementation-owned error enums are rendered."
  [error]
  (let [data (ex-data error)
        reason (:reason data)
        known #{:command-shape :local-build-read-denied
                :reader-effectful-bridge-unavailable :reader-build-result-invalid}
        reason (if (and (= clojure.lang.Keyword (class reason)) (contains? known reason))
                 reason :build-input-rejected)
        code (cond
               (and (= String (class (:id data)))
                    (contains? #{"T1006" "T1003" "T1004"} (:id data))) (:id data)
               (= "L15-CONTRACT" (:id data)) "T1003"
               :else "T1004")
        status (case code "T1006" 2 "T1004" 4 6)]
    {:exit-code status
     :json (str "{\"status\":\"rejected\",\"code\":\"" code
                "\",\"reason\":\"" (name reason)
                "\",\"profile\":\"meta\",\"target\":\"jvm\","
                "\"phase\":\"build\",\"capability\":\"fs/read\","
                "\"exitCode\":" status ",\"runtimeAccepted\":false}")}))
