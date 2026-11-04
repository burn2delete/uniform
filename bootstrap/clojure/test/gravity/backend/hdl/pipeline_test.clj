(ns gravity.backend.hdl.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.hdl :as hdl]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest override-precedence-preserves-borrowed-values
  (let [specific {:scope :b9} lowering {:scope :lowering} specialized {:scope :specialized}]
    (doseq [select [hdl/source-overrides bootstrap/b9-document-source-overrides]]
      (is (identical? specific (select {:metadata {:backend {:b9-document specific
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? lowering (select {:metadata {:backend {:b9-document false
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? specialized (select {:metadata {:backend {:specialized specialized}}})))
      (is (= {} (select {})))
      (let [empty-value {}]
        (is (identical? empty-value (select {:metadata {:backend {:b9-document empty-value
                                                                 :specialized specialized}}})))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b9-document-diagnostic-ids]
    (is (= (bootstrap/b9-document-missing-fact id) (hdl/missing-fact id)))
    (is (= (bootstrap/b9-document-signal-id id) (hdl/signal-id id))))
  (is (= :clk (hdl/signal-id "B9-CLOCK")))
  (is (= :cdc_handshake (hdl/signal-id "B9-CDC")))
  (doseq [id [nil :b9-clock "B9-UNKNOWN"]]
    (is (= :hdl-artifact-manifest (hdl/missing-fact id)))
    (is (= :gravity_stage0_hdl (hdl/signal-id id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b9-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b9_hdl_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-hdl-native-diagnostic-" ".edn")]
    (try
      (is (= "B9-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :hdl-artifact-manifest (:missing-proof-or-constraint data)))
      (is (= :gravity.backend/hdl (:backend data)))
      (is (= :hardware (:profile data)))
      (is (= :systemverilog (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [hdl/missing-fact (fn [id] (swap! calls conj id) :observed-stage-fact)]
      (let [error (actual-refusal "hdl-context.gravity")]
        (is (= "B9-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-fact (:missing-proof-or-constraint (ex-data error))))
        (is (= ["B9-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b9-document-missing-fact (constantly :observed-wrapper-fact)]
      (is (= :observed-wrapper-fact
             (:missing-proof-or-constraint (ex-data (actual-refusal "hdl-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "hdl-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b9-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b9-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b9-document-diagnostic-stream path input-id))))
    (is (every? #(= [:hardware :systemverilog] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (every? #(true? (get-in % [:facts :unmediated-cdc-rejected])) (:diagnostics stream)))))

(deftest native-catalog-assembly-uses-explicit-borrowed-operations
  (let [path "explicit-native-catalog.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        ids ["B9-CLOCK" "B9-MANIFEST"]
        spans [{:source path :provided-index 0} {:source path :provided-index 1}]
        span-calls (atom []) id-calls (atom 0)
        stream (hdl/diagnostic-stream
                path input-id
                {:source-span (fn [source ordinal]
                                (swap! span-calls conj [source ordinal])
                                (nth spans ordinal))
                 :diagnostic-ids (fn [] (swap! id-calls inc) ids)
                 :signal-id (fn [id] [:provided-context id])
                 :missing-fact (fn [id] [:provided-policy id])})
        rows (:diagnostics stream)]
    (is (identical? input-id (:input-artifact stream)))
    (is (= ids (mapv :rule rows)))
    (is (= [[path 0] [path 1]] @span-calls))
    (is (= 1 @id-calls))
    (is (every? true? (map (fn [span row] (identical? span (get-in row [:primary :span]))) spans rows)))
    (is (= (mapv (fn [id] [:provided-context id]) ids) (mapv :signal-or-state-id rows)))
    (is (= (mapv (fn [id] [:provided-policy id]) ids) (mapv :missing-proof-or-constraint rows)))))

(deftest compatibility-catalog-wrapper-retains-late-native-operations
  (let [captured (atom nil)
        path "late-native-catalog.gravity" input-id "supplied-native-input"]
    (with-redefs [hdl/diagnostic-stream (fn [source input operations]
                                        (reset! captured [source input operations])
                                        :observed-stage-catalog)]
      (is (= :observed-stage-catalog
             (bootstrap/b9-document-diagnostic-stream path input-id))))
    (let [[source input operations] @captured]
      (is (= [path input-id] [source input]))
      (is (= #{:source-span :diagnostic-ids :signal-id :missing-fact} (set (keys operations))))
      (with-redefs [bootstrap/source-span (fn [source ordinal] [:late-span source ordinal])
                    bootstrap/b9-document-diagnostic-ids ["LATE-NATIVE-ID"]
                    bootstrap/b9-document-signal-id (constantly :late-context)
                    bootstrap/b9-document-missing-fact (constantly :late-policy)]
        (is (= [:late-span path 3] ((:source-span operations) path 3)))
        (is (= ["LATE-NATIVE-ID"] ((:diagnostic-ids operations))))
        (is (= :late-context ((:signal-id operations) "LATE-NATIVE-ID")))
        (is (= :late-policy ((:missing-fact operations) "LATE-NATIVE-ID")))))))

(deftest native-catalog-operation-failure-is-not-replaced-with-data
  (let [error (ex-info "native source span failed" {:operation :source-span})
        operations {:source-span (fn [_ _] (throw error))
                    :diagnostic-ids (fn [] ["B9-CLOCK"])
                    :signal-id (constantly :native-context)
                    :missing-fact (constantly :native-policy)}
        capture (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo actual actual)))]
    (is (identical? error (capture #(hdl/diagnostic-stream "failure.gravity" "native-input" operations))))
    (with-redefs [bootstrap/source-span (fn [_ _] (throw error))]
      (is (identical? error (capture #(bootstrap/b9-document-diagnostic-stream "failure.gravity" "native-input")))))))

(deftest native-fixture-recognition-retains-reference-text-and-late-wrapper-calls
  (doseq [[stage-var bootstrap-var text required]
          [[#'hdl/hdl-structurally-valid? #'bootstrap/b9-document-hdl-structurally-valid? bootstrap/b9-document-hdl-module "module gravity_stage0_hdl"]
           [#'hdl/testbench-structurally-valid? #'bootstrap/b9-document-testbench-structurally-valid? bootstrap/b9-document-testbench "gravity_stage0_hdl dut"]
           [#'hdl/timing-structurally-valid? #'bootstrap/b9-document-timing-structurally-valid? bootstrap/b9-document-timing-constraints "create_clock"]]]
    (let [missing (str/replace text required "")]
      (is (not= text missing))
      (doseq [recognize [(var-get stage-var) (var-get bootstrap-var)]]
        (is (true? (recognize text)))
        (is (false? (recognize "")))
        (is (false? (recognize missing))))
      (with-redefs-fn
        {stage-var (fn [actual]
                     (is (identical? text actual))
                     :observed-native-fixture-match)}
        (fn []
          (is (= :observed-native-fixture-match ((var-get bootstrap-var) text))))))))

(deftest fixed-native-fixture-text-preserves-bytes-and-compatibility-identity
  (doseq [[stage compatibility expected-sha256]
        [[#'hdl/hdl-module #'bootstrap/b9-document-hdl-module "bddf15e7fde20828f1a615c9f1fb78c50cecbbd2727a3d27a9f977d2d4858fa5"]
         [#'hdl/testbench #'bootstrap/b9-document-testbench "9c4c046a82ca2f154c8d373d102cc30194e5b28199c3cbdf05c2172d6c397b1f"]
         [#'hdl/timing-constraints #'bootstrap/b9-document-timing-constraints "a580b6b5b757d1d50e97c3e8049eaef9855342c7d718630866b4ce22cc527406"]]]
    (let [text (var-get stage)
          file (java.io.File/createTempFile "gravity-native-reference-" ".txt")]
      (try
        (is (string? text))
        (is (identical? text (var-get compatibility)))
        (is (= expected-sha256 (digest/sha256-hex text)))
        (spit file text :encoding "UTF-8")
        (is (= text (slurp file :encoding "UTF-8")))
        (is (= expected-sha256
               (digest/sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath file)))))
        (finally (.delete file))))))

(deftest native-failure-assembly-keeps-operation-order-and-extra-precedence
  (let [calls (atom []) payload (atom nil) result (Object.)
        operations {:source-span (fn [path index] (swap! calls conj [:span path index]) :default-span)
                    :signal-id (fn [id] (swap! calls conj [:first id]) :first-context)
                    :missing-fact (fn [id] (swap! calls conj [:last id]) :last-context)
                    :fail! (fn [id message data]
                             (swap! calls conj [:fail id]) (reset! payload [id message data]) result)}]
    (is (identical? result (hdl/fail! "NATIVE" "borrowed.gravity" {}
                                        {:source-span :extra-span :missing-policy :extra-policy} operations)))
    (is (= [[:first "NATIVE"] [:span "borrowed.gravity" 0] [:last "NATIVE"] [:fail "NATIVE"]] @calls))
    (is (= "NATIVE" (first @payload)))
    (is (= :extra-span (get-in @payload [2 :source-span])))
    (is (= :extra-policy (get-in @payload [2 :missing-policy])))
    (let [span (Object.) marker (ex-info "operation identity" {})]
      (is (identical? result (hdl/fail! "NATIVE" "borrowed.gravity" {:source-span span} {}
                                          (assoc operations :source-span (fn [& _] (throw marker))))))
      (is (identical? span (get-in @payload [2 :source-span])))
      (is (identical? marker
                     (try (hdl/fail! "NATIVE" "borrowed.gravity" {} {}
                                        (assoc operations :fail! (fn [& _] (throw marker))))
                          (catch clojure.lang.ExceptionInfo error error)))))))

(deftest native-failure-wrapper-captures-throw-operation-before-payload-evaluation
  (let [original-result (Object.) substituted-result (Object.)]
    (with-redefs [bootstrap/fail! (fn [& _] original-result)
                  bootstrap/b9-document-missing-fact
                  (fn [_]
                    (alter-var-root #'bootstrap/fail! (constantly (fn [& _] substituted-result)))
                    :observed-context)]
      (is (identical? original-result
                      (bootstrap/b9-document-fail! "NATIVE" "borrowed.gravity" {} {}))))))

(deftest native-override-selection-is-lazy-and-preserves-callback-results
  (let [marker (ex-info "unused operation" {})]
    (doseq [input [nil {} {:fail nil} {:fail false}]]
      (is (nil? (hdl/validate-source-overrides!
                  "borrowed.gravity" input
                  {:override-diagnostics (fn [] (throw marker)) :failure-operation (fn [] (throw marker))})))))
  (let [calls (atom []) result (Object.) id (String. "borrowed-native-id")
        operations {:override-diagnostics (fn [] (swap! calls conj :table) {:custom id})
                    :failure-operation
                    (fn []
                      (swap! calls conj :failure-lookup)
                      (fn [actual path subject extra]
                        (is (identical? id actual))
                        (swap! calls conj [path subject extra]) result))}]
    (is (identical? result (hdl/validate-source-overrides! "borrowed.gravity" {:fail :custom} operations)))
    (is (= [:table :failure-lookup ["borrowed.gravity"
                    {:artifact-id "b9-document-custom" :signal-or-state-id :b9-document-custom}
                    {:missing-fields [:custom]}]] @calls))
    (reset! calls [])
    (is (nil? (hdl/validate-source-overrides! "borrowed.gravity" {:fail :unknown} operations)))
    (is (= [:table] @calls))))

(deftest native-override-wrapper-keeps-table-and-failure-var-lookup-late
  (let [captured (atom nil) path (String. "borrowed.gravity") input {:fail :custom}]
    (with-redefs [hdl/validate-source-overrides!
                  (fn [source overrides operations]
                    (is (identical? path source)) (is (identical? input overrides))
                    (reset! captured operations) :stage-result)]
      (is (= :stage-result (bootstrap/b9-document-validate-source-overrides! path input))))
    (let [table {:custom "NATIVE"} result (Object.) subject {} extra {}]
      (with-redefs [bootstrap/b9-document-override-diagnostics table
                    bootstrap/b9-document-fail!
                    (fn [id source actual-subject actual-extra]
                      (is (= "NATIVE" id)) (is (identical? path source))
                      (is (identical? subject actual-subject)) (is (identical? extra actual-extra)) result)]
        (is (identical? table ((:override-diagnostics @captured))))
        (is (identical? result (((:failure-operation @captured)) "NATIVE" path subject extra)))))))

(deftest native-override-wrapper-captures-failure-root-before-selector-name
  (let [original-result (Object.) substituted-result (Object.)
        selector (reify clojure.lang.Named
                   (getName [_]
                     (alter-var-root #'bootstrap/b9-document-fail!
                                     (constantly (fn [& _] substituted-result)))
                     "custom")
                   (getNamespace [_] nil))]
    (with-redefs [bootstrap/b9-document-override-diagnostics {selector "NATIVE"}
                  bootstrap/b9-document-fail! (fn [& _] original-result)]
      (is (identical? original-result
                      (bootstrap/b9-document-validate-source-overrides!
                        "borrowed.gravity" {:fail selector}))))))
