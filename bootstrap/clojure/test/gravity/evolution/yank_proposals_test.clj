(ns gravity.evolution.yank-proposals-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.evolution.yank-proposals :as yanks]))

(def ^:private historical-lock
  {:lock-id "lock:consumer/previous"
   :package "gravity/http" :registry "gravity-public" :version "1.1.0"
   :content-id "sha256:previous-content" :provenance-ref "provenance:previous"
   :selected-features [:client]
   :capability-summary #{:network/http}
   :safety-summary {:unsafe-islands [] :ai/human-review :required}})

(def ^:private historical-content
  {:package "gravity/http" :registry "gravity-public" :version "1.1.0"
   :content-id "sha256:previous-content" :provenance-ref "provenance:previous"
   :artifact-refs ["artifact:previous-source" "artifact:previous-binary"]
   :signature-ref "signature:previous"})

(def ^:private release
  {:package "gravity/http" :registry "gravity-public" :namespace "gravity"
   :version "1.2.0" :content-id "sha256:released-content"
   :provenance-ref "provenance:released"
   :history {:locks [historical-lock]
             :contents [historical-content]
             :events [{:status :published :release "release:previous"}]}
   :metadata {:profiles #{:hosted :native}
              :source-forms '(ns gravity.http)
              :license "Apache-2.0"}})

(def ^:private proposal
  {:owner "http-maintainer"
   :reason "Incorrect response validation in this release."
   :replacement-guidance "Use gravity/http 1.2.1."
   :resolution-behavior {:new-selection :forbidden :existing-locks :preserve}})

(def ^:private provenance
  {:source-id "source:registry-release-record"
   :input-id "input:yank-request"
   :policy-id "GOV10:registry-yank"
   :tool-id "gravity.evolution.yank-proposals:bootstrap"})

(def ^:private expected-product
  {:status :proposed :diagnostics []
   :artifact {:schema :gravity.evolution/yank-proposal-v1
              :kind :gravity/yank-proposal
              :status :proposed :authority :non-admitting
              :release release :proposal proposal :provenance provenance}})

(defn- rejection [id path reason]
  {:status :rejected
   :diagnostics [{:id id :message "Invalid explicit yank proposal input"
                  :path path :reason reason}]})

(deftest complete-proposal-preserves-release
  (let [inputs [release proposal provenance]
        before (pr-str inputs)
        product (apply yanks/propose-yank inputs)]
    (is (= expected-product product))
    (is (= inputs [release proposal provenance]))
    (is (= before (pr-str inputs)))))

(deftest changed-reason-changes-product
  (let [changed (assoc proposal :reason "Published source contains an incorrect fix.")
        inputs [release changed provenance]
        before (pr-str inputs)
        product (apply yanks/propose-yank inputs)]
    (is (= (assoc-in expected-product [:artifact :proposal] changed) product))
    (is (not= expected-product product))
    (is (= before (pr-str inputs)))))

(deftest reordered-input-map-determinism
  (let [inputs [release proposal provenance]
        reordered (walk/postwalk
                   #(if (map? %) (into (sorted-map-by (fn [a b] (compare b a))) %) %)
                   inputs)
        before (pr-str reordered)
        original-product (apply yanks/propose-yank inputs)
        reordered-product (apply yanks/propose-yank reordered)]
    (is (= inputs reordered))
    (is (not= (pr-str inputs) before))
    (is (= expected-product original-product reordered-product))
    (is (= (pr-str original-product) (pr-str reordered-product)))
    (is (= before (pr-str reordered)))))

(deftest historical-lock-content-preserved
  (let [locked-release (-> historical-lock
                           (assoc :lock-id "lock:consumer/current" :version "1.2.0"
                                  :content-id "sha256:released-content"
                                  :provenance-ref "provenance:released"))
        content (assoc historical-content :version "1.2.0"
                       :content-id "sha256:released-content"
                       :provenance-ref "provenance:released")
        with-history (-> release
                         (update-in [:history :locks] conj locked-release)
                         (update-in [:history :contents] conj content))
        before (pr-str with-history)
        product (yanks/propose-yank with-history proposal provenance)]
    (is (= (assoc-in expected-product [:artifact :release] with-history) product))
    (is (= (:history with-history) (get-in product [:artifact :release :history])))
    (is (= historical-lock (get-in product [:artifact :release :history :locks 0])))
    (is (= historical-content (get-in product [:artifact :release :history :contents 0])))
    (is (= before (pr-str with-history)))))

(deftest changed-bindings-change-complete-product
  (doseq [[argument field value]
          [[0 :package "acme/http"] [0 :registry "mirror-public"]
           [0 :namespace "acme"] [0 :version "release-candidate.1"]
           [0 :content-id "sha256:changed"] [0 :provenance-ref "provenance:changed"]
           [1 :owner "other-maintainer"]
           [1 :replacement-guidance "No replacement exists; discontinue the affected operation."]
           [2 :source-id "source:changed"] [2 :input-id "input:changed"]
           [2 :policy-id "GOV10:other-policy-reference"] [2 :tool-id "tool:changed"]]]
    (testing (str argument " " field)
      (let [inputs (assoc-in [release proposal provenance] [argument field] value)
            before (pr-str inputs)
            product (apply yanks/propose-yank inputs)
            section (nth [:release :proposal :provenance] argument)]
        (is (= (assoc-in expected-product [:artifact section field] value) product))
        (is (not= expected-product product))
        (is (= before (pr-str inputs)))))))

(deftest missing-release-identity-or-owner
  (doseq [[argument field] [[0 :package] [0 :registry] [0 :namespace]
                            [0 :version] [1 :owner]]]
    (let [inputs (update [release proposal provenance] argument dissoc field)]
      (is (= (rejection "GOV10001" [(nth [:release :proposal] argument) field]
                        :missing-field)
             (apply yanks/propose-yank inputs)))))
  (is (= (rejection "GOV10001" [:proposal :owner] :invalid-field)
         (yanks/propose-yank release (assoc proposal :owner " ") provenance))))

(deftest missing-or-blank-reason
  (is (= (rejection "GOV10006" [:proposal :reason] :missing-field)
         (yanks/propose-yank release (dissoc proposal :reason) provenance)))
  (doseq [value [nil "" " \t\n"]]
    (is (= (rejection "GOV10006" [:proposal :reason] :invalid-field)
           (yanks/propose-yank release (assoc proposal :reason value) provenance)))))

(deftest absent-replacement-guidance
  (is (= (rejection "GOV10006" [:proposal :replacement-guidance] :missing-field)
         (yanks/propose-yank release (dissoc proposal :replacement-guidance) provenance)))
  (is (= (rejection "GOV10006" [:proposal :replacement-guidance] :invalid-field)
         (yanks/propose-yank release (assoc proposal :replacement-guidance " ") provenance))))

(deftest missing-resolution-behavior
  (is (= (rejection "GOV10006" [:proposal :resolution-behavior] :missing-field)
         (yanks/propose-yank release (dissoc proposal :resolution-behavior) provenance)))
  (doseq [value [nil {} {:new-selection :allowed :existing-locks :preserve}
                 {:new-selection :forbidden :existing-locks :erase}
                 {:new-selection :forbidden :existing-locks :preserve :emergency :approved}]]
    (is (= (rejection "GOV10006" [:proposal :resolution-behavior] :invalid-field)
           (yanks/propose-yank release (assoc proposal :resolution-behavior value) provenance)))))

(deftest content-provenance-and-history-are-required
  (doseq [[argument field] [[0 :content-id] [0 :provenance-ref]
                            [2 :source-id] [2 :input-id] [2 :policy-id] [2 :tool-id]]]
    (is (= (rejection "GOV10002" [(nth [:release :proposal :provenance] argument) field]
                      :missing-field)
           (apply yanks/propose-yank
                  (update [release proposal provenance] argument dissoc field)))))
  (doseq [path [[:history] [:history :locks] [:history :contents]]]
    (is (= (rejection "GOV10006" (into [:release] path) :missing-field)
           (yanks/propose-yank (if (= [:history] path)
                                (dissoc release :history)
                                (update release :history dissoc (last path)))
                              proposal provenance))))
  (is (= (rejection "GOV10001" [:release :history :locks 0 :lock-id] :missing-field)
         (yanks/propose-yank (update-in release [:history :locks 0] dissoc :lock-id)
                            proposal provenance)))
  (is (= (rejection "GOV10002" [:release :history :contents 0 :content-id] :missing-field)
         (yanks/propose-yank (update-in release [:history :contents 0] dissoc :content-id)
                            proposal provenance))))

(deftest deterministic-field-reasons
  (let [bad-release (dissoc release :package :content-id)
        bad-proposal (dissoc proposal :owner :reason :replacement-guidance :resolution-behavior)
        inputs [bad-release bad-proposal provenance]
        before (pr-str inputs)
        expected {:status :rejected
                  :diagnostics (mapv #(first (:diagnostics (apply rejection %)))
                                     [["GOV10001" [:release :package] :missing-field]
                                      ["GOV10002" [:release :content-id] :missing-field]
                                      ["GOV10001" [:proposal :owner] :missing-field]
                                      ["GOV10006" [:proposal :reason] :missing-field]
                                      ["GOV10006" [:proposal :replacement-guidance] :missing-field]
                                      ["GOV10006" [:proposal :resolution-behavior] :missing-field]])}]
    (is (= expected (apply yanks/propose-yank inputs) (apply yanks/propose-yank inputs)))
    (is (= before (pr-str inputs)))))

(deftest malformed-secret-marker-controls
  (let [marker "YANK_PROPOSAL_SECRET_MARKER"
        secret {:secret marker}
        cases [[[marker proposal provenance] "GOV10001" [:release]]
               [[release marker provenance] "GOV10001" [:proposal]]
               [[release proposal marker] "GOV10002" [:provenance]]
               [[release (assoc proposal :reason secret) provenance] "GOV10006" [:proposal :reason]]
               [[release (assoc proposal :owner secret) provenance] "GOV10001" [:proposal :owner]]
               [[release (assoc proposal :replacement-guidance secret) provenance]
                "GOV10006" [:proposal :replacement-guidance]]
               [[release (assoc proposal :resolution-behavior secret) provenance]
                "GOV10006" [:proposal :resolution-behavior]]
               [[(assoc release :version secret) proposal provenance] "GOV10001" [:release :version]]
               [[(assoc-in release [:history :locks] marker) proposal provenance]
                "GOV10006" [:release :history :locks]]
               [[(assoc-in release [:history :contents 0 :provenance-ref] secret) proposal provenance]
                "GOV10002" [:release :history :contents 0 :provenance-ref]]
               [[release proposal (assoc provenance :source-id secret)] "GOV10002" [:provenance :source-id]]
               [[(assoc release :metadata {marker :unknown-key}) proposal provenance]
                "GOV10006" [:release]]
               [[(assoc release :metadata (atom marker)) proposal provenance]
                "GOV10006" [:release]]]]
    (doseq [[inputs id path] cases]
      (let [;; Even well-shaped public context is omitted from a failed result.
            inputs (if (map? (nth inputs 2)) (assoc-in inputs [2 :input-id] marker) inputs)
            before (pr-str inputs)
            product (apply yanks/propose-yank inputs)]
        (is (= (rejection id path :invalid-field) product))
        (is (not (str/includes? (pr-str product) marker)))
        (is (= product (apply yanks/propose-yank inputs)))
        (is (= before (pr-str inputs)))))))

(deftest caller-supplied-approved-status-cannot-promote-proposal
  (doseq [status [:approved :accepted :published :yanked :complete]]
    (let [claimed (assoc proposal :status status :approval :approved
                        :action :yank :granted-capabilities #{:registry/yank})
          claimed-provenance (assoc provenance :status status :approval :approved)
          inputs [release claimed claimed-provenance]
          before (pr-str inputs)
          product (apply yanks/propose-yank inputs)]
      (is (= expected-product product))
      (is (= before (pr-str inputs)))))
  (is (= (rejection "GOV10006" [:proposal :reason] :missing-field)
         (yanks/propose-yank release (assoc (dissoc proposal :reason) :status :approved)
                            provenance))))
