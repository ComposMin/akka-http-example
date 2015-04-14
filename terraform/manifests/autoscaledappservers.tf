
# Specify the provider and access details
provider "aws" {
    access_key = "${var.aws_access_key}"
    secret_key = "${var.aws_secret_key}"
    region = "ap-southeast-2"
}


module "capability_dev1_net" {
    source = "../modules/basic_vpc"

    capability_name = "${var.capability_name}"
    capability_vpc_cidr = "${var.capability_vpc_cidr}"
    capability_subnet_a_cidr = "${var.capability_subnet_a_cidr}"
    capability_subnet_b_cidr = "${var.capability_subnet_b_cidr}"
}

resource "aws_security_group" "wide_open" {
  name = "wide_open"
  description = "Allow any protocol"
  vpc_id = "${module.capability_dev1_net.vpc_id}"

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
  vpc_id = "${module.capability_dev1_net.vpc_id}"

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
  vpc_id = "${module.capability_dev1_net.vpc_id}"

  ingress {
      from_port = 80
      to_port = 80
      protocol = "-1"    #Any protocol
      cidr_blocks = ["0.0.0.0/0"]
  }
}

###---- TODO: Push the above into a standard module, so that it can be reused

resource "aws_route53_record" "capability_www_dns" {
   zone_id = "${var.dns_zone_id}"
   name = "${var.www_dns_prefix}.{var.www_dns_suffix}"
   type = "CNAME"
   ttl = "60"
   records = ["${aws_elb.capability_www.dns_name}"]
}

resource "aws_elb" "capability_www" {
  name = "webapplicationentry"
  # Attaching subnets determines which VPC the ELB ends up in. Do it! Or it ends up in the default VPC
  subnets = [ "${module.capability_dev1_net.subnet_a_id}", "${module.capability_dev1_net.subnet_b_id}" ]
  security_groups = [ "${aws_security_group.web_standard_ports.id}" ]

  listener {
    instance_port = 8080
    instance_protocol = "http"
    lb_port = 80
    lb_protocol = "http"
  }
  health_check {
    healthy_threshold = 2
    unhealthy_threshold = 10
    timeout = 3
    target = "HTTP:8080/status"
    interval = 300
  }
}

resource "aws_launch_configuration" "as_conf" {
    name = "small_app_server_cluster"
    image_id = "${var.app_server_ami}"
    instance_type = "t2.small"
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
  vpc_zone_identifier = [ "${module.capability_dev1_net.subnet_a_id}", "${module.capability_dev1_net.subnet_b_id}" ]
}

