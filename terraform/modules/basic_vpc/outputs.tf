
output "vpc_id" {
    value = "${aws_vpc.capability_vpc.id}"
}

output "subnet_a_id" {
    value = "${aws_subnet.capability_subnet_a.id}"
}

output "subnet_b_id" {
    value = "${aws_subnet.capability_subnet_b.id}"
}