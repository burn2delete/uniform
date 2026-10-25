(ns gravity.self-hosting.sh15-c5-generated-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-resolved-reader-diagnostic-test]))

;; Actual current C5 producer with explicit reader/AE selection, not renewed pins.
(def ^:private c15 #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/c15)
(def ^:private verify #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/verify)
(def ^:private invocation
  {:reader-source-path bootstrap/sh03-reader-source-relative-path
   :authenticated-envelope-options {:source :development-current}})
(defn- current-resolution [source-path source]
  (bootstrap/compiler-c5-resolution-source-artifact
   (.getCanonicalPath (io/file source-path)) source
   {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
   (.getCanonicalPath (io/file bootstrap/sh03-reader-source-relative-path))
   invocation))
(defn- path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/" family
       "/c5-generated-origin." extension))
(def ^:private originals
  (delay
    (into {}
          (for [extension ["gravity" "qst" "other"]]
            [extension
             (try
               (current-resolution
                (if (= "other" extension)
                  "bootstrap/clojure/fixtures/self-hosting/sh-15/rejected/other-c5-generated-origin.gravity"
                  (path "rejected" extension))
                (slurp (path "rejected" (if (= "other" extension) "gravity" extension))
                       :encoding "UTF-8"))
               (throw (ex-info "Expected actual C5 rejection" {}))
               (catch clojure.lang.ExceptionInfo error (ex-data error)))]))))
(defn- fields [original] (c15 'sh15-c5-unresolved-fields [original]))
(defn- resolved [original]
  (let [unresolved (c15 'sh15-build-c5-unresolved-diagnostic [original])
        request (:diagnostic-id-request unresolved)]
    {:unresolved unresolved :request request
     :candidate (assoc unresolved :diagnostic-id
                       (bootstrap/sh03-reader-canonical-digest
                        (get-in original [:source-span :source]) request))}))
(defn- rejected [result rule]
  (is (= :rejected (:status result)))
  (is (= rule (get-in result [:diagnostics 0 :rule]))))

(deftest actual-c5-accepted-controls-and-generated-rejections-construct-private-c15
  (doseq [extension ["gravity" "qst"]]
    (let [accepted (current-resolution
                    (path "accepted" extension)
                    (slurp (path "accepted" extension) :encoding "UTF-8"))
          original (get @originals extension) before (pr-str original)
          arguments (fields original) {:keys [unresolved request candidate]} (resolved original)
          [source generated] (:origin-chain original)]
      (is (= :accepted (:status accepted)))
      (is (= :passed (:status (bootstrap/sh06-resolution-artifact-verification
                              accepted :gravity.bootstrap/legacy invocation))))
      (is (= 2 (get-in accepted [:gravity-resolution-boundary
                                 :authenticated-envelope-descriptor :schema-version])))
      (is (not (contains? (get-in accepted [:gravity-resolution-boundary
                                           :authenticated-envelope-descriptor]) :bounds)))
      (is (= :development-unadmitted
             (get-in accepted [:gravity-resolution-boundary :authenticated-envelope
                               :source-rule :admission-status])))
      (is (= "C5-UNRESOLVED" (:id original) (:rule candidate)))
      (is (= :gravity/diagnostic (:artifact candidate)))
      (is (= :passed (:status (verify (path "rejected" extension) arguments candidate request))))
      (is (= {:digest-ref 0} (:diagnostic-id unresolved)))
      (is (= (:source-span original) (get-in candidate [:primary :span])))
      (is (= (get-in original [:source-span :primary :file])
             (get-in candidate [:primary :artifact]) (get-in source [:producer :source-id])))
      (is (not= (get-in original [:facts :c4-artifact-id]) (get-in candidate [:primary :artifact])))
      (is (= (:facts original) (:facts candidate)))
      (is (= (:origin-chain original) (:origin-chain candidate)))
      (is (= [:generated-by :macro-call-site] (mapv :role (:related candidate))))
      (is (= (:definition-span generated) (get-in candidate [:related 0 :span])))
      (is (= (:call-site-span generated) (get-in candidate [:related 1 :span])))
      (is (= :declare-or-import-binding (get-in candidate [:remediation 0 :kind])))
      (is (= {:symbol 'listish? :namespace 'example.sh15-c5-generated}
             (get-in candidate [:remediation 0 :binding])))
      (is (= original (get-in candidate [:remediation 0 :source-diagnostic])))
      (is (= (:facts original) (get-in request [:preimage :facts])))
      (is (not (contains? original :redactions)))
      (is (contains? candidate :redactions))
      (is (nil? (:redactions candidate)))
      (let [rendered (c15 'sh15-render-bounded-diagnostic-text [candidate])]
        (is (= :absent (:redaction-status rendered)))
        (is (re-find #"<redacted>" (:text rendered))))
      (is (= before (pr-str original))))))

(deftest c5-span-and-origin-shapes-reject-malformed-and-disconnected-data
  (let [original (get @originals "gravity") arguments (fields original)
        span (get-in arguments [3 :span]) flat (:primary span)
        source (get-in arguments [5 0]) generated (get-in arguments [5 1])]
    (doseq [bad [(dissoc flat :scalar-start) (assoc flat :extra true)
                 (assoc flat :file "sha256:bad") (assoc flat :source "")
                 (assoc flat :scalar-end (inc (:byte-end flat)))
                 (assoc flat :byte-end 0) (assoc flat :column-start 0)]]
      (rejected (c15 'build-c15-diagnostic
                     (assoc-in arguments [3 :span] (assoc span :primary bad :all [bad]))) "C15-SPAN"))
    (doseq [bad [(assoc span :all []) (assoc span :source "other.gravity")
                 (assoc span :extra true) (dissoc span :primary)]]
      (rejected (c15 'build-c15-diagnostic (assoc-in arguments [3 :span] bad)) "C15-SPAN"))
    (doseq [bad [(dissoc source :producer-version) (assoc source :extra true)
                 (assoc-in source [:producer :source-id] "sha256:bad")
                 (assoc-in source [:producer :identity] nil)
                 (assoc-in source [:producer :generated-form-id] :invented)
                 (assoc source :build-effects :missing)]]
      (rejected (c15 'build-c15-diagnostic (assoc-in arguments [5 0] bad)) "C15-ORIGIN"))
    (doseq [bad [(dissoc generated :inputs) (assoc generated :extra true)
                 (assoc generated :inputs [])
                 (assoc-in generated [:producer :version] "sha256:bad")
                 (assoc-in generated [:generated-span :producer] 'other/macro)
                 (assoc-in generated [:generated-span :ordinal] -1)
                 (assoc-in generated [:definition-span :byte-end] -1)]]
      (rejected (c15 'build-c15-diagnostic (assoc-in arguments [5 1] bad)) "C15-ORIGIN"))
    (doseq [bad [[] [generated source] [generated]]]
      (rejected (c15 'build-c15-diagnostic (assoc arguments 5 bad)) "C15-ORIGIN"))
    (doseq [bad [nil [] :invalid (assoc original :origin-chain :invalid)
                 (assoc original :redactions []) (assoc original :id "OTHER")]]
      (rejected (c15 'sh15-build-c5-unresolved-diagnostic [bad]) "C15-SCHEMA"))))

(deftest c5-real-digest-and-fresh-original-comparison-reject-substitution
  (let [original (get @originals "gravity") arguments (fields original)
        source-path (path "rejected" "gravity")
        {:keys [candidate request]} (resolved original)
        wording (assoc original :message "Other explanation" :remediation "Other guidance")
        reworded (resolved wording)
        semantic (resolved (assoc-in original [:facts :control] :changed))
        alias (resolved (get @originals "qst"))
        other (resolved (get @originals "other"))]
    (is (= (:diagnostic-id candidate) (get-in reworded [:candidate :diagnostic-id])))
    (is (not= (:diagnostic-id candidate) (get-in semantic [:candidate :diagnostic-id])))
    (is (= (:diagnostic-id candidate) (get-in alias [:candidate :diagnostic-id])))
    (is (not= (:diagnostic-id candidate) (get-in other [:candidate :diagnostic-id])))
    (is (= :passed (:status (verify source-path (fields wording)
                                    (:candidate reworded) (:request reworded)))))
    (is (not= (:diagnostic-id candidate)
              (bootstrap/sh03-reader-canonical-digest source-path (:preimage request))))
    (doseq [changed [(assoc candidate :redactions [])
                     (assoc-in candidate [:origin-chain 1 :reason] :other-reason)
                     (assoc-in candidate [:primary :span :source] "other.gravity")
                     (:candidate reworded) (:candidate semantic) (:candidate alias) (:candidate other)]]
      (rejected (verify source-path arguments changed (:diagnostic-id-request changed)) "C15-ID"))
    (rejected (verify source-path arguments (:candidate semantic) (:request semantic)) "C15-ID")))
