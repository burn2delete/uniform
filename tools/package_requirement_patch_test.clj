(ns package-requirement-patch-test
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is run-tests]]))

(load-file "tools/package_requirement_patch.clj")
(require '[package-requirement-patch :as patch])

(def inputs (patch/immutable-inputs))
(def manifest (first inputs))
(def snapshot (second inputs))
(def proposal (edn/read-string (slurp patch/artifact-path)))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(defn check [p] (patch/validate! p manifest snapshot))

(deftest exact-family-conservation
  (is (check proposal))
  (is (= 12 (count (:source-documents proposal))))
  (is (= 70 (count (:units proposal))))
  (is (= 1255 (count (:source-facets proposal))))
  (is (= 355 (count (:source-dispositions proposal))))
  (is (= 128 (count (:edits proposal))))
  (is (= 5 (count (:unresolved-ids proposal))))
  (is (every? #(= :unassessed (:current-acceptance %)) (:units proposal)))
  (is (not-any? #(= patch/yank-id (:id %)) (:edits proposal)))
  (is (= 4 (count (get-in (edn/read-string (slurp "specs/build-workflow/yank-interface.edn"))
                          [:source-facets])))))

(deftest guarded-application-refuses-stale-and-duplicate
  (let [edits (:edits proposal)
        applied (patch/apply-edits manifest edits)]
    (is (rejects? #(patch/apply-edits applied edits)))
    (is (rejects? #(patch/apply-edits manifest
                    [(assoc (first edits) :id "behavior/pkg1/unknown")]))))
  (is (rejects? #(patch/apply-edits manifest
                  [(assoc (first (:edits proposal)) :before-present? true)])))
  (is (rejects? #(patch/apply-edits manifest
                  [(assoc (first (:edits proposal)) :before :wrong)]))))

(deftest source-and-negative-condition-loss-refuses
  (is (rejects? #(check (update proposal :source-documents pop))))
  (is (rejects? #(check (assoc-in proposal [:source-documents 0 :current-sha256] "wrong"))))
  (is (rejects? #(check (assoc-in proposal [:source-facets 0 :raw-sha256] "wrong"))))
  (is (rejects? #(check (assoc-in proposal [:source-facets 0 :source-occurrence-id] "PKG1/source/invented"))))
  (let [negative (first (keep-indexed #(when (= :negative-acceptance (:kind %2)) %1)
                                     (:source-facets proposal)))]
    (is (some? negative))
    (is (rejects? #(check (update proposal :source-facets
                                  (fn [fs] (vec (concat (subvec fs 0 negative) (subvec fs (inc negative)))))))))))

(deftest task-and-context-substitution-refuses
  (doseq [[k value] [[:path "specs/build-workflow/renamed.md"] [:sha256 "wrong"]
                     [:marker "migration-tasks"] [:id "T001"]]]
    (is (rejects? #(check (assoc-in proposal [:units 0 :task-references 0 k] value)))))
  ;; A private/latent registry retention refinement cannot become an unqualified
  ;; generic yank alias; its source context and central adjudication survive.
  (is (rejects? #(check (assoc-in proposal [:equivalence-proposals 2 :kind] :alias))))
  (is (rejects? #(check (assoc-in proposal [:units 0 :current-acceptance] :accepted)))))

(deftest local-projections-preserve-the-declared-historical-slice
  (let [projection (edn/read-string (slurp (get-in patch/input-projections [:manifest :path])))
        ids (:global-requirement-ids manifest)]
    (is (= :gravity/read-only-package-input-projection (:kind projection)))
    (is (= :historical-proposal-input-only (:authority projection)))
    (is (= :ids-only (get-in projection [:coverage :other-requirements])))
    (is (= 1231 (count ids) (count (set ids))))
    (is (= (set (get-in projection [:coverage :full-requirement-ids]))
           (set (map :id (:requirements manifest)))))
    (is (every? (set ids) (map :id (:requirements manifest)))))
  (doseq [input (vals patch/input-projections)]
    (let [original slurp]
      (with-redefs [clojure.core/slurp
                    (fn [path] (str (original path) (when (= path (:path input)) " ")))]
        (is (rejects? patch/immutable-inputs))))))

(deftest source-commit-tree-and-conserved-receipt-substitution-refuse
  (doseq [changed [(assoc snapshot :source-commit "not-a-git-id")
                   (assoc snapshot :source-commit (apply str (repeat 40 "0")))
                   (assoc snapshot :source-commit (:source-tree snapshot))
                   (assoc snapshot :source-tree (:source-commit snapshot))]]
    (is (rejects? #(patch/prepare manifest changed))))
  (let [original slurp]
    (doseq [path [patch/yank-receipt-path
                  (:path (first (:source-documents proposal)))]]
      (with-redefs [clojure.core/slurp
                    (fn [candidate] (str (original candidate) (when (= path candidate) " ")))]
        (is (rejects? #(check proposal)))))))

(deftest positive-check-needs-only-the-reachable-source-identity
  (let [original shell/sh
        allowed #{["git" "cat-file" "-t" (:source-commit snapshot)]
                  ["git" "rev-parse" "--verify" (str (:source-commit snapshot) "^{tree}")]}
        calls (atom [])]
    (with-redefs [shell/sh (fn [& args]
                            (swap! calls conj (vec args))
                            (when-not (contains? allowed (vec args))
                              (throw (ex-info "Historical Git object access is unavailable" {})))
                            (apply original args))]
      (let [[m s] (patch/immutable-inputs)]
        (is (patch/validate! proposal m s))))
    (is (= allowed (set @calls)))))

(let [r (run-tests)]
  (shutdown-agents)
  (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
