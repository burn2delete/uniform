(ns gravity.package.native-metadata-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.package :as package]))

(defn- diagnostic [& arguments] arguments)

(defn- presence [value]
  (if (coll? value) (seq value) (some? value)))

(def ^:private checks
  [[package/check-project-manifest! "PKG1006" :lockfile-complete
    {:lockfile-complete true} {}]
   [package/check-artifact-manifest! "PKG3005" :safety-evidence
    {:evidence {:safety :unverified}} {}]
   [package/check-package-operation! "PKG4001" :download-verified
    {:download-verified true} {}]
   [package/check-resolution-report! "PKG5002" :capability-compatible
    {:capability-compatible true} {}]
   [package/check-capability-manifest! "PKG6004" :denied-authority
    {:capabilities {:denies #{:network} :requests #{:io/stdout}}}
    {:capabilities {:denies #{:network} :requests #{:network :io/stdout}}}]
   [(fn [fail-var module subject] (package/check-package-safety! #'presence fail-var module subject))
    "PKG8001" :unsafe-audit-metadata {:unsafe-audit-metadata [:unverified]} {}]
   [package/check-registry-record! "PKG9001" :access-grant {:access-grant true} {}]
   [package/check-provenance-record! "PKG10001" :source-graph-hash {:source-graph-hash :unverified} {}]
   [package/check-target-matrix! "PKG11002" :explicit-target {} {:implicit-host-target? true}]
   [package/check-signing-sbom! "PKG12002" :canonical-payload {:signature {:canonical-payload true}} {}]])

(deftest public-contract-separates-native-metadata-from-admission
  (is (= #{'decide-policy 'select-exact 'check-project-manifest! 'check-artifact-manifest!
           'check-package-operation! 'check-resolution-report! 'check-capability-manifest!
           'check-package-safety! 'check-registry-record! 'check-provenance-record!
           'check-target-matrix! 'check-signing-sbom!}
         (set (keys (ns-publics 'gravity.package)))))
  (is (= #{'clojure.set 'clojure.walk}
         (set (map ns-name (vals (ns-aliases 'gravity.package))))))
  (doseq [[name operation] (ns-publics 'gravity.package)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.package (:namespace contract)))
      (if (contains? #{'decide-policy 'select-exact} name)
        (is (= (map count (:arglists (meta operation)))
               (map count (get-in contract [:public-api name :arglists]))))
        (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists]))))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (= :non-admitting (:authority contract)))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:performs-io? contract)))
      (is (false? (:self-hosted? contract))))))

(deftest accepting-metadata-never-observes-the-failure-var
  (doseq [[check _ _ accepted] checks]
    (is (nil? (check nil {:source-path "native.gravity"} accepted)))))

(deftest rejections-retain-exact-module-subject-and-callback-result
  (doseq [[check id field _ rejected] checks]
    (let [module {:source-path "native.gravity" :unsafe-island :retained}
          subject (assoc rejected :ai/human-review :required :runtime-checked [:retained])
          calls (atom []) sentinel (Object.)]
      (with-redefs [diagnostic (fn [& arguments] (swap! calls conj arguments) sentinel)]
        (is (identical? sentinel (check #'diagnostic module subject))))
      (is (= 1 (count @calls)))
      (is (= [id module subject {:missing-fields [field]}] (vec (first @calls))))
      (is (identical? module (second (first @calls))))
      (is (identical? subject (nth (first @calls) 2))))))

(deftest boolean-flags-require-literal-true
  (doseq [[check field] [[package/check-project-manifest! :lockfile-complete]
                         [package/check-package-operation! :download-verified]
                         [package/check-resolution-report! :capability-compatible]]
          value [nil false :true 1 "true" {} []]]
    (let [calls (atom 0)]
      (with-redefs [diagnostic (fn [& _] (swap! calls inc))]
        (check #'diagnostic {} {field value}))
      (is (= 1 @calls)))))

(deftest safety-evidence-preserves-host-truthiness
  (doseq [value [0 "" [] {} :unverified true]]
    (is (nil? (package/check-artifact-manifest! nil {} {:evidence {:safety value}}))))
  (doseq [value [nil false]]
    (is (= "PKG3005"
           (first (package/check-artifact-manifest! #'diagnostic {} {:evidence {:safety value}}))))))

(deftest capability-overlap-preserves-empty-and-missing-set-behavior
  (doseq [subject [nil {} {:capabilities {}}
                   {:capabilities {:denies #{:network} :requests #{}}}
                   {:capabilities {:denies #{} :requests #{:network}}}]]
    (is (nil? (package/check-capability-manifest! nil {} subject))))
  (is (= "PKG6004"
         (first (package/check-capability-manifest!
                 #'diagnostic {} {:capabilities {:denies #{:network} :requests #{:network}}})))))

(deftest failure-var-is-read-after-subject-observation
  (doseq [[check id field _ rejected] checks]
    (let [calls (atom []) sentinel (Object.)
          final (fn [& arguments] (swap! calls conj arguments) sentinel)
          subject (reify clojure.lang.ILookup
                    (valAt [this key] (.valAt ^clojure.lang.ILookup this key nil))
                    (valAt [_ key fallback]
                      (alter-var-root #'diagnostic (constantly final))
                      (get rejected key fallback)))]
      (with-redefs [diagnostic (fn [& _] (throw (ex-info "Premature failure lookup" {})))]
        (is (identical? sentinel (check #'diagnostic {} subject))))
      (is (= 1 (count @calls)))
      (is (= id (ffirst @calls)))
      (is (= {:missing-fields [field]} (last (first @calls))))
      (is (identical? subject (nth (first @calls) 2))))))

(deftest diagnostic-exceptions-escape-with-original-identity
  (doseq [[check _ _ _ rejected] checks]
    (let [original (ex-info "original failure" {:origin :caller})
          observed (with-redefs [diagnostic (fn [& _] (throw original))]
                     (try (check #'diagnostic {} rejected)
                          (catch Throwable error error)))]
      (is (identical? original observed)))))

(deftest registry-and-signature-flags-require-literal-true
  (doseq [[check path] [[package/check-registry-record! [:access-grant]]
                        [package/check-signing-sbom! [:signature :canonical-payload]]]
          value [nil false :true 1 "true" {} []]]
    (let [calls (atom 0)]
      (with-redefs [diagnostic (fn [& _] (swap! calls inc))]
        (check #'diagnostic {} (assoc-in {} path value)))
      (is (= 1 @calls)))))

(deftest provenance-preserves-host-truthiness-and-target-refuses-only-literal-true
  (doseq [value [0 "" [] {} :unverified true]]
    (is (nil? (package/check-provenance-record! nil {} {:source-graph-hash value}))))
  (doseq [value [nil false]]
    (is (= "PKG10001" (first (package/check-provenance-record! #'diagnostic {} {:source-graph-hash value})))))
  (doseq [value [nil false :true 1 "true" {} []]]
    (is (nil? (package/check-target-matrix! nil {} {:implicit-host-target? value})))))

(deftest supplied-presence-operation-retains-empty-collection-and-false-behavior
  (doseq [value [false "" 0 [:unverified] {:audit :unverified} #{:unverified}]]
    (is (nil? (package/check-package-safety! #'presence nil {} {:unsafe-audit-metadata value}))))
  (doseq [value [nil [] {} #{} '()]]
    (is (= "PKG8001"
           (first (package/check-package-safety! #'presence #'diagnostic {} {:unsafe-audit-metadata value}))))))

(deftest presence-is-resolved-before-subject-and-failure-after-presence
  (let [events (atom []) sentinel (Object.)
        final (fn [& arguments] (swap! events conj [:new-failure (first arguments)]) sentinel)
        subject (reify clojure.lang.ILookup
                  (valAt [this key] (.valAt ^clojure.lang.ILookup this key nil))
                  (valAt [_ key _]
                    (swap! events conj [:lookup key])
                    (alter-var-root #'presence (constantly (fn [_] (throw (ex-info "Late presence root" {})))))
                    (alter-var-root #'diagnostic (constantly final))
                    :unverified))]
    (with-redefs [presence (fn [value] (swap! events conj [:old-presence value]) false)
                  diagnostic (fn [& _] (throw (ex-info "Premature failure root" {})))]
      (is (identical? sentinel (package/check-package-safety! #'presence #'diagnostic {} subject))))
    (is (= [[:lookup :unsafe-audit-metadata] [:old-presence :unverified] [:new-failure "PKG8001"]] @events))))

(deftest presence-operation-exception-retains-identity-and-prevents-failure
  (let [original (ex-info "original presence failure" {:origin :caller}) calls (atom 0)
        observed (with-redefs [presence (fn [_] (throw original))
                               diagnostic (fn [& _] (swap! calls inc))]
                   (try (package/check-package-safety! #'presence #'diagnostic {} {})
                        (catch Throwable error error)))]
    (is (identical? original observed))
    (is (zero? @calls))))
