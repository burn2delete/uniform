(ns gravity.self-hosting.sh07-semantic-carrier-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-semantic-carrier :as carrier])
  (:import [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(defn- sha256 [^bytes bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest bytes)
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff)) (.digest digest))))))

(defn- resolver
  ([purpose preimage] (resolver purpose preimage []))
  ([purpose preimage _resolved]
   (bootstrap/reader-canonical-hash
    {:domain :gravity/sh07-declared-digest-v1
     :purpose purpose :preimage preimage})))

(defn- snapshot [path]
  (let [bytes (.getBytes "(ns demo.core)" StandardCharsets/UTF_8)]
    {:source-path path :source-bytes bytes :source-byte-count (alength bytes)
     :source-bytes-sha256 (sha256 bytes) :source-extension ".gravity"
     :source-kind :gravity/source :sh06-semantic-projection-id "sh06-projection"}))

(defn- request [path]
  {:artifact :gravity/sh07-authenticated-sh06-core-request
   :schema-version 15
   :lineage {:source-revision-id "revision"
             :sh06-semantic-projection-id "sh06-projection"
             :authenticated-sh06-artifact-id "sh06-artifact"}
   :module {:namespace 'demo.core :profile :hosted :target :jvm :safety :safe
            :effects [:io] :capabilities [:filesystem] :exports ['main]}
   :provenance {:actual-source-path path}})

(defn- run-product [path]
  (let [identity {:ordinal 0 :purpose :sh07-core-artifact-id
                  :preimage {:lineage {:authenticated-sh06-artifact-id "sh06-artifact"}}}
        provenance {:ordinal 1 :purpose :sh07-core-provenance-binding-id
                    :preimage {:actual-source-path path}}
        requests [identity provenance]
        resolved (mapv #(resolver (:purpose %) (:preimage %)) requests)]
    {:raw-template-result
     {:core-template {:provenance {:source-path path}
                      :provenance-binding-preimage {:actual-source-path path}
                      :lineage {:authenticated-sh06-artifact-id "sh06-artifact"}}
      :digest-requests requests}
     :canonical-core-artifact
     {:provenance {:source-path path}
      :provenance-binding-preimage {:actual-source-path path}
      :provenance-binding-id (second resolved)}
     :digest-requests requests
     :resolved-digests resolved
     :template-verification {:status :passed}
     :resolved-verification {:status :passed}}))

(defn- sh06 [] {:artifact-id "sh06-artifact"})
(defn- verification []
  {:artifact :gravity/sh06-resolution-artifact-verification
   :status :passed :failed-checks []})

(defn- seams []
  {:resolve-digest resolver
   :digest-rebinding? (fn [_ product]
                         (= (:resolved-digests product)
                            (mapv #(resolver (:purpose %) (:preimage %))
                                  (:digest-requests product))))
   :artifact-identity? (constantly true)
   :provenance-identity? (constantly true)
   :lineage? (constantly true)
   :no-alias? (fn [stored bound]
                 (not (identical? (:run-product stored) bound)))})

(defn- with-authenticated-fixture [path operation]
  (let [current-request (request path)]
    (with-redefs [bootstrap/sh06-resolution-artifact-verification (fn [_] (verification))
                  bootstrap/sh07-core-authenticated-request (fn [_] current-request)]
      (operation current-request))))

(deftest live-pins-close-all-families-and-literal-request-schema
  (let [pins (carrier/live-pins)]
    (is (= #{:request-schema-version :sh03-c2-reader :sh04-c3-syntax
             :sh05-macro :sh06-resolution :sh07-core :runtime}
           (set (keys pins))))
    (is (= 15 (:request-schema-version pins)))
    (is (= #{:java-runtime-version :clojure-version}
           (set (keys (:runtime pins)))))))

(deftest construct-carrier-neutralizes-exactly-eleven-slots-and-closes-shape
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            neutral (:run-product built)]
        (is (= :gravity/sh07-semantic-run-product-carrier-v2 (:artifact built)))
        (is (= :non-authoritative (:authority built)))
        (is (= 11 (count (:path-slot-addresses built))))
        (is (every? #(= carrier/path-slot-sentinel
                         (#'gravity.self-hosting.sh07-semantic-carrier/get-address neutral %))
                    (:path-slot-addresses built)))
        (is (= carrier/path-slot-sentinel
               (get-in neutral [:raw-template-result :core-template :provenance])))
        (is (= carrier/path-slot-sentinel
               (get-in neutral [:canonical-core-artifact :provenance-binding-id])))
        (is (not (re-find #"left\\.gravity" (pr-str built))))
        (is (= (:canonical-byte-count built) (carrier/carrier-byte-count built)))))))

(deftest carrier-integrity-and-pins-fail-closed
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo #"carrier-integrity"
             (carrier/bind-carrier (assoc built :integrity-hash "sha256:bad")
                                   (snapshot "left.gravity") (sh06) (verification)
                                   request (seams))))
        (with-redefs [carrier/live-pins (constantly {:stale true})]
          (is (thrown-with-msg?
               clojure.lang.ExceptionInfo #"source-mutated"
               (carrier/bind-carrier built (snapshot "left.gravity") (sh06)
                                     (verification) request (seams)))))))))

(deftest bind-carrier-rebinds-second-path-without-aggregate-aliasing
  (with-authenticated-fixture
    "left.gravity"
    (fn [left-request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             left-request (run-product "left.gravity"))]
        (with-authenticated-fixture
          "right.gravity"
          (fn [right-request]
            (let [candidate (run-product "right.gravity")
                  preflight (carrier/verify-supplied-product
                             built (snapshot "right.gravity") (sh06) (verification)
                             right-request candidate (seams))
                  _ (is (= :passed (:status preflight)) (:checks preflight))
                  bound (carrier/bind-carrier built (snapshot "right.gravity") (sh06)
                                              (verification) right-request (seams))
                  product (:bound-run-product bound)]
              (is (= :accepted (:status bound)))
              (is (= "right.gravity"
                     (get-in product [:canonical-core-artifact :provenance :source-path])))
              (is (= "sh06-artifact"
                     (get-in product [:digest-requests 0 :preimage :lineage
                                      :authenticated-sh06-artifact-id])))
              (is (not (identical? (:run-product built) product)))
              (is (= :passed (get-in bound [:supplied-verification :status]))))))))))

(deftest supplied-verification-requires-each-integration-predicate
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            candidate (run-product "left.gravity")
            report (carrier/verify-supplied-product built (snapshot "left.gravity")
                                                    (sh06) (verification) request candidate)]
        (is (= :failed (:status report)))
        (is (= [:digest-rebinding :artifact-identity :provenance-identity
                :lineage :no-alias]
               (filterv (set (:failed-checks report))
                        [:digest-rebinding :artifact-identity :provenance-identity
                         :lineage :no-alias])))))))
