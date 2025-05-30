package programmingtheiot.common;

import java.net.URI;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class MyCoapClient {

    private static final Logger _Logger = Logger.getLogger(MyCoapClient.class.getName());

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Uso: java -jar my-coap-client.jar --method=GET coap://localhost:5683/PIOT/ConstrainedDevice/SystemPerfMsg");
            System.exit(1);
        }

        String methodArg = args[0];
        String uriArg = args[1];

        if (!methodArg.startsWith("--method=")) {
            System.out.println("El primer argumento debe indicar el método HTTP, ejemplo: --method=GET");
            System.exit(1);
        }

        String method = methodArg.substring("--method=".length()).toUpperCase();

        try {
            URI uri = new URI(uriArg);
            CoapClient client = new CoapClient(uri);

            CoapResponse response = null;

            switch (method) {
                case "GET":
                    response = client.get();
                    break;
                case "POST":
                    // Aquí podrías añadir payload opcional si quieres
                    response = client.post("", 0);
                    break;
                case "PUT":
                    response = client.put("", 0);
                    break;
                case "DELETE":
                    response = client.delete();
                    break;
                default:
                    System.out.println("Método no soportado: " + method);
                    System.exit(1);
            }

            if (response != null) {
                System.out.println("==[ CoAP Response ]============================================");
                System.out.println("Status : " + response.getCode());
                System.out.println("Payload: " + (response.getPayload() != null ? response.getPayload().length + " Bytes" : "0 Bytes"));
                System.out.println("---------------------------------------------------------------");
                System.out.println(response.getResponseText());
                System.out.println("===============================================================");
            } else {
                System.out.println("No se recibió respuesta.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
