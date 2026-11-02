(ns gravity.syntax.selected-git-configuration
  "Pure candidate comparison, not an active Git binding or admission policy."
  (:require [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets) (java.security MessageDigest)))

(defn- digest [^bytes bytes]
  {:bytes (alength bytes)
   :sha256 (format "%064x" (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes)))})

(defn- bytes-of [^String text]
  (.getBytes text StandardCharsets/ISO_8859_1))

(defn candidate
  "Compare opaque bytes after removing only complete canonical origin-tracking
  sections for other codex branches. No config values or opaque bytes escape.
  Includes, backslash escapes, or an unknown current branch select strict mode.
  This deliberately does not establish effective native configuration coverage."
  [^bytes bytes current-branch]
  (let [text (String. bytes StandardCharsets/ISO_8859_1)
        strict? (or (not (re-matches #"refs/heads/[A-Za-z0-9._/-]+" (or current-branch "")))
                    (str/includes? text "\\")
                    (re-find #"(?im)^[\t ]*\[[\t ]*include(?:if)?(?:[\t .\"]|\])" text))
        ;; A possible header, even an unsupported one, ends the previous block.
        blocks (str/split text #"(?m)(?=^[\t ]*\[)")
        classified
        (mapv (fn [block]
                (let [[_ branch remote merge]
                      (when-not strict?
                        (re-matches
                         #"\[branch \"(codex/[A-Za-z0-9._/-]+)\"\]\n\tremote = (origin)\n\tmerge = (refs/heads/[A-Za-z0-9._/-]+)\n"
                         block))]
                  (if (and branch (not= current-branch (str "refs/heads/" branch)))
                    {:excluded {:branch branch :remote remote :merge merge}}
                    {:opaque block}))) blocks)
        opaque (apply str (keep :opaque classified))]
    {:schema :selected-Git-config-candidate-v1 :active-binding? false
     :mode (if strict? :strict :canonical-noncurrent-origin-tracking)
     :raw (digest bytes) :opaque (digest (bytes-of opaque))
     :excluded (mapv :excluded (filter :excluded classified))
     :native-configuration-coverage :unestablished}))
