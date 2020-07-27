const robot = require("robotjs");
const net = require("net");

let client = net.connect(1337, "localhost")

setInterval(()=>{
    let start = Date.now();
    let ss = robot.screen.capture();
    client.write(ss.image)
    let elapsed = Date.now()-start;
    console.log(`Finished in ${elapsed}ms. Extra data:`);
    console.log(ss.colorAt(0, 0));
}, 1000)