(ns gravity.self-hosting.sh07-b51-attempt30-integration-mutation-bounds-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-integration-support
             :as support]
            [gravity.tools.sh07-b51-attempt30-independent-census
             :as independent-census]
            [gravity.tools.sh07-b51-attempt30-producer-census
             :as producer-census]))

(def fabricated-digest
  (str "sha256:" (apply str (repeat 64 "d"))))

(defn- canonical-id [value]
  (bootstrap/p15-s23-sh07-b51-canonical-id value))

(defn- normalize-transcript [transcript checker inputs]
  (assoc transcript :artifact-id
         (canonical-id
          {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
           :schema-version 1
           :artifact (:artifact transcript)
           :checker checker :inputs inputs
           :node-count (:node-count transcript)
           :row-count (:row-count transcript)
           :record-root (:record-root transcript)})))

(defn- normalize-certificate [certificate]
  (let [role (:role certificate)
        checker (:checker certificate)
        inputs (:inputs certificate)
        raw (normalize-transcript
             (:raw-source-transcript certificate) checker inputs)
        m2 (normalize-transcript (:m2-transcript certificate) checker inputs)
        provenance (assoc (:provenance certificate)
                          :raw-transcript-artifact-id (:artifact-id raw)
                          :m2-transcript-artifact-id (:artifact-id m2))
        claim-preimage
        {:domain :gravity/sh07-b51-common-census-claim-id-v1
         :schema-version 1
         :architecture-report-sha256
         (:architecture-report-sha256 certificate)
         :inputs inputs
         :assumptions (:assumptions certificate)
         :claim (:claim certificate)
         :profile-bounds (:profile-bounds certificate)}
        evidence-domain
        ({:producer-census
          :gravity/sh07-b51-producer-census-evidence-root-v1
          :independent-verifier-census
          :gravity/sh07-b51-independent-verifier-census-evidence-root-v1}
         role)
        base
        (assoc certificate
               :claim-id (canonical-id claim-preimage)
               :raw-source-transcript raw
               :m2-transcript m2
               :provenance provenance)
        evidence-preimage
        {:domain evidence-domain :schema-version 1
         :checker checker :inputs inputs
         :raw-source-transcript raw :m2-transcript m2
         :result (:result base)
         :reproduction-command-id (:reproduction-command-id provenance)
         :profile-bounds (:profile-bounds base)}
        with-evidence (assoc base :evidence-root
                             (canonical-id evidence-preimage))]
    (assoc with-evidence :certificate-id
           (canonical-id (dissoc with-evidence :certificate-id)))))

(defn- nested-vector [depth]
  (loop [remaining depth value 0]
    (if (zero? remaining)
      value
      (recur (dec remaining) [value]))))

(defn- certificate-node-shape [node-count]
  ;; 63 collections: root, two groups, and sixty leaf vectors.  The remaining
  ;; nodes are scalar leaves distributed without exceeding width 35.
  (let [leaf-nodes (- node-count 63)
        base (quot leaf-nodes 60)
        remainder (mod leaf-nodes 60)
        leaves (mapv (fn [ordinal]
                       (vec (repeat (+ base (if (< ordinal remainder) 1 0))
                                    0)))
                     (range 60))]
    [(subvec leaves 0 30) (subvec leaves 30 60)]))

(defn- certificate-collection-shape [collection-count]
  (let [leaf-count (- collection-count 8)
        base (quot leaf-count 7)
        remainder (mod leaf-count 7)]
    (mapv (fn [ordinal]
            (vec (repeat (+ base (if (< ordinal remainder) 1 0)) [])))
          (range 7))))

(defn- canonical-reader-byte-count [value]
  (alength (.getBytes
            (pr-str (bootstrap/reader-canonical-value value)) "UTF-8")))

(defn- certificate-canonical-byte-shape [target]
  (let [empty-shape [(vec (repeat 32 "")) (vec (repeat 32 ""))]
        content-bytes (- target (canonical-reader-byte-count empty-shape))
        lengths
        (loop [remaining content-bytes slots 64 result []]
          (if (zero? slots)
            result
            (let [length (min 4094 remaining)]
              (recur (- remaining length) (dec slots)
                     (conj result length)))))
        strings (mapv #(apply str (repeat % "a")) lengths)]
    [(subvec strings 0 32) (subvec strings 32 64)]))

(deftest attempt30-certificate-six-bounds-admit-exactly-at-limit
  (let [width-35 (into {} (map (fn [ordinal]
                                 [(keyword (str "k" ordinal)) ordinal])
                               (range 35)))
        width-36 (assoc width-35 :plus-one true)
        nodes-2048 (certificate-node-shape 2048)
        nodes-2049 (certificate-node-shape 2049)
        collections-64 (certificate-collection-shape 64)
        collections-65 (certificate-collection-shape 65)
        depth-12 (nested-vector 12)
        depth-13 (nested-vector 13)
        scalar-4096 (apply str (repeat 4094 "a"))
        scalar-4097 (str scalar-4096 "a")
        bytes-262144 (certificate-canonical-byte-shape 262144)
        bytes-262145 (certificate-canonical-byte-shape 262145)]
    (is (= [262144 262145]
           (mapv canonical-reader-byte-count
                 [bytes-262144 bytes-262145])))
    (doseq [[label at-limit plus-one]
            [["width 35/36" width-35 width-36]
             ["nodes 2048/2049" nodes-2048 nodes-2049]
             ["collections 64/65" collections-64 collections-65]
             ["depth 12/13" depth-12 depth-13]
             ["scalar UTF-8 4096/4097" scalar-4096 scalar-4097]
             ["canonical bytes 262144/262145"
              bytes-262144 bytes-262145]]]
      (is (true?
           (bootstrap/p15-s23-sh07-b51-v4-certificate-preflight? at-limit))
          label)
      (is (not
           (bootstrap/p15-s23-sh07-b51-v4-certificate-preflight? plus-one))
          label))))

(deftest attempt30-carrier-profiler-rejects-each-practical-first-plus-one
  (let [bounds {:maximum-nodes 4 :maximum-depth 1 :maximum-width 3
                :maximum-collection-count 1 :maximum-scalar-utf8 3}
        value ["a" "b" "c"]]
    (is (= {:nodes 4 :depth 1 :width 3 :collection-count 1
            :scalar-utf8 3}
           (bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile
            value bounds)))
    (doseq [[field lower]
            [[:maximum-nodes 3]
             [:maximum-depth 0]
             [:maximum-width 2]
             [:maximum-collection-count 0]
             [:maximum-scalar-utf8 2]]]
      (is (nil?
           (bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile
            value (assoc bounds field lower)))
          field))))

(deftest attempt32-profile-bounds-are-exact-and-independent
  (let [expected
        {:certificate-maximum-nodes 2048
         :certificate-maximum-depth 12
         :certificate-maximum-width 35
         :certificate-maximum-collection-count 64
         :certificate-maximum-scalar-utf8 4096
         :certificate-maximum-canonical-reader-bytes 262144
         :outer-request-maximum-collection-count 65536
         :carrier-maximum-nodes 8388608
         :carrier-maximum-depth 256
         :carrier-maximum-width 65536
         :carrier-maximum-collection-count 524288
         :carrier-partition-maximum-collection-count 65536
         :carrier-maximum-scalar-utf8 65536
         :offline-transcript-maximum-nodes 262144
         :offline-transcript-maximum-records 262144
         :offline-transcript-maximum-call-rows 65536
         :offline-transcript-maximum-bytes 268435456}]
    (is (= 17 (count expected)))
    (is (= expected bootstrap/p15-s23-sh07-b51-attempt32-profile-bounds))
    (is (= expected producer-census/profile-bounds))
    (is (= expected independent-census/profile-bounds))))

(deftest attempt32-compositional-collection-bounds-admit-limit-and-reject-plus-one
  (let [profile bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile
        carrier-bounds
        {:maximum-nodes 8388608 :maximum-depth 256 :maximum-width 65536
         :maximum-collection-count 524288 :maximum-scalar-utf8 65536}
        partition-bounds
        (assoc carrier-bounds :maximum-collection-count 65536)
        shared-empty []
        bucket-at-limit [(vec (repeat 65534 shared-empty))]
        bucket-plus-one [(vec (repeat 65535 shared-empty))]
        group-65535 (vec (repeat 65535 shared-empty))
        group-65534 (vec (repeat 65534 shared-empty))
        whole-at-limit
        (conj (vec (repeat 7 group-65535)) group-65534)
        whole-plus-one (vec (repeat 8 group-65535))
        minimal-carrier {:module-plan-carriers (vec (repeat 7 nil))}
        raw-at-limit
        {:v18-executable-descriptor minimal-carrier
         :padding (vec (repeat 65534 shared-empty))}
        raw-plus-one
        (assoc raw-at-limit :padding (vec (repeat 65535 shared-empty)))]
    (is (= 65536 (:collection-count
                  (profile bucket-at-limit partition-bounds))))
    (is (nil? (profile bucket-plus-one partition-bounds)))
    (is (= 524288 (:collection-count
                   (profile whole-at-limit carrier-bounds))))
    (is (nil? (profile whole-plus-one carrier-bounds)))
    ;; Reusing the same empty vector object never deduplicates occurrences.
    (is (= 65536 (:collection-count
                  (profile [(vec (repeat 65534 shared-empty))]
                           partition-bounds))))
    ;; The nested coordinate is replaced by the local sentinel for the outer
    ;; count, so these differ only at the independent outer first-plus-one.
    (is (some?
         (bootstrap/p15-s23-sh07-b51-supplied-raw-v4-preflight-profile
          raw-at-limit)))
    (is (nil?
         (bootstrap/p15-s23-sh07-b51-supplied-raw-v4-preflight-profile
          raw-plus-one)))))

(deftest attempt30-schema-domain-role-assumption-identity-and-join-matrix
  (let [carrier (support/carrier)
        certificate-a (get-in carrier [:compile-census-certificates 0])
        candidates
        {"carrier v3 domain"
         (assoc carrier :domain
                :gravity/sh07-b51-v18-linked-executable-closure-carrier-v3)
         "carrier schema 3" (assoc carrier :schema-version 3)
         "M3-v1 artifact"
         (assoc-in carrier [:execution-bundle :artifact]
                   :gravity/sh07-b51-execution-bundle-v1)
         "M3-v1 schema"
         (assoc-in carrier [:execution-bundle :schema-version] 1)
         "unknown certificate key"
         (assoc-in carrier [:compile-census-certificates 0 :unknown] true)
         "missing certificate key"
         (update-in carrier [:compile-census-certificates 0]
                    dissoc :evidence-root)
         "certificate domain"
         (assoc-in carrier [:compile-census-certificates 0 :domain]
                   :gravity/sh07-b51-independent-verifier-census-certificate-v1)
         "certificate schema"
         (assoc-in carrier [:compile-census-certificates 0 :schema-version] 2)
         "same role"
         (assoc-in carrier [:compile-census-certificates 1 :role]
                   :producer-census)
         "wrong algorithm role"
         (assoc-in carrier
                   [:compile-census-certificates 0 :checker :algorithm-role]
                   :m2-first-independent-exhaustive)
         "false assumption"
         (assoc-in carrier
                   [:compile-census-certificates 0 :assumptions
                    :source-emitter-bytes-immutable]
                   false)
         "stale report identity"
         (assoc-in carrier
                   [:compile-census-certificates 0
                    :architecture-report-sha256]
                   fabricated-digest)
         "snapshot identity"
         (assoc-in carrier
                   [:compile-census-certificates 0 :inputs :snapshot-tree]
                   "cccccccccccccccccccccccccccccccccccccccc")
         "M0 identity"
         (assoc-in carrier
                   [:compile-census-certificates 0 :inputs :m0-ids 0]
                   fabricated-digest)
         "M1 identity"
         (assoc-in carrier
                   [:compile-census-certificates 0 :inputs :m1-id]
                   fabricated-digest)
         "M2 identity"
         (assoc-in carrier
                   [:compile-census-certificates 0 :inputs :m2-ids 0]
                   fabricated-digest)
         "transcript identity"
         (assoc-in carrier
                   [:compile-census-certificates 0
                    :raw-source-transcript :record-root]
                   fabricated-digest)
         "physical census join"
         (update-in carrier [:physical-census :semantic-row-count] inc)
         "category join"
         (update-in carrier
                    [:compile-census-certificates 0 :claim
                     :category-commitments 0 :row-count]
                    inc)
         "certificate/M3 join"
         (assoc-in carrier
                   [:execution-bundle :compile-census-certificate-ids 0]
                   fabricated-digest)
         "M3/S30 join"
         (assoc-in carrier [:semantic-manifest :execution-bundle-id]
                   fabricated-digest)
         "S30/root join" (assoc carrier :semantic-root-hash fabricated-digest)
         "reordered certificate pair"
         (assoc carrier :compile-census-certificates
                (vec (reverse (:compile-census-certificates carrier))))}]
    (is (bootstrap/p15-s23-sh07-b51-compile-census-certificate-v1-valid?
         certificate-a))
    (doseq [[label candidate] candidates]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? candidate))
          label))))

(deftest attempt30-cardinality-and-binding-first-plus-one-reject
  (let [carrier (support/carrier)
        certificate (last (:compile-census-certificates carrier))
        descriptor (last (:module-source-descriptors carrier))
        commitment
        (last (get-in carrier
                      [:semantic-manifest :category-commitments]))
        root (last (get-in carrier [:execution-bundle :public-roots]))]
    (doseq [[label candidate]
            [["certificate 2/3"
              (update carrier :compile-census-certificates
                      conj certificate)]
             ["module 7/8"
              (update carrier :module-source-descriptors conj descriptor)]
             ["category 7/8"
              (update-in carrier
                         [:semantic-manifest :category-commitments]
                         conj commitment)]
             ["public root 8/9"
              (update-in carrier [:execution-bundle :public-roots]
                         conj (assoc root :root-ordinal 9))]
             ["binding occupancy 2440/2441"
              (-> carrier
                  (assoc-in [:physical-census :maximum-binding-occupancy]
                            2441)
                  (assoc-in [:physical-census :module-binding-occupancies 0]
                            2441)
                  (assoc-in [:physical-census :module-censuses 0
                             :aggregate-binding-count]
                            2441))]]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? candidate))
          label))))

(deftest attempt30-shared-checker-closure-and-coherent-rehash-have-no-authority
  (let [carrier (support/carrier)
        products (bootstrap/p15-s23-sh07-b51-current-bundle-core-products)
        [certificate-a certificate-b] (:compile-census-certificates carrier)
        shared-closure-b
        (normalize-certificate
         (assoc-in certificate-b [:checker :dependency-closure-id]
                   (get-in certificate-a [:checker :dependency-closure-id])))
        changed-a
        (normalize-certificate
         (assoc-in certificate-a [:provenance :completed-at]
                   "2026-09-02T00:00:01Z"))
        rehashed-carrier
        (bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
         products [changed-a certificate-b])]
    (is (bootstrap/p15-s23-sh07-b51-compile-census-certificate-v1-valid?
         shared-closure-b))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
          [certificate-a shared-closure-b])))
    (is (= true
           (boolean
            (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid?
             rehashed-carrier)))
        "coherent certificate, M3-v2, S30, and root rehash remains structural")
    (is (not= (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map carrier)
              (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map
               rehashed-carrier)))
    (support/with-provisional-authority
     #(is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-authorized?
            rehashed-carrier))
          "coherent rehash cannot replace the locally pinned candidate"))))

(defn- failure-data [thunk]
  (try (thunk) nil
       (catch clojure.lang.ExceptionInfo failure (ex-data failure))))

(deftest attempt30-preflight-precedes-equality-and-failure-is-atomic
  (support/with-provisional-authority
   (fn []
     (let [good-raw (support/malformed-raw)
           cases
           [["preflight failure"
             (assoc good-raw :v18-executable-descriptor
                    (map identity (range)))
             false]
            ["bounded equality failure"
             (assoc good-raw :v18-executable-descriptor
                    (assoc (support/carrier) :domain :mutated-domain))
             true]]
           preflight-var
           (support/private-var
            'gravity.bootstrap 'p15-s23-sh07-b51-supplied-raw-v4-preflight?)
           equality-var
           (support/private-var
            'gravity.bootstrap 'p15-s23-sh07-b51-v4-bounded-carrier-equal?)
           execute-var
           (support/private-var
            'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function)
           original-preflight @preflight-var
           original-equality @equality-var]
       (doseq [[label raw equality-expected?] cases]
         (let [events (atom [])
               equality-calls (atom 0)
               executions (atom 0)
               caller-trace (atom [])
               caller-result (atom nil)
               output (java.io.StringWriter.)
               data
               (binding [*out* output]
                 (with-redefs-fn
                   {preflight-var
                    (fn [value]
                      (swap! events conj :preflight)
                      (original-preflight value))
                    equality-var
                    (fn [supplied current]
                      (swap! events conj :equality)
                      (swap! equality-calls inc)
                      (original-equality supplied current))
                    execute-var
                    (fn [& _]
                      (swap! executions inc)
                      (reset! caller-result :forbidden-result))}
                   #(failure-data
                     (fn []
                       (bootstrap/p15-s23-sh07-b51-execute-linked-raw-v4-public-root
                        raw 1 [raw]
                        {:sh07-b51-external-call-trace caller-trace})))))]
           (is (= :preflight (first @events)) label)
           (is (= equality-expected? (pos? @equality-calls)) label)
           (is (zero? @executions) label)
           (is (empty? @caller-trace) label)
           (is (nil? @caller-result) label)
           (is (= "" (str output)) label)
           (is (= ["L2-UNKNOWN-CORE-FORM"
                   :unauthenticated-b51-linked-carrier false false]
                  ((juxt :id :reason :result-committed? :output-committed?)
                   data))
               label)))))))

(defn- read-all [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))

(defn- definitions [paths]
  (into {}
        (for [form (mapcat read-all paths)
              :when (and (seq? form) (= 'defn (first form)))]
          [(second form) form])))

(defn- call-heads [value]
  (cond
    (and (seq? value) (= 'quote (first value))) #{}
    (seq? value)
    (into (if (symbol? (first value)) #{(first value)} #{})
          (mapcat call-heads value))
    (coll? value) (into #{} (mapcat call-heads value))
    :else #{}))

(defn- definition-closure [definitions root]
  (loop [pending [root] seen #{}]
    (if (empty? pending)
      seen
      (let [current (peek pending)
            remaining (pop pending)]
        (if (contains? seen current)
          (recur remaining seen)
          (recur (into remaining
                       (filter definitions
                               (call-heads (get definitions current))))
                 (conj seen current)))))))

(deftest attempt30-root1-and-root6-share-no-offline-scanner-or-helper
  (let [producer-paths
        ["bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
         "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
         "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
         "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"]
        root6-paths
        ["bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
         "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"]
        producer-definitions (definitions producer-paths)
        root6-definitions (definitions root6-paths)
        root1-closure
        (definition-closure producer-definitions 'sh07-b51-build-template)
        root6-closure
        (definition-closure
         root6-definitions 'sh07-b51-build-independent-verifier-binding)
        root1-heads
        (into #{} (mapcat #(call-heads (get producer-definitions %)))
              root1-closure)
        root6-heads
        (into #{} (mapcat #(call-heads (get root6-definitions %)))
              root6-closure)
        old-root1-scanners
        '#{sh07-b51-r1-linked-carrier-replay
           sh07-b51-r1-linked-category-rows
           sh07-b51-r1-linked-call-census
           sh07-b51-r1-linked-m2-call-census
           sh07-b51-r1-linked-physical-census}
        old-root6-scanners
        '#{sh07-b51-r6-v18-executable-authentic?
           sh07-b51-r6-evaluate-checks
           sh07-b51-r6-linked-category-rows
           sh07-b51-r6-linked-physical-census
           sh07-b51-r6-linked-operation-occurrences
           sh07-b51-r6-linked-function-occurrences
           sh07-b51-r6-linked-reachable
           sh07-b51-r6-linked-module-census}
        source-text (apply str (map slurp (concat producer-paths root6-paths)))]
    (is (contains? root1-closure 'sh07-b51-r1-compact-carrier-v4?))
    (is (contains? root6-closure 'sh07-b51-r6-c30-carrier-authoritative?))
    (is (empty? (set/intersection root1-closure old-root1-scanners)))
    (is (empty? (set/intersection root6-closure old-root6-scanners)))
    (is (not-any? #(str/starts-with? (name %) "sh07-b51-r6-")
                  root1-heads))
    (is (not-any? #(str/starts-with? (name %) "sh07-b51-r1-")
                  root6-heads))
    (is (empty? (set/intersection root1-closure root6-closure))
        "the compact public closures share no Gravity helper implementation")
    (is (not (str/includes? source-text
                            "gravity.tools.sh07-b51-attempt30"))
        "runtime Gravity source cannot invoke either offline checker")))
