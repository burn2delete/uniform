(ns gravity.build.reader-bridge
  "Fixed Stage2 declared-input bridge. Scope creation/form selection are private
  trusted Reader-host seams; neither is exposed as a Gravity builtin."
  (:require [gravity.build.declared-inputs :as inputs]))

(def ^:private ^:dynamic *scope* nil)
(def ^:private ^:dynamic *form* nil)
(defn- refuse! [reason]
  (throw (ex-info "Reader build scope refused" {:id "L15-CONTRACT" :reason reason})))
(defn- freeze [data]
  (#'inputs/freeze data (java.util.IdentityHashMap.)
                   #{clojure.lang.Keyword clojure.lang.Symbol String Long Boolean nil}))

(defn validate-call!
  "Compile-time legality for this fixed build operation, not grant issuance."
  [module arity]
  (let [module (freeze module)]
    (when-not (= 1 arity) (refuse! :read-arity))
    (when-not (and (= :meta (:profile module)) (= :jvm (:target module))
                   (contains? (:effects module) :build/read-file)
                   (contains? (:capabilities module) :fs/read))
      (refuse! :read-legality)))
  true)

(defn registered-scope? [scope]
  ;; Native Object identity only. Source fields never select this route.
  (boolean (and (= Object (class scope)) *scope*
                (identical? scope (:token *scope*)))))

(defn native-scope!
  "Guard before the existing module-scope keyword lookup. Does not admit it."
  [scope]
  (when-not (or (nil? scope) (= Object (class scope))
                (and (contains? #{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap}
                                (class scope))
                     (nil? (meta scope))
                     (every? #(and (= clojure.lang.Keyword (class %)) (nil? (meta %)))
                             (keys scope))))
    (refuse! :reader-native-scope))
  scope)

(defn- live! [scope runtime plan]
  (when-not (and (registered-scope? scope) @(:active *scope*)
                 (identical? (Thread/currentThread) (:thread *scope*))
                 (identical? runtime (:runtime *scope*))
                 (identical? plan (:plan *scope*)))
    (refuse! :reader-scope-identity))
  ;; Fresh native checks precede equality/lookups on caller carriers.
  (let [runtime-copy (freeze runtime) plan-copy (freeze plan)]
    (when-not (and (= runtime-copy (:runtime-data *scope*))
                   (= plan-copy (:plan-data *scope*)))
      (refuse! :reader-scope-stale)))
  (inputs/preflight! (:capability *scope*) [])
  *scope*)

(defn assert-function-entry! [runtime plan callee _args scope]
  (freeze callee)
  (let [context (live! scope runtime plan)]
    (when-not (contains? (get (:plan-data context) :functions) callee)
      (refuse! :reader-function-entry)))
  true)

(defn- instruction-member? [root instruction]
  ;; root has already passed complete native guards.
  (loop [pending [root]]
    (when (seq pending)
      (let [item (peek pending) tail (pop pending)]
        (if (identical? item instruction) true
          (recur (into tail (cond (map? item) (vals item)
                                  (coll? item) item :else []))))))))

(defn- call-with-scope
  "Trusted host after authentic Reader plan/form derivation, inside issuer.
  rows bind ORIGINAL handler instruction objects to fresh original requests.
  Host callback is control flow, never a caller-supplied provider."
  [capability runtime plan rows run]
  (let [runtime-data (freeze runtime) plan-data (freeze plan)
        _ (freeze rows)
        requests (mapv :request rows)
        handler 'gravity.reader/read-input
        instructions (get-in plan [:functions 'read-input :instructions])]
    (when-not (and (map? runtime-data) (map? plan-data) (vector? rows) (fn? run)
                   (= :gravity/stage2-compiler-artifact-plan (:kind plan-data))
                   (true? (:compiler-artifact-plan? plan-data))
                   (= 'gravity.reader (get-in plan-data [:module :module])))
      (refuse! :reader-plan))
    (validate-call! (:module plan-data) 1)
    (inputs/preflight! capability requests)
    (when-not (= (count requests) (count (set requests)))
      (refuse! :duplicate-reader-form))
    (doseq [{:keys [instruction request] :as row} rows]
      (when-not (and (= #{:instruction :request} (set (keys row)))
                     (= handler (:handler request))
                     (= :builtin-call (:op instruction))
                     (= 'build-read-declared-input (:function instruction))
                     (= 1 (count (:args instruction)))
                     (not (contains? instruction :compiler-internal-caller))
                     (instruction-member? instructions instruction))
        (refuse! :reader-handler-occurrence)))
    (let [context {:token (Object.) :active (atom true) :thread (Thread/currentThread)
                   :capability capability :runtime runtime :plan plan
                   :runtime-data runtime-data :plan-data plan-data
                   :rows (mapv (fn [row] {:instruction (:instruction row)
                                         :request (freeze (:request row))}) rows)
                   :results (atom [])}]
      (binding [*scope* context]
        (try (run (:token context))
             (finally (reset! (:active context) false)
                      (reset! (:results context) [])))))))

(defn- call-with-form
  "Host-only phase/form selection; never inferred from source flags."
  [scope request mode run]
  (let [request (freeze request)]
    (when-not (and (registered-scope? scope) @(:active *scope*)
                   (contains? #{:construct :verify} mode) (fn? run)
                   (some #(= request (:request %)) (:rows *scope*)))
      (refuse! :reader-current-form))
    (binding [*form* {:request request :mode mode}]
      (run))))

(defn read!
  "Only build-read-declared-input dispatch. Original receipt stays host-side."
  [runtime plan instruction args scope]
  (freeze instruction)
  (let [args (freeze args) context (live! scope runtime plan)
        request (:request *form*)]
    (when-not (and (vector? args) (= 1 (count args)) (= String (class (first args)))
                   *form* (= (first args) (:input request))
                   (some #(and (identical? instruction (:instruction %))
                                (= request (:request %))) (:rows context)))
      (refuse! :reader-current-form))
    (let [existing (some #(when (= request (:request %)) %) @(:results context))]
      (if (= :construct (:mode *form*))
        (do
          (when existing (refuse! :reader-duplicate-effect))
          (let [{:keys [receipt record]} (inputs/read-input! (:capability context) request)]
            (swap! (:results context) conj
                   {:request request :receipt receipt :record record})
            (get-in record [:result :text])))
        (do
          (when-not existing (refuse! :reader-original-result-missing))
          (:text (inputs/verify-original-result!
                  (:capability context) request (:receipt existing) (:record existing))))))))
