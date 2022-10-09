#include <esp_camera.h>
#include <WiFi.h>
#include <WebSocketsServer.h>

#include <FS.h>
#include <SD_MMC.h>
#include <EEPROM.h>

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

const int live_stream = 51;
const int sd_storage = 52;
const int settings = 53;

int screen_state = 0;

bool isConnected = false;
uint8_t clientId = 0;

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
        screen_state = 0;
        break;
      }
    case WStype_TEXT:
      {
        Serial.println("Received");
        int temp_value = (int)payload[0];
        if (temp_value == live_stream || temp_value == sd_storage || temp_value == settings) {
          screen_state = temp_value;
        } else {
        }
        Serial.println(temp_value);
        break;
      }
  }
}

void setup() {
  // put your setup code here, to run once:

  //WRITE_PERI_REG(RTL_CNTL_BROWN_OUT_REG, 0) // To disable brownout detector

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

  initWifi();
  if (!isWiFiConnected()) {
    return;
  }
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

void loop() {
  // put your main code here, to run repeatedly:
  webSocket.loop();
  if (isConnected && screen_state == live_stream) {
    sendImage();
    delay(100);
  }
}