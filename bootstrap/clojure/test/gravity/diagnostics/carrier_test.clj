(ns gravity.diagnostics.carrier-test
  "Trusted Stage0 carrier controls and ordinary host-source evidence only."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.diagnostics :as diagnostics]))

(defn- observed-error [operation]
  (try (operation) nil (catch Throwable error error)))

(deftest carrier-contract-keeps-producer-policy-and-admission-outside-diagnostics
  (let [contract (:gravity/namespace-contract (meta #'diagnostics/fail-at-node!))
        api (get-in contract [:public-api 'fail-at-node!])]
    (is (= 20 (count (ns-publics 'gravity.diagnostics)) (count (:public-api contract))))
    (is (= '([fail-var analyzer-stage id message node remediation data]) (:arglists api)))
    (is (= (:arglists api) (:arglists (meta #'diagnostics/fail-at-node!))))
    (is (= :none (:admission api)))
    (is (= '[fail-var] (:operation-vars api)))
    (is (= :caller (:analyzer-stage-owner api)))
    (is (= :producer (:rule-selection-owner api)))
    (is (= :original-callback-result (:returns api)))
    (is (= :supplied-failure-operation (:transport-owner api)))
    (is (= :unchanged (:exception-transport api)))
    (is (true? (:hosted-seed-only? api)))
    (is (every? (set (:governing-contracts contract)) '[L5 L6 C7 C8 C15]))
    (is (some #{:stage-specific-policy} (get-in contract [:diagnostic-ownership :does-not-own])))
    (is (some #{:typed-source-admission} (:unsupported contract)))
    (is (some #{:canonical-c15-conformance} (:unsupported contract)))
    (is (= '[clojure.core clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (some #{'gravity.bootstrap} (get-in contract [:dependency-direction :forbids])))))

(deftest native-carriers-retain-defaults-caller-overrides-and-object-identities
  (let [span (Object.) remediation (Object.) origin (Object.)
        node {:source-span span}
        failure (clojure.lang.Var/create (fn [id message data] {:id id :message message :data data}))]
    (doseq [data [nil {} {:source-span nil :analyzer-stage false :remediation nil :origin origin}
                  {:source-span false :analyzer-stage :producer-stage :remediation false :origin origin}]]
      (let [expected (merge {:source-span span :analyzer-stage :typed-effected-core :remediation remediation} data)
            direct (diagnostics/fail-at-node! failure :typed-effected-core "rule" "message" node remediation data)
            compatibility (with-redefs [b/fail! @failure]
                            (b/typed-diagnostic! "rule" "message" node remediation data))]
        (is (= {:id "rule" :message "message" :data expected} direct compatibility))
        (doseq [key [:source-span :analyzer-stage :remediation :origin]]
          (is (identical? (get expected key) (get-in direct [:data key])))))))
  (let [span (Object.) remediation (Object.) node {:source-span span}
        data {:fact :retained :id nil :message false :bootstrap-stage :caller}
        error (observed-error #(diagnostics/fail-at-node! #'b/fail! :producer "rule" "message" node remediation data))]
    (is (= (merge {:source-span span :analyzer-stage :producer :remediation remediation} data) (ex-data error)))
    (is (= "message" (.getMessage ^Throwable error)))
    (is (identical? span (:source-span (ex-data error))))
    (is (identical? remediation (:remediation (ex-data error))))))

(deftest failure-var-is-selected-before-node-lookup-and-caller-data-realization
  (let [events (atom []) selected (Object.) too-late (Object.)
        failure (clojure.lang.Var/create (fn [& _] selected))
        node (reify clojure.lang.ILookup
               (valAt [_ key]
                 (swap! events conj [:node key])
                 (alter-var-root failure (constantly (fn [& _] too-late)))
                 :actual-span))
        data (lazy-seq (swap! events conj :data) (list (clojure.lang.MapEntry/create :fact :retained)))]
    (is (identical? selected (diagnostics/fail-at-node! failure :producer "rule" "message" node :remediation data)))
    (is (= [[:node :source-span] :data] @events))
    (is (identical? too-late (diagnostics/fail-at-node! failure :producer "rule" "message" {} nil nil)))))

(deftest compatibility-default-arity-retains-live-five-argument-interposition
  (let [original b/typed-diagnostic! node (Object.) remediation (Object.) received (atom nil) result (Object.)]
    (is (identical? result
                    (with-redefs [b/typed-diagnostic! (fn [& args] (reset! received args) result)]
                      (original "rule" "message" node remediation))))
    (is (= ["rule" "message" node remediation {}] @received)))
  (let [span (Object.) received (atom nil) result (Object.)]
    (is (identical? result
                    (with-redefs [b/fail! (fn [& args] (reset! received args) result)]
                      (b/typed-diagnostic! "rule" "message" {:source-span span} :remediation))))
    (is (= ["rule" "message" {:source-span span :analyzer-stage :typed-effected-core :remediation :remediation}]
           @received))))

(deftest carrier-propagates-host-exceptions-and-preserves-unusual-native-inputs
  (let [failure (Exception. "compatibility failure owner")]
    (doseq [invoke [(fn [] (b/typed-diagnostic! "id" "message" {} nil))
                    (fn [] (b/typed-diagnostic! "id" "message" {} nil {:fact :retained}))]]
      (is (identical? failure
                      (with-redefs [b/fail! (fn [& _] (throw failure))]
                        (observed-error invoke))))))
  (let [failure (Exception. "failure owner") calls (atom 0)
        callback (clojure.lang.Var/create (fn [& _] (swap! calls inc) (throw failure)))
        node (reify clojure.lang.ILookup (valAt [_ _] (throw failure)))]
    (is (identical? failure (observed-error #(diagnostics/fail-at-node! callback :stage "id" "message" {} nil nil))))
    (is (= 1 @calls))
    (is (identical? failure (observed-error #(diagnostics/fail-at-node! callback :stage "id" "message" node nil nil))))
    (is (= 1 @calls)))
  (doseq [node [nil false 17 "node"]]
    (let [result (diagnostics/fail-at-node! (clojure.lang.Var/create (fn [_ _ data] data))
                                            nil "id" "message" node false nil)]
      (is (= {:source-span nil :analyzer-stage nil :remediation false} result))))
  (doseq [data [17 :not-a-map "not-a-map"]]
    (let [calls (atom 0) failure (clojure.lang.Var/create (fn [& _] (swap! calls inc)))
          direct (observed-error #(diagnostics/fail-at-node! failure :typed-effected-core "id" "message" {} nil data))
          compatibility (with-redefs [b/fail! @failure]
                          (observed-error #(b/typed-diagnostic! "id" "message" {} nil data)))]
      (is (some? direct))
      (is (= (class direct) (class compatibility)))
      (is (= (.getMessage ^Throwable direct) (.getMessage ^Throwable compatibility)))
      (is (zero? @calls)))))

(deftest real-ordinary-sources-emit-typed-artifacts-with-original-provenance
  (doseq [relative ["bootstrap/clojure/fixtures/accepted/typed-core.gravity" "examples/hello.gravity" "examples/hello.qst"]]
    (let [path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
          artifact (b/typed-source-artifact path text)
          record {:source {:path path :text text :sha256 (str "sha256:" (b/sha256-hex text))}
                  :artifact artifact :authority :ordinary-stage0-host-source-only
                  :observation-sha256 (b/sha256-hex (pr-str (b/c-backend-canonical-value artifact)))}
          output (io/file "target/diagnostics-typed-carrier" (str (.getName (io/file path)) ".edn"))]
      (is (= :gravity/stage0-typed-core-artifact (:kind artifact)))
      (is (= :type-effect-capability-check (get-in artifact [:pass :name])))
      (is (= path (get-in artifact [:module :source-path])))
      (is (seq (:type-facts artifact)))
      (is (every? :source-span (:type-facts artifact)))
      (is (= artifact (b/typed-source-artifact path text)))
      (io/make-parents output)
      (spit output (pr-str record))
      (is (= record (edn/read-string (slurp output)))))))

(deftest actual-type-and-effect-refusals-retain-complete-semantic-payloads
  (doseq [[fixture id expected-type-facts start end bytes]
          [["typed-type-mismatch" "L5-TYPE-MISMATCH" {:expected-type "Integer" :actual-type "String"}
            {:line 8 :column 1 :char 120 :byte 120} {:line 9 :column 44 :char 172 :byte 172} [120 172]]
           ["typed-effect-undeclared" "L6-EFFECT-UNDECLARED" {:effect :io/write :declared-effects #{}}
            {:line 8 :column 1 :char 128 :byte 128} {:line 9 :column 33 :char 169 :byte 169} [128 169]]]]
    (let [path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/rejected/" fixture ".gravity")))
          original diagnostics/fail-at-node! calls (atom 0)
          error (with-redefs [diagnostics/fail-at-node! (fn [& args] (swap! calls inc) (apply original args))]
                  (observed-error #(b/typed-file-artifact path)))
          data (ex-data error)
          expected (merge {:id id :bootstrap-stage :stage0 :analyzer-stage :typed-effected-core
                           :source-span {:source path :form-index 1 :start start :end end
                                         :byte-start (first bytes) :byte-end (second bytes)}} expected-type-facts)]
      (is (pos? @calls))
      (is (instance? clojure.lang.ExceptionInfo error))
      (is (= expected (dissoc data :message :remediation)))
      (is (= (:message data) (.getMessage ^Throwable error)))
      (is (string? (:remediation data))))))
