#include <esp_camera.h>
#include <WiFi.h>
#include <WebSocketsServer.h>

#include <FS.h>
#include <SD_MMC.h>
#include <EEPROM.h>

#include <soc/soc.h>
#include <soc/rtc_cntl_reg.h>
#include <driver/rtc_io.h>

#define EEPROM_SIZE 1

#define PWDN_GPIO_NUM 32
#define RESET_GPIO_NUM -1
#define XCLK_GPIO_NUM 0
#define SIOD_GPIO_NUM 26
#define SIOC_GPIO_NUM 27

#define Y9_GPIO_NUM 35
#define Y8_GPIO_NUM 34
#define Y7_GPIO_NUM 39
#define Y6_GPIO_NUM 36
#define Y5_GPIO_NUM 21
#define Y4_GPIO_NUM 19
#define Y3_GPIO_NUM 18
#define Y2_GPIO_NUM 5
#define VSYNC_GPIO_NUM 25
#define HREF_GPIO_NUM 23
#define PCLK_GPIO_NUM 22

WebSocketsServer webSocket(81);

const char *ssid = "UmmmAde";
const char *password = "20120527385";

const char *live_stream = "3";
const char *sd_storage = "4";
const char *settings = "5";
const char *capture = "6";
const char *openImage = "7";

String screen_state = "0";

bool isConnected = false;
bool sdCardConnected = false;
uint8_t clientId = 0;

String storage = "";

int pitchureNo = 0;

IPAddress local_IP(192, 168, 0, 180);
IPAddress geteway(192, 168, 0, 1);
IPAddress subnet(255, 255, 0, 0);

IPAddress primaryDNS(8, 8, 8, 8);
IPAddress secondaryDNS(8, 8, 4, 4);

void webSocketEvent(uint8_t num, WStype_t type, uint8_t *payload, size_t length) {
  switch (type) {
    case WStype_CONNECTED:
      {
        Serial.println("Connected");
        isConnected = true;
        clientId = num;
        break;
      }
    case WStype_DISCONNECTED:
      {
        Serial.println("Disconnected");
        isConnected = false;
        clientId = num;
        screen_state = "0";
        break;
      }
    case WStype_TEXT:
      {
        String temp_value = (char *)payload;
        Serial.println("Received " + String(temp_value));
        if (temp_value == live_stream || temp_value == settings) {
          screen_state = temp_value;
        } else if (temp_value == sd_storage) {
          screen_state = temp_value;
          fetchStorageInfo();
        } else {
          if (temp_value == capture) {
            captureImage();
          } else if (strstr(temp_value.c_str(), String(openImage).c_str())) {
            fetchImage((char *)payload);
          }
        }
        break;
      }
  }
}

void setup() {
  // put your setup code here, to run once:

  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0);  //disable brownout detector

  Serial.begin(115200);

  camera_config_t config;

  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;

  if (psramFound()) {
    config.frame_size = FRAMESIZE_SXGA;
    config.jpeg_quality = 10;
    config.fb_count = 2;
  } else {
    config.frame_size = FRAMESIZE_SVGA;
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }

  pinMode(4, OUTPUT);
  digitalWrite(4, LOW);

  initWifi();
  if (!isWiFiConnected()) {
    return;
  }
  mountSdCard();
  EEPROM.begin(EEPROM_SIZE);
  initWebSocket();

  esp_err_t cameraState = esp_camera_init(&config);
  if (cameraState != ESP_OK) {
    Serial.println("Camera initialization failed");
    Serial.print(cameraState);
    return;
  }
  delay(1000);
}

void sendImage() {
  camera_fb_t *frameBuffer = NULL;
  frameBuffer = esp_camera_fb_get();
  if (!frameBuffer) {
    Serial.println("Failed to capture");
  }
  webSocket.sendBIN(clientId, frameBuffer->buf, frameBuffer->len);
  esp_camera_fb_return(frameBuffer);
}

void initWifi() {

  if (!WiFi.config(local_IP, geteway, subnet, primaryDNS, secondaryDNS)) {
    Serial.println("Failure to configure WiFi");
  }

  Serial.println("Connecting to " + String(ssid));
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  int count = 0;

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (count > 25) {
      Serial.println("Connection failed");
      return;
    }
    count++;
  }
  Serial.println("");
  Serial.print("Connecting to IP : ");
  String IP = WiFi.localIP().toString();
  Serial.println(IP);
}

bool isWiFiConnected() {
  return WiFi.status() == WL_CONNECTED;
}

void initWebSocket() {
  webSocket.begin();
  webSocket.onEvent(webSocketEvent);
}

void mountSdCard() {
  if (!SD_MMC.begin()) {
    Serial.println("SD card mount failed");
  }
  uint8_t cardType = SD_MMC.cardType();
  Serial.println("Card type " + String(cardType));
  if (cardType != CARD_NONE) {
    sdCardConnected = true;
  }
}

void captureImage() {
  if (sdCardConnected) {
    Serial.println("Capturing image");
    camera_fb_t *frameBuffer = NULL;
    frameBuffer = esp_camera_fb_get();
    if (!frameBuffer) {
      Serial.println("Failed to capture");
    }
    pitchureNo = EEPROM.read(0) + 1;
    String imagePath = "/image/image" + String(pitchureNo) + ".jpg";

    fs::FS &fs = SD_MMC;

    File file = fs.open(imagePath.c_str(), FILE_WRITE);
    if (!file) {
      Serial.println("Failed to open file");
    } else {
      file.write(frameBuffer->buf, frameBuffer->len);
      EEPROM.write(0, pitchureNo);
      EEPROM.commit();
      Serial.println("Image has been captured");
    }
    file.close();

    esp_camera_fb_return(frameBuffer);
    // rtc_gpio_hold_en(GPIO_NUM_4);
  }
}

void fetchImage(char *imagePathData) {
  char *state = strtok(imagePathData, ",");
  char *path = strtok(NULL, ",");
  Serial.println("Fetch image" + String(state) + " " + String(path));
  File file = SD_MMC.open(path, FILE_READ);
  if (!file) {
    Serial.println("Unable to open file/directory");
    return;
  }

  webSocket.sendTXT(clientId, openImage);
  uint8_t *fileBuffer;
  unsigned int fileSize = file.size();
  fileBuffer = (uint8_t *)malloc(fileSize + 1);
  file.read(fileBuffer, fileSize);
  fileBuffer[fileSize] = '\0';
  webSocket.sendBIN(clientId, fileBuffer, fileSize);
  file.close();
  free(fileBuffer);
}

void fetchStorageInfo() {
  storage = "";
  fs::FS &fs = SD_MMC;
  listDirectories(fs, "/");
  storage = storage + "{\"total_space\" : \"" + String(SD_MMC.totalBytes()) + "\",\"used_space\" : \"" + String(SD_MMC.usedBytes()) + "\"}";
  if (isConnected) {
    if (screen_state == sd_storage) {
      webSocket.sendTXT(clientId, storage);
    }
  }
}

void listDirectories(fs::FS &fs, String directory) {
  File root = fs.open(directory);
  if (!root) {
    Serial.println("Failed to open root file");
  }
  if (!root.isDirectory()) {
    Serial.println("Root file is not a directory");
    return;
  }
  File file = root.openNextFile();
  while (file) {
    if (screen_state != sd_storage) {
      break;
    }
    if (String(file.name()) == "") {
      break;
    }
    if (file.isDirectory()) {
      listDirectories(SD_MMC, "/" + String(file.name()));
    } else {
      storage = storage + "{\"file\" : \"" + String(file.path()) + "\" ,\"size\" : \"" + String(file.size()) + "\"}|";
    }
    file = root.openNextFile();
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  webSocket.loop();
  if (isConnected) {
    if (screen_state == live_stream) {
      sendImage();
      delay(100);
    }
  }
}