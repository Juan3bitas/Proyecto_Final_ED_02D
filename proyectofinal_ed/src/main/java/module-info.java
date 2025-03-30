module proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens proyectofinal to javafx.fxml;
    exports proyectofinal;
}
