/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

// ... cabecera y package sin cambios ...

public class CoapClientConnector implements IRequestResponseClient
{
	private static final Logger _Logger = Logger.getLogger(CoapClientConnector.class.getName());

	private String protocol;
	private String host;
	private int port;
	private String serverAddr;
	private CoapClient clientConn;
	private IDataMessageListener dataMsgListener;

	public CoapClientConnector()
	{
		ConfigUtil config = ConfigUtil.getInstance();
		this.host = config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

		if (config.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}

		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
		initClient();

		_Logger.info("Usando URL para conexión CoAP: " + this.serverAddr);
	}

	private void initClient()
	{
		try {
			this.clientConn = new CoapClient(this.serverAddr);
			_Logger.info("Conexión del cliente CoAP creada: " + this.serverAddr);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error al crear conexión del cliente CoAP: " +
				(this.clientConn != null ? this.clientConn.getURI() : this.serverAddr), e);
		}
	}

	public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs)
	{
		// Not implemented for this exercise
	}

	@Override
	public boolean sendDiscoveryRequest(int timeout) {
		_Logger.info("sendDiscoveryRequest() invocado.");

		try {
			this.clientConn.setURI(this.serverAddr + "/.well-known/core");
			Set<WebLink> wlSet = this.clientConn.discover();

			if (wlSet != null && !wlSet.isEmpty()) {
				for (WebLink wl : wlSet) {
					_Logger.info(" --> URI: " + wl.getURI() + ". Atributos: " + wl.getAttributes());
				}
				return true;
			} else {
				_Logger.warning("No se encontraron recursos en el servidor CoAP.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error durante la solicitud de descubrimiento CoAP.", e);
		}

		return false;
	}

	@Override
	public boolean sendDeleteRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
		_Logger.info("sendDeleteRequest() invocado.");
		return false;
	}

	@Override
	public boolean sendGetRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
		_Logger.info("sendGetRequest() invocado.");

		if (resource == null) {
			_Logger.warning("El recurso es null. No se puede enviar la solicitud GET.");
			return false;
		}

		String resourcePath = resource.getResourceName();
		if (name != null && !name.isEmpty()) {
			resourcePath += "/" + name;
		}

		try {
			if (enableCON) {
				this.clientConn.useCONs();
			} else {
				this.clientConn.useNONs();
			}

			this.clientConn.setURI(this.serverAddr + "/" + resourcePath);
			_Logger.info("Enviando solicitud GET a: " + this.clientConn.getURI());

			CoapResponse response = this.clientConn.get();

			if (response != null) {
				_Logger.info("Manejando GET. Respuesta: " + response.isSuccess() + " - " + response.getOptions() +
					" - " + response.getCode() + " - " + response.getResponseText());

				if (this.dataMsgListener != null) {
					// ✅ CAMBIO AQUÍ: Uso correcto del método handleIncomingMessage
					this.dataMsgListener.handleIncomingMessage(response.getResponseText());
				}

				return true;
			} else {
				_Logger.warning("Manejando GET. No se recibió respuesta.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Excepción al enviar solicitud GET", e);
		}

		return false;
	}

	@Override
	public boolean sendPostRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
		_Logger.info("sendPostRequest() invocado.");
		return false;
	}

	@Override
	public boolean sendPutRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
		_Logger.info("sendPutRequest() invocado.");

		if (resource == null) {
			_Logger.warning("El recurso es null. No se puede enviar la solicitud PUT.");
			return false;
		}

		String resourcePath = resource.getResourceName();
		if (name != null && !name.isEmpty()) {
			resourcePath += "/" + name;
		}

		try {
			this.clientConn.setURI(this.serverAddr + "/" + resourcePath);
			_Logger.info("Enviando solicitud PUT a: " + this.clientConn.getURI());
			_Logger.info("Payload: " + payload);

			CoapResponse response = this.clientConn.put(payload, MediaTypeRegistry.APPLICATION_JSON);

			if (response != null && response.getCode() == ResponseCode.CHANGED) {
				_Logger.info("Respuesta exitosa de PUT: " + response.getResponseText());
				return true;
			} else {
				_Logger.warning("Solicitud PUT fallida. Código de respuesta: " + (response != null ? response.getCode() : "null"));
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Excepción al enviar solicitud PUT", e);
		}

		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return true; // ← Podrías devolver true aquí para indicar éxito
	}

	public void clearEndpointPath() {}

	public void setEndpointPath(ResourceNameEnum resource) {}

	@Override
	public boolean startObserver(ResourceNameEnum resource, String name, int ttl) {
		_Logger.info("startObserver() invocado.");
		return false;
	}

	@Override
	public boolean stopObserver(ResourceNameEnum resourceType, String name, int timeout) {
		_Logger.info("stopObserver() invocado.");
		return false;
	}
}
