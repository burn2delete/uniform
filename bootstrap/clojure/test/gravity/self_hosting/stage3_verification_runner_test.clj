(ns gravity.self-hosting.stage3-verification-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.stage3-fragment-size-preflight-test :as fragment]
            [gravity.self-hosting.stage3-verification-runner :as runner]))

(defn- selectors-for
  [namespace-symbol]
  (->> runner/fixed-batch-ids
       (mapcat #(get runner/fixed-batch-selectors %))
       (filter #(= namespace-symbol (symbol (namespace %))))
       vec))

(defn- source-plan-source-order
  []
  ;; The fixed execution order runs source-control-form-arity before
  ;; source-plan-contract, while the source file intentionally interleaves
  ;; those selectors as plan identity, control arity, then bounds.
  [(first runner/source-plan-contract-selectors)
   (first runner/source-control-form-arity-selectors)
   (second runner/source-plan-contract-selectors)])

(def ^:private primitive-ns
  'gravity.self-hosting.sh08-primitive-function-type-test)
(def ^:private recursive-ns
  'gravity.self-hosting.sh08-recursive-function-type-test)
(def ^:private ho-ns
  'gravity.self-hosting.sh08-authoritative-higher-order-function-test)
(def ^:private census-ns
  'gravity.self-hosting.sh07-authoritative-coverage-census-test)
(def ^:private source-plan-ns
  'gravity.self-hosting.sh07-c7-type-source-coverage-test)
(def ^:private fragment-ns
  'gravity.self-hosting.stage3-fragment-size-preflight-test)
(def ^:private bootstrap-ns
  'gravity.bootstrap-test)

(defn- bound-report-arguments
  [batch]
  ["--batch" (name batch)
   "--report-file" "/tmp/stage3-test-report.json"
   "--report-nonce" "test-nonce"
   "--report-check-id" "test-check"
   "--report-command-identity-sha256"
   "sha256:0000000000000000000000000000000000000000000000000000000000000000"])

(def ^:private complete-source-files
  {primitive-ns "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_function_type_test.clj"
   recursive-ns "bootstrap/clojure/test/gravity/self_hosting/sh08_recursive_function_type_test.clj"
   ho-ns "bootstrap/clojure/test/gravity/self_hosting/sh08_authoritative_higher_order_function_test.clj"
   fragment-ns "bootstrap/clojure/test/gravity/self_hosting/stage3_fragment_size_preflight_test.clj"})

(defn- source-deftest-selectors
  [namespace-symbol relative-path]
  (let [root (or (System/getProperty "gravity.repository.root") ".")
        path (java.io.File. root relative-path)
        eof (gensym "eof")]
    (with-open [reader (java.io.PushbackReader.
                        (java.io.FileReader. path))]
      (loop [selectors []]
        (let [form (read {:eof eof} reader)]
          (if (= eof form)
            selectors
            (recur
             (if (and (seq? form) (= 'deftest (first form)))
               (conj selectors
                     (symbol (str namespace-symbol) (str (second form))))
               selectors))))))))

(deftest fixed-batches-have-exact-source-order-vectors
  (is (= [:primitive-pure
          :primitive-bool-authenticated
          :recursive-pure
          :recursive-authenticated
          :authoritative-ho-pure
          :authoritative-ho-authenticated
          :source-control-form-arity
          :source-plan-contract
          :coverage-census-contract
          :fragment-size-preflight
          :public-c7-check]
         runner/fixed-batch-ids))
  (is (= runner/primitive-pure-selectors
         (get runner/fixed-batch-selectors :primitive-pure)))
  (is (= runner/recursive-pure-selectors
         (get runner/fixed-batch-selectors :recursive-pure)))
  (is (= runner/authoritative-ho-pure-selectors
         (get runner/fixed-batch-selectors :authoritative-ho-pure)))
  (is (= runner/source-plan-contract-selectors
         (get runner/fixed-batch-selectors :source-plan-contract)))
  (is (= runner/coverage-census-contract-selectors
         (get runner/fixed-batch-selectors :coverage-census-contract)))
  (is (= runner/source-control-form-arity-selectors
         (get runner/fixed-batch-selectors :source-control-form-arity)))
  (is (= runner/recursive-authenticated-selectors
         (get runner/fixed-batch-selectors :recursive-authenticated)))
  (is (= runner/authoritative-ho-authenticated-selectors
         (get runner/fixed-batch-selectors :authoritative-ho-authenticated)))
  (is (= (count runner/primitive-pure-selectors) 4))
  (is (= (count runner/recursive-pure-selectors) 7))
  (is (= (count runner/recursive-authenticated-selectors) 2))
  (is (= (count runner/authoritative-ho-pure-selectors) 7))
  (is (= (count runner/authoritative-ho-authenticated-selectors) 2))
  (is (= (count runner/source-plan-contract-selectors) 2))
  (is (= (count runner/coverage-census-contract-selectors) 2))
  (is (= (count runner/source-control-form-arity-selectors) 1)))

(deftest authenticated-sibling-batches-have-exact-cross-namespace-order-and-old-ids-are-rejected
  (is (= ['gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-gravity-boundary
          'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-string-gravity-boundary]
         runner/recursive-authenticated-selectors))
  (is (= ['gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-fixtures-are-co-canonical
          'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-authenticated-fixture-boundary]
         runner/authoritative-ho-authenticated-selectors))
  (doseq [old-id [:recursive-integer-authenticated
                  :recursive-string-authenticated
                  :authoritative-ho-fixture-parity
                  :authoritative-ho2-authenticated]]
    (let [error (try
                  (runner/batch-definition old-id)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "STAGE3-UNKNOWN-BATCH" (:id (ex-data error)))
          (str "retired Stage3 batch ID remained accepted: " old-id)))))

(deftest fixed-catalog-covers-owned-source-files-exactly
  (let [discovered
        {primitive-ns (selectors-for primitive-ns)
         recursive-ns (selectors-for recursive-ns)
         ho-ns (selectors-for ho-ns)
         census-ns (conj (selectors-for census-ns)
                         'gravity.self-hosting.sh07-authoritative-coverage-census-test/intentionally-unowned-fixture)
         source-plan-ns (source-plan-source-order)
         fragment-ns (selectors-for fragment-ns)
         bootstrap-ns (selectors-for bootstrap-ns)}]
    (let [catalog-result
          (runner/validate-fixed-catalog! runner/fixed-batches discovered)]
      (is (= :passed (:status catalog-result)))
      (is (= ['gravity.self-hosting.sh07-authoritative-coverage-census-test/intentionally-unowned-fixture]
             (get-in catalog-result [:intentionally-unowned census-ns]))))
    (is (= (set (selectors-for primitive-ns))
           (set (mapcat val
                        (select-keys runner/fixed-batch-selectors
                                    [:primitive-pure
                                     :primitive-bool-authenticated])))))
    (is (= (set (selectors-for recursive-ns))
           (set (mapcat val
                        (select-keys runner/fixed-batch-selectors
                                     [:recursive-pure
                                     :recursive-authenticated])))))
    (is (= (set (selectors-for ho-ns))
           (set (mapcat val
                        (select-keys runner/fixed-batch-selectors
                                    [:authoritative-ho-pure
                                     :authoritative-ho-authenticated])))))))

(deftest complete-source-files-have-exact-fixed-selector-coverage
  (doseq [[namespace-symbol relative-path] complete-source-files]
    (let [actual (source-deftest-selectors namespace-symbol relative-path)
          expected (selectors-for namespace-symbol)]
      (is (= expected actual)
          (str "source-order coverage drift for " namespace-symbol))
      (is (= (set expected) (set actual))
          (str "selector coverage drift for " namespace-symbol)))))

(deftest fixed-catalog-rejects-missing-extra-and-duplicate-drift
  (let [base
        {primitive-ns (selectors-for primitive-ns)
         recursive-ns (selectors-for recursive-ns)
         ho-ns (selectors-for ho-ns)
         census-ns (selectors-for census-ns)
         source-plan-ns (source-plan-source-order)
         fragment-ns (selectors-for fragment-ns)
         bootstrap-ns (selectors-for bootstrap-ns)}
        missing (update base primitive-ns pop)
        extra (update base primitive-ns conj
                      'gravity.self-hosting.sh08-primitive-function-type-test/new-test)
        duplicate (update base primitive-ns conj
                          (first (get base primitive-ns)))]
    (doseq [[catalog expected-id]
            [[missing "STAGE3-CATALOG-MISSING-TEST-VAR"]
             [extra "STAGE3-CATALOG-EXTRA-TEST-VAR"]
             [duplicate "STAGE3-CATALOG-DUPLICATE-TEST-VAR"]]]
      (let [error (try
                    (runner/validate-fixed-catalog! runner/fixed-batches catalog)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= expected-id (:id (ex-data error))))))))

(deftest strict-cli-accepts-only-one-fixed-batch-or-help
  (let [report-args
        ["--batch" "primitive-pure"
         "--report-file" "/tmp/stage3-report.json"
         "--report-nonce" "nonce-1"
         "--report-check-id" "check-1"
         "--report-command-identity-sha256"
         "sha256:0000000000000000000000000000000000000000000000000000000000000000"]]
  (is (= {:help? true} (runner/parse-arguments ["--help"])))
  (is (= :primitive-pure
         (:batch-id (runner/parse-arguments report-args))))
  (doseq [[arguments expected-id]
          [[[] "STAGE3-CLI-MISSING-BATCH"]
           [["primitive-pure"] "STAGE3-CLI-POSITIONAL"]
           [["--batch" "primitive-pure"] "STAGE3-CLI-MISSING-REPORT-BINDING"]
           [["--batch" "not-a-batch"] "STAGE3-CLI-UNKNOWN-BATCH"]
           [["--batch" "primitive-pure" "--batch" "recursive-pure"]
            "STAGE3-CLI-DUPLICATE-BATCH"]
           [["--batch" "primitive-pure" "--report-file" "/tmp/x"
             "--report-file" "/tmp/y"] "STAGE3-CLI-DUPLICATE-OPTION"]
           [["--test-var" "gravity.foo/bar"]
            "STAGE3-CLI-ARBITRARY-SELECTOR"]
           [["--namespace" "gravity.foo"]
            "STAGE3-CLI-ARBITRARY-SELECTOR"]]]
    (let [error (try
                  (runner/parse-arguments arguments)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= expected-id (:id (ex-data error))) (pr-str arguments)))))
  )

(defn- passing-result
  [selector selection-index]
  {:test-var selector
   :selection-index selection-index
   :test-result {:test 1 :pass 1 :fail 0 :error 0}
   :cache {:sh06-hits 0 :sh06-misses 1
           :core-hits 0 :core-misses 0
           :verification-hits 0 :verification-misses 0}
   :elapsed-ms 7
   :completed? true})

(defn- synthetic-batch-run
  [batch-id selectors delegate]
  (with-redefs [runner/fixed-batches
                (assoc runner/fixed-batches
                       batch-id
                       {:batch-id batch-id
                        :name (name batch-id)
                        :test-vars selectors})]
    (binding [runner/*catalog-loader* nil
              runner/*delegate-run-test-vars* (constantly delegate)]
      (runner/run-batch batch-id))))

(defn- contract-error-id
  [batch-id selectors delegate]
  (try
    (synthetic-batch-run batch-id selectors delegate)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(defn- successful-delegate
  [selectors]
  (let [results (mapv (fn [selector selection-index]
                        (passing-result selector selection-index))
                      selectors
                      (range))
        cache {:sh06-hits 0 :sh06-misses (count selectors)
               :core-hits 0 :core-misses 0
               :verification-hits 0 :verification-misses 0}]
    {:test-result {:test (count selectors)
                   :pass (count selectors)
                   :fail 0
                   :error 0}
     :test-var-results results
     :skipped-test-vars []
     :cache cache
     :elapsed-ms 1}))

(deftest multi-var-batches-delegate-with-one-entry-and-fail-fast-tail
  (let [calls (atom [])
        selectors ['gravity.synthetic/first-test
                   'gravity.synthetic/second-test
                   'gravity.synthetic/third-test]
        delegate
        (fn [selection]
          (swap! calls conj selection)
          {:test-result {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
           :test-var-results [(assoc (passing-result (first selectors) 0)
                                      :test-result {:test 1 :pass 0 :fail 1 :error 0})]
           :skipped-test-vars (subvec selectors 1)
           :cache {:sh06-hits 0 :sh06-misses 1
                   :core-hits 0 :core-misses 0
                   :verification-hits 0 :verification-misses 0}
           :elapsed-ms 11})]
    (with-redefs [runner/fixed-batches
                  (assoc runner/fixed-batches
                         :synthetic-multi
                         {:batch-id :synthetic-multi
                          :name "synthetic-multi"
                          :test-vars selectors})]
      (binding [runner/*catalog-loader* nil
                runner/*delegate-run-test-vars* delegate]
        (let [result (runner/run-batch :synthetic-multi)
              selection (first @calls)]
          (is (= {:test-vars selectors :maximum-entries 1 :fail-fast? true}
                 selection))
          (is (= selectors (:selection-order result)))
          (is (= [:failed :skipped :skipped]
                 (mapv :status (:test-var-results result))))
          (is (= (subvec selectors 1) (:skipped-tail result)))
          (is (= :non-authoritative (:authority result)))
          (is (false? (:authoritative? result))))))))

(deftest singleton-batches-omit-generic-fail-fast
  (let [calls (atom [])
        selector 'gravity.synthetic/one-test]
    (binding [runner/*catalog-loader* nil
              runner/*delegate-run-test-vars*
              (fn [selection]
                (swap! calls conj selection)
                {:test-result {:test 1 :pass 1 :fail 0 :error 0}
           :test-var-results [(passing-result selector 0)]
                 :skipped-test-vars []
                 :cache {:sh06-hits 0 :sh06-misses 1
                         :core-hits 0 :core-misses 0
                         :verification-hits 0 :verification-misses 0}
                 :elapsed-ms 7})]
      (let [result
            (with-redefs [runner/fixed-batches
                          (assoc runner/fixed-batches
                                 :synthetic-single
                                 {:batch-id :synthetic-single
                                  :name "synthetic-single"
                                  :test-vars [selector]})]
              (runner/run-batch :synthetic-single))]
        (is (= {:test-vars [selector] :maximum-entries 1} (first @calls)))
        (is (nil? (:fail-fast? (first @calls))))
        (is (= :passed (:status result)))
        (is (= [:passed] (mapv :status (:test-var-results result))))))))

(deftest receipts-preserve-per-var-order-count-cache-elapsed-and-nonauthority
  (let [selectors ['gravity.synthetic/left-test 'gravity.synthetic/right-test]
        result
        (binding [runner/*catalog-loader* nil
                  runner/*delegate-run-test-vars*
                  (fn [_]
                    {:test-result {:test 2 :pass 2 :fail 0 :error 0}
                     :test-var-results
                     [(assoc (passing-result (first selectors) 0)
                             :elapsed-ms 13)
                      (assoc (passing-result (second selectors) 1)
                             :elapsed-ms 17)]
                     :skipped-test-vars []
                     :cache {:sh06-hits 0 :sh06-misses 2
                             :core-hits 0 :core-misses 0
                             :verification-hits 0 :verification-misses 0}
                     :elapsed-ms 31})]
          (with-redefs [runner/fixed-batches
                        (assoc runner/fixed-batches
                               :synthetic-receipt
                               {:batch-id :synthetic-receipt
                                :name "synthetic-receipt"
                                :test-vars selectors})]
            (runner/run-batch :synthetic-receipt)))]
    (is (= selectors (:selection-order result)))
    (is (= [0 1] (mapv :selection-index (:test-var-results result))))
    (is (= [13 17] (mapv :elapsed-ms (:test-var-results result))))
    (is (= [1 1] (mapv :pass (:test-var-results result))))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))
    (is (false? (:cache-authoritative? result)))
    (is (true? (:fresh-authoritative-run-required? result)))))

(deftest delegate-contract-rejects-fail-fast-and-aggregate-tampering
  (let [selectors ['gravity.synthetic/a-test
                   'gravity.synthetic/b-test
                   'gravity.synthetic/c-test]
        failed-a (assoc (passing-result (first selectors) 0)
                        :test-result {:test 1 :pass 0 :fail 1 :error 0})
        failed-b (assoc (passing-result (second selectors) 1)
                        :test-result {:test 1 :pass 0 :fail 1 :error 0})
        passed-b (passing-result (second selectors) 1)
        cache-total {:sh06-hits 0 :sh06-misses 2
                     :core-hits 0 :core-misses 0
                     :verification-hits 0 :verification-misses 0}]
    (is (= "STAGE3-DELEGATE-CONTRACT"
           (contract-error-id
            :synthetic-two-failures selectors
            {:test-result {:test 2 :pass 0 :fail 2 :error 0}
             :test-var-results [failed-a failed-b]
             :skipped-test-vars [(nth selectors 2)]
             :cache cache-total
             :elapsed-ms 7})))
    (is (= "STAGE3-DELEGATE-CONTRACT"
           (contract-error-id
            :synthetic-pass-after-failure selectors
            {:test-result {:test 2 :pass 1 :fail 1 :error 0}
             :test-var-results [failed-a passed-b]
             :skipped-test-vars [(nth selectors 2)]
             :cache cache-total
             :elapsed-ms 7})))
    (is (= "STAGE3-DELEGATE-CONTRACT"
           (contract-error-id
            :synthetic-cache-mismatch selectors
            {:test-result {:test 2 :pass 2 :fail 0 :error 0}
             :test-var-results [(passing-result (first selectors) 0)
                                (passing-result (second selectors) 1)]
             :skipped-test-vars [(nth selectors 2)]
             :cache {:sh06-hits 0 :sh06-misses 1
                     :core-hits 0 :core-misses 0
                     :verification-hits 0 :verification-misses 0}
             :elapsed-ms 7})))))

(deftest delegate-contract-rejects-malformed-counters-and-accepts-independent-test-count
  (let [selector 'gravity.synthetic/counter-test
        valid-cache {:sh06-hits 0 :sh06-misses 1
                     :core-hits 0 :core-misses 0
                     :verification-hits 0 :verification-misses 0}
        valid (fn [summary]
                {:test-result summary
                 :test-var-results
                 [{:test-var selector
                   :selection-index 0
                   :test-result summary
                   :cache valid-cache
                   :elapsed-ms 1
                   :completed? true}]
                 :skipped-test-vars []
                 :cache valid-cache
                 :elapsed-ms 1})]
    (is (= :passed
           (:status
            (synthetic-batch-run
             :synthetic-seven-pass [selector]
             (valid {:test 1 :pass 7 :fail 0 :error 0})))))
    (doseq [summary [{:test 1 :pass -1 :fail 0 :error 0}
                     {:test 1 :pass 1.0 :fail 0 :error 0}
                     {:test 0 :pass 0 :fail 0 :error 0}]]
      (is (= "STAGE3-DELEGATE-CONTRACT"
             (contract-error-id :synthetic-malformed-counter [selector]
                                (valid summary)))))))

(deftest cleanup-fatal-cause-chain-and-ordinary-failure-are-truthful
  (let [ordinary (ex-info "ordinary cleanup" {:id "TEST-CLEANUP-ORDINARY"})
        interrupted (InterruptedException. "interrupted cleanup")
        fatal (ex-info "wrapped interrupt" {:id "TEST-CLEANUP-WRAPPED"}
                       interrupted)]
    (Thread/interrupted)
    (let [{:keys [thrown interrupted?]}
          (try
            (with-redefs [clojure.core/flush (fn [] nil)
                          clojure.core/shutdown-agents (fn [] (throw fatal))]
              (runner/cleanup!))
            nil
            (catch Throwable error
              {:thrown error
               :interrupted? (.isInterrupted (Thread/currentThread))}))]
      (is (identical? fatal thrown))
      (is interrupted?)
      (Thread/interrupted))
    (let [published (atom nil)
          cleanup-error ordinary
          selectors runner/primitive-pure-selectors
          thrown
          (try
            (with-redefs [runner/cleanup! (fn [] (throw cleanup-error))
                          runner/publish-report!
                          (fn [_ receipt] (reset! published receipt))]
              (binding [runner/*catalog-loader* nil
                        runner/*delegate-run-test-vars*
                        (constantly (successful-delegate selectors))
                        runner/*exit-fn* (fn [_] nil)]
                (runner/-main
                 "--batch" "primitive-pure"
                 "--report-file" "/tmp/stage3-cleanup-ordinary.json"
                 "--report-nonce" "cleanup-nonce"
                 "--report-check-id" "cleanup-check"
                 "--report-command-identity-sha256"
                 "sha256:0000000000000000000000000000000000000000000000000000000000000000")))
            nil
            (catch Throwable error error))]
      (is (identical? cleanup-error thrown))
      (is (= :infrastructure-failure (:status @published)))
      (is (= :gravity/stage3-infrastructure-failure-v1 (:schema @published)))
      (is (= "/tmp/stage3-cleanup-ordinary.json" (:report-file @published)))
      (is (false? (:authoritative? @published))))
    (let [published (atom nil)
          execution (ex-info "delegate execution" {:id "STAGE3-DELEGATE-CONTRACT"})
          cleanup-error (ex-info "cleanup after delegate" {:id "TEST-CLEANUP"})
          thrown
          (try
            (with-redefs [runner/cleanup! (fn [] (throw cleanup-error))
                          runner/publish-report!
                          (fn [_ receipt] (reset! published receipt))]
              (binding [runner/*catalog-loader* nil
                        runner/*delegate-run-test-vars* (fn [_] (throw execution))
                        runner/*exit-fn* (fn [_] nil)]
                (runner/-main
                 "--batch" "primitive-pure"
                 "--report-file" "/tmp/stage3-cleanup-execution.json"
                 "--report-nonce" "execution-nonce"
                 "--report-check-id" "execution-check"
                 "--report-command-identity-sha256"
                 "sha256:0000000000000000000000000000000000000000000000000000000000000000")))
            nil
            (catch Throwable error error))]
      (is (identical? execution thrown))
      (is (some #(identical? cleanup-error %)
                (seq (.getSuppressed ^Throwable execution))))
      (is (= :infrastructure-failure (:status @published)))
      (is (= :gravity/stage3-infrastructure-failure-v1 (:schema @published)))
      (is (= "STAGE3-DELEGATE-CONTRACT" (:error-id @published))))))

(deftest catalog-drift-publishes-bound-infrastructure-failure
  (let [published (atom nil)
        arguments (bound-report-arguments :primitive-pure)
        missing-catalog (fn [_]
                          {primitive-ns []})
        thrown
        (try
          (with-redefs [runner/cleanup! (fn [] nil)
                        runner/publish-report!
                        (fn [_ receipt] (reset! published receipt))]
            (binding [runner/*catalog-loader* missing-catalog
                      runner/*delegate-run-test-vars* (fn [_] (throw (ex-info "delegate must not run" {})))
                      runner/*exit-fn* (fn [_] nil)]
              (apply runner/-main arguments)))
          nil
          (catch Throwable error error))
        receipt @published]
    (is (instance? clojure.lang.ExceptionInfo thrown))
    (is (= "STAGE3-CATALOG-MISSING-TEST-VAR" (:id (ex-data thrown))))
    (is (= :gravity/stage3-infrastructure-failure-v1 (:schema receipt)))
    (is (= :infrastructure-failure (:status receipt)))
    (is (= "/tmp/stage3-test-report.json" (:report-file receipt)))
    (is (= "test-nonce" (:nonce receipt)))
    (is (= "test-check" (:check-id receipt)))
    (is (= "sha256:0000000000000000000000000000000000000000000000000000000000000000"
           (:command-identity-sha256 receipt)))
    (is (= "STAGE3-CATALOG-MISSING-TEST-VAR" (:error-id receipt)))
    (is (<= (count (:error-message receipt)) 1024))
    (is (not (contains? receipt :selection-order)))
    (is (false? (:authoritative? receipt)))))

(deftest report-publication-never-replaces-target-created-after-preflight
  (let [directory (java.nio.file.Files/createTempDirectory
                   "stage3-report-race"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        target (.resolve directory "report.json")
        attacker-bytes (.getBytes "attacker-wins\n"
                                  java.nio.charset.StandardCharsets/UTF_8)
        thrown
        (try
          (binding [runner/*before-report-link-hook*
                    (fn [raced-target _temporary]
                      (java.nio.file.Files/write
                       raced-target
                       attacker-bytes
                       (into-array java.nio.file.OpenOption
                                   [java.nio.file.StandardOpenOption/CREATE_NEW
                                    java.nio.file.StandardOpenOption/WRITE])))]
            (runner/publish-report! (str target)
                                    {:schema :gravity/stage3-test-report-v1
                                     :stage :stage3
                                     :status :infrastructure-failure}))
          nil
          (catch Throwable error error))]
    (try
      (is (instance? java.nio.file.FileAlreadyExistsException thrown))
      (is (= (seq attacker-bytes)
             (seq (java.nio.file.Files/readAllBytes target))))
      (finally
        (java.nio.file.Files/deleteIfExists target)
        (java.nio.file.Files/deleteIfExists directory)))))

(deftest cli-lifecycle-cleans-up-and-preserves-fatal-and-interrupt
  (doseq [throwable [(Error. "fatal") (InterruptedException. "interrupt")]]
    (let [cleaned (atom 0)]
        (with-redefs [runner/cleanup! #(swap! cleaned inc)]
          (binding [runner/*catalog-loader* nil
                  runner/*require-report?* false
                  runner/*exit-fn* (fn [_] nil)
                  runner/*delegate-run-test-vars*
                  (fn [_] (throw throwable))]
          (is (identical? throwable
                          (try
                            (runner/-main "--batch" "primitive-pure")
                            nil
                            (catch Throwable error error))))))
      (is (= 1 @cleaned)))))

(deftest no-generic-selector-interface-is-exposed-by-cli
  (doseq [arguments [["--exact" "foo"]
                     ["--namespace" "foo"]
                     ["--test-var" "foo/bar"]
                     ["--max-cache-entries" "1"]]]
    (is (= "STAGE3-CLI-ARBITRARY-SELECTOR"
           (:id (ex-data (try
                           (runner/parse-arguments arguments)
                           nil
                           (catch clojure.lang.ExceptionInfo error error))))))))

(deftest fragment-preflight-child-semantics-and-sh05-differential-seam
  (let [children (fn [value]
                   (cond
                     (map? value) (vec (mapcat identity value))
                     (set? value) (vec (sort-by pr-str value))
                     (or (vector? value) (seq? value)) (vec value)
                     :else []))
        forms ['(ns fixture)
               [:def 'small [:vector 1 {:a #{:b :c}}]]]
        authority {:expanded-syntax-stream (mapv #(hash-map :form %) forms)}
        loaded {:expanded-source-forms forms
                :source-path "fixture.gravity"
                :source-sha256 "sha256:fixture"
                :source-id "sha256:source"
                :children-fn children
                :expanded-form-fn
                (fn [form]
                  (if (and (seq? form) (= 'defn (first form)))
                    (let [[_ name parameters & body] form]
                      (list 'def name (apply list 'fn parameters body)))
                    form))}]
    (is (= :passed
           (:status (fragment/differential-expanded-stream!
                     forms authority))))
    (is (= 1 (count (fragment/fragment-root-records loaded))))
    (is (= 11 (fragment/canonical-form-tree-size children (second forms))))
    (let [regression
          (get-in (fragment/check-fragment-root-bound! loaded)
                  [:regression-evidence :historical-f986-first-offender])]
      (is (= 1227 (:observed regression)))
      (is (= 178 (:ordinal regression)))
      (is (= 'sh08-ft-function-type-core-artifact (:function regression)))
      (is (= "sha256:4f9ff8f11b347afc17984acd558fdbb925cdbc8e1f1e329997ff7a04930ac320"
             (:source-sha256 regression))))))

(deftest source-control-if-arity-is-exact-and-fails-closed
  (let [children (fn [value]
                   (cond
                     (map? value) (vec (mapcat identity value))
                     (set? value) (vec (sort-by pr-str value))
                     (or (vector? value) (seq? value)) (vec value)
                     :else []))]
    (is (empty? (fragment/source-control-form-arity-errors
                 children ['(if :a :b :c)])))
    (is (= [3]
           (mapv :observed
                 (fragment/source-control-form-arity-errors
                  children ['(if :a :b)]))))
    (is (= [6]
           (mapv :observed
                 (fragment/source-control-form-arity-errors
                  children ['(if :a :b :c :d :e)]))))))
