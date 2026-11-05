

(declare compiler-c3-syntax-source-artifact)

(clojure.core/require 'gravity.macro)

(def c4-macro-diagnostic-ids
  gravity.macro/c4-macro-diagnostic-ids)

(def c4-macro-governing-document
  gravity.macro/c4-macro-governing-document)

(def c4-macro-rejected-designs
  gravity.macro/c4-macro-rejected-designs)

(def c4-macro-override-diagnostics
  gravity.macro/c4-macro-override-diagnostics)

(defn c4-macro-source-overrides
  [module]
  (gravity.macro/source-overrides module))

(defn c4-macro-message
  [id]
  (gravity.macro/diagnostic-message id))

(defn c4-macro-fail!
  [id source-path subject extra]
  (gravity.macro/emit-native-diagnostic!
   id source-path subject extra #'c4-macro-message #'source-span
   #'c4-macro-governing-document #'fail!))

(defn c4-macro-validate-overrides!
  [source-path overrides]
  (gravity.macro/validate-native-overrides!
   source-path overrides #'c4-macro-override-diagnostics #'source-span #'c4-macro-fail!))

(defn c4-artifact-id
  [value]
  (str "sha256:" (sha256-hex (pr-str value))))

(defn c4-macro-environment
  [macro-artifact]
  (c4-macro-evidence/c4-macro-environment
   macro-artifact {:sha256-hex sha256-hex}))

(defn c4-expansion-input
  [module c3-artifact macro-artifact]
  (c4-macro-evidence/c4-expansion-input
   module c3-artifact macro-artifact
   {:artifact-id-of c4-artifact-id}))

(defn c4-expanded-syntax-stream
  [macro-artifact]
  (c4-macro-evidence/c4-expanded-syntax-stream
   macro-artifact {:sha256-hex sha256-hex}))

(defn c4-trace-records
  [macro-artifact]
  (c4-macro-evidence/c4-trace-records macro-artifact))

(defn c4-hygiene-capture-records
  [trace-records]
  (c4-macro-evidence/c4-hygiene-capture-records trace-records))

(defn c4-build-effect-log
  [module trace-records]
  (c4-macro-evidence/c4-build-effect-log module trace-records))

(defn c4-macro-safety-declarations
  [macro-environment]
  (c4-macro-evidence/c4-macro-safety-declarations macro-environment))

(defn c4-generated-origin-source-map
  [trace-records expanded-stream]
  (c4-macro-evidence/c4-generated-origin-source-map
   trace-records expanded-stream))

(defn c4-expansion-cache-key
  [expansion-input trace-records]
  (c4-macro-evidence/c4-expansion-cache-key
   expansion-input trace-records {:sha256-hex sha256-hex}))

(defn c4-trace-replay-report
  [trace-records cache-key]
  (c4-macro-evidence/c4-trace-replay-report trace-records cache-key))

(defn c4-macro-safety-report
  [trace-records safety-declarations]
  (c4-macro-evidence/c4-macro-safety-report
   trace-records safety-declarations))

(defn c4-macro-capability-proof
  [artifact]
  (gravity.macro/native-capability-proof artifact #'c4-macro-diagnostic-ids))
