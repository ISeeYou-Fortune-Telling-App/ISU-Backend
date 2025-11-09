package com.iseeyou.fortunetelling.dto.request.chat.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatFile {
    private MultipartFile image;
    private MultipartFile video;
}
