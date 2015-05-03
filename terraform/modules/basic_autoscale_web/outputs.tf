
output "elb_public_name" {
  value = "${aws_elb.capability_www.dns_name}"
}

output "route53_name" {
  value = "${aws_route53_record.capability_www_dns.name}"
}
