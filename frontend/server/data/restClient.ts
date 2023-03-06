import superagent from 'superagent'
import Agent, { HttpsAgent } from 'agentkeepalive'

import sanitiseError from '../sanitisedError'
import { ApiConfig } from '../config'

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

    async get({ path = null, query = '', headers = {}, responseType = '', raw = false }: GetRequest): Promise<unknown> {
        try {
            const result = await superagent
                .get(`${this.apiUrl()}${path}`)
                .agent(this.agent)
                .query(query)
                .auth(this.token, { type: 'bearer' })
                .set(headers)
                .responseType(responseType)
                .timeout(this.timeoutConfig())

            return raw ? result : result.body
        } catch (error) {
            const sanitisedError = sanitiseError(error)
            throw sanitisedError
        }
    }

    async post({
                   path = null,
                   headers = {},
                   responseType = '',
                   data = {},
                   raw = false,
               }: PostRequest = {}): Promise<unknown> {
        try {
            const result = await superagent
                .post(`${this.apiUrl()}${path}`)
                .send(data)
                .agent(this.agent)
                .auth(this.token, { type: 'bearer' })
                .set(headers)
                .responseType(responseType)
                .timeout(this.timeoutConfig())

            return raw ? result : result.body
        } catch (error) {
            const sanitisedError = sanitiseError(error)
            throw sanitisedError
        }
    }
}
