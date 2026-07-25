// 1. Increase Karma's browser ping & disconnect timeouts (default is 2000ms!)
config.browserDisconnectTimeout = 30000;
config.browserNoActivityTimeout = 60000;
config.browserDisconnectTolerance = 3;
config.pingTimeout = 30000;

// 2. Tweak Chrome launcher to prevent Wasm/Skiko memory crashes in headless mode
config.customLaunchers = config.customLaunchers || {};
config.customLaunchers.ChromeHeadlessNoSandbox = {
    base: 'ChromeHeadless',
    flags: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage', // Uses /tmp instead of /dev/shm (prevents OOM crashes in containers/Linux)
        '--js-flags=--max-old-space-size=4096' // Gives V8 enough memory for Wasm compilation
    ]
};
