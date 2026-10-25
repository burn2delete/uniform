(ns gravity.self-hosting.sh15-bounded-diagnostic-text-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private root
  (delay
    (loop [path (.getParent
                 (.toPath (io/file (.toURI
                                   (io/resource "gravity/self_hosting/sh15_bounded_diagnostic_text_test.clj")))))]
      (cond
        (nil? path) (throw (ex-info "repository root missing" {}))
        (.isFile (.toFile (.resolve path "deps.edn"))) path
        :else (recur (.getParent path))))))

(def ^:private c15-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")

(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/" family
       "/coordinator-bounded-diagnostic-text." extension))

(defn- compile-plan [relative]
  (let [path (str (.resolve @root relative))
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))))

(def ^:private c15-plan (delay (compile-plan c15-path)))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-bounded-text-development :compiler-artifact-plan? true}
   plan function arguments))

(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- render [diagnostic]
  (c15 'sh15-render-bounded-diagnostic-text [diagnostic]))
(defn- diagnostic []
  (invoke @accepted-plan 'coordinator-bounded-diagnostic-text []))

(defn- ascii-line? [text]
  (and (string? text) (every? #(<= 32 (int %) 126) text)))

(defn- escaped-ascii [text]
  (apply str
         (map (fn [character]
                (cond
                  (or (< (int character) 32) (= 127 (int character)))
                  (format "\\u%04x" (int character))
                  (= \\ character) "\\\\"
                  (= \" character) "\\\""
                  :else (str character)))
              text)))

(deftest sh15-bounded-text-paired-fixtures-and-golden
  (doseq [[family function]
          [["accepted" 'coordinator-bounded-diagnostic-text]
           ["rejected" 'coordinator-bounded-diagnostic-text-invalid]]]
    (let [g (fixture-path family "gravity") q (fixture-path family "qst")
          g-plan (if (= "accepted" family) @accepted-plan @rejected-plan)
          q-plan (compile-plan q)]
      (is (= (slurp (str (.resolve @root g))) (slurp (str (.resolve @root q)))))
      (is (= (invoke g-plan function []) (invoke q-plan function [])))))
  (let [input (diagnostic) before (pr-str input) result (render input)
        golden (invoke @accepted-plan 'coordinator-bounded-diagnostic-text-golden [])]
    (is (= :accepted (:status result)))
    (is (= golden (:text result)))
    (is (= :bounded-ascii-v1 (:rendering-version result)))
    (is (ascii-line? (:text result)))
    (is (= :none (:semantic-authority result)))
    (is (= before (pr-str input)))
    (is (= input (diagnostic)))
    (is (every? #(not (str/includes? (:text result) %))
                ["PRIVATE-FACT" "PRIVATE-MESSAGE" "PRIVATE-TOOL-OUTPUT"
                 "fixture-syntax" "fixture-producer" "fixture-stable-id"]))
    (is (some #{:cli-integration} (:nonclaims result)))
    (is (some #{:complete-c15-conformance} (:nonclaims result)))))

(deftest sh15-bounded-text-escaping-severities-and-limits
  (let [characters (apply str (map char (range 128)))
        input (-> (diagnostic) (assoc :rule characters)
                  (assoc-in [:primary :span :actual-source-path] characters))
        result (render input)]
    (is (= :accepted (:status result)))
    (is (= (str "code=\"" (escaped-ascii characters)
                "\" severity=:error location=\"" (escaped-ascii characters)
                "\":2:3 redactions=:clear")
           (:text result)))
    (is (ascii-line? (:text result))))
  (doseq [severity [:error :warning :info :hint :internal-error]]
    (let [result (render (assoc (diagnostic) :severity severity))]
      (is (= :accepted (:status result)))
      (is (str/includes? (:text result) (str "severity=" severity)))))
  (let [input (-> (diagnostic)
                  (assoc :rule (apply str (repeat 128 \u0000)))
                  (assoc-in [:primary :span :actual-source-path]
                            (apply str (repeat 512 \u0000)))
                  (assoc-in [:primary :span :start :line] 2147483647)
                  (assoc-in [:primary :span :start :column] 2147483647)
                  (assoc-in [:primary :span :end :line] 2147483647)
                  (assoc-in [:primary :span :end :column] 2147483647))
        result (render input)]
    (is (= :accepted (:status result)))
    (is (<= (count (:text result)) 4096))
    (is (= (count (:text result))
           (alength (.getBytes ^String (:text result) "US-ASCII"))))
    (is (ascii-line? (:text result)))))

(deftest sh15-bounded-text-absent-redacted-and-inert-private-fields
  (doseq [primary [nil {} {:span nil}]]
    (let [result (render (assoc (diagnostic) :primary primary))]
      (is (= :accepted (:status result)))
      (is (str/includes? (:text result) "location=<absent>"))))
  (let [calls (atom 0)
        private (reify Object
                  (toString [_] (swap! calls inc)
                    (throw (ex-info "private value must stay inert" {}))))]
    (doseq [[redactions state] [[nil :absent] [:not-evaluated :not-evaluated]
                                [[private] :recorded]]]
      (let [input (assoc (diagnostic) :redactions redactions :primary private
                         :facts private :message private :origin-chain private)
            result (render input)]
        (is (= :accepted (:status result)))
        (is (= state (:redaction-status result)))
        (is (str/includes? (:text result) "location=<redacted>"))
        (is (= 0 @calls))))
    (is (= :absent (:redaction-status
                    (render (dissoc (diagnostic) :redactions)))))
    (is (= :recorded (:redaction-status
                      (render (assoc (diagnostic) :lifecycle :redacted
                                     :primary private)))))
    (is (= 0 @calls)))
  (let [input (assoc (diagnostic) :rule :C8-CAPABILITY)]
    (is (str/starts-with? (:text (render input)) "code=\":C8-CAPABILITY\""))))

(deftest sh15-bounded-text-rejects-unsupported-input-with-owned-data
  (let [fixtures (invoke @rejected-plan 'coordinator-bounded-diagnostic-text-invalid [])
        cases (concat
               (map vector fixtures
                    [:diagnostic-not-map :empty-rule :unsupported-severity
                     :unsupported-redaction-state :unsupported-rule-character
                     :unsupported-primary-location])
               [[(assoc (diagnostic) :rule (apply str (repeat 129 "A"))) :rule-bound]
                [(assoc (diagnostic) :rule []) :unsupported-rule]
                [(assoc (diagnostic) :rule "\ud83d\ude00") :unsupported-rule-character]
                [(assoc (diagnostic) :redactions (vec (repeat 65 :private)))
                 :unsupported-redaction-state]
                [(assoc-in (diagnostic) [:primary :span :actual-source-path]
                           (apply str (repeat 513 "a"))) :unsupported-primary-location]
                [(assoc-in (diagnostic) [:primary :span :start :line] 0)
                 :unsupported-primary-location]
                [(assoc (diagnostic) :primary []) :unsupported-primary-location]])]
    (doseq [[input reason] cases]
      (let [result (render input) owned (first (:diagnostics result))]
        (is (= :rejected (:status result)) reason)
        (is (not (contains? result :text)) reason)
        (is (= "C15-SCHEMA" (:rule owned)) reason)
        (is (= reason (get-in owned [:facts :reason])))
        (is (= :compiler-diagnostics (:stage owned)))
        (is (nil? (:offending-diagnostic-id owned)))
        (is (nil? (:artifact-id owned)))
        (is (= [:supply-bounded-ascii-diagnostic-view-fields]
               (:remediation owned)))))))

(deftest sh15-bounded-text-original-c15-producer-and-fresh-verification
  ;; Actual original C15 rejection producer; no authenticated SH13/SH14 replay.
  (let [request {}
        original (c15 'sh15-build-diagnostic-boundary [request])
        verification (c15 'sh15-verify-diagnostic-boundary [request original])
        input (first (:diagnostics original))
        before (pr-str original)
        result (render input)
        fresh-plan (compile-plan c15-path)]
    (is (= :rejected (:status original)))
    (is (= :rejected (:status verification)))
    (is (= :request-schema-mismatch
           (get-in verification [:diagnostics 0 :facts :reason])))
    (is (= "C15-SCHEMA" (:rule input)))
    (is (= :request-schema-mismatch (get-in input [:facts :reason])))
    (is (= :accepted (:status result)))
    (is (= "code=\"C15-SCHEMA\" severity=:error location=<redacted> redactions=:recorded"
           (:text result)))
    (is (= before (pr-str original)))
    (is (= original (invoke fresh-plan 'sh15-build-diagnostic-boundary [request])))
    (is (= verification
           (invoke fresh-plan 'sh15-verify-diagnostic-boundary [request original])))
    (is (= result (invoke fresh-plan 'sh15-render-bounded-diagnostic-text [input])))
    (let [visible (diagnostic)
          changed (assoc-in visible [:primary :span :actual-source-path]
                            "src/changed.gravity")
          changed-result
          (invoke fresh-plan 'sh15-render-bounded-diagnostic-text [changed])]
      (is (not= (:text (render visible)) (:text changed-result)))
      (is (str/includes? (:text changed-result) "src/changed.gravity"))
      (is (= (:diagnostic-id visible) (:diagnostic-id changed))))
    (is (= input (first (:diagnostics original))))
    (is (= result (render (assoc input :message "different wording"))))
    (is (= (c15 'sh15-nonclaims []) (invoke fresh-plan 'sh15-nonclaims [])))
    (is (some #{:diagnostic-rendering} (c15 'sh15-nonclaims [])))))
