(ns gravity.self-hosting.sh14-data-layout-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh14_data_layout_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-14 test source is not on the classpath"
                {:id "SH14-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH14-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-14")

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
    "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "data-layouts" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "data-layouts" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-data-layouts" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-data-layouts" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh14-data-layout-leaf
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
  (invoke-engine 'sh14-build-layout [request]))

(def ^:private accepted-functions
  '[sh14-string-request
    sh14-bytes-request
    sh14-symbol-request
    sh14-keyword-request
    sh14-tuple-request
    sh14-record-request
    sh14-variant-request
    sh14-vector-request
    sh14-map-request
    sh14-set-request
    sh14-mutable-buffer-request
    sh14-maximum-layout-request])

(def ^:private rejected-cases
  {'sh14-missing-fact-request ["C12-FACTS" :invalid-ownership-fact]
   'sh14-duplicate-record-fields-request
   ["C12-SCHEMA" :duplicate-record-field]
   'sh14-duplicate-map-keys-request ["C12-SCHEMA" :duplicate-map-key]
   'sh14-duplicate-set-values-request ["C12-SCHEMA" :duplicate-set-value]
   'sh14-invalid-variant-tag-request
   ["C12-SCHEMA" :invalid-variant-schema]
   'sh14-excessive-capacity-request
   ["SAFE2-BOUNDS" :capacity-over-limit]
   'sh14-length-over-capacity-request
   ["SAFE2-BOUNDS" :length-over-capacity]
   'sh14-hidden-mutability-request ["L10-HIDDEN-ALLOC" :allocation-regime]
   'sh14-invalid-buffer-regime-request
   ["L10-HIDDEN-ALLOC" :allocation-regime]
   'sh14-wrong-size-request ["S7-LAYOUT" :unaligned-size-mismatch]
   'sh14-invalid-alignment-request
   ["S7-LAYOUT" :invalid-alignment]
   'sh14-invalid-byte-request ["C12-SCHEMA" :invalid-byte-vector]
   'sh14-negative-byte-request ["C12-SCHEMA" :invalid-byte-vector]
   'sh14-overlong-utf8-request ["C12-SCHEMA" :invalid-utf8]
   'sh14-surrogate-utf8-request ["C12-SCHEMA" :invalid-utf8]
   'sh14-truncated-utf8-request ["C12-SCHEMA" :invalid-utf8]
   'sh14-over-max-utf8-request ["C12-SCHEMA" :invalid-utf8]
   'sh14-empty-origin-request ["C12-SCHEMA" :invalid-origin-chain]
   'sh14-invalid-generated-origin-request
   ["C12-SCHEMA" :invalid-origin-chain]
   'sh14-invalid-source-span-request
   ["C12-SCHEMA" :invalid-source-span]
   'sh14-reversed-source-span-request
   ["C12-SCHEMA" :invalid-source-span]
   'sh14-malformed-fact-id-request ["C12-FACTS" :invalid-type-fact]
   'sh14-element-size-over-limit-request
   ["S7-LAYOUT" :element-size-over-limit]
   'sh14-total-size-over-limit-request
   ["S7-LAYOUT" :total-size-over-limit]
   'sh14-duplicate-variant-tags-request
   ["C12-SCHEMA" :invalid-variant-schema]
   'sh14-variant-arity-request
   ["C12-SCHEMA" :invalid-variant-schema]
   'sh14-nil-variant-tag-request
   ["C12-SCHEMA" :invalid-variant-schema]})

(deftest sh14-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh14-layout-policy sh14-build-layout sh14-verify-layout]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (let [policy (invoke-engine 'sh14-layout-policy [])]
    (is (= :gravity/sh14-layout-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= 4096 (:maximum-capacity policy)))
    (is (= 64 (:maximum-element-size policy)))
    (is (= 131072 (:maximum-size-bytes policy)))
    (is (= 64 (:integer-width-bits policy)))
    (is (= 9223372036854775807 (:maximum-integer policy)))
    (is (= #{1 2 4 8 16 32 64} (:alignments policy)))
    (is (= 11 (count (:kinds policy))))
    (is (contains? (:diagnostics policy) "SAFE2-BOUNDS"))
    (is (contains? (:diagnostics policy) "S7-LAYOUT"))
    (is (some #{:authenticated-sh12-mir-input} (:pending policy)))
    (is (some #{:actual-allocation} (:pending policy))))
  (doseq [[family basename]
          [["accepted" "data-layouts"]
           ["rejected" "invalid-data-layouts"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh14-builds-bounded-layouts-for-every-required-data-kind
  (doseq [function accepted-functions]
    (testing (str function)
      (let [gravity-request (request accepted-gravity-plan function)
            qst-request (request accepted-qst-plan function)
            gravity-layout (build gravity-request)
            qst-layout (build qst-request)]
        (is (= gravity-request qst-request))
        (is (= gravity-layout qst-layout))
        (is (= :accepted (:status gravity-layout)))
        (is (empty? (:diagnostics gravity-layout)))
        (is (= (:value-id gravity-request) (:value-id gravity-layout)))
        (is (= (:kind gravity-request) (:kind gravity-layout)))
        (is (= (:capacity gravity-request)
               (get-in gravity-layout [:layout :capacity])))
        (is (= (:length gravity-request)
               (get-in gravity-layout [:layout :length])))
        (is (= (:size-bytes gravity-request)
               (get-in gravity-layout [:layout :size-bytes])))
        (is (= (:allocation-regime gravity-request)
               (get-in gravity-layout [:layout :allocation-regime])))
        (is (= (:type-fact-id gravity-request)
               (get-in gravity-layout [:facts :type-fact-id])))
        (is (= (:effect-fact-id gravity-request)
               (get-in gravity-layout [:facts :effect-fact-id])))
        (is (= (:ownership-fact-id gravity-request)
               (get-in gravity-layout [:facts :ownership-fact-id])))
        (is (= (:safety-fact-id gravity-request)
               (get-in gravity-layout [:facts :safety-fact-id])))
        (is (= (:source-span gravity-request)
               (get-in gravity-layout [:provenance :source-span])))
        (is (= :passed
               (:status
                (invoke-engine
                 'sh14-verify-layout
                 [gravity-request gravity-layout]))))))))

(deftest sh14-preserves-explicit-shapes-and-allocation-decisions
  (let [string-layout
        (build (request accepted-gravity-plan 'sh14-string-request))
        record-layout
        (build (request accepted-gravity-plan 'sh14-record-request))
        variant-layout
        (build (request accepted-gravity-plan 'sh14-variant-request))
        map-layout
        (build (request accepted-gravity-plan 'sh14-map-request))
        buffer-layout
        (build
         (request accepted-gravity-plan 'sh14-mutable-buffer-request))
        maximum-layout
        (build
         (request accepted-gravity-plan 'sh14-maximum-layout-request))]
    (is (= :utf-8 (get-in string-layout [:identity-input :encoding])))
    (is (= [71 114 97 118 105 116 121 240 159 152 128]
           (get-in string-layout [:identity-input :payload])))
    (is (= [:name :arity]
           (get-in record-layout [:layout :field-names])))
    (is (= :some (get-in variant-layout [:layout :tag])))
    (is (= [:a :b] (get-in map-layout [:identity-input :keys])))
    (is (= :bounded
           (get-in buffer-layout [:layout :allocation-regime])))
    (is (true? (get-in buffer-layout [:layout :mutable])))
    (is (= 64 (get-in buffer-layout [:layout :capacity])))
    (is (= 64 (get-in buffer-layout [:layout :size-bytes])))
    (is (= 131072 (get-in maximum-layout [:layout :size-bytes])))))

(deftest sh14-keeps-actual-paths-outside-layout-identity
  (let [first-request
        (request accepted-gravity-plan 'sh14-record-request)
        second-request
        (request
         accepted-gravity-plan 'sh14-record-alternate-path-request)
        first-layout (build first-request)
        second-layout (build second-request)]
    (is (not= (get-in first-request [:source-span :actual-source-path])
              (get-in second-request [:source-span :actual-source-path])))
    (is (= (:identity-input first-layout)
           (:identity-input second-layout)))
    (is (= (:layout first-layout) (:layout second-layout)))
    (is (= (:facts first-layout) (:facts second-layout)))
    (is (not= (:provenance first-layout)
              (:provenance second-layout)))
    (is (not
         (str/includes?
          (pr-str (:identity-input first-layout)) "/checkout-a/")))
    (is (not
         (str/includes?
          (pr-str (:identity-input second-layout)) "/checkout-b/")))
    (is (str/includes?
         (pr-str (:provenance first-layout)) "/checkout-a/"))
    (is (str/includes?
         (pr-str (:provenance second-layout)) "/checkout-b/"))
    (is (= :passed
           (:status
            (invoke-engine
             'sh14-verify-layout [first-request first-layout]))))
    (is (= :passed
           (:status
            (invoke-engine
             'sh14-verify-layout [second-request second-layout]))))))

(deftest sh14-rejects-incomplete-and-unsafe-layouts-structurally
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-layout (build gravity-request)
            qst-layout (build qst-request)
            diagnostic (first (:diagnostics gravity-layout))]
        (is (= gravity-request qst-request))
        (is (= gravity-layout qst-layout))
        (is (= :rejected (:status gravity-layout)))
        (is (= 1 (count (:diagnostics gravity-layout))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :domain-ir-layout (:stage diagnostic)))
        (is (= (:value-id gravity-request) (:value-id diagnostic)))
        (is (= (:kind gravity-request) (:kind diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:generated-origin-chain diagnostic)))
        (is (= :compiler-data-layout (:domain diagnostic)))
        (is (= (:value-id gravity-request) (:artifact-id diagnostic)))
        (is (= :normalized-data-operation-descriptor
               (:semantic-anchor diagnostic)))
        (is (= "C12" (:owner-document diagnostic)))
        (is (=
             :gravity.compiler.c12-domain-ir-architecture/sh14-verify-layout
             (:verifier diagnostic)))
        (is (keyword? (:family diagnostic)))
        (if (= rule "C12-FACTS")
          (is (keyword? (:missing-fact diagnostic)))
          (is (nil? (:missing-fact diagnostic))))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh14-verifier-recomputes-and-fails-closed-on-substitution
  (let [request
        (request accepted-gravity-plan 'sh14-record-request)
        layout (build request)
        substituted
        (assoc-in layout [:layout :size-bytes] 8)
        verification
        (invoke-engine 'sh14-verify-layout [request substituted])]
    (is (= :accepted (:status layout)))
    (is (= :rejected (:status verification)))
    (is (= "C12-VERIFY"
           (get-in verification [:diagnostics 0 :rule])))
    (is (= "C12-VERIFY"
           (get-in verification [:diagnostics 0 :diagnostic-id])))
    (is (= :layout-result-substitution
           (get-in verification [:diagnostics 0 :reason])))))
