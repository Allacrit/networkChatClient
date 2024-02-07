package com.example.networkchatclient.models;

import com.example.networkchatclient.StartClient;
import com.example.networkchatclient.controllers.ChatController;
import javafx.application.Platform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8186;
    private final String host;
    private final int port;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatController chatController;
    private String username;
    private StartClient startClient;

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autheer"; // +error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String REFRESH_USER_LIST_CMD_PREFIX = "/refresh";
    private static final String REG_USER_CMD_PREFIX = "/reg"; // + login + password + username
    private static final String REGOK_USER_CMD_PREFIX = "/regok"; // + ок
    private static final String REGERR_USER_CMD_PREFIX = "/regerr"; // + error registration

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void connect() {
        try {
            Socket socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
            startClient.showErrorAlert("Ошибка подключения", "Соединение не установлено");
        }
    }

    public synchronized void sendMessage(String message) {
        try {
            out.writeUTF(String.format("%s %s", CLIENT_MSG_CMD_PREFIX, message));
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
            startClient.showErrorAlert("Ошибка подключения", "Соединение не установлено");
        }
    }

    public synchronized void sendPrivateMessage(String recipient, String message) {
        try {
            out.writeUTF(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, recipient, message));
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
            startClient.showErrorAlert("Ошибка отправки", "Не возможно отправить сообщение");
        }
    }

    public synchronized String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String responce = in.readUTF();
            if (responce.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = responce.split("\\s+", 2)[1];
                return null;
            } else {
                return responce.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
            startClient.showErrorAlert("Ошибка аутентификации", "Неизвестная ошибка");
            return e.getMessage();
        }
    }

    public synchronized void sendEndSessionMessage() {
        try {
            out.writeUTF(END_CLIENT_CMD_PREFIX);
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
        }
    }

    public synchronized String sendRegistrationMessage (String login, String password, String user) {
        try {
            out.writeUTF(String.format("%s %s %s %s", REG_USER_CMD_PREFIX, login, password,user));
            String responce = in.readUTF();
            if (responce.startsWith(REGOK_USER_CMD_PREFIX)) {
                return null;
            } else {
                return responce.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
            startClient.showErrorAlert("Ошибка регистрации", "Неизвестная ошибка");
            return e.getMessage();
        }

    }

    public void waitMessage(ChatController chatController) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    String typeMessage = message.split("\\s+")[0];

                    switch (typeMessage) {
                        case CLIENT_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 3);
                            String sender = parts[1];
                            String messageFromSender = parts[2];

                            if (sender.equals(username)) {
                                sender = "Я";
                            }
                            String finalSender = sender;
                            Platform.runLater(() -> chatController.appendMessage(finalSender, messageFromSender));
                        }

                        case PRIVATE_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 4);
                            String sender = parts[1];
                            String recipiend = parts[2];
                            String messageFromSender = parts[3];

                            if (sender.equals(username)) {
                                sender = "Я";
                            }
                            if (recipiend.equals(username)) {
                                recipiend = "Я";
                            }
                            String finalSender = sender;
                            String finalRecipiend = recipiend;
                            Platform.runLater(() -> chatController.appendPrivateMessage(finalSender, finalRecipiend, messageFromSender));
                        }

                        case SERVER_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            String systemMessage = parts[1];
                            Platform.runLater(() -> chatController.appendSystemMessage(systemMessage));
                        }

                        case AUTHERR_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            String errorMessage = parts[1];
                            Platform.runLater(() -> startClient.showErrorAlert("Внимание", errorMessage));
                        }

                        case REFRESH_USER_LIST_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            String[] userArrayList = parts[1].split("\\s+");
                            Platform.runLater(() -> chatController.addUserList(userArrayList));
                        }
                        case REGERR_USER_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            Platform.runLater(() -> startClient.showErrorAlert("Внимание", parts[1]));
                        }
                    }
                }
            } catch (IOException e) {
                startClient.getFileLogSystem().error(e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public String getUsername() {
        return username;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }
}
