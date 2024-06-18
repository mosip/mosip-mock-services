package org.biometric.provider;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.mdm.dto.StreamingRequestDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code StreamRequest} is a servlet responsible for handling streaming
 * requests. It processes incoming HTTP requests to stream images continuously
 * to the client. The servlet supports STREAM, POST, GET, and OPTIONS HTTP
 * methods.
 * 
 * <p>
 * The response content type is set to {@code multipart/x-mixed-replace} with a
 * boundary string to indicate the separation between consecutive images in the
 * stream.
 * 
 * <p>
 * This servlet reads the request details, retrieves images based on device
 * information, and sends these images as a continuous stream to the client.
 * </p>
 * 
 * <p>
 * Note: This class requires additional classes like {@link CORSManager} and
 * {@link StreamingRequestDetail} to function correctly.
 * </p>
 * 
 * @see HttpServlet
 * @see HttpServletRequest
 * @see HttpServletResponse
 */
public class StreamRequest extends HttpServlet {
	private static final long serialVersionUID = -1914160572219233317L;

	/** The user directory system property key. */
	public static final String USER_DIR = "user.dir";

	private static ObjectMapper objMapper = null;
	static {
		objMapper = new ObjectMapper();
	}

	/**
	 * Default constructor.
	 */
	public StreamRequest() {
		super();
	}

	/**
	 * Handles the HTTP request by delegating to the
	 * {@link #doPost(HttpServletRequest, HttpServletResponse)} method for STREAM,
	 * POST, and GET methods. Handles OPTIONS method separately for CORS support.
	 * 
	 * @param req the {@link HttpServletRequest} object that contains the request
	 *            the client made to the servlet
	 * @param res the {@link HttpServletResponse} object that contains the response
	 *            the servlet returns to the client
	 * @throws ServletException if the request could not be handled
	 * @throws IOException      if an input or output error is detected when the
	 *                          servlet handles the request
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("STREAM") || req.getMethod().contentEquals("POST")
				|| req.getMethod().contentEquals("GET"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
	}

	/**
	 * Processes the POST request to stream images continuously. Reads the request
	 * details, retrieves the appropriate images, and streams them to the client.
	 * 
	 * @param request  the {@link HttpServletRequest} object that contains the
	 *                 request the client made to the servlet
	 * @param response the {@link HttpServletResponse} object that contains the
	 *                 response the servlet returns to the client
	 * @throws ServletException if the request could not be handled
	 * @throws IOException      if an input or output error is detected when the
	 *                          servlet handles the request
	 */
	@Override
	@SuppressWarnings({ "java:S1989", "java:S2142", "java:S2189" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<byte[]> imageByteList = null;
		response.setContentType("multipart/x-mixed-replace; boundary=--BoundaryString");
		response = CORSManager.setCors(response);
		BufferedReader bR = request.getReader();
		String s = "";
		StringBuilder sT = new StringBuilder("");
		while ((s = bR.readLine()) != null) {
			sT.append(s);
		}

		if (sT.isEmpty()) {
			String devId = null;
			String devSubId = null;
			if (request.getMethod().equals("GET")) {
				devId = request.getParameter("deviceId");
				devSubId = request.getParameter("deviceSubId");
			}
			sT.append("{\"deviceId\": \"" + (devId != null ? devId : "") + "\", \"deviceSubId\": \""
					+ (devSubId != null ? devSubId : "") + "\"}");
		}
		StreamingRequestDetail streamRequest = objMapper.readValue(sT.toString().getBytes(StandardCharsets.UTF_8),
				StreamingRequestDetail.class);
		imageByteList = new ArrayList<>();
		OutputStream outputStream = response.getOutputStream();

		getImage(streamRequest, imageByteList);
		getImage(imageByteList);

		int i = 0;
		while (true) {
			try {
				outputStream.write(("--BoundaryString\r\n" + "Content-type: image/jpeg\r\n" + "Content-Length: "
						+ imageByteList.get(i).length + "\r\n\r\n").getBytes());
				outputStream.write(imageByteList.get(i));
				outputStream.write("\r\n\r\n".getBytes());
				outputStream.flush();
				if (i == 0)
					i = 1;
				else if (i == 1)
					i = 0;
				TimeUnit.MILLISECONDS.sleep(3000);
			} catch (Exception e) {
				return;
			}
		}
	}

	/**
	 * Retrieves the image based on the streaming request details and adds it to the
	 * list.
	 * 
	 * @param requestBody   the streaming request details containing device
	 *                      information
	 * @param imageByteList the list to store the image bytes
	 * @throws IOException if an error occurs while reading the image file
	 */
	private void getImage(StreamingRequestDetail requestBody, List<byte[]> imageByteList) throws IOException {
		File image = new File(System.getProperty(USER_DIR) + "/files/images/stream" + requestBody.getDeviceId()
				+ requestBody.getDeviceSubId() + ".jpg");
		BufferedImage originalImage = ImageIO.read(image);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "jpg", baos);
		baos.flush();
		imageByteList.add(baos.toByteArray());
	}

	/**
	 * Retrieves a default image and adds it to the list.
	 * 
	 * @param imageByteList the list to store the image bytes
	 * @throws IOException if an error occurs while reading the image file
	 */
	private void getImage(List<byte[]> imageByteList) throws IOException {
		File image = new File(System.getProperty(USER_DIR) + "/files/images/" + "empty" + ".jpg");
		BufferedImage originalImage = ImageIO.read(image);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "jpg", baos);
		baos.flush();
		imageByteList.add(baos.toByteArray());
	}
}