(ns effects-acceptance-test
  "Synthetic transport controls; no compiler or Gravity execution."
  (:refer-clojure :exclude [identity])
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [effects-acceptance :as acceptance]))

(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))

(def texts
  (merge (zipmap acceptance/resource-paths (repeat "; synthetic resource\n"))
    {acceptance/test-path
     (str "(ns " acceptance/test-namespace ")\n"
          (apply str (map #(str "(deftest " % " (is true))\n") acceptance/selectors)))
     acceptance/effects-path
     "(defn build-body-effect-result [typed verification binding]
        (if (body-module-vectors-valid? core facts (get producers :entries))
          :synthetic-accepted (body-error typed :malformed-body-module-vectors {})))
      (defn verify-body-effect-result [t v b c] c)"
     acceptance/consumer-path
     "(defn sh09-build-authenticated-body-effect-result [typed verification binding]
        (effects/build-body-effect-result typed verification binding))
      (defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]
        (effects/verify-body-effect-result typed verification binding candidate))"}))
(def identity
  {:candidate {:commit (apply str (repeat 40 "a")) :tree (apply str (repeat 40 "b"))}
   :main {:commit (apply str (repeat 40 "c")) :tree (apply str (repeat 40 "d"))}
   :dependency-trees (zipmap acceptance/dependency-roots (repeat (apply str (repeat 40 "e"))))
   :resources (into {} (map (fn [[p s]] [p (acceptance/sha256 s)]) texts))})
(defn plan [] (acceptance/prepare identity identity texts))
(def passed
  {:schema :gravity/development-test-result-v1 :authority :non-authoritative
   :authoritative? false
   :selection {:namespaces [acceptance/test-namespace]
               :test-vars (mapv #(str acceptance/test-namespace "/" %) acceptance/selectors)
               :fail-fast? false}
   :summary {:test 5 :pass 24 :fail 0 :error 0} :outcome :passed :exit-code 0})

(deftest preparation-is-nonlaunching-and-pins-selection
  (let [p (plan)]
    (is (= :unrun-held (:execution p)))
    (is (= :none (:authority p)))
    (is (= acceptance/selectors (:selectors p)))
    (is (= (vec (sort acceptance/selectors)) acceptance/selectors))
    (is (= 5 (count (filter #{"--exact"} (:command p)))))
    (is (= :passed (:outcome (acceptance/bind-result p identity identity passed))))
    (is (false? (:semantic-acceptance (acceptance/bind-result p identity identity passed))))))

(deftest missing-or-wrong-source-and-dependency-identities-reject
  (doseq [bad [(dissoc identity :candidate)
               (assoc-in identity [:candidate :commit] nil)
               (update identity :dependency-trees dissoc "contracts")]]
    (is (= :missing-identity (reason #(acceptance/prepare bad bad texts)))))
  (doseq [key [:candidate :main :dependency-trees :resources]]
    (is (= :source-identity (reason #(acceptance/prepare identity (assoc identity key {}) texts)))))
  (is (= :resource-identity
         (reason #(acceptance/prepare identity identity (assoc texts acceptance/effects-path "changed")))))
  (is (= :source-identity
         (reason #(acceptance/bind-result (plan) identity (assoc identity :resources {}) passed)))))

(defn prepare-changed [path change]
  (let [changed (update texts path change)
        pins (assoc-in identity [:resources path] (acceptance/sha256 (get changed path)))]
    (acceptance/prepare pins pins changed)))

(deftest wrong-selector-namespace-consumer-and-builder-route-reject
  (doseq [[path from to expected]
          [[acceptance/test-path (first acceptance/selectors) "unrelated" :missing-selector]
           [acceptance/test-path acceptance/test-namespace "unrelated" :namespace]
           [acceptance/consumer-path "effects/build-body-effect-result" "effects/unrelated" :consumer-route]
           [acceptance/effects-path "body-module-vectors-valid?" "unrelated" :builder-route]
           [acceptance/effects-path "core facts" "facts core" :builder-route]
           [acceptance/effects-path ":malformed-body-module-vectors" ":wrong-refusal" :builder-diagnostic]]]
    (is (= expected (reason #(prepare-changed path (fn [s] (str/replace s from to)))))))
  (is (= :selector (reason #(acceptance/bind-result (assoc (plan) :selectors ["unrelated"]) identity identity passed))))
  (is (= :consumer (reason #(acceptance/bind-result (assoc (plan) :consumers {}) identity identity passed)))))

(deftest driver-errors-and-real-failures-cannot-be-misattributed
  (is (= :missing-runner-result (reason #(acceptance/bind-result (plan) identity identity nil))))
  (doseq [[bad expected]
          [[(assoc-in passed [:selection :test-vars] ["wrong/test"]) :selector]
           [(assoc-in passed [:summary :test] 0) :incomplete-execution]
           [(assoc-in passed [:summary :fail] 1) :misattributed-result]]]
    (is (= expected (reason #(acceptance/bind-result (plan) identity identity bad)))))
  (let [failed (assoc passed :summary {:test 5 :pass 23 :fail 1 :error 0} :outcome :failed :exit-code 1)]
    (is (= :failed (:outcome (acceptance/bind-result (plan) identity identity failed))))
    (is (= failed (:runner (acceptance/bind-result (plan) identity identity failed))))))

(deftest altered-plan-and-runner-authority-never-bind
  (is (= :missing-identity
         (reason #(acceptance/bind-result (assoc (plan) :identity {}) {} {} passed))))
  (doseq [bad [(assoc (plan) :authority :accepted)
               (assoc (plan) :execution :passed)
               (assoc (plan) :command ["other-command"])]]
    (is (= :plan (reason #(acceptance/bind-result bad identity identity passed)))))
  (doseq [bad [(assoc passed :authority :authoritative)
               (assoc passed :authoritative? true)
               (dissoc passed :authoritative?)]]
    (is (= :runner-result (reason #(acceptance/bind-result (plan) identity identity bad))))))

(deftest git-snapshot-binds-real-bytes-and-rejects-uncommitted-dependencies
  (let [root (.toFile (java.nio.file.Files/createTempDirectory
                       "effects-acceptance-control-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        put! (fn [path text]
               (let [file (io/file root path)] (io/make-parents file) (spit file text)))]
    (doseq [[path text] texts] (put! path text))
    (doseq [path acceptance/dependency-roots] (put! (str path "/control.edn") "{}"))
    (acceptance/git root "init" "--quiet")
    (acceptance/git root "add" ".")
    (acceptance/git root "-c" "user.name=Control" "-c" "user.email=control@example.invalid"
                    "commit" "--quiet" "-m" "Synthetic Effects snapshot")
    (let [head (acceptance/git root "rev-parse" "HEAD")
          observed (acceptance/snapshot root head)]
      (is (= head (get-in observed [:candidate :commit])))
      (is (= (:resources identity) (:resources observed)))
      (is (= :unrun-held (:execution (acceptance/prepare observed observed texts))))
      (put! acceptance/effects-path (str (texts acceptance/effects-path) "\n"))
      (is (= :dirty-dependency (reason #(acceptance/snapshot root head))))
      (put! acceptance/effects-path (texts acceptance/effects-path))
      (put! "bootstrap/clojure/src/untracked.clj" "(ns untracked)")
      (is (= :dirty-dependency (reason #(acceptance/snapshot root head)))))))

(let [result (run-tests 'effects-acceptance-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
