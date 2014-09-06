package net.drailing.kunai.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.sun.org.apache.bcel.internal.util.ClassLoader;

public class KunaiWebServer extends AbstractHandler {

	private Server server;
	private String ipAddress;
	private int port;
	private int socketPort;

	public KunaiWebServer(int _port, int _socketPort) throws Exception {
		port = _port;
		socketPort = _socketPort;

		server = new Server(port);
		server.setHandler(this);

		ipAddress = InetAddress.getLocalHost().getHostAddress();
	}

	public void startServer() throws Exception {
		server.start();
		// server.join();
	}

	public void stopServer() throws Exception {
		server.stop();
	}

	private String getResource(String path) {
		File f = new File(path);
		String result = "error";
		try {
			if (!f.exists()) {
				result = IOUtils.toString(
						ClassLoader.getSystemResourceAsStream(path), "UTF-8");
			} else {
				result = IOUtils.toString(new FileInputStream(f), "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if ("/".equals(target)) {
			String index = getResource("htdocs/index.html");

			index = String.format(index,
					String.format("http://%s:%d", ipAddress, socketPort));

			response.getWriter().print(index);
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

		} else if (target.endsWith(".css") || target.endsWith(".js")) {
			response.getWriter().print(getResource("htdocs" + target));

			response.setContentType("text/css;charset=utf-8");
			if (target.endsWith(".js")) {
				response.setContentType("text/javascript;charset=utf-8");
			}
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

		} else {
			// binary
			File f = new File("htdocs" + target);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			ServletOutputStream out = response.getOutputStream();

			if (f.exists()) {
				IOUtils.copy(new FileInputStream(f), out);
			} else {
				IOUtils.copy(ClassLoader.getSystemResourceAsStream("htdocs"
						+ target), out);
			}

			out.flush();
			out.close();
		}
	}

}
