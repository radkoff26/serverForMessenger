package org.example.mappers;

import org.example.models.Chat;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.contractions.ChatContainerContractions.*;

public class ChatMapper implements RowMapper<Chat> {
    @Override
    public Chat mapRow(ResultSet resultSet, int i) throws SQLException {
        return new Chat(
                resultSet.getString(ID),
                resultSet.getInt(CHATTER1),
                resultSet.getInt(CHATTER2),
                resultSet.getInt(IS_REMOVED) == 1,
                resultSet.getInt(NOT_CHECKED)
        );
    }
}
