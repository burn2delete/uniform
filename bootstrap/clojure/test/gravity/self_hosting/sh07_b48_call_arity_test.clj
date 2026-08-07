(ns gravity.self-hosting.sh07-b48-call-arity-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_b48_call_arity_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B48 test source is not on the classpath"
                      {:id "SH07-B48-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "SH-07-B48 repository root not found"
                        {:id "SH07-B48-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b48-call-arity")

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan "bootstrap/gravity/src/gravity/compiler/c6_call_arity_legality.gravity")))
(def ^:private accepted-plans
  (delay
    (mapv
     #(compile-plan (str fixture-root "/accepted/call-arity" %))
     [".gravity" ".qst"])))
(def ^:private rejected-plans
  (delay
    (mapv
     #(compile-plan (str fixture-root "/rejected/call-arity" %))
     [".gravity" ".qst"])))
(def ^:private rejected-arity-plans
  (delay
    (mapv
     (fn [basename]
       (mapv
        #(compile-plan (str fixture-root "/rejected/" basename %))
        [".gravity" ".qst"]))
     ["too-few-call-arity" "too-many-call-arity"])))

(def ^:private artifacts (atom {}))
(def ^:private verification-reports (atom {}))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b48-call-arity
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- b47-artifact-at [subdirectory basename extension]
  (let [cache-key [subdirectory basename extension]]
    (or (get @artifacts cache-key)
        (let [artifact
              (bootstrap/sh07-core-file-artifact
               (path (str fixture-root "/" subdirectory
                         "/" basename extension)))]
          (swap! artifacts assoc cache-key artifact)
          artifact))))

(defn- b47-artifact [extension]
  (b47-artifact-at "accepted" "call-arity" extension))

(defn- b47-verification [artifact]
  (let [artifact-key artifact]
    (or (get @verification-reports artifact-key)
        (let [verifier (or (ns-resolve 'gravity.bootstrap
                                       'sh07-core-artifact-verification)
                           (throw
                            (ex-info
                             "Required SH-07-B47 verification receipt is absent"
                             {:id "SH07-B47-VERIFICATION-ABSENT"})))
              report (verifier artifact)]
          (swap! verification-reports assoc artifact-key report)
          report))))

(defn- b47-verification-preimage [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:domain :gravity/sh07-b48-b47-verification-binding-v1
     :verified-artifact-id (:artifact-id artifact)
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :authenticated-wrapper artifact
     :canonical-core-artifact core
     :authenticated-core-request (:authenticated-core-request boundary)
     :verification-report report}))

(defn- b47-coordinator-verification [artifact extension report]
  (let [identity (bootstrap/sh07-core-artifact-identity-input artifact)
        boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        preimage (b47-verification-preimage artifact report)
        verification-digest (bootstrap/p15-s23-c11-mir-digest preimage)]
    {:artifact :gravity/sh07-b47-coordinator-verification-v16
     :schema-version 16
     :boundary :clojure-coordinator-verifier
     :verified-artifact-id (:artifact-id artifact)
     :verified-identity-input identity
     :verified-source-path (get-in artifact [:provenance :source-path])
     :verified-source-extension extension
     :report report
     :check-catalog (set (keys (:checks report)))
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :verification-digest-resolution
     {:ordinal 0
      :purpose :sh07-b48-b47-verification-binding
      :preimage preimage
      :digest verification-digest}}))

(defn- coordinator-verification-identity [verification]
  (select-keys verification
               [:artifact :schema-version :boundary :verified-artifact-id
                :verified-identity-input :check-catalog]))

(defn- b47-context [artifact report coordinator]
  (let [core (get-in artifact [:gravity-core-boundary
                               :canonical-core-artifact])
        authenticated-request
        (get-in artifact [:gravity-core-boundary
                          :authenticated-core-request])]
    {:artifact-id (:artifact-id artifact)
     :artifact-status (:status artifact)
     :artifact-kind (:kind artifact)
     :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (bootstrap/sh07-core-artifact-identity-input artifact)
     :authenticated-core-request authenticated-request
     :canonical-core-artifact core
     :provenance (:provenance artifact)
     :lineage (:lineage authenticated-request)
     :authenticated-wrapper artifact
     :verification {:status (:status report)
                    :checks (:checks report)
                    :failed-checks (:failed-checks report)
                    :opaque-provenance-binding-id
                    (:opaque-provenance-binding-id coordinator)
                    :verification-digest
                    (get-in coordinator
                            [:verification-digest-resolution :digest])
                    :receipt-context :gravity/sh07-b47-verification-v16}}))

(defn- b47-request-evidence [artifact extension]
  (let [report (b47-verification artifact)
        coordinator-verification
        (b47-coordinator-verification artifact extension report)]
    {:report report
     :coordinator-verification coordinator-verification
     :context (b47-context artifact report coordinator-verification)}))

(defn- b48-request-for [subdirectory extension]
  (let [artifact (b47-artifact-at subdirectory "call-arity" extension)
        {:keys [context coordinator-verification]}
        (b47-request-evidence artifact extension)
        core (get-in artifact [:gravity-core-boundary
                               :canonical-core-artifact])
        source-unit-id (:artifact-id artifact)
        functions (:function-records core)
        calls (:calls core)
        edges (:call-edges core)]
    {:schema-version 1
     :scope :SH-07-B48
     :source-unit-id source-unit-id
     :identity-input
     {:source-unit-id source-unit-id
      :b47-domain :gravity/sh07-b47-canonical-core-v16
      :b47-identity-input (:identity-input context)
      :b47-verification
      (coordinator-verification-identity coordinator-verification)
      :function-records functions
      :calls calls
      :call-edges edges}
     :provenance
     {:source-path
      (path (str fixture-root "/" subdirectory "/call-arity" extension))
      :source-extension extension
      :source-artifact-id source-unit-id}
     :b47-lineage (:lineage context)
     :b47-context context
     :coordinator-verification coordinator-verification
     :function-records functions
     :calls calls
     :call-edges edges}))

(defn- b48-request-for-basename [subdirectory basename extension]
  (let [artifact (b47-artifact-at subdirectory basename extension)
        {:keys [context coordinator-verification]}
        (b47-request-evidence artifact extension)
        core (get-in artifact [:gravity-core-boundary
                               :canonical-core-artifact])
        source-unit-id (:artifact-id artifact)
        functions (:function-records core)
        calls (:calls core)
        edges (:call-edges core)]
    {:schema-version 1
     :scope :SH-07-B48
     :source-unit-id source-unit-id
     :identity-input
     {:source-unit-id source-unit-id
      :b47-domain :gravity/sh07-b47-canonical-core-v16
      :b47-identity-input (:identity-input context)
      :b47-verification
      (coordinator-verification-identity coordinator-verification)
      :function-records functions
      :calls calls
      :call-edges edges}
     :provenance
     {:source-path
      (path (str fixture-root "/" subdirectory "/" basename extension))
      :source-extension extension
      :source-artifact-id source-unit-id}
     :b47-lineage (:lineage context)
     :b47-context context
     :coordinator-verification coordinator-verification
     :function-records functions
     :calls calls
     :call-edges edges}))

(defn- b48-request [extension]
  (b48-request-for "accepted" extension))

(defn- diagnostic-id [result]
  (get-in result [:diagnostics 0 :diagnostic-id]))

(defn- diagnostic-reason [result]
  (get-in result [:diagnostics 0 :reason]))

(defn- outcomes-by-callee [result]
  (group-by :callee-function-syntax-id (:call-results result)))

(defn- index-by [records key-name]
  (into {} (map (juxt key-name identity)) records))

(defn- update-request-products [request functions calls edges]
  (assoc request
         :function-records functions
         :calls calls
         :call-edges edges
         :identity-input
         {:source-unit-id (:source-unit-id request)
          :b47-domain :gravity/sh07-b47-canonical-core-v16
          :b47-identity-input
          (get-in request [:b47-context :identity-input])
          :b47-verification
          (coordinator-verification-identity
           (:coordinator-verification request))
          :function-records functions
          :calls calls
          :call-edges edges}))

(defn- alter-local-arity [request expected actual]
  (let [functions (:function-records request)
        edges (:call-edges request)
        index
        (first
         (keep-indexed
          (fn [index edge]
            (when (= :local-function (:classification edge))
              (let [target
                    (first
                     (filter #(= (:function-syntax-id %)
                                 (:callee-function-syntax-id edge))
                             functions))]
                (when (= expected (:fixed-arity target)) index))))
          edges))
        call (get (:calls request) index)
        edge (get edges index)
        operator-id (:operator-node-id call)
        argument-ids
        (if (< actual (count (:argument-node-ids call)))
          (vec (take actual (:argument-node-ids call)))
          (vec (concat (:argument-node-ids call)
                       (repeat (- actual (count (:argument-node-ids call)))
                               "sha256:b48-added-argument"))))
        ordered (vec (cons operator-id argument-ids))
        calls' (assoc (:calls request) index
                      (assoc call
                             :argument-node-ids argument-ids
                             :ordered-evaluation-node-ids ordered))
        edges' (assoc edges index
                      (assoc edge
                             :argument-core-node-ids argument-ids
                             :ordered-evaluation-node-ids ordered))]
    (update-request-products request functions calls' edges')))

(defn- first-local-edge-index [request]
  (first
   (keep-indexed
    (fn [index edge]
      (when (= :local-function (:classification edge)) index))
    (:call-edges request))))

(defn- no-host-exception [thunk]
  (deref (future (try {:value (thunk)}
                      (catch Throwable throwable
                        {:throwable throwable})))
         30000
         ::timeout))

(deftest sh07-b48-request-evidence-computes-one-coordinator-digest
  (let [artifact {:artifact-id "artifact"
                  :kind :gravity/sh07-core-artifact
                  :status :accepted
                  :provenance {:source-path "fixture.gravity"}
                  :gravity-core-boundary
                  {:authenticated-core-request {:lineage {:source "fixture"}}
                   :canonical-core-artifact
                   {:identity-preimage {:source "fixture"}}}}
        report {:status :passed :checks {:verified? true} :failed-checks []}
        coordinator-calls (atom 0)]
    (with-redefs
     [b47-verification (fn [candidate]
                         (is (identical? artifact candidate))
                         report)
      b47-coordinator-verification
      (fn [candidate extension observed-report]
        (swap! coordinator-calls inc)
        {:opaque-provenance-binding-id [candidate extension]
         :verification-digest-resolution
         {:digest [extension observed-report]}})]
      (let [{:keys [context coordinator-verification]}
            (b47-request-evidence artifact ".gravity")]
        (is (= 1 @coordinator-calls))
        (is (= (:opaque-provenance-binding-id coordinator-verification)
               (get-in context
                       [:verification :opaque-provenance-binding-id])))
        (is (= (get-in coordinator-verification
                       [:verification-digest-resolution :digest])
               (get-in context [:verification :verification-digest])))))))

(deftest sh07-b48-policy-fixtures-and-parity
  (let [policy (invoke engine-plan 'sh07-b48-call-arity-policy [])]
    (is (= :gravity/sh07-b48-call-arity-policy (:artifact policy)))
    (is (= :gravity/sh07-b47-canonical-core-v16
           (:input-domain policy)))
    (is (= 4096 (:maximum-functions policy)))
    (is (= 65536 (:maximum-calls policy)))
    (is (= 1024 (:maximum-arguments policy)))
    (is (= :bounded-linear-indexes (:index-strategy policy)))
    (is (= :pending-nonlocal (:nonlocal-classification policy)))
    (is (= :host-validated-b47-report-and-opaque-provenance-digest
           (:verification-boundary policy))))
  (doseq [extension [".gravity" ".qst"]
          :let [plan (if (= extension ".gravity")
                        (first @accepted-plans)
                        (second @accepted-plans))]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (doseq [plans @rejected-arity-plans
          plan plans]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (is (= (seq (source-bytes (path (str fixture-root
                                      "/accepted/call-arity.gravity"))))
         (seq (source-bytes (path (str fixture-root
                                      "/accepted/call-arity.qst"))))))
  (is (= (seq (source-bytes (path (str fixture-root
                                      "/rejected/call-arity.gravity"))))
         (seq (source-bytes (path (str fixture-root
                                      "/rejected/call-arity.qst")))))))
  (doseq [basename ["too-few-call-arity" "too-many-call-arity"]]
    (is (= (seq (source-bytes (path (str fixture-root
                                        "/rejected/" basename ".gravity"))))
           (seq (source-bytes (path (str fixture-root
                                        "/rejected/" basename ".qst")))))))

(deftest sh07-b48-authenticated-builder-aligns-with-engine-validators
  (doseq [extension [".gravity" ".qst"]
          :let [request (b48-request extension)
                context (:b47-context request)
                core (:canonical-core-artifact context)]]
    (is (string? (:provenance-binding-id core)))
    (is (not (contains? core :provenance-binding-id-request)))
    (is (true?
         (invoke engine-plan 'sh07-b48-context-valid?
                 [context
                  (:source-unit-id request)
                  (:provenance request)
                  (:function-records request)
                  (:calls request)
                  (:call-edges request)])))
    (is (true?
         (invoke engine-plan 'sh07-b48-coordinator-verification-valid?
                 [(:coordinator-verification request)
                  context
                  (:source-unit-id request)])))
    (is (true?
         (invoke engine-plan 'sh07-b48-request-valid-basic?
                 [request])))))

(deftest sh07-b48-accepts-zero-one-and-fixed-multi-arity
  (let [gravity-request (b48-request ".gravity")
        qst-request (b48-request ".qst")
        gravity
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [gravity-request])
        qst
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [qst-request])
        verification
        (invoke engine-plan 'sh07-b48-verify-call-arity-result
                [gravity-request gravity])
        by-callee (outcomes-by-callee gravity)]
    (is (= :accepted (:status gravity) (:status qst)))
    (is (= (:identity-input gravity) (:identity-input qst)))
    (is (= (get-in gravity [:identity-input :function-records])
           (get-in qst [:identity-input :function-records])))
    (is (= (get-in gravity [:identity-input :calls])
           (get-in qst [:identity-input :calls])))
    (is (= (get-in gravity [:identity-input :call-edges])
           (get-in qst [:identity-input :call-edges])))
    (is (= (get-in gravity [:call-results])
           (get-in qst [:call-results])))
    (is (some #(= 0 (:expected-arity %)) (:call-results gravity)))
    (is (some #(= 1 (:expected-arity %)) (:call-results gravity)))
    (is (some #(= 2 (:expected-arity %)) (:call-results gravity)))
    (is (some #(= :pending-nonlocal (:outcome %))
              (:call-results gravity)))
    (is (= :passed (:status verification)))
    (is (= :failed
           (:status
            (invoke engine-plan 'sh07-b48-verify-call-arity-result
                    [gravity-request
                     (assoc gravity :status :replaced)]))))
    (is (= (get-in gravity [:provenance :source-path])
           (get-in gravity-request [:provenance :source-path])))
    (is (= :passed
           (get-in gravity-request
                   [:b47-context :verification :status])))
    (is (= (get-in gravity-request [:b47-context :identity-input])
           (get-in gravity-request [:identity-input :b47-identity-input])))
    (is (= ".gravity"
           (get-in gravity [:provenance :source-extension])))
    (is (not= (get-in gravity [:provenance :source-path])
              (get-in qst [:provenance :source-path])))
    (let [core (get-in gravity-request
                       [:b47-context :canonical-core-artifact])
          bindings (index-by
                    (get-in gravity-request
                            [:b47-context :authenticated-core-request
                             :binding-table])
                    :binding-id)
          definitions (index-by (:definitions core) :binding-id)
          functions (index-by (:function-records core) :function-syntax-id)
          function-nodes (index-by (:function-records core)
                                   :function-core-node-id)
          local-edge (first (filter #(= :local-function (:classification %))
                                    (:call-edges gravity-request)))
          nonlocal-edge
          (first (filter #(= :nonlocal-or-nonfunction (:classification %))
                         (:call-edges gravity-request)))]
      (is (false?
           (invoke engine-plan 'sh07-b48-edge-binding-coherent?
                   [(assoc local-edge
                           :classification :nonlocal-or-nonfunction
                           :callee-function-syntax-id nil
                           :callee-function-core-node-id nil)
                    bindings definitions functions function-nodes])))
      (is (false?
           (invoke engine-plan 'sh07-b48-edge-binding-coherent?
                   [(assoc nonlocal-edge :callee-definition-syntax-id
                           "sha256:b48-wrong-definition")
                    bindings definitions functions function-nodes]))))))

(deftest sh07-b48-rejects-too-few-and-too-many-with-stable-diagnostics
  (let [too-few (b48-request-for-basename
                 "rejected" "too-few-call-arity" ".gravity")
        too-many (b48-request-for-basename
                  "rejected" "too-many-call-arity" ".gravity")
        few-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [too-few])
        many-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [too-many])
        failed-verification
        (invoke engine-plan 'sh07-b48-verify-call-arity-result
                [too-few (assoc few-result :diagnostics [])])]
    (is (= :rejected (:status few-result) (:status many-result)))
    (is (= "B48-ARITY" (diagnostic-id few-result)
           (diagnostic-id many-result)))
    (is (= :too-few-arguments (diagnostic-reason few-result)))
    (is (= :too-many-arguments (diagnostic-reason many-result)))
    (doseq [result [few-result many-result]]
      (is (string? (get-in result [:diagnostics 0 :syntax-id])))
      (is (string? (get-in result [:diagnostics 0 :form-id])))
      (is (string? (get-in result [:diagnostics 0 :core-node-id])))
      (is (map? (get-in result [:diagnostics 0 :source-span])))
      (is (vector? (get-in result
                           [:diagnostics 0 :generated-origin-chain])))
      (is (= :sh07-b47-function-call-recursion-products
             (get-in result [:diagnostics 0 :lowering-rule])))
      (is (= :meta (get-in result [:diagnostics 0 :profile])))
      (is (= :jvm (get-in result [:diagnostics 0 :target])))
      (is (string? (get-in result [:diagnostics 0 :remediation]))))
    (is (= :failed (:status failed-verification)))
    (doseq [field [:syntax-id :form-id :core-node-id :source-span
                   :generated-origin-chain :lowering-rule :profile :target
                   :remediation]]
      (is (contains? (first (:diagnostics failed-verification)) field)))))

(deftest sh07-b48-rejects-malformed-products-stale-identity-and-bounds
  (let [request (b48-request ".gravity")
        stale (assoc-in request [:identity-input :source-unit-id]
                        "sha256:stale-b48-identity")
        altered-lineage
        (assoc-in request [:b47-lineage :schema-version] 15)
        altered-context
        (assoc-in request
                  [:b47-context :canonical-core-artifact :artifact-id]
                  "sha256:b48-detached-core")
        altered-receipt
        (assoc-in request
                  [:b47-context :verification :status]
                  :failed)
        empty-receipt
        (assoc-in request [:b47-context :verification :checks] {})
        stripped-request
        (assoc-in request [:b47-context :authenticated-core-request]
                  {:lineage (:b47-lineage request)
                   :binding-table
                   (get-in request [:b47-context :authenticated-core-request
                                    :binding-table])})
        altered-coordinator
        (assoc-in request
                  [:coordinator-verification
                   :verification-digest-resolution :preimage
                   :verified-artifact-id]
                  "sha256:b48-altered-verification-report")
        altered-provenance
        (assoc-in request [:provenance :source-path]
                  "/attacker/not-the-b47-source.gravity")
        missing (dissoc request :call-edges)
        duplicate
        (let [functions (conj (:function-records request)
                              (first (:function-records request)))]
          (update-request-products request functions
                                    (:calls request)
                                    (:call-edges request)))
        altered-target
        (let [index (first-local-edge-index request)
              edges (assoc-in (:call-edges request)
                              [index :callee-function-core-node-id]
                              "sha256:b48-altered-target")]
          (update-request-products request
                                    (:function-records request)
                                    (:calls request)
                                    edges))
        oversized
        (let [calls (vec (repeat 65537 (first (:calls request))))
              edges (vec (repeat 65537 (first (:call-edges request))))]
          (update-request-products request
                                    (:function-records request)
                                    calls edges))
        stale-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [stale])
        altered-lineage-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [altered-lineage])
        altered-context-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [altered-context])
        altered-receipt-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                  [altered-receipt])
        empty-receipt-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [empty-receipt])
        stripped-request-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [stripped-request])
        altered-coordinator-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [altered-coordinator])
        altered-provenance-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [altered-provenance])
        missing-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [missing])
        duplicate-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [duplicate])
        altered-target-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result
                [altered-target])
        oversized-result
        (invoke engine-plan 'sh07-b48-build-call-arity-result [oversized])]
    (is (= (vec (repeat 12 :rejected))
           (mapv :status
                 [stale-result altered-lineage-result
                  altered-context-result altered-receipt-result
                  empty-receipt-result stripped-request-result
                  altered-coordinator-result altered-provenance-result
                  missing-result duplicate-result altered-target-result
                  oversized-result])))
    (is (= "B48-IDENTITY" (diagnostic-id stale-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-lineage-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-context-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-receipt-result)))
    (is (= "B48-LINEAGE" (diagnostic-id empty-receipt-result)))
    (is (= "B48-LINEAGE" (diagnostic-id stripped-request-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-coordinator-result)))
    (is (= :coordinator-verification-mismatch
           (diagnostic-reason altered-coordinator-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-provenance-result)))
    (is (= (get-in request [:coordinator-verification
                            :verified-source-path])
           (get-in altered-provenance-result [:provenance :source-path])
           (get-in altered-provenance-result
                   [:diagnostics 0 :source-span :source])))
    (is (= :lineage-mismatch
           (diagnostic-reason altered-lineage-result)))
    (is (= "B48-SCHEMA" (diagnostic-id missing-result)))
    (is (= "B48-PRODUCT" (diagnostic-id duplicate-result)))
    (is (= "B48-LINEAGE" (diagnostic-id altered-target-result)))
    (is (= "B48-BOUND" (diagnostic-id oversized-result)))
    (is (= :malformed-function-products
           (diagnostic-reason duplicate-result)))
    (is (= :call-record-bound
           (diagnostic-reason oversized-result)))
    (is (= :failed
           (:status
            (invoke engine-plan 'sh07-b48-verify-call-arity-result
                    [request (assoc (invoke engine-plan
                                             'sh07-b48-build-call-arity-result
                                             [request])
                                    :identity-input stale)]))))
    (let [safe
          (no-host-exception
           #(invoke engine-plan
                    'sh07-b48-build-call-arity-result
                    [request]))]
      (is (not= ::timeout safe))
      (is (nil? (:throwable safe)))))
    )

(deftest sh07-b48-index-carriers-are-bounded-and-exactly-once
  (let [record {:binding-id "sha256:b48-binding"}
        node {:node-id "sha256:b48-node"}
        request (b48-request ".gravity")
        context (:b47-context request)
        binding (first (get-in context
                               [:authenticated-core-request :binding-table]))
        definition (first (get-in context
                                  [:canonical-core-artifact :definitions]))
        core-node (first (get-in context
                                 [:canonical-core-artifact :nodes]))
        duplicate-binding
        (update-in context [:authenticated-core-request :binding-table]
                   conj binding)
        duplicate-definition
        (update-in context [:canonical-core-artifact :definitions]
                   conj definition)
        duplicate-node
        (update-in context [:canonical-core-artifact :nodes]
                   conj core-node)
        oversized-binding
        (assoc-in context [:authenticated-core-request :binding-table]
                  (vec (repeat 2441 binding)))]
    (is (true?
         (invoke engine-plan 'sh07-b48-index-carrier-valid?
                 [[record] :binding-id 1])))
    (is (false?
         (invoke engine-plan 'sh07-b48-index-carrier-valid?
                 [[record record] :binding-id 2])))
    (is (false?
         (invoke engine-plan 'sh07-b48-index-carrier-valid?
                 [[record record] :binding-id 1])))
    (is (false?
         (invoke engine-plan 'sh07-b48-index-carrier-valid?
                 [[node node] :node-id 2])))
    (is (false?
         (invoke engine-plan 'sh07-b48-index-carrier-valid?
                 [[{}] :binding-id 1])))
    (is (= :malformed-binding-index-carrier
           (:reason
            (invoke engine-plan 'sh07-b48-index-carriers-preflight
                    [duplicate-binding]))))
    (is (= :malformed-definition-index-carrier
           (:reason
            (invoke engine-plan 'sh07-b48-index-carriers-preflight
                    [duplicate-definition]))))
    (is (= :malformed-node-index-carrier
           (:reason
            (invoke engine-plan 'sh07-b48-index-carriers-preflight
                    [duplicate-node]))))
    (is (= :binding-table-bound
           (:reason
            (invoke engine-plan 'sh07-b48-index-carriers-preflight
                    [oversized-binding]))))))

(deftest sh07-b48-coordinated-upstream-rewrite-cannot-self-assert-passage
  (let [request (b48-request ".gravity")
        original-wrapper (get-in request [:b47-context
                                          :authenticated-wrapper])
        original-core (get-in original-wrapper
                              [:gravity-core-boundary
                               :canonical-core-artifact])
        definitions (conj (:definitions original-core)
                          (first (:definitions original-core)))
        rewritten-core (assoc original-core :definitions definitions)
        rewritten-wrapper
        (assoc-in original-wrapper
                  [:gravity-core-boundary :canonical-core-artifact]
                  rewritten-core)
        verifier (or (ns-resolve 'gravity.bootstrap
                                 'sh07-core-artifact-verification)
                     (throw (ex-info "B47 verifier absent" {})))
        failed-report (verifier rewritten-wrapper)
        coordinator
        (b47-coordinator-verification rewritten-wrapper ".gravity"
                                      failed-report)
        context
        (-> (:b47-context request)
            (assoc :authenticated-wrapper rewritten-wrapper
                   :canonical-core-artifact rewritten-core
                   :verification
                   {:status (:status failed-report)
                    :checks (:checks failed-report)
                    :failed-checks (:failed-checks failed-report)
                    :opaque-provenance-binding-id
                    (:opaque-provenance-binding-id coordinator)
                    :verification-digest
                    (get-in coordinator
                            [:verification-digest-resolution :digest])
                    :receipt-context
                    :gravity/sh07-b47-verification-v16}))
        rewritten
        (-> request
            (assoc :b47-context context
                   :coordinator-verification coordinator)
            (assoc-in [:identity-input :b47-verification]
                      (coordinator-verification-identity coordinator)))
        result (invoke engine-plan 'sh07-b48-build-call-arity-result
                       [rewritten])]
    (is (= :failed (:status failed-report)))
    (is (= :rejected (:status result)))
    (is (= "B48-LINEAGE" (diagnostic-id result)))))

(deftest sh07-b48-rejected-fixture-compiles-and-direct-products-are-contained
  (doseq [plan @rejected-plans]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (doseq [extension [".gravity" ".qst"]
          :let [request (b48-request-for "rejected" extension)
                result
                (invoke engine-plan
                        'sh07-b48-build-call-arity-result
                        [request])]]
    (is (= :rejected (:status result)))
    (is (= "B48-ARITY" (diagnostic-id result)))
    (is (= (path (str fixture-root "/rejected/call-arity" extension))
           (get-in result [:provenance :source-path]))))
  (let [request (b48-request ".gravity")
        altered (assoc-in request [:call-edges 0 :classification]
                          :unrecognized)
        result (invoke engine-plan 'sh07-b48-build-call-arity-result [altered])]
    (is (= :rejected (:status result)))
    (is (= "B48-PRODUCT" (diagnostic-id result)))))

(deftest sh07-b48-binds-ordered-argument-tail-and-rejects-substitution
  (let [request (b48-request ".gravity")
        index
        (first
         (keep-indexed
          (fn [candidate-index edge]
            (when (and (= :local-function (:classification edge))
                       (>= (count
                            (:argument-node-ids
                             (get (:calls request) candidate-index)))
                           2))
              candidate-index))
          (:call-edges request)))]
    (is (some? index))
    (when (some? index)
      (let [call (get (:calls request) index)
            edge (get (:call-edges request) index)
            operator (:operator-node-id call)
            arguments (:argument-node-ids call)
            reversed-tail (vec (reverse arguments))
            calls' (assoc (:calls request) index
                          (assoc call
                                 :ordered-evaluation-node-ids
                                 (vec (cons operator reversed-tail))))
            edges' (assoc (:call-edges request) index
                          (assoc edge
                                 :ordered-evaluation-node-ids
                                 (vec (cons operator reversed-tail))))
            altered (update-request-products request
                                             (:function-records request)
                                             calls' edges')
            result (invoke engine-plan
                           'sh07-b48-build-call-arity-result
                           [altered])]
        (is (>= (count arguments) 2))
        (is (not= arguments reversed-tail))
        (is (true?
             (invoke engine-plan 'sh07-b48-call-record-valid? [call])))
        (is (true?
             (invoke engine-plan 'sh07-b48-call-record-valid?
                     [(get calls' index)])))
        (is (false?
             (invoke engine-plan 'sh07-b48-ordered-tail-valid?
                     [(get calls' index)])))
        (is (= :rejected (:status result)))
        (is (= "B48-LINEAGE" (diagnostic-id result)))
        (is (= :lineage-mismatch (diagnostic-reason result)))))))
