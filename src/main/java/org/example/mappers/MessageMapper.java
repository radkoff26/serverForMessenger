package org.example.mappers;

import org.example.models.Message;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.contractions.ChatContractions.*;

public class MessageMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet resultSet, int i) throws SQLException {
        return new Message(
                resultSet.getInt(ID),
                resultSet.getString(TEXT),
                resultSet.getInt(AUTHOR_ID),
                resultSet.getInt(IS_WATCHED) == 1,
                resultSet.getInt(IS_REMOVED) == 1,
                resultSet.getString(TIME)
        );
    }
}
