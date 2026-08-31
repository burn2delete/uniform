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
     :source-kind :gravity/source :file-key "fixture-file-key"
     :last-modified-millis 1
     :sh06-semantic-projection-id
     "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"}))

(defn- request [path]
  (let [source-id (:source-bytes-sha256 (snapshot path))]
    {:artifact :gravity/sh07-authenticated-sh06-core-request
     :schema-version 15
     :lineage {:source-revision-id source-id
               :sh06-semantic-projection-id
               "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
               :authenticated-sh06-artifact-id "sh06-artifact"}
     :module {:namespace 'demo.core :profile :hosted :target :jvm :safety :safe
              :effects [:io] :capabilities [:filesystem] :exports ['main]
              :source-revision-id source-id}
     :provenance {:actual-source-path path}}))

(defn- run-product [path]
  (let [identity {:ordinal 0 :purpose :sh07-core-artifact-id
                  :preimage {:lineage {:authenticated-sh06-artifact-id "sh06-artifact"}}}
        provenance {:ordinal 1 :purpose :sh07-core-provenance-binding-id
                    :preimage {:actual-source-path path}}
        requests [identity provenance]
        resolved (mapv #(resolver (:purpose %) (:preimage %)) requests)]
    {:raw-template-result
     {:artifact :gravity/sh07-core-template-result
      :schema-version 16 :status :accepted
      :core-template {:provenance {:actual-source-path path}
                      :provenance-binding-preimage {:actual-source-path path}
                      :lineage {:authenticated-sh06-artifact-id "sh06-artifact"}}
      :digest-requests requests :diagnostics [] :bounds {}
      :containment {} :execution-boundary {}}
     :canonical-core-artifact
     {:artifact-id (first resolved)
      :identity-preimage (:preimage identity)
      :provenance {:actual-source-path path}
      :provenance-binding-preimage {:actual-source-path path}
      :provenance-binding-id (second resolved)}
     :digest-requests requests
     :resolved-digests resolved
     :template-verification
     {:artifact :gravity/sh07-core-template-verification
      :schema-version 16 :status :passed :rule nil :reason nil :details {}}
     :resolved-verification
     {:artifact :gravity/sh07-core-resolved-verification
      :schema-version 16 :status :passed :rule nil}}))

(def ^:dynamic *fixture-path* nil)

(defn- sh06 []
  {:artifact-id "sh06-artifact" :provenance {:source-path *fixture-path*}})
(defn- verification []
  {:artifact :gravity/sh06-resolution-artifact-verification
   :status :passed :checks {:artifact-current? true} :failed-checks []
   :source-path *fixture-path*})

(defn- boundary [sh06 request product]
  {:slice :SH-07 :owner :gravity-source :adapter-contract :adapter
   :plan-binding {} :authenticated-sh06-resolution-artifact sh06
   :authenticated-core-request request
   :raw-template-result (:raw-template-result product)
   :canonical-core-artifact (:canonical-core-artifact product)
   :digest-requests (:digest-requests product)
   :resolved-digests (:resolved-digests product)
   :template-verification (:template-verification product)
   :resolved-verification (:resolved-verification product)
   :authenticated-envelope-descriptor {}
   :authenticated-envelope {}
   :target-source-reread? false :clojure-adapter-residual? true
   :self-hosted? false})

(defn- seams []
  {:resolve-digests (fn [_ requests]
                      (mapv #(resolver (:purpose %) (:preimage %)) requests))
   :assemble-artifact
   (fn [sh06 request product]
     {:artifact-id (get-in product [:canonical-core-artifact :artifact-id])
      :sh06-resolution-artifact sh06
      :gravity-core-boundary (boundary sh06 request product)})
   :upstream-verification?
   (fn [sh06 report]
     (and (= (:artifact-id sh06) "sh06-artifact")
          (= report (verification))))
   :final-envelope?
   (fn [sh06 request product candidate]
     (or (= #{:raw-template-result :canonical-core-artifact :digest-requests
              :resolved-digests :template-verification :resolved-verification}
            (set (keys candidate)))
         (= candidate
            {:artifact-id (get-in product [:canonical-core-artifact :artifact-id])
             :sh06-resolution-artifact sh06
             :gravity-core-boundary (boundary sh06 request product)})))
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
    (binding [*fixture-path* path]
      (with-redefs [bootstrap/sh06-resolution-artifact-verification
                    (fn [_] (verification))
                    bootstrap/sh07-core-authenticated-request (fn [_] current-request)]
        (operation current-request)))))

(deftest live-pins-close-all-families-and-literal-request-schema
  (let [pins (carrier/live-pins)]
    (is (= #{:request-schema-version :sh03-c2-reader :sh04-c3-syntax
             :sh05-macro :sh06-resolution :sh07-core :runtime}
           (set (keys pins))))
    (is (= 15 (:request-schema-version pins)))
    (is (= #{:java-runtime-version :clojure-version}
           (set (keys (:runtime pins)))))))

(deftest cache-key-includes-the-complete-static-module-envelope
  (let [left (request "left.gravity")
        right (request "right.gravity")]
    (is (= (carrier/cache-key (snapshot "left.gravity") left)
           (carrier/cache-key (snapshot "right.gravity") right)))
    (is (not= (carrier/cache-key (snapshot "left.gravity") left)
              (carrier/cache-key (snapshot "left.gravity")
                                 (assoc-in left [:module :profile] :firmware))))))

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

(deftest carrier-construction-rejects-missing-slots-and-undeclared-paths
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"path-slot-mismatch"
           (carrier/construct-carrier
            (snapshot "left.gravity") (sh06) request
            (update-in (run-product "left.gravity")
                       [:canonical-core-artifact :provenance-binding-preimage]
                       dissoc :actual-source-path))))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"path-slot-mismatch"
           (carrier/construct-carrier
            (snapshot "left.gravity") (sh06) request
            (assoc-in (run-product "left.gravity")
                      [:canonical-core-artifact :unexpected-path]
                      "left.gravity"))))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"supplied-verification-failed"
           (carrier/construct-carrier
            (snapshot "left.gravity") (sh06) request
            (assoc-in (run-product "left.gravity")
                      [:template-verification "left.gravity"] true))))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"supplied-verification-failed"
           (carrier/construct-carrier
            (snapshot "left.gravity") (sh06) request
            (assoc (run-product "left.gravity") :resolved-digests [])))))))

(deftest carrier-construction-rejects-every-same-type-slot-alteration
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (doseq [address carrier/path-slot-addresses]
        (let [product (run-product "left.gravity")
              value (#'gravity.self-hosting.sh07-semantic-carrier/get-address
                     product address)
              altered (if (map? value)
                        (assoc value :unexpected true)
                        (str value "-altered"))]
          (is (thrown? clojure.lang.ExceptionInfo
                       (carrier/construct-carrier
                        (snapshot "left.gravity") (sh06) request
                        (#'gravity.self-hosting.sh07-semantic-carrier/assoc-address
                         product address altered)))
              (str address)))))))

(deftest supplied-verification-rejects-a-missing-declared-coordinate
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            missing (update-in (run-product "left.gravity")
                               [:canonical-core-artifact :provenance-binding-preimage]
                               dissoc :actual-source-path)
            report (carrier/verify-supplied-product
                    built (snapshot "left.gravity") (sh06) (verification)
                    request missing (seams))]
        (is (= :failed (:status report)))
        (is (some #{:path-slot-completeness} (:failed-checks report)))))))

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
                  product (get-in bound [:sh07-artifact :gravity-core-boundary])]
              (is (= :accepted (:status bound)))
              (is (= "right.gravity"
                     (get-in product [:canonical-core-artifact :provenance
                                      :actual-source-path])))
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

(deftest snapshot-and-upstream-verification-bind-the-current-request-and-artifact
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            forged (assoc (verification) :checks {:artifact-current? false})
            report (carrier/verify-supplied-product
                    built (snapshot "left.gravity") (sh06) forged request
                    (run-product "left.gravity") (seams))]
        (is (false? (get-in report [:checks :upstream-verification])))
        (is (some #{:upstream-verification} (:failed-checks report)))
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo #"sh06-verification-failed"
             (carrier/bind-carrier built (snapshot "left.gravity") (sh06)
                                   forged request (seams))))
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo #"source-mutated"
             (carrier/construct-carrier
              (assoc (snapshot "left.gravity") :source-path "other.gravity")
              (sh06) request (run-product "left.gravity"))))
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo #"invalid-selection"
             (carrier/cache-key (dissoc (snapshot "left.gravity")
                                        :source-bytes-sha256)
                                request)))))))

(deftest carrier-and-candidate-values-are-closed-and-metadata-free
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            malformed (assoc built :run-product (Object.))
            metadata-candidates
            [(with-meta (run-product "left.gravity") {:unexpected true})
             (with-meta (run-product "left.gravity") {})]]
        (is (false? (carrier/lookup-valid? malformed
                                           (snapshot "left.gravity") request)))
        (is (false? (carrier/lookup-valid? (with-meta built {:unexpected true})
                                           (snapshot "left.gravity") request)))
        (is (false? (carrier/lookup-valid? (with-meta built {})
                                           (snapshot "left.gravity") request)))
        (doseq [metadata-candidate metadata-candidates]
          (let [report (carrier/verify-supplied-product
                        built (snapshot "left.gravity") (sh06) (verification)
                        request metadata-candidate (seams))]
            (is (false? (get-in report [:checks :closed-schema])))
            (is (some #{:closed-schema} (:failed-checks report)))))))))

(deftest supplied-products-reject-extra-keys-sentinels-and-inexact-statuses
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            artifact (fn [product]
                       {:artifact-id "artifact"
                        :gravity-core-boundary (boundary (sh06) request product)})
            candidates
            [(assoc (run-product "left.gravity") :unexpected true)
             (update (artifact (run-product "left.gravity"))
                     :gravity-core-boundary assoc :unexpected true)
             (assoc-in (artifact (run-product "left.gravity"))
                       [:gravity-core-boundary
                        :authenticated-envelope-descriptor :source-revision-id]
                       "sha256:tampered")
             (assoc-in (run-product "left.gravity")
                       [:template-verification :details :sentinel]
                       carrier/path-slot-sentinel)
             (assoc-in (run-product "left.gravity")
                       [:raw-template-result :diagnostics]
                       [{:unexpected true}])
             (assoc-in (run-product "left.gravity")
                       [:template-verification :reason] :unexpected)
             (assoc-in (run-product "left.gravity")
                       [:resolved-verification :rule] "C6-VERIFY")
             (update (run-product "left.gravity") :resolved-digests pop)]]
        (doseq [candidate candidates]
          (let [report (carrier/verify-supplied-product
                        built (snapshot "left.gravity") (sh06) (verification)
                        request candidate (seams))]
            (is (= :failed (:status report)) (pr-str candidate))))))))

(deftest rebound-empty-aggregates-have-no-metadata
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            bound (carrier/bind-carrier built (snapshot "left.gravity") (sh06)
                                        (verification) request (seams))
            product (get-in bound [:sh07-artifact :gravity-core-boundary])]
        (is (nil? (meta (get-in product [:raw-template-result :diagnostics]))))
        (is (nil? (meta (get-in product [:template-verification :details]))))))))

(deftest every-live-pin-family-invalidates-a-carrier-hit
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            pin-spec @#'gravity.self-hosting.sh07-semantic-carrier/pin-spec]
        (doseq [[family symbols] pin-spec]
          (let [pin-var (ns-resolve 'gravity.bootstrap (first symbols))]
            (is (false?
                 (with-redefs-fn
                   {pin-var [:mutated @pin-var]}
                   #(carrier/lookup-valid? built
                                           (snapshot "left.gravity") request)))
                (name family))))
        (is (false?
             (with-redefs [clojure.core/clojure-version (constantly "changed")]
               (carrier/lookup-valid? built (snapshot "left.gravity") request)))
            "runtime pin family")))))

(deftest static-module-envelope-closes-every-field-and-canonicalizes-policy-order
  (let [base (-> (request "left.gravity")
                 (assoc-in [:module :effects] [:state :io])
                 (assoc-in [:module :capabilities] [:network :filesystem]))
        baseline (carrier/cache-key (snapshot "left.gravity") base)
        mutations
        [[:namespace (assoc-in base [:module :namespace] 'other.core)]
         [:profile (assoc-in base [:module :profile] :firmware)]
         [:target (assoc-in base [:module :target] :wasm)]
         [:safety (assoc-in base [:module :safety] :unsafe)]
         [:effects (assoc-in base [:module :effects] [:io])]
         [:capabilities (assoc-in base [:module :capabilities] [:filesystem])]
         [:exports (assoc-in base [:module :exports] ['other])]]]
    (doseq [[field changed] mutations]
      (is (not= baseline (carrier/cache-key (snapshot "left.gravity") changed))
          (name field)))
    (let [reordered
          (carrier/cache-key
           (snapshot "left.gravity")
           (-> base
               (update-in [:module :effects] #(vec (reverse %)))
               (update-in [:module :capabilities] #(vec (reverse %)))))]
      (is (= (get-in baseline [1 :module-policy])
             (get-in reordered [1 :module-policy])))
      (is (= [:io :state]
             (get-in baseline [1 :module-policy :effects])))
      (is (= [:filesystem :network]
             (get-in baseline [1 :module-policy :capabilities])))
      (is (not= baseline reordered)
          "the complete neutral request still preserves exact authenticated order"))))

(deftest source-snapshot-schema-is-exact-and-typed
  (let [request (request "left.gravity")
        valid (snapshot "left.gravity")
        malformed
        (concat
         [(assoc valid :unexpected true)]
         (map #(dissoc valid %)
              [:source-path :source-bytes :source-byte-count
               :source-bytes-sha256 :source-extension :source-kind :file-key
               :last-modified-millis :sh06-semantic-projection-id])
         [(assoc valid :source-path :not-a-string)
          (assoc valid :source-bytes "not-bytes")
          (assoc valid :source-byte-count -1)
          (assoc valid :source-bytes-sha256 "sha256:bad")
          (assoc valid :source-extension ".txt")
          (assoc valid :source-kind :other/source)
          (assoc valid :file-key nil)
          (assoc valid :last-modified-millis "one")
          (assoc valid :sh06-semantic-projection-id nil)])]
    (doseq [candidate malformed]
      (is (thrown? clojure.lang.ExceptionInfo
                   (carrier/cache-key candidate request))
          (pr-str (dissoc candidate :source-bytes))))))

(defn- reseal-carrier [value]
  (let [counted
        (#'gravity.self-hosting.sh07-semantic-carrier/fixpoint-carrier
         (assoc value :integrity-hash nil))]
    (assoc counted :integrity-hash
           (#'gravity.self-hosting.sh07-semantic-carrier/final-integrity counted))))

(deftest top-level-carrier-tampering-fails-as-data-without-throwing
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request (run-product "left.gravity"))
            resealed
            [(reseal-carrier (assoc built :status :rejected))
             (reseal-carrier (assoc built :authority :authoritative))
             (reseal-carrier (assoc-in built [:nonclaims :authoritative?] true))
             (reseal-carrier (assoc-in built [:semantic-key :unexpected] true))
             (reseal-carrier
              (assoc-in built [:semantic-key :pins :request-schema-version] 16))
             (reseal-carrier
              (assoc-in built [:semantic-key :pins :runtime :clojure-version]
                        "changed"))
             (reseal-carrier (assoc-in built [:static-envelope :unexpected] true))]
            candidates (conj resealed
                             (assoc built :canonical-byte-count "invalid")
                             (assoc built :integrity-hash "sha256:bad"))]
        (doseq [candidate candidates]
          (is (false? (carrier/lookup-valid? candidate
                                             (snapshot "left.gravity") request)))
          (let [report (carrier/verify-supplied-product
                        candidate (snapshot "left.gravity") (sh06)
                        (verification) request (run-product "left.gravity")
                        (seams))]
            (is (= :failed (:status report)))
            (is (or (false? (get-in report [:checks :carrier-integrity]))
                    (false? (get-in report [:checks :live-pins]))))))))))

(deftest unrelated-nil-and-path-looking-strings-remain-valid-data
  (with-authenticated-fixture
    "left.gravity"
    (fn [request]
      (let [product (-> (run-product "left.gravity")
                        (assoc-in [:canonical-core-artifact :optional-value] nil)
                        (assoc-in [:canonical-core-artifact :documentation]
                                  "other/root/example.gravity"))
            built (carrier/construct-carrier (snapshot "left.gravity") (sh06)
                                             request product)]
        (is (true? (carrier/lookup-valid? built
                                          (snapshot "left.gravity") request)))
        (is (nil? (get-in built [:run-product :canonical-core-artifact
                                 :optional-value])))
        (is (= "other/root/example.gravity"
               (get-in built [:run-product :canonical-core-artifact
                              :documentation])))))))
