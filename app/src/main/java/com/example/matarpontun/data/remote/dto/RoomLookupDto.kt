package com.example.matarpontun.data.remote.dto

/** Response from GET /wards/rooms/qr/{qrCode} — identifies the ward and room for a scanned QR code. */
data class RoomLookupDto(
    val wardId: Long,
    val roomNumber: String,
    val qrCode: String
)
