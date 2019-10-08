(ns whiplash.time
  (:require [java-time :as time]
            [java-time.repl :as repl]))

;; TODO unit tests

(defn now
  []
  (time/with-zone-same-instant (time/zoned-date-time) "UTC"))

(defn to-millis
  ([] (to-millis (now)))
  ([date] (time/to-millis-from-epoch date)))

(defn days-delta
  ([days]
   (days-delta (now) days))
  ([start days]
   (time/plus start (time/days days))))

(defn days-delta-trunc
  "Returns ZonedDateTime truncated to the day. i.e. 2019-10-07T00:00Z[UTC]"
  ([days]
   (days-delta-trunc (now) days))
  ([start days]
   (time/truncate-to (days-delta start days) :days)))

(defn date-iso-string
  [date]
  (time/format (time/formatter :iso-instant) date))

(defn http-date-str
  [date]
  (time/format (time/formatter :rfc-1123-date-time) date))

(defn last-monday
  "Returns truncated ZonedDateTime of the previous monday. Returns today if it is monday."
  []
  (.with (days-delta-trunc 0) (time/day-of-week :monday)))

(defn next-monday
  "Returns truncated ZonedDateTime of next monday."
  []
  (.with (days-delta-trunc 7) (time/day-of-week :monday)))

