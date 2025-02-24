;; Copyright (C) 2025 Andrew Leverette

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

(ns polydoc.logger.core
  "Component for managing application logging.

  This component provides functions for logging messages at different
  levels of severity, such as info, debug, warn, and error. It uses
  Taoensso's Telemere library to handle the actual logging."
  (:require
   [taoensso.telemere :as t]
   [polydoc.config.interface :as c]))

(t/set-min-level! :log (c/get-config [:polydoc/logger :min-level] :info))

(defn log
  ([level message] (t/log! level message))
  ([level message data] (t/log! {:level level :data data} message)))
