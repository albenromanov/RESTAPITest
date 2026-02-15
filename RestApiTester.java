package com.apitester;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

public class RestApiTester extends JFrame {
    private JComboBox<String> methodCombo;
    private JTextField urlField;
    private JTable headersTable;
    private DefaultTableModel headersModel;
    private JTable paramsTable;
    private DefaultTableModel paramsModel;
    private JTextArea requestBodyArea;
    private JTextArea responseArea;
    private JTextArea responseHeadersArea;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JTabbedPane requestTabs;
    private JTabbedPane responseTabs;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;
    private JComboBox<RequestData> savedRequestsCombo;
    private DefaultComboBoxModel<RequestData> savedRequestsModel;
    private List<RequestData> requestHistory = new ArrayList<>();

    private static class RequestData {
        String method;
        String url;
        Map<String, String> headers;
        Map<String, String> params;
        String body;
        String description;
        List<RequestData> executionHistory = new ArrayList<>();

        RequestData(String method, String url, Map<String, String> headers, Map<String, String> params, String body,
                String description) {
            this.method = method;
            this.url = url;
            this.headers = new HashMap<>(headers);
            this.params = new HashMap<>(params);
            this.body = body;
            this.description = description;
        }

        @Override
        public String toString() {
            if (description != null && !description.trim().isEmpty()) {
                if (description.equals("--- All History ---"))
                    return description;
                return description;
            }
            return method + ": " + (url.length() > 50 ? url.substring(0, 47) + "..." : url);
        }
    }

    private RequestData allHistoryStub = new RequestData("", "", Collections.emptyMap(), Collections.emptyMap(), "",
            "--- All History ---");
    private RequestData activeSavedRequest = null;

    public RestApiTester() {
        setTitle("REST API Tester");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // History Panel
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadRequestFromHistory();
            }
        });

        // Saved Requests Management Panel
        savedRequestsModel = new DefaultComboBoxModel<>();
        savedRequestsModel.addElement(allHistoryStub);
        savedRequestsCombo = new JComboBox<>(savedRequestsModel);
        savedRequestsCombo.setPreferredSize(new Dimension(150, 25));
        savedRequestsCombo.addActionListener(e -> {
            RequestData selected = (RequestData) savedRequestsCombo.getSelectedItem();
            if (selected == allHistoryStub) {
                activeSavedRequest = null;
                updateHistoryUI();
            } else if (selected != null) {
                activeSavedRequest = selected;
                loadRequest(selected);
                updateHistoryUI();
            }
        });

        JButton editButton = new JButton("Edit");
        editButton.setMargin(new Insets(1, 4, 1, 4));
        editButton.addActionListener(e -> editSavedRequest());

        JButton deleteButton = new JButton("Del");
        deleteButton.setMargin(new Insets(1, 4, 1, 4));
        deleteButton.addActionListener(e -> deleteSavedRequest());

        JPanel savedMgmtPanel = new JPanel(new BorderLayout(2, 2));
        savedMgmtPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        savedMgmtPanel.add(savedRequestsCombo, BorderLayout.CENTER);

        JPanel savedButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        savedButtonPanel.add(editButton);
        savedButtonPanel.add(deleteButton);
        savedMgmtPanel.add(savedButtonPanel, BorderLayout.SOUTH);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new TitledBorder("History"));

        JPanel leftPanelContent = new JPanel(new BorderLayout());
        leftPanelContent.add(savedMgmtPanel, BorderLayout.NORTH);
        leftPanelContent.add(new JScrollPane(historyList), BorderLayout.CENTER);

        historyPanel.add(leftPanelContent, BorderLayout.CENTER);

        JButton removeHistoryButton = new JButton("Remove Selected");
        removeHistoryButton.addActionListener(e -> removeSelectedHistory());
        historyPanel.add(removeHistoryButton, BorderLayout.SOUTH);

        historyPanel.setPreferredSize(new Dimension(220, 0));

        // Top panel - URL and method
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        methodPanel.add(new JLabel("Method:"));
        String[] methods = { "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS" };
        methodCombo = new JComboBox<>(methods);
        methodCombo.setPreferredSize(new Dimension(100, 30));
        methodPanel.add(methodCombo);

        urlField = new JTextField();
        urlField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        urlField.setText("https://jsonplaceholder.typicode.com/posts/1");

        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendRequest());

        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(80, 30));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveCurrentRequest());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(sendButton);

        topPanel.add(methodPanel, BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Request panel
        JPanel requestPanel = new JPanel(new BorderLayout(5, 5));
        requestPanel.setBorder(new TitledBorder("Request"));

        requestTabs = new JTabbedPane();

        // Params tab
        JPanel paramsPanel = new JPanel(new BorderLayout());
        paramsModel = new DefaultTableModel(new String[] { "Key", "Value" }, 5);
        paramsTable = new JTable(paramsModel);
        paramsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        paramsPanel.add(new JScrollPane(paramsTable), BorderLayout.CENTER);

        JButton addParamButton = new JButton("Add Param");
        addParamButton.addActionListener(e -> paramsModel.addRow(new Object[] { "", "" }));
        JPanel paramButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paramButtonPanel.add(addParamButton);
        paramsPanel.add(paramButtonPanel, BorderLayout.SOUTH);

        // Headers tab
        JPanel headersPanel = new JPanel(new BorderLayout());
        String[] columnNames = { "Key", "Value" };
        headersModel = new DefaultTableModel(columnNames, 5);
        headersTable = new JTable(headersModel);
        headersTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        headersPanel.add(new JScrollPane(headersTable), BorderLayout.CENTER);

        JButton addHeaderButton = new JButton("Add Header");
        addHeaderButton.addActionListener(e -> headersModel.addRow(new Object[] { "", "" }));
        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerButtonPanel.add(addHeaderButton);
        headersPanel.add(headerButtonPanel, BorderLayout.SOUTH);

        // Body tab
        JPanel bodyPanel = new JPanel(new BorderLayout());
        requestBodyArea = new JTextArea();
        requestBodyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        requestBodyArea.setText("{\n  \"title\": \"foo\",\n  \"body\": \"bar\",\n  \"userId\": 1\n}");
        JScrollPane bodyScroll = new JScrollPane(requestBodyArea);
        bodyPanel.add(bodyScroll, BorderLayout.CENTER);

        requestTabs.addTab("Params", paramsPanel);
        requestTabs.addTab("Headers", headersPanel);
        requestTabs.addTab("Body", bodyPanel);

        requestPanel.add(requestTabs, BorderLayout.CENTER);

        // Response panel
        JPanel responsePanel = new JPanel(new BorderLayout(5, 5));
        responsePanel.setBorder(new TitledBorder("Response"));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusLabel = new JLabel("Status: --");
        timeLabel = new JLabel("Time: --");
        leftStatusPanel.add(statusLabel);
        leftStatusPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftStatusPanel.add(timeLabel);

        JButton copyButton = new JButton("Copy Response");
        copyButton.addActionListener(e -> copyToClipboard(responseArea.getText()));
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightStatusPanel.add(copyButton);

        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);

        responseTabs = new JTabbedPane();

        // Response body tab
        responseArea = new JTextArea();
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responseArea.setEditable(false);
        JScrollPane responseScroll = new JScrollPane(responseArea);

        // Response headers tab
        responseHeadersArea = new JTextArea();
        responseHeadersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responseHeadersArea.setEditable(false);
        JScrollPane responseHeadersScroll = new JScrollPane(responseHeadersArea);

        responseTabs.addTab("Body", responseScroll);
        responseTabs.addTab("Headers", responseHeadersScroll);

        responsePanel.add(statusPanel, BorderLayout.NORTH);
        responsePanel.add(responseTabs, BorderLayout.CENTER);

        // Split panes
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, requestPanel, responsePanel);
        verticalSplit.setDividerLocation(300);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, historyPanel, verticalSplit);
        mainSplit.setDividerLocation(220);
        mainSplit.setBorder(new EmptyBorder(5, 10, 10, 10));

        add(topPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void sendRequest() {
        String baseUrl = urlField.getText().trim();
        String method = (String) methodCombo.getSelectedItem();

        if (baseUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear previous response
        responseArea.setText("Sending request...");
        responseHeadersArea.setText("");
        statusLabel.setText("Status: --");
        timeLabel.setText("Time: --");

        // Execute request in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String response;
            private String responseHeaders;
            private int statusCode;
            private long responseTime;
            private String errorMessage;

            @Override
            protected Void doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();

                try {
                    // Build URL with query parameters
                    Map<String, String> params = getParams();
                    StringBuilder urlBuilder = new StringBuilder(baseUrl);
                    if (!params.isEmpty()) {
                        if (!baseUrl.contains("?")) {
                            urlBuilder.append("?");
                        } else if (!baseUrl.endsWith("&") && !baseUrl.endsWith("?")) {
                            urlBuilder.append("&");
                        }

                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                                    .append("=")
                                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                                    .append("&");
                        }
                        urlBuilder.setLength(urlBuilder.length() - 1); // Remove last &
                    }

                    URL apiUrl = new URL(urlBuilder.toString());
                    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setRequestMethod(method);

                    // Set headers
                    Map<String, String> headers = getHeaders();
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }

                    // Set request body for POST, PUT, PATCH
                    String requestBody = requestBodyArea.getText();
                    if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
                        connection.setDoOutput(true);

                        if (!requestBody.trim().isEmpty()) {
                            if (!headers.containsKey("Content-Type")) {
                                connection.setRequestProperty("Content-Type", "application/json");
                            }

                            try (OutputStream os = connection.getOutputStream()) {
                                byte[] input = requestBody.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }
                        }
                    }

                    // Get response
                    statusCode = connection.getResponseCode();

                    // Get response headers
                    StringBuilder headersBuilder = new StringBuilder();
                    connection.getHeaderFields().forEach((key, values) -> {
                        if (key != null) {
                            headersBuilder.append(key).append(": ").append(String.join(", ", values)).append("\n");
                        } else {
                            headersBuilder.append(String.join(", ", values)).append("\n");
                        }
                    });
                    responseHeaders = headersBuilder.toString();

                    InputStream inputStream;
                    try {
                        inputStream = connection.getInputStream();
                    } catch (IOException e) {
                        inputStream = connection.getErrorStream();
                    }

                    if (inputStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line).append("\n");
                        }
                        reader.close();

                        response = formatJson(responseBuilder.toString());
                    } else {
                        response = "";
                    }

                    connection.disconnect();
                    responseTime = System.currentTimeMillis() - startTime;

                    // Add to history
                    SwingUtilities.invokeLater(() -> addToHistory(method, baseUrl, headers, params, requestBody, null));

                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    responseTime = System.currentTimeMillis() - startTime;
                }

                return null;
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    responseArea.setText("Error: " + errorMessage);
                    statusLabel.setText("Status: Error");
                    statusLabel.setForeground(Color.RED);
                } else {
                    responseArea.setText(response);
                    responseHeadersArea.setText(responseHeaders);

                    Color statusColor;
                    if (statusCode >= 200 && statusCode < 300) {
                        statusColor = new Color(76, 175, 80);
                    } else if (statusCode >= 400) {
                        statusColor = Color.RED;
                    } else {
                        statusColor = Color.ORANGE;
                    }

                    statusLabel.setText("Status: " + statusCode + " " + getStatusText(statusCode));
                    statusLabel.setForeground(statusColor);
                }

                timeLabel.setText("Time: " + responseTime + " ms");
                responseArea.setCaretPosition(0);
                responseHeadersArea.setCaretPosition(0);
            }
        };

        worker.execute();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        for (int i = 0; i < headersModel.getRowCount(); i++) {
            String key = (String) headersModel.getValueAt(i, 0);
            String value = (String) headersModel.getValueAt(i, 1);

            if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                headers.put(key.trim(), value.trim());
            }
        }

        return headers;
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; i < paramsModel.getRowCount(); i++) {
            String key = (String) paramsModel.getValueAt(i, 0);
            String value = (String) paramsModel.getValueAt(i, 1);

            if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                params.put(key.trim(), value.trim());
            }
        }

        return params;
    }

    private void addToHistory(String method, String url, Map<String, String> headers, Map<String, String> params,
            String body, String description) {
        RequestData data = new RequestData(method, url, headers, params, body, description);

        if (description != null) {
            // It's a saved request
            requestHistory.add(0, data);
        } else {
            // It's an execution
            requestHistory.add(0, data);
            if (activeSavedRequest != null) {
                activeSavedRequest.executionHistory.add(0, data);
                if (activeSavedRequest.executionHistory.size() > 50) {
                    activeSavedRequest.executionHistory.remove(50);
                }
            }
        }

        if (requestHistory.size() > 500) { // All history can be larger
            requestHistory.remove(500);
        }
        updateHistoryUI();
    }

    private void saveCurrentRequest() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL to save", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String description = JOptionPane.showInputDialog(this, "Enter a description for this request:", "Save Request",
                JOptionPane.QUESTION_MESSAGE);

        if (description != null) {
            String method = (String) methodCombo.getSelectedItem();
            Map<String, String> headers = getHeaders();
            Map<String, String> params = getParams();
            String body = requestBodyArea.getText();

            addToHistory(method, url, headers, params, body, description);
        }
    }

    private void loadRequestFromHistory() {
        int index = historyList.getSelectedIndex();
        List<RequestData> currentList = (activeSavedRequest != null) ? activeSavedRequest.executionHistory
                : requestHistory;
        if (index >= 0 && index < currentList.size()) {
            loadRequest(currentList.get(index));
        }
    }

    private void loadRequest(RequestData data) {
        if (data == null)
            return;
        methodCombo.setSelectedItem(data.method);
        urlField.setText(data.url);

        // Load headers
        headersModel.setRowCount(0);
        for (Map.Entry<String, String> entry : data.headers.entrySet()) {
            headersModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
        if (headersModel.getRowCount() < 5) {
            headersModel.setRowCount(5);
        }

        // Load params
        paramsModel.setRowCount(0);
        for (Map.Entry<String, String> entry : data.params.entrySet()) {
            paramsModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
        if (paramsModel.getRowCount() < 5) {
            paramsModel.setRowCount(5);
        }

        requestBodyArea.setText(data.body);
    }

    private void editSavedRequest() {
        RequestData selected = (RequestData) savedRequestsCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a saved request to edit.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newDescription = JOptionPane.showInputDialog(this, "Edit description:", "Edit Saved Request",
                JOptionPane.QUESTION_MESSAGE, null, null, selected.description).toString();

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            selected.description = newDescription.trim();
            updateHistoryUI();
        }
    }

    private void deleteSavedRequest() {
        RequestData selected = (RequestData) savedRequestsCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a saved request to delete.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this saved request?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            requestHistory.remove(selected);
            updateHistoryUI();
        }
    }

    private void updateHistoryUI() {
        historyModel.clear();
        List<RequestData> listToShow = (activeSavedRequest != null) ? activeSavedRequest.executionHistory
                : requestHistory;

        for (RequestData data : listToShow) {
            historyModel.addElement(data.toString());
        }

        // Update combo if needed (only if we are adding/deleting SAVED requests)
        RequestData currentSelected = (RequestData) savedRequestsCombo.getSelectedItem();
        savedRequestsModel.removeAllElements();
        savedRequestsModel.addElement(allHistoryStub);
        for (RequestData data : requestHistory) {
            if (data.description != null && !data.description.trim().isEmpty() && data != allHistoryStub) {
                savedRequestsModel.addElement(data);
            }
        }
        savedRequestsCombo.setSelectedItem(currentSelected != null ? currentSelected : allHistoryStub);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty())
            return;
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        JOptionPane.showMessageDialog(this, "Response copied to clipboard!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeSelectedHistory() {
        int index = historyList.getSelectedIndex();
        if (index >= 0) {
            List<RequestData> currentList = (activeSavedRequest != null) ? activeSavedRequest.executionHistory
                    : requestHistory;
            if (index < currentList.size()) {
                RequestData toRemove = currentList.get(index);
                currentList.remove(index);
                // If we are in local view, also remove from global if it's there
                if (activeSavedRequest != null) {
                    requestHistory.remove(toRemove);
                }
            }
            updateHistoryUI();
        }
    }

    private String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        try {
            StringBuilder formatted = new StringBuilder();
            int indent = 0;
            boolean inString = false;

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);

                if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                    inString = !inString;
                }

                if (!inString) {
                    if (c == '{' || c == '[') {
                        formatted.append(c).append('\n');
                        indent++;
                        formatted.append("  ".repeat(indent));
                    } else if (c == '}' || c == ']') {
                        formatted.append('\n');
                        indent--;
                        formatted.append("  ".repeat(indent));
                        formatted.append(c);
                    } else if (c == ',') {
                        formatted.append(c).append('\n');
                        formatted.append("  ".repeat(indent));
                    } else if (c == ':') {
                        formatted.append(c).append(' ');
                    } else if (!Character.isWhitespace(c)) {
                        formatted.append(c);
                    }
                } else {
                    formatted.append(c);
                }
            }

            return formatted.toString();
        } catch (Exception e) {
            return json;
        }
    }

    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new RestApiTester());
    }
}
