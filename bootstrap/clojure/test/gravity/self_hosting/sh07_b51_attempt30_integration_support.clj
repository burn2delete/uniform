(ns gravity.self-hosting.sh07-b51-attempt30-integration-support
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test]))

(def public-root-symbols
  '[sh07-b51-build-template
    sh07-b51-verify-template
    sh07-b51-resolve-digest-preimage
    sh07-b51-resolve-template
    sh07-b51-verify-resolved
    sh07-b51-build-independent-verifier-binding
    sh07-b51-build-final-artifact
    sh07-b51-finalize-rejection])

(def fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b51-vector-destructuring-v18")

(def predecessor-roots
  '[sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved])

(defn private-var [namespace symbol]
  (or (ns-resolve namespace symbol)
      (throw (ex-info "missing Attempt30 integration seam"
                      {:namespace namespace :symbol symbol}))))

(defn- host-test-value [symbol]
  (var-get
   (private-var
    'gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
    symbol)))

(defn fixture []
  ;; This is deliberately the provisional local candidate produced by the
  ;; host tests.  Its ids are derived at runtime and are never copied as pins.
  @(host-test-value 'accepted-fixture))

(defn carrier [] (:carrier (fixture)))
(defn products [] (:products (fixture)))
(defn certificates [] (:certificates (fixture)))

(defn with-provisional-authority [thunk]
  (let [candidate (carrier)
        linked-products (products)
        pins (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map candidate)
        bootstrap-ns 'gravity.bootstrap]
    (with-redefs-fn
      {(private-var bootstrap-ns 'p15-s23-sh07-b51-expected-m3-id)
       (:expected-m3-id pins)
       (private-var bootstrap-ns
                    'p15-s23-sh07-b51-expected-certificate-a-id)
       (:expected-certificate-a-id pins)
       (private-var bootstrap-ns
                    'p15-s23-sh07-b51-expected-certificate-b-id)
       (:expected-certificate-b-id pins)
       (private-var bootstrap-ns 'p15-s23-sh07-b51-expected-semantic-root)
       (:expected-semantic-root pins)
       (private-var bootstrap-ns
                    'p15-s23-sh07-b51-authoritative-linked-carrier-value-v4)
       candidate
       (private-var bootstrap-ns
                    'p15-s23-sh07-b51-authoritative-linked-products)
       (delay linked-products)}
      thunk)))

(defn- operation-records [plan function]
  (letfn [(walk [value]
            (cond
              (vector? value) (vec (mapcat walk value))
              (and (map? value) (= :literal (:op value))) []
              (map? value)
              (let [own (case (:op value)
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
         members #{} edges #{} builtins #{}]
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
              (throw (ex-info "immutable B47 closure has an unresolved call"
                              {:function function})))
            (recur (into remaining (reverse callees))
                   (conj members function)
                   (into edges (map #(vector function %) callees))
                   (into builtins called-builtins))))))))

(def ^:private predecessor-binding
  (delay
    (let [binding (bootstrap/sh07-core-build-binding!)
          closure (predecessor-closure (:plan binding))]
      (when-not (= [305 298 849 32]
                   [(count (get-in binding [:plan :functions]))
                    (count (:members closure))
                    (count (:edges closure))
                    (count (:builtins closure))])
        (throw (ex-info "immutable B47 census changed" {})))
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
        observe
        (fn [thunk]
          (try {:returned (thunk) :thrown nil}
               (catch clojure.lang.ExceptionInfo exception
                 {:returned nil :thrown (ex-data exception)})))
        direct (observe #(bootstrap/sh07-core-execute!
                          source-path 'sh07-build-core-template [request]))
        structural
        (observe #(bootstrap/sh07-core-run-request-for-test resolution request))]
    {:direct-template-observation
     {:entrypoint 'sh07-build-core-template :request request
      :returned (:returned direct) :thrown (:thrown direct)}
     :structural-runner-observation
     {:entrypoint 'sh07-core-run-structural-request-for-test :request request
      :returned (:returned structural) :thrown (:thrown structural)}}))

(defn linked-raw-carrier
  ([family basename]
   (linked-raw-carrier family basename ".gravity"))
  ([family basename extension]
   (let [source-path (str fixture-root "/" family "/" basename
                          extension)
         source-file (io/file source-path)
         bytes (java.nio.file.Files/readAllBytes (.toPath source-file))
         resolution (bootstrap/sh06-resolution-file-artifact source-path)
         report (bootstrap/sh06-resolution-artifact-verification resolution)
         request (bootstrap/sh07-core-authenticated-request resolution)]
     {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v4
      :schema-version 4
      :authenticated-sh06-request request
      :authenticated-sh06-verification-report report
      :sh06-resolution-artifact resolution
      :predecessor-executable-descriptor
      (predecessor-executable-descriptor)
      :v18-executable-descriptor (carrier)
      :predecessor-contract-carrier
      (predecessor-contract-carrier)
      :predecessor-raw-outcome
      (predecessor-observation resolution request)
      :physical-invocation
      {:source-path source-path
       :source-byte-count (alength bytes)
       :source-content-hash
       (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
       :session-id "sh07-b51-attempt30-integration"
       :attempt-id "sh07-b51-attempt30-successor"
       :invocation-id
       (str "sh07-b51-attempt30-" family "-" basename extension)}
      :scope :sh07-b51-vector-destructuring})))

(defn malformed-raw []
  ((host-test-value 'raw-request) (carrier)))

(defn root-plan [root-ordinal]
  (let [linked-products (products)
        root (nth (get-in linked-products [:execution-bundle :public-roots])
                  (dec root-ordinal) nil)
        plan (first (filter #(= (:plan-id root) (:plan-id %))
                            (:module-plans linked-products)))]
    (when-not (and root plan
                   (= (nth public-root-symbols (dec root-ordinal))
                      (:function root)))
      (throw (ex-info "Attempt30 public root is unavailable"
                      {:root-ordinal root-ordinal})))
    {:root root :plan plan}))

(defn execute-root-with-trace [raw root-ordinal arguments]
  (let [attach-var
        (private-var 'gravity.bootstrap
                     'p15-s23-sh07-b51-v4-attach-runtime-token)
        original-attach @attach-var
        captured-runtime (atom nil)
        result
        (with-redefs-fn
          {attach-var
           (fn [authority supplied-raw route args runtime]
             (let [lease (original-attach authority supplied-raw
                                          route args runtime)]
               (reset! captured-runtime (:runtime lease))
               lease))}
          #(bootstrap/p15-s23-sh07-b51-execute-linked-raw-v4-public-root
            raw root-ordinal arguments
            {:engine :gravity-sh07-b51-attempt30-integration
             :compiler-artifact-plan? true}))
        runtime @captured-runtime
        {:keys [root plan]} (root-plan root-ordinal)]
    {:root root
     :plan plan
     :arguments arguments
     :result result
     :runtime runtime
     :trace @(:sh07-b51-external-call-trace runtime)}))

(defn resolve-digests-through-root3 [raw digest-requests]
  (loop [remaining digest-requests resolved [] executions []]
    (if (empty? remaining)
      {:resolved-digests resolved :executions executions}
      (let [request (first remaining)
            execution (execute-root-with-trace
                       raw 3 [request digest-requests resolved])
            envelope (:result execution)]
        (when-not (= [:accepted :digest-preimage-resolved]
                     [(:status envelope) (:tag envelope)])
          (throw (ex-info "Root3 rejected an authenticated digest request"
                          {:request request :envelope envelope})))
        (recur (rest remaining)
               (conj resolved
                     (bootstrap/reader-canonical-hash
                      (get-in envelope [:value :hash-input])))
               (conj executions execution))))))

(defn execute-success-pipeline [raw]
  (let [root1 (execute-root-with-trace raw 1 [raw])
        built (:result root1)]
    (when-not (= [:accepted :template-built]
                 [(:status built) (:tag built)])
      (throw (ex-info "Root1 rejected the authentic Attempt30 fixture"
                      {:result built})))
    (let [template (get-in built [:value :template])
          requests (get-in built [:value :digest-requests])
          root2 (execute-root-with-trace raw 2 [raw template requests])
          root3 (resolve-digests-through-root3 raw requests)
          resolved (:resolved-digests root3)
          root4 (execute-root-with-trace raw 4 [template requests resolved])
          resolved-value (:value (:result root4))
          core (:resolved-core resolved-value)
          observation (:predecessor-observation resolved-value)
          provenance (:provenance-binding resolved-value)
          root5 (execute-root-with-trace raw 5
                                         [raw core requests resolved])
          root6 (execute-root-with-trace
                 raw 6 [raw core observation provenance requests resolved])
          verifier-binding (:value (:result root6))
          root7 (execute-root-with-trace
                 raw 7 [raw core observation provenance verifier-binding
                        requests resolved])]
      {:raw raw :root1 root1 :root2 root2 :root3 root3 :root4 root4
       :root5 root5 :root6 root6 :root7 root7
       :template template :digest-requests requests
       :resolved-digests resolved :resolved-core core
       :predecessor-observation observation
       :provenance-binding provenance
       :verifier-binding verifier-binding})))

(defn first-external-instruction []
  (first
   (for [plan (:module-plans (products))
         [_ definition] (:functions plan)
         instruction (tree-seq coll? seq (:instructions definition))
         :when (and (map? instruction)
                    (= :function-call (:op instruction))
                    (map? (:external-link instruction)))]
     {:plan plan :instruction instruction})))
