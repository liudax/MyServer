package MyWebServer;

import java.io.IOException;


public class ServerSocketTest {
	public static void main(String[] args) {
		HttpServer.setPort(8082);
		try {
			HttpServer.setBasePath("WebRoot");
			HttpServer.start();
			
		} catch (IOException e) {
			System.out.println("·þÎñÆ÷Æô¶¯Ê§°Ü");
			e.printStackTrace();
		}
	}
}
