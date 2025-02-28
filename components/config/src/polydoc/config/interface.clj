;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.config.interface
  (:require [polydoc.config.core :as config]))

(defn get-config
  "Given a sequence of keys, return the value of the config at those keys.
  Returns nil or default if not found."
  ([ks] (config/get-config ks))
  ([ks default] (config/get-config ks default)))

(defn refresh
  "Refresh the config"
  []
  (config/refresh))
