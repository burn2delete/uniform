(ns gravity.self-hosting.authenticated-artifact-pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.authenticated-artifact-pipeline :as pipeline]
            [gravity.digest :as digest]
            [gravity.tooling.strict-json :as json])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption StandardOpenOption]))

(def ^:private fixture "examples/hello.gravity")

;; Exact selectors below construct real products only when explicitly run.
;; Bind the companion .qst path for its separate source/provenance phase.
(def ^:dynamic *owner-population-source*
  "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.gravity")

(defn- pipeline-function [name]
  (or (ns-resolve 'gravity.authenticated-artifact-pipeline name)
      (throw (ex-info "Missing facade implementation" {:name name}))))

(defn- owner-call [name & args]
  (apply (pipeline-function 'bootstrap-call) name args))

(def ^:private owner-parent-fields
  {:syntax :c2-reader-artifact :macroexpansion :sh04-syntax-artifact
   :resolution :sh05-macro-artifact :checked-core :sh06-resolution-artifact})

(defn- owner-child! [stage {:keys [path text context invocation module-inputs]} previous]
  (let [parent (:payload (peek previous))
        reader (:reader-source-path invocation)]
    (case stage
      :reader (owner-call 'compiler-c2-reader-source-artifact
                                       path text context nil nil reader invocation)
      :syntax (owner-call 'compiler-c3-syntax-source-artifact
                                       path text context parent
                                       (var-get (ns-resolve 'gravity.bootstrap 'sh03-reader-internal-product-authority))
                                       nil invocation)
      :macroexpansion (owner-call 'sh05-macro-artifact-from-c3-artifact path parent invocation)
      :resolution (owner-call 'sh06-resolution-artifact-from-sh05-artifact
                                           path parent module-inputs reader invocation)
      :checked-core (owner-call 'sh07-core-from-resolution-artifact parent invocation))))

(defn- owner-prefix! [source-path]
  (let [source (pipeline/stage-artifact
                 (pipeline/run-pipeline {:source-path source-path :target-stage :source :mode :fresh}) :source)
        path (get-in source [:provenance :actual-path])
        inputs {:path path :text (slurp path :encoding "UTF-8")
                :context (owner-call 'reader-project-context-for-source path)
                :invocation {:reader-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
                             :authenticated-envelope-options {:source :development-current}}
                :module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}}
        request {:source-path path :target-stage :checked-core :mode :fresh}
        prefix (reduce (fn [previous stage]
                         (let [parents ((pipeline-function 'parent-ids) stage previous)
                               payload (owner-child! stage inputs previous)
                               key ((pipeline-function 'canonical-id!) stage
                                    ((pipeline-function 'cache-key-preimage) stage :fresh
                                     (:source-identity source) parents request))]
                           (conj previous ((pipeline-function 'envelope) stage :fresh
                                           (:source-identity source) (:provenance source)
                                           parents payload false key))))
                       [source] (rest pipeline/stages))]
    {:inputs inputs :prefix prefix}))

(defn- selected-owner-prefix! []
  (assert (contains? #{"bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.gravity"
                       "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"}
                     *owner-population-source*))
  (owner-prefix! *owner-population-source*))

(defn- parent-mutation-controls! [mutation]
  (let [{:keys [prefix]} (selected-owner-prefix!)]
    (doseq [[index child] (map-indexed vector prefix)
            :let [stage (:stage child)] :when (contains? owner-parent-fields stage)]
      (let [parents ((pipeline-function 'parent-ids) stage (subvec prefix 0 index))
            field (owner-parent-fields stage)
            payload (:payload child)
            wrong (case mutation
                    :missing (dissoc payload field)
                    :wrong-stage (assoc payload field (:payload (peek prefix)))
                    :facade-domain (assoc-in payload [field :artifact-id] (:artifact-id (nth prefix (dec index)))))
            error (try ((pipeline-function 'authenticate-embedded-parent!) stage wrong parents)
                       nil (catch clojure.lang.ExceptionInfo e e))]
        (is (= "ARTIFACT-PARENT" (:id (ex-data error))))
        (is (= stage (:stage (ex-data error))))
        (is (= (last parents) (:expected-parent-id (ex-data error))))
        (is (not= (last parents) (:observed-parent-id (ex-data error))))))))

(defn- owner-prefix-replay! []
  (let [{:keys [inputs prefix]} (selected-owner-prefix!)
        {:keys [path text invocation module-inputs]} inputs
        [_ reader syntax macro resolution core] (mapv :payload prefix)]
    (is (= pipeline/stages (mapv :stage prefix)))
    (is (= text (owner-call 'c2-reader-artifact-source-text path reader)))
    (is (= path (get-in reader [:source-unit-record :path])))
    (let [view (owner-call 'c3-reader-artifact-view reader invocation)
          revision (get-in reader [:gravity-reader-boundary :authenticated-envelope-descriptor :source-revision])
          binding (:binding (owner-call 'sh03-reader-verification-invocation! path invocation))
          identity-keys [:source-content-hash :source-byte-count :plan-semantic-hash :functions-semantic-hash]]
      (is (= view (:c2-reader-artifact syntax)))
      (is (map? revision))
      (is (= revision (get-in view [:sh03-reader-authentication :reader-source-revision])))
      (is (= (select-keys revision identity-keys) (select-keys binding identity-keys))))
    (is (= syntax (get-in macro [:gravity-macro-boundary :authenticated-sh04-artifact])))
    (is (= macro (:sh05-macro-artifact resolution)))
    (is (= resolution (:sh06-resolution-artifact core)))
    (is (= module-inputs (get-in resolution [:gravity-resolution-boundary :module-inputs])))
    (is (= path (get-in core [:provenance :source-path])))
    (let [after (pipeline/stage-artifact
                  (pipeline/run-pipeline {:source-path path :target-stage :source :mode :fresh}) :source)]
      (is (= (:source-identity (first prefix)) (:source-identity after)))
      (is (= (:provenance (first prefix)) (:provenance after))))
    (doseq [[index child] (map-indexed vector prefix) :when (pos? index)]
      (let [stage (:stage child) parents ((pipeline-function 'parent-ids) stage (subvec prefix 0 index))]
        (is (= parents (:input-artifact-ids child)))
        (is (= (:provenance (first prefix)) (:provenance child)))
        (is (= (:source-identity (first prefix)) (:source-identity child)))
        (if (= :reader stage)
          ((pipeline-function 'authenticate-reader-source!) path (:source-identity child) (:payload child))
          ((pipeline-function 'authenticate-embedded-parent!) stage (:payload child) parents))))
    (owner-call 'c2-reader-validate! path reader)
    (owner-call 'c3-syntax-validate! path syntax invocation)
    (doseq [report [(owner-call 'sh05-macro-artifact-verification macro invocation)
                    (owner-call 'sh06-resolution-artifact-verification resolution :gravity.bootstrap/legacy invocation)
                    (owner-call 'sh07-core-artifact-verification core invocation)]]
      (is (= :passed (:status report)))
      (is (empty? (:failed-checks report))))))

(defn- foreign-owner-cache-control! []
  (let [{:keys [inputs prefix]} (selected-owner-prefix!)
        foreign (owner-prefix! (if (.endsWith ^String *owner-population-source* ".qst")
                                 "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-reader-quote.qst"
                                 "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-reader-quote.gravity"))
        source (first prefix)
        cache-var (pipeline-function 'stage-cache)
        build-var (pipeline-function 'build-payload)
        replay-var (pipeline-function 'revalidate-payload?)
        original-replay (var-get replay-var)
        original-cache (var-get cache-var)
        original-build (var-get build-var)]
    (doseq [[index child] (map-indexed vector prefix)
            :let [stage (:stage child)] :when (contains? owner-parent-fields stage)]
      (let [previous (subvec prefix 0 index)
            parents ((pipeline-function 'parent-ids) stage previous)
            foreign-payload (:payload (nth (:prefix foreign) index))
            request {:source-path (:path inputs) :target-stage stage :mode :fast}
            preimage ((pipeline-function 'cache-key-preimage) stage :fast (:source-identity source) parents request)
            key-id ((pipeline-function 'canonical-id!) stage preimage)
            key [key-id (:path inputs)]
            cached ((pipeline-function 'envelope) stage :fast (:source-identity source) (:provenance source)
                    parents foreign-payload false key-id)
            cache (atom {key cached})
            events (atom [])]
        (is (not= (last parents) ((pipeline-function 'embedded-parent-id) stage foreign-payload)))
        (is ((pipeline-function 'envelope-valid?) stage :fast key-id preimage cached
             (:source-identity source) (:provenance source) parents))
        ;; Only invocation forwarding is adapted here: every rebuild delegates
        ;; to the real owner constructor. No verifier or report is substituted.
        ;; This does not establish public facade current-Reader propagation.
        (try
          (clojure.core/with-redefs-fn
          {cache-var cache
           build-var (fn [s _ _ _ actual-previous]
                       (swap! events conj :rebuild)
                       (is (not (contains? @cache key)))
                       (owner-child! s inputs actual-previous))
           replay-var (fn [& args] (swap! events conj :owner-replay) (apply original-replay args))}
          (fn []
            (let [rebuilt ((pipeline-function 'build-stage!) stage :fast (:source-identity source)
                           (:provenance source) (:path inputs) (:text inputs) (:context inputs) previous request)]
              (is (= [:rebuild] @events))
              (is (false? (:cache-hit? rebuilt)))
              (is (= (last parents) ((pipeline-function 'embedded-parent-id) stage (:payload rebuilt))))
              (is (not= cached (get @cache key))))))
          (finally
            (is (identical? original-cache (var-get cache-var)))
            (is (identical? original-build (var-get build-var)))
            (is (identical? original-replay (var-get replay-var)))))))))

(deftest real-owner-gravity-prefix-passes-fresh-replay (owner-prefix-replay!))
(deftest real-owner-gravity-missing-parent-is-rejected (parent-mutation-controls! :missing))
(deftest real-owner-gravity-wrong-stage-parent-is-rejected (parent-mutation-controls! :wrong-stage))
(deftest real-owner-gravity-facade-domain-parent-is-rejected (parent-mutation-controls! :facade-domain))
(deftest real-owner-gravity-foreign-parent-cache-is-rebuilt (foreign-owner-cache-control!))

(deftest real-owner-qst-prefix-passes-fresh-replay
  (binding [*owner-population-source* "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"]
    (owner-prefix-replay!)))
(deftest real-owner-qst-missing-parent-is-rejected
  (binding [*owner-population-source* "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"]
    (parent-mutation-controls! :missing)))
(deftest real-owner-qst-wrong-stage-parent-is-rejected
  (binding [*owner-population-source* "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"]
    (parent-mutation-controls! :wrong-stage)))
(deftest real-owner-qst-facade-domain-parent-is-rejected
  (binding [*owner-population-source* "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"]
    (parent-mutation-controls! :facade-domain)))
(deftest real-owner-qst-foreign-parent-cache-is-rebuilt
  (binding [*owner-population-source* "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys.qst"]
    (foreign-owner-cache-control!)))

(deftest governing-contracts-resolve-to-the-canonical-inventory
  (let [contracts (var-get (ns-resolve 'gravity.authenticated-artifact-pipeline
                                       'contract-binding))
        inventory (json/load-json
                   (.toPath (io/file "docs/document-inventory.json")))]
    (doseq [[key id] [[:pipeline-contract "C1"] [:artifact-contract "PKG3"]]]
      (let [path (get contracts key)
            entry (first (filter #(= id (get % "id")) inventory))]
        (is (.isFile (io/file path)) path)
        (is (= path (get entry "path")) path)))))

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
