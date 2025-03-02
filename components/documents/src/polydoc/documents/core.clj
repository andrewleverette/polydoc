;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.documents.core
  (:require [polydoc.documents.repository :as repo]))

(defmulti create-document-repository
  (fn [tag _] tag))

(defmethod create-document-repository :sql [_ db] (repo/->SqlDocumentRepository db))

(defn search-documents
  [repository params]
  (repo/search-documents repository params))

(defn search-documents-count
  [repository params]
  (repo/search-documents-count repository params))

(defn get-document-by-id
  [repository id]
  (repo/get-document-by-id repository id))
