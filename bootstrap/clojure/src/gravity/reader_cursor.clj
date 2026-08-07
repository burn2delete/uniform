(ns gravity.reader-cursor
  "Stage 0 reader cursor movement across ignored source text.

  This leaf owns only comment and whitespace cursor advancement over the
  Clojure seed reader. It does not parse forms, classify diagnostics, or
  construct source artifacts."
  (:require [gravity.source-span :as source-span])
  (:import [clojure.lang LineNumberingPushbackReader]))

(def ^:private namespace-contract
  {:namespace 'gravity.reader-cursor
   :contract-boundary :stage0-reader-ignored-input-cursor
   :public-api
   {'skip-line-comment!
    {:arglists '([rdr])
     :returns :nil}
    'skip-ignored!
    {:arglists '([rdr])
     :returns #{:eof :form}}}
   :artifact-inputs [:line-numbering-pushback-reader]
   :artifact-outputs [:advanced-reader-cursor :reader-readiness]
   :ownership
   {:owns [:line-comment-cursor-advancement
           :ignored-input-cursor-advancement
           :next-form-readiness]
    :does-not-own [:form-reading
                   :form-parsing
                   :reader-diagnostic-classification
                   :diagnostic-construction
                   :source-span-construction
                   :reader-artifact-construction
                   :bootstrap-orchestration]}
   :dependency-direction
   {:requires ['clojure.core
               'clojure.lang.LineNumberingPushbackReader
               'gravity.source-span]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn skip-line-comment!
  [^LineNumberingPushbackReader rdr]
  (loop [ch (.read rdr)]
    (when (and (not= -1 ch)
               (not (source-span/line-terminator-char? (char ch))))
      (recur (.read rdr)))))

(defn skip-ignored!
  [^LineNumberingPushbackReader rdr]
  (loop []
    (let [ch (.read rdr)]
      (cond
        (= -1 ch) :eof
        (Character/isWhitespace (char ch)) (recur)
        (= \; (char ch)) (do (skip-line-comment! rdr) (recur))
        :else (do (.unread rdr ch) :form)))))
