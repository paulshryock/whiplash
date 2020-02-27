(ns whiplash.routes.services.leaderboard
  (:require [ring.util.http-response :refer :all]
            [whiplash.db.core :as db]
            [whiplash.time :as time]
            [datomic.client.api :as d]
            [whiplash.payouts :as payouts]))

(defn all-time-top-ten
  [{:keys [params] :as req}]
  (ok (db/find-top-ten)))

;; TODO maybe cache this every 10 minutes or so if it takes too long
(defn weekly-leaderboard
  [{:keys [params] :as req}]
  (let [weekly-leaderboard (->> (db/find-this-week-payout-leaderboard (time/to-date (time/last-monday)))
                                (group-by :user/name)
                                (map (fn [[k v]]
                                       (hash-map :user_name k
                                                 :payout (->> v
                                                              (map :bet/payout)
                                                              (apply +)))))
                                (sort-by :payout #(compare %2 %1))
                                vec)]
    (ok weekly-leaderboard)))

;; TODO maybe cache this every 10 minutes or so if it takes too long
(defn weekly-prop-bet-leaderboard
  [{:keys [params] :as req}]
  (let [weekly-leaderboard (->> (db/find-this-week-prop-bet-payout-leaderboard (time/to-date (time/last-monday)))
                                (group-by :user/name)
                                (map (fn [[k v]]
                                       (hash-map :user_name k
                                                 :payout (->> v
                                                              (map :bet/payout)
                                                              (apply +)))))
                                (sort-by :payout #(compare %2 %1))
                                vec)]
    (ok weekly-leaderboard)))

(defn get-bets
  [{:keys [params] :as req}]
  (let [{:keys [game_id match_id]} params
        ;; TODO figure out why this isnt getting casted by middleware
        game-id (Integer/parseInt game_id)
        match-id (Integer/parseInt match_id)
        unprocessed-bets (db/find-all-unprocessed-bets-for-game (d/db (:conn db/datomic-cloud))
                                                                {:match-id match-id
                                                                 :game-id  game-id})
        total-amounts-and-odds (-> unprocessed-bets
                                   (payouts/game-bet-stats :team/name)
                                   (payouts/team-odds))]
    (ok (or (->> unprocessed-bets
                 (group-by :team/name)
                 (map (fn [[team-name bets]]
                        (let [{:keys [bet/total bet/odds]} (get total-amounts-and-odds team-name)]
                          {team-name {:bets (sort-by :bet/amount
                                                     #(compare %2 %1)
                                                     (->> bets
                                                          (group-by :user/name)
                                                          (mapv (fn [[user-name bets]]
                                                                  {:user/name user-name
                                                                   :bet/amount (apply +
                                                                                      (map :bet/amount bets))}))))
                              :total total
                              :odds  odds}})))
                 (apply merge))
            {}))))

(defn get-prop-bets
  [{:keys [params] :as req}]
  (if-let [current-prop-bet (db/find-ongoing-prop-bet)]
    (let [db (d/db (:conn db/datomic-cloud))
          current-bets (->> (db/find-prop-bets {:db          db
                                                :prop-bet-id current-prop-bet})
                            (map #(d/pull db '[:bet/amount :bet/projected-result? :user/_prop-bets] %))
                            (mapv (fn [bet]
                                    (-> bet
                                        (merge (d/pull db
                                                       '[:user/name]
                                                       (-> bet :user/_prop-bets :db/id)))
                                        (dissoc :user/_prop-bets)))))
          total-amounts-and-odds (-> current-bets
                                     (payouts/game-bet-stats :bet/projected-result?)
                                     (payouts/team-odds))]
      {:status 200
       :headers {"Cache-Control" "max-age=3"}
       :body (or (->> current-bets
                     (group-by :bet/projected-result?)
                     (map (fn [[result bets]]
                            (let [{:keys [bet/total bet/odds]} (get total-amounts-and-odds result)]
                              {result {:bets (sort-by :bet/amount
                                                      #(compare %2 %1)
                                                      (->> bets
                                                           (group-by :user/name)
                                                           (mapv (fn [[user-name bets]]
                                                                   {:user/name user-name
                                                                    :bet/amount (apply +
                                                                                       (map :bet/amount bets))}))))
                                       :total total
                                       :odds  odds}})))
                     (apply merge))
                {})})
    {:status 404
     :headers {"Cache-Control" "max-age=3"}
     :body {:message "no ongoing prop bet"}}))
