//package org.example.bus_tracker_backend;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//@Service("busRouteService") // Explicitly define the bean name
//public class BusRouteService implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    public BusRouteService(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        createDatabaseAndTable();
//        insertBusRoutes();
//    }
//
//    private void createDatabaseAndTable() {
//        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS database_1");
//        jdbcTemplate.execute("USE database_1");
//        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS bus_routes (" +
//                "root_id INT PRIMARY KEY, " +
//                "ending_x INT, " +
//                "root_function VARCHAR(255), " +
//                "starting_x INT)");
//    }
//
//    private void insertBusRoutes() {
//        jdbcTemplate.execute("INSERT INTO bus_routes (root_id, ending_x, root_function, starting_x) " +
//                "VALUES (1, 1000, '800 - 300 * Math.pow((x / 1000 - 0.4), 6) + 200', 200)");
//        jdbcTemplate.execute("INSERT INTO bus_routes (root_id, ending_x, root_function, starting_x) " +
//                "VALUES (2, 632, '400 - 350 * Math.pow((x / 1000 - 0.25), 6)', 34)");
//        jdbcTemplate.execute("INSERT INTO bus_routes (root_id, ending_x, root_function, starting_x) " +
//                "VALUES (3, 222, '400 - 300 * Math.pow((x / 1000 - 0.3), 6)', 12)");
//    }
//}
