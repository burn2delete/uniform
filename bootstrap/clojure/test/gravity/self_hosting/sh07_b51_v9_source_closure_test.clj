(ns gravity.self-hosting.sh07-b51-v9-source-closure-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity]))

(def governed-components
  ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00581-p15-s23-stage2-compiler-artifact-binding.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"])

(deftest governed-consumers-have-no-direct-text-or-nio-byte-read
  (doseq [path governed-components]
    (let [source (slurp path)]
      (is (not (str/includes? source "(slurp ")) path)
      (is (not (str/includes? source "Files/readAllBytes")) path)
      (is (not (str/includes? source ".readAllBytes")) path))))

(deftest source-session-authority-is-private-and-global-delays-are-absent
  (let [bootstrap-source
        (apply str
               (map #(str (slurp %) "\n") governed-components))]
    (is (str/includes? bootstrap-source
                       "^:dynamic ^:private *p15-s23-sh07-b51-governed-source-session*"))
    (is (not (str/includes? bootstrap-source
                            "p15-s23-sh07-b51-authoritative-bundle-core-products")))
    (is (not (str/includes? bootstrap-source
                            "p15-s23-sh07-b51-authoritative-linked-products")))
    (is (str/includes? bootstrap-source
                       "p15-s23-sh07-b51-build-receipted-bundle-core-products"))))

(deftest source-object-transport-has-a-closed-concurrent-fail-closed-shape
  (let [source (slurp (last governed-components))]
    (is (str/includes? source
                       "p15-s23-sh07-b51-git-arguments-valid?"))
    (is (str/includes? source
                       "java.util.concurrent.ExecutorCompletionService"))
    (is (str/includes? source ":stdout-max-bytes 1073741824"))
    (is (str/includes? source ":stderr-max-bytes 1048576"))
    (is (str/includes? source ":deadline-ms 3600000"))
    (is (str/includes? source ".destroyForcibly process"))
    (is (not (str/includes? source ".redirectErrorStream true")))
    (is (not (str/includes? source
                            "(p15-s23-sh07-b51-read-stream-bytes stream)")))))

(deftest checker-identity-has-no-live-checkout-or-classpath-resource-fallback
  (let [source
        (slurp
         "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj")]
    (is (not (str/includes? source "Files/readAllBytes")))
    (is (not (str/includes? source "io/resource")))
    (is (str/includes? source "missing authenticated C-derived source input"))
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/checker-identity-manifest
                  "/a/live/worktree"
                  :producer-census)))))

(deftest process-entrypoints-bind-c-objects-before-project-loading
  (doseq [path
          ["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
           "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"]]
    (let [source (slurp path)]
      (is (str/includes? source "pre-manifest-tcb!"))
      (is (str/includes? source "validate-complete-classpath!"))
      (is (str/includes? source "diff-tree"))
      (is (str/includes? source "pre-discharge-reconstruction-unavailable"))
      (is (< (.indexOf source "pre-manifest-tcb!")
             (.lastIndexOf source "load-codec!"))))))

(deftest e2-source-session-proves-thirteen-inputs-and-forty-events
  (let [source
        (slurp
         "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj")]
    (is (str/includes? source
                       ":ordinal 12 :kind :sh06-resolution"))
    (is (str/includes? source
                       "bootstrap/gravity/src/gravity/resolution.gravity"))
    (is (str/includes? source
                       ":ordinal 39 :domain :sh06-resolution-binding"))
    (is (str/includes? source "(= 13 (count descriptors))"))
    (is (str/includes? source "(= 40 @(:cursor state) (count events))"))
    (is (str/includes? source
                       ":acquire-count 13 :decode-count 13"))
    (is (str/includes? source
                       ":event-count 40 :finalize-count 1"))))

(deftest e2-source-session-carries-complete-git-proofs-and-rederives-m0-m1
  (let [source
        (slurp
         "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj")]
    (is (str/includes? source
                       ":gravity/sh07-b51-git-tree-inclusion-proof-v1"))
    (is (str/includes? source
                       "p15-s23-sh07-b51-validate-content-object-proof!"))
    (is (str/includes? source
                       "p15-s23-sh07-b51-validate-source-object-proof!"))
    (is (str/includes? source ":content-object-proof content-object-proof"))
    (is (str/includes? source ":source-object-proof source-object-proof"))
    (is (str/includes? source ":module-source-inputs"))
    (is (str/includes? source
                       "p15-s23-sh07-b51-module-source-valid-with-input?"))
    (is (str/includes? source
                       "p15-s23-sh07-b51-derive-callsite-census-from-modules sources"))
    (is (str/includes? source ":m0-m1-rederivation"))
    (is (not (str/includes? source "requiring-resolve")))))
