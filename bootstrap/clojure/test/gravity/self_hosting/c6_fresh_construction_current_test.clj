(ns gravity.self-hosting.c6-fresh-construction-current-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Required current C6 owner is absent"
                      {:id "C6-FRESH-CURRENT-OWNER-ABSENT" :symbol name}))))

(defn- redefine [functions f]
  (with-redefs-fn
    (into {} (map (fn [[name function]] [(private-var name) function]) functions))
    f))

(defn- failure-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- temporary-context []
  (let [path (java.nio.file.Files/createTempFile
              "gravity-c6-fresh-current-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))
        source (str "(ns gravity.c6.fresh.current)\n")]
    {:path (.toString path)
     :context (bootstrap/p15-s23-stage2-gravity-checked-core-context
               (.toString path) source :jvm)
     :source source}))

(defn- bool-core []
  {:core-nodes
   [{:node-id "literal-0" :source-operation :literal :type :gravity/bool
     :attributes {:value true}}
    {:node-id "truthy" :source-operation :truthy :type :gravity/bool}
    {:node-id "literal-1" :source-operation :literal :type :gravity/bool
     :attributes {:value false}}
    {:node-id "literal-2" :source-operation :literal :type :gravity/bool
     :attributes {:value true}}
    {:node-id "if" :source-operation :if :type :gravity/bool}
    {:node-id "root" :source-operation :function
     :type {:params [] :return :gravity/bool :latent-effects #{}
            :capabilities #{} :throws #{}
            :ownership-constraints #{:persistent-immutable-shareable}
            :profile-constraints #{:hosted}}}]})

(defn- current-fakes [context options f]
  (let [events (atom [])
        source-path (:source-path context)
        canonical-path (.getCanonicalPath (java.io.File. source-path))
        source-snapshot {:canonical-source-path canonical-path
                         :source-byte-count 33
                         :source-content-hash
                         "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
        source-binding {:kind :gravity/p15-s23-c6-c10-source-binding
                        :status :complete
                        :source-path source-path
                        :source-contract-version 2
                        :source-mode :development-current
                        :source-byte-count 33
                        :source-content-hash (:source-content-hash source-snapshot)
                        :plan :synthetic-c6-plan
                        :function-manifest :synthetic-c6-functions}
        reader-binding {:artifact :gravity/sh03-current-source-reader-plan-binding
                        :status :complete
                        :source-path "reader.gravity"
                        :plan :synthetic-reader-plan}
        expression-binding {:artifact :gravity/p15-s23-current-expression-binding
                            :status :complete
                            :source-path "emitter.gravity"
                            :source-mode :development-current
                            :source-content-hash
                            "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                            :plan :synthetic-expression-plan}
        receipt {:source-interface {:interface :gravity/c6-c10 :version 2}
                 :admission-status :development-unadmitted
                 :envelope-verification :passed
                 :self-hosted? false}
        reads (atom 0)
        current-snapshot
        (fn [_]
          (let [read-number (swap! reads inc)]
            (swap! events conj [:source-snapshot read-number])
            (if (= (:mutate-at options) read-number)
              (assoc source-snapshot :source-content-hash
                     "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
              source-snapshot)))]
    (redefine
     {'p15-s23-c6c10-validate-public-context!
      (fn [value] (swap! events conj :validate-context) value)
      'p15-s23-c6c10-require-trusted-carrier!
      (fn [_ reason value] (swap! events conj [:trusted-carrier reason]) value)
      'p15-s23-c6c10-source-binding!
      (fn [path version source-mode]
        (swap! events conj [:source-binding path version source-mode])
        source-binding)
      'p15-s23-c6c10-current-reader-program!
      (fn [path]
        (swap! events conj [:reader-owner path])
        {:binding reader-binding
         :invocation {:reader-source-path "reader.gravity"}
         :c6-source-snapshot {:before source-snapshot :after source-snapshot}})
      'p15-s23-c6c10-read-current-source-snapshot!
      current-snapshot
      'p15-s23-stage2-current-expression-development-binding!
      (fn [path]
        (swap! events conj [:expression-owner path])
        expression-binding)
      'p15-s23-c6c10-private-ingress-products
      (fn [& args]
        (swap! events conj [:private-ingress args])
        {:front-end-products {:source-unit-id "synthetic-source"
                              :c3-artifact-id "synthetic-c3"}
         :source-policy-receipt receipt})
      'p15-s23-c6c10-private-builder-envelope
      (fn [& _]
        (swap! events conj :builder-envelope)
        {:binding-pins :synthetic-pins})
      'p15-s23-c6c10-invoke-pinned-source-function!
      (fn [& _]
        (swap! events conj :builder)
        {:status :accepted :artifact-template :synthetic-template})
      'p15-s23-c6c10-seal-digest-request-result!
      (fn [& _]
        (swap! events conj :seal)
        {:status (if (:sealed-rejected? options) :rejected :accepted)
         :graph-proof :synthetic-proof})
      'p15-s23-c6c10-throw-sealed-rejection!
      (fn [& _]
        (throw (ex-info "Synthetic sealed rejection" {:id "SYNTHETIC-SEALED"})))
      'p15-s23-c6c10-gravity-replay-verification!
      (fn [& _]
        (swap! events conj :gravity-replay)
        {:status :passed :request-count 1
         :semantic-authority :synthetic-replay :checks [:synthetic]})
      'p15-s23-c6c10-public-artifact
      (fn [& _]
        (swap! events conj :public-artifact)
        (bool-core))
      'p15-s23-c6c10-project-expression-owner-metadata
      (fn [_ binding]
        ;; The production helper is exercised by the current-main C6 tests;
        ;; this seam proves the constructor keeps the public projection plan-free.
        (select-keys binding [:artifact :status :source-path :source-mode
                              :source-content-hash]))}
     (fn [] {:value (f) :events @events}))))

(deftest current-v2-construction-routes-current-owners-and-projects-plan-free
  (let [{:keys [path context]} (temporary-context)]
    (try
      (let [{:keys [value events]}
            (current-fakes
             context {}
             #((private-var 'p15-s23-c6c10-fresh-construction) context 2))
            receipt (:source-policy-receipt value)]
        (is (= :synthetic-proof (:graph-proof value)))
        (is (not (contains? (:source-binding value) :plan)))
        (is (= :development-current
               (get-in receipt [:expression-binding :source-mode])))
        (is (= "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
               (get-in receipt [:expression-binding :source-content-hash])))
        (is (some #{[:source-binding path 2 :development-current]} events))
        (is (some #{[:reader-owner path]} events))
        (is (some #{[:expression-owner path]} events))
        (is (some #(and (vector? %)
                        (= :private-ingress (first %))
                        (= path (first (second %)))
                        (= (:source-text context) (second (second %)))
                        (= "reader.gravity" (nth (second %) 2))
                        (= 2 (nth (second %) 3))) events))
        (let [replay-index (.indexOf events :gravity-replay)
              artifact-index (.indexOf events :public-artifact)]
          (is (<= 0 replay-index))
          (is (<= 0 artifact-index))
          (is (< replay-index artifact-index)))
        (is (nil? (get-in receipt [:expression-binding :plan])))
        (is (nil? (get-in value [:source-binding :function-manifest]))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest current-source-bookends-reject-before-and-after-mutation
  (doseq [[mutate-at expected-fact forbidden-events]
          [[1 :stable-current-c6-source-before-builder
            #{:builder-envelope :builder :seal :gravity-replay :public-artifact}]
           [2 :stable-current-c6-source-after-verification
            #{:public-artifact}]]]
    (let [{:keys [path context]} (temporary-context)]
      (try
        (let [{:keys [value events]}
              (current-fakes
               context {:mutate-at mutate-at}
               (fn []
                 (failure-data
                  (fn []
                    (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
                     context {:interface :gravity/c6-c10 :version 2})))))
              data value]
          (is (= "C6-VERIFY" (:id data)))
          (is (= expected-fact (:missing-fact data)))
          (doseq [event forbidden-events]
            (is (not (some #{event} events)))))
        (finally
          (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path))))))))

(deftest public-v3-and-native-version-headers-refuse-before-construction
  (let [{:keys [path context]} (temporary-context)
        fresh-var (private-var 'p15-s23-c6c10-fresh-construction)
        calls (atom 0)
        invoke (fn [header]
                 (try
                   (with-redefs-fn
                     {fresh-var (fn [& _] (swap! calls inc) (bool-core))}
                     #(bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
                       context header))
                   nil
                   (catch clojure.lang.ExceptionInfo error
                     (ex-data error))))]
    (try
      (doseq [header [{:interface :gravity/c6-c10 :version 3}
                      {:interface :gravity/c6-c10 :version (int 3)}
                      {:interface :gravity/c6-c10 :version 1}
                      {:interface :gravity/c11-mir :version 2}]]
        (let [data (invoke header)]
          (is (= "C6-VERIFY" (:id data)))
          (is (zero? @calls))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest sealed-rejection-stops-before-replay
  (let [{:keys [path context]} (temporary-context)]
    (try
      (let [{:keys [value events]}
            (current-fakes
             context {:sealed-rejected? true}
             (fn []
               (failure-data
                (fn [] ((private-var 'p15-s23-c6c10-fresh-construction)
                        context 2)))))]
        (is (= "SYNTHETIC-SEALED" (:id value)))
        (is (not (some #{:gravity-replay} events)))
        (is (not (some #{:public-artifact} events))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(let [result (run-tests 'gravity.self-hosting.c6-fresh-construction-current-test)]
  (println {:scope :current-v2-fresh-construction-routing
            :source-bookends :actual-with-synthetic-owner-seams
            :native-version-refusal :actual
            :sealed-rejection-order :actual
            :construction-products :declared-oracles
            :v3-public-construction :not-enabled
            :bootstrap-loaded true})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
