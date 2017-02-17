/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebWorker implements Runnable
{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      String path;
      path = readHTTPRequest(is);
      System.out.println("The path is:" + path);
      
      String fileType = "";
      fileType = findContentType(path);
      System.out.println("Content Type is:" + fileType);
      
      writeHTTPHeader(os, fileType);
      writeContent(os, fileType,path);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is){
	String line;
	String p="";
	BufferedReader r = new BufferedReader(new InputStreamReader(is));
	
	while (true) {
		
		try {
		
			while (!r.ready()) Thread.sleep(1);
			line = r.readLine();
			String[] a = line.split(" ");
			p = a[1];
			break;
		}catch (Exception e) {
			System.err.println("Request error: "+e);
			break;
		}//End of catch
		
   }//End of while
   return p;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write("HTTP/1.1 200 OK\n".getBytes());
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String contentType, String path) throws Exception{
	try{
	
		if(contentType.equals("text/html") ){
			BufferedReader reader = new BufferedReader (new FileReader(path));
			String line = "";
	        while((line = reader.readLine()) != null){
	        	Date d = new Date();
	        	DateFormat df = DateFormat.getDateTimeInstance();
	        	df.setTimeZone(TimeZone.getTimeZone("GMT"));
	        	line = line.replaceAll("<cs371date>", d.toString());//Replaces cs371date w/ date
	        	line = line.replaceAll("<cs371server>", "Melissa's Server");//Replaces cs371date w/ server
	        	os.write(line.getBytes());
	        }//End of while
	        reader.close();
	    	
	    	System.out.println("html");
	    }//End of if
		
		else if (contentType.equals("image/x-icon")){
			  
			System.out.println("ICO: write image content, filePath: "+path);
			 byte[] imageInByte;
			 System.out.println("ICO: trying to read FileInputStream image: "+path);
			 String fromFileName = path;
			 BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
			 byte[] buffer = new byte[32 * 1024];
			 int len = 0;
			 
			 while((len = in.read(buffer)) > 0){
			    os.write(buffer);
			  }
			 System.out.println("ICO: Done reading FileInputStream image: "+path);
			  in.close();
		}
	

	    else{
	    	FileInputStream fis = new FileInputStream(path);
	    	int i;
	    	while((i = fis.read())!= -1){
	    		os.write(i);
	    	}
	    	fis.close();
	    	System.out.println("Non-html");
	    }//End of else
	}//End of try
		
	catch(FileNotFoundException exception){
		   os.write("\n".getBytes());
	}//End of catch

}


//return the file types
private static String findContentType(String fileName)
{
	if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {return "text/html";}
	if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {return "image/jpeg";}
	if(fileName.endsWith(".png")) {return "image/png";}
	if(fileName.endsWith(".gif")) {return "image/gif";}
	if(fileName.endsWith(".ico")) {return "image/x-icon";}
	return "file type not suported";
}
} // end class
