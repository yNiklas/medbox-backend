-- Create application database
CREATE DATABASE appdb;

-- Create application user with password
CREATE USER appuser WITH ENCRYPTED PASSWORD 'apppass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE appdb TO appuser;

