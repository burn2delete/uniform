(ns gravity.tooling.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tooling :as tooling]
            [gravity.self-hosting.w5-tooling-conformance-fixture-support
             :as fixture-support]))

(def ^:private native-operations
  {:fail (fn [] bootstrap/compiled-tooling-fail!)
   :present (fn [] (var-get (ns-resolve 'gravity.bootstrap 'p13-present?)))})

(defn- native-failure [thunk]
  (try {:returned (thunk)}
       (catch clojure.lang.ExceptionInfo error {:message (ex-message error) :data (ex-data error)})))

(def ^:private native-checks
  [[tooling/native-validate-cli! bootstrap/validate-stage0-compiled-cli!
    "T1003" {:capability-prompts {:shown-denials []}}]
   [tooling/native-validate-repl! bootstrap/validate-stage0-compiled-repl!
    "T2002" {:capability-grants []}]
   [tooling/native-validate-formatter! bootstrap/validate-stage0-compiled-formatter!
    "T3002" {:reader-round-trip false}]
   [tooling/native-validate-linter! bootstrap/validate-stage0-compiled-linter!
    "T4003" {:autofix-safe? false}]
   [tooling/native-validate-lsp! bootstrap/validate-stage0-compiled-lsp!
    "T5001" {:diagnostics-match-cli false}]
   [tooling/native-validate-debugger! bootstrap/validate-stage0-compiled-debugger!
    "T6004" {:variable-reports [{:redacted true :accessed? true}]}]
   [tooling/native-validate-documentation! bootstrap/validate-stage0-compiled-documentation!
    "T7001" {:fresh? false}]
   [tooling/native-validate-dev-server! bootstrap/validate-stage0-compiled-dev-server!
    "T8003" {:hot-reload-decisions [{:decision :reuse}]}]
   [tooling/native-validate-registry-ux! bootstrap/validate-stage0-compiled-registry-ux!
    "T9001" {:update-diff {:capability-diff-visible false}}]
   [tooling/native-validate-ir-inspector! bootstrap/validate-stage0-compiled-ir-inspector!
    "T10002" {:source-span-maps []}]
   [tooling/native-validate-profiler! bootstrap/validate-stage0-compiled-profiler!
    "T11003" {:check-elision-report {:evidence []}}]
   [tooling/native-validate-safety-audit! bootstrap/validate-stage0-compiled-safety-audit!
    "T12001" {:unsafe-islands [:metadata-only] :unsafe-island-evidence? false}]
   [tooling/native-validate-ai-tooling! bootstrap/validate-stage0-compiled-ai-tooling!
    "T13002" {:patch-artifact {:validated false} :validation-report {:type :failed}}]])

(deftest native-metadata-contract-covers-all-public-operations
  (let [publics (ns-publics 'gravity.tooling)
        contract (:gravity/namespace-contract (meta #'tooling/native-validate-tooling!))]
    (is (= 51 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= :stage0-tooling-compiled-gate (:native-metadata-boundary contract)))
    (doseq [[name operation] publics]
      (is (= contract (:gravity/namespace-contract (meta operation))))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission]))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:performs-tool-services? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (true? (:trusted-native-operation-suppliers? contract)))))

(deftest native-catalog-derivation-retains-order-duplicates-and-case-mapping
  (is (= ["B2" "SHARED" "A1" "P13-MANIFEST" "P13-ACCEPTED"
          "P13-REJECTED" "P13-CONFORMANCE"]
         (tooling/native-diagnostic-ids {"A" ["A1" "SHARED"] "B" ["B2" "SHARED"]}
                                       ["B" "A" "B"])))
  (is (= ["P13-MANIFEST" "P13-ACCEPTED" "P13-REJECTED" "P13-CONFORMANCE"]
         (tooling/native-diagnostic-ids {} ["MISSING"])))
  (is (= {:mixed "MIXED" :p13-manifest "P13-MANIFEST"}
         (tooling/native-override-diagnostics ["MiXeD" "MIXED" "P13-MANIFEST"])))
  (is (= {} (tooling/native-override-diagnostics []))))

(deftest native-catalog-values-retain-compatibility-snapshots-and-diagnostic-associations
  (doseq [[stage caller] [[#'tooling/native-documents #'bootstrap/p13-tooling-documents]
                          [#'tooling/native-governing-documents #'bootstrap/p13-tooling-phase-governing-documents]
                          [#'tooling/native-diagnostics-by-document #'bootstrap/p13-tooling-diagnostics-by-document]
                          [#'tooling/native-rejected-diagnostics #'bootstrap/p13-tooling-rejected-diagnostics]
                          [#'tooling/native-rejected-fixture-names #'bootstrap/p13-tooling-rejected-fixture-names]
                          [#'tooling/native-artifact-keys #'bootstrap/p13-tooling-artifact-keys]
                          [#'tooling/native-document-summaries #'bootstrap/p13-tooling-document-summaries]]]
    (let [original (var-get caller)
          contract (:gravity/namespace-contract (meta stage))]
      (is (identical? original (var-get stage)))
      (is (= :catalog-data (get-in contract [:public-api (:name (meta stage)) :kind])))
      (with-redefs-fn {stage :later-stage-root}
        #(is (identical? original (var-get caller))))))
  (is (= "T1003" (tooling/native-rejected-diagnostics "T1")))
  (is (= "T13002" (tooling/native-rejected-diagnostics "T13")))
  (is (= :ir-inspector-bundle (get-in tooling/native-document-summaries ["T10" :owned-surface])))
  (is (= bootstrap/p13-tooling-diagnostic-ids
         (tooling/native-diagnostic-ids tooling/native-diagnostics-by-document tooling/native-documents)))
  (is (= bootstrap/p13-tooling-override-diagnostics
         (tooling/native-override-diagnostics bootstrap/p13-tooling-diagnostic-ids))))

(deftest native-record-refusals-match-the-existing-wrappers
  (let [module {:source-path "native-tooling.gravity" :profile :hosted :target :jvm}]
    (doseq [[direct wrapper id subject] native-checks]
      (let [subject (assoc subject :unsafe-island :retained :runtime-checked :retained
                          :ai/human-review :retained)
            stage (native-failure #(direct native-operations module subject))
            compatibility (native-failure #(wrapper module subject))]
        (is (= id (get-in stage [:data :id])))
        (is (= stage compatibility))))
    ;; These loose native predicates are existing compatibility behavior.
    (is (nil? (tooling/native-validate-linter! native-operations module nil)))
    (is (nil? (tooling/native-validate-documentation! native-operations module nil)))
    (is (nil? (tooling/native-validate-safety-audit! native-operations module nil)))
    (is (= {} (tooling/native-metadata-suite nil)))
    (is (false? (tooling/native-metadata-suite-present? nil)))))

(deftest native-presence-root-is-selected-before-field-evaluation
  (doseq [check [(fn [module subject] (tooling/native-validate-ir-inspector! native-operations module subject))
                 bootstrap/validate-stage0-compiled-ir-inspector!]]
    (let [presence (ns-resolve 'gravity.bootstrap 'p13-present?) calls (atom [])
          after (fn [_] (swap! calls conj :after) true)
          field (fn [key fallback]
                  (if (= :source-span-maps key)
                    (do (alter-var-root presence (constantly after)) [:native-span]) fallback))
          subject (reify clojure.lang.ILookup
                    (valAt [_ key] (field key nil))
                    (valAt [_ key fallback] (field key fallback)))]
      (with-redefs-fn {presence (fn [_] (swap! calls conj :before) false)
                      #'bootstrap/compiled-tooling-fail! (fn [& _] :refused)}
        #(is (= :refused (check {} subject))))
      (is (= [:before] @calls)))))

(deftest native-diagnostic-root-is-selected-before-source-path-evaluation
  (doseq [check [(fn [module] (tooling/native-metadata-fail!
                              (fn [] (var-get (ns-resolve 'gravity.bootstrap 'p13-tooling-fail!)))
                              "T13002" module {} {:stage :caller-stage}))
                 (fn [module] (bootstrap/compiled-tooling-fail! "T13002" module {} {:stage :caller-stage}))]]
    (let [failure (ns-resolve 'gravity.bootstrap 'p13-tooling-fail!) calls (atom [])
          after (fn [& _] (swap! calls conj :after) :after)
          field (fn [key fallback]
                  (if (= :source-path key)
                    (do (alter-var-root failure (constantly after)) "native-tooling.gravity") fallback))
          module (reify clojure.lang.ILookup
                   (valAt [_ key] (field key nil))
                   (valAt [_ key fallback] (field key fallback)))]
      (with-redefs-fn {failure (fn [_ path _ extra]
                                (swap! calls conj [:before path (:stage extra)]) :before)}
        #(is (= :before (check module))))
      (is (= [[:before "native-tooling.gravity" :caller-stage]] @calls)))))

(deftest native-dispatch-keeps-later-record-and-rule-roots-live
  (let [cli #'bootstrap/validate-stage0-compiled-cli!
        repl #'bootstrap/validate-stage0-compiled-repl! calls (atom [])
        later (fn [_ value] (swap! calls conj [:late (:record value)]))
        suite {:cli-command-sets [{:record :first} {:record :second}]
               :repl-sessions [{:record :third}]}
        operations {:suite-present? (fn [] bootstrap/stage0-compiled-tooling-suite-present?)
                    :suite (fn [] bootstrap/stage0-compiled-tooling-suite)
                    :cli (fn [] @cli) :repl (fn [] @repl)}]
    (with-redefs-fn {cli (fn [_ value]
                          (swap! calls conj [:first (:record value)])
                          (alter-var-root cli (constantly later))
                          (alter-var-root repl (constantly later)))
                    repl (fn [& _] (throw (ex-info "premature rule root" {})))}
      #(is (nil? (tooling/native-validate-tooling! operations {:metadata {:tooling {:compiled-gate suite}}}))))
    (is (= [[:first :first] [:late :second] [:late :third]] @calls)))
  (let [error (ex-info "original native failure" {:id "ORIGINAL"})
        result (with-redefs [bootstrap/compiled-tooling-fail! (fn [& _] (throw error))]
                 (try (tooling/native-validate-formatter! native-operations {} {:reader-round-trip false})
                      (catch Throwable observed observed)))]
    (is (identical? error result))))

(deftest complete-native-metadata-programs-emit-and-real-source-refusals-precede-execution
  (doseq [extension [".gravity" ".qst"]]
    (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/tooling/accepted/metadata-checks" extension)))
          source (slurp path)
          artifact (bootstrap/macro-source-artifact path source)
          module (assoc (:module artifact) :forms (:expanded-forms artifact))
          plan (bootstrap/stage0-compiled-core-plan path source module)]
      (is (= "tooling-metadata\n" (bootstrap/execute-stage0-compiled-plan plan)))
      (is (= (:plan-id plan) (str "sha256:" (bootstrap/sha256-hex (pr-str (dissoc plan :plan-id))))))
      (is (= (str "sha256:" (bootstrap/sha256-hex source)) (get-in plan [:source :sha256])))))
  (doseq [{:keys [fixture diagnostic]} bootstrap/stage0-compiled-tooling-rejected-fixtures]
    (let [executions (atom 0)
          result (with-redefs [bootstrap/execute-stage0-compiled-plan (fn [_] (swap! executions inc))]
                   (native-failure #(bootstrap/run-compiled-file fixture)))]
      (is (= diagnostic (get-in result [:data :id])))
      (is (zero? @executions)))))

(defn- invoke [function arguments]
  (fixture-support/invoke fixture-support/engine-plan function arguments))

(defn- original-request [extension]
  (fixture-support/request-at
   (if (= extension ".gravity") fixture-support/accepted-gravity-plan
       fixture-support/accepted-qst-plan)
   (fixture-support/fixture-path "accepted" "tooling-conformance" extension)
   extension (if (= extension ".gravity") :gravity :qst)))

(deftest explicit-contract-does-not-promote-coverage-labels
  (doseq [operation [#'tooling/coverage-record #'tooling/check-evidence
                     #'tooling/execute-evidence #'tooling/verify-evidence]]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.tooling (:namespace contract)))
      (is (= :none (get-in contract [:public-api (:name (meta operation)) :admission])))
      (is (= (:arglists (meta operation))
             (get-in contract [:public-api (:name (meta operation)) :arglists])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-certificates? contract)))
      (is (false? (:performs-tool-services? contract)))
      (is (= '[clojure.set clojure.string] (get-in contract [:dependency-direction :requires])))))
  (is (= #{'set 'str} (set (keys (ns-aliases 'gravity.tooling)))))
  (is (= '([artifact]) (:arglists (meta #'bootstrap/p13-tooling-proof))))
  (let [record (bootstrap/p13-tooling-proof {})]
    (is (= :complete (:status record)))
    (is (false? (:document-coverage-complete? record)))
    (is (false? (:cli-and-project-workflow-covered? record)))
    (is (= "W5-TC-SCHEMA"
           (get-in (tooling/check-evidence invoke record) [:diagnostics 0 :rule])))))

(deftest complete-source-execution-preserves-incomplete-gate-and-refusals
  (doseq [extension [".gravity" ".qst"]]
    (let [original (original-request extension)
          bound (tooling/execute-evidence invoke original)
          result (:result bound)
          rejected-plan (if (= extension ".gravity")
                         fixture-support/rejected-gravity-plan
                         fixture-support/rejected-qst-plan)]
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion-status result)))
      (is (= :blocked (get-in result [:verifier-gate :decision])))
      (is (false? (get-in result [:verifier-gate :release-eligible?])))
      (is (= :non-authority (:authority result)))
      (is (= :passed (get-in bound [:verification :status])))
      (is (= result (tooling/check-evidence invoke original)))
      (doseq [[function rule] [['w5-tooling-conformance-invalid-source-request "W5-TC-SOURCE"]
                              ['w5-tooling-conformance-invalid-narrative-request "W5-TC-NARRATIVE"]
                              ['w5-tooling-conformance-invalid-source-span-missing-request "W5-TC-SOURCE"]]]
        (let [request (fixture-support/invoke rejected-plan function [original])
              rejected (tooling/execute-evidence invoke request)]
          (is (= :rejected (get-in rejected [:result :status])))
          (is (= rule (get-in rejected [:result :diagnostics 0 :rule])))
          (is (= :passed (get-in rejected [:verification :status]))))))))

(deftest replay-keeps-the-original-request-and-rejects-substitution
  (let [original (original-request ".gravity")
        result (tooling/check-evidence invoke original)
        forged (assoc result :completion-status :complete)
        refusal (tooling/verify-evidence invoke original forged)
        alternate (fixture-support/request-at fixture-support/accepted-gravity-plan
                   "fixture/unbound-substitution.gravity" ".gravity" :gravity)
        alternate-result (tooling/check-evidence invoke alternate)]
    (is (= :rejected (:status refusal)))
    (is (= "W5-TC-SUBSTITUTION" (get-in refusal [:diagnostics 0 :rule])))
    (is (= :non-authority (:authority refusal)))
    (is (= :accepted (:status alternate-result)))
    (is (= (:identity-input result) (:identity-input alternate-result)))
    (is (not= (:provenance result) (:provenance alternate-result)))
    (is (= :rejected (:status (tooling/verify-evidence invoke original alternate-result))))))

(deftest failed-fresh-verification-withholds-the-private-result
  (let [original (original-request ".gravity") calls (atom [])
        alternate (fixture-support/request-at fixture-support/accepted-gravity-plan
                   "fixture/unbound-substitution.gravity" ".gravity" :gravity)
        candidate (tooling/check-evidence invoke alternate)
        refusal (tooling/verify-evidence invoke original candidate)
        operation (fn [function arguments]
                    (swap! calls conj [function arguments])
                    (if (= function 'w5-tooling-conformance-verify)
                      candidate
                      (invoke function arguments)))
        result (tooling/execute-evidence operation original)]
    (is (= refusal result))
    (is (= :rejected (:status result)))
    (is (= "W5-TC-SUBSTITUTION" (get-in result [:diagnostics 0 :rule])))
    (is (not (contains? result :result)))
    (is (= ['w5-tooling-conformance-verify 'w5-tooling-conformance-verify-result]
           (mapv first @calls)))
    (is (identical? original (get-in @calls [0 1 0])))
    (is (identical? original (get-in @calls [1 1 0])))))

(def ^:private document-ids [:fixture/before])
(def ^:private diagnostic-ids [])
(defn- present [_value] true)
(defn- task-statuses [] {:fixture :supplied-data})

(defn- observed-artifact [data observe]
  (reify clojure.lang.ILookup
    (valAt [_ key] (observe key) (get data key))
    (valAt [_ key not-found] (observe key) (get data key not-found))))

(deftest document-vars-are-read-at-the-original-body-position
  (with-redefs [document-ids [:fixture/before]]
    (let [artifact
          (observed-artifact
           {:document-contracts {:fixture/after :unbound}}
           (fn [key]
             (when (= key :document-contracts)
               (alter-var-root #'document-ids (constantly [:fixture/after])))))
          result (tooling/coverage-record #'document-ids #'diagnostic-ids #'present #'task-statuses artifact)]
      (is (true? (:document-coverage-complete? result)))
      (is (= {:fixture :supplied-data} (:task-statuses result))))))

(deftest callback-root-is-read-before-its-argument-lookup
  (let [calls (atom [])]
    (with-redefs [present (fn [_value] (swap! calls conj :old) true)]
      (let [artifact
            (observed-artifact
             {:registry-ux-record {:update-diff {:capability-diff-visible true}}
              :ir-inspector-bundle {:source-span-maps [:fixture/unbound]}
              :profiler-report {:check-elision-report {:evidence [:fixture/unbound]}}
              :safety-audit-report {:proof-index [:fixture/unbound]}}
             (fn [key]
               (when (= key :ir-inspector-bundle)
                 (alter-var-root #'present
                                 (constantly (fn [_value] (swap! calls conj :new) true))))))
            result (tooling/coverage-record #'document-ids #'diagnostic-ids #'present #'task-statuses artifact)]
        (is (true? (:dev-registry-and-inspectors-covered? result)))
        (is (= [:old :new :new] @calls))))))

(deftest missing-line-fixture-retains-every-other-map-entry
  (doseq [plan [fixture-support/rejected-gravity-plan
                fixture-support/rejected-qst-plan]]
    (let [span {:source-id :fixture/unbound :line 1 :column 1 :start-byte 0 :end-byte 8
                :extra {:ai/human-review :required} :nil-value nil}
      result (fixture-support/invoke plan 'tc-span-without-line [span])]
      (is (= (dissoc span :line) result))
      (is (contains? result :nil-value))
      (is (= :required (get-in result [:extra :ai/human-review]))))))

(defn- native-record-operations []
  (into {}
        (for [[key name] '{:document-number p13-document-number :task-id p13-task-id
                          :documents p13-tooling-documents :governing-documents p13-tooling-phase-governing-documents
                          :diagnostics-by-document p13-tooling-diagnostics-by-document
                          :fixture-names p13-tooling-rejected-fixture-names :rejected-diagnostics p13-tooling-rejected-diagnostics
                          :diagnostic-ids p13-tooling-diagnostic-ids :override-diagnostics p13-tooling-override-diagnostics
                          :artifact-keys p13-tooling-artifact-keys :summaries p13-tooling-document-summaries
                          :diagnostic-document p13-tooling-diagnostic-document :fail p13-tooling-fail!
                          :document-record p13-tooling-document-record :present p13-present?
                          :artifact-values p13-tooling-artifact-values :compiled-rejected-fixtures stage0-compiled-tooling-rejected-fixtures
                          :transport-fail fail! :source-span source-span :sha256 sha256-hex}
              :let [operation (ns-resolve 'gravity.bootstrap name)]]
          (do (assert operation name) [key (fn [] (var-get operation))]))))

(deftest native-record-contract-and-presence-keep-their-limited-boundary
  (let [contract (:gravity/namespace-contract (meta #'tooling/native-validate-artifact!))]
    (is (= :p13-native-records-and-validation (:native-record-boundary contract)))
    (is (= 21 (count (filter #(contains? (val %) :operation-suppliers) (:public-api contract)))))
    (is (= '[clojure.set clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:performs-tool-services? contract))))
  (doseq [value [nil false 0 "" [] [nil] {} {:a nil} #{} #{nil}]]
    (is (= (bootstrap/p13-present? value) (tooling/native-present? value))))
  (is (= {} (tooling/native-source-overrides nil))))

(deftest native-factory-merge-retains-captured-functions-order-and-original-exceptions
  (let [calls (atom []) value (Object.)
        core (fn [] (swap! calls conj :core) {:left value :shared :core})
        extended (fn [] (swap! calls conj :extended) {:right value :shared :extended})
        result (tooling/native-artifact-values core extended)]
    (is (= [:core :extended] @calls))
    (is (= :extended (:shared result)))
    (is (identical? value (:left result)))
    (is (identical? value (:right result))))
  (let [error (ex-info "original record factory failure" {}) calls (atom [])
        result (try (tooling/native-artifact-values (fn [] (throw error)) (fn [] (swap! calls conj :extended)))
                    (catch Throwable observed observed))]
    (is (identical? error result))
    (is (empty? @calls)))
  (is (= (bootstrap/p13-tooling-artifact-values)
         (tooling/native-artifact-values tooling/native-core-records
                                       #(tooling/native-extended-records (native-record-operations))))))

(deftest native-document-view-selects-the-collection-once-and-later-record-roots-at-each-call
  (doseq [invoke [(fn [] (tooling/native-document-records (native-record-operations)))
                 bootstrap/p13-tooling-document-records]]
    (let [documents #'bootstrap/p13-tooling-documents record #'bootstrap/p13-tooling-document-record
          calls (atom []) later (fn [document] (swap! calls conj [:later document]) :later)
          result (with-redefs-fn {documents ["T1" "T2"]
                                 record (fn [document]
                                          (swap! calls conj [:first document])
                                          (alter-var-root record (constantly later))
                                          (alter-var-root documents (constantly ["T3"])) :first)} invoke)]
      (is (= {"T1" :first "T2" :later} result))
      (is (= [[:first "T1"] [:later "T2"]] @calls)))))

(deftest native-p13-failure-selects-transport-before-payload-and-span-after-field-lookup
  (doseq [invoke [(fn [subject] (tooling/native-fail! (native-record-operations) "T2002"
                                                     "native-records.gravity" subject
                                                     {:stage :caller-stage :source-span :caller-span :document-id :caller-document}))
                 (fn [subject] (bootstrap/p13-tooling-fail! "T2002" "native-records.gravity" subject
                                                           {:stage :caller-stage :source-span :caller-span :document-id :caller-document}))]]
    (let [failure (ns-resolve 'gravity.bootstrap 'fail!) span (ns-resolve 'gravity.bootstrap 'source-span)
          calls (atom [])
          subject (reify clojure.lang.ILookup
                    (valAt [_ key]
                      (case key
                        :document-id "T2"
                        :source-span (do (alter-var-root failure (constantly (fn [& _] :after)))
                                         (alter-var-root span (constantly (fn [path index]
                                                                          (swap! calls conj [:span path index]) :after-span))) nil)
                        nil))
                    (valAt [this key fallback] (or (.valAt ^clojure.lang.ILookup this key) fallback)))
          result (with-redefs-fn {failure (fn [id _ data]
                                           (swap! calls conj [:before id (:stage data) (:source-span data) (:document-id data)]) :before)
                                 span (fn [& _] (throw (ex-info "premature span root" {})))}
                   #(invoke subject))]
      (is (= :before result))
      (is (= [[:span "native-records.gravity" 0] [:before "T2002" :caller-stage :caller-span :caller-document]] @calls)))))

(deftest native-plan-digest-root-is-selected-before-report-serialization
  (doseq [invoke [(fn [] (tooling/native-plan-report (native-record-operations) {:plan-id "native-plan"} {:profile :hosted :target :jvm}))
                 (fn [] (bootstrap/stage0-compiled-plan-tooling-report {:plan-id "native-plan"} {:profile :hosted :target :jvm}))]]
    (let [digest (ns-resolve 'gravity.bootstrap 'sha256-hex) calls (atom [])
          result (with-redefs-fn {digest (fn [text] (swap! calls conj [:before text]) "before")
                                 #'clojure.core/pr-str (fn [_]
                                                        (alter-var-root digest (constantly (fn [_] "after")))
                                                        (swap! calls conj :serialized) "serialized-native-report")}
                   invoke)]
      (is (= "sha256:before" (:report-id result)))
      (is (= [:serialized [:before "serialized-native-report"]] @calls)))))

(deftest complete-paired-native-record-programs-emit-stable-reports-and-real-source-refusals
  (doseq [extension [".gravity" ".qst"]]
    (let [experience-path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/tooling/accepted/experience-records" extension)))
          artifact (bootstrap/tooling-experience-file-artifact experience-path)
          path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/tooling/accepted/metadata-checks" extension)))
          text (slurp path) expanded (bootstrap/macro-source-artifact path text)
          module (assoc (:module expanded) :forms (:expanded-forms expanded))
          plan (bootstrap/stage0-compiled-core-plan path text module)
          report (tooling/native-plan-report (native-record-operations) plan module)]
      (is (= :complete (tooling/native-validate-artifact! (native-record-operations) experience-path artifact)))
      (is (= (:artifact-id artifact) (bootstrap/c4-artifact-id (dissoc artifact :artifact-id))))
      (is (= "tooling-metadata\n" (bootstrap/execute-stage0-compiled-plan plan)))
      (is (= (:plan-id plan) (:compiled-plan-id report)))
      (is (= (:report-id report) (str "sha256:" (bootstrap/sha256-hex (pr-str (dissoc report :report-id))))))
      (is (= (bootstrap/stage0-compiled-plan-tooling-report plan module) report))))
  (doseq [document bootstrap/p13-tooling-documents]
    (let [path (str "bootstrap/clojure/fixtures/rejected/" (bootstrap/p13-tooling-rejected-fixture-names document))
          built (atom 0)
          result (with-redefs [bootstrap/p13-tooling-artifact-values (fn [] (swap! built inc) {})]
                   (native-failure #(bootstrap/tooling-experience-file-artifact path)))]
      (is (= (bootstrap/p13-tooling-rejected-diagnostics document) (get-in result [:data :id])))
      (is (zero? @built)))))

(deftest native-artifact-validation-keeps-the-first-refusal-and-wrapper-payload
  (let [path "bootstrap/clojure/fixtures/tooling/accepted/experience-records.gravity"
        artifact (bootstrap/tooling-experience-file-artifact path)
        altered [(-> artifact (dissoc :document-set) (dissoc :document-contracts))
                 (assoc-in artifact [:formatter-fixture :status] :incomplete)
                 (update-in artifact [:document-contracts "T1"] dissoc :governing-doc)
                 (assoc artifact :accepted-tooling-fixtures [])
                 (assoc artifact :rejected-tooling-fixtures [])
                 (assoc artifact :tooling-conformance-evidence [])
                 (assoc-in artifact [:cli-command-set :commands] [])]
        expected ["P13-MANIFEST" "P13-MANIFEST" "P13-MANIFEST" "P13-ACCEPTED" "P13-REJECTED" "P13-CONFORMANCE" "T1006"]]
    (doseq [[value id] (map vector altered expected)]
      (let [direct (native-failure #(tooling/native-validate-artifact! (native-record-operations) path value))
            wrapper (native-failure #(bootstrap/p13-tooling-validate! path value))]
        (is (= id (get-in direct [:data :id])))
        (is (= wrapper direct))))))

(defn- native-assembly-operations []
  (merge (native-record-operations)
         {:artifact-id (fn [] bootstrap/c4-artifact-id)
          :accepted-fixtures (fn [] bootstrap/p13-accepted-tooling-fixtures)
          :rejected-fixtures (fn [] bootstrap/p13-rejected-tooling-fixtures)
          :task-statuses (fn [] bootstrap/p13-task-statuses)
          :diagnostic-stream (fn [] bootstrap/p13-tooling-diagnostic-stream)
          :document-records (fn [] bootstrap/p13-tooling-document-records)
          :conformance-evidence (fn [] bootstrap/p13-tooling-conformance-evidence)
          :coverage-record (fn [] bootstrap/p13-tooling-proof)
          :validate-artifact (fn [] bootstrap/p13-tooling-validate!)
          :plan-report (fn [] bootstrap/stage0-compiled-plan-tooling-report)}))

(defn- native-experience-request [extension]
  (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/tooling/accepted/experience-records" extension)))
        text (slurp path) forms (mapv :form (bootstrap/read-source-form-records path text))]
    {:path path :text text :module (bootstrap/parse-module path forms)}))

(defn- native-compiled-request [extension]
  (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/tooling/accepted/metadata-checks" extension)))
        text (slurp path) expanded (bootstrap/macro-source-artifact path text)
        module (assoc (:module expanded) :forms (:expanded-forms expanded))
        plan (bootstrap/stage0-compiled-core-plan path text module)]
    {:path path :text text :module module :plan plan :output (bootstrap/execute-stage0-compiled-plan plan)}))

(deftest native-construction-contract-keeps-admission-and-execution-outside
  (let [contract (:gravity/namespace-contract (meta #'tooling/native-experience-artifact))]
    (is (= :p13-native-artifact-construction (:native-assembly-boundary contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:performs-tool-services? contract)))
    (is (= :none (get-in contract [:public-api 'native-compiled-artifact :admission])))))

(deftest native-construction-matches-real-paired-callers-and-recomputed-identities
  (doseq [extension [".gravity" ".qst"]]
    (let [{:keys [path text module]} (native-experience-request extension)
          artifact (tooling/native-experience-artifact (native-assembly-operations) path module {})]
      (is (= (bootstrap/tooling-experience-source-artifact path text) artifact))
      (is (= (:artifact-id artifact) (bootstrap/c4-artifact-id (dissoc artifact :artifact-id)))))
    (let [{:keys [path text module plan output]} (native-compiled-request extension)
          artifact (tooling/native-compiled-artifact (native-assembly-operations) path text module plan output)]
      (is (= "tooling-metadata\n" output))
      (is (= (bootstrap/hosted-core-compiled-tooling-proof-source-artifact path text) artifact))
      (is (= (:plan-id plan) (get-in artifact [:compiled-plan :plan-id])))
      (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in artifact [:source :sha256])))
      (is (= (:artifact-id artifact) (str "sha256:" (bootstrap/sha256-hex (pr-str (dissoc artifact :artifact-id))))))
      ;; The legacy label remains data even when the stdout comparison is false.
      (is (false? (get-in artifact [:capability-based-proof :compiled-plan-executed?]))))))

(deftest experience-construction-reads-the-final-identity-root-after-earlier-callbacks
  (let [{:keys [path module]} (native-experience-request ".gravity")
        identity-var #'bootstrap/c4-artifact-id
        stream bootstrap/p13-tooling-diagnostic-stream calls (atom [])
        result (with-redefs-fn
                 {identity-var (fn [_] (swap! calls conj :before) "before")
                  #'bootstrap/p13-tooling-diagnostic-stream
                  (fn [source input]
                    (alter-var-root identity-var (constantly (fn [_] (swap! calls conj :after) "after")))
                    (stream source input))}
                 #(tooling/native-experience-artifact (native-assembly-operations) path module {}))]
    (is (= [:before :after] @calls))
    (is (= "before" (get-in result [:tooling-diagnostic-stream :input-artifact])))
    (is (= "after" (:artifact-id result)))))

(deftest experience-refusal-precedes-coverage-and-final-identity-and-keeps-exception-identity
  (let [{:keys [path module]} (native-experience-request ".gravity")
        error (ex-info "original artifact validation refusal" {:id "ORIGINAL"}) calls (atom [])
        result (with-redefs [bootstrap/c4-artifact-id (fn [_] (swap! calls conj :input-id) "input")
                             bootstrap/p13-tooling-validate! (fn [& _] (swap! calls conj :validation) (throw error))
                             bootstrap/p13-tooling-proof (fn [_] (swap! calls conj :coverage))]
                 (try (tooling/native-experience-artifact (native-assembly-operations) path module {})
                      (catch Throwable observed observed)))]
    (is (identical? error result))
    (is (= [:input-id :validation] @calls)))
  (let [{:keys [path module]} (native-experience-request ".gravity") calls (atom [])
        result (with-redefs [bootstrap/p13-tooling-validate! (fn [& _] (swap! calls conj :validation) false)
                             bootstrap/p13-tooling-proof (fn [_] (swap! calls conj :coverage) {:supplied :data})]
                 (tooling/native-experience-artifact (native-assembly-operations) path module {}))]
    (is (= [:validation :coverage] @calls))
    (is (= {:supplied :data} (:capability-based-proof result)))))

(deftest compiled-construction-keeps-report-failure-and-digest-before-serialization
  (let [{:keys [path text module plan output]} (native-compiled-request ".gravity")
        error (ex-info "original native report failure" {}) calls (atom [])
        result (with-redefs [bootstrap/stage0-compiled-plan-tooling-report (fn [& _] (throw error))
                             bootstrap/sha256-hex (fn [_] (swap! calls conj :digest))]
                 (try (tooling/native-compiled-artifact (native-assembly-operations) path text module plan output)
                      (catch Throwable observed observed)))]
    (is (identical? error result))
    (is (empty? @calls)))
  (let [{:keys [path text module plan output]} (native-compiled-request ".gravity")
        report (bootstrap/stage0-compiled-plan-tooling-report plan module)
        digest #'bootstrap/sha256-hex calls (atom [])
        result (with-redefs-fn
                 {#'bootstrap/stage0-compiled-plan-tooling-report (fn [& _] report)
                  digest (fn [value] (swap! calls conj [:before value]) "before")
                  #'clojure.core/pr-str (fn [_]
                                         (alter-var-root digest (constantly (fn [_] "after")))
                                         (swap! calls conj :serialized) "serialized-native-artifact")}
                 #(tooling/native-compiled-artifact (native-assembly-operations) path text module plan output))]
    (is (= "sha256:before" (get-in result [:source :sha256])))
    (is (= "sha256:before" (:artifact-id result)))
    (is (= [[:before text] :serialized [:before "serialized-native-artifact"]] @calls))))

(deftest real-source-refusals-precede-both-native-construction-handoffs
  (doseq [document bootstrap/p13-tooling-documents]
    (let [path (str "bootstrap/clojure/fixtures/rejected/" (bootstrap/p13-tooling-rejected-fixture-names document))
          constructed (atom 0)
          result (with-redefs [tooling/native-experience-artifact (fn [& _] (swap! constructed inc))]
                   (native-failure #(bootstrap/tooling-experience-file-artifact path)))]
      (is (= (bootstrap/p13-tooling-rejected-diagnostics document) (get-in result [:data :id])))
      (is (zero? @constructed))))
  (doseq [{:keys [fixture diagnostic]} bootstrap/stage0-compiled-tooling-rejected-fixtures]
    (let [constructed (atom 0)
          result (with-redefs [tooling/native-compiled-artifact (fn [& _] (swap! constructed inc))]
                   (native-failure #(bootstrap/hosted-core-compiled-tooling-proof-source-artifact fixture (slurp fixture))))]
      (is (= diagnostic (get-in result [:data :id])))
      (is (zero? @constructed)))))
