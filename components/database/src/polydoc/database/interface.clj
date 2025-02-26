(ns polydoc.database.interface
  (:require [polydoc.database.core :as core]))

(defn execute!
  "Executes a query on the database"
  [database query]
  (core/execute! database query))
