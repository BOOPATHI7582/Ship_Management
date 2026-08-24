UPDATE cargo SET currency = 'INR', indicative_price = 6500.0000 WHERE id = 2;
UPDATE cargo SET currency = 'INR', indicative_price = 3400.0000 WHERE id = 3;
UPDATE cargo SET currency = 'INR', indicative_price = 76000.0000 WHERE id = 4;
SELECT id, name, indicative_price, currency FROM cargo WHERE status = 'AVAILABLE';
