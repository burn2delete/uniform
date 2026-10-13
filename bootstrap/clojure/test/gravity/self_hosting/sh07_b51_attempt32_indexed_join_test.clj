(ns gravity.self-hosting.sh07-b51-attempt32-indexed-join-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt28-root6-independent-replay-test
             :as root6]
            [gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test
             :as producer]))

(defn- private-bootstrap-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "missing Attempt32 indexed-join seam"
                      {:symbol symbol}))))

(defn- first-edge-coordinate [carrier]
  (let [edge (get-in carrier [:link-manifest :edges 0])
        descriptors (:module-source-descriptors carrier)
        caller-index
        (first
         (keep-indexed
          (fn [index descriptor]
            (when (= (:module-source-id descriptor)
                     (:caller-module-source-id edge))
              index))
          descriptors))]
    {:edge edge :caller-index caller-index}))

(defn- remove-row [rows field value]
  (vec (remove #(= value (get % field)) rows)))

(defn- join-cases [carrier]
  (let [{:keys [edge caller-index]} (first-edge-coordinate carrier)
        descriptors (:module-source-descriptors carrier)
        caller (nth descriptors caller-index)
        caller-definition
        (first (filter #(= (:caller-function edge) (:name %))
                       (:definitions caller)))
        external
        (first (filter #(= (:callee-function edge) (:symbol %))
                       (:external-bindings caller)))
        duplicate-source-id (:module-source-id (first descriptors))]
    [{:label :authentic :carrier carrier :expected true}
     {:label :edge-owner
      :carrier (assoc-in carrier [:link-manifest :edges 0 :owner-edge]
                         :forbidden-owner-edge)
      :expected false}
     {:label :route-plan
      :carrier
      (assoc-in carrier [:execution-bundle :routes 0 :caller-plan-id]
                (bootstrap/reader-canonical-hash :forged-caller-plan))
      :expected false}
     {:label :duplicate-definition
      :carrier
      (update-in carrier
                 [:module-source-descriptors caller-index :definitions]
                 conj caller-definition)
      :expected false}
     {:label :missing-definition
      :carrier
      (update-in carrier
                 [:module-source-descriptors caller-index :definitions]
                 remove-row :name (:caller-function edge))
      :expected false}
     {:label :duplicate-external-binding
      :carrier
      (update-in carrier
                 [:module-source-descriptors caller-index :external-bindings]
                 conj external)
      :expected false}
     {:label :missing-external-binding
      :carrier
      (update-in carrier
                 [:module-source-descriptors caller-index :external-bindings]
                 remove-row :symbol (:callee-function edge))
      :expected false}
     {:label :missing-descriptor
      :carrier (update carrier :module-source-descriptors pop)
      :expected false}
     {:label :duplicate-module-source
      :carrier
      (assoc-in carrier [:module-source-descriptors 1 :module-source-id]
                duplicate-source-id)
      :expected false}]))

(defn- host-reference [carrier]
  (let [descriptors (:module-source-descriptors carrier)
        manifest (:link-manifest carrier)
        plan-carriers (:module-plan-carriers carrier)
        plans (mapv :plan plan-carriers)
        bundle (:execution-bundle carrier)
        bundle-valid
        @(private-bootstrap-var
          'p15-s23-sh07-b51-execution-bundle-valid-structure?)]
    (boolean
     (and (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
           descriptors manifest)
          (bundle-valid descriptors manifest plans bundle)))))

(defn- producer-indexed [carrier]
  (let [descriptors (:module-source-descriptors carrier)
        edges (get-in carrier [:link-manifest :edges])
        routes (get-in carrier [:execution-bundle :routes])
        plan-carriers (:module-plan-carriers carrier)]
    (boolean
     (producer/invoke 'sh07-b51-r1-routes-valid?
                      routes edges descriptors plan-carriers))))

(defn- root6-indexed [carrier]
  (let [descriptors (:module-source-descriptors carrier)
        plan-carriers (:module-plan-carriers carrier)
        manifest (:link-manifest carrier)
        routes (get-in carrier [:execution-bundle :routes])
        indexes
        (root6/invoke 'sh07-b51-r6-c30-edge-indexes
                      descriptors plan-carriers)]
    (boolean
     (and (= :accepted (:status indexes))
          (root6/invoke 'sh07-b51-r6-c30-manifest?
                        manifest descriptors indexes (:manifest-id manifest))
          (root6/invoke 'sh07-b51-r6-c30-routes?
                        routes (:edges manifest) indexes)))))

(deftest indexed-joins-match-host-reference-and-reject-collisions
  (let [carrier (producer/accepted-carrier)]
    (doseq [{:keys [label carrier expected]} (join-cases carrier)]
      (testing (name label)
        (let [host (host-reference carrier)
              producer-result (producer-indexed carrier)
              root6-result (root6-indexed carrier)]
          (is (= expected host) [label :host host])
          (is (= host producer-result) [label :producer producer-result])
          (is (= host root6-result) [label :root6 root6-result]))))))

(deftest runtime-fuel-remains-the-governed-fixed-value
  (is (= 8388608
         @(private-bootstrap-var
           'p15-s23-sh07-b51-runtime-machine-maximum-steps))))
