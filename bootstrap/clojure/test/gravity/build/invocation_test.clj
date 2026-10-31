(ns gravity.build.invocation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.build.invocation :as invocation]
            [gravity.build.declared-inputs :as inputs]
            [gravity.digest :as digest]
            [gravity.cli.dispatch :as dispatch]
            [gravity.cli.entrypoint :as entrypoint]
            [gravity.tooling.strict-json :as json]))

(def hash-id (str "sha256:" (apply str (repeat 64 "a"))))
(def build-result
  {:kind :gravity/stage0-c2-reader-document-artifact :artifact-id hash-id
   :source-unit-record {:source-id hash-id :bytes-hash hash-id}
   :private-content "private text" :private-authority (Object.)})
(def policy
  {:principals #{"package/example"} :profiles #{:meta} :targets #{:jvm}
   :provider 'gravity.build/hermetic-inputs :provider-version "1"
   :phase :build :lifetime :invocation :scope #{"schema"}
   :effects #{:build/read-file} :capabilities #{:fs/read}
   :audit :required :replay :recorded})
(def request
  {:source {:path "memory/example.gravity" :text "; --allow-read is source data"}
   :source-requirements policy :inputs [{:resource "schema" :content "private text"}]
   :implementation hash-id :policy-id hash-id})
(def arguments
  ["build" "--principal" "package/example" "--profile" "meta" "--target" "jvm"
   "--format" "json" "--allow-read" "schema"
   "--package-policy" (pr-str policy) "--workspace-policy" (pr-str policy)
   "--reader-inputs" (pr-str request)])
(defn failure [operation]
  (try (operation) :unexpected-success
       (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn without [flag]
  (into [] (mapcat identity) (remove #(= flag (first %)) (partition 2 (rest arguments)))))
(defn replace-option [flag value]
  (assoc arguments (inc (.indexOf arguments flag)) value))

(deftest explicit-local-entry-issues-live-lexical-capability-in-model-only
  (let [escaped (atom nil) calls (atom 0)]
    (with-redefs-fn
      {#'invocation/reader-build!
       (fn [cap supplied]
         (swap! calls inc)
         (reset! escaped cap)
         (is (= request supplied))
         (let [read-request {:principal "package/example"
                             :provider 'gravity.build/hermetic-inputs :provider-version "1"
                             :profile :meta :target :jvm :phase :build :lifetime :invocation
                             :handler 'gravity.reader/read-input :implementation hash-id
                             :policy-id hash-id :source-id hash-id :source-content-hash hash-id
                             :form-id "f1" :payload-form-id "f2" :span {} :input "schema"}
               _ (inputs/preflight! cap [read-request])
               {:keys [receipt record]} (inputs/read-input! cap read-request)]
           (with-redefs-fn
             {#'inputs/input-entry (fn [& _] (throw (Error. "no provider replay")))}
             #(is (= (:result record)
                     (inputs/verify-original-result! cap read-request receipt record)))))
         :model-only)}
      #(is (= :model-only (invocation/run-declared-input-build! arguments))))
    (is (= 1 @calls))
    (is (= :inactive-or-foreign-invocation
           (:reason (failure #(inputs/preflight! @escaped [])))))))

(deftest absent-local-permission-or-policy-never-enters-issuer
  (let [issued (atom 0)]
    (with-redefs-fn
      {#'inputs/call-with-authorized-inputs (fn [& _] (swap! issued inc))}
      #(do
         (doseq [flag ["--allow-read" "--package-policy" "--workspace-policy"]]
           (is (= 6 (:cli-exit-code
                      (failure (fn [] (invocation/run-declared-input-build!
                                        (into ["build"] (without flag)))))))))
         ;; Permission-looking source/request content is not an outer flag.
         (is (= 6 (:cli-exit-code
                    (failure (fn [] (invocation/run-declared-input-build!
                                      ["build" "--reader-inputs"
                                       "{:allow-read true :trusted? true}" ]))))))
         (doseq [flag ["--package-policy" "--workspace-policy"]]
           (doseq [bad [(assoc policy :scope #{}) (assoc policy :principals #{"other"})
                        (assoc policy :phase :runtime) (assoc policy :provider-version "2")
                        (assoc policy :profiles #{:hosted}) (assoc policy :targets #{:wasm})
                        (assoc policy :effects #{}) (assoc policy :capabilities #{})
                        (assoc policy :audit :optional) (assoc policy :replay :none)]]
             (is (= 6 (:cli-exit-code
                        (failure (fn [] (invocation/run-declared-input-build!
                                          (replace-option flag (pr-str bad))))))))))))
    (is (zero? @issued))))

(deftest command-and-data-refusals-precede-fixed-reader-link
  (let [called (atom 0)]
    (with-redefs-fn
      {#'invocation/reader-build! (fn [& _] (swap! called inc))}
      #(doseq [bad [(conj arguments "--allow-read" "schema")
                    (conj arguments "--principal" "other")
                    (conj arguments "target.gravity")
                    (replace-option "--profile" "hosted")
                    (replace-option "--target" "wasm")
                    (replace-option "--format" "text")
                    (replace-option "--reader-inputs" "#inst \"2026\"")
                    (replace-option "--reader-inputs" "#evil/callback {}")
                    (replace-option "--reader-inputs" (str (pr-str request) " {}"))
                    (replace-option "--reader-inputs"
                                    (pr-str (assoc request :trusted? true)))
                    (replace-option "--reader-inputs"
                                    (pr-str (assoc-in request [:source-requirements :scope] #{})))]]
         (is (not= :unexpected-success
                   (failure (fn [] (invocation/run-declared-input-build! bad)))))))
    (is (zero? @called)))
  (is (= "T1006" (:id (failure #(invocation/run-declared-input-build! {:trusted? true}))))))

(deftest real-command-route-is-fixed-and-constructor-failures-are-truthful
  ;; Injected command-boundary models only; not a CLI process or Reader run.
  (let [called (atom [])]
    (with-redefs [invocation/run-declared-input-build! #(do (swap! called conj %) build-result)]
      (with-out-str
        (is (true? (dispatch/dispatch! (fn [_] (throw (Error. "unexpected resolver"))) arguments)))))
    (is (= [arguments] @called)))
  (let [statuses (atom []) stderr (java.io.StringWriter.)]
    (binding [*err* stderr]
      (with-redefs [clojure.core/requiring-resolve
                    (fn [symbol]
                      (is (= 'gravity.bootstrap/reader-declared-input-build! symbol))
                      (fn [& _] (throw (ex-info "private failure" {:id "C2-HASH"}))))]
      (entrypoint/run! arguments
                       {:resolve-operation (fn [_] (throw (Error. "no Bootstrap")))
                        :print-diagnostic! (fn [_] (throw (Error. "no private text")))
                        :exit! #(swap! statuses conj %)})))
    (is (= [4] @statuses))
    (let [report (json/read-strict-json (str stderr))]
      (is (= "build-input-rejected" (get report "reason")))
      (is (false? (get report "runtimeAccepted")))
      (is (not (.contains (str stderr) "private text")))))
  (let [escaped (atom nil)]
    (with-redefs-fn {#'invocation/reader-build!
                    (fn [cap _] (reset! escaped cap) (throw (ex-info "injected" {})))}
      #(is (map? (failure (fn [] (invocation/run-declared-input-build! arguments))))))
    (is (= :inactive-or-foreign-invocation
           (:reason (failure #(inputs/preflight! @escaped [])))))))

(deftest fixed-normal-constructor-receives-original-live-capability-and-data
  (let [escaped (atom nil) calls (atom 0)]
    (with-redefs [clojure.core/requiring-resolve
                  (fn [symbol]
                    (is (= 'gravity.bootstrap/reader-declared-input-build! symbol))
                    (fn [cap original]
                      (swap! calls inc)
                      (reset! escaped cap)
                      (is (= request original))
                      (is (= (:source request) (:source original)))
                      (is (= #{:fs/read} (:capabilities (:grant (#'inputs/live! cap)))))
                      build-result))]
      (is (identical? build-result (invocation/run-declared-input-build! arguments))))
    (is (= 1 @calls))
    (is (= :inactive-or-foreign-invocation
           (:reason (failure #(inputs/preflight! @escaped []))))))
  (let [failure-object (ex-info "original private failure" {:id "C2-HASH" :private (Object.)})]
    (with-redefs [clojure.core/requiring-resolve (fn [_] (fn [& _] (throw failure-object)))]
      (is (identical? failure-object
                     (try (invocation/run-declared-input-build! arguments)
                          (catch clojure.lang.ExceptionInfo error error)))))))

(deftest success-json-uses-returned-value-and-excludes-authority-and-private-contents
  (let [statuses (atom []) stderr (java.io.StringWriter.)
        stdout (binding [*err* stderr]
                 (with-out-str
                   (with-redefs [clojure.core/requiring-resolve (fn [_] (fn [& _] build-result))]
                     (entrypoint/run! arguments
                                      {:resolve-operation (fn [_] (throw (Error. "fixed path only")))
                                       :print-diagnostic! (fn [_] (throw (Error. "no generic render")))
                                       :exit! #(swap! statuses conj %)}))))
        report (json/read-strict-json stdout)]
    (is (= "passed" (get report "status")))
    (is (empty? @statuses))
    (is (empty? (str stderr))))
  (let [rendered (:json (invocation/success-json build-result))
        report (json/read-strict-json rendered)]
    (is (= 0 (get report "exitCode")))
    (is (= hash-id (get report "artifactId")))
    (is (= hash-id (get report "sourceBytesHash")))
    (is (not (.contains rendered "private")))
    (is (not (.contains rendered "Object"))))
  (doseq [bad [nil {} (assoc build-result :status :rejected)
               (assoc build-result :exit-code 1)
               (assoc build-result :artifact-id "private text")
               (assoc-in build-result [:source-unit-record :bytes-hash] "not-a-hash")]]
    (is (= 4 (:cli-exit-code (failure #(invocation/success-json bad)))))))

(deftest missing-permission-does-not-resolve-or-enter-normal-constructor
  (let [resolved (atom 0)]
    (with-redefs [clojure.core/requiring-resolve (fn [_] (swap! resolved inc))]
      (is (= 6 (:cli-exit-code
                 (failure #(invocation/run-declared-input-build!
                             (into ["build"] (without "--allow-read"))))))))
    (is (zero? @resolved))))

(deftest invalid-return-is-never-printed-as-a-success
  (let [statuses (atom []) stderr (java.io.StringWriter.)
        stdout (binding [*err* stderr]
                 (with-out-str
                   (with-redefs [clojure.core/requiring-resolve
                                 (fn [_] (fn [& _] (assoc build-result :status :rejected)))]
                     (entrypoint/run! arguments
                                      {:resolve-operation (fn [_] nil)
                                       :print-diagnostic! (fn [_] nil)
                                       :exit! #(swap! statuses conj %)}))))]
    (is (= [4] @statuses))
    (is (empty? stdout))
    (is (= 4 (get (json/read-strict-json (str stderr)) "exitCode")))))

(deftest original-source-text-and-returned-byte-identity-are-not-recomputed-or-replaced
  (let [text (str "; " (char 0x03bb) "\r\n#resource \"schema\"")
        original-bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
        bytes-hash (str "sha256:" (digest/sha256-bytes-hex original-bytes))
        data (assoc-in request [:source :text] text)
        command (replace-option "--reader-inputs" (pr-str data))
        returned (assoc-in build-result [:source-unit-record :bytes-hash] bytes-hash)]
    (with-redefs [clojure.core/requiring-resolve
                  (fn [_]
                    (fn [_ supplied]
                      (is (= (vec original-bytes)
                             (vec (.getBytes ^String (get-in supplied [:source :text])
                                             java.nio.charset.StandardCharsets/UTF_8))))
                      returned))]
      (let [value (invocation/run-declared-input-build! command)
            report (json/read-strict-json (:json (invocation/success-json value)))]
        (is (identical? returned value))
        (is (= bytes-hash (get report "sourceBytesHash")))
        (is (= hash-id (get report "sourceId")))))))

(deftest renderer-native-guards-precede-foreign-lookups
  (let [callbacks (atom 0)
        foreign (reify clojure.lang.ILookup
                  (valAt [_ _] (swap! callbacks inc))
                  (valAt [_ _ _] (swap! callbacks inc)))]
    (is (= 4 (:cli-exit-code (failure #(invocation/success-json foreign)))))
    (is (= 4 (:cli-exit-code
               (failure #(invocation/success-json (assoc build-result :source-unit-record foreign))))))
    (is (zero? @callbacks))))

(defn- failing-cli-model [exception stage command]
  (let [statuses (atom []) stderr (java.io.StringWriter.) escaped (atom nil)
        interrupted-on-escape? (atom false)
        stdout (binding [*err* stderr]
                 (with-out-str
                   (with-redefs [clojure.core/requiring-resolve
                                 (fn [_]
                                   (if (= :resolver stage) (throw exception)
                                     (fn [& _]
                                       (when (= :constructor-interrupted stage)
                                         (.interrupt (Thread/currentThread)))
                                       (throw exception))))]
                     (try
                       (entrypoint/run! command
                                        {:resolve-operation (fn [_] (throw exception))
                                         :print-diagnostic! (fn [_] (throw (AssertionError. "unexpected printer")))
                                         :exit! #(swap! statuses conj %)})
                       (catch Throwable error
                         ;; Capture at the boundary, before test reporting/STM
                         ;; may consume an interrupt while recording assertions.
                         (reset! interrupted-on-escape? (.isInterrupted (Thread/currentThread)))
                         (reset! escaped error))))))]
    {:exits @statuses :stderr (str stderr) :stdout stdout :escaped @escaped
     :interrupted-on-escape? @interrupted-on-escape?}))

(deftest ordinary-constructor-and-resolver-failures-have-redacted-cli-exit-four
  (doseq [stage [:constructor :resolver]
          exception [(java.io.IOException. "private contents /private/path"
                                           (IllegalArgumentException. "private cause"))
                     (IllegalStateException. "private state /private/path")
                     (java.net.SocketTimeoutException. "private timeout /private/path")]]
    ;; The host API still preserves the SAME error; only CLI presentation changes.
    (with-redefs [clojure.core/requiring-resolve
                  (fn [_] (if (= :resolver stage) (throw exception)
                            (fn [& _] (throw exception))))]
      (is (identical? exception
                     (try (invocation/run-declared-input-build! arguments)
                          (catch Exception error error)))))
    (let [{:keys [exits stderr stdout escaped]} (failing-cli-model exception stage arguments)
          report (json/read-strict-json stderr)]
      (is (= [4] exits))
      (is (nil? escaped))
      (is (empty? stdout))
      (is (= "T1004" (get report "code")))
      (is (= 4 (get report "exitCode")))
      (is (= "rejected" (get report "status")))
      (is (not (.contains stderr "private")))
      (is (not (.contains stderr "Exception")))))
  (let [exception (java.io.IOException. "ordinary non-build failure")
        result (failing-cli-model exception :constructor ["read" "unused.gravity"])]
    (is (identical? exception (:escaped result)))
    (is (empty? (:exits result)))
    (is (empty? (:stderr result)))))

(deftest build-control-flow-and-interrupts-are-never-converted-to-json
  (doseq [stage [:constructor :resolver]
          exception [(AssertionError. "injected Error")
                     (IllegalStateException. "wrapped Error" (LinkageError. "injected link"))
                     (java.util.concurrent.CancellationException. "cancel")
                     (java.io.InterruptedIOException. "interrupted I/O")
                     (java.nio.channels.ClosedByInterruptException.)]]
    (let [result (failing-cli-model exception stage arguments)]
      (is (identical? exception (:escaped result)))
      (is (empty? (:exits result)))
      (is (empty? (:stdout result)))
      (is (empty? (:stderr result)))))
  (doseq [exception [(InterruptedException. "injected interrupt")
                     (IllegalStateException. "wrapped interrupt" (InterruptedException.))]]
    (let [was-interrupted? (.isInterrupted (Thread/currentThread))]
      (try
        (let [result (failing-cli-model exception :constructor arguments)]
          (is (identical? exception (:escaped result)))
          (is (:interrupted-on-escape? result))
          (is (empty? (:exits result)))
          (is (empty? (:stderr result))))
        ;; Restore only the flag this injected model set; production never clears it.
        (finally (when-not was-interrupted? (Thread/interrupted))))))
  (let [was-interrupted? (.isInterrupted (Thread/currentThread))
        exception (java.io.IOException. "failure while already interrupted")]
    (try
      (let [result (failing-cli-model exception :constructor-interrupted arguments)]
        (is (identical? exception (:escaped result)))
        (is (:interrupted-on-escape? result))
        (is (empty? (:exits result)))
        (is (empty? (:stderr result))))
      (finally (when-not was-interrupted? (Thread/interrupted))))))
