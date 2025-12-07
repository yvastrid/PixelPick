-- Script SQL para eliminar las columnas de verificación de email que ya no se usan
-- Ejecutar este script en tu base de datos PostgreSQL

-- Eliminar las columnas email_verification_token y email_verification_sent_at de la tabla users
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_token;
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_sent_at;

-- Verificar que las columnas fueron eliminadas
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

