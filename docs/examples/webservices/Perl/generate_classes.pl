#!/usr/bin/perl
use strict;
use warnings;

my $WSDL_URL = "https://registry.jbei.org/api/EntryService?wsdl";
my $PATH_TO_WSDL_CLASSES_GENERATOR = "/usr/local/bin/wsdl2perl.pl";

my $command = "$PATH_TO_WSDL_CLASSES_GENERATOR -b . -p=JBEI $WSDL_URL";

system("/usr/bin/perl $command");
