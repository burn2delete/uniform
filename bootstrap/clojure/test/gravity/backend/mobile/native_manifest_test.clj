(ns gravity.backend.mobile.native-manifest-test
  "Cold mobile document data; no source admission or mobile execution."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.mobile :as mobile]
            [gravity.digest :as digest]))

(def ^:private diagnostic-values
  ["B12-TARGET" "B12-PERMISSION" "B12-LIFECYCLE" "B12-THREAD" "B12-NULL"
   "B12-ERROR" "B12-BACKGROUND" "B12-STORAGE" "B12-RESOURCE" "B12-MANIFEST"])
(defn- hash-text [text] (digest/sha256-hex text))
(defn- publish-refusal [id path subject extra]
  (throw (ex-info "Caller-owned unverified mobile document refusal"
                  (merge {:id id :source-path path :subject subject} extra))))
(defn- ports []
  {:artifact-id #'hash-text :app-bundle #'mobile/app-bundle
   :permission-manifest-fixture #'mobile/permission-manifest-fixture
   :bundle-structurally-valid? #'mobile/bundle-structurally-valid?
   :permission-structurally-valid? #'mobile/permission-structurally-valid?
   :diagnostic-ids #'diagnostic-values :fail! #'publish-refusal})
(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :specialized-lowering-artifact {:kind :gravity/stage0-specialized-lowering-artifact
                                   :capability-based-proof {:status :complete}}
   :mobile-backend-manifest (mobile/native-manifest (ports) "unverified-source.gravity" {:fixture :unverified-input})
   :b12-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (mobile/validate-native-manifest! (ports) "mobile-fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-two-fixed-texts-with-deterministic-hashes-and-borrowed-input
  (let [input {:fixture :unverified-input} path "unverified-mobile.gravity"
        manifest (mobile/native-manifest (ports) path input)]
    (is (= manifest (mobile/native-manifest (ports) path input)))
    (is (identical? input (:input-artifact manifest)))
    (is (identical? input (get-in manifest [:mobile-ir-handoff-record :source-artifact])))
    (is (identical? input (get-in manifest [:source-debug-map :source])))
    (is (identical? path (get-in manifest [:source-debug-map :source-path])))
    (is (= (mapv #(str path %) [":mobile" ":permissions" ":lifecycle" ":storage"])
           (get-in manifest [:source-debug-map :locations])))
    (is (= [(str path ":mobile") (str path ":mobile")]
           (mapv :source-location (get-in manifest [:permission-manifest :permissions]))))
    (is (= {:network (str path ":mobile:network") :ui (str path ":mobile:ui") :storage (str path ":mobile:storage")}
           (get-in manifest [:source-debug-map :platform-source-map])))
    (is (= [:mir :c11-mir :c12-mobile-domain-ir :c14-target-lowering :b1-interface :b12-mobile-backend]
           (get-in manifest [:source-debug-map :generated-origin-chain])))
    (is (= :style-manifest-only (get-in manifest [:alternate-platform-targets 0 :status])))
    (is (= :not-available-in-current-environment (get-in manifest [:external-mobile-validation-record :status])))
    (is (= :not-available-in-current-environment (get-in manifest [:device-simulator-conformance-report :device-execution])))
    (is (= :not-available-in-current-environment (get-in manifest [:device-simulator-conformance-report :simulator-execution])))
    (is (= (get-in manifest [:application-bundle-artifact :hash])
           (get-in manifest [:resource-asset-manifest :resources 0 :hash])))
    (doseq [[field text filename sha]
            [[:application-bundle-artifact mobile/app-bundle "GravityStage0.app/manifest.edn"
              "4124e12cbc98bdbbe7e4ec68e037770a38893fd3a2a9b5597758b260015c2a43"]
             [:permission-manifest mobile/permission-manifest-fixture "gravity_stage0_permissions.edn"
              "c6a3b098f102070ac5de06282b58c5cba99e6d991280809f8831cff884df8c35"]]]
      (let [record (get manifest field) file (io/file "target/validation/mobile-native-manifest" filename)]
        (is (= filename (:path record)))
        (is (= text (:content record)))
        (is (= sha (digest/sha256-hex text) (:hash record)))
        (io/make-parents file) (spit file text)
        (is (= text (slurp file)))))))

(deftest rejects-all-existing-record-conditions
  (let [data (unverified-data) m :mobile-backend-manifest]
    (is (= :complete (mobile/validate-native-manifest! (ports) "unverified.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :specialized-lowering-artifact) "B12-MANIFEST" :specialized-lowering-artifact]
             [(assoc-in data [:specialized-lowering-artifact :capability-based-proof :status] :forged) "B12-MANIFEST" :specialized-lowering-proof]
             [(assoc-in data [m :target :platform] :forged) "B12-TARGET" :target-platform]
             [(assoc-in data [m :mobile-ir-handoff-record :status] :missing) "B12-MANIFEST" :mobile-ir-handoff-record]
             [(assoc-in data [m :platform-target-record :status] :missing) "B12-TARGET" :platform-target-record]
             [(assoc-in data [m :application-bundle-artifact :status] :missing) "B12-RESOURCE" :application-bundle-artifact]
             [(assoc-in data [m :platform-binding-descriptors] []) "B12-MANIFEST" :platform-binding-descriptors]
             [(assoc-in data [m :permission-manifest :status] :missing) "B12-PERMISSION" :permission-manifest]
             [(assoc-in data [m :resource-asset-manifest :status] :missing) "B12-RESOURCE" :resource-asset-manifest]
             [(assoc-in data [m :lifecycle-threading-map :status] :missing) "B12-LIFECYCLE" :lifecycle-threading-map]
             [(assoc-in data [m :lifecycle-threading-map :ui-thread] :forged) "B12-THREAD" :main-thread-policy]
             [(assoc-in data [m :ui-bridge-metadata :status] :missing) "B12-TARGET" :ui-bridge-metadata]
             [(assoc-in data [m :null-error-callback-adapter-record :status] :missing) "B12-NULL" :null-error-callback-adapters]
             [(assoc-in data [m :null-error-callback-adapter-record :exceptions] :forged) "B12-ERROR" :platform-error-mapping]
             [(assoc-in data [m :local-storage-sync-schema-bundle :status] :missing) "B12-STORAGE" :local-storage-sync-schema]
             [(assoc-in data [m :background-task-policy :status] :missing) "B12-BACKGROUND" :background-task-policy]
             [(assoc-in data [m :background-task-policy :hidden-background-execution] :forged) "B12-BACKGROUND" :hidden-background-rejection]
             [(assoc-in data [m :store-audit-metadata :status] :missing) "B12-MANIFEST" :store-audit-metadata]
             [(assoc-in data [m :source-debug-map :status] :missing) "B12-MANIFEST" :source-debug-map]
             [(assoc-in data [m :device-simulator-conformance-report :status] :missing) "B12-MANIFEST" :device-simulator-conformance]
             [(update data m dissoc :external-mobile-validation-record) "B12-MANIFEST" :mobile-artifact-manifest]
             [(assoc-in data [:b12-diagnostic-stream :diagnostics] []) "B12-MANIFEST" :b12-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= "mobile-fixture.gravity" (:source-path result)))))))

(deftest missing-fixed-reference-tokens-reject
  (let [data (unverified-data)]
    (doseq [[operation text tokens id missing]
            [[#'mobile/app-bundle mobile/app-bundle
              ["org.gravity.stage0" ":platform :ios" ":architecture :arm64" ":entrypoint :GravityStage0App"]
              "B12-RESOURCE" :application-bundle-structure]
             [#'mobile/permission-manifest-fixture mobile/permission-manifest-fixture
              [":capability :network/request" ":runtime-request :declared" ":denial-policy :return-error" ":hidden-background-work :rejected"]
              "B12-PERMISSION" :permission-policy]]]
      (doseq [token tokens]
        (with-redefs-fn {operation (str/replace text token "removed")}
          (fn [] (let [result (refusal data)]
                   (is (= id (:id result)))
                   (is (= [missing] (:missing-fields result))))))))))

(deftest preserves-all-twenty-four-refusal-steps-and-subjects
  (let [calls (atom []) manifest {}
        supplied (assoc (ports) :app-bundle "invalid" :permission-manifest-fixture "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (mobile/validate-native-manifest! supplied "ordered.gravity" {:mobile-backend-manifest manifest})))
    (is (= ["B12-MANIFEST" "B12-MANIFEST" "B12-TARGET" "B12-MANIFEST" "B12-TARGET" "B12-RESOURCE"
            "B12-RESOURCE" "B12-MANIFEST" "B12-PERMISSION" "B12-PERMISSION" "B12-RESOURCE" "B12-LIFECYCLE"
            "B12-THREAD" "B12-TARGET" "B12-NULL" "B12-ERROR" "B12-STORAGE" "B12-BACKGROUND"
            "B12-BACKGROUND" "B12-MANIFEST" "B12-MANIFEST" "B12-MANIFEST" "B12-MANIFEST" "B12-MANIFEST"]
           (mapv first @calls)))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof] [:target-platform]
            [:mobile-ir-handoff-record] [:platform-target-record] [:application-bundle-artifact]
            [:application-bundle-structure] [:platform-binding-descriptors] [:permission-manifest] [:permission-policy]
            [:resource-asset-manifest] [:lifecycle-threading-map] [:main-thread-policy] [:ui-bridge-metadata]
            [:null-error-callback-adapters] [:platform-error-mapping] [:local-storage-sync-schema]
            [:background-task-policy] [:hidden-background-rejection] [:store-audit-metadata]
            [:source-debug-map] [:device-simulator-conformance] [:mobile-artifact-manifest] [:b12-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? manifest (nth % 2)) (take 21 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest first-failure-preserves-the-caller-exception-object
  (let [error (Error. "Caller-owned mobile failure") calls (atom [])]
    (is (identical? error
                    (try (mobile/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error))) "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B12-MANIFEST" "identity.gravity" nil {:missing-fields [:specialized-lowering-artifact]}]] @calls))))

(deftest failure-var-is-resolved-at-every-original-use
  (let [error (Error. "Late caller failure") calls (atom [])]
    (with-redefs [publish-refusal (fn [& args]
                                  (swap! calls conj args)
                                  (alter-var-root #'publish-refusal
                                                  (constantly (fn [& later]
                                                                (swap! calls conj later) (throw error)))))]
      (is (identical? error
                      (try (mobile/validate-native-manifest! (ports) "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest two-hash-calls-preserve-late-function-and-value-reads
  (let [originals [mobile/app-bundle mobile/permission-manifest-fixture] calls (atom [])
        changed (fn [number] (mapv #(str % "\n; change " number "\n") originals))
        set-texts! (fn [texts]
                     (doseq [[operation text] (map vector [#'mobile/app-bundle #'mobile/permission-manifest-fixture] texts)]
                       (alter-var-root operation (constantly text))))]
    (with-redefs [mobile/app-bundle mobile/app-bundle mobile/permission-manifest-fixture mobile/permission-manifest-fixture
                  hash-text (fn [text]
                              (swap! calls conj [:bundle text]) (set-texts! (changed 1))
                              (alter-var-root #'hash-text
                                              (constantly (fn [second-text]
                                                            (swap! calls conj [:permission second-text])
                                                            (set-texts! (changed 2))
                                                            (digest/sha256-hex second-text))))
                              (digest/sha256-hex text))]
      (let [manifest (mobile/native-manifest (ports) "late.gravity" :unverified-id)]
        (is (= [[:bundle (first originals)] [:permission (second (changed 1))]] @calls))
        (doseq [[field text hashed]
                [[:application-bundle-artifact (first (changed 2)) (first originals)]
                 [:permission-manifest (second (changed 2)) (second (changed 1))]]]
          (let [record (get manifest field)]
            (is (= text (:content record)))
            (is (= (digest/sha256-hex hashed) (:hash record)))
            (is (not= (digest/sha256-hex text) (:hash record)))))
        (is (= (get-in manifest [:application-bundle-artifact :hash])
               (get-in manifest [:resource-asset-manifest :resources 0 :hash])))))))

(deftest predicate-order-short-circuit-and-late-lookup-remain-observable
  (let [data (unverified-data) calls (atom [])
        supplied (assoc (ports)
                        :bundle-structurally-valid? (fn [_] (swap! calls conj :bundle) true)
                        :permission-structurally-valid? (fn [_] (swap! calls conj :permission) true))]
    (is (= :complete (mobile/validate-native-manifest! supplied "order.gravity" data)))
    (is (= [:bundle :permission] @calls))
    (reset! calls [])
    (is (= :complete (:status (mobile/native-coverage-report supplied data))))
    (is (= [:bundle :permission] @calls))
    (reset! calls [])
    (mobile/native-coverage-report supplied {})
    (is (= [] @calls))
    (with-redefs [mobile/permission-structurally-valid? mobile/permission-structurally-valid?
                  mobile/bundle-structurally-valid?
                  (fn [_] (alter-var-root #'mobile/permission-structurally-valid? (constantly (fn [_] false))) true)]
      (is (= [:permission-policy] (:missing-fields (refusal data)))))))

(deftest failure-operation-is-selected-before-final-subject-evaluation
  (let [events (atom []) armed (atom false) error (ex-info "Original caller" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b12-diagnostic-stream left))
                       (reset! armed false) (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late caller"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error (try (mobile/validate-native-manifest! (ports) "selection.gravity" data)
                                 nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest forged-content-and-presence-checks-do-not-authenticate-mobile-semantics
  (let [data (-> (unverified-data)
                 (assoc-in [:mobile-backend-manifest :application-bundle-artifact :content] "not an app bundle")
                 (assoc-in [:mobile-backend-manifest :permission-manifest :content] "not permissions")
                 (assoc-in [:mobile-backend-manifest :permission-manifest :permissions] [])
                 (assoc-in [:mobile-backend-manifest :platform-binding-descriptors] [{}]))
        report (mobile/native-coverage-report (ports) {})]
    (is (= :complete (mobile/validate-native-manifest! (ports) "forged.gravity" data)))
    (is (false? (:platform-bindings-covered? (mobile/native-coverage-report (ports) data))))
    (is (= :complete (:status report)))
    (is (true? (:platform-bindings-covered? report)))
    (is (false? (:manifest-complete? report)))
    (is (false? (:backend-interface-input-verified? report)))
    (is (= :not-available-in-current-environment
           (:external-mobile-validation? (mobile/native-coverage-report (ports) data))))))

(deftest new-apis-have-explicit-non-admitting-contracts
  (is (= 13 (count (ns-publics 'gravity.backend.mobile))))
  (doseq [operation [#'mobile/native-manifest #'mobile/validate-native-manifest! #'mobile/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.mobile (:namespace contract)))
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? entry)))
      (is (seq (:required-operations entry)))
      (is (= '[gravity.bootstrap gravity.cli gravity.release]
             (get-in contract [:dependency-direction :must-not-require])))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by]))))))
