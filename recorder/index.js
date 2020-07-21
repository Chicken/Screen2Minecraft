//libs
const robot = require("robotjs");
const net = require("net");

//connect client, localhost for testing
let client = net.connect(1337, "localhost")

setInterval(()=>{
    //timing
    let start = Date.now();

    //get screen capture
    let ss = robot.screen.capture();

    //write buffer to socket
    client.write(ss.image)

    //timing
    let elapsed = Date.now()-start;

    //logging
    console.log(`Finished in ${elapsed}ms.`);

//for testing 1 fps only
}, 1000)