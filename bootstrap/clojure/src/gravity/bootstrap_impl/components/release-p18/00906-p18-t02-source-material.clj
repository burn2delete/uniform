

(defn p18-t02-source-material
  []
  (mapv (fn [{:keys [path]}]
          {:path path
           :hash (if (string? path)
                   (p18-file-sha256 path)
                   "sha256:missing")})
        p18-t02-source-inventory))

(defn p18-t02-source-hashes
  []
  (into (sorted-map)
        (map (fn [{:keys [role path]}]
               [role (if (string? path)
                       (p18-file-sha256 path)
                       "sha256:missing")]))
        p18-t02-source-inventory))

(defn p18-t02-packaged-source-entries
  []
  (into
   []
   (keep
    (fn [{:keys [path jar-entry]}]
      (let [suffix (str "/" jar-entry)]
        (when (and (string? path) (string? jar-entry)
                   (str/ends-with? path suffix))
          {:path path
           :source-root (subs path 0 (- (count path) (count suffix)))
           :jar-entry jar-entry}))))
   (filterv :jar-entry p18-t02-source-inventory)))

(defn p18-t02-compiled-jar-entries
  []
  (mapv :compiled-jar-entry
        (filterv :compiled-jar-entry p18-t02-source-inventory)))

(defn p18-t02-expected-jar-file-entries
  []
  (->> (concat [p18-t02-manifest-entry]
               (p18-t02-compiled-jar-entries)
               (map :jar-entry (p18-t02-packaged-source-entries)))
       sort
       vec))

(defn p18-t02-source-inventory-report
  []
  (let [inventory p18-t02-source-inventory
        roles (mapv :role inventory)
        paths (mapv :path inventory)
        packaged-inventory (filterv :jar-entry inventory)
        compiled-inventory (filterv :compiled-jar-entry inventory)
        packaged-sources (p18-t02-packaged-source-entries)
        jar-entries (mapv :jar-entry packaged-inventory)
        compiled-jar-entries (mapv :compiled-jar-entry compiled-inventory)
        material (p18-t02-source-material)
        valid?
        (and (= #{:launcher :bootstrap :diagnostics :cli
                  :darwin-publication :deps}
                (set roles))
             (= (count roles) (count (set roles)))
             (= (count paths) (count (set paths)))
             (= #{:bootstrap :diagnostics :cli :darwin-publication}
                (set (map :role packaged-inventory)))
             (= [:launcher] (mapv :role compiled-inventory))
             (= (count packaged-inventory) (count packaged-sources))
             (= (count jar-entries) (count (set jar-entries)))
             (= [p18-t02-launcher-class-entry] compiled-jar-entries)
             (= (count compiled-jar-entries)
                (count (set compiled-jar-entries)))
             (every? (fn [path]
                       (and (string? path)
                            (not (str/blank? path))
                            (not (.isAbsolute (java.io.File. path)))
                            (.isFile (java.io.File. path))))
                     paths)
             (every? #(re-matches #"sha256:[0-9a-f]{64}" (:hash %))
                     material))]
    {:valid? (boolean valid?)
     :roles roles
     :paths paths
     :packaged-source-entries (mapv :jar-entry packaged-sources)
     :source-material material}))

(defn p18-t02-validate-source-inventory!
  []
  (let [report (p18-t02-source-inventory-report)]
    (when-not (:valid? report)
      (fail! "P18T02006"
             "packaged JVM CLI source inventory is invalid"
             {:source-span {:source "P18-T02 source inventory"}
              :missing-fact :complete-packaged-source-inventory
              :observed-source-count (count (:paths report))
              :observed-packaged-source-count
              (count (:packaged-source-entries report))
              :remediation
              "Restore the exact declared launcher, bootstrap, diagnostics, CLI presentation, Darwin publication provider, and deps inputs before building the package."}))
    report))

(defn p18-t02-jar-source-inventory-report
  [jar-entries]
  (let [expected (mapv :jar-entry (p18-t02-packaged-source-entries))
        observed (filterv #(and (string? %)
                                (str/ends-with? % ".clj"))
                          jar-entries)
        entry-frequencies (frequencies jar-entries)
        valid?
        (and (every? string? jar-entries)
             (= expected observed)
             (every? #(= 1 (get entry-frequencies % 0)) expected))]
    {:valid? (boolean valid?)
     :expected expected
     :observed observed
     :entry-frequencies entry-frequencies}))

(defn p18-t02-jar-inventory-report
  [jar-file-entries]
  (let [expected (p18-t02-expected-jar-file-entries)
        string-entries? (every? string? jar-file-entries)
        observed (if string-entries?
                   (vec (sort jar-file-entries))
                   (vec jar-file-entries))
        entry-frequencies (frequencies jar-file-entries)
        valid?
        (and string-entries?
             (= expected observed)
             (every? #(= 1 (get entry-frequencies % 0)) expected))]
    {:valid? (boolean valid?)
     :expected expected
     :observed observed
     :missing (vec (remove (set observed) expected))
     :unexpected (vec (remove (set expected) observed))
     :entry-frequencies entry-frequencies}))

(defn p18-t02-jar-command
  []
  (vec
   (concat
    ["jar" "--create"
     (str "--file=" p18-t02-jar-path)
     (str "--manifest=" p18-t02-manifest-path)
     "--date=2026-01-01T00:00:00Z"]
    (mapcat (fn [jar-entry]
              ["-C" p18-t02-classes-dir jar-entry])
            (p18-t02-compiled-jar-entries))
    (mapcat (fn [{:keys [source-root jar-entry]}]
              ["-C" source-root jar-entry])
            (p18-t02-packaged-source-entries)))))

(defn p18-t02-classpath-entries
  []
  (vec (remove str/blank?
               (str/split (System/getProperty "java.class.path")
                          (re-pattern java.io.File/pathSeparator)))))

(defn p18-t02-runtime-classpath-entries
  []
  (vec
   (remove
    (fn [entry]
      (or (= entry "bootstrap/clojure/src")
          (= entry "./bootstrap/clojure/src")
          (= entry p18-t02-jar-path)
          (str/ends-with? entry "/bootstrap/clojure/src")
          (str/ends-with? entry "/gravity-jvm-cli.jar")))
    (p18-t02-classpath-entries))))

(defn p18-t02-runtime-classpath
  []
  (str/join java.io.File/pathSeparator
            (p18-t02-runtime-classpath-entries)))