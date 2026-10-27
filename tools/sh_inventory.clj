(ns sh-inventory
  "Read-only extraction of the original SH acceptance population; no task credit."
  (:require [clojure.pprint :as pp]))

(def plan-path "docs/self-hosting-milestone-completion-plan.md")
(def original-plan-sha256 "c625b50f3331e4692804392834a12dc4f0845b449786a69fd1b53b15febde188")
(defn sha256 [s]
  (format "%064x" (java.math.BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes s "UTF-8")))))
(defn source-span [path text start end]
  (let [raw (subs text start end)]
    {:path path :start-byte (alength (.getBytes (subs text 0 start) "UTF-8"))
     :end-byte (alength (.getBytes (subs text 0 end) "UTF-8"))
     :start-line (inc (count (filter #{\newline} (subs text 0 start))))
     :end-line (inc (count (filter #{\newline} (subs text 0 (dec end)))))
     :sha256 (sha256 raw) :raw raw}))
(defn inventory [text]
  (let [m (re-matcher #"(?m)^[1-6]\. \*\*(SH-\d{2}\.[1-6])(?:[^*]*)\*\*:?[\s\S]*?(?=\n[1-6]\. \*\*SH-|\n\nChecks:)" text)
        rows (loop [out []]
               (if (.find m)
                 (recur (conj out {:id (.group m 1) :milestone (subs (.group m 1) 0 5)
                                  :source (source-span plan-path text (.start m) (.end m))})) out))
        expected (set (for [n (range 30) r (range 1 7)] (format "SH-%02d.%d" n r)))]
    (when-not (= (count rows) (count (re-seq #"(?m)^[1-6]\. \*\*SH-" text)))
      (throw (ex-info "Malformed or unconsumed SH acceptance row" {})))
    (when-not (= expected (set (map :id rows)))
      (throw (ex-info "SH population differs from actual 30-by-six source" {:actual (count rows)})))
    (when-not (= (count rows) (count expected)) (throw (ex-info "Duplicate SH requirement" {})))
    rows))
(defn -main [& args]
  (when-not (#{["--write"] ["--check"]} (vec args))
    (throw (ex-info "Usage: clojure -M tools/sh_inventory.clj --write|--check" {})))
  (let [text (slurp plan-path) rows (inventory text)]
    (when-not (= original-plan-sha256 (sha256 text))
      (throw (ex-info "Original SH source changed; preserve source lineage before amending inventory" {})))
    (if (= ["--write"] (vec args))
      (spit "specs/sh-family-crosswalk/original-inventory.edn"
            (with-out-str (pp/pprint {:kind :sh-original-acceptance-inventory
                                     :authority :source-provenance-only
                                     :source-commit "15a5499e97798621d270c36dd2546ce98ddd62a9"
                                     :source-file-sha256 (sha256 (slurp plan-path))
                                     :requirements rows})))
      (println "Verified" (count rows) "original acceptance units across" (count (set (map :milestone rows))) "milestones; no implementation denominator."))))
(when (seq *command-line-args*) (apply -main *command-line-args*))
