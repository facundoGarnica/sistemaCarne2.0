package Util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MercadoPagoApi {

    private static final String ACCESS_TOKEN = "APP_USR-1366200440655176-031021-c8c8947c4f31b42fa5609a5877dd3fdd-112339395";
    private static final String URL = "https://api.mercadopago.com/v1/payments/search?"
            + "sort=date_created&criteria=desc&limit=10";
    private List<DatosPagos> listaDatos;

    public MercadoPagoApi() {
        listaDatos = new ArrayList<>();
    }

    public void obtenerUltimosPagos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Convertir respuesta JSON en un objeto
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());

            // Extraer los pagos
            JsonNode results = rootNode.path("results");
            if (results.isArray() && results.size() > 0) {
                for (JsonNode payment : results) {
                    String cliente = payment.path("payer").path("email").asText();
                    String fechaCompleta = payment.path("date_created").asText();
                    double monto = payment.path("transaction_amount").asDouble();
                    String estado = payment.path("status").asText();

                    // Convertir fecha completa a LocalDateTime
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(fechaCompleta);
                    ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("America/Argentina/Buenos_Aires"));
                    LocalTime hora = zonedDateTime.toLocalTime();
                    String horaMinuto = hora.format(DateTimeFormatter.ofPattern("HH:mm"));

                    // Agregar a la lista
                    listaDatos.add(new DatosPagos(cliente, horaMinuto, monto, estado));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<DatosPagos> getListaDatos() {
        return listaDatos;
    }
}
