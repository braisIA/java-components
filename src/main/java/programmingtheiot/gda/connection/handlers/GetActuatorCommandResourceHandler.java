package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

public class GetActuatorCommandResourceHandler extends CoapResource implements IActuatorDataListener
{
	// Logger para registrar eventos y depuración
	private static final Logger _Logger = Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());

	// Variable para almacenar los datos del actuador
	private ActuatorData actuatorData = new ActuatorData();

	// Constructor con el nombre del recurso
	public GetActuatorCommandResourceHandler(String resourceName)
	{
		super(resourceName);

		// Hacer que el recurso sea observable (permite notificaciones automáticas al cliente)
		super.setObservable(true);
	}

	/**
	 * Método que será llamado por el DeviceDataManager cuando haya datos nuevos
	 * del actuador.
	 */
	@Override
	public boolean onActuatorDataUpdate(ActuatorData data)
	{
		if (data != null && this.actuatorData != null) {
			this.actuatorData.updateData(data);

			// Notifica a todos los clientes observadores
			super.changed();

			_Logger.fine("Datos del actuador actualizados para URI: " + super.getURI() +
				": Valor = " + this.actuatorData.getValue());

			return true;
		}

		return false;
	}

	/**
	 * Maneja solicitudes GET del cliente (como el CDA).
	 */
	@Override
	public void handleGET(CoapExchange context)
	{
		if (context != null) {
			_Logger.info("Solicitud GET recibida en " + super.getName());

			// Acepta la solicitud
			context.accept();

			// Convierte los datos actuales del actuador a JSON
			String jsonData = DataUtil.getInstance().actuatorDataToJson(this.actuatorData);

			// Envía la respuesta con los datos actuales
			context.respond(ResponseCode.CONTENT, jsonData);
		} else {
			_Logger.warning("Intercambio CoAP nulo en handleGET(). No se puede procesar.");
		}
	}
}
