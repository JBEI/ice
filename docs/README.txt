DEVELOPER FAQ (v3.3)
====================

Q. How do I change the logo and application title header?
A. Replace src/main/webapp/static/images/logo.png and change the title in src/main/webapp/Gwt_ice.html. These
   can both be done on a live site without rebuilding

Q. Why is JbeirSettings deprecated?
A. The site settings are now stored in the database. When upgrading from 3.1, the system reads the values from
   jbeir.properties. You can modify it on the admin section of the site. After upgrading, I recommend verifying
   all the settings to make sure they are set correctly.

Q. How do I change the authentication mechanism?
A. Coming soon...spring.

Q. Can I upgrade from previous versions?
A. Upgrade from v3.1 is supported. Contact me if you wish to upgrade from older versions.

Q. Can I submit code or bug fixes?
A. Yes.

Q. I get compiler errors with GWT.
A. Delete src/main/gwt-unitCache if it exists and try again. Contact me with the specific errors if it persists.

Q. Can I use the registry without any authentication?
A. Not without non-trivial modifications.

Q. What application servlet containers are supported.
A. Tomcat and jetty. It should run on other containers that implement the servlet spec but it has not been tested.

Q. Where can I find more detailed installation instructions?
A. Please refer to our Manual: https://public-registry.jbei.org/site/docbkx/html/manual/manual.html. It is in the
   process of being updated.
