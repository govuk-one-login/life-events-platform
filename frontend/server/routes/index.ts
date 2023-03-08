import {type RequestHandler, Router} from 'express'

import asyncMiddleware from '../middleware/asyncMiddleware'
import AcquirerController from "../controllers/acquirerController";
import GdxDataShareClient from "../data/gdxDataShareClient";

export default function routes(): Router {
    const router = Router()
    const get = (path: string | string[], handler: RequestHandler) => router.get(path, asyncMiddleware(handler))
    const post = (path: string | string[], handler: RequestHandler) => router.post(path, asyncMiddleware(handler))

    const acquirerController = new AcquirerController(new GdxDataShareClient())

    get('/', (req, res) => {
        res.render('pages/index')
    })

    get('/success', (req, res) => {
        res.render('pages/success')
    })

    get('/add-acquirer', (req, res) => {
        res.render('pages/addAcquirerForm')
    })

    post('/add-acquirer', (req, res) => {
        acquirerController.post(req, res)
    })
    return router
}
