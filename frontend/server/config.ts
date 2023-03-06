const production = process.env.NODE_ENV === 'production'

function get<T>(name: string, fallback: T, options = { requireInProduction: false }): T | string {
    if (process.env[name]) {
        return process.env[name]
    }
    if (fallback !== undefined && (!production || !options.requireInProduction)) {
        return fallback
    }
    throw new Error(`Missing env var ${name}`)
}

const requiredInProduction = { requireInProduction: true }

export class AgentConfig {
    timeout: number

    constructor(timeout = 8000) {
        this.timeout = timeout
    }
}

export interface ApiConfig {
    url: string
    timeout: {
        response: number
        deadline: number
    }
    agent: AgentConfig
}

export default {
    staticResourceCacheDuration: 20,
    apis: {
        gdxDataSharePoc: {
            url: get('GDX_DATA_SHARE_URL', 'http://127.0.0.1:63459', requiredInProduction),
            timeout: {
                response: Number(get('GDX_DATA_SHARE_TIMEOUT_RESPONSE', 10000)),
                deadline: Number(get('GDX_DATA_SHARE_TIMEOUT_DEADLINE', 10000)),
            },
            agent: new AgentConfig(Number(get('GDX_DATA_SHARE_TIMEOUT_RESPONSE', 10000))),
        },
        gdxDataShareAuth: {
            url: get('GDX_DATA_SHARE_AUTH_URL', 'http://localhost:9090', requiredInProduction),
            token: get('GDX_DATA_SHARE_AUTH_TOKEN', 'Basic Y2xpZW50OnNlY3JldA==', requiredInProduction),
        }
    },
}
