package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import com.iseeyou.fortunetelling.repository.KnowledgeCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class Knowledge {
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;

    public void createDummyData() {
        createCategory();
    }

    private void createCategory() {
        knowledgeCategoryRepository.save(new KnowledgeCategory("Cung Hoàng Đạo", "Thông tin về tính cách, tình duyên và sự nghiệp theo 12 cung hoàng đạo"));
        knowledgeCategoryRepository.save(new KnowledgeCategory("Nhân Tướng Học", "Giải mã tính cách và vận mệnh qua khuôn mặt, dáng người"));
        knowledgeCategoryRepository.save(new KnowledgeCategory("Ngũ Hành", "Phân tích sự tương sinh, tương khắc của Kim - Mộc - Thủy - Hỏa - Thổ"));
        knowledgeCategoryRepository.save(new KnowledgeCategory("Chỉ Tay", "Xem vận mệnh, tình duyên và sự nghiệp qua đường chỉ tay"));
        knowledgeCategoryRepository.save(new KnowledgeCategory("Tarot", "Giải bài tarot để tìm lời khuyên và định hướng cho cuộc sống"));
        knowledgeCategoryRepository.save(new KnowledgeCategory("Khác", "Các hình thức xem bói và dự đoán khác"));

        log.info("Dummy knowledge categories created successfully.");
    }
}
