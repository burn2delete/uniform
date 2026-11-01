(ns ci.snapshot-diagnostics-test
  (:require [clojure.test :refer [deftest is run-tests]] [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio.file Files] [java.nio.file.attribute FileAttribute]))

(binding [*command-line-args* nil] (load-file "tools/ci/snapshot_diagnostics.clj"))
(require '[ci.snapshot-diagnostics :as diagnostic])

(defn fixture [f]
  (let [base (.toFile (Files/createTempDirectory "snapshot-diagnostic-DATA-" (make-array FileAttribute 0)))
        dirs (mapv #(doto (io/file base %) .mkdirs) ["project" "home" "install" "capture"])
        write! (fn [file text] (.mkdirs (.getParentFile file)) (spit file text))]
    (try (f dirs write!)
         (finally (doseq [file (reverse (file-seq base))] (io/delete-file file true))))))

(deftest all-uri-schemes-redact-before-path-normalization
  ;; Evaluate only the exact pure normalizer form with synthetic roots. The
  ;; collector, host cache, environment and project namespaces are not invoked.
  (let [forms (binding [*read-eval* false]
                (with-open [reader (java.io.PushbackReader.
                                    (io/reader "tools/ci/snapshot_diagnostics.clj"))]
                  (loop [forms []]
                    (let [form (read {:eof ::end} reader)]
                      (if (= ::end form) forms (recur (conj forms form)))))))
        collect-form (some #(when (and (seq? %) (= 'defn (first %))
                                       (= 'collect (second %))) %) forms)
        normalizer (some (fn [[name form]] (when (= 'normalize name) form))
                         (partition 2 (second (nth collect-form 3))))
        roots [["/DATA/project" "$WORKSPACE" "/DATA/project"]
               ["/DATA/home" "$HOME" "/DATA/home"]]
        normalize (binding [*ns* (the-ns 'ci.snapshot-diagnostics-test)]
                    (eval (list 'let ['roots roots] normalizer)))
        body "DATA-user:DATA-secret:DATA-tail@example.invalid:9443/private:part?token=DATA-token#DATA-fragment"]
    (doseq [scheme ["http" "HTTPS" "HtTpS" "ssh" "SSH" "s3" "S3" "a" "custom+v1.2-X"]]
      (let [text (normalize (str "prefix " scheme "://" body " suffix"))]
        (is (= "prefix <url-redacted> suffix" text) scheme)
        (doseq [marker ["DATA-user" "DATA-secret" "DATA-tail" "DATA-token"
                        "DATA-fragment" "example.invalid" "private:part"]]
          (is (not (str/includes? (pr-str text) marker)) [scheme marker]))))
    (is (= "<url-redacted>"
           (normalize "s3://example.invalid/private?token=DATA-token")))
    (is (= "\"<url-redacted>\" <<url-redacted>>\n<url-redacted>"
           (normalize (str "\"ssh://" body "\" <S3://" body ">\na+.-://" body))))
    (doseq [[text expected] [["DATA-key: DATA-value" "DATA-key: DATA-value"]
                             ["relative/clojure/spec/alpha" "relative/clojure/spec/alpha"]
                             ["/DATA/project/file.edn" "$WORKSPACE/file.edn"]
                             ["/DATA/home/cache" "$HOME/cache"]
                             ["/outside/file.edn" "<external-path>"]
                             ["already <url-redacted>" "already <url-redacted>"]]]
      (is (= expected (normalize text))))))

(deftest finite-data-capture-normalizes-and-authenticates-without-execution
  (fixture
   (fn [[root home install capture :as dirs] write!]
     (write! (io/file install "lib/clojure/deps.edn") "{:deps {org.clojure/clojure {:mvn/version \"1.12.5\"}}}")
     (write! (io/file root ".cpcache/123.cp") (str home "/.m2/repository/org/clojure/clojure/1.12.5/clojure-1.12.5.jar"))
     (write! (io/file root ".cpcache/123.basis") (str "{:classpath-roots [\"" root "\"]}"))
     (write! (io/file root ".cpcache/123.manifest") (str install "/lib/clojure/deps.edn"))
     (write! (io/file root ".cpcache/unrelated-secret") "MUST-NOT-CAPTURE")
     (write! (io/file home ".m2/settings.xml") "MUST-NOT-CAPTURE")
     (write! (io/file capture "init.stderr")
             "Could not locate clojure/spec/alpha; https://user:secret@example.test/private HTTPS://DATA-user:UPPER-TOKEN@example.invalid/a HtTp://DATA-user:MIXED-TOKEN@example.invalid/a")
     (write! (io/file capture "launcher.txt") (str install "/bin/clojure"))
     (let [result (apply diagnostic/collect dirs) text (pr-str result)]
       (is (= :observed (:cache-status result)))
       (is (false? (:dependency-resolution-performed result)))
       (is (= "$CLOJURE_INSTALL/bin/clojure" (get-in result [:files "launcher-path" :text])))
       (is (str/starts-with? (get-in result [:files "classpath-cache/123.cp" :text]) "$HOME/"))
       (is (= :observed (get-in result [:files "classpath-cache/123.basis" :status])))
       (is (= :missing (get-in result [:files "spec.alpha/0.5.238.jar" :status])))
       (is (re-matches #"[0-9a-f]{64}" (get-in result [:files "installed-deps" :sha256])))
       (is (= "Could not locate clojure/spec/alpha; <url-redacted> <url-redacted> <url-redacted>"
              (get-in result [:files "init-stderr" :text])))
       (doseq [secret [(str (.getParentFile root)) "MUST-NOT-CAPTURE" "user:secret"
                       "DATA-user" "UPPER-TOKEN" "MIXED-TOKEN"]]
         (is (not (str/includes? text secret))))))))

(deftest missing-oversize-and-linked-inputs-stay-explicit
  (fixture
   (fn [[root home _install _capture :as dirs] write!]
     (write! (io/file root ".cpcache/9.cp") (apply str (repeat (inc diagnostic/text-cap) "x")))
     (let [outside (io/file home "secret") link (io/file root ".cpcache/9.basis")]
       (write! outside "MUST-NOT-CAPTURE")
       (Files/createSymbolicLink (.toPath link) (.toPath outside) (make-array FileAttribute 0))
       (let [result (apply diagnostic/collect dirs)]
         (is (= :capture-limit (get-in result [:files "classpath-cache/9.cp" :id])))
         (is (= :linked-or-outside-path (get-in result [:files "classpath-cache/9.basis" :status])))
         (is (= :missing (get-in result [:files "installed-deps" :status])))
         (is (not (str/includes? (pr-str result) "MUST-NOT-CAPTURE"))))))))

(let [result (run-tests 'ci.snapshot-diagnostics-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
