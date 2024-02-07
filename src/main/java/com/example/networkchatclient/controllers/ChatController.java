package com.example.networkchatclient.controllers;

import com.example.networkchatclient.StartClient;
import com.example.networkchatclient.models.Network;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

public class ChatController {

    private final String HISTORY_DIR = "src/main/resources/historyLib/";
    private final String POSTFIX_ARGYMENT = "History.txt";
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TextArea chatListHistory;
    @FXML
    private TextField inputMessage;
    @FXML
    private Button sendButton;
    @FXML
    private Label nickNameTitle;
    @FXML
    private ListView<String> userList;
    private Network network;
    private String selectedRecipiend;
    private StartClient startClient;
    private String userFile;
    private File historyFile;
    private PrintWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private final int SIZE_LOAD_HISTORY = 300;;


    @FXML
    public void sendMessage() {
        String message = inputMessage.getText().trim();
        inputMessage.clear();
        if (message.length() != 0) {
            if (selectedRecipiend != null) {
                network.sendPrivateMessage(selectedRecipiend, message);
            } else {
                network.sendMessage(message);
            }
        }
    }

    public void appendMessage(String sender, String message) {
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        chatListHistory.appendText(timeStamp);
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(String.format("%s: %s", sender, message));
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(System.lineSeparator());
        saveUserFileHistory();
    }

    public void appendPrivateMessage(String sender, String recipiend, String message) {
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        chatListHistory.appendText(timeStamp);
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(String.format("[приватное] %s -> %s: %s", sender, recipiend, message));
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(System.lineSeparator());
        saveUserFileHistory();
    }

    public void appendSystemMessage(String systemMessage) {
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        chatListHistory.appendText(timeStamp);
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(systemMessage);
        chatListHistory.appendText(System.lineSeparator());
        chatListHistory.appendText(System.lineSeparator());
        saveUserFileHistory();
    }

    @FXML
    void initialize() {

        sendButton.setOnAction(event -> sendMessage());
        inputMessage.setOnAction(event -> sendMessage());

        userList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = userList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                userList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipiend = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipiend = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }

    public void addUserList(String[] user) {
        Arrays.sort(user);
        userList.setItems(FXCollections.observableArrayList(user));
        userList.refresh();
    }

    public void cheackUserFileHistory() {
        initializeDirHistory();

        if (historyFile.exists()) {
            startClient.getFileLogSystem().warn("Файл истории чата найден");
        } else {
            try {
                historyFile.createNewFile();
                startClient.getFileLogSystem().warn("Файл истории чата не найден");
                startClient.getFileLogSystem().info("Создан новый файл истории чата");
            } catch (IOException e) {
                startClient.getFileLogSystem().warn(e.getMessage());
            }
        }
    }

    private void initializeDirHistory() {
        userFile = network.getUsername() + POSTFIX_ARGYMENT;
        historyFile = new File(HISTORY_DIR + userFile);
    }

    private void saveUserFileHistory() {
        initializeDirHistory();
        try {
            fileWriter = new PrintWriter(new FileWriter(historyFile, false));
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(getText());
            bufferedWriter.close();
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
        }
    }

    public void loadUserFileHistory() {
        initializeDirHistory();

        try {
            List<String> historyList = new ArrayList<>();
            FileInputStream in = new FileInputStream(historyFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                historyList.add(temp);
            }

            if (historyList.size() > SIZE_LOAD_HISTORY) {
                for (int i = historyList.size() - SIZE_LOAD_HISTORY; i <= (historyList.size() - 1); i++) {
                    chatListHistory.appendText(historyList.get(i) + "\n");
                }
            } else {
                for (int i = 0; i < historyList.size(); i++) {
                    chatListHistory.appendText(historyList.get(i) + "\n");
                }
            }
        } catch (IOException e) {
            startClient.getFileLogSystem().error(e.getMessage());
        }
        startClient.getFileLogSystem().info("Файл истории чата загружен");
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public StartClient getStartClient() {
        return startClient;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public Label getNickNameTitle() {
        return nickNameTitle;
    }

    public void setNickNameTitle(String nickNameTitleStr) {
        this.nickNameTitle.setText(nickNameTitleStr);
    }

    public TextArea getChatListHistory() {
        return chatListHistory;
    }

    public void setChatListHistory(TextArea chatListHistory) {
        this.chatListHistory = chatListHistory;
    }

    public String getText() {
        return chatListHistory.getText();
    }
}
