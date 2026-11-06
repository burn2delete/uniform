

(defn c-backend-validate-runtime-plan!
  [source-path target plan]
  (gravity.backend.c/validate-runtime-plan!
   {:instructions #'c-backend-runtime-derived-instructions
    :reject! #'c-backend-runtime-reject!
    :literal? #'c-backend-runtime-literal?
    :local-present? #'c-backend-runtime-local-present?
    :string-expression-supported? #'c-backend-runtime-string-expression-supported?
    :test-expression-supported? #'c-backend-runtime-test-expression-supported?
    :next-local-kinds #'c-backend-runtime-next-local-kinds}
   source-path target plan))

(defn c-backend-resolve-p15-s23-compiler-source-path
  "Resolve the repository-owned P15-S23 source even when a direct caller is
  running from an unrelated working directory."
  []
  (let [relative p15-s23-compiler-source-path
        classpath-roots
        (keep (fn [entry]
                (let [file (java.io.File. entry)]
                  (when (.isDirectory file) file)))
              (str/split (System/getProperty "java.class.path" "")
                         (re-pattern java.io.File/pathSeparator)))
        roots (distinct (cons (java.io.File.
                               (System/getProperty "user.dir"))
                              classpath-roots))]
    (or (some (fn [root]
                (loop [directory root]
                  (let [candidate (java.io.File. directory relative)]
                    (cond
                      (.isFile candidate) (.getPath candidate)
                      (.getParentFile directory)
                      (recur (.getParentFile directory))
                      :else nil))))
              roots)
        relative)))