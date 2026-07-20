package com.gym.server;

import com.gym.dao.BookingDAO;
import com.gym.dao.SlotDAO;
import com.gym.dao.UserDAO;
import com.gym.model.Slot;
import com.gym.model.User;
import com.gym.util.DBConnection;
import com.gym.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class GymServer {

    private static final int    PORT         = 8080;
    private static final String FRONTEND_DIR = "frontend";

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/register",  GymServer::handleRegister);
        server.createContext("/api/login",     GymServer::handleLogin);
        server.createContext("/api/slots",     GymServer::handleSlots);
        server.createContext("/api/book",      GymServer::handleBook);
        server.createContext("/api/bookings",  GymServer::handleBookings);
        server.createContext("/api/cancel",    GymServer::handleCancel);
        server.createContext("/api/users",     GymServer::handleUsers);
        server.createContext("/",              GymServer::handleStatic);

        server.setExecutor(null);
        server.start();

        System.out.println("==========================================");
        System.out.println("  Gym Management System STARTED");
        System.out.println("  Open: http://localhost:" + PORT);
        System.out.println("==========================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            DBConnection.closeConnection();
            System.out.println("Server stopped.");
        }));
    }

    // /api/register
    private static void handleRegister(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { methodNotAllowed(ex); return; }

        Map<String, String> p = parseBody(ex);
        String name     = p.getOrDefault("name",     "").trim();
        String email    = p.getOrDefault("email",    "").trim();
        String phone    = p.getOrDefault("phone",    "").trim();
        String password = p.getOrDefault("password", "").trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            sendJson(ex, 400, JsonUtil.error("All fields are required.")); return;
        }

        UserDAO dao = new UserDAO();
        if (dao.emailExists(email)) {
            sendJson(ex, 409, JsonUtil.error("Email already registered.")); return;
        }

        User u = new User();
        u.setName(name); u.setEmail(email); u.setPhone(phone); u.setPassword(password);

        boolean ok = dao.registerUser(u);
        sendJson(ex, ok ? 200 : 500,
                 ok ? JsonUtil.ok("Registration successful!")
                    : JsonUtil.error("Registration failed. Please try again."));
    }

    // /api/login
    private static void handleLogin(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { methodNotAllowed(ex); return; }

        Map<String, String> p = parseBody(ex);
        String email    = p.getOrDefault("email",    "").trim();
        String password = p.getOrDefault("password", "").trim();

        if (email.isEmpty() || password.isEmpty()) {
            sendJson(ex, 400, JsonUtil.error("Email and password are required.")); return;
        }

        User u = new UserDAO().loginUser(email, password);
        if (u == null) {
            sendJson(ex, 401, JsonUtil.error("Invalid email or password.")); return;
        }

        sendJson(ex, 200, "{\"success\":true,\"user\":" + JsonUtil.userToJson(u) + "}");
    }

    // /api/slots  and  /api/slots/all
    private static void handleSlots(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        if (path.endsWith("/all") && method.equalsIgnoreCase("GET")) {
            sendJson(ex, 200, JsonUtil.slotsToJson(new SlotDAO().getAllSlots()));
            return;
        }

        if (method.equalsIgnoreCase("GET")) {
            String date = queryParam(ex, "date");
            if (date == null || date.isEmpty()) date = java.time.LocalDate.now().toString();
            sendJson(ex, 200, JsonUtil.slotsToJson(new SlotDAO().getSlotsByDate(date)));
            return;
        }

        if (method.equalsIgnoreCase("POST")) {
            Map<String, String> p = parseBody(ex);
            Slot s = new Slot();
            s.setSlotName(p.getOrDefault("slotName", ""));
            s.setStartTime(p.getOrDefault("startTime", ""));
            s.setEndTime(p.getOrDefault("endTime", ""));
            try {
                s.setMaxCapacity(Integer.parseInt(p.getOrDefault("maxCapacity", "20")));
            } catch (NumberFormatException e) {
                s.setMaxCapacity(20);
            }
            s.setSlotDate(p.getOrDefault("slotDate", ""));
            boolean ok = new SlotDAO().addSlot(s);
            sendJson(ex, ok ? 200 : 500,
                     ok ? JsonUtil.ok("Slot added.") : JsonUtil.error("Failed to add slot."));
            return;
        }

        methodNotAllowed(ex);
    }

    // /api/book
    private static void handleBook(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { methodNotAllowed(ex); return; }

        Map<String, String> p = parseBody(ex);
        int userId, slotId;
        try {
            userId = Integer.parseInt(p.getOrDefault("userId", "0"));
            slotId = Integer.parseInt(p.getOrDefault("slotId", "0"));
        } catch (NumberFormatException e) {
            sendJson(ex, 400, JsonUtil.error("Invalid userId or slotId.")); return;
        }

        int result = new BookingDAO().bookSlot(userId, slotId);
        switch (result) {
            case 0  -> sendJson(ex, 200, JsonUtil.ok("Booking confirmed!"));
            case 1  -> sendJson(ex, 409, JsonUtil.error("Sorry, this slot is fully booked."));
            case 2  -> sendJson(ex, 409, JsonUtil.error("You already have a booking for this slot."));
            default -> sendJson(ex, 500, JsonUtil.error("Booking failed. Please try again."));
        }
    }

    // /api/bookings  and  /api/bookings/all
    private static void handleBookings(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) { methodNotAllowed(ex); return; }

        String path = ex.getRequestURI().getPath();
        BookingDAO dao = new BookingDAO();

        if (path.endsWith("/all")) {
            sendJson(ex, 200, JsonUtil.bookingsToJson(dao.getAllBookings()));
            return;
        }

        String uid = queryParam(ex, "userId");
        if (uid == null) { sendJson(ex, 400, JsonUtil.error("userId param missing.")); return; }

        try {
            sendJson(ex, 200, JsonUtil.bookingsToJson(dao.getBookingsByUser(Integer.parseInt(uid))));
        } catch (NumberFormatException e) {
            sendJson(ex, 400, JsonUtil.error("Invalid userId."));
        }
    }

    // /api/cancel
    private static void handleCancel(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { methodNotAllowed(ex); return; }

        Map<String, String> p = parseBody(ex);
        int bookingId;
        try {
            bookingId = Integer.parseInt(p.getOrDefault("bookingId", "0"));
        } catch (NumberFormatException e) {
            sendJson(ex, 400, JsonUtil.error("Invalid bookingId.")); return;
        }

        boolean ok = new BookingDAO().cancelBooking(bookingId);
        sendJson(ex, ok ? 200 : 500,
                 ok ? JsonUtil.ok("Booking cancelled.") : JsonUtil.error("Cancel failed."));
    }

    // /api/users
    private static void handleUsers(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        UserDAO dao = new UserDAO();

        if (ex.getRequestMethod().equalsIgnoreCase("GET")) {
            sendJson(ex, 200, JsonUtil.usersToJson(dao.getAllUsers()));
            return;
        }

        if (ex.getRequestMethod().equalsIgnoreCase("DELETE")) {
            String id = queryParam(ex, "id");
            if (id == null) { sendJson(ex, 400, JsonUtil.error("id param missing.")); return; }
            try {
                boolean ok = dao.deleteUser(Integer.parseInt(id));
                sendJson(ex, ok ? 200 : 500,
                         ok ? JsonUtil.ok("User deleted.") : JsonUtil.error("Delete failed."));
            } catch (NumberFormatException e) {
                sendJson(ex, 400, JsonUtil.error("Invalid id."));
            }
            return;
        }

        methodNotAllowed(ex);
    }

    // Static file server
    private static void handleStatic(HttpExchange ex) throws IOException {
        String uriPath = ex.getRequestURI().getPath();
        if (uriPath.equals("/")) uriPath = "/index.html";

        Path filePath = Paths.get(FRONTEND_DIR + uriPath);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            filePath = Paths.get(FRONTEND_DIR + "/index.html");
        }

        if (!Files.exists(filePath)) {
            byte[] body = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(404, body.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(body); }
            return;
        }

        byte[] body = Files.readAllBytes(filePath);
        ex.getResponseHeaders().set("Content-Type", mimeType(filePath.toString()));
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    // --- Helpers ---

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",                 "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    private static boolean handleOptions(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) return false;
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(204, -1);
        ex.getResponseBody().close();
        return true;
    }

    private static void methodNotAllowed(HttpExchange ex) throws IOException {
        sendJson(ex, 405, JsonUtil.error("Method not allowed."));
    }

    private static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        String raw;
        try (InputStream is = ex.getRequestBody()) {
            raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String queryParam(HttpExchange ex, String key) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key))
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        return null;
    }

    private static String mimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}
