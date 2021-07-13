package org.example.repositories;

import org.example.contractions.ChatContainerContractions;
import org.example.contractions.ChatContractions;
import org.example.contractions.UsersContractions;
import org.example.crypt.Crypt;
import org.example.crypt.RestCrypt;
import org.example.crypt.ServerCrypt;
import org.example.mappers.ChatMapper;
import org.example.mappers.MessageMapper;
import org.example.mappers.UserMapper;
import org.example.models.Chat;
import org.example.models.DateConverter;
import org.example.models.Message;
import org.example.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.contractions.ChatContractions.ID;
import static org.example.contractions.UsersContractions.*;
import static org.example.contractions.ChatContainerContractions.*;
import static org.example.contractions.ChatContractions.*;

@Repository
public class UserRepository {

    public static final String MESSENGER = "messenger";
    public static final String CONTAINERS = "containers";
    public static final String CHATS = "chats";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean createAccount(String nickname, String login, String password) {
        jdbcTemplate.execute("USE " + MESSENGER);
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users",
                new UserMapper());
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getLogin().equals(login)) count++;
        }
        if (count != 0) {
            return false;
        }
        Crypt crypt = new Crypt(RestCrypt.KEY);
        String finalPassword = crypt.decode(password);
        crypt = new Crypt(ServerCrypt.KEY);
        finalPassword = crypt.encode(finalPassword);
        String chat_container;
        if (users.size() == 0) {
            chat_container = 1 + "_container";
        } else {
            chat_container = (users.get(users.size() - 1).getId() + 1) + "_container";
        }
        String time = DateConverter.now();
        jdbcTemplate.execute(String.format(
                "INSERT INTO users (%s, %s, %s, %s, %s, %s, %s) VALUES('%s', '%s', '%s', '%s', '%s', '%s', %d)",
                LOGIN, PASSWORD, CHAT_CONTAINER_NAME, NICKNAME, LAST_ONLINE, AVATAR_URL, IS_ONLINE,
                login, finalPassword, chat_container, nickname, time, null, 1
                )
        );
        jdbcTemplate.execute("USE " + CONTAINERS);
        jdbcTemplate.execute(
                String.format("CREATE TABLE %s " +
                                "(%s VARCHAR(255) NOT NULL PRIMARY KEY, " +
                                "%s INT NOT NULL, " +
                                "%s INT NOT NULL, " +
                                "%s INT)",
                        chat_container,
                        ChatContainerContractions.ID,
                        CHATTER1,
                        CHATTER2,
                        ChatContainerContractions.IS_REMOVED)
        );
        return true;
    }

    public void sendMessage(Integer senderId, Integer receiverId, String text) {
        jdbcTemplate.execute("USE " + MESSENGER);
        User sender = jdbcTemplate.query("SELECT * FROM users where id=" + senderId,
                new UserMapper()).get(0);
        String var1 = senderId + "_" + receiverId;
        String var2 = receiverId + "_" + senderId;
        String res = null;
        jdbcTemplate.execute("USE " + CONTAINERS);
        List<Chat> chatsVar1 = jdbcTemplate.query("SELECT * FROM " + sender.getChatContainerName() + " WHERE id='" + var1 + "'", new ChatMapper());
        List<Chat> chatsVar2 = jdbcTemplate.query("SELECT * FROM " + sender.getChatContainerName() + " WHERE id='" + var2 + "'", new ChatMapper());
        if (!chatsVar1.isEmpty()) {
            res = var1;
        } else if (!chatsVar2.isEmpty()) {
            res = var2;
        }
        String time = DateConverter.now();
        if (res == null) {
            res = var1;
            jdbcTemplate.execute("USE " + CHATS);
            jdbcTemplate.execute(
                    String.format("CREATE TABLE IF NOT EXISTS %s " +
                                    "(%s INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                                    "%s LONGTEXT NOT NULL, " +
                                    "%s INT NOT NULL, " +
                                    "%s INT(1), " +
                                    "%s INT(1)," +
                                    "%s VARCHAR(255) NOT NULL)",
                            res, ID, TEXT, AUTHOR_ID,
                            IS_WATCHED, ChatContractions.IS_REMOVED, TIME)
            );
            jdbcTemplate.execute("USE " + MESSENGER);
            User receiver = jdbcTemplate.query("SELECT * FROM users where id=" + receiverId,
                    new UserMapper()).get(0);
            jdbcTemplate.execute("USE " + CONTAINERS);
            jdbcTemplate.execute(
                    String.format("INSERT INTO %s(%s, %s, %s, %s) VALUES" +
                                    "('%s', %d, %d, %d)", sender.getChatContainerName(),
                            ChatContainerContractions.ID, CHATTER1, CHATTER2, ChatContainerContractions.IS_REMOVED,
                            res, senderId, receiverId, 0)
            );
            jdbcTemplate.execute(
                    String.format("INSERT INTO %s(%s, %s, %s, %s) VALUES" +
                                    "('%s', %d, %d, %d)", receiver.getChatContainerName(),
                            ChatContainerContractions.ID, CHATTER1, CHATTER2, ChatContainerContractions.IS_REMOVED,
                            res, senderId, receiverId, 0)
            );
        }
        jdbcTemplate.execute("USE " + CHATS);
        jdbcTemplate.execute(
                String.format("INSERT INTO %s(%s, %s, %s, %s, %s) VALUES" +
                                "('%s', %d, %d, %d, '%s')",
                        res, TEXT, AUTHOR_ID,
                        IS_WATCHED, ChatContractions.IS_REMOVED, TIME, text,
                        senderId, 0, 0, time)
        );
    }

    public List<Message> getMessages(String chatId) {
        jdbcTemplate.execute("USE " + CHATS);
        return jdbcTemplate.query("SELECT * FROM " + chatId, new MessageMapper());
    }

    public void watchMessage(String chatId, Integer messageId) {
        jdbcTemplate.execute("USE " + CHATS);
        jdbcTemplate.execute(String.format("UPDATE %s " +
                "SET %s=1 WHERE %s=%d", chatId, IS_WATCHED, ID, messageId));
    }

    public void removeMessage(String chatId, Integer messageId) {
        jdbcTemplate.execute("USE " + CHATS);
        jdbcTemplate.execute(String.format("UPDATE %s " +
                "SET %s=1 WHERE %s=%d", chatId, ChatContractions.IS_REMOVED, ID, messageId));
    }

    public User login(String login, String password) {
        Crypt cryptRest = new Crypt(RestCrypt.KEY);
        Crypt cryptInternal = new Crypt(ServerCrypt.KEY);
        jdbcTemplate.execute("USE " + MESSENGER);
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users where login='" + login + "'",
                new UserMapper()
        );
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        String serverPassword = user.getPassword();
        if (cryptInternal.decode(serverPassword).equals(cryptRest.decode(password))) {
            user.setPassword(cryptRest.encode(cryptInternal.decode(user.getPassword())));
            return user;
        }
        return null;
    }

    public void goOnline(Integer userId) {
        jdbcTemplate.execute("USE " + MESSENGER);
        jdbcTemplate.execute(String.format("UPDATE users " +
                "SET %s=1 WHERE %s=%d", IS_ONLINE, ID, userId));
    }

    public void goOffline(Integer userId) {
        jdbcTemplate.execute("USE " + MESSENGER);
        String time = DateConverter.now();
        jdbcTemplate.execute(String.format("UPDATE users " +
                "SET %s=0, %s='%s' WHERE %s=%d", IS_ONLINE, LAST_ONLINE, time, ID, userId));
    }

    public void changeNickname(Integer userId, String nickname) {
        jdbcTemplate.execute("USE " + MESSENGER);
        jdbcTemplate.execute(String.format("UPDATE users " +
                "SET %s='%s' WHERE %s=%d", NICKNAME, nickname, ID, userId));
    }

    public User getPersonalData(Integer userId) {
        jdbcTemplate.execute("USE " + MESSENGER);
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users where id=" + userId,
                new UserMapper()
        );
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }
}
