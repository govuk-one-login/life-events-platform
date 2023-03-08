import superagent from 'superagent'
import Agent, {HttpsAgent} from 'agentkeepalive'

import sanitiseError from '../sanitisedError'
import {ApiConfig} from '../config'

interface GetRequest {
    path?: string
    query?: string
    headers?: Record<string, string>
    responseType?: string
    raw?: boolean
}

interface PostRequest {
    path?: string
    headers?: Record<string, string>
    responseType?: string
    data?: Record<string, unknown>
    raw?: boolean
}

export default class RestClient {
    agent: Agent

    constructor(private readonly name: string, private readonly config: ApiConfig, private readonly token: string) {
        this.agent = config.url.startsWith('https') ? new HttpsAgent(config.agent) : new Agent(config.agent)
    }

    private apiUrl() {
        return this.config.url
    }

    private timeoutConfig() {
        return this.config.timeout
    }

    async get(
        {
            path = null,
            query = '',
            headers = {},
            responseType = '',
            raw = false
        }: GetRequest): Promise<superagent.Response | unknown> {
        const request = superagent
            .get(`${this.apiUrl()}${path}`)
            .query(query)
        return await this.makeRequest(request, headers, responseType, raw)
    }

    async post(
        {
            path = null,
            headers = {},
            responseType = '',
            data = {},
            raw = false,
        }: PostRequest = {}): Promise<superagent.Response | unknown> {
        const request = superagent
            .post(`${this.apiUrl()}${path}`)
            .send(data)
        return await this.makeRequest(request, headers, responseType, raw)
    }

    async makeRequest(
        request: superagent.SuperAgentRequest,
        headers = {},
        responseType: string,
        raw: boolean): Promise<superagent.Response | unknown> {
        try {
            const result = await request.agent(this.agent)
                .auth(this.token, {type: "bearer"})
                .set(headers)
                .responseType(responseType)
                .timeout(this.timeoutConfig())

            return raw ? result : result.body
        } catch (error) {
            throw sanitiseError(error)
        }
    }
}
