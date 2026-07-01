config.devServer = config.devServer || {};
config.devServer.proxy = [
  {
    context: ['/chat', '/dialogues', '/assets'],
    target: process.env.BACKEND_URL || 'http://localhost:8080',
    secure: false,
    changeOrigin: true
  }
];
