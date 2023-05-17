variable "queue_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "message_retention_seconds" {
  type        = number
  default     = 345600 # 4 days
  description = "Message retention for the main queue. Defaults to 4 days"
}

variable "dlq_message_retention_seconds" {
  type        = number
  default     = null
  description = <<-EOF
  Message retention for the dead letter queue.
  This must be higher than the main queue, as the original message timestamp is used.
  If not specified, twice main queue is used (capped at 14 days).
EOF
}

variable "queue_policy" {
  type        = string
  default     = null
  description = "Queue resource policy. Defaults to blocking HTTP traffic"
}
