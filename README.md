File Transfer System

This is a simple Client-Server file transfer system that allows clients to download video files (.mp4) from the server. The system provides a list of available files and a progress bar for file downloads.

Features

Server provides a list of available .mp4 files.

Client can choose a file to download.

Real-time progress bar for download status.

Estimated time remaining for downloads.

Multiple clients can connect to the server.

Requirements

Java 8 or higher

Project Structure

Project/
│── src/
│   ├── Project/
│   │   ├── Client/
│   │   │   ├── Client.java
│   │   │   ├── recieveFile/ (Folder for downloaded files)
│   │   ├── Server/
│   │   │   ├── Server.java
│   │   │   ├── sendingFile/ (Folder containing files to be sent)

How to Run

Server

Place .mp4 files inside src/Project/Server/sendingFile/.

Compile and run the Server.java file:

javac src/Project/Server/Server.java
java Project.Server.Server

The server will listen on port 1234.

Client

Compile and run the Client.java file:

javac src/Project/Client/Client.java
java Project.Client.Client

Enter your name when prompted.

The client will receive a list of available files.

Enter the index of the file you wish to download.

The file will be saved in src/Project/Client/recieveFile/.

Optionally, download more files or exit.

Notes

The client downloads files in the recieveFile directory.

The server only sends .mp4 files.

Ensure that the server is running before starting a client.

License

This project is open-source and can be modified as needed.

