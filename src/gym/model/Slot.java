package gym.model;

import java.sql.Date;
import java.sql.Time;

public class Slot {
    private int    id;
    private String slotName;
    private Time   startTime;
    private Time   endTime;
    private int    maxCapacity;
    private Date   slotDate;
    private boolean active;
    private int    bookedCount;     // populated from view
    private int    availableSpots;  // populated from view

    public Slot() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int     getId()             { return id; }
    public void    setId(int id)       { this.id = id; }

    public String  getSlotName()                    { return slotName; }
    public void    setSlotName(String slotName)     { this.slotName = slotName; }

    public Time    getStartTime()                   { return startTime; }
    public void    setStartTime(Time startTime)     { this.startTime = startTime; }

    public Time    getEndTime()                 { return endTime; }
    public void    setEndTime(Time endTime)     { this.endTime = endTime; }

    public int     getMaxCapacity()                     { return maxCapacity; }
    public void    setMaxCapacity(int maxCapacity)       { this.maxCapacity = maxCapacity; }

    public Date    getSlotDate()                { return slotDate; }
    public void    setSlotDate(Date slotDate)   { this.slotDate = slotDate; }

    public boolean isActive()               { return active; }
    public void    setActive(boolean active) { this.active = active; }

    public int     getBookedCount()                     { return bookedCount; }
    public void    setBookedCount(int bookedCount)       { this.bookedCount = bookedCount; }

    public int     getAvailableSpots()                      { return availableSpots; }
    public void    setAvailableSpots(int availableSpots)    { this.availableSpots = availableSpots; }

    @Override
    public String toString() {
        return "Slot{id=" + id + ", name='" + slotName + "', date=" + slotDate +
               ", available=" + availableSpots + "/" + maxCapacity + "}";
    }
}
