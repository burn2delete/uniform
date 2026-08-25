(ns gravity.self-hosting.sh01-stage2-runtime-execution-profile
  "Bounded, non-authoritative Stage2 runtime execution attribution."
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(def ^:private accepted-source-path "bootstrap/clojure/fixtures/accepted/core-app.gravity")
(def ^:private maximum-iterations 1)
(def ^:private maximum-function-rows 128)
(def ^:private maximum-instruction-ops 32)
(def ^:private maximum-plan-identities 8)
(def ^:private maximum-call-edge-rows 256)
(def ^:private function-sample-mask 8191)
(def ^:private scopes [:fresh-plan-emission :emitted-plan-execution])
(def ^:private sources [:authenticated-envelope :syntax :reader :plan-emitter
                        :stage2-runtime-artifact :stage2-emitted-plan :other])
(def ^:private instruction-ops
  [:literal :quote :local :vector-literal :set-literal :map-literal :println :do
   :if :let :loop :recur :builtin-call :function-call :other-instruction])
(def ^:private source-count (count sources))
(def ^:private scope-count (count scopes))
(def ^:private instruction-op-count (count instruction-ops))
(def ^:private other-function-slot (dec maximum-function-rows))
(def ^:private maximum-function-depth 128)
(def ^:private targeted-cost-targets
  #{:authenticated-envelope-digest-cluster :syntax-c3-lowercase-hex?})
(def ^:private maximum-target-sample-stride 4096)

;; `with-redefs` changes root Vars. Only this explicit profiler uses this lock.
(def ^:private profile-lock (Object.))
(deftype ^:private ExecutionContext [^longs frames depth])

(def ^:private thread-allocated-bytes-reader
  (delay (try
           (let [bean (java.lang.management.ManagementFactory/getThreadMXBean)]
             (when (and (instance? com.sun.management.ThreadMXBean bean)
                        (.isThreadAllocatedMemorySupported ^com.sun.management.ThreadMXBean bean)
                        (.isThreadAllocatedMemoryEnabled ^com.sun.management.ThreadMXBean bean))
               (fn [] (.getThreadAllocatedBytes ^com.sun.management.ThreadMXBean bean
                                                 (.getId (Thread/currentThread))))))
           (catch Exception _ nil))))
(defn- thread-allocated-bytes [] (when-let [f @thread-allocated-bytes-reader] (try (f) (catch Exception _ nil))))
(defn- source-id [source] (.indexOf ^java.util.List sources source))
(defn- scope-id [scope] (.indexOf ^java.util.List scopes scope))
(defn- op-id [op] (let [id (.indexOf ^java.util.List instruction-ops op)] (if (neg? id) (dec instruction-op-count) id)))

(defn- path-source [path]
  (let [path (str/replace path "\\" "/")]
    (cond (str/ends-with? path "/gravity/compiler/authenticated_envelope.gravity") :authenticated-envelope
          (str/ends-with? path "/gravity/bootstrap/syntax.gravity") :syntax
          (str/ends-with? path "/gravity/bootstrap/reader.gravity") :reader
          (str/ends-with? path "/gravity/p15_s23/emitter.gravity") :plan-emitter
          :else :other)))
(defn- function-offset [scope source function]
  (+ (* scope source-count maximum-function-rows)
     (* source maximum-function-rows) function))
(defn- instruction-offset [scope source function operation]
  (+ (* instruction-op-count (function-offset scope source function)) operation))
(defn- edge-offset [scope caller-source caller-function callee-source callee-function]
  (+ (* scope source-count maximum-function-rows source-count maximum-function-rows)
     (* caller-source maximum-function-rows source-count maximum-function-rows)
     (* caller-function source-count maximum-function-rows)
     (* callee-source maximum-function-rows) callee-function))
(defn- overflow! [state kind]
  (.set ^java.util.concurrent.atomic.AtomicBoolean
        (if (= kind :sample) (:sample-overflow state) (:counter-overflow state)) true))
(defn- saturated-increment! [state ^longs values offset]
  (let [current (aget values offset)]
    (if (= Long/MAX_VALUE current)
      (do (overflow! state :counter) current)
      (let [next (inc current)]
        (aset-long values offset next)
        next))))
(defn- saturated-sample-increment! [state ^longs values offset]
  (let [current (aget values offset)]
    (if (= Long/MAX_VALUE current)
      (do (overflow! state :sample) current)
      (let [next (inc current)]
        (aset-long values offset next)
        next))))
(defn- saturated-add! [state ^longs values offset amount]
  (let [current (aget values offset)]
    (if (or (neg? amount) (> amount (- Long/MAX_VALUE current)))
      (do (overflow! state :sample) (aset-long values offset Long/MAX_VALUE) Long/MAX_VALUE)
      (let [next (+ current amount)]
        (aset-long values offset next)
        next))))
(defn- saturated-atomic-increment! [state ^java.util.concurrent.atomic.AtomicLong value]
  (loop [current (.get value)]
    (if (= Long/MAX_VALUE current)
      (do (overflow! state :counter) current)
      (let [next (inc current)]
        (if (.compareAndSet value current next) next (recur (.get value)))))))

(defn- new-state
  ([] (new-state {}))
  ([{:keys [targeted-cost]}]
   (let [targets (vec (or (:targets targeted-cost) []))
         stride (long (or (:sample-stride targeted-cost) 1))]
     (when (some (complement targeted-cost-targets) targets)
       (throw (ex-info "Stage2 targeted profile target is unsupported"
                       {:id "SH01-STAGE2-TARGETED-PROFILE-TARGET"
                        :targets targets})))
     (when (or (empty? targets) (> stride maximum-target-sample-stride) (not (pos? stride)))
       (when targeted-cost
         (throw (ex-info "Stage2 targeted profile sample stride is out of bounds"
                         {:id "SH01-STAGE2-TARGETED-PROFILE-STRIDE"
                          :sample-stride stride :maximum maximum-target-sample-stride}))))
     (let [ids (object-array source-count) labels (object-array source-count)]
    (dotimes [source source-count]
      (let [m (java.util.HashMap.) row-labels (object-array maximum-function-rows)]
        (.put m "<entrypoint>" 0) (aset row-labels 0 "<entrypoint>")
        (aset row-labels other-function-slot "<other-functions>")
        (aset ids source m) (aset labels source row-labels)))
       {:plan-sources (java.util.IdentityHashMap.) :plan-source-registry (java.util.HashMap.)
     :source-identities (object-array source-count) :function-ids ids :function-labels labels
     :next-function-index (int-array source-count 1)
     :function-calls (long-array (* scope-count source-count maximum-function-rows))
     :instruction-calls (long-array (* scope-count source-count maximum-function-rows instruction-op-count))
     :call-edges (long-array (* scope-count source-count maximum-function-rows source-count maximum-function-rows))
     :sample-count (long-array (* scope-count source-count maximum-function-rows))
     :sample-elapsed-ns (long-array (* scope-count source-count maximum-function-rows))
     :sample-allocated-bytes (long-array (* scope-count source-count maximum-function-rows))
     :counter-overflow (java.util.concurrent.atomic.AtomicBoolean. false)
     :sample-overflow (java.util.concurrent.atomic.AtomicBoolean. false)
     :scope (int-array 1) :owner-thread (Thread/currentThread)
     :thread-context (proxy [ThreadLocal] [] (initialValue [] (ExecutionContext. (long-array 256) (volatile! 0))))
     :off-owner-events (java.util.concurrent.atomic.AtomicLong. 0)
     :targeted-cost-targets (when targeted-cost targets) :targeted-cost-stride stride
     :targeted-calls (long-array 2) :targeted-sample-count (long-array 2)
        :targeted-elapsed-ns (long-array 2) :targeted-allocated-bytes (long-array 2)}))))

(defn- register-plan! [state plan path]
  (let [source (source-id (path-source path))]
    (.put ^java.util.IdentityHashMap (:plan-sources state) plan source)
    (when (< (.size ^java.util.HashMap (:plan-source-registry state)) maximum-plan-identities)
      (.put ^java.util.HashMap (:plan-source-registry state) (:plan-id plan)
            {:source (nth sources source) :source-path path :source-hash (get-in plan [:source :sha256])}))))
(defn- runtime-source [state runtime plan]
  (or (.get ^java.util.IdentityHashMap (:plan-sources state) plan)
      (cond (= :gravity/stage2-hosted-core-compiled-plan (:kind plan)) (source-id :stage2-emitted-plan)
            (= (:plan-id plan) bootstrap/sh03-reader-expected-plan-semantic-hash) (source-id :reader)
            (:runtime-artifact-plan runtime) (source-id :stage2-runtime-artifact)
            :else (source-id :other))))
(defn- function-index! [state source callee]
  (let [^java.util.HashMap ids (aget ^objects (:function-ids state) source) old (.get ids callee)]
    (if (some? old) (int old)
        (let [next (aget ^ints (:next-function-index state) source) id (if (< next other-function-slot) next other-function-slot)]
          (when (< next other-function-slot)
            (.put ids callee id) (aset-int ^ints (:next-function-index state) source (inc next))
            (aset ^objects (aget ^objects (:function-labels state) source) id (str callee))) id))))
(defn- record-source! [state source runtime plan]
  (let [^objects identities (:source-identities state)]
    (when-not (aget identities source)
      (aset identities source {:plan-kind (:kind plan) :plan-id (:plan-id plan)
                               :source-path (get-in plan [:source :path])
                               :source-hash (get-in plan [:source :sha256])
                               :runtime-artifact-source-path (:runtime-artifact-source-path runtime)}))))
(defn- owner? [state] (identical? (:owner-thread state) (Thread/currentThread)))
(defn- current-frame [state]
  (let [^ExecutionContext context (.get ^ThreadLocal (:thread-context state)) depth @(.-depth context)]
    (when (pos? depth) (let [offset (* 2 (dec depth)) frames (.-frames context)]
                         [(aget ^longs frames offset) (aget ^longs frames (inc offset))]))))

(defn- targeted-target-index [targets source label]
  (cond
    (and (= source (source-id :authenticated-envelope))
         (some #{:authenticated-envelope-digest-cluster} targets)
         (str/includes? (str/lower-case label) "digest")) 0
    (and (= source (source-id :syntax))
         (= label "c3-lowercase-hex?")
         (some #{:syntax-c3-lowercase-hex?} targets)) 1
    :else -1))

(defn- record-target-call! [state target-index]
  (when (>= target-index 0)
    (saturated-increment! state (:targeted-calls state) target-index)))

(defn- run-function [state runtime plan callee operation]
  (if-not (owner? state)
    (do (saturated-atomic-increment! state (:off-owner-events state)) (operation))
    (let [scope (aget ^ints (:scope state) 0) source (int (runtime-source state runtime plan))
          function (function-index! state source callee) offset (function-offset scope source function)
          ^longs calls (:function-calls state) call-count (saturated-increment! state calls offset)
          label (str callee)
          target-index (targeted-target-index (:targeted-cost-targets state) source label)
          target-call-count (when (>= target-index 0)
                              (record-target-call! state target-index))
          target-sampled? (and (>= target-index 0)
                               (zero? (mod target-call-count (:targeted-cost-stride state))))
          sampled? (and (nil? (:targeted-cost-targets state))
                        (zero? (bit-and call-count function-sample-mask)))
          measured? (or sampled? target-sampled?)
          before (when measured? (thread-allocated-bytes))
          started (when measured? (System/nanoTime))
          ^ExecutionContext context (.get ^ThreadLocal (:thread-context state)) depth @(.-depth context) frames (.-frames context)]
      (record-source! state source runtime plan)
      (when (pos? depth)
        (let [caller-offset (* 2 (dec depth)) caller-source (aget ^longs frames caller-offset)
              caller-function (aget ^longs frames (inc caller-offset))
              edge (edge-offset scope caller-source caller-function source function)]
          (saturated-increment! state (:call-edges state) edge)))
      (when (>= depth maximum-function-depth)
        (throw (ex-info "Stage2 runtime profile function depth exceeded"
                        {:id "SH01-STAGE2-RUNTIME-PROFILE-DEPTH"
                         :maximum-function-depth maximum-function-depth
                         :observed-function-depth (inc depth)})))
      (aset-long ^longs frames (* 2 depth) source)
      (aset-long ^longs frames (inc (* 2 depth)) function)
      (vreset! (.-depth context) (inc depth))
      (try (operation)
           (finally
             (vreset! (.-depth context) depth)
             (when sampled?
               (saturated-sample-increment! state (:sample-count state) offset)
               (saturated-add! state (:sample-elapsed-ns state) offset (- (System/nanoTime) started)))
             (when target-sampled?
               (saturated-sample-increment! state (:targeted-sample-count state) target-index)
               (saturated-add! state (:targeted-elapsed-ns state) target-index (- (System/nanoTime) started)))
             (when (and measured? (some? before))
               (when-let [after (thread-allocated-bytes)]
                 (when sampled? (saturated-add! state (:sample-allocated-bytes state) offset (- after before)))
                 (when target-sampled? (saturated-add! state (:targeted-allocated-bytes state) target-index (- after before))))))))))
(defn- run-instruction [state runtime plan instruction operation]
  (if-not (owner? state)
    (do (saturated-atomic-increment! state (:off-owner-events state)) (operation))
    (let [scope (aget ^ints (:scope state) 0) frame (current-frame state)
          source (int (or (first frame) (runtime-source state runtime plan))) function (int (or (second frame) 0))
          offset (instruction-offset scope source function (op-id (:op instruction)))]
      (record-source! state source runtime plan)
      (saturated-increment! state (:instruction-calls state) offset)
      (operation))))

(defn- function-rows [state]
  (vec (for [scope (range scope-count) source (range source-count) function (range maximum-function-rows)
             :let [label (aget ^objects (aget ^objects (:function-labels state) source) function)
                   n (aget ^longs (:function-calls state) (function-offset scope source function))]
             :when (and label (pos? n))]
         {:scope (nth scopes scope) :source (nth sources source) :function label :call-count n})))
(defn- instruction-rows [state]
  (vec (for [scope (range scope-count) source (range source-count) function (range maximum-function-rows) operation (range instruction-op-count)
             :let [label (aget ^objects (aget ^objects (:function-labels state) source) function)
                   n (aget ^longs (:instruction-calls state) (instruction-offset scope source function operation))]
             :when (and label (pos? n))]
         {:scope (nth scopes scope) :source (nth sources source) :function label :instruction-op (nth instruction-ops operation) :call-count n})))
(defn- sample-rows [state]
  (vec (for [scope (range scope-count) source (range source-count) function (range maximum-function-rows)
             :let [label (aget ^objects (aget ^objects (:function-labels state) source) function)
                   offset (function-offset scope source function) n (aget ^longs (:sample-count state) offset)]
             :when (and label (pos? n))]
         {:scope (nth scopes scope) :source (nth sources source) :function label :sample-count n
          :inclusive-elapsed-ns (aget ^longs (:sample-elapsed-ns state) offset)
         :inclusive-allocated-bytes (aget ^longs (:sample-allocated-bytes state) offset)})))
(defn- targeted-cost-rows [state]
  (mapv (fn [index target]
          {:target target :call-count (aget ^longs (:targeted-calls state) index)
           :sample-count (aget ^longs (:targeted-sample-count state) index)
           :inclusive-elapsed-ns (aget ^longs (:targeted-elapsed-ns state) index)
           :inclusive-allocated-bytes (aget ^longs (:targeted-allocated-bytes state) index)})
        (range 2) [:authenticated-envelope-digest-cluster :syntax-c3-lowercase-hex?]))
(defn- targeted-cost-ranking? [state rows]
  (and (= 2 (count (:targeted-cost-targets state)))
       (every? #(pos? (:call-count %)) rows)
       (not (.get ^java.util.concurrent.atomic.AtomicBoolean (:counter-overflow state)))
       (not (.get ^java.util.concurrent.atomic.AtomicBoolean (:sample-overflow state)))))
(declare receipt-sum)
(defn- call-edge-rows [state]
  (let [rows (for [scope (range scope-count) caller-source (range source-count) caller-function (range maximum-function-rows)
                   callee-source (range source-count) callee-function (range maximum-function-rows)
                   :let [caller (aget ^objects (aget ^objects (:function-labels state) caller-source) caller-function)
                         callee (aget ^objects (aget ^objects (:function-labels state) callee-source) callee-function)
                         n (aget ^longs (:call-edges state) (edge-offset scope caller-source caller-function callee-source callee-function))]
                   :when (and caller callee (pos? n))]
               {:scope (nth scopes scope) :caller-source (nth sources caller-source) :caller-function caller
                :callee-source (nth sources callee-source) :callee-function callee :call-count n})
        named (vec (take (dec maximum-call-edge-rows) rows))
        remainder (- (:total (receipt-sum (:call-edges state)))
                     (:total (receipt-sum (map :call-count named))))]
    (cond-> named
      (pos? remainder)
      (conj {:scope :all :caller-source :other :caller-function "<other-call-edges>"
             :callee-source :other :callee-function "<other-call-edges>" :call-count remainder}))))
(defn- receipt-sum [values]
  (reduce (fn [{:keys [total overflow?]} value]
            (if (> value (- Long/MAX_VALUE total))
              {:total Long/MAX_VALUE :overflow? true}
              {:total (+ total value) :overflow? overflow?}))
          {:total 0 :overflow? false} values))
(defn- row-sum-coverage [state rows counters]
  (let [row-total (receipt-sum (map :call-count rows))
        counter-total (receipt-sum counters)
        overflow? (or (.get ^java.util.concurrent.atomic.AtomicBoolean (:counter-overflow state))
                      (:overflow? row-total) (:overflow? counter-total))]
    {:row-call-count (:total row-total) :counter-call-count (:total counter-total)
     :overflow? overflow?
     :complete? (and (not overflow?) (= (:total row-total) (:total counter-total)))}))

(defn- compiler-emitter [] (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! accepted-source-path :jvm)))
(defn- emitted-plan [] (bootstrap/p15-s23-stage2-plan-emitter-compile-source (compiler-emitter) accepted-source-path (slurp accepted-source-path)))
(defn- execute-emitted-plan [plan] (bootstrap/p15-s23-stage2-runtime-execute-plan {:engine :gravity/sh01-stage2-runtime-execution-profile} plan))
(defn- stage0-record [plan]
  (let [result (atom nil)
        stdout (with-out-str
                 (reset! result
                         (bootstrap/execute-stage0-instructions
                          plan {} (get-in plan [:functions (:entrypoint plan) :instructions]))))]
    {:stdout stdout :entrypoint-result @result
     :instruction-summary (:instruction-summary plan)
     :effect-summary (:effect-summary plan)}))

(defn- run-once [emit-plan execute-plan initialize-state targeted-cost]
  (locking profile-lock
    (let [state (new-state {:targeted-cost targeted-cost}) _ (when initialize-state (initialize-state state)) original-function bootstrap/p15-s23-stage2-runtime-execute-function original-instruction bootstrap/p15-s23-stage2-runtime-execute-instruction original-artifact bootstrap/p15-s23-stage2-runtime-artifact-invoke original-compiler bootstrap/p15-s23-stage2-compiler-artifact-plan started (System/nanoTime)
          [plan runtime-record]
          (with-redefs [bootstrap/p15-s23-stage2-compiler-artifact-plan (fn [emitter path text] (let [plan (original-compiler emitter path text)] (register-plan! state plan path) plan))
                        bootstrap/p15-s23-stage2-runtime-execute-function (fn [runtime plan callee args] (run-function state runtime plan callee #(original-function runtime plan callee args)))
                        bootstrap/p15-s23-stage2-runtime-execute-instruction (fn [runtime plan env instruction] (run-instruction state runtime plan instruction #(original-instruction runtime plan env instruction)))
                        bootstrap/p15-s23-stage2-runtime-artifact-invoke (fn [runtime function args] (when-let [plan (:runtime-artifact-plan runtime)] (register-plan! state plan (or (:runtime-artifact-source-path runtime) "<runtime-artifact>"))) (original-artifact runtime function args))]
            (let [plan (do (aset-int ^ints (:scope state) 0 (scope-id :fresh-plan-emission)) (emit-plan))
                  record (do (aset-int ^ints (:scope state) 0 (scope-id :emitted-plan-execution)) (execute-plan plan))] [plan record]))
          stage0-record (stage0-record plan)
          equivalent? (= stage0-record
                         (select-keys runtime-record [:stdout :entrypoint-result
                                                      :instruction-summary :effect-summary]))]
      (when-not equivalent?
        (throw (ex-info "Stage2 runtime profile observed different execution"
                        {:id "SH01-STAGE2-RUNTIME-PROFILE-EQUIVALENCE"
                         :stage0-record stage0-record
                         :stage2-record (select-keys runtime-record
                                                      [:stdout :entrypoint-result
                                                       :instruction-summary :effect-summary])})))
      (let [functions (function-rows state) instructions (instruction-rows state) edges (call-edge-rows state)]
       {:semantic-receipt {:plan-id (:plan-id plan) :instruction-summary (:instruction-summary plan)
                          :function-order (vec (keys (:functions plan))) :stage0-output (:stdout stage0-record)
                          :stage2-runtime-output (:stdout runtime-record) :stage2-runtime-result (:entrypoint-result runtime-record)}
       :function-rows functions :instruction-rows instructions :call-edge-rows edges :sampled-function-cost-rows (sample-rows state)
       :targeted-cost-rows (targeted-cost-rows state)
       :targeted-cost-overflow? (.get ^java.util.concurrent.atomic.AtomicBoolean (:sample-overflow state))
       :targeted-cost-ranking? (targeted-cost-ranking? state (targeted-cost-rows state))
       :row-sum-coverage {:function (row-sum-coverage state functions (:function-calls state))
                          :instruction (row-sum-coverage state instructions (:instruction-calls state))
                          :call-edge (row-sum-coverage state edges (:call-edges state))}
       :source-identities (into (sorted-map) (map-indexed (fn [id source] [source (aget ^objects (:source-identities state) id)]) sources))
       :plan-source-registry (into (sorted-map) (:plan-source-registry state))
       :counter-overflow? (.get ^java.util.concurrent.atomic.AtomicBoolean (:counter-overflow state))
       :sample-overflow? (.get ^java.util.concurrent.atomic.AtomicBoolean (:sample-overflow state))
       :off-owner-thread-event-count (.get ^java.util.concurrent.atomic.AtomicLong (:off-owner-events state))
       :elapsed-ns (- (System/nanoTime) started)}))))

(defn run-profile
  ([] (run-profile {}))
  ([{:keys [iterations emit-plan execute-plan initialize-state targeted-cost] :or {iterations 1 emit-plan emitted-plan execute-plan execute-emitted-plan}}]
   (when-not (= maximum-iterations iterations) (throw (ex-info "Stage2 runtime profile iteration count is out of bounds" {:id "SH01-STAGE2-RUNTIME-PROFILE-COUNT" :iterations iterations :maximum maximum-iterations})))
   {:artifact :gravity/sh01-stage2-runtime-execution-profile :authority :non-authoritative :authoritative? false :purpose :bounded-runtime-allocation-attribution :fresh-plan-emission? true :persistent-cache-authority? false
    :deterministic-accounting [:semantic-receipt :function-rows :instruction-rows :call-edge-rows :row-sum-coverage :source-identities :plan-source-registry :counter-overflow? :off-owner-thread-event-count]
    :host-variable-observations [:elapsed-ns :sampled-function-cost-rows :targeted-cost-rows :targeted-cost-ranking? :sample-overflow? :java-runtime-version :clojure-version]
    :bounds {:iterations maximum-iterations :maximum-function-rows maximum-function-rows :maximum-instruction-ops maximum-instruction-ops :maximum-call-edge-rows maximum-call-edge-rows :maximum-plan-identities maximum-plan-identities :function-sample-interval (inc function-sample-mask)}
    :concurrency {:same-jvm :serialized-by-private-lock :global-var-redefinition :explicit-profiler-only :off-owner-thread-events :excluded-from-attribution}
    :nonclaims [:performance-baseline :performance-improvement :allocation-bound :fresh-no-cache-verification :cost-ranking-without-control :integration :release :self-hosting :seed-retirement]
    :java-runtime-version (System/getProperty "java.runtime.version") :clojure-version (clojure-version) :sample (run-once emit-plan execute-plan initialize-state targeted-cost)}))
(defn parse-arguments [arguments] (if (empty? arguments) {:iterations 1} (throw (ex-info "Stage2 runtime profile takes no arguments" {:id "SH01-STAGE2-RUNTIME-PROFILE-USAGE" :arguments (vec arguments)}))))
(defn -main [& arguments] (println (pr-str (run-profile (parse-arguments arguments)))))
