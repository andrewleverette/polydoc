;; Copyright (C) 2025 Andrew Leverette

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

(ns polydoc.migrations.interface
  (:require
   [polydoc.migrations.core :as core]))

(defn create-migrations
  "Creates a new migrations component."
  [datasource]
  (core/create-migrations datasource))

(defn create!
  "Creates the up/down migration files with the current timestamp and given name."
  [this name]
  (core/create! this name))

(defn init-schema!
  "Applies the initial database schema."
  [this])

(defn migrate!
  "Runs all pending migrations."
  [this]
  (core/migrate! this))

(defn pending
  "Returns a list of pending migrations."
  [this]
  (core/pending this))

(defn rollback!
  "Rolls back the last migration."
  [this]
  (core/rollback! this))
