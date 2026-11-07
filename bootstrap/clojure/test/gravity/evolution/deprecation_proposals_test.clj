(ns gravity.evolution.deprecation-proposals-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]
            [gravity.evolution.deprecation-proposals :as deprecations]))

(def ^:private subject
  {:contract "gravity.io/read-string"
   :revision "source:read-string/revision-a"
   :surfaces #{:source :diagnostic :profile :artifact}
   :history [{:id "CHANGE-1" :state :stable :record-ref "record:baseline"
              :profiles #{:hosted :native}
              :safety {:unsafe-island "audit:io-wrapper"
                       :runtime-checked true :ai/human-review :required}}]
   :retained-tests ["test:read-string/utf8" "test:read-string/invalid-encoding"]})

(def ^:private proposal
  {:id "DEP-1" :owner "io-maintainer"
   :replacement "Use gravity.io/read-text with an explicit encoding policy."
   :diagnostic "GDEP0001"
   :warn-in "preview-next" :error-in "compatibility-transition"
   :remove-in "approved-window-pending"
   :migration ["fix:rename-read-string" "guide:explicit-encoding"]})

(def ^:private provenance
  {:source-id "source:io-contract"
   :input-id "input:deprecation-draft"
   :policy-id "GOV8:deprecation-record"
   :tool-id "gravity.evolution.deprecation-proposals:bootstrap"})

(def ^:private expected-product
  {:status :proposed :diagnostics []
   :artifact
   {:schema :gravity.evolution/deprecation-proposal-v1
    :kind :gravity/deprecation-proposal
    :status :proposed :authority :non-admitting
    :contract "gravity.io/read-string"
    :revision "source:read-string/revision-a"
    :surfaces #{:source :diagnostic :profile :artifact}
    :history [{:id "CHANGE-1" :state :stable :record-ref "record:baseline"
               :profiles #{:hosted :native}
               :safety {:unsafe-island "audit:io-wrapper"
                        :runtime-checked true :ai/human-review :required}}]
    :retained-tests ["test:read-string/utf8" "test:read-string/invalid-encoding"]
    :id "DEP-1" :owner "io-maintainer"
    :replacement "Use gravity.io/read-text with an explicit encoding policy."
    :diagnostic "GDEP0001"
    :warn-in "preview-next" :error-in "compatibility-transition"
    :remove-in "approved-window-pending"
    :migration ["fix:rename-read-string" "guide:explicit-encoding"]
    :provenance {:source-id "source:io-contract"
                 :input-id "input:deprecation-draft"
                 :policy-id "GOV8:deprecation-record"
                 :tool-id "gravity.evolution.deprecation-proposals:bootstrap"}}})

(defn- diagnostic [id path reason]
  {:id id :message "Invalid explicit deprecation proposal input"
   :path path :reason reason})

(defn- rejection [id path reason]
  {:status :rejected :diagnostics [(diagnostic id path reason)]})

(defn- product-with-unchanged-inputs [inputs]
  (let [before (pr-str inputs)
        snapshot (walk/postwalk identity inputs)
        product (apply deprecations/create-proposal inputs)]
    (is (= snapshot inputs))
    (is (= before (pr-str inputs)))
    product))

(deftest complete-deprecation-proposal
  (is (= expected-product
         (product-with-unchanged-inputs [subject proposal provenance]))))

(deftest explicit-no-replacement-rationale
  (let [rationale "No safe equivalent exists; migrate to explicit text decoding."
        changed (-> proposal (dissoc :replacement)
                    (assoc :no-replacement-rationale rationale))
        expected (-> expected-product
                     (update :artifact dissoc :replacement)
                     (assoc-in [:artifact :no-replacement-rationale] rationale))]
    (is (= expected (product-with-unchanged-inputs [subject changed provenance])))
    (is (= (assoc-in expected-product [:artifact :no-replacement-rationale] rationale)
           (product-with-unchanged-inputs
            [subject (assoc proposal :no-replacement-rationale rationale) provenance])))))

(deftest changed-migration-changes-product
  (let [migration ["fix:rename-with-reviewed-policy" "guide:encoding-v2"]
        changed (assoc proposal :migration migration)
        product (product-with-unchanged-inputs [subject changed provenance])]
    (is (= (assoc-in expected-product [:artifact :migration] migration) product))
    (is (not= expected-product product))))

(deftest retained-history-and-test-references
  (let [history (conj (:history subject)
                      {:id "CHANGE-2" :state :deprecated
                       :record-ref "record:previous-proposal"
                       :decision {:state :rejected :reason "Revise migration."}})
        tests (conj (:retained-tests subject) "test:read-string/profile-rejection")
        changed (assoc subject :history history :retained-tests tests)]
    (is (= (-> expected-product (assoc-in [:artifact :history] history)
               (assoc-in [:artifact :retained-tests] tests))
           (product-with-unchanged-inputs [changed proposal provenance]))))
  (is (= (assoc-in expected-product [:artifact :history] [])
         (product-with-unchanged-inputs [(assoc subject :history []) proposal provenance]))))

(deftest reordered-input-determinism
  (let [inputs [subject proposal provenance]
        reordered (walk/postwalk
                   #(cond
                      (map? %) (into (sorted-map-by (fn [a b] (compare b a))) %)
                      (set? %) (into (sorted-set-by (fn [a b] (compare b a))) %)
                      :else %) inputs)
        original-product (product-with-unchanged-inputs inputs)
        reordered-product (product-with-unchanged-inputs reordered)]
    (is (= inputs reordered))
    (is (not= (pr-str inputs) (pr-str reordered)))
    (is (= expected-product original-product reordered-product))
    (is (= (pr-str original-product) (pr-str reordered-product))))
  (let [product (binding [*print-length* 1 *print-level* 1 *print-meta* true
                         *print-readably* false]
                  (deprecations/create-proposal subject proposal provenance))]
    (is (= expected-product product))
    (is (= (pr-str (deprecations/create-proposal subject proposal provenance))
           (pr-str product)))))

(deftest changed-explicit-fields-change-complete-product
  (doseq [[argument field value path]
          [[0 :contract "gravity.io/old-read" [:artifact :contract]]
           [0 :revision "source:read-string/revision-b" [:artifact :revision]]
           [0 :surfaces #{:source :profile} [:artifact :surfaces]]
           [1 :owner "text-maintainer" [:artifact :owner]]
           [1 :replacement "Use explicit text decoding." [:artifact :replacement]]
           [1 :diagnostic "GDEP0002" [:artifact :diagnostic]]
           [1 :warn-in "z-last" [:artifact :warn-in]]
           [1 :error-in "a-first" [:artifact :error-in]]
           [1 :remove-in "not-a-version" [:artifact :remove-in]]
           [2 :input-id "input:revised-draft" [:artifact :provenance :input-id]]]]
    (let [inputs (assoc-in [subject proposal provenance] [argument field] value)
          product (product-with-unchanged-inputs inputs)]
      (is (= (assoc-in expected-product path value) product))
      (is (not= expected-product product)))))

(deftest missing-replacement-and-rationale
  (is (= (rejection "GOV8003" [:proposal :replacement]
                    :missing-replacement-or-rationale)
         (product-with-unchanged-inputs
          [subject (dissoc proposal :replacement) provenance]))))

(deftest malformed-guidance-is-rejected
  (doseq [[field value] [[:replacement ""] [:replacement "  "]
                         [:replacement nil] [:replacement []]
                         [:no-replacement-rationale ""]]]
    (is (= (rejection "GOV8003" [:proposal field] :invalid-field)
           (product-with-unchanged-inputs
            [subject (assoc proposal field value) provenance])))))

(deftest missing-diagnostic-or-schedule
  (doseq [field [:diagnostic :warn-in :error-in :remove-in]]
    (is (= (rejection "GOV8004" [:proposal field] :missing-field)
           (product-with-unchanged-inputs
            [subject (dissoc proposal field) provenance]))))
  (doseq [field [:diagnostic :warn-in :error-in :remove-in]
          value [nil " " []]]
    (is (= (rejection "GOV8004" [:proposal field] :invalid-field)
           (product-with-unchanged-inputs
            [subject (assoc proposal field value) provenance]))))
  (is (= {:status :rejected
          :diagnostics (mapv #(diagnostic "GOV8004" [:proposal %] :missing-field)
                             [:diagnostic :warn-in :error-in :remove-in])}
         (product-with-unchanged-inputs
          [subject (apply dissoc proposal [:diagnostic :warn-in :error-in :remove-in])
           provenance]))))

(deftest missing-and-malformed-carrier-fields
  (doseq [[index root fields]
          [[0 :subject [:contract :revision :surfaces :history :retained-tests]]
           [1 :proposal [:id :owner :migration]]
           [2 :provenance [:source-id :input-id :policy-id :tool-id]]]
          field fields]
    (is (= (rejection "DEPRECATION-INPUT" [root field] :missing-field)
           (product-with-unchanged-inputs
            (update [subject proposal provenance] index dissoc field)))))
  (doseq [[index field value]
          [[0 :contract " "] [0 :revision nil] [0 :surfaces #{}]
           [0 :surfaces [:source]] [0 :history ["record:history"]]
           [0 :history nil] [0 :retained-tests []] [0 :retained-tests [""]]
           [1 :id ""] [1 :owner " "] [1 :migration []]
           [1 :migration ["fix:valid" nil]] [2 :source-id ""]]]
    (is (= (rejection "DEPRECATION-INPUT"
                      [(nth [:subject :proposal :provenance] index) field] :invalid-field)
           (product-with-unchanged-inputs
            (assoc-in [subject proposal provenance] [index field] value))))))

(defrecord HostRecord [value])

(deftest malformed-or-mutable-input-is-payload-free
  (doseq [[index root] [[0 :subject] [1 :proposal] [2 :provenance]]
          value [nil "secret:do-not-echo" []
                 (java.util.HashMap. {:secret "secret:do-not-echo"})
                 (->HostRecord "secret:do-not-echo")]]
    (let [inputs (assoc [subject proposal provenance] index value)
          result (apply deprecations/create-proposal inputs)]
      (is (= (rejection "DEPRECATION-INPUT" [root] :invalid-field) result))
      (is (not (str/includes? (pr-str result) "secret:do-not-echo")))))
  (doseq [value [(atom "secret:do-not-echo")
                 (java.util.ArrayList. ["secret:do-not-echo"])
                 (java.util.HashMap. {:secret "secret:do-not-echo"})
                 (->HostRecord "secret:do-not-echo")
                 (fn [] (throw (ex-info "Must not invoke input" {})))
                 (lazy-seq (throw (ex-info "Must not realize input" {})))]]
    (let [inputs [(assoc-in subject [:history 0 :extension] value) proposal provenance]
          result (apply deprecations/create-proposal inputs)]
      (is (= (rejection "DEPRECATION-INPUT" [:subject] :invalid-field) result))
      (is (not (str/includes? (pr-str result) "secret:do-not-echo")))))
  (doseq [[index root] [[0 :subject] [1 :proposal] [2 :provenance]]]
    (let [value (java.util.concurrent.atomic.AtomicLong. 7)
          inputs (assoc-in [subject proposal provenance] [index :extension] value)
          expected (rejection "DEPRECATION-INPUT" [root] :invalid-field)
          result (apply deprecations/create-proposal inputs)]
      (is (= expected result))
      (is (= 7 (.get value)))
      (.set value 99)
      (is (= expected result (apply deprecations/create-proposal inputs)))
      (is (= 99 (.get value)))))
  (let [mutable (java.util.HashMap. proposal)
        inputs [subject mutable provenance]
        result (apply deprecations/create-proposal inputs)]
    (is (= (rejection "DEPRECATION-INPUT" [:proposal] :invalid-field) result))
    (is (= proposal (into {} mutable)))
    (.put mutable :state :removed)
    (.put mutable :secret "secret:do-not-echo")
    (is (= result (apply deprecations/create-proposal inputs)))))

(deftest immutable-history-numbers-are-retained
  (doseq [value [(byte 1) (short 2) (int 3) (long 4) (float 1.5) (double 2.5)
                 (biginteger 5) 6N 7.5M 1/3]]
    (let [changed (assoc-in subject [:history 0 :extension] {:number value})]
      (is (= (assoc-in expected-product [:artifact :history 0 :extension] {:number value})
             (product-with-unchanged-inputs [changed proposal provenance]))))))

(deftest nested-mutable-history-remains-rejected-after-mutation
  (let [value (java.util.concurrent.atomic.AtomicLong. 7)
        inputs [(assoc-in subject [:history 0 :extension] {:number value})
                proposal provenance]
        expected (rejection "DEPRECATION-INPUT" [:subject] :invalid-field)
        result (apply deprecations/create-proposal inputs)]
    (is (= expected result))
    (is (= 7 (.get value)))
    (.set value 99)
    (is (= expected result (apply deprecations/create-proposal inputs)))
    (is (= 99 (.get value)))))

(deftest rejection-order-and-payload-free-controls
  (let [inputs [(-> subject (dissoc :revision)
                    (assoc-in [:history 0 :public-note] "secret:do-not-echo"))
                (-> proposal (dissoc :owner :replacement :diagnostic :warn-in)
                    (assoc :public-note "secret:do-not-echo"))
                (assoc provenance :source-id "" :input-id "secret:do-not-echo")]
        expected {:status :rejected
                  :diagnostics
                  [(diagnostic "DEPRECATION-INPUT" [:subject :revision] :missing-field)
                   (diagnostic "DEPRECATION-INPUT" [:proposal :owner] :missing-field)
                   (diagnostic "GOV8003" [:proposal :replacement]
                               :missing-replacement-or-rationale)
                   (diagnostic "GOV8004" [:proposal :diagnostic] :missing-field)
                   (diagnostic "GOV8004" [:proposal :warn-in] :missing-field)
                   (diagnostic "DEPRECATION-INPUT" [:provenance :source-id] :invalid-field)]}
        result (product-with-unchanged-inputs inputs)]
    (is (= expected result))
    (is (not (str/includes? (pr-str result) "secret:do-not-echo")))))

(deftest caller-removed-state-cannot-promote
  (let [history (conj (:history subject)
                      {:state :removed :record-ref "record:historical-removal"})
        claims {:state :removed :status :removed :authority :approved
                :decision :accepted :emergency true :grant "claim:removal"}
        inputs [(merge subject claims {:history history})
                (merge proposal claims) (merge provenance claims)]]
    (is (= (assoc-in expected-product [:artifact :history] history)
           (product-with-unchanged-inputs inputs))))
  (is (= expected-product
         (product-with-unchanged-inputs
          [(assoc subject :unknown "ignored")
           (assoc proposal :unknown "ignored")
           (assoc provenance :unknown "ignored")]))))
