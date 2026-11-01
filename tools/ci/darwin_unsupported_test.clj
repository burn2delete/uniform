(ns ci.darwin-unsupported-test
  "Actual unsupported-kernel refusal; no publication I/O is permitted."
  (:require [gravity.darwin-publication :as publication]
            [gravity.darwin-publication.stat :as stat]))

(let [publication-opens (atom 0)
      result (with-redefs [stat/open-absolute-directory!
                           (fn [& _]
                             (swap! publication-opens inc)
                             (throw (AssertionError. "Publication I/O reached on unsupported kernel")))]
               (try
                 (publication/open-target! "/uniform-ci-missing-parent/bundle")
                 nil
                 (catch clojure.lang.ExceptionInfo error (ex-data error))))]
  (prn {:actual-unsupported-host true :failure result :publication-opens @publication-opens})
  (assert (= :native-preflight (:operation result)))
  (assert (= :unsupported-confinement-features (:reason result)))
  (assert (= 22 (:errno result)))
  (assert (= -1 (:return-code result)))
  (assert (zero? @publication-opens)))
