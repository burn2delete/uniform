
(defn p15-s23-sh07-b51-builtin-call-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-builtin-call-keys)
       (symbol? (:caller row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (contains? p15-s23-sh07-b51-effective-builtins (:builtin row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (= (contains? p15-s23-sh07-declared-builtin-exact-arities
                     (:builtin row))
          (:compiler-internal? row))
       (or (not (:compiler-internal? row))
           (= (get p15-s23-sh07-declared-builtin-exact-arities
                   (:builtin row))
              (:arity row)))))

(defn p15-s23-sh07-b51-module-source-id [descriptor]
  (p15-s23-sh07-b51-canonical-id
   (dissoc descriptor :module-source-id)))

(defn- p15-s23-sh07-b51-module-source-valid* [descriptor]
  (let [spec (p15-s23-sh07-b51-module-spec (:module-ordinal descriptor))
        definitions (:definitions descriptor)
        external-bindings (:external-bindings descriptor)
        builtin-calls (:builtin-calls descriptor)
        lexical-count (:lexical-binding-count descriptor)
        aggregate (when (and (vector? definitions)
                             (integer? (:core-binding-count descriptor))
                             (integer? lexical-count)
                             (vector? external-bindings))
                    (+ (:core-binding-count descriptor)
                       (count definitions) lexical-count
                       (count external-bindings)))
        source-file (when spec (p15-s23-sh07-governed-source-file
                                (:logical-source-path spec)))
        source-bytes (when source-file
                       (java.nio.file.Files/readAllBytes (.toPath source-file)))
        source-text (when source-bytes
                      (String. source-bytes java.nio.charset.StandardCharsets/UTF_8))
        source-module (when source-text
                        (parse-module (.getCanonicalPath source-file)
                                      (read-forms (.getCanonicalPath source-file)
                                                  source-text)))
        source-table (when source-module (stage0-function-table source-module))
        expected-binding-census
        (when source-module
          (p15-s23-sh07-b51-binding-census
           (.getCanonicalPath source-file) source-module
           (:module-ordinal spec)))
        expected-builtin-calls
        (when source-table
          (p15-s23-sh07-b51-builtin-call-census source-table))
        expected-definitions
        (when source-table
          (mapv (fn [form]
                  (let [definition
                        (function-definition-form (.getCanonicalPath source-file)
                                                  form)
                        definition (assoc definition :arity
                                          (count (:params definition)))]
                    {:name (:name definition) :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)}))
                (filter #(function-definition-form
                          (.getCanonicalPath source-file) %)
                        (:forms source-module))))]
    (and (p15-s23-sh07-b51-exact-keys? descriptor
                                              p15-s23-sh07-b51-m0-keys)
         (= :gravity/sh07-b51-module-source-v1 (:artifact descriptor))
         (= 1 (:schema-version descriptor))
         (= (select-keys spec [:module-ordinal :module :logical-source-path
                               :owner :exports])
            (select-keys descriptor
                         [:module-ordinal :module :logical-source-path
                          :owner :exports]))
         (integer? (:source-byte-count descriptor))
         (<= 0 (:source-byte-count descriptor))
         (p15-s23-sh07-b51-digest-id? (:source-sha256 descriptor))
         (= (:source-byte-count descriptor) (alength source-bytes))
         (= (:source-sha256 descriptor)
            (str "sha256:" (sha256-bytes-hex source-bytes)))
         (= :meta (:profile descriptor))
         (= :jvm (:target descriptor))
         (= #{} (:effects descriptor))
         (= #{} (:capabilities descriptor))
         (= :safe (:safety descriptor))
         (vector? definitions)
         (every? p15-s23-sh07-b51-definition-row-valid? definitions)
         (= expected-definitions definitions)
         (= (count definitions) (count (set (map :name definitions))))
         (= (:exports descriptor)
            (vec (filter (set (:exports descriptor))
                         (map :name definitions))))
         (every? #(= (contains? (set (:exports descriptor)) (:name %))
                      (= :public (:visibility %)))
                 definitions)
         (= (:core-binding-count expected-binding-census)
            (:core-binding-count descriptor))
         (integer? (:lexical-binding-count descriptor))
         (<= 0 (:lexical-binding-count descriptor))
         (= (:lexical-binding-count expected-binding-census)
            (:lexical-binding-count descriptor))
         (= (:definition-count expected-binding-census)
            (count definitions))
         (vector? external-bindings)
         (every? p15-s23-sh07-b51-external-binding-row-valid?
                 external-bindings)
         (= (count external-bindings)
            (count (set (map :symbol external-bindings))))
         (vector? builtin-calls)
         (every? p15-s23-sh07-b51-builtin-call-row-valid? builtin-calls)
         (= expected-builtin-calls builtin-calls)
         (= aggregate (:aggregate-binding-count descriptor))
         (<= aggregate 2440)
         (= (:module-source-id descriptor)
            (p15-s23-sh07-b51-module-source-id descriptor)))))

(defn p15-s23-sh07-b51-module-source-valid? [descriptor]
  (try
    (boolean (p15-s23-sh07-b51-module-source-valid* descriptor))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-module-source [descriptor]
  (let [sealed (assoc descriptor :module-source-id
                      (p15-s23-sh07-b51-module-source-id descriptor))]
    (when-not (p15-s23-sh07-b51-module-source-valid? sealed)
      (throw (ex-info "invalid SH-07 B51 M0 descriptor"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    sealed))

(defn p15-s23-sh07-b51-build-module-source
  [source-path source-text module binding-census external-bindings
   builtin-calls]
  (let [spec (first (filter #(= (:module module) (:module %))
                            p15-s23-sh07-b51-module-specs))
        source-bytes (.getBytes ^String source-text
                                java.nio.charset.StandardCharsets/UTF_8)
        function-table (stage0-function-table module)
        definitions
        (mapv (fn [form]
                (when-let [definition (function-definition-form source-path form)]
                  (let [definition (assoc definition
                                          :arity (count (:params definition)))]
                    {:name (:name definition)
                     :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)})))
              (filter #(function-definition-form source-path %)
                      (:forms module)))
        base
        {:artifact :gravity/sh07-b51-module-source-v1
         :schema-version 1
         :module-source-id nil
         :module-ordinal (:module-ordinal spec)
         :module (:module spec)
         :logical-source-path (:logical-source-path spec)
         :owner (:owner spec)
         :source-byte-count (alength source-bytes)
         :source-sha256 (str "sha256:" (sha256-bytes-hex source-bytes))
         :profile (:profile module)
         :target (:target module)
         :effects (:effects module)
         :capabilities (:capabilities module)
         :safety (:safety module)
         :definitions definitions
         :exports (:exports spec)
         :core-binding-count (:core-binding-count binding-census)
         :lexical-binding-count (:lexical-binding-count binding-census)
         :external-bindings external-bindings
         :aggregate-binding-count
         (+ (:core-binding-count binding-census)
            (:definition-count binding-census)
            (:lexical-binding-count binding-census)
            (count external-bindings))
         :builtin-calls builtin-calls}]
    (when-not (and spec
                   (= (.getCanonicalPath (java.io.File. source-path))
                      (.getCanonicalPath
                       ^java.io.File
                       (p15-s23-sh07-governed-source-file
                        (:logical-source-path spec))))
                   (= (set (keys function-table))
                      (set (map :name definitions)))
                   (= (count definitions)
                      (:definition-count binding-census)))
      (throw (ex-info "invalid SH-07 B51 module source input"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (p15-s23-sh07-b51-seal-module-source base)))

(defn p15-s23-sh07-b51-edge-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys? row p15-s23-sh07-b51-edge-keys)
       (integer? (:ordinal row)) (<= 0 (:ordinal row))
       (p15-s23-sh07-b51-digest-id? (:caller-module-source-id row))
       (symbol? (:caller-function row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (vector? (:callsite-path row))
       (every? #(and (integer? %) (<= 0 %)) (:callsite-path row))
       (p15-s23-sh07-b51-digest-id? (:callee-module-source-id row))
       (symbol? (:callee-function row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-function-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))
       (= :authenticated-b51-namespace-external (:authorization row))))

(defn p15-s23-sh07-b51-manifest-id [manifest]
  (p15-s23-sh07-b51-canonical-id (dissoc manifest :manifest-id)))

(defn- p15-s23-sh07-b51-definition-by-name [descriptor function]
  (first (filter #(= function (:name %)) (:definitions descriptor))))
