const {createProxyMiddleware} = require('http-proxy-middleware')

module.exports = function (app){
    app.use(
        '/marks/api',
        createProxyMiddleware({
            target: 'http://localhost:8080',
            changeOrigin: true,
            pathRewrite: path => path.slice(6),
            headers: {
                // 'X-Forwarded-Mail': 'marvin.kramer@ergon.ch' //apprentice
                // 'X-Forwarded-Mail': 'thomas.boksberger@ergon.ch' //supervisor
                'X-Forwarded-Mail': 'bernd.lienberger@ergon.ch' //admin
                // 'X-Forwarded-Mail': 'unknown.user@ergon.ch'
            }
        })
    )
}