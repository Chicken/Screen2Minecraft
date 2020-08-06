const robot = require("robotjs"),
      net = require("net"),
      config = require("./config.json"),
      client = net.connect(config.port, config.address);

let totalFramesSend = 0;

function sendScreen() {
    client.write(robot.screen.capture(0,0,config.screenWidth,config.screenHeight).image, ()=>{
        totalFramesSend++;
        sendScreen()
    });
}

sendScreen();
setInterval(()=>{
    console.log("Fps: ", (totalFramesSend/process.uptime()).toFixed(1));
}, 1000)