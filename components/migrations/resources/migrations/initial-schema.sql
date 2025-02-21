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
        ON DELETE CASCADE -- Cascade delete subscriptions if user is deleted
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
        ON DELETE SET NULL  -- Or ON DELETE NO ACTION / RESTRICT, depending on your requirement when a user is deleted
);

--;;

-- Junction Table: user_roles (FK to users and roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key constraints
    CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_roles
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON DELETE CASCADE,
    -- Unique constraint for user_id and role_id combination
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
    -- Foreign key constraint for document_id
    CONSTRAINT fk_document_metadata_values_documents
        FOREIGN KEY (document_id)
        REFERENCES documents(document_id)
        ON DELETE CASCADE, -- Cascading delete when a document is deleted
    -- Foreign key constraint for attribute_definition_id
    CONSTRAINT fk_document_metadata_values_metadata_attribute_definitions
        FOREIGN KEY (attribute_definition_id)
        REFERENCES metadata_attribute_definitions(attribute_definition_id)
        ON DELETE CASCADE  -- Cascading delete if an attribute definition is deleted (consider if this is appropriate)
);

--;;

-- Specialized Table: file_share_ingestion_sources (FK to document_ingestion_sources)
CREATE TABLE IF NOT EXISTS file_share_ingestion_sources (
    ingestion_source_id UUID PRIMARY KEY,
    file_share_type VARCHAR(10),
    file_share_path TEXT,
    file_share_username VARCHAR(100),
    file_share_password VARCHAR(255),
    -- One-to-one relationship with document_ingestion_sources
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
    -- One-to-one relationship with document_ingestion_sources
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
    -- One-to-one relationship with user_subscriptions
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
    -- One-to-one relationship with user_subscriptions
    CONSTRAINT fk_metadata_attribute_subscriptions_base
        FOREIGN KEY (user_subscription_id)
        REFERENCES user_subscriptions(user_subscription_id)
        ON DELETE CASCADE,
    -- Foreign key to metadata_attribute_definitions
    CONSTRAINT fk_metadata_attribute_subscriptions_metadata_attribute_definitions
        FOREIGN KEY (attribute_definition_id_criteria)
        REFERENCES metadata_attribute_definitions(attribute_definition_id)
        ON DELETE SET NULL -- Or RESTRICT/NO ACTION, depending on desired behavior if attribute definition is deleted
);
