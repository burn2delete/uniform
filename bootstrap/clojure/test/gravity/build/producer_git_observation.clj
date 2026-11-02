(ns gravity.build.producer-git-observation
  "RV2 selected observer Git bookends. Loading starts no process."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner])
  (:import [java.nio.file Files LinkOption]
           [java.nio.file.attribute BasicFileAttributes]))

(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- require! [condition field]
  (when-not condition
    (throw (ex-info "RV2 Git observation cannot establish its binding"
                    {:id "RV2-GIT-OBSERVATION" :field field}))))

(defn- sha [bytes] (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- file-state [path]
  (let [p (.toPath (io/file path))]
    (require! (.isAbsolute p) :absolute-input)
    (cond
      (Files/isSymbolicLink p)
      (let [target (str (Files/readSymbolicLink p))
            resolved (.getCanonicalPath (io/file path))
            _ (require! (not (Files/isSymbolicLink (.toPath (io/file resolved)))) :unresolved-link)
            file (if (.isDirectory (io/file resolved))
                   {:path resolved :state :directory :contents :unbound}
                   (file-state resolved))]
        (require! (= target (str (Files/readSymbolicLink p))) :input-drift)
        {:path path :state :symlink :target target :resolved file})

      (Files/isRegularFile p no-links)
      (let [attributes #(Files/readAttributes p BasicFileAttributes no-links)
            stamp (fn [^BasicFileAttributes a]
                    [(.fileKey a) (.size a) (.lastModifiedTime a)])
            before (stamp (attributes))
            bytes (Files/readAllBytes p)
            after (stamp (attributes))]
        (require! (= before after) :input-drift)
        {:path path :state :file :sha256 (sha bytes) :bytes (alength bytes)})

      (Files/notExists p no-links) {:path path :state :absent}
      :else (require! false :unsupported-input))))

(defn- path-git []
  (some (fn [directory]
          (let [file (io/file directory "git")]
            (when (and (.isFile file) (.canExecute file))
              (.getAbsolutePath file))))
        (str/split (or (System/getenv "PATH") "")
                   (re-pattern (java.util.regex.Pattern/quote java.io.File/pathSeparator)) -1)))

(defn freeze-binding!
  "Read explicit executable/dispatch/config/search inputs, never run Git.
  The file set is supplied by the exact method; includes/native helper coverage
  remain unknown. Inherited environment is hashed, never written as values."
  [root {:keys [executable dispatch-files configuration-files selection-links] :as inputs}]
  (require! (= #{:executable :dispatch-files :configuration-files :selection-links}
                (set (keys inputs))) :input-shape)
  (require! (and (.isAbsolute (io/file root)) (string? executable)
                 (every? vector? [dispatch-files configuration-files selection-links])
                 (seq configuration-files)) :inputs)
  (let [selected (path-git)]
    (require! (and selected (= (.getCanonicalPath (io/file selected))
                               (.getCanonicalPath (io/file executable)))) :path-selection)
    {:schema :gravity/rv2-git-binding-v1
     :root (.getCanonicalPath (io/file root)) :inputs inputs
     :path-selected-entry selected
     :environment-sha256 (sha (.getBytes (pr-str (into (sorted-map) (System/getenv))) "UTF-8"))
     :executable-inputs (mapv file-state (into [executable] dispatch-files))
     :configuration-inputs (mapv file-state configuration-files)
     :selection-inputs (mapv file-state selection-links)
     :configuration-scope :explicit-file-set
     :native-helper-and-config-search-coverage :unknown}))

(defn verify-binding! [binding]
  (require! (= binding (freeze-binding! (:root binding) (:inputs binding))) :identity-drift))

(defn- cause-with [predicate error]
  (loop [error error seen #{}]
    (when (and error (not (contains? seen error)))
      (if (predicate error) error (recur (.getCause ^Throwable error) (conj seen error))))))

(defn- fatal [error]
  (cause-with #(or (instance? ThreadDeath %) (instance? VirtualMachineError %)
                   (instance? LinkageError %)) error))

(defn- restore-interrupt! [error]
  (when (cause-with #(instance? InterruptedException %) error)
    (.interrupt (Thread/currentThread))))

(defn lifecycle
  "Build hooks for selected bookends. ports are the canonical retaining API;
  injected ports support compiler-free controls. Raw records stay local."
  ([binding records]
   (lifecycle binding records {:retain runner/retain-owned-process!
                              :handles runner/retained-process-handles
                              :finalize runner/finalize-owned-process!
                              :verify verify-binding!}))
  ([binding records ports]
   {:before! (fn [_ directory]
               (require! (= (:root binding) (.getCanonicalPath ^java.io.File directory)) :cwd)
               (try ((:verify ports) binding)
                    (catch Throwable error (restore-interrupt! error) (throw error))))
    :retain! (fn [process command directory]
               (let [record (atom {:process process :argv command :cwd (str directory)
                                   :root-wait-exited? :unknown :descendant-coverage :unknown})]
                 ;; Preserve the actual Process even if retention itself fails.
                 (swap! records conj record)
                 (try
                   (let [owned ((:retain ports) process)]
                     (swap! record assoc :owned-process owned)
                     record)
                   (catch Throwable error
                     (swap! record assoc :retain-error error :terminal :unproven)
                     (restore-interrupt! error)
                     (throw error)))))
    :waited! (fn [record exit writer-error]
               (swap! record assoc :exit-code exit :root-wait-exited? true)
               (when writer-error
                 (swap! record assoc :writer-error writer-error)
                 (restore-interrupt! writer-error)
                 (when-let [error (fatal writer-error)] (throw error))))
    :finalize!
    (fn [record original]
      (let [errors (atom [])
            interrupted? (.isInterrupted (Thread/currentThread))]
        (when original (swap! record assoc :operation-error original))
        (try
          (swap! record assoc :finalization ((:finalize ports) (:owned-process @record)))
          (swap! record assoc :raw-handles ((:handles ports) (:owned-process @record)))
          (catch Throwable error
            (swap! record assoc :finalization-error error :terminal :unproven)
            (swap! errors conj error)))
        (try ((:verify ports) binding)
             (catch Throwable error (swap! record assoc :identity-error error)
                    (swap! errors conj error)))
        (doseq [error (cond-> @errors original (conj original))] (restore-interrupt! error))
        (when interrupted? (.interrupt (Thread/currentThread)))
        ;; Never demote fatal cleanup to an ordinary nonzero Git refusal.
        (let [primary (or (when (fatal original) original)
                          (some fatal @errors) original (first @errors))]
          (when primary
            (doseq [error (cond-> @errors original (conj original))
                    :when (and (not (identical? primary error))
                               (not (cause-with #(identical? primary %) error)))]
              (.addSuppressed ^Throwable primary error))
            (when-not (identical? primary original) (throw primary))))))}))

(defn- observation-view [binding records]
  {:schema :gravity/rv2-git-observation-v1
   :binding binding
   :processes
   (mapv (fn [record]
           (let [record @record]
             (reduce (fn [view field]
                       (if-let [error (get record field)]
                         (assoc view field {:class (.getName (class error))}) view))
                     (select-keys record [:argv :cwd :exit-code :root-wait-exited?
                                          :finalization :descendant-coverage :terminal])
                     [:retain-error :operation-error :writer-error :finalization-error :identity-error])))
         @records)
   :capture-v1-ready? false :cache-admission? false})

(defn run-selected!
  "Route only the two actual fresh C2 observer bookends through these hooks.
  Test execution is outside their thread binding. The existing test entrypoint
  may supply a frozen Git binding; raw Process/token/handles remain local.
  Serialization of these observations supplies no capture/cache admission."
  [binding records options operation]
  (require! (and (map? binding) (string? (:root binding))
                 (try (.isAbsolute (.toPath (io/file (:root binding))))
                      (catch Exception _ false))) :binding-shape)
  (require! (= (:root binding) (.getCanonicalPath (io/file (System/getProperty "user.dir")))) :root)
  (let [report (wiring/run-child-unit! ['gravity.c2-artifact-identity-test]
                                      (assoc options :observe-producer? true
                                             :producer-git-binding binding
                                             :producer-git-lifecycle (lifecycle binding records))
                                      operation)]
    (assoc report :test-producer-git-observation (observation-view binding records))))
