(ns gravity.self-hosting.w5-tooling-conformance-fixture-support
  "Shared W5 tooling-conformance plans and request adapters.

  This namespace owns only the delayed source/fixture compilation and the
  normalized request adapters.  The W5 verifier tests and tooling pipeline
  consume these values without loading one another or probing private vars."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_tooling_conformance_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 tooling-conformance test source is not on the classpath"
        {:id "W5-TC-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-TC-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_tooling_conformance_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-tooling-conformance")

(defn path [relative]
  (str (.resolve @root relative)))

(defn fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def engine-plan (delay (compile-plan engine-source)))
(def accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "tooling-conformance" ".gravity"))))
(def accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "tooling-conformance" ".qst"))))
(def rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-tooling-conformance" ".gravity"))))
(def rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-tooling-conformance" ".qst"))))

(defn invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-tooling-conformance-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn request
  ([plan function]
   (invoke plan function []))
  ([plan function source-path extension source-kind]
   (invoke plan function [source-path extension source-kind])))

(defn request-at [plan source-path extension source-kind]
  (invoke plan 'w5-tooling-conformance-request-at
          [source-path extension source-kind]))

(defn verify [request-value]
  (invoke engine-plan 'w5-tooling-conformance-verify [request-value]))

(def rejected-cases
  {'w5-tooling-conformance-invalid-source-request "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-check-request "W5-TC-CHECK"
   'w5-tooling-conformance-invalid-replay-request "W5-TC-REPLAY"
   'w5-tooling-conformance-invalid-narrative-request "W5-TC-NARRATIVE"
   'w5-tooling-conformance-invalid-suite-request "W5-TC-SUITE"
   'w5-tooling-conformance-invalid-provenance-request "W5-TC-PROVENANCE"
   'w5-tooling-conformance-invalid-authority-request "W5-TC-AUTHORITY"
   'w5-tooling-conformance-source-substitution-request "W5-TC-SOURCE"
   'w5-tooling-conformance-check-substitution-request "W5-TC-CHECK"
   'w5-tooling-conformance-replay-substitution-request "W5-TC-REPLAY"
   'w5-tooling-conformance-narrative-substitution-request
   "W5-TC-NARRATIVE"
   'w5-tc-invalid-source-request "W5-TC-SOURCE"
   'w5-tc-invalid-check-request "W5-TC-CHECK"
   'w5-tc-invalid-replay-request "W5-TC-REPLAY"
   'w5-tc-invalid-narrative-request "W5-TC-NARRATIVE"})

(def span-rejected-cases
  {'w5-tooling-conformance-invalid-source-span-missing-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-extra-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-wrong-type-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-negative-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-order-request
   "W5-TC-SOURCE"})

(def provenance-binding-rejected-cases
  {'w5-tooling-conformance-invalid-top-provenance-cross-kind-request
   "W5-TC-PROVENANCE"
   'w5-tooling-conformance-invalid-nested-source-provenance-cross-kind-request
   "W5-TC-PROVENANCE"})
