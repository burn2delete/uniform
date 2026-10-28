(ns gravity.resolve.selected-source-harness
  "Prepared selected-source consumer checks. Loading this namespace runs no owner.
  Injected controls test the harness, never compiler authentication or admission."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def fixture-relative "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.gravity")
(def fixture-sha "f1cb7bf7cb3011d608da53513ff7fcccae206f17bf3d6061bc44b99f1995b670")
(def reader-relative "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
(def syntax-relative "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")
(def semantic-fields [:admission-status :binding-id :compiler-identity
                      :emitter-source-rule-hash :public-function-hashes :public-DATA])

(defn ensure! [condition reason]
  (when-not condition (throw (ex-info "Selected source harness check failed" {:reason reason}))))

(defn sha [data]
  (format "%064x" (java.math.BigInteger. 1
                   (.digest (java.security.MessageDigest/getInstance "SHA-256") data))))

(defn strict-text [data]
  (str (.decode (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                  (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                  (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
                (java.nio.ByteBuffer/wrap data))))

(defn file-input! [path]
  (let [file (io/file path) p (.toPath file)]
    (ensure! (and (.isAbsolute file)
                  (not (java.nio.file.Files/isSymbolicLink p))
                  (java.nio.file.Files/isRegularFile p
                    (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS])))
             :regular-absolute-input)
    (let [data (java.nio.file.Files/readAllBytes p)]
      {:path (.getCanonicalPath file) :sha256 (sha data) :bytes (alength data)
       :text (strict-text data)})))

(defn git! [root & args]
  (let [result (apply shell/sh "git" "-C" root args)]
    (ensure! (zero? (:exit result)) :git-identity-read)
    (str/trim (:out result))))

(defn prepare-inputs!
  "File/Git reads only. Caller supplies an exact source tuple; no runtime admission."
  [root {:keys [head tree] :as expected}]
  (ensure! (.isAbsolute (io/file root)) :absolute-root)
  (ensure! (= #{:head :tree} (set (keys expected))) :exact-source-tuple)
  (ensure! (= head (git! root "rev-parse" "HEAD")) :source-head)
  (ensure! (= tree (git! root "rev-parse" "HEAD^{tree}")) :source-tree)
  (ensure! (empty? (git! root "status" "--porcelain" "--untracked-files=all")) :dirty-source)
  (let [source (file-input! (str (io/file root fixture-relative)))
        reader (file-input! (str (io/file root reader-relative)))
        syntax (file-input! (str (io/file root syntax-relative)))
        reader-path (:path reader)
        invocation {:reader-source-path reader-path
                    :authenticated-envelope-options {:source :development-current}
                    :syntax-source-selection {:schema :gravity/sh04-development-syntax-selection-v1
                                              :source-path (:path syntax)}}]
    (ensure! (and (= 192 (:bytes source)) (= fixture-sha (:sha256 source))) :fixture-identity)
    (ensure! (= head (git! root "rev-parse" "HEAD")) :source-head-after-reads)
    (ensure! (= tree (git! root "rev-parse" "HEAD^{tree}")) :source-tree-after-reads)
    (ensure! (empty? (git! root "status" "--porcelain" "--untracked-files=all")) :dirty-source-after-reads)
    {:root (.getCanonicalPath (io/file root))
     :source expected :inputs {:fixture source :reader reader :syntax syntax}
     :source-path (:path source) :source-text (:text source)
     :reader-source-path reader-path :invocation invocation
     :module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
     :execution :unrun :admission :not-established}))

(defn recheck-inputs! [{:keys [root source inputs]}]
  (when root
    (ensure! (= (:head source) (git! root "rev-parse" "HEAD")) :source-head-drift)
    (ensure! (= (:tree source) (git! root "rev-parse" "HEAD^{tree}")) :source-tree-drift)
    (ensure! (empty? (git! root "status" "--porcelain" "--untracked-files=all")) :dirty-source))
  (doseq [[_ input] inputs]
    (ensure! (= input (file-input! (:path input))) :input-drift)))

(defn loaded-owner
  "Resolve an already-loaded normal owner. Does not load Bootstrap or launch it."
  [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Normal owner is not loaded" {:reason :missing-owner :operator name}))))

(defn replace-existing [value path replacement]
  (ensure! (contains? (get-in value (vec (butlast path))) (last path)) :missing-mutation-field)
  (ensure! (not= (get-in value path) replacement) :mutation-noop)
  (assoc-in value path replacement))

(defn different-id [value]
  (ensure! (and (string? value) (re-matches #"sha256:[0-9a-f]{64}" value)) :non-sha-id)
  (let [zero (str "sha256:" (apply str (repeat 64 "0")))]
    (if (= zero value) (str "sha256:" (apply str (repeat 64 "f"))) zero)))

(defn projection-index [descriptor name]
  (let [matches (keep-indexed #(when (= name (:name %2)) %1) (:semantic-projections descriptor))]
    (ensure! (= 1 (count matches)) :projection-cardinality)
    (first matches)))

(defn semantic-mutation [descriptor field]
  (ensure! (some #{field} semantic-fields) :unknown-semantic-field)
  (let [base [:semantic-projections (projection-index descriptor :syntax-implementation-binding) :value]
        implementation (get-in descriptor base)
        suffix (case field
                 :compiler-identity
                 (let [key (first (filter #(contains? (:compiler-identity implementation) %)
                                          [:source-content-hash :expression-lowering-source-content-hash
                                           :semantic-hash :artifact-hash]))]
                   (ensure! key :missing-compiler-identity-hash) [:compiler-identity key])
                 :public-function-hashes [:public-function-hashes 'c3-syntax-stream-verify-resolved]
                 :public-DATA [:public-DATA 'c3-syntax-object-contract :reference-order]
                 [field])
        path (into base suffix)
        original (get-in descriptor path)
        replacement (case field
                      :admission-status (do (ensure! (= :development-unadmitted original) :admission-scope)
                                            :substituted-admission-scope)
                      :public-DATA (do (ensure! (= :prior-only original) :reference-policy)
                                       :substituted-reference-policy)
                      (different-id original))]
    (replace-existing descriptor path replacement)))

(defn c3-rejected? [report]
  (and (= :failed (:status report)) (false? (:reader-products-authentic? report))))
(defn sh05-rejected? [report]
  (and (= :failed (:status report))
       (false? (get-in report [:checks :sh04-syntax-lineage-current?]))
       (some #{:sh04-syntax-lineage-current} (:failed-checks report))))
(defn c5-rejected? [report]
  (and (= :failed (:status report))
       (false? (get-in report [:checks :fresh-upstream-sh05-verification?]))
       (some #{:fresh-upstream-sh05-verification?} (:failed-checks report))))

(defn negative-passed? [label report]
  (if (= :invocation-mismatch label)
    (and (= "C5-UNRESOLVED" (:id report))
         (= [:selected-current-reader-invocation] (:missing-fields report))
         (= :reader-invocation-source-mismatch (get-in report [:facts :reason])))
    (and (= :failed (:status report))
         (case label
           (:missing-reader :changed-reader)
           (and (false? (get-in report [:checks :sh03-reader-lineage-current?]))
                (some #{:sh03-reader-lineage-current} (:failed-checks report)))
           :changed-parent
           (and (false? (get-in report [:checks :fresh-upstream-sh05-verification?]))
                (false? (get-in report [:checks :upstream-request-binding?]))
                (false? (get-in report [:upstream-verification :checks :artifact-id-current?]))
                (some #{:artifact-id-current} (get-in report [:upstream-verification :failed-checks])))
           :changed-provenance
           (and (false? (get-in report [:checks :provenance-current?]))
                (some #{:provenance-current?} (:failed-checks report)))
           false))))

(defn diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(defn source-nodes [c3]
  (filterv #(not= :generated-form (get-in % [:form :kind])) (:syntax-object-stream c3)))
(def span-keys [:byte-start :byte-end :scalar-start :scalar-end
                :line-start :column-start :line-end :column-end])
(defn expansion-facts [node]
  (:value (first (filter #(= :authenticated-reader-products (:fact-kind %)) (:facts node)))))

(defn coordinate-at [data offset]
  (ensure! (and (integer? offset) (<= 0 offset (alength data))) :byte-coordinate)
  (let [prefix (strict-text (java.util.Arrays/copyOfRange data 0 (int offset)))
        lines (str/split prefix #"\r\n|\r|\n" -1)
        tail (peek lines)]
    {:scalar (.codePointCount prefix 0 (.length prefix)) :line (count lines)
     :column (inc (.codePointCount ^String tail 0 (.length ^String tail)))}))

(defn check-source-coordinates! [owner c3 source text]
  (let [c2 (:c2-reader-artifact c3) seeds (:syntax-seed-stream c2)
        forms (into {} (map (juxt :form-id identity)) (:form-tree c2))
        nodes (source-nodes c3) data (.getBytes ^String text "UTF-8")
        semantic-id ((owner 'sh04-syntax-semantic-source-id) source (:source-unit-record c2))]
    (ensure! (= source (get-in c2 [:source-unit-record :path])) :actual-source-path)
    (ensure! (and (seq seeds) (= (mapv :form-id seeds) (mapv #(get-in % [:source :form-id]) nodes)))
             :source-form-join)
    (doseq [[seed node] (map vector seeds nodes)]
      (let [form (forms (:form-id seed)) span (get-in node [:span :primary])
            origin (first (filter #(= :source (:kind %)) (:origin node)))
            start (coordinate-at data (:byte-start span)) end (coordinate-at data (:byte-end span))]
        (ensure! (and form (= source (:source span)) (= semantic-id (:file span))
                      (= semantic-id (get-in node [:source :source-id]))
                      (= [(:open-token form) (:close-token form)] (get-in node [:source :token-range]))
                      (every? #(contains? (:span seed) %) span-keys)
                      (every? #(contains? (:span form) %) span-keys)
                      (= (select-keys (:span seed) span-keys)
                         (select-keys span span-keys)
                         (select-keys (:span form) span-keys))
                      (<= (:byte-start span) (:byte-end span))
                      (= start {:scalar (:scalar-start span) :line (:line-start span) :column (:column-start span)})
                      (= end {:scalar (:scalar-end span) :line (:line-end span) :column (:column-end span)})
                      (= (get-in node [:form :raw])
                         (strict-text (java.util.Arrays/copyOfRange data (int (:byte-start span)) (int (:byte-end span)))))
                      (= span (:span origin))
                      (= 'gravity.bootstrap.reader (get-in origin [:producer :name]))
                      (= [origin] (:origin node)))
                 :source-coordinate-and-origin)))
    :checked))

(defn check-expansion-history! [owner c3]
  (let [c2 (:c2-reader-artifact c3) source (get-in c2 [:source-unit-record :path])
        history ((owner 'sh04-syntax-reader-expansion-history) source c2)
        descriptor (get-in c3 [:gravity-syntax-boundary :authenticated-envelope-descriptor])
        summary (get-in descriptor [:semantic-projections (projection-index descriptor :syntax-product-identities) :value])
        roots (into {} (map (juxt :form-id :origins)) (:roots history))]
    (ensure! (= (some? (:id history)) (contains? summary :reader-expansion-history-id)) :history-presence)
    (ensure! (= (:id history) (:reader-expansion-history-id summary)) :history-envelope)
    (doseq [node (source-nodes c3)]
      (let [id (get-in node [:source :form-id]) origins (get roots id) facts (expansion-facts node)]
        (ensure! (= 1 (count (filter #(= :authenticated-reader-products (:fact-kind %)) (:facts node))))
                 :history-fact-cardinality)
        (ensure! (contains? roots id) :history-root-join)
        (ensure! (= (boolean (seq origins)) (contains? facts :reader-expansion-origins)) :history-fact-presence)
        (when (seq origins)
          (ensure! (= origins (:reader-expansion-origins facts)) :history-fact-value))))
    history))

(defn check-diagnostic-controls!
  "Exercise the supplied exact owner kernel; fixtures are not authenticated parents."
  [owner {:keys [source sh05 request diagnostic expected-origin]}]
  (let [recover (owner 'sh06-resolution-diagnostic-origin)]
    (ensure! (and (seq expected-origin) (= expected-origin (recover source sh05 request diagnostic))) :causal-origin)
    (doseq [changed [(assoc diagnostic :symbol 'unrelated)
                     (assoc diagnostic :namespace 'unrelated)
                     (assoc diagnostic :active-profile :core)
                     (assoc diagnostic :target :wasm)
                     (assoc-in diagnostic [:facts :c4-artifact-id] :substituted)
                     (assoc-in diagnostic [:facts :source-revision-id] :substituted)
                     (assoc-in diagnostic [:facts :rule-specific :position] :unrelated)
                     (assoc diagnostic :id "C5-CYCLE")
                     (assoc-in diagnostic [:facts :rule-specific] {:reason :missing-module-artifact})]]
      (ensure! (not= diagnostic changed) :diagnostic-mutation-noop)
      (ensure! (nil? (recover source sh05 request changed)) :noncausal-origin-rejected))
    ;; Duplicating the entire stream duplicates its actual matching owner even
    ;; when that owner is not the last node.
    (ensure! (nil? (recover source (update sh05 :expanded-syntax-stream #(into % %)) request diagnostic))
             :ambiguous-origin-rejected)
    {:status :checked :authority :kernel-only-parent-authentication-is-a-premise}))

(defn check-transported-diagnostic! [original carried expected-origin]
  (ensure! (and (seq expected-origin)
                (= expected-origin (:origin-chain (ex-data carried)))
                (= (ex-data original) (dissoc (ex-data carried) :origin-chain))
                (= (.getMessage ^Throwable original) (.getMessage ^Throwable carried))
                (identical? original (.getCause ^Throwable carried)))
           :diagnostic-conservation)
  :checked)

(defn coherent-negative [owner c3 sh05 c5 descriptor envelope]
  (let [c3-copy (-> c3 (assoc-in [:gravity-syntax-boundary :authenticated-envelope-descriptor] descriptor)
                    (assoc-in [:gravity-syntax-boundary :authenticated-envelope] envelope))
        c3-copy (assoc c3-copy :artifact-id ((owner 'c3-artifact-id) c3-copy))
        sh05-copy (-> sh05 (assoc-in [:gravity-macro-boundary :authenticated-sh04-artifact] c3-copy)
                      (assoc-in [:sh04-syntax-artifact :artifact-id] (:artifact-id c3-copy)))
        sh05-copy (assoc sh05-copy :artifact-id ((owner 'sh05-macro-artifact-id) sh05-copy))
        c5-copy (assoc c5 :sh05-macro-artifact sh05-copy)]
    {:c3 c3-copy :sh05 sh05-copy
     :c5 (assoc c5-copy :artifact-id ((owner 'sh06-resolution-artifact-id) c5-copy))}))

(defn exercise!
  "Explicit future consumer entry. Caller owns source/runtime review and admission.
  No main, process launcher, broker, Var replacement or automatic Bootstrap load."
  [owner {:keys [source-path source-text module-inputs reader-source-path invocation inputs] :as prepared}]
  (ensure! (and (:root prepared) (get-in prepared [:source :head]) (get-in prepared [:source :tree])
                (= #{:fixture :reader :syntax} (set (keys inputs)))
                (= source-path (get-in inputs [:fixture :path]))
                (= source-text (get-in inputs [:fixture :text]))
                (= reader-source-path (get-in inputs [:reader :path]))
                (= 192 (get-in inputs [:fixture :bytes]))
                (= fixture-sha (get-in inputs [:fixture :sha256]))) :prepared-input-contract)
  (recheck-inputs! prepared)
  (let [artifact ((owner 'compiler-c5-resolution-source-artifact)
                  source-path source-text module-inputs reader-source-path invocation)
        sh05 (:sh05-macro-artifact artifact)
        c3 (get-in sh05 [:gravity-macro-boundary :authenticated-sh04-artifact])
        boundary (:gravity-syntax-boundary c3) binding (:plan-binding boundary)
        descriptor (:authenticated-envelope-descriptor boundary) envelope (:authenticated-envelope boundary)
        implementation (get-in descriptor [:semantic-projections (projection-index descriptor :syntax-implementation-binding) :value])
        c3-report (fn [a] ((owner 'c3-syntax-verification-report) (:syntax-object-stream a)
                           (:syntax-serialization-fixture a) (:c2-reader-artifact a) (:gravity-syntax-boundary a) invocation))
        sh05-report #((owner 'sh05-macro-artifact-verification) % invocation)
        c5-report #((owner 'sh06-resolution-artifact-verification) % :gravity.bootstrap/legacy invocation)]
    (ensure! (and (= :gravity/sh06-resolution-artifact (:kind artifact)) (= :accepted (:status artifact))
                  (= :complete (get-in artifact [:capability-based-proof :status]))
                  (= :accepted (:status sh05)) (= :complete (get-in sh05 [:capability-based-proof :status]))
                  (= :complete (get-in c3 [:c3-syntax-results :status]))) :actual-products)
    (ensure! (and (= :gravity/sh04-development-syntax-plan-binding-v1 (:artifact binding))
                  (= :development-unadmitted (:admission-status binding))
                  (= (get-in inputs [:syntax :path]) (:source-path binding))
                  (= (str "sha256:" (get-in inputs [:syntax :sha256])) (:source-content-hash binding))
                  (= implementation (:implementation-projection binding))
                  (= :development-unadmitted (:admission-status implementation))
                  (= invocation (:operation-context boundary))) :selected-binding)
    (ensure! (= (str "sha256:" (get-in inputs [:reader :sha256]))
                (get-in c3 [:c2-reader-artifact :sh03-reader-authentication :reader-source-revision :source-content-hash]))
             :Reader-source-binding)
    (ensure! (= (str "sha256:" (get-in inputs [:fixture :sha256]))
                (get-in c3 [:c2-reader-artifact :source-unit-record :bytes-hash])) :fixture-source-binding)
    (doseq [[actual computed] [[(:artifact-id c3) ((owner 'c3-artifact-id) c3)]
                               [(:artifact-id sh05) ((owner 'sh05-macro-artifact-id) sh05)]
                               [(:artifact-id artifact) ((owner 'sh06-resolution-artifact-id) artifact)]
                               [(:artifact-id c3) (get-in sh05 [:sh04-syntax-artifact :artifact-id])]]]
      (ensure! (= actual computed) :immediate-parent-identities))
    (let [report (c3-report c3)]
      (ensure! (= :passed (:status report)) :fresh-C3)
      (ensure! (true? (:reader-products-authentic? report)) :fresh-Reader))
    (doseq [report [(sh05-report sh05) (c5-report artifact)]]
      (ensure! (and (= :passed (:status report)) (empty? (:failed-checks report))) :fresh-owner-verification))
    (check-source-coordinates! owner c3 source-path source-text)
    (let [history (check-expansion-history! owner c3)
          index (projection-index descriptor :syntax-product-identities)
          path [:gravity-syntax-boundary :authenticated-envelope-descriptor :semantic-projections index :value]
          changed (if (:id history)
                    (update-in c3 path dissoc :reader-expansion-history-id)
                    (assoc-in c3 (conj path :reader-expansion-history-id) (str "sha256:" fixture-sha)))]
      (ensure! (c3-rejected? (c3-report changed)) :history-envelope-substitution)
      (doseq [ordinal (range (count (:syntax-object-stream c3)))
              :let [node (nth (:syntax-object-stream c3) ordinal)]
              :when (and (not= :generated-form (get-in node [:form :kind]))
                         (seq (:reader-expansion-origins (expansion-facts node))))]
        (ensure! (c3-rejected? (c3-report (update-in c3 [:syntax-object-stream ordinal :facts]
                           #(mapv (fn [fact] (update fact :value dissoc :reader-expansion-origins)) %))))
                 :history-fact-removal)))
    (let [ordinal (first (keep-indexed #(when (not= :generated-form (get-in %2 [:form :kind])) %1)
                                      (:syntax-object-stream c3)))
          base [:syntax-object-stream ordinal]]
      (doseq [changed [(update-in c3 (into base [:span :primary :byte-end]) inc)
                       (update-in c3 (into base [:form :raw]) str " ")
                       (update-in c3 (conj base :origin) pop)]]
        (ensure! (c3-rejected? (c3-report changed)) :source-coordinate-substitution)))
    (let [registered (diagnostic #((owner 'sh04-development-syntax-verification-context!) invocation
                                   (assoc (:c2-reader-artifact c3) :registered-literal-projection {}) boundary))]
      (ensure! (and (= "C3-ID" (:id registered))
                    (= [:selected-syntax-registered-projection-unsupported] (:missing-fields registered)))
               :selected-registered-projection))
    (let [artifact-only (diagnostic #((owner 'sh04-syntax-current-binding!) source-path
                                      (dissoc invocation :syntax-source-selection)))]
      (ensure! (and (= "C3-ID" (:id artifact-only))
                    (= [:exact-pinned-syntax-source] (:missing-fields artifact-only)))
               :artifact-only-selection-does-not-select-current-source))
    (let [lineage [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :sh03-reader-authentication]
          revision (get-in sh05 (conj lineage :reader-source-revision))]
      (ensure! (map? revision) :Reader-revision)
      (doseq [[label report]
              [[:missing-reader (sh05-report (update-in sh05 lineage dissoc :reader-source-revision))]
               [:changed-reader (sh05-report (replace-existing sh05 (conj lineage :reader-source-revision :source-content-hash)
                                                  (different-id (:source-content-hash revision))))]
               [:changed-parent (c5-report (replace-existing artifact [:sh05-macro-artifact :artifact-id] (different-id (:artifact-id sh05))))]
               [:changed-provenance (c5-report (replace-existing artifact [:provenance :source-revision-id]
                                                               (different-id (get-in artifact [:provenance :source-revision-id]))))]
               [:invocation-mismatch (diagnostic #((owner 'sh06-resolution-artifact-from-sh05-artifact)
                                                    source-path sh05 module-inputs (get-in inputs [:syntax :path]) invocation))]]]
        (ensure! (negative-passed? label report) label)))
    (let [build (owner 'p15-s23-stage2-sh02-descriptor-envelope)
          verify (owner 'p15-s23-stage2-sh02-descriptor-envelope-verify!)
          options (:authenticated-envelope-options invocation)
          baseline (build :c3-syntax :gravity/sh04-syntax-products descriptor source-path options)]
      (ensure! (and (= :accepted (:status baseline))
                    (= :development-unadmitted (get-in baseline [:source-rule :admission-status]))
                    (= :passed (verify baseline :c3-syntax :gravity/sh04-syntax-products descriptor source-path options))
                    (= (:semantic-envelope-id envelope) (:semantic-envelope-id baseline))) :normal-AE-baseline)
      (doseq [field semantic-fields]
        (let [changed (semantic-mutation descriptor field)
              changed-envelope (build :c3-syntax :gravity/sh04-syntax-products changed source-path options)
              copies (coherent-negative owner c3 sh05 artifact changed changed-envelope)]
          (ensure! (and (= :accepted (:status changed-envelope))
                        (= :passed (verify changed-envelope :c3-syntax :gravity/sh04-syntax-products changed source-path options)))
                   [field :actual-AE])
          (doseq [[a b] [[(:semantic-envelope-id envelope) (:semantic-envelope-id changed-envelope)]
                          [(:artifact-id c3) (get-in copies [:c3 :artifact-id])]
                          [(:artifact-id sh05) (get-in copies [:sh05 :artifact-id])]
                          [(:artifact-id artifact) (get-in copies [:c5 :artifact-id])]]]
            (ensure! (not= a b) [field :identity-propagation]))
          (ensure! (c3-rejected? (c3-report (:c3 copies))) [field :C3-rejection])
          (ensure! (sh05-rejected? (sh05-report (:sh05 copies))) [field :SH05-rejection])
          (ensure! (c5-rejected? (c5-report (:c5 copies))) [field :C5-rejection]))))
    (let [physical (replace-existing c3 [:gravity-syntax-boundary :plan-binding :source-path] reader-source-path)]
      (ensure! (= (:artifact-id c3) ((owner 'c3-artifact-id) physical)) :physical-semantic-neutrality)
      (ensure! (c3-rejected? (c3-report physical)) :physical-binding-rejection)
      (ensure! (c3-rejected? (c3-report (update c3 :gravity-syntax-boundary dissoc :operation-context))) :context-rejection)
      (let [wrong (diagnostic #((owner 'sh05-macro-artifact-verification) sh05
                               (assoc-in invocation [:syntax-source-selection :source-path] reader-source-path)))]
        (ensure! (and (= "C3-ID" (:id wrong)) (= [:selected-current-syntax-source] (:missing-fields wrong))) :wrong-selection)))
    (recheck-inputs! prepared)
    {:status :harness-returned :identities {:C3 (:artifact-id c3) :SH05 (:artifact-id sh05) :C5 (:artifact-id artifact)}
     :nonclaims [:runtime-admission :independent-acceptance :relocation :paired-extension
                 :causal-diagnostic-composition :release :ACC13-closure]}))
