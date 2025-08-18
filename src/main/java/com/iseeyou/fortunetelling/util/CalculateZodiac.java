package com.iseeyou.fortunetelling.util;

public final class CalculateZodiac {
    public static String getZodiacSign(int month, int day) {
        String zodiacSign = "";
        switch (month) {
            case 1:
                zodiacSign = (day < 20) ? "Ma Kết" : "Bảo Bình";
                break;
            case 2:
                zodiacSign = (day < 19) ? "Bảo Bình" : "Song Ngư";
                break;
            case 3:
                zodiacSign = (day < 21) ? "Song Ngư" : "Bạch Dương";
                break;
            case 4:
                zodiacSign = (day < 20) ? "Bạch Dương" : "Kim Ngưu";
                break;
            case 5:
                zodiacSign = (day < 21) ? "Kim Ngưu" : "Song Tử";
                break;
            case 6:
                zodiacSign = (day < 21) ? "Song Tử" : "Cự Giải";
                break;
            case 7:
                zodiacSign = (day < 23) ? "Cự Giải" : "Sư Tử";
                break;
            case 8:
                zodiacSign = (day < 23) ? "Sư Tử" : "Xử Nữ";
                break;
            case 9:
                zodiacSign = (day < 23) ? "Xử Nữ" : "Thiên Bình";
                break;
            case 10:
                zodiacSign = (day < 23) ? "Thiên Bình" : "Bọ Cạp";
                break;
            case 11:
                zodiacSign = (day < 22) ? "Bọ Cạp" : "Nhân Mã";
                break;
            case 12:
                zodiacSign = (day < 22) ? "Nhân Mã" : "Ma Kết";
                break;
        }
        return zodiacSign;
    }

    public static String getChineseZodiac(int year) {
        String[] chineseZodiac = {
                "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
                "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
        };
        return chineseZodiac[(year - 4) % 12];
    }

    public static String getFiveElements(int year) {
        String[] fiveElements = {
                "Kim", "Mộc", "Thủy", "Hỏa", "Thổ"
        };
        return fiveElements[(year - 4) % 10 / 2];
    }
}