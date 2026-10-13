(ns gravity.self-hosting.artifact-parent-consumption-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-pass-cache :as c2-pass-cache]))

(def ^:private accepted-path
  "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/control-flow-order.gravity")
(def ^:private rejected-path
  "bootstrap/clojure/fixtures/self-hosting/sh-05/rejected/build-effect-ungranted.gravity")

(defn- ex-id
  [f]
  (try
    (f)
    :accepted
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(deftest authenticated-parent-apis-preserve-accepted-semantic-parity
  (let [source-text (slurp accepted-path)
        c3-artifact (bootstrap/compiler-c3-syntax-file-artifact accepted-path)
        old-sh05 (bootstrap/sh05-macro-source-artifact accepted-path source-text)
        new-sh05 (bootstrap/sh05-macro-artifact-from-c3-artifact
                  accepted-path c3-artifact)
        old-sh06 (bootstrap/sh06-resolution-source-artifact
                  accepted-path source-text)
        new-sh06 (bootstrap/sh06-resolution-artifact-from-sh05-artifact
                  accepted-path new-sh05)]
    (testing "C3 -> SH05 preserves the established artifact and proof"
      (is (= (:artifact-id old-sh05) (:artifact-id new-sh05)))
      (is (= :accepted (:status new-sh05)))
      (is (= :passed
             (:status (bootstrap/sh05-macro-artifact-verification new-sh05))))
      (is (= (:expanded-syntax-stream-id old-sh05)
             (:expanded-syntax-stream-id new-sh05)))
      (is (= (:macro-expansion-trace-id old-sh05)
             (:macro-expansion-trace-id new-sh05))))
    (testing "SH05 -> SH06 preserves the established artifact and proof"
      (is (= (:artifact-id old-sh06) (:artifact-id new-sh06)))
      (is (= :accepted (:status new-sh06)))
      (is (= :passed
             (:status (bootstrap/sh06-resolution-artifact-verification new-sh06))))
      (is (= (:resolution-table old-sh06) (:resolution-table new-sh06)))
      (is (= (:binding-table old-sh06) (:binding-table new-sh06))))))

(deftest authenticated-parent-apis-preserve-rejected-diagnostics
  (let [source-text (slurp rejected-path)
        c3-artifact (bootstrap/compiler-c3-syntax-file-artifact rejected-path)
        old-id (ex-id #(bootstrap/sh05-macro-source-artifact
                        rejected-path source-text))
        new-id (ex-id #(bootstrap/sh05-macro-artifact-from-c3-artifact
                        rejected-path c3-artifact))]
    (is (= old-id new-id))
    (is (= "C4-BUILD-EFFECT" (str old-id)))))

(deftest parent-apis-do-not-call-source-rebuild-entrypoints
  (let [source-text (slurp accepted-path)
        c3-artifact (bootstrap/compiler-c3-syntax-file-artifact accepted-path)
        calls (atom [])
        compiler-c3-var #'bootstrap/compiler-c3-syntax-source-artifact
        sh05-source-var #'bootstrap/sh05-macro-source-artifact]
    (with-redefs-fn
      {compiler-c3-var (fn [& _]
                         (swap! calls conj :c3-source)
                         (throw (ex-info "unexpected C3 source rebuild"
                                         {:id "PARENT-REBUILD"})))
       sh05-source-var (fn [& _]
                         (swap! calls conj :sh05-source)
                         (throw (ex-info "unexpected SH05 source rebuild"
                                         {:id "PARENT-REBUILD"})))}
      #(let [sh05 (bootstrap/sh05-macro-artifact-from-c3-artifact
                   accepted-path c3-artifact)
             sh06 (bootstrap/sh06-resolution-artifact-from-sh05-artifact
                   accepted-path sh05)]
         (is (= :accepted (:status sh05)))
         (is (= :accepted (:status sh06)))
         (is (empty? @calls))))))

(deftest parent-apis-reject-stale-rebound-and-forged-owners
  (let [c3 (bootstrap/compiler-c3-syntax-file-artifact accepted-path)
        c2 (:c2-reader-artifact c3)
        snap (c2-pass-cache/bounded-source-snapshot! accepted-path)
        stale (assoc snap :bytes-hash "sha256:stale")
        rebound (assoc snap :canonical-path "/rebound/accepted.gravity")]
    (testing "stale bytes are rejected before consumption"
      (with-redefs [c2-pass-cache/bounded-source-snapshot! (constantly stale)]
        (is (= "C4-RETURN"
               (str (ex-id #(bootstrap/sh05-macro-artifact-from-c3-artifact
                             accepted-path c3)))))))
    (testing "path rebinding is rejected at the authenticated boundary"
      (with-redefs [c2-pass-cache/bounded-source-snapshot! (constantly rebound)]
        (is (= "C4-RETURN"
               (str (ex-id #(bootstrap/sh05-macro-artifact-from-c3-artifact
                             accepted-path c3)))))))
    (testing "forged C3 identity is rejected"
      (is (= "C4-RETURN"
             (str (ex-id #(bootstrap/sh05-macro-artifact-from-c3-artifact
                           accepted-path (assoc c3 :artifact-id "sha256:forged")))))))
    (testing "post-construction mutation is rejected"
      (let [calls (atom 0)]
        (with-redefs [c2-pass-cache/bounded-source-snapshot!
                      (fn [_]
                        (if (= 1 (swap! calls inc)) snap stale))]
          (is (= "C4-RETURN"
                 (str (ex-id #(bootstrap/sh05-macro-artifact-from-c3-artifact
                               accepted-path c3))))))))))

(deftest sh06-rejects-forged-parent
  (let [source-text (slurp accepted-path)
        sh05 (bootstrap/sh05-macro-source-artifact accepted-path source-text)]
    (is (= "C5-UNRESOLVED"
           (str (ex-id #(bootstrap/sh06-resolution-artifact-from-sh05-artifact
                         accepted-path (assoc sh05 :artifact-id "sha256:forged"))))))))

(deftest public-parent-apis-expose-no-compatibility-bypass
  (is (= '([c3-artifact] [source-path c3-artifact])
         (:arglists (meta #'bootstrap/sh05-macro-artifact-from-c3-artifact))))
  (is (= '([sh05-artifact] [source-path sh05-artifact])
         (:arglists
          (meta #'bootstrap/sh06-resolution-artifact-from-sh05-artifact))))
  (is (nil? (ns-resolve 'gravity.bootstrap '*legacy-supplied-source*)))
  (is (thrown? clojure.lang.ArityException
               (bootstrap/sh05-macro-artifact-from-c3-artifact
                accepted-path {} true)))
  (is (thrown? clojure.lang.ArityException
               (bootstrap/sh06-resolution-artifact-from-sh05-artifact
                accepted-path {} true))))
