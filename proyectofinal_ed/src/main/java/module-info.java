module proyectofinal {
    requires transitive java.logging;
    requires com.google.gson;  // Necesario para JSON


    opens main.java.proyectofinal.modelo to com.google.gson;  // Para serialización
    opens main.java.proyectofinal.servidor to com.google.gson;

    exports main.java.proyectofinal.modelo;
    exports main.java.proyectofinal.servidor;
}