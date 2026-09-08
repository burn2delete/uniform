(ns gravity.self-hosting.sh07-b51-attempt28-acceptance-support
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(def public-root-symbols
  '[sh07-b51-build-template
    sh07-b51-verify-template
    sh07-b51-resolve-digest-preimage
    sh07-b51-resolve-template
    sh07-b51-verify-resolved
    sh07-b51-build-independent-verifier-binding
    sh07-b51-build-final-artifact
    sh07-b51-finalize-rejection])

(def predecessor-roots
  '[sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved])

(def fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b51-vector-destructuring-v18")

(defn current-carrier []
  (bootstrap/p15-s23-sh07-b51-current-linked-carrier-v3))

(defn current-products []
  (let [carrier (current-carrier)]
    {:artifact :gravity/sh07-b51-linked-plan-products-v1
     :module-source-descriptors (:module-source-descriptors carrier)
     :link-manifest (:link-manifest carrier)
     :module-plans (mapv :plan (:module-plan-carriers carrier))
     :execution-bundle (:execution-bundle carrier)}))

(defn- operation-records [plan function]
  (letfn [(walk [value]
            (cond
              (vector? value) (vec (mapcat walk value))
              (and (map? value) (= :literal (:op value))) []
              (map? value)
              (let [own
                    (case (:op value)
                      :function-call
                      [{:op :function-call :callee (:function value)}]
                      :builtin-call
                      [{:op :builtin-call :builtin (:function value)}]
                      [])]
                (into own (mapcat walk (vals value))))
              :else []))]
    (walk (get-in plan [:functions function]))))

(defn- predecessor-closure [plan]
  (loop [pending (vec (reverse predecessor-roots))
         members #{}
         edges #{}
         builtins #{}]
    (if (empty? pending)
      {:members members :edges edges :builtins builtins}
      (let [function (peek pending)
            remaining (pop pending)]
        (if (contains? members function)
          (recur remaining members edges builtins)
          (let [operations (operation-records plan function)
                callees (keep #(when (= :function-call (:op %))
                                 (:callee %))
                              operations)
                called-builtins
                (keep #(when (= :builtin-call (:op %))
                         (:builtin %))
                      operations)]
            (when-not (every? #(contains? (:functions plan) %) callees)
              (throw
               (ex-info "immutable B47 closure contains an unresolved call"
                        {:id "SH07-B51-ATTEMPT28-B47-CLOSURE"
                         :function function})))
            (recur (into remaining (reverse callees))
                   (conj members function)
                   (into edges (map #(vector function %) callees))
                   (into builtins called-builtins))))))))

(def predecessor-binding
  (delay
    (let [binding (bootstrap/sh07-core-build-binding!)
          closure (predecessor-closure (:plan binding))]
      (when-not (= [305 298 849 32]
                   [(count (get-in binding [:plan :functions]))
                    (count (:members closure))
                    (count (:edges closure))
                    (count (:builtins closure))])
        (throw
         (ex-info "immutable B47 census changed"
                  {:id "SH07-B51-ATTEMPT28-B47-CENSUS"
                   :observed
                   [(count (get-in binding [:plan :functions]))
                    (count (:members closure))
                    (count (:edges closure))
                    (count (:builtins closure))]})))
      (assoc binding :closure closure))))

(defn- predecessor-executable-descriptor []
  (let [closure (:closure @predecessor-binding)]
    {:domain :gravity/sh07-b51-predecessor-executable-closure-v18
     :schema-version 18
     :base-commit "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
     :source-relative-path
     "bootstrap/gravity/src/gravity/checked_core.gravity"
     :roots predecessor-roots
     :root-symbols-hash
     "sha256:d92f6a8991c28e6514e2df9f7b4ca6aeb0baccf9f75cdce47779fd0337c9667b"
     :plan-function-count 305
     :function-member-count 298
     :function-names-hash
     "sha256:aba0868e849b735529ded667ba6aadda30451c8af67dd9686abfdb2d7a97b1fc"
     :function-shapes-hash
     "sha256:79509cc6a17101d22220b3f60278d77547612ab49f6b57d719d463f5cefa5500"
     :selected-functions-hash
     "sha256:34bacc392d81d22757dd7d8f75d82a4ff9ddc55dbe8ececc79daa294496283b1"
     :edge-count 849
     :edge-carrier (:edges closure)
     :edge-carrier-kind :canonical-sorted-set-of-pair-vectors
     :edge-hash
     "sha256:5f7a87d2a3fc9b43469c5a46109f7b1d8edd1860631d20508d6222a2c2f465ef"
     :roots-members-functions-descriptor-hash
     "sha256:13a9131f49c09fd8c62c365fe68442cfee82cc34cdf73679dcf5c90fe46cdb1c"
     :referenced-builtin-name-count 32
     :referenced-builtin-names-hash
     "sha256:c8769b21181d28b6129f6c4f8e2c42bf0860381d0005f67c63f1ef30e844a28e"
     :contract-form-names-hash
     "sha256:20169a279466cae667cb1e41783b862c9f6dd9fe067574e170282846868f5e4a"
     :contract-form-reader-hashes
     ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
      "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
      "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
     :contract-form-reader-vector-hash
     "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
     :contract-core-hash
     "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
     :contract-bounds-hash
     "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
     :contract-diagnostics-hash
     "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"
     :combined-contracts-hash
     "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"
     :filtered-module-header-export-hash
     "sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f"
     :executable-closure-descriptor-id
     "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"
     :excluded-plan-members
     '[sh07-find-alias sh07-lower-top-level-forms
       sh07-resolve-match-branch-vector
       sh07-resolve-match-decision-skeleton-vector
       sh07-resolve-match-pattern-vector sh07-scope-binding-ids
       sh07-utf8-byte-width]}))

(defn- predecessor-contract-carrier []
  {:domain :gravity/sh07-b51-predecessor-contract-v18
   :schema-version 18
   :adapter-contract :gravity/sh07-to-c6-core-products-v16
   :identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
   :actual-core-artifact-kind :gravity/sh07-canonical-core-artifact
   :actual-wrapper-artifact-kind nil
   :request-artifact :gravity/sh07-authenticated-sh06-core-request
   :request-schema-version 15
   :lowering-rule :sh07-b47-function-call-recursion-products
   :entrypoints predecessor-roots
   :source-contract-forms
   {:contract-form-count 3
    :contract-form-names
    '[sh07-core-contract sh07-core-bounds sh07-core-diagnostic-catalog]
    :contract-form-reader-hashes
    ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
     "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
     "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
    :contract-form-reader-vector-hash
    "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
    :contract-value-hashes-by-name
    {'sh07-core-bounds
     "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
     'sh07-core-contract
     "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
     'sh07-core-diagnostic-catalog
     "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"}
    :combined-contracts-hash
    "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"}
   :executable-closure-descriptor-id
   "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"})

(defn- predecessor-observation [resolution request]
  (let [source-path (get-in request [:provenance :actual-source-path])
        direct
        (try
          {:returned
           (bootstrap/sh07-core-execute!
            source-path 'sh07-build-core-template [request])
           :thrown nil}
          (catch clojure.lang.ExceptionInfo exception
            {:returned nil :thrown (ex-data exception)}))
        structural
        (try
          {:returned
           (bootstrap/sh07-core-run-request-for-test resolution request)
           :thrown nil}
          (catch clojure.lang.ExceptionInfo exception
            {:returned nil :thrown (ex-data exception)}))]
    {:direct-template-observation
     {:entrypoint 'sh07-build-core-template
      :request request
      :returned (:returned direct)
      :thrown (:thrown direct)}
     :structural-runner-observation
     {:entrypoint 'sh07-core-run-structural-request-for-test
      :request request
      :returned (:returned structural)
      :thrown (:thrown structural)}}))

(defn linked-raw-carrier
  ([family basename]
   (linked-raw-carrier family basename ".gravity"))
  ([family basename extension]
   (let [source-path (str fixture-root "/" family "/" basename extension)
         source-file (io/file source-path)
         bytes (java.nio.file.Files/readAllBytes (.toPath source-file))
         resolution (bootstrap/sh06-resolution-file-artifact source-path)
         report (bootstrap/sh06-resolution-artifact-verification resolution)
         request (bootstrap/sh07-core-authenticated-request resolution)
         carrier
         {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v3
          :schema-version 3
          :authenticated-sh06-request request
          :authenticated-sh06-verification-report report
          :sh06-resolution-artifact resolution
          :predecessor-executable-descriptor
          (predecessor-executable-descriptor)
          :v18-executable-descriptor (current-carrier)
          :predecessor-contract-carrier (predecessor-contract-carrier)
          :predecessor-raw-outcome
          (predecessor-observation resolution request)
          :physical-invocation
          {:source-path source-path
           :source-byte-count (alength bytes)
           :source-content-hash
           (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
           :session-id "sh07-b51-attempt28-acceptance"
           :attempt-id "sh07-b51-attempt28-public-roots"
           :invocation-id
           (str "sh07-b51-attempt28-" family "-" basename extension)}
          :scope :sh07-b51-vector-destructuring}]
     (when-not (bootstrap/p15-s23-sh07-b51-linked-raw-v3-authorized?
                carrier)
       (throw
        (ex-info "linked-v3 raw carrier failed host authority"
                 {:id "SH07-B51-ATTEMPT28-RAW-AUTHORITY"})))
     carrier)))

(defn runtime []
  (bootstrap/p15-s23-sh07-b51-runtime-context
   (current-products)
   {:engine :gravity-sh07-b51-attempt28-public-roots
    :compiler-artifact-plan? true}))

(defn root-plan [root-ordinal]
  (let [products (current-products)
        bundle (:execution-bundle products)
        root (when (and (integer? root-ordinal)
                        (<= 1 root-ordinal 8))
               (nth (:public-roots bundle) (dec root-ordinal) nil))
        plan (when root
               (first (filter #(= (:plan-id root) (:plan-id %))
                              (:module-plans products))))]
    (when-not (and root plan
                   (= (nth public-root-symbols (dec root-ordinal))
                      (:function root)))
      (throw
       (ex-info "M3 public root is unavailable"
                {:id "SH07-B51-ATTEMPT28-PUBLIC-ROOT"
                 :root-ordinal root-ordinal})))
    {:root root :plan plan}))

(defn execute-root-with-trace [root-ordinal arguments]
  (let [products (current-products)
        carrier (current-carrier)
        {:keys [root plan]} (root-plan root-ordinal)
        execution-runtime
        (bootstrap/p15-s23-sh07-b51-runtime-context
         products
         {:engine :gravity-sh07-b51-attempt28-public-roots
          :compiler-artifact-plan? true})]
    (when-not (and (vector? arguments)
                   (= (:arity root) (count arguments))
                   (bootstrap/p15-s23-sh07-b51-execution-bundle-authorized?
                    (:module-source-descriptors products)
                    (:link-manifest products)
                    (:module-plans products)
                    (:execution-bundle products))
                   (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
                    carrier
                    (:module-source-descriptors products)
                    (:link-manifest products)
                    (:module-plans products)
                    (:execution-bundle products)))
      (throw
       (ex-info "unauthenticated Attempt28 root invocation"
                {:id "SH07-B51-ATTEMPT28-ROOT-AUTHORITY"
                 :root-ordinal root-ordinal})))
    (let [result
          (bootstrap/p15-s23-stage2-runtime-execute-function
           execution-runtime plan (:function root) arguments)]
      {:root root
       :arguments arguments
       :result result
       :trace @(:sh07-b51-external-call-trace execution-runtime)})))

(defn resolve-digests-through-root3 [digest-requests]
  (loop [remaining digest-requests
         resolved []
         executions []]
    (if (empty? remaining)
      {:resolved-digests resolved :executions executions}
      (let [request (first remaining)
            execution
            (execute-root-with-trace
             3 [request digest-requests resolved])
            envelope (:result execution)]
        (when-not (= [:accepted :digest-preimage-resolved]
                     [(:status envelope) (:tag envelope)])
          (throw
           (ex-info "Root3 rejected an authenticated digest request"
                    {:id "SH07-B51-ATTEMPT28-ROOT3"
                     :request request :envelope envelope})))
        (recur (rest remaining)
               (conj resolved
                     (bootstrap/reader-canonical-hash
                      (get-in envelope [:value :hash-input])))
               (conj executions execution))))))

(defn execute-success-pipeline [raw]
  (let [root1 (execute-root-with-trace 1 [raw])
        built (:result root1)]
    (when-not (= [:accepted :template-built]
                 [(:status built) (:tag built)])
      (throw
       (ex-info "Root1 did not admit the success fixture"
                {:id "SH07-B51-ATTEMPT28-ROOT1"
                 :result built})))
    (let [template (get-in built [:value :template])
          requests (get-in built [:value :digest-requests])
          root2 (execute-root-with-trace 2 [raw template requests])
          root3 (resolve-digests-through-root3 requests)
          resolved (:resolved-digests root3)
          root4 (execute-root-with-trace 4 [template requests resolved])
          resolved-value (:value (:result root4))
          core (:resolved-core resolved-value)
          observation (:predecessor-observation resolved-value)
          provenance (:provenance-binding resolved-value)
          root5 (execute-root-with-trace 5 [raw core requests resolved])
          root6
          (execute-root-with-trace
           6 [raw core observation provenance requests resolved])
          verifier-binding (:value (:result root6))
          root7
          (execute-root-with-trace
           7 [raw core observation provenance verifier-binding
              requests resolved])]
      {:raw-carrier raw
       :root1 root1 :root2 root2 :root3 root3 :root4 root4
       :root5 root5 :root6 root6 :root7 root7
       :template template :digest-requests requests
       :resolved-digests resolved :resolved-core core
       :predecessor-observation observation
       :provenance-binding provenance
       :verifier-binding verifier-binding})))

(def acceptance-fixture
  (delay
    (let [success-raw
          (linked-raw-carrier "accepted" "nested-empty-wildcard-let")
          success-pipeline (execute-success-pipeline success-raw)
          success-root8 (execute-root-with-trace 8 [success-raw])
          pending-raw
          (linked-raw-carrier "rejected" "unsupported-vector-rest")
          pending-root8 (execute-root-with-trace 8 [pending-raw])
          malformed-root8 (execute-root-with-trace 8 [{}])]
      {:success-raw success-raw
       :success-pipeline success-pipeline
       :success-root8 success-root8
       :pending-raw pending-raw
       :pending-root8 pending-root8
       :malformed-root8 malformed-root8})))
