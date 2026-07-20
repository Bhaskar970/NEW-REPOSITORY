package com.gym.model;

/**
 * Represents a gym time-slot stored in the `slots` table.
 */
public class Slot {
    private int    id;
    private String slotName;
    private String startTime;
    private String endTime;
    private int    maxCapacity;
    private String slotDate;
    private int    currentBookings;  // computed, not persisted

    public Slot() {}

    public Slot(int id, String slotName, String startTime, String endTime,
                int maxCapacity, String slotDate) {
        this.id          = id;
        this.slotName    = slotName;
        this.startTime   = startTime;
        this.endTime     = endTime;
        this.maxCapacity = maxCapacity;
        this.slotDate    = slotDate;
    }

    // ── Getters & Setters ────────────────────────────────────────────

    public int    getId()            { return id; }
    public void   setId(int id)      { this.id = id; }

    public String getSlotName()                { return slotName; }
    public void   setSlotName(String slotName) { this.slotName = slotName; }

    public String getStartTime()                 { return startTime; }
    public void   setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime()               { return endTime; }
    public void   setEndTime(String endTime) { this.endTime = endTime; }

    public int  getMaxCapacity()               { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getSlotDate()                { return slotDate; }
    public void   setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public int  getCurrentBookings()                  { return currentBookings; }
    public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }

    /** Returns the number of seats remaining in this slot. */
    public int getAvailableSeats() { return maxCapacity - currentBookings; }

    /** True when no more seats are available. */
    public boolean isFull() { return currentBookings >= maxCapacity; }

    @Override
    public String toString() {
        return "Slot{id=" + id + ", name='" + slotName + "', date='" + slotDate +
               "', " + currentBookings + "/" + maxCapacity + "}";
    }
}
