;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

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
  [this]
  (core/init-schema! this))

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
