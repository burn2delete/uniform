(ns gravity.types.private-map-cycle-test
  "Compiler-free source models for the private Map entry-cycle premise.
  Native/source/type admission and actual compiler composition remain unrun."
  (:require [clojure.test :refer [deftest is]]))

(def owners
  [{:prefix "sh08" :path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"}
   {:prefix "body" :path "bootstrap/gravity/src/gravity/effects.gravity"}
   {:prefix "p15-s23" :path "bootstrap/gravity/p15_s23/emitter.gravity"}])

(defn definitions [path]
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. (slurp path)))]
      (loop [out {}]
        (let [f (read {:eof ::end} r)]
          (if (= ::end f) out
            (recur (if (and (seq? f) (= 'defn (first f))) (assoc out (second f) f) out))))))))

(defn model [{:keys [prefix path]}]
  (let [space (create-ns (symbol (str "gravity.types.private-map-cycle-model-" prefix)))
        defs (definitions path)
        named #(symbol (str prefix "-" %))]
    (binding [*ns* space]
      (refer 'clojure.core)
      (eval (get defs (named "pmk-acyclic")))
      (when-not (= prefix "sh08")
        ;; These are explicit admission seams for already-validated native
        ;; source/type inputs. They do not execute native or compiler admission.
        (intern space (named "sse-ids?")
                (fn [xs n] (and (vector? xs) (= n (count xs)) (every? keyword? xs))))
        (intern space (named "mkeys-ids?")
                (fn [node call _ _ module]
                  (and (= :call (:node-id node)) (= :call (:core-node-id call))
                       (= true (:permission module)))))
        (doseq [suffix ["pmk-constraint" "pmk-call-fact"]]
          (eval (get defs (named suffix))))
        (intern space (named "pmk-header") (fn [& _] (throw (ex-info "Header seam not installed" {}))))
        (doseq [suffix ["pmk-row-census" "pmk-fact-census"]]
          (eval (get defs (named suffix))))))
    {:prefix prefix :space space :defs defs}))

(def models (delay (mapv model owners)))
(defn invoke [m suffix & args]
  (apply (var-get (ns-resolve (:space m) (symbol (str (:prefix m) "-" suffix)))) args))

(defn edge [a b] {:caller-function-syntax-id a :callee-function-syntax-id b})
(defn root-cycle? [edges root]
  ;; Independent path search: an edge back to root is the forbidden premise.
  (loop [pending [root] seen #{}]
    (if-let [at (peek pending)]
      (let [tail (pop pending) children (keep #(when (= at (:caller-function-syntax-id %))
                                                (:callee-function-syntax-id %)) edges)]
        (if (some #{root} children) true
          (recur (into tail (remove seen children)) (conj seen at))))
      false)))

(deftest exact-graph-premise-agrees-with-independent-path-search
  (let [vertices [:a :b :c] possible (vec (for [a vertices b vertices] (edge a b)))]
    (doseq [mask (range 512) root vertices
            :let [edges (vec (keep-indexed #(when (bit-test mask %1) %2) possible))
                  expected (if (root-cycle? edges root) :rejected :accepted)]
            m @models]
      (is (= expected (:status (invoke m "pmk-acyclic" {:call-edges edges} root)))))))

(deftest long-late-cycle-shared-path-and-inert-fields
  (let [vertices (mapv #(keyword (str "f" %)) (range 140))
        chain (mapv edge (butlast vertices) (rest vertices))
        calls (atom 0)
        foreign (proxy [Object] []
                  (equals [_] (swap! calls inc) (throw (ex-info "opaque equality" {})))
                  (hashCode [] (swap! calls inc) (throw (ex-info "opaque hash" {})))
                  (toString [] (swap! calls inc) (throw (ex-info "opaque rendering" {}))))]
    (doseq [m @models]
      (is (= {:status :accepted}
             (invoke m "pmk-acyclic" {:call-edges (vec (reverse chain))} (first vertices))))
      (is (= {:status :rejected :reason :private-map-keys-entry-domain-required}
             (invoke m "pmk-acyclic" {:call-edges (conj chain (edge (last vertices) (first vertices)))} (first vertices))))
      (is (= {:status :accepted}
             (invoke m "pmk-acyclic" {:call-edges [(edge :a :b) (edge :a :c) (edge :b :d) (edge :c :d)
                                                               (edge :x :x) (edge :a nil)]
                                      :unobserved foreign} :a))))
    (is (zero? @calls))))

(defn fixture [m]
  (let [core {:module {:profile :meta :target :jvm :permission true}
              :call-edges [(edge :private :leaf)]}
        node {:node-id :call} call {:core-node-id :call :operator-node-id :operator}
        descriptor {:kind :gravity.type/applied-private-map-keys
                    :call-core-node-id :call :result-type {:kind :gravity.type/list :member-type :keyword}
                    :source {:syntax-id :syntax :origin-chain [:original]}
                    :source-edge {} :argument-types [:gravity.type/map]
                    :direct-effects [:memory/allocate] :profile :meta :target :jvm
                    :key-domain-proof {:coverage :all-resolved-private-entries
                                       :entries [{:call-core-node-id :entry :argument-type :keyword-map}]
                                       :source-call-edges (:call-edges core)}}
        header (fn [actual-core & _]
                 (let [graph (invoke m "pmk-acyclic" actual-core :private)]
                   (if (= :accepted (:status graph))
                     {:status :accepted :work-count 0
                      :descriptor (assoc-in descriptor [:key-domain-proof :source-call-edges] (:call-edges actual-core))}
                     (assoc graph :work-count 0))))]
    (alter-var-root (ns-resolve (:space m) (symbol (str (:prefix m) "-pmk-header"))) (constantly header))
    {:core core :node node :call call :descriptor descriptor
     :nodes {:call node} :calls {:call call} :edges {:call {}}
     :types {:operator descriptor} :row (invoke m "pmk-constraint" descriptor)
     :fact (invoke m "pmk-call-fact" descriptor (:module core))}))

(defn consume [m {:keys [core descriptor nodes calls edges types row fact]}]
  ;; Force a fresh header reconstruction for :call, instead of the internal
  ;; target shortcut whose descriptor is supplied by its enclosing consumer.
  (let [rows (invoke m "pmk-row-census" core [row] nodes {} types {} calls edges :other descriptor 65536)]
    (if (= :accepted (:status rows))
      (invoke m "pmk-fact-census" [fact] (:module core) (:descriptors rows) :call)
      rows)))

(deftest fresh-row-fact-reconstruction-rejects-stale-and-coherent-forgeries
  (doseq [m (rest @models)
          :let [f (fixture m) d (:descriptor f)]]
    (is (= :accepted (:status (consume m f))))
    (doseq [changed [(assoc-in f [:row :solution :key-domain-proof :coverage] :partial)
                    (assoc-in f [:row :solution :construction-work] nil)
                    (assoc-in f [:row :solution :construction-work] 0)
                    (assoc-in f [:row :solution :construction-work] 1)
                    (assoc-in f [:fact :profile] :hosted)
                    (assoc-in f [:core :module :permission] false)
                    (update-in f [:core :call-edges] conj (edge :leaf :private))
                    (update-in f [:core :call-edges] conj (edge :private :new-entry))]]
      (is (= :rejected (:status (consume m changed)))))
    (doseq [forged [(assoc-in d [:key-domain-proof :entries] [])
                    (assoc-in d [:key-domain-proof :coverage] :partial)
                    (assoc d :source {:syntax-id :forged :origin-chain [:forged]})
                    (assoc d :result-type :gravity.type/never)]]
      (is (= :rejected
             (:status (consume m (assoc f :descriptor forged :types {:operator forged}
                                         :row (invoke m "pmk-constraint" forged)
                                         :fact (invoke m "pmk-call-fact" forged (get-in f [:core :module]))))))))))
