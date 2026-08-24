-- Removes test/demo accounts created during development.
-- Run against the LOCAL database only:
--   cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p1234 -D shimanagment < scripts\cleanup-test-accounts.sql'

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM email_verifications WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'verifyflow%@test.com' OR email = 'locktest@x.com');
DELETE FROM password_resets WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'verifyflow%@test.com' OR email = 'locktest@x.com');
DELETE FROM audit_logs WHERE actor_email LIKE 'verifyflow%@test.com' OR actor_email = 'locktest@x.com';
DELETE FROM users WHERE email LIKE 'verifyflow%@test.com' OR email = 'locktest@x.com';

DELETE FROM contact_messages WHERE email LIKE 'verifyflow%' OR full_name = 'BOOPATHI s';

SET FOREIGN_KEY_CHECKS = 1;

SELECT email, active, created_at FROM users ORDER BY id;
