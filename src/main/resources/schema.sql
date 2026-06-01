CREATE EXTENSION IF NOT EXISTS vector@@

DO $$
BEGIN
    CREATE TYPE file_type_enum AS ENUM ('SOURCE', 'INFRA', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE analysis_status_enum AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE severity_enum AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE reference_type_enum AS ENUM ('OWASP', 'CWE', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@
