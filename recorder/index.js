const robot = require("robotjs"),
      net = require("net"),
      config = require("./config.json"),
      client = net.connect(config.port, config.address);

setInterval(()=>{
    let start = Date.now();
    let ss = robot.screen.capture(0,0,config.screenWidth,config.screenHeight);
    client.write(ss.image);
    let elapsed = Date.now()-start;
    console.log(`Finished in ${elapsed}ms.`);
    if(config.debug) console.log(ss);
}, 1000/config.fps)
