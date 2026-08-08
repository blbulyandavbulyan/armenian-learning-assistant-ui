config.resolve = config.resolve || {};
let osFallback = false;
let pathFallback = false;
try {
    osFallback = require.resolve("os-browserify/browser");
} catch (e) {
    console.log(`Exception while requiring os-browserify/browser: ${e}`);
}
try {
    pathFallback = require.resolve("path-browserify");
} catch (e) {
    console.log(`Exception while requiring path-browserify: ${e}`);
}

config.resolve.fallback = {
    ...config.resolve.fallback,
    "os": osFallback,
    "path": pathFallback,
    "fs": false
};
