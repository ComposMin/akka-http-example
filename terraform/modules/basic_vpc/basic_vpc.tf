
# VPC, which /24 network range
# Two subnets, in distinct availability zones

resource "aws_vpc" "capability_vpc" {
    cidr_block = "${var.capability_vpc_cidr}"
    enable_dns_support = true
    enable_dns_hostnames = true

    tags {
        Name = "${var.capability_name}_vpc"
    }
}

resource "aws_internet_gateway" "capability_igw" {
    vpc_id = "${aws_vpc.capability_vpc.id}"
}

resource "aws_subnet" "capability_subnet_a" {
    vpc_id = "${aws_vpc.capability_vpc.id}"
    cidr_block = "${var.capability_subnet_a_cidr}"
    availability_zone = "ap-southeast-2a"
    # Set to false for Corpname scenarios, where external network connectivity not allowed
    map_public_ip_on_launch = false

    tags {
        Name = "${var.capability_name}_subnet_a"
    }
}

resource "aws_subnet" "capability_subnet_b" {
    vpc_id = "${aws_vpc.capability_vpc.id}"
    cidr_block = "${var.capability_subnet_b_cidr}"
    availability_zone = "ap-southeast-2b"
    # Set to false for Corpname scenarios, where external network connectivity not allowed
    map_public_ip_on_launch = false

    tags {
        Name = "${var.capability_name}_subnet_b"
    }
}

resource "aws_route_table" "capability_routetab" {
    vpc_id = "${aws_vpc.capability_vpc.id}"
    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = "${aws_internet_gateway.capability_igw.id}"
    }
}

resource "aws_route_table_association" "capability_routeassoc_1" {
    subnet_id = "${aws_subnet.capability_subnet_a.id}"
    route_table_id = "${aws_route_table.capability_routetab.id}"
}

resource "aws_route_table_association" "capability_routeassoc_2" {
    subnet_id = "${aws_subnet.capability_subnet_b.id}"
    route_table_id = "${aws_route_table.capability_routetab.id}"
}

output "vpc_id" {
    value = "${aws_vpc.capability_vpc.id}"
}

output "subnet_a_id" {
    value = "${aws_subnet.capability_subnet_a.id}"
}

output "subnet_b_id" {
    value = "${aws_subnet.capability_subnet_b.id}"
}
