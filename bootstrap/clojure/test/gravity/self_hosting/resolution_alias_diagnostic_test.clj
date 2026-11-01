(ns gravity.self-hosting.resolution-alias-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-c5-generated-diagnostic-test]
            [gravity.self-hosting.sh15-resolved-reader-diagnostic-test]))

;; Actual producer/Gravity execution selectors. Definitions only until admitted;
;; Source/data checks read these definitions without loading this namespace.
(def ^:private c15 #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/c15)
(def ^:private verify #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/verify)
(def ^:private unresolved-originals #'gravity.self-hosting.sh15-c5-generated-diagnostic-test/originals)
(defn- path [extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/rejected/resolution-alias-origin." extension))
(def ^:private originals
  (delay
    (into {}
          (for [extension ["gravity" "qst"]
                :let [p (path extension)]]
            [extension
             (try
               (bootstrap/compiler-c5-resolution-source-artifact
                (.getCanonicalPath (io/file p)) (slurp p :encoding "UTF-8")
                {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                (.getCanonicalPath (io/file bootstrap/sh03-reader-source-relative-path))
                {:reader-source-path bootstrap/sh03-reader-source-relative-path
                 :authenticated-envelope-options {:source :development-current}})
               {:unexpected-success true}
               (catch clojure.lang.ExceptionInfo error (ex-data error)))]))))
(defn- resolved [original]
  (let [fields (c15 'resolution-alias-diagnostic-fields [original])
        record (c15 'build-resolution-alias-diagnostic [original])
        request (:diagnostic-id-request record)]
    {:fields fields :request request
     :candidate (assoc record :diagnostic-id
                       (bootstrap/sh03-reader-canonical-digest
                        (get-in original [:source-span :source]) request))}))
(defn- refused [result rule]
  (is (= :rejected (:status result)))
  (is (= rule (get-in result [:diagnostics 0 :rule]))))

(deftest actual-paired-generated-alias-consumes-canonical-C15-fields
  (doseq [extension ["gravity" "qst"]]
    (let [original (get @originals extension) before (pr-str original)
          {:keys [fields request candidate]} (resolved original)
          generated (first (filter #(= :generated (:kind %)) (:origin-chain original)))]
      (is (= "C5-ALIAS" (:id original) (:rule candidate)))
      (is (= :unknown-namespace-alias (get-in original [:facts :reason])))
      (is (= :passed (:status (verify (path extension) fields candidate request))))
      (is (= (:source-span original) (get-in candidate [:primary :span])))
      (is (= (:syntax-id original) (get-in candidate [:primary :syntax-id])))
      (is (= (get-in original [:source-span :primary :file]) (get-in candidate [:primary :artifact])))
      (is (= (:origin-chain original) (:origin-chain candidate)))
      (is (= (:facts original) (:facts candidate) (get-in request [:preimage :facts])))
      (is (= [:generated-by :macro-call-site] (mapv :role (:related candidate))))
      (is (= (:definition-span generated) (get-in candidate [:related 0 :span])))
      (is (= (:call-site-span generated) (get-in candidate [:related 1 :span])))
      (is (= (get-in generated [:producer :definition-syntax-id]) (get-in candidate [:related 0 :artifact])))
      (is (= (get-in generated [:generated-span :call-site-syntax-id]) (get-in candidate [:related 1 :artifact])))
      (is (= :declare-namespace-alias (get-in candidate [:remediation 0 :kind])))
      (is (= {:qualifier 'missing.qualifier :namespace 'example.resolution-alias}
             (get-in candidate [:remediation 0 :binding])))
      (is (= (:remediation original) (get-in candidate [:remediation 0 :message])))
      (is (= original (get-in candidate [:remediation 0 :source-diagnostic])))
      (is (contains? candidate :redactions))
      (is (nil? (:redactions candidate)))
      (let [view (c15 'sh15-render-bounded-diagnostic-text [candidate])]
        (is (= :accepted (:status view)))
        (is (= :absent (:redaction-status view)))
        (is (re-find #"location=<redacted>" (:text view)))
        (is (some #{:cli-integration} (:nonclaims view))))
      (is (= before (pr-str original)))
      ;; The original adapter remains UNRESOLVED-only.
      (refused (c15 'sh15-build-c5-unresolved-diagnostic [original]) "C15-SCHEMA")))
  (let [original (get @@unresolved-originals "gravity")]
    (is (= "C5-UNRESOLVED" (:rule (c15 'sh15-build-c5-unresolved-diagnostic [original]))))
    (refused (c15 'build-resolution-alias-diagnostic [original]) "C15-SCHEMA")))

(deftest noncausal-or-malformed-alias-inputs-stay-unsupported
  (let [original (get @originals "gravity")]
    (doseq [bad [nil []
                 (assoc original :id "C5-UNRESOLVED")
                 (assoc original :symbol 'other/inspect)
                 (assoc-in original [:facts :rule-specific :candidate-count] 1)
                 (assoc-in original [:facts :rule-specific :candidate-count] 0.0)
                 (assoc original :candidate-bindings [:invented])
                 (-> original (assoc-in [:facts :reason] :duplicate-namespace-alias)
                     (assoc-in [:facts :rule-specific] {:reason :duplicate-namespace-alias :alias 'missing.qualifier}))
                 (assoc original :origin-chain [])
                 (assoc-in original [:origin-chain 1 :inputs] [])
                 (assoc-in original [:source-span :primary :source] "other.gravity")
                 (update-in original [:source-span :primary :byte-end] inc)
                 (assoc original :redactions [])]]
      (refused (c15 'build-resolution-alias-diagnostic [bad]) "C15-SCHEMA"))))

(deftest fresh-original-comparison-and-whole-request-refuse-substitution
  (let [original (get @originals "gravity") {:keys [fields request candidate]} (resolved original)
        reworded (resolved (assoc original :remediation "Other valid guidance"))]
    (is (= (:diagnostic-id candidate) (get-in reworded [:candidate :diagnostic-id])))
    (is (not= (:diagnostic-id candidate)
              (bootstrap/sh03-reader-canonical-digest (path "gravity") (:preimage request))))
    (doseq [changed [(assoc candidate :rule "C5-UNRESOLVED")
                     (assoc-in candidate [:primary :span :source] "other.qst")
                     (update-in candidate [:primary :span :primary :byte-end] inc)
                     (assoc candidate :origin-chain [])
                     (assoc-in candidate [:facts :rule-specific :candidate-count] 1)
                     (assoc candidate :redactions [])
                     (:candidate reworded)
                     (:candidate (resolved (get @originals "qst")))]]
      (refused (verify (path "gravity") fields changed request) "C15-ID"))
    (refused (verify (path "gravity") fields candidate (assoc request :domain :invented)) "C15-ID")
    (refused (c15 'verify-c15-resolved-diagnostic [fields candidate request "sha256:invalid"]) "C15-ID")))
