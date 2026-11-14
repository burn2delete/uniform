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
                      "language/compiler/src/gravity/compiler/authenticated_envelope.gravity")]
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

(defn- invoke-current-envelope [function arguments]
  (#'gravity.self-hosting.sh02-authenticated-envelope-test/invoke-envelope
   function arguments))

(deftest current-owner-has-an-acyclic-function-call-graph
  ;; The complete owner must pass C7's structural recursion gate before body
  ;; inference can inspect it. This is a seed-plan regression, not SH acceptance.
  (let [plan @(deref #'gravity.self-hosting.sh02-authenticated-envelope-test/envelope-plan)
        calls (fn [definition]
                (loop [pending (vec (:instructions definition)) found #{}]
                  (if (empty? pending)
                    found
                    (let [instruction (peek pending)]
                      (recur
                       (into (pop pending)
                             (bootstrap/p15-s23-stage2-module-instruction-children
                              instruction))
                       (if (= :function-call (:op instruction))
                         (conj found (:function instruction))
                         found))))))
        graph (into {} (map (fn [[name definition]] [name (calls definition)]))
                    (:functions plan))
        recursive? (fn [start]
                     (loop [pending (vec (get graph start)) seen #{}]
                       (if (empty? pending)
                         false
                         (let [node (peek pending)]
                           (cond
                             (= start node) true
                             (contains? seen node) (recur (pop pending) seen)
                             :else (recur (into (pop pending) (get graph node))
                                          (conj seen node)))))))]
    (is (= [] (filterv recursive? (sort (keys graph)))))))

(deftest iterative-envelope-traversals-preserve-discovery-and-identity
  (let [refs (array-map {:digest-ref 2} [{:digest-ref 3} {:digest-ref 2}]
                        :later (list {:digest-ref 4}
                                     {:nested {:digest-ref 5}}))]
    (is (= [9 3 2 4 5]
           (invoke-current-envelope 'ae-collect-digest-refs [refs [9 3]])))
    (is (= [1 2 2 3 5 5]
           (invoke-current-envelope 'ae-sort-ordinals [[5 2 1 5 2 3] []])))
    (is (= [0 1 3 3 3 6]
           (invoke-current-envelope 'ae-insert-ordinal [[1 3 3 6] 3 [0] false])))
    (is (= [0 1 3 3 6]
           (invoke-current-envelope 'ae-insert-ordinal [[1 3 3 6] 3 [0] true]))))
  (doseq [[value expected]
          [[nil [:nil]] [false [:boolean false]] [true [:boolean true]]
           [-1 [:integer -1]] [17 [:integer 17]] ["17" [:string "17"]]
           ['item [:symbol 'item]] [:item [:keyword :item]]
           [\x [:character \x]] [1.5 [:unsupported-scalar 1.5]]
           [{:digest-ref 0} [:digest-ref 0]]
           [{:digest-ref -1} [:map #{[:entry [:keyword :digest-ref] [:integer -1]]}]]
           [{:digest-ref 1 :other 2}
            [:map #{[:entry [:keyword :digest-ref] [:integer 1]]
                    [:entry [:keyword :other] [:integer 2]]}]]
           [[] [:vector []]] ['() [:list []]] [#{} [:set #{}]] [{} [:map #{}]]
           [[1 :item] [:vector [[:integer 1] [:keyword :item]]]]
           ['(1 :item) [:list [[:integer 1] [:keyword :item]]]]
           [#{1 :item} [:set #{[:integer 1] [:keyword :item]}]]]]
    (is (= expected (invoke-current-envelope 'ae-digest-intern-key [value]))))
  (is (= (invoke-current-envelope 'ae-digest-intern-key [(array-map :first 1 :last 2)])
         (invoke-current-envelope 'ae-digest-intern-key [(array-map :last 2 :first 1)]))))

(deftest iterative-envelope-traversals-reject-late-and-nested-physical-input
  (doseq [value [(array-map :first [:valid] :last {:actual-path "/tmp/physical"})
                 {[:key {:workspace-root "/tmp/physical"}] :value}
                 [1 '(2 {:digest-ref 0})]
                 {:logical-source-path "../outside"}
                 {:file "physical.gravity"}]]
    (is (false? (invoke-current-envelope 'ae-path-neutral-semantic-value? [value]))))
  (doseq [value [{:digest-ref -1} {:digest-ref 0 :other :value}
                 {:logical-source-path "compiler/source.gravity"}
                 {:file (str "sha256:" (apply str (repeat 64 "a")))}]]
    (is (true? (invoke-current-envelope 'ae-path-neutral-semantic-value? [value]))))
  (is (true? (invoke-current-envelope 'ae-lowercase-hex? [(vec "0123456789abcdef")])))
  (is (false? (invoke-current-envelope 'ae-lowercase-hex? [(vec "0123456789abcdefX")]))))

(deftest iterative-envelope-traversals-use-data-depth-instead-of-call-depth
  ;; This depth is an executable regression input, not an accepted-data quota.
  (let [depth 512
        nest (fn [value] (nth (iterate vector value) depth))
        encoded (invoke-current-envelope 'ae-digest-intern-key [(nest :leaf)])]
    (is (= [19]
           (invoke-current-envelope 'ae-collect-digest-refs [(nest {:digest-ref 19}) []])))
    (is (false? (invoke-current-envelope 'ae-path-neutral-semantic-value?
                                        [(nest {:path "/tmp/physical"})])))
    (is (true? (invoke-current-envelope 'ae-path-neutral-semantic-value? [(nest :leaf)])))
    (is (= [:keyword :leaf]
           (loop [value encoded remaining depth]
             (if (zero? remaining)
               value
               (if (and (= :vector (first value)) (= 1 (count (second value))))
                 (recur (first (second value)) (dec remaining))
                 [:invalid-container remaining])))))))

(deftest digest-request-search-preserves-present-missing-and-empty-results
  (let [product (invoke-current-envelope 'authenticated-envelope-build-template [@descriptor])
        requests (:digest-requests product)
        last-request (last requests)
        preimage (:preimage last-request)]
    (is (= (:key last-request)
           (invoke-current-envelope 'ae-request-key-for-preimage [requests preimage])))
    (is (nil? (invoke-current-envelope 'ae-request-key-for-preimage
                                      [requests [::missing-preimage]])))
    (is (nil? (invoke-current-envelope 'ae-request-key-for-preimage [[] preimage])))))
