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
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.*;

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
                login, finalPassword, chat_container, nickname, time, null, 0
                )
        );
        jdbcTemplate.execute("USE " + CONTAINERS);
        jdbcTemplate.execute(
                String.format("CREATE TABLE %s " +
                                "(%s VARCHAR(255) NOT NULL PRIMARY KEY, " +
                                "%s INT NOT NULL, " +
                                "%s INT NOT NULL, " +
                                "%s INT NOT NULL, " +
                                "%s INT)",
                        chat_container,
                        ChatContainerContractions.ID,
                        CHATTER1,
                        CHATTER2,
                        NOT_CHECKED,
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
                    String.format("INSERT INTO %s(%s, %s, %s, %s, %s) VALUES" +
                                    "('%s', %d, %d, %d, %d)", sender.getChatContainerName(),
                            ChatContainerContractions.ID, CHATTER1, CHATTER2, NOT_CHECKED, ChatContainerContractions.IS_REMOVED,
                            res, senderId, receiverId, 0, 0)
            );
            jdbcTemplate.execute(
                    String.format("INSERT INTO %s(%s, %s, %s, %s, %s) VALUES" +
                                    "('%s', %d, %d, %d, %d)", receiver.getChatContainerName(),
                            ChatContainerContractions.ID, CHATTER1, CHATTER2, NOT_CHECKED, ChatContainerContractions.IS_REMOVED,
                            res, senderId, receiverId, 1, 0)
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
        jdbcTemplate.execute("USE " + CONTAINERS);
        chatId = chatId.replace("\"", "");
        int id = Integer.parseInt(chatId.split("_")[0]);
        String chat_id_permuted = chatId.split("_")[1] + "_" + chatId.split("_")[0];
        List<Chat> chats1 = jdbcTemplate.query("SELECT * FROM " + id + "_container WHERE id='" + chatId + "'", new ChatMapper());
        List<Chat> chats2 = jdbcTemplate.query("SELECT * FROM " + id + "_container WHERE id='" + chat_id_permuted + "'", new ChatMapper());
        if (chats1.isEmpty() && chats2.isEmpty()) {
            return new ArrayList<>();
        }
        List<Chat> chats = chats1.isEmpty() ? chats2 : chats1;
        jdbcTemplate.execute("USE " + CHATS);
        List<Message> messages = jdbcTemplate.query("SELECT * FROM " + chats.get(0).getId() + " WHERE " + ChatContractions.IS_REMOVED + "=0", new MessageMapper());
        messages.sort((o1, o2) -> {
            try {
                return DateConverter.toDate(o1.getTime()).before(
                        DateConverter.toDate(o2.getTime())
                ) ? 0 : -1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        return messages;
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

    public Message getLastVisibleSentMessage(String chatId) {
        jdbcTemplate.execute("USE " + CHATS);
        List<Message> messages = jdbcTemplate.query(
                String.format("SELECT * FROM %s WHERE %s=0", chatId, ChatContractions.IS_REMOVED),
                new MessageMapper()
        );
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    public List<Chat> getChats(Integer userId) {
        jdbcTemplate.execute("USE " + CONTAINERS);
        List<Chat> chats = jdbcTemplate.query(
                String.format("SELECT * FROM %d_container WHERE %s=0", userId, ChatContainerContractions.IS_REMOVED),
                new ChatMapper()
        );

        if (chats.isEmpty()) {
            return null;
        }

        int n = chats.size();

        for (int i = 0; i < n; ) {
            Chat chat = chats.get(i);
            if (getLastVisibleSentMessage(chat.getId()) == null) {
                chats.remove(chat);
                n--;
            } else {
                chat.setLastMessage(getLastVisibleSentMessage(chat.getId()));
                chat.setNotChecked(getNumberOfNotChecked(chat.getId(), userId));
                if (chat.getChatter1().equals(userId)) {
                    chat.setChatterLocalNickname(getPersonalData(chat.getChatter2()).getNickname());
                } else {
                    chat.setChatterLocalNickname(getPersonalData(chat.getChatter1()).getNickname());
                }
                i++;
            }
        }
        chats.sort((o1, o2) -> {
            try {
                return DateConverter.toDate(o1.getLastMessage().getTime()).before(
                        DateConverter.toDate(o2.getLastMessage().getTime())
                ) ? 0 : -1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        return chats;
    }

    public Integer getNumberOfNotChecked(String chatId, Integer userId) {
        jdbcTemplate.execute("USE " + CHATS);
        List<Message> messages;
        int sum = 0;
        messages = getMessages(chatId);
        for (Message message : messages) {
            if (!message.getIsWatched() && !message.getAuthorId().equals(userId) && !message.getIsRemoved()) {
                sum += 1;
            }
        }
        return sum;
    }

    public Integer getNotCheckedNumber(Integer userId) {
        jdbcTemplate.execute("USE " + CONTAINERS);
        List<Chat> chats = getChats(userId);
        int sum = 0;
        if (chats != null) {
            for (Chat chat : chats) {
                sum += getNumberOfNotChecked(chat.getId(), userId);
            }
        }
        return sum;
    }

    public Boolean isUserOnline(Integer userId) {
        jdbcTemplate.execute("USE " + MESSENGER);
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE " + ID + "=" + userId, new UserMapper());
        if (!users.isEmpty()) {
            return users.get(0).getIsOnline();
        }
        return false;
    }

    public List<User> getUsers(String login, String userLogin) {
        jdbcTemplate.execute("USE " + MESSENGER);
        List<User> users = jdbcTemplate.query(
                String.format("SELECT * FROM users WHERE %s='%s'", LOGIN, login),
                new UserMapper()
        );
        List<User> alikeUsers = jdbcTemplate.query(
                "SELECT * FROM users WHERE " + LOGIN + " LIKE '" + login + "%'",
                new UserMapper()
        );
        alikeUsers.sort(Comparator.comparing(User::getLogin));
        alikeUsers.removeIf(user -> user.getLogin().equals(login));
        users.addAll(alikeUsers);
        users.removeIf(user -> user.getLogin().equals(userLogin));
        return users;
    }

    public void setAvatar(String filename, Integer userId) {
        jdbcTemplate.execute("USE " + MESSENGER);
        jdbcTemplate.execute(
                String.format(
                        "UPDATE users SET %s='%s' WHERE %s=%d",
                        AVATAR_URL, filename, ID, userId
                )
        );
    }
}
