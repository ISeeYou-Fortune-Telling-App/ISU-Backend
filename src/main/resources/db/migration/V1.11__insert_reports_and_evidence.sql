-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.12
-- Description: Insert initial data for reports and evidence
-- =============================================

-- Ghi chú Enum:
-- target_type: 0(SERVICE_PACKAGE), 1(CHAT/CONVERSATION), 2(BOOKING), 3(SEER)
-- status: 0(PENDING), 1(VIEWED), 2(RESOLVED), 3(REJECTED)
-- action_type: 0(NO_ACTION), 1(WARNING_ISSUED), 2(CONTENT_REMOVED), 3(USER_SUSPENDED), 4(USER_BANNED)

-- Report UUIDs: e00e...0001 -> e00e...0030
-- Report Type UUIDs: f00e...0000 -> f00e...0009

-- Report 1: (PENDING) Customer 0001 reports Seer 0071 for FRAUD
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000001', '2025-03-15 10:00:00', '2025-03-15 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440071', 'Thầy bói này hứa hẹn chắc chắn 100% trúng số, tôi nghi ngờ lừa đảo.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000008');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-03-15 10:01:00', '2025-03-15 10:01:00', 'e00e0000-0000-0000-0000-000000000001', 'https://example.com/evidence1.jpg');

-- Report 2: (RESOLVED) Customer 0002 reports Package 0101 for SPAM
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000002', '2025-03-16 11:00:00', '2025-03-17 09:00:00', 0, '880e8400-e29b-41d4-a716-446655440101', 'Gói dịch vụ này spam link quảng cáo trong phần mô tả.', 2, 2, 'Admin đã gỡ bỏ nội dung spam.', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440072', 'f00e0000-0000-0000-0000-000000000000');

-- Report 3: (REJECTED) Customer 0003 reports Seer 0073 for HATE_SPEECH
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000003', '2025-03-17 14:00:00', '2025-03-18 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440073', 'Thầy bói nói lời thù ghét.', 3, 0, 'Admin xem xét không đủ bằng chứng, đây là hiểu lầm.', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440073', 'f00e0000-0000-0000-0000-000000000003');

-- Report 4: (RESOLVED) Customer 0004 reports Booking 0011 for HARASSMENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000004', '2025-03-18 15:00:00', '2025-03-19 11:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0011', 'Seer đã quấy rối tôi trong buổi xem này.', 2, 1, 'Admin đã gửi cảnh cáo cho Seer.', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000002');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
                                                                                                     (gen_random_uuid(), '2025-03-18 15:01:00', '2025-03-18 15:01:00', 'e00e0000-0000-0000-0000-000000000004', 'https://example.com/evidence2.jpg'),
                                                                                                     (gen_random_uuid(), '2025-03-18 15:01:10', '2025-03-18 15:01:10', 'e00e0000-0000-0000-0000-000000000004', 'https://example.com/evidence3.jpg');

-- Report 5: (VIEWED) Customer 0005 reports Conversation (Admin chat) for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000005', '2025-03-20 09:00:00', '2025-03-20 10:00:00', 1, 'c00e8400-e29b-41d4-a716-200000000005', 'Nội dung chat không liên quan.', 1, 0, NULL, '550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', 'f00e0000-0000-0000-0000-000000000009');

-- Report 6: (RESOLVED) Customer 0010 reports Seer 0080 for IMPERSONATION
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000006', '2025-04-01 10:00:00', '2025-04-02 11:00:00', 3, '550e8400-e29b-41d4-a716-446655440080', 'Người này đang giả mạo Cô Oanh nổi tiếng.', 2, 3, 'Tài khoản đã bị đình chỉ để xác minh danh tính.', '550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440080', 'f00e0000-0000-0000-0000-000000000007');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-04-01 10:01:00', '2025-04-01 10:01:00', 'e00e0000-0000-0000-0000-000000000006', 'https://example.com/evidence4.jpg');

-- Report 7: (PENDING) Customer 0015 reports Package 0601 for COPYRIGHT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000007', '2025-04-05 13:00:00', '2025-04-05 13:00:00', 0, '880e8400-e29b-41d4-a716-446655440601', 'Hình ảnh gói dịch vụ này lấy cắp từ trang của tôi.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440077', 'f00e0000-0000-0000-0000-000000000006');

-- Report 8: (REJECTED) Customer 0020 reports Booking 0017 for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000008', '2025-04-10 16:00:00', '2025-04-11 10:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0017', 'Thầy bói đến trễ.', 3, 0, 'Admin: Seer đã báo cáo sự cố kỹ thuật và khách hàng đã đồng ý dời 5 phút. Từ chối report.', '550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000009');

-- Report 9: (RESOLVED) Customer 0030 reports Conversation (Booking ...0030) for NUDITY
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000009', '2025-04-12 10:00:00', '2025-04-13 09:00:00', 1, 'c00e8400-e29b-41d4-a716-300000000030', 'Seer đã gửi hình ảnh nhạy cảm trong lúc chat.', 2, 4, 'Tài khoản Seer 0075 đã bị cấm vĩnh viễn.', '550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440075', 'f00e0000-0000-0000-0000-000000000005');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-04-12 10:01:00', '2025-04-12 10:01:00', 'e00e0000-0000-0000-0000-000000000009', 'https://example.com/evidence5.jpg');

-- Report 10: (PENDING) Customer 0040 reports Seer 0072 for FRAUD
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000010', '2025-04-15 11:00:00', '2025-04-15 11:00:00', 3, '550e8400-e29b-41d4-a716-446655440072', 'Cô này xem không đúng gì cả, đòi hoàn tiền.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440040', '550e8400-e29b-41d4-a716-446655440072', 'f00e0000-0000-0000-0000-000000000008');

-- Report 11: (RESOLVED) Customer 0050 reports Package 0903 for INAPPROPRIATE_CONTENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000011', '2025-05-01 10:00:00', '2025-05-02 09:00:00', 0, '880e8400-e29b-41d4-a716-446655440903', 'Mô tả gói dịch vụ có từ ngữ không phù hợp.', 2, 2, 'Admin đã chỉnh sửa mô tả gói.', '550e8400-e29b-41d4-a716-446655440050', '550e8400-e29b-41d4-a716-446655440080', 'f00e0000-0000-0000-0000-000000000001');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-05-01 10:01:00', '2025-05-01 10:01:00', 'e00e0000-0000-0000-0000-000000000011', 'https://example.com/evidence1.jpg');

-- Report 12: (PENDING) Customer 0055 reports Seer 0079 for IMPERSONATION
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000012', '2025-05-03 14:00:00', '2025-05-03 14:00:00', 3, '550e8400-e29b-41d4-a716-446655440079', 'Giả mạo Thầy Lý Hoàng Long.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440055', '550e8400-e29b-41d4-a716-446655440079', 'f00e0000-0000-0000-0000-000000000007');

-- Report 13: (REJECTED) Customer 0060 reports Package 0001 for FRAUD
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000013', '2025-05-10 10:00:00', '2025-05-11 09:00:00', 0, '880e8400-e29b-41d4-a716-446655440001', 'Xem không đúng gì cả.', 3, 0, 'Admin: Đã xem xét đánh giá, đây là trải nghiệm cá nhân, không phải lừa đảo.', '550e8400-e29b-41d4-a716-446655440060', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000008');

-- Report 14: (RESOLVED) Customer 0008 reports Booking 0008 for VIOLENCE
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000014', '2025-05-12 11:00:00', '2025-05-13 10:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0008', 'Seer đe dọa tôi trong buổi xem.', 2, 1, 'Admin đã cảnh cáo Seer 0071.', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000004');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-05-12 11:01:00', '2025-05-12 11:01:00', 'e00e0000-0000-0000-0000-000000000014', 'https://example.com/evidence2.jpg');

-- Report 15: (PENDING) Customer 0012 reports Conversation (Booking ...0012) for INAPPROPRIATE_CONTENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000015', '2025-05-15 14:00:00', '2025-05-15 14:00:00', 1, 'c00e8400-e29b-41d4-a716-300000000012', 'Nội dung chat không phù hợp.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440073', 'f00e0000-0000-0000-0000-000000000001');

-- Report 16: (PENDING) Customer 0023 reports Seer 0078 for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000016', '2025-06-01 10:00:00', '2025-06-01 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440078', 'Seer này có vẻ không đủ kinh nghiệm.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440078', 'f00e0000-0000-0000-0000-000000000009');

-- Report 17: (RESOLVED) Customer 0033 reports Package 1003 for COPYRIGHT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000017', '2025-06-05 11:00:00', '2025-06-06 09:00:00', 0, '880e8400-e29b-41d4-a716-446655441003', 'Sao chép nội dung gói của tôi.', 2, 2, 'Admin đã yêu cầu Seer 0081 gỡ bỏ nội dung.', '550e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440081', 'f00e0000-0000-0000-0000-000000000006');

-- Report 18: (REJECTED) Customer 0044 reports Booking 0044 for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000018', '2025-06-10 12:00:00', '2025-06-11 10:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0044', 'Không hài lòng với chất lượng.', 3, 0, 'Admin: Đã xem xét, không vi phạm quy định.', '550e8400-e29b-41d4-a716-446655440044', '550e8400-e29b-41d4-a716-446655440078', 'f00e0000-0000-0000-0000-000000000009');

-- Report 19: (RESOLVED) Customer 0051 reports Conversation (Booking ...0052) for HARASSMENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000019', '2025-06-15 13:00:00', '2025-06-16 09:00:00', 1, 'c00e8400-e29b-41d4-a716-300000000052', 'Seer liên tục nhắn tin cho tôi sau khi buổi xem kết thúc.', 2, 1, 'Admin đã cảnh cáo Seer 0075.', '550e8400-e29b-41d4-a716-446655440051', '550e8400-e29b-41d4-a716-446655440075', 'f00e0000-0000-0000-0000-000000000002');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-06-15 13:01:00', '2025-06-15 13:01:00', 'e00e0000-0000-0000-0000-000000000019', 'https://example.com/evidence1.jpg');

-- Report 20: (PENDING) Customer 0062 reports Seer 0081 for FRAUD
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000020', '2025-06-20 14:00:00', '2025-06-20 14:00:00', 3, '550e8400-e29b-41d4-a716-446655440081', 'Thầy này lừa đảo, yêu cầu chuyển khoản thêm.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440062', '550e8400-e29b-41d4-a716-446655440081', 'f00e0000-0000-0000-0000-000000000008');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
                                                                                                     (gen_random_uuid(), '2025-06-20 14:01:00', '2025-06-20 14:01:00', 'e00e0000-0000-0000-0000-000000000020', 'https://example.com/evidence4.jpg'),
                                                                                                     (gen_random_uuid(), '2025-06-20 14:01:10', '2025-06-20 14:01:10', 'e00e0000-0000-0000-0000-000000000020', 'https://example.com/evidence5.jpg');

-- Report 21: (VIEWED) Customer 0070 reports Package 0001 for SPAM
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000021', '2025-07-01 10:00:00', '2025-07-01 11:00:00', 0, '880e8400-e29b-41d4-a716-446655440001', 'Mô tả spam.', 1, 0, NULL, '550e8400-e29b-41d4-a716-446655440070', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000000');

-- Report 22: (RESOLVED) Customer 0001 reports Conversation (Admin chat) for HATE_SPEECH
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000022', '2025-07-05 11:00:00', '2025-07-06 09:00:00', 1, 'c00e8400-e29b-41d4-a716-200000000001', 'Admin sử dụng ngôn từ thù ghét.', 2, 1, 'Admin đã bị nhắc nhở nội bộ.', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'f00e0000-0000-0000-0000-000000000003');

-- Report 23: (REJECTED) Customer 0015 reports Seer 0076 for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000023', '2025-07-10 12:00:00', '2025-07-11 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440076', 'Không thích cách nói chuyện của seer này.', 3, 0, 'Admin: Không vi phạm quy định.', '550e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440076', 'f00e0000-0000-0000-0000-000000000009');

-- Report 24: (RESOLVED) Customer 0025 reports Booking 0025 for INAPPROPRIATE_CONTENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000024', '2025-07-15 13:00:00', '2025-07-16 09:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0025', 'Seer nói chuyện không phù hợp.', 2, 1, 'Admin đã cảnh cáo Seer 0071.', '550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000001');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-07-15 13:01:00', '2025-07-15 13:01:00', 'e00e0000-0000-0000-0000-000000000024', 'https://example.com/evidence3.jpg');

-- Report 25: (PENDING) Customer 0035 reports Conversation (Booking ...0034) for HARASSMENT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000025', '2025-07-20 14:00:00', '2025-07-20 14:00:00', 1, 'c00e8400-e29b-41d4-a716-300000000034', 'Seer liên tục gạ gẫm tôi.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440035', '550e8400-e29b-41d4-a716-446655440072', 'f00e0000-0000-0000-0000-000000000002');

-- Report 26: (PENDING) Customer 0045 reports Seer 0077 for IMPERSONATION
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000026', '2025-08-01 10:00:00', '2025-08-01 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440077', 'Giả mạo.', 0, 0, NULL, '550e8400-e29b-41d4-a716-446655440045', '550e8400-e29b-41d4-a716-446655440077', 'f00e0000-0000-0000-0000-000000000007');

-- Report 27: (RESOLVED) Customer 0055 reports Package 0801 for COPYRIGHT
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000027', '2025-08-05 11:00:00', '2025-08-06 09:00:00', 0, '880e8400-e29b-41d4-a716-446655440801', 'Hình ảnh vi phạm bản quyền.', 2, 2, 'Admin đã gỡ ảnh.', '550e8400-e29b-41d4-a716-446655440055', '550e8400-e29b-41d4-a716-446655440079', 'f00e0000-0000-0000-0000-000000000006');

-- Report 28: (REJECTED) Customer 0065 reports Booking 0065 for OTHER
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000028', '2025-08-10 12:00:00', '2025-08-11 10:00:00', 2, '8a09da6c-0671-4f17-82d2-0c43342d0065', 'Xem không hay.', 3, 0, 'Admin: Không vi phạm.', '550e8400-e29b-41d4-a716-446655440065', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000009');

-- Report 29: (VIEWED) Customer 0070 reports Conversation (Booking ...0070) for SPAM
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000029', '2025-08-15 13:00:00', '2025-08-16 09:00:00', 1, 'c00e8400-e29b-41d4-a716-300000000070', 'Seer spam link trong lúc chat.', 1, 0, NULL, '550e8400-e29b-41d4-a716-446655440070', '550e8400-e29b-41d4-a716-446655440071', 'f00e0000-0000-0000-0000-000000000000');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-08-15 13:01:00', '2025-08-15 13:01:00', 'e00e0000-0000-0000-0000-000000000029', 'https://example.com/evidence1.jpg');

-- Report 30: (RESOLVED) Customer 0001 reports Seer 0081 for FRAUD
INSERT INTO report (report_id, created_at, updated_at, target_type, target_id, report_description, status, action_type, note, reporter_id, reported_user_id, report_type_id) VALUES
    ('e00e0000-0000-0000-0000-000000000030', '2025-08-20 14:00:00', '2025-08-21 10:00:00', 3, '550e8400-e29b-41d4-a716-446655440081', 'Yêu cầu tôi cung cấp thông tin thẻ tín dụng.', 2, 4, 'Tài khoản Seer 0081 đã bị cấm vĩnh viễn do lừa đảo.', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440081', 'f00e0000-0000-0000-0000-000000000008');

INSERT INTO report_evidence (evidence_id, created_at, updated_at, report_id, evidence_image_url) VALUES
    (gen_random_uuid(), '2025-08-20 14:01:00', '2025-08-20 14:01:00', 'e00e0000-0000-0000-0000-000000000030', 'https://example.com/evidence5.jpg');

-- =============================================
-- END OF MIGRATION
-- =============================================