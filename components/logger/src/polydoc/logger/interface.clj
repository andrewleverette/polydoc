(ns polydoc.logger.interface
  (:require [polydoc.logger.core :as l]))

(def info (partial l/log :info))

(def debug (partial l/log :debug))

(def warn (partial l/log :warn))

(def error (partial l/log :error))
