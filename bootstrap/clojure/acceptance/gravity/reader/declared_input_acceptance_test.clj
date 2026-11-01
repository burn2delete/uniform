(ns gravity.reader.declared-input-acceptance-test
  "UNRUN normal-entry acceptance DEFINITIONS. This root is absent from default
  classpaths. Requiring this namespace loads Bootstrap; even --list is execution
  outside the SOURCE-only cut. No provider read-count observer is available."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.build.invocation :as invocation]
            [gravity.cli.entrypoint :as entrypoint]
            [gravity.digest :as digest]
            [gravity.tooling.strict-json :as json]))

(def ^:private fixture-root "bootstrap/clojure/fixtures/reader-extension-binding/")
(def ^:private resource "bootstrap/clojure/fixtures/accepted/reader-extension-input.txt")
(def ^:private principal "package/reader-normal-acceptance")

(defn- fixture [stem extension]
  (let [path (.getCanonicalPath (io/file fixture-root (str stem extension)))]
    {:path path :text (slurp path :encoding "UTF-8")}))

(defn- policy [keys]
  {:principals #{principal} :profiles #{:meta} :targets #{:jvm}
   :provider 'gravity.build/hermetic-inputs :provider-version "1"
   :phase :build :lifetime :invocation :scope (set keys)
   :effects #{:build/read-file} :capabilities #{:fs/read}
   :audit :required :replay :recorded})

(defn- implementation-constraints [path]
  ;; Actual implementation identities are narrowing DATA, not grant issuance.
  ;; Both bindings come from the real compiler; neither is a mocked plan.
  (let [reader (#'bootstrap/sh03-reader-current-binding!
                path bootstrap/sh03-reader-source-relative-path)
        handler (#'bootstrap/reader-resource-handler-binding! path)]
    {:implementation (bootstrap/reader-canonical-hash
                      {:reader (dissoc reader :plan) :handler (:identity handler)})
     :policy-id (#'bootstrap/reader-resource-policy-id)}))

(defn- command [source inputs requirements]
  (into ["build" "--principal" principal "--profile" "meta" "--target" "jvm"
         "--format" "json" "--package-policy" (pr-str requirements)
         "--workspace-policy" (pr-str requirements)]
        (concat (mapcat #(vector "--allow-read" %) (sort (:scope requirements)))
                ["--reader-inputs"
                 (pr-str (merge {:source source :source-requirements requirements :inputs inputs}
                                (implementation-constraints (:path source))))])))

(defn- failure-data [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- resource-artifact? [artifact]
  (= 3 (get-in artifact [:reader-extension-invocation-records 0 :schema-version])))

(defn- normal-build! [arguments inspect-live]
  ;; Observe the EXISTING C2 consuming Var while the normal command still owns
  ;; its live issuer. Always forward the original authority/path/context to the
  ;; unchanged consumer. No parser, compiler, issuer, handler, provider or result
  ;; replacement; no authority object escapes this lexical observer.
  (let [consume @#'bootstrap/sh03-c3-precomputed-c2-verify!
        observations (atom 0)
        artifact (with-redefs-fn
                   {#'bootstrap/sh03-c3-precomputed-c2-verify!
                    (fn [& arguments]
                      (let [verified (apply consume arguments)]
                        (when (and (= 4 (count arguments)) (resource-artifact? verified))
                          (swap! observations inc)
             ;; These really re-enter fresh reconstruction in verification mode.
             ;; They are NOT a causal provider-read counter.
                          (is (= verified (apply consume arguments)))
                          (is (= verified (apply consume arguments)))
                          (inspect-live verified
                                        (fn [candidate]
                                          (apply consume (assoc (vec arguments) 2 candidate)))))
                        verified))}
                   #(invocation/run-declared-input-build! arguments))]
    ;; Count matching live consumers, never provider reads. This check must
    ;; run after every successful command, even when its observer never fires.
    (is (= 1 @observations))
    artifact))

(defn- rehash-c2 [artifact]
  ;; Public identity recomputation is DATA, never authentication authority.
  (let [hashes (bootstrap/c2-incremental-hashes
                (:source-unit-record artifact) (:token-stream artifact)
                (:form-tree artifact) (:syntax-seed-stream artifact)
                (:reader-extension-invocation-records artifact) (:reader-diagnostics artifact))
        integrity (bootstrap/c2-reader-product-integrity-record
                   (:source-unit-record artifact) (:top-level-form-ids artifact) hashes
                   (:literal-decoding-records artifact)
                   (get-in artifact [:semantic-error-deferment-record :deferred-literal-records]))
        artifact (assoc artifact :incremental-reader-hashes hashes :reader-product-integrity integrity)]
    (assoc artifact :artifact-id (bootstrap/c2-reader-artifact-id artifact))))

(defn- assert-artifact! [artifact source requirements expected-inputs]
  (let [record (first (:reader-extension-invocation-records artifact))
        calls (:invocations record) unit (:source-unit-record artifact)
        {:keys [policy-id implementation]} (implementation-constraints (:path source))]
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind artifact)))
    (is (= 1 (count (:reader-extension-invocation-records artifact))))
    (is (= 3 (:schema-version record)))
    (is (= (#'bootstrap/reader-resource-policy) (:policy record)))
    (is (= 'gravity.reader/read-input (:handler record)))
    (is (= #{:build/read-file} (:build-effects record)))
    (is (= #{:fs/read} (:capabilities record)))
    (is (= #{:meta} (:profiles record)))
    (is (= :invoked (:status record)))
    (is (= policy-id (get-in unit [:reader-options :extension-policy])))
    (is (= (str "sha256:" (digest/sha256-bytes-hex
                           (.getBytes ^String (:text source) java.nio.charset.StandardCharsets/UTF_8)))
           (:bytes-hash unit)))
    (is (= (count expected-inputs) (count calls)))
    (is (= (count calls) (count (set (map :form-id calls)))))
    (doseq [[call [key content]] (map vector calls expected-inputs)]
      (let [seed (:input-seed call) returned (:returned-seed call)
            request (get-in call [:invocation :request])]
        (is (= (:source-id unit) (:source-id seed) (:source-id request)))
        (is (= (:bytes-hash unit) (:source-content-id seed) (:source-content-hash request)))
        (is (= (:form-id call) (:form-id seed) (:form-id request)))
        (is (= (:payload-form-id call) (:payload-form-id seed) (:payload-form-id request)))
        (is (= (:span call) (:span seed) (:span request)))
        (is (= key (:datum seed) (:input request)))
        (is (= (assoc seed :datum content) returned))
        (is (= implementation (:implementation request)))
        (is (= policy-id (:policy-id request)))
        (is (= principal (:principal request)))
        (is (= 'gravity.reader/read-input (get-in call [:invocation :handler]) (:handler request)))
        (is (= 'gravity.build/hermetic-inputs (:provider request)))
        (is (= [:meta :jvm :build :invocation "1"]
               ((juxt :profile :target :phase :lifetime :provider-version) request)))
        (is (contains? (:scope requirements) (:input request)))))))

(deftest normal-granted-pair-and-distinct-same-key-occurrences
  (let [contents ["" "declared input text" (str "input " (char 0x03bb))]]
    (doseq [extension [".qst" ".gravity"] content contents copies [1 2]]
      (let [source (fixture (if (= copies 1) "granted" "repeated-granted") extension)
            requirements (policy [resource])
            arguments (command source [{:resource resource :content content}] requirements)
            observed (atom [])
            artifact (normal-build! arguments
                                    (fn [value _] (swap! observed conj value)
                                      (assert-artifact! value source requirements (repeat copies [resource content]))))]
        (is (= 1 (count @observed)))
        (is (= artifact (first @observed)))
        ;; Returned data and public hashes cannot extend the live invocation.
        (is (= "C2-HASH"
               (:id (failure-data #(bootstrap/c3-reader-artifact-view
                                    artifact {:reader-source-path bootstrap/sh03-reader-source-relative-path
                                              :authenticated-envelope-options {:source :development-current}})))))))))

(deftest normal-live-verifier-refuses-tampered-duplicate-missing-and-stale-records
  (doseq [extension [".qst" ".gravity"]]
    (let [source (fixture "granted" extension) requirements (policy [resource])
          arguments (command source [{:resource resource :content "original return"}] requirements)
          saved (normal-build! arguments (fn [_ _]))]
      ;; A prior result cannot replace this invocation's different original
      ;; return. Equal public data alone does not prove a stale receipt.
      (normal-build! (command source [{:resource resource :content "new invocation return"}] requirements)
                     (fn [artifact verify]
                       (is (= "C2-HASH" (:id (failure-data #(verify saved)))))
                       (is (= "C2-HASH" (:id (failure-data
                                              #(verify (assoc artifact :artifact-id "sha256:stale"))))))
                       (doseq [changed [(assoc artifact :reader-extension-invocation-records [])
                                        (assoc-in artifact [:source-unit-record :bytes-hash] "sha256:stale")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations] [])
                                        (update-in artifact [:reader-extension-invocation-records 0 :invocations] #(into % %))
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :policy] {})
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :handler] 'unregistered)
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :input-seed :datum] "other input")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :returned-seed :datum] "forged")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :payload-form-id] "other")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :invocation :request :implementation] "sha256:stale")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :invocation :request :policy-id] "sha256:stale")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :invocation :request :input] "other input")
                                        (assoc-in artifact [:reader-extension-invocation-records 0 :invocations 0 :invocation :request :source-content-hash] "sha256:stale")]]
                         (is (= "C2-HASH" (:id (failure-data #(verify changed)))))
                         (is (= "C2-HASH" (:id (failure-data #(verify (rehash-c2 changed)))))))))
      ;; A different original source, even with the same resource and return,
      ;; cannot admit a product built for the earlier source.
      (normal-build! (command (fixture "changed-source" extension)
                              [{:resource resource :content "original return"}] requirements)
                     (fn [_ verify]
                       (is (= "C2-HASH" (:id (failure-data #(verify saved))))))))))

(deftest normal-complete-population-and-policy-refusals
  (doseq [extension [".qst" ".gravity"]]
    (let [source (fixture "granted" extension) requirements (policy [resource])
          inputs [{:resource resource :content "declared"}]
          arguments (command source inputs requirements)]
      (is (= {:id "L15-CONTRACT" :reason :undeclared-input}
             (select-keys (failure-data #(invocation/run-declared-input-build!
                                          (command source [] requirements))) [:id :reason])))
      ;; Keep source requirements and declared store entries intact; remove
      ;; only the explicit host permission. Source DATA cannot restore it.
      (is (= "T1003" (:id (failure-data
                           #(invocation/run-declared-input-build!
                             (into [(first arguments)]
                                   (mapcat identity
                                           (remove (fn [[flag _]] (= "--allow-read" flag))
                                                   (partition 2 (rest arguments))))))))))
      (doseq [[case stem expected]
              [[:unknown "unknown" "C2-HASH"]
               [:resource-then-unknown "resource-unknown" "C2-HASH"]
               [:resource-then-outside-grant "resource-outside-grant" "L15-CONTRACT"]
               ;; Fixed effectful policy enables resource only, not text.
               [:disallowed-text-then-resource "mixed-effect" "C2-HASH"]]]
        (testing (str case extension)
          (let [consumed (atom 0)]
            (is (= expected (:id (failure-data
                                  #(normal-build! (command (fixture stem extension) inputs requirements)
                                                  (fn [_ _] (swap! consumed inc)))))))
            ;; This counts verified consumer products, NOT provider reads.
            (is (zero? @consumed)))))
      (doseq [stem ["disallowed" "undeclared-effect"]]
        (let [denied (if (= stem "disallowed") (assoc requirements :scope #{"other"})
                         (assoc requirements :effects #{}))
              inputs (if (= stem "disallowed") [{:resource "other" :content "other"}] inputs)]
          (is (= (if (= stem "disallowed") "L15-CONTRACT" "T1003")
                 (:id (failure-data #(invocation/run-declared-input-build!
                                      (command (fixture stem extension) inputs denied)))))))))))

(deftest normal-cli-json-is-the-real-command-projection
  (let [source (fixture "granted" ".gravity") requirements (policy [resource])
        arguments (command source [{:resource resource :content "not public output"}] requirements)
        stderr (java.io.StringWriter.) statuses (atom []) diagnostics (atom [])
        stdout (binding [*err* stderr]
                 (with-out-str
                   (entrypoint/run! arguments
                                    {:resolve-operation #(ns-resolve 'gravity.bootstrap %)
                                     :print-diagnostic! #(swap! diagnostics conj %)
                                     :exit! #(swap! statuses conj %)})))
        report (json/read-strict-json stdout)]
    (is (empty? @statuses))
    (is (empty? @diagnostics))
    (is (= "" (str stderr)))
    (is (= "passed" (get report "status")))
    (is (= "gravity/stage0-c2-reader-document-artifact" (get report "artifactKind")))
    (is (false? (get report "runtimeAccepted")))
    (is (not (.contains stdout "not public output")))))
