package com.takeshi.sharedlink.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeshi.sharedlink.SharedLinkApplication;
import com.takeshi.sharedlink.bo.RequestDatalistBO;
import com.takeshi.sharedlink.bo.UserInfoBO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * SharedLinkController
 *
 * @author 七濑武【Nanase Takeshi】
 */
public class SharedLinkController {

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Pane mask;

    @FXML
    private TextArea sharedLinkTextArea;

    @FXML
    private TextFlow logTextFlow;

    @FXML
    private Label userNameLabel;

    private String cookie;

    private String cid;

    private UserInfoBO.Data userData;

    public synchronized void setUserData(String cookie, String cid) throws IOException, InterruptedException {
        this.cookie = cookie;
        this.cid = cid;
        this.userData = this.getUserId();
        this.userNameLabel.setText(this.userData.getUname());
    }

    @FXML
    public synchronized void onReturnButtonClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(SharedLinkApplication.class.getResource("frontPage-view.fxml"));
        Scene frontPageScene = new Scene(loader.load());
        Stage stage = (Stage) this.userNameLabel.getScene().getWindow();
        stage.setScene(frontPageScene);
        stage.show();
    }

    @FXML
    public synchronized void onSaveLinkFromText(ActionEvent event) {
        this.logTextFlow.getChildren().clear();
        Button clickedButton = (Button) event.getSource();
        Platform.runLater(() -> {
            clickedButton.setDisable(true);
            loadingIndicator.setVisible(true);
            mask.setVisible(true);
        });
        new Thread(() -> {
            try {
                String sharedLinkText = this.sharedLinkTextArea.getText();
                if (Objects.isNull(sharedLinkText) || sharedLinkText.isBlank()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("输入错误");
                        alert.setContentText("请输入需要转存的115网盘分享链接内容");
                        alert.showAndWait();
                    });
                    return;
                }
                List<Matcher> matcherList =
                        Stream.of(sharedLinkText.split("\\r?\\n"))
                              .map(CODE_PATTERN::matcher)
                              .filter(Matcher::find)
                              .toList();
                if (matcherList.isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("输入错误");
                        alert.setContentText("请输入需要转存的115网盘分享链接内容");
                        alert.showAndWait();
                    });
                    return;
                }
                this.saveLinkFromText(matcherList);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setContentText("系统错误: " + e.getMessage());
                    alert.showAndWait();
                });
            } finally {
                Platform.runLater(() -> {
                    clickedButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    mask.setVisible(false);
                });
            }
        }).start();
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern CODE_PATTERN = Pattern.compile("https://115\\.com/s/(\\w+)\\?password=(\\w+)");

    private static final String REQUEST_DATALIST_URL = "https://webapi.115.com/share/snap?share_code=%s&offset=%d&limit=20&receive_code=%s&cid=";

    private static final String FORMAT_URL = "[https://115.com/share/s/%s?password=%s&#]";

    public UserInfoBO.Data getUserId() throws IOException, InterruptedException {
        JsonNode responseJson = this.get("https://my.115.com/?ct=ajax&ac=get_user_aq");
        System.out.printf("Fake115Client.getUserId --> responseJson: %s%n", responseJson);
        UserInfoBO userInfoBO = objectMapper.convertValue(responseJson, UserInfoBO.class);
        if (Objects.nonNull(userInfoBO.getState()) && userInfoBO.getState()) {
            return userInfoBO.getData();
        } else {
            throw new RuntimeException(String.format("获取用户信息错误：%s", userInfoBO.getError_msg()));
        }
    }

    public synchronized void saveLinkFromText(List<Matcher> matcherList) {
        for (int i = 0; i < matcherList.size(); i++) {
            Matcher matcher = matcherList.get(i);
            String shareCode = matcher.group(1);
            String receiveCode = matcher.group(2);
            try {
                this.saveBySr(i, matcherList.size(), shareCode, receiveCode);
            } catch (IOException | InterruptedException e) {
                this.appendErrorLog(String.format(FORMAT_URL, shareCode, receiveCode) + " 请求接口异常： " + e.getMessage() + "\n");
                throw new RuntimeException(e);
            } catch (Exception e) {
                this.appendErrorLog(String.format(FORMAT_URL, shareCode, receiveCode) + " 系统运行异常： " + e.getMessage() + "\n");
                throw new RuntimeException(e);
            }
        }
    }

    public void saveBySr(int index, int total, String shareCode, String receiveCode) throws IOException, InterruptedException {
        RequestDatalistBO requestDatalistBO = this.requestDatalist(shareCode, receiveCode);
        if (Objects.nonNull(requestDatalistBO)) {
            List<Map<String, Object>> dataList = requestDatalistBO.getDataList();
            if (Objects.nonNull(dataList) && !dataList.isEmpty()) {
                String fileIdStr = dataList.stream()
                                           .map(item -> {
                                               String fid = (String) item.get("fid");
                                               return Objects.nonNull(fid) && !fid.isBlank() ? fid : (String) item.get("cid");
                                           })
                                           .collect(Collectors.joining(","));
                this.postSave(index, total, shareCode, receiveCode, fileIdStr);
            }
        }
    }

    public RequestDatalistBO requestDatalist(String shareCode, String receiveCode) throws IOException, InterruptedException {
        List<Map<String, Object>> dataList;
        JsonNode responseJson = this.get(String.format(REQUEST_DATALIST_URL, shareCode, 0, receiveCode));
        System.out.printf("Fake115Client.requestDatalist --> responseJson: %s%n", responseJson);
        if (responseJson.path("state").asBoolean()) {
            int count = responseJson.path("data").path("count").asInt();
            dataList = objectMapper.convertValue(responseJson.path("data").path("list"), new TypeReference<>() {
            });
            while (dataList.size() < count) {
                JsonNode nextResponseJson = this.get(String.format(REQUEST_DATALIST_URL, shareCode, dataList.size(), receiveCode));
                System.out.printf("Fake115Client.requestDatalist --> nextResponseJson: %s%n", nextResponseJson);
                dataList.addAll(objectMapper.convertValue(nextResponseJson.path("data").path("list"), new TypeReference<List<Map<String, Object>>>() {
                }));
            }
        } else {
            this.appendErrorLog(String.format(FORMAT_URL, shareCode, receiveCode) + " 获取请求数据列表异常: " + responseJson.path("error").asText());
            return null;
        }
        Map<String, Object> shareInfo = objectMapper.convertValue(responseJson.path("data").path("shareinfo"), new TypeReference<>() {
        });
        return new RequestDatalistBO(shareInfo, dataList);
    }

    public void postSave(int index, int total, String shareCode, String receiveCode, String fileIdStr) throws IOException, InterruptedException {
        String formatUrl = String.format(FORMAT_URL, shareCode, receiveCode);
        this.appendInfoLog("\n" + formatUrl + " ...正在转存 " + "(" + (index + 1) + "/" + total + ")...\n ");
        Map<String, Object> formData = new HashMap<>();
        formData.put("user_id", this.userData.getUid());
        formData.put("share_code", shareCode);
        formData.put("receive_code", receiveCode);
        formData.put("file_id", fileIdStr);
        if (Objects.nonNull(this.cid) && !this.cid.isBlank()) {
            formData.put("cid", this.cid);
        }
        JsonNode responseJson = this.post("https://webapi.115.com/share/receive", formData);
        System.out.printf("Fake115Client.postSave --> responseJson: %s%n", responseJson);
        if (responseJson.path("state").asBoolean()) {
            this.appendInfoLog(formatUrl + " 转存成功: " + "\n");
        } else {
            this.appendErrorLog(formatUrl + " 转存失败: " + responseJson.path("error").asText() + "\n");
        }
        if (index < total) {
            // 每转存一个休眠5秒
            TimeUnit.SECONDS.sleep(5);
        }
    }

    public JsonNode get(String url) throws IOException, InterruptedException {
        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                           .uri(URI.create(url))
                           .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                           .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                           .header("Accept-Encoding", "gzip, deflate, br")
                           .header("Cookie", this.cookie)
                           .GET()
                           .build();
        return this.getJsonNode(httpRequest);
    }

    public JsonNode post(String url, Map<String, Object> formData) throws IOException, InterruptedException {
        // 将 Map 转换为 x-www-form-urlencoded 格式的字符串
        String formBody = formData.entrySet()
                                  .stream()
                                  .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                                  .collect(Collectors.joining("&"));
        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                           .uri(URI.create(url))
                           .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                           .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                           .header("Accept-Encoding", "gzip, deflate, br")
                           .header("Cookie", this.cookie)
                           .POST(HttpRequest.BodyPublishers.ofString(formBody))
                           .build();
        return this.getJsonNode(httpRequest);
    }

    private JsonNode getJsonNode(HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<byte[]> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        JsonNode responseJson;
        if (httpResponse.statusCode() == 200) {
            if ("gzip".equals(httpResponse.headers().firstValue("Content-Encoding").orElse(""))) {
                try (InputStream byteStream = new ByteArrayInputStream(httpResponse.body());
                     InputStream gzipStream = new GZIPInputStream(byteStream);
                     Reader reader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8)) {
                    // 读取解压缩后的内容
                    StringBuilder result = new StringBuilder();
                    char[] buffer = new char[1024];
                    int length;
                    while ((length = reader.read(buffer)) != -1) {
                        result.append(buffer, 0, length);
                    }
                    responseJson = objectMapper.readTree(result.toString());
                }
            } else {
                responseJson = objectMapper.readTree(httpResponse.body());
            }
        } else {
            throw new RuntimeException("请求失败: " + httpResponse.statusCode());
        }
        return responseJson;
    }

    public void appendInfoLog(String message) {
        Platform.runLater(() -> {
            Text text = new Text(message + "\n");
            text.setStyle("-fx-fill: black;");
            this.logTextFlow.getChildren().add(text);
        });
    }

    public void appendErrorLog(String message) {
        Platform.runLater(() -> {
            Text text = new Text(message + "\n");
            text.setStyle("-fx-fill: red;");
            this.logTextFlow.getChildren().add(text);
        });
    }

}
