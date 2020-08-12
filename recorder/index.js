const robot = require("robotjs"),
      net = require("net"),
      config = require("./config.json"),
      sharp = require('sharp'),
      client = net.connect(config.port, config.address);

client.write(Buffer.from(config.password + "\n"));
client.write(Buffer.from(config.targetHeight + "\n"));
client.write(Buffer.from(config.targetWidth + "\n"));
client.write(Buffer.from(config.world + "\n"));
client.write(Buffer.from(config.xoffset + "\n"));
client.write(Buffer.from(config.yoffset + "\n"));
client.write(Buffer.from(config.zoffset + "\n"));

function sendScreen() {
    sharp(robot.screen.capture(0,0,config.screenWidth,config.screenHeight).image, {
        raw: {
            width: config.screenWidth,
            height: config.screenHeight,
            channels: 4,
        }
    })
    .removeAlpha()
    .resize(config.targetWidth, config.targetHeight, {kernel: "nearest"})
    .toBuffer((err,data,info)=>{
        client.write(data);
    })
}

setInterval(()=>{
    sendScreen()
}, 1000/config.fps)