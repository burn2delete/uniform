(ns gravity.self-hosting.authenticated-artifact-pipeline-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.authenticated-artifact-pipeline :as pipeline]
            [gravity.digest :as digest]
            [gravity.self-hosting.unified-execution-plan :as plan])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption StandardOpenOption]))

(def ^:private fixture "examples/hello.gravity")
(def ^:private plan-source
  "bootstrap/gravity/src/gravity/checked_core.gravity")

(defn- source-plan-catalog
  []
  ;; The reviewed catalog currently requests checked-core.  This test-only
  ;; projection exercises the same closed UEP stage contract at the earliest
  ;; source boundary without changing the catalog on disk or claiming a new
  ;; production route.
  (assoc-in plan/catalog [:modules "sh07-checked-core" :artifact-request]
            :source))

(defn- bound-plan-binding
  []
  (with-redefs [plan/catalog (source-plan-catalog)]
    (plan/artifact-pipeline-binding
     (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
     (pipeline/binding-identities))))

(defn- temp-source!
  [text]
  (let [path (Files/createTempFile
              "gravity-authenticated-pipeline-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (Files/write path (.getBytes text StandardCharsets/UTF_8)
                 (make-array OpenOption 0))
    path))

(defn- temp-project!
  [text]
  (let [root (Files/createTempDirectory
              "gravity-authenticated-project-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        src (Files/createDirectories
             (.resolve root "src")
             (make-array java.nio.file.attribute.FileAttribute 0))
        source (.resolve src "unit.gravity")]
    (Files/write (.resolve root "deps.edn")
                 (.getBytes "{}\n" StandardCharsets/UTF_8)
                 (make-array OpenOption 0))
    (Files/write source (.getBytes text StandardCharsets/UTF_8)
                 (make-array OpenOption 0))
    {:root root :source source}))

(defn- delete-project!
  [{:keys [root source]}]
  (Files/deleteIfExists source)
  (Files/deleteIfExists (.resolve root "src"))
  (Files/deleteIfExists (.resolve root "deps.edn"))
  (Files/deleteIfExists root))

(deftest earliest-artifact-request-builds-closed-prefix
  (pipeline/clear-cache!)
  (let [result (pipeline/run-pipeline
                {:source-path fixture :target-stage :reader :mode :fresh})]
    (is (= :reader (:requested-stage result)))
    (is (= [:source :reader]
           (vec (keys (:stage-artifacts result)))))
    (is (= :gravity/stage0-c2-reader-document-artifact
           (get-in result [:payload :kind])))
    (is (false? (get-in result [:artifact :authority :authoritative?])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:result-id result)))
    (is (= (:bytes-hash (get-in result [:artifact :source-identity]))
           (:source-content-hash result)))))

(deftest plan-binding-authenticates-cache-key-and-bounded-reference
  (pipeline/clear-cache!)
  (let [binding (bound-plan-binding)
        request {:source-path plan-source
                 :target-stage :source
                 :mode :fast
                 :plan-binding binding}
        first-result (pipeline/run-pipeline request)
        second-result (pipeline/run-pipeline request)
        reference (:artifact-reference second-result)]
    (is (= binding (:plan-binding first-result)))
    (is (= binding (:plan-binding reference)))
    (is (= #{:artifact :schema-version :pipeline :stage :mode :artifact-id
             :payload-artifact-id :payload-integrity-id :receipt-id
             :source-content-hash :provenance :plan-binding :cache-hit?
             :authority :reference-id}
           (set (keys reference))))
    (is (not (contains? reference :payload)))
    (is (= :source (:stage reference)))
    (is (true? (:cache-hit? reference)))
    (is (false? (get-in reference [:authority :authoritative?])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:reference-id reference)))
    (is (true? (get-in second-result [:artifact :receipt :cache :hit?])))
    (is (= (:cache-key-id (get-in first-result [:artifact :receipt]))
           (:cache-key-id (get-in second-result [:artifact :receipt]))))))

(deftest plan-binding-is-closed-and-bound-to-request
  (let [binding (bound-plan-binding)
        request {:source-path plan-source
                 :target-stage :source
                 :mode :fast
                 :plan-binding binding}]
    (is (= "ARTIFACT-PLAN-BINDING-KEYS"
           (try
             (pipeline/run-pipeline
              (assoc-in request [:plan-binding :extra] true))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))
    (is (= "ARTIFACT-PLAN-BINDING"
           (try
             (pipeline/run-pipeline
              (assoc-in request [:plan-binding :catalog-id] "sha256:bad"))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))
    (is (= "ARTIFACT-PLAN-BINDING-MISMATCH"
           (try
             (pipeline/run-pipeline
              (assoc-in request [:plan-binding :artifact-stage] :reader))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))
    (is (= "ARTIFACT-PLAN-BINDING-MISMATCH"
           (try
             (pipeline/run-pipeline
              (assoc-in request [:plan-binding :source-path] "examples/other.gravity"))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))
    (is (= "ARTIFACT-PLAN-BINDING-CLOSURE"
           (try
             (pipeline/run-pipeline
              (assoc-in request [:plan-binding :closure-paths]
                        ["examples/other.gravity"]))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))))

(deftest plan-binding-partitions-fast-reuse
  (pipeline/clear-cache!)
  (let [binding (bound-plan-binding)
        first-result
        (pipeline/run-pipeline {:source-path plan-source
                                 :target-stage :source
                                 :mode :fast
                                 :plan-binding binding})
        alternate-binding (assoc binding
                                  :route-id "sh07-alternate-route")
        second-result
        (pipeline/run-pipeline {:source-path plan-source
                                 :target-stage :source
                                 :mode :fast
                                 :plan-binding alternate-binding})]
    (is (= (:artifact-id (get-in first-result [:artifact]))
           (:artifact-id (get-in second-result [:artifact]))))
    (is (not= (:cache-key-id (get-in first-result [:artifact :receipt]))
              (:cache-key-id (get-in second-result [:artifact :receipt]))))
    (is (false? (get-in second-result [:artifact :receipt :cache :hit?])))
    (is (not= (:reference-id (:artifact-reference first-result))
              (:reference-id (:artifact-reference second-result))))))

(deftest contract-content-identity-partitions-cache-key
  (let [bound (bound-plan-binding)
        preimage (var-get (ns-resolve 'gravity.authenticated-artifact-pipeline
                                      'cache-key-preimage))
        canonical-id (var-get (ns-resolve 'gravity.authenticated-artifact-pipeline
                                          'canonical-id!))
        source-id {:project-root-id "sha256:root"
                   :project-relative-path plan-source
                   :byte-count 1
                   :bytes-hash "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                   :encoding :utf-8
                   :reader-policy :gravity/standard-reader}
        request {:target-stage :source :mode :fast :plan-binding bound}
        altered-binding (update bound :contract-binding
                                 assoc :cache-contract
                                 "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        first-id (canonical-id :source
                               (binding [pipeline/*contract-binding*
                                         (:contract-binding bound)]
                                 (preimage :source :fast source-id [] request)))
        second-id (canonical-id :source
                                (binding [pipeline/*contract-binding*
                                          (:contract-binding altered-binding)]
                                  (preimage :source :fast source-id []
                                            (assoc request :plan-binding altered-binding))))]
    (is (not= first-id second-id))
    (is (not= (get-in (binding [pipeline/*contract-binding*
                                (:contract-binding bound)]
                       (preimage :source :fast source-id [] request))
                     [:contract-binding :cache-contract])
           (get-in (binding [pipeline/*contract-binding*
                             (:contract-binding altered-binding)]
                    (preimage :source :fast source-id []
                              (assoc request :plan-binding altered-binding)))
                   [:contract-binding :cache-contract])))))

(deftest fast-reuse-is-receipted-and-fresh-bypasses-it
  (pipeline/clear-cache!)
  (let [request {:source-path fixture :target-stage :source :mode :fast}
        first-result (pipeline/run-pipeline request)
        second-result (pipeline/run-pipeline request)
        fresh-result (pipeline/run-pipeline (assoc request :mode :fresh))]
    (is (false? (get-in first-result [:artifact :receipt :cache :hit?])))
    (is (true? (get-in second-result [:artifact :receipt :cache :hit?])))
    (is (false? (get-in fresh-result [:artifact :receipt :cache :hit?])))
    (is (= (get-in first-result [:artifact :artifact-id])
           (get-in fresh-result [:artifact :artifact-id])))
    (is (= (:result-id first-result) (:result-id fresh-result)))
    (is (= 1 (:entry-count (pipeline/cache-stats))))
    (is (= :fresh (:mode fresh-result)))
    (is (false? (get-in fresh-result [:artifact :authority :authoritative?])))))

(deftest cached-payload-shape-mutation-is-rejected
  (pipeline/clear-cache!)
  (let [request {:source-path fixture :target-stage :source :mode :fast}
        first-result (pipeline/run-pipeline request)
        cache-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                              'stage-cache)
        cache (var-get cache-var)
        key (first (keys @cache))]
    ;; Retain the owning payload identity while changing a nested source fact.
    ;; The independent bounded witness must invalidate this entry.
    (swap! cache update key
           #(assoc-in % [:payload :source-identity :bytes-hash]
                      "sha256:tampered"))
    (let [second-result (pipeline/run-pipeline request)]
      (is (false? (get-in second-result [:artifact :receipt :cache :hit?])))
      (is (not= "sha256:tampered"
                (get-in second-result [:payload :source-identity :bytes-hash])))
      (is (= (:source-content-hash first-result)
             (:source-content-hash second-result))))))

(deftest cached-reader-nested-mutation-retaining-id-and-shape-is-rejected
  (pipeline/clear-cache!)
  (let [request {:source-path fixture :target-stage :reader :mode :fast}
        first-result (pipeline/run-pipeline request)
        cache-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                              'stage-cache)
        cache (var-get cache-var)
        [key cached-reader]
        (first (filter (fn [[_ envelope]] (= :reader (:stage envelope)))
                       @cache))]
    (is (= :reader (:stage cached-reader)))
    ;; Keep the reader payload's owning :artifact-id and every top-level shape
    ;; witness unchanged while altering a nested identity fact.  The bounded
    ;; owner-domain C2 identity recomputation must reject this cache entry.
    (swap! cache update key
           #(assoc-in % [:payload :source-unit-record :identity-inputs
                         :bytes-hash]
                      "sha256:tampered-reader-source"))
    (let [second-result (pipeline/run-pipeline request)]
      (is (false? (get-in second-result [:artifact :receipt :cache :hit?])))
      (is (not= "sha256:tampered-reader-source"
                (get-in second-result
                        [:payload :source-unit-record :identity-inputs
                         :bytes-hash])))
      (is (= (:source-content-hash first-result)
             (:source-content-hash second-result))))))

(deftest failed-final-snapshot-does-not-publish-staged-fast-entry
  (pipeline/clear-cache!)
  (let [stable-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                               'stable-source-after!)]
    (clojure.core/with-redefs-fn
      {stable-var (fn [_ _]
                    (throw (ex-info "synthetic final snapshot failure"
                                    {:id "ARTIFACT-MUTATED"})))}
      #(is (thrown? clojure.lang.ExceptionInfo
                   (pipeline/run-pipeline
                    {:source-path fixture
                     :target-stage :source
                     :mode :fast}))))
    ;; The source envelope was staged in the per-run pending map, so the
    ;; synthetic post-snapshot failure cannot publish it globally.
    (is (= 0 (:entry-count (pipeline/cache-stats))))))

(deftest fast-cache-is-deterministically-bounded
  (pipeline/clear-cache!)
  (let [cache-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                              'stage-cache)
        cache (var-get cache-var)
        entries
        (into {}
              (for [idx (range 40)]
                [[(str "sha256:cache-" idx) (str "/tmp/source-" idx)]
                 {:stage :source
                  :artifact-id (str "sha256:artifact-" idx)
                  :payload-artifact-id (str "sha256:payload-" idx)
                  :payload-integrity-id {:full-id
                                         (str "sha256:integrity-" idx)}
                  :cache-key-id (str "sha256:key-" idx)
                  :input-artifact-ids []
                  :pass {:preserves [:source-identity]
                         :invalidates [:none-before-source-read]}
                  :authentication {:parent-artifact-ids []}}]))]
    (reset! cache entries)
    (let [stats (pipeline/cache-stats)]
      (is (<= (:entry-count stats)
              (get-in stats [:limits :max-entries])))
      (is (<= (:node-count stats)
              (get-in stats [:limits :max-node-count])))
      (is (<= (:cost-bytes stats)
              (get-in stats [:limits :max-cost-bytes])))
      (is (pos? (:evictions stats))))))

(deftest repeated-distinct-sources-commit-within-bounds
  (pipeline/clear-cache!)
  (let [paths (mapv #(temp-source! (str "(ns bounded.source" % ")\n"))
                    (range 36))
        cache-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                              'stage-cache)
        cache (var-get cache-var)]
    (try
      (doseq [path paths]
        (pipeline/run-pipeline {:source-path (str path)
                                :target-stage :source
                                :mode :fast}))
      ;; Read the committed atom directly before cache-stats can perform any
      ;; defensive pruning.  Publication itself must already enforce bounds.
      (let [entry-count-before (count @cache)
            stats (pipeline/cache-stats)]
        (is (= entry-count-before (:entry-count stats)))
        (is (<= entry-count-before
                (get-in stats [:limits :max-entries])))
        (is (<= (:node-count stats)
                (get-in stats [:limits :max-node-count])))
        (is (<= (:cost-bytes stats)
                (get-in stats [:limits :max-cost-bytes]))))
      (finally
        (doseq [path paths]
          (Files/deleteIfExists path))))))

(deftest eight-megabyte-byte-array-is-rejected-before-cache-publication
  (let [measure-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                                'bounded-payload-measure)
        bound-var (ns-resolve 'gravity.authenticated-artifact-pipeline
                              'bounded-cache-map)
        payload {:nested {:blob (byte-array (* 8 1024 1024))}}
        measured (measure-var payload)
        bounded (bound-var
                 {["sha256:oversized" "/tmp/oversized"]
                  {:stage :source :payload payload}})]
    (is (= :over-limit (:status measured)))
    (is (> (:bytes measured)
           (* 2 1024 1024)))
    (is (empty? (:entries bounded)))))

(deftest source-mutation-changes-content-key-and-never-reuses-old-result
  (pipeline/clear-cache!)
  (let [path (temp-source! "(ns mutation.test)\n")
        path-text (str path)]
    (try
      (let [request {:source-path path-text :target-stage :source :mode :fast}
            before (pipeline/run-pipeline request)]
        (Files/write path (.getBytes "(ns mutation.changed)\n"
                                     StandardCharsets/UTF_8)
                     (into-array OpenOption
                                 [StandardOpenOption/TRUNCATE_EXISTING]))
        (let [after (pipeline/run-pipeline request)]
          (is (false? (get-in after [:artifact :receipt :cache :hit?])))
          (is (not= (:source-content-hash before)
                    (:source-content-hash after)))
          (is (not= (:result-id before) (:result-id after)))))
      (finally
        (Files/deleteIfExists path)))))

(deftest source-identity-is-path-neutral-but-provenance-is-not
  (pipeline/clear-cache!)
  (let [left (temp-project! "(ns same.identity)\n")
        right (temp-project! "(ns same.identity)\n")]
    (try
      (let [left-result (pipeline/run-pipeline
                         {:source-path (str (:source left))
                          :target-stage :source :mode :fresh})
            right-result (pipeline/run-pipeline
                          {:source-path (str (:source right))
                           :target-stage :source :mode :fresh})
            left-source (get-in left-result [:artifact :source-identity])
            right-source (get-in right-result [:artifact :source-identity])]
        (is (= left-source right-source))
        (is (= (get-in left-result [:artifact :artifact-id])
               (get-in right-result [:artifact :artifact-id])))
        (is (not= (get-in left-result [:artifact :provenance :actual-path])
                  (get-in right-result [:artifact :provenance :actual-path]))))
      (finally
        (delete-project! left)
        (delete-project! right)))))

(deftest request-schema-and-parent-boundary-fail-closed
  (testing "closed request schema"
    (is (thrown? clojure.lang.ExceptionInfo
                 (pipeline/run-pipeline
                  {:source-path fixture :mode :fast :unknown true})))
    (is (thrown? clojure.lang.ExceptionInfo
                 (pipeline/run-pipeline
                  {:source-path fixture :target-stage :not-a-stage
                   :mode :fresh}))))
  (testing "stage helper does not expose an unrequested suffix"
    (let [result (pipeline/run-pipeline
                  {:source-path fixture :target-stage :source :mode :fresh})]
      (is (thrown? clojure.lang.ExceptionInfo
                   (pipeline/stage-artifact result :reader))))))

(deftest stage-parent-chain-uses-bootstrap-identity-domain
  ;; Keep this test bounded by replacing only the delegated stage calls.  The
  ;; real source snapshot/authentication still executes, while every edge in
  ;; the six-stage graph is checked against the payload artifact id (rather
  ;; than this facade's envelope id).
  (pipeline/clear-cache!)
  (let [ids {:reader "sha256:1111111111111111111111111111111111111111111111111111111111111111"
             :syntax "sha256:2222222222222222222222222222222222222222222222222222222222222222"
             :macro "sha256:3333333333333333333333333333333333333333333333333333333333333333"
             :resolution "sha256:4444444444444444444444444444444444444444444444444444444444444444"
             :checked "sha256:5555555555555555555555555555555555555555555555555555555555555555"}
        calls (atom [])]
    (with-redefs [pipeline/bootstrap-call
                  (fn [symbol & args]
                    (swap! calls conj symbol)
                    (cond
                      (= symbol 'reader-project-context-for-source)
                      {:project-root-id "sha256:root"
                       :project-root-path "."
                       :project-relative-path "examples/hello.gravity"}
                      (= symbol 'c2-reader-artifact-id)
                      (:reader ids)
                      (= symbol 'c3-artifact-id)
                      (:syntax ids)
                      (= symbol 'sh05-macro-artifact-id)
                      (:macro ids)
                      (= symbol 'sh06-resolution-artifact-id)
                      (:resolution ids)
                      (= symbol 'sh07-core-artifact-identity-input)
                      nil
                      (= symbol 'reader-canonical-hash)
                      (:checked ids)
                      (= symbol 'compiler-c2-reader-source-artifact)
                      (let [[source-path source-text context] args
                            bytes-hash (str "sha256:" (digest/sha256-hex
                                                         source-text))
                            source-unit
                            {:artifact :gravity/source-unit
                             :source-id "sha256:fake-source-unit"
                             :path source-path
                             :bytes-hash bytes-hash
                             :project-relative-path
                             (:project-relative-path context)
                             :project-root (:project-root-id context)
                             :project-root-record
                             {:path (:project-root-path context)
                              :project-root-id (:project-root-id context)}
                             :identity-inputs
                             {:bytes-hash bytes-hash
                              :project-relative-path
                              (:project-relative-path context)
                              :project-root-id (:project-root-id context)}}]
                        {:kind :reader :artifact-id (:reader ids)
                         :source-unit-record source-unit})
                      (= symbol 'compiler-c3-syntax-source-artifact)
                      {:kind :syntax :artifact-id (:syntax ids)
                       :c2-reader-artifact {:artifact-id (:reader ids)}}
                      (= symbol 'sh05-macro-artifact-from-c3-artifact)
                      {:kind :macro :artifact-id (:macro ids)
                       :sh04-syntax-artifact {:artifact-id (:syntax ids)}}
                      (= symbol 'sh06-resolution-artifact-from-sh05-artifact)
                      {:kind :resolution :artifact-id (:resolution ids)
                       :sh05-macro-artifact {:artifact-id (:macro ids)}}
                      (= symbol 'sh07-core-from-resolution-artifact)
                      {:kind :checked-core :artifact-id (:checked ids)
                       :sh06-resolution-artifact {:artifact-id (:resolution ids)}}
                      :else
                      (throw (ex-info "unexpected stage call" {:symbol symbol}))))]
      (let [result (pipeline/run-pipeline
                    {:source-path fixture
                     :target-stage :checked-core
                     :mode :fresh})
            stages (keys (:stage-artifacts result))]
        (is (= [:source :reader :syntax :macroexpansion :resolution
                :checked-core]
               (vec stages)))
        (is (= (get-in result [:stage-artifacts :source :payload
                               :source-identity-id])
               (get-in result [:stage-artifacts :reader
                               :input-artifact-ids 0])))
        (is (= (:reader ids)
               (get-in result [:stage-artifacts :syntax :input-artifact-ids 1])))
        (is (= (:syntax ids)
               (get-in result [:stage-artifacts :macroexpansion
                               :input-artifact-ids 2])))
        (is (= (:macro ids)
               (get-in result [:stage-artifacts :resolution
                               :input-artifact-ids 3])))
        (is (= (:resolution ids)
               (get-in result [:stage-artifacts :checked-core
                               :input-artifact-ids 4])))
        (is (some #{'compiler-c3-syntax-source-artifact} @calls))))))
