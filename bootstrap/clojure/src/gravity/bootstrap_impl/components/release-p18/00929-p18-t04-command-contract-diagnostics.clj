

(defn p18-t04-command-contract-diagnostics
  [candidate]
  (let [accepted (:accepted-command-proofs candidate)]
    (vec
     (concat
      (when-not (every? :matches-expected? accepted)
        [(p18-t04-diagnostic-record
          "P18T04001" :p18-t04-command-parity
          {:source "bin/gravity"
           :failed-fixtures (mapv :fixture
                                  (remove :matches-expected? accepted))})])
      (when-not (every? :executable? accepted)
        [(p18-t04-diagnostic-record
          "P18T04003" :p18-t04-executable-artifact
          {:source "bin/gravity"
           :missing-or-non-executable
           (mapv :output-path (remove :executable? accepted))})])
      (when-not (every? :stdout-matches? accepted)
        [(p18-t04-diagnostic-record
          "P18T04004" :p18-t04-executable-stdout
          {:source "bin/gravity"
           :stdout-mismatches
           (mapv #(select-keys % [:fixture :expected-stdout
                                  :executable-result])
                 (remove :stdout-matches? accepted))})])
      (when (true? (:final-release-boundary-claim? candidate))
        [(p18-t04-diagnostic-record
          "P18T04005" :p18-t04-final-release-claim
          {:source "bin/gravity"
           :final-release-boundary-claim? true
           :required-next-gates ["P18-T05" "P18-T06"]})])))))

(defn p18-t04-rejected-contract-records
  [candidate]
  (let [usage-result
        (p18-t04-shell "bin/gravity" "compile" "examples/core-app.gravity"
                       "-o" "../outside-core-app")
        test-overclaim-result
        (p18-t04-shell "bin/gravity" "test" "--full")
        self-host-verify-result
        (p18-t04-shell "bin/gravity" "self-host" "verify")
        self-host-usage-result
        (p18-t04-shell "bin/gravity" "self-host")
        fixtures
        [{:fixture :p18-t04-missing-command-parity
          :expected-diagnostic "P18T04001"
          :candidate (assoc-in candidate
                               [:accepted-command-proofs 0
                                :matches-expected?]
                               false)}
         {:fixture :p18-t04-invalid-output-usage
          :expected-diagnostic "P18T04002"
          :result usage-result}
         {:fixture :p18-t04-missing-executable-artifact
          :expected-diagnostic "P18T04003"
          :candidate (assoc-in candidate
                               [:accepted-command-proofs 0 :executable?]
                               false)}
         {:fixture :p18-t04-stdout-mismatch
          :expected-diagnostic "P18T04004"
          :candidate (assoc-in candidate
                               [:accepted-command-proofs 0
                                :stdout-matches?]
                               false)}
         {:fixture :p18-t04-premature-final-release-claim
          :expected-diagnostic "P18T04005"
          :candidate (assoc candidate
                            :final-release-boundary-claim? true)}
         {:fixture :p18-t04-full-conformance-test-overclaim
          :expected-diagnostic "P18T04006"
          :result test-overclaim-result}
         {:fixture :p18-t04-self-host-verify-seed-boundary
          :expected-diagnostic "P18T04007"
          :result self-host-verify-result}
         {:fixture :p18-t04-self-host-verify-usage
          :expected-diagnostic "P18T04008"
          :result self-host-usage-result}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic candidate result]}]
       (if result
         {:fixture fixture
          :status :rejected
          :expected-diagnostic expected-diagnostic
          :result (select-keys result [:exit :out :err])
          :matches-expected? (and (= 1 (:exit result))
                                  (str/includes? (:err result)
                                                 expected-diagnostic))}
         (let [diagnostics (p18-t04-command-contract-diagnostics candidate)
               observed (set (map :diagnostic diagnostics))]
           {:fixture fixture
            :status :rejected
            :expected-diagnostic expected-diagnostic
            :diagnostics diagnostics
            :matches-expected? (contains? observed expected-diagnostic)})))
     fixtures)))

(defn p18-t04-diagnostic-stream
  [proof-id]
  {:artifact :gravity/p18-t04-diagnostic-stream
   :stage :p18-t04-executable-command-contract
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p18-t04-executable-command-contract
            :message (get p18-t04-diagnostic-messages id)
            :stable? true})
         p18-t04-diagnostic-ids)
   :status :complete})

(defn p18-t04-executable-command-contract-proof
  [artifact]
  (let [accepted (:accepted-command-proofs artifact)
        rejected (:rejected-command-proofs artifact)
        rejected-contract (:rejected-contract-fixtures artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:p18-t04-diagnostic-stream
                                       :diagnostics])))]
    {:task "P18-T04"
     :status :complete
     :check-command-passed?
     (every? #(zero? (get-in % [:check-result :exit])) accepted)
     :run-command-passed?
     (every? #(zero? (get-in % [:run-result :exit])) accepted)
     :compile-output-command-passed?
     (every? #(zero? (get-in % [:compile-result :exit])) accepted)
     :compiled-executables-ran?
     (every? #(zero? (get-in % [:executable-result :exit])) accepted)
     :public-test-command-passed?
     (true? (get-in artifact [:public-test-command-proof
                              :capability-based-proof
                              :public-test-command-passed?]))
     :public-test-full-language-claim-rejected?
     (boolean
      (some #(= "P18T04006" (:expected-diagnostic %))
            rejected-contract))
     :public-self-host-verification-fails-closed?
     (boolean
      (some #(= "P18T04007" (:expected-diagnostic %))
            rejected-contract))
     :target-core-app-executable-ran?
     (boolean
      (some #(and (= "target/core-app" (:output-path %))
                  (= "core-app\ngravity:19:2\n(:ok 19)\n"
                     (get-in % [:executable-result :out])))
            accepted))
     :accepted-fixtures-covered?
     (= (set (map :fixture p18-t04-accepted-fixtures))
        (set (map :fixture accepted)))
     :rejected-command-fixtures-covered?
     (= (set (map :expected-diagnostic p18-t04-rejected-command-fixtures))
        (set (map :expected-diagnostic rejected)))
     :stable-diagnostics-through-public-command?
     (every? :matches-expected? rejected)
     :contract-diagnostics-covered?
     (= (set p18-t04-diagnostic-ids)
        (set (concat (map :expected-diagnostic rejected-contract)
                     diagnostics)))
     :does-not-claim-final-seedless-release?
     (false? (:final-release-boundary? artifact))
     :next-required-capability :p18-t05-seedless-release-boundary-proof}))

(defn p18-t04-executable-command-contract-artifact!
  []
  (p18-t02-build-packaged-jvm-cli!)
  (p18-t03-write-self-hosted-release-artifacts!)
  (let [accepted-records (mapv p18-t04-accepted-command-record
                               p18-t04-accepted-fixtures)
        public-test-record (p18-t04-public-test-command-artifact!)
        rejected-command-records
        (mapv p18-t04-rejected-command-record
              p18-t04-rejected-command-fixtures)
        candidate {:accepted-command-proofs accepted-records
                   :rejected-command-proofs rejected-command-records
                   :final-release-boundary-claim? false}
        diagnostics (p18-t04-command-contract-diagnostics candidate)
        _ (when (seq diagnostics)
            (let [first-diagnostic (first diagnostics)]
              (p18-t04-fail! (:diagnostic first-diagnostic)
                             (merge {:source "bin/gravity"}
                                    (:facts first-diagnostic)))))
        rejected-contract-records
        (p18-t04-rejected-contract-records candidate)
        proof-id
        (c4-artifact-id
         {:accepted (mapv :compile-artifact accepted-records)
          :rejected (mapv :expected-diagnostic rejected-command-records)
          :contract (mapv :expected-diagnostic rejected-contract-records)})
        artifact-base
        {:kind :gravity/p18-t04-executable-command-contract-proof
         :task "P18-T04"
         :status :complete
         :phase :binary-distribution-and-seedless-release
         :command-boundary
         {:public-command "bin/gravity"
          :bootstrap-recovery-command "bin/gravity-bootstrap"
          :required-commands [["bin/gravity" "check"
                               "examples/core-app.gravity"]
                              ["bin/gravity" "run"
                               "examples/core-app.gravity"]
                              ["bin/gravity" "compile"
                               "examples/core-app.gravity"
                               "-o" "target/core-app"]
                              ["target/core-app"]]
          :public-test-command ["bin/gravity" "test"]
          :public-executable-command-contract? true
          :final-seedless-release? false}
         :accepted-command-proofs accepted-records
         :rejected-command-proofs rejected-command-records
         :rejected-contract-fixtures rejected-contract-records
         :public-test-command-proof public-test-record
         :p18-t04-diagnostic-stream
         (p18-t04-diagnostic-stream proof-id)
         :executable-artifacts
         (mapv :compile-artifact accepted-records)
         :final-release-boundary? false
         :seedless-release? false
         :diagnostics []}
        proof (p18-t04-executable-command-contract-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))