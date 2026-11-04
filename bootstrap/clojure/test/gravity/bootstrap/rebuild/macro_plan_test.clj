(ns gravity.bootstrap.rebuild.macro-plan-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.bootstrap.rebuild.macro-plan :as macro-plan])
  (:import [java.nio.file Files StandardCopyOption]))

(def ^:private macro-source
  "language/compiler/src/gravity/macro.gravity")

(def ^:private identity-keys
  [:source-path :source-byte-count :source-content-hash :source-mode
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])

(defn- temporary-root
  []
  (.toFile (Files/createTempDirectory
            "uniform-macro-plan-test-"
            (make-array java.nio.file.attribute.FileAttribute 0))))

(defn- delete-tree!
  [root]
  (doseq [path (reverse (file-seq root))]
    (when (.exists path)
      (Files/deleteIfExists (.toPath path)))))

(defn- current-payload
  []
  (let [component (macro-plan/development-macro-plan)
        binding (bootstrap/sh05-macro-build-binding! macro-source
                                                     :development-current)]
    {:schema (:schema component)
     :component-id (:component-id component)
     :source (:source component)
     :identities (select-keys binding identity-keys)
     :plan (:plan binding)}))

(defn- caught
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      error)))

(defn- private-function
  [name]
  (deref (ns-resolve 'gravity.bootstrap.rebuild.macro-plan name)))

(deftest persisted-plan-identities-reject-substitution
  (let [payload (current-payload)
        valid (macro-plan/validate-persisted-plan! payload)
        tampered (update-in payload [:plan :functions]
                            assoc
                            'tampered
                            {:arity 0 :params [] :instructions []})
        tampered-plan-id (assoc-in payload
                                   [:plan :plan-id]
                                   "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        wrong-shape (assoc-in payload
                              [:plan :functions 'sh05-expand-macro-template
                               :params]
                              ['wrong-request])
        wrong-source (assoc-in payload
                               [:identities :source-content-hash]
                               "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
    (is (= payload valid))
    (is (= :plan-identities
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       tampered)))))
        "a changed plan body cannot retain copied identity labels")
    (is (= :plan-id
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       tampered-plan-id)))))
        "a copied or substituted plan ID is rejected by canonical recomputation")
    (is (= :plan-shape
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       wrong-shape)))))
        "the entry ABI is checked before execution")
    (is (= :source-plan-binding
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       wrong-source)))))
        "the source identity is checked before plan execution")))

(deftest output-collision-does-not-overwrite
  (let [root (temporary-root)
        component (macro-plan/development-macro-plan)
        plan-file (io/file root (:output-plan-path component))]
    (try
      (io/make-parents plan-file)
      (spit plan-file "pre-existing-output")
      (let [error (caught #(macro-plan/run-development-macro-plan! "." root))]
        (is (= :output-exists (:reason (ex-data error))))
        (is (= "pre-existing-output" (slurp plan-file))))
      (finally
        (delete-tree! root)))))

(deftest child-reloads-plan-and-observes-template-and-rejection
  (let [root (temporary-root)]
    (try
      (let [result (macro-plan/run-development-macro-plan! "." root)
            record (:record result)
            accepted (:accepted record)
            rejected (:rejected record)]
        (is (= :development-current
               (get-in result [:component :source-mode])))
        (is (= :complete (get-in result [:producer-summary :status])))
        (is (= true (get-in record [:execution-child :loaded-plan?])))
        (is (= true (get-in record [:execution-child :fresh-c3-request?])))
        (is (= :clojure-stage0-seed
               (get-in record [:boundary :compiled-by])))
        (is (= :clojure-stage2-generic-rule-runner
               (get-in record [:boundary :executed-by])))
        (is (= :non-authoritative (get-in record [:boundary :authority])))
        (is (= [(list 'def 'add-one (list 'fn '[x] '(+ x 1)))]
               (:expanded-forms accepted)))
        (is (= (get-in result [:plan :plan-id])
               (get-in record [:execution-child :loaded-plan-id])))
        (is (= (get-in result [:output-sha256 :plan])
               (get-in record [:execution-child :loaded-plan-sha256])))
        (is (= (:compiler-revision result)
               (get-in record [:provenance :compiler-revision])))
        (is (= (:toolchain result)
               (get-in record [:provenance :toolchain])))
        (is (= true (get-in record [:provenance :live-worktree?])))
        (is (= :non-authoritative (get-in record [:provenance :authority])))
        (is (= (:source result) (get-in record [:provenance :source])))
        (is (= :stable (get-in record [:provenance :input-stability])))
        (is (= (:record-sha256 (:execution-summary result))
               (get-in result [:output-sha256 :record])))
        (is (= :gravity/sh05-macro-template-result (:artifact accepted)))
        (is (= 2 (count (:digest-requests accepted))))
        (is (= (get-in accepted [:expansion-template :expanded-syntax-recipe])
               (:expanded-syntax-recipe accepted)))
        (is (= 2 (:digest-request-count accepted)))
        (is (= :accepted (:status accepted)))
        (is (= 'gravity.core/defn (:macro accepted)))
        (is (= 290 (:source-byte-count accepted)))
        (is (= "sha256:b840a6da3225c88aca70cb9e3234d509a2729d312d876192856aafe5622e2ed2"
               (:source-content-hash accepted)))
        (is (true? (:utf8-snapshot? accepted)))
        (is (= [] (:diagnostics accepted)))
        (is (= :gravity/sh05-macro-template-result (:artifact rejected)))
        (is (nil? (:expansion-template rejected)))
        (is (= 1 (count (:digest-requests rejected))))
        (is (= 1 (:digest-request-count rejected)))
        (is (= "C4-RETURN" (:diagnostic rejected)))
        (is (= 565 (:source-byte-count rejected)))
        (is (= "sha256:b5e68ccf6f7acc74f6b5054d38896fb6a24c81a07ea0b4eaf139caa43409b9a8"
               (:source-content-hash rejected)))
        (is (true? (:utf8-snapshot? rejected)))
        (is (= 1 (count (:diagnostics rejected))))
        (is (= #{:artifact :rule :severity :lifecycle :stage :macro-name
                 :macro-version :definition-span :call-site-span
                 :generated-span :profile :target :build-effects :capabilities
                 :hygiene-context :origin-chain :facts :remediation
                 :diagnostic-id-request}
               (set (keys (first (:diagnostics rejected))))))
        (let [validate-record! (private-function 'validate-fixture-record!)
              validate-summary! (private-function 'validate-execution-summary!)
              record-sha256 (get-in result [:output-sha256 :record])
              plan-id (get-in result [:plan :plan-id])
              plan-sha256 (get-in result [:output-sha256 :plan])
              wrong-expansion (assoc-in record [:accepted :expanded-forms]
                                        [(list 'def 'not-add-one)])
              wrong-observation (assoc-in record
                                          [:execution-child :loaded-plan-id]
                                          :substituted-plan)
              macro-version (get-in result [:plan :identities
                                            :public-function-hashes
                                            'sh05-expand-macro-template])
              wrong-diagnostic (assoc-in record
                                         [:rejected :diagnostics 0 :severity]
                                         :warning)
              wrong-diagnostic-provenance
              (assoc-in record
                        [:rejected :diagnostics 0 :call-site-span :primary :source]
                        "forged-source.gravity")
              wrong-provenance (assoc-in record
                                         [:provenance :input-stability]
                                         :unknown)
              wrong-summary (assoc (:execution-summary result)
                                   :record-sha256
                                   "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
              expansion-error
              (caught #(validate-record! wrong-expansion "." (:source result)
                                         (:compiler-revision result)
                                         (:toolchain result) macro-version))
              diagnostic-error
              (caught #(validate-record! wrong-diagnostic "." (:source result)
                                         (:compiler-revision result)
                                         (:toolchain result) macro-version))
              diagnostic-provenance-error
              (caught #(validate-record! wrong-diagnostic-provenance "."
                                         (:source result)
                                         (:compiler-revision result)
                                         (:toolchain result) macro-version))
              provenance-error
              (caught #(validate-record! wrong-provenance "." (:source result)
                                         (:compiler-revision result)
                                         (:toolchain result) macro-version))
              observation-error
              (caught #(validate-summary! (:execution-summary result)
                                          wrong-observation plan-id plan-sha256
                                          record-sha256))
              summary-error
              (caught #(validate-summary! wrong-summary record plan-id
                                          plan-sha256 record-sha256))]
          (is (= :conformance (-> expansion-error ex-data :reason))
              "the accepted fixture must retain its exact expansion datum")
          (is (= :conformance (-> diagnostic-error ex-data :reason))
              "the rejected fixture must retain its full structured diagnostic")
          (is (= :conformance (-> diagnostic-provenance-error ex-data :reason))
              "the rejected fixture must retain authenticated source provenance")
          (is (= :conformance (-> provenance-error ex-data :reason))
              "the execution record must retain stable source/compiler provenance")
          (is (= :execution-summary (-> observation-error ex-data :reason))
              "the child observation must bind the loaded plan identity")
          (is (= :execution-summary (-> summary-error ex-data :reason))
              "the child summary must bind the read-back record hash"))
        (is (.isFile (io/file (:plan-path result))))
        (is (.isFile (io/file (:record-path result))))
        (is (seq (get-in accepted [:expanded-syntax-recipe :children]))))
      (finally
        (delete-tree! root)))))

(deftest failed-output-retention-records-owned-file
  (let [root (temporary-root)
        owned-file (io/file root "owned.edn")
        owned (atom #{})
        remember! (private-function 'remember-output!)
        retain! (private-function 'retain-owned-outputs!)]
    (try
      (io/make-parents owned-file)
      (spit owned-file "owned-by-this-run")
      (remember! owned owned-file)
      (let [receipt (retain! owned)]
        (is (.exists owned-file))
        (is (= :retain-published-outputs (:policy receipt)))
        (is (= (.getCanonicalPath owned-file)
               (:path (first (:outputs receipt))))))
      (finally
        (delete-tree! root)))))

(deftest failed-output-retention-preserves-replacement-after-ownership-snapshot
  (let [root (temporary-root)
        owned-file (io/file root "owned.edn")
        replacement-file (io/file root "replacement.edn")
        owned (atom #{})
        remember! (private-function 'remember-output!)
        retain! (private-function 'retain-owned-outputs!)]
    (try
      (io/make-parents owned-file)
      (spit owned-file "owned-by-this-run")
      (remember! owned owned-file)
      (spit replacement-file "replacement-after-write")
      (Files/move (.toPath replacement-file)
                  (.toPath owned-file)
                  (into-array StandardCopyOption
                              [StandardCopyOption/REPLACE_EXISTING]))
      (let [receipt (retain! owned)]
        (is (= "replacement-after-write" (slurp owned-file)))
        (is (= (.getCanonicalPath owned-file)
               (:path (first (:outputs receipt)))))
        (is (string? (:sha256 (first (:outputs receipt))))))
      (finally
        (delete-tree! root)))))

(deftest invalid-utf8-fixture-snapshot-is-rejected
  (let [utf8-snapshot! (private-function 'utf8-snapshot)
        invalid (byte-array [(unchecked-byte 0xC3)
                             (unchecked-byte 0x28)])
        error (caught #(utf8-snapshot! invalid :invalid-fixture))]
    (is (= :utf8-snapshot (-> error ex-data :reason)))))

(deftest source-compiler-input-drift-is-rejected
  (let [assert-stable! (private-function 'assert-stable-inputs!)
        before {:source {:path macro-source :byte-count 1 :sha256 "sha256:old"}
                :compiler-revision {:commit "old" :tree "old"}}
        error (with-redefs-fn
                {#'gravity.bootstrap.rebuild.macro-plan/source-identity!
                 (fn [_] {:path macro-source :byte-count 2 :sha256 "sha256:new"})
                 #'gravity.bootstrap.rebuild.macro-plan/compiler-revision!
                 (fn [_] {:commit "new" :tree "new"})}
                (fn [] (caught (fn [] (assert-stable! before "." :after-producer)))))]
    (is (= :provenance-drift (-> error ex-data :reason)))
    (is (= :after-producer (-> error ex-data :phase)))))
