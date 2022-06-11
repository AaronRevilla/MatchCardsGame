package revilla.aaron.showtime.models

enum class ServerStatusResponse(val status: Int) {
    SUCCESS(200),
    UNAUTHORIZED(401),
    SERVER_ERROR(500)
}