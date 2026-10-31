(ns gravity.self-hosting.resolution-parent-mutation-test
  "Fresh supplied-parent mutation control; requires the owner resource wrapper."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(def observations (atom nil))

(defn- implementation [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing Resolve observation seam" {:name name}))))

(defn- aliased-parent [parent]
  (let [run (get-in parent [:gravity-macro-boundary :expansion-runs 0])
        backing (object-array (mapcat identity run))
        position (first (keep #(when (= :resolved-verification (aget backing %)) %)
                              (range 0 (alength backing) 2)))
        value (clojure.lang.PersistentArrayMap. backing)
        result (assoc-in parent [:gravity-macro-boundary :expansion-runs 0] value)]
    (when-not (and position (= parent result))
      (throw (ex-info "Parent alias fixture is not value-preserving" {})))
    {:parent result
     ;; Leave the expansion and digest operands intact. Only the stored report
     ;; key changes, so a subsequent real owner shape check must observe it.
     :mutate! #(aset backing position :changed-resolved-verification-key)
     :restore! #(aset backing position :resolved-verification)
     :restored? #(= run value)}))

(defn- observe-construction [parent invocation mutate! operation]
  (let [verify-var (implementation 'sh06-resolution-verify-sh05)
        run-var (implementation 'sh06-resolution-run-request!)
        verify @verify-var owner-run @run-var
        events (atom []) mutated? (atom false) returns (atom 0)
        roots (atom nil)
        sample (fn []
                 {:same-parent? true
                  :invocation-current? (= invocation ((implementation 'sh06-resolution-reader-invocation)))
                  :proof-transaction-absent? (nil? ((implementation 'sh07-proof-transaction-context!)))
                  ;; This is the existing short-seam catalog, not a claim of
                  ;; complete transitive dispatch or closed-over-state proof.
                  :observed-roots-stable?
                  (every? (fn [[var value]] (identical? value @var)) @roots)})]
    (with-redefs-fn
      {verify-var
       (fn [value]
         (swap! events conj (assoc (sample) :event :macro-enter
                                   :same-parent? (identical? parent value) :mutated? @mutated?))
         (try
           (let [report (verify value)]
             (swap! events conj {:event :macro-return :mutated? @mutated? :status (:status report)
                                 :run-storage-exact? (get-in report [:checks :macro-run-storage-exact?])
                                 :failed-checks (:failed-checks report)})
             report)
           (catch Throwable error
             (swap! events conj {:event :macro-throw :mutated? @mutated?
                                 :error-class (.getName (class error)) :id (:id (ex-data error))})
             (throw error))))
       run-var
       (fn [& arguments]
         (swap! events conj {:event :resolve-enter :mutated? @mutated?})
         (let [result (apply owner-run arguments)]
           (swap! events conj {:event :resolve-return :mutated? @mutated?})
           (when (and mutate! (= 1 (swap! returns inc)))
             ;; The actual Resolve operation has returned successfully. No
             ;; verifier result or exception is substituted by this observer.
             (mutate!)
             (reset! mutated? true)
             (swap! events conj {:event :parent-mutated}))
           result))}
      (fn []
        (reset! roots ((implementation 'resolution-construction-roots)))
        (try
          (let [value (operation)]
            {:value value :events @events :mutated? @mutated?})
          (catch Throwable error
            {:error error :events @events :mutated? @mutated?}))))))

(deftest actual-current-parent-mutation-is-rejected-after-resolve
  (reset! observations {:phase :constructing-fresh-current-reader-parent})
  (when (some? ((implementation 'sh07-proof-transaction-context!)))
    (throw (ex-info "Fresh mutation control cannot borrow a proof transaction" {})))
  (let [source (.getCanonicalPath
                (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/accepted/compiler-subset.gravity"))
        reader (.getCanonicalPath (io/file b/sh03-reader-source-relative-path))
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}}
        syntax (b/compiler-c3-syntax-source-artifact
                 source (slurp source) (b/reader-project-context-for-source source)
                 nil nil reader invocation)
        _ (reset! observations {:phase :constructing-macro
                                :c3-proof-status (get-in syntax [:capability-based-proof :status])})
        original (b/sh05-macro-artifact-from-c3-artifact source syntax invocation)
        _ (reset! observations {:phase :constructing-positive-resolve
                                :parent-proof-status (get-in original [:capability-based-proof :status])})
        {:keys [parent mutate! restore! restored?]} (aliased-parent original)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        before-roots { (implementation 'sh06-resolution-verify-sh05) @(implementation 'sh06-resolution-verify-sh05)
                       (implementation 'sh06-resolution-run-request!) @(implementation 'sh06-resolution-run-request!)}]
    (is (= :complete (get-in original [:capability-based-proof :status])))
    (is (= original parent))
    (let [positive (observe-construction parent invocation nil construct)
          public-report (atom nil)]
      (reset! observations {:phase :positive-construction :events (:events positive)})
      (when-let [error (:error positive)] (throw error))
      (is (= :complete (get-in positive [:value :capability-based-proof :status])))
      (when-not (= :complete (get-in positive [:value :capability-based-proof :status]))
        (throw (ex-info "Positive construction did not produce a complete proof"
                        {:phase :positive-construction})))
      (reset! observations {:phase :independent-public-verification :events (:events positive)})
      (let [public (b/sh06-resolution-artifact-verification
                     (:value positive) :gravity.bootstrap/legacy invocation)]
        (reset! public-report public)
        (is (= :passed (:status public)))
        (when-not (= :passed (:status public))
          (throw (ex-info "Independent fresh positive verification failed"
                          {:phase :independent-positive :failed-checks (:failed-checks public)}))))
      (reset! observations {:phase :changed-construction})
      (let [changed (try (observe-construction parent invocation mutate! construct)
                         (finally (restore!)))
            error (:error changed) data (ex-data error)
            checks (get-in data [:observed :failed-checks])
            entries (filter #(= :macro-enter (:event %)) (:events changed))
            before-mutation (take-while #(not= :parent-mutated (:event %)) (:events changed))
            after-mutation (rest (drop-while #(not= :parent-mutated (:event %)) (:events changed)))
            result {:phase :complete :scope :current-selected-reader-parent-mutation
                    :positive-events (:events positive) :changed-events (:events changed)
                    :public-positive-status :passed :public-positive-report @public-report
                    :parent-construction-proof (:capability-based-proof original)
                    :changed-id (:id data)
                    :changed-missing-fields (:missing-fields data) :changed-failed-checks checks
                    :same-parent? (every? :same-parent? entries)
                    :invocation-current? (every? :invocation-current? entries)
                    :proof-transaction-absent? (every? :proof-transaction-absent? entries)
                    :observed-roots-stable? (every? :observed-roots-stable? entries)
                    :alias-restored? (restored?) :no-precomputed-reader-product? true
                    :no-verifier-report-substitution? true :whole-rv1-closed? false}]
        (reset! observations result)
        (is (= :resolve-return (:event (last before-mutation))))
        (is (= 1 (count (filter #(= :parent-mutated (:event %)) (:events changed)))))
        (is (= :passed (:status (first (filter #(= :macro-return (:event %)) before-mutation)))))
        (is (some #(and (= :macro-return (:event %)) (= :failed (:status %))
                        (false? (:run-storage-exact? %))
                        (some #{:macro-run-storage-exact} (:failed-checks %))) after-mutation))
        (is (= "C5-UNRESOLVED" (:id data)))
        (is (= [:final-authenticated-resolution-artifact] (:missing-fields data)))
        (is (some #{:fresh-upstream-sh05-verification?} checks))
        (is (nil? (:value changed)))
        (is (:mutated? changed))
        (is (seq entries))
        (is (:same-parent? result))
        (is (:invocation-current? result))
        (is (:proof-transaction-absent? result))
        (is (:observed-roots-stable? result))
        (is (:alias-restored? result))
        (is (every? (fn [[var value]] (identical? value @var)) before-roots))))))

;; Source preparation only: each exact var below needs the existing owner
;; wrapper and fresh producer admission. No earlier report is a positive here.
(defn- fragment-aliased-parent [parent field change]
  (let [run (get-in parent [:gravity-macro-boundary :expansion-runs 0])
        backing (object-array (mapcat identity run))
        position (first (keep #(when (= field (aget backing %)) (inc %))
                              (range 0 (alength backing) 2)))
        _ (when-not position (throw (ex-info "Missing Macro operand" {:field field})))
        before (aget backing position)
        after (change before)
        value (clojure.lang.PersistentArrayMap. backing)
        result (assoc-in parent [:gravity-macro-boundary :expansion-runs 0] value)]
    (when-not (and (= parent result) (not= before after))
      (throw (ex-info "Macro operand mutation is not distinct/value-preserving" {:field field})))
    {:parent result :before before :after after
     :mutate! #(aset backing position after)
     :restore! #(aset backing position before)
     :restored? #(and (identical? before (aget backing position)) (= run value))}))

(defn- changed-fragment-digest [before]
  (let [zero (str "sha256:" (apply str (repeat 64 "0")))]
    (if (= before zero) (str "sha256:" (apply str (repeat 64 "1"))) zero)))

(defn- macro-dispatch-mutation [kind]
  (let [execute-var (implementation 'sh05-macro-execute!)
        original @execute-var
        changed? (atom false) calls (atom [])
        dispatch
        (fn [redirect? source binding callee arguments]
          (let [target? (= 'sh05-verify-macro-template callee)
                changed-callee (if (and redirect? target?) 'sh05-expand-macro-template callee)
                plan-change? (and @changed? (= :plan kind) target?)
                selected (if plan-change?
                           (assoc-in binding [:plan :functions callee]
                                     (get-in binding [:plan :functions 'sh05-expand-macro-template]))
                           binding)
                _ (when plan-change?
                    (when-not (and (some? (get-in selected [:plan :functions callee]))
                                   (not= (:plan binding) (:plan selected))
                                   (= (dissoc binding :plan) (dissoc selected :plan)))
                      (throw (ex-info "Plan mutation did not preserve advertised identities" {}))))
                event {:event :macro-dispatch-enter :mutated? @changed?
                       :requested-callee callee :actual-callee changed-callee
                       :plan-changed? plan-change?
                       :advertised-identities-unchanged? (= (dissoc binding :plan) (dissoc selected :plan))
                       :arguments arguments}]
            (swap! calls conj event)
            (try
              ;; The original execution boundary owns the actual result/error.
              (let [result (original source selected changed-callee arguments)]
                (swap! calls conj {:event :macro-dispatch-return :mutated? @changed?
                                   :requested-callee callee :status (:status result)})
                result)
              (catch Throwable error
                (swap! calls conj {:event :macro-dispatch-throw :mutated? @changed?
                                   :requested-callee callee :id (:id (ex-data error))
                                   :error-class (.getName (class error))
                                   :error error})
                (throw error)))))
        initial (fn [& args] (apply dispatch (and @changed? (= :closed-state kind)) args))
        replacement (fn [& args] (apply dispatch true args))]
    {:execute-var execute-var :original original :initial initial :calls calls
     :mutate! #(do (reset! changed? true)
                   (when (= :callee kind) (alter-var-root execute-var (constantly replacement))))
     :restore! #(do (reset! changed? false) (alter-var-root execute-var (constantly initial)))
     :root-changed? #(not (identical? initial @execute-var))
     :restored? #(and (not @changed?) (identical? initial @execute-var))}))

(defn- fresh-fragment-parent []
  (when (some? ((implementation 'sh07-proof-transaction-context!)))
    (throw (ex-info "Fragment mutation cannot borrow a proof transaction" {})))
  (let [source (.getCanonicalPath
                (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/accepted/compiler-subset.gravity"))
        reader (.getCanonicalPath (io/file b/sh03-reader-source-relative-path))
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}}
        syntax (b/compiler-c3-syntax-source-artifact
                 source (slurp source) (b/reader-project-context-for-source source)
                 nil nil reader invocation)
        parent (b/sh05-macro-artifact-from-c3-artifact source syntax invocation)]
    (when-not (= :complete (get-in parent [:capability-based-proof :status]))
      (throw (ex-info "Fresh fragment parent failed" {})))
    {:source source :reader reader :invocation invocation :parent parent}))

(defn- check-fragment-mutation! [case-id field change dispatch-kind]
  (reset! observations {:phase :constructing-fresh-fragment-parent :case case-id})
  (let [{:keys [source reader invocation parent]} (fresh-fragment-parent)
        alias (when field (fragment-aliased-parent parent field change))
        parent (or (:parent alias) parent)
        run-count (count (get-in parent [:gravity-macro-boundary :expansion-runs]))
        dispatch (macro-dispatch-mutation dispatch-kind)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        before-roots (into {} (for [name ['sh06-resolution-verify-sh05 'sh06-resolution-run-request!
                                          'sh05-macro-execute!]
                                   :let [v (implementation name)]] [v @v]))
        restored (atom false)]
    (try
      (with-redefs-fn
        {(:execute-var dispatch) (:initial dispatch)}
        (fn []
          (try
            (let [positive (observe-construction parent invocation nil construct)
                  _ (when-let [error (:error positive)] (throw error))
                  _ (when-not (= :complete (get-in positive [:value :capability-based-proof :status]))
                      (throw (ex-info "Fragment positive construction failed" {:case case-id})))
                  public (b/sh06-resolution-artifact-verification
                           (:value positive) :gravity.bootstrap/legacy invocation)
                  _ (when-not (= :passed (:status public))
                      (throw (ex-info "Fragment positive public verification failed"
                                      {:case case-id :failed-checks (:failed-checks public)})))
                  positive-calls @(:calls dispatch)
                  _ (reset! (:calls dispatch) [])
                  root-changed (atom nil)
                  mutate! #(do (when alias ((:mutate! alias)))
                               ((:mutate! dispatch))
                               (reset! root-changed ((:root-changed? dispatch))))
                  changed (try (observe-construction parent invocation mutate! construct)
                               (finally
                                 (when alias ((:restore! alias)))
                                 ((:restore! dispatch))))
                  before (take-while #(not= :parent-mutated (:event %)) (:events changed))
                  after (rest (drop-while #(not= :parent-mutated (:event %)) (:events changed)))
                  calls (filter :mutated? @(:calls dispatch))
                  entries (filter #(= :macro-enter (:event %)) (:events changed))
                  error (:error changed) data (ex-data error)
                  result {:case case-id :phase :complete :scope :Macro-replay-fragment-only
                          :preparation-base "149503152d7edab3f7e638c000a553f62c54e679"
                          :source-path source :reader-source-path reader :invocation invocation
                          :original-parent-id (:artifact-id parent)
                          :original-parent-plan-binding (get-in parent [:gravity-macro-boundary :plan-binding])
                          :positive-events (:events positive) :public-positive-report public
                          :positive-macro-dispatch positive-calls
                          :changed-events (:events changed)
                          :actual-macro-dispatch (mapv #(dissoc % :error) calls)
                          :dispatch-kind dispatch-kind :dispatch-root-changed? @root-changed
                          :mutated-operand field :operand-before (:before alias) :operand-after (:after alias)
                          :changed-id (:id data) :changed-missing-fields (:missing-fields data)
                          :changed-failed-checks (get-in data [:observed :failed-checks])
                          :alias-restored? (if alias ((:restored? alias)) true)
                          :dispatch-restored? ((:restored? dispatch))
                          :whole-rv1-closed? false :elision-authorized? false}]
              (reset! observations result)
              (is (= :resolve-return (:event (last before))))
              (is (= 1 (count (filter #(= :parent-mutated (:event %)) (:events changed)))))
              (is (= :passed (:status (first (filter #(= :macro-return (:event %)) before)))))
              (is (seq entries))
              (is (every? #(and (:same-parent? %) (:invocation-current? %)
                               (:proof-transaction-absent? %) (:observed-roots-stable? %)) entries))
              (is (some #(and (= :macro-return (:event %)) (= :failed (:status %))) after))
              (is (not-any? #(= :resolve-enter (:event %)) after))
              (is (= "C5-UNRESOLVED" (:id data)))
              (is (= [:final-authenticated-resolution-artifact] (:missing-fields data)))
              (is (some #{:fresh-upstream-sh05-verification?} (get-in data [:observed :failed-checks])))
              (is (nil? (:value changed)))
              (is (:mutated? changed))
              (is (:alias-restored? result))
              (is (:dispatch-restored? result))
              (is (seq calls))
              (is (seq positive-calls))
              (is (every? #(= ['sh05-verify-macro-template 'sh05-verify-macro-resolved] %)
                          (mapv vec (partition-all 2 (map :requested-callee
                            (filter #(= :macro-dispatch-enter (:event %)) positive-calls))))))
              (is (every? :advertised-identities-unchanged?
                          (filter #(= :macro-dispatch-enter (:event %)) calls)))
              (if field
                (let [entered (filter #(= :macro-dispatch-enter (:event %)) calls)]
                  (is (= (vec (mapcat identity (repeat run-count
                               ['sh05-verify-macro-template 'sh05-verify-macro-resolved])))
                         (mapv :requested-callee entered)))
                  (is (= (vec (mapcat identity (repeat run-count
                               [:macro-dispatch-enter :macro-dispatch-return
                                :macro-dispatch-enter :macro-dispatch-return])))
                         (mapv :event calls)))
                  (is (some #(and (= :macro-dispatch-return (:event %)) (= :failed (:status %))) calls))
                  (is (some #(and (= :macro-return (:event %))
                                  (true? (:run-storage-exact? %))) after)))
                (do
                  (is (= (= :callee dispatch-kind) @root-changed))
                  (is (= :macro-dispatch-enter (:event (first calls))))
                  (is (= 'sh05-verify-macro-template (:requested-callee (first calls))))
                  (is (= :macro-dispatch-throw (:event (second calls))))
                  (is (= 2 (count calls)))
                  (is (= "C4-RETURN" (:id (second calls))))
                  (is (not-any? #(= 'sh05-verify-macro-resolved (:requested-callee %)) calls))
                  (is (= (= :plan dispatch-kind) (:plan-changed? (first calls))))
                  (is (= (if (= :plan dispatch-kind) 'sh05-verify-macro-template
                             'sh05-expand-macro-template) (:actual-callee (first calls)))))))
            (finally
              (when alias ((:restore! alias)))
              ((:restore! dispatch))
              (reset! restored (and (if alias ((:restored? alias)) true)
                                   ((:restored? dispatch))))))))
      (finally
        ;; with-redefs-fn has restored the original roots even on failed setup.
        (is @restored)
        (is (every? (fn [[v root]] (identical? root @v)) before-roots))))))

(deftest actual-macro-template-operand-mutation-after-resolve
  (check-fragment-mutation! :template :raw-template-result
    #(assoc-in % [:expansion-template :artifact-id] :rv1-changed-template) :operand))

(deftest actual-macro-resolved-operand-mutation-after-resolve
  (check-fragment-mutation! :resolved-expansion :resolved-expansion
    #(update % :artifact-id changed-fragment-digest) :operand))

(deftest actual-macro-request-operand-mutation-after-resolve
  (check-fragment-mutation! :request :digest-requests #(assoc-in % [0 :ordinal] 1) :operand))

(deftest actual-macro-digest-operand-mutation-after-resolve
  (check-fragment-mutation! :digest :resolved-digests
    #(update % 0 changed-fragment-digest) :operand))

(deftest actual-macro-live-callee-swap-after-resolve
  (check-fragment-mutation! :live-callee nil nil :callee))

(deftest actual-macro-live-plan-swap-with-unchanged-advertised-hash-after-resolve
  (check-fragment-mutation! :live-plan nil nil :plan))

(deftest actual-macro-same-function-closed-state-change-after-resolve
  (check-fragment-mutation! :closed-over-state nil nil :closed-state))

;; SOURCE PREPARATION ONLY. These exact vars have not run. They use the same
;; fresh authentic parent/Resolve/public route above, without borrowing its
;; historical results. Loading/running them requires separate runtime admission.
;; This is one native step-slot alias control, not nested-child alias coverage
;; or a callback/printer sentinel control; those definitions remain unimplemented.
(defn- trace-step-alias [parent field]
  (let [trace (:macro-expansion-trace parent)
        step (first trace)
        backing (object-array (mapcat identity step))
        position (first (keep #(when (= field (aget backing %)) (inc %))
                              (range 0 (alength backing) 2)))
        value (when position (aget backing position))
        changed (if (= :macro field)
                  'rv1-changed-macro
                  (when (and (string? value) (seq value))
                    (str (subs value 0 (dec (count value)))
                         (if (= \0 (last value)) \1 \0))))
        aliased (clojure.lang.PersistentArrayMap. backing)
        result (assoc parent :macro-expansion-trace (assoc trace 0 aliased))]
    (when-not (and position (some? changed) (not= value changed) (= parent result))
      (throw (ex-info "Trace alias must preserve the authentic parent before mutation"
                      {:field field})))
    {:parent result :before value :after changed
     :mutate! #(aset backing position changed)
     :restore! #(aset backing position value)
     :restored? #(= step aliased)}))

(defn- observe-trace-construction
  [source parent invocation mutate! operation]
  (let [trace (:macro-expansion-trace parent)
        trace-var (implementation 'sh05-macro-trace-id)
        verify-var (implementation 'sh06-resolution-verify-sh05)
        run-var (implementation 'sh06-resolution-run-request!)
        trace-id @trace-var verify @verify-var owner-run @run-var
        events (atom []) calls (atom 0) returns (atom 0) mutated? (atom false)
        roots (atom nil)
        sample (fn []
                 {:invocation-current? (= invocation ((implementation 'sh06-resolution-reader-invocation)))
                  :proof-transaction-absent? (nil? ((implementation 'sh07-proof-transaction-context!)))
                  :observed-roots-stable?
                  (every? (fn [[var value]] (identical? value @var)) @roots)})]
    (with-redefs-fn
      {trace-var
       (fn [path operand]
         (if-not (and (= source path) (identical? trace operand))
           (trace-id path operand)
           (let [ordinal (swap! calls inc)
                 event (merge (sample) {:event :trace-id-enter :ordinal ordinal
                                        :resolve-returns @returns :operand operand
                                        :stored-id (:macro-expansion-trace-id parent)})]
             (swap! events conj event)
             (when (= 2 ordinal)
               (when-not (= 1 @returns)
                 (throw (ex-info "Second trace ID was not reached after the actual Resolve return"
                                 {:id "RV1-TRACE-REACHABILITY" :resolve-returns @returns})))
               ;; The replay's full-trace equality has already succeeded to
               ;; reach this call. Mutate its actual operand, not a copy passed
               ;; to a replacement model. The delegate and stored ID stay fresh.
               (when mutate!
                 (mutate!)
                 (reset! mutated? true)
                 (swap! events conj {:event :trace-field-mutated :ordinal ordinal})))
             (try
               (let [result (trace-id path operand)]
                 (swap! events conj {:event :trace-id-return :ordinal ordinal :result result})
                 result)
               (catch Throwable error
                 (swap! events conj {:event :trace-id-throw :ordinal ordinal :error error})
                 (throw error))))))
       verify-var
       (fn [value]
         (swap! events conj (merge (sample) {:event :macro-enter
                                            :same-parent? (identical? parent value)}))
         (try
           (let [report (verify value)]
             (swap! events conj {:event :macro-return :report report})
             report)
           (catch Throwable error
             (swap! events conj {:event :macro-throw :error error})
             (throw error))))
       run-var
       (fn [& arguments]
         (swap! events conj {:event :resolve-enter})
         (let [result (apply owner-run arguments)]
           (swap! returns inc)
           (swap! events conj {:event :resolve-return})
           result))}
      (fn []
        (reset! roots ((implementation 'resolution-construction-roots)))
        (try
          (let [value (operation)]
            {:value value :events @events :mutated? @mutated?})
          (catch Throwable error
            {:error error :events @events :mutated? @mutated?}))))))

(defn- check-trace-field-mutation! [field]
  (reset! observations {:phase :constructing-fresh-trace-parent :field field})
  (let [{:keys [source reader invocation parent]} (fresh-fragment-parent)
        alias (trace-step-alias parent field)
        parent (:parent alias)
        stored-id (:macro-expansion-trace-id parent)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        vars (mapv implementation ['sh05-macro-trace-id 'sh06-resolution-verify-sh05
                                   'sh06-resolution-run-request!])
        before-roots (into {} (map (fn [var] [var @var]) vars))]
    (try
      (let [positive (observe-trace-construction source parent invocation nil construct)
            _ (reset! observations {:phase :positive-trace-reachability :field field
                                    :events (:events positive)})
            _ (when-let [error (:error positive)] (throw error))
            entered (filter #(= :trace-id-enter (:event %)) (:events positive))
            _ (when-not (and (= :complete (get-in positive [:value :capability-based-proof :status]))
                             (= [1 2] (mapv :ordinal entered))
                             (= [0 1] (mapv :resolve-returns entered)))
                (throw (ex-info "Authentic positive did not reach the selected two trace-ID calls"
                                {:id "RV1-TRACE-REACHABILITY" :events (:events positive)})))
            public (b/sh06-resolution-artifact-verification
                     (:value positive) :gravity.bootstrap/legacy invocation)
            _ (when-not (= :passed (:status public))
                (throw (ex-info "Fresh public trace positive failed"
                                {:phase :independent-positive :failed-checks (:failed-checks public)})))
            changed (try
                      (observe-trace-construction source parent invocation (:mutate! alias) construct)
                      (finally ((:restore! alias))))
            events (:events changed)
            entered (filter #(= :trace-id-enter (:event %)) events)
            returned (filter #(= :trace-id-return (:event %)) events)
            mutated (first (filter #(= :trace-field-mutated (:event %)) events))
            failed (some #(when (and (= :macro-return (:event %))
                                     (= :failed (get-in % [:report :status]))
                                     (false? (get-in % [:report :checks :trace-replay-current?])))
                            (:report %)) events)
            result {:phase :trace-control-complete :field field
                    :positive-events (:events positive) :public-positive-report public
                    :changed-events events :changed-error (:error changed)
                    :reached-second-id? (= [1 2] (mapv :ordinal entered))
                    :earlier-gate-refusal? (not (:mutated? changed))
                    :before (:before alias) :after (:after alias)
                    :alias-restored? ((:restored? alias))
                    :callback-printer-control-implemented? false
                    :nested-child-alias-control-implemented? false
                    :no-verifier-report-substitution? true :whole-rv1-closed? false}]
        (reset! observations result)
        (is (:reached-second-id? result))
        (is (= [0 1] (mapv :resolve-returns entered)))
        (is (= 1 (count (filter #(= :trace-field-mutated (:event %)) events))))
        (is (some? mutated))
        (is (false? (:earlier-gate-refusal? result)))
        (is (= [1 2] (mapv :ordinal returned)))
        (is (= stored-id (:result (first returned))))
        (is (not= stored-id (:result (second returned))))
        (is (some? failed))
        (is (some #{:trace-replay-current} (:failed-checks failed)))
        (is (= "C5-UNRESOLVED" (:id (ex-data (:error changed)))))
        (is (nil? (:value changed)))
        (is (= stored-id (:macro-expansion-trace-id parent)))
        (is (every? :same-parent? (filter #(= :macro-enter (:event %)) events)))
        (is (every? #(and (:invocation-current? %) (:proof-transaction-absent? %)
                         (:observed-roots-stable? %)) entered))
        (is (:alias-restored? result)))
      (finally
        ((:restore! alias))
        (is ((:restored? alias)))
        (is (every? (fn [[var value]] (identical? value @var)) before-roots))))))

(deftest actual-macro-trace-input-syntax-id-mutation-at-second-id
  (check-trace-field-mutation! :input-syntax-id))

(deftest actual-macro-trace-output-syntax-id-mutation-at-second-id
  (check-trace-field-mutation! :output-syntax-id))

(deftest actual-macro-trace-macro-mutation-at-second-id
  (check-trace-field-mutation! :macro))

(deftest actual-macro-trace-macro-version-mutation-at-second-id
  (check-trace-field-mutation! :macro-version))

(deftest actual-macro-trace-replay-id-mutation-at-second-id
  (check-trace-field-mutation! :trace-replay-id))

;; Separate SOURCE-ONLY definitions; the five field controls above are unchanged.
;; No target, callback, printer or provider has been executed by preparation.
(def ^:dynamic ^:private *trace-second-event-context* nil)

(defn- observe-trace-second-event [source parent invocation kind fail? operation]
  (let [trace (:macro-expansion-trace parent)
        trace-var (implementation 'sh05-macro-trace-id)
        digest-var (or (ns-resolve 'gravity.digest 'sha256-output-hex)
                       (throw (ex-info "Missing production digest writer port" {})))
        trace-id @trace-var original-digest @digest-var original-pr @#'clojure.core/pr
        port-var (case kind :digest-writer digest-var :printer #'clojure.core/pr)
        original-port @port-var
        selected-calls (atom 0) port-calls (atom 0) events (atom [])
        sentinel (ex-info "Selected trace second-event sentinel"
                          {:id "RV1-TRACE-SECOND-EVENT" :kind kind})]
    (letfn [(enter! [function]
              (let [event {:event :port-enter :ordinal (:ordinal *trace-second-event-context*)
                           :port-call (swap! port-calls inc) :function function
                           :installed-port @port-var}]
                (swap! events conj event)
                event))
            (delegate! [function operation]
              (let [event (enter! function)]
                (try
                  ;; Both events use the actual production delegate. Throw only
                  ;; after it returns, never supply a digest/print result model.
                  (let [result (operation)]
                    (swap! events conj (assoc event :event :port-delegate-return))
                    (when (and fail? (= 2 (:ordinal event)) (= 2 (:port-call event)))
                      (throw sentinel))
                    result)
                  (catch Throwable error
                    (swap! events conj (assoc event :event :port-throw :error error))
                    (throw error)))))
            (writer-port [output]
              ;; This ONE callback object reaches the real scoped digest sink
              ;; at both selected uses; only the dynamic production writer differs.
              (delegate! writer-port #((:write-bytes! *trace-second-event-context*) output)))
            (digest-port [write-bytes!]
              (if *trace-second-event-context*
                (binding [*trace-second-event-context*
                          (assoc *trace-second-event-context* :write-bytes! write-bytes!)]
                  (original-digest writer-port))
                (original-digest write-bytes!)))
            (printer-port [& values]
              (let [frame (first values)]
                ;; Sort-key printing remains fresh and is not a selected event.
                ;; Select only C6's final canonical digest frame, on this trace.
                (if (and *trace-second-event-context* (= 1 (count values))
                         (vector? frame) (= 2 (count frame))
                         (= :gravity/canonical-edn-v1 (first frame)))
                  (delegate! printer-port #(apply original-pr values))
                  (apply original-pr values))))
            (trace-port [path operand]
              (if (and (= source path) (identical? trace operand))
                (let [ordinal (swap! selected-calls inc)]
                  (binding [*trace-second-event-context* {:ordinal ordinal}]
                    (try
                      (trace-id path operand)
                      (catch Throwable error
                        (swap! events conj {:event :trace-delegate-throw :ordinal ordinal :error error})
                        (throw error)))))
                (trace-id path operand)))]
      (let [port (case kind :digest-writer digest-port :printer printer-port)
            result (with-redefs-fn {trace-var trace-port port-var port}
                     #(observe-trace-construction source parent invocation nil operation))]
        (assoc result :port-events @events :sentinel sentinel
               :port-restored? (identical? original-port @port-var)
               :trace-root-restored? (identical? trace-id @trace-var)
               :context-restored? (nil? *trace-second-event-context*))))))

(defn- check-trace-second-event! [kind]
  (when (some? *trace-second-event-context*)
    (throw (ex-info "Second-event control cannot borrow another trace scope" {})))
  (reset! observations {:phase :constructing-fresh-second-event-parent :kind kind})
  (let [{:keys [source reader invocation parent]} (fresh-fragment-parent)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        positive (observe-trace-second-event source parent invocation kind false construct)
        trace-events (filter #(= :trace-id-enter (:event %)) (:events positive))
        port-events (filter #(= :port-enter (:event %)) (:port-events positive))]
    (reset! observations {:phase :positive-second-event-reachability :kind kind
                          :trace-events (:events positive) :port-events (:port-events positive)})
    (when-let [error (:error positive)] (throw error))
    (when-not (and (= :complete (get-in positive [:value :capability-based-proof :status]))
                   (= [1 2] (mapv :ordinal trace-events))
                   (= [0 1] (mapv :resolve-returns trace-events))
                   (= [1 2] (mapv :ordinal port-events))
                   (= [1 2] (mapv :port-call port-events)))
      (throw (ex-info "Authentic positive did not reach both selected production events"
                      {:id "RV1-TRACE-REACHABILITY" :kind kind})))
    (let [public (b/sh06-resolution-artifact-verification
                   (:value positive) :gravity.bootstrap/legacy invocation)]
      (when-not (= :passed (:status public))
        (throw (ex-info "Fresh public second-event positive failed"
                        {:phase :independent-positive :failed-checks (:failed-checks public)})))
      (let [changed (observe-trace-second-event source parent invocation kind true construct)
            events (:events changed) ports (:port-events changed) sentinel (:sentinel changed)
            entered (filter #(= :trace-id-enter (:event %)) events)
            port-entered (filter #(= :port-enter (:event %)) ports)
            port-thrown (filter #(= :port-throw (:event %)) ports)
            trace-thrown (filter #(= :trace-id-throw (:event %)) events)
            macro-thrown (filter #(= :macro-throw (:event %)) events)
            reached? (and (= [1 2] (mapv :ordinal entered))
                          (= [0 1] (mapv :resolve-returns entered))
                          (= [1 2] (mapv :ordinal port-entered)))
            result {:phase :second-event-control-complete :kind kind
                    :positive-events (:events positive) :positive-port-events (:port-events positive)
                    :public-positive-report public :changed-events events :changed-port-events ports
                    :changed-error (:error changed) :sentinel sentinel
                    :reached-second-event? reached? :earlier-gate-refusal? (not reached?)
                    :nested-child-alias-control-implemented? false
                    :no-verifier-report-substitution? true :whole-rv1-closed? false}]
        (reset! observations result)
        (is reached?)
        (is (false? (:earlier-gate-refusal? result)))
        (is (= [1 2] (mapv :port-call port-entered)))
        (is (identical? (:function (first port-entered)) (:function (second port-entered))))
        (is (identical? (:installed-port (first port-entered)) (:installed-port (second port-entered))))
        (is (= [:port-enter :port-delegate-return :port-enter :port-delegate-return
                :port-throw :trace-delegate-throw] (mapv :event ports)))
        (is (= 1 (count port-thrown) (count trace-thrown) (count macro-thrown)))
        (is (= 2 (:ordinal (first trace-thrown))))
        (is (every? #(identical? sentinel (:error %))
                    (concat port-thrown trace-thrown macro-thrown
                            (filter #(= :trace-delegate-throw (:event %)) ports))))
        (is (= [:trace-id-throw :macro-throw]
               (mapv :event (filter #(#{:trace-id-throw :macro-throw} (:event %)) events))))
        ;; C5's original catch contains the Macro Throwable and emits its own
        ;; existing boundary diagnostic. Do not demand a substituted outer throw.
        (is (= "C5-UNRESOLVED" (:id (ex-data (:error changed)))))
        (is (not (identical? sentinel (:error changed))))
        (is (nil? (:value changed)))
        (is (= :passed (get-in (first (filter #(= :macro-return (:event %)) events)) [:report :status])))
        (is (every? #(and (:invocation-current? %) (:proof-transaction-absent? %)
                         (:observed-roots-stable? %)) entered))
        (is (every? #(and (:port-restored? %) (:trace-root-restored? %) (:context-restored? %))
                    [positive changed]))))))

(deftest actual-macro-trace-same-writer-second-event-throws-after-original-write
  (check-trace-second-event! :digest-writer))

(deftest actual-macro-trace-same-printer-second-event-throws-after-original-print
  (check-trace-second-event! :printer))

;; SOURCE PREPARATION ONLY. This is a deliberately changed carrier after the
;; original full-trace equality, not a nested child in the authentic producer.
(defn- canonical-refusal-trace-parent [parent]
  (let [trace (:macro-expansion-trace parent)
        step (first trace)
        backing (object-array (mapcat identity step))
        position (first (keep #(when (= :input-syntax-id (aget backing %)) (inc %))
                              (range 0 (alength backing) 2)))
        before (when position (aget backing position))
        forbidden (with-meta {:changed-carrier :rv1} {:canonical-refusal true})
        aliased (clojure.lang.PersistentArrayMap. backing)
        result (assoc parent :macro-expansion-trace (assoc trace 0 aliased))]
    (when-not (and position (string? before) (= parent result)
                   (identical? clojure.lang.PersistentArrayMap (class forbidden)))
      (throw (ex-info "Canonical-refusal fixture must start with the authentic scalar"
                      {:field :input-syntax-id})))
    {:parent result :before before :forbidden forbidden
     :mutate! #(aset backing position forbidden)
     :restore! #(aset backing position before)
     :restored? #(= step aliased)}))

(deftest actual-macro-trace-canonical-metadata-refusal-at-second-id
  (reset! observations {:phase :constructing-fresh-canonical-refusal-parent})
  (let [{:keys [source reader invocation parent]} (fresh-fragment-parent)
        alias (canonical-refusal-trace-parent parent)
        parent (:parent alias)
        stored-id (:macro-expansion-trace-id parent)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        before-roots (into {} (for [name ['sh05-macro-trace-id 'sh06-resolution-verify-sh05
                                          'sh06-resolution-run-request!]
                                   :let [var (implementation name)]] [var @var]))]
    (try
      (let [positive (observe-trace-construction source parent invocation nil construct)
            entered (filter #(= :trace-id-enter (:event %)) (:events positive))]
        (reset! observations {:phase :positive-canonical-refusal-reachability
                              :events (:events positive)})
        (when-let [error (:error positive)] (throw error))
        (when-not (and (= :complete (get-in positive [:value :capability-based-proof :status]))
                       (= [1 2] (mapv :ordinal entered))
                       (= [0 1] (mapv :resolve-returns entered)))
          (throw (ex-info "Authentic positive did not reach the selected trace pair"
                          {:id "RV1-TRACE-REACHABILITY"})))
        (let [public (b/sh06-resolution-artifact-verification
                       (:value positive) :gravity.bootstrap/legacy invocation)]
          (when-not (= :passed (:status public))
            (throw (ex-info "Fresh public canonical-refusal positive failed"
                            {:phase :independent-positive :failed-checks (:failed-checks public)})))
          (let [changed (try
                          (observe-trace-construction source parent invocation (:mutate! alias) construct)
                          (finally ((:restore! alias))))
                events (:events changed)
                entered (filter #(= :trace-id-enter (:event %)) events)
                returned (filter #(= :trace-id-return (:event %)) events)
                thrown (filter #(= :trace-id-throw (:event %)) events)
                macro-thrown (filter #(= :macro-throw (:event %)) events)
                original-error (:error (first thrown))
                data (ex-data original-error)
                result {:phase :canonical-refusal-control-complete
                        :positive-events (:events positive) :public-positive-report public
                        :changed-events events :changed-error (:error changed)
                        :original-canonical-error original-error
                        :reached-second-id? (= [1 2] (mapv :ordinal entered))
                        :earlier-gate-refusal? (not (:mutated? changed))
                        :changed-field :input-syntax-id :before (:before alias)
                        :forbidden-class (.getName (class (:forbidden alias)))
                        :forbidden-metadata (meta (:forbidden alias))
                        :deliberate-changed-carrier? true :authentic-nested-child? false
                        :alias-restored? ((:restored? alias))
                        :no-canonical-result-substitution? true :whole-rv1-closed? false}]
            (reset! observations result)
            (is (:reached-second-id? result))
            (is (= [0 1] (mapv :resolve-returns entered)))
            (is (false? (:earlier-gate-refusal? result)))
            (is (= [:trace-id-enter :trace-id-return :trace-id-enter
                    :trace-field-mutated :trace-id-throw :macro-throw]
                   (mapv :event (filter #(#{:trace-id-enter :trace-id-return :trace-field-mutated
                                           :trace-id-throw :macro-throw} (:event %)) events))))
            (is (= [1] (mapv :ordinal returned)))
            (is (= stored-id (:result (first returned))))
            (is (= 1 (count thrown) (count macro-thrown)))
            (is (= 2 (:ordinal (first thrown))))
            (is (identical? original-error (:error (first macro-thrown))))
            (is (= "C6-VERIFY" (:id data)))
            (is (= :metadata-free-canonical-value (:missing-fact data)))
            (is (= source (get-in data [:source-span :source])))
            (is (= "clojure.lang.PersistentArrayMap" (get-in data [:subject :value-kind])))
            ;; Preserve the existing C5 containment diagnostic, rather than
            ;; replacing it with the inner canonical Throwable.
            (is (= "C5-UNRESOLVED" (:id (ex-data (:error changed)))))
            (is (not (identical? original-error (:error changed))))
            (is (nil? (:value changed)))
            (is (= stored-id (:macro-expansion-trace-id parent)))
            (is (= :passed (get-in (first (filter #(= :macro-return (:event %)) events))
                                  [:report :status])))
            (is (every? :same-parent? (filter #(= :macro-enter (:event %)) events)))
            (is (every? #(and (:invocation-current? %) (:proof-transaction-absent? %)
                             (:observed-roots-stable? %)) entered))
            (is (:alias-restored? result)))))
      (finally
        ((:restore! alias))
        (is ((:restored? alias)))
        (is (every? (fn [[var value]] (identical? value @var)) before-roots))))))

;; SOURCE PREPARATION ONLY. Profile is packaged from the real module, but is
;; absent from the five-field ID preimage. A skipped second ID proves no reuse.
(defn- unprojected-profile-parent [parent]
  (let [trace (:macro-expansion-trace parent) step (first trace)
        backing (object-array (mapcat identity step))
        position (first (keep #(when (= :profile (aget backing %)) (inc %))
                              (range 0 (alength backing) 2)))
        before (when position (aget backing position))
        after (if (= :hosted before) :native :hosted)
        aliased (clojure.lang.PersistentArrayMap. backing)
        result (assoc parent :macro-expansion-trace (assoc trace 0 aliased))
        projected #(mapv (fn [value]
                          (select-keys value [:input-syntax-id :output-syntax-id :macro
                                              :macro-version :trace-replay-id]))
                        (:macro-expansion-trace result))]
    (when-not (and position (keyword? before) (not= before after) (= parent result))
      (throw (ex-info "Profile alias must preserve the actual packaged parent" {})))
    {:parent result :before before :after after :projected projected
     :mutate! #(aset backing position after)
     :restore! #(aset backing position before)
     :restored? #(= step aliased)}))

(defn- observe-unprojected-profile [source parent invocation mutate! operation]
  (let [trace (:macro-expansion-trace parent) stream (:expanded-syntax-stream parent)
        trace-var (implementation 'sh05-macro-trace-id)
        stream-var (implementation 'sh05-expanded-syntax-stream-id)
        verify-var (implementation 'sh06-resolution-verify-sh05)
        trace-id @trace-var stream-id @stream-var verify @verify-var
        changed? (atom false) details (atom [])
        record! (fn [event data]
                  (swap! details conj (merge {:event event :after-mutation? @changed?} data)))]
    (with-redefs-fn
      {trace-var
       (fn [path operand]
         (when (and (= source path) (identical? trace operand)) (record! :trace-id-enter {}))
         (let [value (trace-id path operand)]
           (when (and (= source path) (identical? trace operand))
             (record! :trace-id-return {:value value}))
           value))
       stream-var
       (fn [path operand]
         (when (and (= source path) (identical? stream operand)) (record! :stream-id-enter {}))
         (let [value (stream-id path operand)]
           (when (and (= source path) (identical? stream operand))
             (record! :stream-id-return {:value value}))
           value))
       verify-var
       (fn [value]
         (let [report (verify value)]
           (when (identical? parent value) (record! :macro-report {:report report}))
           report))}
      (fn []
        (assoc
         (observe-construction
          parent invocation
          (when mutate!
            #(do (mutate!) (reset! changed? true) (record! :profile-mutated {})))
          operation)
         :details @details)))))

(deftest actual-macro-trace-unprojected-profile-refused-before-second-id
  (reset! observations {:phase :constructing-fresh-unprojected-profile-parent})
  (let [{:keys [source reader invocation parent]} (fresh-fragment-parent)
        alias (unprojected-profile-parent parent) parent (:parent alias)
        before-projected ((:projected alias))
        stored-id (:macro-expansion-trace-id parent)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        before-roots (into {} (for [name ['sh05-macro-trace-id 'sh05-expanded-syntax-stream-id
                                          'sh06-resolution-verify-sh05 'sh06-resolution-run-request!]
                                   :let [var (implementation name)]] [var @var]))]
    (try
      (let [positive (observe-unprojected-profile source parent invocation nil construct)]
        (reset! observations {:phase :positive-unprojected-reachability :events (:events positive)
                              :details (:details positive)})
        (when-let [error (:error positive)] (throw error))
        (when-not (and (= :complete (get-in positive [:value :capability-based-proof :status]))
                       (= 2 (count (filter #(= :trace-id-return (:event %)) (:details positive)))))
          (throw (ex-info "Authentic positive did not reach both trace-ID uses"
                          {:id "RV1-TRACE-REACHABILITY"})))
        (let [public (b/sh06-resolution-artifact-verification
                       (:value positive) :gravity.bootstrap/legacy invocation)]
          (when-not (= :passed (:status public))
            (throw (ex-info "Fresh public unprojected-profile positive failed"
                            {:phase :independent-positive :failed-checks (:failed-checks public)})))
          (let [mutation (atom nil)
                changed (try
                          (observe-unprojected-profile
                           source parent invocation
                           #(do ((:mutate! alias))
                                (reset! mutation {:profile (get-in parent [:macro-expansion-trace 0 :profile])
                                                  :projected ((:projected alias))
                                                  :stored-id (:macro-expansion-trace-id parent)}))
                           construct)
                          (finally ((:restore! alias))))
                details (:details changed) events (:events changed)
                later (filter :after-mutation? details)
                report (:report (first (filter #(= :macro-report (:event %)) later)))
                before (take-while #(not= :parent-mutated (:event %)) events)
                after (rest (drop-while #(not= :parent-mutated (:event %)) events))
                result {:phase :unprojected-profile-control-complete
                        :positive-events (:events positive) :public-positive-report public
                        :changed-events events :changed-details details :changed-error (:error changed)
                        :mutation @mutation :before-profile (:before alias) :after-profile (:after alias)
                        :original-failed-macro-report report
                        :second-id-skipped? (not-any? #(= :trace-id-enter (:event %)) later)
                        :earlier-full-trace-refusal? (false? (get-in report [:checks :trace-replay-current?]))
                        :digest-reuse-evidence? false :alias-restored? ((:restored? alias))
                        :no-verifier-report-substitution? true :whole-rv1-closed? false}]
            (reset! observations result)
            (is (:mutated? changed))
            (is (= :resolve-return (:event (last before))))
            (is (= 1 (count (filter #(= :parent-mutated (:event %)) events))))
            (is (= (:after alias) (:profile @mutation)))
            (is (= before-projected (:projected @mutation)))
            (is (= stored-id (:stored-id @mutation) (:macro-expansion-trace-id parent)))
            (is (= 1 (count (filter #(= :trace-id-return (:event %)) details))))
            (is (= stored-id (:value (first (filter #(= :trace-id-return (:event %)) details)))))
            (is (:second-id-skipped? result))
            (is (:earlier-full-trace-refusal? result))
            (is (= [:profile-mutated :stream-id-enter :stream-id-return :macro-report]
                   (mapv :event later)))
            (is (= (:expanded-syntax-stream-id parent)
                   (:value (first (filter #(= :stream-id-return (:event %)) later)))))
            (is (= :failed (:status report)))
            (is (= :passed (get-in report [:gravity-verifiers :template :status])))
            (is (= :passed (get-in report [:gravity-verifiers :resolved :status])))
            (is (some #{:trace-replay-current} (:failed-checks report)))
            (is (true? (get-in report [:checks :macro-output-current?])))
            (is (true? (get-in report [:checks :generated-origins-current?])))
            (is (true? (get-in report [:checks :input-lineage-current?])))
            (is (not-any? #(= :resolve-enter (:event %)) after))
            (is (= "C5-UNRESOLVED" (:id (ex-data (:error changed)))))
            (is (nil? (:value changed)))
            (is (every? #(and (:same-parent? %) (:invocation-current? %)
                             (:proof-transaction-absent? %) (:observed-roots-stable? %))
                        (filter #(= :macro-enter (:event %)) events)))
            (is (:alias-restored? result)))))
      (finally
        ((:restore! alias))
        (is ((:restored? alias)))
        (is (every? (fn [[var value]] (identical? value @var)) before-roots))))))

;; SOURCE PREPARATION ONLY. Actual source fixtures supply the runs; no synthetic
;; artifact, verifier result or standalone chunk calculation supplies a positive.
(defn- fresh-trace-order-parent [size]
  (when (some? ((implementation 'sh07-proof-transaction-context!)))
    (throw (ex-info "Trace-order control cannot borrow a proof transaction" {})))
  (let [source (.getCanonicalPath
                (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-06/accepted/trace-order-"
                              size ".gravity")))
        reader (.getCanonicalPath (io/file b/sh03-reader-source-relative-path))
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}}
        syntax (b/compiler-c3-syntax-source-artifact
                 source (slurp source) (b/reader-project-context-for-source source)
                 nil nil reader invocation)
        parent (b/sh05-macro-artifact-from-c3-artifact source syntax invocation)
        trace (:macro-expansion-trace parent)]
    (when-not (and (= :complete (get-in parent [:capability-based-proof :status]))
                   (= size (:expanded-defn-count parent) (count trace)
                      (count (get-in parent [:gravity-macro-boundary :expansion-runs])))
                   (= size (count (set (map :input-syntax-id trace))))
                   (= size (count (set (map :output-syntax-id trace)))))
      (throw (ex-info "Actual fixture did not produce distinct ordered Macro runs"
                      {:fixture-size size})))
    {:source source :reader reader :invocation invocation :parent parent}))

(defn- trace-order-parent [parent kind]
  (let [trace (:macro-expansion-trace parent) size (count trace)
        edge (min 15 (dec size))
        changed (case kind
                  :permutation (assoc trace 0 (peek trace) (dec size) (first trace))
                  :insertion (let [at (min 16 size)]
                               (into (conj (subvec trace 0 at) (peek trace))
                                     (subvec trace at)))
                  :deletion (into (subvec trace 0 edge) (subvec trace (inc edge)))
                  :duplicate (assoc trace edge (first trace)))
        backing (object-array (mapcat identity parent))
        position (first (keep #(when (= :macro-expansion-trace (aget backing %)) (inc %))
                              (range 0 (alength backing) 2)))
        aliased (clojure.lang.PersistentArrayMap. backing)]
    (when-not (and position (= parent aliased) (not= trace changed)
                   (every? (fn [step] (some #(identical? step %) trace)) changed))
      (throw (ex-info "Trace order fixture must retain only actual produced steps" {:kind kind})))
    {:parent aliased :before trace :after changed
     :mutate! #(aset backing position changed)
     :restore! #(aset backing position trace)
     :restored? #(and (identical? trace (:macro-expansion-trace aliased)) (= parent aliased))}))

(defn- observe-trace-order [source parent invocation mutate! operation]
  (let [trace-var (implementation 'sh05-macro-trace-id)
        projection-var (implementation 'sh05-macro-trace-identity-input)
        stream-var (implementation 'sh05-expanded-syntax-stream-id)
        verify-var (implementation 'sh06-resolution-verify-sh05)
        trace-id @trace-var projection @projection-var stream-id @stream-var verify @verify-var
        changed? (atom false) details (atom [])
        record! (fn [event data]
                  (swap! details conj (merge {:event event :after-mutation? @changed?} data)))]
    (with-redefs-fn
      {trace-var
       (fn [path operand]
         ;; Count ALL trace-ID calls for this actual source, including an
         ;; unexpected stale operand after replacement of the parent slot.
         (when (= source path)
           (record! :trace-id-enter {:current-operand? (identical? operand (:macro-expansion-trace parent))}))
         (let [value (trace-id path operand)]
           (when (= source path) (record! :trace-id-return {:value value}))
           value))
       projection-var
       (fn [operand]
         (let [value (projection operand)]
           (when (identical? operand (:macro-expansion-trace parent))
             (record! :projection-return {:value value}))
           value))
       stream-var
       (fn [path operand]
         (let [value (stream-id path operand)]
           (when (and (= source path) (identical? operand (:expanded-syntax-stream parent)))
             (record! :stream-id-return {:value value}))
           value))
       verify-var
       (fn [value]
         (let [report (verify value)]
           (when (identical? parent value) (record! :macro-report {:report report}))
           report))}
      (fn []
        (assoc (observe-construction
                parent invocation
                (when mutate! #(do (mutate!) (reset! changed? true) (record! :trace-mutated {})))
                operation)
               :details @details)))))

(defn- check-trace-order! [size kind]
  (reset! observations {:phase :constructing-fresh-trace-order-parent :size size :kind kind})
  (let [{:keys [source reader invocation parent]} (fresh-trace-order-parent size)
        alias (trace-order-parent parent kind) parent (:parent alias)
        stored-id (:macro-expansion-trace-id parent)
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        construct #(b/sh06-resolution-artifact-from-sh05-artifact source parent inputs reader invocation)
        before-roots (into {} (for [name ['sh05-macro-trace-id 'sh05-macro-trace-identity-input
                                          'sh05-expanded-syntax-stream-id 'sh06-resolution-verify-sh05
                                          'sh06-resolution-run-request!]
                                   :let [var (implementation name)]] [var @var]))]
    (try
      (let [positive (observe-trace-order source parent invocation nil construct)
            projections (mapv :value (filter #(= :projection-return (:event %)) (:details positive)))
            expected-chunks (case size 15 [15] 16 [16] 17 [16 1])]
        (reset! observations {:phase :positive-order-reachability :size size :kind kind
                              :events (:events positive) :details (:details positive)})
        (when-let [error (:error positive)] (throw error))
        (when-not (and (= :complete (get-in positive [:value :capability-based-proof :status]))
                       (= 2 (count projections)
                          (count (filter #(= :trace-id-return (:event %)) (:details positive))))
                       (every? :current-operand? (filter #(= :trace-id-enter (:event %)) (:details positive))))
          (throw (ex-info "Actual positive did not reach two original trace projections"
                          {:id "RV1-TRACE-REACHABILITY" :size size :kind kind})))
        (is (= (first projections) (second projections)))
        (is (= [stored-id stored-id]
               (mapv :value (filter #(= :trace-id-return (:event %)) (:details positive)))))
        ;; Inspect the actual original projection returns, not a replacement
        ;; partition model. Width 16 is an encoding detail, never admission fuel.
        (doseq [value projections]
          (is (= size (:item-count value)))
          (is (= expected-chunks (mapv :item-count (:item-chunks value))))
          (is (= (vec (range size)) (mapv :ordinal (mapcat :items (:item-chunks value))))))
        (let [public (b/sh06-resolution-artifact-verification
                       (:value positive) :gravity.bootstrap/legacy invocation)]
          (when-not (= :passed (:status public))
            (throw (ex-info "Fresh public order positive failed"
                            {:phase :independent-positive :failed-checks (:failed-checks public)})))
          (let [changed (try (observe-trace-order source parent invocation (:mutate! alias) construct)
                             (finally ((:restore! alias))))
                events (:events changed) details (:details changed)
                later (filter :after-mutation? details)
                report (:report (first (filter #(= :macro-report (:event %)) later)))
                before (take-while #(not= :parent-mutated (:event %)) events)
                after (rest (drop-while #(not= :parent-mutated (:event %)) events))
                result {:phase :trace-order-control-complete :size size :kind kind
                        :before-trace (:before alias) :after-trace (:after alias)
                        :positive-projections projections :public-positive-report public
                        :changed-events events :changed-details details :changed-error (:error changed)
                        :original-failed-macro-report report
                        :second-id-skipped? (not-any? #(= :trace-id-enter (:event %)) later)
                        :earlier-full-trace-refusal? (false? (get-in report [:checks :trace-replay-current?]))
                        :digest-reuse-evidence? false :alias-restored? ((:restored? alias))
                        :no-synthetic-step-or-report? true :whole-rv1-closed? false}]
            (reset! observations result)
            (is (:mutated? changed))
            (is (= :resolve-return (:event (last before))))
            (is (= 1 (count (filter #(= :parent-mutated (:event %)) events))))
            (is (= (case kind :insertion (inc size) :deletion (dec size) size) (count (:after alias))))
            (is (not= (:before alias) (:after alias)))
            (is (= stored-id (:macro-expansion-trace-id parent)))
            (is (= 1 (count (filter #(= :trace-id-return (:event %)) details))))
            (is (= stored-id (:value (first (filter #(= :trace-id-return (:event %)) details)))))
            (is (:second-id-skipped? result))
            (is (:earlier-full-trace-refusal? result))
            (is (= [:trace-mutated :stream-id-return :macro-report] (mapv :event later)))
            (is (= (:expanded-syntax-stream-id parent) (:value (second later))))
            (is (= :failed (:status report)))
            (is (= :passed (get-in report [:gravity-verifiers :template :status])))
            (is (= :passed (get-in report [:gravity-verifiers :resolved :status])))
            (is (some #{:trace-replay-current} (:failed-checks report)))
            (is (true? (get-in report [:checks :macro-output-current?])))
            (is (false? (get-in report [:checks :generated-origins-current?])))
            (is (true? (get-in report [:checks :input-lineage-current?])))
            (is (not-any? #(= :resolve-enter (:event %)) after))
            (is (= "C5-UNRESOLVED" (:id (ex-data (:error changed)))))
            (is (nil? (:value changed)))
            (is (every? #(and (:same-parent? %) (:invocation-current? %)
                             (:proof-transaction-absent? %) (:observed-roots-stable? %))
                        (filter #(= :macro-enter (:event %)) events)))
            (is (:alias-restored? result)))))
      (finally
        ((:restore! alias))
        (is ((:restored? alias)))
        (is (every? (fn [[var value]] (identical? value @var)) before-roots))))))

(deftest actual-macro-trace-15-permutation-before-second-id
  (check-trace-order! 15 :permutation))
(deftest actual-macro-trace-15-insertion-before-second-id
  (check-trace-order! 15 :insertion))
(deftest actual-macro-trace-15-deletion-before-second-id
  (check-trace-order! 15 :deletion))
(deftest actual-macro-trace-15-duplicate-before-second-id
  (check-trace-order! 15 :duplicate))
(deftest actual-macro-trace-16-permutation-before-second-id
  (check-trace-order! 16 :permutation))
(deftest actual-macro-trace-16-insertion-before-second-id
  (check-trace-order! 16 :insertion))
(deftest actual-macro-trace-16-deletion-before-second-id
  (check-trace-order! 16 :deletion))
(deftest actual-macro-trace-16-duplicate-before-second-id
  (check-trace-order! 16 :duplicate))
(deftest actual-macro-trace-17-permutation-before-second-id
  (check-trace-order! 17 :permutation))
(deftest actual-macro-trace-17-insertion-before-second-id
  (check-trace-order! 17 :insertion))
(deftest actual-macro-trace-17-deletion-before-second-id
  (check-trace-order! 17 :deletion))
(deftest actual-macro-trace-17-duplicate-before-second-id
  (check-trace-order! 17 :duplicate))
