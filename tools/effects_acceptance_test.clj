(ns effects-acceptance-test
  "Pure adapter controls; no compiler, process, Git or broker execution."
  (:refer-clojure :exclude [identity])
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]
            [effects-acceptance :as acceptance]))

(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (:reason (ex-data error)))))

(def texts
  (merge
   (zipmap acceptance/resource-paths (repeat "; synthetic resource\n"))
   {acceptance/test-path
    (str "(ns " acceptance/test-namespace ")\n"
         "(deftest body-private-map-keys-authenticated-builder-composition\n"
         "  (compile-c8-module-bundle (path \"bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity\"))\n"
         "  (sh09-build-authenticated-body-effect-result typed verification binding)\n"
         "  (sh09-verify-authenticated-body-effect-result typed verification binding actual))\n"
         "(deftest body-module-vectors-have-no-cumulative-quota (is true))\n"
         "(deftest body-module-vectors-check-caller-and-every-producer (is true))\n")
    acceptance/effects-path
    (str "(defn build-body-effect-result [typed verification binding]\n"
         "  (body-analyze-producers entries))\n"
         "(defn verify-body-effect-result [typed verification binding candidate] candidate)")
    acceptance/consumer-path
    (str "(ns consumer (:requires [gravity.effects :as effects])\n"
         "  (:exports [sh09-build-authenticated-body-effect-result sh09-verify-authenticated-body-effect-result]))\n"
         "(defn sh09-build-authenticated-body-effect-result [typed verification binding]\n"
         "  (effects/build-body-effect-result typed verification binding))\n"
         "(defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]\n"
         "  (effects/verify-body-effect-result typed verification binding candidate))")}))

(def identity
  {:candidate {:commit (apply str (repeat 40 "a"))
               :tree (apply str (repeat 40 "b"))}
   :main {:commit (apply str (repeat 40 "c"))
          :tree (apply str (repeat 40 "d"))}
   :dependency-trees (zipmap acceptance/dependency-roots (repeat (apply str (repeat 40 "e"))))
   :resources (into (sorted-map)
                    (map (fn [[path text]] [path (acceptance/sha256 text)]) texts))})

(def passed
  {:schema :gravity/development-test-result-v1
   :authority :non-authoritative
   :authoritative? false
   :selection {:namespaces [acceptance/test-namespace]
               :test-vars (mapv #(str acceptance/test-namespace "/" %)
                                acceptance/selectors)
               :fail-fast? false}
   :summary {:test 3 :pass 3 :fail 0 :error 0}
   :outcome :passed
   :exit-code 0})

(defn plan []
  (acceptance/prepare identity identity texts))

(deftest current-selection-is-nonlaunching-and-binds-runner-receipt
  (let [prepared (plan)
        observation (acceptance/bind-result prepared identity identity passed)]
    (is (= :effects/current-selection-v1 (:schema prepared)))
    (is (= :none (:authority prepared)))
    (is (= :unrun (:execution prepared)))
    (is (= acceptance/selectors (:selectors prepared)))
    (is (= :effects/current-observation-v1 (:schema observation)))
    (is (= :passed (:outcome observation)))
    (is (false? (:semantic-acceptance observation)))))

(deftest pmk-selection-is-explicit-and-shape-regressions-remain-in-batch
  (let [prepared (acceptance/prepare-pmk identity identity texts)]
    (is (= acceptance/pmk-selectors (:selectors prepared)))
    (is (= 1 (count (filter #(= "--exact" %) (:command prepared)))))
    (is (= :selector
           (reason #(acceptance/prepare identity identity texts
                                        ["body-module-vectors-have-no-cumulative-quota"]))))))

(deftest source-route-and-runner-mismatches-refuse-binding
  (is (= :source-identity
         (reason #(acceptance/prepare identity
                                       (assoc identity :resources {}) texts))))
  (is (= :builder-route
         (let [changed (str/replace (texts acceptance/effects-path)
                                    "(body-analyze-producers entries)"
                                    "(body-module-vectors-valid? core facts entries)")
               observed (assoc texts acceptance/effects-path changed)
               pins (assoc identity :resources
                           (assoc (:resources identity) acceptance/effects-path
                                  (acceptance/sha256 changed)))]
           (reason #(acceptance/prepare pins pins observed)))))
  (is (= :selector
         (reason #(acceptance/bind-result
                   (assoc (plan) :selectors ["wrong-selector"])
                   identity identity passed))))
  (is (= :runner-result
         (reason #(acceptance/bind-result
                   (plan) identity identity (assoc passed :authoritative? true))))))

(deftest source-drift-and-incomplete-receipts-refuse
  (is (= :source-identity
         (reason #(acceptance/bind-result (plan) identity
                                          (assoc identity :candidate {}) passed))))
  (is (= :missing-runner-result
         (reason #(acceptance/bind-result (plan) identity identity nil))))
  (is (= :incomplete-execution
         (reason #(acceptance/bind-result
                   (plan) identity identity
                   (assoc passed :summary {:test 0 :pass 0 :fail 0 :error 0})))))
  (is (= :incomplete-execution
         (reason #(acceptance/bind-result
                   (plan) identity identity
                   (assoc passed :summary {:test 3 :pass 2 :fail 0 :error 0}))))))

(let [result (run-tests 'effects-acceptance-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
