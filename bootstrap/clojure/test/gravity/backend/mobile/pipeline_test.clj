(ns gravity.backend.mobile.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.mobile :as mobile]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest native-override-projection-preserves-borrowed-values
  (doseq [select [mobile/source-overrides bootstrap/b12-document-source-overrides]]
    (is (= {} (select {})))
    (is (= {} (select {:metadata {:backend {:specialized {:ignored true}
                                          :b12-document {:ignored true}}}})))
    (doseq [value [{:scope :lowering} {} false nil]]
      (is (identical? value (select {:metadata {:backend {:specialized-lowering value
                                                        :specialized {:ignored true}
                                                        :b12-document {:ignored true}}}}))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b12-document-diagnostic-ids]
    (is (= (bootstrap/b12-document-missing-policy id) (mobile/missing-policy id)))
    (is (= (bootstrap/b12-document-domain-anchor id) (mobile/domain-anchor id))))
  (is (= :platform-api-binding (mobile/domain-anchor "B12-PERMISSION")))
  (is (= :ui-thread-update (mobile/domain-anchor "B12-THREAD")))
  (doseq [id [nil :b12-schema "B12-UNKNOWN"]]
    (is (= :mobile-artifact-manifest (mobile/missing-policy id)))
    (is (= :mobile-manifest (mobile/domain-anchor id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b12-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b12_mobile_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-mobile-native-diagnostic-" ".edn")]
    (try
      (is (= "B12-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :mobile-artifact-manifest (:missing-policy data)))
      (is (= :gravity.backend/mobile (:backend data)))
      (is (= :hosted (:profile data)))
      (is (= :ios (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [mobile/missing-policy (fn [id] (swap! calls conj id) :observed-stage-policy)]
      (let [error (actual-refusal "mobile-context.gravity")]
        (is (= "B12-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-policy (:missing-policy (ex-data error))))
        (is (= ["B12-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b12-document-missing-policy (constantly :observed-wrapper-policy)]
      (is (= :observed-wrapper-policy
             (:missing-policy (ex-data (actual-refusal "mobile-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "mobile-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b12-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b12-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b12-document-diagnostic-stream path input-id))))
    (is (every? #(= [:hosted :ios] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (= [:network :network/request]
           ((juxt :permission :capability)
            (first (filter #(= "B12-PERMISSION" (:rule %)) (:diagnostics stream))))))))

(deftest native-catalog-assembly-uses-explicit-borrowed-operations
  (let [path "explicit-native-catalog.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        ids ["B12-THREAD" "B12-MANIFEST"]
        spans [{:source path :provided-index 0} {:source path :provided-index 1}]
        span-calls (atom []) id-calls (atom 0)
        stream (mobile/diagnostic-stream
                path input-id
                {:source-span (fn [source ordinal]
                                (swap! span-calls conj [source ordinal])
                                (nth spans ordinal))
                 :diagnostic-ids (fn [] (swap! id-calls inc) ids)
                 :domain-anchor (fn [id] [:provided-context id])
                 :missing-policy (fn [id] [:provided-policy id])})
        rows (:diagnostics stream)]
    (is (identical? input-id (:input-artifact stream)))
    (is (= ids (mapv :rule rows)))
    (is (= [[path 0] [path 1]] @span-calls))
    (is (= 1 @id-calls))
    (is (every? true? (map (fn [span row] (identical? span (get-in row [:primary :span]))) spans rows)))
    (is (= (mapv (fn [id] [:provided-context id]) ids) (mapv :domain-anchor rows)))
    (is (= (mapv (fn [id] [:provided-policy id]) ids) (mapv :missing-policy rows)))))

(deftest compatibility-catalog-wrapper-retains-late-native-operations
  (let [captured (atom nil)
        path "late-native-catalog.gravity" input-id "supplied-native-input"]
    (with-redefs [mobile/diagnostic-stream (fn [source input operations]
                                        (reset! captured [source input operations])
                                        :observed-stage-catalog)]
      (is (= :observed-stage-catalog
             (bootstrap/b12-document-diagnostic-stream path input-id))))
    (let [[source input operations] @captured]
      (is (= [path input-id] [source input]))
      (is (= #{:source-span :diagnostic-ids :domain-anchor :missing-policy} (set (keys operations))))
      (with-redefs [bootstrap/source-span (fn [source ordinal] [:late-span source ordinal])
                    bootstrap/b12-document-diagnostic-ids ["LATE-NATIVE-ID"]
                    bootstrap/b12-document-domain-anchor (constantly :late-context)
                    bootstrap/b12-document-missing-policy (constantly :late-policy)]
        (is (= [:late-span path 3] ((:source-span operations) path 3)))
        (is (= ["LATE-NATIVE-ID"] ((:diagnostic-ids operations))))
        (is (= :late-context ((:domain-anchor operations) "LATE-NATIVE-ID")))
        (is (= :late-policy ((:missing-policy operations) "LATE-NATIVE-ID")))))))

(deftest native-catalog-operation-failure-is-not-replaced-with-data
  (let [error (ex-info "native source span failed" {:operation :source-span})
        operations {:source-span (fn [_ _] (throw error))
                    :diagnostic-ids (fn [] ["B12-THREAD"])
                    :domain-anchor (constantly :native-context)
                    :missing-policy (constantly :native-policy)}
        capture (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo actual actual)))]
    (is (identical? error (capture #(mobile/diagnostic-stream "failure.gravity" "native-input" operations))))
    (with-redefs [bootstrap/source-span (fn [_ _] (throw error))]
      (is (identical? error (capture #(bootstrap/b12-document-diagnostic-stream "failure.gravity" "native-input")))))))

(deftest native-fixture-recognition-retains-reference-text-and-late-wrapper-calls
  (doseq [[stage-var bootstrap-var text required]
          [[#'mobile/bundle-structurally-valid? #'bootstrap/b12-document-bundle-structurally-valid? bootstrap/b12-document-app-bundle ":entrypoint :GravityStage0App"]
           [#'mobile/permission-structurally-valid? #'bootstrap/b12-document-permission-structurally-valid? bootstrap/b12-document-permission-manifest-fixture ":runtime-request :declared"]]]
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
        [[#'mobile/app-bundle #'bootstrap/b12-document-app-bundle "4124e12cbc98bdbbe7e4ec68e037770a38893fd3a2a9b5597758b260015c2a43"]
         [#'mobile/permission-manifest-fixture #'bootstrap/b12-document-permission-manifest-fixture "c6a3b098f102070ac5de06282b58c5cba99e6d991280809f8831cff884df8c35"]]]
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
                    :domain-anchor (fn [id] (swap! calls conj [:first id]) :first-context)
                    :missing-policy (fn [id] (swap! calls conj [:last id]) :last-context)
                    :fail! (fn [id message data]
                             (swap! calls conj [:fail id]) (reset! payload [id message data]) result)}]
    (is (identical? result (mobile/fail! "NATIVE" "borrowed.gravity" {}
                                        {:source-span :extra-span :missing-policy :extra-policy} operations)))
    (is (= [[:first "NATIVE"] [:span "borrowed.gravity" 0] [:last "NATIVE"] [:fail "NATIVE"]] @calls))
    (is (= "NATIVE" (first @payload)))
    (is (= :extra-span (get-in @payload [2 :source-span])))
    (is (= :extra-policy (get-in @payload [2 :missing-policy])))
    (let [span (Object.) marker (ex-info "operation identity" {})]
      (is (identical? result (mobile/fail! "NATIVE" "borrowed.gravity" {:source-span span} {}
                                          (assoc operations :source-span (fn [& _] (throw marker))))))
      (is (identical? span (get-in @payload [2 :source-span])))
      (is (identical? marker
                     (try (mobile/fail! "NATIVE" "borrowed.gravity" {} {}
                                        (assoc operations :fail! (fn [& _] (throw marker))))
                          (catch clojure.lang.ExceptionInfo error error)))))))

(deftest native-failure-wrapper-captures-throw-operation-before-payload-evaluation
  (let [original-result (Object.) substituted-result (Object.)]
    (with-redefs [bootstrap/fail! (fn [& _] original-result)
                  bootstrap/b12-document-missing-policy
                  (fn [_]
                    (alter-var-root #'bootstrap/fail! (constantly (fn [& _] substituted-result)))
                    :observed-context)]
      (is (identical? original-result
                      (bootstrap/b12-document-fail! "NATIVE" "borrowed.gravity" {} {}))))))

(deftest native-override-selection-is-lazy-and-preserves-callback-results
  (let [marker (ex-info "unused operation" {})]
    (doseq [input [nil {} {:fail nil} {:fail false}]]
      (is (nil? (mobile/validate-source-overrides!
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
    (is (identical? result (mobile/validate-source-overrides! "borrowed.gravity" {:fail :custom} operations)))
    (is (= [:table :failure-lookup ["borrowed.gravity"
                    {:artifact-id "b12-document-custom" :domain-anchor :b12-document-custom}
                    {:missing-fields [:custom]}]] @calls))
    (reset! calls [])
    (is (nil? (mobile/validate-source-overrides! "borrowed.gravity" {:fail :unknown} operations)))
    (is (= [:table] @calls))))

(deftest native-override-wrapper-keeps-table-and-failure-var-lookup-late
  (let [captured (atom nil) path (String. "borrowed.gravity") input {:fail :custom}]
    (with-redefs [mobile/validate-source-overrides!
                  (fn [source overrides operations]
                    (is (identical? path source)) (is (identical? input overrides))
                    (reset! captured operations) :stage-result)]
      (is (= :stage-result (bootstrap/b12-document-validate-source-overrides! path input))))
    (let [table {:custom "NATIVE"} result (Object.) subject {} extra {}]
      (with-redefs [bootstrap/b12-document-override-diagnostics table
                    bootstrap/b12-document-fail!
                    (fn [id source actual-subject actual-extra]
                      (is (= "NATIVE" id)) (is (identical? path source))
                      (is (identical? subject actual-subject)) (is (identical? extra actual-extra)) result)]
        (is (identical? table ((:override-diagnostics @captured))))
        (is (identical? result (((:failure-operation @captured)) "NATIVE" path subject extra)))))))

(deftest native-override-wrapper-captures-failure-root-before-selector-name
  (let [original-result (Object.) substituted-result (Object.)
        selector (reify clojure.lang.Named
                   (getName [_]
                     (alter-var-root #'bootstrap/b12-document-fail!
                                     (constantly (fn [& _] substituted-result)))
                     "custom")
                   (getNamespace [_] nil))]
    (with-redefs [bootstrap/b12-document-override-diagnostics {selector "NATIVE"}
                  bootstrap/b12-document-fail! (fn [& _] original-result)]
      (is (identical? original-result
                      (bootstrap/b12-document-validate-source-overrides!
                        "borrowed.gravity" {:fail selector}))))))
