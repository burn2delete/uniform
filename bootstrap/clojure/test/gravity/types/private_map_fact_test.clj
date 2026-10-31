(ns gravity.types.private-map-fact-test
  "Compiler-free models of the actual PMK fact census and its caller.
  Header/type/native admission remains an explicit seam, not acceptance."
  (:require [clojure.test :refer [deftest is]]
            [gravity.types.private-map-cycle-test :as cycle]))

(defn ordinary-fact [id]
  {:call-core-node-id id :callee-function-syntax-id :ordinary-callee
   :fact-id-request {:domain :gravity/sh08-call-type-fact-v1}})

(def wide-facts
  (delay (mapv #(ordinary-fact (keyword "ordinary" (format "%05d" %))) (range 21846))))

(defn census [m facts descriptors]
  (cycle/invoke m "pmk-fact-census" facts {:profile :meta :target :jvm} descriptors :call))

(deftest full-width-and-identity-coverage
  (doseq [m (rest @cycle/models)
          :let [{:keys [descriptor fact]} (cycle/fixture m)
                descriptors {:call descriptor}
                facts (conj @wide-facts fact)
                table (into (sorted-map) (map (juxt :call-core-node-id identity) facts))]]
    (is (< 65536 (* 3 (count facts))))
    (is (= {:status :accepted :items [fact]} (census m facts descriptors)))
    (is (= {:status :accepted :items [fact]} (census m table descriptors)))
    (is (= {:status :accepted :items []} (census m [] {})))
    (is (= {:status :rejected :reason :private-map-keys-source-mismatch} (census m [] descriptors)))
    (doseq [bad [(conj facts fact)
                 (conj facts (assoc fact :call-core-node-id :missing))
                 (assoc table :wrong-key fact)
                 (conj facts (ordinary-fact 42))
                 (conj facts {:call-core-node-id :late :fact-id-request {}})]]
      (is (= :rejected (:status (census m bad descriptors)))))))

(deftest complete-fact-equality-domain-dispatch-and-inert-payloads
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque rendering" {}))))]
    (doseq [m (rest @cycle/models)
            :let [{:keys [descriptor fact]} (cycle/fixture m) descriptors {:call descriptor}]]
      (doseq [changed [(assoc fact :profile :hosted) (assoc fact :target :llvm)
                       (assoc fact :effects [:io/read]) (assoc fact :capabilities [:filesystem/read])
                       (assoc fact :result-type :gravity.type/never) (assoc fact :syntax-id :forged)
                       (assoc fact :origin-chain [:forged]) (assoc fact :status :pending)
                       (assoc fact :evaluation-order :arguments-first)
                       (assoc fact :dispatch :other)
                       (assoc-in fact [:fact-id-request :domain] :gravity/sh08-call-type-fact-v1)
                       (assoc-in fact [:applied-private-map-keys :key-domain-proof :entries] [])
                       (assoc fact :unexpected :field)]]
        (is (= :rejected (:status (census m [changed] descriptors)))))
      (is (= {:status :accepted :items [fact]}
             (census m [(assoc (ordinary-fact :inert) :payload opaque) fact] descriptors)))
      (doseq [bad [(assoc (ordinary-fact :late) :dispatch opaque)
                   (ordinary-fact opaque)
                   {:call-core-node-id :late :fact-id-request {:domain opaque}}]]
        (is (= :rejected (:status (census m (conj @wide-facts fact bad) descriptors))))
        (is (empty? @callbacks)))
      (let [late (assoc (into (sorted-map) (map (juxt :call-core-node-id identity) (conj @wide-facts fact)))
                        :zzzz/late (assoc (ordinary-fact :zzzz/late) :dispatch opaque))]
        (is (= :zzzz/late (last (keys late))))
        (is (= :rejected (:status (census m late descriptors))))
        (is (empty? @callbacks)))
      (is (empty? @callbacks)))))

(defn caller-model [m]
  (let [space (:space m) named #(symbol (str (:prefix m) "-" %))
        observed (atom [])
        f (cycle/fixture m)
        core (-> (:core f) (assoc :calls [(:call f)])
                 (update :call-edges #(mapv (fn [edge] (assoc edge :call-core-node-id :call)) %)))
        descriptor (:descriptor (cycle/invoke m "pmk-header" core))
        f (assoc f :core core :descriptor descriptor :types {:operator descriptor}
                 :row (cycle/invoke m "pmk-constraint" descriptor)
                 :fact (cycle/invoke m "pmk-call-fact" descriptor (:module core)))]
    (binding [*ns* space]
      ;; Only aggregation and entry admission are injected. Census, constructors,
      ;; original-source indexing and the actual evidence caller execute below.
      (intern space (named (if (= "body" (:prefix m)) "all?" "typed-all")) #(every? true? %))
      (intern space (named "pmk-entry-evidence")
              (fn [& args] (swap! observed conj (last args)) {:status :accepted :work-count 5}))
      (doseq [suffix ["pmk-originals" "pmk-evidence"]]
        (eval (get (:defs m) (named suffix)))))
    {:fixture f :observed observed}))

(deftest caller-no-longer-debits-the-fact-table
  (doseq [m (rest @cycle/models)
          :let [{:keys [fixture observed]} (caller-model m)
                {:keys [core node call descriptor nodes types row fact]} fixture
                facts (conj @wide-facts fact)
                invoke #(cycle/invoke m "pmk-evidence" core [row] % node call
                                     (first (:call-edges core)) nodes {} types {} 20)]]
    (is (= {:status :accepted :descriptor descriptor :work-count 13} (invoke facts)))
    (is (= [12] @observed) "Only the remaining header/source/row charges reduce entry allowance")
    (is (= :private-map-keys-source-mismatch
           (:reason (invoke (assoc facts (dec (count facts)) (assoc fact :profile :hosted))))))
    (is (= [12] @observed) "Malformed facts refuse before entry admission")
    (is (nil? (find-ns 'gravity.bootstrap)))))
