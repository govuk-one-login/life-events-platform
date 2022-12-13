locals {
  waf_acl_name       = "${var.environment}-gdx-data-share-poc"
  waf_acl_name_short = join("", split("-", local.waf_acl_name))

  # Check the contents using both HTML-encoded and URL-encoded versions
  # Applies to the SQL Injection rules
  waf_rules_transforms = [
    "HTML_ENTITY_DECODE",
    "URL_DECODE",
  ]
  waf_rules_fields = [
    "BODY",
    "HEADER.cookie",
    "QUERY_STRING",
    "URI",
  ]
  waf_transform_field_pairs = setproduct(local.waf_rules_transforms, local.waf_rules_fields)

  waf_rules_max_sizes = {
    "BODY"          = 65536,
    "HEADER.cookie" = 8192,
    "QUERY_STRING"  = 1024,
    "URI"           = 1024,
  }

  blocked_ips    = ["94.9.95.206/32", "86.107.21.87/32", "170.39.116.186/32"]
}

resource "aws_waf_web_acl" "gdx_data_share_poc" {
  name        = local.waf_acl_name
  metric_name = local.waf_acl_name_short

  default_action {
    type = "ALLOW"
  }

  dynamic "rules" {
    for_each = [
      {
        id     = aws_waf_rule.max_size.id
        type   = "REGULAR"
        action = "BLOCK"
      },
      {
        id     = aws_waf_rule.sql_injection.id
        type   = "REGULAR"
        action = "BLOCK"
      },
      {
        id     = aws_waf_rule.blocked_ips.id
        type   = "REGULAR"
        action = "BLOCK"
      },
    ]
    iterator = rule
    content {
      action {
        type = rule.value.action
      }
      rule_id  = rule.value.id
      type     = rule.value.type
      priority = rule.key + 1
    }
  }

  depends_on = [
    aws_waf_rule.max_size,
    aws_waf_rule.sql_injection,
    aws_waf_rule.blocked_ips,
  ]
}

resource "aws_waf_rule" "sql_injection" {
  name        = "${local.waf_acl_name}-sql-injection-rule"
  metric_name = "${local.waf_acl_name_short}sqlinjectionrule"

  predicates {
    data_id = aws_waf_sql_injection_match_set.sql.id
    negated = false
    type    = "SqlInjectionMatch"
  }
}

resource "aws_waf_rule" "max_size" {
  name        = "${local.waf_acl_name}-max-field-size-rule"
  metric_name = "${local.waf_acl_name_short}maxfieldsizesrule"

  predicates {
    data_id = aws_waf_size_constraint_set.max_size.id
    negated = false
    type    = "SizeConstraint"
  }
}

resource "aws_waf_sql_injection_match_set" "sql" {
  name = "${local.waf_acl_name}-sql"

  dynamic "sql_injection_match_tuples" {
    # Create a match for every combination of transform and request field
    for_each = local.waf_transform_field_pairs
    iterator = transform_field_pair
    content {
      text_transformation = transform_field_pair.value[0]
      field_to_match {
        type = split(".", transform_field_pair.value[1])[0]
        data = length(split(".", transform_field_pair.value[1])) > 1 ? split(".", transform_field_pair.value[1])[1] : ""
      }
    }
  }
}

resource "aws_waf_size_constraint_set" "max_size" {
  name = "${local.waf_acl_name}-max-size"

  dynamic "size_constraints" {
    for_each = local.waf_rules_max_sizes
    iterator = size
    content {
      text_transformation = "NONE"
      comparison_operator = "GT"
      size                = size.value
      field_to_match {
        type = split(".", size.key)[0]
        data = length(split(".", size.key)) > 1 ? split(".", size.key)[1] : ""
      }
    }
  }
}

resource "aws_waf_ipset" "blocked_ipset" {
  name = "${local.waf_acl_name}-blocked-ipset"

  dynamic "ip_set_descriptors" {
    for_each = local.blocked_ips
    iterator = cidr

    content {
      type  = "IPV4"
      value = cidr.value
    }
  }
}

resource "aws_waf_rule" "blocked_ips" {
  depends_on  = [aws_waf_ipset.blocked_ipset]
  name        = "${local.waf_acl_name}-blocked-ips-rule"
  metric_name = "${local.waf_acl_name_short}blockedipsrule"

  predicates {
    data_id = aws_waf_ipset.blocked_ipset.id
    negated = false
    type    = "IPMatch"
  }
}