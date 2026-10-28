(ns gravity.resolve.selected-source-harness-test
  "Cheap injected controls only. No Bootstrap, source constructor or broker."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.resolve.selected-source-harness :as h]))

(defn id [s] (str "sha256:" (h/sha (.getBytes (pr-str s) "UTF-8"))))
(defn reason [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))

(defn source-case []
  (let [text "a\r\n\u03bb" source "/fixture.gravity" semantic (id :source)
        span {:source source :file semantic :byte-start 3 :byte-end 5
              :scalar-start 3 :scalar-end 4 :line-start 2 :column-start 1 :line-end 2 :column-end 2}
        seed {:form-id :form :span span}
        form {:form-id :form :span span :open-token :open :close-token :close}
        node {:source {:source-id semantic :form-id :form :token-range [:open :close]}
              :span {:primary span} :form {:kind :symbol :raw "\u03bb"}
              :origin [{:kind :source :span span :producer {:name 'gravity.bootstrap.reader}}]
              :facts [{:fact-kind :authenticated-reader-products :value {}}]}
        c3 {:c2-reader-artifact {:source-unit-record {:path source}
                                :syntax-seed-stream [seed] :form-tree [form]}
            :syntax-object-stream [node]}]
    {:text text :source source :semantic semantic :c3 c3}))

(deftest coordinates-join-actual-inputs-and-reject-substitutions
  (let [{:keys [text source semantic c3]} (source-case)
        owner (fn [name] (is (= 'sh04-syntax-semantic-source-id name)) (fn [& _] semantic))]
    (is (= :checked (h/check-source-coordinates! owner c3 source text)))
    (doseq [[label changed]
            [[:physical-path (assoc-in c3 [:syntax-object-stream 0 :span :primary :source] "/other.gravity")]
             [:semantic-file (assoc-in c3 [:syntax-object-stream 0 :span :primary :file] (id :other))]
             [:form-id (assoc-in c3 [:syntax-object-stream 0 :source :form-id] :other)]
             [:tokens (assoc-in c3 [:syntax-object-stream 0 :source :token-range] [:other :close])]
             [:column (assoc-in c3 [:syntax-object-stream 0 :span :primary :column-end] 99)]
             [:scalar (assoc-in c3 [:syntax-object-stream 0 :span :primary :scalar-end] 99)]
             [:raw (assoc-in c3 [:syntax-object-stream 0 :form :raw] "other")]
             [:origin (assoc-in c3 [:syntax-object-stream 0 :origin 0 :span :byte-end] 99)]
             [:reader (assoc-in c3 [:syntax-object-stream 0 :origin 0 :producer :name] 'other)]
             [:form-span (assoc-in c3 [:c2-reader-artifact :form-tree 0 :span :byte-end] 4)]
             [:form-line (assoc-in c3 [:c2-reader-artifact :form-tree 0 :span :line-start] 99)]
             [:form-column (assoc-in c3 [:c2-reader-artifact :form-tree 0 :span :column-end] 99)]
             [:form-scalar (assoc-in c3 [:c2-reader-artifact :form-tree 0 :span :scalar-start] 99)]
             [:seed-line (assoc-in c3 [:c2-reader-artifact :syntax-seed-stream 0 :span :line-end] 99)]
             [:seed-column (assoc-in c3 [:c2-reader-artifact :syntax-seed-stream 0 :span :column-start] 99)]
             [:missing-coordinate (update-in c3 [:c2-reader-artifact :form-tree 0 :span] dissoc :line-start)]
             [:extra-origin (update-in c3 [:syntax-object-stream 0 :origin] conj {:kind :generated})]
             [:duplicate-origin (update-in c3 [:syntax-object-stream 0 :origin] #(conj % (first %)))]
             [:reordered-origin (update-in c3 [:syntax-object-stream 0 :origin] #(into [{:kind :generated}] %))]
             [:seed (assoc-in c3 [:c2-reader-artifact :syntax-seed-stream 0 :span :byte-end] 4)]
             [:actual-path (assoc-in c3 [:c2-reader-artifact :source-unit-record :path] "/other.gravity")]
             [:missing-seed (assoc-in c3 [:c2-reader-artifact :syntax-seed-stream] [])]]]
      (testing (name label)
        (is (not= c3 changed))
        (is (some? (reason #(h/check-source-coordinates! owner changed source text))))))
    (is (thrown? java.nio.charset.CharacterCodingException
                 (h/coordinate-at (.getBytes text "UTF-8") 4)))
    (is (= :byte-coordinate (reason #(h/coordinate-at (.getBytes text "UTF-8") -1))))))

(defn history-case [present?]
  (let [origins (if present? [{:reader-abbreviation :quote :expanded-form '(quote x)}] [])
        history {:roots [{:form-id :form :origins origins}] :id (when present? (id :history))}
        c3 (-> (:c3 (source-case))
               (assoc-in [:gravity-syntax-boundary :authenticated-envelope-descriptor :semantic-projections]
                         [{:name :syntax-product-identities
                           :value (if present? {:reader-expansion-history-id (:id history)} {})}]))]
    {:history history
     :c3 (if present? (assoc-in c3 [:syntax-object-stream 0 :facts 0 :value :reader-expansion-origins] origins) c3)}))

(deftest history-presence-value-and-source-facts-are-separate-checks
  (doseq [present? [true false]]
    (let [{:keys [history c3]} (history-case present?)
          owner (fn [name] (is (= 'sh04-syntax-reader-expansion-history name)) (fn [& _] history))
          summary [:gravity-syntax-boundary :authenticated-envelope-descriptor :semantic-projections 0 :value]
          forged (assoc-in c3 (conj summary :reader-expansion-history-id) (id :forged))]
      (is (= history (h/check-expansion-history! owner c3)))
      (is (some? (reason #(h/check-expansion-history! owner forged))))
      (when present?
        (doseq [changed [(update-in c3 summary dissoc :reader-expansion-history-id)
                         (update-in c3 [:syntax-object-stream 0 :facts 0 :value] dissoc :reader-expansion-origins)
                         (assoc-in c3 [:syntax-object-stream 0 :facts 0 :value :reader-expansion-origins] [:forged])
                         (update-in c3 [:syntax-object-stream 0 :facts] #(conj % (first %)))]]
          (is (not= c3 changed))
          (is (some? (reason #(h/check-expansion-history! owner changed)))))))))

(defn diagnostic-kernel []
  ;; Evaluate ONLY the existing pure origin-recovery defn in an isolated fixture
  ;; namespace. Hash/source-revision collaborators are injected premises. This
  ;; tests the real leaf's rejection logic, not parent/request authentication.
  (let [path "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00880-sh06-resolution-artifact-verification-03.clj"
        form (with-open [r (java.io.PushbackReader. (io/reader path))]
               (binding [*read-eval* false] (read r)))
        space (create-ns (gensym "diagnostic-fixture-"))]
    (h/ensure! (and (= 'defn- (first form)) (= 'sh06-resolution-diagnostic-origin (second form))) :exact-kernel-form)
    (binding [*ns* space]
      (clojure.core/refer 'clojure.core)
      (intern space 'reader-canonical-hash id)
      (intern space 'sh06-resolution-source-revision-id (fn [a] (:fixture-revision a)))
      (eval form))
    {:space (ns-name space) :operation (ns-resolve space 'sh06-resolution-diagnostic-origin)}))

(defn diagnostic-case []
  (let [source "/generated.gravity" span {:byte-start 1 :byte-end 4}
        origin [{:kind :source :span span} {:kind :generated :producer {:name 'fixture/macro}}]
        node {:syntax/id (id :syntax) :span span :origin origin}
        ref {:syntax-id (id {:domain :gravity/sh06-reference-syntax-v1 :owner-syntax-id (:syntax/id node)
                            :ordinal 1 :symbol 'missing :position :expression})
             :symbol 'missing :name 'missing :qualifier nil :position :expression :scope-chain []
             :source-span span :semantic-span (assoc span :ordinal 1)}
        parent {:artifact-id (id :parent) :expanded-syntax-stream-id (id :stream)
                :expanded-syntax-stream [node] :fixture-revision (id :revision)}
        binding {:c4-artifact-id (:artifact-id parent) :source-revision-id (:fixture-revision parent)}
        request {:module (merge binding {:namespace 'fixture :profile :meta :target :jvm})
                 :references [ref] :provenance {:actual-source-path source}
                 :macro-expansion-binding (assoc binding :macro-result-id (:expanded-syntax-stream-id parent))}
        details (merge (select-keys ref [:name :qualifier :position :scope-chain])
                       {:reason :no-legal-binding :candidate-count 0})
        problem {:id "C5-UNRESOLVED" :diagnostic-family :c5-name-resolution :stage :name-resolution
                 :namespace 'fixture :active-profile :meta :target :jvm :symbol 'missing
                 :syntax-id (:syntax-id ref) :source-span (assoc span :source source) :candidate-bindings []
                 :severity :error :remediation "retain this" :diagnostic-id "retain this too"
                 :facts (merge binding {:reason :no-legal-binding :rule-specific details :semantic-span (:semantic-span ref)})}]
    {:source source :sh05 parent :request request :diagnostic problem :expected-origin origin}))

(deftest exact-causal-leaf-rejects-altered-facts-and-unrelated-errors
  (let [{:keys [space operation]} (diagnostic-kernel)
        input (diagnostic-case)
        owner (fn [name] (is (= 'sh06-resolution-diagnostic-origin name)) operation)]
    (try
      (is (= :checked (:status (h/check-diagnostic-controls! owner input))))
      (let [unrelated {:syntax/id (id :unrelated) :span {:byte-start 9 :byte-end 12} :origin []}
            changed (update-in input [:sh05 :expanded-syntax-stream] conj unrelated)]
        (is (= :checked (:status (h/check-diagnostic-controls! owner changed)))))
      (doseq [[label changed]
              [[:reference (assoc-in input [:request :references 0 :symbol] 'changed)]
               [:physical (assoc-in input [:request :provenance :actual-source-path] "/other.gravity")]
               [:parent (assoc-in input [:request :module :c4-artifact-id] (id :changed))]
               [:revision (assoc-in input [:sh05 :fixture-revision] (id :changed))]
               [:span (assoc-in input [:diagnostic :source-span :byte-end] 8)]
               [:expected-origin (assoc input :expected-origin [:wrong])]]]
        (testing (name label)
          (is (not= input changed))
          (is (= :causal-origin (reason #(h/check-diagnostic-controls! owner changed))))))
      (finally (remove-ns space)))))

(deftest transported-diagnostic-preserves-original-fields-message-and-cause
  (let [{:keys [diagnostic expected-origin]} (diagnostic-case)
        original (ex-info "original" diagnostic)
        carried (ex-info "original" (assoc diagnostic :origin-chain expected-origin) original)]
    (is (= :checked (h/check-transported-diagnostic! original carried expected-origin)))
    (is (= :diagnostic-conservation
           (reason #(h/check-transported-diagnostic! original
                      (ex-info "original" (assoc diagnostic :origin-chain []) original) []))))
    (doseq [changed [(ex-info "changed" (ex-data carried) original)
                     (ex-info "original" (assoc (ex-data carried) :remediation "changed") original)
                     (ex-info "original" (ex-data carried))
                     (ex-info "original" (assoc diagnostic :origin-chain [:wrong]) original)]]
      (is (= :diagnostic-conservation (reason #(h/check-transported-diagnostic! original changed expected-origin)))))))

(deftest mutation-and-negative-oracles-fail-closed
  (let [descriptor {:semantic-projections [{:name :syntax-implementation-binding
                                          :value {:admission-status :development-unadmitted :binding-id (id :binding)
                                                  :compiler-identity {:source-content-hash (id :compiler)}
                                                  :emitter-source-rule-hash (id :emitter)
                                                  :public-function-hashes {'c3-syntax-stream-verify-resolved (id :verify)}
                                                  :public-DATA {'c3-syntax-object-contract {:reference-order :prior-only}}}}]}]
    (doseq [field h/semantic-fields]
      (is (not= descriptor (h/semantic-mutation descriptor field))))
    (is (= :unknown-semantic-field (reason #(h/semantic-mutation descriptor :unknown))))
    (is (= :projection-cardinality (reason #(h/projection-index (update descriptor :semantic-projections into (:semantic-projections descriptor))
                                                              :syntax-implementation-binding)))))
  (is (= :mutation-noop (reason #(h/replace-existing {:x 1} [:x] 1))))
  (is (= :missing-mutation-field (reason #(h/replace-existing {} [:x] 1))))
  (is (not (h/c3-rejected? {:status :failed})))
  (is (not (h/sh05-rejected? {:status :failed})))
  (is (not (h/c5-rejected? {:status :failed})))
  (is (not (h/negative-passed? :invocation-mismatch {:id "C5-UNRESOLVED"})))
  (is (h/negative-passed? :invocation-mismatch {:id "C5-UNRESOLVED" :missing-fields [:selected-current-reader-invocation]
                                              :facts {:reason :reader-invocation-source-mismatch}})))

(deftest explicit-inputs-read-current-files-and-detect-drift
  (let [data (h/file-input! (.getCanonicalPath (io/file h/fixture-relative)))
        input {:inputs {:fixture data}}]
    (is (= 192 (:bytes data)))
    (is (= h/fixture-sha (:sha256 data)))
    (is (nil? (h/recheck-inputs! input)))
    (is (= :input-drift (reason #(h/recheck-inputs! (assoc-in input [:inputs :fixture :sha256] "changed")))))
    (is (= :regular-absolute-input (reason #(h/file-input! h/fixture-relative)))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest preparation-is-root-explicit-and-checks-Git-bookends
  ;; Inject only the harness's file/Git readers. No owner is replaced or called.
  (doseq [root ["/portable-one" "/portable-two"]]
    (let [reads (atom [])
          identity {:head "exact-head" :tree "exact-tree"}
          git (fn [actual-root & args]
                (is (= root actual-root))
                (case (vec args)
                  ["rev-parse" "HEAD"] (:head identity)
                  ["rev-parse" "HEAD^{tree}"] (:tree identity)
                  ["status" "--porcelain" "--untracked-files=all"] ""))
          file (fn [path]
                 (swap! reads conj path)
                 {:path path :sha256 h/fixture-sha :bytes 192 :text "injected fixture"})]
      (with-redefs [h/git! git h/file-input! file]
        (let [prepared (h/prepare-inputs! root identity)]
          (is (= (mapv #(str (io/file root %)) [h/fixture-relative h/reader-relative h/syntax-relative]) @reads))
          (is (= (str (io/file root h/reader-relative)) (:reader-source-path prepared)))
          (is (= (str (io/file root h/syntax-relative))
                 (get-in prepared [:invocation :syntax-source-selection :source-path])))
          (is (= identity (:source prepared)))
          (is (= :unrun (:execution prepared)))
          (is (= :not-established (:admission prepared)))
          (is (nil? (h/recheck-inputs! prepared)))
          (is (= :source-head-drift
                 (reason #(h/recheck-inputs! (assoc-in prepared [:source :head] "changed")))))))))
  (with-redefs [h/git! (fn [& _] "dirty")
                h/file-input! (fn [& _] (throw (ex-info "unexpected file read" {:reason :unexpected-read}))) ]
    (is (= :source-head (reason #(h/prepare-inputs! "/portable" {:head "head" :tree "tree"}))))
    (is (= :exact-source-tuple (reason #(h/prepare-inputs! "/portable" {:head "head"}))))))
