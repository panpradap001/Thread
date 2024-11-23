package Project.Client;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client { // คลาสหลักสำหรับ Client
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final int PROGRESS_BAR_LENGTH = 20;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    public static void main(String[] args) { // เมธอดหลักที่ใช้ในการรัน Client
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String clientName = scanner.nextLine();
            out.writeUTF(clientName);
            log("Connected to the server as [" + clientName + "]");

            while (true) {
                String fileDetails = in.readUTF();
                log("Available files:\n" + convertFileDetailsToMB(fileDetails));

                System.out.print("Enter the index of the file you want to download: ");
                String fileIndex = scanner.nextLine();
                out.writeUTF(fileIndex);

                long fileSize = in.readLong();
                log("Downloading file with index " + fileIndex);

                String[] fileDetailParts = fileDetails.split("\n")[Integer.parseInt(fileIndex) - 1].split(": ", 2);
                String fileNameWithSize = fileDetailParts[1];
                String fileName = fileNameWithSize.split(" \\[")[0];
                FileOutputStream fileOut = new FileOutputStream("src/Project/Client/recieveFile/downloaded_" + fileName);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                long startTime = System.currentTimeMillis();

                while (totalBytesRead < fileSize) {
                    bytesRead = in.read(buffer, 0, Math.min(buffer.length, (int) (fileSize - totalBytesRead)));
                    if (bytesRead == -1) {
                        break;
                    }

                    fileOut.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    int percent = (int) ((totalBytesRead * 100) / fileSize);

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long remainingTime = (fileSize - totalBytesRead) * elapsedTime / totalBytesRead;

                    int progressBars = (int) (percent / (100.0 / PROGRESS_BAR_LENGTH));
                    String progressBar = "[" + "|".repeat(progressBars)
                            + ".".repeat(PROGRESS_BAR_LENGTH - progressBars) + "]";

                    long remainingSeconds = remainingTime / 1000;
                    double totalMBRead = totalBytesRead / 1048576.0;
                    double totalMBSize = fileSize / 1048576.0;

                    System.out.printf("\rDownloading: %d%% %s (%.2f/%.2f MB), Time left: %d seconds",
                            percent, progressBar, totalMBRead, totalMBSize, remainingSeconds);
                }

                String endMessage = in.readUTF();
                if (endMessage.equals("END")) {
                    System.out.println();
                    log("File " + fileName + " downloaded successfully");
                }

                fileOut.close();

                System.out.print("Do you want to download another file? (y/n): ");
                String response;
                boolean x = false;
                while (true) {
                    response = scanner.nextLine().trim().toLowerCase();

                    if (response.equals("y")) {
                        out.writeUTF(response);
                        break;
                    } else if (response.equals("n")) {
                        x = true;
                        out.writeUTF(response);
                        break;
                    } else {
                        System.out.print("Please enter 'y' or 'n': ");
                    }
                }
                if (x) {
                    break;
                }
            }

        } catch (IOException ex) {
            log("An error occurred: " + ex.getMessage());
        }
    }

    private static void log(String message) { // เมธอดสำหรับแสดง log พร้อมเวลาปัจจุบัน
        System.out.println("[" + dateFormat.format(new Date()) + "] " + message);
    }

    private static String convertFileDetailsToMB(String fileDetails) { // เมธอดสำหรับแปลงขนาดไฟล์เป็นหน่วย MB
        String[] files = fileDetails.split("\n");
        StringBuilder result = new StringBuilder();

        for (String file : files) {
            String[] parts = file.split(" \\[");
            String fileName = parts[0];
            long fileSize = Long.parseLong(parts[1].replaceAll("[^0-9]", ""));
            double fileSizeMB = fileSize / 1048576.0;
            result.append(fileName).append(" [").append(String.format("%.2f", fileSizeMB)).append(" MB]").append("\n");
        }

        return result.toString().trim();
    }
}
