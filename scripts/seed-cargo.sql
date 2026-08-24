INSERT INTO cargo (name, category_id, description, quantity, unit, origin_country, destination_country, loading_port_id, destination_port_id, loading_date, estimated_arrival, indicative_price, currency, status, created_at, updated_at)
VALUES
('Iron Ore Fines Fe 62%', 12, 'Hematite iron ore fines, Fe content 62%, ready at berth.', 50000.0000, 'MT', 'India', 'China', 1, NULL, '2026-09-05', '2026-09-28', 78.5000, 'USD', 'AVAILABLE', NOW(), NOW()),
('Portland Slag Cement', 4, 'OPC-grade slag cement in 50kg bags or break-bulk.', 12000.0000, 'MT', 'India', 'UAE', 3, NULL, '2026-09-12', '2026-10-02', 41.0000, 'USD', 'AVAILABLE', NOW(), NOW()),
('Basmati Rice 1121 Steam', 7, 'Premium 1121 steam basmati, containerized lots available.', 800.0000, 'MT', 'India', 'Saudi Arabia', 5, NULL, '2026-09-18', '2026-10-14', 920.0000, 'USD', 'AVAILABLE', NOW(), NOW());
SELECT id, name, status FROM cargo;
