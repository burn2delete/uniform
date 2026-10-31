(ns gravity.reader.resource-connection-test
  "Inert HOST source orchestration with decoder/compiler/C2 DATA ports and
  the real lexical issuer/bridge over explicitly injected in-memory inputs.
  No Bootstrap load, actual Reader parser, Gravity plan, CLI or provider IO."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.build.declared-inputs :as inputs]
            [gravity.build.reader-bridge :as bridge]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]
            [gravity.reader.pure-text :as pure-text]))

(defn- forms [path]
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (io/reader path))]
      (loop [out []]
        (let [f (read {:eof ::end} r)]
          (if (= ::end f) out (recur (conj out f))))))))

(def ^:private root "bootstrap/clojure/src/gravity/bootstrap_impl/components/")
(def ^:private host-forms
  (forms (str root "self-hosting-sh03-sh07/00813-sh03-reader-canonical-identity.clj")))
(def ^:private definitions
  (into {} (for [f (concat host-forms
                          (forms (str root "self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj"))
                          (forms (str root "self-hosting-sh03-sh07/00812-sh03-reader-input-source-unit.clj")))
                 :when (and (seq? f) (contains? #{'defn 'defn-} (first f)))]
             [(second f) f])))

(defn- hash-value [value]
  (letfn [(normalize [v] (canonical/reader-canonical-value normalize v))]
    (canonical/reader-canonical-hash digest/sha256-hex normalize value)))

(deftest resource-options-compile-before-the-later-standard-options-definition
  ;; Actual load_order.edn installs this component before foundation/00826.
  ;; Exercise that order without importing Bootstrap or declaring every port.
  (let [n (create-ns (gensym "resource-options-load-order-"))
        forward (first (filter #(and (seq? %) (= 'declare (first %))
                                    (some #{'compiler-c2-reader-source-artifact} (rest %))) host-forms))]
    (try
      (binding [*ns* n]
        (clojure.core/refer 'clojure.core)
        (intern n 'reader-resource-policy-id (constantly "modeled-policy"))
        (eval forward)
        (eval (get definitions 'reader-resource-options?))
        (intern n 'standard-reader-options {:retain-comments true}))
      (let [select (ns-resolve n 'reader-resource-options?)]
        (is (true? (select {:retain-comments true :extension-policy "modeled-policy"})))
        (is (false? (select {:retain-comments true :extension-policy "other"}))))
      (finally (remove-ns (ns-name n))))))
(defn- hash-bytes [bytes]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes))))

(def ^:private source "#resource \"input\"")
(def ^:private project {:project-root-id (str "sha256:" (apply str (repeat 64 "a")))
                       :project-relative-path "source.qst"})
(def ^:private instruction {:op :builtin-call :function 'build-read-declared-input
                           :args [{:op :local :name 'input}]})
(def ^:private handler-plan
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:module 'gravity.reader :profile :meta :target :jvm
            :effects #{:build/read-file} :capabilities #{:fs/read}}
   :functions {'read-input {:instructions [instruction]}}})
(def ^:private handler {:plan handler-plan :instruction instruction :identity {:source :modeled-handler}})
(def ^:private parser-binding {:plan {:kind :modeled-pure-parser} :source-content-hash "modeled-source"
                              :plan-semantic-hash "modeled-plan"})
(def ^:private resource {:form-id "form/0" :payload-form-id "form/1"
                        :span {:byte-start 0 :byte-end 17} :raw {:modeled-raw true}
                        :input-codepoints [105 110 112 117 116]})

(defn- with-model [changes run]
  (let [n (create-ns (gensym "resource-connection-model-"))
        parse-calls (atom [])
        population (get changes :resources [resource])
        prepare (fn [unit bytes] (swap! parse-calls conj [unit bytes])
                  {:artifact :gravity/reader-resource-preparation :status :prepared
                   :source-unit unit :source-bytes bytes :resources population})
        lookup #(ns-resolve n %)
        ports {'reader-canonical-hash hash-value
               'macro-source-artifact (fn [& _] (throw (AssertionError. "Compiler port reached")))
               'stage0-function-table (fn [& _] (throw (AssertionError. "Compiler port reached")))
               'c-backend-stage2-plan-emitter-source-rule! (fn [& _] (throw (AssertionError. "Compiler port reached")))
               'p15-s23-stage2-compiler-artifact-augmented-emitter identity
               'p15-s23-stage2-seed-compile-function (fn [& _] (throw (AssertionError. "Compiler port reached")))
               'sh03-reader-plan-instruction-summary (fn [& _] (throw (AssertionError. "Compiler port reached")))
               'sh03-reader-function-keys #{:name :arity :params :instructions :binding}
               'sh03-reader-canonical-digest (fn [_ value] (hash-value value))
               'reader-explicit-project-context identity
               'gravity-source-extension (constantly ".qst")
               'sha256-bytes-hex hash-bytes
               'sh03-reader-byte-vector #(mapv (fn [b] (bit-and 255 b)) %)
               'reader-project-context-for-source (constantly project)
               'sh03-reader-current-binding! (fn [& _] parser-binding)
               'reader-resource-handler-binding! (constantly handler)
               'sh03-reader-source-relative-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
               'standard-reader-options {:retain-comments true :enabled-features #{} :extension-policy "standard"}
               'sh03-reader-strict-source-text! (fn [_ _ bytes] (String. bytes java.nio.charset.StandardCharsets/UTF_8))
               'sh03-reader-codepoints-text! (fn [_ cps] (String. (int-array cps) 0 (count cps)))
               'sh03-reader-require-result-carrier! (fn [_ _ value] (#'bridge/freeze value))
               'sh03-reader-boundary-fail! (fn [_ reason _ _] (throw (ex-info "modeled refusal" {:reason reason})))
               'sh03-reader-result-preflight! (fn [_ _ _ raw] (assert (= :accepted (:status raw))))
               'sh03-reader-verifier-preflight! (fn [_ _ report] (assert (= :accepted (:status report))))
               'sh03-reader-resolve-digest-requests! (fn [_ raw _] {:result raw :raw-result raw})
               'sh03-reader-adapt-products!
               (fn [_ _ _ options _ resolved]
                 {:kind :gravity/stage0-c2-reader-document-artifact :modeled true
                  :source-unit-record {:reader-options options} :raw (:raw-result resolved)
                  :gravity-reader-boundary
                  {:slice :SH-03 :owner :gravity-source :adapter-contract :gravity/sh03-to-c2-reader-products-v2
                   :plan-binding (dissoc parser-binding :plan) :authenticated-envelope-descriptor {}
                   :authenticated-envelope {:status :accepted :stage :c2-reader
                                            :sealed-artifact {:artifact-kind :gravity/sh03-reader-products}}}})
               'p15-s23-trusted-carrier-validation
               (fn [value _] (#'bridge/freeze value) {:status :passed})
               'sh03-reader-verification-invocation! (fn [& _] {:binding parser-binding})
               'sh03-reader-authenticated-envelope-options (fn [& _] nil)
               'reader-pure-source-artifact (fn [& _] (throw (AssertionError. "pure route reached")))
               'c2-reader-fail! (fn [_ _ _ facts] (throw (ex-info "modeled C2 refusal" facts)))
               'source-span (fn [& _] {})
               'p15-s23-stage2-sh02-descriptor-envelope-verify! (fn [& _] :passed)
               'p15-s23-sh02-stage-envelope-keys #{:status :stage :sealed-artifact}
               'sh03-reader-internal-product-authority (Object.)
               'p15-s23-stage2-runtime-execute-function-scoped
               (fn [runtime plan callee [seed] scope]
                 (bridge/assert-function-entry! runtime plan callee [seed] scope)
                 (assert (= 'read-input callee))
                 (assoc seed :datum (bridge/read! runtime plan instruction [(:datum seed)] scope)))
               'sh03-reader-execute-plan!
               (fn [_ _ function args]
                 (case function
                   reader-prepare-resources (apply prepare args)
                   reader-verify-resource-preparation
                   (let [[unit bytes candidate] args]
                     {:status (if (= (prepare unit bytes) candidate) :accepted :rejected)})
                   reader-complete-resources
                   (let [[unit bytes preparation actual] args]
                     {:artifact :modeled-raw-result :status :accepted :unit unit :bytes bytes
                      :preparation preparation :actual actual})
                   reader-verify-resource-result
                   (let [[unit bytes preparation actual raw] args]
                     {:status (if (= raw {:artifact :modeled-raw-result :status :accepted
                                             :unit unit :bytes bytes :preparation preparation :actual actual})
                                :accepted :rejected)})))}]
    (try
      (binding [*ns* n]
        (clojure.core/refer 'clojure.core)
        (alias 'io 'clojure.java.io)
        (alias 'reader-pure-text (ns-name (:ns (meta #'pure-text/admission-required?))))
        (eval (cons 'declare (concat (keys definitions) (keys ports)
                                    '[compiler-c2-reader-source-artifact sh03-c3-precomputed-c2-verify!])))
        (eval '(def ^:dynamic *reader-resource-invocation* nil))
        (doseq [name '[sh03-reader-input-source-unit reader-resource-policy reader-resource-policy-id
                      reader-resource-handler-binding!
                      reader-resource-options? reader-resource-context! reader-resource-prepare!
                      reader-resource-requests! reader-resource-invocations! reader-resource-resolved-result!
                      reader-declared-input-build! reader-resource-products! reader-resource-consume!
                      reader-resource-admission-required? sh03-c3-precomputed-c2-verify!]]
          (eval (get definitions name)))
        (doseq [[name value] (merge ports (:ports changes))] (intern n name value))
        (let [constructions (atom 0)]
          (intern n 'compiler-c2-reader-source-artifact
                  (fn [path text context _ _ _ _ options]
                    (let [artifact ((lookup 'reader-resource-products!) path text options context)]
                      (if (and (= 1 (swap! constructions inc)) (:candidate-change changes))
                        ((:candidate-change changes) artifact)
                        artifact))))))
      (let [implementation (hash-value {:reader (dissoc parser-binding :plan) :handler (:identity handler)})
            grant {:id "source-model" :principal "model-principal" :provider 'gravity.build/hermetic-inputs
                   :provider-version "1" :profile :meta :target :jvm :phase :build :lifetime :invocation
                   :scope #{"input"} :effects #{:build/read-file} :capabilities #{:fs/read}
                   :audit :required :replay :recorded :handler 'gravity.reader/read-input
                   :implementation implementation :policy-id ((lookup 'reader-resource-policy-id))}
            request {:source {:path "source.qst" :text source} :implementation implementation
                     :policy-id (:policy-id grant)}]
        (run lookup grant request parse-calls))
      (finally (remove-ns (ns-name n))))))

(deftest original-issuer-results-survive-fresh-construction-without-a-second-read
  (with-model {}
    (fn [lookup grant request parse-calls]
      (let [reads (atom 0) verifications (atom 0)
            read-input inputs/read-input! verify inputs/verify-original-result!]
        (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))
                      inputs/verify-original-result! (fn [& args] (swap! verifications inc) (apply verify args))]
          (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "actual injected return"}]
           (fn [capability]
             (let [result ((lookup 'reader-declared-input-build!) capability request)
                   invocation (get-in result [:raw :actual 0])]
               (is (= "input" (get-in invocation [:input-seed :datum])))
               (is (= "actual injected return" (get-in invocation [:returned-seed :datum])))
               (is (= 1 @reads))
               (is (= 7 @verifications))
               (is (>= (count @parse-calls) 8))
               (is (= (str "sha256:" (hash-bytes (.getBytes source java.nio.charset.StandardCharsets/UTF_8)))
                      (get-in invocation [:invocation :request :source-content-hash])))))))))))

(deftest whole-population-and-original-grant-refuse-before-any-read
  (doseq [changes [{:resources [(assoc resource :input-codepoints [111 116 104 101 114])]}
                   {:resources [resource (assoc resource :form-id "form/2" :input-codepoints [111 116 104 101 114])]}
                   {:ports {'sh03-reader-current-binding! (fn [& _] (assoc parser-binding :plan-semantic-hash "changed"))}}]]
    (with-model changes
      (fn [lookup grant request _]
        (let [reads (atom 0) read-input inputs/read-input!]
          (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))]
            (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "unused"}]
             (fn [capability]
               (is (thrown? clojure.lang.ExceptionInfo
                            ((lookup 'reader-declared-input-build!) capability request)))))
            (is (zero? @reads))))))))

(deftest a-supplied-source-hash-cannot-replace-the-original-byte-digest
  (with-model {:ports {'sh03-reader-input-source-unit
                      (fn [& _] {:source-content-hash (str "sha256:" (apply str (repeat 64 "b")))})}}
    (fn [lookup grant request _]
      (let [reads (atom 0) read-input inputs/read-input!]
        (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))]
          (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "unused"}]
           (fn [capability]
             (is (= :original-byte-resource-preparation
                    (try ((lookup 'reader-declared-input-build!) capability request) nil
                         (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))))
        (is (zero? @reads)))))))

(deftest fresh-consumer-reconstructs-return-and-request-joins-before-envelope-consumption
  (doseq [change [#(assoc-in % [:raw :actual 0 :returned-seed :datum] "forged")
                  #(assoc-in % [:raw :actual 0 :invocation :request :principal] "other")
                  #(assoc-in % [:raw :actual 0 :input-seed :datum] "other")
                  #(assoc-in % [:raw :actual 0 :form-id] "other")
                  #(assoc-in % [:raw :actual] [])]]
    (with-model {:candidate-change change}
      (fn [lookup grant request _]
        (let [reads (atom 0) read-input inputs/read-input!]
          (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))]
            (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "original"}]
             (fn [capability]
               (is (= :fresh-original-resource-artifact
                      (try ((lookup 'reader-declared-input-build!) capability request) nil
                           (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))))
          (is (= 1 @reads))))))))

(deftest empty-and-Unicode-returns-and-distinct-occurrences-use-original-receipts
  (doseq [text ["" (str (char 0x03bb))]]
    (with-model {:resources [resource (assoc resource :form-id "form/2" :payload-form-id "form/3"
                                            :span {:byte-start 18 :byte-end 35})]}
      (fn [lookup grant request _]
        (let [reads (atom 0) verifications (atom 0)
              read-input inputs/read-input! verify inputs/verify-original-result!]
          (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))
                        inputs/verify-original-result! (fn [& args] (swap! verifications inc) (apply verify args))]
            (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content text}]
             (fn [capability]
               (let [artifact ((lookup 'reader-declared-input-build!) capability request)]
                 (is (= [text text] (mapv #(get-in % [:returned-seed :datum]) (get-in artifact [:raw :actual])))))))
          (is (= 2 @reads))
          (is (= 14 @verifications))))))))

(deftest source-plan-drift-is-rejected-before-the-original-read
  (let [calls (atom 0)]
    (with-model {:ports {'reader-resource-handler-binding!
                        (fn [& _] (if (= 1 (swap! calls inc)) handler
                                      (assoc handler :identity {:source :changed-handler})))}}
      (fn [lookup grant request _]
        (let [reads (atom 0) read-input inputs/read-input!]
          (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))]
            (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "unused"}]
             (fn [capability]
               (is (= :unchanged-original-resource-plans
                      (try ((lookup 'reader-declared-input-build!) capability request) nil
                           (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))))
          (is (zero? @reads))))))))

(deftest original-capability-expires-and-data-cannot-select-a-consumer-scope
  (with-model {}
    (fn [lookup grant request _]
      (let [expired (atom nil) artifact (atom nil)]
        (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "original"}]
         (fn [capability]
           (reset! expired capability)
           (reset! artifact ((lookup 'reader-declared-input-build!) capability request))))
        (is (thrown? clojure.lang.ExceptionInfo
                     ((lookup 'reader-declared-input-build!) @expired request)))
        (is (thrown? clojure.lang.ExceptionInfo
                     ((lookup 'reader-resource-consume!) (Object.) "source.qst" @artifact {})))
        (is (thrown? clojure.lang.ExceptionInfo
                     ((lookup 'sh03-c3-precomputed-c2-verify!) nil "source.qst" @artifact {})))))))

(deftest a-handler-return-cannot-substitute-for-an-original-issuer-receipt
  (doseq [read? [false true]]
    (with-model {:ports {'p15-s23-stage2-runtime-execute-function-scoped
                        (fn [runtime plan _ [seed] scope]
                          (when read? (bridge/read! runtime plan instruction [(:datum seed)] scope))
                          (assoc seed :datum "substitute"))}}
      (fn [lookup grant request _]
        (let [reads (atom 0) read-input inputs/read-input!]
          (with-redefs [inputs/read-input! (fn [& args] (swap! reads inc) (apply read-input args))]
            (#'inputs/call-with-authorized-inputs grant [{:resource "input" :content "original"}]
             (fn [capability]
               (is (thrown? clojure.lang.ExceptionInfo
                            ((lookup 'reader-declared-input-build!) capability request)))))
            (is (= (if read? 1 0) @reads))))))))
