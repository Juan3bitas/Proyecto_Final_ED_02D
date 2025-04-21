module proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.logging;


    opens main.java.proyectofinal to javafx.fxml;
}
