import { request, RequestOptions } from "https"

export const makeRequest = async (
    url: string,
    options: RequestOptions,
    requestData: string,
): Promise<{ statusCode: number; responseBody: string }> =>
    new Promise((resolve, reject) => {
        const req = request(url, options, res => {
            res.setEncoding("utf8")
            let responseBody = ""

            res.on("data", chunk => {
                responseBody += chunk
            })

            res.on("end", () => {
                if (!res.statusCode || res.statusCode < 200 || res.statusCode >= 300) {
                    return reject({ statusCode: res.statusCode, responseBody: responseBody })
                }
                return resolve({ statusCode: res.statusCode, responseBody: responseBody })
            })
        })
        req.on("error", err => {
            reject(err)
        })

        req.write(requestData)
        req.end()
    })
