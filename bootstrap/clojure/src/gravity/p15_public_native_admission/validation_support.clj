(ns gravity.p15-public-native-admission.validation-support
  (:require [gravity.digest :as digest]
            [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]))

(defn exact-keys?
  [value expected]
  (and (map? value)
       (= expected (set (keys value)))))

(defn identifier-text
  "Normalize only keyword/symbol/string identity fields.

  JSON producers use strings while EDN producers may use symbols or keywords.
  No arbitrary value is coerced; maps, numbers, booleans, and collections are
  never silently accepted as identities."
  [value]
  (cond
    (string? value) value
    (symbol? value) (str value)
    (keyword? value) (let [text (str value)]
                       (subs text 1))
    :else nil))

(defn identifier?
  [value]
  (let [text (identifier-text value)]
    (and (string? text)
         (not (empty? text))
         (not (re-find #"[\s\u0000]" text)))))

(defn same-identity?
  [left right]
  (let [left-text (identifier-text left)
        right-text (identifier-text right)]
    (and (some? left-text) (= left-text right-text))))

(defn sha256?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn commit?
  [value]
  (and (string? value)
       (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn visible-ascii-string?
  [value]
  (and (string? value)
       (not (empty? value))
       (every? (fn [character]
                 (let [code (int character)]
                   (<= 0x21 code 0x7e)))
               value)))

(defn exact-ascii-keyword?
  [value]
  (and (keyword? value)
       (let [text (subs (str value) 1)]
         (visible-ascii-string? text))))

(defn positive-integer?
  [value]
  (and (integer? value) (pos? value)))

(defn normalized-repo-relative-posix-path?
  [value]
  (and (visible-ascii-string? value)
       (not (.startsWith ^String value "/"))
       (not (.endsWith ^String value "/"))
       (not (.contains ^String value "\\"))
       (not (.contains ^String value "//"))
       (not (re-find #"^[A-Za-z]:" value))
       (not (re-find #"(^|/)\.\.?(/|$)" value))))

(defn derive-checkout-root-id
  [payload-containing-commit payload-containing-tree]
  (when (and (commit? payload-containing-commit)
             (commit? payload-containing-tree))
    (str "sha256:"
         (digest/sha256-hex
          (str "gravity-w4-checkout-root-v1\u0000"
               payload-containing-commit
               "\u0000"
               payload-containing-tree)))))

(defn relative-path?
  [value]
  (and (string? value)
       (not (empty? value))
       (not (.startsWith ^String value "/"))
       (not (re-find #"(^|/)\.\.?(/|$)" value))
       (not (re-find #"[\u0000\r\n]" value))))

(defn nonempty-evidence?
  [value]
  (and (some? value)
       (or (and (string? value) (not (empty? value)))
           (and (identifier? value) (not (empty? (identifier-text value))))
           (and (coll? value) (seq value))
           (and (map? value) (seq value)))))

(defn exact-structured-values?
  [value expected-keys]
  (and (exact-keys? value expected-keys)
       (every? coll? (vals value))))

(defn os-gate-target
  "Return the target identity carried by a scalar or target-bearing gate.

  W3 implementations may carry a scalar target identity or a structured gate
  record.  A structured record must expose :target; no other field can
  silently stand in for the target binding."
  [os-gate]
  (when (exact-keys? os-gate os-gate-keys)
    (:target os-gate)))

(defn issue
  [code path]
  {:id p18-id
   :diagnostic p18-id
   :code code
   :path path})

(defn append-issue
  [issues code path condition]
  (if condition issues (conj issues (issue code path))))
