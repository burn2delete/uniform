; Semantic decomposition of committed HEAD reader line 150856.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh05-macro-artifact-verification*-carrier
 [state]
 (clojure.core/let
  [{:keys [artifact allow-missing-proof?]}
   state
   source-path
   (get-in artifact [:provenance :source-path])
   boundary
   (:gravity-macro-boundary artifact)
   c3-artifact
   (:authenticated-sh04-artifact boundary)
   c3-boundary
   (:gravity-syntax-boundary c3-artifact)
   gravity-verifiers
   (try
    (sh05-macro-gravity-verifier-report source-path boundary)
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch Throwable _ {:template {:status :failed}, :resolved {:status :failed}}))
   output-envelope-ok?
   (try
    (=
     :passed
     (p15-s23-stage2-sh02-descriptor-envelope-verify!
      (:authenticated-envelope boundary)
      sh05-macro-envelope-stage
      sh05-macro-sealed-artifact-kind
      (:authenticated-envelope-descriptor boundary)
      source-path))
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch Throwable _ false))
   binding
   (sh05-macro-current-binding! source-path)
   embedded-descriptor
   (:authenticated-envelope-descriptor boundary)
   expected-descriptor
   (try
    (sh05-macro-sh02-descriptor source-path binding (sh05-macro-envelope-summary artifact))
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch Throwable _ nil))
   descriptor-current?
   (= expected-descriptor embedded-descriptor)
   sh03-current?
   (try
    (true?
     (c3-syntax-stream-reader-products-authentic?
      (:syntax-object-stream c3-artifact)
      (:c2-reader-artifact c3-artifact)
      c3-boundary))
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch Throwable _ false))
   sh04-current?
   (try
    (and
     (= (:artifact-id c3-artifact) (c3-artifact-id c3-artifact))
     (= :complete (:status (c3-syntax-capability-proof c3-artifact))))
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch Throwable _ false))
   template-passed?
   (= :passed (get-in gravity-verifiers [:template :status]))
   resolved-passed?
   (= :passed (get-in gravity-verifiers [:resolved :status]))]
  (clojure.core/assoc
   state
   :source-path
   source-path
   :boundary
   boundary
   :c3-artifact
   c3-artifact
   :c3-boundary
   c3-boundary
   :gravity-verifiers
   gravity-verifiers
   :output-envelope-ok?
   output-envelope-ok?
   :binding
   binding
   :embedded-descriptor
   embedded-descriptor
   :expected-descriptor
   expected-descriptor
   :descriptor-current?
   descriptor-current?
   :sh03-current?
   sh03-current?
   :sh04-current?
   sh04-current?
   :template-passed?
   template-passed?
   :resolved-passed?
   resolved-passed?)))
