/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.config.UdpConfig;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;

public class CoapServerGateway
{
	static {
		CoapConfig.register();
		UdpConfig.register();
	}

	private static final Logger _Logger = Logger.getLogger(CoapServerGateway.class.getName());

	private CoapServer coapServer = null;
	private IDataMessageListener dataMsgListener = null;

	public CoapServerGateway(IDataMessageListener dataMsgListener)
	{
		super();
		this.dataMsgListener = dataMsgListener;

		// Llamar al método initServer con los recursos deseados
		initServer(
			ResourceNameEnum.GDA_ACTUATOR_CMD_RESOURCE,
			ResourceNameEnum.GDA_SENSOR_MSG_RESOURCE,
			ResourceNameEnum.GDA_SYSTEM_PERF_MSG_RESOURCE
		);
	}

	public void addResource(ResourceNameEnum name, String endName, Resource resource)
	{
		if (name != null && resource != null) {
			this.coapServer.add(resource);
			_Logger.info("Recurso agregado al servidor CoAP: " + name.toString());
		}
	}

	public boolean hasResource(String name)
	{
		if (this.coapServer != null && name != null) {
			return this.coapServer.getRoot().getChild(name) != null;
		}
		return false;
	}

	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
		}
	}
	
	public boolean startServer()
	{
		try {
			if (this.coapServer != null) {
				_Logger.info("Iniciando servidor CoAP...");
				this.coapServer.start();

				for (Endpoint ep : this.coapServer.getEndpoints()) {
					_Logger.info("Servidor escuchando en: " + ep.getAddress());
					ep.addInterceptor(new MessageTracer());
				}

				_Logger.info("Servidor CoAP iniciado.");
				return true;
			} else {
				_Logger.warning("Fallo al iniciar el servidor CoAP. No inicializado.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error al iniciar el servidor CoAP.", e);
		}

		return false;
	}


	public boolean stopServer()
	{
		try {
			if (this.coapServer != null) {
				this.coapServer.stop();
				_Logger.info("Servidor CoAP detenido.");
				return true;
			} else {
				_Logger.warning("Fallo al detener el servidor CoAP. No inicializado.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error al detener el servidor CoAP.", e);
		}

		return false;
	}

	private Resource createResourceChain(ResourceNameEnum resource)
	{
		if (resource != null) {
			String[] levels = resource.getResourceName().split("/");
			Resource root = null;
			Resource current = null;

			for (String level : levels) {
				if (level.isEmpty()) continue;
				Resource next = new CoapResource(level);

				if (root == null) {
					root = next;
					current = root;
				} else {
					current.add(next);
					current = next;
				}
			}

			if (current != null) {
				GenericCoapResourceHandler handler = new GenericCoapResourceHandler(resource);
				handler.setDataMessageListener(this.dataMsgListener);
				current.add(handler);
			}

			return root;
		}

		return null;
	}

	private void initServer(ResourceNameEnum... resources)
	{
		this.coapServer = new CoapServer();

		if (resources != null && resources.length > 0) {
			for (ResourceNameEnum res : resources) {
				Resource resource = createResourceChain(res);
				if (resource != null) {
					this.coapServer.add(resource);
					_Logger.info("Recurso CoAP registrado: " + res.getResourceName());
				}
			}
		}
	}
}
