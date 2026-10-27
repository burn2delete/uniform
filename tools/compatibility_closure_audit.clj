(ns compatibility-closure-audit
  "Read-only dependency observations for the compiler-pass compatibility seam.
  Does not load compiler namespaces, authorize a cache closure or run a test."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.nio.file Files LinkOption Path]))

(def selection 'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test)
(def source-roots ["bootstrap/clojure/src" "bootstrap/clojure/test"])
(def manifest-resource "gravity/bootstrap_impl/load_order.edn")
(def loader-path "bootstrap/clojure/src/gravity/bootstrap_impl/loader.clj")
(def accepted-fixture "bootstrap/clojure/fixtures/accepted/compiler-passes.gravity")
(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- reject! [reason data]
  (throw (ex-info "Compatibility dependency observation rejected"
                  (assoc data :diagnostic "RV-COMPATIBILITY-AUDIT" :reason reason))))

(defn- relative-path! [value]
  (when-not (and (string? value)
                 (not (str/blank? value))
                 (not (str/includes? value "\\"))
                 (every? #(not (contains? #{"" "." ".."} %))
                         (str/split value #"/" -1)))
    (reject! :relative-path {:path value}))
  value)

(defn- local-path! [^Path root relative]
  (relative-path! relative)
  (let [path (.resolve root ^String relative)]
    ;; This is a current read observation, not an atomic filesystem snapshot.
    (loop [cursor path]
      (when-not (= cursor root)
        (when (Files/isSymbolicLink cursor)
          (reject! :symbolic-link {:path relative}))
        (recur (.getParent cursor))))
    path))

(defn- read-input! [root relative]
  (let [path (local-path! root relative)]
    (when-not (Files/isRegularFile path no-links)
      (reject! :missing-input {:path relative}))
    {:path relative
     :sha256 (digest/sha256-bytes-hex (Files/readAllBytes path))
     :mode (if (Files/isExecutable path) :executable :regular)}))

(defn with-source-reader
  "Runs trusted audit code with a non-evaluating source reader. The callback
  must inspect data only; this helper supplies no source execution authority."
  [text path operation]
  (try
    (binding [*read-eval* false
              *data-readers*
              {'inst (fn [_] (reject! :tagged-source {:path path :tag 'inst}))
               'uuid (fn [_] (reject! :tagged-source {:path path :tag 'uuid}))}
              *default-data-reader-fn*
              (fn [tag _] (reject! :tagged-source {:path path :tag tag}))]
      (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                         (java.io.StringReader. text))]
        (operation reader)))
    (catch Exception exception
      (reject! :namespace-read {:path path :message (.getMessage exception)}))))

(defn- read-first-form [text path]
  (with-source-reader text path #(read {:eof nil} %)))

(defn- required-namespaces [form path]
  (when-not (and (seq? form) (= 'ns (first form)) (symbol? (second form)))
    (reject! :namespace-form {:path path}))
  (letfn [(libspec [spec]
            (cond
              (symbol? spec) [spec]
              (and (vector? spec) (symbol? (first spec))) [(first spec)]
              ;; Prefix lists are deliberately unresolved rather than guessed.
              :else (reject! :unsupported-libspec {:path path :libspec spec})))]
    {:namespace (second form)
     :requires (vec (mapcat libspec
                           (mapcat rest
                                   (filter #(and (seq? %) (= :require (first %)))
                                           (drop 2 form)))))
     :other-loading-clauses
     (vec (filter #(and (seq? %) (contains? #{:use :load :require-macros} (first %)))
                  (drop 2 form)))}))

(defn- namespace-candidates [root namespace]
  (let [resource (-> (str namespace) (str/replace "-" "_") (str/replace "." "/"))]
    (vec (for [prefix source-roots suffix [".clj" ".cljc" ".class"]
               :let [relative (str prefix "/" resource suffix)
                     path (local-path! root relative)]
               :when (Files/exists path no-links)]
           relative))))

(defn- namespace-inputs [root start]
  (loop [pending [start] visited #{} paths #{} external #{} loading []]
    (if-let [namespace (first pending)]
      (cond
        (contains? visited namespace)
        (recur (subvec pending 1) visited paths external loading)

        (str/starts-with? (str namespace) "clojure.")
        (recur (subvec pending 1) (conj visited namespace) paths
               (conj external namespace) loading)

        :else
        (let [candidates (namespace-candidates root namespace)]
          (when-not (and (= 1 (count candidates)) (str/ends-with? (first candidates) ".clj"))
            (reject! :namespace-resolution {:namespace namespace :candidates candidates}))
          (let [path (first candidates)
                parsed (required-namespaces
                        (read-first-form (slurp (.toFile (local-path! root path))) path) path)]
            (when-not (= namespace (:namespace parsed))
              (reject! :namespace-identity {:path path :expected namespace}))
            (recur (into (subvec pending 1) (:requires parsed))
                   (conj visited namespace) (conj paths path) external
                   (into loading (map #(hash-map :path path :clause %)
                                      (:other-loading-clauses parsed)))))))
      {:paths paths :external-namespaces (vec (sort external))
       :other-loading-clauses loading})))

(defn- resource-candidates [text]
  ;; Syntactic literal candidates. Comments/metadata may overapproximate them;
  ;; dynamic resources and arbitrary initialization effects remain unresolved.
  (mapv second
        (re-seq #"\"(gravity/bootstrap_impl/data/[^\"\r\n]+\.edn)\"" text)))

(defn- literal-load-inputs [root initial-paths]
  (loop [pending (vec (sort initial-paths)) visited #{} loads #{}]
    (if-let [path (first pending)]
      (if (or (contains? visited path) (not (str/ends-with? path ".clj")))
        (recur (subvec pending 1) (conj visited path) loads)
        (let [resources
              (mapcat (fn [[_ arguments]]
                        (map second (re-seq #"\"(/[^\"\r\n]+)\"" arguments)))
                      (re-seq #"\((?:clojure\.core/)?load\s+((?:\"/[^\"\r\n]+\"\s*)+)\)"
                              (slurp (.toFile (local-path! root path)))))
              loaded
              (mapv
               (fn [resource]
                 (let [relative (relative-path! (subs resource 1))
                       candidates
                       (vec (for [prefix source-roots suffix [".clj" ".cljc" ".class"]
                                  :let [p (str prefix "/" relative suffix)]
                                  :when (Files/exists (local-path! root p) no-links)] p))]
                   (when-not (and (= 1 (count candidates))
                                  (str/ends-with? (first candidates) ".clj"))
                     (reject! :literal-load-resolution {:resource resource :candidates candidates}))
                   (first candidates))) resources)]
          (recur (into (subvec pending 1) loaded) (conj visited path) (into loads loaded))))
      loads)))

(defn- read-sites [path text]
  (vec (keep-indexed
        (fn [index line]
          (when (re-find #"\((?:[A-Za-z0-9_.-]+/)?(?:slurp|resource|load|load-file|require|requiring-resolve|append-data!)(?=\s|\)|$)" line)
            {:path path :line (inc index) :text (str/trim line)
             :classification :syntactic-candidate-not-execution}))
        (str/split-lines text))))

(defn audit
  "Collects source observations only. Options select a small synthetic seam in
  tests; no report, input count or observation fingerprint grants cache reuse."
  ([root] (audit root {}))
  ([root {:keys [namespace manifest loader fixtures]
          :or {namespace selection manifest manifest-resource loader loader-path
               fixtures [accepted-fixture]}}]
   (let [supplied (.normalize (.toAbsolutePath (.toPath (io/file root))))
         _ (when (Files/isSymbolicLink supplied)
             (reject! :symbolic-root {}))
         _ (when-not (Files/isDirectory supplied no-links)
             (reject! :audit-root {}))
         ;; Resolve ancestor aliases such as macOS /var; reject a supplied root
         ;; that is itself a link. Child links still reject in local-path!.
         root (.toRealPath supplied (make-array LinkOption 0))
         manifest-path (str (first source-roots) "/" (relative-path! manifest))
         _ (read-input! root manifest-path)
         entries (edn/read-string {:readers {'inst (fn [_] (reject! :tagged-manifest {:tag 'inst}))
                                            'uuid (fn [_] (reject! :tagged-manifest {:tag 'uuid}))}
                                  :default (fn [tag _] (reject! :tagged-manifest {:tag tag}))}
                                  (slurp (.toFile (local-path! root manifest-path))))
         _ (when-not (and (vector? entries)
                          (every? #(and (map? %) (string? (:resource %))) entries))
             (reject! :manifest-shape {}))
         component-paths (mapv #(str (first source-roots) "/" (relative-path! (:resource %))) entries)
         _ (doseq [path component-paths] (read-input! root path))
         data-paths (set (for [path component-paths
                               resource (resource-candidates (slurp (.toFile (local-path! root path))))]
                           (str (first source-roots) "/" (relative-path! resource))))
         ns-inputs (namespace-inputs root namespace)
         ;; The loader is an explicit require outside bootstrap's ns form.
         loader-inputs (namespace-inputs root 'gravity.bootstrap-impl.loader)
         initial-paths (into (set (concat [manifest-path loader] component-paths data-paths fixtures))
                             (concat (:paths ns-inputs) (:paths loader-inputs)))
         loaded-paths (literal-load-inputs root initial-paths)
         paths (into initial-paths loaded-paths)
         inputs (mapv #(read-input! root %) (sort paths))
         source-paths (filter #(str/ends-with? % ".clj") paths)
         sites (vec (mapcat #(read-sites % (slurp (.toFile (local-path! root %))))
                            (sort source-paths)))
         observations {:inputs inputs :ordered-components (mapv :resource entries)}]
     {:schema :gravity/compatibility-dependency-observations-v1
      :selection namespace :authority :none :cache-narrowing-authorized? false
      :closure-complete? false
      :observation-sha256 (digest/sha256-hex (pr-str observations))
      :inputs inputs :ordered-components (:ordered-components observations)
      :literal-data-candidates (vec (sort data-paths))
      :literal-load-candidates (vec (sort loaded-paths))
      :external-namespaces (vec (sort (set (concat (:external-namespaces ns-inputs)
                                                  (:external-namespaces loader-inputs)))))
      :other-loading-clauses (vec (concat (:other-loading-clauses ns-inputs)
                                        (:other-loading-clauses loader-inputs)))
      :read-candidates sites
      :unresolved [:arbitrary-initialization-effects :dynamic-body-call-targets
                   :computed-resource-paths :computed-relative-or-unparsed-load-forms
                   :atomic-read-consistency
                   :classpath-runtime-identity :manifest-backed-closure-representation]
      :representation {:current-kind :bounded-flat-explicit-inputs
                       :policy-source "bootstrap/clojure/test/gravity/self_hosting/sh01_component_test_dependencies.clj"
                       :observed-input-count (count inputs)
                       :existing-flat-declaration-eligible? false}})))

(when (seq *command-line-args*)
  (when-not (= ["--audit"] (vec *command-line-args*))
    (throw (ex-info "usage: clojure -M tools/compatibility_closure_audit.clj --audit" {})))
  (prn (audit ".")))
