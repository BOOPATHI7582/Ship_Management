-- Remove client rows whose user account was deleted (user 18, 19).
-- No enquiries/invoices/shipments/etc. reference these clients.
DELETE c
FROM clients c
LEFT JOIN users u ON u.id = c.user_id
WHERE u.id IS NULL;