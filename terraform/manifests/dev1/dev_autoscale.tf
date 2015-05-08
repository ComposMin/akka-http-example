
# Specify the provider and access details
provider "aws" {
    access_key = "${var.aws_access_key}"
    secret_key = "${var.aws_secret_key}"
    region = "ap-southeast-2"
}


module "capability_net" {
    source = "../../modules/basic_vpc"

    capability_name = "${var.capability_name}"
    capability_vpc_cidr = "${var.capability_vpc_cidr}"
    capability_subnet_a_cidr = "${var.capability_subnet_a_cidr}"
    capability_subnet_b_cidr = "${var.capability_subnet_b_cidr}"
}

module "capability_autoscale_web" {
    source = "../../modules/basic_autoscale_web"

    vpc_id = "${module.capability_net.vpc_id}"
    subnet_a_id = "${module.capability_net.subnet_a_id}"
    subnet_b_id = "${module.capability_net.subnet_b_id}"

    app_server_ami = "${var.app_server_ami}"
    ssh_key_name = "${var.ssh_key_name}"
    dns_zone_id = "${var.dns_zone_id}"
    www_dns_prefix = "${var.www_dns_prefix}"
    www_dns_suffix = "${var.www_dns_suffix}"
}
