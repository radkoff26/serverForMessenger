package org.example.services;

import org.example.models.Message;
import org.example.models.MessageSending;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.models.Constants.TOKEN_VALUE;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean createAccount(User user, String token) {
        if (token.equals(TOKEN_VALUE)) {
            return userRepository.createAccount(
                    user.getNickname(),
                    user.getLogin(),
                    user.getPassword()
            );
        }
        return false;
    }

    public void sendMessage(MessageSending message, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.sendMessage(
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getText()
            );
        }
    }

    public List<Message> getMessages(String chatId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            return userRepository.getMessages(chatId);
        }
        return null;
    }

    public void watchMessage(String chatId, Integer messageId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.watchMessage(chatId, messageId);
        }
    }

    public void removeMessage(String chatId, Integer messageId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.removeMessage(chatId, messageId);
        }
    }

    public User login(String login, String password, String token) {
        if (token.equals(TOKEN_VALUE)) {
            return userRepository.login(login, password);
        }
        return null;
    }

    public void goOnline(Integer userId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.goOnline(userId);
        }
    }

    public void changeNickname(Integer userId, String nickname, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.changeNickname(userId, nickname);
        }
    }

    public User getPersonalData(Integer userId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            return userRepository.getPersonalData(userId);
        }
        return null;
    }

    public void goOffline(Integer userId, String token) {
        if (token.equals(TOKEN_VALUE)) {
            userRepository.goOffline(userId);
        }
    }
}
