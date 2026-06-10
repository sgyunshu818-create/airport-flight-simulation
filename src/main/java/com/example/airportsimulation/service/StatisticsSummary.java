package com.example.airportsimulation.service;

public class StatisticsSummary {
    private final long totalFlights;
    private final long departureFlights;
    private final long arrivalFlights;
    private final long completedFlights;
    private final long delayedFlights;
    private final double averageDelayMinutes;
    private final int maxDelayMinutes;
    private final long idleRunways;
    private final long idleGates;
    private final long waitingFlights;
    private final double completionRate;
    private final double delayRate;
    private final double runwayOccupancyRate;
    private final double gateOccupancyRate;

    public StatisticsSummary(
            long totalFlights,
            long departureFlights,
            long arrivalFlights,
            long completedFlights,
            long delayedFlights,
            double averageDelayMinutes,
            int maxDelayMinutes,
            long idleRunways,
            long idleGates,
            long waitingFlights) {
        this(totalFlights, departureFlights, arrivalFlights, completedFlights, delayedFlights,
                averageDelayMinutes, maxDelayMinutes, idleRunways, idleGates, waitingFlights,
                0.0, 0.0, 0.0, 0.0);
    }

    public StatisticsSummary(
            long totalFlights,
            long departureFlights,
            long arrivalFlights,
            long completedFlights,
            long delayedFlights,
            double averageDelayMinutes,
            int maxDelayMinutes,
            long idleRunways,
            long idleGates,
            long waitingFlights,
            double completionRate,
            double delayRate,
            double runwayOccupancyRate,
            double gateOccupancyRate) {
        this.totalFlights = totalFlights;
        this.departureFlights = departureFlights;
        this.arrivalFlights = arrivalFlights;
        this.completedFlights = completedFlights;
        this.delayedFlights = delayedFlights;
        this.averageDelayMinutes = averageDelayMinutes;
        this.maxDelayMinutes = maxDelayMinutes;
        this.idleRunways = idleRunways;
        this.idleGates = idleGates;
        this.waitingFlights = waitingFlights;
        this.completionRate = completionRate;
        this.delayRate = delayRate;
        this.runwayOccupancyRate = runwayOccupancyRate;
        this.gateOccupancyRate = gateOccupancyRate;
    }

    public long getTotalFlights() { return totalFlights; }

    public long getDepartureFlights() { return departureFlights; }

    public long getArrivalFlights() { return arrivalFlights; }

    public long getCompletedFlights() { return completedFlights; }

    public long getDelayedFlights() { return delayedFlights; }

    public double getAverageDelayMinutes() { return averageDelayMinutes; }

    public int getMaxDelayMinutes() { return maxDelayMinutes; }

    public long getIdleRunways() { return idleRunways; }

    public long getIdleGates() { return idleGates; }

    public long getWaitingFlights() { return waitingFlights; }

    public double getCompletionRate() { return completionRate; }

    public double getDelayRate() { return delayRate; }

    public double getRunwayOccupancyRate() { return runwayOccupancyRate; }

    public double getGateOccupancyRate() { return gateOccupancyRate; }
}
