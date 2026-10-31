(ns gravity.syntax.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.c3-literal-projection :as literal]
            [gravity.syntax :as syntax]))

(defn- private-operation [name & args]
  (apply (var-get (ns-resolve 'gravity.bootstrap name)) args))

(defn- build-operations [] (private-operation 'syntax-stage-build-operations))
(defn- reader-operations [] (private-operation 'syntax-stage-reader-verification-operations))
(defn- runtime-operations [] (private-operation 'syntax-stage-runtime-operations))
(defn- view-operations [] (private-operation 'syntax-stage-reader-view-operations))
(defn- assembly-operations [] (private-operation 'syntax-stage-assembly-operations))
(defn- descriptor-operations [] (private-operation 'syntax-stage-descriptor-operations))
(defn- index-operations [] (private-operation 'syntax-stage-index-operations))
(defn- reader-path [] (.getCanonicalPath (io/file bootstrap/sh03-reader-source-relative-path)))
(defn- invocation []
  {:reader-source-path (reader-path)
   :authenticated-envelope-options {:source :development-current}})
(defn- verification-operations []
  (private-operation 'c3-syntax-verification-ops (invocation)))
(defn- source-path [extension]
  (.getCanonicalPath
   (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-04/accepted/source-syntax" extension))))
(defn- request [path text]
  {:source-path path :source-text text
   :project-context (bootstrap/reader-project-context-for-source path)
   :precomputed-c2-artifact nil :candidate nil
   :reader-source-path (reader-path) :invocation (invocation)})
(defn- construct [extension]
  (syntax/build (request (source-path extension) "1") (build-operations)))
(def ^:private gravity-product (delay (construct ".gravity")))
(def ^:private qst-product (delay (construct ".qst")))
(defn- caught [thunk]
  (try (thunk) ::unexpected-success
       (catch clojure.lang.ExceptionInfo error error)))

(deftest genuine-paired-products-use-the-stage-and-preserve-identity
  (doseq [[extension product] [[".gravity" gravity-product] [".qst" qst-product]]]
    (let [path (source-path extension)
          artifact @product
          compatibility (bootstrap/compiler-c3-syntax-source-artifact
                         path "1" (bootstrap/reader-project-context-for-source path)
                         nil nil (reader-path) (invocation))]
      (is (= artifact compatibility))
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact)))
      (is (= 2 (count (:syntax-object-stream artifact))))
      (is (= :passed (get-in artifact [:syntax-verification-report :status])))
      (is (= :complete (get-in artifact [:capability-based-proof :status])))
      (is (= :complete (syntax/verify path artifact (verification-operations))))
      (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))
      (is (= path (get-in artifact [:c2-reader-artifact :source-unit-record :path])))
      (is (= extension (get-in artifact [:c2-reader-artifact :source-unit-record :extension])))
      (is (= (:artifact-id artifact) (bootstrap/c3-artifact-id artifact)))
      (is (= :gravity-source (get-in artifact [:gravity-syntax-boundary :owner])))
      (is (= (get-in artifact [:gravity-syntax-boundary :gravity-syntax-serialization :semantic-payload])
             (get-in artifact [:gravity-syntax-boundary :gravity-syntax-deserialization :semantic-payload])))
      (is (true? (syntax/reader-products-authentic?
                  (:syntax-object-stream artifact) (:c2-reader-artifact artifact)
                  (:gravity-syntax-boundary artifact) (invocation) (reader-operations)))))))

(deftest genuine-source-file-retains-metadata-hygiene-origins-and-unicode
  (let [path (source-path ".gravity")
        text (slurp path)
        artifact (syntax/build (request path text) (build-operations))
        stream (:syntax-object-stream artifact)
        generated (first (filter #(= :generated-form (get-in % [:form :kind])) stream))]
    (is (= text (slurp (source-path ".qst"))))
    (is (= :passed (get-in artifact [:syntax-verification-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= 5 (count stream)))
    (is (some #(= "Metadata survives C3 construction." (get-in % [:metadata :doc])) stream))
    (is (seq (get-in generated [:hygiene :marks])))
    (is (seq (get-in generated [:hygiene :captures])))
    (is (seq (or (get-in generated [:origin 0 :input-syntax-ids])
                 (get-in generated [:origin 0 :inputs]))))
    (is (seq (get-in artifact [:fact-ledger :attached])))
    (is (seq (get-in artifact [:fact-ledger :invalidated])))
    (is (= (str "sha256:" (bootstrap/sha256-hex text))
           (get-in artifact [:c2-reader-artifact :source-unit-record :bytes-hash])))
    (is (.contains (pr-str stream) "\u03bb"))
    (is (.contains (pr-str stream) "\ud83d\ude00"))
    (is (= (:artifact-id artifact) (bootstrap/c3-artifact-id artifact)))))

(deftest real-lexical-and-syntax-errors-preserve-diagnostic-ownership
  (doseq [extension [".gravity" ".qst"]]
    (let [path (source-path extension)
          lexical (caught #(syntax/build (request path "[") (build-operations)))
          bad-source (.getCanonicalPath
                      (io/file (str "bootstrap/clojure/fixtures/rejected/compiler-c3-shape" extension)))
          syntax-error (caught #(syntax/build (request bad-source (slurp bad-source)) (build-operations)))]
      (is (= "C2-DELIMITER" (:id (ex-data lexical))))
      (is (= "C3-SHAPE" (:id (ex-data syntax-error))))
      (is (= :fixture-override (:producer (ex-data syntax-error))))
      (is (= bad-source (get-in (ex-data syntax-error) [:source-span :source]))))))

(deftest genuine-products-reject-source-syntax-envelope-and-serialization-substitution
  (let [path (source-path ".gravity") artifact @gravity-product]
    (doseq [[changed expected]
            [[(assoc-in artifact [:syntax-object-stream 0 :form :value] 2) "C3-FACT-STALE"]
             [(assoc-in artifact [:gravity-syntax-boundary :reader-source-revision :revision-id]
                        "sha256:substituted") "C3-FACT-STALE"]
             [(assoc-in artifact [:c2-reader-artifact :source-unit-record :bytes-hash]
                        "sha256:substituted") "C3-FACT-STALE"]
             [(assoc-in artifact [:syntax-serialization-fixture :roundtrip?] false) "C3-SERIALIZE"]]]
      (let [error (caught #(syntax/verify path changed (verification-operations)))]
        (is (= expected (:id (ex-data error))))
        (is (= path (get-in (ex-data error) [:source-span :source])))))
    (is (false? (syntax/reader-products-authentic?
                 (:syntax-object-stream artifact) (:c2-reader-artifact artifact)
                 (assoc (:gravity-syntax-boundary artifact) :owner :substituted)
                 (invocation) (reader-operations))))))

(deftest constructor-gates-and-late-interposition-remain-visible
  (let [path (source-path ".gravity") input (request path "1")
        operations (build-operations)
        reached (atom false)
        refused (caught #(syntax/build
                          (assoc input :candidate (Object.))
                          (assoc operations :compiler-c2-reader-source-artifact
                                 (fn [& _] (reset! reached true)))))
        sentinel (ex-info "interposed owner" {:id "C3-TEST"})]
    (is (= "C2-HASH" (:id (ex-data refused))))
    (is (= [:direct-current-sh03-reader-construction] (:missing-fields (ex-data refused))))
    (is (false? @reached))
    (testing "operations captured before a redefinition still call the owning Var"
      (is (identical? sentinel
                      (with-redefs [bootstrap/c3-syntax-overrides-from-forms
                                    (fn [_] (throw sentinel))]
                        (caught #(syntax/build input operations))))))
    (testing "the bootstrap constructor delegates its explicit request"
      (let [seen (atom nil)]
        (is (identical? sentinel
                        (with-redefs [syntax/build
                                      (fn [actual ports] (reset! seen [actual ports]) (throw sentinel))]
                          (caught #(bootstrap/compiler-c3-syntax-source-artifact
                                    path "1" (:project-context input) nil nil
                                    (reader-path) (invocation))))))
        (is (= input (first @seen)))
        (is (= (set (keys operations)) (set (keys (second @seen)))))))))

(deftest namespace-contract-is-explicit-and-does-not-load-the-orchestrator
  (let [contract (:gravity/namespace-contract (meta #'syntax/build))]
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.syntax)))))
    (is (= 'gravity.syntax (:namespace contract)))
    (is (= :stage0-syntax-construction-verification-and-pinned-plan-runtime (:contract-boundary contract)))
    (is (= ['clojure.core 'clojure.string 'gravity.c3-reader-expansions 'gravity.c3-syntax-verification]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.cli 'gravity.release]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{'clojure.string 'gravity.c3-reader-expansions 'gravity.c3-syntax-verification}
           (set (map ns-name (vals (ns-aliases 'gravity.syntax))))))
    (is (= (set (keys (build-operations))) (set (get-in contract [:operation-inputs :build]))))
    (is (= (set (keys (reader-operations)))
           (set (get-in contract [:operation-inputs :reader-products-authentic?]))))
    (is (= (set (keys (private-operation 'syntax-stage-runtime-operations)))
           (set (get-in contract [:operation-inputs :runtime]))))
    (is (= (set (keys (view-operations)))
           (set (get-in contract [:operation-inputs :reader-views]))))
    (is (= (set (keys (assembly-operations)))
           (set (get-in contract [:operation-inputs :assembly]))))
    (is (= (set (keys (descriptor-operations)))
           (set (get-in contract [:operation-inputs :descriptor]))))
    (is (= (set (keys (index-operations)))
           (set (get-in contract [:operation-inputs :native-index]))))
    (is (false? (:injected-operations-are-proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))

(def ^:private runtime-template
  (delay
    (let [artifact @gravity-product
          resolved (get-in artifact [:gravity-syntax-boundary :resolved-syntax-result])
          reader-binding (:reader-binding resolved)
          revision (:reader-source-revision resolved)
          binding (bootstrap/sh04-syntax-current-binding! (source-path ".gravity"))
          base (:syntax-object (first (:resolved-object-products resolved)))
          product (bootstrap/sh04-syntax-generated-products!
                   (source-path ".gravity") binding base
                   (:semantic-source-id reader-binding) reader-binding revision)]
      {:binding binding :reader-binding reader-binding :revision revision :product product})))

(deftest pinned-runtime-builds-the-real-plan-and-rechecks-cache-freshness
  (let [path (source-path ".gravity") operations (runtime-operations)
        cache (syntax/plan-binding-cache operations)
        _ (is (false? (realized? cache)))
        binding (syntax/current-plan-binding! path cache operations)]
    (is (realized? cache))
    (is (= (bootstrap/sh04-syntax-current-binding! path) binding))
    (is (= :gravity/sh04-pinned-syntax-plan-binding (:artifact binding)))
    (is (= :gravity-source (:semantic-authority binding)))
    (is (= bootstrap/sh04-syntax-expected-source-content-hash (:source-content-hash binding)))
    (is (= bootstrap/sh04-syntax-expected-source-byte-count (:source-byte-count binding)))
    (is (= bootstrap/sh04-syntax-expected-function-count (count (get-in binding [:plan :functions]))))
    (is (= (select-keys binding (keys (syntax/plan-identities (:plan binding) operations)))
           (syntax/plan-identities (:plan binding) operations)))
    (doseq [changed [(assoc binding :source-content-hash "sha256:substituted")
                     (assoc binding :source-byte-count 0)
                     (update-in binding [:plan :functions] dissoc 'c3-syntax-verify-resolved)]]
      (let [error (caught #(syntax/current-plan-binding! path (delay changed) operations))]
        (is (= "C3-ID" (:id (ex-data error))))
        (is (= path (get-in (ex-data error) [:source-span :source])))))
    (let [bad-plan (update-in binding [:plan] update :functions dissoc 'c3-syntax-build-template)
          error (caught #(syntax/build-plan-binding!
                          path (assoc operations :p15-s23-stage2-compiler-artifact-plan
                                      (fn [& _] (:plan bad-plan)))))]
      (is (= "C3-ID" (:id (ex-data error))))
      (is (= [:pinned-syntax-plan-and-functions] (:missing-fields (ex-data error)))))))

(deftest pinned-source-refuses-real-byte-length-and-content-drift
  (let [path (source-path ".gravity") operations (runtime-operations)
        source (syntax/read-pinned-plan-source! path operations)
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file (:source-path source))))
        temporary (java.nio.file.Files/createTempFile
                   "gravity-syntax-pin-" ".gravity"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        selected (assoc operations :sh04-syntax-resolve-source-path (constantly (str temporary)))]
    (try
      (spit (str temporary) "1")
      (let [error (caught #(syntax/read-pinned-plan-source! path selected))]
        (is (= "C3-ID" (:id (ex-data error))))
        (is (= [:exact-pinned-syntax-source] (:missing-fields (ex-data error)))))
      (aset-byte bytes 0 (byte (bit-xor 1 (aget bytes 0))))
      (java.nio.file.Files/write temporary bytes (make-array java.nio.file.OpenOption 0))
      (let [error (caught #(syntax/read-pinned-plan-source! path selected))]
        (is (= "C3-ID" (:id (ex-data error))))
        (is (= [:pinned-syntax-source-identity] (:missing-fields (ex-data error))))
        (is (= path (get-in (ex-data error) [:source-span :source]))))
      (finally (java.nio.file.Files/deleteIfExists temporary)))))

(deftest real-template-replay-and-digest-resolution-preserve-and-authenticate-products
  (let [{:keys [binding reader-binding revision product]} @runtime-template
        path (source-path ".gravity") operations (runtime-operations) raw (:raw-result product)
        resolve! #(syntax/resolve-template! path binding % reader-binding revision operations)
        actual (resolve! raw)]
    (is (= product actual))
    (is (= :passed (get-in actual [:verification-report :status])))
    (is (= :passed (get-in actual [:resolved-verification-report :status])))
    (is (= (:semantic-binding-id reader-binding) (first (:resolved-digests actual))))
    (is (= (:serialization-id actual) (second (:resolved-digests actual))))
    (is (= reader-binding (get-in actual [:syntax :reader-binding])))
    (is (= revision (get-in actual [:syntax :reader-source-revision])))
    (doseq [[changed expected]
            [[(assoc raw :status :rejected) "C3-SHAPE"]
             [(assoc raw :schema-version 2) "C3-SHAPE"]
             [(assoc-in raw [:containment :downstream-artifacts-forbidden] true) "C3-SHAPE"]
             [(update raw :digest-requests #(vec (reverse %))) "C3-ID"]
             [(assoc-in raw [:syntax-template :syntax-id] {:digest-ref 0}) "C3-ID"]
             [(assoc-in raw [:digest-requests 1 :depends-on] []) "C3-ID"]]]
      (let [error (caught #(resolve! changed))]
        (is (= expected (:id (ex-data error))))
        (is (= path (get-in (ex-data error) [:source-span :source])))))
    (doseq [[changed-binding changed-revision]
            [[(assoc reader-binding :semantic-binding-id "sha256:substituted") revision]
             [reader-binding (assoc revision :source-content-hash "sha256:substituted")]]]
      (is (= "C3-ID" (:id (ex-data
                           (caught #(syntax/resolve-template!
                                     path binding raw changed-binding changed-revision operations)))))))))

(deftest digest-resolution-treats-only-declared-identity-slots-as-control-data
  (let [path (source-path ".gravity") operations (runtime-operations)
        semantic-data {:digest-ref 0 :ordinary-source-map {:digest-ref 1}}
        object {:syntax-id {:digest-ref 1} :form semantic-data}
        stream {:artifact-id {:digest-ref 0} :semantic-value semantic-data}
        preimage {:reader-binding {:semantic-binding-id {:digest-ref 0}}
                  :form semantic-data}
        resolved (syntax/resolve-request-preimage!
                  path {:ordinal 1 :depends-on [0] :preimage preimage} ["reader-id"] operations)]
    (is (= (assoc object :syntax-id "syntax-id")
           (syntax/resolve-object-template! path object ["reader-id" "syntax-id"] operations)))
    (is (= (assoc stream :artifact-id "stream-id")
           (syntax/resolve-stream-template! path stream ["stream-id"] operations)))
    (is (= (assoc-in preimage [:reader-binding :semantic-binding-id] "reader-id") resolved))
    (is (= semantic-data (syntax/resolve-stream-request-preimage!
                         path {:ordinal 0 :depends-on [] :preimage semantic-data} [] operations)))
    (doseq [thunk [#(syntax/resolve-object-template! path object ["only-one"] operations)
                   #(syntax/resolve-stream-template! path stream [] operations)
                   #(syntax/resolve-request-preimage! path {:ordinal 2 :depends-on []} [] operations)
                   #(syntax/resolve-request-preimage! path {:ordinal 0 :depends-on [1]} [] operations)
                   #(syntax/resolve-stream-request-preimage! path {:ordinal 1 :depends-on []} [] operations)]]
      (is (= "C3-ID" (:id (ex-data (caught thunk))))))))

(deftest result-carrier-admission-refuses-foreign-values-without-claiming-quotas
  (let [path (source-path ".gravity") operations (runtime-operations)
        raw (get-in @runtime-template [:product :raw-result])]
    (is (= raw (syntax/require-result-carrier! path :actual-syntax-result raw operations)))
    (is (= raw (syntax/accept-result! path raw operations)))
    (doseq [unsupported [(Object.) (with-meta {:value 1} {:host-only true})]]
      (let [error (caught #(syntax/require-result-carrier! path :invalid-carrier unsupported operations))]
        (is (= "C3-SHAPE" (:id (ex-data error))))
        (is (= [:bounded-syntax-result-carrier] (:missing-fields (ex-data error))))))
    ;; The shared validator's legacy limit arguments do not enforce quotas.
    ;; Preserve that explicit boundary rather than presenting them as bounds.
    (let [deep (reduce (fn [value _] [value]) nil (range 257))]
      (is (identical? deep (syntax/require-result-carrier! path :deep-native-carrier deep operations))))))

(deftest execution-contains-host-failures-and-propagates-interruptions
  (let [path (source-path ".gravity") operations (runtime-operations)
        run! (fn [error]
               (syntax/execute-plan! path {} 'test-function []
                                     (assoc operations :p15-s23-stage2-runtime-execute-function
                                            (fn [& _] (throw error)))))]
    (doseq [[error field]
            [[(StackOverflowError. "test") :bounded-syntax-host-stack]
             [(AssertionError. "test") :contained-syntax-assertion]
             [(UnsatisfiedLinkError. "test") :contained-syntax-linkage]
             [(ex-info "test" {:id "TEST"}) :contained-syntax-runtime-diagnostic]
             [(Exception. "test") :contained-syntax-host-failure]]]
      (let [diagnostic (ex-data (caught #(run! error)))]
        (is (= "C3-SHAPE" (:id diagnostic)))
        (is (= [field] (:missing-fields diagnostic)))
        (is (= path (get-in diagnostic [:source-span :source])))))
    (let [interruption (InterruptedException. "test")
          observed (try
                     (run! interruption) nil
                     (catch InterruptedException error
                       ;; Capture before clojure.test's reporting uses STM,
                       ;; which can consume the current thread's interrupt.
                       [error (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? interruption (first observed)))
      (is (true? (second observed))))))

(deftest runtime-ports-retain-late-interposition-and-strip-host-metadata
  (let [path (source-path ".gravity") operations (runtime-operations)
        arguments [(with-meta {:value 1} {:host-only true})]
        observed (atom nil)
        runner-var #'bootstrap/p15-s23-stage2-runtime-execute-function]
    (is (var? (:p15-s23-stage2-runtime-execute-function operations)))
    (with-redefs-fn
      {runner-var (fn [_ _ _ clean]
                    (reset! observed clean)
                    (with-meta {:result (first clean)} {:host-only true}))}
      #(let [result (syntax/execute-plan! path {} 'test-function arguments operations)]
         (is (= {:result {:value 1}} result))
         (is (nil? (meta result)))
         (is (nil? (meta (first @observed))))
         (is (nil? (meta (:result result)))))))
  (let [operations (runtime-operations) cache (syntax/plan-binding-cache operations)
        builder-var #'bootstrap/sh04-syntax-build-binding!]
    (with-redefs-fn {builder-var (fn [source] {:interposed-source source})}
      #(is (= {:interposed-source "<sh04-syntax-bootstrap>"} @cache)))))

(defn- literal-path [extension]
  (.getCanonicalPath
   (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-04/accepted/registered-literal-views" extension))))

(defn- literal-case [extension]
  (let [path (literal-path extension) text (slurp path)
        c2 (bootstrap/compiler-c2-reader-source-artifact
            path text (bootstrap/reader-project-context-for-source path)
            nil nil (reader-path) (invocation))
        artifact (syntax/build (request path text) (build-operations))]
    {:source path :text text :reader c2 :artifact artifact
     :registry (syntax/registered-literal-registry path c2 (view-operations))
     :view (syntax/reader-artifact-view c2 (invocation) (view-operations))}))

(def ^:private literal-gravity (delay (literal-case ".gravity")))
(def ^:private literal-qst (delay (literal-case ".qst")))

(deftest paired-literal-views-preserve-occurrences-identity-and-provenance
  (doseq [[extension product] [[".gravity" literal-gravity] [".qst" literal-qst]]]
    (let [{:keys [source reader artifact registry view]} @product
          operations (view-operations) boundary (:gravity-syntax-boundary artifact)
          bindings (get-in view [:registered-literal-projection :bindings])]
      (is (= view (:c2-reader-artifact artifact)
             (bootstrap/c3-reader-artifact-view reader (invocation))))
      (is (= view (syntax/reader-artifact-view reader nil operations)))
      (is (= (private-operation 'c3-current-reader-identity-view source reader)
             (syntax/current-reader-identity-view source reader operations)))
      (is (= 3 (count bindings) (count (:bindings registry))
             (count (set (map :form-id bindings)))))
      (is (= 2 (count (:lookup registry))))
      (is (= ['inst 'uuid 'uuid] (mapv :tag bindings)))
      (is (= (get-in bindings [1 :descriptor]) (get-in bindings [2 :descriptor])))
      (is (= "Registered literal provenance." (get-in artifact [:syntax-object-stream 0 :metadata :doc])))
      (is (= source (get-in view [:source-unit-record :path])))
      (is (= extension (get-in view [:source-unit-record :extension])))
      (is (= (get-in reader [:gravity-reader-boundary :authenticated-envelope-descriptor :source-revision])
             (get-in view [:sh03-reader-authentication :reader-source-revision])))
      (is (true? (syntax/registered-literal-projection-authentic? view boundary operations)))
      (is (true? (syntax/current-reader-revision-authentic? view boundary (invocation) operations)))
      (is (= :complete (syntax/verify source artifact (verification-operations))))
      (is (= :development-unadmitted
             (get-in boundary [:authenticated-envelope :source-rule :admission-status])))))
  (let [gravity @literal-gravity qst @literal-qst]
    (is (= (:text gravity) (:text qst)))
    (is (= (get-in gravity [:artifact :artifact-id]) (get-in qst [:artifact :artifact-id])))
    (is (= (get-in gravity [:view :registered-literal-projection :projection-id])
           (get-in qst [:view :registered-literal-projection :projection-id])))
    (is (not= (get-in gravity [:artifact :gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])
              (get-in qst [:artifact :gravity-syntax-boundary :authenticated-envelope :provenance-binding-id])))))

(deftest registered-occurrence-validation-rejects-real-reader-product-substitution
  (let [{:keys [source reader]} @literal-gravity operations (view-operations)
        form (first (filter #(= :tagged-literal (:kind %)) (:form-tree reader)))
        id (:form-id form)
        change-form (fn [f] (update reader :form-tree #(mapv (fn [record] (if (= id (:form-id record)) (f record) record)) %)))
        literal (first (filter #(= id (:form-id %)) (:literal-decoding-records reader)))
        changes [(change-form #(assoc % :value (java.util.Date. 1793059200001)))
                 (change-form #(assoc % :children []))
                 (update reader :literal-decoding-records #(filterv (fn [record] (not= id (:form-id record))) %))
                 (update reader :literal-decoding-records conj literal)
                 (update reader :literal-decoding-records
                         #(mapv (fn [record] (if (= id (:form-id record)) (assoc record :raw "substituted") record)) %))
                 (update reader :literal-decoding-records
                         #(mapv (fn [record] (if (= id (:form-id record)) (update-in record [:span :byte-end] inc) record)) %))
                 (assoc-in reader [:reader-extension-invocation-records 0 :invocations] [])
                 (update-in reader [:reader-extension-invocation-records 0 :invocations]
                            conj {:form-id :unregistered-form :raw (:raw form) :span (:span form)})]]
    (doseq [changed changes]
      (let [diagnostic (ex-data (caught #(syntax/registered-literal-registry source changed operations)))]
        (is (= "C3-FACT-STALE" (:id diagnostic)))
        (is (= :gravity.bootstrap.reader (:producer diagnostic)))
        (is (= source (get-in diagnostic [:source-span :source])))))))

(deftest native-literal-projection-retains-ordinary-data-and-refuses-unbound-values
  (let [{:keys [source registry reader]} @literal-gravity operations (view-operations)
        values (mapv :value (filter #(= :tagged-literal (:kind %)) (:form-tree reader)))
        projected (syntax/project-registered-literal-values
                   source registry {:ordinary {:digest-ref 0} :values values} operations)]
    (is (= {:digest-ref 0} (:ordinary projected)))
    (is (= (mapv :descriptor (:bindings registry)) (:values projected)))
    (is (= (get-in projected [:values 1]) (get-in projected [:values 2])))
    (doseq [value [(java.util.Date. 0)
                   (java.util.UUID/fromString "00000000-0000-0000-0000-000000000000")]]
      (let [diagnostic (ex-data (caught #(syntax/project-registered-literal-values source registry value operations)))]
        (is (= "C3-FACT-STALE" (:id diagnostic)))
        (is (= source (get-in diagnostic [:source-span :source])))))))

(deftest projection-and-revision-verification-reject-tampered-native-views
  (let [{:keys [source artifact view]} @literal-gravity
        boundary (:gravity-syntax-boundary artifact) operations (view-operations)]
    (doseq [changed [(assoc-in view [:registered-literal-projection :projection-id] "sha256:substituted")
                     (update-in view [:registered-literal-projection :bindings] pop)
                     (assoc-in view [:registered-literal-projection :bindings 0 :descriptor :semantic-validation] :substituted)
                     (assoc-in view [:registered-literal-projection :upstream-artifact-id] "sha256:substituted")
                     (assoc-in view [:registered-literal-projection :upstream-integrity-hash] "sha256:substituted")
                     (assoc-in view [:registered-literal-projection :reader-source-revision :owner] :substituted)]]
      (is (false? (syntax/registered-literal-projection-authentic? changed boundary operations))))
    (is (false? (syntax/current-reader-revision-authentic?
                 (assoc-in view [:sh03-reader-authentication :reader-source-revision :source-content-hash] "sha256:substituted")
                 boundary (invocation) operations)))
    (let [old (get-in view [:registered-literal-projection :bindings 0 :descriptor])
          replacement (assoc-in old [:canonical-value :epoch-milliseconds] "0")
          ;; Replace every occurrence and recompute public ids, while retaining
          ;; the original independently authenticated reader/envelope authority.
          projected (walk/postwalk #(if (= old %) replacement %) view)
          projection-id (bootstrap/p15-s23-c6c10-canonical-digest
                         source {:domain :gravity/sh04-registered-literal-projection-v1
                                 :bindings (get-in projected [:registered-literal-projection :bindings])})
          forged-view (assoc-in projected [:registered-literal-projection :projection-id] projection-id)
          forged-base (-> (walk/postwalk #(if (= old %) replacement %) artifact)
                          (assoc :c2-reader-artifact forged-view))
          forged (assoc forged-base :artifact-id (bootstrap/c3-artifact-id forged-base))]
      (is (false? (syntax/reader-products-authentic?
                   (:syntax-object-stream forged) forged-view (:gravity-syntax-boundary forged)
                   (invocation) (reader-operations))))
      (let [diagnostic (ex-data (caught #(syntax/verify source forged (verification-operations))))]
        ;; The unchanged verifier diagnoses serialization before association
        ;; facts. The separate predicate above checks reader authentication.
        (is (= "C3-SERIALIZE" (:id diagnostic)))
        (is (= source (get-in diagnostic [:source-span :source])))))))

(deftest reader-view-ingress-still-reverifies-internal-products-before-projection
  (let [{:keys [reader]} @literal-gravity operations (view-operations) projected? (atom false)
        observing (assoc operations :sh04-syntax-registered-literal-registry
                         (fn [& _] (reset! projected? true) (throw (ex-info "unexpected projection" {}))))]
    (doseq [[product ports] [[(assoc-in reader [:gravity-reader-boundary :owner] :substituted) observing]
                             [reader (assoc observing :sh03-reader-internal-product-authority (Object.))]]]
      (reset! projected? false)
      (is (= "C2-HASH" (:id (ex-data (caught #(syntax/reader-artifact-view product (invocation) ports))))))
      (is (false? @projected?)))))

(deftest reader-view-ports-preserve-interposition-private-wrappers-and-interrupts
  (let [{:keys [source reader view artifact]} @literal-gravity operations (view-operations)
        sentinel (ex-info "interposed registry" {:id "TEST-REGISTRY"})]
    (with-redefs [bootstrap/sh04-syntax-registered-literal-registry (fn [& _] (throw sentinel))]
      (is (identical? sentinel (caught #(syntax/current-reader-identity-view source reader operations)))))
    (doseq [name ['c3-current-reader-identity-view 'c3-current-reader-revision-authentic?]]
      (is (true? (:private (meta (ns-resolve 'gravity.bootstrap name))))))
    (with-redefs [syntax/reader-artifact-view (fn [actual-reader actual-invocation ports]
                                             [actual-reader actual-invocation (set (keys ports))])]
      (is (= [reader nil (set (keys operations))] (bootstrap/c3-reader-artifact-view reader)))
      (is (= [reader (invocation) (set (keys operations))]
             (bootstrap/c3-reader-artifact-view reader (invocation)))))
    (let [interruption (InterruptedException. "test")
          ports (assoc operations :p15-s23-c6c10-canonical-digest (fn [& _] (throw interruption)))
          observed (try (syntax/registered-literal-projection-authentic? view (:gravity-syntax-boundary artifact) ports) nil
                        (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
                        (finally (Thread/interrupted)))]
      (is (identical? interruption (first observed)))
      (is (true? (second observed))))))

(defn- assembly-case [case]
  (let [{:keys [source reader] :as actual} @case
        report (bootstrap/c3-validate-c2-reader-artifact! source reader)]
    (assoc actual :report report
           :assembly (syntax/assemble-fresh-reader! source reader report (invocation) (assembly-operations)))))

(def ^:private assembly-gravity (delay (assembly-case literal-gravity)))
(def ^:private assembly-qst (delay (assembly-case literal-qst)))

(deftest assembly-products-retain-real-stream-envelope-and-serialization-records
  (doseq [case [assembly-gravity assembly-qst]]
    (let [{:keys [source reader report artifact assembly]} @case
          operations (assembly-operations)]
      (is (= assembly (private-operation 'sh04-syntax-build-from-fresh-c2! source reader report (invocation))))
      (is (= (:resolved-result assembly) (get-in artifact [:gravity-syntax-boundary :resolved-syntax-result])))
      (is (= (:rich-syntax assembly) (:syntax-object-stream artifact)))
      (is (= (:envelope assembly) (get-in artifact [:gravity-syntax-boundary :authenticated-envelope])))
      (is (= :accepted (get-in assembly [:resolved-result :status])))
      (is (= :passed (get-in assembly [:stream-verification :status])))
      (is (= (get-in assembly [:serialization :semantic-payload])
             (get-in assembly [:deserialization :semantic-payload])))
      (is (= (:reader-binding assembly)
             (:reader-binding (syntax/reader-semantic-binding
                               source reader (syntax/semantic-source-id source (:source-unit-record reader) operations)
                               operations))))
      (is (= (:source-id (:source-unit-record reader))
             (get-in assembly [:reader-authentication-provenance :actual-reader-source-id]))))))

(deftest assembly-replays-real-results-and-rejects-stream-and-round-trip-substitution
  (let [{:keys [source reader report]} @assembly-gravity
        operations (assembly-operations) execute (:sh04-syntax-execute! operations)]
    (doseq [[function change expected missing]
            [['c3-syntax-stream-build-template #(assoc % :artifact :substituted) "C3-ID" :gravity-syntax-stream-template]
             ['c3-syntax-stream-verify-resolved #(assoc % :status :failed) "C3-ID" :fresh-resolved-syntax-stream-verification]
             ['c3-syntax-serialize-template #(assoc % :status :rejected) "C3-SERIALIZE" :gravity-syntax-stream-serialization]
             ['c3-syntax-deserialize-template #(assoc % :semantic-payload nil) "C3-SERIALIZE" :gravity-syntax-stream-round-trip]]]
      (let [ports (assoc operations :sh04-syntax-execute!
                         (fn [path binding called arguments]
                           (let [actual (execute path binding called arguments)]
                             (if (= function called) (change actual) actual))))
            diagnostic (ex-data (caught #(syntax/assemble-fresh-reader! source reader report (invocation) ports)))]
        (is (= expected (:id diagnostic)))
        (is (= [missing] (:missing-fields diagnostic)))
        (is (= source (get-in diagnostic [:source-span :source])))))))

(deftest assembly-custom-descriptors-cannot-retain-canonical-index-reuse
  (let [{:keys [source reader report assembly]} @assembly-gravity
        operations (assembly-operations)
        index (ns-resolve 'gravity.bootstrap 'sh04-syntax-form-index)
        descriptor (:sh04-syntax-source-descriptor operations)
        transaction (:syntax-with-source-index-transaction operations)
        canonical-observations (atom []) custom-observations (atom [])
        canonical (syntax/assemble-fresh-reader!
                   source reader report (invocation)
                   (assoc operations :syntax-with-source-index-transaction
                          (fn [artifact descriptor-operation thunk]
                            (transaction artifact descriptor-operation
                                         (fn []
                                           (swap! canonical-observations conj
                                                  (identical? (index artifact) (index artifact)))
                                           (thunk))))))
        custom (syntax/assemble-fresh-reader!
                source reader report (invocation)
                (assoc operations :sh04-syntax-source-descriptor
                       (fn [seed form artifact & rest-arguments]
                         (swap! custom-observations conj (identical? (index artifact) (index artifact)))
                         (apply descriptor seed form artifact rest-arguments))))]
    (is (= assembly canonical custom))
    (is (= [true] @canonical-observations))
    (is (= [false] @custom-observations))
    (is (nil? @(ns-resolve 'gravity.bootstrap '*sh04-syntax-form-index-transaction*)))
    (is (not (identical? (index reader) (index reader))))))

(deftest semantic-reader-revision-retains-count-shape-without-a-source-quota
  (let [{:keys [source reader artifact]} @assembly-gravity operations (assembly-operations)
        source-id (syntax/semantic-source-id source (:source-unit-record reader) operations)
        revision-path [:gravity-reader-boundary :authenticated-envelope-descriptor :source-revision :source-byte-count]]
    (doseq [count [0 1048577N 1099511627776N]]
      (let [changed (assoc-in reader revision-path count)
            binding (syntax/reader-semantic-binding source changed source-id operations)]
        ;; Structural projection is not authority for these deliberately
        ;; altered revisions. Independent current-source verification refuses.
        (is (= count (get-in binding [:reader-source-revision :source-byte-count])))
        (is (false? (syntax/current-reader-revision-authentic?
                     changed (:gravity-syntax-boundary artifact) (invocation) (view-operations))))))
    (doseq [count [-1 nil 1.5]]
      (let [diagnostic (ex-data (caught #(syntax/reader-semantic-binding
                                        source (assoc-in reader revision-path count) source-id operations)))]
        (is (= "C2-HASH" (:id diagnostic)))
        (is (= source (get-in diagnostic [:source-span :source])))))))

(deftest stage-source-descriptors-preserve-real-reader-facts-and-context-qualification
  (let [{:keys [source reader report assembly registry]} @assembly-gravity
        source-id (syntax/semantic-source-id source (:source-unit-record reader) (assembly-operations))
        seed (first (:syntax-seed-stream reader))
        form (:form-record (first (bootstrap/c2-top-level-products reader)))
        operations (descriptor-operations)
        arguments [seed form reader report source-id (:reader-binding assembly) (:reader-source-revision assembly) registry]
        transaction-var (ns-resolve 'gravity.bootstrap '*sh04-syntax-form-index-transaction*)
        canonical-slot (volatile! nil) custom-slot (volatile! nil)
        canonical (with-bindings {transaction-var {:artifact reader :thread (Thread/currentThread) :slot canonical-slot}}
                    (apply syntax/source-descriptor (into arguments [operations operations transaction-var])))
        custom (with-bindings {transaction-var {:artifact reader :thread (Thread/currentThread) :slot custom-slot}}
                 (apply syntax/source-descriptor
                        (into arguments [(assoc operations :c3-source-facts
                                                (fn [& args] (apply (:c3-source-facts operations) args))
                                                :descriptor-context (fn [_ thunk] (thunk)))
                                         operations transaction-var])))]
    (is (= canonical custom (apply bootstrap/sh04-syntax-source-descriptor arguments)))
    (is (some? @canonical-slot))
    (is (nil? @custom-slot))
    (is (= "Registered literal provenance." (get-in canonical [:metadata :doc])))
    (is (= (:reader-binding assembly) (:reader-binding canonical)))
    (is (= (:reader-source-revision assembly) (:reader-source-revision canonical)))
    (is (= :authenticated-reader-products (get-in canonical [:facts 0 :fact-kind])))
    (is (= source (get-in canonical [:origin 0 :span :source])))
    (is (= (syntax/empty-hygiene (get-in canonical [:namespace :current])) (:hygiene canonical)))
    (is (nil? (meta canonical)))))

(deftest stage-index-operations-and-new-consumer-roots-disable-reuse
  (let [artifact {:form-tree (mapv #(hash-map :form-id (keyword (str "f" %))) (range 20))}
        transaction {:artifact artifact :thread (Thread/currentThread) :slot (volatile! nil)}
        operations (index-operations)
        first-index (syntax/form-index artifact transaction operations operations)]
    (is (identical? first-index (syntax/form-index artifact transaction operations operations)))
    (is (= first-index (private-operation 'sh04-syntax-form-index artifact)))
    (is (not (identical? first-index
                        (syntax/form-index artifact transaction
                                           (assoc operations :runtime-supported? (fn [] true)) operations))))
    (is (not (identical? first-index
                        (syntax/form-index artifact transaction
                                           (assoc operations :qualify-transaction (fn [_ tx] tx)) operations))))
    (doseq [v [#'syntax/source-descriptor #'syntax/descendant-form-ids]]
      (let [original @v
            observed (with-redefs-fn {v (fn [& args] (apply original args))}
                       #(syntax/form-index artifact transaction operations operations))]
        (is (= first-index observed))
        (is (not (identical? first-index observed)))))
    (is (identical? first-index (syntax/form-index artifact transaction operations operations)))
    (is (= #{:a :b} (syntax/descendant-form-ids {:a {:children [:b]} :b {:children [:a]}} :a)))))

(deftest recursive-metadata-cleanup-keeps-late-callback-order-and-semantic-data
  (let [value (with-meta [{:k (with-meta '(x) {:host-only true})}] {:host-only true})
        original bootstrap/sh04-syntax-strip-host-metadata
        observations (atom [])
        result (with-redefs [bootstrap/sh04-syntax-strip-host-metadata
                            (fn [item]
                              (swap! observations conj
                                     (cond (vector? item) :vector (map? item) :map
                                           (keyword? item) :keyword (seq? item) :list :else :symbol))
                              (original item))]
                 (bootstrap/sh04-syntax-strip-host-metadata value))]
    (is (= '[{:k (x)}] result))
    (is (= [:vector :map :keyword :list :symbol] @observations))
    (is (nil? (meta result)))
    (is (nil? (meta (:k (first result)))))
    (is (= {:metadata {:doc "preserved"}}
           (bootstrap/sh04-syntax-strip-host-metadata {:metadata {:doc "preserved"}})))))

(deftest stage-literal-index-custom-operations-keep-the-factory-live
  (literal/with-operations (private-operation 'c3-literal-projection-ops)
   (fn []
    (let [artifact {:form-tree (mapv #(hash-map :form-id (keyword (str "f" %))) (range 20))}
        transaction {:artifact artifact :thread (Thread/currentThread) :slot (volatile! nil)}
        operations (index-operations)
        calls (atom 0)
        factory (fn [] (swap! calls inc)
                  (into {} (map (juxt :form-id identity)) (:form-tree artifact)))
        first-index (syntax/literal-index artifact :literal-forms :form-tree :form-id factory transaction operations operations)
        hit (syntax/literal-index artifact :literal-forms :form-tree :form-id factory transaction operations operations)
        custom (syntax/literal-index artifact :literal-forms :form-tree :form-id factory transaction
                                     (assoc operations :runtime-supported? (fn [] true)
                                            :qualify-transaction (fn [_ tx] tx)) operations)]
    (is (identical? first-index hit))
    (is (= first-index custom))
    (is (not (identical? first-index custom)))
    (is (= 2 @calls))
    (is (identical? first-index
                    (syntax/literal-index artifact :literal-forms :form-tree :form-id factory transaction operations operations)))))))
