# Read from DWP queue
aws --endpoint-url=http://localhost:4566 sqs \
  receive-message --queue-url http://localhost:4566/000000000000/dwp-event-queue

# Read from audit queue
aws --endpoint-url=http://localhost:4566 sqs \
  receive-message --queue-url http://localhost:4566/000000000000/b6ef7439-8fa6-48e4-9131-1887e09dde6d