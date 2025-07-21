package de.umr.ds.task2;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TextClient implements  Runnable {

	@Override
	public void run() {
			Scanner scanner=new Scanner(System.in);
			try {
				Socket client = new Socket("localhost",5555);
				System.out.println("Client Started");

				InputStream inputStream=client.getInputStream();
				BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));

				OutputStream outputStream=client.getOutputStream();
				PrintWriter writer=new PrintWriter(outputStream);



				System.out.println("Eingabe: ");
				String toServer=scanner.nextLine();
				writer.write(toServer+ "\n");
				//  flush output streams after a message has been sent
				writer.flush();



				String s;
				while ((s=reader.readLine())!=null){
					System.out.println("Received from Server: "+s);

				}
				reader.close();
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


