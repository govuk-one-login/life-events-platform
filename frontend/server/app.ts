import express from 'express'

import path from 'path'
import createError from 'http-errors'

import nunjucksSetup from './utils/nunjucksSetup'

import setUpStaticResources from './middleware/setUpStaticResources'
import setUpWebRequestParsing from './middleware/setupRequestParsing'

import routes from './routes'

export default function createApp(): express.Application {
    const app = express()

    app.set('port', process.env.PORT || 3000)

    app.use(setUpWebRequestParsing())
    app.use(setUpStaticResources())
    nunjucksSetup(app, path)

    app.use(routes())

    app.use((req, res, next) => next(createError(404, 'Not found')))

    return app
}
