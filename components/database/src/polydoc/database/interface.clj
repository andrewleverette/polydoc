;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.database.interface
  (:require [polydoc.database.core :as core]))

(defn execute!
  "Executes a query on the database"
  [database query]
  (core/execute! database query))
