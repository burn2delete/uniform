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
    (is (= 21 (count publics)))
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
      (is (= '[clojure.set] (get-in contract [:dependency-direction :requires])))))
  (is (= #{'set} (set (keys (ns-aliases 'gravity.tooling)))))
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
