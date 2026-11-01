(ns gravity.self-hosting.macro-run-storage-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.macro-run-storage :as storage]))

(defn- exact-keys? [value expected] (= expected (set (keys value))))
(defn- original [runs]
  (every? (fn [run]
            (and
             (exact-keys? run #{:template-verification :raw-template-result :resolved-expansion
                               :digest-requests :resolved-digests :resolved-verification})
             (exact-keys? (:raw-template-result run)
                          #{:schema-version :status :artifact :expansion-template})
             (exact-keys? (:template-verification run) #{:rule :schema-version :status :artifact})
             (exact-keys? (:resolved-verification run) #{:rule :schema-version :status :artifact})))
          runs))
(defn- run []
  {:template-verification {:rule :template :schema-version 1 :status :passed :artifact :report}
   :resolved-verification {:rule :resolved :schema-version 1 :status :passed :artifact :report}
   :raw-template-result {:schema-version 1 :status :accepted :artifact :raw :expansion-template {}}
   :resolved-expansion {} :digest-requests [] :resolved-digests []})
(defn- caught [operation] (try (operation) (catch Throwable error error)))

(deftest native-shapes-match-original-with-consumed-mutations
  (let [value (run)]
    (doseq [runs [nil [] [value] (vec (repeat 65 value))
                  [(dissoc value :resolved-expansion)] [(assoc value :extra nil)]
                  [(assoc value :raw-template-result nil)]
                  [(update value :template-verification dissoc :rule)]
                  [(update value :resolved-verification assoc :extra true)]]]
      (is (= (original runs) (storage/exact? runs))))))

(deftest every-consumed-key-mutation-is-observed-fresh
  (doseq [path [[] [:raw-template-result] [:template-verification] [:resolved-verification]]]
    (let [value (run) shape (get-in value path value)]
      (doseq [key (keys shape)]
        (let [changed (if (empty? path) (dissoc value key) (update-in value path dissoc key))]
          (is (true? (storage/exact? [value])))
          (is (false? (storage/exact? [changed])))
          (is (= (original [changed]) (storage/exact? [changed]))))))))

(deftest ignored-values-and-metadata-are-not-observed
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! calls inc) (throw (ex-info "hash" {})))
                 (equals [_] (swap! calls inc) (throw (ex-info "equals" {})))
                 (toString [] (swap! calls inc) (throw (ex-info "print" {}))))
        runs (with-meta [(assoc (run) :resolved-expansion opaque)] {:opaque opaque})]
    (is (= true (original runs) (storage/exact? runs) (storage/exact? runs)))
    (is (zero? @calls))))

(deftest unsupported-carriers-and-nil-retain-original-results
  (doseq [runs [(list (run)) (lazy-seq [(run)]) (subvec [(run) (run)] 0 1)
                [(into (sorted-map) (run))] [(into {} (concat (run) (map #(vector % nil) (range 20))))]
                [nil]]]
    (is (= (original runs) (storage/exact? runs)))))

(deftest foreign-key-callback-and-identical-error-are-preserved
  (let [calls (atom 0) failure (ex-info "original key failure" {})
        key (proxy [Object] [] (hashCode [] (swap! calls inc) (throw failure)))
        runs [(array-map key nil)]]
    (is (identical? failure (caught #(original runs))))
    (let [original-count @calls]
      (reset! calls 0)
      (is (identical? failure (caught #(storage/exact? runs))))
      (is (= original-count @calls)))))

(deftest extra-keyword-does-not-hide-a-later-foreign-key-error
  (let [calls (atom 0) failure (ex-info "later key failure" {})
        key (proxy [Object] [] (hashCode [] (swap! calls inc) (throw failure)))
        runs [(array-map :extra nil key nil)]]
    (is (identical? failure (caught #(original runs))))
    (let [original-count @calls]
      (reset! calls 0)
      (is (identical? failure (caught #(storage/exact? runs))))
      (is (= original-count @calls)))))

(deftest lazy-failure-and-short-circuit-order-are-preserved
  (let [calls (atom []) failure (ex-info "later input" {})]
    (doseq [operation [original storage/exact?]]
      (reset! calls [])
      (let [runs (lazy-seq (cons {} (lazy-seq (swap! calls conj :later) (throw failure))))]
        (is (false? (operation runs)))
        (is (empty? @calls))))))

(deftest aliased-native-map-storage-is-always-read-fresh
  (let [backing (object-array (mapcat identity (run)))
        value (clojure.lang.PersistentArrayMap. backing) runs [value]]
    (is (= true (original runs) (storage/exact? runs)))
    (aset backing 0 :changed-key)
    (is (= false (original runs) (storage/exact? runs)))))

(deftest duplicate-native-array-keys-retain-set-semantics
  (let [value (run)
        backing (object-array (concat (mapcat identity value) [:resolved-expansion :duplicate]))
        duplicate (clojure.lang.PersistentArrayMap. backing)]
    (is (= true (original [duplicate]) (storage/exact? [duplicate])))))

(deftest malformed-native-backing-keeps-original-error-behavior
  (doseq [backing [[] [:template-verification]
                   [:template-verification {} :raw-template-result]
                   (conj (vec (mapcat identity (run))) :odd-key)]]
    (let [value (clojure.lang.PersistentArrayMap. (object-array backing))
          before (caught #(original [value])) after (caught #(storage/exact? [value]))]
      (if (instance? Throwable before)
        (do (is (= (class before) (class after)))
            (is (= (.getMessage ^Throwable before) (.getMessage ^Throwable after))))
        (is (= before after))))))

(deftest foreign-schema-keywords-never-fill-a-missing-slot
  (doseq [path [[] [:raw-template-result] [:template-verification] [:resolved-verification]]
          extra [:extra :foreign/status :template-verification :raw-template-result
                 :resolved-expansion :digest-requests :resolved-digests :resolved-verification
                 :schema-version :status :artifact :expansion-template :rule]]
    (let [value (run) changed (if (empty? path) (assoc value extra nil)
                                (assoc-in value (conj path extra) nil))]
      (is (= (original [changed]) (storage/exact? [changed]))))))

(defn- consumer-forms []
  (binding [*read-eval* false]
    (let [forms (read-string (str "(" (slurp "bootstrap/clojure/src/gravity/macro.clj") ")"))
          dependency (first (filter #(and (vector? %) (= 'gravity.macro-run-storage (first %)))
                                    (rest (last (first forms)))))
          verify (first (filter #(and (seq? %) (= 'defn (first %)) (= 'verify (second %))) forms))
          helper (first (filter #(= 'semantic-late-sh05-macro-artifact-verification*-expansion-replay (first %))
                                (second (nth verify 3))))]
      (when-not (and dependency helper)
        (throw (ex-info "Actual macro storage consumer is absent" {})))
      [(list 'require (list 'quote dependency))
       (apply list 'defn (first helper) (conj (second helper) 'operations) (drop 2 helper))])))

(deftest actual-expansion-stage-keeps-live-call-order
  ;; Execute the actual consumer form with explicitly modeled collaborators.
  ;; This tests adapter wiring, not an authenticated artifact or Gravity replay.
  (let [namespace (create-ns (gensym "macro-storage-stage-"))
        events (atom [])
        forms [(list 'ns 'example) (list 'defn 'answer [] 1)]
        stream (mapv #(hash-map :form %) forms)
        operations
        {'sh05-source-syntax-stream (fn [_] (swap! events conj :syntax) [{} {}])
         'parse-module (fn [& _] (swap! events conj :parse) {:profile :meta :target :jvm})
         'sh05-defn-form? (fn [form] (swap! events conj :defn) (= 'defn (first form)))
         'sh05-expanded-syntax-object (fn [_ form _] (swap! events conj :expand) {:form form})
         'sh05-package-trace (fn [& _] (swap! events conj :trace) {:trace :same})
         'sh05-macro-trace-id (fn [& _] (swap! events conj :trace-id) :trace)
         'sh05-expanded-syntax-stream-id (fn [& _] (swap! events conj :stream-id) :stream)
         'sh05-macro-version :version}
        stage-operations (assoc (into {} (for [[name value] operations :when (fn? value)]
                                          [(keyword (str name)) value]))
                                :input {:sh05-macro-version :version})]
    (try
      (binding [*ns* namespace]
        (clojure.core/refer 'clojure.core)
        (require '[clojure.set :as set])
        (is (nil? (get (ns-aliases namespace) 'macro-run-storage)))
        (doseq [form (consumer-forms)] (eval form))
        (is (= 'gravity.macro-run-storage
               (some-> (get (ns-aliases namespace) 'macro-run-storage) ns-name))))
      (let [stage (ns-resolve namespace 'semantic-late-sh05-macro-artifact-verification*-expansion-replay)
            expected [:syntax :parse :defn :expand :defn :expand :trace :trace-id :stream-id]]
        (doseq [runs [[(run)] [(dissoc (run) :resolved-expansion)]]]
          (let [state {:artifact {:expanded-defn-count 1 :expanded-syntax-stream stream
                                  :expanded-forms (subvec forms 1) :expanded-syntax-stream-id :stream
                                  :macro-expansion-trace [{:trace :same}] :macro-expansion-trace-id :trace}
                       :boundary {:expansion-runs runs}
                       :c3-artifact {:c2-reader-artifact {:parsed-semantic-values forms}}
                       :binding {} :source-path "model.gravity"
                       :template-passed? true :resolved-passed? true}]
            (dotimes [_ 2]
              (reset! events [])
              (let [result (stage state stage-operations)]
                (is (= (original runs) (:run-storage-exact? result)))
                (is (= expected @events)))))))
      (finally (remove-ns (ns-name namespace))))))

(deftest missing-consumer-require-is-a-load-error
  ;; The stage namespace owns the require. A primed test alias must not mask it.
  (let [namespace (create-ns (gensym "macro-storage-unprimed-"))]
    (try
      (binding [*ns* namespace]
        (clojure.core/refer 'clojure.core)
        (require '[clojure.set :as set])
        (is (nil? (get (ns-aliases namespace) 'macro-run-storage)))
        (binding [*read-eval* false]
            (let [[dependency consumer] (consumer-forms)
                  _ (is (= '(require '[gravity.macro-run-storage :as macro-run-storage]) dependency))
                  failure (caught #(eval consumer))
                  messages (map #(.getMessage ^Throwable %) (take-while some? (iterate ex-cause failure)))]
              (is (instance? clojure.lang.Compiler$CompilerException failure))
              (is (some #(and % (.contains ^String % "No such namespace: macro-run-storage")) messages)))))
      (finally (remove-ns (ns-name namespace))))))
