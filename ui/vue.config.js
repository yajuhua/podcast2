const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true
  // devServer: {
  //   port: 7070,
  //   proxy: {
  //     '/api': {
  //       target: 'http://localhost:8088',
  //       pathRewrite: {
  //         '^/api': ''
  //       }
  //     }
  //   }
  // }
})