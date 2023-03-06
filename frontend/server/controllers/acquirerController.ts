import type { Request, Response } from 'express'
import GdxDataShareClient from "../data/gdxDataShareClient";

export default class AcquirerController {
    constructor(private readonly gdxDataShareClient: GdxDataShareClient) {}

    async get(req: Request, res: Response): Promise<void> {
        res.render('/pages/addAcquirerForm')
    }

    async post(req: Request, res: Response): Promise<void> {
        const token = await this.gdxDataShareClient.getToken();
        const acquirer = {
            clientName: req.body.clientName,
            eventType: req.body.eventType,
            enrichmentFields: [req.body.enrichmentFields].flatMap(ef => ef === undefined ? [] : ef),
            enrichmentFieldsIncludedInPoll: req.body.enrichmentFieldsIncludedInPoll
        };
        const acquirerResponse = await this.gdxDataShareClient.postAcquirer(token, acquirer);

        res.locals.clientName = acquirerResponse.clientName;
        res.locals.clientId = acquirerResponse.clientId;
        res.locals.clientSecret = acquirerResponse.clientSecret;

        res.render('/pages/success')
    }
}
