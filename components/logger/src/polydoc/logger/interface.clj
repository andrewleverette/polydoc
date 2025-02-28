;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.logger.interface
  (:require [polydoc.logger.core :as l]))

(def info (partial l/log :info))

(def debug (partial l/log :debug))

(def warn (partial l/log :warn))

(def error (partial l/log :error))
