(ns gravity.self-hosting.sh07-current-set-q-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh07-current-set-order-test]))

;; Golden output from the genuine G product on 5cce4203. This is an expected
;; result, never a replacement for constructing/authenticating the Q product.
(def ^:private expected-g-core-id
  "sha256:3af34430488e5d4fa7bbf256d28f0793b996d5badad7f83a30fa14a0c104bdc1")
(def ^:private expected-fixture-hash
  "sha256:eb7f58bb76901cc5e7f27443fe565d20741a04daad0ec55c105501aab661ed3c")

(defn- helper [name]
  (ns-resolve 'gravity.self-hosting.sh07-current-set-order-test name))

(defn- fixture [extension]
  (.getCanonicalPath
   (io/file ((helper 'root))
            (str "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-collection-kind" extension))))

(defn- source-bytes [path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file path))))

(def observed-q-product (atom nil))

(deftest q-source-set-order-and-coherent-order-rejection
  (let [source-path (fixture ".qst")
        before (source-bytes source-path)
        source-hash (str "sha256:" (b/sha256-bytes-hex before))
        _ (is (= expected-fixture-hash source-hash))
        _ (is (java.util.Arrays/equals before (source-bytes (fixture ".gravity"))))
        artifact (b/sh07-public-core-file-artifact source-path {:reader :development-current})
        boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        request (:authenticated-core-request boundary)
        c2 (get-in artifact [:sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
                            :authenticated-sh04-artifact :c2-reader-artifact])
        source-index (into {} (map (juxt :form-id identity)) (:form-tree c2))
        nodes (into {} (map (juxt :node-id identity)) (:nodes core))
        mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
        forms (into {} (map (juxt :form-id identity)) (:forms request))]
    (is (= :accepted (:status artifact)))
    (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
    (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
    (is (= ".qst" (get-in c2 [:source-unit-record :extension])))
    (is (= source-path (get-in c2 [:source-unit-record :path]) (get-in artifact [:provenance :source-path])
           (get-in core [:provenance :actual-source-path])))
    (is (= source-hash (get-in c2 [:source-unit-record :bytes-hash])))
    (is (= expected-g-core-id (:artifact-id artifact) (:artifact-id core)))
    (doseq [name '[sh07-collection-kind? set-call-order nested-set-order]]
      (let [input ((helper 'source-function) (:form-tree c2) name)
            function ((helper 'one) (:function-records core) #(= name (:definition-name %)))]
        ((helper 'check-source-order!) source-index nodes ((helper 'source-body) source-index input)
         (nodes (:body-core-node-id function)))))
    (doseq [node (:nodes core)]
      (let [source (:source node) form (forms (:form-id source)) mapping (mappings (:node-id node))
            fields [:syntax-id :form-id :origin-chain :generated-origin]]
        (is (and (map? form) (map? mapping)))
        (is (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields)))
        (is (= (:source-span form) (:source-span mapping)))))
    (let [template (get-in boundary [:raw-template-result :core-template])
          function ((helper 'one) (:function-records core) #(= 'set-call-order (:definition-name %)))
          collection (nodes (:body-core-node-id function))
          target ((helper 'one) (:nodes template) #(= (get-in collection [:source :form-id]) (get-in % [:source :form-id])))
          children (:children target)
          reordered (vec (concat (rest children) [(first children)]))
          replacement (-> target (assoc :children reordered)
                          (assoc-in [:evaluation :order] (mapv (fn [index id] {:index index :core-node-id id}) (range) reordered)))
          changed (update template :nodes #(mapv (fn [node] (if (= (:node-id target) (:node-id node)) replacement node)) %))
          changed (update changed :evaluation-order #(mapv (fn [row] (if (= (:node-id target) (:core-node-id row))
                                                                     (assoc row :children reordered) row)) %))
          changed (update changed :identity-preimage assoc :nodes (:nodes changed) :evaluation-order (:evaluation-order changed))
          rejected (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
      (is (= :set (get-in collection [:attributes :literal-kind])))
      (is (not= children reordered))
      (is (= (set children) (set reordered)))
      (is (= reordered (mapv :core-node-id (get-in replacement [:evaluation :order]))))
      (is (= (:nodes changed) (get-in changed [:identity-preimage :nodes])))
      (is (= (:evaluation-order changed) (get-in changed [:identity-preimage :evaluation-order])))
      (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected)))
      (reset! observed-q-product {:artifact artifact :rejection rejected :expected-g-core-id expected-g-core-id}))
    (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending} (get-in artifact [:execution-boundary :downstream-fact-statuses])))
    (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
    (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
    (is (java.util.Arrays/equals before (source-bytes source-path)))))
