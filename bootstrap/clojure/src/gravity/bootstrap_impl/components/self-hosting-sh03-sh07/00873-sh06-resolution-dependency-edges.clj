

(defn sh06-resolution-dependency-edges
  [source-path module imports overrides]
  (let [base
        (mapv
         (fn [ordinal dependency]
           {:from (:module module)
            :to (:namespace dependency)
            :kind (:kind dependency)
            :boundary (:boundary dependency)
            :semantic-span {:dependency-edge ordinal}
            :source-span (:source-span dependency)})
         (range) imports)
        explicit (get-in overrides [:module-graph :edges])]
    (vec
     (distinct
      (concat
       base
       (map-indexed
        (fn [ordinal edge]
          (let [[from to] (when (vector? edge) edge)]
            {:from (or (:from edge) from)
             :to (or (:to edge) to)
             :kind (or (:kind edge) :namespace)
             :boundary (:boundary edge)
             :semantic-span {:explicit-edge ordinal}
             :source-span (source-span source-path 0)}))
        explicit))))))

(defn sh06-resolution-request
  [source-path sh05-artifact]
  (let [verification (sh05-macro-artifact-verification sh05-artifact)
        _ (when-not (= :passed (:status verification))
            (c5-resolution-fail!
             "C5-UNRESOLVED" source-path
             {:stage :name-resolution}
             {:missing-fields [:verified-sh05-macro-artifact]}))
        forms (mapv :form (:expanded-syntax-stream sh05-artifact))
        module (parse-module source-path forms)
        overrides (sh06-resolution-request-overrides module)
        imports (sh06-resolution-import-records
                 source-path module overrides)
        explicit-definitions
        (sh06-resolution-explicit-candidate-records
         module sh05-artifact overrides)
        analysis-inputs
        (sh06-resolution-analysis-inputs
         module sh05-artifact overrides)
        source-revision-id
        (sh06-resolution-source-revision-id sh05-artifact)]
    {:artifact :gravity/sh06-authenticated-c4-resolution-request
     :schema-version 1
     :module
     {:namespace (:module module)
      :package (sh06-resolution-package (:module module))
      :profile (:profile module)
      :target (:target module)
      :safety (:safety module)
      :effects (vec (sort (:effects module)))
      :capabilities (vec (sort (:capabilities module)))
      :exports (vec (:exports module))
      :source-revision-id source-revision-id
      :c4-artifact-id (:artifact-id sh05-artifact)}
     :definitions
     (vec (concat
           (sh06-resolution-definition-records module sh05-artifact)
           explicit-definitions))
     :imports imports
     :import-bindings
     (sh06-resolution-import-binding-records module imports overrides)
     :lexical-scopes (:lexical-scopes analysis-inputs)
     :references (:references analysis-inputs)
     :core-bindings (sh06-resolution-core-records)
     :dependency-edges
     (sh06-resolution-dependency-edges source-path module imports overrides)
     :macro-expansion-binding
     {:artifact :gravity/sh05-macro-expansion-binding
      :schema-version 1
      :c4-artifact-id (:artifact-id sh05-artifact)
      :source-revision-id source-revision-id
      :macro-result-id (:expanded-syntax-stream-id sh05-artifact)
      :authenticated-envelope-id
      (sh06-resolution-envelope-id sh05-artifact)}
     :provenance {:actual-source-path source-path}}))

(def sh06-resolution-source-relative-path
  "bootstrap/gravity/src/gravity/resolution.gravity")
(def sh06-resolution-facade-relative-path
  "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity")
(def sh06-resolution-adapter-contract
  :gravity/sh06-to-c5-resolution-products-v1)
(def sh06-resolution-envelope-stage :c5-resolution)
(def sh06-resolution-sealed-artifact-kind
  :gravity/sh06-resolution-products)

;; Final source/plan pins are filled only after the leaf's semantic matrix is
;; stable.  A nil pin is rejected, so an in-progress leaf cannot accidentally
;; receive integration credit.
(def sh06-resolution-expected-source-byte-count 77209)
(def sh06-resolution-expected-source-content-hash
  "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56")
(def sh06-resolution-expected-plan-semantic-hash
  "sha256:ba8c292ffe3c703bbc220af0a7121496c134793d1e80a012f6fb0a0d9dc6b6fa")
(def sh06-resolution-expected-functions-semantic-hash
  "sha256:a1445872cda26a9aa0fb8959165eada593619de7a03f527a8ea8a8c216237b91")
(def sh06-resolution-expected-function-count 94)
(def sh06-resolution-expected-function-names-hash
  "sha256:1a12e29aea416d4df7c2904d366975ce240f5ecaee0b2b9c5aa95b1aa83d1ba4")
(def sh06-resolution-expected-function-shapes-hash
  "sha256:62aec401ab663019a10b63c1b91487f70b9756503d59e43879283afae8c02f57")
(def sh06-resolution-public-function-hashes
  {'sh06-build-resolution-template
   "sha256:154d52c95bc0488e5ec302ce9219b57d72995f5f54f137816ca980d97bf71f38"
   'sh06-verify-resolution-template
   "sha256:de579ce14c12644979fe636d5f7b32d02c505753b85463b8a1994eb9de7a03ec"
   'sh06-verify-resolution-resolved
   "sha256:80e3c1e5313ffb38805eba60110dc80edccb50218eaabd5edba98b2025e99342"})
(def sh06-resolution-public-function-shapes
  {'sh06-build-resolution-template {:arity 1 :params ['request]}
   'sh06-verify-resolution-template
   {:arity 2 :params ['analysis 'digest-requests]}
   'sh06-verify-resolution-resolved
   {:arity 3
    :params ['analysis 'digest-requests 'resolved-digests]}})

(defn sh06-resolution-boundary-fail!
  [rule source-path missing-field observed facts]
  (c5-resolution-fail!
   rule source-path
   {:source-span (source-span source-path 0)
    :symbol (:symbol facts)
    :syntax-id (:syntax-id facts)
    :namespace (:namespace facts)
    :profile (:profile facts)
    :target (:target facts)
    :candidate-bindings (:candidate-bindings facts)
    :dependency-edge (:dependency-edge facts)}
   {:severity :error
    :profile (:profile facts)
    :missing-fields [missing-field]
    :facts (merge {:sh06-boundary :gravity-resolution-plan} facts)
    :observed observed}))

(defn sh06-resolution-resolve-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate
               (java.io.File. directory sh06-resolution-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh06-resolution-source-relative-path)))

(defn sh06-resolution-read-pinned-source!
  [request-source]
  (let [source-path (sh06-resolution-resolve-source-path)
        nio-path (.toPath (java.io.File. source-path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           nio-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" request-source :pinned-resolution-source-readable
             source-path {:cause-message (.getMessage error)})))
        bytes
        (try
          (java.nio.file.Files/readAllBytes nio-path)
          (catch Exception error
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" request-source :pinned-resolution-source-bytes
             source-path {:cause-message (.getMessage error)})))
        content-hash (str "sha256:" (sha256-bytes-hex bytes))]
    (when-not
     (and (number? sh06-resolution-expected-source-byte-count)
          (string? sh06-resolution-expected-source-content-hash)
          attributes (.isRegularFile attributes)
          (= (long sh06-resolution-expected-source-byte-count)
             (.size attributes))
          (= sh06-resolution-expected-source-byte-count (alength bytes))
          (= sh06-resolution-expected-source-content-hash content-hash))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" request-source :exact-pinned-resolution-source
       {:source-path source-path
        :source-byte-count (alength bytes)
        :source-content-hash content-hash}
       {:expected-byte-count sh06-resolution-expected-source-byte-count
        :expected-content-hash
        sh06-resolution-expected-source-content-hash}))
    {:source-path source-path
     :source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
     :source-byte-count (alength bytes)
     :source-content-hash content-hash}))

(defn sh06-resolution-plan-identities
  [plan]
  (let [functions (:functions plan)
        shapes
        (into (sorted-map)
              (map (fn [[name function]]
                     [name (select-keys function [:arity :params])]))
              functions)]
    {:plan-semantic-hash
     (p15-s23-c11-mir-digest
      (p15-s23-stage2-compiler-artifact-semantic-input plan))
     :functions-semantic-hash (p15-s23-c11-mir-digest functions)
     :function-count (count functions)
     :function-names-hash
     (p15-s23-c11-mir-digest (vec (keys functions)))
     :function-shapes-hash (p15-s23-c11-mir-digest shapes)
     :public-function-hashes
     (into (sorted-map)
           (map (fn [name]
                  [name (p15-s23-c11-mir-digest (get functions name))]))
           (keys sh06-resolution-public-function-shapes))
     :public-function-shapes
     (select-keys shapes (keys sh06-resolution-public-function-shapes))}))