(ns gravity.source-unit
  "Stage 0 source-unit extension metadata.

  This leaf owns only the co-canonical source-extension policy and the
  extension-derived provenance kind. Project-relative paths, source identity,
  reader options, and source-unit artifacts remain with the reader/source-unit
  pass until their wider artifact-identity dependencies are extracted.")

(def ^:private namespace-contract
  {:namespace 'gravity.source-unit
   :contract-boundary :stage0-co-canonical-source-extension-metadata
   :public-api
   {'co-canonical-source-extensions
    {:kind :constant
     :returns :set-of-source-extensions}
    'gravity-source-extension
    {:arglists '([path])
     :returns :extension-or-nil}
    'qst-or-gravity-source?
    {:arglists '([path])
     :returns :boolean}
    'gravity-source-kind
    {:arglists '([path])
     :returns :source-kind}}
   :artifact-inputs [:supplied-source-path]
   :artifact-outputs [:source-extension :source-kind]
   :ownership
   {:owns [:co-canonical-extension-policy
           :extension-derived-source-kind
           :actual-path-extension-provenance]
    :does-not-own [:project-relative-path
                   :source-unit-identity
                   :reader-options
                   :source-bytes
                   :source-content-hash
                   :source-unit-artifact
                   :source-extension-diagnostics]}
   :dependency-direction
   {:requires ['clojure.core 'java.io.File]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner 'gravity.source-unit-test/co-canonical-source-extension-policy-is-extracted
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(def co-canonical-source-extensions #{".qst" ".gravity"})

(defn gravity-source-extension
  "Return the final dot extension of a supplied source path.

  The stage0 compatibility contract intentionally follows the existing
  bootstrap behavior: paths are coerced with `str`, `java.io.File` supplies
  the platform path-name semantics, a leading dot is not an extension, and
  extension matching remains case-sensitive."
  [path]
  (let [name (.getName (java.io.File. (str path)))
        dot (.lastIndexOf name ".")]
    (when (pos? dot)
      (subs name dot))))

(defn qst-or-gravity-source?
  "Return true only for the two co-canonical source extensions."
  [path]
  (contains? co-canonical-source-extensions
             (gravity-source-extension path)))

(defn gravity-source-kind
  "Return the source provenance kind associated with a supplied path.

  Unknown extensions intentionally retain the historical generic
  `:gravity-source` fallback. Callers that enforce the extension policy must
  reject the path before reading it."
  [path]
  (case (gravity-source-extension path)
    ".qst" :qst-theory-source
    ".gravity" :gravity-branded-source
    :gravity-source))
