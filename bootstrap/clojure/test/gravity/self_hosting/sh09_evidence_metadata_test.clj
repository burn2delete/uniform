(ns gravity.self-hosting.sh09-evidence-metadata-test
  "Focused SH-09 cold-metadata semantics.

  The kernel retains its original assertions while using the cold fixture
  builder moved into shared support.  Loading this namespace does not load
  the SH-08 or SH-09 monoliths."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- leaf
  [function & args]
  (apply kernel-support/leaf function args))

(def ^:private monolithic-namespaces
  #{'gravity.self-hosting.sh08-function-call-type-test
    'gravity.self-hosting.sh08-recursive-function-type-test
    'gravity.self-hosting.sh09-body-effect-test})

(deftest focused-evidence-load-isolation-development-check
  ;; This group must remain cheap and independently discoverable.
  (let [loaded-before (set (map ns-name (all-ns)))
        {:keys [completed? exit output] :as probe} (kernel-support/fresh-sh09-load-probe)
        loaded-after (set (map ns-name (all-ns)))
        monoliths-loaded (fn [loaded]
                           (set (filter #(contains? loaded %) monolithic-namespaces)))]
    (is (= (monoliths-loaded loaded-before)
           (monoliths-loaded loaded-after))
        "The child probe must not mutate the parent's namespace set")
    (is completed? (pr-str probe))
    (is (= 0 exit) (pr-str probe))
    (is (str/includes? output "SH09-FOCUSED-LOAD-ISOLATION") (pr-str probe))))

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
