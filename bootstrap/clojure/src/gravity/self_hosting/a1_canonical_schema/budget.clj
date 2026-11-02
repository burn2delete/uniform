(ns gravity.self-hosting.a1-canonical-schema.budget
  "Reservation, live-resource, and terminal-result accounting for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.config :as config]))

(defn budget []
  (atom {:work {:limit (:work config/limits)
                :reserved config/terminal-work :committed 0}
         :input {:limit (:input-bytes config/limits) :reserved 0 :committed 0}
         :output {:limit (:output-bytes config/limits) :reserved 0 :committed 0}
         :terminal-result {:limit config/terminal-bytes
                           :reserved config/terminal-bytes
                           :committed 0 :work config/terminal-work
                           :metered-bytes config/terminal-bytes :reserved? true}
         :frames {:limit (:frames config/limits) :live 0 :peak 0}
         :key-slots {:limit (:key-slots config/limits) :live 0 :peak 0}
         :digest-slots {:limit (:digest-slots config/limits) :live 0 :peak 0}}))

(defn reserve! [state counter quantity path]
  (when (neg? quantity) (config/fail! "E-HOST" (config/path-of "internal")))
  (let [{:keys [limit reserved committed]} (get @state counter)]
    (when (> (+ committed reserved quantity) limit)
      (config/fail! "E-BOUND" path))
    (swap! state update-in [counter :reserved] + quantity)))

(defn commit! [state counter quantity]
  (when (or (neg? quantity)
            (< (get-in @state [counter :reserved]) quantity))
    (config/fail! "E-HOST" (config/path-of "internal")))
  (swap! state (fn [s]
                 (-> s
                     (update-in [counter :reserved] - quantity)
                     (update-in [counter :committed] + quantity)))))

(defn release-reservation! [state counter quantity]
  (when (or (neg? quantity)
            (< (get-in @state [counter :reserved]) quantity))
    (config/fail! "E-HOST" (config/path-of "internal")))
  (swap! state update-in [counter :reserved] - quantity))

(defn charge! [state counter quantity path]
  (reserve! state counter quantity path)
  (commit! state counter quantity))

(defn acquire! [state counter quantity path]
  (let [{:keys [limit live]} (get @state counter)]
    (when (neg? quantity)
      (config/fail! "E-HOST" (config/path-of "internal")))
    (when (> (+ live quantity) limit) (config/fail! "E-BOUND" path))
    (swap! state (fn [s]
                   (-> s
                       (update-in [counter :live] + quantity)
                       (update-in [counter :peak] max (+ live quantity)))))))

(defn release! [state counter quantity]
  (when (or (neg? quantity) (< (get-in @state [counter :live]) quantity))
    (config/fail! "E-HOST" (config/path-of "internal")))
  (swap! state update-in [counter :live] - quantity))

(defn work! [state quantity path]
  (charge! state :work quantity path))

(defn path-payload-size [path]
  (reduce (fn [total index]
            (let [segment (config/path-segment path index)]
              (+ total (if (string? segment)
                         (+ 5 (config/utf8-length segment))
                         9))))
          0 (range (config/path-count path))))

(defn emit-rejection! [state diagnostic path]
  (let [element-work (config/path-count path)
        payload-bytes (path-payload-size path)
        snapshot @state
        work-counter (:work snapshot)
        output-counter (:output snapshot)
        fits? (and (<= (+ (:committed work-counter)
                          (:reserved work-counter)
                          element-work)
                       (:limit work-counter))
                   (<= (+ (:committed output-counter)
                          (:reserved output-counter)
                          payload-bytes)
                       (:limit output-counter)))]
    (if-not fits?
      (config/rejected "E-BOUND" [])
      (do
        (swap! state (fn [current]
                       (-> current
                           (update-in [:work :reserved] + element-work)
                           (update-in [:output :reserved] + payload-bytes))))
        (commit! state :work element-work)
        (commit! state :output payload-bytes)
        (config/rejected diagnostic (config/path-vector path))))))

(defn finalize-terminal! [state]
  (commit! state :work config/terminal-work)
  (commit! state :terminal-result config/terminal-bytes)
  (swap! state assoc-in [:terminal-result :reserved?] false))
