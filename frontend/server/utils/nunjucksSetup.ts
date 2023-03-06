import nunjucks from 'nunjucks'
import express from 'express'
import * as pathModule from 'path'

const production = process.env.NODE_ENV === 'production'

export default function nunjucksSetup(app: express.Express, path: pathModule.PlatformPath): void {
    app.set('view engine', 'njk')

    app.locals.asset_path = '/assets/'
    app.locals.applicationName = 'GDX Acquirer/Supplier UI'

    // Cachebusting version string
    if (production) {
        // Version only changes on reboot
        app.locals.version = Date.now().toString()
    } else {
        // Version changes every request
        app.use((req, res, next) => {
            res.locals.version = Date.now().toString()
            return next()
        })
    }

    nunjucks.configure(
        [
            path.join(__dirname, '../../server/views'),
            'node_modules/govuk-frontend/',
            'node_modules/govuk-frontend/components/',
        ],
        {
            autoescape: true,
            express: app,
        },
    )
}
