# REST API Tester

A Java Swing-based REST API testing application that allows you to send HTTP requests and view responses.

## Features

- **HTTP Methods**: Supports GET, POST, PUT, DELETE, PATCH, HEAD, and OPTIONS
- **Custom Headers**: Add multiple custom headers to your requests
- **Request Body**: Send JSON or other data in the request body
- **Response Viewer**: View formatted responses with status codes and response times
- **JSON Formatting**: Automatic JSON response formatting for better readability
- **User-Friendly GUI**: Clean and intuitive graphical interface

## Requirements

- Java 8 or higher
- No external dependencies required (uses built-in Java Swing and HttpURLConnection)

## Compilation

### Using javac (Command Line)

```bash
cd rest-api-tester/src/main/java
javac com/apitester/RestApiTester.java
```

### Using a Build Tool

You can also integrate this into a Maven or Gradle project if desired.

## Running the Application

### From Command Line

```bash
cd rest-api-tester/src/main/java
java com.apitester.RestApiTester
```

### Create JAR File

```bash
cd rest-api-tester/src/main/java
javac com/apitester/RestApiTester.java
jar cfe RestApiTester.jar com.apitester.RestApiTester com/apitester/RestApiTester.class
java -jar RestApiTester.jar
```

## Usage

1. **Select HTTP Method**: Choose from GET, POST, PUT, DELETE, etc.
2. **Enter URL**: Type the API endpoint URL
3. **Add Headers** (optional): 
   - Go to the Headers tab
   - Enter key-value pairs
   - Click "Add Header" for more rows
4. **Add Request Body** (optional):
   - Go to the Body tab
   - Enter your JSON or other data
5. **Click Send**: The response will appear in the Response section
6. **View Results**: Check the status code, response time, and formatted response body

## Example Usage

The application comes pre-configured with a sample request to JSONPlaceholder API:
- URL: `https://jsonplaceholder.typicode.com/posts/1`
- Method: GET

You can test it immediately by clicking the "Send" button.

### Testing POST Request

1. Change method to POST
2. URL: `https://jsonplaceholder.typicode.com/posts`
3. Body tab:
```json
{
  "title": "foo",
  "body": "bar",
  "userId": 1
}
```
4. Click Send

## Features in Detail

- **Status Display**: Shows HTTP status code with color coding (green for 2xx, red for 4xx/5xx)
- **Response Time**: Displays how long the request took in milliseconds
- **JSON Formatting**: Automatically formats JSON responses for better readability
- **Error Handling**: Displays error messages if the request fails

## License

Free to use and modify.
