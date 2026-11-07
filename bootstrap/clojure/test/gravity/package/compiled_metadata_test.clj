(ns gravity.package.compiled-metadata-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.package :as package]))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error
         {:message (.getMessage error) :data (ex-data error)})))

(defn- compile-fixture [relative]
  (let [path (.getCanonicalPath (io/file relative))
        text (slurp path :encoding "UTF-8")
        macro-artifact (b/macro-source-artifact path text)
        module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))]
    (b/stage0-compiled-core-plan path text module)))

(def ^:private checks
  [[package/check-project-manifest! b/validate-stage0-compiled-project-manifest! "PKG1006" {}]
   [package/check-artifact-manifest! b/validate-stage0-compiled-artifact-manifest! "PKG3005" {}]
   [package/check-package-operation! b/validate-stage0-compiled-package-operation! "PKG4001" {}]
   [package/check-resolution-report! b/validate-stage0-compiled-resolution-report! "PKG5002" {}]
   [package/check-capability-manifest! b/validate-stage0-compiled-capability-manifest! "PKG6004"
    {:capabilities {:denies #{:network} :requests #{:network}}}]
   [(fn [fail-var module subject] (package/check-package-safety! #'b/p12-present? fail-var module subject))
    b/validate-stage0-compiled-package-safety! "PKG8001" {}]
   [package/check-registry-record! b/validate-stage0-compiled-registry-record! "PKG9001" {}]
   [package/check-provenance-record! b/validate-stage0-compiled-provenance-record! "PKG10001" {}]
   [package/check-target-matrix! b/validate-stage0-compiled-target-matrix! "PKG11002" {:implicit-host-target? true}]
   [package/check-signing-sbom! b/validate-stage0-compiled-signing-sbom! "PKG12002" {}]])

(deftest wrappers-preserve-complete-bootstrap-diagnostic-payloads
  (doseq [[direct wrapper expected rejected] checks]
    (let [module {:source-path "metadata.gravity" :profile :hosted :target :jvm}
          subject (assoc rejected :unsafe-island {:audit :required}
                         :runtime-checked [:retained] :ai/human-review :required)
          owned (failure #(direct #'b/compiled-package-fail! module subject))
          compatibility (failure #(wrapper module subject))]
      (is (= owned compatibility))
      (is (= expected (get-in owned [:data :id])))
      (is (= :stage0-compiled-package-gate (get-in owned [:data :stage])))
      (is (= :phase12-compiled-package-artifacts (get-in owned [:data :diagnostic-family])))
      (is (= :package-build-artifact (get-in owned [:data :compiled-gate])))
      (is (= {:source "metadata.gravity" :form-index 0} (get-in owned [:data :source-span]))))))

(deftest wrappers-retain-late-diagnostic-root-and-exact-exceptions
  (doseq [[direct wrapper _ rejected] checks
          operation [(fn [module subject] (direct #'b/compiled-package-fail! module subject)) wrapper]]
    (let [sentinel (Object.) calls (atom [])
          final (fn [& arguments] (swap! calls conj arguments) sentinel)
          subject (reify clojure.lang.ILookup
                    (valAt [this key] (.valAt ^clojure.lang.ILookup this key nil))
                    (valAt [_ key fallback]
                      (alter-var-root #'b/compiled-package-fail! (constantly final))
                      (get rejected key fallback)))]
      (with-redefs [b/compiled-package-fail! (fn [& _] (throw (ex-info "Premature diagnostic root" {})))]
        (is (identical? sentinel (operation {} subject))))
      (is (= 1 (count @calls)))
      (is (identical? subject (nth (first @calls) 2))))
    (let [original (ex-info "original failure" {:origin :bootstrap-caller})
          observed (with-redefs [b/compiled-package-fail! (fn [& _] (throw original))]
                     (try (operation {} rejected) (catch Throwable error error)))]
      (is (identical? original observed)))))

(deftest package-and-build-aggregate-positions-remain-interleaved
  (let [module {:metadata {:package {:compiled-gate
                                     {:project-manifests [{}]
                                      :build-graphs [{:declared-effects #{}
                                                       :nodes [{:effects #{:build/network}}]}]
                                      :artifact-manifests [{}] :package-operations [{}]
                                      :resolution-reports [{}]
                                      :capability-manifests [{:capabilities {:denies #{:network}
                                                                            :requests #{:network}}}]
                                      :reproducible-build-recipes [{:environment {:network :enabled}}]
                                      :package-safety-records [{}] :registry-records [{}]
                                      :provenance-records [{}] :target-matrices [{:implicit-host-target? true}]
                                      :signing-sbom-verifications [{}]}}}}
        calls (atom [])]
    (with-redefs [b/compiled-package-fail! (fn [id & _] (swap! calls conj id))]
      (is (nil? (b/validate-stage0-compiled-package! module))))
    (is (= ["PKG1006" "PKG2001" "PKG3005" "PKG4001" "PKG5002" "PKG6004" "PKG7003"
            "PKG8001" "PKG9001" "PKG10001" "PKG11002" "PKG12002"] @calls))
    (is (= "PKG1006" (get-in (failure #(b/validate-stage0-compiled-package! module)) [:data :id])))))

(deftest accepted-source-emits-deterministic-provenance-bearing-plans-and-runs
  ;; Ordinary host reader and Stage0 instructions; no authenticated source or package admission.
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (str "bootstrap/clojure/fixtures/package-metadata/accepted/metadata-checks" extension)
          plan (compile-fixture relative)
          target (io/file "target/package-metadata" (str "metadata-checks" extension ".edn"))]
      (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
      (is (= plan (compile-fixture relative)))
      (is (= (:plan-id plan) (str "sha256:" (b/sha256-hex (pr-str (dissoc plan :plan-id))))))
      (is (= (str "sha256:" (b/sha256-hex (slurp relative))) (get-in plan [:source :sha256])))
      (is (= (.getCanonicalPath (io/file relative)) (get-in plan [:source :path])))
      (io/make-parents target)
      (spit target (pr-str plan))
      (is (= plan (edn/read-string (slurp target))))
      (is (= "package-metadata\n" (b/execute-stage0-compiled-plan plan))))))

(deftest invalid-metadata-refuses-both-source-extensions-before-execution
  (doseq [extension [".gravity" ".qst"]
          [name expected] [["missing-lockfile" "PKG1006"] ["missing-safety-evidence" "PKG3005"]
                           ["unverified-download" "PKG4001"] ["incompatible-capability" "PKG5002"]
                           ["denied-capability" "PKG6004"] ["missing-unsafe-audit" "PKG8001"]
                           ["denied-registry" "PKG9001"] ["missing-provenance" "PKG10001"]
                           ["implicit-host-target" "PKG11002"] ["noncanonical-signature" "PKG12002"]]]
    (let [relative (str "bootstrap/clojure/fixtures/package-metadata/rejected/" name extension)
          executions (atom 0)
          rejected (with-redefs [b/execute-stage0-compiled-plan (fn [_] (swap! executions inc))]
                     (failure #(b/run-compiled-file relative)))]
      (is (= expected (get-in rejected [:data :id])))
      (is (= :stage0-compiled-package-gate (get-in rejected [:data :stage])))
      (is (= relative (get-in rejected [:data :source-span :source])))
      (is (zero? @executions)))))

(deftest original-p12-presence-port-remains-before-subject-observation
  (doseq [operation [(fn [module subject]
                      (package/check-package-safety! #'b/p12-present? #'b/compiled-package-fail! module subject))
                    b/validate-stage0-compiled-package-safety!]]
    (let [events (atom []) sentinel (Object.)
          subject (reify clojure.lang.ILookup
                    (valAt [this key] (.valAt ^clojure.lang.ILookup this key nil))
                    (valAt [_ key _]
                      (swap! events conj [:lookup key])
                      (alter-var-root #'b/p12-present? (constantly (fn [_] (throw (ex-info "Late presence root" {})))))
                      :unverified))]
      (with-redefs [b/p12-present? (fn [value] (swap! events conj [:old-presence value]) false)
                    b/compiled-package-fail! (fn [id & _] (swap! events conj [:failure id]) sentinel)]
        (is (identical? sentinel (operation {} subject))))
      (is (= [[:lookup :unsafe-audit-metadata] [:old-presence :unverified] [:failure "PKG8001"]] @events))))
  (doseq [value [false "" 0 [:unverified] {:audit :unverified} #{:unverified}]]
    (is (nil? (b/validate-stage0-compiled-package-safety! {} {:unsafe-audit-metadata value})))))
