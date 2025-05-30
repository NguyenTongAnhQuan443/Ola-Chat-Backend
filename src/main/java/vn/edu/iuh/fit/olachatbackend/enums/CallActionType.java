package vn.edu.iuh.fit.olachatbackend.enums;

public enum CallActionType {
    calling,        // Người gọi đang khởi tạo cuộc gọi
    invited,        // Đã gửi lời mời tới thiết bị
    accepted,       // Người nhận đã chấp nhận cuộc gọi
    rejected,       // Người nhận đã từ chối cuộc gọi
    noAnswer,       // Không phản hồi (timeout)
}
