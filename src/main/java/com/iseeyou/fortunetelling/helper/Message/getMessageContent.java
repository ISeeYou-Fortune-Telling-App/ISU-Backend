package com.iseeyou.fortunetelling.helper.Message;

import com.iseeyou.fortunetelling.entity.Message;

public class getMessageContent {
    public static final int MESSAGE_PREVIEW_LENGTH = 100;

    private getMessageContent() { }

    public static String getMessage(Message message) {
        if (message == null) {
            return "";
        }

        String content = "";

        // Text content
        if (message.getTextContent() != null && !message.getTextContent().isEmpty()) {
            content = message.getTextContent();
        }
        // Image message
        else if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            content = "[Đã gửi hình ảnh]";
        }
        // Video message
        else if (message.getVideoUrl() != null && !message.getVideoUrl().isEmpty()) {
            content = "[Đã gửi video]";
        }

        // Truncate if too long
        if (content.length() > MESSAGE_PREVIEW_LENGTH) {
            return content.substring(0, MESSAGE_PREVIEW_LENGTH) + "...";
        }

        return content;
    }
}
