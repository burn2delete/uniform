(ns gravity.compiler.application-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.compiler.application :as application]
            [gravity.compiler.public-js-acceptance :as public-js-acceptance])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]
           [java.security MessageDigest]))

;; These are fresh caller source programs, not accepted Core/MIR fixtures or
;; imports from the SH12/SH13 test helpers. C2-C11 and SAFE9 remain the owners.
(def ^:private add-policy
  {:profile :hosted :target :jvm :effects #{:error/raise}
   :capabilities #{} :safety :safe})

(def ^:private bool-policy (assoc add-policy :effects #{}))

(deftest public-js-compiler-acceptance
  (public-js-acceptance/run-acceptance!))

(defn- source-text [module parameters body effects]
  (str "(ns " module "\n"
       "  (:profile :hosted)\n  (:target :jvm)\n"
       "  (:effects " (pr-str effects) ")\n"
       "  (:capabilities #{})\n  (:safety :safe)\n  (:exports [main]))\n\n"
       "(defn main [" parameters "]\n  " body ")\n"))

(def ^:private add-source
  (source-text 'compiler.integration.add "left :- I64 right :- I64"
               "(+ left right)" #{:error/raise}))

(defn- module-source [module exports effects definitions]
  (str "(ns " module "\n"
       "  (:profile :hosted)\n  (:target :jvm)\n"
       "  (:effects " (pr-str effects) ")\n"
       "  (:capabilities #{})\n  (:safety :safe)\n"
       "  (:exports " (pr-str exports) "))\n\n"
       definitions "\n"))

(def ^:private scalar-source
  (module-source 'acceptance.scalar '[score] #{:error/raise}
    (str "(defn add [left :- I64 right :- I64]\n"
         "  (+ left right))\n\n"
         "(defn score [choose-sum :- Bool left :- I64 right :- I64]\n"
         "  (let [sum (add left right)]\n"
         "    (if choose-sum\n"
         "      sum\n"
         "      left)))")))

(def ^:private composed-source
  (module-source 'renamed.scalar.composition '[evaluate] #{:error/raise}
    (str "(defn evaluate [enabled :- Bool first-value :- I64 second-value :- I64]\n"
         "  (let [combined (combine first-value second-value)\n"
         "        computed (relay combined first-value)]\n"
         "    (if enabled\n"
         "      (+ computed (if enabled second-value first-value))\n"
         "      (relay second-value first-value))))\n\n"
         "(defn relay [item :- I64 extra :- I64]\n"
         "  (combine item extra))\n\n"
         "(defn combine [augend :- I64 addend :- I64]\n"
         "  (+ augend addend))")))

(defn- with-source-file [filename text operation]
  (let [directory (Files/createTempDirectory
                   "uniform-application-" (make-array FileAttribute 0))
        path (.resolve directory filename)]
    (try
      (spit (.toFile path) text :encoding "UTF-8")
      (operation (.getCanonicalPath (.toFile path)))
      (finally
        (Files/deleteIfExists path)
        (Files/deleteIfExists directory)))))

(defn- sha256 [^bytes bytes]
  (str "sha256:"
       (apply str
              (map #(format "%02x" (bit-and 255 %))
                   (.digest (MessageDigest/getInstance "SHA-256") bytes)))))

(defn- content-id? [value]
  (and (string? value) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- diagnostic-source [data]
  (or (get-in data [:source-span :source])
      (get-in data [:source-span :file])
      (get-in data [:primary :span :source])
      (get-in data [:primary :span :file])
      (:actual-source-path data)
      (get-in data [:provenance :actual-source-path])
      (:source-path data)))

(defn- diagnostic-fact [data key]
  (if (contains? (:facts data) key) (get-in data [:facts key]) (get data key)))

(defn- record-rejection [label data]
  (println "current-source-to-verified-mir rejection"
           (pr-str {:case label :id (:id data) :missing-fact (:missing-fact data)
                    :source (diagnostic-source data)})))

(defn- core-function [core function-name]
  (let [id (get-in core [:functions function-name])]
    (first (filter #(= id (:node-id %)) (:core-nodes core)))))

(defn- function-operations [mir function-name]
  (let [function (get-in mir [:functions function-name])]
    (vec (mapcat #(get-in function [:blocks % :instructions]) (:block-order function)))))

(defn- source-nodes [core operation]
  (filterv #(= operation (:source-operation %)) (:core-nodes core)))

(defn- reference-invoker [source-path]
  ;; Compile the actual current C11 owner. Inputs to its execution functions
  ;; below are the unchanged products returned by the source-only API.
  (let [binding (bootstrap/p15-s23-c11-mir-source-binding!
                  source-path :jvm 2 :development-current)]
    (fn [function arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
        {:engine :gravity-compiler-application-reference-test
         :compiler-artifact-plan? true}
        (:plan binding) function arguments))))

(defn- execute-result [invoke result exported-function arguments]
  (invoke 'sh13-run-scalar-functions-mir
          [(:checked-core result) (:mir-module result) exported-function arguments]))

(defn- assert-execution-verified [invoke result exported-function arguments execution]
  (is (= :passed (get-in execution [:verification :status])))
  (is (= :passed
         (:status (invoke 'sh13-verify-scalar-functions-execution
                    [(:checked-core result) (:mir-module result)
                     exported-function arguments execution])))))

(defn- assert-source-result [result path text module policy]
  (let [bytes (.getBytes ^String text StandardCharsets/UTF_8)
        expected-hash (sha256 bytes)
        core (:checked-core result)
        mir (:mir-module result)
        report (:mir-verification result)
        receipt (:source-policy-receipt result)
        resolution (:function-resolution receipt)]
    (is (= :accepted (:status result)))
    (is (not-any? #{:compositional-multi-function} (:nonclaims result)))
    (is (= {:path path :content-hash expected-hash :byte-count (alength bytes)}
           (:source result)))
    (is (content-id? (:artifact-id result)))
    (is (= expected-hash (:source-content-hash core)
           (get-in core [:authenticated-input :source-content-hash])
           (get-in core [:semantic-provenance :source-content-hash])
           (get-in core [:physical-provenance :source-content-hash])))
    (is (= path (get-in core [:physical-provenance :actual-paths :source])))
    (is (= module (get-in core [:source-core-input :module]) (:module-id mir)))
    (is (= (:target policy) (:source-target core)
           (get-in core [:target-request-metadata :source-target])
           (get-in core [:target-request-metadata :requested-target])))
    (is (= :hosted (:profile core) (:profile mir)))
    (is (= {:implementation :clojure-stage0-seed :target :jvm :self-hosted? false}
           (:compiler-host result)))
    (is (= :passed (get-in result [:core-verification :status]) (:status report)))
    (is (= :gravity-source (get-in result [:core-verification :semantic-authority])))
    (is (= (:artifact-id core) (:source-core mir) (:checked-core-artifact-id report)))
    (is (= (:module-id mir) (:mir-module-id report)))
    (is (= [] (:diagnostics core) (:diagnostics mir) (:diagnostics report)))
    (is (= {:interface :gravity/compiler-application :version 1}
           (:source-interface receipt)))
    (is (= policy (:policy receipt)))
    (is (= :passed (:envelope-verification receipt)))
    (is (= :development-unadmitted (:admission-status receipt)))
    (is (= :selected-current-source-host-seed (:verification-scope receipt)))
    (is (false? (:self-hosted? receipt)))
    (is (= #{:reader :syntax :expansion :resolution :expression :checked-core}
           (set (keys (:owner-sources receipt)))))
    (doseq [[owner binding] (:owner-sources receipt)]
      (let [owner-bytes (Files/readAllBytes (.toPath (io/file (:source-path binding))))]
        (is (= (sha256 owner-bytes) (:source-content-hash binding)) (str owner))
        (is (= (alength owner-bytes) (:source-byte-count binding)) (str owner))))
    (is (= (get-in receipt [:owner-sources :reader :source-content-hash])
           (get-in receipt [:reader-plan-binding :source-content-hash])
           (get-in receipt [:reader-source-revision :source-content-hash])))
    (is (= (get-in receipt [:owner-sources :syntax :source-content-hash])
           (get-in receipt [:syntax-owner :binding :source-content-hash])
           (get-in receipt [:syntax-owner :source-revision :source-content-hash])))
    (is (= (get-in receipt [:owner-sources :expression :source-content-hash])
           (get-in receipt [:expression-binding :source-content-hash])))
    (is (content-id? (get-in receipt [:syntax-owner :artifact-id])))
    (is (= :accepted (get-in receipt [:syntax-owner :envelope :status])))
    (doseq [key [:semantic-envelope-id :provenance-binding-id]]
      (is (content-id? (get-in receipt [:syntax-owner :envelope key])) (str key)))
    (doseq [key [:sh06-artifact-id :sh06-semantic-projection-id :projection-binding]]
      (is (content-id? (get resolution key)) (str key)))
    (is (seq (:root-syntax-ids resolution)))
    (is (every? content-id? (:root-syntax-ids resolution)))
    (is (= (set (keys (:functions core))) (set (map :name (:declarations resolution)))))
    (doseq [declaration (:declarations resolution)]
      (doseq [key [:function-syntax-id :function-scope-id]]
        (is (content-id? (get declaration key)) (str (:name declaration) " " key)))
      (let [function (core-function core (:name declaration))
            origin (get-in core [:source-origin-table (get-in function [:source :origin-id])])]
        (is (keyword? (:function-form-id declaration)))
        (is (= (:function-form-id declaration) (:c2-form-id origin))
            "The ordinal C2 form ID must identify the actual function source origin"))
      (is (= (get-in declaration [:function-binding :binding-id])
             (get-in (core-function core (:name declaration)) [:attributes :declaration-binding-id]))))
    (is (false? (get-in receipt [:expression-binding :invoked?])))
    (is (= :scalar-module-seed-plan (get-in receipt [:expression-binding :reason])))
    (is (false? (:self-hosted? core)))
    (is (true? (:clojure-seed-boundary? core)))
    (doseq [[owner builder verifier]
            [[:c6-c10 'c6-c10-build-checked-core-template 'c6-c10-verify-checked-core-template]
             [:c11 'c11-build-scalar-functions-mir 'c11-verify-scalar-functions-mir]]]
      (let [binding (get-in result [:compiler-bindings owner])
            owner-bytes (Files/readAllBytes (.toPath (io/file (:source-path binding))))]
        (is (= builder (:builder-function binding)))
        (is (= verifier (:verifier-function binding)))
        (is (= (sha256 owner-bytes) (:source-content-hash binding)))
        (is (= (alength owner-bytes) (:source-byte-count binding)))
        (doseq [key [:plan-semantic-hash :functions-semantic-hash
                     :builder-semantic-hash :verifier-semantic-hash]]
          (is (content-id? (get binding key)) (str owner " " key)))))
    (println "current-source-to-verified-mir"
             (pr-str {:source expected-hash :path path :module module
                      :artifact (:artifact-id result) :checked-core (:artifact-id core)
                      :mir-module (:module-id mir) :source-target (:source-target core)
                      :source-policy (:owner-metadata-content-id receipt)
                      :syntax (get-in receipt [:syntax-owner :artifact-id])
                      :resolution (:sh06-artifact-id resolution)
                      :mir-report-scope (:scope report)
                      :compiler-bindings
                      (into {}
                            (map (fn [[owner binding]]
                                   [owner (select-keys binding
                                            [:source-path :source-content-hash
                                             :plan-semantic-hash :functions-semantic-hash
                                             :builder-function :builder-semantic-hash
                                             :verifier-function :verifier-semantic-hash])]))
                            (:compiler-bindings result))}))))

(deftest scalar-functions-js-source-target
  (let [text (str/replace scalar-source "(:target :jvm)" "(:target :js-ts)")
        policy (assoc add-policy :target :js-ts)]
    (with-source-file "scalar-js.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path policy)
              core (:checked-core result) mir (:mir-module result)]
          (assert-source-result result path text 'acceptance.scalar policy)
          (is (= :js-ts (:source-target result) (:source-target mir) (:target-request mir)))
          (is (= :pending (:verification-status mir))
              "The actual fresh verifier report stays separate from the raw MIR")
          (is (= [:js-ts] (get-in core [:scope :source-targets])
                 (get-in mir [:pass-contract :source-targets])))
          (is (= :complete-for-compositional-scalar-hosted-safe-js-ts-slice (:status core)))
          (doseq [table [:type-facts :effect-facts :ownership-facts :capability-facts]]
            (is (= #{:js-ts} (set (map :source-target (vals (get core table))))) (str table)))
          (is (= #{:js-ts} (set (map :target (vals (:safety-facts core))))))
          (is (= #{:js-ts} (set (map :target (vals (:runtime-check-table mir))))))
          (is (= #{:js-ts} (set (map :source-target (vals (:profile-target-table mir))))))
          (doseq [declaration (get-in result [:source-policy-receipt :function-resolution :declarations])]
            (is (= [:js-ts] (get-in declaration [:function-binding :target-set])))
            (doseq [parameter (:parameters declaration)]
              (is (= [:js-ts] (get-in parameter [:parameter-binding :target-set]))))))))))

(deftest current-source-to-verified-mir
  (let [compiler-identities (atom nil)]
   (doseq [[filename module parameters body names operand-order]
          [["add.gravity" 'compiler.integration.add "left :- I64 right :- I64"
            "(+ left right)" '[left right] [0 1]]
           ["renamed-sum.qst" 'example.renamed.sum "augend :- I64 addend :- I64"
            "(+ addend augend)" '[augend addend] [1 0]]]]
    (testing filename
      (let [text (source-text module parameters body #{:error/raise})]
        (with-source-file filename text
          (fn [path]
            (let [result (if (= filename "add.gravity")
                           (application/current-source-to-verified-mir path add-policy)
                           (binding [*print-length* 1 *print-level* 1 *print-meta* true
                                     *print-dup* true *print-readably* false
                                     *print-namespace-maps* true]
                             (application/current-source-to-verified-mir path add-policy)))
                  core (:checked-core result) mir (:mir-module result)
                  nodes (:core-nodes core)
                  parameters (source-nodes core :parameter)
                  locals (source-nodes core :local)
                  add (first (source-nodes core :integer-add))
                  root (core-function core 'main)
                  function (get-in mir [:functions 'main])
                  operations (function-operations mir 'main)
                  guard (first (filter #(= :runtime-check (:opcode %)) operations))
                  addition (first (filter #(= :integer-add (:opcode %)) operations))
                  token (:result guard)
                  identities (into {}
                                   (map (fn [[owner value]]
                                          [owner (select-keys value
                                                   [:source-content-hash :plan-semantic-hash
                                                    :functions-semantic-hash
                                                    :builder-semantic-hash :verifier-semantic-hash])]))
                                   (:compiler-bindings result))]
              (assert-source-result result path text module add-policy)
              (if-let [previous @compiler-identities]
                (is (= previous identities)
                    "Caller print bindings and source rename cannot change compiler identities")
                (reset! compiler-identities identities))
              (is (= [:parameter :parameter :local :local :integer-add :function]
                     (mapv :source-operation nodes)))
              (is (= names (get-in root [:attributes :params])))
              (is (= (mapv #(vector (:node-id (get parameters %))) operand-order)
                     (mapv :operands locals)))
              (is (= [:gravity/i64 :gravity/i64] (get-in root [:type :params])))
              (is (= :gravity/i64 (:type add) (get-in root [:type :return]) (:returns function)))
              (is (= #{:error/raise} (get-in root [:type :latent-effects])
                     (:latent-effects function)
                     (get-in core [:effect-facts (:node-id root) :residual])))
              (is (= #{:error/numeric} (get-in root [:type :throws])))
              (is (every? #(= :persistent-immutable-value (:model %)) (vals (:ownership-facts core))))
              (is (= :runtime-checked (get-in add [:safety :outcome])
                     (get-in core [:safety-facts (:node-id add) :outcome])))
              (is (= [64 :signed :checked :error/numeric #{:error/raise}]
                     ((juxt :bit-width :signedness :numeric-mode :failure :effects-introduced)
                      (get-in add [:safety :check]))))
              (is (= [:local :local :runtime-check :integer-add]
                     (mapv :opcode (remove #(= :function-boundary (:opcode %)) operations))))
              (is (= (mapv :node-id parameters) (:params function)))
              (is (= (mapv :node-id locals) (:operands guard)))
              (is (= (into [token] (map :node-id locals)) (:operands addition)))
              (is (= 1 (count (:runtime-check-table mir))))
              (is (= (:op-id guard)
                     (get-in mir [:data-flow-graph :definitions token :operation-id]))))))))))
  (let [text (source-text 'example.boolean.choice "enabled :- Bool"
                          "(if enabled true false)" #{})]
    (with-source-file "choice.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path bool-policy)
              core (:checked-core result) mir (:mir-module result)
              root (core-function core 'main) function (get-in mir [:functions 'main])
              invoke (reference-invoker path)]
          (assert-source-result result path text 'example.boolean.choice bool-policy)
          (is (= [:gravity/bool] (get-in root [:type :params])))
          (is (= :gravity/bool (get-in root [:type :return]) (:returns function)))
          (is (= #{} (get-in root [:type :latent-effects]) (:latent-effects function)))
          (is (= '[enabled] (get-in root [:attributes :params])))
          (is (= 1 (count (filter #(= :conditional-branch (get-in % [:terminator :kind]))
                                  (vals (:blocks function))))))
          (is (empty? (:runtime-check-table mir)))
          (doseq [value [true false]]
            (let [execution (execute-result invoke result 'main [value])]
              (is (= value (get-in execution [:execution :value])))
              (is (= :returned (get-in execution [:execution :status])))
              (assert-execution-verified invoke result 'main [value] execution))))))))

(deftest current-source-flat-boolean-to-verified-mir
  (let [module 'compiler.integration.boolean.sequence
        text (source-text module "" "(do true false)" #{})]
    (with-source-file "boolean-sequence.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path bool-policy)
              core (:checked-core result) mir (:mir-module result)
              root (core-function core 'main) function (get-in mir [:functions 'main])
              operations (function-operations mir 'main)
              invoke (reference-invoker path)]
          (assert-source-result result path text module bool-policy)
          (is (= [:literal :literal :do :function] (mapv :source-operation (:core-nodes core))))
          (is (= [] (get-in root [:type :params]) (:params function)))
          (is (= :gravity/bool (get-in root [:type :return]) (:returns function)))
          (is (= #{} (get-in root [:type :latent-effects]) (:latent-effects function)))
          (is (= 1 (count (:blocks function))))
          (is (= [:constant :constant :sequence]
                 (mapv :opcode (remove #(= :function-boundary (:opcode %)) operations))))
          (is (= [true false] (mapv #(get-in % [:constant-payload :value]) (take 2 operations))))
          (is (every? #(= :proven-safe (:outcome %)) (vals (:safety-facts core))))
          (is (empty? (:runtime-check-table mir)))
          (is (seq (get-in result [:mir-verification :checks])))
          (let [execution (execute-result invoke result 'main [])]
            (is (= :returned (get-in execution [:execution :status])))
            (is (= false (get-in execution [:execution :value])))
            (assert-execution-verified invoke result 'main [] execution)))))))

(deftest scalar-functions-and-control-flow
  (with-source-file "scalar-functions.gravity" scalar-source
    (fn [path]
      (let [result (application/current-source-to-verified-mir path add-policy)
            core (:checked-core result)
            mir (:mir-module result)
            helper (core-function core 'add)
            caller (core-function core 'score)
            call (first (source-nodes core :call))
            call-operation (first (filter #(= :call (:opcode %)) (function-operations mir 'score)))
            helper-operations (function-operations mir 'add)
            guard (first (filter #(= :runtime-check (:opcode %)) helper-operations))
            addition (first (filter #(= :integer-add (:opcode %)) helper-operations))
            invoke (reference-invoker path)]
        (assert-source-result result path scalar-source 'acceptance.scalar add-policy)
        (is (= '#{add score} (set (keys (:functions core))) (set (keys (:functions mir)))))
        (is (= '[score] (:exports core) (:exports mir)))
        (is (= '[left right] (get-in helper [:attributes :params])))
        (is (= '[choose-sum left right] (get-in caller [:attributes :params])))
        (is (= [:gravity/i64 :gravity/i64] (get-in helper [:type :params])))
        (is (= [:gravity/bool :gravity/i64 :gravity/i64] (get-in caller [:type :params])))
        (doseq [[name function visibility exported?]
                [['add helper :private false] ['score caller :public true]]]
          (is (= :gravity/i64 (get-in function [:type :return])
                 (get-in mir [:functions name :returns])))
          (is (= visibility (get-in function [:attributes :visibility])
                 (get-in mir [:functions name :visibility])))
          (is (= exported? (get-in function [:attributes :exported?])
                 (get-in mir [:functions name :exported?])))
          (is (= #{:error/raise} (get-in function [:type :latent-effects])
                 (get-in core [:effect-facts (:node-id function) :residual])
                 (get-in mir [:functions name :latent-effects])))
          (is (= #{:error/numeric} (get-in function [:type :throws])))
          (is (content-id? (get-in function [:attributes :declaration-binding-id]))))
        (is (= 'add (get-in call [:attributes :callee]) (:callee call-operation)))
        (is (= (:node-id helper) (get-in call [:attributes :callee-node-id])
               (:callee-id call-operation)))
        (is (= (get-in helper [:attributes :declaration-binding-id])
               (get-in call [:attributes :callee-binding-id])
               (:callee-binding-id call-operation)))
        (is (= (:type helper) (get-in call [:attributes :signature]) (:signature call-operation)))
        (is (= :left-to-right (get-in call [:attributes :evaluation-order])))
        (is (= :direct (:dispatch call-operation)))
        (is (= #{:error/raise} (:effects call) (:effects call-operation)))
        (is (seq (filter #(= 'sum (get-in % [:attributes :name])) (:core-nodes core))))
        (is (every? #(= :persistent-immutable-value (:model %)) (vals (:ownership-facts core))))
        (is (= 1 (count (:runtime-check-table mir))))
        (is (= (:result guard) (first (:operands addition))))
        (is (= (:operands guard) (subvec (:operands addition) 1)))
        (is (< (.indexOf helper-operations guard) (.indexOf helper-operations addition)))
        (doseq [[arguments expected]
                [[[true 3 7] 10]
                 [[false 7 3] 7]
                 [[true 9007199254740993 9007199254740994] 18014398509481987]]]
          (let [execution (execute-result invoke result 'score arguments)]
            (is (= :accepted (:status execution)))
            (is (= :returned (get-in execution [:execution :status])))
            (is (= expected (get-in execution [:execution :value])))
            (assert-execution-verified invoke result 'score arguments execution)))
        (let [arguments [false Long/MAX_VALUE 1]
              execution (execute-result invoke result 'score arguments)
              trace (get-in execution [:execution :trace])
              failed-check (last (filter #(= :runtime-check (:opcode %)) trace))]
          (is (= :rejected (:status execution)))
          (is (= :raised (get-in execution [:execution :status])))
          (is (= [:error/numeric :overflow]
                 ((juxt :type :kind) (get-in execution [:execution :error]))))
          (is (= [Long/MAX_VALUE 1] (get-in execution [:execution :error :operands])))
          (is (= (:node-id helper) (:function failed-check)))
          (is (= :fail (:guard-result failed-check)))
          (is (false? (:token-produced? failed-check)))
          (is (not-any? #(= :conditional-branch (:terminator %)) trace)
              "Eager let must raise from add before the false branch is selected")
          (assert-execution-verified invoke result 'score arguments execution))
        (let [private-call (execute-result invoke result 'add [3 7])]
          (is (= :rejected (:status private-call)))
          (is (nil? (:execution private-call))
              "The external entry selection must honor the actual export list"))))))

(deftest scalar-functions-compose-renamed-qst
  (with-source-file "renamed-functions.qst" composed-source
    (fn [path]
      (let [result (application/current-source-to-verified-mir path add-policy)
            core (:checked-core result)
            mir (:mir-module result)
            invoke (reference-invoker path)]
        (assert-source-result result path composed-source 'renamed.scalar.composition add-policy)
        (is (= '#{evaluate relay combine} (set (keys (:functions core)))
               (set (keys (:functions mir)))))
        (is (= '[evaluate] (:exports core) (:exports mir)))
        (is (= '[combine relay evaluate] (:function-order core)))
        (is (= 4 (count (source-nodes core :call))))
        (is (= 2 (count (source-nodes core :if))))
        (is (= 2 (count (:runtime-check-table mir))))
        (doseq [[arguments expected] [[[true 3 7] 20] [[false 3 7] 10]]]
          (let [execution (execute-result invoke result 'evaluate arguments)]
            (is (= :accepted (:status execution)))
            (is (= expected (get-in execution [:execution :value])))
            (is (= :returned (get-in execution [:execution :status])))
            (assert-execution-verified invoke result 'evaluate arguments execution)))))))

(deftest scalar-functions-resolve-shadowed-addition
  (let [text (module-source 'acceptance.shadowed '[select-value] #{}
               (str "(defn + [selected :- I64 unused :- I64] selected)\n\n"
                    "(defn select-value [first-value :- I64 second-value :- I64]\n"
                    "  (+ second-value first-value))"))]
    (with-source-file "shadowed-addition.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path bool-policy)
              core (:checked-core result)
              mir (:mir-module result)
              helper (core-function core '+)
              caller (core-function core 'select-value)
              call (first (source-nodes core :call))
              helper-mir (get-in mir [:functions '+])
              invoke (reference-invoker path)]
          (assert-source-result result path text 'acceptance.shadowed bool-policy)
          (is (= '#{+ select-value} (set (keys (:functions core))) (set (keys (:functions mir)))))
          (is (= '[select-value] (:exports core) (:exports mir)))
          (is (= '[selected unused] (get-in helper [:attributes :params])))
          (is (= (get-in helper [:attributes :parameter-node-ids]) (:params helper-mir)))
          (is (= 2 (count (:params helper-mir)) (count (:operands call))))
          (is (= [:gravity/i64 :gravity/i64] (:param-types helper-mir)))
          (is (= '+ (get-in call [:attributes :callee])))
          (is (= (get-in helper [:attributes :declaration-binding-id])
                 (get-in call [:attributes :callee-binding-id])))
          (is (empty? (source-nodes core :integer-add)))
          (is (empty? (:runtime-check-table mir)))
          (doseq [function [helper caller]]
            (is (= #{} (get-in function [:type :latent-effects])
                   (get-in core [:effect-facts (:node-id function) :residual])))
            (is (= #{} (get-in function [:type :throws]))))
          (doseq [[arguments expected] [[[3 7] 7] [[Long/MAX_VALUE 1] 1]
                                       [[1 Long/MAX_VALUE] Long/MAX_VALUE]]]
            (let [execution (execute-result invoke result 'select-value arguments)]
              (is (= :accepted (:status execution)))
              (is (= :returned (get-in execution [:execution :status])))
              (is (= expected (get-in execution [:execution :value])))
              (is (not-any? #(contains? #{:runtime-check :integer-add} (:opcode %))
                            (get-in execution [:execution :trace])))
              (assert-execution-verified invoke result 'select-value arguments execution))))))))

(deftest current-source-rejects-invalid-programs
  (doseq [[label text policy expected-rule]
          [["malformed" (str add-source "[") add-policy "C2-DELIMITER"]
           ["operand-type" (str/replace add-source "(+ left right)" "(+ left true)")
            add-policy "C7-TYPE-MISMATCH"]
           ["arity" (str/replace add-source "(+ left right)" "(+ left)")
            add-policy "L2-BUILTIN-ARITY"]
           ["undeclared-raise" (str/replace add-source "#{:error/raise}" "#{}")
            bool-policy "C8-UNDECLARED"]]]
    (testing label
      (with-source-file (str label ".gravity") text
        (fn [path]
          (let [data (failure #(application/current-source-to-verified-mir path policy))]
            (record-rejection label data)
            (is (= expected-rule (:id data)) (pr-str data))
            (is (= path (diagnostic-source data)) (pr-str data))
            (when (= label "undeclared-raise")
              (is (= :declared-numeric-error-effect (:missing-fact data))))))))))

(deftest scalar-functions-reject-invalid-programs
  (doseq [[label text policy expected-rule expected-facts]
          [["unknown-callee" (str/replace scalar-source "(add left right)" "(missing left right)")
            add-policy "C5-UNRESOLVED" {}]
           ["call-arity" (str/replace scalar-source "(add left right)" "(add left)")
            add-policy "L2-BUILTIN-ARITY" {:expected-arity 2 :actual-arity 1}]
           ["call-argument-type" (str/replace scalar-source "(add left right)" "(add true right)")
            add-policy "C7-TYPE-MISMATCH"
            {:missing-fact :scalar-call-argument-types
             :expected-type [:gravity/i64 :gravity/i64]
             :actual-type [:gravity/bool :gravity/i64]}]
           ["unused-invalid-function"
            (str scalar-source "\n(defn unused [value :- Bool] (+ value value))\n")
            add-policy "C7-TYPE-MISMATCH"
            {:missing-fact :binary-i64-add-operands
             :expected-type [:gravity/i64 :gravity/i64]
             :actual-type [:gravity/bool :gravity/bool]}]
           ["branch-results" (str/replace scalar-source "(if choose-sum\n      sum\n      left)" "(if choose-sum true left)")
            add-policy "C7-TYPE-MISMATCH"
            {:missing-fact :scalar-branch-result-types
             :expected-type :gravity/bool :actual-type :gravity/i64}]
           ["branch-condition" (str/replace scalar-source "(if choose-sum\n      sum\n      left)" "(if left sum left)")
            add-policy "C7-TYPE-MISMATCH"
            {:missing-fact :scalar-bool-condition
             :expected-type :gravity/bool :actual-type :gravity/i64}]
           ["caller-undeclared-raise" (str/replace scalar-source "#{:error/raise}" "#{}")
            bool-policy "C8-UNDECLARED"
            {:missing-fact :declared-numeric-error-effect :function 'score
             :inferred-effects #{:error/raise} :declared-effects #{}}]
           ["lexical-binding" (str/replace scalar-source "(if choose-sum\n      sum\n      left)" "(if choose-sum missing left)")
            add-policy "C5-UNRESOLVED" {}]
           ["direct-recursion" (str/replace scalar-source "(+ left right)" "(add left right)")
            add-policy "C6-LOWERING-GAP" {:missing-fact :nonrecursive-scalar-call-graph}]
           ["mutual-recursion"
            (str (str/replace scalar-source "(+ left right)" "(bounce left right)")
                 "\n(defn bounce [first :- I64 second :- I64] (add first second))\n")
            add-policy "C6-LOWERING-GAP" {:missing-fact :nonrecursive-scalar-call-graph}]]]
    (testing label
      (with-source-file (str label ".gravity") text
        (fn [path]
          (let [data (failure #(application/current-source-to-verified-mir path policy))]
            (record-rejection label data)
            (is (= expected-rule (:id data)) (pr-str data))
            (is (= path (diagnostic-source data)) (pr-str data))
            (doseq [[key expected] expected-facts]
              (is (= expected (diagnostic-fact data key))
                  (str label " " key " " (pr-str data))))))))))

(deftest current-source-rejects-supplied-authority
  (with-source-file "explicit-policy.gravity" add-source
    (fn [path]
      (doseq [[label policy]
              (concat [["missing-policy" nil] ["empty-policy" {}]
                       ["missing-policy-effects" (dissoc add-policy :effects)]
                       ["unsupported-js-target" (assoc add-policy :target :js)]
                       ["unsupported-profile" (assoc add-policy :profile :native)]
                       ["unsupported-capability" (assoc add-policy :capabilities #{:io/file})]
                       ["unsupported-safety" (assoc add-policy :safety :unsafe)]]
                      (for [key [:checked-core :mir-module :verifier-report :binding :token
                                 :type-facts :effect-facts :ownership-facts :safety-facts]]
                        [(str "supplied-" (name key))
                         (assoc add-policy key {:status :accepted})]))]
        (let [data (failure #(application/current-source-to-verified-mir path policy))]
          (record-rejection label data)
          (is (some? data) (pr-str policy))
          (is (string? (:id data)) (pr-str data))))
      (let [data (failure #(application/current-source-to-verified-mir
                            {:source-path path :checked-core {:status :accepted}} add-policy))]
        (record-rejection "supplied-source-artifact" data)
        (is (some? data))
        (is (string? (:id data)))))))

(deftest current-source-handoff-keeps-lexical-constructor
  (with-source-file "handoff.gravity" scalar-source
    (fn [path]
      (let [substituted-calls (atom 0) consumed? (atom false)
            data
            (with-redefs [bootstrap/p15-s23-current-source-to-verified-mir
                          (fn [& _]
                            (swap! substituted-calls inc)
                            (throw (ex-info "Substituted public constructor" {:id :substituted-constructor})))]
              (failure
                #(bootstrap/p15-s23-call-with-current-source-verified-mir
                   path (assoc add-policy :target :unsupported)
                   (fn [_ _] (reset! consumed? true)))))]
        (is (= "C11-MODULE" (:id data)))
        (is (= :application-explicit-hosted-safe-scalar-policy (:missing-fact data)))
        (is (= path (diagnostic-source data)))
        (is (zero? @substituted-calls))
        (is (false? @consumed?))))))

(deftest scalar-functions-reject-policy-mismatch
  (doseq [[label text expected-rule expected-facts]
          [["effect-policy-mismatch" (str/replace scalar-source "#{:error/raise}" "#{}")
            "C6-ORIGIN" {:missing-fact :declared-source-policy-matches-request}]
           ["target-policy-mismatch" (str/replace scalar-source "(:target :jvm)" "(:target :js)")
            "B1-TARGET-UNSUPPORTED" {:target :js :supported #{:jvm :js-ts} :bootstrap-stage :stage0}]
           ["canonical-target-policy-mismatch" (str/replace scalar-source "(:target :jvm)" "(:target :js-ts)")
            "C6-ORIGIN" {:missing-fact :declared-source-policy-matches-request}]]]
    (with-source-file (str label ".gravity") text
      (fn [path]
        (let [data (failure #(application/current-source-to-verified-mir path add-policy))]
          (record-rejection label data)
          (is (= expected-rule (:id data)) (pr-str data))
          (is (= path (diagnostic-source data)) (pr-str data))
          (doseq [[key expected] expected-facts]
            (is (= expected (diagnostic-fact data key))
                (str label " " key " " (pr-str data)))))))))

(defn- with-mutated-runtime-result [function-name transform operation]
  (let [original bootstrap/p15-s23-stage2-runtime-execute-function
        changed? (atom false)]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [context plan function arguments]
                    (let [result (original context plan function arguments)]
                      (if (and (= function-name function)
                               (compare-and-set! changed? false true))
                        (transform result)
                        result)))]
      (let [data (failure operation)]
        (is @changed? (str "The original " function-name " result must be reached"))
        (is (some? data) (str "Mutated " function-name " result must be rejected"))
        (is (string? (:id data)) (pr-str data))
        data))))

(deftest current-source-rejects-tampered-handoffs
  (with-source-file "tamper.gravity" add-source
    (fn [path]
      (doseq [[label function transform]
              [["Core type fact" 'c6-c10-build-checked-core-template
                (fn [result]
                  (let [id (:node-id (first (source-nodes (:artifact-template result) :integer-add)))]
                    (assoc-in result [:artifact-template :type-facts id :type] :gravity/bool)))]
               ["Core report" 'c6-c10-verify-checked-core-template
                #(assoc % :checks [])]
               ["MIR guard" 'c11-build-scalar-functions-mir
                #(assoc % :runtime-check-table {})]
               ["MIR report" 'c11-verify-scalar-functions-mir
                #(assoc % :checked-core-artifact-id (str "sha256:" (apply str (repeat 64 "0"))))]]]
        (testing label
          (let [data (with-mutated-runtime-result function transform
                       #(application/current-source-to-verified-mir path add-policy))]
            (record-rejection label data)
            (is (= path (diagnostic-source data)) (pr-str data))))))))

(defn- mutate-operation [mir opcode transform]
  (let [path (first
               (for [[name function] (:functions mir)
                     block-id (:block-order function)
                     [index operation] (map-indexed vector (get-in function [:blocks block-id :instructions]))
                     :when (= opcode (:opcode operation))]
                 [:functions name :blocks block-id :instructions index]))]
    (is (some? path) (str "The genuine MIR must contain " opcode))
    (if path (update-in mir path transform) mir)))

(defn- mutate-core-function [core name transform]
  (let [id (get-in core [:functions name])
        index (first (keep-indexed (fn [i node] (when (= id (:node-id node)) i)) (:core-nodes core)))]
    (is (some? index) (str "The genuine Core must contain " name))
    (if index (update-in core [:core-nodes index] transform) core)))

(deftest scalar-functions-reject-tampered-products
  (with-source-file "scalar-tamper.gravity" scalar-source
    (fn [path]
      (let [result (application/current-source-to-verified-mir path add-policy)
            core (:checked-core result)
            mir (:mir-module result)
            caller-id (get-in core [:functions 'score])
            add-node (first (source-nodes core :integer-add))
            addition (first (filter #(= :integer-add (:opcode %)) (function-operations mir 'add)))
            guard (first (filter #(= :runtime-check (:opcode %)) (function-operations mir 'add)))
            invoke (reference-invoker path)
            cases
            [["Core callee signature"
              (mutate-core-function core 'add #(assoc-in % [:type :params 0] :gravity/bool)) mir]
             ["Core caller effect" (assoc-in core [:effect-facts caller-id :residual] #{}) mir]
             ["Core ownership" (assoc-in core [:ownership-facts caller-id :model] :linear-resource) mir]
             ["Core safety" (assoc-in core [:safety-facts (:node-id add-node) :outcome] :proven-safe) mir]
             ["MIR call signature" core
              (mutate-operation mir :call #(assoc-in % [:signature :params 0] :gravity/bool))]
             ["MIR call callee" core (mutate-operation mir :call #(assoc % :callee 'missing))]
             ["MIR call arity" core (mutate-operation mir :call #(update % :operands pop))]
             ["MIR call argument order" core (mutate-operation mir :call #(update % :operands (comp vec reverse)))]
             ["MIR call declaration" core
              (mutate-operation mir :call #(assoc % :callee-binding-id caller-id))]
             ["MIR ownership fact" core
              (assoc-in mir [:ownership-table (get-in addition [:facts :ownership-fact-id]) :model]
                        :linear-resource)]
             ["MIR safety fact" core
              (assoc-in mir [:safety-table (get-in addition [:facts :safety-outcome-id]) :outcome]
                        :proven-safe)]
             ["MIR type fact" core
              (assoc-in mir [:type-table (get-in addition [:facts :type-fact-id]) :type] :gravity/bool)]
             ["MIR guard table" core (assoc mir :runtime-check-table {})]
             ["MIR missing token consumption" core
              (mutate-operation mir :integer-add #(update % :operands subvec 1))]
             ["MIR wrong token definition" core
              (assoc-in mir [:data-flow-graph :definitions (:result guard) :operation-id] (:op-id addition))]
             ["MIR guard use before definition" core
              (let [entry (get-in mir [:functions 'add :entry])
                    instructions (get-in mir [:functions 'add :blocks entry :instructions])
                    guard-index (.indexOf instructions guard)
                    add-index (.indexOf instructions addition)]
                (is (<= 0 guard-index))
                (is (< guard-index add-index))
                (assoc-in mir [:functions 'add :blocks entry :instructions]
                          (assoc instructions guard-index addition add-index guard)))]]
            rules {"Core callee signature" "C11-TYPE" "Core caller effect" "C11-EFFECT"
                   "Core ownership" "C11-VERIFY" "Core safety" "C11-SAFETY"
                   "MIR call signature" "C11-TYPE" "MIR call callee" "C11-TYPE"
                   "MIR call arity" "C11-TYPE" "MIR call argument order" "C11-VERIFY"
                   "MIR call declaration" "C11-TYPE" "MIR ownership fact" "C11-VERIFY"
                   "MIR safety fact" "C11-SAFETY" "MIR type fact" "C11-TYPE"
                   "MIR guard table" "C11-SAFETY" "MIR missing token consumption" "C11-SAFETY"
                   "MIR wrong token definition" "C11-DOMINANCE"
                   "MIR guard use before definition" "C11-SAFETY"}
            owners {"Core callee signature" 'add "Core caller effect" 'score
                    "Core ownership" 'score "Core safety" 'add
                    "MIR call signature" 'score "MIR call callee" 'score
                    "MIR call arity" 'score "MIR call argument order" 'score
                    "MIR call declaration" 'score "MIR ownership fact" 'add
                    "MIR safety fact" 'add "MIR type fact" 'add
                    "MIR guard table" 'add "MIR missing token consumption" 'add
                    "MIR wrong token definition" 'add "MIR guard use before definition" 'add}]
        (is (= :passed (:status (invoke 'c11-verify-scalar-functions-mir [core mir]))))
        (doseq [[label candidate-core candidate-mir] cases]
          (testing label
            (let [verification (invoke 'c11-verify-scalar-functions-mir [candidate-core candidate-mir])
                  execution (invoke 'sh13-run-scalar-functions-mir
                                    [candidate-core candidate-mir 'score [true 3 7]])
                  diagnostic (first (:diagnostics verification))]
              (is (= :rejected (:status verification)) (str label " " (pr-str verification)))
              (is (= (get rules label) (:diagnostic-id diagnostic)) (pr-str diagnostic))
              (is (= path (diagnostic-source diagnostic)) (pr-str diagnostic))
              (is (= (get owners label) (:function diagnostic)) (pr-str diagnostic))
              (is (string? (:operation-id diagnostic)) (pr-str diagnostic))
              (is (not= (:expected diagnostic) (:actual diagnostic)) (pr-str diagnostic))
              (is (= :rejected (:status execution)))
              (is (nil? (:execution execution)) "Rejected MIR must not execute")
              (when (= label "MIR call signature")
                (let [data (failure #((ns-resolve 'gravity.bootstrap 'p15-s23-application-c11-result!)
                                      path :test-actual-c11-rejected-report verification))]
                  (is (= "C11-TYPE" (:id data)) (pr-str data))
                  (is (= path (diagnostic-source data)) (pr-str data))
                  (is (= 'score (diagnostic-fact data :function-id)) (pr-str data))
                  (is (= [:gravity/i64 :gravity/i64]
                         (get-in (diagnostic-fact data :expected) [8 :params])) (pr-str data))
                  (is (= [:gravity/bool :gravity/i64]
                         (get-in (diagnostic-fact data :actual) [8 :params])) (pr-str data)))))))
        (let [execution (execute-result invoke result 'score [true 3 7])
              altered (assoc-in execution [:execution :value] 11)
              verification (invoke 'sh13-verify-scalar-functions-execution
                                   [core mir 'score [true 3 7] altered])]
          (is (= :rejected (:status verification)))
          (is (= :scalar-execution-result-substitution
                 (get-in verification [:diagnostics 0 :missing-fact]))))))))

(defn- assert-source-and-binding-substitution [filename text]
  (with-source-file filename text
    (fn [path]
      (let [original bootstrap/p15-s23-c6c10-private-builder-envelope]
        (doseq [kind [:source :binding :file-bytes]]
          (testing kind
            (spit path text :encoding "UTF-8")
            (let [changed? (atom false)
                  data
                  (with-redefs [bootstrap/p15-s23-c6c10-private-builder-envelope
                                (fn [request-source ingress binding]
                                  (let [envelope (original request-source ingress binding)]
                                    (if (compare-and-set! changed? false true)
                                      (case kind
                                        :source (assoc envelope :source-content-hash
                                                       (str "sha256:" (apply str (repeat 64 "0"))))
                                        :binding (assoc-in envelope [:binding-pins :builder-source-hash]
                                                           (str "sha256:" (apply str (repeat 64 "0"))))
                                        :file-bytes (do (spit path (str/replace text "left" "renamed")
                                                              :encoding "UTF-8")
                                                        envelope))
                                      envelope)))]
                    (failure #(application/current-source-to-verified-mir path add-policy)))]
              (record-rejection (str "substitution-" (name kind)) data)
              (is @changed? "The genuine source envelope must be constructed first")
              (is (some? data) (str kind " substitution must be rejected"))
              (is (string? (:id data)) (pr-str data))
              (is (= path (diagnostic-source data)) (pr-str data)))))))))

(deftest current-source-rejects-source-and-binding-substitution
  (assert-source-and-binding-substitution "substitution.gravity" add-source)
  (assert-source-and-binding-substitution "scalar-substitution.qst" scalar-source))

(deftest current-source-keeps-the-increment-scope
  (doseq [[label text]
          [["subtraction" (str/replace add-source "(+ left right)" "(- left right)")]
           ["multiplication" (str/replace add-source "(+ left right)" "(* left right)")]]]
    (testing label
      (with-source-file (str label ".gravity") text
        (fn [path]
          (let [data (failure #(application/current-source-to-verified-mir path add-policy))]
            (record-rejection label data)
            (is (some? data) (str label " is outside this increment"))
            (is (string? (:id data)) (pr-str data))))))))
