package org.example.controllers;

import com.sun.xml.internal.ws.util.ASCIIUtility;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.buf.Ascii;
import org.example.crypt.Utility;
import org.example.models.*;
import org.example.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.example.models.Constants.FILENAME;
import static org.example.models.Constants.TOKEN_VALUE;

@RestController
public class InteractionController {

    private static final String TOKEN = "TOKEN";

    @Autowired
    private UserService userService;

    @PostMapping(value = "/createAccount", consumes = "application/json", produces = "text/plain")
    public String createAccount(
            @RequestBody User user,
            @RequestHeader(value = TOKEN) String token
    ) {
        return String.valueOf(userService.createAccount(user, token));
    }

    @PostMapping(value = "/sendMessage", consumes = "application/json")
    public void sendMessage(
            @RequestHeader(value = TOKEN) String token,
            @RequestBody MessageSending message
    ) {
        userService.sendMessage(message, token);
    }

    @PostMapping(value = "/getMessages", consumes = "application/json")
    public List<Message> getMessages(
            @RequestHeader("token") String token,
            @RequestBody String chatId
    ) {
        return userService.getMessages(chatId, token);
    }

    @PostMapping(value = "/watchMessage", consumes = "application/json")
    public void watchMessage(
            @RequestHeader("token") String token,
            @RequestBody String body
    ) {
        body = body.replace("\"", "");
        String chatId = body.split(" ")[0];
        Integer messageId = Integer.parseInt(body.split(" ")[1]);
        userService.watchMessage(chatId, messageId, token);
    }

    @PostMapping(value = "/removeMessage", consumes = "application/json")
    public void removeMessage(
            @RequestHeader("token") String token,
            @RequestBody String body
    ) {
        body = body.replace("\"", "");
        String chatId = body.split(" ")[0];
        Integer messageId = Integer.parseInt(body.split(" ")[1]);
        userService.removeMessage(chatId, messageId, token);
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public User login(
            @RequestHeader("token") String token,
            @RequestBody User user
    ) {
        return userService.login(user.getLogin(), user.getPassword(), token);
    }

    @PostMapping(value = "/goOnline", consumes = "application/json")
    public void goOnline(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        userService.goOnline(userId, token);
    }

    @PostMapping(value = "/goOffline", consumes = "application/json")
    public void goOffline(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        userService.goOffline(userId, token);
    }

    @PostMapping(value = "/changeNickname", consumes = "application/json")
    public void goOffline(
            @RequestHeader("token") String token,
            @RequestBody String body
    ) {
        body = body.replace("\"", "");
        Integer userId = Integer.parseInt(body.split(" ")[0]);
        String nickname = body.split(" ")[1];
        userService.changeNickname(userId, nickname, token);
    }

    @PostMapping(value = "/getPersonalData", consumes = "application/json")
    public User getPersonalData(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        return userService.getPersonalData(userId, token);
    }

    @PostMapping(value = "/getLastVisibleSentMessage", consumes = "application/json")
    public Message getLastVisibleSentMessage(
            @RequestHeader("token") String token,
            @RequestBody String chatId
    ) {
        return userService.getLastVisibleSentMessage(chatId, token);
    }

    @PostMapping(value = "/getChats", consumes = "application/json")
    public List<Chat> getChats(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        return userService.getChats(userId, token);
    }

    @PostMapping(value = "/getNotCheckedNumber", consumes = "application/json")
    public Integer getNotCheckedNumber(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        return userService.getNotCheckedNumber(userId, token);
    }

    @PostMapping(value = "/isUserOnline", consumes = "application/json")
    public Boolean isUserOnline(
            @RequestHeader("token") String token,
            @RequestBody Integer userId
    ) {
        return userService.isUserOnline(userId, token);
    }

    @PostMapping(value = "/getUsers", consumes = "application/json")
    public List<User> getUsers(
            @RequestHeader("token") String token,
            @RequestBody String body
    ) {
        body = body.replace("\"", "");
        String login = body.split(" ")[0];
        String userLogin = body.split(" ")[1];
        return userService.getUsers(login, userLogin, token);
    }


    @PostMapping(path = "/setAvatar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String setAvatar(
            @RequestHeader("token") String token,
            @RequestHeader("id") Integer userId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return userService.setAvatar(file, userId, token);
    }

    @GetMapping(path = "/getAvatar", produces = {MediaType.IMAGE_PNG_VALUE})
    public byte[] getAvatar(
            @RequestParam("url") String url
    ) throws IOException {
        Path path = Paths.get(FILENAME, url + ".png");
        return IOUtils.toByteArray(path.toUri());
    }
}
