package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;

public class UpdateTelemetryResourceHandler extends CoapResource
{
    private static final Logger _Logger = Logger.getLogger(UpdateTelemetryResourceHandler.class.getName());
    private IDataMessageListener dataMsgListener = null;

    public UpdateTelemetryResourceHandler(String resourceName)
    {
        super(resourceName);
    }

    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }

    @Override
    public void handleGET(CoapExchange exchange)
    {
        _Logger.info("GET request received on " + getName());
        exchange.respond(ResponseCode.METHOD_NOT_ALLOWED, "GET not supported.");
    }

    @Override
    public void handlePOST(CoapExchange exchange)
    {
        _Logger.info("POST request received on " + getName());
        exchange.respond(ResponseCode.METHOD_NOT_ALLOWED, "POST not supported.");
    }

    @Override
    public void handleDELETE(CoapExchange exchange)
    {
        _Logger.info("DELETE request received on " + getName());
        exchange.respond(ResponseCode.METHOD_NOT_ALLOWED, "DELETE not supported.");
    }

    @Override
    public void handlePUT(CoapExchange exchange)
    {
        ResponseCode code = ResponseCode.NOT_ACCEPTABLE;
        exchange.accept();

        if (this.dataMsgListener != null) {
            try {
                String jsonData = new String(exchange.getRequestPayload());
                SensorData sensorData = DataUtil.getInstance().jsonToSensorData(jsonData);

                this.dataMsgListener.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
                code = ResponseCode.CHANGED;
            } catch (Exception e) {
                _Logger.warning("Failed to handle PUT request. Message: " + e.getMessage());
                code = ResponseCode.BAD_REQUEST;
            }
        } else {
            _Logger.info("No callback listener for request. Ignoring PUT.");
            code = ResponseCode.CONTINUE;
        }

        String msg = "Update sensor data request handled: " + super.getName();
        exchange.respond(code, msg);
    }
}
