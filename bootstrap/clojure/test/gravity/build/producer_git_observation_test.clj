(ns gravity.build.producer-git-observation-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.build.producer-git-observation :as git]
            [gravity.self-hosting.sh01-component-test-dependencies :as dependencies]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting-test-runner :as child-runner])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.nio.charset StandardCharsets]))

(defn- wiring-var [name]
  (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring name))

(def ^:private temporary-root (.getCanonicalPath (java.io.File. "/tmp")))

(defn- fake-process
  ([bytes exit events] (fake-process bytes exit events {}))
  ([bytes exit events {:keys [read-error stdin-error]}]
  (let [stdin (proxy [ByteArrayOutputStream] []
                (close [] (swap! events conj [:stdin (.toByteArray this)])
                  (when stdin-error (throw stdin-error))))
        stdout (if read-error
                 (proxy [java.io.InputStream] []
                   (read ([] (throw read-error)) ([_ _ _] (throw read-error))))
                 (ByteArrayInputStream. bytes))]
    (proxy [Process] []
      (getOutputStream [] stdin)
      (getInputStream [] stdout)
      (getErrorStream [] (throw (AssertionError. "stderr must remain merged")))
      (waitFor [] (swap! events conj :wait)
        (if (instance? Throwable exit) (throw exit) exit))
      (exitValue [] exit)
      (destroy [] (throw (AssertionError. "no guessed root cleanup")))))))

;; Actual run-git-bytes! body from 40ecbb97, replacing only ProcessBuilder
;; construction with the supplied fake Process. Keep its byte/merged-stream,
;; daemon-writer, join, root wait and diagnostic protocol as the oracle.
(defn- original-form [process arguments input-bytes]
  (let [command (into ["git"] arguments)
        writer-error (atom nil)
        writer (doto (Thread. ^Runnable
                              (fn []
                                (try
                                  (with-open [input (.getOutputStream ^Process process)]
                                    (when input-bytes (.write input ^bytes input-bytes)))
                                  (catch Throwable error (reset! writer-error error)))))
                 (.setDaemon true) (.start))
        output (ByteArrayOutputStream.)]
    (with-open [input (.getInputStream ^Process process)] (.transferTo input output))
    (.join ^Thread writer)
    (let [exit-code (.waitFor ^Process process) bytes (.toByteArray output)
          fail! (fn [message details]
                  (throw (ex-info message (assoc details :id "SH01-DEVELOPMENT-LOOP-GIT"
                                                 :authority :non-authoritative :authoritative? false))))]
      (when-not (zero? exit-code)
        (fail! "Development-loop repository identity command failed"
               {:command command :exit-code exit-code
                :output (String. bytes StandardCharsets/UTF_8)}))
      (when @writer-error
        (fail! "Development-loop repository identity input failed"
               {:command command :exit-code exit-code}))
      bytes)))

(defn- outcome [operation]
  (try {:bytes (vec (operation))}
       (catch Throwable error {:error error :data (ex-data error) :message (.getMessage error)
                               :interrupted? (.isInterrupted (Thread/currentThread))})))

(defn- ports [token events overrides]
  (merge {:retain (fn [process] (swap! events conj [:retain process]) token)
          :handles (fn [owned] (is (identical? token owned)) [:raw-handle])
          :finalize (fn [owned] (is (identical? token owned))
                      (swap! events conj :finalize)
                      {:root-alive? false :tracked-complete? true :complete? nil
                       :descendant-coverage :unknown})
          :verify (fn [_] (swap! events conj :verify))} overrides))

(defn- bound-run [process lifecycle arguments input-bytes]
  (with-redefs-fn {(wiring-var 'start-git-process) (fn [_ _] process)}
    #(with-bindings {(wiring-var '*producer-git-lifecycle*) lifecycle}
       ((var-get (wiring-var 'run-git-bytes!)) "/tmp" arguments input-bytes))))

(deftest actual-git-protocol-is-preserved-with-owned-token
  (doseq [bytes [(byte-array []) (byte-array [(unchecked-byte 0xff) 0 65])
                (byte-array (repeat (+ (* 1024 1024) 7) (byte 65)))]
          exit [0 7]
          input [nil (.getBytes "input\u0000bytes" "UTF-8")]]
    (let [events (atom []) token (Object.) records (atom [])
          process (fake-process bytes exit events)
          observed (outcome #(bound-run process
                                        (git/lifecycle {:root temporary-root} records
                                                       (ports token events {}))
                                        ["status" "--porcelain"] input))
          original-events (atom [])
          original (outcome #(original-form (fake-process bytes exit original-events)
                                             ["status" "--porcelain"] input))]
      (is (true? (= (dissoc original :error) (dissoc observed :error))))
      (is (= (vec (or input (byte-array []))) (vec (second (first (filter vector? @original-events))))))
      (is (= (vec (or input (byte-array [])))
             (vec (second (first (filter #(and (vector? %) (= :stdin (first %))) @events))))))
      (is (= [:verify :retain :stdin :wait :finalize :verify]
             (mapv #(if (vector? %) (first %) %) @events)))
      (is (identical? process (:process @(first @records))))
      (is (identical? token (:owned-process @(first @records))))
      (is (= true (:root-wait-exited? @(first @records))))
      (is (nil? (get-in @(first @records) [:finalization :complete?])))
      (is (= :unknown (:descendant-coverage @(first @records)))))))

(deftest launch-retain-wait-and-cleanup-errors-remain-explicit
  (let [token (Object.) root {:root temporary-root}]
    (doseq [stage [:before :launch :retain :wait :finalize :identity]
            make-error [#(ex-info "injected" {}) #(InterruptedException. "injected")]]
      (let [error (make-error) events (atom []) records (atom [])
            process (fake-process (byte-array []) (if (= stage :wait) error 0) events)
            p (ports token events
                     (case stage
                       :before {:verify (fn [_] (throw error))}
                       :retain {:retain (fn [_] (throw error))}
                       :finalize {:finalize (fn [_] (throw error))}
                       :identity {:verify (let [calls (atom 0)]
                                            (fn [_] (when (= 2 (swap! calls inc)) (throw error))))}
                       {}))]
        (try
          (let [result (with-redefs-fn
                         {(wiring-var 'start-git-process)
                          (fn [& _] (if (= stage :launch) (throw error) process))}
                         (fn []
                           (with-bindings {(wiring-var '*producer-git-lifecycle*) (git/lifecycle root records p)}
                             (outcome #((var-get (wiring-var 'run-git-bytes!)) "/tmp" ["status"])))))]
            (is (identical? error (:error result)))
            (is (= (not (#{:before :launch} stage)) (boolean (seq @records))))
            (when (= stage :retain)
              (is (identical? process (:process @(first @records))))
              (is (= :unproven (:terminal @(first @records)))))
            (when (and (instance? InterruptedException error) (not= stage :launch))
              (is (:interrupted? result) (str stage))))
          (finally (Thread/interrupted)))))))

(deftest cleanup-does-not-hide-original-interruption-or-fatal-errors
  (doseq [exit [0 9]
          cleanup [(InterruptedException. "cleanup") (LinkageError. "cleanup fatal")]]
    (let [events (atom []) records (atom []) token (Object.)
          p (ports token events {:finalize (fn [_] (throw cleanup))})
          result (try
                   (outcome #(bound-run (fake-process (byte-array []) exit events)
                                        (git/lifecycle {:root temporary-root} records p) ["status"] nil))
                   (finally (when (instance? InterruptedException cleanup)
                              (is (.isInterrupted (Thread/currentThread))))
                            (Thread/interrupted)))]
      (if (or (zero? exit) (instance? LinkageError cleanup))
        (is (identical? cleanup (:error result)))
        (do (is (= "SH01-DEVELOPMENT-LOOP-GIT" (get-in result [:data :id])))
            (is (some #(identical? cleanup %) (.getSuppressed ^Throwable (:error result)))))))))

(deftest io-failures-and-original-errors-survive-finalization
  (doseq [port [:read-error :stdin-error]]
    (let [error (java.io.IOException. "injected stream") events (atom [])
          original (outcome #(original-form (fake-process (byte-array []) 0 (atom []) {port error})
                                             ["status"] nil))
          observed (outcome #(bound-run (fake-process (byte-array []) 0 events {port error})
                                        (git/lifecycle {:root temporary-root} (atom [])
                                                       (ports (Object.) events {})) ["status"] nil))]
      (is (= (dissoc original :error) (dissoc observed :error)))
      (when (= port :read-error) (is (identical? error (:error observed))))
      (is (= 1 (count (filter #{:finalize} @events))))))
  (let [original (InterruptedException. "root wait") cleanup (java.io.IOException. "cleanup")
        events (atom []) records (atom [])
        result (try
                 (outcome #(bound-run (fake-process (byte-array []) original events)
                                      (git/lifecycle {:root temporary-root} records
                                                     (ports (Object.) events {:finalize (fn [_] (throw cleanup))}))
                                      ["status"] nil))
                 (finally (Thread/interrupted)))]
    (is (identical? original (:error result)))
    (is (:interrupted? result))
    (is (some #(identical? cleanup %) (.getSuppressed original)))))

(deftest selected-bookend-scope-restores-without-instrumenting-tests
  (let [observer (wiring-var 'observe-fresh-producer-binding!)
        calls (atom []) marker {:injected true} returned (Object.)]
    (doseq [throw? [false true]]
      (reset! calls [])
      (with-redefs-fn
        {observer (fn [& _]
                    (swap! calls conj (var-get (wiring-var '*producer-git-lifecycle*)))
                    {:binding {:source {}} :repository-snapshot {}})}
        #(try
           ((var-get (wiring-var 'observe-fresh-producer!))
            ['gravity.c2-artifact-identity-test] {:producer-git-lifecycle marker}
            (fn [] (is (nil? (var-get (wiring-var '*producer-git-lifecycle*))))
              (if throw? (throw (ex-info "operation" {})) {:returned returned})))
           (catch clojure.lang.ExceptionInfo _ nil)))
      (is (= (vec (repeat (if throw? 1 2) marker)) @calls))
      (is (nil? (var-get (wiring-var '*producer-git-lifecycle*)))))))

(deftest frozen-git-input-drift-refuses-before-launch
  ;; Only a disposable file and injected PATH selection are used; no Git runs.
  (let [file (java.io.File/createTempFile "rv2-git-input" ".txt")
        path (.getCanonicalPath file) root (.getCanonicalPath (java.io.File. "."))
        path-var (ns-resolve 'gravity.build.producer-git-observation 'path-git)]
    (try
      (spit file "first")
      (with-redefs-fn {path-var (constantly path)}
        #(let [binding (git/freeze-binding! root {:executable path :dispatch-files []
                                                 :configuration-files [path] :selection-links []})]
           (is (nil? (git/verify-binding! binding)))
           (is (= :unknown (:native-helper-and-config-search-coverage binding)))
           (spit file "changed")
           (is (= :identity-drift
                  (try (git/verify-binding! binding) nil
                       (catch clojure.lang.ExceptionInfo error (:field (ex-data error))))))))
      (finally (.delete file)))))

(deftest git-module-is-bound-only-in-the-selected-closure
  (let [path "bootstrap/clojure/test/gravity/build/producer_git_observation.clj"
        index (dependencies/dependency-index)
        closure (dependencies/reviewed-cache-closure index 'gravity.c2-artifact-identity-test)
        contract (dependencies/read-contract)
        changed (update-in contract [:components 0 :tests 0 :cache-closure :inputs]
                           conj {:path path :role :runner})]
    (is (some #{path} (map :path (:inputs closure))))
    (is (false? (get-in (dependencies/dependency-index changed)
                        [:by-test-path "bootstrap/clojure/test/gravity/c11_mir_test.clj"
                         :cache-closure-valid?])))
    (is (true? (get-in index [:by-test-path "bootstrap/clojure/test/gravity/c2_artifact_identity_test.clj"
                             :test-policy :freshness-required?])))))

(deftest supported-entrypoint-requires-the-exact-opt-in-selection
  (let [base {:cache-directory "/tmp/unused-rv2-cli-control" :timeout-ms 1000
              :repository-identity (str "sha256:" (apply str (repeat 64 "a")))}
        options (assoc base :observe-producer? true
                            :producer-git-binding {:schema :gravity/rv2-git-binding-v1
                                                   :root "/tmp/unused-rv2-cli-control"})
        select (fn [options namespaces]
                 (child-runner/select-tests
                  (into ["--development-cache" (pr-str options)]
                        (mapcat #(vector "--namespace" (str %)) namespaces))))]
    (is (child-runner/valid-development-cache-options? base))
    (is (= options (:development-cache-options
                    (select options ['gravity.c2-artifact-identity-test]))))
    (doseq [invalid [(dissoc options :observe-producer?)
                     (assoc options :observe-producer? false)
                     (assoc options :producer-git-binding nil)
                     (assoc options :producer-git-binding {:schema :unknown})
                     (assoc options :producer-git-binding {:schema :gravity/rv2-git-binding-v1})
                     (assoc-in options [:producer-git-binding :root] nil)
                     (assoc-in options [:producer-git-binding :root] 42)
                     (assoc-in options [:producer-git-binding :root] "relative")
                     (assoc-in options [:producer-git-binding :root] "\u0000invalid")
                     (assoc options :producer-git-lifecycle {})]]
      (is (false? (child-runner/valid-development-cache-options? invalid)))
      (is (= "SH01-TEST-USAGE"
             (try (select invalid ['gravity.c2-artifact-identity-test]) nil
                  (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))))
    (doseq [namespaces [['gravity.c11-mir-test]
                        ['gravity.c2-artifact-identity-test 'gravity.c11-mir-test]]]
      (is (= "SH01-TEST-USAGE"
             (try (select options namespaces) nil
                  (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))))))

(deftest direct-source-entrypoint-refuses-malformed-roots-before-dispatch
  (doseq [binding [nil {} {:root nil} {:root 42} {:root "relative"} {:root "\u0000invalid"}]]
    (with-redefs [wiring/run-child-unit! (fn [& _] (throw (AssertionError. "unexpected dispatch")))]
      (is (= {:id "RV2-GIT-OBSERVATION" :field :binding-shape}
             (try (git/run-selected! binding (atom []) {} (fn [] :unused)) nil
                  (catch clojure.lang.ExceptionInfo error (ex-data error))))))))

(deftest actual-main-dispatch-preserves-default-and-git-routes
  (let [dispatch (ns-resolve 'gravity.self-hosting-test-runner 'run-development-child-unit!)
        calls (atom []) operation (fn [] :operation)
        binding {:schema :gravity/rv2-git-binding-v1}]
    (with-redefs [clojure.core/requiring-resolve
                  (fn [symbol] (fn [& args] (swap! calls conj [symbol args]) :report))]
      (is (= :report (dispatch ['gravity.c2-artifact-identity-test] {} operation)))
      (is (= 'gravity.self-hosting.sh01-development-loop-wiring/run-child-unit!
             (ffirst @calls)))
      (is (identical? operation (last (second (first @calls)))))
      (is (= :report (dispatch ['gravity.c2-artifact-identity-test]
                              {:producer-git-binding binding} operation)))
      (let [[symbol args] (last @calls)]
        (is (= 'gravity.build.producer-git-observation/run-selected! symbol))
        (is (identical? binding (first args)))
        (is (instance? clojure.lang.Atom (second args)))
        (is (empty? @(second args)))
        (is (identical? operation (last args)))))))

(deftest child-report-retains-git-facts-without-raw-java-state
  (let [binding {:root (.getCanonicalPath (java.io.File. "."))
                 :schema :gravity/rv2-git-binding-v1}
        records (atom []) raw-process (Object.) token (Object.)
        record {:process raw-process :owned-process token :raw-handles [(Object.)]
                :argv ["git" "status"] :cwd (:root binding) :exit-code 0
                :root-wait-exited? true :descendant-coverage :unknown
                :finalization {:root-alive? false :tracked-complete? true
                               :complete? nil :descendant-coverage :unknown}}
        summary {:schema :synthetic-report :child-cache-hit? false}]
    (with-redefs [wiring/run-child-unit!
                  (fn [namespaces options _]
                    (is (= ['gravity.c2-artifact-identity-test] namespaces))
                    (is (true? (:observe-producer? options)))
                    (is (identical? binding (:producer-git-binding options)))
                    (is (map? (:producer-git-lifecycle options)))
                    (swap! records conj (atom record)) summary)]
      (let [report (git/run-selected! binding records {} (fn [] :unused))
            observation (:test-producer-git-observation report)
            readback (edn/read-string (pr-str report))]
        (is (= report readback))
        (is (= summary (dissoc report :test-producer-git-observation)))
        (is (= binding (:binding observation)))
        (is (= (dissoc record :process :owned-process :raw-handles)
               (first (:processes observation))))
        (is (false? (:capture-v1-ready? observation)))
        (is (false? (:cache-admission? observation)))
        (is (identical? token (:owned-process @(first @records))))))))
