const robot = require("robotjs");
const net = require("net");

let client = net.connect(1337, "localhost")

setInterval(()=>{
    //timing
    let start = Date.now();

    //get screen capture
    let ss = robot.screen.capture();

    client.write(ss.image)

    //timing
    let elapsed = Date.now()-start;
    //logging
    console.log(`Finished in ${elapsed}ms.`);
}, 1000)