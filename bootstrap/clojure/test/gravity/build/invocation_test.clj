(ns gravity.build.invocation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.build.invocation :as invocation]
            [gravity.build.declared-inputs :as inputs]
            [gravity.cli.dispatch :as dispatch]
            [gravity.cli.entrypoint :as entrypoint]
            [gravity.tooling.strict-json :as json]))

(def hash-id (str "sha256:" (apply str (repeat 64 "a"))))
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

(deftest real-command-route-is-fixed-and-missing-bridge-is-truthful
  ;; Injected command-boundary models only; not a CLI process or Reader run.
  (let [called (atom [])]
    (with-redefs [invocation/run-declared-input-build! #(swap! called conj %)]
      (is (true? (dispatch/dispatch! (fn [_] (throw (Error. "unexpected resolver"))) arguments))))
    (is (= [arguments] @called)))
  (let [statuses (atom []) stderr (java.io.StringWriter.)]
    (binding [*err* stderr]
      (entrypoint/run! arguments
                       {:resolve-operation (fn [_] (throw (Error. "no Bootstrap")))
                        :print-diagnostic! (fn [_] (throw (Error. "no private text")))
                        :exit! #(swap! statuses conj %)}))
    (is (= [4] @statuses))
    (let [report (json/read-strict-json (str stderr))]
      (is (= "reader-effectful-bridge-unavailable" (get report "reason")))
      (is (false? (get report "runtimeAccepted")))
      (is (not (.contains (str stderr) "private text")))))
  (let [escaped (atom nil)]
    (with-redefs-fn {#'invocation/reader-build!
                    (fn [cap _] (reset! escaped cap) (throw (ex-info "injected" {})))}
      #(is (map? (failure (fn [] (invocation/run-declared-input-build! arguments))))))
    (is (= :inactive-or-foreign-invocation
           (:reason (failure #(inputs/preflight! @escaped [])))))))
