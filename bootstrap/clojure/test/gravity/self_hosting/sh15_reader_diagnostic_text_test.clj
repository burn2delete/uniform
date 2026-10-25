(ns gravity.self-hosting.sh15-reader-diagnostic-text-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh15-bounded-diagnostic-text-test]
            [gravity.self-hosting.sh15-resolved-reader-diagnostic-test]))

(def ^:private original
  #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/original)
(def ^:private fields #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/fields)
(def ^:private c15 #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/c15)
(def ^:private path #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/path)
(def ^:private resolve-private
  #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/verify)
(def ^:private escaped-ascii
  #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/escaped-ascii)
(def ^:private ascii-line?
  #'gravity.self-hosting.sh15-bounded-diagnostic-text-test/ascii-line?)

(defn- row [family extension & [physical-path]]
  (let [source (original family extension physical-path)
        arguments (fields (:event source)) source-path (path family extension)]
    (assoc (resolve-private source-path arguments)
           :source source :arguments arguments :source-path source-path)))
(defn- render [candidate] (c15 'sh15-render-bounded-diagnostic-text [candidate]))
(defn- golden [candidate]
  (let [span (get-in candidate [:primary :span]) start (:start span)]
    (str "code=\"" (:rule candidate) "\" severity=:error location=\""
         (escaped-ascii (:source span)) "\":" (:line start) ":" (:column start)
         " redactions=:clear")))

(deftest original-resolved-reader-diagnostics-reach-the-existing-bounded-text-view
  (doseq [family ["delimiter" "duplicate-set"]]
    (let [rows
          (mapv
           (fn [extension]
             (let [{:keys [source arguments source-path request candidate]} (row family extension)
                   before (pr-str [source candidate])
                   verification (verify source-path arguments candidate request)
                   result (render candidate)]
               (is (= :rejected (get-in source [:result :status])))
               (is (= :passed (:status verification)))
               (is (= :accepted (:status result)))
               (is (= (golden candidate) (:text result)))
               (is (= :clear (:redaction-status result)))
               (is (= :bounded-ascii-v1 (:rendering-version result)))
               (is (= :none (:semantic-authority result)))
               (is (ascii-line? (:text result)))
               (is (every? (set (:nonclaims result))
                           [:diagnostic-authority :redaction-policy :stream-ordering
                            :cli-integration :complete-c15-conformance :self-hosting :release]))
               (is (= before (pr-str [source candidate])))
               (when (= family "duplicate-set") (is (seq (:related candidate))))
               [candidate result]))
           ["gravity" "qst"])]
      (is (= (get-in rows [0 0 :diagnostic-id]) (get-in rows [1 0 :diagnostic-id])))
      (is (not= (get-in rows [0 1 :text]) (get-in rows [1 1 :text]))))))

(deftest reader-path-display-escapes-ascii-and-preserves-path-neutral-identity
  (let [base (row "delimiter" "gravity")
        paths ["src/\"tab\tline\nback\\slash.gravity" (apply str (map char (range 128)))
               (apply str (repeat 512 \u0000))]]
    (doseq [physical-path paths]
      (let [{:keys [source-path arguments request candidate]} (row "delimiter" "gravity" physical-path)
            result (render candidate)]
        (is (= :passed (:status (verify source-path arguments candidate request))))
        (is (= (:diagnostic-id (:candidate base)) (:diagnostic-id candidate)))
        (is (= physical-path (get-in candidate [:primary :span :source])))
        (is (= :accepted (:status result)))
        (is (= (golden candidate) (:text result)))
        (is (ascii-line? (:text result)))
        (is (not= (:text (render (:candidate base))) (:text result)))))))

(deftest malformed-reader-locations-retain-owned-bounded-view-rejections
  (let [candidate (:candidate (row "duplicate-set" "gravity"))
        span (get-in candidate [:primary :span])
        mutations [(dissoc span :source) (assoc span :extra true)
                   (assoc span :source nil) (assoc span :source "")
                   (assoc span :source "src/\u00e9.gravity")
                   (update span :byte-start inc) (update span :byte-end inc)
                   (assoc-in span [:start :column] 0) (assoc-in span [:end :line] 0)
                   (assoc-in span [:start :char] 2147483648)
                   (assoc-in span [:end :char] -1)
                   (assoc-in span [:start :column-unit] :utf16)
                   (assoc-in span [:end :column-unit] :utf16)
                   (assoc-in span [:start :extra] true)
                   (assoc span :start :unavailable)
                   (-> span (assoc-in [:end :byte] 0) (assoc :byte-end 0))]]
    (doseq [malformed mutations]
      (let [result (render (assoc-in candidate [:primary :span] malformed))
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (not (contains? result :text)))
        (is (= "C15-SCHEMA" (:rule diagnostic)))
        (is (= :unsupported-primary-location (get-in diagnostic [:facts :reason])))
        (is (= :primary (get-in diagnostic [:facts :schema-field])))
        (is (nil? (:offending-diagnostic-id diagnostic)))
        (is (nil? (:artifact-id diagnostic)))))))

(deftest reader-rendering-keeps-private-fields-inert-and-location-disclosure-explicit
  (let [candidate (:candidate (row "delimiter" "gravity")) calls (atom 0)
        private (reify Object
                  (toString [_] (swap! calls inc) (throw (ex-info "private" {}))))
        inert (assoc candidate :facts private :message private :remediation private
                              :origin-chain private :related private :private-output private)]
    (is (= (:text (render candidate)) (:text (render inert))))
    (doseq [redactions [nil :not-evaluated [private]]]
      (let [result (render (assoc inert :primary private :redactions redactions))]
        (is (= :accepted (:status result)))
        (is (str/includes? (:text result) "location=<redacted>"))))
    (let [result (render (assoc inert :primary private :lifecycle :redacted))]
      (is (= :accepted (:status result)))
      (is (= :recorded (:redaction-status result)))
      (is (str/includes? (:text result) "location=<redacted>")))
    (is (= :rejected (:status (render (assoc-in candidate [:primary :span :source] private)))))
    (is (= 0 @calls))))
