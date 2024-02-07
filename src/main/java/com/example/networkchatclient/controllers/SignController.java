package com.example.networkchatclient.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import com.example.networkchatclient.StartClient;
import com.example.networkchatclient.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SignController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField loginField;

    @FXML
    private TextField loginReg;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField passwordReg;

    @FXML
    private TextField usernameReg;
    private Network network;
    private StartClient startClient;

    @FXML
    void checkAuth(ActionEvent event) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            startClient.showErrorAlert("Ошибка ввода при аутентификации","Поля не должны быть пустыми");
            return;
        }

        if (login.length() > 32 || password.length() > 32) {
            startClient.showErrorAlert("Ошибка ввода при аутентификации","Длина логина и пароля превышает 32 знака");
            return;
        }
        String authErrorMessage = network.sendAuthMessage(login, password);

        if (authErrorMessage == null) {
            startClient.openChatDialog();
        } else {
            startClient.showErrorAlert("Ошибка аутентификации",authErrorMessage);
        }
    }

    @FXML
    void signUp(ActionEvent event) {
        String login = loginReg.getText().trim();
        String password = passwordReg.getText().trim();
        String username = usernameReg.getText().trim();

        if (login.length() == 0 || password.length() == 0 || username.length() == 0) {
            startClient.showErrorAlert("Ошибка при регистации","Поля не должны быть пустыми");
            return;
        }
        if (login.length() == 32 || password.length() == 32 || username.length() == 32) {
            startClient.showErrorAlert("Ошибка при регистрации","Длина логина, пароля, имя превышает 32 знака");
            return;
        }
        String regErrorMessage = network.sendRegistrationMessage(login,password,username);

        if (regErrorMessage == null) {
            String authErrorMessage = network.sendAuthMessage(login, password);

            if (authErrorMessage == null) {
                startClient.openChatDialog();
            } else {
                startClient.showErrorAlert("Ошибка аутентификации",authErrorMessage);
            }
        } else {
            startClient.showErrorAlert("Ошибка при регистрации",regErrorMessage);
        }
    }

    @FXML
    void initialize() {
    }


    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }
}

