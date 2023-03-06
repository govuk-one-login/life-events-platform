import superagent from "superagent";
import RestClient from "./restClient";
import config from "../config";

export type AcquirerRequest = {
    clientName: string
    eventType: string
    enrichmentFields: string[]
    enrichmentFieldsIncludedInPoll: boolean
}

export type AcquirerResponse = {
    clientName: string,
    clientId: string,
    clientSecret: string
}

export default class GdxDataShareClient {
    constructor() {}

    private static restClient(token: string): RestClient {
        return new RestClient('GDX Data Share Client', config.apis.gdxDataSharePoc, token)
    }

    postAcquirer(token: string, acquirer: AcquirerRequest): Promise<AcquirerResponse> {
        return GdxDataShareClient.restClient(token).post({ path: '/admin/acquirer', data: acquirer}) as Promise<AcquirerResponse>
    }

    async getToken(): Promise<string> {
        const tokenResponse = await superagent
            .post(`${config.apis.gdxDataShareAuth.url}/issuer1/token`)
            .set('Authorization', config.apis.gdxDataShareAuth.token)
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .send('grant_type=client_credentials')
            .send('scope=len');
        return tokenResponse.body.access_token;
    }
}
