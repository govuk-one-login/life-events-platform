PASSWORD="$(cat encryption_key.txt)"

unzip -o -P "$PASSWORD" configuration/build/vpc.zip
unzip -o -P "$PASSWORD" configuration/dev/vpc.zip
unzip -o -P "$PASSWORD" configuration/integration/vpc.zip
unzip -o -P "$PASSWORD" configuration/production/vpc.zip
unzip -o -P "$PASSWORD" configuration/staging/vpc.zip
