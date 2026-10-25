(ns gravity.self-hosting.sh07-selected-owner-core-join-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

;; These pins identify the historical association records derived from actual
;; accepted captures. They are test expectations, not compiler/admission keys.
(def ^:private association-pins
  {:bounds-data "sha256:5ba8423286d91f140398c3e9df6631e19e936a41f7ba5c798ec9bacb4fd17398"
   :reader-quote "sha256:4cbcf237d07887fccaeb3cde93709cbad13a9564201df82886caf2261957d073"
   :collection-kind "sha256:14d162f12281aa45f91ec226d27a742672d86761bfc43f6080f589edfb87d1ca"
   :literal-cluster "sha256:0b5c00bd405b94e7eba3ec31a1cfb194c45a82b87088359b5e7107e1c5d4162b"})

(def ^:private report-path
  "docs/artifacts/phase-15/reports/sh07-c01-selected-owner-use-map-v1.edn")

(defn- sha [text]
  (str "sha256:" (format "%064x" (java.math.BigInteger. 1
    (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes ^String text "UTF-8"))))))

(defn- stable [value]
  (cond
    (map? value) [:map (mapv (fn [[k v]] [(stable k) (stable v)]) (sort-by (comp pr-str key) value))]
    (set? value) [:set (mapv stable (sort-by pr-str value))]
    (vector? value) [:vector (mapv stable value)]
    (seq? value) [:list (mapv stable value)]
    :else value))

(defn- fail! [reason]
  (throw (ex-info "C01 historical Core association mismatch"
                  {:id "SH07-C01-CORE-JOIN" :reason reason :authority :none})))

(defn- require! [passed? reason] (when-not passed? (fail! reason)))

(defn- read-report []
  (with-open [reader (java.io.PushbackReader. (io/reader report-path :encoding "UTF-8"))]
    (let [eof (Object.) value (edn/read {:eof eof} reader)]
      (require! (identical? eof (edn/read {:eof eof} reader)) :report-eof)
      value)))

(defn- historical [revision path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str revision ":" path))]
    (require! (zero? exit) :historical-source-unavailable) out))

(defn- region [text {:keys [byte-start byte-end]}]
  ;; Governing source and fixtures in these retained captures are ASCII.
  (require! (and (string? text) (integer? byte-start) (integer? byte-end)
                 (<= 0 byte-start byte-end (count text))) :source-span)
  (subs text byte-start byte-end))

(defn- assets [cases]
  {:historical (into {} (for [c cases :let [{:keys [path tested-revision]} (:owner c)]]
                          [[tested-revision path] (historical tested-revision path)]))
   :current (into {} (for [c cases :let [path (get-in c [:owner :path])]] [path (slurp path :encoding "UTF-8")]))
   :fixtures (into {} (for [c cases key [:gravity :qst] :let [path (get-in c [:fixture key])]]
                         [path (slurp path :encoding "UTF-8")]))})

(defn- validate-case! [c inputs]
  (let [{:keys [owner fixture declarations core execution]} c
        old (get-in inputs [:historical [(:tested-revision owner) (:path owner)]])
        current (get-in inputs [:current (:path owner)])
        g (get-in inputs [:fixtures (:gravity fixture)]) q (get-in inputs [:fixtures (:qst fixture)])]
    (require! (and (= :accepted-for-scope (:status c))
                   (= :extracted-original-body (:origin-scope c))
                   (false? (:full-original-module-c5-authority? c))) :authority-scope)
    (require! (and old (= (:sha256 owner) (sha old)) (= (:bytes owner) (count old))
                   (= (:tested-revision owner) (:base-revision execution))) :owner-source)
    (require! (and g (= g q) (= (:sha256 fixture) (sha g)) (= (:bytes fixture) (count g))) :fixture-source)
    (require! (and (seq declarations) (= (count declarations) (count (set (map :name declarations))))) :declarations)
    (doseq [d declarations
            :let [original (region old (:owner-span d)) extracted (region g (:fixture-span d))
                  form (binding [*read-eval* false] (read-string original))
                  definition (:core-definition d) function (:core-function d)
                  projected (:projected-declaration d) name (:name d)]]
      (require! (and (= original extracted) (= (:raw-sha256 d) (sha original))
                     (= name (second form))
                     (str/includes? current original)) :owner-declaration)
      (require! (= (:source-unit-id fixture) (:fixture-source-unit-id d)) :fixture-occurrence)
      (require! (= (:fixture-span d) (select-keys (:source-span projected) [:byte-start :byte-end])) :projected-origin)
      (require! (and (= name (:name definition))
                     (= (:syntax-id definition) (:syntax-id projected))
                     (or (nil? function)
                         (and (= name (:definition-name function))
                              (= (:binding-id definition) (:definition-binding-id function))
                              (= (:core-node-id definition) (:definition-core-node-id function))
                              (= (:value-node-id definition) (:function-core-node-id function))))) :core-definition))
    (doseq [occurrence (:observed-c2-occurrences c)]
      (require! (= (:raw occurrence) (region g (:span occurrence))) :c2-source-occurrence))
    (require! (and (seq (:observed-c2-occurrences c))
                   (= :passed (:template-status core) (:resolved-status core))
                   (pos-int? (:node-count core))
                   (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id core))) :core-context)
    (when-let [capture (:additional-c2-capture execution)]
      (require! (and (= (:artifact-id core) (:canonical-core-id capture))
                     (= :same-complete-core-and-source-unit-different-execution-context (:association capture))
                     (= 2147483648 (:heap-max capture)) (= 120000 (:execution-timeout-ms capture))
                     (<= (:execution-elapsed-ms capture) 120000)) :c2-capture-context))
    (require! (and (= 0 (get-in execution [:focused-counts :fail]) (get-in execution [:focused-counts :error]))
                   (= 2147483648 (:heap-max execution)) (= 120000 (:execution-timeout-ms execution))
                   (<= (:execution-elapsed-ms execution) 120000)
                   (seq (:owning-negatives c))
                   (every? #(= [:rejected "C6-VERIFY" :template-replay]
                               ((juxt :status :rule :reason) (:result %))) (:owning-negatives c))) :actual-outcome)
    (require! (= (association-pins (:id c)) (sha (pr-str (stable c)))) :captured-context)
    {:status :passed :authority :none :scope :supplied-record-association-only}))

(deftest accepted-slices-join-original-source-fixture-occurrences-and-captured-core
  (let [report (read-report) supplement (:source-to-core-evidence report)
        cases (:cases supplement) inputs (assets cases)]
    (is (= :mapped-selected-plans (:status report)))
    (is (= :selected-seed-plan-potential-use (:scope report)))
    (is (= :accepted-extracted-original-slices-full-module-open (:status supplement)))
    (is (= :supplied-record-association-only (:validation-role supplement)))
    (is (false? (:full-original-module-c5-authority? supplement)))
    (is (= :unchanged-open-in-existing-map (:uncovered-occurrences supplement)))
    (is (= (set (keys association-pins)) (set (map :id cases))))
    (is (= 7 (reduce + (map #(count (:declarations %)) cases))))
    (doseq [c cases]
      (testing (str (:id c))
        (is (= {:status :passed :authority :none :scope :supplied-record-association-only}
               (validate-case! c inputs)))))
    (is (= [:declaration-roots :declaration-roots :original-set-and-children :character-occurrences-only]
           (mapv :c2-observation-scope cases)))))

(deftest mismatched-associations-never-become-compiler-or-module-authority
  (let [cases (get-in (read-report) [:source-to-core-evidence :cases]) inputs (assets cases)
        rejected (fn [candidate]
                   (try (validate-case! candidate inputs) nil
                        (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (doseq [c cases
            mutation [(assoc c :full-original-module-c5-authority? true)
                      (assoc-in c [:owner :tested-revision] "stale-source")
                      (assoc-in c [:owner :path] "bootstrap/gravity/src/gravity/foreign.gravity")
                      (assoc-in c [:fixture :sha256] (get-in c [:owner :sha256]))
                      (assoc-in c [:declarations 0 :name] 'different-function)
                      (update-in c [:declarations 0 :owner-span :byte-start] inc)
                      (assoc-in c [:declarations 0 :fixture-source-unit-id] "sha256:wrong-fixture")
                      (update-in c [:observed-c2-occurrences 0 :span :byte-start] inc)
                      (assoc-in c [:observed-c2-occurrences 0 :raw] "different-source-occurrence")
                      (update-in c [:declarations 0 :projected-declaration :source-span :byte-end] dec)
                      (assoc-in c [:declarations 0 :core-definition :binding-id] "sha256:wrong-binding")
                      (assoc-in c [:core :artifact-id] (str "sha256:" (apply str (repeat 64 "0"))))
                      (assoc-in c [:execution :host-source-subtree] "different-compiler")
                      (assoc-in c [:execution :products :sha256] "different-capture")
                      (assoc-in c [:owning-negatives 0 :result :status] :passed)]]
      (is (= "SH07-C01-CORE-JOIN" (:id (rejected mutation))))
      (is (= :none (:authority (rejected mutation)))))
    ;; A different valid case's identifiers cannot be spliced into this one.
    (doseq [[left right] (partition 2 1 cases)]
      (is (contains? #{:captured-context :c2-capture-context}
                     (:reason (rejected (assoc left :core (:core right)))))))))
