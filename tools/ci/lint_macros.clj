(ns ci.lint-macros
  "Admit the exact reviewed source recipe before enabling its static node hook."
  (:require [clojure.string :as str]
            [ci.lint-context :as context]))

(def source "bootstrap/clojure/src/gravity/c2_artifact_identity.clj")
(def config-directory "tools/ci/kondo")
(def recipe-sha256 "c5086b2467e5b6940b9c8ad3d81eb94bcc5b460373e3f9bcc2fabb800dd0db20")

(defn core-name [value]
  (when (and (symbol? value) (contains? #{nil "clojure.core"} (namespace value)))
    (symbol (name value))))

(defn guarded-declarations [form]
  (if (and (seq? form) (= 'quote (core-name (first form)))) []
      (concat
       (when (and (seq? form)
                  (or (#{'def 'defonce 'defn 'defn- 'defmacro 'defmulti 'definterposable} (core-name (first form)))
                      (= 'gravity.c2-artifact-identity/definterposable (first form)))
                  (symbol? (second form))
                  (contains? #{nil "gravity.c2-artifact-identity"} (namespace (second form))))
         [(symbol (name (second form)))])
       (when (coll? form) (mapcat guarded-declarations form)))))

(defn observation! [bytes]
  (let [text (context/source-text! source bytes)
        lines (str/split text #"\r?\n" -1)
        observations
        (binding [*read-eval* false *data-readers* {}
                  *default-data-reader-fn* (fn [tag _] (throw (ex-info "Unsupported macro source tag" {:tag tag})))]
          (with-open [reader (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
            (let [eof (Object.)]
              (loop [result []]
                (let [form (read {:eof eof} reader)]
                  (if (identical? eof form) result
                      (recur (conj result {:form form :start (:line (meta form)) :end (.getLineNumber reader)}))))))))
        declarations (filter #(and (seq? (:form %)) (= 'defmacro (first (:form %)))
                                   (= 'definterposable (second (:form %)))) observations)
        support (fn [name heads]
                  (filter #(and (seq? (:form %)) (heads (core-name (first (:form %))))
                                (= name (second (:form %)))) observations))
        bindings (frequencies (mapcat #(guarded-declarations (:form %)) observations))
        {:keys [start end]} (first declarations)]
    (when-not (and (= '(ns gravity.c2-artifact-identity) (take 2 (:form (first observations))))
                   (= 1 (count declarations))
                   (not-any? #(and (seq? (:form %))
                                   (#{'ns 'clojure.core/ns 'in-ns 'clojure.core/in-ns} (first (:form %))))
                             (rest observations))
                   (every? pos-int? [start end]) (<= start end (count lines)))
      (throw (ex-info "Unsupported C2 macro namespace or declaration" {})))
    (doseq [name '[definterposable current-operation *bypass-next-operation-keys* *active-operation-keys*]]
      (when-not (= 1 (get bindings name 0))
        (throw (ex-info "Missing or repeated guarded macro/support declaration" {:name name}))))
    (doseq [name '[*bypass-next-operation-keys* *active-operation-keys*]
            :let [definitions (support name #{'def})]]
      (when-not (and (= 1 (count definitions))
                     (:dynamic (meta (second (:form (first definitions)))))
                     (< (:start (first definitions)) start))
        (throw (ex-info "Missing or unsupported dynamic macro support" {:name name}))))
    (let [definitions (support 'current-operation #{'defn 'defn-})]
      (when-not (and (= 1 (count definitions)) (< (:start (first definitions)) start)
                     (vector? (nth (:form (first definitions)) 2 nil))
                     (= 1 (count (nth (:form (first definitions)) 2 nil))))
        (throw (ex-info "Missing or unsupported macro operation lookup" {}))))
    {:path source :start start :end end :source-sha256 (context/sha256 bytes)
     :macro-sha256 (context/sha256 (.getBytes (str/join "\n" (subvec lines (dec start) end)) "UTF-8"))}))

(defn contract! [bytes]
  (let [observation (observation! bytes)]
    (when-not (= recipe-sha256 (:macro-sha256 observation))
      (throw (ex-info "C2 definterposable changed; lint model requires independent review" observation)))
    observation))
