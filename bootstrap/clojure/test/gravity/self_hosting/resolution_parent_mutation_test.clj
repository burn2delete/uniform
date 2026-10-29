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
                  (is (= ['sh05-verify-macro-template 'sh05-verify-macro-resolved]
                         (mapv :requested-callee entered)))
                  (is (= [:macro-dispatch-enter :macro-dispatch-return
                          :macro-dispatch-enter :macro-dispatch-return]
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
