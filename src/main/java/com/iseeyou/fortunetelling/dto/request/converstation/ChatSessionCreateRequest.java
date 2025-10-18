package com.iseeyou.fortunetelling.dto.request.converstation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ChatSessionCreateRequest {
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
}
