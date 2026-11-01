(ns gravity.self-hosting.sh09-evidence-metadata-test
  "Focused SH-09 cold-metadata semantics.

  The kernel retains its original assertions while using the cold fixture
  builder moved into shared support.  Loading this namespace does not load
  the SH-08 or SH-09 monoliths."
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- leaf
  [function & args]
  (apply kernel-support/leaf function args))

(def ^:private namespaces-at-load
  (set (map ns-name (all-ns))))

(deftest focused-evidence-load-isolation-development-check
  ;; This group must remain cheap and independently discoverable.
  (is (some? (find-ns 'gravity.self-hosting.sh08-function-call-kernel-support)))
  (is (not (contains? namespaces-at-load 'gravity.self-hosting.sh08-function-call-type-test)))
  (is (not (contains? namespaces-at-load 'gravity.self-hosting.sh09-body-effect-test))))

(deftest cold-quote-metadata-effects-development-kernel
  (let [{:keys [core facts record bindings forms input]} (kernel-support/cold-metadata-input)
        cut {:status :accepted :mode (get-in input [:cut :mode])
             :excluded-node-ids (get-in input [:cut :excluded-node-ids])}
        cold {:mode (:mode input) :cut cut
              :source-argument-call-node-ids (:source-argument-call-node-ids input)}
        run #(leaf 'body-is-cold-metadata-valid? core facts record bindings forms % cut)]
    (is (true? (run cold)))
    (doseq [changed (kernel-support/cold-metadata-invalid-inputs cold)]
      (is (false? (run changed))))))
