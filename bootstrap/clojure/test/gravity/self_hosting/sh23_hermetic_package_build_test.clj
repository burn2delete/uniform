(ns gravity.self-hosting.sh23-hermetic-package-build-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh23_hermetic_package_build_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-23 test source is not on the classpath"
                {:id "SH23-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH23-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-23")

(defn- fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity")))

(def ^:private sh20-engine-plan
  (delay
   (compile-plan
    "bootstrap/clojure/fixtures/self-hosting/sh-20/artifact_bundle_engine.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "hermetic-builds" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "hermetic-builds" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-hermetic-builds" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-hermetic-builds" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh23-hermetic-package-build-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- build
  [request]
  (invoke-engine 'sh23-build [request]))

(defn- large-build-request
  []
  (let [base (request accepted-gravity-plan 'sh23-hermetic-build-request)
        package-ids (mapv #(keyword "package" (str "library-" %)) (range 33))
        component-ids (mapv #(keyword "component" (str "module-" %)) (range 130))
        packages (mapv (fn [id]
                         (assoc (first (get-in base [:lockfile :packages]))
                                :package-id id
                                :content-id (str "sha256:" id)
                                :provenance-id (str "sha256:provenance-" id)))
                       package-ids)
        components
        (mapv (fn [index id]
                (assoc (first (:components base))
                       :component-id id
                       :source-content-id (str "sha256:source-" id)
                       :compiler-module-id (keyword "compiler" (name id))
                       :dependencies (if (= index 129)
                                       (subvec component-ids 0 index)
                                       [])
                       :stdlib-dependencies (if (= index 129)
                                              package-ids
                                              [(first package-ids)])
                       :output-id (keyword "artifact" (name id))
                       :output-content-hash (format "%064x" (inc index))
                       :topological-ordinal (inc index)
                       :actual-source-path (str "/checkout-a/src/" (name id)
                                                ".gravity")))
              (range 130) component-ids)]
    (-> base
        (assoc :components components :component-order component-ids)
        (assoc-in [:project :entrypoints] (subvec component-ids 0 33))
        (assoc-in [:project :dependency-roots] package-ids)
        (assoc-in [:project :source-roots]
                  (mapv #(str "src/root-" %) (range 17)))
        (assoc-in [:lockfile :packages] packages)
        (assoc-in [:environment :allowed-environment]
                  (mapv #(keyword (str "BUILD_INPUT_" %)) (range 17))))))

(def ^:private rejected-cases
  {'sh23-missing-lock-request
   ["PKG5004" :bootstrap-lockfile-record-required]
   'sh23-online-request
   ["PKG7003" :offline-lockfile-required]
   'sh23-capability-expansion-request
   ["PKG5002" :dependency-capability-expansion]
   'sh23-forbidden-effect-request
   ["PKG2001" :undeclared-or-unsupported-build-effect]
   'sh23-uncontrolled-environment-request
   ["PKG7005" :filesystem-order-must-be-sorted]
   'sh23-implicit-target-request
   ["PKG11002" :explicit-target-manifest-required]
   'sh23-illegal-meta-request
   ["BOOT3002" :compiler-program-not-legal]
   'sh23-library-gap-request
   ["PKG3003" :stdlib-conformance-identity-required]
   'sh23-missing-dependency-request
   ["PKG2004" :missing-cyclic-or-forward-component-dependency]
   'sh23-cycle-request
   ["PKG2004" :missing-cyclic-or-forward-component-dependency]
   'sh23-order-mismatch-request
   ["PKG2004" :component-order-mismatch]
   'sh23-path-identity-input-request
   ["PKG3003" :component-source-identity-required]
   'sh23-inexact-project-authority-request
   ["PKG1005" :exact-project-build-effects-required]
   'sh23-inexact-build-authority-request
   ["PKG2001" :undeclared-or-unsupported-build-effect]
   'sh23-extra-locked-package-request
   ["PKG5004" :locked-package-set-mismatch]
   'sh23-unlocked-component-package-request
   ["PKG5004" :component-standard-library-dependency-not-locked]
   'sh23-malformed-forward-ordinal-request
   ["PKG2004" :component-ordinal-must-be-integer]
   'sh23-unsupported-backend-request
   ["PKG11001" :unsupported-bootstrap-backend]})

(deftest sh23-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh23-build-policy sh23-build sh23-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "hermetic-builds"]
           ["rejected" "invalid-hermetic-builds"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh23-policy-is-hermetic-and-honest
  (let [policy (invoke-engine 'sh23-build-policy [])]
    (is (= :gravity/sh23-hermetic-build-policy (:artifact policy)))
    (is (= 1 (:schema-version policy)))
    (is (= #{:meta} (:allowed-profiles policy)))
    (is (= #{:portable-mir} (:allowed-targets policy)))
    (is (contains? (:forbidden-effects policy) :build/network))
    (is (contains? (:forbidden-effects policy) :shell/exec))
    (is (contains? (:diagnostics policy) "PKG2004"))
    (is (contains? (:diagnostics policy) "PKG7005"))
    (is (some #{:general-semver-resolution} (:pending policy)))
    (is (some #{:network-registry} (:pending policy)))
    (is (some #{:build-action-execution} (:pending policy)))
    (is (some #{:seedless-bootstrap} (:pending policy)))))

(deftest sh23-resolves-fixed-lock-and-builds-deterministic-actions
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request (request plan 'sh23-hermetic-build-request)
          result (build request)
          encoded (pr-str result)
          decoded (edn/read-string encoded)]
      (is (= :accepted (:status result)))
      (is (empty? (:diagnostics result)))
      (is (= result decoded))
      (is (= :fixed-lockfile (get-in result [:resolution :mode])))
      (is (true? (get-in result [:resolution :offline])))
      (is (= "sha256:sh23-lock"
             (get-in result [:resolution :lock-id])))
      (is (= 3 (get-in result [:build-graph :component-count])))
      (is (true? (get-in result [:build-graph :acyclic])))
      (is (= [:component/reader
              :component/analyzer
              :component/emitter]
             (get-in result [:build-graph :component-order])))
      (is (= [1 2 3] (mapv :ordinal (:actions result))))
      (is (= [:component/reader
              :component/analyzer
              :component/emitter]
             (mapv :action-id (:actions result))))
      (is (every? #(= #{:build/read-file :build/write-artifact}
                      (:effects %))
                  (:actions result)))
      (is (= 3 (count (:artifact-requests result))))
      (is (= :passed
             (:status
              (invoke-engine
               'sh23-verify-result [request result])))))))

(deftest sh23-builds-with-more-than-sixteen-source-roots
  (let [base-request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)
        base-result (build base-request)
        roots (mapv #(str "bootstrap/gravity/src/root-" %) (range 17))
        expanded-request (assoc-in base-request [:project :source-roots] roots)
        result (build expanded-request)]
    (is (= :accepted (:status result)))
    (is (empty? (:diagnostics result)))
    (is (= roots (get-in result [:identity-input :project :source-roots])))
    (is (= (assoc-in (:identity-input base-result)
                    [:project :source-roots] roots)
           (:identity-input result)))
    (is (= (:actions base-result) (:actions result)))
    (is (= (:build-graph base-result) (:build-graph result)))
    (is (= (:artifact-requests base-result) (:artifact-requests result)))
    (is (= (:provenance base-result) (:provenance result)))
    (is (= result (edn/read-string (pr-str result))))
    (is (= :passed
           (:status (invoke-engine 'sh23-verify-result
                                   [expanded-request result]))))))

(deftest sh23-builds-large-locked-graphs-with-complete-identities-and-replay
  ;; PKG1/2/5/6/7: populations do not replace lock, graph or authority legality.
  (let [request (large-build-request)
        components (:components request)
        result (build request)
        decoded (edn/read-string (pr-str result))]
    (is (= :accepted (:status result)))
    (is (empty? (:diagnostics result)))
    (is (= 33 (count (get-in result [:resolution :packages]))))
    (is (= (get-in request [:lockfile :packages])
           (get-in result [:resolution :packages])))
    (is (= 130 (get-in result [:build-graph :component-count])))
    (is (true? (get-in result [:build-graph :acyclic])))
    (is (= (:component-order request)
           (get-in result [:build-graph :component-order])
           (mapv :action-id (:actions result))))
    (is (= (vec (range 1 131)) (mapv :ordinal (:actions result))))
    (is (= (mapv :dependencies components)
           (mapv #(get-in % [:inputs :dependency-components]) (:actions result))))
    (is (= 129 (count (get-in result [:actions 129 :inputs
                                     :dependency-components]))))
    (is (= (mapv :stdlib-dependencies components)
           (mapv #(get-in % [:inputs :standard-library-packages])
                 (:actions result))))
    (is (= (mapv :source-content-id components)
           (mapv #(get-in % [:inputs :source-content-id]) (:actions result))))
    (is (every? #(= (get-in request [:compiler :artifact-id])
                    (get-in % [:inputs :compiler-artifact-id]))
                (:actions result)))
    (is (every? #(= (get-in request [:lockfile :lock-id])
                    (get-in % [:inputs :lock-id]))
                (:actions result)))
    (is (every? #(and (= (:effects request) (:effects %))
                     (= (:capabilities request) (:capabilities %)))
                (:actions result)))
    (is (= (mapv :output-id components)
           (mapv :output-id (:actions result))
           (mapv :artifact-id (:artifact-requests result))))
    (is (= (mapv :output-content-hash components)
           (mapv #(get-in % [:content-hash :digest]) (:artifact-requests result))))
    (is (= (assoc request :components (mapv #(dissoc % :actual-source-path)
                                            components))
           (:identity-input result)))
    (is (= (mapv #(select-keys % [:component-id :source-content-id
                                 :actual-source-path]) components)
           (get-in result [:provenance :components])))
    (is (= result decoded))
    (is (= :passed
           (:status (invoke-engine 'sh23-verify-result [request decoded]))))
    (let [verification
          (invoke-engine 'sh23-verify-result
                         [request (assoc-in decoded [:actions 129 :output-id]
                                            :artifact/substituted)])]
      (is (= :rejected (:status verification)))
      (is (= ["SH23-VERIFY" :build-result-substitution]
             ((juxt :rule :reason) (first (:diagnostics verification))))))))

(deftest sh23-large-builds-retain-semantic-refusals
  (let [request (large-build-request)]
    (doseq [[label invalid rule reason]
            [[:missing-lock (dissoc request :lockfile)
              "PKG5004" :bootstrap-lockfile-record-required]
             [:extra-locked-package
              (update-in request [:lockfile :packages] conj
                         (assoc (first (get-in request [:lockfile :packages]))
                                :package-id :package/unrequested))
              "PKG5004" :locked-package-set-mismatch]
             [:unlocked-dependency
              (assoc-in request [:components 129 :stdlib-dependencies]
                        [:package/unlocked])
              "PKG5004" :component-standard-library-dependency-not-locked]
             [:forward-edge
              (assoc-in request [:components 0 :dependencies]
                        [(second (:component-order request))])
              "PKG2004" :missing-cyclic-or-forward-component-dependency]
             [:cycle
              (assoc-in request [:components 129 :dependencies]
                        (:component-order request))
              "PKG2004" :missing-cyclic-or-forward-component-dependency]
             [:malformed-ordinal
              (assoc-in request [:components 129 :topological-ordinal] "130")
              "PKG2004" :component-ordinal-must-be-integer]
             [:exact-authority
              (assoc request :capabilities #{:build/read-file})
              "PKG2001" :build-effect-capability-mismatch]
             [:unsupported-backend
              (assoc-in request [:target :backend] :gravity.backend/llvm)
              "PKG11001" :unsupported-bootstrap-backend]
             [:environment-policy
              (assoc-in request [:environment :network] :enabled)
              "PKG7003" :network-must-be-disabled]]]
      (testing (name label)
        (let [result (build invalid)]
          (is (= :rejected (:status result)))
          (is (= [[rule reason]]
                 (mapv (juxt :rule :reason) (:diagnostics result)))))))))

(deftest sh23-rejects-malformed-source-roots
  (let [base-request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)]
    (doseq [roots [nil [] '("src") ["src" "src"] [""] [17]]]
      (testing (pr-str roots)
        (let [result (build (assoc-in base-request [:project :source-roots]
                                      roots))
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= "PKG1001" (:rule diagnostic)))
          (is (= :source-root-vector-invalid (:reason diagnostic))))))))

(deftest sh23-retains-vector-and-authority-shape-validation
  (let [request (request accepted-gravity-plan 'sh23-hermetic-build-request)]
    (doseq [[key-path rule reason]
            [[[:project :entrypoints] "PKG1001" :entrypoint-vector-required]
             [[:project :dependency-roots] "PKG1004" :dependency-root-vector-required]
             [[:lockfile :packages 0 :targets] "PKG5003" :locked-target-vector-required]
             [[:environment :allowed-environment] "PKG7005" :environment-allowlist-invalid]
             [[:components 0 :stdlib-dependencies] "PKG3003" :stdlib-dependency-vector-invalid]]
            invalid [nil '(:invalid) [:duplicate :duplicate] ["not-a-keyword"]]]
      (testing (pr-str [key-path invalid])
        (let [result (build (assoc-in request key-path invalid))]
          (is (= :rejected (:status result)))
          (is (= [[rule reason]]
                 (mapv (juxt :rule :reason) (:diagnostics result)))))))
    (doseq [[key-path rule reason]
            [[[:project :effects] "PKG1005" :effect-set-required]
             [[:project :capabilities] "PKG1005" :capability-set-required]
             [[:lockfile :packages 0 :effects] "PKG5002" :locked-effect-set-invalid]
             [[:lockfile :packages 0 :capabilities] "PKG5002" :locked-capability-set-invalid]
             [[:effects] "PKG2001" :build-effect-set-required]
             [[:capabilities] "PKG6004" :build-capability-set-required]]]
      (testing (pr-str key-path)
        (let [result (build (assoc-in request key-path []))]
          (is (= :rejected (:status result)))
          (is (= [[rule reason]]
                 (mapv (juxt :rule :reason) (:diagnostics result)))))))
    (let [authorities (set (map #(keyword "unsupported" (str "authority-" %))
                                (range 17)))]
      (doseq [[key-path rule reason]
              [[[:project :effects] "PKG1005" :exact-project-build-effects-required]
               [[:project :capabilities] "PKG1005" :project-effect-capability-mismatch]
               [[:lockfile :packages 0 :effects] "PKG5002" :dependency-effect-expansion]
               [[:lockfile :packages 0 :capabilities] "PKG5002" :dependency-capability-expansion]
               [[:effects] "PKG2001" :undeclared-or-unsupported-build-effect]
               [[:capabilities] "PKG2001" :build-effect-capability-mismatch]]]
        (testing (pr-str key-path)
          (let [result (build (assoc-in request key-path authorities))]
            (is (= :rejected (:status result)))
            (is (= [[rule reason]]
                   (mapv (juxt :rule :reason) (:diagnostics result))))))))))

(deftest sh23-links-sh21-sh22-and-emits-valid-sh20-requests
  (let [request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)
        result (build request)]
    (is (= :gravity/sh21-meta-legality-result
           (get-in result [:identity-input :legality :artifact])))
    (is (= :accepted
           (get-in result [:identity-input :legality :status])))
    (is (= :gravity/standard-library-module-manifest
           (get-in result [:identity-input :library :artifact])))
    (is (= "sha256:sh22-conformance"
           (get-in result [:identity-input :library :conformance-id])))
    (doseq [artifact-request (:artifact-requests result)]
      (let [bundle
            (invoke
             sh20-engine-plan 'sh20-build-bundle [artifact-request])]
        (is (= :accepted (:status bundle)))
        (is (= :gravity/artifact-manifest
               (get-in bundle [:manifest :artifact])))
        (is (= :meta (get-in bundle [:manifest :profile])))
        (is (= :portable-mir
               (get-in bundle [:manifest :target])))
        (is (= :passed
               (:status
                (invoke
                 sh20-engine-plan
                 'sh20-verify-bundle
                 [artifact-request bundle]))))))))

(deftest sh23-keeps-physical-paths-outside-semantic-identity
  (let [first-request
        (request
         accepted-gravity-plan 'sh23-hermetic-build-request)
        second-request
        (request
         accepted-gravity-plan
         'sh23-hermetic-build-alternate-path-request)
        first-result (build first-request)
        second-result (build second-request)
        first-bundles
        (mapv
         #(invoke sh20-engine-plan 'sh20-build-bundle [%])
         (:artifact-requests first-result))
        second-bundles
        (mapv
         #(invoke sh20-engine-plan 'sh20-build-bundle [%])
         (:artifact-requests second-result))]
    (is (not= (mapv :actual-source-path (:components first-request))
              (mapv :actual-source-path (:components second-request))))
    (is (= (:identity-input first-result)
           (:identity-input second-result)))
    (is (= (:actions first-result) (:actions second-result)))
    (is (= (mapv :identity-input first-bundles)
           (mapv :identity-input second-bundles)))
    (is (every? #(= :accepted (:status %))
                (concat first-bundles second-bundles)))
    (is (= (mapv #(dissoc % :actual-source-path
                          :actual-output-path :source-span :source-map)
                 (:artifact-requests first-result))
           (mapv #(dissoc % :actual-source-path
                          :actual-output-path :source-span :source-map)
                 (:artifact-requests second-result))))
    (is (not= (:provenance first-result)
              (:provenance second-result)))
    (is (not (str/includes?
              (pr-str (:identity-input first-result)) "/checkout-a/")))
    (is (not (str/includes?
              (pr-str (:identity-input second-result)) "/checkout-b/")))))

(deftest sh23-rejects-every-incomplete-build-family-structurally
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (build gravity-request)
            qst-result (build qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :hermetic-package-build (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :passed
               (:status
                (invoke-engine
                 'sh23-verify-result
                 [gravity-request gravity-result]))))))))

(deftest sh23-verifier-recomputes-and-rejects-substitutions
  (let [request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)
        result (build request)
        changed
        (assoc-in result [:actions 1 :output-id] :artifact/substituted)
        verification
        (invoke-engine 'sh23-verify-result [request changed])
        diagnostic (first (:diagnostics verification))]
    (is (= :rejected (:status verification)))
    (is (= "SH23-VERIFY" (:rule diagnostic)))
    (is (= :build-result-substitution (:reason diagnostic)))
    (is (= :hermetic-package-build (:stage diagnostic)))))
