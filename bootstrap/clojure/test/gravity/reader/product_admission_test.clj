(ns gravity.reader.product-admission-test
  "SOURCE/MODELED admission checks. Reads application forms as data and runs
  only the new refusal statement with synthetic ports. No Bootstrap, Reader,
  handler, provider, plan runner or envelope implementation is loaded."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]))

(def ^:private components
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/")

(defn- source-definition [path name]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop []
        (let [form (read {:eof ::end} reader)]
          (when (= ::end form)
            (throw (ex-info "Missing definition" {:path path :name name})))
          (if (and (seq? form) (= name (second form))) form (recur)))))))

(def ^:private precomputed-body
  (source-definition
   (str components "self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj")
   'sh03-c3-precomputed-c2-verify!))

(defn- modeled-admission [guard]
  ;; Continuation is a sentinel, not a simulated successful verification.
  (eval (list 'fn '[c2-reader-fail! source-path c2-artifact continue!]
              guard '(continue!))))

(deftest pure-presence-refuses-before-standard-verification
  (let [arity (last precomputed-body)
        guard (some #(when (= '(contains? c2-artifact :pure-extension-results) (second %)) %)
                    (rest arity))
        _ (is (= 'when (first guard)))
        _ (is (= '(contains? c2-artifact :pure-extension-results) (second guard)))
        admission (modeled-admission guard)]
    (doseq [source-path ["paired.qst" "paired.gravity"]
            companion [nil [] [{:artifact :gravity/reader-pure-extension-result}]]]
      (let [calls (atom [])
            failure (try
                      (admission
                       (fn [id path context facts]
                         (swap! calls conj [id path context facts])
                         (throw (ex-info id facts)))
                       source-path {:pure-extension-results companion}
                       #(swap! calls conj :continued))
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
        (is (= {:missing-fields [:authenticated-pure-extension-results]}
               (ex-data failure)))
        (is (= [["C2-HASH" source-path {:stage :read-source}
                 {:missing-fields [:authenticated-pure-extension-results]}]]
               @calls))))
    (let [continued (atom 0)]
      (admission (fn [& _] (throw (ex-info "Unexpected standard refusal" {})))
                 "paired.gravity" {} #(swap! continued inc))
      (is (= 1 @continued)))
    ;; Removing the statement reaches the continuation for the same input.
    ;; This demonstrates that the presence oracle detects guard removal.
    (let [continued (atom 0)]
      ((modeled-admission nil) nil "paired.qst"
       {:pure-extension-results []} #(swap! continued inc))
      (is (= 1 @continued)))))

(deftest standard-authority-and-existing-guards-are-retained
  (let [body (last (last precomputed-body))
        nodes (tree-seq coll? seq body)]
    (is (= 'let (first body)))
    (is (some #{'(identical? candidate sh03-reader-internal-product-authority)} nodes))
    (is (some #{'(= expected-binding (:plan-binding boundary))} nodes))
    (is (some #{'p15-s23-stage2-sh02-descriptor-envelope-verify!} nodes)))
  (let [view (source-definition "bootstrap/clojure/src/gravity/syntax.clj"
                                'reader-artifact-view)
        guard (some #(when (= '(contains? c2-artifact :pure-extension-results) (second %)) %)
                    (drop 3 view))
        products (source-definition
                  (str components "self-hosting-sh03-sh07/00828-sh03-reader-internal-product-authority.clj")
                  'sh03-reader-precomputed-products-verify!)]
    (is (some #{:authenticated-pure-extension-results} (tree-seq coll? seq guard)))
    (is (some #{'(not (contains? products :pure-extension-results))}
              (tree-seq coll? seq products)))))

(deftest producer-boundary-remains-explicit
  (let [revision (source-definition
                  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity"
                  'ae-source-revision-valid?)
        raw-verifier (source-definition
                      "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
                      'sh03-verify-reader-result)
        nodes (tree-seq coll? seq raw-verifier)]
    (is (some #{'(= (get revision :source-language) :gravity)}
              (tree-seq coll? seq revision)))
    (is (some #{'(sh03-read-source-unit source-unit source-bytes reader-policy)} nodes))
    (is (not (some #{:pure-extension-results} nodes))))
  (let [constructor (source-definition
                     (str components "compiler/00829-compiler-c2-reader-source-artifact.clj")
                     'compiler-c2-reader-source-artifact)]
    (is (some #{'(compiler-c2-reader-source-artifact
                  source-path source-text project-context precomputed-products candidate
                  reader-source-path invocation standard-reader-options)}
              (tree-seq coll? seq constructor)))))

(deftest paired-definitions-are-source-only
  (let [packet (edn/read-string
                (slurp "specs/reader-workflow/sealed-pure-producer-definitions.edn"))
        pairs (:source-pairs packet)]
    (is (= :unrun (:execution-status packet)))
    (is (false? (:implemented-selector? packet)))
    (is (= #{:text :mixed-unknown :mixed-effectful} (set (map :case pairs))))
    (doseq [{:keys [paths]} pairs]
      (is (= 2 (count paths)))
      (is (.endsWith ^String (first paths) ".qst"))
      (is (.endsWith ^String (second paths) ".gravity"))
      (is (= (slurp (first paths)) (slurp (second paths)))))
    (is (= 14 (count (:future-tamper-controls packet))
           (count (set (map :field (:future-tamper-controls packet))))))
    (doseq [refusal (vals (:future-refusals packet))]
      (is (= {:diagnostic "C2-EXTENSION" :handler-calls 0 :provider-calls 0} refusal)))))
