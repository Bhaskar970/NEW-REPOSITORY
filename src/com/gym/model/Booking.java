package com.gym.model;

/**
 * Represents a booking record in the `bookings` table.
 */
public class Booking {
    private int    id;
    private int    userId;
    private int    slotId;
    private String status;       // "confirmed" | "cancelled"
    private String bookedAt;

    // Joined / display fields (not in DB columns)
    private String userName;
    private String slotName;
    private String slotDate;
    private String startTime;
    private String endTime;

    public Booking() {}

    public Booking(int id, int userId, int slotId, String status) {
        this.id     = id;
        this.userId = userId;
        this.slotId = slotId;
        this.status = status;
    }

    // ── Getters & Setters ────────────────────────────────────────────

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public int    getUserId()            { return userId; }
    public void   setUserId(int userId)  { this.userId = userId; }

    public int    getSlotId()            { return slotId; }
    public void   setSlotId(int slotId)  { this.slotId = slotId; }

    public String getStatus()              { return status; }
    public void   setStatus(String status) { this.status = status; }

    public String getBookedAt()                { return bookedAt; }
    public void   setBookedAt(String bookedAt) { this.bookedAt = bookedAt; }

    public String getUserName()                { return userName; }
    public void   setUserName(String userName) { this.userName = userName; }

    public String getSlotName()                { return slotName; }
    public void   setSlotName(String slotName) { this.slotName = slotName; }

    public String getSlotDate()                { return slotDate; }
    public void   setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public String getStartTime()                 { return startTime; }
    public void   setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime()               { return endTime; }
    public void   setEndTime(String endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", userId=" + userId + ", slotId=" + slotId +
               ", status='" + status + "'}";
    }
}
