package de.umr.ds.task2;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EchoServer {

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
		System.out.println("Server is started!");
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(5555);
			executor.submit((Runnable) new TextClient());
			Socket clientSocket = serverSocket.accept();// establish connection and waits for the client
			System.out.println("Connected to Client");
			OutputStream outputStream = clientSocket.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream, true);

			InputStream inputStream = clientSocket.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


			String messages = null;
			String s;
			while ((messages = bufferedReader.readLine()) != null) {
				System.out.println("On Server: " + messages);
				writer.println(messages);
			}

			bufferedReader.close();
			writer.close();
			clientSocket.close();
			serverSocket.close();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// TODO Task 2b)

}
