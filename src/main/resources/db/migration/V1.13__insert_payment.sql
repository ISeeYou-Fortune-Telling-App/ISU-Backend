-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.15
-- Description: Insert initial data for booking payments (Jan 2025) - REVISED LOGIC
-- =============================================

-- Ghi chú Enum:
-- payment_method: 2 (Mặc định)
-- status: 1 (COMPLETED), 3 (REFUND)
-- payment_type: 0 (PAID_PACKAGE), 1 (RECEIVED_PACKAGE)
--
-- Logic tính toán Payout (RECEIVED_PACKAGE):
-- amount = package.price - package.service_fee_amount
--
-- Logic Thời gian:
-- Cả PAID và RECEIVED đều được tạo/cập nhật vào ngày booking hoàn thành (booking.updated_at)

INSERT INTO booking_payment (booking_payment_id, created_at, updated_at, payment_method, amount, status, payment_type, transaction_id, approval_url, failure_reason, extra_info, booking_id) VALUES

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0001 (Completed: 2025-01-04 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-01-04 14:00:00', '2025-01-04 14:00:00', 2, 299000.0, 1, 0, 'PAYID-JAN0001A', 'https://example.com/checkout/...?token=JAN0001', NULL, 'Payment for booking ...0001', '8a09da6c-0671-4f17-82d2-0c43342d0001'),
(gen_random_uuid(), '2025-01-04 14:00:00', '2025-01-04 14:00:00', 2, 269100.0, 1, 1, 'PAYOUT-JAN0001B', NULL, NULL, 'Payout for booking ...0001', '8a09da6c-0671-4f17-82d2-0c43342d0001'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0002 (Completed: 2025-01-04 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-01-04 16:30:00', '2025-01-04 16:30:00', 2, 279000.0, 1, 0, 'PAYID-JAN0002A', 'https://example.com/checkout/...?token=JAN0002', NULL, 'Payment for booking ...0002', '8a09da6c-0671-4f17-82d2-0c43342d0002'),
(gen_random_uuid(), '2025-01-04 16:30:00', '2025-01-04 16:30:00', 2, 251100.0, 1, 1, 'PAYOUT-JAN0002B', NULL, NULL, 'Payout for booking ...0002', '8a09da6c-0671-4f17-82d2-0c43342d0002'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0003 (Completed: 2025-01-05 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-01-05 11:00:00', '2025-01-05 11:00:00', 2, 249000.0, 1, 0, 'PAYID-JAN0003A', 'https://example.com/checkout/...?token=JAN0003', NULL, 'Payment for booking ...0003', '8a09da6c-0671-4f17-82d2-0c43342d0003'),
(gen_random_uuid(), '2025-01-05 11:00:00', '2025-01-05 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JAN0003B', NULL, NULL, 'Payout for booking ...0003', '8a09da6c-0671-4f17-82d2-0c43342d0003'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0005 (Completed: 2025-01-06 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-01-06 10:30:00', '2025-01-06 10:30:00', 2, 429000.0, 1, 0, 'PAYID-JAN0005A', 'https://example.com/checkout/...?token=JAN0005', NULL, 'Payment for booking ...0005', '8a09da6c-0671-4f17-82d2-0c43342d0005'),
(gen_random_uuid(), '2025-01-06 10:30:00', '2025-01-06 10:30:00', 2, 386100.0, 1, 1, 'PAYOUT-JAN0005B', NULL, NULL, 'Payout for booking ...0005', '8a09da6c-0671-4f17-82d2-0c43342d0005'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0006 (Completed: 2025-01-06 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-01-06 15:00:00', '2025-01-06 15:00:00', 2, 499000.0, 1, 0, 'PAYID-JAN0006A', 'https://example.com/checkout/...?token=JAN0006', NULL, 'Payment for booking ...0006', '8a09da6c-0671-4f17-82d2-0c43342d0006'),
(gen_random_uuid(), '2025-01-06 15:00:00', '2025-01-06 15:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JAN0006B', NULL, NULL, 'Payout for booking ...0006', '8a09da6c-0671-4f17-82d2-0c43342d0006'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0007 (Completed: 2025-01-07 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-01-07 11:00:00', '2025-01-07 11:00:00', 2, 199000.0, 1, 0, 'PAYID-JAN0007A', 'https://example.com/checkout/...?token=JAN0007', NULL, 'Payment for booking ...0007', '8a09da6c-0671-4f17-82d2-0c43342d0007'),
(gen_random_uuid(), '2025-01-07 11:00:00', '2025-01-07 11:00:00', 2, 179100.0, 1, 1, 'PAYOUT-JAN0007B', NULL, NULL, 'Payout for booking ...0007', '8a09da6c-0671-4f17-82d2-0c43342d0007'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0008 (Completed: 2025-01-07 | Pkg: ...0012 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-01-07 17:00:00', '2025-01-07 17:00:00', 2, 399000.0, 1, 0, 'PAYID-JAN0008A', 'https://example.com/checkout/...?token=JAN0008', NULL, 'Payment for booking ...0008', '8a09da6c-0671-4f17-82d2-0c43342d0008'),
(gen_random_uuid(), '2025-01-07 17:00:00', '2025-01-07 17:00:00', 2, 359100.0, 1, 1, 'PAYOUT-JAN0008B', NULL, NULL, 'Payout for booking ...0008', '8a09da6c-0671-4f17-82d2-0c43342d0008'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0009 (Completed: 2025-01-08 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-01-08 10:30:00', '2025-01-08 10:30:00', 2, 379000.0, 1, 0, 'PAYID-JAN0009A', 'https://example.com/checkout/...?token=JAN0009', NULL, 'Payment for booking ...0009', '8a09da6c-0671-4f17-82d2-0c43342d0009'),
(gen_random_uuid(), '2025-01-08 10:30:00', '2025-01-08 10:30:00', 2, 341100.0, 1, 1, 'PAYOUT-JAN0009B', NULL, NULL, 'Payout for booking ...0009', '8a09da6c-0671-4f17-82d2-0c43342d0009'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0010 (Completed: 2025-01-08 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-01-08 15:30:00', '2025-01-08 15:30:00', 2, 329000.0, 1, 0, 'PAYID-JAN0010A', 'https://example.com/checkout/...?token=JAN0010', NULL, 'Payment for booking ...0010', '8a09da6c-0671-4f17-82d2-0c43342d0010'),
(gen_random_uuid(), '2025-01-08 15:30:00', '2025-01-08 15:30:00', 2, 296100.0, 1, 1, 'PAYOUT-JAN0010B', NULL, NULL, 'Payout for booking ...0010', '8a09da6c-0671-4f17-82d2-0c43342d0010'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0011 (Completed: 2025-01-09 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-01-09 11:30:00', '2025-01-09 11:30:00', 2, 899000.0, 1, 0, 'PAYID-JAN0011A', 'https://example.com/checkout/...?token=JAN0011', NULL, 'Payment for booking ...0011', '8a09da6c-0671-4f17-82d2-0c43342d0011'),
(gen_random_uuid(), '2025-01-09 11:30:00', '2025-01-09 11:30:00', 2, 809100.0, 1, 1, 'PAYOUT-JAN0011B', NULL, NULL, 'Payout for booking ...0011', '8a09da6c-0671-4f17-82d2-0c43342d0011'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0012 (Completed: 2025-01-09 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-01-09 17:00:00', '2025-01-09 17:00:00', 2, 229000.0, 1, 0, 'PAYID-JAN0012A', 'https://example.com/checkout/...?token=JAN0012', NULL, 'Payment for booking ...0012', '8a09da6c-0671-4f17-82d2-0c43342d0012'),
(gen_random_uuid(), '2025-01-09 17:00:00', '2025-01-09 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-JAN0012B', NULL, NULL, 'Payout for booking ...0012', '8a09da6c-0671-4f17-82d2-0c43342d0012'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0013 (Completed: 2025-01-10 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-01-10 11:00:00', '2025-01-10 11:00:00', 2, 319000.0, 1, 0, 'PAYID-JAN0013A', 'https://example.com/checkout/...?token=JAN0013', NULL, 'Payment for booking ...0013', '8a09da6c-0671-4f17-82d2-0c43342d0013'),
(gen_random_uuid(), '2025-01-10 11:00:00', '2025-01-10 11:00:00', 2, 287100.0, 1, 1, 'PAYOUT-JAN0013B', NULL, NULL, 'Payout for booking ...0013', '8a09da6c-0671-4f17-82d2-0c43342d0013'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0014 (Completed: 2025-01-10 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-01-10 14:00:00', '2025-01-10 14:00:00', 2, 149000.0, 1, 0, 'PAYID-JAN0014A', 'https://example.com/checkout/...?token=JAN0014', NULL, 'Payment for booking ...0014', '8a09da6c-0671-4f17-82d2-0c43342d0014'),
(gen_random_uuid(), '2025-01-10 14:00:00', '2025-01-10 14:00:00', 2, 134100.0, 1, 1, 'PAYOUT-JAN0014B', NULL, NULL, 'Payout for booking ...0014', '8a09da6c-0671-4f17-82d2-0c43342d0014'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0015 (Completed: 2025-01-11 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-01-11 11:30:00', '2025-01-11 11:30:00', 2, 399000.0, 1, 0, 'PAYID-JAN0015A', 'https://example.com/checkout/...?token=JAN0015', NULL, 'Payment for booking ...0015', '8a09da6c-0671-4f17-82d2-0c43342d0015'),
(gen_random_uuid(), '2025-01-11 11:30:00', '2025-01-11 11:30:00', 2, 359100.0, 1, 1, 'PAYOUT-JAN0015B', NULL, NULL, 'Payout for booking ...0015', '8a09da6c-0671-4f17-82d2-0c43342d0015'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0016 (Completed: 2025-01-11 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-01-11 16:00:00', '2025-01-11 16:00:00', 2, 329000.0, 1, 0, 'PAYID-JAN0016A', 'https://example.com/checkout/...?token=JAN0016', NULL, 'Payment for booking ...0016', '8a09da6c-0671-4f17-82d2-0c43342d0016'),
(gen_random_uuid(), '2025-01-11 16:00:00', '2025-01-11 16:00:00', 2, 296100.0, 1, 1, 'PAYOUT-JAN0016B', NULL, NULL, 'Payout for booking ...0016', '8a09da6c-0671-4f17-82d2-0c43342d0016'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0017 (Completed: 2025-01-12 | Pkg: ...0013 | Price: 649000 | Payout: 584100)
(gen_random_uuid(), '2025-01-12 10:00:00', '2025-01-12 10:00:00', 2, 649000.0, 1, 0, 'PAYID-JAN0017A', 'https://example.com/checkout/...?token=JAN0017', NULL, 'Payment for booking ...0017', '8a09da6c-0671-4f17-82d2-0c43342d0017'),
(gen_random_uuid(), '2025-01-12 10:00:00', '2025-01-12 10:00:00', 2, 584100.0, 1, 1, 'PAYOUT-JAN0017B', NULL, NULL, 'Payout for booking ...0017', '8a09da6c-0671-4f17-82d2-0c43342d0017'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0018 (Completed: 2025-01-12 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-01-12 14:00:00', '2025-01-12 14:00:00', 2, 449000.0, 1, 0, 'PAYID-JAN0018A', 'https://example.com/checkout/...?token=JAN0018', NULL, 'Payment for booking ...0018', '8a09da6c-0671-4f17-82d2-0c43342d0018'),
(gen_random_uuid(), '2025-01-12 14:00:00', '2025-01-12 14:00:00', 2, 404100.0, 1, 1, 'PAYOUT-JAN0018B', NULL, NULL, 'Payout for booking ...0018', '8a09da6c-0671-4f17-82d2-0c43342d0018'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0019 (Completed: 2025-01-13 | Pkg: ...0014 | Price: 479000 | Payout: 431100)
(gen_random_uuid(), '2025-01-13 11:30:00', '2025-01-13 11:30:00', 2, 479000.0, 1, 0, 'PAYID-JAN0019A', 'https://example.com/checkout/...?token=JAN0019', NULL, 'Payment for booking ...0019', '8a09da6c-0671-4f17-82d2-0c43342d0019'),
(gen_random_uuid(), '2025-01-13 11:30:00', '2025-01-13 11:30:00', 2, 431100.0, 1, 1, 'PAYOUT-JAN0019B', NULL, NULL, 'Payout for booking ...0019', '8a09da6c-0671-4f17-82d2-0c43342d0019'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0021 (Completed: 2025-01-14 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-01-14 11:00:00', '2025-01-14 11:00:00', 2, 429000.0, 1, 0, 'PAYID-JAN0021A', 'https://example.com/checkout/...?token=JAN0021', NULL, 'Payment for booking ...0021', '8a09da6c-0671-4f17-82d2-0c43342d0021'),
(gen_random_uuid(), '2025-01-14 11:00:00', '2025-01-14 11:00:00', 2, 386100.0, 1, 1, 'PAYOUT-JAN0021B', NULL, NULL, 'Payout for booking ...0021', '8a09da6c-0671-4f17-82d2-0c43342d0021'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0022 (Completed: 2025-01-14 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-01-14 15:00:00', '2025-01-14 15:00:00', 2, 699000.0, 1, 0, 'PAYID-JAN0022A', 'https://example.com/checkout/...?token=JAN0022', NULL, 'Payment for booking ...0022', '8a09da6c-0671-4f17-82d2-0c43342d0022'),
(gen_random_uuid(), '2025-01-14 15:00:00', '2025-01-14 15:00:00', 2, 629100.0, 1, 1, 'PAYOUT-JAN0022B', NULL, NULL, 'Payout for booking ...0022', '8a09da6c-0671-4f17-82d2-0c43342d0022'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0023 (Completed: 2025-01-15 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-01-15 11:30:00', '2025-01-15 11:30:00', 2, 279000.0, 1, 0, 'PAYID-JAN0023A', 'https://example.com/checkout/...?token=JAN0023', NULL, 'Payment for booking ...0023', '8a09da6c-0671-4f17-82d2-0c43342d0023'),
(gen_random_uuid(), '2025-01-15 11:30:00', '2025-01-15 11:30:00', 2, 251100.0, 1, 1, 'PAYOUT-JAN0023B', NULL, NULL, 'Payout for booking ...0023', '8a09da6c-0671-4f17-82d2-0c43342d0023'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0024 (Completed: 2025-01-15 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-01-15 16:00:00', '2025-01-15 16:00:00', 2, 599000.0, 1, 0, 'PAYID-JAN0024A', 'https://example.com/checkout/...?token=JAN0024', NULL, 'Payment for booking ...0024', '8a09da6c-0671-4f17-82d2-0c43342d0024'),
(gen_random_uuid(), '2025-01-15 16:00:00', '2025-01-15 16:00:00', 2, 539100.0, 1, 1, 'PAYOUT-JAN0024B', NULL, NULL, 'Payout for booking ...0024', '8a09da6c-0671-4f17-82d2-0c43342d0024'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0025 (Completed: 2025-01-16 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-01-16 11:00:00', '2025-01-16 11:00:00', 2, 299000.0, 1, 0, 'PAYID-JAN0025A', 'https://example.com/checkout/...?token=JAN0025', NULL, 'Payment for booking ...0025', '8a09da6c-0671-4f17-82d2-0c43342d0025'),
(gen_random_uuid(), '2025-01-16 11:00:00', '2025-01-16 11:00:00', 2, 269100.0, 1, 1, 'PAYOUT-JAN0025B', NULL, NULL, 'Payout for booking ...0025', '8a09da6c-0671-4f17-82d2-0c43342d0025'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0026 (Completed: 2025-01-16 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-01-16 14:30:00', '2025-01-16 14:30:00', 2, 549000.0, 1, 0, 'PAYID-JAN0026A', 'https://example.com/checkout/...?token=JAN0026', NULL, 'Payment for booking ...0026', '8a09da6c-0671-4f17-82d2-0c43342d0026'),
(gen_random_uuid(), '2025-01-16 14:30:00', '2025-01-16 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-JAN0026B', NULL, NULL, 'Payout for booking ...0026', '8a09da6c-0671-4f17-82d2-0c43342d0026'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0027 (Completed: 2025-01-17 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-01-17 11:00:00', '2025-01-17 11:00:00', 2, 499000.0, 1, 0, 'PAYID-JAN0027A', 'https://example.com/checkout/...?token=JAN0027', NULL, 'Payment for booking ...0027', '8a09da6c-0671-4f17-82d2-0c43342d0027'),
(gen_random_uuid(), '2025-01-17 11:00:00', '2025-01-17 11:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JAN0027B', NULL, NULL, 'Payout for booking ...0027', '8a09da6c-0671-4f17-82d2-0c43342d0027'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0028 (Completed: 2025-01-17 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-01-17 16:30:00', '2025-01-17 16:30:00', 2, 799000.0, 1, 0, 'PAYID-JAN0028A', 'https://example.com/checkout/...?token=JAN0028', NULL, 'Payment for booking ...0028', '8a09da6c-0671-4f17-82d2-0c43342d0028'),
(gen_random_uuid(), '2025-01-17 16:30:00', '2025-01-17 16:30:00', 2, 719100.0, 1, 1, 'PAYOUT-JAN0028B', NULL, NULL, 'Payout for booking ...0028', '8a09da6c-0671-4f17-82d2-0c43342d0028'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0029 (Completed: 2025-01-18 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-01-18 11:00:00', '2025-01-18 11:00:00', 2, 269000.0, 1, 0, 'PAYID-JAN0029A', 'https://example.com/checkout/...?token=JAN0029', NULL, 'Payment for booking ...0029', '8a09da6c-0671-4f17-82d2-0c43342d0029'),
(gen_random_uuid(), '2025-01-18 11:00:00', '2025-01-18 11:00:00', 2, 242100.0, 1, 1, 'PAYOUT-JAN0029B', NULL, NULL, 'Payout for booking ...0029', '8a09da6c-0671-4f17-82d2-0c43342d0029'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0030 (Completed: 2025-01-18 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-01-18 14:30:00', '2025-01-18 14:30:00', 2, 199000.0, 1, 0, 'PAYID-JAN0030A', 'https://example.com/checkout/...?token=JAN0030', NULL, 'Payment for booking ...0030', '8a09da6c-0671-4f17-82d2-0c43342d0030'),
(gen_random_uuid(), '2025-01-18 14:30:00', '2025-01-18 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-JAN0030B', NULL, NULL, 'Payout for booking ...0030', '8a09da6c-0671-4f17-82d2-0c43342d0030'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0031 (Completed: 2025-01-19 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-01-19 10:00:00', '2025-01-19 10:00:00', 2, 339000.0, 1, 0, 'PAYID-JAN0031A', 'https://example.com/checkout/...?token=JAN0031', NULL, 'Payment for booking ...0031', '8a09da6c-0671-4f17-82d2-0c43342d0031'),
(gen_random_uuid(), '2025-01-19 10:00:00', '2025-01-19 10:00:00', 2, 305100.0, 1, 1, 'PAYOUT-JAN0031B', NULL, NULL, 'Payout for booking ...0031', '8a09da6c-0671-4f17-82d2-0c43342d0031'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0032 (Completed: 2025-01-19 | Pkg: ...0009 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-01-19 11:30:00', '2025-01-19 11:30:00', 2, 199000.0, 1, 0, 'PAYID-JAN0032A', 'https://example.com/checkout/...?token=JAN0032', NULL, 'Payment for booking ...0032', '8a09da6c-0671-4f17-82d2-0c43342d0032'),
(gen_random_uuid(), '2025-01-19 11:30:00', '2025-01-19 11:30:00', 2, 179100.0, 1, 1, 'PAYOUT-JAN0032B', NULL, NULL, 'Payout for booking ...0032', '8a09da6c-0671-4f17-82d2-0c43342d0032'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0033 (Completed: 2025-01-20 | Pkg: ...1003 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-01-20 14:00:00', '2025-01-20 14:00:00', 2, 1499000.0, 1, 0, 'PAYID-JAN0033A', 'https://example.com/checkout/...?token=JAN0033', NULL, 'Payment for booking ...0033', '8a09da6c-0671-4f17-82d2-0c43342d0033'),
(gen_random_uuid(), '2025-01-20 14:00:00', '2025-01-20 14:00:00', 2, 1349100.0, 1, 1, 'PAYOUT-JAN0033B', NULL, NULL, 'Payout for booking ...0033', '8a09da6c-0671-4f17-82d2-0c43342d0033'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0034 (Completed: 2025-01-20 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-01-20 17:00:00', '2025-01-20 17:00:00', 2, 249000.0, 1, 0, 'PAYID-JAN0034A', 'https://example.com/checkout/...?token=JAN0034', NULL, 'Payment for booking ...0034', '8a09da6c-0671-4f17-82d2-0c43342d0034'),
(gen_random_uuid(), '2025-01-20 17:00:00', '2025-01-20 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JAN0034B', NULL, NULL, 'Payout for booking ...0034', '8a09da6c-0671-4f17-82d2-0c43342d0034'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0035 (Completed: 2025-01-21 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-01-21 10:00:00', '2025-01-21 10:00:00', 2, 369000.0, 1, 0, 'PAYID-JAN0035A', 'https://example.com/checkout/...?token=JAN0035', NULL, 'Payment for booking ...0035', '8a09da6c-0671-4f17-82d2-0c43342d0035'),
(gen_random_uuid(), '2025-01-21 10:00:00', '2025-01-21 10:00:00', 2, 332100.0, 1, 1, 'PAYOUT-JAN0035B', NULL, NULL, 'Payout for booking ...0035', '8a09da6c-0671-4f17-82d2-0c43342d0035'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0036 (Completed: 2025-01-21 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-01-21 15:30:00', '2025-01-21 15:30:00', 2, 499000.0, 1, 0, 'PAYID-JAN0036A', 'https://example.com/checkout/...?token=JAN0036', NULL, 'Payment for booking ...0036', '8a09da6c-0671-4f17-82d2-0c43342d0036'),
(gen_random_uuid(), '2025-01-21 15:30:00', '2025-01-21 15:30:00', 2, 449100.0, 1, 1, 'PAYOUT-JAN0036B', NULL, NULL, 'Payout for booking ...0036', '8a09da6c-0671-4f17-82d2-0c43342d0036'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0037 (Completed: 2025-01-22 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-01-22 11:00:00', '2025-01-22 11:00:00', 2, 219000.0, 1, 0, 'PAYID-JAN0037A', 'https://example.com/checkout/...?token=JAN0037', NULL, 'Payment for booking ...0037', '8a09da6c-0671-4f17-82d2-0c43342d0037'),
(gen_random_uuid(), '2025-01-22 11:00:00', '2025-01-22 11:00:00', 2, 197100.0, 1, 1, 'PAYOUT-JAN0037B', NULL, NULL, 'Payout for booking ...0037', '8a09da6c-0671-4f17-82d2-0c43342d0037'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0038 (Completed: 2025-01-22 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-01-22 17:00:00', '2025-01-22 17:00:00', 2, 249000.0, 1, 0, 'PAYID-JAN0038A', 'https://example.com/checkout/...?token=JAN0038', NULL, 'Payment for booking ...0038', '8a09da6c-0671-4f17-82d2-0c43342d0038'),
(gen_random_uuid(), '2025-01-22 17:00:00', '2025-01-22 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JAN0038B', NULL, NULL, 'Payout for booking ...0038', '8a09da6c-0671-4f17-82d2-0c43342d0038'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0039 (Completed: 2025-01-23 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-01-23 11:00:00', '2025-01-23 11:00:00', 2, 299000.0, 1, 0, 'PAYID-JAN0039A', 'https://example.com/checkout/...?token=JAN0039', NULL, 'Payment for booking ...0039', '8a09da6c-0671-4f17-82d2-0c43342d0039'),
(gen_random_uuid(), '2025-01-23 11:00:00', '2025-01-23 11:00:00', 2, 269100.0, 1, 1, 'PAYOUT-JAN0039B', NULL, NULL, 'Payout for booking ...0039', '8a09da6c-0671-4f17-82d2-0c43342d0039'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0041 (Completed: 2025-01-24 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-01-24 11:00:00', '2025-01-24 11:00:00', 2, 249000.0, 1, 0, 'PAYID-JAN0040A', 'https://example.com/checkout/...?token=JAN0040', NULL, 'Payment for booking ...0041', '8a09da6c-0671-4f17-82d2-0c43342d0041'),
(gen_random_uuid(), '2025-01-24 11:00:00', '2025-01-24 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JAN0040B', NULL, NULL, 'Payout for booking ...0041', '8a09da6c-0671-4f17-82d2-0c43342d0041'),

-- ---------------------------------
-- XỬ LÝ CÁC BOOKING BỊ HỦY (REFUNDS)
-- ---------------------------------

-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0004 (Canceled: 2025-01-03 | Pkg: ...0201 | Price: 369000)
(gen_random_uuid(), '2025-01-03 14:20:00', '2025-01-03 14:21:00', 2, 369000.0, 3, 0, 'PAYID-JAN-CNL-01', 'https://example.com/checkout/...?token=JAN-CNL-01', NULL, 'Refund for booking ...0004', '8a09da6c-0671-4f17-82d2-0c43342d0004'),

-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0020 (Canceled: 2025-01-11 | Pkg: ...0402 | Price: 219000)
(gen_random_uuid(), '2025-01-11 14:30:00', '2025-01-11 14:31:00', 2, 219000.0, 3, 0, 'PAYID-JAN-CNL-02', 'https://example.com/checkout/...?token=JAN-CNL-02', NULL, 'Refund for booking ...0020', '8a09da6c-0671-4f17-82d2-0c43342d0020'),

-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0040 (Canceled: 2025-01-21 | Pkg: ...0005 | Price: 279000)
(gen_random_uuid(), '2025-01-21 11:00:00', '2025-01-21 11:01:00', 2, 279000.0, 3, 0, 'PAYID-JAN-CNL-03', 'https://example.com/checkout/...?token=JAN-CNL-03', NULL, 'Refund for booking ...0040', '8a09da6c-0671-4f17-82d2-0c43342d0040'),

-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0056 (Canceled: 2025-01-29 | Pkg: ...0301 | Price: 429000)
(gen_random_uuid(), '2025-01-29 11:00:00', '2025-01-29 11:01:00', 2, 429000.0, 3, 0, 'PAYID-JAN-CNL-04', 'https://example.com/checkout/...?token=JAN-CNL-04', NULL, 'Refund for booking ...0056', '8a09da6c-0671-4f17-82d2-0c43342d0056'),

-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0080 (Canceled: 2025-01-21 | Pkg: ...0003 | Price: 399000)
(gen_random_uuid(), '2025-01-21 10:00:00', '2025-01-21 10:01:00', 2, 399000.0, 3, 0, 'PAYID-JAN-CNL-05', 'https://example.com/checkout/...?token=JAN-CNL-05', NULL, 'Refund for booking ...0080', '8a09da6c-0671-4f17-82d2-0c43342d0080');


-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.16
-- Description: Insert initial data for booking payments (Feb & Mar 2025)
-- =============================================

-- Ghi chú Enum:
-- payment_method: 2 (Mặc định)
-- status: 1 (COMPLETED), 3 (REFUND)
-- payment_type: 0 (PAID_PACKAGE), 1 (RECEIVED_PACKAGE)
-- Logic Payout: amount = package.price - package.service_fee_amount

INSERT INTO booking_payment (booking_payment_id, created_at, updated_at, payment_method, amount, status, payment_type, transaction_id, approval_url, failure_reason, extra_info, booking_id) VALUES

-- =============================================
-- BOOKING PAYMENTS - FEB 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0092 (Completed: 2025-02-03 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-02-03 11:00:00', '2025-02-03 11:00:00', 2, 329000.0, 1, 0, 'PAYID-FEB0001A', 'https://example.com/checkout/...?token=FEB0001', NULL, 'Payment for booking ...0092', '8a09da6c-0671-4f17-82d2-0c43342d0092'),
(gen_random_uuid(), '2025-02-03 11:00:00', '2025-02-03 11:00:00', 2, 296100.0, 1, 1, 'PAYOUT-FEB0001B', NULL, NULL, 'Payout for booking ...0092', '8a09da6c-0671-4f17-82d2-0c43342d0092'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0093 (Completed: 2025-02-03 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-02-03 14:30:00', '2025-02-03 14:30:00', 2, 249000.0, 1, 0, 'PAYID-FEB0002A', 'https://example.com/checkout/...?token=FEB0002', NULL, 'Payment for booking ...0093', '8a09da6c-0671-4f17-82d2-0c43342d0093'),
(gen_random_uuid(), '2025-02-03 14:30:00', '2025-02-03 14:30:00', 2, 224100.0, 1, 1, 'PAYOUT-FEB0002B', NULL, NULL, 'Payout for booking ...0093', '8a09da6c-0671-4f17-82d2-0c43342d0093'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0094 (Completed: 2025-02-03 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-02-03 16:30:00', '2025-02-03 16:30:00', 2, 369000.0, 1, 0, 'PAYID-FEB0003A', 'https://example.com/checkout/...?token=FEB0003', NULL, 'Payment for booking ...0094', '8a09da6c-0671-4f17-82d2-0c43342d0094'),
(gen_random_uuid(), '2025-02-03 16:30:00', '2025-02-03 16:30:00', 2, 332100.0, 1, 1, 'PAYOUT-FEB0003B', NULL, NULL, 'Payout for booking ...0094', '8a09da6c-0671-4f17-82d2-0c43342d0094'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0096 (Completed: 2025-02-04 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-02-04 14:00:00', '2025-02-04 14:00:00', 2, 199000.0, 1, 0, 'PAYID-FEB0004A', 'https://example.com/checkout/...?token=FEB0004', NULL, 'Payment for booking ...0096', '8a09da6c-0671-4f17-82d2-0c43342d0096'),
(gen_random_uuid(), '2025-02-04 14:00:00', '2025-02-04 14:00:00', 2, 179100.0, 1, 1, 'PAYOUT-FEB0004B', NULL, NULL, 'Payout for booking ...0096', '8a09da6c-0671-4f17-82d2-0c43342d0096'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0097 (Completed: 2025-02-04 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-02-04 17:00:00', '2025-02-04 17:00:00', 2, 379000.0, 1, 0, 'PAYID-FEB0005A', 'https://example.com/checkout/...?token=FEB0005', NULL, 'Payment for booking ...0097', '8a09da6c-0671-4f17-82d2-0c43342d0097'),
(gen_random_uuid(), '2025-02-04 17:00:00', '2025-02-04 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-FEB0005B', NULL, NULL, 'Payout for booking ...0097', '8a09da6c-0671-4f17-82d2-0c43342d0097'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0098 (Completed: 2025-02-05 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-02-05 11:30:00', '2025-02-05 11:30:00', 2, 699000.0, 1, 0, 'PAYID-FEB0006A', 'https://example.com/checkout/...?token=FEB0006', NULL, 'Payment for booking ...0098', '8a09da6c-0671-4f17-82d2-0c43342d0098'),
(gen_random_uuid(), '2025-02-05 11:30:00', '2025-02-05 11:30:00', 2, 629100.0, 1, 1, 'PAYOUT-FEB0006B', NULL, NULL, 'Payout for booking ...0098', '8a09da6c-0671-4f17-82d2-0c43342d0098'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0099 (Completed: 2025-02-05 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-02-05 14:30:00', '2025-02-05 14:30:00', 2, 149000.0, 1, 0, 'PAYID-FEB0007A', 'https://example.com/checkout/...?token=FEB0007', NULL, 'Payment for booking ...0099', '8a09da6c-0671-4f17-82d2-0c43342d0099'),
(gen_random_uuid(), '2025-02-05 14:30:00', '2025-02-05 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-FEB0007B', NULL, NULL, 'Payout for booking ...0099', '8a09da6c-0671-4f17-82d2-0c43342d0099'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0100 (Completed: 2025-02-05 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-02-05 17:00:00', '2025-02-05 17:00:00', 2, 449000.0, 1, 0, 'PAYID-FEB0008A', 'https://example.com/checkout/...?token=FEB0008', NULL, 'Payment for booking ...0100', '8a09da6c-0671-4f17-82d2-0c43342d0100'),
(gen_random_uuid(), '2025-02-05 17:00:00', '2025-02-05 17:00:00', 2, 404100.0, 1, 1, 'PAYOUT-FEB0008B', NULL, NULL, 'Payout for booking ...0100', '8a09da6c-0671-4f17-82d2-0c43342d0100'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0101 (Completed: 2025-02-06 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-02-06 10:30:00', '2025-02-06 10:30:00', 2, 499000.0, 1, 0, 'PAYID-FEB0009A', 'https://example.com/checkout/...?token=FEB0009', NULL, 'Payment for booking ...0101', '8a09da6c-0671-4f17-82d2-0c43342d0101'),
(gen_random_uuid(), '2025-02-06 10:30:00', '2025-02-06 10:30:00', 2, 449100.0, 1, 1, 'PAYOUT-FEB0009B', NULL, NULL, 'Payout for booking ...0101', '8a09da6c-0671-4f17-82d2-0c43342d0101'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0102 (Completed: 2025-02-06 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-02-06 14:30:00', '2025-02-06 14:30:00', 2, 549000.0, 1, 0, 'PAYID-FEB0010A', 'https://example.com/checkout/...?token=FEB0010', NULL, 'Payment for booking ...0102', '8a09da6c-0671-4f17-82d2-0c43342d0102'),
(gen_random_uuid(), '2025-02-06 14:30:00', '2025-02-06 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-FEB0010B', NULL, NULL, 'Payout for booking ...0102', '8a09da6c-0671-4f17-82d2-0c43342d0102'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0103 (Completed: 2025-02-06 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-02-06 17:00:00', '2025-02-06 17:00:00', 2, 299000.0, 1, 0, 'PAYID-FEB0011A', 'https://example.com/checkout/...?token=FEB0011', NULL, 'Payment for booking ...0103', '8a09da6c-0671-4f17-82d2-0c43342d0103'),
(gen_random_uuid(), '2025-02-06 17:00:00', '2025-02-06 17:00:00', 2, 269100.0, 1, 1, 'PAYOUT-FEB0011B', NULL, NULL, 'Payout for booking ...0103', '8a09da6c-0671-4f17-82d2-0c43342d0103'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0104 (Completed: 2025-02-07 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-02-07 11:30:00', '2025-02-07 11:30:00', 2, 499000.0, 1, 0, 'PAYID-FEB0012A', 'https://example.com/checkout/...?token=FEB0012', NULL, 'Payment for booking ...0104', '8a09da6c-0671-4f17-82d2-0c43342d0104'),
(gen_random_uuid(), '2025-02-07 11:30:00', '2025-02-07 11:30:00', 2, 449100.0, 1, 1, 'PAYOUT-FEB0012B', NULL, NULL, 'Payout for booking ...0104', '8a09da6c-0671-4f17-82d2-0c43342d0104'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0105 (Completed: 2025-02-07 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-02-07 14:30:00', '2025-02-07 14:30:00', 2, 399000.0, 1, 0, 'PAYID-FEB0013A', 'https://example.com/checkout/...?token=FEB0013', NULL, 'Payment for booking ...0105', '8a09da6c-0671-4f17-82d2-0c43342d0105'),
(gen_random_uuid(), '2025-02-07 14:30:00', '2025-02-07 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-FEB0013B', NULL, NULL, 'Payout for booking ...0105', '8a09da6c-0671-4f17-82d2-0c43342d0105'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0106 (Completed: 2025-02-07 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-02-07 17:00:00', '2025-02-07 17:00:00', 2, 229000.0, 1, 0, 'PAYID-FEB0014A', 'https://example.com/checkout/...?token=FEB0014', NULL, 'Payment for booking ...0106', '8a09da6c-0671-4f17-82d2-0c43342d0106'),
(gen_random_uuid(), '2025-02-07 17:00:00', '2025-02-07 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-FEB0014B', NULL, NULL, 'Payout for booking ...0106', '8a09da6c-0671-4f17-82d2-0c43342d0106'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0107 (Completed: 2025-02-08 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-02-08 10:30:00', '2025-02-08 10:30:00', 2, 279000.0, 1, 0, 'PAYID-FEB0015A', 'https://example.com/checkout/...?token=FEB0015', NULL, 'Payment for booking ...0107', '8a09da6c-0671-4f17-82d2-0c43342d0107'),
(gen_random_uuid(), '2025-02-08 10:30:00', '2025-02-08 10:30:00', 2, 251100.0, 1, 1, 'PAYOUT-FEB0015B', NULL, NULL, 'Payout for booking ...0107', '8a09da6c-0671-4f17-82d2-0c43342d0107'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0108 (Completed: 2025-02-08 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-02-08 14:30:00', '2025-02-08 14:30:00', 2, 219000.0, 1, 0, 'PAYID-FEB0016A', 'https://example.com/checkout/...?token=FEB0016', NULL, 'Payment for booking ...0108', '8a09da6c-0671-4f17-82d2-0c43342d0108'),
(gen_random_uuid(), '2025-02-08 14:30:00', '2025-02-08 14:30:00', 2, 197100.0, 1, 1, 'PAYOUT-FEB0016B', NULL, NULL, 'Payout for booking ...0108', '8a09da6c-0671-4f17-82d2-0c43342d0108'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0109 (Completed: 2025-02-08 | Pkg: ...0009 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-02-08 17:00:00', '2025-02-08 17:00:00', 2, 199000.0, 1, 0, 'PAYID-FEB0017A', 'https://example.com/checkout/...?token=FEB0017', NULL, 'Payment for booking ...0109', '8a09da6c-0671-4f17-82d2-0c43342d0109'),
(gen_random_uuid(), '2025-02-08 17:00:00', '2025-02-08 17:00:00', 2, 179100.0, 1, 1, 'PAYOUT-FEB0017B', NULL, NULL, 'Payout for booking ...0109', '8a09da6c-0671-4f17-82d2-0c43342d0109'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0111 (Completed: 2025-02-09 | Pkg: ...0010 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-02-09 14:00:00', '2025-02-09 14:00:00', 2, 549000.0, 1, 0, 'PAYID-FEB0018A', 'https://example.com/checkout/...?token=FEB0018', NULL, 'Payment for booking ...0111', '8a09da6c-0671-4f17-82d2-0c43342d0111'),
(gen_random_uuid(), '2025-02-09 14:00:00', '2025-02-09 14:00:00', 2, 494100.0, 1, 1, 'PAYOUT-FEB0018B', NULL, NULL, 'Payout for booking ...0111', '8a09da6c-0671-4f17-82d2-0c43342d0111'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0112 (Completed: 2025-02-09 | Pkg: ...1003 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-02-09 17:30:00', '2025-02-09 17:30:00', 2, 1499000.0, 1, 0, 'PAYID-FEB0019A', 'https://example.com/checkout/...?token=FEB0019', NULL, 'Payment for booking ...0112', '8a09da6c-0671-4f17-82d2-0c43342d0112'),
(gen_random_uuid(), '2025-02-09 17:30:00', '2025-02-09 17:30:00', 2, 1349100.0, 1, 1, 'PAYOUT-FEB0019B', NULL, NULL, 'Payout for booking ...0112', '8a09da6c-0671-4f17-82d2-0c43342d0112'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0113 (Completed: 2025-02-10 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-02-10 10:30:00', '2025-02-10 10:30:00', 2, 319000.0, 1, 0, 'PAYID-FEB0020A', 'https://example.com/checkout/...?token=FEB0020', NULL, 'Payment for booking ...0113', '8a09da6c-0671-4f17-82d2-0c43342d0113'),
(gen_random_uuid(), '2025-02-10 10:30:00', '2025-02-10 10:30:00', 2, 287100.0, 1, 1, 'PAYOUT-FEB0020B', NULL, NULL, 'Payout for booking ...0113', '8a09da6c-0671-4f17-82d2-0c43342d0113'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0114 (Completed: 2025-02-10 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-02-10 14:30:00', '2025-02-10 14:30:00', 2, 269000.0, 1, 0, 'PAYID-FEB0021A', 'https://example.com/checkout/...?token=FEB0021', NULL, 'Payment for booking ...0114', '8a09da6c-0671-4f17-82d2-0c43342d0114'),
(gen_random_uuid(), '2025-02-10 14:30:00', '2025-02-10 14:30:00', 2, 242100.0, 1, 1, 'PAYOUT-FEB0021B', NULL, NULL, 'Payout for booking ...0114', '8a09da6c-0671-4f17-82d2-0c43342d0114'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0115 (Completed: 2025-02-10 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-02-10 17:00:00', '2025-02-10 17:00:00', 2, 599000.0, 1, 0, 'PAYID-FEB0022A', 'https://example.com/checkout/...?token=FEB0022', NULL, 'Payment for booking ...0115', '8a09da6c-0671-4f17-82d2-0c43342d0115'),
(gen_random_uuid(), '2025-02-10 17:00:00', '2025-02-10 17:00:00', 2, 539100.0, 1, 1, 'PAYOUT-FEB0022B', NULL, NULL, 'Payout for booking ...0115', '8a09da6c-0671-4f17-82d2-0c43342d0115'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0116 (Completed: 2025-02-11 | Pkg: ...0012 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-02-11 11:00:00', '2025-02-11 11:00:00', 2, 399000.0, 1, 0, 'PAYID-FEB0023A', 'https://example.com/checkout/...?token=FEB0023', NULL, 'Payment for booking ...0116', '8a09da6c-0671-4f17-82d2-0c43342d0116'),
(gen_random_uuid(), '2025-02-11 11:00:00', '2025-02-11 11:00:00', 2, 359100.0, 1, 1, 'PAYOUT-FEB0023B', NULL, NULL, 'Payout for booking ...0116', '8a09da6c-0671-4f17-82d2-0c43342d0116'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0117 (Completed: 2025-02-11 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-02-11 14:30:00', '2025-02-11 14:30:00', 2, 329000.0, 1, 0, 'PAYID-FEB0024A', 'https://example.com/checkout/...?token=FEB0024', NULL, 'Payment for booking ...0117', '8a09da6c-0671-4f17-82d2-0c43342d0117'),
(gen_random_uuid(), '2025-02-11 14:30:00', '2025-02-11 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-FEB0024B', NULL, NULL, 'Payout for booking ...0117', '8a09da6c-0671-4f17-82d2-0c43342d0117'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0118 (Completed: 2025-02-11 | Pkg: ...0013 | Price: 649000 | Payout: 584100)
(gen_random_uuid(), '2025-02-11 17:00:00', '2025-02-11 17:00:00', 2, 649000.0, 1, 0, 'PAYID-FEB0025A', 'https://example.com/checkout/...?token=FEB0025', NULL, 'Payment for booking ...0118', '8a09da6c-0671-4f17-82d2-0c43342d0118'),
(gen_random_uuid(), '2025-02-11 17:00:00', '2025-02-11 17:00:00', 2, 584100.0, 1, 1, 'PAYOUT-FEB0025B', NULL, NULL, 'Payout for booking ...0118', '8a09da6c-0671-4f17-82d2-0c43342d0118'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0119 (Completed: 2025-02-12 | Pkg: ...0014 | Price: 479000 | Payout: 431100)
(gen_random_uuid(), '2025-02-12 10:00:00', '2025-02-12 10:00:00', 2, 479000.0, 1, 0, 'PAYID-FEB0026A', 'https://example.com/checkout/...?token=FEB0026', NULL, 'Payment for booking ...0119', '8a09da6c-0671-4f17-82d2-0c43342d0119'),
(gen_random_uuid(), '2025-02-12 10:00:00', '2025-02-12 10:00:00', 2, 431100.0, 1, 1, 'PAYOUT-FEB0026B', NULL, NULL, 'Payout for booking ...0119', '8a09da6c-0671-4f17-82d2-0c43342d0119'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0120 (Completed: 2025-02-12 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-02-12 14:30:00', '2025-02-12 14:30:00', 2, 369000.0, 1, 0, 'PAYID-FEB0027A', 'https://example.com/checkout/...?token=FEB0027', NULL, 'Payment for booking ...0120', '8a09da6c-0671-4f17-82d2-0c43342d0120'),
(gen_random_uuid(), '2025-02-12 14:30:00', '2025-02-12 14:30:00', 2, 332100.0, 1, 1, 'PAYOUT-FEB0027B', NULL, NULL, 'Payout for booking ...0120', '8a09da6c-0671-4f17-82d2-0c43342d0120'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0121 (Completed: 2025-02-12 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-02-12 17:00:00', '2025-02-12 17:00:00', 2, 299000.0, 1, 0, 'PAYID-FEB0028A', 'https://example.com/checkout/...?token=FEB0028', NULL, 'Payment for booking ...0121', '8a09da6c-0671-4f17-82d2-0c43342d0121'),
(gen_random_uuid(), '2025-02-12 17:00:00', '2025-02-12 17:00:00', 2, 269100.0, 1, 1, 'PAYOUT-FEB0028B', NULL, NULL, 'Payout for booking ...0121', '8a09da6c-0671-4f17-82d2-0c43342d0121'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0122 (Completed: 2025-02-13 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-02-13 11:00:00', '2025-02-13 11:00:00', 2, 279000.0, 1, 0, 'PAYID-FEB0029A', 'https://example.com/checkout/...?token=FEB0029', NULL, 'Payment for booking ...0122', '8a09da6c-0671-4f17-82d2-0c43342d0122'),
(gen_random_uuid(), '2025-02-13 11:00:00', '2025-02-13 11:00:00', 2, 251100.0, 1, 1, 'PAYOUT-FEB0029B', NULL, NULL, 'Payout for booking ...0122', '8a09da6c-0671-4f17-82d2-0c43342d0122'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0123 (Completed: 2025-02-13 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-02-13 14:30:00', '2025-02-13 14:30:00', 2, 329000.0, 1, 0, 'PAYID-FEB0030A', 'https://example.com/checkout/...?token=FEB0030', NULL, 'Payment for booking ...0123', '8a09da6c-0671-4f17-82d2-0c43342d0123'),
(gen_random_uuid(), '2025-02-13 14:30:00', '2025-02-13 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-FEB0030B', NULL, NULL, 'Payout for booking ...0123', '8a09da6c-0671-4f17-82d2-0c43342d0123'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0124 (Completed: 2025-02-13 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-02-13 17:00:00', '2025-02-13 17:00:00', 2, 429000.0, 1, 0, 'PAYID-FEB0031A', 'https://example.com/checkout/...?token=FEB0031', NULL, 'Payment for booking ...0124', '8a09da6c-0671-4f17-82d2-0c43342d0124'),
(gen_random_uuid(), '2025-02-13 17:00:00', '2025-02-13 17:00:00', 2, 386100.0, 1, 1, 'PAYOUT-FEB0031B', NULL, NULL, 'Payout for booking ...0124', '8a09da6c-0671-4f17-82d2-0c43342d0124'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0125 (Completed: 2025-02-14 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-02-14 10:00:00', '2025-02-14 10:00:00', 2, 199000.0, 1, 0, 'PAYID-FEB0032A', 'https://example.com/checkout/...?token=FEB0032', NULL, 'Payment for booking ...0125', '8a09da6c-0671-4f17-82d2-0c43342d0125'),
(gen_random_uuid(), '2025-02-14 10:00:00', '2025-02-14 10:00:00', 2, 179100.0, 1, 1, 'PAYOUT-FEB0032B', NULL, NULL, 'Payout for booking ...0125', '8a09da6c-0671-4f17-82d2-0c43342d0125'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0126 (Completed: 2025-02-14 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-02-14 14:30:00', '2025-02-14 14:30:00', 2, 379000.0, 1, 0, 'PAYID-FEB0033A', 'https://example.com/checkout/...?token=FEB0033', NULL, 'Payment for booking ...0126', '8a09da6c-0671-4f17-82d2-0c43342d0126'),
(gen_random_uuid(), '2025-02-14 14:30:00', '2025-02-14 14:30:00', 2, 341100.0, 1, 1, 'PAYOUT-FEB0033B', NULL, NULL, 'Payout for booking ...0126', '8a09da6c-0671-4f17-82d2-0c43342d0126'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0127 (Completed: 2025-02-14 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-02-14 17:00:00', '2025-02-14 17:00:00', 2, 699000.0, 1, 0, 'PAYID-FEB0034A', 'https://example.com/checkout/...?token=FEB0034', NULL, 'Payment for booking ...0127', '8a09da6c-0671-4f17-82d2-0c43342d0127'),
(gen_random_uuid(), '2025-02-14 17:00:00', '2025-02-14 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-FEB0034B', NULL, NULL, 'Payout for booking ...0127', '8a09da6c-0671-4f17-82d2-0c43342d0127'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0128 (Completed: 2025-02-15 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-02-15 11:00:00', '2025-02-15 11:00:00', 2, 149000.0, 1, 0, 'PAYID-FEB0035A', 'https://example.com/checkout/...?token=FEB0035', NULL, 'Payment for booking ...0128', '8a09da6c-0671-4f17-82d2-0c43342d0128'),
(gen_random_uuid(), '2025-02-15 11:00:00', '2025-02-15 11:00:00', 2, 134100.0, 1, 1, 'PAYOUT-FEB0035B', NULL, NULL, 'Payout for booking ...0128', '8a09da6c-0671-4f17-82d2-0c43342d0128'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0129 (Completed: 2025-02-15 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-02-15 14:30:00', '2025-02-15 14:30:00', 2, 449000.0, 1, 0, 'PAYID-FEB0036A', 'https://example.com/checkout/...?token=FEB0036', NULL, 'Payment for booking ...0129', '8a09da6c-0671-4f17-82d2-0c43342d0129'),
(gen_random_uuid(), '2025-02-15 14:30:00', '2025-02-15 14:30:00', 2, 404100.0, 1, 1, 'PAYOUT-FEB0036B', NULL, NULL, 'Payout for booking ...0129', '8a09da6c-0671-4f17-82d2-0c43342d0129'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0130 (Completed: 2025-02-15 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-02-15 17:00:00', '2025-02-15 17:00:00', 2, 499000.0, 1, 0, 'PAYID-FEB0037A', 'https://example.com/checkout/...?token=FEB0037', NULL, 'Payment for booking ...0130', '8a09da6c-0671-4f17-82d2-0c43342d0130'),
(gen_random_uuid(), '2025-02-15 17:00:00', '2025-02-15 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-FEB0037B', NULL, NULL, 'Payout for booking ...0130', '8a09da6c-0671-4f17-82d2-0c43342d0130'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0131 (Completed: 2025-02-16 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-02-16 10:00:00', '2025-02-16 10:00:00', 2, 549000.0, 1, 0, 'PAYID-FEB0038A', 'https://example.com/checkout/...?token=FEB0038', NULL, 'Payment for booking ...0131', '8a09da6c-0671-4f17-82d2-0c43342d0131'),
(gen_random_uuid(), '2025-02-16 10:00:00', '2025-02-16 10:00:00', 2, 494100.0, 1, 1, 'PAYOUT-FEB0038B', NULL, NULL, 'Payout for booking ...0131', '8a09da6c-0671-4f17-82d2-0c43342d0131'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0132 (Completed: 2025-02-16 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-02-16 14:30:00', '2025-02-16 14:30:00', 2, 299000.0, 1, 0, 'PAYID-FEB0039A', 'https://example.com/checkout/...?token=FEB0039', NULL, 'Payment for booking ...0132', '8a09da6c-0671-4f17-82d2-0c43342d0132'),
(gen_random_uuid(), '2025-02-16 14:30:00', '2025-02-16 14:30:00', 2, 269100.0, 1, 1, 'PAYOUT-FEB0039B', NULL, NULL, 'Payout for booking ...0132', '8a09da6c-0671-4f17-82d2-0c43342d0132'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0133 (Completed: 2025-02-16 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-02-16 17:00:00', '2025-02-16 17:00:00', 2, 499000.0, 1, 0, 'PAYID-FEB0040A', 'https://example.com/checkout/...?token=FEB0040', NULL, 'Payment for booking ...0133', '8a09da6c-0671-4f17-82d2-0c43342d0133'),
(gen_random_uuid(), '2025-02-16 17:00:00', '2025-02-16 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-FEB0040B', NULL, NULL, 'Payout for booking ...0133', '8a09da6c-0671-4f17-82d2-0c43342d0133'),

-- ---------------------------------
-- FEB 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0095 (Canceled: 2025-02-02 | Pkg: ...0301 | Price: 429000)
(gen_random_uuid(), '2025-02-02 09:15:00', '2025-02-02 09:16:00', 2, 429000.0, 3, 0, 'PAYID-FEB-CNL-01', 'https://example.com/checkout/...?token=FEB-CNL-01', NULL, 'Refund for booking ...0095', '8a09da6c-0671-4f17-82d2-0c43342d0095'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0110 (Canceled: 2025-02-07 | Pkg: ...0502 | Price: 339000)
(gen_random_uuid(), '2025-02-07 09:30:00', '2025-02-07 09:31:00', 2, 339000.0, 3, 0, 'PAYID-FEB-CNL-02', 'https://example.com/checkout/...?token=FEB-CNL-02', NULL, 'Refund for booking ...0110', '8a09da6c-0671-4f17-82d2-0c43342d0110'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0134 (Canceled: 2025-02-15 | Pkg: ...0003 | Price: 399000)
(gen_random_uuid(), '2025-02-15 09:30:00', '2025-02-15 09:31:00', 2, 399000.0, 3, 0, 'PAYID-FEB-CNL-03', 'https://example.com/checkout/...?token=FEB-CNL-03', NULL, 'Refund for booking ...0134', '8a09da6c-0671-4f17-82d2-0c43342d0134'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0158 (Canceled: 2025-02-23 | Pkg: ...0101 | Price: 249000)
(gen_random_uuid(), '2025-02-23 09:30:00', '2025-02-23 09:31:00', 2, 249000.0, 3, 0, 'PAYID-FEB-CNL-04', 'https://example.com/checkout/...?token=FEB-CNL-04', NULL, 'Refund for booking ...0158', '8a09da6c-0671-4f17-82d2-0c43342d0158'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0175 (Canceled: 2025-02-28 | Pkg: ...0009 | Price: 199000)
(gen_random_uuid(), '2025-02-28 14:30:00', '2025-02-28 14:31:00', 2, 199000.0, 3, 0, 'PAYID-FEB-CNL-05', 'https://example.com/checkout/...?token=FEB-CNL-05', NULL, 'Refund for booking ...0175', '8a09da6c-0671-4f17-82d2-0c43342d0175'),

-- =============================================
-- BOOKING PAYMENTS - MAR 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0182 (Completed: 2025-03-03 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-03-03 11:00:00', '2025-03-03 11:00:00', 2, 599000.0, 1, 0, 'PAYID-MAR0001A', 'https://example.com/checkout/...?token=MAR0001', NULL, 'Payment for booking ...0182', '8a09da6c-0671-4f17-82d2-0c43342d0182'),
(gen_random_uuid(), '2025-03-03 11:00:00', '2025-03-03 11:00:00', 2, 539100.0, 1, 1, 'PAYOUT-MAR0001B', NULL, NULL, 'Payout for booking ...0182', '8a09da6c-0671-4f17-82d2-0c43342d0182'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0183 (Completed: 2025-03-03 | Pkg: ...0012 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-03-03 14:30:00', '2025-03-03 14:30:00', 2, 399000.0, 1, 0, 'PAYID-MAR0002A', 'https://example.com/checkout/...?token=MAR0002', NULL, 'Payment for booking ...0183', '8a09da6c-0671-4f17-82d2-0c43342d0183'),
(gen_random_uuid(), '2025-03-03 14:30:00', '2025-03-03 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-MAR0002B', NULL, NULL, 'Payout for booking ...0183', '8a09da6c-0671-4f17-82d2-0c43342d0183'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0184 (Completed: 2025-03-03 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-03-03 17:00:00', '2025-03-03 17:00:00', 2, 329000.0, 1, 0, 'PAYID-MAR0003A', 'https://example.com/checkout/...?token=MAR0003', NULL, 'Payment for booking ...0184', '8a09da6c-0671-4f17-82d2-0c43342d0184'),
(gen_random_uuid(), '2025-03-03 17:00:00', '2025-03-03 17:00:00', 2, 296100.0, 1, 1, 'PAYOUT-MAR0003B', NULL, NULL, 'Payout for booking ...0184', '8a09da6c-0671-4f17-82d2-0c43342d0184'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0185 (Completed: 2025-03-04 | Pkg: ...0013 | Price: 649000 | Payout: 584100)
(gen_random_uuid(), '2025-03-04 10:00:00', '2025-03-04 10:00:00', 2, 649000.0, 1, 0, 'PAYID-MAR0004A', 'https://example.com/checkout/...?token=MAR0004', NULL, 'Payment for booking ...0185', '8a09da6c-0671-4f17-82d2-0c43342d0185'),
(gen_random_uuid(), '2025-03-04 10:00:00', '2025-03-04 10:00:00', 2, 584100.0, 1, 1, 'PAYOUT-MAR0004B', NULL, NULL, 'Payout for booking ...0185', '8a09da6c-0671-4f17-82d2-0c43342d0185'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0186 (Completed: 2025-03-04 | Pkg: ...0014 | Price: 479000 | Payout: 431100)
(gen_random_uuid(), '2025-03-04 14:30:00', '2025-03-04 14:30:00', 2, 479000.0, 1, 0, 'PAYID-MAR0005A', 'https://example.com/checkout/...?token=MAR0005', NULL, 'Payment for booking ...0186', '8a09da6c-0671-4f17-82d2-0c43342d0186'),
(gen_random_uuid(), '2025-03-04 14:30:00', '2025-03-04 14:30:00', 2, 431100.0, 1, 1, 'PAYOUT-MAR0005B', NULL, NULL, 'Payout for booking ...0186', '8a09da6c-0671-4f17-82d2-0c43342d0186'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0187 (Completed: 2025-03-04 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-03-04 17:00:00', '2025-03-04 17:00:00', 2, 369000.0, 1, 0, 'PAYID-MAR0006A', 'https://example.com/checkout/...?token=MAR0006', NULL, 'Payment for booking ...0187', '8a09da6c-0671-4f17-82d2-0c43342d0187'),
(gen_random_uuid(), '2025-03-04 17:00:00', '2025-03-04 17:00:00', 2, 332100.0, 1, 1, 'PAYOUT-MAR0006B', NULL, NULL, 'Payout for booking ...0187', '8a09da6c-0671-4f17-82d2-0c43342d0187'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0188 (Completed: 2025-03-05 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-03-05 11:00:00', '2025-03-05 11:00:00', 2, 299000.0, 1, 0, 'PAYID-MAR0007A', 'https://example.com/checkout/...?token=MAR0007', NULL, 'Payment for booking ...0188', '8a09da6c-0671-4f17-82d2-0c43342d0188'),
(gen_random_uuid(), '2025-03-05 11:00:00', '2025-03-05 11:00:00', 2, 269100.0, 1, 1, 'PAYOUT-MAR0007B', NULL, NULL, 'Payout for booking ...0188', '8a09da6c-0671-4f17-82d2-0c43342d0188'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0189 (Completed: 2025-03-05 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-03-05 14:30:00', '2025-03-05 14:30:00', 2, 279000.0, 1, 0, 'PAYID-MAR0008A', 'https://example.com/checkout/...?token=MAR0008', NULL, 'Payment for booking ...0189', '8a09da6c-0671-4f17-82d2-0c43342d0189'),
(gen_random_uuid(), '2025-03-05 14:30:00', '2025-03-05 14:30:00', 2, 251100.0, 1, 1, 'PAYOUT-MAR0008B', NULL, NULL, 'Payout for booking ...0189', '8a09da6c-0671-4f17-82d2-0c43342d0189'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0190 (Completed: 2025-03-05 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-03-05 17:00:00', '2025-03-05 17:00:00', 2, 329000.0, 1, 0, 'PAYID-MAR0009A', 'https://example.com/checkout/...?token=MAR0009', NULL, 'Payment for booking ...0190', '8a09da6c-0671-4f17-82d2-0c43342d0190'),
(gen_random_uuid(), '2025-03-05 17:00:00', '2025-03-05 17:00:00', 2, 296100.0, 1, 1, 'PAYOUT-MAR0009B', NULL, NULL, 'Payout for booking ...0190', '8a09da6c-0671-4f17-82d2-0c43342d0190'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0191 (Completed: 2025-03-06 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-03-06 10:00:00', '2025-03-06 10:00:00', 2, 429000.0, 1, 0, 'PAYID-MAR0010A', 'https://example.com/checkout/...?token=MAR0010', NULL, 'Payment for booking ...0191', '8a09da6c-0671-4f17-82d2-0c43342d0191'),
(gen_random_uuid(), '2025-03-06 10:00:00', '2025-03-06 10:00:00', 2, 386100.0, 1, 1, 'PAYOUT-MAR0010B', NULL, NULL, 'Payout for booking ...0191', '8a09da6c-0671-4f17-82d2-0c43342d0191'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0192 (Completed: 2025-03-06 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-03-06 14:30:00', '2025-03-06 14:30:00', 2, 199000.0, 1, 0, 'PAYID-MAR0011A', 'https://example.com/checkout/...?token=MAR0011', NULL, 'Payment for booking ...0192', '8a09da6c-0671-4f17-82d2-0c43342d0192'),
(gen_random_uuid(), '2025-03-06 14:30:00', '2025-03-06 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-MAR0011B', NULL, NULL, 'Payout for booking ...0192', '8a09da6c-0671-4f17-82d2-0c43342d0192'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0193 (Completed: 2025-03-06 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-03-06 17:00:00', '2025-03-06 17:00:00', 2, 379000.0, 1, 0, 'PAYID-MAR0012A', 'https://example.com/checkout/...?token=MAR0012', NULL, 'Payment for booking ...0193', '8a09da6c-0671-4f17-82d2-0c43342d0193'),
(gen_random_uuid(), '2025-03-06 17:00:00', '2025-03-06 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-MAR0012B', NULL, NULL, 'Payout for booking ...0193', '8a09da6c-0671-4f17-82d2-0c43342d0193'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0194 (Completed: 2025-03-07 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-03-07 11:00:00', '2025-03-07 11:00:00', 2, 699000.0, 1, 0, 'PAYID-MAR0013A', 'https://example.com/checkout/...?token=MAR0013', NULL, 'Payment for booking ...0194', '8a09da6c-0671-4f17-82d2-0c43342d0194'),
(gen_random_uuid(), '2025-03-07 11:00:00', '2025-03-07 11:00:00', 2, 629100.0, 1, 1, 'PAYOUT-MAR0013B', NULL, NULL, 'Payout for booking ...0194', '8a09da6c-0671-4f17-82d2-0c43342d0194'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0195 (Completed: 2025-03-07 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-03-07 14:30:00', '2025-03-07 14:30:00', 2, 149000.0, 1, 0, 'PAYID-MAR0014A', 'https://example.com/checkout/...?token=MAR0014', NULL, 'Payment for booking ...0195', '8a09da6c-0671-4f17-82d2-0c43342d0195'),
(gen_random_uuid(), '2025-03-07 14:30:00', '2025-03-07 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-MAR0014B', NULL, NULL, 'Payout for booking ...0195', '8a09da6c-0671-4f17-82d2-0c43342d0195'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0196 (Completed: 2025-03-07 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-03-07 17:00:00', '2025-03-07 17:00:00', 2, 269000.0, 1, 0, 'PAYID-MAR0015A', 'https://example.com/checkout/...?token=MAR0015', NULL, 'Payment for booking ...0196', '8a09da6c-0671-4f17-82d2-0c43342d0196'),
(gen_random_uuid(), '2025-03-07 17:00:00', '2025-03-07 17:00:00', 2, 242100.0, 1, 1, 'PAYOUT-MAR0015B', NULL, NULL, 'Payout for booking ...0196', '8a09da6c-0671-4f17-82d2-0c43342d0196'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0197 (Completed: 2025-03-08 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-03-08 10:00:00', '2025-03-08 10:00:00', 2, 499000.0, 1, 0, 'PAYID-MAR0016A', 'https://example.com/checkout/...?token=MAR0016', NULL, 'Payment for booking ...0197', '8a09da6c-0671-4f17-82d2-0c43342d0197'),
(gen_random_uuid(), '2025-03-08 10:00:00', '2025-03-08 10:00:00', 2, 449100.0, 1, 1, 'PAYOUT-MAR0016B', NULL, NULL, 'Payout for booking ...0197', '8a09da6c-0671-4f17-82d2-0c43342d0197'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0198 (Completed: 2025-03-08 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-03-08 14:30:00', '2025-03-08 14:30:00', 2, 549000.0, 1, 0, 'PAYID-MAR0017A', 'https://example.com/checkout/...?token=MAR0017', NULL, 'Payment for booking ...0198', '8a09da6c-0671-4f17-82d2-0c43342d0198'),
(gen_random_uuid(), '2025-03-08 14:30:00', '2025-03-08 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-MAR0017B', NULL, NULL, 'Payout for booking ...0198', '8a09da6c-0671-4f17-82d2-0c43342d0198'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0199 (Completed: 2025-03-08 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-03-08 17:00:00', '2025-03-08 17:00:00', 2, 899000.0, 1, 0, 'PAYID-MAR0018A', 'https://example.com/checkout/...?token=MAR0018', NULL, 'Payment for booking ...0199', '8a09da6c-0671-4f17-82d2-0c43342d0199'),
(gen_random_uuid(), '2025-03-08 17:00:00', '2025-03-08 17:00:00', 2, 809100.0, 1, 1, 'PAYOUT-MAR0018B', NULL, NULL, 'Payout for booking ...0199', '8a09da6c-0671-4f17-82d2-0c43342d0199'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0200 (Completed: 2025-03-09 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-03-09 11:00:00', '2025-03-09 11:00:00', 2, 499000.0, 1, 0, 'PAYID-MAR0019A', 'https://example.com/checkout/...?token=MAR0019', NULL, 'Payment for booking ...0200', '8a09da6c-0671-4f17-82d2-0c43342d0200'),
(gen_random_uuid(), '2025-03-09 11:00:00', '2025-03-09 11:00:00', 2, 449100.0, 1, 1, 'PAYOUT-MAR0019B', NULL, NULL, 'Payout for booking ...0200', '8a09da6c-0671-4f17-82d2-0c43342d0200'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0201 (Completed: 2025-03-09 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-03-09 14:30:00', '2025-03-09 14:30:00', 2, 399000.0, 1, 0, 'PAYID-MAR0020A', 'https://example.com/checkout/...?token=MAR0020', NULL, 'Payment for booking ...0201', '8a09da6c-0671-4f17-82d2-0c43342d0201'),
(gen_random_uuid(), '2025-03-09 14:30:00', '2025-03-09 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-MAR0020B', NULL, NULL, 'Payout for booking ...0201', '8a09da6c-0671-4f17-82d2-0c43342d0201'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0202 (Completed: 2025-03-09 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-03-09 17:00:00', '2025-03-09 17:00:00', 2, 229000.0, 1, 0, 'PAYID-MAR0021A', 'https://example.com/checkout/...?token=MAR0021', NULL, 'Payment for booking ...0202', '8a09da6c-0671-4f17-82d2-0c43342d0202'),
(gen_random_uuid(), '2025-03-09 17:00:00', '2025-03-09 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-MAR0021B', NULL, NULL, 'Payout for booking ...0202', '8a09da6c-0671-4f17-82d2-0c43342d0202'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0203 (Completed: 2025-03-10 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-03-10 10:00:00', '2025-03-10 10:00:00', 2, 279000.0, 1, 0, 'PAYID-MAR0022A', 'https://example.com/checkout/...?token=MAR0022', NULL, 'Payment for booking ...0203', '8a09da6c-0671-4f17-82d2-0c43342d0203'),
(gen_random_uuid(), '2025-03-10 10:00:00', '2025-03-10 10:00:00', 2, 251100.0, 1, 1, 'PAYOUT-MAR0022B', NULL, NULL, 'Payout for booking ...0203', '8a09da6c-0671-4f17-82d2-0c43342d0203'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0204 (Completed: 2025-03-10 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-03-10 14:30:00', '2025-03-10 14:30:00', 2, 219000.0, 1, 0, 'PAYID-MAR0023A', 'https://example.com/checkout/...?token=MAR0023', NULL, 'Payment for booking ...0204', '8a09da6c-0671-4f17-82d2-0c43342d0204'),
(gen_random_uuid(), '2025-03-10 14:30:00', '2025-03-10 14:30:00', 2, 197100.0, 1, 1, 'PAYOUT-MAR0023B', NULL, NULL, 'Payout for booking ...0204', '8a09da6c-0671-4f17-82d2-0c43342d0204'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0206 (Completed: 2025-03-11 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-03-11 11:00:00', '2025-03-11 11:00:00', 2, 249000.0, 1, 0, 'PAYID-MAR0024A', 'https://example.com/checkout/...?token=MAR0024', NULL, 'Payment for booking ...0206', '8a09da6c-0671-4f17-82d2-0c43342d0206'),
(gen_random_uuid(), '2025-03-11 11:00:00', '2025-03-11 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-MAR0024B', NULL, NULL, 'Payout for booking ...0206', '8a09da6c-0671-4f17-82d2-0c43342d0206'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0207 (Completed: 2025-03-11 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-03-11 14:30:00', '2025-03-11 14:30:00', 2, 339000.0, 1, 0, 'PAYID-MAR0025A', 'https://example.com/checkout/...?token=MAR0025', NULL, 'Payment for booking ...0207', '8a09da6c-0671-4f17-82d2-0c43342d0207'),
(gen_random_uuid(), '2025-03-11 14:30:00', '2025-03-11 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-MAR0025B', NULL, NULL, 'Payout for booking ...0207', '8a09da6c-0671-4f17-82d2-0c43342d0207'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0208 (Completed: 2025-03-11 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-03-11 17:00:00', '2025-03-11 17:00:00', 2, 799000.0, 1, 0, 'PAYID-MAR0026A', 'https://example.com/checkout/...?token=MAR0026', NULL, 'Payment for booking ...0208', '8a09da6c-0671-4f17-82d2-0c43342d0208'),
(gen_random_uuid(), '2025-03-11 17:00:00', '2025-03-11 17:00:00', 2, 719100.0, 1, 1, 'PAYOUT-MAR0026B', NULL, NULL, 'Payout for booking ...0208', '8a09da6c-0671-4f17-82d2-0c43342d0208'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0209 (Completed: 2025-03-12 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-03-12 10:00:00', '2025-03-12 10:00:00', 2, 269000.0, 1, 0, 'PAYID-MAR0027A', 'https://example.com/checkout/...?token=MAR0027', NULL, 'Payment for booking ...0209', '8a09da6c-0671-4f17-82d2-0c43342d0209'),
(gen_random_uuid(), '2025-03-12 10:00:00', '2025-03-12 10:00:00', 2, 242100.0, 1, 1, 'PAYOUT-MAR0027B', NULL, NULL, 'Payout for booking ...0209', '8a09da6c-0671-4f17-82d2-0c43342d0209'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0210 (Completed: 2025-03-12 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-03-12 14:30:00', '2025-03-12 14:30:00', 2, 599000.0, 1, 0, 'PAYID-MAR0028A', 'https://example.com/checkout/...?token=MAR0028', NULL, 'Payment for booking ...0210', '8a09da6c-0671-4f17-82d2-0c43342d0210'),
(gen_random_uuid(), '2025-03-12 14:30:00', '2025-03-12 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-MAR0028B', NULL, NULL, 'Payout for booking ...0210', '8a09da6c-0671-4f17-82d2-0c43342d0210'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0211 (Completed: 2025-03-12 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-03-12 17:00:00', '2025-03-12 17:00:00', 2, 899000.0, 1, 0, 'PAYID-MAR0029A', 'https://example.com/checkout/...?token=MAR0029', NULL, 'Payment for booking ...0211', '8a09da6c-0671-4f17-82d2-0c43342d0211'),
(gen_random_uuid(), '2025-03-12 17:00:00', '2025-03-12 17:00:00', 2, 809100.0, 1, 1, 'PAYOUT-MAR0029B', NULL, NULL, 'Payout for booking ...0211', '8a09da6c-0671-4f17-82d2-0c43342d0211'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0212 (Completed: 2025-03-13 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-03-13 11:00:00', '2025-03-13 11:00:00', 2, 249000.0, 1, 0, 'PAYID-MAR0030A', 'https://example.com/checkout/...?token=MAR0030', NULL, 'Payment for booking ...0212', '8a09da6c-0671-4f17-82d2-0c43342d0212'),
(gen_random_uuid(), '2025-03-13 11:00:00', '2025-03-13 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-MAR0030B', NULL, NULL, 'Payout for booking ...0212', '8a09da6c-0671-4f17-82d2-0c43342d0212'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0213 (Completed: 2025-03-13 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-03-13 14:30:00', '2025-03-13 14:30:00', 2, 319000.0, 1, 0, 'PAYID-MAR0031A', 'https://example.com/checkout/...?token=MAR0031', NULL, 'Payment for booking ...0213', '8a09da6c-0671-4f17-82d2-0c43342d0213'),
(gen_random_uuid(), '2025-03-13 14:30:00', '2025-03-13 14:30:00', 2, 287100.0, 1, 1, 'PAYOUT-MAR0031B', NULL, NULL, 'Payout for booking ...0213', '8a09da6c-0671-4f17-82d2-0c43342d0213'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0214 (Completed: 2025-03-13 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-03-13 17:00:00', '2025-03-13 17:00:00', 2, 329000.0, 1, 0, 'PAYID-MAR0032A', 'https://example.com/checkout/...?token=MAR0032', NULL, 'Payment for booking ...0214', '8a09da6c-0671-4f17-82d2-0c43342d0214'),
(gen_random_uuid(), '2025-03-13 17:00:00', '2025-03-13 17:00:00', 2, 296100.0, 1, 1, 'PAYOUT-MAR0032B', NULL, NULL, 'Payout for booking ...0214', '8a09da6c-0671-4f17-82d2-0c43342d0214'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0215 (Completed: 2025-03-14 | Pkg: ...0903 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-03-14 10:00:00', '2025-03-14 10:00:00', 2, 1499000.0, 1, 0, 'PAYID-MAR0033A', 'https://example.com/checkout/...?token=MAR0033', NULL, 'Payment for booking ...0215', '8a09da6c-0671-4f17-82d2-0c43342d0215'),
(gen_random_uuid(), '2025-03-14 10:00:00', '2025-03-14 10:00:00', 2, 1349100.0, 1, 1, 'PAYOUT-MAR0033B', NULL, NULL, 'Payout for booking ...0215', '8a09da6c-0671-4f17-82d2-0c43342d0215'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0216 (Completed: 2025-03-14 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-03-14 14:30:00', '2025-03-14 14:30:00', 2, 449000.0, 1, 0, 'PAYID-MAR0034A', 'https://example.com/checkout/...?token=MAR0034', NULL, 'Payment for booking ...0216', '8a09da6c-0671-4f17-82d2-0c43342d0216'),
(gen_random_uuid(), '2025-03-14 14:30:00', '2025-03-14 14:30:00', 2, 404100.0, 1, 1, 'PAYOUT-MAR0034B', NULL, NULL, 'Payout for booking ...0216', '8a09da6c-0671-4f17-82d2-0c43342d0216'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0217 (Completed: 2025-03-14 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-03-14 17:00:00', '2025-03-14 17:00:00', 2, 149000.0, 1, 0, 'PAYID-MAR0035A', 'https://example.com/checkout/...?token=MAR0035', NULL, 'Payment for booking ...0217', '8a09da6c-0671-4f17-82d2-0c43342d0217'),
(gen_random_uuid(), '2025-03-14 17:00:00', '2025-03-14 17:00:00', 2, 134100.0, 1, 1, 'PAYOUT-MAR0035B', NULL, NULL, 'Payout for booking ...0217', '8a09da6c-0671-4f17-82d2-0c43342d0217'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0218 (Completed: 2025-03-15 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-03-15 11:00:00', '2025-03-15 11:00:00', 2, 549000.0, 1, 0, 'PAYID-MAR0036A', 'https://example.com/checkout/...?token=MAR0036', NULL, 'Payment for booking ...0218', '8a09da6c-0671-4f17-82d2-0c43342d0218'),
(gen_random_uuid(), '2025-03-15 11:00:00', '2025-03-15 11:00:00', 2, 494100.0, 1, 1, 'PAYOUT-MAR0036B', NULL, NULL, 'Payout for booking ...0218', '8a09da6c-0671-4f17-82d2-0c43342d0218'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0219 (Completed: 2025-03-15 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-03-15 14:30:00', '2025-03-15 14:30:00', 2, 379000.0, 1, 0, 'PAYID-MAR0037A', 'https://example.com/checkout/...?token=MAR0037', NULL, 'Payment for booking ...0219', '8a09da6c-0671-4f17-82d2-0c43342d0219'),
(gen_random_uuid(), '2025-03-15 14:30:00', '2025-03-15 14:30:00', 2, 341100.0, 1, 1, 'PAYOUT-MAR0037B', NULL, NULL, 'Payout for booking ...0219', '8a09da6c-0671-4f17-82d2-0c43342d0219'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0220 (Completed: 2025-03-15 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-03-15 17:00:00', '2025-03-15 17:00:00', 2, 229000.0, 1, 0, 'PAYID-MAR0038A', 'https://example.com/checkout/...?token=MAR0038', NULL, 'Payment for booking ...0220', '8a09da6c-0671-4f17-82d2-0c43342d0220'),
(gen_random_uuid(), '2025-03-15 17:00:00', '2025-03-15 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-MAR0038B', NULL, NULL, 'Payout for booking ...0220', '8a09da6c-0671-4f17-82d2-0c43342d0220'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0221 (Completed: 2025-03-16 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-03-16 10:00:00', '2025-03-16 10:00:00', 2, 699000.0, 1, 0, 'PAYID-MAR0039A', 'https://example.com/checkout/...?token=MAR0039', NULL, 'Payment for booking ...0221', '8a09da6c-0671-4f17-82d2-0c43342d0221'),
(gen_random_uuid(), '2025-03-16 10:00:00', '2025-03-16 10:00:00', 2, 629100.0, 1, 1, 'PAYOUT-MAR0039B', NULL, NULL, 'Payout for booking ...0221', '8a09da6c-0671-4f17-82d2-0c43342d0221'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0222 (Completed: 2025-03-16 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-03-16 14:30:00', '2025-03-16 14:30:00', 2, 449000.0, 1, 0, 'PAYID-MAR0040A', 'https://example.com/checkout/...?token=MAR0040', NULL, 'Payment for booking ...0222', '8a09da6c-0671-4f17-82d2-0c43342d0222'),
(gen_random_uuid(), '2025-03-16 14:30:00', '2025-03-16 14:30:00', 2, 404100.0, 1, 1, 'PAYOUT-MAR0040B', NULL, NULL, 'Payout for booking ...0222', '8a09da6c-0671-4f17-82d2-0c43342d0222'),

-- ---------------------------------
-- MAR 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0205 (Canceled: 2025-03-08 | Pkg: ...0009 | Price: 199000)
(gen_random_uuid(), '2025-03-08 14:30:00', '2025-03-08 14:31:00', 2, 199000.0, 3, 0, 'PAYID-MAR-CNL-01', 'https://example.com/checkout/...?token=MAR-CNL-01', NULL, 'Refund for booking ...0205', '8a09da6c-0671-4f17-82d2-0c43342d0205'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0232 (Canceled: 2025-03-17 | Pkg: ...0701 | Price: 149000)
(gen_random_uuid(), '2025-03-17 14:30:00', '2025-03-17 14:31:00', 2, 149000.0, 3, 0, 'PAYID-MAR-CNL-02', 'https://example.com/checkout/...?token=MAR-CNL-02', NULL, 'Refund for booking ...0232', '8a09da6c-0671-4f17-82d2-0c43342d0232'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0253 (Canceled: 2025-03-24 | Pkg: ...0501 | Price: 379000)
(gen_random_uuid(), '2025-03-24 14:30:00', '2025-03-24 14:31:00', 2, 379000.0, 3, 0, 'PAYID-MAR-CNL-03', 'https://example.com/checkout/...?token=MAR-CNL-03', NULL, 'Refund for booking ...0253', '8a09da6c-0671-4f17-82d2-0c43342d0253'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0271 (Canceled: 2025-03-31 | Pkg: ...0002 | Price: 499000)
(gen_random_uuid(), '2025-03-31 09:30:00', '2025-03-31 09:31:00', 2, 499000.0, 3, 0, 'PAYID-MAR-CNL-04', 'https://example.com/checkout/...?token=MAR-CNL-04', NULL, 'Refund for booking ...0271', '8a09da6c-0671-4f17-82d2-0c43342d0271');

-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.17
-- Description: Insert initial data for booking payments (Apr & May 2025)
-- =============================================

-- Ghi chú Enum:
-- payment_method: 2 (Mặc định)
-- status: 1 (COMPLETED), 3 (REFUND)
-- payment_type: 0 (PAID_PACKAGE), 1 (RECEIVED_PACKAGE)
-- Logic Payout: amount = package.price - package.service_fee_amount
-- Logic Thời gian: Cả PAID và RECEIVED đều được tạo/cập nhật vào ngày booking hoàn thành (booking.updated_at)

INSERT INTO booking_payment (booking_payment_id, created_at, updated_at, payment_method, amount, status, payment_type, transaction_id, approval_url, failure_reason, extra_info, booking_id) VALUES

-- =============================================
-- BOOKING PAYMENTS - APR 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0272 (Completed: 2025-04-03 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-04-03 10:00:00', '2025-04-03 10:00:00', 2, 399000.0, 1, 0, 'PAYID-APR0001A', 'https://example.com/checkout/...?token=APR0001', NULL, 'Payment for booking ...0272', '8a09da6c-0671-4f17-82d2-0c43342d0272'),
(gen_random_uuid(), '2025-04-03 10:00:00', '2025-04-03 10:00:00', 2, 359100.0, 1, 1, 'PAYOUT-APR0001B', NULL, NULL, 'Payout for booking ...0272', '8a09da6c-0671-4f17-82d2-0c43342d0272'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0273 (Completed: 2025-04-03 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-04-03 14:30:00', '2025-04-03 14:30:00', 2, 229000.0, 1, 0, 'PAYID-APR0002A', 'https://example.com/checkout/...?token=APR0002', NULL, 'Payment for booking ...0273', '8a09da6c-0671-4f17-82d2-0c43342d0273'),
(gen_random_uuid(), '2025-04-03 14:30:00', '2025-04-03 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-APR0002B', NULL, NULL, 'Payout for booking ...0273', '8a09da6c-0671-4f17-82d2-0c43342d0273'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0274 (Completed: 2025-04-03 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-04-03 17:00:00', '2025-04-03 17:00:00', 2, 279000.0, 1, 0, 'PAYID-APR0003A', 'https://example.com/checkout/...?token=APR0003', NULL, 'Payment for booking ...0274', '8a09da6c-0671-4f17-82d2-0c43342d0274'),
(gen_random_uuid(), '2025-04-03 17:00:00', '2025-04-03 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-APR0003B', NULL, NULL, 'Payout for booking ...0274', '8a09da6c-0671-4f17-82d2-0c43342d0274'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0275 (Completed: 2025-04-04 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-04-04 11:00:00', '2025-04-04 11:00:00', 2, 219000.0, 1, 0, 'PAYID-APR0004A', 'https://example.com/checkout/...?token=APR0004', NULL, 'Payment for booking ...0275', '8a09da6c-0671-4f17-82d2-0c43342d0275'),
(gen_random_uuid(), '2025-04-04 11:00:00', '2025-04-04 11:00:00', 2, 197100.0, 1, 1, 'PAYOUT-APR0004B', NULL, NULL, 'Payout for booking ...0275', '8a09da6c-0671-4f17-82d2-0c43342d0275'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0276 (Completed: 2025-04-04 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-04-04 14:30:00', '2025-04-04 14:30:00', 2, 339000.0, 1, 0, 'PAYID-APR0005A', 'https://example.com/checkout/...?token=APR0005', NULL, 'Payment for booking ...0276', '8a09da6c-0671-4f17-82d2-0c43342d0276'),
(gen_random_uuid(), '2025-04-04 14:30:00', '2025-04-04 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-APR0005B', NULL, NULL, 'Payout for booking ...0276', '8a09da6c-0671-4f17-82d2-0c43342d0276'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0277 (Completed: 2025-04-04 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-04-04 17:00:00', '2025-04-04 17:00:00', 2, 799000.0, 1, 0, 'PAYID-APR0006A', 'https://example.com/checkout/...?token=APR0006', NULL, 'Payment for booking ...0277', '8a09da6c-0671-4f17-82d2-0c43342d0277'),
(gen_random_uuid(), '2025-04-04 17:00:00', '2025-04-04 17:00:00', 2, 719100.0, 1, 1, 'PAYOUT-APR0006B', NULL, NULL, 'Payout for booking ...0277', '8a09da6c-0671-4f17-82d2-0c43342d0277'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0278 (Completed: 2025-04-05 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-04-05 10:00:00', '2025-04-05 10:00:00', 2, 269000.0, 1, 0, 'PAYID-APR0007A', 'https://example.com/checkout/...?token=APR0007', NULL, 'Payment for booking ...0278', '8a09da6c-0671-4f17-82d2-0c43342d0278'),
(gen_random_uuid(), '2025-04-05 10:00:00', '2025-04-05 10:00:00', 2, 242100.0, 1, 1, 'PAYOUT-APR0007B', NULL, NULL, 'Payout for booking ...0278', '8a09da6c-0671-4f17-82d2-0c43342d0278'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0279 (Completed: 2025-04-05 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-04-05 14:30:00', '2025-04-05 14:30:00', 2, 599000.0, 1, 0, 'PAYID-APR0008A', 'https://example.com/checkout/...?token=APR0008', NULL, 'Payment for booking ...0279', '8a09da6c-0671-4f17-82d2-0c43342d0279'),
(gen_random_uuid(), '2025-04-05 14:30:00', '2025-04-05 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-APR0008B', NULL, NULL, 'Payout for booking ...0279', '8a09da6c-0671-4f17-82d2-0c43342d0279'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0280 (Completed: 2025-04-05 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-04-05 17:00:00', '2025-04-05 17:00:00', 2, 249000.0, 1, 0, 'PAYID-APR0009A', 'https://example.com/checkout/...?token=APR0009', NULL, 'Payment for booking ...0280', '8a09da6c-0671-4f17-82d2-0c43342d0280'),
(gen_random_uuid(), '2025-04-05 17:00:00', '2025-04-05 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-APR0009B', NULL, NULL, 'Payout for booking ...0280', '8a09da6c-0671-4f17-82d2-0c43342d0280'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0281 (Completed: 2025-04-06 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-04-06 11:00:00', '2025-04-06 11:00:00', 2, 319000.0, 1, 0, 'PAYID-APR0010A', 'https://example.com/checkout/...?token=APR0010', NULL, 'Payment for booking ...0281', '8a09da6c-0671-4f17-82d2-0c43342d0281'),
(gen_random_uuid(), '2025-04-06 11:00:00', '2025-04-06 11:00:00', 2, 287100.0, 1, 1, 'PAYOUT-APR0010B', NULL, NULL, 'Payout for booking ...0281', '8a09da6c-0671-4f17-82d2-0c43342d0281'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0282 (Completed: 2025-04-06 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-04-06 14:30:00', '2025-04-06 14:30:00', 2, 329000.0, 1, 0, 'PAYID-APR0011A', 'https://example.com/checkout/...?token=APR0011', NULL, 'Payment for booking ...0282', '8a09da6c-0671-4f17-82d2-0c43342d0282'),
(gen_random_uuid(), '2025-04-06 14:30:00', '2025-04-06 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-APR0011B', NULL, NULL, 'Payout for booking ...0282', '8a09da6c-0671-4f17-82d2-0c43342d0282'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0284 (Completed: 2025-04-07 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-04-07 10:00:00', '2025-04-07 10:00:00', 2, 449000.0, 1, 0, 'PAYID-APR0012A', 'https://example.com/checkout/...?token=APR0012', NULL, 'Payment for booking ...0284', '8a09da6c-0671-4f17-82d2-0c43342d0284'),
(gen_random_uuid(), '2025-04-07 10:00:00', '2025-04-07 10:00:00', 2, 404100.0, 1, 1, 'PAYOUT-APR0012B', NULL, NULL, 'Payout for booking ...0284', '8a09da6c-0671-4f17-82d2-0c43342d0284'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0285 (Completed: 2025-04-07 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-04-07 14:30:00', '2025-04-07 14:30:00', 2, 149000.0, 1, 0, 'PAYID-APR0013A', 'https://example.com/checkout/...?token=APR0013', NULL, 'Payment for booking ...0285', '8a09da6c-0671-4f17-82d2-0c43342d0285'),
(gen_random_uuid(), '2025-04-07 14:30:00', '2025-04-07 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-APR0013B', NULL, NULL, 'Payout for booking ...0285', '8a09da6c-0671-4f17-82d2-0c43342d0285'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0286 (Completed: 2025-04-07 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-04-07 17:00:00', '2025-04-07 17:00:00', 2, 549000.0, 1, 0, 'PAYID-APR0014A', 'https://example.com/checkout/...?token=APR0014', NULL, 'Payment for booking ...0286', '8a09da6c-0671-4f17-82d2-0c43342d0286'),
(gen_random_uuid(), '2025-04-07 17:00:00', '2025-04-07 17:00:00', 2, 494100.0, 1, 1, 'PAYOUT-APR0014B', NULL, NULL, 'Payout for booking ...0286', '8a09da6c-0671-4f17-82d2-0c43342d0286'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0287 (Completed: 2025-04-08 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-04-08 11:00:00', '2025-04-08 11:00:00', 2, 379000.0, 1, 0, 'PAYID-APR0015A', 'https://example.com/checkout/...?token=APR0015', NULL, 'Payment for booking ...0287', '8a09da6c-0671-4f17-82d2-0c43342d0287'),
(gen_random_uuid(), '2025-04-08 11:00:00', '2025-04-08 11:00:00', 2, 341100.0, 1, 1, 'PAYOUT-APR0015B', NULL, NULL, 'Payout for booking ...0287', '8a09da6c-0671-4f17-82d2-0c43342d0287'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0288 (Completed: 2025-04-08 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-04-08 14:30:00', '2025-04-08 14:30:00', 2, 229000.0, 1, 0, 'PAYID-APR0016A', 'https://example.com/checkout/...?token=APR0016', NULL, 'Payment for booking ...0288', '8a09da6c-0671-4f17-82d2-0c43342d0288'),
(gen_random_uuid(), '2025-04-08 14:30:00', '2025-04-08 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-APR0016B', NULL, NULL, 'Payout for booking ...0288', '8a09da6c-0671-4f17-82d2-0c43342d0288'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0289 (Completed: 2025-04-08 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-04-08 17:00:00', '2025-04-08 17:00:00', 2, 699000.0, 1, 0, 'PAYID-APR0017A', 'https://example.com/checkout/...?token=APR0017', NULL, 'Payment for booking ...0289', '8a09da6c-0671-4f17-82d2-0c43342d0289'),
(gen_random_uuid(), '2025-04-08 17:00:00', '2025-04-08 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-APR0017B', NULL, NULL, 'Payout for booking ...0289', '8a09da6c-0671-4f17-82d2-0c43342d0289'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0290 (Completed: 2025-04-09 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-04-09 10:00:00', '2025-04-09 10:00:00', 2, 449000.0, 1, 0, 'PAYID-APR0018A', 'https://example.com/checkout/...?token=APR0018', NULL, 'Payment for booking ...0290', '8a09da6c-0671-4f17-82d2-0c43342d0290'),
(gen_random_uuid(), '2025-04-09 10:00:00', '2025-04-09 10:00:00', 2, 404100.0, 1, 1, 'PAYOUT-APR0018B', NULL, NULL, 'Payout for booking ...0290', '8a09da6c-0671-4f17-82d2-0c43342d0290'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0291 (Completed: 2025-04-09 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-04-09 14:30:00', '2025-04-09 14:30:00', 2, 299000.0, 1, 0, 'PAYID-APR0019A', 'https://example.com/checkout/...?token=APR0019', NULL, 'Payment for booking ...0291', '8a09da6c-0671-4f17-82d2-0c43342d0291'),
(gen_random_uuid(), '2025-04-09 14:30:00', '2025-04-09 14:30:00', 2, 269100.0, 1, 1, 'PAYOUT-APR0019B', NULL, NULL, 'Payout for booking ...0291', '8a09da6c-0671-4f17-82d2-0c43342d0291'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0292 (Completed: 2025-04-09 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-04-09 17:00:00', '2025-04-09 17:00:00', 2, 279000.0, 1, 0, 'PAYID-APR0020A', 'https://example.com/checkout/...?token=APR0020', NULL, 'Payment for booking ...0292', '8a09da6c-0671-4f17-82d2-0c43342d0292'),
(gen_random_uuid(), '2025-04-09 17:00:00', '2025-04-09 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-APR0020B', NULL, NULL, 'Payout for booking ...0292', '8a09da6c-0671-4f17-82d2-0c43342d0292'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0293 (Completed: 2025-04-10 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-04-10 11:00:00', '2025-04-10 11:00:00', 2, 329000.0, 1, 0, 'PAYID-APR0021A', 'https://example.com/checkout/...?token=APR0021', NULL, 'Payment for booking ...0293', '8a09da6c-0671-4f17-82d2-0c43342d0293'),
(gen_random_uuid(), '2025-04-10 11:00:00', '2025-04-10 11:00:00', 2, 296100.0, 1, 1, 'PAYOUT-APR0021B', NULL, NULL, 'Payout for booking ...0293', '8a09da6c-0671-4f17-82d2-0c43342d0293'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0294 (Completed: 2025-04-10 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-04-10 14:30:00', '2025-04-10 14:30:00', 2, 249000.0, 1, 0, 'PAYID-APR0022A', 'https://example.com/checkout/...?token=APR0022', NULL, 'Payment for booking ...0294', '8a09da6c-0671-4f17-82d2-0c43342d0294'),
(gen_random_uuid(), '2025-04-10 14:30:00', '2025-04-10 14:30:00', 2, 224100.0, 1, 1, 'PAYOUT-APR0022B', NULL, NULL, 'Payout for booking ...0294', '8a09da6c-0671-4f17-82d2-0c43342d0294'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0295 (Completed: 2025-04-10 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-04-10 17:00:00', '2025-04-10 17:00:00', 2, 369000.0, 1, 0, 'PAYID-APR0023A', 'https://example.com/checkout/...?token=APR0023', NULL, 'Payment for booking ...0295', '8a09da6c-0671-4f17-82d2-0c43342d0295'),
(gen_random_uuid(), '2025-04-10 17:00:00', '2025-04-10 17:00:00', 2, 332100.0, 1, 1, 'PAYOUT-APR0023B', NULL, NULL, 'Payout for booking ...0295', '8a09da6c-0671-4f17-82d2-0c43342d0295'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0296 (Completed: 2025-04-11 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-04-11 10:00:00', '2025-04-11 10:00:00', 2, 429000.0, 1, 0, 'PAYID-APR0024A', 'https://example.com/checkout/...?token=APR0024', NULL, 'Payment for booking ...0296', '8a09da6c-0671-4f17-82d2-0c43342d0296'),
(gen_random_uuid(), '2025-04-11 10:00:00', '2025-04-11 10:00:00', 2, 386100.0, 1, 1, 'PAYOUT-APR0024B', NULL, NULL, 'Payout for booking ...0296', '8a09da6c-0671-4f17-82d2-0c43342d0296'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0297 (Completed: 2025-04-11 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-04-11 14:30:00', '2025-04-11 14:30:00', 2, 199000.0, 1, 0, 'PAYID-APR0025A', 'https://example.com/checkout/...?token=APR0025', NULL, 'Payment for booking ...0297', '8a09da6c-0671-4f17-82d2-0c43342d0297'),
(gen_random_uuid(), '2025-04-11 14:30:00', '2025-04-11 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-APR0025B', NULL, NULL, 'Payout for booking ...0297', '8a09da6c-0671-4f17-82d2-0c43342d0297'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0298 (Completed: 2025-04-11 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-04-11 17:00:00', '2025-04-11 17:00:00', 2, 379000.0, 1, 0, 'PAYID-APR0026A', 'https://example.com/checkout/...?token=APR0026', NULL, 'Payment for booking ...0298', '8a09da6c-0671-4f17-82d2-0c43342d0298'),
(gen_random_uuid(), '2025-04-11 17:00:00', '2025-04-11 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-APR0026B', NULL, NULL, 'Payout for booking ...0298', '8a09da6c-0671-4f17-82d2-0c43342d0298'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0299 (Completed: 2025-04-12 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-04-12 11:00:00', '2025-04-12 11:00:00', 2, 699000.0, 1, 0, 'PAYID-APR0027A', 'https://example.com/checkout/...?token=APR0027', NULL, 'Payment for booking ...0299', '8a09da6c-0671-4f17-82d2-0c43342d0299'),
(gen_random_uuid(), '2025-04-12 11:00:00', '2025-04-12 11:00:00', 2, 629100.0, 1, 1, 'PAYOUT-APR0027B', NULL, NULL, 'Payout for booking ...0299', '8a09da6c-0671-4f17-82d2-0c43342d0299'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0300 (Completed: 2025-04-12 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-04-12 14:30:00', '2025-04-12 14:30:00', 2, 149000.0, 1, 0, 'PAYID-APR0028A', 'https://example.com/checkout/...?token=APR0028', NULL, 'Payment for booking ...0300', '8a09da6c-0671-4f17-82d2-0c43342d0300'),
(gen_random_uuid(), '2025-04-12 14:30:00', '2025-04-12 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-APR0028B', NULL, NULL, 'Payout for booking ...0300', '8a09da6c-0671-4f17-82d2-0c43342d0300'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0301 (Completed: 2025-04-12 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-04-12 17:00:00', '2025-04-12 17:00:00', 2, 269000.0, 1, 0, 'PAYID-APR0029A', 'https://example.com/checkout/...?token=APR0029', NULL, 'Payment for booking ...0301', '8a09da6c-0671-4f17-82d2-0c43342d0301'),
(gen_random_uuid(), '2025-04-12 17:00:00', '2025-04-12 17:00:00', 2, 242100.0, 1, 1, 'PAYOUT-APR0029B', NULL, NULL, 'Payout for booking ...0301', '8a09da6c-0671-4f17-82d2-0c43342d0301'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0302 (Completed: 2025-04-13 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-04-13 10:00:00', '2025-04-13 10:00:00', 2, 499000.0, 1, 0, 'PAYID-APR0030A', 'https://example.com/checkout/...?token=APR0030', NULL, 'Payment for booking ...0302', '8a09da6c-0671-4f17-82d2-0c43342d0302'),
(gen_random_uuid(), '2025-04-13 10:00:00', '2025-04-13 10:00:00', 2, 449100.0, 1, 1, 'PAYOUT-APR0030B', NULL, NULL, 'Payout for booking ...0302', '8a09da6c-0671-4f17-82d2-0c43342d0302'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0303 (Completed: 2025-04-13 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-04-13 14:30:00', '2025-04-13 14:30:00', 2, 549000.0, 1, 0, 'PAYID-APR0031A', 'https://example.com/checkout/...?token=APR0031', NULL, 'Payment for booking ...0303', '8a09da6c-0671-4f17-82d2-0c43342d0303'),
(gen_random_uuid(), '2025-04-13 14:30:00', '2025-04-13 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-APR0031B', NULL, NULL, 'Payout for booking ...0303', '8a09da6c-0671-4f17-82d2-0c43342d0303'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0305 (Completed: 2025-04-14 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-04-14 11:00:00', '2025-04-14 11:00:00', 2, 499000.0, 1, 0, 'PAYID-APR0032A', 'https://example.com/checkout/...?token=APR0032', NULL, 'Payment for booking ...0305', '8a09da6c-0671-4f17-82d2-0c43342d0305'),
(gen_random_uuid(), '2025-04-14 11:00:00', '2025-04-14 11:00:00', 2, 449100.0, 1, 1, 'PAYOUT-APR0032B', NULL, NULL, 'Payout for booking ...0305', '8a09da6c-0671-4f17-82d2-0c43342d0305'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0306 (Completed: 2025-04-14 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-04-14 14:30:00', '2025-04-14 14:30:00', 2, 399000.0, 1, 0, 'PAYID-APR0033A', 'https://example.com/checkout/...?token=APR0033', NULL, 'Payment for booking ...0306', '8a09da6c-0671-4f17-82d2-0c43342d0306'),
(gen_random_uuid(), '2025-04-14 14:30:00', '2025-04-14 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-APR0033B', NULL, NULL, 'Payout for booking ...0306', '8a09da6c-0671-4f17-82d2-0c43342d0306'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0307 (Completed: 2025-04-14 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-04-14 17:00:00', '2025-04-14 17:00:00', 2, 229000.0, 1, 0, 'PAYID-APR0034A', 'https://example.com/checkout/...?token=APR0034', NULL, 'Payment for booking ...0307', '8a09da6c-0671-4f17-82d2-0c43342d0307'),
(gen_random_uuid(), '2025-04-14 17:00:00', '2025-04-14 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-APR0034B', NULL, NULL, 'Payout for booking ...0307', '8a09da6c-0671-4f17-82d2-0c43342d0307'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0308 (Completed: 2025-04-15 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-04-15 10:00:00', '2025-04-15 10:00:00', 2, 279000.0, 1, 0, 'PAYID-APR0035A', 'https://example.com/checkout/...?token=APR0035', NULL, 'Payment for booking ...0308', '8a09da6c-0671-4f17-82d2-0c43342d0308'),
(gen_random_uuid(), '2025-04-15 10:00:00', '2025-04-15 10:00:00', 2, 251100.0, 1, 1, 'PAYOUT-APR0035B', NULL, NULL, 'Payout for booking ...0308', '8a09da6c-0671-4f17-82d2-0c43342d0308'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0309 (Completed: 2025-04-15 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-04-15 14:30:00', '2025-04-15 14:30:00', 2, 219000.0, 1, 0, 'PAYID-APR0036A', 'https://example.com/checkout/...?token=APR0036', NULL, 'Payment for booking ...0309', '8a09da6c-0671-4f17-82d2-0c43342d0309'),
(gen_random_uuid(), '2025-04-15 14:30:00', '2025-04-15 14:30:00', 2, 197100.0, 1, 1, 'PAYOUT-APR0036B', NULL, NULL, 'Payout for booking ...0309', '8a09da6c-0671-4f17-82d2-0c43342d0309'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0310 (Completed: 2025-04-15 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-04-15 17:00:00', '2025-04-15 17:00:00', 2, 339000.0, 1, 0, 'PAYID-APR0037A', 'https://example.com/checkout/...?token=APR0037', NULL, 'Payment for booking ...0310', '8a09da6c-0671-4f17-82d2-0c43342d0310'),
(gen_random_uuid(), '2025-04-15 17:00:00', '2025-04-15 17:00:00', 2, 305100.0, 1, 1, 'PAYOUT-APR0037B', NULL, NULL, 'Payout for booking ...0310', '8a09da6c-0671-4f17-82d2-0c43342d0310'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0311 (Completed: 2025-04-16 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-04-16 11:00:00', '2025-04-16 11:00:00', 2, 799000.0, 1, 0, 'PAYID-APR0038A', 'https://example.com/checkout/...?token=APR0038', NULL, 'Payment for booking ...0311', '8a09da6c-0671-4f17-82d2-0c43342d0311'),
(gen_random_uuid(), '2025-04-16 11:00:00', '2025-04-16 11:00:00', 2, 719100.0, 1, 1, 'PAYOUT-APR0038B', NULL, NULL, 'Payout for booking ...0311', '8a09da6c-0671-4f17-82d2-0c43342d0311'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0312 (Completed: 2025-04-16 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-04-16 14:30:00', '2025-04-16 14:30:00', 2, 269000.0, 1, 0, 'PAYID-APR0039A', 'https://example.com/checkout/...?token=APR0039', NULL, 'Payment for booking ...0312', '8a09da6c-0671-4f17-82d2-0c43342d0312'),
(gen_random_uuid(), '2025-04-16 14:30:00', '2025-04-16 14:30:00', 2, 242100.0, 1, 1, 'PAYOUT-APR0039B', NULL, NULL, 'Payout for booking ...0312', '8a09da6c-0671-4f17-82d2-0c43342d0312'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0313 (Completed: 2025-04-16 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-04-16 17:00:00', '2025-04-16 17:00:00', 2, 599000.0, 1, 0, 'PAYID-APR0040A', 'https://example.com/checkout/...?token=APR0040', NULL, 'Payment for booking ...0313', '8a09da6c-0671-4f17-82d2-0c43342d0313'),
(gen_random_uuid(), '2025-04-16 17:00:00', '2025-04-16 17:00:00', 2, 539100.0, 1, 1, 'PAYOUT-APR0040B', NULL, NULL, 'Payout for booking ...0313', '8a09da6c-0671-4f17-82d2-0c43342d0313'),

-- ---------------------------------
-- APR 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0283 (Canceled: 2025-04-04 | Pkg: ...0903 | Price: 1499000)
(gen_random_uuid(), '2025-04-04 14:30:00', '2025-04-04 14:31:00', 2, 1499000.0, 3, 0, 'PAYID-APR-CNL-01', 'https://example.com/checkout/...?token=APR-CNL-01', NULL, 'Refund for booking ...0283', '8a09da6c-0671-4f17-82d2-0c43342d0283'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0304 (Canceled: 2025-04-11 | Pkg: ...0004 | Price: 899000)
(gen_random_uuid(), '2025-04-11 14:30:00', '2025-04-11 14:31:00', 2, 899000.0, 3, 0, 'PAYID-APR-CNL-02', 'https://example.com/checkout/...?token=APR-CNL-02', NULL, 'Refund for booking ...0304', '8a09da6c-0671-4f17-82d2-0c43342d0304'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0322 (Canceled: 2025-04-17 | Pkg: ...0202 | Price: 229000)
(gen_random_uuid(), '2025-04-17 14:30:00', '2025-04-17 14:31:00', 2, 229000.0, 3, 0, 'PAYID-APR-CNL-03', 'https://example.com/checkout/...?token=APR-CNL-03', NULL, 'Refund for booking ...0322', '8a09da6c-0671-4f17-82d2-0c43342d0322'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0343 (Canceled: 2025-04-24 | Pkg: ...0402 | Price: 219000)
(gen_random_uuid(), '2025-04-24 14:30:00', '2025-04-24 14:31:00', 2, 219000.0, 3, 0, 'PAYID-APR-CNL-04', 'https://example.com/checkout/...?token=APR-CNL-04', NULL, 'Refund for booking ...0343', '8a09da6c-0671-4f17-82d2-0c43342d0343'),

-- =============================================
-- BOOKING PAYMENTS - MAY 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0363 (Completed: 2025-05-03 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-05-03 11:00:00', '2025-05-03 11:00:00', 2, 249000.0, 1, 0, 'PAYID-MAY0001A', 'https://example.com/checkout/...?token=MAY0001', NULL, 'Payment for booking ...0363', '8a09da6c-0671-4f17-82d2-0c43342d0363'),
(gen_random_uuid(), '2025-05-03 11:00:00', '2025-05-03 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-MAY0001B', NULL, NULL, 'Payout for booking ...0363', '8a09da6c-0671-4f17-82d2-0c43342d0363'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0364 (Completed: 2025-05-03 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-05-03 14:30:00', '2025-05-03 14:30:00', 2, 369000.0, 1, 0, 'PAYID-MAY0002A', 'https://example.com/checkout/...?token=MAY0002', NULL, 'Payment for booking ...0364', '8a09da6c-0671-4f17-82d2-0c43342d0364'),
(gen_random_uuid(), '2025-05-03 14:30:00', '2025-05-03 14:30:00', 2, 332100.0, 1, 1, 'PAYOUT-MAY0002B', NULL, NULL, 'Payout for booking ...0364', '8a09da6c-0671-4f17-82d2-0c43342d0364'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0365 (Completed: 2025-05-03 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-05-03 17:00:00', '2025-05-03 17:00:00', 2, 429000.0, 1, 0, 'PAYID-MAY0003A', 'https://example.com/checkout/...?token=MAY0003', NULL, 'Payment for booking ...0365', '8a09da6c-0671-4f17-82d2-0c43342d0365'),
(gen_random_uuid(), '2025-05-03 17:00:00', '2025-05-03 17:00:00', 2, 386100.0, 1, 1, 'PAYOUT-MAY0003B', NULL, NULL, 'Payout for booking ...0365', '8a09da6c-0671-4f17-82d2-0c43342d0365'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0366 (Completed: 2025-05-04 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-05-04 10:00:00', '2025-05-04 10:00:00', 2, 199000.0, 1, 0, 'PAYID-MAY0004A', 'https://example.com/checkout/...?token=MAY0004', NULL, 'Payment for booking ...0366', '8a09da6c-0671-4f17-82d2-0c43342d0366'),
(gen_random_uuid(), '2025-05-04 10:00:00', '2025-05-04 10:00:00', 2, 179100.0, 1, 1, 'PAYOUT-MAY0004B', NULL, NULL, 'Payout for booking ...0366', '8a09da6c-0671-4f17-82d2-0c43342d0366'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0367 (Completed: 2025-05-04 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-05-04 14:30:00', '2025-05-04 14:30:00', 2, 379000.0, 1, 0, 'PAYID-MAY0005A', 'https://example.com/checkout/...?token=MAY0005', NULL, 'Payment for booking ...0367', '8a09da6c-0671-4f17-82d2-0c43342d0367'),
(gen_random_uuid(), '2025-05-04 14:30:00', '2025-05-04 14:30:00', 2, 341100.0, 1, 1, 'PAYOUT-MAY0005B', NULL, NULL, 'Payout for booking ...0367', '8a09da6c-0671-4f17-82d2-0c43342d0367'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0368 (Completed: 2025-05-04 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-05-04 17:00:00', '2025-05-04 17:00:00', 2, 699000.0, 1, 0, 'PAYID-MAY0006A', 'https://example.com/checkout/...?token=MAY0006', NULL, 'Payment for booking ...0368', '8a09da6c-0671-4f17-82d2-0c43342d0368'),
(gen_random_uuid(), '2025-05-04 17:00:00', '2025-05-04 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-MAY0006B', NULL, NULL, 'Payout for booking ...0368', '8a09da6c-0671-4f17-82d2-0c43342d0368'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0369 (Completed: 2025-05-05 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-05-05 11:00:00', '2025-05-05 11:00:00', 2, 149000.0, 1, 0, 'PAYID-MAY0007A', 'https://example.com/checkout/...?token=MAY0007', NULL, 'Payment for booking ...0369', '8a09da6c-0671-4f17-82d2-0c43342d0369'),
(gen_random_uuid(), '2025-05-05 11:00:00', '2025-05-05 11:00:00', 2, 134100.0, 1, 1, 'PAYOUT-MAY0007B', NULL, NULL, 'Payout for booking ...0369', '8a09da6c-0671-4f17-82d2-0c43342d0369'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0370 (Completed: 2025-05-05 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-05-05 14:30:00', '2025-05-05 14:30:00', 2, 269000.0, 1, 0, 'PAYID-MAY0008A', 'https://example.com/checkout/...?token=MAY0008', NULL, 'Payment for booking ...0370', '8a09da6c-0671-4f17-82d2-0c43342d0370'),
(gen_random_uuid(), '2025-05-05 14:30:00', '2025-05-05 14:30:00', 2, 242100.0, 1, 1, 'PAYOUT-MAY0008B', NULL, NULL, 'Payout for booking ...0370', '8a09da6c-0671-4f17-82d2-0c43342d0370'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0371 (Completed: 2025-05-05 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-05-05 17:00:00', '2025-05-05 17:00:00', 2, 499000.0, 1, 0, 'PAYID-MAY0009A', 'https://example.com/checkout/...?token=MAY0009', NULL, 'Payment for booking ...0371', '8a09da6c-0671-4f17-82d2-0c43342d0371'),
(gen_random_uuid(), '2025-05-05 17:00:00', '2025-05-05 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-MAY0009B', NULL, NULL, 'Payout for booking ...0371', '8a09da6c-0671-4f17-82d2-0c43342d0371'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0372 (Completed: 2025-05-06 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-05-06 10:00:00', '2025-05-06 10:00:00', 2, 549000.0, 1, 0, 'PAYID-MAY0010A', 'https://example.com/checkout/...?token=MAY0010', NULL, 'Payment for booking ...0372', '8a09da6c-0671-4f17-82d2-0c43342d0372'),
(gen_random_uuid(), '2025-05-06 10:00:00', '2025-05-06 10:00:00', 2, 494100.0, 1, 1, 'PAYOUT-MAY0010B', NULL, NULL, 'Payout for booking ...0372', '8a09da6c-0671-4f17-82d2-0c43342d0372'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0373 (Completed: 2025-05-06 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-05-06 14:30:00', '2025-05-06 14:30:00', 2, 899000.0, 1, 0, 'PAYID-MAY0011A', 'https://example.com/checkout/...?token=MAY0011', NULL, 'Payment for booking ...0373', '8a09da6c-0671-4f17-82d2-0c43342d0373'),
(gen_random_uuid(), '2025-05-06 14:30:00', '2025-05-06 14:30:00', 2, 809100.0, 1, 1, 'PAYOUT-MAY0011B', NULL, NULL, 'Payout for booking ...0373', '8a09da6c-0671-4f17-82d2-0c43342d0373'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0374 (Completed: 2025-05-06 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-05-06 17:00:00', '2025-05-06 17:00:00', 2, 499000.0, 1, 0, 'PAYID-MAY0012A', 'https://example.com/checkout/...?token=MAY0012', NULL, 'Payment for booking ...0374', '8a09da6c-0671-4f17-82d2-0c43342d0374'),
(gen_random_uuid(), '2025-05-06 17:00:00', '2025-05-06 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-MAY0012B', NULL, NULL, 'Payout for booking ...0374', '8a09da6c-0671-4f17-82d2-0c43342d0374'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0375 (Completed: 2025-05-07 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-05-07 11:00:00', '2025-05-07 11:00:00', 2, 399000.0, 1, 0, 'PAYID-MAY0013A', 'https://example.com/checkout/...?token=MAY0013', NULL, 'Payment for booking ...0375', '8a09da6c-0671-4f17-82d2-0c43342d0375'),
(gen_random_uuid(), '2025-05-07 11:00:00', '2025-05-07 11:00:00', 2, 359100.0, 1, 1, 'PAYOUT-MAY0013B', NULL, NULL, 'Payout for booking ...0375', '8a09da6c-0671-4f17-82d2-0c43342d0375'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0376 (Completed: 2025-05-07 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-05-07 14:30:00', '2025-05-07 14:30:00', 2, 229000.0, 1, 0, 'PAYID-MAY0014A', 'https://example.com/checkout/...?token=MAY0014', NULL, 'Payment for booking ...0376', '8a09da6c-0671-4f17-82d2-0c43342d0376'),
(gen_random_uuid(), '2025-05-07 14:30:00', '2025-05-07 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-MAY0014B', NULL, NULL, 'Payout for booking ...0376', '8a09da6c-0671-4f17-82d2-0c43342d0376'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0378 (Completed: 2025-05-08 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-05-08 10:00:00', '2025-05-08 10:00:00', 2, 219000.0, 1, 0, 'PAYID-MAY0015A', 'https://example.com/checkout/...?token=MAY0015', NULL, 'Payment for booking ...0378', '8a09da6c-0671-4f17-82d2-0c43342d0378'),
(gen_random_uuid(), '2025-05-08 10:00:00', '2025-05-08 10:00:00', 2, 197100.0, 1, 1, 'PAYOUT-MAY0015B', NULL, NULL, 'Payout for booking ...0378', '8a09da6c-0671-4f17-82d2-0c43342d0378'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0379 (Completed: 2025-05-08 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-05-08 14:30:00', '2025-05-08 14:30:00', 2, 339000.0, 1, 0, 'PAYID-MAY0016A', 'https://example.com/checkout/...?token=MAY0016', NULL, 'Payment for booking ...0379', '8a09da6c-0671-4f17-82d2-0c43342d0379'),
(gen_random_uuid(), '2025-05-08 14:30:00', '2025-05-08 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-MAY0016B', NULL, NULL, 'Payout for booking ...0379', '8a09da6c-0671-4f17-82d2-0c43342d0379'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0380 (Completed: 2025-05-08 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-05-08 17:00:00', '2025-05-08 17:00:00', 2, 799000.0, 1, 0, 'PAYID-MAY0017A', 'https://example.com/checkout/...?token=MAY0017', NULL, 'Payment for booking ...0380', '8a09da6c-0671-4f17-82d2-0c43342d0380'),
(gen_random_uuid(), '2025-05-08 17:00:00', '2025-05-08 17:00:00', 2, 719100.0, 1, 1, 'PAYOUT-MAY0017B', NULL, NULL, 'Payout for booking ...0380', '8a09da6c-0671-4f17-82d2-0c43342d0380'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0381 (Completed: 2025-05-09 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-05-09 11:00:00', '2025-05-09 11:00:00', 2, 269000.0, 1, 0, 'PAYID-MAY0018A', 'https://example.com/checkout/...?token=MAY0018', NULL, 'Payment for booking ...0381', '8a09da6c-0671-4f17-82d2-0c43342d0381'),
(gen_random_uuid(), '2025-05-09 11:00:00', '2025-05-09 11:00:00', 2, 242100.0, 1, 1, 'PAYOUT-MAY0018B', NULL, NULL, 'Payout for booking ...0381', '8a09da6c-0671-4f17-82d2-0c43342d0381'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0382 (Completed: 2025-05-09 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-05-09 14:30:00', '2025-05-09 14:30:00', 2, 599000.0, 1, 0, 'PAYID-MAY0019A', 'https://example.com/checkout/...?token=MAY0019', NULL, 'Payment for booking ...0382', '8a09da6c-0671-4f17-82d2-0c43342d0382'),
(gen_random_uuid(), '2025-05-09 14:30:00', '2025-05-09 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-MAY0019B', NULL, NULL, 'Payout for booking ...0382', '8a09da6c-0671-4f17-82d2-0c43342d0382'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0383 (Completed: 2025-05-09 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-05-09 17:00:00', '2025-05-09 17:00:00', 2, 249000.0, 1, 0, 'PAYID-MAY0020A', 'https://example.com/checkout/...?token=MAY0020', NULL, 'Payment for booking ...0383', '8a09da6c-0671-4f17-82d2-0c43342d0383'),
(gen_random_uuid(), '2025-05-09 17:00:00', '2025-05-09 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-MAY0020B', NULL, NULL, 'Payout for booking ...0383', '8a09da6c-0671-4f17-82d2-0c43342d0383'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0384 (Completed: 2025-05-10 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-05-10 10:00:00', '2025-05-10 10:00:00', 2, 319000.0, 1, 0, 'PAYID-MAY0021A', 'https://example.com/checkout/...?token=MAY0021', NULL, 'Payment for booking ...0384', '8a09da6c-0671-4f17-82d2-0c43342d0384'),
(gen_random_uuid(), '2025-05-10 10:00:00', '2025-05-10 10:00:00', 2, 287100.0, 1, 1, 'PAYOUT-MAY0021B', NULL, NULL, 'Payout for booking ...0384', '8a09da6c-0671-4f17-82d2-0c43342d0384'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0385 (Completed: 2025-05-10 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-05-10 14:30:00', '2025-05-10 14:30:00', 2, 329000.0, 1, 0, 'PAYID-MAY0022A', 'https://example.com/checkout/...?token=MAY0022', NULL, 'Payment for booking ...0385', '8a09da6c-0671-4f17-82d2-0c43342d0385'),
(gen_random_uuid(), '2025-05-10 14:30:00', '2025-05-10 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-MAY0022B', NULL, NULL, 'Payout for booking ...0385', '8a09da6c-0671-4f17-82d2-0c43342d0385'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0386 (Completed: 2025-05-10 | Pkg: ...0903 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-05-10 17:00:00', '2025-05-10 17:00:00', 2, 1499000.0, 1, 0, 'PAYID-MAY0023A', 'https://example.com/checkout/...?token=MAY0023', NULL, 'Payment for booking ...0386', '8a09da6c-0671-4f17-82d2-0c43342d0386'),
(gen_random_uuid(), '2025-05-10 17:00:00', '2025-05-10 17:00:00', 2, 1349100.0, 1, 1, 'PAYOUT-MAY0023B', NULL, NULL, 'Payout for booking ...0386', '8a09da6c-0671-4f17-82d2-0c43342d0386'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0387 (Completed: 2025-05-11 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-05-11 11:00:00', '2025-05-11 11:00:00', 2, 449000.0, 1, 0, 'PAYID-MAY0024A', 'https://example.com/checkout/...?token=MAY0024', NULL, 'Payment for booking ...0387', '8a09da6c-0671-4f17-82d2-0c43342d0387'),
(gen_random_uuid(), '2025-05-11 11:00:00', '2025-05-11 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-MAY0024B', NULL, NULL, 'Payout for booking ...0387', '8a09da6c-0671-4f17-82d2-0c43342d0387'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0388 (Completed: 2025-05-11 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-05-11 14:30:00', '2025-05-11 14:30:00', 2, 149000.0, 1, 0, 'PAYID-MAY0025A', 'https://example.com/checkout/...?token=MAY0025', NULL, 'Payment for booking ...0388', '8a09da6c-0671-4f17-82d2-0c43342d0388'),
(gen_random_uuid(), '2025-05-11 14:30:00', '2025-05-11 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-MAY0025B', NULL, NULL, 'Payout for booking ...0388', '8a09da6c-0671-4f17-82d2-0c43342d0388'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0389 (Completed: 2025-05-11 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-05-11 17:00:00', '2025-05-11 17:00:00', 2, 549000.0, 1, 0, 'PAYID-MAY0026A', 'https://example.com/checkout/...?token=MAY0026', NULL, 'Payment for booking ...0389', '8a09da6c-0671-4f17-82d2-0c43342d0389'),
(gen_random_uuid(), '2025-05-11 17:00:00', '2025-05-11 17:00:00', 2, 494100.0, 1, 1, 'PAYOUT-MAY0026B', NULL, NULL, 'Payout for booking ...0389', '8a09da6c-0671-4f17-82d2-0c43342d0389'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0390 (Completed: 2025-05-12 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-05-12 10:00:00', '2025-05-12 10:00:00', 2, 379000.0, 1, 0, 'PAYID-MAY0027A', 'https://example.com/checkout/...?token=MAY0027', NULL, 'Payment for booking ...0390', '8a09da6c-0671-4f17-82d2-0c43342d0390'),
(gen_random_uuid(), '2025-05-12 10:00:00', '2025-05-12 10:00:00', 2, 341100.0, 1, 1, 'PAYOUT-MAY0027B', NULL, NULL, 'Payout for booking ...0390', '8a09da6c-0671-4f17-82d2-0c43342d0390'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0391 (Completed: 2025-05-12 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-05-12 14:30:00', '2025-05-12 14:30:00', 2, 229000.0, 1, 0, 'PAYID-MAY0028A', 'https://example.com/checkout/...?token=MAY0028', NULL, 'Payment for booking ...0391', '8a09da6c-0671-4f17-82d2-0c43342d0391'),
(gen_random_uuid(), '2025-05-12 14:30:00', '2025-05-12 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-MAY0028B', NULL, NULL, 'Payout for booking ...0391', '8a09da6c-0671-4f17-82d2-0c43342d0391'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0392 (Completed: 2025-05-12 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-05-12 17:00:00', '2025-05-12 17:00:00', 2, 699000.0, 1, 0, 'PAYID-MAY0029A', 'https://example.com/checkout/...?token=MAY0029', NULL, 'Payment for booking ...0392', '8a09da6c-0671-4f17-82d2-0c43342d0392'),
(gen_random_uuid(), '2025-05-12 17:00:00', '2025-05-12 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-MAY0029B', NULL, NULL, 'Payout for booking ...0392', '8a09da6c-0671-4f17-82d2-0c43342d0392'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0393 (Completed: 2025-05-13 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-05-13 11:00:00', '2025-05-13 11:00:00', 2, 449000.0, 1, 0, 'PAYID-MAY0030A', 'https://example.com/checkout/...?token=MAY0030', NULL, 'Payment for booking ...0393', '8a09da6c-0671-4f17-82d2-0c43342d0393'),
(gen_random_uuid(), '2025-05-13 11:00:00', '2025-05-13 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-MAY0030B', NULL, NULL, 'Payout for booking ...0393', '8a09da6c-0671-4f17-82d2-0c43342d0393'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0394 (Completed: 2025-05-13 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-05-13 14:30:00', '2025-05-13 14:30:00', 2, 299000.0, 1, 0, 'PAYID-MAY0031A', 'https://example.com/checkout/...?token=MAY0031', NULL, 'Payment for booking ...0394', '8a09da6c-0671-4f17-82d2-0c43342d0394'),
(gen_random_uuid(), '2025-05-13 14:30:00', '2025-05-13 14:30:00', 2, 269100.0, 1, 1, 'PAYOUT-MAY0031B', NULL, NULL, 'Payout for booking ...0394', '8a09da6c-0671-4f17-82d2-0c43342d0394'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0395 (Completed: 2025-05-13 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-05-13 17:00:00', '2025-05-13 17:00:00', 2, 279000.0, 1, 0, 'PAYID-MAY0032A', 'https://example.com/checkout/...?token=MAY0032', NULL, 'Payment for booking ...0395', '8a09da6c-0671-4f17-82d2-0c43342d0395'),
(gen_random_uuid(), '2025-05-13 17:00:00', '2025-05-13 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-MAY0032B', NULL, NULL, 'Payout for booking ...0395', '8a09da6c-0671-4f17-82d2-0c43342d0395'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0396 (Completed: 2025-05-14 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-05-14 10:00:00', '2025-05-14 10:00:00', 2, 329000.0, 1, 0, 'PAYID-MAY0033A', 'https://example.com/checkout/...?token=MAY0033', NULL, 'Payment for booking ...0396', '8a09da6c-0671-4f17-82d2-0c43342d0396'),
(gen_random_uuid(), '2025-05-14 10:00:00', '2025-05-14 10:00:00', 2, 296100.0, 1, 1, 'PAYOUT-MAY0033B', NULL, NULL, 'Payout for booking ...0396', '8a09da6c-0671-4f17-82d2-0c43342d0396'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0397 (Completed: 2025-05-14 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-05-14 14:30:00', '2025-05-14 14:30:00', 2, 249000.0, 1, 0, 'PAYID-MAY0034A', 'https://example.com/checkout/...?token=MAY0034', NULL, 'Payment for booking ...0397', '8a09da6c-0671-4f17-82d2-0c43342d0397'),
(gen_random_uuid(), '2025-05-14 14:30:00', '2025-05-14 14:30:00', 2, 224100.0, 1, 1, 'PAYOUT-MAY0034B', NULL, NULL, 'Payout for booking ...0397', '8a09da6c-0671-4f17-82d2-0c43342d0397'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0399 (Completed: 2025-05-15 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-05-15 11:00:00', '2025-05-15 11:00:00', 2, 429000.0, 1, 0, 'PAYID-MAY0035A', 'https://example.com/checkout/...?token=MAY0035', NULL, 'Payment for booking ...0399', '8a09da6c-0671-4f17-82d2-0c43342d0399'),
(gen_random_uuid(), '2025-05-15 11:00:00', '2025-05-15 11:00:00', 2, 386100.0, 1, 1, 'PAYOUT-MAY0035B', NULL, NULL, 'Payout for booking ...0399', '8a09da6c-0671-4f17-82d2-0c43342d0399'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0400 (Completed: 2025-05-15 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-05-15 14:30:00', '2025-05-15 14:30:00', 2, 199000.0, 1, 0, 'PAYID-MAY0036A', 'https://example.com/checkout/...?token=MAY0036', NULL, 'Payment for booking ...0400', '8a09da6c-0671-4f17-82d2-0c43342d0400'),
(gen_random_uuid(), '2025-05-15 14:30:00', '2025-05-15 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-MAY0036B', NULL, NULL, 'Payout for booking ...0400', '8a09da6c-0671-4f17-82d2-0c43342d0400'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0401 (Completed: 2025-05-15 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-05-15 17:00:00', '2025-05-15 17:00:00', 2, 379000.0, 1, 0, 'PAYID-MAY0037A', 'https://example.com/checkout/...?token=MAY0037', NULL, 'Payment for booking ...0401', '8a09da6c-0671-4f17-82d2-0c43342d0401'),
(gen_random_uuid(), '2025-05-15 17:00:00', '2025-05-15 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-MAY0037B', NULL, NULL, 'Payout for booking ...0401', '8a09da6c-0671-4f17-82d2-0c43342d0401'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0402 (Completed: 2025-05-16 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-05-16 10:00:00', '2025-05-16 10:00:00', 2, 699000.0, 1, 0, 'PAYID-MAY0038A', 'https://example.com/checkout/...?token=MAY0038', NULL, 'Payment for booking ...0402', '8a09da6c-0671-4f17-82d2-0c43342d0402'),
(gen_random_uuid(), '2025-05-16 10:00:00', '2025-05-16 10:00:00', 2, 629100.0, 1, 1, 'PAYOUT-MAY0038B', NULL, NULL, 'Payout for booking ...0402', '8a09da6c-0671-4f17-82d2-0c43342d0402'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0403 (Completed: 2025-05-16 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-05-16 14:30:00', '2025-05-16 14:30:00', 2, 149000.0, 1, 0, 'PAYID-MAY0039A', 'https://example.com/checkout/...?token=MAY0039', NULL, 'Payment for booking ...0403', '8a09da6c-0671-4f17-82d2-0c43342d0403'),
(gen_random_uuid(), '2025-05-16 14:30:00', '2025-05-16 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-MAY0039B', NULL, NULL, 'Payout for booking ...0403', '8a09da6c-0671-4f17-82d2-0c43342d0403'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0404 (Completed: 2025-05-16 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-05-16 17:00:00', '2025-05-16 17:00:00', 2, 269000.0, 1, 0, 'PAYID-MAY0040A', 'https://example.com/checkout/...?token=MAY0040', NULL, 'Payment for booking ...0404', '8a09da6c-0671-4f17-82d2-0c43342d0404'),
(gen_random_uuid(), '2025-05-16 17:00:00', '2025-05-16 17:00:00', 2, 242100.0, 1, 1, 'PAYOUT-MAY0040B', NULL, NULL, 'Payout for booking ...0404', '8a09da6c-0671-4f17-82d2-0c43342d0404'),

-- ---------------------------------
-- MAY 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0377 (Canceled: 2025-05-05 | Pkg: ...0005 | Price: 279000)
(gen_random_uuid(), '2025-05-05 14:30:00', '2025-05-05 14:31:00', 2, 279000.0, 3, 0, 'PAYID-MAY-CNL-01', 'https://example.com/checkout/...?token=MAY-CNL-01', NULL, 'Refund for booking ...0377', '8a09da6c-0671-4f17-82d2-0c43342d0377'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0398 (Canceled: 2025-05-12 | Pkg: ...0201 | Price: 369000)
(gen_random_uuid(), '2025-05-12 14:30:00', '2025-05-12 14:31:00', 2, 369000.0, 3, 0, 'PAYID-MAY-CNL-02', 'https://example.com/checkout/...?token=MAY-CNL-02', NULL, 'Refund for booking ...0398', '8a09da6c-0671-4f17-82d2-0c43342d0398'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0419 (Canceled: 2025-05-19 | Pkg: ...0102 | Price: 329000)
(gen_random_uuid(), '2025-05-19 14:30:00', '2025-05-19 14:31:00', 2, 329000.0, 3, 0, 'PAYID-MAY-CNL-03', 'https://example.com/checkout/...?token=MAY-CNL-03', NULL, 'Refund for booking ...0419', '8a09da6c-0671-4f17-82d2-0c43342d0419'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0437 (Canceled: 2025-05-25 | Pkg: ...0701 | Price: 149000)
(gen_random_uuid(), '2025-05-25 14:30:00', '2025-05-25 14:31:00', 2, 149000.0, 3, 0, 'PAYID-MAY-CNL-04', 'https://example.com/checkout/...?token=MAY-CNL-04', NULL, 'Refund for booking ...0437', '8a09da6c-0671-4f17-82d2-0c43342d0437'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0452 (Canceled: 2025-05-31 | Pkg: ...0011 | Price: 319000)
(gen_random_uuid(), '2025-05-31 09:30:00', '2025-05-31 09:31:00', 2, 319000.0, 3, 0, 'PAYID-MAY-CNL-05', 'https://example.com/checkout/...?token=MAY-CNL-05', NULL, 'Refund for booking ...0452', '8a09da6c-0671-4f17-82d2-0c43342d0452');

-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.17
-- Description: Insert initial data for booking payments (Jun & Jul 2025)
-- =============================================

-- Ghi chú Enum:
-- payment_method: 2 (Mặc định)
-- status: 1 (COMPLETED), 3 (REFUND)
-- payment_type: 0 (PAID_PACKAGE), 1 (RECEIVED_PACKAGE)
-- Logic Payout: amount = package.price - package.service_fee_amount
-- Logic Thời gian: Cả PAID và RECEIVED đều được tạo/cập nhật vào ngày booking hoàn thành (booking.updated_at)

INSERT INTO booking_payment (booking_payment_id, created_at, updated_at, payment_method, amount, status, payment_type, transaction_id, approval_url, failure_reason, extra_info, booking_id) VALUES

-- =============================================
-- BOOKING PAYMENTS - JUN 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0453 (Completed: 2025-06-03 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-06-03 10:00:00', '2025-06-03 10:00:00', 2, 329000.0, 1, 0, 'PAYID-JUN0001A', 'https://example.com/checkout/...?token=JUN0001', NULL, 'Payment for booking ...0453', '8a09da6c-0671-4f17-82d2-0c43342d0453'),
(gen_random_uuid(), '2025-06-03 10:00:00', '2025-06-03 10:00:00', 2, 296100.0, 1, 1, 'PAYOUT-JUN0001B', NULL, NULL, 'Payout for booking ...0453', '8a09da6c-0671-4f17-82d2-0c43342d0453'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0454 (Completed: 2025-06-03 | Pkg: ...0903 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-06-03 14:30:00', '2025-06-03 14:30:00', 2, 1499000.0, 1, 0, 'PAYID-JUN0002A', 'https://example.com/checkout/...?token=JUN0002', NULL, 'Payment for booking ...0454', '8a09da6c-0671-4f17-82d2-0c43342d0454'),
(gen_random_uuid(), '2025-06-03 14:30:00', '2025-06-03 14:30:00', 2, 1349100.0, 1, 1, 'PAYOUT-JUN0002B', NULL, NULL, 'Payout for booking ...0454', '8a09da6c-0671-4f17-82d2-0c43342d0454'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0455 (Completed: 2025-06-03 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-06-03 17:00:00', '2025-06-03 17:00:00', 2, 449000.0, 1, 0, 'PAYID-JUN0003A', 'https://example.com/checkout/...?token=JUN0003', NULL, 'Payment for booking ...0455', '8a09da6c-0671-4f17-82d2-0c43342d0455'),
(gen_random_uuid(), '2025-06-03 17:00:00', '2025-06-03 17:00:00', 2, 404100.0, 1, 1, 'PAYOUT-JUN0003B', NULL, NULL, 'Payout for booking ...0455', '8a09da6c-0671-4f17-82d2-0c43342d0455'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0456 (Completed: 2025-06-04 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-06-04 11:00:00', '2025-06-04 11:00:00', 2, 149000.0, 1, 0, 'PAYID-JUN0004A', 'https://example.com/checkout/...?token=JUN0004', NULL, 'Payment for booking ...0456', '8a09da6c-0671-4f17-82d2-0c43342d0456'),
(gen_random_uuid(), '2025-06-04 11:00:00', '2025-06-04 11:00:00', 2, 134100.0, 1, 1, 'PAYOUT-JUN0004B', NULL, NULL, 'Payout for booking ...0456', '8a09da6c-0671-4f17-82d2-0c43342d0456'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0457 (Completed: 2025-06-04 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-06-04 14:30:00', '2025-06-04 14:30:00', 2, 549000.0, 1, 0, 'PAYID-JUN0005A', 'https://example.com/checkout/...?token=JUN0005', NULL, 'Payment for booking ...0457', '8a09da6c-0671-4f17-82d2-0c43342d0457'),
(gen_random_uuid(), '2025-06-04 14:30:00', '2025-06-04 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-JUN0005B', NULL, NULL, 'Payout for booking ...0457', '8a09da6c-0671-4f17-82d2-0c43342d0457'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0458 (Completed: 2025-06-04 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-06-04 17:00:00', '2025-06-04 17:00:00', 2, 379000.0, 1, 0, 'PAYID-JUN0006A', 'https://example.com/checkout/...?token=JUN0006', NULL, 'Payment for booking ...0458', '8a09da6c-0671-4f17-82d2-0c43342d0458'),
(gen_random_uuid(), '2025-06-04 17:00:00', '2025-06-04 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-JUN0006B', NULL, NULL, 'Payout for booking ...0458', '8a09da6c-0671-4f17-82d2-0c43342d0458'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0459 (Completed: 2025-06-05 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-06-05 10:00:00', '2025-06-05 10:00:00', 2, 229000.0, 1, 0, 'PAYID-JUN0007A', 'https://example.com/checkout/...?token=JUN0007', NULL, 'Payment for booking ...0459', '8a09da6c-0671-4f17-82d2-0c43342d0459'),
(gen_random_uuid(), '2025-06-05 10:00:00', '2025-06-05 10:00:00', 2, 206100.0, 1, 1, 'PAYOUT-JUN0007B', NULL, NULL, 'Payout for booking ...0459', '8a09da6c-0671-4f17-82d2-0c43342d0459'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0460 (Completed: 2025-06-05 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-06-05 14:30:00', '2025-06-05 14:30:00', 2, 699000.0, 1, 0, 'PAYID-JUN0008A', 'https://example.com/checkout/...?token=JUN0008', NULL, 'Payment for booking ...0460', '8a09da6c-0671-4f17-82d2-0c43342d0460'),
(gen_random_uuid(), '2025-06-05 14:30:00', '2025-06-05 14:30:00', 2, 629100.0, 1, 1, 'PAYOUT-JUN0008B', NULL, NULL, 'Payout for booking ...0460', '8a09da6c-0671-4f17-82d2-0c43342d0460'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0462 (Completed: 2025-06-06 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-06-06 11:00:00', '2025-06-06 11:00:00', 2, 299000.0, 1, 0, 'PAYID-JUN0009A', 'https://example.com/checkout/...?token=JUN0009', NULL, 'Payment for booking ...0462', '8a09da6c-0671-4f17-82d2-0c43342d0462'),
(gen_random_uuid(), '2025-06-06 11:00:00', '2025-06-06 11:00:00', 2, 269100.0, 1, 1, 'PAYOUT-JUN0009B', NULL, NULL, 'Payout for booking ...0462', '8a09da6c-0671-4f17-82d2-0c43342d0462'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0463 (Completed: 2025-06-06 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-06-06 14:30:00', '2025-06-06 14:30:00', 2, 279000.0, 1, 0, 'PAYID-JUN0010A', 'https://example.com/checkout/...?token=JUN0010', NULL, 'Payment for booking ...0463', '8a09da6c-0671-4f17-82d2-0c43342d0463'),
(gen_random_uuid(), '2025-06-06 14:30:00', '2025-06-06 14:30:00', 2, 251100.0, 1, 1, 'PAYOUT-JUN0010B', NULL, NULL, 'Payout for booking ...0463', '8a09da6c-0671-4f17-82d2-0c43342d0463'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0464 (Completed: 2025-06-06 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-06-06 17:00:00', '2025-06-06 17:00:00', 2, 329000.0, 1, 0, 'PAYID-JUN0011A', 'https://example.com/checkout/...?token=JUN0011', NULL, 'Payment for booking ...0464', '8a09da6c-0671-4f17-82d2-0c43342d0464'),
(gen_random_uuid(), '2025-06-06 17:00:00', '2025-06-06 17:00:00', 2, 296100.0, 1, 1, 'PAYOUT-JUN0011B', NULL, NULL, 'Payout for booking ...0464', '8a09da6c-0671-4f17-82d2-0c43342d0464'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0465 (Completed: 2025-06-07 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-06-07 10:00:00', '2025-06-07 10:00:00', 2, 249000.0, 1, 0, 'PAYID-JUN0012A', 'https://example.com/checkout/...?token=JUN0012', NULL, 'Payment for booking ...0465', '8a09da6c-0671-4f17-82d2-0c43342d0465'),
(gen_random_uuid(), '2025-06-07 10:00:00', '2025-06-07 10:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JUN0012B', NULL, NULL, 'Payout for booking ...0465', '8a09da6c-0671-4f17-82d2-0c43342d0465'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0466 (Completed: 2025-06-07 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-06-07 14:30:00', '2025-06-07 14:30:00', 2, 369000.0, 1, 0, 'PAYID-JUN0013A', 'https://example.com/checkout/...?token=JUN0013', NULL, 'Payment for booking ...0466', '8a09da6c-0671-4f17-82d2-0c43342d0466'),
(gen_random_uuid(), '2025-06-07 14:30:00', '2025-06-07 14:30:00', 2, 332100.0, 1, 1, 'PAYOUT-JUN0013B', NULL, NULL, 'Payout for booking ...0466', '8a09da6c-0671-4f17-82d2-0c43342d0466'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0467 (Completed: 2025-06-07 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-06-07 17:00:00', '2025-06-07 17:00:00', 2, 429000.0, 1, 0, 'PAYID-JUN0014A', 'https://example.com/checkout/...?token=JUN0014', NULL, 'Payment for booking ...0467', '8a09da6c-0671-4f17-82d2-0c43342d0467'),
(gen_random_uuid(), '2025-06-07 17:00:00', '2025-06-07 17:00:00', 2, 386100.0, 1, 1, 'PAYOUT-JUN0014B', NULL, NULL, 'Payout for booking ...0467', '8a09da6c-0671-4f17-82d2-0c43342d0467'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0468 (Completed: 2025-06-08 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-06-08 11:00:00', '2025-06-08 11:00:00', 2, 199000.0, 1, 0, 'PAYID-JUN0015A', 'https://example.com/checkout/...?token=JUN0015', NULL, 'Payment for booking ...0468', '8a09da6c-0671-4f17-82d2-0c43342d0468'),
(gen_random_uuid(), '2025-06-08 11:00:00', '2025-06-08 11:00:00', 2, 179100.0, 1, 1, 'PAYOUT-JUN0015B', NULL, NULL, 'Payout for booking ...0468', '8a09da6c-0671-4f17-82d2-0c43342d0468'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0469 (Completed: 2025-06-08 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-06-08 14:30:00', '2025-06-08 14:30:00', 2, 379000.0, 1, 0, 'PAYID-JUN0016A', 'https://example.com/checkout/...?token=JUN0016', NULL, 'Payment for booking ...0469', '8a09da6c-0671-4f17-82d2-0c43342d0469'),
(gen_random_uuid(), '2025-06-08 14:30:00', '2025-06-08 14:30:00', 2, 341100.0, 1, 1, 'PAYOUT-JUN0016B', NULL, NULL, 'Payout for booking ...0469', '8a09da6c-0671-4f17-82d2-0c43342d0469'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0470 (Completed: 2025-06-08 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-06-08 17:00:00', '2025-06-08 17:00:00', 2, 699000.0, 1, 0, 'PAYID-JUN0017A', 'https://example.com/checkout/...?token=JUN0017', NULL, 'Payment for booking ...0470', '8a09da6c-0671-4f17-82d2-0c43342d0470'),
(gen_random_uuid(), '2025-06-08 17:00:00', '2025-06-08 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-JUN0017B', NULL, NULL, 'Payout for booking ...0470', '8a09da6c-0671-4f17-82d2-0c43342d0470'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0471 (Completed: 2025-06-09 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-06-09 10:00:00', '2025-06-09 10:00:00', 2, 149000.0, 1, 0, 'PAYID-JUN0018A', 'https://example.com/checkout/...?token=JUN0018', NULL, 'Payment for booking ...0471', '8a09da6c-0671-4f17-82d2-0c43342d0471'),
(gen_random_uuid(), '2025-06-09 10:00:00', '2025-06-09 10:00:00', 2, 134100.0, 1, 1, 'PAYOUT-JUN0018B', NULL, NULL, 'Payout for booking ...0471', '8a09da6c-0671-4f17-82d2-0c43342d0471'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0472 (Completed: 2025-06-09 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-06-09 14:30:00', '2025-06-09 14:30:00', 2, 269000.0, 1, 0, 'PAYID-JUN0019A', 'https://example.com/checkout/...?token=JUN0019', NULL, 'Payment for booking ...0472', '8a09da6c-0671-4f17-82d2-0c43342d0472'),
(gen_random_uuid(), '2025-06-09 14:30:00', '2025-06-09 14:30:00', 2, 242100.0, 1, 1, 'PAYOUT-JUN0019B', NULL, NULL, 'Payout for booking ...0472', '8a09da6c-0671-4f17-82d2-0c43342d0472'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0473 (Completed: 2025-06-09 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-06-09 17:00:00', '2025-06-09 17:00:00', 2, 499000.0, 1, 0, 'PAYID-JUN0020A', 'https://example.com/checkout/...?token=JUN0020', NULL, 'Payment for booking ...0473', '8a09da6c-0671-4f17-82d2-0c43342d0473'),
(gen_random_uuid(), '2025-06-09 17:00:00', '2025-06-09 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JUN0020B', NULL, NULL, 'Payout for booking ...0473', '8a09da6c-0671-4f17-82d2-0c43342d0473'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0474 (Completed: 2025-06-10 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-06-10 11:00:00', '2025-06-10 11:00:00', 2, 549000.0, 1, 0, 'PAYID-JUN0021A', 'https://example.com/checkout/...?token=JUN0021', NULL, 'Payment for booking ...0474', '8a09da6c-0671-4f17-82d2-0c43342d0474'),
(gen_random_uuid(), '2025-06-10 11:00:00', '2025-06-10 11:00:00', 2, 494100.0, 1, 1, 'PAYOUT-JUN0021B', NULL, NULL, 'Payout for booking ...0474', '8a09da6c-0671-4f17-82d2-0c43342d0474'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0475 (Completed: 2025-06-10 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-06-10 14:30:00', '2025-06-10 14:30:00', 2, 899000.0, 1, 0, 'PAYID-JUN0022A', 'https://example.com/checkout/...?token=JUN0022', NULL, 'Payment for booking ...0475', '8a09da6c-0671-4f17-82d2-0c43342d0475'),
(gen_random_uuid(), '2025-06-10 14:30:00', '2025-06-10 14:30:00', 2, 809100.0, 1, 1, 'PAYOUT-JUN0022B', NULL, NULL, 'Payout for booking ...0475', '8a09da6c-0671-4f17-82d2-0c43342d0475'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0476 (Completed: 2025-06-10 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-06-10 17:00:00', '2025-06-10 17:00:00', 2, 499000.0, 1, 0, 'PAYID-JUN0023A', 'https://example.com/checkout/...?token=JUN0023', NULL, 'Payment for booking ...0476', '8a09da6c-0671-4f17-82d2-0c43342d0476'),
(gen_random_uuid(), '2025-06-10 17:00:00', '2025-06-10 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JUN0023B', NULL, NULL, 'Payout for booking ...0476', '8a09da6c-0671-4f17-82d2-0c43342d0476'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0477 (Completed: 2025-06-11 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-06-11 10:00:00', '2025-06-11 10:00:00', 2, 399000.0, 1, 0, 'PAYID-JUN0024A', 'https://example.com/checkout/...?token=JUN0024', NULL, 'Payment for booking ...0477', '8a09da6c-0671-4f17-82d2-0c43342d0477'),
(gen_random_uuid(), '2025-06-11 10:00:00', '2025-06-11 10:00:00', 2, 359100.0, 1, 1, 'PAYOUT-JUN0024B', NULL, NULL, 'Payout for booking ...0477', '8a09da6c-0671-4f17-82d2-0c43342d0477'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0478 (Completed: 2025-06-11 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-06-11 14:30:00', '2025-06-11 14:30:00', 2, 229000.0, 1, 0, 'PAYID-JUN0025A', 'https://example.com/checkout/...?token=JUN0025', NULL, 'Payment for booking ...0478', '8a09da6c-0671-4f17-82d2-0c43342d0478'),
(gen_random_uuid(), '2025-06-11 14:30:00', '2025-06-11 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-JUN0025B', NULL, NULL, 'Payout for booking ...0478', '8a09da6c-0671-4f17-82d2-0c43342d0478'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0479 (Completed: 2025-06-11 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-06-11 17:00:00', '2025-06-11 17:00:00', 2, 279000.0, 1, 0, 'PAYID-JUN0026A', 'https://example.com/checkout/...?token=JUN0026', NULL, 'Payment for booking ...0479', '8a09da6c-0671-4f17-82d2-0c43342d0479'),
(gen_random_uuid(), '2025-06-11 17:00:00', '2025-06-11 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-JUN0026B', NULL, NULL, 'Payout for booking ...0479', '8a09da6c-0671-4f17-82d2-0c43342d0479'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0480 (Completed: 2025-06-12 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-06-12 11:00:00', '2025-06-12 11:00:00', 2, 219000.0, 1, 0, 'PAYID-JUN0027A', 'https://example.com/checkout/...?token=JUN0027', NULL, 'Payment for booking ...0480', '8a09da6c-0671-4f17-82d2-0c43342d0480'),
(gen_random_uuid(), '2025-06-12 11:00:00', '2025-06-12 11:00:00', 2, 197100.0, 1, 1, 'PAYOUT-JUN0027B', NULL, NULL, 'Payout for booking ...0480', '8a09da6c-0671-4f17-82d2-0c43342d0480'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0481 (Completed: 2025-06-12 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-06-12 14:30:00', '2025-06-12 14:30:00', 2, 339000.0, 1, 0, 'PAYID-JUN0028A', 'https://example.com/checkout/...?token=JUN0028', NULL, 'Payment for booking ...0481', '8a09da6c-0671-4f17-82d2-0c43342d0481'),
(gen_random_uuid(), '2025-06-12 14:30:00', '2025-06-12 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-JUN0028B', NULL, NULL, 'Payout for booking ...0481', '8a09da6c-0671-4f17-82d2-0c43342d0481'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0482 (Completed: 2025-06-12 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-06-12 17:00:00', '2025-06-12 17:00:00', 2, 799000.0, 1, 0, 'PAYID-JUN0029A', 'https://example.com/checkout/...?token=JUN0029', NULL, 'Payment for booking ...0482', '8a09da6c-0671-4f17-82d2-0c43342d0482'),
(gen_random_uuid(), '2025-06-12 17:00:00', '2025-06-12 17:00:00', 2, 719100.0, 1, 1, 'PAYOUT-JUN0029B', NULL, NULL, 'Payout for booking ...0482', '8a09da6c-0671-4f17-82d2-0c43342d0482'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0483 (Completed: 2025-06-13 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-06-13 10:00:00', '2025-06-13 10:00:00', 2, 269000.0, 1, 0, 'PAYID-JUN0030A', 'https://example.com/checkout/...?token=JUN0030', NULL, 'Payment for booking ...0483', '8a09da6c-0671-4f17-82d2-0c43342d0483'),
(gen_random_uuid(), '2025-06-13 10:00:00', '2025-06-13 10:00:00', 2, 242100.0, 1, 1, 'PAYOUT-JUN0030B', NULL, NULL, 'Payout for booking ...0483', '8a09da6c-0671-4f17-82d2-0c43342d0483'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0484 (Completed: 2025-06-13 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-06-13 14:30:00', '2025-06-13 14:30:00', 2, 599000.0, 1, 0, 'PAYID-JUN0031A', 'https://example.com/checkout/...?token=JUN0031', NULL, 'Payment for booking ...0484', '8a09da6c-0671-4f17-82d2-0c43342d0484'),
(gen_random_uuid(), '2025-06-13 14:30:00', '2025-06-13 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-JUN0031B', NULL, NULL, 'Payout for booking ...0484', '8a09da6c-0671-4f17-82d2-0c43342d0484'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0485 (Completed: 2025-06-13 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-06-13 17:00:00', '2025-06-13 17:00:00', 2, 249000.0, 1, 0, 'PAYID-JUN0032A', 'https://example.com/checkout/...?token=JUN0032', NULL, 'Payment for booking ...0485', '8a09da6c-0671-4f17-82d2-0c43342d0485'),
(gen_random_uuid(), '2025-06-13 17:00:00', '2025-06-13 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JUN0032B', NULL, NULL, 'Payout for booking ...0485', '8a09da6c-0671-4f17-82d2-0c43342d0485'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0486 (Completed: 2025-06-14 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-06-14 11:00:00', '2025-06-14 11:00:00', 2, 319000.0, 1, 0, 'PAYID-JUN0033A', 'https://example.com/checkout/...?token=JUN0033', NULL, 'Payment for booking ...0486', '8a09da6c-0671-4f17-82d2-0c43342d0486'),
(gen_random_uuid(), '2025-06-14 11:00:00', '2025-06-14 11:00:00', 2, 287100.0, 1, 1, 'PAYOUT-JUN0033B', NULL, NULL, 'Payout for booking ...0486', '8a09da6c-0671-4f17-82d2-0c43342d0486'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0487 (Completed: 2025-06-14 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-06-14 14:30:00', '2025-06-14 14:30:00', 2, 329000.0, 1, 0, 'PAYID-JUN0034A', 'https://example.com/checkout/...?token=JUN0034', NULL, 'Payment for booking ...0487', '8a09da6c-0671-4f17-82d2-0c43342d0487'),
(gen_random_uuid(), '2025-06-14 14:30:00', '2025-06-14 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-JUN0034B', NULL, NULL, 'Payout for booking ...0487', '8a09da6c-0671-4f17-82d2-0c43342d0487'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0489 (Completed: 2025-06-15 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-06-15 10:00:00', '2025-06-15 10:00:00', 2, 449000.0, 1, 0, 'PAYID-JUN0035A', 'https://example.com/checkout/...?token=JUN0035', NULL, 'Payment for booking ...0489', '8a09da6c-0671-4f17-82d2-0c43342d0489'),
(gen_random_uuid(), '2025-06-15 10:00:00', '2025-06-15 10:00:00', 2, 404100.0, 1, 1, 'PAYOUT-JUN0035B', NULL, NULL, 'Payout for booking ...0489', '8a09da6c-0671-4f17-82d2-0c43342d0489'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0490 (Completed: 2025-06-15 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-06-15 14:30:00', '2025-06-15 14:30:00', 2, 149000.0, 1, 0, 'PAYID-JUN0036A', 'https://example.com/checkout/...?token=JUN0036', NULL, 'Payment for booking ...0490', '8a09da6c-0671-4f17-82d2-0c43342d0490'),
(gen_random_uuid(), '2025-06-15 14:30:00', '2025-06-15 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-JUN0036B', NULL, NULL, 'Payout for booking ...0490', '8a09da6c-0671-4f17-82d2-0c43342d0490'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0491 (Completed: 2025-06-15 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-06-15 17:00:00', '2025-06-15 17:00:00', 2, 549000.0, 1, 0, 'PAYID-JUN0037A', 'https://example.com/checkout/...?token=JUN0037', NULL, 'Payment for booking ...0491', '8a09da6c-0671-4f17-82d2-0c43342d0491'),
(gen_random_uuid(), '2025-06-15 17:00:00', '2025-06-15 17:00:00', 2, 494100.0, 1, 1, 'PAYOUT-JUN0037B', NULL, NULL, 'Payout for booking ...0491', '8a09da6c-0671-4f17-82d2-0c43342d0491'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0492 (Completed: 2025-06-16 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-06-16 11:00:00', '2025-06-16 11:00:00', 2, 379000.0, 1, 0, 'PAYID-JUN0038A', 'https://example.com/checkout/...?token=JUN0038', NULL, 'Payment for booking ...0492', '8a09da6c-0671-4f17-82d2-0c43342d0492'),
(gen_random_uuid(), '2025-06-16 11:00:00', '2025-06-16 11:00:00', 2, 341100.0, 1, 1, 'PAYOUT-JUN0038B', NULL, NULL, 'Payout for booking ...0492', '8a09da6c-0671-4f17-82d2-0c43342d0492'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0493 (Completed: 2025-06-16 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-06-16 14:30:00', '2025-06-16 14:30:00', 2, 229000.0, 1, 0, 'PAYID-JUN0039A', 'https://example.com/checkout/...?token=JUN0039', NULL, 'Payment for booking ...0493', '8a09da6c-0671-4f17-82d2-0c43342d0493'),
(gen_random_uuid(), '2025-06-16 14:30:00', '2025-06-16 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-JUN0039B', NULL, NULL, 'Payout for booking ...0493', '8a09da6c-0671-4f17-82d2-0c43342d0493'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0494 (Completed: 2025-06-16 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-06-16 17:00:00', '2025-06-16 17:00:00', 2, 699000.0, 1, 0, 'PAYID-JUN0040A', 'https://example.com/checkout/...?token=JUN0040', NULL, 'Payment for booking ...0494', '8a09da6c-0671-4f17-82d2-0c43342d0494'),
(gen_random_uuid(), '2025-06-16 17:00:00', '2025-06-16 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-JUN0040B', NULL, NULL, 'Payout for booking ...0494', '8a09da6c-0671-4f17-82d2-0c43342d0494'),

-- ---------------------------------
-- JUN 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0461 (Canceled: 2025-06-03 | Pkg: ...0801 | Price: 449000)
(gen_random_uuid(), '2025-06-03 14:30:00', '2025-06-03 14:31:00', 2, 449000.0, 3, 0, 'PAYID-JUN-CNL-01', 'https://example.com/checkout/...?token=JUN-CNL-01', NULL, 'Refund for booking ...0461', '8a09da6c-0671-4f17-82d2-0c43342d0461'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0488 (Canceled: 2025-06-12 | Pkg: ...0903 | Price: 1499000)
(gen_random_uuid(), '2025-06-12 14:30:00', '2025-06-12 14:31:00', 2, 1499000.0, 3, 0, 'PAYID-JUN-CNL-02', 'https://example.com/checkout/...?token=JUN-CNL-02', NULL, 'Refund for booking ...0488', '8a09da6c-0671-4f17-82d2-0c43342d0488'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0504 (Canceled: 2025-06-18 | Pkg: ...0601 | Price: 699000)
(gen_random_uuid(), '2025-06-18 09:30:00', '2025-06-18 09:31:00', 2, 699000.0, 3, 0, 'PAYID-JUN-CNL-03', 'https://example.com/checkout/...?token=JUN-CNL-03', NULL, 'Refund for booking ...0504', '8a09da6c-0671-4f17-82d2-0c43342d0504'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0521 (Canceled: 2025-06-23 | Pkg: ...0102 | Price: 329000)
(gen_random_uuid(), '2025-06-23 14:30:00', '2025-06-23 14:31:00', 2, 329000.0, 3, 0, 'PAYID-JUN-CNL-04', 'https://example.com/checkout/...?token=JUN-CNL-04', NULL, 'Refund for booking ...0521', '8a09da6c-0671-4f17-82d2-0c43342d0521'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0533 (Canceled: 2025-06-27 | Pkg: ...0101 | Price: 249000)
(gen_random_uuid(), '2025-06-27 14:30:00', '2025-06-27 14:31:00', 2, 249000.0, 3, 0, 'PAYID-JUN-CNL-05', 'https://example.com/checkout/...?token=JUN-CNL-05', NULL, 'Refund for booking ...0533', '8a09da6c-0671-4f17-82d2-0c43342d0533'),

-- =============================================
-- BOOKING PAYMENTS - JUL 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0542 (Completed: 2025-07-03 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-07-03 10:00:00', '2025-07-03 10:00:00', 2, 549000.0, 1, 0, 'PAYID-JUL0001A', 'https://example.com/checkout/...?token=JUL0001', NULL, 'Payment for booking ...0542', '8a09da6c-0671-4f17-82d2-0c43342d0542'),
(gen_random_uuid(), '2025-07-03 10:00:00', '2025-07-03 10:00:00', 2, 494100.0, 1, 1, 'PAYOUT-JUL0001B', NULL, NULL, 'Payout for booking ...0542', '8a09da6c-0671-4f17-82d2-0c43342d0542'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0543 (Completed: 2025-07-03 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-07-03 14:30:00', '2025-07-03 14:30:00', 2, 899000.0, 1, 0, 'PAYID-JUL0002A', 'https://example.com/checkout/...?token=JUL0002', NULL, 'Payment for booking ...0543', '8a09da6c-0671-4f17-82d2-0c43342d0543'),
(gen_random_uuid(), '2025-07-03 14:30:00', '2025-07-03 14:30:00', 2, 809100.0, 1, 1, 'PAYOUT-JUL0002B', NULL, NULL, 'Payout for booking ...0543', '8a09da6c-0671-4f17-82d2-0c43342d0543'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0544 (Completed: 2025-07-03 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-07-03 17:00:00', '2025-07-03 17:00:00', 2, 499000.0, 1, 0, 'PAYID-JUL0003A', 'https://example.com/checkout/...?token=JUL0003', NULL, 'Payment for booking ...0544', '8a09da6c-0671-4f17-82d2-0c43342d0544'),
(gen_random_uuid(), '2025-07-03 17:00:00', '2025-07-03 17:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JUL0003B', NULL, NULL, 'Payout for booking ...0544', '8a09da6c-0671-4f17-82d2-0c43342d0544'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0545 (Completed: 2025-07-04 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-07-04 11:00:00', '2025-07-04 11:00:00', 2, 399000.0, 1, 0, 'PAYID-JUL0004A', 'https://example.com/checkout/...?token=JUL0004', NULL, 'Payment for booking ...0545', '8a09da6c-0671-4f17-82d2-0c43342d0545'),
(gen_random_uuid(), '2025-07-04 11:00:00', '2025-07-04 11:00:00', 2, 359100.0, 1, 1, 'PAYOUT-JUL0004B', NULL, NULL, 'Payout for booking ...0545', '8a09da6c-0671-4f17-82d2-0c43342d0545'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0546 (Completed: 2025-07-04 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-07-04 14:30:00', '2025-07-04 14:30:00', 2, 229000.0, 1, 0, 'PAYID-JUL0005A', 'https://example.com/checkout/...?token=JUL0005', NULL, 'Payment for booking ...0546', '8a09da6c-0671-4f17-82d2-0c43342d0546'),
(gen_random_uuid(), '2025-07-04 14:30:00', '2025-07-04 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-JUL0005B', NULL, NULL, 'Payout for booking ...0546', '8a09da6c-0671-4f17-82d2-0c43342d0546'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0547 (Completed: 2025-07-04 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-07-04 17:00:00', '2025-07-04 17:00:00', 2, 279000.0, 1, 0, 'PAYID-JUL0006A', 'https://example.com/checkout/...?token=JUL0006', NULL, 'Payment for booking ...0547', '8a09da6c-0671-4f17-82d2-0c43342d0547'),
(gen_random_uuid(), '2025-07-04 17:00:00', '2025-07-04 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-JUL0006B', NULL, NULL, 'Payout for booking ...0547', '8a09da6c-0671-4f17-82d2-0c43342d0547'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0548 (Completed: 2025-07-05 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-07-05 10:00:00', '2025-07-05 10:00:00', 2, 219000.0, 1, 0, 'PAYID-JUL0007A', 'https://example.com/checkout/...?token=JUL0007', NULL, 'Payment for booking ...0548', '8a09da6c-0671-4f17-82d2-0c43342d0548'),
(gen_random_uuid(), '2025-07-05 10:00:00', '2025-07-05 10:00:00', 2, 197100.0, 1, 1, 'PAYOUT-JUL0007B', NULL, NULL, 'Payout for booking ...0548', '8a09da6c-0671-4f17-82d2-0c43342d0548'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0549 (Completed: 2025-07-05 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-07-05 14:30:00', '2025-07-05 14:30:00', 2, 339000.0, 1, 0, 'PAYID-JUL0008A', 'https://example.com/checkout/...?token=JUL0008', NULL, 'Payment for booking ...0549', '8a09da6c-0671-4f17-82d2-0c43342d0549'),
(gen_random_uuid(), '2025-07-05 14:30:00', '2025-07-05 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-JUL0008B', NULL, NULL, 'Payout for booking ...0549', '8a09da6c-0671-4f17-82d2-0c43342d0549'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0551 (Completed: 2025-07-06 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-07-06 11:00:00', '2025-07-06 11:00:00', 2, 269000.0, 1, 0, 'PAYID-JUL0009A', 'https://example.com/checkout/...?token=JUL0009', NULL, 'Payment for booking ...0551', '8a09da6c-0671-4f17-82d2-0c43342d0551'),
(gen_random_uuid(), '2025-07-06 11:00:00', '2025-07-06 11:00:00', 2, 242100.0, 1, 1, 'PAYOUT-JUL0009B', NULL, NULL, 'Payout for booking ...0551', '8a09da6c-0671-4f17-82d2-0c43342d0551'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0552 (Completed: 2025-07-06 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-07-06 14:30:00', '2025-07-06 14:30:00', 2, 599000.0, 1, 0, 'PAYID-JUL0010A', 'https://example.com/checkout/...?token=JUL0010', NULL, 'Payment for booking ...0552', '8a09da6c-0671-4f17-82d2-0c43342d0552'),
(gen_random_uuid(), '2025-07-06 14:30:00', '2025-07-06 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-JUL0010B', NULL, NULL, 'Payout for booking ...0552', '8a09da6c-0671-4f17-82d2-0c43342d0552'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0553 (Completed: 2025-07-06 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-07-06 17:00:00', '2025-07-06 17:00:00', 2, 249000.0, 1, 0, 'PAYID-JUL0011A', 'https://example.com/checkout/...?token=JUL0011', NULL, 'Payment for booking ...0553', '8a09da6c-0671-4f17-82d2-0c43342d0553'),
(gen_random_uuid(), '2025-07-06 17:00:00', '2025-07-06 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-JUL0011B', NULL, NULL, 'Payout for booking ...0553', '8a09da6c-0671-4f17-82d2-0c43342d0553'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0554 (Completed: 2025-07-07 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-07-07 10:00:00', '2025-07-07 10:00:00', 2, 319000.0, 1, 0, 'PAYID-JUL0012A', 'https://example.com/checkout/...?token=JUL0012', NULL, 'Payment for booking ...0554', '8a09da6c-0671-4f17-82d2-0c43342d0554'),
(gen_random_uuid(), '2025-07-07 10:00:00', '2025-07-07 10:00:00', 2, 287100.0, 1, 1, 'PAYOUT-JUL0012B', NULL, NULL, 'Payout for booking ...0554', '8a09da6c-0671-4f17-82d2-0c43342d0554'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0555 (Completed: 2025-07-07 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-07-07 14:30:00', '2025-07-07 14:30:00', 2, 329000.0, 1, 0, 'PAYID-JUL0013A', 'https://example.com/checkout/...?token=JUL0013', NULL, 'Payment for booking ...0555', '8a09da6c-0671-4f17-82d2-0c43342d0555'),
(gen_random_uuid(), '2025-07-07 14:30:00', '2025-07-07 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-JUL0013B', NULL, NULL, 'Payout for booking ...0555', '8a09da6c-0671-4f17-82d2-0c43342d0555'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0556 (Completed: 2025-07-07 | Pkg: ...0903 | Price: 1499000 | Payout: 1349100)
(gen_random_uuid(), '2025-07-07 17:00:00', '2025-07-07 17:00:00', 2, 1499000.0, 1, 0, 'PAYID-JUL0014A', 'https://example.com/checkout/...?token=JUL0014', NULL, 'Payment for booking ...0556', '8a09da6c-0671-4f17-82d2-0c43342d0556'),
(gen_random_uuid(), '2025-07-07 17:00:00', '2025-07-07 17:00:00', 2, 1349100.0, 1, 1, 'PAYOUT-JUL0014B', NULL, NULL, 'Payout for booking ...0556', '8a09da6c-0671-4f17-82d2-0c43342d0556'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0557 (Completed: 2025-07-08 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-07-08 11:00:00', '2025-07-08 11:00:00', 2, 449000.0, 1, 0, 'PAYID-JUL0015A', 'https://example.com/checkout/...?token=JUL0015', NULL, 'Payment for booking ...0557', '8a09da6c-0671-4f17-82d2-0c43342d0557'),
(gen_random_uuid(), '2025-07-08 11:00:00', '2025-07-08 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-JUL0015B', NULL, NULL, 'Payout for booking ...0557', '8a09da6c-0671-4f17-82d2-0c43342d0557'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0558 (Completed: 2025-07-08 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-07-08 14:30:00', '2025-07-08 14:30:00', 2, 149000.0, 1, 0, 'PAYID-JUL0016A', 'https://example.com/checkout/...?token=JUL0016', NULL, 'Payment for booking ...0558', '8a09da6c-0671-4f17-82d2-0c43342d0558'),
(gen_random_uuid(), '2025-07-08 14:30:00', '2025-07-08 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-JUL0016B', NULL, NULL, 'Payout for booking ...0558', '8a09da6c-0671-4f17-82d2-0c43342d0558'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0559 (Completed: 2025-07-08 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-07-08 17:00:00', '2025-07-08 17:00:00', 2, 549000.0, 1, 0, 'PAYID-JUL0017A', 'https://example.com/checkout/...?token=JUL0017', NULL, 'Payment for booking ...0559', '8a09da6c-0671-4f17-82d2-0c43342d0559'),
(gen_random_uuid(), '2025-07-08 17:00:00', '2025-07-08 17:00:00', 2, 494100.0, 1, 1, 'PAYOUT-JUL0017B', NULL, NULL, 'Payout for booking ...0559', '8a09da6c-0671-4f17-82d2-0c43342d0559'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0560 (Completed: 2025-07-09 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-07-09 10:00:00', '2025-07-09 10:00:00', 2, 379000.0, 1, 0, 'PAYID-JUL0018A', 'https://example.com/checkout/...?token=JUL0018', NULL, 'Payment for booking ...0560', '8a09da6c-0671-4f17-82d2-0c43342d0560'),
(gen_random_uuid(), '2025-07-09 10:00:00', '2025-07-09 10:00:00', 2, 341100.0, 1, 1, 'PAYOUT-JUL0018B', NULL, NULL, 'Payout for booking ...0560', '8a09da6c-0671-4f17-82d2-0c43342d0560'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0561 (Completed: 2025-07-09 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-07-09 14:30:00', '2025-07-09 14:30:00', 2, 229000.0, 1, 0, 'PAYID-JUL0019A', 'https://example.com/checkout/...?token=JUL0019', NULL, 'Payment for booking ...0561', '8a09da6c-0671-4f17-82d2-0c43342d0561'),
(gen_random_uuid(), '2025-07-09 14:30:00', '2025-07-09 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-JUL0019B', NULL, NULL, 'Payout for booking ...0561', '8a09da6c-0671-4f17-82d2-0c43342d0561'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0562 (Completed: 2025-07-09 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-07-09 17:00:00', '2025-07-09 17:00:00', 2, 699000.0, 1, 0, 'PAYID-JUL0020A', 'https://example.com/checkout/...?token=JUL0020', NULL, 'Payment for booking ...0562', '8a09da6c-0671-4f17-82d2-0c43342d0562'),
(gen_random_uuid(), '2025-07-09 17:00:00', '2025-07-09 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-JUL0020B', NULL, NULL, 'Payout for booking ...0562', '8a09da6c-0671-4f17-82d2-0c43342d0562'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0563 (Completed: 2025-07-10 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-07-10 11:00:00', '2025-07-10 11:00:00', 2, 449000.0, 1, 0, 'PAYID-JUL0021A', 'https://example.com/checkout/...?token=JUL0021', NULL, 'Payment for booking ...0563', '8a09da6c-0671-4f17-82d2-0c43342d0563'),
(gen_random_uuid(), '2025-07-10 11:00:00', '2025-07-10 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-JUL0021B', NULL, NULL, 'Payout for booking ...0563', '8a09da6c-0671-4f17-82d2-0c43342d0563'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0564 (Completed: 2025-07-10 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-07-10 14:30:00', '2025-07-10 14:30:00', 2, 299000.0, 1, 0, 'PAYID-JUL0022A', 'https://example.com/checkout/...?token=JUL0022', NULL, 'Payment for booking ...0564', '8a09da6c-0671-4f17-82d2-0c43342d0564'),
(gen_random_uuid(), '2025-07-10 14:30:00', '2025-07-10 14:30:00', 2, 269100.0, 1, 1, 'PAYOUT-JUL0022B', NULL, NULL, 'Payout for booking ...0564', '8a09da6c-0671-4f17-82d2-0c43342d0564'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0565 (Completed: 2025-07-10 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-07-10 17:00:00', '2025-07-10 17:00:00', 2, 279000.0, 1, 0, 'PAYID-JUL0023A', 'https://example.com/checkout/...?token=JUL0023', NULL, 'Payment for booking ...0565', '8a09da6c-0671-4f17-82d2-0c43342d0565'),
(gen_random_uuid(), '2025-07-10 17:00:00', '2025-07-10 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-JUL0023B', NULL, NULL, 'Payout for booking ...0565', '8a09da6c-0671-4f17-82d2-0c43342d0565'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0566 (Completed: 2025-07-11 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-07-11 10:00:00', '2025-07-11 10:00:00', 2, 329000.0, 1, 0, 'PAYID-JUL0024A', 'https://example.com/checkout/...?token=JUL0024', NULL, 'Payment for booking ...0566', '8a09da6c-0671-4f17-82d2-0c43342d0566'),
(gen_random_uuid(), '2025-07-11 10:00:00', '2025-07-11 10:00:00', 2, 296100.0, 1, 1, 'PAYOUT-JUL0024B', NULL, NULL, 'Payout for booking ...0566', '8a09da6c-0671-4f17-82d2-0c43342d0566'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0567 (Completed: 2025-07-11 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-07-11 14:30:00', '2025-07-11 14:30:00', 2, 249000.0, 1, 0, 'PAYID-JUL0025A', 'https://example.com/checkout/...?token=JUL0025', NULL, 'Payment for booking ...0567', '8a09da6c-0671-4f17-82d2-0c43342d0567'),
(gen_random_uuid(), '2025-07-11 14:30:00', '2025-07-11 14:30:00', 2, 224100.0, 1, 1, 'PAYOUT-JUL0025B', NULL, NULL, 'Payout for booking ...0567', '8a09da6c-0671-4f17-82d2-0c43342d0567'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0568 (Completed: 2025-07-11 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-07-11 17:00:00', '2025-07-11 17:00:00', 2, 369000.0, 1, 0, 'PAYID-JUL0026A', 'https://example.com/checkout/...?token=JUL0026', NULL, 'Payment for booking ...0568', '8a09da6c-0671-4f17-82d2-0c43342d0568'),
(gen_random_uuid(), '2025-07-11 17:00:00', '2025-07-11 17:00:00', 2, 332100.0, 1, 1, 'PAYOUT-JUL0026B', NULL, NULL, 'Payout for booking ...0568', '8a09da6c-0671-4f17-82d2-0c43342d0568'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0569 (Completed: 2025-07-12 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-07-12 11:00:00', '2025-07-12 11:00:00', 2, 429000.0, 1, 0, 'PAYID-JUL0027A', 'https://example.com/checkout/...?token=JUL0027', NULL, 'Payment for booking ...0569', '8a09da6c-0671-4f17-82d2-0c43342d0569'),
(gen_random_uuid(), '2025-07-12 11:00:00', '2025-07-12 11:00:00', 2, 386100.0, 1, 1, 'PAYOUT-JUL0027B', NULL, NULL, 'Payout for booking ...0569', '8a09da6c-0671-4f17-82d2-0c43342d0569'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0570 (Completed: 2025-07-12 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-07-12 14:30:00', '2025-07-12 14:30:00', 2, 199000.0, 1, 0, 'PAYID-JUL0028A', 'https://example.com/checkout/...?token=JUL0028', NULL, 'Payment for booking ...0570', '8a09da6c-0671-4f17-82d2-0c43342d0570'),
(gen_random_uuid(), '2025-07-12 14:30:00', '2025-07-12 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-JUL0028B', NULL, NULL, 'Payout for booking ...0570', '8a09da6c-0671-4f17-82d2-0c43342d0570'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0572 (Completed: 2025-07-13 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-07-13 10:00:00', '2025-07-13 10:00:00', 2, 699000.0, 1, 0, 'PAYID-JUL0029A', 'https://example.com/checkout/...?token=JUL0029', NULL, 'Payment for booking ...0572', '8a09da6c-0671-4f17-82d2-0c43342d0572'),
(gen_random_uuid(), '2025-07-13 10:00:00', '2025-07-13 10:00:00', 2, 629100.0, 1, 1, 'PAYOUT-JUL0029B', NULL, NULL, 'Payout for booking ...0572', '8a09da6c-0671-4f17-82d2-0c43342d0572'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0573 (Completed: 2025-07-13 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-07-13 14:30:00', '2025-07-13 14:30:00', 2, 149000.0, 1, 0, 'PAYID-JUL0030A', 'https://example.com/checkout/...?token=JUL0030', NULL, 'Payment for booking ...0573', '8a09da6c-0671-4f17-82d2-0c43342d0573'),
(gen_random_uuid(), '2025-07-13 14:30:00', '2025-07-13 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-JUL0030B', NULL, NULL, 'Payout for booking ...0573', '8a09da6c-0671-4f17-82d2-0c43342d0573'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0574 (Completed: 2025-07-13 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-07-13 17:00:00', '2025-07-13 17:00:00', 2, 269000.0, 1, 0, 'PAYID-JUL0031A', 'https://example.com/checkout/...?token=JUL0031', NULL, 'Payment for booking ...0574', '8a09da6c-0671-4f17-82d2-0c43342d0574'),
(gen_random_uuid(), '2025-07-13 17:00:00', '2025-07-13 17:00:00', 2, 242100.0, 1, 1, 'PAYOUT-JUL0031B', NULL, NULL, 'Payout for booking ...0574', '8a09da6c-0671-4f17-82d2-0c43342d0574'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0575 (Completed: 2025-07-14 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-07-14 11:00:00', '2025-07-14 11:00:00', 2, 499000.0, 1, 0, 'PAYID-JUL0032A', 'https://example.com/checkout/...?token=JUL0032', NULL, 'Payment for booking ...0575', '8a09da6c-0671-4f17-82d2-0c43342d0575'),
(gen_random_uuid(), '2025-07-14 11:00:00', '2025-07-14 11:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JUL0032B', NULL, NULL, 'Payout for booking ...0575', '8a09da6c-0671-4f17-82d2-0c43342d0575'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0576 (Completed: 2025-07-14 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-07-14 14:30:00', '2025-07-14 14:30:00', 2, 549000.0, 1, 0, 'PAYID-JUL0033A', 'https://example.com/checkout/...?token=JUL0033', NULL, 'Payment for booking ...0576', '8a09da6c-0671-4f17-82d2-0c43342d0576'),
(gen_random_uuid(), '2025-07-14 14:30:00', '2025-07-14 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-JUL0033B', NULL, NULL, 'Payout for booking ...0576', '8a09da6c-0671-4f17-82d2-0c43342d0576'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0577 (Completed: 2025-07-14 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-07-14 17:00:00', '2025-07-14 17:00:00', 2, 899000.0, 1, 0, 'PAYID-JUL0034A', 'https://example.com/checkout/...?token=JUL0034', NULL, 'Payment for booking ...0577', '8a09da6c-0671-4f17-82d2-0c43342d0577'),
(gen_random_uuid(), '2025-07-14 17:00:00', '2025-07-14 17:00:00', 2, 809100.0, 1, 1, 'PAYOUT-JUL0034B', NULL, NULL, 'Payout for booking ...0577', '8a09da6c-0671-4f17-82d2-0c43342d0577'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0578 (Completed: 2025-07-15 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-07-15 10:00:00', '2025-07-15 10:00:00', 2, 499000.0, 1, 0, 'PAYID-JUL0035A', 'https://example.com/checkout/...?token=JUL0035', NULL, 'Payment for booking ...0578', '8a09da6c-0671-4f17-82d2-0c43342d0578'),
(gen_random_uuid(), '2025-07-15 10:00:00', '2025-07-15 10:00:00', 2, 449100.0, 1, 1, 'PAYOUT-JUL0035B', NULL, NULL, 'Payout for booking ...0578', '8a09da6c-0671-4f17-82d2-0c43342d0578'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0579 (Completed: 2025-07-15 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-07-15 14:30:00', '2025-07-15 14:30:00', 2, 399000.0, 1, 0, 'PAYID-JUL0036A', 'https://example.com/checkout/...?token=JUL0036', NULL, 'Payment for booking ...0579', '8a09da6c-0671-4f17-82d2-0c43342d0579'),
(gen_random_uuid(), '2025-07-15 14:30:00', '2025-07-15 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-JUL0036B', NULL, NULL, 'Payout for booking ...0579', '8a09da6c-0671-4f17-82d2-0c43342d0579'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0580 (Completed: 2025-07-15 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-07-15 17:00:00', '2025-07-15 17:00:00', 2, 229000.0, 1, 0, 'PAYID-JUL0037A', 'https://example.com/checkout/...?token=JUL0037', NULL, 'Payment for booking ...0580', '8a09da6c-0671-4f17-82d2-0c43342d0580'),
(gen_random_uuid(), '2025-07-15 17:00:00', '2025-07-15 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-JUL0037B', NULL, NULL, 'Payout for booking ...0580', '8a09da6c-0671-4f17-82d2-0c43342d0580'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0581 (Completed: 2025-07-16 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-07-16 11:00:00', '2025-07-16 11:00:00', 2, 279000.0, 1, 0, 'PAYID-JUL0038A', 'https://example.com/checkout/...?token=JUL0038', NULL, 'Payment for booking ...0581', '8a09da6c-0671-4f17-82d2-0c43342d0581'),
(gen_random_uuid(), '2025-07-16 11:00:00', '2025-07-16 11:00:00', 2, 251100.0, 1, 1, 'PAYOUT-JUL0038B', NULL, NULL, 'Payout for booking ...0581', '8a09da6c-0671-4f17-82d2-0c43342d0581'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0582 (Completed: 2025-07-16 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-07-16 14:30:00', '2025-07-16 14:30:00', 2, 219000.0, 1, 0, 'PAYID-JUL0039A', 'https://example.com/checkout/...?token=JUL0039', NULL, 'Payment for booking ...0582', '8a09da6c-0671-4f17-82d2-0c43342d0582'),
(gen_random_uuid(), '2025-07-16 14:30:00', '2025-07-16 14:30:00', 2, 197100.0, 1, 1, 'PAYOUT-JUL0039B', NULL, NULL, 'Payout for booking ...0582', '8a09da6c-0671-4f17-82d2-0c43342d0582'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0583 (Completed: 2025-07-16 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-07-16 17:00:00', '2025-07-16 17:00:00', 2, 339000.0, 1, 0, 'PAYID-JUL0040A', 'https://example.com/checkout/...?token=JUL0040', NULL, 'Payment for booking ...0583', '8a09da6c-0671-4f17-82d2-0c43342d0583'),
(gen_random_uuid(), '2025-07-16 17:00:00', '2025-07-16 17:00:00', 2, 305100.0, 1, 1, 'PAYOUT-JUL0040B', NULL, NULL, 'Payout for booking ...0583', '8a09da6c-0671-4f17-82d2-0c43342d0583'),

-- ---------------------------------
-- JUL 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0550 (Canceled: 2025-07-03 | Pkg: ...1002 | Price: 799000)
(gen_random_uuid(), '2025-07-03 14:30:00', '2025-07-03 14:31:00', 2, 799000.0, 3, 0, 'PAYID-JUL-CNL-01', 'https://example.com/checkout/...?token=JUL-CNL-01', NULL, 'Refund for booking ...0550', '8a09da6c-0671-4f17-82d2-0c43342d0550'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0571 (Canceled: 2025-07-10 | Pkg: ...0501 | Price: 379000)
(gen_random_uuid(), '2025-07-10 14:30:00', '2025-07-10 14:31:00', 2, 379000.0, 3, 0, 'PAYID-JUL-CNL-02', 'https://example.com/checkout/...?token=JUL-CNL-02', NULL, 'Refund for booking ...0571', '8a09da6c-0671-4f17-82d2-0c43342d0571'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0601 (Canceled: 2025-07-20 | Pkg: ...0101 | Price: 249000)
(gen_random_uuid(), '2025-07-20 14:30:00', '2025-07-20 14:31:00', 2, 249000.0, 3, 0, 'PAYID-JUL-CNL-03', 'https://example.com/checkout/...?token=JUL-CNL-03', NULL, 'Refund for booking ...0601', '8a09da6c-0671-4f17-82d2-0c43342d0601'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0613 (Canceled: 2025-07-24 | Pkg: ...0003 | Price: 399000)
(gen_random_uuid(), '2025-07-24 14:30:00', '2025-07-24 14:31:00', 2, 399000.0, 3, 0, 'PAYID-JUL-CNL-04', 'https://example.com/checkout/...?token=JUL-CNL-04', NULL, 'Refund for booking ...0613', '8a09da6c-0671-4f17-82d2-0c43342d0613'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0621 (Canceled: 2025-07-23 | Pkg: ...0102 | Price: 329000)
(gen_random_uuid(), '2025-07-23 14:30:00', '2025-07-23 14:31:00', 2, 329000.0, 3, 0, 'PAYID-JUL-CNL-05', 'https://example.com/checkout/...?token=JUL-CNL-05', NULL, 'Refund for booking ...0621', '8a09da6c-0671-4f17-82d2-0c43342d0621');

-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.19
-- Description: Insert initial data for booking payments (Oct 2025)
-- =============================================

-- Ghi chú Enum:
-- payment_method: 2 (Mặc định)
-- status: 1 (COMPLETED), 3 (REFUND)
-- payment_type: 0 (PAID_PACKAGE), 1 (RECEIVED_PACKAGE)
-- Logic Payout: amount = package.price - package.service_fee_amount
-- Logic Thời gian: Cả PAID và RECEIVED đều được tạo/cập nhật vào ngày booking hoàn thành (booking.updated_at)

INSERT INTO booking_payment (booking_payment_id, created_at, updated_at, payment_method, amount, status, payment_type, transaction_id, approval_url, failure_reason, extra_info, booking_id) VALUES

-- =============================================
-- BOOKING PAYMENTS - OCT 2025
-- =============================================

-- 1. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0812 (Completed: 2025-10-03 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-10-03 11:00:00', '2025-10-03 11:00:00', 2, 399000.0, 1, 0, 'PAYID-OCT0001A', 'https://example.com/checkout/...?token=OCT0001', NULL, 'Payment for booking ...0812', '8a09da6c-0671-4f17-82d2-0c43342d0812'),
(gen_random_uuid(), '2025-10-03 11:00:00', '2025-10-03 11:00:00', 2, 359100.0, 1, 1, 'PAYOUT-OCT0001B', NULL, NULL, 'Payout for booking ...0812', '8a09da6c-0671-4f17-82d2-0c43342d0812'),

-- 2. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0813 (Completed: 2025-10-03 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-10-03 14:30:00', '2025-10-03 14:30:00', 2, 229000.0, 1, 0, 'PAYID-OCT0002A', 'https://example.com/checkout/...?token=OCT0002', NULL, 'Payment for booking ...0813', '8a09da6c-0671-4f17-82d2-0c43342d0813'),
(gen_random_uuid(), '2025-10-03 14:30:00', '2025-10-03 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-OCT0002B', NULL, NULL, 'Payout for booking ...0813', '8a09da6c-0671-4f17-82d2-0c43342d0813'),

-- 3. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0814 (Completed: 2025-10-03 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-10-03 17:00:00', '2025-10-03 17:00:00', 2, 279000.0, 1, 0, 'PAYID-OCT0003A', 'https://example.com/checkout/...?token=OCT0003', NULL, 'Payment for booking ...0814', '8a09da6c-0671-4f17-82d2-0c43342d0814'),
(gen_random_uuid(), '2025-10-03 17:00:00', '2025-10-03 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-OCT0003B', NULL, NULL, 'Payout for booking ...0814', '8a09da6c-0671-4f17-82d2-0c43342d0814'),

-- 4. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0815 (Completed: 2025-10-04 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-10-04 10:00:00', '2025-10-04 10:00:00', 2, 219000.0, 1, 0, 'PAYID-OCT0004A', 'https://example.com/checkout/...?token=OCT0004', NULL, 'Payment for booking ...0815', '8a09da6c-0671-4f17-82d2-0c43342d0815'),
(gen_random_uuid(), '2025-10-04 10:00:00', '2025-10-04 10:00:00', 2, 197100.0, 1, 1, 'PAYOUT-OCT0004B', NULL, NULL, 'Payout for booking ...0815', '8a09da6c-0671-4f17-82d2-0c43342d0815'),

-- 5. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0816 (Completed: 2025-10-04 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-10-04 14:30:00', '2025-10-04 14:30:00', 2, 339000.0, 1, 0, 'PAYID-OCT0005A', 'https://example.com/checkout/...?token=OCT0005', NULL, 'Payment for booking ...0816', '8a09da6c-0671-4f17-82d2-0c43342d0816'),
(gen_random_uuid(), '2025-10-04 14:30:00', '2025-10-04 14:30:00', 2, 305100.0, 1, 1, 'PAYOUT-OCT0005B', NULL, NULL, 'Payout for booking ...0816', '8a09da6c-0671-4f17-82d2-0c43342d0816'),

-- 6. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0817 (Completed: 2025-10-04 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-10-04 17:00:00', '2025-10-04 17:00:00', 2, 799000.0, 1, 0, 'PAYID-OCT0006A', 'https://example.com/checkout/...?token=OCT0006', NULL, 'Payment for booking ...0817', '8a09da6c-0671-4f17-82d2-0c43342d0817'),
(gen_random_uuid(), '2025-10-04 17:00:00', '2025-10-04 17:00:00', 2, 719100.0, 1, 1, 'PAYOUT-OCT0006B', NULL, NULL, 'Payout for booking ...0817', '8a09da6c-0671-4f17-82d2-0c43342d0817'),

-- 7. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0818 (Completed: 2025-10-05 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-10-05 11:00:00', '2025-10-05 11:00:00', 2, 269000.0, 1, 0, 'PAYID-OCT0007A', 'https://example.com/checkout/...?token=OCT0007', NULL, 'Payment for booking ...0818', '8a09da6c-0671-4f17-82d2-0c43342d0818'),
(gen_random_uuid(), '2025-10-05 11:00:00', '2025-10-05 11:00:00', 2, 242100.0, 1, 1, 'PAYOUT-OCT0007B', NULL, NULL, 'Payout for booking ...0818', '8a09da6c-0671-4f17-82d2-0c43342d0818'),

-- 8. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0819 (Completed: 2025-10-05 | Pkg: ...0901 | Price: 599000 | Payout: 539100)
(gen_random_uuid(), '2025-10-05 14:30:00', '2025-10-05 14:30:00', 2, 599000.0, 1, 0, 'PAYID-OCT0008A', 'https://example.com/checkout/...?token=OCT0008', NULL, 'Payment for booking ...0819', '8a09da6c-0671-4f17-82d2-0c43342d0819'),
(gen_random_uuid(), '2025-10-05 14:30:00', '2025-10-05 14:30:00', 2, 539100.0, 1, 1, 'PAYOUT-OCT0008B', NULL, NULL, 'Payout for booking ...0819', '8a09da6c-0671-4f17-82d2-0c43342d0819'),

-- 9. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0820 (Completed: 2025-10-05 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-10-05 17:00:00', '2025-10-05 17:00:00', 2, 249000.0, 1, 0, 'PAYID-OCT0009A', 'https://example.com/checkout/...?token=OCT0009', NULL, 'Payment for booking ...0820', '8a09da6c-0671-4f17-82d2-0c43342d0820'),
(gen_random_uuid(), '2025-10-05 17:00:00', '2025-10-05 17:00:00', 2, 224100.0, 1, 1, 'PAYOUT-OCT0009B', NULL, NULL, 'Payout for booking ...0820', '8a09da6c-0671-4f17-82d2-0c43342d0820'),

-- 10. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0821 (Completed: 2025-10-06 | Pkg: ...0011 | Price: 319000 | Payout: 287100)
(gen_random_uuid(), '2025-10-06 10:00:00', '2025-10-06 10:00:00', 2, 319000.0, 1, 0, 'PAYID-OCT0010A', 'https://example.com/checkout/...?token=OCT0010', NULL, 'Payment for booking ...0821', '8a09da6c-0671-4f17-82d2-0c43342d0821'),
(gen_random_uuid(), '2025-10-06 10:00:00', '2025-10-06 10:00:00', 2, 287100.0, 1, 1, 'PAYOUT-OCT0010B', NULL, NULL, 'Payout for booking ...0821', '8a09da6c-0671-4f17-82d2-0c43342d0821'),

-- 11. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0822 (Completed: 2025-10-06 | Pkg: ...0102 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-10-06 14:30:00', '2025-10-06 14:30:00', 2, 329000.0, 1, 0, 'PAYID-OCT0011A', 'https://example.com/checkout/...?token=OCT0011', NULL, 'Payment for booking ...0822', '8a09da6c-0671-4f17-82d2-0c43342d0822'),
(gen_random_uuid(), '2025-10-06 14:30:00', '2025-10-06 14:30:00', 2, 296100.0, 1, 1, 'PAYOUT-OCT0011B', NULL, NULL, 'Payout for booking ...0822', '8a09da6c-0671-4f17-82d2-0c43342d0822'),

-- 12. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0824 (Completed: 2025-10-07 | Pkg: ...0008 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-10-07 11:00:00', '2025-10-07 11:00:00', 2, 449000.0, 1, 0, 'PAYID-OCT0012A', 'https://example.com/checkout/...?token=OCT0012', NULL, 'Payment for booking ...0824', '8a09da6c-0671-4f17-82d2-0c43342d0824'),
(gen_random_uuid(), '2025-10-07 11:00:00', '2025-10-07 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-OCT0012B', NULL, NULL, 'Payout for booking ...0824', '8a09da6c-0671-4f17-82d2-0c43342d0824'),

-- 13. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0825 (Completed: 2025-10-07 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-10-07 14:30:00', '2025-10-07 14:30:00', 2, 149000.0, 1, 0, 'PAYID-OCT0013A', 'https://example.com/checkout/...?token=OCT0013', NULL, 'Payment for booking ...0825', '8a09da6c-0671-4f17-82d2-0c43342d0825'),
(gen_random_uuid(), '2025-10-07 14:30:00', '2025-10-07 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-OCT0013B', NULL, NULL, 'Payout for booking ...0825', '8a09da6c-0671-4f17-82d2-0c43342d0825'),

-- 14. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0826 (Completed: 2025-10-07 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-10-07 17:00:00', '2025-10-07 17:00:00', 2, 549000.0, 1, 0, 'PAYID-OCT0014A', 'https://example.com/checkout/...?token=OCT0014', NULL, 'Payment for booking ...0826', '8a09da6c-0671-4f17-82d2-0c43342d0826'),
(gen_random_uuid(), '2025-10-07 17:00:00', '2025-10-07 17:00:00', 2, 494100.0, 1, 1, 'PAYOUT-OCT0014B', NULL, NULL, 'Payout for booking ...0826', '8a09da6c-0671-4f17-82d2-0c43342d0826'),

-- 15. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0827 (Completed: 2025-10-08 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-10-08 10:00:00', '2025-10-08 10:00:00', 2, 379000.0, 1, 0, 'PAYID-OCT0015A', 'https://example.com/checkout/...?token=OCT0015', NULL, 'Payment for booking ...0827', '8a09da6c-0671-4f17-82d2-0c43342d0827'),
(gen_random_uuid(), '2025-10-08 10:00:00', '2025-10-08 10:00:00', 2, 341100.0, 1, 1, 'PAYOUT-OCT0015B', NULL, NULL, 'Payout for booking ...0827', '8a09da6c-0671-4f17-82d2-0c43342d0827'),

-- 16. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0828 (Completed: 2025-10-08 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-10-08 14:30:00', '2025-10-08 14:30:00', 2, 229000.0, 1, 0, 'PAYID-OCT0016A', 'https://example.com/checkout/...?token=OCT0016', NULL, 'Payment for booking ...0828', '8a09da6c-0671-4f17-82d2-0c43342d0828'),
(gen_random_uuid(), '2025-10-08 14:30:00', '2025-10-08 14:30:00', 2, 206100.0, 1, 1, 'PAYOUT-OCT0016B', NULL, NULL, 'Payout for booking ...0828', '8a09da6c-0671-4f17-82d2-0c43342d0828'),

-- 17. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0829 (Completed: 2025-10-08 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-10-08 17:00:00', '2025-10-08 17:00:00', 2, 699000.0, 1, 0, 'PAYID-OCT0017A', 'https://example.com/checkout/...?token=OCT0017', NULL, 'Payment for booking ...0829', '8a09da6c-0671-4f17-82d2-0c43342d0829'),
(gen_random_uuid(), '2025-10-08 17:00:00', '2025-10-08 17:00:00', 2, 629100.0, 1, 1, 'PAYOUT-OCT0017B', NULL, NULL, 'Payout for booking ...0829', '8a09da6c-0671-4f17-82d2-0c43342d0829'),

-- 18. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0830 (Completed: 2025-10-09 | Pkg: ...0801 | Price: 449000 | Payout: 404100)
(gen_random_uuid(), '2025-10-09 11:00:00', '2025-10-09 11:00:00', 2, 449000.0, 1, 0, 'PAYID-OCT0018A', 'https://example.com/checkout/...?token=OCT0018', NULL, 'Payment for booking ...0830', '8a09da6c-0671-4f17-82d2-0c43342d0830'),
(gen_random_uuid(), '2025-10-09 11:00:00', '2025-10-09 11:00:00', 2, 404100.0, 1, 1, 'PAYOUT-OCT0018B', NULL, NULL, 'Payout for booking ...0830', '8a09da6c-0671-4f17-82d2-0c43342d0830'),

-- 19. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0831 (Completed: 2025-10-09 | Pkg: ...0001 | Price: 299000 | Payout: 269100)
(gen_random_uuid(), '2025-10-09 14:30:00', '2025-10-09 14:30:00', 2, 299000.0, 1, 0, 'PAYID-OCT0019A', 'https://example.com/checkout/...?token=OCT0019', NULL, 'Payment for booking ...0831', '8a09da6c-0671-4f17-82d2-0c43342d0831'),
(gen_random_uuid(), '2025-10-09 14:30:00', '2025-10-09 14:30:00', 2, 269100.0, 1, 1, 'PAYOUT-OCT0019B', NULL, NULL, 'Payout for booking ...0831', '8a09da6c-0671-4f17-82d2-0c43342d0831'),

-- 20. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0832 (Completed: 2025-10-09 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-10-09 17:00:00', '2025-10-09 17:00:00', 2, 279000.0, 1, 0, 'PAYID-OCT0020A', 'https://example.com/checkout/...?token=OCT0020', NULL, 'Payment for booking ...0832', '8a09da6c-0671-4f17-82d2-0c43342d0832'),
(gen_random_uuid(), '2025-10-09 17:00:00', '2025-10-09 17:00:00', 2, 251100.0, 1, 1, 'PAYOUT-OCT0020B', NULL, NULL, 'Payout for booking ...0832', '8a09da6c-0671-4f17-82d2-0c43342d0832'),

-- 21. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0833 (Completed: 2025-10-10 | Pkg: ...0006 | Price: 329000 | Payout: 296100)
(gen_random_uuid(), '2025-10-10 10:00:00', '2025-10-10 10:00:00', 2, 329000.0, 1, 0, 'PAYID-OCT0021A', 'https://example.com/checkout/...?token=OCT0021', NULL, 'Payment for booking ...0833', '8a09da6c-0671-4f17-82d2-0c43342d0833'),
(gen_random_uuid(), '2025-10-10 10:00:00', '2025-10-10 10:00:00', 2, 296100.0, 1, 1, 'PAYOUT-OCT0021B', NULL, NULL, 'Payout for booking ...0833', '8a09da6c-0671-4f17-82d2-0c43342d0833'),

-- 22. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0834 (Completed: 2025-10-10 | Pkg: ...0101 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-10-10 14:30:00', '2025-10-10 14:30:00', 2, 249000.0, 1, 0, 'PAYID-OCT0022A', 'https://example.com/checkout/...?token=OCT0022', NULL, 'Payment for booking ...0834', '8a09da6c-0671-4f17-82d2-0c43342d0834'),
(gen_random_uuid(), '2025-10-10 14:30:00', '2025-10-10 14:30:00', 2, 224100.0, 1, 1, 'PAYOUT-OCT0022B', NULL, NULL, 'Payout for booking ...0834', '8a09da6c-0671-4f17-82d2-0c43342d0834'),

-- 23. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0835 (Completed: 2025-10-10 | Pkg: ...0201 | Price: 369000 | Payout: 332100)
(gen_random_uuid(), '2025-10-10 17:00:00', '2025-10-10 17:00:00', 2, 369000.0, 1, 0, 'PAYID-OCT0023A', 'https://example.com/checkout/...?token=OCT0023', NULL, 'Payment for booking ...0835', '8a09da6c-0671-4f17-82d2-0c43342d0835'),
(gen_random_uuid(), '2025-10-10 17:00:00', '2025-10-10 17:00:00', 2, 332100.0, 1, 1, 'PAYOUT-OCT0023B', NULL, NULL, 'Payout for booking ...0835', '8a09da6c-0671-4f17-82d2-0c43342d0835'),

-- 24. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0836 (Completed: 2025-10-11 | Pkg: ...0301 | Price: 429000 | Payout: 386100)
(gen_random_uuid(), '2025-10-11 11:00:00', '2025-10-11 11:00:00', 2, 429000.0, 1, 0, 'PAYID-OCT0024A', 'https://example.com/checkout/...?token=OCT0024', NULL, 'Payment for booking ...0836', '8a09da6c-0671-4f17-82d2-0c43342d0836'),
(gen_random_uuid(), '2025-10-11 11:00:00', '2025-10-11 11:00:00', 2, 386100.0, 1, 1, 'PAYOUT-OCT0024B', NULL, NULL, 'Payout for booking ...0836', '8a09da6c-0671-4f17-82d2-0c43342d0836'),

-- 25. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0837 (Completed: 2025-10-11 | Pkg: ...0401 | Price: 199000 | Payout: 179100)
(gen_random_uuid(), '2025-10-11 14:30:00', '2025-10-11 14:30:00', 2, 199000.0, 1, 0, 'PAYID-OCT0025A', 'https://example.com/checkout/...?token=OCT0025', NULL, 'Payment for booking ...0837', '8a09da6c-0671-4f17-82d2-0c43342d0837'),
(gen_random_uuid(), '2025-10-11 14:30:00', '2025-10-11 14:30:00', 2, 179100.0, 1, 1, 'PAYOUT-OCT0025B', NULL, NULL, 'Payout for booking ...0837', '8a09da6c-0671-4f17-82d2-0c43342d0837'),

-- 26. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0838 (Completed: 2025-10-11 | Pkg: ...0501 | Price: 379000 | Payout: 341100)
(gen_random_uuid(), '2025-10-11 17:00:00', '2025-10-11 17:00:00', 2, 379000.0, 1, 0, 'PAYID-OCT0026A', 'https://example.com/checkout/...?token=OCT0026', NULL, 'Payment for booking ...0838', '8a09da6c-0671-4f17-82d2-0c43342d0838'),
(gen_random_uuid(), '2025-10-11 17:00:00', '2025-10-11 17:00:00', 2, 341100.0, 1, 1, 'PAYOUT-OCT0026B', NULL, NULL, 'Payout for booking ...0838', '8a09da6c-0671-4f17-82d2-0c43342d0838'),

-- 27. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0839 (Completed: 2025-10-12 | Pkg: ...0601 | Price: 699000 | Payout: 629100)
(gen_random_uuid(), '2025-10-12 10:00:00', '2025-10-12 10:00:00', 2, 699000.0, 1, 0, 'PAYID-OCT0027A', 'https://example.com/checkout/...?token=OCT0027', NULL, 'Payment for booking ...0839', '8a09da6c-0671-4f17-82d2-0c43342d0839'),
(gen_random_uuid(), '2025-10-12 10:00:00', '2025-10-12 10:00:00', 2, 629100.0, 1, 1, 'PAYOUT-OCT0027B', NULL, NULL, 'Payout for booking ...0839', '8a09da6c-0671-4f17-82d2-0c43342d0839'),

-- 28. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0840 (Completed: 2025-10-12 | Pkg: ...0701 | Price: 149000 | Payout: 134100)
(gen_random_uuid(), '2025-10-12 14:30:00', '2025-10-12 14:30:00', 2, 149000.0, 1, 0, 'PAYID-OCT0028A', 'https://example.com/checkout/...?token=OCT0028', NULL, 'Payment for booking ...0840', '8a09da6c-0671-4f17-82d2-0c43342d0840'),
(gen_random_uuid(), '2025-10-12 14:30:00', '2025-10-12 14:30:00', 2, 134100.0, 1, 1, 'PAYOUT-OCT0028B', NULL, NULL, 'Payout for booking ...0840', '8a09da6c-0671-4f17-82d2-0c43342d0840'),

-- 29. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0842 (Completed: 2025-10-13 | Pkg: ...0902 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-10-13 11:00:00', '2025-10-13 11:00:00', 2, 499000.0, 1, 0, 'PAYID-OCT0029A', 'https://example.com/checkout/...?token=OCT0029', NULL, 'Payment for booking ...0842', '8a09da6c-0671-4f17-82d2-0c43342d0842'),
(gen_random_uuid(), '2025-10-13 11:00:00', '2025-10-13 11:00:00', 2, 449100.0, 1, 1, 'PAYOUT-OCT0029B', NULL, NULL, 'Payout for booking ...0842', '8a09da6c-0671-4f17-82d2-0c43342d0842'),

-- 30. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0843 (Completed: 2025-10-13 | Pkg: ...1001 | Price: 549000 | Payout: 494100)
(gen_random_uuid(), '2025-10-13 14:30:00', '2025-10-13 14:30:00', 2, 549000.0, 1, 0, 'PAYID-OCT0030A', 'https://example.com/checkout/...?token=OCT0030', NULL, 'Payment for booking ...0843', '8a09da6c-0671-4f17-82d2-0c43342d0843'),
(gen_random_uuid(), '2025-10-13 14:30:00', '2025-10-13 14:30:00', 2, 494100.0, 1, 1, 'PAYOUT-OCT0030B', NULL, NULL, 'Payout for booking ...0843', '8a09da6c-0671-4f17-82d2-0c43342d0843'),

-- 31. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0844 (Completed: 2025-10-13 | Pkg: ...0004 | Price: 899000 | Payout: 809100)
(gen_random_uuid(), '2025-10-13 17:00:00', '2025-10-13 17:00:00', 2, 899000.0, 1, 0, 'PAYID-OCT0031A', 'https://example.com/checkout/...?token=OCT0031', NULL, 'Payment for booking ...0844', '8a09da6c-0671-4f17-82d2-0c43342d0844'),
(gen_random_uuid(), '2025-10-13 17:00:00', '2025-10-13 17:00:00', 2, 809100.0, 1, 1, 'PAYOUT-OCT0031B', NULL, NULL, 'Payout for booking ...0844', '8a09da6c-0671-4f17-82d2-0c43342d0844'),

-- 32. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0845 (Completed: 2025-10-14 | Pkg: ...0002 | Price: 499000 | Payout: 449100)
(gen_random_uuid(), '2025-10-14 10:00:00', '2025-10-14 10:00:00', 2, 499000.0, 1, 0, 'PAYID-OCT0032A', 'https://example.com/checkout/...?token=OCT0032', NULL, 'Payment for booking ...0845', '8a09da6c-0671-4f17-82d2-0c43342d0845'),
(gen_random_uuid(), '2025-10-14 10:00:00', '2025-10-14 10:00:00', 2, 449100.0, 1, 1, 'PAYOUT-OCT0032B', NULL, NULL, 'Payout for booking ...0845', '8a09da6c-0671-4f17-82d2-0c43342d0845'),

-- 33. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0846 (Completed: 2025-10-14 | Pkg: ...0003 | Price: 399000 | Payout: 359100)
(gen_random_uuid(), '2025-10-14 14:30:00', '2025-10-14 14:30:00', 2, 399000.0, 1, 0, 'PAYID-OCT0033A', 'https://example.com/checkout/...?token=OCT0033', NULL, 'Payment for booking ...0846', '8a09da6c-0671-4f17-82d2-0c43342d0846'),
(gen_random_uuid(), '2025-10-14 14:30:00', '2025-10-14 14:30:00', 2, 359100.0, 1, 1, 'PAYOUT-OCT0033B', NULL, NULL, 'Payout for booking ...0846', '8a09da6c-0671-4f17-82d2-0c43342d0846'),

-- 34. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0847 (Completed: 2025-10-14 | Pkg: ...0202 | Price: 229000 | Payout: 206100)
(gen_random_uuid(), '2025-10-14 17:00:00', '2025-10-14 17:00:00', 2, 229000.0, 1, 0, 'PAYID-OCT0034A', 'https://example.com/checkout/...?token=OCT0034', NULL, 'Payment for booking ...0847', '8a09da6c-0671-4f17-82d2-0c43342d0847'),
(gen_random_uuid(), '2025-10-14 17:00:00', '2025-10-14 17:00:00', 2, 206100.0, 1, 1, 'PAYOUT-OCT0034B', NULL, NULL, 'Payout for booking ...0847', '8a09da6c-0671-4f17-82d2-0c43342d0847'),

-- 35. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0848 (Completed: 2025-10-15 | Pkg: ...0005 | Price: 279000 | Payout: 251100)
(gen_random_uuid(), '2025-10-15 11:00:00', '2025-10-15 11:00:00', 2, 279000.0, 1, 0, 'PAYID-OCT0035A', 'https://example.com/checkout/...?token=OCT0035', NULL, 'Payment for booking ...0848', '8a09da6c-0671-4f17-82d2-0c43342d0848'),
(gen_random_uuid(), '2025-10-15 11:00:00', '2025-10-15 11:00:00', 2, 251100.0, 1, 1, 'PAYOUT-OCT0035B', NULL, NULL, 'Payout for booking ...0848', '8a09da6c-0671-4f17-82d2-0c43342d0848'),

-- 36. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0849 (Completed: 2025-10-15 | Pkg: ...0402 | Price: 219000 | Payout: 197100)
(gen_random_uuid(), '2025-10-15 14:30:00', '2025-10-15 14:30:00', 2, 219000.0, 1, 0, 'PAYID-OCT0036A', 'https://example.com/checkout/...?token=OCT0036', NULL, 'Payment for booking ...0849', '8a09da6c-0671-4f17-82d2-0c43342d0849'),
(gen_random_uuid(), '2025-10-15 14:30:00', '2025-10-15 14:30:00', 2, 197100.0, 1, 1, 'PAYOUT-OCT0036B', NULL, NULL, 'Payout for booking ...0849', '8a09da6c-0671-4f17-82d2-0c43342d0849'),

-- 37. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0850 (Completed: 2025-10-15 | Pkg: ...0502 | Price: 339000 | Payout: 305100)
(gen_random_uuid(), '2025-10-15 17:00:00', '2025-10-15 17:00:00', 2, 339000.0, 1, 0, 'PAYID-OCT0037A', 'https://example.com/checkout/...?token=OCT0037', NULL, 'Payment for booking ...0850', '8a09da6c-0671-4f17-82d2-0c43342d0850'),
(gen_random_uuid(), '2025-10-15 17:00:00', '2025-10-15 17:00:00', 2, 305100.0, 1, 1, 'PAYOUT-OCT0037B', NULL, NULL, 'Payout for booking ...0850', '8a09da6c-0671-4f17-82d2-0c43342d0850'),

-- 38. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0851 (Completed: 2025-10-16 | Pkg: ...1002 | Price: 799000 | Payout: 719100)
(gen_random_uuid(), '2025-10-16 10:00:00', '2025-10-16 10:00:00', 2, 799000.0, 1, 0, 'PAYID-OCT0038A', 'https://example.com/checkout/...?token=OCT0038', NULL, 'Payment for booking ...0851', '8a09da6c-0671-4f17-82d2-0c43342d0851'),
(gen_random_uuid(), '2025-10-16 10:00:00', '2025-10-16 10:00:00', 2, 719100.0, 1, 1, 'PAYOUT-OCT0038B', NULL, NULL, 'Payout for booking ...0851', '8a09da6c-0671-4f17-82d2-0c43342d0851'),

-- 39. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0852 (Completed: 2025-10-16 | Pkg: ...0802 | Price: 269000 | Payout: 242100)
(gen_random_uuid(), '2025-10-16 14:30:00', '2025-10-16 14:30:00', 2, 269000.0, 1, 0, 'PAYID-OCT0039A', 'https://example.com/checkout/...?token=OCT0039', NULL, 'Payment for booking ...0852', '8a09da6c-0671-4f17-82d2-0c43342d0852'),
(gen_random_uuid(), '2025-10-16 14:30:00', '2025-10-16 14:30:00', 2, 242100.0, 1, 1, 'PAYOUT-OCT0039B', NULL, NULL, 'Payout for booking ...0852', '8a09da6c-0671-4f17-82d2-0c43342d0852'),

-- 40. Booking: 8a09da6c-0671-4f17-82d2-0c43342d0854 (Completed: 2025-10-17 | Pkg: ...0007 | Price: 249000 | Payout: 224100)
(gen_random_uuid(), '2025-10-17 11:00:00', '2025-10-17 11:00:00', 2, 249000.0, 1, 0, 'PAYID-OCT0040A', 'https://example.com/checkout/...?token=OCT0040', NULL, 'Payment for booking ...0854', '8a09da6c-0671-4f17-82d2-0c43342d0854'),
(gen_random_uuid(), '2025-10-17 11:00:00', '2025-10-17 11:00:00', 2, 224100.0, 1, 1, 'PAYOUT-OCT0040B', NULL, NULL, 'Payout for booking ...0854', '8a09da6c-0671-4f17-82d2-0c43342d0854'),

-- ---------------------------------
-- OCT 2025 - REFUNDS
-- ---------------------------------
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0823 (Canceled: 2025-10-04 | Pkg: ...0903 | Price: 1499000)
(gen_random_uuid(), '2025-10-04 14:30:00', '2025-10-04 14:31:00', 2, 1499000.0, 3, 0, 'PAYID-OCT-CNL-01', 'https://example.com/checkout/...?token=OCT-CNL-01', NULL, 'Refund for booking ...0823', '8a09da6c-0671-4f17-82d2-0c43342d0823'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0841 (Canceled: 2025-10-10 | Pkg: ...0802 | Price: 269000)
(gen_random_uuid(), '2025-10-10 14:30:00', '2025-10-10 14:31:00', 2, 269000.0, 3, 0, 'PAYID-OCT-CNL-02', 'https://example.com/checkout/...?token=OCT-CNL-02', NULL, 'Refund for booking ...0841', '8a09da6c-0671-4f17-82d2-0c43342d0841'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0853 (Canceled: 2025-10-14 | Pkg: ...0901 | Price: 599000)
(gen_random_uuid(), '2025-10-14 14:30:00', '2025-10-14 14:31:00', 2, 599000.0, 3, 0, 'PAYID-OCT-CNL-03', 'https://example.com/checkout/...?token=OCT-CNL-03', NULL, 'Refund for booking ...0853', '8a09da6c-0671-4f17-82d2-0c43342d0853'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0874 (Canceled: 2025-10-21 | Pkg: ...0701 | Price: 149000)
(gen_random_uuid(), '2025-10-21 14:30:00', '2025-10-21 14:31:00', 2, 149000.0, 3, 0, 'PAYID-OCT-CNL-04', 'https://example.com/checkout/...?token=OCT-CNL-04', NULL, 'Refund for booking ...0874', '8a09da6c-0671-4f17-82d2-0c43342d0874'),
-- Booking: 8a09da6c-0671-4f17-82d2-0c43342d0889 (Canceled: 2025-10-26 | Pkg: ...0011 | Price: 319000)
(gen_random_uuid(), '2025-10-26 14:30:00', '2025-10-26 14:31:00', 2, 319000.0, 3, 0, 'PAYID-OCT-CNL-05', 'https://example.com/checkout/...?token=OCT-CNL-05', NULL, 'Refund for booking ...0889', '8a09da6c-0671-4f17-82d2-0c43342d0889');

-- =============================================
-- END OF MIGRATION
-- =============================================
-- =============================================
-- END OF MIGRATION
-- =============================================
-- =============================================
-- END OF MIGRATION
-- =============================================
-- =============================================
-- END OF MIGRATION
-- =============================================
-- =============================================
-- END OF MIGRATION
-- =============================================