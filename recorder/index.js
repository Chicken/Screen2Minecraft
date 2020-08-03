const robot = require("robotjs"),
net = require("net"),
config = require("./config.json"),
client = net.connect(config.port, config.address);
config.screenWidth = config.screenWidth || robot.getScreenSize().width
config.screenHeight = config.screenHeight || robot.getScreenSize().height
console.log(`Sending screenshots to ${config.address}:${config.port}.
${config.screenWidth}x${config.screenHeight} at ${config.fps}fps.${config.debug ? "\nDebug mode is enabled" : ""}`)
setInterval(() => {
	let start = Date.now();
	let ss = robot.screen.capture(0, 0, config.screenWidth, config.screenHeight);
	client.write(ss.image);
	let elapsed = Date.now() - start;
	console.log(`Finished in ${elapsed}ms.`);
	if (config.debug) console.log(ss);
}, 1000 / config.fps)
