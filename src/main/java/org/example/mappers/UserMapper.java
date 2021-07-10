package org.example.mappers;

import org.example.models.User;

import static org.example.contractions.UsersContractions.*;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        return new User(
                resultSet.getInt(ID),
                resultSet.getString(NICKNAME),
                resultSet.getString(LOGIN),
                resultSet.getString(PASSWORD),
                resultSet.getString(CHAT_CONTAINER_NAME),
                resultSet.getString(LAST_ONLINE),
                resultSet.getString(AVATAR_URL),
                resultSet.getInt(IS_ONLINE) == 1
        );
    }
}
