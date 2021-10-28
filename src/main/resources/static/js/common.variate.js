//变量定义
var websocket = null;
var div = '</div></div>';
var host = "ws://localhost:8900/chat";
var focus = false;
var msgSwitchTips = '点击可开启/关闭消息通知';
var clearTips = '清屏！';
var sendTips = '点击发送消息(回车也可发送消息)';
var onerrorMsg = "与服务器连接发生错误，请刷新页面重新进入！";
var oncloseMsg = '已与服务器断开连接！';
var unSupportWsMsg = "当前浏览器不支持 WebSocket";

