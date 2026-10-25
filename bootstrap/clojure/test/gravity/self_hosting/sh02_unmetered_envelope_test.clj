(ns gravity.self-hosting.sh02-unmetered-envelope-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh02-authenticated-envelope-test]))

(def ^:private fixture
  (delay (#'gravity.self-hosting.sh02-authenticated-envelope-test/fixture-case
          "accepted/envelope-comparison.gravity")))
(def ^:private descriptor
  (delay (#'gravity.self-hosting.sh02-authenticated-envelope-test/descriptor @fixture)))
(def ^:private options {:source :development-current})
(defn- build [input]
  (bootstrap/p15-s23-stage2-sh02-descriptor-envelope
   :sh02-fixture :gravity/sh02-fixture-plan input (:source-path @fixture) options))
(defn- verify [original candidate]
  (bootstrap/p15-s23-stage2-sh02-descriptor-envelope-verify!
   candidate :sh02-fixture :gravity/sh02-fixture-plan original (:source-path @fixture) options))
(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest actual-current-source-builds-and-freshly-verifies-schema-two
  (let [input @descriptor before (pr-str input) product (build input)
        rule (:source-rule product) sealed (:sealed-artifact product)
        current-path (#'gravity.self-hosting.sh02-authenticated-envelope-test/path
                      "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")]
    (is (= 2 (:schema-version input)))
    (is (not (contains? input :bounds)))
    (is (= :accepted (:status product)))
    (is (= 1 (:schema-version product)))
    (is (= 2 (:schema-version sealed)))
    (is (not (contains? sealed :bounds)))
    (is (= :development-unadmitted (:admission-status rule)))
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp current-path)))
           (:source-content-hash rule)))
    (is (every? string? ((juxt :plan-semantic-hash :functions-semantic-hash
                               :builder-semantic-hash :verifier-semantic-hash) rule)))
    (is (= :passed (verify input product)))
    (is (= product (build input)))
    (is (= before (pr-str input)))))

(deftest current-source-selection-does-not-renew-default-admission
  (let [input @descriptor historical
        (failure #(bootstrap/p15-s23-stage2-sh02-descriptor-envelope
                   :sh02-fixture :gravity/sh02-fixture-plan input (:source-path @fixture)))]
    (is (map? historical))
    (is (= :error (:severity historical)))
    (is (not= :accepted (:status historical))))
  (doseq [selection [nil {:source :unknown} {:source :development-current :extra true}
                     {:reader :development-current}]]
    (is (map? (failure #(bootstrap/p15-s23-stage2-sh02-descriptor-envelope
                        :sh02-fixture :gravity/sh02-fixture-plan @descriptor
                        (:source-path @fixture) selection))))))

(deftest prior-schema-quota-fields-and-semantic-substitutions-fail
  (let [input @descriptor product (build input)]
    (doseq [invalid [(assoc input :schema-version 1) (assoc input :bounds {})
                     (assoc-in input [:fact-transitions 0 :output-count] -1)
                     (assoc-in input [:reference-closure :observed-node-count] 999)
                     (assoc-in input [:source-revision :logical-source-path] "../escape")]]
      (is (map? (failure #(build invalid)))))
    (doseq [candidate [(assoc-in product [:sealed-artifact :schema-version] 1)
                       (assoc-in product [:sealed-artifact :bounds] {})
                       (assoc-in product [:source-rule :source-content-hash] "sha256:bad")
                       (update product :source-rule dissoc :admission-status)
                       (assoc product :semantic-envelope-id (:provenance-binding-id product))]]
      (is (map? (failure #(verify input candidate)))))
    (let [altered (assoc-in input [:source-revision :logical-source-path] "other/logical-source")
          coherently-resealed (build altered)]
      (is (not= (:semantic-envelope-id product) (:semantic-envelope-id coherently-resealed)))
      (is (map? (failure #(verify input coherently-resealed)))))))

(deftest native-kind-unicode-and-lazy-carriers-remain-invalid
  (let [calls (atom 0)
        opaque (reify Object (toString [_] (swap! calls inc) (throw (ex-info "opaque" {}))))
        lazy-value (lazy-seq (swap! calls inc) (throw (ex-info "lazy" {})))
        input @descriptor]
    (doseq [invalid [opaque (assoc input :semantic-projections lazy-value)
                     (with-meta input {:foreign true})
                     (assoc-in input [:source-revision :logical-source-path] "bad\ud800")]]
      (is (map? (failure #(build invalid)))))
    (is (= 0 @calls))))
