(ns platform-source-transition-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/requirement_reconciliation.clj"))
(require '[requirement-reconciliation :as rr]
         '[requirement-sources :as sources])

(def manifest
  (edn/read-string (slurp "contracts/requirement-reconciliation.edn")))
(def bridge
  (first (filter #(= "platform-product" (:document-id %))
                (:source-version-bridges manifest))))
(def document
  (first (filter #(= "platform-product" (:id %))
                (concat (:original-documents manifest) (:extension-sources manifest)))))
(def live-text (slurp (:path bridge)))
(def baseline-text
  (sources/git-source "." (:baseline-ref bridge) (:path bridge) rr/require!))
(def verified-options
  {:baseline-texts {"platform-product" baseline-text}
   :verified-baselines? #{"platform-product"}})

(defn manifest-with-bridge [m replacement]
  (update m :source-version-bridges
          (fn [bridges]
            (mapv #(if (= "platform-product" (:document-id %))
                     (assoc % :live-sha256 (rr/sha256 replacement))
                     %)
                  bridges))))

(deftest generated-platform-view-is-an-authenticated-current-transition
  (let [view (sources/view manifest document live-text rr/sha256 rr/require!
                           verified-options)]
    (is (= baseline-text (:text view)))
    (is (= :git-bytes-verified (get-in view [:bridge :snapshot-provenance])))
    (is (= :authenticated-source-baseline-view (get-in view [:bridge :span-basis])))))

(deftest generated-view-tampering-and-bridge-rewrite-fail-closed
  (let [tampered (str/replace live-text "platform-product/implement-os"
                              "platform-product/implement-unknown")
        missing-region (str/replace live-text "<!-- platform-product-projection:start -->"
                                    "<!-- platform-product-projection:removed -->")]
    (is (thrown? Exception
                 (sources/view manifest document tampered rr/sha256 rr/require!
                              verified-options)))
    (is (thrown? Exception
                 (sources/view (manifest-with-bridge manifest tampered)
                              document tampered rr/sha256 rr/require! verified-options)))
    (is (thrown? Exception
                 (sources/view manifest document missing-region rr/sha256 rr/require!
                              verified-options)))))

(run-tests)
