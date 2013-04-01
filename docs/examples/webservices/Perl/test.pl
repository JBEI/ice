#/bin/perl
use strict;
use warnings;

use SOAP::Lite +trace => debug => sub {};
use JBEIInterfaces::RegistryAPIService::RegistryAPIPort;

# Uncomment next line for SSL debug info
#$ENV{HTTPS_DEBUG} = 1;

my $SERVICE_URL = 'https://registry.jbei.org/api/RegistryAPI';

my $service = JBEIInterfaces::RegistryAPIService::RegistryAPIPort->new({ proxy => $SERVICE_URL });

my $login = 'YOUR_USER_NAME_GOES_HERE!';
my $password = 'YOUR_PASSWORD_GOES_HERE';

# SERVICE CALL: login()
my $session_id = $service->login({login => $login, password => $password})->get_return();
print $session_id;

# SERVICE CALL: getNumberOfPublicEntries()
print $service->getNumberOfPublicEntries()->get_return();