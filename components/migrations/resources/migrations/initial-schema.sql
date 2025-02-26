-- Description: Create initial tables for PolyDoc

-- Base Table: users (No dependencies)
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

-- Base Table: roles (No dependencies)
CREATE TABLE IF NOT EXISTS roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_description TEXT,
    is_system_role BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

-- Table: metadata_attribute_definitions (No dependencies)
CREATE TABLE IF NOT EXISTS metadata_attribute_definitions (
    attribute_definition_id UUID PRIMARY KEY,
    attribute_name VARCHAR(100) UNIQUE,
    attribute_description TEXT,
    attribute_data_type VARCHAR(20),
    is_system_attribute BOOLEAN,
    validation_rules TEXT,
    user_interface_hint VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

-- Base Table: document_ingestion_sources (No dependencies)
CREATE TABLE IF NOT EXISTS document_ingestion_sources (
    ingestion_source_id UUID PRIMARY KEY,
    source_type VARCHAR(20),
    source_name VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

-- Base Table: user_subscriptions (FK to users)
CREATE TABLE IF NOT EXISTS user_subscriptions (
    user_subscription_id UUID PRIMARY KEY,
    user_id UUID,
    subscription_name VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    subscription_criteria_type VARCHAR(20),
    notification_channel VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key constraint for user_id
    CONSTRAINT fk_user_subscriptions_users
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

--;;

-- Table: documents (FK to users)
CREATE TABLE IF NOT EXISTS documents (
    document_id UUID PRIMARY KEY,
    document_type VARCHAR(50),
    file_type VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    uploaded_by_user_id UUID,
    upload_date TIMESTAMP WITH TIME ZONE,
    document_storage_path TEXT,
    ocr_text_available BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key constraint for uploadedByUserId
    CONSTRAINT fk_documents_users
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL
);

--;;

-- Junction Table: user_roles (FK to users and roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_roles
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON DELETE CASCADE,
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);

--;;

-- Table: document_type_summary_metadata_config (FK to metadata_attribute_definitions)
CREATE TABLE IF NOT EXISTS document_type_summary_metadata_config (
    config_id UUID PRIMARY KEY,
    document_type VARCHAR(50),
    attribute_definition_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key constraint for attribute_definition_id
    CONSTRAINT fk_document_type_summary_metadata_config_metadata_attribute_definitions
        FOREIGN KEY (attribute_definition_id)
        REFERENCES metadata_attribute_definitions(attribute_definition_id)
        ON DELETE CASCADE, -- Assuming cascading delete is appropriate here
    -- Unique constraint to prevent duplicate configurations
    CONSTRAINT unique_doc_type_attr_def UNIQUE (document_type, attribute_definition_id)
);

--;;

-- Table: document_metadata_values (FK to documents and metadata_attribute_definitions)
CREATE TABLE IF NOT EXISTS document_metadata_values (
    document_metadata_value_id UUID PRIMARY KEY,
    document_id UUID,
    attribute_definition_id UUID,
    attribute_value TEXT,
    value_data_type VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_metadata_values_documents
        FOREIGN KEY (document_id)
        REFERENCES documents(document_id)
        ON DELETE CASCADE, -- Cascading delete when a document is deleted
    CONSTRAINT fk_document_metadata_values_metadata_attribute_definitions
        FOREIGN KEY (attribute_definition_id)
        REFERENCES metadata_attribute_definitions(attribute_definition_id)
        ON DELETE CASCADE
);

--;;

-- Specialized Table: file_share_ingestion_sources (FK to document_ingestion_sources)
CREATE TABLE IF NOT EXISTS file_share_ingestion_sources (
    ingestion_source_id UUID PRIMARY KEY,
    file_share_type VARCHAR(10),
    file_share_path TEXT,
    file_share_username VARCHAR(100),
    file_share_password VARCHAR(255),
    CONSTRAINT fk_file_share_ingestion_sources_base
        FOREIGN KEY (ingestion_source_id)
        REFERENCES document_ingestion_sources(ingestion_source_id)
        ON DELETE CASCADE
);

--;;

-- Specialized Table: email_inbox_ingestion_sources (FK to document_ingestion_sources)
CREATE TABLE IF NOT EXISTS email_inbox_ingestion_sources (
    ingestion_source_id UUID PRIMARY KEY,
    email_protocol VARCHAR(10),
    email_server_address VARCHAR(255),
    email_server_port INTEGER,
    email_username VARCHAR(100),
    email_password VARCHAR(255),
    email_polling_interval_sec INTEGER,
    CONSTRAINT fk_email_inbox_ingestion_sources_base
        FOREIGN KEY (ingestion_source_id)
        REFERENCES document_ingestion_sources(ingestion_source_id)
        ON DELETE CASCADE
);

--;;

-- Specialized Table: document_type_subscriptions (FK to user_subscriptions)
CREATE TABLE IF NOT EXISTS document_type_subscriptions (
    user_subscription_id UUID PRIMARY KEY,
    document_type_criteria VARCHAR(50),
    CONSTRAINT fk_document_type_subscriptions_base
        FOREIGN KEY (user_subscription_id)
        REFERENCES user_subscriptions(user_subscription_id)
        ON DELETE CASCADE
);

--;;

-- Specialized Table: metadata_attribute_subscriptions (FK to user_subscriptions and metadata_attribute_definitions)
CREATE TABLE IF NOT EXISTS metadata_attribute_subscriptions (
    user_subscription_id UUID PRIMARY KEY,
    attribute_definition_id_criteria UUID,
    attribute_value_criteria TEXT,
    CONSTRAINT fk_metadata_attribute_subscriptions_base
        FOREIGN KEY (user_subscription_id)
        REFERENCES user_subscriptions(user_subscription_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_metadata_attribute_subscriptions_metadata_attribute_definitions
        FOREIGN KEY (attribute_definition_id_criteria)
        REFERENCES metadata_attribute_definitions(attribute_definition_id)
        ON DELETE SET NULL
);

--;

-- TEST DATA

-- Insert into users table (Assuming these UUIDs are valid as they didn't cause error)
INSERT INTO users (user_id, username, hashed_password, first_name, last_name, email, is_active) VALUES
('a1b2c3d4-e5f6-4789-90ab-cdef01234567'::uuid, 'admin_user', '$2a$10$рь./довр.ExampleHashedPassword1', 'Admin', 'User', 'admin.user@example.com', TRUE),
('b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, 'end_user1', '$2a$10$yt.ExampleHashedPassword2', ' সাধারণ ', 'ব্যবহারকারী', 'user1@example.com', TRUE),
('c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'doc_manager', '$2a$10$yt.ExampleHashedPassword3', 'Document', 'Manager', 'doc.manager@example.com', TRUE);

--;;

-- Insert into roles table (Corrected UUIDs)
INSERT INTO roles (role_id, role_name, role_description, is_system_role) VALUES
('44940e0a-9b4a-49a3-9d3a-8f192c9a7b5d'::uuid, 'Administrator', 'System administrator with full access.', TRUE),
('876a8e5d-b3a7-4a8e-b8c9-9c3d7e8f0a1b'::uuid, 'EndUser', 'Standard end-user with limited access.', FALSE),
('3c5d6e7f-8a9b-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'DocumentManager', 'Role for managing documents and metadata.', FALSE);

--;;

-- Insert into metadata_attribute_definitions table (Corrected UUIDs)
INSERT INTO metadata_attribute_definitions (attribute_definition_id, attribute_name, attribute_description, attribute_data_type, is_system_attribute, validation_rules, user_interface_hint) VALUES
('9e4f8a9b-0c1d-4e2f-8a9b-0c1d2e3f4a5b'::uuid, 'invoiceNumber', 'Invoice Number', 'STRING', TRUE, '{"maxLength": 50, "pattern": "^INV-\\d+-\\d+$"}', 'text-field'),
('f0a1b2c3-d4e5-4f8a-9b0c-1d2e3f4a5b6c'::uuid, 'contractDate', 'Contract Date', 'DATE', TRUE, '{"format": "YYYY-MM-DD"}', 'date-picker'),
('1b2c3d4e-f5a6-4789-90ab-cdef01234567'::uuid, 'customerName', 'Customer Name', 'STRING', FALSE, '{"maxLength": 100}', 'text-field'),
('5c6d7e8f-9a0b-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'department', 'Department', 'STRING', FALSE, '{"enum": ["Sales", "Marketing", "Legal", "Finance"]}', 'dropdown'),
('a9b0c1d2-e3f4-4a5b-8c9d-e0f1a2b3c4d5'::uuid, 'contractId', 'Contract ID', 'STRING', TRUE, '{"maxLength": 50, "pattern": "^CONTRACT-\\d+$"}', 'text-field');

--;;

-- Insert into document_ingestion_sources table (Corrected UUIDs)
INSERT INTO document_ingestion_sources (ingestion_source_id, source_type, source_name, description, is_active) VALUES
('2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f6a'::uuid, 'FILE_SHARE', 'Legal File Share', 'File share for legal documents', TRUE),
('6e7f8a9b-0c1d-4e2f-8a9b-0c1d2e3f4a5b'::uuid, 'EMAIL_INBOX', 'Invoices Inbox', 'Email inbox for incoming invoices', TRUE);

--;;

-- Insert into user_subscriptions table (linking to user 'b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5' - end_user1) (Corrected UUIDs)
INSERT INTO user_subscriptions (user_subscription_id, user_id, subscription_name, description, is_active, subscription_criteria_type, notification_channel) VALUES
('3a4b5c6d-7e8f-9a0b-4c1d-9e2f3a4b5c6d'::uuid, 'b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, 'Contract Notifications', 'Subscription for new contract documents', TRUE, 'DOCUMENT_TYPE', 'EMAIL'),
('7a8b9c0d-4e5f-4a2b-3c4d-5e6f7a8b9c0d'::uuid, 'c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'Legal Documnets', 'Subscription for legal documents', TRUE, 'DOCUMENT_TYPE', 'EMAIL');

--;;

-- Insert into documents table (linking to user 'a1b2c3d4-e5f6-4789-90ab-cdef01234567' - admin_user) (Assuming these UUIDs are valid)
INSERT INTO documents (document_id, document_type, file_type, title, uploaded_by_user_id, upload_date, document_storage_path, ocr_text_available) VALUES
('1a2b3c4d-e5f6-4789-90ab-cdef01234567'::uuid, 'contract', 'pdf', 'Contract Alpha Project - Acme Corp', 'a1b2c3d4-e5f6-4789-90ab-cdef01234567'::uuid, '2024-01-20T10:00:00Z', '/storage/contracts/alpha-acme.pdf', TRUE),
('b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, 'invoice', 'docx', 'Invoice INV-2024-123 for Beta Inc', 'a1b2c3d4-e5f6-4789-90ab-cdef01234567'::uuid, '2024-01-21T14:30:00Z', '/storage/invoices/inv-123.docx', FALSE),
('c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'report', 'pdf', 'Monthly Sales Report - January 2024', 'c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, '2024-01-22T09:00:00Z', '/storage/reports/sales-jan-2024.pdf', TRUE);

--;;

-- Insert into user_roles junction table (assigning roles to users) (Corrected UUIDs)
INSERT INTO user_roles (user_role_id, user_id, role_id) VALUES
('7b5d6e7f-8a9b-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'a1b2c3d4-e5f6-4789-90ab-cdef01234567'::uuid, '44940e0a-9b4a-49a3-9d3a-8f192c9a7b5d'::uuid), -- admin_user is Administrator
('8a9b0c1d-2e3f-4a5b-8c9d-e0f1a2b3c4d5'::uuid, 'b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, '876a8e5d-b3a7-4a8e-b8c9-9c3d7e8f0a1b'::uuid), -- end_user1 is EndUser
('9c1d2e3f-4a5b-8c9d-e0f1-2a3b4c5d6e7f'::uuid, 'c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, '3c5d6e7f-8a9b-4c1d-9e2f-3a4b5c6d7e8f'::uuid); -- doc_manager is DocumentManager

--;;

-- Insert into document_type_summary_metadata_config (configuring summary metadata for 'invoice' and 'contract') (Corrected UUIDs)
INSERT INTO document_type_summary_metadata_config (config_id, document_type, attribute_definition_id) VALUES
('a1b2c3d4-e5f6-4789-90ab-cdef01234567'::uuid, 'invoice', '9e4f8a9b-0c1d-4e2f-8a9b-0c1d2e3f4a5b'::uuid), -- invoiceNumber for invoice
('b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, 'invoice', '1b2c3d4e-f5a6-4789-90ab-cdef01234567'::uuid), -- customerName for invoice
('c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'contract', 'a9b0c1d2-e3f4-4a5b-8c9d-e0f1a2b3c4d5'::uuid), -- contractId for contract
('d2e3f4a5-b6c7-4d8e-9f0a-1b2c3d4e5f6a'::uuid, 'contract', '1b2c3d4e-f5a6-4789-90ab-cdef01234567'::uuid); -- customerName for contract

--;;

-- Insert into document_metadata_values (metadata for the documents) (Corrected UUIDs)
INSERT INTO document_metadata_values (document_metadata_value_id, document_id, attribute_definition_id, attribute_value, value_data_type) VALUES
('e9f0a1b2-c3d4-4e5f-8a9b-0c1d2e3f4a5b'::uuid, '1a2b3c4d-e5f6-4789-90ab-cdef01234567'::uuid, 'a9b0c1d2-e3f4-4a5b-8c9d-e0f1a2b3c4d5'::uuid, 'CONTRACT-2024-001', 'STRING'), -- contractId for Contract Alpha doc
('f6a7b8c9-d0e1-4f2a-b3c4-d5e6f7a8b9c0'::uuid, '1a2b3c4d-e5f6-4789-90ab-cdef01234567'::uuid, '1b2c3d4e-f5a6-4789-90ab-cdef01234567'::uuid, 'Acme Corporation', 'STRING'), -- customerName for Contract Alpha doc
('1a2b3c4d-e5f6-4789-90ab-cdef01234567'::uuid, 'b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, '9e4f8a9b-0c1d-4e2f-8a9b-0c1d2e3f4a5b'::uuid, 'INV-2024-123', 'STRING'), -- invoiceNumber for Invoice Beta Inc doc
('2b3c4d5e-f6a7-4b8c-9d0e-f1a2b3c4d5e6'::uuid, 'b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5'::uuid, '1b2c3d4e-f5a6-4789-90ab-cdef01234567'::uuid, 'Beta Inc', 'STRING'),     -- customerName for Invoice Beta Inc doc
('3c4d5e6f-7a8b-4c9d-e0f1-a2b3c4d5e6f7'::uuid, 'c5d6e7f8-a9b0-4c1d-9e2f-3a4b5c6d7e8f'::uuid, '5c6d7e8f-9a0b-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'Sales', 'STRING');      -- department for Sales Report doc

--;;

-- Insert into file_share_ingestion_sources (linking to ingestion_source 'ingest-src-1a2b3c4d-e5f6-4789-90ab-cdef01234567') (Corrected UUIDs)
INSERT INTO file_share_ingestion_sources (ingestion_source_id, file_share_type, file_share_path, file_share_username, file_share_password) VALUES
('2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f6a'::uuid, 'SMB', '//legal-share/documents', 'polydoc_service', '$2a$10$ExaExamplePasswordShare');

--;;

-- Insert into email_inbox_ingestion_sources (linking to ingestion_source 'ingest-src-b8c9d0e1-f2a3-4b5c-8d9e-f0a1b2c3d4e5') (Corrected UUIDs)
INSERT INTO email_inbox_ingestion_sources (ingestion_source_id, email_protocol, email_server_address, email_server_port, email_username, email_password, email_polling_interval_sec) VALUES
('6e7f8a9b-0c1d-4e2f-8a9b-0c1d2e3f4a5b'::uuid, 'IMAP', 'imap.example.com', 993, 'invoices@example.com', '$2a$10$InvcExamplePasswordEmail', 300);

--;;

-- Insert into document_type_subscriptions (linking to user_subscription 'user-sub-1a2b3c4d-e5f6-4789-90ab-cdef01234567') (Corrected UUIDs)
INSERT INTO document_type_subscriptions (user_subscription_id, document_type_criteria) VALUES
('3a4b5c6d-7e8f-9a0b-4c1d-9e2f3a4b5c6d'::uuid, 'contract');

--;;

-- Insert into metadata_attribute_subscriptions (linking to user_subscription 'user-sub-1a2b3c4d-e5f6-4789-90ab-cdef01234567' and attribute_definition 'attr-def-d2e3f4a5-b6c7-4d8e-9f0a-1b2c3d4e5f6a' - department) (Corrected UUIDs)
INSERT INTO metadata_attribute_subscriptions (user_subscription_id, attribute_definition_id_criteria, attribute_value_criteria) VALUES
('7a8b9c0d-4e5f-4a2b-3c4d-5e6f7a8b9c0d'::uuid, '5c6d7e8f-9a0b-4c1d-9e2f-3a4b5c6d7e8f'::uuid, 'Legal'); -- Subscription for documents with Department = Legal
