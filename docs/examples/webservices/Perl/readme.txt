In order to call JBEI Registry webservices from Perl you have to install few libraries.

1. SOAP library - to be able to handle SOAP messages
$ sudo apt-get install libsoap-lite-perl

2. SSL library - to be able to handle call over SSL
$ sudo apt-get install libnet-ssleay-perl
$ sudo apt-get install libcrypt-ssleay-perl

3. Install SOAP::WSDL Perl package and all required dependencies
Call this:
$ sudo perl -MCPAN -e shell

and then call install this:
$ cpan> install SOAP::WSDL

4. Modify generate_classes.pl file if needed and make it executable chmod u+x generate_classes.pl 

5. Run ./generate_class.pl

6. Update $login and $password variable according to your JBEI registry credentials and make test.pl executable - chmod u+x test.pl

7. Run ./test.pl 