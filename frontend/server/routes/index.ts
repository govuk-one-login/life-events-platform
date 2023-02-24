import { type RequestHandler, Router } from 'express'

import asyncMiddleware from '../middleware/asyncMiddleware'

export default function routes(): Router {
    const router = Router()
    const get = (path: string | string[], handler: RequestHandler) => router.get(path, asyncMiddleware(handler))

    get('/', (req, res) => {
        res.render('pages/index')
    })

    return router
}
