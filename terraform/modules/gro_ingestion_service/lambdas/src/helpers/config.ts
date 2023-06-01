export const config = {
    gdxUrl: process.env.GDX_URL ?? "",
    authUrl: process.env.AUTH_URL ?? "",
    clientId: process.env.CLIENT_ID,
    clientSecret: process.env.CLIENT_SECRET,
    tableName: process.env.TABLE_NAME ?? "",
    s3BucketArn: process.env.S3_BUCKET_ARN ?? "",
}
