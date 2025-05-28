package programmingtheiot.gda.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class MyCoapClient {

	public static void main(String[] args) {
		Options options = new Options();

		Option methodOption = new Option("m", "method", true, "HTTP method (GET or POST)");
		methodOption.setRequired(true);
		options.addOption(methodOption);

		Option urlOption = new Option("u", "url", true, "CoAP URL (e.g., coap://localhost:5683/PIOT/ConstrainedDevice/SystemPerfMsg)");
		urlOption.setRequired(true);
		options.addOption(urlOption);

		Option payloadOption = new Option("p", "payload", true, "Payload for POST (optional)");
		payloadOption.setRequired(false);
		options.addOption(payloadOption);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("MyCoapClient", options);
			System.exit(1);
			return;
		}

		String method = cmd.getOptionValue("method").toUpperCase();
		String url = cmd.getOptionValue("url");
		String payload = cmd.getOptionValue("payload", "");

		CoapClient client = new CoapClient(url);

		try {
			CoapResponse response = null;

			switch (method) {
				case "GET":
					response = client.get();
					break;
				case "POST":
					response = client.post(payload, 0); // 0 = text/plain
					break;
				default:
					System.out.println("Método no soportado: " + method);
					System.exit(1);
			}

			if (response != null) {
				System.out.println("==[ CoAP Response ]==");
				System.out.println("Status : " + response.getCode());
				System.out.println("Payload: " + response.getResponseText());
			} else {
				System.out.println("No se recibió respuesta del servidor.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.shutdown();
		}
	}
}
