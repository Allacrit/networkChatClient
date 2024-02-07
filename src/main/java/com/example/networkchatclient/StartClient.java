package com.example.networkchatclient;

import com.example.networkchatclient.controllers.ChatController;
import com.example.networkchatclient.controllers.SignController;
import com.example.networkchatclient.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class StartClient extends Application {

    private Logger fileLogSystem;
    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;
    private SignController signController;

    @Override
    public void start(Stage stage) throws IOException {
        PropertyConfigurator.configure("src/main/resources/log/config/log4j.properties");
        fileLogSystem = Logger.getLogger("file");
        primaryStage = stage;
        network = new Network();
        network.setStartClient(this);
        network.connect();
        openAuthDialog();
        createChatDialog();
    }

    private void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader(StartClient.class.getResource("auth-view.fxml"));
        authStage = new Stage();
        Scene scene = new Scene(authLoader.load());
        authStage.setScene(scene);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setResizable(false);
        authStage.setTitle("Аутентификация");
        authStage.show();
        signController = authLoader.getController();
        signController.setNetwork(network);
        signController.setStartClient(this);
    }

    private void createChatDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Супер Чат by Allacrit");
        primaryStage.setScene(scene);
        chatController = fxmlLoader.getController();
        chatController.setNetwork(network);
        chatController.setStartClient(this);
    }

    public static void main(String[] args) {

        launch();
    }

    public void stop() {
         network.sendEndSessionMessage();
    }

    public void openChatDialog() {
        chatController.setNickNameTitle(network.getUsername());
        chatController.cheackUserFileHistory();
        chatController.loadUserFileHistory();
        authStage.close();
        primaryStage.show();
        network.waitMessage(chatController);
    }

    public void showErrorAlert(String title,String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        alert.show();
    }

    public void showInfoAlert(String title,String infoMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(infoMessage);
        alert.show();
    }

    public Logger getFileLogSystem() {
        return fileLogSystem;
    }
}