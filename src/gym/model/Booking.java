package gym.model;

import java.sql.Timestamp;

public class Booking {
    private int       id;
    private int       userId;
    private int       slotId;
    private Timestamp bookedOn;
    private String    status;

    // Joined fields (for display)
    private String    userName;
    private String    slotName;
    private String    slotDate;
    private String    startTime;
    private String    endTime;

    public Booking() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int       getId()           { return id; }
    public void      setId(int id)     { this.id = id; }

    public int       getUserId()               { return userId; }
    public void      setUserId(int userId)     { this.userId = userId; }

    public int       getSlotId()               { return slotId; }
    public void      setSlotId(int slotId)     { this.slotId = slotId; }

    public Timestamp getBookedOn()                      { return bookedOn; }
    public void      setBookedOn(Timestamp bookedOn)    { this.bookedOn = bookedOn; }

    public String    getStatus()                { return status; }
    public void      setStatus(String status)   { this.status = status; }

    public String    getUserName()                  { return userName; }
    public void      setUserName(String userName)   { this.userName = userName; }

    public String    getSlotName()                  { return slotName; }
    public void      setSlotName(String slotName)   { this.slotName = slotName; }

    public String    getSlotDate()                  { return slotDate; }
    public void      setSlotDate(String slotDate)   { this.slotDate = slotDate; }

    public String    getStartTime()                     { return startTime; }
    public void      setStartTime(String startTime)     { this.startTime = startTime; }

    public String    getEndTime()                   { return endTime; }
    public void      setEndTime(String endTime)     { this.endTime = endTime; }
}
