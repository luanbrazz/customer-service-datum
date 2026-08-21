package com.lb.customerservice.repository;

import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.domain.CustomerStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CustomerJdbcRepository {

    private static final String SELECT_BY_STATUS_SQL =
            "SELECT id, name, cpf, email, status, created_at, updated_at " +
                    "FROM customers WHERE status = ? ORDER BY name";

    private final JdbcTemplate jdbcTemplate;

    public CustomerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findByStatus(CustomerStatus status) {
        return jdbcTemplate.query(SELECT_BY_STATUS_SQL, CUSTOMER_ROW_MAPPER, status.name());
    }

    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Customer.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .cpf(rs.getString("cpf"))
                    .email(rs.getString("email"))
                    .status(CustomerStatus.valueOf(rs.getString("status")))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
        }
    };
}