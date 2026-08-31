

(defn p18-t04-public-test-accepted-record
  [{:keys [fixture expected-stdout]}]
  (let [output-path (p18-t04-public-test-output-path fixture)
        check-result (p18-t04-shell "bin/gravity" "check" fixture)
        run-result (p18-t04-shell "bin/gravity" "run" fixture)
        compile-result (p18-t04-shell "bin/gravity" "compile" fixture
                                      "-o" output-path)
        compile-artifact (p18-t04-read-edn-stdout compile-result)
        executable-result (p18-t04-shell output-path)
        executable-file (java.io.File. output-path)
        stdout-matches? (= expected-stdout (:out executable-result))
        source-path-preserved? (= fixture (get-in compile-artifact
                                                  [:source :path]))
        source-extension-preserved?
        (= (gravity-source-extension fixture)
           (gravity-source-extension (get-in compile-artifact [:source :path])))
        matches-expected? (and (zero? (:exit check-result))
                               (zero? (:exit run-result))
                               (zero? (:exit compile-result))
                               (zero? (:exit executable-result))
                               (.isFile executable-file)
                               (.canExecute executable-file)
                               stdout-matches?
                               source-path-preserved?
                               source-extension-preserved?)]
    {:fixture fixture
     :command-family [:check :run :compile :execute]
     :expected-stdout expected-stdout
     :output-path output-path
     :check-result (select-keys check-result [:exit :out :err])
     :run-result (select-keys run-result [:exit :out :err])
     :compile-result (select-keys compile-result [:exit :out :err])
     :compile-artifact
     (select-keys compile-artifact
                  [:kind :artifact-id :source :executable-path
                   :executable-content-hash :compiled-plan-id])
     :executable-result (select-keys executable-result [:exit :out :err])
     :executable? (and (.isFile executable-file)
                       (.canExecute executable-file))
     :stdout-matches? stdout-matches?
     :source-path-preserved? source-path-preserved?
     :source-extension-preserved? source-extension-preserved?
     :matches-expected? matches-expected?}))

(defn p18-t04-public-test-rejected-record
  [{:keys [fixture command expected-diagnostic]}]
  (let [output-path (p18-t04-public-test-output-path fixture)
        result (case command
                 :check (p18-t04-shell "bin/gravity" "check" fixture)
                 :compile (p18-t04-shell "bin/gravity" "compile" fixture
                                         "-o" output-path))
        diagnostic-present? (str/includes? (:err result) expected-diagnostic)
        source-path-present? (str/includes? (:err result) fixture)
        generic-unsupported? (str/includes? (:err result) "P18T06004")]
    {:fixture fixture
     :command command
     :expected-diagnostic expected-diagnostic
     :result (select-keys result [:exit :out :err])
     :stable-diagnostic-through-public-command? diagnostic-present?
     :source-path-preserved? source-path-present?
     :generic-unsupported? generic-unsupported?
     :matches-expected? (and (= 1 (:exit result))
                             diagnostic-present?
                             source-path-present?
                             (not generic-unsupported?))}))

(defn p18-t04-public-test-command-proof
  [artifact]
  (let [accepted (:accepted-test-proofs artifact)
        rejected (:rejected-test-proofs artifact)]
    {:task "P18-T04"
     :public-test-command-passed? (every? :matches-expected? accepted)
     :accepted-check-run-compile-execute-covered?
     (every? #(= [:check :run :compile :execute] (:command-family %))
             accepted)
     :rejected-stable-diagnostics-covered?
     (every? :matches-expected? rejected)
     :rejected-generic-unsupported-diagnostics? (boolean
                                                 (some :generic-unsupported?
                                                       rejected))
     :co-canonical-source-paths-preserved?
     (and (every? :source-path-preserved? accepted)
          (every? :source-path-preserved? rejected)
          (every? :source-extension-preserved? accepted))
     :bootstrap-hosted? true
     :full-language-conformance? false
     :self-hosted-conformance-runner? false
     :next-required-capability :self_hosted_public_gravity_test_runner}))

(defn p18-t04-public-test-command-artifact!
  []
  (let [accepted (mapv p18-t04-public-test-accepted-record
                       p18-t04-public-test-accepted-fixtures)
        rejected (mapv p18-t04-public-test-rejected-record
                       p18-t04-public-test-rejected-fixtures)
        artifact-base
        {:kind :gravity/p18-t04-public-test-command-proof
         :task "P18-T04"
         :status :complete
         :phase :binary-distribution-and-seedless-release
         :command ["gravity" "test"]
         :scope :current-public-release-surface
         :suite-id :gravity-public-bootstrap-conformance-subset
         :governing-documents ["TEST1" "TEST13" "T1" "D9"]
         :accepted-test-proofs accepted
         :rejected-test-proofs rejected
         :accepted-count (count accepted)
         :rejected-count (count rejected)
         :bootstrap-hosted? true
         :packaged-jvm-cli? (p18-packaged-jvm-cli?)
         :full-language-conformance? false
         :self-hosted-conformance-runner? false
         :unsupported-full-suite-claim
         {:command ["gravity" "test" "--full"]
          :diagnostic "P18T04006"
          :reason :bootstrap_public_subset_only}
         :diagnostics []}
        proof (p18-t04-public-test-command-proof artifact-base)
        artifact (assoc artifact-base
                        :capability-based-proof proof
                        :artifact-id
                        (c4-artifact-id
                         (assoc artifact-base
                                :capability-based-proof proof)))]
    (when-not (:public-test-command-passed? proof)
      (p18-t04-fail!
       "P18T04001"
       {:source "gravity test"
        :failed-fixtures (mapv :fixture (remove :matches-expected?
                                                accepted))}))
    (when-not (:rejected-stable-diagnostics-covered? proof)
      (p18-t04-fail!
       "P18T04001"
       {:source "gravity test"
        :failed-fixtures (mapv :fixture (remove :matches-expected?
                                                rejected))}))
    artifact))

(defn p18-t04-write-public-test-command-artifacts!
  []
  (let [artifact (p18-t04-public-test-command-artifact!)]
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-public-test-command-proof.edn")
     artifact)
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-public-test-accepted-proofs.edn")
     (:accepted-test-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-public-test-rejected-proofs.edn")
     (:rejected-test-proofs artifact))
    artifact))

(defn p18-t04-public-test-overclaim!
  [args]
  (p18-t04-fail!
   "P18T04006"
   {:source "gravity test"
    :command (vec args)
    :bootstrap-hosted? true
    :full-language-conformance? false
    :remediation "Run `gravity test` for the current public bootstrap subset, or complete the self-hosted conformance runner before claiming full language conformance."}))

(defn p18-t04-accepted-command-record
  [{:keys [fixture output-path expected-stdout]}]
  (let [expected (or expected-stdout (run-compiled-file fixture))
        check-result (p18-t04-shell "bin/gravity" "check" fixture)
        run-result (p18-t04-shell "bin/gravity" "run" fixture)
        compile-result (p18-t04-shell "bin/gravity" "compile" fixture
                                      "-o" output-path)
        compile-artifact (p18-t04-read-edn-stdout compile-result)
        executable-result (p18-t04-shell output-path)
        executable-file (java.io.File. output-path)
        executable? (and (.isFile executable-file)
                         (.canExecute executable-file))
        stdout-matches? (= expected (:out executable-result))]
    {:fixture fixture
     :output-path output-path
     :status (if (and (zero? (:exit check-result))
                      (zero? (:exit run-result))
                      (zero? (:exit compile-result))
                      (zero? (:exit executable-result))
                      executable?
                      stdout-matches?)
               :accepted
               :failed)
     :expected-stdout expected
     :check-result (select-keys check-result [:exit :out :err])
     :run-result (select-keys run-result [:exit :out :err])
     :compile-result (select-keys compile-result [:exit :out :err])
     :compile-artifact
     (select-keys compile-artifact
                  [:kind :artifact-id :executable-path
                   :executable-content-hash :compiled-plan-id
                   :release-artifact-id])
     :executable-result (select-keys executable-result [:exit :out :err])
     :executable-exists? (.isFile executable-file)
     :executable? executable?
     :stdout-matches? stdout-matches?
     :matches-expected? (and (zero? (:exit check-result))
                             (zero? (:exit run-result))
                             (zero? (:exit compile-result))
                             (zero? (:exit executable-result))
                             executable?
                             stdout-matches?)}))

(defn p18-t04-rejected-command-record
  [{:keys [fixture category output-path expected-diagnostic]}]
  (let [result (p18-t04-shell "bin/gravity" "compile" fixture "-o"
                              output-path)
        diagnostic-present? (str/includes? (:err result)
                                           expected-diagnostic)]
    {:fixture fixture
     :category category
     :command ["bin/gravity" "compile" fixture "-o" output-path]
     :status :rejected
     :expected-diagnostic expected-diagnostic
     :result (select-keys result [:exit :out :err])
     :stable-diagnostic-through-public-command? diagnostic-present?
     :matches-expected? (and (= 1 (:exit result))
                             diagnostic-present?)}))