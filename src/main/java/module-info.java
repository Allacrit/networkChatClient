module com.example.networkchatclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires log4j;


    opens com.example.networkchatclient to javafx.fxml;
    exports com.example.networkchatclient;
    exports com.example.networkchatclient.controllers;
    opens com.example.networkchatclient.controllers to javafx.fxml;
}