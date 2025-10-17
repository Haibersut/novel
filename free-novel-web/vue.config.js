const { defineConfig } = require('@vue/cli-service')
const WebpackObfuscator = require('webpack-obfuscator');
module.exports = defineConfig({
    devServer: {
        host: '0.0.0.0', // 允许外部访问
        port: 8080,
        allowedHosts: 'all',
        headers: {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
            "Access-Control-Allow-Headers": "X-Requested-With, content-type, Authorization"
        },
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                pathRewrite: {
                    '^/api': ''
                }
            }
        }
    },
    transpileDependencies: true,
    productionSourceMap: false,
configureWebpack: config => {
    }
})