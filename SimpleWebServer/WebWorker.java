/**
*
*Melissa Duran
*
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
import java.nio.file.*;

public class WebWorker implements Runnable
{

private Socket socket;
private String path = "";
private boolean fileExists;
private String fileFromDoc = "";




/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}




/**
* Pre-Requesits: a valid open socket object.
* Description: Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with 
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      	InputStream  is = socket.getInputStream();//Receives input
    	OutputStream os = socket.getOutputStream();//Will write output
      	readHTTPRequest(is);
        
	//Checks if file exists in host computer
        File varTmpDir = new File(path);
	fileExists = varTmpDir.exists();
	System.out.println("fileExists = " + fileExists);
 
      	fileFromDoc = convertFileToString();//Converts file to a string
      	writeHTTPHeader(os,"text/html");
      	writeContent(os);
      	os.flush();
      	socket.close();}
 
   catch (Exception e) {
      	System.err.println("Output error: "+e);}

   System.err.println("Done handling connection.");
   return;
}




/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
            line = r.readLine();

	    //Grabs the path and cleans it up
	    if(path == ""){
		path = line.substring(4, line.indexOf("H"));//Grabs the path
		path = path.replaceAll("\\s+","");//Gets rid of spaces
		System.out.println("Path: " + path);}//Prints the path
			
            System.err.println("Request line: ("+line+")");//pritns the line
            if (line.length()==0) break;}
       
      catch (Exception e) {
         System.err.println("Request error: "+e);
         break;}
   }
   return;
}//End readHTTPRequest




/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{


   Date d = new Date();//Gets current date
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   //os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   //os.write("Connection: close\n".getBytes());
   //os.write("Content-Type: ".getBytes());
   //os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines

   //Prints statements when file exists on comp
   if(fileExists == true)
	os.write("HTTP/1.1 200 OK\n".getBytes());
	
   //Prints statements when file doesn't exists on comp
   else if (fileExists == false)
	os.write("HTTP/1.1 404 Not Found\n".getBytes());
	

   return;
}




/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os) throws Exception
{
   Date d2 = new Date();
   String server = "Server: Melissa Duran's Server\n";

   fileFromDoc = fileFromDoc.replaceAll("<cs371date>", d2.toString());//Replaces cs371date w/ date
   fileFromDoc = fileFromDoc.replaceAll("<cs371server>", server);//Replaces cs371date w/ server

   os.write(fileFromDoc.getBytes());//Writes doc.
}



/**
* Converts file into a string
**/
private String convertFileToString(){
	String content = "";
   try {
       BufferedReader in = new BufferedReader(new FileReader(path));
       String str;
       while ((str = in.readLine()) != null) {
           content +=str;
       }
       in.close();
   } catch (IOException e) {

   }
	return content;
}

} // end class