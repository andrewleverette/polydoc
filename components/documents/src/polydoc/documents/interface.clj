;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.documents.interface
  (:require [polydoc.documents.core :as core]))

(defn create-document-repository
  "Creates a document repository instance"
  [tag db]
  (core/create-document-repository tag db))

(defn search-documents
  "Searches for documents
  
  Returns a list of document objects that match the search criteria"
  [repository params]
  (core/search-documents repository params))

(defn search-documents-count
  "Counts the number of documents that match the search criteria"
  [repository params]
  (core/search-documents-count repository params))

(defn get-document-by-id
  "Retrieves a document by its UUID"
  [repository id]
  (core/get-document-by-id repository id))
