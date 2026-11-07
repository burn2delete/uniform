(ns gravity.backend.llvm.differential-evidence-input-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path
  "bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity")

;; Compile the current owner, without renewing historical migration pins.
(def ^:private plan
  (delay
    (let [emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path (slurp source-path)))))

(defn- invoke [arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-b14-linux-evidence-input-test
    :compiler-artifact-plan? true}
   @plan 'w5-b14-linux-differential-evidence-input arguments))

(def ^:private fields
  [:content-hash :byte-count :format :architecture :header])

(defn- expected [process-result object executable]
  {:kind :gravity/b14-bounded-llvm-differential-record
   :process-result process-result
   :elf-object (select-keys object fields)
   :elf-executable (select-keys executable fields)})

(deftest current-source-constructs-the-complete-preimage
  (let [process {:status :complete :expected-exit-code 7 :observed-exit-code 7
                 :stdout "" :stderr "" :extra {:nested [nil false 17]}}
        object {:content-hash "sha256:object" :byte-count 64 :format :elf
                :architecture :x86_64 :header {:type :relocatable}
                :bytes [1 2] :bookkeeping :excluded}
        executable {:content-hash "sha256:executable" :byte-count 128 :format :elf
                    :architecture :x86_64 :header {:type :executable}
                    :bytes [3 4] :bookkeeping :also-excluded}]
    (is (= (expected process object executable)
           (invoke [process object executable])))
    (doseq [side [0 1]
            field fields
            change [#(dissoc % field) #(assoc % field nil)
                    #(assoc % field [:changed field])]]
      (let [[o e] (update [object executable] side change)]
        (is (= (expected process o e) (invoke [process o e])))))
    (is (not= (invoke [process {} {}])
              (invoke [process {:header nil} {}])))
    (is (not= (invoke [process object executable])
              (invoke [(assoc process :arbitrary-field :retained)
                       object executable])))
    (is (= (invoke [process object executable])
           (invoke [process (assoc object :bytes [9] :bookkeeping :changed)
                    (assoc executable :bytes [8] :bookkeeping :changed)])))))

(deftest evidence-input-rejects-wrong-arity
  (doseq [arguments [[] [{}] [{} {}] [{} {} {} {}]]]
    (is (thrown? clojure.lang.ExceptionInfo (invoke arguments)))))

(deftest current-source-preserves-differential-entry-rejections
  ;; The complete owner must compile, including the validator whose missing
  ;; outer else previously blocked evidence-input execution.
  (let [entry {:backend :llvm :target :llvm-x86_64-linux
               :fixture-id "fixture" :reference-id "same" :observed-id "same"
               :status :matched :execution :not-required-static-only}
        valid? (fn [value]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-b14-linux-evidence-input-test
                   :compiler-artifact-plan? true}
                  @plan 'w5-b14-differential-entry-valid? [value]))]
    (is (true? (valid? entry)))
    (doseq [value [nil [] (dissoc entry :execution) (assoc entry :extra :field)
                   (assoc entry :backend :c)
                   (assoc entry :target :darwin)
                   (assoc entry :fixture-id "")
                   (assoc entry :reference-id "")
                   (assoc entry :observed-id "")
                   (assoc entry :observed-id "different")
                   (assoc entry :status :mismatched)
                   (assoc entry :execution :observed)]]
      (is (false? (valid? value)) (pr-str value)))))

(defn- native-var [name]
  (or (ns-resolve 'gravity.bootstrap (symbol name))
      (throw (ex-info "Missing production function" {:name name}))))

(def ^:private observed-toolchain
  {:status :complete :authoritative? false :native-authority? false
   :process-result {:status :complete :expected-exit-code 7 :observed-exit-code 7
                    :stdout "out" :stderr "err" :matched? true
                    :extra {:nested [nil false 17]}}
   :elf-object {:content-hash "sha256:object" :byte-count 64 :format :elf
                :architecture :x86_64 :header {:type :relocatable}
                :bytes [1 2] :bookkeeping :excluded}
   :elf-executable {:content-hash "sha256:executable" :byte-count 128 :format :elf
                    :architecture :x86_64 :header {:type :executable}
                    :bytes [3 4] :bookkeeping :also-excluded}})

(defn- construct-artifact []
  ;; Exercise the real internal constructor. These are data fixtures, not
  ;; authenticated inputs for the public build or tool execution boundary.
  ((native-var "p15-s23-b3-llvm-linux-artifact-record")
   {:mir-id "fixture-mir"} {:artifact-id "fixture-core"}
   {:source-path "controlled-observations.gravity"}
   {:artifact-id "fixture-bridge" :c13 {:artifact-id "fixture-c13"}
    :c14 {:artifact-id "fixture-c14"} :b1 {:artifact-id "fixture-b1"}}
   {:llvm-ir "define i32 @main() { ret i32 7 }"
    :expected-exit-code 7 :semantic-result 7 :result-type :gravity/integer}
   {:source-path "fixture-b3.gravity"} {}))

(defn- with-observations [toolchain f]
  (with-redefs-fn
    {(native-var "p15-s23-b3-llvm-linux-development-evidence")
     (fn [_ _ options]
       (is (= {} options))
       toolchain)
     (native-var "p15-s23-b3-llvm-linux-toolchain-transaction!")
     (fn [& _] (throw (ex-info "No tool execution in data tests" {})))}
    f))

(defn- evidence-id [toolchain]
  (with-observations toolchain
    #(get-in (construct-artifact) [:b14-record :evidence-id])))

(defn- expected-id [toolchain]
  (bootstrap/p15-s23-c11-mir-digest
   (expected (:process-result toolchain) (:elf-object toolchain)
             (:elf-executable toolchain))))

(defn- diagnostic [f]
  (try (f) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest real-caller-executes-current-source-and-keeps-nonauthority
  (let [execute bootstrap/p15-s23-stage2-runtime-execute-function
        calls (atom [])
        before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
        artifact
        (with-observations observed-toolchain
          #(with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                         (fn [runtime plan function arguments]
                           (swap! calls conj [runtime plan function arguments])
                           (execute runtime plan function arguments))]
             (construct-artifact)))
        [runtime compiled function arguments] (first @calls)]
    (is (= 1 (count @calls)))
    (is (= :gravity-b14-linux-evidence-input-current-source (:engine runtime)))
    (is (= 'w5-b14-linux-differential-evidence-input function))
    (is (= (mapv observed-toolchain [:process-result :elf-object :elf-executable]) arguments))
    (is (= (get-in @plan [:source :sha256]) (get-in compiled [:source :sha256])))
    (is (= #{:memory/allocate} (get-in compiled [:module :effects])))
    (is (= #{} (get-in compiled [:module :capabilities])))
    (is (= (expected-id observed-toolchain) (get-in artifact [:b14-record :evidence-id])))
    (is (= :development-emulation-observed (:status artifact)
           (get-in artifact [:b14-record :status])))
    (is (true? (get-in artifact [:b14-record :same-result?])))
    (is (= (:process-result observed-toolchain) (get-in artifact [:b14-record :process-result])))
    (doseq [record [artifact (:b3-record artifact) (:b13-record artifact)
                    (:b14-record artifact) (:c18-record artifact)]]
      (is (true? (:clojure-seed-boundary? record)))
      (is (false? (:self-hosted? record))))
    (doseq [flag [:c11-llvm-credit? :target-lowering-credit? :backend-credit?
                  :public-target? :release-credit? :whole-language?]]
      (is (false? (get artifact flag))))
    (doseq [gate [:release-gate :public-target-gate :self-hosting-gate]]
      (is (= :closed (get-in artifact [:c18-record gate]))))
    (is (false? (get-in artifact [:toolchain-evidence :authoritative?])))
    (is (false? (get-in artifact [:toolchain-evidence :native-authority?])))
    (let [data (diagnostic
                #((native-var "p15-s23-b3-llvm-require-authority!")
                  artifact "controlled-observations.gravity" :execute-tools))]
      (is (= "B3-MANIFEST" (:id data)))
      (is (= :opaque-authenticated-b3-side-effect-authority (:missing-fact data))))
    (is (= before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)))))

(deftest production-identity-follows-every-retained-field
  ;; Reuse the actual current-source plan across this data matrix. Execution,
  ;; independent verification, hashing and artifact construction stay real.
  (with-redefs-fn
    {(native-var "p15-s23-b3-llvm-linux-differential-binding!") (fn [_] @plan)}
    (fn []
      (let [baseline (evidence-id observed-toolchain)]
        (is (= (expected-id observed-toolchain) baseline))
        (doseq [side [:elf-object :elf-executable]
                field fields
                change [#(dissoc % field) #(assoc % field nil)
                        #(assoc % field [:changed field])]]
          (let [changed (update observed-toolchain side change)]
            (is (= (expected-id changed) (evidence-id changed)) [side field])
            (is (not= baseline (evidence-id changed)) [side field])))
        (doseq [field (keys (:process-result observed-toolchain))
                change [#(dissoc % field) #(assoc % field nil)
                        #(assoc % field [:changed field])]]
          (let [changed (update observed-toolchain :process-result change)]
            (is (= (expected-id changed) (evidence-id changed)) field)
            (is (not= baseline (evidence-id changed)) field)))
        (let [changed (assoc-in observed-toolchain [:process-result :arbitrary-field]
                                {:entire-value [nil false :retained]})]
          (is (= (expected-id changed) (evidence-id changed)))
          (is (not= baseline (evidence-id changed))))
        (doseq [side [:elf-object :elf-executable] field fields]
          (is (not= (evidence-id (update observed-toolchain side dissoc field))
                    (evidence-id (assoc-in observed-toolchain [side field] nil)))))
        (is (= baseline
               (evidence-id (-> observed-toolchain
                                (update :elf-object assoc :bytes [99] :bookkeeping :changed)
                                (update :elf-executable assoc :bytes [88] :bookkeeping :changed)))))
        (let [mismatch (assoc-in observed-toolchain [:process-result :observed-exit-code] 8)]
          (with-observations mismatch
            #(let [record (:b14-record (construct-artifact))]
               (is (= (expected-id mismatch) (:evidence-id record)))
               (is (false? (:same-result? record)))
               (is (= :development-emulation-observed (:status record))))))))))

(deftest incomplete-toolchain-never-constructs-an-evidence-id
  (with-redefs-fn
    {(native-var "p15-s23-b3-llvm-linux-differential-binding!")
     (fn [& _] (throw (ex-info "Incomplete observations must not compile B14" {})))
     (native-var "p15-s23-b3-llvm-linux-toolchain-transaction!")
     (fn [& _] (throw (ex-info "No process authority" {})))}
    (fn []
      ;; Real default observations and a partial supplied observation both
      ;; remain incomplete, even when the latter carries complete-looking data.
      (doseq [artifact (cons (construct-artifact)
                            (for [status [nil :incomplete :rejected]]
                              (with-observations (assoc observed-toolchain :status status)
                                construct-artifact)))]
        (is (= :incomplete (:status artifact) (get-in artifact [:b14-record :status])))
        (is (nil? (get-in artifact [:b14-record :evidence-id])))
        (is (= :not-established (get-in artifact [:b14-record :same-result?])))
        (is (= {:status :runtime-derived-required}
               (get-in artifact [:b14-record :process-result])))))))

(deftest incompatible-current-source-bindings-fail-before-execution
  (doseq [change [#(assoc-in % [:source :sha256] "sha256:substituted")
                  #(assoc % :plan-id "sha256:substituted")
                  #(assoc-in % [:module :module] 'substituted.owner)
                  #(assoc-in % [:module :profile] :hosted)
                  #(assoc-in % [:module :target] :llvm-x86_64-linux)
                  #(assoc-in % [:module :effects] #{})
                  #(assoc-in % [:module :capabilities] #{:process/execute})
                  #(assoc-in % [:module :safety] :unsafe)
                  #(assoc-in % [:module :exports] [])
                  #(assoc-in % [:functions 'w5-b14-linux-differential-evidence-input :arity] 2)
                  #(assoc-in % [:functions 'w5-b14-linux-differential-evidence-input :params]
                             '[elf-object process-result elf-executable])]]
    (let [changed (change @plan)
          ;; Exercise ABI and policy checks even with a recomputed plan ID.
          changed (if (= "sha256:substituted" (:plan-id changed)) changed
                      (assoc changed :plan-id
                             (bootstrap/p15-s23-c11-mir-digest (dissoc changed :plan-id))))
          calls (atom 0)
          data (with-observations observed-toolchain
                 #(with-redefs [bootstrap/p15-s23-stage2-compiler-artifact-plan
                                (fn [& _] changed)
                                bootstrap/p15-s23-stage2-runtime-execute-function
                                (fn [& _] (swap! calls inc))]
                    (diagnostic construct-artifact)))]
      (is (= "B1-INPUT" (:id data)))
      (is (= :current-b14-evidence-source-binding (:missing-fact data)))
      (is (zero? @calls)))))

(deftest malformed-rejected-and-substituted-results-have-no-host-fallback
  (let [correct (expected (:process-result observed-toolchain)
                         (:elf-object observed-toolchain) (:elf-executable observed-toolchain))]
    (doseq [result [nil [] :malformed {:status :rejected :diagnostic "B14-DIFFERENTIAL"}
                    (dissoc correct :elf-executable)
                    (assoc correct :kind :substituted)
                    (assoc correct :extra :substituted)
                    (update correct :process-result dissoc :extra)
                    (assoc-in correct [:elf-object :header] :substituted)
                    (assoc-in correct [:elf-executable :bytes] [3 4])]]
      (let [digests (atom [])
            digest bootstrap/p15-s23-c11-mir-digest
            data (with-observations observed-toolchain
                   #(with-redefs-fn
                      {(native-var "p15-s23-b3-llvm-linux-differential-binding!") (fn [_] @plan)
                       #'bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] result)
                       #'bootstrap/p15-s23-c11-mir-digest
                       (fn [value] (swap! digests conj value) (digest value))}
                      (fn [] (diagnostic construct-artifact))))]
        (is (= "B3-PASS" (:id data)))
        (is (= :independent-b14-evidence-input-reconstruction (:missing-fact data)))
        (is (not-any? #(= (:kind correct) (:kind %)) @digests))))))

(deftest execution-exceptions-propagate-without-host-fallback
  (let [failure (ex-info "Rejected current-source execution" {:id "TEST-EXECUTION-REJECTED"})]
    (with-observations observed-toolchain
      #(with-redefs-fn
         {(native-var "p15-s23-b3-llvm-linux-differential-binding!") (fn [_] @plan)
          #'bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] (throw failure))}
         (fn []
           (is (identical? failure
                           (try (construct-artifact) nil
                                (catch clojure.lang.ExceptionInfo error error)))))))))
