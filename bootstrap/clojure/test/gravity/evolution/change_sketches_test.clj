(ns gravity.evolution.change-sketches-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.evolution.change-sketches :as sketches]))

(def ^:private change
  {:id "change:diagnostic-origins" :revision "revision:initial"
   :title "Retain macro origins in diagnostics"
   :owner "compiler-maintainer"
   :problem-statement "Generated diagnostics lose the user's macro call site."
   :scope #{:compiler :tooling}
   :affected-surfaces #{:source :diagnostics :artifact}
   :affected-documents ["C3" "C4" "T8"]})

(def ^:private provenance
  {:source-id "source:diagnostic-origins"
   :input-id "input:change-sketch"
   :policy-id "GOV1:sketch"
   :tool-id "gravity.evolution.change-sketches:bootstrap"})

(def ^:private expected-product
  {:status :sketch :diagnostics []
   :artifact
   {:schema :gravity.evolution/change-sketch-v1
    :kind :gravity/language-change-record
    :state :sketch :authority :non-admitting
    :id "change:diagnostic-origins" :revision "revision:initial"
    :title "Retain macro origins in diagnostics"
    :owner "compiler-maintainer"
    :problem-statement "Generated diagnostics lose the user's macro call site."
    :scope #{:compiler :tooling}
    :affected-surfaces #{:source :diagnostics :artifact}
    :affected-documents ["C3" "C4" "T8"]
    :provenance provenance
    :decision-history
    [{:event :authored :actor "compiler-maintainer" :state :sketch
      :subject {:id "change:diagnostic-origins" :revision "revision:initial"}
      :provenance provenance}]}})

(defn- diagnostic [path reason]
  {:id "GOV1001" :message "Invalid explicit change sketch input"
   :path path :reason reason})

(defn- rejection [path reason]
  {:status :rejected :diagnostics [(diagnostic path reason)]})

(defn- expect-product [expected change-input provenance-input]
  (let [inputs [change-input provenance-input]
        before (pr-str inputs)
        result (apply sketches/create-sketch inputs)]
    (is (= expected result))
    (is (= before (pr-str inputs)))
    result))

(deftest complete-owned-sketch
  (expect-product expected-product change provenance))

(deftest changed-problem-changes-product
  (let [problem "The language server loses generated-source diagnostic origins."
        changed (assoc change :problem-statement problem)
        result (expect-product
                (assoc-in expected-product [:artifact :problem-statement] problem)
                changed provenance)]
    (is (not= expected-product result))))

(deftest reordered-map-determinism
  (let [backwards (fn [m] (into (sorted-map-by #(compare %2 %1)) m))
        reordered (-> (backwards change)
                      (assoc :scope (into (sorted-set-by #(compare %2 %1))
                                          (:scope change))
                             :affected-surfaces
                             (into (sorted-set-by #(compare %2 %1))
                                   (:affected-surfaces change))))
        original (expect-product expected-product change provenance)
        result (expect-product expected-product reordered (backwards provenance))
        truncated (binding [*print-length* 1 *print-level* 1]
                    (sketches/create-sketch reordered (backwards provenance)))]
    (is (= change reordered))
    (is (not= (pr-str change) (pr-str reordered)))
    (is (= expected-product truncated))
    (is (= (pr-str original) (pr-str result) (pr-str truncated)))))

(deftest initial-history-and-provenance-retained
  (let [changed (assoc change :id "change:profile-diagnostics"
                      :revision "revision:second" :owner "profile-maintainer")
        references {:source-id "source:profile-diagnostics"
                    :input-id "input:second-sketch"
                    :policy-id "GOV1:current-sketch"
                    :tool-id "tool:current-producer"}
        expected (-> expected-product
                     (assoc-in [:artifact :id] "change:profile-diagnostics")
                     (assoc-in [:artifact :revision] "revision:second")
                     (assoc-in [:artifact :owner] "profile-maintainer")
                     (assoc-in [:artifact :provenance] references)
                     (assoc-in [:artifact :decision-history]
                               [{:event :authored :actor "profile-maintainer"
                                 :state :sketch
                                 :subject {:id "change:profile-diagnostics"
                                           :revision "revision:second"}
                                 :provenance references}]))]
    (expect-product expected changed references)))

(deftest declared-inputs-change-complete-product
  (doseq [[field value] [[:title "Retain complete diagnostic origins"]
                         [:scope #{:compiler :tooling :documentation}]
                         [:affected-surfaces #{:diagnostics :schema}]
                         [:affected-documents ["T8" "C4" "C3"]]]]
    (expect-product (assoc-in expected-product [:artifact field] value)
                    (assoc change field value) provenance)))

(deftest missing-owner
  (expect-product (rejection [:change :owner] :missing-field)
                  (dissoc change :owner) provenance))

(deftest missing-problem
  (expect-product (rejection [:change :problem-statement] :missing-field)
                  (dissoc change :problem-statement) provenance))

(deftest empty-affected-documents
  (expect-product (rejection [:change :affected-documents] :empty-field)
                  (assoc change :affected-documents []) provenance))

(deftest malformed-scope
  (doseq [[value reason] [[nil :invalid-type]
                          [[:compiler] :invalid-type]
                          [#{} :empty-field]
                          [#{:compiler "secret-marker:scope"} :invalid-member]]]
    (expect-product (rejection [:change :scope] reason)
                    (assoc change :scope value) provenance)))

(deftest text-identity-and-provenance-rejections
  (doseq [[argument fields] [[:change [:id :revision :title :owner :problem-statement]]
                              [:provenance [:source-id :input-id :policy-id :tool-id]]]
          field fields
          [operation reason] [[#(dissoc % field) :missing-field]
                              [#(assoc % field " \t\n") :blank-field]
                              [#(assoc % field nil) :invalid-type]
                              [#(assoc % field {:secret-marker "private"})
                               :invalid-type]]]
    (testing (str argument " " field " " reason)
      (expect-product (rejection [argument field] reason)
                      (if (= :change argument) (operation change) change)
                      (if (= :provenance argument)
                        (operation provenance) provenance)))))

(deftest collection-and-map-rejections
  (doseq [[field value reason]
          [[:scope nil :invalid-type]
           [:affected-surfaces nil :invalid-type]
           [:affected-surfaces #{} :empty-field]
           [:affected-surfaces #{:source 7} :invalid-member]
           [:affected-documents '("C3") :invalid-type]
           [:affected-documents ["C3" " "] :invalid-member]
           [:affected-documents ["C3" {:secret-marker "private"}] :invalid-member]]]
    (expect-product (rejection [:change field] reason)
                    (assoc change field value) provenance))
  (doseq [field [:scope :affected-surfaces :affected-documents]]
    (expect-product (rejection [:change field] :missing-field)
                    (dissoc change field) provenance))
  (doseq [value [nil [] "secret-marker:map"]]
    (expect-product (rejection [:change] :invalid-type) value provenance)
    (expect-product (rejection [:provenance] :invalid-type) change value)))

(deftest diagnostics-are-ordered-and-payload-free
  (let [malformed (-> change (dissoc :id :owner)
                      (assoc :problem-statement {:secret-marker "private"}
                             :scope #{"secret-marker:scope"}
                             :affected-documents []))
        references (dissoc provenance :source-id :tool-id)
        expected {:status :rejected
                  :diagnostics [(diagnostic [:change :id] :missing-field)
                                (diagnostic [:change :owner] :missing-field)
                                (diagnostic [:change :problem-statement] :invalid-type)
                                (diagnostic [:change :scope] :invalid-member)
                                (diagnostic [:change :affected-documents] :empty-field)
                                (diagnostic [:provenance :source-id] :missing-field)
                                (diagnostic [:provenance :tool-id] :missing-field)]}
        backwards (fn [m] (into (sorted-map-by #(compare %2 %1)) m))
        result (expect-product expected malformed references)]
    (expect-product expected (backwards malformed) (backwards references))
    (is (not (str/includes? (pr-str result) "secret-marker")))))

(deftest caller-approved-state-cannot-promote-sketch
  (doseq [state [:draft :rfc :prototype :review :accepted :implemented
                 :stabilized :deprecated :removed :rejected]]
    (let [claims {:state state :status :approved :authority :admitting
                  :approval {:decision :accepted :reviewer "claimed-reviewer"}
                  :decision-history [{:state :accepted :actor "claimed-reviewer"}]}
          decorated (merge change claims)
          references (merge provenance claims)]
      (expect-product expected-product decorated references))))

(deftest input-metadata-is-not-exported
  (let [decorate #(with-meta % {:secret-marker "private metadata"})
        input (-> change decorate
                  (update :scope decorate)
                  (update :affected-surfaces decorate)
                  (update :affected-documents decorate))
        before (binding [*print-meta* true] (pr-str [input (decorate provenance)]))
        result (expect-product expected-product input (decorate provenance))]
    (is (= before (binding [*print-meta* true]
                    (pr-str [input (decorate provenance)]))))
    (is (not (str/includes? (binding [*print-meta* true] (pr-str result))
                           "secret-marker")))))

(deftest mutable-host-input-rejected
  (let [host-change (java.util.HashMap. change)
        host-provenance (java.util.HashMap. provenance)
        host-scope (java.util.HashSet. (:scope change))
        host-documents (java.util.ArrayList. (:affected-documents change))
        host-text (StringBuilder. "secret-marker:mutable-text")
        host-number (java.util.concurrent.atomic.AtomicLong. 1)]
    (doseq [[argument value path mutation]
            [[:change host-change [:change]
              #(.put host-change :owner "secret-marker:changed-owner")]
             [:provenance host-provenance [:provenance]
              #(.put host-provenance :source-id "secret-marker:changed-source")]
             [:change (assoc change :scope host-scope) [:change :scope]
              #(.add host-scope :secret-marker)]
             [:change (assoc change :affected-documents host-documents)
              [:change :affected-documents]
              #(.add host-documents "secret-marker:document")]
             [:change (assoc change :problem-statement host-text)
              [:change :problem-statement] #(.append host-text ":changed")]
             [:change (assoc change :revision host-number) [:change :revision]
              #(.incrementAndGet host-number)]]]
      (let [change-input (if (= :change argument) value change)
            references (if (= :provenance argument) value provenance)
            expected (rejection path :invalid-type)
            before-text (str host-text)
            before-number (.get host-number)
            result (expect-product expected change-input references)
            rendered (pr-str result)]
        (is (= before-text (str host-text)))
        (is (= before-number (.get host-number)))
        (mutation)
        (is (= expected result))
        (is (= rendered (pr-str result)))
        (expect-product expected change-input references)
        (is (not (str/includes? rendered "secret-marker")))))))
