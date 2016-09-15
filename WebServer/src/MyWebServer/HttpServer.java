package MyWebServer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import MyThreadPool.DefaultThreadPool;
import MyThreadPool.MyThreadPool;

public class HttpServer {
	private static MyThreadPool<Runnable> pool = new DefaultThreadPool<Runnable>(10);
	private static int port = 80;
	private static String basePath;
	private static ServerSocket serverSocket;
	public static void setPort(int port){
		if(port>0)
			HttpServer.port = port;
	}
	
	public static void setBasePath(String path) throws IOException{
		if(path!=null && new File(path).exists() && new File(path).isDirectory()){
			HttpServer.basePath =path;
			//System.out.println("路径正确");
		}
	}
	public static void start() throws IOException{
		serverSocket = new ServerSocket(port);
		System.out.println("服务器启动");
		Socket socket;
		while((socket=serverSocket.accept())!= null ){
				pool.execute(new HttpRequestHandler(socket));
		}
		//serverSocket.close();
	}
	
	
	static class HttpRequestHandler implements Runnable{
		private Socket socket;
		
		public HttpRequestHandler(Socket socket){
			this.socket = socket;
		}
		@Override
		public void run() {
			System.out.println(socket.hashCode());
			BufferedReader  br= null;
			PrintWriter out = null;
			InputStream in = null;
			try {
				 br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//				while((line=br.readLine())!=null){
//					System.out.println(line);
//				}
				String header = br.readLine();
				String filePath = basePath +header.split(" ")[1];
				System.out.println(filePath);
				//输入流读取请求的文件
				in= new FileInputStream(new File(filePath));
				//文件流写到输出流中
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int i =0;
				while((i=in.read())!=-1){
					baos.write(i);
				}
				byte[] bytes =baos.toByteArray();
				out = new PrintWriter(socket.getOutputStream());
				out.println("HTTP/1.1 200 OK");
				out.println("Server: Molly");
				out.println("Content-Type: text/html");
				out.println("Content-Length: "+bytes.length);
				out.println("");
				out.flush();
				socket.getOutputStream().write(bytes,0,bytes.length);
			} catch (IOException e) {
			
			}finally{
				try{
					socket.shutdownInput();
					socket.shutdownOutput();
					br.close();
					in.close();
					out.close();
					socket.close();
				}catch(Exception e){
					
				}
				
			}
			
		}
		
		public static void close(Closeable... closeables){
			if(closeables!=null){
				for (Closeable closeable : closeables) {
					try {
						closeable.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
