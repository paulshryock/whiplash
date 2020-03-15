(ns whiplash.routes.services.event
  (:require [ring.util.http-response :refer :all]
            [whiplash.db.core :as db]
            [datomic.client.api :as d]))

(defn create-event
  [{:keys [body-params] :as req}]
  ;; TODO validation of twitch and twitch_user
  (let [{:keys [title channel-id source]} body-params
        source-valid? (or (= source "twitch")
                          (= source "youtube"))]
    (cond
      (some empty? [title channel-id source])
      (bad-request {:message "No args can be empty."})

      (some? (db/find-ongoing-event))
      (method-not-allowed {:message "Cannot create event, one is already ongoing"})

      (not source-valid?)
      (bad-request {:message "Source is incorrect."})

      :else
      (do
        (db/create-event {:title       title
                          :channel-id channel-id
                          :source (if (= "twitch" source)
                                    :event.stream-source/twitch
                                    :event.stream-source/youtube)})
        (ok {})))))

(defn get-current-event
  [{:keys [body-params] :as req}]
  (if-let [event (db/find-ongoing-event)]
    (ok (db/resolve-enum
          (d/pull (d/db (:conn db/datomic-cloud))
                  '[:event/start-time
                    :event/running?
                    :event/channel-id
                    :event/title
                    :event/stream-source]
                  event)
          :event/stream-source))
    (not-found {})))

(defn end-current-event
  [{:keys [body-params] :as req}]
  (let [event (db/find-ongoing-event)
        prop-bet (db/find-ongoing-proposition)]
    (cond
      (some? prop-bet)
      (method-not-allowed {:message "Ongoing prop bet, you must end it before ending the event"})

      (nil? event)
      (method-not-allowed {:message "No ongoing event"})

      :else
      (do (db/end-event event)
          (ok {})))))
