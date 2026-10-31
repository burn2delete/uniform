(ns ci.lint-context
  "Non-evaluating lint views of the reviewed Bootstrap and facade resource loaders."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.security MessageDigest]
           [java.util.zip ZipFile]))

(def source-root "bootstrap/clojure/src/")
(def bootstrap (str source-root "gravity/bootstrap.clj"))
(def manifest (str source-root "gravity/bootstrap_impl/load_order.edn"))
(def semantic-late-manifest (str source-root "gravity/bootstrap_impl/semantic_late_order.edn"))
(def loader (str source-root "gravity/bootstrap_impl/loader.clj"))
(def loader-sha "ac79eb7b09a0ad5cef2b67e92e8669e555ec26877caa50e2599a2b6ec200aee7")
(def facade (str source-root "gravity/authenticated_artifact_pipeline.clj"))
(def facade-prefix (str source-root "gravity/authenticated_artifact_pipeline/"))
(def facade-resources
  ["01-pipeline-schema-version" "02-clear-cache" "03-revalidate-payload"
   "04-envelope-valid" "05-run-pipeline"])

(defn context-path? [path]
  (or (= bootstrap path) (str/starts-with? path (str source-root "gravity/bootstrap_impl/"))
      (= facade path) (str/starts-with? path facade-prefix)))

(defn whole-context-path? [path] (contains? #{bootstrap manifest semantic-late-manifest loader facade} path))

(defn source-text! [path bytes]
  (let [text (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)]
    (when (re-find #"\r(?!\n)" text)
      (throw (ex-info "Unsupported lone CR in lint context source" {:path path})))
    text))

(defn- source-reader-scope [scope form]
  ;; Declarations are DATA. Never require a library or mutate a Namespace to
  ;; teach the reader aliases; baseline and current inputs have separate scopes.
  (let [operation (when (seq? form) (first form))
        ns? (contains? #{'ns 'clojure.core/ns} operation)
        scope (if ns? {:namespace (second form) :aliases {}} scope)
        specs (cond ns? (mapcat rest (filter #(and (seq? %) (= :require (first %))) (drop 2 form)))
                    (contains? #{'require 'clojure.core/require} operation) (rest form))]
    (reduce
      (fn [scope spec]
        (let [spec (if (and (seq? spec) (= 'quote (first spec))) (second spec) spec)]
          (if (and (vector? spec) (symbol? (first spec)) (even? (count (rest spec))))
            (let [options (apply hash-map (rest spec)) alias (or (:as options) (:as-alias options))]
              (if (symbol? alias) (assoc-in scope [:aliases alias] (first spec)) scope))
            scope)))
      scope specs)))

(defn source-forms! [path bytes]
  (let [scope (atom {:namespace (ns-name *ns*) :aliases {}})
        refuse-tag (fn [tag _] (throw (ex-info "Unsupported source tag" {:tag tag :path path})))
        resolver (reify clojure.lang.LispReader$Resolver
                   (currentNS [_] (:namespace @scope))
                   (resolveAlias [_ alias] (get-in @scope [:aliases alias]))
                   (resolveClass [_ _] nil)
                   (resolveVar [_ _] nil))]
  (binding [*read-eval* false *data-readers* {'inst #(refuse-tag 'inst %) 'uuid #(refuse-tag 'uuid %)}
            *default-data-reader-fn* refuse-tag *reader-resolver* resolver]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (java.io.StringReader. (source-text! path bytes)))]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form) forms
              (do (swap! scope source-reader-scope form)
                  (recur (conj forms form)))))))))))

(defn manifest! [bytes]
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)))]
    (let [eof (Object.) result (edn/read {:eof eof :readers {}} reader)]
      (when-not (and (vector? result) (seq result) (every? map? result)
                     (identical? eof (edn/read {:eof eof :readers {}} reader)))
        (throw (ex-info "Malformed or trailing Bootstrap load manifest" {})))
      result)))

(defn bootstrap-shape! [bytes]
  (let [[header & tail :as forms] (source-forms! bootstrap bytes)]
    (when-not (and (= 3 (count forms)) (= '(ns gravity.bootstrap) (take 2 header))
                   (= '[(clojure.core/require 'gravity.bootstrap-impl.loader)
                        (gravity.bootstrap-impl.loader/load-components! "gravity/bootstrap_impl/load_order.edn")]
                      tail))
      (throw (ex-info "Bootstrap entrypoint changed; unsupported lint assembly recipe" {})))))

(defn sha256 [bytes]
  (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))

(defn resource-path! [resource]
  (when-not (and (string? resource) (str/starts-with? resource "gravity/bootstrap_impl/")
                 (str/ends-with? resource ".clj")
                 (every? #(not (#{"" "." ".."} %)) (str/split resource #"/"))
                 (not (str/includes? resource "\\")))
    (throw (ex-info "Unsupported Bootstrap resource path" {:resource resource})))
  (str source-root resource))

(defn local-bytes! [path]
  (loop [file (io/file path)]
    (when file
      (when (java.nio.file.Files/isSymbolicLink (.toPath file))
        (throw (ex-info "Lint context cannot follow symbolic links" {:path path})))
      (recur (.getParentFile file))))
  (java.nio.file.Files/readAllBytes (.toPath (io/file path))))

(defn plan! [read-bytes]
  (let [loader-bytes (read-bytes loader)
        _ (when-not (= loader-sha (sha256 loader-bytes))
            (throw (ex-info "Bootstrap loader changed; lint context requires independent review" {})))
        _ (bootstrap-shape! (read-bytes bootstrap))
        entries (manifest! (read-bytes manifest))
        paths (into [bootstrap] (map #(resource-path! (:resource %)) entries))]
    (when-not (= (count paths) (count (distinct paths)))
      (throw (ex-info "Duplicate Bootstrap resource in lint context" {})))
    {:paths paths :inputs (into {} (for [path (conj paths loader manifest)
                                         :let [bytes (read-bytes path)
                                               _ (when (str/ends-with? path ".clj") (source-forms! path bytes))]]
                                     [path bytes]))}))

(defn facade-plan! [read-bytes]
  (let [entry (read-bytes facade)
        [header load-form :as forms] (source-forms! facade entry)
        expected (cons 'load (map #(str "/gravity/authenticated_artifact_pipeline/" %)
                                  facade-resources))
        paths (into [facade] (map #(str facade-prefix % ".clj") facade-resources))]
    (when-not (and (= 2 (count forms))
                   (= '(ns gravity.authenticated-artifact-pipeline) (take 2 header))
                   (= expected load-form))
      (throw (ex-info "Facade entrypoint changed; unsupported lint assembly recipe" {})))
    {:paths paths
     :namespace-by-path (zipmap paths (repeat 'gravity.authenticated-artifact-pipeline))
     :entrypoints #{facade}
     :inputs (into {facade entry}
                   (for [path (rest paths) :let [bytes (read-bytes path)
                                                 forms (source-forms! path bytes)]]
                     (do
                       (when (some #(and (seq? %) (#{'ns 'in-ns 'clojure.core/ns 'clojure.core/in-ns
                                                     'load 'clojure.core/load} (first %)))
                                   (tree-seq coll? seq forms))
                         (throw (ex-info "Unsupported namespace/load change in facade fragment" {:path path})))
                       [path bytes])))}))

(defn selected-plan! [read-bytes selected]
  (let [bootstrap? (some #(or (= bootstrap %) (str/starts-with? % (str source-root "gravity/bootstrap_impl/"))) selected)
        facade? (some #(or (= facade %) (str/starts-with? % facade-prefix)) selected)
        plans (cond-> [] bootstrap? (conj (plan! read-bytes)) facade? (conj (facade-plan! read-bytes)))
        combined {:paths (vec (mapcat :paths plans))
                  :inputs (apply merge (map :inputs plans))
                  :namespace-by-path (apply merge (map :namespace-by-path plans))
                  :entrypoints (into #{bootstrap} (mapcat :entrypoints plans))}]
    ;; The companion manifest is EDN data, linted separately from source forms.
    (doseq [path selected :when (and (context-path? path) (not (#{loader manifest semantic-late-manifest} path)))]
      (when-not (some #{path} (:paths combined))
        (throw (ex-info "Changed fragment is absent from lint context" {:path path}))))
    combined))

(defn assemble! [directory {:keys [paths inputs namespace-by-path entrypoints]}]
  (let [target (io/file directory "gravity/bootstrap.clj")
        spans (atom [])
        row (atom 1)]
    (io/make-parents target)
    (with-open [writer (io/writer target)]
      (doseq [path paths]
        (when-not (contains? (or entrypoints #{bootstrap}) path)
          ;; load-component! binds *ns* afresh for EVERY resource.
          (.write writer (str "(in-ns '" (get namespace-by-path path 'gravity.bootstrap) ")\n"))
          (swap! row inc))
        (let [text (source-text! path (get inputs path))
              lines (str/split text #"\r?\n" -1)
              start @row]
          (.write writer text)
          (.write writer "\n")
          (swap! row + (count lines))
          (swap! spans conj {:path path :start start :end @row :lines lines}))))
    (let [result {:file (.getPath target)
                  :sha256 (sha256 (java.nio.file.Files/readAllBytes (.toPath target))) :spans @spans
                  :inputs (into (sorted-map) (for [[path bytes] inputs]
                                               [path {:sha256 (sha256 bytes) :bytes (alength ^bytes bytes)}]))}]
      (spit (io/file directory "source-map.edn") (str (pr-str result) "\n"))
      result)))

(defn remap! [{:keys [spans]} finding]
  (let [{:keys [row end-row col end-col]} finding
        span (some #(when (and (pos-int? row) (<= (:start %) row) (< row (:end %))) %) spans)]
    (when-not (and span (every? pos-int? [row end-row col end-col])
                   (<= row end-row) (< end-row (:end span))
                   (or (< row end-row) (<= col end-col))
                   (<= col (inc (count (nth (:lines span) (- row (:start span))))))
                   (<= end-col (inc (count (nth (:lines span) (- end-row (:start span)))))))
      (throw (ex-info "Lint finding has no exact single-source context span" {:finding finding})))
    (assoc finding :filename (:path span)
           :row (inc (- row (:start span))) :end-row (inc (- end-row (:start span))))))

(defn archived-plan!
  ([archive inventory] (archived-plan! archive inventory [bootstrap]))
  ([archive inventory selected]
   (let [modes (into {} (for [entry (remove str/blank? (str/split (slurp inventory) #"\u0000"))
                              :let [[header path] (str/split entry #"\t" 2)]]
                          [path (first (str/split header #" "))]))]
     (with-open [zip (ZipFile. (io/file archive))]
       (selected-plan! (fn [path]
                         (when-not (#{"100644" "100755"} (get modes path))
                           (throw (ex-info "Missing or nonregular base context source" {:path path})))
                         (let [entry (.getEntry zip path)]
                           (when-not entry (throw (ex-info "Missing archived context source" {:path path})))
                           (with-open [input (.getInputStream zip entry)] (.readAllBytes input)))) selected)))))

(defn- core-operation [value]
  (when (and (symbol? value) (contains? #{nil "clojure.core"} (namespace value)))
    (symbol (name value))))

(defn- require-spec [value]
  (let [spec (if (and (seq? value) (= 'quote (core-operation (first value)))) (second value) value)]
    (when (and (vector? spec) (symbol? (first spec)) (even? (count (rest spec))))
      (let [options (apply hash-map (rest spec))]
        (when-not (contains? options :as-alias)
          {:namespace (first spec) :alias (:as options)})))))

(declare ^:private call-references)

(defn- binding-references [bindings locals aliases]
  ;; The compiler's data-only expansion puts used :or defaults in executable
  ;; initializers and leaves unused defaults/binding syntax out of the walk.
  (loop [pairs (partition 2 (clojure.core/destructure bindings)) locals locals calls []]
    (if-let [[name expression] (first pairs)]
      (recur (next pairs) (conj locals name)
             (into calls (call-references expression locals aliases)))
      {:locals locals :calls calls})))

(defn- function-references [tail locals aliases]
  (let [[locals tail] (if (symbol? (first tail)) [(conj locals (first tail)) (rest tail)] [locals tail])
        tail (drop-while #(or (string? %) (map? %)) tail)
        arities (if (vector? (first tail)) [tail] tail)]
    (mapcat (fn [[parameters & body]]
              (when (vector? parameters)
                ;; Simple parameters are all bound before the function's
                ;; sequential destructuring let, including later parameters.
                (let [locals (into locals (remove #{'&} (filter symbol? parameters)))
                      patterns (remove symbol? parameters)
                      {:keys [locals calls]} (binding-references (vec (mapcat #(vector % nil) patterns)) locals aliases)]
                  (concat calls (mapcat #(call-references % locals aliases) body))))) arities)))

(defn- call-references
  ([form] (call-references form #{} {}))
  ([form locals aliases]
   (let [head (when (seq? form) (first form))
         qualified-head (if (and (symbol? head) (namespace head)
                                 (= 'clojure.core (get aliases (symbol (namespace head)))))
                          (symbol "clojure.core" (name head)) head)
         core (core-operation qualified-head)
         operation (when-not (and (symbol? head) (nil? (namespace head))
                                  (contains? locals head) (not (#{'quote 'var} core))) core)
         walk #(call-references % locals aliases)]
     (cond
       (#{'quote 'var 'comment} operation) []
       (= 'case operation)
       (let [clauses (drop 2 form)]
         (concat (walk (second form))
                 (mapcat walk (map second (partition 2 clauses)))
                 (when (odd? (count clauses)) (walk (last clauses)))))
       (#{'let 'let* 'loop 'loop* 'with-open 'with-local-vars} operation)
       (let [{:keys [locals calls]} (binding-references (second form) locals aliases)]
         (concat calls (mapcat #(call-references % locals aliases) (drop 2 form))))
       (#{'fn 'fn*} operation) (function-references (rest form) locals aliases)
       (#{'defn 'defn- 'defmacro} operation) (function-references (drop 2 form) locals aliases)
       (= 'letfn operation)
       (let [functions (second form) locals (into locals (map first functions))]
         (concat (mapcat #(function-references (rest %) locals aliases) functions)
                 (mapcat #(call-references % locals aliases) (drop 2 form))))
       (#{'if-let 'if-some 'when-let 'when-some} operation)
       (let [body (drop 2 form)
             {bound :locals calls :calls} (binding-references (second form) locals aliases)]
         (concat calls
                 (if (#{'if-let 'if-some} operation)
                   (concat (call-references (first body) bound aliases) (walk (second body)))
                   (mapcat #(call-references % bound aliases) body))))
       (= 'catch operation)
       (mapcat #(call-references % (conj locals (nth form 2)) aliases) (drop 3 form))
       ;; Unknown core macros may contain data/bindings. Retain warnings rather
       ;; than invent executable positions for an unsupported expansion.
       (and operation (:macro (meta (ns-resolve 'clojure.core operation)))
            (not (#{'when 'when-not 'if-not 'and 'or 'cond 'condp 'assert 'lazy-seq
                    'binding 'dosync 'locking 'with-redefs 'with-local-vars} operation))) []
       :else
       (concat
        (when (and (seq? form) (seq? head) (= 'var (core-operation (first head))) (= 2 (count head)))
          (let [reference (second head)]
            (when (and (symbol? reference) (namespace reference))
              [{:reference reference :row (:line (meta form)) :col (:column (meta form))}])))
        (when (coll? form) (mapcat walk form)))))))

(defn private-var-usages
  "Non-evaluating, source-backed direct Var call edges in the actual lint inputs.
  Each group has the same namespace lifetime as one linter/context input."
  [groups]
  (let [definitions (atom []) calls (atom [])]
    (doseq [{:keys [paths inputs namespace-by-path]} groups]
      (let [scopes (atom {})]
        (doseq [path paths :let [bytes (get inputs path) digest (sha256 bytes)]
                :when (str/ends-with? path ".clj")]
          (loop [forms (source-forms! path bytes)
                 current-ns (get namespace-by-path path 'gravity.bootstrap)]
            (when-let [form (first forms)]
              (let [operation (when (seq? form) (core-operation (first form)))
                    current-ns (if (= 'ns operation) (second form) current-ns)
                    specs (cond (= 'ns operation) (mapcat rest (filter #(and (seq? %) (= :require (first %))) (drop 2 form)))
                                (= 'require operation) (rest form))]
                (when (= 'ns operation) (swap! scopes assoc current-ns {:aliases {} :required {} :defined #{}}))
                (doseq [{target :namespace alias :alias} (keep require-spec specs)]
                  (swap! scopes assoc-in [current-ns :required target]
                         {:namespace target :alias alias :path path :row (:line (meta form)) :sha256 digest})
                  (when alias (swap! scopes assoc-in [current-ns :aliases alias] target)))
                (when (and (#{'def 'defonce 'defn 'defn- 'defmacro} operation) (symbol? (second form)))
                  (swap! scopes update-in [current-ns :defined] (fnil conj #{}) (second form)))
                (let [{:keys [aliases required defined]} (get @scopes current-ns)]
                  (when (and (#{'defn 'defn-} operation) (symbol? (second form))
                             (or (= 'defn- operation) (:private (meta (second form)))))
                    (swap! definitions conj {:namespace current-ns :name (second form)
                                             :path path :row (:line (meta form)) :sha256 digest}))
                  (doseq [{:keys [reference row col]} (call-references form (or defined #{}) aliases)
                          :let [prefix (symbol (namespace reference)) target (get aliases prefix prefix)]
                          :when (and (contains? required target) (not= current-ns target)
                                     (every? pos-int? [row col]))]
                    (swap! calls conj {:namespace target :name (symbol (name reference))
                                       :caller-namespace current-ns :path path :row row :col col
                                       :reference reference :alias (when (contains? aliases prefix) prefix)
                                       :require (get required target)
                                       :sha256 digest})))
                (recur (next forms) current-ns)))))))
    (let [by-name (group-by (juxt :namespace :name) @definitions)]
      (vec (for [[key definitions] by-name :when (= 1 (count definitions))
                 :let [callers (filterv #(= key ((juxt :namespace :name) %)) @calls)]
                 :when (seq callers)]
             {:definition (first definitions) :callers callers})))))

(defn resolve-private-usages [findings usages]
  (reduce (fn [result finding]
            (if-let [usage (when (and (= :unused-private-var (:type finding)) (= :warning (:level finding)))
                             (some (fn [{:keys [definition] :as usage}]
                                     (when (and (= (:path definition) (:filename finding))
                                                (= (:row definition) (:row finding))
                                                (= (str "Unused private var " (:namespace definition) "/" (:name definition))
                                                   (:message finding))) usage)) usages))]
              (update result :resolved conj (assoc usage :finding finding))
              (update result :findings conj finding)))
          {:findings [] :resolved []} findings))
