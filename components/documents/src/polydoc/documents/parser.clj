;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.documents.parser
  (:require
   [polydoc.logger.interface :as l]))

(def document-keys
  [:documents/document_id
   :documents/document_type
   :documents/file_type
   :documents/title
   :documents/uploaded_by_user_id
   :documents/upload_date
   :documents/document_storage_path
   :documents/ocr_text_available
   :documents/created_at
   :documents/updated_at])

(def document-metadata-value-keys
  [:document_metadata_values/value_data_type
   :document_metadata_values/attribute_definition_id
   :document_metadata_values/attribute_value
   :document_metadata_values/created_at
   :document_metadata_values/updated_at])

(def query-params-mapping
  {:search-term (fn [term] [:like [:lower :d.title] [:lower (str "%" term "%")]])
   :document-type (fn [doc-type] [:= :d.document_type doc-type])
   :file-type (fn [file-type] [:= :d.file_type file-type])})

(defn parse-document-metadata
  "Parses a document metadata record
  
  Returns a map of document metadata values or nil if the record
  does not contain document metadata values"
  [record]
  (let [{:document_metadata_values/keys [attribute_definition_id]
         :as metadata} (select-keys record document-metadata-value-keys)]
    (when attribute_definition_id
      metadata)))

(defn parse-document-object
  "Parses a document object
  
  Given a document UUID and a list of document metadata records,
  returns a map representing the document object or nil if the
  document records do not all represent the same document
  
  The attributes key is used to identify the document metadata. This property will
  either be `:documents/summaryMetadataAttributes` or `:documents/metadataAttributes`
  depending on if the query is a search or a get-by-id query"
  [document-id attrbutes-key records]
  (when (and (seq records) (every? #(= document-id (:documents/document_id %)) records))
    (merge
     (select-keys (first records) document-keys)
     {attrbutes-key (->> records (map parse-document-metadata) (filterv identity))})))

(defn group-records-by-document-id
  [records]
  (group-by :documents/document_id records))

(defn parse-document-search-results
  "Parses a document search result set
  
  Returns a list of document objects with their summary metadata attributes"
  [results]
  (let [grouped (group-records-by-document-id results)]
    (mapv (fn [[document-id records]] (parse-document-object document-id :documents/summary-metadata-attributes records)) grouped)))

(defn parse-document-result
  "Parses a document result set

  Returns a document object with all its metadata attributes"
  [results]
  (l/info "Parsing document result set" {:results results})
  (let [grouped (group-records-by-document-id results)]
    (if (<= 1 (count grouped))
      (let [[document-id records] (first grouped)]
        (parse-document-object document-id :documents/metadata-attributes records))
      (throw (ex-info "Document result set contains multiple document UUIDs" {:results results})))))

(defmulti parse-query-params
  "Parses parameters into a query map for document querys"
  (fn [tag _] tag))

(defmethod parse-query-params :search [_ params]
  (let [filters (->> params (map (fn [[k v]] (when-let [f (k query-params-mapping)] (f v)))) (filterv identity))
        where-clause (if (= 1 (count filters))
                       (first filters)
                       (reduce conj [:and] filters))]
    (cond-> {}
      where-clause (assoc :where where-clause)
      (:limit params) (assoc :limit (:limit params))
      (:offset params) (assoc :offset (:offset params)))))
