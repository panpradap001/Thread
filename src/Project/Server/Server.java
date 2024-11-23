package Project.Server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server { // คลาสหลักสำหรับ Server
    private static final int PORT = 1234;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static int clientCount = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) { // เมธอดหลักที่ใช้ในการรัน Server
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String clientName = in.readUTF();
                synchronized (lock) {
                    clientCount++;
                }
                log("[" + clientName + "] connected");

                new ClientHandler(socket, clientName).start();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void log(String message) { // เมธอดสำหรับแสดง log พร้อมเวลาปัจจุบันและจำนวน client ที่เชื่อมต่ออยู่
        synchronized (lock) {
            System.out.println("[" + dateFormat.format(new Date()) + "] " + message + " (Clients connected: " + clientCount + ")");
        }
    }

    static class ClientHandler extends Thread { // คลาสสำหรับจัดการกับ Client แต่ละราย
        private Socket socket;
        private String clientName;

        public ClientHandler(Socket socket, String clientName) { // คอนสตรักเตอร์ รับ socket และชื่อของ client
            this.socket = socket;
            this.clientName = clientName;
        }

        @Override
        public void run() { // เมธอดที่ทำงานเมื่อเริ่ม Thread
            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {

                while (true) {
                    File folder = new File("src/Project/Server/sendingFile");
                    File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".mp4"));
                    List<String> fileDetails = new ArrayList<>();
                    for (int i = 0; i < listOfFiles.length; i++) {
                        long fileSize = listOfFiles[i].length();
                        fileDetails.add((i + 1) + ": " + listOfFiles[i].getName() + " [" + fileSize + " bytes]");
                    }
                    out.writeUTF(String.join("\n", fileDetails));

                    int fileIndex = Integer.parseInt(in.readUTF()) - 1;
                    File fileToSend = listOfFiles[fileIndex];
                    String fileName = fileToSend.getName();

                    long fileSize = fileToSend.length();
                    out.writeLong(fileSize);

                    FileInputStream fileIn = new FileInputStream(fileToSend);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    log("[" + clientName + "] started downloading " + fileName);

                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    fileIn.close();
                    log("[" + clientName + "] finished downloading " + fileName);

                    out.writeUTF("END");

                    log("Waiting for further instructions from [" + clientName + "]");
                    String response = in.readUTF().trim().toLowerCase();
                    if (!response.equals("y")) {
                        break;
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    clientCount--;
                }
                log("[" + clientName + "] disconnected");
            }
        }
    }
}
