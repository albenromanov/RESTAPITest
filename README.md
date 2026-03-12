# REST API Tester

A robust, Java Swing-based REST API testing application that allows you to easily simulate and analyze HTTP requests and responses. Designed as a lightweight desktop alternative to full-featured tools like Postman or Insomnia.

## 🚀 Features

### Core Capabilities
- **Comprehensive HTTP Methods**: Supports GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, CONNECT, and TRACE.
- **Dynamic Request Configuration**:
  - **Query Parameters**: Easily add, edit, and manage query strings.
  - **Custom Headers**: Send specific request headers.
  - **Authentication**: Built-in support for Basic Auth and Bearer Token authentication.
  - **Environment Variables**: Define dynamic variables injected via the `{{variableName}}` syntax throughout your URLs, headers, and body.
  - **Request Body**: Edit raw request payloads (JSON, text) for POST, PUT, and PATCH methods. Automatically disables the editor for unsupported methods.

### Execution & Response
- **Background Execution**: Keep the UI responsive while waiting for server responses via asynchronous `SwingWorker` threads.
- **Response Analysis**:
  - View explicit, color-coded HTTP status codes and response times.
  - Formatted JSON response bodies for optimal readability.
  - Dedicated tab to inspect response headers.
- **Convenience Tools**:
  - **Copy Response**: Instantly copy the full response payload to your system clipboard.
  - **cURL Export**: Convert any configured request into a valid shell `cURL` command and copy it to the clipboard.

### Request Management & History
- **Saved Requests**: Bookmark and name your frequently-used requests.
- **History Tracking**:
  - Tracks specific history per saved request.
  - Maintains an aggregate global history of all executed requests.
  - Click on any historical execution to reload its parameters completely.
  - Edit or delete Saved requests, and manage the History log to clear clutter.

---

## 🛠 Compilation and Usage

### Requirements
- Java 8 or higher
- No external dependencies/libraries. The project relies purely on standard Java `javax.swing.*` and `java.net.HttpURLConnection`.

### Compiling on the Command Line

```bash
# Navigate to the source folder
cd src/main/java

# Compile the file
javac com/apitester/RestApiTester.java
```

### Running the Application

```bash
# Run the compiled Main class
java com.apitester.RestApiTester
```

Alternatively, pack it into an executable JAR:
```bash
jar cfe RestApiTester.jar com.apitester.RestApiTester com/apitester/RestApiTester.class
java -jar RestApiTester.jar
```

---

## 📖 Quick Guide

1. **Set Up the Request**:  
   Select a method from the dropdown and paste your URL.
2. **Add Parameters / Headers / Auth**:  
   Switch through the tabs in the **Request** pane to add key/value data.
3. **Use Environment Variables (Optional)**:
   Add variables in the **Env** tab like `userId = 1`. Then use them in endpoints like `https://api.example.com/users/{{userId}}`.
4. **Execute**:  
   Click the **Send** button. View the data populate in the **Response** pane.
5. **Save State**:  
   Click the **Save** button, assign a description, and the request is safely bookmarked along with its future executed histories.

## License
Free to use, modify, and distribute.
