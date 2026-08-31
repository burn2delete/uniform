

(defn c-backend-runtime-bytes
  [value]
  (let [text (str value)
        bytes (.getBytes (str text) java.nio.charset.StandardCharsets/UTF_8)]
    (vec (map #(bit-and (int %) 0xff) bytes))))

(defn c-backend-runtime-byte-array-source
  [name bytes]
  (let [values (if (seq bytes) bytes [0])]
    (str "  static const unsigned char " name "[] = {"
         (str/join "," values)
         "};\n"
         "  fwrite(" name ", 1, " (if (seq bytes) (str "sizeof(" name ")") "0")
         ", stdout);\n")))

(defn c-backend-runtime-value-declaration
  "Declare a stable byte array for one scalar value and return the descriptor
  used by local references.  The descriptor retains Clojure truthiness so a
  runtime `if` can branch without re-evaluating the source in the host."
  [instruction counter indent]
  (let [padding (apply str (repeat indent "  "))
        name (str "gravity_literal_" (swap! counter inc))
        value (:value instruction)
        bytes (c-backend-runtime-bytes value)
        values (if (seq bytes) bytes [0])]
    {:source (str padding
                  "static const unsigned char " name "[] = {"
                  (str/join "," values) "};\n")
     :descriptor {:name name
                  :length (count bytes)
                  ;; Reference the declaration in local conditions so
                  ;; -Wall/-Werror does not report an unused binding while
                  ;; preserving Clojure truthiness for scalar values.
                  :truth (str "(sizeof(" name ") >= 0 && "
                              (if (or (nil? value) (false? value)) "0" "1")
                              ")")}}))

(defn c-backend-runtime-write-source
  [descriptor indent]
  (let [padding (apply str (repeat indent "  "))
        name (:name descriptor)
        length (:length descriptor)]
    (str padding "fwrite(" name ", 1, "
         (if (nil? length) (str "sizeof(" name ")") length)
         ", stdout);\n")))

(defn c-backend-runtime-test-source
  [instruction env]
  (case (:op instruction)
    :literal (if (or (nil? (:value instruction))
                     (false? (:value instruction))) "0" "1")
    :quote (if (or (nil? (:value instruction))
                   (false? (:value instruction))) "0" "1")
    :local (get-in env [(:name instruction) :truth] "0")
    "0"))

(declare c-backend-runtime-value-source)
(declare c-backend-runtime-instruction-source)

(defn c-backend-runtime-value-source
  [instruction counter indent env]
  (let [padding (apply str (repeat indent "  "))
        op (:op instruction)]
    (cond
      (#{:literal :quote} op)
      (let [{:keys [source descriptor]}
            (c-backend-runtime-value-declaration instruction counter indent)]
        (str source (c-backend-runtime-write-source descriptor indent)))
      (= :local op)
      (c-backend-runtime-write-source (get env (:name instruction)) indent)
      (= :builtin-call op)
      (if (= 'str (:function instruction))
        ;; `str` is a byte-string concatenation at the runtime boundary.  Each
        ;; operand writes directly to stdout in sequence, so no separator is
        ;; introduced and embedded NUL/UTF-8 bytes remain lossless.
        (apply str
               (map #(c-backend-runtime-value-source % counter indent env)
                    (:args instruction)))
        "")
      (= :if op)
      (str padding "if (" (c-backend-runtime-test-source (:test instruction) env)
           ") {\n"
           (c-backend-runtime-value-source (:then instruction) counter
                                           (inc indent) env)
           padding "} else {\n"
           (c-backend-runtime-value-source (:else instruction) counter
                                           (inc indent) env)
           padding "}\n")
      (= :let op)
      (let [binding-state
            (reduce (fn [state {:keys [name expr]}]
                      (let [prior-source (:source state)
                            prior-env (:env state)
                            {:keys [source descriptor]}
                            (c-backend-runtime-value-declaration
                             expr counter indent)]
                        {:source (str prior-source
                                      ;; Bindings evaluate to scalar literals;
                                      ;; declaration is enough to make later
                                      ;; local references runtime writes.
                                      source)
                         :env (assoc prior-env name descriptor)}))
                    {:source "" :env env}
                    (:bindings instruction))]
        (str (:source binding-state)
             (c-backend-runtime-value-source
              (first (:body instruction)) counter indent (:env binding-state))))
      :else "")))

(defn c-backend-runtime-instruction-source
  ([instruction counter indent]
   (c-backend-runtime-instruction-source instruction counter indent {}))
  ([instruction counter indent env]
   (let [padding (apply str (repeat indent "  "))
         op (:op instruction)]
     (cond
       (#{:literal :quote :local} op) ""
       (= :println op)
       (str (apply str
                   (map-indexed
                    (fn [index arg]
                      (str (when (pos? index)
                             (c-backend-runtime-byte-array-source
                              (str "gravity_space_" (swap! counter inc))
                              [32]))
                           (c-backend-runtime-value-source
                            arg counter indent env)))
                    (:args instruction)))
            padding
            (c-backend-runtime-byte-array-source
             (str "gravity_newline_" (swap! counter inc)) [10]))
       (= :do op)
       (apply str
              (map #(c-backend-runtime-instruction-source % counter indent env)
                   (:body instruction)))
       (= :if op)
       (str padding "if (" (c-backend-runtime-test-source (:test instruction) env)
            ") {\n"
            (c-backend-runtime-instruction-source (:then instruction) counter
                                                   (inc indent) env)
            padding "} else {\n"
            (c-backend-runtime-instruction-source (:else instruction) counter
                                                   (inc indent) env)
            padding "}\n")
       (= :let op)
       (let [binding-state
             (reduce (fn [state {:keys [name expr]}]
                       (let [prior-source (:source state)
                             prior-env (:env state)
                             {:keys [source descriptor]}
                             (c-backend-runtime-value-declaration
                              expr counter indent)]
                         {:source (str prior-source source)
                          :env (assoc prior-env name descriptor)}))
                     {:source "" :env env}
                     (:bindings instruction))]
         (str (:source binding-state)
              (apply str
                     (map #(c-backend-runtime-instruction-source
                            % counter indent (:env binding-state))
                          (:body instruction)))))
       :else ""))))

(defn c-backend-runtime-source
  "Emit C that performs the instruction semantics at process runtime.  Each
  scalar literal is a byte array written by an individual fwrite call, so a
  source edit changes the generated runtime program without collapsing the
  whole result into a compile-time stdout string."
  [plan]
  (let [counter (atom 0)
        main (get-in plan [:functions (:entrypoint plan)])]
    (str "#include <stdio.h>\n\n"
         "int main(void) {\n"
         (apply str
                (map #(c-backend-runtime-instruction-source % counter 1 {})
                     (:instructions main)))
         "  return 0;\n"
         "}\n")))

(defn c-backend-source
  [stdout]
  (str "#include <stdio.h>\n\n"
       "int main(void) {\n"
       "  static const unsigned char gravity_output[] = \""
       (c-backend-c-escape stdout) "\";\n"
       "  fwrite(gravity_output, 1, sizeof(gravity_output) - 1, stdout);\n"
       "  return 0;\n"
       "}\n"))

(def ^:private c-backend-process-timeout-ms 60000)
(def ^:private c-backend-process-max-output-bytes (* 8 1024 1024))
(def ^:private c-backend-process-max-descendants 64)
(def ^:private c-backend-process-max-staging-entries 16)
(def ^:dynamic *c-backend-process-timeout-ms*
  c-backend-process-timeout-ms)
(def ^:dynamic *c-backend-process-max-output-bytes*
  c-backend-process-max-output-bytes)
(def ^:dynamic *c-backend-process-max-descendants*
  c-backend-process-max-descendants)
(def ^:dynamic *c-backend-process-start-fn*
  (fn [^java.lang.ProcessBuilder builder]
    (.start builder)))
(def ^:private c-backend-private-directory-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE})

(def ^:private c-backend-no-follow
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))