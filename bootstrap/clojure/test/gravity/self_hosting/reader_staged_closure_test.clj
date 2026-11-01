(ns gravity.self-hosting.reader-staged-closure-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh03-phase-lifetime-test]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- existing [namespace name] (var-get (ns-resolve namespace name)))
(def ^:private reader-path
  (delay (str (.resolve @(existing 'gravity.self-hosting.sh12-branch-join-mir-test 'root)
                        "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))))
(def ^:private staged-binding
  (delay
    ;; Established explicit-current-source route. Compile the real selected
    ;; Reader closure, including its structural verifier; retain historical pins.
    (binding [bootstrap/*p15-s23-stage2-compiler-artifact-binding*
              @(existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter)]
      (bootstrap/sh03-reader-current-binding! "<reader-staged-closure>" @reader-path))))
(defn- arguments [text]
  ((existing 'gravity.self-hosting.sh03-phase-lifetime-test 'input) text true))
(defn- execute [function args]
  (bootstrap/sh03-reader-execute-plan! "<reader-staged-closure>"
                                     (:plan @staged-binding) function args))

(deftest staged-reader-builds-selected-closure-and-fresh-verifier
  (let [binding @staged-binding plan (:plan binding)
        args (arguments "(foo :key \"\u03bb\uD83D\uDE42\")\r\n")
        calls (atom {}) dispatch (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @dispatch
        [result report]
        (with-redefs-fn
          {dispatch (fn [runtime plan callee arguments scope]
                      (swap! calls update callee (fnil inc 0))
                      (original runtime plan callee arguments scope))}
          #(let [result (execute 'sh03-read-source-unit args)]
             [result (execute 'sh03-verify-reader-result (conj args result))]))
        phases '[sh03-decode-source-bytes sh03-scan-tokens sh03-parse-tokens
                 sh03-construct-parsed-source-unit]]
    (is (= :complete (:status binding)))
    (is (contains? (:functions plan) 'sh03-token-with-raw))
    (is (= 8 (get-in plan [:functions 'sh03-token-with-raw :arity])))
    (is (not (contains? (:functions plan) 'reader-token-with-raw)))
    (is (every? #(str/starts-with? (name %) "sh03-") (keys (:functions plan))))
    (is (= (:source-content-hash binding) (get-in plan [:source :sha256])))
    (is (= [:accepted :accepted] (mapv :status [result report])))
    (is (true? (:verified? report)))
    (is (= (zipmap phases (repeat 2)) (select-keys @calls phases)))
    (doseq [path [[:token-stream 0 :raw :byte-end]
                  [:token-stream 0 :span :byte-start]
                  [:semantic-value-table 0 :descriptor :name-codepoints]
                  [:digest-requests 1 :preimage :logical-source-id]]]
      (let [report (execute 'sh03-verify-reader-result (conj args (assoc-in result path :substituted)))]
        (is (= :rejected (:status report)))
        (is (false? (:verified? report)))
        (is (= ["C2-HASH"] (mapv :id (:diagnostics report))))
        (is (= [:reader-result-replay-mismatch]
               (mapv #(get-in % [:reader-state :reason]) (:diagnostics report))))))
    (let [args (arguments "12abc")
          malformed (execute 'sh03-read-source-unit args)
          report (execute 'sh03-verify-reader-result (conj args malformed))]
      (is (= :rejected (:status malformed) (:reader-result-status report)))
      (is (= :accepted (:status report)))
      (is (true? (:verified? report)))
      (is (= ["STAGE1READER007"] (mapv :reader-engine-diagnostic (:diagnostics malformed)))))
    (doseq [[bad expected-arity]
            [[(update plan :functions dissoc 'sh03-token-with-raw) nil]
             [(assoc-in plan [:functions 'sh03-token-with-raw :arity] 7) 7]]]
      (let [error (try (bootstrap/sh03-reader-plan-static-audit! "<reader-staged-closure>" bad)
                       nil (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= "C2-HASH" (:id error)))
        (is (= [:exact-sh03-reader-function-call] (:missing-fields error)))
        (is (= 'sh03-token-with-raw (get-in error [:facts :function])))
        (is (= expected-arity (get-in error [:facts :expected-arity])))
        (is (= 8 (get-in error [:facts :actual-arity])))))))
