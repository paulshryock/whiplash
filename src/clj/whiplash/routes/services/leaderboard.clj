(ns whiplash.routes.services.leaderboard
  (:require [ring.util.http-response :refer :all]
            [whiplash.db.core :as db]
            [datomic.client.api :as d]
            [whiplash.payouts :as payouts]))

(defn all-time-top-ten
  [{:keys [params] :as req}]
  (ok (db/find-top-ten)))

(defn event-score-leaderboard
  [{:keys [params] :as req}]
  (let [db (d/db (:conn db/datomic-cloud))
        ongoing-event (db/find-ongoing-event db)
        last-event (when-not ongoing-event
                     (db/find-last-event db))
        bets (when (or ongoing-event
                       last-event)
               (db/find-all-user-bets-for-event {:db       db
                                                 :event-id (or ongoing-event
                                                               last-event)}))]
    (if bets
      (ok
        (let [transformed-bets (->> bets
                                    :event/propositions
                                    (mapcat :bet/_proposition)
                                    (map (fn [bet]
                                           (-> bet
                                               (assoc :user/name (get-in bet [:user/_prop-bets :user/name]))
                                               (dissoc :user/_prop-bets)))))]
          (->> transformed-bets
               (group-by :user/name)
               (map (fn [[user bets]]
                      {:user_name user
                       :score     (apply +
                                         0
                                         (keep (fn [{:keys [bet/amount bet/payout]}]
                                                 (when (number? payout)
                                                   (- payout amount)))
                                               bets))}))
               (sort-by :score #(compare %2 %1)))))
      (not-found []))))

(defn get-prop-bets
  [{:keys [params] :as req}]
  (let [db (d/db (:conn db/datomic-cloud))
        ;; TODO: use pull-ongoing-proposiiton
        current-proposition (db/find-ongoing-proposition db)]
    (if current-proposition
      (let [current-bets (->> (db/find-all-user-bets-for-proposition {:db db
                                                                      :prop-bet-id current-proposition})
                              (apply concat)
                              (map (fn [bet]
                                     (-> bet
                                         (assoc :user/name (get-in bet [:user/_prop-bets :user/name]))
                                         (dissoc :user/_prop-bets)))))
            total-amounts-and-odds (-> current-bets
                                       (payouts/game-bet-totals :bet/projected-result?)
                                       (payouts/team-odds))
            grouped-bets (group-by :bet/projected-result? current-bets)
            add-other-side (cond
                             (and (nil? (get grouped-bets true))
                                  (some? (get grouped-bets false)))
                             (assoc grouped-bets true [])

                             (and (some? (get grouped-bets true))
                                  (nil? (get grouped-bets false)))
                             (assoc grouped-bets false [])

                             :else
                             grouped-bets)]
        (ok (or (->> add-other-side
                     (map (fn [[result bets]]
                            (let [{:keys [bet/total bet/odds]} (get total-amounts-and-odds result)]
                              {result {:bets  (sort-by :bet/amount
                                                       #(compare %2 %1)
                                                       (->> bets
                                                            (group-by :user/name)
                                                            (mapv (fn [[user-name bets]]
                                                                    {:user/name  user-name
                                                                     :bet/amount (apply +
                                                                                        (map :bet/amount bets))}))))
                                       :total total
                                       :odds  odds}})))
                     (apply merge))
                {})))
      (not-found {:message "no ongoing prop bet"}))))
