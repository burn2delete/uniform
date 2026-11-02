(ns gravity.self-hosting.sh07-b51-attempt30-root6-compact-verifier-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.set :as set]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt28-root6-independent-replay-test
             :as root6]
            [gravity.self-hosting.sh07-b51-attempt30-independent-census-test
             :as checker-fixture]
            [gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test]
            [gravity.tools.sh07-b51-attempt30-independent-census :as checker]))

(def report-id
  "sha256:b105b395ba03a29e5bbd75ded681f21cdbb0375e4df73df9f9fbccf9aafa78db")

(def old-exhaustive-functions
  '#{sh07-b51-r6-v18-executable-authentic?
     sh07-b51-r6-evaluate-checks
     sh07-b51-r6-linked-category-rows
     sh07-b51-r6-linked-physical-census
     sh07-b51-r6-linked-operation-occurrences
     sh07-b51-r6-linked-function-occurrences
     sh07-b51-r6-linked-reachable
     sh07-b51-r6-linked-module-census})

(defn- private-host-value [name]
  (var-get
   (or (ns-resolve
        'gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
        name)
       (throw (ex-info "missing Attempt30 host-test fixture" {:name name})))))

(defn- checker-b-certificate []
  (let [envelope
        (assoc (checker-fixture/envelope)
               :architecture-report-sha256 report-id)
        result (checker/produce-certificate envelope)]
    (when-not (= :accepted (:status result))
      (throw (ex-info "checker B test certificate was rejected" result)))
    (:certificate result)))

(defn- accepted-carrier []
  (:carrier @(private-host-value 'accepted-fixture)))

(defn- raw-carrier [carrier]
  ((private-host-value 'raw-request) carrier))

(defn- replace-carrier [raw carrier]
  (assoc raw :v18-executable-descriptor carrier))

(defn- old-exhaustive-replacements [calls]
  (into {}
        (keep
         (fn [name]
           (when-let [variable (ns-resolve @root6/runtime name)]
             [variable
              (fn [& _]
                (swap! calls conj name)
                (throw (ex-info "old exhaustive scanner was invoked"
                                {:function name})))])))
        old-exhaustive-functions))

(deftest root6-accepts-the-exact-independent-census-certificate
  (let [certificate (checker-b-certificate)]
    (is (true?
         (root6/invoke 'sh07-b51-r6-c30-checker-b-certificate?
                       certificate)))
    (doseq [mutation
            [(assoc certificate :unknown true)
             (assoc certificate :role :producer-census)
             (assoc-in certificate [:checker :algorithm-role]
                       :source-first-exhaustive)
             (assoc-in certificate
                       [:assumptions :source-emitter-bytes-immutable]
                       false)
             (assoc-in certificate
                       [:raw-source-transcript :artifact-id]
                       (bootstrap/reader-canonical-hash :mutated-raw))
             (assoc-in certificate
                       [:m2-transcript :record-root]
                       (bootstrap/reader-canonical-hash :mutated-m2))
             (assoc-in certificate
                       [:result :external-bijection-complete]
                       false)
             (update-in certificate
                        [:claim :physical-census
                         :reachable-definition-count]
                        inc)
             (update-in certificate
                        [:claim :category-commitments 0 :row-count]
                        inc)
             (assoc certificate :evidence-root
                    (bootstrap/reader-canonical-hash :mutated-evidence))
             (assoc certificate :certificate-id
                    (bootstrap/reader-canonical-hash :mutated-certificate))]]
      (is (false?
           (root6/invoke 'sh07-b51-r6-c30-checker-b-certificate?
                         mutation))))))

(deftest root6-certificate-profile-enforces-the-fixed-compact-ceilings
  (let [width-35 (into {} (map (fn [index] [(keyword (str "k" index)) index])
                               (range 35)))
        width-36 (assoc width-35 :one-key-too-many true)
        scalar-at-limit (apply str (repeat 4094 "a"))
        scalar-over-limit (str scalar-at-limit "a")]
    (is (= :accepted
           (:status
            (root6/invoke 'sh07-b51-r6-c30-certificate-profile width-35))))
    (is (= :rejected
           (:status
            (root6/invoke 'sh07-b51-r6-c30-certificate-profile width-36))))
    (is (= 4096
           (:scalar-utf8
            (root6/invoke 'sh07-b51-r6-c30-certificate-profile
                          scalar-at-limit))))
    (is (= :rejected
           (:status
            (root6/invoke 'sh07-b51-r6-c30-certificate-profile
                          scalar-over-limit))))))

(deftest root6-compact-carrier-rebuilds-b-from-tier0-through-s30
  (let [carrier (accepted-carrier)
        raw (raw-carrier carrier)]
    (is (true?
         (root6/invoke 'sh07-b51-r6-c30-carrier-authoritative? raw)))
    (testing "certificate A is opaque except for its ordered M3-v2 identity"
      (is (true?
           (root6/invoke
            'sh07-b51-r6-c30-carrier-authoritative?
            (replace-carrier
             raw
             (assoc-in carrier [:compile-census-certificates 0 :unknown]
                       true))))))
    (doseq [mutation
            [(assoc-in carrier
                       [:compile-census-certificates 1 :role]
                       :producer-census)
             (assoc-in carrier [:link-manifest :edges 0 :owner-edge]
                       :forbidden-owner-edge)
             (assoc-in carrier [:execution-bundle :public-roots 5 :function]
                       'sh07-b51-build-template)
             (assoc-in carrier
                       [:execution-bundle :compile-census-certificate-ids 1]
                       (bootstrap/reader-canonical-hash :mutated-m3))
             (update-in carrier [:semantic-manifest :total-row-count] inc)
             (assoc carrier :semantic-root-hash
                    (bootstrap/reader-canonical-hash :mutated-s30-root))]]
      (is (false?
           (root6/invoke
            'sh07-b51-r6-c30-carrier-authoritative?
            (replace-carrier raw mutation)))))))

(deftest root6-public-closure-has-no-offline-or-exhaustive-census-path
  (let [closure
        (root6/definition-closure
         'sh07-b51-build-independent-verifier-binding)
        call-heads
        (into #{}
              (mapcat #(root6/call-heads (get @root6/definitions %)))
              closure)]
    (is (contains? closure 'sh07-b51-r6-c30-checker-b-certificate?))
    (is (contains? closure 'sh07-b51-r6-c30-carrier-authoritative?))
    (is (contains? closure 'sh07-b51-r6-c30-request-proof?))
    (is (empty? (set/intersection closure old-exhaustive-functions)))
    (is (not-any?
         #(or (.startsWith (name %) "sh07-b51-r1-")
              (.startsWith (name %) "sh07-b51-producer-")
              (.startsWith (name %) "p15-s23-sh07-b51-attempt30"))
         call-heads))))

(deftest root6-accepted-and-mutated-compact-carriers-never-run-old-scanners
  (let [carrier (accepted-carrier)
        raw (raw-carrier carrier)
        calls (atom [])
        replacements (old-exhaustive-replacements calls)]
    (with-redefs-fn
      replacements
      #(do
         (is (true?
              (root6/invoke 'sh07-b51-r6-c30-carrier-authoritative? raw)))
         (is (false?
              (root6/invoke
               'sh07-b51-r6-c30-carrier-authoritative?
               (replace-carrier
                raw
                (assoc carrier :semantic-root-hash
                       (bootstrap/reader-canonical-hash
                        :rejected-without-scan))))))))
    (is (empty? @calls))))

(deftest root6-public-boundary-rejection-is-atomic
  (let [raw (raw-carrier (accepted-carrier))
        result
        (root6/invoke 'sh07-b51-build-independent-verifier-binding
                      raw {} {} {} [] [])]
    (is (= :boundary-rejected (:status result)))
    (is (= :independent-verifier-boundary-rejected (:tag result)))
    (is (= {:status :boundary-rejected
            :boundary :sh07-b51-build-independent-verifier-binding
            :reason :invocation-mismatch
            :recursive-diagnostic-forbidden true}
           (:value result)))))
