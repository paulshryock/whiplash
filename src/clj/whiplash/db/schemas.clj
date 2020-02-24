(ns whiplash.db.schemas)

(def ^:private schemas
  ;; Game betting MVP
  {:0 [{:db/doc         "User first name"
        :db/ident       :user/first-name
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "User last name"
        :db/ident       :user/last-name
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "User email address"
        :db/ident       :user/email
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        :db/unique      :db.unique/identity
        }
       {:db/doc         "Username"
        :db/ident       :user/name
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        :db/unique      :db.unique/identity
        }
       {:db/doc         "User password"
        :db/ident       :user/password
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "User status"
        :db/ident       :user/status
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "User verify email token"
        :db/ident       :user/verify-token
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }
       {:db/doc "User's cash"
        :db/ident :user/cash
        :db/valueType :db.type/bigint
        :db/cardinality :db.cardinality/one
        }
       ;; example of enumeration in Datomic
       {:db/ident :user.status/pending}
       {:db/ident :user.status/active}
       {:db/ident :user.status/inactive}

       {:db/doc         "Team name"
        :db/ident       :team/name
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Team id (from pandascore)"
        :db/ident       :team/id
        :db/valueType   :db.type/long
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Game type"
        :db/ident       :game/type
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/one
        }
       {:db/ident :game.type/csgo}
       {:db/doc         "Match id (from pandascore)"
        :db/ident       :match/id
        :db/valueType   :db.type/long
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Game id (from pandascore)"
        :db/ident       :game/id
        :db/valueType   :db.type/long
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Match name (from pandascore)"
        :db/ident       :match/name
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one
        }

       {:db/doc         "Bet amount"
        :db/ident       :bet/amount
        :db/valueType   :db.type/bigint
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Bet payout"
        :db/ident       :bet/payout
        :db/valueType   :db.type/bigint
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Time of bet"
        :db/ident       :bet/time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Bet processed yet"
        :db/ident       :bet/processed?
        :db/valueType   :db.type/boolean
        :db/cardinality :db.cardinality/one
        }
       {:db/doc         "Bet processed time"
        :db/ident       :bet/processed-time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one}

       {:db/doc         "User bets"
        :db/ident       :user/bets
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/many
        :db/isComponent true
        }

       {:db/doc         "Game bet pool for a team"
        :db/ident       :game/team-pool
        :db/valueType   :db.type/bigint
        :db/cardinality :db.cardinality/one
        }
       ]
   ;; Prop betting MVP
   :1 [{:db/ident :user.status/admin}

       {:db/doc         "Prop bets"
        :db/ident       :user/prop-bets
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/many
        :db/isComponent true}

       {:db/doc         "Events that we've hosted"
        :db/ident       :whiplash/events
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/many}

       {:db/doc         "Prop bets that we showed to the users during the event"
        :db/ident       :event/propositions
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/many
        :db/isComponent true}

       {:db/doc         "Title of the event"
        :db/ident       :event/title
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one}

       {:db/doc         "twitch user to stream for duration of event"
        :db/ident       :event/twitch-user
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Is this event still running?"
        :db/ident       :event/running?
        :db/valueType   :db.type/boolean
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Event start time"
        :db/ident       :event/start-time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Event end time"
        :db/ident       :event/end-time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Is this the current proposition?"
        :db/ident       :proposition/running?
        :db/valueType   :db.type/boolean
        :db/cardinality :db.cardinality/one}

       {:db/doc         "The actual prop bet string"
        :db/ident       :proposition/text
        :db/valueType   :db.type/string
        :db/cardinality :db.cardinality/one}

       {:db/doc         "The outcome of the prop bet"
        :db/ident       :proposition/result?
        :db/valueType   :db.type/boolean
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Proposition start time"
        :db/ident       :proposition/start-time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Proposition end time"
        :db/ident       :proposition/end-time
        :db/valueType   :db.type/instant
        :db/cardinality :db.cardinality/one}

       {:db/doc         "Reference back to admin prop bet"
        :db/ident       :bet/proposition
        :db/valueType   :db.type/ref
        :db/cardinality :db.cardinality/one}

       {:db/doc         "User's projected outcome of the proposition"
        :db/ident       :bet/projected-result?
        :db/valueType   :db.type/boolean
        :db/cardinality :db.cardinality/one}
       ]})

(defn migrations->schema-tx
  []
  (->> schemas
       (mapcat val)
       vec))
