(ns gravity.self-hosting.sh07-b51-attempt32-runtime-admission-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]
            [gravity.tools.sh07-b51-attempt32-tier0-materializer :as tier0]
            [gravity.tools.sh07-b51-git-evidence :as git-evidence])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(defn- v9-sha [c]
  (str "sha256:" (apply str (repeat 64 c))))

(defn- rejection-reason
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:reason (ex-data error)))))

(deftest obsolete-same-jvm-materializer-publication-is-unreachable
  (let [namespace-symbol 'gravity.tools.sh07-b51-attempt32-materialize
        publics (ns-publics namespace-symbol)
        source
        (slurp
         "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj")
        retired-publics
        '[validate-snapshot! reload-validate-output! publication-bundle
          materialize-and-publish! -main]
        forbidden-source-tokens
        ["gravity.tools.sh07-b51-attempt32-checker-identity"
         "gravity.tools.sh07-b51-attempt32-tier0-materializer"
         "gravity.tools.sh07-b51-git-evidence"
         "gravity.tools.sh07-b51-attempt32-coordinator-child"
         "gravity.tools.sh07-b51-attempt32-checker-child"
         "materialize-current"
         "validate-reloaded-evidence!"
         "ProcessBuilder"
         "Files/readAllBytes"
         "Files/write"]]
    (testing "the materializer is a pure M-loaded semantic leaf"
      (is (= #{'codec} (set (keys (ns-aliases namespace-symbol)))))
      (is (every? #(not (contains? publics %)) retired-publics))
      (is (every? #(not (.contains source ^CharSequence %))
                  forbidden-source-tokens)))
    (testing "Git-object launch and publication remain owned by L"
      (is (fn? git-evidence/coordinator-argv))
      (is (fn? git-evidence/launch-coordinator-transport!))
      (is (fn? git-evidence/publish-new-evidence!))
      (is (fn? git-evidence/verify-existing-object-replay!)))))

(deftest git-evidence-owns-the-exact-coordinator-argv
  (let [content {:commit (apply str (repeat 40 "1"))
                 :tree (apply str (repeat 40 "2"))}
        eligibility-coordinate
        {:schema :gravity/sh07-b51-external-review-object-v1
         :commit (apply str (repeat 40 "3"))
         :tree (apply str (repeat 40 "4"))
         :parents [(:commit content)]
         :path git-evidence/eligibility-receipt-path
         :mode "100644"
         :type "blob"
         :blob-oid (apply str (repeat 40 "5"))
         :byte-count 17
         :raw-sha256 (v9-sha "6")
         :receipt-id (v9-sha "7")}
        classpath ["/tmp/sh07-v9-project"
                   "/tmp/sh07-v9-clojure.jar"
                   "/tmp/sh07-v9-core-specs.jar"
                   "/tmp/sh07-v9-spec.jar"]
        staging-root
        (Files/createTempDirectory
         "sh07-v9-runtime-admission-" (make-array FileAttribute 0))
        java-path
        (str (.normalize
              (.toAbsolutePath
               (java.nio.file.Paths/get
                (System/getProperty "java.home")
                (into-array String ["bin" "java"])))))
        argv
        (git-evidence/coordinator-argv
         {:classpath classpath
          :content content
          :staging-root staging-root
          :eligibility-coordinate eligibility-coordinate})]
    (is (= 39 (count argv)))
    (is (= [java-path "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
            "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
            "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
            "-Duser.home=/nonexistent" "-cp"
            (clojure.string/join java.io.File/pathSeparator classpath)
            "clojure.main" "-m"
            "gravity.tools.sh07-b51-attempt32-coordinator-child"
            "--snapshot-commit" (:commit content)
            "--snapshot-tree" (:tree content)
            "--completed-at" "1970-01-01T00:00:00Z"
            "--evidence-relative-root" git-evidence/evidence-relative-root
            "--staging-root" (str staging-root)
            "--eligibility-review-commit" (:commit eligibility-coordinate)
            "--eligibility-review-tree" (:tree eligibility-coordinate)
            "--eligibility-receipt-path" (:path eligibility-coordinate)
            "--eligibility-receipt-blob" (:blob-oid eligibility-coordinate)
            "--eligibility-receipt-byte-count" "17"
            "--eligibility-receipt-raw-sha256"
            (:raw-sha256 eligibility-coordinate)
            "--eligibility-receipt-id" (:receipt-id eligibility-coordinate)]
           argv))
    (is (not-any? #{"--mode" "--publication-worktree"
                    "--existing-evidence-commit"
                    "--existing-evidence-tree"}
                  argv))
    (testing "the external-review object coordinate is an exact closed schema"
      (let [invoke
            (fn [coordinate]
              (git-evidence/coordinator-argv
               {:classpath classpath
                :content content
                :staging-root staging-root
                :eligibility-coordinate coordinate}))
            mutations
            [(assoc eligibility-coordinate :schema
                    :gravity/sh07-b51-evidence-eligibility-coordinate-v1)
             (dissoc eligibility-coordinate :receipt-id)
             (assoc eligibility-coordinate :candidate-tree
                    (apply str (repeat 40 "0")))
             (assoc eligibility-coordinate :parents [])
             (assoc eligibility-coordinate :path "foreign.edn")
             (assoc eligibility-coordinate :mode "100755")
             (assoc eligibility-coordinate :type "tree")
             (assoc eligibility-coordinate :byte-count -1)
             (assoc eligibility-coordinate :raw-sha256 "sha256:bad")
             (assoc eligibility-coordinate :receipt-id "sha256:bad")]]
        (doseq [coordinate mutations]
          (is (= :eligibility-coordinate
                 (rejection-reason #(invoke coordinate)))))))))

(deftest runtime-carrier-pair-join-is-role-neutral-and-witness-exact
  (let [common {:architecture-report-sha256 (v9-sha "0")
                :claim-id (v9-sha "1") :claim {:census :exact}
                :assumptions {:closed true} :inputs {:snapshot :fixed}
                :profile-bounds {:bounded true}
                :semantic-witness {:witness-id (v9-sha "2")}}
        checker-a {:source-sha256 (v9-sha "3")
                   :semantic-id (v9-sha "4")
                   :executable-id (v9-sha "5")
                   :dependency-closure-id (v9-sha "6")}
        checker-b {:source-sha256 (v9-sha "7")
                   :semantic-id (v9-sha "8")
                   :executable-id (v9-sha "9")
                   :dependency-closure-id (v9-sha "a")}
        a (merge common {:role :producer-census :checker checker-a
                         :certificate-id (v9-sha "b")})
        b (merge common {:role :independent-verifier-census
                         :checker checker-b :certificate-id (v9-sha "c")})
        validator-var
        #'bootstrap/p15-s23-sh07-b51-compile-census-certificate-v1-valid?]
    (with-redefs-fn
      {validator-var (constantly true)}
      #(do
         (is (true?
              (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
               [a b])))
         (is (false?
              (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
               [b a])))
         (is (false?
              (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
               [a (assoc-in b [:semantic-witness :witness-id]
                            (v9-sha "d"))])))))))

(deftest runtime-rejection-observation-cannot-commit-row-or-output
  (let [bad-actual
        {:status :rejected :code "SH07-B51-EDGE-AUTHORITY"
         :rule "L2-UNKNOWN-CORE-FORM"
         :reason :unauthenticated-b51-external-link
         :input-id (v9-sha "1") :mutated-id (v9-sha "2")
         :expected {:module-ordinal 1} :actual {:module-ordinal 2}
         :committed-row nil :output nil}]
    (is (= bad-actual (tier0/validate-bad-actual! bad-actual)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (tier0/validate-bad-actual!
                  (assoc bad-actual :committed-row :forged))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (tier0/validate-bad-actual!
                  (assoc bad-actual :output :forged))))))

(defn- private-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "missing Attempt32 runtime seam" {:symbol symbol}))))

(defn- synthetic-authority []
  (let [manifest-id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        instruction
        {:op :function-call
         :function 'callee
         :args []
         :external-link
         {:artifact :gravity/sh07-b51-external-call-v1
          :manifest-id manifest-id
          :edge-ordinal 0}}
        caller-plan
        {:plan-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
         :sh07-b51-module-source-id "caller-source"
         :functions {'caller {:name 'caller :arity 0
                              :instructions [instruction]}}}
        callee-plan
        {:plan-id "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
         :sh07-b51-module-source-id "callee-source"
         :functions {'callee {:name 'callee :arity 0 :instructions []}}}
        descriptors [{:module-source-id "caller-source"}
                     {:module-source-id "callee-source"}]
        manifest
        {:manifest-id manifest-id
         :edges [{:ordinal 0
                  :caller-module-source-id "caller-source"
                  :caller-function 'caller
                  :callee-module-source-id "callee-source"
                  :callee-function 'callee
                  :arity 0}]}
        plans [caller-plan callee-plan]
        bundle
        {:bundle-id
         "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
         :routes [{:ordinal 0
                   :caller-plan-id (:plan-id caller-plan)
                   :callee-plan-id (:plan-id callee-plan)}]}
        products {:module-source-descriptors descriptors
                  :link-manifest manifest
                  :module-plans plans
                  :execution-bundle bundle}
        carrier {:artifact :synthetic-attempt32-carrier}]
    {:authority {:carrier carrier :products products}
     :carrier carrier
     :products products
     :caller-plan caller-plan
     :callee-plan callee-plan
     :instruction instruction
     :raw {:v18-executable-descriptor carrier}}))

(deftest one-admission-loads-authority-once-and-runtime-checks-never-rebuild
  (let [{:keys [authority carrier products caller-plan callee-plan
                instruction raw]}
        (synthetic-authority)
        events (atom [])
        authority-loads (atom 0)
        admitted-runtime (atom nil)
        preflight-var
        (private-var 'p15-s23-sh07-b51-supplied-raw-v4-preflight?)
        load-var (private-var 'p15-s23-sh07-b51-load-local-authority-v4)
        equality-var
        (private-var 'p15-s23-sh07-b51-v4-bounded-carrier-equal?)
        joins-var (private-var 'p15-s23-sh07-b51-v4-raw-joins-valid?)
        carrier-valid-var
        (private-var 'p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?)
        route-var (private-var 'p15-s23-sh07-b51-v4-route-and-arity)
        register-var
        (private-var 'p15-s23-sh07-b51-register-admitted-runtime-context)
        execute-var
        (private-var 'p15-s23-stage2-runtime-execute-function-scoped)
        original-register @register-var
        result
        (with-redefs-fn
          {preflight-var
           (fn [_] (swap! events conj :preflight) true)
           load-var
           (fn []
             (swap! authority-loads inc)
             (swap! events conj :local-authority)
             authority)
           equality-var
           (fn [supplied current]
             (swap! events conj :equality)
             (identical? supplied current))
           joins-var
           (fn [_] (swap! events conj :joins) true)
           carrier-valid-var
           (fn [_] (swap! events conj :carrier-validity) true)
           route-var
           (fn [_ _ _]
             (swap! events conj :route-and-arity)
             {:root {:root-ordinal 2 :function 'caller :arity 0}
              :plan caller-plan})
           register-var
           (fn [linked-products linked-carrier supplied-raw route args runtime]
             (swap! events conj :token)
             (let [lease
                   (original-register linked-products linked-carrier
                                      supplied-raw route args runtime)]
               (reset! admitted-runtime (:runtime lease))
               lease))
           execute-var
           (fn [runtime plan function args public-invocation-scope]
             (swap! events conj :invocation)
             (is (nil? public-invocation-scope))
             (dotimes [_ 32]
               (is (true?
                    (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
                     runtime plan)))
               (is (true?
                    (bootstrap/p15-s23-sh07-b51-runtime-admitted-products-match?
                     runtime carrier
                     (:module-source-descriptors products)
                     (:link-manifest products)
                     (:module-plans products)
                     (:execution-bundle products))))
               (is (= callee-plan
                      (:callee-plan
                       (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
                        runtime plan instruction)))))
             {:function function :args args :status :executed})}
          #(bootstrap/p15-s23-sh07-b51-execute-linked-raw-v4-public-root
            raw 1 [] {:caller-token :must-not-authorize}))]
    (is (= {:function 'caller :args [] :status :executed} result))
    (is (= 1 @authority-loads))
    (is (= [:preflight :local-authority :equality :joins
            :carrier-validity :route-and-arity :token :invocation]
           @events))
    (is (map? @admitted-runtime))))

(deftest admitted-runtime-token-is-unique-and-bound-to-the-exact-context
  (let [{:keys [authority carrier products caller-plan instruction]}
        (synthetic-authority)
        register
        @(private-var 'p15-s23-sh07-b51-register-admitted-runtime-context)
        raw {:v18-executable-descriptor carrier}
        route {:root {:root-ordinal 2 :function 'caller :arity 0}
               :plan caller-plan}
        first-lease (register products carrier raw route [] {})
        second-lease (register products carrier raw route [] {})
        first-runtime (:runtime first-lease)
        second-runtime (:runtime second-lease)
        stolen-token-clone
        (into {} first-runtime)
        mutations
        [(assoc first-runtime :sh07-b51-runtime-context-token (Object.))
         (assoc first-runtime :sh07-b51-link-manifest {})
         (assoc first-runtime :sh07-b51-module-plans [])
         (assoc first-runtime :caller-mutation true)
         stolen-token-clone]
        invocations (atom 0)
        external-machine-var
        (private-var 'p15-s23-sh07-b51-runtime-execute-external-machine)]
    (is (not (identical?
              (:sh07-b51-runtime-context-token first-runtime)
              (:sh07-b51-runtime-context-token second-runtime))))
    (is (not (identical? first-runtime stolen-token-clone)))
    (is (true?
         (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
          first-runtime caller-plan)))
    (doseq [runtime mutations]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
            runtime caller-plan)))
      (with-redefs-fn
        {external-machine-var
         (fn [& _] (swap! invocations inc) :must-not-run)}
        #(is (thrown? clojure.lang.ExceptionInfo
                      (bootstrap/p15-s23-stage2-runtime-execute-instruction
                       runtime caller-plan {} instruction)))))
    (is (zero? @invocations))
    (is (empty? @(:sh07-b51-external-call-trace first-runtime)))
    ((deref (private-var
             'p15-s23-sh07-b51-unregister-admitted-runtime-context!))
     first-runtime)
    ((deref (private-var
             'p15-s23-sh07-b51-unregister-admitted-runtime-context!))
     second-runtime)))

(deftest public-compatibility-context-cannot-attach-an-invocation-token
  (let [{:keys [products caller-plan]} (synthetic-authority)
        loads (atom 0)
        load-var (private-var 'p15-s23-sh07-b51-load-local-authority-v4)
        matching
        (with-redefs-fn
          {load-var (fn [] (swap! loads inc) nil)}
          #(bootstrap/p15-s23-sh07-b51-runtime-context products {}))
        supplied
        (with-redefs-fn
          {load-var (fn [] (swap! loads inc) nil)}
          #(bootstrap/p15-s23-sh07-b51-runtime-context
            (assoc products :caller-supplied true)
            {:sh07-b51-runtime-context-token (Object.)}))]
    (testing "the compatibility path cannot even enter local authority"
      (is (zero? @loads)))
    (is (nil? (:sh07-b51-runtime-context-token matching)))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
          matching caller-plan)))
    (is (nil? (:sh07-b51-runtime-context-token supplied)))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
          supplied caller-plan)))))
