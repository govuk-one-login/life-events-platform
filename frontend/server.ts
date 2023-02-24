import { app } from './server/index'

app.listen(app.get('port'), () => {
    console.info(`Server listening on port ${app.get('port')}`)
})
