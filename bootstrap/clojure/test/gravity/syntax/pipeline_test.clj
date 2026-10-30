(ns gravity.syntax.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.syntax :as syntax]))

(defn- private-operation [name & args]
  (apply (var-get (ns-resolve 'gravity.bootstrap name)) args))

(defn- build-operations [] (private-operation 'syntax-stage-build-operations))
(defn- reader-operations [] (private-operation 'syntax-stage-reader-verification-operations))
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
    (is (= #{'build 'verify 'reader-products-authentic?}
           (set (keys (ns-publics 'gravity.syntax)))))
    (is (= 'gravity.syntax (:namespace contract)))
    (is (= :stage0-syntax-construction-and-verification-pipeline (:contract-boundary contract)))
    (is (= ['clojure.core 'gravity.c3-syntax-verification]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.cli 'gravity.release]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{'gravity.c3-syntax-verification}
           (set (map ns-name (vals (ns-aliases 'gravity.syntax))))))
    (is (= (set (keys (build-operations))) (set (get-in contract [:operation-inputs :build]))))
    (is (= (set (keys (reader-operations)))
           (set (get-in contract [:operation-inputs :reader-products-authentic?]))))
    (is (false? (:injected-operations-are-proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
