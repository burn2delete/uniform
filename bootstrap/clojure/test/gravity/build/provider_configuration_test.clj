(ns gravity.build.provider-configuration-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.build.provider-configuration :as configuration]))

(def ^:private layer-order
  [:source :package :workspace :profile-default :compiler-default])

(defn- location [n]
  {:file "public/project.gravity" :start n :end (+ n 4)})

(defn- selection [id version phase n]
  {:capability :fs/read :phase phase
   :provider {:id id :version version} :location (location n)})

(defn- declaration [id n] {:id id :location (location n)})

(defn- empty-summary []
  {:build {:request [] :deny []} :runtime {:request [] :deny []}})

(defn- fixture []
  {:profile :hosted :target :jvm-21
   :layers
   (into {}
         (map-indexed
          (fn [i layer]
            [layer (cond-> {:selections [(selection (symbol "example.fs" (name layer))
                                                    (str (inc i) ".0.0") :build i)]
                            :effects (empty-summary) :capabilities (empty-summary)}
                     (#{:profile-default :compiler-default} layer)
                     (assoc :profile :hosted :target :jvm-21))])
          layer-order))})

(defn- diagnostic [input]
  (try (configuration/normalize input) nil
       (catch clojure.lang.ExceptionInfo error
         (is (= "Invalid explicit provider configuration" (.getMessage error)))
         (is (nil? (.getCause error)))
         (ex-data error))))

(defn- rejected [input reason path]
  (is (= {:id "BUILD-PROVIDER-CONFIGURATION" :reason reason :path path}
         (diagnostic input))))

(deftest all-layers-retain-exact-references-and-public-locations
  (let [input (fixture) result (configuration/normalize input)]
    (is (= #{:profile :target :layers} (set (keys result))))
    (is (= :hosted (:profile result)))
    (is (= :jvm-21 (:target result)))
    (is (= layer-order (mapv :layer (:layers result))))
    (doseq [[i layer] (map-indexed vector layer-order)]
      (is (= (get-in input [:layers layer :selections])
             (get-in result [:layers i :selections]))))
    (doseq [i [3 4]]
      (is (= [:hosted :jvm-21]
             ((juxt :profile :target) (get-in result [:layers i])))))))

(defn- reverse-maps [value]
  (cond
    (map? value) (into (array-map) (map (fn [[k v]] [k (reverse-maps v)])
                                       (reverse (seq value))))
    (vector? value) (mapv reverse-maps value)
    :else value))

(deftest reordering-maps-and-unordered-declarations-is-deterministic
  (let [input (-> (fixture)
                  (update-in [:layers :source :selections] conj
                             (selection 'example.fs/runtime "1.2.3-rc.1+build.2" :runtime 10))
                  (assoc-in [:layers :package :effects :build :request]
                            [(declaration :network/http 30) (declaration :filesystem/read 20)]))
        reordered (-> (reverse-maps input)
                      (update-in [:layers :source :selections] #(vec (reverse %)))
                      (update-in [:layers :package :effects :build :request] #(vec (reverse %))))]
    (is (= (configuration/normalize input) (configuration/normalize reordered)))
    (is (= (pr-str (configuration/normalize input))
           (pr-str (configuration/normalize reordered))))))

(deftest requests-and-denials-and-phases-survive-without-decisions
  (let [request (declaration :fs/read 20)
        denial (declaration :fs/read 30)
        input (-> (fixture)
                  (assoc-in [:layers :package :capabilities :build :request] [request])
                  (assoc-in [:layers :workspace :capabilities :build :deny] [denial])
                  (assoc-in [:layers :workspace :effects :runtime :deny]
                            [(declaration :filesystem/read 40)])
                  (assoc-in [:layers :source :capabilities :runtime :request] [request]))
        result (configuration/normalize input)]
    (is (= [request] (get-in result [:layers 1 :capabilities :build :request])))
    (is (= [denial] (get-in result [:layers 2 :capabilities :build :deny])))
    (is (= [] (get-in result [:layers 2 :capabilities :runtime :deny])))
    (is (= [(declaration :filesystem/read 40)]
           (get-in result [:layers 2 :effects :runtime :deny])))
    (is (= [request] (get-in result [:layers 0 :capabilities :runtime :request])))
    ;; Overlap in a single summary also remains explicit for later enforcement.
    (is (= {:request [request] :deny [denial]}
           (get-in (configuration/normalize
                    (assoc-in input [:layers :package :capabilities :build :deny] [denial]))
                   [:layers 1 :capabilities :build])))))

(deftest explicit-empty-layers-do-not-invent-defaults
  (let [input (reduce #(assoc-in %1 [:layers %2 :selections] []) (fixture) layer-order)]
    (is (every? empty? (map :selections (:layers (configuration/normalize input)))))))

(deftest malformed-or-inexact-provider-references-reject
  (doseq [reference [{:id 'unqualified :version "1"}
                     {:id :example/fs :version "1"}
                     {:id 'example/fs :version ">=1.0.0"}
                     {:id 'example/fs :version "latest"}
                     {:id 'example/fs :version "1.*"}
                     {:id 'example/fs :version "1.0.0 "}
                     {:id 'example/fs :version 1}
                     {:id (with-meta 'example/fs {:hidden "payload"}) :version "1"}]]
    (rejected (assoc-in (fixture) [:layers :source :selections 0 :provider] reference)
              :reference [:layers :source :selections 0 :provider]))
  (rejected (assoc-in (fixture) [:layers :source :selections 0 :provider]
                     {:id 'example/fs :version "1" :grant "payload"})
            :fields [:layers :source :selections 0 :provider]))

(deftest ambiguous-layer-representation-rejects
  (rejected (assoc (fixture) :layers (vec (vals (:layers (fixture))))) :carrier [:layers])
  (rejected (update (fixture) :layers dissoc :source) :missing-field [:layers :source])
  (rejected (assoc-in (fixture) [:layers :workspace :selections] {:fs/read 'example/fs})
            :carrier [:layers :workspace :selections])
  (rejected (update-in (fixture) [:layers :source :selections] conj
                       (selection 'example/other "2" :build 20))
            :duplicate-selection [:layers :source :selections 1])
  (rejected (update-in (fixture) [:layers :source :selections] #(conj % (first %)))
            :duplicate-selection [:layers :source :selections 1]))

(deftest context-and-default-bindings-are-required
  (doseq [key [:profile :target]]
    (rejected (dissoc (fixture) key) :missing-field [key])
    (rejected (assoc (fixture) key nil) :identifier [key]))
  (doseq [layer [:profile-default :compiler-default]]
    (rejected (update-in (fixture) [:layers layer] dissoc :profile)
              :missing-field [:layers layer :profile])
    (rejected (assoc-in (fixture) [:layers layer :profile] :native)
              :profile-binding [:layers layer :profile])
    (rejected (assoc-in (fixture) [:layers layer :target] :wasm)
              :target-binding [:layers layer :target])))

(deftest malformed-summaries-and-locations-reject
  (rejected (assoc-in (fixture) [:layers :package :effects :build :request]
                     [(declaration :unqualified 1)])
            :identifier [:layers :package :effects :build :request 0 :id])
  (rejected (update-in (fixture) [:layers :package :capabilities :runtime] dissoc :deny)
            :missing-field [:layers :package :capabilities :runtime :deny])
  (doseq [bad [{:file "public.gravity" :start -1 :end 10}
               {:file "public.gravity" :start 10 :end 1}
               {:file (str "bad" (char 0xD800)) :start 0 :end 1}]]
    (rejected (assoc-in (fixture) [:layers :source :selections 0 :location] bad)
              :location [:layers :source :selections 0 :location]))
  (rejected (assoc-in (fixture) [:layers :source :selections 0 :phase] :compile)
            :phase [:layers :source :selections 0 :phase]))

(deftest diagnostics-never-echo-submitted-payloads
  (let [secret "PRIVATE-provider-token-never-public"
        cases [(assoc (fixture) secret {:credentials secret})
               (assoc-in (fixture) [:layers :source :selections 0 :provider :version] secret)
               (assoc-in (fixture) [:layers :source :selections 0 :location]
                         {:file secret :start -1 :end 0})]]
    (doseq [input cases]
      (let [error (diagnostic input)]
        (is (some? error))
        (is (not (.contains (pr-str error) secret)))))))

(deftest active-carriers-and-authority-fields-are-rejected-without-callbacks
  (let [called (atom false)
        lazy-value (lazy-seq (reset! called true) (list :payload))
        callback (fn [] (reset! called true))
        hostile (proxy [Object] []
                  (hashCode [] (reset! called true) 0)
                  (toString [] (reset! called true) "payload"))]
    (doseq [value [lazy-value callback hostile]]
      (rejected (assoc-in (fixture) [:layers :source :selections] value)
                :carrier [:layers :source :selections]))
    (rejected (assoc-in (fixture) [:layers :source :selections 0 :phase] hostile)
              :phase [:layers :source :selections 0 :phase])
    (rejected (with-meta (fixture) {:payload callback}) :metadata [])
    (doseq [key [:grants :trust :execute]]
      (rejected (assoc (fixture) key callback) :fields []))
    (is (false? @called))))
