(ns gravity.self-hosting.sh02-sh06-authenticated-plan-context-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh02_sh06_authenticated_plan_context_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-02--SH-06 plan-context test is not on the classpath"
                {:id "SH02-SH06-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH02-SH06-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private exact-plan-contract-identities
  {:sh02
   {:stage :sh02
    :module 'gravity.compiler.authenticated-envelope
    :source-byte-count 59495
    :source-content-hash
    "sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71"
    :plan-id
    "sha256:ba3e5452a2775af96e480196b830448505d15af80be27503f9915d3c4299a5db"}
   :sh03
   {:stage :sh03
    :module 'gravity.bootstrap.reader
    :source-byte-count 257161
    :source-content-hash
    "sha256:bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77"
    :plan-id
    "sha256:c6ef36bdf9616b97ff92ded8786b21eab1f2f94075a87aa96f59b8e7d710b3f2"}
   :sh04
   {:stage :sh04
    :module 'gravity.bootstrap.syntax
    :source-byte-count 81241
    :source-content-hash
    "sha256:afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553"
    :plan-id
    "sha256:07178c6b5b68b9b17bbd3bd4144aa3e4b97171fa6bda6494410cb339677f0adc"}
   :sh05
   {:stage :sh05
    :module 'gravity.macro
    :source-byte-count 56488
    :source-content-hash
    "sha256:19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549"
    :plan-id
    "sha256:382fd0afe5e271a96d006f69cc76d40a67a68f0ef8abd091c133271b1f7c3e93"}
   :sh06
   {:stage :sh06
    :module 'gravity.resolution
    :source-byte-count 77209
    :source-content-hash
    "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56"
    :plan-id
    "sha256:a85dd6c53929b8a9f9b4fdf9d7b8b3b5c92fd06bacccf282ea036f9738cc13ee"}})

(defn- current-bindings []
  {:sh03 (bootstrap/sh03-reader-current-binding! "<sh02-sh06-context-test>")
   :sh04 (bootstrap/sh04-syntax-current-binding! "<sh02-sh06-context-test>")
   :sh05 (bootstrap/sh05-macro-current-binding! "<sh02-sh06-context-test>")
   :sh06 (bootstrap/sh06-resolution-current-binding!
          "<sh02-sh06-context-test>")})

(deftest sh02-through-sh06-closed-table-and-real-plans-join-exactly
  (let [contract-keys
        [:stage :module :source-byte-count :source-content-hash :plan-id]
        contracts
        (into {}
              (map (juxt :stage identity))
              (bootstrap/p15-s23-stage2-pinned-augmented-plan-contracts))]
    (is (= (set (keys exact-plan-contract-identities))
           (set (keys contracts))))
    (doseq [[stage expected] exact-plan-contract-identities]
      (let [contract (get contracts stage)]
        (is (= expected (select-keys contract contract-keys)) stage)
        (is (bootstrap/p15-s23-stage2-pinned-augmented-plan-contract?
             contract)
            stage)))
    (doseq [[stage binding] (current-bindings)]
      (let [plan (:plan binding)
            contract (get contracts stage)]
        (is (= (:source-byte-count contract)
               (get-in plan [:source :byte-count]))
            stage)
        (is (= (:source-content-hash contract)
               (get-in plan [:source :sha256]))
            stage)
        (is (= (:plan-id contract) (:plan-id plan)) stage)
        (is (bootstrap/p15-s23-stage2-pinned-augmented-plan-authorized?
             plan)
            stage)))))

(defn- assert-live-sh06-artifact [relative]
  (let [source-path (path relative)
        artifact (bootstrap/sh06-resolution-file-artifact source-path)
        verification
        (bootstrap/sh06-resolution-artifact-verification artifact)
        boundary (:gravity-resolution-boundary artifact)
        plan-binding (:plan-binding boundary)
        envelope (:authenticated-envelope boundary)]
    (is (= :gravity/sh06-resolution-artifact (:kind artifact)) relative)
    (is (= :accepted (:status artifact)) relative)
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact))
        relative)
    (is (= :passed (:status verification)) relative)
    (is (empty? (:failed-checks verification)) relative)
    (is (= 77209 (:source-byte-count plan-binding)) relative)
    (is (= "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56"
           (:source-content-hash plan-binding))
        relative)
    (is (= "sha256:ba8c292ffe3c703bbc220af0a7121496c134793d1e80a012f6fb0a0d9dc6b6fa"
           (:plan-semantic-hash plan-binding))
        relative)
    (is (= :gravity/sh06-authenticated-c4-resolution-request
           (get-in boundary [:authenticated-resolution-request :artifact]))
        relative)
    (is (= :accepted (:status envelope)) relative)
    (is (= :c5-resolution (:stage envelope)) relative)))

(deftest live-hello-and-lane-a-fixture-reach-authenticated-sh06
  (testing "public hello source crosses the real SH-02--SH-06 plans"
    (assert-live-sh06-artifact "examples/hello.gravity"))
  (testing "Lane-A success fixture crosses the same authenticated plans"
    (assert-live-sh06-artifact
     (str "bootstrap/clojure/fixtures/self-hosting/sh-07/"
          "b51-vector-destructuring-v18/accepted/"
          "nested-empty-wildcard-let.gravity"))))
