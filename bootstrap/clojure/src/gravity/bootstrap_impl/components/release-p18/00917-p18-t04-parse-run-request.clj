

(defn p18-t04-parse-run-request
  "Parse the public run command while keeping native execution opt-in.

  The historical `run <source>` form deliberately returns a request that the
  existing Clojure-hosted `run-file` path can consume.  The only additional
  form is the explicit pair `--target c --lowering runtime-derived`; option
  order is flexible, but output paths and every other option are rejected
  before the source is read or lowered."
  [args]
  (let [[command source-path & options] (vec args)]
    (when-not (= "run" command)
      (p18-t04-fail!
       "P18T04002"
       {:source "bin/gravity"
        :command args
        :missing-fields [:run-command]}))
    (when-not (and (string? source-path)
                   (not (str/blank? source-path)))
      (p18-t04-fail!
       "P18T04002"
       {:source "bin/gravity"
        :command args
        :missing-fields [:source-path]}))
    (loop [remaining (vec options)
           target-argument nil
           lowering-argument nil]
      (if (empty? remaining)
        (let [target-requested? (some? target-argument)
              lowering-requested? (some? lowering-argument)
              native-requested?
              (and target-requested? lowering-requested?)]
          (when (or (and target-requested?
                         (not= "c" target-argument))
                    (and lowering-requested?
                         (not= "runtime-derived" lowering-argument)))
            (p18-t04-fail!
             "P18T04002"
             {:source source-path
              :command args
              :target target-argument
              :lowering-mode lowering-argument
              :missing-fields [:exact-runtime-derived-c-run-options]
              :remediation
              "Use exactly --target c --lowering runtime-derived."}))
          (when (not= target-requested? lowering-requested?)
            (p18-t04-fail!
             "P18T04002"
             {:source source-path
              :command args
              :target target-argument
              :lowering-mode lowering-argument
              :missing-fields
              (if target-requested?
                [:lowering-mode]
                [:target])
              :remediation
              "The native run route requires both --target c and --lowering runtime-derived."}))
          {:command command
           :source-path source-path
           :source-extension (gravity-source-extension source-path)
           :source-kind (gravity-source-kind source-path)
           :target (when native-requested? :c)
           :target-argument target-argument
           :target-requested? target-requested?
           :lowering-mode (when native-requested? :runtime-derived)
           :lowering-argument lowering-argument
           :lowering-requested? lowering-requested?
           :runtime-derived-requested? native-requested?
           :output-option nil
           :output-path nil})
        (let [option (first remaining)
              rest-options (subvec remaining 1)]
          (cond
            (= "--target" option)
            (do
              (when (or (empty? rest-options) (some? target-argument))
                (p18-t04-fail!
                 "P18T04002"
                 {:source source-path
                  :command args
                  :option option
                  :missing-fields
                  (if (empty? rest-options)
                    [:target]
                    [:duplicate-target-option])}))
              (let [candidate (first rest-options)]
                (when-not (string? candidate)
                  (p18-t04-fail!
                   "P18T04002"
                   {:source source-path
                    :command args
                    :option option
                    :missing-fields [:target]}))
                (recur (subvec rest-options 1)
                       candidate lowering-argument)))

            (= "--lowering" option)
            (do
              (when (or (empty? rest-options) (some? lowering-argument))
                (p18-t04-fail!
                 "P18T04002"
                 {:source source-path
                  :command args
                  :option option
                  :missing-fields
                  (if (empty? rest-options)
                    [:lowering-mode]
                    [:duplicate-lowering-option])}))
              (let [candidate (first rest-options)]
                (when-not (string? candidate)
                  (p18-t04-fail!
                   "P18T04002"
                   {:source source-path
                    :command args
                    :option option
                    :missing-fields [:lowering-mode]}))
                (recur (subvec rest-options 1)
                       target-argument candidate)))

            :else
            (p18-t04-fail!
             "P18T04002"
             {:source source-path
              :command args
              :unsupported-option option
              :expected-forms
              [["run" "<file.qst|file.gravity>"]
               ["run" "<file.qst|file.gravity>"
                "--target" "c" "--lowering" "runtime-derived"]]})))))))

(def p18-t04-verified-mir-c-maximum-source-bytes (* 1024 1024))

(def p18-t04-experimental-verified-mir-c-route-policy
  {:artifact :gravity/p18-t04-experimental-verified-mir-c-route-policy
   :schema-version 1
   :task "P18-T04"
   :implementation-tier :experimental
   :experiment-state :proposed
   :target-support-tier :unassigned
   :exposure :internal-only
   :command-grammar
   ["gravity" "compile" "<file.qst|file.gravity>"
    "--target" "c" "--lowering" "verified-mir"
    "-o" "target/<bundle-directory>"]
   :option-order :flexible
   :option-cardinality :exactly-once
   :output-kind :exclusive-seven-file-bundle-directory
   :source-declaration-target :jvm
   :requested-lowering-target :c
   :profile :hosted
   :lowering-mode :verified-mir
   :nested-capability :bounded-hosted-c17-gate-b
   :source-snapshot-policy
   {:maximum-byte-count p18-t04-verified-mir-c-maximum-source-bytes
    :capture-provider :jdk26-ffm-darwin-libsystem
    :native-functions ["fstatat" "open" "fstat" "fcntl" "read" "close"]
    :open-flags {:o-rdonly 0 :o-cloexec 0x01000000
                 :o-nofollow-any 0x20000000 :combined 0x21000000
                 :variadic-mode 0}
    :path-stat-flags {:at-symlink-nofollow-any 0x0800}
    :descriptor-path-command {:f-getpath 50}
    :identity-observation-phases
    [:path-before-open :opened-descriptor-before-read
     :descriptor-path-before-read :opened-descriptor-after-read
     :descriptor-path-after-read :path-after-read]
    :required-identity-fields
    [:device :inode :mode :byte-count :modified-time
     :changed-time :birth-time]
    :opened-handle-file-key-observation :native-fstat-device-and-inode
    :content-identity :sha256
    :source-byte-path-reopen-count 0}
   :governance-status :pending-feature-specific-review
   :feature-specific-experiment-record-complete? false
   :governance-conforming? false
   :security-review-complete? false
   :unsafe-review-complete? false
   :target-support-record-complete? false
   :t1-cli-conformance? false
   :p18-t04-proof-credited? false
   :governance-blockers
   [:gov4-security-review :gov5-target-support-record
    :gov7-experiment-record :gov9-unsafe-island-review
    :t1-cli-automation-contract]
   :experimental-use-notice
   {:status :implementation-present-public-exposure-disabled
    :activation :blocked-by-governance-authority-gate
    :blocking-diagnostic-order ["GOV7006" "GOV7007" "GOV7001"]
    :replacement :established-bootstrap-compile-routes}
   :public-command-route? false
   :public-target-support-claim? false
   :whole-b2? false
   :release? false
   :self-hosted? false})

(def p18-t04-experimental-verified-mir-c-route-artifact-keys
  #{:kind :schema-version :task :status :route-policy :source
    :source-snapshot-evidence :source-snapshot-evidence-id
    :source-target :requested-target :profile :lowering-mode
    :command-boundary :gate-b-summary :semantic-id :artifact-id
    :actual-path-provenance :actual-path-binding-id :diagnostics
    :governance-status :governance-conforming?
    :security-review-complete? :unsafe-review-complete?
    :target-support-record-complete? :t1-cli-conformance?
    :p18-t04-proof-credited? :experimental-use-notice
    :public-command-route? :public-target-support-claim? :whole-b2?
    :public? :release? :self-hosted? :seed-boundary?
    :clojure-seed-boundary?})

(def p18-t04-verified-mir-c-source-snapshot-evidence-keys
  #{:kind :schema-version :policy-id :actual-path
    :file-key-hash :byte-count :content-hash
    :capture-provider :native-functions
    :identity-observation-phase-count
    :source-byte-path-reopen-count
    :opened-handle-file-key-observation
    :opened-handle-size-parity?
    :path-and-descriptor-identity-parity?
    :native-access-enabled? :status})

(defn p18-t04-repository-root-path
  "Resolve the repository root through the pinned C11 Gravity source, not CWD."
  []
  (let [relative p15-s23-c11-mir-source-relative-path
        pinned (.getCanonicalFile
                (java.io.File. (p15-s23-c11-mir-resolve-source-path)))
        root
        (loop [candidate (.getParentFile pinned)]
          (when candidate
            (if (= pinned
                   (.getCanonicalFile (java.io.File. candidate relative)))
              (.getCanonicalFile candidate)
              (recur (.getParentFile candidate)))))]
    (when-not root
      (p18-t04-fail!
       "P18T04002"
       {:source "bin/gravity"
        :missing-fields [:normalized-repository-root]
        :remediation
        "Run the current-source compiler from a checkout containing the pinned C11 Gravity source."}))
    (.toPath root)))