(ns gravity.self-hosting.sh15-public-namespace-text-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh15-bounded-diagnostic-text-test]
            [gravity.self-hosting.sh15-public-namespace-diagnostic-test]))

(def ^:private originals #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/originals)
(def ^:private fixture #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/fixture)
(def ^:private fields #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/fields)
(def ^:private c15 #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/c15)
(def ^:private resolve-private #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/verify)
(def ^:private escaped-ascii #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/escaped-ascii)
(def ^:private ascii-line? #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/ascii-line?)
(defn- row [extension]
  (let [arguments (fields (get (force @originals) extension)) source-path (fixture extension)]
    (assoc (resolve-private source-path arguments) :arguments arguments :source-path source-path)))
(defn- render [candidate] (c15 'sh15-render-bounded-diagnostic-text [candidate]))
(defn- golden [candidate]
  (let [span (get-in candidate [:primary :span]) start (:start span)]
    (str "code=\"" (:rule candidate) "\" severity=:error location=\""
         (escaped-ascii (:source span)) "\":" (:line start) ":" (:column start)
         " redactions=:clear")))

(deftest public-namespace-records-reach-the-existing-point-view
  (doseq [extension ["gravity" "qst"]]
    (let [{:keys [arguments source-path request candidate]} (row extension)
          before (pr-str candidate) verification (verify source-path arguments candidate request)
          result (render candidate)]
      (is (= :passed (:status verification)))
      (is (= :accepted (:status result)))
      (is (= (golden candidate) (:text result)))
      (is (= before (pr-str candidate))))))

(deftest public-point-view-retains-existing-format-and-semantic-id
  (let [candidate (:candidate (row "gravity")) before (:diagnostic-id candidate)
        result (render candidate)]
    (is (= :bounded-ascii-v1 (:rendering-version result)))
    (is (= :clear (:redaction-status result)))
    (is (= :none (:semantic-authority result)))
    (is (every? (set (:nonclaims result))
                [:diagnostic-authority :redaction-policy :stream-ordering :cli-integration
                 :complete-c15-conformance :self-hosting :release]))
    ;; Synthetic display controls preserve the actual point shape and file ID.
    (doseq [physical-path ["src/\"tab\tline\nback\\slash.gravity"
                           (apply str (map char (range 128)))
                           (apply str (repeat 512 \u0000))]]
      (let [input (assoc-in candidate [:primary :span :source] physical-path)
            view (render input)]
        (is (= :accepted (:status view)))
        (is (= (golden input) (:text view)))
        (is (ascii-line? (:text view)))
        (is (= before (:diagnostic-id input)))))
    (let [five-key (update-in candidate [:primary :span] dissoc :file)]
      (is (= (:text result) (:text (render five-key)))))
    (is (= before (:diagnostic-id candidate)))))

(deftest public-point-view-rejects-malformed-selected-location-fields
  (let [candidate (:candidate (row "gravity")) span (get-in candidate [:primary :span])]
    (doseq [malformed [(assoc span :file "sha256:bad") (assoc span :file :source-id)
                       (assoc span :extra true) (assoc span :source "")
                       (assoc span :source "src/\u00e9.gravity")
                       (update span :byte-end inc) (assoc-in span [:start :column] 0)
                       (assoc-in span [:end :column-unit] :utf16)]]
      (let [result (render (assoc-in candidate [:primary :span] malformed))]
        (is (= :rejected (:status result)))
        (is (not (contains? result :text)))
        (is (= "C15-SCHEMA" (get-in result [:diagnostics 0 :rule])))
        (is (= :unsupported-primary-location (get-in result [:diagnostics 0 :facts :reason])))))))

(deftest public-point-view-keeps-private-values-inert-and-existing-disclosure-gates
  (let [candidate (:candidate (row "gravity")) calls (atom 0)
        private (reify Object
                  (toString [_] (swap! calls inc) (throw (ex-info "private" {}))))
        input (assoc candidate :facts private :message private :origin-chain private
                               :related private :remediation private :private-output private)]
    (is (= (:text (render candidate)) (:text (render input))))
    (doseq [redactions [nil :not-evaluated [private]]]
      (let [result (render (assoc input :primary private :redactions redactions))]
        (is (= :accepted (:status result)))
        (is (str/includes? (:text result) "location=<redacted>"))))
    (let [result (render (assoc input :primary private :lifecycle :redacted))]
      (is (= :accepted (:status result)))
      (is (= :recorded (:redaction-status result)))
      (is (str/includes? (:text result) "location=<redacted>")))
    (is (= :rejected (:status (render (assoc-in candidate [:primary :span :file] private)))))
    (is (= 0 @calls))))
