module proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    //requires javafx.base;
    requires transitive java.logging;
    requires com.google.gson;  // Necesario para JSON

    opens proyectofinal to javafx.fxml;
    opens proyectofinal.modelo to com.google.gson;
    opens proyectofinal.servidor to com.google.gson;
    opens proyectofinal.controladores to javafx.fxml;

    exports proyectofinal;
    exports proyectofinal.modelo;
    exports proyectofinal.servidor;
    exports proyectofinal.controladores;
}