import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PassengerManagement {

    private List<Passenger> passengers;
    private FlightManagement flightManagement;
    private String passengerFilePath = "passengers.txt";
    private String passengerRemovedFilePath = "passengerRemoved.txt";
    private String cancelledBookingsFilePath = "cancelledBookings.txt";

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";

    public PassengerManagement(FlightManagement fm) {
        this.passengers = new ArrayList<>();
        this.flightManagement = fm;
        loadPassengersFromFile();
    }

    // ============================================================
    // FILE OPERATIONS (unchanged)
    // ============================================================
    private void loadPassengersFromFile() { /* same as original */ }
    public void savePassengersToFile() { /* same */ }
    private void saveRemovedPassengers(List<Passenger> removedPassengers) { /* same */ }
    public void saveCancelledBooking(Passenger p) { /* same */ }
    public void clearRemovedPassengersFile() { /* same */ }
    public void clearCancelledBookingsFile() { /* same */ }
    private Passenger findPassengerByTicket(String ticketId) { /* same */ }
    public List<Passenger> getAllPassengers() { return passengers; }
    public void updatePassengersFlightTimes(String flightInstanceId, LocalDateTime newDepartTime) { /* same */ }
    public void cancelBooking(Passenger p) { /* same */ }
    public void freePassengersOfFlight(String flightInstanceId) { /* same */ }
    public void addPassenger(Passenger p) { /* same */ }
    public void displayPassengers() { /* same */ }
    public void displayCancelledBookings() { /* same */ }

    // ============================================================
    // CHECK-IN METHODS (overloaded for name verification)
    // ============================================================

    // Original method (used by logged‑in passengers – no name check)
    public void checkIn(String ticketId, LocalDateTime currentTime) {
        checkIn(ticketId, null, currentTime);  // call overloaded version with null name
    }

    // New method with optional name verification (pass null to skip name check)
    public void checkIn(String ticketId, String passengerName, LocalDateTime currentTime) {
        Passenger p = findPassengerByTicket(ticketId);
        if (p == null) {
            System.out.println(RED + " Invalid ticket." + RESET);
            return;
        }
        // Verify name if provided
        if (passengerName != null && !p.getPassengerName().equalsIgnoreCase(passengerName.trim())) {
            System.out.println(RED + " Name does not match the ticket." + RESET);
            return;
        }

        // === Existing check‑in logic (copy from your original method) ===
        if (p.isBoardingPassIssued()) {
            System.out.println(YELLOW + " Boarding pass already issued." + RESET);
            return;
        }
        if (p.hasCheckedIn()) {
            System.out.println(YELLOW + " Already checked in. Please proceed to gate." + RESET);
            return;
        }
        Flight flight = flightManagement.FindFlightByInstanceId(p.getFlightInstanceId());
        if (flight == null) {
            System.out.println(RED + " Flight not found." + RESET);
            return;
        }

        LocalDateTime passengerDepartTime = p.getJourneyDateTime();
        LocalDateTime flightBoardingStart = flight.getScheduledActionTime();
        LocalDateTime flightBoardingClose = passengerDepartTime.minusMinutes(15);
        LocalDateTime flightCheckInStart = passengerDepartTime.minusHours(2);
        LocalDateTime flightCheckInEnd = flightBoardingClose.minusMinutes(30);

        boolean flightTimeChanged = !p.getJourneyDateTime().equals(flight.getDepartDateTime());

        if (flightTimeChanged) {
            System.out.println(YELLOW + "⚠️ Flight time changed." + RESET);
            System.out.println("   Original departure: " + p.getJourneyDateTime());
            System.out.println("   New departure: " + flight.getDepartDateTime());
            System.out.println("   New check-in starts at: " + flightCheckInStart);
            System.out.println("   New check-in closes at: " + flightCheckInEnd);
            System.out.println("   New boarding starts at: " + flightBoardingStart);
            System.out.println("   New boarding closes at: " + flightBoardingClose);

            p.setJourneyDateTime(flight.getDepartDateTime());
            p.setCheckInStartTime(flightCheckInStart);
            savePassengersToFile();
        }

        if (currentTime.isBefore(flightCheckInStart)) {
            System.out.println(RED + " Too early to check in." + RESET);
            System.out.println("   Check-in starts at: " + flightCheckInStart);
            if (flightTimeChanged) {
                System.out.println(YELLOW + "   (Flight was delayed. Please return at the new check-in time.)" + RESET);
            }
            return;
        }
        if (currentTime.isAfter(flightCheckInEnd)) {
            System.out.println(RED + " Sorry! Check-in is closed." + RESET);
            System.out.println("   Check-in closed at: " + flightCheckInEnd);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Boarding closes at: " + flightBoardingClose);
            System.out.println("   You needed 30 minutes to reach the gate.");
            if (flightTimeChanged) {
                System.out.println(YELLOW + "   (Flight was delayed, but you arrived after check-in closed.)" + RESET);
            }
            return;
        }

        p.setCheckedIn(true);
        savePassengersToFile();

        long minutesUntilBoarding = Duration.between(currentTime, flightBoardingStart).toMinutes();
        String gateInfo = flight.getGateId();
        if (gateInfo == null || gateInfo.equals("-")) {
            gateInfo = "Will be assigned at boarding";
        }

        System.out.println(GREEN + "\n Check-in successful!" + RESET);
        System.out.println(CYAN + "┌────────────────────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Passenger", p.getPassengerName());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Flight", flight.getFlightInstanceId());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Gate", gateInfo);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Starts", flightBoardingStart);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Closes", flightBoardingClose);
        System.out.println(CYAN + "└────────────────────────────────────────────────────────────────────────────┘" + RESET);

        if (minutesUntilBoarding <= 0) {
            System.out.println(YELLOW + "   ⚠️ Boarding is already happening! Please go to gate IMMEDIATELY." + RESET);
        } else if (minutesUntilBoarding <= 15) {
            System.out.println(YELLOW + "   ⚠️ Boarding starts in " + minutesUntilBoarding + " minutes. Please go to gate soon." + RESET);
        } else {
            System.out.println(GREEN + "   You have " + minutesUntilBoarding + " minutes until boarding starts." + RESET);
        }
        System.out.println("   Please proceed to gate before boarding closes.\n");
    }

    // ============================================================
    // BOARDING PASS METHODS (overloaded for name verification)
    // ============================================================

    // Original method (logged‑in passengers)
    public void processBoarding(String ticketId, LocalDateTime currentTime) {
        processBoarding(ticketId, null, currentTime);
    }

    // New method with optional name verification
    public void processBoarding(String ticketId, String passengerName, LocalDateTime currentTime) {
        Passenger p = findPassengerByTicket(ticketId);
        if (p == null) {
            System.out.println(RED + " Invalid ticket." + RESET);
            return;
        }
        if (passengerName != null && !p.getPassengerName().equalsIgnoreCase(passengerName.trim())) {
            System.out.println(RED + " Name does not match the ticket." + RESET);
            return;
        }

        // === Existing boarding pass logic (copy from your original method) ===
        if (p.isBoardingPassIssued()) {
            System.out.println(YELLOW + " Boarding pass already issued." + RESET);
            return;
        }
        if (!p.hasCheckedIn()) {
            System.out.println(RED + "Please check in first at the counter." + RESET);
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(p.getFlightInstanceId());
        if (flight == null) {
            System.out.println(RED + " Flight not found." + RESET);
            return;
        }

        LocalDateTime flightDepartTime = flight.getDepartDateTime();
        LocalDateTime flightBoardingStart = flight.getScheduledActionTime();
        LocalDateTime flightBoardingClose = flightDepartTime.minusMinutes(15);

        boolean flightTimeChanged = !p.getJourneyDateTime().equals(flightDepartTime);

        if (flightTimeChanged) {
            System.out.println(YELLOW + " Flight time has changed since you checked in." + RESET);
            System.out.println("   Your original departure: " + p.getJourneyDateTime());
            System.out.println("   New departure: " + flightDepartTime);
            System.out.println("   New boarding starts at: " + flightBoardingStart);
            System.out.println("   New boarding closes at: " + flightBoardingClose);

            p.setJourneyDateTime(flightDepartTime);
            p.setCheckInStartTime(flightDepartTime.minusHours(2));
            savePassengersToFile();

            flightBoardingStart = flight.getScheduledActionTime();
            flightBoardingClose = flightDepartTime.minusMinutes(15);
        }

        if (currentTime.isBefore(flightBoardingStart)) {
            long minutesUntilBoarding = Duration.between(currentTime, flightBoardingStart).toMinutes();
            System.out.println(RED + " Boarding hasn't started yet." + RESET);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Please return in " + minutesUntilBoarding + " minutes.");
            return;
        }
        if (currentTime.isAfter(flightBoardingClose)) {
            System.out.println(RED + " Sorry! Boarding is over." + RESET);
            System.out.println("   Boarding closed at: " + flightBoardingClose);
            System.out.println("   Flight departed at: " + flightDepartTime);
            return;
        }
        if (!"BOARDING".equalsIgnoreCase(flight.getStatus())) {
            System.out.println(RED + " Flight not boarding yet." + RESET);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Current flight status: " + flight.getStatus());
            return;
        }

        String gateId = flight.getGateId();
        if (gateId == null || gateId.equals("-")) {
            flightManagement.assignGate(flight);
            gateId = flight.getGateId();
        }

        p.issueBoardingPass(gateId);
        savePassengersToFile();

        long minutesUntilDeparture = Duration.between(currentTime, flightDepartTime).toMinutes();
        long minutesUntilBoardingCloses = Duration.between(currentTime, flightBoardingClose).toMinutes();

        System.out.println(GREEN + "\n Boarding pass issued!" + RESET);
        System.out.println(CYAN + "┌────────────────────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Passenger", p.getPassengerName());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Gate", gateId);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Flight", flight.getFlightInstanceId());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Departure", flightDepartTime);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Closes", flightBoardingClose);
        System.out.println(CYAN + "└────────────────────────────────────────────────────────────────────────────┘" + RESET);

        if (minutesUntilBoardingCloses <= 5) {
            System.out.println(RED + "    Boarding closes in " + minutesUntilBoardingCloses + " minutes! Please board immediately." + RESET);
        } else {
            System.out.println(GREEN + "   You have " + minutesUntilDeparture + " minutes until departure." + RESET);
        }
        System.out.println("   Please board the plane now.\n");
    }

    // ============================================================
    // HELPER METHODS (unchanged)
    // ============================================================
    private String truncate(String str, int maxLen) { /* same */ }
    private String padRight(String s, int width) { /* same */ }
    private String centerText(String text, int width) { /* same */ }
}
