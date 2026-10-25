(ns gravity.self-hosting.sh15-public-encoding-text-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh15-bounded-diagnostic-text-test]
            [gravity.self-hosting.sh15-public-encoding-diagnostic-test]))

(def ^:private originals #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/originals)
(def ^:private fixture #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/fixture)
(def ^:private fields #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/fields)
(def ^:private c15 #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/c15)
(def ^:private resolve-private #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-public-encoding-diagnostic-test/verify)
(def ^:private escaped-ascii #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/escaped-ascii)
(def ^:private ascii-line? #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/ascii-line?)
(defn- row [extension]
  (let [arguments (fields (get (force @originals) extension)) source-path (fixture extension)]
    (assoc (resolve-private source-path arguments) :arguments arguments :source-path source-path)))
(defn- render [candidate] (c15 'sh15-render-bounded-diagnostic-text [candidate]))
(defn- golden [candidate]
  (let [span (get-in candidate [:primary :span])]
    (str "code=\"" (escaped-ascii (str (:rule candidate))) "\" severity=:error location=\""
         (escaped-ascii (:source span)) "\":bytes[" (:byte-start span) "," (:byte-end span)
         ") redactions=:clear")))

(deftest public-encoding-records-reach-a-byte-range-view-without-decoded-points
  (doseq [extension ["gravity" "qst"]]
    (let [{:keys [arguments source-path request candidate]} (row extension)
          before (pr-str candidate) verification (verify source-path arguments candidate request)
          result (render candidate)]
      (is (= :passed (:status verification)))
      (is (= :accepted (:status result)))
      (is (= :bounded-ascii-bytes-v1 (:rendering-version result)))
      (is (= (golden candidate) (:text result)))
      (is (= before (pr-str candidate))))))

(deftest byte-range-view-retains-bounds-escaping-and-no-authority-contract
  (let [candidate (:candidate (row "gravity")) result (render candidate)]
    (is (= :clear (:redaction-status result)))
    (is (= :none (:semantic-authority result)))
    (is (every? (set (:nonclaims result))
                [:diagnostic-authority :redaction-policy :stream-ordering :cli-integration
                 :complete-c15-conformance :self-hosting :release]))
    ;; Synthetic display controls carry byte offsets, never line/column points.
    (doseq [input [(assoc-in candidate [:primary :span :source] "src/\"tab\tline\nback\\slash.gravity")
                   (assoc-in candidate [:primary :span :source] (apply str (map char (range 128))))
                   (-> candidate (assoc :rule (apply str (repeat 128 \u0000)))
                       (assoc-in [:primary :span :source] (apply str (repeat 512 \u0000)))
                       (assoc-in [:primary :span :byte-start] 2147483647)
                       (assoc-in [:primary :span :byte-end] 2147483647))]]
      (let [view (render input)]
        (is (= :accepted (:status view)))
        (is (= :bounded-ascii-bytes-v1 (:rendering-version view)))
        (is (= (golden input) (:text view)))
        (is (ascii-line? (:text view)))
        (is (<= (count (:text view)) 4096))
        (is (not (contains? (get-in input [:primary :span]) :start)))
        (is (not (contains? (get-in input [:primary :span]) :end)))))))

(deftest byte-range-view-rejects-malformed-selected-fields-with-owned-data
  (let [candidate (:candidate (row "gravity")) span (get-in candidate [:primary :span])]
    (doseq [malformed [(dissoc span :file) (assoc span :file "sha256:bad")
                       (assoc span :source "") (assoc span :source "src/\u00e9.gravity")
                       (assoc span :source (apply str (repeat 513 "a")))
                       (assoc span :byte-start -1) (assoc span :byte-end 1.5)
                       (assoc span :byte-start 2 :byte-end 1)
                       (assoc span :byte-end 2147483648) (assoc span :start nil)
                       (assoc span :end nil) (assoc span :extra true)]]
      (let [result (render (assoc-in candidate [:primary :span] malformed))]
        (is (= :rejected (:status result)))
        (is (not (contains? result :text)))
        (is (= "C15-SCHEMA" (get-in result [:diagnostics 0 :rule])))
        (is (= :unsupported-primary-location (get-in result [:diagnostics 0 :facts :reason])))))))

(deftest byte-range-view-does-not-inspect-private-values-when-location-is-not-cleared
  (let [candidate (:candidate (row "gravity")) calls (atom 0)
        private (reify Object
                  (toString [_] (swap! calls inc) (throw (ex-info "private" {}))))
        input (assoc candidate :facts private :message private :origin-chain private
                               :related private :remediation private :private-output private)]
    (is (= (:text (render candidate)) (:text (render input))))
    (doseq [redactions [nil :not-evaluated [private]]]
      (let [result (render (assoc input :primary private :redactions redactions))]
        (is (= :accepted (:status result)))
        (is (= :bounded-ascii-v1 (:rendering-version result)))
        (is (str/includes? (:text result) "location=<redacted>"))))
    (let [result (render (assoc input :primary private :lifecycle :redacted))]
      (is (= :accepted (:status result)))
      (is (= :bounded-ascii-v1 (:rendering-version result)))
      (is (= :recorded (:redaction-status result)))
      (is (str/includes? (:text result) "location=<redacted>")))
    (is (= :rejected (:status (render (assoc-in candidate [:primary :span :file] private)))))
    (is (= 0 @calls))))
