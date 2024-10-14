package org.example.bus_tracker_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RootEntityService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void executeRawInsert() {

        jdbcTemplate.update("INSERT INTO root_entity (root_id, starting_x, ending_x, root_function) " +
                "SELECT '1', 200, 1000, '800 - 300 * Math.pow(x / 1000 - 0.4, 6) + 200 * Math.pow(x / 1000 - 0.6, 4) + 100 * Math.pow(x / 1000 - 0.5, 2)' " +
                "WHERE NOT EXISTS (SELECT 1 FROM root_entity WHERE root_id = '1'); ");

        jdbcTemplate.update("INSERT INTO root_entity (root_id, starting_x, ending_x, root_function) " +
                "SELECT '2', 34, 632, '400 - 350 * Math.pow((x / 1000.0 - 0.25), 6) ' " +
                "WHERE NOT EXISTS (SELECT 1 FROM root_entity WHERE root_id = '2');");

        jdbcTemplate.update("INSERT INTO root_entity (root_id, starting_x, ending_x, root_function) " +
                "SELECT '3', 12, 222, '400 - 300 * Math.pow((x / 1000.0 - 0.3), 6) ' " +
                "WHERE NOT EXISTS (SELECT 1 FROM root_entity WHERE root_id = '3'); ");



        jdbcTemplate.update("INSERT INTO bus_entity (session_id, bus_id, day_of_week, root_id, starting_time) " +
                "SELECT 1, 'dl435', 1, 1, '10:20' WHERE NOT EXISTS (SELECT 1 FROM bus_entity WHERE session_id = 1);");

        jdbcTemplate.update("INSERT INTO bus_entity (session_id, bus_id, day_of_week, root_id, starting_time) " +
                "SELECT 2, 'dl642', 1, 1, '11:30' WHERE NOT EXISTS (SELECT 1 FROM bus_entity WHERE session_id = 2);");

        jdbcTemplate.update("INSERT INTO bus_entity (session_id, bus_id, day_of_week, root_id, starting_time) " +
                "SELECT 3, 'dl735', 1, 1, '12:30' WHERE NOT EXISTS (SELECT 1 FROM bus_entity WHERE session_id = 3);");



        jdbcTemplate.update("INSERT INTO bus_stop_entity (root_id, x_coordinate, expected_time)" +
                "SELECT '1', 188, 5 " +
                "WHERE NOT EXISTS (SELECT 1 FROM bus_stop_entity WHERE root_id = '1' AND x_coordinate = 188);");

        jdbcTemplate.update("INSERT INTO bus_stop_entity (root_id, x_coordinate, expected_time) " +
                "SELECT '1', 672, 10 " +
                "WHERE NOT EXISTS (SELECT 1 FROM bus_stop_entity WHERE root_id = '1' AND x_coordinate = 672)");

    }
}

