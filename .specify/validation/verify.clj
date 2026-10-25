(require '[clojure.java.io :as io]
         '[clojure.java.shell :as shell]
         '[clojure.pprint :as pprint]
         '[clojure.string :as str]
         '[gravity.tooling.strict-json :as json])
(import '[java.nio.file Files]
        '[java.nio.file.attribute FileAttribute]
        '[java.security MessageDigest])

;; Run from the repository root. Optional argument: pinned external specify CLI.
;; This verifier never dispatches an agent or compiler and writes only results.edn.
(def root (.getCanonicalFile (io/file ".")))
(def checks (atom []))
(defn check! [name condition]
  (when-not condition (throw (ex-info (str "Spec Kit check failed: " name) {})))
  (swap! checks conj name))
(defn read-json [file] (json/read-strict-json (slurp file)))
(defn files [path] (filter #(.isFile %) (file-seq (io/file path))))
(defn sha256 [file]
  (format "%064x" (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256")
                                        (Files/readAllBytes (.toPath file))))))
(defn relative [file] (str (.relativize (.toPath root) (.toPath (.getCanonicalFile file)))))
(defn execute! [directory & command]
  (let [result (apply shell/sh (concat command [:dir directory]))]
    (when-not (zero? (:exit result))
      (throw (ex-info "External Spec Kit command failed" {:command command :result result})))
    (:out result)))
(defn copy-tree! [source target]
  (doseq [file (files source)]
    (let [suffix (.relativize (.toPath (io/file source)) (.toPath file))
          output (.toFile (.resolve (.toPath (io/file target)) suffix))]
      (io/make-parents output)
      (io/copy file output))))

(let [pin (read-json ".specify/upstream.json")
      config (read-json ".specify/integration.json")
      project-files (mapcat files [".specify" ".agents" "specs/001-paired-source-core"])]
  (check! :pinned-release (= "5ea8b1cf0a38699340a38e00d02770308997d8c0" (get pin "commit")))
  (check! :codex-with-events-disabled
          (and (= ["codex"] (get config "installed_integrations"))
               (= "codex" (get config "default_integration"))
               (= "false" (get-in config ["integration_settings" "codex" "parsed_options" "events"]))))
  (check! :no-runtime-hooks-or-workflows
          (and (= {} (get (read-json ".specify/workflows/workflow-registry.json") "workflows"))
               (not (.exists (io/file ".specify/extensions.yml")))
               (not (.exists (io/file ".codex")))))
  (check! :no-new-non-lisp-executable-source
          (not-any? #(re-find #"\.(sh|bash|py|pyc|ps1)$" (.getName %)) project-files))
  (check! :ascii (every? #(every? (fn [c] (<= (int c) 127)) (slurp %)) project-files))
  (doseq [file project-files :when (or (str/ends-with? (.getName file) ".json")
                                      (= ".registry" (.getName file)))]
    (read-json file))
  (check! :json-parse true)
  (doseq [file (files ".specify/local-preset")]
    (let [suffix (.relativize (.toPath (io/file ".specify/local-preset")) (.toPath file))]
      (check! [:installed-preset (str suffix)]
              (= (slurp file) (slurp (.toFile (.resolve (.toPath (io/file ".specify/presets/uniform")) suffix)))))))
  (doseq [file (files ".specify/local-preset/commands")]
    (let [command (-> (.getName file) (str/replace #"^speckit\." "") (str/replace #"\.md$" ""))
          skill (slurp (str ".agents/skills/speckit-" command "/SKILL.md"))
          body (str/trim (nth (str/split (slurp file) #"---" 3) 2))]
      (check! [:generated-skill command]
              (and (str/includes? skill body) (str/includes? skill "source: preset:uniform")
                   (not (str/includes? skill "__SPECKIT_COMMAND_"))))))
  (doseq [path [".specify/README.md" ".specify/memory/constitution.md"
                "specs/001-paired-source-core/spec.md" "specs/001-paired-source-core/plan.md"
                "specs/001-paired-source-core/tasks.md" "specs/001-paired-source-core/evidence.md"]
          [_ target] (re-seq #"\]\(([^)]+)\)" (slurp path))
          :when (not (or (str/includes? target "://") (str/starts-with? target "#")))]
    (check! [:local-link path target]
            (.exists (io/file (.getParentFile (io/file path)) (first (str/split target #"#"))))))
  (when-let [cli (first *command-line-args*)]
    (let [scratch (.toFile (Files/createTempDirectory "uniform-speckit-cli-" (make-array FileAttribute 0)))
          generated (io/file scratch "generated")]
      (try
        (check! :cli-version (str/includes? (execute! scratch cli "version") "1.2.3"))
        (execute! scratch cli "init" (str generated) "--integration" "codex"
              "--integration-options=--events false" "--script" "sh" "--non-interactive" "--ignore-agent-tools")
        (execute! generated cli "workflow" "remove" "speckit")
        (copy-tree! ".specify/local-preset" (io/file generated ".specify/local-preset"))
        (copy-tree! ".specify/templates/overrides" (io/file generated ".specify/templates/overrides"))
        (execute! generated cli "preset" "add" "--dev" ".specify/local-preset")
        (check! :cli-local-preset (= "uniform" (get (first (json/read-strict-json (execute! generated cli "preset" "list" "--json"))) "id")))
        (doseq [file (files ".agents/skills")]
          (check! [:official-cli-skill-generation (relative file)]
                  (= (slurp file) (slurp (io/file generated (relative file))))))
        (check! :cli-no-workflows (str/includes? (execute! generated cli "workflow" "list") "No workflows installed"))
        (check! :cli-no-extensions (str/includes? (execute! generated cli "extension" "list") "No extensions installed"))
        (execute! generated cli "preset" "update" "uniform" "--dev" ".specify/local-preset")
        (doseq [file (files ".agents/skills")]
          (check! [:official-preset-update (relative file)]
                  (= (slurp file) (slurp (io/file generated (relative file))))))
        (finally (doseq [file (reverse (file-seq scratch))] (io/delete-file file true))))))
  (let [report {:scope :spec-kit-scaffold-only :upstream-commit (get pin "commit")
                :checks @checks :passed (count @checks)
                :input-sha256 (into (sorted-map) (map (juxt relative sha256))
                                    (remove #(= "results.edn" (.getName %)) (mapcat files [".agents" ".specify"])))
                :unrun [:codex-runtime-discovery-and-chat :compiler-tests :raw-PR356-log-authentication
                        :independent-adoption-review :integration-merge]}]
    (spit ".specify/validation/results.edn" (with-out-str (pprint/pprint report)))
    (println "validation passed:" (count @checks) "Spec Kit scaffold checks; compiler/model unrun")))

(shutdown-agents)
