(ns gravity.self-hosting.sh07-b51-plan-linker-governed-read-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tools.sh07-b51-v9-codec :as codec]))

(defn- private-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "missing private bootstrap Var" {:name name}))))

(defn- c5-fixture []
  (let [sha (fn [ordinal] (format "sha256:%064x" (inc ordinal)))
        path "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
        bytes (.getBytes "a" java.nio.charset.StandardCharsets/UTF_8)
        descriptors
        (mapv
         (fn [ordinal]
           {:module-ordinal ordinal
            :module (symbol (str "gravity.sh07-b51.fixture-" ordinal))
            :module-source-id (sha ordinal)
            :logical-source-path (if (zero? ordinal) path (str "source-" ordinal))
            :source-sha256 (sha (+ 10 ordinal))
            :source-byte-count (if (zero? ordinal) (alength bytes) 1)
            :owner (if (< ordinal 4) :r1-producer
                       (if (< ordinal 6) :r6-independent-verifier
                           :r8-finalizer))
            :profile :meta :target :jvm :effects #{} :capabilities #{}
            :safety :safe :definitions [] :external-bindings []})
         (range 7))
        caller-definition
        {:name 'caller :arity 0 :visibility :private :source-hash (sha 30)}
        callee-definition
        {:name 'governed-external :arity 2 :visibility :private
         :source-hash (sha 31)}
        callee (-> (nth descriptors 2)
                   (assoc :module 'gravity.sh07-b51.producer-c
                          :definitions [callee-definition]))
        external
        {:symbol 'governed-external
         :callee-module-ordinal 2
         :callee-module (:module callee)
         :callee-source-sha256 (:source-sha256 callee)
         :callee-definition-source-hash (:source-hash callee-definition)
         :arity 2 :owner-edge :producer-internal}
        caller (-> (first descriptors)
                   (assoc :module 'gravity.sh07-b51.producer-a
                          :source-sha256 (sha 10)
                          :definitions [caller-definition]
                          :external-bindings [external]))
        descriptors (assoc descriptors 0 caller 2 callee)
        edge
        {:ordinal 0
         :caller-module-source-id (:module-source-id caller)
         :caller-function 'caller
         :caller-function-source-hash (:source-hash caller-definition)
         :callsite-path [0 1]
         :callee-module-source-id (:module-source-id callee)
         :callee-function 'governed-external
         :callee-function-source-hash (:source-hash callee-definition)
         :arity 2 :owner-edge :producer-internal
         :authorization :authenticated-b51-namespace-external}
        source
        {:path path :bytes bytes :text "a"
         :source-sha256 (:source-sha256 caller)
         :source-descriptor {:path path :raw-sha256 (:source-sha256 caller)}
         :module {:module (:module caller)}}]
    {:source source :descriptors descriptors
     :manifest {:manifest-id (sha 50) :edges [edge]}
     :edge edge}))

(defn- c5-failure-data [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest governed-projection-and-event-plan-are-closed
  (let [specs (var-get (private-var 'p15-s23-sh07-b51-source-specs))
        events (var-get (private-var 'p15-s23-sh07-b51-source-event-plan))]
    (is (= 13 (count specs)))
    (is (= (vec (range 13)) (mapv :ordinal specs)))
    (is (= [nil nil nil 0 1 2 3 4 5 6 nil nil nil]
           (mapv :module-ordinal specs)))
    (is (= ["docs/phase-01-core-language/011-l1-surface-syntax-specification.md"
            "docs/phase-01-core-language/012-l2-core-language-semantics.md"
            "bootstrap/gravity/src/gravity/checked_core.gravity"]
           (mapv :path (subvec specs 0 3))))
    (is (= {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
            :path "bootstrap/gravity/src/gravity/resolution.gravity"}
           (nth specs 12)))
    (is (= 40 (count events)))
    (is (= (vec (range 40)) (mapv :ordinal events)))
    (is (= (vec (concat (range 3 10) (range 3 10) (range 3 10)))
           (mapv :source-ordinal (subvec events 3 24))))
    (is (= (vec (concat (repeat 7 :module-validation)
                        (repeat 7 :callsite-census)
                        (repeat 7 :link-authority)))
           (mapv :domain (subvec events 3 24))))
    (is (= (vec (range 7))
           (mapv :module-ordinal (subvec events 25 32))))
    (is (= (vec (range 7))
           (mapv :module-ordinal (subvec events 32 39))))
    (is (= {:ordinal 39 :domain :sh06-resolution-binding
            :source-ordinal 12 :module-ordinal nil
            :consumer
            'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}
           (nth events 39)))))

(deftest process-role-requires-the-authenticated-entrypoint-and-checker
  (let [role-for-observation
        (var-get
         (private-var 'p15-s23-sh07-b51-process-role-for-observation))
        current-role
        (var-get (private-var 'p15-s23-sh07-b51-process-role))
        bootstrap-ns 'gravity.bootstrap
        checker-child 'gravity.tools.sh07-b51-attempt32-checker-child
        coordinator 'gravity.tools.sh07-b51-attempt32-coordinator-child
        producer 'gravity.tools.sh07-b51-attempt30-producer-census
        independent 'gravity.tools.sh07-b51-attempt30-independent-census
        checker-arguments
        (fn [role]
          ["-Xmx8g" "clojure.main" "-m"
           "gravity.tools.sh07-b51-attempt32-checker-child"
           "--role" role])
        coordinator-options
        ["--snapshot-commit" (apply str (repeat 40 "0"))
         "--snapshot-tree" (apply str (repeat 40 "1"))
         "--completed-at" "1970-01-01T00:00:00Z"
         "--evidence-relative-root" "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9"
         "--staging-root" "/tmp/staging"
         "--eligibility-review-commit" (apply str (repeat 40 "2"))
         "--eligibility-review-tree" (apply str (repeat 40 "3"))
         "--eligibility-receipt-path" "receipt.edn"
         "--eligibility-receipt-blob" (apply str (repeat 40 "4"))
         "--eligibility-receipt-byte-count" "1"
         "--eligibility-receipt-raw-sha256" (str "sha256:" (apply str (repeat 64 "5")))
         "--eligibility-receipt-id" (str "sha256:" (apply str (repeat 64 "6")))]
        coordinator-arguments
        (into ["-Xmx8g" "clojure.main" "-m"
               "gravity.tools.sh07-b51-attempt32-coordinator-child"]
              coordinator-options)
        observe
        (fn [arguments loaded]
          (role-for-observation
           {:arguments arguments :loaded-project-namespaces loaded}))]
    (is (= :producer-census
           (observe (checker-arguments "producer-census")
                    #{bootstrap-ns checker-child producer})))
    (is (= :independent-verifier-census
           (observe (checker-arguments "independent-verifier-census")
                    #{bootstrap-ns checker-child independent})))
    (is (= :m
           (observe coordinator-arguments #{bootstrap-ns coordinator})))
    (doseq [[arguments loaded]
            [[[] #{}]
             [(checker-arguments "producer-census") #{bootstrap-ns producer}]
             [(checker-arguments "producer-census")
              #{bootstrap-ns checker-child producer independent}]
             [coordinator-arguments #{bootstrap-ns}]
             [coordinator-arguments #{bootstrap-ns coordinator checker-child}]]]
      (let [failure (try (observe arguments loaded) nil
                         (catch clojure.lang.ExceptionInfo error error))]
        (is (= "SH07-B51-TRUSTED-TCB" (:id (ex-data failure))))
        (is (= :unauthenticated-process-role (:reason (ex-data failure))))))
    (testing "an ordinary test JVM cannot silently become M"
      (let [failure (try (current-role) nil
                         (catch clojure.lang.ExceptionInfo error error))]
        (is (= "SH07-B51-TRUSTED-TCB" (:id (ex-data failure))))
        (is (= :unauthenticated-process-role
               (:reason (ex-data failure))))))
    (let [failure (try (observe (checker-arguments "producer-census")
                                #{bootstrap-ns checker-child producer
                                  independent})
                       nil
                       (catch clojure.lang.ExceptionInfo error error))]
      (is (= "SH07-B51-TRUSTED-TCB" (:id (ex-data failure)))))))

(deftest git-transport-is-closed-bounded-and-keeps-stderr-separate
  (let [valid-arguments?
        (var-get (private-var 'p15-s23-sh07-b51-git-arguments-valid?))
        read-bounded
        (var-get (private-var 'p15-s23-sh07-b51-read-stream-bytes))
        git-bytes (var-get (private-var 'p15-s23-sh07-b51-git-bytes))
        zero-id (apply str (repeat 40 "0"))
        source-path
        "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"]
    (is (true? (valid-arguments?
                ["rev-parse" "--verify" (str zero-id "^{commit}")])))
    (is (true? (valid-arguments? ["ls-tree" "-z" zero-id "--" source-path])))
    (is (true? (valid-arguments? ["cat-file" "blob" zero-id])))
    (doseq [arguments
            [["status"]
             ["cat-file" "-p" zero-id]
             ["ls-tree" "-z" zero-id "--" "../outside"]
             ["rev-parse" (str zero-id "^{commit}") "--verify"]]]
      (is (false? (boolean (valid-arguments? arguments))) arguments))
    (let [failure
          (try
            (read-bounded
             (java.io.ByteArrayInputStream. (byte-array [1 2 3 4])) 3 :test)
            nil
            (catch clojure.lang.ExceptionInfo error error))]
      (is (= :git-stream-overflow (:reason (ex-data failure))))
      (is (= 3 (get-in (ex-data failure) [:details :maximum]))))
    (let [failure (try (git-bytes ["cat-file" "blob" zero-id]) nil
                       (catch clojure.lang.ExceptionInfo error error))
          details (:details (ex-data failure))]
      (is (= :git-object-read (:reason (ex-data failure))))
      (is (zero? (:stdout-byte-count details)))
      (is (pos? (:stderr-byte-count details)))
      (is (string? (:stderr details)))
      (is (not (contains? details :output))))))

(deftest bootstrap-canonical-identities-match-the-v9-codec
  (let [raw-sha256
        (var-get (private-var 'p15-s23-sh07-b51-raw-sha256))
        value [:gravity/sh07-b51-source-event-product-id-v1
               {:ordinal 7 :domain :module-validation
                :consumer 'gravity.bootstrap/example
                :source-raw-sha256
                "sha256:0000000000000000000000000000000000000000000000000000000000000000"
                :product {:accepted true}}]
        bytes (.getBytes "strict UTF-8 parity" java.nio.charset.StandardCharsets/UTF_8)]
    (is (= (codec/H value) (bootstrap/reader-canonical-hash value)))
    (is (= (codec/R bytes) (raw-sha256 bytes)))))

(deftest commit-and-tree-inclusion-proofs-are-closed-and-byte-authenticated
  (let [git-object-id
        (var-get (private-var 'p15-s23-sh07-b51-git-object-id))
        validate-content!
        (var-get (private-var
                  'p15-s23-sh07-b51-validate-content-object-proof!))
        validate-source!
        (var-get (private-var
                  'p15-s23-sh07-b51-validate-source-object-proof!))
        unsigned
        (var-get (private-var 'p15-s23-sh07-b51-unsigned-byte-vector))
        from-hex
        (fn [text]
          (byte-array
           (map (fn [index]
                  (unchecked-byte
                   (Integer/parseInt (subs text index (+ index 2)) 16)))
                (range 0 (count text) 2))))
        append
        (fn [& byte-arrays]
          (let [output (java.io.ByteArrayOutputStream.)]
            (doseq [bytes byte-arrays]
              (.write output ^bytes bytes 0 (alength ^bytes bytes)))
            (.toByteArray output)))
        ascii #(-> %
                   str
                   (.getBytes java.nio.charset.StandardCharsets/US_ASCII))
        blob-bytes (ascii "governed source\n")
        blob-oid (git-object-id "blob" blob-bytes)
        tree-bytes
        (append (ascii "100644 file.gravity\u0000") (from-hex blob-oid))
        tree-oid (git-object-id "tree" tree-bytes)
        commit-bytes (ascii (str "tree " tree-oid "\n\nfixture\n"))
        commit-oid (git-object-id "commit" commit-bytes)
        content {:commit commit-oid :tree tree-oid}
        content-proof {:oid commit-oid :raw-bytes (unsigned commit-bytes)}
        descriptor
        {:path "file.gravity" :mode "100644" :blob-oid blob-oid
         :byte-count (alength blob-bytes)
         :raw-sha256 (codec/R blob-bytes)}
        source-proof
        {:schema :gravity/sh07-b51-git-tree-inclusion-proof-v1
         :path "file.gravity" :root-tree tree-oid
         :tree-chain
         [{:tree-oid tree-oid :tree-bytes (unsigned tree-bytes)
           :component "file.gravity" :selected-mode "100644"
           :selected-oid blob-oid}]
         :blob-bytes (unsigned blob-bytes)}]
    (is (true? (validate-content! content content-proof)))
    (is (java.util.Arrays/equals
         ^bytes blob-bytes
         ^bytes (validate-source! content descriptor source-proof)))
    (doseq [[kind operation]
            [[:commit-byte
              #(validate-content!
                content (update-in content-proof [:raw-bytes 0] bit-xor 1))]
             [:tree-byte
              #(validate-source!
                content descriptor
                (update-in source-proof [:tree-chain 0 :tree-bytes 0]
                           bit-xor 1))]
             [:path-component
              #(validate-source!
                content descriptor
                (assoc-in source-proof [:tree-chain 0 :component] "other"))]
             [:mode
              #(validate-source!
                content descriptor
                (assoc-in source-proof [:tree-chain 0 :selected-mode]
                          "40000"))]
             [:blob-byte
              #(validate-source!
                content descriptor
                (update-in source-proof [:blob-bytes 0] bit-xor 1))]]]
      (let [failure (try (operation) nil
                         (catch clojure.lang.ExceptionInfo error error))]
        (is (= "SH07-B51-00945-GOVERNED-READ"
               (:id (ex-data failure))) kind)))))

(deftest dispatcher-is-dense-single-use-and-finalizes-once
  (let [session-var (private-var '*p15-s23-sh07-b51-governed-source-session*)
        dispatch (var-get (private-var
                           'p15-s23-sh07-b51-governed-source-event!))
        finalize (var-get (private-var
                           'p15-s23-sh07-b51-finalize-source-session!))
        events (var-get (private-var 'p15-s23-sh07-b51-source-event-plan))
        descriptors
        (mapv (fn [ordinal]
                {:ordinal ordinal :kind :test :module-ordinal nil
                 :path (str "source-" ordinal) :mode "100644"
                 :blob-oid "0000000000000000000000000000000000000000"
                 :byte-count 0
                 :raw-sha256
                 "sha256:0000000000000000000000000000000000000000000000000000000000000000"})
              (range 13))
        entries (mapv #(hash-map :descriptor % :bytes (byte-array 0) :text ""
                                 :content-object-proof {}
                                 :source-object-proof {})
                      descriptors)
        token (Object.)
        state {:token token :thread (Thread/currentThread) :role :m
               :commit "0000000000000000000000000000000000000000"
               :tree "1111111111111111111111111111111111111111"
               :entries entries :descriptors descriptors
               :descriptors-root (bootstrap/reader-canonical-hash
                                  [:gravity/sh07-b51-source-descriptors-root-v1
                                   descriptors])
               :cursor (atom 0) :events (atom [])
               :checked-products (atom {}) :finalized (atom false)}]
    (with-bindings {session-var {:token token :state state}}
      (testing "reordered first event rejects without consuming it"
        (let [failure
              (try (dispatch :l2-contract 1 nil
                             'gravity.bootstrap/p15-s23-sh07-b51-l2-contract
                             (constantly true))
                   nil
                   (catch clojure.lang.ExceptionInfo error error))]
          (is (= :event-order (:reason (ex-data failure))))
          (is (zero? @(:cursor state)))))
      (doseq [{:keys [domain source-ordinal module-ordinal consumer]} events]
        (dispatch domain source-ordinal module-ordinal consumer
                  (constantly true)))
      (let [receipt (finalize)]
        (is (= #{:schema :role :commit :tree :source-descriptors
                 :source-descriptors-root :events :event-root :counters
                 :finalized}
               (set (keys receipt))))
        (is (= {:acquire-count 13 :decode-count 13
                :event-count 40 :finalize-count 1}
               (:counters receipt)))
        (is (true? (:finalized receipt))))
      (is (= :inactive-or-forged-session
             (:reason
              (ex-data
               (try (finalize) nil
                    (catch clojure.lang.ExceptionInfo error error)))))))))

(deftest returned-m0-m1-values-replay-source-authority-event-products
  (let [event-plan
        (var-get (private-var 'p15-s23-sh07-b51-source-event-plan))
        event-product-id
        (var-get (private-var 'p15-s23-sh07-b51-source-event-product-id))
        source-authority-product
        (var-get
         (private-var
          'p15-s23-sh07-b51-source-authority-product-from-m0-m1))
        replay!
        (var-get
         (private-var
          'p15-s23-sh07-b51-replay-returned-source-authority-events!))
        sha (fn [ordinal] (format "sha256:%064x" (inc ordinal)))
        source-descriptors
        (mapv (fn [ordinal]
                {:ordinal ordinal
                 :path (str "source-" ordinal ".gravity")
                 :raw-sha256 (sha ordinal)})
              (range 13))
        m0s
        (mapv
         (fn [module-ordinal]
           {:module-ordinal module-ordinal
            :module (symbol (str "gravity.replay.module-" module-ordinal))
            :module-source-id (sha (+ 20 module-ordinal))
            :logical-source-path
            (:path (nth source-descriptors (+ 3 module-ordinal)))
            :source-sha256
            (:raw-sha256 (nth source-descriptors (+ 3 module-ordinal)))
            :definitions
            [{:name (symbol (str "function-" module-ordinal))
              :source-hash (sha (+ 30 module-ordinal))}]})
         (range 7))
        link-manifest {:manifest-id (sha 50)}
        events
        (mapv
         (fn [{:keys [ordinal source-ordinal] :as coordinate}]
           (let [event
                 (assoc coordinate :source-raw-sha256
                        (:raw-sha256
                         (nth source-descriptors source-ordinal)))
                 product
                 (if (<= 25 ordinal 31)
                   (source-authority-product
                    (nth m0s (- ordinal 25)) link-manifest)
                   :opaque-test-product)]
             (assoc event :product-id (event-product-id event product))))
         event-plan)
        receipt {:source-descriptors source-descriptors :events events}
        plan-authority :opaque-test-product
        plan-validator
        (private-var 'p15-s23-sh07-b51-resolution-plan-authority-valid?)]
    (with-redefs-fn
      {plan-validator (constantly true)}
      #(is (true? (replay! m0s link-manifest plan-authority receipt))))
    (let [mutated
          (assoc-in m0s [3 :definitions 0 :source-hash] (sha 60))
          failure
          (try (replay! mutated link-manifest plan-authority receipt) nil
               (catch clojure.lang.ExceptionInfo error error))]
      (is (= :source-authority-event-replay
             (:reason (ex-data failure))))
      (is (= 3 (get-in (ex-data failure) [:details :module-ordinal]))))))

(deftest public-wrapper-has-no-authority-override-arity
  (is (= '([commit tree])
         (:arglists
          (meta #'bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products)))))

(deftest compatibility-reader-is-behaviorally-unreachable-in-v9
  (let [compatibility-reader
        (var-get (private-var 'p15-s23-sh07-b51-compatibility-source-record))
        compatibility-module-input
        (var-get (private-var 'p15-s23-sh07-b51-compatibility-module-input))
        current-emitter
        (var-get (private-var 'p15-s23-sh07-b51-current-emitter))
        v9-construction-var
        (private-var '*p15-s23-sh07-b51-v9-receipted-bundle-construction*)
        source-session-var
        (private-var '*p15-s23-sh07-b51-governed-source-session*)
        module-spec (first bootstrap/p15-s23-sh07-b51-module-specs)
        temporary-path
        (java.nio.file.Files/createTempFile
         "sh07-b51-legacy-source-" ".gravity"
         (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (spit (.toFile temporary-path) "legacy source bytes")
      (testing "the pre-V9 compatibility surface remains available by itself"
        (is (= "legacy source bytes"
               (:text (compatibility-reader (str temporary-path))))))
      (testing "V9 fails before reading the same extant live-worktree path"
        (let [failure
              (with-bindings {v9-construction-var true}
                (try
                  (compatibility-reader (str temporary-path))
                  nil
                  (catch clojure.lang.ExceptionInfo error error)))]
          (is (= "SH07-B51-00945-GOVERNED-READ"
                 (:id (ex-data failure))))
          (is (= :legacy-live-source-read-in-v9
                 (:reason (ex-data failure))))))
      (testing "both immediate compatibility-reader callers fail closed"
        (with-bindings {v9-construction-var true}
          (doseq [operation [#(compatibility-module-input module-spec)
                             current-emitter]]
            (let [failure
                  (try (operation) nil
                       (catch clojure.lang.ExceptionInfo error error))]
              (is (= :legacy-live-source-read-in-v9
                     (:reason (ex-data failure))))))))
      (testing "the source-authority and compiler-binding fallbacks fail closed"
        (with-bindings {v9-construction-var true}
          (doseq [operation
                  [#(bootstrap/p15-s23-sh07-compiler-source-authority
                     "untrusted.gravity" "" {} {})
                   #(bootstrap/p15-s23-sh07-compiler-source-authority
                     (:logical-source-path module-spec) ""
                     {:p15-s23-sh07-module-source-descriptor
                      {:module-ordinal 0
                       :logical-source-path
                       (:logical-source-path module-spec)}}
                     {})
                   #(bootstrap/p15-s23-stage2-compiler-artifact-binding!
                     {} "untrusted.gravity" :jvm)]]
            (let [failure
                  (try (operation) nil
                       (catch clojure.lang.ExceptionInfo error error))]
              (is (= :legacy-live-source-read-in-v9
                     (:reason (ex-data failure))))))))
      (testing "public legacy call paths cannot become V9 fallback paths"
        (with-bindings {v9-construction-var true}
          (doseq [operation [bootstrap/p15-s23-sh07-b51-derive-callsite-census
                             bootstrap/p15-s23-sh07-b51-build-link-authority
                             bootstrap/p15-s23-sh07-b51-build-execution-bundle]]
            (let [failure
                  (try (operation) nil
                       (catch clojure.lang.ExceptionInfo error error))]
              (is (= :legacy-live-source-read-in-v9
                     (:reason (ex-data failure))))))))
      (testing "a present but forged governed session never falls back"
        (let [failure
              (with-bindings {v9-construction-var true
                              source-session-var
                              {:token (Object.) :state {}}}
                (try
                  (bootstrap/p15-s23-sh07-b51-derive-callsite-census)
                  nil
                  (catch clojure.lang.ExceptionInfo error error)))]
          (is (= :inactive-or-forged-session
                 (:reason (ex-data failure))))))
      (finally
        (java.nio.file.Files/deleteIfExists temporary-path)))))

(deftest public-v9-wrapper-enters-deny-scope-before-source-acquisition
  (let [v9-construction-var
        (private-var '*p15-s23-sh07-b51-v9-receipted-bundle-construction*)
        open-session-var
        (private-var 'p15-s23-sh07-b51-open-source-session)
        observed-scope (atom nil)
        sentinel ::stop-before-object-read
        failure
        (with-redefs-fn
          {open-session-var
           (fn [_commit _tree]
             (reset! observed-scope @v9-construction-var)
             (throw (ex-info "test stop" {:sentinel sentinel})))}
          #(try
             (bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products
              (apply str (repeat 40 "0"))
              (apply str (repeat 40 "1")))
             nil
             (catch clojure.lang.ExceptionInfo error error)))]
    (is (true? @observed-scope))
    (is (= sentinel (:sentinel (ex-data failure))))
    (is (false? @v9-construction-var))))

(deftest c5-import-carriers-are-an-exact-m0-m1-projection
  (let [{:keys [source descriptors manifest edge]} (c5-fixture)
        authority-rows
        ((var-get (private-var 'p15-s23-sh07-b51-c5-authority-rows*))
         0 source descriptors manifest)
        carriers
        ((var-get (private-var 'p15-s23-sh07-b51-c5-request-carriers))
         (first descriptors) manifest authority-rows)
        binding (first (:import-bindings carriers))
        coordinate (:semantic-span binding)]
    (is (= 1 (count authority-rows)))
    (is (= 'governed-external (:name binding)))
    (is (= 'gravity.sh07-b51.producer-c (:namespace binding)))
    (is (= (:module-source-id (nth descriptors 2))
           (:definition-artifact-id binding)
           (:callee-module-source-id coordinate)))
    (is (= (:source-sha256 (nth descriptors 2))
           (:callee-source-sha256 coordinate)))
    (is (= (get-in descriptors [2 :definitions 0 :source-hash])
           (:callee-definition-source-hash coordinate)))
    (is (= 2 (:arity coordinate)))
    (is (= :producer-internal (:owner-edge coordinate)))
    (is (= [(:ordinal edge)] (:authorized-edge-ordinals coordinate)))
    (is (= ['governed-external] (get-in carriers [:imports 0 :refer])))
    (is (= (:dependency-edges carriers)
           [{:from 'gravity.sh07-b51.producer-a
             :to 'gravity.sh07-b51.producer-c
             :kind :namespace
             :boundary :producer-internal
             :semantic-span (get-in carriers [:imports 0 :semantic-span])
             :source-span (get-in carriers [:imports 0 :source-span])}]))))

(deftest c5-authority-preflight-rejects-closed-set-mutations
  (let [{:keys [source descriptors manifest edge]} (c5-fixture)
        rows (var-get (private-var
                       'p15-s23-sh07-b51-c5-authority-rows*))
        failure-cause
        (fn [changed-descriptors changed-manifest]
          (get-in
           (c5-failure-data
            #(rows 0 source changed-descriptors changed-manifest))
           [:details :cause]))
        duplicate-binding
        (update-in descriptors [0 :external-bindings]
                   #(conj % (assoc (first %) :callee-module-ordinal 3)))
        stale-binding
        (assoc-in descriptors [0 :external-bindings 0
                               :callee-source-sha256]
                  (format "sha256:%064x" 999))
        missing-edge (assoc manifest :edges [])
        extra-edge
        (update manifest :edges conj
                (assoc edge :ordinal 1 :callee-function 'fabricated-extra))
        unauthorized-edge
        (assoc-in manifest [:edges 0 :authorization] :fabricated)]
    (is (= :ambiguous-external-binding
           (failure-cause duplicate-binding manifest)))
    (is (= :stale-external-binding
           (failure-cause stale-binding manifest)))
    (is (= :missing-authorized-edge
           (failure-cause descriptors missing-edge)))
    (is (= :extra-or-ambiguous-authorized-edge
           (failure-cause descriptors extra-edge)))
    (is (= :unauthorized-owner-edge
           (failure-cause descriptors unauthorized-edge)))))

(deftest c5-scoped-request-adapter-is-closed-and-delegates-nonmatches
  (let [{:keys [source descriptors manifest]} (c5-fixture)
        caller (first descriptors)
        rows
        ((var-get (private-var 'p15-s23-sh07-b51-c5-authority-rows*))
         0 source descriptors manifest)
        carriers
        ((var-get (private-var 'p15-s23-sh07-b51-c5-request-carriers))
         caller manifest rows)
        sh05 {:artifact-id (format "sha256:%064x" 200)}
        base-request
        {:module {:namespace (:module caller)}
         :definitions [] :imports [] :import-bindings []
         :dependency-edges []}
        adapter
        (merge
         {:source-path (:path source)
          :sh05-artifact-id (:artifact-id sh05)
          :base-request-id
          (bootstrap/reader-canonical-hash
           [:gravity/sh07-b51-sh06-base-request-v1 base-request])
          :caller-module-source-id (:module-source-id caller)
          :manifest-id (:manifest-id manifest)}
         carriers)
        scoped
        ((var-get (private-var
                   'p15-s23-sh07-b51-c5-scoped-request-function))
         adapter (fn [_ _] base-request))
        unrelated (scoped "unrelated.gravity" sh05)
        admitted (scoped (:path source) sh05)
        fabricated
        (update adapter :import-bindings conj
                (assoc (first (:import-bindings adapter))
                       :name 'fabricated-extra))
        fabricated-failure
        (c5-failure-data
         #((var-get (private-var
                     'p15-s23-sh07-b51-apply-c5-request-adapter))
           fabricated (:path source) sh05 base-request))]
    (is (identical? base-request unrelated))
    (is (= (:imports adapter) (:imports admitted)))
    (is (= (:import-bindings adapter) (:import-bindings admitted)))
    (is (= :unauthenticated-c5-external-binding
           (:reason fabricated-failure)))
    (is (= :request-replay-mismatch
           (get-in fabricated-failure [:details :cause])))))
