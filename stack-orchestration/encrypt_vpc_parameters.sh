PASSWORD="$(cat encryption_key.txt)"

zip -er configuration/build/vpc.zip configuration/build/vpc -P "$PASSWORD"
zip -er configuration/dev/vpc.zip configuration/dev/vpc -P "$PASSWORD"
zip -er configuration/integration/vpc.zip configuration/integration/vpc -P "$PASSWORD"
zip -er configuration/production/vpc.zip configuration/production/vpc -P "$PASSWORD"
zip -er configuration/staging/vpc.zip configuration/staging/vpc -P "$PASSWORD"
