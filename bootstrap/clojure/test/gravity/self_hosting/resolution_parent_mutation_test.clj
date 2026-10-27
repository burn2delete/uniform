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
        original (b/sh05-macro-artifact-from-c3-artifact source syntax invocation)
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
      (let [public (b/sh06-resolution-artifact-verification
                     (:value positive) :gravity.bootstrap/legacy invocation)]
        (reset! public-report public)
        (is (= :passed (:status public)))
        (when-not (= :passed (:status public))
          (throw (ex-info "Independent fresh positive verification failed"
                          {:phase :independent-positive :failed-checks (:failed-checks public)}))))
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
