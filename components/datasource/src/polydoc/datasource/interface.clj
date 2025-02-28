;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.datasource.interface
  (:require [polydoc.datasource.core :as core]))

(defn create-datasource
  [config]
  (core/create-datasource config))

(defn init
  [ds]
  (core/init ds))

(defn get-datasource
  "Returns the database datasource from the datasource component."
  [ds]
  (core/get-datasource ds))
