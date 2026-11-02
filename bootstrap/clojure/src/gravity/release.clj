(ns gravity.release
  "Native JAR inventory and observed file bytes/digests; no release admission."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {file-bytes {:arglists ([path]), :admission :none},
  bytes-sha256 {:arglists ([bytes]), :admission :none},
  file-sha256 {:arglists ([bytes-sha256-var file-bytes-var path]), :admission :none},
  jar-source-inventory-report
  {:arglists ([packaged-source-entries-var jar-entries]),
   :admission :none},
  jar-inventory-report
  {:arglists ([expected-jar-file-entries-var jar-file-entries]),
   :admission :none}},
 :supported
 [:canonical-source-entry-order
  :exact-membership
  :duplicate-refusal
  :missing-and-unexpected-entry-data
  :observed-file-octets :observed-sha256 :legacy-missing-file-sentinel],
 :bootstrap-hosted? true,
 :dependency-direction
 {:requires [clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli]},
 :test-owner gravity.release.pipeline-test,
 :self-hosted? false,
 :governing-contracts
 [D1 D2 D3 D8 D9 B13 C1 C15 PKG3 PKG7 PKG10 PKG12 T1 BOOT8 TEST2],
 :performs-io? true,
 :inventory-operations-perform-io? false,
 :unsupported
 [:untrusted-inventory-operation-authority
  :input-authentication
  :archive-creation-and-entry-parsing
  :path-authentication :filesystem-race-prevention :artifact-verification
  :package-dependency-closure
  :launcher-execution
  :canonical-release-assembly
  :signing
  :sbom
  :reproducibility-proof
  :migration
  :release-readiness
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:observed-native-entry-names :late-expected-inventory-owner-var
  :caller-owned-path-or-byte-array :late-byte-and-file-digest-owner-vars],
 :namespace gravity.release,
 :diagnostic-ownership
 {:decides
  [:source-entry-membership-order-and-duplicates
   :whole-file-membership-and-duplicates],
  :publishes [],
  :host-file-errors :propagated-unchanged,
  :missing-file-digest :legacy-sentinel-only,
  :existing-caller ["P18T02006"]},
 :artifact-outputs
 [:source-entry-inventory-report :whole-jar-file-inventory-report
  :observed-file-bytes :bare-or-prefixed-sha256 :legacy-missing-file-sentinel],
 :clojure-seed-boundary? true,
 :issues-certificates? false,
 :contract-boundary :native-inventory-and-observed-file-data})

(defn jar-source-inventory-report
  [packaged-source-entries-var jar-entries]
  (let [expected (vec (sort (map :jar-entry ((deref packaged-source-entries-var)))))
        observed (filterv #(and (string? %)
                                (or (str/ends-with? % ".clj")
                                    (str/ends-with? % ".cljc")
                                    (str/ends-with? % ".edn")))
                          jar-entries)
        entry-frequencies (frequencies jar-entries)
        valid?
        (and (every? string? jar-entries)
             (= expected observed)
             (every? #(= 1 (get entry-frequencies % 0)) expected))]
    {:valid? (boolean valid?)
     :expected expected
     :observed observed
     :entry-frequencies entry-frequencies}))

(defn jar-inventory-report
  [expected-jar-file-entries-var jar-file-entries]
  (let [expected ((deref expected-jar-file-entries-var))
        string-entries? (every? string? jar-file-entries)
        observed (if string-entries?
                   (vec (sort jar-file-entries))
                   (vec jar-file-entries))
        entry-frequencies (frequencies jar-file-entries)
        valid?
        (and string-entries?
             (= expected observed)
             (every? #(= 1 (get entry-frequencies % 0)) expected))]
    {:valid? (boolean valid?)
     :expected expected
     :observed observed
     :missing (vec (remove (set observed) expected))
     :unexpected (vec (remove (set expected) observed))
     :entry-frequencies entry-frequencies}))

(defn file-bytes
  [path]
  (java.nio.file.Files/readAllBytes
   (.toPath (java.io.File. path))))

(defn bytes-sha256
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn file-sha256
  [bytes-sha256-var file-bytes-var path]
  (if (.isFile (java.io.File. path))
    (str "sha256:" ((deref bytes-sha256-var) ((deref file-bytes-var) path)))
    "sha256:missing"))

(doseq [operation [#'jar-source-inventory-report #'jar-inventory-report
                   #'file-bytes #'bytes-sha256 #'file-sha256]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
