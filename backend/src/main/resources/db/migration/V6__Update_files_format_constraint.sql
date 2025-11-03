-- Update files format check constraint to include new C format

-- First, drop the existing constraint if it exists
ALTER TABLE files DROP CONSTRAINT IF EXISTS files_format_check;

-- Add the updated constraint with all current FileFormat enum values
ALTER TABLE files ADD CONSTRAINT files_format_check
CHECK (format IN (
    'FILE_NOT_FOUND',
    'PDF',
    'TEXT',
    'MARKDOWN',
    'C',
    'CPP_14',
    'CPP_17',
    'JAVA_17',
    'PYTHON',
    'JSON',
    'XML',
    'IMAGE_PNG',
    'IMAGE_JPEG',
    'ZIP'
));