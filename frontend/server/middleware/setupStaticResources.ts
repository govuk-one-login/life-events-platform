import path from 'path'
import express, { Router } from 'express'


export default function setUpStaticResources(): Router {
    const router = express.Router()

    //  Static Resources Configuration
    const cacheControl = { maxAge: 20000 }

    Array.of(
        '/assets',
        '/assets/stylesheets',
        '/assets/js',
        '/node_modules/govuk-frontend/govuk/assets',
        '/node_modules/govuk-frontend',
    ).forEach(dir => {
        router.use('/assets', express.static(path.join(process.cwd(), dir), cacheControl))
    })

    Array.of('/node_modules/govuk_frontend_toolkit/images').forEach(dir => {
        router.use('/assets/images/icons', express.static(path.join(process.cwd(), dir), cacheControl))
    })

    Array.of('/node_modules/jquery/dist/jquery.min.js').forEach(dir => {
        router.use('/assets/js/jquery.min.js', express.static(path.join(process.cwd(), dir), cacheControl))
    })

    return router
}
