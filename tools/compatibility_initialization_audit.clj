(ns compatibility-initialization-audit
  "Non-executing source observations for initialization review. Spellings and
  form containment do not resolve calls, expand macros or prove effects."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets CodingErrorAction]
           [java.nio.file Files]))

(binding [*command-line-args* nil] (load-file "tools/compatibility_closure_audit.clj"))
(require '[compatibility-closure-audit :as dependencies])

(def schema :gravity/compatibility-initialization-observations-v1)

(def ^:private lexical-resolver
  ;; Alias names remain lexical labels. No observed namespace is created or
  ;; required, and these labels must never be treated as runtime Var identity.
  (reify clojure.lang.LispReader$Resolver
    (currentNS [_] 'compatibility-source-observation)
    (resolveClass [_ s] s) (resolveAlias [_ s] s) (resolveVar [_ s] s)))

(defn- core-spelling [s]
  (if (and (symbol? s) (contains? #{nil "clojure.core"} (namespace s)))
    (symbol (name s)) s))

(def ^:private body-spellings
  '#{fn fn* defn defn- defmacro delay lazy-seq reify deftype})

(defn- syntax-calls [form region]
  (cond
    (seq? form)
    (let [head (first form) spelling (core-spelling head)]
      (cond
        (contains? '#{quote var} spelling) []
        (contains? body-spellings spelling)
        (into [{:head head :region region :body-spelling true}]
              (mapcat #(syntax-calls % :body) (rest form)))
        :else
        (into [{:head (if (symbol? head) head :non-symbol-call-head)
                :region region :body-spelling false}]
              (mapcat #(syntax-calls % region)
                      (if (symbol? head) (rest form) form)))))
    (map? form) (mapcat #(syntax-calls % region) (concat (keys form) (vals form)))
    (coll? form) (mapcat #(syntax-calls % region) form)
    :else []))

(defn- form-kind [head]
  (case (core-spelling head)
    (defn defn-) :function-definition
    defmacro :macro-definition
    def :def-initializer
    defonce :conditional-def-initializer
    ns :namespace-declaration
    declare :var-declaration
    :other-form))

(defn analyze-source
  "Reads source data only. Current/body regions are syntactic labels, not
  claims about runtime eagerness: callbacks, macros and lexical bindings are
  deliberately unresolved, including immediately invoked function literals."
  [path text]
  (dependencies/with-source-reader
   text path
   (fn [reader]
     (binding [*reader-resolver* lexical-resolver]
       (let [eof (Object.)]
         (loop [records [] ordinal 0]
           (let [before (.getLineNumber reader)
                 form (read {:eof eof :read-cond :preserve} reader)]
             (if (identical? eof form)
               records
               (let [head (when (seq? form) (first form))
                     kind (form-kind head)
                     selected (if (contains? #{:def-initializer :conditional-def-initializer} kind)
                                (when (> (count form) 2) (last form)) form)
                     calls (if (contains? #{:namespace-declaration :var-declaration} kind)
                             [] (syntax-calls selected :current))
                     heads (fn [region]
                             (vec (sort-by str (set (map :head (filter #(= region (:region %)) calls))))))]
                 (recur
                  (conj records
                        {:path path :ordinal ordinal :line (or (:line (meta form)) before)
                         :end-line (.getLineNumber reader)
                         :kind kind :head (when (symbol? head) head)
                         :name (when (and (seq? form) (symbol? (second form))) (second form))
                         :current-call-candidates (heads :current)
                         :body-call-candidates (heads :body)
                         :body-spelling-assumptions
                         (vec (sort-by str (set (map :head (filter :body-spelling calls)))))
                         :call-and-effect-resolution :unresolved})
                  (inc ordinal)))))))))))

(defn- source-text [root {:keys [path sha256]}]
  (let [bytes (Files/readAllBytes (.toPath (io/file root path)))]
    (when-not (= sha256 (digest/sha256-bytes-hex bytes))
      (throw (ex-info "Source changed after dependency observation"
                      {:diagnostic "RV-INITIALIZATION-AUDIT" :reason :source-drift :path path})))
    (str (.decode (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))
                  (ByteBuffer/wrap bytes)))))

(defn observation-report
  "Separate audit representation, never a cache-closure declaration. Caller
  fields cannot upgrade its authority or complete the unresolved effects."
  [observed files]
  {:schema schema :authority :none :source-evaluated? false
     :closure-complete? false :cache-narrowing-authorized? false
     :dependency-observation (:observation-sha256 observed)
     :inputs (:inputs observed) :ordered-components (:ordered-components observed)
     :file-order :lexicographic-not-runtime-load-order
     :reader-resolution :lexical-labels-not-runtime-vars
     :files (vec files)
     :unresolved [:namespace-load-order :macro-resolution-expansion-and-generated-code
                  :lexical-and-dynamic-call-targets :transitive-initializer-effects
                  :reader-conditionals-and-runtime-classpath :atomic-read-consistency
                  :computed-and-unparsed-resources :cache-representation-and-producer-evidence]})

(defn audit [root]
  (let [observed (dependencies/audit root)
        canonical (.getCanonicalFile (io/file root))
        files (vec (for [input (:inputs observed)
                         :when (str/ends-with? (:path input) ".clj")]
                     {:path (:path input) :sha256 (:sha256 input)
                      :forms (analyze-source (:path input) (source-text canonical input))}))]
    (observation-report observed files)))

(when (seq *command-line-args*)
  (when-not (= ["--audit"] (vec *command-line-args*))
    (throw (ex-info "usage: clojure -M tools/compatibility_initialization_audit.clj --audit" {})))
  (prn (audit ".")))
