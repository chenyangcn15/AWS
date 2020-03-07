package com.ccproject;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

@MultipartConfig
@WebServlet(name = "upload", urlPatterns = {"/upload"})
@SuppressWarnings("serial")
public class Upload extends HttpServlet {
	  final String mBucket = "ccproj2gcloud.appspot.com";

	  // initialize gcs service
	  private final GcsService mGcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	      .initialRetryDelayMillis(10)
	      .retryMaxAttempts(10)
	      .totalRetryPeriodMillis(15000)
	      .build());
	  
	  private Connection mConnection;
	  
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
		
	    if (req.getParameter("lat") == null || req.getParameter("lon") == null) {
		    resp.getWriter().print("You need provide lat and lon!\r\n");
		    return;
	    }
	    	
	    double latitude = Double.valueOf(req.getParameter("lat"));
	    double longitude = Double.valueOf(req.getParameter("lon"));
	    resp.getWriter().println("lat is :" + latitude);
	    resp.getWriter().println("lon is :" + longitude);
	    
//		String description = req.getParameter("description"); // Retrieves <input type="text" name="description">
	    Part filePart = req.getPart("file"); // Retrieves <input type="file" name="file">
	    if (filePart == null) {
		    resp.getWriter().print("File is null!\r\n");
		    return;
	    }
	    
//	    String imageName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
	    InputStream fileInputStream = filePart.getInputStream();
	    //[START original_image]
	    // Read the image.jpg resource into a ByteBuffer.
	    byte[] imageBytes = new byte[fileInputStream.available()];
	    fileInputStream.read(imageBytes);
	    fileInputStream.close();
	    String fileName =  UUID.randomUUID().toString() + ".jpeg";
	    // Write the original image to Cloud Storage
	    mGcsService.createOrReplace(
	        new GcsFilename(mBucket, fileName),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(imageBytes));

	    //[START resize]
	    // Get an instance of the imagesService we can use to transform images.
	    ImagesService imagesService = ImagesServiceFactory.getImagesService();

	    // Make an image directly from a byte array, and transform it.
	    Image image = ImagesServiceFactory.makeImage(imageBytes);
	    Point size = getProperSize();
	    Transform resize = ImagesServiceFactory.makeResize(size.x, size.y);
	    Image resizedImage = imagesService.applyTransform(resize, image);

	    final String resizedFileName = "resized" + fileName;
	    // Write the transformed image back to a Cloud Storage object.
	    mGcsService.createOrReplace(
	        new GcsFilename(mBucket, resizedFileName),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(resizedImage.getImageData()));
        
	    // The format of the large image url and the small image url is "bucket name/file name".
	    // The format is fixed.
	    String largeImgUrl = mBucket + "/" + fileName;
	    String smallImgUrl = mBucket + "/" + resizedFileName;
	    String desp = req.getParameter("desp");
	    resp.getWriter().println("desp is :" + desp);

	    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        		PrintStream ps = new PrintStream(bos, true, "utf-8");){
		    //Detection
			Detect.detectLabelsGcs("gs://" + largeImgUrl, ps);
			String label = new String(bos.toByteArray(), StandardCharsets.UTF_8);
			resp.getWriter().println(largeImgUrl);
			resp.getWriter().println(label);
		    CloudSql.insert(label, largeImgUrl, smallImgUrl, desp, latitude, longitude, mConnection);
		    resp.getWriter().println("upload success.");
		} catch (Exception e) {
			e.printStackTrace();
			resp.getWriter().println(e.getMessage());
		    resp.getWriter().print("Error with image dection!\r\n");
		}
	    
	}
	
	private Point getProperSize() {
		// TODO: calculate the proper size for the map marker
		Point point = new Point(50, 50);
		return point;
	}
	
	@Override
	public void init() throws ServletException {
		try {
			mConnection = Utils.connectToDatabase();
			CloudSql.createTable(mConnection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void destroy() {
		Utils.closeConnection(mConnection);
	}

}
