(ns gravity.evolution.advisory-drafts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.evolution.advisory-drafts :as drafts]))

(def complete-input
  {:id "ADV-demo-17" :revision "r2" :owner "public-fixture-maintainer"
   :subject {:package "demo/parser" :registry "fixture-registry"
             :namespace "demo"}
   :affected-versions [">=release-a <release-c" "vendor-branch"]
   :patched-versions ["release-c"]
   :severity "high (supplied assessment)"
   :impact "An untrusted document can disclose parser state."
   :mitigations ["Disable remote document ingestion."]
   :provenance {:source-id "public-source:r2" :input-id "public-incident:17"
                :policy-id "GOV10/GOV4" :tool-id "public-fixture-tool:r1"
                :references ["public-report:17" "public-patch:17"]
                :assessment {:reference "public-assessment:17"}}
   :history [{:revision "r0" :state :withdrawn
              :reason "Incorrect affected-version declaration."
              :provenance ["public-report:16"]}
             {:revision "r1" :state :draft
              :embargo {:evidence-ref "public-disclosure-log:17"}
              :patch {:evidence-ref "public-patch:17"}
              :backport {:evidence-ref "public-backport:17"}
              :release {:evidence-ref "public-release:17"}}]})

;; A complete independent expected product, rather than a projection computed
;; with the producer's field list or canonicalization.
(def complete-product
  {:status :draft :diagnostics []
   :artifact
   {:schema :gravity.evolution/advisory-draft-v1
    :kind :gravity/security-advisory-draft :status :draft
    :authority :non-admitting :version-declarations :unauthenticated-claims
    :advisory
    {:id "ADV-demo-17" :revision "r2" :owner "public-fixture-maintainer"
     :subject {:package "demo/parser" :registry "fixture-registry"
               :namespace "demo"}
     :affected-versions [">=release-a <release-c" "vendor-branch"]
     :patched-versions ["release-c"]
     :severity "high (supplied assessment)"
     :impact "An untrusted document can disclose parser state."
     :mitigations ["Disable remote document ingestion."]
     :provenance {:source-id "public-source:r2" :input-id "public-incident:17"
                  :policy-id "GOV10/GOV4" :tool-id "public-fixture-tool:r1"
                  :references ["public-report:17" "public-patch:17"]
                  :assessment {:reference "public-assessment:17"}}
     :history [{:revision "r0" :state :withdrawn
                :reason "Incorrect affected-version declaration."
                :provenance ["public-report:16"]}
               {:revision "r1" :state :draft
                :embargo {:evidence-ref "public-disclosure-log:17"}
                :patch {:evidence-ref "public-patch:17"}
                :backport {:evidence-ref "public-backport:17"}
                :release {:evidence-ref "public-release:17"}}]}}})

(defn rejection [path reason]
  {:status :rejected
   :diagnostics [{:id "GOV10006"
                  :message "Invalid explicit advisory draft input"
                  :path path :reason reason}]})

(defn complete-render [x]
  (binding [*print-length* nil *print-level* nil *print-readably* true
            *print-meta* true *print-dup* false *print-namespace-maps* false]
    (pr-str x)))

(defn assert-product [input expected]
  (let [before (complete-render input)
        actual (drafts/create-draft input)]
    (is (= expected actual))
    (is (= before (complete-render input)) "Producer must leave input unchanged")
    actual))

(deftest complete-advisory-payload
  (assert-product complete-input complete-product))

(deftest changed-impact-changes-product
  (let [impact "An untrusted document can disclose credentials."
        input (assoc complete-input :impact impact)
        expected (assoc-in complete-product [:artifact :advisory :impact] impact)]
    (assert-product input expected)
    (is (not= complete-product (drafts/create-draft input)))
    (assert-product complete-input complete-product)))

(deftest retained-history-and-provenance
  (let [history (conj (:history complete-input)
                      {:revision "r2" :state :corrected
                       :patch {:claim :unverified :references ["public-new-patch:18"]}
                       :release {:existing-locks ["public-lock:old"]}})
        provenance (assoc (:provenance complete-input)
                          :references ["public-report:18" "public-patch:18"])
        input (assoc complete-input :history history :provenance provenance)
        expected (-> complete-product
                     (assoc-in [:artifact :advisory :history] history)
                     (assoc-in [:artifact :advisory :provenance] provenance))]
    (assert-product input expected)
    (assert-product complete-input complete-product)))

(defn reverse-maps [x]
  (cond
    (map? x) (into (sorted-map-by #(compare %2 %1))
                   (map (fn [[k v]] [k (reverse-maps v)])) x)
    (vector? x) (mapv reverse-maps x)
    :else x))

(deftest reordered-map-determinism
  (let [reordered (reverse-maps complete-input)
        expected (drafts/create-draft complete-input)
        actual (assert-product reordered complete-product)]
    (is (= (complete-render expected) (complete-render actual)))
    (doseq [settings [{:length 1 :level 1 :readably false}
                      {:length nil :level nil :readably true}]]
      (let [under-bindings
            (binding [*print-length* (:length settings)
                      *print-level* (:level settings)
                      *print-readably* (:readably settings)
                      *print-meta* true *print-namespace-maps* true]
              (drafts/create-draft reordered))]
        (is (= complete-product under-bindings))
        (is (= (complete-render actual) (complete-render under-bindings)))))))

(deftest missing-affected-versions
  (assert-product (dissoc complete-input :affected-versions)
                  (rejection [:advisory :affected-versions] :missing-field)))

(deftest missing-patched-version-declaration
  (assert-product (dissoc complete-input :patched-versions)
                  (rejection [:advisory :patched-versions] :missing-field)))

(deftest missing-severity
  (assert-product (dissoc complete-input :severity)
                  (rejection [:advisory :severity] :missing-field)))

(deftest missing-impact
  (assert-product (dissoc complete-input :impact)
                  (rejection [:advisory :impact] :missing-field)))

(deftest missing-mitigations
  (assert-product (dissoc complete-input :mitigations)
                  (rejection [:advisory :mitigations] :missing-field)))

(deftest missing-provenance
  (assert-product (dissoc complete-input :provenance)
                  (rejection [:advisory :provenance] :missing-field)))

(deftest identity-subject-and-provenance-are-required
  (doseq [field [:id :revision :owner :subject :history]]
    (assert-product (dissoc complete-input field)
                    (rejection [:advisory field] :missing-field)))
  (doseq [field [:package :registry]]
    (assert-product (update complete-input :subject dissoc field)
                    (rejection [:advisory :subject field] :missing-field)))
  (doseq [field [:source-id :input-id :policy-id :tool-id :references]]
    (assert-product (update complete-input :provenance dissoc field)
                    (rejection [:advisory :provenance field] :missing-field))))

(deftest malformed-required-fields
  (doseq [[field values]
          [[:id [nil "" "  " :id]] [:revision [nil "" 2]]
           [:owner [nil "" []]] [:subject [nil [] "package"]]
           [:affected-versions [nil [] [""] '("release-a")]]
           [:patched-versions [nil "release-c" [""]]]
           [:severity [nil "" :high]] [:impact [nil "  " []]]
           [:mitigations [nil [] [""] "disable"]]
           [:provenance [nil [] "public-ref"]]
           [:history [nil {} ["not-a-record"]]]]]
    (doseq [value values]
      (testing (str "Malformed field " field)
        (assert-product (assoc complete-input field value)
                        (rejection [:advisory field] :invalid-field)))))
  (doseq [[parent fields]
          [[:subject [:package :registry]]
           [:provenance [:source-id :input-id :policy-id :tool-id :references]]]
          field fields]
    (assert-product (assoc-in complete-input [parent field] nil)
                    (rejection [:advisory parent field] :invalid-field))))

(deftest multiple-errors-have-stable-field-order
  (let [input (-> complete-input
                  (dissoc :id :impact)
                  (update :subject dissoc :package :registry)
                  (update :provenance dissoc :source-id :references))
        paths [[:advisory :id] [:advisory :subject :package]
               [:advisory :subject :registry] [:advisory :impact]
               [:advisory :provenance :source-id]
               [:advisory :provenance :references]]
        expected {:status :rejected
                  :diagnostics (mapv #(first (:diagnostics
                                              (rejection % :missing-field))) paths)}]
    (assert-product input expected)
    (assert-product (reverse-maps input) expected)))

(deftest explicit-empty-patched-declaration-and-history
  (let [input (assoc complete-input :patched-versions [] :history [])
        expected (-> complete-product
                     (assoc-in [:artifact :advisory :patched-versions] [])
                     (assoc-in [:artifact :advisory :history] []))]
    (assert-product input expected)))

(deftest version-declarations-are-preserved-without-interpretation
  (let [affected ["vendor-z" ">=opaque-branch" "vendor-z"]
        patched ["older-label" "unknown-order"]
        input (assoc complete-input :affected-versions affected :patched-versions patched)
        expected (-> complete-product
                     (assoc-in [:artifact :advisory :affected-versions] affected)
                     (assoc-in [:artifact :advisory :patched-versions] patched))]
    (assert-product input expected)))

(deftest malformed-secret-marker
  (let [marker "SYNTHETIC-SECRET-MARKER"]
    (doseq [[input path]
            [[(assoc complete-input :impact {:private marker}) [:advisory :impact]]
             [(assoc-in complete-input [:provenance :references] [nil marker])
              [:advisory :provenance :references]]
             [(assoc complete-input :history [{marker "unknown-key"}])
              [:advisory :history]]
             [(assoc complete-input (keyword marker) (atom marker)) [:advisory]]]]
      (let [actual (assert-product input (rejection path :invalid-field))]
        (is (not (str/includes? (complete-render actual) marker)))))))

(deftest mutable-host-input
  (let [number (java.util.concurrent.atomic.AtomicLong. 7)
        host-map (java.util.HashMap. {:package "demo/parser" :registry "fixture-registry"})
        host-list (java.util.ArrayList. ["public-reference"])
        cases [[(assoc-in complete-input [:history 0 :mutable] number) [:advisory :history]]
               [(assoc complete-input :subject host-map) [:advisory :subject]]
               [(assoc-in complete-input [:provenance :references] host-list)
                [:advisory :provenance :references]]
               [(java.util.HashMap. complete-input) [:advisory]]]
        before (mapv (fn [[input path]]
                       (assert-product input (rejection path :invalid-field))) cases)]
    (is (= 7 (.get number)))
    (is (= {:package "demo/parser" :registry "fixture-registry"} (into {} host-map)))
    (is (= ["public-reference"] (vec host-list)))
    (.set number 9)
    (.put host-map :package "SYNTHETIC-SECRET-MARKER")
    (.add host-list "SYNTHETIC-SECRET-MARKER")
    (doseq [[[input path] previous] (map vector cases before)]
      (is (= (rejection path :invalid-field) previous))
      (assert-product input previous))))

(deftest caller-published-state-cannot-promote-draft
  (doseq [claims [{:status :published :published true :authority :admitting
                   :approval :accepted :grants [:registry/publish]}
                  {:status :released :artifact {:status :published}
                   :version-declarations :verified}]]
    (assert-product (merge complete-input claims) complete-product)))

(defrecord HostRecord [reference])

(deftest carrier-rejects-host-values-and-metadata
  (doseq [value [(atom "public") (->HostRecord "public") (Object.)
                 (constantly "public") (map identity ["public"])
                 'public '("public") #{"public"}
                 (with-meta {:reference "public"} {:hidden "data"})
                 (with-meta ["public"] {:hidden "data"})]]
    (assert-product (assoc-in complete-input [:history 0 :extension] value)
                    (rejection [:advisory :history] :invalid-field)))
  (doseq [input [nil [] "not-a-map" (->HostRecord "public")
                 (with-meta complete-input {:hidden "data"})]]
    (assert-product input (rejection [:advisory] :invalid-field))))

(deftest immutable-leaves-and-map-vector-representations
  (doseq [value [nil true false "public" :public
                 (byte 1) (short 2) (int 3) (long 4) (float 1.5) (double 2.5)
                 (java.math.BigInteger. "17") (java.math.BigDecimal. "1.25")
                 19N 2/3
                 (array-map :reference "public")
                 (hash-map :reference "public")
                 (sorted-map :reference "public")
                 (subvec ["discarded" "retained"] 1)]]
    (let [input (assoc-in complete-input [:history 0 :extension] value)
          expected (assoc-in complete-product [:artifact :advisory :history 0 :extension] value)]
      (assert-product input expected))))
