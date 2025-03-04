;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.srm-api.controllers.documents
  (:require
   [polydoc.documents.interface :as docs]))

(defn get-document
  [document-repository request]
  (let [id (get-in request [:path-params :id])
        document (docs/get-document-by-id document-repository id)]
    {:status 200 :body document}))


