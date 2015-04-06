# VPC, which /24 network range
# Two subnets, in distinct availability zones

# Specify the provider and access details
provider "aws" {
    access_key = "${var.aws_access_key}"
    secret_key = "${var.aws_secret_key}"
    region = "ap-southeast-2"
}

resource "aws_vpc" "capability_vpc" {
    cidr_block = "${var.capability_vpc_cidr}"
    enable_dns_support = true
    enable_dns_hostnames = true

    tags {
        Name = "${var.capability_name}_vpc"
    }
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

# Create a peering pcx - might need to be done as a second phase
# and then added to the routing table
#

resource "aws_route_table" "capability_routetab" {
    vpc_id = "${aws_vpc.capability_vpc.id}"
# TODO: Fill in route details after VPC Peering done and PCX value known
#    route {
#        cidr_block = "0.0.0.0/0"
#        # Route all traffic out via the parents pcx
#        gateway_id = "${var.parent_external_pcx}"
#    }
}

resource "aws_route_table_association" "capability_routeassoc_1" {
    subnet_id = "${aws_subnet.capability_subnet_a.id}"
    route_table_id = "${aws_route_table.capability_routetab.id}"
}

resource "aws_route_table_association" "capability_routeassoc_2" {
    subnet_id = "${aws_subnet.capability_subnet_b.id}"
    route_table_id = "${aws_route_table.capability_routetab.id}"
}

resource "aws_security_group" "wide_open" {
  name = "wide_open"
  description = "Allow any protocol"
  vpc_id = "${aws_vpc.capability_vpc.id}"

  ingress {
      from_port = 0
      to_port = 65535
      protocol = "-1"    #Any protocol
      cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ssh_from_anywhere" {
  name = "ssh_from_anywhere"
  description = "Allow SSH from anywhere"
  vpc_id = "${aws_vpc.capability_vpc.id}"

  ingress {
      from_port = 22
      to_port = 22
      protocol = "-1"    #Any protocol
      cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "web_standard_ports" {
  name = "web_standard_ports"
  description = "Allow standard web ports from anywhere"
  vpc_id = "${aws_vpc.capability_vpc.id}"

  ingress {
      from_port = 80
      to_port = 80
      protocol = "-1"    #Any protocol
      cidr_blocks = ["0.0.0.0/0"]
  }
}

###---- TODO: Push the above into a standard module, so that it can be reused

#resource "aws_route53_record" "capability_www_dns" {
#   zone_id = "Z2KQQ9G95L92Q3"   # TODO: make an input variable
#   name = "${var.capability_name}.composmin.net"
#   type = "CNAME"
#   ttl = "300"
#   records = ["${aws_elb.capability_www.dns_name}"]
#}

resource "aws_elb" "capability_www" {
  name = "webapplicationentry"
  # Attaching subnets determines which VPC the ELB ends up in. Do it! Or it ends up in the default VPC
  subnets = ["${aws_subnet.capability_subnet_a.id}", "${aws_subnet.capability_subnet_b.id}"]
  security_groups = ["${aws_security_group.web_standard_ports.id}"]

  # Corpname scenarios, external network connectivity not allowed directly from this ELB
  internal = true

  listener {
    instance_port = 8080
    instance_protocol = "http"
    lb_port = 80
    lb_protocol = "http"
  }
  health_check {
    healthy_threshold = 4
    unhealthy_threshold = 2
    timeout = 2
    target = "HTTP:8080/blah"
    interval = 30
  }
}

resource "aws_launch_configuration" "as_conf" {
    name = "small_app_server_cluster"
    image_id = "${var.app_server_ami}"
    instance_type = "t1.micro"
    key_name = "${var.ssh_key_name}"
    # TODO: Restrict to a more sensible sercurity group definition
    security_groups = [ "${aws_security_group.wide_open.id}" ]
}


resource "aws_autoscaling_group" "scaleout_web_app" {
  availability_zones = [ "ap-southeast-2a", "ap-southeast-2b" ]
  name = "autoscaledappservers"
  max_size = 1
  min_size = 1
  health_check_grace_period = 300
  health_check_type = "ELB"
  force_delete = true
  launch_configuration = "${aws_launch_configuration.as_conf.name}"
  load_balancers = [ "${aws_elb.capability_www.name}" ]
  vpc_zone_identifier = [ "${aws_subnet.capability_subnet_a.id}", "${aws_subnet.capability_subnet_b.id}" ]
}
