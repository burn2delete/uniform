(ns gravity.pmk.first-request
  "Input binding for one PMK operation. No launch, lease or production ports."
  (:require [gravity.pmk.acceptance :as pmk]))

(def selector 'sh08-private-map-keys-preparation-semantic-cells)
(def required-apis #{'sh08-pmk-index-forms 'sh08-pmk-type-view})
(def required-ports #{:acquire :release :start :supervise :capture :terminate :bookend :claim})

(defn digest [data]
  (pmk/sha (.getBytes (pr-str (pmk/owner-contract-data data)) "UTF-8")))

(defn- reject! [reason]
  (throw (ex-info "PMK first-operation input binding rejected" {:reason reason})))

(defn- need! [condition reason]
  (when-not condition (reject! reason)))

(defn- hash? [x]
  (and (string? x) (boolean (re-matches #"[0-9a-f]{64}" x))))

(defn- absolute? [x]
  (and (string? x) (.isAbsolute (java.io.File. ^String x))))

(defn prepare
  "Revalidate the entire frozen adapter contract, then project one exact var.
  The parent plan is retained; its fifteen-selector argv is never submitted."
  [manifest observation snapshot operation-id outputs]
  (let [parent (pmk/prepare manifest observation snapshot)
        selected (first (:closure parent))]
    (need! (and (= selector (:selector selected))
                (= {:c7 required-apis} (:owner-apis selected))
                (= 15 (count (:selection parent)) (count (:closure parent)))
                (= (:selection parent)
                   (mapv #(str pmk/test-ns "/" (:selector %)) (:closure parent)))) :first-selector)
    (need! (and (string? operation-id) (seq operation-id)) :operation-id)
    (need! (and (= #{:result :stdout :stderr :terminal} (set (keys outputs)))
                (every? absolute? (vals outputs))
                (= 4 (count (set (vals outputs))))) :outputs)
    {:schema :pmk-first-request-v1 :execution :unrun
     :operation-id operation-id :parent-plan parent :parent-sha256 (digest parent)
     :source-revision (:source-revision parent) :checkout (:checkout parent)
     :inputs-sha256 (:inputs-sha256 parent) :runtime (:runtime parent)
     :cwd (.getPath (java.net.URI. ^String (:root parent)))
     :selection [(str pmk/test-ns "/" selector)]
     :owner-apis {:c7 required-apis}
     :argv ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace" pmk/test-ns
            "--exact" (str selector)]
     :outputs outputs}))

(defn bind-inputs
  "Check independently reviewed expectations against observations before a
  caller may even submit to its normal platform route. This is consistency,
  not authentication or execution admission. No default roots or controllers.
  verify! must be the separately reviewed normal owner's binding verifier;
  this namespace provides no production implementation and cannot launch."
  [request expected observed verify!]
  (need! (= :pmk-first-request-v1 (:schema request)) :request-schema)
  (let [parent (:parent-plan request)
        selection [(str pmk/test-ns "/" selector)]]
    (need! (and (= :unrun (:execution request) (:execution parent))
                (= (:parent-sha256 request) (digest parent))
                (= selection (:selection request))
                (= {:c7 required-apis} (:owner-apis request))
                (= ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace" pmk/test-ns
                    "--exact" (str selector)] (:argv request))
                (= (select-keys parent [:source-revision :checkout :inputs-sha256 :runtime])
                   (select-keys request [:source-revision :checkout :inputs-sha256 :runtime]))
                (= (.getPath (java.net.URI. ^String (:root parent))) (:cwd request)))
           :request-drift))
  (need! (= (digest request) (:request-sha256 expected)) :request-binding)
  (need! (= expected observed) :observed-binding)
  (need! (= #{:request-sha256 :runtime :controller :guards :coverage :limits :history :outputs}
             (set (keys expected))) :binding-shape)
  (let [{:keys [runtime controller guards coverage limits history outputs]} expected]
    (need! (and (= #{:ordered-classpath :java :clojure :config-sha256 :environment-sha256}
                   (set (keys runtime)))
                (vector? (:ordered-classpath runtime)) (seq (:ordered-classpath runtime))
                (every? #(and (absolute? (:path %)) (hash? (:sha256 %)))
                        (concat (:ordered-classpath runtime) [(:java runtime) (:clojure runtime)]))
                (every? hash? [(:config-sha256 runtime) (:environment-sha256 runtime)]))
           :runtime-binding)
    (need! (and (absolute? (:path controller)) (hash? (:sha256 controller))
                (= required-ports (set (keys (:ports controller))))
                (every? hash? (vals (:ports controller)))) :controller-binding)
    (need! (and (vector? guards) (seq guards)
                (every? #(and (keyword? (:id %)) (absolute? (:root %))
                              (keyword? (:resource-class %)) (hash? (:policy-sha256 %))) guards)
                (= (count guards) (count (set (map :id guards))))) :guard-binding)
    (need! (and (= (:acquisition-order coverage) (mapv :id guards))
                (= (set (:required-guards coverage)) (set (map :id guards)))
                (hash? (:authority-sha256 coverage))
                (seq (:participants coverage))
                (every? #(= (set (map :id guards)) (set (get % :guards)))
                        (:participants coverage))) :participant-coverage)
    (need! (and (= #{:heap-bytes :whole-timeout-ms :capture-bytes :containment-sha256
                     :deadline-policy :release-policy} (set (keys limits)))
                (= 2147483648 (:heap-bytes limits))
                (every? #(and (integer? %) (pos? %))
                        [(:whole-timeout-ms limits) (:capture-bytes limits)])
                (hash? (:containment-sha256 limits))
                (= :before-acquisition-through-release (:deadline-policy limits))
                (= :terminal-before-publication (:release-policy limits))) :host-limits)
    (need! (and (= (:operation-id request) (:operation-id history))
                (= :never-submitted (:state history))
                (nil? (:original-handle history)) (hash? (:evidence-sha256 history))) :operation-history)
    (need! (and (= (:outputs request) (:paths outputs))
                (= :absent (:state outputs)) (hash? (:observation-sha256 outputs))) :output-binding)
    ;; A supplied Boolean is insufficient: the existing owner's verifier must
    ;; bind the exact complete tuple, including root coverage and containment.
    (need! (fn? verify!) :normal-verifier-unavailable)
    (need! (= {:request-sha256 (digest request) :binding-sha256 (digest expected)
               :status :verified-for-submission}
              (verify! request expected)) :normal-verifier-refused)
    {:schema :pmk-first-bound-inputs-v1 :execution :unrun :authenticated? false
     :request request :binding expected :status :ready-for-platform-assessment}))
