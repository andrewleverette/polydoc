;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.



(ns polydoc.database.core
  (:require
   [honey.sql :as sql]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [polydoc.logger.interface :as l]))

;; Component key
(def query-engine-component-key :polydoc/database)

(defn parse-query
  [query]
  (try
    (sql/format query)
    (catch Exception e
      (l/error "Failed to parse query" {:cause e :query query})
      (ex-info "Failed to parse query" {:cause e :query query}))))

(defprotocol Database
  "Interface for executing queries against a datasource."
  (execute! [this query] "Executes a SQL query provided as a map and returns the result."))

(defrecord SqlDatabase [datasource]
  Database
  (execute! [_ query]
    (try
      (let [paresed-query (parse-query query)]
        (l/info "Executing query" {:query paresed-query})
        (jdbc/execute! datasource paresed-query))
      (catch Exception e
        (l/error "Error executing query" {:cause e :query query})))))

(defn create-database
  [datasource]
  (->SqlDatabase datasource))

(defmethod ig/init-key query-engine-component-key [_ datasource]
  (l/info "Initializing query engine component...")
  (create-database datasource))

(defmethod ig/halt-key! query-engine-component-key [_ query-engine]
  (l/info "Shutting down query engine component...")
  (assoc query-engine :datasource nil))
